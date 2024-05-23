/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataFootNoteSaaRef}s.
 */
public class iPartsDataFootNoteSaaRefList extends EtkDataObjectList<iPartsDataFootNoteSaaRef> implements iPartsConst {

    /**
     * Liefert alle Fußnoten zu eine Saa (sortiert nach FNId, FN_SEQNO)
     *
     * @param project
     * @param saaId
     * @return
     */
    public static iPartsDataFootNoteSaaRefList loadFootNotesForSaaNumber(EtkProject project, iPartsSaaId saaId) {
        iPartsDataFootNoteSaaRefList list = new iPartsDataFootNoteSaaRefList();
        list.loadFootNotesForSaaFromDB(project, saaId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert alle Fußnoten zu einer SAA (sortiert nach FNId, FN_SEQNO)
     *
     * @param project
     * @param saaId
     * @param origin
     */
    private void loadFootNotesForSaaFromDB(EtkProject project, iPartsSaaId saaId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DFNS_SAA };
        String[] whereValues = new String[]{ saaId.getSaaNumber() };
        String[] sortFields = new String[]{ FIELD_DFNS_FNID, FIELD_DFNS_FN_SEQNO };
        searchSortAndFill(project, TABLE_DA_FN_SAA_REF, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    /**
     * Liefert alle Fußnoten zu den SAAs einer SA (sortiert nach SAA, FNId, FN_SEQNO)
     *
     * @param project
     * @param saNumber
     */
    public void loadFootNotesForAllSaasOfSaFromDB(EtkProject project, String saNumber) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereFields = new String[]{ FIELD_DFNS_SAA };
        String[] whereValues = new String[]{ saNumber + "*" };
        String[] sortFields = new String[]{ FIELD_DFNS_SAA, FIELD_DFNS_FNID, FIELD_DFNS_FN_SEQNO };
        searchWithWildCardsSortAndFill(project, whereFields, whereValues, sortFields, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataFootNoteSaaRef getNewDataObject(EtkProject project) {
        return new iPartsDataFootNoteSaaRef(project, null);
    }
}
