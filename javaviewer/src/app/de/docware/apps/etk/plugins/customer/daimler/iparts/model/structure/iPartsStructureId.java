/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für einen Strukturknoten.
 */
public class iPartsStructureId extends IdWithType {

    public static final String TYPE = "DA_iPartsStructureId";

    private enum INDEX {STRUCTURE_NAME}

    /**
     * Der normale Konstruktor
     *
     * @param structureName
     */
    public iPartsStructureId(String structureName) {
        super(TYPE, new String[]{ structureName });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsStructureId() {
        this("");
    }

    public String getStructureName() {
        return id[INDEX.STRUCTURE_NAME.ordinal()];
    }

    @Override
    public String toString() {
        return "(" + getStructureName() + ") structure";
    }
}
