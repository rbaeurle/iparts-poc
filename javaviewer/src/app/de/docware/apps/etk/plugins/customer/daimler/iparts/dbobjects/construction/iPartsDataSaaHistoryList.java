/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;

/**
 * Liste mit {@link iPartsDataSaaHistory} Objekten
 */
public class iPartsDataSaaHistoryList extends EtkDataObjectList<iPartsDataSaaHistory> implements iPartsConst {

    public iPartsDataSaaHistoryList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataSaaHistory}s, die der übergebenen SAA zugeordnet sind.
     *
     * @param project
     * @param saaKey
     * @return
     */
    public static iPartsDataSaaHistoryList loadSaaHistoryDataForSaa(EtkProject project, String saaKey) {
        iPartsDataSaaHistoryList list = new iPartsDataSaaHistoryList();
        list.loadSaaHistoryDataForSaaFromDB(project, saaKey, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataSaaHistory}s, die der übergebenen SAA zugeordnet sind.
     *
     * @param project
     * @param saaKey
     * @param origin
     * @return
     */
    public void loadSaaHistoryDataForSaaFromDB(EtkProject project, String saaKey, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DSH_SAA };
        String[] whereValues = new String[]{ saaKey };
        String[] sortFields = new String[]{ FIELD_DSH_SAA, FIELD_DSH_REV_FROM };
        searchSortAndFill(project, TABLE_DA_SAA_HISTORY, whereFields, whereValues, sortFields, DBDataObjectList.LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataSaaHistory getNewDataObject(EtkProject project) {
        return new iPartsDataSaaHistory(project, null);
    }

}
