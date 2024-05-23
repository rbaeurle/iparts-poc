/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

import java.util.List;

/**
 * Response Data Transfer Object für einen Strukturknoten (KG/TU, EinPAS) inkl. Kindknoten und enthaltenen {@link iPartsWSPartsListModule}s
 * für den partsList-Webservices
 */
public class iPartsWSPartsListNavNode extends iPartsWSNavNode {

    private List<iPartsWSPartsListNavNode> nextNodes;
    private iPartsWSPartsListModule module;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSPartsListNavNode() {
    }

    /**
     * Konstruktor mit Mussfeldern für Verwendung in Response
     *
     * @param type
     * @param id
     * @param partsAvailable
     * @param label
     */
    public iPartsWSPartsListNavNode(iPartsWSNavNode.TYPE type, String id, boolean partsAvailable, String label, String labelRef) {
        super(type, id, partsAvailable, label, labelRef);
    }

    /**
     * Konstruktor basierend auf einem vorhandenen {@link iPartsWSNavNode}.
     *
     * @param navNode
     */
    public iPartsWSPartsListNavNode(iPartsWSNavNode navNode) {
        this(navNode.getTypeAsEnum(), navNode.getId(), navNode.isPartsAvailable(), navNode.getLabel(), navNode.getLabelRef());
        setNotes(navNode.getNotes());
        setFootNotes(navNode.getFootNotes());
        // Thumbnails explizit nicht übernehmen, da nicht gewünscht
        setSaCodes(navNode.getSaCodes());
        setSaaNavNodeInfos(navNode.getSaaNavNodeInfos());
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        // Keine Überprüfung notwendig, da dieses DTO nur im Response verwendet wird
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return null; // Kein CacheKey notwendig, da dieses DTO nur im Response verwendet wird
    }

    public List<iPartsWSPartsListNavNode> getNextNodes() {
        return nextNodes;
    }

    public void setNextNodes(List<iPartsWSPartsListNavNode> nextNodes) {
        this.nextNodes = nextNodes;
    }

    public iPartsWSPartsListModule getModule() {
        return module;
    }

    public void setModule(iPartsWSPartsListModule module) {
        this.module = module;
    }
}