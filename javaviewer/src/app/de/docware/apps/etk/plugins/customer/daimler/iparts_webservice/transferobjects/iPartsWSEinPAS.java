/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * EinPAS Data Transfer Object für die iParts Webservices
 */
public class iPartsWSEinPAS implements RESTfulTransferObjectInterface {

    private String version;
    private String hg;
    private String g;
    private String tu;

    /**
     * Leerer Konstruktur (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSEinPAS() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getHg() {
        return hg;
    }

    public void setHg(String hg) {
        this.hg = hg;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public String getTu() {
        return tu;
    }

    public void setTu(String tu) {
        this.tu = tu;
    }
}
