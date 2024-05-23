/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EditCreateMode;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSeriesCodesData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSeriesCodesDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSeriesCodesDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Formular für die Anzeige der X4E-Daten zu einer Baureihe
 */
public class EditSeriesCodesForm extends AbstractJavaViewerForm implements iPartsConst {

    public static void showSeriesCodesForSeries(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                iPartsSeriesId seriesId) {
        EditSeriesCodesForm dlg = new EditSeriesCodesForm(dataConnector, parentForm);
        dlg.setTitle(TranslationHandler.translate(iPartsConst.RELATED_INFO_SERIES_CODES_TEXT_MULTIPLE, seriesId.getSeriesNumber()));
        dlg.setSeriesId(seriesId);
        dlg.doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.SCALE_FROM_PARENT);
        dlg.showModal();
    }

    // Felder für speziellen Spaltenfilter
    private static final String[] SPECIAL_TABLEFILTER_FIELDS = new String[]{ FIELD_DSC_GROUP, FIELD_DSC_REGULATION,
                                                                             FIELD_DSC_CGKZ, FIELD_DSC_RFG, FIELD_DSC_DISTR };

    // normale Felder für Spaltenfilter
    private static String[] FILTERABLE_FIELDS = new String[]{ FIELD_DSC_POS, FIELD_DSC_POSV,
                                                              FIELD_DSC_AA, FIELD_DSC_STEERING,
                                                              FIELD_DSC_PRODUCT_GRP, FIELD_DSC_FEASIBILITY_COND };
    // 'unsichtbare' Felder wird aktuell nur verwendet wenn es keine Workbench Config gibt, deshalb können hierüber
    // auch die Default Displayfields abgedeckt werden
    private static String[] INVISIBLE_FIELDS = new String[]{ DBConst.FIELD_STAMP, FIELD_DSC_SERIES_NO,
                                                             FIELD_DSC_DISTR, FIELD_DSC_FED, FIELD_DSC_FEASIBILITY_COND,
                                                             FIELD_DSC_GLOBAL_CODE_SIGN, FIELD_DSC_EVENT_FROM, FIELD_DSC_EVENT_TO };

    private SeriesCodesFilterGrid grid;
    private iPartsSeriesId seriesId;
    private boolean showHistoricData;
    private boolean storeData;  // sollen die X4E-Daten zwischengespeichert werden?
    private List<iPartsSeriesCodesData> x4eList;
    private List<iPartsSeriesCodesData> x4eHistoricList;
    private Map<String, iPartsDataCode> codeMap;

    /**
     * Erzeugt eine Instanz von EditSeriesCodesForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditSeriesCodesForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.showHistoricData = false;
        this.x4eList = null;
        this.x4eHistoricList = null;
        this.codeMap = null;
        this.storeData = false;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        grid = new SeriesCodesFilterGrid(getConnector(), this);

        grid.setDisplayFields(buildDisplayFields());
        grid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelMain.addChild(grid.getGui());
        GuiPanel southPanel = createHistoricCheckBox();
        mainWindow.panelMain.addChild(southPanel);
    }

    private GuiPanel createHistoricCheckBox() {
        GuiPanel southPanel = new GuiPanel();
        southPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_SOUTH));
        southPanel.setLayout(new LayoutBorder());
        southPanel.setPaddingTop(4);
        final GuiCheckbox historicCheckBox = new GuiCheckbox();
        historicCheckBox.setText("!!Historische Daten anzeigen");
        historicCheckBox.setSelected(showHistoricData);
        historicCheckBox.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_WEST));
        historicCheckBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                toggleHistoricData(historicCheckBox.isSelected());
            }
        });
        southPanel.addChild(historicCheckBox);
        return southPanel;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        close();
        return modalResult;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES kind) {
        switch (kind) {
            case MAXIMIZE:
                Dimension screenSize = FrameworkUtils.getScreenSize();
                mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
                break;
            case SCALE_FROM_PARENT:
                if (parentForm != null) {
                    int height = parentForm.getGui().getParentWindow().getHeight();
                    int width = parentForm.getGui().getParentWindow().getWidth();
                    mainWindow.setSize(width - iPartsConst.CASCADING_WINDOW_OFFSET_WIDTH, height - iPartsConst.CASCADING_WINDOW_OFFSET_HEIGHT);
                }
                break;
        }
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(iPartsSeriesId seriesId) {
        this.seriesId = seriesId;
        if ((seriesId != null) && seriesId.isValidId()) {
            fillGridThreadSafeWithForm();
        } else {
            clearGrid();
        }
    }

    /**
     * Liefert das Grid zurück.
     *
     * @return
     */
    public GuiTable getTable() {
        return grid.getTable();
    }

    private void clearGrid() {
        grid.clearGrid();
        grid.showNoResultsLabel(true);
    }

    private void toggleHistoricData(boolean isChecked) {
        showHistoricData = isChecked;
        fillGridThreadSafeWithForm();
    }

    private void fillGridThreadSafeWithForm() {
        final EtkMessageLogForm progressForm = new EtkMessageLogForm("!!Daten laden", "!!Daten werden geladen", null, true);
        progressForm.disableButtons(true);
        progressForm.setMessagesTitle("");
        progressForm.getGui().setSize(300, 160);
        progressForm.showModal(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                fillGridThreadSafe(progressForm);
            }
        });
    }

    private void fillGridThreadSafe(final EtkMessageLogForm progressForm) {
        Session.invokeThreadSafeInSessionWithChildThread(() -> {
            GuiWindow.showWaitCursorForRootWindow(true);
            try {
                reloadGrid(true);
            } finally {
                GuiWindow.showWaitCursorForRootWindow(false);
                if (progressForm != null) {
                    // Autoclose
                    progressForm.getMessageLog().hideProgress();
                    progressForm.closeWindowIfNotAutoClose(ModalResult.OK);
                }
            }
        });
    }

    private void reloadGrid(boolean restoreFilter) {
        List<IdWithType> selectedIds = grid.getSelectedObjectIds(TABLE_DA_SERIES_CODES);

        int oldSortColumn = -1;
        boolean isSortAscending = getTable().isSortAscending();
        Map<Integer, Object> columnFilterValuesMap = null; // Map von Spalten-Indizes auf Filtertwert-Objekte
        AbstractGuiTableColumnFilterFactory copyColumnFilterFactory = null;
        if (getTable().getRowCount() > 0) {
            oldSortColumn = getTable().getSortColumn();
            if (restoreFilter) {
                columnFilterValuesMap = new HashMap<>(); // Map von Spalten-Indizes auf Filtertwert-Objekte
                copyColumnFilterFactory = grid.storeFilterFactory(columnFilterValuesMap);
            }
        }

        fillGrid();

        if (copyColumnFilterFactory != null) {
            grid.restoreFilterFactory(copyColumnFilterFactory, columnFilterValuesMap);
        }
        // Sortierung wiederherstellen falls vorher sortiert war
        if (getTable().isSortEnabled() && (oldSortColumn >= 0)) {
            getTable().sortRowsAccordingToColumn(oldSortColumn, isSortAscending);
        }
        grid.setSelectedObjectIds(selectedIds, TABLE_DA_SERIES_CODES);
    }


    private void clearGridAndFilter() {
        grid.getTable().clearAllFilterValues();
        grid.updateFilters();
        grid.clearGrid();
    }

    /**
     * Grid füllen
     */
    private void fillGrid() {
        grid.getTable().switchOffEventListeners();
        clearGridAndFilter();

        if ((seriesId == null) || !seriesId.isValidId()) {
            grid.getTable().switchOnEventListeners();
            grid.showNoResultsLabel(true);
            return;
        }

        if (codeMap == null) {
            // Daten aus DA_CODE laden
            codeMap = new HashMap<>();
            // DAIMLER-6328:
            // Lade alle Code, die allgemeingültig sind (keinen Bezug zur Baureihe) und alle Code zur Baureihe.
            iPartsDataCodeList codeList = iPartsDataCodeList.loadAllUniversalCodesAndAllCodesForSeries(getProject(), seriesId.getSeriesNumber(), iPartsImportDataOrigin.DIALOG);
            // Durchlaufe alle gefundenen Code und lege sie anhand ihrer ID und ihrer Produktgruppe in der Code-Map ab.
            // Weil baureihenbezogene Code nach den allgemeinen geladen werden, überschreiben die baureihenbezogenen Code
            // die allgemeinen beim Füllen der Code-Map. Das ist so gewollt, da baureihenbezogene Code spezifischer
            // (und somit genauer) sind.
            for (iPartsDataCode dataCode : codeList) {
                // DAIMLER-6328:
                // Code ablegen -> Schlüssel ist Code-ID und Produktgruppe
                String productGrp = dataCode.getAsId().getProductGroup();
                String codeId = dataCode.getAsId().getCodeId();
                codeMap.put(makeRegulationKey(codeId, productGrp), dataCode);
            }
        }
        List<iPartsSeriesCodesData> seriesCodesDataList = getSeriesCodesData();
        // falls es keine Daten aus DA_CODE gibt
        iPartsDataCode emptyCode = new iPartsDataCode(getProject(), null);
        emptyCode.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);

        for (iPartsSeriesCodesData seriesCodesData : seriesCodesDataList) {
            String completeRegulation = seriesCodesData.getRegulation();
            String productGrp = StrUtils.copySubString(completeRegulation, 1, 1);
            String regulationCode = iPartsSeriesCodesDataList.normalizeCode(completeRegulation);
            seriesCodesData.setFieldValue(FIELD_DSC_REGULATION, regulationCode,
                                          DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            // DAIMLER-6328:
            // Aus Code-ID und Produktgruppe den Schlüssel für das DA_CODE Objekt erzeugen
            String key = makeRegulationKey(regulationCode, productGrp);
            iPartsDataCode dataCode = codeMap.get(key);
            if (dataCode == null) {
                dataCode = emptyCode;
            }
            grid.addObjectToGrid(seriesCodesData, dataCode);
        }
        grid.getTable().switchOnEventListeners();
        grid.showNoResultsLabel(seriesCodesDataList.isEmpty());
    }

    private String makeRegulationKey(String codeId, String productGrp) {
        return codeId + "|" + productGrp;
    }

    private List<iPartsSeriesCodesData> getSeriesCodesData() {
        List<iPartsSeriesCodesData> seriesCodesDataList;
        if (showHistoricData) {
            if (x4eHistoricList == null) {
                List<iPartsSeriesCodesData> tempCodesDataList = iPartsSeriesCodesDataList.loadAllSeriesCodesDataForSeries(getProject(), seriesId.getSeriesNumber()).getAsList();
                x4eHistoricList = reorderHistoricData(tempCodesDataList);
            }
            seriesCodesDataList = x4eHistoricList;
            if (!storeData) {
                x4eHistoricList = null;
            }
        } else {
            if (x4eList == null) {
                x4eList = iPartsSeriesCodesDataList.loadCurrentValidSeriesCodesDataForSeries(getProject(), seriesId.getSeriesNumber()).getAsList();
            }
            seriesCodesDataList = x4eList;
            if (!storeData) {
                x4eList = null;
            }
        }
        return seriesCodesDataList;
    }

    private List<iPartsSeriesCodesData> reorderHistoricData(List<iPartsSeriesCodesData> tempCodesDataList) {
        List<iPartsSeriesCodesData> tempList = new DwList<>();
        List<iPartsSeriesCodesData> resultList = new DwList<>();
        String firstId = getSeriesCodesDataId(new iPartsSeriesCodesDataId());
        for (iPartsSeriesCodesData seriesCodesData : tempCodesDataList) {
            String scndId = getSeriesCodesDataId(seriesCodesData.getAsId());
            if (!firstId.equals(scndId)) {
                if (!tempList.isEmpty()) {
                    for (int i = tempList.size() - 1; i >= 0; i--) {
                        resultList.add(tempList.get(i));
                    }
                    tempList = new DwList<>();
                }
                firstId = scndId;
            }
            tempList.add(seriesCodesData);
        }
        if (!tempList.isEmpty()) {
            for (int i = tempList.size() - 1; i >= 0; i--) {
                resultList.add(tempList.get(i));
            }
        }
        return resultList;
    }

    private String getSeriesCodesDataId(iPartsSeriesCodesDataId id) {
        iPartsSeriesCodesDataId helperId = id.getIdWithoutSDatA();
        return helperId.toTabString();
    }

    /**
     * Display-Fields bestimmen
     *
     * @return
     */
    private EtkDisplayFields buildDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(getConfig(), iPartsEditConfigConst.iPARTS_EDIT_SERIES_CODE_KEY);
        if (displayFields.size() == 0) {
            String tableName = iPartsConst.TABLE_DA_SERIES_CODES;
            List<String> invisibleFieldList = new DwList<>(INVISIBLE_FIELDS);
            List<String> filterableFieldList = new DwList<>(StrUtils.mergeArrays(FILTERABLE_FIELDS, SPECIAL_TABLEFILTER_FIELDS));
            EtkDatabaseTable table = getProject().getConfig().getDBDescription().getTable(tableName);
            for (EtkDatabaseField field : table.getFieldList()) {
                if (!invisibleFieldList.contains(field.getName())) {
                    EtkDisplayField displayField = new EtkDisplayField(tableName, field.getName(), field.isMultiLanguage(), field.isArray());
                    displayField.setColumnFilterEnabled(filterableFieldList.contains(field.getName()));
                    displayFields.addFeld(displayField);
                    if (field.getName().equals(FIELD_DSC_REGULATION)) {
                        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_DESC, true, false);
                        //displayField.setColumnFilterEnabled(filterableFieldList.contains(field.getName()));
                        displayFields.addFeld(displayField);
                    }
                }

            }
            displayFields.loadStandards(getConfig());
        }
        return displayFields;
    }

    private class SeriesCodesFilterGrid extends EditDataObjectFilterGrid {

        public SeriesCodesFilterGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
            setColumnFilterFactory(new SeriesCodesColumnFilterFactory(getProject()));
        }

        protected class SeriesCodesColumnFilterFactory extends DataObjectColumnFilterFactory {

            public SeriesCodesColumnFilterFactory(EtkProject project) {
                super(project);
            }

            @Override
            protected boolean changeColumnTableFilterValues(int column, EditControlFactory editControl) {
                if (editControl.getValues().editCreateMode == EditCreateMode.ecmTableColumnFilter) {
                    String fieldName = editControl.getFieldName();
                    java.util.List<String> filterNames = new DwList<>(SPECIAL_TABLEFILTER_FIELDS);
                    if (filterNames.contains(fieldName)) {
                        // Trick um im Tabellenfilter ein SetOfEnum Eingabefeld zu erzeugen, dass als Tokens
                        // die Werte aus der zugehörigen Spalte der Tabelle enthält
                        editControl.getValues().field = editControl.getField().cloneMe();  // zur Sicherheit, damit die Originalwerte sich nicht ändern
                        editControl.getValues().field.setType(EtkFieldType.feSetOfEnum);  // behaupte, das Feld ist ein SetOfEnum
                        editControl.getOptions().handleAsSetOfEnum = true;  // und soll als SetOfEnum behandelt werden
                        editControl.getOptions().searchDisjunctive = true;
                        // alles weitere übernimmt EditControlFactory und das FilterInterface
                        AbstractGuiControl guiCtrl = EditControlFactory.doCreateEnumCheckBoxForTableColumnFilter(editControl.getValues(), editControl.getOptions());
                        if (guiCtrl != null) {
                            editControl.setControl(guiCtrl);
                        }
                    }
                }
                return super.changeColumnTableFilterValues(column, editControl);
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

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
            panelMain.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
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
            buttonpanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
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