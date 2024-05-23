/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolButton;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.collections.dwlist.DwList;

import java.awt.*;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Formular, um aus der Stückliste einen oder mehrere Einträge aus zu wählen
 */
public class EditSelectPartlistEntryForm extends AbstractJavaViewerForm {

    protected EditDataObjectGrid selectedDataGrid;
    protected EditDataObjectFilterGrid actualDataGrid;

    protected EditToolbarButtonMenuHelper toolbarHelper;

    protected EtkDataPartListEntry currentPartListEntry; // der Stücklisteneintrag von dem die Related Info geöffnet wurde
    protected List<EtkDataPartListEntry> allPartListEntries; // alle zur Auswahl stehenden Stücklisteneinträge
    protected Set<PartListEntryId> selectedIds;
    protected List<EtkDataPartListEntry> initialSelection; // bei Dialogstart bereits als selektiert markierte Einträge
    protected List<EtkDataPartListEntry> editedSelection; // während der Bearbeitung selektierte Einträge

    // Parameter für das Dialog Verhalten
    protected boolean isEditAllowed = true;

    protected boolean isMultiselect = true;
    protected GuiMenuItem applyMenuItem;
    protected GuiToolButton applyToolbarButton;

    /**
     * Erzeugt eine Instanz von EditSelectPartlistEntryForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector                  Edit Connector über den u.a. die Stückliste übertragen wird
     * @param parentForm
     * @param partListEntry
     * @param initialSelectedPartlistEntries Welche Einträge sollen bei Dialogstart bereits ausgewählt werden, {@code null} für leer
     * @param displayFieldsTop               wenn {@code null} werden die Default Felder der Stückliste über den Connector ermittelt
     * @param displayFieldsBottom            wenn {@code null} werden die Default Felder der Stückliste über den Connector ermittelt
     * @param multiselect                    Kann nur ein Eintrag oder mehrere ausgewählt werden
     * @param includeOriginalEntry           Soll der PartlistEntry von dem die Related Info geöffnet wurde in der Auswahlliste enthalten sein
     */
    public EditSelectPartlistEntryForm(EditModuleFormIConnector dataConnector,
                                       AbstractJavaViewerForm parentForm,
                                       EtkDataPartListEntry partListEntry,
                                       List<EtkDataPartListEntry> initialSelectedPartlistEntries,
                                       EtkDisplayFields displayFieldsTop, EtkDisplayFields displayFieldsBottom,
                                       boolean multiselect, boolean includeOriginalEntry) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();

        isMultiselect = multiselect;
        if (isMultiselect) {
            mainWindow.panelTop.setTitle("!!Selektierte Einträge");
        } else {
            mainWindow.panelTop.setTitle("!!Selektierter Eintrag");
            selectedDataGrid.setMultiSelect(false);
            actualDataGrid.setMultiSelect(false);
        }
        currentPartListEntry = partListEntry;

        if (displayFieldsTop == null) {
            displayFieldsTop = getConnector().getAssemblyListDisplayFields();
        }
        if (displayFieldsBottom == null) {
            displayFieldsBottom = getConnector().getAssemblyListDisplayFields();
        }
        selectedDataGrid.setDisplayFields(displayFieldsTop);
        actualDataGrid.setDisplayFields(displayFieldsBottom);

        initSelectionLists(initialSelectedPartlistEntries, includeOriginalEntry);
        dataToGrid();

