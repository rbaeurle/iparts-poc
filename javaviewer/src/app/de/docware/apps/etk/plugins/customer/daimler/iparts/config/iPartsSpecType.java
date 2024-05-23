/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.util.StrUtils;

import java.util.EnumSet;

/**
 * Definition der Spec Types
 */
public enum iPartsSpecType {
    ENGINE_OIL,
    BREAK_FLUID,
    COOLANT,
    REFRIGERANT,
    REFRIGERATOR_OIL,
    FRONT_AXLE_GEAR_OIL,
    REAR_AXLE_GEAR_OIL,
    TRANSFERCASE_GEAR_OIL,
    GEAR_OIL,
    NONE,
    UNKNOWN;

    public static final EnumSet<iPartsSpecType> NOT_RELEVANT_TYPES = EnumSet.of(NONE, UNKNOWN);

    public static final EnumSet<iPartsSpecType> RELEVANT_TYPES = EnumSet.of(ENGINE_OIL, BREAK_FLUID, COOLANT,
                                                                            REFRIGERANT, REFRIGERATOR_OIL,
                                                                            FRONT_AXLE_GEAR_OIL, REAR_AXLE_GEAR_OIL,
                                                                            TRANSFERCASE_GEAR_OIL, GEAR_OIL);

    /**
     * Liefert die {@link iPartsSpecType} für den übergeben {@code dbValue} zurück.
     *
     * @param dbValue
     * @return
     */
    public static iPartsSpecType getFromDBValue(String dbValue) {
        if (StrUtils.isEmpty(dbValue)) {
            return NONE;
        }
        for (iPartsSpecType specType : values()) {
            if (specType.getDbValue().equals(dbValue)) {
                return specType;
            }
        }
        return UNKNOWN;
    }

    public static boolean isSpecTypeRelevant(iPartsSpecType specType) {
        return !NOT_RELEVANT_TYPES.contains(specType);
    }

    public String getDbValue() {
        return name();
    }
}