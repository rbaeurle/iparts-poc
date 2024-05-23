/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

/**
 * Liste von {@link iPartsDataSupplierPartNoMapping}.
 */
public class iPartsDataSupplierPartNoMappingList extends EtkDataObjectList<iPartsDataSupplierPartNoMapping> implements iPartsConst {

    public iPartsDataSupplierPartNoMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    public static String[] getSortFields() {
        return new String[]{ FIELD_DSPM_PARTNO, FIELD_DSPM_SUPPLIER_PARTNO, FIELD_DSPM_SUPPLIER_NO };
    }

    /**
     * Lädt den kompletten Tabelleninhalt aus DA_SUPPLIER_PARTNO_MAPPING und liefert sie als Liste aller {@link iPartsDataSupplierPartNoMapping}s.
     * Sorteriung: DSPM_PARTNO, DSPM_SUPPLIER_PARTNO, DSPM_SUPPLIER_NO
     *
     * @param project
     * @param descending (true/false) = (absteigend/aufsteigend)e Sortierung
     * @return
     */
    public static iPartsDataSupplierPartNoMappingList loadEntireMapping(EtkProject project, boolean descending) {
        iPartsDataSupplierPartNoMappingList list = new iPartsDataSupplierPartNoMappingList();
        list.loadEntireMappingFromDB(project, DBActionOrigin.FROM_DB, descending);
        return list;
    }

    /**
     * Lädt eine Liste zur Daimler-SNR aus DA_SUPPLIER_PARTNO_MAPPING und liefert sie als Liste aller {@link iPartsDataSupplierPartNoMapping}s.
     *
     * @param project
     * @param partNo
     * @param descending
     * @return
     */
    public static iPartsDataSupplierPartNoMappingList loadMappingForPartNo(EtkProject project, String partNo, boolean descending) {
        iPartsDataSupplierPartNoMappingList list = new iPartsDataSupplierPartNoMappingList();
        list.loadMappingForPartNo(project, partNo, DBActionOrigin.FROM_DB, descending);
        return list;
    }

    /**
     * Lädt eine Liste zur Lieferanten-SNR aus DA_SUPPLIER_PARTNO_MAPPING und liefert sie als Liste aller {@link iPartsDataSupplierPartNoMapping}s.
     *
     * @param project
     * @param supplierPartNo
     * @param descending
     * @return
     */
    public static iPartsDataSupplierPartNoMappingList loadMappingForSupplierPartNo(EtkProject project, String supplierPartNo, boolean descending,
                                                                                   String[] sortFields) {
        iPartsDataSupplierPartNoMappingList list = new iPartsDataSupplierPartNoMappingList();
        list.loadMappingForSupplierPartNo(project, supplierPartNo, DBActionOrigin.FROM_DB, descending, sortFields);
        return list;
    }

    /**
     * Lädt eine Liste zur Lieferantennummer aus DA_SUPPLIER_PARTNO_MAPPING und liefert sie als Liste aller {@link iPartsDataSupplierPartNoMapping}s.
     *
     * @param project
     * @param supplierNo
     * @param descending
     * @return
     */
    public static iPartsDataSupplierPartNoMappingList loadMappingForSupplierNo(EtkProject project, String supplierNo, boolean descending) {
        iPartsDataSupplierPartNoMappingList list = new iPartsDataSupplierPartNoMappingList();
        list.loadMappingForSupplierNo(project, supplierNo, DBActionOrigin.FROM_DB, descending);
        return list;
    }

    /**
     * Lädt eine Liste passend zu den gesetzten Werten der übergebenen ID aus DA_SUPPLIER_PARTNO_MAPPING
     * und liefert sie als Liste aller {@link iPartsDataSupplierPartNoMapping}s.
     *
     * @param project
     * @param mappingId
     * @param descending
     * @return
     */
    public static iPartsDataSupplierPartNoMappingList loadMappingForId(EtkProject project, iPartsSupplierPartNoMappingId mappingId,
                                                                       boolean descending, String[] sortFields) {
        iPartsDataSupplierPartNoMappingList list = new iPartsDataSupplierPartNoMappingList();
        list.loadMappingForId(project, mappingId, DBActionOrigin.FROM_DB, descending, sortFields, false);
        return list;
    }

    /**
     * Der Griff in die Datenbank, hier wird die komplette Tabelle geladen und sortiert.
     *
     * @param project
     * @param origin
     * @param descending
     */
    private void loadEntireMappingFromDB(EtkProject project, DBActionOrigin origin, boolean descending) {
        clear(origin);
        loadMappingFromDB(project, null, null, origin, descending, getSortFields(), false);
    }

