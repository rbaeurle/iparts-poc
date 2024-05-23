/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert die PK-Values aus der Tabelle TABLE_DA_EDS_CONST_PROPS im iParts Plug-in.
 */
public class iPartsEDSConstPropsId extends IdWithType {

    public static String TYPE = "DA_iPartsEDSConstPropsId";

    protected enum INDEX {EDS_PROPSSACHNO, EDS_PROPSPARTPOS, EDS_PROPSKEMFROM}

    /**
     * Der normale Konstruktor
     *
     * @param edsPropsSachNo
     * @param edsPropsPartPos
     * @param edsPropsKEMFrom
     */
    public iPartsEDSConstPropsId(String edsPropsSachNo, String edsPropsPartPos, String edsPropsKEMFrom) {
        super(TYPE, new String[]{ edsPropsSachNo, edsPropsPartPos, edsPropsKEMFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsEDSConstPropsId() {
        this("", "", "");
    }

    /**
     * Liegt eine gültige ID vor (orderGuid ist nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getEdsPropsSachNo().isEmpty() && !getEdsPropsPartPos().isEmpty() && !getEdsPropsKEMFrom().isEmpty();
    }

    public String getEdsPropsSachNo() {
        return id[INDEX.EDS_PROPSSACHNO.ordinal()];
    }

    public String getEdsPropsPartPos() {
        return id[INDEX.EDS_PROPSPARTPOS.ordinal()];
    }

    public String getEdsPropsKEMFrom() {
        return id[INDEX.EDS_PROPSKEMFROM.ordinal()];
    }


}
