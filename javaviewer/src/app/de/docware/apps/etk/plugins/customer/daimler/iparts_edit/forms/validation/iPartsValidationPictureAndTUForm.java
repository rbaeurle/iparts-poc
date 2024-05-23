/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataFactoryDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsDocuRelBaseResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualAssemblyDialogBase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualCalcFieldDocuRel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsLoader;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.*;

/**
 * Formular zur Validierung von Zeichnungen, Bildaufträgen und Stücklisteneinträgen.
 */
public class iPartsValidationPictureAndTUForm extends iPartsEditAssemblyListValidationForm {

    private PictureAndTUGrid pictureAndTUGrid;
    private PictureAndTUValidationEntryList validationTUEntries;

    public iPartsValidationPictureAndTUForm(AbstractJavaViewerFormIConnector dataConnector,
                                            iPartsEditAssemblyListValidationOverlappingEntriesForm parentForm,
                                            boolean removeAdditionalInfoPanel, boolean minimizeAdditionalInfoPanel) {
        super(dataConnector, parentForm, removeAdditionalInfoPanel, minimizeAdditionalInfoPanel);
    }

    @Override
    protected void postCreateGui(boolean removeAdditionalInfoPanel, boolean minimizeAdditionalInfoPanel) {
        super.postCreateGui(removeAdditionalInfoPanel, minimizeAdditionalInfoPanel);
        // ErgebnisGrid bilden und an Panel hängen
        pictureAndTUGrid = new PictureAndTUGrid(this,
                                                (EditModuleFormIConnector)getParentForm().getConnector(),
                                                getParentForm());

        getEvaluationContentPanel().removeAllChildren();
        getEvaluationContentPanel().addChildBorderCenter(pictureAndTUGrid.getGui());
    }

    public void startPictureAndTUValidation() {
        updateData(this.parentForm, true);
    }

    /**
     * wird für "Überprüfung aller TUs in einem Produkt" benötigt
     *
     * @return
     */
    public PictureAndTUGrid getPictureAndTUGrid() {
        return pictureAndTUGrid;
    }

    /**
     * wird für "Überprüfung aller TUs in einem Produkt" benötigt
     *
     * @return
     */
    public PictureAndTUValidationEntryList getValidationTUEntries() {
        return validationTUEntries;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (pictureAndTUGrid != null) {
            pictureAndTUGrid.dispose();
        }
    }

    @Override
    protected boolean logPerformanceMessages() {
        return false; // In dieser Klasse wird ein eigener RunTimeLogger verwendet
    }

    /**
     * Liefert das Gesamtergebnis aller Qualitätsprüfungen zurück, wobei {@link ValidationResult#ERROR} eine {@link ValidationResult#WARNING}
     * übertrumpft.
     *
     * @return
     */
    public ValidationResult getTotalValidationResult() {
        if (validationTUEntries != null) {
            if (validationTUEntries.getIsError()) {
                return ValidationResult.ERROR;
            }
            if (validationTUEntries.getIsWarning()) {
                return ValidationResult.WARNING;
            }
        }
        return ValidationResult.OK;
    }

    public void resetMarkFirstErrorOrWarning() {
        pictureAndTUGrid.getTable().setSelectedRow(-1, false);
    }

    public void markFirstErrorOrWarning() {
        if ((validationTUEntries != null) && (pictureAndTUGrid.getTable().getSelectedRowIndex() == -1)) {
            ValidationResult result = getTotalValidationResult();
            int index = -1;
            if (result != ValidationResult.OK) {
                int lfdNr = 0;
                for (PictureAndTUValidationEntry entry : validationTUEntries) {
                    if (entry.getValidationResult() == result) {
                        index = lfdNr;
                        break;
                    }
                    lfdNr++;
                }
            }
            if (index != -1) {
                pictureAndTUGrid.getTable().setSelectedRow(index, true);
            }
        }
    }

    @Override
    protected void doValidationForModels(List<EtkDataPartListEntry> filteredPartList, iPartsDataAssembly assembly) {
        // In dieser Klasse findet keine Baumuster-spezifische Validierung statt

        // Zur Sicherheit auch hier gefilterte Werkseinsatzdaten und darin v.a. auch das für die Filterung verwendete Baumuster zurücksetzen
        if (assembly != null) {
            assembly.clearAllFactoryDataForRetailForPartList();
        }
    }

