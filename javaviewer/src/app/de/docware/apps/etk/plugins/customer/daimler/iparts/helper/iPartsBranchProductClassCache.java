/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataBranchProductClass;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataBranchProductClassList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Cache für die Tabelle DA_BRANCH_PC_MAPPING (Mapping JWT Token Attribut "branch" auf AS-Produktklassen)
 */
public class iPartsBranchProductClassCache {

    private static ObjectInstanceStrongLRUList<Object, iPartsBranchProductClassCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private HashMap<String, Set<String>> branchToProductClasses;

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsBranchProductClassCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsBranchProductClassCache.class,
                                                             "BranchProductClassMapping", false);
        iPartsBranchProductClassCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsBranchProductClassCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        branchToProductClasses = new HashMap<String, Set<String>>();
        iPartsDataBranchProductClassList branchProductClassesList = iPartsDataBranchProductClassList.loadCompleteBranchProductClassMapping(project);
        for (iPartsDataBranchProductClass branchProductClassMapping : branchProductClassesList) {
            String branch = branchProductClassMapping.getAsId().getBranch();
            Set<String> productClasses = branchToProductClasses.get(branch);
            if (productClasses == null) {
                productClasses = new HashSet<String>();
                branchToProductClasses.put(branch, productClasses);
            }
            productClasses.addAll(branchProductClassMapping.getProductClasses());
        }
    }

    /**
     * Überprüft, ob mindestens eine zum <i>branch</i> gemappte AS-Produktklasse in den AS-Produktklassen des übergebenen
     * Produkts enthalten ist.
     *
     * @param branch
     * @param product
     * @return
     */
    public boolean isProductValidForBranch(String branch, iPartsProduct product) {
        if (product == null) {
            return false;
        }
        return isProductClassValidForBranch(branch, product.getAsProductClasses());
    }

    /**
     * Überprüft, ob mindestens eine zum <i>branch</i> gemappte AS-Produktklasse in den übergebenen AS-Produktklassen
     * enthalten ist.
     *
     * @param branch
     * @param productClasses
     * @return
     */
    public boolean isProductClassValidForBranch(String branch, Set<String> productClasses) {
        if (StrUtils.isEmpty(branch) || (productClasses == null) || productClasses.isEmpty()) {
            return false;
        }

        Set<String> mappedProductClasses = branchToProductClasses.get(branch);
        if (mappedProductClasses != null) {
            for (String productClass : productClasses) {
                if (mappedProductClasses.contains(productClass)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Liefert alle Branches zurück.
     *
     * @return
     */
    public Set<String> getAllBranches() {
        return branchToProductClasses.keySet();
    }
}
