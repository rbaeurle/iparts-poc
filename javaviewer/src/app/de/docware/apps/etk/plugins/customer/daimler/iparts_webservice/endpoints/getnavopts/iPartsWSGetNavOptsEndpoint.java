/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getnavopts;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Endpoint für den GetNavOpts-Webservice
 */
public class iPartsWSGetNavOptsEndpoint extends iPartsWSAbstractEndpoint<iPartsWSGetNavOptsRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/GetNavOpts";

    public iPartsWSGetNavOptsEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected iPartsWSGetNavOptsResponse executeWebservice(EtkProject project, iPartsWSGetNavOptsRequest requestObject) {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        requestObject.getIdentContext().checkIfModelValid("identContext", project, userInfo);

        // Integrierte Navigation
        if (requestObject.isIntegratedNavigation()) {
            iPartsProduct.setProductStructureWithAggregatesForSession(true);
        }

        iPartsWSGetNavOptsResponse response = new iPartsWSGetNavOptsResponse();

        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);
        String productNumber = requestObject.getIdentContext().getProductId();
        iPartsProductId productId = new iPartsProductId(productNumber);
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
        Set<String> modelNumbers = new HashSet<>(1);
        modelNumbers.add(requestObject.getIdentContext().getModelId());
        List<iPartsWSNavNode> navContext = requestObject.getNavContext();

        // Filter anhand vom Ident Context setzen (inkl. Datenkarte aber ohne Befestigungsteile holen)
        requestObject.getIdentContext().setFilterForIdentContext(userInfo.getCountry(), false, project);

        response.setNextNodes(iPartsWSNavHelper.getChildNavNodes(project, product, productStructures, modelNumbers, null,
                                                                 requestObject.getIdentContext(), navContext, this, true,
                                                                 true, true, false, false,
                                                                 userInfo.getSpecialPermissions()));
        if (!iPartsWebservicePlugin.isRMIActive()) {
            response.setTopNodes(iPartsWSNavHelper.getTopNodes(project, product, productStructures, requestObject.getUser().getCountry(),
                                                               navContext));
        }
        return response;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.PART_LIST) ||
            (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }
}