/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getpartinfo;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPartlistTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractGetPartInfoEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSFilteredPartListsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * Endpoint für den GetPartInfo-Webservice
 * Der GetMaterialPartInfo-Webservice für die Sonderkataloge ist davon abgeleitet.
 */
public class iPartsWSGetPartInfoEndpoint extends iPartsWSAbstractGetPartInfoEndpoint<iPartsWSGetPartInfoRequest> implements iPartsConst {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/GetPartInfo";

    private Map<String, LinkedList<Thread>> parallelExecutionRequestParamsMap = new HashMap<>(); // Benutzer+Modul auf wartende Threads für ähnliche Requests

    public iPartsWSGetPartInfoEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        if (parallelExecutionRequestParamsMap != null) {
            synchronized (parallelExecutionRequestParamsMap) {
                parallelExecutionRequestParamsMap.clear();
            }
        }
    }

    @Override
    protected iPartsWSGetPartInfoResponse executeWebservice(EtkProject project, iPartsWSGetPartInfoRequest requestObject) {
        iPartsWSUserInfo userInfo = requestObject.getUser();

        // Parallele Bearbeitung von Requests mit identischem Benutzer und Modul vermeiden
        String parallelExecutionRequestParams = userInfo.getUserId() + CacheHelper.CACHE_KEY_DELIMITER + requestObject.partContext.getModuleId();
        int maxParallelRequests = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_GET_PART_INFO_MAX_PARALLEL_REQUESTS);
        long timeout = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_GET_PART_INFO_TIMEOUT_PARALLEL_REQUESTS);
        long startTime = System.currentTimeMillis();
        boolean waiting = false;
        if (maxParallelRequests > 0) {
            while (!Thread.currentThread().isInterrupted()) {
                int numRequests;
                synchronized (parallelExecutionRequestParamsMap) {
                    LinkedList<Thread> threadList = parallelExecutionRequestParamsMap.computeIfAbsent(parallelExecutionRequestParams,
                                                                                                      key -> new LinkedList<>());
                    numRequests = threadList.size();
                    if (!waiting) {
                        numRequests++;
                        threadList.add(Thread.currentThread());
                    }
                    if (threadList.getFirst() == Thread.currentThread()) {
                        if (waiting) {
                            Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, getEndpointUri()
                                                                                                      + ": Execution of parallel request of user \""
                                                                                                      + userInfo.getUserId()
                                                                                                      + "\" for module \""
                                                                                                      + requestObject.partContext.getModuleId()
                                                                                                      + "\" after waiting "
                                                                                                      + ((System.currentTimeMillis() - startTime) / 1000) + " s");
                        }
                        break;
                    }
                }

                // Maximal maxParallelRequests parallele Requests mit identischem Benutzer und Modul erlauben
                if (numRequests > maxParallelRequests) {
                    requestFinished(parallelExecutionRequestParams);
                    throwError(HttpConstants.HTTP_STATUS_TOO_MANY_REQUESTS, WSError.REQUEST_TOO_MANY_REQUESTS, getEndpointUri()
                                                                                                               + ": More than "
                                                                                                               + maxParallelRequests
                                                                                                               + " parallel similar requests", null);
                }

                // Warten bis der vorherige Request mit identischem Benutzer und Modul fertig ist
                if (!waiting) {
                    waiting = true;
                    Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, getEndpointUri() + ": Waiting because of "
                                                                                              + numRequests + " parallel requests of user \""
                                                                                              + userInfo.getUserId()
                                                                                              + "\" for module \""
                                                                                              + requestObject.partContext.getModuleId() + "\"...");
                }
                try {
                    synchronized (parallelExecutionRequestParamsMap) {
                        if (timeout > 0) {
                            parallelExecutionRequestParamsMap.wait(timeout * 1000);
                        } else {
                            parallelExecutionRequestParamsMap.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    requestFinished(parallelExecutionRequestParams);
                    Thread.currentThread().interrupt();
                    throwError(HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT, WSError.REQUEST_TIMEOUT, "Request was interrupted", null);
                }

                if ((timeout > 0) && (System.currentTimeMillis() - startTime >= timeout * 1000)) {
                    requestFinished(parallelExecutionRequestParams);
                    Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, getEndpointUri() + ": Request timeout of "
                                                                                              + timeout + " s reached for execution of request of user \""
                                                                                              + userInfo.getUserId()
                                                                                              + "\" for module \""
                                                                                              + requestObject.partContext.getModuleId() + "\"");
                    throwError(HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT, WSError.REQUEST_TIMEOUT, "Request timeout of "
                                                                                                   + timeout + " s reached for execution", null);
                }
            }
        }

        try {
            iPartsWSIdentContext identContext = requestObject.getIdentContext();
            if (requestObject.isIntegratedNavigation()) {
                iPartsWSIdentRequest identRequest = new iPartsWSIdentRequest();
                // User setzen um Rechte für den Aufruf des Ident-Webservices zu erhalten
                identRequest.setUser(userInfo);
                // ProductId setzen um exakt einen IdentContext zurück zu bekommen
                identRequest.setProductId(requestObject.getIdentContext().getProductId());
                // Erst FIN, dann VIN, dann BM verwenden
                if (StrUtils.isValid(requestObject.getIdentContext().getFin())) {
                    identRequest.setIdentCode(requestObject.getIdentContext().getFin());
                } else if (StrUtils.isValid(requestObject.getIdentContext().getVin())) {
                    identRequest.setIdentCode(requestObject.getIdentContext().getVin());
                } else {
                    // ModelId ist ein Pflichtfeld und wird im Vorfeld validiert und existiert in jedem Fall
                    identRequest.setIdentCode(requestObject.getIdentContext().getModelId());
                }
                iPartsWSIdentEndpoint identEndpoint = new iPartsWSIdentEndpoint("");
                iPartsWSIdentResponse identResponse = identEndpoint.executeWebservice(project, identRequest);
                if (identResponse.getIdentContexts().isEmpty()) {
                    throwProcessingError(WSError.REQUEST_OBJECT_NOT_FOUND, "Ident context could not be loaded to process integrated navigation", null);
                }
                // IdentContext setzen, der Rest läuft wie vorher
                identContext = identResponse.getIdentContexts().get(0);
                // Falls integratedNavigation fälschlicherweise gesetzt wurde und im ermittelten
                // IdentContext "integratedNavigationAvailable = false" ist
                if (!identContext.isIntegratedNavigationAvailable()) {
                    throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Integrated navigation not available", null);
                }
            }
            identContext.checkIfModelValid("identContext", project, userInfo);

            setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

            // Filter anhand vom Ident Context setzen
            iPartsFilter filter = identContext.setFilterForIdentContext(userInfo.getCountry(), false, project);

            String inputProductId = identContext.getProductId(); // Produkt
            String inputModuleId = requestObject.getPartContext().getModuleId(); // entspricht k_vari = Baugruppe
            String inputLfdNr = requestObject.getPartContext().getSequenceId(); // entspricht k_lfdnr

            // Relation Produkt - Modul prüfen
            iPartsAssemblyId assemblyId = new iPartsAssemblyId(inputModuleId, "");

            // Integrierte Navigation
            if (requestObject.isIntegratedNavigation()) {
                iPartsProduct.setProductStructureWithAggregatesForSession(true);

                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
                if (assembly instanceof iPartsDataAssembly) {
                    iPartsDataAssembly dataAssembly = (iPartsDataAssembly)assembly;
                    iPartsProductId productId = dataAssembly.getProductIdFromModuleUsage();
                    if (productId != null) {
                        if (!productId.getProductNumber().equals(inputProductId)) {
                            List<iPartsWSIdentContext> aggContexts = identContext.getAggregates();
                            if ((aggContexts != null) && !aggContexts.isEmpty()) {
                                for (iPartsWSIdentContext aggContext : aggContexts) {
                                    if (aggContext.getProductId().equals(productId.getProductNumber())) {
                                        inputProductId = aggContext.getProductId();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            checkModuleValid(project, inputProductId, assemblyId, inputModuleId, userInfo.getSpecialPermissions());
            return createResponse(assemblyId, inputLfdNr, inputProductId, requestObject, filter.getCurrentDataCard().getModelNo(),
                                  userInfo.getCountry(), project);
        } finally {
            // Fertig -> nächster Request mit identischem Benutzer und Modul kann bearbeitet werden
            requestFinished(parallelExecutionRequestParams);
        }
    }

    private void requestFinished(String parallelExecutionRequestParams) {
        synchronized (parallelExecutionRequestParamsMap) {
            LinkedList<Thread> threadList = parallelExecutionRequestParamsMap.get(parallelExecutionRequestParams);
            if (threadList != null) {
                threadList.remove(Thread.currentThread());
                if (threadList.isEmpty()) {
                    parallelExecutionRequestParamsMap.remove(parallelExecutionRequestParams);
                }
            }
            parallelExecutionRequestParamsMap.notifyAll();
        }
    }

    @Override
    protected iPartsWSIdentContext getIdentContext(iPartsWSGetPartInfoRequest requestObject) {
        return requestObject.getIdentContext();
    }

    @Override
    protected Set<String> getFilteredPartListSequenceNumbers(EtkDataAssembly assembly, iPartsProduct product, iPartsWSGetPartInfoRequest requestObject) {
        return iPartsWSFilteredPartListsCache.getFilteredPartListSequenceNumbers(assembly, iPartsPartlistTypes.PART_LIST_TYPE_FOR_GET_PARTS,
                                                                                 requestObject.getIdentContext(), this);
    }
}