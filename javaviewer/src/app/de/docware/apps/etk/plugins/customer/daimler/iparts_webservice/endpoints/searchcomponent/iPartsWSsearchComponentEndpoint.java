/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchcomponent;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkSectionInfos;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.search.model.EtkPartResult;
import de.docware.apps.etk.base.search.model.EtkPartsSearch;
import de.docware.apps.etk.base.search.model.EtkSearchBaseResult;
import de.docware.apps.etk.base.search.model.EtkSearchModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductSAs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsEqualPartType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsRetailPartSearch;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithDBDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractSearchPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSNavHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.*;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Endpoint für den SearchComponent-Webservice
 * Dieser Webservice soll nicht verbaute aber erlaubte Ausstattungen ermitteln.
 */
public class iPartsWSsearchComponentEndpoint extends iPartsWSAbstractSearchPartsEndpoint<iPartsWSsearchComponentRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/SearchComponent";

    public iPartsWSsearchComponentEndpoint(String endpointUri) {
        super(endpointUri);
    }

    private void setFilterAndIdentContext(EtkProject project, iPartsWSIdentContext identContext, boolean includeAggs, String country) {
        // Es soll nur der Baumuster-Filter aktiv sein, deshalb werden hier die filterOptions überschrieben
        iPartsWSFilterOptions filterOptions = new iPartsWSFilterOptions();
        filterOptions.setModel(true);
        filterOptions.setSaVersion(true); // SA-Strich-Filter muss für die Filterung der SAAs in den freien SAs aktiv sein
        identContext.setFilterOptions(filterOptions);
        iPartsFilter filter = identContext.setFilterForIdentContext(country, false, project);
        filter.setAggModelsFilterActive(includeAggs);

        // Beim SearchComponent WS soll die Konfiguration für die Ausgabe der freien SAs nicht einbezogen werden
        filter.setIgnoreLooseSaConfiguration(true);
    }

    @Override
    protected RESTfulTransferObjectInterface executeWebservice(EtkProject project, iPartsWSsearchComponentRequest requestObject) throws RESTfulWebApplicationException {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        requestObject.getIdentContext().checkIfModelValid("identContext", project, userInfo);

        iPartsWSsearchComponentResponse response = new iPartsWSsearchComponentResponse();
        String language = userInfo.getLanguage();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        boolean includeAggs = requestObject.isIncludeAggs();
        iPartsProduct.setProductStructureWithAggregatesForSession(includeAggs);

        String productNo = requestObject.getIdentContext().getProductId();
        iPartsProductId productId = new iPartsProductId(productNo);
        iPartsProduct product = iPartsProduct.getInstance(project, productId);

        // Produkt ist nicht retail-relevant und es sollen nur retail-relevante Produkte berücksichtigt werden -> leere Trefferliste
        if (!product.isRetailRelevant() && iPartsWebservicePlugin.isOnlyRetailRelevantProducts()) {
            return response;
        }

        EtkSearchModel searchModel = setupSearchModel(project, product, null, false, null);

        // Filter anhand vom Ident Context setzen (inkl. Datenkarte holen)
        // Befestigungsteile werden falls nötig während der Suche geladen
        iPartsWSIdentContext identContext = requestObject.getIdentContext();
        setFilterAndIdentContext(project, identContext, includeAggs, userInfo.getCountry());

        try {
            // Zusätzliche benötigte Ergebnisfelder (z.B. für die Filter), die gleich im Select mit abgefragt werden sollen
            EtkSectionInfos resultFields = setupResultFields(project);
            searchModel.setGridResultFields(resultFields);

            // Bestimmen, welche Aggregate zusätzlich noch durchsucht werden sollen
            Collection<iPartsProduct> additionalAggregates = null;
            if (includeAggs) {
                additionalAggregates = iPartsFilter.get().getCurrentDataCard().getSubDatacardsProducts(project);
            }

            final EtkPartsSearch search = new iPartsRetailPartSearch(searchModel, true, null, null, null, includeAggs,
                                                                     requestObject.isIncludeSAs(), false, additionalAggregates);

//        search.setSearchValuesDisjunction(true);  // mehrere Suchbegriffe werden verodert (= ein Begriff der passt reicht); für künftige Erweiterung des WS wichtig

            VarParam<Boolean> hasMoreResults = new VarParam<>(false);

            String searchText = StrUtils.trimRight(requestObject.getSearchText()); // Leerzeichen nur rechts entfernen da es auch Eingaben wie " 38092/01" geben kann

            // SAA-Formatierungszeichen "/" immer entfernen, um beliebige (falsche) SAA-Formatierungen zu erlauben
            String searchTextWithoutSaaFormatChars = StrUtils.removeCharsFromString(searchText, new char[]{ '.', '/' });

            // Z für SAA bei Bedarf voranstellen, falls die Länge von searchText ohne SAA-Formatierungszeichen <= 9 ist
            // (dann muss es eine SAA und kein Baukasten sein)
            if ((searchTextWithoutSaaFormatChars.length() <= 9) && !searchText.startsWith("Z")) {
                // SAA befindet sich in der visualisierten Darstellungsform (sollte dann aber eigentlich auch schon ein führendes Z haben)
                if (searchText.contains(".")) {
                    searchText = "Z " + searchText;
                } else {
                    searchText = "Z" + searchText;
                }
            }

            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            searchText = numberHelper.unformatSaaBkForDB(project, searchText, true);

            Collection<EtkSearchBaseResult> searchResults = executeSearch(search, searchText, hasMoreResults, project);

            iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
            List<iPartsWSSAAResult> resultList = createSAAResultList(searchResults, language, project, product, productStructures,
                                                                     identContext, includeAggs, userInfo);
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
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.MATERIAL)
            || (dataType == iPartsDataChangedEventByEdit.DataType.PART_LIST) || (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }

    @Override
    protected EtkDisplayFields setupSearchFields(boolean isPartNo, iPartsEqualPartType equalPartType) {
        EtkDisplayFields searchFields = new EtkDisplayFields();
        EtkDisplayField searchField;

        searchField = new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_SA_VALIDITY), false, true);
        searchFields.addFeld(searchField);

        return searchFields;
    }

    @Override
    protected String getResultMapKey(EtkPartResult partResult) {
        // Relevant ist nur die Baugruppe, nicht ein einzelner Stücklisteneintrag
        String resultMapKey = partResult.getEntry().getAsId().getOwnerAssemblyId().toString("|");

        // Bei freien SAs auch den Einbauort (Produkt und KG) berücksichtigen
        DBDataObject resultDataProductSas = iPartsSearchVirtualDatasetWithDBDataset.getResultDataProductSAs(partResult.getEntry());
        if (resultDataProductSas != null) {
            resultMapKey += resultDataProductSas.getFieldValue(iPartsConst.FIELD_DPS_PRODUCT_NO) + "|" + resultDataProductSas.getFieldValue(iPartsConst.FIELD_DPS_KG);
        }
        return resultMapKey;
    }

    protected String getCacheKeyForResponse(iPartsWSSAAResult responseEntry) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getCacheKeyForResponseCache(null, responseEntry.getIdentContext().createCacheKeyObjectsForResponseCache()));
        for (iPartsWSNavNode navNode : responseEntry.getNavContext()) {
            stringBuilder.append(getCacheKeyForResponseCache(null, navNode.createCacheKeyObjectsForResponseCache()));
        }
        return stringBuilder.toString();
    }

    protected List<iPartsWSSAAResult> createSAAResultList(Collection<EtkSearchBaseResult> searchResults, String language,
                                                          EtkProject project, iPartsProduct product, iPartsProductStructures productStructures,
                                                          iPartsWSIdentContext identContext, boolean includeAggs, iPartsWSUserInfo userInfo) {
        List<iPartsWSSAAResult> resultList = new DwList<>();

        // Für jedes Modul nur einen Eintrag im Ergebnis erzeugen
        Set<String> foundModules = new HashSet<>();

        // Jede Kombination aus IdentContext und NavContext soll nur einmal im Ergebnis enthalten sein
        Set<String> resultKeys = new HashSet<>();

        // Flag, um unnötig häufiges Setzen vom Filter inkl. der Erzeugung der (Baumuster-)Datenkarte für den IdentContext
        // zu vermeiden
        setFilterAndIdentContext(project, identContext, includeAggs, userInfo.getCountry());
        boolean isFilterValidForIdentContext = true;

        // Mögliche Aggregate-Baumuster bestimmen bei includeAggs
        Set<String> aggregateModelNumbers = null;

        boolean permissionErrorDetected = false;
        // Response erstellen
        for (EtkSearchBaseResult searchBaseResultEntry : searchResults) {

            // Daten aus Stücklisteneintrag
            EtkDataPartListEntry partListEntry = ((EtkPartResult)searchBaseResultEntry).getEntry();

            List<iPartsWSNavNode> navContext;
            String partListEntryProductNr;
            String partListEntryKgSa = null;

            String moduleNumber = partListEntry.getAsId().getKVari();
            if (!foundModules.contains(moduleNumber)) { // Liefert für freie SAs immer false, weil dort der Einbauort (Produkt und KG) noch im Schlüssel enthalten ist
                // Daten aus DA_MODULES_EINPAS
                iPartsDataModuleEinPAS dataModuleEinPAS = iPartsSearchVirtualDatasetWithDBDataset.getResultDataModuleEinPAS(partListEntry);
                if (dataModuleEinPAS != null) { // Treffer im Produkt über normale Verortung
                    navContext = iPartsWSNavHelper.createNavContext(product, productStructures, dataModuleEinPAS, language, project);

                    partListEntryProductNr = dataModuleEinPAS.getAsId().getProductNumber();
                } else {
                    // Daten aus DA_PRODUCT_SAS
                    iPartsDataProductSAs dataProductSAs = iPartsSearchVirtualDatasetWithDBDataset.getResultDataProductSAs(partListEntry);
                    if (dataProductSAs != null) { // Treffer in einer SA
                        navContext = iPartsWSNavHelper.createNavContext(product, productStructures, dataProductSAs, partListEntry.getOwnerAssemblyId(),
                                                                        language, project);
                        partListEntryProductNr = dataProductSAs.getAsId().getProductNumber();
                        partListEntryKgSa = dataProductSAs.getAsId().getKG();
                    } else {
                        continue; // Keine Verortung gefunden -> Treffer überspringen
                    }
                }
            } else {
                continue; // bereits enthalten -> Treffer überspringen
            }

            if (!StrUtils.isEmpty(partListEntryProductNr)) {
                String foundModulesKey = moduleNumber;
                if (partListEntryKgSa != null) { // Bei freien SAs auch den Einbauort (Produkt und KG) berücksichtigen
                    foundModulesKey += "|" + partListEntryProductNr + "|" + partListEntryKgSa;
                    if (foundModules.contains(foundModulesKey)) {
                        continue; // bereits enthalten -> Treffer überspringen
                    }
                }
                if (partListEntryProductNr.equals(product.getAsId().getProductNumber())) {
                    iPartsWSSAAResult iPartsResultEntry = new iPartsWSSAAResult(identContext, navContext);
                    String key = getCacheKeyForResponse(iPartsResultEntry);
                    if (!resultKeys.contains(key)) {
                        resultKeys.add(key);
                        resultList.add(iPartsResultEntry);
                        foundModules.add(foundModulesKey);
                    }
                } else {
                    // Aggregate Treffer
                    iPartsProduct partListEntryProduct = iPartsProduct.getInstance(project, new iPartsProductId(partListEntryProductNr));
                    // Token Gültigkeitsprüfung
                    boolean onlyRetailRelevantProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();
                    if (onlyRetailRelevantProducts && !partListEntryProduct.isRetailRelevant()) {
                        continue;
                    }
                    boolean validForPermissions = partListEntryProduct.isValidForPermissions(project, userInfo.getCountryForValidation(), userInfo.getPermissionsAsMapForValidation());
                    permissionErrorDetected |= !validForPermissions;
                    if (!validForPermissions) {
                        continue;
                    }
                    // Baumuster für das gefundene Produkt bestimmen
                    Set<String> modelsForPartListEntry = partListEntryProduct.getModelNumbers(project);

                    // Relevante Baumuster für die Filterung bestimmen über die (Baumuster-)Datenkarte vom ursprünglichen IdentContext
                    if (!isFilterValidForIdentContext) {
                        setFilterAndIdentContext(project, identContext, includeAggs, userInfo.getCountry());
                        isFilterValidForIdentContext = true;
                    }

                    if (aggregateModelNumbers == null) {
                        AbstractDataCard filterDatacard = iPartsFilter.get().getCurrentDataCard();
                        if (filterDatacard instanceof VehicleDataCard) {
                            aggregateModelNumbers = ((VehicleDataCard)filterDatacard).getAggregateModelNumbers().getAllCheckedValues();
                        } else {
                            aggregateModelNumbers = new TreeSet<>();
                            aggregateModelNumbers.add(filterDatacard.getModelNo());
                        }
                    }

                    // Prüfen welche Baumuster aus den relevanten Baumustern für die Filterung mit den Baumustern
                    // des gefundenen Produktes übereinstimmen
                    Set<String> commonModels = new TreeSet<>();
                    for (String partListModel : modelsForPartListEntry) {
                        if (aggregateModelNumbers.contains(partListModel)) {
                            commonModels.add(partListModel);
                        }
                    }

                    // Pro gemeinsamem Baumuster einen IdentContext erzeugen
                    for (String model : commonModels) {
                        iPartsWSIdentContext aggregateIdentContext = new iPartsWSIdentContext(iPartsModel.getInstance(project, new iPartsModelId(model)),
                                                                                              partListEntryProduct, false, userInfo, project, true);

                        // Mit diesem erzeugten IdentContext filtern, um zu prüfen, ob der gefundene Stücklisteneintrag
                        // bei diesem Baumuster auch wirklich sichtbar ist
                        setFilterAndIdentContext(project, aggregateIdentContext, false, userInfo.getCountry());
                        isFilterValidForIdentContext = false;
                        boolean valid = iPartsFilter.get().checkFilter(partListEntry);
                        if (valid) {
                            iPartsWSSAAResult iPartsResultEntry = new iPartsWSSAAResult(aggregateIdentContext, navContext);
                            String key = getCacheKeyForResponse(iPartsResultEntry);
                            if (!resultKeys.contains(key)) {
                                resultKeys.add(key);
                                resultList.add(iPartsResultEntry);
                                foundModules.add(foundModulesKey);
                            }
                        }
                    }

                }
            }
        }

        // Das Ergebnis ist leer, aber es hat Daten gegeben die durch Berechtigungsprüfung ausgefiltert wurden
        if (!searchResults.isEmpty() && resultList.isEmpty() && permissionErrorDetected) {
            throwPermissionsError();
        }

        return resultList;
    }
}
