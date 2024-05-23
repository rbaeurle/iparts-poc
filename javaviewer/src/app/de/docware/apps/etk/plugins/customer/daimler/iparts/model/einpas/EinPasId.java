/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;

/**
 * Repräsentiert eine EinPAS-ID.
 */
public class EinPasId extends HierarchicalIDWithType {

    public static final String TYPE = "DA_EinPasId";
    public static final String VIRTUAL_HG_FOR_KG_TU = "KG/TU";

    private enum INDEX {HG, G, TU}

    /**
     * Erzeugt eine virtuelle {@link EinPasId} für KG/TU-Knoten innerhalb der EinPAS-Struktur.
     *
     * @param kgTuId
     * @return
     */
    public static EinPasId createVirtualEinPasIdForKgTu(KgTuId kgTuId) {
        return new EinPasId(VIRTUAL_HG_FOR_KG_TU, kgTuId.getKg(), kgTuId.getTu());
    }

    /**
     * Der normale Konstruktor
     *
     * @param hg
     * @param g
     * @param tu
     */
    public EinPasId(String hg, String g, String tu) {
        super(TYPE, new String[]{ hg, g, tu });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public EinPasId() {
        this("", "", "");
    }

    public String getHg() {
        return id[INDEX.HG.ordinal()];
    }

    public String getG() {
        return id[INDEX.G.ordinal()];
    }

    public String getTu() {
        return id[INDEX.TU.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getHg() + "/" + getG() + "/" + getTu() + ") EinPAS";
    }

    public boolean isHgNode() {
        return !getHg().isEmpty() && getG().isEmpty() && getTu().isEmpty();
    }

    public boolean isGNode() {
        return !getHg().isEmpty() && !getG().isEmpty() && getTu().isEmpty();
    }

    public boolean isTuNode() {
        return !getHg().isEmpty() && !getG().isEmpty() && !getTu().isEmpty();
    }

    public boolean isValidId() {
        return allValuesFilled();
    }

    @Override
    public EinPasId getParentId() {
        if (isTuNode()) {
            return new EinPasId(getHg(), getG(), "");
        } else if (isGNode()) {
            return new EinPasId(getHg(), "", "");
        } else {
            return null;
        }
    }
}



