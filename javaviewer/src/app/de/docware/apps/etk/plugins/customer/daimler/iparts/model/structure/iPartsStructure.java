/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCTTHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMBSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.util.AbstractCacheWithChangeSets;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.SortType;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceMap;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Singleton Object pro Projekt für die ganze Struktur (Tabelle TABLE_DA_STRUCTURE).
 */
public class iPartsStructure implements EtkDbConst, iPartsConst {

    // Lebensdauer für die ChangeSets ist iPartsPlugin.getCachesLifeTime(); Lebensdauer im Cache aber MAX_CACHE_LIFE_TIME_CORE
    private static AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsStructure>> cacheWithChangeSets =
            new AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsStructure>>(MAX_CACHE_SIZE_CHANGE_SETS, iPartsPlugin.getCachesLifeTime()) {
                @Override
                protected ObjectInstanceStrongLRUList<Object, iPartsStructure> createNewCache() {
                    return new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE, MAX_CACHE_LIFE_TIME_CORE);
                }
            };

    private static ObjectInstanceMap<Object, Map<iPartsStructureId, iPartsStructureNode>> constructionNodesCache = new ObjectInstanceMap<>();

    // Knoten der ersten Ebene
    private iPartsStructureNode rootNode;

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        synchronized (cacheWithChangeSets) {
            cacheWithChangeSets.clearCache();
        }
        constructionNodesCache.clear();
    }

    /**
     * Entfernt den Cache-Eintrag für die gerade aktiven {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s.
     *
     * @param project
     */
    public static void removeCacheForActiveChangeSets(EtkProject project) {
        synchronized (cacheWithChangeSets) {
            cacheWithChangeSets.removeCacheForActiveChangeSets(project);
        }
    }

    /**
     * Ersetzt den Cache, der mit {@code destinationCacheKey} referenziert wurde, durch den Cache der aktuell aktiven
     * {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s ohne den Cache dort zu entfernen. Wird z.B. verwendet,
     * um vor Edit-Aktionen den Cache als reine Referenz vollständig für den {@code destinationCacheKey} zu übernehmen.
     *
     * @param project
     * @param destinationCacheKey
     */
    public static void useCacheForActiveChangeSets(EtkProject project, String destinationCacheKey) {
        synchronized (cacheWithChangeSets) {
            cacheWithChangeSets.useActiveChangeSetCache(project, destinationCacheKey);
        }
    }

    public static synchronized iPartsStructure getInstance(EtkProject project) {
        return getInstance(project, false);
    }

    private static synchronized iPartsStructure getInstance(final EtkProject project, boolean forceNormalCache) {
        ObjectInstanceStrongLRUList<Object, iPartsStructure> cache;
        ObjectInstanceStrongLRUList<Object, iPartsStructure> normalCache;
        synchronized (cacheWithChangeSets) {
            normalCache = cacheWithChangeSets.getNormalCache();
            if (forceNormalCache) {
                cache = normalCache;
            } else {
                cache = cacheWithChangeSets.getCacheInstance(project);
            }
        }
        final boolean isNormalCache = cache == normalCache;

        // Hier nun keine ChangeSets mehr verwenden für das hashObject, weil dies ja bereits über cacheWithChangeSets
        // gelöst wurde
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsStructure.class, "Structure", false);
        final VarParam<iPartsStructure> result = new VarParam<>(cache.get(hashObject));

        if (result.getValue() == null) {
            // Noch nicht geladen -> lade aus der Datenbank, wobei der normale Cache ohne Aggregate in den Produkten wegen
            // den Konstruktionsknoten unbedingt zuerst geladen worden sein muss
            if (!isNormalCache) {
                getInstance(project, true);
            }

            if (forceNormalCache && project.getEtkDbs().isRevisionChangeSetActive()) {
                project.executeWithoutActiveChangeSets((runSession, runProject) -> {
                    Object runHashObject = CacheHelper.getDBCacheIdentifier(runProject.getEtkDbs(), iPartsStructure.class,
                                                                            "Structure", false);
                    if (!Utils.objectEquals(hashObject, runHashObject)) {
                        Logger.getLogger().log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "EtkProject DB connection has changed while creating normal iPartsStructure cache");
                    }

                    // Zunächst alle Produkte am Stück laden, was deutlich schneller geht als jedes Produkt einzeln
                    iPartsProduct.getAllProducts(runProject);
                    result.setValue(new iPartsStructure(runProject, true));
                    return true;
                });
            } else {
                // Zunächst alle Produkte am Stück laden, was deutlich schneller geht als jedes Produkt einzeln
                iPartsProduct.getAllProducts(project);
                result.setValue(new iPartsStructure(project, isNormalCache));
            }

            cache.put(hashObject, result.getValue());
        }

        return result.getValue();
    }

    private iPartsStructure(EtkProject project, boolean isStructureForNormalCache) {
        rootNode = new iPartsStructureNode(new iPartsStructureId(project.getProjectId()));
        Map<iPartsStructureId, iPartsStructureNode> nodesCache = new HashMap<>();
        loadStructure(project, nodesCache, isStructureForNormalCache);
        loadProducts(project, nodesCache);
    }

    /**
     * Die Strukturhierarchie laden
     *
     * @param project
     * @param nodesCache
     * @param isStructureForNormalCache
     */
    private void loadStructure(EtkProject project, Map<iPartsStructureId, iPartsStructureNode> nodesCache, boolean isStructureForNormalCache) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsStructure.class, "constructionNodes", false);
        Map<iPartsStructureId, iPartsStructureNode> constructionNodes = constructionNodesCache.get(hashObject);
        if (constructionNodes == null) {
            constructionNodes = new HashMap<>();
            constructionNodesCache.put(hashObject, constructionNodes);
        }

        Set<iPartsSeriesId> seriesIds = null;
        Set<iPartsModelId> edsModelIds = null;
        Set<iPartsModelId> mbsModelIds = null;
        Set<iPartsModelId> cttModelIds = null;

        // Die Liste der Strukturhierarchie-Knoten auf einen Schlag laden und aus dem Tabelleninhalt die Knoten erzeugen
        DBDataObjectAttributesList attributesList = project.getDbLayer().getAttributesList(TABLE_DA_STRUCTURE);
        attributesList.sort(new String[]{ FIELD_DS_PARENT, FIELD_DS_SORT },
                            new SortType[]{ SortType.AUTOMATIC, SortType.AUTOMATIC });
        iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
        for (DBDataObjectAttributes attributes : attributesList) {
            String parent = attributes.getField(FIELD_DS_PARENT).getAsString();
            String child = attributes.getField(FIELD_DS_CHILD).getAsString();
            String title = attributes.getField(FIELD_DS_TITLE).getAsString();
            String pictureName = attributes.getField(FIELD_DS_PICTURE).getAsString();
            String constructionTypeString = attributes.getField(FIELD_DS_CONSTRUCTION).getAsString();
            String modelTypePrefix = attributes.getField(FIELD_DS_MODEL_TYPE_PREFIX).getAsString();
            Set<String> productClasses = new LinkedHashSet<>(SetOfEnumDataType.parseSetofEnum(attributes.getField(FIELD_DS_ASPRODUCT_CLASSES).getAsString(),
                                                                                              false, false));
            String aggregateType = attributes.getField(FIELD_DS_AGGREGATE_TYPE).getAsString();
            STRUCTURE_CONSTRUCTION_TYPE constructionType = STRUCTURE_CONSTRUCTION_TYPE.NONE;
            if (!constructionTypeString.isEmpty()) {
                try {
                    constructionType = STRUCTURE_CONSTRUCTION_TYPE.valueOf(constructionTypeString);
                } catch (IllegalArgumentException e) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Wrong enum value for DA_STRUCTURE.DS_CONSTRUCTION (StructureId: "
                                                                              + child + "): " + constructionTypeString);
                    constructionType = STRUCTURE_CONSTRUCTION_TYPE.NONE;
                }
            }

            iPartsStructureNode parentNode;
            if (parent.isEmpty()) {
                parentNode = rootNode;
            } else {
                parentNode = getOrCreateStructureNode(rootNode, new iPartsStructureId(parent), true, nodesCache);
            }

            iPartsStructureId childStructureId = new iPartsStructureId(child);
            iPartsStructureNode childNode = getOrCreateStructureNode(rootNode, childStructureId, false, nodesCache,
                                                                     constructionNodes, constructionType);
            if (childNode != null) {
                // Falls der neu eingefügte Kindknoten schon irgendwo im Hauptknoten vorhanden ist, existiert er schon
                // In diesem Fall muss der gefundenen Kindknoten an den richtigen Parentknoten gehängt werden
                // Das kommt vor, wenn der Kindknoten vor dem Parentknoten eingeladen wurde

                // Ist ein Parentknoten vorhanden, in welchem der Kindknoten aktuell hängt?
                iPartsStructureNode existingParent = childNode.getParentNode();
                if ((existingParent != null) && (existingParent != parentNode)) {
                    // Aus diesem Knoten entfernen und in den neuen einhängen
                    existingParent.removeNode(childNode);
                    parentNode.addChild(childNode);
                }
            } else {
                // Kindknoten existiert noch nicht -> erzeugen
                childNode = getOrCreateStructureNode(parentNode, childStructureId, true, nodesCache, constructionNodes,
                                                     constructionType);
            }

            // Die Konstruktionsknoten nur einmal für die Struktur vom normalen Cache ohne ChangeSets erzeugen
            if (!isStructureForNormalCache && (constructionType != STRUCTURE_CONSTRUCTION_TYPE.NONE)) {
                continue;
            }

            childNode.setTitle(project.getEtkDbs().getMultiLanguageByTextNr(TableAndFieldName.make(TABLE_DA_STRUCTURE, FIELD_DS_TITLE), title));
            childNode.setPictureName(pictureName);
            childNode.setConstructionType(constructionType);
            childNode.setModelTypePrefix(modelTypePrefix);
            childNode.setProductClasses(productClasses);
            childNode.setAggregateType(aggregateType);

            // Die Baureihen- und Baumuster-Knoten in der Konstruktion auf einen Schlag laden und aus dem Tabelleninhalt
            // die Knoten erzeugen
            if (constructionType == STRUCTURE_CONSTRUCTION_TYPE.DIALOG_SERIES) {
                if (seriesIds == null) {
                    // Es sollen laut DAIMLER-1125 nur die Baureihen angezeigt werden, zu denen es auch Stücklisten gibt.
                    // deshalb wird hier nicht die eigentliche Baureihentabelle (DA_SERIES) abgefragt, sondern per Distinct die Konstruktionsstückliste
                    // Sollte für einen gefundenen Datensatz keine Satz in der DA_SERIES vorhanden sein, dann bekommt der Knoten den Namen 'Baureihe nicht gefunden'

                    DBDataObjectAttributesList seriesAttributesList = project.getDbLayer().getAttributesList(TABLE_DA_DIALOG,
                                                                                                             new String[]{ FIELD_DD_SERIES_NO },
                                                                                                             null, null,
                                                                                                             null, null,
                                                                                                             ExtendedDataTypeLoadType.NONE, true);
                    seriesAttributesList.sort(new String[]{ FIELD_DD_SERIES_NO }, new SortType[]{ SortType.AUTOMATIC });
                    seriesIds = new LinkedHashSet<>(seriesAttributesList.size());
                    for (DBDataObjectAttributes seriesAttributes : seriesAttributesList) {
                        seriesIds.add(new iPartsSeriesId(seriesAttributes.getField(FIELD_DD_SERIES_NO).getAsString()));
                    }
                }
                addConstructionDialogSeriesNodes(childNode, seriesIds);
            } else if (constructionType == STRUCTURE_CONSTRUCTION_TYPE.EDS_MODEL) {
                if (edsModelIds == null) {
                    String tableName = structureHelper.getStructureTableName();
                    String modelField = structureHelper.getModelNumberField();
                    DBDataObjectAttributesList edsModelsAttributesList = project.getDbLayer().getAttributesList(tableName,
                                                                                                                new String[]{ modelField },
                                                                                                                null, null,
                                                                                                                null, null,
                                                                                                                ExtendedDataTypeLoadType.NONE, true);
                    edsModelsAttributesList.sort(new String[]{ modelField }, new SortType[]{ SortType.AUTOMATIC });
                    edsModelIds = new LinkedHashSet<>(edsModelsAttributesList.size());
                    for (DBDataObjectAttributes modelAttributes : edsModelsAttributesList) {
                        edsModelIds.add(new iPartsModelId(modelAttributes.getField(modelField).getAsString()));
                    }
                }
                addConstructionEdsModelNodes(childNode, edsModelIds, structureHelper);
            } else if (constructionType == STRUCTURE_CONSTRUCTION_TYPE.MBS_MODEL) {
                if (mbsModelIds == null) {
                    mbsModelIds = iPartsMBSHelper.getAllModels(project);
                }
                addConstructionMBSModelNodes(childNode, mbsModelIds);
            } else if (constructionType == STRUCTURE_CONSTRUCTION_TYPE.CTT_MODEL) {
                if (cttModelIds == null) {
                    cttModelIds = iPartsCTTHelper.getAllPossibleModels(project);
                }
                addConstructionCTTModelNodes(childNode, cttModelIds);
            }
        }
    }

    /**
     * Die Produkte unterhalb der Strukturknoten laden
     *
     * @param project
     * @param nodesCache
     */
    private void loadProducts(EtkProject project, Map<iPartsStructureId, iPartsStructureNode> nodesCache) {
        iPartsStructureId specialCatalogStructureId = new iPartsStructureId(STRUCT_SPECIAL_CAT);

        // Die Liste der Produkt-Knoten auf einen Schlag laden und aus dem Tabelleninhalt die Knoten erzeugen
        List<iPartsProduct> productList = iPartsProduct.getAllProducts(project);
        for (iPartsProduct product : productList) {
            boolean isCommonProduct = product.isCommonProduct(project);
            loadProduct(project, nodesCache, specialCatalogStructureId, product, isCommonProduct, false);

            // Fahrzeug-Produkte müssen sowohl einzeln als auch mit deren dazugemischten Aggregaten jeweils als Knoten
            // hinzugefügt werden
            if (!isCommonProduct) {
                loadProduct(project, nodesCache, specialCatalogStructureId, product, false, true);
            }
        }
    }

    private void loadProduct(EtkProject project, Map<iPartsStructureId, iPartsStructureNode> nodesCache, iPartsStructureId specialCatalogStructureId,
                             iPartsProduct product, boolean isCommonProduct, boolean isProductStructureWithAggregates) {
        PRODUCT_STRUCTURING_TYPE productStructuringType = product.getProductStructuringType();

        // Retailsicht mit Produkten
        iPartsNodeType nodeType = null;
        if (productStructuringType == PRODUCT_STRUCTURING_TYPE.EINPAS) {
            nodeType = iPartsNodeType.getProductEinPASType(isCommonProduct, isProductStructureWithAggregates);
        } else if (productStructuringType == PRODUCT_STRUCTURING_TYPE.KG_TU) {
            nodeType = iPartsNodeType.getProductKgTuType(isCommonProduct, isProductStructureWithAggregates);
        }

        if (nodeType != null) { // ohne gültigen Knotentyp wird das Produkt nirgends eingehangen
            if (product.isSpecialCatalog()) { // Spezialkatalog
                iPartsStructureNode parentNode = getOrCreateStructureNode(rootNode, specialCatalogStructureId, true, nodesCache);
                parentNode.addProduct(new iPartsStructureProductNode(product.getAsId(), nodeType));
            } else { // normales Produkt
                // Alle passenden Strukturknoten für das Produkt suchen und das Produkt dort unter dessen Typkennzahl(en) einhängen
                List<iPartsStructureNode> structureNodesForProduct = rootNode.findRetailNodesInAllChildren(product, project);
                if (!structureNodesForProduct.isEmpty()) {
                    for (iPartsStructureNode parentNode : structureNodesForProduct) {
                        parentNode.setChildrenSortedById(true);

                        // Typkennzahl(en) bestimmen und Produkt dort einhängen
                        // Typkennzahl zunächst aus der referenzierten Baureihe ermitteln
                        iPartsSeriesId seriesId = product.getReferencedSeries();
                        if ((seriesId != null) && !seriesId.getSeriesNumber().isEmpty()) {
                            iPartsStructureNode modelTypeNode = getOrCreateStructureNode(parentNode,
                                                                                         new iPartsStructureId(parentNode.getId().getStructureName()
                                                                                                               + "_" + seriesId.getSeriesNumber()),
                                                                                         true, nodesCache);
                            modelTypeNode.setTitle(new EtkMultiSprache(seriesId.getSeriesNumber(), project.getConfig().getDatabaseLanguages()));
                            modelTypeNode.addProduct(new iPartsStructureProductNode(product.getAsId(), nodeType));
                            continue;
                        }

                        // Fallback auf Typkennzahl(en) aus den Baumustern ermitteln
                        Set<String> modelTypes = product.getAllModelTypes(project);
                        if (!modelTypes.isEmpty()) {
                            for (String modelType : modelTypes) {
                                iPartsStructureNode modelTypeNode = getOrCreateStructureNode(parentNode,
                                                                                             new iPartsStructureId(parentNode.getId().getStructureName()
                                                                                                                   + "_" + modelType),
                                                                                             true, nodesCache);
                                modelTypeNode.setTitle(new EtkMultiSprache(modelType, project.getConfig().getDatabaseLanguages()));
                                modelTypeNode.addProduct(new iPartsStructureProductNode(product.getAsId(), nodeType));
                            }
                        } else {
                            // Weder eine referenzierte Baureihe noch Baumuster gefunden -> erzeuge einen Strukturknoten
                            // für "fehlende Typkennzahl" für das Produkt
                            iPartsStructureNode missingModelTypeNode = getOrCreateStructureNode(parentNode,
                                                                                                new iPartsStructureId(parentNode.getId().getStructureName()
                                                                                                                      + "_missingModelType"),
                                                                                                true, nodesCache);
                            missingModelTypeNode.setTitle(new EtkMultiSprache("!!Fehlende Typkennzahl", project.getConfig().getDatabaseLanguages()));
                            missingModelTypeNode.setMissingNode(true);
                            missingModelTypeNode.setChildrenSortedById(true);
                            missingModelTypeNode.addProduct(new iPartsStructureProductNode(product.getAsId(), nodeType));
                        }
                    }
                } else { // Fehlender Strukturknoten für die Daten des Produkts -> erzeuge einen "fehlenden Strukturknoten"
                    String modelTypePrefix = product.getModelTypePrefix(project);
                    Set<String> productClasses = product.getAsProductClasses();
                    String aggregateType = product.getAggregateType();

                    // Hat das Produkt überhaupt Module? (bei Import von der Applikationsliste wäre das Produkt leer)?
                    iPartsProductStructures productStructures = iPartsProductStructures.getInstance(project, product.getAsId());
                    if (productStructures.hasModules(project)) {
                        String structureNodeParams = modelTypePrefix + " - " + StrUtils.stringListToString(productClasses, ", ")
                                                     + " - " + aggregateType;
                        iPartsStructureId parentStructureId = new iPartsStructureId(structureNodeParams);

                        iPartsStructureNode missingStructureNode = getOrCreateStructureNode(rootNode, parentStructureId,
                                                                                            true, nodesCache);
                        missingStructureNode.setTitle(new EtkMultiSprache("!!Fehlender Strukturknoten: %1", project.getConfig().getDatabaseLanguages(),
                                                                          parentStructureId.getStructureName()));
                        missingStructureNode.setMissingNode(true);
                        missingStructureNode.setChildrenSortedById(true);
                        missingStructureNode.addProduct(new iPartsStructureProductNode(product.getAsId(), nodeType));
                    }
                }
            }
        }
    }

    private iPartsStructureNode getOrCreateStructureNode(iPartsStructureNode parentNode, iPartsStructureId structureId,
                                                         boolean createIfNotExists, Map<iPartsStructureId, iPartsStructureNode> nodesCache,
                                                         Map<iPartsStructureId, iPartsStructureNode> constructionNodes,
                                                         STRUCTURE_CONSTRUCTION_TYPE constructionType) {
        iPartsStructureNode node = nodesCache.get(structureId);
        if (node == null) {
            if (createIfNotExists) {
                // Neu zu erzeugende Knoten zunächst in dem zentralen Cache für die Konstruktionsknoten suchen (hier ist
                // keine Synchronisierung mehr notwendig, weil diese Methode sowieso nur innerhalb von synchronisierten
                // Methoden aufgerufen wird)
                if (constructionNodes != null) {
                    node = constructionNodes.get(structureId);
                }
                if (node != null) {
                    // Konstruktionsknoten als Kindknoten registrieren OHNE den Vaterknoten zu setzen (es kann nur einen
                    // Vaterknoten geben und das ist in der normalen Struktur ohne ChangeSets)
                    parentNode.addChild(node, false);
                } else {
                    // Knoten neu erzeugen
                    node = parentNode.getOrCreateChild(structureId);

                    // Falls es sich um einen Konstruktionsknoten handelt (und der Vater-Knoten kein Konstruktionsknoten
                    // ist), den Knoten in den Cache für die Konstruktionsknoten legen -> es landen nur die jeweils obersten
                    // Konstruktionsknoten im Cache (aktuell ist das bei iParts nur der Knoten "Konstruktion")
                    if ((constructionNodes != null) && (node != null) && (constructionType != STRUCTURE_CONSTRUCTION_TYPE.NONE)
                        && !parentNode.isConstructionNode()) {
                        constructionNodes.put(structureId, node);
                    }
                }
            } else {
                node = parentNode.findNode(structureId);
            }
            if (node != null) {
                nodesCache.put(structureId, node);
            }
        }
        return node;
    }

    private iPartsStructureNode getOrCreateStructureNode(iPartsStructureNode parentNode, iPartsStructureId structureId,
                                                         boolean createIfNotExists, Map<iPartsStructureId, iPartsStructureNode> nodesCache) {
        return getOrCreateStructureNode(parentNode, structureId, createIfNotExists, nodesCache, null, STRUCTURE_CONSTRUCTION_TYPE.NONE);
    }

    /**
     * Alle DIALOG-Baureihen unterhalb des angegebenen Konstruktions-Strukturknotens hinzufügen
     *
     * @param constructionNode
     * @param seriesIds
     */
    private void addConstructionDialogSeriesNodes(iPartsStructureNode constructionNode, Set<iPartsSeriesId> seriesIds) {
        for (iPartsSeriesId seriesId : seriesIds) {
            // Konstruktionsknoten für DIALOG-Baureihen ist gewünscht -> HM/M/SM und EinPAS einfügen falls Baureihentyp passt
            if (seriesId.getSeriesNumber().startsWith(constructionNode.getModelTypePrefix())) {
                constructionNode.addSeries(new iPartsStructureSeriesNode(seriesId, iPartsNodeType.DIALOG_HMMSM));
                constructionNode.addSeries(new iPartsStructureSeriesNode(seriesId, iPartsNodeType.DIALOG_EINPAS));
            }
        }
    }

    /**
     * Alle EDS-Baumuster unterhalb des angegebenen Konstruktions-Strukturknotens hinzufügen
     *
     * @param constructionNode
     * @param modelIds
     * @param structureHelper
     */
    private void addConstructionEdsModelNodes(iPartsStructureNode constructionNode, Set<iPartsModelId> modelIds,
                                              iPartsEdsStructureHelper structureHelper) {
        for (iPartsModelId modelId : modelIds) {
            // Konstruktionsknoten für EDS-Baumuster gewünscht -> OPS und EinPAS einfügen falls Baumustertyp passt
            if (modelId.getModelNumber().startsWith(constructionNode.getModelTypePrefix())) {
                constructionNode.addModel(structureHelper.createStructureNode(modelId));
                constructionNode.addModel(new iPartsStructureModelNode(modelId, iPartsNodeType.EDS_EINPAS));
            }
        }
    }

    /**
     * Alle MBS-Baumuster unterhalb des angegebenen Konstruktions-Strukturknotens hinzufügen
     *
     * @param constructionNode
     * @param modelIds
     */
    private void addConstructionMBSModelNodes(iPartsStructureNode constructionNode, Set<iPartsModelId> modelIds) {
        for (iPartsModelId modelId : modelIds) {
            if (modelId.getModelNumber().startsWith(constructionNode.getModelTypePrefix())) {
                constructionNode.addModel(new iPartsStructureModelNode(modelId, iPartsNodeType.MBS_STRUCTURE));
            }
        }
    }

    /**
     * Alle CTT-Baumuster unterhalb des angegebenen Konstruktions-Strukturknotens hinzufügen
     *
     * @param constructionNode
     * @param modelIds
     */
    private void addConstructionCTTModelNodes(iPartsStructureNode constructionNode, Set<iPartsModelId> modelIds) {
        for (iPartsModelId modelId : modelIds) {
            if (modelId.getModelNumber().startsWith(constructionNode.getModelTypePrefix())) {
                constructionNode.addModel(new iPartsStructureModelNode(modelId, iPartsNodeType.CTT_MODEL));
            }
        }
    }


    public Collection<iPartsStructureNode> getChildren() {
        return rootNode.getChildren();
    }

    /**
     * Liefert mit Hilfe einer Tiefensuche eine Liste mit allen Strukturknoten.
     *
     * @return
     */
    public Collection<iPartsStructureNode> getChildrenRecursively() {
        return rootNode.getChildrenRecursively(null);
    }

    public iPartsStructureNode findNodeInAllChilds(iPartsStructureId nodeId) {
        return rootNode.findNodeInAllChilds(nodeId);
    }

    public Collection<iPartsStructureProductNode> getProductList() {
        return rootNode.getProductList();
    }

    public Collection<iPartsStructureSeriesNode> getSeriesList() {
        return rootNode.getSeriesList();
    }

    public Collection<iPartsStructureModelNode> getModelList() {
        return rootNode.getModelList();
    }

    public iPartsStructureNode getRootNode() {
        return rootNode;
    }


}