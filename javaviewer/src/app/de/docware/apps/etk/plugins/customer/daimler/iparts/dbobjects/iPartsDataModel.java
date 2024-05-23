/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelPropertiesId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_MODEL.
 */
public class iPartsDataModel extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DM_MODEL_NO };

    public iPartsDataModel(EtkProject project, iPartsModelId id) {
        super(KEYS);
        tableName = TABLE_DA_MODEL;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsModelId createId(String... idValues) {
        return new iPartsModelId(idValues[0]);
    }

    @Override
    public iPartsModelId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsModelId)id;
    }

    /**
     * Überprüfung, ob die ID noch in weiteren Tabellen verwendet wird
     * (da i.A. noch nicht bekannt ist, welche Abhängigkeiten es gibt => immer true)
     *
     * @return
     */
    public boolean hasDependencies() {
        boolean result = true;
        return result;
    }

    public String getProductGroup() {
        return getFieldValue(FIELD_DM_PRODUCT_GRP);
    }

    public String getCodes() {
        return getFieldValue(FIELD_DM_CODE);
    }

    public String getAusfuehrungsart() {
        return getFieldValue(FIELD_DM_AA);
    }

    public String getModelType() {
        return getFieldValue(FIELD_DM_MODEL_TYPE);
    }

    public String getRevisionStateFrom() {
        return getFieldValue(FIELD_DM_AS_FROM);
    }

    public boolean isEdited() {
        return getFieldValueAsBoolean(FIELD_DM_MANUAL_CHANGE);
    }

    public String getRelatedConstructionModelNo() {
        return getFieldValue(FIELD_DM_CONST_MODEL_NO).trim();
    }

    public iPartsModelPropertiesId getRelatedConstructionId() {
        return new iPartsModelPropertiesId(getRelatedConstructionModelNo(), getFieldValue(FIELD_DM_DATA).trim());
    }

    public boolean isRelatedToConstruction() {
        return getRelatedConstructionId().isValidId();
    }

    public void setRelatedConstructionId(iPartsModelPropertiesId modelPropertiesId, DBActionOrigin origin) {
        setFieldValue(FIELD_DM_CONST_MODEL_NO, modelPropertiesId.getModelNumber(), origin);
        setFieldValue(FIELD_DM_DATA, modelPropertiesId.getKemFrom(), origin);
    }

}
