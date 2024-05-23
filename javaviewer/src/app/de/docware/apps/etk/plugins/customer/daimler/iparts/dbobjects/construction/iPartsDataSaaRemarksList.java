/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataSaaRemarks} Objekten
 */
public class iPartsDataSaaRemarksList extends EtkDataObjectList<iPartsDataSaaRemarks> implements iPartsConst {

    public iPartsDataSaaRemarksList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataSaaRemarks}s, die der übergebenen SAA und Änderungsstand zugeordnet sind.
     *
     * @param project
     * @param saaNumber
     * @param revFrom
     * @return
     */
    public static iPartsDataSaaRemarksList loadAllRemarksForSAARevision(EtkProject project, String saaNumber, String revFrom) {
        iPartsDataSaaRemarksList list = new iPartsDataSaaRemarksList();
        list.loadRemarksForSAARevFromDB(project, saaNumber, revFrom, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataSaaRemarks}s, die der übergebenen SAA und Änderungsstand zugeordnet sind.
     *
     * @param project
     * @param saaNumber
     * @param revFrom
     * @param origin
     * @return
     */
    public void loadRemarksForSAARevFromDB(EtkProject project, String saaNumber, String revFrom, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DESR_SAA, FIELD_DESR_REV_FROM };
        String[] whereValues = new String[]{ saaNumber, revFrom };
        String[] sortFields = new String[]{ FIELD_DESR_REMARK_NO };
        searchSortAndFill(project, TABLE_DA_EDS_SAA_REMARKS, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle Bemerkung zur übergebenen Saa
     *
     * @param project
     * @param saaNumber
     * @return
     */
    public static iPartsDataSaaRemarksList loadAllRemarksForSaa(EtkProject project, String saaNumber) {
        iPartsDataSaaRemarksList list = new iPartsDataSaaRemarksList();
        list.loadAllRemarksForSaaFromDB(project, saaNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllRemarksForSaaFromDB(EtkProject project, String saaNumber, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DESR_SAA };
        String[] whereValues = new String[]{ saaNumber };
        String[] sortFields = new String[]{ FIELD_DESR_REMARK_NO };
        searchSortAndFill(project, TABLE_DA_EDS_SAA_REMARKS, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataSaaRemarks getNewDataObject(EtkProject project) {
        return new iPartsDataSaaRemarks(project, null);
    }
}
