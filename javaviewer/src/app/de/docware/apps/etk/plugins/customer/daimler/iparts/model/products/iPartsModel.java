/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import com.owlike.genson.GenericType;
import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsSpecType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Repräsentation eines Baumusters im After-Sales (Tabelle DA_MODEL).
 */
public class iPartsModel implements CacheForGetCacheDataEvent<iPartsModel>, iPartsConst {

    // ObjectInstanceStrongLRUList, da Baumuster eine zentrale Datenstruktur sind, die ständig benötigt werden und dadurch
    // insgesamt die Performance besser wird
    private static ObjectInstanceStrongLRUList<Object, iPartsModel> instances = new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_MODELS,
                                                                                                                  MAX_CACHE_LIFE_TIME_CORE);

    private static final boolean ENABLE_CASHES_PROVIDER_FOR_SINGLE_MODEL = false;

    @JsonProperty
    protected iPartsModelId modelId;
    protected volatile EtkMultiSprache modelSalesTitle;
    @JsonProperty
    protected String modelSalesTitleTextNo; // Textnummer für das spätere Nachladen der Texte
    @JsonProperty
    protected String aggregateType;
    @JsonProperty
    protected String productGroup;
    @JsonProperty
    protected iPartsSeriesId seriesId;
    @JsonProperty
    protected boolean modelVisible;
    @JsonProperty
    protected boolean modelInvalid;
    @JsonProperty
    protected String ausfuehrungsArt;
    @JsonProperty
    protected String codes;
    @JsonProperty
    protected boolean isFilterRelevant;
    @JsonProperty
    protected boolean isNotDocuRelevant;
    @JsonProperty
    protected volatile Set<String> codeSetWithAA; // bm-bildende Code MIT Ausführungsart
    @JsonProperty
    protected volatile Set<String> codeSetWithoutAA; // bm-bildende Code OHNE Ausführungsart
    @JsonProperty
    protected volatile Set<String> negativeModelBuildingCodeSetWithAA; // negative BM-bildende Code MIT Ausführungsart
    @JsonProperty
    protected volatile Set<String> negativeModelBuildingCodeSetWithoutAA; // negative BM-bildende Code OHNE Ausführungsart
    @JsonProperty
    protected volatile Set<String> saas;
    protected volatile EtkMultiSprache modelAddText;
    @JsonProperty
    protected String modelAddTextTextNo; // Textnummer für das spätere Nachladen der Texte
    protected volatile EtkMultiSprache modelName;
    @JsonProperty
    protected String modelNameTextNo; // Textnummer für das späteres Nachladen
    @JsonProperty
    protected String validFrom;
    @JsonProperty
    protected String validTo;
    protected volatile Map<iPartsSpecType, Map<String, iPartsDataModelOil>> specValidityAndCode;
    protected volatile Map<iPartsSpecType, List<iPartsDataModelOilQuantity>> quantityAndValidities;

    @JsonProperty
    private boolean existsInDB; // Existiert das Baumuster in der DB oder ist es nur mit Dummy-Werten gefüllt

    public static void warmUpCache(final EtkProject project) {
        synchronized (iPartsModel.class) {
            if (instances.size() > 1000) {
                // Cache ist gar nicht leer -> kein WarmUp notwendig (1000 als Schwellwert, weil auch bei einem initial leeren Cache
                // durch einzelne getInstance()-Aufrufe der Cache schon teilweise befüllt sein könnte ohne echtes WarmUp; insgesamt
                // gibt es aber erheblich mehr als 1000 Baumuster bei iParts
                return;
            }
        }

        // Alle Baumuster laden
        // Laden der Daten mittels IAC vom Caches-Provider?
        iPartsModel dummyModel = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsModel(), null, instances, null);
        if (dummyModel != null) {
            return;
        }

        synchronized (iPartsModel.class) {
            // Laden der Daten aus der DB
            iPartsDataModelList dataModelList = iPartsDataModelList.loadAllDataModelList(project, DBDataObjectList.LoadType.COMPLETE);
            for (iPartsDataModel dataModel : dataModelList) {
                getInstance(project, dataModel.getAsId(), dataModel.getAttributes());
            }
        }

        // DAIMLER-2337 TODO Alternative zum CacheWarmUp für alle Baumuster inkl. SAAs -> braucht gut 500 MB und ca. 2 Minuten
