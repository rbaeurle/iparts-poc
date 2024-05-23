/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataReplaceConstMat}.
 */
public class iPartsDataReplaceConstMatList extends EtkDataObjectList<iPartsDataReplaceConstMat> implements iPartsConst {

    @Override
    protected iPartsDataReplaceConstMat getNewDataObject(EtkProject project) {
        return new iPartsDataReplaceConstMat(project, null);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataReplaceConstMat} für das angegebene Teil (der Nachfolger)
     *
     * @param project
     * @param partNumber
     * @return
     */
    public static iPartsDataReplaceConstMatList loadDataReplacementForPart(EtkProject project, String partNumber) {
        iPartsDataReplaceConstMatList list = new iPartsDataReplaceConstMatList();
        list.loadDataReplacementForPartFromDB(project, partNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataReplaceConstMat} aus der DB für das angegebene Teil (der Nachfolger).
     * Aber nur solche Datensätze, bei denen der Vorgänger nicht leer ist
     *
     * @param project
     * @param partNumber
     * @param origin
     */
    private void loadDataReplacementForPartFromDB(EtkProject project, String partNumber, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_REPLACE_CONST_MAT, new String[]{ FIELD_DRCM_PART_NO }, new String[]{ partNumber },
                          new String[]{ FIELD_DRCM_PRE_PART_NO }, new String[]{ "" },
                          new String[]{ FIELD_DRCM_SDATA }, LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataReplaceConstMat} für das angegebene Teil (der Vorgänger)
     *
     * @param project
     * @param predecessorPartNumber
     * @return
     */
    public static iPartsDataReplaceConstMatList loadDataReplacementForPredecessor(EtkProject project, String predecessorPartNumber) {
        iPartsDataReplaceConstMatList list = new iPartsDataReplaceConstMatList();
        list.loadDataReplacementForPredecessorFromDB(project, predecessorPartNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataReplaceConstMat} aus der DB für das angegebene Teil (der Vorgänger)
     *
     * @param project
     * @param predecessorPartNumber
     * @param origin
     */
    private void loadDataReplacementForPredecessorFromDB(EtkProject project, String predecessorPartNumber, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_REPLACE_CONST_MAT, new String[]{ FIELD_DRCM_PRE_PART_NO }, new String[]{ predecessorPartNumber },
                          new String[]{ FIELD_DRCM_PART_NO, FIELD_DRCM_SDATA }, LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataReplaceConstMat} für den angegebenen Vorgänger und Nachfolger
     * für den angegebenen Stand
     *
     * @param project
     * @param predecessorPartNumber
     * @param sDatA
     * @param successorPartNumber
     * @return
     */
    public static iPartsDataReplaceConstMatList loadDataReplacementForPartsAndSDATA(EtkProject project, String predecessorPartNumber,
                                                                                    String sDatA, String successorPartNumber) {
        iPartsDataReplaceConstMatList list = new iPartsDataReplaceConstMatList();
        list.loadDataReplacementForPartsAndSDATAFromDB(project, predecessorPartNumber, sDatA, successorPartNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadDataReplacementForPartsAndSDATAFromDB(EtkProject project, String predecessorPartNumber,
                                                           String sDatA, String successorPartNumber, DBActionOrigin origin) {
        String[] whereFields = new String[]{ FIELD_DRCM_PRE_PART_NO, FIELD_DRCM_SDATA, FIELD_DRCM_PART_NO };
        String[] whereValues = new String[]{ predecessorPartNumber, sDatA, successorPartNumber };

        clear(origin);

        searchAndFill(project, TABLE_DA_REPLACE_CONST_MAT,
                      whereFields, whereValues,
                      LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle relevanten Ersetzungen für eine DIALOG Konstruktionsstückliste. D.h alle Ersetzungen für die gilt:
     * Nachfolger-Materialnummer der Ersetzung = Materialnummer des Stücklisteneintrags der HmMSm Stückliste und
     * SDATA der Ersetzung = SDATA des Stücklisteneintrags und Vorgänger-Materialnummer ist nicht leer.
     *
     * @param project
     * @param hmMSmId
     * @return
     */
    public static iPartsDataReplaceConstMatList loadAllReplacementsForHmMSm(EtkProject project, HmMSmId hmMSmId) {
        iPartsDataReplaceConstMatList list = new iPartsDataReplaceConstMatList();
        list.loadAllReplacementsForHmMSmFromDB(project, hmMSmId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllReplacementsForHmMSmFromDB(EtkProject project, HmMSmId hmMSmId, DBActionOrigin origin) {

        clear(origin);
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_PRE_PART_NO, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_RFME, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_CONST_MAT, FIELD_DRCM_PRE_RFME, false, false));
        String[] whereFields = { FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM, FIELD_DRCM_PRE_PART_NO };
        String[] whereValues = { hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm(), EtkDataObjectList.getNotWhereValue("") };

        // Distinct-Abfrage ist wichtig, um keine doppelten Datensätze zu bekommen, was schlecht für die Performance wäre
        // bei der Erzeugung der Ersetzungen
        searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields, whereFields, whereValues, false, null,
                                  false, false, true, null,
                                  new EtkDataObjectList.JoinData(TABLE_DA_DIALOG,
                                                                 new String[]{ FIELD_DRCM_PART_NO, FIELD_DRCM_SDATA },
                                                                 new String[]{ FIELD_DD_PARTNO, FIELD_DD_SDATA },
                                                                 false, false));
    }
}
