/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataOPSGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsOPSGroupId;
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
import de.docware.util.sql.SQLStringConvert;

import java.util.HashMap;
import java.util.Map;

/**
 * EDS (Bom-DB) Baumustergruppe (Navigational module) Importer (T43RBMAG)
 */
public class EDSModelGroupImporter extends AbstractBOMDataImporter {

    public static final String IMPORT_TABLENAME = "T43RBMAG";

    private static final String EDS_BMAG_SNR = "BMAG_SNR";               // Baumustergruppe (Feld "SNR"; 17-stellig; in T43RBMAG und T43RBMAS vorhanden). Zur Information: Die Baumustergruppe wird in iParts nur 3- oder 4-stellig angezeigt (Stellen 14 bis 17).
    private static final String EDS_BMAG_AS_AB = "BMAG_AS_AB";           // Änderungsstand Ab (Feld "AS_AB"; 3-stellig; in T43RBMAG und T43RBMAS vorhanden)
    private static final String EDS_BMAG_VAKZ_AB = "BMAG_VAKZ_AB";       // Verarbeitungskennzeichen Ab (Feld „VAKZ_AB“; 2-stellig; nur in T43RBMAG vorhanden)
    private static final String EDS_BMAG_UNG_KZ_BIS = "BMAG_UNG_KZ_BIS"; // Ungültig-Kennzeichen (Feld "UNG_KZ_BIS"; einstellig; nur in T43RBMAG vorhanden; nur übernehmen bei Datensätzen mit VAKZ_BIS = leer)
    private static final String EDS_BMAG_AS_BIS = "BMAG_AS_BIS";         // Änderungsstand Bis (Feld "AS_BIS"; 3-stellig; in T43RBMAG und T43RBMAS vorhanden; nur übernehmen bei Datensätzen mit VAKZ_BIS = leer, ansonsten den Wert '999' aufnehmen)
    private static final String EDS_BMAG_VAKZ_BIS = "BMAG_VAKZ_BIS";     // Verarbeitungskennzeichen Bis (Feld „VAKZ_BIS“; 2-stellig; nur in T43RBMAG vorhanden)
    private static final String EDS_BMAG_BEN = "BMAG_BEN";               // Benennung (Feld "BEN"; 50-stellig; in T43RBMAG und T43RBMAS vorhanden)

    private String[] headerNames = new String[]{
            EDS_BMAG_SNR,
            EDS_BMAG_AS_AB,
            EDS_BMAG_VAKZ_AB,
            EDS_BMAG_UNG_KZ_BIS,
            EDS_BMAG_AS_BIS,
            EDS_BMAG_VAKZ_BIS,
            EDS_BMAG_BEN
    };

    private String[] primaryKeysImport;
    private Map<String, iPartsDataOPSGroup> groupMapping;

