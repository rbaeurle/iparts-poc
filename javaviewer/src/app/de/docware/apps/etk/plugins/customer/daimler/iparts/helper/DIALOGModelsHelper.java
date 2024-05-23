/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hilfsklasse für die Übernahme von DIALOG-Baumustern aus der Konstruktion in den After-Sales und Synchronisierung der
 * entsprechenden Daten.
 */
public class DIALOGModelsHelper implements iPartsConst {

    private static HashMap<String, String> modelPropertiesMapping;
    private static HashMap<String, String> modelPropertiesMappingDIALOG;
    private static HashMap<String, String> modelPropertiesForcedMappingDIALOG;
    private static HashMap<String, String> modelDataMapping;
    private static HashMap<String, String> modelDataMappingDIALOG;

    static {
        // Mapping DA_MODEL_PROPERTIES (Quelle X2E) zu DA_MODEL
        modelPropertiesMapping = new HashMap<>();
        modelPropertiesMapping.put(FIELD_DMA_SERIES_NO, FIELD_DM_SERIES_NO);
        modelPropertiesMapping.put(FIELD_DMA_PRODUCT_GRP, FIELD_DM_PRODUCT_GRP);
        modelPropertiesMapping.put(FIELD_DMA_AA, FIELD_DM_AA);
        modelPropertiesMapping.put(FIELD_DMA_CODE, FIELD_DM_CODE);
        modelPropertiesMapping.put(FIELD_DMA_MODEL_NO, FIELD_DM_CONST_MODEL_NO);

        // Mapping DA_MODEL_PROPERTIES (Quelle X2E) zu DA_MODEL (nur für Datenherkunft DM_SOURCE = DIALOG)
        // Diese Felder halten sich an die Vorgabe: overwriteExisting == (true/false)
        modelPropertiesMappingDIALOG = new HashMap<>();
//        modelPropertiesMappingDIALOG.put(FIELD_DMA_DATA, FIELD_DM_DATA);

        // Mapping DA_MODEL_PROPERTIES (Quelle X2E) zu DA_MODEL (nur für Datenherkunft DM_SOURCE = DIALOG)
        // DAIMLER-7913, Diese Felder werden zwingend überschrieben.
        modelPropertiesForcedMappingDIALOG = new HashMap<>();
        modelPropertiesForcedMappingDIALOG.put(FIELD_DMA_DATA, FIELD_DM_DATA);
        modelPropertiesForcedMappingDIALOG.put(FIELD_DMA_DATB, FIELD_DM_DATB);

        // Mapping DA_MODEL_DATA (Quelle BMS) zu DA_MODEL
        modelDataMapping = new HashMap<>();
        modelDataMapping.put(FIELD_DMD_DEVELOPMENT_TITLE, FIELD_DM_DEVELOPMENT_TITLE);

        // Mapping DA_MODEL_DATA (Quelle BMS) zu DA_MODEL (nur für Datenherkunft DM_SOURCE = DIALOG)
        modelDataMappingDIALOG = new HashMap<>();
        modelDataMappingDIALOG.put(FIELD_DMD_SALES_TITLE, FIELD_DM_SALES_TITLE);
    }

    /**
     * Liefert für eine 8-stellige Baumuster-ID aus der Konstruktion eine 7-stellige Baumuster-ID für den After-Sales zurück.
     *
     * @param constructionModelId
     * @return
     */
    public static iPartsModelId getAfterSalesModelId(iPartsModelDataId constructionModelId) {
        String constructionModelNumber = constructionModelId.getModelNumber();
        if (constructionModelNumber.length() >= 7) {
            constructionModelNumber = constructionModelNumber.substring(0, 7);
        }
        return new iPartsModelId(constructionModelNumber);
    }

