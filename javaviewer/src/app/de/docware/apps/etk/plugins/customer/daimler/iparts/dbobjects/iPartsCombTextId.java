/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert den Schlüssel für einen kombinierten Ergänzungstexteintrag aus DA_COMB_TEXT
 */
public class iPartsCombTextId extends IdWithType {

    public static final String TYPE = "DA_iPartsCombTextId";
    public static final String DESCRIPTION = "!!Kombinierter Text";

    private enum INDEX {
        MODULE,      // k_vari
        MODVER,      // k_ver
        SEQNO,       // k_lfdnr
        TEXT_SEQNO
    }

    /**
     * Der normale Konstruktor
     *
     * @param module
     * @param modver
     * @param seqNo
     * @param textSeqNo
     */
    public iPartsCombTextId(String module, String modver, String seqNo, String textSeqNo) {
        super(TYPE, new String[]{ module, modver, seqNo, textSeqNo });
    }

    /**
     * Konstruktor mit einer {@link PartListEntryId}.
     *
     * @param partListEntryId
     * @param textSeqNo
     */
    public iPartsCombTextId(PartListEntryId partListEntryId, String textSeqNo) {
        super(TYPE, new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr(), textSeqNo });
    }

    /**
     * Für kombinierten Ergänzungstexteintrag basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsCombTextId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsCombTextId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsCombTextId() {
        this("", "", "", "");
    }

    public String getModuleId() {
        return id[INDEX.MODULE.ordinal()];
    }

    public String getModuleVer() {
        return id[INDEX.MODVER.ordinal()];
    }

    public String getModuleSeqNo() {
        return id[INDEX.SEQNO.ordinal()];
    }

    public String getTextSeqNo() {
        return id[INDEX.TEXT_SEQNO.ordinal()];
    }

    public PartListEntryId getPartListEntryId() {
        return new PartListEntryId(getModuleId(), getModuleVer(), getModuleSeqNo());
    }

    public String getDescription() {
        return DESCRIPTION;
    }
}
