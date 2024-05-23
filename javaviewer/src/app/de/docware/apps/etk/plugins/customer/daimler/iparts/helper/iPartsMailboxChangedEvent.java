/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.project.events.AbstractEtkClusterEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsDataMailboxRecipient;
import de.docware.util.collections.dwlist.DwList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event, um alle Cluster-Knoten über neue oder als (un)gelesen markierte Nachrichten zu informieren.
 * Der Event enthält den Zustand der Nachrichten (neu, gelesen, ungelesen), die Nachrichten-GUID sowie
 * alle Empfänger-Daten, so dass jede Session ermitteln kann ob sie von den Änderungen betroffen ist.
 */
public class iPartsMailboxChangedEvent extends AbstractEtkClusterEvent {

    // Neuer Zustand der Nachrichten
    public enum MailboxItemState {NEW, READ, UNREAD}

    private MailboxItemState mailboxItemState;

    // die Empfängerdaten pro GUID. Aktuell kann es pro Nachricht nur einen Empfänger geben,
    // aber in Zukunft könnte es auch möglich sein, eine Nachricht an mehrere Benutzer zu schicken
    private Map<String, List<iPartsMailboxChangedRecipient>> mailboxItems;

    public iPartsMailboxChangedEvent() {
        mailboxItems = new HashMap<>();
    }

    public iPartsMailboxChangedEvent(MailboxItemState itemState) {
        this();
        this.mailboxItemState = itemState;
    }

    public iPartsMailboxChangedEvent(MailboxItemState itemState, String creationUserId, iPartsDataMailboxRecipient recipient) {
        this(itemState);
        addMailboxRecipient(creationUserId, recipient);
    }

    public MailboxItemState getMailboxItemState() {
        return mailboxItemState;
    }

    public void setMailboxItemState(MailboxItemState mailboxItemState) {
        this.mailboxItemState = mailboxItemState;
    }

    @JsonIgnore
    public void setMailboxItemReadState(boolean isRead) {
        if (isRead) {
            this.mailboxItemState = MailboxItemState.READ;
        } else {
            this.mailboxItemState = MailboxItemState.UNREAD;
        }
    }

    public Map<String, List<iPartsMailboxChangedRecipient>> getMailboxItems() {
        return mailboxItems;
    }

    public void setMailboxItems(Map<String, List<iPartsMailboxChangedRecipient>> mailboxItems) {
        this.mailboxItems = mailboxItems;
    }

    @JsonIgnore
    public void addMailboxRecipient(String messageGuid, String creationUserId, String userId, String organisationId, String roleId,
                                    String virtualUserGroupId) {
        iPartsMailboxChangedRecipient recipient = new iPartsMailboxChangedRecipient();
        recipient.setCreationUserId(creationUserId);
        recipient.setUserId(userId);
        recipient.setOrganisationId(organisationId);
        recipient.setRoleId(roleId);
        recipient.setVirtualUserGroupId(virtualUserGroupId);
        List<iPartsMailboxChangedRecipient> recipients = mailboxItems.get(messageGuid);
        if (recipients == null) {
            recipients = new DwList<>();
            mailboxItems.put(messageGuid, recipients);
        }
        recipients.add(recipient);
    }

    @JsonIgnore
    public void addMailboxRecipient(String creationUserId, iPartsDataMailboxRecipient mailboxRecipient) {
        addMailboxRecipient(mailboxRecipient.getMsgID(), creationUserId, mailboxRecipient.getUserID(),
                            mailboxRecipient.getOrganisationID(), mailboxRecipient.getRoleID(),
                            mailboxRecipient.getGroupID());
    }

    @JsonIgnore
    public void addAllRecipients(Map<String, List<iPartsMailboxChangedRecipient>> allRecipients) {
        for (Map.Entry<String, List<iPartsMailboxChangedRecipient>> entry : allRecipients.entrySet()) {
            List<iPartsMailboxChangedRecipient> recipients = mailboxItems.get(entry.getKey());
            if (recipients == null) {
                recipients = new DwList<>();
                mailboxItems.put(entry.getKey(), recipients);
            }
            recipients.addAll(entry.getValue());
        }
    }

    /**
     * Die Daten für einen Empfänger einer Nachricht.
     */
    public static class iPartsMailboxChangedRecipient {

        private String creationUserId;
        private String userId;
        private String organisationId;
        private String roleId;
        private String virtualUserGroupId;

        public iPartsMailboxChangedRecipient() {
        }

        public String getCreationUserId() {
            return creationUserId;
        }

        public void setCreationUserId(String creationUserId) {
            this.creationUserId = creationUserId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getOrganisationId() {
            return organisationId;
        }

        public void setOrganisationId(String organisationId) {
            this.organisationId = organisationId;
        }

        public String getRoleId() {
            return roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }

        public String getVirtualUserGroupId() {
            return virtualUserGroupId;
        }

        public void setVirtualUserGroupId(String virtualUserGroupId) {
            this.virtualUserGroupId = virtualUserGroupId;
        }
    }
}
