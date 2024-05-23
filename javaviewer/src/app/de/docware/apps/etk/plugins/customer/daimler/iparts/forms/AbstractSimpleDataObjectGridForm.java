/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Abstraktes Formular für die Anzeige von Daten in einem {@link DataObjectGrid} (optional in einem Fenster).
 * <br/>Nach dem Initialisieren muss {@link #dataToGrid()} aufgerufen werden.
 */
public abstract class AbstractSimpleDataObjectGridForm extends AbstractRelatedInfoPartlistDataForm {

    protected String configKey;
    protected String title;
    protected DataObjectFilterGrid grid;

    protected AbstractSimpleDataObjectGridForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               IEtkRelatedInfo relatedInfo, String configKey, String dataObjectGridTitle) {
        super(dataConnector, parentForm, relatedInfo);
        this.configKey = configKey;
        this.title = dataObjectGridTitle;
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    protected AbstractSimpleDataObjectGridForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               String configKey, String dataObjectGridTitle, String windowTitle, String title) {
        this(new RelatedInfoFormConnector(dataConnector), parentForm, null, configKey, dataObjectGridTitle);
        addOwnConnector(getConnector());
        setWindowTitle(windowTitle, title);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    protected void postCreateGui() {
        setDataObjectGridTitle();
        grid = createGrid();
        grid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        grid.getGui().setConstraints(constraints);
        getPanelDataObjectGrid().addChild(grid.getGui());
    }

    protected DataObjectFilterGrid createGrid() {
        return new DataObjectFilterGrid(getConnector(), this);
    }

    protected void setDataObjectGridTitle() {
        if (title != null) {
            mainWindow.panelNorth.setVisible(true);
            mainWindow.labelDataObjectGridTitle.setText(title);
        } else {
            mainWindow.panelNorth.setVisible(false);
        }
    }

    protected GuiLabel getLabelDataObjectGridTitle() {
        return mainWindow.labelDataObjectGridTitle;
    }

    protected GuiPanel getPanelDataObjectGrid() {
        return mainWindow.panelDataObjectGrid;
    }

    @Override
    public GuiPanel getGui() {
        return mainWindow.mainPanel;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        dataToGrid();
    }

    public GuiWindow getWindow() {
        return mainWindow;
    }

    protected boolean addExtraPanelToNorthPanel(GuiPanel extraPanel) {
        if (mainWindow.panelNorth.getChildren().size() > 1) {
            return false;
        }
        mainWindow.panelNorth.addChildBorderEast(extraPanel);
        return true;
    }

    protected GuiButtonPanel getButtonPanel() {
        return mainWindow.buttonPanel;
    }

    /**
     * Zeigt den Dialog als modales Fenster. Vorher sollte {@link #setWindowTitle(String, String)} aufgerufen werden,
     * um den Titel des Fensters und den Untertitel zu setzen.
     */
    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        close();
        return modalResult;
    }


    public void setWindowTitle(String windowTitle, String subTitle) {
        mainWindow.setTitle(windowTitle);
        mainWindow.title.setTitle(subTitle);
    }

    protected EtkDisplayFields getDisplayFields() {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        if (StrUtils.isValid(configKey)) {
            displayResultFields.load(getConfig(), configKey);
        }

        if (displayResultFields.size() == 0) {
            List<EtkDisplayField> defaultDisplayFields = createDefaultDisplayFields();
            if (defaultDisplayFields != null) {
                for (EtkDisplayField defaultDisplayField : defaultDisplayFields) {
                    displayResultFields.addFeld(defaultDisplayField);
                }
            }

            displayResultFields.loadStandards(getConfig());
        }

        return displayResultFields;
    }

    protected void dataToGrid() {
        if (grid.getDisplayFields() == null) {
            grid.setDisplayFields(getDisplayFields());
        }

        grid.clearGrid();

        List<DBDataObjectList<? extends EtkDataObject>> multipleDataObjectList = createMultipleDataObjectList();
        int rowCount = 0;
        if (multipleDataObjectList != null) { // mehrere DBDataObjectLists für verschiedene EtkDataObjects
            if (!multipleDataObjectList.isEmpty()) {
                rowCount = multipleDataObjectList.get(0).size();
                for (int row = 0; row < rowCount; row++) {
                    EtkDataObject[] dataObjectsForRow = new EtkDataObject[multipleDataObjectList.size()];
                    for (int dataObjectListIndex = 0; dataObjectListIndex < multipleDataObjectList.size(); dataObjectListIndex++) {
                        dataObjectsForRow[dataObjectListIndex] = multipleDataObjectList.get(dataObjectListIndex).get(row);
                    }
                    grid.addObjectToGrid(dataObjectsForRow);
                }
            }
        } else { // einfacher Fall mit nur einer DBDataObjectList
            DBDataObjectList<? extends EtkDataObject> dataObjectList = createDataObjectList();
            if (dataObjectList != null) {
                rowCount = dataObjectList.size();
                for (EtkDataObject dataObject : dataObjectList) {
                    grid.addObjectToGrid(dataObject);
                }
            }
        }

        grid.showNoResultsLabel(rowCount == 0);
        grid.updateFilters();
    }

    /**
     * Funktion, die die DEFAULT-Spalten des Ergebnis-Grids festlegt, falls niemand die Spalten über die Workbench konfiguriert.
     */
    protected abstract List<EtkDisplayField> createDefaultDisplayFields();

    @Override
    protected final List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        if (configKey.equals(this.configKey)) {
            return createDefaultDisplayFields();
        } else {
            return null;
        }
    }

    private void closeWindow(Event event) {
        close();
        mainWindow.setVisible(false);
    }

    public DataObjectFilterGrid getGrid() {
        return grid;
    }

    /**
     * Erzeugt die darzustellenden Daten in Form einer {@link DBDataObjectList} bestehend aus {@link EtkDataObject}s.
     *
     * @return
     */
    protected abstract DBDataObjectList<? extends EtkDataObject> createDataObjectList();

    /**
     * Erzeugt eine Liste von {@link DBDataObjectList}s mit den darzustellenden Daten (jeweils ein Listeneintrag pro Tabelle
     * bzw. {@link EtkDataObject}-Typ). Diese Methode hat Vorrang vor {@link #createDataObjectList()}. Beim Überschreiben
     * dieser Methode muss {@link #createDataObjectList()} trotzdem überschrieben werden (weil abstrakt), kann aber {@code null}
     * zurückliefern, weil diese dann nicht benötigt wird.
     *
     * @return
     */
    protected List<DBDataObjectList<? extends EtkDataObject>> createMultipleDataObjectList() {
        return null;
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
        private de.docware.framework.modules.gui.controls.GuiPanel mainPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelNorth;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDataObjectGridTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelDataObjectGrid;

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
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            mainPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            mainPanel.setName("mainPanel");
            mainPanel.__internal_setGenerationDpi(96);
            mainPanel.registerTranslationHandler(translationHandler);
            mainPanel.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanel.setLayout(mainPanelLayout);
            panelNorth = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelNorth.setName("panelNorth");
            panelNorth.__internal_setGenerationDpi(96);
            panelNorth.registerTranslationHandler(translationHandler);
            panelNorth.setScaleForResolution(true);
            panelNorth.setMinimumWidth(10);
            panelNorth.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelNorthLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelNorth.setLayout(panelNorthLayout);
            labelDataObjectGridTitle = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDataObjectGridTitle.setName("labelDataObjectGridTitle");
            labelDataObjectGridTitle.__internal_setGenerationDpi(96);
            labelDataObjectGridTitle.registerTranslationHandler(translationHandler);
            labelDataObjectGridTitle.setScaleForResolution(true);
            labelDataObjectGridTitle.setMinimumWidth(10);
            labelDataObjectGridTitle.setMinimumHeight(10);
            labelDataObjectGridTitle.setPaddingTop(4);
            labelDataObjectGridTitle.setPaddingLeft(8);
            labelDataObjectGridTitle.setPaddingRight(4);
            labelDataObjectGridTitle.setPaddingBottom(4);
            labelDataObjectGridTitle.setText("DataObjectGridTitle");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelDataObjectGridTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelDataObjectGridTitle.setConstraints(labelDataObjectGridTitleConstraints);
            panelNorth.addChild(labelDataObjectGridTitle);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelNorthConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelNorthConstraints.setPosition("north");
            panelNorth.setConstraints(panelNorthConstraints);
            mainPanel.addChild(panelNorth);
            panelDataObjectGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelDataObjectGrid.setName("panelDataObjectGrid");
            panelDataObjectGrid.__internal_setGenerationDpi(96);
            panelDataObjectGrid.registerTranslationHandler(translationHandler);
            panelDataObjectGrid.setScaleForResolution(true);
            panelDataObjectGrid.setMinimumWidth(10);
            panelDataObjectGrid.setMinimumHeight(10);
            panelDataObjectGrid.setPaddingTop(4);
            panelDataObjectGrid.setPaddingLeft(8);
            panelDataObjectGrid.setPaddingRight(8);
            panelDataObjectGrid.setPaddingBottom(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelDataObjectGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelDataObjectGrid.setLayout(panelDataObjectGridLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelDataObjectGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelDataObjectGrid.setConstraints(panelDataObjectGridConstraints);
            mainPanel.addChild(panelDataObjectGrid);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            this.addChild(mainPanel);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCloseActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    closeWindow(event);
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