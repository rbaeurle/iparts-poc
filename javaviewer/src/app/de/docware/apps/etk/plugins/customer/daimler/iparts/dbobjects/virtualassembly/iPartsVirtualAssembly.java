/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ConstructionValidationDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureNode;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.Collection;
import java.util.List;

/**
 * Basisklasse für die verschiedenen virtuellen Assembly-Strukturen
 */
public abstract class iPartsVirtualAssembly implements iPartsConst, EtkDbConst {

    public static final EtkDisplayFields NEEDED_DISPLAY_FIELDS = new EtkDisplayFields();

    static {
        // Benötigte Felder aus der MAT-Tabelle
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_VER), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_REFSER), false, false));
        NEEDED_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_IMAGE_AVAILABLE), false, false));
    }

    public enum SubAssemblyState {DEFAULT, HAS_NEVER, HAS_ALWAYS}

    private EtkProject project;
    private iPartsAssemblyId assemblyId;
    private List<iPartsVirtualNode> virtualIds;

    public iPartsVirtualAssembly(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        this.project = project;
        this.assemblyId = assemblyId;
        this.virtualIds = virtualIds;
    }

    public abstract DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields);

    EtkProject getEtkProject() {
        return project;
    }

    protected EtkDataPartListEntry createVirtualNode(int lfdNumber, iPartsVirtualNode... virtualNodesPath) {
        // Attribute erzeugen und alle Felder mit Leer auffüllen
        DBDataObjectAttributes katAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_KATALOG);
        DBDataObjectAttributes matAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_MAT);

        String virtualKeyString = iPartsVirtualNode.getVirtualIdString(virtualNodesPath);

        katAttributes.addField(FIELD_K_VARI, getAsId().getKVari(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, getAsId().getKVer(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SACH, virtualKeyString, DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MATNR, virtualKeyString, DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MVER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_LFDNR, EtkDbsHelper.formatLfdNr(lfdNumber), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SEQNR, EtkDbsHelper.formatLfdNr(lfdNumber), DBActionOrigin.FROM_DB);

        katAttributes.addField(iPartsDataVirtualFieldsDefinition.STRUCT_PICTURE, iPartsDataVirtualFieldsHelper.getPictureNameFromVirtualKey(virtualKeyString, getEtkProject()), DBActionOrigin.FROM_DB);

        matAttributes.addField(FIELD_M_MATNR, virtualKeyString, DBActionOrigin.FROM_DB);
        matAttributes.addField(FIELD_M_VER, "", DBActionOrigin.FROM_DB);
        matAttributes.addField(FIELD_M_BESTNR, iPartsDataVirtualFieldsHelper.getOrderNumberFromVirtualKey(virtualKeyString, getEtkProject()), DBActionOrigin.FROM_DB);


        EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(), katAttributes);

        // Texte des neuen Eintrags
        EtkMultiSprache texts = iPartsDataVirtualFieldsHelper.getTextsFromVirtualKey(virtualKeyString, getEtkProject());
        if (texts != null) {
            DBDataObjectAttribute textAttribute = matAttributes.getField(FIELD_M_TEXTNR);
            if (textAttribute != null) {
                textAttribute.setValueAsMultiLanguage(texts, DBActionOrigin.FROM_DB);
            }
        }

        EtkDataPart partForPartListEntry = newPartListEntry.getPart();
        partForPartListEntry.setAttributes(matAttributes, DBActionOrigin.FROM_DB);

        if (newPartListEntry instanceof iPartsDataPartListEntry) {
            ((iPartsDataPartListEntry)newPartListEntry).setDataLoadedFlagsForVirtualPLE(true);
        }
        return newPartListEntry;
    }

    /**
     * Die ganz normalen Attribute des Kopfdatensatzes der Assembly
     *
     * @param resultFields
     * @return
     */
    public DBDataObjectAttributes loadAssemblyHeadAttributes(String[] resultFields) {
        DBDataObjectAttributes result = new DBDataObjectAttributes();

        // Aktuell sind bei der virtuellen Navigation nur diese Felder im Kopfdatensatz gesetzt
        result.addField(FIELD_K_VARI, getAsId().getKVari(), DBActionOrigin.FROM_DB);
        result.addField(FIELD_K_VER, getAsId().getKVer(), DBActionOrigin.FROM_DB);
        result.addField(FIELD_K_SACH, getAsId().getKVari(), DBActionOrigin.FROM_DB);
        result.addField(FIELD_K_VER, getAsId().getKVer(), DBActionOrigin.FROM_DB);
        result.addField(FIELD_K_MATNR, getAsId().getKVari(), DBActionOrigin.FROM_DB);
        result.addField(FIELD_K_MVER, getAsId().getKVer(), DBActionOrigin.FROM_DB);
        result.addField(FIELD_K_ART, EtkConfigConst.BAUGRUPPEKENN, DBActionOrigin.FROM_DB);
        result.addField(FIELD_K_EBENE, getPartsListType(), DBActionOrigin.FROM_DB);

        // Den Rest einfach Leer
        for (String fieldName : resultFields) {
            if (!result.fieldExists(fieldName)) {
                result.addField(fieldName, "", DBActionOrigin.FROM_DB);
            }
        }
        return result;
    }

    protected iPartsAssemblyId getAsId() {
        return assemblyId;
    }

    protected List<iPartsVirtualNode> getVirtualIds() {
        return virtualIds;
    }

    protected iPartsVirtualNode getLastVirtualNode() {
        if (!virtualIds.isEmpty()) {
            return virtualIds.get(virtualIds.size() - 1);
        }
        return null;
    }

    protected iPartsVirtualNode getRootNode() {
        if (!virtualIds.isEmpty()) {
            return virtualIds.get(0);
        }
        return null;
    }

    public SubAssemblyState getSubAssemblyState() {
        return SubAssemblyState.DEFAULT;
    }

    public String getOrderNumber() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        // Die obersten Strukturen werden in der Basisklasse behandelt
        if (lastNodeId instanceof iPartsProductId) { // Produkt
            return ((iPartsProductId)lastNodeId).getProductNumber();
        } else if (lastNodeId instanceof iPartsModelId) { // Baumuster
            return ((iPartsModelId)lastNodeId).getModelNumber();
        } else if (lastNodeId instanceof iPartsSeriesId) { // Baureihe
            return ((iPartsSeriesId)lastNodeId).getSeriesNumber();
        } else if (lastNodeId instanceof iPartsStructureId) { // Structure
            return ((iPartsStructureId)lastNodeId).getStructureName();
        }

        if (Constants.DEVELOPMENT) {
            throw new RuntimeException("getOrderNumber findet keine Klasse, die zuständig ist! " + lastNodeId.toString());
        }
        return "";
    }

    public String getPartsListType() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        // Die obersten Strukturen werden in der Basisklasse behandelt
        if (lastNodeId instanceof iPartsProductId) { // Produkt
            iPartsProduct product = iPartsProduct.getInstance(project, (iPartsProductId)lastNodeId);
            if (product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.EINPAS) {
                return PARTS_LIST_TYPE_PRODUCT_EINPAS;
            } else if (product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.KG_TU) {
                return PARTS_LIST_TYPE_PRODUCT_KGTU;
            } else {
                return PARTS_LIST_TYPE_PRODUCT;
            }
        } else if (lastNodeId instanceof iPartsModelId) { // Baumuster
            // Neben EDS/BCS, MBS hat auch CTT Baumusterknoten. Um die Konfiguration getrennt zu halten, muss hier unterschieden werden
            if (lastVirtualNode.getType() == iPartsNodeType.MBS_STRUCTURE) {
                return PARTS_LIST_TYPE_MODEL_MBS;
            } else if (lastVirtualNode.getType() == iPartsNodeType.CTT_MODEL) {
                return PARTS_LIST_TYPE_MODEL_CTT;
            } else {
                return PARTS_LIST_TYPE_MODEL;
            }
        } else if (lastNodeId instanceof iPartsSeriesId) { // Baureihe
            return PARTS_LIST_TYPE_SERIES;
        } else if (lastNodeId instanceof iPartsStructureId) { // Structure
            iPartsStructure productStructure = iPartsStructure.getInstance(project);
            iPartsStructureNode node = productStructure.findNodeInAllChilds((iPartsStructureId)lastNodeId);

            if (node == null) {
                return "";
            } else if (node.hasProduct()) { // Falls Knoten Produkt enthält
                return PARTS_LIST_TYPE_PRODUCT;
            } else if (node.hasModel()) { // Falls Knoten Baumuster enthält
                return PARTS_LIST_TYPE_STRUCTURE_MODEL;
            } else if (node.hasSeries()) { // Falls Knoten Baureihen enthält
                return PARTS_LIST_TYPE_STRUCTURE_SERIES;
            } else { // Normaler Strukturknoten
                return PARTS_LIST_TYPE_STRUCTURE;
            }
        }

        if (Constants.DEVELOPMENT) {
            throw new RuntimeException("getPartsListType findet keine Klasse, die zuständig ist! " + lastNodeId.toString());
        }
        return "";
    }

    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof iPartsStructureId) { // Structure
            iPartsStructure structure = iPartsStructure.getInstance(project);
            iPartsStructureNode node = structure.findNodeInAllChilds((iPartsStructureId)lastNodeId);
            if (node != null) {
                return node.getTitle();
            } else {
                return null;
            }
        }

        if (Constants.DEVELOPMENT) {
            throw new RuntimeException("getTexts findet keine Klasse, die zuständig ist! " + lastNodeId.toString());
        }
        return null;
    }

    public String getPictureName() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof iPartsModelId) { // Baumuster
            // Baumuster haben noch kein Bild
            return "";
        } else if (lastNodeId instanceof iPartsSeriesId) { // Baureihe
            // Baureihen haben noch kein Bild
            return "";
        } else if (lastNodeId instanceof iPartsProductId) { // Produkt
            iPartsProduct product = iPartsProduct.getInstance(project, (iPartsProductId)lastNodeId);
            if (product != null) {
                return product.getPictureName();
            }
            return "";
        } else if (lastNodeId instanceof iPartsStructureId) { // Structure
            iPartsStructure structure = iPartsStructure.getInstance(project);
            iPartsStructureNode node = structure.findNodeInAllChilds((iPartsStructureId)lastNodeId);
            if (node != null) {
                return node.getPictureName();
            }
            return "";
        }


        if (Constants.DEVELOPMENT) {
            throw new RuntimeException("getPictureName findet keine Klasse, die zuständig ist! " + lastNodeId.toString());
        }
        return "";
    }

    /**
     * In diesen Baugruppen ist die virtuelle Baugruppe eingebaut
     *
     * @return
     */
    public void getParentAssemblyEntries(boolean filtered, List<EtkDataPartListEntry> result) {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof iPartsProductId) { // Produkt
            iPartsProduct product = iPartsProduct.getInstance(project, (iPartsProductId)lastNodeId);

            if ((lastVirtualNode.getType() != iPartsNodeType.PRODUCT_KGTU_COMMON) && (lastVirtualNode.getType() != iPartsNodeType.PRODUCT_EINPAS_COMMON)) {
                // Keine ParentAssemblyEntries zurückliefern, wenn das aktuelle Flag für "Aggregate in Fahrzeug-Produkten anzeigen"
                // sich vom Flag am Produktknoten unterscheidet, da es sich dann um eine komplett andere Struktur handelt
                boolean isStructureWithAggregates = product.isStructureWithAggregates();
                if (isStructureWithAggregates != lastVirtualNode.getType().isProductStructureWithAggregates()) {
                    return;
                }
            }

            // Alle automatischen Verortungen des Produkts ermitteln und als Vater-Baugruppen zurückliefern
            List<iPartsStructureNode> parentStructureNodes = iPartsStructure.getInstance(project).getRootNode().getRetailParentNodesForProduct(product);
            for (iPartsStructureNode parentStructureNode : parentStructureNodes) {
                iPartsVirtualNode virtualParentNode = new iPartsVirtualNode(iPartsNodeType.STRUCTURE, parentStructureNode.getId());
                iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, getEtkProject(), virtualParentNode);
            }
            return;
        } else if (lastNodeId instanceof iPartsModelId) { // Baumuster
            // für Baumuster gibt es noch keinen Knoten
            return;
        } else if (lastNodeId instanceof iPartsSeriesId) { // Baureihe
            getParentAssemblyEntriesForSeriesOrModel(lastVirtualNode, ((iPartsSeriesId)lastNodeId).getSeriesNumber(), filtered, result);
            return;
        } else if (lastNodeId instanceof iPartsStructureId) { // Structure
            iPartsStructure structure = iPartsStructure.getInstance(getEtkProject());
            iPartsStructureNode node = structure.findNodeInAllChilds((iPartsStructureId)lastNodeId);
            if (node != null) {
                iPartsStructureNode parentNode = node.getParentNode();
                PartListEntryId rootEntryId;
                if (parentNode.getParentNode() != null) {
                    iPartsVirtualNode virtualParentNode = new iPartsVirtualNode(iPartsNodeType.STRUCTURE, parentNode.getId());
                    iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, getEtkProject(), virtualParentNode);
                } else { // Root
                    rootEntryId = new PartListEntryId(EtkConfigConst.ROOTKNOTEN, EtkConfigConst.ROOTKNOTENVER, "");
                    EtkDataPartListEntry rootPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(), rootEntryId);
                    result.add(rootPartListEntry);
                }
            }
            return;
        }

        if (Constants.DEVELOPMENT) {
            throw new RuntimeException("getParentAssemblyEntries findet keine Klasse, die zuständig ist! " + lastNodeId.toString());
        }
    }

    protected void getParentAssemblyEntriesForSeriesOrModel(iPartsVirtualNode virtualNode, String seriesOrModelNumber, boolean filtered,
                                                            List<EtkDataPartListEntry> result) {
        STRUCTURE_CONSTRUCTION_TYPE constructionType = null;
        if ((virtualNode.getType() == iPartsNodeType.DIALOG_HMMSM) || (virtualNode.getType() == iPartsNodeType.DIALOG_EINPAS)) {
            if (iPartsRight.checkCarAndVanInSession()) {
                constructionType = STRUCTURE_CONSTRUCTION_TYPE.DIALOG_SERIES;
            }
        } else if ((virtualNode.getType() == iPartsNodeType.EDS_EINPAS) || iPartsEdsStructureHelper.isEdsModelStructureNode(virtualNode.getType())) {
            if (iPartsRight.checkTruckAndBusInSession()) {
                constructionType = STRUCTURE_CONSTRUCTION_TYPE.EDS_MODEL;
            }
        } else if (virtualNode.getType() == iPartsNodeType.MBS_STRUCTURE) {
            if (iPartsRight.checkTruckAndBusInSession()) {
                constructionType = STRUCTURE_CONSTRUCTION_TYPE.MBS_MODEL;
            }
        } else if (virtualNode.getType() == iPartsNodeType.CTT_MODEL) {
            if (iPartsRight.checkTruckAndBusInSession()) {
                constructionType = STRUCTURE_CONSTRUCTION_TYPE.CTT_MODEL;
            }
        }

        if (constructionType != null) {
            // In allen Strukturknoten suchen, wo dieser Konstruktionstyp mit dem entsprechenden Baumusternummerpräfix verwendet wird
            Collection<iPartsStructureNode> structureNodes = iPartsStructure.getInstance(project).getChildrenRecursively();
            for (iPartsStructureNode structureNode : structureNodes) {
                if ((structureNode.getConstructionType() == constructionType) && seriesOrModelNumber.startsWith(structureNode.getModelTypePrefix())) {
                    iPartsVirtualNode virtualParentNode = new iPartsVirtualNode(iPartsNodeType.STRUCTURE, structureNode.getId());
                    iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, getEtkProject(), virtualParentNode);
                }
            }
        }
    }

    protected boolean getParentAssemblyEntriesForParentId(Class<? extends HierarchicalIDWithType> idClass, iPartsNodeType parentNodeType,
                                                          boolean filtered, List<EtkDataPartListEntry> result) {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (idClass.isAssignableFrom(lastNodeId.getClass())) { // ID-Typ stimmt
            HierarchicalIDWithType parentId = ((HierarchicalIDWithType)lastNodeId).getParentId();

            int virtualParentNodesLength = getVirtualIds().size();
            if (parentId == null) {
                virtualParentNodesLength--; // kein Vater-Knoten als letzte virtualId
            }
            iPartsVirtualNode[] virtualParentNodes = new iPartsVirtualNode[virtualParentNodesLength];
            for (int i = 0; i < getVirtualIds().size() - 1; i++) { // ohne Vater-Knoten als letzte virtualId
                virtualParentNodes[i] = getVirtualIds().get(i);
            }

            // letzte virtuelId ist die neue parentId
            if (parentId != null) {
                virtualParentNodes[virtualParentNodesLength - 1] = new iPartsVirtualNode(parentNodeType, parentId);
            }

            iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(result, filtered, getEtkProject(), virtualParentNodes);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Fügt der übergebenen String-Liste die {@link DBDataObjectAttribute}s der Multisprachfelder aus der übergebenen
     * Tabelle {@link EtkDatabaseTable} inkl. Rückfallsprachen hinzu. Falls die DB Version größer 6.2, dann werden Attribute
     * für Lang- und Kurztexte angelegt.
     *
     * @param databaseTable
     * @param selectFields
     * @param strList
     * @param attributes
     * @param hasLongTexts
     * @param startFieldIndex
     * @param languages
     * @return Endindex basierend auf dem {@code startFieldIndex} und der Anzahl der mehrspachigen Felder
     */
    protected int addMultiLangText(EtkDatabaseTable databaseTable, Collection<String> selectFields, List<String> strList,
                                   DBDataObjectAttributes attributes, boolean hasLongTexts, int startFieldIndex, List<String> languages) {
        int langFieldIndex = startFieldIndex;
        // Es wurde für jedes MultilLang-Feld pro eingestellter Sprache ein Join auf die SPRACHE Tabelle gemacht.
        // Dadurch muss man auch hier pro Sprache die Werteliste für alle MultiLang-Felder (strList) durchgehen,
        // z.B. erst DE Text, dann EN Text, usw.
        for (String dbLanguage : languages) {
            for (String multiLangField : databaseTable.getMultiLangFields()) {
                if (selectFields.contains(TableAndFieldName.make(databaseTable.getName(), multiLangField).toLowerCase())) {
                    // ACHTUNG! Ab DB-Version 6.2 sind die Multisprachfelder doppelt! Erst der kurze und dann zusätzlich der lange Text.
                    // UND: der lange Text ist nur gesetzt, wenn er tatsächlich länger ist, als der kurze!
                    String text = strList.get(langFieldIndex);
                    if (hasLongTexts) {
                        langFieldIndex++;
                        String longText = strList.get(langFieldIndex);
                        text = getEtkProject().getEtkDbs().getLongestText(longText, text);
                    }

                    // Das Attribut könnte schon existieren, wenn es für eine andere Sprache angelegt wurde
                    DBDataObjectAttribute multiLangAttribute = attributes.getField(multiLangField);
                    if (multiLangAttribute == null) {
                        multiLangAttribute = new DBDataObjectAttribute(multiLangField, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false);
                        attributes.addField(multiLangAttribute, DBActionOrigin.FROM_DB);
                    }

                    multiLangAttribute.setPreloadValueForMultiLanguage(dbLanguage, text);
                    langFieldIndex++;
                }
            }
        }

        return langFieldIndex;
    }

    /**
     * Wird nach dem Laden der virtuellen Stückliste (aus der DB oder aus dem Cache) aufgerufen.
     *
     * @param subAssembliesOnly  Sollen NUR die Unterbaugruppen von dieser Baugruppe geladen werden und NICHT auch normale
     *                           Stücklisteneinträge?
     * @param fields             Liste der nötigen Ergebnisfelder
     * @param partlist           Geladene Stückliste als Liste von {@link EtkDataPartListEntry}s
     * @param loadAdditionalData Sollen zusätzliche Daten für die Stücklisteneinträge geladen werden (nur relevant für Ableitungen)
     */
    public synchronized void afterLoadPartlist(boolean subAssembliesOnly, EtkDisplayFields fields, DBDataObjectList<EtkDataPartListEntry> partlist,
                                               boolean loadAdditionalData) {
        // standardmäßig nichts weiter machen
    }

    /**
     * Prüft ob der im <code>validationHelper</code> gesetzte Zeitstempel innerhalb des übergebenen Zeitintervalls liegt
     *
     * @param releaseFrom
     * @param releaseTo
     * @param validationHelper
     * @return
     */
    protected boolean releaseDateCheck(String releaseFrom, String releaseTo, ConstructionValidationDateHelper validationHelper) {
        return validationHelper.releaseDateCheck(releaseFrom, releaseTo);
    }

    /**
     * Soll diese Stückliste den globalen Cache verwenden?
     *
     * @return
     */
    public boolean isCacheAssemblyEntries() {
        return true;
    }
}
