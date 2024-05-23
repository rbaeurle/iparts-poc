/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper.iPartsSaaBkConstPartsListHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ConstructionValidationDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds.iPartsEdsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.CanceledException;
import de.docware.util.collections.weak.WeakKeysMap;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Virtuelle Sicht auf die EDS-Daten in OPS/ModuleElementUsage-Sicht
 */
public class iPartsVirtualAssemblyEdsStructure extends iPartsVirtualAssemblyEdsBase {

    public iPartsVirtualAssemblyEdsStructure(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsEdsModel model = iPartsEdsModel.getInstance(getEtkProject(), (iPartsModelId)(getRootNode().getId()));
        iPartsCatalogNode nodes;
        if (structureHelper.isNewStructureActive()) {
            nodes = model.getCompleteModelElementUsageStructure(getEtkProject());
        } else {
            nodes = model.getCompleteOpsStructure(getEtkProject());
        }
        IdWithType subIdStructureNode = null;
        EdsSaaId subIdSaa = null;
        // Suche, ob der letzte Knoten ein EDS Struktur-Knoten ist
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if ((iPartsVirtualNodeId instanceof OpsId) || (iPartsVirtualNodeId instanceof ModelElementUsageId)) {
                subIdStructureNode = iPartsVirtualNodeId;
                break;
            }
        }
        // Suche, ob der letzte Knoten ein Saa-Knoten ist
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if (iPartsVirtualNodeId instanceof EdsSaaId) {
                subIdSaa = (EdsSaaId)iPartsVirtualNodeId;
                break;
            }
        }
        if (subIdSaa != null) {
            ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getSaaPartsListConstructionDateHelper();
            return partsListHelper.loadVirtualSaaPartsList(subIdSaa.getSaaNumber(), subIdSaa.getSaaNumber(), subAssembliesOnly, validationHelper);
        } else {
            // EDS Struktur-Knoten in subId oder subId == null, dann erste Ebene laden
            return loadVirtualEdsStructure(nodes, getRootNode(), subIdStructureNode);
        }
    }


    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof iPartsModelId) { // Baumuster
            iPartsEdsModel model = iPartsEdsModel.getInstance(getEtkProject(), (iPartsModelId)lastNodeId);

            String titlePostFix = structureHelper.isNewStructureActive() ? "" : " (OPS)";

            // Die Art des Einstiegs in dem mehrsprachigen Feld hinzufügen
            EtkMultiSprache result = new EtkMultiSprache();
            EtkMultiSprache modelTitle = model.getModelTitle();

            List<String> fallbackLanguages = getEtkProject().getDataBaseFallbackLanguages();
            for (String lang : getEtkProject().getConfig().getDatabaseLanguages()) {
                result.setText(lang, modelTitle.getTextByNearestLanguage(lang, fallbackLanguages) + titlePostFix);
            }

            return result;
        }

        return super.getTexts();
    }


    /**
     * Sucht in der EDS-Struktur nach den Werten aus der Suche
     *
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param andOrWhereValues
     * @param andOrSearch
     * @param wildCardSettings
     * @param andOrSearch
     * @param project
     * @param multiLanguageCache
     * @return
     */
    public static iPartsSearchVirtualDataset searchPartListEntriesForEdsStruct(final EtkDisplayFields selectFields,
                                                                               final List<String> selectValues,
                                                                               final EtkDisplayFields whereFields,
                                                                               final List<String> andOrWhereValues,
                                                                               final boolean andOrSearch,
                                                                               final WildCardSettings wildCardSettings,
                                                                               EtkProject project,
                                                                               WeakKeysMap<String, String> multiLanguageCache) {
        return new iPartsSearchVirtualDatasetWithEntries(selectFields, project, multiLanguageCache) {
            @Override
            public List<EtkDataPartListEntry> createResultPartListEntries() throws CanceledException {

                List<EtkDataPartListEntry> result = new ArrayList<>();
                // Für die Anzeige der Stücklisten in der EDS/BCS Konstruktionssicht müssen vorher die zu zeigenden
                // Konstruktionsbaumuster ausgewählt werden. Die Suche soll auch nur diese Baumuster berücksichtigen.
                // Wenn keine Baumuster für die EDS/BCS Konstruktionssicht gewählt wurden, brauch man hier nicht weitermachen.
                Map<String, Set<String>> selectedModelsMap = (Map<String, Set<String>>)Session.get().getAttribute(iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL);
                if ((selectedModelsMap == null) || selectedModelsMap.isEmpty()) {
                    return result;
                }

                // Es sollen nur die Baumuster betrachtet werden, die der Benutzer in der EDS/BCS Konstruktionssicht ausgewählt hat
                iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
                for (Set<String> models : selectedModelsMap.values()) {
                    for (String model : models) {
                        List<? extends AbstractiPartsNode> foundNodes = structureHelper.searchInEdsStructure(project,
                                                                                                             new iPartsModelId(model),
                                                                                                             selectFields,
                                                                                                             selectValues,
                                                                                                             whereFields,
                                                                                                             andOrWhereValues,
                                                                                                             andOrSearch,
                                                                                                             wildCardSettings);
                        if (!foundNodes.isEmpty()) {
                            Set<String> validModelList = createValidModelList(model, structureHelper);
                            // Und jetzt alle gültigen Knoten einfügen
                            // dazu die ParentAssembly ermitteln und davon alle Childs für die spätere Filterung zurückliefern
                            Set<AssemblyId> resultAssemblyIds = new HashSet<>(); // Set mit allen AssemblyIds, um doppeltes Laden zu vermeiden
                            for (AbstractiPartsNode node : foundNodes) {
                                addSearchResultEntries(result, validModelList, resultAssemblyIds, node, structureHelper);
                            }
                        }
                    }
                }
                return result;
            }

            private void addSearchResultEntries(List<EtkDataPartListEntry> result, Set<String> validModelList, Set<AssemblyId> resultAssemblyIds,
                                                AbstractiPartsNode node, iPartsEdsStructureHelper structureHelper) throws CanceledException {
                for (String modelNo : validModelList) {
                    if (Session.currentSessionThreadAppActionCancelled()) {
                        throw new CanceledException(null);
                    }
                    List<iPartsVirtualNode> nodes = new ArrayList<>();

                    nodes.add(structureHelper.createEdsModelVirtualNode(new iPartsModelId(modelNo)));
                    if (node.getParent() != null) {
                        nodes.add(structureHelper.createEdsStructureVirtualNode(node.getParent().getId()));
                    }
                    iPartsVirtualNode[] nodeArray = ArrayUtil.toArray(nodes);
                    if (nodeArray == null) {
                        continue;
                    }
                    AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(nodeArray), "");
                    if (!resultAssemblyIds.contains(assemblyId)) {
                        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                        if (assembly.existsInDB()) {
                            // Alle ungefilterten Unterbaugruppen vom OPS Knoten zurückliefern (darunter befindet sich auch der gesuchte OPS Knoten)
                            result.addAll(assembly.getSubAssemblyEntries(false, null));
                        }
                        resultAssemblyIds.add(assemblyId);
                    }
                }
            }

            private Set<String> createValidModelList(String model, iPartsEdsStructureHelper structureHelper) throws CanceledException {
                Set<String> validModelList = new HashSet<>();
                if (model != null) {
                    validModelList.add(model);
                } else {
                    // Weder auf ein Baumuster oder eine Baureihe ist eingeschränkt
                    // alles ist deshalb möglich -> hole deshalb alles aus der Datenbank
                    DBDataObjectAttributesList recs = project.getDbLayer().getAttributesListCancelable(structureHelper.getStructureTableName(),
                                                                                                       new String[]{}, new String[]{});
                    for (DBDataObjectAttributes rec : recs) {
                        String modulNo = rec.getFieldValue(structureHelper.getModelNumberField());
                        validModelList.add(modulNo);
                    }
                }
                return validModelList;
            }
        };
    }

    /**
     * Sucht in Saas und Baukästen nach den Werten aus der Suche.
     * INFO: An einem OPS Umfang können Saas und Baukästen hängen
     *
     * @param selectFields
     * @param selectValues
     * @param andOrWhereValues
     * @param andOrSearch
     * @param project
     * @param multiLanguageCache
     * @return
     */
    public static iPartsSearchVirtualDataset searchSaaConstKitListEntriesForEdsStruct(final EtkDisplayFields selectFields,
                                                                                      final List<String> selectValues,
                                                                                      final List<String> andOrWhereValues,
                                                                                      final boolean andOrSearch,
                                                                                      EtkProject project,
                                                                                      WeakKeysMap<String, String> multiLanguageCache) {

        return new iPartsSearchVirtualDatasetWithEntries(selectFields, project, multiLanguageCache) {

            @Override
            protected List<EtkDataPartListEntry> createResultPartListEntries() throws CanceledException {
                List<EtkDataPartListEntry> result = new ArrayList<>();
                // Für die Anzeige der Stücklisten in der EDS/BCS Konstruktionssicht müssen vorher die zu zeigenden
                // Konstruktionsbaumuster ausgewählt werden. Die Suche soll auch nur diese Baumuster berücksichtigen.
                // Wenn keine Baumuster für die EDS/BCS Konstruktionssicht gewählt wurden, brauch man hier nicht weitermachen.
                Map<String, Set<String>> selectedModelsMap = (Map<String, Set<String>>)Session.get().getAttribute(iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL);
                boolean validConstModelsSelected = (selectedModelsMap != null) && !selectedModelsMap.isEmpty();
                if (validConstModelsSelected) {
                    validConstModelsSelected = false;
                    for (Set<String> models : selectedModelsMap.values()) {
                        if ((models != null) && !models.isEmpty()) {
                            validConstModelsSelected = true;
                            break;
                        }
                    }
                }
                if (!validConstModelsSelected) {
                    return result;
                }
                // Normalerweise müsste man das Statement wie in EtkSqlDbSelect.addAllSelectFieldsAndOr unglaublich kompliziert mit den verschiedenenen like und upper und andor
                // aufbauen.
                // Hier machen wir es uns einfach, indem wir alle Suchstrings mit like und * auffüllen und mit or in den beiden Felder Nummer und Benennung suchen
                // Das Ergebniss ist alles, was auch nur eventuell den Suchkriterien entsprechen könnte.
                // Falls hier zu viel Ergebnisse zurückkommen, so wird das im PostProzess wieder rausgeworfen

                List<String> selectFields = new ArrayList<>();
                List<String> searchValues = new ArrayList<>();
                String tableName = iPartsEdsStructureHelper.getInstance().getStructureTableName();
                iPartsSaaBkConstPartsListHelper.fillSearchDataForSaaStructSearch(project, tableName, andOrSearch, andOrWhereValues, selectValues, searchValues, selectFields);


                if (searchValues.isEmpty()) {
                    // In der Suche ist nichts relevantes für die SAA's dabei -> null Ergebnis und raus
                    return null;
                }


                // Weil hier nicht nur Saas sondern auch Baukästen vorkommen können, muss zweimal gesucht werden:
                // Saas in DA_SAA und Baukästen in MAT
                DBSQLQuery querySAA = getSqlQueryForSaaOrConstKit(project, selectFields, searchValues, selectedModelsMap,
                                                                  TABLE_DA_SAA, FIELD_DS_SAA, FIELD_DS_CONST_DESC);
                DBSQLQuery queryConstKit = getSqlQueryForSaaOrConstKit(project, selectFields, searchValues, selectedModelsMap,
                                                                       TABLE_MAT, FIELD_M_MATNR, FIELD_M_CONST_DESC);

                EtkDatabaseTable tableWithStructData = project.getConfig().getDBDescription().getTable(tableName);
                if (tableWithStructData != null) {
                    handleResultSet(project, tableWithStructData, result, querySAA);
                    handleResultSet(project, tableWithStructData, result, queryConstKit);
                }

                return result;
            }
        };
    }

    private static void handleResultSet(EtkProject project, EtkDatabaseTable tableWithStructData, List<EtkDataPartListEntry> result,
                                        DBSQLQuery query) throws CanceledException {
        if (query == null) {
            return;
        }
        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
        DBDataSetCancelable dbSet = query.executeQueryCancelable();
        Set<AssemblyId> assemblyIdList = new LinkedHashSet<>();
        String modelField = structureHelper.getModelNumberField();
        String upperStructureValueField = structureHelper.getUpperStructureValueField();
        String lowerStructureValueField = structureHelper.getLowerStructureValueField();
        while (dbSet.next()) {
            List<String> strList = dbSet.getStringList();
            DBDataObjectAttributes attributes = new DBDataObjectAttributes();
            for (int lfdNr = 0; lfdNr < tableWithStructData.getFieldList().size(); lfdNr++) {
                attributes.addField(tableWithStructData.getField(lfdNr).getName(), strList.get(lfdNr), DBActionOrigin.FROM_DB);
            }
            iPartsVirtualNode virtualRootNode = structureHelper.createEdsModelVirtualNode(new iPartsModelId(attributes.get(modelField).getAsString()));
            iPartsVirtualNode structureNode = structureHelper.createEdsStructureVirtualNode(attributes.get(upperStructureValueField).getAsString(),
                                                                                            attributes.get(lowerStructureValueField).getAsString());
            AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(virtualRootNode, structureNode), "");
            assemblyIdList.add(assemblyId);
        }
        dbSet.close();

        iPartsSaaBkConstPartsListHelper.fillSearchResultsFromSubAssemblyEntries(project, assemblyIdList, result);
    }

    public static DBSQLQuery getSqlQueryForSaaOrConstKit(EtkProject project, List<String> selectFields, List<String> searchValues,
                                                         Map<String, Set<String>> selectedModelsMap, String saaConstKitTable,
                                                         String saaConstKitField, String saaConstKitDescField) {
        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
        String tableName = structureHelper.getStructureTableName();
        String subElementField = structureHelper.getSubElementField();
        String modelField = structureHelper.getModelNumberField();
        return iPartsSaaBkConstPartsListHelper.createQueryForSaaStructSearch(project, tableName, subElementField,
                                                                             selectFields, saaConstKitTable,
                                                                             saaConstKitField, saaConstKitDescField,
                                                                             modelField, searchValues, selectedModelsMap);
    }
}
