/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.List;

public class iPartsDataMailboxRecipient extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DMT_GUID, FIELD_DMT_USER_ID, FIELD_DMT_GROUP_ID,
                                                       FIELD_DMT_ORGANISATION_ID, FIELD_DMT_ROLE_ID };

    public static iPartsDataMailboxRecipient getMailboxRecipient(List<EtkDataObject> dataObjects) {
        if (dataObjects != null) {
            for (EtkDataObject dataObject : dataObjects) {
                if (dataObject instanceof iPartsDataMailboxRecipient) {
                    return (iPartsDataMailboxRecipient)(dataObject);
                }
            }
        }
        return null;
    }

    public iPartsDataMailboxRecipient(EtkProject project, iPartsMailboxRecipientId id) {
        super(KEYS);
        tableName = TABLE_DA_MESSAGE_TO;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataMailboxRecipient cloneMe(EtkProject project) {
        iPartsDataMailboxRecipient clone = new iPartsDataMailboxRecipient(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsMailboxRecipientId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4]);
    }

    @Override
    public iPartsMailboxRecipientId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsMailboxRecipientId)id;
    }

    public String getMsgID() {
        return getAsId().getMsgGUID();
    }

    public String getUserID() {
        return getAsId().getUserID();
    }

    public String getGroupID() {
        return getAsId().getGroupID();
    }

    public String getOrganisationID() {
        return getAsId().getOrganisationID();
    }

    public String getRoleID() {
        return getAsId().getRoleID();
    }

    public String getReadByUser() {
        return getFieldValue(FIELD_DMT_READ_BY_USER_ID);
    }

    public boolean isMessageRead() {
        return StrUtils.isValid(getReadByUser());
    }

    public void setMessageRead(boolean messageRead) {
        String readBy = "";
        String readDate = "";
        if (messageRead) {
            readBy = iPartsUserAdminDb.getLoginUserName();
            readDate = DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss);
        }
        setFieldValue(FIELD_DMT_READ_BY_USER_ID, readBy, DBActionOrigin.FROM_EDIT);
        setFieldValue(FIELD_DMT_READ_DATE, readDate, DBActionOrigin.FROM_EDIT);
    }
}
