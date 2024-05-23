/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsWSvpd implements RESTfulTransferObjectInterface {

    private String content;
    //    private String delta;
//    private String designation;
//    private String productCategory;
//    private String typeOf;
//    private String value;
    private String vpdIdent;

    public iPartsWSvpd() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVpdIdent() {
        return vpdIdent;
    }

    public void setVpdIdent(String vpdIdent) {
        this.vpdIdent = vpdIdent;
    }
}
