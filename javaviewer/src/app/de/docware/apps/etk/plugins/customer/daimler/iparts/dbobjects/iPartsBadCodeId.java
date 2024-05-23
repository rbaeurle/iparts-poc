/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Tabelle DA_BAD_CODE
 */
public class iPartsBadCodeId extends IdWithType {

    public static String TYPE = "DA_iPartsBadCodeId";


    protected enum INDEX {SERIES_NO, AA, CODE_ID}

    /**
     * Der normale Konstruktor
     */
    public iPartsBadCodeId(String seriesNo, String aa, String codeId) {

        super(TYPE, new String[]{ seriesNo, aa, codeId });
    }

    public String getCodeId() {
        return id[INDEX.CODE_ID.ordinal()];
    }

    public String getAusfuehrungsart() {
        return id[INDEX.AA.ordinal()];
    }

    public String getSeriesNo() {
        return id[INDEX.SERIES_NO.ordinal()];
    }

    public String toStringWithDescription() {
        return "Series: \"" + getSeriesNo() + "\" " +
               "AA: \"" + getAusfuehrungsart() + "\" " +
               "Code: \"" + getCodeId() + "\"";
    }
}
