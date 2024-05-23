/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSpringMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSpringMappingId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MAD Federmapping Importer
 * Die Datei enthält das Mapping für ZB Federbein auf Feder
 * Diese Datei ist die Zentrale Datei der Migration
 */
public class MADSpringMappingImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String ZB_SPRING_LEG = "ZB_FEDERBEIN";
    final static String SPRING = "FEDER";
    final static String CREATION_DATE = "ANLAGEDATUM";
    final static String CHANGE_DATE = "AENDERUNGSDATUM";

    private String[] headerNames = new String[]{
            ZB_SPRING_LEG,
            SPRING,
            CREATION_DATE,
            CHANGE_DATE
    };

    private HashMap<String, String> mappingSpringData;
    private String[] primaryKeysSpringImport;
    private String tableName = TABLE_DA_SPRING_MAPPING;
    private iPartsNumberHelper numberHelper;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    public MADSpringMappingImporter(EtkProject project) {
        super(project, "MAD Feder-Mapping",
              new FilesImporterFileListType(TABLE_DA_SPRING_MAPPING, "!!MAD-Feder-Mapping", true, false, true,
                                            new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
        numberHelper = new iPartsNumberHelper();
    }


    private void initMapping() {
        primaryKeysSpringImport = new String[]{ ZB_SPRING_LEG };
        mappingSpringData = new HashMap<String, String>();
        mappingSpringData.put(FIELD_DSM_SPRING, SPRING);
        mappingSpringData.put(FIELD_DSM_EDAT, CREATION_DATE);
        mappingSpringData.put(FIELD_DSM_ADAT, CHANGE_DATE);
    }


    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustHaveAndExist = StrUtils.mergeArrays(primaryKeysSpringImport, new String[]{ SPRING });
        importer.setMustExists(mustHaveAndExist);
        importer.setMustHaveData(mustHaveAndExist);

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SpringImportHelper importHelper = new SpringImportHelper(getProject(), mappingSpringData, tableName);
        String springLeg = importHelper.handleValueOfSpecialField(ZB_SPRING_LEG, importRec);
        String spring = importHelper.handleValueOfSpecialField(SPRING, importRec);
        if (springLeg.isEmpty() || spring.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, weil ZB Federbeinnummer und " +
                                                        "Federnummer nicht leer sein dürfen. ZB Fedeberin: %2; Feder: %3",
                                                        String.valueOf(recordNo), springLeg, spring),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return;
        }
        iPartsSpringMappingId id = new iPartsSpringMappingId(springLeg);
        iPartsDataSpringMapping dataSpringMapping = new iPartsDataSpringMapping(getProject(), id);
        if (!dataSpringMapping.existsInDB()) {
            dataSpringMapping.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        importHelper.fillOverrideCompleteDataForMADReverse(dataSpringMapping, importRec, iPartsMADLanguageDefs.MAD_DE);
        if (importToDB) {
            saveToDB(dataSpringMapping);
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(tableName)) {
            getProject().getDB().delete(tableName);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNames));
        } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNames));
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
    }

    private class SpringImportHelper extends MADImportHelper {

        public SpringImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(CHANGE_DATE) || sourceField.equals(CREATION_DATE)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(ZB_SPRING_LEG) || sourceField.equals(SPRING)) {
                value = StrUtils.replaceSubstring(numberHelper.checkNumberInputFormat(value.trim(), getMessageLog()), " ", "");
            }
            return value;
        }
    }
}
