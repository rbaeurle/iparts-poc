/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * Data Transfer Object für die iParts Webservices GetPartInfo zur Darstellung der Code Matrix
 * Enthält alle Teilkonjunktionen in aufgeschlüsselter Form
 */
public class iPartsWSCodeMatrixElement implements RESTfulTransferObjectInterface {

    String code;
    boolean negative;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSCodeMatrixElement() {
    }

    public iPartsWSCodeMatrixElement(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isNegative() {
        return negative;
    }

    public void setNegative(boolean negative) {
        this.negative = negative;
    }
}
