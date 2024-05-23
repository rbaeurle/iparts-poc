/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import de.docware.util.StrUtils;

import java.util.EnumSet;

/**
 * TypeOf der Aggregate
 */
public enum DCAggregateTypeOf {
    NONE("", ""),

    ELECTRO_ENGINE1("1", "eEngine1"),
    ELECTRO_ENGINE2("2", "eEngine2"),
    ELECTRO_ENGINE3("3", "eEngine3"),
    ELECTRO_ENGINE4("4", "eEngine4"),
    ELECTRO_ENGINE5("5", "eEngine5"),
    ELECTRO_ENGINE6("6", "eEngine6"),

    FUEL_CELL1("1", "fuelCell1"),
    FUEL_CELL2("2", "fuelCell2"),
    FUEL_CELL3("3", "fuelCell3"),

    HIGH_VOLTAGE_BATTERY1("1", "highVoltageBat1"),
    HIGH_VOLTAGE_BATTERY2("2", "highVoltageBat2"),
    HIGH_VOLTAGE_BATTERY3("3", "highVoltageBat3"),
    HIGH_VOLTAGE_BATTERY4("4", "highVoltageBat4"),
    HIGH_VOLTAGE_BATTERY5("5", "highVoltageBat5"),
    HIGH_VOLTAGE_BATTERY6("6", "highVoltageBat6"),
    HIGH_VOLTAGE_BATTERY7("7", "highVoltageBat7"),
    HIGH_VOLTAGE_BATTERY8("8", "highVoltageBat8"),

    FRONT1("!!vorne 1", "front1"),
    FRONT2("!!vorne 2", "front2"),
    FRONT3("!!vorne 3", "front3"),
    FRONT4("!!vorne 4", "front4"),

    REAR1("!!hinten 1", "rear1"),
    REAR2("!!hinten 2", "rear2"),
    REAR3("!!hinten 3", "rear3"),
    REAR4("!!hinten 4", "rear4"),

    UNKNOWN("!!Subtyp unbekannt", "");

    private String description;
    private String jsonName;

    DCAggregateTypeOf(String description, String jsonName) {
        this.description = description;
        this.jsonName = jsonName;
    }

    public String getDescription() {
        return description;
    }

    public String getJsonName() {
        return jsonName;
    }

    public boolean isFront() {
        return dcFrontAggregateTypes.contains(this);
    }

    public boolean isRear() {
        return dcRearAggregateTypes.contains(this);
    }

    public boolean isHighVoltageBattery() {
        return dcBatteryAggregateTypes.contains(this);
    }

    public boolean isFuelCell() {
        return dcFuelCellAggregateTypes.contains(this);
    }

    public boolean isElectroEngine() {
        return dcElectroAggregateTypes.contains(this);
    }


    public static DCAggregateTypeOf getAggregateTypeOfByJson(String jsonValue) {
        if (StrUtils.isValid(jsonValue)) {
            for (DCAggregateTypeOf aggTypeOf : values()) {
                if (aggTypeOf.getJsonName().equals(jsonValue)) {
                    return aggTypeOf;
                }
            }
            return UNKNOWN;
        } else {
            return NONE;
        }
    }

    static private EnumSet<DCAggregateTypeOf> dcFrontAggregateTypes = EnumSet.of(FRONT1, FRONT2, FRONT3, FRONT4);
    static private EnumSet<DCAggregateTypeOf> dcRearAggregateTypes = EnumSet.of(REAR1, REAR2, REAR3, REAR4);
    static private EnumSet<DCAggregateTypeOf> dcBatteryAggregateTypes = EnumSet.of(HIGH_VOLTAGE_BATTERY1, HIGH_VOLTAGE_BATTERY2,
                                                                                   HIGH_VOLTAGE_BATTERY3, HIGH_VOLTAGE_BATTERY4,
                                                                                   HIGH_VOLTAGE_BATTERY5, HIGH_VOLTAGE_BATTERY6,
                                                                                   HIGH_VOLTAGE_BATTERY7, HIGH_VOLTAGE_BATTERY8);
    static private EnumSet<DCAggregateTypeOf> dcFuelCellAggregateTypes = EnumSet.of(FUEL_CELL1, FUEL_CELL2, FUEL_CELL3);
    static private EnumSet<DCAggregateTypeOf> dcElectroAggregateTypes = EnumSet.of(ELECTRO_ENGINE1, ELECTRO_ENGINE2, ELECTRO_ENGINE3,
                                                                                   ELECTRO_ENGINE4, ELECTRO_ENGINE5, ELECTRO_ENGINE6);

    public static EnumSet<DCAggregateTypeOf> getFrontAggregateTypes() {
        return dcFrontAggregateTypes;
    }

    public static EnumSet<DCAggregateTypeOf> getRearAggregateTypes() {
        return dcRearAggregateTypes;
    }

    public static EnumSet<DCAggregateTypeOf> getBatteryAggregateTypes() {
        return dcBatteryAggregateTypes;
    }

    public static EnumSet<DCAggregateTypeOf> getFuelCellAggregateTypes() {
        return dcFuelCellAggregateTypes;
    }

    public static EnumSet<DCAggregateTypeOf> getElectroEngineAggregateTypes() {
        return dcElectroAggregateTypes;
    }

    public static boolean isFront(DCAggregateTypeOf aggregateTypeOf) {
        return dcFrontAggregateTypes.contains(aggregateTypeOf);
    }

    public static boolean isRear(DCAggregateTypeOf aggregateTypeOf) {
        return dcRearAggregateTypes.contains(aggregateTypeOf);
    }

    public static boolean isHighVoltageBattery(DCAggregateTypeOf aggregateTypeOf) {
        return dcBatteryAggregateTypes.contains(aggregateTypeOf);
    }

    public static boolean isFuelCell(DCAggregateTypeOf aggregateTypeOf) {
        return dcFuelCellAggregateTypes.contains(aggregateTypeOf);
    }

    public static boolean isElectroEngine(DCAggregateTypeOf aggregateTypeOf) {
        return dcElectroAggregateTypes.contains(aggregateTypeOf);
    }

}
