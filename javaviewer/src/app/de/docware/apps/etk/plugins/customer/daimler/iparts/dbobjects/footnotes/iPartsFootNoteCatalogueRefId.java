/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine FN-Catalogue-Ref-ID (Tabelle DA_FN_KATALOG_REF) im iParts Plug-in.
 */
public class iPartsFootNoteCatalogueRefId extends IdWithType {

    public static final String TYPE = "DA_iPartsFootNoteCatalogueRefId";
    public static final String DESCRIPTION = "!!Fußnotenreferenz";

    protected enum INDEX {MODULE_ID, MODULE_VER, MODULE_SEQNO, FNID}

    /**
     * Der normale Konstruktor
     *
     * @param moduleId
     * @param modulVer
     * @param modulSeqNo
     * @param footNoteId
     */
    public iPartsFootNoteCatalogueRefId(String moduleId, String modulVer, String modulSeqNo, String footNoteId) {
        super(TYPE, new String[]{ moduleId, modulVer, modulSeqNo, footNoteId });
    }

    /**
     * Convenience Konstruktor
     *
     * @param partListEntryId
     * @param footNoteId
     */
    public iPartsFootNoteCatalogueRefId(PartListEntryId partListEntryId, String footNoteId) {
        super(TYPE, new String[]{ partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr(), footNoteId });
    }

    /**
     * Für FN-Catalogue-Ref basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsFootNoteCatalogueRefId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsFootNoteCatalogueRefId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsFootNoteCatalogueRefId() {
        this("", "", "", "");
    }

    public String getModuleId() {
        return id[INDEX.MODULE_ID.ordinal()];
    }

    public String getModuleVer() {
        return id[INDEX.MODULE_VER.ordinal()];
    }

    public String getModuleSeqNo() {
        return id[INDEX.MODULE_SEQNO.ordinal()];
    }

    public String getFootNoteId() {
        return id[INDEX.FNID.ordinal()];
    }

    public PartListEntryId getPartListEntryId() {
        return new PartListEntryId(getModuleId(), getModuleVer(), getModuleSeqNo());
    }

    public String getDescription() {
        return DESCRIPTION;
    }
}