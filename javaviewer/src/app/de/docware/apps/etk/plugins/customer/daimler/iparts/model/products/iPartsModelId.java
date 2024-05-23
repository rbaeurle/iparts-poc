/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.products;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * ID für ein Baumuster (7-stellig) im After-Sales (DA_MODEL) bzw. EDS-Baumuster (7-stellig) in der Konstruktion (DA_EDS_MODEL)
 */
public class iPartsModelId extends IdWithType {

    public static final String TYPE = "DA_iPartsModelId";

    private enum INDEX {MODEL_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param modelNumber 7-stellige Baumusternummer aus dem After-Sales
     */
    public iPartsModelId(String modelNumber) {
        super(TYPE, new String[]{ modelNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModelId() {
        this("");
    }

    public String getModelNumber() {
        return id[INDEX.MODEL_NUMBER.ordinal()];
    }

    /**
     * Liefert zurück, ob es sich bei diesem Baumuster um ein Aggregat handelt.
     *
     * @return
     */
    public boolean isAggregateModel() {
        return !getModelNumber().startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR);
    }

    /**
     * Liefert die Typkennzahl für dieses Baumuster zurück.
     *
     * @return
     */
    public String getModelTypeNumber() {
        return StrUtils.cutIfLongerThan(getModelNumber(), 4);
    }

    @Override
    public String toString() {
        return "(" + getModelNumber() + ") model";
    }

    /**
     * Überprüft, ob die Baumusternummer gültig ist, wobei diese 6-stellig mit Sachnummernkennbuchstabe (C oder D) sein
     * muss bei {@code mustHavePrefix == true} bzw. auch ohne Sachnummernkennbuchstabe gültig ist (bei {@code mustHavePrefix == false}).
     *
     * @param mustHavePrefix Bei {@code true} muss die 6-stellige Baumusternummer den Sachnummernkennbuchstaben (C oder D)
     *                       als Präfix haben
     * @return
     */
    public boolean isModelNumberValid(boolean mustHavePrefix) {
        return isModelNumberValid(getModelNumber(), mustHavePrefix);
    }

    /**
     * Überprüft, ob die Baumusternummer gültig ist, wobei diese 6-stellig mit Sachnummernkennbuchstabe (C oder D) sein
     * muss bei {@code mustHavePrefix == true} bzw. auch ohne Sachnummernkennbuchstabe gültig ist (bei {@code mustHavePrefix == false}).
     *
     * @param modelNumber    Die zu prüfende Baumusternummer
     * @param mustHavePrefix Bei {@code true} muss die 6-stellige Baumusternummer den Sachnummernkennbuchstaben (C oder D)
     *                       als Präfix haben
     * @return
     */
    public static boolean isModelNumberValid(String modelNumber, boolean mustHavePrefix) {
        if (StrUtils.isEmpty(modelNumber)) {
            return false;
        }

        if ((modelNumber.length() == 7) || (modelNumber.length() == 10)) { // 6-stelliges Baumuster oder 9-stelliges Aggregate-Baunmuster mit Sachnummernkennbuchstabe
            String prefix = modelNumber.substring(0, 1);
            String modelDigits = modelNumber.substring(1);
            if (((prefix.equals(iPartsConst.MODEL_NUMBER_PREFIX_CAR) && (modelNumber.length() == 7)) || prefix.equals(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE))
                && StrUtils.isDigit(modelDigits)) {
                return true;
            }
        } else if (((modelNumber.length() == 6) || (modelNumber.length() == 9)) && !mustHavePrefix) {
            if (StrUtils.isDigit(modelNumber)) {
                return true;
            }
        }

        return false;
    }
}
