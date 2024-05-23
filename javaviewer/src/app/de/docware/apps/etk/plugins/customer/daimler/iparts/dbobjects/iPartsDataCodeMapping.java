/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_CODE_MAPPING.
 */
public class iPartsDataCodeMapping extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCM_CATEGORY, FIELD_DCM_MODEL_TYPE_ID, FIELD_DCM_INITIAL_CODE,
                                                       FIELD_DCM_TARGET_CODE };

    public iPartsDataCodeMapping(EtkProject project, iPartsCodeMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_CODE_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsCodeMappingId createId(String... idValues) {
        return new iPartsCodeMappingId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsCodeMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsCodeMappingId)id;
    }
}
