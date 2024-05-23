/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsSpecType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Motoröle: Zuordnung Motorbaumuster zu Nachfüllmenge aus der Tabelle DA_MODEL_OIL_QUANTITY im iParts-Plug-in.
 * Liste von {@link iPartsDataModelOilQuantity}.
 */
public class iPartsDataModelOilQuantityList extends EtkDataObjectList<iPartsDataModelOilQuantity> implements iPartsConst {

    public iPartsDataModelOilQuantityList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DMOQ_MODEL_NO, FIELD_DMOQ_CODE_VALIDITY, FIELD_DMOQ_SPEC_TYPE, FIELD_DMOQ_IDENT_TO, FIELD_DMOQ_IDENT_FROM };
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelOilQuantity} aus der Datenbank
     *
     * @param project
     * @return
     */
    public static iPartsDataModelOilQuantityList loadAllEntriesFromDB(EtkProject project) {
        iPartsDataModelOilQuantityList list = new iPartsDataModelOilQuantityList();
        list.loadAllEntriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt die am besten passendsten {@link iPartsDataModelOilQuantity}-Datensätze aus der Datenbank für das übergebene Baumuster
     * auch unter Berücksichtung von Datensätzen mit kürzerem Baumuster-Präfix bis hin zur Typkennzahl des Baumusters.
     *
     * @param project
     * @param modelId
     * @param specType
     * @return
     */
    public static iPartsDataModelOilQuantityList loadDataModelOilQuantityForModel(EtkProject project, iPartsModelId modelId, iPartsSpecType specType) {
        iPartsDataModelOilQuantityList list = loadDataModelOilQuantityForModelTypeNumber(project, modelId.getModelTypeNumber(), specType);

        final iPartsDataModelOilQuantityList matchingModelOilQuantityList = new iPartsDataModelOilQuantityList(); // Zum Baumuster passende Datensätze
        if (modelId.isModelNumberValid(true)) {
            String modelPrefix = modelId.getModelTypeNumber().substring(0, 1);
            iPartsDataModelOilQuantityList listModelPrefix = loadDataModelOilQuantityForModelPrefix(project, modelPrefix, specType);
            if (!listModelPrefix.isEmpty()) {
                list.addAll(listModelPrefix, DBActionOrigin.FROM_DB);
            }
        }
        list.forEach(modelOilQuantity -> {
            // Baumuster aus der Gültigkeit muss ein Präfix von dem gewünschten Baumuster sein (oder damit identisch)
            if (!modelId.getModelNumber().startsWith(modelOilQuantity.getAsId().getModelNo())) {
                return;
            }
            matchingModelOilQuantityList.add(modelOilQuantity, DBActionOrigin.FROM_DB);
        });

        return matchingModelOilQuantityList;
    }

    /**
     * Lädt eine Liste von {@link iPartsDataModelOilQuantity} aus der Datenbank passend zur Baumuster-Typnummer-Prefix
     * (keine like-Abfrage)
     *
     * @param project
     * @param modelPrefix
     * @param specType
     * @return
     */
    public static iPartsDataModelOilQuantityList loadDataModelOilQuantityForModelPrefix(EtkProject project, String modelPrefix, iPartsSpecType specType) {
        iPartsDataModelOilQuantityList list = new iPartsDataModelOilQuantityList();
        list.loadDataModelOilForModelPrefixFromDB(project, modelPrefix, specType, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste von {@link iPartsDataModelOilQuantity} aus der Datenbank passend zur Baumuster-Typnummer mit einer
     * like-Abfrage.
     *
     * @param project
     * @param modelTypeNumber
     * @param specType
     * @return
     */
    public static iPartsDataModelOilQuantityList loadDataModelOilQuantityForModelTypeNumber(EtkProject project, String modelTypeNumber, iPartsSpecType specType) {
        iPartsDataModelOilQuantityList list = new iPartsDataModelOilQuantityList();
        list.loadDataModelOilForModelTypeNumberFromDB(project, modelTypeNumber, specType, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelOilQuantity} aus der Datenbank
     *
     * @param project
     * @param origin
     * @return
     */
    private void loadAllEntriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_MODEL_OIL_QUANTITY, null, null, getSortFields(), LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste von {@link iPartsDataModelOilQuantity} aus der Datenbank passend zur Baumuster-Typnummer mit einer
     * like-Abfrage.
     *
     * @param project
     * @param modelTypeNumber
     * @param specType
     * @param type
     * @param origin
     * @return
     */
    private void loadDataModelOilForModelTypeNumberFromDB(EtkProject project, String modelTypeNumber, iPartsSpecType specType, LoadType type, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DMOQ_MODEL_NO, FIELD_DMOQ_SPEC_TYPE };
        String[] whereValues = new String[]{ modelTypeNumber + "*", specType.getDbValue() };

        searchSortAndFillWithLike(project, TABLE_DA_MODEL_OIL_QUANTITY, null, whereFields, whereValues, whereFields, false, type,
                                  false, origin);
    }

    /**
     * Lädt eine Liste von {@link iPartsDataModelOilQuantity} aus der Datenbank passend zu einem Baumuster-Prefix
     *
     * @param project
     * @param modelPrefix
     * @param specType
     * @param type
     * @param origin
     * @return
     */
    private void loadDataModelOilForModelPrefixFromDB(EtkProject project, String modelPrefix, iPartsSpecType specType, LoadType type, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DMOQ_MODEL_NO, FIELD_DMOQ_SPEC_TYPE };
        String[] whereValues = new String[]{ modelPrefix, specType.getDbValue() };

        searchSortAndFill(project, TABLE_DA_MODEL_OIL_QUANTITY, whereFields, whereValues, null, type, origin);

    }

    /**
     * Neues {@link EtkDataObject} erzeugen für den Aufbau der {@link EtkDataObjectList}
     *
     * @param project
     * @return
     */
    protected iPartsDataModelOilQuantity getNewDataObject(EtkProject project) {
        return new iPartsDataModelOilQuantity(project, null);
    }

}