/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMConstKitContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSConstProps;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsEDSConstPropsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Deprecated
public class MasterDataEdsConstructionKitImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    private enum IMPORT_TYPE {IMPORT_CONSTRUCTION, IMPORT_CONSTRUCTION_NAMES, IMPORT_UNKNOWN}


    // Felder der EDS SAA-Baukasteninhalt
    public static final String IMPORT_TABLENAME_BK = "BOMDB_T43RBK";

    private static final String FIELD_EDS_YET_NOT_IMPLEMENTED = "<unknown>";

    private static final String BK_SNR = "BK_SNR";    //*  war D_USNR
    private static final String BK_POS = "BK_POS";    //*  war TPOS
    private static final String BK_AS_AB = "BK_AS_AB";    //*  war AS_AB
    private static final String BK_KEM_AB = "BK_KEM_AB";    //*  war D_KEM_AB
    private static final String BK_FRG_KZ_AB = "BK_FRG_KZ_AB";
    private static final String BK_FRG_DAT_AB = "BK_FRG_DAT_AB";    //*
    private static final String BK_AS_BIS = "BK_AS_BIS";    //*  war AS_BIS
    private static final String BK_KEM_BIS = "BK_KEM_BIS";    //*2  war D_KEM_BIS
    private static final String BK_FRG_KZ_BIS = "BK_FRG_KZ_BIS";    //**2
    private static final String BK_FRG_DAT_BIS = "BK_FRG_DAT_BIS";
    private static final String BK_BEMZ = "BK_BEMZ";    //*
    private static final String BK_WWKB = "BK_WWKB";    //*
    private static final String BK_SNRU = "BK_SNRU";    //*  war D_TEIL
    private static final String BK_MENGE = "BK_MENGE";    //*  war MENGE
    private static final String BK_MGKZ = "BK_MGKZ";    //*
    private static final String BK_LKG = "BK_LKG";    //*
    private static final String BK_PLMI = "BK_PLMI";
    private static final String BK_VARIANTEN_KZ = "BK_VARIANTEN_KZ";
    private static final String BK_VAR_KONSTR_MASS = "BK_VAR_KONSTR_MASS";
    private static final String BK_RF = "BK_RF";    //*
    private static final String BK_WK = "BK_WK";    //*  war WERKE
    private static final String BK_BZA = "BK_BZA";    //*
    private static final String BK_LTG_BK = "BK_LTG_BK";    //*
    private static final String BK_URL_AS_AB = "BK_URL_AS_AB";
    private static final String BK_DAT_URL = "BK_DAT_URL";
    private static final String BK_TS_AEND = "BK_TS_AEND";


    public static final String IMPORT_TABLENAME_BKV = "BOMDB_T43RBKV";

    private static final String BKV_SNR = "BKV_SNR";    //*                   PK!!
    private static final String BKV_POS = "BKV_POS";    //*                   PK!!
    private static final String BKV_SPS = "BKV_SPS";    //* indirekt
    private static final String BKV_BTXKZ = "BKV_BTXKZ";    //*
    private static final String BKV_AS_AB = "BKV_AS_AB";    //*
    private static final String BKV_KEM_AB = "BKV_KEM_AB";    //*
    private static final String BKV_FRG_KZ_AB = "BKV_FRG_KZ_AB";
    private static final String BKV_FRG_DAT_AB = "BKV_FRG_DAT_AB";    //*
    private static final String BKV_AS_BIS = "BKV_AS_BIS";    //*1
    private static final String BKV_KEM_BIS = "BKV_KEM_BIS";    //*1
    private static final String BKV_FRG_KZ_BIS = "BKV_FRG_KZ_BIS";    //**1
    private static final String BKV_FRG_DAT_BIS = "BKV_FRG_DAT_BIS";    //*1
    private static final String BKV_DAT_URL = "BKV_DAT_URL";
    private static final String BKV_TS_AEND = "BKV_TS_AEND";
    private static final String BKV_TEXT = "BKV_TEXT";    //*

    private IMPORT_TYPE importType;
    private HashMap<String, String> edsMapping;
    private String[] primaryKeysEDSImportData;
    private HashMap<String, String> edsDescriptionMapping;
    private String[] primaryKeysEDSDescriptionImportData;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean createMatEntryForSAA = true; //sollen in der MAT Einträge erzeugt werden?

    /**
     * Constructor für Excel oder XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MasterDataEdsConstructionKitImporter(EtkProject project) {
        super(project, "!!EDS-Baukasteninhalt (BK/BKV)",
              new FilesImporterFileListType(TABLE_DA_EDS_CONST_KIT, EDS_SAA_CONSTRUCTION_NAME, false, false, true, MimeTypes.getValidImportExcelAndCsvMimeTypes()),
              new FilesImporterFileListType(TABLE_DA_EDS_CONST_PROPS, EDS_SAA_CONSTRUCTION_TEXT, false, false, true, MimeTypes.getValidImportExcelAndCsvMimeTypes()));
        this.importType = IMPORT_TYPE.IMPORT_UNKNOWN;
        initMapping();
    }

    private void initMapping() {
        // Das Mapping für die Edsfelder in die DBTABLENAME_FOR_BK
        edsMapping = new HashMap<String, String>();
        edsMapping.put(BK_SNR, FIELD_DCK_SNR);
        //edsMapping.put(STUFE, FIELD_EDS_LEVEL);
        edsMapping.put(BK_POS, FIELD_DCK_PARTPOS);
        edsMapping.put(BK_SNRU, FIELD_DCK_SUB_SNR);
        edsMapping.put(BK_MENGE, FIELD_DCK_QUANTITY);
        edsMapping.put(BK_AS_AB, FIELD_DCK_REVFROM);
        edsMapping.put(BK_AS_BIS, FIELD_DCK_REVTO);
        edsMapping.put(BK_KEM_AB, FIELD_DCK_KEMFROM);
        edsMapping.put(BK_KEM_BIS, FIELD_DCK_KEMTO);
        edsMapping.put(BK_FRG_DAT_AB, FIELD_DCK_RELEASE_FROM);
        edsMapping.put(BK_FRG_DAT_BIS, FIELD_DCK_RELEASE_TO);
        edsMapping.put(BK_BEMZ, FIELD_DCK_NOTE_ID);
        edsMapping.put(BK_WWKB, FIELD_DCK_WWKB);
        edsMapping.put(BK_MGKZ, FIELD_DCK_QUANTITY_FLAG);
        edsMapping.put(BK_LKG, FIELD_DCK_STEERING);
        edsMapping.put(BK_RF, FIELD_DCK_RFG);
        edsMapping.put(BK_WK, FIELD_DCK_FACTORY_IDS);
        edsMapping.put(BK_BZA, FIELD_DCK_REPLENISHMENT_KIND);
        edsMapping.put(BK_LTG_BK, FIELD_DCK_TRANSMISSION_KIT);

        primaryKeysEDSImportData = new String[]{ BK_SNR, BK_POS, BK_SNRU, BK_AS_AB, BK_AS_BIS, BK_KEM_AB, BK_KEM_BIS };

        //  Das Mapping für die Edsfelder in die DBTABLENAME_FOR_BKV
        edsDescriptionMapping = new HashMap<String, String>();
        edsDescriptionMapping.put(BKV_SNR, FIELD_DCP_SNR);
        edsDescriptionMapping.put(BKV_POS, FIELD_DCP_PARTPOS);
        edsDescriptionMapping.put(BKV_BTXKZ, FIELD_DCP_BTX_FLAG);
        edsDescriptionMapping.put(BKV_AS_AB, FIELD_DCP_REVFROM);
        edsDescriptionMapping.put(BKV_KEM_AB, FIELD_DCP_KEMFROM);
        edsDescriptionMapping.put(BKV_FRG_DAT_AB, FIELD_DCP_RELEASE_FROM);
        edsDescriptionMapping.put(BKV_AS_BIS, FIELD_DCP_REVTO);
        edsDescriptionMapping.put(BKV_KEM_BIS, FIELD_DCP_KEMTO);
        edsDescriptionMapping.put(BKV_FRG_DAT_BIS, FIELD_DCP_RELEASE_TO);
        edsDescriptionMapping.put(BKV_TEXT, FIELD_DCP_TEXT);

        primaryKeysEDSDescriptionImportData = new String[]{ BKV_SNR, BKV_POS, BKV_KEM_AB };
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        if (importType == IMPORT_TYPE.IMPORT_CONSTRUCTION) {
            //bestehend aus den primaryKey und den beiden AbfrageFeldern für AS_BIS, KEM_BIS, FRG_DAT_BIS
            String[] mustExists = StrUtils.mergeArrays(primaryKeysEDSImportData, new String[]{ BK_FRG_KZ_BIS });
            String[] mustHaveData = new String[]{ BK_SNR };
            importer.setMustExists(mustExists);
            importer.setMustHaveData(mustHaveData);
        } else if (importType == IMPORT_TYPE.IMPORT_CONSTRUCTION_NAMES) {
            String[] mustExist = StrUtils.mergeArrays(primaryKeysEDSDescriptionImportData, new String[]{ BKV_SPS, BKV_FRG_KZ_BIS });
            String[] mustHaveData = primaryKeysEDSDescriptionImportData;
            importer.setMustExists(mustExist);
            importer.setMustHaveData(mustHaveData);
        }
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return importType != IMPORT_TYPE.IMPORT_UNKNOWN;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }

        //hier weitere Abprüfungen
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        switch (importType) {
            case IMPORT_CONSTRUCTION:
                importConstructionRecord(importRec, recordNo);
                break;
            case IMPORT_CONSTRUCTION_NAMES:
                importConstructionNameRecord(importRec, recordNo);
                break;
            default:
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                            translateForLog("!!Importtyp"), importType.name()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                break;
        }
    }

    private void importConstructionRecord(Map<String, String> importRec, int recordNo) {
        // jetzt Record bilden und abspeichern
        ConstructionKitImportHelper importHelper = initHelperForBK();
        //nur Datensätze abspeichern die freigegeben sind
        if (importHelper.isReleasedDataSet(importRec)) {
            //Daten nur komplett importieren, wenn noch kein Datensatz in der DB existiert
            //sonst werden nur die SpecialFields upgedated
            List<String> specialFields = new DwList<String>();
            // SpecialFields sind AS_BIS; KEM_BIS; FRG_DAT_BIS
            specialFields.add(BK_AS_BIS);
            specialFields.add(BK_KEM_BIS);
            specialFields.add(BK_FRG_DAT_BIS);

            iPartsDataBOMConstKitContent dataConstKit = getExistingBKRecord(importHelper, importRec);
            boolean recordIsNew = (dataConstKit == null);
            if (recordIsNew) {
                //Nachfolger bzw neuen Record setzen speichern
                iPartsBOMConstKitContentId edsConstKitId = new iPartsBOMConstKitContentId(importRec.get(BK_SNR),
                                                                                          importRec.get(BK_POS),
                                                                                          importRec.get(BK_KEM_AB));
                dataConstKit = new iPartsDataBOMConstKitContent(getProject(), edsConstKitId);
                dataConstKit.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                //GUID setzen
                dataConstKit.setFieldValue(FIELD_DCK_GUID, StrUtils.makeGUID(), DBActionOrigin.FROM_EDIT);
                //kompletten Datensatz mit Werten füllen unter Berücksichtigung der SpecialFields
                importHelper.fillCompleteData(dataConstKit, importRec, specialFields);
            } else {
                //nur die SpecialFields updaten
                importHelper.updateData(dataConstKit, importRec, specialFields);
            }

            if (importToDB) {
                saveToDB(dataConstKit);
            }
            // Baukasten
            createPartEntry(importRec.get(BK_SNR));
            // Untere Sachnummer
            createPartEntry(importRec.get(BK_SNRU));
        } else {
            //Skip Record, da nicht released
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                        "FRG_KZ_AB", importHelper.getFRG_KZ_AB(importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    private ConstructionKitImportHelper initHelperForBK() {
        ConstructionKitImportHelper helper = new ConstructionKitImportHelper();
        helper.project = getProject();
        helper.AS_BIS = BK_AS_BIS;
        helper.AS_AB = BK_AS_AB;
        helper.FRG_DAT_AB = BK_FRG_DAT_AB;
        helper.FRG_DAT_BIS = BK_FRG_DAT_BIS;
        helper.FRG_KZ_BIS = BK_FRG_KZ_BIS;
        helper.KEM_BIS = BK_KEM_BIS;
        helper.FRG_KZ_AB = BK_FRG_KZ_AB;
        helper.SPS = "";
        helper.dbTableName = TABLE_DA_EDS_CONST_KIT;
        helper.mapping = edsMapping;
        return helper;
    }

    private iPartsDataBOMConstKitContent getExistingBKRecord(ConstructionKitImportHelper importHelper, Map<String, String> importRec) {
        // Um einen Vorgänger zu finden, wird nach BK_SNR, BK_POS und BK_SNRU sowie
        // BK_AS_BIS="999" und BK_FRG_DAT_BIS="99999999999999" (bzw leer) gesucht
        String[] primaryKeys = new String[]{ BK_SNR, BK_POS, BK_SNRU };
        DBDataObjectAttributesList datas = importHelper.getExistingRecord(importRec, primaryKeys);
        if (datas != null) {
            if (datas.size() > 0) { //ggf auf == 1 abfragen?
                DBDataObjectAttributes dbData = datas.get(0);
                iPartsBOMConstKitContentId edsConstKitId = new iPartsBOMConstKitContentId(dbData.getField(FIELD_DCK_SNR).getAsString(),
                                                                                          dbData.getField(FIELD_DCK_PARTPOS).getAsString(),
                                                                                          dbData.getField(FIELD_DCK_KEMFROM).getAsString());
                iPartsDataBOMConstKitContent dataConstKit = new iPartsDataBOMConstKitContent(getProject(), edsConstKitId);
                //bevor die Attribute kopiert werden, lieber nochmal DB-Abfrage
                dataConstKit.loadFromDB(edsConstKitId);
                return dataConstKit;
            }
        }
        return null;
    }

    private void createPartEntry(String SAANo) {
        if (createMatEntryForSAA) {
            iPartsPartId partId = new iPartsPartId(SAANo, "");
            EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getProject(), partId.getMatNr(), "");
            if (!dataPart.loadFromDB(partId)) {
                dataPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                dataPart.setFieldValue(FIELD_M_BESTNR, SAANo, DBActionOrigin.FROM_EDIT);
                dataPart.setFieldValueAsBoolean(FIELD_M_BESTFLAG, true, DBActionOrigin.FROM_EDIT);
                dataPart.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.EDS.getOrigin(), DBActionOrigin.FROM_EDIT);
                if (importToDB) {
                    saveToDB(dataPart);
                }
            }
        }
    }


    /**
     * importiere BKV-Element
     *
     * @param importRec
     * @param recordNo
     */
    private void importConstructionNameRecord(Map<String, String> importRec, int recordNo) {
        // jetzt Record bilden und abspeichern
        ConstructionKitImportHelper importHelper = initHelperForBKV();
        //nur Datensätze abspeichern die freigegeben sind
        if (importHelper.isReleasedDataSet(importRec)) {
            //Daten nur komplett importieren, wenn noch kein Datensatz in der DB existiert
            //sonst werden nur die SpecialFields upgedated
            List<String> specialFields = new DwList<String>();
            // SpecialFields sind AS_BIS; KEM_BIS; FRG_DAT_BIS
            specialFields.add(BKV_AS_BIS);
            specialFields.add(BKV_KEM_BIS);
            specialFields.add(BKV_FRG_DAT_BIS);

            iPartsDataEDSConstProps dataEdsConstProps = getExistingBKVRecord(importHelper, importRec);
            boolean recordIsNew = (dataEdsConstProps == null);
            if (recordIsNew) {
                //Nachfolger bzw neuen Record setzen speichern
                iPartsEDSConstPropsId edsSAAConstPropsId = new iPartsEDSConstPropsId(importRec.get(primaryKeysEDSDescriptionImportData[0]),
                                                                                     importRec.get(primaryKeysEDSDescriptionImportData[1]),
                                                                                     importRec.get(primaryKeysEDSDescriptionImportData[2]));
                dataEdsConstProps = new iPartsDataEDSConstProps(getProject(), edsSAAConstPropsId);
                dataEdsConstProps.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                //kompletten Datensatz mit Werten füllen unter Berücksichtigung der SpecialFields
                importHelper.fillCompleteData(dataEdsConstProps, importRec, specialFields);
            } else {
                //nur die SpecialFields updaten
                importHelper.updateData(dataEdsConstProps, importRec, specialFields);
            }

            if (importToDB) {
                saveToDB(dataEdsConstProps);
            }
        } else {
            //Skip Record, da nicht released
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                        "FRG_KZ_AB", importHelper.getFRG_KZ_AB(importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    private ConstructionKitImportHelper initHelperForBKV() {
        ConstructionKitImportHelper helper = new ConstructionKitImportHelper();
        helper.project = getProject();
        helper.AS_BIS = BKV_AS_BIS;
        helper.AS_AB = BKV_AS_AB;
        helper.FRG_DAT_AB = BKV_FRG_DAT_AB;
        helper.FRG_DAT_BIS = BKV_FRG_DAT_BIS;
        helper.FRG_KZ_BIS = BKV_FRG_KZ_BIS;
        helper.KEM_BIS = BKV_KEM_BIS;
        helper.FRG_KZ_AB = BKV_FRG_KZ_AB;
        helper.SPS = BKV_SPS;
        helper.dbTableName = TABLE_DA_EDS_CONST_PROPS;
        helper.mapping = edsDescriptionMapping;
        return helper;
    }

    private iPartsDataEDSConstProps getExistingBKVRecord(ConstructionKitImportHelper importHelper, Map<String, String> importRec) {
        DBDataObjectAttributesList datas = importHelper.getExistingRecord(importRec, primaryKeysEDSDescriptionImportData);
        if (datas != null) {
            if (datas.size() > 0) { //ggf auf == 1 abfragen?
                DBDataObjectAttributes dbData = datas.get(0);
                iPartsEDSConstPropsId edsConstPropsId = new iPartsEDSConstPropsId(dbData.getField(FIELD_DCP_SNR).getAsString(),
                                                                                  dbData.getField(FIELD_DCP_PARTPOS).getAsString(),
                                                                                  dbData.getField(FIELD_DCP_KEMFROM).getAsString());
                iPartsDataEDSConstProps dataEdsConstProps = new iPartsDataEDSConstProps(getProject(), edsConstPropsId);
                //bevor die Attribute kopiert werden, lieber nochmal DB-Abfrage
                dataEdsConstProps.loadFromDB(edsConstPropsId);
                return dataEdsConstProps;
            }
        }
        return null;
    }


    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_EDS_CONST_KIT)) {
            getProject().getDB().delete(TABLE_DA_EDS_CONST_KIT);
        } else if (importFileType.getFileListType().equals(TABLE_DA_EDS_CONST_PROPS)) {
            getProject().getDB().delete(TABLE_DA_EDS_CONST_PROPS);
        } else {
            return false;
        }
        return true;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_DA_EDS_CONST_KIT)) {
            importType = IMPORT_TYPE.IMPORT_CONSTRUCTION;
            return importMasterData(prepareImporterKeyValue(importFile, TABLE_DA_EDS_CONST_KIT, true, null));
        }
        if (importFileType.getFileListType().equals(TABLE_DA_EDS_CONST_PROPS)) {
            importType = IMPORT_TYPE.IMPORT_CONSTRUCTION_NAMES;
            return importMasterData(prepareImporterKeyValue(importFile, TABLE_DA_EDS_CONST_PROPS, true, null));
        }
        importType = IMPORT_TYPE.IMPORT_UNKNOWN;
        return false;
    }

}