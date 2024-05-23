/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Importer für die FTTE Daten.
 */
public class MigrationDialogProductColorTableFactoryImporter extends MigrationDialogProductColorFactoryBaseImporter {

    final static String FTTE_NR = "FTTE_NR";
    final static String FTTE_TEIL = "FTTE_TEIL";
    final static String FTTE_SDA = "FTTE_SDA";
    final static String FTTE_WERK = "FTTE_WERK";
    final static String FTTE_SDB = "FTTE_SDB";
    final static String FTTE_ADAT1 = "FTTE_ADAT1";
    final static String FTTE_PTAB = "FTTE_PTAB";
    final static String FTTE_PTBI = "FTTE_PTBI";
    final static String FTTE_PEMA = "FTTE_PEMA";
    final static String FTTE_PEMB = "FTTE_PEMB";
    final static String FTTE_FIKZ = "FTTE_FIKZ";
    final static String FTTE_STCA = "FTTE_STCA";
    final static String FTTE_STCB = "FTTE_STCB";
    final static String FTTE_EDAT = "FTTE_EDAT";  // wird nicht migriert
    final static String FTTE_ADAT = "FTTE_ADAT";  // wird nicht migriert

    protected static final String DATASET_PREFIX = "FTTE";
    private static final String DB_KEYS_SEPERATOR = "|";

    private final static String[] headerNames = new String[]{
            FTTE_NR,
            FTTE_TEIL,
            FTTE_SDA,
            FTTE_WERK,
            FTTE_SDB,
            FTTE_ADAT1,
            FTTE_PTAB,
            FTTE_PTBI,
            FTTE_PEMA,
            FTTE_PEMB,
            FTTE_FIKZ,
            FTTE_STCA,
            FTTE_STCB,
            FTTE_EDAT,
            FTTE_ADAT
    };
    private final static String[] fieldsInDBToCompare = new String[]{ FIELD_DCCF_TABLE_ID,
                                                                      FIELD_DCCF_SDATA,
                                                                      FIELD_DCCF_SDATB,
                                                                      FIELD_DCCF_FACTORY,
                                                                      FIELD_DCCF_PEMA,
                                                                      FIELD_DCCF_PEMB,
                                                                      FIELD_DCCF_PEMTA,
                                                                      FIELD_DCCF_PEMTB,
                                                                      FIELD_DCCF_STCA,
                                                                      FIELD_DCCF_STCB
    };

    private HashMap<String, String> mappingFTSData; // Mapping für FTS Datensätze
    private Map<String, ArrayList<Map<String, String>>> x10EDataSetMap; // Map für alle X10E Datensätze mit gleichen iParts DB Schlüssel (ohne POS)
    private Map<String, ArrayList<Map<String, String>>> colorTableFactoryMap; // Map für alle VX10 und WX10 Datensätze mit gleichen iParts DB Schlüssel (ohne POS)
    private Map<String, iPartsDataColorTableData> ftsDataSetMap;


    public MigrationDialogProductColorTableFactoryImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG Color Table Factory (FTTE)", withHeader, headerNames, DATASET_PREFIX,
              new FilesImporterFileListType("table", "!!DIALOG Color Table Factory (FTTE)", true, false, false, new String[]{ MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        x10EDataSetMap = new HashMap<>();
        colorTableFactoryMap = new HashMap<>();
        ftsDataSetMap = new HashMap<>();
        primaryKeysImport = new String[]{ FTTE_NR, FTTE_TEIL, FTTE_SDA };

        // FTS Mapping
        mappingFTSData = new HashMap<>();
        mappingFTSData.put(FIELD_DCTD_TABLE_ID, FTTE_NR);
        mappingFTSData.put(FIELD_DCTD_DESC, FTTE_NR);
//        mappingFTSData.put(FIELD_DCTD_BEM, FTS_BEM); existiert nur im DIALOG Datensatz
        mappingFTSData.put(FIELD_DCTD_FIKZ, FTTE_FIKZ);

        // X10E Mapping
        mappingXXE = new HashMap<>();
        mappingXXE.put(FIELD_DCTP_TABLE_ID, FTTE_NR);
//        mappingX10E.put(FIELD_DCTP_POS, X10E_POS); existiert nur im DIALOG Datensatz - kritisch, da Schlüsselattribut
        mappingXXE.put(FIELD_DCTP_SDATA, FTTE_SDA);
        mappingXXE.put(FIELD_DCTP_SDATB, FTTE_SDB);
        mappingXXE.put(FIELD_DCTP_PART, FTTE_TEIL);

        // VX10 und WX10 Mapping
        mappingColortableFactory = new HashMap<>();
        mappingColortableFactory.put(FIELD_DCCF_TABLE_ID, FTTE_NR);
        mappingColortableFactory.put(FIELD_DCCF_SDATA, FTTE_SDA);
        mappingColortableFactory.put(FIELD_DCCF_SDATB, FTTE_SDB);
//        mappingVX10.put(FIELD_DCCF_POS, prefixForImporterInstance + POS); existiert nur im DIALOG Datensatz - kritisch, da Schlüsselattribut
        mappingColortableFactory.put(FIELD_DCCF_FACTORY, FTTE_WERK);
        mappingColortableFactory.put(FIELD_DCCF_PEMA, FTTE_PEMA);
        mappingColortableFactory.put(FIELD_DCCF_PEMB, FTTE_PEMB);
        mappingColortableFactory.put(FIELD_DCCF_PEMTA, FTTE_PTAB);
        mappingColortableFactory.put(FIELD_DCCF_PEMTB, FTTE_PTBI);
        mappingColortableFactory.put(FIELD_DCCF_STCA, FTTE_STCA);
        mappingColortableFactory.put(FIELD_DCCF_STCB, FTTE_STCB);

    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        progressMessageType = ProgressMessageType.READING;
    }


    @Override
    protected boolean importXXEDataset(Map<String, String> importRec, int recordNo, String colorTableId, String sdata) {
        // Erst Überprüfen, ob ein FTS Datensatz importiert werden kann
        if (!importFTSDataset(importRec, recordNo, colorTableId)) {
            return false;
        }
        // HashKey generieren und alle X10E Datensätze unter dem selben Key in der Map speichern
        String key = makeDBKey(colorTableId, sdata);
        ArrayList<Map<String, String>> importRecordList = x10EDataSetMap.get(key);
        if (importRecordList == null) {
            importRecordList = new ArrayList<>();
            x10EDataSetMap.put(key, importRecordList);
        }
        importRecordList.add(importRec);
        return true;
    }

    @Override
    protected boolean importColortableFactoryDataset(Map<String, String> importRec, int recordNo, String colorTableId, String factory, String adat, String sdata) {
        FTTEImportHelper helper = new FTTEImportHelper(getProject(), mappingColortableFactory, importTableInDB);
        String pemA = helper.handleValueOfSpecialField(FTTE_PEMA, importRec);
        String pemB = helper.handleValueOfSpecialField(FTTE_PEMB, importRec);
        handleSingleASPemFactoryCacheEntry(helper, colorTableId, factory, pemA, pemB);

        // HashKey generieren und alle ColortableFactory Datensätze unter dem selben Key in der Map speichern
        String key = makeDBKey(colorTableId, factory, adat, currentDatasetId.getDbValue(), sdata);
        ArrayList<Map<String, String>> storedImportRecList = colorTableFactoryMap.get(key);
        if (storedImportRecList == null) {
            storedImportRecList = new ArrayList<>();
            colorTableFactoryMap.put(key, storedImportRecList);
        }
        storedImportRecList.add(importRec);
        return true;
    }

    /**
     * Generiert einen HASH Key mit den übergebenen Strings
     *
     * @param keyValues
     * @return
     */
    private String makeDBKey(String... keyValues) {
        return StrUtils.makeDelimitedString(DB_KEYS_SEPERATOR, keyValues);
    }

    @Override
    protected void postImportAction() {
        if (processX10EDatasets()) {
            processColorTableFactoryDatasets();
        }
    }

    @Override
    protected void deleteUnprovidedData() {
        iPartsDataColorTableToPartList colorTableToPartList = new iPartsDataColorTableToPartList();
        EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String colortableId = attributes.getFieldValue(FIELD_DCTP_TABLE_ID);
                String pos = attributes.getFieldValue(FIELD_DCTP_POS);
                String sdata = attributes.getFieldValue(FIELD_DCTP_SDATA);
                iPartsColorTableToPartId colorTableToPartId = new iPartsColorTableToPartId(colortableId, pos, sdata);
                // Füge den gefundenen Datensatz zum Diff hinzu
                putSecond(colorTableToPartId);
                // Wenn der gerade hinzugefügte Datensatz in der zweiten Liste existiert,dann wurde er initial nicht importiert
                // -> muss aus der DB gelöscht werden
                return contentOrPartData.getOnlyInSecondItems().containsKey(colorTableToPartId.toString());
            }
        };
        // Like-Abfrage für alle Einträge, die mit QFT[Baureihe] beginnen
        colorTableToPartList.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(), null,
                                                       new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_TABLE_ID),
                                                                     TableAndFieldName.make(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SOURCE) },
                                                       new String[]{ getColortableSeriesWhereValue(), iPartsImportDataOrigin.MAD.getOrigin() },
                                                       false, null, false, true, callback);
        colorTableToPartList.deleteAll(DBActionOrigin.FROM_EDIT);
        if (importToDB) {
            colorTableToPartList.saveToDB(getProject());
        }
    }

    /**
     * Verarbeitet alle ColortableFactory Datensätze nachdem die komplette Datei eingelesen wurde
     */
    private boolean processColorTableFactoryDatasets() {
        getMessageLog().fireMessage(translateForLog("!!Verarbeite VX und WX Datensätze."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        FTTEImportHelper helper = new FTTEImportHelper(getProject(), mappingColortableFactory, importTableInDB);
        int saveCounter = 0;
        getMessageLog().fireProgress(0, colorTableFactoryMap.size(), "", true, false);

        for (Map.Entry<String, ArrayList<Map<String, String>>> ctfEntry : colorTableFactoryMap.entrySet()) {
            if (Thread.currentThread().isInterrupted()) {
                cancelImport("!!Import-Thread wurde frühzeitig beendet");
                return false;
            }
            // Zerlege den Key in die Schlüsselattribute
            String[] dbKeys = StrUtils.toStringArray(ctfEntry.getKey(), DB_KEYS_SEPERATOR, true);
            ArrayList<Map<String, String>> importRecList = ctfEntry.getValue();
            for (Map<String, String> importRec : importRecList) {
                String pos = POS_PREFIX + helper.handleValueOfSpecialField(FTTE_TEIL, importRec);
                // Primary keys setzen:
                // colorTableId = dbKeys[0];
                // factory = dbKeys[1];
                // adat = dbKeys[2];
                // dataId = dbKeys[3];
                // sdata = dbKeys[4];
                iPartsColorTableFactoryId id = makeColorTableFactoryId(dbKeys[0], pos, dbKeys[1], dbKeys[2], dbKeys[3], dbKeys[4]);
                iPartsDataColorTableFactory colorTableFactoryDataObject = new iPartsDataColorTableFactory(getProject(), id);
                if (!colorTableFactoryDataObject.existsInDB()) {
                    colorTableFactoryDataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                colorTableFactoryDataObject.setFieldValue(FIELD_DCCF_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
                colorTableFactoryDataObject.setFieldValue(FIELD_DCCF_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
                helper.fillOverrideCompleteDataForMADReverse(colorTableFactoryDataObject, importRec, iPartsMADLanguageDefs.MAD_DE);
                saveToDB(colorTableFactoryDataObject);
            }
            saveCounter++;
            getMessageLog().fireProgress(saveCounter, colorTableFactoryMap.size(), "", true, true);
        }
        getMessageLog().hideProgress();
        getMessageLog().fireMessage(translateForLog("!!Verarbeitung der VX und WX Datensätze beendet."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        colorTableFactoryMap = null;
        return true;
    }


    /**
     * Verarbeitet alle X10E Datensätze nachdem die komplette Datei eingelesen wurde
     */
    private boolean processX10EDatasets() {
        if (!processFTSDatasets()) {
            return false;
        }
        getMessageLog().fireMessage(translateForLog("!!Verarbeite X10E Datensätze."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        FTTEImportHelper helper = new FTTEImportHelper(getProject(), mappingXXE, importTableInDB);
        int saveCounter = 0;
        getMessageLog().fireProgress(0, x10EDataSetMap.size(), "", true, false);

        for (Map.Entry<String, ArrayList<Map<String, String>>> x10EEntry : x10EDataSetMap.entrySet()) {
            if (Thread.currentThread().isInterrupted()) {
                cancelImport("!!Import-Thread wurde frühzeitig beendet");
                return false;
            }
            // Zerlege den Key in die Schlüsselattribute
            String[] dbKeys = StrUtils.toStringArray(x10EEntry.getKey(), DB_KEYS_SEPERATOR, true);

            ArrayList<Map<String, String>> importRecList = x10EEntry.getValue();
            for (Map<String, String> importRec : importRecList) {
                String pos = POS_PREFIX + helper.handleValueOfSpecialField(FTTE_TEIL, importRec);
                // Primary keys setzen
                // colorTableId = dbKeys[0];
                // sdata = dbKeys[1];
                iPartsColorTableToPartId id = new iPartsColorTableToPartId(dbKeys[0], pos, dbKeys[1]);
                iPartsDataColorTableToPart x10EDataObject = new iPartsDataColorTableToPart(getProject(), id);
                if (!x10EDataObject.existsInDB()) {
                    x10EDataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    x10EDataObject.setFieldValue(FIELD_DCTP_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
                }
                x10EDataObject.setFieldValue(FIELD_DCTP_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
                helper.fillOverrideCompleteDataForMADReverse(x10EDataObject, importRec, iPartsMADLanguageDefs.MAD_DE);
                saveToDB(x10EDataObject);
                // Füge den neuen Datensatz zum Diff hinzu
                putFirst(id);
            }
            saveCounter++;
            getMessageLog().fireProgress(saveCounter, x10EDataSetMap.size(), "", true, true);
        }
        getMessageLog().hideProgress();
        getMessageLog().fireMessage(translateForLog("!!Verarbeitung der X10E Datensätze beendet."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        x10EDataSetMap = null;
        return true;
    }

    /**
     * Verarbeitet alle gesammelten FTS Datensätze
     */
    private boolean processFTSDatasets() {
        getMessageLog().fireMessage(translateForLog("!!Verarbeite FTS Datensätze."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        if ((ftsDataSetMap != null) && !ftsDataSetMap.isEmpty()) {
            int saveCounter = 0;
            getMessageLog().fireProgress(0, ftsDataSetMap.size(), "", true, false);
            for (iPartsDataColorTableData ftsDataObject : ftsDataSetMap.values()) {
                if (Thread.currentThread().isInterrupted()) {
                    cancelImport("!!Import-Thread wurde frühzeitig beendet");
                    return false;
                }
                saveToDB(ftsDataObject);
                saveCounter++;
                getMessageLog().fireProgress(saveCounter, ftsDataSetMap.size(), "", true, true);
            }
        }
        getMessageLog().hideProgress();
        getMessageLog().fireMessage(translateForLog("!!Verarbeitung der FTS Datensätze beendet."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        ftsDataSetMap = null;
        return true;
    }


//    @Override
//    protected boolean importXXEDataset(Map<String, String> importRec, int recordNo, String colorTableId, String sdata) {
//        // Erst Überprüfen, ob ein FTS Datensatz importiert werden kann
//        if (!importFTSDataset(importRec, recordNo, colorTableId)) {
//            return false;
//        }
//        FTTEImportHelper helper = new FTTEImportHelper(getProject(), mappingXXE, importTableInDB);
//        iPartsColorTableToPartId id = new iPartsColorTableToPartId(colorTableId, "", sdata);
//        iPartsDataColorTableToPart dataObject = new iPartsDataColorTableToPart(getProject(), id);
//        if (!dataObject.existsInDB()) {
//            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
//        } else {
//            // POS Konflikt - Datensatz existiert ohne genaue POS in der DB. Jetzt muss geprüft werden, ob es sich um den gelichen Datensatz handelt oder nicht.
//            // Falls nicht, neue POS generieren. Überprüfung auf Gleichheit erfolgt via Teilenummer, da im MAD FTTE_NR, FTTE_SDA und FTTE_TEIL Schlüsselattributte sind.
//            iPartsDataColorTableToPartList list = iPartsDataColorTableToPartList.loadColorTableToPartListForNumberAndDate(getProject(), colorTableId, sdata);
//            boolean foundInDB = false;
//            // Check, ob einer der gefunden Datensätz (gleiche Nr und gleiches SDATA) auch das gleiche Teil besitzen.
//            // Falls ja, gleicher Datensatz (da TEIL ein Schlüssel in MAD war)
//            for (iPartsDataColorTableToPart storedObject : list) {
//                if (storedObject.getFieldValue(FIELD_DCTP_PART).equals(helper.handleValueOfSpecialField(FTTE_TEIL, importRec))) {
//                    dataObject = storedObject;
//                    foundInDB = true;
//                    break;
//                }
//            }
//            if (!foundInDB) {
//                // Datensatz hat nicht den gleichen MAD Schlüsselwert -> POS generieren
//                String newPOS = helper.generatePOS(list);
//                getMessageLog().fireMessage(translateForLog("!!Record %1: möglicher POS Konflikt mit \"%2\" und \"%3\". Neu generierte POS: \"%4\"",
//                                                            String.valueOf(recordNo), colorTableId, sdata, newPOS),
//                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
//                id = new iPartsColorTableToPartId(colorTableId, newPOS, sdata);
//                dataObject = new iPartsDataColorTableToPart(getProject(), id);
//                if (dataObject.existsInDB()) {
//                    getMessageLog().fireMessage(translateForLog("!!Record %1 mit \"%2\" und \"%3\" übersprungen. Generieren der neue POS \"%4\" fehlgeschlagen",
//                                                                String.valueOf(recordNo), colorTableId, sdata, newPOS),
//                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
//                    return false;
//                }
//                dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
//            }
//
//        }
//        dataObject.setFieldValue(FIELD_DCTP_SOURCE, iPartsFactoryDataTypes.COLORTABLE_PART.getDbMADValue(), DBActionOrigin.FROM_EDIT);
//        helper.fillOverrideCompleteDataForDIALOGReverse(dataObject, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
//        if (importToDB) {
//            saveToDB(dataObject);
//        }
//        return true;
//    }

    /**
     * Importiert den FTS Datensatz
     *
     * @param importRec
     * @param recordNo
     * @param colorTableId
     * @return
     */
    private boolean importFTSDataset(Map<String, String> importRec, int recordNo, String colorTableId) {
        FTTEImportHelper helper = new FTTEImportHelper(getProject(), mappingFTSData, TABLE_DA_COLORTABLE_DATA);
        String ftsHash = helper.createHashNeu(importRec, new String[]{ FTTE_NR, FTTE_FIKZ });
        if (ftsDataSetMap.get(ftsHash) != null) {
            return true;
        }
        // Baureihe aus der Farbtabellen ID extrahieren (siehe WikiPage)
        String series = ColorTableHelper.extractSeriesNumberFromTableId(colorTableId);
        if (series.isEmpty() || series.equals(MODEL_NUMBER_PREFIX_CAR)) {
            cancelImport(translateForLog("!!Record %1 fehlerhaft (Baureihe konnte nicht aus Farbtabellen ID extrahiert werden: %2)",
                                         String.valueOf(recordNo), colorTableId));
            return false;
        }
        iPartsColorTableDataId id = new iPartsColorTableDataId(colorTableId);
        iPartsDataColorTableData dataColorTableData = new iPartsDataColorTableData(getProject(), id);
        if (!dataColorTableData.existsInDB()) {
            dataColorTableData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            // gültige Baureihe setzen
            dataColorTableData.setFieldValue(FIELD_DCTD_VALID_SERIES, series, DBActionOrigin.FROM_EDIT);
        }
        helper.fillOverrideCompleteDataForMADReverse(dataColorTableData, importRec, iPartsMADLanguageDefs.MAD_DE);
        dataColorTableData.setFieldValue(FIELD_DCTD_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
        ftsDataSetMap.put(ftsHash, dataColorTableData);
        return true;
    }


    protected class FTTEImportHelper extends CTImportHelper {

        public FTTEImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(FTTE_PEMA) || sourceField.equals(FTTE_PEMB) || sourceField.equals(FTTE_STCA) || sourceField.equals(FTTE_STCB)) {
                value = value.trim();
            } else {
                value = super.handleValueOfSpecialField(sourceField, value);
            }
            return value;
        }
    }
}
