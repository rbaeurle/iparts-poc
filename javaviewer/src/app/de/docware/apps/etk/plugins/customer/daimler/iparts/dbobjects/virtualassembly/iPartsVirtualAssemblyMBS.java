/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.project.mechanic.usage.MechanicUsagePosition;
import de.docware.apps.etk.base.search.model.PartsSearchSqlSelect;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDatasetWithEntries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsVirtualMaterialSearchDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ConstructionValidationDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMBSHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.mbs.iPartsMBSModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.mbs.iPartsMBSPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.weak.WeakKeysMap;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Virtuelle Sicht auf die MBS-Konstruktionsdaten
 */
public class iPartsVirtualAssemblyMBS extends iPartsVirtualAssembly {

    private static final char ALIAS_FOR_DB_JOIN = 'P';

    private static EtkMultiSprache multiLangForPseudoKgNode;

    public iPartsVirtualAssemblyMBS(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);

        // Es gibt sehr viele Objekte dieser Klasse -> statische Variable für den sowieso immer gleichen Text "Ohne KG"
        // verwenden. Diese kann aber NICHT schon statisch initialisiert werden, weil die Übersetzungsdateien erst beim
        // Start der Anwendung eingelesen werden.
        if (multiLangForPseudoKgNode == null) {
            multiLangForPseudoKgNode = new EtkMultiSprache("!!Ohne KG", project.getConfig().getDatabaseLanguages());
        }
    }

    @Override
    public boolean isCacheAssemblyEntries() {
        // MBS-Strukturen sollen nicht im globalen Cache landen, weil diese abhängig vom ausgewählten Datum sind
        return false;
    }

    @Override
    public DBDataObjectList<EtkDataPartListEntry> loadPartList(boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsModelId modelId = (iPartsModelId)(getRootNode().getId());
        // Die komplette Struktur holen
        iPartsMBSModel model = iPartsMBSModel.getInstance(getEtkProject(), modelId);
        iPartsCatalogNode nodes = model.getCompleteMBSStructure(getEtkProject());
        MBSStructureId structureId = null;
        // Suche den MBSStructure Knoten
        for (int i = getVirtualIds().size() - 1; i > 0; i--) {
            IdWithType iPartsVirtualNodeId = getVirtualIds().get(i).getId();
            if (iPartsVirtualNodeId instanceof MBSStructureId) {
                structureId = (MBSStructureId)iPartsVirtualNodeId;
                break;
            }
        }
        // Knoten für subId oder subId == null, dann erste Ebene laden
        return loadVirtualMBSPartListData(nodes, getRootNode(), structureId, subAssembliesOnly, model);
    }

    @Override
    public String getOrderNumber() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        if (lastVirtualNode.getId() instanceof MBSStructureId) {
            MBSStructureId structureId = (MBSStructureId)lastVirtualNode.getId();
            if (structureId.isListNumberNode()) {
                return structureId.getListNumber();
            } else if (structureId.isConGroupNode()) {
                return structureId.getConGroup();
            }
        }
        return super.getOrderNumber();
    }

    @Override
    public String getPartsListType() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();
        if (lastNodeId instanceof MBSStructureId) {
            MBSStructureId structureId = (MBSStructureId)lastNodeId;
            if (structureId.isListNumberNode()) {
                return PARTS_LIST_TYPE_MBS_LIST_NUMBER;
            } else if (structureId.isConGroupNode()) {
                return PARTS_LIST_TYPE_MBS_CON_GROUP;
            }
        }

        return super.getPartsListType();
    }

    @Override
    public String getPictureName() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof MBSStructureId) {
            MBSStructureId structureId = (MBSStructureId)lastNodeId;
            iPartsModelId modelId = new iPartsModelId(iPartsVirtualNode.getModelNumberFromAssemblyId(getAsId()));
            if (modelId.isValidId()) {
                MBSStructureNode node = MBSStructure.getInstance(getEtkProject(), modelId).getNode(structureId);
                if (node != null) {
                    return node.getPictureName();
                }
            }
            return "";
        }

        return super.getPictureName();
    }

    @Override
    public DBDataObjectAttributes loadAssemblyHeadAttributes(String[] resultFields) {
        DBDataObjectAttributes result = super.loadAssemblyHeadAttributes(resultFields);
        String partsListType = getPartsListType();
        if (partsListType.equals(PARTS_LIST_TYPE_MBS_LIST_NUMBER) || partsListType.equals(PARTS_LIST_TYPE_MBS_CON_GROUP)) {
            iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
            IdWithType lastNodeId = lastVirtualNode.getId();
            if (lastNodeId instanceof MBSStructureId) {
                MBSStructureId structureId = (MBSStructureId)lastNodeId;
                iPartsMBSModel model = iPartsMBSModel.getInstance(getEtkProject(), (iPartsModelId)(getRootNode().getId()));
                // Virtuelle Felder befüllen
                fillMBSAttributes(result, structureId, model);
            }
        }

        return result;
    }


    @Override
    public void getParentAssemblyEntries(boolean filtered, List<EtkDataPartListEntry> result) {
        if (getParentAssemblyEntriesForParentId(MBSStructureId.class, iPartsNodeType.MBS, filtered, result)) {
            return;
        }
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();
        if (lastNodeId instanceof iPartsModelId) { // MBS-Baumuster
            getParentAssemblyEntriesForSeriesOrModel(lastVirtualNode, ((iPartsModelId)lastNodeId).getModelNumber(), filtered, result);
            return;
        }
        super.getParentAssemblyEntries(filtered, result);
    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();
        if (lastNodeId instanceof iPartsModelId) { // Baumuster
            iPartsModelId modelId = (iPartsModelId)lastNodeId;
            if (!SessionKeyHelper.isSelectedModel(getEtkProject(), modelId)) {
                return null;
            }
            iPartsMBSModel model = iPartsMBSModel.getInstance(getEtkProject(), modelId);
            // Zur Sicherheit hier den Text für alle Subknoten zum Baumuster laden. Ist eigentlich nicht nötig, da das
            // schon beim Auswählen der Baumuster passiert.
            model.loadSubTextsForModelId(getEtkProject());
            String titlePostFix = " (MBS)";
            // Die Art des Einstiegs in dem mehrsprachigen Feld hinzufügen
            EtkMultiSprache result = new EtkMultiSprache();
            EtkMultiSprache modelTitle = model.getModelTitle();

            List<String> fallbackLanguages = getEtkProject().getDataBaseFallbackLanguages();
            for (String lang : getEtkProject().getConfig().getDatabaseLanguages()) {
                result.setText(lang, modelTitle.getTextByNearestLanguage(lang, fallbackLanguages) + titlePostFix);
            }
            return result;
        } else if (lastNodeId instanceof MBSStructureId) { // MBS Struktur
            iPartsModelId modelId = new iPartsModelId(iPartsVirtualNode.getModelNumberFromAssemblyId(getAsId()));
            if (modelId.isValidId()) {
                // Zur Sicherheit hier den Text für alle Subknoten zur oberen Sachnummer (GS/SAA) laden. Ist eigentlich
                // nicht nötig, da das schon beim Auswählen der Baumuster passiert.
                MBSStructureId mbsStructureId = (MBSStructureId)lastNodeId;

                // Pseudo-KG-Knoten haben keine eigenen Daten, daher die Daten des oberen Knoten
                // verwenden (SAA/GS)
                boolean isPseudoKgNode = false;
                if (mbsStructureId.getListNumber().equals(mbsStructureId.getConGroup())) {
                    isPseudoKgNode = true;
                    mbsStructureId = new MBSStructureId(mbsStructureId.getListNumber(), "");
                }

                iPartsMBSModel model = iPartsMBSModel.getInstance(getEtkProject(), modelId);
                model.loadSubTextsForStructureId(getEtkProject(), mbsStructureId);

                if (!isPseudoKgNode) {
                    // Nun den Title aus dem geladenen Knoten bestimmen
                    MBSStructure mbsStructure = MBSStructure.getInstance(getEtkProject(), modelId);
                    MBSStructureNode node = mbsStructure.getNode(mbsStructureId);
                    if (node != null) {
                        return node.getTitle(getEtkProject());
                    }
                } else {
                    return multiLangForPseudoKgNode;
                }
            }
            return null;
        }
        return super.getTexts();
    }


    /**
     * Lädt die virtulle Struktur für MBS Konstruktionsknoten
     *
     * @param completeStructure
     * @param rootNode
     * @param subNodeId
     * @param subAssembliesOnly
     * @param model
     * @return
     */
    private DBDataObjectList<EtkDataPartListEntry> loadVirtualMBSPartListData(iPartsCatalogNode completeStructure,
                                                                              iPartsVirtualNode rootNode,
                                                                              MBSStructureId subNodeId,
                                                                              boolean subAssembliesOnly,
                                                                              iPartsMBSModel model) {
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
        // Flag, ob es sich um einen ListNumber Knoten handelt (1.Ebene)
        boolean isMBSStructureParentNode = (nodeParent.getId() instanceof MBSStructureId);
        if (isMBSStructureParentNode && ((MBSStructureId)nodeParent.getId()).isConGroupNode()) {
            if (subAssembliesOnly) {
                // Es sind nur die Unterbaugruppen gesucht, per Definition gibt es unterhalb eines ConGroup Knotens keine
                // Unterbaugruppen mehr
                // -> ist immer leer
                return new EtkDataPartListEntryList();

            } else {
                // Stückliste laden
                return loadMBSPartList(nodeParent, model);
            }
        } else {
            // Parent ist kein Baumuster sondern ein ListNumber Knoten -> Prüfung, ob der Datensatz valide ist
            if (isMBSStructureParentNode) {
                MBSStructureId structureId = (MBSStructureId)nodeParent.getId();
                if (structureId.isListNumberNode()) {
                    if (!model.isValidStructure(structureId)) {
                        return new EtkDataPartListEntryList();
                    }
                }
            }
            // Struktur laden
            return loadMBSStructurePartList(nodeParent, rootNode, subAssembliesOnly, model);
        }
    }

    /**
     * Lädt die Positionen der MBS Strukturknoten
     *
     * @param nodeParent
     * @param rootNode
     * @param subAssembliesOnly
     * @param model
     * @return
     */
    private DBDataObjectList<EtkDataPartListEntry> loadMBSStructurePartList(iPartsCatalogNode nodeParent, iPartsVirtualNode rootNode,
                                                                            boolean subAssembliesOnly, iPartsMBSModel model) {
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();
        Set<EtkDataPartListEntry> resultSets = createSetWithComparator();
        // Wir sind noch in der MBS Struktur -> Zeige die Unterstrukturknoten an
        for (iPartsCatalogNode nodeChild : nodeParent.getChildren()) {
            if (nodeChild.getId() instanceof MBSStructureId) {
                MBSStructureId structureId = (MBSStructureId)nodeChild.getId();
                // Check, ob der Knoten für das aktuelle Datum gültig ist
                if (!model.isValidStructure(structureId)) {
                    continue;
                }
                // Handelt es sich bei dem Kindknoten um einen ListNumber Koten (1.Ebene der Struktur), dann muss
                // zusätzlich geprüft werden, ob die Kinder gültig sind
                if (structureId.isListNumberNode()) {
                    boolean hasValidChild = false;
                    for (iPartsCatalogNode child : nodeChild.getChildren()) {
                        MBSStructureId childStructureId = (MBSStructureId)child.getId();
                        // Check, ob der Kindknoten für das aktuelle Datum gültig ist
                        if (model.isValidStructure(childStructureId)) {
                            hasValidChild = true;
                            break;
                        }
                    }
                    // Ist kein Kindknoten gültig, dann ist auch der ListNumber Knoten nicht gültig
                    if (!hasValidChild) {
                        continue;
                    }
                }
                // Die Untergeordneten Strukturknoten anzeigen
                EtkDataPartListEntry entry = createVirtualNode(0, rootNode, new iPartsVirtualNode(iPartsNodeType.MBS, structureId));
                if (entry != null) {
                    DBDataObjectAttributes attributes = entry.getAttributes();
                    // Die virtuellen Felder befüllen
                    if (fillMBSAttributes(attributes, structureId, model)) {
                        resultSets.add(entry);
                    }
                }
            }
        }
        if (!resultSets.isEmpty() && !subAssembliesOnly) { // Texte sind bei subAssembliesOnly irrelevant
            String listNumber = getListNumberFromCatalogNode(nodeParent);
            MBSStructureId pseudoKgNodeId = new MBSStructureId(listNumber, listNumber);
            if ((model.getDataObjectForStructureId(pseudoKgNodeId) == null) || (resultSets.size() > 1)) {
                // Sind echte Stücklistenpositionen vorhanden (bei einem Pseudo-KG-Knoten mehr als einer (das ist nämlich
                // gerade der Pseudo-KG-Knoten), dann müssen hier die Texte hinzugefügt werden
                addNodeTextEntries(nodeParent, resultSets, model);
            }
        }
        // Alle erzeugten Stücklistenpositionen sind schon sortiert und bekommen vor dem Hinzufügen ihre laufende Nummer
        int lfdNumber = 0;
        for (EtkDataPartListEntry entry : resultSets) {
            lfdNumber++;
            iPartsMBSStructureId structureId = new iPartsMBSStructureId(entry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SNR),
                                                                        entry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SNR_SUFFIX),
                                                                        entry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_POS),
                                                                        entry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SORT),
                                                                        entry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_KEM_FROM));
            String mbsStructureGuid = structureId.createMBSStructureGuid();
            entry.setFieldValue(FIELD_K_LFDNR, mbsStructureGuid, DBActionOrigin.FROM_DB);
            entry.setFieldValue(FIELD_K_SEQNR, EtkDbsHelper.formatLfdNr(lfdNumber), DBActionOrigin.FROM_DB);
            entry.updateIdFromPrimaryKeys();
            result.add(entry, DBActionOrigin.FROM_DB);
        }
        return result;
    }

    /**
     * Fügt zum übergebenen <code>nodeParent</code> die dazugehörigen Texte für die Stückliste
     *
     * @param nodeParent
     * @param resultSets
     * @param model
     */
    private void addNodeTextEntries(iPartsCatalogNode nodeParent, Set<EtkDataPartListEntry> resultSets, iPartsMBSModel model) {
        String listNumber = getListNumberFromCatalogNode(nodeParent);
        if (StrUtils.isValid(listNumber)) {
            // Textpositionen haben keine untere Sachnummer. Hier alle Texte für die obere Sachnummer holen
            Set<iPartsDataMBSStructure> textsForListNumber = MBSTextEntryCache.getInstance(getEtkProject()).getTextsForStructureListNumber(listNumber);
            if (textsForListNumber != null) {
                // Gibt es einen Pseudo-KG-Knoten, in dem evtl. einige Texte nur angezeigt werden sollen aufgrund der Zugehörigkeit
                // zu anderen Stücklisteneinträgen?
                Set<String> invalidMBSPosNumberSet = new HashSet<>();
                Set<String> invalidMBSSortNumberSet = new HashSet<>();
                MBSStructureId pseudoKgNodeId = new MBSStructureId(listNumber, listNumber);
                if (model.getDataObjectForStructureId(pseudoKgNodeId) != null) {
                    List<iPartsVirtualNode> nodes = new ArrayList<>();
                    nodes.add(new iPartsVirtualNode(iPartsNodeType.MBS_STRUCTURE, new iPartsModelId(model.getModelNumber())));
                    nodes.add(new iPartsVirtualNode(iPartsNodeType.MBS, pseudoKgNodeId));
                    AssemblyId pseudoKgNodeAssemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
                    EtkDataAssembly pseudoKgAssembly = EtkDataObjectFactory.createDataAssembly(getEtkProject(), pseudoKgNodeAssemblyId);
                    for (EtkDataPartListEntry partListEntry : pseudoKgAssembly.getPartListUnfiltered(pseudoKgAssembly.getEbene())) {
                        // Nur die MBS-Position und MBS-Sortierung von Datensätzen mit echter Materialnummer und Strukturstufe 1
                        // in den Sets merken (alle anderen sind Texte)
                        if (!partListEntry.getFieldValue(FIELD_K_MATNR).isEmpty() && partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_LEVEL).equals("1")) {
                            invalidMBSPosNumberSet.add(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_POS));
                            invalidMBSSortNumberSet.add(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SORT));
                        }
                    }
                }

                for (iPartsDataMBSStructure structure : textsForListNumber) {
                    // Falls die MBS-Position oder MBS-Sortierung des Text-Datensatzes identisch ist zur MBS-Position bzw.
                    // MBS-Sortierung von einem Nicht-Text-Datensatz in dem Pseudo-KG-Knoten, dann den Text in diesem SAA/GS-Knoten
                    // nicht anzeigen
                    if (!invalidMBSPosNumberSet.contains(structure.getFieldValue(FIELD_DSM_POS)) && !invalidMBSSortNumberSet.contains(structure.getFieldValue(FIELD_DSM_SORT))) {
                        String text = structure.getFieldValue(FIELD_DSM_SNR_TEXT);
                        // Stücklistenposition erzeugen
                        EtkDataPartListEntry entry = createTextEntry(text);
                        if (entry != null) {
                            // Die MBS spezifischen virtuellen Attribute füllen
                            DBDataObjectAttributes attributes = entry.getAttributes();
                            if (fillMBSAttributes(attributes, structure, structure.getFieldValue(FIELD_DSM_SNR_TEXT))) {
                                resultSets.add(entry);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Liefert die ListNumber (obere Sachnummer) zum übergebenen nodeParent auf Basis der enthaltenen ID
     *
     * @param nodeParent
     * @return
     */
    private String getListNumberFromCatalogNode(iPartsCatalogNode nodeParent) {
        // Unterscheidung Baumuster <-> "GS/SAA" auf "KG zu GS/SAA"
        if (nodeParent.isIPartsModelId()) {
            iPartsModelId modelId = (iPartsModelId)nodeParent.getId();
            return modelId.getModelNumber();
        } else if (nodeParent.isMBSStructureId()) {
            MBSStructureId structureId = (MBSStructureId)nodeParent.getId();
            return structureId.getListNumber();
        }
        return null;
    }

    /**
     * Lädt die MBS Konstruktionsstückliste
     *
     * @param nodeParent
     * @param model
     * @return
     */
    private DBDataObjectList<EtkDataPartListEntry> loadMBSPartList(iPartsCatalogNode nodeParent, iPartsMBSModel model) {
        MBSStructureId structureId = (MBSStructureId)nodeParent.getId();
        iPartsDataMBSStructure dataMBSStructure = model.getDataObjectForStructureId(structureId);
        if (dataMBSStructure == null) {
            // Kann eigentlich nicht passieren, da die IDs ja schon aus der Struktur kommen
            return new EtkDataPartListEntryList();
        }
        Calendar validationDate = SessionKeyHelper.getMbsConstructionDate();
        ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getMbsConstructionDateHelper();

        // Stücklistenpositionen bestimmen
        DBDataObjectList<EtkDataPartListEntry> result = computeMBSPartslistDataForOneSubPart(structureId.getConGroup(),
                                                                                             structureId.getConGroup(),
                                                                                             validationDate, 1,
                                                                                             getMaxLevelFromConfig(),
                                                                                             validationHelper);
        // Alle erzeugten Positionen liegen nun in der richtigen Reihenfolge in der Liste. Jetzt muss nur noch die
        // Sequenznummer gesetzt werden.
        addSeqNumbers(result);
        return result;
    }

    /**
     * Brechnet alle Stücklistenpositionen zur übergebenen oberen Sachnummer inkl. aller unteren Strukturstufen
     *
     * @param upperNo
     * @param mbsStructurePath
     * @param validationDate
     * @param level
     * @return
     */
    public DBDataObjectList<EtkDataPartListEntry> computeMBSPartslistDataForOneSubPart(String upperNo, String mbsStructurePath,
                                                                                       Calendar validationDate,
                                                                                       int level, int maxLevel,
                                                                                       ConstructionValidationDateHelper validationHelper) {
        // Set in dem die Positionen nach ihrem Sortierungswert und der Position sortiert werden. Das passiert pro Ebene
        // damit die Texte und Positionen in der richtigen Reihenfolge angelegt werden.
        Set<EtkDataPartListEntry> setWithEntries = createSetWithComparator();
        Map<PartListEntryId, MBSPartsListRowData> entryToData = new LinkedHashMap<>();
        // Legt die Textpositionen und normalen Stücklistenpositionen zusammen
        mergeTextAndDataEntries(setWithEntries, entryToData, upperNo, mbsStructurePath, level, validationDate, validationHelper);
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();
        // Jetzt müssen pro Position alle Positionen der unteren Strukturen bestimmt und hinzugefügt werden
        addSubStructureEntries(setWithEntries, result, entryToData, mbsStructurePath, validationDate, level, maxLevel,
                               validationHelper);
        return result;
    }

    /**
     * Legt alle Text- und normalen Stücklistenpositionen zusammen
     *
     * @param setWithEntries
     * @param entryToData
     * @param upperNo
     * @param mbsStructurePath
     * @param level
     * @param validationDate
     */
    private void mergeTextAndDataEntries(Set<EtkDataPartListEntry> setWithEntries, Map<PartListEntryId, MBSPartsListRowData> entryToData,
                                         String upperNo, String mbsStructurePath, int level, Calendar validationDate, ConstructionValidationDateHelper validationHelper) {
        // Textpositionen zur oberen Sachnummer laden
        setWithEntries.addAll(loadTextEntries(upperNo, validationDate, mbsStructurePath, level, validationHelper));

        // Alle Teilepositionen zur oberen Sachnummer laden
        // Es wird die Lademethode über die EtkDataObjectList verwendet, weil sie zentral gewartet wird und somit
        // zukünftige Änderungen an z.B. den Fallback-Sprachen automatisch berücksichtigen würde. Sie ist zwar etwas
        // langsamer als die händische Abfrage, wobei die Unterschiede minimal sind. Sollte das Laden via EtkDataObjectList
        // in Zukunft langsamer werden, könnten wir hier wieder auf die händische Abfrage umschalten (siehe alten Commit).
        Map<iPartsMBSPartlistId, MBSPartsListRowData> idToRowObject = loadDataForUpperWithDataObjectList(upperNo, validationDate, validationHelper);

        // Teilepositionen erzeugen und in dem Set und der Map ablegen
        for (MBSPartsListRowData singleEntry : idToRowObject.values()) {
            EtkDataPartListEntry entry = createMBSPartListEntry(singleEntry, level, mbsStructurePath);
            setWithEntries.add(entry);
            entryToData.put(entry.getAsId(), singleEntry);
        }
    }

    /**
     * Lädt alle Einträge aus DA_PARTSLIST_MBS zur übergebenen <code>upperNo</code> (obere Sachnummer) mit Join auf die
     * <code>MAT</code> Tabelle und Join auf sich selber um zu bestimmen, ob es weitere Teile in den Unterstrukturen gibt.
     * <p>
     * Zum Laden der Daten wird der <code>searchSortAndFillWithJoin()</code> Mechanismus aus {@link EtkDataObjectList)}
     * verwendet.
     *
     * @param upperNo
     * @param validationDate
     * @return
     */
    private Map<iPartsMBSPartlistId, MBSPartsListRowData> loadDataForUpperWithDataObjectList(String upperNo, Calendar validationDate, ConstructionValidationDateHelper validationHelper) {
        EtkDisplayFields partsListFields = getEtkProject().getAllDisplayFieldsForTable(TABLE_DA_PARTSLIST_MBS);
        EtkDisplayFields fields = new EtkDisplayFields(partsListFields);

        // Benötigte Felder für den Materialstamm hinzufügen
        EtkDisplayFields matFields = iPartsVirtualAssemblyHelper.getNeededMatSelectFields(getEtkProject(), getAsId(), FIELD_M_ASSEMBLY, FIELD_M_IMAGE_AVAILABLE);
        fields.addFelder(matFields);

        Map<iPartsMBSPartlistId, MBSPartsListRowData> idToRowObject = new HashMap<>();
        EtkDataObjectList.FoundAttributesCallback callback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                // Bestimmen der anzuzeigenden Materialnummer oder Abbruch, wenn es sich nicht um Material handelt.
                if (!iPartsVirtualAssemblyHelper.checkMatNoMBS(attributes)) {
                    return false;
                }
                // Erstmal die berechneten Felder in die Row
                MBSPartsListRowData objectWithRowData = new MBSPartsListRowData();
                // Die Felder aus der Tabelle [DA_PARTSLIST_MBS] auslesen
                iPartsVirtualAssemblyHelper.addAttributesFromDB(partsListFields, attributes, objectWithRowData.partsListAttributes);
                // ID des Datensatzes kann nun ermittelt werden
                iPartsMBSPartlistId partsMBSPartlistId = new iPartsMBSPartlistId(
                        objectWithRowData.partsListAttributes.getField(FIELD_DPM_SNR).getAsString(),
                        objectWithRowData.partsListAttributes.getField(FIELD_DPM_POS).getAsString(),
                        objectWithRowData.partsListAttributes.getField(FIELD_DPM_SORT).getAsString(),
                        objectWithRowData.partsListAttributes.getField(FIELD_DPM_KEM_FROM).getAsString());

                // Check, ob für die iPartsMBSPartlistId schon ein Objekt existiert. Falls ja, und es gibt einen
                // Text, dann muss der neue Text dem bestehenden Objekt hinzugefügt werden.
                MBSPartsListRowData existingRowObject = idToRowObject.get(partsMBSPartlistId);
                if (existingRowObject != null) {
                    objectWithRowData = existingRowObject;
                }

                // Wegen dem Join auf die Unterstrukturen können hier mehrere kommen -> teste, ob diese Id schon da war
                if (existingRowObject != null) {
                    return false;
                }
                // Die Felder aus der Tabelle [MAT] auslesen
                iPartsVirtualAssemblyHelper.addAttributesFromDB(matFields, attributes, objectWithRowData.matAttributes);
                // Das letzte Feld ist gefüllt, falls diese Sachnummer noch eine Unterstruktur hat.
                objectWithRowData.hasSubStruct = !attributes.getFieldValue(ALIAS_FOR_DB_JOIN + "_" + FIELD_DPM_SNR).isEmpty();

                // Testen, ob das Item gültig für das übergebene Datum ist
                String releaseFrom = objectWithRowData.partsListAttributes.getField(FIELD_DPM_RELEASE_FROM).getAsString();
                String releaseTo = objectWithRowData.partsListAttributes.getField(FIELD_DPM_RELEASE_TO).getAsString();
                boolean dateValid = releaseDateCheck(releaseFrom, releaseTo, validationHelper);
                if (dateValid) {
                    idToRowObject.put(partsMBSPartlistId, objectWithRowData);
                }
                return false;
            }
        };
        iPartsDataMBSPartlistList list = new iPartsDataMBSPartlistList();
        list.searchSortAndFillWithJoin(getEtkProject(), getEtkProject().getDBLanguage(), fields,
                                       new String[]{ TableAndFieldName.make(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR)/*,
                                                     TableAndFieldName.make(TABLE_MAT, FIELD_M_ASSEMBLY)*/ },
                                       new String[]{ upperNo/*, EtkDataObjectList.getNotWhereValue(SQLStringConvert.booleanToPPString(true))*/ },
                                       false, null, // Sortierung funktioniert mit einem reinen Callback nicht
                                       false, callback,
                                       new EtkDataObjectList.JoinData(TABLE_MAT,
                                                                      new String[]{ FIELD_DPM_SUB_SNR },
                                                                      new String[]{ FIELD_M_MATNR }, true, false),
                                       new EtkDataObjectList.JoinData(TABLE_DA_PARTSLIST_MBS,
                                                                      new String[]{ FIELD_DPM_SUB_SNR },
                                                                      new String[]{ FIELD_DPM_SNR }, true, false, ALIAS_FOR_DB_JOIN));
        return idToRowObject;
    }

    /**
     * Lädt alle Textpositionen zur übergebenen oberen Sachnummer via {@link MBSTextEntryCache}
     *
     * @param upperNo
     * @param validationDate
     * @param mbsStructurePath
     * @param level
     * @return
     */
    private List<EtkDataPartListEntry> loadTextEntries(String upperNo, Calendar validationDate, String mbsStructurePath, int level, ConstructionValidationDateHelper validationHelper) {
        List<EtkDataPartListEntry> result = new ArrayList<>();
        Set<iPartsDataMBSPartlist> list = MBSTextEntryCache.getInstance(getEtkProject()).getTextsForPartlistUpperNo(getEtkProject(), upperNo);
        if ((list == null) || list.isEmpty()) {
            return result;
        }

        for (iPartsDataMBSPartlist textEntry : list) {
            // Jetzt prüfen, ob die Texte bezüglich dem Datum gültig sind
            String releaseFrom = textEntry.getFieldValue(FIELD_DPM_RELEASE_FROM);
            String releaseTo = textEntry.getFieldValue(FIELD_DPM_RELEASE_TO);
            if (releaseDateCheck(releaseFrom, releaseTo, validationHelper)) {
                // Text ist gültig, also eine Textposition erzeugen
                String text = textEntry.getFieldValue(FIELD_DPM_SNR_TEXT);
                EtkDataPartListEntry newEntry = createTextEntry(text);
                if (newEntry != null) {
                    DBDataObjectAttributes attributes = newEntry.getAttributes();
                    // Hier die virtuellen Felder des Textes setzen
                    fillVirtualFieldsWithDataFromObject(TABLE_DA_PARTSLIST_MBS, textEntry, attributes);
                    // Texte haben auch eine Strukturstufe (sind ja eigentliche normale Stücklistenpositionen aus DA_PARTSLIST_MBS)
                    attributes.addField(iPartsDataVirtualFieldsDefinition.MBS_LEVEL, String.valueOf(level), true, DBActionOrigin.FROM_DB);
                    // Die klfdNr wird, wie bei den anderen Positionen bestimmt. Zusätzlich wird ein Suffix angehängt
                    // um die Texte sofort zu erkennen
                    String klfdNr = iPartsMBSPrimaryKey.buildMBSGUIDForPartListSeqNo(textEntry.getAttributes(), mbsStructurePath);
                    newEntry.setFieldValue(FIELD_K_LFDNR, klfdNr + "|TextEntry", DBActionOrigin.FROM_DB);
                    newEntry.updateIdFromPrimaryKeys();
                    result.add(newEntry);
                }
            }
        }
        return result;
    }

    /**
     * Setzt die Sequenznummer bei den übergebenen Stücklistenpositionen
     *
     * @param result
     */
    public void addSeqNumbers(DBDataObjectList<EtkDataPartListEntry> result) {
        for (int seqNo = 0; seqNo < result.size(); seqNo++) {
            EtkDataPartListEntry entry = result.get(seqNo);
            entry.setFieldValue(FIELD_K_SEQNR, EtkDbsHelper.formatLfdNr(seqNo + 1), DBActionOrigin.FROM_DB);
            entry.updateIdFromPrimaryKeys();
        }
    }

    /**
     * Erzeugt einen virtuellen MBS Stücklisteneintrag
     *
     * @param currentLevel
     * @param mbsStructurePath
     * @return
     */
    private EtkDataPartListEntry createMBSPartListEntry(MBSPartsListRowData mbsPartsListRowData, int currentLevel, String mbsStructurePath) {

        // Attribute erzeugen und alle Felder mit Leer auffüllen
        DBDataObjectAttributes katAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_KATALOG);

        String matNo = mbsPartsListRowData.partsListAttributes.getField(FIELD_DPM_SUB_SNR).getAsString();

        katAttributes.addField(FIELD_K_VARI, getAsId().getKVari(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, getAsId().getKVer(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SACH, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SVER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MATNR, matNo, DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MVER, "", DBActionOrigin.FROM_DB);
        // KlfdNr setzen
        String guid = iPartsMBSPrimaryKey.buildMBSGUIDForPartListSeqNo(mbsPartsListRowData.partsListAttributes, mbsStructurePath);
        katAttributes.addField(FIELD_K_LFDNR, guid, DBActionOrigin.FROM_DB);

        // Jetzt die virtuellen Felder besetzen
        fillVirtualFieldsWithDataFromObject(TABLE_DA_PARTSLIST_MBS, mbsPartsListRowData.partsListAttributes, katAttributes);
        // Strukturstufe setzen
        katAttributes.addField(iPartsDataVirtualFieldsDefinition.MBS_LEVEL, String.valueOf(currentLevel), true, DBActionOrigin.FROM_DB);

        EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(), katAttributes);
        EtkDataPart partForPartListEntry = newPartListEntry.getPart();
        partForPartListEntry.setAttributes(mbsPartsListRowData.matAttributes, DBActionOrigin.FROM_DB);
        iPartsVirtualAssemblyHelper.addFilterAttributesForTable(getEtkProject(), TABLE_MAT, partForPartListEntry.getAttributes());

        if (newPartListEntry instanceof iPartsDataPartListEntry) {
            ((iPartsDataPartListEntry)newPartListEntry).setDataLoadedFlagsForVirtualPLE(true);
        }
        return newPartListEntry;
    }

    /**
     * Fügt einer Position alle Positionen der unteren Strukturen hinzu
     *
     * @param setWithEntries
     * @param result
     * @param entryToData
     * @param mbsStructurePath
     * @param validationDate
     * @param level
     * @param maxLevel
     */
    private void addSubStructureEntries(Set<EtkDataPartListEntry> setWithEntries, DBDataObjectList<EtkDataPartListEntry> result,
                                        Map<PartListEntryId, MBSPartsListRowData> entryToData, String mbsStructurePath,
                                        Calendar validationDate, int level, int maxLevel,
                                        ConstructionValidationDateHelper validationHelper) {
        // Jetzt alle Positionen durchlaufen und Positionen aus den unteren Strukturen bestimmen
        for (EtkDataPartListEntry partListEntry : setWithEntries) {
            // Hier die Reihenfolge aufbauen. Erst die Position reinlegen und dann prüfen, ob es Positionen in den unteren
            // Strukturen gibt
            result.add(partListEntry, DBActionOrigin.FROM_DB);
            MBSPartsListRowData rowData = entryToData.get(partListEntry.getAsId());
            // Texteinträge haben keine Daten und keine Positionen in den unteren Strukturen
            if (rowData == null) {
                continue;
            }
            // Falls keine Sub-Strukturen oder die maximale Strukturstufe erreicht wurde -> nicht tiefer gehen
            if (rowData.hasSubStruct && (level <= maxLevel)) {
                // Die Schlüsselwerte der aktuellen Sub-Teileposition werden hier in den Pfad aufgenommen
                String subSnr = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SUB_SNR);
                iPartsMBSPrimaryKey primaryKey = createPrimaryKeyForStructurePath(subSnr, partListEntry);
                String nextMBSStructurePath = mbsStructurePath + K_SOURCE_CONTEXT_DELIMITER + primaryKey.createMBSGUID();
                // Zu dieser unteren Sachnummer die unteren Positionen ermitteln
                result.addAll(computeMBSPartslistDataForOneSubPart(subSnr, nextMBSStructurePath, validationDate,
                                                                   level + 1, maxLevel, validationHelper),
                              DBActionOrigin.FROM_DB);
            }
        }
    }

    private iPartsMBSPrimaryKey createPrimaryKeyForStructurePath(String subSnr, EtkDataPartListEntry partListEntry) {
        String pos = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_POS);
        String sort = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SORT);
        String kemFrom = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_KEM_FROM);
        return new iPartsMBSPrimaryKey(subSnr, pos, sort, kemFrom);
    }

    /**
     * Liefert ein Set in dem die Stücklistenpositionen nach ihrem SORT und POSITiON Wert sortiert werden
     *
     * @return
     */
    private Set<EtkDataPartListEntry> createSetWithComparator() {
        return new TreeSet<>((o1, o2) -> {
            String sort1 = Utils.toSortString(o1.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SORT));
            String sort2 = Utils.toSortString(o2.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_SORT));
            int result = sort1.compareTo(sort2);
            if (result == 0) {
                String pos1 = Utils.toSortString(o1.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_POS));
                String pos2 = Utils.toSortString(o2.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_POS));
                result = pos1.compareTo(pos2);
            }
            return result;
        });
    }

    /**
     * Erzeugt einen Text-Stücklisteneintrag für eine Stückliste innerhalb der komplette MBS Struktur
     *
     * @param textForEntry
     * @return
     */
    protected EtkDataPartListEntry createTextEntry(String textForEntry) {
        // Attribute erzeugen und alle Felder mit Leer auffüllen
        DBDataObjectAttributes katAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_KATALOG);
        DBDataObjectAttributes matAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_MAT);

        katAttributes.addField(FIELD_K_VARI, getAsId().getKVari(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, getAsId().getKVer(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SACH, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MATNR, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MVER, "", DBActionOrigin.FROM_DB);

        katAttributes.addField(iPartsDataVirtualFieldsDefinition.STRUCT_PICTURE, "", DBActionOrigin.FROM_DB);

        EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(), katAttributes);

        // Texte des neuen Eintrags
        EtkMultiSprache texts = new EtkMultiSprache();
        texts.setText(Language.DE, textForEntry);
        setTextAttribute(matAttributes.getField(FIELD_M_TEXTNR), texts);
        setTextAttribute(matAttributes.getField(FIELD_M_CONST_DESC), texts);

        EtkDataPart partForPartListEntry = newPartListEntry.getPart();
        partForPartListEntry.setAttributes(matAttributes, DBActionOrigin.FROM_DB);

        if (newPartListEntry instanceof iPartsDataPartListEntry) {
            ((iPartsDataPartListEntry)newPartListEntry).setDataLoadedFlagsForVirtualPLE(true);
        }
        return newPartListEntry;
    }

    private void setTextAttribute(DBDataObjectAttribute textAttribute, EtkMultiSprache texts) {
        if (textAttribute != null) {
            textAttribute.setValueAsMultiLanguage(texts, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Befüllt die virtuellen Attribute mit den Werten aus dem {@link iPartsDataMBSStructure} zur übergebenen {@link MBSStructureId}
     *
     * @param attributes
     * @param structureId
     * @param mbsModel
     * @return
     */
    private boolean fillMBSAttributes(DBDataObjectAttributes attributes, MBSStructureId structureId, iPartsMBSModel mbsModel) {
        iPartsDataMBSStructure dataMBSStructure = mbsModel.getDataObjectForStructureId(structureId);
        MBSStructure structure = MBSStructure.getInstance(getEtkProject(), new iPartsModelId(mbsModel.getModelNumber()));

        // Pseudo-KG-Knoten haben keine eigenen Daten, daher die Daten des oberen Knoten
        // verwenden (SAA/GS)
        EtkMultiSprache multiLang = null;
        if (structureId.getListNumber().equals(structureId.getConGroup())) {
            multiLang = multiLangForPseudoKgNode;
        } else {
            // Existiert zur MBSStructureId ein Knoten, dann wird über den der Text bestimmt
            MBSStructureNode node = structure.getNode(structureId);
            if (node != null) {
                multiLang = node.getTitle(getEtkProject());
            }
        }

        String text = null;
        if (multiLang != null) {
            text = multiLang.getTextByNearestLanguage(getEtkProject().getDBLanguage(), getEtkProject().getDataBaseFallbackLanguages());
        }
        return fillMBSAttributes(attributes, dataMBSStructure, text);
    }

    /**
     * Befüllt die virtuellen Attribute mit den Werten aus dem {@link iPartsDataMBSStructure}
     *
     * @param attributes
     * @param dataMBSStructure
     * @param text
     * @return
     */
    private boolean fillMBSAttributes(DBDataObjectAttributes attributes, iPartsDataMBSStructure dataMBSStructure, String text) {
        if (!iPartsMBSHelper.isValidDataset(dataMBSStructure)) {
            return false;
        }
        if (text == null) {
            text = "";
        }
        attributes.addField(iPartsDataVirtualFieldsDefinition.MBS_LIST_NUMBER_DESC, text, true, DBActionOrigin.FROM_DB);
        // Nun die virtuellen Felder erzeugen und befüllen
        fillVirtualFieldsWithDataFromObject(TABLE_DA_STRUCTURE_MBS, dataMBSStructure, attributes);
        return true;
    }

    private void fillVirtualFieldsWithDataFromObject(String tablename, EtkDataObject dataObject, DBDataObjectAttributes attributesToFill) {
        fillVirtualFieldsWithDataFromObject(tablename, ((dataObject != null) ? dataObject.getAttributes() : null), attributesToFill);
    }


    /**
     * Befüllt die übergebenen <code>attributesToFill</code> mit den virtuellen Feldern aus dem Mapping der übergebenen
     * <code>tabelname</code>. Die Werte für die virtuellen Felder werden aus den übergebenen <code>attributesWithData</code>
     * geholt.
     *
     * @param tablename
     * @param attributesWithData
     * @param attributesToFill
     */
    private void fillVirtualFieldsWithDataFromObject(String tablename, DBDataObjectAttributes attributesWithData, DBDataObjectAttributes attributesToFill) {
        // Alle virtuellen Felder für OPS-Scope mit den Daten aus dem neusten Datensatz befüllen (sofern vorhanden)
        List<VirtualFieldDefinition> virtualFieldDefinitions = iPartsDataVirtualFieldsDefinition.getMapping(tablename,
                                                                                                            TABLE_KATALOG);
        // Durchlaufe alle virtuellen Felder aus dem Mapping zur Tabelle, erzeuge die Felder und befülle sie.
        for (VirtualFieldDefinition virtualFieldDefinition : virtualFieldDefinitions) {
            if (virtualFieldDefinition.getVirtualFieldName().startsWith(VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX
                                                                        + iPartsDataVirtualFieldsDefinition.MBS
                                                                        + iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER)) {
                String virtualFieldValue;
                if (attributesWithData != null) {
                    virtualFieldValue = attributesWithData.getFieldValue(virtualFieldDefinition.getSourceFieldName());
                } else {
                    virtualFieldValue = "";
                }
                if (virtualFieldValue != null) {
                    attributesToFill.addField(virtualFieldDefinition.getVirtualFieldName(), virtualFieldValue, true, DBActionOrigin.FROM_DB);
                }
            }
        }
    }

    /**
     * Liefert die höchste Strukturstufe der Änderungsstände für die Suche und den Aufbau der Stückliste
     *
     * @return
     */
    public static int getMaxLevelFromConfig() {
        return iPartsPlugin.isEditPluginActive() ? de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin.getMaxStructureLevelForMBSConstPartList() : DEFAULT_MAX_CONST_PARTS_LIST_STRUCTURE_LEVEL;
    }

    public static iPartsSearchVirtualDataset searchPartListEntriesForMBSPartLists(AssemblyId optionalRootAssemblyId,
                                                                                  boolean isSearchValuesDisjunction,
                                                                                  EtkDisplayFields selectFields,
                                                                                  List<String> selectValues,
                                                                                  EtkDisplayFields whereFields,
                                                                                  List<String> whereValues,
                                                                                  boolean andOrSearch,
                                                                                  EtkProject project,
                                                                                  WeakKeysMap<String, String> multiLanguageCache,
                                                                                  WildCardSettings wildCardSettings) {
        return new iPartsVirtualMaterialSearchDataset(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SUB_SNR, optionalRootAssemblyId,
                                                      isSearchValuesDisjunction, selectFields, selectValues,
                                                      whereFields, whereValues, andOrSearch, project, multiLanguageCache,
                                                      wildCardSettings) {

            protected int fieldIndexPartNumber;


            @Override
            protected void addNeededJoins(PartsSearchSqlSelect partsSearchSqlSelect, List<String> doNotJoinList) {

            }

            @Override
            protected void addAdditionalSelectFields(EtkDisplayFields selectFieldsWithoutKatalog, List<String> selectValuesWithoutKatalog) {
                fieldIndexPartNumber = selectFieldsWithoutKatalog.size();
                selectFieldsWithoutKatalog.addFeld(new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SUB_SNR, false, false));
                selectValuesWithoutKatalog.add("");
            }

            @Override
            protected String getPartNumber(List<String> values) {
                return values.get(fieldIndexPartNumber);
            }

            @Override
            protected HierarchicalIDWithType createParentId(List<String> values) {
                return null;
            }

            @Override
            protected List<EtkDataPartListEntry> searchResultPartListEntries(List<String> values, String partNumber, HierarchicalIDWithType parentId) {
                List<EtkDataPartListEntry> resultPartListEntries = new ArrayList<>();

                List<MechanicUsagePosition> usagePositions = new ArrayList<>();

                // Suche die Verwendung dieses Materials in der MBS-Stückliste. Wir verwenden diese Funktion, weil dort schon das mit der Hirarchie
                // der oberen und unteren Sachnummer berücksichtigt wird.
                // Von der Performance ist das nicht ideal, aber die Rekursion muss irgendwo durchlaufen werden
                getMechanicUsageForMBSMaterial(new PartId(partNumber, ""), true, project, usagePositions);

                for (MechanicUsagePosition usagePosition : usagePositions) {
                    createAndAddSearchResultPartListEntry(usagePosition.getParentAssemblyId(),
                                                          usagePosition.getInternChildPartListEntryId().getKLfdnr(),
                                                          partNumber, resultPartListEntries);
                }

                return resultPartListEntries;
            }
        };
    }

    /**
     * Sucht in der kompletten MBS Struktur nach den Werten aus der Suche
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
    public static iPartsSearchVirtualDataset searchPartListEntriesWithinMBSStruct(final EtkDisplayFields selectFields,
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
                // Für die Anzeige der Stücklisten in der MBS Konstruktionssicht müssen vorher die zu zeigenden
                // Konstruktionsbaumuster ausgewählt werden. Die Suche soll auch nur diese Baumuster berücksichtigen.
                // Wenn keine Baumuster für die MBS Konstruktionssicht gewählt wurden, braucht man hier nicht weitermachen.
                Map<String, Set<String>> selectedModelsMap = SessionKeyHelper.getSelectedMBSModelMapWithUserSettingsCheck(project);
                if ((selectedModelsMap == null) || selectedModelsMap.isEmpty()) {
                    return result;
                }

                // Es sollen nur die Baumuster betrachtet werden, die der Benutzer in der MBS Konstruktionssicht ausgewählt hat
                for (Set<String> models : selectedModelsMap.values()) {
                    for (String model : models) {
                        // Jetzt einfach alle Treffer aus der MBS Struktur holen
                        MBSStructure structure = MBSStructure.getInstance(project, new iPartsModelId(model));
                        List<MBSStructureNode> mbsNodes = structure.search(selectFields, selectValues, whereFields,
                                                                           andOrWhereValues, andOrSearch, wildCardSettings,
                                                                           project.getDBLanguage(), project.getDataBaseFallbackLanguages());
                        iPartsMBSModel mbsModel = iPartsMBSModel.getInstance(project, new iPartsModelId(model));

                        if (!mbsNodes.isEmpty()) {
                            Set<String> validModelList = new HashSet<>();

                            if (model != null) {
                                validModelList.add(model);
                            }
                            // Und jetzt alle gültigen Knoten einfügen. Dazu die ParentAssembly ermitteln und davon alle
                            // Childs für die spätere Filterung zurückliefern
                            Set<AssemblyId> resultAssemblyIds = new HashSet<>(); // Set mit allen AssemblyIds, um doppeltes Laden zu vermeiden
                            Set<MBSStructureId> invalidNodes = new HashSet<>();
                            for (MBSStructureNode structureNode : mbsNodes) {
                                if (!isValidNode(structureNode, invalidNodes, mbsModel)) {
                                    continue;
                                }
                                for (String modelNo : validModelList) {
                                    if (Session.currentSessionThreadAppActionCancelled()) {
                                        throw new CanceledException(null);
                                    }
                                    List<iPartsVirtualNode> nodes = new ArrayList<>();
                                    nodes.add(new iPartsVirtualNode(iPartsNodeType.MBS_STRUCTURE, new iPartsModelId(modelNo)));
                                    if (structureNode.getParent() != null) {
                                        nodes.add(new iPartsVirtualNode(iPartsNodeType.MBS, structureNode.getParent().getId()));
                                    }
                                    AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
                                    if (!resultAssemblyIds.contains(assemblyId)) {
                                        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);

                                        if (assembly.existsInDB()) {
                                            // Die ungefilterte Stückliste vom MBS Knoten zurückliefern
                                            // (darunter befindet sich auch der gesuchte MBS Knoten)
                                            DBDataObjectList<EtkDataPartListEntry> objectList = assembly.getPartListUnfiltered(null);
                                            result.addAll(objectList.getAsList());
                                        }
                                        resultAssemblyIds.add(assemblyId);
                                    }
                                }
                            }
                        }
                    }
                }
                return result;
            }

            /**
             * Check, ob der Knoten inkl aller Elternknoten bis zum Root valide sind
             * @param structureNode
             * @param invalidNodes
             * @param mbsModel
             * @return
             */
            private boolean isValidNode(MBSStructureNode structureNode, Set<MBSStructureId> invalidNodes, iPartsMBSModel mbsModel) {
                MBSStructureId structureId = structureNode.getId();
                if (invalidNodes.contains(structureId)) {
                    return false;
                }
                iPartsDataMBSStructure mbsStructureObject = mbsModel.getDataObjectForStructureId(structureId);
                if (!iPartsMBSHelper.isValidDataset(mbsStructureObject)) {
                    invalidNodes.add(structureId);
                    return false;
                }
                AbstractiPartsNode<MBSStructureNodes, MBSStructureNode, MBSStructureType> parent = structureNode.getParent();
                if (parent instanceof MBSStructureNode) {
                    return isValidNode((MBSStructureNode)structureNode.getParent(), invalidNodes, mbsModel);
                }
                return true;
            }
        };
    }

    /**
     * Liefert die Verwendungsstellen {@link MechanicUsagePosition} für das übergebene Teil innerhalb der MBS Stücklisten
     *
     * @param partId
     * @param filtered
     * @param project
     * @param result
     */
    public static void getMechanicUsageForMBSMaterial(PartId partId, boolean filtered, EtkProject project, List<MechanicUsagePosition> result) {
        Map<String, Set<String>> selectedModelsMap = SessionKeyHelper.getSelectedMBSModelMapWithUserSettingsCheck(project);
        if ((selectedModelsMap == null) || selectedModelsMap.isEmpty()) {
            return;
        }
        // Hole alle ConGroups (untere Ebene in der MBS Struktur) zu dem übergebenen Teil bis zur maximal möglichen Strukturstufe.
        // Hierbei werden via DB Zugriffe // die Strukturstufen von unten nach oben durchlaufen bis wir auf die höchste
        // obere Sachnummer treffen. Diese ist // dann die ConGroup aus der MBS Struktur.
        Set<String> parentNumbers = getAllParentNumbersOfMBSMat(project, partId.getMatNr(), 1, getMaxLevelFromConfig());
        Set<AssemblyId> resultAssemblyIds = new HashSet<>(); // Set mit allen AssemblyIds, um doppeltes Laden zu vermeiden
        // Es sollen nur die Baumuster betrachtet werden, die der Benutzer in der MBS Konstruktionssicht ausgewählt hat
        MechanicUsagePositionCreator mupCreator = new MechanicUsagePositionCreator(project, partId, result, parentNumbers, resultAssemblyIds, filtered);
        for (Set<String> models : selectedModelsMap.values()) {
            for (String model : models) {
                iPartsModelId modelId = new iPartsModelId(model);
                iPartsMBSModel mbsModel = iPartsMBSModel.getInstance(project, modelId);
                for (iPartsCatalogNode node : mbsModel.getCompleteMBSStructureFiltered(project).getChildren()) {
                    searchMechanicUsagePosition(node, modelId, mupCreator);
                }
            }
        }
    }

    /**
     * Bestimmt alle ConGroups (untere Ebene in der MBS Struktur) zu dem übergebenen Teil. Hierbei werden via DB Zugriffe
     * die Strukturstufen von unten nach oben durchlaufen bis wir auf die höchste obere Sachnummer treffen. Diese ist
     * dann die ConGroup aus der MBS Struktur.
     *
     * @param project
     * @param partNo
     * @return
     */
    private static Set<String> getAllParentNumbersOfMBSMat(EtkProject project, String partNo, int level, int maxLevel) {
        Set<String> result = new LinkedHashSet<>();
        if (level > maxLevel) {
            return result;
        }
        DBSQLQuery query = iPartsMBSHelper.createQueryForValidMBSPartsListData(project, partNo);

        DBDataSetCancelable dataSet = null;
        try {
            // Query ausführen
            dataSet = query.executeQueryCancelable();
            if (dataSet != null) {
                while (dataSet.next()) {
                    String upperNumber = dataSet.getStringList().get(0);
                    if (!result.contains(upperNumber)) {
                        // Jetzt rekursiv die Ebenen durchgehen
                        Set<String> parentResults = getAllParentNumbersOfMBSMat(project, upperNumber, level + 1, maxLevel);
                        // Hat das Teil keine obere Sachnummer, dann ist es selber die oberste Sachnummer
                        if (parentResults.isEmpty()) {
                            result.add(upperNumber);
                        } else {
                            result.addAll(parentResults);
                        }
                    }
                }
            }
        } catch (CanceledException e) {
            Logger.getLogger().throwRuntimeException(e);
        } finally {
            // Verbindung schließen
            if (dataSet != null) {
                dataSet.close();
            }
        }
        return result;
    }

    /**
     * Sucht im übergeben Knoten und in seinen Kindknoten nach ConGroup Nummern in denen die gesuchte Teileposition
     * vorkommt.
     * Enthält der übergebene Knoten eine ConGroup, die eine von den gesuchten ConGroups ist, dann wird aus dem Knoten
     * ein Treffer erzeugt.
     *
     * @param node
     * @param modelId
     * @param mupCreationData
     */
    private static void searchMechanicUsagePosition(iPartsCatalogNode node, iPartsModelId modelId,
                                                    MechanicUsagePositionCreator mupCreationData) {
        if (node.getId() instanceof MBSStructureId) {
            MBSStructureId structureId = (MBSStructureId)node.getId();
            if (structureId.isListNumberNode()) {
                // Der Knoten ist ein ListNumber Knoten (erste Ebene der MBS Struktur). Durchsuche alle Kindknoten (ConGroups)
                for (iPartsCatalogNode conGroupNode : node.getChildren()) {
                    searchMechanicUsagePosition(conGroupNode, modelId, mupCreationData);
                }
            } else {
                // Der Knoten ist ein ConGroup Knoten (zweite Ebene der MBS Struktur). Prüfe, ob es eine von den gesuchten
                // ConGroups ist
                if (mupCreationData.isFoundConGroup(structureId)) {
                    mupCreationData.createMechanicUsagePositioFromVirtualNodes(structureId, modelId);
                }
            }
        }
    }

    /**
     * Hilfsklasse zum Halten aller Stücklistendaten zu einer oberen Sachnummer
     */
    private static class MBSPartsListRowData {

        private DBDataObjectAttributes partsListAttributes = new DBDataObjectAttributes();
        private DBDataObjectAttributes matAttributes = new DBDataObjectAttributes();
        private boolean hasSubStruct = false;
    }

    /**
     * Hilfsklasse zum Erzeugen von {@link MechanicUsagePosition} auf Basis von gefundenen MBS Strukturknoten und dem
     * ausgewählten Baumuster
     */
    private static class MechanicUsagePositionCreator {

        private EtkProject project;
        private PartId partId;
        private List<MechanicUsagePosition> result;
        private Set<String> parentNumbers;
        private Set<AssemblyId> resultAssemblyIds;
        private boolean filtered;

        public MechanicUsagePositionCreator(EtkProject project, PartId partId, List<MechanicUsagePosition> result,
                                            Set<String> parentNumbers, Set<AssemblyId> resultAssemblyIds, boolean filtered) {
            this.project = project;
            this.partId = partId;
            this.result = result;
            this.parentNumbers = parentNumbers;
            this.resultAssemblyIds = resultAssemblyIds;
            this.filtered = filtered;
        }

        /**
         * Erzeugt auf Basis der übergebenen MBS Struktur und Baumuster den dazugehörigen Verwendungsstellentreffer
         *
         * @param structureId
         * @param modelId
         */
        private void createMechanicUsagePositioFromVirtualNodes(MBSStructureId structureId, iPartsModelId modelId) {
            EtkDataPartListEntry parentAssembly
                    = iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(null,
                                                                                          filtered,
                                                                                          project,
                                                                                          new iPartsVirtualNode(iPartsNodeType.MBS_STRUCTURE, modelId), // Baumuster-Knoten
                                                                                          new iPartsVirtualNode(iPartsNodeType.MBS, structureId)); // MBSStructure-Knoten
            if (parentAssembly != null) {
                AssemblyId assemblyId = parentAssembly.getOwnerAssemblyId();
                if (resultAssemblyIds.add(assemblyId)) {
                    // Jetzt diese Stückliste laden und testen, ob nach der Filterung über das aktuelle Datum das Teil noch drin ist
                    EtkDataAssembly parent = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
                    List<EtkDataPartListEntry> entries;
                    if (filtered) {
                        entries = parent.getPartList(null);
                    } else {
                        entries = parent.getPartListUnfiltered(null).getAsList();
                    }
                    for (EtkDataPartListEntry entry : entries) {
                        if (entry.getPart().getAsId().equals(partId)) {
                            String quantity = entry.getFieldValue(FIELD_K_MENGE);
                            result.add(MechanicUsagePosition.createAsPartsEntry(parent.getAsId(), partId, entry.getAsId(), quantity));
                        }
                    }
                }
            }
        }

        public boolean isFoundConGroup(MBSStructureId structureId) {
            return structureId.isConGroupNode() && parentNumbers.contains(structureId.getConGroup());
        }
    }
}
