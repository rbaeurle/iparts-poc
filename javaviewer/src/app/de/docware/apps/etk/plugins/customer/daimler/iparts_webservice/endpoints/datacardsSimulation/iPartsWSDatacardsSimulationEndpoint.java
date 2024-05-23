/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.datacardsSimulation;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.http.server.HttpServerRequest;
import de.docware.framework.modules.webservice.restful.RESTfulErrorResponse;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.modules.webservice.restful.annotations.Path;
import de.docware.framework.modules.webservice.restful.annotations.PathParam;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.methods.GET;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.java1_1.Java1_1_Utils;

/**
 * Endpoint für die Simulation der Datenkarten-Webservices
 */
public class iPartsWSDatacardsSimulationEndpoint extends iPartsWSAbstractEndpoint {

    public static final String DEFAULT_ENDPOINT_URI = iPartsConst.WEBSERVICE_URI_DATACARDS_SIM_BASE;

    public iPartsWSDatacardsSimulationEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected boolean isHttpMethodPostValid() {
        return false;
    }

    @Override
    protected iPartsWSDatacardsSimulationResponse executeWebservice(EtkProject project, WSRequestTransferObjectInterface requestObject) {
        // Diese Methode sollte hier eigentlich nicht aufgerufen werden (stattdessen wird immer getVehicleDatacards() aufgerufen)
        throwError(HttpConstants.HTTP_STATUS_METHOD_NOT_ALLOWED, WSError.REQUEST_SYNTAX_ERROR,
                   "Invalid HTTP request method for this iParts Web service", null);
        return null;
    }

    /**
     * VehicleInformation benötigt die FIN als einzigen Input Parameter, ermittelt daraus einen Pfad aus WMI und Baumuster
     * und sucht dann dort nach einer JSON Datei, die die FIN im Dateinamen enthält
     *
     * @return
     */
    @GET
    @Path("/{visVersion}" + iPartsConst.WEBSERVICE_URI_VEHICLE_DATACARDS + "/{fin}")
    @Produces({ MimeTypes.MIME_TYPE_JSON })
    public iPartsWSDatacardsSimulationResponse getVehicleDatacards(@PathParam("visVersion") String visVersion,
                                                                   @PathParam("fin") String fin) {
        // Pfad zum Suchen der JSON Dateien zusammenbauen
        DWFile basePath = getWebserviceBasePath(visVersion, iPartsConst.WEBSERVICE_URI_VEHICLE_DATACARDS);
        DWFile searchPath = searchPathForFIN(basePath, fin);
        return executeSimulation(basePath, searchPath, fin);
    }

    /**
     * FixingParts benötigt die FIN als einzigen Input Parameter, ermittelt daraus einen Pfad aus WMI und Baumuster
     * und sucht dann dort nach einer JSON Datei, die die FIN im Dateinamen enthält
     * (gleiche Logik wie bei VehicleDataCards
     *
     * @return
     */
    @GET
    @Path("/{visVersion}" + iPartsConst.WEBSERVICE_URI_FIXING_PARTS + "/{fin}")
    @Produces({ MimeTypes.MIME_TYPE_JSON })
    public iPartsWSDatacardsSimulationResponse getFixingParts(@PathParam("visVersion") String visVersion,
                                                              @PathParam("fin") String fin) {
        // Pfad zum Suchen der JSON Dateien zusammenbauen
        DWFile basePath = getWebserviceBasePath(visVersion, iPartsConst.WEBSERVICE_URI_FIXING_PARTS);
        DWFile searchPath = searchPathForFIN(basePath, fin);
        return executeSimulation(basePath, searchPath, fin);
    }

    @GET
    @Path("/{visVersion}" + iPartsConst.WEBSERVICE_URI_AGGREGATE_DATACARDS + "/{aggType}/{aggSearchValue}")
    @Produces({ MimeTypes.MIME_TYPE_JSON })
    public iPartsWSDatacardsSimulationResponse getAggregateDatacards(@PathParam("visVersion") String visVersion,
                                                                     @PathParam("aggType") String aggType,
                                                                     @PathParam("aggSearchValue") String aggSearchValue) {
        DWFile basePath = getWebserviceBasePath(visVersion, iPartsConst.WEBSERVICE_URI_AGGREGATE_DATACARDS);
        DWFile searchPath = basePath.getChild(aggType);
        // hier keinen Fallback auf das Hauptverzeichnis machen, weil sich die Idents der verschiedenen Aggregate
        // überschneiden könnten und damit nicht mehr eindeutig sind
        return executeSimulation(null, searchPath, aggSearchValue);
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
    }

    @Override
    protected SecureResult isValidRequestSignature(HttpServerRequest request) {
        return new SecureResult(SecureReturnCode.SUCCESS); // Datenkarten-Simulation braucht kein Token
    }

    private iPartsWSDatacardsSimulationResponse executeSimulation(DWFile basePath, DWFile searchPath, String filename) {
        // Simuliertes Warten auf Antwort vom echten Webservice
        if (Java1_1_Utils.sleep(iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_DATACARDS_SIM_DELAY))) {
            return null; // Thread wurde abgebrochen
        }

        // JSON Datei in angegebenem Pfad suchen
        DWFile returnFile = findFileInPath(searchPath, filename);
        if (returnFile == null && basePath != null) {
            // Fallback: Suche nochmal im Hauptverzeichnis für den entsprechenden Webservice ohne Unterverzeichnisse
            returnFile = findFileInPath(basePath, filename);
        }

        if (returnFile != null) {
            byte[] fileContent = returnFile.readByteArray();
            return new iPartsWSDatacardsSimulationResponse(fileContent);
        }

        // Exceptions sind in der yaml Spec leider nicht genau spezifiziert
        throw new RESTfulWebApplicationException(new RESTfulErrorResponse(HttpConstants.HTTP_STATUS_NOT_FOUND));
    }

    private DWFile findFileInPath(DWFile searchPath, String searchTerm) {
        if ((searchPath != null) && searchPath.isDirectory()) {
            for (DWFile file : searchPath.listDWFiles()) {
                String filename = file.extractFileName(true);
                String searchFilename = searchTerm + ".json";
                if (filename.equals(searchFilename)) {
                    return file;
                }
            }
        }
        return null;
    }

    private DWFile searchPathForFIN(DWFile basePath, String fin) {
        FinId finIn = new FinId(fin);
        if (finIn.isValidId()) {
            String wmi = finIn.getWorldManufacturerIdentifier();
            String modelNumber = finIn.getModelNumber();
            return basePath.getChild(wmi).getChild(modelNumber);
        }
        return null;
    }

    private DWFile getWebserviceBasePath(String visVersion, String uri) {
        DWFile basePath = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsDWFile(iPartsWebservicePlugin.CONFIG_DATACARDS_SIM_DIR);
        String subPath = StrUtils.removeFirstCharacterIfCharacterIs(uri, "/");
        return basePath.getChild(visVersion).getChild(subPath);
    }
}