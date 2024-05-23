/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataASPLMUserRole}.
 */
public class iPartsDataASPLMUserRoleList extends EtkDataObjectList<iPartsDataASPLMUserRole> implements iPartsConst {

    public static iPartsDataASPLMUserRoleList loadByRoleId(EtkProject project, iPartsASPLMRoleId roleId) {
        iPartsDataASPLMUserRoleList list = new iPartsDataASPLMUserRoleList();
        list.loadByRoleId(project, roleId, DBActionOrigin.FROM_DB);
        return list;
    }

    public void loadByRoleId(EtkProject project, iPartsASPLMRoleId roleId, DBActionOrigin origin) {
        clear(origin);

        String roleGuid = roleId.getRoleGuid();
        String[] whereFields = new String[]{ FIELD_DA_UR_RGUID };
        String[] whereValues = new String[]{ roleGuid };
        searchAndFill(project, TABLE_DA_UM_USER_ROLES, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataASPLMUserRole getNewDataObject(EtkProject project) {
        return new iPartsDataASPLMUserRole(project, null);
    }
}
