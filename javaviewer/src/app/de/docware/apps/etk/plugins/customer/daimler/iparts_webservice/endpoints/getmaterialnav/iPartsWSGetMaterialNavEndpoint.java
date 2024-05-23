/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialnav;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSSpecialProductHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;

import java.util.List;

/**
 * Endpoint für den GetMaterialNav-Webservice
 */
public class iPartsWSGetMaterialNavEndpoint extends iPartsWSAbstractEndpoint<iPartsWSGetMaterialNavRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/material/GetMaterialNav";

    public iPartsWSGetMaterialNavEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected iPartsWSGetMaterialNavResponse executeWebservice(EtkProject project, iPartsWSGetMaterialNavRequest requestObject) {
        iPartsWSGetMaterialNavResponse response = new iPartsWSGetMaterialNavResponse();

        iPartsWSUserInfo userInfo = requestObject.getUser();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        String productNumber = requestObject.getProductId();
        iPartsProductId productId = new iPartsProductId(productNumber);
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);

        // Die AssortmentClassId wird nur noch in der Übergangsphase, bis alle Konsumenten auf das neue Attribut
        // productClassId migriert haben, verwendet. Die Id wird direkt mit den AS-Produktklassen am Produktstamm
        // verglichen!!
        String assortmentClassId = requestObject.getAssortmentClassId();
        String productClassId = requestObject.getProductClassId();
        String filterValue = iPartsWSSpecialProductHelper.handleAssortmentAndProductClassId(assortmentClassId, productClassId, product);

        // Damit alle Webservices gleich funktionieren, auch hier die Gültigkeitsprüfung für Token und RetailRelevant
        boolean isValid = checkRetailRelevantAndPermissionWithException(project, product, userInfo);
        if (!isValid) {
            return response;
        }

        iPartsWSSpecialProductHelper.checkIfSpecialProduct(product);

        List<iPartsWSNavNode> navContext = requestObject.getNavContext();

        // Filter setzen
        iPartsWSSpecialProductHelper.setSpecialProductFilterActive(filterValue, requestObject.getIdentContext(), userInfo.getCountry(),
                                                                   project);

        response.setNextNodes(iPartsWSNavHelper.getChildNavNodes(project, product, productStructures, filterValue,
                                                                 requestObject.getIdentContext(), navContext, this, true,
                                                                 true, true, false,
                                                                 userInfo.getSpecialPermissions()));
        return response;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.PART_LIST) ||
            (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }
}