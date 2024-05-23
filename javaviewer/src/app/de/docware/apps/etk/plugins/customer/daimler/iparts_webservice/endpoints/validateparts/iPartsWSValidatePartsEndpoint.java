/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.validateparts;

import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts.iPartsWSSearchPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts.iPartsWSSearchPartsRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSIdentContextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSValidateResult;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.framework.modules.gui.session.SessionType;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.QueryParam;
import de.docware.framework.modules.webservice.restful.annotations.SecurePayloadParam;
import de.docware.framework.modules.webservice.restful.annotations.methods.GET;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Endpoint für den validateParts-Webservice (Validierung mehrerer Teilenummern gegen eine FIN)
 */
public class iPartsWSValidatePartsEndpoint extends iPartsWSAbstractEndpoint<iPartsWSValidatePartsRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/validate";

    private iPartsWSIdentEndpoint identEndpoint;
    private iPartsWSSearchPartsEndpoint searchPartsEndpoint;

    public iPartsWSValidatePartsEndpoint(String endpointUri) {
        super(endpointUri);

        // Ident-Endpoint erzeugen für die FIN/VIN-Auflösung
        identEndpoint = new iPartsWSIdentEndpoint("");
        identEndpoint.setIdentCodeAttributeName("finOrVin");

        // SearchParts-Endpoint erzeugen für die Teilesuche
        searchPartsEndpoint = new iPartsWSSearchPartsEndpoint("", false) {
            @Override
            protected LogChannels getLogChannelDebugForSearchListener() {
                return null; // Keine Logausgaben für die einzelnen Suchergebnisse
            }
        };
        searchPartsEndpoint.setMaxResults(1);
    }

    @Override
    protected boolean isHttpMethodPostValid() {
        return false;
    }

    /**
     * ValidateParts verwendet GET mit diversen Query-Parametern in der URL.
     *
     * @return
     */
    @GET
    @Produces(MimeTypes.MIME_TYPE_JSON)
    public RESTfulTransferObjectInterface getPartsList(@QueryParam("finOrVin") String finOrVin, @QueryParam("parts") String parts,
                                                       @SecurePayloadParam String securePayload) {
        // Request mit den Parametern erzeugen für die weitere Verarbeitung analog zu POST-Requests
        iPartsWSValidatePartsRequest request = new iPartsWSValidatePartsRequest();
        request.setFinOrVin(finOrVin);
        request.setParts(parts);
        request.setSecurePayload(securePayload); // SecurePayload muss bei GET explizit gesetzt werden
        request.checkIfValid(""); // checkIfValid() muss bei GET explizit aufgerufen werden
        return handleWebserviceRequestIntern(request);
    }

    @Override
    protected iPartsWSValidatePartsResponse executeWebservice(EtkProject project, iPartsWSValidatePartsRequest requestObject) {
        iPartsWSUserInfo userInfo = requestObject.getUser();
        setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

        // Künstlich einen IdentRequest erzeugen
        iPartsWSIdentRequest identRequest = new iPartsWSIdentRequest();
        identRequest.setUser(requestObject.getUser());
        identRequest.setIdentCode(requestObject.getFinOrVin());

        // ... und damit den IdentEndpoint aufrufen
        iPartsWSIdentResponse identResponse = identEndpoint.executeWebservice(project, identRequest);

        // Response erzeugen
        iPartsWSValidatePartsResponse response = new iPartsWSValidatePartsResponse();
        if ((identResponse == null) || identResponse.getIdentContexts().isEmpty()) {
            // Nichts Passendes gefunden -> FIN/VIN ist ungültig
            throwResourceNotFoundError("No datacard found for FIN/VIN: " + requestObject.getFinOrVin());
        } else { // Mindestens ein Produkt gefunden -> Teilenummern suchen und jeweils erste Treffer ausgeben
            Session session = Session.get();
            if (session != null) {
                iPartsWSIdentContext identContext = identResponse.getIdentContexts().get(0);
                if (!identContext.isDatacardExists()) {
                    // Datenkarte konnte nicht geladen werden -> FIN/VIN ist ungültig
                    throwResourceNotFoundError("No datacard found for FIN/VIN: " + requestObject.getFinOrVin());
                }

                requestObject.setIdentContext(identContext); // IdentContext im Request speichern für die Verwendung in anderen Methoden

                // Erst hier existiert der identContext
                // In checkIfValid() noch nicht
                iPartsWSIdentContextHelper.checkIfIdentContextValid(identContext);

                int threadCount = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_VALIDATE_PARTS_THREAD_COUNT);
                Map<String, iPartsWSValidateResult> searchResultsMap = Collections.synchronizedMap(new HashMap<>());
                Set<String> partNumbers = new LinkedHashSet<>(StrUtils.toStringList(requestObject.getParts(), ",", false, true)); // Die Original-Sortierung soll erhalten bleiben
                Logger.log(logChannelDebug, LogType.DEBUG, getEndpointUri() + ": Validating " + partNumbers.size() + " part numbers using "
                                                           + threadCount + " search threads...");

                ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
                try {
                    // Suche für die Teilenummern durchführen
                    for (String partNumber : partNumbers) {
                        executorService.execute(() -> {
                            if (session.isActive()) {
                                // Eigene Session, damit jeder Such-Thread einen eigenen Filter hat, da dieser auf die Filterung
                                // eines Moduls spezialisiert ist und sich die parallelen Such-Threads bei einem einzigen
                                // Filter ansonsten gegenseitig in die Quere kommen würden
                                Session searchPartsSession = EtkEndpointHelper.createSession(SessionType.ENDPOINT, false);
                                searchPartsSession.setAttribute(JavaViewerApplication.SESSION_KEY_DWK_FILE, session.getAttribute(JavaViewerApplication.SESSION_KEY_DWK_FILE));
                                searchPartsSession.setAttribute(JavaViewerApplication.SESSION_KEY_PROJECT, project);
                                searchPartsSession.runInSession(() -> {
                                    // Filter für die Session und damit auch die Suche setzen
                                    iPartsFilter filter = identContext.setFilterForIdentContext(userInfo.getCountry(), false, project);
                                    filter.setAggModelsFilterActive(true);

                                    // Inkl. Aggregate
                                    iPartsProduct.setProductStructureWithAggregatesForSession(true);

                                    // SearchPartsRequest befüllen
                                    iPartsWSSearchPartsRequest searchPartsRequest = new iPartsWSSearchPartsRequest();
                                    searchPartsRequest.setUser(requestObject.getUser());
                                    searchPartsRequest.setIdentContext(identContext);
                                    searchPartsRequest.setSearchText(partNumber);
                                    searchPartsRequest.setIncludeAggs(true);
                                    searchPartsRequest.setIncludeSAs(false);
                                    searchPartsRequest.setIncludeES2Keys(false);

                                    // SearchParts aufrufen
                                    try {
                                        searchPartsEndpoint.executeWebservice(
                                                project, searchPartsRequest, (searchResult, wsPartResult) -> {
                                                    EtkDataPartListEntry partListEntry = searchResult.getEntry();
                                                    String matNr = partListEntry.getPart().getAsId().getMatNr();
                                                    if (searchResultsMap.containsKey(matNr)) { // Pro Materialnummer nur das erste Ergebniss
                                                        return;
                                                    }

                                                    if (partListEntry instanceof iPartsDataPartListEntry) {
                                                        iPartsDataPartListEntry iPartsEntry = (iPartsDataPartListEntry)partListEntry;

                                                        iPartsWSValidateResult partResult = new iPartsWSValidateResult();

                                                        String hotspotField = TableAndFieldName.getFieldName(iPartsEntry.getOwnerAssembly().getEbene().getKeyFeldName());
                                                        partResult.assignValuesForValidate(iPartsEntry, hotspotField, identContext.getModelId(),
                                                                                           userInfo.getCountry());
                                                        partResult.assignValuesFromSearchResponse(wsPartResult);

                                                        searchResultsMap.put(matNr, partResult);
                                                    }
                                                });
                                    } catch (Exception e) {
                                        String exceptionMessage = Utils.exceptionToString(e);
                                        if (e instanceof RESTfulWebApplicationException) {
                                            exceptionMessage += "Error details: " + ((RESTfulWebApplicationException)e).getErrorResponse();
                                        }
                                        Logger.log(logChannelDebug, LogType.ERROR, getEndpointUri() + ": Error while validating part \""
                                                                                   + partNumber + "\" for FIN/VIN \"" + requestObject.getFinOrVin()
                                                                                   + "\": " + exceptionMessage);
                                    }
                                });
                                SessionManager.getInstance().destroySession(searchPartsSession);
                            }
                        });
                    }
                } finally {
                    executorService.shutdown();
                }
                try {
                    executorService.awaitTermination(1, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    WSRequestTransferObject.throwRequestError(WSError.INTERNAL_ERROR, "Waiting for " + getEndpointUri()
                                                                                      + " search threads to finish interrupted", "");
                }
                // Die Original-Sortierung soll erhalten bleiben
                List<iPartsWSValidateResult> result = new ArrayList<>(partNumbers.size());
                partNumbers.forEach(partNumber -> result.add(searchResultsMap.get(partNumber)));
                response.setSearchResults(result);
            } else {
                WSRequestTransferObject.throwRequestError(WSError.INTERNAL_ERROR, "Session is null for " + getEndpointUri(), "");
            }
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