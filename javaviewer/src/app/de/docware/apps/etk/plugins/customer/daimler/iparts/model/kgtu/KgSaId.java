/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;

/**
 * Id für eine KG (Konstruktionsgruppe) mit einer eingehängten SA bei Daimler.
 */

public class KgSaId extends HierarchicalIDWithType {

    public static final String TYPE = "DA_KgSaId";

    private enum INDEX {KG, SA}

    /**
     * Der normale Konstruktor
     *
     * @param kg
     * @param sa
     */
    public KgSaId(String kg, String sa) {
        super(TYPE, new String[]{ kg, sa });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public KgSaId() {
        this("", "");
    }

    public String getKg() {
        return id[INDEX.KG.ordinal()];
    }

    public String getSa() {
        return id[INDEX.SA.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getKg() + "/" + getSa() + ") KG/SA";
    }

    @Override
    public KgTuId getParentId() {
        return new KgTuId(getKg(), ""); // ParentId ist eine KgTuId
    }

    /**
     * Gibt die ID für den zugehörigen Datensatz in der Tabelle DA_SA
     *
     * @return
     */
    public iPartsSaId getSaId() {
        if (isValidId()) {
            return new iPartsSaId(getSa());
        }
        return null;
    }
}