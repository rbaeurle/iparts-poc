/*
 * Copyright (c) 2019 Docware GmbH
 *
 * Tabelle [DA_MODEL_BUILDING_CODE], Id für ein Element einer weiteren Liste mit bm-bildende Codes.
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

public class iPartsModelBuildingCodeId extends IdWithType {

    public static String TYPE = "DA_iPartsModelBuildingCodeId";

    protected enum INDEX {SERIES_NO, AA, CODE}

    public iPartsModelBuildingCodeId(String seriesNo, String aa, String code) {
        super(TYPE, new String[]{ seriesNo, aa, code });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsModelBuildingCodeId() {
        this("", "", "");
    }

    public String getSeriesNo() {
        return id[INDEX.SERIES_NO.ordinal()];
    }

    public String getAA() {
        return id[INDEX.AA.ordinal()];
    }

    public String getCode() {
        return id[INDEX.CODE.ordinal()];
    }

}
