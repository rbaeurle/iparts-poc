/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

/**
 * Ausprägungen der Kennungen für Getriebe/Kupplungsart bzw. für Tauschmotor des MotorIdents
 * siehe https://confluence.docware.de/confluence/pages/viewpage.action?spaceKey=DAIM&title=Werkskennung+-+Aggregate
 */
public enum TransmissionIdentKeys {

    TRANSMISSION_IDENT_ZERO("0", "!!Schalt-Getriebe mit Normal-Kupplung"),
    TRANSMISSION_IDENT_ONE("1", "!!Schalt-Getriebe mit hydr. automat. Kupplung"),
    TRANSMISSION_IDENT_TWO("2", "!!Automatik-Getriebe"),
    TRANSMISSION_IDENT_SIX("6", "!!Nachgebauter Neumotor"),
    TRANSMISSION_IDENT_SEVEN("7", "!!Tauschmotor, Rumpfmotor"),
    TRANSMISSION_IDENT_EIGHT("8", "!!Tauschmotor, werk instandgesetzt"),
    TRANSMISSION_IDENT_NINE("9", "!!Tauschmotor, Komplett-Motor"),
    TRANSMISSION_IDENT_UNKNOWN("", "");

    public static final String TRANSMISSION_MECHANICAL = "GM";
    public static final String TRANSMISSION_AUTOMATED = "GA";

    private String key;
    private String description;

    public static String ENUM_KEY = "GearboxType";

    TransmissionIdentKeys(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public static TransmissionIdentKeys getTransmissionIdentKeyByValue(String value) {
        for (TransmissionIdentKeys transmissionKey : values()) {
            if (transmissionKey.getKey().equals(value)) {
                return transmissionKey;
            }
        }
        return TRANSMISSION_IDENT_UNKNOWN;
    }

    public String getTransmissionEnumKey() {
        if (key.equals("0") || key.equals("1")) {
            return TRANSMISSION_MECHANICAL;
        }
        if (key.equals("2")) {
            return TRANSMISSION_AUTOMATED;
        }
        return "";
    }

}
