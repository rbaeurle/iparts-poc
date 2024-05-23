package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst;

import de.docware.util.misc.id.IdWithType;

public class iPartsWorkOrderSupplierId extends IdWithType {

    public static final String TYPE = "DA_iPartsWorkOrderSupplierId";

    protected enum INDEX {SUPPLIER_ID, SUPPLIER_SHORTNAME, SUPPLIER_NAME}

    /**
     * Der normale Konstruktor
     *
     * @param supplierId
     * @param supplierShortName
     * @param supplierName
     */
    public iPartsWorkOrderSupplierId(String supplierId, String supplierShortName, String supplierName) {
        super(TYPE, new String[]{ supplierId, supplierShortName, supplierName });
    }

    public String getSupplierId() {
        return id[INDEX.SUPPLIER_ID.ordinal()];
    }

    public String getSupplierShortName() {
        return id[INDEX.SUPPLIER_SHORTNAME.ordinal()];
    }

    public String getSupplierName() {
        return id[INDEX.SUPPLIER_NAME.ordinal()];
    }

}
