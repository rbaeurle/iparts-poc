/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataMaterialRemark}.
 */
public class iPartsDataMaterialRemarkList extends EtkDataObjectList<iPartsDataMaterialRemark> implements iPartsConst {

    public iPartsDataMaterialRemarkList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMaterialRemark} für eine Materialnummer und Revision aus DA_EDS_MAT_REMARKS
     *
     * @param project Das Projekt
     * @param partNo  Teilenummer
     * @param revFrom Änderungsstand
     */
    public static iPartsDataMaterialRemarkList loadAllRemarksForMaterialAndRevisionFromDB(EtkProject project, String partNo, String revFrom) {
        iPartsDataMaterialRemarkList list = new iPartsDataMaterialRemarkList();
        list.loadAllRemarksForMaterialAndRevisionFromDB(project, partNo, revFrom, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Sortierfelder:
     * Prinzipiell sollen alle in dieser Klasse zusammengestellten Ergebnislisten gleich sortiert sein,
     * daher gibt es diese zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DEMR_PART_NO, FIELD_DEMR_REV_FROM, FIELD_DEMR_REMARK_NO };
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataMaterialRemark} für eine Materialnummer und Revision aus DA_EDS_MAT_REMARKS
     *
     * @param project Das Projekt
     * @param partNo  Teilenummer
     * @param revFrom Änderungsstand
     * @param origin  Die Herkunft
     */
    private void loadAllRemarksForMaterialAndRevisionFromDB(EtkProject project, String partNo, String revFrom, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_EDS_MAT_REMARKS,
                          new String[]{ FIELD_DEMR_PART_NO, FIELD_DEMR_REV_FROM },
                          new String[]{ partNo, revFrom },
                          getSortFields(), LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt alle Bemerkungen zum übergebenen Baukasten (= Teilenummer)
     *
     * @param project
     * @param constKitNumber
     * @return
     */
    public static iPartsDataMaterialRemarkList loadAllRemarksForConstKit(EtkProject project, String constKitNumber) {
        iPartsDataMaterialRemarkList list = new iPartsDataMaterialRemarkList();
        list.loadAllRemarksForConstKitFromDB(project, constKitNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllRemarksForConstKitFromDB(EtkProject project, String partNumber, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DEMR_PART_NO };
        String[] whereValues = new String[]{ partNumber };
        String[] sortFields = new String[]{ FIELD_DEMR_REMARK_NO };
        searchSortAndFill(project, TABLE_DA_EDS_MAT_REMARKS, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataMaterialRemark getNewDataObject(EtkProject project) {
        return new iPartsDataMaterialRemark(project, null);
    }
}
