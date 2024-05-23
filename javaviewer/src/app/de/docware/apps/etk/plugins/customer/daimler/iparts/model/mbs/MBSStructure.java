package de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructureList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.SearchMatchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Singleton Object pro Projekt, das die MBS-Struktur komplett enthält. Pro Projekt wird die komplette Sturktur gehalten.
 * Zusätzlich wird pro Baumuster die Baumuster spezifische Struktur geladen und gehalten.
 */
public class MBSStructure implements iPartsConst {

    // Baumuster-abhängige MBS Knoten
    private static ObjectInstanceLRUList<Object, MBSStructure> instancesWithModel = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_MODELS,
                                                                                                                MAX_CACHE_LIFE_TIME_CORE);
    // Alle MBS Knoten für das übergebene {@link EtkProject}
    private static ObjectInstanceLRUList<Object, MBSStructure> instances = new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS,
                                                                                                       MAX_CACHE_LIFE_TIME_CORE);

    // Knoten der ersten Ebene
    protected MBSStructureNodes nodes = new MBSStructureNodes();

    public static synchronized void clearCache() {
        instances.clear();
        instancesWithModel.clear();
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    /**
     * Liefert die komplette Struktur zum Projekt
     *
     * @param project
     * @return
     */
    public static synchronized MBSStructure getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), MBSStructure.class, null, false);
        MBSStructure instanceWithAllNodes = instances.get(hashObject);
        if (instanceWithAllNodes == null) {
            instanceWithAllNodes = new MBSStructure();
            instanceWithAllNodes.load(project);
            if (!instanceWithAllNodes.nodes.getValues().isEmpty()) {
                instances.put(hashObject, instanceWithAllNodes);
            }
        }
        return instanceWithAllNodes;
    }

    /**
     * Liefert die Struktur zu einem Baumuster
     *
     * @param project
     * @param modelId
     * @return
     */
    public static synchronized MBSStructure getInstance(EtkProject project, iPartsModelId modelId) {
        Object hashObjectForModelInstances = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), MBSStructure.class, modelId.getModelNumber(), false);
        MBSStructure result = instancesWithModel.get(hashObjectForModelInstances);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new MBSStructure();
            if (result.loadAllNodesForModelId(project, modelId)) {
                instancesWithModel.put(hashObjectForModelInstances, result);
            }
        }
        return result;
    }

    /**
     * Lädt alle Strukturknoten für das übergebene Baumuster
     *
     * @param project
     * @param modelId
     * @return
     */
    private boolean loadAllNodesForModelId(EtkProject project, iPartsModelId modelId) {
        MBSStructure instanceWithAllNodes = getInstance(project);
        String[] mbsStructureField = new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR };
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_STRUCTURE_MBS,
                                                                                           mbsStructureField,
                                                                                           new String[]{ FIELD_DSM_SNR },
                                                                                           new String[]{ modelId.getModelNumber() },
                                                                                           ExtendedDataTypeLoadType.NONE,
                                                                                           false, true);
        for (DBDataObjectAttributes attributes : attributesList) {
            String keyValue = attributes.getFieldValue(FIELD_DSM_SUB_SNR);
            // Ist die SubNummer leer, ein Aggregate- oder ein Fahrzeugbaumuster, kann der Datensatz übersprungen werden
            if (keyValue.isEmpty() || iPartsModel.isAggregateModel(keyValue) || iPartsModel.isVehicleModel(keyValue)) {
                continue;
            }
            MBSStructureNode node = instanceWithAllNodes.getListNumberNode(keyValue);
            if (node != null) {
                nodes.add(keyValue, node);
            }
        }

        return !nodes.getValues().isEmpty();
    }


    /**
     * Lädt die komplette MBS Struktur
     *
     * @param project
     */
    private void load(EtkProject project) {
        nodes = new MBSStructureNodes();

        iPartsDataMBSStructureList structureList = new iPartsDataMBSStructureList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR, false, false));

        EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String listNumber = attributes.getFieldValue(FIELD_DSM_SNR);

                // Pseudo-Baumuster, die nur ein Mapping zwischen C- und identischem D-Baumuster bzw. C-Baumuster mit Suffix
                // sind, sollen hier nicht berücksichtigt werden
                if (listNumber.endsWith(MBS_VEHICLE_AGGREGATE_MAPPING_SUFFIX)) {
                    return false;
                }

                String conGroup = attributes.getFieldValue(FIELD_DSM_SUB_SNR);
                if (!conGroup.isEmpty()) {
                    MBSStructureNode listNumberNode = nodes.getOrCreate(MBSStructureType.LIST_NUMBER, listNumber, null);
                    listNumberNode.getOrCreateChild(MBSStructureType.CON_GROUP, conGroup, listNumberNode);
                }
                return false;
            }
        };

        structureList.searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields,
                                                new String[]{ TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR),
                                                              TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR),
                                                              TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR),
                                                              TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR) },
                                                new String[]{ EtkDataObjectList.getNotWhereValue(MODEL_NUMBER_PREFIX_AGGREGATE + "*"),
                                                              EtkDataObjectList.getNotWhereValue(MODEL_NUMBER_PREFIX_CAR + "*"),
                                                              EtkDataObjectList.getNotWhereValue(MODEL_NUMBER_PREFIX_AGGREGATE + "*"),
                                                              EtkDataObjectList.getNotWhereValue(MODEL_NUMBER_PREFIX_CAR + "*") },
                                                false,
                                                new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR },
                                                false, true, true, callback);
    }

    public MBSStructureNode getListNumberNode(String listNumber) {
        return nodes.get(listNumber);
    }

    public MBSStructureNode getConGroupNode(String listNumber, String conGroup) {
        MBSStructureNode listNumberNode = nodes.get(listNumber);
        if (listNumberNode != null) {
            return listNumberNode.getChild(conGroup);
        }
        return null;
    }

    public MBSStructureNode getNode(MBSStructureId id) {
        if (id.getConGroup().isEmpty()) {
            return getListNumberNode(id.getListNumber());
        }
        return getConGroupNode(id.getListNumber(), id.getConGroup());
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
    public List<MBSStructureNode> search(EtkDisplayFields selectFields,
                                         List<String> selectValues,
                                         EtkDisplayFields whereFields,
                                         List<String> andOrWhereValues,
                                         boolean andOrSearch,
                                         WildCardSettings wildCardSettings,
                                         String language,
                                         List<String> fallbackLanguages) {
        List<MBSStructureNode> result = new ArrayList<>();

        internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, nodes.getValues());

        return result;
    }

    private void internSearch(List<MBSStructureNode> result,
                              EtkDisplayFields selectFields,
                              List<String> selectValues,
                              EtkDisplayFields whereFields,
                              List<String> andOrWhereValues,
                              boolean andOrSearch,
                              WildCardSettings wildCardSettings,
                              String language, List<String> fallbackLanguages, Collection<MBSStructureNode> nodes) {
        for (MBSStructureNode node : nodes) {
            Map<String, String> fieldsAndValues = new HashMap<>();

            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR), node.getNumber());
            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR), node.getTitle().getTextByNearestLanguage(language, fallbackLanguages));

            if (SearchMatchHelper.fieldsMatchSearchOptions(fieldsAndValues, andOrSearch, selectFields, selectValues, whereFields, andOrWhereValues, wildCardSettings)) {
                result.add(node);
            }

            internSearch(result, selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings, language, fallbackLanguages, node.getChildren());
        }
    }

}
