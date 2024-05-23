package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

/*
 * Copyright (c) 2018 Docware GmbH
 *
 * Tabelle [DA_SERIES_CODES], Id für die DIALOG Baubarkeit (gültige Code zu Baureihe).
 *
 * Die Primärschlüsselfelder
 *       FIELD_DSC_SERIES_NO   // PK // Baureihe "C205", "D6519"
 *       FIELD_DSC_GROUP       // PK // Gruppe (3-stellig) "AAM", "CAG"
 *       FIELD_DSC_POS         // PK // Position "0100"
 *       FIELD_DSC_POSV        // PK // Positionsvariante  "0001"
 *       FIELD_DSC_AA          // PK // AA der BR (z.B. Hubraumcode) "FW", "FS", "M20"
 *       FIELD_DSC_SDATA       // PK // KEM-Status+Datum- AB
 *
 */

import de.docware.util.misc.id.IdWithType;

public class iPartsSeriesCodesDataId extends IdWithType {

    public static String TYPE = "DA_iPartsSeriesCodesId";

    protected enum INDEX {SERIES_NO, GROUP, POS, POSV, AA, SDATA}

    public iPartsSeriesCodesDataId(String seriesNo, String group, String pos, String posv, String aa, String sdata) {
        super(TYPE, new String[]{ seriesNo, group, pos, posv, aa, sdata });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSeriesCodesDataId() {
        this("", "", "", "", "", "");
    }

    // Baureihe "C205", "D6519"
    public String getSeriesNo() {
        return id[INDEX.SERIES_NO.ordinal()];
    }

    // Gruppe (3-stellig) "AAM", "CAG"
    public String getGroup() {
        return id[INDEX.GROUP.ordinal()];
    }

    // Position "0100"
    public String getPos() {
        return id[INDEX.POS.ordinal()];
    }

    // Positionsvariante  "0001"
    public String getPosV() {
        return id[INDEX.POSV.ordinal()];
    }

    // AA der BR (z.B. Hubraumcode) "FW", "FS", "M20"
    public String getAA() {
        return id[INDEX.AA.ordinal()];
    }

    // KEM-Status+Datum- AB
    public String getSDATA() {
        return id[INDEX.SDATA.ordinal()];
    }

    public iPartsSeriesCodesDataId getIdWithoutSDatA() {
        if (id.length > 0) {
            return new iPartsSeriesCodesDataId(getSeriesNo(), getGroup(), getPos(), getPosV(),
                                               getAA(), "");
        } else {
            return new iPartsSeriesCodesDataId("", "", "", "", "", "");
        }
    }
}


