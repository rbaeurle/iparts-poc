/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Ein Strukturknoten mit den Bezeichnungen.
 */
public class iPartsStructureNode implements iPartsConst {

    // Name in der Datenbank für diese Struktur (leer für oberste Ebene)
    protected iPartsStructureId id;

    // Produkte, Baureihen und Baumuster unter diesem Knoten
    protected List<iPartsStructureProductNode> productList = new ArrayList<iPartsStructureProductNode>();
    protected List<iPartsStructureSeriesNode> seriesList = new ArrayList<iPartsStructureSeriesNode>();
    protected List<iPartsStructureModelNode> modelList = new ArrayList<iPartsStructureModelNode>();

    protected EtkMultiSprache title;
    protected String pictureName;
    protected STRUCTURE_CONSTRUCTION_TYPE constructionType = STRUCTURE_CONSTRUCTION_TYPE.NONE;
    protected String modelTypePrefix;
    protected Set<String> productClasses;
    protected String aggregateType;

    protected iPartsStructureNode parentNode;
    protected boolean isMissingNode;

    protected boolean childrenSortedById;
    protected Map<iPartsStructureId, iPartsStructureNode> children;

    public iPartsStructureNode(iPartsStructureId id) {
        this.id = id;
        this.title = new EtkMultiSprache();
        pictureName = "";

        // Die Sortierung soll initial basierend auf der Reihenfolge beim Hinzufügen bleiben sofern nicht setChildrenSortedById(true) aufgerufen wird
        children = new LinkedHashMap<iPartsStructureId, iPartsStructureNode>();
    }

    /**
     * Flag, ob alle Kind-Knoten basierend auf ihren {@link iPartsStructureId}s sortiert werden sollen.
     *
     * @return
     */
    public boolean isChildrenSortedById() {
        return childrenSortedById;
    }

    /**
     * Flag, ob alle Kind-Knoten basierend auf ihren {@link iPartsStructureId}s sortiert werden sollen.
     *
     * @param childrenSortedById
     */
    public void setChildrenSortedById(boolean childrenSortedById) {
        if (this.childrenSortedById == childrenSortedById) {
            return;
        }

        if (childrenSortedById) {
            // Kind-Knoten basierend auf ihrer ID in einer TreeMap sortieren
            Map sortedChildren = new TreeMap<iPartsStructureId, iPartsStructureNode>(new Comparator<iPartsStructureId>() {
                @Override
                public int compare(iPartsStructureId o1, iPartsStructureId o2) {
                    return o1.getStructureName().compareTo(o2.getStructureName());
                }
            });
            sortedChildren.putAll(children);
            children = sortedChildren;
        } else {
            // Kindknoten ab sofort ohne Sortierung aber mit garantierter Reihenfolge in einer LinkedHashMap ablegen
            children = new LinkedHashMap<>(children);
        }
    }

    /**
     * Liefert alle AS-Produktklassen von diesem Knoten inkl. aller Vater-Knoten zurück.
     *
     * @return {@code null} falls es gar keine AS-Produktklassen gibt
     */
    public Set<String> getProductClassesIncludingParentNodes() {
        if (parentNode == null) {
            return productClasses;
        }

        Set<String> parentNodeProductClasses = parentNode.getProductClassesIncludingParentNodes();
        if (parentNodeProductClasses == null) {
            return productClasses;
        }

        if (productClasses != null) {
            Set<String> productClassesIncludingParentNodes = new HashSet<>(productClasses);
            productClassesIncludingParentNodes.addAll(parentNodeProductClasses);
            return productClassesIncludingParentNodes;
        } else {
            return parentNodeProductClasses;
        }
    }

    public iPartsStructureId getId() {
        return id;
    }

    public EtkMultiSprache getTitle() {
        return title;
    }

    public void setTitle(EtkMultiSprache title) {
        this.title = title;
    }

