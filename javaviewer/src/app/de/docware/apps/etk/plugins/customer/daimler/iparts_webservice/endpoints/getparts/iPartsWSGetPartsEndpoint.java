/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getparts;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPartlistTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractGetPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSFilteredPartListsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkDataArray;

import java.util.*;

/**
 * Endpoint für den GetParts-Webservice
 */
public class iPartsWSGetPartsEndpoint extends iPartsWSAbstractGetPartsEndpoint<iPartsWSGetPartsRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/GetParts";

    public iPartsWSGetPartsEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected iPartsWSGetPartsResponse executeWebservice(EtkProject project, iPartsWSGetPartsRequest requestObject) {
        requestObject.getIdentContext().checkIfModelValid("identContext", project, requestObject.getUser());

        // Integrierte Navigation
        if (requestObject.isIntegratedNavigation()) {
            iPartsProduct.setProductStructureWithAggregatesForSession(true);
        }

        iPartsWSUserInfo userInfo = requestObject.getUser();
        String language = userInfo.getLanguage();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        String productNumber = requestObject.getIdentContext().getProductId();
        iPartsProductId productId = new iPartsProductId(productNumber);
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
        Set<String> modelNumbers = new HashSet<>(1);
        modelNumbers.add(requestObject.getIdentContext().getModelId());

        // Filter anhand vom Ident Context setzen
        iPartsFilter filter = requestObject.getIdentContext().setFilterForIdentContext(userInfo.getCountry(), false, project);

        AssemblyId assemblyId = getAssemblyForNavContext(product, productStructures, modelNumbers, requestObject.getNavContext(),
                                                         language, project, false, null, requestObject.getIdentContext(),
                                                         userInfo.getSpecialPermissions());
        return createResponse(assemblyId, product, requestObject, language, filter.getCurrentDataCard().getModelNo(),
                              userInfo.getCountry(), project);
    }

    @Override
    protected Collection<EtkDataPartListEntry> getFilteredPartListEntries(EtkDataAssembly assembly, iPartsProduct product,
                                                                          iPartsWSGetPartsRequest requestObject) {
        iPartsWSIdentContext identContext = requestObject.getIdentContext();

        // DAIMLER-7343: Wenn die Filter-Optionen komplett leer sind, dann die Baumuster-Gültigkeit bei DIALOG-Stücklisten
        // für alle Baumuster des Produkts analog zur Baumusterauswertung in iPartsEdit berechnen
        if ((identContext.getFilterOptions() != null) && identContext.getFilterOptions().isEmpty()) {
            if ((assembly instanceof iPartsDataAssembly)) {
                iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                if (iPartsAssembly.getDocumentationType().isPKWDocumentationType()) {
                    EtkProject project = assembly.getEtkProject();
                    Set<String> modelNumbers = product.getModelNumbers(project);
                    if (!modelNumbers.isEmpty()) {
                        // Nur den Baumuster-Filter aktivieren
                        final iPartsFilterSwitchboard filterForModelEvaluationSwitchboard = new iPartsFilterSwitchboard();
                        filterForModelEvaluationSwitchboard.setMainSwitchActive(true);
                        filterForModelEvaluationSwitchboard.setFilterActivated(iPartsFilterSwitchboard.FilterTypes.MODEL, true);
                        iPartsFilter filterForModelValidity = new iPartsFilter();
                        filterForModelValidity.setSwitchboardState(filterForModelEvaluationSwitchboard);

                        // Stückliste zunächst klonen, dann mit den obligatorischen Filtern für Webservices filtern und
                        // danach die Baumustergültigkeiten bestimmen
                        iPartsDataAssembly assemblyWithModelValidity = (iPartsDataAssembly)iPartsAssembly.cloneMe(project);
                        List<EtkDataPartListEntry> partListEntriesWithModelValidity = assemblyWithModelValidity.getPartList(iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS);
                        assemblyWithModelValidity.clearAllFactoryDataForRetailForPartList();
                        Map<EtkDataPartListEntry, EtkDataArray> partListEntryToModelValidityMap = new HashMap<>();

                        // Für alle Baumuster aus modelNumbers eine virtuelle Baumuster-Datenkarte erzeugen, damit filtern
                        // und das Filterergebnis in modelValidity speichern
                        for (String modelNumber : modelNumbers) {
                            // Baumuster-Datenkarte erzeugen und im Filter setzen
                            filterForModelValidity.setDataCardByModel(modelNumber, project);

                            for (EtkDataPartListEntry partListEntry : partListEntriesWithModelValidity) {
                                // Baumustergültigkeit nicht direkt am Feld K_MODEL_VALIDITY setzen, weil dies bei den
                                // folgenden Baumustern dazu führen würde, dass der Stücklisteneintrag aufgrund der
                                // Baumustergültigkeit ausgefiltert wird -> modelValidity zunächst nur in Map speichern
                                EtkDataArray modelValidity = partListEntryToModelValidityMap.get(partListEntry);
                                if (modelValidity == null) {
                                    modelValidity = partListEntry.getFieldValueAsArray(FIELD_K_MODEL_VALIDITY);
                                    partListEntryToModelValidityMap.put(partListEntry, modelValidity);
                                }

                                // Den Filter ausführen und die Baumustergültigkeit ergänzen falls das Filterergebnis positiv ist
                                if (filterForModelValidity.checkFilter(partListEntry)) {
                                    modelValidity.add(modelNumber);
                                }
                            }

                            // Gefilterte Werkseinsatzdaten und darin v.a. auch das für die Filterung verwendete Baumuster zurücksetzen
                            assemblyWithModelValidity.clearAllFactoryDataForRetailForPartList();
                        }

                        // Baumustergültigkeiten für alle Stücklisteneinträge setzen
                        for (Map.Entry<EtkDataPartListEntry, EtkDataArray> partListEntryToModelValidity : partListEntryToModelValidityMap.entrySet()) {
                            partListEntryToModelValidity.getKey().setFieldValueAsArray(FIELD_K_MODEL_VALIDITY, partListEntryToModelValidity.getValue(),
                                                                                       DBActionOrigin.FROM_DB);
                        }

                        return partListEntriesWithModelValidity;
                    }
                }
            }
        }
        return iPartsWSFilteredPartListsCache.getFilteredPartListEntries(assembly,
                                                                         iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS,
                                                                         identContext, this, false);
    }

    @Override
    protected iPartsWSIdentContext getIdentContext(iPartsWSGetPartsRequest requestObject) {
        return requestObject.getIdentContext();
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