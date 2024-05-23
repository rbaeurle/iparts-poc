package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsCombTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPASList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.db.etkrecord.EtkRecords;
import de.docware.framework.modules.db.serialization.*;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class ReportForEditOfAutoTransferEntriesWorker implements iPartsConst {

    private static boolean DEBUG_CALCULATION = false;
    static public final String FIELD_DCE_DO_REASON = VirtualFieldsUtils.addVirtualFieldMask("DCE_DO_REASON");
    static public final String FIELD_DCE_DO_VALUE = VirtualFieldsUtils.addVirtualFieldMask("DCE_DO_VALUE");
    static public final String FIELD_DCE_DO_CURRENT_VALUE = VirtualFieldsUtils.addVirtualFieldMask("DCE_DO_CURRENT_VALUE");
    static public final String FIELD_DCE_DO_LASTCHANGE = VirtualFieldsUtils.addVirtualFieldMask("DCE_DO_LASTCHANGE");

    static private final Language OUTPUT_LANG = Language.DE;

    private EtkProject project;
    private EtkDisplayFields csvFields;
    private EtkDisplayFields valueFields;
    private Map<AssemblyId, List<String>> resultCsvMap;
    private boolean isParallelCalculationActive;
    private CsvZipExportWriter exportWriter;
    private StringBuilder extraNotes;

    public ReportForEditOfAutoTransferEntriesWorker() {
    }

    public EtkProject getProject() {
        return project;
    }

    protected void init(EtkProject project) {
        this.project = project;
        this.resultCsvMap = new TreeMap<>();
        this.csvFields = getCsvFields();
        this.valueFields = getValueFields();
        this.exportWriter = null;
        this.extraNotes = new StringBuilder();
    }

    /**
     * Spalten für die CSV-Datei
     * WICHTIG: Werden hier neue Werte aufgenommen, so muss buildOneCsvLine() angepasst werden
     *
     * @return
     */
    private EtkDisplayFields getCsvFields() {
        EtkDisplayFields csvFields = new EtkDisplayFields();
        csvFields.addFeld(TABLE_KATALOG, FIELD_K_VARI, false, false, "!!TU", getProject());
        csvFields.addFeld(TABLE_KATALOG, FIELD_K_LFDNR, false, false, "!!Laufende Nummer", getProject());
        csvFields.addFeld(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_REASON, false, false, "!!Typ der Änderung", getProject());
        csvFields.addFeld(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_VALUE, false, false, "!!Wert aus Automatisierung", getProject());
        csvFields.addFeld(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_CURRENT_VALUE, false, false, "!!Aktueller Wert", getProject());
        csvFields.addFeld(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_LASTCHANGE, false, false, "!!Letzte Änderung", getProject());

        return csvFields;
    }

    /**
     * Felder, deren Änderung in den ChangeSets protokolliert werden sollen
     *
     * @return
     */
    private EtkDisplayFields getValueFields() {
        EtkDisplayFields valueFields = new EtkDisplayFields();
        valueFields.addFeld(TABLE_KATALOG, FIELD_K_POS, false, false, null, getProject());
        valueFields.addFeld(TABLE_KATALOG, FIELD_K_HIERARCHY, false, false, null, getProject());
        valueFields.addFeld(TABLE_KATALOG, FIELD_K_EVAL_PEM_FROM, false, false, null, getProject());
        valueFields.addFeld(TABLE_KATALOG, FIELD_K_EVAL_PEM_TO, false, false, null, getProject());
        valueFields.addFeld(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, true, false, null, getProject());

        return valueFields;
    }

    private void addToExtraNotes(String key, String... placeHolderTexts) {
        if (extraNotes.length() > 0) {
            extraNotes.append("\n");
        }
        extraNotes.append(TranslationHandler.translate(key, placeHolderTexts));
    }

    protected void addToResultMap(AssemblyId assemblyId, List<String> csvList) {
        if ((assemblyId != null) && assemblyId.isValidId()) {
            if (csvList.isEmpty()) {
                return;
            }
            synchronized (resultCsvMap) {
                resultCsvMap.put(assemblyId, csvList);
            }
        }
    }

    public void startCalculation(EtkProject project, AssemblyId assemblyId) {
        init(project);
        if (assemblyId.isVirtual()) {
            isParallelCalculationActive = false; // true für parallele Bearbeitung
            String product = iPartsVirtualNode.getProductNumberFromAssemblyId(assemblyId);
            KgTuId kgTuId = iPartsVirtualNode.getKgTuFromAssemblyId(assemblyId);
            if (product != null) {
                if (kgTuId == null) {
                    calculateProduct(product);
                } else if (kgTuId.isTuNode()) {
                    calculateTuNode(product, kgTuId);
                } else {
                    calculateKgNode(product, kgTuId);
                }
            }
        } else {
            isParallelCalculationActive = false;
            calculateSingleTU(assemblyId);
        }
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

    public String getExtraNotes() {
        return extraNotes.toString();
    }

    /**
     * Berechnung für gesamtes Produkt
     *
     * @param product
     */
    protected void calculateProduct(String product) {
        Set<AssemblyId> productAssemblyIds = calculateAssembliesForProduct(product);
        if (productAssemblyIds.isEmpty()) {
            addToExtraNotes("!!Für Produkt \"%1\" wurden keine TUs gefunden", product);
        } else {
            calculateAssemblyList(productAssemblyIds, product);
        }
    }

    /**
     * Bestimme alle Module im Produkt
     *
     * @param product
     * @return
     */
    protected Set<AssemblyId> calculateAssembliesForProduct(String product) {
        return getAssembliesFromProductEinPas(product, null);
    }

    private Set<AssemblyId> getAssembliesFromProductEinPas(String product, KgTuId kgTuId) {
        Set<AssemblyId> assemblyIds = new TreeSet<>();
        iPartsDataModuleEinPASList einPASList = new iPartsDataModuleEinPASList();
        einPASList.setSearchWithoutActiveChangeSets(true);
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(TABLE_DA_MODULES_EINPAS, FIELD_DME_PRODUCT_NO, false, false, null, project);
        selectFields.addFeld(TABLE_DA_MODULES_EINPAS, FIELD_DME_MODULE_NO, false, false, null, project);
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_PRODUCT_NO) };
        String[] whereValues = new String[]{ product };
        if (kgTuId != null) {
            if (kgTuId.isKgNode()) {
                whereTableAndFields = StrUtils.mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_KG) });
                whereValues = StrUtils.mergeArrays(whereValues, new String[]{ kgTuId.getKg() });
            } else if (kgTuId.isTuNode()) {
                whereTableAndFields = StrUtils.mergeArrays(whereTableAndFields, new String[]{ TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_KG),
                                                                                              TableAndFieldName.make(TABLE_DA_MODULES_EINPAS, FIELD_DME_SOURCE_TU) });
                whereValues = StrUtils.mergeArrays(whereValues, new String[]{ kgTuId.getKg(), kgTuId.getTu() });
            }
        }
        String[] sortFields = null;
        einPASList.searchSortAndFillWithJoin(project, null, selectFields, whereTableAndFields, whereValues,
                                             false, sortFields, false,
                                             new EtkDataObjectList.FoundAttributesCallback() {
                                                 @Override
                                                 public boolean foundAttributes(DBDataObjectAttributes attributes) {
                                                     String assemblyNo = attributes.getFieldValue(FIELD_DME_MODULE_NO);
                                                     if (StrUtils.isValid(assemblyNo)) {
                                                         assemblyIds.add(new AssemblyId(assemblyNo, ""));
                                                     }
                                                     return false;
                                                 }
                                             });
        return assemblyIds;
    }

    /**
     * Berechnung für alle Module unter einem KG-Knoten
     *
     * @param product
     * @param kgTuId
     */
    protected void calculateKgNode(String product, KgTuId kgTuId) {
        Set<AssemblyId> kgAssemblyIds = calculateAssembliesForKgNode(product, kgTuId.getKg());
        if (kgAssemblyIds.isEmpty()) {
            addToExtraNotes("!!Für Produkt \"%1\" KG \"%2\" wurden keine TUs gefunden", product, kgTuId.getKg());
        } else {
            calculateAssemblyList(kgAssemblyIds, product + "_" + kgTuId.getKg());
        }
    }

    /**
     * Bestimme alle Module unterhalb eines KG-Knotens in einem Produkt
     *
     * @param product
     * @param kgNode
     * @return
     */
    protected Set<AssemblyId> calculateAssembliesForKgNode(String product, String kgNode) {
        return getAssembliesFromProductEinPas(product, new KgTuId(kgNode, ""));
    }

    /**
     * Berechnung für alle Module unter einem TU-Knoten
     *
     * @param product
     * @param kgTuId
     */
    protected void calculateTuNode(String product, KgTuId kgTuId) {
        Set<AssemblyId> tuAssemblyIds = calculateAssembliesForTuNode(product, kgTuId);
        if (tuAssemblyIds.isEmpty()) {
            addToExtraNotes("!!Für Produkt \"%1\" KG/TU \"%2 %3\" wurden keine TUs gefunden", product, kgTuId.getKg(), kgTuId.getTu());
        } else {
            calculateAssemblyList(tuAssemblyIds, product + "_" + kgTuId.toString("_"));
        }
    }


    /**
     * Bestimme alle Module unterhalb eines TU-Knotens in einem Produkt
     *
     * @param product
     * @param kgTuId
     * @return
     */
    protected Set<AssemblyId> calculateAssembliesForTuNode(String product, KgTuId kgTuId) {
        return getAssembliesFromProductEinPas(product, kgTuId);
    }

    /**
     * Berechnung für einen TU und Schreiben der CSV-Datei
     *
     * @param assemblyId
     */
    protected void calculateSingleTU(AssemblyId assemblyId) {
        buildCsvLines(assemblyId);
        writeCsvFile(assemblyId.getKVari());
    }

    /**
     * Berechnung für mehrere TUs und Schreiben der CSV-Datei
     *
     * @param assemblyIdList
     * @param fileName
     */
    protected void calculateAssemblyList(Set<AssemblyId> assemblyIdList, String fileName) {
        Session session = Session.get();
        for (AssemblyId assemblyId : assemblyIdList) {
            Runnable calculationRunnable = createRunnableForEditOfAutoTransferAssembly(assemblyId);
            if (isParallelCalculationActive) {
                // mit Multi-Threading
//                executorService.execute(() -> {
//                    if ((session != null) && session.isActive()) {
//                        session.runInSession(calculationRunnable);
//                    }
//                });
                // Auf executorService warten
            } else {
                // ohne Multi-Threading
                calculationRunnable.run();
            }
        }
        writeCsvFile(fileName);
    }

    protected void writeCsvFile(String fileName) {
        if (resultCsvMap.isEmpty()) {
            // Meldung ausgeben
            addToExtraNotes("!!Für \"%1\" wurden keine relevanten Einträge gefunden", fileName);
            return;
        }
        // csv-Datei schreiben
        exportWriter = new CsvZipExportWriter();
        try {
            fileName += "_" + SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
            if (exportWriter.open(fileName)) {
                exportWriter.writeHeader(getCsvHeader());
                String newLine = OsUtils.NEWLINE;
                for (List<String> csvLines : resultCsvMap.values()) {
                    for (String csvLine : csvLines) {
                        exportWriter.writeToZipStream(csvLine + newLine);
                    }
                }
            }
        } finally {
            if (exportWriter != null) {
                exportWriter.closeOutputStreams();
            }
        }
    }

    private List<String> getCsvHeader() {
        List<String> header = new DwList<>();
        for (EtkDisplayField field : csvFields.getFields()) {
            String displayText = field.getText().getText(OUTPUT_LANG.getCode());
            header.add(displayText);
        }
        return header;
    }

    private Runnable createRunnableForEditOfAutoTransferAssembly(AssemblyId assemblyId) {
        return () -> {
//            if (checkIsCanceled()) {
//                return;
//            }
            buildCsvLines(assemblyId);
//            checkIsCanceled();
        };
    }

    /**
     * Berechnungs-Instanz starten, berechnen und Ergebnisse sammeln
     *
     * @param assemblyId
     */
    protected void buildCsvLines(AssemblyId assemblyId) {
        EditOfAutoTransferEntriesCalculator calculator = new EditOfAutoTransferEntriesCalculator(getProject(), csvFields, valueFields);
        List<String> csvLines = calculator.calculateCsvLines(assemblyId);
        addToResultMap(assemblyId, csvLines);
    }

    /**
     * Hilfsklasse für die Berechnung der Werte für ein Assembly
     */
    protected static class EditOfAutoTransferEntriesCalculator {

        private EtkProject project;
        private EtkDisplayFields csvFields;
        private EtkDisplayFields valueFields;
        private Set<String> valueFieldNames;
        private SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);

        public EditOfAutoTransferEntriesCalculator(EtkProject project, EtkDisplayFields csvFields, EtkDisplayFields valueFields) {
            this.project = project;
            this.csvFields = csvFields;
            this.valueFields = valueFields;
            this.valueFieldNames = new HashSet<>();
            for (EtkDisplayField displayField : this.valueFields.getFields()) {
                this.valueFieldNames.add(displayField.getKey().getFieldName());
            }
        }

        /**
         * Startroutine für ein Assembly
         *
         * @param assemblyId
         * @return
         */
        public List<String> calculateCsvLines(AssemblyId assemblyId) {
            List<EtkDataPartListEntry> calculateEntries = calculatePartListEntries(assemblyId);
            // Bestimmung der betroffenen Kombinierten Texte
            Map<PartListEntryId, List<ModifiedAttributeData>> combTextChangeMap = prepareCombTexte(assemblyId, calculateEntries);
            List<String> csvLines = new DwList<>();
            for (EtkDataPartListEntry partListEntry : calculateEntries) {
                List<String> calculatedCsvLines = calculateCsvLine(partListEntry, combTextChangeMap.get(partListEntry.getAsId()));
                if (Utils.isValid(calculatedCsvLines)) {
                    csvLines.addAll(calculatedCsvLines);
                }
            }
            return csvLines;
        }

        /**
         * Bestimmt die betroffenen Kombinierten Texte für die berechneten PartlistEntries
         *
         * @param assemblyId
         * @param calculateEntries
         * @return
         */
        protected Map<PartListEntryId, List<ModifiedAttributeData>> prepareCombTexte(AssemblyId assemblyId, List<EtkDataPartListEntry> calculateEntries) {
            Map<PartListEntryId, List<ModifiedAttributeData>> extraChangeList = new HashMap<>();
            // Berechnung nur starten, wenn auch verlangt
            if (valueFieldNames.contains(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT)) {
                // kombinierte Texte Map
                Map<String, ChangeSetData> extraChangeSetDataMap = new HashMap<>();
                // CombTextId für die Suche
                iPartsCombTextId combTextPartialId = new iPartsCombTextId(assemblyId.getKVari(), assemblyId.getKVer(), "*", "*");
                // Suche in den ChangeSetEntries mit Callback
                loadCommittedChangeSetEntriesForPartialId(combTextPartialId, extraChangeSetDataMap, createAttributesCallbackForCombText(calculateEntries), project);
                if (!extraChangeSetDataMap.isEmpty()) {
                    // Aufbau der ModifiedAttributeData-Liste
                    for (Map.Entry<String, ChangeSetData> entry : extraChangeSetDataMap.entrySet()) {
                        String changeSetGuid = entry.getKey();
                        ChangeSetData changeSetData = entry.getValue();
                        if ((changeSetData.source == iPartsChangeSetSource.AUTHOR_ORDER) && (changeSetData.serializedHistory != null)) {
                            for (SerializedDBDataObject serializedDataObject : changeSetData.serializedHistory.getHistory()) {
                                if (serializedDataObject.getAttributes() != null) {
                                    for (SerializedDBDataObjectAttribute attribute : serializedDataObject.getAttributes()) {
                                        String fieldName = attribute.getName();
                                        if (fieldName.equals(FIELD_DCT_DICT_TEXT)) {
                                            ModifiedAttributeData modifiedData = new ModifiedAttributeData(serializedDataObject, attribute);
                                            modifiedData.changeSetGUID = changeSetGuid;
                                            String[] pkValues = serializedDataObject.getPkValues();
                                            iPartsCombTextId id = new iPartsCombTextId(pkValues);
                                            if (id != null) {
                                                PartListEntryId partListEntryId = id.getPartListEntryId();
                                                List<ModifiedAttributeData> modifiedList = extraChangeList.computeIfAbsent(partListEntryId, field -> new LinkedList<>());
                                                modifiedList.add(modifiedData);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (List<ModifiedAttributeData> modifiedList : extraChangeList.values()) {
                // nach Datum lastModificationDate sortieren
                Collections.sort(modifiedList, Comparator.comparing(o -> o.lastModificationDate));
            }

            return extraChangeList;
            // dieser Teil wird später via Vergleich mit lastModificationDate durchgeführt
//            Map<PartListEntryId, List<ModifiedAttributeData>> returnChangeList = new HashMap<>();
//            // Entferne alle Einträge vor dem ersten (zeitlich) NEW
//            if (!extraChangeList.isEmpty()) {
//                for (Map.Entry<PartListEntryId, List<ModifiedAttributeData>> entry : extraChangeList.entrySet()) {
//                    List<ModifiedAttributeData> withoutModifiedList = removeElementsBeforeLastState(entry.getValue(), SerializedDBDataObjectState.NEW);
//                    if (!withoutModifiedList.isEmpty()) {
//                        returnChangeList.put(entry.getKey(), withoutModifiedList);
//                    }
//                }
//            }
//            return returnChangeList;
        }

        /**
         * Erwartet eine sortierte {@code modifiedList}: ältestes Datum = 1. Eintrag; jüngstes Datum = letzter Eintrag
         * Suche rückwärts nach den ersten Vorkommen von {@code lastState} und liefert alle Einträge ab diesem zurück.
         * Bei lastState = NEW inklusive; bei lastState = DELETED ab dem nächsten Eintrag
         *
         * @param modifiedList
         * @param lastState
         * @return kann auch leere Liste sein!
         */
        private List<ModifiedAttributeData> removeElementsBeforeLastState(List<ModifiedAttributeData> modifiedList, SerializedDBDataObjectState lastState) {
            if (modifiedList.size() > 1) {
                int lastIndex = -1;
                // Suche "von hinten" das erste Vorkommen von lastState
                for (int lfdNr = modifiedList.size() - 1; lfdNr >= 0; lfdNr--) {
                    if (modifiedList.get(lfdNr).state == lastState) {
                        lastIndex = lfdNr;
                        if (lastState == SerializedDBDataObjectState.DELETED) {
                            lastIndex++;
                        }
                        break;
                    }
                }
                if (lastIndex > 0) {
                    if (lastIndex == modifiedList.size()) { // Element wurde gelöscht/reverted
                        return modifiedList;
                    }

                    // liefere die restliche Liste zurück
                    List<ModifiedAttributeData> withoutModifiedList = new DwList<>();
                    for (int lfdNr = lastIndex; lfdNr < modifiedList.size(); lfdNr++) {
                        withoutModifiedList.add(modifiedList.get(lfdNr));
                    }
                    return withoutModifiedList;
                }
            }
            return modifiedList;
        }

        /**
         * Callback für die Bestimmung der betroffenen Kombinierten Texte
         * Berücksichtigt werden nur die Einträge, die zu den vorher bestimmten TeilePositionen gehören
         *
         * @param calculateEntries
         * @return
         */
        private EtkDataObjectList.FoundAttributesCallback createAttributesCallbackForCombText(List<EtkDataPartListEntry> calculateEntries) {
            List<PartListEntryId> idList = new DwList<>();
            calculateEntries.forEach(partListEntry -> idList.add(partListEntry.getAsId()));
            return new EtkDataObjectList.FoundAttributesCallback() {

                @Override
                public boolean foundAttributes(DBDataObjectAttributes attributes) {
                    // CombTextId bilden
                    String doId = attributes.getFieldValue(FIELD_DCE_DO_ID);
                    iPartsCombTextId id = iPartsDataChangeSetEntry.getIdFromChangeSetEntry(iPartsCombTextId.class, iPartsCombTextId.TYPE, doId);
                    if (id != null) {
                        // passt der Eintrag zu den betroffenen Teilepositionen?
                        return idList.contains(id.getPartListEntryId());
                    }
                    return false;
                }
            };
        }

        /**
         * Für ein Assembly alle PartListEntries bestimmen, bei denen K_WAS_AUTO_CREATED == true und
         * K_AUTO_CREATED == false sind. D.h. alle automatisch übernommenen Teilepositionen, die mittlerweile verändert wurden.
         *
         * @param assemblyId
         * @return
         */
        protected List<EtkDataPartListEntry> calculatePartListEntries(AssemblyId assemblyId) {
            // Bestimme PartListEntries, die in der Assembly betroffen sind
            EtkDataPartListEntryList searchList = new EtkDataPartListEntryList();
            searchList.setSearchWithoutActiveChangeSets(true);
            String[] whereFields = new String[]{ FIELD_K_VARI, FIELD_K_VER, FIELD_K_WAS_AUTO_CREATED, FIELD_K_AUTO_CREATED };
            String[] whereValues = new String[]{ assemblyId.getKVari(), assemblyId.getKVer(),
                                                 SQLStringConvert.booleanToPPString(true), SQLStringConvert.booleanToPPString(false) };
            String[] sortFields = new String[]{ FIELD_K_VARI, FIELD_K_VER, FIELD_K_LFDNR };
            searchList.searchSortAndFill(project, TABLE_KATALOG, whereFields, whereValues, sortFields,
                                         DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
            // DEBUG only
            if (DEBUG_CALCULATION && Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
                List<String> lfdNrs = new DwList<>();
                for (EtkDataPartListEntry ple : searchList.getAsList()) {
                    lfdNrs.add(ple.getAsId().getKLfdnr());
                }
                Logger.log(iPartsEditPlugin.LOG_CHANNEL_WS_DEBUG, LogType.INFO, "ReportForEditOfAutoTransferEntriesWorker.calculatePartListEntries(): "
                                                                                + searchList.size() + " TeilePos (" + StrUtils.stringListToString(lfdNrs, ", ") + ")");
            }
            List<EtkDataPartListEntry> relevantPartListEntries = searchList.getAsList();

            // Bestimme gelöschte PartListEntries in der Assembly, die ursprünglich automatisch erzeugt wurden
            iPartsDataChangeSetEntryList changeSetEntriesSearchList = new iPartsDataChangeSetEntryList();
            whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_EDIT_INFO),
                                        TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                        TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID),
                                        TableAndFieldName.make(TABLE_DA_CHANGE_SET, FIELD_DCS_STATUS) };
            whereValues = new String[]{ CHANGE_SET_ENTRY_EDIT_INFO.DELETED_AFTER_AUTO_CREATED.name(), PartListEntryId.TYPE,
                                        assemblyId.getKVari() + IdWithType.DB_ID_DELIMITER + "*", iPartsChangeSetStatus.COMMITTED.name() };
            sortFields = new String[]{ FIELD_DCE_DO_ID };
            changeSetEntriesSearchList.searchSortAndFillWithJoin(project, null, null, whereFields, whereValues, false,
                                                                 sortFields, false, true, false, null,
                                                                 new EtkDataObjectList.JoinData(TABLE_DA_CHANGE_SET,
                                                                                                new String[] { FIELD_DCE_GUID },
                                                                                                new String[] { FIELD_DCS_GUID },
                                                                                                false, false));
            for (iPartsDataChangeSetEntry deletedChangeSetEntry : changeSetEntriesSearchList) {
                // EtkDataPartListEntry aus ChangeSetEntry erzeugen
                SerializedDBDataObject deletedSerializedDBDataObject = deletedChangeSetEntry.getSerializedDBDataObject();
                if (deletedSerializedDBDataObject != null) {
                    PartListEntryId deletedPartListEntryId = new PartListEntryId(deletedSerializedDBDataObject.getPkValues());
                    EtkDataPartListEntry deletedPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, deletedPartListEntryId);
                    deletedPartListEntry.setAttributes(deletedSerializedDBDataObject.createDBDataObjectAttributes(deletedPartListEntry),
                                                       DBActionOrigin.FROM_DB);
                    relevantPartListEntries.add(deletedPartListEntry);
                }
            }

            // DEBUG only
            if (DEBUG_CALCULATION && Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
                List<String> lfdNrs = new DwList<>();
                for (EtkDataPartListEntry ple : relevantPartListEntries) {
                    lfdNrs.add(ple.getAsId().getKLfdnr());
                }
                Logger.log(iPartsEditPlugin.LOG_CHANNEL_WS_DEBUG, LogType.INFO, "ReportForEditOfAutoTransferEntriesWorker.calculatePartListEntries(): "
                                                                                + changeSetEntriesSearchList.size() + " TeilePos (" + StrUtils.stringListToString(lfdNrs, ", ") + ")");
            }

            return relevantPartListEntries;
        }

        /**
         * @param partListEntry aktuelle TeilePos
         * @param combTextList  Liste der ModifiedAttributeData eines zugehörigen Comb-Textes (kann auch null sein)
         * @return
         */
        protected List<String> calculateCsvLine(EtkDataPartListEntry partListEntry, List<ModifiedAttributeData> combTextList) {
            // Map der betroffenen Elemente aus TeilePos und CombText zusammenmischen
            Map<String, List<ModifiedAttributeData>> changeList = prepareEntriesForPartListEntry(partListEntry, combTextList);
            if (!changeList.isEmpty()) {
                List<String> csvLines = new DwList<>();
                for (Map.Entry<String, List<ModifiedAttributeData>> changeEntry : changeList.entrySet()) {
                    String tableAndFieldNameForReason = changeEntry.getKey();
                    List<ModifiedAttributeData> modifiedList = changeEntry.getValue();
                    // Neu angelegte Einträge werden übersprungen
                    if ((modifiedList.size() == 1) && (getFirstElem(modifiedList).state == SerializedDBDataObjectState.NEW)) {
                        continue;
                    }
                    // CSV Zeile bilden
                    String csvLine = buildOneCsvLine(partListEntry, tableAndFieldNameForReason, modifiedList);
                    if (StrUtils.isValid(csvLine)) {
                        csvLines.add(csvLine);
                    }
                }
                return csvLines;
            }
            return null;
        }

        /**
         * @param partListEntry Aktuelle Teileposition
         * @param combTextList  Liste der {@link ModifiedAttributeData} eines zugehörigen kombinierten Textes (kann auch
         *                      {@code null} sein)
         * @return
         */
        protected Map<String, List<ModifiedAttributeData>> prepareEntriesForPartListEntry(EtkDataPartListEntry partListEntry,
                                                                                          List<ModifiedAttributeData> combTextList) {
            Map<String, List<ModifiedAttributeData>> changeList = new HashMap<>();
            Map<String, ChangeSetData> changeSetDataMap = new HashMap<>();
            // hole ChangeSetEntries für eine TeilePos
            loadCommittedChangeSetEntriesForPartialId(partListEntry.getAsId(), changeSetDataMap, project);
            if (!changeSetDataMap.isEmpty()) {
                for (Map.Entry<String, ChangeSetData> entry : changeSetDataMap.entrySet()) {
                    String changeSetGUID = entry.getKey();
                    ChangeSetData changeSetData = entry.getValue();
                    if ((changeSetData.source == iPartsChangeSetSource.AUTHOR_ORDER) && (changeSetData.serializedHistory != null)) {
                        for (SerializedDBDataObject serializedDataObject : changeSetData.serializedHistory.getHistory()) {
                            if (serializedDataObject.getAttributes() != null) {
                                for (SerializedDBDataObjectAttribute attribute : serializedDataObject.getAttributes()) {
                                    String fieldName = attribute.getName();
                                    if (valueFieldNames.contains(fieldName)) {
                                        ModifiedAttributeData modifiedData = new ModifiedAttributeData(serializedDataObject, attribute);
                                        modifiedData.changeSetGUID = changeSetGUID;
                                        String tableAndFieldName = TableAndFieldName.make(DBConst.TABLE_KATALOG, fieldName);
                                        List<ModifiedAttributeData> modifiedList = changeList.computeIfAbsent(tableAndFieldName,
                                                                                                              field -> new LinkedList<>());
                                        modifiedList.add(modifiedData);
                                    }
                                }
                            }
                        }
                    }
                }
                // nach Datum sortieren
                for (List<ModifiedAttributeData> modifiedList : changeList.values()) {
                    // nach Datum sortieren
                    Collections.sort(modifiedList, Comparator.comparing(o -> o.lastModificationDate));
                }
                if (!changeList.isEmpty()) {
                    // alle Einträge vor dem letzten DELETE entfernen
                    Map<String, List<ModifiedAttributeData>> returnChangeList = new HashMap<>();
                    for (Map.Entry<String, List<ModifiedAttributeData>> entry : changeList.entrySet()) {
                        List<ModifiedAttributeData> withoutModifiedList = removeElementsBeforeLastState(entry.getValue(),
                                                                                                        SerializedDBDataObjectState.DELETED);
                        if (!withoutModifiedList.isEmpty()) {
                            returnChangeList.put(entry.getKey(), withoutModifiedList);
                        }
                    }
                    changeList = returnChangeList;
                }

                // sind Einträge für CombTexte zu dieser TeilePos vorhanden?
                if (combTextList != null) {
                    List<ModifiedAttributeData> modifiedDataList = null;
                    if (!changeList.isEmpty()) {
                        modifiedDataList = changeList.values().iterator().next();
                    }
                    if ((modifiedDataList != null) && !modifiedDataList.isEmpty()) {
                        ModifiedAttributeData modifiedData = getFirstElem(modifiedDataList);
                        combTextList = reduceCombTextElements(combTextList, modifiedData);
                        if (!combTextList.isEmpty()) {
                            if (modifiedData.state == SerializedDBDataObjectState.NEW) {
                                if (getFirstElem(combTextList).state == SerializedDBDataObjectState.NEW) {
                                    if (!isNearlySameTime(modifiedData, combTextList)) {
                                        if (combTextList.size() == 1) {
                                            // state NEW umsetzen!
                                            getFirstElem(combTextList).state = SerializedDBDataObjectState.MODIFIED;
                                        }
                                    }
                                }
                            } else {
                                if ((combTextList.size() == 1) && (getFirstElem(combTextList).state == SerializedDBDataObjectState.NEW)) {
                                    // state NEW umsetzen!
                                    getFirstElem(combTextList).state = SerializedDBDataObjectState.MODIFIED;
                                }
                            }
                            addCombTextList(changeList, combTextList);
                        }
                    } else {
                        if ((combTextList.size() == 1) && (getFirstElem(combTextList).state == SerializedDBDataObjectState.NEW)) {
                            // state NEW umsetzen!
                            getFirstElem(combTextList).state = SerializedDBDataObjectState.MODIFIED;
                        }
                        addCombTextList(changeList, combTextList);
                    }
                }
            } else if (combTextList != null) {
                addCombTextList(changeList, combTextList);
            }
            return changeList;
        }

        /**
         * Entferne aus der {@code combTextList} alle Einträge vor dem {@code lastModificationDate} der Teileposition ({@code modifiedData})
         * bezogen auf deren Erstellungszeitpunkt.
         *
         * @param combTextList
         * @param modifiedData
         * @return
         */
        private List<ModifiedAttributeData> reduceCombTextElements(List<ModifiedAttributeData> combTextList, ModifiedAttributeData modifiedData) {
            boolean isReduced = false;
            if ((modifiedData.state == SerializedDBDataObjectState.NEW) && modifiedData.isAutoCreated) {
                Iterator<ModifiedAttributeData> iter = combTextList.iterator();
                while (iter.hasNext()) {
                    if (modifiedData.lastModificationDate.compareTo(iter.next().lastModificationDate) > 0) {
                        iter.remove();
                    } else {
                        break;
                    }
                }
                isReduced = true;
            }
            if (!isReduced) {
                // Entferne alle Einträge vor dem ersten (zeitlich) NEW
                combTextList = removeElementsBeforeLastState(combTextList, SerializedDBDataObjectState.NEW);
            }
            return combTextList;
        }

        private void addCombTextList(Map<String, List<ModifiedAttributeData>> changeList, List<ModifiedAttributeData> combTextList) {
            String tableAndFieldName = TableAndFieldName.make(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);
            List<ModifiedAttributeData> modifiedList = changeList.computeIfAbsent(tableAndFieldName, field -> new LinkedList<>());
            modifiedList.addAll(combTextList);
        }

        private boolean isNearlySameTime(ModifiedAttributeData modifiedData, List<ModifiedAttributeData> combTextList) {
            return (Math.abs(Long.valueOf(modifiedData.lastModificationDate) - Long.valueOf(getFirstElem(combTextList).lastModificationDate)) <= 2);
        }

        private String buildOneCsvLine(EtkDataPartListEntry partListEntry, String tableAndFieldNameForReason, List<ModifiedAttributeData> modifiedList) {
            StringBuilder str = new StringBuilder();
            String firstValue = null;
            String currentValue = null;
            // Bestimme CsvLine
            int index = 0;
            for (EtkDisplayField displayField : csvFields.getFields()) {
                String tableName = displayField.getKey().getTableName();
                String fieldName = displayField.getKey().getFieldName();
                if (tableName.equals(TABLE_KATALOG)) {
                    addToCsvLine(str, index, partListEntry.getFieldValue(fieldName));
                } else if (tableName.equals(TABLE_DA_CHANGE_SET_ENTRY)) {
                    if (fieldName.equals(FIELD_DCE_DO_REASON)) {
                        String reason = getReason(tableAndFieldNameForReason);
                        addToCsvLine(str, index, reason);
                    } else if (fieldName.equals(FIELD_DCE_DO_VALUE)) {
                        firstValue = getFirstElem(modifiedList).getVisOriginalValue(project, OUTPUT_LANG);
                        addToCsvLine(str, index, firstValue);
                    } else if (fieldName.equals(FIELD_DCE_DO_CURRENT_VALUE)) {
                        currentValue = getLastElem(modifiedList).getVisCurrentValue(project, OUTPUT_LANG);
                        addToCsvLine(str, index, currentValue);
                    } else if (fieldName.equals(FIELD_DCE_DO_LASTCHANGE)) {
                        addToCsvLine(str, index, getLastElem(modifiedList).lastModificationDate);
                    }
                } else {
                    addToCsvLine(str, index, "");
                }
                index++;
            }
            //  Datensätze werden nur ausgegeben, wenn sich der Wert aus der autom. Verarbeitung zum aktuellen Wert unterscheidet
            if ((firstValue != null) && (currentValue != null) && firstValue.equals(currentValue)) {
                return "";
            }
            return str.toString();
        }

        private String getReason(String tableAndFieldNameForReason) {
            EtkDatabaseField baseField = project.getConfig().getFieldDescription(tableAndFieldNameForReason);
            String reason;
            if (StrUtils.isEmpty(baseField.getDisplayName())) {
                reason = baseField.getDisplayText(OUTPUT_LANG.getCode(), null);
            } else {
                reason = TranslationHandler.translateForLanguage(baseField.getDisplayName(), OUTPUT_LANG.getCode());
            }
            if (StrUtils.isEmpty(reason)) {
                reason = TableAndFieldName.getFieldName(tableAndFieldNameForReason);
            }
            return reason;
        }

        private ModifiedAttributeData getFirstElem(List<ModifiedAttributeData> modifiedList) {
            return modifiedList.get(0);
        }

        private ModifiedAttributeData getLastElem(List<ModifiedAttributeData> modifiedList) {
            return modifiedList.get(modifiedList.size() - 1);
        }

        private boolean hasSameValues(List<ModifiedAttributeData> modifiedList) {
            String firstValue = getFirstElem(modifiedList).getOriginalValue();
            String currentValue = getLastElem(modifiedList).getCurrentValue();
            if ((firstValue != null) && (currentValue != null) && firstValue.equals(currentValue)) {
                return true;
            }
            return false;
        }

        private void loadCommittedChangeSetEntriesForPartialId(IdWithType partialId, Map<String, ChangeSetData> changeSetDataMap,
                                                               EtkProject project) {
            loadCommittedChangeSetEntriesForPartialId(partialId, changeSetDataMap, null, project);
        }

        private void loadCommittedChangeSetEntriesForPartialId(IdWithType partialId, Map<String, ChangeSetData> changeSetDataMap,
                                                               EtkDataObjectList.FoundAttributesCallback foundAttributesCallback,
                                                               EtkProject project) {
            iPartsDataChangeSetEntryList changeSetEntryList = new iPartsDataChangeSetEntryList();
            changeSetEntryList.loadForDataObjectIdWithTypeAndChangeSetStatus(project, partialId, iPartsChangeSetStatus.COMMITTED, foundAttributesCallback);
            for (iPartsDataChangeSetEntry dataChangeSetEntry : changeSetEntryList) {
                // hier die komplette ID des ChangeSetEntries nehmen, da mehrere Änderungen im gleichen ChangeSet möglich sins
                changeSetDataMap.computeIfAbsent(dataChangeSetEntry.getAsId().toDBString(), changeSetGUID -> {
                    ChangeSetData newChangeSetData = new ChangeSetData();
                    newChangeSetData.source = iPartsChangeSetSource.getSourceByDbValue(dataChangeSetEntry.getFieldValue(FIELD_DCS_SOURCE));

                    String jsonString = dataChangeSetEntry.getHistoryData();
                    if (!jsonString.isEmpty()) {
                        SerializedDBDataObjectHistory serializedHistory = serializedDbDataObjectAsJSON.getHistoryFromJSON(jsonString);
                        if ((serializedHistory != null) && (serializedHistory.getHistory() != null) && !serializedHistory.getHistory().isEmpty()) {
                            newChangeSetData.serializedHistory = serializedHistory;
                        }
                    }
                    return newChangeSetData;
                });
            }
        }

        private void addToCsvLine(StringBuilder str, int index, String value) {
            if (index > 0) {
                str.append(CsvZipExportWriter.CSV_DELIMITER);
            }
            str.append(value);
        }
    }

    private static class ChangeSetData {

        iPartsChangeSetSource source;
        SerializedDBDataObjectHistory<SerializedDBDataObject> serializedHistory;
    }

    private static class ModifiedAttributeData {

        String originalValue;
        String currentValue;
        DBDataObjectAttribute.TYPE type;
        String lastModificationDate;
        SerializedDBDataObjectState state;
        String changeSetGUID;
        boolean isAutoCreated;

        public ModifiedAttributeData(SerializedDBDataObject serializedDataObject, SerializedDBDataObjectAttribute attribute) {
            if (attribute != null) {
                setType(attribute);
                switch (type) {
                    case MULTI_LANGUAGE:
                        if (attribute.getMultiLanguage() != null) {
                            this.currentValue = attribute.getMultiLanguage().getTextId();
                        }
                        if (attribute.getOldMultiLanguage() != null) {
                            this.originalValue = attribute.getOldMultiLanguage().getTextId();
                        }
                        break;
                    case ARRAY:
                        break;
                    case STRING:
                        this.currentValue = attribute.getValue();
                        this.originalValue = attribute.getOldValue();
                        break;
                }
            } else {
                this.type = DBDataObjectAttribute.TYPE.STRING;
            }
            this.lastModificationDate = serializedDataObject.getDateTime();
            this.state = serializedDataObject.getState();
            if (this.state == SerializedDBDataObjectState.REVERTED) { // REVERTED wie DELETED behandeln
                this.state = SerializedDBDataObjectState.DELETED;
            }
            this.isAutoCreated = false;
            if (this.state == SerializedDBDataObjectState.NEW) {
                SerializedDBDataObjectAttribute autoAttribute = serializedDataObject.getAttribute(FIELD_K_WAS_AUTO_CREATED);
                if ((autoAttribute != null) && Utils.objectEquals(autoAttribute.getValue(), SQLStringConvert.booleanToPPString(true))) {
                    isAutoCreated = true;
                }
            }
        }

        private void setType(SerializedDBDataObjectAttribute attribute) {
            this.type = attribute.getType();
            if (this.type == null) {
                this.type = DBDataObjectAttribute.TYPE.STRING;
            }
        }

        public DBDataObjectAttribute.TYPE getType() {
            return type;
        }

        public String getOriginalValue() {
            if ((state == SerializedDBDataObjectState.NEW) || (state == SerializedDBDataObjectState.DELETED)) {
                return currentValue;
            } else {
                return originalValue;
            }
        }

        public String getCurrentValue() {
            if (state == SerializedDBDataObjectState.DELETED) {
                return null; // Bei gelöschten Elementen gibt es keinen aktuellen Wert
            } else {
                return currentValue;
            }
        }

        public String getVisOriginalValue(EtkProject project, Language lang) {
            String value = StrUtils.nullToEmpty(getOriginalValue());
            if (type == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                value = getValueFromDB(project, lang, value);
            }
            return value;
        }

        public String getVisCurrentValue(EtkProject project, Language lang) {
            String value = StrUtils.nullToEmpty(getCurrentValue());
            if (type == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                value = getValueFromDB(project, lang, value);
            }
            return value;
        }

        private String getValueFromDB(EtkProject project, Language lang, String value) {
            if (StrUtils.isValid(value)) {
                EtkRecords recs = project.getEtkDbs().getRecords(TABLE_SPRACHE,
                                                                 new String[]{ FIELD_S_BENENN },
                                                                 new String[]{ FIELD_S_TEXTID, FIELD_S_SPRACH },
                                                                 new String[]{ value, lang.getCode() });
                if (recs.getCount() > 0) {
                    return recs.get(0).getField(FIELD_S_BENENN).getAsString();
                }
            }
            return value;
        }
    }
}
