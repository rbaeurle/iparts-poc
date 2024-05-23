/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminCache;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

import java.util.List;

public class iPartsDataMailboxItem extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DMSG_GUID };

    public static iPartsDataMailboxItem getMailboxItem(List<EtkDataObject> dataObjects) {
        if (dataObjects != null) {
            for (EtkDataObject dataObject : dataObjects) {
                if (dataObject instanceof iPartsDataMailboxItem) {
                    return (iPartsDataMailboxItem)(dataObject);
                }
            }
        }
        return null;
    }

    public iPartsDataMailboxItem(EtkProject project, iPartsMailboxItemId id) {
        super(KEYS);
        tableName = TABLE_DA_MESSAGE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataMailboxItem cloneMe(EtkProject project) {
        iPartsDataMailboxItem clone = new iPartsDataMailboxItem(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsMailboxItemId(idValues[0]);
    }

    @Override
    public iPartsMailboxItemId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsMailboxItemId)id;
    }

    public String getMsgGuid() {
        return getAsId().getMsgGuid();
    }

    /**
     * Ist für diese Nachricht eine Antwort möglich?
     *
     * @return
     */
    public boolean isAnswerPossible() {
        if (!iPartsRight.CREATE_MAILBOX_MESSAGES.checkRightInSession()) {
            return false;
        }

        iPartsMailboxHelper.MailboxMessageType messageType = iPartsMailboxHelper.MailboxMessageType.valueOf(getFieldValue(FIELD_DMSG_TYPE));
        return ((messageType == null) || !messageType.isAutomaticallyCreated())
               && (iPartsUserAdminCache.getCacheByUserName(getFieldValue(FIELD_DMSG_CREATION_USER_ID)) != null);
    }

    /**
     * Ist für diese Nachricht das Weiterleiten möglich?
     *
     * @return
     */
    public boolean isForwardPossible() {
        return iPartsRight.CREATE_MAILBOX_MESSAGES.checkRightInSession();
    }

    public boolean isRelevantForResubmission() {
        return true; // Aktuell ist das Wiedervorlagedatum für alle Nachrichtentypen zulässig
    }
}
