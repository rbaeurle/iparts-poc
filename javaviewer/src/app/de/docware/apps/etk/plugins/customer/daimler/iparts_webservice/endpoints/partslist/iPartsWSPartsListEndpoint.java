/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPartlistTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleCemat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleCematList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractGetPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.visualNav.iPartsWSVisualNavEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.visualNav.iPartsWSVisualNavRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.*;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.QueryParam;
import de.docware.framework.modules.webservice.restful.annotations.SecurePayloadParam;
import de.docware.framework.modules.webservice.restful.annotations.methods.GET;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Endpoint für den partsList-Webservice (Stücklisten-Aufruf für FIN/VIN oder BM6)
 */
public class iPartsWSPartsListEndpoint extends iPartsWSAbstractGetPartsEndpoint<iPartsWSPartsListRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/partsList";

    private static boolean USE_ALL_MODELS_FOR_VALIDITIES_FILTER = false;

    private iPartsWSIdentEndpoint identEndpoint;
    private iPartsWSVisualNavEndpoint visualNavEndpoint;

    public iPartsWSPartsListEndpoint(String endpointUri) {
        super(endpointUri);
        identEndpoint = new iPartsWSIdentEndpoint("");
        identEndpoint.setIdentCodeAttributeName("finOrVin");
        visualNavEndpoint = new iPartsWSVisualNavEndpoint("");
    }

    @Override
    protected boolean isHttpMethodPostValid() {
        return false;
    }

    /**
     * PartsList verwendet GET mit diversen Query-Parametern in der URL.
     *
     * @return
     */
    @GET
    @Produces(MimeTypes.MIME_TYPE_JSON)
    public RESTfulTransferObjectInterface getPartsList(@QueryParam("model") String model, @QueryParam("finOrVin") String finOrVin,
                                                       @QueryParam("productClassId") String productClassId, @QueryParam("productId") String productId,
                                                       @QueryParam("includeAggs") String includeAggs,
                                                       @QueryParam("includeVisualNav") String includeVisualNav,
                                                       @QueryParam("extendedDescriptions") String extendedDescriptions,
                                                       @QueryParam("reducedInformation") String reducedInformation,
                                                       @QueryParam("images") String images,
                                                       @SecurePayloadParam String securePayload) {
        // Request mit den Parametern erzeugen für die weitere Verarbeitung analog zu POST-Requests
        iPartsWSPartsListRequest request = new iPartsWSPartsListRequest();
        request.setModel(model);
        request.setFinOrVin(finOrVin);
        request.setProductClassId(productClassId);
        request.setProductId(productId);
        request.setIncludeAggs(Boolean.parseBoolean(includeAggs));
        request.setIncludeVisualNav(Boolean.parseBoolean(includeVisualNav));
        request.setExtendedDescriptions(Boolean.parseBoolean(extendedDescriptions));
        request.setReducedInformation(Boolean.parseBoolean(reducedInformation));
        request.setImages(Boolean.parseBoolean(images));
        request.setSecurePayload(securePayload); // SecurePayload muss bei GET explizit gesetzt werden
        request.checkIfValid(""); // checkIfValid() muss bei GET explizit aufgerufen werden
        return handleWebserviceRequestIntern(request);
    }

    @Override
    protected iPartsWSPartsListResponse executeWebservice(EtkProject project, iPartsWSPartsListRequest requestObject) {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        String language = userInfo.getLanguage();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        // Künstlich einen IdentRequest erzeugen
        iPartsProductId productId = null;
        boolean isSpecialProduct = false;
        iPartsWSIdentRequest identRequest = new iPartsWSIdentRequest();
        identRequest.setUser(requestObject.getUser());
        String identCode = null;
        if (StrUtils.isValid(requestObject.getModel())) {
            identCode = requestObject.getModel();
        } else if (StrUtils.isValid(requestObject.getFinOrVin())) {
            identCode = requestObject.getFinOrVin();
        }
        if (StrUtils.isValid(identCode)) {
            identRequest.setIdentCode(identCode);
        } else if (StrUtils.isValid(requestObject.getProductClassId())) {
            productId = new iPartsProductId(requestObject.getProductId());
            iPartsProduct product = iPartsProduct.getInstance(project, productId);
            if (product.isSpecialCatalog()) {
                isSpecialProduct = true;
            } else {
                WSRequestTransferObject.throwRequestError(WSError.REQUEST_SEMANTIC_ERROR, "The attribute 'productClassId' is only valid for special products but '"
                                                                                          + requestObject.getProductId()
                                                                                          + "' is no special product", "");
                return null;
            }
        } else { // Kann eigentlich gar nicht passieren, weil in iPartsWSPartsListRequest.checkIfValid() schon geprüft
            WSRequestTransferObject.checkExactlyOneAttribValid("", new String[]{ "model", "finOrVin", "productClassId" },
                                                               new String[]{ requestObject.getModel(), requestObject.getFinOrVin(),
                                                                             requestObject.getProductClassId() });
        }

        iPartsWSIdentResponse identResponse = null;
        if (!isSpecialProduct) {
            identRequest.setProductId(requestObject.getProductId());

            // ... und damit den IdentEndpoint aufrufen
            identResponse = identEndpoint.executeWebservice(project, identRequest);
        }

        // Response erzeugen
        iPartsWSPartsListResponse response = new iPartsWSPartsListResponse();
        if ((identResponse != null) && identResponse.getIdentContexts().isEmpty()) {
            // Nichts Passendes gefunden -> Response bleibt leer
        } else if ((identResponse != null) && (identResponse.getIdentContexts().size() > 1)) { // Das Produkt ist mehrdeutig
            Set<String> productIds = new LinkedHashSet<>();
            for (iPartsWSIdentContext identContext : identResponse.getIdentContexts()) {
                productIds.add(identContext.getProductId());
            }
            response.setAmbiguousProductIds(productIds);
        } else { // Genau 1 (Spezial-)Produkt -> Stücklisten bestimmen und ausgeben
            iPartsWSIdentContext identContext = null;
            iPartsFilter filter = null;
            if (identResponse != null) {
                identContext = identResponse.getIdentContexts().get(0);

                // includeAggs ist nur bei einer echte Datenkarte zulässig
                if (requestObject.isIncludeAggs() && !identContext.isDatacardExists()) {
                    String message = "The attribute 'includeAggs' is only valid for a FIN or VIN with an existing datacard";
                    throwBadRequestError(WSError.REQUEST_SEMANTIC_ERROR, message, null);
                    return null;
                }

                requestObject.setIdentContext(identContext); // IdentContext im Request speichern für die Verwendung in anderen Methoden
                response.setIdentContext(identContext);

                // Erst hier existiert der identContext
                // In checkValue noch nicht
                iPartsWSIdentContextHelper.checkIfIdentContextValid(identContext);

                // Inkl. Aggregate?
                iPartsProduct.setProductStructureWithAggregatesForSession(requestObject.isIncludeAggs());

                productId = new iPartsProductId(identContext.getProductId());

                filter = identContext.setFilterForIdentContext(userInfo.getCountry(), false, project);
            }
            try {
                iPartsProduct product = iPartsProduct.getInstance(project, productId); // productId vom identResponse bzw. vom Spezial-Produkt

                String productClassFilterValue = null;
                if (isSpecialProduct) {
                    productClassFilterValue = iPartsWSSpecialProductHelper.handleAssortmentAndProductClassId(null, requestObject.getProductClassId(),
                                                                                                             product);
                    // Filter für Sortimentsklassen-Gültigkeit setzen (IdentContext für Spezial-Produkte wird hier nicht
                    // unterstützt, ist aber hier auch nicht notwendig, weil nur der AS-Produktklassen-Filter für den
                    // automatischen Export benötigt wird)
                    iPartsWSSpecialProductHelper.setSpecialProductFilterActive(productClassFilterValue, null, null, project);
                }

                Set<String> modelNumbers = null;
                if ((identContext != null) && (filter != null)) {
                    if (USE_ALL_MODELS_FOR_VALIDITIES_FILTER && requestObject.isIncludeAggs()) {
                        modelNumbers = filter.getCurrentDataCard().getFilterModelNumbers(project);
                    } else {
                        modelNumbers = new HashSet<>(1);
                        modelNumbers.add(identContext.getModelId());
                    }
                }

                // Rekursiv alle Strukturknoten und Stücklisten bestimmen
                iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, productId);
                List<iPartsWSPartsListNavNode> nextNodes = getNextNavNodes(project, requestObject, product, productStructures,
                                                                           modelNumbers, productClassFilterValue, language,
                                                                           iPartsFilter.get(), null);
                response.setNextNodes(nextNodes);

                // Fahrzeugnavigation ausgeben?
                if (requestObject.isIncludeVisualNav() && StrUtils.isValid(identCode) && product.containsVisibleCarPerspectiveTU(project, false)) {
                    iPartsWSVisualNavRequest visualNavRequest = new iPartsWSVisualNavRequest();
                    visualNavRequest.setUser(requestObject.getUser());
                    visualNavRequest.setFinOrVin(identCode);
                    try {
                        response.setVisualNav(visualNavEndpoint.executeWebservice(project, visualNavRequest, false));
                    } catch (Exception e) {
                        Logger.logExceptionWithoutThrowing(logChannelDebug, LogType.ERROR, new RuntimeException("Error while creating visualNav for ident code \""
                                                                                                                + identCode + "\"", e));
                    }
                }
            } finally {
                // Filter wieder zurücksetzen, damit die Session wieder ohne Filter weiterverwendet werden kann (z.B. für die Unittests)
                iPartsFilter.disableAllFilters();
                iPartsProduct.setProductStructureWithAggregatesForSession(false);
            }
        }

        return response;
    }

    private List<iPartsWSPartsListNavNode> getNextNavNodes(EtkProject project, iPartsWSPartsListRequest requestObject,
                                                           iPartsProduct product, iPartsProductStructures productStructures,
                                                           Set<String> modelNumbers, String additionalFilterCacheKey, String language,
                                                           iPartsFilter filter, List<iPartsWSPartsListNavNode> navContext) {
        // BOM-Schlüssel nur bei withExtendedDescriptions setzen
        Map<String, String> bomKeysMap = null;
        if (requestObject.isExtendedDescriptions()) {
            bomKeysMap = new HashMap<>();
        }

        List<iPartsWSPartsListNavNode> nextPartsListNodes = new DwList<>();
        Set<String> specialUserPermissions = requestObject.getUser().getSpecialPermissions();
        List<iPartsWSNavNode> nextNavNodes = iPartsWSNavHelper.getChildNavNodes(project, product, productStructures, modelNumbers,
                                                                                additionalFilterCacheKey, requestObject.getIdentContext(),
                                                                                navContext, this, true, false, false, requestObject.isExtendedDescriptions(),
                                                                                requestObject.isReducedInformation(), specialUserPermissions);
        String mainModelNumber = filter.getCurrentDataCard().getModelNo();
        String countryCode = requestObject.getUser().getCountry();
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        iPartsCatalogNode completeKgTuStructure = null;
        for (iPartsWSNavNode nextNavNode : nextNavNodes) {
            iPartsWSPartsListNavNode nextPartsListNode = new iPartsWSPartsListNavNode(nextNavNode);

            // NavContext für den betrachteten Knoten erzeugen
            List<iPartsWSPartsListNavNode> childNavContext;
            if (navContext == null) {
                childNavContext = new DwList<>();
            } else {
                childNavContext = new DwList<>(navContext);
            }
            childNavContext.add(nextPartsListNode);

            if (nextPartsListNode.isPartsAvailable()) { // Stückliste vorhanden
                iPartsWSPartsListModule module = new iPartsWSPartsListModule();

                // Modul bestimmen
                boolean isSpecialProduct = product.isSpecialCatalog() && StrUtils.isValid(requestObject.getProductClassId());
                AssemblyId assemblyId = getAssemblyForNavContext(product, productStructures, modelNumbers, childNavContext,
                                                                 language, project, isSpecialProduct, null, requestObject.getIdentContext(),
                                                                 requestObject.isExtendedDescriptions(),
                                                                 requestObject.isReducedInformation(),
                                                                 specialUserPermissions);
                if (assemblyId == null) {
                    continue;
                }
                module.setModuleId(assemblyId.getKVari());
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                // Gefilterte Stückliste holen inkl. Laden der Befestigungsteile falls notwendig
                Collection<EtkDataPartListEntry> partListEntries;
                if (isSpecialProduct) {
                    partListEntries = iPartsWSFilteredPartListsCache.getFilteredPartListEntries(assembly, iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS,
                                                                                                product, requestObject.getProductClassId(),
                                                                                                requestObject.getIdentContext(),
                                                                                                this);

                } else {
                    partListEntries = getFilteredPartListEntries(assembly, product, requestObject);
                }

                // EinPAS Daten für Modul nur laden, wenn extendedDescriptions = true und NICHT für RMI.
                Map<PartListEntryId, List<iPartsDataModuleCemat>> einPasDataMap = null;
                if ((requestObject.isExtendedDescriptions()) && (!iPartsWebservicePlugin.isRMIActive())) {
                    einPasDataMap = iPartsDataModuleCematList.loadCematMapForModule(project, assembly.getAsId());
                }

                // Bilder für das Modul laden, wenn der Inputparameter images = true
                if (requestObject.isImages()) {
                    List<iPartsWSImage> imagesForModule = iPartsWSImageHelper.getImagesForModule(assembly, partListEntries, project, language, true);
                    // Nur setzen, wenn nicht leer, sonst wird ein leeres Objekt in der Response übergeben
                    if (!imagesForModule.isEmpty()) {
                        module.setImages(imagesForModule);
                    }
                }

                // Teile für das Modul bestimmen
                module.setParts(getPartsForPartList(assembly, partListEntries, false, language, requestObject.isExtendedDescriptions(),
                                                    mainModelNumber, countryCode, bomKeysMap, requestObject.isReducedInformation(),
                                                    true, einPasDataMap, false, false,
                                                    product.isWireHarnessDataVisible(), project));

                // Ein leeres Modul soll nicht ausgegeben werden
                if (module.getParts().isEmpty()) {
                    continue;
                }

                // Baumuster- und Produkt-Infos setzen (nur notwendig bei Aggregaten)
                if (requestObject.isIncludeAggs() && (assembly instanceof iPartsDataAssembly)) {
                    iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
                    iPartsProductId assemblyProductId = null;
                    // Bei einer freien SA muss die ProductId über die Struktur ermittelt werden
                    if (iPartsAssembly.isSAAssembly()) {
                        String kg = "";
                        for (iPartsWSPartsListNavNode navNode : childNavContext) {
                            iPartsWSNavNode.TYPE firstNodeType = navNode.getTypeAsEnum();
                            if (firstNodeType == iPartsWSNavNode.TYPE.cg_group) {
                                kg = navNode.getId();
                                break;
                            }
                        }
                        String sa = nextNavNode.getId();
                        sa = numberHelper.unformatSaForDB(sa);
                        if (StrUtils.isValid(kg, sa)) {
                            if (completeKgTuStructure == null) {
                                completeKgTuStructure = productStructures.getCompleteKgTuStructure(project, true);
                            }
                            iPartsCatalogNode kgSaNode = completeKgTuStructure.getNode(new KgSaId(kg, sa));
                            if (kgSaNode != null) {
                                assemblyProductId = kgSaNode.getProductId();
                            }
                        }
                    } else {
                        assemblyProductId = iPartsAssembly.getProductIdFromModuleUsage();
                    }
                    // Wenn das Produkt des Moduls sich vom Produkt des Requests unterscheidet, ist es ein Aggregate-Produkt
                    // und die iPartsWSPartsListModule-Datenstruktur muss vollständig ausgefüllt werden
                    if ((assemblyProductId != null) && !Utils.objectEquals(product.getAsId(), assemblyProductId)) {
                        List<AbstractDataCard> dataCards = filter.getRelevantDatacardsForAssembly(iPartsAssembly);
                        if (!dataCards.isEmpty()) { // Es muss eigentlich immer eine passende Datenkarte geben
                            // Aktuell keine Unterstützung für mehrere passende Datenkarten und damit evtl. auch Baumuster
                            // zu einer Stückliste (kann z.B. bei Achse 1 und 2 passieren, die dasselbe Produkt aber unterschiedliche
                            // Baumuster usw. haben können)
                            AbstractDataCard dataCard = dataCards.get(0);
                            String modelNumber = dataCard.getModelNo();
                            module.setModelId(modelNumber);
                            if (dataCard instanceof AggregateDataCard) { // Muss eigentlich immer eine AggregateDataCard sein
                                module.setAggTypeId(((AggregateDataCard)dataCard).getAggregateTypeFromModel(project));
                            }
                            iPartsProduct assemblyProduct = iPartsProduct.getInstance(project, assemblyProductId);
                            module.setProductClassIds(assemblyProduct.getAsProductClasses());
                            module.setProductId(assemblyProduct.getAsId().getProductNumber());
                        }
                    }
                }

                nextPartsListNode.setModule(module);
            } else {
                // Kind-Knoten bestimmen
                nextPartsListNode.setNextNodes(getNextNavNodes(project, requestObject, product, productStructures, modelNumbers,
                                                               additionalFilterCacheKey, language, filter, childNavContext));
            }

            nextPartsListNodes.add(nextPartsListNode);
        }
        return nextPartsListNodes;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.PART_LIST) ||
            (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }

    @Override
    protected iPartsWSIdentContext getIdentContext(iPartsWSPartsListRequest requestObject) {
        return requestObject.getIdentContext();
    }

    @Override
    protected Collection<EtkDataPartListEntry> getFilteredPartListEntries(EtkDataAssembly assembly, iPartsProduct product,
                                                                          iPartsWSPartsListRequest requestObject) {
        return iPartsWSFilteredPartListsCache.getFilteredPartListEntries(assembly, iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS,
                                                                         requestObject.getIdentContext(), this,
                                                                         requestObject.isExtendedDescriptions());
    }
}