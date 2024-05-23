/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;

/**
 * Wird für die beiden unteren Strukturebenen in der Konstruktion verwendet.
 * Die erste Ebene nach dem Baumuster hat nur eine listNumber und keine conGroup. Die zweite Ebene hat beide Felder.
 * In der listNumber steht die SAA bzw. Nummer der Grundstückliste und in der conGroup wird diese Nummer in der Regel
 * noch durch eine KG erweitert
 */
public class MBSStructureId extends HierarchicalIDWithType {

    public static final String TYPE = "DA_MBSStructureId";

    private enum INDEX {LIST_NUMBER, CON_GROUP}

    /**
     * Der normale Konstruktor
     *
     * @param listNumber
     * @param conGroup
     */
    public MBSStructureId(String listNumber, String conGroup) {
        super(TYPE, new String[]{ listNumber, conGroup });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public MBSStructureId() {
        this("", "");
    }

    /**
     * @return Die SAA bzw. Nummer der Grundstückliste
     */
    public String getListNumber() {
        return id[INDEX.LIST_NUMBER.ordinal()];
    }

    /**
     * @return Die SAA bzw. Nummer der Grundstückliste mit angehängter KG Nummer
     */
    public String getConGroup() {
        return id[INDEX.CON_GROUP.ordinal()];
    }


    @Override
    public String toString() {
        return "(" + getListNumber() + "/" + getConGroup() + ") MBS";
    }

    /**
     * @return <code>true</code> falls es sich um die erste Ebene der Struktur handelt (z.B. SAA)
     */
    public boolean isListNumberNode() {
        return !getListNumber().isEmpty() && getConGroup().isEmpty();
    }

    /**
     * @return <code>true</code> falls es sich um die zweite Ebene der Struktur handelt (z.B. SAA + KG)
     */
    public boolean isConGroupNode() {
        return !getListNumber().isEmpty() && !getConGroup().isEmpty();
    }

    public boolean isValidId() {
        return allValuesFilled();
    }

    @Override
    public MBSStructureId getParentId() {
        if (isConGroupNode()) {
            return new MBSStructureId(getListNumber(), "");
        } else {
            return null;
        }
    }

    /**
     * Gibt an ob es sich um eine Id zur Grundstückliste (beginnt mit "G") handelt. Wenn man sicher stellen möchte dass
     * man auf Stücklistenebene ist, sollte man zusätzlich noch {@link #isConGroupNode()} abfragen.
     *
     * @return <code>true</code> wenn es sich um eine Grundstückliste handelt
     */
    public boolean isBasePartlistId() {
        return !getListNumber().isEmpty() && getListNumber().startsWith(iPartsConst.BASE_LIST_NUMBER_PREFIX);
    }

    /**
     * Gibt an ob es sich um eine Id zu einer normalen SAA handelt. Wenn man sicher stellen möchte dass man auf Stücklistenebene
     * ist, sollte man zusätzlich noch {@link #isConGroupNode()} abfragen.
     *
     * @return <code>true</code> wenn es sich um eine SAA handelt
     */
    public boolean isSaaId() {
        return !getListNumber().isEmpty() && getListNumber().startsWith(iPartsConst.SAA_NUMBER_PREFIX);
    }

    /**
     * Gibt an ob es sich um eine Id zu einer freien SA (beginnt mit "W") handelt. Wenn man sicher stellen möchte dass
     * man auf Stücklistenebene ist, sollte man zusätzlich noch {@link #isConGroupNode()} abfragen.
     *
     * @return <code>true</code> wenn es sich um eine freie SA handelt
     */
    public boolean isFreeSaId() {
        return !getListNumber().isEmpty() && (getListNumber().startsWith(iPartsConst.FREE_SAA_NUMBER_PREFIX) ||
                                              getListNumber().startsWith("W"));
    }

}
