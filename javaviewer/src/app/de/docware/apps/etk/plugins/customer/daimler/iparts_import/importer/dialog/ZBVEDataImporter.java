/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsConstKitContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataConstKitContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ZBVEDataImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    //Felder der DIALOG Baukasteninhalt zu Teilenummer
    public static final String DIALOG_TABLENAME = "ZBVE";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    public static final String ZBVE_ZBV = "ZBVE_ZBV";
    public static final String ZBVE_POSE = "ZBVE_POSE";
    public static final String ZBVE_WW = "ZBVE_WW";
    public static final String ZBVE_SDA = "ZBVE_SDA";
    public static final String ZBVE_SDB = "ZBVE_SDB";
    public static final String ZBVE_TEIL = "ZBVE_TEIL";
    public static final String ZBVE_KEMA = "ZBVE_KEMA";
    public static final String ZBVE_KEMB = "ZBVE_KEMB";
    public static final String ZBVE_MG = "ZBVE_MG";
    public static final String ZBVE_URS = "ZBVE_URS";
    public static final String ZBVE_BZAE = "ZBVE_BZAE";
    public static final String ZBVE_SESI = "ZBVE_SESI";  // nicht übernehmen
    public static final String ZBVE_POSP = "ZBVE_POSP";  // nicht übernehmen
    public static final String ZBVE_VS_POS = "ZBVE_VS_POS";  // nicht übernehmen
    public static final String ZBVE_VS_WWKB = "ZBVE_VS_WWKB";  // nicht übernehmen

    private HashMap<String, String> dialogMapping;
    private String tableName;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean doBufferedSave = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public ZBVEDataImporter(EtkProject project) {
        super(project, "!!DIALOG-Stammdaten Baukasteninhalt zu Teilenummer (ZBVE)",
              new FilesImporterFileListType(TABLE_DA_CONST_KIT_CONTENT, DZBVE_CONSTRUCTION_KIT_CONTENTS, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        this.tableName = TABLE_DA_CONST_KIT_CONTENT;
        // Das Mapping für die Baumuster-Felder aus DIALOG in die DA_MODEL-Tabelle
        dialogMapping = new HashMap<>();
        dialogMapping.put(FIELD_DCKC_SDB, ZBVE_SDB);
        dialogMapping.put(FIELD_DCKC_SUB_PART_NO, ZBVE_TEIL);
        dialogMapping.put(FIELD_DCKC_KEM_FROM, ZBVE_KEMA);
        dialogMapping.put(FIELD_DCKC_KEM_TO, ZBVE_KEMB);
        dialogMapping.put(FIELD_DCKC_QUANTITY, ZBVE_MG);
        dialogMapping.put(FIELD_DCKC_SOURCE_KEY, ZBVE_URS);
        dialogMapping.put(FIELD_DCKC_PROPOSED_SOURCE_TYPE, ZBVE_BZAE);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExists = new String[]{ ZBVE_ZBV, ZBVE_POSE, ZBVE_WW, ZBVE_SDA };
        String[] mustHaveData = new String[]{ ZBVE_ZBV, ZBVE_POSE };

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
    protected void preImportTask() {
        super.preImportTask();

        setBufferedSave(doBufferedSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ConstructionKitHelper importHelper = new ConstructionKitHelper(getProject(), dialogMapping, tableName);
        iPartsConstKitContentId constKitContentId = importHelper.getConstKitId(importRec);
        if (!StrUtils.isValid(constKitContentId.getPartNo(), constKitContentId.getPosE())) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Werte für die ID)",
                                                        String.valueOf(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
            return;
        }
        iPartsDataConstKitContent dataConstKitContent = new iPartsDataConstKitContent(getProject(), constKitContentId);
        if (!dataConstKitContent.existsInDB()) {
            dataConstKitContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
        importHelper.fillOverrideCompleteDataForDIALOGReverse(dataConstKitContent, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);

        if (importToDB) {
            saveToDB(dataConstKitContent);
        }
    }

    @Override
    public void postImportTask() {
        if (!isCancelled()) {
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        // hier wird in die DB gespeichert ...
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class ConstructionKitHelper extends DIALOGImportHelper {

        public ConstructionKitHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public iPartsConstKitContentId getConstKitId(Map<String, String> importRec) {
            return new iPartsConstKitContentId(handleValueOfSpecialField(ZBVE_ZBV, importRec),
                                               handleValueOfSpecialField(ZBVE_POSE, importRec),
                                               handleValueOfSpecialField(ZBVE_WW, importRec),
                                               handleValueOfSpecialField(ZBVE_SDA, importRec));
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(ZBVE_SDA) || sourceField.equals(ZBVE_SDB)) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(ZBVE_MG)) {
                value = checkQuantityFormat(value);
            } else if (sourceField.equals(ZBVE_POSE)) {
                value = StrUtils.prefixStringWithCharsUpToLength(StrUtils.removeLeadingCharsFromString(value, '0'), '0',
                                                                 EtkConfigConst.NUMBER_FORMAT_LENGTH, false);
            } else if (sourceField.equals(ZBVE_TEIL)) {
                checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }
    }
}
