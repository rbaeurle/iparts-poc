package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

/*
 * Copyright (c) 2017 Docware GmbH
 *
 * Tabelle [DA_DIALOG_DSR], Id für sicherheits- und zertifizierungsrelevante Teile, DSR-Kenner
 */

import de.docware.util.misc.id.IdWithType;

public class iPartsDSRDataId extends IdWithType {

    public static String TYPE = "DA_iPartsDSRDataId";

    protected enum INDEX {MATNR, DSR_TYPE, DSR_NO, SDATA, DSR_MK4, DSR_MK5}

    /**
     * Der normale Konstruktor
     *
     * @param matNr
     * @param dsrType
     * @param dsrNo
     * @param sdata
     * @param dsrMK4
     * @param dsrMK5
     */
    public iPartsDSRDataId(String matNr, String dsrType, String dsrNo, String sdata, String dsrMK4, String dsrMK5) {
        super(TYPE, new String[]{ matNr, dsrType, dsrNo, sdata, dsrMK4, dsrMK5 });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDSRDataId() {
        this("", "", "", "", "", "");
    }

    public String getMatNumber() {
        return id[INDEX.MATNR.ordinal()];
    }

    public String getDsrType() {
        return id[INDEX.DSR_TYPE.ordinal()];
    }

    public String getDsrNo() {
        return id[INDEX.DSR_NO.ordinal()];
    }

    public String getSdata() {
        return id[INDEX.SDATA.ordinal()];
    }

    public String getMk4() {
        return id[INDEX.DSR_MK4.ordinal()];
    }

    public String getMk5() {
        return id[INDEX.DSR_MK5.ordinal()];
    }



/*

    String FIELD_DSR_MATNR = "DSR_MATNR";         // PK
    String FIELD_DSR_TYPE = "DSR_TYPE";           // PK
    String FIELD_DSR_NO = "DSR_NO";               // PK
    String FIELD_DSR_SDATA = "DSR_SDATA";         // PK
    String FIELD_DSR_SDATB = "DSR_SDATB";
    String FIELD_DSR_MK1 = "DSR_MK1";
    String FIELD_DSR_MK2 = "DSR_MK2";
    String FIELD_DSR_MK3 = "DSR_MK3";
    String FIELD_DSR_MK4 = "DSR_MK4";             // PK
    String FIELD_DSR_MK5 = "DSR_MK5";             // PK
    String FIELD_DSR_MK6 = "DSR_MK6";
    String FIELD_DSR_MK7 = "DSR_MK7";
    String FIELD_DSR_MK_TEXT = "DSR_MK_TEXT";

     */

}
