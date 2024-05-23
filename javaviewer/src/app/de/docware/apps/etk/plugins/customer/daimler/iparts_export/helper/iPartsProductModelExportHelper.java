/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadExportMessageFileHelper;
import de.docware.framework.modules.db.DBDataSet;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;

import java.util.*;

/**
 * Hilfsklasse um Produkte und Baumuster via PartsList-Webservice durchzurechnen
 */
public class iPartsProductModelExportHelper implements iPartsConst {

    /**
     * Die geänderten sichtbaren Produkte mit den sichtbaren Baumustern bestimmen und je nach Company in den drei Maps
     * speichern.
     *
     * @param project
     * @param includeSpecialProducts
     * @param testModeSmallDataCount
     */
    public static CalculatedVisibleProducts calculateVisibleModifiedProducts(EtkProject project,
                                                                             boolean includeSpecialProducts,
                                                                             int testModeSmallDataCount) {
        Map<iPartsProductId, Set<String>> carProductModelsOrPCMap = new TreeMap<>();
        Map<iPartsProductId, Set<String>> truckProductModelsOrPCMap = new TreeMap<>();
        Map<iPartsProductId, Set<String>> bothCompaniesModelsOrPCMap = new TreeMap<>();

        List<String> selectFields = new ArrayList<>();
        String productField = TableAndFieldName.make(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_NO);
        selectFields.add(productField);
        DBSQLQuery query = project.getDB().getDBForTable(TABLE_DA_PRODUCT).getNewQuery();
        query.select(new Fields(selectFields)).from(TABLE_DA_PRODUCT);
        // Änderungsdatum größer als das Exportdatum und nur sichtbare Produkte
        query.where(new Condition(TableAndFieldName.make(TABLE_DA_PRODUCT, FIELD_DP_MODIFICATION_TIMESTAMP).toLowerCase(),
                                  ">",
                                  new Fields(TableAndFieldName.make(TABLE_DA_PRODUCT, FIELD_DP_ES_EXPORT_TIMESTAMP).toLowerCase())))
                .and(new Condition(TableAndFieldName.make(TABLE_DA_PRODUCT, FIELD_DP_PRODUCT_VISIBLE).toLowerCase(),
                                   Condition.OPERATOR_EQUALS, SQLStringConvert.booleanToPPString(true)));
        query.orderBy(productField);

        try (DBDataSet dbSet = query.executeQuery()) {
            while (dbSet.next()) {
                EtkRecord rec = dbSet.getRecord(selectFields);
                String productNo = rec.getField(productField).getAsString();
                if (StrUtils.isValid(productNo)) {
                    iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNo));
                    fillModelsForProduct(project, product, includeSpecialProducts, testModeSmallDataCount,
                                         carProductModelsOrPCMap, truckProductModelsOrPCMap, bothCompaniesModelsOrPCMap);
                }
                // Falls eine Maximal-Anzahl Produkte gesetzt ist, hier abbrechen, wenn jede Firmenzugehörigkeit genug
                // Produkte hat
                if ((testModeSmallDataCount > 0) && (carProductModelsOrPCMap.size() >= testModeSmallDataCount)
                    && (truckProductModelsOrPCMap.size() >= testModeSmallDataCount)
                    && (bothCompaniesModelsOrPCMap.size() >= testModeSmallDataCount)) {
                    break;
                }
            }
        }
        return new CalculatedVisibleProducts(carProductModelsOrPCMap, truckProductModelsOrPCMap, bothCompaniesModelsOrPCMap);
    }


    /**
     * Die sichtbaren Produkte mit den sichtbaren Baumustern bestimmen und je nach Company in den drei Maps speichern.
     *
     * @param project
     * @param includeSpecialProducts
     * @param testModeSmallDataCount
     */
    public static CalculatedVisibleProducts calculateVisibleProducts(EtkProject project, boolean includeSpecialProducts,
                                                                     int testModeSmallDataCount) {
        Map<iPartsProductId, Set<String>> carProductModelsOrPCMap = new TreeMap<>();
        Map<iPartsProductId, Set<String>> truckProductModelsOrPCMap = new TreeMap<>();
        Map<iPartsProductId, Set<String>> bothCompaniesModelsOrPCMap = new TreeMap<>();
        for (iPartsProduct product : iPartsProduct.getAllProducts(project)) {
            fillModelsForProduct(project, product, includeSpecialProducts, testModeSmallDataCount, carProductModelsOrPCMap,
                                 truckProductModelsOrPCMap, bothCompaniesModelsOrPCMap);
            // Falls eine Maximal-Anzahl Produkte gesetzt ist, hier abbrechen, wenn jede Firmenzugehörigkeit genug
            // Produkte hat
            if ((testModeSmallDataCount > 0) && (carProductModelsOrPCMap.size() >= testModeSmallDataCount)
                && (truckProductModelsOrPCMap.size() >= testModeSmallDataCount)
                && (bothCompaniesModelsOrPCMap.size() >= testModeSmallDataCount)) {
                break;
            }
        }
        return new CalculatedVisibleProducts(carProductModelsOrPCMap, truckProductModelsOrPCMap, bothCompaniesModelsOrPCMap);
    }

    private static void fillModelsForProduct(EtkProject project, iPartsProduct product, boolean includeSpecialProducts,
                                             int testModeSmallDataCount, Map<iPartsProductId, Set<String>> carProductModelsOrPCMap,
                                             Map<iPartsProductId, Set<String>> truckProductModelsOrPCMap,
                                             Map<iPartsProductId, Set<String>> bothCompaniesModelsOrPCMap) {
        if (product.isRetailRelevantFromDB()) {
            Set<String> visibleModelNumbers;

            // Spezial-Behandlung für Spezial-Produkte :)
            if (product.isSpecialCatalog()) {
                if (includeSpecialProducts) {
                    visibleModelNumbers = new TreeSet<>(product.getAsProductClasses()); // AS-Produktklassen wie Baumuster behandeln
                } else {
                    return;
                }
            } else {
                visibleModelNumbers = product.getVisibleModelNumbers(project);
            }

            if (!visibleModelNumbers.isEmpty()) {
                if (product.isCarAndVanProduct() && product.isTruckAndBusProduct()) {
                    // Beide Companies aufsammeln
                    if ((testModeSmallDataCount > 0) && (bothCompaniesModelsOrPCMap.size() >= testModeSmallDataCount)) {
                        // in diesem TestModus nur testModeSmallDataCount Produkte finden
                        return;
                    }
                    bothCompaniesModelsOrPCMap.put(product.getAsId(), visibleModelNumbers);
                } else {
                    if (product.isCarAndVanProduct()) {
                        // MBAG aufsammeln
                        if ((testModeSmallDataCount > 0) && (carProductModelsOrPCMap.size() >= testModeSmallDataCount)) {
                            // in diesem TestModus nur testModeSmallDataCount Produkte finden
                            return;
                        }
                        carProductModelsOrPCMap.put(product.getAsId(), visibleModelNumbers);
                    }
                    if (product.isTruckAndBusProduct()) {
                        // DTAG aufsammeln
                        if ((testModeSmallDataCount > 0) && (truckProductModelsOrPCMap.size() >= testModeSmallDataCount)) {
                            // in diesem TestModus nur testModeSmallDataCount Produkte finden
                            return;
                        }
                        truckProductModelsOrPCMap.put(product.getAsId(), visibleModelNumbers);
                    }
                }
            }
        }
    }

    public static String calculateJsonForModelOrPCPartsLists(iPartsProductId productId, String modelNoOrPC,
                                                             MultiThreadExportMessageFileHelper messageHelper,
                                                             LogChannels logChannel, EtkProject project) {
        try {
            if (iPartsPlugin.isWebservicePluginPresent()) {
                return de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist.iPartsWSPartsListDirectHelper.callWSPartsListDirectJSON(modelNoOrPC,
                                                                                                                                                                  productId,
                                                                                                                                                                  logChannel,
                                                                                                                                                                  project);
            }
        } catch (RuntimeException e) {
            messageHelper.logExceptionWithoutThrowing(e);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public static de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist.iPartsWSPartsListResponse calculateResponseForModelOrPCPartsLists(iPartsProductId productId,
                                                                                                                                                                       String modelNoOrPC,
                                                                                                                                                                       MultiThreadExportMessageFileHelper messageHelper,
                                                                                                                                                                       LogChannels logChannel, EtkProject project) {
        try {
            if (iPartsPlugin.isWebservicePluginPresent()) {
                return de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist.iPartsWSPartsListDirectHelper.callWSPartsListDirectDTO(modelNoOrPC,
                                                                                                                                                                 productId,
                                                                                                                                                                 logChannel,
                                                                                                                                                                 project);
            }
        } catch (RuntimeException e) {
            messageHelper.logExceptionWithoutThrowing(e);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public static int countModelsOrPCs(Map<iPartsProductId, Set<String>> productModelsOrPCMap) {
        int count = 0;
        for (Set<String> modelsOrPCs : productModelsOrPCMap.values()) {
            count += modelsOrPCs.size();
        }
        return count;
    }


    public static class CalculatedVisibleProducts {

        private Map<iPartsProductId, Set<String>> carProductModelsOrPCMap;
        private Map<iPartsProductId, Set<String>> truckProductModelsMapOrPCMap;
        private Map<iPartsProductId, Set<String>> bothCompaniesModelsOrPCMap;

        public CalculatedVisibleProducts(Map<iPartsProductId, Set<String>> carProductModelsOrPCMap,
                                         Map<iPartsProductId, Set<String>> truckProductModelsOrPCMap,
                                         Map<iPartsProductId, Set<String>> bothCompaniesModelsOrPCMap) {
            setCarProductModelsOrPCMap(carProductModelsOrPCMap);
            setTruckProductModelsMapOrPCMap(truckProductModelsOrPCMap);
            setBothCompaniesModelsOrPCMap(bothCompaniesModelsOrPCMap);
        }

        public Map<iPartsProductId, Set<String>> getCarProductModelsOrPCMap() {
            return carProductModelsOrPCMap;
        }

        private void setCarProductModelsOrPCMap(Map<iPartsProductId, Set<String>> carProductModelsOrPCMap) {
            if (carProductModelsOrPCMap == null) {
                this.carProductModelsOrPCMap = new TreeMap<>();
            } else {
                this.carProductModelsOrPCMap = carProductModelsOrPCMap;
            }
        }

        public Map<iPartsProductId, Set<String>> getTruckProductModelsMapOrPCMap() {
            return truckProductModelsMapOrPCMap;
        }

        private void setTruckProductModelsMapOrPCMap(Map<iPartsProductId, Set<String>> truckProductModelsMapOrPCMap) {
            if (truckProductModelsMapOrPCMap == null) {
                this.truckProductModelsMapOrPCMap = new TreeMap<>();
            } else {
                this.truckProductModelsMapOrPCMap = truckProductModelsMapOrPCMap;
            }
        }

        public Map<iPartsProductId, Set<String>> getBothCompaniesModelsOrPCMap() {
            return bothCompaniesModelsOrPCMap;
        }

        private void setBothCompaniesModelsOrPCMap(Map<iPartsProductId, Set<String>> bothCompaniesModelsOrPCMap) {
            if (bothCompaniesModelsOrPCMap == null) {
                this.bothCompaniesModelsOrPCMap = new TreeMap<>();
            } else {
                this.bothCompaniesModelsOrPCMap = bothCompaniesModelsOrPCMap;
            }
        }

        public int getTotalProductCount() {
            return carProductModelsOrPCMap.size() + truckProductModelsMapOrPCMap.size() + bothCompaniesModelsOrPCMap.size();
        }

        public int getTotalModelOrPCCount() {
            return iPartsProductModelExportHelper.countModelsOrPCs(carProductModelsOrPCMap)
                   + iPartsProductModelExportHelper.countModelsOrPCs(truckProductModelsMapOrPCMap)
                   + iPartsProductModelExportHelper.countModelsOrPCs(bothCompaniesModelsOrPCMap);
        }

        /**
         * Ermittelt die Gesamtliste der Produkte.
         *
         * @return
         */
        public Map<iPartsProductId, Set<String>> getMergedProductMaps() {
            Map<iPartsProductId, Set<String>> totalMap = new HashMap<>();
            totalMap.putAll(carProductModelsOrPCMap);
            totalMap.putAll(truckProductModelsMapOrPCMap);
            totalMap.putAll(bothCompaniesModelsOrPCMap);

            return totalMap;
        }

        public int getTruckProductModelsOrPCMapSize() {
            return getTruckProductModelsMapOrPCMap().size();
        }

        public int getCarProductModelsOrPCMapSize() {
            return getCarProductModelsOrPCMap().size();
        }

        public int getBothCompaniesModelsOrPCMapSize() {
            return getBothCompaniesModelsOrPCMap().size();
        }
    }
}
