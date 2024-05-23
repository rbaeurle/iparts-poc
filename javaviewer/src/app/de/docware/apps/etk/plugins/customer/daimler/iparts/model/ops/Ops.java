/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataOPSGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataOPSGroupList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.SearchMatchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.SortType;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Singleton Object pro Projekt, das die Eds/Bcs OPS-Struktur komplett enthält.
 */
public class Ops implements EtkDbConst, iPartsConst {

    // Baumuster-abhängige OPS Knoten
    private static ObjectInstanceLRUList<Object, Ops> instancesWithModel = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_MODELS,
                                                                                                       MAX_CACHE_LIFE_TIME_CORE);

    // Knoten der ersten Ebene
    protected OpsNodes nodes = new OpsNodes();
    protected iPartsModelId modelId;

    public static synchronized void clearCache() {
        instancesWithModel.clear();
    }

    public static synchronized Ops getInstance(EtkProject project, iPartsModelId modelId) {
        Object hashObjectForModelInstances = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), Ops.class, modelId.getModelNumber(), false);
        Ops result = instancesWithModel.get(hashObjectForModelInstances);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new Ops(modelId);
            result.load(project);
            if (!result.nodes.getValues().isEmpty()) {
                instancesWithModel.put(hashObjectForModelInstances, result);
            }
        }
        return result;
    }

    public Ops(iPartsModelId modelId) {
        this.modelId = modelId;
    }

    private void load(EtkProject project) {
        nodes = new OpsNodes();
        String[] opsFields = new String[]{ FIELD_EDS_GROUP, FIELD_EDS_SCOPE, FIELD_EDS_MODEL_MSAAKEY };

        // Die Liste der Ops-Knoten auf einen Schlag laden und aus dem Tabelleninhalt die Knoten erzeugen
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_EDS_MODEL, opsFields,
                                                                                           new String[]{ FIELD_EDS_MODELNO },
                                                                                           new String[]{ modelId.getModelNumber() },
                                                                                           ExtendedDataTypeLoadType.NONE, false, true);
        attributesList.sort(opsFields, SortType.AUTOMATIC);

        for (DBDataObjectAttributes attributes : attributesList) {
            // Baumuster-Aggregate ignorieren im OPS-Scope
            String saaBkNumber = attributes.getFieldValue(FIELD_EDS_MODEL_MSAAKEY);
            if (saaBkNumber.startsWith(MODEL_NUMBER_PREFIX_AGGREGATE)) {
                continue;
            }

            String group = attributes.getFieldValue(FIELD_EDS_GROUP);
            String scope = attributes.getFieldValue(FIELD_EDS_SCOPE);

            // Alle Knotenebenen suchen und falls nicht vorhanden erzeugen
            OpsNode groupNode = nodes.getOrCreate(OpsType.GROUP, group, null);
            groupNode.getOrCreateChild(OpsType.SCOPE, scope, groupNode);
        }
        iPartsDataOPSGroupList groupList = iPartsDataOPSGroupList.loadOPSGroupsForModel(project, modelId);
        iPartsDataOPSGroupList groupListWithoutModel = iPartsDataOPSGroupList.loadOPSGroupsForModel(project, new iPartsModelId(""));
        loadTexts(project, nodes.getValues(), groupList.getAsGroupMap(), groupListWithoutModel.getAsGroupMap());
    }

    private void loadTexts(EtkProject project, Collection<OpsNode> nodes, Map<String, iPartsDataOPSGroup> groupMap, Map<String, iPartsDataOPSGroup> groupMapWithoutModel) {
        for (OpsNode node : nodes) {
            if (node.getId().isGroupNode()) {
                String groupName = node.getId().getGroup();
                iPartsDataOPSGroup group = groupMapWithoutModel.get(groupName);
                // Falls es zur Gruppe ohne Verbindung zum Baumuster keinen Text gibt -> Prüfen, ob es einen Text mit
                // Verbindung zum Baumuster gibt
                if (group == null) {
                    group = groupMap.get(groupName);
                }
                if ((group != null) && !group.isInvalidDataSet()) {
                    node.setTitle(group.getDescription());
                    node.setPictureName(group.getPictureName());
                }
            } else {
                node.setTitle(OpsScopeCache.getInstance(project).getDescriptionForScope(node.getId().getScope()));
                node.setPictureName(OpsScopeCache.getInstance(project).getPictureNameForScope(node.getId().getScope()));
            }
            if (!node.getChildren().isEmpty()) {
                loadTexts(project, node.getChildren(), groupMap, groupMapWithoutModel);
            }
        }
    }

    public OpsNode getGroupNode(String opsGroup) {
        return nodes.get(opsGroup);
    }

    public OpsNode getScopeNode(String opsGroup, String opsScope) {
        OpsNode hmNode = nodes.get(opsGroup);
        if (hmNode != null) {
            return hmNode.getChild(opsScope);
        }
        return null;
    }

    public OpsNode getNode(OpsId id) {
        if (id.getScope().isEmpty()) {
            return getGroupNode(id.getGroup());
        }

        return getScopeNode(id.getGroup(), id.getScope());
    }

    /**
     * Suche in allen Unterknoten nach einer Nummer und oder einem Text
     *
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param andOrWhereValues
     * @param andOrSearch
     * @param wildCardSettings
     * @param language
     * @return
     */
    public List<OpsNode> search(EtkDisplayFields selectFields,
                                List<String> selectValues,
                                EtkDisplayFields whereFields,
                                List<String> andOrWhereValues,
                                boolean andOrSearch,
                                WildCardSettings wildCardSettings,
                                String language,
                                List<String> fallbackLanguages) {
        List<OpsNode> result = new ArrayList<OpsNode>();

        internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, nodes.getValues());

        return result;
    }

    private void internSearch(List<OpsNode> result,
                              EtkDisplayFields selectFields,
                              List<String> selectValues,
                              EtkDisplayFields whereFields,
                              List<String> andOrWhereValues,
                              boolean andOrSearch,
                              WildCardSettings wildCardSettings,
                              String language, List<String> fallbackLanguages, Collection<OpsNode> nodes) {
        for (OpsNode node : nodes) {
            Map<String, String> fieldsAndValues = new HashMap<String, String>();

            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR), node.getNumber());
            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR), node.getTitle().getTextByNearestLanguage(language, fallbackLanguages));

            if (SearchMatchHelper.fieldsMatchSearchOptions(fieldsAndValues, andOrSearch, selectFields, selectValues, whereFields, andOrWhereValues, wildCardSettings)) {
                result.add(node);
            }

            internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, node.getChildren());
        }
    }

}
