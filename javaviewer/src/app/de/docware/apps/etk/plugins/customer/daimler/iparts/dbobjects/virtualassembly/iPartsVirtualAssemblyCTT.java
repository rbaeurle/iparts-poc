/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.usage.MechanicUsagePosition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper.iPartsSaaBkConstPartsListHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ConstructionValidationDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCTTHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.ctt.iPartsCTTModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.*;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.weak.WeakKeysMap;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.*;

import java.util.*;

/**
 * Virtuelle Sicht auf die CTT-Konstruktionsdaten
 */
public class iPartsVirtualAssemblyCTT extends iPartsVirtualAssembly {

    private final iPartsSaaBkConstPartsListHelper partsListHelper;

    public iPartsVirtualAssemblyCTT(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
        this.partsListHelper = new iPartsSaaBkConstPartsListHelper(project, assemblyId, getMaxLevelFromConfig(),
                                                                   iPartsSaaBkConstPartsListHelper.SaaBkConstPartListType.CTT);
    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof iPartsModelId) { // Baumuster
            iPartsCTTModel model = iPartsCTTModel.getInstance(getEtkProject(), (iPartsModelId)lastNodeId);

            // Die Art des Einstiegs in dem mehrsprachigen Feld hinzufügen
            EtkMultiSprache result = new EtkMultiSprache();
            EtkMultiSprache modelTitle = model.getModelTitle();

            List<String> fallbackLanguages = getEtkProject().getDataBaseFallbackLanguages();
            for (String lang : getEtkProject().getConfig().getDatabaseLanguages()) {
                result.setText(lang, modelTitle.getTextByNearestLanguage(lang, fallbackLanguages));
            }

            return result;
        }
        if (lastNodeId instanceof EdsSaaId) { // ein EdsSAA
            String saaOrBkNumber = ((EdsSaaId)lastNodeId).getSaaNumber();
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            if (numberHelper.isValidSaaOrBk(saaOrBkNumber, true)) {
                // Bei einer gültigen SAA muss in der CTT Konstruktion die Benennung aus den Stammdaten der HMO Nummer ermittelt werden
                if (numberHelper.isValidSaa(saaOrBkNumber, true)) {
                    iPartsVirtualNode modelNode = getRootNode();
                    IdWithType modelId = modelNode.getId();
                    if (modelId instanceof iPartsModelId) {
                        iPartsCTTModel model = iPartsCTTModel.getInstance(getEtkProject(), (iPartsModelId)modelId);
                        String mappedHMONumber = model.getHmoForSaa(saaOrBkNumber);
                        // Wenn keine HMO Nummer gefunden wurde, wird zur Sicherheit nach der Benennung in den Stammdaten der SAA gesucht
                        if (StrUtils.isEmpty(mappedHMONumber)) {
                            iPartsDataSaa dataSaa = new iPartsDataSaa(getEtkProject(), new iPartsSaaId(saaOrBkNumber));
                            if (dataSaa.existsInDB()) {
                                return dataSaa.getFieldValueAsMultiLanguage(FIELD_DS_CONST_DESC);
                            }
                        }
                        saaOrBkNumber = mappedHMONumber;
                    }
                }

                // Wenn es eine A-Sachnummer (Baukasten= oder eine HMO Nummer) ist -> Suche die Benennung in der MAT Tabelle
                EtkDataPart part = EtkDataObjectFactory.createDataPart(getEtkProject(), new iPartsPartId(saaOrBkNumber, ""));
                if (part.existsInDB()) {
                    return part.getFieldValueAsMultiLanguage(FIELD_M_CONST_DESC);
                }
            }
            return null;
        }

