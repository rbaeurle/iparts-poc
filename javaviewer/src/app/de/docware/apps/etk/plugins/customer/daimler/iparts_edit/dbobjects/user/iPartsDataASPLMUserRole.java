/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_UM_USER_ROLES.
 */
public class iPartsDataASPLMUserRole extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_UR_UGUID, FIELD_DA_UR_RGUID };

    public iPartsDataASPLMUserRole(EtkProject project, iPartsASPLMUserRoleId id) {
        super(KEYS);
        tableName = TABLE_DA_UM_USER_ROLES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsASPLMUserRoleId createId(String... idValues) {
        return new iPartsASPLMUserRoleId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsASPLMUserRoleId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsASPLMUserRoleId)id;
    }
}
