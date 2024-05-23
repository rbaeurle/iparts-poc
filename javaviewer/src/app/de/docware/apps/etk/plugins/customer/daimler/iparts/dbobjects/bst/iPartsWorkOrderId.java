package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst;

import de.docware.util.misc.id.IdWithType;

public class iPartsWorkOrderId extends IdWithType {

    public static final String TYPE = "DA_iPartsWorkOrderId";

    protected enum INDEX {BST_ID}

    /**
     * Der normale Konstruktor
     *
     * @param bstId
     */
    public iPartsWorkOrderId(String bstId) {
        super(TYPE, new String[]{ bstId });
    }

    public String getBSTId() {
        return id[INDEX.BST_ID.ordinal()];
    }
}
