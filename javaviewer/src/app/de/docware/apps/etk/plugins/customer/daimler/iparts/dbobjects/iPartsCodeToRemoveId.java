package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 *
 */
public class iPartsCodeToRemoveId extends IdWithType {

    public static String TYPE = "DA_iPartsCodeToRemoveId";


    protected enum INDEX {CODE}

    /**
     * Der normale Konstruktor
     */
    public iPartsCodeToRemoveId(String code) {

        super(TYPE, new String[]{ code });
    }

    public String getCode() {
        return id[INDEX.CODE.ordinal()];
    }
}
