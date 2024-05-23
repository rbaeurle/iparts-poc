/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.util.misc.id.IdWithType;

/**
 * Created by suedkamp on 14.08.2015.
 */
public class iPartsDialogPosTextId extends IdWithType {

    public static final String TYPE = "DA_DialogPosTextId";

    private enum INDEX {BR, HM, M, SM, POS, DATE_FROM}

    /**
     * Der normale Konstruktor
     */
    public iPartsDialogPosTextId(String br, String hm, String m, String sm, String pos, String kemFrom) {
        super(TYPE, new String[]{ br, hm, m, sm, pos, kemFrom });
    }

    public iPartsDialogPosTextId(HmMSmId hmMSm, String pos, String kemFrom) {
        this(hmMSm.getSeries(), hmMSm.getHm(), hmMSm.getM(), hmMSm.getSm(), pos, kemFrom);
    }


    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDialogPosTextId() {
        this("", "", "", "", "", "");
    }

    public String getBR() {
        return id[INDEX.BR.ordinal()];
    }

    public String getHM() {
        return id[INDEX.HM.ordinal()];
    }

    public String getM() {
        return id[INDEX.M.ordinal()];
    }

    public String getSM() {
        return id[INDEX.SM.ordinal()];
    }

    public String getPos() {
        return id[INDEX.POS.ordinal()];
    }

    public String getDateFrom() {
        return id[INDEX.DATE_FROM.ordinal()];
    }

    public HmMSmId getHmMSmId() {
        return new HmMSmId(getBR(), getHM(), getM(), getSM());
    }

    @Override
    public String toString() {
        return "(" + getBR() + "," + getHM() + "," + getM() + "," + getSM() + "," + getPos() + "," + getDateFrom() + ")";
    }

    public iPartsDialogPosTextId getIdWithoutSdata() {
        return new iPartsDialogPosTextId(getHmMSmId(), getPos(), "");
    }

}
