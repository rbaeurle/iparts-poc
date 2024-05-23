/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsOmittedParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.iPartsSaaPartsListPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsConstructionPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsRetailUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuListItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.EditTransferToASHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.RowContentForTransferToAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.TransferToASElement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonType;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.os.OsUtils;

import java.awt.*;
import java.util.List;
import java.util.*;


public class EditTransferPartlistEntriesWithPredictionForm extends AbstractJavaViewerForm implements iPartsConst {

    public static void startTransferToASPartList(AssemblyListFormIConnector connector, TransferMode transferMode) {
        if (!connector.getProject().getEtkDbs().isRevisionChangeSetActiveForEdit()) {
            MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
            return;
        }

        EtkDataAssembly assembly = connector.getCurrentAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            if (!iPartsEditPlugin.startEditing()) { // Paralleles Bearbeiten verhindern
                return;
            }

            try {
                List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
                if (virtualNodesPath != null) {
                    // Der Baugruppenbaum setzt sich zusammen aus realen Knoten aus Tabelle Katalog und virtuellen
                    // Knoten, die programmatisch erzeugt werden. Diese virtuellen Knoten bilden den vitualNodesPath.
                    // Für DIALOG besteht dieser aus zwei Knoten. Der erste Knoten hat den Typ {@link iPartsNodeType#DIALOG_HMMSM}
                    // und enthält die Baureihe. Der zweite Knoten repräsentiert {@link iPartsNodeType#HMMSM}.
                    // Für die Saa Stückliste in EDS besteht dieser aus drei Knoten. Der erste Knoten hat den Typ {@link iPartsNodeType#EDS_OPS}
                    // oder {@link iPartsNodeType#EDS_MODEL_ELEMENT_USAGE} und enthält das Baumuster. Der zweite Knoten
                    // repräsentiert {@link iPartsNodeType#OPS} oder {@link iPartsNodeType#MODEL_ELEMENT_USAGE} und der
                    // dritte Knoten die SAA über {@link iPartsNodeType#EDS_SAA}.
                    // Für die Saa Stückliste in CTT besteht dieser aus zwei Knoten. Der erste Knoten hat den Typ {@link iPartsNodeType#CTT_MODEL}
                    // und enthält das Baumuster. Der zweite Knoten hält die Saa über {@link iPartsNodeType#EDS_SAA}.
                    // Ein virtueller Knoten repräsentiert nicht notwendigerweise einen Anzeigeknoten im Baum.
                    // So wird der HMMSM Knoten z.B. in drei Anzeigebenenen aufgesplittet.

                    if (iPartsVirtualNode.isHmMSmNode(virtualNodesPath)) {
                        doTransferToASPartlistDialog(virtualNodesPath, connector);
                    } else if (iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)) {
                        doTransferToASPartlistSaaPartsList(Mode.EDS, virtualNodesPath, connector, transferMode);
                    } else if (iPartsVirtualNode.isCTTSaaConstNode(virtualNodesPath)) {
                        doTransferToASPartlistSaaPartsList(Mode.CTT, virtualNodesPath, connector, transferMode);
                    } else if (iPartsVirtualNode.isMBSNode(virtualNodesPath)) {
                        doTransferToASPartlistMBS(virtualNodesPath, connector, transferMode);
                    }
                }
            } finally {
                iPartsEditPlugin.stopEditing();
            }
        }
    }

    public static void doTransferToASPartlistDialog(List<iPartsVirtualNode> virtualNodesPath, AssemblyListFormIConnector connector) {
        List<EtkDataPartListEntry> filteredSourcePartList = filterSourcePartList(connector.getSelectedPartListEntries());
        if (filteredSourcePartList.isEmpty()) {
            MessageDialog.show(TranslationHandler.translate("!!Es sind keine Doku-relevanten Stücklisteneinträge oder Einträge mit nicht spezifizierter Doku-Relevanz markiert"));
            return;
        }

        EditTransferPartlistEntriesWithPredictionForm form =
                new EditTransferPartlistEntriesWithPredictionForm(Mode.DIALOG, connector, connector.getCurrentAssembly(),
                                                                  null, virtualNodesPath, TransferMode.PARTLIST);
        List<EtkDataPartListEntry> alreadyTransferedPartlistEntries = getToASTransferredPartListEntries(connector.getCurrentAssembly(),
                                                                                                        filteredSourcePartList);
        if (form.init(filteredSourcePartList, alreadyTransferedPartlistEntries)) {
            form.showModal();
        }
    }

    public static List<EtkDataPartListEntry> filterSourcePartList(List<EtkDataPartListEntry> partListEntries) {
        List<EtkDataPartListEntry> filteredSourcePartList = new DwList<>();
        // Kopie der Stückliste erzeugen aus doku-relevanten Positionen oder Positionen mit nicht spezifizierter Doku-Relevanz
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            // docuValues kann nur einen Wert haben. SetOfEnum wurde das Feld nur deklariert, damit man im Spaltenfilter
            // mehrere Werte auswählen kann
            List<String> docuValues = partListEntry.getFieldValueAsSetOfEnum(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT);
            iPartsDocuRelevant docuRelevant = iPartsDocuRelevant.getFromDBValue(docuValues.isEmpty() ? "" : docuValues.get(0));
            if (iPartsDocuRelevant.canBeTransferredToAS(docuRelevant)) {
                filteredSourcePartList.add(partListEntry);
            }
        }
        return filteredSourcePartList;
    }

    public static void doTransferToASPartlistSaaPartsList(Mode formMode, List<iPartsVirtualNode> virtualNodesPath, AssemblyListFormIConnector connector,
                                                          TransferMode transferMode) {
        List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
        // bei EDS müssen doppelte Materialnummern in der Selektion entfernt werden
        selectedPartListEntries = filterSelectedPartlistEntriesForSaaPartsList(selectedPartListEntries);

        EditTransferPartlistEntriesWithPredictionForm form =
                new EditTransferPartlistEntriesWithPredictionForm(formMode, connector, connector.getCurrentAssembly(), null,
                                                                  virtualNodesPath, transferMode);
        if (form.init(selectedPartListEntries, null)) {
            form.showModal();
        }
    }

    public static void doTransferToASPartlistMBS(List<iPartsVirtualNode> virtualNodesPath, AssemblyListFormIConnector connector,
                                                 TransferMode transferMode) {
        List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();

        // bei MBS müssen leere Materialnummern entfernt werden (Texte)
        selectedPartListEntries = filterSelectedPartlistEntriesForMBS(selectedPartListEntries);

        EditTransferPartlistEntriesWithPredictionForm form =
                new EditTransferPartlistEntriesWithPredictionForm(Mode.MBS, connector, connector.getCurrentAssembly(), null,
                                                                  virtualNodesPath, transferMode);
        if (form.init(selectedPartListEntries, null)) {
            form.showModal();
        }
    }

    public static List<EtkDataPartListEntry> filterSelectedPartlistEntriesForSaaPartsList(List<EtkDataPartListEntry> selectedPartlistEntries) {
        Map<PartListEntryId, EtkDataPartListEntry> resultMap = new LinkedHashMap<>();
        for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
            resultMap.put(selectedPartlistEntry.getAsId(), selectedPartlistEntry);
        }
        return new DwList<>(resultMap.values());
    }

    public static List<EtkDataPartListEntry> filterSelectedPartlistEntriesForMBS(List<EtkDataPartListEntry> selectedPartlistEntries) {
        DwList<EtkDataPartListEntry> filteredList = new DwList<>();
        for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
            if (!selectedPartlistEntry.getPart().getAsId().getMatNr().isEmpty()) {
                filteredList.add(selectedPartlistEntry);
            }
        }
        return filteredList;
    }

    public static List<EtkDataPartListEntry> getToASTransferredPartListEntries(EtkDataAssembly assembly, List<EtkDataPartListEntry> selectedPartListEntries) {
        List<String> selectedGuids = new DwList<>();
        if (selectedPartListEntries != null) {
            for (EtkDataPartListEntry partListEntry : selectedPartListEntries) {
                selectedGuids.add(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID));
            }
        }
        List<EtkDataPartListEntry> transferredPartListEntries = new DwList<>();
        for (EtkDataPartListEntry partListEntry : assembly.getPartListUnfiltered(assembly.getEbene())) {
            if (partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(RETAIL_ASSIGNED)) {
                if (!selectedGuids.contains(partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID))) {
                    transferredPartListEntries.add(partListEntry);
                }
            }
        }
        return transferredPartListEntries;
    }

    public enum Mode {
        DIALOG,
        EDS,
        CTT,
        MBS
    }

    public enum TransferMode {
        PARTLIST,
        SA
    }

    private static final int PARTNOLIMIT = 8;
    private final Map<String, List<iPartsProduct>> aaDetectedProductMap;
    private final Map<String, List<EtkDataPartListEntry>> guidToRetailUse;
    private final Map<String, Set<EtkDataPartListEntry>> partialGUIDToRetailUse; // Retail Stücklistenpositionen für BR-HM-M-SM-POSE
    private final EtkDataAssembly sourceConstAssembly;
    private final List<String> warnings;
    private final Mode formMode;
    private final TransferMode transferMode;
    private EditTransferPartlistPredictionGrid grid;
    private List<RowContentForTransferToAS> initialTableRows;
    private iPartsSeriesId seriesId;
    private List<iPartsProduct> preFilteredProducts;
    private IdWithType sourceId; // ID des aktuellen virtuellen Knotens (DIALOG: HM/M/SM; EDS: SAA)
    private iPartsModelId modelId;
    private IdWithType edsStructureId; // Id der Struktur (OPSId oder ModelElementUsageId
    private iPartsProduct masterProduct;
    private ImportExportLogHelper logHelper;
    private iPartsRevisionChangeSet techChangeSet;


    /**
     * Erzeugt eine Instanz von EditTransferPartlistEntriesWithPredictionForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditTransferPartlistEntriesWithPredictionForm(Mode formMode, AbstractJavaViewerFormIConnector dataConnector,
                                                         EtkDataAssembly sourceConstAssembly, AbstractJavaViewerForm parentForm,
                                                         List<iPartsVirtualNode> virtualNodesPath, TransferMode transferMode) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.formMode = formMode;
        this.transferMode = transferMode;
        this.preFilteredProducts = null;
        this.sourceId = null;
        this.modelId = null;
        this.guidToRetailUse = new HashMap<>();
        this.partialGUIDToRetailUse = new HashMap<>();
        this.aaDetectedProductMap = new HashMap<>();
        if (formMode == Mode.DIALOG) {
            if (iPartsVirtualNode.isHmMSmNode(virtualNodesPath)) {
                this.seriesId = (iPartsSeriesId)virtualNodesPath.get(0).getId();
                iPartsVirtualNode hmMSmNode = virtualNodesPath.get(1);
                this.sourceId = hmMSmNode.getId();
            }
        } else if (formMode == Mode.EDS) {
            if (iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)) {
                this.modelId = (iPartsModelId)virtualNodesPath.get(0).getId();
                this.edsStructureId = virtualNodesPath.get(1).getId();
                this.sourceId = virtualNodesPath.get(2).getId();
            }
        } else if (formMode == Mode.CTT) {
            if (iPartsVirtualNode.isCTTSaaConstNode(virtualNodesPath)) {
                this.modelId = (iPartsModelId)virtualNodesPath.get(0).getId();
                this.sourceId = virtualNodesPath.get(1).getId();
            }
        } else if (formMode == Mode.MBS) {
            if (iPartsVirtualNode.isMBSNode(virtualNodesPath)) {
                this.modelId = (iPartsModelId)virtualNodesPath.get(0).getId();
                sourceId = virtualNodesPath.get(1).getId(); // MBSStructureId
            }
        }

        this.warnings = new DwList<>();
        this.sourceConstAssembly = sourceConstAssembly;
        postCreateGui();
        getGui().setName("TransferToASPredictionForm");
        // Dialog als Vollbild öffnen
        Dimension screenSize = FrameworkUtils.getScreenSize();
        mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        grid = new EditTransferPartlistPredictionGrid(getConnector(), this, formMode, transferMode, EditTransferPartlistPredictionGrid.TABLE_PSEUDO, new OnEditChangeRecordEvent() {
            @Override
            public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                RowContentForTransferToAS rowContent = grid.getSelectedRowContent();
                // Neu
                if (rowContent != null) {
                    if (id == null) {
                        RowContentForTransferToAS createdRowContent = new RowContentForTransferToAS();
                        createdRowContent.copyValues(rowContent);
                        createdRowContent.resetValuesForCreate();
                        return (insertRowContentAfter(rowContent, createdRowContent) != -1);
                    }
                }
                // Neu mit beliebiger ProduktId
                if (id != null) {
                    Map<String, List<RowContentForTransferToAS>> matNrToRowContent = getMatNrToRowContent(grid.getAllItems());
                    int[] selectedIndices = new int[matNrToRowContent.size()];
                    int counter = 0;
                    // Pro selektierter Teileposition aus der Konstruktion soll ein Eintrag mit dem ausgewählten Produkt eingefügt werden
                    // Am besten pro Teileposition nach dem letzten gefundenen Produkt
                    for (String matNr : matNrToRowContent.keySet()) {
                        List<RowContentForTransferToAS> rowContentForTransferToASList = matNrToRowContent.get(matNr);
                        RowContentForTransferToAS lastRowOfMatNr = rowContentForTransferToASList.get(rowContentForTransferToASList.size() - 1);
                        if (lastRowOfMatNr != null) {
                            RowContentForTransferToAS createdRowContent = new RowContentForTransferToAS();
                            createdRowContent.copyValues(lastRowOfMatNr);
                            createdRowContent.resetValuesForCreate();
                            if (id instanceof iPartsProductId) {
                                iPartsProductId iPartsProductId = (iPartsProductId)id;
                                iPartsProduct product = iPartsProduct.getInstance(getProject(), iPartsProductId);
                                createdRowContent.getTransferElement().setProduct(product);
                                // Die zum gewählten Produkt passende KGTU Struktur setzen
                                Map<String, KgTuListItem> kgTuMap = KgTuHelper.getKGTUStructure(getProject(), iPartsProductId);
                                createdRowContent.setKgTuMap(kgTuMap);
                                int selectedIndex = insertRowContentAfter(lastRowOfMatNr, createdRowContent);
                                selectedIndices[counter] = selectedIndex;
                                counter++;
                            }
                        }
                    }
                    //Alle hinzugefügten Positionen selektieren
                    if (selectedIndices.length != 0) {
                        grid.getTable().clearSelection();
                        grid.getTable().setSelectedRows(selectedIndices, false, true);
                    }
                    return true;
                }
                enableButtons();
                return false;
            }

            @Override
            public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes) {
                RowContentForTransferToAS rowContent = grid.getSelectedRowContent();
                if (rowContent != null) {
                    RowContentForTransferToAS createdRowContent = new RowContentForTransferToAS();
                    createdRowContent.copyValues(rowContent);
                    createdRowContent.resetValuesForCreate();
                    createdRowContent.setKgTuId(rowContent.getKgTuId());
                    if (createdRowContent.isEditable()) {
                        createdRowContent.setTransferMark(true);
                    }

                    return (insertRowContentAfter(rowContent, createdRowContent) != -1);
                }
                enableButtons();
                return false;
            }

            /**
             *
             * @param selectedRowContent
             * @param createdRowContent
             * @return -1 falls Zeile nicht eingefügt wurde, sonst den Index
             */
            private int insertRowContentAfter(RowContentForTransferToAS selectedRowContent,
                                              RowContentForTransferToAS createdRowContent) {
                if ((selectedRowContent != null) && (createdRowContent != null)) {
                    List<RowContentForTransferToAS> itemList = grid.getAllItems();
                    int selectedIndex = itemList.indexOf(selectedRowContent);
                    if (selectedIndex != -1) {
                        itemList.add(selectedIndex + 1, createdRowContent);
                        fillGrid(itemList);
                        itemList = grid.getAllVisibleItems();
                        selectedIndex = itemList.indexOf(createdRowContent);
                        grid.getTable().setSelectedRow(selectedIndex, true, true);
                        enableButtons();
                        return selectedIndex;
                    }
                }
                enableButtons();
                return -1;
            }

            @Override
            public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                return true;
            }

            @Override
            public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList) {
                RowContentForTransferToAS rowContent = grid.getSelectedRowContent();
                int visibleSelectedIndex = grid.getTable().getSelectedRowIndex();
                if (rowContent != null) {
                    List<RowContentForTransferToAS> itemList = grid.getAllItems();
                    int index = itemList.indexOf(rowContent);
                    if (index != -1) {
                        itemList.remove(index);
                        fillGrid(itemList);
                        grid.getTable().setSelectedRow(visibleSelectedIndex - 1, true, true);
                    } else {
                        grid.getTable().setSelectedRow(visibleSelectedIndex, true, true);
                    }
                }
                enableButtons();
                // immer false, weil sonst die Suche aktiviert wird
                return false;
            }
        });

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        grid.getGui().setConstraints(constraints);
        mainWindow.contentTablePanel.addChild(grid.getGui());

        EditToolbarButtonAlias info = EditToolbarButtonAlias.EDIT_TEST;
        GuiToolButton infoButton = new GuiToolButton(ToolButtonType.BUTTON, info.getText(), info.getImages());
        infoButton.setTooltip("!!Info");
        infoButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                showWarnings();
            }
        });

        // Den Info Button nach Delete einhängen
        grid.getToolbarManager().insertButtonAfter(infoButton, info.getAlias(), EditToolbarButtonAlias.EDIT_DELETE.getAlias());
        grid.getTable().addEventListener(new EventListenerFireOnce(Event.TABLE_COLUMN_SORTED_EVENT, grid.getTable()) {
            @Override
            public boolean isFireOnceValid(Event event) {
                return grid.getTable().getRowCount() > 0;
            }

            @Override
            public void fireOnce(Event event) {
                try {
                    if (grid.getTable().getRowCount() > 0) {
                        if (grid.getTable().getSortColumn() >= 0) {
                            // hier wurde sortiert => nur Background setzen
                            setBackground();
                        } else {
                            // hier wurde 'Sortierung aufheben' geklickt => alle Items neu laden
                            if (initialTableRows != null) {
                                fillGrid(initialTableRows);
                            }
                        }
                    }
                } finally {
                    // wieder TABLE_COLUMN_SORTED_EVENT-Events zulassen
                    resetFired();
                }
            }
        });

        // Enter und ESC bei den Buttons vom ButtonPanel deregistrieren
        mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setDefaultButton(false, true);
        mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setCancelButton(false, true);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (grid != null) {
            grid.dispose();
        }
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setMasterProduct(iPartsProduct masterProduct) {
        this.masterProduct = masterProduct;
    }

    public void setLogHelper(ImportExportLogHelper logHelper) {
        this.logHelper = logHelper;
    }

    public void setTechChangeSet(iPartsRevisionChangeSet techChangeSet) {
        this.techChangeSet = techChangeSet;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.updateData(sender, forceUpdateAll);
        enableButtons();
    }

    public boolean init(List<EtkDataPartListEntry> selectedPartlistEntries,
                        List<EtkDataPartListEntry> alreadyTransferredPartlistEntries) {
        warnings.clear();
        if (formMode == Mode.DIALOG) {
            if ((seriesId == null) || (sourceId == null)) {
                warnings.add(TranslationHandler.translate("!!Ungenügende Initialisierung für DIALOG!"));
            }
        } else if (formMode == Mode.EDS) {
            if ((modelId == null) || (edsStructureId == null) || (sourceId == null)) {
                warnings.add(TranslationHandler.translate("!!Ungenügende Initialisierung für EDS!"));
            }
        } else if (formMode == Mode.CTT) {
            if ((modelId == null) || (sourceId == null)) {
                warnings.add(TranslationHandler.translate("!!Ungenügende Initialisierung für CTT!"));
            }
        } else if (formMode == Mode.MBS) {
            if ((modelId == null) || (sourceId == null)) {
                warnings.add(TranslationHandler.translate("!!Ungenügende Initialisierung für SAP.MBS!"));
            }
        } else {
            warnings.add(TranslationHandler.translate("!!Ungültiger Aufruf!"));
        }
        if (!showWarnings()) {
            return false;
        }

        if (formMode == Mode.DIALOG) {
            buildSubTitleDIALOG(seriesId, selectedPartlistEntries);
        } else if (isSaaPartsListMode()) {
            buildSubTitleEDS(selectedPartlistEntries);
        } else if (formMode == Mode.MBS) {
            buildSubTitleMBS(selectedPartlistEntries);
        }
        mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, false);
        // Checkbox TU öffnen soll selektiert sein
        mainWindow.checkboxShowModule.setSelected(true);

        Map<String, Map<String, KgTuListItem>> kgTuForProductMap = new HashMap<>();

        if (formMode == Mode.DIALOG) {
            grid.initDIALOG();
        } else if (isSaaPartsListMode()) {
            grid.initSaaPartsListAndMBS(false, transferMode);
        } else if (formMode == Mode.MBS) {
            grid.initSaaPartsListAndMBS(true, transferMode);
        }

        initialTableRows = new DwList<>();
        if (formMode == Mode.DIALOG) {
            findCompleteRetailUse(selectedPartlistEntries, alreadyTransferredPartlistEntries);

            Map<String, iPartsDataModuleEinPASList> einPASListMap = new HashMap<>();

            List<RowContentForTransferToAS> alreadyTransferredRows = calculateTransferListDIALOG(
                    alreadyTransferredPartlistEntries, einPASListMap, null, null);
            initialTableRows = calculateTransferListDIALOG(selectedPartlistEntries, einPASListMap, alreadyTransferredRows, warnings);
        } else if (isSaaPartsListMode()) {
            initialTableRows = calculateTransferListSaaPartsList(selectedPartlistEntries, warnings);
        } else if (formMode == Mode.MBS) {
            initialTableRows = calculateTransferListMBS(selectedPartlistEntries, warnings);
        }

        if ((initialTableRows == null) || initialTableRows.isEmpty()) {
            warnings.add(TranslationHandler.translate("!!Es konnten keine Einträge zur Übernahme bestimmt werden."));
        }

        updateKgTuPredictionForOmittedParts(initialTableRows);

        if (!showWarnings()) {
            return false;
        }

        startPseudoTransactionForActiveChangeSet(true);
        try {
            for (RowContentForTransferToAS tableRow : initialTableRows) {
                iPartsProductId productId = tableRow.getProductId();
                if (productId != null) {
                    Map<String, KgTuListItem> kgtuCacheEntry = kgTuForProductMap.get(productId.getProductNumber());
                    if (kgtuCacheEntry == null) {
                        kgtuCacheEntry = KgTuHelper.getKGTUStructure(getProject(), productId);
                        kgTuForProductMap.put(productId.getProductNumber(), kgtuCacheEntry);
                    }
                    tableRow.setKgTuMap(kgtuCacheEntry);

                    // den zuvor in calculateTransferList ermittelten KG/TU Vorschlag der KI auf Gültigkeit prüfen
                    if (tableRow.getAssignmentValue() == iPartsTransferAssignmentValue.FROM_KI) {
                        if (!tableRow.hasTUItems()) {
                            tableRow.setKgTuId(new KgTuId());
                            tableRow.setAssignmentValue(iPartsTransferAssignmentValue.NOT_ASSIGNED);
                        }
                    }

                    // bei EDS den ermittelten Hotspot Vorschlag wieder entfernen wenn der KGTU nicht gültig ist
                    if (isSaaPartsListMode()) {
                        if (tableRow.getAssignmentValue() == iPartsTransferAssignmentValue.ASSIGNED) {
                            if (!tableRow.isKGTUValidForTransfer()) {
                                tableRow.setHotspot("");
                            }
                        }
                        if (tableRow.getAssignmentValue() != iPartsTransferAssignmentValue.NOT_ASSIGNED) {
                            if (!tableRow.isKGTUValidForTransfer() && tableRow.isAlternateKgTuListValid()) {
                                tableRow.correctPredictedKgTuWithAlternatives();
                            }
                        }
                    }
                }
            }
        } finally {
            stopPseudoTransactionForActiveChangeSet();
        }

        if (formMode == Mode.DIALOG) {
            // DAIMLER-8053: Stücklisteneinträge nach  AA, POS, PV, WW, ETZ, KEM-Datum ab (aufsteigend), Produkt sortieren
            initialTableRows.sort(RowContentForTransferToAS.createComparatorForBCTEandProduct());
        } else if (formMode == Mode.MBS) {
            initialTableRows.sort(RowContentForTransferToAS.createComparatorForSeqNrAndProduct());
        }
        // bei EDS keine spezielle Sortierung anwenden

        fillGrid(initialTableRows);
        boolean result = true;
        if (initialTableRows.isEmpty()) {
            result = showWarnings();
        }
        if (warnings.isEmpty()) {
            grid.getToolbarManager().hideButton(EditToolbarButtonAlias.EDIT_TEST.getAlias());
        } else {
            grid.getToolbarManager().showButton(EditToolbarButtonAlias.EDIT_TEST.getAlias());
        }
        return result;
    }

    private boolean isSaaPartsListMode() {
        return (formMode == Mode.EDS) || (formMode == Mode.CTT);
    }

    private boolean showWarnings() {
        if (!warnings.isEmpty()) {
            MessageDialog.showWarning(StrUtils.stringListToString(warnings, "\n"));
            return false;
        }
        return true;
    }

    private void fillGrid(List<RowContentForTransferToAS> itemList) {
        grid.switchOffEventListeners();

        // Spalten Filter merken
        Map<Integer, Object> columnFilterValuesMap = new HashMap<>(); // Map von Spalten-Indizes auf Filtertwert-Objekte
        AbstractGuiTableColumnFilterFactory storedFilterFactory = grid.storeFilterFactory(columnFilterValuesMap);
        int sortColumn = grid.getTable().getSortColumn();
        boolean isSortAscending = grid.getTable().isSortAscending();

        // danach alle Filter deaktivieren und sämtliche Inhalte löschen
        grid.getTable().clearAllFilterValues();
        grid.clearGrid();
        // Entries enthält alle Zeilen, auch die aktuell durch den Filter ausgeblendeten. Es wird nur einmalig neu bestimmt, wenn es null ist
        grid.clearEntries();

        // Tabelle mit Inhalt füllen
        for (RowContentForTransferToAS tableRow : itemList) {
            grid.addToGrid(tableRow);
        }
        setBackground();
        // gemerkte Spalten Filter wieder aktivieren
        if (!columnFilterValuesMap.isEmpty()) {
            grid.restoreFilterFactory(storedFilterFactory, columnFilterValuesMap);
        }
        // Sortierung wiederherstellen falls vorher sortiert war
        if (grid.getTable().isSortEnabled() && (sortColumn >= 0)) {
            grid.getTable().sortRowsAccordingToColumn(sortColumn, isSortAscending);
        }
        grid.switchOnEventListeners();
        grid.showNoResultsLabel(grid.getTable().getRowCount() == 0, false);
    }

    private void setBackground() {
        Color back1 = Colors.clDesignTableContentBackground.getColor();
        Color back2 = Colors.clDesignTableContentBackgroundAlternating.getColor();
        Color backgroundColor = back2;
        String guid = "";
        for (GuiTableRow row : grid.getTable().getAllRows()) {
            if (row instanceof SimpleSelectSearchResultGrid.GuiTableRowWithAttributes) {
                SimpleSelectSearchResultGrid.GuiTableRowWithAttributes rowWithAttributes = (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)row;
                if (rowWithAttributes instanceof EditTransferPartlistPredictionGrid.RowWithAttributesForTransfer) {
                    RowContentForTransferToAS rowContent = ((EditTransferPartlistPredictionGrid.RowWithAttributesForTransfer)rowWithAttributes).rowContent;
                    if (rowContent != null) {
                        String rowContentGuid;
                        if (isSaaPartsListMode() || (formMode == Mode.MBS)) {
                            // bei MBS und EDS die kLfdNr verwenden
                            rowContentGuid = rowContent.getTransferElement().getSelectedPartlistEntry().getAsId().getKLfdnr();
                        } else {
                            rowContentGuid = rowContent.getSourceGUIDForAttribute();
                        }

                        if (!guid.equals(rowContentGuid)) {
                            if (backgroundColor.equals(back1)) {
                                backgroundColor = back2;
                            } else {
                                backgroundColor = back1;
                            }
                            guid = rowContentGuid;
                        }
                        row.setBackgroundColor(backgroundColor);
                    }
                }
            }
        }
    }

    private List<RowContentForTransferToAS> findOfferCandidatesForProduct(List<RowContentForTransferToAS> list,
                                                                          iPartsProductId productId) {
        List<RowContentForTransferToAS> result = new DwList<>();
        for (RowContentForTransferToAS content : list) {
            if ((content.getAssignmentValue() == iPartsTransferAssignmentValue.ASSIGNED) &&
                content.getProductId().equals(productId)) {
                result.add(content);
            }
        }
        return result;
    }

    private Collection<RowContentForTransferToAS> findPosVariante(RowContentForTransferToAS currentTableRow,
                                                                  List<RowContentForTransferToAS> selectedRows,
                                                                  List<RowContentForTransferToAS> alreadyTransferredRows) {
        List<RowContentForTransferToAS> selectedSetRows =
                findOfferCandidatesForProduct(selectedRows, currentTableRow.getProductId());

        List<RowContentForTransferToAS> alreadyTransferredSetRows =
                findOfferCandidatesForProduct(alreadyTransferredRows, currentTableRow.getProductId());

        // Es soll pro gefundenem Produkt, KG, TU und Hotspot nur ein Vorschlag angezeigt werden, selbst wenn dafür
        // mehrere Stücklisteneinträge in Frage kommen
        Set<RowContentForTransferToAS> result = new TreeSet<>(RowContentForTransferToAS.createComparatorForFindPosVariant());

        // Usecase: Neuer Änderungsstand zu einer PV die schon dokumentiert wurde (gleiche Pos, gleiche PV)

        // erstmal in den selektierten nach einer PositionsVariante suchen
        for (RowContentForTransferToAS tableRow : selectedSetRows) {
            if (!currentTableRow.getSourceGUIDForAttribute().equals(tableRow.getSourceGUIDForAttribute()) &&
                currentTableRow.getBcteKeyForPosVar().equals(tableRow.getBcteKeyForPosVar())) {
                result.add(tableRow);
            }
        }

        // nun in den bereits nach AS-verschobenen nach einer PositionsVariante suchen
        for (RowContentForTransferToAS tableRow : alreadyTransferredSetRows) {
            if (currentTableRow.getBcteKeyForPosVar().equals(tableRow.getBcteKeyForPosVar())) {
                result.add(tableRow);
            }
        }

        // Usecase: Neue technische Variante (neue PV, gleiche Pos)

        // in den selektierten nach einem gleichen Modul und AA suchen
        for (RowContentForTransferToAS tableRow : selectedSetRows) {
            if (!currentTableRow.getSourceGUIDForAttribute().equals(tableRow.getSourceGUIDForAttribute()) &&
                currentTableRow.getBcteKeyForNewVar().equals(tableRow.getBcteKeyForNewVar())) {
                result.add(tableRow);
            }
        }

        // in den bereits nach AS-verschobenen nach einem gleichen Modul und AA suchen
        for (RowContentForTransferToAS tableRow : alreadyTransferredSetRows) {
            if (currentTableRow.getBcteKeyForNewVar().equals(tableRow.getBcteKeyForNewVar())) {
                result.add(tableRow);
            }
        }
        return result;
    }

    public List<RowContentForTransferToAS> calculateTransferListDIALOG(List<EtkDataPartListEntry> selectedPartlistEntries,
                                                                       Map<String, iPartsDataModuleEinPASList> einPASListMap,
                                                                       List<RowContentForTransferToAS> alreadyTransferredRows,
                                                                       List<String> warnings) {
        if ((selectedPartlistEntries == null) || (formMode != Mode.DIALOG)) {
            return null;
        }
        List<RowContentForTransferToAS> result = new DwList<>();

        for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(selectedPartlistEntry);
            if (bctePrimaryKey == null) {
                continue;
            }

            List<iPartsProduct> matchingProducts = findMatchingProductsForAA(bctePrimaryKey.aa);
            if (matchingProducts.isEmpty()) {
                if (warnings != null) {
                    warnings.add(TranslationHandler.translate("!!Für Baureihe %1 und Ausführungsart %2 wurde kein Produkt gefunden.",
                                                              seriesId.getSeriesNumber(), bctePrimaryKey.aa));
                }
            }

            for (iPartsProduct product : matchingProducts) {
                RowContentForTransferToAS tableRow = new RowContentForTransferToAS(bctePrimaryKey, selectedPartlistEntry, product);

                boolean copyAdded = false;
                // Usecase: gleicher PV-Stand soll nochmals übernommen werden (gleiche Pos, gleiche PV, gleiche KEM-Datum ab
                // Je Verwendung eine eigene Zeile angezeigt. Die Übernahme in den gleichen technischen Umfang und Hotspot
                // ist nicht möglich. Der Anwender kann auf Basis des Eintrags einen neuen erzeugen. Die Hotspot-Nummer ist in diesem Fall leer.
                List<EtkDataPartListEntry> retailPartlistEntries = guidToRetailUse.get(tableRow.getSourceGUIDForAttribute());
                for (EtkDataPartListEntry retailPartlistEntry : retailPartlistEntries) {
                    String assemblyNr = retailPartlistEntry.getAsId().getKVari();
                    iPartsDataModuleEinPASList einPASList = einPASListMap.get(assemblyNr);
                    if (einPASList == null) {
                        einPASList = iPartsDataModuleEinPASList.loadForModule(getProject(), new AssemblyId(assemblyNr, ""));
                        einPASListMap.put(assemblyNr, einPASList);
                    }
                    for (iPartsDataModuleEinPAS einPasModuleItem : einPASList) {
                        String productNumber = einPasModuleItem.getAsId().getProductNumber();
                        if (productNumber.equals(product.getAsId().getProductNumber())) {
                            // Pro bereits übernommener Assembly soll eine eigene Zeile in der Tabelle erzeugt werden
                            RowContentForTransferToAS tableRowCopy = new RowContentForTransferToAS();
                            tableRowCopy.copyValues(tableRow);

                            tableRowCopy.setAssignedASpartlistEntry(retailPartlistEntry.getAsId());

                            // KG/TU Verortung eintragen
                            tableRowCopy.setKgTuId(einPasModuleItem.getFieldValue(FIELD_DME_SOURCE_KG),
                                                   einPasModuleItem.getFieldValue(FIELD_DME_SOURCE_TU));

                            // hotspot eintragen
                            tableRowCopy.setHotspot(retailPartlistEntry.getFieldValue(FIELD_K_POS));
                            tableRowCopy.setAssignmentValue(iPartsTransferAssignmentValue.ASSIGNED);

                            result.add(tableRowCopy);
                            copyAdded = true;
                        }
                    }
                }
                if (!copyAdded) {
                    result.add(tableRow);
                }
            }
        }
        if (alreadyTransferredRows != null) {
            List<RowContentForTransferToAS> finalTransferRows = new DwList<>();
            for (RowContentForTransferToAS tableRow : result) {
                if (tableRow.getAssignmentValue() == iPartsTransferAssignmentValue.ASSIGNED) {
                    // Wenn die Zeile schon fertig ist, dann wird hier nichts mehr verändert
                    finalTransferRows.add(tableRow);
                } else {
                    // Wenn die Zeile noch keine Werte für KG/TU und Hotspot hat, wird versucht ein Vorschlag zu ermitteln
                    Collection<RowContentForTransferToAS> offerRows = findPosVariante(tableRow, result, alreadyTransferredRows);
                    // Wenn es bis hierher keinen Vorschlag gab, dann wird nach Positionen mit gleichem BR-HM-M-SM-POSE
                    // Schlüssel gesucht. Unabhängig von AA oder sogar Produkt.
                    if (offerRows.isEmpty()) {
                        offerRows = findPosVariantWithoutAA(tableRow, einPASListMap);
                    }

                    if (offerRows.isEmpty()) {
                        if (tableRow.getConstructionPrimaryKey().isDialog() && (sourceConstAssembly instanceof iPartsDataAssembly)) {
                            // Falls bis jetzt kein Vorschlag gefunden wurde, eine Zeile pro Produkt einfügen
                            KgTuId predictedKGTU = ((iPartsDataAssembly)sourceConstAssembly).getPredictedKGTUforBCTEKey(tableRow.getConstructionPrimaryKey().getAsDialogBCTEPrimaryKey());
                            if (predictedKGTU != null) {
                                // Wenn ein Vorschlag für einen KG/TU in der KI-Tabelle gefunden wurde, dann verwende diesen...
                                tableRow.setKgTuId(predictedKGTU);
                                tableRow.setAssignmentValue(iPartsTransferAssignmentValue.FROM_KI);
                            }
                            // ... ansonsten gibt es keinen Vorschlag.
                            finalTransferRows.add(tableRow);
                        }
                    } else {
                        // Für jeden ermittelten Vorschlag eine eigene Zeile einfügen
                        for (RowContentForTransferToAS offerRow : offerRows) {
                            if (offerRow != null) {
                                RowContentForTransferToAS rowCopy = new RowContentForTransferToAS();
                                rowCopy.copyValues(tableRow);
                                rowCopy.setKgTuId(offerRow.getKgTuId());

                                // hotspot eintragen
                                rowCopy.setHotspot(offerRow.getHotspotForAttribute());
                                if (rowCopy.getBcteKeyForPosVar().equals(offerRow.getBcteKeyForPosVar())) {
                                    rowCopy.setAssignmentValue(iPartsTransferAssignmentValue.ASSIGNED_SAME_PV);
                                } else {
                                    rowCopy.setAssignmentValue(iPartsTransferAssignmentValue.ASSIGNED_OTHER_PV);
                                }
                                finalTransferRows.add(rowCopy);
                            }
                        }
                    }
                }
            }
            result = finalTransferRows;
        }
        return result;
    }

    /**
     * Sucht nach Positionsvorschlägen auf Basis dem Teilschlüssel BR-HM-M-SM-POSE. Es wird sogar produktunabhängig gesucht
     * und bei einem Treffer die KG/TU Struktur und der Hotspot übernommen.
     *
     * @param tableRow
     * @param einPASListMap
     * @return
     */
    private Collection<RowContentForTransferToAS> findPosVariantWithoutAA(RowContentForTransferToAS tableRow, Map<String, iPartsDataModuleEinPASList> einPASListMap) {
        // Für die aktuelle Position den Teilschlüssel bestimmen
        iPartsDialogBCTEPrimaryKey positionKey = tableRow.getBcteKeyForPosVar();
        positionKey = positionKey.getPositionBCTEPrimaryKey();
        // Zum Teilschlüssel alle AS Positionen bestimmen
        Set<EtkDataPartListEntry> entries = partialGUIDToRetailUse.get(positionKey.createGUID());
        Set<RowContentForTransferToAS> result = new LinkedHashSet<>();
        if (entries != null) {
            // Set um gleiche KGTU Struktur + HotSpot zu halten
            Set<String> usedKGTUAndHotSpotStrings = new HashSet<>();
            // Jede AS Position durchlaufen
            entries.forEach(entry -> {
                // BCTE Schlüssel der AS Positon bestimmen
                iPartsDialogBCTEPrimaryKey primaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(entry);
                if (primaryKey != null) {
                    // AS Stückliste der AS Position bestimmen um an die KGTU Struktur zu kommen
                    EtkDataAssembly assembly = entry.getOwnerAssembly();
                    if (assembly instanceof iPartsDataAssembly) {
                        String assemblyNr = assembly.getAsId().getKVari();
                        // Die Stückliste muss existieren, hängt aber an einem Produkt, dass nicht das Zielprodukt sein kann
                        // -> Sonst hätten die Vorschläge davor getroffen
                        iPartsDataModuleEinPASList einPASList = einPASListMap.get(assemblyNr);
                        if (einPASList == null) {
                            einPASList = iPartsDataModuleEinPASList.loadForModule(getProject(), new AssemblyId(assemblyNr, ""));
                            einPASListMap.put(assemblyNr, einPASList);
                        }
                        if (!einPASList.isEmpty()) {
                            // Vorschlag erzeugen auf Basis der aktuellen Position + KGTU und Hotspot der Position im AS
                            // ohne Produkt bestimmen
                            RowContentForTransferToAS copyTableRow = new RowContentForTransferToAS(primaryKey, entry, tableRow.getTransferElement().getProduct());
                            iPartsDataModuleEinPAS moduleEinPASData = einPASList.get(0);
                            // KG/TU Verortung eintragen
                            copyTableRow.setKgTuId(moduleEinPASData.getFieldValue(FIELD_DME_SOURCE_KG),
                                                   moduleEinPASData.getFieldValue(FIELD_DME_SOURCE_TU));

                            // hotspot eintragen
                            copyTableRow.setHotspot(entry.getFieldValue(FIELD_K_POS));
                            // Vorschlag nur hinzufügen, wenn es die Kombination KGTU+Hotspot noch nicht gibt
                            if (usedKGTUAndHotSpotStrings.add(copyTableRow.getKgTuId().toString() + "||" + copyTableRow.getHotspotForAttribute())) {
                                result.add(copyTableRow);
                            }
                        }
                    }
                }
            });
        }
        return result;
    }

    /**
     * KG/TU Vorschläge für Wegfall Sachnummern überarbeiten (DAIMLER-15259).
     * Bei Wegfall Sachnummern soll in den anderen Einträgen, die gerade mit übernommen nach einem passenden Vorschlag
     * gesucht werden, und nur falls sich dort nichts findet, ggf. ein KI-generierter Vorschlag verwendet werden
     *
     * @param transferList
     */
    public void updateKgTuPredictionForOmittedParts(List<RowContentForTransferToAS> transferList) {
        iPartsOmittedParts omittedParts = iPartsOmittedParts.getInstance(getProject());
        for (RowContentForTransferToAS tableRow : transferList) {
            if (tableRow.getAssignmentValue() != iPartsTransferAssignmentValue.ASSIGNED) {
                // prüfen, ob es eine Wegfall SNR ist
                String matNr = tableRow.getSelectedPartlistEntry().getPart().getAsId().getMatNr();
                if (omittedParts.isOmittedPart(matNr)) {
                    // nach einer anderen PosV suchen (Gleiche BR - HM - M - SM - posE und AA)
                    KgTuId searchKgTu = findOtherPosVForKgTuPrediction(transferList, omittedParts, tableRow);
                    if (searchKgTu != null) {
                        tableRow.setKgTuId(searchKgTu);
                        tableRow.setAssignmentValue(iPartsTransferAssignmentValue.ASSIGNED_OTHER_PV);
                    }
                }
            }
        }
    }

    private static KgTuId findOtherPosVForKgTuPrediction(List<RowContentForTransferToAS> transferList, iPartsOmittedParts omittedParts,
                                                         RowContentForTransferToAS searchRow) {
        iPartsDialogBCTEPrimaryKey searchBCTE = searchRow.getBcteKeyForOmittedPartsPos();
        iPartsProductId searchProductId = searchRow.getProductId();
        for (RowContentForTransferToAS currentRow : transferList) {
            if (currentRow.getProductId().equals(searchProductId) &&
                currentRow.getBcteKeyForOmittedPartsPos().equals(searchBCTE)) {
                String currentRowMat = currentRow.getSelectedPartlistEntry().getPart().getAsId().getMatNr();
                if (!omittedParts.isOmittedPart(currentRowMat) && (currentRow.getKgTuId().isValidId())) {
                    return currentRow.getKgTuId();
                }
            }
        }
        return null;
    }

