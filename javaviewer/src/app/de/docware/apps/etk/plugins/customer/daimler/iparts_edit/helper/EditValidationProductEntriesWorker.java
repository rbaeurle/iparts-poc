/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.mechanic.AssemblyFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsEditBaseValidationForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.PictureAndTUGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.iPartsEditAssemblyListValidationOverlappingEntriesForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.iPartsFINValidationForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.iPartsValidationPictureAndTUForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.services.bomdb.kem.generated.Language;

/**
 * Helper für die Überprüfung aller TUs in einem Produkt im Hintergrund (ggf mit MultiThreading)
 */
public class EditValidationProductEntriesWorker implements iPartsConst {

    private AssemblyFormIConnector connector;
    private EtkProject project;
    private String viewerLanguage;
    private String dbLanguage;
    private List<String> fallbackLanguages;

    private StringBuilder extraNotes;
    private Set<String> extraNotesFromMessageLog;
    private RunTimeLogger runTimeLogger;
    private VarParam<Boolean> isCancelled;
    private boolean cancelStarted;
    private boolean isQualityCheckParallelActive;
    private ExecutorService executorService;
    private int threadCount;
    private CsvZipMultiExportWriter exportWriter;
    private EtkDisplayFields displayFieldsPictureAndTU;
    private Map<AssemblyId, List<String>> csvLinesPictureAndTUMap;
    private EtkDisplayFields displayFieldsColors;
    private Map<AssemblyId, List<String>> csvLinesColorsMap;
    private EtkDisplayFields displayFieldsFINCheck;
    private Map<AssemblyId, List<String>> csvLinesFINCheckMap;
    private EnumValue validationEnumValue;

    public EtkProject getValidationProject() {
        return project;
    }

    public String getExtraNotes() {
        return extraNotes.toString();
    }

    public boolean downloadFile() {
        if (exportWriter != null) {
            exportWriter.downloadExportFile();
            exportWriter.clearAfterDownload();
            exportWriter = null;
            return true;
        }
        return false;
    }

    protected void init(AssemblyFormIConnector connector, EtkProject project) {
        this.project = project;
        viewerLanguage = project.getViewerLanguage();
        dbLanguage = project.getDBLanguage();
        fallbackLanguages = project.getConfig().getDataBaseFallbackLanguages();
        this.connector = new EditModuleFormConnector(connector) {
            @Override
            public EtkProject getProject() {
                return getValidationProject();
            }
        };

        this.exportWriter = null;
        this.extraNotes = new StringBuilder();
        this.extraNotesFromMessageLog = new HashSet<>();
        this.isCancelled = new VarParam<>(false);
        this.csvLinesPictureAndTUMap = new TreeMap<>();
        this.csvLinesColorsMap = new TreeMap<>();
        this.csvLinesFINCheckMap = new TreeMap<>();
        this.displayFieldsPictureAndTU = null;
        this.displayFieldsColors = null;
        this.displayFieldsFINCheck = null;
        this.validationEnumValue = project.getEtkDbs().getEnumValue(ENUM_KEY_VALIDATION_RESULT);
    }


    /**
     * Haupt-Routine für die Überprüfung aller TUs in einem Produkt
     *
     * @param connector
     * @param project
     * @param assemblyId
     */
    public void startValidation(AssemblyFormIConnector connector, EtkProject project, AssemblyId assemblyId) {
        init(connector, project);
        if (assemblyId.isVirtual()) {
            // Produkt bestimmen
            String product = iPartsVirtualNode.getProductNumberFromAssemblyId(assemblyId);
            if (StrUtils.isValid(product)) {
                try {
                    validateProduct(product);
                } finally {
                    this.connector.dispose();
                }
                return;
            }
        }
        addToExtraNotes("!!Kein Produkt gefunden");
        this.connector.dispose();
    }

    protected void addToResultMap(AssemblyId assemblyId, List<String> csvColorList, List<String> csvPictureAndTUList, List<String> csvFINCheckList) {
        if ((assemblyId != null) && assemblyId.isValidId()) {
            if (Utils.isValid(csvColorList)) {
                synchronized (csvLinesColorsMap) {
                    csvLinesColorsMap.put(assemblyId, csvColorList);
                }
            }
            if (Utils.isValid(csvPictureAndTUList)) {
                synchronized (csvLinesPictureAndTUMap) {
                    csvLinesPictureAndTUMap.put(assemblyId, csvPictureAndTUList);
                }
            }
            if (Utils.isValid(csvFINCheckList)) {
                synchronized (csvLinesFINCheckMap) {
                    csvLinesFINCheckMap.put(assemblyId, csvFINCheckList);
                }
            }
        }
    }

