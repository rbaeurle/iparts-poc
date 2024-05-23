/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;

/**
 * Id für die KG (Konstruktionsgruppe) / TU (Technischer Umfang) Struktur bei Daimler.
 */

public class KgTuId extends HierarchicalIDWithType {

    public static final String TYPE = "DA_KgTuId";

    private enum INDEX {KG, TU}

    /**
     * Der normale Konstruktor
     *
     * @param kg
     * @param tu
     */
    public KgTuId(String kg, String tu) {
        super(TYPE, new String[]{ kg, tu });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public KgTuId() {
        this("", "");
    }

    public String getKg() {
        return id[INDEX.KG.ordinal()];
    }

    public String getTu() {
        return id[INDEX.TU.ordinal()];
    }

    public boolean isKgNode() {
        return !getKg().isEmpty() && getTu().isEmpty();
    }

    public boolean isTuNode() {
        return !getKg().isEmpty() && !getTu().isEmpty();
    }

    public boolean isValidId() {
        return allValuesFilled();
    }

    @Override
    public String toString() {
        return "(" + getKg() + "/" + getTu() + ") KG/TU";
    }

    @Override
    public KgTuId getParentId() {
        if (isTuNode()) {
            return new KgTuId(getKg(), "");
        } else {
            return null;
        }
    }
}