/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.endpoints.versioninfo;

import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Response Data Transfer Object für den VersionInfo-Webservice für BST
 *
 * Beispiel-Response (kein Request-Objekt nötig)
 *
 * {
 * "version": "Development"
 * }
 */
public class iPartsWSVersionInfoBSTResponse implements RESTfulTransferObjectInterface {

    private String version;

    public iPartsWSVersionInfoBSTResponse() {
        this.version = Constants.APP_VERSION;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}