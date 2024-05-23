/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Codestamm-GUID aus der Tabelle TABLE_DA_CODE im iParts Plug-in.
 */
public class iPartsCodeDataId extends IdWithType {

    public static final String TYPE = "DA_iPartsCodeDataId";

    protected enum INDEX {CODE_ID, SERIES_NO, PGRP, SDATA, SOURCE}

    /**
     * Der normale Konstruktor
     */
    public iPartsCodeDataId(String codeId, String seriesNo, String productGroup, String sdata, iPartsImportDataOrigin source) {

        super(TYPE, new String[]{ codeId, seriesNo, productGroup, sdata, source.getOrigin() });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsCodeDataId() {
        this("", "", "", "", iPartsImportDataOrigin.UNKNOWN);
    }

    public String getCodeId() {
        return id[INDEX.CODE_ID.ordinal()];
    }

    public String getProductGroup() {
        return id[INDEX.PGRP.ordinal()];
    }

    public String getSdata() {
        return id[INDEX.SDATA.ordinal()];
    }

    public String getSeriesNo() {
        return id[INDEX.SERIES_NO.ordinal()];
    }

    public iPartsImportDataOrigin getSource() {
        return iPartsImportDataOrigin.getTypeFromCode(id[INDEX.SOURCE.ordinal()]);
    }
}
