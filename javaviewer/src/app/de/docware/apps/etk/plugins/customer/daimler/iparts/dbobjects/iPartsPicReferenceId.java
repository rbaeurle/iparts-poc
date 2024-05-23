/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert die DASTI Bildreferenz(Tabelle DA_PIC_REFERENCE) im iParts Plug-in.
 */
public class iPartsPicReferenceId extends IdWithType {

    public static String TYPE = "DA_iPartsPicReference";

    protected enum INDEX {REF_ID, REF_DATE}

    /**
     * Der normale Konstruktor
     *
     * @param picReferenceNumber
     */
    public iPartsPicReferenceId(String picReferenceNumber, String picReferenceDate) {
        super(TYPE, new String[]{ picReferenceNumber, picReferenceDate });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPicReferenceId() {
        this("", "");
    }

    public String getPicReferenceNumber() {
        return id[INDEX.REF_ID.ordinal()];
    }

    public String getPicReferenceDate() {
        return id[INDEX.REF_DATE.ordinal()];
    }

}
