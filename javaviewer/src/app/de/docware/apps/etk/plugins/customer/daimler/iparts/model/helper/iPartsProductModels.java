/*
 * Copyright (c) 2018 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModelsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Cache für die Tabelle DA_PRODUCT_MODELS
 * Speichert die Beziehung Baumuster zu Produkte
 */
public class iPartsProductModels implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsProductModels> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    // Maps für Produkte pro Baumuster
    protected Map<String, Set<String>> productNumbersByModel = new HashMap<>();
    protected Map<String, Set<iPartsDataProductModels>> productModelsByModel = new HashMap<>();

    private iPartsProductModels() {
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsProductModels getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsProductModels.class, "ProductModels", false);
        iPartsProductModels result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsProductModels();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        iPartsDataProductModelsList productModelsList = iPartsDataProductModelsList.loadAllData(project);
        for (iPartsDataProductModels productModels : productModelsList) {
            addDataProductModel(productModels);
        }
    }

    private synchronized void addDataProductModel(final iPartsDataProductModels productModel) {
        String modelNumber = productModel.getAsId().getModelNumber();
        Set<iPartsDataProductModels> productModels = productModelsByModel.get(modelNumber);
        if (productModels == null) {
            productModels = new TreeSet<>(new Comparator<iPartsDataProductModels>() {
                @Override
                public int compare(iPartsDataProductModels o1, iPartsDataProductModels o2) {
                    // Baumuster sichtbar im Produkt hat Vorrang vor Baumuster nicht sichtbar im Produkt
                    int result = -o1.getFieldValue(FIELD_DPM_MODEL_VISIBLE).compareTo(o2.getFieldValue(FIELD_DPM_MODEL_VISIBLE));
                    if (result != 0) {
                        return result;
                    }

                    // Produkt sichtbar hat Vorrang vor Produkt nicht sichtbar (bzw. existiert nicht in der DB, dann ist
                    // isRetailRelevantFromDB() ebenfalls false)
                    String productNumber1 = o1.getAsId().getProductNumber();
                    iPartsProduct product1 = iPartsProduct.getInstance(productModel.getEtkProject(), new iPartsProductId(productNumber1));
                    String productNumber2 = o2.getAsId().getProductNumber();
                    iPartsProduct product2 = iPartsProduct.getInstance(productModel.getEtkProject(), new iPartsProductId(productNumber2));
                    result = -Boolean.valueOf(product1.isRetailRelevantFromDB()).compareTo(Boolean.valueOf(product2.isRetailRelevantFromDB()));
                    if (result != 0) {
                        return result;
                    }

                    // Zuletzt die Produktnummer vergleichen für eine deterministische Reihenfolge
                    return productNumber1.compareTo(productNumber2);
                }
            });
            productModelsByModel.put(modelNumber, productModels);
        }
        productModels.add(productModel);

        // Cache-Eintrag für das Baumuster in productNumbersByModel löschen, damit er beim nächsten Zugriff neu aufgebaut wird
        productNumbersByModel.remove(productModel.getAsId().getModelNumber());
    }

    private void addByModelNumber(EtkProject project, String modelNumber) {
        iPartsDataProductModelsList updateListForModel = iPartsDataProductModelsList.loadDataProductModelsList(project, new iPartsModelId(modelNumber));
        for (iPartsDataProductModels dataProductModels : updateListForModel) {
            addDataProductModel(dataProductModels);
        }
    }

    private void addByProductNumber(EtkProject project, String productNumber) {
        iPartsDataProductModelsList updateListForProduct = iPartsDataProductModelsList.loadDataProductModelsList(project, new iPartsProductId(productNumber));
        for (iPartsDataProductModels dataProductModels : updateListForProduct) {
            addDataProductModel(dataProductModels);
        }
    }

    /**
     * Gibt alle dem Baumuster zugeordneten Produkte als Produktnummer zurück
     *
     * @param modelNumber
     * @return
     */
    public synchronized Set<String> getProductNumbersByModel(String modelNumber) {
        Set<String> products = productNumbersByModel.get(modelNumber);
        if (products == null) {
            // Set der Produktnummern mit korrekter Reihenfolge aus dem Set der sortierten iPartsDataProductModels bestimmen
            products = new LinkedHashSet<>();
            productNumbersByModel.put(modelNumber, products);

            Set<iPartsDataProductModels> dataProductModels = productModelsByModel.get(modelNumber);
            if (dataProductModels != null) {
                for (iPartsDataProductModels dataProductModel : dataProductModels) {
                    products.add(dataProductModel.getAsId().getProductNumber());
                }
            }
        }
        return products;
    }

    /**
     * Gibt alle dem Baumuster zugeordneten Produkte zurück
     *
     * @param project
     * @param modelNumber
     * @return
     */
    public synchronized List<iPartsProduct> getProductsByModel(EtkProject project, String modelNumber) {
        List<iPartsProduct> products = new DwList<>();
        Set<String> productNumbers = getProductNumbersByModel(modelNumber);
        if (productNumbers != null) {
            for (String productNumber : productNumbers) {
                if (StrUtils.isValid(productNumber)) {
                    iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNumber));
                    if (product != null) {
                        products.add(product);
                    }
                }
            }
        }
        return products;
    }

    /**
     * Gibt alle {@link iPartsDataProductModels}-Datensätze für das übergebene Baumuster zurück
     *
     * @param project
     * @param modelNumber
     * @return {@code null} falls es keine Datensätze gibt
     */
    public synchronized Set<iPartsDataProductModels> getProductModelsByModel(EtkProject project, String modelNumber) {
        Set<iPartsDataProductModels> dataProductModelsSet = productModelsByModel.get(modelNumber);
        if ((dataProductModelsSet != null) && !dataProductModelsSet.isEmpty()) {
            Set<iPartsDataProductModels> dataProductModelsSetClone = new LinkedHashSet<>();
            for (iPartsDataProductModels dataProductModels : dataProductModelsSet) {
                dataProductModelsSetClone.add(dataProductModels.cloneMe(project));
            }
            return dataProductModelsSetClone;
        }
        return null;
    }

    /**
     * Gibt den ersten {@link iPartsDataProductModels}-Datensatz für das übergebene Baumuster zurück
     *
     * @param project
     * @param modelNumber
     * @return {@code null} falls es keine Datensätze gibt
     */
    public synchronized iPartsDataProductModels getFirstProductModelsByModel(EtkProject project, String modelNumber) {
        Set<iPartsDataProductModels> dataProductModelsSet = productModelsByModel.get(modelNumber);
        if ((dataProductModelsSet != null) && !dataProductModelsSet.isEmpty()) {
            return dataProductModelsSet.iterator().next().cloneMe(project);
        }
        return null;
    }

    /**
     * Aktualisiert den Cache für alle Baumuster, die dem Produkt zugeordnet sind
     *
     * @param project
     * @param productId
     */
    public synchronized void updateCacheByProduct(EtkProject project, iPartsProductId productId) {
        String productNumber = productId.getProductNumber();
        List<String> detectedModels = new DwList<>();
        for (Map.Entry<String, Set<iPartsDataProductModels>> entry : productModelsByModel.entrySet()) {
            for (iPartsDataProductModels dataProductModel : entry.getValue()) {
                if (dataProductModel.getAsId().getProductNumber().equals(productNumber)) {
                    detectedModels.add(entry.getKey());
                    break;
                }
            }
        }

        for (String detectedModel : detectedModels) {
            productNumbersByModel.remove(detectedModel);
            productModelsByModel.remove(detectedModel);
            addByModelNumber(project, detectedModel);
        }

        addByProductNumber(project, productNumber);
    }

    /**
     * Sammelt alle Baumuster, für die es eine Baumuster-zu-Produkt-Beziehung gibt, in einem TreeSet auf
     *
     * @return
     */
    public synchronized TreeSet<iPartsModelId> getAllModelIdsAsTreeSet() {
        TreeSet<iPartsModelId> allModelIds = new TreeSet<>();
        for (String modelNumber : productModelsByModel.keySet()) {
            allModelIds.add(new iPartsModelId(modelNumber));
        }
        return allModelIds;
    }

    /**
     * Sucht in den gecachten Baumuster-zu-Produkt-Beziehungen nach genau diesem Baumuster und Produkt
     *
     * @param project
     * @param modelNumber
     * @param productNumber
     * @return <code>null</code> wenn es diese Beziehung nicht gibt
     */
    public synchronized iPartsDataProductModels getProductModelsByModelAndProduct(EtkProject project, String modelNumber, String productNumber) {
        Set<iPartsDataProductModels> dataProductModelsSet = productModelsByModel.get(modelNumber);
        if (dataProductModelsSet != null) {
            for (iPartsDataProductModels dataProductModels : dataProductModelsSet) {
                if (dataProductModels.getAsId().getProductNumber().equals(productNumber)) {
                    return dataProductModels.cloneMe(project);
                }
            }
        }
        return null;
    }
}
