/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.util.StrUtils;

/**
 * EDS Aggregatekennzeichen (mit Mapping auf Aggregate Enum)
 */
public enum EDSAggregateTypes {

    AUFBAU("A", "FH"), // Aufbau (auf Fahrerhaus mappen!)
    TRANSMISSION("G", "G"), // Getriebe
    AXLE_REAR_FIRST("H", "HA"), // 1.Hinterachse
    AXLE_REAR_SECOND("J", "HA"), // 2. Hinterachse
    APPLICATION_MODULE("I", "I"), // Applikationsmodul
    STEERING("L", "LG"), // Lenkung
    ENGINE("M", "M"), // Motor
    AXLE_FRONT_FIRST("V", "VA"), // 1.Vorderachse
    AXLE_FRONT_SECOND("W", "VA"), // 2.Vorderachse
    TRANSFER_CASE("Z", "VG"), // Verteilergetriebe
    UNKNOWN("", "");

    private String edsValue;
    private String mappedValue;

    EDSAggregateTypes(String edsValue, String mappedValue) {
        this.edsValue = edsValue;
        this.mappedValue = mappedValue;
    }

    public String getEdsValue() {
        return edsValue;
    }

    public String getMappedValue() {
        return mappedValue;
    }

    public static EDSAggregateTypes getFromEDSValue(String edsValue) {
        if (StrUtils.isValid(edsValue)) {
            for (EDSAggregateTypes aggType : values()) {
                if (aggType.getEdsValue().equalsIgnoreCase(edsValue)) {
                    return aggType;
                }
            }
        }
        return UNKNOWN;
    }
}
