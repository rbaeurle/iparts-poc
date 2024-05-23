/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts;

import de.docware.apps.etk.base.config.partlist.EtkSectionInfos;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.search.model.EtkPartsSearch;
import de.docware.apps.etk.base.search.model.EtkSearchBaseResult;
import de.docware.apps.etk.base.search.model.EtkSearchModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsEqualPartType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsRetailPartSearch;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractSearchPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.utils.VarParam;

import java.util.Collection;
import java.util.List;

/**
 * Endpoint für den SearchParts-Webservice
 */
public class iPartsWSSearchPartsEndpoint extends iPartsWSAbstractSearchPartsEndpoint<iPartsWSSearchPartsRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/SearchParts";

    private boolean setFilterByRequest;

    private iPartsWSSearchPartsCallback searchCallback;

    public iPartsWSSearchPartsEndpoint(String endpointUri) {
        this(endpointUri, true);
    }

    public iPartsWSSearchPartsEndpoint(String endpointUri, boolean setFilterByRequest) {
        super(endpointUri);
        this.setFilterByRequest = setFilterByRequest;
    }

    public iPartsWSSearchPartsResponse executeWebservice(EtkProject project, iPartsWSSearchPartsRequest requestObject,
                                                         iPartsWSSearchPartsCallback searchCallback) throws RESTfulWebApplicationException {
        this.searchCallback = searchCallback;
        return executeWebservice(project, requestObject);
    }

    @Override
    public iPartsWSSearchPartsResponse executeWebservice(EtkProject project, iPartsWSSearchPartsRequest requestObject) throws RESTfulWebApplicationException {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        requestObject.getIdentContext().checkIfModelValid("identContext", project, userInfo);

        iPartsWSSearchPartsResponse response = new iPartsWSSearchPartsResponse();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        iPartsProduct.setProductStructureWithAggregatesForSession(requestObject.isIncludeAggs());

        String productNo = requestObject.getIdentContext().getProductId();
        List<iPartsWSNavNode> navContext = requestObject.getNavContext();

        iPartsProductId productId = new iPartsProductId(productNo);
        iPartsProduct product = iPartsProduct.getInstance(project, productId);

        // Gültigkeitsprüfung JWT Token und Retail-Relevant
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

        // Filter anhand vom Ident Context setzen (inkl. Datenkarte holen)
        // Befestigungsteile werden falls nötig während der Suche geladen
        iPartsFilter filter;
        if (setFilterByRequest) {
            filter = requestObject.getIdentContext().setFilterForIdentContext(userInfo.getCountry(), false, project);
            filter.setAggModelsFilterActive(requestObject.isIncludeAggs());
        } else {
            filter = iPartsFilter.get();
        }
        iPartsEqualPartType equalPartType = filter.getEqualPartTypeForMainModel();

        EtkSearchModel searchModel = setupSearchModel(project, product, navContext, isPartNo, equalPartType);

        try {
            // Zusätzliche benötigte Ergebnisfelder (z.B. für die Filter), die gleich im Select mit abgefragt werden sollen
            EtkSectionInfos resultFields = setupResultFields(project);
            searchModel.setGridResultFields(resultFields);

            // Bestimmen, welche Aggregate zusätzlich noch durchsucht werden sollen
            Collection<iPartsProduct> additionalAggregates = null;
            if (requestObject.isIncludeAggs()) {
                additionalAggregates = iPartsFilter.get().getCurrentDataCard().getSubDatacardsProducts(project);
            }

            // Optionale Einschränkung auf KG/TU- bzw. EinPAS-Knoten
            KgTuId optionalKgTuId = iPartsWSNavHelper.getKgTuId(navContext);
            EinPasId optionalEinPasId = null;
            if (optionalKgTuId == null) {
                optionalEinPasId = iPartsWSNavHelper.getEinPasId(navContext);
            }

            final EtkPartsSearch search = new iPartsRetailPartSearch(searchModel, true, optionalKgTuId, optionalEinPasId,
                                                                     null, requestObject.isIncludeAggs(), requestObject.isIncludeSAs(),
                                                                     true, additionalAggregates);
//        search.setSearchValuesDisjunction(true);  // mehrere Suchbegriffe werden verodert (= ein Begriff der passt reicht); für künftige Erweiterung des WS wichtig

            VarParam<Boolean> hasMoreResults = new VarParam<>(false);
            Collection<EtkSearchBaseResult> searchResults = executeSearch(search, requestObject.getSearchText(), hasMoreResults,
                                                                          project);
            iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
            List<iPartsWSPartResult> resultList = createResultList(searchResults, userInfo, project, product, productStructures,
                                                                   requestObject.isIncludeES2Keys(), equalPartType, searchCallback);
            response.setSearchResults(resultList);
            response.setMoreResults(hasMoreResults.getValue());
        } finally {
            if (setFilterByRequest) {
                // Filter wieder zurücksetzen, damit die Session wieder ohne Filter weiterverwendet werden kann (z.B. für die Unittests)
                iPartsFilter.disableAllFilters();
            }
        }
        return response;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.MATERIAL)
            || (dataType == iPartsDataChangedEventByEdit.DataType.PART_LIST) || (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }
}