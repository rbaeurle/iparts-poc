/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.interfaces.OnMoveDataObjectsEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditDataObjectCustomFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Basis-Formular für die Anzeige und Auswahl von {@link EtkDataObject}s
 */
public abstract class EditSelectDataObjectsForm extends AbstractJavaViewerForm implements iPartsConst {

    protected ToolbarButtonMenuHelper toolbarHelperSelected;
    private GuiContextMenu contextMenuAvailableEntries;
    protected EditDataObjectCustomFilterGrid availableEntriesGrid;
    protected EditDataObjectCustomFilterGrid selectedEntriesGrid;
    protected boolean isEditAllowed;
    protected String searchTable;
    protected String selectedTableName;
    protected String configKeyForAvailableEntriesDisplayFields;
    protected String configKeyForSelectedEntriesDisplayFields;
    protected List<EtkDataObject> originalList;
    protected Set<IdWithType> deletedEntries;
    protected boolean moveEntriesVisible;
    protected boolean withDeleteEntry;
    protected boolean noDoubles;
    protected boolean withSetSelection;
    protected boolean fireTableSelectionEventBySetSelection;
    protected OnMoveDataObjectsEvent onMoveDataObjectsEvent;

    /**
     * Erzeugt eine Instanz von EditSelectDataObjectsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param searchTable
     */
    public EditSelectDataObjectsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                     String searchTable, String configKeyForAvailableEntriesDisplayFields, String configKeyForSelectedEntriesDisplayFields) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.searchTable = searchTable;
        this.selectedTableName = null;
        this.configKeyForAvailableEntriesDisplayFields = configKeyForAvailableEntriesDisplayFields;
        this.configKeyForSelectedEntriesDisplayFields = configKeyForSelectedEntriesDisplayFields;
        this.isEditAllowed = true;
        this.contextMenuAvailableEntries = new GuiContextMenu();
        this.originalList = new DwList<EtkDataObject>();
        this.deletedEntries = new HashSet<IdWithType>();
        this.withDeleteEntry = false;
        this.noDoubles = false;
        this.withSetSelection = false;
        this.fireTableSelectionEventBySetSelection = false;
        this.onMoveDataObjectsEvent = null;

        postCreateGui();

        // Maximal verfügbare Größe verwenden
        Dimension screenSize = FrameworkUtils.getScreenSize();
        setSize(screenSize.width - 20, screenSize.height - 20);

        setMoveEntriesVisible(true);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        toolbarHelperSelected = new EditToolbarButtonMenuHelper(getConnector(), mainWindow.toolbarSelectedEntries);
        createToolbarButtons();

        availableEntriesGrid = new EditDataObjectCustomFilterGrid(getConnector(), parentForm) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                doSelectionChangedAvailableGrid(event);
                doEnableButtons();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doCellDblClickedAvailableGrid(event);
                doAdd(event);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                if (!contextMenu.getChildren().isEmpty()) {
                    contextMenu.addChild(getToolbarHelper().createMenuSeparator("availableEntriesSeparator", getUITranslationHandler()));
                }
                List<AbstractGuiControl> menuList = new DwList<AbstractGuiControl>(EditSelectDataObjectsForm.this.contextMenuAvailableEntries.getChildren());
                for (AbstractGuiControl menu : menuList) {
                    menu.removeFromParent();
                    contextMenu.addChild(menu);
                }
                EditSelectDataObjectsForm.this.contextMenuAvailableEntries = contextMenu;
            }

            @Override
            protected AbstractGuiControl createCellContent(String tableName, String fieldName, EtkDataObject objectForTable, String value) {
                AbstractGuiControl control = createCellContentAvailable(tableName, fieldName, objectForTable, value);
                if (control != null) {
                    return control;
                }
                return super.createCellContent(tableName, fieldName, objectForTable, value);
            }

            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                String value = getVisualValueOfFieldAvailable(tableName, fieldName, objectForTable);
                if (value != null) {
                    return value;
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }
        };
        availableEntriesGrid.setDisplayFields(buildDisplayFields(false));
        availableEntriesGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelForAvailableEntries.addChild(availableEntriesGrid.getGui());

        selectedEntriesGrid = new EditDataObjectCustomFilterGrid(getConnector(), parentForm) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                doSelectionChangedSelectedGrid(event);
                doEnableButtons();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                if (doCellDblClickedSelectedGrid(event)) {
                    doRemove(event);
                }
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                if (!contextMenu.getChildren().isEmpty()) {
                    contextMenu.addChild(getToolbarHelper().createMenuSeparator("selectedEntriesSeparator", getUITranslationHandler()));
                }
                List<AbstractGuiControl> menuList = new DwList<AbstractGuiControl>(contextMenuSelectedEntries.getChildren());
                for (AbstractGuiControl menu : menuList) {
                    menu.removeFromParent();
                    contextMenu.addChild(menu);
                }
                contextMenuSelectedEntries = contextMenu;
            }

            @Override
            protected AbstractGuiControl createCellContent(String tableName, String fieldName, EtkDataObject objectForTable, String value) {
                AbstractGuiControl control = createCellContentSelected(tableName, fieldName, objectForTable, value);
                if (control != null) {
                    return control;
                }
                return super.createCellContent(tableName, fieldName, objectForTable, value);
            }

            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                String value = getVisualValueOfFieldSelected(tableName, fieldName, objectForTable);
                if (value != null) {
                    return value;
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }
        };
        selectedEntriesGrid.setDisplayFields(buildDisplayFields(true));
        selectedEntriesGrid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelForSelectedEntries.addChild(selectedEntriesGrid.getGui());
        doEnableButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    /**
     * Hier können über/unter oder rechts/links neben den beiden Grids weitere Controls eingebaut werden.
     * Layout: Border (belegt Center für SplitPane mit den beiden Grids)
     *
     * @return
     */
    public GuiPanel getPanelForFurtherElements() {
        return mainWindow.panelForFurtherElements;
    }

    /**
     * Hier können weitere Controls im Bereich der verfügbaren Einträge angelegt werden.
     * Layout: Border (belegt North für Label und Center für Grid)
     *
     * @return
     */
    public GuiPanel getSplitPanePanelForAvailableEntries() {
        return mainWindow.splitPanePanelForAvailableEntries;
    }

    /**
     * Hier können weitere Controls im Bereich der ausgewählten Einträge angelegt werden.
     * Layout: Border (belegt North für Label, West für ToolBar und Center für Grid)
     *
     * @return
     */
    public GuiPanel getSplitPanePanelForSelectedEntries() {
        return mainWindow.splitPanePanelForSelectedEntries;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setSize(int width, int height) {
        mainWindow.setSize(width, height);
        mainWindow.splitpaneEntries.setDividerPosition((width - 50) / 2);
    }

    protected int calcHeight() {
        int rowCount = Math.max(selectedEntriesGrid.getTable().getRowCount(), availableEntriesGrid.getTable().getRowCount());
        if (rowCount == 0) {
            rowCount = 3;
        } else {
            rowCount++;
        }
        if (rowCount < 5) {
            rowCount = 5;
        }
        int calculatedHeight = (selectedEntriesGrid.getTable().getHeader().getHeight()) * rowCount;
        calculatedHeight += mainWindow.labelAvailableEntries.getPreferredHeight() + 20;
        calculatedHeight += mainWindow.title.getPreferredHeight() + mainWindow.buttonpanel.getPreferredHeight() + 80;
        calculatedHeight = Math.max(calculatedHeight, mainWindow.getMinimumHeight());

        return Math.min((int)FrameworkUtils.getScreenSize().getHeight() - 20, calculatedHeight);
    }

    protected int calcWidth() {
        int headerWidth = selectedEntriesGrid.getTable().getHeader().getWidth() + availableEntriesGrid.getTable().getHeader().getWidth();
        headerWidth *= 3;
        int calculatedWidth = Math.max(headerWidth, mainWindow.getMinimumWidth());

        return Math.min((int)FrameworkUtils.getScreenSize().getWidth() - 20, calculatedWidth);
    }


    public void setName(String name) {
        mainWindow.setName(name);
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setAvailableEntriesTitle(String title) {
        mainWindow.labelAvailableEntries.setText(title);
    }

    public void setSelectedEntriesTitle(String title) {
        mainWindow.labelSelectedEntries.setText(title);
    }

    public boolean isEditAllowed() {
        return isEditAllowed;
    }

    public void setEditAllowed(boolean editAllowed) {
        if (isEditAllowed != editAllowed) {
            isEditAllowed = editAllowed;
            if (editAllowed) {
                mainWindow.toolbarSelectedEntries.setEnabled(true);
                doEnableButtons();
            } else {
                mainWindow.toolbarSelectedEntries.setEnabled(false);
            }
        }
    }

    public boolean isMoveEntriesVisible() {
        return moveEntriesVisible;
    }

    public void setMoveEntriesVisible(boolean moveEntriesVisible) {
        this.moveEntriesVisible = moveEntriesVisible;
        if (!moveEntriesVisible) {
            toolbarHelperSelected.hideToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_UP, contextMenuSelectedEntries);
            toolbarHelperSelected.hideToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_DOWN, contextMenuSelectedEntries);
        } else {
            toolbarHelperSelected.showToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_UP, contextMenuSelectedEntries);
            toolbarHelperSelected.showToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_DOWN, contextMenuSelectedEntries);
        }
    }

    public void setToolbarButtonVisible(iPartsToolbarButtonAlias alias, boolean visible) {
        if (visible) {
            toolbarHelperSelected.showToolbarButtonAndMenu(alias, contextMenuSelectedEntries);
        } else {
            toolbarHelperSelected.hideToolbarButtonAndMenu(alias, contextMenuSelectedEntries);
        }
    }

    public void enableToolbarButtonVisible(iPartsToolbarButtonAlias alias, boolean enabled) {
        toolbarHelperSelected.enableToolbarButtonAndMenu(alias, contextMenuAvailableEntries, enabled);
    }

    public void addContextMenuEntriesToSelectedEntriesGrid(List<AbstractGuiControl> menuList) {
        for (AbstractGuiControl menu : menuList) {
            menu.removeFromParent();
            contextMenuSelectedEntries.addChild(menu);
        }
    }

    public void addContextMenuEntriesToAvailableEntriesGrid(List<AbstractGuiControl> menuList) {
        for (AbstractGuiControl menu : menuList) {
            menu.removeFromParent();
            contextMenuAvailableEntries.addChild(menu);
        }
    }

    public boolean isWithDeleteEntry() {
        return withDeleteEntry;
    }

    public void setWithDeleteEntry(boolean withDeleteEntry) {
        this.withDeleteEntry = withDeleteEntry;
    }

    public boolean isNoDoubles() {
        return noDoubles;
    }

    public void setNoDoubles(boolean noDoubles) {
        this.noDoubles = noDoubles;
    }

    public boolean isWithSetSelection() {
        return withSetSelection;
    }

    public void setWithSetSelection(boolean withSetSelection) {
        this.withSetSelection = withSetSelection;
    }

    public OnMoveDataObjectsEvent getOnMoveDataObjectsEvent() {
        return onMoveDataObjectsEvent;
    }

    public void setOnMoveDataObjectsEvent(OnMoveDataObjectsEvent onMoveDataObjectsEvent) {
        this.onMoveDataObjectsEvent = onMoveDataObjectsEvent;
    }


    public void fillAvailableEntries(List<EtkDataObject> availableList) {
        fillEntries(availableEntriesGrid, availableList);
        originalList = new DwList<EtkDataObject>(availableList);
        if (selectedEntriesGrid.getTable().getRowCount() == 0) {
            deletedEntries.clear();
        }
    }

    /**
     * setzt MultiSelect im linken Grid
     *
     * @param multiSelect
     */
    public void setAvailableEntriesMultiSelect(boolean multiSelect) {
        availableEntriesGrid.setMultiSelect(multiSelect);
    }

    /**
     * lieferte den Tablenamen für das linke Grid
     *
     * @return
     */
    public String getAvailableTableName() {
        return searchTable;
    }

    /**
     * falls sich die {@link EtkDataObject}s zwischen den Grids unterscheiden,
     * kann hier der TableName für das rechte Grid gesetzt werden
     *
     * @param selectedTableName
     */
    public void setSelectedTableName(String selectedTableName) {
        this.selectedTableName = selectedTableName;
    }

    /**
     * lieferte den Tablenamen für das rechte Grid
     * (kann unterschiedlich zum linken Grid sein
     *
     * @return
     */
    private String getSelectedTableName() {
        if (selectedTableName == null) {
            return getAvailableTableName();
        }
        return selectedTableName;
    }

    protected void doSelectionChangedAvailableGrid(Event event) {

    }

    protected void doSelectionChangedSelectedGrid(Event event) {

    }

    protected boolean doCellDblClickedAvailableGrid(Event event) {
        return true;
    }

    protected boolean doCellDblClickedSelectedGrid(Event event) {
        return true;
    }

    /**
     * hier kann getVisualValueOfField des available-Grids überlagert werden
     *
     * @param tableName
     * @param fieldName
     * @param objectForTable
     * @return null: normale Ausgabe, sonst Anzeige des übergebenen Wertes
     */
    protected String getVisualValueOfFieldAvailable(String tableName, String fieldName, EtkDataObject objectForTable) {
        return null;
    }

    /**
     * hier kann createCellContentAvailable des available-Grids überlagert werden
     *
     * @param tableName
     * @param fieldName
     * @param objectForTable
     * @param value
     * @return null: normale Ausgabe, sonst Erzeugung eines eigenen AbstractGuiControls des übergebenen Wertes
     */
    protected AbstractGuiControl createCellContentAvailable(String tableName, String fieldName, EtkDataObject objectForTable, String value) {
        return null;
    }

    /**
     * hier kann getVisualValueOfField des selected-Grids überlagert werden
     *
     * @param tableName
     * @param fieldName
     * @param objectForTable
     * @return null: normale Ausgabe, sonst Anzeige des übergebenen Wertes
     */
    protected String getVisualValueOfFieldSelected(String tableName, String fieldName, EtkDataObject objectForTable) {
        return null;
    }

    /**
     * hier kann createCellContentAvailable des selected-Grids überlagert werden
     *
     * @param tableName
     * @param fieldName
     * @param objectForTable
     * @param value
     * @return null: normale Ausgabe, sonst Erzeugung eines eigenen AbstractGuiControls des übergebenen Wertes
     */
    protected AbstractGuiControl createCellContentSelected(String tableName, String fieldName, EtkDataObject objectForTable, String value) {
        return null;
    }

    protected void fillSelectedEntries(List<EtkDataObject> selectedList) {
        fillEntries(selectedEntriesGrid, selectedList);
    }

    protected void initSelectedEntries(List<EtkDataObject> selectedList) {
        if (withDeleteEntry && (selectedList != null)) {
            deletedEntries.clear();
            for (EtkDataObject dataObject : selectedList) {
                deletedEntries.add(dataObject.getAsId());
            }
        }
        sortSelectedObjects(selectedList);
        fillEntries(selectedEntriesGrid, selectedList);
    }

    private boolean isAvailableGrid(DataObjectGrid grid) {
        return grid == availableEntriesGrid;
    }

    private void fillEntries(DataObjectFilterGrid grid, List<EtkDataObject> list) {
        grid.clearGrid();
        addListToGrid(grid, list);
        doEnableButtons();
    }

    /**
     * ein einzelnes dataObject hinzufügen
     *
     * @param dataObject
     */
    protected void addEntryToAvailable(EtkDataObject dataObject) {
        availableEntriesGrid.addObjectToGrid(dataObject);
        originalList.add(dataObject);
    }

    private void addListToGrid(DataObjectFilterGrid grid, List<EtkDataObject> list) {
        if (list != null) {
            if (withDeleteEntry && isAvailableGrid(grid)) {
                for (EtkDataObject dataObject : list) {
                    if (!deletedEntries.contains(dataObject.getAsId())) {
                        grid.addObjectToGrid(dataObject);
                    }
                }
            } else {
                for (EtkDataObject dataObject : list) {
                    grid.addObjectToGrid(dataObject);
                }
            }
            grid.updateFilters();
        }
    }

    private List<EtkDataObject> getCompleteList(DataObjectGrid grid) {
        List<EtkDataObject> result = new DwList<>();
        String tableName = isAvailableGrid(grid) ? getAvailableTableName() : getSelectedTableName();
        for (int lfdNr = 0; lfdNr < grid.getTable().getRowCount(); lfdNr++) {
            GuiTableRow row = grid.getTable().getRow(lfdNr);
            if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                result.add(((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(tableName));
            }
        }
        return result;
    }

    protected List<EtkDataObject> getCompleteAvailableList() {
        return getCompleteList(availableEntriesGrid);
    }

    protected List<EtkDataObject> getCompleteSelectedList() {
        return getCompleteList(selectedEntriesGrid);
    }

    protected List<IdWithType> getCompleteSelectedIdList() {
        List<EtkDataObject> currentList = getCompleteSelectedList();
        return getListAsIdList(currentList);
    }

    protected List<IdWithType> getListAsIdList(List<EtkDataObject> currentList) {
        List<IdWithType> currentIdList = new DwList<>(currentList.size());
        for (EtkDataObject dataObject : currentList) {
            currentIdList.add(dataObject.getAsId());
        }
        return currentIdList;
    }

    protected List<EtkDataObject> getSelectedList(DataObjectGrid grid) {
        List<List<EtkDataObject>> list = grid.getMultiSelection();
        List<EtkDataObject> result = new DwList<EtkDataObject>();
        String tableName = isAvailableGrid(grid) ? getAvailableTableName() : getSelectedTableName();
        for (List<EtkDataObject> dataObjects : list) {
            for (EtkDataObject value : dataObjects) {
                EtkDataObject dataObject = value.getDataObjectByTableName(tableName, false);
                if (dataObject != null) {
                    result.add(dataObject);
                }
            }
        }
        return result;
    }

    protected void refreshSelectedEntries(List<EtkDataObject> list) {
        fillEntries(selectedEntriesGrid, list);
        doEnableButtons();
    }

    protected void doAdd(Event event) {
        doAddEntries(getSelectedList(availableEntriesGrid));
    }

    protected void doAddEntries(List<EtkDataObject> selectedList) {
        if (onMoveDataObjectsEvent != null) {
            if (!onMoveDataObjectsEvent.doBeforeAddEntries(selectedList)) {
                return;
            }
        }
        if (withDeleteEntry) {
            for (EtkDataObject dataObject : selectedList) {
                deletedEntries.add(dataObject.getAsId());
            }
            availableEntriesGrid.clearGrid();
            addListToGrid(availableEntriesGrid, originalList);
        }

        List<EtkDataObject> dataFromSelectedGrid = getCompleteSelectedList();
        List<IdWithType> selectedIDsFromSelectedGrid = selectedEntriesGrid.getSelectedObjectIds(getSelectedTableName());
        List<EtkDataObject> mergedDataFromGridAndNewData = mergeAndSortNewObjectsAndObjectsInGrid(selectedEntriesGrid, selectedList,
                                                                                                  dataFromSelectedGrid);
        selectedEntriesGrid.clear();
        addListToGrid(selectedEntriesGrid, mergedDataFromGridAndNewData);

        if (withSetSelection) {
            Set<Integer> allSelectedIndices = new TreeSet<>();
            List<IdWithType> listAsIdList = getListAsIdList(mergedDataFromGridAndNewData);
            for (IdWithType oldSelectedObjectId : selectedIDsFromSelectedGrid) {
                allSelectedIndices.add(listAsIdList.indexOf(oldSelectedObjectId));
            }
            for (EtkDataObject newSelectedObject : selectedList) {
                allSelectedIndices.add(listAsIdList.indexOf(newSelectedObject.getAsId()));
            }
            allSelectedIndices.remove(-1); // Falls eine bisherige ID nicht mehr gefunden wurde wäre -1 in allSelectedIndices enthalten
            int[] allSelectedIndicesArray = new int[allSelectedIndices.size()];
            int i = 0;
            for (Integer selectedIndex : allSelectedIndices) {
                allSelectedIndicesArray[i] = selectedIndex;
                i++;
            }
            selectedEntriesGrid.getTable().setSelectedRows(allSelectedIndicesArray, fireTableSelectionEventBySetSelection, true, true);
        }

        doEnableButtons();
        doEnableOKButton();
    }

    /**
     * Objekte im Grid selectedEntriesGrid mit neuen Objekten zusammenführen und sortieren
     *
     * @param newSelectedList
     * @param objectsInGrid
     */
    protected List<EtkDataObject> mergeAndSortNewObjectsAndObjectsInGrid(DataObjectFilterGrid grid, List<EtkDataObject> newSelectedList,
                                                                         List<EtkDataObject> objectsInGrid) {
        List<EtkDataObject> result = new ArrayList<>(newSelectedList);
        List<IdWithType> resultListAsIdList = getListAsIdList(result);
        if (noDoubles && !isAvailableGrid(grid)) {
            for (EtkDataObject selectedObjectsInGrid : objectsInGrid) {
                if (!resultListAsIdList.contains(selectedObjectsInGrid.getAsId())) {
                    result.add(selectedObjectsInGrid);
                }
            }
        } else {
            result.addAll(objectsInGrid);
        }
        sortSelectedObjects(result);
        return result;
    }

    protected void sortSelectedObjects(List<EtkDataObject> selectedObjects) {
        Comparator<EtkDataObject> sortComparator = Comparator.comparing(EtkDataObject::getAsId);
        Collections.sort(selectedObjects, sortComparator);
    }

    /**
     * Fügt der Liste mit den ausgewählten Werten das übergebene {@link EtkDataObject} als neuen Wert hinzu.
     *
     * @param selectEntry
     */
    protected void doAddSingleSelectedEntry(EtkDataObject selectEntry) {
        if (selectEntry == null) {
            return;
        }
        List<EtkDataObject> selectEntries = getCompleteSelectedList();
        selectEntries.add(selectEntry);
        doRemoveAll(null);
        doAddEntries(selectEntries);
    }

    protected void doRemove(Event event) {
        doRemoveEntries(getSelectedList(selectedEntriesGrid));
    }

    protected void doRemoveEntries(List<EtkDataObject> selectedList) {
        if (onMoveDataObjectsEvent != null) {
            if (!onMoveDataObjectsEvent.doBeforeRemoveEntries(selectedList)) {
                return;
            }
        }
        List<EtkDataObject> totalList = getCompleteSelectedList();

        if (withDeleteEntry) {
            for (EtkDataObject dataObject : selectedList) {
                deletedEntries.remove(dataObject.getAsId());
            }
            availableEntriesGrid.clearGrid();
            addListToGrid(availableEntriesGrid, originalList);
        }

        for (EtkDataObject dataObject : selectedList) {
            totalList.remove(dataObject);
        }
        refreshSelectedEntries(totalList); // ruft bereits doEnableButtons() auf

        doEnableOKButton();
    }

    protected void doAddAll(Event event) {
        GuiWindow.showWaitCursorForRootWindow(true);
        doAddEntries(getCompleteAvailableList());
        GuiWindow.showWaitCursorForRootWindow(false);
    }

    protected void doRemoveAll(Event event) {
        GuiWindow.showWaitCursorForRootWindow(true);
        doRemoveEntries(getCompleteSelectedList());
        GuiWindow.showWaitCursorForRootWindow(false);
    }

    protected void doMoveUp(Event event) {
        if (selectedEntriesGrid.getTable().getSelectedRows().size() == 1) {
            int selectedIndex = selectedEntriesGrid.getTable().getSelectedRowIndex();
            if (selectedIndex > 0) {
                List<EtkDataObject> selectedList = getCompleteSelectedList();
                EtkDataObject movedDataObject = selectedList.remove(selectedIndex);
                selectedList.add(selectedIndex - 1, movedDataObject);
                refreshSelectedEntries(selectedList);
                selectedEntriesGrid.getTable().setSelectedRow(selectedIndex - 1, false);
                doEnableOKButton();
            }
        }
    }

    protected void doMoveDown(Event event) {
        if (selectedEntriesGrid.getTable().getSelectedRows().size() == 1) {
            int selectedIndex = selectedEntriesGrid.getTable().getSelectedRowIndex();
            if ((selectedIndex + 1) < selectedEntriesGrid.getTable().getRowCount()) {
                List<EtkDataObject> selectedList = getCompleteSelectedList();
                EtkDataObject movedDataObject = selectedList.remove(selectedIndex);
                selectedList.add(selectedIndex + 1, movedDataObject);
                refreshSelectedEntries(selectedList);
                selectedEntriesGrid.getTable().setSelectedRow(selectedIndex + 1, false);
                doEnableOKButton();
            }
        }
    }

    protected void doEnableButtons() {
        if (!isEditAllowed) {
            return;
        }
        int availableEntriesSelectionCount = availableEntriesGrid.getTable().getSelectedRows().size();
        int selectedEntriesSelectionCount = selectedEntriesGrid.getTable().getSelectedRows().size();

        if (noDoubles && (availableEntriesSelectionCount > 0)) {
            List<IdWithType> currentIdList = getCompleteSelectedIdList();
            List<EtkDataObject> currentList = getSelectedList(availableEntriesGrid);
            boolean somethingNew = false;
            for (EtkDataObject dataObject : currentList) {
                if (!currentIdList.contains(dataObject.getAsId())) {
                    somethingNew = true;
                    break;
                }
            }
            if (!somethingNew) {
                availableEntriesSelectionCount = 0;
            }
        }
        toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_RIGHT, contextMenuAvailableEntries,
                                                         availableEntriesSelectionCount > 0);
        toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_LEFT, contextMenuSelectedEntries,
                                                         selectedEntriesSelectionCount > 0);
        toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_RIGHT_ALL, null,
                                                         availableEntriesGrid.getTable().getRowCount() > 0);
        toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_LEFT_ALL, null,
                                                         selectedEntriesGrid.getTable().getRowCount() > 0);

        int itemCount = selectedEntriesGrid.getTable().getRowCount();
        if ((itemCount > 1) && (selectedEntriesSelectionCount == 1)) {
            int selectedIndex = selectedEntriesGrid.getTable().getSelectedRowIndex();
            toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_UP, contextMenuSelectedEntries,
                                                             selectedIndex > 0);
            toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_DOWN, contextMenuSelectedEntries,
                                                             (selectedIndex + 1) < itemCount);
        } else {
            toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_UP, contextMenuSelectedEntries, false);
            toolbarHelperSelected.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.IMG_DOWN, contextMenuSelectedEntries, false);
        }
        availableEntriesGrid.showNoResultsLabel(availableEntriesGrid.getTable().getRowCount() <= 0);
        selectedEntriesGrid.showNoResultsLabel(selectedEntriesGrid.getTable().getRowCount() <= 0);
    }

    protected void doEnableOKButton() {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, areEntriesChanged());
    }

    protected void showOnlySelectedElements() {
        if (mainWindow.splitpaneEntries.getParent() != null) {
            mainWindow.splitpaneEntries.removeFromParent();
            mainWindow.splitPanePanelForSelectedEntries.removeFromParent();
            mainWindow.panelForFurtherElements.addChildBorderCenter(mainWindow.splitPanePanelForSelectedEntries);
        }
    }

    protected GuiContextMenu getContextMenuSelectedEntries() {
        return contextMenuSelectedEntries;
    }

    protected boolean areEntriesChanged() {
        return true;
    }

    protected void createToolbarButtons() {
        ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
        holder = toolbarHelperSelected.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.IMG_RIGHT, "!!Übernehmen",
                                                                     getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doAdd(event);
                    }
                });
        contextMenuAvailableEntries.addChild(holder.menuItem);

        holder = toolbarHelperSelected.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.IMG_LEFT, "!!Entfernen",
                                                                     getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doRemove(event);
                    }
                });
        contextMenuSelectedEntries.addChild(holder.menuItem);

        toolbarHelperSelected.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.IMG_RIGHT_ALL, "!!Alle übernehmen",
                                                            getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doAddAll(event);
                    }
                });

        toolbarHelperSelected.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.IMG_LEFT_ALL, "!!Alle entfernen",
                                                            getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doRemoveAll(event);
                    }
                });

        holder = toolbarHelperSelected.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.IMG_UP,
                                                                     getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doMoveUp(event);
                    }
                });
        contextMenuSelectedEntries.addChild(holder.menuItem);

        holder = toolbarHelperSelected.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.IMG_DOWN,
                                                                     getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doMoveDown(event);
                    }
                });
        contextMenuSelectedEntries.addChild(holder.menuItem);
    }

    /**
     * Erstellt die benötigten Anzeigefelder für ein Grid.
     *
     * @param forSelectedEntries Sollen die Anzeigefelder für das Grid mit den ausgewählten Einträgen erstellt werden?
     *                           Bei {@code false} werden die Anzeigefelder für das Grid mit den verfügbaren Einträgen erstellt
     * @return
     */
    protected EtkDisplayFields buildDisplayFields(boolean forSelectedEntries) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(getConfig(), forSelectedEntries ? configKeyForSelectedEntriesDisplayFields : configKeyForAvailableEntriesDisplayFields);
        if (displayFields.size() == 0) {
            displayFields = buildDefaultDisplayFields(forSelectedEntries);
        }

        // Bei den ausgewählten Einträgen Filterung unterbinden
        if (forSelectedEntries) {
            for (EtkDisplayField displayField : displayFields.getFields()) {
                displayField.setColumnFilterEnabled(false);
            }
        }

        return displayFields;
    }

    /**
     * Erstellt die benötigten Standard-Anzeigefelder für ein Grid.
     *
     * @param forSelectedEntries Sollen die Standard-Anzeigefelder für das Grid mit den ausgewählten Einträgen erstellt werden?
     *                           Bei {@code false} werden die Standard-Anzeigefelder für das Grid mit den verfügbaren Einträgen erstellt
     * @return
     */
    protected abstract EtkDisplayFields buildDefaultDisplayFields(boolean forSelectedEntries);

    /**
     * Erstellt ein Anzeigefeld für den übergebenen Feldnamen.
     *
     * @param tableDef
     * @param tableName
     * @param fieldName
     * @return
     */
    protected EtkDisplayField createDisplayField(EtkDatabaseTable tableDef, String tableName, String fieldName) {
        EtkDatabaseField dbField = tableDef.getField(fieldName);
        return new EtkDisplayField(TableAndFieldName.make(tableName, dbField.getName()), dbField.isMultiLanguage(), dbField.isArray());
    }

    /**
     * Erstellt ein Anzeigefeld für den übergebenen Feldnamen.
     *
     * @param tableDef
     * @param fieldName
     * @return
     */
    protected EtkDisplayField createDisplayField(EtkDatabaseTable tableDef, String fieldName) {
        return createDisplayField(tableDef, getAvailableTableName(), fieldName);
    }


    protected GuiButtonPanel getButtonPanel() {
        return mainWindow.buttonpanel;
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextMenuSelectedEntries;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelForFurtherElements;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitPanePanelForAvailableEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelAvailableEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelForAvailableEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitPanePanelForSelectedEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelSelectedEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbarSelectedEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelForSelectedEntries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextMenuSelectedEntries = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextMenuSelectedEntries.setName("contextMenuSelectedEntries");
            contextMenuSelectedEntries.__internal_setGenerationDpi(96);
            contextMenuSelectedEntries.registerTranslationHandler(translationHandler);
            contextMenuSelectedEntries.setScaleForResolution(true);
            contextMenuSelectedEntries.setMinimumWidth(10);
            contextMenuSelectedEntries.setMinimumHeight(10);
            contextMenuSelectedEntries.setMenuName("contextMenuSelectedEntries");
            contextMenuSelectedEntries.setParentControl(this);
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(1000);
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
            panelMain.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            panelForFurtherElements = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelForFurtherElements.setName("panelForFurtherElements");
            panelForFurtherElements.__internal_setGenerationDpi(96);
            panelForFurtherElements.registerTranslationHandler(translationHandler);
            panelForFurtherElements.setScaleForResolution(true);
            panelForFurtherElements.setMinimumWidth(10);
            panelForFurtherElements.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelForFurtherElementsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelForFurtherElements.setLayout(panelForFurtherElementsLayout);
            splitpaneEntries = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneEntries.setName("splitpaneEntries");
            splitpaneEntries.__internal_setGenerationDpi(96);
            splitpaneEntries.registerTranslationHandler(translationHandler);
            splitpaneEntries.setScaleForResolution(true);
            splitpaneEntries.setMinimumWidth(10);
            splitpaneEntries.setMinimumHeight(10);
            splitpaneEntries.setDividerPosition(439);
            splitPanePanelForAvailableEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitPanePanelForAvailableEntries.setName("splitPanePanelForAvailableEntries");
            splitPanePanelForAvailableEntries.__internal_setGenerationDpi(96);
            splitPanePanelForAvailableEntries.registerTranslationHandler(translationHandler);
            splitPanePanelForAvailableEntries.setScaleForResolution(true);
            splitPanePanelForAvailableEntries.setMinimumWidth(0);
            splitPanePanelForAvailableEntries.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder splitPanePanelForAvailableEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitPanePanelForAvailableEntries.setLayout(splitPanePanelForAvailableEntriesLayout);
            labelAvailableEntries = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelAvailableEntries.setName("labelAvailableEntries");
            labelAvailableEntries.__internal_setGenerationDpi(96);
            labelAvailableEntries.registerTranslationHandler(translationHandler);
            labelAvailableEntries.setScaleForResolution(true);
            labelAvailableEntries.setMinimumWidth(10);
            labelAvailableEntries.setMinimumHeight(10);
            labelAvailableEntries.setPaddingBottom(4);
            labelAvailableEntries.setText("!!Verfügbar:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelAvailableEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelAvailableEntriesConstraints.setPosition("north");
            labelAvailableEntries.setConstraints(labelAvailableEntriesConstraints);
            splitPanePanelForAvailableEntries.addChild(labelAvailableEntries);
            panelForAvailableEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelForAvailableEntries.setName("panelForAvailableEntries");
            panelForAvailableEntries.__internal_setGenerationDpi(96);
            panelForAvailableEntries.registerTranslationHandler(translationHandler);
            panelForAvailableEntries.setScaleForResolution(true);
            panelForAvailableEntries.setMinimumWidth(10);
            panelForAvailableEntries.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelForAvailableEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelForAvailableEntries.setLayout(panelForAvailableEntriesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelForAvailableEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelForAvailableEntries.setConstraints(panelForAvailableEntriesConstraints);
            splitPanePanelForAvailableEntries.addChild(panelForAvailableEntries);
            splitpaneEntries.addChild(splitPanePanelForAvailableEntries);
            splitPanePanelForSelectedEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitPanePanelForSelectedEntries.setName("splitPanePanelForSelectedEntries");
            splitPanePanelForSelectedEntries.__internal_setGenerationDpi(96);
            splitPanePanelForSelectedEntries.registerTranslationHandler(translationHandler);
            splitPanePanelForSelectedEntries.setScaleForResolution(true);
            splitPanePanelForSelectedEntries.setMinimumWidth(0);
            splitPanePanelForSelectedEntries.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder splitPanePanelForSelectedEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitPanePanelForSelectedEntries.setLayout(splitPanePanelForSelectedEntriesLayout);
            labelSelectedEntries = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSelectedEntries.setName("labelSelectedEntries");
            labelSelectedEntries.__internal_setGenerationDpi(96);
            labelSelectedEntries.registerTranslationHandler(translationHandler);
            labelSelectedEntries.setScaleForResolution(true);
            labelSelectedEntries.setMinimumWidth(10);
            labelSelectedEntries.setMinimumHeight(10);
            labelSelectedEntries.setPaddingLeft(4);
            labelSelectedEntries.setPaddingBottom(4);
            labelSelectedEntries.setText("!!Ausgewählt:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelSelectedEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelSelectedEntriesConstraints.setPosition("north");
            labelSelectedEntries.setConstraints(labelSelectedEntriesConstraints);
            splitPanePanelForSelectedEntries.addChild(labelSelectedEntries);
            toolbarSelectedEntries = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbarSelectedEntries.setName("toolbarSelectedEntries");
            toolbarSelectedEntries.__internal_setGenerationDpi(96);
            toolbarSelectedEntries.registerTranslationHandler(translationHandler);
            toolbarSelectedEntries.setScaleForResolution(true);
            toolbarSelectedEntries.setMinimumWidth(20);
            toolbarSelectedEntries.setMinimumHeight(10);
            toolbarSelectedEntries.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            toolbarSelectedEntries.setButtonOrientation(de.docware.framework.modules.gui.controls.misc.DWOrientation.VERTICAL);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarSelectedEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarSelectedEntriesConstraints.setPosition("west");
            toolbarSelectedEntries.setConstraints(toolbarSelectedEntriesConstraints);
            splitPanePanelForSelectedEntries.addChild(toolbarSelectedEntries);
            panelForSelectedEntries = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelForSelectedEntries.setName("panelForSelectedEntries");
            panelForSelectedEntries.__internal_setGenerationDpi(96);
            panelForSelectedEntries.registerTranslationHandler(translationHandler);
            panelForSelectedEntries.setScaleForResolution(true);
            panelForSelectedEntries.setMinimumWidth(10);
            panelForSelectedEntries.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelForSelectedEntriesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelForSelectedEntries.setLayout(panelForSelectedEntriesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelForSelectedEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelForSelectedEntries.setConstraints(panelForSelectedEntriesConstraints);
            splitPanePanelForSelectedEntries.addChild(panelForSelectedEntries);
            splitpaneEntries.addChild(splitPanePanelForSelectedEntries);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneEntriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneEntries.setConstraints(splitpaneEntriesConstraints);
            panelForFurtherElements.addChild(splitpaneEntries);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelForFurtherElementsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelForFurtherElements.setConstraints(panelForFurtherElementsConstraints);
            panelMain.addChild(panelForFurtherElements);
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