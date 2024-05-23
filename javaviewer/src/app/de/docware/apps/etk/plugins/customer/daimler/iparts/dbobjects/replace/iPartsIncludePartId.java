/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace;

import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Klasse für eine IncludePart-ID (Mitlieferteil)
 */
public class iPartsIncludePartId extends IdWithType {

    public static final String TYPE = "DA_iPartsIncludePartId";
    public static final String DESCRIPTION = "!!Mitlieferteil";

    public static final int SEQNO_LENGTH = 2;

    protected enum INDEX {IN_VARI, IN_VER, IN_LFDNR, IN_REPLACE_MATNR, IN_REPLACE_LFDNR, IN_SEQNO}

    /**
     * Der Standardkonstruktor
     */
    public iPartsIncludePartId(String inVari, String inVer, String inLfdNr, String inReplaceMatNr, String inReplaceLfdNr, String inSeqNo) {
        super(TYPE, new String[]{ inVari, inVer, inLfdNr, inReplaceMatNr, inReplaceLfdNr, inSeqNo });
    }

    /**
     * Konstruktor, dem die zugehörige Ersetzung übergeben wird
     */
    public iPartsIncludePartId(iPartsDataReplacePart replacePart, String inSeqNo) {
        super(TYPE, new String[]{ replacePart.getAsId().getReplaceVari(),
                                  replacePart.getAsId().getReplaceVer(),
                                  replacePart.getAsId().getPredecessorLfdNr(),
                                  replacePart.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_MATNR),
                                  replacePart.getFieldValue(iPartsConst.FIELD_DRP_REPLACE_LFDNR),
                                  inSeqNo });
    }

    /**
     * Für IncludePart basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsIncludePartId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsIncludePartId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsIncludePartId() {
        this("", "", "", "", "", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getIncludeVari() {
        return id[INDEX.IN_VARI.ordinal()];
    }

    public String getIncludeVer() {
        return id[INDEX.IN_VER.ordinal()];
    }

    public String getPredecessorLfdNr() {
        return id[INDEX.IN_LFDNR.ordinal()];
    }

    public String getIncludeReplaceMatNr() {
        return id[INDEX.IN_REPLACE_MATNR.ordinal()];
    }

    public String getIncludeReplaceLfdNr() {
        return id[INDEX.IN_REPLACE_LFDNR.ordinal()];
    }

    public String getIncludeSeqNo() {
        return id[INDEX.IN_SEQNO.ordinal()];
    }

    public PartListEntryId getPredecessorPartListEntryId() {
        if (StrUtils.isValid(getIncludeVari(), getPredecessorLfdNr())) {
            return new PartListEntryId(getIncludeVari(), getIncludeVer(), getPredecessorLfdNr());
        }
        return null;
    }
}
