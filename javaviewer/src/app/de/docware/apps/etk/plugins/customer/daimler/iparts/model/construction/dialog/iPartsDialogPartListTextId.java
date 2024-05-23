/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.util.misc.id.IdWithType;

/**
 * Created by suedkamp on 21.08.2015.
 */
public class iPartsDialogPartListTextId extends IdWithType {

    public static final String TYPE = "DA_DialogPosTextId";

    // wg. Schlüssel Info siehe Kommentar zu https://confluence.docware.de/confluence/pages/viewpage.action?pageId=8028162
    private enum INDEX {
        BR, HM, M, SM, POS, PV, WW, ETZ, TEXT_ART, SDATA
    }

    /**
     * Der normale Konstruktor
     */
    public iPartsDialogPartListTextId(String br, String hm, String m, String sm, String pos, String pv, String ww, String etz, String textArt, String sdata) {
        super(TYPE, new String[]{ br, hm, m, sm, pos, pv, ww, etz, textArt, sdata });
    }

    public iPartsDialogPartListTextId(HmMSmId hmMSm, String pos, String pv, String ww, String etz, String textArt, String sdata) {
        this(hmMSm.getSeries(), hmMSm.getHm(), hmMSm.getM(), hmMSm.getSm(), pos, pv, ww, etz, textArt, sdata);
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

    public String getPV() {
        return id[INDEX.PV.ordinal()];
    }

    public String getWW() {
        return id[INDEX.WW.ordinal()];
    }

    public String getEtz() {
        return id[INDEX.ETZ.ordinal()];
    }

    public String getTextArt() {
        return id[INDEX.TEXT_ART.ordinal()];
    }

    public String getSdata() {
        return id[INDEX.SDATA.ordinal()];
    }

    public HmMSmId getHmMSmId() {
        return new HmMSmId(getBR(), getHM(), getM(), getSM());
    }


    @Override
    public String toString() {
        return "(" + getBR() + "," + getHM() + "," + getM() + "," + getSM() + "," + getPos() + "," + getPV() + "," + getWW() + "," + getEtz() + "," + getTextArt() + "," + getSdata() + ")";
    }

    public iPartsDialogPartListTextId getIdWithoutSdata() {
        return new iPartsDialogPartListTextId(getHmMSmId(), getPos(), getPV(), getWW(), getEtz(), getTextArt(), "");
    }


}
