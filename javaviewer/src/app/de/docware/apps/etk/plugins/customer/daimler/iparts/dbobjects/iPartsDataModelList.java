/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste von {@link iPartsDataModel}.
 */
public class iPartsDataModelList extends EtkDataObjectList<iPartsDataModel> implements iPartsConst {

    public iPartsDataModelList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt die komplette TABLE_DA_MODEL; hier nur IDs
     *
     * @param project
     * @param loadType
     * @return
     */
    public static iPartsDataModelList loadAllDataModelList(EtkProject project, LoadType loadType) {
        iPartsDataModelList list = new iPartsDataModelList();
        list.loadAllDataModelsFromDB(project, loadType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModel} für die übergebene Baureihe
     *
     * @param project
     * @param seriesNumber
     * @param loadType
     * @return
     */
    public static iPartsDataModelList loadDataModelList(EtkProject project, String seriesNumber, LoadType loadType) {
        iPartsDataModelList list = new iPartsDataModelList();
        list.loadDataModelsFromDB(project, seriesNumber, loadType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModel} für die übergebene (partielle) Baumusternummer, die auch
     * Wildcards enthalten kann.
     *
     * @param project
     * @param partialModelNumberWithWildCard
     * @return
     */
    public static iPartsDataModelList loadDataModelListForModelNumberWithWildCards(EtkProject project, String partialModelNumberWithWildCard) {
        iPartsDataModelList list = new iPartsDataModelList();
        list.loadForPartialModelNumberFromDB(project, partialModelNumberWithWildCard, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModel} für die übergebene 6-stellige Baumusternummer ohne
     * Sachnummernkennbuchstabe (C oder D)
     *
     * @param project
     * @param modelNumberWithoutPrefix
     * @return
     */
    public static iPartsDataModelList loadForModelNumberWithoutPrefix(EtkProject project, String modelNumberWithoutPrefix) {
        iPartsDataModelList list = new iPartsDataModelList();
        list.loadForPartialModelNumberFromDB(project, "?" + modelNumberWithoutPrefix, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModel} für die übergebene 4-stellige Typkennzahl
     *
     * @param project
     * @param modelTypeNumber
     * @return
     */
    public static iPartsDataModelList loadForModelTypeNumber(EtkProject project, String modelTypeNumber) {
        iPartsDataModelList list = new iPartsDataModelList();
        list.loadForPartialModelNumberFromDB(project, modelTypeNumber + "*", DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataModelList loadModelsForProductWithModelSource(EtkProject project, String productNumber, iPartsImportDataOrigin source) {
        iPartsDataModelList list = new iPartsDataModelList();
        list.loadModelsForProductWithModelSourceFromDB(project, productNumber, source, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataModelList loadAggregateModelsForModelWithModelSource(EtkProject project, String modelNumber, iPartsImportDataOrigin source) {
        iPartsDataModelList list = new iPartsDataModelList();
        list.loadAggregateModelsForModelWithModelSourceFromDB(project, modelNumber, source, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt die komplette TABLE_DA_MODEL; hier nur IDs
     *
     * @param project
     * @param loadType
     * @param origin
     */
    private void loadAllDataModelsFromDB(EtkProject project, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_MODEL, null, null, loadType, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModel} für die übergebene Baureihe
     *
     * @param project
     * @param seriesNumber
     * @param loadType
     * @param origin
     */
    private void loadDataModelsFromDB(EtkProject project, String seriesNumber, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DM_SERIES_NO };
        String[] whereValues = new String[]{ seriesNumber };
        searchAndFill(project, TABLE_DA_MODEL, whereFields, whereValues, loadType, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModel} für die übergebene teilweise Baumusternummer inkl. Wildcard
     *
     * @param project
     * @param partialModelNumberWithWildCard
     * @param origin
     */
    private void loadForPartialModelNumberFromDB(EtkProject project, String partialModelNumberWithWildCard, DBActionOrigin origin) {
        clear(origin);

        // Aktuell mittels SQL Select Like: erste Stelle von DM_MODEL_NO ist der (beliebige) Sachnummernkennbuchstabe
        String[] whereFields = new String[]{ FIELD_DM_MODEL_NO };
        String[] whereValues = new String[]{ partialModelNumberWithWildCard };

        DBDataObjectAttributesList dataModels = project.getDbLayer().getAttributesList(TABLE_DA_MODEL, null,
                                                                                       whereFields, whereValues,
                                                                                       ExtendedDataTypeLoadType.MARK,
                                                                                       true, false);

        for (DBDataObjectAttributes attributes : dataModels) {
            iPartsDataModel dataModel = new iPartsDataModel(project, null);
            dataModel.setAttributes(attributes, DBActionOrigin.FROM_DB);
            add(dataModel, origin);
        }
    }

    private void loadModelsForProductWithModelSourceFromDB(EtkProject project, String productNumber, iPartsImportDataOrigin source, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO) };
        String[] whereValues = new String[]{ productNumber };

        if (source != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ TableAndFieldName.make(TABLE_DA_MODEL, FIELD_DM_SOURCE) });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ source.getOrigin() });
        }

        searchSortAndFillWithJoin(project, null, null, new String[]{ FIELD_DM_MODEL_NO }, TABLE_DA_PRODUCT_MODELS,
                                  new String[]{ FIELD_DPM_MODEL_NO }, true, false,
                                  whereFields, whereValues, false,
                                  new String[]{ FIELD_DM_MODEL_NO }, false);
    }

    private void loadAggregateModelsForModelWithModelSourceFromDB(EtkProject project, String modelNumber, iPartsImportDataOrigin source, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_MODELS_AGGS, FIELD_DMA_MODEL_NO), TableAndFieldName.make(TABLE_DA_MODEL, FIELD_DM_SOURCE) };
        String[] whereValues = new String[]{ modelNumber, source.getOrigin() };

        searchSortAndFillWithJoin(project, null, null, new String[]{ FIELD_DM_MODEL_NO }, TABLE_DA_MODELS_AGGS,
                                  new String[]{ FIELD_DMA_AGGREGATE_NO }, true, false,
                                  whereFields,
                                  whereValues, false,
                                  new String[]{ FIELD_DM_MODEL_NO }, false);
    }

    @Override
    protected iPartsDataModel getNewDataObject(EtkProject project) {
        return new iPartsDataModel(project, null);
    }
}
