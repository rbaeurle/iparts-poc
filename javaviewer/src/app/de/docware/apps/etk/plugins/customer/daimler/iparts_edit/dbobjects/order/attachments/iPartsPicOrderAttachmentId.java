/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Bildauftragsanhang-ID im iParts Plug-in.
 */
public class iPartsPicOrderAttachmentId extends IdWithType {

    public static String TYPE = "DA_iPartsPOAttachmentId";

    protected enum INDEX {ATTACHMENT_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param attachmentGuid
     */
    public iPartsPicOrderAttachmentId(String attachmentGuid) {
        super(TYPE, new String[]{ attachmentGuid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPicOrderAttachmentId() {
        this("");
    }

    /**
     * Liegt eine gültige ID vor (attachmentGuid ist nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getAttachmentGuid().isEmpty();
    }

    public String getAttachmentGuid() {
        return id[INDEX.ATTACHMENT_GUID.ordinal()];
    }

}
