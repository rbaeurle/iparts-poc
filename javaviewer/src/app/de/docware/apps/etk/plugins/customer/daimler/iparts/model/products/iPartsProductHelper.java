/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Hilfsklasse für {@link iPartsProduct} und {@link iPartsProductId} in iParts.
 */
public class iPartsProductHelper implements iPartsConst {

    public enum PRODUCTS_FOR_SERIES_MODE {EINPAS_INCLUDING_KG_TU, FILTER_ALL, FILTER_ONE}

    /**
     * Liefert eine Liste aller {@link iPartsProduct}s für die angegebene referenzierte {@link iPartsSeriesId} zurück mit optionaler
     * Filterung auf den angegebenen {@link PRODUCT_STRUCTURING_TYPE} und optionaler Beschränkung auf exakt ein Produkt.
     *
     * @param project
     * @param seriesId
     * @param filterProductStructuringType Bei {@link PRODUCT_STRUCTURING_TYPE#EINPAS} werden EinPAS-Produkte bei {@code mode == FILTER_ONE}
     *                                     bevorzugt, bei fehlenden Treffern aber auch ein KG/TU-Produkt zurückgeliefert.
     *                                     Bei {@code mode == EINPAS_INCLUDING_KG_TU} werden sowohl EinPAS- als auch KG/TU-Produkte
     *                                     zurückgeliefert.
     *                                     Bei {@link PRODUCT_STRUCTURING_TYPE#KG_TU} werden immer nur KG/TU-Produkte zurückgeliefert.
     *                                     Bei {@code null} wird der {@link PRODUCT_STRUCTURING_TYPE} ignoriert und nicht
     *                                     danach gefiltert.
     * @param mode                         Siehe Erklärung zu <i><filterProductStructuringType/i>.
     * @return
     */
    public static List<iPartsProduct> getProductsForSeries(EtkProject project, iPartsSeriesId seriesId, PRODUCT_STRUCTURING_TYPE filterProductStructuringType,
                                                           PRODUCTS_FOR_SERIES_MODE mode) {
        return getProductsForSeriesOrModel(project, seriesId.getSeriesNumber(), false, filterProductStructuringType, mode,
                                           false, "series");
    }

    /**
     * Liefert eine Liste aller {@link iPartsProduct}s für die angegebene {@link iPartsModelId} zurück (auch wenn das Baumuster
     * für das jeweilige Produkt im AS nicht sichtbar ist) mit optionaler Filterung auf den angegebenen {@link PRODUCT_STRUCTURING_TYPE}
     * und optionaler Beschränkung auf exakt ein Produkt.
     *
     * @param project
     * @param modelId
     * @param filterProductStructuringType Bei {@link PRODUCT_STRUCTURING_TYPE#EINPAS} werden EinPAS-Produkte bei {@code mode == FILTER_ONE}
     *                                     bevorzugt, bei fehlenden Treffern aber auch ein KG/TU-Produkt zurückgeliefert.
     *                                     Bei {@code mode == EINPAS_INCLUDING_KG_TU} werden sowohl EinPAS- als auch KG/TU-Produkte
     *                                     zurückgeliefert.
     *                                     Bei {@link PRODUCT_STRUCTURING_TYPE#KG_TU} werden immer nur KG/TU-Produkte zurückgeliefert.
     *                                     Bei {@code null} wird der {@link PRODUCT_STRUCTURING_TYPE} ignoriert und nicht
     *                                     danach gefiltert.
     * @param mode                         Siehe Erklärung zu <i><filterProductStructuringType/i>.
     * @return
     */
    public static List<iPartsProduct> getProductsForModelAndSessionType(EtkProject project, iPartsModelId modelId, PRODUCT_STRUCTURING_TYPE filterProductStructuringType,
                                                                        PRODUCTS_FOR_SERIES_MODE mode) {
        // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster sollen beim Edit aber nicht bei den
        // Webservices berücksichtigt werden (je nach Admin-Option). Über die Session wird geprüft, um welchen Sessiontyp es handelt.
        Session session = Session.get();
        return getProductsForModel(project, modelId, filterProductStructuringType, mode, iPartsPlugin.isCheckModelVisibility()
                                                                                         && ((session == null) || !session.canHandleGui()));
    }

