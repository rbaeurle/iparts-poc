/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getpartinfo;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartInfo;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Response Data Transfer Object für den GetPartInfo-Webservice
 *
 * Beispiel Response siehe Confluence: https://confluence.docware.de/confluence/x/7oFbAQ
 */
public class iPartsWSGetPartInfoResponse implements RESTfulTransferObjectInterface {

    private iPartsWSPartInfo partInfo;

    public iPartsWSGetPartInfoResponse() {
    }

    public iPartsWSGetPartInfoResponse(iPartsWSPartInfo partInfo) {
        this.partInfo = partInfo;
    }

    public iPartsWSPartInfo getPartInfo() {
        return partInfo;
    }

    public void setPartInfo(iPartsWSPartInfo partInfo) {
        this.partInfo = partInfo;
    }
}