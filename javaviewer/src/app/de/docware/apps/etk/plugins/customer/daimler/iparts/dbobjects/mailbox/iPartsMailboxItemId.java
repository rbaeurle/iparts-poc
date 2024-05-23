/*
 * Copyright (c) 2019 Docware GmbH
 *
 * Tabelle [DA_MESSAGE], Id für Nachrichtenpostkorb, die Nachrichten an sich.
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox;

import de.docware.util.misc.id.IdWithType;

public class iPartsMailboxItemId extends IdWithType {

    public static String TYPE = "DA_iPartsMailboxItemId";

    protected enum INDEX {MSG_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param msgGuid
     */
    public iPartsMailboxItemId(String msgGuid) {
        super(TYPE, new String[]{ msgGuid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsMailboxItemId() {
        this("");
    }

    public String getMsgGuid() {
        return id[INDEX.MSG_GUID.ordinal()];
    }

}
