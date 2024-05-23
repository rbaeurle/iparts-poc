/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsEDSModelContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSModelMasterImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSWbSaaCalculationHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Importer für EDS Baumusterinhalt (B2I)
 */
public class EDSModelMasterContentUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RB2I";

    public static final String B2I_MODEL = "Model";  // Baumuster
    public static final String B2I_MODULE = "Module"; // Gruppe
    public static final String B2I_SCOPE = "Scope";
    public static final String B2I_POSITION = "Position";
    public static final String B2I_STEERING_TYPE = "SteeringType";
    public static final String B2I_BODYTYPES = "BodyTypes";
    public static final String B2I_BODYTYPE = "BodyType";
    public static final String B2I_VERSION_FROM = "VersionFrom";
    public static final String B2I_ECO_FROM = "EcoFrom";
    public static final String B2I_RELEASE_FROM = "ReleaseDateFrom";
    private static final String B2I_VERSION_TO = "VersionTo";
    public static final String B2I_ECO_TO = "EcoTo";
    public static final String B2I_RELEASE_TO = "ReleaseDateTo";
    public static final String B2I_ITEM = "Item";
    public static final String B2I_MATURITY_LEVEL = "MaturityLevel";
    public static final String B2I_QUANTITY = "Quantity";
    public static final String B2I_PRODUCT_GROUP = "ProductGroup";
    public static final String B2I_CODE_RULE = "CodeRule";
    public static final String B2I_PLANTSUPPLIES = "PlantSupplies";
    public static final String B2I_PLANTSUPPLY = "PlantSupply";
    public static final String B2I_STATUS_FROM = "StatusFrom";
    public static final String B2I_STATUS_TO = "StatusTo";
    private static final String B2I_TYPE = "Type";
    private static final String B2I_ENGINEERING_DATE_FROM = "EngineeringDateFrom";
    private static final String B2I_ENGINEERING_DATE_TO = "EngineeringDateTo";
    private static final String B2I_MANUAL_FLAG_FROM = "ManualFlagFrom";
    private static final String B2I_MANUAL_FLAG_TO = "ManualFlagTo";

    private Set<IdWithType> usedIds;
    private EDSWbSaaCalculationHelper wbCalcHelper;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public EDSModelMasterContentUpdateImporter(EtkProject project) {
        super(project, "!!EDS-Baumusterinhalt (B2I)", TABLE_DA_EDS_MODEL, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_EDS_MODEL, EDS_MODEL_CONTENT_NAME, false, false, true, new String[]{ MimeTypes.EXTENSION_XML }));

    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        mapping.put(FIELD_EDS_MODEL_MODELNO, B2I_MODEL);
        mapping.put(FIELD_EDS_MODEL_GROUP, B2I_MODULE);
        mapping.put(FIELD_EDS_MODEL_SCOPE, B2I_SCOPE);
        mapping.put(FIELD_EDS_MODEL_POS, B2I_POSITION);
        mapping.put(FIELD_EDS_MODEL_STEERING, B2I_STEERING_TYPE);
        mapping.put(FIELD_EDS_MODEL_REVFROM, B2I_VERSION_FROM);
        mapping.put(FIELD_EDS_MODEL_KEMFROM, B2I_ECO_FROM);
        mapping.put(FIELD_EDS_MODEL_RELEASE_FROM, B2I_RELEASE_FROM);
        mapping.put(FIELD_EDS_MODEL_REVTO, B2I_VERSION_TO);
        mapping.put(FIELD_EDS_MODEL_KEMTO, B2I_ECO_TO);
        mapping.put(FIELD_EDS_MODEL_RELEASE_TO, B2I_RELEASE_TO);
        mapping.put(FIELD_EDS_MODEL_MSAAKEY, B2I_ITEM);
        mapping.put(FIELD_EDS_MODEL_RFG, B2I_MATURITY_LEVEL);
        mapping.put(FIELD_EDS_MODEL_QUANTITY, B2I_QUANTITY);
        mapping.put(FIELD_EDS_MODEL_PGKZ, B2I_PRODUCT_GROUP);
        mapping.put(FIELD_EDS_MODEL_CODE, B2I_CODE_RULE);

        allXMLElements.addAll(mapping.values());
        allXMLElements.add(B2I_PLANTSUPPLIES);
        allXMLElements.add(B2I_PLANTSUPPLY);
        allXMLElements.add(B2I_STATUS_FROM);
        allXMLElements.add(B2I_STATUS_TO);
        allXMLElements.add(B2I_TYPE);
        allXMLElements.add(B2I_ENGINEERING_DATE_FROM);
        allXMLElements.add(B2I_ENGINEERING_DATE_TO);
        allXMLElements.add(B2I_MANUAL_FLAG_FROM);
        allXMLElements.add(B2I_MANUAL_FLAG_TO);
        allXMLElements.add(B2I_BODYTYPES);
        allXMLElements.add(B2I_BODYTYPE);
    }

    @Override
    protected void preImportTask() {
        usedIds = new HashSet<>();
        wbCalcHelper = new EDSWbSaaCalculationHelper(getProject(), this);
        super.preImportTask();
    }

    @Override
    protected void postImportTask() {
        // Zuerst die Daten aus dem Import speichern, damit dann in der Arbeitsvorrat-Vorverdichtung darauf
        // zugegriffen werden kann
        super.postImportTask();

        setBufferedSave(true);
        // Arbeitsvorrat Vorverdichtung mit anschließendem Speichern
        wbCalcHelper.handleSaaAVEntries();
        super.postImportTask();

        usedIds = new HashSet<>();
        wbCalcHelper = null;
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ B2I_MODEL, B2I_MODULE, B2I_SCOPE, B2I_POSITION };
    }

    @Override
    protected String[] getMustHaveData() {
        return new String[]{ B2I_MODEL, B2I_ITEM };
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EDSModelContentImportHelper helper = new EDSModelContentImportHelper(getProject(), getMapping(), getDestinationTable(), importRec.get(B2I_STATUS_TO));
        if (!helper.isValidRecord(importRec, B2I_STATUS_FROM)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 (VAKZ_AB) \"%3\" übersprungen (Datensatz nicht freigegeben)",
                                                        String.valueOf(recordNo), B2I_STATUS_FROM, importRec.get(B2I_STATUS_FROM)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            return;
        }
        String bodyTypes = helper.buildStringFromSubDatasets(importRec, B2I_BODYTYPES, B2I_BODYTYPE, 1);
        if (bodyTypes.length() > 1) {
            // bei ungültigem BodyType Warnung ausgeben und Datensatz nicht importieren
            getMessageLog().fireMessage(translateForLog("!!Record %1 ungültig und wird übersprungen (BodyType (AA) darf nur einen einstelligen Wert haben. AA: %2)",
                                                        String.valueOf(recordNo), bodyTypes), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return;
        }
        iPartsEDSModelContentId id = new iPartsEDSModelContentId(helper.handleValueOfSpecialField(B2I_MODEL, importRec), helper.handleValueOfSpecialField(B2I_MODULE, importRec),
                                                                 helper.handleValueOfSpecialField(B2I_SCOPE, importRec), helper.handleValueOfSpecialField(B2I_POSITION, importRec),
                                                                 helper.handleValueOfSpecialField(B2I_STEERING_TYPE, importRec), bodyTypes,
                                                                 helper.handleValueOfSpecialField(B2I_VERSION_FROM, importRec));
        iPartsDataEDSModelContent dataObject = new iPartsDataEDSModelContent(getProject(), id);
        boolean existsInDb = true;
        if (!dataObject.existsInDB()) {
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            existsInDb = false;
        }
        helper.fillOverrideCompleteDataForEDSReverse(dataObject, importRec, iPartsEDSLanguageDefs.EDS_DE);
        helper.fillPlantSupplies(dataObject, importRec, B2I_PLANTSUPPLIES, B2I_PLANTSUPPLY, FIELD_EDS_MODEL_PLANTSUPPLY);
        helper.createSaaModelEntryIfNotExists(this, dataObject, importToDB, usedIds);
        helper.createModelsAggsEntryIfNotExists(this, dataObject, importToDB, usedIds);

        wbCalcHelper.addToMap(existsInDb, dataObject);

        if (importToDB) {
            saveToDB(dataObject);
        }

    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            getProject().getDB().delete(getDestinationTable());
        }
        return true;
    }

    private class EDSModelContentImportHelper extends EDSModelMasterImportHelper {

        private String vakzToValue;

        public EDSModelContentImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName, String vakzToValue) {
            super(project, mapping, tableName);
            this.vakzToValue = handleValueOfSpecialField(B2I_STATUS_TO, vakzToValue);
        }

        @Override
        protected void importValue(EtkDataObject dataObject, String importFieldName, String dbDestFieldName, String value, Language langDef) {
            // null Values sind zugelassen
            if ((value == null) && getAllXMLElements().contains(importFieldName)) {
                value = "";
            }
            super.importValue(dataObject, importFieldName, dbDestFieldName, value, langDef);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            // nur bei VAKZ_BIS = leer wird der Originalwert übernommen bei den folgenden Feldern, ansonsten wird der Wert abgeändert
            if (StrUtils.isValid(vakzToValue)) {
                if (sourceField.equals(B2I_VERSION_TO)) { // Änderungsstand bis
                    value = EDS_AS_BIS_UNENDLICH;
                } else if (sourceField.equals(B2I_ECO_TO)) { // KEM bis
                    value = "";
                } else if (sourceField.equals(B2I_RELEASE_TO)) { // Freigabetermin bis
                    value = "";
                }
            }
            // Felder für ein Update, die nicht leer sein dürfen
            if (sourceField.equals(B2I_VERSION_TO) || sourceField.equals(B2I_ECO_TO) || sourceField.equals(B2I_RELEASE_TO)) {
                if (value == null) {
                    value = "";
                }
            }

            if (sourceField.equals(B2I_STEERING_TYPE) || sourceField.equals(B2I_BODYTYPES) || sourceField.equals(B2I_PLANTSUPPLIES)
                || sourceField.equals(B2I_MATURITY_LEVEL) || sourceField.equals(B2I_STATUS_FROM) || sourceField.equals(B2I_STATUS_TO)) {
                if (value == null) {
                    value = "";
                }
            } else if (sourceField.equals(B2I_RELEASE_FROM) || sourceField.equals(B2I_RELEASE_TO)) {
                if ((value == null) || (value.length() > 15)) {
                    String originalValue = value;
                    value = getEDSDateTimeValueFromISO(value);
                    if (value == null) {
                        cancelImport(translateForLog("!!Ungültiges DateTime-Format (%1: %2). Automatische Korrektur nicht möglich!",
                                                     sourceField, originalValue));
                        value = "";
                    }
                } else {
                    value = getEDSDateTimeValue(value);
                }
            }

            if (sourceField.equals(B2I_POSITION) && StrUtils.isValid(value)) {
                value = StrUtils.leftFill(value, 3, '0');
            }
            return value;
        }
    }
}