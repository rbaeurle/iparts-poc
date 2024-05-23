/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSeriesEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSeriesEventList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditSeriesEventsForm extends AbstractJavaViewerForm implements iPartsConst {

    public static final String[] SPECIAL_TABLEFILTER_FIELDS = new String[]{ FIELD_DSE_EVENT_ID, FIELD_DSE_PREVIOUS_EVENT_ID };
    private static String[] FILTERABLE_FIELDS = new String[]{ FIELD_DSE_CONV_RELEVANT,
                                                              FIELD_DSE_STATUS, FIELD_DSE_CODES };
    private static String[] INVISIBLE_FIELDS = new String[]{ FIELD_STAMP, FIELD_DSE_SERIES_NO, FIELD_DSE_SDATB };

    public static void showSeriesEventsForSeries(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 iPartsSeriesId seriesId) {
        EditSeriesEventsForm dlg = new EditSeriesEventsForm(dataConnector, parentForm);
        dlg.setTitle(TranslationHandler.translate("!!Ereigniskette zur Baureihe %1", seriesId.getSeriesNumber()));
        dlg.setSeriesId(seriesId);
        dlg.doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.SCALE_FROM_PARENT);
        dlg.showModal();
    }

    private EditDataObjectFilterGrid grid;
    private iPartsSeriesId seriesId;

    /**
     * Erzeugt eine Instanz von EditSeriesEventsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditSeriesEventsForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        grid = new EditDataObjectFilterGrid(getConnector(), this);
        grid.setDisplayFields(buildDisplayFields());
        grid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelMain.addChild(grid.getGui());
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
            fillGrid();
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

    /**
     * Grid füllen
     */
    private void fillGrid() {
        // Zum Wiederherstellen der Sortierung
        int oldSortColumn = -1;
        boolean isSortAscending = grid.getTable().isSortAscending();
        Map<Integer, Object> columnFilterValuesMap = null; // Map von Spalten-Indizes auf Filtertwert-Objekte
        AbstractGuiTableColumnFilterFactory columnFilterFactory = null;
        if (grid.getTable().getRowCount() > 0) {
            oldSortColumn = grid.getTable().getSortColumn();
            columnFilterValuesMap = new HashMap<Integer, Object>(); // Map von Spalten-Indizes auf Filtertwert-Objekte
            columnFilterFactory = grid.storeFilterFactory(columnFilterValuesMap);
        }
        grid.clearGrid();
        List<iPartsDataSeriesEvent> seriesEventGraph = iPartsDataSeriesEventList.loadEventGraphForSeries(getProject(),
                                                                                                         seriesId.getSeriesNumber(),
                                                                                                         true);
        int counter = 1;
        for (iPartsDataSeriesEvent seriesEventData : seriesEventGraph) {
            // virtuelles Feld + Wert hinzufügen
            DBDataObjectAttributes attributes = seriesEventData.getAttributes();
            attributes.addField(iPartsDataVirtualFieldsDefinition.DSE_LFDNR, StrUtils.leftFill(String.valueOf(counter), 3, '0'), true, DBActionOrigin.FROM_DB);
            seriesEventData.setAttributes(attributes, DBActionOrigin.FROM_DB);
            grid.addObjectToGrid(seriesEventData);
            counter++;
        }
        grid.showNoResultsLabel(seriesEventGraph.isEmpty());

        // Filterung wiederherstellen, falls vorher gefiltert war.
        if (columnFilterFactory != null) {
            grid.restoreFilterFactory(columnFilterFactory, columnFilterValuesMap);
        }
        // Sortierung wiederherstellen falls vorher sortiert war.
        if (grid.getTable().isSortEnabled() && (oldSortColumn >= 0)) {
            grid.getTable().sortRowsAccordingToColumn(oldSortColumn, isSortAscending);
        }
    }

    /**
     * Display-Fields bestimmen
     *
     * @return
     */
    private EtkDisplayFields buildDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_SERIES_EVENTS_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            String tableName = iPartsConst.TABLE_DA_SERIES_EVENTS;
            List<String> invisibleFieldList = new DwList<>(INVISIBLE_FIELDS);
            List<String> filterableFieldList = new DwList<>(StrUtils.mergeArrays(FILTERABLE_FIELDS, SPECIAL_TABLEFILTER_FIELDS));
            EtkDatabaseTable table = getProject().getConfig().getDBDescription().getTable(tableName);
            EtkDisplayField displayField = new EtkDisplayField(tableName, iPartsDataVirtualFieldsDefinition.DSE_LFDNR, false, false);
            displayField.setDefaultText(false);
            EtkMultiSprache multi = new EtkMultiSprache();
            multi.setText(getProject().getDBLanguage(), "!!Nr");
            displayField.setText(multi);
            displayFields.addFeld(displayField);
            for (EtkDatabaseField field : table.getFieldList()) {
                if (!invisibleFieldList.contains(field.getName())) {
                    displayField = new EtkDisplayField(tableName, field.getName(), field.isMultiLanguage(), field.isArray());
                    displayField.setColumnFilterEnabled(filterableFieldList.contains(field.getName()));
                    displayFields.addFeld(displayField);
                }
            }

            displayFields.loadStandards(getConfig());
        }
        return displayFields;
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