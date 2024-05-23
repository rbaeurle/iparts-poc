/*
 * Copyright (c) 2019 Docware GmbH
 *
 * Tabelle [DA_MESSAGE_TO], Id für Nachrichtenpostkorb, die Empfänger und die Quittierungsarten User/Group/Organisation+Role.
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox;

import de.docware.util.misc.id.IdWithType;

public class iPartsMailboxRecipientId extends IdWithType {

    public static String TYPE = "DA_iPartsMailboxRecipientId";

    protected enum INDEX {MSG_GUID, USER_ID, GROUP_ID, ORGANISATION_ID, ROLE_ID}

    public iPartsMailboxRecipientId(String msgGUID, String userID, String groupID, String organisationID, String roleID) {
        super(TYPE, new String[]{ msgGUID, userID, groupID, organisationID, roleID });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsMailboxRecipientId() {
        this("", "", "", "", "");
    }


    public String getMsgGUID() {
        return id[INDEX.MSG_GUID.ordinal()];
    }

    public String getUserID() {
        return id[INDEX.USER_ID.ordinal()];
    }

    public String getGroupID() {
        return id[INDEX.GROUP_ID.ordinal()];
    }

    public String getOrganisationID() {
        return id[INDEX.ORGANISATION_ID.ordinal()];
    }

    public String getRoleID() {
        return id[INDEX.ROLE_ID.ordinal()];
    }

}
