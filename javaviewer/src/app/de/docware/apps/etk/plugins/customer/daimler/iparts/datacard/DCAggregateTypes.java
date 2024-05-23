/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsSpecType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsBiDirectMap;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.TransmissionIdentKeys;
import de.docware.util.StrUtils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Typen der Aggregate
 */
public enum DCAggregateTypes {

    VEHICLE("!!Fahrzeug", ""),
    ENGINE("!!Motor", "engine"),
    TRANSMISSION("!!Getriebe", "transmission"),
    TRANSFER_CASE("!!Verteilergetriebe", "transferCase"),
    AXLE("!!Achse", "axle"),  // TODO Muss in VA unf HA basierend auf dem typeOf im JSON aufgesplittet werden
    CAB("!!Fahrerhaus", "cab"),
    AFTER_TREATMENT_SYSTEM("!!Abgasnachbehandlung", "afterTreatmentSystem"),
    ELECTRO_ENGINE("!!Elektromotor", "electroEngine"),
    FUEL_CELL("!!Brennstoffzelle", "fuelCell"),
    HIGH_VOLTAGE_BATTERY("!!Hochvoltbatterie", "highVoltageBattery"),
    STEERING("!!Lenkung", "steering"),
    EXHAUST_SYSTEM("!!Abgassystem", "exhaust_system"),  // ignorieren
    PLATFORM("!!Pritsche", "platform"),
    UNKNOWN("", "");

    // Aggregate-Typen, die bei einem PKW die ZB Anreicherung bekommen
    private static final EnumSet<DCAggregateTypes> TYPES_FOR_CAR_ZB_ENRICHMENT = EnumSet.of(ENGINE, FUEL_CELL,
                                                                                            HIGH_VOLTAGE_BATTERY);
    // Aggregate-Typen, die eine ZB Anreicherung bekommen
    private static final EnumSet<DCAggregateTypes> ZB_ENRICHMENT_TYPES = EnumSet.copyOf(TYPES_FOR_CAR_ZB_ENRICHMENT);

    static {
        ZB_ENRICHMENT_TYPES.add(TRANSMISSION);
    }

    public static final String EXCHANGE_ENGINE = "!!Tauschmotor";
    public static final String EXCHANGE_TRANSMISSION = "!!Tauschgetriebe";
    public static final String EXCHANGE_AGGREGATE = "!!Tauschaggregat";

    private final String description;
    private final String jsonName;

    DCAggregateTypes(String description, String jsonName) {
        this.description = description;
        this.jsonName = jsonName;
    }

    public String getDescription() {
        return description;
    }

    public String getJsonName() {
        return jsonName;
    }

    public boolean isZBEnrichmentType() {
        return ZB_ENRICHMENT_TYPES.contains(this);
    }

    public boolean isTypeForCarZBEnrichment() {
        return TYPES_FOR_CAR_ZB_ENRICHMENT.contains(this);
    }

    public static DCAggregateTypes getAggregateTypeByJson(String jsonValue) {
        for (DCAggregateTypes aggType : values()) {
            if (aggType.getJsonName().equals(jsonValue)) {
                return aggType;
            }
        }
        return UNKNOWN;
    }

    private static iPartsBiDirectMap<String, DCAggregateTypes> aggTypeToDCAggType = new iPartsBiDirectMap();

    static {
        aggTypeToDCAggType.put(iPartsConst.AGGREGATE_TYPE_CAR, VEHICLE);  // Fahrzeug
        aggTypeToDCAggType.put("B", HIGH_VOLTAGE_BATTERY);
        aggTypeToDCAggType.put("E", ELECTRO_ENGINE);
        aggTypeToDCAggType.put("G", TRANSMISSION);
        aggTypeToDCAggType.put(TransmissionIdentKeys.TRANSMISSION_AUTOMATED, TRANSMISSION);
        aggTypeToDCAggType.put(TransmissionIdentKeys.TRANSMISSION_MECHANICAL, TRANSMISSION);
        aggTypeToDCAggType.put("VA", AXLE); // Vorderachse
        aggTypeToDCAggType.put("HA", AXLE); // Hinterachse
        aggTypeToDCAggType.put("LG", STEERING);
        aggTypeToDCAggType.put("M", ENGINE);
        aggTypeToDCAggType.put("N", FUEL_CELL);
        aggTypeToDCAggType.put("P", PLATFORM); // Pritsche
        aggTypeToDCAggType.put("FH", CAB); // Fahrerhaus
        aggTypeToDCAggType.put("VG", TRANSFER_CASE); // Zwischengetriebe
        aggTypeToDCAggType.put("AS", AFTER_TREATMENT_SYSTEM); // Abgasnachbehandlung
        //aggTypeToDCAggType.put("AS", EXHAUST_SYSTEM);

    }

    /**
     * Liefert das {@link DCAggregateTypes} Objekt zum übergebenen DB Wert
     *
     * @param aggregateType
     * @return
     */
    public static DCAggregateTypes getDCAggregateTypeByAggregateType(String aggregateType) {
        if (StrUtils.isValid(aggregateType)) {
            DCAggregateTypes result = aggTypeToDCAggType.get(aggregateType);
            if (result != null) {
                return result;
            }
        }
        return UNKNOWN;
    }

    /**
     * Liefert den DB Wert zum übergebenen {@link DCAggregateTypes}
     *
     * @param aggregateType
     * @return
     */
    public static String getDBAggregateTypeByDCAggregateType(DCAggregateTypes aggregateType) {
        if (aggregateType != UNKNOWN) {
            String result = aggTypeToDCAggType.getKey(aggregateType);
            if (StrUtils.isValid(result)) {
                return result;
            }
        }
        return "";
    }

    private static Map<iPartsSpecType, DCAggregateTypes> specTypeToDCAggType = new HashMap<>();

    static {
        specTypeToDCAggType.put(iPartsSpecType.ENGINE_OIL, ENGINE);  // Motorenöl
        specTypeToDCAggType.put(iPartsSpecType.COOLANT, ENGINE);  // Korrosions-/Frostschutz
        specTypeToDCAggType.put(iPartsSpecType.BREAK_FLUID, VEHICLE);  // Bremsflüssigkeit
        specTypeToDCAggType.put(iPartsSpecType.REFRIGERANT, VEHICLE);  // Kältemittel
        specTypeToDCAggType.put(iPartsSpecType.REFRIGERATOR_OIL, VEHICLE);  // Kompressoröl
        specTypeToDCAggType.put(iPartsSpecType.FRONT_AXLE_GEAR_OIL, VEHICLE);  // Vorderachsengetriebeöl
        specTypeToDCAggType.put(iPartsSpecType.REAR_AXLE_GEAR_OIL, VEHICLE);  // Hinterachsengetriebeöl
        specTypeToDCAggType.put(iPartsSpecType.TRANSFERCASE_GEAR_OIL, VEHICLE);  // Verteilergetriebeöl
        // CAVE! Individuelle Businesslogik bei TRANSMISSION - muss bei weiterem SpecType angepasst werden!
        specTypeToDCAggType.put(iPartsSpecType.GEAR_OIL, TRANSMISSION);  // Getriebeöl
    }

    public static DCAggregateTypes getDCAggregateTypeBySpecType(iPartsSpecType specType) {
        if (specType != null) {
            DCAggregateTypes result = specTypeToDCAggType.get(specType);
            if (result != null) {
                return result;
            }
        }
        return UNKNOWN;
    }
}
