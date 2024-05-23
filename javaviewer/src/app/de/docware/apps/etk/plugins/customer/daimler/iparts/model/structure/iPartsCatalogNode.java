/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure;

import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage.ModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Strukturobjekt für den Katalog. Als Element wird eine Id verwendet. Die Id kann z.B. eine {@link EinPasId}
 * sein oder eine {@link de.docware.apps.etk.base.project.mechanic.ids.AssemblyId} oder sonst eine {@link IdWithType}.
 */
public class iPartsCatalogNode {

    public static final Comparator<IdWithType> COMPARATOR_FOR_TU_SA_SORT = new Comparator<IdWithType>() {
        @Override
        public int compare(IdWithType o1, IdWithType o2) {
            if (o1.getType().equals(o2.getType())) { // bei gleichem ID-Typ normalen Vergleich durchführen
                return o1.compareTo(o2);
            }

            if (o1 instanceof KgSaId) { // o1 ist SA (o2 aber nicht) -> o1 nach o2 einsortieren
                return 1;
            }

            if (o2 instanceof KgSaId) { // o2 ist SA (o1 aber nicht) -> o2 nach o1 einsortieren
                return -1;
            }

            return o1.compareTo(o2); // Fallback falls weder o1 noch o2 KgSaId ist
        }
    };

    protected IdWithType id;
    protected Map<IdWithType, iPartsCatalogNode> children;
    private iPartsProductId productId;

    public static iPartsCatalogNode getOrCreateHmMSmNode(iPartsCatalogNode parentNode, HmMSmId hmMSmId) {
        // Erst die HM/M/SM-Struktur erstellen oder holen
        iPartsCatalogNode HmNode = parentNode.getOrCreateChild(new HmMSmId(hmMSmId.getSeries(), hmMSmId.getHm(), "", ""), true);
        iPartsCatalogNode MNode = HmNode.getOrCreateChild(new HmMSmId(hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), ""), true);
        iPartsCatalogNode SmNode = MNode.getOrCreateChild(hmMSmId, false);

