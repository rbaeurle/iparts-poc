/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductFactoryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.HashSet;
import java.util.Set;

/**
 * Hilfsklasse mit Convenvience-Methoden in Verbindung mit Werken in iParts.
 */
public class FactoriesHelper {

    private static ObjectInstanceLRUList<Object, Set<String>> validFactoriesCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_PRODUCT,
                                                                                                                iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static void clearCache() {
        validFactoriesCache.clear();
    }

    /**
     * Liefert die gültigen Werke für ein Produkt.
     *
     * @param project
     * @param productId
     * @return
     */
    public static Set<String> getValidFactories(EtkProject project, iPartsProductId productId) {
        if ((productId == null) || !productId.isValidId()) { // Ohne gültige productId gibt es keine gültigen Werke
            return new HashSet<String>(0);
        }

        // Werke zum Produkt ist nicht von ChangeSets abhängig
        String hashKey = "@product_" + productId.getProductNumber();
        Object hash = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), FactoriesHelper.class, hashKey, false);
        Set<String> validFactories = validFactoriesCache.get(hash);
        if (validFactories != null) {
            return validFactories;
        }

        // Gültige Werke für das Produkt bestimmen
        iPartsDataProductFactoryList productFactoryList = iPartsDataProductFactoryList.loadDataProductFactoryListForProduct(project, productId);
        validFactories = new HashSet<String>(productFactoryList.size());
        for (iPartsDataProductFactory dataProductFactory : productFactoryList) {
            validFactories.add(dataProductFactory.getAsId().getFactoryNumber());
        }

        validFactoriesCache.put(hash, validFactories);
        return validFactories;
    }
}
