/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.mediaobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Response Objekt von einem einzelnen Einzelteilbild vom Mediaservice
 */
public class MediaServiceMediaObjectResponse implements RESTfulTransferObjectInterface {

    // Alle JSON-Attribute außer url sind irrelevant für iParts
    private String url;
    private String redirectUrl; // Wird nur intern für den Redirect benötigt

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonIgnore
    public String getRedirectUrl() {
        return redirectUrl;
    }

    @JsonIgnore
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}