    /**
     * @param project
     * @param partNo
     * @param origin
     * @param descending
     */
    private void loadMappingForPartNo(EtkProject project, String partNo, DBActionOrigin origin, boolean descending) {
        iPartsSupplierPartNoMappingId id = new iPartsSupplierPartNoMappingId(partNo, null, null);
        loadMappingForId(project, id, origin, descending, getSortFields(), partNo.contains("*"));
    }


    /**
     * @param project
     * @param supplierPartNo
     * @param origin
     * @param descending
     */
    private void loadMappingForSupplierPartNo(EtkProject project, String supplierPartNo, DBActionOrigin origin, boolean descending,
                                              String[] sortFields) {
        iPartsSupplierPartNoMappingId id = new iPartsSupplierPartNoMappingId(null, supplierPartNo, null);
        loadMappingForId(project, id, origin, descending, sortFields, supplierPartNo.contains("*"));
    }

    /**
     * @param project
     * @param supplierNo
     * @param origin
     * @param descending
     */
    private void loadMappingForSupplierNo(EtkProject project, String supplierNo, DBActionOrigin origin, boolean descending) {
        iPartsSupplierPartNoMappingId id = new iPartsSupplierPartNoMappingId(null, null, supplierNo);
        loadMappingForId(project, id, origin, descending, getSortFields(), supplierNo.contains("*"));
    }

    /**
     * @param project
     * @param mappingId
     * @param origin
     * @param descending
     */
    private void loadMappingForId(EtkProject project, iPartsSupplierPartNoMappingId mappingId, DBActionOrigin origin,
                                  boolean descending, String[] sortFields, boolean withWildCardSearch) {
        clear(origin);
        if ((mappingId == null) || !mappingId.isValidForSearch()) {
            return;
        }
        String[] whereFields = null;
        String[] whereValues = null;
        if (StrUtils.isValid(mappingId.getPartNo())) {
            whereFields = mergeArrays(whereFields, new String[]{ FIELD_DSPM_PARTNO });
            whereValues = mergeArrays(whereValues, new String[]{ mappingId.getPartNo() });
        }
        if (StrUtils.isValid(mappingId.getSupplierPartNo())) {
            // Alle Leerzeichen aus dem Such-String entfernen, und dann im bereinigten DB Feld suchen
            String supplierPartNoPlain = StrUtils.removeCharsFromString(mappingId.getSupplierPartNo(), new char[]{ ' ' });
            if (StrUtils.isValid(supplierPartNoPlain)) {
                whereFields = mergeArrays(whereFields, new String[]{ FIELD_DSPM_SUPPLIER_PARTNO_PLAIN });
                whereValues = mergeArrays(whereValues, new String[]{ supplierPartNoPlain });
            }
        }
        if (StrUtils.isValid(mappingId.getSupplierNo())) {
            whereFields = mergeArrays(whereFields, new String[]{ FIELD_DSPM_SUPPLIER_NO });
            whereValues = mergeArrays(whereValues, new String[]{ mappingId.getSupplierNo() });
        }
        loadMappingFromDB(project, whereFields, whereValues, origin, descending, sortFields, withWildCardSearch);
    }

    private String[] mergeArrays(String[] array1, String[] array2) {
        if (array1 != null) {
            return StrUtils.mergeArrays(array1, array2);
        }
        return array2;
    }

    /**
     * Der Griff in die Datenbank hier wird die komplette Tabelle geladen und sortiert.
     *
     * @param project
     * @param origin
     * @param descending
     * @param sortFields
     * @param wildCardSearch Suche mit Wildcards
     */
    private void loadMappingFromDB(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin,
                                   boolean descending, String[] sortFields, boolean wildCardSearch) {
        if (wildCardSearch) {
            searchWithWildCardsSortAndFill(project, whereFields, whereValues, sortFields, descending,
                                           LoadType.COMPLETE, origin);
        } else {
            searchSortAndFill(project, TABLE_DA_SUPPLIER_PARTNO_MAPPING, whereFields, whereValues, sortFields,
                              LoadType.COMPLETE, descending, origin);
        }
    }

    @Override
    protected iPartsDataSupplierPartNoMapping getNewDataObject(EtkProject project) {
        return new iPartsDataSupplierPartNoMapping(project, null);
    }
}


