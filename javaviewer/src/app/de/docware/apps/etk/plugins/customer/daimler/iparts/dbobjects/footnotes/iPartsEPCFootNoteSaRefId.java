/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.util.misc.id.IdWithType;

public class iPartsEPCFootNoteSaRefId extends IdWithType {

    public static String TYPE = "DA_iPartsEPCFootNoteSaRefId";

    protected enum INDEX {SA_NO, FN_NO}

    /**
     * Der normale Konstruktor
     *
     * @param saNo
     * @param footNoteNumber
     */
    public iPartsEPCFootNoteSaRefId(String saNo, String footNoteNumber) {
        super(TYPE, new String[]{ saNo, footNoteNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsEPCFootNoteSaRefId() {
        this("", "");
    }

    public String getSaNo() {
        return id[INDEX.SA_NO.ordinal()];
    }

    public String getFootNoteNo() {
        return id[INDEX.FN_NO.ordinal()];
    }

}
