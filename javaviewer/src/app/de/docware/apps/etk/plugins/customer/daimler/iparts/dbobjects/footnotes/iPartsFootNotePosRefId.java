/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine FN-Pos-ID im iParts Plug-in.
 * Tabelle: [DA_FN_POS] für die Fußnoten zur Teileposition aus DIALOG, (VBFN)
 */
public class iPartsFootNotePosRefId extends IdWithType {

    public static String TYPE = "DA_iPartsFootNotePosRefId";
    public static String DESCRIPTION = "!!Teileposition-Fußnotenreferenz";

    protected enum INDEX {
        BCTE_KEY,    // Der BCTE-Schlüssel
        SESI,        // Strukturerzeugende Sicht: 'E' = Entw. bzw. ET, 'Pnnn' = Prod, 'Knnn' = Kalkulation, 'C' = CKD, weitere nach Bedarf
        POSP,        // Positionsnummer Produktion bei SESI <> E
        FN_NO        // Fußnotennummer
    }

    /**
     * Der normale Konstruktor
     *
     * @param bcteKey
     * @param sesi
     * @param posp
     * @param footNoteId
     */
    public iPartsFootNotePosRefId(String bcteKey, String sesi, String posp, String footNoteId) {
        super(TYPE, new String[]{ bcteKey, sesi, posp, footNoteId });
    }

    /**
     * Für FN-Catalogue-Ref basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsFootNotePosRefId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsFootNotePosRefId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsFootNotePosRefId() {
        this("", "", "", "");
    }

    public String getBCTEKey() {
        return id[INDEX.BCTE_KEY.ordinal()];
    }

    public String getSesi() {
        return id[INDEX.SESI.ordinal()];
    }

    public String getPosP() {
        return id[INDEX.POSP.ordinal()];
    }

    public String getFnNo() {
        return id[INDEX.FN_NO.ordinal()];
    }

    public String getDescription() {
        return DESCRIPTION;
    }
}
