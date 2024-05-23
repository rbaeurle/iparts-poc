/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_VIN_MODEL_MAPPING.
 */
public class iPartsDataVINModelMapping extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DVM_VIN_PREFIX, FIELD_DVM_MODEL_PREFIX };

    public iPartsDataVINModelMapping(EtkProject project, iPartsVINModelMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_VIN_MODEL_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsVINModelMappingId createId(String... idValues) {
        return new iPartsVINModelMappingId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsVINModelMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsVINModelMappingId)id;
    }
}
