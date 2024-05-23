/*
 * Copyright (c) 2017 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import java.util.EnumSet;

/**
 * Dokumentationsmethoden für Produkte/Module
 */
public enum iPartsDocumentationType {

    DIALOG("01", "BCT/D"),
    DIALOG_IPARTS("11", "DIALOG iParts"), // iParts Neuanlage
    ELDAS("02", "BCT/E"),
    BCS_PLUS_GLOBAL_BM("03", "BOMDB iParts"),
    BCS_PLUS("04", "BCS+"),
    PLUS_MINUS("05", "+/-"),
    PSK_PKW("20", "PSK PKW/VAN"),
    PSK_TRUCK("21", "PSK TRUCK"),
    UNKNOWN("", "");

    private String dbValue;
    private String exportValue;

    iPartsDocumentationType(String dbValue, String exportValue) {
        this.dbValue = dbValue;
        this.exportValue = exportValue;
    }

    public String getDBValue() {
        return dbValue;
    }

    public static iPartsDocumentationType getFromDBValue(String dbValue) {
        for (iPartsDocumentationType docType : values()) {
            if (docType.dbValue.equalsIgnoreCase(dbValue)) {
                return docType;
            }
        }
        return UNKNOWN;
    }

    static private EnumSet<iPartsDocumentationType> eldasDocuTypes = EnumSet.of(BCS_PLUS, BCS_PLUS_GLOBAL_BM, ELDAS, PLUS_MINUS);
    static private EnumSet<iPartsDocumentationType> dialogDocuTypes = EnumSet.of(DIALOG, DIALOG_IPARTS);
    static private EnumSet<iPartsDocumentationType> pskDocuTypes = EnumSet.of(PSK_PKW, PSK_TRUCK);

    public boolean isELDASDocumentationType() {
        return eldasDocuTypes.contains(this);
    }

    /**
     * Handelt es sich um eine ELDAS-Dokumentationsmethode oder die kompatible {@link #PSK_TRUCK}?
     *
     * @return
     * @see #isELDASDocumentationType()
     */
    public boolean isTruckDocumentationType() {
        return isELDASDocumentationType() || (this == PSK_TRUCK);
    }

    public boolean isDIALOGDocumentationType() {
        return dialogDocuTypes.contains(this);
    }

    /**
     * Handelt es sich um eine DIALOG-Dokumentationsmethode oder die kompatible {@link #PSK_PKW}?
     *
     * @return
     * @see #isDIALOGDocumentationType()
     */
    public boolean isPKWDocumentationType() {
        return isDIALOGDocumentationType() || (this == PSK_PKW);
    }

    public boolean isPSKDocumentationType() {
        return pskDocuTypes.contains(this);
    }

    public String getExportValue() {
        return exportValue;
    }

    public iPartsModuleTypes getModuleType(boolean isSA) {
        if (isDIALOGDocumentationType()) {
            return iPartsModuleTypes.DialogRetail;
        } else if (isELDASDocumentationType()) {
            return iPartsModuleTypes.EDSRetail;
        } else if (isSA) {
            return iPartsModuleTypes.SA_TU;
        } else if (this == PSK_PKW) {
            return iPartsModuleTypes.PSK_PKW;
        } else if (this == PSK_TRUCK) {
            return iPartsModuleTypes.PSK_TRUCK;
        } else {
            return iPartsModuleTypes.UNKNOWN;
        }
    }

    /**
     * Check, ob die übergebene Dokumentationsmethode den gleichen Typ (DIALOG, ELDAS oder PSK passend zu PKW/TRUCK), wie
     * die aktuelle Dokumenationsmethode hat.
     *
     * @param documentationType
     * @return
     */
    public boolean isSameOverallDocType(iPartsDocumentationType documentationType) {
        if (documentationType == null) {
            return false;
        }
        return (this == documentationType) || (isPKWDocumentationType() && documentationType.isPKWDocumentationType())
               || (isTruckDocumentationType() && documentationType.isTruckDocumentationType());
    }
}
