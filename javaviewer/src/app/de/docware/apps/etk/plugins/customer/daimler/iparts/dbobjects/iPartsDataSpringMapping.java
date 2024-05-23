/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_SPRING_MAPPING.
 */
public class iPartsDataSpringMapping extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DSM_ZB_SPRING_LEG };

    public iPartsDataSpringMapping(EtkProject project, iPartsSpringMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_SPRING_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSpringMappingId createId(String... idValues) {
        return new iPartsSpringMappingId(idValues[0]);
    }

    @Override
    public iPartsSpringMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSpringMappingId)id;
    }
}
