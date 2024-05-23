/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

/**
 * Enum für den Grund der Anreichnung einer Aggregate-Datenkarte
 */
public enum EnrichReasons {

    VEHICLE_TO_AGGREGAT("!!Fahrzeug zu Aggregat"),
    CODE_TO_CODE("!!Code zu Code"),
    APARTNO_TO_CODE("!!Sachnummer zu Code"),
    FCODE("!!F-Code"),
    MARKETSPECIFIC_SAA("!!Marktspezifische SAA (Z0*)"),
    FIXING_PART("!!Befestigungsteil"),
    TSA("!!Traktor SAA");

    private String description;

    EnrichReasons(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
