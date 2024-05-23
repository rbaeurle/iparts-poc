package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

public class iPartsDataReplaceConstPartList extends EtkDataObjectList<iPartsDataReplaceConstPart> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataReplaceConstPart} für den angegebenen Vorgänger und Nachfolger
     * für den angegebenen Stand
     *
     * @param project
     * @param predecessorPartNumber
     * @param sDatB
     * @param successorPartNumber
     * @return
     */
    public static iPartsDataReplaceConstPartList loadDataReplacementForPartsAndSDATA(EtkProject project, String predecessorPartNumber,
                                                                                     String sDatB, String successorPartNumber) {
        iPartsDataReplaceConstPartList list = new iPartsDataReplaceConstPartList();
        list.loadDataReplacementForPartsAndSDATAFromDB(project, predecessorPartNumber, sDatB, successorPartNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataReplaceConstPart} für das angegebene Teil (der Nachfolger)
     * für den neusten Stand (sDatB == "")
     *
     * @param project
     * @param partNumber
     * @return
     */
    public static iPartsDataReplaceConstPartList loadDataReplacementForPart(EtkProject project, String partNumber) {
        iPartsDataReplaceConstPartList list = new iPartsDataReplaceConstPartList();
        list.loadDataReplacementForPartFromDB(project, partNumber, null, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataReplaceConstPart} für das angegebene Teil (der Nachfolger)
     * alle Stände
     *
     * @param project
     * @param partNumber
     * @return
     */
    public static iPartsDataReplaceConstPartList loadDataReplacementForPartWithHistory(EtkProject project, String partNumber) {
        iPartsDataReplaceConstPartList list = new iPartsDataReplaceConstPartList();
        list.loadDataReplacementForPartFromDB(project, partNumber, "", DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataReplaceConstPart} für das angegebene Teil (der Vorgänger)
     * für den neusten Stand (sDatB == "")
     *
     * @param project
     * @param predecessorPartNumber
     * @return
     */
    public static iPartsDataReplaceConstPartList loadDataReplacementForPredecessor(EtkProject project, String predecessorPartNumber) {
        iPartsDataReplaceConstPartList list = new iPartsDataReplaceConstPartList();
        list.loadDataReplacementForPredecessorFromDB(project, predecessorPartNumber, null, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataReplaceConstPart} für das angegebene Teil (der Vorgänger)
     * alle Stände
     *
     * @param project
     * @param predecessorPartNumber
     * @return
     */
    public static iPartsDataReplaceConstPartList loadDataReplacementForPredecessorWithHistory(EtkProject project, String predecessorPartNumber) {
        iPartsDataReplaceConstPartList list = new iPartsDataReplaceConstPartList();
        list.loadDataReplacementForPredecessorFromDB(project, predecessorPartNumber, "", DBActionOrigin.FROM_DB);
        return list;
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
    public static iPartsDataReplaceConstPartList loadAllReplacementsForHmMSm(EtkProject project, HmMSmId hmMSmId) {
        iPartsDataReplaceConstPartList list = new iPartsDataReplaceConstPartList();
        list.loadAllReplacementsForHmMSmFromDB(project, hmMSmId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllReplacementsForHmMSmFromDB(EtkProject project, HmMSmId hmMSmId, DBActionOrigin origin) {
        clear(origin);
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_PRE_MATNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_REPLACE_CONST_PART, FIELD_DRCP_RFME, false, false));
        String[] whereFields = { FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM, FIELD_DRCP_PRE_MATNR };
        String[] whereValues = { hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), hmMSmId.getSm(), EtkDataObjectList.getNotWhereValue("") };

        // Distinct-Abfrage ist wichtig, um keine doppelten Datensätze zu bekommen, was schlecht für die Performance wäre
        // bei der Erzeugung der Ersetzungen
        searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields, whereFields, whereValues, false, null,
                                  false, false, true, null,
                                  new EtkDataObjectList.JoinData(TABLE_DA_DIALOG,
                                                                 new String[]{ FIELD_DRCP_PART_NO, FIELD_DRCP_SDATA },
                                                                 new String[]{ FIELD_DD_PARTNO, FIELD_DD_SDATA },
                                                                 false, false));
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataReplaceConstPart} für den angegebenen Vorgänger und Nachfolger
     * für den angegebenen Stand
     *
     * @param project
     * @param predecessorPartNumber
     * @param sDatB
     * @param successorPartNumber
     * @return
     */
    private void loadDataReplacementForPartsAndSDATAFromDB(EtkProject project, String predecessorPartNumber,
                                                           String sDatB, String successorPartNumber, DBActionOrigin origin) {
        String[] whereFields = new String[]{ FIELD_DRCP_PRE_MATNR, FIELD_DRCP_SDATA, FIELD_DRCP_PART_NO };
        String[] whereValues = new String[]{ predecessorPartNumber, sDatB, successorPartNumber };

        clear(origin);

        searchAndFill(project, TABLE_DA_REPLACE_CONST_PART,
                      whereFields, whereValues,
                      LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataReplaceConstPart} aus der DB für das angegebene Teil (der Nachfolger).
     * Aber nur solche Datensätze, bei denen der Vorgänger nicht leer ist
     *
     * @param project
     * @param partNumber
     * @param origin
     */
    private void loadDataReplacementForPartFromDB(EtkProject project, String partNumber, String sDatB, DBActionOrigin origin) {
        String[] whereFields = new String[]{ FIELD_DRCP_PART_NO };
        String[] whereValues = new String[]{ partNumber };

        clear(origin);
        if (sDatB != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DRCP_SDATB });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ "" });
        }
        searchSortAndFill(project, TABLE_DA_REPLACE_CONST_PART,
                          whereFields, whereValues,
                          new String[]{ FIELD_DRCP_PRE_MATNR }, new String[]{ "" },
                          new String[]{ FIELD_DRCP_PART_NO, FIELD_DRCP_SDATA },
                          LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataReplaceConstPart} aus der DB für das angegebene Teil (der Vorgänger)
     *
     * @param project
     * @param predecessorPartNumber
     * @param origin
     */
    private void loadDataReplacementForPredecessorFromDB(EtkProject project, String predecessorPartNumber, String sDatB, DBActionOrigin origin) {
        String[] whereFields = new String[]{ FIELD_DRCP_PRE_MATNR };
        String[] whereValues = new String[]{ predecessorPartNumber };

        clear(origin);
        if (sDatB != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DRCP_SDATB });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ "" });
        }

        searchSortAndFill(project, TABLE_DA_REPLACE_CONST_PART,
                          whereFields, whereValues,
                          new String[]{ FIELD_DRCP_PART_NO, FIELD_DRCP_SDATA },
                          LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataReplaceConstPart getNewDataObject(EtkProject project) {
        return new iPartsDataReplaceConstPart(project, null);
    }
}
