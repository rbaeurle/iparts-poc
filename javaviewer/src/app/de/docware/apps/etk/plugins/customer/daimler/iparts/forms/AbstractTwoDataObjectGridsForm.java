/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstraktes Formular für die Anzeige von zwei unterschiedlichen Datensatzarten in zwei {@link DataObjectGrid}s (optional
 * in einem Fenster).
 * <br/>Nach dem Initialisieren muss {@link #dataToGrid()} aufgerufen werden.
 */
public abstract class AbstractTwoDataObjectGridsForm extends AbstractRelatedInfoPartlistDataForm {

    protected String configKeyTop;
    protected String titleTop;
    protected DataObjectGrid gridTop;
    protected String configKeyBottom;
    protected String titleBottom;
    protected DataObjectGrid gridBottom;
    protected double splitPaneDividerRatio = 0.5d;
    protected boolean isAutoDividerPosition = true;
    protected boolean editMode = false; // wird nur bei Formular-Erzeugung gesetzt. Ändern zur Laufzeit macht keinen Sinn

    protected AbstractTwoDataObjectGridsForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             IEtkRelatedInfo relatedInfo, String configKeyTop, String dataObjectGridTopTitle,
                                             String configKeyBottom, String dataObjectGridBottomTitle, boolean enableEditMode) {
        super(dataConnector, parentForm, relatedInfo);
        this.configKeyTop = configKeyTop;
        this.titleTop = dataObjectGridTopTitle;
        this.configKeyBottom = configKeyBottom;
        this.titleBottom = dataObjectGridBottomTitle;
        if (enableEditMode) {
            editMode = isEditContext(dataConnector, false);
        }
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    protected AbstractTwoDataObjectGridsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             String configKeyTop, String dataObjectGridTopTitle, String configKeyBottom,
                                             String dataObjectGridBottomTitle) {
        this(dataConnector, parentForm, configKeyTop, dataObjectGridTopTitle, configKeyBottom, dataObjectGridBottomTitle, false);
    }

    protected AbstractTwoDataObjectGridsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             String configKeyTop, String dataObjectGridTopTitle, String configKeyBottom,
                                             String dataObjectGridBottomTitle, boolean isEditMode) {
        this(new RelatedInfoFormConnector(dataConnector), parentForm, null, configKeyTop, dataObjectGridTopTitle,
             configKeyBottom, dataObjectGridBottomTitle, isEditMode);
        if (isEditMode && (getConnector().getEditContext() == null)) {
            if (dataConnector instanceof RelatedInfoFormConnector) {
                getConnector().setEditContext(((RelatedInfoFormConnector)dataConnector).getEditContext());
            }
        }
        addOwnConnector(getConnector());
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    protected void postCreateGui() {
        // gridTop
        setGridTopTitle(titleTop);
        gridTop = createGrid(true);
        gridTop.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        gridTop.getGui().setConstraints(constraints);
        mainWindow.panelDataObjectGridTop.addChild(gridTop.getGui());

        // gridBottom
        setGridBottomTitle(titleBottom);
        gridBottom = createGrid(false);
        gridBottom.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

        constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        gridBottom.getGui().setConstraints(constraints);
        mainWindow.panelDataObjectGridBottom.addChild(gridBottom.getGui());

        mainWindow.splitPane.addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, mainWindow.splitPane) {
            @Override
            public void fireOnce(Event event) {
                if (isAutoDividerPosition()) {
                    int height = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                    mainWindow.splitPane.setDividerPosition((int)(height * splitPaneDividerRatio));
                }
            }
        });

        // TableSelectionListener zum enablen/ disablen der Buttons und Menus je nach Selektion
        EventListener tableSelectedListener = new EventListener(Event.TABLE_SELECTION_EVENT) {
            @Override
            public void fire(Event event) {
                if ((event != null) && (event.getSource() != null)) {
                    boolean isTop = event.getSource().equals(gridTop.getTable());
                    doTableSelectionChanged(isTop);
                }
            }
        };

        gridTop.getTable().addEventListener(tableSelectedListener);
        gridBottom.getTable().addEventListener(tableSelectedListener);

        // es würde für die Werkseinsatzdaten Sinn machen hier die Retailfilter-Checkbox sichtbar zu setzen wenn editMode true ist;
        // hier müsste aber erst das Verhalten der existierenden Ableitungen geprüft bzw. angepasst werden
    }

    /**
     * Wird aufgerufen, wenn sich die Selektion in einem Grid geändert hat, damit z.B. das andere Grid geupdatet werden kann.
     *
     * @param top, gibt das Grid an, in dem sich die Selektion geändert hat
     */
    protected void doTableSelectionChanged(boolean top) {
    }

    protected DataObjectGrid createGrid(boolean top) {
        return new DataObjectGrid(getConnector(), this);
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

    public void setGridTopTitle(String gridTopTitle) {
        if (editMode) {
            setGridTitle(mainWindow.labelDataObjectGridTopTitle, "");
            mainWindow.panelDataObjectGridTop.setTitle(gridTopTitle);
        } else {
            setGridTitle(mainWindow.labelDataObjectGridTopTitle, gridTopTitle);
        }
        titleTop = gridTopTitle;
    }

    public void addToGridTopTitle(String gridTopTitleExtend) {
        setGridTopTitle(handleAddTitle(titleTop, gridTopTitleExtend));
    }

    public void setGridBottomTitle(String gridBottomTitle) {
        if (editMode) {
            setGridTitle(mainWindow.labelDataObjectGridBottomTitle, "");
            mainWindow.panelDataObjectGridBottom.setTitle(gridBottomTitle);
        } else {
            setGridTitle(mainWindow.labelDataObjectGridBottomTitle, gridBottomTitle);
        }
        titleBottom = gridBottomTitle;
    }

    public void addToGridBottomTitle(String gridBottomTitleExtend) {
        setGridBottomTitle(handleAddTitle(titleBottom, gridBottomTitleExtend));
    }

    private void setGridTitle(GuiLabel label, String gridTitle) {
        if (StrUtils.isValid(gridTitle)) {
            label.setText(gridTitle);
            label.setVisible(true);
        } else {
            label.setVisible(false);
        }
    }

    private String handleAddTitle(String currentTitle, String gridTitleExtension) {
        String text = currentTitle;
        text = StrUtils.stringUpToCharacter(text, " (");
        if (text.isEmpty()) {
            text = currentTitle;
        }
        while ((text.length() > 0) && text.startsWith("?")) {
            text = StrUtils.removeFirstCharacterIfCharacterIs(text, '?');
        }
        if (!text.startsWith("!!")) {
            text = "!!" + text;
        }
        if (StrUtils.isValid(gridTitleExtension)) {
            text = TranslationHandler.translate("!!%1 (%2)", TranslationHandler.translate(text),
                                                TranslationHandler.translate(gridTitleExtension));
        }
        return text;
    }

    public void setGridTopVisible(boolean visible) {
        if (visible != isGridVisible(true)) {
            if (visible) {
                mainWindow.panelBottom.removeFromParent();
                mainWindow.splitPane.removeAllChildren();
                mainWindow.splitPane.addChild(mainWindow.panelTop);
                mainWindow.splitPane.addChild(mainWindow.panelBottom);
                mainWindow.mainPanel.addChild(mainWindow.splitPane);
                dataToGrid();
            } else {
                mainWindow.panelTop.removeFromParent();
                mainWindow.splitPane.removeFromParent();
                mainWindow.panelBottom.setConstraints(mainWindow.splitPane.getConstraints());
                mainWindow.mainPanel.addChild(mainWindow.panelBottom);
            }
        }
    }

    public void setGridBottomVisible(boolean visible) {
        if (visible != isGridVisible(false)) {
            if (visible) {
                mainWindow.panelTop.removeFromParent();
                mainWindow.splitPane.removeAllChildren();
                mainWindow.splitPane.addChild(mainWindow.panelTop);
                mainWindow.splitPane.addChild(mainWindow.panelBottom);
                mainWindow.mainPanel.addChild(mainWindow.splitPane);
                dataToGrid();
            } else {
                mainWindow.panelBottom.removeFromParent();
                mainWindow.splitPane.removeFromParent();
                mainWindow.panelTop.setConstraints(mainWindow.splitPane.getConstraints());
                mainWindow.mainPanel.addChild(mainWindow.panelTop);
            }
        }
    }

    protected boolean isGridVisible(boolean top) {
        if (top) {
            return mainWindow.panelTop.getParent() != null;
        } else {
            return mainWindow.panelBottom.getParent() != null;
        }
    }

    protected DataObjectGrid getGrid(boolean top) {
        if (top) {
            return gridTop;
        } else {
            return gridBottom;
        }
    }

    /**
     * Liefert alle {@link EtkDataObject}s zur passenden Klasse aus dem oberen oder unteren Grid zurück.
     *
     * @param top
     * @param dataObjectClass
     * @return
     */
    public <E extends EtkDataObject> List<E> getDataObjectList(boolean top, Class<E> dataObjectClass) {
        return getGrid(top).getDataObjectList(dataObjectClass);
    }

    /**
     * Liefert das selektierte {@link EtkDataObject} zur passenden Klasse aus dem oberen oder unteren Grid zurück.
     *
     * @param top
     * @param dataObjectClass
     * @return
     */
    public <E extends EtkDataObject> E getSelection(boolean top, Class<E> dataObjectClass) {
        List<EtkDataObject> selection = getGrid(top).getSelection();
        if (selection != null) {
            for (EtkDataObject selectedDataObject : selection) {
                if (dataObjectClass.isAssignableFrom(selectedDataObject.getClass())) {
                    return (E)selectedDataObject;
                }
            }
        }

        return null;
    }

    /**
     * Liefert die selektierten {@link EtkDataObject}s zur passenden Klasse aus dem oberen oder unteren Grid zurück.
     *
     * @param top
     * @param dataObjectClass
     * @return
     */
    public <E extends EtkDataObject> EtkDataObjectList<E> getMultiSelection(boolean top, Class<E> dataObjectClass) {
        List<List<EtkDataObject>> selection = getGrid(top).getMultiSelection();
        EtkDataObjectList<E> result = new GenericEtkDataObjectList();
        if (selection != null) {
            for (List<EtkDataObject> selectedRow : selection) {
                for (EtkDataObject selectedDataObject : selectedRow) {
                    if (dataObjectClass.isAssignableFrom(selectedDataObject.getClass())) {
                        result.add((E)selectedDataObject, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }

        return result;
    }

    protected GuiButtonPanel getButtonPanel() {
        return mainWindow.buttonPanel;
    }

    protected GuiPanel getBottomPanel() {
        return mainWindow.panelBottom;
    }

    protected GuiPanel getAdditionalTopPanel() {
        return mainWindow.additionalTopPanel;
    }

    protected GuiPanel getTopPanel() {
        return mainWindow.panelTop;
    }

    // Checkbox "Alle Stände"
    protected GuiCheckbox getCheckboxShowHistory() {
        return mainWindow.checkboxShowHistory;
    }

    public void setCheckboxShowHistoryVisible(boolean visible) {
        mainWindow.checkboxShowHistory.setVisible(visible);
    }

    public void setShowHistory(boolean showHistory) {
        mainWindow.checkboxShowHistory.setSelected(showHistory);
    }

    public boolean isShowHistory() {
        return mainWindow.checkboxShowHistory.isSelected();
    }

    // Checkbox "Retailfilter"
    protected GuiCheckbox getCheckboxRetailFilter() {
        return mainWindow.checkboxRetailFilter;
    }

    public void setCheckboxRetailFilterVisible(boolean visible) {
        mainWindow.checkboxRetailFilter.setVisible(visible);
    }

    public void setRetailFilter(boolean retailFilter) {
        mainWindow.checkboxRetailFilter.setSelected(retailFilter);
    }

    public boolean isRetailFilterSet() {
        return mainWindow.checkboxRetailFilter.isSelected();
    }

    public boolean isRetailFilter() {
        return !editMode || isRetailFilterSet();
    }

    public void setSplitPaneDividerPosition(int splitPaneDividerPos) {
        mainWindow.splitPane.setDividerPosition(splitPaneDividerPos);
    }

    public int getSplitPaneDividerPosition() {
        return mainWindow.splitPane.getDividerPosition();
    }

    public void setSplitPaneDividerRatio(double splitPaneDividerRatio) {
        mainWindow.splitPane.setResizeWeight(splitPaneDividerRatio);
        this.splitPaneDividerRatio = splitPaneDividerRatio;
    }

    public double getSplitPaneDividerRatio() {
        return splitPaneDividerRatio;
    }

    protected EtkDisplayFields getDisplayFields(boolean top) {
        return getDisplayFields(top ? configKeyTop : configKeyBottom);
    }

    protected void dataToGrid() {
        dataToGrid(true);
        dataToGrid(false);
    }

    protected void dataToGrid(boolean top) {
        if (top) {
            if (mainWindow.panelTop.getParent() != null) { // ist panelTop überhaupt in der GUI eingehängt?
                if (gridTop.getDisplayFields() == null) {
                    gridTop.setDisplayFields(getDisplayFields(true));
                }
                fillGrid(top);
            }
        } else {
            if (mainWindow.panelBottom.getParent() != null) {// ist panelBottom überhaupt in der GUI eingehängt?
                if (gridBottom.getDisplayFields() == null) {
                    gridBottom.setDisplayFields(getDisplayFields(false));
                }
                fillGrid(top);
            }
        }
    }

    protected List<IdWithType> getSelectedObjectIds(boolean top) {
        return new DwList<>();
    }

    protected void setSelectedObjectIds(boolean top, List<IdWithType> selectedIds) {
    }

    /**
     * Grid aus DBDataObjectList befüllen.
     *
     * @param top
     */
    protected void fillGrid(boolean top) {
        DataObjectGrid dataGrid = getGrid(top);
        int sortColumn = dataGrid.getTable().getSortColumn();
        boolean sortAscending = dataGrid.getTable().isSortAscending();
        List<IdWithType> selectedIds = getSelectedObjectIds(top);
        Map<Integer, Object> columnFilterValuesMap = new HashMap<>();
        AbstractGuiTableColumnFilterFactory copyColumnFilterFactory = null;
        if (dataGrid instanceof DataObjectFilterGrid) {
            copyColumnFilterFactory = ((DataObjectFilterGrid)dataGrid).storeFilterFactory(columnFilterValuesMap);
        }

        dataGrid.clearGrid();
        createAndAddDataObjectsToGrid(top);

        if (copyColumnFilterFactory != null) {
            ((DataObjectFilterGrid)dataGrid).restoreFilterFactory(copyColumnFilterFactory, columnFilterValuesMap);
        }
        if (dataGrid.getTable().isSortEnabled() && (sortColumn >= 0)) {
            dataGrid.getTable().sortRowsAccordingToColumn(sortColumn, sortAscending);
        }
        setSelectedObjectIds(top, selectedIds);
        dataGrid.showNoResultsLabel(dataGrid.getTable().getRowCount() == 0);
    }

    protected void addDataObjectListToGrid(boolean top, DBDataObjectList<? extends EtkDataObject> dataObjectList) {
        for (EtkDataObject dataObject : dataObjectList) {
            addDataObjectToGrid(top, dataObject);
        }
    }

    protected void addDataObjectToGrid(boolean top, EtkDataObject... dataObjectsInRow) {
        checkStatusValuesForReadOnly(dataObjectsInRow);
        getGrid(top).addObjectToGrid(dataObjectsInRow);
    }

    /**
     * Liefert den Feldnamen für den Status eines Datensatzes zurück. Muss überschrieben werden, wenn das obere und
     * untere Grid verschiedene Status-Felder haben.
     *
     * @return Bei {@code null} wird der Status nicht berücksichtigt
     */
    protected String getStatusFieldName(boolean top) {
        return getStatusFieldName();
    }

    protected void setSize(int width, int heigth) {
        mainWindow.setSize(width, heigth);
    }

    protected abstract List<EtkDisplayField> createDefaultDisplayFields(boolean top);

    /**
     * DataObjectList für das jeweilige Grid bestimmen
     *
     * @param top
     * @return
     */
    protected abstract void createAndAddDataObjectsToGrid(boolean top);

    @Override
    protected final List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        if (configKey.equals(configKeyTop)) {
            return createDefaultDisplayFields(true);
        } else if (configKey.equals(configKeyBottom)) {
            return createDefaultDisplayFields(false);
        } else {
            return null;
        }
    }

    protected void checkboxShowHistoryClicked(Event event) {
    }

    protected void checkboxRetailFilterClicked(Event event) {
    }

    /**
     * Soll die Position vom Divider automatisch gesetzt werden?
     *
     * @return
     */
    public boolean isAutoDividerPosition() {
        return isAutoDividerPosition;
    }

    /**
     * Soll die Position vom Divider automatisch gesetzt werden?
     *
     * @param autoDividerPosition
     */
    public void setAutoDividerPosition(boolean autoDividerPosition) {
        isAutoDividerPosition = autoDividerPosition;
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
        private de.docware.framework.modules.gui.controls.GuiPanel additionalTopPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitPane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDataObjectGridTopTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelDataObjectGridTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDataObjectGridBottomTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelDataObjectGridBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxShowHistory;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiCheckbox checkboxRetailFilter;

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
            de.docware.framework.modules.gui.layout.LayoutGridBag mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mainPanel.setLayout(mainPanelLayout);
            additionalTopPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            additionalTopPanel.setName("additionalTopPanel");
            additionalTopPanel.__internal_setGenerationDpi(96);
            additionalTopPanel.registerTranslationHandler(translationHandler);
            additionalTopPanel.setScaleForResolution(true);
            additionalTopPanel.setMinimumWidth(10);
            additionalTopPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag additionalTopPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            additionalTopPanel.setLayout(additionalTopPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag additionalTopPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 2, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            additionalTopPanel.setConstraints(additionalTopPanelConstraints);
            mainPanel.addChild(additionalTopPanel);
            splitPane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitPane.setName("splitPane");
            splitPane.__internal_setGenerationDpi(96);
            splitPane.registerTranslationHandler(translationHandler);
            splitPane.setScaleForResolution(true);
            splitPane.setMinimumWidth(10);
            splitPane.setMinimumHeight(10);
            splitPane.setPaddingBottom(1);
            splitPane.setHorizontal(false);
            splitPane.setDividerPosition(484);
            splitPane.setResizeWeight(0.5);
            panelTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTop.setName("panelTop");
            panelTop.__internal_setGenerationDpi(96);
            panelTop.registerTranslationHandler(translationHandler);
            panelTop.setScaleForResolution(true);
            panelTop.setMinimumHeight(0);
            panelTop.setPaddingTop(4);
            panelTop.setPaddingLeft(8);
            panelTop.setPaddingRight(8);
            panelTop.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelTop.setLayout(panelTopLayout);
            labelDataObjectGridTopTitle = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDataObjectGridTopTitle.setName("labelDataObjectGridTopTitle");
            labelDataObjectGridTopTitle.__internal_setGenerationDpi(96);
            labelDataObjectGridTopTitle.registerTranslationHandler(translationHandler);
            labelDataObjectGridTopTitle.setScaleForResolution(true);
            labelDataObjectGridTopTitle.setMinimumWidth(10);
            labelDataObjectGridTopTitle.setMinimumHeight(10);
            labelDataObjectGridTopTitle.setPaddingBottom(2);
            labelDataObjectGridTopTitle.setText("DataObjectGridTopTitle");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelDataObjectGridTopTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelDataObjectGridTopTitleConstraints.setPosition("north");
            labelDataObjectGridTopTitle.setConstraints(labelDataObjectGridTopTitleConstraints);
            panelTop.addChild(labelDataObjectGridTopTitle);
            panelDataObjectGridTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelDataObjectGridTop.setName("panelDataObjectGridTop");
            panelDataObjectGridTop.__internal_setGenerationDpi(96);
            panelDataObjectGridTop.registerTranslationHandler(translationHandler);
            panelDataObjectGridTop.setScaleForResolution(true);
            panelDataObjectGridTop.setMinimumWidth(10);
            panelDataObjectGridTop.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelDataObjectGridTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelDataObjectGridTop.setLayout(panelDataObjectGridTopLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelDataObjectGridTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelDataObjectGridTop.setConstraints(panelDataObjectGridTopConstraints);
            panelTop.addChild(panelDataObjectGridTop);
            splitPane.addChild(panelTop);
            panelBottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelBottom.setName("panelBottom");
            panelBottom.__internal_setGenerationDpi(96);
            panelBottom.registerTranslationHandler(translationHandler);
            panelBottom.setScaleForResolution(true);
            panelBottom.setMinimumHeight(0);
            panelBottom.setPaddingTop(4);
            panelBottom.setPaddingLeft(8);
            panelBottom.setPaddingRight(8);
            panelBottom.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelBottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelBottom.setLayout(panelBottomLayout);
            labelDataObjectGridBottomTitle = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDataObjectGridBottomTitle.setName("labelDataObjectGridBottomTitle");
            labelDataObjectGridBottomTitle.__internal_setGenerationDpi(96);
            labelDataObjectGridBottomTitle.registerTranslationHandler(translationHandler);
            labelDataObjectGridBottomTitle.setScaleForResolution(true);
            labelDataObjectGridBottomTitle.setMinimumWidth(10);
            labelDataObjectGridBottomTitle.setMinimumHeight(10);
            labelDataObjectGridBottomTitle.setPaddingBottom(2);
            labelDataObjectGridBottomTitle.setText("DataObjectGridBottomTitle");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelDataObjectGridBottomTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelDataObjectGridBottomTitleConstraints.setPosition("north");
            labelDataObjectGridBottomTitle.setConstraints(labelDataObjectGridBottomTitleConstraints);
            panelBottom.addChild(labelDataObjectGridBottomTitle);
            panelDataObjectGridBottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelDataObjectGridBottom.setName("panelDataObjectGridBottom");
            panelDataObjectGridBottom.__internal_setGenerationDpi(96);
            panelDataObjectGridBottom.registerTranslationHandler(translationHandler);
            panelDataObjectGridBottom.setScaleForResolution(true);
            panelDataObjectGridBottom.setMinimumWidth(10);
            panelDataObjectGridBottom.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelDataObjectGridBottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelDataObjectGridBottom.setLayout(panelDataObjectGridBottomLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelDataObjectGridBottomConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelDataObjectGridBottom.setConstraints(panelDataObjectGridBottomConstraints);
            panelBottom.addChild(panelDataObjectGridBottom);
            splitPane.addChild(panelBottom);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag splitPaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 2, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
            splitPane.setConstraints(splitPaneConstraints);
            mainPanel.addChild(splitPane);
            checkboxShowHistory = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxShowHistory.setName("checkboxShowHistory");
            checkboxShowHistory.__internal_setGenerationDpi(96);
            checkboxShowHistory.registerTranslationHandler(translationHandler);
            checkboxShowHistory.setScaleForResolution(true);
            checkboxShowHistory.setVisible(false);
            checkboxShowHistory.setText("!!Alle Stände");
            checkboxShowHistory.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    checkboxShowHistoryClicked(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxShowHistoryConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 2, 1, 0.0, 0.0, "w", "h", 0, 8, 8, 8);
            checkboxShowHistory.setConstraints(checkboxShowHistoryConstraints);
            mainPanel.addChild(checkboxShowHistory);
            checkboxRetailFilter = new de.docware.framework.modules.gui.controls.GuiCheckbox();
            checkboxRetailFilter.setName("checkboxRetailFilter");
            checkboxRetailFilter.__internal_setGenerationDpi(96);
            checkboxRetailFilter.registerTranslationHandler(translationHandler);
            checkboxRetailFilter.setScaleForResolution(true);
            checkboxRetailFilter.setMinimumWidth(10);
            checkboxRetailFilter.setMinimumHeight(10);
            checkboxRetailFilter.setVisible(false);
            checkboxRetailFilter.setText("!!Retailfilter");
            checkboxRetailFilter.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    checkboxRetailFilterClicked(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag checkboxRetailFilterConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "w", "n", 0, 8, 8, 4);
            checkboxRetailFilter.setConstraints(checkboxRetailFilterConstraints);
            mainPanel.addChild(checkboxRetailFilter);
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