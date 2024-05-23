/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductclasses;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSProductClass;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den GetProductClasses-Webservice
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/confluence/x/tgBUAQ
 */
public class iPartsWSGetProductClassesResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSProductClass> productClasses;

    public iPartsWSGetProductClassesResponse() {
    }

    public List<iPartsWSProductClass> getProductClasses() {
        return productClasses;
    }

    public void setProductClasses(List<iPartsWSProductClass> productClasses) {
        this.productClasses = productClasses;
    }

}
