/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_PRODUCT_MODELS.
 */
public class iPartsDataProductModels extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPM_PRODUCT_NO, FIELD_DPM_MODEL_NO };

    public iPartsDataProductModels(EtkProject project, iPartsProductModelsId id) {
        super(KEYS);
        tableName = TABLE_DA_PRODUCT_MODELS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataProductModels cloneMe(EtkProject project) {
        iPartsDataProductModels clone = new iPartsDataProductModels(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public void initAttributesWithDefaultValues(DBActionOrigin origin) {
        super.initAttributesWithDefaultValues(origin);
        setFieldValueAsBoolean(FIELD_DPM_MODEL_VISIBLE, true, origin);
    }

    @Override
    public iPartsProductModelsId createId(String... idValues) {
        return new iPartsProductModelsId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsProductModelsId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsProductModelsId)id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof iPartsDataProductModels) {
            return getAsId().equals(((iPartsDataProductModels)obj).getAsId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getAsId().hashCode();
    }
}
