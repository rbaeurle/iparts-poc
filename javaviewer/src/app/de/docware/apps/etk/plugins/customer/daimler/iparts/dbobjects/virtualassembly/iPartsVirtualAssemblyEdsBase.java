/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.usage.MechanicUsagePosition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper.SaaPartsListConstKitData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper.iPartsSaaBkConstPartsListHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ConstructionValidationDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.Ops;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping.MappingOpsToEinPas;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.misc.id.IdWithType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basisklasse für die virtuelle EDS/BCS Struktur
 */
public abstract class iPartsVirtualAssemblyEdsBase extends iPartsVirtualAssemblyEinPasBase {

    public static void getMechanicUsageForEdsMaterial(PartId partId, boolean filtered, EtkProject project, List<MechanicUsagePosition> result) {
        iPartsEdsStructureHelper edsStructureHelper = iPartsEdsStructureHelper.getInstance();
        // Alle oberen Sachnummer ermitteln, in denen das Teil eingebaut ist bis zur maximal möglichen Strukturstufe
        Set<String> parentNumbers = iPartsSaaBkConstPartsListHelper.getAllMatParentNumbersOfSaaPartsList(project, partId.getMatNr(), 1, getMaxLevelFromConfig());
        Set<AssemblyId> resultAssemblyIds = new HashSet<>(); // Set mit allen AssemblyIds, um doppeltes Laden zu vermeiden
        for (String parentNo : parentNumbers) {
            // SAA
            EdsSaaId saaId = new EdsSaaId(parentNo);
            iPartsVirtualNode virtualSaaParentNode = new iPartsVirtualNode(iPartsNodeType.EDS_SAA, saaId);

            List<iPartsVirtualNode[]> virtualParentNodesList = getVirtualParentNodesForSaa(saaId, project, edsStructureHelper);

            if (virtualParentNodesList != null) {
                for (iPartsVirtualNode[] virtualParentNodes : virtualParentNodesList) {
                    EtkDataPartListEntry parentAssembly = iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(null,
                                                                                                                              filtered,
                                                                                                                              project,
                                                                                                                              virtualParentNodes[0], // Baumuster-Knoten
                                                                                                                              virtualParentNodes[1], // OPS/MEU/EinPAS-Knoten
                                                                                                                              virtualSaaParentNode); // EDS SAA-Knoten
                    iPartsSaaBkConstPartsListHelper.addAssemblyAsMechanicalUsage(project, parentAssembly, resultAssemblyIds,
                                                                                 partId, filtered, result);
                }
            }
        }
    }

    /**
     * Liefert die höchste Strukturstufe der Änderungsstände für die Suche und dem Aufbau der Stückliste
     *
     * @return
     */
    public static int getMaxLevelFromConfig() {
        return iPartsPlugin.isEditPluginActive() ? de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin.getMaxStructureLevelForEDSBCSConstPartList() : DEFAULT_MAX_CONST_PARTS_LIST_STRUCTURE_LEVEL;
    }

    public static DBDataObjectAttributesList loadModelStructureForSaa(EtkProject project, String saaNumber, iPartsEdsStructureHelper edsStructureHelper) {
        iPartsEdsStructureHelper tempStructureHelper = (edsStructureHelper == null) ? iPartsEdsStructureHelper.getInstance() : edsStructureHelper;
        String[] fields = new String[]{ tempStructureHelper.getModelNumberField(), tempStructureHelper.getUpperStructureValueField(),
                                        tempStructureHelper.getLowerStructureValueField() };
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(tempStructureHelper.getStructureTableName(), fields,
                                                                                           new String[]{ tempStructureHelper.getSubElementField() },
                                                                                           new String[]{ saaNumber },
                                                                                           ExtendedDataTypeLoadType.NONE,
                                                                                           false, true);
        attributesList.sort(fields);
        return attributesList;
    }

