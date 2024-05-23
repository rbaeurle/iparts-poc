/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper;

public enum EPCAggregateTypes {

    AGGREGATE_TYPE_FRONTAX("VA"),   // Vorderachse
    AGGREGATE_TYPE_STEER("LG"),     // Lenkung
    AGGREGATE_TYPE_REARAX("HA"),    // Hinterachse
    AGGREGATE_TYPE_A_BODY("FH"),    // Fahrgestell
    AGGREGATE_TYPE_DIST("VG"),      // Verteilergetriebe
    AGGREGATE_TYPE_ENGINE("M"),     // Motor
    AGGREGATE_TYPE_PLATFRM("P"),    // Pritsche
    AGGREGATE_TYPE_AUTO("GA"),      // Automatikgetriebe
    AGGREGATE_TYPE_MANUAL("GM"),    // manuelles Getriebe
    AGGREGATE_TYPE_FUELCELL("N"),   // Wasserstoffzelle
    AGGREGATE_TYPE_HVBATTERY("B"),       // HV-Batterie
    AGGREGATE_TYPE_EMOTOR("E"),     // Elektormotor
    AGGREGATE_TYPE_EXHAUSTSYS("AS"); // Abgasbox

    private String epcValue;

    EPCAggregateTypes(String epcValue) {
        this.epcValue = epcValue;
    }

    public String getEpcValue() {
        return epcValue;
    }

}
