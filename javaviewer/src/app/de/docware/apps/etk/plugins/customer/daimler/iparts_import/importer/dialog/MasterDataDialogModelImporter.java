/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MasterDataDialogModelImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    //Felder der DIALOG Baumuster Daten
    public static final String DIALOG_TABLENAME = "BMS";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    public static final String BMS_SNR = "BMS_SNR";
    public static final String BMS_SPS = "BMS_SPS";
    public static final String BMS_SDA = "BMS_SDA";
    public static final String BMS_SDB = "BMS_SDB";
    public static final String BMS_PS = "BMS_PS";
    public static final String BMS_KW = "BMS_KW";
    public static final String BMS_VBEZ = "BMS_VBEZ";
    public static final String BMS_BEN = "BMS_BEN";
    public static final String BMS_KON_BEZ = "BMS_KON_BEZ";
    public static final String BMS_UNG = "BMS_UNG";
    public static final String BMS_ANT_ART = "BMS_ANT_ART";
    public static final String BMS_MOT_KON = "BMS_MOT_KON";
    public static final String BMS_ANZ_ZYL = "BMS_ANZ_ZYL";
    public static final String BMS_MOT_ART = "BMS_MOT_ART";

    private HashMap<String, String> dialogMapping;
    private String tableName;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MasterDataDialogModelImporter(EtkProject project) {
        super(project, "!!DIALOG-Stammdaten Baumuster (BMS)",
              new FilesImporterFileListType(TABLE_DA_MODEL_DATA, EDS_MODEL_NAME, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        this.tableName = TABLE_DA_MODEL_DATA;

        // Das Mapping für die Baumuster-Felder aus DIALOG in die DA_MODEL-Tabelle
        dialogMapping = new HashMap<>();
        dialogMapping.put(FIELD_DMD_MODEL_NO, BMS_SNR);
        dialogMapping.put(FIELD_DMD_NAME, BMS_BEN);   //Zuordnung ??
        dialogMapping.put(FIELD_DMD_HORSEPOWER, BMS_PS);
        dialogMapping.put(FIELD_DMD_KILOWATTS, BMS_KW);
        dialogMapping.put(FIELD_DMD_SALES_TITLE, BMS_VBEZ);  //multilang
        dialogMapping.put(FIELD_DMD_DEVELOPMENT_TITLE, BMS_KON_BEZ);  //multilang
        dialogMapping.put(FIELD_DMD_MODEL_INVALID, BMS_UNG);
        dialogMapping.put(FIELD_DMD_DRIVE_SYSTEM, BMS_ANT_ART);
        dialogMapping.put(FIELD_DMD_ENGINE_CONCEPT, BMS_MOT_KON);
        dialogMapping.put(FIELD_DMD_CYLINDER_COUNT, BMS_ANZ_ZYL);
        dialogMapping.put(FIELD_DMD_ENGINE_KIND, BMS_MOT_ART);
        dialogMapping.put(FIELD_DMD_DATA, BMS_SDA);
        dialogMapping.put(FIELD_DMD_DATB, BMS_SDB);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExists = new String[]{ BMS_SNR };
        String[] mustHaveData = new String[]{ BMS_SNR };

        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME)
                   || importer.getTableNames().get(0).equals(tableName)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ModelImportHelper importHelper = new ModelImportHelper(getProject(), dialogMapping, tableName);
        // Nur den letzten freigegeben Stand übernehmen.
        if (isFinalStateDateTime(importRec.get(BMS_SDB))) {
            iPartsModelDataId modelDataId = new iPartsModelDataId(importHelper.handleValueOfSpecialField(BMS_SNR, importRec));
            // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
            if (!importHelper.checkImportRelevanceForSeriesFromModel(modelDataId.getModelNumber(), getInvalidSeriesSet(), this)) {
                return;
            }
            iPartsDataModelData dataModelData = new iPartsDataModelData(getProject(), modelDataId);
            if (!dataModelData.loadFromDB(modelDataId)) {
                dataModelData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }

            // Sprachdefinition des Records holen
            iPartsDIALOGLanguageDefs langDef = getLanguageDefinition(importRec);
            boolean doImport = false;
            if (langDef == iPartsDIALOGLanguageDefs.DIALOG_DE) {
                //kompletten Datensatz mit Werten füllen oder überschreiben
                importHelper.fillOverrideCompleteDataForDIALOGReverse(dataModelData, importRec, langDef);
                doImport = true;
            } else if (langDef != iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN) {
                //nur die Sprachtexte übernehmen bzw überschreiben
                importHelper.fillOverrideLanguageTextForDIALOGReverse(dataModelData, importRec, langDef);
                doImport = true;
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Sprachkennung: %2)",
                                                            String.valueOf(recordNo), importRec.get(BMS_SPS)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }

            if (importToDB) {
                if (doImport) {
                    saveToDB(dataModelData);
                }
            }

        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                        "BMS_SDB", importRec.get(BMS_SDB)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    private class ModelImportHelper extends DIALOGImportHelper {

        public ModelImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(BMS_SDA) || sourceField.equals(BMS_SDB)) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(BMS_SNR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }
    }

    /**
     * SprachDefinition aus den Importdaten holen
     *
     * @param importRec
     * @return
     */
    private iPartsDIALOGLanguageDefs getLanguageDefinition(Map<String, String> importRec) {
        return iPartsDIALOGLanguageDefs.getType(importRec.get(BMS_SPS));
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            // für XML-Datei Import
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }
}
