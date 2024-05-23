/*
 * Copyright (c) 2020 Quanos
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine ID aus der Tabelle DA_COUNTRY_INVALID_PARTS im iParts Plug-in.
 * <p>
 * (StarPart-) Bauteile pro Land, die (!)NICHT(!) ausgegeben werden dürfen!
 */
public class iPartsCountryInvalidPartsId extends IdWithType {

    public static String TYPE = "DA_iPartsCountryInvalidPartsId";
    public static final String DESCRIPTION = "!!StartParts-Teile im Land nicht ausgeben.";

    protected enum INDEX {PART_NO, COUNTRY_CODE}

    /**
     * Der normale Konstruktor
     */
    public iPartsCountryInvalidPartsId(String partNo, String countryCode) {
        super(TYPE, new String[]{ partNo, countryCode });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsCountryInvalidPartsId() {
        this("", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getPartNo() {
        return id[INDEX.PART_NO.ordinal()];
    }

    public String getCountryCode() {
        return id[INDEX.COUNTRY_CODE.ordinal()];
    }

    public boolean isValidCountryCode(EtkProject project) {
        return iPartsLanguage.isValidDaimlerIsoCountryCode(project, getCountryCode());
    }
}
