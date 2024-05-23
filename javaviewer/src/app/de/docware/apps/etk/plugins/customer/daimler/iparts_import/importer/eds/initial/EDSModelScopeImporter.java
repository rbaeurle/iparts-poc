/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataOPSScope;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsOPSScopeId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;

import java.util.HashMap;
import java.util.Map;

/**
 * EDS (Bom-DB) Baumusterumfang (Navigational module) Importer (T43RUMF)
 */
public class EDSModelScopeImporter extends AbstractBOMDataImporter {

    public static final String IMPORT_TABLENAME = "T43RUMF";

    private static final String EDS_UMF_UMF = "UMF_UMF";  // Umfang (Feld "UMF"; 12-stellig)
    private static final String EDS_UMF_SPS = "UMF_SPS";  // Sprachschlüssel (Feld "SPS"; 2-stellig)
    private static final String EDS_UMF_BEN = "UMF_BEN";  // Benennung (Feld "BEN"; 50-stellig)

    private String[] headerNames = new String[]{
            EDS_UMF_UMF,
            EDS_UMF_SPS,
            EDS_UMF_BEN
    };

    private String[] primaryKeysImport;
    private Map<String, iPartsDataOPSScope> scopeMapping;

    public EDSModelScopeImporter(EtkProject project) {
        super(project, "EDS Baumusterumfang (T43RUMF)", true, TABLE_DA_OPS_SCOPE, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_OPS_SCOPE, "!!EDS/BCS Baumusterumfang Stammdatei", true, false, true, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        primaryKeysImport = new String[]{ EDS_UMF_UMF };
        // Standard Mapping für neuen oder BOM-DB Datensatz
        mapping.put(FIELD_DOS_DESC, EDS_UMF_BEN);
    }

    @Override
    protected String[] getMustExist() {
        return primaryKeysImport;
    }

    @Override
    protected String[] getMustHaveData() {
        return primaryKeysImport;
    }

    @Override
    protected void preImportTask() {
        scopeMapping = new HashMap<>();
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EDSModelScopeImportHelper helper = new EDSModelScopeImportHelper(getProject(), getMapping(), getDestinationTable());
        iPartsOPSScopeId opsScopeId = new iPartsOPSScopeId(helper.handleValueOfSpecialField(EDS_UMF_UMF, importRec));
        if (opsScopeId.getScope().isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerem Umfang \"%2\" übersprungen",
                                                        String.valueOf(recordNo), importRec.get(EDS_UMF_UMF)),
                                        MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        iPartsEDSLanguageDefs langDef = iPartsEDSLanguageDefs.getType(helper.handleValueOfSpecialField(EDS_UMF_SPS, importRec));
        if (langDef == iPartsEDSLanguageDefs.EDS_UNKNOWN) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wegen ungültigem Sprachkürzel \"%2\" übersprungen",
                                                        String.valueOf(recordNo), importRec.get(EDS_UMF_SPS)),
                                        MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        iPartsDataOPSScope dataOPSScope = scopeMapping.get(opsScopeId.getScope());
        if (dataOPSScope == null) {
            dataOPSScope = new iPartsDataOPSScope(getProject(), opsScopeId);
            if (!dataOPSScope.loadFromDB(opsScopeId)) {
                dataOPSScope.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            scopeMapping.put(opsScopeId.getScope(), dataOPSScope);
        } else {
            reduceRecordCount();
        }
        helper.fillOverrideOneLanguageTextForEDS(dataOPSScope, langDef, FIELD_DOS_DESC, helper.handleValueOfSpecialField(EDS_UMF_BEN, importRec));
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                getMessageLog().fireProgress(0, scopeMapping.size(), "", true, false);
                int counter = 0;
                for (Map.Entry<String, iPartsDataOPSScope> entry : scopeMapping.entrySet()) {
                    saveToDB(entry.getValue());
                    getMessageLog().fireProgress(counter++, scopeMapping.size(), "", true, true);
                }
                getMessageLog().hideProgress();
            }
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            getProject().getDB().delete(getDestinationTable());
        }
        return true;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(prepareImporterKeyValueGZ(importFile, getDestinationTable(), '|', withHeader, headerNames, Character.MIN_VALUE, DWFileCoding.CP_1252));
            } else {
                return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', withHeader, headerNames, Character.MIN_VALUE, DWFileCoding.CP_1252));
            }
        }
        return false;
    }

    private class EDSModelScopeImportHelper extends EDSImportHelper {

        public EDSModelScopeImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(EDS_UMF_BEN)) {
                value = StrUtils.trimRight(value);
            } else {
                value = value.trim();
            }
            return value;
        }
    }
}
