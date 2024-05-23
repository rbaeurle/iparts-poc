/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGWithPEMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für die Produktionswerke- und AS-Werkeinsatzdaten (WBCT und VBW)
 * sowie für die ereignisgesteuerten Produktionswerkseinsatzdaten (WBRT)
 */
public class FactoryDataImporter extends AbstractDIALOGWithPEMDataImporter implements iPartsConst {

    public static final String WBRT_PREFIX = "WBRT";
    private static final String WBCT_PREFIX = "WBCT";
    public static final String VBW_PREFIX = "VBW";
    private static final String VALID_SESI_VALUE = "E";
    private static final String VALID_SPKZ1_VALUE = "0";
    private static final String TARGET_PREFIX_FOR_CONNECTED_DATA = VBW_PREFIX + "_";

    public static final String IMPORT_TABLENAME_WBCT = TABLE_NAME_PREFIX + WBCT_PREFIX;
    public static final String IMPORT_TABLENAME_WBRT = TABLE_NAME_PREFIX + WBRT_PREFIX;
    public static final String IMPORT_TABLENAME_VBW = TABLE_NAME_PREFIX + VBW_PREFIX;
    public static final String DEST_TABLENAME = TABLE_DA_FACTORY_DATA;

    public static final String PG = "PG";
    public static final String RAS = "RAS";
    public static final String BR = "BR";
    public static final String POSE = "POSE";
    public static final String PV = "PV";
    public static final String WW = "WW";
    public static final String ETZ = "ETZ";
    public static final String AA = "AA";
    public static final String SDATA = "SDATA";
    public static final String PEMA = "PEMA";
    public static final String PEMB = "PEMB";
    public static final String PEMTA = "PEMTA";
    public static final String PEMTB = "PEMTB";
    public static final String STCA = "STCA";
    public static final String STCB = "STCB";
    public static final String SESI = "SESI";

    // Elemente nur bei WBCT und WBRT
    public static final String WK = "WK";
    public static final String SPKZ = "SPKZ";
    public static final String SPKZ1 = "SPKZ1";
    public static final String CRN = "CRN";
    public static final String ADAT = "ADAT";
    public static final String TEIL = "TEIL";

    // Elemente, die nur bei WBRT auftauschen
    public static final String WBRT_EREIA = "WBRT_EREIA";     // nur WBRT
    public static final String WBRT_EREIB = "WBRT_EREIB";     // nur WBRT

    // Suffix für Sonderfelder von WBCT und WBRT
    public static final String WBXT_OLD_SUFFIX = "_ALT"; // Daten vor der Jahresbereinigung -> soll bei WBCT verwendet werden

    public static final String VBW_WERK = "VBW_WERK";

    private String importTableInXML;
    private HashMap<String, String> mapping;
    private String[] primaryKeysImport;
    private String prefixForImporterInstance;
    private iPartsFactoryDataTypes currentDatasetId;
    // Für VBW und WBRT Import. Id ist die Id ohne ADAT und ohne Sequenznummer.
    private HashMap<iPartsFactoryDataId, DataObjectForIdWithoutAdatAndSeqNo> processedDatasets;
    private Map<String, List<String>> bcteKeyForLinkedFactoryDataMap;
    private HashSet<Long> adatAsLongSet;
    private List<Map<String, String>> connectedBCTEFactoryData; // Liste mit Werksdaten für gekoppelte BCTE Schlüssel
    private boolean importToDB = true;
    private boolean doBufferedSave = true;
    private FactoryDataImportHelper factoryDataImportHelper;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public FactoryDataImporter(EtkProject project, String xmlTableName) {
        super(project, "Invalid Importer");
        // Tabellenname aus der XML Datei
        this.importTableInXML = xmlTableName;
        initImporter();
        setIsImportWithOnlyNewDataObjects(true); // Um Probleme bei Daimler mit BufferedSave zu vermeiden
    }

    /**
     * Initialisiert den Importer.
     */
    private void initImporter() {
        String fileListName = setImportConfigurationForDataType();
        // Setzen des FileListTypes für den Importdialog
        importFileTypes = new FilesImporterFileListType[]{ new FilesImporterFileListType(TABLE_DA_FACTORY_DATA,
                                                                                         fileListName, false, false, true,
                                                                                         new String[]{ MimeTypes.EXTENSION_XML }) };
    }

    private String getSourceFieldName(String fieldNameSuffix) {
        // Bei PEMA, PEMTA und STCA müssen bei WBCT die Quellfeldnamen *_ALT verwendet werden (Daten vor der Jahresbereinigung)
        if (isConstructionFactoryDataData() && (fieldNameSuffix.equals(PEMA) || fieldNameSuffix.equals(PEMTA) || fieldNameSuffix.equals(STCA))) {
            return prefixForImporterInstance + fieldNameSuffix + WBXT_OLD_SUFFIX;
        } else {
            return prefixForImporterInstance + fieldNameSuffix;
        }
    }

    private boolean isConstructionFactoryDataData() {
        return importTableInXML.equals(IMPORT_TABLENAME_WBCT) || importTableInXML.equals(IMPORT_TABLENAME_WBRT);
    }

