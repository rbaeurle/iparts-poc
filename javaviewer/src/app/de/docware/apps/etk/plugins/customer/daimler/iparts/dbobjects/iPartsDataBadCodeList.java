/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataBadCode}.
 */
public class iPartsDataBadCodeList extends EtkDataObjectList<iPartsDataBadCode> implements iPartsConst {

    public iPartsDataBadCodeList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataBadCode}s für die übergebene Baureihe
     * sortiert nach FIELD_DSP_AA
     *
     * @param project
     * @return
     */
    public static iPartsDataBadCodeList loadAllBadCodesForSeries(EtkProject project, String seriesNumber) {
        iPartsDataBadCodeList list = new iPartsDataBadCodeList();
        list.loadAllBadCodeDataForSeries(project, seriesNumber, DBActionOrigin.FROM_DB);
        // FIELD_DBC_EXPIRY_DATE Attribut setzen
        for (iPartsDataBadCode badCode : list) {
            badCode.addExpiredField();
        }
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste von {@link iPartsDataBadCode}s, die nicht abgelaufen sind für die übergebene Baureihe
     * sortiert nach FIELD_DSP_AA
     *
     * @param project
     * @param seriesNumber
     * @return
     */
    public static iPartsDataBadCodeList loadAllBadCodesForSeriesNotExpired(EtkProject project, String seriesNumber) {
        iPartsDataBadCodeList list = loadAllBadCodesForSeries(project, seriesNumber);
        iPartsDataBadCodeList result = new iPartsDataBadCodeList();
        // FIELD_DBC_EXPIRY_DATE Attribut setzen
        for (iPartsDataBadCode badCode : list) {
            if (!badCode.isExpired()) {
                result.add(badCode, DBActionOrigin.FROM_DB);
            }
        }
        return result;
    }

    /**
     * lädt alle AA (Ausführungsarten) zu einer SeriesId
     * sortiert nach FIELD_DSP_AA
     *
     * @param project
     * @param seriesNumber
     * @param origin
     */
    private void loadAllBadCodeDataForSeries(EtkProject project, String seriesNumber, DBActionOrigin origin) {
        clear(origin);
        searchSortAndFill(project, TABLE_DA_BAD_CODE, new String[]{ FIELD_DBC_SERIES_NO }, new String[]{ seriesNumber },
                          new String[]{ FIELD_DBC_CODE_ID, FIELD_DBC_AA }, LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataBadCode getNewDataObject(EtkProject project) {
        return new iPartsDataBadCode(project, null);
    }
}
