/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Klasse für eine ReplacePart-ID
 */
public class iPartsReplacePartId extends IdWithType {

    public static final String TYPE = "DA_iPartsReplacePartId";
    public static final String DESCRIPTION = "!!Ersetzung";

    protected enum INDEX {R_VARI, R_VER, R_LFDNR, R_SEQNO}

    /**
     * Der Standardkonstruktor
     */
    public iPartsReplacePartId(String rVari, String rVer, String rLfdNr, String rSeqNo) {
        super(TYPE, new String[]{ rVari, rVer, rLfdNr, rSeqNo });
    }

    public iPartsReplacePartId(String rVari, String rVer, String rLfdNr, int rSeqNo) {
        this(rVari, rVer, rLfdNr, EtkDbsHelper.formatLfdNr(rSeqNo));
    }

    public iPartsReplacePartId(PartListEntryId replacePartListEntryId, int rSeqNo) {
        this(replacePartListEntryId.getKVari(), replacePartListEntryId.getKVer(), replacePartListEntryId.getKLfdnr(), rSeqNo);
    }

    public iPartsReplacePartId(PartListEntryId replacePartListEntryId, String rSeqNo) {
        this(replacePartListEntryId.getKVari(), replacePartListEntryId.getKVer(), replacePartListEntryId.getKLfdnr(), rSeqNo);
    }

    /**
     * Für ReplacePart basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsReplacePartId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsReplacePartId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsReplacePartId() {
        this("", "", "", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getReplaceVari() {
        return id[INDEX.R_VARI.ordinal()];
    }

    public String getReplaceVer() {
        return id[INDEX.R_VER.ordinal()];
    }

    public String getPredecessorLfdNr() {
        return id[INDEX.R_LFDNR.ordinal()];
    }

    public String getSeqNo() {
        return id[INDEX.R_SEQNO.ordinal()];
    }

    public PartListEntryId getPredecessorPartListEntryId() {
        if (StrUtils.isValid(getReplaceVari(), getPredecessorLfdNr())) {
            return new PartListEntryId(getReplaceVari(), getReplaceVer(), getPredecessorLfdNr());
        }
        return null;
    }
}
