/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsExportState;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataExportContentList extends EtkDataObjectList<iPartsDataExportContent> implements iPartsConst {

    /**
     * Lädt den kompletten Inhalt aus DA_EXPORT_CONTENT in eine Liste.
     * Mal sehen, ob das so überhaupt benötigt wird.
     *
     * @param project
     * @param origin
     */
    public void loadAllContentsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_EXPORT_CONTENT, null, null, LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und läd eine Liste aller {@link iPartsDataExportContent} für die übergebene {@link iPartsExportRequestId}.
     *
     * @param project
     * @return
     */
    public static iPartsDataExportContentList loadExportContentsForJobId(EtkProject project, iPartsExportRequestId requestId) {
        iPartsDataExportContentList list = new iPartsDataExportContentList();
        list.loadExportContentsForJobIdFromDB(project, requestId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadExportContentsForJobIdFromDB(EtkProject project, iPartsExportRequestId requestId, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DEC_JOB_ID };
        String[] whereValues = new String[]{ requestId.getJobId() };
        searchAndFill(project, TABLE_DA_EXPORT_CONTENT, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und läd eine Liste aller {@link iPartsDataExportContent} mit Status {@link iPartsExportState#NEW},
     * nach Job-Id sortiert.
     *
     * @param project
     * @return
     */
    public static iPartsDataExportContentList loadNewExportContents(EtkProject project) {
        iPartsDataExportContentList list = new iPartsDataExportContentList();
        list.loadNewExportContentsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadNewExportContentsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DEC_STATE };
        String[] whereValues = new String[]{ iPartsExportState.NEW.getDbValue() };
        String[] sortFields = new String[]{ FIELD_DEC_JOB_ID };
        searchSortAndFill(project, TABLE_DA_EXPORT_CONTENT, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataExportContent getNewDataObject(EtkProject project) {
        return new iPartsDataExportContent(project, null);
    }
}
