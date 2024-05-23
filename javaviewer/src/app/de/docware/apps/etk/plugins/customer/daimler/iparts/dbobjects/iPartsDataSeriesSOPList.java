/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataSeriesSOP}.
 */
public class iPartsDataSeriesSOPList extends EtkDataObjectList<iPartsDataSeriesSOP> implements iPartsConst {

    public iPartsDataSeriesSOPList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataSeriesSOPList getAllSeriesSOP(EtkProject project, iPartsSeriesId seriesId) {
        iPartsDataSeriesSOPList list = new iPartsDataSeriesSOPList();
        list.loadAllSeriesFromDB(project, seriesId, DBActionOrigin.FROM_DB);
        return list;
    }


    private void loadAllSeriesFromDB(EtkProject project, iPartsSeriesId seriesId, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_SERIES_SOP, new String[]{ FIELD_DSP_SERIES_NO }, new String[]{ seriesId.getSeriesNumber() },
                          new String[]{ FIELD_DSP_SERIES_NO, FIELD_DSP_AA }, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataSeriesSOP getNewDataObject(EtkProject project) {
        return new iPartsDataSeriesSOP(project, null);
    }
}