    public EDSModelGroupImporter(EtkProject project) {
        super(project, "EDS Baumustergruppen (T43RBMAG)", true, TABLE_DA_OPS_GROUP, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_OPS_GROUP, "!!EDS/BCS Baumustergruppen Stammdatei", true, false, true, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        primaryKeysImport = new String[]{ EDS_BMAG_SNR };
        // Standard Mapping für neuen oder BOM-DB Datensatz
        mapping.put(FIELD_DOG_DESC, EDS_BMAG_BEN);
        mapping.put(FIELD_DOG_AS_FROM, EDS_BMAG_AS_AB);
        mapping.put(FIELD_DOG_INVALID, EDS_BMAG_UNG_KZ_BIS);
        mapping.put(FIELD_DOG_AS_TO, EDS_BMAG_AS_BIS);
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
        groupMapping = new HashMap<>();
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EDSModelGroupImportHelper helper = new EDSModelGroupImportHelper(getProject(), getMapping(), getDestinationTable(), importRec.get(EDS_BMAG_VAKZ_BIS));
        iPartsOPSGroupId opsGroupId = helper.getOpsGroupId(importRec);
        if (opsGroupId.isEmpty() || opsGroupId.getModelNo().isEmpty() || opsGroupId.getGroup().isEmpty() || opsGroupId.getGroup().startsWith("C")) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit SNR \"%2\" übersprungen",
                                                        String.valueOf(recordNo), importRec.get(EDS_BMAG_SNR)),
                                        MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        iPartsDataOPSGroup dataOPSGroup = groupMapping.get(opsGroupId.toDBString());
        if (dataOPSGroup == null) {
            dataOPSGroup = new iPartsDataOPSGroup(getProject(), opsGroupId);
            if (!dataOPSGroup.loadFromDB(opsGroupId)) {
                dataOPSGroup.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            groupMapping.put(opsGroupId.toDBString(), dataOPSGroup);
        } else {
            reduceRecordCount();
        }
        // nur höchsten oder gleichen Änderungsstand übernehmen
        if (helper.hasHigherOrEqualsVersion(importRec, dataOPSGroup.getRevisionStateFrom(), EDS_BMAG_VAKZ_AB, EDS_BMAG_AS_AB)) {
            // höheren Änderungsstand gefunden
            // Sonderbehandlung für Benennung (EDS_BMAG_BEN)
            importRec.put(EDS_BMAG_BEN, helper.handleValueOfSpecialField(EDS_BMAG_BEN, importRec));
            helper.fillOverrideCompleteDataForEDSReverse(dataOPSGroup, importRec, iPartsEDSLanguageDefs.EDS_DE);
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                getMessageLog().fireProgress(0, groupMapping.size(), "", true, false);
                int counter = 0;
                for (Map.Entry<String, iPartsDataOPSGroup> entry : groupMapping.entrySet()) {
                    saveToDB(entry.getValue());
                    getMessageLog().fireProgress(counter++, groupMapping.size(), "", true, true);
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

    private class EDSModelGroupImportHelper extends EDSImportHelper {

        private String vakzToValue;

        public EDSModelGroupImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName, String vakzToValue) {
            super(project, mapping, tableName);
            this.vakzToValue = handleValueOfSpecialField(EDS_BMAG_VAKZ_BIS, vakzToValue);
        }

        public iPartsOPSGroupId getOpsGroupId(Map<String, String> importRec) {
            String modelNo = extractCorrespondingSubstring(EDS_BMAG_SNR, FIELD_DOG_MODEL_NO, handleValueOfSpecialField(EDS_BMAG_SNR, importRec));
            String opsGroup = extractCorrespondingSubstring(EDS_BMAG_SNR, FIELD_DOG_GROUP, handleValueOfSpecialField(EDS_BMAG_SNR, importRec));
            return new iPartsOPSGroupId(modelNo, opsGroup);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            // nur bei VAKZ_BIS = leer wird der Originalwert übernommen bei den folgenden Feldern, ansonsten wird der Wert abgeändert
            if (StrUtils.isValid(vakzToValue)) {
                if (sourceField.equals(EDS_BMAG_AS_BIS)) { // Änderungsstand bis
                    value = EDS_AS_BIS_UNENDLICH;
                } else if (sourceField.equals(EDS_BMAG_UNG_KZ_BIS)) { // Ungültig-Kennzeichen
                    value = "";
                }
            }
            if (sourceField.equals(EDS_BMAG_BEN)) {
                value = StrUtils.trimRight(value);
            } else if (sourceField.equals(EDS_BMAG_UNG_KZ_BIS)) {
                value = SQLStringConvert.booleanToPPString(!value.trim().isEmpty());
            } else {
                value = value.trim();
            }
            return value;
        }

        @Override
        protected String extractCorrespondingSubstring(String sourceField, String destField, String sourceValue) {
            if (destField.equals(FIELD_DOG_MODEL_NO) || destField.equals(FIELD_DOG_GROUP)) {
                String[] splittedModelAndGroup = sourceValue.split("\\s+");
                if (splittedModelAndGroup.length != 2) {
                    cancelImport(translateForLog("!!Fehlerhafter Record (ungültiges Format für Baumuster und Gruppe: %1)",
                                                 sourceValue));
                    return sourceValue;
                }
                if (destField.equals(FIELD_DOG_MODEL_NO)) {
                    sourceValue = splittedModelAndGroup[0].trim();
                } else {
                    sourceValue = splittedModelAndGroup[1].trim();
                }
            }
            return sourceValue;
        }
    }
}