        return super.getTexts();
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsModelId modelId = (iPartsModelId)(getRootNode().getId());
        // Die komplette Struktur holen
        iPartsCTTModel model = iPartsCTTModel.getInstance(getEtkProject(), modelId);
        iPartsCatalogNode nodes = model.getCompleteCTTStructure(getEtkProject());
        EdsSaaId saaId = null;
        // Check, ob der letzte Knoten der aktuellen Ebene ein SAA-Knoten ist
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if (iPartsVirtualNodeId instanceof EdsSaaId) {
                saaId = (EdsSaaId)iPartsVirtualNodeId;
                break;
            }
        }

        // Falls wir bei der SAA sind, dann muss die Stückliste zur SAA geladen werden. Sind wir auf der Ebene des
        // Baumusters, laden wir die Daten dafür.
        if (saaId != null) {
            ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getSaaPartsListConstructionDateHelper();
            String saaNumber = saaId.getSaaNumber();
            String hmoNumber = model.getHmoForSaa(saaNumber);
            String validNumber = StrUtils.isValid(hmoNumber) ? hmoNumber : saaNumber;
            return partsListHelper.loadVirtualSaaPartsList(validNumber, validNumber, subAssembliesOnly, validationHelper);
        } else {
            return loadModelNode(nodes, getRootNode(), modelId);
        }
    }

    /**
     * Lädt die Daten für die Ebene des Baumusters in der CTT Struktur
     *
     * @param modelNode
     * @param rootNode
     * @param modelId
     * @return
     */
    private DBDataObjectList<EtkDataPartListEntry> loadModelNode(iPartsCatalogNode modelNode, iPartsVirtualNode rootNode,
                                                                 iPartsModelId modelId) {
        if (modelId == null) {
            // Stückliste nicht mehr da, evtl. von jemandem gelöscht worden
            return new EtkDataPartListEntryList();
        }

        //Jetzt die Kinder an diese Baugruppe anfügen (SAAs)
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();

        int lfdNumber = 0;
        for (iPartsCatalogNode nodeChild : modelNode.getChildren()) {
            lfdNumber++;
            IdWithType nodeId = nodeChild.getId();
            if (nodeId instanceof EdsSaaId) {
                // Hier wird eine EDS SAA als Child angezeigt
                EdsSaaId childSaaId = (EdsSaaId)nodeId;
                iPartsVirtualNode childNode = new iPartsVirtualNode(iPartsNodeType.EDS_SAA, childSaaId);
                String virtualKeyString = iPartsVirtualNode.getVirtualIdString(rootNode, childNode);
                EtkDataPartListEntry entry = iPartsSaaBkConstPartsListHelper.createSaaPartsListNode(getEtkProject(),
                                                                                                    lfdNumber, getAsId(),
                                                                                                    virtualKeyString);
                result.add(entry, DBActionOrigin.FROM_DB);
            }
        }
        return result;
    }

    @Override
    public String getOrderNumber() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();
        if (lastNodeId instanceof EdsSaaId) { // SAA
            EdsSaaId saaId = (EdsSaaId)lastNodeId;
            return saaId.getSaaNumber();
        }
        return super.getOrderNumber();
    }

    @Override
    public String getPartsListType() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();
        if (lastNodeId instanceof EdsSaaId) {
            return PARTS_LIST_TYPE_CTT_SAA;
        }
        return super.getPartsListType();
    }

    @Override
    public void getParentAssemblyEntries(boolean filtered, List<EtkDataPartListEntry> result) {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof EdsSaaId) { // EDS SAA (nur den direkten konkreten Vater-Knoten für diese SAA zurückgeben)
            iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result,
                                                                                filtered,
                                                                                getEtkProject(),
                                                                                getVirtualIds().get(0)); // Baumuster-Knoten

            return;
        } else if (lastNodeId instanceof iPartsModelId) { // CTT-Baumuster
            // aktuell wie Baureihe behandeln
            getParentAssemblyEntriesForSeriesOrModel(lastVirtualNode, ((iPartsModelId)lastNodeId).getModelNumber(), filtered, result);
            return;
        }

        super.getParentAssemblyEntries(filtered, result);
    }

    /**
     * Liefert die höchste Strukturstufe der Änderungsstände für die Suche und dem Aufbau der Stückliste
     *
     * @return
     */
    public static int getMaxLevelFromConfig() {
        return iPartsPlugin.isEditPluginActive() ? de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin.getMaxStructureLevelForCTTConstPartList()
                                                 : DEFAULT_MAX_CONST_PARTS_LIST_STRUCTURE_LEVEL;
    }

    /**
     * Liefert die direkten übergeordneten Knoten zur übergebenen SAA
     *
     * @param saaId
     * @param project
     * @return
     */
    protected static List<iPartsVirtualNode[]> getVirtualParentNodesForSaa(EdsSaaId saaId, EtkProject project) {
        // DB-Abfrage anstatt simples Entfernen der letzten virtualId notwendig, weil SAAs in mehreren Baumuster-Knoten eingehängt sein können
        String saaNumber = saaId.getSaaNumber();
        // Alle CTT Baumuster zur SAA laden
        DBDataObjectAttributesList distinctPaths = iPartsCTTHelper.loadCTTModelsForSaa(project, saaNumber);
        if (!distinctPaths.isEmpty()) {
            List<iPartsVirtualNode[]> virtualParentNodesList = new ArrayList<>(distinctPaths.size());

            for (DBDataObjectAttributes path : distinctPaths) {
                // CTT Baumuster Knoten pro BM erzeugen (ohne EDS SAA-Knoten)
                iPartsModelId modelId = new iPartsModelId(path.getFieldValue(FIELD_DA_ESM_MODEL_NO));
                iPartsVirtualNode node = new iPartsVirtualNode(iPartsNodeType.CTT_MODEL, modelId);
                iPartsVirtualNode[] virtualParentNodes = new iPartsVirtualNode[1];
                virtualParentNodes[0] = node;
                virtualParentNodesList.add(virtualParentNodes);
            }

            return virtualParentNodesList;
        } else {
            return null;
        }
    }

    /**
     * Sucht in der Beziehung zwischen Baumuster und SAAs (z.B. Benennungen)
     *
     * @param selectFields
     * @param selectValues
     * @param andOrWhereValues
     * @param andOrSearch
     * @param project
     * @param multiLanguageCache
     * @return
     */
    public static iPartsSearchVirtualDataset searchPartListEntriesWithinCTTStruct(final EtkDisplayFields selectFields,
                                                                                  final List<String> selectValues,
                                                                                  final List<String> andOrWhereValues,
                                                                                  final boolean andOrSearch,
                                                                                  EtkProject project,
                                                                                  WeakKeysMap<String, String> multiLanguageCache) {
        return new iPartsSearchVirtualDatasetWithEntries(selectFields, project, multiLanguageCache) {
            @Override
            public List<EtkDataPartListEntry> createResultPartListEntries() throws CanceledException {
                List<EtkDataPartListEntry> result = new ArrayList<>();
                // Für die Anzeige der Stücklisten in der CTT Konstruktionssicht müssen vorher die zu zeigenden
                // Konstruktionsbaumuster ausgewählt werden. Die Suche soll auch nur diese Baumuster berücksichtigen.
                // Wenn keine Baumuster für die CTT Konstruktionssicht gewählt wurden, braucht man hier nicht weiterzumachen.
                Map<String, Set<String>> selectedModelsMap = SessionKeyHelper.getSelectedCTTModelMapWithUserSettingsCheck(project);
                if (!Utils.isValid(selectedModelsMap)) {
                    return result;
                }

                EtkDatabaseTable edsSaaModelsTable = project.getConfig().getDBDescription().getTable(TABLE_DA_EDS_SAA_MODELS);
                EtkDatabaseTable hmoSaaMappingTable = project.getConfig().getDBDescription().getTable(TABLE_DA_HMO_SAA_MAPPING);
                if ((edsSaaModelsTable == null) || (hmoSaaMappingTable == null)) {
                    return result;
                }

                List<String> selectFields = new ArrayList<>();
                List<String> searchValues = new ArrayList<>();
                iPartsSaaBkConstPartsListHelper.fillSearchDataForSaaStructSearch(project,
                                                                                 TABLE_DA_EDS_SAA_MODELS,
                                                                                 andOrSearch,
                                                                                 andOrWhereValues,
                                                                                 selectValues, searchValues,
                                                                                 selectFields);
                for (EtkDatabaseField field : hmoSaaMappingTable.getFieldList()) {
                    selectFields.add(TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, field.getName()));
                }

                if (searchValues.isEmpty()) {
                    // In der Suche ist nichts Relevantes für die SAA's dabei -> null Ergebnis und raus
                    return null;
                }
                DBSQLQuery query = project.getDB().getDBForDomain(DBDatabaseDomain.MAIN).getNewQuery();
                query.select(new Fields(selectFields)).from(new Tables(TABLE_DA_EDS_SAA_MODELS));

                // Join HMO <-> SAA Mapping
                Condition hmoCondition = new Condition(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO), Condition.OPERATOR_EQUALS,
                                                       new Fields(TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_SAA)));
                query.join(new InnerJoin(TABLE_DA_HMO_SAA_MAPPING, hmoCondition));
                // Join auf MAT um die Benennung der HMO Nummer zu bekommen
                hmoCondition = new Condition(TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_HMO), Condition.OPERATOR_EQUALS,
                                             new Fields(TableAndFieldName.make(TABLE_MAT, FIELD_M_MATNR)));
                query.join(new InnerJoin(TABLE_MAT, hmoCondition));

                // Join auf die Tabelle, die die Bezeichnungen für die gesuchte SAA/HMO oder den gesuchten Baukasten hat
                Condition langCondition = new Condition(TableAndFieldName.make(TABLE_MAT, FIELD_M_CONST_DESC), Condition.OPERATOR_EQUALS,
                                                        new Fields(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTNR)));
                query.join(new InnerJoin(TABLE_SPRACHE, langCondition));
                AbstractCondition completeCondition
                        = iPartsSaaBkConstPartsListHelper.createCompleteWhereConditionForSaaStructSearch(project, searchValues,
                                                                                                         TABLE_DA_EDS_SAA_MODELS,
                                                                                                         FIELD_DA_ESM_SAA_NO,
                                                                                                         TABLE_MAT,
                                                                                                         FIELD_M_CONST_DESC,
                                                                                                         FIELD_DA_ESM_MODEL_NO,
                                                                                                         selectedModelsMap);
                query.where(completeCondition);

                Set<AssemblyId> assemblyIdList = new LinkedHashSet<>();
                try (DBDataSetCancelable dbSet = query.executeQueryCancelable()) {

                    // Pro Treffer eine AssemblyId erzeugen
                    while (dbSet.next()) {
                        List<String> strList = dbSet.getStringList();
                        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                        for (int lfdNr = 0; lfdNr < edsSaaModelsTable.getFieldList().size(); lfdNr++) {
                            attributes.addField(edsSaaModelsTable.getField(lfdNr).getName(), strList.get(lfdNr), DBActionOrigin.FROM_DB);
                        }
                        iPartsVirtualNode node = new iPartsVirtualNode(iPartsNodeType.CTT_MODEL, new iPartsModelId(attributes.get(FIELD_DA_ESM_MODEL_NO).getAsString()));
                        AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(node), "");
                        assemblyIdList.add(assemblyId);
                    }
                }
                iPartsSaaBkConstPartsListHelper.fillSearchResultsFromSubAssemblyEntries(project, assemblyIdList, result);
                return result;
            }
        };
    }


    /**
     * Liefert die Verwendungsstellen {@link MechanicUsagePosition} für das übergebene Teil innerhalb der CTT Stücklisten
     *
     * @param partId
     * @param filtered
     * @param project
     * @param result
     */
    public static void getMechanicUsageForCTTMaterial(PartId partId, boolean filtered, EtkProject project, List<MechanicUsagePosition> result) {
        // Ist kein BM aktiv, dann kann eine CTT Stückliste nicht angesteuert werden.
        Map<String, Set<String>> selectedModelsMap = SessionKeyHelper.getSelectedCTTModelMapWithUserSettingsCheck(project);
        if (!Utils.isValid(selectedModelsMap)) {
            return;
        }

        // Alle oberen Sachnummern ermitteln, in denen das Teil eingebaut ist bis zur maximal möglichen Strukturstufe
        Set<String> parentNumbers = iPartsSaaBkConstPartsListHelper.getAllMatParentNumbersOfSaaPartsList(project,
                                                                                                         partId.getMatNr(),
                                                                                                         1,
                                                                                                         getMaxLevelFromConfig());
        if (parentNumbers.isEmpty()) {
            return;
        }

        // Alle CTT-Baumuster aufsammeln
        Map<String, iPartsCTTModel> allActiveModels = new TreeMap<>();
        selectedModelsMap.values().forEach(modelNumbers -> {
            for (String modelNumber : modelNumbers) {
                allActiveModels.putIfAbsent(modelNumber, iPartsCTTModel.getInstance(project, new iPartsModelId(modelNumber)));
            }
        });

        Set<AssemblyId> resultAssemblyIds = new HashSet<>(); // Set mit allen AssemblyIds, um doppeltes Laden zu vermeiden
        for (String parentNo : parentNumbers) {
            // Hier können echte Baukasten (A Sachnummern) und HMO Nummern vorkommen aber keine SAA Nummern. Check,
            // ob es zur aktuellen Nummer in den aktiven Baumustern ein HMO Mapping gibt. Falls nicht, ist es ein echter
            // Baukasten und hier nicht relevant.
            Set<String> mappedSaaNumbers = new HashSet<>();
            for (iPartsCTTModel cttModel : allActiveModels.values()) {
                String mappedSaaNumber = cttModel.getSaaForHmo(parentNo);
                if (StrUtils.isValid(mappedSaaNumber)) {
                    mappedSaaNumbers.add(mappedSaaNumber);
                }
            }
            mappedSaaNumbers.forEach(saaNumber -> {
                EdsSaaId saaId = new EdsSaaId(saaNumber);
                iPartsVirtualNode virtualSaaParentNode = new iPartsVirtualNode(iPartsNodeType.EDS_SAA, saaId);

                List<iPartsVirtualNode[]> virtualParentNodesList = getVirtualParentNodesForSaa(saaId, project);

                if (virtualParentNodesList != null) {
                    for (iPartsVirtualNode[] virtualParentNodes : virtualParentNodesList) {
                        EtkDataPartListEntry parentAssembly = iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(null,
                                                                                                                                  filtered,
                                                                                                                                  project,
                                                                                                                                  virtualParentNodes[0], // Baumuster-Knoten
                                                                                                                                  virtualSaaParentNode); // EDS SAA-Knoten
                        iPartsSaaBkConstPartsListHelper.addAssemblyAsMechanicalUsage(project, parentAssembly, resultAssemblyIds,
                                                                                     partId, filtered, result);
                    }
                }
            });
        }
    }
}
