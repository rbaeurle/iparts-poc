/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataColorTableContent} Objekten
 */
public class iPartsDataColorTableContentList extends EtkDataObjectList<iPartsDataColorTableContent> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableContent}s mit der übergebenen Teileposition und Farbtabellen ID
     *
     * @param project
     * @param colorTableId Farbtabellen Id
     * @param pos
     * @return
     */
    public static iPartsDataColorTableContentList loadColorTableContentListForPartNumberAndColortableId(EtkProject project, String colorTableId, String pos) {
        iPartsDataColorTableContentList list = new iPartsDataColorTableContentList();
        list.loadColorTableContentListForPartNumberAndColortableIdFromDB(project, colorTableId, pos, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadColorTableContentListForPartNumberAndColortableIdFromDB(EtkProject project, String colorTableId, String pos, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_COLORTABLE_CONTENT, new String[]{ FIELD_DCTC_TABLE_ID, FIELD_DCTC_POS },
                      new String[]{ colorTableId, pos }, LoadType.ONLY_IDS, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableContent}s
     *
     * @param project
     * @param colorTableId Farbtabellennummer
     * @return
     */
    public static iPartsDataColorTableContentList loadColorTableContentListForColorTable(EtkProject project, iPartsColorTableDataId colorTableId) {
        iPartsDataColorTableContentList list = new iPartsDataColorTableContentList();
        list.loadColorTableContentListForColorTableFromDB(project, colorTableId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Farbtabellen-Werksdaten zur übergebenen Baureihe und Quelle.
     *
     * @param project
     * @param seriesNo
     * @param source
     * @return
     */
    public static iPartsDataColorTableContentList loadColortableContentForSeriesAndSource(EtkProject project, String seriesNo, iPartsImportDataOrigin source) {
        iPartsDataColorTableContentList list = new iPartsDataColorTableContentList();
        list.loadAllForSeriesAndOrigin(project, seriesNo, source, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllForSeriesAndOrigin(EtkProject project, String seriesNo, iPartsImportDataOrigin source, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = { FIELD_DCTC_TABLE_ID, FIELD_DCTC_SOURCE };
        String[] whereValues = { ColorTableHelper.makeWhereValueForColorTableWithSeries(new iPartsSeriesId(seriesNo)) + "*", source.getOrigin() };

        searchAndFillWithLike(project, TABLE_DA_COLORTABLE_CONTENT, null, whereFields, whereValues, LoadType.ONLY_IDS, false, DBActionOrigin.FROM_DB);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataColorTableContent}s
     *
     * @param project
     * @param colorTableId Teilenummer
     * @param origin
     */
    public void loadColorTableContentListForColorTableFromDB(EtkProject project, iPartsColorTableDataId colorTableId, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_COLORTABLE_CONTENT, new String[]{ FIELD_DCTC_TABLE_ID }, new String[]{ colorTableId.getColorTableId() },
                          new String[]{ FIELD_DCTC_POS }, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataColorTableContent getNewDataObject(EtkProject project) {
        return new iPartsDataColorTableContent(project, null);
    }
}