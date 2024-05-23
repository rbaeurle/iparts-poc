/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataReportConstNode} Objekten
 */
public class iPartsDataReportConstNodeList extends EtkDataObjectList<iPartsDataReportConstNode> implements iPartsConst {

    /**
     * Löscht alle {@Link iPartsDataReportConstNode}s zu einer bestimmten Changeset-GUID.
     *
     * @param project
     * @param changeSetGuid
     */
    public static void deleteAllDataForChangesetGuid(EtkProject project, String changeSetGuid) {
        String[] whereFields = new String[]{ FIELD_DRCN_CHANGESET_GUID };
        String[] whereValues = new String[]{ changeSetGuid };
        project.getDbLayer().delete(TABLE_DA_REPORT_CONST_NODES, whereFields, whereValues);
    }

    @Override
    protected iPartsDataReportConstNode getNewDataObject(EtkProject project) {
        return new iPartsDataReportConstNode(project, null);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataReportConstNode}s zu einer bestimmten Baureihe.
     *
     * @param project
     * @param seriesNo
     */
    public void loadAllDataForSeries(EtkProject project, String seriesNo) {
        String[] whereFields = new String[]{ FIELD_DRCN_SERIES_NO };
        String[] whereValues = new String[]{ seriesNo };
        localSearchSortAndFill(project, whereFields, whereValues, DBActionOrigin.FROM_DB);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@Link iPartsDataReportConstNode}s zu einer bestimmten Changeset-GUID.
     *
     * @param project
     * @param changeSetGuid
     */
    public void loadAllDataForChangesetGuid(EtkProject project, String changeSetGuid) {
        String[] whereFields = new String[]{ FIELD_DRCN_CHANGESET_GUID };
        String[] whereValues = new String[]{ changeSetGuid };
        localSearchSortAndFill(project, whereFields, whereValues, DBActionOrigin.FROM_DB);
    }

    /**
     * Füllt die Liste mit Daten
     *
     * @param project
     * @param whereFields
     * @param whereValues
     * @param origin
     */
    private void localSearchSortAndFill(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin) {
        String[] sortFields = new String[]{ FIELD_DRCN_SERIES_NO, FIELD_DRCN_NODE_ID, FIELD_DRCN_CHANGESET_GUID, FIELD_DRCN_CALCULATION_DATE };
        searchSortAndFill(project, TABLE_DA_REPORT_CONST_NODES, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }
}
