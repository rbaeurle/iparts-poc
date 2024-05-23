/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Farbtabellen Stammdaten-GUID aus der Tabelle TABLE_DA_COLORTABLE_DATA im iParts Plug-in.
 */
public class iPartsColorTableDataId extends IdWithType {

    public static String TYPE = "DA_iPartsColorTableDataId";

    protected enum INDEX {COLOR_TABLE_ID}

    /**
     * Der normale Konstruktor
     */
    public iPartsColorTableDataId(String colorTableId) {

        super(TYPE, new String[]{ colorTableId });
    }

    public iPartsColorTableDataId(iPartsColorTableToPartId colorTableToPartId) {

        super(TYPE, new String[]{ colorTableToPartId.getColorTableId() });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsColorTableDataId() {
        this("");
    }

    public String getColorTableId() {
        return id[INDEX.COLOR_TABLE_ID.ordinal()];
    }

}
