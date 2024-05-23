package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst;

import de.docware.util.misc.id.IdWithType;

public class iPartsWorkOrderTaskId extends IdWithType {

    public static final String TYPE = "DA_iPartsWorkOrderTaskId";

    protected enum INDEX {BST_ID, LFDNR}

    /**
     * Der normale Konstruktor
     *
     * @param bstId
     */
    public iPartsWorkOrderTaskId(String bstId, String lfdNr) {
        super(TYPE, new String[]{ bstId, lfdNr });
    }

    public String getBSTId() {
        return id[INDEX.BST_ID.ordinal()];
    }

    public String getLfdNr() {
        return id[INDEX.LFDNR.ordinal()];
    }
}
