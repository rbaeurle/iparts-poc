/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;

/**
 * Festlegung der Reihenfolge für FIN und Aggregate-Idents zur Auswahl im Filter-Dialog
 * Eigentlich sind Aggregatearten gemeint und nicht -Idents.
 */
public enum DatacardIdentOrderTypes {

    FIN("fin", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.VEHICLE, true, true),
    VIN("vin", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.VEHICLE, true, true),
    ENGINE_NEW("M", AggregateIdent.SPECIFICATION_NEW, DCAggregateTypes.ENGINE, true, true),
    ENGINE_OLD("M", AggregateIdent.SPECIFICATION_OLD, DCAggregateTypes.ENGINE, true, true),
    TRANSMISSION("G", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.TRANSMISSION, true, true),
    TRANSMISSION_AUTOMATED(TransmissionIdentKeys.TRANSMISSION_AUTOMATED, AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.TRANSMISSION, true, true),
    TRANSMISSION_MECHANICAL(TransmissionIdentKeys.TRANSMISSION_MECHANICAL, AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.TRANSMISSION, true, true),
    TRANSFER_CASE("VG", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.TRANSFER_CASE, true, true),
    AXLE_REAR_NEW("HA", AggregateIdent.SPECIFICATION_NEW, DCAggregateTypes.AXLE, true, true),
    AXLE_REAR_OLD("HA", AggregateIdent.SPECIFICATION_OLD, DCAggregateTypes.AXLE, true, true),
    AXLE_FRONT_NEW("VA", AggregateIdent.SPECIFICATION_NEW, DCAggregateTypes.AXLE, true, true),
    AXLE_FRONT_OLD("VA", AggregateIdent.SPECIFICATION_OLD, DCAggregateTypes.AXLE, true, true),
    CAB("FH", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.CAB, true, true),
    AFTER_TREATMENT("AS", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.AFTER_TREATMENT_SYSTEM, true, true),
    STEERING("LG", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.STEERING, true, true),
    PLATFORM("P", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.PLATFORM, true, false),
    HIGH_VOLTAGE_BATTERY("B", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.HIGH_VOLTAGE_BATTERY, true, true),
    ELECTRO_ENGINE("E", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.ELECTRO_ENGINE, true, true),
    FUEL_CELL("N", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.FUEL_CELL, false, false),
    UNKNOWN("", AggregateIdent.SPECIFICATION_DONT_CARE, DCAggregateTypes.UNKNOWN, false, false);

/*
F  - FIN
M  - Motor (neu)
M  - Motor (alt)
G  - Getriebe
GA - Getriebe automatisch
GM - Getriebe mechanisch
VG - Verteiler-Getriebe
HA - Hinter-Achse (neu)
HA - Hinter-Achse (alt)
VA - Vorder-Achse (neu)
VA - Vorder-Achse (alt)
FH - Fahrerhaus
AS - Abgassystem
L  - Lenkung
FH - Aufbau  (== Fahrerhaus)
-------
B  - HV Batterie
E  - Elektromotor
N  - Brennstoffzellensystem
P  - Pritsche
 */

    private String dbValue;
    private int specification;  // 0: dont care; 1: isNew 2: isOld
    private DCAggregateTypes dcAggregateType;
    private boolean isVisible;
    private boolean allowVIScall;

    DatacardIdentOrderTypes(String dbValue, int specification, DCAggregateTypes dcAggregateType, boolean isVisible, boolean allowVIScall) {
        this.dbValue = dbValue;
        this.specification = specification;
        this.dcAggregateType = dcAggregateType;
        this.isVisible = isVisible;
        this.allowVIScall = allowVIScall;
    }

    public String getDbValue() {
        return dbValue;
    }

    public int getSpecification() {
        return specification;
    }

    public DCAggregateTypes getDCAggregateType() {
        return dcAggregateType;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public static DatacardIdentOrderTypes getIdentOrderTypeByAggregateType(String aggregateType) {
        for (DatacardIdentOrderTypes identOrderType : values()) {
            if (identOrderType.getDbValue().equals(aggregateType)) {
                return identOrderType;
            }
        }
        return UNKNOWN;
    }

    public static DatacardIdentOrderTypes getIdentOrderTypeByAggregateTypes(DCAggregateTypes dcAggregateType) {
        for (DatacardIdentOrderTypes identOrderType : values()) {
            if (identOrderType.getDCAggregateType() == dcAggregateType) {
                return identOrderType;
            }
        }
        return UNKNOWN;
    }

    /**
     * Check, ob es sich um ein Fahrzeug Ident handelt (FIN oder VIN)
     *
     * @param identOrderType
     * @return
     */
    public static boolean isVehicleType(DatacardIdentOrderTypes identOrderType) {
        return (identOrderType == FIN) || (identOrderType == VIN);
    }

    public boolean isOldIdent() {
        return (specification == AggregateIdent.SPECIFICATION_OLD);
    }

    public boolean isNewIdent() {
        return (specification == AggregateIdent.SPECIFICATION_NEW);
    }

    public boolean isVIScallAllowed() {
        return allowVIScall;
    }

}
