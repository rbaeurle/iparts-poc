/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.SearchMatchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureModelNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureProductNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureSeriesNode;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.CanceledException;
import de.docware.util.collections.weak.WeakKeysMap;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Basisklasse für die Strukturknoten
 */
public abstract class iPartsVirtualAssemblyStructureBase extends iPartsVirtualAssembly {

    public iPartsVirtualAssemblyStructureBase(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    protected DBDataObjectList<EtkDataPartListEntry> loadVirtualStructureNode(Collection<iPartsStructureNode> structureNodes,
                                                                              Collection<iPartsStructureProductNode> productNodes,
                                                                              Collection<iPartsStructureSeriesNode> seriesNodes,
                                                                              Collection<iPartsStructureModelNode> modelNodes) {
        int lfdNumber = 0;
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();

        for (iPartsStructureNode node : structureNodes) {
            lfdNumber++;
            EtkDataPartListEntry newEntry = createStructureNode(lfdNumber, node);
            if (newEntry != null) {
                result.add(newEntry, DBActionOrigin.FROM_DB);
            }
        }

        for (iPartsStructureProductNode productNode : productNodes) {
            lfdNumber++;
            EtkDataPartListEntry newEntry = createProductNode(lfdNumber, productNode.getProductId(), productNode.getStructureType());
            if (newEntry != null) {
                result.add(newEntry, DBActionOrigin.FROM_DB);
            }

        }

        for (iPartsStructureSeriesNode seriesNode : seriesNodes) {
            lfdNumber++;
            EtkDataPartListEntry newEntry = createSeriesNode(lfdNumber, seriesNode.getSeriesId(), seriesNode.getStructureType());
            if (newEntry != null) {
                result.add(newEntry, DBActionOrigin.FROM_DB);
            }

        }

        for (iPartsStructureModelNode modelNode : modelNodes) {
            lfdNumber++;
            EtkDataPartListEntry newEntry = createModelNode(lfdNumber, modelNode.getModelId(), modelNode.getStructureType());
            if (newEntry != null) {
                result.add(newEntry, DBActionOrigin.FROM_DB);
            }
        }

        return result;
    }

    private EtkDataPartListEntry createStructureNode(int lfdNumber, iPartsStructureNode node) {
        iPartsVirtualNode virtualNode = new iPartsVirtualNode(iPartsNodeType.STRUCTURE, node.getId());
        return createVirtualNode(lfdNumber, virtualNode);
    }

    private EtkDataPartListEntry createProductNode(int lfdNumber, iPartsProductId productId, iPartsNodeType nodeType) {
        iPartsVirtualNode virtualNode = new iPartsVirtualNode(nodeType, productId);
        return createVirtualNode(lfdNumber, virtualNode);
    }

    private EtkDataPartListEntry createSeriesNode(int lfdNumber, iPartsSeriesId seriesId, iPartsNodeType nodeType) {
        iPartsVirtualNode virtualNode = new iPartsVirtualNode(nodeType, seriesId);
        return createVirtualNode(lfdNumber, virtualNode);
    }

    private EtkDataPartListEntry createModelNode(int lfdNumber, iPartsModelId modelId, iPartsNodeType nodeType) {
        iPartsVirtualNode virtualNode = new iPartsVirtualNode(nodeType, modelId);
        return createVirtualNode(lfdNumber, virtualNode);
    }

    private static boolean isStructureAssemblyId(AssemblyId id) {
        iPartsAssemblyId optionalIPartsAssemblyId = new iPartsAssemblyId(id.getKVari(), id.getKVer());
        if (optionalIPartsAssemblyId.isVirtual()) {
            List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(optionalIPartsAssemblyId);
            for (iPartsVirtualNode virtualNode : virtualNodes) {
                if (virtualNode.getType().isStructureType()) {
                    // Der Knoten ist Structure, dann kann auch was aus dem Strukturknoten gefunden werden
                    return true;
                }
            }
        }
        return false;
    }


    public static iPartsSearchVirtualDataset searchPartListEntriesForStructure(final AssemblyId optionalRootAssemblyId,
                                                                               final EtkDisplayFields selectFields,
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
                if (!(optionalRootAssemblyId.isRootNode() || isStructureAssemblyId(optionalRootAssemblyId))) {
                    // Der Suchknoten ist nicht der Root und auch kein Strukturknoten -> hier findet man keinen Strukturknoten mehr
                    return new ArrayList<EtkDataPartListEntry>();
                }

                // Hier kann was gefunden werden, erzeuge einfach alle Unterknoten der Struktur und liefere alle Entries zurück
                // die eigentliche Suche findet dann im PostProzess statt
                // Die Struktur ist nicht wirklich groß und kommt aus dem Cache,
                // aus dem Grund können wir das hier problemlos machen
                List<EtkDataPartListEntry> result = new ArrayList<EtkDataPartListEntry>();
                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, optionalRootAssemblyId);

                searchAllChildStructureEntries(selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings,
                                               project, result, assembly);

                return result;
            }
        };
    }

    private static void searchAllChildStructureEntries(EtkDisplayFields selectFields,
                                                       List<String> selectValues,
                                                       EtkDisplayFields whereFields,
                                                       List<String> andOrWhereValues,
                                                       boolean andOrSearch,
                                                       WildCardSettings wildCardSettings,
                                                       EtkProject project, List<EtkDataPartListEntry> result, EtkDataAssembly assembly) throws CanceledException {
        // über alle gefilterten Strukturknoten iterieren (Filterung ist hier schon sinnvoll wegen rekursivem Aufruf)
        for (EtkDataPartListEntry entry : assembly.getSubAssemblyEntries(true, null)) {
            if (Session.currentSessionThreadAppActionCancelled()) {
                throw new CanceledException(null);
            }


            Map<String, String> fieldsAndValues = new HashMap<String, String>();

            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR), entry.getPart().getFieldValue(FIELD_M_BESTNR, project.getDBLanguage(), true));
            fieldsAndValues.put(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR), entry.getPart().getFieldValue(FIELD_M_TEXTNR, project.getDBLanguage(), true));

            if (SearchMatchHelper.fieldsMatchSearchOptions(fieldsAndValues, andOrSearch, selectFields, selectValues, whereFields,
                                                           andOrWhereValues, wildCardSettings)) {
                result.add(entry);
            }

            if (isStructureAssemblyId(entry.getDestinationAssemblyId())) {
                EtkDataAssembly child = EtkDataObjectFactory.createDataAssembly(project, entry.getDestinationAssemblyId());

                searchAllChildStructureEntries(selectFields, selectValues, whereFields, andOrWhereValues, andOrSearch, wildCardSettings,
                                               project, result, child);
            }
        }
    }
}
