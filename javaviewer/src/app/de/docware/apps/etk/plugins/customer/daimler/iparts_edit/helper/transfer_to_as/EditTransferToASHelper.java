/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as;

import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualCalcFieldDocuRel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementKEMHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsModelYearCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacementConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditTransferPartlistPredictionGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.CopyAndPasteData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditEqualizeFootNoteHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditValidationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.hierarchycalcualtion.HierarchyStructuresForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sort.SortBetweenHelper;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.stream.Collectors;

public class EditTransferToASHelper implements iPartsConst {

    private static final String MATERIAL_ETKZ_CHECK_VALUE = "E";
    private static final int EXTRA_INCREMENT_FOR_PROGRESS = 5;

    private final EtkProject project;
    private final EtkDataAssembly sourceConstAssembly;
    private final IdWithType sourceId; // ID des aktuellen virtuellen Knotens (DIALOG: HM/M/SM; EDS: SAA)
    private EtkMessageLog messageLog;
    private final Map<String, String> matNoTextIdMap;
    private OnModifyCreatedRetailPartListEntryEvent onModifyCreatedRetailPartListEntryEvent;
    private boolean showOnlyProgress;
    private VarParam<Boolean> isCancelledVarParam;
    private boolean withLinkingChanges;
    private ImportExportLogHelper logHelper;


    /**
     * Alle Konstruktions-Ersetzungen aufsammeln und zurückliefern inkl. Berechnung der KEM-Ketten für die Vorgänger und
     * Nachfolger, damit die Konstruktions-Ersetzungen im Nachgang in Retail-Ersetzungen umgewandelt werden können
     *
     * @param constPLEsForKemChain       Konstruktions-Stücklisteneinträge (Quelle) für die KEM-Kette
     * @param constDialogPositionsHelper
     * @return
     */
    public static Set<iPartsReplacementConst> getConstructionReplacementsForKemChain(Set<EtkDataPartListEntry> constPLEsForKemChain,
                                                                                     iPartsDIALOGPositionsHelper constDialogPositionsHelper) {
        Set<iPartsReplacementConst> replacementsConstToTransfer = new HashSet<>();
        for (EtkDataPartListEntry sourcePLEForKemChain : constPLEsForKemChain) {
            if ((sourcePLEForKemChain instanceof iPartsDataPartListEntry)) {
                iPartsDataPartListEntry iPartsSourcePLE = (iPartsDataPartListEntry)sourcePLEForKemChain;
                if (iPartsSourcePLE.hasPredecessorsConst()) {
                    Collection<iPartsReplacementConst> predecessorsConst = iPartsSourcePLE.getPredecessorsConst();
                    for (iPartsReplacementConst replacementConst : predecessorsConst) {
                        EditConstructionToRetailHelper.calculateMinMaxKEMDates(replacementConst.predecessorEntry,
                                                                               constDialogPositionsHelper);
                    }
                    replacementsConstToTransfer.addAll(predecessorsConst);
                }
                if (iPartsSourcePLE.hasSuccessorsConst()) {
                    Collection<iPartsReplacementConst> successorsConst = iPartsSourcePLE.getSuccessorsConst();
                    for (iPartsReplacementConst replacementConst : successorsConst) {
                        EditConstructionToRetailHelper.calculateMinMaxKEMDates(replacementConst.successorEntry,
                                                                               constDialogPositionsHelper);
                    }
                    replacementsConstToTransfer.addAll(successorsConst);
                }
            }
        }
        return replacementsConstToTransfer;
    }

    /**
     * Fügt die übergebenen Konstruktions-Ersetzungen als Retail-Ersetzungen zur Stückliste hinzu falls nicht bereits vorhanden.
     *
     * @param replacementsConstToTransfer
     * @param destPartList
     * @param destPartListSourceGUIDMap
     * @param dataReplacementsRetail
     * @param dataIncludePartsRetail
     * @param project
     */
    public static void addConstReplacementsToRetailPartList(Set<iPartsReplacementConst> replacementsConstToTransfer,
                                                            DBDataObjectList<EtkDataPartListEntry> destPartList,
                                                            HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap,
                                                            iPartsDataReplacePartList dataReplacementsRetail,
                                                            iPartsDataIncludePartList dataIncludePartsRetail,
                                                            EtkProject project) {
        if (!replacementsConstToTransfer.isEmpty()) {
            Map<String, EtkDataPartListEntry> destPartListKLfdnrMap = iPartsReplacementHelper.createLfdNrToPartlistEntryMap(destPartList);
            iPartsReplacementKEMHelper replacementKEMHelper = new iPartsReplacementKEMHelper(destPartList);
            for (iPartsReplacementConst replacementConst : replacementsConstToTransfer) {
                convertConstToRetailReplacements(replacementConst, dataReplacementsRetail, dataIncludePartsRetail,
                                                 replacementKEMHelper, destPartListKLfdnrMap, destPartListSourceGUIDMap, project);
            }
        }
    }

    private static void convertConstToRetailReplacements(iPartsReplacementConst replacementConst, iPartsDataReplacePartList replacementsRetail,
                                                         iPartsDataIncludePartList includePartsRetail, iPartsReplacementKEMHelper replacementHelper,
                                                         Map<String, EtkDataPartListEntry> destPartListKLfdnrMap,
                                                         HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap,
                                                         EtkProject project) {
        if ((replacementConst.predecessorEntry == null) || (replacementConst.successorEntry == null)) {
            return;
        }

        Map<String, EtkDataPartListEntry> hotspotToBestPredecessorRetailMap = replacementHelper.getBestPredecessorForHotspotMap(replacementConst,
                                                                                                                                destPartListSourceGUIDMap
        );

        Map<String, EtkDataPartListEntry> hotspotToBestSuccessorRetailMap = replacementHelper.getBestSuccessorForHotspotMap(replacementConst,
                                                                                                                            destPartListSourceGUIDMap
        );

        if ((hotspotToBestPredecessorRetailMap.isEmpty()) || (hotspotToBestSuccessorRetailMap.isEmpty())) {
            return;
        }

        // Bereits vorhandene Ersetzungen (inkl. manueller Änderungen durch Autoren) mit Berücksichtigung der Hotspots suchen,
        // die aber einen "schlechteren" Vorgänger- bzw. Nachfolgerstand verwenden
        final String POS_DELIMITER = "|";
        String predecessorGUID = replacementConst.predecessorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
        String successorGUID = replacementConst.successorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
        Map<String, List<iPartsDataReplacePart>> existingMatchingReplacementsMap = new HashMap<>();
        for (iPartsDataReplacePart existingReplacePart : replacementsRetail) {
            if (existingReplacePart.isSameReplacementInConstruction(predecessorGUID, successorGUID)) {
                EtkDataPartListEntry predecessorPLE = destPartListKLfdnrMap.get(existingReplacePart.getPredecessorPartListEntryId().getKLfdnr());
                if (predecessorPLE != null) {
                    EtkDataPartListEntry successorPLE = destPartListKLfdnrMap.get(existingReplacePart.getSuccessorPartListEntryId().getKLfdnr());
                    if (successorPLE != null) {
                        String existingMatchingReplacementKey = predecessorPLE.getFieldValue(iPartsConst.FIELD_K_POS) + POS_DELIMITER
                                                                + successorPLE.getFieldValue(iPartsConst.FIELD_K_POS);
                        List<iPartsDataReplacePart> existingMatchingReplacementsList = existingMatchingReplacementsMap.get(existingMatchingReplacementKey);
                        if (existingMatchingReplacementsList == null) {
                            existingMatchingReplacementsList = new ArrayList<>();
                            existingMatchingReplacementsMap.put(existingMatchingReplacementKey, existingMatchingReplacementsList);
                        }
                        existingMatchingReplacementsList.add(existingReplacePart);
                    }
                }
            }
        }

        // Neue Ersetzungen anlegen bzw. die besseren Vorgänger- bzw. Nachfolgerstände setzen
        for (Map.Entry<String, EtkDataPartListEntry> hotspotAndSuccessorRetail : hotspotToBestSuccessorRetailMap.entrySet()) {
            String successorHotspot = hotspotAndSuccessorRetail.getKey();
            EtkDataPartListEntry successorRetail = hotspotAndSuccessorRetail.getValue();

            // Entsprechende Vorgänger sind in Retail-Stückliste enthalten --> für jeden existierenden Eintrag eine Ersetzung
            // erzeugen bzw. eine vorhandene anpassen
            for (Map.Entry<String, EtkDataPartListEntry> hotspotAndPredecessorRetail : hotspotToBestPredecessorRetailMap.entrySet()) {
                String predecessorHotspot = hotspotAndPredecessorRetail.getKey();
                EtkDataPartListEntry predecessorRetail = hotspotAndPredecessorRetail.getValue();

                String existingMatchingReplacementKey = predecessorHotspot + POS_DELIMITER + successorHotspot;
                List<iPartsDataReplacePart> existingMatchingReplacementsList = existingMatchingReplacementsMap.get(existingMatchingReplacementKey);
                if (existingMatchingReplacementsList != null) {
                    for (iPartsDataReplacePart existingMatchingReplacement : existingMatchingReplacementsList) {
                        // Existiert die Ersetzung schon für die besten Vorgänger- bzw. Nachfolgerstände?
                        if (predecessorRetail.getAsId().equals(existingMatchingReplacement.getPredecessorPartListEntryId())
                            && successorRetail.getAsId().equals(existingMatchingReplacement.getSuccessorPartListEntryId())) {
                            continue;
                        }

                        // Besseren Vorgänger- und Nachfolgerstand setzen (zuerst bei den Mitlieferteilen und danach erst
                        // bei der eigentlichen Ersetzung, damit die Mitlieferteile korrekt gefunden werden können)
                        String bestPredecessorKLfdnr = predecessorRetail.getAsId().getKLfdnr();
                        String bestSuccessorKLfdnr = successorRetail.getAsId().getKLfdnr();
                        for (iPartsDataIncludePart dataIncludePart : includePartsRetail) {
                            if (dataIncludePart.isValidForReplacement(existingMatchingReplacement)) {
                                dataIncludePart.setFieldValue(iPartsConst.FIELD_DIP_LFDNR, bestPredecessorKLfdnr, DBActionOrigin.FROM_EDIT);
                                dataIncludePart.setFieldValue(iPartsConst.FIELD_DIP_REPLACE_LFDNR, bestSuccessorKLfdnr, DBActionOrigin.FROM_EDIT);
                            }
                        }
                        existingMatchingReplacement.setFieldValue(iPartsConst.FIELD_DRP_LFDNR, bestPredecessorKLfdnr, DBActionOrigin.FROM_EDIT);
                        existingMatchingReplacement.setFieldValue(iPartsConst.FIELD_DRP_REPLACE_LFDNR, bestSuccessorKLfdnr, DBActionOrigin.FROM_EDIT);
                    }
                } else {
                    // Ersetzungs- und Mitlieferteile-Datenobjekte erstellen, die am Ende in der DB gespeichert werden.
                    // Laut Daimler (siehe DAIMLER-9182) auch für verschiedene Hotspots von Vorgänger und Nachfolger
                    iPartsDataReplacePart replacement = EditConstructionToRetailHelper.createAndAddRetailReplacement(project,
                                                                                                                     replacementsRetail,
                                                                                                                     includePartsRetail,
                                                                                                                     predecessorRetail,
                                                                                                                     successorRetail,
                                                                                                                     replacementConst);
                    if (replacement != null) {
                        // Die neue Ersetzung freigeben
                        iPartsReplacementHelper.updateStates(replacementsRetail, replacement, true);
                    }
                }
            }
        }
    }

