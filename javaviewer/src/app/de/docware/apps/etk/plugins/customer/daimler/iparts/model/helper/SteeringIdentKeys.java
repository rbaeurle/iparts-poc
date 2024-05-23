/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;

/**
 * Ausprägungen der Lenkungs/Zerlegungsgrad-Kennungen für MotorIdent
 * siehe https://confluence.docware.de/confluence/pages/viewpage.action?spaceKey=DAIM&title=Werkskennung+-+Aggregate
 */
public enum SteeringIdentKeys {

    STEERING_IDENT_ZERO("0", "CBU", "!!Lenkungsunabhängig", "!!komplett montiert"),
    STEERING_IDENT_ONE("1", "CBU", "!!Links-Lenkung", "!!komplett montiert"),
    STEERING_IDENT_TWO("2", "CBU", "!!Rechts-Lenkung", "!!komplett montiert"),
    STEERING_IDENT_FOUR("4", "CKD", "!!Lenkungsunabhängig", "!!Zerlegt"),
    STEERING_IDENT_FIVE("5", "CKD", "!!Links-Lenkung", "!!Zerlegt"),
    STEERING_IDENT_SIX("6", "CKD", "!!Rechts-Lenkung", "!!Zerlegt"),
    STEERING_IDENT_INDEPENDENT("", "", "!!Lenkungsunabhängig", "");

    public static final String STEERING_LEFT = "L";
    public static final String STEERING_RIGHT = "R";
    public static final String STEERING_INDEPENDENT = STEERING_IDENT_INDEPENDENT.description;
    public static String ENUM_KEY = "Lenkung";

    private String key;
    private String shortDescription;
    private String description;
    private String subDescription;

    SteeringIdentKeys(String key, String shortDescription, String description, String subDescription) {
        this.key = key;
        this.shortDescription = shortDescription;
        this.description = description;
        this.subDescription = subDescription;
    }

    public String getKey() {
        return key;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public String getSubDescription() {
        return subDescription;
    }

    public static SteeringIdentKeys getSteeringIdentKeyByValue(String value) {
        for (SteeringIdentKeys steeringKey : values()) {
            if (steeringKey.getKey().equals(value)) {
                return steeringKey;
            }
        }
        return STEERING_IDENT_INDEPENDENT;
    }

    public static boolean isValid(String steeringKey, String ident, boolean logInvalidSteering) {
        if (StrUtils.isValid(steeringKey)) {
            // steeringKey ist nicht leer, muss also einen der bekannten Werte haben
            SteeringIdentKeys steeringIdentKeyByValue = SteeringIdentKeys.getSteeringIdentKeyByValue(steeringKey);
            if (steeringIdentKeyByValue != SteeringIdentKeys.STEERING_IDENT_INDEPENDENT) {
                // Übereinstimmung gefunden; Lenkung ist gültig
                return true;
            } else {
                if (logInvalidSteering) {
                    // keine Übereinstimmung, Lenkung ist ungültig -> nur loggen
                    Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Steering key extracted from Ident is invalid (Ident : \"" + ident +
                                                                              "\"; Steering: \"" + steeringKey + "\")");
                }
                return true;
            }
        } else {
            if (logInvalidSteering && StrUtils.isValid(ident)) {
                // steeringKey ist leer, damit eigentlich ungültig -> loggen und trotzdem als gültig behandeln
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, "Steering key extracted from Ident is empty " +
                                                                          "(Ident : \"" + ident + "\")");
            }
            return true;
        }
    }

    public String getSteeringEnumKey() {
        if (key.equals("1") || key.equals("5")) {
            return STEERING_LEFT;
        }
        if (key.equals("2") || key.equals("6")) {
            return STEERING_RIGHT;
        }
        return STEERING_INDEPENDENT;
    }

    public boolean isValidLeftOrRightSteering() {
        return !getSteeringEnumKey().equals(STEERING_INDEPENDENT);
    }
}