    @Override
    protected void validatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly, String selectedModel) {
    }

    @Override
    protected void beforeValidatePartList(List<EtkDataPartListEntry> partlist, iPartsDataAssembly assembly) {
        validationTUEntries = null;
        EditModuleFormIConnector editConnector = (EditModuleFormIConnector)getConnector();
        EtkMessageLogFormHelper messageLogHelper = ((iPartsEditAssemblyListValidationOverlappingEntriesForm)getParentForm()).getMessageLogHelper();
        boolean changeSetActive = isRevisionChangeSetActive();
        PictureAndTUValidationHelper pictureAndTuHelper;
        boolean isDIALOG = assembly.getDocumentationType().isPKWDocumentationType();
        if (isDIALOG) {
            pictureAndTuHelper = new PictureAndTUValidationHelper(editConnector, messageLogHelper, isSimplifiedQualityCheck());
        } else {
            pictureAndTuHelper = new PictureAndTUValidationTRUCKHelper(editConnector, messageLogHelper, isSimplifiedQualityCheck());
        }

        if (messageLogHelper != null) {
            messageLogHelper.incMaxProgress(pictureAndTuHelper.getNumberOfChecks());
            if (!isSimplifiedQualityCheck() && isDIALOG) {
                if (changeSetActive) {
                    messageLogHelper.incMaxProgress(partlist.size() * 2);
                }
            }
        }

        validationTUEntries = pictureAndTuHelper.startPictureAndTUValidation();

        if (!isSimplifiedQualityCheck() && isDIALOG) {
            RunTimeLogger runTimeLogger = new RunTimeLogger(iPartsEditPlugin.LOG_CHANNEL_QUALITY_CHECK);
            doDIALOGInvalidEntriesCheck(assembly, partlist, messageLogHelper, runTimeLogger);
            if (changeSetActive) {
                doDIALOGEqualFieldsCheck(assembly, partlist, messageLogHelper, runTimeLogger);

                PictureAndTUValidationEntryList validationReplacementEntries = checkReplacements(assembly, partlist);
                if (!validationReplacementEntries.isEmpty()) {
                    validationTUEntries.addAll(validationReplacementEntries);
                } else {
                    if (Constants.DEVELOPMENT) {
                        validationTUEntries.addOK(assembly.getAsId(), "",
                                                  "DEV: Test für Ersetzungen ohne Fehler", "");
                    }
                }
            }
            runTimeLogger.logRunTime(assembly.getAsId().getKVari() + ": DIALOG entries check in");
        }

        Session.invokeThreadSafeInSession(() -> pictureAndTUGrid.fillGrid(validationTUEntries));
    }

    /**
     * Prüfung von ungültigen DIALOG Stücklistenpositionen
     *
     * @param assembly
     * @param partlist
     * @param messageLogHelper
     */
    private void doDIALOGInvalidEntriesCheck(iPartsDataAssembly assembly, List<EtkDataPartListEntry> partlist, EtkMessageLogFormHelper messageLogHelper,
                                             RunTimeLogger runTimeLogger) {
        if (iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_CHECK_INVALID_ENTRIES_IN_QUALITY_CHECK)) {
            // Produkt bestimmen. Da es sich um ein DIALOG Stückliste handelt, wird hier immer ein Produkt zurückgeliefert
            iPartsProductId productId = assembly.getProductIdFromModuleUsage();
            if (productId == null) {
                return;
            }
            iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
            // Ist der Kenner nicht gesetzt, die Prüfung nicht durchführen
            if (!product.isDoDIALOGInvalidEntriesCheck()) {
                if (Constants.DEVELOPMENT) {
                    validationTUEntries.addOK(productId, "",
                                              "DEV: Test für ungültige DIALOG Stücklistenpositionen wurde am Produkt deaktiviert!", "");
                }
                return;
            }
            runTimeLogger.setStartTime();
            List<EntryWithSuccessorForInvalidEntriesCheck> entriesForCheck = detectEntriesForInvalidEntriesCheck(partlist);
            if (messageLogHelper != null) {
                messageLogHelper.incMaxProgress(entriesForCheck.size());
            }
            Map<iPartsDocuRelevant, List<EntryWithSuccessorForInvalidEntriesCheck>> validEntriesMap = new HashMap<>();
            // Positionen durchlaufen und die DokuRelevanz berechnen
            entriesForCheck.sort(Comparator.comparing(o -> o.getCurrentEntry().getAsId().getKLfdnr()));
            entriesForCheck.forEach(entries -> {
                EtkDataPartListEntry currentEntry = entries.getCurrentEntry();
                iPartsDataDialogData dataDialogData = getDataDialogDataIfExists(currentEntry);
                iPartsDocuRelBaseResult docuRelResult;
                if (dataDialogData != null) {
                    iPartsDialogSeries dialogSeries = iPartsDialogSeries.getInstance(getProject(), new iPartsSeriesId(dataDialogData.getFieldValue(FIELD_DD_SERIES_NO)));
                    boolean isSpecialCalc = dialogSeries.isAlternativeDocuCalc();
                    docuRelResult = calcDocuRelForInvalidEntriesCheck(currentEntry, dataDialogData, isSpecialCalc);
                    // Hat die Teileposition nach der Berechnung den Status ANR oder NR, dann gebe die Hinweismeldung aus
                    iPartsDocuRelevant docuRelevant = docuRelResult.getResult();
                    if ((docuRelevant == iPartsDocuRelevant.DOCU_RELEVANT_NO) || (docuRelevant == iPartsDocuRelevant.DOCU_RELEVANT_NOT_YET)) {
                        EtkDataPartListEntry successor = entries.getSuccessor();
                        validationTUEntries.addWarning(currentEntry.getAsId(), "",
                                                       translate("!!Ungültige DIALOG-Teileposition, da selbst nicht relevant und es existiert ein konstruktiver Nachfolger"),
                                                       translate("!!Ungültige Teileposition: %1 - Teilenummer: %2",
                                                                 currentEntry.getAsId().getKLfdnr(),
                                                                 currentEntry.getFieldValue(FIELD_K_MATNR))
                                                       + "\n"
                                                       + translate("!!Konstruktiver Nachfolger: %1",
                                                                   successor.getAsId().getKLfdnr())
                                                       + "\n"
                                                       + translate("!!Prüfung, die zum Ergebnis \"%1\" führte: %2", docuRelevant.getDisplayValue(getProject()), docuRelResult.getReason(isSpecialCalc)));
                    } else {
                        List<EntryWithSuccessorForInvalidEntriesCheck> validEntriesForDocuRel = validEntriesMap.computeIfAbsent(docuRelResult.getResult(), k -> new ArrayList<>());
                        validEntriesForDocuRel.add(entries);
                    }
                } else {
                    if (Constants.DEVELOPMENT) {
                        validationTUEntries.addWarning(currentEntry.getAsId(), "",
                                                       translate("DEV: Keine Konstruktionsdaten für Position \"%1\" gefunden!", currentEntry.getAsId().toStringForLogMessages()),
                                                       "Nachfolger: " + entries.getSuccessor().getAsId().toStringForLogMessages());
                    }
                }
                if (messageLogHelper != null) {
                    messageLogHelper.fireProgress();
                }
            });
            runTimeLogger.stopTimeAndStore();
        } else {
            if (Constants.DEVELOPMENT) {
                validationTUEntries.addOK(assembly.getAsId(), "",
                                          translate("!!Prüfung von ungültigen DIALOG-Teilepositionen ist deaktiviert"), "");
            }
        }
    }

    /**
     * Hilfsklasse für die Prüfung von Stücklistenpositionen auf Basis der Doku-Relevanz
     */
    private static class EntryWithSuccessorForInvalidEntriesCheck {

        private final EtkDataPartListEntry currentEntry;
        private final EtkDataPartListEntry successor;

        public EntryWithSuccessorForInvalidEntriesCheck(EtkDataPartListEntry currentEntry, EtkDataPartListEntry successor) {
            this.currentEntry = currentEntry;
            this.successor = successor;
        }

        public EtkDataPartListEntry getCurrentEntry() {
            return currentEntry;
        }

        public EtkDataPartListEntry getSuccessor() {
            return successor;
        }
    }

    /**
     * Liefert die konstruktiven Daten zur übergebenen AS Stücklistenposition
     *
     * @param entryForCheck
     * @return
     */
    private iPartsDataDialogData getDataDialogDataIfExists(EtkDataPartListEntry entryForCheck) {
        String kSourceGUID = entryForCheck.getFieldValue(FIELD_K_SOURCE_GUID);
        iPartsDataDialogData dataDialogData = new iPartsDataDialogData(getProject(), new iPartsDialogId(kSourceGUID));
        return dataDialogData.existsInDB() ? dataDialogData : null;
    }

    /**
     * Berechnet die Basis-Doku-Relevanz für die übergebene AS Stücklistenposition
     *
     * @param entryForCheck
     * @param dataDialogData
     * @return
     */
    private iPartsDocuRelBaseResult calcDocuRelForInvalidEntriesCheck(EtkDataPartListEntry entryForCheck,
                                                                      iPartsDataDialogData dataDialogData,
                                                                      boolean isSpecialCalc) {
        // Mit der AS Position und den verknüpften Konstruktionsdaten eine "Dummy" Stücklistenposition aus der Konstruktion erzeugen
        EtkDataPartListEntry entryWithConstData = createDIALOGEntryWithConstData(entryForCheck, dataDialogData);
        // Die Doku-Relevanz der "Dummy" Stücklistenposition berechnen
        iPartsVirtualCalcFieldDocuRel docuRel = new iPartsVirtualCalcFieldDocuRel(getProject(), entryWithConstData, true);
        return docuRel.checkBaseDocRelevanceForRealFilter(isSpecialCalc);
    }

    /**
     * Ermittelt die Positionen, die für die Prüfung von ungültigen Stücklistenpositionen infrage kommen
     *
     * @param partList
     * @return
     */
    private List<EntryWithSuccessorForInvalidEntriesCheck> detectEntriesForInvalidEntriesCheck(List<EtkDataPartListEntry> partList) {
        // BCTE Schlüssel (ohne sdata) mit Teilenummer auf Stücklistenpositionen
        Map<String, Set<EtkDataPartListEntry>> groupedEntries = groupEntriesForInvalidEntriesCheck(partList);
        List<EntryWithSuccessorForInvalidEntriesCheck> entriesForCheck = new ArrayList<>();
        // Jetzt alle Gruppierungen durchlaufen
        groupedEntries.values().stream()
                .filter(listWithEntries -> listWithEntries.size() > 1) // Wir brauchen nur die Gruppierungen, wo mind. zwei Positionen enthalten sind
                .forEach(listWithEntries -> {
                    Iterator<EtkDataPartListEntry> iterator = listWithEntries.iterator();
                    EtkDataPartListEntry currentEntry = iterator.next(); // Aktuelle und ältere Position (sortiert nach SDATA)
                    // Solange noch andere Positionen vorhanden sind, die nächste bestimmen (sortiert nach SDATA)
                    // und prüfen, ob SDATB der aktuellen Position (ältere) = SDATA der nächsten Position (jüngere)
                    while (iterator.hasNext()) {
                        EtkDataPartListEntry nextEntry = iterator.next();
                        // SDATB der aktuellen Position (ältere)
                        String currentEntrySdatb = currentEntry.getFieldValue(FIELD_K_DATETO);
                        if (StrUtils.isValid(currentEntrySdatb)) {
                            // SDATA der nächsten Position (jüngere)
                            String nextEntrySdata = nextEntry.getFieldValue(FIELD_K_DATEFROM);
                            // Sind SDATB und SDATA gleich, muss die Position geprüft werden
                            if (StrUtils.isValid(nextEntrySdata) && currentEntrySdatb.equals(nextEntrySdata)) {
                                EntryWithSuccessorForInvalidEntriesCheck entriesCheck = new EntryWithSuccessorForInvalidEntriesCheck(currentEntry, nextEntry);
                                entriesForCheck.add(entriesCheck);
                            }
                        }
                        // Die nächste Position wird beim nächsten Loop die aktuelle Position
                        currentEntry = nextEntry;
                    }
                });
        return entriesForCheck;
    }

    /**
     * Gruppiert alle Positionen nach ihrem BCTE Schlüssel ohne SDATA aber mit der Teilenummer
     *
     * @param partlist
     * @return
     */
    private Map<String, Set<EtkDataPartListEntry>> groupEntriesForInvalidEntriesCheck(List<EtkDataPartListEntry> partlist) {
        Map<String, Set<EtkDataPartListEntry>> groupedEntries = new HashMap<>();
        partlist.forEach(entry -> {
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(entry);
            if (bctePrimaryKey != null) {
                // BCTE ohne SDATA
                bctePrimaryKey = bctePrimaryKey.getPositionBCTEPrimaryKeyWithoutSDA();
                // Schlüssel für die Map: BCTE (ohne SDATA) UND Teilenummer (DAIMLER-13606)
                String key = bctePrimaryKey.toString() + "##" + entry.getPart().getAsId().getMatNr();
                Set<EtkDataPartListEntry> entriesForKey
                        = groupedEntries.computeIfAbsent(key, k -> new TreeSet<>(Comparator.comparing((EtkDataPartListEntry o) -> o.getFieldValue(FIELD_K_DATEFROM))));
                entriesForKey.add(entry);
            }
        });
        return groupedEntries;
    }

    /**
     * Erzeugt aus der übergebenen AS Position und den via BCTE Schlüssel verknüpften Konstruktionsdaten eine
     * "Dummy" Stücklistenposition aus der Konstruktion
     *
     * @param entryForCheck
     * @param dataDialogData
     * @return
     */
    private EtkDataPartListEntry createDIALOGEntryWithConstData(EtkDataPartListEntry entryForCheck, iPartsDataDialogData dataDialogData) {
        // Alle notwendigen KATALOG-Attribute vom Original übernehmen (Klonen aller Attribute kann zu einer ConcurrentModificationException
        // führen aufgrund der Berechnung in einem parallelen Thread während an entryForCheck in einem anderen Thread neue
        // Attribute für die Qualitätsprüfungsergebnisse hinzugefügt werden)
        DBDataObjectAttributes katAttributes = new DBDataObjectAttributes();
        katAttributes.addField(FIELD_K_VARI, entryForCheck.getFieldValue(FIELD_K_VARI), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, entryForCheck.getFieldValue(FIELD_K_VER), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_LFDNR, entryForCheck.getFieldValue(FIELD_K_LFDNR), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MATNR, entryForCheck.getFieldValue(FIELD_K_MATNR), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MVER, entryForCheck.getFieldValue(FIELD_K_MVER), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SOURCE_TYPE, entryForCheck.getFieldValue(FIELD_K_SOURCE_TYPE), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SOURCE_GUID, entryForCheck.getFieldValue(FIELD_K_SOURCE_GUID), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_DATETO, entryForCheck.getFieldValue(FIELD_K_DATETO), DBActionOrigin.FROM_DB);

        // Virtuelle Felder mit den Werten aus den Konstruktionsdaten besetzen
        for (VirtualFieldDefinition mapping : iPartsDataVirtualFieldsDefinition.getMapping(TABLE_DA_DIALOG, TABLE_KATALOG)) {
            String fieldName = mapping.getVirtualFieldName();
            String value;
            DBDataObjectAttribute attrib = dataDialogData.getAttribute(mapping.getSourceFieldName(), false);
            if (attrib != null) {
                value = attrib.getAsString();
            } else {
                value = null;
            }
            if (value != null) { // Feld hat nur einen Wert, wenn es benötigt wird
                katAttributes.addField(fieldName, value, true, DBActionOrigin.FROM_DB);
            }
        }

        // Die neue Position mit allen KATALOG und virtuellen Attributen
        EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), katAttributes);
        // Das EtkPart mit den Attributen aus dem Original besetzen
        EtkDataPart partForPartListEntry = newPartListEntry.getPart();
        partForPartListEntry.setAttributes(entryForCheck.getPart().getAttributes(), DBActionOrigin.FROM_DB);
        // Das ETKZ vom Material explizit laden, wenn es nicht vorhanden oder leer ist, damit die Doku-Relevanz berechnet werden kann
        if (!partForPartListEntry.attributeExists(FIELD_M_ETKZ) || partForPartListEntry.getFieldValue(FIELD_M_ETKZ).isEmpty()) {
            partForPartListEntry.loadFromDB(partForPartListEntry.getAsId());
        }
        // Für den BCTE Schlüssel alle Werksdaten laden
        iPartsDataFactoryDataList factoryDataList = iPartsDataFactoryDataList.loadFactoryDataGUID(getProject(),
                                                                                                  entryForCheck.getFieldValue(FIELD_K_SOURCE_GUID));
        // Aus den Werksdaten konstruktive Werksdaten erzeugen, weil diese für die Doku-Relevanz benötigt werden
        Map<iPartsDialogBCTEPrimaryKey, iPartsFactoryData> factoryDataWithHistory = iPartsVirtualAssemblyDialogBase.loadFactoryDataForConstruction(factoryDataList);

        // Jetzt die konstruktiven Werksdaten an der Stücklistenposition setzen
        iPartsDialogBCTEPrimaryKey dialogKeyOfEntry = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dataDialogData.getFieldValue(FIELD_DD_GUID));
        iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)newPartListEntry;
        iPartsFactoryData factoryData;
        if (dialogKeyOfEntry != null) {
            if (factoryDataWithHistory.containsKey(dialogKeyOfEntry)) {
                factoryData = factoryDataWithHistory.get(dialogKeyOfEntry);
            } else {
                // Damit das "factoryDataForConstructionLoaded" Flag auf true steht und der Datensatz nicht manuell nachgeladen werden muss -> StackOverFlow
                factoryData = new iPartsFactoryData();
            }
        } else {
            factoryData = new iPartsFactoryData();
        }
        partListEntry.setFactoryDataForConstruction(factoryData);

        // Nur die Fußnoten für DIALOG-Stücklisteneinträge laden
        Collection<iPartsFootNote> allFootnotes = new LinkedHashSet<>();
        // Set mit bereits hinzugefügten Fußnoten-IDs
        Set<iPartsFootNoteId> alreadyCollectedFootNotes = new HashSet<>();
        // Teilestamm und DIALOG Fußnoten hinzufügen
        iPartsFootNoteHelper.addPartAndDIALOGFootnotes(partListEntry, allFootnotes, alreadyCollectedFootNotes, iPartsPartFootnotesCache.getInstance(entryForCheck.getEtkProject()),
                                                       iPartsDIALOGFootNotesCache.getInstance(entryForCheck.getEtkProject()));
        if (allFootnotes.isEmpty()) {
            allFootnotes = null;
        }
        partListEntry.setFootNotes(allFootnotes);

        return newPartListEntry;
    }

    /**
     * Prüfung von vererbten Attributen
     *
     * @param assembly
     * @param partlist
     * @param messageLogHelper
     */
    private void doDIALOGEqualFieldsCheck(iPartsDataAssembly assembly, List<EtkDataPartListEntry> partlist, EtkMessageLogFormHelper messageLogHelper,
                                          RunTimeLogger runTimeLogger) {
        if (iPartsEditPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsEditPlugin.CONFIG_CHECK_EQUAL_FIELDS_IN_QUALITY_CHECK)) {
            runTimeLogger.setStartTime();
            EditMultiCheckEqualFieldsHelper checkEqualFieldsHelper = new EditMultiCheckEqualFieldsHelper(getProject(), messageLogHelper);
            if (assembly.isPSKAssembly()) {
                validationTUEntries.addOK(assembly.getAsId(), "", translate("!!Keine Prüfung von vererbten Attributen bei PSK-Produkten"), "");
            } else if (checkEqualFieldsHelper.checkAssemblyForAsEqualFields(assembly, partlist)) {
                if (messageLogHelper != null) {
                    messageLogHelper.incMaxProgress(checkEqualFieldsHelper.getNumberOfChecks());
                }
                checkEqualFieldsHelper.doExecuteCheckForEqualFields();
                if (checkEqualFieldsHelper.getValidationEntries().isEmpty()) {
                    if (Constants.DEVELOPMENT) {
                        validationTUEntries.addOK(assembly.getAsId(), "", "DEV: Test für vererbte Attribute ohne Fehler", "");
                    }
                } else {
                    validationTUEntries.addAll(checkEqualFieldsHelper.getValidationEntries());
                }
            } else {
                if (Constants.DEVELOPMENT) {
                    validationTUEntries.addOK(assembly.getAsId(), "", "DEV: Kein Test für vererbte Attribute nötig", "");
                }
            }
            runTimeLogger.stopTimeAndStore();
        } else {
            validationTUEntries.addOK(assembly.getAsId(), "", translate("!!Prüfung von vererbten Attributen ist deaktiviert"), "");
        }
    }

    protected PictureAndTUValidationEntryList checkReplacements(iPartsDataAssembly assembly, List<EtkDataPartListEntry> partListEntryList) {
        PictureAndTUValidationEntryList validationReplacementEntries = new PictureAndTUValidationEntryList();
        boolean replacementsWithError = false;
        if (assembly.isRetailPartList()) {
            // Suche alle neuen Ersetzungen
            iPartsDataReplacePartList newReplacements = new iPartsDataReplacePartList();
            newReplacements.loadDataForAssembly(getProject(), assembly.getAsId(), iPartsDataReleaseState.NEW, DBActionOrigin.FROM_DB);
            Set<String> kLfdNrWithNewReplacements = new HashSet<>();
            for (iPartsDataReplacePart newReplacement : newReplacements) {
                kLfdNrWithNewReplacements.add(newReplacement.getAsId().getPredecessorLfdNr());
            }

            // Alle Stücklisteneinträge überprüfen
            for (EtkDataPartListEntry etkPartListEntry : partListEntryList) {
                if (etkPartListEntry instanceof iPartsDataPartListEntry) {
                    Collection<iPartsReplacement> successors = null;

                    iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)etkPartListEntry;

                    // DAIMLER-15287 RFME-FN-Prüfung
                    // Freigegebene Nachfolger prüfen und Hinweis bei Vorgänger-Fußnote = 414 && RFME != V0X
                    // Die Prüfung erfolgt nur bei PKW/VAN-TUs (Doku-Methode "PSK PKW/VAN", "DIALOG")
                    if (assembly.getDocumentationType().isPKWDocumentationType()) {
                        successors = partListEntry.getSuccessors();
                        if (Utils.isValid(successors)) {
                            // Fußnoten des Vorgängers prüfen: Hinweis bei Fußnote = 414
                            Collection<iPartsFootNote> footNotes = partListEntry.getFootNotes();
                            if (Utils.isValid(footNotes)) {
                                footNotesLoop:
                                for (iPartsFootNote footNote : footNotes) {
                                    // Nur echte Fußnoten in Betracht ziehen
                                    if (footNote.isRealFootnote()) {
                                        // Falls 414, dann RFME-Daten prüfen
                                        if (footNote.getFootNoteId().equals(iPartsDefaultStandardFootNote.FN_DONT_USE_OLD_PART.getFootNoteId())) {
                                            for (iPartsReplacement successor : successors) {
                                                if (successor.releaseState == iPartsDataReleaseState.RELEASED) {
                                                    // RFME Flags für Nachfolger auswerten: Hinweis bei Nachfolger != V0X
                                                    iPartsRFMEA rfmeaSuccessor = new iPartsRFMEA(successor.rfmeaFlags);
                                                    iPartsRFMEN rfmenSuccessor = new iPartsRFMEN(successor.rfmenFlags);
                                                    if (!(rfmeaSuccessor.isUsePredecessorForbidden() && rfmenSuccessor.isPredecessorDirectReplaceable())) {  // !(V0 && X)
                                                        validationReplacementEntries.addWarning(etkPartListEntry.getAsId(),
                                                                                                translate("!!Vorgänger enthält Fußnote 414 und RFME-Daten sind ungleich V0X"));
                                                        break footNotesLoop;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Neue Ersetzungen überprüfen
                    if (kLfdNrWithNewReplacements.contains(etkPartListEntry.getAsId().getKLfdnr())) {
                        replacementsWithError = true;
                        validationReplacementEntries.addError(etkPartListEntry.getAsId(), translate("!!Neue Ersetzung überprüfen"));
                    }

                    // Mehr als eine freigegebene Ersetzung für gleichen Vorgänger und Nachfolger überprüfen
                    // Nachfolger laden, falls in vorheriger Prüfung nicht initialisiert
                    if (successors == null) {
                        successors = partListEntry.getSuccessors();
                    }

                    if (successors != null) {
                        Set<String> releasedReplacementSuccessorLfdnrSet = new HashSet<>();
                        for (iPartsReplacement successor : successors) {
                            if (iPartsDataReleaseState.replacementRelevantStates.contains(successor.releaseState)) {
                                if (successor.successorEntry != null) {
                                    if (!releasedReplacementSuccessorLfdnrSet.add(successor.successorEntry.getAsId().getKLfdnr())) {
                                        replacementsWithError = true;
                                        validationReplacementEntries.addError(etkPartListEntry.getAsId(),
                                                                              translate("!!Mehrere retail-relevante Ersetzungen für denselben Nachfolger mit laufender Nummer %1",
                                                                                        successor.successorEntry.getAsId().getKLfdnr()));
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    checkUsageOfDontUseOldPartFootnote(partListEntry, validationReplacementEntries);
                }
                fireProgress();
            }
        }
        if (Constants.DEVELOPMENT && !replacementsWithError) {
            validationReplacementEntries.addOK(assembly.getAsId(), "",
                                               "DEV: Kein Test für Ersetzungen nötig", "");
        }

        return validationReplacementEntries;
    }

    /**
     * Überprüft die Verwendung von virtuellen Fußnoten. Die virtuellen Fußnoten wurden nicht in die Liste der Fußnoten
     * am Stücklisteneintrag eingefügt, falls sie schon als echte Fußnoten existieren. Deshalb werden die virtuellen Fußnoten
     * hier nochmal bestimmt und entsprechende Warnungen ausgegeben, falls sie in der Liste der echten Fußnoten bereits
     * vorhanden sind.
     *
     * @param partListEntry
     * @param validationReplacementEntries
     */
    private void checkUsageOfDontUseOldPartFootnote(iPartsDataPartListEntry partListEntry,
                                                    PictureAndTUValidationEntryList validationReplacementEntries) {

        Collection<iPartsFootNote> footNotes = partListEntry.getFootNotes();
        EtkDataPartListEntryList partListEntries = new EtkDataPartListEntryList();
        partListEntries.add(partListEntry, DBActionOrigin.FROM_DB);
        iPartsFootNote virtualFootNote = iPartsVirtualFootnoteHelper.createVirtualFootNoteFromReplacements(partListEntry,
                                                                                                           new iPartsPRIMUSReplacementsLoader(partListEntry.getEtkProject(),
                                                                                                                                              partListEntries));

        boolean containsVirtualAsRealFootNote = false;
        if (virtualFootNote != null) {
            containsVirtualAsRealFootNote = iPartsFootNoteHelper.containsFootnote(footNotes, virtualFootNote.getFootNoteId(),
                                                                                  iPartsFootnoteType.getRealFootNoteTypes());
            if (containsVirtualAsRealFootNote) {
                validationReplacementEntries.addWarning(partListEntry.getAsId(),
                                                        translate("!!Verwendung einer echten %1-er Fußnote, obwohl sie" +
                                                                  " als virtuelle Fußnote aus den Ersetzungsdaten erzeugt wird",
                                                                  virtualFootNote.getFootNoteId().getFootNoteId()));
            }
        }

        // Check, ob Standardfußnoten, die auch durch Ersetzungen ausgedrückt werden können, manuell Verwendet werden.
        // Falls es eine virtuelle Fußnote gibt, die manuell verwendet wird, wird diese hier nicht mehr geprüft.
        for (iPartsDefaultStandardFootNote defaultStandardFootNote : iPartsDefaultStandardFootNote.values()) {
            if (containsVirtualAsRealFootNote && defaultStandardFootNote.getFootNoteId().equals(virtualFootNote.getFootNoteId())) {
                continue;
            }
            boolean containsDefaultAsRealFootNote = iPartsFootNoteHelper.containsFootnote(footNotes, defaultStandardFootNote.getFootNoteId(),
                                                                                          iPartsFootnoteType.getRealFootNoteTypes());
            if (containsDefaultAsRealFootNote) {
                validationReplacementEntries.addWarning(partListEntry.getAsId(),
                                                        translate("!!Verwendung einer echten," +
                                                                  " also nicht aus den Ersetzungsdaten erzeugten %1-er Fußnote",
                                                                  defaultStandardFootNote.getFootNoteId().getFootNoteId()));
            }
        }

        if (virtualFootNote != null) {
            if (!iPartsFootNoteHelper.containsFootnote(footNotes, virtualFootNote.getFootNoteId(), null)) {
                validationReplacementEntries.addWarning(partListEntry.getAsId(),
                                                        translate("!!Fehlende, von den Ersetzungsdaten geforderte," +
                                                                  " %1-er Fußnote",
                                                                  virtualFootNote.getFootNoteId().getFootNoteId()));
            }
        }
    }

    @Override
    protected String getDisplayFieldConfigKey(String partListType) {
        return null;
    }

    @Override
    public String createVirtualFieldNameForModelOrFINEvaluation(String modelNumber) {
        return null;
    }
}
