package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine MBS Navigationsstruktur-ID aus der Tabelle DA_STRUCTURE_MBS im iParts Plug-in.
 */
public class iPartsMBSStructureId extends IdWithType {

    public static final String STRUCTURE_GUID_DELIMITER = "|";

    public static final String TYPE = "DA_iPartsMBSStructureId";

    protected enum INDEX {SNR, SNR_SUFFIX, POS, SORT, KEM_FROM}

    public iPartsMBSStructureId(String snr, String snrSuffix, String position, String sortValue, String kemFrom) {
        super(TYPE, new String[]{ snr, snrSuffix, position, sortValue, kemFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsMBSStructureId() {
        this("", "", "", "", "");
    }

    public String getSNR() {
        return id[INDEX.SNR.ordinal()];
    }

    public String getSNRSuffix() {
        return id[INDEX.SNR_SUFFIX.ordinal()];
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

    public String createMBSStructureGuid() {
        return toString(STRUCTURE_GUID_DELIMITER);
    }

}
