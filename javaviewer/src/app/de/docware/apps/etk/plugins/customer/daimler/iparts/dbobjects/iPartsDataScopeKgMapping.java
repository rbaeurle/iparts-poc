/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_SCOPE_KG_MAPPING.
 */
public class iPartsDataScopeKgMapping extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DSKM_SCOPE_ID, FIELD_DSKM_KG };

    public iPartsDataScopeKgMapping(EtkProject project, iPartsScopeKgMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_SCOPE_KG_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsScopeKgMappingId createId(String... idValues) {
        return new iPartsScopeKgMappingId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsScopeKgMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsScopeKgMappingId)id;
    }
}
