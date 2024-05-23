/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import com.owlike.genson.GenericType;
import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.SortType;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.cache.ObjectInstanceMap;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Repräsentation eines Produkts (Tabelle TABLE_DA_PRODUCT) hauptsächlich für die Produkt-Stammdaten. Die Strukturen werden
 * in {@link iPartsProductStructures} verwaltet. Die Aggregate werden bei Bedarf nachgeladen.
 */
public class iPartsProduct implements CacheForGetCacheDataEvent<iPartsProduct>, EtkDbConst, iPartsConst {

    public static final String SESSION_KEY_PRODUCT_STRUCTURE_WITH_AGGREGATES = "session_product_structure_with_aggregates";
    public static final Set<String> AS_PRODUCT_CLASSES_CAR_AND_VAN = new HashSet<>();
    public static final Set<String> AS_PRODUCT_CLASSES_TRUCK_AND_BUS = new HashSet<>();

    static {
        AS_PRODUCT_CLASSES_CAR_AND_VAN.add(AS_PRODUCT_CLASS_CAR);
        AS_PRODUCT_CLASSES_CAR_AND_VAN.add(AS_PRODUCT_CLASS_TRANSPORTER);
        AS_PRODUCT_CLASSES_CAR_AND_VAN.add(AS_PRODUCT_CLASS_OFF_ROAD_VEHICLE);
        AS_PRODUCT_CLASSES_CAR_AND_VAN.add(AS_PRODUCT_CLASS_SMART);

        AS_PRODUCT_CLASSES_TRUCK_AND_BUS.add(AS_PRODUCT_CLASS_LKW);
        AS_PRODUCT_CLASSES_TRUCK_AND_BUS.add(AS_PRODUCT_CLASS_TRACTOR);
        AS_PRODUCT_CLASSES_TRUCK_AND_BUS.add(AS_PRODUCT_CLASS_BUS);
        AS_PRODUCT_CLASSES_TRUCK_AND_BUS.add(AS_PRODUCT_CLASS_POWERSYSTEMS);
        AS_PRODUCT_CLASSES_TRUCK_AND_BUS.add(AS_PRODUCT_CLASS_UNIMOG);
    }

