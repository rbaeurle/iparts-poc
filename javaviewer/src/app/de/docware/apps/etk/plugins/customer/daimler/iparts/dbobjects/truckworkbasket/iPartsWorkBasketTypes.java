/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket;

import java.util.EnumSet;

public enum iPartsWorkBasketTypes {
    EDS_SAA_WB("!!EDS-SAA-Arbeitsvorrat"),
    EDS_KEM_WB("!!EDS-KEM-Arbeitsvorrat"),
    MBS_SAA_WB("!!MBS-SAA-Arbeitsvorrat"),
    MBS_KEM_WB("!!MBS-KEM-Arbeitsvorrat"),
    CTT_SAA_WB("!!CTT-SAA-Arbeitsvorrat"),
    MISS_SAA_WB("!!Fehlende SAA-NutzDok Elemente"),
    MISS_KEM_WB("!!Fehlende KEM-NutzDok Elemente"),
    UNKNOWN("!!Unbekannt");

    private String title;

    public static EnumSet<iPartsWorkBasketTypes> SAA_WORKBASKET_TYPES = EnumSet.of(EDS_SAA_WB, MBS_SAA_WB, CTT_SAA_WB, MISS_SAA_WB);
    public static EnumSet<iPartsWorkBasketTypes> KEM_WORKBASKET_TYPES = EnumSet.of(EDS_KEM_WB, MBS_KEM_WB, MISS_KEM_WB);
    public static EnumSet<iPartsWorkBasketTypes> EDS_WORKBASKET_TYPES = EnumSet.of(EDS_SAA_WB, EDS_KEM_WB);
    public static EnumSet<iPartsWorkBasketTypes> MBS_WORKBASKET_TYPES = EnumSet.of(MBS_SAA_WB, MBS_KEM_WB);
    public static EnumSet<iPartsWorkBasketTypes> CTT_WORKBASKET_TYPES = EnumSet.of(CTT_SAA_WB);
    public static EnumSet<iPartsWorkBasketTypes> MISSING_WORKBASKET_TYPES = EnumSet.of(MISS_SAA_WB, MISS_KEM_WB);

    public static iPartsWorkBasketTypes getFromDbValue(String dbValue) {
        for (iPartsWorkBasketTypes type : iPartsWorkBasketTypes.values()) {
            if (type.name().equals(dbValue)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public static boolean isSaaWorkBasket(iPartsWorkBasketTypes wbType) {
        return SAA_WORKBASKET_TYPES.contains(wbType);
    }

    public static boolean isKemWorkBasket(iPartsWorkBasketTypes wbType) {
        return KEM_WORKBASKET_TYPES.contains(wbType);
    }

    public static boolean isMissingWorkBasket(iPartsWorkBasketTypes wbType) {
        return MISSING_WORKBASKET_TYPES.contains(wbType);
    }

    iPartsWorkBasketTypes(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
