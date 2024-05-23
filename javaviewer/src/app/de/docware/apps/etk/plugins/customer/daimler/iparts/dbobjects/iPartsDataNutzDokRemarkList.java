/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataNutzDokRemarkList extends EtkDataObjectList<iPartsDataNutzDokRemark> implements iPartsConst {

    // Der normale Constructor.
    public iPartsDataNutzDokRemarkList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataNutzDokRemarkList} aus der Datenbank,
     * passend zur Referenz-ID und Referenz-Typ
     *
     * @param project
     * @return
     */
    public static iPartsDataNutzDokRemarkList loadRemarksForReferenceFromDB(EtkProject project, String refId) {
        iPartsDataNutzDokRemarkList list = new iPartsDataNutzDokRemarkList();
        list.loadRemarksForReferenceFromDB(project, refId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataNutzDokRemarkList} aus der Datenbank,
     * passend zur Referenz-ID und Referenz-Typ
     *
     * @param project
     * @return
     */
    public static iPartsDataNutzDokRemarkList loadRemarksForReferenceFromDB(EtkProject project, String refId, String refType) {
        iPartsDataNutzDokRemarkList list = new iPartsDataNutzDokRemarkList();
        list.loadRemarksForReferenceFromDB(project, refId, refType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataNutzDokRemarkList} aus der Datenbank,
     * passend zur Referenz-ID, Referenz-Typ und Remark-ID
     *
     * @param project
     * @return
     */
    public static iPartsDataNutzDokRemarkList loadRemarksForReferenceFromDB(EtkProject project, String refId, String refType, String remarkId) {
        iPartsDataNutzDokRemarkList list = new iPartsDataNutzDokRemarkList();
        list.loadRemarksForReferenceFromDB(project, refId, refType, remarkId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataNutzDokRemarkList} aus der Datenbank,
     * passend zur Remark-ID
     *
     * @param project
     * @param remarkId
     * @return
     */
    public static iPartsDataNutzDokRemarkList loadRemarksForRemarkIdFromDB(EtkProject project, String remarkId) {
        iPartsDataNutzDokRemarkList list = new iPartsDataNutzDokRemarkList();
        list.loadRemarksForRemarkIdFromDB(project, remarkId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataNutzDokRemarkList} aus der Datenbank
     *
     * @param project
     * @return
     */
    public static iPartsDataNutzDokRemarkList loadAllEntriesFromDB(EtkProject project) {
        iPartsDataNutzDokRemarkList list = new iPartsDataNutzDokRemarkList();
        list.loadAllEntriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataNutzDokRemarkList} aus der Datenbank,
     * passend zur Referenz-ID und Referenz-Typ
     *
     * @param project
     * @param refId
     * @param origin
     * @return
     */
    private void loadRemarksForReferenceFromDB(EtkProject project, String refId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DNR_REF_ID };
        String[] whereValues = new String[]{ refId };

        searchSortAndFill(project, TABLE_DA_NUTZDOK_REMARK, whereFields, whereValues, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataNutzDokRemarkList} aus der Datenbank,
     * passend zur Referenz-ID und Referenz-Typ
     *
     * @param project
     * @param refId
     * @param refType
     * @param origin
     * @return
     */
    private void loadRemarksForReferenceFromDB(EtkProject project, String refId, String refType, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DNR_REF_ID, FIELD_DNR_REF_TYPE };
        String[] whereValues = new String[]{ refId, refType };

        searchSortAndFill(project, TABLE_DA_NUTZDOK_REMARK, whereFields, whereValues, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataNutzDokRemarkList} aus der Datenbank,
     * passend zur Referenz-ID, Referenz-Typ und Remark-ID
     *
     * @param project
     * @param refId
     * @param refType
     * @param remarkId
     * @param origin
     * @return
     */
    private void loadRemarksForReferenceFromDB(EtkProject project, String refId, String refType, String remarkId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DNR_REF_ID, FIELD_DNR_REF_TYPE, FIELD_DNR_ID };
        String[] whereValues = new String[]{ refId, refType, remarkId };

        searchSortAndFill(project, TABLE_DA_NUTZDOK_REMARK, whereFields, whereValues, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste von {@link iPartsDataNutzDokRemarkList} aus der Datenbank,
     * passend zur remark-ID
     *
     * @param project
     * @param remarkId
     * @param origin
     * @return
     */
    private void loadRemarksForRemarkIdFromDB(EtkProject project, String remarkId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DNR_ID };
        String[] whereValues = new String[]{ remarkId };

        searchSortAndFill(project, TABLE_DA_NUTZDOK_REMARK, whereFields, whereValues, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataNutzDokRemark} aus der Datenbank
     *
     * @param project
     * @param origin
     * @return
     */
    private void loadAllEntriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_NUTZDOK_REMARK, null, null, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Liefert die Liste der Sortierfelder zum Lesen aus der Datebank für dieses Objekt.
     *
     * @return
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DNR_REF_ID, FIELD_DNR_REF_TYPE, FIELD_DNR_ID };
    }

    /**
     * Neues {@link EtkDataObject} erzeugen für den Aufbau der {@link EtkDataObjectList}
     *
     * @param project
     * @return
     */
    @Override
    protected iPartsDataNutzDokRemark getNewDataObject(EtkProject project) {
        return new iPartsDataNutzDokRemark(project, null);
    }

}
