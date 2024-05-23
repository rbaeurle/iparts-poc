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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_UM_ROLES.
 */
public class iPartsDataASPLMRole extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_R_GUID };

    public iPartsDataASPLMRole(EtkProject project, iPartsASPLMRoleId id) {
        super(KEYS);
        tableName = TABLE_DA_UM_ROLES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsASPLMRoleId createId(String... idValues) {
        return new iPartsASPLMRoleId(idValues[0]);
    }

    @Override
    public iPartsASPLMRoleId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsASPLMRoleId)id;
    }

    // Convenience Method
    public String getRoleId() {
        return getFieldValue(FIELD_DA_R_ID);
    }

    public String getRoleAlias() {
        return getFieldValue(FIELD_DA_R_ALIAS);
    }
}
