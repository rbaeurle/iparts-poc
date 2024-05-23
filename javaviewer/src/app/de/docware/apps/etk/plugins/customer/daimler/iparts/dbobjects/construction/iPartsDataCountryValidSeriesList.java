/*
 * Copyright (c) 2020 Quanos
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataCountryValidSeries} Objekten
 * StarParts-Teile nur noch in erlaubten Ländern ausgeben, Baureihe + Land, bei denen die StarParts grundsätzlich ausgegeben werden dürfen.
 */
public class iPartsDataCountryValidSeriesList extends EtkDataObjectList<iPartsDataCountryValidSeries> implements iPartsConst {

    public iPartsDataCountryValidSeriesList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert alle {@link iPartsDataCountryValidSeries}s aus der Tabelle TABLE_DA_COUNTRY_VALID_SERIES
     * sortiert nach siehe: getSortFields()
     *
     * @param project
     * @return
     */

    public static iPartsDataCountryValidSeriesList getAllCountryValidSeries(EtkProject project) {
        iPartsDataCountryValidSeriesList list = new iPartsDataCountryValidSeriesList();
        list.loadFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    @Override
    protected iPartsDataCountryValidSeries getNewDataObject(EtkProject project) {
        return new iPartsDataCountryValidSeries(project, null);
    }

    /**
     * Sortierfelder:
     * Prinzipiell sollen alle in dieser Klasse zusammengestellten Ergebnislisten gleich sortiert sein,
     * daher gibt es diese zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DCVS_SERIES_NO, FIELD_DCVS_COUNTRY_CODE };
    }

    /**
     * Lädt alle Daten aus TABLE_DA_COUNTRY_VALID_SERIES sortiert
     *
     * @param project Das Projekt
     */
    public void loadFromDB(EtkProject project, DBActionOrigin origin) {
        searchSortAndFill(project, TABLE_DA_COUNTRY_VALID_SERIES, null, null, getSortFields(), LoadType.COMPLETE, origin);
    }
}
