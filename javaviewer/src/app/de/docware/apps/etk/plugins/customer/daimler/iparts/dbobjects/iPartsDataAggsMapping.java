/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_AGGS_MAPPING.
 */
public class iPartsDataAggsMapping extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DAM_DIALOG_AGG_TYPE };

    public iPartsDataAggsMapping(EtkProject project, iPartsAggsMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_AGGS_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsAggsMappingId createId(String... idValues) {
        return new iPartsAggsMappingId(idValues[0]);
    }

    @Override
    public iPartsAggsMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsAggsMappingId)id;
    }

    public String getMADAggType() {
        return getFieldValue(FIELD_DAM_MAD_AGG_TYPE);
    }
}