//    private List<RowContentForTransferToAS> calculateTransferListEDS_OLD(List<EtkDataPartListEntry> selectedPartlistEntries,
//                                                                         List<String> warnings) {
//        if ((selectedPartlistEntries == null) || (formMode != Mode.EDS)) {
//            return null;
//        }
//        String saaNumber = null;
//        if (sourceId instanceof EdsSaaId) {
//            EdsSaaId saaId = (EdsSaaId)(sourceId);
//            saaNumber = saaId.getSaaNumber();
//        }
//        if (saaNumber == null) {
//            return null;
//        }
//
//        List<RowContentForTransferToAS> result = new DwList<>();
//        Set<iPartsProductId> matchingProductIds = findMatchingProductsIdsForSAA(new EdsSaaId(saaNumber));
//        Set<String> matchingProductNumbers = new HashSet<>();
//        for (iPartsProductId productId : matchingProductIds) {
//            matchingProductNumbers.add(productId.getProductNumber());
//        }
//
//        Map<String, Map<KgTuId, PartListEntryId>> suggestionCheck1Cache = new HashMap<>();
//        Map<String, Map<KgTuId, PartListEntryId>> suggestionCheck2Cache = null;
//        Map<KgTuId, PartListEntryId> suggestionCheck3Cache = null;
//
//        ASUsageHelper usageHelper = new ASUsageHelper(getProject());
//        for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
//            String partNo = selectedPartlistEntry.getPart().getAsId().getMatNr();
//            iPartsEDSPrimaryKey edsPrimaryKey = iPartsEDSPrimaryKey.createFromEDSPartListEntry(selectedPartlistEntry);
//
//            // Prüfung 1: wenn hier ein Vorschlag ermittelt werden kann, dann gilt er für alle Produkte
//            Map<KgTuId, PartListEntryId> kgtuSuggestion = suggestionCheck1Cache.get(partNo);
//            if (kgtuSuggestion == null) {
//                Map<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> kgTuSuggestionCheck1 =
//                        usageHelper.getEDSUsageBySaaMatNoInASPartList(saaNumber, partNo);
//                kgtuSuggestion = extractKgTuId(matchingProductNumbers, kgTuSuggestionCheck1);
//                if (kgtuSuggestion == null) {
//                    // falls es keine Treffer gab, auf jeden Fall für die nächste gleiche PartNo vorbereiten
//                    kgtuSuggestion = new HashMap<>();
//                }
//                suggestionCheck1Cache.put(partNo, kgtuSuggestion);
//            }
//
//            if (!kgtuSuggestion.isEmpty()) {
//                // Prüfung 1 hat gezogen
//                // Ergebnis für alle Produkte übernehmen
//                TransferContainer transferContainer = new TransferContainer(kgtuSuggestion,
//                                                                            iPartsTransferAssignmentValue.ASSIGNED);
//                for (iPartsProductId productId : matchingProductIds) {
//                    result.add(createEDSRowContent(selectedPartlistEntry, edsPrimaryKey, productId,
//                                                   transferContainer));
//                }
//            } else {
//                // Prüfung 1 hat kein Ergebnis geliefert also mit Prüfung 2 weitermachen
//                // ErgebnisListe pro Produkt vorbereiten
//                Map<iPartsProductId, TransferContainer> resultMapForRowContent = new HashMap<>();
//                for (iPartsProductId productId : matchingProductIds) {
//                    resultMapForRowContent.put(productId, new TransferContainer());
//                }
//
//                // Prüfung 2 Produktabhängig durchführen
//                if (suggestionCheck2Cache == null) {
//                    // Cache für 2. Prüfung besetzen
//                    suggestionCheck2Cache = new HashMap<>();
//                    Map<iPartsProductId, Map<KgTuId, PartListEntryId>> suggestionCheck2 = edsTransferListCheck2(
//                            usageHelper, saaNumber, new DwList<>(matchingProductIds), matchingProductNumbers);
//                    for (Map.Entry<iPartsProductId, Map<KgTuId, PartListEntryId>> entry : suggestionCheck2.entrySet()) {
//                        iPartsProductId productId = entry.getKey();
//                        Map<KgTuId, PartListEntryId> kgTuIdList = entry.getValue();
//                        suggestionCheck2Cache.put(productId.getProductNumber(), kgTuIdList);
//                    }
//                }
//                List<iPartsProductId> inputProductIdsForCheck3 = new DwList<>();
//                // zuerst im Cache für Prüfung 2 suchen
//                for (iPartsProductId productId : matchingProductIds) {
//                    Map<KgTuId, PartListEntryId> kgTuIdPartEntryMap = suggestionCheck2Cache.get(productId.getProductNumber());
//                    if (kgTuIdPartEntryMap != null) {
//                        // Prüfung 2: Treffer für Produkt => Ergebnis merken
//                        resultMapForRowContent.put(productId, new TransferContainer(kgTuIdPartEntryMap, iPartsTransferAssignmentValue.ASSIGNED_OTHER_PART));
//                    } else {
//                        // Kein Treffer => Vorbereitung für Prüfung 3
//                        inputProductIdsForCheck3.add(productId);
//                    }
//                }
//
//                // Prüfung 3 für alle Produkte die bisher noch keinen Vorschlag bekommen haben
//                if (!inputProductIdsForCheck3.isEmpty()) {
//                    // prüfen ob der Cache schon gefüllt ist, wenn nicht muss er jetzt bestimmt werden
//                    if (suggestionCheck3Cache == null) {
//                        suggestionCheck3Cache = edsTransferlistCheck3(usageHelper, saaNumber,
//                                                                      inputProductIdsForCheck3, matchingProductNumbers);
//                    }
//                    if (suggestionCheck3Cache != null) {
//                        // restliche Produkte mit Ergebnis von Prüfung 3 befüllen
//                        for (iPartsProductId productId : inputProductIdsForCheck3) {
//                            resultMapForRowContent.put(productId, new TransferContainer(suggestionCheck3Cache, iPartsTransferAssignmentValue.ASSIGNED_OTHER_SAA));
//                        }
//                    }
//                }
//
//                // Ergebisse aus Prüfung 2 und 3 in der richtigen Produktreihenfolge ins Result
//                for (iPartsProductId productId : matchingProductIds) {
//                    TransferContainer transferContainer = resultMapForRowContent.get(productId);
//                    if (transferContainer == null) {
//                        // kein Treffer für Produkt in Prüfung 2 und 3 => einfach leeren TransferContainer
//                        transferContainer = new TransferContainer();
//                    }
//                    result.add(createEDSRowContent(selectedPartlistEntry, edsPrimaryKey,
//                                                   productId, transferContainer));
//                }
//            }
//        }
//        return result;
//    }

    private List<RowContentForTransferToAS> calculateTransferListSaaPartsList(List<EtkDataPartListEntry> selectedPartlistEntries,
                                                                              List<String> warnings) {
        if ((selectedPartlistEntries == null) || !isSaaPartsListMode()) {
            return null;
        }
        String saaNumber = null;
        if (sourceId instanceof EdsSaaId) {
            EdsSaaId saaId = (EdsSaaId)(sourceId);
            saaNumber = saaId.getSaaNumber();
        }
        if (saaNumber == null) {
            return null;
        }

        if (transferMode == TransferMode.SA) {
            return calculateTransferListForSaModule(saaNumber, selectedPartlistEntries, warnings);
        } else {
            Set<iPartsProductId> matchingProductIds = findMatchingProductsIdsForSAA(new EdsSaaId(saaNumber));
            return createTransferSuggestionsForEDS_MBS(selectedPartlistEntries, saaNumber, matchingProductIds, true);
        }
    }

    private List<RowContentForTransferToAS> calculateTransferListMBS(List<EtkDataPartListEntry> selectedPartlistEntries,
                                                                     List<String> warnings) {
        if ((selectedPartlistEntries == null) || (formMode != Mode.MBS)) {
            return null;
        }

        if (sourceId instanceof MBSStructureId) {
            MBSStructureId mbsStructureId = (MBSStructureId)(sourceId);

            if (transferMode == TransferMode.SA) {
                String saaNumber = mbsStructureId.getListNumber();
                return calculateTransferListForSaModule(saaNumber, selectedPartlistEntries, warnings);
            } else {
                if (mbsStructureId.isConGroupNode()) {
                    if (mbsStructureId.isBasePartlistId()) {
                        // Grundstückliste
                        return calculateTransferListMBSBasePartlist(selectedPartlistEntries, warnings);
                    } else {
                        // normale Übernahme
                        return calculateTransferListMBSDefault(mbsStructureId, selectedPartlistEntries, warnings);
                    }
                }
            }
        }
        return null;
    }

    private List<RowContentForTransferToAS> calculateTransferListMBSBasePartlist(List<EtkDataPartListEntry> selectedPartlistEntries,
                                                                                 List<String> warnings) {
        // Es kann kein Vorschlag ermittelt werden
        String model = iPartsVirtualNode.getModelNumberFromAssemblyId(sourceConstAssembly.getAsId());
        if (model == null) {
            warnings.add(TranslationHandler.translate("!!Es konnte kein Baumuster im Pfad gefunden werden."));
            return new DwList<>();
        }

        String modelForLog = model;
        Set<iPartsProduct> allProducts = new HashSet<>(iPartsProductModels.getInstance(getProject()).getProductsByModel(getProject(), model));
        if (iPartsModel.isAggregateModel(model)) {
            String alternateModel = iPartsConst.MODEL_NUMBER_PREFIX_CAR + model.substring(1);
            allProducts.addAll(iPartsProductModels.getInstance(getProject()).getProductsByModel(getProject(), alternateModel));
            modelForLog += ", " + alternateModel;
        }

        if (allProducts.isEmpty()) {
            warnings.add(TranslationHandler.translate("!!Es konnten keine Produkte zu Baumuster \"%1\" gefunden werden.", modelForLog));
            return new DwList<>();
        }

        List<RowContentForTransferToAS> result = new DwList<>();
        for (iPartsProduct product : allProducts) {
            for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
                RowContentForTransferToAS rowContentForTransferToAS = new RowContentForTransferToAS(null, selectedPartlistEntry, product);
                result.add(rowContentForTransferToAS);
            }
        }
        return result;
    }

    private List<RowContentForTransferToAS> calculateTransferListMBSDefault(MBSStructureId mbsStructureId, List<EtkDataPartListEntry> selectedPartlistEntries,
                                                                            List<String> warnings) {
        // Für den Vorschlag sollen SAA, Baumuster und Teilenummer verwendet werden
        String saaNumber = mbsStructureId.getListNumber();
        if (StrUtils.isEmpty(saaNumber)) {
            warnings.add(TranslationHandler.translate("!!Es konnte keine SAA aus \"%1\" ermittelt werden.", mbsStructureId.toString()));
            return new DwList<>();
        }

        String model = iPartsVirtualNode.getModelNumberFromAssemblyId(sourceConstAssembly.getAsId());
        if (model == null) {
            warnings.add(TranslationHandler.translate("!!Es konnte kein Baumuster im Pfad gefunden werden."));
            return new DwList<>();
        }

        String modelForLog = model;
        Set<iPartsProduct> allProducts = new HashSet<>(iPartsProductModels.getInstance(getProject()).getProductsByModel(getProject(), model));
        if (iPartsModel.isAggregateModel(model)) {
            String alternateModel = iPartsConst.MODEL_NUMBER_PREFIX_CAR + model.substring(1);
            allProducts.addAll(iPartsProductModels.getInstance(getProject()).getProductsByModel(getProject(), alternateModel));
            modelForLog += ", " + alternateModel;
        }

        if (allProducts.isEmpty()) {
            warnings.add(TranslationHandler.translate("!!Es konnten keine Produkte zu Baumuster \"%1\" gefunden werden.", modelForLog));
            return new DwList<>();
        }

        List<iPartsProductId> productIds = new DwList<>();
        for (iPartsProduct product : allProducts) {
            productIds.add(product.getAsId());
        }

        return createTransferSuggestionsForEDS_MBS(selectedPartlistEntries, saaNumber, productIds, false);
    }

    private List<RowContentForTransferToAS> calculateTransferListForSaModule(String saaNumber, List<EtkDataPartListEntry> selectedPartlistEntries,
                                                                             List<String> warnings) {
        List<RowContentForTransferToAS> result = new DwList<>();

        // Für den Vorschlag sind SAA und Teilenummer relevant
        String saNumber = iPartsNumberHelper.convertSAAtoSANumber(saaNumber);

        if (saNumber == null) {
            warnings.add(TranslationHandler.translate("!!Es konnte keine SA aus \"%1\" ermittelt werden.", saaNumber));
            return null;
        }

        // zur SAA nach einem passen SA Modul suchen
        iPartsSAModulesId saModulesId = new iPartsSAModulesId(saNumber);
        iPartsDataSAModules saModule = new iPartsDataSAModules(getProject(), saModulesId);
        if (saModule.existsInDB()) {
            // Wenn zur SA bereits ein TU existiert, dann diesen laden und dort nach der Teilenummer suchen
            String moduleNumber = saModule.getFieldValue(FIELD_DSM_MODULE_NO);
            AssemblyId assemblyId = new AssemblyId(moduleNumber, "");
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
            DBDataObjectList<EtkDataPartListEntry> partListUnfiltered = assembly.getPartListUnfiltered(null, false, false);
            Map<String, List<EtkDataPartListEntry>> existingEntries = new HashMap<>();
            for (EtkDataPartListEntry partListEntry : partListUnfiltered) {
                String matNr = partListEntry.getPart().getAsId().getMatNr();
                existingEntries.putIfAbsent(matNr, new DwList<>());
                existingEntries.get(matNr).add(partListEntry);
            }

            for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
                EtkDataPartListEntry referenceEntry = null;
                String matNr = selectedPartlistEntry.getPart().getAsId().getMatNr();
                List<EtkDataPartListEntry> sameMatNoList = existingEntries.get(matNr);
                if (sameMatNoList != null) {
                    for (EtkDataPartListEntry similarEntry : sameMatNoList) {
                        EtkDataArray similarSaValidity = similarEntry.getFieldValueAsArray(FIELD_K_SA_VALIDITY);
                        if (similarSaValidity.containsValue(saaNumber)) {
                            // Eintrag mit gleicher Materialnummer und passender SAA Gültigkeit gefunden
                            referenceEntry = similarEntry;
                            break;
                        }
                    }
                }

                RowContentForTransferToAS rowContent = createSARowContent(assemblyId, moduleNumber, selectedPartlistEntry,
                                                                          referenceEntry);
                result.add(rowContent);
            }

        } else {
            // Sa-Modul existiert noch nicht
            for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
                String moduleNumber = SA_MODULE_PREFIX + saNumber;

                RowContentForTransferToAS rowContent = createSARowContent(new AssemblyId(moduleNumber, ""), moduleNumber,
                                                                          selectedPartlistEntry, null);
                rowContent.setAssignmentValue(iPartsTransferAssignmentValue.NOT_ASSIGNED_NEW_SA);
                result.add(rowContent);
            }
        }
        return result;
    }

    private List<RowContentForTransferToAS> createTransferSuggestionsForEDS_MBS(List<EtkDataPartListEntry> selectedPartlistEntries,
                                                                                String saaNumber,
                                                                                Collection<iPartsProductId> matchingProductIds,
                                                                                boolean isEDS) {
        List<RowContentForTransferToAS> result = new DwList<>();
        Set<String> matchingProductNumbers = new HashSet<>();
        for (iPartsProductId productId : matchingProductIds) {
            matchingProductNumbers.add(productId.getProductNumber());
        }

        Map<String, Map<iPartsProductId, Map<KgTuId, PartListEntryId>>> suggestionCheck1Cache = new HashMap<>();
        Map<String, Map<KgTuId, PartListEntryId>> suggestionCheck2Cache = null;
        Map<KgTuId, PartListEntryId> suggestionCheck3Cache = null;

        ASUsageHelper usageHelper = new ASUsageHelper(getProject());
        for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
            String partNo = selectedPartlistEntry.getPart().getAsId().getMatNr();
            // für MBS bleibt der Key absichtlich null
            iPartsConstructionPrimaryKey constructionPrimaryKey = null;
            if (isEDS) {
                constructionPrimaryKey = iPartsSaaPartsListPrimaryKey.createFromSaaConstPartListEntry(selectedPartlistEntry, iPartsConstructionPrimaryKey.Type.SAA_PARTSLIST);
            }
            // Prüfung 1: wenn hier ein Vorschlag ermittelt werden kann, dann gilt er für alle Produkte
            Map<iPartsProductId, Map<KgTuId, PartListEntryId>> productKgTuSuggestion = suggestionCheck1Cache.get(partNo);
            if (productKgTuSuggestion == null) {
                Map<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> kgTuSuggestionCheck1 =
                        usageHelper.getEDSUsageBySaaMatNoInASPartList(saaNumber, partNo);
                productKgTuSuggestion = extractProductKgTuId(matchingProductNumbers, kgTuSuggestionCheck1);
                if (productKgTuSuggestion == null) {
                    // falls es keine Treffer gab, auf jeden Fall für die nächste gleiche PartNo vorbereiten
                    productKgTuSuggestion = new HashMap<>();
                }
                suggestionCheck1Cache.put(partNo, productKgTuSuggestion);
            }

            if (!productKgTuSuggestion.isEmpty()) {
                // Prüfung 1 hat gezogen
                // zuerst einen Hauptvorschlag ermitteln für den Fall dass es keinen konkreten Vorschlag für ein Produkt gibt
                Map<KgTuId, PartListEntryId> kgTuFallbackCheckl = null;
                iPartsProductId fallBackProductId1 = null;
                for (iPartsProductId productId : matchingProductIds) {
                    Map<KgTuId, PartListEntryId> kgTuSuggestion = productKgTuSuggestion.get(productId);
                    if (kgTuSuggestion != null) {
                        kgTuFallbackCheckl = kgTuSuggestion;
                        fallBackProductId1 = productId;
                        break;
                    }
                }

                // Ergebnis für alle Produkte übernehmen
                for (iPartsProductId productId : matchingProductIds) {
                    Map<KgTuId, PartListEntryId> kgTuSuggestion = productKgTuSuggestion.get(productId);
                    TransferContainer transferContainer;
                    if (kgTuSuggestion != null) {
                        transferContainer = new TransferContainer(kgTuSuggestion, iPartsTransferAssignmentValue.ASSIGNED);
                    } else if (kgTuFallbackCheckl != null) {
                        transferContainer = new TransferContainer(kgTuFallbackCheckl, fallBackProductId1,
                                                                  iPartsTransferAssignmentValue.ASSIGNED_OTHER_PRODUCT);
                    } else {
                        transferContainer = new TransferContainer();
                    }
                    result.add(createEDSRowContent(selectedPartlistEntry, constructionPrimaryKey, productId,
                                                   transferContainer));
                }
            } else {
                // Prüfung 1 hat kein Ergebnis geliefert also mit Prüfung 2 weitermachen
                // ErgebnisListe pro Produkt vorbereiten
                Map<iPartsProductId, TransferContainer> resultMapForRowContent = new HashMap<>();
                for (iPartsProductId productId : matchingProductIds) {
                    resultMapForRowContent.put(productId, new TransferContainer());
                }

                // Prüfung 2 Produktabhängig durchführen
                if (suggestionCheck2Cache == null) {
                    // Cache für 2. Prüfung besetzen
                    suggestionCheck2Cache = new HashMap<>();
                    Map<iPartsProductId, Map<KgTuId, PartListEntryId>> suggestionCheck2 = edsTransferListCheck2(
                            usageHelper, saaNumber, new DwList<>(matchingProductIds), matchingProductNumbers);
                    for (Map.Entry<iPartsProductId, Map<KgTuId, PartListEntryId>> entry : suggestionCheck2.entrySet()) {
                        iPartsProductId productId = entry.getKey();
                        Map<KgTuId, PartListEntryId> kgTuIdList = entry.getValue();
                        suggestionCheck2Cache.put(productId.getProductNumber(), kgTuIdList);
                    }
                }
                List<iPartsProductId> inputProductIdsForCheck3 = new DwList<>();
                // zuerst im Cache für Prüfung 2 suchen
                for (iPartsProductId productId : matchingProductIds) {
                    Map<KgTuId, PartListEntryId> kgTuIdPartEntryMap = suggestionCheck2Cache.get(productId.getProductNumber());
                    if (kgTuIdPartEntryMap != null) {
                        // Prüfung 2: Treffer für Produkt => Ergebnis merken
                        resultMapForRowContent.put(productId, new TransferContainer(kgTuIdPartEntryMap, iPartsTransferAssignmentValue.ASSIGNED_OTHER_PART));
                    } else {
                        // Kein Treffer => Vorbereitung für Prüfung 3
                        inputProductIdsForCheck3.add(productId);
                    }
                }

                // Prüfung 3 für alle Produkte die bisher noch keinen Vorschlag bekommen haben
                if (!inputProductIdsForCheck3.isEmpty()) {
                    // prüfen ob der Cache schon gefüllt ist, wenn nicht muss er jetzt bestimmt werden
                    if (suggestionCheck3Cache == null) {
                        suggestionCheck3Cache = edsTransferlistCheck3(usageHelper, saaNumber,
                                                                      inputProductIdsForCheck3, matchingProductNumbers);
                    }
                    if (suggestionCheck3Cache != null) {
                        // restliche Produkte mit Ergebnis von Prüfung 3 befüllen
                        for (iPartsProductId productId : inputProductIdsForCheck3) {
                            resultMapForRowContent.put(productId, new TransferContainer(suggestionCheck3Cache, iPartsTransferAssignmentValue.ASSIGNED_OTHER_SAA));
                        }
                    }
                }

                // Ergebisse aus Prüfung 2 und 3 in der richtigen Produktreihenfolge ins Result
                for (iPartsProductId productId : matchingProductIds) {
                    TransferContainer transferContainer = resultMapForRowContent.get(productId);
                    if (transferContainer == null) {
                        // kein Treffer für Produkt in Prüfung 2 und 3 => einfach leeren TransferContainer
                        transferContainer = new TransferContainer();
                    }
                    result.add(createEDSRowContent(selectedPartlistEntry, constructionPrimaryKey,
                                                   productId, transferContainer));
                }
            }
        }
        return result;
    }


    private class TransferContainer {

        protected iPartsTransferAssignmentValue assignementValue;
        protected Map<KgTuId, PartListEntryId> kgTuIdPartEntryMap;
        private List<KgTuId> alternateKgTuIdList;
        private iPartsDataPartListEntry partListEntry;
        private iPartsProductId alternateProductId;

        public TransferContainer() {
            this.alternateKgTuIdList = null;
            this.kgTuIdPartEntryMap = null;
            this.partListEntry = null;
            this.alternateProductId = null;
            this.assignementValue = iPartsTransferAssignmentValue.NOT_ASSIGNED;
        }

        public TransferContainer(Map<KgTuId, PartListEntryId> kgTuPartEntryMap,
                                 iPartsTransferAssignmentValue assignmentValue) {
            this();
            this.kgTuIdPartEntryMap = kgTuPartEntryMap;
            this.assignementValue = assignmentValue;
        }

        public TransferContainer(Map<KgTuId, PartListEntryId> kgTuPartEntryMap,
                                 iPartsProductId alternateProductId,
                                 iPartsTransferAssignmentValue assignmentValue) {
            this();
            this.kgTuIdPartEntryMap = kgTuPartEntryMap;
            this.assignementValue = assignmentValue;
            this.alternateProductId = alternateProductId;
        }

        public KgTuId getKgTuId() {
            if ((kgTuIdPartEntryMap != null) && !kgTuIdPartEntryMap.isEmpty()) {
                return kgTuIdPartEntryMap.keySet().iterator().next();
            }
            return null;
        }

        public List<KgTuId> getAlternateKgTuIdList() {
            return getAlternateKgTuIdList(getKgTuId());
        }

        public List<KgTuId> getAlternateKgTuIdList(KgTuId kgTuIdForRemove) {
            if (kgTuIdForRemove != null) {
                if (kgTuIdPartEntryMap != null) {
                    if (alternateKgTuIdList == null) {
                        alternateKgTuIdList = new DwList<>(kgTuIdPartEntryMap.keySet());
                        alternateKgTuIdList.remove(kgTuIdForRemove);
                    }
                }
                if (!alternateKgTuIdList.isEmpty()) {
                    return alternateKgTuIdList;
                }
            }
            return null;
        }

        public String getHotSpot() {
            KgTuId currentKgTuId = getKgTuId();
            if (currentKgTuId != null) {
                if (kgTuIdPartEntryMap != null) {
                    if (partListEntry == null) {
                        PartListEntryId entryId = kgTuIdPartEntryMap.get(currentKgTuId);
                        if (entryId != null) {
                            partListEntry = new iPartsDataPartListEntry(getProject(), entryId);
                        }
                    }
                    if (partListEntry != null) {
                        if (partListEntry.existsInDB()) {
                            return partListEntry.getFieldValue(iPartsConst.FIELD_K_POS);
                        }
                    }
                }
            }
            return "";
        }

        public iPartsProductId getAlternateProductId() {
            return alternateProductId;
        }
    }

    private Map<iPartsProductId, Map<KgTuId, PartListEntryId>> edsTransferListCheck2(ASUsageHelper usageHelper, String saaNumber,
                                                                                     List<iPartsProductId> inputProductIdsForCheck2,
                                                                                     Set<String> matchingProductNumbers) {
        Map<iPartsProductId, Map<KgTuId, PartListEntryId>> result = new TreeMap<>();
        Map<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> kgTuSuggestionCheck2 =
                usageHelper.getEDSUsageBySaaProductInASPartList(saaNumber, inputProductIdsForCheck2);
        for (Map.Entry<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> entry : kgTuSuggestionCheck2.entrySet()) {
            iPartsProductId productId = entry.getKey();
            if (matchingProductNumbers.contains(productId.getProductNumber()) &&
                inputProductIdsForCheck2.contains(productId)) {
                Map<KgTuId, PartListEntryId> kgTuIdPartEntryMap = getKgTuListFromUsageContainers(entry.getValue());
                if (kgTuIdPartEntryMap != null) {
                    result.put(productId, kgTuIdPartEntryMap);
                }
            }
        }
        return result;
    }

    private Map<KgTuId, PartListEntryId> edsTransferlistCheck3(ASUsageHelper usageHelper, String saaNumber,
                                                               List<iPartsProductId> inputProductIdsForCheck3,
                                                               Set<String> matchingProductNumbers) {
        Map<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> kgTuSuggestionCheck3 =
                usageHelper.getEDSUsageBySaASPartList(saaNumber);
        if (!kgTuSuggestionCheck3.isEmpty()) {
            // pro Vorschlag prüfen ob das Produkt in der Liste der relevanten Produkte enthalten ist
            for (Map.Entry<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> productEntry : kgTuSuggestionCheck3.entrySet()) {
                iPartsProductId productId = productEntry.getKey();
                if (matchingProductNumbers.contains(productId.getProductNumber()) &&
                    inputProductIdsForCheck3.contains(productId)) {
                    Map<KgTuId, PartListEntryId> kgTuIdPartEntryMap = getKgTuListFromUsageContainers(productEntry.getValue());
                    if (kgTuIdPartEntryMap != null) {
                        return kgTuIdPartEntryMap;
                    }
                }
            }
        }
        return null;
    }

    private Map<KgTuId, PartListEntryId> getKgTuListFromUsageContainers(List<ASUsageHelper.EDSUsageContainer> usageContainers) {
        if ((usageContainers != null) && !usageContainers.isEmpty()) {
            Map<KgTuId, PartListEntryId> kgTuIdPartEntryMap = new LinkedHashMap<>();
            for (ASUsageHelper.EDSUsageContainer usageContainer : usageContainers) {
                KgTuId kgTuId = usageContainer.getKgTuId();
                PartListEntryId partListEntryId = null;
                if (!usageContainer.getPartListEntryIdList().isEmpty()) {
                    partListEntryId = usageContainer.getPartListEntryIdList().get(0);
                }
                kgTuIdPartEntryMap.put(kgTuId, partListEntryId);
            }
            return kgTuIdPartEntryMap;
        }
        return null;
    }

    /**
     * Pro gewählter MatNr alle TU Übernahme Positionen
     *
     * @param rowContentForTransferToASList
     * @return
     */
    private Map<String, List<RowContentForTransferToAS>> getMatNrToRowContent(List<RowContentForTransferToAS> rowContentForTransferToASList) {
        Map<String, List<RowContentForTransferToAS>> matNrToRowContent = new LinkedHashMap<>();
        for (RowContentForTransferToAS rowContentForTransferToAS : rowContentForTransferToASList) {
            String matNr = rowContentForTransferToAS.getSelectedPartlistEntry().getFieldValue(iPartsConst.FIELD_K_MATNR);
            matNrToRowContent.putIfAbsent(matNr, new ArrayList<>());
            matNrToRowContent.get(matNr).add(rowContentForTransferToAS);
        }
        return matNrToRowContent;
    }


    private RowContentForTransferToAS createEDSRowContent(EtkDataPartListEntry selectedPartlistEntry,
                                                          iPartsConstructionPrimaryKey constructionPrimaryKey,
                                                          iPartsProductId productId,
                                                          TransferContainer transferContainer) {
        iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
        RowContentForTransferToAS tableRow = new RowContentForTransferToAS(constructionPrimaryKey, selectedPartlistEntry, product);
        tableRow.setKgTuId(transferContainer.getKgTuId());
        tableRow.setAssignmentValue(transferContainer.assignementValue);
        tableRow.setAlternativeKgTuList(transferContainer.getAlternateKgTuIdList());
        tableRow.setFallBackProductId(transferContainer.getAlternateProductId());
        // Hotspots nur bei Prüfung 1 als Vorschlag übernehmen
        if (transferContainer.assignementValue == iPartsTransferAssignmentValue.ASSIGNED) {
            tableRow.setHotspot(transferContainer.getHotSpot());
        }
        return tableRow;
    }

    private RowContentForTransferToAS createSARowContent(AssemblyId assemblyId, String saModulNumber, EtkDataPartListEntry selectedPartlistEntry,
                                                         EtkDataPartListEntry referencePartlistEntry) {
        RowContentForTransferToAS tableRow = new RowContentForTransferToAS();

        String hotspot = "";
        if (referencePartlistEntry != null) {
            tableRow.setAssignmentValue(iPartsTransferAssignmentValue.ASSIGNED);
            tableRow.setAssignedASpartlistEntry(referencePartlistEntry.getAsId());
            hotspot = referencePartlistEntry.getFieldValue(iPartsConst.FIELD_K_POS);
        } else {
            tableRow.setAssignmentValue(iPartsTransferAssignmentValue.NOT_ASSIGNED_EXISTING_SA);
        }

        tableRow.getTransferElement().setAssemblyId(assemblyId);
        tableRow.getTransferElement().setSaModuleNumber(saModulNumber);
        tableRow.getTransferElement().setSelectedPartlistEntry(selectedPartlistEntry);
        tableRow.getTransferElement().setHotspot(hotspot);
        if ((formMode == Mode.EDS) || (formMode == Mode.CTT)) {
            tableRow.setConstructionPrimaryKey(iPartsSaaPartsListPrimaryKey.createFromSaaConstPartListEntry(selectedPartlistEntry, iPartsConstructionPrimaryKey.Type.SAA_PARTSLIST));
        }
        // MBS: sourceGUID bzw. der ConstPrimaryKey kann hier noch nicht besetzt werden, da die laufende Nummer noch nicht bekannt ist
        return tableRow;
    }

    private Map<KgTuId, PartListEntryId> extractKgTuId(Set<String> matchingProductNumbers,
                                                       Map<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> suggestionResult) {
        if (!suggestionResult.isEmpty()) {
            // pro Vorschlag prüfen ob das Produkt in der Liste der relevanten Produkte enthalten ist
            for (Map.Entry<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> productEntry : suggestionResult.entrySet()) {
                iPartsProductId productId = productEntry.getKey();
                if (matchingProductNumbers.contains(productId.getProductNumber())) {
                    Map<KgTuId, PartListEntryId> kgtuSuggestion = getKgTuListFromUsageContainers(productEntry.getValue());
                    if (kgtuSuggestion != null) {
                        return kgtuSuggestion;
                    }
                }
            }
        }
        return null;
    }

    private Map<iPartsProductId, Map<KgTuId, PartListEntryId>> extractProductKgTuId(Set<String> matchingProductNumbers,
                                                                                    Map<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> suggestionResult) {
        if (!suggestionResult.isEmpty()) {
            // pro Vorschlag prüfen ob das Produkt in der Liste der relevanten Produkte enthalten ist
            Map<iPartsProductId, Map<KgTuId, PartListEntryId>> result = new HashMap<>();
            for (Map.Entry<iPartsProductId, List<ASUsageHelper.EDSUsageContainer>> productEntry : suggestionResult.entrySet()) {
                iPartsProductId productId = productEntry.getKey();
                if (matchingProductNumbers.contains(productId.getProductNumber())) {
                    Map<KgTuId, PartListEntryId> kgtuSuggestion = getKgTuListFromUsageContainers(productEntry.getValue());
                    if (kgtuSuggestion != null) {
                        result.put(productId, kgtuSuggestion);
                    }
                }
            }
            return result;
        }
        return null;
    }

    public void findCompleteRetailUse(List<EtkDataPartListEntry> selectedPartlistEntries, List<EtkDataPartListEntry> alreadyTransferredPartlistEntries) {
        // Suche via sourceContext beschleunigt
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(selectedPartlistEntries.get(0));
        if (bctePrimaryKey != null) {
            String sourceContext = EditConstructionToRetailHelper.createSourceContext(iPartsEntrySourceType.DIALOG, bctePrimaryKey.getHmMSmId());
            List<EtkDataPartListEntry> retailPartListEntries = EditConstructionToRetailHelper.getRetailPartListEntries(iPartsEntrySourceType.DIALOG, sourceContext,
                                                                                                                       null, getProject());
            // Nach BCTE Schlüssel gruppieren um nicht jedesmal die Liste zu durchlaufen
            Map<String, List<EtkDataPartListEntry>> foundRetailPartListEntries = new HashMap<>();
            retailPartListEntries.forEach(entry -> {
                String guid = entry.getFieldValue(FIELD_K_SOURCE_GUID);
                List<EtkDataPartListEntry> entries = foundRetailPartListEntries.computeIfAbsent(guid, k -> new ArrayList<>());
                entries.add(entry);
            });

            findRetailUse(selectedPartlistEntries, foundRetailPartListEntries);
            findRetailUse(alreadyTransferredPartlistEntries, foundRetailPartListEntries);
        }
    }

    protected void findRetailUse(List<EtkDataPartListEntry> partlistEntries, Map<String, List<EtkDataPartListEntry>> foundRetailPartListEntries) {
        Map<String, List<EtkDataPartListEntry>> foundRetailPartListEntriesForPartialGUID = null;
        for (EtkDataPartListEntry partListEntry : partlistEntries) {
            String guid = partListEntry.getAsId().getKLfdnr();
            List<EtkDataPartListEntry> retailPartlistEntries = guidToRetailUse.get(guid);
            if (retailPartlistEntries == null) {
                retailPartlistEntries = new DwList<>();
                guidToRetailUse.put(guid, retailPartlistEntries);
            }

            if (partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(iPartsDataVirtualFieldsDefinition.RETAIL_ASSIGNED)) {
                List<EtkDataPartListEntry> retailSourceGuidPartListEntries;
                if ((foundRetailPartListEntries == null) || (foundRetailPartListEntries.get(guid) == null)) {
                    // Einzelsuche via Guid
                    retailSourceGuidPartListEntries = EditConstructionToRetailHelper.
                            getRetailSourceGuidPartListEntries(iPartsEntrySourceType.DIALOG, guid, null, getProject());
                } else {
                    // Suche in sourceContext-Liste
                    retailSourceGuidPartListEntries = findGuidInFoundRetailPartListEntries(guid, foundRetailPartListEntries);
                }
                retailPartlistEntries.addAll(retailSourceGuidPartListEntries);
            }
            // Die AS Positionen zum Teilschlüssel BR-HM-M-SM-POSE gruppieren
            if (foundRetailPartListEntries != null) {
                iPartsDialogBCTEPrimaryKey primaryKey = getPositionBCTEKeyFromPartListEntry(partListEntry);
                if (primaryKey != null) {
                    String partialGUID = primaryKey.createGUID();
                    Set<EtkDataPartListEntry> retailPartlistEntriesForPartialGUID = partialGUIDToRetailUse.computeIfAbsent(partialGUID, k -> new LinkedHashSet<>());
                    // AS Positionen nach ihrem Teilschlüssel gruppieren
                    if (foundRetailPartListEntriesForPartialGUID == null) {
                        foundRetailPartListEntriesForPartialGUID = new HashMap<>();
                        for (Map.Entry<String, List<EtkDataPartListEntry>> entry : foundRetailPartListEntries.entrySet()) {
                            String foundGUID = entry.getKey();
                            List<EtkDataPartListEntry> entries = entry.getValue();
                            iPartsDialogBCTEPrimaryKey partialBCTEKey = getPositionBCTEKeyFromGUID(foundGUID);
                            if (partialBCTEKey != null) {
                                String partialGUIDString = partialBCTEKey.createGUID();
                                List<EtkDataPartListEntry> entriesForPartialGUID = foundRetailPartListEntriesForPartialGUID.computeIfAbsent(partialGUIDString, k -> new ArrayList<>());
                                entriesForPartialGUID.addAll(entries);
                            }
                        }
                    }

                    List<EtkDataPartListEntry> entriesForPartialGUID = foundRetailPartListEntriesForPartialGUID.get(partialGUID);
                    if (entriesForPartialGUID != null) {
                        retailPartlistEntriesForPartialGUID.addAll(entriesForPartialGUID);
                    }
                }
            }
        }
    }

    private iPartsDialogBCTEPrimaryKey getPositionBCTEKeyFromPartListEntry(EtkDataPartListEntry partListEntry) {
        iPartsDialogBCTEPrimaryKey primaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
        if (primaryKey != null) {
            return primaryKey.getPositionBCTEPrimaryKey();
        }
        return null;
    }

    private iPartsDialogBCTEPrimaryKey getPositionBCTEKeyFromGUID(String guid) {
        iPartsDialogBCTEPrimaryKey completeGUID = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(guid);
        if (completeGUID != null) {
            return completeGUID.getPositionBCTEPrimaryKey();
        }
        return null;
    }

    private List<EtkDataPartListEntry> findGuidInFoundRetailPartListEntries(String guid, Map<String, List<EtkDataPartListEntry>> foundRetailPartListEntries) {
        List<EtkDataPartListEntry> result = new DwList<>();
        List<EtkDataPartListEntry> entries = foundRetailPartListEntries.get(guid);
        if (entries != null) {
            result.addAll(entries);
        }
        return result;
    }

    private void buildSubTitleDIALOG(iPartsSeriesId seriesId, List<EtkDataPartListEntry> selectedPartlistEntries) {
        Map<String, Set<String>> aaToPosMap = new TreeMap<>();
        for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
            iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(selectedPartlistEntry);
            if (bctePrimaryKey != null) {
                Set<String> mapEntry = aaToPosMap.computeIfAbsent(bctePrimaryKey.aa, k -> new TreeSet<>());
                mapEntry.add(bctePrimaryKey.posE);
            }
        }

        StringBuilder title = new StringBuilder();
        for (Map.Entry<String, Set<String>> aaToPos : aaToPosMap.entrySet()) {
            title.append(TranslationHandler.translate("!!BR: %1 - AA: %2 - Pos: %3%4", seriesId.getSeriesNumber(), aaToPos.getKey(), StrUtils.stringListToString(aaToPos.getValue(), ", "), "\n"));
        }
        setSubtitle(title.toString());
    }

    private void buildSubTitleEDS(List<EtkDataPartListEntry> selectedPartlistEntries) {
        // SAA und Teilenummern anzeigen
        StringBuilder title = new StringBuilder();
        if (sourceId instanceof EdsSaaId) {
            EdsSaaId saaId = (EdsSaaId)(sourceId);
            String formattedSAA = iPartsNumberHelper.formatPartNo(getProject(), saaId.getSaaNumber());
            if (transferMode == TransferMode.SA) {
                title.append(TranslationHandler.translate("!!Übernahme in eine freie SA für SAA: %1", formattedSAA));
            } else {
                title.append(TranslationHandler.translate("!!Übernahme in eine Stückliste für SAA: %1", formattedSAA));
            }
        }

        appendPartNumbers(selectedPartlistEntries, title);
        setSubtitle(title.toString());
    }

    private void appendPartNumbers(List<EtkDataPartListEntry> selectedPartlistEntries, StringBuilder title) {
        if (title != null) {
            List<String> partNumbers = new DwList<>();
            int partNoCount = 0;
            for (EtkDataPartListEntry selectedPartlistEntry : selectedPartlistEntries) {
                if (partNoCount > PARTNOLIMIT) {
                    partNumbers.add("...");
                    break;
                }
                partNumbers.add(iPartsNumberHelper.formatPartNo(getProject(), selectedPartlistEntry.getPart().getAsId().getMatNr()));
                partNoCount++;
            }
            title.append("\n");
            if (!partNumbers.isEmpty()) {
                if (partNumbers.size() > 1) {
                    title.append(TranslationHandler.translate("!!Teilenummern: %1", StrUtils.stringListToString(partNumbers, ", ")));
                } else {
                    title.append(TranslationHandler.translate("!!Teilenummer: %1", partNumbers.get(0)));
                }
            }
        }
    }

    private void buildSubTitleMBS(List<EtkDataPartListEntry> selectedPartlistEntries) {
        StringBuilder title = new StringBuilder();
        if (sourceId instanceof MBSStructureId) {
            MBSStructureId mbsStructureId = (MBSStructureId)(sourceId);
            String formattedNumber = iPartsNumberHelper.formatPartNo(getProject(), mbsStructureId.getListNumber());
            if (transferMode == TransferMode.SA) {
                title.append(TranslationHandler.translate("!!Übernahme in eine freie SA aus einer SAA-KG Stückliste: %1", formattedNumber));
            } else {
                if (mbsStructureId.isBasePartlistId()) {
                    title.append(TranslationHandler.translate("!!Übernahme aus einer Grundstückliste: %1", formattedNumber));
                } else {
                    title.append(TranslationHandler.translate("!!Übernahme aus einer SAA-KG Stückliste: %1", formattedNumber));
                }
            }

            appendPartNumbers(selectedPartlistEntries, title);
            setSubtitle(title.toString());
        }
    }

    protected List<iPartsProduct> preFilterProducts() {
        if (preFilteredProducts == null) {
            preFilteredProducts = new DwList<>();
            if (masterProduct != null) {
                preFilteredProducts.add(masterProduct);
            } else {
                // Liste aller Produkte und Eingrenzen nach referenzierter Baureihe (früher wurde nach der verknüpften Baureihe eingegrenzt)
                List<iPartsProduct> productList = iPartsProduct.getAllProducts(getConnector().getProject());
                for (iPartsProduct product : productList) {
                    if ((product.getReferencedSeries() != null) && product.getReferencedSeries().equals(seriesId)) {
                        iPartsModuleTypes moduleType = product.getDocumentationType().getModuleType(false);
                        // Damit für die Übernahme aus der DIALOG-Konstruktion keine Nicht-DIALOG-Produkte verwendet werden,
                        // werden die Suchergebnisse bezüglich des Modultyps überprüft
                        // Wenn das Produkt keinen Dokumentationstyp hat, dann wird der Modultyp später anahnd der Herkunft bestimmt (HmMSm oder OPSSaa)
                        if (iPartsModuleTypes.isDialogRetailType(moduleType) || (product.getDocumentationType() == iPartsDocumentationType.UNKNOWN)) {
                            // nur KG/TU-Produkte erlaubt
                            if (product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.KG_TU) {
                                preFilteredProducts.add(product);
                            }
                        }
                    }
                }
            }
        }
        return preFilteredProducts;
    }

    protected List<iPartsProduct> findMatchingProductsForAA(String matrixCode) {
        List<iPartsProduct> resultProducts = aaDetectedProductMap.get(matrixCode);
        if (resultProducts == null) {
            resultProducts = new DwList<>();
            for (iPartsProduct product : preFilterProducts()) {
                if (product.hasModelsWithAA(getConnector().getProject(), matrixCode)) {
                    resultProducts.add(product);
                }
            }

//            // Liste aller Produkte und Eingrenzen nach referenzierter Baureihe (früher wurde nach der verknüpften Baureihe eingegrenzt)
//            List<iPartsProduct> productList = iPartsProduct.getAllProducts(getConnector().getProject());
//            resultProducts = new DwList<iPartsProduct>();
//            for (iPartsProduct product : productList) {
//                if ((product.getReferencedSeries() != null) && product.getReferencedSeries().equals(seriesId)) {
//                    iPartsModuleTypes moduleType = product.getDocumentationType().getModuleType(false);
//                    // Damit für die Übernahme aus der DIALOG-Konstruktion keine Nicht-DIALOG-Produkte verwendet werden,
//                    // werden die Suchergebnisse bezüglich des Modultyps überprüft
//                    // Wenn das Produkt keinen Dokumentationstyp hat, dann wird der Modultyp später anahnd der Herkunft bestimmt (HmMSm oder OPSSaa)
//                    if (iPartsModuleTypes.isDialogRetailType(moduleType) || (product.getDocumentationType() == iPartsDocumentationType.UNKNOWN)) {
//                        // nur KG/TU-Produkte erlaubt
//                        if (product.getProductStructuringType() == PRODUCT_STRUCTURING_TYPE.KG_TU) {
//                            for (String ausfuehrungsart : this.matrixCodeList) {
//                                if (product.hasModelsWithAA(getConnector().getProject(), ausfuehrungsart)) {
//                                    resultProducts.add(product);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
            aaDetectedProductMap.put(matrixCode, resultProducts);
        }

        return resultProducts;
    }

    private Set<iPartsProductId> findMatchingProductsIdsForSAA(EdsSaaId edsSaaId) {
        iPartsSaaId saaId = new iPartsSaaId(edsSaaId.getSaaNumber());
        return iPartsDataSAAModelsList.loadAllProductsForSaaBk(getProject(), saaId, false);
    }


    private void setSubtitle(String text) {
        mainWindow.title.setSubtitle(text);
    }


    public ModalResult showModal() {
        ModalResult result = mainWindow.showModal();
        dispose();
        return result;
    }

    private void onOKbuttonClick(Event event) {
        List<TransferToASElement> allTransferItems = grid.getAllTransferItems();
        if (!allTransferItems.isEmpty()) {

            // Prüfen ob zur Übernahme markierte Zeilen ausgeblendet sind
            List<RowContentForTransferToAS> allVisibleTransferItems = grid.getAllVisibleTransferItems();
            if (allVisibleTransferItems.size() != allTransferItems.size()) {
                String msg = TranslationHandler.translate("!!Es sind gefilterte Zeilen zur Übernahme markiert.") + "\n\n" +
                             TranslationHandler.translate("!!Trotzdem fortfahren?");
                ModalResult modalResult = MessageDialog.showYesNo(msg, "!!Übernahme");
                if (modalResult == ModalResult.NO) {
                    return;
                }
            }
            doTransfer(allTransferItems, mainWindow.checkboxShowModule.isSelected());
        }
    }

    public void doTransfer(List<TransferToASElement> allTransferItems, boolean openModulesInEdit) {
        Map<String, List<TransferToASElement>> moduleTransferMap = new HashMap<>();
        Map<String, TransferToASElement> notExistingModuleTransferMap = new HashMap<>();

        // handelt es sich um die Übernahme in ein SA-Modul?
        if (transferMode == TransferMode.SA) {
            collectByModulesSA(allTransferItems, moduleTransferMap, notExistingModuleTransferMap);
        } else {
            collectByModules(allTransferItems, moduleTransferMap, notExistingModuleTransferMap);
        }
        // Bestätigungsdialog, ob nicht existierende Module erzeugt werden sollen (sofern vorhanden)
        mainWindow.setModalResult(ModalResult.CANCEL);
        if (iPartsConst.ONLY_SINGLE_MODULE_PER_KGTU && !checkNewModulesIfExists(notExistingModuleTransferMap, moduleTransferMap)) {
            return;
        }

        List<EditModuleForm.EditModuleInfo> moduleInfosInEditor = new DwList<>();
        List<EditModuleForm.EditModuleInfo> moduleInfosInEditorToSave = new DwList<>();

        if (!checkLoadedEditModules(moduleTransferMap, moduleInfosInEditor, moduleInfosInEditorToSave)) {
            return;
        }

        EtkMessageLogForm progressForm = null;
        if (logHelper == null) {
            // Dialog zeigt den Fortschritt der Übernahme, und bleibt so lange im Vordergrund bis optional die Module geladen wurden
            progressForm = new EtkMessageLogForm("!!In AS-Stückliste übernehmen", "!!Fortschritt", null);
        }
        FrameworkRunnable runnable = createRunnable(moduleTransferMap, notExistingModuleTransferMap,
                                                    moduleInfosInEditor, moduleInfosInEditorToSave,
                                                    progressForm, openModulesInEdit);
        if (logHelper == null) {
            progressForm.disableButtons(true);
            progressForm.setMessagesTitle("");
            progressForm.getGui().setSize(600, 250);

            progressForm.showModal(runnable);
        } else {
            runnable.run(null);
        }
    }

    private FrameworkRunnable createRunnable(final Map<String, List<TransferToASElement>> moduleTransferMap,
                                             final Map<String, TransferToASElement> notExistingModuleTransferMap,
                                             final List<EditModuleForm.EditModuleInfo> moduleInfosInEditor,
                                             final List<EditModuleForm.EditModuleInfo> moduleInfosInEditorToSave,
                                             final EtkMessageLogForm progressForm, final boolean openModulesInEdit) {
        return new FrameworkRunnable() {

            @Override
            public void run(FrameworkThread thread) {
                EtkMessageLog messageLog = null;
                if (progressForm != null) {
                    messageLog = progressForm.getMessageLog();
                }
                EditTransferToASHelper transferToASHelper = new EditTransferToASHelper(getProject(), sourceConstAssembly, sourceId);
                transferToASHelper.setLogHelper(logHelper);
                try {
                    // Neue Module anlegen
                    transferToASHelper.createNewModules(notExistingModuleTransferMap, null, techChangeSet);
                    // Module im Editor speichern
                    saveEditModules(moduleInfosInEditorToSave);

                    // Stücklisteneinträge übernehmen
                    List<String> logMessages = new ArrayList<>();
                    if (formMode == Mode.DIALOG) {
                        transferToASHelper.createAndTransferDIALOGPartListEntriesForTransferToAS(moduleTransferMap, notExistingModuleTransferMap,
                                                                                                 logMessages, messageLog, techChangeSet);
                    } else if (isSaaPartsListMode() || (formMode == Mode.MBS)) {
                        transferToASHelper.createAndTransferPartListEntriesEDS_MBS(moduleTransferMap, notExistingModuleTransferMap,
                                                                                   logMessages, messageLog);
                    } else {
                        logMessages.add("Not implemented");
                    }
                    if (!logMessages.isEmpty()) {
                        if (logHelper == null) {
                            MessageDialog.show(logMessages);
                        } else {
                            for (String msg : logMessages) {
                                logHelper.addLogMsgWithTranslation(msg);
                            }
                        }
                    }

                } catch (EditTransferToASHelper.EditTransferPartListEntriesException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                    if (logHelper == null) {
                        mainWindow.showWaitCursor(false);
                        mainWindow.setVisible(false);
                        MessageDialog.showError(e.getMessage());
                    } else {
                        logHelper.addLogErrorWithTranslation(e.getMessage());
                    }
                    return;
                } catch (Exception e) {
                    if (logHelper == null) {
                        mainWindow.showWaitCursor(false);
                        mainWindow.setVisible(false);
                    } else {
                        logHelper.addLogErrorWithTranslation(e.getMessage());
                    }
                    Logger.getLogger().throwRuntimeException(e);
                }

                startPseudoTransactionForActiveChangeSet(true);
                try {
                    // Es müssen diverse Caches aktualisiert werden
                    boolean dataChanged = handleCachesAndModifications(moduleTransferMap, notExistingModuleTransferMap, transferToASHelper.isWithLinkingChanges());

                    // Optional alle betroffenen Module öffnen
                    openModifiedModulesInEdit(openModulesInEdit, moduleTransferMap, moduleInfosInEditor, progressForm);

                    if (dataChanged) {
                        // Jetzt erst den DataChangedEvent feuern, damit ein bereits im Edit geladenes Modul nicht unnötig
                        // neu geladen wird (welches mit der Übernahme ja gar nichts zu tun hat)
                        getProject().fireProjectEvent(new DataChangedEvent(null), true);
                    }
                } finally {
                    stopPseudoTransactionForActiveChangeSet();
                }

                mainWindow.setModalResult(ModalResult.OK);
                mainWindow.setVisible(false);
            }
        };
    }

    private void openModifiedModulesInEdit(final boolean loadModules,
                                           final Map<String, List<TransferToASElement>> moduleTransferMap,
                                           final List<EditModuleForm.EditModuleInfo> moduleInfosInEditor,
                                           EtkMessageLogForm progressForm) {
        if (loadModules || !moduleInfosInEditor.isEmpty()) {
            String msg = TranslationHandler.translate("!!Lade %1 TUs...", String.valueOf(moduleTransferMap.size()));
            if (moduleTransferMap.size() == 1) {
                msg = TranslationHandler.translate("!!Lade TU...");
            }
            progressForm.getMessageLog().fireMessage(msg);
            // damit auch in Swing was geht
            if (J2EEHandler.isJ2EE()) {
                Session.invokeThreadSafeInSession(() -> loadOrReloadModules(loadModules, moduleTransferMap, moduleInfosInEditor));
            } else {
                loadOrReloadModules(loadModules, moduleTransferMap, moduleInfosInEditor);
            }
        }
    }

    private boolean handleCachesAndModifications(Map<String, List<TransferToASElement>> moduleTransferMap,
                                                 Map<String, TransferToASElement> notExistingModuleTransferMap,
                                                 boolean withLinkingChanges) {
        iPartsEntrySourceType retailUsageType;
        if (formMode == Mode.DIALOG) {
            retailUsageType = iPartsEntrySourceType.DIALOG;
        } else if (isSaaPartsListMode()) {
            retailUsageType = iPartsEntrySourceType.EDS;
        } else if (formMode == Mode.MBS) {
            retailUsageType = iPartsEntrySourceType.NONE; // todo anpassen wenn wir wissen wie die Retailverwendung in MBS geht
        } else {
            return false; // sollte nicht auftreten
        }

        Set<iPartsProductId> productIdList = collectProducts(notExistingModuleTransferMap);
        if (!productIdList.isEmpty()) {
            for (iPartsProductId productId : productIdList) {
                // Bei aktivem ChangeSet muss kein CacheHelper.invalidateCaches() aufgerufen werden, sondern das
                // Entfernen des betroffenen Produkts aus dem Cache und ein paar nachfolgende Events für das Produkt
                // und alle neuen Module reichen
                iPartsProduct.removeProductFromCache(getProject(), productId);
                KgTuForProduct.removeKgTuForProductFromCache(getProject(), productId);
            }
            getProject().fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PRODUCT,
                                                                             iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                             productIdList, false), true);
        }

        // Neue Module
        Collection<AssemblyId> newModuleIds = new DwList<>();
        if (!notExistingModuleTransferMap.isEmpty()) {
            for (TransferToASElement transferElement : notExistingModuleTransferMap.values()) {
                newModuleIds.add(transferElement.getAssemblyId());
            }
            getProject().fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                             iPartsDataChangedEventByEdit.Action.NEW,
                                                                             newModuleIds, false), true);
        }

        // Neue Stücklisteneinträge
        Set<iPartsRetailUsageId> newRetailUsageIds = new HashSet<>();

        // Veränderte Module
        List<AssemblyId> modifiedAssemblies = new DwList<>();
        for (Map.Entry<String, List<TransferToASElement>> entry : moduleTransferMap.entrySet()) {
            List<TransferToASElement> transferElements = entry.getValue();
            for (TransferToASElement transferElement : transferElements) {
                if (retailUsageType != iPartsEntrySourceType.NONE) {
                    newRetailUsageIds.add(new iPartsRetailUsageId(retailUsageType.getDbValue(), transferElement.getSourceGUIDForAttribute()));
                }
                AssemblyId assemblyId = transferElement.getAssemblyId();
                if ((assemblyId != null) && !newModuleIds.contains(assemblyId) && !modifiedAssemblies.contains(assemblyId)) {
                    modifiedAssemblies.add(assemblyId);
                    EtkDataAssembly.removeDataAssemblyFromCache(getProject(), assemblyId);
                }
            }
        }

        // Es wurde während der Übernahme gekoppelt -> Konstruktions-Assembly muss raus aus den Caches
        // Event muss geschmissen werden
        if (withLinkingChanges) {
            modifiedAssemblies.add(sourceConstAssembly.getAsId());
            EtkDataAssembly.removeDataAssemblyFromCache(getProject(), sourceConstAssembly.getAsId());
        }

        if (!modifiedAssemblies.isEmpty()) {
            getProject().fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                             iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                             modifiedAssemblies, false));
            if (withLinkingChanges) {
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
            }
        }

        if (!newRetailUsageIds.isEmpty()) {
            getProject().fireProjectEvent(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.RETAIL_USAGE,
                                                                             iPartsDataChangedEventByEdit.Action.NEW,
                                                                             newRetailUsageIds, false));
            return true;
        } else {
            return false;
        }
    }

    /**
     * neu erzeugte Module ggf im Editor laden und modifizierte Module im Editor aktualisieren
     *
     * @param loadModules
     * @param moduleTransferMap
     * @param moduleInfosInEditor
     */
    private void loadOrReloadModules(boolean loadModules, Map<String, List<TransferToASElement>> moduleTransferMap,
                                     List<EditModuleForm.EditModuleInfo> moduleInfosInEditor) {
        EditModuleForm editModuleForm = EditTransferToASHelper.getEditModuleForm(getConnector());
        if (editModuleForm != null) {
            GuiWindow.showWaitCursorForRootWindow(true);
            try {
                if (loadModules || !moduleInfosInEditor.isEmpty()) {
                    startPseudoTransactionForActiveChangeSet(true);
                    try {
                        if (loadModules) {
                            boolean firstModule = true;
                            for (Map.Entry<String, List<TransferToASElement>> entry : moduleTransferMap.entrySet()) {
                                if (!entry.getValue().isEmpty()) {
                                    AssemblyId assemblyId = entry.getValue().get(0).getAssemblyId();
                                    if (assemblyId != null) {
                                        if (!EditTransferToASHelper.isLoadedInEditor(assemblyId, moduleInfosInEditor)) {
                                            editModuleForm.loadModule(assemblyId.getKVari());
                                        } else if (firstModule) { // Erstes Modul im Edit selektieren (wird beim Laden auch gemacht)
                                            editModuleForm.selectModuleTab(assemblyId.getKVari());
                                        }
                                        firstModule = false;
                                    }
                                }
                            }
                        }

                        // Im Editor geladene Module neu laden
                        if (!moduleInfosInEditor.isEmpty()) {
                            for (EditModuleForm.EditModuleInfo moduleInfo : moduleInfosInEditor) {
                                editModuleForm.reloadModule(moduleInfo);
                            }
                        }
                    } finally {
                        stopPseudoTransactionForActiveChangeSet();
                    }

                    // Wenn alle Module geladen sind am Ende zum EditModuleForm wechseln
                    getConnector().getMainWindow().displayForm(editModuleForm);
                }
            } finally {
                GuiWindow.showWaitCursorForRootWindow(false);
            }
        }
    }

    private void saveEditModules(final List<EditModuleForm.EditModuleInfo> moduleInfosInEditorToSave) {
        if (!moduleInfosInEditorToSave.isEmpty()) {
            EditModuleForm editModuleForm = EditTransferToASHelper.getEditModuleForm(getConnector());
            if (editModuleForm != null) {
                for (EditModuleForm.EditModuleInfo editModuleInfo : moduleInfosInEditorToSave) {
                    editModuleForm.saveModule(editModuleInfo);
                }
            }
        }
    }

    private Set<iPartsProductId> collectProducts(Map<String, TransferToASElement> notExistingModuleMap) {
        Set<iPartsProductId> productIdList = new HashSet<>();
        for (TransferToASElement rowContent : notExistingModuleMap.values()) {
            if (rowContent.getProduct() != null) {
                productIdList.add(rowContent.getProduct().getAsId());
            }
        }
        return productIdList;
    }


    private boolean checkLoadedEditModules(Map<String, List<TransferToASElement>> moduleMap,
                                           List<EditModuleForm.EditModuleInfo> moduleInfosInEditor,
                                           List<EditModuleForm.EditModuleInfo> moduleInfosInEditorToSave) {
        //Überprüfung, was im Editor geladen ist
        Collection<EditModuleForm.EditModuleInfo> editModuleInfoList = EditTransferToASHelper.getEditModuleInfoList(getConnector());
        for (EditModuleForm.EditModuleInfo moduleInfo : editModuleInfoList) {
            if (moduleMap.get(moduleInfo.getAssemblyId().getKVari()) != null) {
                if (moduleInfo.isModuleModified()) {
                    moduleInfosInEditorToSave.add(moduleInfo);
                }
                moduleInfosInEditor.add(moduleInfo);
            }
        }
        if (!moduleInfosInEditorToSave.isEmpty()) {
            // Text für MessageDialog erzeugen (Liste der zu speichernden Module)
            StringBuilder loadedModulesString = new StringBuilder();
            //EinPas einPas = EinPas.getInstance(project);

            for (EditModuleForm.EditModuleInfo moduleInfo : moduleInfosInEditorToSave) {
                List<TransferToASElement> transferList;
                transferList = moduleMap.get(moduleInfo.getAssemblyId().getKVari());
                if ((transferList != null) && !transferList.isEmpty()) {
                    loadedModulesString.append(OsUtils.NEWLINE);
                    loadedModulesString.append(" - ");
                    loadedModulesString.append(transferList.get(0).getAssemblyId().getKVari());
                    if (transferList.get(0).getKgTuId() != null) {
                        loadedModulesString.append(" (");
                        loadedModulesString.append(transferList.get(0).getKgTuId().toString("/"));
                        loadedModulesString.append(")");
                    }
                }
            }
            if (logHelper == null) {
                if (MessageDialog.show(TranslationHandler.translate("!!Folgende Module sind im Editor geändert und müssen gespeichert werden:") + '\n'
                                       + loadedModulesString + "\n\n" + TranslationHandler.translate("!!Fortfahren?"), "!!Module speichern",
                                       MessageDialogIcon.CONFIRMATION, MessageDialogButtons.YES, MessageDialogButtons.NO) != ModalResult.YES) {
                    return false;
                }
            }
        }

        return true;
    }

    public void collectByModules(List<TransferToASElement> allTransferItems,
                                 Map<String, List<TransferToASElement>> moduleMap,
                                 Map<String, TransferToASElement> notExistingModuleMap) {
        Map<String, DBDataObjectAttributesList> existingModules = new HashMap<>();
        for (TransferToASElement transferElement : allTransferItems) {
            String moduleNameWithoutSerial = EditModuleHelper.buildKgTuModuleNumberWithoutSerial(transferElement.getProductId(), transferElement.getKgTuId());
            DBDataObjectAttributesList attributeList = existingModules.get(moduleNameWithoutSerial);
            if (attributeList == null) {
                attributeList = EditModuleHelper.findExistingModules(transferElement.getKgTuId(), moduleNameWithoutSerial, getProject());
                existingModules.put(moduleNameWithoutSerial, attributeList);
            }

            if (attributeList.isEmpty()) {
                // neues Modul
//                String moduleName = moduleNameWithoutSerial + EditModuleHelper.IPARTS_MODULE_NAME_DELIMITER + EditModuleHelper.formatModuleSerialNumber(1);
                String moduleName = EditModuleHelper.createStandardModuleName(transferElement.getProductId(), transferElement.getKgTuId());
                notExistingModuleMap.put(moduleName, transferElement);
                List<TransferToASElement> transferList = moduleMap.get(moduleName);
                if (transferList == null) {
                    transferList = new DwList<>();
                    moduleMap.put(moduleName, transferList);
                }
                transferList.add(transferElement);
                DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                attributes.addField(iPartsConst.FIELD_DM_MODULE_NO, moduleName, DBActionOrigin.FROM_DB);
                attributeList.add(attributes);
                existingModules.put(moduleNameWithoutSerial, attributeList);
            } else {
                for (DBDataObjectAttributes attributes : attributeList) {
                    String moduleName = attributes.getField(iPartsConst.FIELD_DM_MODULE_NO).getAsString();
                    List<TransferToASElement> transferList = moduleMap.get(moduleName);
                    if (transferList == null) {
                        transferList = new DwList<>();
                        moduleMap.put(moduleName, transferList);
                    }
                    transferList.add(transferElement);
                    transferElement.setAssemblyId(new AssemblyId(moduleName, ""));
                }
            }
        }
    }

    private void collectByModulesSA(List<TransferToASElement> allTransferItems,
                                    Map<String, List<TransferToASElement>> moduleMap,
                                    Map<String, TransferToASElement> notExistingModuleMap) {
        Map<String, iPartsDataModule> checkedModules = new HashMap<>();
        for (TransferToASElement transferElement : allTransferItems) {
            String moduleName = transferElement.getSaModuleNumber();
            iPartsDataModule dataModule = checkedModules.get(moduleName);
            if (dataModule == null) {
                dataModule = new iPartsDataModule(getProject(), new iPartsModuleId(moduleName));
                checkedModules.put(moduleName, dataModule);
            }
            moduleMap.putIfAbsent(moduleName, new DwList<>());
            List<TransferToASElement> elementsForModule = moduleMap.get(moduleName);
            elementsForModule.add(transferElement);
            if (!dataModule.existsInDB()) {
                notExistingModuleMap.put(moduleName, transferElement);
            }
        }
    }

    private boolean checkNewModulesIfExists(Map<String, TransferToASElement> notExistingModuleMap,
                                            Map<String, List<TransferToASElement>> moduleMap) {
        if (!notExistingModuleMap.isEmpty()) {
            // Text für MessageDialog erzeugen (Liste der zu erzeugenden Module, die bereits existieren)
            StringBuilder newModulesString = new StringBuilder();
            List<String> moduleNames = new DwList<>();
            for (Map.Entry<String, TransferToASElement> entry : notExistingModuleMap.entrySet()) {
                TransferToASElement transferElement = entry.getValue();
                boolean moduleReserved = false;
                List<String> errors = new DwList<>();
                if (transferElement.getProductId() != null) {
                    iPartsProductId productId = transferElement.getProduct().getAsId();
                    KgTuId kgTuId = transferElement.getKgTuId();
                    moduleReserved = EditModuleHelper.isStandardModuleInReservedPK(getProject(), productId, kgTuId, false, errors);
                } else if (transferElement.getSaModuleNumber() != null) {
                    AssemblyId assemblyId = new AssemblyId(transferElement.getSaModuleNumber(), "");
                    moduleReserved = EditModuleHelper.isModuleInReservedPK(getProject(), false, errors, assemblyId);
                }
                if (moduleReserved) {
                    moduleNames.add(entry.getKey());
                    newModulesString.append(entry.getKey());
                    if (newModulesString.length() > 0) {
                        newModulesString.append(OsUtils.NEWLINE);
                    }
                    for (String error : errors) {
                        newModulesString.append(error);
                        newModulesString.append(OsUtils.NEWLINE);
                    }
                }
            }
            if (!moduleNames.isEmpty()) {
                String title = "!!In Autoren-Aufträgen belegte TUs";
                String header = "!!Folgende TUs können nicht automatisch angelegt werden:";
                if (moduleNames.size() == 1) {
                    title = "!!Im Autoren-Auftrag belegter TU";
                    header = "!!Folgender TU kann nicht automatisch angelegt werden:";
                }
                String msg = TranslationHandler.translate(header) +
                             OsUtils.NEWLINE + OsUtils.NEWLINE + newModulesString;
                if (moduleNames.size() == moduleMap.size()) {
                    if (logHelper == null) {
                        MessageDialog.showError(msg, title);
                    } else {
                        logHelper.addLogError(logHelper.translateForLog(header) + newModulesString);
                    }
                    return false;
                } else {
                    if (logHelper == null) {
                        String question = "!!Trotzdem die restlichen Einträge übernehmen?";
                        if ((moduleMap.size() - moduleNames.size()) == 1) {
                            question = "!!Trotzdem den restlichen Eintrag übernehmen?";
                        }
                        msg += OsUtils.NEWLINE + OsUtils.NEWLINE + TranslationHandler.translate(question);
                        if (MessageDialog.showYesNo(msg, title) == ModalResult.NO) {
                            return false;
                        }
                    } else {
                        logHelper.addLogMsg(logHelper.translateForLog(header) + newModulesString);
                    }
                    for (String moduleName : moduleNames) {
                        moduleMap.remove(moduleName);
                        notExistingModuleMap.remove(moduleName);
                    }
                    return true;
                }
            }
        }
        return true;
    }

    private void enableButtons() {
        if (grid != null) {
            List<RowContentForTransferToAS> transferItems = grid.getAllVisibleTransferItems();
            if (!transferItems.isEmpty()) {
                mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, true);
            } else {
                mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, false);
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel centerPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel contentTablePanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelSouth;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxShowModule;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelInfo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setPaddingLeft(8);
            title.setPaddingRight(8);
            title.setTitle("!!In AS-Stückliste übernehmen");
            title.setSubtitle("Untertitel (hier evtl. die BR, AA Infos eintragen) (mehrzeilig)");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            centerPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            centerPanel.setName("centerPanel");
            centerPanel.__internal_setGenerationDpi(96);
            centerPanel.registerTranslationHandler(translationHandler);
            centerPanel.setScaleForResolution(true);
            centerPanel.setMinimumWidth(10);
            centerPanel.setMinimumHeight(10);
            centerPanel.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder centerPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            centerPanel.setLayout(centerPanelLayout);
            contentTablePanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            contentTablePanel.setName("contentTablePanel");
            contentTablePanel.__internal_setGenerationDpi(96);
            contentTablePanel.registerTranslationHandler(translationHandler);
            contentTablePanel.setScaleForResolution(true);
            contentTablePanel.setMinimumWidth(10);
            contentTablePanel.setMinimumHeight(10);
            contentTablePanel.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder contentTablePanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            contentTablePanel.setLayout(contentTablePanelLayout);
            panelSouth = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelSouth.setName("panelSouth");
            panelSouth.__internal_setGenerationDpi(96);
            panelSouth.registerTranslationHandler(translationHandler);
            panelSouth.setScaleForResolution(true);
            panelSouth.setMinimumWidth(10);
            panelSouth.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelSouthLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelSouth.setLayout(panelSouthLayout);
            checkboxShowModule = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxShowModule.setName("checkboxShowModule");
            checkboxShowModule.__internal_setGenerationDpi(96);
            checkboxShowModule.registerTranslationHandler(translationHandler);
            checkboxShowModule.setScaleForResolution(true);
            checkboxShowModule.setMinimumWidth(10);
            checkboxShowModule.setMinimumHeight(10);
            checkboxShowModule.setPaddingTop(4);
            checkboxShowModule.setText("!!Nach dem Speichern Module zur Bearbeitung öffnen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder checkboxShowModuleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            checkboxShowModuleConstraints.setPosition("east");
            checkboxShowModule.setConstraints(checkboxShowModuleConstraints);
            panelSouth.addChild(checkboxShowModule);
            labelInfo = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelInfo.setName("labelInfo");
            labelInfo.__internal_setGenerationDpi(96);
            labelInfo.registerTranslationHandler(translationHandler);
            labelInfo.setScaleForResolution(true);
            labelInfo.setMinimumWidth(10);
            labelInfo.setMinimumHeight(10);
            labelInfo.setPaddingTop(4);
            labelInfo.setText("!!* Standard KG/TU-Benennung (bisher keine Verwendung im Produkt)");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelInfoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelInfoConstraints.setPosition("west");
            labelInfo.setConstraints(labelInfoConstraints);
            panelSouth.addChild(labelInfo);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelSouthConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelSouthConstraints.setPosition("south");
            panelSouth.setConstraints(panelSouthConstraints);
            contentTablePanel.addChild(panelSouth);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder contentTablePanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            contentTablePanel.setConstraints(contentTablePanelConstraints);
            centerPanel.addChild(contentTablePanel);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder centerPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            centerPanel.setConstraints(centerPanelConstraints);
            this.addChild(centerPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onOKbuttonClick(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}