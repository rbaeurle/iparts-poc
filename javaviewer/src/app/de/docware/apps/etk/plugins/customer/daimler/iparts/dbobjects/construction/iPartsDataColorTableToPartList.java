/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste mit {@link iPartsDataColorTableToPart} Objekten
 */
public class iPartsDataColorTableToPartList extends EtkDataObjectList<iPartsDataColorTableToPart> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableToPart}s
     *
     * @param project
     * @param partNumber Teilenummer
     * @return
     */
    public static iPartsDataColorTableToPartList loadColorTableToPartListForPartNumber(EtkProject project, String partNumber) {
        iPartsDataColorTableToPartList list = new iPartsDataColorTableToPartList();
        list.loadColorTableToPartListForPartNumberFromDB(project, partNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataColorTableToPart}s
     *
     * @param project
     * @param partNumber Teilenummer
     * @param origin
     */
    public void loadColorTableToPartListForPartNumberFromDB(EtkProject project, String partNumber, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_PART }, new String[]{ partNumber },
                          new String[]{ FIELD_DCTP_TABLE_ID, FIELD_DCTP_POS, FIELD_DCTP_SDATA },
                          LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableToPart}s mit der übergebenen Farbtabellen ID
     *
     * @param project
     * @param colortableId Farbtabellen Id
     * @return
     */
    public static iPartsDataColorTableToPartList loadColorTableToPartListForColortableId(EtkProject project, iPartsColorTableDataId colortableId) {
        iPartsDataColorTableToPartList list = new iPartsDataColorTableToPartList();
        list.loadColorTableToPartListForColortableIdFromDB(project, colortableId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataColorTableToPart}s
     *
     * @param project
     * @param colortableId Farbtabellen Id
     * @param origin
     */
    public void loadColorTableToPartListForColortableIdFromDB(EtkProject project, iPartsColorTableDataId colortableId, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_TABLE_ID }, new String[]{ colortableId.getColorTableId() },
                          new String[]{ FIELD_DCTP_TABLE_ID, FIELD_DCTP_POS, FIELD_DCTP_SDATA },
                          LoadType.COMPLETE, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableToPart}s für die übergebene Baureihe und Quelle
     *
     * @param project
     * @param seriesNo
     * @param dataOrigin
     * @return
     */
    public static iPartsDataColorTableToPartList loadColortableToPartForSeriesAndSource(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin) {
        iPartsDataColorTableToPartList list = new iPartsDataColorTableToPartList();
        list.loadAllForSeriesAndOrigin(project, seriesNo, dataOrigin, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllForSeriesAndOrigin(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = { FIELD_DCTP_TABLE_ID, FIELD_DCTP_SOURCE };
        String[] whereValues = { ColorTableHelper.makeWhereValueForColorTableWithSeries(new iPartsSeriesId(seriesNo)) + "*", dataOrigin.getOrigin() };

        searchAndFillWithLike(project, TABLE_DA_COLORTABLE_PART, null, whereFields, whereValues, LoadType.ONLY_IDS, false, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataColorTableToPart getNewDataObject(EtkProject project) {
        return new iPartsDataColorTableToPart(project, null);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableToPart}s mit der übergebenen Teileposition und Farbtabellen ID
     *
     * @param project
     * @param colorTableId Farbtabellen Id
     * @param pos
     * @return
     */
    public static iPartsDataColorTableToPartList loadColorTableToPartListForPartNumberAndColortableId(EtkProject project, String colorTableId, String pos) {
        iPartsDataColorTableToPartList list = new iPartsDataColorTableToPartList();
        list.loadColorTableToPartListForPartNumberAndColortableIdFromDB(project, colorTableId, pos, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadColorTableToPartListForPartNumberAndColortableIdFromDB(EtkProject project, String colorTableId, String pos, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_TABLE_ID, FIELD_DCTP_POS },
                      new String[]{ colorTableId, pos }, LoadType.ONLY_IDS, origin);
    }

    public static iPartsDataColorTableToPartList loadColorTableToPartListForNumberAndDate(EtkProject project, String colorTableId, String sdata) {
        iPartsDataColorTableToPartList list = new iPartsDataColorTableToPartList();
        list.loadSimilarColorTableToPartList(project, colorTableId, sdata, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadSimilarColorTableToPartList(EtkProject project, String colorTableId, String sdata, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_TABLE_ID, FIELD_DCTP_SDATA },
                      new String[]{ colorTableId, sdata }, LoadType.ONLY_IDS, origin);
    }

    /**
     * Liefert alle relevanten BCTE-Schlüssel für alle Teile, auf die sich die Farbtabelle bezieht.
     * Aber nur die Schlüssel mit der Baureihe der Farbtabelle.
     */
    public static iPartsDataColorTableToPartList loadWithGUIDandPartNoForColorTable(EtkProject project, String colorTableId) {
        iPartsDataColorTableToPartList list = new iPartsDataColorTableToPartList();
        list.loadWithGUIDandPartNoForColorTable(project, colorTableId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadWithGUIDandPartNoForColorTable(EtkProject project, String colorTableId, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DCTP_TABLE_ID };
        String[] whereValues = new String[]{ colorTableId };
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_GUID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_PART, false, false));
        searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields, whereFields, whereValues, false, null,
                                  false, null,
                                  new JoinData(TABLE_DA_DIALOG,
                                               new String[]{ FIELD_DCTP_PART },
                                               new String[]{ FIELD_DD_PARTNO },
                                               false, false),
                                  new JoinData(TABLE_DA_COLORTABLE_DATA,
                                               new String[]{ FIELD_DCTP_TABLE_ID, TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_SERIES_NO) },
                                               new String[]{ FIELD_DCTD_TABLE_ID, FIELD_DCTD_VALID_SERIES },
                                               false, false));
    }

    /**
     * Lädt eine Liste mit allen {@link iPartsDataColorTableToPart} für die übergebene Farbtabelle, Datum ab und
     * Original-POS Wert
     *
     * @param project
     * @param colorTableId
     * @param sdata
     * @param originalPos
     * @return
     */
    public static iPartsDataColorTableToPartList loadColorTableToPartListWithOriginalPos(EtkProject project, String colorTableId,
                                                                                         String sdata, String originalPos) {
        iPartsDataColorTableToPartList list = new iPartsDataColorTableToPartList();
        list.loadColorTableToPartWithOriginalPos(project, colorTableId, sdata, originalPos, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadColorTableToPartWithOriginalPos(EtkProject project, String colorTableId, String sdata, String originalPos,
                                                     DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_COLORTABLE_PART, new String[]{ FIELD_DCTP_TABLE_ID, FIELD_DCTP_SDATA, FIELD_DCTP_POS_SOURCE },
                      new String[]{ colorTableId, sdata, originalPos }, LoadType.COMPLETE, origin);
    }
}