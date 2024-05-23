/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataOPSGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsOPSGroupId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Importer für EDS Baumustergruppen (BMAG)
 */
public class EDSModelGroupUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RBMAG";

    private static final String BMAG_MODULE = "ModuleNumber"; // Gruppe
    private static final String BMAG_MODEL = "ModelNumber";  // Baumuster
    private static final String BMAG_MODULE_LANG_DATA = "ModuleLangData";
    private static final String BMAG_RELEASE_FROM = "ReleaseDateFrom";
    private static final String BMAG_RELEASE_TO = "ReleaseDateTo";
    private static final String BMAG_ENGINEERING_DATE_FROM = "EngineeringDateFrom";
    private static final String BMAG_ENGINEERING_DATE_TO = "EngineeringDateTo";
    private static final String BMAG_MANUAL_FLAG_FROM = "ManualFlagFrom";
    private static final String BMAG_MANUAL_FLAG_TO = "ManualFlagTo";
    private static final String BMAG_ECO_FROM = "EcoFrom";
    private static final String BMAG_ECO_TO = "EcoTo";
    private static final String BMAG_VAKZ_AB = "StatusFrom";
    private static final String BMAG_VAKZ_BIS = "StatusTo";
    private static final String BMAG_UNG_KZ_BIS = "VersionToInvalidFlag";
    private static final String BMAG_DOC_STATE = "DocumentationStatus";
    private static final String BMAG_APPROVED_M_TYPES = "ApprovedModelTypes";
    private static final String BMAG_LEAD_DESIGN_RELEASE = "LeadingDesignRelease";
    private static final String BMAG_PLANTSUPPLIES = "PlantSupplies";
    private static final String BMAG_PLANTSUPPLY = "PlantSupply";
    private static final String BMAG_BEN = "Description";  // Benennung
    private static final String BMAG_DOC_STATE_DESC = "DocumentationStatusDescription";
    private static final String BMAG_AS_AB = "VersionFrom";
    private static final String BMAG_AS_BIS = "VersionTo";

    private HashMap<String, iPartsDataOPSGroup> handledDataObjects;


    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public EDSModelGroupUpdateImporter(EtkProject project) {
        super(project, "!!EDS-Baumustergruppe (BMAG)", TABLE_DA_OPS_GROUP, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_OPS_GROUP, EDS_MODEL_GROUP_NAME, false, false, true, new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        mapping.put(FIELD_DOG_AS_FROM, BMAG_AS_AB);
        mapping.put(FIELD_DOG_DESC, BMAG_BEN);

        allXMLElementsToConsider.addAll(mapping.values());
        allXMLElementsToConsider.add(BMAG_MODEL);
        allXMLElementsToConsider.add(BMAG_MODULE);
        allXMLElementsToConsider.add(BMAG_VAKZ_AB);
        allXMLElementsToConsider.add(BMAG_VAKZ_BIS);
        allXMLElementsToConsider.add(BMAG_RELEASE_FROM);
        allXMLElementsToConsider.add(BMAG_RELEASE_TO);
        allXMLElementsToConsider.add(BMAG_AS_AB);

        allXMLElements.addAll(mapping.values());
        allXMLElements.add(BMAG_MODEL);
        allXMLElements.add(BMAG_ECO_FROM);
        allXMLElements.add(BMAG_RELEASE_FROM);
        allXMLElements.add(BMAG_ECO_TO);
        allXMLElements.add(BMAG_RELEASE_TO);
        allXMLElements.add(BMAG_PLANTSUPPLIES);
        allXMLElements.add(BMAG_PLANTSUPPLY);
        allXMLElements.add(BMAG_VAKZ_AB);
        allXMLElements.add(BMAG_VAKZ_BIS);
        allXMLElements.add(BMAG_ENGINEERING_DATE_FROM);
        allXMLElements.add(BMAG_ENGINEERING_DATE_TO);
        allXMLElements.add(BMAG_MANUAL_FLAG_FROM);
        allXMLElements.add(BMAG_MANUAL_FLAG_TO);
        allXMLElements.add(BMAG_MODULE_LANG_DATA);
        allXMLElements.add(BMAG_AS_BIS);
        allXMLElements.add(BMAG_UNG_KZ_BIS);
        allXMLElements.add(BMAG_DOC_STATE_DESC);
        allXMLElements.add(BMAG_LEAD_DESIGN_RELEASE);
        allXMLElements.add(BMAG_APPROVED_M_TYPES);
        allXMLElements.add(BMAG_DOC_STATE);
    }

    @Override
    protected void preImportTask() {
        handledDataObjects = new HashMap<>();
        super.preImportTask();
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ BMAG_MODULE, BMAG_BEN };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ModelGroupImportHelper helper = new ModelGroupImportHelper(getProject(), getMapping(), getDestinationTable());
        helper.prepareXMLImportRec(importRec, getAllXMLElementsToConsider(), getAllXMLElements());

        if (!helper.isValidRecord(importRec, BMAG_VAKZ_AB)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 (VAKZ_AB) \"%3\" übersprungen (Datensatz nicht freigegeben)",
                                                        String.valueOf(recordNo), BMAG_VAKZ_AB, importRec.get(BMAG_VAKZ_AB)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        String model = helper.handleValueOfSpecialField(BMAG_MODEL, importRec);
        String group = helper.handleValueOfSpecialField(BMAG_MODULE, importRec);
        iPartsOPSGroupId opsGroupId = new iPartsOPSGroupId(model, group);
        if (opsGroupId.isEmpty() || opsGroupId.getModelNo().isEmpty() || opsGroupId.getGroup().isEmpty()) {
            if (opsGroupId.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerem Baumuster und Gruppe übersprungen",
                                                            String.valueOf(recordNo)),
                                            MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            } else if (opsGroupId.getModelNo().isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerem Baumuster und Gruppe \"%2\" übersprungen",
                                                            String.valueOf(recordNo), group),
                                            MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit Baumuster \"%2\" und leerer Gruppe übersprungen",
                                                            String.valueOf(recordNo), model),
                                            MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            }
            reduceRecordCount();
            return;
        }
        iPartsDataOPSGroup dataObject = handledDataObjects.get(opsGroupId.toDBString());
        if (dataObject == null) {
            dataObject = new iPartsDataOPSGroup(getProject(), opsGroupId);
            if (!dataObject.existsInDB()) {
                dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            handledDataObjects.put(opsGroupId.toDBString(), dataObject);
        } else {
            reduceRecordCount();
        }
        helper.fillDataObject(importRec, dataObject);
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                getMessageLog().fireProgress(0, handledDataObjects.size(), "", true, false);
                int counter = 0;
                for (iPartsDataOPSGroup dataOPSGroup : handledDataObjects.values()) {
                    saveToDB(dataOPSGroup);
                    getMessageLog().fireProgress(counter++, handledDataObjects.size(), "", true, true);
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

    private class ModelGroupImportHelper extends EDSImportHelper {

        public ModelGroupImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(BMAG_RELEASE_FROM) || sourceField.equals(BMAG_RELEASE_TO)) {
                if ((value.length() > 15)) {
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
            return value;
        }

        public void fillDataObject(Map<String, String> importRec, iPartsDataOPSGroup dataObject) {
            if (hasHigherOrEqualsVersion(importRec, dataObject.getRevisionStateFrom(), BMAG_VAKZ_AB, BMAG_AS_AB)) {
                // es ist ein BOM-DB Baumuster
                fillOverrideCompleteDataForEDSReverse(dataObject, importRec, iPartsEDSLanguageDefs.EDS_DE);
                // Nacharbeiten
                if (StrUtils.isEmpty(handleValueOfSpecialField(BMAG_VAKZ_BIS, importRec))) {
                    dataObject.setFieldValueAsBoolean(FIELD_DOG_INVALID, getInvalidSign(importRec, BMAG_UNG_KZ_BIS), DBActionOrigin.FROM_EDIT);
                    dataObject.setFieldValue(FIELD_DOG_AS_TO, handleValueOfSpecialField(BMAG_AS_BIS, importRec), DBActionOrigin.FROM_EDIT);
                } else {
                    dataObject.setFieldValue(FIELD_DOG_AS_TO, EDS_AS_BIS_UNENDLICH, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }
}