    /**
     * Setzt die Einstellungen zum aktuellen Import-Dateityp.
     *
     * @return
     */
    private String setImportConfigurationForDataType() {
        mapping = new HashMap<>();
        prefixForImporterInstance = "";
        String fileListName = "";
        // Unterscheidung WBCT - WBRT - VBW
        if (importTableInXML.equals(IMPORT_TABLENAME_WBCT)) {
            importName = "!!DIALOG-Werkseinsatzdaten (WBCT)";
            fileListName = DFD_FACTORY_DATA;
            initConstructionFactoryData(WBCT_PREFIX);
        } else if (importTableInXML.equals(IMPORT_TABLENAME_WBRT)) {
            importName = "!!DIALOG-Werkseinsatzdaten (WBRT)";
            fileListName = DFD_FACTORY_DATA_EVENT;
            initConstructionFactoryData(WBRT_PREFIX);
            mapping.put(FIELD_DFD_EVENT_FROM, WBRT_EREIA);
            mapping.put(FIELD_DFD_EVENT_TO, WBRT_EREIB);
        } else if (importTableInXML.equals(IMPORT_TABLENAME_VBW)) {
            currentDatasetId = iPartsFactoryDataTypes.FACTORY_DATA_AS;
            prefixForImporterInstance = VBW_PREFIX + "_";
            fileListName = DFD_FACTORY_DATA_AS;
            importName = "!!DIALOG-Werkseinsatzdaten AS (VBW)";
            mapping.put(FIELD_DFD_FACTORY, VBW_WERK);
            primaryKeysImport = new String[]{ VBW_WERK };
        }
        primaryKeysImport = StrUtils.mergeArrays(primaryKeysImport, getSourceFieldName(RAS), getSourceFieldName(BR),
                                                 getSourceFieldName(POSE), getSourceFieldName(PV),
                                                 getSourceFieldName(WW), getSourceFieldName(ETZ),
                                                 getSourceFieldName(AA), getSourceFieldName(SDATA));

        mapping.put(FIELD_DFD_PRODUCT_GRP, getSourceFieldName(PG));
        mapping.put(FIELD_DFD_SERIES_NO, getSourceFieldName(BR));
        mapping.put(FIELD_DFD_POSE, getSourceFieldName(POSE));
        mapping.put(FIELD_DFD_POSV, getSourceFieldName(PV));
        mapping.put(FIELD_DFD_WW, getSourceFieldName(WW));
        mapping.put(FIELD_DFD_ET, getSourceFieldName(ETZ));
        mapping.put(FIELD_DFD_AA, getSourceFieldName(AA));
        mapping.put(FIELD_DFD_SDATA, getSourceFieldName(SDATA));
        mapping.put(FIELD_DFD_PEMA, getSourceFieldName(PEMA));
        mapping.put(FIELD_DFD_PEMB, getSourceFieldName(PEMB));
        mapping.put(FIELD_DFD_PEMTA, getSourceFieldName(PEMTA));
        mapping.put(FIELD_DFD_PEMTB, getSourceFieldName(PEMTB));
        mapping.put(FIELD_DFD_STCA, getSourceFieldName(STCA));
        mapping.put(FIELD_DFD_STCB, getSourceFieldName(STCB));

        factoryDataImportHelper = new FactoryDataImportHelper(getProject(), mapping, DEST_TABLENAME);

        return fileListName;
    }

