package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert einen ES1 Code aus der Tabelle DA_ES1 im iParts Plug-in.
 */
public class iPartsES1Id extends IdWithType {

    public static String TYPE = "DA_iPartsES1Id";


    protected enum INDEX {ES1_CODE, ES1_FNID}

    /**
     * Der normale Konstruktor
     */
    public iPartsES1Id(String es1Code, String es1FnId) {

        super(TYPE, new String[]{ es1Code, es1FnId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsES1Id() {
        this("", "");
    }

    public String getES1Code() {
        return id[INDEX.ES1_CODE.ordinal()];
    }

    public String getES1FnId() {
        return id[INDEX.ES1_FNID.ordinal()];
    }
}