    /**
     * überprüft, ob ein Modul im Editor geladen ist
     *
     * @param assemblyId
     * @param moduleInfosInEditor
     * @return
     */
    public static boolean isLoadedInEditor(AssemblyId assemblyId, List<EditModuleForm.EditModuleInfo> moduleInfosInEditor) {
        if (!moduleInfosInEditor.isEmpty()) {
            for (EditModuleForm.EditModuleInfo moduleInfo : moduleInfosInEditor) {
                if (moduleInfo.getAssemblyId().equals(assemblyId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * bestimmt die {@link }EditModuleForm}
     *
     * @return
     */
    public static EditModuleForm getEditModuleForm(AbstractJavaViewerFormIConnector connector) {
        JavaViewerMainWindow mainWindow = connector.getMainWindow();
        if (mainWindow != null) {
            List<AbstractJavaViewerMainFormContainer> editModuleForms = mainWindow.getFormsFromClass(EditModuleForm.class);
            if (!editModuleForms.isEmpty()) {
                return (EditModuleForm)editModuleForms.get(0);
            }
        }
        return null;
    }


    /**
     * Liefert die Liste der im Editor geladenen und editierbaren Module
     *
     * @return
     */
    public static Collection<EditModuleForm.EditModuleInfo> getEditModuleInfoList(AbstractJavaViewerFormIConnector connector) {
        EditModuleForm editModuleForm = getEditModuleForm(connector);
        if (editModuleForm != null) {
            return editModuleForm.getEditModuleInfoList(false);
        }
        return Collections.unmodifiableList(new DwList<>());
    }

    public EditTransferToASHelper(EtkProject project, EtkDataAssembly sourceConstAssembly, IdWithType sourceId) {
        this.project = project;
        this.sourceConstAssembly = sourceConstAssembly;
        this.sourceId = sourceId;
        this.matNoTextIdMap = new HashMap();
        this.messageLog = null;
        this.onModifyCreatedRetailPartListEntryEvent = null;
        this.showOnlyProgress = false;
        this.isCancelledVarParam = null;
        this.withLinkingChanges = false;
    }

    public EtkProject getProject() {
        return project;
    }

    public OnModifyCreatedRetailPartListEntryEvent getOnModifyCreatedRetailPartListEntryEvent() {
        return onModifyCreatedRetailPartListEntryEvent;
    }

    public void setOnModifyCreatedRetailPartListEntryEvent(OnModifyCreatedRetailPartListEntryEvent onModifyCreatedRetailPartListEntryEvent) {
        this.onModifyCreatedRetailPartListEntryEvent = onModifyCreatedRetailPartListEntryEvent;
    }

    public void setShowOnlyProgress(boolean showOnlyProgress) {
        this.showOnlyProgress = showOnlyProgress;
    }

    public void setIsCancelledVarParam(VarParam<Boolean> isCancelledVarParam) {
        this.isCancelledVarParam = isCancelledVarParam;
    }

    private boolean isCancelled() {
        if (isCancelledVarParam != null) {
            return isCancelledVarParam.getValue();
        }
        return false;
    }

    public void createNewModules(Map<String, TransferToASElement> notExistingModuleMap, Map<String, iPartsDataModule> moduleMetaDataMap) throws EditTransferPartListEntriesException {
        createNewModules(notExistingModuleMap, moduleMetaDataMap, null);
    }

    /**
     * Nicht existierende Module erzeugen.
     *
     * @param notExistingModuleMap Map von Ziel-Modulnummer auf {@link TransferToASElement}
     * @param moduleMetaDataMap    Optionale Map von Ziel-Modulnummer auf die Modul-Metadaten für das jeweilige neue Modul
     * @param techChangeSet        Nur besetzt bei AutoTransferPLEsExtendedWholeProduct
     * @throws EditTransferPartListEntriesException Exception falls beim Erzeugen von einem der Module ein Fehler aufgetreten ist
     */
    public void createNewModules(Map<String, TransferToASElement> notExistingModuleMap, Map<String, iPartsDataModule> moduleMetaDataMap,
                                 iPartsRevisionChangeSet techChangeSet) throws EditTransferPartListEntriesException {
        if (!notExistingModuleMap.isEmpty()) {
            // Neue Module erzeugen und in DB speichern
            for (Map.Entry<String, TransferToASElement> entry : notExistingModuleMap.entrySet()) {
                TransferToASElement transferElem = entry.getValue();
                iPartsProduct product = transferElem.getProduct();
                iPartsDataAssembly newAssembly = null;
                if (product != null) {
                    // Neues Modul für Produkt - KG/TU erzeugen
                    iPartsProductId productId = product.getAsId();
                    iPartsDocumentationType documentationType = product.getDocumentationType();
                    if (documentationType == iPartsDocumentationType.UNKNOWN) {
                        documentationType = iPartsDocumentationType.DIALOG_IPARTS;
                    }
                    iPartsModuleTypes moduleType = documentationType.getModuleType(false);

                    KgTuId kgTuId = transferElem.getKgTuId();
                    AssemblyId assemblyId = new AssemblyId(entry.getKey(), "");
                    Map<String, KgTuListItem> kgtuCacheEntry = KgTuHelper.getKGTUStructure(getProject(), productId);
                    KgTuListItem kgTuListItem = getKgTuListItem(kgTuId, kgtuCacheEntry);

                    // Modul-Metadaten kopieren
                    iPartsDataModule moduleMetaData = null;
                    boolean moduleIsSpringFilterRelevant = false;
                    DCAggregateTypes aggregateTypeForZBFilter = DCAggregateTypes.UNKNOWN;
                    if (moduleMetaDataMap != null) {
                        moduleMetaData = moduleMetaDataMap.get(entry.getKey());
                        if (moduleMetaData != null) {
                            moduleIsSpringFilterRelevant = moduleMetaData.isSpringFilterRelevant();
                            aggregateTypeForZBFilter = moduleMetaData.getAggTypeForSpecialZBFilter();
                            moduleMetaData = moduleMetaData.cloneMe(project);

                            // Neue Modul-ID setzen und Modul-Meta-Daten als neu markieren
                            moduleMetaData.setId(new iPartsModuleId(assemblyId.getKVari()), DBActionOrigin.FROM_EDIT);
                            moduleMetaData.updateOldId();
                            moduleMetaData.__internal_setNew(true);

                            // Bei PSK-Ziel-Produkten die Original-Doku-Methode vom Quell-Modul verwenden
                            if (product.isPSK() && (moduleMetaData.getDocumentationType() != iPartsDocumentationType.UNKNOWN)) {
                                documentationType = moduleMetaData.getDocumentationType();
                            }

                            moduleMetaData.setDbDocuType(documentationType.getDBValue(), DBActionOrigin.FROM_EDIT);
                        }

                        KgTuListItem destKgListItem = kgtuCacheEntry.get(kgTuId.getKg());
                        // Kontrolle, ob KG-Knoten vorhanden ist
                        boolean destKgExistsInProduct = false;
                        if (destKgListItem != null) {
                            destKgExistsInProduct = destKgListItem.isSourceProduct();
                        }
                        // Bei PSK-Ziel-Produkten soll das Template nicht beachtet werden -> Selber KG/TU-Knoten anlegen
                        if (kgTuListItem == null) {
                            KgTuNode kgNode = new KgTuNode(KgTuType.KG, kgTuId.getKg(), null);
                            KgTuNode tuNode = new KgTuNode(KgTuType.TU, kgTuId.getTu(), kgNode);
                            KgTuListItem kgTuListItemParent;
                            // Falls KG im Ziel-Produkt schon vorhanden ist, dann nicht überschreiben
                            if (destKgExistsInProduct) {
                                kgTuListItemParent = destKgListItem;
                            } else {
                                kgTuListItemParent = new KgTuListItem(kgNode, KgTuListItem.Source.PRODUCT, true);
                                kgTuListItemParent.setPskNature(KgTuListItem.PSK_NATURE.PSK_NEW_NODE);
                            }
                            kgTuListItem = new KgTuListItem(tuNode, KgTuListItem.Source.PRODUCT, kgTuListItemParent, false);
                            kgTuListItem.setPskNature(KgTuListItem.PSK_NATURE.PSK_NEW_NODE);
                        }
                        // Bei PSK-Ziel-Produkten soll der Original-Titel vom Quell-Modul verwendet werden
                        // Bei der KG aber nur falls diese im Ziel-Produkt noch nicht schon vorhanden ist
                        if (!destKgExistsInProduct) {
                            EtkMultiSprache tempKgTitle = transferElem.sourceKgTitle;
                            if (tempKgTitle != null) {
                                kgTuListItem.getParent().getKgTuNode().setTitle(tempKgTitle);
                            }
                        }
                        // TU kann im Ziel-Produkt nicht vorhanden sein -> Titel kommt immer von der Quelle
                        EtkMultiSprache tempTuTitle = transferElem.sourceTuTitlesMap.get(kgTuId);
                        if (tempTuTitle != null) {
                            kgTuListItem.getKgTuNode().setTitle(tempTuTitle);
                        }
                    }

                    if (kgTuListItem != null) {
                        newAssembly = EditModuleHelper.createAndSaveModuleWithKgTuAssignment(assemblyId,
                                                                                             moduleType,
                                                                                             kgTuListItem.getKgTuNode().getTitle(),
                                                                                             productId,
                                                                                             kgTuId,
                                                                                             getProject(),
                                                                                             documentationType,
                                                                                             moduleIsSpringFilterRelevant,
                                                                                             aggregateTypeForZBFilter,
                                                                                             true, techChangeSet);

                        // Explizite Modul-Meta-Daten speichern falls vorhanden
                        if (moduleMetaData != null) {
                            if (project.isRevisionChangeSetActiveForEdit()) {
                                project.addDataObjectToActiveChangeSetForEdit(moduleMetaData);
                            } else {
                                if (techChangeSet != null) {
                                    techChangeSet.addDataObject(moduleMetaData, false, false, false);
                                }
                                moduleMetaData.saveToDB();
                            }
                        }
                        kgTuListItem.saveToDB(getProject(), productId.getProductNumber(), techChangeSet);
                    } else {
                        transferElem.setAssemblyId(null);
                        throw new EditTransferPartListEntriesException(TranslationHandler.translate("!!Fehler beim Anlegen vom Modul \"%1\". Der KG/TU-Knoten \"%2\" kann nicht angelegt werden!",
                                                                                                    assemblyId.getKVari(), kgTuId.toStringForLogMessages()));
                    }
                } else if (transferElem.getSaModuleNumber() != null) {
                    // Neues SA-Modul erzeugen
                    // SA Benennung als TU Benennung übernehmen, falls vorhanden
                    EtkMultiSprache saDesc = null;
                    String saaNumber;
                    if (sourceId instanceof MBSStructureId) {
                        MBSStructureId mbsStructureId = (MBSStructureId)(sourceId);
                        MBSStructure mbsStructure = MBSStructure.getInstance(getProject());
                        MBSStructureNode node = mbsStructure.getNode(mbsStructureId.getParentId());
                        if (node != null) {
                            saDesc = node.getTitle(getProject());
                        }
                        saaNumber = mbsStructureId.getListNumber();
                    } else if (sourceId instanceof EdsSaaId) {
                        EdsSaaId saaId = (EdsSaaId)(sourceId);
                        saaNumber = saaId.getSaaNumber();
                    } else {
                        throw new EditTransferPartListEntriesException(TranslationHandler.translate(
                                "!!Fehler bei der Bestimmung der SA-Nummer!"));
                    }

                    iPartsModuleId moduleId = new iPartsModuleId(transferElem.getSaModuleNumber());
                    AssemblyId assemblyId = new AssemblyId(moduleId.getModuleNumber(), "");
                    String saNumber = iPartsNumberHelper.convertSAAtoSANumber(saaNumber);
                    // neue Assembly erzeugen und speichern (inkl. Einträge in KATALOG, MAT, DA_MODULE, DA_SA_MODULES)
                    iPartsDocumentationType docuType = iPartsDocumentationType.BCS_PLUS;
                    newAssembly = EditModuleHelper.createAndSaveModuleWithSAAssignment(assemblyId,
                                                                                       iPartsModuleTypes.SA_TU,
                                                                                       saDesc,
                                                                                       null,
                                                                                       new iPartsSAModulesId(saNumber),
                                                                                       null,
                                                                                       getProject(),
                                                                                       docuType,
                                                                                       iPartsImportDataOrigin.IPARTS,
                                                                                       true, techChangeSet);

                    // jetzt noch die restlichen Werte für DA_MODULE speichern
                    iPartsDataModule moduleMetaData = newAssembly.getModuleMetaData();
                    if (techChangeSet != null) {
                        techChangeSet.addDataObject(moduleMetaData, false, false, false);
                    }
                    moduleMetaData.saveToDB();
                }
                if (newAssembly != null) {
                    transferElem.setAssemblyId(newAssembly.getAsId());
                    if (iPartsConst.ONLY_SINGLE_MODULE_PER_KGTU) {
                        iPartsDataReservedPKList.reservePrimaryKey(getProject(), newAssembly.getAsId());
                    }
                } else {
                    transferElem.setAssemblyId(null);
                    throw new EditTransferPartListEntriesException(TranslationHandler.translate("!!Fehler beim Anlegen vom Modul!"));
                }
            }
        }
    }

    /**
     * Stücklisteneinträge aus der Konstruktion (DIALOG) in die selektierten After-Sales-Module übernehmen für das
     * Kopieren von Positionen in einen anderen TU.
     *
     * @param moduleMap
     * @param notExistingModuleMap Map mit Modul-ID auf Zeile mit Informationen aus dem Übernahmedialog. Für Module, die
     *                             im Zuge der Übernahme erzeugt wurden und somit noch nicht in der DB existieren.
     * @param logMessages
     * @param messageLog
     * @throws EditTransferPartListEntriesException Exception falls beim Übernehmen von einem Stücklisteneintrag ein Fehler aufgetreten ist
     */
    public void createAndTransferDIALOGPartListEntriesForCopyModule(Map<String, List<TransferToASElement>> moduleMap,
                                                                    Map<String, TransferToASElement> notExistingModuleMap,
                                                                    List<String> logMessages, EtkMessageLog messageLog) throws EditTransferPartListEntriesException {
        createAndTransferPartListEntriesDIALOG(moduleMap, notExistingModuleMap, logMessages, messageLog, true, null);
    }

    /**
     * Stücklisteneinträge aus der Konstruktion (DIALOG) in die selektierten After-Sales-Module übernehmen für die
     * Übernahme aus der Konstruktion in den AS.
     *
     * @param moduleMap
     * @param notExistingModuleMap Map mit Modul-ID auf Zeile mit Informationen aus dem Übernahmedialog. Für Module, die
     *                             im Zuge der Übernahme erzeugt wurden und somit noch nicht in der DB existieren.
     * @param logMessages
     * @param messageLog
     * @param techChangeSet        Nur besetzt bei AutoTransferPLEsExtendedWholeProduct
     * @throws EditTransferPartListEntriesException Exception falls beim Übernehmen von einem Stücklisteneintrag ein Fehler aufgetreten ist
     */
    public void createAndTransferDIALOGPartListEntriesForTransferToAS(Map<String, List<TransferToASElement>> moduleMap,
                                                                      Map<String, TransferToASElement> notExistingModuleMap,
                                                                      List<String> logMessages, EtkMessageLog messageLog,
                                                                      iPartsRevisionChangeSet techChangeSet) throws EditTransferPartListEntriesException {
        createAndTransferPartListEntriesDIALOG(moduleMap, notExistingModuleMap, logMessages, messageLog, false, techChangeSet);
    }


    /**
     * Stücklisteneinträge aus der Konstruktion (DIALOG) in die selektierten After-Sales-Module übernehmen.
     *
     * @param moduleMap
     * @param notExistingModuleMap - Map mit ModulId auf Zeile mit Informationen aus dem Übernahmedialog. Für Module, die
     *                             im Zuge der Übernahme erzeugt wurden und somit noch nicht in der DB existieren.
     * @param logMessages
     * @param messageLog
     * @param isCopy               Wird gerade in einen anderen TU kopiert?
     * @param techChangeSet        Nur besetzt bei AutoTransferPLEsExtendedWholeProduct
     * @throws EditTransferPartListEntriesException Exception falls beim Übernehmen von einem Stücklisteneintrag ein Fehler aufgetreten ist
     */
    private void createAndTransferPartListEntriesDIALOG(Map<String, List<TransferToASElement>> moduleMap,
                                                        Map<String, TransferToASElement> notExistingModuleMap,
                                                        List<String> logMessages, EtkMessageLog messageLog,
                                                        boolean isCopy, iPartsRevisionChangeSet techChangeSet) throws EditTransferPartListEntriesException {
        final EtkProject project = getProject();
        this.messageLog = messageLog;
        // Die automatische Strukturstufenberechnung soll nur bei der Übernahme aus der Konstruktion in die AS Stückliste
        // durchgeführt werden.
        boolean isTransferFromConstPartsList = (sourceConstAssembly != null) && !isCopy;
        // Zu übernehmende Stücklisteneinträge mit angepassten Feldern in alle selektierten Ziel-Module kopieren
        AssemblyId destAssemblyId = null;

        // Nur eine GenericEtkDataObjectList für das Speichern im ChangeSet verwenden
        EtkDataObjectList<? extends EtkDataObject> dataObjectListToBeSaved = new GenericEtkDataObjectList<>();
        // Verschiedene Listen fürs Koppeln
        iPartsDataFactoryDataList changedLinkingDataInChangeSetToCommit = new iPartsDataFactoryDataList();
        EtkDataObjectList<? extends EtkDataObject> changedLinkingDataWithoutChangeSet = new GenericEtkDataObjectList<>();
        iPartsDataFactoryDataList afterLinkingOverwrite = new iPartsDataFactoryDataList();

        boolean revisionChangeSetActiveForEdit = isRevisionChangeSetActiveForEdit();
        startPseudoTransactionForActiveChangeSet(true);
        try {
            int currentRowIdx = 0;
            int maxPos = 0;
            Map<iPartsProductId, Set<String>> productToAAValues = new HashMap<>();
            for (List<TransferToASElement> values : moduleMap.values()) {
                if (!values.isEmpty()) {
                    maxPos += values.size() + (3 * EXTRA_INCREMENT_FOR_PROGRESS);
                }
                if (isTransferFromConstPartsList) {
                    // Alle AA Werte zusammensammeln um später die Strukturstufenberechnung durchführen zu können
                    values.forEach(transferToASElement -> {
                        iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(transferToASElement.getSelectedPartlistEntry());
                        if ((bctePrimaryKey != null) && StrUtils.isValid(bctePrimaryKey.getAA())) {
                            Set<String> aaValues = productToAAValues.computeIfAbsent(transferToASElement.getProduct().getAsId(), k -> new TreeSet<>());
                            aaValues.add(bctePrimaryKey.getAA());
                        }
                    });
                }
            }

            // Die "nackten" Strukturstufenbäume für alle nötigen Produkte und alle benötigten AA berechnen
            Map<String, HierarchyStructuresForProduct> hierarchyStructuresForProduct = new HashMap<>();
            if (isTransferFromConstPartsList) {
                // Alle AAs über alle Produkte hinweg sammeln
                Set<String> allAAValues = productToAAValues.values().stream()
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet());
                // Jetzt die Konstruktionsstückliste einmal durchlaufen und die DIALOG Positionen nach ihren AAs gruppieren
                Map<String, Map<String, Integer>> posToHierarchyForAllAAs = calculatePosToHierarchy(allAAValues);
                // Jetzt alle Produkte und die dazugehörigen AAs durchlaufen
                productToAAValues.forEach((product, aaValues) -> {
                    // Hier die Positionslisten (inkl. Strukturstufe) für die gültigen AAs zu Produkt bestimmen
                    Map<String, Map<String, Integer>> posToHierarchyForProductAAs = posToHierarchyForAllAAs.entrySet().stream()
                            .filter(entry -> aaValues.contains(entry.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    // Produkt mit seinen Positionslisten im neuen Objekt ablegen
                    HierarchyStructuresForProduct structures = new HierarchyStructuresForProduct(posToHierarchyForProductAAs);
                    hierarchyStructuresForProduct.put(product.getProductNumber(), structures);
                });
            }
            int currentPos = 0;
            fireProgress(currentPos, maxPos);

            // Helper für mögliche AS Stücklistenpositonen
            ASUsageHelper asUsageHelper = new ASUsageHelper(getProject());
            iPartsDIALOGPositionsHelper sourceDialogPositionsHelper = new iPartsDIALOGPositionsHelper(sourceConstAssembly.getPartListUnfiltered(null));

            // Map mit Produkt+AA auf die berechneten AS Strukturstufen auf Basis der Quell-Konstruktionsstückliste und
            // den jeweiligen AS Zielstücklisten
            for (Map.Entry<String, List<TransferToASElement>> entry : moduleMap.entrySet()) {
                if (isCancelled()) {
                    return;
                }
                List<TransferToASElement> transferList = entry.getValue();
                currentRowIdx++;
                String msg = "!!Übernehme %1 Datensätze in TU %2 von %3...";
                if (transferList.size() == 1) {
                    msg = "!!Übernehme %1 Datensatz in TU %2 von %3...";
                }
                fireMessage(msg, String.valueOf(transferList.size()), String.valueOf(currentRowIdx), String.valueOf(moduleMap.size()));
                if (transferList.isEmpty()) {
                    continue;
                }
                destAssemblyId = transferList.get(0).assemblyId;
                if (logHelper != null) {
                    logHelper.addLogMsgWithTranslation("!!Ziel-TU %1", destAssemblyId.getKVari());
                }
                if (destAssemblyId != null) { // bei null konnte Modul nicht angelegt werden
                    EtkDataAssembly destAssembly = EtkDataObjectFactory.createDataAssembly(project, destAssemblyId, false);

                    // Stückliste muss nur bei bereits vorhandenen Modulen geladen werden, ansonsten neue Liste erzeugen
                    boolean assemblyIsNew = notExistingModuleMap.containsKey(destAssemblyId.getKVari());
                    DBDataObjectList<EtkDataPartListEntry> destPartList = EditModuleHelper.getDestPartList(destAssembly, assemblyIsNew);
                    if (destPartList == null) {
                        continue;
                    }

                    iPartsDocumentationType documentationType = null;
                    if (assemblyIsNew) {
                        // bei neuen Modulen den DocuTyp vom Produkt verwenden
                        documentationType = transferList.get(0).getProduct().getDocumentationType();
                    } else if (destAssembly instanceof iPartsDataAssembly) {
                        // bei vorhandenen Modulen des DocuTyp vom Modul verwenden
                        documentationType = ((iPartsDataAssembly)destAssembly).getDocumentationType();
                    }
                    // Fallback
                    if ((documentationType == null) || (documentationType == iPartsDocumentationType.UNKNOWN)) {
                        documentationType = iPartsDocumentationType.DIALOG_IPARTS;
                    }
                    iPartsModuleTypes moduleType = documentationType.getModuleType(false);
                    String sourceContext = EditConstructionToRetailHelper.createSourceContext(moduleType.getSourceType(), sourceId);

                    // Alle Stücklisteneinträge VOR der Übernahme merken
                    List<EtkDataPartListEntry> originalDestPartListEntries = destPartList.getAsList();

                    // die Ziel-Stückliste für schnelleren Zugriff als Map mit der sourceGUID als Schlüssel ablegen
                    // wird unter anderem verwendet um zu prüfen ob der Eintrag schon übernommen wurde
                    HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap = new HashMap<>();

                    // Falls kein Hotspot angegeben ist, soll auf Basis des PV-Schlüssels einer ermittelt werden
                    // Dazu wird davon ausgegangen dass die Stückliste in ihrer Default sortierung vorliegt.
                    // Sollte sich in Zukunft daran etwas ändern muss hier angepasst werden
                    HashMap<String, String> hotspotSuggestions = new HashMap<>();

                    VarParam<String> finalSeqNr = new VarParam<>("");
                    VarParam<String> calculatedMaxHotSpot = new VarParam<>("0");// maxHotspot dient als Fallback falls kein Vorschlagswert gefunden wird
                    VarParam<Integer> destLfdNr = new VarParam<>(0);
                    // Startwert für die laufende Nummer und Sequenznummer auf Basis der höchsten existierenden laufenden
                    // Nummer bzw. Sequenznummer bestimmt

                    // Map von GenVO nummer auf Hotspot der Zielstückliste anlegen
                    Map<String, String> genVoHotspots = new HashMap<>();

                    EditModuleHelper.preprocessDestPartListEntries(destPartList, destLfdNr, finalSeqNr, calculatedMaxHotSpot,
                                                                   destPartListSourceGUIDMap, hotspotSuggestions, moduleType,
                                                                   sourceContext, genVoHotspots);

                    // Bei der Bestimmung von maxHotspot müssen auch die zu übernehmenden neuen Stücklisteneinträge berücksichtigt werden
                    // ebenso für die GenVO Hotspot Vorschläge
                    for (TransferToASElement transferElem : transferList) {
                        String rowHotspot = transferElem.hotspot;
                        if (logHelper != null) {
                            logHelper.addLogMsgWithTranslation("!!(%1) %2 %3", transferElem.getKgTuId().toString("/"), rowHotspot,
                                                               transferElem.getConstPrimaryKey().toString("/", false));
                        }
                        if (StrUtils.isValid(rowHotspot)) {
                            if (SortBetweenHelper.isGreater(rowHotspot, calculatedMaxHotSpot.getValue())) {
                                calculatedMaxHotSpot.setValue(rowHotspot);
                            }
                        }

                        EditModuleHelper.addToGenVoHotspotSuggestions(genVoHotspots, rowHotspot,
                                                                      transferElem.selectedPartlistEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO));
                    }

                    calculatedMaxHotSpot.setValue(EditModuleHelper.incrementMaxHotspotWithDefaultIncrement(calculatedMaxHotSpot.getValue()));

                    List<EtkDataPartListEntry> sourcePartListEntriesToTransferFiltered = new ArrayList<>();

                    Set<iPartsReplacementConst> replacementsConstToTransfer = new HashSet<>();

                    // Retail-Ersetzungen, die später aus den Konstruktions-Ersetzungen generiert werden
                    // Vorbesetzt mit allen Ersetzungen, die schon in der Assembly existieren um später nicht existierende lfdNrn zu generieren
                    iPartsDataReplacePartList dataReplacementsRetail = iPartsDataReplacePartList.loadReplacementsForAssembly(project, destAssemblyId);

                    // Retail-Mitlieferteile, die später aus den Konstruktions-Mitlieferteilen generiert werden
                    iPartsDataIncludePartList dataIncludePartsRetail = iPartsDataIncludePartList.loadIncludePartsForAssembly(project, destAssemblyId);

                    // Kombinierte Texte und Fußnoten, die ggf. vererbt werden
                    iPartsDataCombTextList combinedTextList = new iPartsDataCombTextList();
                    iPartsDataFootNoteCatalogueRefList fnCatalogueRefList = new iPartsDataFootNoteCatalogueRefList();
                    if (isTransferFromConstPartsList) {
                        // "Gültige" DIALOG Positionen hinzufügen
                        addDIALOGPosValuesToHierarchyStructures(transferList, destPartList, hierarchyStructuresForProduct);
                    }

                    // OmittedParts Cache für die Prüfung, ob GenVO Texte angelegt werden sollen
                    iPartsOmittedParts omittedParts = iPartsOmittedParts.getInstance(project);

                    // AS Strukturstufen pro Hotspot aus der Ziel-Stückliste bestimmen
                    Map<String, Integer> destPartsListPosToHierarchyValue = determineASHierarchyValues(originalDestPartListEntries);

                    // Map mit Produkt auf Stücklistenpositionen ohne berechneten HotSpot
                    Map<String, List<EtkDataPartListEntry>> entriesWithNotRelatedHotSpots = new HashMap<>();
                    Set<String> bcteKeysWithNotRelatedHotSpot = new HashSet<>();
                    // Jetzt die neuen Stücklisteneinträge der Reihe nach anlegen und einfügen
                    for (TransferToASElement rowContent : transferList) {
                        if (isCancelled()) {
                            return;
                        }
                        // Fortschritt erhöhen
                        currentPos++;
                        fireProgress(currentPos, maxPos);

                        // SourceGUID des zu übernehmenden Eintrags bestimmen
                        String sourceGUID = rowContent.getSourceGUIDForAttribute();

                        String genVo = rowContent.selectedPartlistEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO);
                        // Wenn der Hotspot leer ist, einen Vorschlag ermitteln
                        if (StrUtils.isEmpty(rowContent.hotspot)) {
                            if (rowContent.getConstPrimaryKey().isDialog()) {
                                rowContent.hotspot = EditModuleHelper.getSuggestedHotSpot(hotspotSuggestions,
                                                                                          rowContent.getConstPrimaryKey().getAsDialogBCTEPrimaryKey(),
                                                                                          calculatedMaxHotSpot,
                                                                                          project, genVoHotspots, genVo,
                                                                                          bcteKeysWithNotRelatedHotSpot);
                            }
                        }

                        if (EditModuleHelper.partListEntryAlreadyExistInDestPartList(destPartListSourceGUIDMap, sourceGUID, rowContent.hotspot)) {
                            continue;
                        }

                        // KEM-Kette inkl. minimalem KEM-Datum ab und maximalem KEM-Datum bis berechnen für den
                        // Quell-Konstruktions-Stücklisteneintrag
                        Set<EtkDataPartListEntry> sourcePLEsForKemChain = EditConstructionToRetailHelper.calculateMinMaxKEMDatesWithoutCache(rowContent.selectedPartlistEntry,
                                                                                                                                             sourceDialogPositionsHelper);

                        // DAIMLER-9238: laufende Nummer hochzählen und reservieren
                        destLfdNr.setValue(iPartsDataReservedPKList.getAndReserveNextKLfdNr(project, destAssemblyId, destLfdNr.getValue()));
                        // Den neuen Stücklisteneintrag erzeugen
                        PartListEntryId destPartListEntryId = new PartListEntryId(destAssemblyId.getKVari(), destAssemblyId.getKVer(),
                                                                                  EtkDbsHelper.formatLfdNr(destLfdNr.getValue()));
                        EtkDataPartListEntry destPartListEntry = EditConstructionToRetailHelper.createRetailPartListEntry(sourceContext,
                                                                                                                          rowContent.selectedPartlistEntry,
                                                                                                                          destPartListEntryId,
                                                                                                                          moduleType,
                                                                                                                          false, project, logMessages);

                        // Wenn kein HotSpot berechnet werden konnte, wird die Position für eine Berechnung auf Basis
                        // der AS Strukturstufen zwischengespeichert
                        if (!bcteKeysWithNotRelatedHotSpot.isEmpty()) {
                            String hotSpotBCTEKey = rowContent.getConstPrimaryKey().getAsDialogBCTEPrimaryKey().getPositionKeyWithAA();
                            if (bcteKeysWithNotRelatedHotSpot.contains(hotSpotBCTEKey)) {
                                List<EtkDataPartListEntry> entriesForProduct = entriesWithNotRelatedHotSpots.computeIfAbsent(rowContent.getProductId().getProductNumber(), k -> new ArrayList<>());
                                entriesForProduct.add(destPartListEntry);
                            }
                        }
                        EditModuleHelper.setHotSpotAndNextSequenceNumber(destPartListEntry, rowContent.hotspot, sourceGUID,
                                                                         destPartList, finalSeqNr.getValue(), sourcePartListEntriesToTransferFiltered);

                        // wenn hier ein Hotspot Vorschlag ermittelt wurde muss dieser auch zur GenVo Liste hinzugefügt werden
                        EditModuleHelper.addToGenVoHotspotSuggestions(genVoHotspots, rowContent.hotspot, genVo);

                        EditModuleHelper.adaptQuantity(rowContent.selectedPartlistEntry.getPart(), destPartListEntry);

                        // Übernahme in AS-Stückliste mit Daten vom evtl. vorhandenen Referenz-Stücklisteneintrag
                        inheritValuesFromReference(destPartListEntry, originalDestPartListEntries, combinedTextList,
                                                   fnCatalogueRefList, asUsageHelper, omittedParts, isTransferFromConstPartsList);

                        // Prüfen, ob die Stücklistenposition unterdrückt werden soll
                        if (omitPartListEntry(destPartListEntry)) {
                            destPartListEntry.setFieldValueAsBoolean(iPartsConst.FIELD_K_OMIT, true, DBActionOrigin.FROM_EDIT);
                        }

                        // Flag setzen, falls es die automatische Übernahme ist
                        if (rowContent.isAutoTransfer) {
                            destPartListEntry.setFieldValueAsBoolean(iPartsConst.FIELD_K_AUTO_CREATED, true, DBActionOrigin.FROM_EDIT);
                            destPartListEntry.setFieldValueAsBoolean(iPartsConst.FIELD_K_WAS_AUTO_CREATED, true, DBActionOrigin.FROM_EDIT);
                        }

                        // Konstruktions-Ersetzungen zusammensuchen
                        replacementsConstToTransfer.addAll(getConstructionReplacementsForKemChain(sourcePLEsForKemChain, sourceDialogPositionsHelper));

                        // AS Strukturstufe:
                        setASHierarchyValues(destPartListEntry, rowContent, destAssemblyId, hierarchyStructuresForProduct,
                                             isTransferFromConstPartsList, destPartsListPosToHierarchyValue);

                        if (rowContent.getConstPrimaryKey().isDialog()) {
                            EditModuleHelper.finishPartListEntryCreation(destPartListSourceGUIDMap, sourceGUID, destPartListEntry,
                                                                         rowContent.hotspot, rowContent.getConstPrimaryKey().getAsDialogBCTEPrimaryKey(),
                                                                         hotspotSuggestions, sourcePartListEntriesToTransferFiltered,
                                                                         destLfdNr, finalSeqNr);
                        }

                        // Maximales "KEM-Datum bis" an allen Vorgänger- und Nachfolgerständen korrigieren (das minimale
                        // "KEM-Datum ab" kann sich eigentlich nicht ändern, weil es nur neuere Konstruktions-Stücklisteneinträge
                        // geben kann, aber keine älteren -> sicherheitshalber trotzdem setzen)
                        if (sourcePLEsForKemChain.size() > 1) { // Gibt es überhaupt Vorgänger- oder Nachfolgerstände?
                            String minKemDateFrom = rowContent.selectedPartlistEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM);
                            String maxKemDateTo = rowContent.selectedPartlistEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO);
                            for (EtkDataPartListEntry sourcePLEForKemChain : sourcePLEsForKemChain) {
                                List<EtkDataPartListEntry> destPLEsForKemChain = destPartListSourceGUIDMap.get(sourcePLEForKemChain.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID));
                                if (destPLEsForKemChain != null) {
                                    for (EtkDataPartListEntry destPLEForKemChain : destPLEsForKemChain) {
                                        destPLEForKemChain.setFieldValue(iPartsConst.FIELD_K_MIN_KEM_DATE_FROM, minKemDateFrom, DBActionOrigin.FROM_EDIT);
                                        destPLEForKemChain.setFieldValue(iPartsConst.FIELD_K_MAX_KEM_DATE_TO, maxKemDateTo, DBActionOrigin.FROM_EDIT);
                                    }
                                }
                            }
                        }
                        if (onModifyCreatedRetailPartListEntryEvent != null) {
                            onModifyCreatedRetailPartListEntryEvent.onModifyCreatedPartListEntry(getProject(), destPartListEntry,
                                                                                                 rowContent, combinedTextList,
                                                                                                 fnCatalogueRefList);
                        }
                    }

                    // Neue Logik zum Korrigieren von AS Stufe und Hotspot nur anwenden, wenn sie aktiv ist und nur bei
                    // den Positionen, bei denen ein HotSpot durch die aktuellen Logiken nicht berechnet werden konnte
                    if (iPartsEditPlugin.isDIALOGHotSpotAndHierarchyCorrectionActive() && !entriesWithNotRelatedHotSpots.isEmpty()) {
                        calculateHotSpotBasedOnASHierarchies(entriesWithNotRelatedHotSpots, hierarchyStructuresForProduct);
                    }

                    // Jetzt die neuen Einträge zur Stückliste hinzufügen
                    for (EtkDataPartListEntry partListEntry : sourcePartListEntriesToTransferFiltered) {
                        destPartList.add(partListEntry, DBActionOrigin.FROM_EDIT);
                    }

                    // DAIMLER-15494, Bei Übernahme eines direkten Nachfolger-KEM-Stands PEMab/bis-Prüfen nach Teilkonjunktionsprüfung automatisch setzen
                    enablePEMEvalFlagsForPartialConjunctionOverlap(sourcePartListEntriesToTransferFiltered, destPartList);

                    // Zielstückliste inkl. der neuen Einträge nach Hotspot gruppieren
                    Map<String, List<EtkDataPartListEntry>> destPartListForHotspot = new HashMap<>();
                    for (EtkDataPartListEntry partListEntry : destPartList) {
                        String hotspot = partListEntry.getFieldValue(FIELD_K_POS);
                        destPartListForHotspot.computeIfAbsent(hotspot, s -> {
                            return new ArrayList<>();
                        }).add(partListEntry);
                    }

                    // DAIMLER-15495: Prüfen ob PEM ab/bis Auswerten aktiviert werden muss für Änderungsjahr Rückdokumentation
                    for (EtkDataPartListEntry partListEntry : sourcePartListEntriesToTransferFiltered) {
                        String hotspot = partListEntry.getFieldValue(FIELD_K_POS);
                        List<EtkDataPartListEntry> destPartListEntriesForHotspot = new DwList<>(destPartListForHotspot.get(hotspot));
                        destPartListEntriesForHotspot.removeIf(destPartListEntry -> destPartListEntry.getAsId().equals(partListEntry.getAsId()));
                        enablePEMEvalFlagForBackwardsDocumentation(partListEntry, destPartListEntriesForHotspot, documentationType);
                    }

                    if (isCancelled()) {
                        return;
                    }
                    currentPos += EXTRA_INCREMENT_FOR_PROGRESS;
                    fireProgress(currentPos, maxPos);
                    // Konstruktions-Ersetzungen als Retail-Ersetzungen zur Stückliste hinzufügen
                    addConstReplacementsToRetailPartList(replacementsConstToTransfer, destPartList, destPartListSourceGUIDMap,
                                                         dataReplacementsRetail, dataIncludePartsRetail, getProject());

                    if (isCancelled()) {
                        return;
                    }
                    currentPos += EXTRA_INCREMENT_FOR_PROGRESS;
                    fireProgress(currentPos, maxPos);
                    if (destAssembly instanceof iPartsDataAssembly) {
                        EditConstructionToRetailHelper.addWWPartsFromConstruction((iPartsDataAssembly)destAssembly, destPartList, sourcePartListEntriesToTransferFiltered);
                    }


                    if (revisionChangeSetActiveForEdit || (techChangeSet != null)) {
                        if (isCancelled()) {
                            return;
                        }
                        currentPos += EXTRA_INCREMENT_FOR_PROGRESS;
                        fireProgress(currentPos, maxPos);
                        EtkDataObjectList<? extends EtkDataObject> dataObjectListForChangeSet = (techChangeSet == null)
                                                                                                ? dataObjectListToBeSaved
                                                                                                : new GenericEtkDataObjectList<>();
                        EditModuleHelper.finishModuleModification(dataObjectListForChangeSet, destPartList, assemblyIsNew,
                                                                  destAssembly, dataReplacementsRetail, dataIncludePartsRetail,
                                                                  combinedTextList, fnCatalogueRefList, true);
                        if (onModifyCreatedRetailPartListEntryEvent != null) {
                            onModifyCreatedRetailPartListEntryEvent.onModifyCreatedAssembly(getProject(), dataObjectListForChangeSet,
                                                                                            assemblyIsNew, destAssembly);
                        }
                        // Werksdaten für die Kopplung aufsammeln
                        getFactoryDataLinkingCandidates(sourcePartListEntriesToTransferFiltered, sourceDialogPositionsHelper,
                                                        changedLinkingDataInChangeSetToCommit, changedLinkingDataWithoutChangeSet,
                                                        afterLinkingOverwrite);
                        if (techChangeSet != null) {
                            // In destAssembly temporär explizit eine leere Stückliste setzen, damit die Stücklisteneinträge nicht
                            // doppelt im ChangeSet landen bzw. gespeichert werden (einzeln und als Kind-Elemente von destAssembly),
                            // weil die modifiedFlags nicht zurückgesetzt werden
                            EtkDataPartListEntryList emptyPartList = new EtkDataPartListEntryList();
                            emptyPartList.resetModifiedFlags();
                            try {
                                destAssembly.setChildren(EtkDataAssembly.CHILDREN_NAME_PART_LIST_ENTRIES, emptyPartList);
                                techChangeSet.addDataObjectList(dataObjectListForChangeSet, false, false);

                                // Daten direkt abspeichern und Ziel-Modul aus dem Cache löschen
                                dataObjectListForChangeSet.saveToDB(project);
                            } finally {
                                destAssembly.setChildren(EtkDataAssembly.CHILDREN_NAME_PART_LIST_ENTRIES, destPartList);
                            }
                            EtkDataAssembly.removeDataAssemblyFromCache(getProject(), destAssembly.getAsId());
                        }
                    } else {
                        // Ziel-Stückliste abspeichern und aus dem Cache löschen
                        destPartList.saveToDB(project, false);
                        EtkDataAssembly.removeDataAssemblyFromCache(getProject(), destAssembly.getAsId());
                    }
                }
            }
        } catch (Exception e) {
            throw new EditTransferPartListEntriesException(TranslationHandler.translate("!!Fehler beim Übernehmen der Positionen für das Modul \"%1\"!",
                                                                                        (destAssemblyId != null) ? destAssemblyId.getKVari() : "?"), e);
        } finally {
            stopPseudoTransactionForActiveChangeSet();
            this.messageLog = null;
        }

        try {
            if (!dataObjectListToBeSaved.isEmpty() && revisionChangeSetActiveForEdit) {
                addDataObjectListToActiveChangeSetForEdit(dataObjectListToBeSaved);
            }
            if (revisionChangeSetActiveForEdit || (techChangeSet != null)) {
                // Falls es Datensätze zum Koppeln gibt diese Speichern
                if (!changedLinkingDataInChangeSetToCommit.isEmpty()) {
                    saveLinking(changedLinkingDataInChangeSetToCommit, changedLinkingDataWithoutChangeSet, afterLinkingOverwrite,
                                (techChangeSet != null) ? techChangeSet : project.getActiveChangeSetForEdit());
                    withLinkingChanges = true;
                }
            }
        } catch (Exception e) {
            throw new EditTransferPartListEntriesException(TranslationHandler.translate("!!Fehler beim Speichern."), e);
        }
    }

