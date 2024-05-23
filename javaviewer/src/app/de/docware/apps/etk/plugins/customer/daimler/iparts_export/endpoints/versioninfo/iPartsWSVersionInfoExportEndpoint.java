/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.versioninfo;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.iPartsWSAbstractExportEndpoint;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.server.HttpServerRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.framework.modules.webservice.restful.annotations.Produces;
import de.docware.framework.modules.webservice.restful.annotations.methods.GET;

/**
 * Endpoint für den VersionInfo-Webservice des Export-Plugins
 */
public class iPartsWSVersionInfoExportEndpoint extends iPartsWSAbstractExportEndpoint<WSRequestTransferObjectInterface> {

    public static final String DEFAULT_ENDPOINT_URI = "/iPartsExport/VersionInfo";

    public iPartsWSVersionInfoExportEndpoint(String endpointUri) {
        super(endpointUri);
        setResponseCacheSize(1); // Es gibt für VersionInfo nur einen Eintrag im JSON Response-Cache
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
    protected iPartsWSVersionInfoExportResponse executeWebservice(EtkProject project, WSRequestTransferObjectInterface requestObject) {
        return new iPartsWSVersionInfoExportResponse();
    }

    @Override
    protected SecureResult isValidRequestSignature(HttpServerRequest request) {
        return new SecureResult(SecureReturnCode.SUCCESS); // VersionInfo braucht kein Token
    }
}