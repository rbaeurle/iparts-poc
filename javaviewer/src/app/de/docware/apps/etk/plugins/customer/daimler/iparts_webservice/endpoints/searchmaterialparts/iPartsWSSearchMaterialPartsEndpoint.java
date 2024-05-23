/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchmaterialparts;

import de.docware.apps.etk.base.config.partlist.EtkSectionInfos;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.search.model.EtkPartsSearch;
import de.docware.apps.etk.base.search.model.EtkSearchBaseResult;
import de.docware.apps.etk.base.search.model.EtkSearchModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsEqualPartType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsEqualPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsRetailPartSearch;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractSearchPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts.iPartsWSSearchPartsResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSSpecialProductHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.utils.VarParam;

import java.util.Collection;
import java.util.List;

/**
 * Endpoint für den SearchParts-Webservice
 */
public class iPartsWSSearchMaterialPartsEndpoint extends iPartsWSAbstractSearchPartsEndpoint<iPartsWSSearchMaterialPartsRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/material/SearchMaterialParts";

    public iPartsWSSearchMaterialPartsEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected RESTfulTransferObjectInterface executeWebservice(EtkProject project, iPartsWSSearchMaterialPartsRequest requestObject) throws RESTfulWebApplicationException {
        iPartsWSSearchPartsResponse response = new iPartsWSSearchPartsResponse();

        iPartsWSUserInfo userInfo = requestObject.getUser();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        String productNo = requestObject.getProductId();
        List<iPartsWSNavNode> navContext = requestObject.getNavContext();

        iPartsProductId productId = new iPartsProductId(productNo);
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        iPartsWSSpecialProductHelper.checkIfSpecialProduct(product);

        String assortmentClassId = requestObject.getAssortmentClassId();
        String productClassId = requestObject.getProductClassId();
        // Die AssortmentClassId wird nur noch in der Übergangsphase, bis alle Konsumenten auf das neue Attribut
        // productClassId migriert haben, verwendet. Die Id wird direkt mit den AS-Produktklassen am Produktstamm
        // verglichen!!
        String filterValue = iPartsWSSpecialProductHelper.handleAssortmentAndProductClassId(assortmentClassId, productClassId, product);


        // Gültigkeitsprüfung für Token und RetailRelevant
        boolean isValid = checkRetailRelevantAndPermissionWithException(project, product, userInfo);
        if (!isValid) {
            return response;
        }

        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        VarParam<String> resultSearchText = new VarParam<>();
        boolean isPartNo = numberHelper.testPartNumber(requestObject.getSearchText(), resultSearchText, project);

        // Nicht nur SearchFields sondern auch SearchText ändern!
        if (!resultSearchText.getValue().equals(requestObject.getSearchText())) {
            requestObject.setSearchText(resultSearchText.getValue());
        }

        // Hier gibt es keine Datenkarte und damit Baumuster -> AS-Produktklassen des Produkts auswerten für Gleichteile-Typ
        iPartsEqualPartType equalPartType = iPartsEqualPartType.NONE;
        if (iPartsEqualPartsHelper.SHOW_EQUAL_PARTS) {
            if (product.isCarAndVanProduct() && !product.isTruckAndBusProduct()) {
                equalPartType = iPartsEqualPartType.MB;
            } else if (!product.isCarAndVanProduct() && product.isTruckAndBusProduct()) {
                equalPartType = iPartsEqualPartType.DT;
            }
        }

        // Falls es sich um eine Teilenummer handelt, muss nur ein Search field gesetzt werden.
        EtkSearchModel searchModel = setupSearchModel(project, product, navContext, isPartNo, equalPartType);

        // Filter für Sortimentsklassen-Gültigkeit setzen
        if (!filterValue.isEmpty()) {
            iPartsWSSpecialProductHelper.setSpecialProductFilterActive(filterValue, requestObject.getIdentContext(), userInfo.getCountry(),
                                                                       project);
        }

        try {
            // Zusätzliche benötigte Ergebnisfelder (z.B. für die Filter), die gleich im Select mit abgefragt werden sollen
            EtkSectionInfos resultFields = setupResultFields(project);
            searchModel.setGridResultFields(resultFields);

            // Optionale Einschränkung auf KG/TU- bzw. EinPAS-Knoten
            KgTuId optionalKgTuId = iPartsWSNavHelper.getKgTuId(navContext);
            EinPasId optionalEinPasId = null;
            if (optionalKgTuId == null) {
                optionalEinPasId = iPartsWSNavHelper.getEinPasId(navContext);
            }

            final EtkPartsSearch search = new iPartsRetailPartSearch(searchModel, true, optionalKgTuId, optionalEinPasId,
                                                                     null, false, false, true, null);

            VarParam<Boolean> hasMoreResults = new VarParam<>(false);
            Collection<EtkSearchBaseResult> searchResults = executeSearch(search, requestObject.getSearchText(), hasMoreResults,
                                                                          project);
            iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
            List<iPartsWSPartResult> resultList = createResultList(searchResults, userInfo, project, product, productStructures,
                                                                   false, equalPartType, null);
            response.setSearchResults(resultList);
            response.setMoreResults(hasMoreResults.getValue());
        } finally {
            // Filter wieder zurücksetzen, damit die Session wieder ohne Filter weiterverwendet werden kann (z.B. für die Unittests)
            iPartsFilter.disableAllFilters();
        }
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