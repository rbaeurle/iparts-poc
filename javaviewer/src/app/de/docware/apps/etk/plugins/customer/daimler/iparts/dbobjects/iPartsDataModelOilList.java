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
import de.docware.framework.utils.EtkMultiSprache;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Zuordnung Motorbaumuster zu Spezifikation aus der Tabelle DA_MODEL_OIL im iParts-Plug-in.
 * Liste von {@link iPartsDataModelOil}.
 */
public class iPartsDataModelOilList extends EtkDataObjectList<iPartsDataModelOil> implements iPartsConst {

    public iPartsDataModelOilList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Zentrale Methode, die die Sortierfelder zurückliefert.
     *
     * @return Die Sortierfelder
     */
    private String[] getSortFields() {
        return new String[]{ FIELD_DMO_MODEL_NO, FIELD_DMO_SPEC_VALIDITY, FIELD_DMO_SPEC_TYPE };
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelOil} aus der Datenbank
     *
     * @param project
     * @return
     */
    public static iPartsDataModelOilList loadAllEntriesFromDB(EtkProject project) {
        iPartsDataModelOilList list = new iPartsDataModelOilList();
        list.loadAllEntriesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt die am besten passendsten {@link iPartsDataModelOil}-Datensätze aus der Datenbank für das übergebene Baumuster
     * und den übergebenen Spezifikationstypen auch unter Berücksichtung von Datensätzen mit kürzerem Baumuster-Präfix bis hin zur Typkennzahl des Baumusters.
     *
     * @param project
     * @param modelId
     * @param specType
     * @return
     */
    public static iPartsDataModelOilList loadDataModelOilForModelAndSpecType(EtkProject project, iPartsModelId modelId, iPartsSpecType specType) {
        iPartsDataModelOilList dataModelOilListTotalForSpecType = new iPartsDataModelOilList();
        if (modelId.isModelNumberValid(true)) {
            String modelPrefix = modelId.getModelTypeNumber().substring(0, 1);
            // "C" oder "D"
            dataModelOilListTotalForSpecType.addAll(loadDataModelOilForModelPrefixAndSpecType(project, modelPrefix, specType).getAsList(), DBActionOrigin.FROM_DB);
            // Typenkennzahl z.B. "C205"
            dataModelOilListTotalForSpecType.addAll(loadDataModelOilForModelTypeNumberAndSpecType(project, modelId.getModelTypeNumber(), specType).getAsList(), DBActionOrigin.FROM_DB);

            // Für jeden Spezifikationstypen den besten Datensatz ermitteln
            Map<String, iPartsDataModelOil> bestModelOilMapTotal = new TreeMap<>();
            Map<String, iPartsDataModelOil> bestModelOilMapSpecTypeFiltered = getBestFittingSpec(dataModelOilListTotalForSpecType, modelId);
            if (!bestModelOilMapSpecTypeFiltered.isEmpty()) {
                bestModelOilMapTotal.putAll(bestModelOilMapSpecTypeFiltered);
            }

            dataModelOilListTotalForSpecType = new iPartsDataModelOilList();
            dataModelOilListTotalForSpecType.addAll(bestModelOilMapTotal.values(), DBActionOrigin.FROM_DB);
        }

        return dataModelOilListTotalForSpecType;
    }

    public static Map<String, iPartsDataModelOil> getBestFittingSpec(iPartsDataModelOilList dataModelOilList, iPartsModelId modelId) {
        Map<String, iPartsDataModelOil> bestModelOilMap = new TreeMap<>(); // Spec-Gültigkeit -> bester Datensatz
        Map<String, Integer> bestModelOilModelNoLengthMap = new HashMap<>(); // Spec-Gültigkeit -> Baumuster-Präfix-Länge
        dataModelOilList.forEach(modelOil -> {
            // Baumuster aus der Gültigkeit muss ein Präfix von dem gewünschten Baumuster sein (oder damit identisch)
            if (!modelId.getModelNumber().startsWith(modelOil.getAsId().getModelNo())) {
                return;
            }

            // Datensätze mit dem längsten Baumusterpräfix pro Spec-Gültigkeit in bestModelOilMap merken
            String specValidity = modelOil.getAsId().getSpecValidity();
            int modelOilLModelNoLength = modelOil.getAsId().getModelNo().length();
            Integer bestModelOilModelNoLength = bestModelOilModelNoLengthMap.get(specValidity);
            if (bestModelOilModelNoLength == null) {
                bestModelOilModelNoLength = 0;
            }
            if (modelOilLModelNoLength >= bestModelOilModelNoLength) {
                bestModelOilMap.put(specValidity, modelOil);
                bestModelOilModelNoLengthMap.put(specValidity, modelOilLModelNoLength);
            }
        });
        return bestModelOilMap;
    }

    /**
     * Lädt eine Liste von {@link iPartsDataModelOil} aus der Datenbank passend zur Baumuster-Typnummer mit einer like Abfrage
     *
     * @param project
     * @param modelTypeNumber
     * @param specType
     * @return
     */
    public static iPartsDataModelOilList loadDataModelOilForModelTypeNumberAndSpecType(EtkProject project, String modelTypeNumber, iPartsSpecType specType) {
        iPartsDataModelOilList list = new iPartsDataModelOilList();
        list.loadDataModelOilForModelTypeNumberAndSpecTypeFromDB(project, modelTypeNumber, specType, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        addMultiTextDataToModelOilList(list);
        return list;
    }

    /**
     * Lädt eine Liste von {@link iPartsDataModelOil} aus der Datenbank passend zu einem übergebenen Baumuster-Präfix (KEIN LIKE!)
     * und einem übergebenen Spezifikationstypen
     *
     * @param project
     * @param modelPrefix
     * @param specType
     * @return
     */
    public static iPartsDataModelOilList loadDataModelOilForModelPrefixAndSpecType(EtkProject project, String modelPrefix, iPartsSpecType specType) {
        iPartsDataModelOilList list = new iPartsDataModelOilList();
        list.loadDataModelOilForModelPrefixAndSpecTypeFromDB(project, modelPrefix, specType, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        addMultiTextDataToModelOilList(list);
        return list;
    }

    /**
     * Vervollständigt eine übergebene {@link iPartsDataModelOilList} mit MultiText-Informationen
     *
     * @param list
     */
    private static void addMultiTextDataToModelOilList(iPartsDataModelOilList list) {
        Map<String, EtkMultiSprache> multiMap = new HashMap<>();
        list.forEach((data) -> {
            EtkMultiSprache multi = multiMap.get(data.getTextId());
            if (multi == null) {
                multi = data.getText();
                multiMap.put(data.getTextId(), multi);
            } else {
                data.setMultiText(multi);
            }
        });
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModelOil} aus der Datenbank
     *
     * @param project
     * @param origin
     * @return
     */
    private void loadAllEntriesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchSortAndFill(project, TABLE_DA_MODEL_OIL, null, null, getSortFields(),
                          LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste von {@link iPartsDataModelOil} aus der Datenbank passend zur Baumuster-Typnummer
     * und einem übergebenen Spezifikationstypen mit einer like Abfrage
     *
     * @param project
     * @param modelTypeNumber
     * @param specType
     * @param type
     * @param origin
     * @return
     */
    private void loadDataModelOilForModelTypeNumberAndSpecTypeFromDB(EtkProject project, String modelTypeNumber, iPartsSpecType specType, LoadType type, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DMO_MODEL_NO, FIELD_DMO_SPEC_TYPE };
        String[] whereValues = new String[]{ modelTypeNumber + "*", specType.getDbValue() };

        searchSortAndFillWithLike(project, TABLE_DA_MODEL_OIL, null, whereFields, whereValues, whereFields, false, type,
                                  false, origin);
    }

    /**
     * Lädt eine Liste von {@link iPartsDataModelOil} aus der Datenbank passend zu einem übergebenen Baumuster-Präfix
     * und einem übergebenen Spezifikationstypen
     *
     * @param project
     * @param modelPrefix
     * @param specType
     * @param type
     * @param origin
     */
    private void loadDataModelOilForModelPrefixAndSpecTypeFromDB(EtkProject project, String modelPrefix, iPartsSpecType specType, LoadType type, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DMO_MODEL_NO, FIELD_DMO_SPEC_TYPE };
        String[] whereValues = new String[]{ modelPrefix, specType.getDbValue() };

        searchSortAndFill(project, TABLE_DA_MODEL_OIL, whereFields, whereValues, null, type, origin);
    }

    /**
     * Neues {@link EtkDataObject} erzeugen für den Aufbau der {@link EtkDataObjectList}
     *
     * @param project
     * @return
     */
    protected iPartsDataModelOil getNewDataObject(EtkProject project) {
        return new iPartsDataModelOil(project, null);
    }
}
