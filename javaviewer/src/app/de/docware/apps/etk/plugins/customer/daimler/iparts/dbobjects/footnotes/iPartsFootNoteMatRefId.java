/*
 * Copyright (c) 2018 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine FN-MAT-Ref-ID (Tabelle DA_FN_MAT_REF) im iParts Plug-in.
 */
public class iPartsFootNoteMatRefId extends IdWithType {

    public static String TYPE = "DA_iPartsFootNoteMatRefId";
    public static String DESCRIPTION = "!!Material-Fußnotenreferenz";

    protected enum INDEX {MATNR, FNID}

    /**
     * Der normale Konstruktor
     *
     * @param matNumber
     * @param footNoteId
     */
    public iPartsFootNoteMatRefId(String matNumber, String footNoteId) {
        super(TYPE, new String[]{ matNumber, footNoteId });
    }

    /**
     * Für FN-MAT-Ref basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsFootNoteMatRefId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsFootNoteMatRefId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsFootNoteMatRefId() {
        this("", "");
    }

    public String getMatNumber() {
        return id[INDEX.MATNR.ordinal()];
    }

    public PartId getAsPartId() {
        return new PartId(getMatNumber(), "");
    }

    public String getFootNoteId() {
        return id[INDEX.FNID.ordinal()];
    }

    public iPartsFootNoteId getAsFootNoteId() {
        return new iPartsFootNoteId(getFootNoteId());
    }

    public String getDescription() {
        return DESCRIPTION;
    }
}