    protected static List<iPartsVirtualNode[]> getVirtualParentNodesForSaa(EdsSaaId saaId, EtkProject project, iPartsEdsStructureHelper edsStructureHelper) {
        iPartsEdsStructureHelper structureHelper = (edsStructureHelper == null) ? iPartsEdsStructureHelper.getInstance() : edsStructureHelper;
        // DB-Abfrage anstatt simples Entfernen der letzten virtualId notwendig, weil SAAs in mehreren Struktur-Knoten eingehängt sein können
        String saaNumber = saaId.getSaaNumber();
        DBDataObjectAttributesList distinctEdsPaths = loadModelStructureForSaa(project, saaNumber, structureHelper);
        if (!distinctEdsPaths.isEmpty()) {
            List<iPartsVirtualNode[]> virtualParentNodesList = new ArrayList<>(distinctEdsPaths.size() * 2);

            for (DBDataObjectAttributes edsPath : distinctEdsPaths) {
                iPartsModelId modelId = new iPartsModelId(edsPath.getField(structureHelper.getModelNumberField()).getAsString());

                String upperValue = edsPath.getField(structureHelper.getUpperStructureValueField()).getAsString();
                String lowerValue = edsPath.getField(structureHelper.getLowerStructureValueField()).getAsString();

                // EDS Struktur
                iPartsVirtualNode[] virtualParentNodes = new iPartsVirtualNode[2]; // ohne EDS SAA-Knoten
                virtualParentNodes[0] = structureHelper.createEdsModelVirtualNode(modelId);
                virtualParentNodes[1] = structureHelper.createEdsStructureVirtualNode(upperValue, lowerValue);
                virtualParentNodesList.add(virtualParentNodes);

                // Nur möglich, wenn OPS Struktur aktiv ist. Ein Mapping von ModelElementUsage auf EinPAS gibt es nicht
                if (!structureHelper.isNewStructureActive()) {
                    // EinPAS (gemappt auf die OPS Struktur)
                    OpsId opsId = new OpsId(upperValue, lowerValue);
                    iPartsVirtualNode virtualModelNode = new iPartsVirtualNode(iPartsNodeType.EDS_EINPAS, modelId);
                    MappingOpsToEinPas mapping = MappingOpsToEinPas.getInstance(project);
                    List<MappingOpsToEinPas.SaaAndEinpas> saaAndEinpasList = mapping.get(opsId);
                    if (saaAndEinpasList != null) {
                        for (MappingOpsToEinPas.SaaAndEinpas saaAndEinpas : saaAndEinpasList) {
                            if (saaNumber.startsWith(saaAndEinpas.saaPrefix)) {
                                virtualParentNodes = new iPartsVirtualNode[2]; // ohne EDS SAA-Knoten
                                virtualParentNodes[0] = virtualModelNode;
                                virtualParentNodes[1] = new iPartsVirtualNode(iPartsNodeType.EINPAS, saaAndEinpas.einPasId);
                                virtualParentNodesList.add(virtualParentNodes);
                            }
                        }
                    }
                }
            }

            return virtualParentNodesList;
        } else {
            return null;
        }
    }

    protected iPartsEdsStructureHelper structureHelper;
    protected iPartsSaaBkConstPartsListHelper partsListHelper;

    public iPartsVirtualAssemblyEdsBase(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
        this.structureHelper = iPartsEdsStructureHelper.getInstance();
        this.partsListHelper = new iPartsSaaBkConstPartsListHelper(project, assemblyId, getMaxLevelFromConfig(),
                                                                   iPartsSaaBkConstPartsListHelper.SaaBkConstPartListType.EDS);
    }

    protected DBDataObjectList<EtkDataPartListEntry> loadVirtualFlattenEdsSaa(iPartsCatalogNode completeStructure,
                                                                              EinPasId subNodeId, boolean subAssembliesOnly) {
        iPartsCatalogNode nodeParent;
        if (subNodeId != null) {
            nodeParent = completeStructure.getNode(subNodeId);
        } else {
            // Kein subknoten -> nehme den obersten
            nodeParent = completeStructure;
        }

        if (nodeParent == null) {
            // Stückliste nicht mehr da, evtl. von jemandem gelöscht worden
            return new EtkDataPartListEntryList();
        }
        //Jetzt die Childs an diese Baugruppe anfügen
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();
        int lfdNumber = 0;
        for (iPartsCatalogNode nodeChild : nodeParent.getChildren()) {
            if (nodeChild.getId() instanceof EdsSaaId) {
                // Hier wird eine ganz normale Baugruppe als Child angezeigt
                EdsSaaId childSaaId = (EdsSaaId)nodeChild.getId();
                ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getSaaPartsListConstructionDateHelper();
                SaaPartsListConstKitData consKitDataPartList = partsListHelper.getSaaPartsList(childSaaId.getSaaNumber(), childSaaId.getSaaNumber(), subAssembliesOnly, validationHelper);
                if (consKitDataPartList != null) {
                    lfdNumber = partsListHelper.createAllPartListEntriesSorted(consKitDataPartList, result, lfdNumber);
                }
            }
        }
        return result;
    }


