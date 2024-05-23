/*
 * Copyright (c) 2020 Quanos GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataPSKProductVariantList extends EtkDataObjectList<iPartsDataPSKProductVariant> implements iPartsConst {

    public iPartsDataPSKProductVariantList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert alle {@link iPartsDataPSKProductVariant}s aus der Tabelle TABLE_DA_PSK_PRODUCT_VARIANTS
     * sortiert nach siehe: getSortFields()
     *
     * @param project
     * @return
     */
    public static iPartsDataPSKProductVariantList loadAllPSKProductVariants(EtkProject project) {
        iPartsDataPSKProductVariantList list = new iPartsDataPSKProductVariantList();
        list.loadFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPSKProductVariant}s für die übergebene Produkt-ID
     *
     * @param project
     * @param productId
     * @return
     */
    public static iPartsDataPSKProductVariantList loadPSKProductVariants(EtkProject project, iPartsProductId productId) {
        return loadPSKProductVariants(project, productId.getProductNumber());
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPSKProductVariant}s für die übergebene Produktnummer
     *
     * @param project
     * @param productNumber
     * @return
     */
    public static iPartsDataPSKProductVariantList loadPSKProductVariants(EtkProject project, String productNumber) {
        iPartsDataPSKProductVariantList list = new iPartsDataPSKProductVariantList();
        list.loadPSKProductVariantsFromDB(project, productNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Sortierfelder:
     * Prinzipiell sollen alle in dieser Klasse zusammengestellten Ergebnislisten gleich sortiert sein,
     * daher gibt es diese zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DPPV_PRODUCT_NO, FIELD_DPPV_VARIANT_ID };
    }

    /**
     * Lädt alle Daten aus TABLE_DA_PSK_PRODUCT_VARIANTS sortiert
     *
     * @param project Das Projekt
     */
    public void loadFromDB(EtkProject project, DBActionOrigin origin) {
        searchSortAndFill(project, TABLE_DA_PSK_PRODUCT_VARIANTS, null, null, getSortFields(), LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataPSKProductVariant}s aus der DB für das übergebene Produkt
     *
     * @param project
     * @param productNumber
     * @param origin
     */
    private void loadPSKProductVariantsFromDB(EtkProject project, String productNumber, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_PSK_PRODUCT_VARIANTS, new String[]{ FIELD_DPPV_PRODUCT_NO }, new String[]{ productNumber },
                          getSortFields(), LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataPSKProductVariant getNewDataObject(EtkProject project) {
        return new iPartsDataPSKProductVariant(project, null);
    }

}
