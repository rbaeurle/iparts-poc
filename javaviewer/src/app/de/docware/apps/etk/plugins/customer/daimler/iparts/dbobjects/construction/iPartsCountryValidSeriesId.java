/*
 * Copyright (c) 2020 Quanos
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine ID aus der Tabelle DA_COUNTRY_VALID_SERIES im iParts Plug-in.
 * <p>
 * Baureihe + Land, bei denen die StarParts grundsätzlich ausgegeben werden dürfen.
 */
public class iPartsCountryValidSeriesId extends IdWithType {

    public static String TYPE = "DA_iPartsCountryValidSeriesId";
    public static final String DESCRIPTION = "!!StarParts der Baureihe im Land ausgeben.";

    protected enum INDEX {SERIES_NO, COUNTRY_CODE}

    /**
     * Der normale Konstruktor
     */
    public iPartsCountryValidSeriesId(String seriesNo, String countryCode) {
        super(TYPE, new String[]{ seriesNo, countryCode });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsCountryValidSeriesId() {
        this("", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getSeriesNo() {
        return id[INDEX.SERIES_NO.ordinal()];
    }

    public String getCountryCode() {
        return id[INDEX.COUNTRY_CODE.ordinal()];
    }

    public boolean isValidCountryCode(EtkProject project) {
        return iPartsLanguage.isValidDaimlerIsoCountryCode(project, getCountryCode());
    }
}
