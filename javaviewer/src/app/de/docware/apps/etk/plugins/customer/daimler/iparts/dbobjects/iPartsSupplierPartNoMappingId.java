/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Zuordnung Daimler-Sachnummer zu Lieferantennummer aus SRM aus der Tabelle DA_SUPPLIER_PARTNO_MAPPING im iParts Plug-in.
 */
public class iPartsSupplierPartNoMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsSupplierPartnoMapping";

    // DSM_PARTNO,DSM_SUPPLIER_PARTNO,DSM_SUPPLIER_NO
    protected enum INDEX {PARTNO, SUPPLIER_PARTNO, SUPPLIER_NO}

    /**
     * Der normale Konstruktor
     *
     * @param partNo,         die Daimler-Sachnummer
     * @param supplierPartNo, die Sachnummer des Herstellers
     * @param supplierNo,     die Herstellernummer
     */
    public iPartsSupplierPartNoMappingId(String partNo, String supplierPartNo, String supplierNo) {
        super(TYPE, new String[]{ partNo, supplierPartNo, supplierNo });
    }

    /**
     * Für Stücklisteneintrag basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsSupplierPartNoMappingId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsSupplierPartNoMappingId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    public static iPartsSupplierPartNoMappingId getFromDBString(String dbValue) {
        IdWithType id = IdWithType.fromDBString(iPartsSupplierPartNoMappingId.TYPE, dbValue);
        if (id != null) {
            return new iPartsSupplierPartNoMappingId(id.toStringArrayWithoutType());
        }
        return null;
    }

    public boolean isValidForSearch() {
        return StrUtils.isValid(getPartNo()) || StrUtils.isValid(getSupplierPartNo()) || StrUtils.isValid(getSupplierNo());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSupplierPartNoMappingId() {
        this("", "", "");
    }

    public String getPartNo() {
        return id[INDEX.PARTNO.ordinal()];
    }

    public String getSupplierPartNo() {
        return id[INDEX.SUPPLIER_PARTNO.ordinal()];
    }

    public String getSupplierNo() {
        return id[INDEX.SUPPLIER_NO.ordinal()];
    }
}
