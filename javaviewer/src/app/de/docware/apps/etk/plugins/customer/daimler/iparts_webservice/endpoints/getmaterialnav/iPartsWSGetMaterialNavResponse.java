/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialnav;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den GetMaterialNav-Webservice
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/confluence/x/2YF8AQ
 */
public class iPartsWSGetMaterialNavResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSNavNode> nextNodes;

    public iPartsWSGetMaterialNavResponse() {
    }

    public List<iPartsWSNavNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<iPartsWSNavNode> nextNodes) {
        this.nextNodes = nextNodes;
    }
}