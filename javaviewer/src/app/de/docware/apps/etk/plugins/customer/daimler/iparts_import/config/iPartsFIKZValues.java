/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.config;

/**
 * Alle aktuellen Werte für FIKZ aus DIALOG
 */
public enum iPartsFIKZValues {
    MANUAL("B", "BEDIENUNGSANLEITUNG"),
    CONTROL_UNIT_EXPLICIT("E0", "STEUERGERÄTE (EXPLIZIT)"),
    CONTROL_UNIT_IMPLICIT("E1", "STEUERGERÄTE (IMPLIZIT)"),
    COLOR_ISSUES("F", "FARBSACHVERHALTE"),
    WEIGHT_AXLE_SPRING("G", "GEWICHTE ACHSDRUCK FEDERBEINE"),
    VEHICLE_WEIGHT("GF", "FAHRZEUGGESAMTGEWICHT"),
    CONTROL_UNIT_INFO_EXPLICIT("N0", "ST.-GER. MIT NUTZINFO (EXPL.)"),
    CONTROL_UNIT_INFO_IMPLICIT("N1", "ST.-GER. MIT NUTZINFO (IMPL.)"),
    FACTORY_COLOR_METHOD("P", "WERKSVARIANTE FARBMETHODE"),
    FACTORY_EXPLICIT("P0", "WERKSVARIANTE EXPLIZIT"),
    FACTORY_IMPLICIT("P1", "WERKSVARIANTE IMPLIZIT"),
    TIRE_MANUFACTURER("R0", "REIFENHERSTELLER"),
    HOLE_PATTERN("S", "LOCHBILDVARIANTEN"),
    SIGN_DATA("S1", "SCHILDERDATEN"),
    MARGIN_TABLE_SPRING("T", "TOLERANZTABELLE FEDERBEINBEST."),
    WIRE_HARNESS_SEAT_VARIANT("Z", "LEITUNGSSATZ- UND SITZVAR."),
    UNKNOWN("UNKNOWN", "UNKNOWN");


    private String fikzValue;
    private String descriptionInDIALOG;

    iPartsFIKZValues(String fikz, String description) {
        this.fikzValue = fikz;
        this.descriptionInDIALOG = description;
    }

    public String getFIKZValue() {
        return fikzValue;
    }

    public String getDescriptionFromDIALOG() {
        return descriptionInDIALOG;
    }

    public static iPartsFIKZValues getTypeFromCode(String fikzValue) {
        fikzValue = fikzValue.trim();
        for (iPartsFIKZValues type : values()) {
            if (type.getFIKZValue().equals(fikzValue)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static iPartsFIKZValues getTypeFromDIALOGDescription(String description) {
        description = description.trim();
        for (iPartsFIKZValues type : values()) {
            if (type.getDescriptionFromDIALOG().equals(description)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