    /**
     * Berechnung für gesamtes Produkt
     *
     * @param product
     */
    protected void validateProduct(String product) {
        Set<AssemblyId> productAssemblyIds = calculateAssembliesForProduct(product);
        //!! zum Debuggen
//        AssemblyId assemblyId = productAssemblyIds.iterator().next();
//        productAssemblyIds.clear();
//        productAssemblyIds.add(assemblyId);
//        System.out.println("used Assembly " + assemblyId.toString());
        //!! zum Debuggen End
        if (productAssemblyIds.isEmpty()) {
            addToExtraNotes("!!Für Produkt \"%1\" wurden keine TUs gefunden", product);
        } else {
            validateAssemblyList(productAssemblyIds, product);
        }
    }

    /**
     * Überprüfung mehrerer TUs und Schreiben der CSV-Datei
     *
     * @param assemblyIdList
     * @param product
     */
    protected void validateAssemblyList(Set<AssemblyId> assemblyIdList, String product) {
        if (!handleDisplayFieldsForCsv(assemblyIdList)) {
            addToExtraNotes("!!Fehler beim Initialisieren!");
            return;
        }
        runTimeLogger = new RunTimeLogger(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK, true);
        addLogMsg("Starting quality check for product \"" + product + "\" with " + assemblyIdList.size() + " TU(s)");


        isQualityCheckParallelActive = iPartsEditPlugin.isQualityCheckParallelActive();
        //!! zum Debuggen
//        isQualityCheckParallelActive = false;
        //!! zum Debuggen End

        handleMultiThreading();
        // Überprüfung vornehmen
        if (startModulesValidation(assemblyIdList)) {
            // ggf Warten auf gestartete Threads
            waitForThreadsEnded();
        }

        // CSV-Dateien der Ergebnisse erstellen
        createCsvFiles(product);

        String runtime = runTimeLogger.getDurationString();
        addToExtraNotes("!!Überprüfung von Produkt \"%1\" mit %2 TU(s) beendet. Laufzeit: %3",
                        product, String.valueOf(assemblyIdList.size()), runtime);
        addLogMsg("Total runtime of quality check for product \"" + product + "\": " + runtime);
    }

    private void createCsvFiles(String product) {
        if (checkIsCanceled()) {
            return;
        }
        if (csvLinesColorsMap.isEmpty() && csvLinesPictureAndTUMap.isEmpty() && csvLinesFINCheckMap.isEmpty()) {
            // Meldung ausgeben
            addToExtraNotes("!!Für \"%1\" wurden keine relevanten Einträge gefunden", product);
            return;
        }
        // csv-Datei schreiben
        boolean allOk = false;
        exportWriter = new CsvZipMultiExportWriter();
        try {
            String currentDate = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
            String exportFileName = product + "_Validation_" + currentDate;
            if (exportWriter.open(exportFileName)) {
                if (exportWriter.openZipEntry(product + "_PartsListAndColors_" + currentDate)) {
                    writeCsvElems(getCsvHeader(displayFieldsColors), csvLinesColorsMap.values());
                    if (exportWriter.openZipEntry(product + "_PictureAndTU_" + currentDate)) {
                        writeCsvElems(getCsvHeader(displayFieldsPictureAndTU), csvLinesPictureAndTUMap.values());

                        // Hat das Produkt überhaupt FINs?
                        boolean hasFINs = false;
                        iPartsDataProduct dataProduct = new iPartsDataProduct(project, new iPartsProductId(product));
                        if (dataProduct.existsInDB()) {
                            EtkDataArray finOrVinArray = dataProduct.getFieldValueAsArray(FIELD_DP_FINS);
                            hasFINs = (finOrVinArray != null) && !finOrVinArray.isEmpty();
                        }

                        if (!hasFINs) {
                            allOk = true;
                        } else if (exportWriter.openZipEntry(product + "_FINcheck_" + currentDate)) {
                            writeCsvElems(getCsvHeader(displayFieldsFINCheck), csvLinesFINCheckMap.values());
                            allOk = true;
                        }
                    }
                }
            }
        } finally {
            if (exportWriter != null) {
                exportWriter.closeOutputStreams();
            }
        }
        if (!allOk) {
            addToExtraNotes("!!Fehler beim Schreiben der Ergebnisse für \"%1\"", product);
            if (exportWriter != null) {
                // Zip-Stream aufräumen
                exportWriter.clearAfterDownload();
                exportWriter = null;
            }
        }
    }