    /**
     * Initilaisiert die Strukturen für Werkseinsatzdaten Konstruktion (WBCT und WBRT)
     *
     * @param prefix
     */
    private void initConstructionFactoryData(String prefix) {
        if (isConstructionFactoryDataData()) {
            prefixForImporterInstance = prefix + "_";
            currentDatasetId = iPartsFactoryDataTypes.FACTORY_DATA_CONSTRUCTION;
            mapping.put(FIELD_DFD_FACTORY, getSourceFieldName(WK));
            mapping.put(FIELD_DFD_SPKZ, getSourceFieldName(SPKZ));
            mapping.put(FIELD_DFD_CRN, getSourceFieldName(CRN));
            mapping.put(FIELD_DFD_ADAT, getSourceFieldName(ADAT));
            primaryKeysImport = new String[]{ getSourceFieldName(WK), getSourceFieldName(SPKZ), getSourceFieldName(ADAT) };
        }
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysImport);
        importer.setMustHaveData(new String[]{ getSourceFieldName(BR),
                                               getSourceFieldName(RAS) });

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(TABLE_NAME_PREFIX + currentDatasetId.getDatasetValue())
                   || importer.getTableNames().get(0).equals(IMPORT_TABLENAME_WBRT)
                   || importer.getTableNames().get(0).equals(WBRT_PREFIX)
                   || importer.getTableNames().get(0).equals(VBW_PREFIX);
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        processedDatasets = new HashMap<>();
        connectedBCTEFactoryData = new DwList<>();
        adatAsLongSet = new HashSet<>();
        setBufferedSave(doBufferedSave);
        bcteKeyForLinkedFactoryDataMap = iPartsDataDialogDataList.loadAllBCTEKeysForLinkedFactoryDataGuid(getProject());
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        boolean isVBWImport = importTableInXML.equals(IMPORT_TABLENAME_VBW);
        boolean isMarkedForDeletion = DIALOGImportHelper.isDatasetMarkedForDeletion(importRec);
        // Den Werksdatensatz erzeugen und initial befüllen
        iPartsDataFactoryData factoryData = preprocessFactoryData(factoryDataImportHelper, importRec, recordNo, isVBWImport, isMarkedForDeletion);
        if (factoryData == null) {
            return;
        }
        // Und nun den Datensatz abhängig vom Typ verarbeiten
        handleProcessedDataSet(factoryData, importRec, isVBWImport, isMarkedForDeletion, false, factoryDataImportHelper);
    }

    private iPartsDataFactoryData preprocessFactoryData(FactoryDataImportHelper factoryDataImportHelper, Map<String, String> importRec,
                                                        int recordNo, boolean isVBWImport, boolean isMarkedForDeletion) {
        // SPKZ1 muss "0" sein beim WBCT/WBRT-Import
        if (isConstructionFactoryDataData()) {
            String spkz1Value = factoryDataImportHelper.handleValueOfSpecialField(getSourceFieldName(SPKZ1), importRec);
            if (!StrUtils.stringEquals(spkz1Value, VALID_SPKZ1_VALUE)) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. SPKZ1 Wert der Importdatei \"%2\" ungleich \"%3\"",
                                                            String.valueOf(recordNo), spkz1Value, VALID_SPKZ1_VALUE),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                reduceRecordCount();
                return null;
            }
        } else {
            // VBW Daten haben kein ADAT, daher wird hier ein künstliches ADAT erzeugt, sofern kein ADAT aus einem verlinkten
            // WBRT bzw. WBCT Datensatz gesetzt wurde. Diese wird auch für die PEM Stammdaten verwendet
            handleVBWAdat(factoryDataImportHelper, importRec);
        }

        // PEM Stamm anlegen
        handlePEMData(importRec, factoryDataImportHelper);
        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!factoryDataImportHelper.checkImportRelevanceForSeries(getSourceFieldName(BR), importRec, getInvalidSeriesSet(), this)) {
            return null;
        }
        // DAIMLER-2502: Nur Datensätze übernehmen, die als SESI Wert = "E" haben
        String sesiValue = factoryDataImportHelper.handleValueOfSpecialField(getSourceFieldName(SESI), importRec);
        if ((sesiValue == null) || !StrUtils.stringEquals(sesiValue, VALID_SESI_VALUE)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. SESI Wert der Importdatei \"%2\" ungleich erlaubten SESI Wert \"%3\"",
                                                        String.valueOf(recordNo), sesiValue, VALID_SESI_VALUE),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return null;
        }
        // Wenn PEMTA_ALT und PEMA_ALT nicht gesetzt sind -> Record überspringen und Meldung ausgeben
        if (!isMarkedForDeletion && !factoryDataImportHelper.isPemaAndPemtaValid(importRec, recordNo)) {
            return null;
        }

        // HMMSM Id wird bei beiden Logiken benötigt
        HmMSmId hmMSmId = factoryDataImportHelper.getHmSmId(importRec);
        iPartsDataFactoryData factoryData = null;

        // Unterscheidung der Werkseinsatzdaten
        if (isConstructionFactoryDataData()) {
            factoryData = handleImportLogicForWBCT(importRec, recordNo, factoryDataImportHelper, hmMSmId);
        } else if (isVBWImport) {
            factoryData = handleImportLogicForVBW(importRec, recordNo, factoryDataImportHelper, hmMSmId);
        }
        // Falls dataObject == null, überspringen. Logmessage wurde in tieferer Methode erstellt
        if (factoryData == null) {
            reduceRecordCount();
            return null;
        }

        iPartsFactoryDataId factoryDataId = factoryData.getAsId();
        iPartsDataReleaseState datasetState;
        iPartsDialogBCTEPrimaryKey bcteKey = factoryDataId.getBCTEPrimaryKey();
        boolean isInitialDataImport = isDIALOGInitialDataImport();

        if (isInitialDataImport || !iPartsFactories.getInstance(getProject()).isValidForFilter(factoryDataId.getFactory())) {
            // Status wird direkt auf "freigegeben" gesetzt, wenn:
            // 1. Datensätze aus der Urladung kommen
            // 2. das Werk irrelevant ist
            datasetState = iPartsDataReleaseState.RELEASED;
        } else {
            datasetState = factoryDataImportHelper.calculateReleaseStateByUsage(bcteKey);
        }
        if (isMarkedForDeletion) {
            // Im Fall von Löschdatensätzen bedeutet NEW dann CHECK_DELETION und RELEASED dann DELETED
            if (datasetState == iPartsDataReleaseState.NEW) {
                datasetState = iPartsDataReleaseState.CHECK_DELETION;
            } else {
                datasetState = iPartsDataReleaseState.DELETED;
            }
        }

        if (!factoryData.existsInDB()) {
            factoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        } else if (factoryData.getSource() != iPartsImportDataOrigin.MAD) {
            // Nichts importieren wenn es den Datensatz schon gibt, außer der Datensatz hat Quelle MAD...
            reduceRecordCount();
            return null;
        }
        // ... Im Fall von MAD wird der existierende Datensatz überschrieben
        factoryDataImportHelper.deleteContentIfMADSource(factoryData, FIELD_DFD_SOURCE, false);

        factoryDataImportHelper.fillOverrideCompleteDataForDIALOGReverse(factoryData, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
        // Setzen der separaten HMMSM Felder
        factoryDataImportHelper.setHmMSmFields(factoryData, hmMSmId);

        // Die Importdatenquelle auf DIALOG setzen
        factoryData.setFieldValue(FIELD_DFD_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
        // Status setzen falls leer oder Urladung
        if (isInitialDataImport || factoryData.getFieldValue(FIELD_DFD_STATUS).isEmpty()) {
            factoryData.setFieldValue(FIELD_DFD_STATUS, datasetState.getDbValue(), DBActionOrigin.FROM_EDIT);
        }
        // Redundante BCTE-Felder mit den Werten aus der GUID aktualisieren
        factoryData.updateDialogBCTEFields(DBActionOrigin.FROM_EDIT);
        return factoryData;
    }

    /**
     * Fügt bei einem AS Datensatz ein künstliches ADAT hinzu, weil die Originaldaten kein ADAT besitzen und wir eins
     * zur Abgrenzung der Daten benötigen.
     *
     * @param factoryDataImportHelper
     * @param importRec
     */
    private void handleVBWAdat(FactoryDataImportHelper factoryDataImportHelper, Map<String, String> importRec) {
        if (!isConstructionFactoryDataData()) {
            // Hier muss der Import unterschieden werden und bei VBW-Import der Zeitstempel (DFD_ADAT) künstlich gesetzt werden,
            // da kein entsprechender Wert in der Importdatei vorhanden ist.
            // Und: Jede Änderung muss den Zeitstempel erhöhen um so etwas wie eine Historie zu bekommen.
            String adat = factoryDataImportHelper.handleValueOfSpecialField(getSourceFieldName(ADAT), importRec);
            if (StrUtils.isEmpty(adat)) {
                String formatedTime = iPartsDialogDateTimeHandler.getNextDBDateTimeForExistingDateTimes(adatAsLongSet);
                importRec.put(getSourceFieldName(ADAT), formatedTime);
            } else {
                // VBWs können doch ein "echtes" Datum besitzen, wenn sie Ergebnisse einer Verlinkung von BCTE Schlüsseln
                // sind
                adat = factoryDataImportHelper.getDIALOGDateTimeValue(adat);
                if (StrUtils.isValid(adat)) {
                    importRec.put(getSourceFieldName(ADAT), adat);
                }
            }
        }
    }

    private void handleProcessedDataSet(iPartsDataFactoryData factoryData, Map<String, String> importRec,
                                        boolean isVBWImport, boolean isMarkedForDeletion, boolean isHandleConstAsRetailData,
                                        FactoryDataImportHelper factoryDataImportHelper) {
        iPartsDialogBCTEPrimaryKey bcteKey = factoryData.getAsId().getBCTEPrimaryKey();

        iPartsFactoryDataId factoryDataId = factoryData.getAsId();
        // Id ohne ADAT, da ADAT bei VBW der aktuelle Timestamp ist und nicht Teil der Importdaten. Sonst würden nie gleiche Ids rauskommen
        iPartsFactoryDataId factoryDataIdWithoutADATAndSeqNo = new iPartsFactoryDataId(factoryDataId.getGuid(), factoryDataId.getFactory(),
                                                                                       factoryDataId.getSplitAttribute(), "",
                                                                                       factoryDataId.getDataId(), "");

        // Objekt, das alle Daten für eine ID ohne Adat und ohne Sequenznummer aus der DB und dem Import hält
        DataObjectForIdWithoutAdatAndSeqNo allDatasetsWithSpecialId = processedDatasets.get(factoryDataIdWithoutADATAndSeqNo);
        if (allDatasetsWithSpecialId == null) {
            allDatasetsWithSpecialId = new DataObjectForIdWithoutAdatAndSeqNo(isVBWImport);
            allDatasetsWithSpecialId.setReferencedSeriesNumber(bcteKey.seriesNo);
            allDatasetsWithSpecialId.setReferencedBCTEKey(factoryData.getGUID());
            iPartsDataFactoryDataList factoryDataHistoryList = iPartsDataFactoryDataList.loadAfterSalesFactoryDataListWithHistoryData(getProject(), factoryDataIdWithoutADATAndSeqNo);
            allDatasetsWithSpecialId.setDBData(factoryDataHistoryList.getAsList());
            processedDatasets.put(factoryDataIdWithoutADATAndSeqNo, allDatasetsWithSpecialId);
        } else {
            // Es sind schon Datensätze zu diesem Id-Rumpf vorhanden. Kontrollieren, ob die Seq richtig ist
            // Diese ist schon die höchste von den Daten aus der DB, aber nicht unbedingt von denen aus der Importdatei
            // Anpassen der Seq ist nur für WBRT relevant
            if (!isVBWImport) {
                if (!allDatasetsWithSpecialId.getImportDataObjects().isEmpty()) {
                    iPartsDataFactoryData highestFactoryData = allDatasetsWithSpecialId.getNewestDataObject(factoryData.getAsId().getAdat());
                    if (highestFactoryData != null) {
                        String highestSeqNoStr = highestFactoryData.getFieldValue(FIELD_DFD_SEQ_NO);
                        int highestSeqNo = StrUtils.strToIntDef(highestSeqNoStr, 0);
                        String newHighestSeqNo = EtkDbsHelper.formatLfdNr(highestSeqNo + 1);
                        factoryData.setFieldValue(FIELD_DFD_SEQ_NO, newHighestSeqNo, DBActionOrigin.FROM_EDIT);
                        factoryData.updateOldId();
                    }
                }
            }
        }

        boolean addedSuccessfully = false;
        // VBW Datensätze können im Gegensatz zu WBCT Löschkennzeichen enthalten
        if (isMarkedForDeletion) {
            // VBW Datensätze werden im postImportTask gespeichert
            allDatasetsWithSpecialId.addImportedDeletionData(factoryData, this, mapping);
        } else {
            addedSuccessfully = allDatasetsWithSpecialId.addImportedData(factoryData, this, mapping);
        }

        // Nur die gekoppelten Datensätze abarbeiten, wenn der dazugehörige Datensatz auch wirklich
        // importiert wird. (z.B. nicht bei Duplikaten)
        if (addedSuccessfully) {
            // Falls es sich um ein Prototypenwerk handelt, sollen die gekoppelten Werksdaten nicht aktualisiert werden
            if (iPartsFactories.getInstance(getProject()).isValidForFilter(factoryDataId.getFactory())) {
                if (isVBWImport) {
                    // Falls es eine BCTE Schlüssel Verknüpfung gibt, muss dafür ein Fake-ImportRecord angelegt werden
                    List<String> connectedBCTEKeys = bcteKeyForLinkedFactoryDataMap.get(bcteKey.createDialogGUID());
                    if (connectedBCTEKeys != null) {
                        for (String connectedBCTEKey : connectedBCTEKeys) {
                            // Den verknüpften BCTE Schlüssel bestimmen
                            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(connectedBCTEKey);
                            if (bctePrimaryKey != null) {
                                Map<String, String> importRecForConnectedData = new HashMap<>(importRec);
                                if (!isHandleConstAsRetailData) {
                                    // DIe Felder aus dem BCTE Schlüssel in das neuen importRec kopieren
                                    addBCTEKeyValues(importRecForConnectedData, bctePrimaryKey);
                                    connectedBCTEFactoryData.add(importRecForConnectedData);
                                } else {
                                    // In diesen Zweig kommt man nur, wenn wir aus importConstDataAsRetailData kommen. Es wird
                                    // der Ziel BCTE Schlüssel der Kopplung behandelt. Ist er als Schlüssel in bcteKeyforLinkedFactoryDataMap,
                                    // dann ist das Ziel gleichzeitig eine Quelle und es ist irgendwie eine Kette entstanden.
                                    getMessageLog().fireMessage(translateForLog("!!Der Ziel BCTE Schlüssel %1 dieser Kopplung ist gleichzeitig der Quell BCTE Schlüssel" +
                                                                                "einer anderen Kopplung", bcteKey.toString()),
                                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                                }
                            }
                        }
                    }
                } else {
                    if (importToDB) {
                        // Falls es eine BCTE Schlüssel Verknüpfung gibt, muss dafür ein Fake-ImportRecord angelegt werden
                        List<String> connectedBCTEKeys = bcteKeyForLinkedFactoryDataMap.get(bcteKey.createDialogGUID());
                        if (connectedBCTEKeys != null) {
                            for (String connectedBCTEKey : connectedBCTEKeys) {
                                // Den verknüpften BCTE Schlüssel bestimmen
                                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(connectedBCTEKey);
                                if (bctePrimaryKey != null) {
                                    // Bei WBRT/WBCT muss der Prefix bei jedem Feld angepasst werden
                                    Map<String, String> importRecForConnectedData = new HashMap<>();
                                    for (Map.Entry<String, String> entry : importRec.entrySet()) {
                                        String prefix;
                                        if (importTableInXML.equals(IMPORT_TABLENAME_WBCT)) {
                                            prefix = WBCT_PREFIX;
                                        } else {
                                            prefix = WBRT_PREFIX;
                                        }
                                        String newKey;
                                        if (entry.getKey().equals(getSourceFieldName(WK))) {
                                            newKey = VBW_WERK;
                                        } else {
                                            newKey = StrUtils.replaceSubstring(entry.getKey(), prefix, VBW_PREFIX);
                                        }

                                        String entryValue = entry.getValue();

                                        // Der WBRT Datensatz verwendet die Daten vor der Jahresbereinigung (Feld ALT)
                                        // Auch der generierte VBW Datensatz muss diese Daten nehmen
                                        if (newKey.equals(TARGET_PREFIX_FOR_CONNECTED_DATA + PEMA)) {
                                            entryValue = importRec.get(getSourceFieldName(PEMA));
                                        } else if (newKey.equals(TARGET_PREFIX_FOR_CONNECTED_DATA + PEMTA)) {
                                            entryValue = importRec.get(getSourceFieldName(PEMTA));
                                        } else if (newKey.equals(TARGET_PREFIX_FOR_CONNECTED_DATA + STCA)) {
                                            entryValue = importRec.get(getSourceFieldName(STCA));
                                        } else if (newKey.equals(TARGET_PREFIX_FOR_CONNECTED_DATA + ADAT)) {
                                            entryValue = factoryDataImportHelper.handleValueOfSpecialField(getSourceFieldName(ADAT), importRec);
                                        }

                                        importRecForConnectedData.put(newKey, entryValue);
                                    }

                                    addBCTEKeyValues(importRecForConnectedData, bctePrimaryKey);
                                    connectedBCTEFactoryData.add(importRecForConnectedData);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addBCTEKeyValues(Map<String, String> importRecForConnectedData, iPartsDialogBCTEPrimaryKey bctePrimaryKey) {
        importRecForConnectedData.put(TARGET_PREFIX_FOR_CONNECTED_DATA + BR, bctePrimaryKey.seriesNo);
        importRecForConnectedData.put(TARGET_PREFIX_FOR_CONNECTED_DATA + RAS, bctePrimaryKey.hm + bctePrimaryKey.m + bctePrimaryKey.sm);
        importRecForConnectedData.put(TARGET_PREFIX_FOR_CONNECTED_DATA + POSE, bctePrimaryKey.getPosE());
        importRecForConnectedData.put(TARGET_PREFIX_FOR_CONNECTED_DATA + PV, bctePrimaryKey.getPosV());
        importRecForConnectedData.put(TARGET_PREFIX_FOR_CONNECTED_DATA + WW, bctePrimaryKey.getWW());
        importRecForConnectedData.put(TARGET_PREFIX_FOR_CONNECTED_DATA + ETZ, bctePrimaryKey.getET());
        importRecForConnectedData.put(TARGET_PREFIX_FOR_CONNECTED_DATA + AA, bctePrimaryKey.getAA());
        importRecForConnectedData.put(TARGET_PREFIX_FOR_CONNECTED_DATA + SDATA, "0" + bctePrimaryKey.getSData());
    }

    @Override
    public void postImportTask() {
        boolean hasConnectedBCTEData = (connectedBCTEFactoryData != null) && !connectedBCTEFactoryData.isEmpty();
        // Wenn verknüpfte ImportRecs existieren, dann müssen diese am Ende als eigene ImportRecs durch den Importer
        // laufen
        if (hasConnectedBCTEData) {
            getMessageLog().fireMessage(translateForLog("!!Behandle %1 gekoppelte Werkseinsatzdaten...", String.valueOf(connectedBCTEFactoryData.size())),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

            // Bin ich ein WBRT/WBCT Import und habe verknüpfte BCTE Datensätze, dann müssen diese als VBW Daten
            // verarbeitet werden -> Importeinstellungen ändern
            Optional<String> oldImportTableXML = switchFromConstToRetailData();
            oldImportTableXML.ifPresent(value -> {
                try {
                    // Daten als VBW importieren
                    importConstDataAsRetailData();
                } finally {
                    // Einstellungen wieder zurücksetzen
                    switchFromRetailToConstData(value);
                }
            });
        }

        // Aufgesammelte PEMs importieren
        importPEMData();

        if (importTableInXML.equals(IMPORT_TABLENAME_VBW)) {
            postImportTaskForVBW();
        } else {
            // VBW und WBRT sind gemischt in processedDatasets bei gekoppelten Datensätzen
            // Es befinden sich keine Löschdatensätze bei gekoppelten oder WBRT-Datensätzen
            // postImportTaskForWBRT reicht
            postImportTaskForWBRT();
        }

        setBufferedSave(doBufferedSave);
        // PEMs am Ende einer Importdatei leeren
        clearPEMs();
        adatAsLongSet = null;
        processedDatasets = null;
        factoryDataImportHelper = null;
    }

    /**
     * Importiert Werksdaten aus der Produktion als AS Werksdaten
     */
    private void importConstDataAsRetailData() {
        // Datensätze durch den Import jagen
        for (Map<String, String> connectedFactoryData : new ArrayList<>(connectedBCTEFactoryData)) {
            boolean isMarkedForDeletion = DIALOGImportHelper.isDatasetMarkedForDeletion(connectedFactoryData);
            iPartsDataFactoryData factoryData = preprocessFactoryData(factoryDataImportHelper, connectedFactoryData, 0, true, isMarkedForDeletion);
            if (factoryData == null) {
                continue;
            }
            // Der Datensatz muss als "gekoppelt" markiert werden und die Quelle "IPARTS" haben
            factoryData.setFieldValueAsBoolean(FIELD_DFD_LINKED, true, DBActionOrigin.FROM_EDIT);
            factoryData.setFieldValue(FIELD_DFD_SOURCE, iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);
            handleProcessedDataSet(factoryData, connectedFactoryData, true, isMarkedForDeletion, true, factoryDataImportHelper);
        }
    }

    /**
     * Stellt die Einstellungen des Importers um - von AS Import (VBW) zu Produktion Import (WBRT/WBCT)
     *
     * @param oldImportTableXML
     */
    private void switchFromRetailToConstData(String oldImportTableXML) {
        if (StrUtils.isValid(oldImportTableXML)) {
            importTableInXML = oldImportTableXML;
            // Hier zurück zu den WBRT Einstellungen wechseln, damit der Importer beim Abschließen des Imports
            // die initialen Daten hat
            setImportConfigurationForDataType();
        }
    }

    /**
     * Stellt die Einstellungen des Importers um - von Produktion Import (WBRT/WBCT) zu AS Import (VBW)
     *
     * @return
     */
    private Optional<String> switchFromConstToRetailData() {
        if (isConstructionFactoryDataData()) {
            String oldImportTableXML = importTableInXML;
            importTableInXML = IMPORT_TABLENAME_VBW;
            // Einstellungen
            setImportConfigurationForDataType();
            return Optional.ofNullable(oldImportTableXML);
        }
        return Optional.empty();
    }

    private void postImportTaskForVBW() {
        saveBufferListToDB(true);
        int nDatasetsMarkedForDeletion = 0;
        GenericEtkDataObjectList<iPartsDataDIALOGChange> dialogChangesDeleteList = new GenericEtkDataObjectList<>();
        if (!isCancelled()) {
            for (DataObjectForIdWithoutAdatAndSeqNo vbwData : processedDatasets.values()) {
                if (vbwData.getImportDeletionDataObject() != null) {
                    nDatasetsMarkedForDeletion++;
                }

                if (importToDB) {
                    vbwData.saveImportDataObjects(this, factoryDataImportHelper);
                }
            }
            getMessageLog().fireMessage(translateForLog("!!Lege %1 Löschdatensätze an", String.valueOf(nDatasetsMarkedForDeletion)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (importToDB) {
                for (DataObjectForIdWithoutAdatAndSeqNo vxData : processedDatasets.values()) {
                    vxData.saveDeletionDataObject(this, factoryDataImportHelper, dialogChangesDeleteList);
                }
            }
            getMessageLog().fireMessage(translateForLog("!!Es wurden %1 Löschdatensätze angelegt", String.valueOf(nDatasetsMarkedForDeletion)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
        super.postImportTask();
        if (!isCancelled() && importToDB) {
            // Löschen der DIALOG_CHANGES erst ganz zum Schluss
            dialogChangesDeleteList.deleteFromDB(getProject(), true);
        }
    }

    private void postImportTaskForWBRT() {
        saveBufferListToDB(true);
        if (!isCancelled()) {
            if (importToDB) {
                for (DataObjectForIdWithoutAdatAndSeqNo vbwData : processedDatasets.values()) {
                    vbwData.saveImportDataObjects(this, factoryDataImportHelper);
                }
            }
        }
        super.postImportTask();
    }

    private iPartsDataFactoryData handleImportLogicForVBW(Map<String, String> importRec, int recordNo, FactoryDataImportHelper factoryDataImportHelper, HmMSmId hmMSmId) {
        // Keine spezielle Logik (laut WikiPage)
        iPartsDialogBCTEPrimaryKey primaryBCTEKey = factoryDataImportHelper.getPrimaryBCTEKey(this, importRec, hmMSmId, recordNo);
        if (primaryBCTEKey == null) {
            factoryDataImportHelper.cancelImporterDueToIncorrectBCTEKey(this, recordNo);
            return null;
        }
        iPartsFactoryDataId factoryDataASId = factoryDataImportHelper.getFactoryDataASId(importRec, primaryBCTEKey);
        return new iPartsDataFactoryData(getProject(), factoryDataASId);
    }

    private iPartsDataFactoryData handleImportLogicForWBCT(Map<String, String> importRec, int recordNo, FactoryDataImportHelper factoryDataImportHelper, HmMSmId hmMSmId) {
        iPartsDialogBCTEPrimaryKey primaryBCTEKey = factoryDataImportHelper.getPrimaryBCTEKey(this, importRec, hmMSmId, recordNo);
        if (primaryBCTEKey == null) {
            factoryDataImportHelper.cancelImporterDueToIncorrectBCTEKey(this, recordNo);
            return null;
        }
        // Wenn kein dazugehöriger BCTE Datensatz existiert -> mit Warnung trotzdem importieren
        if (!iPartsDataDialogDataList.existsInDB(getProject(), primaryBCTEKey)) {
            //Ausgabe der factoryDataId war hier falsch, muss primaryBCTEKey.toString() sein
            getMessageLog().fireMessage(translateForLog("!!Record %1 ungültig (kein passender BCTE Datensatz gefunden. BCTE Schlüssel aus %2: %3)",
                                                        String.valueOf(recordNo),
                                                        StrUtils.replaceFirstSubstring(importTableInXML, TABLE_NAME_PREFIX, ""),
                                                        primaryBCTEKey.toString()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
        iPartsFactoryDataId factoryDataId = factoryDataImportHelper.getFactoryDataId(importRec, primaryBCTEKey);
        return new iPartsDataFactoryData(getProject(), factoryDataId);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_FACTORY_DATA)) {
            getProject().getDbLayer().delete(TABLE_DA_FACTORY_DATA, new String[]{ FIELD_DFD_DATA_ID }, new String[]{ currentDatasetId.getDbValue() });
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_DA_FACTORY_DATA)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    @Override
    protected String getSTCFieldname(boolean isFrom) {
        if (isFrom) {
            return getSourceFieldName(STCA);
        } else {
            return getSourceFieldName(STCB);
        }
    }

    @Override
    protected String getPEMTFieldname(boolean isFrom) {
        if (isFrom) {
            return getSourceFieldName(PEMTA);
        } else {
            return getSourceFieldName(PEMTB);
        }
    }

    @Override
    protected String getPemFieldName(boolean isFrom) {
        if (isFrom) {
            return getSourceFieldName(PEMA);
        } else {
            return getSourceFieldName(PEMB);
        }
    }

    @Override
    protected String getFactoryFieldname() {
        if (isConstructionFactoryDataData()) {
            return getSourceFieldName(WK);
        } else {
            return VBW_WERK;
        }
    }

    @Override
    protected String getADATFieldname() {
        return getSourceFieldName(ADAT);
    }

    private class FactoryDataImportHelper extends DIALOGImportHelper {

        public FactoryDataImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(getSourceFieldName(SDATA))) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(getSourceFieldName(ADAT))) {
                // Das ADAT für VBW wurde von uns gesetzt und darf durch den DateTimeHelper nicht verändert werden
                if (isConstructionFactoryDataData()) {
                    value = getDIALOGDateTimeValue(value);
                }
            } else if (sourceField.equals(getSourceFieldName(PEMA)) || sourceField.equals(getSourceFieldName(PEMB))) {
                value = value.trim();
            } else if (sourceField.equals(getSourceFieldName(PEMTA)) || sourceField.equals(getSourceFieldName(PEMTB))) {
                value = getDIALOGDateValueForPEMT(value);
            } else if (sourceField.equals(getSourceFieldName(TEIL))) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(getSourceFieldName(SESI))) {
                value = value.toUpperCase().trim();
            } else if (sourceField.equals(getSourceFieldName(BR))) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }

        private HmMSmId getHmSmId(Map<String, String> importRec) {
            return HmMSmId.getIdFromRaster(handleValueOfSpecialField(getSourceFieldName(BR), importRec),
                                           handleValueOfSpecialField(getSourceFieldName(RAS), importRec));
        }

        private void setHmMSmFields(iPartsDataFactoryData factoryData, HmMSmId hmMSmId) {
            factoryData.setAttributeValue(FIELD_DFD_HM, hmMSmId.getHm(), DBActionOrigin.FROM_EDIT);
            factoryData.setAttributeValue(FIELD_DFD_M, hmMSmId.getM(), DBActionOrigin.FROM_EDIT);
            factoryData.setAttributeValue(FIELD_DFD_SM, hmMSmId.getSm(), DBActionOrigin.FROM_EDIT);
        }

        private iPartsDialogBCTEPrimaryKey getPrimaryBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, HmMSmId hmMSmId, int recordNo) {
            return getPartListPrimaryBCTEKey(importer, recordNo, hmMSmId,
                                             handleValueOfSpecialField(getSourceFieldName(POSE), importRec),
                                             handleValueOfSpecialField(getSourceFieldName(PV), importRec),
                                             handleValueOfSpecialField(getSourceFieldName(WW), importRec),
                                             handleValueOfSpecialField(getSourceFieldName(ETZ), importRec),
                                             handleValueOfSpecialField(getSourceFieldName(AA), importRec),
                                             handleValueOfSpecialField(getSourceFieldName(SDATA), importRec));
        }

        private iPartsFactoryDataId getFactoryDataId(Map<String, String> importRec, iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
            iPartsFactoryDataId factoryDataId = new iPartsFactoryDataId(primaryBCTEKey.createDialogGUID(),
                                                                        handleValueOfSpecialField(getSourceFieldName(WK), importRec),
                                                                        handleValueOfSpecialField(getSourceFieldName(SPKZ), importRec),
                                                                        handleValueOfSpecialField(getSourceFieldName(ADAT), importRec),
                                                                        currentDatasetId.getDbValue());
            int i = 1;
            while (new iPartsDataFactoryData(getProject(), factoryDataId).existsInDB()) {
                String seqNo = EtkDbsHelper.formatLfdNr(i);
                factoryDataId = new iPartsFactoryDataId(primaryBCTEKey.createDialogGUID(),
                                                        handleValueOfSpecialField(getSourceFieldName(WK), importRec),
                                                        handleValueOfSpecialField(getSourceFieldName(SPKZ), importRec),
                                                        handleValueOfSpecialField(getSourceFieldName(ADAT), importRec),
                                                        currentDatasetId.getDbValue(),
                                                        seqNo);
                i++;
            }
            return factoryDataId;
        }

        private iPartsFactoryDataId getFactoryDataASId(Map<String, String> importRec, iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
            return new iPartsFactoryDataId(primaryBCTEKey.createDialogGUID(), handleValueOfSpecialField(VBW_WERK, importRec),
                                           "", handleValueOfSpecialField(getSourceFieldName(ADAT), importRec),
                                           currentDatasetId.getDbValue());
        }

        /**
         * Überprüft, ob die XML Elemente "PEMTA_ALT" und "PEMA_ALT" gültige Werte enthalten. Falls nicht, Meldung in
         * der Log-Asugabe.
         *
         * @param importRec
         * @param recordNo
         * @return
         */
        private boolean isPemaAndPemtaValid(Map<String, String> importRec, int recordNo) {
            String[] pemFields = { PEMA, PEMTA };
            Boolean retValue = true;
            for (String pemField : pemFields) {
                // Wenn das PEMA_ALT oder PEMTA_ALT Element leer ist oder nicht existiert, dann soll der Datensatz übersprungen werden
                pemField = getSourceFieldName(pemField);
                String pemValue = importRec.get(pemField);
                if ((pemValue == null) || pemValue.trim().isEmpty()) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. %2 Wert ist leer",
                                                                String.valueOf(recordNo), pemField),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    retValue = false;
                }
            }
            if (!retValue) {
                reduceRecordCount();
            }

            return retValue;
        }
    }

    public static class DataObjectForIdWithoutAdatAndSeqNo extends ImportDataObjectsForPartialId<iPartsDataFactoryData> {

        private boolean isVBWImport;

        DataObjectForIdWithoutAdatAndSeqNo(boolean isVBWImport) {
            this.isVBWImport = isVBWImport;
        }

        @Override
        public Comparator<iPartsDataFactoryData> getComparator() {
            return iPartsDataFactoryData.comparator;
        }

        @Override
        public String getStatusField() {
            return FIELD_DFD_STATUS;
        }

        @Override
        public String getSourceField() {
            return FIELD_DFD_SOURCE;
        }

        @Override
        public String getAdatField() {
            // Nur bei WBRT das ADAT berücksichtigen
            if (isVBWImport) {
                return null;
            } else {
                return FIELD_DFD_ADAT;
            }
        }
    }
}