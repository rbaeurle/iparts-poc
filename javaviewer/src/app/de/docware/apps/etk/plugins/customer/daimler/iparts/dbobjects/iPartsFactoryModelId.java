/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Beziehung zwischen Weltherstellercode, Werkskennbuchstabe, Werksnummer, Baumuster-Prefix, Zusatzwerksnummer
 * zur Bestimmung des Millionenüberlaufs für Idents im iParts Plug-in.
 *
 * Tabelle DA_FACTORY_MODEL)
 */
public class iPartsFactoryModelId extends IdWithType {

    public static String TYPE = "DA_iPartsFactoryModel";

    protected enum INDEX {WMI, FACTORY_SIGN, FACTORY, MODEL_NO_PREFIX, ADD_FACTORY, AGG_TYPE, BELT_SIGN, BELT_GROUPING}

    /**
     * Der normale Konstruktor
     *
     * @param wmi
     * @param factorySign
     * @param factory
     * @param modelNumberPrefix
     * @param addFactory
     * @param beltSign
     * @param beltGrouping
     */
    public iPartsFactoryModelId(String wmi, String factorySign, String factory, String modelNumberPrefix, String addFactory,
                                String aggType, String beltSign, String beltGrouping) {
        super(TYPE, new String[]{ wmi, factorySign, factory, modelNumberPrefix, addFactory, aggType, beltSign, beltGrouping });
    }


    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsFactoryModelId() {
        this("", "", "", "", "", "", "", "");
    }

    public String getModelNumberPrefix() {
        return id[INDEX.MODEL_NO_PREFIX.ordinal()];
    }

    public String getFactorySign() {
        return id[INDEX.FACTORY_SIGN.ordinal()];
    }

    public String getFactory() {
        return id[INDEX.FACTORY.ordinal()];
    }

    public String getWorldManufacturerIdentifier() {
        return id[INDEX.WMI.ordinal()];
    }

    public String getAddFactory() {
        return id[INDEX.ADD_FACTORY.ordinal()];
    }

    public String getAggType() {
        return id[INDEX.AGG_TYPE.ordinal()];
    }

    public String getBeltSign() {
        return id[INDEX.BELT_SIGN.ordinal()].trim();
    }

    public String getBeltGrouping() {
        return id[INDEX.BELT_GROUPING.ordinal()].trim();
    }
}
