/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.SQLStringConvert;

/**
 * DataObjectList für {@link iPartsDataSeries}
 */
public class iPartsDataSeriesList extends EtkDataObjectList<iPartsDataSeries> implements iPartsConst {

    public iPartsDataSeriesList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataSeries}
     *
     * @param project
     * @param loadType
     * @return
     */
    public static iPartsDataSeriesList loadDataSeriesList(EtkProject project, LoadType loadType) {
        iPartsDataSeriesList list = new iPartsDataSeriesList();
        list.loadDataSeriesFromDB(project, null, null, loadType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataSeries}, die automatisch berechnet und exportiert werden sollen
     *
     * @param project
     * @return
     */
    public static iPartsDataSeriesList loadAllAutoCalcAndExportSeries(EtkProject project) {
        iPartsDataSeriesList list = new iPartsDataSeriesList();
        String[] whereFields = new String[]{ FIELD_DS_AUTO_CALCULATION };
        String[] whereValues = new String[]{ SQLStringConvert.booleanToPPString(true) };
        list.loadDataSeriesFromDB(project, whereFields, whereValues, LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataSeries} aus der DB mit den optionalen {@code whereValues}.
     *
     * @param project
     * @param whereFields
     * @param whereValues
     * @param loadType
     * @param origin
     */
    private void loadDataSeriesFromDB(EtkProject project, String[] whereFields, String[] whereValues, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_SERIES, whereFields, whereValues, loadType, origin);
    }

    @Override
    protected iPartsDataSeries getNewDataObject(EtkProject project) {
        return new iPartsDataSeries(project, null);
    }
}
