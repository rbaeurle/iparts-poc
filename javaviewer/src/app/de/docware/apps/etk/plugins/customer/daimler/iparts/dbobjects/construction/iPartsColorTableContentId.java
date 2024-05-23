/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Farbtabelleninhalt-GUID aus der Tabelle TABLE_DA_COLORTABLE_CONTENT im iParts Plug-in.
 */
public class iPartsColorTableContentId extends IdWithType {

    public static final String TYPE = "DA_iPartsColorTableContentId";
    public static final String DESCRIPTION = "!!Variante zu Variantentabelle";

    protected enum INDEX {COLOR_TABLE_ID, POS, SDATA}

    /**
     * Der normale Konstruktor
     */
    public iPartsColorTableContentId(String colorTableId, String pos, String sdata) {

        super(TYPE, new String[]{ colorTableId, pos, sdata });
    }

    /**
     * Für Farbtabelleninhalt basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsColorTableContentId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsColorTableContentId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    public iPartsColorTableContentId(iPartsColorTableFactoryId colorTableFactoryId) {
        super(TYPE, new String[]{ colorTableFactoryId.getTableId(), colorTableFactoryId.getPos(), colorTableFactoryId.getSdata() });
    }


    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsColorTableContentId() {
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