    private void enablePEMEvalFlagForBackwardsDocumentation(EtkDataPartListEntry newPartlistEntry,
                                                            List<EtkDataPartListEntry> partListEntriesWithSameHotspot,
                                                            iPartsDocumentationType documentationType) {
        if (partListEntriesWithSameHotspot == null || partListEntriesWithSameHotspot.isEmpty()) {
            return;
        }

        // DAIMLER-15717: Änderungsjahrrückdokumentation nur bei der Dokumethode "DIALOG iParts" anwenden
        if (documentationType == iPartsDocumentationType.DIALOG_IPARTS) {
            enablePEMFlagsForDifferentPV(newPartlistEntry, partListEntriesWithSameHotspot);
            enablePEMFlagsForBCTEPredecessor(newPartlistEntry, partListEntriesWithSameHotspot);
        }
    }

    private void enablePEMFlagsForBCTEPredecessor(EtkDataPartListEntry newPartlistEntry, List<EtkDataPartListEntry> partListEntriesWithSameHotspot) {
        // Prüfe, ob die neue Teileposition keinen Änderungsjahrcode enthält und einen BCTE-Vorgänger im gleichen Hotspot hat
        // (gleiche BR-HM-M-SM-POS-PV-AA, Teilenummer, KEMbis Datum vorhanden, gibt es mehrere soll die Teileposition,
        // bei der das KEM Datum bis am nächsten zum KEM Datum ab der neuen Teileposition liegt).
        // Das KEMbis-Datum muss nicht dem KEMab-Datum der neuen Teileposition entsprechen.
        String codesNewPosition = newPartlistEntry.getFieldValue(FIELD_K_CODES);
        if (!iPartsModelYearCode.isModelYearCode(codesNewPosition)) {
            iPartsDialogBCTEPrimaryKey bcteKeyNewPosition = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(newPartlistEntry);
            String matNrNewPosition = newPartlistEntry.getPart().getAsId().getMatNr();
            String dateFromNewPos = newPartlistEntry.getFieldValue(FIELD_K_DATEFROM);

            EtkDataPartListEntry bctePredecessor = findBCTEPredecessor(partListEntriesWithSameHotspot, matNrNewPosition, bcteKeyNewPosition, dateFromNewPos);
            if (bctePredecessor != null) {
                // wurde ein passender Vorgänger gefunden, dort das PEM bis Flag setzen, beim neuen Eintrag PEM ab setzen
                bctePredecessor.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO, true, DBActionOrigin.FROM_EDIT);
                newPartlistEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM, true, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    private EtkDataPartListEntry findBCTEPredecessor(List<EtkDataPartListEntry> partListEntriesWithSameHotspot, String matNrNewPosition, iPartsDialogBCTEPrimaryKey bcteKeyNewPosition, String dateFromNewPos) {
        // Liste aller möglichen Vorgänger ermitteln (gleiche BR-HM-M-SM-POS-PV-AA, Teilenummer)
        List<EtkDataPartListEntry> predecessorCandidates = new DwList<>();
        for (EtkDataPartListEntry existingPosition : partListEntriesWithSameHotspot) {

            iPartsDialogBCTEPrimaryKey bcteKeyExistingPosition = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(existingPosition);
            String matNrExistingPosition = existingPosition.getPart().getAsId().getMatNr();

            if (matNrNewPosition.equals(matNrExistingPosition) && bcteKeyExistingPosition.isBCTEPredecessorCandidate(bcteKeyNewPosition)) {
                String dateToExistingPos = existingPosition.getFieldValue(FIELD_K_DATETO);
                if (!dateToExistingPos.isEmpty()) {
                    predecessorCandidates.add(existingPosition);
                }
            }
        }

        // die Liste der möglichen Vorgänger so sortieren, dass der neuste Eintrag an erster Stelle steht.
        predecessorCandidates.sort(Comparator.comparing(partListEntry -> {
            return partListEntry.getFieldValue(FIELD_K_DATETO);
        }, Comparator.reverseOrder()));

        // Liste so lange durchlaufen, bis ein Datumswert kleiner/älter oder gleich dem Datum ab der neuen Position ist
        for (EtkDataPartListEntry predecessorCandidate : predecessorCandidates) {
            String predecessorDateTo = predecessorCandidate.getFieldValue(FIELD_K_DATETO);
            if (dateFromNewPos.compareTo(predecessorDateTo) >= 0) {
                // prüfen ob im Vorgänger ein Änderungsjahrcode positiv enthalten ist.
                if (iPartsModelYearCode.isModelYearCode(predecessorCandidate.getFieldValue(FIELD_K_CODES))) {
                    return predecessorCandidate;
                }
            }
        }
        return null;
    }

    private void enablePEMFlagsForDifferentPV(EtkDataPartListEntry newPartlistEntry, List<EtkDataPartListEntry> partListEntriesWithSameHotspot) {
        // Prüfe, ob es eine Teileposition im gleichen Hotspot gibt, deren KEMbis dem KEMab der neuen Teileposition entspricht
        // und sich die PV-Nummer von der neuen Teileposition unterscheidet.

        // KEMab und BCTE Key der neuen Teileposition
        String kemFromNewPosition = newPartlistEntry.getFieldValue(FIELD_K_DATEFROM);
        iPartsDialogBCTEPrimaryKey bcteKeyNewPosition = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(newPartlistEntry);

        for (EtkDataPartListEntry existingPartListEntry : partListEntriesWithSameHotspot) {
            // KEMbis und PV-Nummer der existierenden Teileposition
            String kemToExistingPosition = existingPartListEntry.getFieldValue(FIELD_K_DATETO);
            iPartsDialogBCTEPrimaryKey bcteKeyExistingPosition = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(existingPartListEntry);

            // KEM Datum muss passen und der BCTE Schlüssel (ohne PosV) muss übereinstimmen, PosV muss unterschiedlich sein
            if (kemToExistingPosition.equals(kemFromNewPosition) &&
                bcteKeyNewPosition.isPositionVariant(bcteKeyExistingPosition) &&
                !bcteKeyNewPosition.getPosV().equals(bcteKeyExistingPosition.getPosV())) {
                // Lenkungsgültigkeit prüfen
                if (iPartsEditValidationHelper.isSteeringValid(existingPartListEntry.getFieldValue(FIELD_K_STEERING),
                                                               newPartlistEntry.getFieldValue(FIELD_K_STEERING))) {
                    // prüfen, ob alle Teilekonjunktionen der ermittelten Teilepositionen gleich sind.
                    String codesNewPosition = newPartlistEntry.getFieldValue(FIELD_K_CODES);
                    String codesExistingPosition = existingPartListEntry.getFieldValue(FIELD_K_CODES);
                    if (iPartsEditValidationHelper.isAllPartialConjunctionsEqual(codesExistingPosition, codesNewPosition)) {
                        // bei der neuen Teileposition das PEMab-Flag und bei der anderen Teileposition das PEMbis-Flag setzen
                        newPartlistEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM, true, DBActionOrigin.FROM_EDIT);
                        existingPartListEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO, true, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
    }

    /**
     * DAIMLER-15494, Bei Übernahme eines direkten Nachfolger-KEM-Stands "PEMab/bis-Prüfen" nach Teilkonjunktionsprüfung automatisch setzen.
     *
     * @param newPartsList
     * @param destPartList
     */
    private void enablePEMEvalFlagsForPartialConjunctionOverlap(List<EtkDataPartListEntry> newPartsList, DBDataObjectList<EtkDataPartListEntry> destPartList) {
        // Über alle zu transferierenden, neuen Teilepositionen iterieren
        Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> destBCTEpkMap = new HashMap<>();
        for (EtkDataPartListEntry destPartListEntry : destPartList) {
            iPartsDialogBCTEPrimaryKey destPleBCTEpk = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(destPartListEntry);
            if (destPleBCTEpk != null) {
                destPleBCTEpk = new iPartsDialogBCTEPrimaryKey(destPleBCTEpk.getHmMSmId(), destPleBCTEpk.getPosE(), destPleBCTEpk.getPosV(), "", "", destPleBCTEpk.getAA(), "");
                if (destPleBCTEpk != null) {
                    List<EtkDataPartListEntry> ples = destBCTEpkMap.computeIfAbsent(destPleBCTEpk, k -> new DwList<>());
                    ples.add(destPartListEntry);
                }
            }
        }

        for (EtkDataPartListEntry newPartListEntry : newPartsList) {
            EtkDataAssembly ownerAssembly = newPartListEntry.getOwnerAssembly();
            if ((ownerAssembly instanceof iPartsDataAssembly) && ((iPartsDataAssembly)ownerAssembly).getDocumentationType() != iPartsDocumentationType.DIALOG_IPARTS) {
                continue;
            }
            iPartsDialogBCTEPrimaryKey newPleBCTEpk = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(newPartListEntry);
            if (newPleBCTEpk != null) {
                newPleBCTEpk = new iPartsDialogBCTEPrimaryKey(newPleBCTEpk.getHmMSmId(), newPleBCTEpk.getPosE(), newPleBCTEpk.getPosV(), "", "", newPleBCTEpk.getAA(), "");
            }
            if (newPleBCTEpk != null) {
                List<EtkDataPartListEntry> ples = destBCTEpkMap.get(newPleBCTEpk);
                // Bei gleichem BCTE-Schlüssel
                if (ples != null) {
                    String newMatNo = newPartListEntry.getFieldValue(iPartsConst.FIELD_K_MATNR);
                    String newKEMA = newPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_KEMA);
                    for (EtkDataPartListEntry destPartListEntry : ples) {
                        String destKEMB = destPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_KEMB);
                        // Bei gleicher Materialnummer und
                        // wenn das "KEM-Datum bis" des Vorgängers dem "KEM Datum ab" der neuen Teileposition entspricht
                        if (newMatNo.equals(destPartListEntry.getFieldValue(iPartsConst.FIELD_K_MATNR)) &&
                            newKEMA.equals(destKEMB)) {
                            // Prüfung auf "Vorgänger hat alle Teilkonjunktionen der neuen Teilepos und der Vorgänger hat weitere Teilkonjunktionen."
                            if (iPartsEditValidationHelper.isAllPartialConjunctionsIncluded(destPartListEntry.getFieldValue(FIELD_K_CODES),
                                                                                            newPartListEntry.getFieldValue(FIELD_K_CODES),
                                                                                            true)) {
                                // bei der neuen Teileposition das PEMab-Flag und bei der anderen Teileposition das PEMbis-Flag setzen
                                newPartListEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_FROM, true, DBActionOrigin.FROM_EDIT);
                                destPartListEntry.setFieldValueAsBoolean(FIELD_K_EVAL_PEM_TO, true, DBActionOrigin.FROM_EDIT);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Berechnet die HotSpots für Positionen, die im berechneten AS Strukturen Baum liegen und durch die bisherigen
     * Logiken keinen berechneten HotSpot erhalten haben
     *
     * @param entriesWithNotRelatedHotSpots
     * @param hierarchyStructuresForProduct
     */
    private void calculateHotSpotBasedOnASHierarchies(Map<String, List<EtkDataPartListEntry>> entriesWithNotRelatedHotSpots,
                                                      Map<String, HierarchyStructuresForProduct> hierarchyStructuresForProduct) {
        // Durchlaufe alle Positionen, bei denen ein Hotspot nicht berechnet werden konnte
        entriesWithNotRelatedHotSpots.forEach((product, entries) -> {
            // Pro Produkt wird der komplette Baum geholt
            HierarchyStructuresForProduct hierarchy = hierarchyStructuresForProduct.get(product);
            if ((hierarchy != null) && !entries.isEmpty()) {
                entries.forEach(hierarchy::setHotSpotAndSeqNo);
            }
        });

    }

    /**
     * Bestimmt zu jedem HotSpot in der Stücklisten die gültige AS Strukturstufe. Existieren zu einem HotSpot mehrere
     * Strukturstufen, wird die höhere verwendet
     *
     * @param originalDestPartListEntries
     * @return
     */
    private Map<String, Integer> determineASHierarchyValues(List<EtkDataPartListEntry> originalDestPartListEntries) {
        // Hotspot zu AS Strukturstufe
        Map<String, Integer> destPartsListHotSpotToHierarchyValue = new TreeMap<>();
        originalDestPartListEntries.forEach(asEntry -> {
            // Hotspot bestimmen
            String hotSpot = asEntry.getFieldValue(FIELD_K_POS);
            if (StrUtils.isValid(hotSpot)) {
                // HotSpot ist gültig -> Strukturstufe auslesen
                int asHierarchyValue = StrUtils.strToIntDef(asEntry.getFieldValue(FIELD_K_HIERARCHY), -1);
                if (asHierarchyValue > 0) {
                    // Strukturstufe gültig -> Ablegen bzw prüfen, ob schon eine existiert. Falls ja, höhere verwenden
                    Integer currentHierarchy = destPartsListHotSpotToHierarchyValue.get(hotSpot);
                    if ((currentHierarchy == null) || (asHierarchyValue > currentHierarchy)) {
                        destPartsListHotSpotToHierarchyValue.put(hotSpot, asHierarchyValue);
                    }
                }
            }
        });
        return destPartsListHotSpotToHierarchyValue;
    }

    private void setASHierarchyValues(EtkDataPartListEntry destPartListEntry, TransferToASElement rowContent, AssemblyId destAssemblyId,
                                      Map<String, HierarchyStructuresForProduct> hierarchyStructuresForProduct,
                                      boolean isTransferFromConstPartsList, Map<String, Integer> destPartsListHotSpotToHierarchyValue) {
        // Prüfen, ob zum HotSpot schon eine AS Strukturstufe existiert
        String hierarchyValue = null;
        String hotSpot = destPartListEntry.getFieldValue(FIELD_K_POS);
        if (StrUtils.isValid(hotSpot)) {
            Integer hierarchyForHotSpot = destPartsListHotSpotToHierarchyValue.get(hotSpot);
            if (hierarchyForHotSpot != null) {
                hierarchyValue = String.valueOf(hierarchyForHotSpot);
            }
        }
        // Existiert eine Strukturstufe in der Ziel-Stückliste, dann wird diese gesetzt. Ansonsten wird auf Basis der
        // Konstruktionsstückliste eine Strukturstufe bestimmt.
        if (StrUtils.isValid(hierarchyValue)) {
            destPartListEntry.setFieldValue(iPartsConst.FIELD_K_HIERARCHY, hierarchyValue, DBActionOrigin.FROM_EDIT);
        } else if (isTransferFromConstPartsList) {
            // BCTE Schlüssel der zu übernehmenden Position (aus der Konstruktion)
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(destPartListEntry);
            if ((bctePrimaryKey != null) && StrUtils.isValid(bctePrimaryKey.getAA())) {
                // Alle Strukturen zum Produkt. Es könnten mehrere Konstruktions- bzw.
                // AS-Stücklisten in einem Produkt+AA betroffen sein.
                HierarchyStructuresForProduct structure = hierarchyStructuresForProduct.get(rowContent.getProduct().getAsId().getProductNumber());
                // Struktur für die aktuelle AA und Zielstückliste laden, indem die Sturkturstufe für die Position
                // der aktuellen Konstruktionsposition erfragt wird (AA wird innerhalb der Methode geprüft)
                String calculatedHierarchyValue = structure.getHierarchyForDIALOGSourceEntry(bctePrimaryKey, destAssemblyId);
                if (StrUtils.isValid(calculatedHierarchyValue)) {
                    destPartListEntry.setFieldValue(iPartsConst.FIELD_K_HIERARCHY, calculatedHierarchyValue, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    /**
     * Erzeugt den "nackten" DIALOG Positions-Baum für die aktuelle Konstruktionsstückliste, indem Stücklistenpositionen
     * mit der gleichen DIALOG Position verdichtet werden. Jede Position erhält dann ihre verdichtete Entwicklungs-
     * Strukturstufe. Eine Konstruktionsposition wird nur berücksichtigt, wenn sie eine der übergebenen AAs hat und
     * der Reifegrad "leer" ist.
     *
     * @return
     */
    private Map<String, Map<String, Integer>> calculatePosToHierarchy(Set<String> allAAValues) {
        Map<String, Map<String, Integer>> posToHierarchyForAA = new HashMap<>();
        Map<String, Map<String, Integer>> posWithRFGToHierarchyForAA = new HashMap<>();
        // Durchlaufe alle Konstruktionspositionen
        for (EtkDataPartListEntry partListEntry : sourceConstAssembly.getPartListUnfiltered(null)) {
            // Positionstexte nicht berücksichtigen
            if (VirtualMaterialType.isPartListTextEntry(partListEntry)) {
                continue;
            }
            // BCTE Schlüssel, AS und Reifegrad der Position bestimmen
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
            String aaCurrentPosition = bctePrimaryKey.getAA();
            if (allAAValues.contains(aaCurrentPosition)) {
                String rfg = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RFG);
                // Positionen, die einen Reifegrad haben, sollen ganz normal verarbeitet werden. Positionen, die NUR
                // nicht-leere-Reifegrade haben sollen ebenfalls verarbeitet werden
                Map<String, Integer> posToHierarchy = StrUtils.isEmpty(rfg) ? posToHierarchyForAA.computeIfAbsent(aaCurrentPosition, k -> new TreeMap<>())
                                                                            : posWithRFGToHierarchyForAA.computeIfAbsent(aaCurrentPosition, k -> new TreeMap<>());
                // DIALOG Position
                String pos = bctePrimaryKey.getPosE();
                // Strukturstufe aus der Entwicklung
                String constHierarchy = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_HIERARCHY);
                int hValue = StrUtils.strToIntDef(constHierarchy, -1);
                // Falls eine Strukturstufe zur DIALOG Position existiert, wird die höhere genommen
                Integer currentValue = posToHierarchy.get(pos);
                if ((currentValue == null) || (currentValue < hValue)) {
                    posToHierarchy.put(pos, hValue);
                }
            }
        }

        // Prüfen, ob zu einer oder mehreren AAs Maps mit DIALOG Position auf Strukturstufe existieren -> bezieht sich nur auf
        // Positionen bei denen mind. eine Positionsvariante mit Reifegrad existiert
        if (!posWithRFGToHierarchyForAA.isEmpty()) {
            // Für jede Ausführungsart die Maps mit DIALOG-Position auf Strukturstufe durchgehen
            posWithRFGToHierarchyForAA.forEach((aaValue, posToHierarchyWithRFG) -> {
                // Schauen, ob es zur AA aus der Reifegrade Map auch Einträge in der Map ohne Reifegrade gibt
                Map<String, Integer> posToHierarchyForAAResult = posToHierarchyForAA.get(aaValue);
                if (posToHierarchyForAAResult == null) {
                    // Kein Treffer, dann Daten zur AA aus Reifegrad Map in Map ohne Reifegrade hinzufügen
                    posToHierarchyForAA.put(aaValue, posToHierarchyWithRFG);
                } else {
                    // Treffer zur AA: Positionen mit Reifegrade durchgehen
                    // Schauen, ob zur Position mit Reifegrad schon Einträge in der Map ohne Reifegrad-Positionen existieren.
                    // Falls ja, gab es also zur DIALOG Position Einträge mit UND ohne Reifegrad. In dem Fall sollen
                    // nur die Positionen ohne Reifegrad berücksichtigt werden (Einträge in der posToHierarchyForAA)
                    // Falls nein, dann gibt es zur DIALOG Position nur Positionsvarianten MIT Reifegrad. In diesem
                    // Fall soll die DIALOG Position zur Berechnung herangezogen werden.
                    posToHierarchyWithRFG.forEach(posToHierarchyForAAResult::putIfAbsent);
                }
            });
        }
        return posToHierarchyForAA;
    }

    /**
     * Fügt die DIALOG-Positionen der selektierten und die DIALOG Positionen der AS Stücklistenpositionen zu den
     * Strukturstufen-Brechnungsobjekten hinzu
     *
     * @param transferList
     * @param destPartList
     * @param structureForProductAndAA
     */
    private void addDIALOGPosValuesToHierarchyStructures(List<TransferToASElement> transferList, DBDataObjectList<EtkDataPartListEntry> destPartList,
                                                         Map<String, HierarchyStructuresForProduct> structureForProductAndAA) {
        // Die zu übernehmenden Positionen geben vor, welche Produkt+AA Kombinationen gültig sind. Damit beim Durchlaufen die
        // Positionen in der Zielstückliste keine "falschen" Treffer angelegt werden, wird hier gesammelt, welche Produkt+AA
        // Kombinationen gültig sind
        Set<String> validProductsAndAAs = fillConstructionPos(transferList, structureForProductAndAA);
        // Jetzt die passenden Positionen der Zielstückliste durchlaufen und die DIALOG Positionen der passenden
        // AS Positionen als "gültige" Positionen ablegen
        if ((destPartList != null) && !destPartList.isEmpty()) {
            // Zielstückliste bestimmen
            EtkDataAssembly destAssembly = destPartList.get(0).getOwnerAssembly();
            if (destAssembly instanceof iPartsDataAssembly) {
                // Produkt zur Zielstückliste bestimmen
                iPartsProductId productId = ((iPartsDataAssembly)destAssembly).getProductIdFromModuleUsage();
                if ((productId != null) && StrUtils.isValid(productId.getProductNumber())) {
                    for (EtkDataPartListEntry destEntry : destPartList) {
                        // Pro Position prüfen, ob die AS Positionen für das Erzeugen der Strukturstufen gültig ist
                        iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(destEntry);
                        if (bctePrimaryKey != null) {
                            String keyForHierarchyData = createProductAndAADataKey(productId, bctePrimaryKey);
                            if (validProductsAndAAs.contains(keyForHierarchyData)) {
                                HierarchyStructuresForProduct structure = structureForProductAndAA.get(productId.getProductNumber());
                                if (structure != null) {
                                    structure.addDIALOGPosOfExistingEntry(bctePrimaryKey, destEntry, destAssembly.getAsId());
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private Set<String> fillConstructionPos(List<TransferToASElement> transferList,
                                            Map<String, HierarchyStructuresForProduct> structureForProductAndAA) {
        Set<String> validProductsAndAAs = new HashSet<>();
        // Erst die selektierten Konstruktionspositionen durchgehen
        for (TransferToASElement transferObject : transferList) {
            EtkDataPartListEntry partListEntry = transferObject.getSelectedPartlistEntry();
            // BCTE Schlüssel der Konstruktionsposition bestimmen
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
            if ((bctePrimaryKey != null) && StrUtils.isValid(bctePrimaryKey.getAA())) {
                // Strukturen für Produkt
                HierarchyStructuresForProduct structure = structureForProductAndAA.get(transferObject.getProduct().getAsId().getProductNumber());
                if (structure == null) {
                    continue;
                }
                String keyForHierarchyData = createProductAndAADataKey(transferObject.getProductId(), bctePrimaryKey);
                validProductsAndAAs.add(keyForHierarchyData);
                // Die DIALOG Konstruktionsposition zu den "gültigen" Positionen hinzufügen
                structure.addDIALOGPosOfSelectedEntry(bctePrimaryKey, partListEntry);
            }
        }
        return validProductsAndAAs;
    }

    private String createProductAndAADataKey(iPartsProductId productId, iPartsDialogBCTEPrimaryKey bctePrimaryKey) {
        return createProductAndAADataKey(productId.getProductNumber(), bctePrimaryKey.getAA());
    }

    private String createProductAndAADataKey(String productNumber, String aaValue) {
        return productNumber + "||" + aaValue;
    }

    /**
     * Liefert zurück, ob die erzeugte Stücklistenposition unterdrückt werden soll (K_OMIT)
     *
     * @param destPartListEntry
     * @return
     */
    private boolean omitPartListEntry(EtkDataPartListEntry destPartListEntry) {
        // Bei der Übernahme in AS sollen alle Teilepositionen mit ET-KZ ungleich "E" (am Teilestamm)
        // automatisch unterdrückt werden. Es gibt aber folgende Ausnahmen:
        // - DAIMLER-12313: Teilepositionen mit ET-KZ = "E" und ETK = "N, K, NB oder KB" (am DIALOG Datensatz) sollen
        // trotz ET-KZ = "E" ebenfalls unterdrückt werden
        // - DAIMLER-12452: Teilepositionen mit ET-KZ = "K", sonstige-KZ = "LA" und Teilenummer in DA_WIRE_HARNESS (Leitungssatz-BK)
        // sollen obwohl ET-KZ ungleich "E" ist NICHT unterdrückt werden
        if (destPartListEntry.getPart().getFieldValue(iPartsConst.FIELD_M_ETKZ).equals(MATERIAL_ETKZ_CHECK_VALUE)) {
            // ET-KZ = "E" -> Nur unterdrücken, wenn ETK = "N, K, NB oder KB"
            return hasOmitETKValue(destPartListEntry);
        } else {
            // ET-KZ ungleich "E" -> eigentlich alle unterdrücken, außer ET-KZ = "K", sonstige-KZ = "LA" und Teilenummer in DA_WIRE_HARNESS (Leitungssatz-BK)
            return !isWireHarnessPart(destPartListEntry);
        }
    }

    /**
     * Liefert zurück, ob es sich um einen Leitungssatz-BK handelt (ET-KZ = "K", sonstige-KZ = "LA" und Teilenummer in DA_WIRE_HARNESS)
     *
     * @param destPartListEntry
     * @return
     */
    private boolean isWireHarnessPart(EtkDataPartListEntry destPartListEntry) {
        return destPartListEntry.getPart().getFieldValue(iPartsConst.FIELD_M_ETKZ).equals(iPartsWireHarnessHelper.WIRE_HARNESS_ETKZ_WITH_ADDITIONAL_CHECK)
               && iPartsWireHarnessHelper.hasValidAdditionalWireHarnessFlag(destPartListEntry)
               && iPartsWireHarnessHelper.isWireHarnessPartListEntry(getProject(), destPartListEntry);
    }

    /**
     * Überprüft, ob die übergebene Position einen DIALOG ETK Kenner hat, der dafür sorgt, dass die Position bei der
     * Übernahme unterdrückt wird.
     *
     * @param destPartListEntry
     * @return
     */
    private boolean hasOmitETKValue(EtkDataPartListEntry destPartListEntry) {
        String etkValue = destPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETKZ);
        return iPartsVirtualCalcFieldDocuRel.ETK_VALUES.contains(etkValue);
    }

    /**
     * Stücklisteneinträge aus der Konstruktion (EDS) in die selektierten After-Sales-Module übernehmen.
     *
     * @param moduleMap
     * @param notExistingModuleMap - Map mit ModulId auf Zeile mit Informationen aus dem Übernahmedialog. Für Module, die
     *                             im Zuge der Übernahme erzeugt wurden und somit noch nicht in der DB existieren.
     * @param logMessages
     * @param messageLog
     * @throws EditTransferPartListEntriesException Exception falls beim Übernehmen von einem Stücklisteneintrag ein Fehler aufgetreten ist
     */
    public void createAndTransferPartListEntriesEDS_MBS(Map<String, List<TransferToASElement>> moduleMap,
                                                        Map<String, TransferToASElement> notExistingModuleMap,
                                                        List<String> logMessages, EtkMessageLog messageLog) throws EditTransferPartListEntriesException {
        final EtkProject project = getProject();
        this.messageLog = messageLog;
        // Zu übernehmende Stücklisteneinträge mit angepassten Feldern in alle selektierten Ziel-Module kopieren
        AssemblyId destAssemblyId = null;

        // Nur eine GenericEtkDataObjectList für das Speichern im ChangeSet verwenden
        EtkDataObjectList dataObjectListToBeSaved = new GenericEtkDataObjectList();

        startPseudoTransactionForActiveChangeSet(true);
        try {
            int currentRowIdx = 0;
            int maxPos = 0;
            for (List<TransferToASElement> values : moduleMap.values()) {
                if (!values.isEmpty()) {
                    maxPos += values.size() + (2 * EXTRA_INCREMENT_FOR_PROGRESS);
                }
            }
            int currentPos = 0;
            fireProgress(currentPos, maxPos);

//            // Helper für mögliche AS Stücklistenpositonen
//            ASUsageHelper asUsageHelper = new ASUsageHelper(getProject());
//            iPartsDIALOGPositionsHelper sourceDialogPositionsHelper = new iPartsDIALOGPositionsHelper(sourceConstAssembly.getPartListUnfiltered(null));

            boolean isMBS = (sourceId instanceof MBSStructureId);

            // Bei MBS Grundstücklisten brauchen wir zusätzlich die Baumuster Nummer
            String model = null;
            if (isMBS && ((MBSStructureId)sourceId).isBasePartlistId()) {
                model = iPartsVirtualNode.getModelNumberFromAssemblyId(sourceConstAssembly.getAsId());
            }

            for (Map.Entry<String, List<TransferToASElement>> entry : moduleMap.entrySet()) {
                if (isCancelled()) {
                    return;
                }
                List<TransferToASElement> transferList = entry.getValue();
                currentRowIdx++;
                String msg = "!!Übernehme %1 Datensätze in TU %2 von %3...";
                if (transferList.size() == 1) {
                    msg = "!!Übernehme %1 Datensatz in TU %2 von %3...";
                }
                fireMessage(msg, String.valueOf(transferList.size()), String.valueOf(currentRowIdx), String.valueOf(moduleMap.size()));

                if (transferList.isEmpty()) {
                    continue;
                }
                destAssemblyId = transferList.get(0).assemblyId;
                if (destAssemblyId != null) { // bei null konnte Modul nicht angelegt werden
                    EtkDataAssembly destAssembly = EtkDataObjectFactory.createDataAssembly(project, destAssemblyId, false);

                    // Stückliste muss nur bei bereits vorhandenen Modulen geladen werden, ansonsten neue Liste erzeugen
                    boolean assemblyIsNew = notExistingModuleMap.containsKey(destAssemblyId.getKVari());
                    DBDataObjectList<EtkDataPartListEntry> destPartList = EditModuleHelper.getDestPartList(destAssembly, assemblyIsNew);
                    if (destPartList == null) {
                        continue;
                    }

                    iPartsDocumentationType documentationType = null;
                    if (assemblyIsNew) {
                        // bei neuen Modulen den DocuTyp vom Produkt verwenden wenn es ein Produkt gibt
                        if (transferList.get(0).getProduct() != null) {
                            documentationType = transferList.get(0).getProduct().getDocumentationType();
                        } else {
                            // sonst gehen wir davon aus dass es ein SA Modul war, dort ist default BCS+
                            documentationType = iPartsDocumentationType.BCS_PLUS;
                        }
                    } else if (destAssembly instanceof iPartsDataAssembly) {
                        // bei vorhandenen Modulen des DocuTyp vom Modul verwenden
                        documentationType = ((iPartsDataAssembly)destAssembly).getDocumentationType();
                    }
                    // Fallback
                    if ((documentationType == null) || (documentationType == iPartsDocumentationType.UNKNOWN)) {
                        documentationType = iPartsDocumentationType.DIALOG_IPARTS;
                    }

                    iPartsModuleTypes moduleType = documentationType.getModuleType(false);
                    String sourceContext = EditConstructionToRetailHelper.createSourceContext(moduleType.getSourceType(), sourceId);

//                    // Alle Stücklisteneinträge VOR der Übernahme merken
//                    List<EtkDataPartListEntry> originalDestPartListEntries = destPartList.getAsList();

                    // die Ziel-Stückliste für schnelleren Zugriff als Map mit der sourceGUID als Schlüssel ablegen
                    // wird unter anderem verwendet um zu prüfen ob der Eintrag schon übernommen wurde
                    HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap = new HashMap<>();

                    VarParam<String> finalSeqNr = new VarParam<>("");
                    VarParam<Integer> destLfdNr = new VarParam<>(0);
                    // Startwert für die laufende Nummer und Sequenznummer auf Basis der höchsten existierenden laufenden
                    // Nummer bzw. Sequenznummer bestimmt
                    // maximaler Hotspot wird für EDS nicht benötigt, bleibt im Zweifelsfall leer
                    EditModuleHelper.preprocessDestPartListEntries(destPartList, destLfdNr, finalSeqNr, null,
                                                                   destPartListSourceGUIDMap, null, moduleType,
                                                                   sourceContext, null);

                    List<EtkDataPartListEntry> sourcePartListEntriesToTransferFiltered = new ArrayList<>();

                    // Kombinierte Texte und Fußnoten, die ggf. vererbt werden; wird aktuell beides bei EDS nicht gemacht
                    iPartsDataCombTextList combinedTextList = new iPartsDataCombTextList();
                    iPartsDataFootNoteCatalogueRefList fnCatalogueRefList = new iPartsDataFootNoteCatalogueRefList();

                    // Jetzt die neuen Stücklisteneinträge der Reihe nach anlegen und einfügen
                    for (TransferToASElement rowContent : transferList) {
                        if (isCancelled()) {
                            return;
                        }
                        // Fortschritt erhöhen
                        currentPos++;
                        fireProgress(currentPos, maxPos);

                        // DAIMLER-9238: laufende Nummer hochzählen und reservieren
                        destLfdNr.setValue(iPartsDataReservedPKList.getAndReserveNextKLfdNr(project, destAssemblyId, destLfdNr.getValue()));
                        // Den neuen Stücklisteneintrag erzeugen
                        PartListEntryId destPartListEntryId = new PartListEntryId(destAssemblyId.getKVari(), destAssemblyId.getKVer(),
                                                                                  EtkDbsHelper.formatLfdNr(destLfdNr.getValue()));
                        EtkDataPartListEntry destPartListEntry = EditConstructionToRetailHelper.createRetailPartListEntry(sourceContext,
                                                                                                                          rowContent.selectedPartlistEntry,
                                                                                                                          destPartListEntryId,
                                                                                                                          moduleType, isMBS,
                                                                                                                          project, logMessages);
                        // Einsortieren nur nach Hotspot (sourceGUID wird absichtlich leer übergeben)
                        EditModuleHelper.setHotSpotAndNextSequenceNumberELDAS(destPartListEntry, rowContent.hotspot,
                                                                              destPartList, finalSeqNr.getValue(), sourcePartListEntriesToTransferFiltered);

                        boolean usePRIMUSSuccessor;
                        EtkDataArray saaValidity = new EtkDataArray();
                        EtkDataArray modelValidity = new EtkDataArray();
                        if (isMBS) {
                            MBSStructureId mbsStructureId = (MBSStructureId)(sourceId);
                            if (mbsStructureId.isBasePartlistId()) {
                                // MBS: bei Grundstücklisten die Baumustergültigkeit setzen
                                if (model != null) {
                                    modelValidity.add(model);
                                }
                            } else {
                                // MBS: SAA-Gültigkeit aus der structureId bestimmen (nicht bei Grundstücklisten)
                                saaValidity.add(mbsStructureId.getListNumber());
                            }

                            // MBS: Code Gültigkeit übernehmen
                            String code = rowContent.selectedPartlistEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_CODE);

                            // DAIMLER-11945 Codenummer kürzen: bei 5-stelligen Code, die mit I anfangen, die ersten beiden
                            // Zeichen entfernen
                            code = CodeHelper.removeCodePrefixForLength(code, CodeHelper.CODE_PREFIX, 5, 2);

                            destPartListEntry.setFieldValue(iPartsConst.FIELD_K_CODES, code, DBActionOrigin.FROM_EDIT);

                            String level = rowContent.selectedPartlistEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_LEVEL);
                            destPartListEntry.setFieldValue(iPartsConst.FIELD_K_HIERARCHY, level, DBActionOrigin.FROM_EDIT);

                            // DAIMLER-10831 Falls der ET-KZ für MBS V ist, muss K_USE_PRIMUS_SUCCESSOR bei der Übernahme
                            // auf true gesetzt werden.
                            usePRIMUSSuccessor = rowContent.selectedPartlistEntry.getPart().getFieldValue(iPartsConst.FIELD_M_ETKZ_MBS).equals("V");
                        } else {
                            // EDS: SAA-Gültigkeit aus dem sourceContext bestimmen
                            saaValidity.add(sourceContext);

                            // DAIMLER-10831 Falls der ET-KZ für die aktuelle ET-Sicht V ist, muss K_USE_PRIMUS_SUCCESSOR
                            // bei der Übernahme auf true gesetzt werden. Schließt auch V* ein (der * bedeutet ja, dass es
                            // in anderen ET-Sichten auch noch Werte gibt)
                            String mMarketETKZ = rowContent.selectedPartlistEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_MARKET_ETKZ);
                            usePRIMUSSuccessor = mMarketETKZ.equals("V") || mMarketETKZ.equals("V*");
                        }

                        // SAA-Gültigkeit am neuen Stücklisteneintrag schreiben
                        if (!saaValidity.isEmpty()) {
                            String arrayId = project.getDbLayer().getNewArrayNo(TableAndFieldName.make(iPartsConst.TABLE_KATALOG,
                                                                                                       iPartsConst.FIELD_K_SA_VALIDITY),
                                                                                destPartListEntry.getAsId().toString("|"),
                                                                                false);
                            saaValidity.setArrayId(arrayId);
                            destPartListEntry.setIdForArray(iPartsConst.FIELD_K_SA_VALIDITY, arrayId, DBActionOrigin.FROM_EDIT);
                        } else {
                            saaValidity = null;
                        }
                        destPartListEntry.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, saaValidity, DBActionOrigin.FROM_EDIT);

                        // Baumustergültigkeit am neuen Stücklisteneintrag schreiben
                        if (!modelValidity.isEmpty()) {
                            String arrayId = project.getDbLayer().getNewArrayNo(TableAndFieldName.make(iPartsConst.TABLE_KATALOG,
                                                                                                       iPartsConst.FIELD_K_MODEL_VALIDITY),
                                                                                destPartListEntry.getAsId().toString("|"),
                                                                                false);
                            modelValidity.setArrayId(arrayId);
                            destPartListEntry.setIdForArray(iPartsConst.FIELD_K_MODEL_VALIDITY, arrayId, DBActionOrigin.FROM_EDIT);
                        } else {
                            modelValidity = null;
                        }
                        destPartListEntry.setFieldValueAsArray(iPartsConst.FIELD_K_MODEL_VALIDITY, modelValidity, DBActionOrigin.FROM_EDIT);

                        destPartListEntry.setFieldValueAsBoolean(iPartsConst.FIELD_K_USE_PRIMUS_SUCCESSOR, usePRIMUSSuccessor,
                                                                 DBActionOrigin.FROM_EDIT);

                        // DAIMLER-10948: Sowohl bei EDS als auch MBS eine K_SOURCE_GUID analog der aus der ELDAS Migration verwenden
                        String sourceGUID = EditConstructionToRetailHelper.createNonDIALOGSourceGUID(destPartListEntry.getAsId());
                        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_GUID, sourceGUID, DBActionOrigin.FROM_EDIT);

                        EditModuleHelper.finishPartListEntryCreation(destPartListSourceGUIDMap, rowContent.getSourceGUIDForAttribute(),
                                                                     destPartListEntry, sourcePartListEntriesToTransferFiltered,
                                                                     finalSeqNr);

                        if (onModifyCreatedRetailPartListEntryEvent != null) {
                            onModifyCreatedRetailPartListEntryEvent.onModifyCreatedPartListEntryEDS(getProject(), destPartListEntry,
                                                                                                    rowContent, combinedTextList,
                                                                                                    fnCatalogueRefList);
                        }
                    }

                    // Die virtuellen Felder aus der Konstruktion können benutzt werden, weil sie beim Erstellen der "destPartListEntry"s
                    // mit übernommen wurden -> siehe  EditConstructionToRetailHelper.createRetailPartListEntry()
                    Map<String, List<EtkDataPartListEntry>> wwKeyToPLEsMap = getWWCandidatesMap(sourcePartListEntriesToTransferFiltered,
                                                                                                isMBS);
                    String wwGuid = "";
                    if (destAssembly instanceof iPartsDataAssembly) {
                        iPartsDataAssembly destiPartsDataAssembly = (iPartsDataAssembly)destAssembly;
                        wwGuid = destiPartsDataAssembly.getNextUnusedWWGUID();
                    }
                    if (!wwGuid.isEmpty()) {
                        for (List<EtkDataPartListEntry> wwCandidates : wwKeyToPLEsMap.values()) {
                            if (wwCandidates.size() > 1) {
                                for (EtkDataPartListEntry wwCandidate : wwCandidates) {
                                    wwCandidate.setFieldValue(iPartsConst.FIELD_K_WW, wwGuid, DBActionOrigin.FROM_DB);
                                }
                                wwGuid = Integer.toString(Integer.valueOf(wwGuid) + 1);
                            }
                        }
                    }

                    // Jetzt die neuen Einträge zur Stückliste hinzufügen
                    for (EtkDataPartListEntry partListEntry : sourcePartListEntriesToTransferFiltered) {
                        destPartList.add(partListEntry, DBActionOrigin.FROM_EDIT);
                    }

                    if (isCancelled()) {
                        return;
                    }
                    currentPos += EXTRA_INCREMENT_FOR_PROGRESS;
                    fireProgress(currentPos, maxPos);

                    if (isRevisionChangeSetActiveForEdit()) {
                        if (isCancelled()) {
                            return;
                        }
                        currentPos += EXTRA_INCREMENT_FOR_PROGRESS;
                        fireProgress(currentPos, maxPos);
                        EditModuleHelper.finishModuleModificationEDS(dataObjectListToBeSaved, destPartList, assemblyIsNew,
                                                                     destAssembly, combinedTextList, fnCatalogueRefList, true);
                        if (onModifyCreatedRetailPartListEntryEvent != null) {
                            onModifyCreatedRetailPartListEntryEvent.onModifyCreatedAssemblyEDS(getProject(), dataObjectListToBeSaved,
                                                                                               assemblyIsNew,
                                                                                               destAssembly);
                        }
                    } else {
                        // Ziel-Stückliste abspeichern und aus dem Cache löschen
                        destPartList.saveToDB(project, false);
                        EtkDataAssembly.removeDataAssemblyFromCache(getProject(), destAssembly.getAsId());
                    }
                }
            }
        } catch (Exception e) {
            throw new EditTransferPartListEntriesException(TranslationHandler.translate("!!Fehler beim Übernehmen der Positionen für das Modul \"%1\"!",
                                                                                        (destAssemblyId != null) ? destAssemblyId.getKVari() : "?"), e);
        } finally {
            stopPseudoTransactionForActiveChangeSet();
            this.messageLog = null;
        }

        try {
            if (!dataObjectListToBeSaved.isEmpty() && isRevisionChangeSetActiveForEdit()) {
                addDataObjectListToActiveChangeSetForEdit(dataObjectListToBeSaved);
            }
        } catch (Exception e) {
            throw new EditTransferPartListEntriesException(TranslationHandler.translate("!!Fehler beim Speichern."), e);
        }
    }

    /**
     * Falls ein WW-Kennzeichen am Stücklisteneintrag vorhanden ist, muss ein Schlüssel berechnet werden, der für alle für
     * ein WW-Set potentielle Stücklisteneinträge gleich sein muss.
     * Mit diesem Schlüssel wird die Map gefüllt und zurückgegeben.
     * Falls schon ein oder mehrere Stücklisteneintrag zu diesem Schlüssel existieren, muss die Materialnummer abgegelichen werden.
     * Stücklisteneinträge mit gleichen MatNr dürfen kein WW-Set ergeben.
     *
     * @param sourcePartListEntriesToTransferFiltered
     * @param isMBS
     */
    private Map<String, List<EtkDataPartListEntry>> getWWCandidatesMap(List<EtkDataPartListEntry> sourcePartListEntriesToTransferFiltered,
                                                                       boolean isMBS) {
        Map<String, List<EtkDataPartListEntry>> wwKeyToPLEsMap = new HashMap<>();

        // Die virtuellen Felder aus der Konstruktion können benutzt werden, weil sie beim Erstellen der "destPartListEntry"s
        // mit übernommen wurden -> siehe  EditConstructionToRetailHelper.createRetailPartListEntry()
        String wwFlagField;
        String snrField;
        String levelField;
        if (isMBS) {
            wwFlagField = iPartsDataVirtualFieldsDefinition.MBS_WW_FLAG;
            snrField = iPartsDataVirtualFieldsDefinition.MBS_SNR;
            levelField = iPartsDataVirtualFieldsDefinition.MBS_LEVEL;
        } else {
            wwFlagField = iPartsDataVirtualFieldsDefinition.EDS_WWKB;
            snrField = iPartsDataVirtualFieldsDefinition.EDS_SNR;
            levelField = iPartsDataVirtualFieldsDefinition.EDS_LEVEL;
        }
        for (EtkDataPartListEntry partListEntryToTransfer : sourcePartListEntriesToTransferFiltered) {
            String wwFlag = partListEntryToTransfer.getFieldValue(wwFlagField);
            if (!wwFlag.isEmpty()) {
                String wwKey = calculateWWKey(partListEntryToTransfer, wwFlagField, levelField, snrField);
                List<EtkDataPartListEntry> wwCandidates = wwKeyToPLEsMap.get(wwKey);
                if (wwCandidates != null) {
                    boolean sameMatNr = false;
                    for (EtkDataPartListEntry wwCandidate : wwCandidates) {
                        // Stücklisteneinträge mit gleichen MatNr dürfen kein Set ergeben. Nicht zur Liste der Kandidaten hizufügen
                        if (wwCandidate.getFieldValue(iPartsConst.FIELD_K_MATNR).equals(partListEntryToTransfer.getFieldValue(iPartsConst.FIELD_K_MATNR))) {
                            sameMatNr = true;
                            break;
                        }
                    }
                    if (!sameMatNr) {
                        wwCandidates.add(partListEntryToTransfer);
                    }
                } else {
                    List<EtkDataPartListEntry> list = new ArrayList<>();
                    list.add(partListEntryToTransfer);
                    wwKeyToPLEsMap.put(wwKey, list);
                }
            }
        }

        return wwKeyToPLEsMap;
    }

    /**
     * Gemeinsamen Schlüssel berechnen, den alle Stücklisteneinträge für einen identischen Wahlweise-Wert haben müssen
     * -> Gleiche obere Sachnummer, gleiche Struktustufe und gleicher Wahlweisekenner
     *
     * @param partListEntry
     * @param wwFlagField
     * @param levelField
     * @param snrField
     * @return
     */
    private String calculateWWKey(EtkDataPartListEntry partListEntry, String wwFlagField, String levelField, String snrField) {
        String str = partListEntry.getFieldValue(snrField) + // Obere Sachnummer
                     iPartsConst.K_SOURCE_CONTEXT_DELIMITER +
                     partListEntry.getFieldValue(levelField) + // Strukturstufe
                     iPartsConst.K_SOURCE_CONTEXT_DELIMITER +
                     partListEntry.getFieldValue(wwFlagField); // WW-Kennzeichen
        return str;
    }

    /**
     * Bei der AS-Übernahme soll geprüft werden, sind zu der übernehmenden Position mit ET-Zählerstand und ET-KZ = E weitere
     * V-Positionen ohne ET-Zählerstand mit gleicher POS, PV, AA und Code vorhanden.
     * Falls ja, ermittle den Stand mit dem höchsten KEM-Datum-Ab, bei dem ETZ blank und ET-KZ = V.
     * Die ermittelten Position ist die Quelle der Kopplung. Die übernommene Position mit ET-Zählerstand ist das Ziel der Kopplung.
     *
     * @param partListEntriesToTransfer      Potentielle Ziele der Kopplungen -> zu übernehmende Positionen
     * @param sourceDialogPositionsHelper    Dialog Positions Helper
     * @param changedDataInChangeSetToCommit Änderungen in DIALOG
     * @param changedDataWithoutChangeSet    Änderungen die bei der Kopplung entstehen
     * @param afterOverwrite                 Änderungen die beim Überschreiben einer vorherigen Kopplung entstehen
     */
    private void getFactoryDataLinkingCandidates(List<EtkDataPartListEntry> partListEntriesToTransfer,
                                                 iPartsDIALOGPositionsHelper sourceDialogPositionsHelper,
                                                 iPartsDataFactoryDataList changedDataInChangeSetToCommit,
                                                 EtkDataObjectList<? extends EtkDataObject> changedDataWithoutChangeSet,
                                                 iPartsDataFactoryDataList afterOverwrite) {
        if (!partListEntriesToTransfer.isEmpty()) {
            // Falls an der Baureihe keine Kopplung der Werksdaten gewünscht ist, hier abbrechen
            // Es reicht, die Baureihe vom ersten Stücklisteneintrag zu prüfen, weil alle aus derselben DIALOG-Baureihe kommen
            String seriesNumber = partListEntriesToTransfer.get(0).getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO);
            iPartsDialogSeries dialogSeries = iPartsDialogSeries.getInstance(getProject(), new iPartsSeriesId(seriesNumber));
            if (!dialogSeries.isVPositionCheckAndLinkingActive()) {
                return;
            }
        }

        for (EtkDataPartListEntry partListEntry : partListEntriesToTransfer) {
            boolean isSameLinkingThanAlreadyExistingLinking = false;
            String sourceGUIDPartListEntry = partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
            for (EtkDataObject dataObject : changedDataWithoutChangeSet) {
                if (dataObject instanceof iPartsDataDialogData) {
                    String dialogGUID = dataObject.getFieldValue(iPartsConst.FIELD_DD_GUID);
                    // An der Quelle wurde schon die Ziel-GUID hinterlegt
                    // Werksdaten wurden in dieser Quelle-Ziel Zusammensetzung schon in einem
                    // vorherigen Durchlauf gekoppelt
                    // Bei gleichzeitiger Übernahme einer Teilepos in verschiedene TUs
                    if (sourceGUIDPartListEntry.equals(dialogGUID)) {
                        isSameLinkingThanAlreadyExistingLinking = true;
                    }
                }
            }
            if (isSameLinkingThanAlreadyExistingLinking) {
                break;
            }
            // Falls zu übernehmende Teilepos schon eine Quelle für eine andere Kopplung ist -> keine erneute Kopplung möglich
            if (!CopyAndPasteData.isTargetAlreadyASource(getProject(), sourceGUIDPartListEntry)) {
                String partListEntryETZ = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETZ);
                String partListEntryMatETKZ = partListEntry.getPart().getFieldValue(iPartsConst.FIELD_M_ETKZ);
                // ET-Zähler der zu übernehmenden Position darf nicht leer sein
                // ET-KZ muss E sein
                if (!partListEntryETZ.isEmpty() && partListEntryMatETKZ.equals("E")) {
                    // Positionsvarianten mit gleicher PV und AA suchen
                    // Absteigend nach KEM Datum ab sortieren
                    List<EtkDataPartListEntry> positionVariantsSortedKEMDateAndFiltered = getPositionVariantsSortedKEMDateAndFiltered(sourceDialogPositionsHelper, partListEntry);
                    if (positionVariantsSortedKEMDateAndFiltered.isEmpty()) {
                        continue;
                    }
                    iPartsDataFactoryDataList partListEntryFactoryDataAS =
                            iPartsDataFactoryDataList.loadAfterSalesFactoryDataListForDialogPositionsVariant(project, sourceGUIDPartListEntry, true);
                    for (EtkDataPartListEntry positionVariant : positionVariantsSortedKEMDateAndFiltered) {
                        String sourceGUIDPositionVariant = positionVariant.getFieldValue(iPartsConst.FIELD_K_LFDNR);
                        iPartsDataDialogData dialogEntry = new iPartsDataDialogData(getProject(), new iPartsDialogId(sourceGUIDPartListEntry));
                        String targetAlreadyLinkedSourceGUID = dialogEntry.getFieldValue(iPartsConst.FIELD_DD_LINKED_FACTORY_DATA_GUID);
                        // Wenn dieses Ziel schon mit dieser Quelle gekoppelt ist, ist nichts zu tun
                        // passiert bei nochmaliger Übernahme in einen anderen TU
                        if (!targetAlreadyLinkedSourceGUID.isEmpty()) {
                            if (targetAlreadyLinkedSourceGUID.equals(sourceGUIDPositionVariant)) {
                                break;
                            }
                        }
                        // Passende Positionsvariante gefunden -> Werksdaten laden
                        iPartsDataFactoryDataList factoryDataAS =
                                iPartsDataFactoryDataList.loadAfterSalesFactoryDataListForDialogPositionsVariant(project, sourceGUIDPositionVariant, true);
                        iPartsDataFactoryDataList factoryData =
                                iPartsDataFactoryDataList.loadConstructionFactoryDataListForDialogPositionsVariant(project, sourceGUIDPositionVariant, true);
                        factoryData.addAll(factoryDataAS, DBActionOrigin.FROM_DB);
                        if (!factoryData.isEmpty()) {
                            // Filtert die Daten nach freigegeben Datensätzen und bevorzugt Daten aus AS
                            iPartsDataFactoryDataList filteredFactoryDataList = FactoryDataHelper.getDataForCopy(factoryData);
                            // Kopieren
                            CopyAndPasteData.copyFactoryDataOfPart(filteredFactoryDataList, true, isSomethingLinkedInFactoryDataList(factoryDataAS),
                                                                   positionVariant.getFieldValue(iPartsConst.FIELD_K_LFDNR));
                            // Falls Positionsvariante schon Quelle einer anderen Kopplung ist, darf nicht erneut gekoppelt werden
                            if (!CopyAndPasteData.isSourceAlreadyATarget()) {
                                // Ist zu übernehmende Position schon ein Ziel einer anderen Kopplung -> Kopplung
                                // überschreiben
                                CopyAndPasteData.overwriteLinkFlag(afterOverwrite, partListEntryFactoryDataAS.getAsList());
                                // Einfügen und Koppeln
                                CopyAndPasteData.pasteAndLink(partListEntry, null, getProject(), changedDataInChangeSetToCommit,
                                                              changedDataWithoutChangeSet, false);
                                // Kopieren Cache leeren
                                CopyAndPasteData.clearCopyCache();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Positionsvarianten mit gleicher PV und AA suchen
     * Absteigend nach KEM Datum ab sortieren und filtern:
     * - Die Code der Positionsvariante muss gleich sein
     * - ET-Zähler der Positionsvariante muss leer sein
     * - ETKZ muss V sein
     *
     * @param sourceDialogPositionsHelper
     * @param partListEntry
     * @return
     */
    private List<EtkDataPartListEntry> getPositionVariantsSortedKEMDateAndFiltered(iPartsDIALOGPositionsHelper sourceDialogPositionsHelper,
                                                                                   EtkDataPartListEntry partListEntry) {
        // Positionsvarianten mit gleicher PV und AA suchen
        List<EtkDataPartListEntry> positionVariants = sourceDialogPositionsHelper.getPositionVariantsWithPVAndAACheck(partListEntry, false);
        if (positionVariants.isEmpty()) {
            return positionVariants;
        }
        // Absteigend nach KEM Datum ab sortieren
        List<EtkDataPartListEntry> positionVariantsSortedKEMDate = positionVariants.stream()
                .sorted((o1, o2) -> o2.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA)
                        .compareTo(o1.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA)))
                .collect(Collectors.toList());
        // und jetzt filtern
        positionVariants.clear();
        // Die Code der Positionsvariante muss gleich sein
        String partListEntryCode = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES);
        for (EtkDataPartListEntry positionVariant : positionVariantsSortedKEMDate) {
            // Die Code der Positionsvariante muss gleich sein
            if (partListEntryCode.equals(positionVariant.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES))) {
                String positionVariantETZ = positionVariant.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETZ);
                String positionVariantMatETKZ = positionVariant.getPart().getFieldValue(iPartsConst.FIELD_M_ETKZ);
                // ET-Zähler der Positionsvariante muss leer sein
                // ET-KZ muss V sein
                if (positionVariantETZ.isEmpty() && positionVariantMatETKZ.equals("V")) {
                    positionVariants.add(positionVariant);
                }
            }
        }
        return positionVariants;
    }

    /**
     * Speichert die Änderungen, die durch das Koppeln entstanden sind
     *
     * @param changedDataWithoutChangeSet    Änderungen in DA-DIALOG
     * @param changedDataInChangeSetToCommit Der Datensatz der Committed wird, der durch das Koppeln entstanden ist
     * @param afterOverwrite                 Der Datensatz der Committed wird, der durch das Entkoppeln entstanden ist
     * @param changeSet                      {@link AbstractRevisionChangeSet} zum Speichern der gekoppelten Daten
     */
    private void saveLinking(iPartsDataFactoryDataList changedDataInChangeSetToCommit,
                             EtkDataObjectList<? extends EtkDataObject> changedDataWithoutChangeSet,
                             iPartsDataFactoryDataList afterOverwrite,
                             AbstractRevisionChangeSet changeSet) {
        if (changeSet != null) {
            CopyAndPasteData.saveChangesForLinkingRunnable(getProject(),
                                                           changedDataWithoutChangeSet,
                                                           changedDataInChangeSetToCommit,
                                                           afterOverwrite,
                                                           changeSet,
                                                           iPartsConst.FIELD_DFD_STATUS,
                                                           iPartsConst.FIELD_DFD_SERIES_NO);
        }
    }

    private boolean isSomethingLinkedInFactoryDataList(iPartsDataFactoryDataList factoryDataAS) {
        for (iPartsDataFactoryData dataObject : factoryDataAS) {
            if (CopyAndPasteData.isFactoryDataAlreadyLinked(dataObject)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience-Methode für {@link EtkRevisionsHelper#startPseudoTransactionForActiveChangeSet(EtkProject, boolean)}
     * vom {@link EtkDbs} des {@link EtkProject}s.
     *
     * @param startDelayed Soll bei aktiver GetRecords-Simulation diese verwendet werden anstatt einer Pseudo-Transaktion?
     * @return
     */
    public void startPseudoTransactionForActiveChangeSet(boolean startDelayed) {
        getProject().startPseudoTransactionForActiveChangeSet(startDelayed);
    }

    /**
     * Convenience-Methode für {@link EtkRevisionsHelper#stopPseudoTransactionForActiveChangeSet(EtkProject)} vom {@link EtkDbs}
     * des {@link EtkProject}s.
     *
     * @return
     */
    public void stopPseudoTransactionForActiveChangeSet() {
        getProject().stopPseudoTransactionForActiveChangeSet();
    }

    /**
     * Convenience-Methode für {@link EtkRevisionsHelper#addDataObjectListToActiveChangeSetForEdit(de.docware.framework.modules.db.DBDataObjectList)} von
     * {@link #getProject().getRevisionsHelper()}.
     *
     * @param dataObjectList
     * @return Liste der serialisierten {@link EtkDataObject}s als {@link SerializedDBDataObject}s bzw {@code null} falls
     * das jeweils entsprechende {@link EtkDataObject} unverändert war.
     */
    public List<SerializedDBDataObject> addDataObjectListToActiveChangeSetForEdit(EtkDataObjectList dataObjectList) {
        EtkRevisionsHelper revisionsHelper = getProject().getRevisionsHelper();
        if (revisionsHelper != null) {
            return revisionsHelper.addDataObjectListToActiveChangeSetForEdit(dataObjectList);
        } else {
            return null;
        }
    }

    /**
     * Convenience-Methode für {@link EtkRevisionsHelper#isRevisionChangeSetActiveForEdit()} von {@link #getProject().getRevisionsHelper()}.
     *
     * @return
     */
    public boolean isRevisionChangeSetActiveForEdit() {
        return getProject().isRevisionChangeSetActiveForEdit();
    }


    /**
     * Vererbt die AS Attribute "kombinierter Text", "AS-Coderegel", "Ereignisse ab/bis" und "Fußnoten"
     * von Stücklistenpositionen, die schon im AS existieren, auf neu erzeugte AS-Positionen.
     * <p>
     * Bei den AS Referenzpositionen wird zwischen GUID und KEM Positionen unterschieden. GUID Positionen besitzen die
     * gleichen BCTE Attribute wie der Zielpositionsstücklisteneintrag und können sich in der AA unterscheiden (über alle
     * Produkte und Stücklisten hinweg). Bei KEM Positionen muss ein KEM Stand Unterschied vorliegen (im gleichen Produkt
     * und gleicher Zielstückliste).
     * <p>
     * GUID Positionen werden gegenüber den KEM Positionen bevorzugt. Außer bei Fußnoten: Fußnoten sind AA abhängig, d.h.
     * wenn eine GUID Position existiert und die AA eine andere ist als bei der Zielposition, dann wird bei der GUID-
     * sondern bei der KEM Position nach möglichen Fußnoten geschaut (sofern eine KEM Position existiert).
     *
     * @param destPartListEntry
     * @param originalDestPartListEntries
     * @param combinedTextList
     * @param fnCatalogueRefList
     * @param asUsageHelper
     * @param isTransferFromConstPartsList
     */
    private void inheritValuesFromReference(EtkDataPartListEntry destPartListEntry,
                                            List<EtkDataPartListEntry> originalDestPartListEntries,
                                            iPartsDataCombTextList combinedTextList,
                                            iPartsDataFootNoteCatalogueRefList fnCatalogueRefList,
                                            ASUsageHelper asUsageHelper, iPartsOmittedParts omittedParts,
                                            boolean isTransferFromConstPartsList) {
        EditTransferPartlistPredictionGrid.PartListEntryReferenceKey referenceKey = buildReferenceKey(destPartListEntry, false);
        // AS Position, die via Original-GUID gefunden wurde
        EtkDataPartListEntry referenceDestPartListEntryForGuid = asUsageHelper.getFirstASPartListEntryForBCTEWithOutAA(referenceKey.getReferenceKeyByAA());
        // Flag, ob eine GUID Position vorliegt und die Attribute daraus kopiert werden
        boolean copyASDataFromGUIDReference = referenceDestPartListEntryForGuid != null;
        // AS Position mit gleichen BCTE Attributen und anderem KEM Stand (KEM Datum)
        EtkDataPartListEntry referenceDestPartListEntryForKEM = null;
        // Flag, ob die GUID Referenz die gleiche AA hat, wie die Zielposition
        boolean copyFootnotesFromASGUIDReference = copyASDataFromGUIDReference && referenceDestPartListEntryForGuid.getFieldValue(iPartsConst.FIELD_K_AA).equals(referenceKey.getReferenceKeyByAA().getOriginalAA());
        if (!copyFootnotesFromASGUIDReference) {
            referenceDestPartListEntryForKEM = findReferencePartListEntry(referenceKey, originalDestPartListEntries);
        }
        // GUID Positionen werden bevorzugt
        EtkDataPartListEntry referenceDestPartListEntry = copyASDataFromGUIDReference ? referenceDestPartListEntryForGuid : referenceDestPartListEntryForKEM;
        boolean isSearchCombTextInGenVo = false;
        if (referenceDestPartListEntry != null) {
            boolean oldLogLoadFieldIfNeeded = referenceDestPartListEntry.isLogLoadFieldIfNeeded();
            referenceDestPartListEntry.setLogLoadFieldIfNeeded(false); // Feld _K_CODES_CONST ist z.B. in der Regel nicht geladen
            try {
                // DAIMLER-6210 Übernahme in AS Stückliste anpassen
                String codesAS = referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_CODES);
                String hierarchyAS = referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_HIERARCHY);
                // Unterscheidung, ob die Daten von einer GUID Position oder von einer KEM Position übernommen werden.
                // Bei GUID Positionen, werden die AS Werte unabhängig von den Konstruktionswerten übernommen.
                if (!copyASDataFromGUIDReference) {
                    // AS-Codes und gekürzte AS-Codes nur übernehmen, wenn die AS-Coderegel ungleich der Entwicklungscoderegel ist
                    String codesConst = referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_CODES_CONST);
                    if (!codesAS.equals(codesConst)) {
                        setCodeFromReference(codesAS, destPartListEntry, referenceDestPartListEntry);
                    }
                    // AS-Strukturstufe nur setzen, wenn es sich nicht um eine echte Übernahme aus der Konstruktion handelt
                    if (!isTransferFromConstPartsList && !hierarchyAS.equals(referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_HIERARCHY_CONST))) {
                        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_HIERARCHY, hierarchyAS, DBActionOrigin.FROM_EDIT);
                    }
                } else {
                    // AS-Codes und gekürzten AS-Code
                    setCodeFromReference(codesAS, destPartListEntry, referenceDestPartListEntry);
                    // AS-Menge -> ab DAIMLER-9891: nur vererben wenn AA gleich ist
                    if (destPartListEntry.getFieldValue(iPartsConst.FIELD_K_AA).equals(referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_AA))) {
                        String quantity = referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_MENGE);
                        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_MENGE, quantity, DBActionOrigin.FROM_EDIT);
                    }
                    if (!isTransferFromConstPartsList) {
                        // AS-Strukturstufe nur setzen, wenn es sich nicht um eine echte Übernahme aus der Konstruktion handelt
                        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_HIERARCHY, hierarchyAS, DBActionOrigin.FROM_EDIT);
                    }
                    // Ereignisse nur von GUID Positionen übernehmen
                    // AS Ereignis ab
                    String eventFrom = referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_EVENT_FROM);
                    destPartListEntry.setFieldValue(iPartsConst.FIELD_K_EVENT_FROM, eventFrom, DBActionOrigin.FROM_EDIT);
                    // AS Ereignis bis
                    String eventTo = referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_EVENT_TO);
                    destPartListEntry.setFieldValue(iPartsConst.FIELD_K_EVENT_TO, eventTo, DBActionOrigin.FROM_EDIT);
                    // Konstruktions-Ereignis ab
                    String eventFromConst = referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_EVENT_FROM_CONST);
                    destPartListEntry.setFieldValue(iPartsConst.FIELD_K_EVENT_FROM_CONST, eventFromConst, DBActionOrigin.FROM_EDIT);
                    // Konstruktions-Ereignis bis
                    String eventToConst = referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_EVENT_TO_CONST);
                    destPartListEntry.setFieldValue(iPartsConst.FIELD_K_EVENT_TO_CONST, eventToConst, DBActionOrigin.FROM_EDIT);
                }


                // Kombinierte Texte
                if (!referenceDestPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT).isEmpty()) {
                    // Neuen kombinierten Text anlegen
                    iPartsDataCombTextList combList = iPartsDataCombTextList.loadForPartListEntry(referenceDestPartListEntry.getAsId(),
                                                                                                  getProject());
                    for (iPartsDataCombText dataCombText : combList) {
                        iPartsCombTextId id = new iPartsCombTextId(destPartListEntry.getAsId(), dataCombText.getAsId().getTextSeqNo());
                        iPartsDataCombText newDataCombText = new iPartsDataCombText(getProject(), id);
                        newDataCombText.assignAttributes(getProject(), dataCombText.getAttributes(), false, DBActionOrigin.FROM_EDIT);

                        // ID wird durch assignAttributes() auf die ID von dataCombText geändert
                        newDataCombText.setId(id, DBActionOrigin.FROM_EDIT);
                        newDataCombText.updateOldId();
                        newDataCombText.removeForeignTablesAttributes();  // da loadForPartListEntry ggf nach LangText sucht
                        combinedTextList.add(newDataCombText, DBActionOrigin.FROM_EDIT);
                    }
                } else {
                    // Es gibt keine Ergänzungstexte zu vererben -> Gibt es Texte bei GenVo?
                    isSearchCombTextInGenVo = true;
                }
                // Existiert eine AS Position, die über die GUID bestimmt wurde und hat diese AS Position die gleiche
                // AA wie die Ziel-Position, dann übernehme die Fußnoten von der GUID-Position. Stimmen die AA nicht
                // überein, dann übernehme die Fußnoten der AS Position mit anderem KEM Stand.
                EtkDataPartListEntry entryForFootnotes = copyFootnotesFromASGUIDReference ? referenceDestPartListEntryForGuid : referenceDestPartListEntryForKEM;
                if (entryForFootnotes != null) {
                    // Fußnoten
                    if (entryForFootnotes instanceof iPartsDataPartListEntry) {
                        EditEqualizeFootNoteHelper.inheritOnePartListEntryFootNote(getProject(), fnCatalogueRefList,
                                                                                   entryForFootnotes, destPartListEntry);
                    }
                }
            } finally {
                referenceDestPartListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
        } else {
            // Es gibt kein Stücklisteneintrag von dem Ergänzungstexte zu verebt werden können
            // -> Gibt es Texte bei GenVo?
            isSearchCombTextInGenVo = true;
        }

        // Wenn es sich um eine Verrechnungs-SNR (DA_OMITTED_PARTS) handelt, dann darf kein GenVO Text angelegt werden
        if (isSearchCombTextInGenVo) {
            if (omittedParts.isOmittedPart(destPartListEntry)) {
                isSearchCombTextInGenVo = false;
            }
        }

        // Ergänzungstexte bei GenVo suchen und gegebenenfalls zur weiterverarbeitung aufnehmen
        if (isSearchCombTextInGenVo) {
            String genVoNo = destPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO);
            if (StrUtils.isValid(genVoNo)) {
                String retailText = destPartListEntry.getPart().getFieldValue(iPartsConst.FIELD_M_TEXTNR, Language.DE.getCode(), false);
                iPartsDataGenVoSuppTextList dataForGenVoNoList = iPartsDataGenVoSuppTextList.loadDataForGenVoNo(getProject(), genVoNo);
                int textSqNo = 1;
                for (iPartsDataGenVoSuppText genVoSuppTextData : dataForGenVoNoList) {
                    EtkMultiSprache multiLang = genVoSuppTextData.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DA_GENVO_DESCR);
                    String genVOText = multiLang.getText(Language.DE.getCode());
                    // GenVO Text nur anlegen, wenn er sich von der deutschen Retailbenennung (incl Umlaute) unterscheidet
                    if (!equalsIgnoreCaseAndUmlaute(retailText, genVOText)) {
                        iPartsCombTextId id = new iPartsCombTextId(destPartListEntry.getAsId(), EtkDbsHelper.formatLfdNr(textSqNo));
                        iPartsDataCombText newDataCombText = new iPartsDataCombText(getProject(), id);
                        newDataCombText.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        newDataCombText.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DCT_DICT_TEXT, multiLang, DBActionOrigin.FROM_EDIT);
                        newDataCombText.setFieldValueAsBoolean(iPartsConst.FIELD_DCT_SOURCE_GENVO, true, DBActionOrigin.FROM_EDIT);
                        combinedTextList.add(newDataCombText, DBActionOrigin.FROM_EDIT);
                        textSqNo++;
                    }
                }
            }
        }

        // Setzt das virtuelle Feld RETAIL_COMB_TEXT_SOURCE_GENVO
        if (destPartListEntry instanceof iPartsDataPartListEntry) {
            ((iPartsDataPartListEntry)destPartListEntry).updateCombTextSourceGenVO(combinedTextList);
        }
    }

    private boolean equalsIgnoreCaseAndUmlaute(String textOne, String textTwo) {
        return convertUmlautText(textOne).equalsIgnoreCase(convertUmlautText(textTwo));
    }

    private String convertUmlautText(String text) {
        text = StrUtils.nullToEmpty(text);
        int len = text.length();
        if (len == 0) {
            return text;
        }
        StringBuilder str = new StringBuilder(len + len / 8);
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            switch (c) {
                case 'ä':
                    str.append("ae");
                    break;
                case 'ö':
                    str.append("oe");
                    break;
                case 'ü':
                    str.append("ue");
                    break;
                case 'ß':
                    str.append("ss");
                    break;
                case 'Ä':
                    str.append("Ae");
                    break;
                case 'Ö':
                    str.append("Oe");
                    break;
                case 'Ü':
                    str.append("Ue");
                    break;
                default:
                    str.append(c);
                    break;
            }
        }
        return str.toString();
    }

    /**
     * Setzt den AS Code und kopiert den gekürzten AS Code von der Referenzposition
     *
     * @param codesAS
     * @param destPartListEntry
     * @param referenceDestPartListEntry
     */
    private void setCodeFromReference(String codesAS, EtkDataPartListEntry destPartListEntry, EtkDataPartListEntry
            referenceDestPartListEntry) {
        if ((destPartListEntry == null) || (referenceDestPartListEntry == null)) {
            return;
        }
        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_CODES, codesAS, DBActionOrigin.FROM_EDIT);
        String reducedCode = referenceDestPartListEntry.getFieldValue(iPartsConst.FIELD_K_CODES_REDUCED);
        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_CODES_REDUCED, reducedCode, DBActionOrigin.FROM_EDIT);

    }

    private EtkDataPartListEntry findReferencePartListEntry
            (EditTransferPartlistPredictionGrid.PartListEntryReferenceKey referenceKey,
             List<EtkDataPartListEntry> retailPartListEntries) {
        EtkDataPartListEntry result = null;
        long closestDateDiff = Long.MAX_VALUE;
        for (EtkDataPartListEntry partListEntry : retailPartListEntries) {
            // BCTE-Schlüssel außer SData (dateFrom) muss identisch sein
            EditTransferPartlistPredictionGrid.PartListEntryReferenceKey retailRefKey = buildReferenceKey(partListEntry, false);
            if (referenceKey.getBcteKeyWithoutSData().equals(retailRefKey.getBcteKeyWithoutSData())) {
                // Suche die Referenzteileposition mit dem nächst kleineren dateFrom zur neuen Teileposition (referenceDateFrom)
                long dateDiff = referenceKey.getDateFrom() - retailRefKey.getDateFrom();
                if (dateDiff < closestDateDiff) {
                    closestDateDiff = dateDiff;

                    // TextId muss übereinstimmen -> nur dann ist die Referenzteileposition gültig
                    if (referenceKey.getTextId().equals(retailRefKey.getTextId())) {
                        result = partListEntry;
                    } else {
                        // Wenn die TextId nicht stimmt muss eine evtl. schon gefundene Referenzteileposition mit größerem
                        // Abstand zum referenceDateFrom wieder auf null gesetzt werden
                        result = null;
                    }
                }
            }
        }

        return result;
    }

    private EditTransferPartlistPredictionGrid.PartListEntryReferenceKey buildReferenceKey(EtkDataPartListEntry partListEntry,
                                                                                           boolean isConstruction) {
        PartId partId = partListEntry.getPart().getAsId();
        String textId = matNoTextIdMap.get(partId.getMatNr());
        if (textId == null) {
            EtkDataPart part;
            if (isConstruction) {
                // in der Konstruktion ist M_TEXTNR mit dem Konstruktionstext überschrieben
                part = EtkDataObjectFactory.createDataPart(getProject(), partId);
            } else {
                part = partListEntry.getPart();
            }

            // TextId aus M_TEXTNR auslesen (TextId ist nicht zwangsweise identisch zur TextNr!)
            EtkMultiSprache multi = part.getFieldValueAsMultiLanguage(iPartsConst.FIELD_M_TEXTNR);
            if (multi != null) {
                textId = multi.getTextId();
            }
            matNoTextIdMap.put(partId.getMatNr(), textId);
        }

        return new EditTransferPartlistPredictionGrid.PartListEntryReferenceKey(partListEntry, isConstruction, textId);
    }

    public KgTuListItem getKgTuListItem(KgTuId kgTuId, Map<String, KgTuListItem> kgTuMap) {
        if ((kgTuId != null) && (kgTuMap != null)) {
            // KG/TU Knoten bestimmen
            KgTuListItem kgListItem = kgTuMap.get(kgTuId.getKg());
            KgTuListItem tuListItem = null;
            if (kgListItem != null) {
                for (KgTuListItem currentTuListItem : kgListItem.getChildren()) {
                    if (currentTuListItem.getKgTuId().equals(kgTuId)) {
                        tuListItem = currentTuListItem;
                        break;
                    }
                }
                if (tuListItem != null) {
                    KgTuListItem parentKgListItem = new KgTuListItem(kgListItem.getKgTuNode(), KgTuListItem.Source.TEMPLATE, true);
                    return new KgTuListItem(tuListItem.getKgTuNode(), KgTuListItem.Source.TEMPLATE, parentKgListItem, false);
                }
            }
        }
        return null;

//        if (kgTuListItem != null) {
//            kgTuListItem.getKgTuNode().getTuVar()
//        }
//
//
//        KgTuNode kgTuNode = kgTuMap.getTuNode(kgTuId.getKg(), kgTuId.getTu());
//        // das Produkt enthält den KG/TU Knoten nicht -> wir nehmen den ersten gefundenen Knoten aus den Templates
//        for (KgTuTemplate kgTuTemplate : kgTuMap.values()) {
//            kgTuNode = kgTuTemplate.getTuNode(kgTuId.getKg(), kgTuId.getTu());
//            if (kgTuNode != null) {
//                kgTuTemplateUsed = kgTuTemplate;
//                break;
//            }
//        }
    }


    private void fireProgress(int pos, int maxPos) {
        if (messageLog != null) {
            messageLog.fireProgress(pos, maxPos, "", false, true);
        }
    }

    private void fireMessage(String message, String... placeHolderTexts) {
        if (messageLog != null) {
            if (!showOnlyProgress) {
                messageLog.fireMessage(TranslationHandler.translate(message, placeHolderTexts));
            }
        } else if (logHelper != null) {
            logHelper.addLogMsgWithTranslation(message, placeHolderTexts);
        }
    }

    public boolean isWithLinkingChanges() {
        return withLinkingChanges;
    }

    public void setWithLinkingChanges(boolean withLinkingChanges) {
        this.withLinkingChanges = withLinkingChanges;
    }

    public void setLogHelper(ImportExportLogHelper logHelper) {
        this.logHelper = logHelper;
    }

    public interface OnModifyCreatedRetailPartListEntryEvent {

        void onModifyCreatedPartListEntry(EtkProject project, EtkDataPartListEntry createdRetailPartListEntry,
                                          TransferToASElement transferElem, iPartsDataCombTextList combinedTextList,
                                          iPartsDataFootNoteCatalogueRefList fnCatalogueRefList);

        void onModifyCreatedPartListEntryEDS(EtkProject project, EtkDataPartListEntry createdRetailPartListEntry,
                                             TransferToASElement transferElem, iPartsDataCombTextList combinedTextList,
                                             iPartsDataFootNoteCatalogueRefList fnCatalogueRefList);

        void onModifyCreatedAssembly(EtkProject project, EtkDataObjectList dataObjectListToBeSaved,
                                     boolean assemblyIsNew, EtkDataAssembly destAssembly);

        void onModifyCreatedAssemblyEDS(EtkProject project, EtkDataObjectList dataObjectListToBeSaved,
                                        boolean assemblyIsNew, EtkDataAssembly destAssembly);
    }

    public class EditTransferPartListEntriesException extends Exception {

        private EditTransferPartListEntriesException(String message, Throwable cause) {
            super(message, cause);
        }

        private EditTransferPartListEntriesException(String message) {
            super(message);
        }
    }


}
