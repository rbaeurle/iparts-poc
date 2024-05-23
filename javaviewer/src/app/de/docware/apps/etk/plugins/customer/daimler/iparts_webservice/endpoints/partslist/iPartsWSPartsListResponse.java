/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist;

import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.visualNav.iPartsWSVisualNavResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartsListNavNode;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.Collection;
import java.util.List;

/**
 * Response Data Transfer Object für den partsList-Webservice (Stücklisten-Aufruf für FIN/VIN oder BM6)
 *
 * Beispiel-Response siehe Confluence:
 */
public class iPartsWSPartsListResponse implements RESTfulTransferObjectInterface {

    private Collection<String> ambiguousProductIds;
    private iPartsWSIdentContext identContext;
    private List<iPartsWSPartsListNavNode> nextNodes;
    private iPartsWSVisualNavResponse visualNav;

    /**
     * Leerer Konstruktor (notwendig für die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPartsListResponse() {
    }

    public Collection<String> getAmbiguousProductIds() {
        return ambiguousProductIds;
    }

    public void setAmbiguousProductIds(Collection<String> ambiguousProductIds) {
        this.ambiguousProductIds = ambiguousProductIds;
    }

    public iPartsWSIdentContext getIdentContext() {
        return identContext;
    }

    public void setIdentContext(iPartsWSIdentContext identContext) {
        this.identContext = identContext;
    }

    public List<iPartsWSPartsListNavNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<iPartsWSPartsListNavNode> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public iPartsWSVisualNavResponse getVisualNav() {
        return visualNav;
    }

    public void setVisualNav(iPartsWSVisualNavResponse visualNav) {
        this.visualNav = visualNav;
    }
}