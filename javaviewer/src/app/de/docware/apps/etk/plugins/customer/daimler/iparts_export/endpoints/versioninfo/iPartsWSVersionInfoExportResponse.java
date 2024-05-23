/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.versioninfo;

import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Response Data Transfer Object für den VersionInfo-Webservice des Export-Plugins
 */
public class iPartsWSVersionInfoExportResponse implements RESTfulTransferObjectInterface {

    private String version;

    public iPartsWSVersionInfoExportResponse() {
        this.version = Constants.APP_VERSION;
    }

    public String getVersion() {
        return version;
    }

}