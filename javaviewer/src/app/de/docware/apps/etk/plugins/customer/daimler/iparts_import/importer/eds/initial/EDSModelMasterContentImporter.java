/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsEDSModelContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSModelMasterImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * EDS (Bom-DB) Baumusterinhalt-Stammdaten Importer (T43RB2I)
 */
public class EDSModelMasterContentImporter extends AbstractBOMDataImporter {

    public static final String IMPORT_TABLENAME = "T43RB2I";

    private static final String EDS_B2I_SNR = "B2I_SNR";  // Fahrzeug oder Aggregatebaumustergruppe (Feld "SNR"; 17-stellig)
    private static final String EDS_B2I_UMF = "B2I_UMF";  // Umfang (Feld "UMF"; 12-stellig)
    private static final String EDS_B2I_POS = "B2I_POS";  // Position (Feld "POS"; 3-stellig)
    private static final String EDS_B2I_LKG = "B2I_LKG";  // Lenkung (Feld "LKG"; 1-stellig)
    private static final String EDS_B2I_AA = "B2I_AA";   // Aufbauart (Feld "AA"; 1-stellig)
    private static final String EDS_B2I_AS_AB = "B2I_AS_AB"; // Änderungsstand Ab (Feld "AS_AB"; 3-stellig)
    private static final String EDS_B2I_KEM_AB = "B2I_KEM_AB"; // KEM Ab (Feld "KEM_AB"; 13-stellig)
    private static final String EDS_B2I_VAKZ_AB = "B2I_VAKZ_AB"; // Verarbeitungskennzeichen Ab (Feld „VAKZ_AB“; 2-stellig)
    private static final String EDS_B2I_FRG_DAT_AB = "B2I_FRG_DAT_AB"; // Freigabetermin Ab (Feld "FRG_DAT_AB"; 14-stellig)
    private static final String EDS_B2I_AS_BIS = "B2I_AS_BIS";  // Änderungsstand Bis (Feld "AS_BIS"; 3-stellig; nur übernehmen bei Datensätzen mit VAKZ_BIS = leer, ansonsten den Wert '999' aufnehmen)
    private static final String EDS_B2I_KEM_BIS = "B2I_KEM_BIS"; // KEM Bis (Feld "KEM_BIS"; 13-stellig; nur übernehmen bei Datensätzen mit VAKZ_BIS = leer)
    private static final String EDS_B2I_VAKZ_BIS = "B2I_VAKZ_BIS"; // Verarbeitungskennzeichen Bis (Feld „VAKZ_BIS“; 2-stellig)
    private static final String EDS_B2I_FRG_DAT_BIS = "B2I_FRG_DAT_BIS"; // Freigabetermin Bis (Feld "FRG_DAT_BIS"; 14-stellig; nur übernehmen bei Datensätzen mit VAKZ_BIS = leer, ansonsten den Wert '99999999999999' aufnehmen)
    private static final String EDS_B2I_SNRU = "B2I_SNRU";  // Untere Sachnummer (Feld "SNRU"; 13-stellig)
    private static final String EDS_B2I_RF = "B2I_RF";    // Reifegrad (Feld "RF"; 1-stellig)
    private static final String EDS_B2I_MG = "B2I_MG";    // Menge (Feld "MG"; 2-stellig)
    private static final String EDS_B2I_PGKZ = "B2I_PGKZ";  // Produktgruppenkennzeichen (Feld "PGKZ"; 1-stellig)
    private static final String EDS_B2I_CDBED = "B2I_CDBED"; // Codebedingung (Feld "CDBED"; 150-stellig)
    private static final String EDS_B2I_WK = "B2I_WK";    // Werke (Feld "WK"; 24-stellig; bis zu 12 2-stellige Werke)

    /* Umsetzung zu EDSModelContentImporter
    EDS_B2I_SNR         = B2I_MODEL = "Model";  // Baumuster
                          B2I_MODULE = "Module"; // Gruppe  Teil von B2I_MODEL
    EDS_B2I_UMF         = B2I_SCOPE = "Scope";
    EDS_B2I_POS         = B2I_POSITION = "Position";
    EDS_B2I_LKG         = B2I_STEERING_TYPE = "SteeringType";
                          B2I_BODYTYPES = "BodyTypes";
    EDS_B2I_AA          = B2I_BODYTYPE = "BodyType";
    EDS_B2I_AS_AB       = B2I_VERSION_FROM = "VersionFrom";
    EDS_B2I_KEM_AB      = B2I_ECO_FROM = "EcoFrom";
    EDS_B2I_FRG_DAT_AB  = B2I_RELEASE_FROM = "ReleaseDateFrom";
    EDS_B2I_AS_BIS      = B2I_VERSION_TO = "VersionTo";
    EDS_B2I_KEM_BIS     = B2I_ECO_TO = "EcoTo";
    EDS_B2I_FRG_DAT_BIS = B2I_RELEASE_TO = "ReleaseDateTo";
    EDS_B2I_SNRU        = B2I_ITEM = "Item";
    EDS_B2I_RF          = B2I_MATURITY_LEVEL = "MaturityLevel";
    EDS_B2I_MG          = B2I_QUANTITY = "Quantity";
    EDS_B2I_PGKZ        = B2I_PRODUCT_GROUP = "ProductGroup";
    EDS_B2I_CDBED       = B2I_CODE_RULE = "CodeRule";
                          B2I_PLANTSUPPLIES = "PlantSupplies";
    EDS_B2I_WK          = B2I_PLANTSUPPLY = "PlantSupply";
    EDS_B2I_VAKZ_AB     = B2I_STATUS_FROM = "StatusFrom";
    EDS_B2I_VAKZ_BIS    = B2I_STATUS_TO = "StatusTo";
     */

