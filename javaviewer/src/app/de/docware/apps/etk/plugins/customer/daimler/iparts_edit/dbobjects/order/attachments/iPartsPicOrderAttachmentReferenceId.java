/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments;

import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Verknüpfung zwischen Bidlauftrag-ID und Anhang-ID im iParts Plug-in.
 */
public class iPartsPicOrderAttachmentReferenceId extends IdWithType {

    public static String TYPE = "DA_iPartsPOAttachmentReferenceId";

    protected enum INDEX {PICORDER_GUID, ATTACHMENT_GUID}

    /**
     * Der normale Konstruktor
     *
     * @param attachmentGuid
     */
    public iPartsPicOrderAttachmentReferenceId(String picOrderGuid, String attachmentGuid) {
        super(TYPE, new String[]{ picOrderGuid, attachmentGuid });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPicOrderAttachmentReferenceId() {
        this("", "");
    }

    public boolean isValidId() {
        return StrUtils.isValid(getPicOrderGuid(), getAttachmentGuid());
    }

    public String getAttachmentGuid() {
        return id[INDEX.ATTACHMENT_GUID.ordinal()];
    }

    public String getPicOrderGuid() {
        return id[INDEX.PICORDER_GUID.ordinal()];
    }

}
