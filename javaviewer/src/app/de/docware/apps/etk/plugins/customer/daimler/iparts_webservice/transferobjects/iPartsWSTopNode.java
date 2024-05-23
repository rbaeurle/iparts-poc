/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * TopNode Data Transfer Object für die iParts Webservices
 */
public class iPartsWSTopNode implements RESTfulTransferObjectInterface, iPartsConst {

    private String type;
    private String id;
    private String label;
    private iPartsWSTopNode nextTopNode;
    private int topRank;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSTopNode() {
    }

    /**
     * Konstruktor mit Mussfeldern für Verwendung in Response
     *
     * @param type
     * @param id
     * @param label
     */
    public iPartsWSTopNode(iPartsWSNavNode.TYPE type, String id, String label) {
        this.type = type.name();
        this.id = id;
        this.label = label;
    }

    // Getter und Setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JsonIgnore
    public iPartsWSNavNode.TYPE getTypeAsEnum() {
        if (type == null) {
            return null;
        }

        try {
            return iPartsWSNavNode.TYPE.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @JsonIgnore
    public void setType(iPartsWSNavNode.TYPE type) {
        this.type = type.name();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public iPartsWSTopNode getNextTopNode() {
        return nextTopNode;
    }

    public void setNextTopNode(iPartsWSTopNode nextTopNode) {
        this.nextTopNode = nextTopNode;
    }

    public int getTopRank() {
        return topRank;
    }

    public void setTopRank(int topRank) {
        this.topRank = topRank;
    }
}