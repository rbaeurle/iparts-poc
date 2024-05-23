/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

/**
 * DTO für zusätzliche Daten am Materialstamm aus der Tabelle {@code CUSTPROP}.
 */
public class iPartsWSAdditionalPartInformation implements RESTfulTransferObjectInterface {

    public static String SOURCE_IPARTS = "iParts";
    public static String SOURCE_PRIMUS = "PRIMUS";

    private String type;
    private String description;
    private String value;
    private String source = SOURCE_IPARTS; // Quelle soll immer ausgegeben werden. Wenn nicht Primus, dann iParts

    public iPartsWSAdditionalPartInformation() {
    }

    public iPartsWSAdditionalPartInformation(String type, String description, String value) {
        this.type = type;
        this.description = description;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setSourcePrimus() {
        this.source = SOURCE_PRIMUS;
    }
}
