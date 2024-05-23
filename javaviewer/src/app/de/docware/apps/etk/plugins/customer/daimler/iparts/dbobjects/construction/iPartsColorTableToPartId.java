/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Teil zu Farbtabelle-GUID aus der Tabelle TABLE_DA_CTABLE_PART im iParts Plug-in.
 */
public class iPartsColorTableToPartId extends IdWithType {

    public static String TYPE = "DA_iPartsColorTableToPartId";
    public static String DESCRIPTION = "!!Variantentabelle";

    protected enum INDEX {COLOR_TABLE_ID, POS, SDATA}

    /**
     * Der normale Konstruktor
     */
    public iPartsColorTableToPartId(String colorTableId, String pos, String sdata) {

        super(TYPE, new String[]{ colorTableId, pos, sdata });
    }

    /**
     * Für Teil zu Farbtabelle basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsColorTableToPartId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsColorTableToPartId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsColorTableToPartId() {
        this("", "", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getColorTableId() {
        return id[INDEX.COLOR_TABLE_ID.ordinal()];
    }

    public String getPosition() {
        return id[INDEX.POS.ordinal()];
    }

    public String getSDATA() {
        return id[INDEX.SDATA.ordinal()];
    }

}