    /**
     * Liefert für eine 8-stellige Baumuster-ID aus der Konstruktion eine 7-stellige Baumusternummer für den After-Sales zurück.
     *
     * @param constructionModelId
     * @return
     */
    public static String getAfterSalesModelNumber(iPartsModelPropertiesId constructionModelId) {
        String constructionModelNumber = constructionModelId.getModelNumber();
        if (constructionModelNumber.length() >= 7) {
            constructionModelNumber = constructionModelNumber.substring(0, 7);
        }
        return constructionModelNumber;
    }

    /**
     * Synchronisiert die Werte des zu übernehmenden 8-stelligen Baumusters aus der Konstruktion (DA_MODEL_PROPERTIES) in die
     * After-Sales Tabelle (DA_MODEL) mit 7 Stellen unter Berücksichtigung aller anderen dazugehörigen AS-relevanten 8-stelligen
     * Baumuster aus der Konstruktion.
     *
     * @param constructionModelId
     * @param removeModelAndProductsFromCache Flag, ob das neue, veränderte oder gelöschte After-Sales-Baumuster und alle
     *                                        Produkte, die über die Baureihe indirekt dieses After-Sales-Baumuster verwenden,
     *                                        aus dem Cache gelöscht werden sollen
     * @param isDIALOGImport                  Flag, ob es sich um einen DIALOG-Import handelt.
     * @param isDIALOGDeltaImport             Flag, ob es sich im Fall von DIALOG um eine Delta-Ladung handelt.
     *                                        Außerdem werden keine neuen AS-Baumuster angelegt, sondern nur bereits vorhandene synchronisiert.
     * @param project
     * @return {@code true} wenn AS Baumuster übernommen oder synchronisiert wurde
     */
    public static boolean synchronizeConstructionAndASModels(iPartsModelDataId constructionModelId, boolean removeModelAndProductsFromCache,
                                                             boolean isDIALOGImport, boolean isDIALOGDeltaImport, EtkProject project) {
        iPartsModelId afterSalesModelId = getAfterSalesModelId(constructionModelId);

        // AS Baumuster in DB suchen
        iPartsDataModel afterSalesModel = new iPartsDataModel(project, afterSalesModelId);
        boolean existInDB = afterSalesModel.existsInDB();

        iPartsImportDataOrigin ASModelSource = iPartsImportDataOrigin.DIALOG;
        boolean isManualChange = false;
        if (existInDB) {
            if (isDIALOGDeltaImport) {
                // DAIMLER-8064: Falls es ein DIALOG-Delta-Import ist und das AS-BM bereits exisitert, wird keine Synchronisierung
                // durchgeführt, da der Autor in diesem Fall bei Bedarf den neuesten Stand zum Produkt manuell referenzieren muss.
                return false;
            }
            ASModelSource = iPartsImportDataOrigin.getTypeFromCode(afterSalesModel.getFieldValue(FIELD_DM_SOURCE));
            isManualChange = afterSalesModel.getFieldValueAsBoolean(FIELD_DM_MANUAL_CHANGE);  // wurde das Baumuster über iParts editiert?
        }

        // ein über iParts editiertes Baumuster darf von keinem Importer mehr angetastet werden
        if (isManualChange) {
            return false;
        }

        // Konstruktion-Baumuster aus DB bestimmen, die ähnlich zu diesem AS-Baumuster sind. Aus diesen wird das
        // eine einzige, für AS-relevante, Konstruktions-Baumuster bestimmt.
        // Die 8. Stelle der Konstruktions-Baumusternummer kann beliebig sein
        iPartsDataModelPropertiesList releasedModelPropertiesForASModel;
        releasedModelPropertiesForASModel = iPartsDataModelPropertiesList.loadDataModelPropertiesForASModel(project,
                                                                                                            afterSalesModelId.getModelNumber(),
                                                                                                            iPartsDataReleaseState.RELEASED,
                                                                                                            DBDataObjectList.LoadType.COMPLETE);
        iPartsDataModelPropertiesList similarConstructionModelList = new iPartsDataModelPropertiesList();
        if (isDIALOGImport && existInDB) {
            // Bei der DIALOG Urladung wird das, für AS relevante, Konstruktions-Baumuster im Folgenden bestimmt.
            // In Frage kommen nur solche Konstruktions-Baumuster die die gleiche Coderegel wie das AS-Baumuster haben.
            for (iPartsDataModelProperties modelProperties : releasedModelPropertiesForASModel) {
                String asModelCodes = afterSalesModel.getFieldValue(FIELD_DM_CODE);
                String modelPropertiesCodes = modelProperties.getFieldValue(FIELD_DMA_CODE);
                try {
                    // Klonen der DNF ist für getStringRepresentation() nicht notwendig
                    asModelCodes = DaimlerCodes.getDnfCodeOriginal(asModelCodes).getStringRepresentation();
                    modelPropertiesCodes = DaimlerCodes.getDnfCodeOriginal(modelPropertiesCodes).getStringRepresentation();
                } catch (BooleanFunctionSyntaxException e) {
                    continue;
                }
                if (modelPropertiesCodes.equals(asModelCodes)) {
                    similarConstructionModelList.add(modelProperties, DBActionOrigin.FROM_DB);
                }
            }
        } else {
            // Bei der Migration oder falls das AS-BM noch nicht existiert wird die Coderegel später ins AS-Baumuster übernommen.
            similarConstructionModelList = releasedModelPropertiesForASModel;
        }

        // Aus DIALOG übernommene AS-Baumuster  müssen auch als DIALOG Baumuster vorliegen. Wenn es kein DIALOG-Baumuster mehr gibt,
        // darf auch das AS-Baumuster nicht mehr existieren. Also ggf. löschen.
        if (similarConstructionModelList.isEmpty()) {
            String deletedASModel = deleteASModel(afterSalesModelId, afterSalesModel, existInDB, ASModelSource, removeModelAndProductsFromCache,
                                                  project);
            Logger.log(iPartsPlugin.LOG_CHANNEL_MODELS, LogType.DEBUG, "No relevant construction model found for model number \""
                                                                       + constructionModelId.getModelNumber() + "\"." + deletedASModel);
            return false;
        }

        // für die ähnlichen Konstruktionsbaumuster das jeweils höchste DatA und den Datensatz je Baumusternummernsuffix bestimmen
        String[] highestDatAValues = new String[6]; // für die Endziffern mit folgender Priorität: 1, 2, 0, 5, 6, 4
        iPartsDataModelProperties[] newestConstructionModels = new iPartsDataModelProperties[6]; // pro Endziffer der jeweils neueste Datensatz
        for (iPartsDataModelProperties similarConstructionModel : similarConstructionModelList) {
            String modelNumber = similarConstructionModel.getFieldValue(FIELD_DMA_MODEL_NO);
            if (modelNumber.length() < 8) {
                continue;
            }
            char modelNumberSuffix = modelNumber.charAt(7);

            // Priorität für die Endziffer bestimmen
            int priorityIndex = -1;
            switch (modelNumberSuffix) {
                case '1':
                    priorityIndex = 0;
                    break;
                case '2':
                    priorityIndex = 1;
                    break;
                case '0':
                    priorityIndex = 2;
                    break;
                case '5':
                    priorityIndex = 3;
                    break;
                case '6':
                    priorityIndex = 4;
                    break;
                case '4':
                    priorityIndex = 5;
                    break;
            }

            if (priorityIndex == -1) { // falsche Endziffer
                continue;
            }

            // höchsten Datensatz für die Endziffer merken
            String highestDatA = highestDatAValues[priorityIndex];
            if (highestDatA == null) {
                highestDatA = "";
            }
            String attributesDatA = Utils.toSortString(similarConstructionModel.getFieldValue(FIELD_DMA_DATA).toUpperCase());
            if (attributesDatA.toUpperCase().compareTo(highestDatA) > 0) {
                newestConstructionModels[priorityIndex] = similarConstructionModel;
                highestDatAValues[priorityIndex] = attributesDatA;
            }
        }

        // in absteigender Priorität den neusten Datensatz suchen
        iPartsDataModelProperties sourceConstructionModel = null;
        for (iPartsDataModelProperties newestConstructionModelForIndex : newestConstructionModels) {
            if (newestConstructionModelForIndex != null) {
                sourceConstructionModel = newestConstructionModelForIndex;
                break;
            }
        }
        // Synchronisieren (After-Sales Baumuster anlegen bzw. aktualisieren)
        if (sourceConstructionModel != null) {
            // Das Konstruktions-Baumuster, das als AS-relevant bestimmt wurde, wurde gefunden. Bei diesem wird das Flag
            // DMA_AS_RELEVANT gesetzt. Bei den anderen wird das Flag zurückgesetzt, sodass nur eines AS-relevant ist.
            for (iPartsDataModelProperties similarConstructionModel : similarConstructionModelList) {
                boolean isSourceConstructionModel = (similarConstructionModel == sourceConstructionModel);
                similarConstructionModel.setFieldValueAsBoolean(FIELD_DMA_AS_RELEVANT, isSourceConstructionModel, DBActionOrigin.FROM_EDIT);
            }
            similarConstructionModelList.saveToDB(project);

            if (!existInDB) {
                afterSalesModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                if (isDIALOGImport) {
                    // DAIMLER-8064: Bei der DIALOG Urladung/Änderungsdienst bei nicht bereits vorhandenen AS-Baumustern
                    // das Flag DM_MODEL_VISIBLE auf false setzen, damit es im Retail nicht erreicht werden kann.
                    afterSalesModel.setFieldValueAsBoolean(FIELD_DM_MODEL_VISIBLE, false, DBActionOrigin.FROM_EDIT);
                }

                // DAIMLER-15213 Bei der Neuanlage eines AS-FBM "Relevant für BM-Filter" automatisch setzen
                if (!afterSalesModel.getAsId().isAggregateModel()) {
                    afterSalesModel.setFieldValueAsBoolean(FIELD_DM_FILTER_RELEVANT, true, DBActionOrigin.FROM_EDIT);
                }
            }

            // Beim DIALOG-Import wurden evtl. MAD AS-BMs erweitert. Diese haben ab jetzt Quelle DIALOG
            if (isDIALOGImport) {
                afterSalesModel.setFieldValue(FIELD_DM_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
                ASModelSource = iPartsImportDataOrigin.DIALOG;
            }

            boolean overwriteExisting = !isDIALOGImport;
            copyAttributeValuesFromConstructionToAS(project, afterSalesModel, sourceConstructionModel, ASModelSource, overwriteExisting);

            if (afterSalesModel.isNew()) {
                if (removeModelAndProductsFromCache) {
                    removeModelAndProductsFromCache(afterSalesModel.getAsId(), project, iPartsDataChangedEventByEdit.Action.NEW);
                }
                Logger.log(iPartsPlugin.LOG_CHANNEL_MODELS, LogType.DEBUG, "New after sales model \"" + afterSalesModelId.getModelNumber()
                                                                           + "\" added from construction model \""
                                                                           + sourceConstructionModel.getAsId().toString(", ") + "\".");
            } else if (afterSalesModel.isModified()) {
                if (removeModelAndProductsFromCache) {
                    removeModelAndProductsFromCache(afterSalesModel.getAsId(), project, iPartsDataChangedEventByEdit.Action.MODIFIED);
                }
                Logger.log(iPartsPlugin.LOG_CHANNEL_MODELS, LogType.DEBUG, "Existing after sales model \"" + afterSalesModelId.getModelNumber()
                                                                           + "\" updated from construction model \""
                                                                           + sourceConstructionModel.getAsId().toString(", ") + "\".");
            }

            if (ASModelSource == iPartsImportDataOrigin.DIALOG) {
                afterSalesModel.setFieldValue(FIELD_DM_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
            }

            // Den ModelTyp von der Baureihe übernehmen
            String asModelSeriesNo = afterSalesModel.getFieldValue(FIELD_DM_SERIES_NO);
            if (StrUtils.isValid(asModelSeriesNo)) {
                iPartsSeriesId seriesId = new iPartsSeriesId(asModelSeriesNo);
                String aggregateTyp = iPartsDialogSeries.getInstance(project, seriesId).getAggregateType();
                if (StrUtils.isValid(aggregateTyp)) {
                    afterSalesModel.setFieldValue(FIELD_DM_MODEL_TYPE, aggregateTyp, DBActionOrigin.FROM_EDIT);
                }
            }

            afterSalesModel.saveToDB();
            return true;
        } else {
            String deletedASModel = deleteASModel(afterSalesModelId, afterSalesModel, existInDB, ASModelSource, removeModelAndProductsFromCache,
                                                  project);
            Logger.log(iPartsPlugin.LOG_CHANNEL_MODELS, LogType.DEBUG, "No valid after sales model found for model number \""
                                                                       + constructionModelId.getModelNumber() + "\"." + deletedASModel);
            return false;
        }
    }

    public static void copyAttributeValuesFromConstructionToAS(EtkProject project, iPartsDataModel afterSalesModel, iPartsDataModelProperties sourceConstructionModel,
                                                               iPartsImportDataOrigin ASModelSource, boolean overwriteExisting) {
        // Daten von DA_MODEL_PROPERTIES (Quelle X2E) nach DA_MODEL kopieren
        copyAttributesForMapping(sourceConstructionModel, afterSalesModel, modelPropertiesMapping, overwriteExisting);
        if (ASModelSource == iPartsImportDataOrigin.DIALOG) {
            copyAttributesForMapping(sourceConstructionModel, afterSalesModel, modelPropertiesMappingDIALOG, overwriteExisting);
            // DAIMLER-7913, Die betroffenen Attribute dieses Mappings werden zwingend überschrieben:
            copyAttributesForMapping(sourceConstructionModel, afterSalesModel, modelPropertiesForcedMappingDIALOG, true);
        }

        // Daten von DA_MODEL_DATA (Quelle BMS) nach DA_MODEL kopieren
        String sourceModelNumber = sourceConstructionModel.getFieldValue(FIELD_DMA_MODEL_NO);
        iPartsDataModelData sourceModelData = new iPartsDataModelData(project, new iPartsModelDataId(sourceModelNumber));
        if (sourceModelData.loadFromDB(sourceModelData.getAsId())) {
            copyAttributesForMapping(sourceModelData, afterSalesModel, modelDataMapping, overwriteExisting);
            if (ASModelSource == iPartsImportDataOrigin.DIALOG) {
                copyAttributesForMapping(sourceModelData, afterSalesModel, modelDataMappingDIALOG, overwriteExisting);
            }
        } else if (overwriteExisting) {
            deleteAttributesForMapping(afterSalesModel, modelDataMapping);
            if (ASModelSource == iPartsImportDataOrigin.DIALOG) {
                deleteAttributesForMapping(afterSalesModel, modelDataMappingDIALOG);
            }
        }
    }

    private static String deleteASModel(iPartsModelId afterSalesModelId, iPartsDataModel afterSalesModel, boolean existInDB,
                                        iPartsImportDataOrigin source, boolean removeModelAndProductsFromCache, EtkProject project) {
        String deletedASModel = "";
        if (existInDB) {
            if (source == iPartsImportDataOrigin.DIALOG) {
                if (removeModelAndProductsFromCache) {
                    removeModelAndProductsFromCache(afterSalesModel.getAsId(), project, iPartsDataChangedEventByEdit.Action.DELETED);
                }
                deletedASModel = " Existing after sales model \"" + afterSalesModelId.getModelNumber() + "\" deleted.";
                afterSalesModel.deleteFromDB();
            } else if (source == iPartsImportDataOrigin.MAD) {
                // Das AS-Baumuster existiert, aber es besitzt keine Konstruktionsbaumuster. Wenn es vorher durch
                // Konstruktionsbaumuster gesynct wurde und diese Konstruktionsbaumuster aufgrund einer Neu-Versorgung
                // verschwunden sind, müssen die Werte, die vorher gesynct wurden geleert werden.
                deleteAttributesForMapping(afterSalesModel, modelPropertiesMapping);
                afterSalesModel.saveToDB();
            }
        }
        return deletedASModel;
    }

    /**
     * Löscht das übergebene AS-Baumuster sowie alle betroffenen Produkte in allen Cluster-Knoten aus dem Cache.
     *
     * @param afterSalesModelId
     * @param project
     * @param action
     */
    public static void removeModelAndProductsFromCache(iPartsModelId afterSalesModelId, EtkProject project, iPartsDataChangedEventByEdit.Action action) {
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.MODEL,
                                                                                                  action,
                                                                                                  afterSalesModelId,
                                                                                                  false));

        // Alle Produkte, die dieses After-Sales-Baumuster verwenden, aus dem Cache entfernen
        // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster ebenfalls berücksichtigen.
        List<iPartsProduct> productList = iPartsProductHelper.getProductsForModel(project, afterSalesModelId, null,
                                                                                  iPartsProductHelper.PRODUCTS_FOR_SERIES_MODE.FILTER_ALL, false);
        if (!productList.isEmpty()) {
            List<iPartsProductId> productIds = new ArrayList<>(productList.size());
            for (iPartsProduct product : productList) {
                productIds.add(product.getAsId());
            }
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                                                      iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                      productIds,
                                                                                                      false));
        }
    }

    private static void copyAttributesForMapping(EtkDataObject sourceDataObject, iPartsDataModel afterSalesModel,
                                                 Map<String, String> mapping, boolean overwriteExisting) {
        for (Map.Entry<String, String> mappingEntry : mapping.entrySet()) {
            String modelPropertiesFieldName = mappingEntry.getKey();
            String afterSalesModelFieldName = mappingEntry.getValue();
            DBDataObjectAttribute sourceAttribute = sourceDataObject.getAttribute(modelPropertiesFieldName);
            DBDataObjectAttribute destinationAttribute = afterSalesModel.getAttribute(afterSalesModelFieldName);
            if (destinationAttribute.isEmpty() || overwriteExisting) {
                if (sourceAttribute.getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                    // EtkMultiSprache von der Quelle laden und Sprachen mit leeren Texten entfernen (für späteren internen
                    // Vergleich in setValueAsMultiLanguage() notwendig)
                    EtkMultiSprache sourceMultiLanguage = sourceAttribute.getAsMultiLanguage(sourceDataObject, false);
                    sourceMultiLanguage.removeLanguagesWithEmptyTexts();

                    // EtkMultiSprache auch vom Ziel laden und Sprachen mit leeren Texten entfernen (für späteren internen
                    // Vergleich in setValueAsMultiLanguage() notwendig)
                    EtkMultiSprache modelMutliLanguage = destinationAttribute.getAsMultiLanguage(afterSalesModel, false);
                    modelMutliLanguage.removeLanguagesWithEmptyTexts();

                    afterSalesModel.setFieldValueAsMultiLanguage(afterSalesModelFieldName, sourceMultiLanguage, DBActionOrigin.FROM_EDIT);
                } else {
                    afterSalesModel.setFieldValue(afterSalesModelFieldName, sourceAttribute.getAsString(), DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    private static void deleteAttributesForMapping(iPartsDataModel afterSalesModel, Map<String, String> mapping) {
        for (String afterSalesModelFieldName : mapping.values()) {
            DBDataObjectAttribute destinationAttribute = afterSalesModel.getAttribute(afterSalesModelFieldName);
            if (destinationAttribute.getType() == DBDataObjectAttribute.TYPE.MULTI_LANGUAGE) {
                afterSalesModel.setFieldValueAsMultiLanguage(afterSalesModelFieldName, null, DBActionOrigin.FROM_EDIT);
            } else {
                afterSalesModel.setFieldValue(afterSalesModelFieldName, "", DBActionOrigin.FROM_EDIT);
            }
        }
    }
}