    private String[] primaryKeysImport;
    private HashMap<String, String> idMapping;
    private HashMap<String, String> kemMapping;
    private Set<IdWithType> usedIds;

    public EDSModelMasterContentImporter(EtkProject project) {
        super(project, "EDS-Stammdaten Baumusterinhalt (B2I)", true, TABLE_DA_EDS_MODEL, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_EDS_MODEL, "!!EDS/BCS Baumusterinhalt Stammdatei", true, false, false, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void initMapping(HashMap<String, String> mapping) {
        primaryKeysImport = new String[]{ EDS_B2I_SNR };
        // Mapping für iPartsEDSModelContentId
        idMapping = new HashMap<>();
        idMapping.put(FIELD_EDS_MODEL_MODELNO, EDS_B2I_SNR); // Spezialbehandlung
        idMapping.put(FIELD_EDS_MODEL_GROUP, EDS_B2I_SNR);  // Spezialbehandlung
        idMapping.put(FIELD_EDS_MODEL_SCOPE, EDS_B2I_UMF);
        idMapping.put(FIELD_EDS_MODEL_POS, EDS_B2I_POS);
        idMapping.put(FIELD_EDS_MODEL_STEERING, EDS_B2I_LKG);
        idMapping.put(FIELD_EDS_MODEL_AA, EDS_B2I_AA);
        idMapping.put(FIELD_EDS_MODEL_REVFROM, EDS_B2I_AS_AB);

        // Standard Mapping ohne die Felder für iPartsEDSModelContentId
        mapping.put(FIELD_EDS_MODEL_KEMFROM, EDS_B2I_KEM_AB);
        mapping.put(FIELD_EDS_MODEL_RELEASE_FROM, EDS_B2I_FRG_DAT_AB);
        mapping.put(FIELD_EDS_MODEL_REVTO, EDS_B2I_AS_BIS);
        mapping.put(FIELD_EDS_MODEL_KEMTO, EDS_B2I_KEM_BIS);
        mapping.put(FIELD_EDS_MODEL_RELEASE_TO, EDS_B2I_FRG_DAT_BIS);
        mapping.put(FIELD_EDS_MODEL_MSAAKEY, EDS_B2I_SNRU);
        mapping.put(FIELD_EDS_MODEL_RFG, EDS_B2I_RF);
        mapping.put(FIELD_EDS_MODEL_QUANTITY, EDS_B2I_MG);
        mapping.put(FIELD_EDS_MODEL_PGKZ, EDS_B2I_PGKZ);
        mapping.put(FIELD_EDS_MODEL_CODE, EDS_B2I_CDBED);


        kemMapping = new HashMap<>();
        kemMapping.put(FIELD_EDS_REVTO, EDS_B2I_AS_BIS);
        kemMapping.put(FIELD_EDS_KEMTO, EDS_B2I_KEM_BIS);
        kemMapping.put(FIELD_EDS_RELEASE_TO, EDS_B2I_FRG_DAT_BIS);
    }

    @Override
    protected void preImportTask() {
        usedIds = new HashSet<>();
        super.preImportTask();
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        usedIds = null;
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
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EDSModelMasterContentImportHelper helper = new EDSModelMasterContentImportHelper(getProject(), getMapping(), getDestinationTable(), importRec.get(EDS_B2I_VAKZ_BIS));
        if (!helper.isValidRecord(importRec)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit VAKZ_AB \"%2\" übersprungen (Datensatz nicht freigegeben)",
                                                        String.valueOf(recordNo), importRec.get(EDS_B2I_VAKZ_AB)),
                                        MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        iPartsEDSModelContentId id = helper.buildEDSModelContentId(importRec, idMapping);
        if (cancelled) {
            return;
        }

        iPartsDataEDSModelContent dataObject = new iPartsDataEDSModelContent(getProject(), id);
        if (!dataObject.existsInDB()) {
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        helper.fillOverrideCompleteDataForEDSReverse(dataObject, importRec, iPartsEDSLanguageDefs.EDS_DE);
        helper.fillPlantSupplies(dataObject, importRec, EDS_B2I_WK, FIELD_EDS_MODEL_PLANTSUPPLY);

        if (!cancelled) {
            helper.createSaaModelEntryIfNotExists(this, dataObject, importToDB, usedIds);
            helper.createModelsAggsEntryIfNotExists(this, dataObject, importToDB, usedIds);
            if (importToDB) {
                saveToDB(dataObject);
            }
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(prepareImporterKeyValueGZ(importFile, getDestinationTable(), '|', withHeader, null, Character.MIN_VALUE));
            } else {
                return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', withHeader, null, Character.MIN_VALUE));
            }
        }
        return false;
    }

    private class EDSModelMasterContentImportHelper extends EDSModelMasterImportHelper {

        private String vakzToValue;

        public EDSModelMasterContentImportHelper(EtkProject project, Map<String, String> mapping, String tableName, String vakzToValue) {
            super(project, mapping, tableName);
            this.vakzToValue = handleValueOfSpecialField(EDS_B2I_VAKZ_BIS, vakzToValue);
        }

        /**
         * Prüft, ob der Datensatz valide ist und importiert werden kann (VAKZ ab muss leer sein)
         *
         * @param importRec
         * @return
         */
        public boolean isValidRecord(Map<String, String> importRec) {
            String vakzFrom = handleValueOfSpecialField(EDS_B2I_VAKZ_AB, importRec);
            return StrUtils.isEmpty(vakzFrom);
        }

        /**
         * bildet die Id durch füllen der PK-Values (mapping) in ein neues DataObjerct
         * dabei werden alle Wandlungen der Werte durch handleValueOfSpecialField und extractCorrespondingSubstring erledigt
         *
         * @param importRec
         * @param mapping
         * @return
         */
        public iPartsEDSModelContentId buildEDSModelContentId(Map<String, String> importRec, Map<String, String> mapping) {
            EDSModelMasterContentImportHelper helper = new EDSModelMasterContentImportHelper(getProject(), mapping, tableName, vakzToValue);
            iPartsDataEDSModelContent dataObject = new iPartsDataEDSModelContent(getProject(), null);
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            helper.fillOverrideCompleteDataForEDSReverse(dataObject, importRec, iPartsEDSLanguageDefs.EDS_DE);
            return dataObject.getAsId();
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            // nur bei VAKZ_BIS = leer wird der Originalwert übernommen bei den folgenden Feldern, ansonsten wird der Wert abgeändert
            if (StrUtils.isValid(vakzToValue)) {
                if (sourceField.equals(EDS_B2I_AS_BIS)) { // Änderungsstand bis
                    value = EDS_AS_BIS_UNENDLICH;
                } else if (sourceField.equals(EDS_B2I_KEM_BIS)) { // KEM bis
                    value = "";
                } else if (sourceField.equals(EDS_B2I_FRG_DAT_BIS)) { // Freigabetermin bis
                    value = "";
                }
            }
            if (sourceField.equals(EDS_B2I_FRG_DAT_AB) || sourceField.equals(EDS_B2I_FRG_DAT_BIS)) {
                if (value.length() > 15) {
                    String originalValue = value;
                    value = getEDSDateTimeValueFromISO(value);
                    if (value != null) {
                        getMessageLog().fireMessage(translateForLog("!!Ungültiges DateTime-Format (%1: %2). Automatische Korrektur: %3",
                                                                    sourceField, originalValue, value), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    } else {
                        cancelImport(translateForLog("!!Ungültiges DateTime-Format (%1: %2). Automatische Korrektur nicht möglich!",
                                                     sourceField, originalValue));
                        value = "";
                    }
                } else {
                    value = getEDSDateTimeValue(value);
                }
            } else if (sourceField.equals(EDS_B2I_CDBED)) {
                if (value.trim().isEmpty()) {
                    value = ";";
                } else {
                    value = StrUtils.removeCharsFromString(value, new char[]{ ' ' });
                }
            } else /*if (!sourceField.equals(EDS_B2I_WK))*/ {
                value = value.trim();
            }
            return value;
        }

        @Override
        protected String extractCorrespondingSubstring(String sourceField, String destField, String sourceValue) {
            if (destField.equals(FIELD_EDS_MODEL_MODELNO) || destField.equals(FIELD_EDS_MODEL_GROUP)) {
                String[] splittedModelAndGroup = sourceValue.split("\\s+");
                if (splittedModelAndGroup.length != 2) {
                    cancelImport(translateForLog("!!fehlerhafter Record (ungültiges Format für Baumustergruppe und Umfang: %1)",
                                                 sourceValue));
                    return sourceValue;
                }
                if (destField.equals(FIELD_EDS_MODEL_MODELNO)) {
                    sourceValue = splittedModelAndGroup[0].trim();
                } else {
                    sourceValue = splittedModelAndGroup[1].trim();
                }
            }
            return sourceValue;
        }
    }
}
