/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodels;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Endpoint für den GetModels-Webservice
 */
public class iPartsWSGetModelsEndpoint extends iPartsWSAbstractEndpoint<iPartsWSGetModelsRequest> implements iPartsConst {

    public static final String DEFAULT_ENDPOINT_URI = "/ident/GetModels";

    @Override
    protected iPartsWSGetModelsResponse executeWebservice(EtkProject project, iPartsWSGetModelsRequest requestObject) throws RESTfulWebApplicationException {
        // Sprache bestimmen
        iPartsWSUserInfo userInfo = requestObject.getUser();
        String language = userInfo.getLanguage().toUpperCase();

        List<iPartsWSModel> wsModels = getWSModels(requestObject.getAggTypeId(), requestObject.getProductId(),
                                                   requestObject.getProductName(), requestObject.getProductClassIds(),
                                                   requestObject.getModelTypeId(), project, language, userInfo);

        // Response erzeugen
        iPartsWSGetModelsResponse response = new iPartsWSGetModelsResponse();
        response.setModels(wsModels);
        return response;
    }

    public static List<iPartsWSModel> getWSModels(String aggTypeId, String productId, String productName, List<String> productClassIds,
                                                  String modelTypeId, EtkProject project, String language, iPartsWSUserInfo userInfo) {
        Set<String> models = new TreeSet<>();

        // Produkte filtern und daraus Liste aller Baumuster erstellen
        List<iPartsProduct> productList = iPartsProduct.getAllProducts(project);
        boolean isModelTypeIdFilter = !StrUtils.isEmpty(modelTypeId);
        Map<iPartsModelId, Set<String>> modelToASProductClassesMap = new HashMap<>();
        boolean onlyRetailRelevantProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();
        boolean permissionErrorDetected = false;
        for (iPartsProduct product : productList) {
            VarParam<Boolean> permissionError = new VarParam<>(false);
            if (productFitsSearchValues(product, onlyRetailRelevantProducts, aggTypeId, productId, productName, productClassIds,
                                        modelTypeId, project, language, userInfo, permissionError)) {
                boolean checkModelVisibility = iPartsPlugin.isCheckModelVisibility();
                Set<String> modelNumbers = checkModelVisibility ? product.getVisibleModelNumbers(project) : product.getModelNumbers(project);
                for (String model : modelNumbers) {
                    // Baureihe/Typkennzahl vom Baumuster muss bei vorhandenem Typkennzahlenfilter mit modelTypeId übereinstimmen
                    iPartsModelId modelId = new iPartsModelId(model);
                    if (!isModelTypeIdFilter || modelId.getModelTypeNumber().equals(modelTypeId)) {
                        // Die AS Produktklassen in der Map für dieses Baumuster merken
                        modelToASProductClassesMap.put(modelId, product.getAsProductClasses());

                        models.add(model);
                    }
                }
            }
            permissionErrorDetected |= permissionError.getValue();
        }

        // Das Ergebnis ist leer, aber es hat Daten gegeben die durch Berechtigungsprüfung ausgefiltert wurden
        if (!productList.isEmpty() && models.isEmpty() && permissionErrorDetected) {
            throwPermissionsError();
        }

        // Ergebnisliste erzeugen
        List<iPartsWSModel> wsModels = new DwList<>(models.size());
        for (String model : models) {
            iPartsModelId modelId = new iPartsModelId(model);
            iPartsWSModel wsModel = new iPartsWSModel(iPartsModel.getInstance(project, modelId), aggTypeId, modelToASProductClassesMap.get(modelId),
                                                      language, project);
            wsModels.add(wsModel);
        }
        return wsModels;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }

    /**
     * Überprüft, ob die übergebene Produkt den Suchwerten entspricht, wobei <i>productName</i> auch Wildcards enthalten kann.
     *
     * @param product
     * @param onlyRetailRelevantProducts Flag, ob nur retail-relevante Produkte berücksichtigt werden sollen
     * @param aggregateTypeId
     * @param productId
     * @param productName
     * @param productClassIds
     * @param modelTypeId
     * @param project
     * @param language
     * @param permissionError            Rückgabewert; true falls die Token Gültigkeiten verletzt wurden (wird benötigt um später die entsprechende Exception zu werfen)
     * @return
     */
    public static boolean productFitsSearchValues(iPartsProduct product, boolean onlyRetailRelevantProducts, String aggregateTypeId,
                                                  String productId, String productName, List<String> productClassIds, String modelTypeId,
                                                  EtkProject project, String language, iPartsWSUserInfo userInfo, VarParam<Boolean> permissionError) {
        if (onlyRetailRelevantProducts && !product.isRetailRelevant()) {
            return false;
        }

        if (!StrUtils.isEmpty(aggregateTypeId)) {
            if (!aggregateTypeId.equals(product.getAggregateType())) {
                return false;
            }
        }

        if (!StrUtils.isEmpty(productId)) {
            if (!productId.equals(product.getAsId().getProductNumber())) {
                return false;
            }
        }

        // Wenn eine der Aftersales Produktklassen des Produkts mit der gesuchten AS Produktklasse übereinstimmt, dann passt der Vergleich.
        if (!product.isOneProductClassValid(productClassIds)) {
            return false;
        }

        if (!StrUtils.isEmpty(productName)) {
            if (!StrUtils.matchesSqlLike(productName, product.getProductTitle(project).getTextByNearestLanguage(language, project.getDataBaseFallbackLanguages()), false)) {
                return false;
            }
        }

        if (!StrUtils.isEmpty(modelTypeId)) {
            Set<String> modelTypes = iPartsPlugin.isCheckModelVisibility() ? product.getVisibleModelTypes(project) : product.getAllModelTypes(project);
            if (!modelTypes.contains(modelTypeId)) {
                return false;
            }
        }

        // Token Gültigkeitsprüfung erst ganz am Ende, wenn das Produkt prinzipiell zu den Filterkriteriem passt
        boolean validForPermissions = product.isValidForPermissions(project, userInfo.getCountryForValidation(), userInfo.getPermissionsAsMapForValidation());
        permissionError.setValue(!validForPermissions);
        return validForPermissions;
    }

    public iPartsWSGetModelsEndpoint(String endpointUri) {
        super(endpointUri);
    }
}