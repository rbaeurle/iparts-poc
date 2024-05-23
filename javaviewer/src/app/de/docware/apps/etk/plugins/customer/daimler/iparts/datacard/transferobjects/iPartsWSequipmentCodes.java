/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsWSequipmentCodes implements RESTfulTransferObjectInterface {

//    Nicht benötigte Parameter
//    private String codeType;
//    private boolean delta;
//    private String designation;
//    private String productGroupIndication;
//    private String source;
//    private String value;
//    private Object descriptions;

    private String code;

    public iPartsWSequipmentCodes() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
