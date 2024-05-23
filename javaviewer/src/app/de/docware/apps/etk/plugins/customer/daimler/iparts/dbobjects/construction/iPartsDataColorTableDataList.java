/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataColorTableData} Objekten
 */
public class iPartsDataColorTableDataList extends EtkDataObjectList<iPartsDataColorTableData> implements iPartsConst {


    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataColorTableData}s für die übergebene Baureihe und Quelle
     *
     * @param project
     * @param series
     * @param source
     * @return
     */
    public static iPartsDataColorTableDataList loadColorTableDataForSeriesAndSource(EtkProject project, iPartsSeriesId series, iPartsImportDataOrigin source) {
        iPartsDataColorTableDataList list = new iPartsDataColorTableDataList();
        list.loadColorTableDataForSeriesFromDB(project, series, source, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadColorTableDataForSeriesFromDB(EtkProject project, iPartsSeriesId series, iPartsImportDataOrigin source, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_COLORTABLE_DATA, new String[]{ FIELD_DCTD_VALID_SERIES, FIELD_DCTD_SOURCE },
                      new String[]{ series.getSeriesNumber(), source.getOrigin() }, LoadType.ONLY_IDS, origin);
    }

    @Override
    protected iPartsDataColorTableData getNewDataObject(EtkProject project) {
        return new iPartsDataColorTableData(project, null);
    }
}
