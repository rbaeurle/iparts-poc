/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Abstrakter Container DTO für eine Antwort vom Datenkarten-Webservice, die auch eine {@link iPartsWSDataCardException}
 * enthalten kann.
 */
public abstract class AbstractiPartsDatacardContainerResponse implements RESTfulTransferObjectInterface {

    private iPartsWSDataCardException exception;
    private String version;

    public AbstractiPartsDatacardContainerResponse() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public iPartsWSDataCardException getException() {
        return exception;
    }

    public void setException(iPartsWSDataCardException exception) {
        this.exception = exception;
    }
}
