/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataASPLMUser}.
 */
public class iPartsDataASPLMUserList extends EtkDataObjectList<iPartsDataASPLMUser> implements iPartsConst {

    /**
     * Lädt die Liste aller {@link iPartsDataASPLMUser}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataASPLMUserList loadCompleteUserList(EtkProject project) {
        iPartsDataASPLMUserList list = new iPartsDataASPLMUserList();
        list.loadCompleteUsersFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataASPLMUserList loadUsersByGroupId(EtkProject project, iPartsASPLMGroupId groupId) {
        iPartsDataASPLMUserList list = new iPartsDataASPLMUserList();
        list.loadUsersByGroupId(project, groupId, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataASPLMUserList loadUsersByRoleId(EtkProject project, iPartsASPLMRoleId roleId) {
        iPartsDataASPLMUserList list = new iPartsDataASPLMUserList();
        list.loadUsersByRoleId(project, roleId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Users, sortiert nach User-Id
     *
     * @param project
     * @param origin
     */
    public void loadCompleteUsersFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_UM_USERS, null, null, new String[]{ FIELD_DA_U_ID }, LoadType.COMPLETE, origin);
    }

    public void loadUsersByGroupId(EtkProject project, iPartsASPLMGroupId groupId, DBActionOrigin origin) {
        clear(origin);

        iPartsDataASPLMUserGroupList list = iPartsDataASPLMUserGroupList.loadByGroupId(project, groupId);
        for (iPartsDataASPLMUserGroup userGroup : list) {
            iPartsASPLMUserId id = new iPartsASPLMUserId(userGroup.getAsId().getUserGuid());
            iPartsDataASPLMUser user = new iPartsDataASPLMUser(project, id);
            add(user, origin);
        }
    }

    public void loadUsersByRoleId(EtkProject project, iPartsASPLMRoleId roleId, DBActionOrigin origin) {
        clear(origin);

        iPartsDataASPLMUserRoleList list = iPartsDataASPLMUserRoleList.loadByRoleId(project, roleId);
        for (iPartsDataASPLMUserRole userRole : list) {
            iPartsASPLMUserId id = new iPartsASPLMUserId(userRole.getAsId().getUserGuid());
            iPartsDataASPLMUser user = new iPartsDataASPLMUser(project, id);
            add(user, origin);
        }
    }

    @Override
    protected iPartsDataASPLMUser getNewDataObject(EtkProject project) {
        return new iPartsDataASPLMUser(project, null);
    }
}
