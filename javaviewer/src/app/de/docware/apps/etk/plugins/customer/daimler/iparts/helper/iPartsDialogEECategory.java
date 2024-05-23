/*
 * Copyright (c) 2017 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

/**
 * E/E-Kategorie (TS1_VERKSNR aus DIALOG bei Urladung/Änderungsdienst)
 */
public enum iPartsDialogEECategory {

    DIO_VAR("D"),       // DioVar-SNR
    SW_SNR("SW"),       // Software-SNR
    HW_SNR("HW"),       // Hardware-SNR
    LUZB_FLASHABLE("SGF"),          // LU/ZB Daimler konform flashbar
    LUZB_NOT_FLASHABLE("SGNF"),     // LU/ZB nicht Daimler konform flashbar
    UNKNOWN("");

    private String dbValue;

    iPartsDialogEECategory(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDBValue() {
        return dbValue;
    }

    public static iPartsDialogEECategory getFromDBValue(String dbValue) {
        for (iPartsDialogEECategory docType : values()) {
            if (docType.dbValue.equalsIgnoreCase(dbValue)) {
                return docType;
            }
        }
        return UNKNOWN;
    }
}