    @Override
    public synchronized void afterLoadPartlist(boolean subAssembliesOnly, EtkDisplayFields fields, DBDataObjectList<EtkDataPartListEntry> partlist,
                                               boolean loadAdditionalData) {
        super.afterLoadPartlist(subAssembliesOnly, fields, partlist, loadAdditionalData);

        if (!subAssembliesOnly && loadAdditionalData) {
            // SAA bestimmen
            EdsSaaId saaId = structureHelper.getEdsStructureSaaFromAssemblyId(getAsId());
            if (saaId != null) {
                // Retail-Stücklisteneinträge für Source-Context SAA bestimmen wenn benötigt
                if (fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.EDS_RETAIL_USE, false)) {
                    String sourceContext = EditConstructionToRetailHelper.createSourceContext(iPartsEntrySourceType.EDS, saaId);
                    Set<String> retailEdsSaaGUIDs = EditConstructionToRetailHelper.getRetailSourceGUIDs(iPartsEntrySourceType.EDS,
                                                                                                        sourceContext, null,
                                                                                                        true, getEtkProject());

                    for (EtkDataPartListEntry partListEntry : partlist) {
                        if (partListEntry instanceof iPartsDataPartListEntry) {
                            String edsGUID = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_SAAGUID);

                            // Im Retail übernommen?
                            String assignedValue;
                            if (retailEdsSaaGUIDs.contains(edsGUID)) {
                                assignedValue = RETAIL_ASSIGNED;
                            } else {
                                assignedValue = RETAIL_NOT_ASSIGNED;
                            }
                            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.EDS_RETAIL_USE,
                                                                   assignedValue, true, DBActionOrigin.FROM_DB);
                        }
                    }
                }
            }
        }
    }


    protected DBDataObjectList<EtkDataPartListEntry> loadVirtualEdsEinPas(iPartsCatalogNode completeStructure, iPartsVirtualNode rootNode,
                                                                          EinPasId subNodeId) {
        iPartsCatalogNode nodeParent;
        if (subNodeId != null) {
            nodeParent = completeStructure.getNode(subNodeId);
        } else {
            // Kein subknoten -> nehme den obersten
            nodeParent = completeStructure;
        }

        if (nodeParent == null) {
            // Stückliste nicht mehr da, evtl. von jemandem gelöscht worden
            return new EtkDataPartListEntryList();
        }


        //Jetzt die Childs an diese Baugruppe anfügen
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();
        int lfdNumber = 0;
        for (iPartsCatalogNode nodeChild : nodeParent.getChildren()) {
            lfdNumber++;
            if (nodeChild.getId() instanceof EdsSaaId) {
                // Hier wird eine ganz normale Baugruppe als Child angezeigt
                EdsSaaId childSaaId = (EdsSaaId)nodeChild.getId();
                EtkDataPartListEntry entry = createEdsSaaNode(lfdNumber, rootNode, nodeParent.getId(), childSaaId);
                result.add(entry, DBActionOrigin.FROM_DB);
            } else if (nodeChild.getId() instanceof EinPasId) {
                // Hier wird die EinPAS angezeigt
                EinPasId childId = (EinPasId)nodeChild.getId();
                EtkDataPartListEntry newEntry = createVirtualNode(lfdNumber, rootNode, new iPartsVirtualNode(iPartsNodeType.EINPAS, childId));
                if (newEntry != null) {
                    result.add(newEntry, DBActionOrigin.FROM_DB);
                }
            } else if (nodeChild.getId() instanceof OpsId) {
                // Hier fehlt das EinPasMapping -> deshalb wird Ops angezeigt
                OpsId childId = (OpsId)nodeChild.getId();
                EtkDataPartListEntry newEntry = createVirtualNode(lfdNumber, rootNode, new iPartsVirtualNode(iPartsNodeType.OPS, childId));
                if (newEntry != null) {
                    result.add(newEntry, DBActionOrigin.FROM_DB);
                }
            }

        }
        return result;
    }

    protected DBDataObjectList<EtkDataPartListEntry> loadVirtualEdsStructure(iPartsCatalogNode completeStructure,
                                                                             iPartsVirtualNode rootNode, IdWithType subNode) {
        iPartsCatalogNode nodeParent = null;
        if (subNode != null) {
            if (!structureHelper.isNewStructureActive() && subNode.getType().equals(OpsId.TYPE)) {
                nodeParent = completeStructure.getNode((OpsId)subNode);
            } else if (structureHelper.isNewStructureActive() && subNode.getType().equals(ModelElementUsageId.TYPE)) {
                nodeParent = completeStructure.getNode((ModelElementUsageId)subNode);
            }
        } else {
            // Kein subknoten -> nehme den obersten
            nodeParent = completeStructure;
        }

        if (nodeParent == null) {
            // Stückliste nicht mehr da, evtl. von jemandem gelöscht worden
            return new EtkDataPartListEntryList();
        }


        //Jetzt die Childs an diese Baugruppe anfügen
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();

        int lfdNumber = 0;
        for (iPartsCatalogNode nodeChild : nodeParent.getChildren()) {
            lfdNumber++;
            IdWithType nodeId = nodeChild.getId();
            if (nodeId instanceof EdsSaaId) {
                // Hier wird eine EDS SAA als Child angezeigt
                EdsSaaId childSaaId = (EdsSaaId)nodeId;
                EtkDataPartListEntry entry = createEdsSaaNode(lfdNumber, rootNode, nodeParent.getId(), childSaaId);
                result.add(entry, DBActionOrigin.FROM_DB);
            } else if ((nodeId instanceof OpsId) || (nodeId instanceof ModelElementUsageId)) {
                // Hier wird die OPS/MEU angezeigt
                EtkDataPartListEntry entry = createVirtualNode(lfdNumber, rootNode, structureHelper.createEdsStructureVirtualNode(nodeId));
                if (entry != null) {
                    result.add(entry, DBActionOrigin.FROM_DB);
                }
            }
        }
        return result;
    }

    public EtkDataPartListEntry createEdsSaaNode(int lfdNumber, iPartsVirtualNode rootNode, IdWithType parentNodeId, EdsSaaId childSaaId) {
        iPartsVirtualNode childNode = new iPartsVirtualNode(iPartsNodeType.EDS_SAA, childSaaId);
        String virtualKeyString = createVirtualKeyString(parentNodeId, rootNode, childNode);
        return iPartsSaaBkConstPartsListHelper.createSaaPartsListNode(getEtkProject(), lfdNumber, getAsId(), virtualKeyString);
    }

    public String createVirtualKeyString(IdWithType parentNodeId, iPartsVirtualNode rootNode, iPartsVirtualNode childNode) {
        String virtualKeyString;
        if ((parentNodeId instanceof OpsId) || (parentNodeId instanceof ModelElementUsageId)) {  // EDS SAA befindet sich in einem OPS/ModelElementUsage-Knoten
            iPartsVirtualNode node = structureHelper.createEdsStructureVirtualNode(parentNodeId);
            virtualKeyString = iPartsVirtualNode.getVirtualIdString(rootNode, node, childNode);
        } else if (parentNodeId instanceof EinPasId) { // EDS SAA befindet sich in einem EinPAS-Knoten
            iPartsVirtualNode einPasNode = new iPartsVirtualNode(iPartsNodeType.EINPAS, parentNodeId);
            virtualKeyString = iPartsVirtualNode.getVirtualIdString(rootNode, einPasNode, childNode);
        } else {
            virtualKeyString = iPartsVirtualNode.getVirtualIdString(rootNode, childNode);
        }
        return virtualKeyString;
    }

    @Override
    public String getOrderNumber() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();

        IdWithType lastNodeId = lastVirtualNode.getId();

        // SAA-Knoten werden in dieser Klasse behandelt
        if (lastNodeId instanceof EdsSaaId) { // Saa
            EdsSaaId saaId = (EdsSaaId)lastNodeId;
            return saaId.getSaaNumber();
        }

        // Hier darf keine Prüfung auf structureHelper.isNewStructureActive() stattfinden
        // Ops-Knoten werden in dieser Klasse behandelt
        if (lastNodeId instanceof OpsId) { // OPS
            OpsId opsId = (OpsId)lastNodeId;
            if (opsId.isGroupNode()) {
                return opsId.getGroup();
            } else if (opsId.isScopeNode()) {
                return opsId.getScope();
            }
        }

        if (lastNodeId instanceof ModelElementUsageId) { // ModelElementUsage
            ModelElementUsageId modelElementUsageId = (ModelElementUsageId)lastNodeId;
            if (modelElementUsageId.isModuleNode()) {
                return modelElementUsageId.getModule();
            } else if (modelElementUsageId.isSubModuleNode()) {
                return modelElementUsageId.getSubModule();
            }
        }

        return super.getOrderNumber();
    }

    @Override
    public String getPartsListType() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof EdsSaaId) { // SAA
            return PARTS_LIST_TYPE_EDS_SAA;
        }

        // Hier darf keine Prüfung auf structureHelper.isNewStructureActive() stattfinden
        if (lastNodeId instanceof OpsId) { // EDS OPS
            OpsId opsId = (OpsId)lastNodeId;
            if (opsId.isGroupNode()) {
                return PARTS_LIST_TYPE_OPS_GROUP;
            } else if (opsId.isScopeNode()) {
                return PARTS_LIST_TYPE_OPS_SCOPE;
            }
        }

        if (lastNodeId instanceof ModelElementUsageId) { // EDS ModelElementUsage
            ModelElementUsageId modelElementUsageId = (ModelElementUsageId)lastNodeId;
            if (modelElementUsageId.isModuleNode()) {
                return PARTS_LIST_TYPE_MODEL_ELEMENT_USAGE_MODULE;
            } else if (modelElementUsageId.isSubModuleNode()) {
                return PARTS_LIST_TYPE_MODEL_ELEMENT_USAGE_SUB_MODULE;
            }
        }

        return super.getPartsListType();
    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof EdsSaaId) { // ein EdsSAA
            String saaOrBkNumber = ((EdsSaaId)lastNodeId).getSaaNumber();
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            if (numberHelper.isValidSaaOrBk(saaOrBkNumber, true)) {
                if (numberHelper.isValidSaa(saaOrBkNumber, true)) {
                    iPartsDataSaa dataSaa = new iPartsDataSaa(getEtkProject(), new iPartsSaaId(saaOrBkNumber));
                    if (dataSaa.existsInDB()) {
                        return dataSaa.getFieldValueAsMultiLanguage(FIELD_DS_CONST_DESC);
                    }
                } else {
                    EtkDataPart part = EtkDataObjectFactory.createDataPart(getEtkProject(), new iPartsPartId(saaOrBkNumber, ""));
                    if (part.existsInDB()) {
                        return part.getFieldValueAsMultiLanguage(FIELD_M_CONST_DESC);
                    }
                }
            }
            return null;
        }

        // Hier darf keine Prüfung auf structureHelper.isNewStructureActive() stattfinden
        if (lastNodeId instanceof ModelElementUsageId) { // ModelElementUsage - Struktur aus TB.f
            iPartsModelId modelId = new iPartsModelId(iPartsVirtualNode.getModelNumberFromAssemblyId(getAsId()));
            if (modelId.isValidId()) {
                ModelElementUsage modelElementUsage = ModelElementUsage.getInstance(getEtkProject(), modelId);
                ModelElementUsageId modelElementUsageId = (ModelElementUsageId)lastNodeId;
                ModelElementUsageNode modelElementUsageNode = modelElementUsage.getNode(modelElementUsageId);
                if (modelElementUsageNode != null) {
                    return modelElementUsageNode.getTitle();
                }
            }
            return null;
        }

        if (lastNodeId instanceof OpsId) { // EDS OPS
            iPartsModelId modelId = new iPartsModelId(iPartsVirtualNode.getModelNumberFromAssemblyId(getAsId()));
            if (modelId.isValidId()) {
                Ops ops = Ops.getInstance(getEtkProject(), modelId);
                OpsId opsId = (OpsId)lastNodeId;

                OpsNode node = ops.getNode(opsId);

                if (node != null) {
                    return node.getTitle();
                }
            }
            return null;
        }


        return super.getTexts();
    }

    @Override
    public void getParentAssemblyEntries(boolean filtered, List<EtkDataPartListEntry> result) {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof EdsSaaId) { // EDS SAA (nur den direkten konkreten Vater-Knoten für diese SAA zurückgeben)
            iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result,
                                                                                filtered,
                                                                                getEtkProject(),
                                                                                getVirtualIds().get(0), // Baumuster-Knoten
                                                                                getVirtualIds().get(1)); // OPS/MEU/EinPAS-Knoten

            return;
        } else if (!structureHelper.isNewStructureActive() && getParentAssemblyEntriesForParentId(OpsId.class, iPartsNodeType.OPS,
                                                                                                  filtered, result)) { // OPS
            return;
        } else if (structureHelper.isNewStructureActive() && getParentAssemblyEntriesForParentId(ModelElementUsageId.class,
                                                                                                 iPartsNodeType.MODEL_ELEMENT_USAGE,
                                                                                                 filtered, result)) { // ModelElementUsage
            return;
        } else if (lastNodeId instanceof iPartsModelId) { // EDS-Baumuster
            // aktuell wie Baureihe behandeln
            getParentAssemblyEntriesForSeriesOrModel(lastVirtualNode, ((iPartsModelId)lastNodeId).getModelNumber(), filtered, result);
            return;
        }

        super.getParentAssemblyEntries(filtered, result);
    }
}