    private static final ObjectInstanceStrongLRUList<Object, iPartsProduct> productCache = new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_PRODUCT,
                                                                                                                             MAX_CACHE_LIFE_TIME_CORE);

    private static final ObjectInstanceMap<Object, List<iPartsProductId>> allProductIdsCache = new ObjectInstanceMap<>();
    private static final ObjectInstanceMap<Object, List<iPartsProduct>> allProductsCache = new ObjectInstanceMap<>();

    private static final ObjectInstanceLRUList<Object, List<iPartsProductId>> allProductsForReferencedSeriesCache
            = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_SERIES, MAX_CACHE_LIFE_TIME_CORE);

    private static final boolean ENABLE_CASHES_PROVIDER_FOR_SINGLE_PRODUCT = false;

    @JsonProperty
    private iPartsProductId productId;
    @JsonProperty
    private PRODUCT_STRUCTURING_TYPE productStructuringType;
    private volatile EtkMultiSprache productTitle;
    @JsonProperty
    private String productTitleTextNo; //Textnummer für das spätere Nachladen der Texte
    @JsonProperty
    private String pictureName;
    @JsonProperty
    private String productGroup;
    @JsonProperty
    private String aggregateType;
    @JsonProperty
    private boolean specialCatalog;
    @JsonProperty
    private boolean retailRelevant;
    @JsonProperty
    private Set<String> asProductClasses;
    @JsonProperty
    private iPartsDocumentationType documentationType;
    @JsonProperty
    private boolean isPSK;
    @JsonProperty
    private boolean isWireHarnessDataVisible;
    private volatile EtkMultiSprache productRemark;
    @JsonProperty
    private String productRemarkTextNo; //Textnummer für das spätere Nachladen der Texte
    @JsonProperty
    private String productComment;
    @JsonProperty
    private String codeForAutoSelect;
    @JsonProperty
    private List<iPartsIdentRange> identsForAutoSelect;
    @JsonProperty
    private iPartsSeriesId referencedSeries;
    @JsonProperty
    private volatile Set<String> visibleModelNumbers;
    @JsonProperty
    private volatile Set<String> modelNumbers;
    private volatile Set<String> visibleModelTypes; // muss nicht serialisiert werden, weil kann aus visibleModelNumbers schnell berechnet werden
    private volatile Set<String> allModelTypes; // muss nicht serialisiert werden, weil kann aus modelNumbers schnell berechnet werden
    @JsonProperty
    private volatile long minModelsValidFrom;      // kleinstes Gültigkeitsdatum-Ab aller zum Produkt gültigen Baumuster
    @JsonProperty
    private volatile long maxModelsValidTo;        // größtes Gültigkeitsdatum-Bis aller zum Produkt gültigen Baumuster
    @JsonProperty
    private volatile boolean minMaxModelsValidityLoaded;
    private volatile List<iPartsProduct> aggregates; // muss nicht serialisiert werden, weil kann aus modelNumbers schnell berechnet werden
    private volatile List<iPartsProduct> vehicles; // muss nicht serialisiert werden, weil kann aus modelNumbers schnell berechnet werden
    @JsonProperty
    private Set<String> validCountries;
    @JsonProperty
    private Set<String> invalidCountries;
    @JsonProperty
    private Set<String> brands;
    @JsonProperty
    private boolean isIdentOld;
    @JsonProperty
    private String datasetDateString;
    @JsonProperty
    private volatile Set<String> productFactories;
    @JsonProperty
    private volatile Set<String> productVariantIds;
    @JsonProperty
    private volatile Map<String, Set<iPartsModelId>> modelAAMap;
    @JsonProperty
    private boolean ttzFilter; // wenn true wird mit TTZ Datum statt Rückmelde-Idents im Endnummernfilter gefiltert
    @JsonProperty
    private boolean extendedCodeScoringWithModelCodes; // wenn true, dann werden bm-bildende Code beim erweiterten Code-Filter berücksichtigt
    @JsonProperty
    private Set<String> disabledFilters;
    @JsonProperty
    private boolean showLooseSasWhileFiltering;
    @JsonProperty
    private boolean noPrimusHints;
    @JsonProperty
    private boolean useProductionAggregates;
    @JsonProperty
    private boolean useSVGs; // SVGs beim Export verwenden
    @JsonProperty
    private boolean preferSVGs; // SVGs bevorzugen im Autorenprozess (im Zusammenspiel mit den Benutzereinstellungen)
    @JsonProperty
    private String modificationTimestamp;
    @JsonProperty
    private boolean isIdentFactoryFiltering;
    @JsonProperty
    private boolean isFullLanguageSupport;
    @JsonProperty
    private volatile Boolean isAggregateProduct; // Boolean-Objekt ist Absicht, da Lazy-Loading mit Prüfung auf null
    @JsonProperty
    private boolean isCarAndVanProduct;
    @JsonProperty
    private boolean isTruckAndBusProduct;
    @JsonProperty
    private boolean isDoDIALOGInvalidEntriesCheck; // Flag, ob bei der Qualitätsprüfung eines DIALOG TUs die Prüfung auf ungültige Positionen durchgeführt werden soll
    @JsonProperty
    private String productSupplier;
    @JsonProperty
    private boolean useVehicleFactoryNumber;
    @JsonProperty
    private boolean carPerspective;
    @JsonProperty
    private Boolean containsCarPerspectiveTU;

    public static void warmUpCache(EtkProject project) {
        getAllProductIds(project);

        iPartsProductStructures.warmUpCache(project);
    }

    public static void clearCache() {
        synchronized (iPartsProduct.class) {
            productCache.clear();

            clearAllProductsCache();
        }

        iPartsProductStructures.clearCache();
    }

    public static synchronized void clearAllProductsCache() {
        synchronized (allProductsCache) {
            allProductIdsCache.clear();
            allProductsCache.clear();
        }
        allProductsForReferencedSeriesCache.clear();
    }

    private static String getInstanceHashObject(EtkProject project, iPartsProductId productId) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsProduct.class, (productId != null) ? productId.getProductNumber() : null, false);
    }

    public static void removeProductFromCache(EtkProject project, iPartsProductId productId) {
        synchronized (iPartsProduct.class) {
            Object hashObject = getInstanceHashObject(project, productId);
            productCache.removeKey(hashObject);

            // Die Liste ist auch ungültig, weil das Produkt z.B. gelöscht wurde
            clearAllProductsCache();
        }

        iPartsProductStructures.removeProductFromCache(project, productId);
    }

    /**
     * @param project
     * @param productId
     * @param productAttributes null möglich
     * @return
     */
    public static synchronized iPartsProduct getInstance(EtkProject project, iPartsProductId productId, DBDataObjectAttributes productAttributes) {
        Object hashObject = getInstanceHashObject(project, productId);
        iPartsProduct result = productCache.get(hashObject);

        if (result == null) {
            // Beim WarmUp nicht einzeln jedes Produkt per IAC abfragen falls es initial Probleme gab bei der Abfrage aller Produkte
            if (ENABLE_CASHES_PROVIDER_FOR_SINGLE_PRODUCT && (productAttributes == null)) {
                Map<String, String> cacheParameters = new HashMap<>();
                cacheParameters.put(iPartsProductId.TYPE, productId.getProductNumber());
                result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsProduct(), cacheParameters, productCache, hashObject);
                if (result != null) {
                    return result;
                }
            }

            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsProduct(project, productId, productAttributes);
            productCache.put(hashObject, result);
        }

        return result;
    }

    public static iPartsProduct getInstance(EtkProject project, iPartsProductId productId) {
        return getInstance(project, productId, null);
    }

    /**
     * Sollen für die aktuelle {@link Session} bei allen Produkten alle relevanten Aggregate dazugemischt werden?
     *
     * @param productStructureWithAggregates
     */
    public static void setProductStructureWithAggregatesForSession(boolean productStructureWithAggregates) {
        Session.get().setAttribute(SESSION_KEY_PRODUCT_STRUCTURE_WITH_AGGREGATES, productStructureWithAggregates);
    }

    /**
     * Sollen für die aktuelle {@link Session} bei allen Produkten alle relevanten Aggregate dazugemischt werden?
     *
     * @return
     */
    public static boolean isProductStructureWithAggregatesForSession() {
        Object productStructureWithAggregates = Session.get().getAttribute(SESSION_KEY_PRODUCT_STRUCTURE_WITH_AGGREGATES);
        if (productStructureWithAggregates != null) {
            return (Boolean)productStructureWithAggregates;
        } else {
            return false;
        }
    }

    static List<iPartsProductId> getAllProductIds(EtkProject project) {
        synchronized (allProductsCache) {
            Object hashObject = getInstanceHashObject(project, null);
            List<iPartsProductId> allProductsIds;
            allProductsIds = allProductIdsCache.get(hashObject);

            if (allProductsIds == null) {
                // Alle Produkte laden
                // Laden der Daten mittels IAC vom Caches-Provider?
                iPartsProduct dummyProduct = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsProduct(), null, productCache, null);
                if (dummyProduct != null) {
                    synchronized (iPartsProduct.class) {
                        allProductsIds = allProductIdsCache.get(hashObject);
                    }
                    if (allProductsIds != null) {
                        return allProductsIds;
                    }
                }

                synchronized (iPartsProduct.class) {
                    // Noch nicht geladen -> lade alle Produkte aus der Datenbank
                    List<iPartsProduct> allProducts = loadAllProducts(project);
                    allProductsCache.put(hashObject, allProducts);

                    // Und die Ids in den Cache eintragen
                    allProductsIds = new ArrayList<>(allProducts.size());

                    for (iPartsProduct product : allProducts) {
                        allProductsIds.add(product.getAsId());
                    }

                    allProductIdsCache.put(hashObject, allProductsIds);
                }
            }

            return allProductsIds;
        }
    }

    public static List<iPartsProduct> getAllProducts(EtkProject project) {
        List<iPartsProduct> result;
        synchronized (allProductsCache) {
            Object hashObject = getInstanceHashObject(project, null);
            result = allProductsCache.get(hashObject);

            if (result == null) {
                // Alle Produkte laden
                // Laden der Daten mittels IAC vom Caches-Provider?
                iPartsProduct dummyProduct = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsProduct(), null, productCache, null);
                if (dummyProduct != null) {
                    synchronized (iPartsProduct.class) {
                        result = allProductsCache.get(hashObject);
                    }
                }

                if (result == null) {
                    synchronized (iPartsProduct.class) {
                        List<iPartsProductId> allProductsIds = getAllProductIds(project);
                        result = allProductsCache.get(hashObject);
                        if (result == null) { // getAllProductIds() setzt eigentlich schon allProductsCache
                            // Die IDs sind gültig im Cache, lade alle Produkte in die Liste über den instanceCache
                            result = new ArrayList<>(allProductsIds.size());

                            for (iPartsProductId productId : allProductsIds) {
                                iPartsProduct product = getInstance(project, productId);
                                result.add(product);
                            }

                            // Alle Produkte zu den Produkt-IDs ermitteln
                            allProductsCache.put(hashObject, result);
                        }
                    }
                }
            }
        }

        iPartsProductStructures.warmUpCache(project); // Alle Modul-Referenzen von Produkten laden

        return result;
    }

    /**
     * Liefert alle Produkte, die die übergebene Baureihe als "referenzierte Baureihe" haben
     *
     * @param project
     * @param referencedSeries
     * @return
     */
    public static List<iPartsProduct> getAllProductsForReferencedSeries(EtkProject project, iPartsSeriesId referencedSeries) {
        // Liste aller Produkt-IDs für eine referenzierte Baureihe ist NICHT abhängig von ChangeSets
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsProduct.class, referencedSeries.getSeriesNumber(), false);
        List<iPartsProductId> allProductsIdsForReferencedSeries;
        synchronized (iPartsProduct.class) {
            allProductsIdsForReferencedSeries = allProductsForReferencedSeriesCache.get(hashObject);
        }

        if (allProductsIdsForReferencedSeries == null) {
            List<iPartsProduct> result = new DwList<>();

            // Und die Ids in den Cache eintragen
            allProductsIdsForReferencedSeries = new DwList<>();

            for (iPartsProduct product : getAllProducts(project)) {
                iPartsSeriesId seriesInProduct = product.getReferencedSeries();
                if ((seriesInProduct != null) && seriesInProduct.equals(referencedSeries)) {
                    allProductsIdsForReferencedSeries.add(product.getAsId());
                    result.add(product);
                }
            }

            synchronized (iPartsProduct.class) {
                allProductsForReferencedSeriesCache.put(hashObject, allProductsIdsForReferencedSeries);
            }

            return result;
        } else {
            // Die Ids sind gültig im Cache, lade alle Produkte in die Liste über den instanceCache
            List<iPartsProduct> result = new ArrayList<>(allProductsIdsForReferencedSeries.size());

            for (iPartsProductId productId : allProductsIdsForReferencedSeries) {
                iPartsProduct product = getInstance(project, productId);
                result.add(product);
            }
            return result;
        }
    }

    private static List<iPartsProduct> loadAllProducts(EtkProject project) {
        List<iPartsProduct> result = new DwList<>();
        DBDataObjectAttributesList productsAttributesList = project.getDbLayer().getAttributesList(TABLE_DA_PRODUCT);

        productsAttributesList.sort(new String[]{ FIELD_DP_PRODUCT_NO }, new SortType[]{ SortType.AUTOMATIC });
        Map<String, Set<String>> productToVisibleModelTypesSet = new HashMap<>();
        Map<String, Set<String>> productToAllModelTypesSet = new HashMap<>();
        Map<String, Set<String>> productToVisibleModelNumbersSet = new HashMap<>();
        Map<String, Set<String>> productToAllModelNumbersSet = new HashMap<>();
        Map<String, String> modelToAA = new HashMap<>();

        // Typkennzahlen für alle sichtbaren Baumuster pro Produkt bestimmen
        iPartsDataModelList modelList = new iPartsDataModelList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_VISIBLE, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_AA, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_VISIBLE, false, false));
        modelList.searchSortAndFillWithJoin(project, null, selectFields, new String[]{ FIELD_DM_MODEL_NO }, TABLE_DA_PRODUCT_MODELS,
                                            new String[]{ FIELD_DPM_MODEL_NO }, false, false, null, null, false, null, false);
        fillModelNumbersOrModelTypes(productToVisibleModelTypesSet, productToAllModelTypesSet,
                                     productToVisibleModelNumbersSet, productToAllModelNumbersSet,
                                     modelList, FIELD_DPM_PRODUCT_NO);

        for (iPartsDataModel dataModel : modelList) {
            String aa = dataModel.getFieldValue(FIELD_DM_AA);
            modelToAA.put(dataModel.getAsId().getModelNumber(), aa);
        }

        // Alle Produkte erzeugen und Daten zuweisen
        for (DBDataObjectAttributes productAttributes : productsAttributesList) {
            String productNumber = productAttributes.getField(FIELD_DP_PRODUCT_NO).getAsString();
            iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNumber), productAttributes);

            synchronized (product) {
                // (Sichtbare) Typkennzahlen zuweisen
                product.visibleModelTypes = productToVisibleModelTypesSet.get(productNumber);
                product.allModelTypes = productToAllModelTypesSet.get(productNumber);

                // (Sichtbare) Baumuster zuweisen
                Set<String> visibleModelNumbers = productToVisibleModelNumbersSet.get(productNumber);
                if (visibleModelNumbers == null) { // visibleModelNumbers darf nicht null bleiben
                    visibleModelNumbers = new TreeSet<>();
                }
                product.visibleModelNumbers = visibleModelNumbers;

                Set<String> modelNumbers = productToAllModelNumbersSet.get(productNumber);
                if (modelNumbers == null) { // modelNumbers darf nicht null bleiben
                    modelNumbers = new TreeSet<>();
                }
                product.modelNumbers = modelNumbers;

                // Baumuster pro Ausführungsart bestimmen
                Map<String, Set<iPartsModelId>> newModelAAMap = new HashMap<>();
                for (String modelNumber : modelNumbers) {
                    String aa = modelToAA.get(modelNumber);
                    Set<iPartsModelId> modelIds = newModelAAMap.computeIfAbsent(aa, k -> new HashSet<>());
                    modelIds.add(new iPartsModelId(modelNumber));
                }
                product.modelAAMap = newModelAAMap;
            }

            result.add(product);
        }

        return result;
    }

    private static void fillModelNumbersOrModelTypes(Map<String, Set<String>> productToVisibleModelTypesSet, Map<String, Set<String>> productToAllModelTypesSet,
                                                     Map<String, Set<String>> productToVisibleModelsSet, Map<String, Set<String>> productToAllModelsSet,
                                                     iPartsDataModelList modelList, String productField) {
        for (iPartsDataModel dataModel : modelList) {
            String productNumber = dataModel.getFieldValue(productField);
            String modelNumber = dataModel.getAsId().getModelNumber();
            fillProductMap(productNumber, modelNumber, productToAllModelsSet);

            String modelTypeNumber = dataModel.getAsId().getModelTypeNumber();
            fillProductMap(productNumber, modelTypeNumber, productToAllModelTypesSet);

            // Hier muss zusätzlich DPM_MODEL_VISIBLE mit geprüft werden.
            boolean dpmModelVisible = true;
            if (dataModel.attributeExists(FIELD_DPM_MODEL_VISIBLE)) {
                dpmModelVisible = dataModel.getFieldValueAsBoolean(FIELD_DPM_MODEL_VISIBLE);
            }

            // Abwärtskompatibilität für nicht gefüllte Felder, was "ja" entsprechen soll
            DBDataObjectAttribute modelVisibleField = dataModel.getAttribute(FIELD_DM_MODEL_VISIBLE);
            boolean modelVisible = (modelVisibleField.getAsString().isEmpty() || modelVisibleField.getAsBoolean())
                                   && dpmModelVisible;
            if (modelVisible) {
                fillProductMap(productNumber, modelNumber, productToVisibleModelsSet);
                fillProductMap(productNumber, modelTypeNumber, productToVisibleModelTypesSet);
            }
        }
    }

    private static void fillProductMap(String productNumber, String value, Map<String, Set<String>> productMap) {
        Set<String> productsSet = productMap.computeIfAbsent(productNumber, k -> new TreeSet<>());
        productsSet.add(value);
    }


    public static void fillCacheDataForAllProducts(SetCacheDataEvent setCacheDataEvent, EtkProject project) {
        setCacheDataEvent.setCacheData(getAllProducts(project), new GenericType<List<iPartsProduct>>() {
        });
    }

    public static void setCacheByDataForAllProducts(SetCacheDataEvent setCacheDataEvent, EtkProject project) {
        List<iPartsProduct> productsList = setCacheDataEvent.getCacheData(new GenericType<List<iPartsProduct>>() {
        });
        Map<Object, iPartsProduct> newInstances = new HashMap<>();
        List<iPartsProductId> newAllProductIds = new ArrayList<>(productsList.size());
        for (iPartsProduct product : productsList) {
            String hashObject = getInstanceHashObject(project, product.getAsId());
            newInstances.put(hashObject, product);
            newAllProductIds.add(product.getAsId());
        }
        synchronized (iPartsProduct.class) {
            String hashObjectForAllProducts = getInstanceHashObject(project, null);
            allProductIdsCache.put(hashObjectForAllProducts, newAllProductIds);
            allProductsCache.put(hashObjectForAllProducts, productsList);
            for (Map.Entry<Object, iPartsProduct> newInstance : newInstances.entrySet()) {
                productCache.put(newInstance.getKey(), newInstance.getValue());
            }
            synchronized (productCache) {
                productCache.notifyAll();
            }
        }
    }

    public iPartsProduct() {
        productId = new iPartsProductId();
    }

    /**
     * Erzeugt eine neue Instanz.
     *
     * @param project
     * @param productId
     * @param productAttributes null möglich
     */
    private iPartsProduct(EtkProject project, iPartsProductId productId, DBDataObjectAttributes productAttributes) {
        this.productId = productId;
        loadHeader(project, productAttributes);
    }

    @Override
    public iPartsProduct createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        Map<String, String> cacheParametersMap = setCacheDataEvent.getCacheParametersMap();
        if ((cacheParametersMap != null) && StrUtils.isValid(cacheParametersMap.get(iPartsProductId.TYPE))) {
            return createInstance(setCacheDataEvent, productCache, getInstanceHashObject(project, new iPartsProductId(cacheParametersMap.get(iPartsProductId.TYPE))));
        } else {
            setCacheByDataForAllProducts(setCacheDataEvent, project);
            return new iPartsProduct(); // Dummy-Cache-Eintrag als Bestätigung, dass der Cache vollständig geladen wurde
        }
    }

    public iPartsProductId getAsId() {
        return productId;
    }

    public PRODUCT_STRUCTURING_TYPE getProductStructuringType() {
        return productStructuringType;
    }

    public EtkMultiSprache getProductTitle(EtkProject project) {
        if (productTitle == null) {
            EtkMultiSprache newProductTitle = project.getEtkDbs().getMultiLanguageByTextNr(TableAndFieldName.make(TABLE_DA_PRODUCT,
                                                                                                                  FIELD_DP_TITLE),
                                                                                           productTitleTextNo);
            synchronized (this) {
                if (productTitle == null) {
                    productTitle = newProductTitle;
                }
            }
        }
        return productTitle;
    }

    public EtkMultiSprache getProductRemark(EtkProject project) {
        if (productRemark == null) {
            EtkMultiSprache newProductRemark = project.getEtkDbs().getMultiLanguageByTextNr(TableAndFieldName.make(TABLE_DA_PRODUCT,
                                                                                                                   FIELD_DP_APS_REMARK),
                                                                                            productRemarkTextNo);
            synchronized (this) {
                if (productRemark == null) {
                    productRemark = newProductRemark;
                }
            }
        }
        return productRemark;
    }

    /**
     * Liefert die Produktbemerkung (TAL47S) mit Fallback auf den Produktkommentar (Applikationsliste)
     *
     * @param project
     * @return
     */
    public EtkMultiSprache getProductRemarkWithCommentFallback(EtkProject project) {
        // Check, ob Produktbemerkung aus TAL47S oder der Applikationsliste kommt
        if (StrUtils.isValid(productRemarkTextNo)) {
            return getProductRemark(project);
        } else if (StrUtils.isValid(productComment)) {
            return new EtkMultiSprache(productComment, project.getConfig().getDatabaseLanguages());
        }

        return new EtkMultiSprache();
    }

    public String getPictureName() {
        return pictureName;
    }

    public iPartsSeriesId getReferencedSeries() {
        return referencedSeries;
    }

    public String getProductComment() {
        return productComment;
    }

    /**
     * Liefert alle sichtbaren Baumusternummern für dieses Produkt zurück, d.h. alle sichtbaren After-Sales-Baumusternummern
     * dieses Produkts und bei einem Produkt mit explizit zugewiesenen Baumustern genau diese sichtbaren Baumusternummern.
     *
     * @param project
     * @return
     */
    public Set<String> getVisibleModelNumbers(EtkProject project) {
        loadModelsIfNeeded(project);
        return Collections.unmodifiableSet(visibleModelNumbers);
    }

    /**
     * Liefert alle Baumusternummern für dieses Produkt zurück, d.h. alle After-Sales-Baumusternummern dieses Produkts und
     * bei einem Produkt mit explizit zugewiesenen Baumustern genau diese Baumusternummern.
     *
     * @param project
     * @return
     */
    public Set<String> getModelNumbers(EtkProject project) {
        loadModelsIfNeeded(project);
        return Collections.unmodifiableSet(modelNumbers);
    }

    /**
     * Liefert alle sichtbaren Typkennzahlen für dieses Produkt in einem Set zurück, d.h. die Baureihe bzw. die ersten vier Stellen
     * aller im Retail sichtbaren Baumuster für dieses Produkt (Sachnummernkennbuchstabe + erste drei Ziffern des Baumusters).
     *
     * @param project
     * @return
     */
    public Set<String> getVisibleModelTypes(EtkProject project) {
        if (visibleModelTypes == null) {
            Set<String> newVisibleModelTypes = new TreeSet<>();
            for (String model : getVisibleModelNumbers(project)) {
                newVisibleModelTypes.add(new iPartsModelId(model).getModelTypeNumber());
            }

            synchronized (this) {
                if (visibleModelTypes == null) {
                    visibleModelTypes = newVisibleModelTypes;
                }
            }
        }
        return Collections.unmodifiableSet(visibleModelTypes);
    }

    /**
     * Liefert alle Typkennzahlen für dieses Produkt in einem Set zurück, d.h. die Baureihe bzw. die ersten vier Stellen
     * aller Baumuster für dieses Produkt (Sachnummernkennbuchstabe + erste drei Ziffern des Baumusters).
     *
     * @param project
     * @return
     */
    public Set<String> getAllModelTypes(EtkProject project) {
        if (allModelTypes == null) {
            Set<String> newAllModelTypes = new TreeSet<>();
            for (String model : getModelNumbers(project)) {
                newAllModelTypes.add(new iPartsModelId(model).getModelTypeNumber());
            }

            synchronized (this) {
                if (allModelTypes == null) {
                    allModelTypes = newAllModelTypes;
                }
            }
        }
        return Collections.unmodifiableSet(allModelTypes);
    }

    /**
     * Liefert das kleinste Gültigkeitsdatum-Ab aller zum Produkt gültigen Baumuster.
     *
     * @param project
     * @return
     */
    public long getMinModelsValidFrom(EtkProject project) {
        loadMinMaxModelsValidityIfNeeded(project);
        return minModelsValidFrom;
    }

    /**
     * Liefert das größte Gültigkeitsdatum-Bis aller zum Produkt gültigen Baumuster.
     *
     * @param project
     * @return
     */
    public long getMaxModelsValidTo(EtkProject project) {
        loadMinMaxModelsValidityIfNeeded(project);
        return maxModelsValidTo;
    }

    private void loadMinMaxModelsValidityIfNeeded(EtkProject project) {
        if (!minMaxModelsValidityLoaded) {
            long minModelsValidFromLocal = Long.MAX_VALUE;
            long maxModelsValidToLocal = 0;
            iPartsProductModels productModels = iPartsProductModels.getInstance(project);
            for (String modelNo : getModelNumbers(project)) {
                iPartsDataProductModels dataProductModels = productModels.getProductModelsByModelAndProduct(project, modelNo,
                                                                                                            productId.getProductNumber());
                iPartsModelId modelId = new iPartsModelId(modelNo);
                long currentMinModelsValidFrom = StrUtils.strToLongDef(iPartsProductModelHelper.getValidFromValue(project, dataProductModels, modelId), 0);
                minModelsValidFromLocal = Math.min(minModelsValidFromLocal, currentMinModelsValidFrom);
                long currentMaxModelsValidTo = StrUtils.strToLongDef(iPartsProductModelHelper.getValidToValue(project, dataProductModels, modelId), 0);
                if (currentMaxModelsValidTo == 0) { // unendlich
                    currentMaxModelsValidTo = Long.MAX_VALUE;
                }
                maxModelsValidToLocal = Math.max(maxModelsValidToLocal, currentMaxModelsValidTo);
            }

            synchronized (this) {
                if (!minMaxModelsValidityLoaded) {
                    minModelsValidFrom = minModelsValidFromLocal;
                    maxModelsValidTo = maxModelsValidToLocal;
                    minMaxModelsValidityLoaded = true;
                }
            }
        }
    }

    /**
     * Liefert die referenzierte Baureihe (falls vorhanden) bzw. alle Typkennzahlen für dieses Produkt in einem Set zurück,
     * d.h. die Baureihe bzw. die ersten vier Stellen aller Baumuster für dieses Produkt (Sachnummernkennbuchstabe + erste
     * drei Ziffern des Baumusters).
     *
     * @param project
     * @return
     */
    public Set<String> getReferencedSeriesOrAllModelTypes(EtkProject project) {
        // DIALOG-Baureihe hat Vorrang vor den berechneten Typkennzahlen
        iPartsSeriesId seriesId = getReferencedSeries();
        if (seriesId != null) {
            Set<String> seriesSet = new TreeSet<>();
            seriesSet.add(seriesId.getSeriesNumber());
            return Collections.unmodifiableSet(seriesSet);
        }

        return getAllModelTypes(project);
    }

    /**
     * Liefert den Typkennzahl-Präfix (Sachnummernkennbuchstabe {@code C} oder {@code D}) basierend auf der referenzierten
     * Baureihe bzw. den enthaltenen Baumustern zurück mit Fallback auf die Ermittlung über den Aggregatetyp.
     *
     * @param project
     * @return {@code ""} falls weder eine referenzierte Baureihe noch ein Baumuster noch ein gültiger Aggregatetyp vorhanden sind
     */
    public String getModelTypePrefix(EtkProject project) {
        // Bei Typkennzahl-Präfix zunächst aus der referenzierten Baureihe ermitteln
        iPartsSeriesId seriesId = getReferencedSeries();
        if ((seriesId != null) && !seriesId.getSeriesNumber().isEmpty()) {
            return StrUtils.copySubString(seriesId.getSeriesNumber(), 0, 1);
        }

        // Als ersten Fallback Typkennzahl-Präfix aus dem ersten Baumuster ermitteln
        Set<String> modelTypes = getAllModelTypes(project);
        if (!modelTypes.isEmpty()) {
            return StrUtils.copySubString(modelTypes.iterator().next(), 0, 1);
        }

        // Als letzten Fallback den Typkennzahl-Präfix aus dem Aggregatetyp ermitteln
        if (StrUtils.isValid(aggregateType)) {
            if (aggregateType.equals(AGGREGATE_TYPE_CAR)) {
                return MODEL_NUMBER_PREFIX_CAR;
            } else {
                return MODEL_NUMBER_PREFIX_AGGREGATE;
            }
        }

        return "";
    }

    /**
     * Gegenstück zu {@link #getAggregates(EtkProject)}: Liefert alle Fahrzeug-Produkte, die dieses Aggregat verwendet.
     *
     * @param project
     * @return
     */
    public List<iPartsProduct> getVehicles(EtkProject project) {
        if (vehicles == null) {
            List<iPartsProduct> newVehicles = null;
            // Fahrzeug-Produkte zu einem Aggregate-Produkt bestimmen
            if (isAggregateProduct(project)) {
                if (!getModelNumbers(project).isEmpty()) { // Produkt mit Aggregate-Baumustern
                    Set<iPartsModelId> modelIds = new LinkedHashSet<>();
                    for (String aggregateModel : getModelNumbers(project)) {
                        DBDataObjectAttributesList modelAggsAttributesList = project.getDbLayer().getAttributesList(TABLE_DA_MODELS_AGGS,
                                                                                                                    new String[]{ FIELD_DMA_MODEL_NO },
                                                                                                                    new String[]{ FIELD_DMA_AGGREGATE_NO },
                                                                                                                    new String[]{ aggregateModel });
                        for (DBDataObjectAttributes attributes : modelAggsAttributesList) {
                            String modelNumber = attributes.getField(FIELD_DMA_MODEL_NO).getAsString();
                            modelIds.add(new iPartsModelId(modelNumber));
                        }
                    }

                    newVehicles = new ArrayList<>(modelIds.size());

                    // Produkte der Fahrzeug-Baumuster bestimmen
                    Set<iPartsProductId> modelProductIds = new HashSet<>();
                    for (iPartsModelId modelId : modelIds) {
                        // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster ebenfalls berücksichtigen.
                        List<iPartsProduct> productsForModel = iPartsProductHelper.getProductsForModel(project, modelId,
                                                                                                       getProductStructuringType(),
                                                                                                       iPartsProductHelper.PRODUCTS_FOR_SERIES_MODE.EINPAS_INCLUDING_KG_TU,
                                                                                                       false);
                        for (iPartsProduct modelProduct : productsForModel) {
                            if (!modelProductIds.contains(modelProduct.getAsId())) {
                                modelProductIds.add(modelProduct.getAsId());
                                newVehicles.add(modelProduct);
                            }
                        }
                    }
                }
            }

            if (newVehicles == null) {
                newVehicles = new ArrayList<>(0);
            }

            synchronized (this) {
                if (vehicles == null) {
                    vehicles = newVehicles;
                }
            }
        }

        return Collections.unmodifiableList(vehicles);
    }

    public String getProductGroup() {
        return productGroup;
    }

    public Set<String> getAsProductClasses() {
        return Collections.unmodifiableSet(asProductClasses);
    }

    public List<String> getASProductClassesTitles(EtkProject project, String language, Set<String> asProductClasses) {
        List<String> result = null;
        if (asProductClasses != null) {
            result = new ArrayList<>(asProductClasses.size());
            for (String productClassID : asProductClasses) {
                result.add(project.getEnumText(iPartsConst.ENUM_KEY_ASPRODUCT_CLASS, productClassID, language, true));
            }
        }
        return (result != null) ? Collections.unmodifiableList(result) : null;
    }

    public boolean isSpecialCatalog() {
        return specialCatalog;
    }

    public boolean isTtzFilter() {
        return ttzFilter;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public iPartsDocumentationType getDocumentationType() {
        return documentationType;
    }

    public boolean isPSK() {
        return isPSK;
    }

    public boolean isWireHarnessDataVisible() {
        return isWireHarnessDataVisible;
    }

    /**
     * Überprüft, ob das Produkt zu den Eigenschaften des Benutzers der aktuellen Session passt
     *
     * @return
     */
    public boolean isProductVisibleForUserInSession() {
        return isProductVisibleForUserProperties(iPartsRight.checkCarAndVanInSession(), iPartsRight.checkTruckAndBusInSession(),
                                                 iPartsRight.checkPSKInSession());
    }

    /**
     * Überprüft, ob das Produkt zu den übergebenen Eigenschaften des Benutzers passt
     *
     * @return
     */
    public boolean isProductVisibleForUserProperties(boolean carAndVanInSession, boolean truckAndBusInSession, boolean isPSKUser) {
        if (isPSK()) {
            return isPSKUser;
        }
        if (carAndVanInSession && truckAndBusInSession) {
            return true;
        }
        return iPartsFilterHelper.isProductVisibleForUserProperties(this, carAndVanInSession, truckAndBusInSession);
    }

    /**
     * Liefert zurück, ob es sich um ein PKW/Van Produkt handelt
     *
     * @return
     */
    public boolean isCarAndVanProduct() {
        return isCarAndVanProduct;
    }

    /**
     * Liefert zurück, ob es sich um ein Truck/Bus Produkt handelt
     *
     * @return
     */
    public boolean isTruckAndBusProduct() {
        return isTruckAndBusProduct;
    }

    /**
     * Setter für die Flags, ob es sich um ein PKW/Van und/oder Truck/Bus Produkt handelt (ausschließlich für Unittests!).
     *
     * @param isCarAndVanProduct
     * @param isTruckAndBusProduct
     */
    public void __internal_setCarAndTruckProduct(boolean isCarAndVanProduct, boolean isTruckAndBusProduct) {
        this.isCarAndVanProduct = isCarAndVanProduct;
        this.isTruckAndBusProduct = isTruckAndBusProduct;
    }

    /**
     * Ist dieses Produkt insofern Retail-relevant als dass es in der Filterung verwendet wird? Dazu muss es entweder im
     * Retail sichtbar sein oder die aktuelle Session ist eine GUI-Session von iPartsEdit, wo für Testzwecke alle Produkte
     * als sichtbar und damit Filter-relevant gelten.
     *
     * @return
     * @see #isRetailRelevantFromDB()
     */
    public boolean isRetailRelevant() {
        Session session = Session.get();
        return ((session != null) && session.canHandleGui()) || isRetailRelevantFromDB();
    }

    /**
     * Ist dieses Produkt wirklich Retail-relevant unabhängig von der Art der Session? Es wird direkt der Wert aus der DB
     * zurückgeliefert.
     *
     * @return
     * @see #isRetailRelevant()
     */
    public boolean isRetailRelevantFromDB() {
        return retailRelevant;
    }

    /**
     * Überprüft, ob dieses Produkt für die übergebenen Berechtigungen gültig ist, ohne {@link #isRetailRelevant()}
     * zu berücksichtigen
     *
     * @param countryCode      Ländercode
     * @param brandAndBranches Map mit Marken auf Set von Sparten
     * @return
     */
    public boolean isValidForPermissions(EtkProject project, String countryCode, Map<String, Set<String>> brandAndBranches) {
        // Wenn countryCode null ist, keine Ländercode-Überprüfung durchführen
        if (countryCode != null) {
            if (countryCode.isEmpty()) {
                return false;
            }

            // "Nur gültig in Ländern" ist gesetzt -> countryCode muss darin enthalten sein
            if (!getValidCountries().isEmpty() && !getValidCountries().contains(countryCode.toUpperCase())) {
                return false;
            }

            // "Nicht gültig in Ländern" ist gesetzt -> countryCode darf nicht darin enthalten sein
            if (!getInvalidCountries().isEmpty() && getInvalidCountries().contains(countryCode.toUpperCase())) {
                return false;
            }
        }

        // Wenn alle Überprüfungen bestanden, gültig. Ansonsten nicht gültig.
        return checkBrandAndBranches(project, brandAndBranches);
    }

    /**
     * Überprüft eine einzelne AS-Produktklasse, ob sie mit den Permissions aus dem Token angezeigt werden darf.
     *
     * @param project
     * @param productASProductClass
     * @param userPermissions
     * @return (true / false)
     */
    public boolean isASProductClassValidForAssortmentPermissions(EtkProject project, String productASProductClass, Map<String, Set<String>> userPermissions) {
        // Den Cache verwenden
        iPartsDataAssortmentPermissionsMappingCache permissionsMappingCache = iPartsDataAssortmentPermissionsMappingCache.getInstance(project);

        for (Map.Entry<String, Set<String>> userPermission : userPermissions.entrySet()) {
            // 'SMT', 'MB', 'MYB', ...
            String brand = userPermission.getKey();
            // 'PASSENGER-CAR', 'TRUCK', 'UNIMOG', 'VAN', ...
            Set<String> branches = userPermission.getValue();
            for (String branch : branches) {
                String permission = iPartsDataAssortmentPermissionsMappingCache.getPermission(brand, branch);
                Set<String> permittedASProductClasses = permissionsMappingCache.getAsProductClassesForPermission(permission);
                // Bei der ersten Übereinstimmung "passt" zurückgeben.
                if ((permittedASProductClasses != null) && permittedASProductClasses.contains(productASProductClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Überprüft, ob mindestens eine der Relationen Marke zu Sparten (brand und branches) für dieses Produkt gültig ist.
     *
     * @param project
     * @param brandAndBranches Map mit Marken auf Set von Sparten; bei {@code null} ist das Produkt auch gültig
     * @return
     */
    private boolean checkBrandAndBranches(EtkProject project, Map<String, Set<String>> brandAndBranches) {
        if (brandAndBranches == null) {
            return true;
        }

        for (String brand : getBrands()) {
            Set<String> branchesForBrand = brandAndBranches.get(brand);
            if ((branchesForBrand != null) && !branchesForBrand.isEmpty()) {
                iPartsBranchProductClassCache branchProductClassCache = iPartsBranchProductClassCache.getInstance(project);
                for (String branch : branchesForBrand) {
                    // Wenn die zur Sparte (branch) gemappten AS-Produktklassen im Produkt nicht enthalten sind, dann ist es nicht gültig.
                    if (branchProductClassCache.isProductValidForBranch(branch, this)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Liefert alle für dieses Produkt gültigen Marken und Sparten zurück.
     *
     * @return
     */
    public Set<String> getAllValidPermissions(EtkProject project) {
        Set<String> validPermissions = new TreeSet<>();
        // Für die AS-Produktklassen des Produkts alle möglichen Brand zu Branches Kombinationen bestimmen
        Map<String, Set<String>> brandAndBranches = iPartsDataAssortmentPermissionsMappingCache.getInstance(project).getBrandsAndBranchesForASProductClasses(getAsProductClasses());
        // Nur die zurückliefern, die zu den Brands des Produkts passen
        if (Utils.isValid(brandAndBranches)) {
            getBrands().forEach(brand -> {
                Set<String> branches = brandAndBranches.get(brand);
                if (Utils.isValid(branches)) {
                    branches.forEach(branch -> validPermissions.add(iPartsDataAssortmentPermissionsMappingCache.getPermission(brand, branch)));
                }
            });
        }
        return validPermissions;
    }

    public boolean isAggregateProduct(EtkProject project) {
        if (isAggregateProduct == null) {
            synchronized (this) {
                if (isAggregateProduct == null) {
                    boolean isAggregateProductLocal = false;
                    if (!getModelNumbers(project).isEmpty()) {
                        for (String model : getModelNumbers(project)) {
                            if (new iPartsModelId(model).isAggregateModel()) {
                                isAggregateProductLocal = true;
                                break;
                            }
                        }
                    }
                    isAggregateProduct = isAggregateProductLocal;
                }
            }
        }

        return isAggregateProduct;
    }

    /**
     * Handelt es sich um ein allgemeingültiges Produkt (Spezialkatalog oder Aggregate-Produkt)?
     *
     * @param project
     * @return
     */
    public boolean isCommonProduct(EtkProject project) {
        return isSpecialCatalog() || isAggregateProduct(project);
    }

    public String getCodeForAutoSelect() {
        return codeForAutoSelect;
    }

    public List<iPartsIdentRange> getIdentsForAutoSelect() {
        return identsForAutoSelect;
    }

    /**
     * Vergleich, ob in der Liste der Produktklassen des Produkts mindestens eine passende Produktklasse aus der übergebenen Liste enthalten ist.
     * Wenn mindestens eine Übereinstimmung da ist, ist das Ergebnis gültig (true).
     * <p>
     * Abstrakt gesehen nur der Vergleich zweier Listen.
     * (1) Ist die Liste mit den Werten nach denen gesucht werden soll leer, dann soll nach nichts gesucht werden ==> jedes Element passt ==> true.
     * (2) Ist die Liste mit Suchwerten gesetzt, die Listen in der gesucht werden soll aber leer, dann passt kein Element ==> false.
     * (3) Stimmt ein Wert aus der Suchliste mit einem Wert in der zu durchsuchenden Liste überein, passt das Element ==> true.
     * Beim ersten, passenen Wert wird die Suchschleife verlassen.
     *
     * @param searchValues Liste mit den Werten nach denen gesucht wird.
     * @return true/false
     */
    public boolean isOneProductClassValid(Collection<String> searchValues) {
        // Wenn nach nichts gesucht wird, passen alle Werte.
        if ((searchValues == null) || (searchValues.isEmpty())) {
            return true;
        }

        // Wenn Suchwerte vorhanden sind, aber keine Werte, in denen gesucht werden soll, passt das Ergebnis nicht.
        if ((getAsProductClasses() == null) || getAsProductClasses().isEmpty()) {
            return false;
        }

        // So! Nun bleiben zwei gültige Listen übrig, die nun verglichen werden:
        for (String searchValue : searchValues) {
            if (getAsProductClasses().contains(searchValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set mit allen nur gültigen Ländercodes.
     * Falls befüllt, muss {@link #getInvalidCountries()} leer sein.
     *
     * @return
     */
    public Set<String> getValidCountries() {
        return Collections.unmodifiableSet(validCountries);
    }

    /**
     * Set mit allen nicht gültigen Ländercodes.
     * Falls befüllt, muss {@link #getValidCountries()} leer sein.
     *
     * @return
     */
    public Set<String> getInvalidCountries() {
        return Collections.unmodifiableSet(invalidCountries);
    }

    public Set<String> getDisabledFilters() {
        return Collections.unmodifiableSet(disabledFilters);
    }

    public boolean isUseProductionAggregates() {
        return useProductionAggregates;
    }

    /**
     * Sollen in diesem Produkt SVGs verwendet werden können?
     *
     * @return
     */
    public boolean isUseSVGs() {
        return useSVGs;
    }

    /**
     * Sollen in diesem Produkt SVGs bevorzugt werden?
     *
     * @return
     */
    public boolean isPreferSVGs() {
        return preferSVGs;
    }

    public boolean isDoDIALOGInvalidEntriesCheck() {
        return isDoDIALOGInvalidEntriesCheck;
    }

    public String getProductSupplier() {
        return productSupplier;
    }

    public String getProductSupplierText(EtkProject project, String language) {
        String productSupplier = getProductSupplier();
        if (StrUtils.isValid(productSupplier)) {
            productSupplier = project.getEnumText(iPartsConst.ENUM_KEY_SUPPLIER_TRUCK_PRODUCT, productSupplier, language, true);
        }
        return productSupplier;
    }

    public boolean isUseVehicleFactoryNumber() {
        return useVehicleFactoryNumber;
    }

    public boolean isCarPerspective() {
        return carPerspective;
    }

    /**
     * Überprüft, ob ein Produkt für den Navigations-TU freigeschaltet ist und ob dieser TU sichtbar ist.
     *
     * @param project
     * @param isModelCall Direkter Baumustereinstieg?
     * @return Nur {@code true}, wenn {@link #containsCarPerspectiveTU(EtkProject)} ebenfalls {@code true} ist, es sich nicht
     * um einen Baumustereinstieg handelt und der Navigations-TU nicht ausgeblendet ist
     */
    public boolean containsVisibleCarPerspectiveTU(EtkProject project, boolean isModelCall) {
        // Beim Baumuster-Einstieg grundsätzlich FALSE zurückgeben und somit nichts ausgegeben.
        if (isModelCall) {
            return false;
        } else {
            // Prüfung auf "Produkt hat einen Navigations-TU".
            if (containsCarPerspectiveTU(project)) {
                String moduleNumber = EditModuleHelper.buildCarPerspectiveKVari(getAsId().getProductNumber());
                AssemblyId assemblyId = new AssemblyId(moduleNumber, "");
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
                if (assembly instanceof iPartsDataAssembly) {
                    iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                    // Nur erfolgreich, wenn am Navigationsmodul "TU ausblenden" NICHT gesetzt ist.
                    if (!iPartsAssembly.getAsId().isVirtual()) {
                        return !iPartsAssembly.getModuleMetaData().getFieldValueAsBoolean(iPartsConst.FIELD_DM_MODULE_HIDDEN);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Überprüft, ob ein Produkt für den Navigations-TU freigeschaltet ist und ob dieser TU "real" existiert
     *
     * @param project
     * @return
     */
    public boolean containsCarPerspectiveTU(EtkProject project) {
        if (containsCarPerspectiveTU == null) {
            containsCarPerspectiveTU = isCarPerspective() && EditModuleHelper.carPerspectiveModuleExists(project, getAsId().getProductNumber());
        }
        return containsCarPerspectiveTU;
    }

    public Set<String> getBrands() {
        return brands;
    }

    public boolean isIdentOld() {
        return isIdentOld;
    }

    private boolean loadHeader(EtkProject project, DBDataObjectAttributes attributes) {
        if (attributes == null) {
            attributes = project.getDbLayer().getAttributes(TABLE_DA_PRODUCT, new String[]{ FIELD_DP_PRODUCT_NO },
                                                            new String[]{ productId.getProductNumber() });
        }

        if (attributes != null) {
            try {
                productStructuringType = PRODUCT_STRUCTURING_TYPE.valueOf(attributes.getField(FIELD_DP_STRUCTURING_TYPE).getAsString());
            } catch (IllegalArgumentException e) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Wrong enum value for DA_PRODUCT.DP_STRUCTURING_TYPE (ProductId: "
                                                                          + productId.getProductNumber() + "): "
                                                                          + attributes.getField(FIELD_DP_STRUCTURING_TYPE).getAsString());
                productStructuringType = PRODUCT_STRUCTURING_TYPE.EINPAS;
            }

            productTitleTextNo = attributes.getField(FIELD_DP_TITLE).getAsString();

            pictureName = attributes.getField(FIELD_DP_PICTURE).getAsString();
            productGroup = attributes.getField(FIELD_DP_PRODUCT_GRP).getAsString();
            aggregateType = attributes.getField(FIELD_DP_AGGREGATE_TYPE).getAsString();
            specialCatalog = attributes.getField(FIELD_DP_IS_SPECIAL_CAT).getAsBoolean();
            retailRelevant = attributes.getField(iPartsConst.FIELD_DP_PRODUCT_VISIBLE).getAsBoolean();
            asProductClasses = new LinkedHashSet<>(SetOfEnumDataType.parseSetofEnum(attributes.getField(FIELD_DP_ASPRODUCT_CLASSES).getAsString(),
                                                                                    false, false));
            String referencedSeriesNumber = attributes.getField(FIELD_DP_SERIES_REF).getAsString();
            if (!referencedSeriesNumber.isEmpty()) {
                referencedSeries = new iPartsSeriesId(referencedSeriesNumber);
            }
            documentationType = iPartsDocumentationType.getFromDBValue(attributes.getField(FIELD_DP_DOCU_METHOD).getAsString());
            isPSK = attributes.getField(FIELD_DP_PSK).getAsBoolean();
            isWireHarnessDataVisible = attributes.getField(FIELD_DP_CONNECT_DATA_VISIBLE).getAsBoolean();
            productRemarkTextNo = attributes.getField(FIELD_DP_APS_REMARK).getAsString();
            productComment = attributes.getField(FIELD_DP_COMMENT).getAsString();
            codeForAutoSelect = attributes.getField(FIELD_DP_APS_CODE).getAsString();
            fillAutoSelectIdentsMap(attributes.getField(FIELD_DP_APS_FROM_IDENTS).getAsString(),
                                    attributes.getField(FIELD_DP_APS_TO_IDENTS).getAsString());
            validCountries = new TreeSet<>(SetOfEnumDataType.parseSetofEnum(attributes.getField(FIELD_DP_VALID_COUNTRIES).getAsString(),
                                                                            false, false));
            invalidCountries = new TreeSet<>(SetOfEnumDataType.parseSetofEnum(attributes.getField(FIELD_DP_INVALID_COUNTRIES).getAsString(),
                                                                              false, false));
            brands = new LinkedHashSet<>(SetOfEnumDataType.parseSetofEnum(attributes.getField(FIELD_DP_BRAND).getAsString(),
                                                                          false, false));
            isIdentOld = attributes.getField(FIELD_DP_IDENT_CLASS_OLD).getAsBoolean();
            datasetDateString = attributes.getField(FIELD_DP_DATASET_DATE).getAsString();
            ttzFilter = attributes.getField(FIELD_DP_TTZ_FILTER).getAsBoolean();
            extendedCodeScoringWithModelCodes = attributes.getField(FIELD_DP_SCORING_WITH_MCODES).getAsBoolean();
            disabledFilters = new TreeSet<>(SetOfEnumDataType.parseSetofEnum(attributes.getField(FIELD_DP_DISABLED_FILTERS).getAsString(),
                                                                             false, false));
            showLooseSasWhileFiltering = attributes.getField(FIELD_DP_SHOW_SAS).getAsBoolean();
            noPrimusHints = attributes.getField(FIELD_DP_NO_PRIMUS_HINTS).getAsBoolean();
            modificationTimestamp = attributes.getField(FIELD_DP_MODIFICATION_TIMESTAMP).getAsString();
            useProductionAggregates = attributes.getField(FIELD_DP_CAB_FALLBACK).getAsBoolean();
            useSVGs = attributes.getField(FIELD_DP_USE_SVGS).getAsBoolean();
            preferSVGs = attributes.getField(FIELD_DP_PREFER_SVG).getAsBoolean();
            isIdentFactoryFiltering = attributes.getField(FIELD_DP_IDENT_FACTORY_FILTERING).getAsBoolean();
            isFullLanguageSupport = attributes.getField(FIELD_DP_FULL_LANGUAGE_SUPPORT).getAsBoolean();

            isCarAndVanProduct = getAsProductClasses().stream().anyMatch(AS_PRODUCT_CLASSES_CAR_AND_VAN::contains);
            isTruckAndBusProduct = getAsProductClasses().stream().anyMatch(AS_PRODUCT_CLASSES_TRUCK_AND_BUS::contains);
            isDoDIALOGInvalidEntriesCheck = attributes.getField(FIELD_DP_DIALOG_POS_CHECK).getAsBoolean();
            productSupplier = attributes.getField(FIELD_DP_SUPPLIER_NO).getAsString();
            useVehicleFactoryNumber = attributes.getField(FIELD_DP_USE_FACTORY).getAsBoolean();
            DBDataObjectAttribute fieldCarPerspective = attributes.getField(FIELD_DP_CAR_PERSPECTIVE, false);
            if (fieldCarPerspective != null) {
                carPerspective = fieldCarPerspective.getAsBoolean();
            }
            containsCarPerspectiveTU = null;
            return true;
        } else { // Produkt mit Dummy-Werten befüllen, um NPEs zu vermeiden
            productStructuringType = PRODUCT_STRUCTURING_TYPE.EINPAS;
            productTitleTextNo = "";
            productTitle = new EtkMultiSprache("!!Produkt '%1' nicht gefunden", project.getConfig().getDatabaseLanguages(),
                                               productId.getProductNumber());
            pictureName = "";
            productGroup = "";
            aggregateType = "";
            specialCatalog = false;
            asProductClasses = new LinkedHashSet<>();
            documentationType = iPartsDocumentationType.UNKNOWN;
            productRemarkTextNo = "";
            productComment = "";
            codeForAutoSelect = "";
            identsForAutoSelect = new DwList<>(0);
            minMaxModelsValidityLoaded = true;
            validCountries = new TreeSet<>();
            invalidCountries = new TreeSet<>();
            brands = new LinkedHashSet<>();
            isIdentOld = false;
            datasetDateString = "";
            ttzFilter = false;
            extendedCodeScoringWithModelCodes = false; // normalerweise werden bm-bildende Code nicht berücksichtigt
            disabledFilters = new TreeSet<>();
            showLooseSasWhileFiltering = false;
            noPrimusHints = false;
            useProductionAggregates = false;
            useSVGs = false;
            preferSVGs = false;
            isIdentFactoryFiltering = false;
            isFullLanguageSupport = false;
            modificationTimestamp = "";
            useVehicleFactoryNumber = false;
            containsCarPerspectiveTU = null;
            return false;
        }
    }

    /**
     * Erzeugt eine Liste mit {@link iPartsIdentRange} aus den "Ident Ab" und den "Ident Bis" Strings
     *
     * @param fromIdentsString
     * @param toIdentsString
     * @return
     */
    private void fillAutoSelectIdentsMap(String fromIdentsString, String toIdentsString) {
        identsForAutoSelect = new DwList<>(0);
        String[] fromIdents = StrUtils.toStringArray(fromIdentsString, "/", true);
        String[] toIdents = StrUtils.toStringArray(toIdentsString, "/", true);
        int length = Math.max(fromIdents.length, toIdents.length);
        for (int i = 0; i < length; i++) {
            String fromIdent = (i < fromIdents.length) ? fromIdents[i] : "";
            String toIdent = (i < toIdents.length) ? toIdents[i] : "";
            if (!StrUtils.isEmpty(fromIdent, toIdent)) {
                identsForAutoSelect.add(new iPartsIdentRange(fromIdent, toIdent));
            }
        }
    }

    // Baumuster laden
    private void loadModelsIfNeeded(EtkProject project) {
        if (modelNumbers == null) {
            Set<String> newModelNumbers = new TreeSet<>();
            Set<String> newVisibleModelNumbers = new TreeSet<>();
            Map<String, Set<iPartsModelId>> newModelAAMap = new HashMap<>();
            iPartsDataModelList modelsList = new iPartsDataModelList();
            EtkDisplayFields selectFields = project.getAllDisplayFieldsForTable(TABLE_DA_MODEL);
            selectFields.addFeld(new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_VISIBLE, false, false));
            modelsList.searchSortAndFillWithJoin(project, null, selectFields, new String[]{ FIELD_DM_MODEL_NO }, TABLE_DA_PRODUCT_MODELS,
                                                 new String[]{ FIELD_DPM_MODEL_NO }, true, false,
                                                 new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO) },
                                                 new String[]{ productId.getProductNumber() }, false,
                                                 new String[]{ FIELD_DM_MODEL_NO }, false);

            if (!modelsList.isEmpty()) { // Produkt mit Baumustern
                for (iPartsDataModel productModelData : modelsList) {
                    iPartsModel model = iPartsModel.getInstance(project, productModelData.getAsId(), productModelData.getAttributes());
                    addModelToAAMap(productModelData, newModelAAMap);
                    String modelNumber = model.getModelId().getModelNumber();

                    // Hier muss zusätzlich DPM_MODEL_VISIBLE mit geprüft werden.
                    boolean dpmModelVisible = true;
                    if (productModelData.attributeExists(FIELD_DPM_MODEL_VISIBLE)) {
                        dpmModelVisible = productModelData.getFieldValueAsBoolean(FIELD_DPM_MODEL_VISIBLE);
                    }

                    if (model.isModelVisible() && dpmModelVisible) {
                        newVisibleModelNumbers.add(modelNumber);
                    }
                    newModelNumbers.add(modelNumber);
                }
            }

            synchronized (this) {
                if (modelNumbers == null) {
                    modelNumbers = newModelNumbers;
                    visibleModelNumbers = newVisibleModelNumbers;
                    modelAAMap = newModelAAMap;
                }
            }
        }
    }

    /**
     * Fügt das Baumuster zur übergebenen "Ausführungsart zu Baumuster" - Map hinzu.
     *
     * @param modelData
     * @param modelAAMap
     */
    private static void addModelToAAMap(iPartsDataModel modelData, Map<String, Set<iPartsModelId>> modelAAMap) {
        String aa = modelData.getFieldValue(FIELD_DM_AA);
        Set<iPartsModelId> modelIds;
        if (!modelAAMap.containsKey(aa)) {
            modelIds = new HashSet<>();
            modelAAMap.put(aa, modelIds);
        } else {
            modelIds = modelAAMap.get(aa);
        }
        modelIds.add(modelData.getAsId());
    }

    /**
     * Gegenstück zu {@link #getVehicles(EtkProject)}: Liefert alle Aggregate-Produkte, die diese Fahrzeug verwendet.
     *
     * @param project
     * @return
     */
    public List<iPartsProduct> getAggregates(EtkProject project) {
        if (aggregates == null) {
            List<iPartsProduct> newAggregates;
            Set<iPartsModelId> aggregateModelIds = new LinkedHashSet<>();
            for (String model : getModelNumbers(project)) {
                // Bei einem Fahrzeugbaumuster Aggregate laden
                if (!new iPartsModelId(model).isAggregateModel()) {
                    DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_MODELS_AGGS, null,
                                                                                                       new String[]{ FIELD_DMA_MODEL_NO },
                                                                                                       new String[]{ model });

                    for (DBDataObjectAttributes attributes : attributesList) {
                        String aggregateNumber = attributes.getField(FIELD_DMA_AGGREGATE_NO).getAsString();
                        aggregateModelIds.add(new iPartsModelId(aggregateNumber));
                    }
                }
            }

            // Produkte der Aggregate-Baumuster bestimmen
            newAggregates = new DwList<>(aggregateModelIds.size());
            if (!aggregateModelIds.isEmpty()) {
                Set<iPartsProductId> aggregateProductIds = new HashSet<>();
                for (iPartsModelId aggregateModelId : aggregateModelIds) {
                    // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster ebenfalls berücksichtigen.
                    List<iPartsProduct> productsForAggregate = iPartsProductHelper.getProductsForModel(project, aggregateModelId,
                                                                                                       getProductStructuringType(),
                                                                                                       iPartsProductHelper.PRODUCTS_FOR_SERIES_MODE.EINPAS_INCLUDING_KG_TU,
                                                                                                       false);
                    for (iPartsProduct aggregateProduct : productsForAggregate) {
                        if (!aggregateProductIds.contains(aggregateProduct.getAsId())) {
                            aggregateProductIds.add(aggregateProduct.getAsId());
                            newAggregates.add(aggregateProduct);
                        }
                    }
                }
            }

            synchronized (this) {
                if (aggregates == null) {
                    aggregates = newAggregates;
                }
            }
        }

        return Collections.unmodifiableList(aggregates);
    }

    /**
     * Ermittele aus welchem Aggregateprodukt diese Stückliste stammt
     *
     * @param project
     * @param assemblyId
     * @return
     */
    public List<iPartsProductId> getAggregateProductsForModule(EtkProject project, iPartsAssemblyId assemblyId) {
        // Suche alle Aggrgate durch, in welchem diese Stückliste enthelten ist
        List<iPartsProductId> result = new DwList<>();

        for (iPartsProduct aggregate : getAggregates(project)) {
            for (iPartsModuleReferences moduleReferences : iPartsProductStructures.getInstance(project, aggregate.getAsId()).getModules(project)) {
                if (moduleReferences.getAssemblyId().equals(assemblyId)) {
                    result.add(aggregate.getAsId());
                }
            }
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Sollen alle relevanten Aggregate dazugemischt werden?
     *
     * @return
     */
    public boolean isStructureWithAggregates() {
        // Für die Anzeige von Aggregaten in EinPAS unabhängig vom Session-Flag müsste hier nur als erstes auf
        // getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.EINPAS geprüft werden
        return isProductStructureWithAggregatesForSession();
    }

    /**
     * Für Anzeige im Debugger
     *
     * @return
     */
    @Override
    public String toString() {
        return productId.toString();
    }

    /**
     * Gibt das Datum von Datenstand unformatiert zurück.
     *
     * @return
     */
    public String getDatasetDateString() {
        return datasetDateString;
    }

    /**
     * Gibt das Datum und Uhrzeit der letzten Veränderung unformatiert zurück.
     *
     * @return
     */
    public String getModificationTimestamp() {
        return modificationTimestamp;
    }

    /**
     * Liefert alle Werke, die zu diesem Produkt hinzugefügt wurden
     *
     * @param project
     * @return
     */
    public Set<String> getProductFactories(EtkProject project) {
        if (productFactories == null) {
            Set<String> newProductFactories = new HashSet<>();
            iPartsDataProductFactoryList productFactoryList = iPartsDataProductFactoryList.loadDataProductFactoryListForProduct(project, new iPartsProductId(getAsId().getProductNumber()));
            for (iPartsDataProductFactory productFactory : productFactoryList) {
                newProductFactories.add(productFactory.getAsId().getFactoryNumber());
            }

            synchronized (this) {
                if (productFactories == null) {
                    productFactories = newProductFactories;
                }
            }
        }
        return Collections.unmodifiableSet(productFactories);
    }

    /**
     * Liefert alle Varianten, die zu diesem Produkt hinzugefügt wurden
     *
     * @param project
     * @return
     */
    public Set<String> getProductVariantsIds(EtkProject project) {
        if (productVariantIds == null) {
            Set<String> productVariants = new HashSet<>();
            iPartsDataPSKProductVariantList productVariantsList = iPartsDataPSKProductVariantList.loadPSKProductVariants(project, getAsId());
            for (iPartsDataPSKProductVariant productVariantFromDB : productVariantsList) {
                productVariants.add(productVariantFromDB.getAsId().getVariantId());
            }

            synchronized (this) {
                if (productVariantIds == null) {
                    productVariantIds = productVariants;
                }
            }
        }
        return Collections.unmodifiableSet(productVariantIds);
    }

    /**
     * Überprüft, ob mind. ein Baumuster dieses Produkts die übergebene Ausführungsart besitzt
     *
     * @param project
     * @param ausfuehrungsart
     * @return
     */
    public boolean hasModelsWithAA(EtkProject project, String ausfuehrungsart) {
        loadModelsIfNeeded(project);
        Map<String, Set<iPartsModelId>> localModelAAMap = modelAAMap;
        if (localModelAAMap == null) {
            return false;
        }
        return localModelAAMap.containsKey(ausfuehrungsart);
    }

    /**
     * Liefert zurück, ob bm-bildende Code beim Erweiterten-Code-Filter übersprungen werden. Im Normalfall werden diese
     * Code übersprungen, sofern man nicht aktiv den Kenner am Produkt gesetzt hat.
     *
     * @return
     */
    public boolean skipModelCodeInExtendedCodeFilter() {
        return !extendedCodeScoringWithModelCodes;
    }

    /**
     * Liefert alle Ausführungsarten der zum Produkt gehörenden Baumuster
     *
     * @param project
     * @return
     */
    public Set<String> getAAsFromModels(EtkProject project) {
        loadModelsIfNeeded(project);
        Map<String, Set<iPartsModelId>> localModelAAMap = modelAAMap;
        if (localModelAAMap == null) {
            return null;
        }
        return Collections.unmodifiableSet(modelAAMap.keySet());
    }

    /**
     * Gibt zurück, ob die referenzierte Baureihe am Produkt eventgesteuert ist.
     *
     * @return
     */
    public boolean isReferencedSeriesEventControlled(EtkProject project) {
        if ((referencedSeries != null) && referencedSeries.isValidId()) {
            return iPartsDialogSeries.getInstance(project, referencedSeries).isEventTriggered();
        }
        return false;
    }

    /**
     * Liefert die verfügbaren Sprachen des Produktes in Abhängigkeit des Schalters in DP_FULL_LANGUAGE_SUPPORT
     *
     * @param project
     * @return
     */
    public List<String> getSupportedLanguages(EtkProject project) {
        if (isFullLanguageSupport) {
            return iPartsLanguage.getCompleteDaimlerLanguageList(project);
        } else {
            return iPartsLanguage.getEPCLanguageList();
        }
    }

    /**
     * Gibt zurück, ob freie SAs im Produkt angezeigt werden sollen (unabhängig von der Admin-Option)
     *
     * @return
     */
    public boolean showLooseSasWhileFiltering() {
        return showLooseSasWhileFiltering;
    }

    /**
     * Gibt zurück, ob PRIMUS-Ersetzungen ausgegeben werden sollen
     *
     * @return true: sollen NICHT ausgegeben werden
     */
    public boolean isNoPrimusHints() {
        return noPrimusHints;
    }

    /**
     * Sollen Rückmeldedaten nach Montagewerk gefiltert werden im Endnummernfilter?
     *
     * @return
     */
    public boolean isIdentFactoryFiltering() {
        return isIdentFactoryFiltering;
    }

    /**
     * Gibt zurück, ob das Produkt in allen Datenbanksprachen oder nur in den EPC-Sprachen vorliegt.
     * Siehe DAIMLER-13859
     *
     * @return
     */
    public boolean isFullLanguageSupport() {
        return isFullLanguageSupport;
    }

    /**
     * Liefert die SAA-Gültigkeiten von allen Baumustern des Produkts zurück.
     *
     * @param project
     * @return
     */
    public Set<String> getAllModelSAAValidities(EtkProject project) {
        Set<String> saaValidities = new TreeSet<>();
        Set<String> modelNumbers = getModelNumbers(project);
        for (String modelNumber : modelNumbers) {
            saaValidities.addAll(iPartsModel.getInstance(project, new iPartsModelId(modelNumber)).getSaas(project));
        }
        return saaValidities;
    }
}