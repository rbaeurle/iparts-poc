/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.SearchMatchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.SortType;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton Object pro Projekt, das die neue EDS/BCS-Struktur komplett enthält.
 */
public class ModelElementUsage implements iPartsConst {

    private static final ObjectInstanceLRUList<Object, ModelElementUsage> INSTANCES_WITH_MODEL = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_MODELS,
                                                                                                                             MAX_CACHE_LIFE_TIME_CORE);

    // Knoten der ersten Ebene
    protected ModelElementUsageNodes nodes = new ModelElementUsageNodes();
    protected iPartsModelId modelId;

    public static synchronized void clearCache() {
        INSTANCES_WITH_MODEL.clear();
    }

    public static synchronized ModelElementUsage getInstance(EtkProject project, iPartsModelId modelId) {
        Object hashObjectForModelInstances = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), ModelElementUsage.class,
                                                                              modelId.getModelNumber(), false);
        ModelElementUsage result = INSTANCES_WITH_MODEL.get(hashObjectForModelInstances);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new ModelElementUsage(modelId);
            result.load(project);
            if (!result.nodes.getValues().isEmpty()) {
                INSTANCES_WITH_MODEL.put(hashObjectForModelInstances, result);
            }
        }
        return result;
    }

    public ModelElementUsage(iPartsModelId modelId) {
        this.modelId = modelId;
    }

    private void load(EtkProject project) {
        nodes = new ModelElementUsageNodes();
        String[] structureFields = new String[]{ FIELD_DMEU_MODULE, FIELD_DMEU_SUB_MODULE, FIELD_DMEU_SUB_ELEMENT };

        // Die Liste der Struktur-Knoten auf einen Schlag laden und aus dem Tabelleninhalt die Knoten erzeugen
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_MODEL_ELEMENT_USAGE, structureFields,
                                                                                           new String[]{ FIELD_DMEU_MODELNO },
                                                                                           new String[]{ modelId.getModelNumber() },
                                                                                           ExtendedDataTypeLoadType.NONE, false, true);
        attributesList.sort(structureFields, SortType.AUTOMATIC);

        for (DBDataObjectAttributes attributes : attributesList) {
            // Baumuster-Aggregate ignorieren
            String subElement = attributes.getFieldValue(FIELD_DMEU_SUB_ELEMENT);
            if (subElement.startsWith(MODEL_NUMBER_PREFIX_AGGREGATE)) {
                continue;
            }

            String module = attributes.getFieldValue(FIELD_DMEU_MODULE);
            String subModule = attributes.getFieldValue(FIELD_DMEU_SUB_MODULE);

            // Alle Knotenebenen suchen und falls nicht vorhanden erzeugen
            ModelElementUsageNode moduleNode = nodes.getOrCreate(ModelElementUsageType.MODULE, module, null);
            moduleNode.getOrCreateChild(ModelElementUsageType.SUB_MODULE, subModule, moduleNode);
        }

        loadTexts(project, nodes.getValues());
    }

    private void loadTexts(EtkProject project, Collection<ModelElementUsageNode> nodes) {
        for (ModelElementUsageNode node : nodes) {
            if (node.getId().isModuleNode()) {
                String module = node.getId().getModule();
                node.setTitle(ModuleConstructionCache.getInstance(project).getDescriptionForModule(module));
                node.setPictureName(ModuleConstructionCache.getInstance(project).getPictureNameForModule(module));
            } else {
                String subModule = node.getId().getSubModule();
                node.setTitle(SubModuleConstructionCache.getInstance(project).getDescriptionForSubModule(subModule));
                node.setPictureName(SubModuleConstructionCache.getInstance(project).getPictureNameForSubModule(subModule));
            }
            if (!node.getChildren().isEmpty()) {
                loadTexts(project, node.getChildren());
            }
        }
    }

    public ModelElementUsageNode getModuleNode(String module) {
        return nodes.get(module);
    }

    public ModelElementUsageNode getSubModuleNode(String module, String subModule) {
        ModelElementUsageNode moduleNode = nodes.get(module);
        if (moduleNode != null) {
            return moduleNode.getChild(subModule);
        }
        return null;
    }

    public ModelElementUsageNode getNode(ModelElementUsageId id) {
        if (id.getSubModule().isEmpty()) {
            return getModuleNode(id.getModule());
        }

        return getSubModuleNode(id.getModule(), id.getSubModule());
    }

    public List<ModelElementUsageNode> search(EtkDisplayFields selectFields,
                                              List<String> selectValues,
                                              EtkDisplayFields whereFields,
                                              List<String> andOrWhereValues,
                                              boolean andOrSearch,
                                              WildCardSettings wildCardSettings,
                                              String language,
                                              List<String> fallbackLanguages) {
        List<ModelElementUsageNode> result = new DwList<>();

        internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language,
                     fallbackLanguages, nodes.getValues());

        return result;
    }

    private void internSearch(List<ModelElementUsageNode> result,
                              EtkDisplayFields selectFields,
                              List<String> selectValues,
                              EtkDisplayFields whereFields,
                              List<String> andOrWhereValues,
                              boolean andOrSearch,
                              WildCardSettings wildCardSettings,
                              String language, List<String> fallbackLanguages, Collection<ModelElementUsageNode> nodes) {
        for (ModelElementUsageNode node : nodes) {
            Map<String, String> fieldsAndValues = new HashMap<>();

            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR), node.getNumber());
            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR), node.getTitle().getTextByNearestLanguage(language,
                                                                                                                            fallbackLanguages));

            if (SearchMatchHelper.fieldsMatchSearchOptions(fieldsAndValues, andOrSearch, selectFields, selectValues, whereFields,
                                                           andOrWhereValues, wildCardSettings)) {
                result.add(node);
            }
            internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings,
                         language, fallbackLanguages, node.getChildren());
        }
    }
}