    private void writeCsvElems(List<String> headerLineElems, Collection<List<String>> csvMapLines) {
        String newLine = OsUtils.NEWLINE;
        exportWriter.writeHeader(headerLineElems);
        for (List<String> csvLines : csvMapLines) {
            for (String csvLine : csvLines) {
                exportWriter.writeToZipStream(csvLine + newLine);
            }
        }
    }

    private List<String> getCsvHeader(EtkDisplayFields displayFields) {
        List<String> header = new DwList<>();
        for (EtkDisplayField field : displayFields.getFields()) {
            String displayText = field.getText().getTextByNearestLanguage(viewerLanguage, fallbackLanguages);
            header.add(displayText);
        }
        return header;
    }

    /**
     * Überprüfung für alle Module vornehmen
     *
     * @param assemblyIdList
     */
    private boolean startModulesValidation(Set<AssemblyId> assemblyIdList) {
        Session session = Session.get();
        for (AssemblyId assemblyId : assemblyIdList) {
            if (checkIsCanceled()) {
                return false;
            }
            // Assembly aus der Id bestimmen
            iPartsDataAssembly iPartsAssembly = getAssemblyFromId(assemblyId);
            if (iPartsAssembly != null) {
                // das Runnable für einen TU
                Runnable calculationRunnable = createRunnableForValdidationAssembly(iPartsAssembly);
                if (isQualityCheckParallelActive) {
                    // mit Multi-Threading
                    executorService.execute(() -> {
                        if ((session != null) && session.isActive()) {
                            session.runInSession(calculationRunnable);
                        }
                    });
                } else {
                    // ohne Multi-Threading
                    calculationRunnable.run();
                }
            }
        }
        return true;
    }

