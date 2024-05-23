/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsExportState;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.TableAndFieldName;

public class iPartsDataExportRequestList extends EtkDataObjectList<iPartsDataExportRequest> implements iPartsConst {

    /**
     * Funktion, die die Ergebnisfelder für alle Select-Statements zusammenstellt.
     *
     * @param project
     * @return
     */
    private EtkDisplayFields getSelectFields(EtkProject project) {
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_EXPORT_REQUEST));
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_EXPORT_CONTENT));
        return selectFields;
    }

    /**
     * Sortierfelder:
     * Prinzipiell sollen alle in dieser Klasse zusammengestellten Ergebnislisten gleich sortiert sein,
     * daher gibt es diese zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return
     */
    private String[] getSortFields() {
        String[] sortFields = new String[]{ FIELD_DEC_JOB_ID, FIELD_DEC_SEQNO };
        return sortFields;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataExportRequest}s in der Tabelle DA_EXPORT_REQUEST
     * und joint die Einzelaufträge aus TABLE_DA_EXPORT_CONTENT dazu.
     *
     * @param project
     * @param origin
     */
    public void loadAllExportRequests(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = getSelectFields(project);
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DER_JOB_ID },
                                  TABLE_DA_EXPORT_CONTENT, new String[]{ FIELD_DEC_JOB_ID },
                                  false, false,
                                  null, null,
                                  false, getSortFields(), false);
    }

    /**
     * Erzeugt und lädt eine Liste der Exportanforderungen {@link iPartsDataExportRequest}s für eine RequestID
     * aus der Tabelle DA_EXPORT_REQUEST und joint die Einzelaufträge aus TABLE_DA_EXPORT_CONTENT dazu.
     *
     * @param project
     * @param id
     * @param origin
     */
    public void loadExportRequestsForRequestId(EtkProject project, iPartsExportRequestId id, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = getSelectFields(project);
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DER_JOB_ID },
                                  TABLE_DA_EXPORT_CONTENT, new String[]{ FIELD_DEC_JOB_ID },
                                  false, false,
                                  new String[]{ TableAndFieldName.make(TABLE_DA_EXPORT_REQUEST, FIELD_DER_JOB_ID) }, new String[]{ id.getJobId() },
                                  false, getSortFields(), false);
    }

    /**
     * Erzeugt und lädt eine Liste der Exportanforderungen {@link iPartsDataExportRequest}s eines Kunden
     * aus der Tabelle DA_EXPORT_REQUEST und joint die Einzelaufträge aus TABLE_DA_EXPORT_CONTENT dazu.
     *
     * @param project
     * @param customer
     * @param origin
     */
    public void loadExportRequestsForCustomer(EtkProject project, String customer, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = getSelectFields(project);
        searchSortAndFillWithJoin(project, null, selectFields,
                                  new String[]{ FIELD_DER_JOB_ID },
                                  TABLE_DA_EXPORT_CONTENT, new String[]{ FIELD_DEC_JOB_ID },
                                  false, false,
                                  new String[]{ TableAndFieldName.make(TABLE_DA_EXPORT_REQUEST, FIELD_DER_CUSTOMER_ID) }, new String[]{ customer },
                                  false, getSortFields(), false);
    }

    /**
     * Erzeugt und läd eine Liste aller {@link iPartsDataExportRequest}s mit Status {@link iPartsExportState#NEW},
     * nach Job-Id sortiert.
     *
     * @param project
     * @return
     */
    public static iPartsDataExportRequestList loadExportRequestsWithState(EtkProject project, iPartsExportState state) {
        iPartsDataExportRequestList list = new iPartsDataExportRequestList();
        list.loadNewExportRequestsFromDB(project, state, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadNewExportRequestsFromDB(EtkProject project, iPartsExportState state, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DER_STATE };
        String[] whereValues = new String[]{ state.getDbValue() };
        String[] sortFields = new String[]{ FIELD_DER_JOB_ID };
        searchSortAndFill(project, TABLE_DA_EXPORT_REQUEST, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataExportRequest getNewDataObject(EtkProject project) {
        return new iPartsDataExportRequest(project, null);
    }
}
