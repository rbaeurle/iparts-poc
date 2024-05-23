/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Konfigurierbarkeit der Pseudo-Einsatztermine durch die Tabelle DA_PSEUDO_PEM_DATE im iParts-Plug-in.
 * Liste von {@link iPartsDataPseudoPEMDate}.
 */
public class iPartsDataPseudoPEMDateList extends EtkDataObjectList<iPartsDataPseudoPEMDate> implements iPartsConst {

    public iPartsDataPseudoPEMDateList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DPD_PEM_DATE };
    }

    public static iPartsDataPseudoPEMDateList loadAllEntriesFromDB(EtkProject project) {
        iPartsDataPseudoPEMDateList list = new iPartsDataPseudoPEMDateList();
        list.loadAllEntriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;

    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPseudoPEMDate} aus der Datenbank.
     *
     * @param project
     * @param origin
     * @return
     */
    private void loadAllEntriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_PSEUDO_PEM_DATE, null, null, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Neues {@link EtkDataObject} erzeugen für den Aufbau der {@link EtkDataObjectList}
     *
     * @param project
     * @return
     */
    protected iPartsDataPseudoPEMDate getNewDataObject(EtkProject project) {
        return new iPartsDataPseudoPEMDate(project, null);
    }

}