        if (parentForm != null) {
            int height = parentForm.getGui().getParentWindow().getHeight();
            int width = parentForm.getGui().getParentWindow().getWidth();
            mainWindow.setSize(width - iPartsConst.CASCADING_WINDOW_OFFSET_WIDTH, height - iPartsConst.CASCADING_WINDOW_OFFSET_HEIGHT);
        }
    }

    protected void setupGrids() {
        selectedDataGrid = new EditDataObjectGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                refreshToolbar(this);
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doRemove(event);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                GuiMenuItem menuItem = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.IMG_DOWN, "!!Entfernen",
                                                                                 getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
                            @Override
                            public void fire(Event event) {
                                doRemove(event);
                            }
                        });
                contextMenu.addChild(menuItem);
                addFurtherContextMenusToSelectedGrid(contextMenu, getToolbarHelper());
            }
        };

        actualDataGrid = new EditDataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                actualDataGridSelectionChanged();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                if ((applyToolbarButton == null) || applyToolbarButton.isEnabled()) {
                    doAdd(event);
                }
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                applyMenuItem = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.IMG_UP, "!!Übernehmen",
                                                                          getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
                            @Override
                            public void fire(Event event) {
                                doAdd(event);
                            }
                        });
                contextMenu.addChild(applyMenuItem);
                addFurtherContextMenusToActualGrid(contextMenu, getToolbarHelper());
            }
        };
    }

    protected void actualDataGridSelectionChanged() {
        refreshToolbar(actualDataGrid);
    }

    /**
     * Fügt zusätzliche Kontextmenüeinträge zum Grid mit den wählbaren Stücklistenpositionen hinzu
     *
     * @param contextMenu
     * @param toolbarHelper
     */
    protected void addFurtherContextMenusToActualGrid(GuiContextMenu contextMenu, EditToolbarButtonMenuHelper toolbarHelper) {

    }

    /**
     * Fügt zusätzliche Kontextmenüeinträge zum Grid mit den ausgewählten Stücklistenpositionen
     *
     * @param contextMenu
     * @param toolbarHelper
     */
    protected void addFurtherContextMenusToSelectedGrid(GuiContextMenu contextMenu, EditToolbarButtonMenuHelper toolbarHelper) {

    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz erzeugt wurde.
     */
    protected void postCreateGui() {
        setupGrids();
        toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbar);

        selectedDataGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
        selectedDataGrid.showToolbar(false);

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        selectedDataGrid.getGui().setConstraints(constraints);
        mainWindow.panelCurrentSelection.addChild(selectedDataGrid.getGui());

        actualDataGrid.showToolbar(false);

        constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        actualDataGrid.getGui().setConstraints(constraints);
        mainWindow.panelActualEntries.addChild(actualDataGrid.getGui());

        createToolbarButtons();
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public void setName(String name) {
        mainWindow.setName(name);
    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    protected void hideTopPanel() {
        mainWindow.splitpane.setDividerPosition(0);
        mainWindow.splitpane.setDividerSize(0, true);
    }

    protected void setToolbarVisible(boolean visible) {
        mainWindow.toolbar.setVisible(visible);
    }

    protected void setPanelTitle(boolean top, String title) {
        if (top) {
            mainWindow.panelCurrentSelection.setTitle(title);
        } else {
            mainWindow.panelActualEntries.setTitle(title);
        }
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    /**
     * Überträgt die Stückliste in die interne Struktur.
     *
     * @param initialSelectedPartlistEntries
     * @param includeOriginalEntry
     */
    protected void initSelectionLists(List<EtkDataPartListEntry> initialSelectedPartlistEntries, boolean includeOriginalEntry) {

        Set<PartListEntryId> initialSelectedIds = new HashSet<>();
        if (initialSelectedPartlistEntries != null) {
            for (EtkDataPartListEntry initialSelectedPartlistEntry : initialSelectedPartlistEntries) {
                initialSelectedIds.add(initialSelectedPartlistEntry.getAsId());
            }
        }

        List<EtkDataPartListEntry> list = getCurrentPartListEntries();
        initialSelection = new DwList<>(initialSelectedPartlistEntries);
        allPartListEntries = new DwList<>();
        selectedIds = new LinkedHashSet<>();
        for (EtkDataPartListEntry partListEntry : list) {
            if (!includeOriginalEntry && partListEntry.getAsId().equals(currentPartListEntry.getAsId())) {
                continue;
            }
            if (initialSelectedIds.contains(partListEntry.getAsId())) {
                selectedIds.add(partListEntry.getAsId());
            }
            allPartListEntries.add(partListEntry);
        }
        editedSelection = new DwList<>(initialSelection);
    }

    protected List<EtkDataPartListEntry> getCurrentPartListEntries() {
        return getConnector().getCurrentPartListEntries();
    }

    /**
     * Überträgt die Stückliste und die aktuelle Auswahl in beide Grids
     */
    protected void dataToGrid() {
        selectedDataGrid.clearGrid();
        actualDataGrid.clearGrid();
        fillGrids();
        actualDataGrid.showNoResultsLabel(actualDataGrid.getTable().getRowCount() == 0);
        selectedDataGrid.showNoResultsLabel(selectedDataGrid.getTable().getRowCount() == 0);
        actualDataGrid.updateFilters();
        selectedDataGrid.updateFilters();
        refreshToolbar(null);
        refreshOKButton();
    }

    protected void fillGrids() {
        for (EtkDataPartListEntry partListEntry : allPartListEntries) {
            boolean oldLogLoadFieldIfNeeded = partListEntry.isLogLoadFieldIfNeeded();
            partListEntry.setLogLoadFieldIfNeeded(false);
            try {
                if (selectedIds.contains(partListEntry.getAsId())) {
                    selectedDataGrid.addObjectToGrid(partListEntry);
                } else {
                    actualDataGrid.addObjectToGrid(partListEntry);
                }
            } finally {
                partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
        }
    }

    protected void createToolbarButtons() {
        applyToolbarButton = toolbarHelper.addToolbarButton(EditToolbarButtonAlias.IMG_UP, "!!Übernehmen", new EventListener(de.docware.framework.modules.gui.event.Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                doAdd(event);
            }
        });
        toolbarHelper.addToolbarButton(EditToolbarButtonAlias.IMG_DOWN, "!!Entfernen", new EventListener(de.docware.framework.modules.gui.event.Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                doRemove(event);
            }
        });
        refreshToolbar(null);
    }

    protected void refreshToolbar(DataObjectGrid sender) {
        boolean upEnabled = false;
        boolean downEnabled = false;
        if ((sender != null) && isEditAllowed) {
            List<EtkDataObject> list = actualDataGrid.getSelection();
            upEnabled = ((list != null) && !list.isEmpty());
            list = selectedDataGrid.getSelection();
            downEnabled = ((list != null) && !list.isEmpty());
            if (downEnabled && (list.size() == 1)) {
                downEnabled = !list.get(0).getAsId().equals(currentPartListEntry.getAsId());
            }
        }
        toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.IMG_UP, upEnabled);
        toolbarHelper.enableToolbarButton(EditToolbarButtonAlias.IMG_DOWN, downEnabled);
    }

    protected boolean isModified() {
        boolean modified = false;
        if (isEditAllowed) {
            List<EtkDataPartListEntry> selectedList = getSelectedPartListEntries();
            if (selectedList.size() != initialSelection.size()) {
                modified = true;
            } else {
                if (selectedList.isEmpty() && initialSelection.isEmpty()) {
                    modified = false;
                } else {
                    modified = !containsAll(selectedList);
                }
            }
        }
        return modified;
    }

    protected void setOKButtonEnabled(boolean enabled) {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, enabled);
    }

    protected void refreshOKButton() {
        setOKButtonEnabled(isModified());
    }

    protected boolean containsAll(List<EtkDataPartListEntry> selectedList) {
        List<PartListEntryId> idList = new DwList<>(selectedList.size());
        for (EtkDataPartListEntry partListEntry : selectedList) {
            idList.add(partListEntry.getAsId());
        }
        for (EtkDataPartListEntry partListEntry : initialSelection) {
            if (!idList.contains(partListEntry.getAsId())) {
                return false;
            }
        }
        return true;
    }

    protected List<EtkDataPartListEntry> getSelectedPartListEntries() {
        if (editedSelection == null) {
            editedSelection = new DwList<>();
            for (EtkDataPartListEntry partListEntry : allPartListEntries) {
                if (selectedIds.contains(partListEntry.getAsId())) {
                    editedSelection.add(partListEntry);
                }
            }
        }
        return editedSelection;
    }


    protected EtkDataPartListEntry findPartListEntry(EtkDataPartListEntry partListEntry) {
        if (partListEntry.getAsId().equals(currentPartListEntry.getAsId())) {
            selectedIds.add(currentPartListEntry.getAsId());
            return currentPartListEntry;
        }
        for (EtkDataPartListEntry entry : allPartListEntries) {
            if (entry.getAsId().equals(partListEntry.getAsId())) {
                return partListEntry;
            }
        }
        return null;
    }

    /**
     * Ändert den Status der übergeben Tabellenzeilen entweder zu selected oder deselected
     *
     * @param highlightedRows gewählte Tabellenzeilen (multiselect)
     * @param isSelected      {@code true} wenn die Auswahl selektiert (oben hinzugefügt) oder {@code false} deselektiert (unten hinzugefügt) werden soll
     */
    protected void modifySelection(List<List<EtkDataObject>> highlightedRows, boolean isSelected) {
        if (!isMultiselect && !getSelectedPartListEntries().isEmpty()) {
            // wenn keine mehrfachselektion erlaubt ist müssen alle bisher selektierten Einträge wieder deselektiert werden
            for (EtkDataPartListEntry selectedEntry : getSelectedPartListEntries()) {
                EtkDataPartListEntry partListEntry = findPartListEntry(selectedEntry);
                if (partListEntry != null) {
                    selectedIds.remove(partListEntry.getAsId());
                }
            }
        }
        editedSelection = null; // wichtig damit bei getSelected.. die selektierten Einträge neu ermittelt werden
        for (List<EtkDataObject> dataObjectList : highlightedRows) {
            for (EtkDataObject dataObject : dataObjectList) {
                EtkDataPartListEntry partListEntry = findPartListEntry((EtkDataPartListEntry)dataObject);
                if (partListEntry != null) {
                    if (isSelected) {
                        selectedIds.add(partListEntry.getAsId());
                    } else {
                        selectedIds.remove(partListEntry.getAsId());
                    }
                }
            }
        }
        dataToGrid();
    }

    protected boolean buildTransferData() {
        if (isModified()) {
            getSelectedPartListEntries();
            return true;
        }
        return false;
    }

    protected void doAdd(Event event) {
        if (isEditAllowed) {
            modifySelection(actualDataGrid.getMultiSelection(), true);
        }
    }

    protected void doRemove(Event event) {
        if (isEditAllowed) {
            modifySelection(selectedDataGrid.getMultiSelection(), false);
        }
    }

    private void buttonOKClicked(Event event) {
        if (isEditAllowed) {
            if (buildTransferData()) {
                mainWindow.setModalResult(ModalResult.OK);
                close();
            }
        } else {
            mainWindow.setModalResult(ModalResult.CANCEL);
            close();
        }
    }

    protected void closeWithModalResult(ModalResult modalResult) {
        mainWindow.setModalResult(modalResult);
        close();
    }

    /**
     * Setzt die Fenstergröße des Forms auf screensize - offset
     *
     * @param widthOffset
     * @param heightOffset
     */
    protected void scaleWindowOffset(int widthOffset, int heightOffset) {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        mainWindow.setSize(screenSize.width - widthOffset, screenSize.height - heightOffset);
    }

    protected GuiPanel getPanelTop() {
        return mainWindow.panelTop;
    }

    protected void setDividerPos(int pos) {
        mainWindow.splitpane.setDividerPosition(pos);
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelCurrentSelection;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMovement;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbar;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelActualEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

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
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitpane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpane.setName("splitpane");
            splitpane.__internal_setGenerationDpi(96);
            splitpane.registerTranslationHandler(translationHandler);
            splitpane.setScaleForResolution(true);
            splitpane.setMinimumWidth(10);
            splitpane.setMinimumHeight(10);
            splitpane.setHorizontal(false);
            splitpane.setDividerPosition(200);
            splitpane_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_firstChild.setName("splitpane_firstChild");
            splitpane_firstChild.__internal_setGenerationDpi(96);
            splitpane_firstChild.registerTranslationHandler(translationHandler);
            splitpane_firstChild.setScaleForResolution(true);
            splitpane_firstChild.setMinimumWidth(0);
            splitpane_firstChild.setMinimumHeight(0);
            splitpane_firstChild.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_firstChild.setLayout(splitpane_firstChildLayout);
            panelTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTop.setName("panelTop");
            panelTop.__internal_setGenerationDpi(96);
            panelTop.registerTranslationHandler(translationHandler);
            panelTop.setScaleForResolution(true);
            panelTop.setMinimumWidth(10);
            panelTop.setMinimumHeight(10);
            panelTop.setTitle("!!Selektierte Einträge");
            de.docware.framework.modules.gui.layout.LayoutBorder panelTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelTop.setLayout(panelTopLayout);
            panelCurrentSelection = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelCurrentSelection.setName("panelCurrentSelection");
            panelCurrentSelection.__internal_setGenerationDpi(96);
            panelCurrentSelection.registerTranslationHandler(translationHandler);
            panelCurrentSelection.setScaleForResolution(true);
            panelCurrentSelection.setMinimumWidth(500);
            panelCurrentSelection.setMinimumHeight(70);
            panelCurrentSelection.setPaddingTop(4);
            panelCurrentSelection.setPaddingLeft(8);
            panelCurrentSelection.setPaddingRight(8);
            panelCurrentSelection.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelCurrentSelectionLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelCurrentSelection.setLayout(panelCurrentSelectionLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelCurrentSelectionConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelCurrentSelection.setConstraints(panelCurrentSelectionConstraints);
            panelTop.addChild(panelCurrentSelection);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelTop.setConstraints(panelTopConstraints);
            splitpane_firstChild.addChild(panelTop);
            splitpane.addChild(splitpane_firstChild);
            splitpane_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_secondChild.setName("splitpane_secondChild");
            splitpane_secondChild.__internal_setGenerationDpi(96);
            splitpane_secondChild.registerTranslationHandler(translationHandler);
            splitpane_secondChild.setScaleForResolution(true);
            splitpane_secondChild.setMinimumWidth(0);
            splitpane_secondChild.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_secondChild.setLayout(splitpane_secondChildLayout);
            panelBottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelBottom.setName("panelBottom");
            panelBottom.__internal_setGenerationDpi(96);
            panelBottom.registerTranslationHandler(translationHandler);
            panelBottom.setScaleForResolution(true);
            panelBottom.setMinimumWidth(10);
            panelBottom.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelBottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelBottom.setLayout(panelBottomLayout);
            panelMovement = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMovement.setName("panelMovement");
            panelMovement.__internal_setGenerationDpi(96);
            panelMovement.registerTranslationHandler(translationHandler);
            panelMovement.setScaleForResolution(true);
            panelMovement.setMinimumWidth(10);
            panelMovement.setMinimumHeight(10);
            panelMovement.setBorderWidth(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMovementLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMovement.setLayout(panelMovementLayout);
            toolbar = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbar.setName("toolbar");
            toolbar.__internal_setGenerationDpi(96);
            toolbar.registerTranslationHandler(translationHandler);
            toolbar.setScaleForResolution(true);
            toolbar.setMinimumWidth(10);
            toolbar.setMinimumHeight(10);
            toolbar.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbar.setConstraints(toolbarConstraints);
            panelMovement.addChild(toolbar);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMovementConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMovementConstraints.setPosition("north");
            panelMovement.setConstraints(panelMovementConstraints);
            panelBottom.addChild(panelMovement);
            panelActualEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelActualEntries.setName("panelActualEntries");
            panelActualEntries.__internal_setGenerationDpi(96);
            panelActualEntries.registerTranslationHandler(translationHandler);
            panelActualEntries.setScaleForResolution(true);
            panelActualEntries.setMinimumWidth(10);
            panelActualEntries.setMinimumHeight(10);
            panelActualEntries.setBorderWidth(8);
            panelActualEntries.setPaddingTop(4);
            panelActualEntries.setPaddingLeft(8);
            panelActualEntries.setPaddingRight(8);
            panelActualEntries.setPaddingBottom(4);
            panelActualEntries.setTitle("!!Aktuelle Einträge");
            de.docware.framework.modules.gui.layout.LayoutBorder panelActualEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelActualEntries.setLayout(panelActualEntriesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelActualEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelActualEntries.setConstraints(panelActualEntriesConstraints);
            panelBottom.addChild(panelActualEntries);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelBottomConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelBottom.setConstraints(panelBottomConstraints);
            splitpane_secondChild.addChild(panelBottom);
            splitpane.addChild(splitpane_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpane.setConstraints(splitpaneConstraints);
            panelMain.addChild(splitpane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    buttonOKClicked(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}