    public void setTitle(String language, String value) {
        title.setText(language, value);
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public boolean isConstructionNode() {
        return constructionType != STRUCTURE_CONSTRUCTION_TYPE.NONE;
    }

    public STRUCTURE_CONSTRUCTION_TYPE getConstructionType() {
        return constructionType;
    }

    public void setConstructionType(STRUCTURE_CONSTRUCTION_TYPE constructionType) {
        this.constructionType = constructionType;
    }

    public String getModelTypePrefix() {
        return modelTypePrefix;
    }

    public void setModelTypePrefix(String modelTypePrefix) {
        this.modelTypePrefix = modelTypePrefix;
    }

    public Set<String> getProductClasses() {
        return productClasses;
    }

    public void setProductClasses(Set<String> productClasses) {
        this.productClasses = productClasses;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public iPartsStructureNode getParentNode() {
        return parentNode;
    }

    protected void setParentNode(iPartsStructureNode parentNode) {
        this.parentNode = parentNode;
    }

    public boolean isMissingNode() {
        return isMissingNode;
    }

    public void setMissingNode(boolean isMissingNode) {
        this.isMissingNode = isMissingNode;
    }

    public Collection<iPartsStructureNode> getChildren() {
        return Collections.unmodifiableCollection(children.values());
    }

    /**
     * Liefert mit Hilfe einer Tiefensuche eine Liste mit allen Strukturknoten unterhalb des Teilbaums, der von diesem
     * Strukturknoten aufgespannt wird, inkl. dieses Strukturknotens selbst.
     *
     * @param childrenRecursively Kann mit einer Liste von {@link iPartsStructureNode}s vorbelegt werden oder {@code null} sein.
     * @return
     */
    public Collection<iPartsStructureNode> getChildrenRecursively(Collection<iPartsStructureNode> childrenRecursively) {
        if (childrenRecursively == null) {
            childrenRecursively = new ArrayList<iPartsStructureNode>();
        }
        if (children != null) {
            for (iPartsStructureNode child : children.values()) {
                child.getChildrenRecursively(childrenRecursively);
            }
        }
        childrenRecursively.add(this);
        return Collections.unmodifiableCollection(childrenRecursively);
    }

    public iPartsStructureNode findNode(iPartsStructureId nodeId) {
        iPartsStructureNode result = children.get(nodeId);
        if (result != null) {
            return result;
        } else {
            // In den childs suchen
            for (iPartsStructureNode child : children.values()) {
                result = child.findNode(nodeId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }


    public iPartsStructureNode getOrCreateChild(iPartsStructureId nodeId) {
        iPartsStructureNode result = findNode(nodeId);
        if (result == null) {
            result = new iPartsStructureNode(nodeId);
            addChild(result);
        }
        return result;
    }

    @Override
    public String toString() {
        return "iPartsStructureNode " + id;
    }

    public iPartsStructureNode getChild(iPartsStructureId nodeId) {
        return children.get(nodeId);
    }

    /**
     * Suche in allen Knoten und deren Kindern nach einem Knoten mit der angegebenen ID.
     *
     * @param nodeId
     * @return
     */
    public iPartsStructureNode findNodeInAllChilds(iPartsStructureId nodeId) {
        iPartsStructureNode result = getChild(nodeId);
        if (result != null) {
            return result;
        }

        for (iPartsStructureNode child : getChildren()) {
            result = child.findNodeInAllChilds(nodeId);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Suche in allen Retail-Knoten und deren Kindern nach passenden Knoten für das übergebene {@link iPartsProduct}.
     *
     * @param product
     * @param project
     * @return
     */
    public List<iPartsStructureNode> findRetailNodesInAllChildren(iPartsProduct product, EtkProject project) {
        String productModelTypePrefix = product.getModelTypePrefix(project);
        DwList<iPartsStructureNode> foundStructureNodes = new DwList<iPartsStructureNode>();
        if (StrUtils.isValid(productModelTypePrefix)) {
            findRetailNodesInAllChildren(product, productModelTypePrefix, foundStructureNodes);
        }
        return foundStructureNodes;
    }

    /**
     * Suche in allen Retail-Knoten und deren Kindern nach passenden Knoten für das übergebene {@link iPartsProduct}.
     *
     * @param product
     * @param productModelTypePrefix Vorberechneter Typkennzahl-Präfix vom übergebenen Produkt
     * @param foundStructureNodes    InOut-Liste für die gefundenen {@link iPartsStructureNode}s
     * @return
     */
    protected void findRetailNodesInAllChildren(iPartsProduct product, String productModelTypePrefix, List<iPartsStructureNode> foundStructureNodes) {
        if (isConstructionNode()) {
            return;
        }

        if (Utils.objectEquals(getModelTypePrefix(), productModelTypePrefix) && product.isOneProductClassValid(getProductClasses())
            && (StrUtils.isEmpty(getAggregateType()) || Utils.objectEquals(getAggregateType(), product.getAggregateType()))) {
            foundStructureNodes.add(this);
        }

        for (iPartsStructureNode child : getChildren()) {
            child.findRetailNodesInAllChildren(product, productModelTypePrefix, foundStructureNodes);
        }
    }

    /**
     * Liefert alle Vater-Knoten für das übergebene {@link iPartsProduct} in Retail-Knoten und deren Kindern zurück.
     *
     * @param product
     * @return
     */
    public List<iPartsStructureNode> getRetailParentNodesForProduct(iPartsProduct product) {
        DwList<iPartsStructureNode> parentStructureNodes = new DwList<iPartsStructureNode>();
        getRetailParentNodesForProduct(product, parentStructureNodes);
        return parentStructureNodes;
    }

    /**
     * Liefert alle Vater-Knoten für das übergebene {@link iPartsProduct} in Retail-Knoten und deren Kindern zurück.
     *
     * @param product
     * @param parentStructureNodes InOut-Liste für die gefundenen Vater-{@link iPartsStructureNode}s für das übergebene {@link iPartsProduct}
     * @return
     */
    protected void getRetailParentNodesForProduct(iPartsProduct product, List<iPartsStructureNode> parentStructureNodes) {
        if (isConstructionNode()) {
            return;
        }

        iPartsProductId productId = product.getAsId();
        for (iPartsStructureProductNode structureProductNode : productList) {
            if (structureProductNode.getProductId().equals(productId)) {
                parentStructureNodes.add(this);
                break;
            }
        }

        for (iPartsStructureNode child : getChildren()) {
            child.getRetailParentNodesForProduct(product, parentStructureNodes);
        }
    }

    public void addProduct(iPartsStructureProductNode product) {
        productList.add(product);
    }

    public void addSeries(iPartsStructureSeriesNode series) {
        seriesList.add(series);
    }

    public void addModel(iPartsStructureModelNode model) {
        modelList.add(model);
    }

    public Collection<iPartsStructureProductNode> getProductList() {
        return Collections.unmodifiableCollection(productList);
    }

    public Collection<iPartsStructureSeriesNode> getSeriesList() {
        return Collections.unmodifiableCollection(seriesList);
    }

    public Collection<iPartsStructureModelNode> getModelList() {
        return Collections.unmodifiableCollection(modelList);
    }


    public void removeNode(iPartsStructureNode node) {
        children.remove(node.getId());
        node.setParentNode(null);
    }

    public void addChild(iPartsStructureNode node, boolean setParentNode) {
        children.put(node.getId(), node);
        if (setParentNode) {
            node.setParentNode(this);
        }
    }

    public void addChild(iPartsStructureNode node) {
        addChild(node, true);
    }

    public boolean hasProduct() {
        return (productList.size() > 0);
    }

    public boolean hasSeries() {
        return (seriesList.size() > 0);
    }

    public boolean hasModel() {
        return (modelList.size() > 0);
    }

    /**
     * Liste aller Kinder und Kindeskinder über Breitensuche
     *
     * @return
     */
    public Collection<iPartsStructureNode> getAllSubNodes() {
        Collection<iPartsStructureNode> result = new ArrayList<iPartsStructureNode>();
        result.addAll(getChildren());

        for (iPartsStructureNode child : getChildren()) {
            result.addAll(child.getAllSubNodes());
        }
        return Collections.unmodifiableCollection(result);
    }

    /**
     * In diesem Knoten ist eine Retailstückliste
     *
     * @return
     */
    public boolean hasRetailPartList() {
        return !productList.isEmpty();
    }

    /**
     * In diesem Knoten ist eine Baureihe mit Dialog-Konstruktionsdaten
     *
     * @return
     */
    public boolean hasDialogPartList() {
        for (iPartsStructureSeriesNode seriesNode : seriesList) {
            if (seriesNode.getStructureType() == iPartsNodeType.DIALOG_EINPAS
                || seriesNode.getStructureType() == iPartsNodeType.DIALOG_HMMSM) {
                return true;
            }
        }
        return false;
    }

    /**
     * In diesem Knoten ist ein Baumuster mit EDS-Konstruktionsdaten
     *
     * @return
     */
    public boolean hasEdsPartList() {
        for (iPartsStructureModelNode modelNode : modelList) {
            if ((modelNode.getStructureType() == iPartsNodeType.EDS_EINPAS)
                || iPartsEdsStructureHelper.isEdsModelStructureNode(modelNode.getStructureType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * In diesem Knoten ist ein Baumuster mit CTT-Konstruktionsdaten
     *
     * @return
     */
    public boolean hasCTTPartList() {
        for (iPartsStructureModelNode modelNode : modelList) {
            if (modelNode.getStructureType() == iPartsNodeType.CTT_MODEL) {
                return true;
            }
        }
        return false;
    }

    /**
     * In diesem Knoten ist ein Baumuster mit MBS-Konstruktionsdaten
     *
     * @return
     */
    public boolean hasMBSPartList() {
        for (iPartsStructureModelNode modelNode : modelList) {
            if (modelNode.getStructureType() == iPartsNodeType.MBS_STRUCTURE) {
                return true;
            }
        }
        return false;
    }
}