    /**
     * Liefert eine Liste aller {@link iPartsProduct}s für die angegebene {@link iPartsModelId} zurück mit optionaler
     * Filterung auf den angegebenen {@link PRODUCT_STRUCTURING_TYPE} und optionaler Beschränkung auf exakt ein Produkt.
     *
     * @param project
     * @param modelId
     * @param filterProductStructuringType Bei {@link PRODUCT_STRUCTURING_TYPE#EINPAS} werden EinPAS-Produkte bei {@code mode == FILTER_ONE}
     *                                     bevorzugt, bei fehlenden Treffern aber auch ein KG/TU-Produkt zurückgeliefert.
     *                                     Bei {@code mode == EINPAS_INCLUDING_KG_TU} werden sowohl EinPAS- als auch KG/TU-Produkte
     *                                     zurückgeliefert.
     *                                     Bei {@link PRODUCT_STRUCTURING_TYPE#KG_TU} werden immer nur KG/TU-Produkte zurückgeliefert.
     *                                     Bei {@code null} wird der {@link PRODUCT_STRUCTURING_TYPE} ignoriert und nicht
     *                                     danach gefiltert.
     * @param mode                         Siehe Erklärung zu <i><filterProductStructuringType/i>.
     * @param onlyVisibleModels            Sollen nur Produkte zurückgegeben werden, in denen das Baumuster im AS auch wirklich
     *                                     sichtbar ist?
     * @return
     */
    public static List<iPartsProduct> getProductsForModel(EtkProject project, iPartsModelId modelId, PRODUCT_STRUCTURING_TYPE filterProductStructuringType,
                                                          PRODUCTS_FOR_SERIES_MODE mode, boolean onlyVisibleModels) {
        return getProductsForSeriesOrModel(project, modelId.getModelNumber(), true, filterProductStructuringType, mode,
                                           onlyVisibleModels, "model");
    }

    private static List<iPartsProduct> getProductsForSeriesOrModel(EtkProject project, String seriesOrModelNumber, boolean isModelNumber,
                                                                   PRODUCT_STRUCTURING_TYPE filterProductStructuringType,
                                                                   PRODUCTS_FOR_SERIES_MODE mode, boolean onlyVisibleModels,
                                                                   String seriesOrModelLogName) {
        Set<String> productNumbers = new TreeSet<>();
        for (iPartsProduct product : iPartsProduct.getAllProducts(project)) {
            if (isModelNumber) {
                Set<String> modelNumbers;
                if (onlyVisibleModels) {
                    modelNumbers = product.getVisibleModelNumbers(project);
                } else {
                    modelNumbers = product.getModelNumbers(project);
                }
                if (modelNumbers.contains(seriesOrModelNumber)) {
                    productNumbers.add(product.getAsId().getProductNumber());
                    continue;
                }
            } else {
                iPartsSeriesId referencedSeriesId = product.getReferencedSeries();
                if ((referencedSeriesId != null) && referencedSeriesId.getSeriesNumber().equals(seriesOrModelNumber)) {
                    productNumbers.add(product.getAsId().getProductNumber());
                }
            }
        }

        String productNumbersString = "";
        List<iPartsProduct> products = new DwList<>(productNumbers.size());
        boolean foundEinPASProduct = false;
        for (String productNumber : productNumbers) {
            iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNumber));
            if ((filterProductStructuringType == null) || ((filterProductStructuringType == product.getProductStructuringType())
                                                           || (filterProductStructuringType == PRODUCT_STRUCTURING_TYPE.EINPAS))) {
                products.add(product);
                if (product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.EINPAS) {
                    foundEinPASProduct = true;
                }
            }
        }

        List<iPartsProduct> validProducts = new DwList<>(productNumbers.size());
        for (iPartsProduct product : products) {
            if (!productNumbersString.isEmpty()) {
                productNumbersString += ", ";
            }
            productNumbersString += product.getAsId().getProductNumber();

            boolean validProduct = false;
            if (filterProductStructuringType == PRODUCT_STRUCTURING_TYPE.EINPAS) {
                if (mode == PRODUCTS_FOR_SERIES_MODE.EINPAS_INCLUDING_KG_TU) {
                    validProduct = true;
                } else if (mode == PRODUCTS_FOR_SERIES_MODE.FILTER_ALL) {
                    validProduct = product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.EINPAS;
                } else if (validProducts.isEmpty() && (!foundEinPASProduct || (product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.EINPAS))) {
                    validProduct = true;
                }
            } else if ((mode != PRODUCTS_FOR_SERIES_MODE.FILTER_ONE) || validProducts.isEmpty()) { // kein Strukturtypfilter oder KG/TU
                validProduct = true;
            }
            if (validProduct) {
                validProducts.add(product);
            }
        }

        if ((mode == PRODUCTS_FOR_SERIES_MODE.FILTER_ONE) || (filterProductStructuringType != null)) {
            String productStructuringTypeString = "";
            if (filterProductStructuringType != null) {
                productStructuringTypeString = " and structuring type " + filterProductStructuringType.name();
            }
            if (validProducts.isEmpty()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "No product found for " + seriesOrModelLogName
                                                                          + " '" + seriesOrModelNumber + "'" + productStructuringTypeString
                                                                          + " (mode " + mode.name() + "): " + productNumbersString);
            } else if ((mode == PRODUCTS_FOR_SERIES_MODE.FILTER_ONE) && (validProducts.size() > 1)) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "More than one product found for " + seriesOrModelLogName + " '"
                                                                          + seriesOrModelNumber + "'" + productStructuringTypeString
                                                                          + "(mode " + mode.name() + "): " + productNumbersString);
            }
        }

        return validProducts;
    }
}
