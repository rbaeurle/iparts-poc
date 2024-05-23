/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

/**
 * Liste von {@link iPartsDataModelProperties}.
 */
public class iPartsDataModelPropertiesList extends EtkDataObjectList<iPartsDataModelProperties> implements iPartsConst {

    public iPartsDataModelPropertiesList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und läd eine Liste aller {@link iPartsDataModelProperties} für das übergebene Baumuster
     *
     * @param project
     * @param modelNumber
     * @return
     */
    public static iPartsDataModelPropertiesList loadDataModelPropertiesList(EtkProject project, String modelNumber) {
        iPartsDataModelPropertiesList list = new iPartsDataModelPropertiesList();
        list.loadDataModelPropertiesFromDB(project, modelNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und läd eine Liste aller {@link iPartsDataModelProperties} für die übergebene Baureihe
     *
     * @param project
     * @param seriesNumber
     * @param loadType
     * @return
     */
    public static iPartsDataModelPropertiesList loadDataModelPropertiesListForSeries(EtkProject project, String seriesNumber,
                                                                                     LoadType loadType) {
        iPartsDataModelPropertiesList list = new iPartsDataModelPropertiesList();
        list.loadDataModelPropertiesFromDBForSeries(project, seriesNumber, null, loadType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelProperties} für das übergebene Baumuster, sortiert nach Datum ab.
     * Der neuste Datensatz steht dabei an erster Stelle
     *
     * @param project
     * @param modelNumber
     * @return
     */
    public static iPartsDataModelPropertiesList loadDataModelPropertiesListSorted(EtkProject project, String modelNumber) {
        iPartsDataModelPropertiesList list = new iPartsDataModelPropertiesList();
        list.loadDataModelPropertiesSortedFromDB(project, modelNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelProperties}s für die übergebene Baureihe und Quelle
     *
     * @param project
     * @param seriesNumber
     * @param source
     * @return
     */
    public static iPartsDataModelPropertiesList loadDataModelPropertiesListForSeriesAndSource(EtkProject project, String seriesNumber,
                                                                                              iPartsImportDataOrigin source, LoadType loadType) {
        iPartsDataModelPropertiesList list = new iPartsDataModelPropertiesList();
        list.loadDataModelPropertiesFromDBForSeries(project, seriesNumber, source, loadType, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataModelPropertiesList loadDataModelPropertiesListForSeriesAndDIALOG(EtkProject project, String seriesNumber,
                                                                                              LoadType loadType) {
        iPartsDataModelPropertiesList list = new iPartsDataModelPropertiesList();
        list.loadDataModelPropertiesFromDBForSeries(project, seriesNumber, iPartsImportDataOrigin.DIALOG, loadType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelProperties}s auf die sich die After-Sales Baureihe bezieht.
     * Aber nur solche mit dem angegebenen Status.
     *
     * @param project
     * @param ASModelNumber
     * @param status
     * @param loadType
     * @return
     */
    public static iPartsDataModelPropertiesList loadDataModelPropertiesForASModel(EtkProject project, String ASModelNumber,
                                                                                  iPartsDataReleaseState status, LoadType loadType) {
        iPartsDataModelPropertiesList list = new iPartsDataModelPropertiesList();
        list.loadDataModelPropertiesForASModelFromDB(project, ASModelNumber, status, loadType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Läd eine Liste aller {@link iPartsDataModelProperties} für das übergebene Baumuster
     *
     * @param project
     * @param modelNumber
     * @param origin
     */
    private void loadDataModelPropertiesFromDB(EtkProject project, String modelNumber, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DMA_MODEL_NO };
        String[] whereValues = new String[]{ modelNumber };
        searchAndFill(project, TABLE_DA_MODEL_PROPERTIES, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    private void loadDataModelPropertiesSortedFromDB(EtkProject project, String modelNumber, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DMA_MODEL_NO };
        String[] whereValues = new String[]{ modelNumber };
        String[] sortFields = new String[]{ FIELD_DMA_DATA };
        searchSortAndFill(project, TABLE_DA_MODEL_PROPERTIES, whereFields, whereValues, sortFields, LoadType.COMPLETE, true, origin);
    }

    /**
     * Läd eine Liste aller {@link iPartsDataModelProperties} für die übergebene Baureihe
     *
     * @param project
     * @param seriesNumber
     * @param source
     * @param loadType
     * @param origin
     */
    private void loadDataModelPropertiesFromDBForSeries(EtkProject project, String seriesNumber, iPartsImportDataOrigin source,
                                                        LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DMA_SERIES_NO };
        String[] whereValues = new String[]{ seriesNumber };
        if (source != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DMA_SOURCE });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ source.getOrigin() });
        }
        String[] sortFields = new String[]{ FIELD_DMA_MODEL_NO };
        searchSortAndFill(project, TABLE_DA_MODEL_PROPERTIES, whereFields, whereValues, sortFields, loadType, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModelProperties}s auf die sich die After-Sales Baureihe bezieht.
     * Aber nur solche mit dem angegebenen Status.
     *
     * @param project
     * @param modelNumber
     * @param status
     * @param loadType
     * @param origin
     */
    private void loadDataModelPropertiesForASModelFromDB(EtkProject project, String modelNumber, iPartsDataReleaseState status,
                                                         LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DMA_MODEL_NO,
                                             FIELD_DMA_STATUS };
        String[] whereValues = new String[]{ modelNumber + "?",
                                             status.getDbValue() };
        searchAndFillWithLike(project, TABLE_DA_MODEL_PROPERTIES, null, whereFields, whereValues, loadType, false, origin);
    }

    @Override
    protected iPartsDataModelProperties getNewDataObject(EtkProject project) {
        return new iPartsDataModelProperties(project, null);
    }
}
