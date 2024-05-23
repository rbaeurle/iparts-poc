/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert die eindeutige ID für ein vereinfachtes Einzelteil zu einem Einzelteil eines Leitungssatzbaukastens aus
 * der Tabelle DA_WH_SIMPLIFIED_PARTS im iParts Plug-in.
 */
public class iPartsWireHarnessSimplifiedPartId extends IdWithType {

    public static String TYPE = "DA_iPartsWireHarnessSimplifiedPartId";

    protected enum INDEX {PART_NO, SUCCESSOR_PART_NO}

    /**
     * Der normale Konstruktor
     *
     * @param partNo
     * @param successorPartNo
     */
    public iPartsWireHarnessSimplifiedPartId(String partNo, String successorPartNo) {
        super(TYPE, new String[]{ partNo, successorPartNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsWireHarnessSimplifiedPartId() {
        this("", "");
    }

    public String getPartNo() {
        return id[INDEX.PART_NO.ordinal()];
    }

    public String getSuccessorPartNo() {
        return id[INDEX.SUCCESSOR_PART_NO.ordinal()];
    }


}
