/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductgroups;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSProductGroup;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den GetProductGroups-Webservice
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/confluence/x/EQD1
 */
public class iPartsWSGetProductGroupsResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSProductGroup> productGroups;

    public iPartsWSGetProductGroupsResponse() {
    }

    public List<iPartsWSProductGroup> getProductGroups() {
        return productGroups;
    }

    public void setProductGroups(List<iPartsWSProductGroup> productGroups) {
        this.productGroups = productGroups;
    }

}
