/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataAOHistory}s.
 */
public class iPartsDataAOHistoryList extends EtkDataObjectList<iPartsDataAOHistory> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataAOHistory}s für die übergebene Author-GUID.
     *
     * @param project
     * @param authorGuid
     * @param decending
     * @return
     */
    public static iPartsDataAOHistoryList loadAOHistoryList(EtkProject project, String authorGuid, boolean decending) {
        iPartsDataAOHistoryList list = new iPartsDataAOHistoryList();
        list.loadAOHistoriesFromDB(project, authorGuid, DBActionOrigin.FROM_DB, decending);
        return list;
    }


    /**
     * Lädt eine Liste aller {@link iPartsDataAOHistory} für die übergebene Author-GUID.
     *
     * @param project
     * @param authorGuid
     * @param origin
     * @param decending
     */
    private void loadAOHistoriesFromDB(EtkProject project, String authorGuid, DBActionOrigin origin, boolean decending) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DAH_GUID };
        String[] whereValues = new String[]{ authorGuid };
        String[] sortFields = new String[]{ FIELD_DAH_SEQNO };

        searchSortAndFill(project, TABLE_DA_AO_HISTORY, whereFields, whereValues, sortFields, LoadType.COMPLETE, decending, origin);
    }


    @Override
    protected iPartsDataAOHistory getNewDataObject(EtkProject project) {
        return new iPartsDataAOHistory(project, null);
    }
}
