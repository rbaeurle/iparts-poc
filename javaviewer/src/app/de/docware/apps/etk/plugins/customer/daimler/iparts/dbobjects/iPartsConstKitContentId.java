/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Tabelle DA_CONST_KIT_CONTENT
 */
public class iPartsConstKitContentId extends IdWithType {

    public static final String TYPE = "DA_iPartsConstKitContentId";

    private enum INDEX {PARTNO, POSE, WW, SDA}

    /**
     * Der normale Konstruktor
     *
     * @param partNo
     * @param posE
     * @param ww
     * @param sda
     */
    public iPartsConstKitContentId(String partNo, String posE, String ww, String sda) {
        super(TYPE, new String[]{ partNo, posE, ww, sda });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsConstKitContentId() {
        this("", "", "", "");
    }

    public String getPartNo() {
        return id[INDEX.PARTNO.ordinal()];
    }

    public String getPosE() {
        return id[INDEX.POSE.ordinal()];
    }

    public String getWW() {
        return id[INDEX.WW.ordinal()];
    }

    public String getsSDA() {
        return id[INDEX.SDA.ordinal()];
    }
}
