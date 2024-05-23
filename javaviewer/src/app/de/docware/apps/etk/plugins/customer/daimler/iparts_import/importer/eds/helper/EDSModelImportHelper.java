/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.EDSAggregateTypes;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper für das Importieren der Baumuster-Stammdaten aus der BOM-DB (T43RBM)
 */
public class EDSModelImportHelper extends EDSImportHelper implements iPartsConst {

    private static Set<String> noModificationFields = new HashSet<String>();

    static {
        noModificationFields.add(FIELD_DM_SALES_TITLE);
        noModificationFields.add(FIELD_DM_NAME);
        noModificationFields.add(FIELD_DM_ADD_TEXT);
        noModificationFields.add(FIELD_DM_VALID_FROM);
        noModificationFields.add(FIELD_DM_VALID_TO);
        noModificationFields.add(FIELD_DM_MODEL_VISIBLE);
    }

    private String modelAggSign;
    private String vakzFrom;
    private String vakzTo;
    private String revFrom;
    private String revTo;
    private String modelInvalidToSign;

    public EDSModelImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName, String modelAggSign,
                                String vakzFrom, String vakzTo, String revFrom, String revTo, String modelInvalidToSign) {
        super(project, mapping, tableName);
        this.modelAggSign = modelAggSign;
        this.vakzFrom = vakzFrom;
        this.vakzTo = vakzTo;
        this.revFrom = revFrom;
        this.revTo = revTo;
        this.modelInvalidToSign = modelInvalidToSign;
    }

    /**
     * Auffüllen der Werte das DataObjects, je nach Mapping im ImportHelper
     *
     * @param importRec
     * @param dataModel
     */
    public void fillBOMDataObject(Map<String, String> importRec, iPartsDataModel dataModel) {
        if (hasHigherOrEqualsVersion(importRec, dataModel.getRevisionStateFrom(), vakzFrom, revFrom)) {
            // es ist ein BOM-DB Baumuster
            fillOverrideCompleteDataForEDSReverse(dataModel, importRec, iPartsEDSLanguageDefs.EDS_DE);
            // Nacharbeiten
            if (StrUtils.isEmpty(handleValueOfSpecialField(vakzTo, importRec))) {
                dataModel.setFieldValueAsBoolean(FIELD_DM_MODEL_INVALID, getInvalidSign(importRec, modelInvalidToSign), DBActionOrigin.FROM_EDIT);
                dataModel.setFieldValue(FIELD_DM_AS_TO, handleValueOfSpecialField(revTo, importRec), DBActionOrigin.FROM_EDIT);
            } else {
                dataModel.setFieldValue(FIELD_DM_AS_TO, EDS_AS_BIS_UNENDLICH, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    @Override
    protected void importValue(EtkDataObject dataObject, String importFieldName, String dbDestFieldName, String value, Language langDef) {
        // null Values sind zugelassen
        if (checkModifiedModelField(dataObject, dbDestFieldName)) {
            return;
        }
        super.importValue(dataObject, importFieldName, dbDestFieldName, value, langDef);
    }

    private boolean checkModifiedModelField(EtkDataObject dataObject, String dbDestFieldName) {
        if (dataObject instanceof iPartsDataModel) {
            iPartsDataModel dataModel = (iPartsDataModel)dataObject;
            if (dataModel.isEdited() && noModificationFields.contains(dbDestFieldName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String handleValueOfSpecialField(String sourceField, String value) {
        if (value == null) {
            value = "";
        }
        value = value.trim();
        if (sourceField.equals(modelAggSign)) {
            // BOM-DB Aggregate-Kennzeichen nach DIALOG Aggregate-Kennzeichen umsetzen
            value = mapEDSValueToDBValue(value.trim());
        }
        return value;
    }

    private String mapEDSValueToDBValue(String value) {
        // BOM-DB Aggregate-Kennzeichen nach DIALOG Aggregate-Kennzeichen umsetzen
        if (!value.isEmpty()) {
            EDSAggregateTypes edsAggType = EDSAggregateTypes.getFromEDSValue(value);
            if (edsAggType != EDSAggregateTypes.UNKNOWN) {
                return edsAggType.getMappedValue();
            }
        }
        return value;
    }
}
