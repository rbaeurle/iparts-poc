/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataASPLMUserGroup}.
 */
public class iPartsDataASPLMUserGroupList extends EtkDataObjectList<iPartsDataASPLMUserGroup> implements iPartsConst {

    public static iPartsDataASPLMUserGroupList loadByGroupId(EtkProject project, iPartsASPLMGroupId groupId) {
        iPartsDataASPLMUserGroupList list = new iPartsDataASPLMUserGroupList();
        list.loadByGroupId(project, groupId, DBActionOrigin.FROM_DB);
        return list;
    }

    public void loadByGroupId(EtkProject project, iPartsASPLMGroupId groupId, DBActionOrigin origin) {
        clear(origin);

        String groupGuid = groupId.getGroupGuid();
        String[] whereFields = new String[]{ FIELD_DA_UG_GGUID };
        String[] whereValues = new String[]{ groupGuid };
        searchAndFill(project, TABLE_DA_UM_USER_GROUPS, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataASPLMUserGroup getNewDataObject(EtkProject project) {
        return new iPartsDataASPLMUserGroup(project, null);
    }
}
