/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.datacardsSimulation;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.webservice.restful.RESTfulTransferBinaryInterface;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Response Data Transfer Object für ein JSON File in Form eines byte[]
 */
public class iPartsWSDatacardsSimulationResponse implements RESTfulTransferBinaryInterface, RESTfulTransferObjectInterface {

    private byte[] content;

    public iPartsWSDatacardsSimulationResponse() {
    }

    public iPartsWSDatacardsSimulationResponse(byte[] content) {
        this.content = content;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public String getContentType() {
        return MimeTypes.MIME_TYPE_JSON;
    }

    @JsonIgnore
    public void setContent(byte[] content) {
        this.content = content;
    }
}