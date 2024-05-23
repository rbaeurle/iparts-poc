/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

public class iPartsWSsaa implements RESTfulTransferObjectInterface {

//    Nicht benötigte Parameter
//    private int amountPerSaa;
//    private boolean delta;
//    private String description;
//    private boolean indicatorASRelevance;
//    private String source;
//    private String value;

    private String id;
    private String saaDesignation; // id bei Aggregaten
    private String typeIndicator;

    public iPartsWSsaa() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSaaDesignation() {
        return saaDesignation;
    }

    public void setSaaDesignation(String saaDesignation) {
        this.saaDesignation = saaDesignation;
    }

    public String getTypeIndicator() {
        return typeIndicator;
    }

    public void setTypeIndicator(String typeIndicator) {
        this.typeIndicator = typeIndicator;
    }
}
