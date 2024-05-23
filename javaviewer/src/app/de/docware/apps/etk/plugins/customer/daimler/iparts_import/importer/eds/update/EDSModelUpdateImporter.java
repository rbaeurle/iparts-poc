/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSModelImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * EDS (Bom-DB) Baumuster-Importer (Änderungsdienst) (T43RBM)
 */
public class EDSModelUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RBM";

    public static final String EDSU_BM_SNR = "ModelNumber";  // Baumuster
    public static final String EDSU_BM_AS_AB = "VersionFrom";  // Änderungsstand Ab
    public static final String EDSU_BM_VAKZ_AB = "StatusFrom";
    public static final String EDSU_BM_VAKZ_BIS = "StatusTo";
    public static final String EDSU_BM_PGKZ = "ProductGroup";  // Produktgruppenkennzeichen
    public static final String EDSU_BM_BEN = "Description";  // Benennung
    public static final String EDSU_BM_VBEZ = "SalesDescription";  // Verkaufsbezeichnung
    public static final String EDSU_BM_BEM = "Remark";  // Bemerkung
    public static final String EDSU_BM_TEDAT = "TechnicalData";  // Technische Daten
    private static final String EDSU_BM_UNG_KZ_BIS = "VersionToInvalidFlag";  // Ungültig-Kennzeichen (kommt nicht vor)
    private static final String EDSU_BM_AS_BIS = "VersionTo";  // Änderungsstand Bis
    private static final String EDSU_BM_AGRKZ = "MajorComponent"; // kommt nicht vor

    private static final Map<String, String> EXTERNAL_TEXT_MAPPING = new HashMap<>();

    static {
        EXTERNAL_TEXT_MAPPING.put(EDSU_BM_BEN, FIELD_DM_NAME);
        EXTERNAL_TEXT_MAPPING.put(EDSU_BM_BEM, FIELD_DM_COMMENT);
        EXTERNAL_TEXT_MAPPING.put(EDSU_BM_TEDAT, FIELD_DM_TECHDATA);
        EXTERNAL_TEXT_MAPPING.put(EDSU_BM_VBEZ, FIELD_DM_SALES_TITLE);
    }

    private HashMap<String, String> noBOMmapping;
    private HashMap<String, String> eldasMapping;
    private HashMap<String, iPartsDataModel> handledDataObjects;

    public EDSModelUpdateImporter(EtkProject project) {
        super(project, "!!EDS Baumusterstamm Update (T43RBM)", TABLE_DA_MODEL, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_MODEL, "!!EDS/BCS Baumuster Update Stammdatei", false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        // Standard Mapping für neuen oder BOM-DB Datensatz
        mapping.put(FIELD_DM_MODEL_NO, EDSU_BM_SNR);
        mapping.put(FIELD_DM_NAME, EDSU_BM_BEN); // Entwicklungsbezeichnung = Benennung?
        mapping.put(FIELD_DM_DEVELOPMENT_TITLE, EDSU_BM_BEN);
        mapping.put(FIELD_DM_SALES_TITLE, EDSU_BM_VBEZ);
        mapping.put(FIELD_DM_PRODUCT_GRP, EDSU_BM_PGKZ);
        mapping.put(FIELD_DM_MODEL_TYPE, EDSU_BM_AGRKZ);
        mapping.put(FIELD_DM_AS_FROM, EDSU_BM_AS_AB);
        mapping.put(FIELD_DM_COMMENT, EDSU_BM_BEM);
        mapping.put(FIELD_DM_TECHDATA, EDSU_BM_TEDAT);

        // Mapping für MAD-Datensätze
        noBOMmapping = new HashMap<>();
        noBOMmapping.put(FIELD_DM_AS_FROM, EDSU_BM_AS_AB);
        noBOMmapping.put(FIELD_DM_COMMENT, EDSU_BM_BEM);
        noBOMmapping.put(FIELD_DM_TECHDATA, EDSU_BM_TEDAT);

        // Mapping für ELDAS (TAL40) Datensätze (Erweiterung von noBOMmapping)
        eldasMapping = new HashMap<>(noBOMmapping);
        eldasMapping.put(FIELD_DM_NAME, EDSU_BM_BEN);
        eldasMapping.put(FIELD_DM_DEVELOPMENT_TITLE, EDSU_BM_BEN);
        eldasMapping.put(FIELD_DM_PRODUCT_GRP, EDSU_BM_PGKZ);
        eldasMapping.put(FIELD_DM_MODEL_TYPE, EDSU_BM_AGRKZ);

        // in XML sind Tags, die keinen Wert besitzen, nicht vorhanden
        allXMLElements.add(EDSU_BM_SNR);
        allXMLElements.add(EDSU_BM_UNG_KZ_BIS);
        allXMLElements.add(EDSU_BM_AS_AB);
        allXMLElements.add(EDSU_BM_AS_BIS);
        allXMLElements.add(EDSU_BM_PGKZ);
        allXMLElements.add(EDSU_BM_BEN);
        allXMLElements.add(EDSU_BM_VBEZ);
        allXMLElements.add(EDSU_BM_BEM);
        allXMLElements.add(EDSU_BM_TEDAT);
        allXMLElements.add(EDSU_BM_AGRKZ);
        allXMLElements.add(EDSU_BM_VAKZ_AB);
        allXMLElements.add(EDSU_BM_VAKZ_BIS);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ EDSU_BM_SNR };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        handledDataObjects = new HashMap<>();
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EDSModelUpdateImportHelper helper = new EDSModelUpdateImportHelper(getProject(), getMapping(), getDestinationTable());
        String modelNo = helper.handleValueOfSpecialField(EDSU_BM_SNR, importRec);
        if (StrUtils.isEmpty(modelNo)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (leere Baumuster Nummer)",
                                                        String.valueOf(recordNo)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        iPartsDataModel dataModel = handledDataObjects.get(modelNo);
        EDSModelUpdateImportHelper tempHelper = helper;
        if (dataModel == null) {
            // Model war noch nicht in der Importdatei
            iPartsModelId modelId = new iPartsModelId(modelNo);
            dataModel = new iPartsDataModel(getProject(), modelId);
            // Check, ob das Baumuster schon in der DB existiert
            if (!dataModel.existsInDB()) {
                // neues Baumuster
                dataModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                // Die Importdatenquelle auf BOM-DB setzen
                dataModel.setFieldValue(iPartsConst.FIELD_DM_SOURCE, iPartsImportDataOrigin.EDS.getOrigin(), DBActionOrigin.FROM_EDIT);
            }
            handledDataObjects.put(modelNo, dataModel);
        } else {
            // wurde bereits gezählt und ist somit doppelt
            reduceRecordCount();
        }

        // Model bereits vorhanden
        if (!dataModel.getFieldValue(FIELD_DM_SOURCE).equals(iPartsImportDataOrigin.EDS.getOrigin())) {
            // es handelt sich um ein Model NICHT aus dem BOM-DB Baumusterstamm-Importer
            // => nur zusätzliche Werte aus der BOM-DB füllen
            if (dataModel.getFieldValue(FIELD_DM_SOURCE).equals(iPartsImportDataOrigin.ELDAS.getOrigin())) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 ELDAS-Baumuster \"%2\" gefüllt",
                                                            String.valueOf(recordNo), dataModel.getAsId().getModelNumber()),
                                            MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                tempHelper = new EDSModelUpdateImportHelper(getProject(), eldasMapping, getDestinationTable());
            } else {
                tempHelper = new EDSModelUpdateImportHelper(getProject(), noBOMmapping, getDestinationTable());
            }
        }

        tempHelper.fillBOMDataObject(importRec, dataModel);
        // Texte setzen, die von außen hinzugefügt wurden
        setExternalTexts(dataModel, EXTERNAL_TEXT_MAPPING);
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                getMessageLog().fireProgress(0, handledDataObjects.size(), "", true, false);
                int counter = 0;
                for (Map.Entry<String, iPartsDataModel> entry : handledDataObjects.entrySet()) {
                    saveToDB(entry.getValue());
                    getMessageLog().fireProgress(counter++, handledDataObjects.size(), "", true, true);
                }
                getMessageLog().hideProgress();
            }
        }
        super.postImportTask();
    }

    private class EDSModelUpdateImportHelper extends EDSModelImportHelper {

        public EDSModelUpdateImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName, EDSU_BM_AGRKZ, EDSU_BM_VAKZ_AB, EDSU_BM_VAKZ_BIS, EDSU_BM_AS_AB, EDSU_BM_AS_BIS, EDSU_BM_UNG_KZ_BIS);
        }

        @Override
        protected void importValue(EtkDataObject dataObject, String importFieldName, String dbDestFieldName, String value, Language langDef) {
            // null Values sind zugelassen
            if ((value == null) && getAllXMLElements().contains(importFieldName)) {
                value = "";
            }
            super.importValue(dataObject, importFieldName, dbDestFieldName, value, langDef);
        }

    }
}
