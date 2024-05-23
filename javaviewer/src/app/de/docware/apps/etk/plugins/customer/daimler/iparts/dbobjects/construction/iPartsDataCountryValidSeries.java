/*
 * Copyright (c) 2020 Quanos
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link de.docware.apps.etk.base.project.base.EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_COUNTRY_VALID_SERIES.
 * StarParts-Teile nur noch in erlaubten Ländern ausgeben, Baureihe + Land, bei denen die StarParts grundsätzlich ausgegeben werden dürfen.
 */
public class iPartsDataCountryValidSeries extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DCVS_SERIES_NO, FIELD_DCVS_COUNTRY_CODE };

    public iPartsDataCountryValidSeries(EtkProject project, iPartsCountryValidSeriesId id) {
        super(KEYS);
        tableName = TABLE_DA_COUNTRY_VALID_SERIES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsCountryValidSeriesId createId(String... idValues) {
        return new iPartsCountryValidSeriesId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsCountryValidSeriesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsCountryValidSeriesId)id;
    }

    public String getSeriesNo() {
        return getAsId().getSeriesNo();
    }

    public String getCountryCode() {
        return getAsId().getCountryCode();
    }
}
