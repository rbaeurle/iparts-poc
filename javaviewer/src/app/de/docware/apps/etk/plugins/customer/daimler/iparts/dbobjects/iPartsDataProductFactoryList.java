/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * DataObjectList für die Zuordnung Produkt (=Katalog) zu Werken im After Sales.
 * {@link iPartsDataProductFactory}
 */

public class iPartsDataProductFactoryList extends EtkDataObjectList<iPartsDataProductFactory> implements iPartsConst {

    public iPartsDataProductFactoryList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller Produkte eines Werkes
     *
     * @param project
     * @param factoryNo
     * @return
     */
    public static iPartsDataProductFactoryList loadDataProductFactoryListForFactory(EtkProject project, String factoryNo) {
        iPartsDataProductFactoryList list = new iPartsDataProductFactoryList();
        list.loadDataProductFactoriesForFactoryFromDB(project, factoryNo, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller Werke eines Produktes
     *
     * @param project
     * @param productId
     * @return
     */
    public static iPartsDataProductFactoryList loadDataProductFactoryListForProduct(EtkProject project, iPartsProductId productId) {
        iPartsDataProductFactoryList list = new iPartsDataProductFactoryList();
        list.loadDataProductFactoriesForProductFromDB(project, productId.getProductNumber(), DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataProductFactory} aus der DB für das angegebene Werk in der productFactoryID
     *
     * @param project
     * @param factoryNo
     * @param origin
     */
    private void loadDataProductFactoriesForFactoryFromDB(EtkProject project, String factoryNo, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_PRODUCT_FACTORIES, new String[]{ FIELD_DPF_FACTORY_NO }, new String[]{ factoryNo },
                          new String[]{ FIELD_DPF_PRODUCT_NO }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataProductFactory} aus der DB für das Produkt in der productFactoryID
     *
     * @param project
     * @param productNo
     * @param origin
     */
    private void loadDataProductFactoriesForProductFromDB(EtkProject project, String productNo, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_PRODUCT_FACTORIES, new String[]{ FIELD_DPF_PRODUCT_NO }, new String[]{ productNo },
                          new String[]{ FIELD_DPF_FACTORY_NO }, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataProductFactory getNewDataObject(EtkProject project) {
        return new iPartsDataProductFactory(project, null);
    }
}