        return SmNode;
    }

    public static iPartsCatalogNode getOrCreateKgTuNode(iPartsCatalogNode parentNode, KgTuId kgTuId) {
        // Erst die KG/TU-Struktur erstellen oder holen
        iPartsCatalogNode kgNode = parentNode.getOrCreateChild(new KgTuId(kgTuId.getKg(), ""), COMPARATOR_FOR_TU_SA_SORT);
        iPartsCatalogNode tuNode = kgNode.getOrCreateChild(kgTuId, false);

        return tuNode;
    }

    public static iPartsCatalogNode getOrCreateKgSaNode(iPartsCatalogNode parentNode, KgSaId kgSaId) {
        // Erst die KG der KG/TU-Struktur erstellen oder holen
        iPartsCatalogNode kgNode = parentNode.getOrCreateChild(new KgTuId(kgSaId.getKg(), ""), COMPARATOR_FOR_TU_SA_SORT);
        iPartsCatalogNode saNode = kgNode.getOrCreateChild(kgSaId, false);

        return saNode;
    }

    public static iPartsCatalogNode getOrCreateEinPasNode(iPartsCatalogNode parentNode, EinPasId einPasId) {
        // Erst die EinPAS-Struktur erstellen oder holen
        iPartsCatalogNode einPasHgNode = parentNode.getOrCreateChild(new EinPasId(einPasId.getHg(), "", ""), true);
        iPartsCatalogNode einPasGNode = einPasHgNode.getOrCreateChild(new EinPasId(einPasId.getHg(), einPasId.getG(), ""), true);
        iPartsCatalogNode einPasTuNode = einPasGNode.getOrCreateChild(einPasId, false);

        return einPasTuNode;
    }

    public iPartsCatalogNode(IdWithType id) {
        this(id, false);
    }

    public iPartsCatalogNode(IdWithType id, boolean sorted) {
        this.id = id;
        if (sorted) {
            children = new TreeMap<IdWithType, iPartsCatalogNode>();
        } else {
            children = new LinkedHashMap<IdWithType, iPartsCatalogNode>();
        }
    }

    public iPartsCatalogNode(IdWithType id, Comparator<IdWithType> comparatorForSort) {
        this.id = id;
        children = new TreeMap<IdWithType, iPartsCatalogNode>(comparatorForSort);
    }

    public IdWithType getId() {
        return id;
    }

    public Collection<iPartsCatalogNode> getChildren() {
        return Collections.unmodifiableCollection(children.values());
    }

    public void addChild(iPartsCatalogNode node) {
        children.put(node.getId(), node);
    }

    public iPartsCatalogNode getChild(IdWithType id) {
        return children.get(id);
    }

    public iPartsCatalogNode getOrCreateChild(IdWithType id, boolean sorted) {
        iPartsCatalogNode node = children.get(id);
        if (node == null) {
            // Kein Knoten mit dieser Id gefunden -> erstelle einen neuen Knoten und füge ihn den Children hinzu
            node = new iPartsCatalogNode(id, sorted);
            addChild(node);
        }
        return node;
    }

    public iPartsCatalogNode getOrCreateChild(IdWithType id, Comparator<IdWithType> comparatorForSort) {
        iPartsCatalogNode node = children.get(id);
        if (node == null) {
            // Kein Knoten mit dieser Id gefunden -> erstelle einen neuen Knoten und füge ihn den Children hinzu
            node = new iPartsCatalogNode(id, comparatorForSort);
            addChild(node);
        }
        return node;
    }

    /**
     * Vereint zwei Strukturen. Dazu werden die {@link #children} zusammengeführt und doppelte vereinzelt.
     *
     * @param source
     * @parem sorted
     * @param childSourceProductId
     */
    public void mergeSubStructure(iPartsCatalogNode source, boolean sorted, iPartsProductId childSourceProductId) {
        for (iPartsCatalogNode childSource : source.getChildren()) {
            iPartsCatalogNode childDest = getOrCreateChild(childSource.getId(), sorted);
            // In der KG/SA-Kind-Ebene das Produkt am Knoten speichern. Somit wissen wir später von
            // welchem Aggregate-Produkt es kam.
            if (childSource.isKgSaId()) {
                childDest.setProductId(childSourceProductId);
            }
            childDest.mergeSubStructure(childSource, sorted, childSourceProductId);
        }
    }

    @Override
    public String toString() {
        return formatToString("");
    }

    private String formatToString(String prefix) {
        StringBuilder result = new StringBuilder(prefix);
        result.append(getId().toString());
        result.append("\n");
        for (iPartsCatalogNode child : getChildren()) {
            result.append(child.formatToString(prefix + "  "));
        }
        return result.toString();
    }

    public iPartsCatalogNode getNode(EinPasId einPasId) {
        boolean isHgNode = einPasId.isHgNode();
        iPartsCatalogNode hgNode = getChild(isHgNode ? einPasId : new EinPasId(einPasId.getHg(), "", ""));

        if (hgNode != null) {
            if (isHgNode) {
                // es ist der HG-Knoten gesucht
                return hgNode;
            }

            boolean isGNode = einPasId.isGNode();
            iPartsCatalogNode gNode = hgNode.getChild(isGNode ? einPasId : new EinPasId(einPasId.getHg(), einPasId.getG(), ""));
            if (gNode != null) {
                if (isGNode) {
                    // es ist der G-Knoten gesucht
                    return gNode;
                }

                iPartsCatalogNode tuNode = gNode.getChild(einPasId);
                if (tuNode != null) {
                    return tuNode;
                }
            }
        }

        return null;
    }

    public iPartsCatalogNode getNode(OpsId opsId) {
        boolean isGroupNode = opsId.isGroupNode();
        iPartsCatalogNode groupNode = findInAllChildren(isGroupNode ? opsId : new OpsId(opsId.getGroup(), ""));

        if (groupNode != null) {
            if (isGroupNode) {
                // es ist der Group-Knoten gesucht
                return groupNode;
            }

            iPartsCatalogNode scopeNode = groupNode.getChild(opsId);
            if (scopeNode != null) {
                return scopeNode;
            }
        }

        return null;
    }

    public iPartsCatalogNode getNode(ModelElementUsageId modelElementUsageId) {
        boolean isModuleNode = modelElementUsageId.isModuleNode();
        iPartsCatalogNode moduleNode = findInAllChildren(isModuleNode ? modelElementUsageId : new ModelElementUsageId(modelElementUsageId.getModule(), ""));

        if (moduleNode != null) {
            if (isModuleNode) {
                // es ist der Modul-Knoten gesucht
                return moduleNode;
            }

            iPartsCatalogNode subModuleNode = moduleNode.getChild(modelElementUsageId);
            if (subModuleNode != null) {
                return subModuleNode;
            }
        }

        return null;
    }

    public iPartsCatalogNode getNode(MBSStructureId structureId) {
        boolean isListNumberNode = structureId.isListNumberNode();
        iPartsCatalogNode listNumberNode = findInAllChildren(isListNumberNode ? structureId : new MBSStructureId(structureId.getListNumber(), ""));
        if (listNumberNode != null) {
            if (isListNumberNode) {
                // Es ist der SAA/GS Knoten
                return listNumberNode;
            }
            iPartsCatalogNode conGroupNode = listNumberNode.getChild(structureId);
            if (conGroupNode != null) {
                return conGroupNode;
            }
        }

        return null;
    }


    public iPartsCatalogNode getNode(HmMSmId hmMSmId) {
        boolean isHmNode = hmMSmId.isHmNode();
        iPartsCatalogNode hmNode = findInAllChildren(isHmNode ? hmMSmId : new HmMSmId(hmMSmId.getSeries(), hmMSmId.getHm(), "", ""));

        if (hmNode != null) {
            if (isHmNode) {
                // es ist der HM-Knoten gesucht
                return hmNode;
            }

            boolean isMNode = hmMSmId.isMNode();
            iPartsCatalogNode mNode = hmNode.getChild(isMNode ? hmMSmId : new HmMSmId(hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), ""));
            if (mNode != null) {
                if (isMNode) {
                    // es ist der M-Knoten gesucht
                    return mNode;
                }

                iPartsCatalogNode smNode = mNode.getChild(hmMSmId);
                if (smNode != null) {
                    return smNode;
                }
            }
        }

        return null;
    }


    public iPartsCatalogNode getNode(KgTuId kgTuId) {
        boolean isKgNode = kgTuId.isKgNode();
        iPartsCatalogNode kgNode = getChild(isKgNode ? kgTuId : new KgTuId(kgTuId.getKg(), ""));

        if (kgNode != null) {
            if (isKgNode) {
                // Es ist der KG-Knoten gesucht
                return kgNode;
            }

            iPartsCatalogNode tuNode = kgNode.getChild(kgTuId);
            if (tuNode != null) {
                // Es ist der TU-Knoten gesucht
                return tuNode;
            }
        }

        return null;
    }

    public iPartsCatalogNode getNode(KgSaId kgSaId) {
        iPartsCatalogNode kgNode = getChild(new KgTuId(kgSaId.getKg(), ""));

        if (kgNode != null) {
            iPartsCatalogNode saNode = kgNode.getChild(kgSaId);
            if (saNode != null) {
                return saNode;
            }
        }

        return null;
    }

    public boolean isEinPasId() {
        return isTypeOf(EinPasId.TYPE);
    }

    public boolean isOpsId() {
        return isTypeOf(OpsId.TYPE);
    }

    public boolean isModelElementUsageId() {
        return isTypeOf(ModelElementUsageId.TYPE);
    }

    public boolean isMBSStructureId() {
        return isTypeOf(MBSStructureId.TYPE);
    }

    public boolean isHmMSmId() {
        return isTypeOf(HmMSmId.TYPE);
    }

    public boolean isKgTuId() {
        return isTypeOf(KgTuId.TYPE);
    }

    public boolean isKgSaId() {
        return isTypeOf(KgSaId.TYPE);
    }

    public boolean isAssemblyId() {
        return isTypeOf(AssemblyId.TYPE);
    }

    public boolean isIPartsModelId() {
        return isTypeOf(iPartsModelId.TYPE);
    }


    private boolean isTypeOf(String type) {
        return getId().getType().equals(type);
    }

    /**
     * Einen Node in einem der Kinder oder Kindeskinder suchen
     *
     * @param childId
     * @return
     */
    private iPartsCatalogNode findInAllChildren(IdWithType childId) {
        iPartsCatalogNode result = getChild(childId);

        if (result == null) {
            for (iPartsCatalogNode child : children.values()) {
                result = child.findInAllChildren(childId);
                if (result != null) {
                    return result;
                }

            }
        }
        return result;
    }

    public iPartsProductId getProductId() {
        return productId;
    }

    public void setProductId(iPartsProductId productId) {
        this.productId = productId;
    }
}
