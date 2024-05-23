/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodeltypes;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodels.iPartsWSGetModelsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSModelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.utils.VarParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Endpoint für den GetModelTypes-Webservice
 */
public class iPartsWSGetModelTypesEndpoint extends iPartsWSAbstractEndpoint<iPartsWSGetModelTypesRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/ident/GetModelTypes";

    public iPartsWSGetModelTypesEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected iPartsWSGetModelTypesResponse executeWebservice(EtkProject project, iPartsWSGetModelTypesRequest requestObject) {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        String language = userInfo.getLanguage();
        List<iPartsProduct> productList = iPartsProduct.getAllProducts(project);

        // Produkte filtern und alle Typkennzahlen der passenden Produkte zu modelTypeIds hinzufügen
        Set<String> modelTypeIds = new TreeSet<>();
        boolean onlyRetailRelevantProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();
        boolean permissionErrorDetected = false;
        boolean checkModelVisibility = iPartsPlugin.isCheckModelVisibility();
        for (iPartsProduct product : productList) {
            VarParam<Boolean> permissionError = new VarParam<>(false);
            if (iPartsWSGetModelsEndpoint.productFitsSearchValues(product, onlyRetailRelevantProducts, requestObject.getAggTypeId(),
                                                                  requestObject.getProductId(), requestObject.getProductName(),
                                                                  requestObject.getProductClassIds(), null, project, language,
                                                                  userInfo, permissionError)) {
                Set<String> modelTypes = checkModelVisibility ? product.getVisibleModelTypes(project) : product.getAllModelTypes(project);
                modelTypeIds.addAll(modelTypes);
            }
            permissionErrorDetected |= permissionError.getValue();
        }

        // Das Ergebnis ist leer, aber es hat Daten gegeben die durch Berechtigungsprüfung ausgefiltert wurden
        if (!productList.isEmpty() && modelTypeIds.isEmpty() && permissionErrorDetected) {
            throwPermissionsError();
        }

        // Typkennzahlen zur Ergebnisliste hinzufügen
        List<iPartsWSModelType> modelTypesList = new ArrayList<>(productList.size());
        for (String modelTypeId : modelTypeIds) {
            iPartsWSModelType modelType = new iPartsWSModelType(modelTypeId, project, language);


            // Baumuster hinzufügen falls gewünscht
            if (requestObject.isIncludeModels()) {
                modelType.setModels(iPartsWSGetModelsEndpoint.getWSModels(requestObject.getAggTypeId(), requestObject.getProductId(),
                                                                          requestObject.getProductName(), requestObject.getProductClassIds(),
                                                                          modelTypeId, project, language, userInfo));
            }

            modelTypesList.add(modelType);
        }

        // Response erzeugen
        iPartsWSGetModelTypesResponse modelTypes = new iPartsWSGetModelTypesResponse();
        modelTypes.setModelTypes(modelTypesList);
        return modelTypes;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }
}