    /**
     * Ggf Warten, bis alle Threads beendet sind
     */
    private void waitForThreadsEnded() {
        if (isCancelled.getValue()) {
            return;
        }
        if (isQualityCheckParallelActive) {
            ExecutorService executorServiceLocal = executorService;
            if (executorServiceLocal != null) {
                // Alle gewünschten Überprüfungen wurden zum ExecutorService hinzugefügt -> Shutdown aufrufen und warten bis alle
                // Tasks abgearbeitet wurden
                executorServiceLocal.shutdown();
                try {
                    executorServiceLocal.awaitTermination(1, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (!isCancelled.getValue()) {
                        addLogMsg("Waiting for quality check threads to finish interrupted");
                    }
                }
                executorService = null;
            }
        }
    }


    /**
     * Runnable für die Überprüfung eines TUs anlegen
     *
     * @param iPartsAssembly
     * @return
     */
    private Runnable createRunnableForValdidationAssembly(iPartsDataAssembly iPartsAssembly) {
        return () -> {
            if (checkIsCanceled()) {
                return;
            }
            checkAssemblyAndAdd(iPartsAssembly);
        };
    }

    /**
     * Der eigentliche Überprüfungsaufruf und Aufsammeln des Ergebnisses
     *
     * @param iPartsAssembly
     */
    private void checkAssemblyAndAdd(iPartsDataAssembly iPartsAssembly) {
        if (checkIsCanceled()) {
            return;
        }

        // EtkMessageLog z.B. für Fehler bei der FIN-Prüfung
        EtkMessageLog messageLog = new EtkMessageLog();
        messageLog.addMessageEventListener(event -> {
            synchronized (extraNotesFromMessageLog) {
                String message = event.getMessage();
                if (!message.isEmpty() && extraNotesFromMessageLog.add(message)) { // Doppelte Meldungen vermeiden
                    addToExtraNotes(message);
                }
            }
        });

        iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm =
                iPartsEditAssemblyListValidationOverlappingEntriesForm.validateAssemblySilent(connector, iPartsAssembly, messageLog);
        addForm(iPartsAssembly, validationForm);
        validationForm.dispose();
    }

    /**
     * Ergebnis einer Prüfung aufsammeln und den Fortschritt weiterschalten
     *
     * @param iPartsAssembly
     * @param validationForm
     */
    private void addForm(iPartsDataAssembly iPartsAssembly, iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm) {
        iPartsEditBaseValidationForm.ValidationResult validationResult = validationForm.getTotalValidationResult();
        if (validationResult != iPartsEditBaseValidationForm.ValidationResult.OK) {
            List<String> csvLinesForColors = null;
            List<String> csvLinesForPictureAndTU = null;
            List<String> csvLinesForFINCheck = null;
            if (validationForm.getColorAndPartlistTableValidationResult() != iPartsEditBaseValidationForm.ValidationResult.OK) {
                // Ergebnisse aufsammeln
                csvLinesForColors = collectColorsFormResults(validationForm);
            }
            if (validationForm.getValidationPictureAndTUForm().getTotalValidationResult() != iPartsEditBaseValidationForm.ValidationResult.OK) {
                // Ergebnisse aus PictureAndTuForm aufsammeln
                csvLinesForPictureAndTU = collectPictureAndTuFormResults(iPartsAssembly.getAsId(), validationForm.getValidationPictureAndTUForm());
            }
            if (validationForm.getFinValidationForm().getTotalValidationResult() != iPartsEditBaseValidationForm.ValidationResult.OK) {
                // Ergebnisse aus FinValidationForm aufsammeln
                csvLinesForFINCheck = collectFinValidationFormResults(validationForm.getFinValidationForm());
            }
            addToResultMap(iPartsAssembly.getAsId(), csvLinesForColors, csvLinesForPictureAndTU, csvLinesForFINCheck);
        }
    }

    private List<String> collectColorsFormResults(iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm) {
        List<EtkDataPartListEntry> partListEntries = validationForm.getCurrentPartListEntries();
        if ((partListEntries != null) && (displayFieldsColors != null)) {
            List<String> csvLines = new DwList<>();
            for (EtkDataPartListEntry partListEntry : partListEntries) {
                iPartsEditBaseValidationForm.ValidationResult colorEntry = null;
                boolean isColorEntryValidForCsv = false;
                if (validationForm.getColorTableValidationResult() != null) {
                    colorEntry = validationForm.getColorTableValidationResult().get(partListEntry.getAsId().getKLfdnr());
                    isColorEntryValidForCsv = isColorEntryValidForCsv(colorEntry);
                }

                iPartsEditAssemblyListValidationOverlappingEntriesForm.ValidationEntry entry = validationForm.getGeneralValidationEntry(partListEntry);
                boolean isValidationEntryValidForCsv = isValidationEntryValidForCsv(entry);

                if (isColorEntryValidForCsv || isValidationEntryValidForCsv) {
                    String csvLine = buildCsvLine(partListEntry, colorEntry, entry, displayFieldsColors);
                    if (StrUtils.isValid(csvLine)) {
                        csvLines.add(csvLine);
                    }
                }
            }
            return csvLines;
        }
        return null;
    }

    private String buildCsvLine(EtkDataPartListEntry partListEntry, iPartsEditBaseValidationForm.ValidationResult colorEntry,
                                iPartsEditAssemblyListValidationOverlappingEntriesForm.ValidationEntry entry, EtkDisplayFields displayFields) {
        CsvZipExportWriter.CsvStringBuilder str = new CsvZipExportWriter.CsvStringBuilder();
        for (EtkDisplayField cField : displayFields.getFields()) {
            String value = "";
            String tableName = cField.getKey().getTableName();
            String fieldName = cField.getKey().getFieldName();
            if (tableName.equals(TABLE_KATALOG)) {
                if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DA_COLORTABLE_QUALITY_CHECK)) {
                    // hier auf partListEntry setOfEnum zugreifen
                    if (colorEntry != null) {
                        value = project.getVisObject().asText(tableName, fieldName, partListEntry.getAttribute(fieldName, false),
                                                              dbLanguage, true);
                    }
                } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DA_QUALITY_CHECK_ERROR)) {
                    if ((entry != null) && (entry.getFilterReason() != null)) {
                        value = StrUtils.nullToEmpty(entry.getFilterReason().replace("\n", "; "));
                    }
                } else {
                    value = partListEntry.getFieldValue(fieldName);
                }
            } else if (tableName.equals(TABLE_MAT)) {
                if (cField.isMultiLanguage()) {
                    value = partListEntry.getPart().getFieldValueAsMultiLanguage(fieldName).getTextByNearestLanguage(dbLanguage,
                                                                                                                     fallbackLanguages);
                } else {
                    value = partListEntry.getPart().getFieldValue(fieldName);
                }
            }
            str.append(value);
        }
        return str.toString();
    }

    private boolean isColorEntryValidForCsv(iPartsEditBaseValidationForm.ValidationResult colorEntry) {
        return ((colorEntry != null) && (colorEntry != iPartsEditBaseValidationForm.ValidationResult.OK));
    }

    private boolean isValidationEntryValidForCsv(iPartsEditAssemblyListValidationOverlappingEntriesForm.ValidationEntry entry) {
        return ((entry != null) && (entry.getValidationResult() != null) &&
                (entry.getValidationResult() != iPartsEditBaseValidationForm.ValidationResult.OK));
    }

    private boolean isPictureAndTuValidationEntryValidForCsv(PictureAndTUValidationEntry entry) {
        return ((entry != null) && (entry.getValidationResult() != null) &&
                (entry.getValidationResult() != iPartsEditBaseValidationForm.ValidationResult.OK));
    }

    private List<String> collectPictureAndTuFormResults(AssemblyId assemblyId, iPartsValidationPictureAndTUForm validationPictureAndTUForm) {
        PictureAndTUValidationEntryList entryList = validationPictureAndTUForm.getValidationTUEntries();
        if ((entryList != null) && (displayFieldsPictureAndTU != null)) {
            List<String> csvLines = new DwList<>();
            for (PictureAndTUValidationEntry entry : entryList) {
                if (isPictureAndTuValidationEntryValidForCsv(entry)) {
                    String csvLine = buildCsvLine(assemblyId, entry);
                    if (StrUtils.isValid(csvLine)) {
                        csvLines.add(csvLine);
                    }
                }
            }
            return csvLines;
        }
        return null;
    }

    private List<String> collectFinValidationFormResults(iPartsFINValidationForm finValidationForm) {
        Map<EtkDataPartListEntry, String> errorList = finValidationForm.getPartListEntry2ErrorMap();
        if ((errorList != null) && (displayFieldsFINCheck != null)) {
            List<String> csvLines = new DwList<>();
            for (Map.Entry<EtkDataPartListEntry, String> errorEntry : errorList.entrySet()) {
                iPartsEditAssemblyListValidationOverlappingEntriesForm.ValidationEntry validationEntry = new iPartsEditAssemblyListValidationOverlappingEntriesForm.ValidationEntry(iPartsEditBaseValidationForm.ValidationResult.ERROR,
                                                                                                                                                                                    errorEntry.getValue());
                String csvLine = buildCsvLine(errorEntry.getKey(), null, validationEntry, displayFieldsFINCheck);
                if (StrUtils.isValid(csvLine)) {
                    csvLines.add(csvLine);
                }
            }
            return csvLines;
        }
        return null;
    }

    private String buildCsvLine(AssemblyId assemblyId, PictureAndTUValidationEntry entry) {
        CsvZipExportWriter.CsvStringBuilder str = new CsvZipExportWriter.CsvStringBuilder();
        for (EtkDisplayField cField : displayFieldsPictureAndTU.getFields()) {
            String value = "";
            String tableName = cField.getKey().getTableName();
            String fieldName = cField.getKey().getFieldName();
            if (tableName.equals(TABLE_KATALOG) && fieldName.equals(FIELD_K_VARI)) {
                value = assemblyId.getKVari();
            } else if (tableName.equals(PictureAndTUGrid.DUMMY_TABLE_NAME.toUpperCase())) {
                if (fieldName.equals(PictureAndTUGrid.DISPLAY_NAME_OBJECT)) {
                    value = entry.getVisiualValueOfId();
                } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DA_PICTURE_AND_TU_QUALITY_CHECK)) {
                    value = entry.getValidationResult().getDbValue();
                    if (validationEnumValue != null) {
                        EnumEntry enumEntry = validationEnumValue.getEnumEntry(value);
                        if (enumEntry != null) {
                            value = enumEntry.getEnumText().getTextByNearestLanguage(dbLanguage, fallbackLanguages);
                        }
                    }
                } else if (fieldName.equals(PictureAndTUGrid.DISPLAY_NAME_MESSAGE)) {
                    value = entry.getValidationMessage();
                }
            }
            str.append(value);
        }
        return str.toString();
    }

    /**
     * Bestimmung der DisplayFields beider Forms
     *
     * @param assemblyIdList
     * @return
     */
    private boolean handleDisplayFieldsForCsv(Set<AssemblyId> assemblyIdList) {
        iPartsDataAssembly assembly = getAssemblyFromId(assemblyIdList.iterator().next());
        if (assembly == null) {
            return false;
        }
        // Dummy Instanz erzeugen
        EditModuleFormConnector editConnectorForValidation = new EditModuleFormConnector(connector);
        editConnectorForValidation.setCurrentAssembly(assembly);

        iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm = new iPartsEditAssemblyListValidationOverlappingEntriesForm(editConnectorForValidation, null);
        if (validationForm != null) {
            EtkDisplayFields pictureAndTuDisplayFields = validationForm.getValidationPictureAndTUForm().getPictureAndTUGrid().getDisplayResultFields();
            if (pictureAndTuDisplayFields != null) {
                displayFieldsPictureAndTU = new EtkDisplayFields();
                // extra Spalte für kVari
                displayFieldsPictureAndTU.addFeld(TABLE_KATALOG, FIELD_K_VARI, false, false, null, project);
                for (EtkDisplayField pDisplayField : pictureAndTuDisplayFields.getFields()) {
                    if (pDisplayField.isVisible()) {
                        displayFieldsPictureAndTU.addFeld(pDisplayField);
                    }
                }
            }
        }
        if (validationForm != null) {
            List<EtkDisplayField> displayList = validationForm.getValidationDisplayFields();
            if (displayList != null) {
                String validationPrefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DA_MODEL_EVALUATION;
                displayFieldsColors = new EtkDisplayFields();
                // extra Spalte für kVari
                displayFieldsColors.addFeld(TABLE_KATALOG, FIELD_K_VARI, false, false, null, project);
                for (EtkDisplayField formDisplayField : displayList) {
                    if (formDisplayField.getKey().getFieldName().startsWith(validationPrefix)) {
                        continue;
                    }
                    if (formDisplayField.isVisible()) {
                        displayFieldsColors.addFeld(formDisplayField);
                    }
                }

                // Die DisplayFields für den FIN-Check analog zu denen von Stückliste/Farben ohne das Feld für die Farbvarianten-Qualitätsprüfungen
                displayFieldsFINCheck = new EtkDisplayFields(displayFieldsColors);
                displayFieldsFINCheck.removeField(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_COLORTABLE_QUALITY_CHECK, false);
            }
        }
        validationForm.dispose();
        editConnectorForValidation.dispose();
        return (displayFieldsPictureAndTU != null) && (displayFieldsColors != null);
    }

    /**
     * Einstellungen aus der Konfig abfragen und ggf Multi-Threading vorbereiten
     */
    private void handleMultiThreading() {
        threadCount = 1;
        if (isQualityCheckParallelActive) {
            threadCount = iPartsEditPlugin.getQualityCheckThreadCount();
            if (threadCount == 1) {
                isQualityCheckParallelActive = false;
            }
        }
        if (isQualityCheckParallelActive) {
            addLogMsg("Validation with " + threadCount + " Threads");
            executorService = Executors.newFixedThreadPool(threadCount);
        } else {
            addLogMsg("Sequential Validation");
        }
    }

    private iPartsDataAssembly getAssemblyFromId(AssemblyId assemblyId) {
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getValidationProject(), assemblyId);
        assembly = assembly.getLastHiddenSingleSubAssemblyOrThis(assembly.getEbene());
        if (assembly instanceof iPartsDataAssembly) {
            return (iPartsDataAssembly)assembly;
        }
        return null;
    }

    private boolean checkIsCanceled() {
        if (isCancelled.getValue()) {
            synchronized (isCancelled) {
                if (cancelStarted) {
                    return true;
                } else {
                    cancelStarted = true;
                }
            }
            addLogMsg("Validation cancelled by user");
            if (isQualityCheckParallelActive) {
                ExecutorService executorServiceLocal = executorService;
                if (executorServiceLocal != null) {
                    executorServiceLocal.shutdownNow();
                    Thread.currentThread().interrupt();
                    try {
                        executorServiceLocal.awaitTermination(1, TimeUnit.HOURS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    executorService = null;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Bestimme alle Module im Produkt
     *
     * @param productNumber
     * @return
     */
    protected Set<AssemblyId> calculateAssembliesForProduct(String productNumber) {
        iPartsProductStructures productStructure = iPartsProductStructures.getInstance(project, new iPartsProductId(productNumber));
        return productStructure.getModuleIds(project);
    }

    private void addToExtraNotes(String key, String... placeHolderTexts) {
        if (extraNotes.length() > 0) {
            extraNotes.append("\n");
        }
        extraNotes.append(TranslationHandler.translate(key, placeHolderTexts));
    }

    private void addLogMsg(String msg) {
        if (runTimeLogger != null) {
            runTimeLogger.addLogMsg(msg);
        }
    }
}
