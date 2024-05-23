/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialparts;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPartlistTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getparts.iPartsWSGetPartsResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractGetPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSFilteredPartListsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSSpecialProductHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;

import java.util.Collection;

/**
 * Endpoint für den GetMaterialParts-Webservice
 * Beispiel siehe Confluence: https://confluence.docware.de/confluence/x/2YF8AQ
 */
public class iPartsWSGetMaterialPartsEndpoint extends iPartsWSAbstractGetPartsEndpoint<iPartsWSGetMaterialPartsRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/material/GetMaterialParts";

    public iPartsWSGetMaterialPartsEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected iPartsWSGetPartsResponse executeWebservice(EtkProject project, iPartsWSGetMaterialPartsRequest requestObject) {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        String language = userInfo.getLanguage();
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
            return new iPartsWSGetPartsResponse();
        }
        iPartsWSSpecialProductHelper.checkIfSpecialProduct(product);

        // Filter setzen
        iPartsWSSpecialProductHelper.setSpecialProductFilterActive(filterValue, requestObject.getIdentContext(), userInfo.getCountry(),
                                                                   project);

        AssemblyId assemblyId = getAssemblyForNavContext(product, productStructures, null, requestObject.getNavContext(),
                                                         language, project, true, filterValue, requestObject.getIdentContext(),
                                                         userInfo.getSpecialPermissions());
        return createResponse(assemblyId, product, requestObject, language, null, userInfo.getCountry(), project);
    }

    @Override
    protected Collection<EtkDataPartListEntry> getFilteredPartListEntries(EtkDataAssembly assembly, iPartsProduct product,
                                                                          iPartsWSGetMaterialPartsRequest requestObject) {
        return iPartsWSFilteredPartListsCache.getFilteredPartListEntries(assembly, iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS, product,
                                                                         requestObject.getAssortmentClassId(), requestObject.getIdentContext(),
                                                                         this);
    }

    @Override
    protected iPartsWSIdentContext getIdentContext(iPartsWSGetMaterialPartsRequest requestObject) {
        return null;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.PART_LIST) ||
            (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }
}