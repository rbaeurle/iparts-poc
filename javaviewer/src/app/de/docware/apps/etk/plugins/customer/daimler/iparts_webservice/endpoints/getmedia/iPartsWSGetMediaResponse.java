/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmedia;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.gui.misc.ContentTypes;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.webservice.restful.RESTfulTransferBinaryInterface;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Response Data Transfer Object für den GetMedia-Webservice
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/confluence/x/C4IFAQ
 */
public class iPartsWSGetMediaResponse implements RESTfulTransferBinaryInterface, RESTfulTransferObjectInterface {

    private byte[] imageContent;
    private String contentType;

    public iPartsWSGetMediaResponse() {
    }

    @Override
    public byte[] getBytes() {
        return imageContent;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @JsonIgnore
    public void setImage(byte[] imageContent, String mimeType) {
        this.imageContent = imageContent;
        this.contentType = ContentTypes.get(mimeType);
    }

    @JsonIgnore
    public void setImage(FrameworkImage image) {
        setImage(image.getContent(), image.getMimeType());
    }
}