/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataASPLMRole}.
 */
public class iPartsDataASPLMRoleList extends EtkDataObjectList<iPartsDataASPLMRole> implements iPartsConst {

    /**
     * Lädt die Liste aller {@link iPartsDataASPLMRole}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataASPLMRoleList loadCompleteRoleList(EtkProject project) {
        iPartsDataASPLMRoleList list = new iPartsDataASPLMRoleList();
        list.loadCompletRolesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Rollen, sortiert nach Rollen-Id
     *
     * @param project
     * @param origin
     */
    public void loadCompletRolesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_UM_ROLES, null, null, new String[]{ FIELD_DA_R_ID }, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataASPLMRole getNewDataObject(EtkProject project) {
        return new iPartsDataASPLMRole(project, null);
    }
}
