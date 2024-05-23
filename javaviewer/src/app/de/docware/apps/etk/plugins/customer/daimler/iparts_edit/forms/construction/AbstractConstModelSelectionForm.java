/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EditControls;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.mechanic.AssemblyFormIConnector;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AbstractAssemblyTreeForm;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.events.FilterChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractTwoDataObjectGridsForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsShowDataObjectsDialog;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Abstrakte Klasse für die Auswahl von Konstruktionsbaumuster
 */
public abstract class AbstractConstModelSelectionForm extends AbstractTwoDataObjectGridsForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SELECT_CONST_MODEL_DATA = "iPartsMenuItemSelectModelData"; // Zur Wiedererkennung beim Pop-Up Menü im Baum
    public static final int MIN_CHAR_FOR_SEARCH = 4; // Komplette Typkennzahl

    private iPartsDataModelList modelList = new iPartsDataModelList();
    private EditControls editControls;
    private FrameworkThread searchThread;
    private iPartsDataAssembly dataAssembly;

    private boolean isAggregateForm; // Ist die aktuelle Einschränkung für Fahrzeug- oder Aggegatebaumuster

    protected AbstractConstModelSelectionForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                              iPartsDataAssembly dataAssembly, String configKeyTop, String configKeyBottom) {
        super(dataConnector, parentForm, configKeyTop, "!!Konstruktions-Baumuster", configKeyBottom, "!!Ausgewählte Baumuster");
        this.dataAssembly = dataAssembly;
        setTitle();
        initModels();
    }

    /**
     * Auswahldialog anzeigen aus dem Baum heraus
     *
     * @param formWithTree
     */
    public static void showSelectionForm(AbstractJavaViewerForm formWithTree) {
        if (formWithTree instanceof AbstractAssemblyTreeForm) {
            EtkDataAssembly assembly = ((AbstractAssemblyTreeForm)formWithTree).getCurrentAssembly();
            showSelectionForm(assembly, formWithTree.getConnector(), formWithTree);
        }
    }

    /**
     * Auswahldialog anzeigen aus der Stückliste heraus
     *
     * @param connector
     */
    public static void showSelectionForm(AssemblyListFormIConnector connector) {
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        showSelectionForm(assembly, connector, connector.getActiveForm());
    }

    /**
     * Auswahldialog anzeigen und die Filterkriterien in der Session speichern
     *
     * @param assembly
     * @param dataConnector
     * @param parentForm
     */
    private static void showSelectionForm(EtkDataAssembly assembly, AbstractJavaViewerFormIConnector dataConnector,
                                          AbstractJavaViewerForm parentForm) {
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly dataAssembly = (iPartsDataAssembly)assembly;
            if (dataAssembly.isEdsConstructionAssembly() || dataAssembly.isMBSConstructionAssembly()
                || dataAssembly.isCTTConstructionAssembly()) {
                AbstractConstModelSelectionForm selForm = null;
                if (dataAssembly.isEdsConstructionAssembly()) {
                    selForm = new EDSConstModelSelectionForm(dataConnector, parentForm, dataAssembly);
                } else if (dataAssembly.isMBSConstructionAssembly()) {
                    selForm = new MBSConstModelSelectionForm(dataConnector, parentForm, dataAssembly);
                } else if (dataAssembly.isCTTConstructionAssembly()) {
                    selForm = new CTTConstModelSelectionForm(dataConnector, parentForm, dataAssembly);
                }
                if (selForm == null) {
                    return;
                }
                final AbstractConstModelSelectionForm selectionForm = selForm;
                Session.invokeThreadSafeInSession(() -> {
                    if (selectionForm.showModal() == ModalResult.OK) {
                        // Baugruppe mit der Liste der ausgewählten Baumuster neu laden und im Connector setzen, damit
                        // die Stückliste sauber aktualisiert wird
                        if (dataConnector instanceof AssemblyListFormIConnector) {
                            EtkDataAssembly updatedAssembly = EtkDataObjectFactory.createDataAssembly(dataAssembly.getEtkProject(),
                                                                                                      assembly.getAsId());
                            ((AssemblyListFormIConnector)dataConnector).setCurrentAssembly(updatedAssembly);
                            dataConnector.updateAllViews(parentForm, false);
                        }
                    }
                });
            }
        }
    }

    /**
     * Fügt dem Kontextmenü den neuen Eintrag hinzu
     *
     * @param menu
     * @param formWithTree
     */
    public static void modifyTreePopupMenu(GuiContextMenu menu, final AbstractJavaViewerForm formWithTree) {
        GuiMenuItem menuItem = modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SELECT_CONST_MODEL_DATA, RELATED_INFO_MODEL_SELECT_DATA_TEXT,
                                                   null);
        if (menuItem != null) {
            EventListener listener = new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    showSelectionForm(formWithTree);
                }
            };
            menuItem.addEventListener(listener);
            menuItem.setIcon(DefaultImages.module.getImage());
        }
    }

    /**
     * Überprüfung, wann der Kontextmenüeintrag angezeigt werden soll
     *
     * @param popupMenu
     * @param connector
     */
    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        if (assembly != null) {
            boolean isVisible = false;
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDataAssembly dataAssembly = (iPartsDataAssembly)assembly;
                isVisible = dataAssembly.isEdsConstructionAssembly() || dataAssembly.isMBSConstructionAssembly()
                            || dataAssembly.isCTTConstructionAssembly();
            }

            for (AbstractGuiControl item : popupMenu.getChildren()) {
                if ((item.getUserObject() != null) && item.getUserObject().equals(IPARTS_MENU_ITEM_SELECT_CONST_MODEL_DATA)) {
                    item.setVisible(isVisible);
                    break;
                }
            }
        }
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();
        this.editControls = new EditControls();
        initGrids();
        initButtons();
        addSearchArea();
        Dimension screenSize = FrameworkUtils.getScreenSize();
        setSize(screenSize.width - 20, screenSize.height - 20);
        setSplitPaneDividerRatio(0.6);

        // Erstes Suchfeld fokussieren
        if (!editControls.isEmpty()) {
            editControls.get(0).getAbstractGuiControl().requestFocus();
        }
    }

    private void setTitle() {
        String windowTitle = getTitle();
        String subTitle = getSubTitle();
        if (StrUtils.isValid(windowTitle, subTitle)) {
            setWindowTitle(windowTitle, subTitle);
        }
    }

    private void initButtons() {
        GuiButton deselectAllButton = getButtonPanel().addCustomButton("!!Auswahl aufheben");
        deselectAllButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                deleteAllSelectedModels();
            }
        });
        if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            GuiButton showModelsButton = getButtonPanel().addCustomButton("Vorhandene Baumuster");
            showModelsButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    showModels();
                }
            });
        }
        getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.OK, true);
        final GuiButtonOnPanel buttonOnPanel = getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK);
        buttonOnPanel.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                saveModelFilterValues(event);
            }
        });
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(boolean top) {
        EtkDisplayFields displayFields = SelectSearchGridModel.createDisplayResultFields(getProject());
        displayFields.getFeldByName(TABLE_DA_MODEL, FIELD_DM_MODEL_NO).setColumnFilterEnabled(true);
        return displayFields.getFields();
    }

    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
        // Initial die Grids gar nicht befüllen
    }

    /**
     * Initialisiert entweder das obere oder das untere Grid (abhängig vom übergebenen boolean Wert)
     *
     * @param top
     */
    private void initGrid(boolean top) {
        DataObjectGrid grid = top ? gridTop : gridBottom;
        grid.setMultiSelect(true);
        EventListener eventListener;
        if (top) {
            grid.setNoResultsLabelText("!!Minimale Anzahl von Zeichen für Suche nicht erreicht");
            eventListener = new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT) {
                @Override
                public void fire(Event event) {
                    addModelToSelectedModels();
                }
            };
        } else {
            eventListener = new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT) {
                @Override
                public void fire(Event event) {
                    deleteSelectedModels();
                }
            };
        }
        grid.getTable().addEventListener(eventListener);
        grid.getTable().setContextMenu(createContextMenu(top));
    }

    /**
     * Erstellt ein Kontextmenü für das obere oder das untere Grid (abhängig vom übergebenen boolean Wert)
     *
     * @param top
     * @return
     */
    private GuiContextMenu createContextMenu(boolean top) {
        GuiContextMenu contextmenu = new GuiContextMenu();
        contextmenu.setName("contextmenuSelectModel");
        EditToolbarButtonMenuHelper toolbarHelper = new EditToolbarButtonMenuHelper(getConnector(), null);
        final GuiTable table = top ? gridTop.getTable() : gridBottom.getTable();
        GuiMenuItem menuItem = toolbarHelper.createCopyMenuForTable(table, getUITranslationHandler());
        contextmenu.addChild(menuItem);
        if (top) {
            menuItem = toolbarHelper.createContextMenuEntry(iPartsToolbarButtonAlias.EDIT_NEW, "!!Baumuster übernehmen", getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    addModelToSelectedModels();
                }
            });
            contextmenu.addChild(menuItem);

        } else {
            menuItem = toolbarHelper.createContextMenuEntry(iPartsToolbarButtonAlias.EDIT_DELETE, "!!Baumuster entfernen", getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    deleteSelectedModels();
                }
            });
            contextmenu.addChild(menuItem);
        }
        menuItem = toolbarHelper.createContextMenuEntry(iPartsToolbarButtonAlias.EDIT_SELECTALL, "!!Alles markieren", getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                table.switchOffEventListeners();
                table.selectAllRows();
                table.switchOnEventListeners();
            }
        });
        contextmenu.addChild(menuItem);
        return contextmenu;
    }

    /**
     * Löscht die selektierten Baumuster aus dem unteren Grid (die zu filternden Baumuster)
     */
    private void deleteSelectedModels() {
        List<Integer> selectedList = new DwList<Integer>(gridBottom.getTable().getSelectedRowIndices());
        List<EtkDataObject> resultList = new DwList<EtkDataObject>();
        int bottomRowCount = gridBottom.getTable().getRowCount();
        for (int i = 0; i < bottomRowCount; i++) {
            if (selectedList.contains(i)) {
                continue;
            }
            GuiTableRow row = gridBottom.getTable().getRow(i);
            if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                EtkDataObject object = ((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(TABLE_DA_MODEL);
                if (object != null) {
                    resultList.add(object);
                }
            }
        }
        fillGridIntern(resultList, false);
        enableOKButton();
    }

    private void deleteAllSelectedModels() {
        gridBottom.clearGrid();
        gridBottom.showNoResultsLabel(true);
        addToGridBottomTitle(String.valueOf(gridBottom.getTable().getRowCount()));
        enableOKButton();
    }

    private void showModels() {
        List<iPartsDataModel> showList = new DwList<>();
        showList.addAll(modelList.getAsList());
        showList.sort(Comparator.comparing(o -> o.getAsId().getModelNumber()));
        String title = TranslationHandler.translate("!!Vorhandene %1", TranslationHandler.translate(getTitle()));
        iPartsShowDataObjectsDialog.showModelList(getConnector(), this, showList, gridTop.getDisplayFields(), title, false);
    }

    /**
     * Fügt die im oberen Grid selektierten Baumuster zum unteren Grid hinzu
     */
    private void addModelToSelectedModels() {
        // Die bereits ausgewählten Baumuster in einer Map sammeln
        Map<String, EtkDataObject> result = new TreeMap<>();
        int bottomRowCount = gridBottom.getTable().getRowCount();
        for (int i = 0; i < bottomRowCount; i++) {
            GuiTableRow row = gridBottom.getTable().getRow(i);
            if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                EtkDataObject object = ((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(TABLE_DA_MODEL);
                if (object != null) {
                    result.put(object.getAsId().toString(), object);
                }
            }
        }

        // neue hinzufügen
        List<List<EtkDataObject>> selectedModels = gridTop.getMultiSelection();
        if (!selectedModels.isEmpty()) {
            for (List<EtkDataObject> selecedModelEntry : selectedModels) {
                EtkDataObject newObject = selecedModelEntry.get(0);
                if (newObject != null) {
                    result.put(newObject.getAsId().toString(), newObject);
                }
            }
        }
        // Ausgewählte Baumuster entfernen
//        for (GuiTableRow row : gridTop.getTable().getSelectedRows()) {
//            gridTop.getTable().removeRow(row);
//        }
        // Grid befüllen
        fillGridIntern(result.values(), false);
        enableOKButton();
    }

    /**
     * Zeigt an, ob es sich bei der aktuellen Eingrenzung der Baumuster um Aggregate Baumuster handelt
     *
     * @param isAggregateForm
     */
    private void setIsAggregateForm(boolean isAggregateForm) {
        this.isAggregateForm = isAggregateForm;
    }

    /**
     * Initialisiert beide Grids
     */
    private void initGrids() {
        initGrid(true);
        initGrid(false);
    }

    /**
     * Initialisiert alle Baumuster und initiiert den Datentransfer in das obere Grid
     */
    private void initModels() {
        Set<iPartsModelId> models = new LinkedHashSet<>();
        // Bestimmen der Baumuster über die SubAssemblies
        for (EtkDataPartListEntry modelEntry : getDataAssembly().getSubAssemblyEntries(false, null)) {
            if (modelEntry instanceof iPartsDataPartListEntry) {
                List<iPartsVirtualNode> virtNodes = iPartsVirtualNode.parseVirtualIds(modelEntry.getDestinationAssemblyId());
                if (iPartsVirtualNode.isModelNode(virtNodes)) {
                    models.add((iPartsModelId)virtNodes.get(0).getId());
                }
            }
        }
        for (iPartsModelId modelId : models) {
            iPartsDataModel model = new iPartsDataModel(getProject(), modelId);
            modelList.add(model, DBActionOrigin.FROM_DB);
        }
        dataToGrid();
        // Wenn Baumuster existieren:
        // - Bestimme den Typ (Fahrzeug oder Aggregat) mit Hilfe des ersten Baumusters
        // - Setze abhängig davon zuvor ausgewählte Baumuster
        if (!modelList.isEmpty()) {
            iPartsModelId firstModel = modelList.iterator().next().getAsId();
            setIsAggregateForm(firstModel.isAggregateModel());
            setSelectedModels(getSelectedModelSet(firstModel.isAggregateModel()));
        }
        enableOKButton();
    }

    private void enableOKButton() {
        Set<String> bottomModels = getModelsFromBottomGrid();
        Set<String> selectedModels = getSelectedModelSet(isAggregateForm);
        if (selectedModels == null) {
            selectedModels = new HashSet<>();
        }
        // Speichern kann aman, wenn die Anzahl ausgewählter Baumuster unterschiedlich der Anzahl gespeicherter ist. Oder
        // bei gleicher Anzahl sich die Baumuster unterscheiden.
        boolean changed = (bottomModels.size() != selectedModels.size()) || !bottomModels.containsAll(selectedModels);
        getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setEnabled(changed);
    }

    private iPartsDataAssembly getDataAssembly() {
        if (dataAssembly != null) {
            return dataAssembly;
        }
        RelatedInfoBaseFormIConnector connector = getConnector();
        if (connector instanceof AssemblyFormIConnector) {
            EtkDataAssembly assembly = ((AssemblyFormIConnector)connector).getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                dataAssembly = (iPartsDataAssembly)assembly;
            }
        }
        return dataAssembly;
    }

    /**
     * Filtert abhängig vom eingegebenen Suchkriterium die Anzeige im oberen Grid
     *
     * @param event
     */
    protected void doFilterModel(Event event) {
        if (searchThread != null) {
            searchThread.cancel();
            searchThread = null;
        }
        String searchValue = "";
        EditControlFactory ctrl = editControls.getControlByFeldIndex(0).getEditControl();
        if (ctrl != null) {
            searchValue = ctrl.getText().trim();
        }

        // Minimale Anzahl Zeichen ohne Wildcards für die Baumuster-Suche prüfen
        if (searchValue.trim().replace("*", "").replace("?", "").length() < MIN_CHAR_FOR_SEARCH) {
            gridTop.clearGrid();
            gridTop.setNoResultsLabelText("!!Minimale Anzahl von Zeichen für Suche nicht erreicht");
            gridTop.showNoResultsLabel(gridTop.getTable().getRowCount() < 1);
            return;
        }

        if (StrUtils.isValid(searchValue)) {
            if (!searchValue.endsWith("*")) {
                searchValue = searchValue + "*";
            }
            final String finalSearchValue = searchValue;
            gridTop.clearGrid();

            searchThread = Session.invokeThreadSafeInSessionWithChildThread(() -> {
                gridTop.getTable().switchOffEventListeners();
                try {
                    for (iPartsDataModel modelData : modelList) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        String modelNumber = modelData.getAsId().getModelNumber();
                        if (StrUtils.matchesSqlLike(finalSearchValue, modelNumber, false) && isValidModel(modelData)) {
                            gridTop.addObjectToGrid(modelData);
                        }
                    }
                } finally {
                    gridTop.setNoResultsLabelText(DataObjectGrid.NO_ENTRIES_IN_GRID_TEXT);
                    gridTop.showNoResultsLabel(gridTop.getTable().getRowCount() < 1);
                    gridTop.getTable().switchOnEventListeners();
                    addToGridTopTitle(String.valueOf(gridTop.getTable().getRowCount()));
                }
            });
        }
    }

    /**
     * Liefert alle Baumuster aus dem unteren Grid
     *
     * @return
     */
    private Set<String> getModelsFromBottomGrid() {
        List<iPartsDataModel> models = gridBottom.getDataObjectList(iPartsDataModel.class);
        Set<String> result = new HashSet<>();
        if (!models.isEmpty()) {
            for (iPartsDataModel model : models) {
                result.add(model.getAsId().getModelNumber());
            }
        }
        return result;
    }

    /**
     * Speichert die ausgewählten Baumuster in der aktuellen Session
     *
     * @param event
     */
    private void saveModelFilterValues(Event event) {
        Set<String> filterValues = new HashSet<>();
        // Die Tabelle durchlaufen und alle Baumusternummern sammeln
        collectModelFilterValues(gridBottom, filterValues);
        // Abhängig vom Typ (Aggregat oder Fahrzeug) Baumuster für den Filter setzen
        setConstructionModelSetToFilter(filterValues);

        // Refresh auf der Ansicht
        getConnector().getProject().fireProjectEvent(new FilterChangedEvent());

        // Fenster Schließen
        getWindow().setVisible(false);
    }

    protected void collectModelFilterValues(DataObjectGrid grid, Set<String> filterValues) {
        int rowCount = grid.getTable().getRowCount();
        // Die Tabelle durchlaufen und alle Baumusternummern sammeln
        if (rowCount > 0) {
            for (int i = 0; i < rowCount; i++) {
                GuiTableRow row = grid.getTable().getRow(i);
                if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                    EtkDataObject dataObject = ((DataObjectGrid.GuiTableRowWithObjects)row).getObjectForTable(TABLE_DA_MODEL);
                    if (dataObject instanceof iPartsDataModel) {
                        filterValues.add(((iPartsModelId)dataObject.getAsId()).getModelNumber());
                    }
                }
            }
        }
    }

    protected boolean isAggregateForm() {
        return isAggregateForm;
    }

    /**
     * Fügt den Bereich für die Suchwerte hinzu
     */
    protected void addSearchArea() {
        EditControl ctrl = createSearchFieldControl();

        ConstraintsGridBag gridbagConstraints = new ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_WEST,
                                                                       ConstraintsGridBag.FILL_NONE, 8, 8, 8, 4);
        ctrl.getLabel().setConstraints(gridbagConstraints);
        getAdditionalTopPanel().addChild(ctrl.getLabel());

        ConstraintsGridBag gridbagConstraints1 = new ConstraintsGridBag(1, 0, 1, 1, getWeightXForSearchFieldEditControl(), 0.0, ConstraintsGridBag.ANCHOR_WEST,
                                                                        ConstraintsGridBag.FILL_NONE, 8, 4, 8, 4);
        ctrl.getEditControl().getControl().setConstraints(gridbagConstraints1);
        getAdditionalTopPanel().addChild(ctrl.getEditControl().getControl());

        int additionalGridWidth = addAdditionalControlsToSearchArea(ctrl);

        GuiSeparator separator = new GuiSeparator(DWOrientation.HORIZONTAL);
        separator.setConstraints(new ConstraintsGridBag(0, 1, 3 + additionalGridWidth, 1, 0, 0, ConstraintsGridBag.ANCHOR_CENTER,
                                                        ConstraintsGridBag.FILL_HORIZONTAL, 8, 4, 8, 4));
        getAdditionalTopPanel().addChild(separator);

    }

    protected EditControl createSearchFieldControl() {
        EtkDisplayField searchField = new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_NO, false, false);
        searchField.setSprache(getProject().getDBLanguage());

        String labelText = searchField.isDefaultText() ? null : searchField.getText().getText(getProject().getViewerLanguage());
        EditControl ctrl = editControls.createForSearch(null, getProject(), searchField.getKey().getTableName(), searchField.getKey().getFieldName(),
                                                        getProject().getDBLanguage(), getProject().getViewerLanguage(), "", labelText, 0);
        ctrl.getEditControl().getControl().setMinimumWidth(200);
        ctrl.getEditControl().getControl().addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doFilterModel(event);
            }
        });

        return ctrl;
    }

    protected double getWeightXForSearchFieldEditControl() {
        return 100.0;
    }

    protected int addAdditionalControlsToSearchArea(EditControl searchFieldControl) {
        return 0;
    }

    /**
     * Setzt zuvor ausgewählte Baumuster (nach denen im Workflow gefiltert werden soll)
     *
     * @param selectedModels
     */
    public void setSelectedModels(Set<String> selectedModels) {
        if (selectedModels != null) {
            fillGridIntern(selectedModels, false);
        }
    }

    private void fillGridIntern(Set<String> selectedModels, boolean top) {
        List<EtkDataObject> modelList = new DwList<EtkDataObject>();
        if ((selectedModels != null) && !selectedModels.isEmpty()) {
            for (String selectedModel : selectedModels) {
                iPartsModelId modelId = new iPartsModelId(selectedModel);
                if (modelId.isAggregateModel() == isAggregateForm) {
                    modelList.add(new iPartsDataModel(getProject(), modelId));
                }
            }
        }
        fillGridIntern(modelList, top);
    }

    private void fillGridIntern(Collection<EtkDataObject> dataObjectList, boolean top) {
        DataObjectGrid grid = top ? gridTop : gridBottom;
        grid.getTable().switchOffEventListeners();
        // Grid befüllen
        grid.clearGrid();
        for (EtkDataObject dataObject : dataObjectList) {
            grid.addObjectToGrid(dataObject);
        }
        if (!top) {
            grid.showNoResultsLabel(grid.getTable().getRowCount() < 1);
            addToGridBottomTitle(String.valueOf(grid.getTable().getRowCount()));
        } else {
            addToGridTopTitle(String.valueOf(grid.getTable().getRowCount()));
        }
        grid.getTable().switchOnEventListeners();
    }

    public iPartsDataModelList getModelList() {
        return modelList;
    }

    protected abstract void setConstructionModelSetToFilter(Set<String> filterValues);

    protected abstract Set<String> getSelectedModelSet(boolean isAggregate);

    protected abstract String getTitle();

    protected abstract String getSubTitle();

    protected abstract boolean isValidModel(iPartsDataModel modelData);
}