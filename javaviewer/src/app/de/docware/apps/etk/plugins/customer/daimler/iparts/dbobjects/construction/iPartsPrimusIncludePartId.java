package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert ein Mitlieferteil einer Primus Ersetzung.
 */
public class iPartsPrimusIncludePartId extends IdWithType {

    public static final String TYPE = "DA_iPartsPrimusIncludePartId";

    protected enum INDEX {PART_NO, INCLUDE_PART_NO}

    public iPartsPrimusIncludePartId(String partNo, String includePartNo) {
        super(TYPE, new String[]{ partNo, includePartNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPrimusIncludePartId() {
        this("", "");
    }

    public String getPartNo() {
        return id[INDEX.PART_NO.ordinal()];
    }

    public String getIncludePartNo() {
        return id[INDEX.INCLUDE_PART_NO.ordinal()];
    }

}
