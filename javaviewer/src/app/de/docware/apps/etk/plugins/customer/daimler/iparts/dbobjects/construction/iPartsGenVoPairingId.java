/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine ID für ein Links-Rechts-Pärchen zu GenVO (Tabelle DA_GENVO_PAIRING) im iParts Plug-in.
 */
public class iPartsGenVoPairingId extends IdWithType {

    public static final String TYPE = "DA_GenVoPairingId";

    private enum INDEX {GENVO_L, GENVO_R}

    /**
     * Der normale Konstruktor
     */
    public iPartsGenVoPairingId(String genvo_left, String genvo_right) {
        super(TYPE, new String[]{ genvo_left, genvo_right });
    }

    /**
     * Für iPartsGenVoPairingId basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsGenVoPairingId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsGenVoPairingId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsGenVoPairingId() {
        this("", "");
    }

    public String getGenVoLeft() {
        return id[INDEX.GENVO_L.ordinal()];
    }

    public String getGenVoRight() {
        return id[INDEX.GENVO_R.ordinal()];
    }
}
