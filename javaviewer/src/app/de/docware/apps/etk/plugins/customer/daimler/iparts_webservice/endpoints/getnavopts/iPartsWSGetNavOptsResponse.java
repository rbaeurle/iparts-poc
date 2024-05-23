/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getnavopts;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSTopNode;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

/**
 * Response Data Transfer Object für den GetNavOpts-Webservice
 *
 * Beispiel-Response siehe Confluence: https://confluence.docware.de/confluence/x/bQH1
 */
public class iPartsWSGetNavOptsResponse implements RESTfulTransferObjectInterface {

    private List<iPartsWSNavNode> nextNodes;
    private iPartsWSNavNode parentNodes;
    private List<iPartsWSTopNode> topNodes;

    public iPartsWSGetNavOptsResponse() {
    }

    public List<iPartsWSNavNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<iPartsWSNavNode> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public iPartsWSNavNode getParentNodes() {
        return parentNodes;
    }

    public void setParentNodes(iPartsWSNavNode parentNodes) {
        this.parentNodes = parentNodes;
    }

    public List<iPartsWSTopNode> getTopNodes() {
        return topNodes;
    }

    public void setTopNodes(List<iPartsWSTopNode> topNodes) {
        this.topNodes = topNodes;
    }
}