//        EtkDisplayFields selectFields = new EtkDisplayFields();
//        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_NO, false, false));
//        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_SALES_TITLE, false, false)); // absichtlich NICHT als MultiLang gekennzeichnet, da hier nur die TextNr ausgelesen wird
//        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_TYPE, false, false));
//        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_PRODUCT_GRP, false, false));
//        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_SERIES_NO, false, false));
//        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_AA, false, false));
//        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_CODE, false, false));
//        selectFields.addFeld(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_VISIBLE, false, false));
//
//        selectFields.addFeld(new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, false));
//
//        iPartsDataSAAModelsList dataModelList = new iPartsDataSAAModelsList();
//        dataModelList.searchSortAndFillWithJoin(project, null, selectFields, new String[]{ FIELD_DA_ESM_MODEL_NO }, TABLE_DA_MODEL,
//                                                new String[]{ FIELD_DM_MODEL_NO }, false, false, null, null, false, null, false,
//                                                new EtkDataObjectList.FoundAttributesCallback() {
//                                                    @Override
//                                                    public boolean foundAttributes(DBDataObjectAttributes attributes) {
//                                                        iPartsModelId newModelIdFromDB = new iPartsModelId(attributes.getField(FIELD_DM_MODEL_NO).getAsString());
//                                                        iPartsModel model = getInstance(project, newModelIdFromDB, attributes);
//                                                        if (model.saas == null) {
//                                                            model.saas = new TreeSet<String>();
//                                                        }
//                                                        model.saas.add(attributes.getField(FIELD_DA_ESM_SAA_NO).getAsString());
//
//                                                        return false;
//                                                    }
//                                                });
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized void removeModelFromCache(EtkProject project, iPartsModelId modelId) {
        Object hashObject = getInstanceHashObject(project, modelId);
        instances.removeKey(hashObject);
    }

    private static String getInstanceHashObject(EtkProject project, iPartsModelId modelId) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsModel.class, (modelId != null) ? modelId.getModelNumber() : null, false);
    }

    public static synchronized iPartsModel getInstance(EtkProject project, iPartsModelId modelId, DBDataObjectAttributes attributes) {
        Object hashObject = getInstanceHashObject(project, modelId);
        iPartsModel result = instances.get(hashObject);

        if (result == null) {
            // Beim WarmUp nicht einzeln jedes Baumuster per IAC abfragen falls es initial Probleme gab bei der Abfrage aller Baumuster
            if (ENABLE_CASHES_PROVIDER_FOR_SINGLE_MODEL && (attributes == null)) {
                Map<String, String> cacheParameters = new HashMap<>();
                cacheParameters.put(iPartsModelId.TYPE, modelId.getModelNumber());
                result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsModel(), cacheParameters, instances, hashObject);
                if (result != null) {
                    return result;
                }
            }

            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsModel(project, modelId, attributes);
            instances.put(hashObject, result);
        }

        return result;
    }

    public static synchronized iPartsModel getInstance(EtkProject project, iPartsModelId modelId) {
        return getInstance(project, modelId, null);
    }

    public static boolean isAggregateModel(String modelNo) {
        if (!StrUtils.isEmpty(modelNo)) {
            return modelNo.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE);
        }
        return false;
    }

    public static boolean isVehicleModel(String modelNo) {
        if (!StrUtils.isEmpty(modelNo)) {
            return modelNo.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR);
        }
        return false;
    }

    /**
     * Check, ob die übergebene Baumusternummer valide ist
     * <p>
     * Valide, wenn
     * - "C" oder "D" am Anfang
     * - Länge mit Buchstabe = 7
     *
     * @param modelNo
     * @return
     */
    public static boolean isModelNumberValid(String modelNo) {
        return iPartsModelId.isModelNumberValid(modelNo, true);
    }

    /**
     * Check, ob die übergebene Baumusternummer valide ist
     * wenn bothWays == true, dann wird auch ohne "C"/"D" am Anfang überprüft
     * sonst Verhalten wie bei isModelNumberValid(String modelNo)
     *
     * @param modelNo
     * @param bothWays
     * @return
     */
    public static boolean isModelNumberValid(String modelNo, boolean bothWays) {
        boolean result = isModelNumberValid(modelNo);
        if (!result && bothWays) {
            result = iPartsModelId.isModelNumberValid(modelNo, false);
        }
        return result;
    }


    public static void fillCacheDataForAllModels(SetCacheDataEvent setCacheDataEvent, EtkProject project) {
        String hashObjectPrefix = getInstanceHashObject(project, null);
        List<iPartsModel> modelsList = new ArrayList<>();
        for (Map.Entry<Object, iPartsModel> modelEntry : instances.getMap().entrySet()) {
            if (modelEntry.getKey().toString().startsWith(hashObjectPrefix)) {
                modelsList.add(modelEntry.getValue());
            }
        }

        setCacheDataEvent.setCacheData(modelsList, new GenericType<List<iPartsModel>>() {
        });
    }

    public static void setCacheByDataForAllModels(SetCacheDataEvent setCacheDataEvent, EtkProject project) {
        List<iPartsModel> modelsList = setCacheDataEvent.getCacheData(new GenericType<List<iPartsModel>>() {
        });
        Map<Object, iPartsModel> newInstances = new HashMap<>();
        for (iPartsModel model : modelsList) {
            String hashObject = getInstanceHashObject(project, model.getModelId());
            newInstances.put(hashObject, model);
        }
        synchronized (instances) {
            for (Map.Entry<Object, iPartsModel> newInstance : newInstances.entrySet()) {
                instances.put(newInstance.getKey(), newInstance.getValue());
            }
            instances.notifyAll();
        }
    }

    public iPartsModel() {
    }

    protected iPartsModel(EtkProject project, iPartsModelId modelId, DBDataObjectAttributes attributes) {
        this.modelId = modelId;
        loadHeader(project, attributes);
    }

    @Override
    public iPartsModel createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        Map<String, String> cacheParametersMap = setCacheDataEvent.getCacheParametersMap();
        if ((cacheParametersMap != null) && StrUtils.isValid(cacheParametersMap.get(iPartsModelId.TYPE))) {
            return createInstance(setCacheDataEvent, instances, getInstanceHashObject(project, new iPartsModelId(cacheParametersMap.get(iPartsModelId.TYPE))));
        } else {
            setCacheByDataForAllModels(setCacheDataEvent, project);
            return new iPartsModel(); // Dummy-Cache-Eintrag als Bestätigung, dass der Cache vollständig geladen wurde
        }
    }

    public iPartsModelId getModelId() {
        return modelId;
    }

    public String getModelTypeNumber() {
        return getModelId().getModelTypeNumber();
    }

    public EtkMultiSprache getModelSalesTitle(EtkProject project) {
        if (modelSalesTitle == null) {
            EtkMultiSprache newModelSalesTitle = project.getEtkDbs().getMultiLanguageByTextNr(TableAndFieldName.make(TABLE_DA_MODEL,
                                                                                                                     FIELD_DM_SALES_TITLE),
                                                                                              modelSalesTitleTextNo);
            synchronized (this) {
                if (modelSalesTitle == null) {
                    modelSalesTitle = newModelSalesTitle;
                }
            }
        }
        return modelSalesTitle;
    }

    public EtkMultiSprache getModelAddText(EtkProject project) {
        if (modelAddText == null) {
            EtkMultiSprache newModelAddText = project.getEtkDbs().getMultiLanguageByTextNr(TableAndFieldName.make(TABLE_DA_MODEL,
                                                                                                                  FIELD_DM_ADD_TEXT),
                                                                                           modelAddTextTextNo);
            synchronized (this) {
                if (modelAddText == null) {
                    modelAddText = newModelAddText;
                }
            }
        }
        return modelAddText;
    }

    /**
     * Liefert die Spezifikationen und Coderegel zum aktuellen Baumuster
     *
     * @param project
     * @param specType
     * @return
     */
    public Map<String, iPartsDataModelOil> getSpecValidityAndCode(EtkProject project, iPartsSpecType specType) {
        loadSpecValiditiesIfNeeded(project);
        if (specValidityAndCode.containsKey(specType)) {
            return specValidityAndCode.get(specType);
        } else {
            return new HashMap<>();
        }

    }

    /**
     * Liefert die Motoröl-Mengen inkl. Gültigkeiten zum aktuellen Baumuster
     *
     * @param project
     * @return
     */
    public List<iPartsDataModelOilQuantity> getQuantityAndValidities(EtkProject project, iPartsSpecType specType) {
        loadSpecValiditiesIfNeeded(project);
        if (quantityAndValidities.containsKey(specType)) {
            return quantityAndValidities.get(specType);
        } else {
            return new DwList<>();
        }
    }

    private void loadSpecValiditiesIfNeeded(EtkProject project) {
        if ((quantityAndValidities == null) || (specValidityAndCode == null)) {
            Map<iPartsSpecType, Map<String, iPartsDataModelOil>> specValidityAndCodeLoaded = new HashMap<>();
            Map<iPartsSpecType, List<iPartsDataModelOilQuantity>> quantityAndValiditiesLoaded = new HashMap<>();
            for (iPartsSpecType specType : iPartsSpecType.RELEVANT_TYPES) {
                Map<String, iPartsDataModelOil> specValidityAndCodeForSpecType = new HashMap<>();
                iPartsDataModelOilList modelOilListForSpecType = iPartsDataModelOilList.loadDataModelOilForModelAndSpecType(project, modelId, specType);
                if (!modelOilListForSpecType.isEmpty()) {
                    modelOilListForSpecType.forEach(modelOil -> {
                        specValidityAndCodeForSpecType.put(modelOil.getAsId().getSpecValidity(), modelOil);
                    });
                    iPartsDataModelOilQuantityList modelOilQuantityList = iPartsDataModelOilQuantityList.loadDataModelOilQuantityForModel(project, modelId, specType);
                    if (!modelOilQuantityList.isEmpty()) {
                        quantityAndValiditiesLoaded.put(specType, modelOilQuantityList.getAsList());
                    }
                }
                specValidityAndCodeLoaded.put(specType, specValidityAndCodeForSpecType);
            }
            synchronized (this) {
                if (specValidityAndCode == null) {
                    specValidityAndCode = specValidityAndCodeLoaded;
                }
                if (quantityAndValidities == null) {
                    quantityAndValidities = quantityAndValiditiesLoaded;
                }
            }
        }
    }

    public EtkMultiSprache getModelName(EtkProject project) {
        if (modelName == null) {
            EtkMultiSprache newModelName = project.getEtkDbs().getMultiLanguageByTextNr(
                    TableAndFieldName.make(TABLE_DA_MODEL, FIELD_DM_NAME),
                    modelNameTextNo);
            synchronized (this) {
                if (modelName == null) {
                    modelName = newModelName;
                }
            }
        }
        return modelName;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getProductGroup() {
        return productGroup;
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public boolean isAggregateModel() {
        return modelId.isAggregateModel();
    }

    public boolean isVehicleModel() {
        return isVehicleModel(modelId.getModelNumber());
    }

    public boolean isModelVisible() {
        return modelVisible;
    }

    public boolean isModelInvalid() {
        return modelInvalid;
    }

    public String getAusfuehrungsArt() {
        return ausfuehrungsArt;
    }

    public boolean isDocuRelevant() {
        return !isNotDocuRelevant;
    }

    public String getCodes() {
        return codes;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    /**
     * Liefert die Liste aller baumusterbildenden Codes inkl. Ausführungsart-Code zurück.
     *
     * @return
     */
    public Set<String> getCodeSetWithAA() {
        if (codeSetWithAA == null) {
            Set<String> newCodeSet = new TreeSet<>(getCodeSetWithoutAA());
            if (!getAusfuehrungsArt().isEmpty()) {
                newCodeSet.add(getAusfuehrungsArt());
            }
            synchronized (this) {
                if (codeSetWithAA == null) {
                    codeSetWithAA = newCodeSet;
                }
            }
        }
        return Collections.unmodifiableSet(codeSetWithAA);
    }

    /**
     * Liefert die Liste aller baumusterbildenden Codes OHNE Ausführungsart-Code zurück.
     *
     * @return
     */
    public Set<String> getCodeSetWithoutAA() {
        if (codeSetWithoutAA == null) {
            Set<String> newCodeSet = new TreeSet<>(DaimlerCodes.getCodeSet(getCodes()));
            synchronized (this) {
                if (codeSetWithoutAA == null) {
                    codeSetWithoutAA = newCodeSet;
                }
            }
        }
        return Collections.unmodifiableSet(codeSetWithoutAA);
    }

    public Set<String> getSaas(EtkProject project) {
        if (saas == null) {
            Set<String> newSaas = new TreeSet<>();
            for (iPartsDataSAAModels dataSAAModel : iPartsDataSAAModelsList.loadDataSAAModelsListForModel(project, modelId)) {
                newSaas.add(dataSAAModel.getFieldValue(iPartsConst.FIELD_DA_ESM_SAA_NO));
            }

            synchronized (this) {
                if (saas == null) {
                    saas = newSaas;
                }
            }
        }
        return Collections.unmodifiableSet(saas);
    }

    private boolean loadHeader(EtkProject project, DBDataObjectAttributes attributes) {
        if (attributes == null) {
            attributes = project.getDbLayer().getAttributes(TABLE_DA_MODEL, new String[]{ FIELD_DM_MODEL_NO },
                                                            new String[]{ modelId.getModelNumber() });
        }

        if (attributes != null) {
            modelSalesTitleTextNo = attributes.getFieldValue(FIELD_DM_SALES_TITLE);
            aggregateType = attributes.getFieldValue(FIELD_DM_MODEL_TYPE);
            productGroup = attributes.getFieldValue(FIELD_DM_PRODUCT_GRP);
            seriesId = new iPartsSeriesId(attributes.getFieldValue(FIELD_DM_SERIES_NO));
            ausfuehrungsArt = attributes.getFieldValue(iPartsConst.FIELD_DM_AA);
            codes = attributes.getFieldValue(iPartsConst.FIELD_DM_CODE);
            isFilterRelevant = attributes.getField(iPartsConst.FIELD_DM_FILTER_RELEVANT).getAsBoolean();
            isNotDocuRelevant = attributes.getField(iPartsConst.FIELD_DM_NOT_DOCU_RELEVANT).getAsBoolean();
            modelAddTextTextNo = attributes.getFieldValue(FIELD_DM_ADD_TEXT);
            modelNameTextNo = attributes.getFieldValue(FIELD_DM_NAME);

            // Zielformat ist yyyyMMddHHmmss
            validFrom = attributes.getFieldValue(iPartsConst.FIELD_DM_VALID_FROM);
            if (!validFrom.isEmpty()) {
                validFrom = StrUtils.padStringWithCharsUpToLength(attributes.getFieldValue(iPartsConst.FIELD_DM_VALID_FROM), '0', 14);
            }
            validTo = attributes.getFieldValue(iPartsConst.FIELD_DM_VALID_TO);
            if (!validTo.isEmpty()) {
                validTo = StrUtils.padStringWithCharsUpToLength(attributes.getFieldValue(iPartsConst.FIELD_DM_VALID_TO), '0', 14);
            }

            existsInDB = true;

            // Abwärtskompatibilität für nicht gefüllte Felder, was "ja" entsprechen soll
            DBDataObjectAttribute modelVisibleField = attributes.getField(FIELD_DM_MODEL_VISIBLE);
            modelVisible = modelVisibleField.getAsString().isEmpty() || modelVisibleField.getAsBoolean();
            modelInvalid = attributes.getField(iPartsConst.FIELD_DM_MODEL_INVALID).getAsBoolean();
        } else { // Baumuster mit Dummy-Werten befüllen, um NPEs zu vermeiden
            modelSalesTitleTextNo = "";
            modelSalesTitle = new EtkMultiSprache("!!Baumuster '%1' nicht gefunden", project.getConfig().getDatabaseLanguages(),
                                                  modelId.getModelNumber());
            aggregateType = "";
            productGroup = "";
            seriesId = new iPartsSeriesId();
            ausfuehrungsArt = "";
            codes = "";
            isFilterRelevant = false;
            isNotDocuRelevant = false;
            modelAddTextTextNo = "";
            modelAddText = new EtkMultiSprache();
            modelNameTextNo = "";
            modelName = new EtkMultiSprache();
            validFrom = "";
            validTo = "";
            existsInDB = false;
            return false;
        }

        return true;
    }

    public boolean existsInDB() {
        return existsInDB;
    }

    /**
     * Produkt zu Baumuster ermitteln.
     * Da es laut Datenbank mehrere Produkte geben kann, nehmen wir per Konvention das erste gefundene Produkt.
     * static weil SB schreibt: "Das Ergebnis sollte in iPartsModel aber nicht in einer Variablen gehalten werden, weil sie sonst beim
     * Ändern eines relevanten Produkts ja gelöscht werden müsste, was relativ kompliziert wäre..."
     * Bei Bedarf kann man ja hier ja noch einen Cache hinzufügen.
     *
     * @param project
     * @param modelNo
     * @return
     */
    public static iPartsDataProductModels getFirstDataProductModels(EtkProject project, String modelNo) {
        // Das Baumuster kann in mehreren Produkten vorkommen.
        // Für die Filterung wird einfach das erste Produkt genommen, das kann allerdings auch falsch sein
        iPartsDataProductModels dataProductModels = iPartsProductModels.getInstance(project).getFirstProductModelsByModel(project, modelNo);
        if (dataProductModels != null) {
            return dataProductModels;
        }

        //falls keine Product-Models Beziehung gefunden wurde, eine Default anlegen
        iPartsDataProductModels defaultDataProductModels = new iPartsDataProductModels(project, new iPartsProductModelsId("", modelNo));
        defaultDataProductModels.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
        return defaultDataProductModels;
    }

    public static String getFirstProductNumber(EtkProject project, String modelNo) {
        Set<String> productNumbersByModel = iPartsProductModels.getInstance(project).getProductNumbersByModel(modelNo);
        if ((productNumbersByModel != null) && !productNumbersByModel.isEmpty()) {
            return productNumbersByModel.iterator().next();
        }
        return "";
    }

    /**
     * Produkt zu Baumuster ermitteln.
     * Da es laut Datenbank mehrere Produkte geben kann, nehmen wir per Konvention das erste gefundene Produkt.
     * static weil SB schreibt: "Das Ergebnis sollte in iPartsModel aber nicht in einer Variablen gehalten werden, weil sie sonst beim
     * Ändern eines relevanten Produkts ja gelöscht werden müsste, was relativ kompliziert wäre..."
     * Bei Bedarf kann man ja hier ja noch einen Cache hinzufügen.
     *
     * @param project
     * @param modelNo
     * @return
     */
    public static iPartsProduct getFirstProduct(EtkProject project, String modelNo) {
        String productNumber = getFirstProductNumber(project, modelNo);
        if (StrUtils.isValid(productNumber)) {
            return iPartsProduct.getInstance(project, new iPartsProductId(productNumber));
        } else {
            return null;
        }
    }

    /**
     * Das erste Auto-Product-Select gültige Produkt zu Baumuster ermitteln.
     *
     * @param project
     * @param modelId
     * @return
     */
    public static iPartsProduct getFirstAPSFilteredProduct(EtkProject project, iPartsModelId modelId, FinId finId, AbstractDataCard dataCard) {
        if ((finId == null) || (modelId == null)) {
            return null;
        }
        List<iPartsProduct> products = iPartsProductModels.getInstance(project).getProductsByModel(project, modelId.getModelNumber());
        products = iPartsFilterHelper.getAutoSelectProductsForFIN(project, products, finId, modelId,
                                                                  (dataCard != null) ? dataCard.getCodes().getAllCheckedValues() : null);
        if ((products != null) && !products.isEmpty()) {
            return products.get(0);
        }
        return null;
    }

    /**
     * Liefert den Aggregatetyp zum übergebenen Baumuster zurück basierend auf dem zum Baumuster ermittelten ersten Produkt
     * mit Fallback auf den Aggregatetyp vom Baumuster selbst.
     *
     * @param project
     * @param modelNo
     * @return
     */
    public static String getAggregateTypeFromFirstProduct(EtkProject project, String modelNo) {
        iPartsProduct product = getFirstProduct(project, modelNo);
        if (product != null) { // Aggregatetyp vom Produkt
            return product.getAggregateType();
        } else { // Fallback auf den Aggregatetyp vom Baumuster
            return getInstance(project, new iPartsModelId(modelNo)).getAggregateType();
        }
    }

    /**
     * Liefert die positiven BM-bildenden Code für dieses Baumuster zurück.
     *
     * @param withAAModelBuildingCode Flag, ob die Ausführungsart zu den BM-bildenden Code hinzugefügt werden soll
     * @param project
     * @return
     */
    public Set<String> getPositiveModelBuildingCodeSet(boolean withAAModelBuildingCode, EtkProject project) {
        // Soll die Ausführungsart wie ein bm-bildender Code behandelt werden (nur bei Doku-Methode ungleich DIALOG abhängig
        // von withAAModelBuildingCode)?
        Set<String> positiveModelBuildingCodeSet;
        if (withAAModelBuildingCode && StrUtils.isValid(getAusfuehrungsArt())) {
            positiveModelBuildingCodeSet = getCodeSetWithAA();
        } else {
            positiveModelBuildingCodeSet = getCodeSetWithoutAA();
        }

        iPartsSeriesId seriesId = getSeriesId();
        if (seriesId.isValidId()) {
            Set<String> additionalModelBuildingCode = iPartsModelBuildingCode.getInstance(project).getAdditionalModelBuildingCode(seriesId,
                                                                                                                                  getAusfuehrungsArt());
            if (additionalModelBuildingCode != null) {
                positiveModelBuildingCodeSet = new TreeSet<>(positiveModelBuildingCodeSet); // Set ist bisher nicht veränderbar
                positiveModelBuildingCodeSet.addAll(additionalModelBuildingCode);
                positiveModelBuildingCodeSet = Collections.unmodifiableSet(positiveModelBuildingCodeSet);
            }
        }
        return positiveModelBuildingCodeSet;
    }

    /**
     * Liefert die negativen BM-bildenden Code für dieses Baumuster zurück.
     *
     * @param withAAModelBuildingCode Flag, ob die Ausführungsart zu den BM-bildenden Code hinzugefügt werden soll
     * @param project
     * @return
     */
    public Set<String> getNegativeModelBuildingCodeSet(boolean withAAModelBuildingCode, EtkProject project) {
        Set<String> negativeModelBuildingCodeSet;
        if (withAAModelBuildingCode) {
            negativeModelBuildingCodeSet = negativeModelBuildingCodeSetWithAA;
        } else {
            negativeModelBuildingCodeSet = negativeModelBuildingCodeSetWithoutAA;
        }

        if (negativeModelBuildingCodeSet == null) {
            // Alle Code aus den anderen Baumustern mit derselben AA sind die Negativ-Code
            negativeModelBuildingCodeSet = new TreeSet<>();
            if (!getAusfuehrungsArt().isEmpty()) {
                iPartsSeriesId seriesId = getSeriesId();
                if (seriesId.isValidId()) {
                    iPartsDialogSeries series = iPartsDialogSeries.getInstance(project, seriesId);
                    Set<String> modelNumbers = series.getModelNumbers(project);
                    if (!modelNumbers.isEmpty()) {
                        String modelNumber = getModelId().getModelNumber();
                        for (String negativeModelNumber : modelNumbers) {
                            if (!negativeModelNumber.equals(modelNumber)) {
                                iPartsModel negativeModel = iPartsModel.getInstance(project, new iPartsModelId(negativeModelNumber));
                                // nur Code von filterrelevanten Baumustern verwenden
                                if (negativeModel.isFilterRelevant) {
                                    String negativeModelAA = negativeModel.getAusfuehrungsArt();
                                    if (negativeModelAA.equals(getAusfuehrungsArt())) { // Gleiche Ausführungsart -> andere Code als Negativ-Code hinzufügen
                                        negativeModelBuildingCodeSet.addAll(negativeModel.getCodeSetWithoutAA());
                                    } else {
                                        // Andere Ausführungsart ebenfalls als Negativ-Code hinzufügen (nur bei Doku-Methode ungleich DIALOG)
                                        if (withAAModelBuildingCode) {
                                            negativeModelBuildingCodeSet.add(negativeModelAA);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (withAAModelBuildingCode) {
                negativeModelBuildingCodeSetWithAA = negativeModelBuildingCodeSet;
            } else {
                negativeModelBuildingCodeSetWithoutAA = negativeModelBuildingCodeSet;
            }
        }

        return Collections.unmodifiableSet(negativeModelBuildingCodeSet);
    }

    /**
     * Löscht den Cache für die negativen BM-bildenden Code für dieses Baumuster.
     */
    public void clearNegativeModelBuildingCodeCache() {
        negativeModelBuildingCodeSetWithAA = null;
        negativeModelBuildingCodeSetWithoutAA = null;
    }
}