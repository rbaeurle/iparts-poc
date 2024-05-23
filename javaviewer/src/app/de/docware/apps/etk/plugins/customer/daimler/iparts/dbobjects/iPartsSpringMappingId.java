/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Farbnummer-GUID aus der Tabelle TABLE_DA_COLOR_NUMBER im iParts Plug-in.
 */
public class iPartsSpringMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsSpringMapping";

    protected enum INDEX {ZB_SPRING_LEG}

    /**
     * Der normale Konstruktor
     *
     * @param springLeg
     */
    public iPartsSpringMappingId(String springLeg) {
        super(TYPE, new String[]{ springLeg });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSpringMappingId() {
        this("");
    }

    public String getZBSpringLeg() {
        return id[INDEX.ZB_SPRING_LEG.ordinal()];
    }
}
