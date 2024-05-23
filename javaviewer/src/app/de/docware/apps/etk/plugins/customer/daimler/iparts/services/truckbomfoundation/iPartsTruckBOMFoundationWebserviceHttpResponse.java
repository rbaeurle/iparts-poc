/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Allgemeines Antwort-Objekt bei Webservice-Aufrufen der TruckBOM.foundation durch den HTTP-Client von Apache
 */
public class iPartsTruckBOMFoundationWebserviceHttpResponse implements RESTfulTransferObjectInterface {

    private int responseStatusCode;
    private String responseContent;

    public iPartsTruckBOMFoundationWebserviceHttpResponse(int responseStatusCode, String responseContent) {
        this.responseStatusCode = responseStatusCode;
        this.responseContent = responseContent;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(int responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }
}
