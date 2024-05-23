/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * DataObjectList für {@link iPartsDataModelsAggs}
 */
public class iPartsDataModelsAggsList extends EtkDataObjectList<iPartsDataModelsAggs> implements iPartsConst {

    public iPartsDataModelsAggsList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelsAggs} für das angegebene Fahrzeugbaumuster
     *
     * @param project
     * @param modelNumber
     * @return
     */
    public static iPartsDataModelsAggsList loadDataModelsAggsListForModel(EtkProject project, String modelNumber) {
        iPartsDataModelsAggsList list = new iPartsDataModelsAggsList();
        list.loadDataModelsAggsListForModelFromDB(project, modelNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelsAggs} für das angegebene Fahrzeugbaumuster
     *
     * @param project
     * @param modelNumber
     * @return
     */
    public static iPartsDataModelsAggsList loadDataModelsAggsListSortedForModel(EtkProject project, String modelNumber, boolean descending) {
        iPartsDataModelsAggsList list = new iPartsDataModelsAggsList();
        list.loadDataModelsAggsListSortedForModelFromDB(project, modelNumber, descending, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelsAggs} für das angegebene Fahrzeugbaumuster
     *
     * @param project
     * @param modelNumber
     * @return
     */
    public static iPartsDataModelsAggsList loadDataModelsAggsListWithSourceForModel(EtkProject project, String modelNumber, iPartsImportDataOrigin source) {
        iPartsDataModelsAggsList list = new iPartsDataModelsAggsList();
        list.loadDataModelsAggsListWithSourceForModelFromDB(project, modelNumber, source, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelsAggs} für das angegebene Aggregatebaumuster
     *
     * @param project
     * @param aggregateModelNumber
     * @return
     */
    public static iPartsDataModelsAggsList loadDataModelsAggsListForAggregateModel(EtkProject project, String aggregateModelNumber) {
        iPartsDataModelsAggsList list = new iPartsDataModelsAggsList();
        list.loadDataModelsAggsListForAggregateModelFromDB(project, aggregateModelNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataModelsAggs} aus der DB für das angegebene Fahrzeugbaumuster
     *
     * @param project
     * @param modelNumber
     * @param origin
     */
    private void loadDataModelsAggsListForModelFromDB(EtkProject project, String modelNumber, DBActionOrigin origin) {
        clear(origin);

        searchAndFill(project, TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_MODEL_NO }, new String[]{ modelNumber },
                      LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataModelsAggs} aus der DB für das angegebene Fahrzeugbaumuster
     *
     * @param project
     * @param modelNumber
     * @param origin
     */
    private void loadDataModelsAggsListSortedForModelFromDB(EtkProject project, String modelNumber, boolean descending, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_MODEL_NO }, new String[]{ modelNumber }, new String[]{ FIELD_DMA_AGGREGATE_NO },
                          LoadType.COMPLETE, descending, origin);
    }

    /**
     * Lädt alle {@link iPartsDataModelsAggs} aus der DB für das angegebene Fahrzeugbaumuster
     *
     * @param project
     * @param modelNumber
     * @param source
     * @param origin
     */
    private void loadDataModelsAggsListWithSourceForModelFromDB(EtkProject project, String modelNumber, iPartsImportDataOrigin source, DBActionOrigin origin) {
        clear(origin);

        searchAndFill(project, TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_MODEL_NO, FIELD_DMA_SOURCE }, new String[]{ modelNumber, source.getOrigin() },
                      LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataModelsAggs} aus der DB für das angegebene Aggregatebaumuster
     *
     * @param project
     * @param aggregateModelNumber
     * @param origin
     */
    private void loadDataModelsAggsListForAggregateModelFromDB(EtkProject project, String aggregateModelNumber, DBActionOrigin origin) {
        clear(origin);

        searchAndFill(project, TABLE_DA_MODELS_AGGS, new String[]{ FIELD_DMA_AGGREGATE_NO }, new String[]{ aggregateModelNumber },
                      LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataModelsAggs getNewDataObject(EtkProject project) {
        return new iPartsDataModelsAggs(project, null);
    }
}
