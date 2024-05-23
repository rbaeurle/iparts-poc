/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.versioninfo;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.server.HttpServerRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.methods.GET;

/**
 * Endpoint für den VersionInfo-Webservice
 */
public class iPartsWSVersionInfoEndpoint extends iPartsWSAbstractEndpoint<WSRequestTransferObjectInterface> {

    public static final String DEFAULT_ENDPOINT_URI = "/parts/VersionInfo";

    public iPartsWSVersionInfoEndpoint(String endpointUri) {
        super(endpointUri);
    }

    /**
     * GET-Aufruf für den Webservice VersionInfo
     *
     * @return
     */
    @GET
    @Produces(MimeTypes.MIME_TYPE_JSON)
    public RESTfulTransferObjectInterface handleWebserviceRequest() {
        return handleWebserviceRequestIntern(null);
    }

    @Override
    protected iPartsWSVersionInfoResponse executeWebservice(EtkProject project, WSRequestTransferObjectInterface requestObject) {
        return new iPartsWSVersionInfoResponse(project);
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
    }

    @Override
    protected SecureResult isValidRequestSignature(HttpServerRequest request) {
        return new SecureResult(SecureReturnCode.SUCCESS); // VersionInfo braucht kein Token
    }
}