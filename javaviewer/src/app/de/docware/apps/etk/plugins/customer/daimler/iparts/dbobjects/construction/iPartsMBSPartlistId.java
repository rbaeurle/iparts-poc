package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine MBS Stücklisten-ID aus der Tabelle DA_PARTLIST_SAP im iParts Plug-in.
 */
public class iPartsMBSPartlistId extends IdWithType {

    public static final String TYPE = "DA_iPartsMBSPartlistId";

    protected enum INDEX {SNR, POS, SORT, KEM_FROM}

    public iPartsMBSPartlistId(String snr, String position, String sortValue, String kemFrom) {
        super(TYPE, new String[]{ snr, position, sortValue, kemFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsMBSPartlistId() {
        this("", "", "", "");
    }

    public String getUpperNo() {
        return id[INDEX.SNR.ordinal()];
    }

    public String getPosition() {
        return id[INDEX.POS.ordinal()];
    }

    public String getSortValue() {
        return id[INDEX.SORT.ordinal()];
    }

    public String getKemFrom() {
        return id[INDEX.KEM_FROM.ordinal()];
    }

}
