/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * DataObjectList für {@link iPartsDataProductModels}
 */
public class iPartsDataProductModelsList extends EtkDataObjectList<iPartsDataProductModels> implements iPartsConst {

    public iPartsDataProductModelsList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProductModels} für das angegebene Produkt
     *
     * @param project
     * @param productId
     * @return
     */
    public static iPartsDataProductModelsList loadDataProductModelsList(EtkProject project, iPartsProductId productId) {
        iPartsDataProductModelsList list = new iPartsDataProductModelsList();
        list.loadDataProductModelsFromDB(project, productId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProductModels} für das angegebene Baumuster
     *
     * @param project
     * @param modelId
     * @return
     */
    public static iPartsDataProductModelsList loadDataProductModelsList(EtkProject project, iPartsModelId modelId) {
        iPartsDataProductModelsList list = new iPartsDataProductModelsList();
        list.loadDataProductModelsFromDB(project, modelId, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataProductModelsList loadDataProductModelsListByLike(EtkProject project, String modelNo, String productNo) {
        iPartsDataProductModelsList list = new iPartsDataProductModelsList();
        list.loadDataProductModelsLikeFromDB(project, modelNo, productNo, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProductModels}
     *
     * @param project
     * @return
     */
    public static iPartsDataProductModelsList loadAllData(EtkProject project) {
        iPartsDataProductModelsList list = new iPartsDataProductModelsList();
        list.loadDataProductModelsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Gibt es einen Eintrag für das angegebene Baumuster?
     *
     * @param project
     * @param modelId
     * @return
     */
    public static boolean existsModel(EtkProject project, iPartsModelId modelId) {
        return loadDataProductModelsList(project, modelId).size() > 0;
    }

    /**
     * Gibt es mehr als einen Eintrag für das angegebene Baumuster?
     *
     * @param project
     * @param modelId
     * @return
     */
    public static boolean modelIsUsedMoreThanOnce(EtkProject project, iPartsModelId modelId) {
        return loadDataProductModelsList(project, modelId).size() > 1;
    }

    /**
     * Lädt alle {@link iPartsDataProductModels} aus der DB für das angegebene Produkt
     *
     * @param project
     * @param productId
     * @param origin
     */
    private void loadDataProductModelsFromDB(EtkProject project, iPartsProductId productId, DBActionOrigin origin) {
        clear(origin);

        String productNumber = productId.getProductNumber();
        searchAndFill(project, TABLE_DA_PRODUCT_MODELS, new String[]{ FIELD_DPM_PRODUCT_NO }, new String[]{ productNumber },
                      LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataProductModels} aus der DB für das angegebene Baumuster
     *
     * @param project
     * @param modelsId
     * @param origin
     */
    private void loadDataProductModelsFromDB(EtkProject project, iPartsModelId modelsId, DBActionOrigin origin) {
        clear(origin);

        String modelsNumber = modelsId.getModelNumber();
        searchAndFill(project, TABLE_DA_PRODUCT_MODELS, new String[]{ FIELD_DPM_MODEL_NO }, new String[]{ modelsNumber },
                      LoadType.COMPLETE, origin);
    }

    private void loadDataProductModelsLikeFromDB(EtkProject project, String modelNo, String productNo, DBActionOrigin origin) {
        clear(origin);

        searchWithWildCardsSortAndFill(project, new String[]{ FIELD_DPM_MODEL_NO, FIELD_DPM_PRODUCT_NO },
                                       new String[]{ modelNo, productNo }, new String[]{ FIELD_DPM_MODEL_NO, FIELD_DPM_PRODUCT_NO }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataProductModels} aus der DB
     *
     * @param project
     * @param origin
     */
    private void loadDataProductModelsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchAndFill(project, TABLE_DA_PRODUCT_MODELS, null, null,
                      LoadType.COMPLETE, origin);
    }


    @Override
    protected iPartsDataProductModels getNewDataObject(EtkProject project) {
        return new iPartsDataProductModels(project, null);
    }
}
