/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Repräsentiert eine FIN-ID mit Hilfsmethoden.
 */
public class FinId extends IdWithType {

    public static final int IDENT_NO_LENGTH = 6;

    public static final Pattern FIN_VIN_EXTENDED_PATTERN = Pattern.compile("[A-HJ-NPR-Z0-9]*");

    public static boolean containsValidLettersAndDigits(String s) {
        Matcher matcher = FIN_VIN_EXTENDED_PATTERN.matcher(s);
        return matcher.matches();
    }

    public static final int INVALID_SERIAL_NUMBER = -1;

    public static final String TYPE = "DA_FinId";

    private enum INDEX {FIN}

    /**
     * Der normale Konstruktor
     *
     * @param fin
     */
    public FinId(String fin) {
        super(TYPE, new String[]{ fin });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public FinId() {
        this("");
    }

    public String getFIN() {
        return id[INDEX.FIN.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getFIN() + ") FIN";
    }

    public String getWorldManufacturerIdentifier() {
        if (isWMIValid()) {
            return extractWMI();
        }
        return "";
    }

    public String getModelType() {
        if (isModelTypeValid()) {
            return extractModelType();
        }
        return "";
    }

    /**
     * Baumusternummer ohne Sachnummernkennbuchstabe C
     *
     * @return
     */
    public String getModelNumber() {
        if (isModelNumberValid()) {
            return extractModelNumber();
        }
        return "";
    }

    /**
     * Komplette Baumusternummer (mit Sachnummernkennbuchstabe C)
     *
     * @return
     */
    public String getFullModelNumber() {
        String modelNo = getModelNumber();
        if (!modelNo.isEmpty() && !iPartsModel.isVehicleModel(modelNo)) {
            modelNo = iPartsConst.MODEL_NUMBER_PREFIX_CAR + modelNo;
        }
        return modelNo;
    }

    public String getSteering() {
        if (isSteeringValid(false)) {
            return extractSteering();
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
            return StrUtils.strToIntDef(extractSerialNumber(), INVALID_SERIAL_NUMBER);
        }
        return INVALID_SERIAL_NUMBER;
    }

    /**
     * Der Ident setzt sich aus dem Werkskennbuchstaben ({@see #getFactorySign} und der Endnummer ({@see #getSerialNumber})
     * zusammen.
     *
     * @return
     */
    public String getIdent() {
        if (isFactorySignValid() && isSerialNumberValid()) {
            return StrUtils.copySubString(getFIN(), 10, 7).toUpperCase();
        }
        return "";
    }

    public boolean isWMIValid() {
        String wmi = extractWMI();
        if (StrUtils.isValid(wmi)) {
            return containsValidLettersAndDigits(wmi) && (wmi.length() == 3);
        }
        return false;
    }

    public boolean isModelTypeValid() {
        String modelType = extractModelType();
        if (StrUtils.isValid(modelType)) {
            return StrUtils.isDigit(modelType) && (modelType.length() == 3);
        }
        return false;
    }

    public boolean isModelNumberValid() {
        String modelNumber = extractModelNumber();
        if (StrUtils.isValid(modelNumber)) {
            return StrUtils.isDigit(modelNumber) && (modelNumber.length() == 6);
        }
        return false;
    }

    public boolean isSteeringValid(boolean checkOnlyIsLeftOrRightSteering) {
        if (checkOnlyIsLeftOrRightSteering) {
            return SteeringIdentKeys.getSteeringIdentKeyByValue(extractSteering()).isValidLeftOrRightSteering();
        } else {
            return SteeringIdentKeys.isValid(extractSteering(), getFIN(), isValidIdWithoutSteering());
        }
    }

    public boolean isFactorySignValid() {
        String factorySign = extractFactorySign();
        if (StrUtils.isValid(factorySign)) {
            return StrUtils.isDigit(factorySign) || containsValidLettersAndDigits(factorySign);
        }
        return false;
    }

    public boolean isSerialNumberValid() {
        String serialNumber = extractSerialNumber();
        if (StrUtils.isValid(serialNumber)) {
            return StrUtils.isDigit(serialNumber) && (serialNumber.length() == IDENT_NO_LENGTH);
        }
        return false;
    }

    protected String extractWMI() {
        return StrUtils.copySubString(getFIN(), 0, 3).toUpperCase();
    }

    protected String extractModelType() {
        return StrUtils.copySubString(getFIN(), 3, 3);
    }

    public String extractModelNumber() {
        return StrUtils.copySubString(getFIN(), 3, 6);
    }

    protected String extractSteering() {
        // DAIMLER-7561: Bei den 800er Baureihen (Industrieaggregate) sind Lenkung und Werkskenner vertauscht
        if (extractModelType().equals("800")) {
            return StrUtils.copySubString(getFIN(), 10, 1);
        }
        return StrUtils.copySubString(getFIN(), 9, 1);
    }

    protected String extractFactorySign() {
        // DAIMLER-7561: Bei den 800er Baureihen (Industrieaggregate) sind Lenkung und Werkskenner vertauscht
        if (extractModelType().equals("800")) {
            return StrUtils.copySubString(getFIN(), 9, 1);
        }
        return StrUtils.copySubString(getFIN(), 10, 1).toUpperCase();
    }

    protected String extractSerialNumber() {
        return StrUtils.copySubString(getFIN(), 11, IDENT_NO_LENGTH);
    }

    /**
     * Liefert den Lenkungskenner als dazugehörigen DB Kenner zurück.
     * "1" oder "5" -> Linkslenker
     * "2" oder "6" -> Rechtslenker
     *
     * @return "L" für Linkslenker
     * "R" für Rechtslenker
     */
    public String getLeftOrRightSteeringAsEnumKey() {
        String steering = extractSteering();
        if (!StrUtils.isEmpty(steering)) {
            return SteeringIdentKeys.getSteeringIdentKeyByValue(steering).getSteeringEnumKey();
        }
        return "";
    }

    /**
     * Überprüfung, ob alle Bestandteile der FIN syntaktisch korrekt sind
     *
     * - Gesamtlänge = 17
     * - ersten 3 Stellen sind Buchstaben
     * - gefolgt von 6 Ziffern
     * - die 10. Stelle darf nur Ziffer von "0" bis "6" enthalten (Rechtslenker = 2,6, Linkslenker = 1,5, Rest = Lenkungsneutral)
     * - 11. Stelle = Buchstabe oder Ziffer
     * - 12-17 Ziffern
     *
     * @return
     */
    @Override
    public boolean isValidId() {
        return isValidIdWithoutSteering() && isSteeringValid(false);
    }

    /**
     * Prüfung wie {@link #isValidId()} nur OHNE Berücksichtigung der Lenkung (10. Stelle).
     *
     * @return
     */
    public boolean isValidIdWithoutSteering() {
        return (getFIN() != null) && (getFIN().length() == 17) && isWMIValid() && isModelNumberValid() && isFactorySignValid()
               && isSerialNumberValid();
    }
}
