/*
 * Copyright (c) 2020 Quanos
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataCountryInvalidParts} Objekten
 * StarParts-Teile nur noch in erlaubten Ländern ausgeben, Bauteile pro Land, die (!)NICHT(!) ausgegeben werden dürfen!
 */
public class iPartsDataCountryInvalidPartsList extends EtkDataObjectList<iPartsDataCountryInvalidParts> implements iPartsConst {

    public iPartsDataCountryInvalidPartsList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert alle {@link iPartsDataCountryInvalidParts}s aus der Tabelle TABLE_DA_COUNTRY_INVALID_PARTS
     * sortiert nach siehe: getSortFields()
     *
     * @param project
     * @return
     */
    public static iPartsDataCountryInvalidPartsList getAllCountryInvalidParts(EtkProject project) {
        iPartsDataCountryInvalidPartsList list = new iPartsDataCountryInvalidPartsList();
        list.loadFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    @Override
    protected iPartsDataCountryInvalidParts getNewDataObject(EtkProject project) {
        return new iPartsDataCountryInvalidParts(project, null);
    }

    /**
     * Sortierfelder:
     * Prinzipiell sollen alle in dieser Klasse zusammengestellten Ergebnislisten gleich sortiert sein,
     * daher gibt es diese zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DCIP_PART_NO, FIELD_DCIP_COUNTRY_CODE };
    }

    /**
     * Lädt alle Daten aus TABLE_DA_COUNTRY_INVALID_PARTS sortiert
     *
     * @param project Das Projekt
     */
    public void loadFromDB(EtkProject project, DBActionOrigin origin) {
        searchSortAndFill(project, TABLE_DA_COUNTRY_INVALID_PARTS, null, null, getSortFields(), LoadType.COMPLETE, origin);
    }
}
