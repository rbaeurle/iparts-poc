/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsWSfixingParts implements RESTfulTransferObjectInterface {

    private String partNumber;

    public iPartsWSfixingParts() {
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }
}
