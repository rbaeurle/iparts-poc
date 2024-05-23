/*
 * Copyright (c) 2018 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;


public class iPartsDataFootNoteMatRefList extends EtkDataObjectList<iPartsDataFootNoteMatRef> implements iPartsConst {

    public iPartsDataFootNoteMatRefList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert alle Fussnoten zu eine Materialnummer (sortiert nach FNId)
     *
     * @param project
     * @param partId
     * @return
     */
    public static iPartsDataFootNoteMatRefList loadFootNotesForMatNumber(EtkProject project, PartId partId, boolean withJoin) {
        return loadFootNotesForMatNumber(project, partId, withJoin, true);
    }

    /**
     * Liefert alle Fussnoten zu eine Materialnummer (sortiert nach FNId)
     *
     * @param project
     * @param partId
     * @return
     */
    public static iPartsDataFootNoteMatRefList loadFootNotesForMatNumber(EtkProject project, PartId partId, boolean withJoin, boolean withoutChangeSet) {
        iPartsDataFootNoteMatRefList list = new iPartsDataFootNoteMatRefList();
        list.setSearchWithoutActiveChangeSets(withoutChangeSet);
        list.loadFootNotesForMatFromDB(project, partId, withJoin, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Liefert alle Fussnoten zu einer Materialnummer (sortiert nach Material, Fussnoten-Id)
     *
     * @param project
     * @param partId
     * @param origin
     */
    private void loadFootNotesForMatFromDB(EtkProject project, PartId partId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DFNM_MATNR };
        String[] whereValues = new String[]{ partId.getMatNr() };
        String[] sortFields = new String[]{ FIELD_DFNM_MATNR, FIELD_DFNM_FNID };
        searchSortAndFill(project, TABLE_DA_FN_MAT_REF, whereFields, whereValues, sortFields,
                          LoadType.COMPLETE, origin);
    }

    /**
     * Liefert alle Fußnoten zu einer Materialnummer (sortiert nach FNId, FN_SEQNO)
     *
     * @param project
     * @param partId
     * @param origin
     */
    private void loadFootNotesForMatFromDB(EtkProject project, PartId partId, boolean withJoin,
                                           DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DFNM_MATNR };
        String[] whereValues = new String[]{ partId.getMatNr() };
        String[] sortFields = new String[]{ FIELD_DFNM_MATNR, FIELD_DFNM_FNID };
        if (withJoin) {
            EtkDisplayFields selectFields = project.getAllDisplayFieldsForTable(TABLE_DA_FN_MAT_REF);
            selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_FN));
            searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields, whereFields, whereValues, false, sortFields,
                                      false, null,
                                      new JoinData(TABLE_DA_FN,
                                                   new String[]{ FIELD_DFNM_FNID },
                                                   new String[]{ FIELD_DFN_ID },
                                                   false, false));
        } else {
            searchSortAndFill(project, TABLE_DA_FN_MAT_REF, whereFields, whereValues, sortFields,
                              LoadType.COMPLETE, origin);
        }
    }

    @Override
    protected iPartsDataFootNoteMatRef getNewDataObject(EtkProject project) {
        return new iPartsDataFootNoteMatRef(project, null);
    }
}
