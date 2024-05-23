/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine VIN-ID mit Hilfsmethoden.
 * Die Bedeutung der einzelnen Stellen ist in
 * https://confluence.docware.de/confluence/display/DAIM/Beziehung+und+Wertebereiche+WMI%2C+WKB?preview=%2F21889556%2F21889756%2FV9998016_2016-05-02_Nummerierungssystematiken_FIN_VIN_online_v_05.08.2016.pdf
 * definiert
 */
public class VinId extends IdWithType {

    public static final String TYPE = "DA_VinId";
    public static final int MODEL_MAPPING_PREFIX_LENGTH = 5;
    public static final int MODEL_MAPPING_SUFFIX_LENGTH = 2;

    private enum INDEX {VIN}

    /**
     * Der normale Konstruktor
     *
     * @param vin
     */
    public VinId(String vin) {
        super(TYPE, new String[]{ vin });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public VinId() {
        this("");
    }

    public String getVIN() {
        return id[INDEX.VIN.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getVIN() + ") VIN";
    }

    public String getWorldManufacturerIdentifier() {
        if (isWMIValid()) {
            return extractWMI();
        }
        return "";
    }

    public String getCodedSeries() {
        if (isCodedSeriesValid()) {
            return extractCodedSeries();
        }
        return "";
    }

    public String getTypeOfDrive() {
        if (isTypeOfDriveValid()) {
            return extractTypeOfDrive();
        }
        return "";
    }

    public String getPartOfModel() {
        if (isPartOfModelValid()) {
            return extractPartOfModel();
        }
        return "";
    }

    public String getRestraintSystem() {
        if (isRestraintSystemValid()) {
            return extractRestraintSystem();
        }
        return "";
    }

    public String getCheckDigit() {
        if (isCheckDigitValid()) {
            return extractCheckDigit();
        }
        return "";
    }

    public String getModelYearDigit() {
        if (isModelYearDigitValid()) {
            return extractModelYearKey();
        }
        return "";
    }

    public String getFactorySign() {
        if (isFactorySignValid()) {
            return extractFactorySign();
        }
        return "";
    }

    public int getSerialNumber() {
        if (isSerialNumberValid()) {
            return StrUtils.strToIntDef(extractSerialNumber(), FinId.INVALID_SERIAL_NUMBER);
        }
        return FinId.INVALID_SERIAL_NUMBER;
    }

    /**
     * Liefert den VIN Prefix für das Fallback-Mapping auf Baumuster
     *
     * @return
     */
    public String getPrefixForModelMapping() {
        if (isPrefixForModelMappingValid()) {
            return extractPrefixForModelMapping();
        }
        return "";
    }

    /**
     * Liefert den Baumuster-Suffix für das Fallback-Mapping auf Baumuster
     *
     * @return
     */
    public String getSuffixForModelMapping() {
        if (isSuffixForModelMappingValid()) {
            return extractModelSuffixForMapping();
        }
        return "";
    }

    public boolean isWMIValid() {
        String wmi = extractWMI();
        if (StrUtils.isValid(wmi)) {
            return FinId.containsValidLettersAndDigits(wmi) && (wmi.length() == 3);
        }
        return false;
    }

    public boolean isCodedSeriesValid() {
        String cSeries = extractCodedSeries();
        if (StrUtils.isValid(cSeries)) {
            return FinId.containsValidLettersAndDigits(cSeries) && (cSeries.length() == 1);
        }
        return false;
    }

    public boolean isTypeOfDriveValid() {
        String typeOfDrive = extractTypeOfDrive();
        if (StrUtils.isValid(typeOfDrive)) {
            return FinId.containsValidLettersAndDigits(typeOfDrive) && (typeOfDrive.length() == 1);
        }
        return false;
    }

    public boolean isPartOfModelValid() {
        String partOfModel = extractPartOfModel();
        if (StrUtils.isValid(partOfModel)) {
            return FinId.containsValidLettersAndDigits(partOfModel) && (partOfModel.length() == 2);
        }
        return false;
    }

    public boolean isRestraintSystemValid() {
        String restraintSystem = extractRestraintSystem();
        if (StrUtils.isValid(restraintSystem)) {
            return FinId.containsValidLettersAndDigits(restraintSystem) && (restraintSystem.length() == 1);
        }
        return false;
    }

    public boolean isCheckDigitValid() {
        String checkDigit = extractCheckDigit();
        if (StrUtils.isValid(checkDigit)) {
            return StrUtils.isDigit(checkDigit) && (checkDigit.length() == 1);
        }
        return false;
    }

    public boolean isModelYearDigitValid() {
        String modelYearDigit = extractModelYearKey();
        if (StrUtils.isValid(modelYearDigit)) {
            return FinId.containsValidLettersAndDigits(modelYearDigit) && (modelYearDigit.length() == 1);
        }
        return false;
    }

    public boolean isFactorySignValid() {
        String factorySign = extractFactorySign();
        if (StrUtils.isValid(factorySign)) {
            return StrUtils.isDigit(factorySign) || StrUtils.stringContainsLetters(factorySign);
        }
        return false;
    }

    public boolean isSerialNumberValid() {
        String serialNumber = extractSerialNumber();
        if (StrUtils.isValid(serialNumber)) {
            return StrUtils.isDigit(serialNumber) && (serialNumber.length() == FinId.IDENT_NO_LENGTH);
        }
        return false;
    }

    public boolean isPrefixForModelMappingValid() {
        String prefixForModelMapping = extractPrefixForModelMapping();
        if (StrUtils.isValid(prefixForModelMapping)) {
            return prefixForModelMapping.length() == MODEL_MAPPING_PREFIX_LENGTH;
        }
        return false;
    }

    public boolean isSuffixForModelMappingValid() {
        String suffixForModelMapping = extractModelSuffixForMapping();
        if (StrUtils.isValid(suffixForModelMapping)) {
            return StrUtils.isDigit(suffixForModelMapping) && (suffixForModelMapping.length() == MODEL_MAPPING_SUFFIX_LENGTH);
        }
        return false;
    }

    /**
     * Check, ob die eingegebene VIN für den VIN-Baumuster Fallback valide ist
     *
     * @return
     */
    public boolean isValidForModelMapping() {
        return StrUtils.isValid(getPrefixForModelMapping()) && StrUtils.isValid(getSuffixForModelMapping());
    }

    /**
     * Überprüfung, ob alle Bestandteile der VIN syntaktisch korrekt sind
     *
     * - Gesamtlänge = 17
     * weitere Überprüfungen sind i.A. nicht möglich
     *
     * @return
     */
    @Override
    public boolean isValidId() {
        String vin = getVIN();
        return !StrUtils.isEmpty(vin) && (vin.length() == 17) && FinId.containsValidLettersAndDigits(vin);
    }

    protected String extractWMI() {
        return StrUtils.copySubString(getVIN(), 0, 3).toUpperCase();
    }

    protected String extractCodedSeries() {
        return StrUtils.copySubString(getVIN(), 3, 1).toUpperCase();
    }

    protected String extractTypeOfDrive() {
        return StrUtils.copySubString(getVIN(), 4, 1).toUpperCase();
    }

    protected String extractPartOfModel() {
        return StrUtils.copySubString(getVIN(), 5, 2).toUpperCase();
    }

    protected String extractRestraintSystem() {
        return StrUtils.copySubString(getVIN(), 7, 1).toUpperCase();
    }

    protected String extractCheckDigit() {
        return StrUtils.copySubString(getVIN(), 8, 1);
    }

    protected String extractModelYearKey() {
        return StrUtils.copySubString(getVIN(), 9, 1).toUpperCase();
    }

    protected String extractFactorySign() {
        return StrUtils.copySubString(getVIN(), 10, 1);
    }

    protected String extractSerialNumber() {
        return StrUtils.copySubString(getVIN(), 11, FinId.IDENT_NO_LENGTH);
    }

    protected String extractPrefixForModelMapping() {
        return StrUtils.copySubString(getVIN(), 0, 5);
    }

    public String extractModelSuffixForMapping() {
        return StrUtils.copySubString(getVIN(), 5, 2);
    }
}