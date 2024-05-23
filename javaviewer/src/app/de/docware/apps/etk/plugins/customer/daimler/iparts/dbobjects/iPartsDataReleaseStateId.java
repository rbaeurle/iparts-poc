package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

/*
 * Copyright (c) 2017 Docware GmbH
 */

import de.docware.util.misc.id.IdWithType;

public class iPartsDataReleaseStateId extends IdWithType {

    public static String TYPE = "DA_iPartsDataReleaseStateToFactoryId";

    protected enum INDEX {RELEASE_STATE, FACTORY_NO}

    /**
     * Der normale Konstruktor
     *
     * @param releaseState
     * @param factoryNo
     */
    public iPartsDataReleaseStateId(String releaseState, String factoryNo) {
        super(TYPE, new String[]{ releaseState, factoryNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDataReleaseStateId() {
        this("", "");
    }

    public String getReleaseState() {
        return id[INDEX.RELEASE_STATE.ordinal()];
    }

    public String getFactoryNo() {
        return id[INDEX.FACTORY_NO.ordinal()];
    }

}
