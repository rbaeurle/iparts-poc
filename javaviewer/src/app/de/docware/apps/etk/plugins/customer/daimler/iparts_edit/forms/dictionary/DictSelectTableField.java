/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Dialog zum Selektieren einer Table und dem dazugehörigen MultiLang-Field
 */
public class DictSelectTableField extends AbstractJavaViewerForm implements EtkDbConst, iPartsConst {

    private static String[] forbiddenTablesStandard = new String[]{ TABLE_KATALOG, TABLE_SPRACHE, TABLE_ICONS, TABLE_STRUKT,
                                                                    TABLE_S_ITEMS, TABLE_ENUM, TABLE_S_SET, TABLE_ESTRUCT,
                                                                    TABLE_DA_DICT_TXTKIND };

    private List<String> forbiddenTables;
    private Map<String, List<String>> tableList;

    /**
     * Erzeugt eine Instanz von DictSelectTableField.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public DictSelectTableField(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                List<String> forbiddenTables) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        setForbiddenList(forbiddenTables);
        tableList = new HashMap<String, List<String>>();
        postCreateGui();
    }

    private void setForbiddenList(List<String> forbiddenTables) {
        this.forbiddenTables = forbiddenTables;
        if (forbiddenTables == null) {
            this.forbiddenTables = new DwList<String>();
        }
        for (int lfdNr = 0; lfdNr < forbiddenTablesStandard.length; lfdNr++) {
            if (!this.forbiddenTables.contains(forbiddenTablesStandard[lfdNr])) {
                this.forbiddenTables.add(forbiddenTablesStandard[lfdNr]);
            }
        }
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        Collection<EtkDatabaseTable> dbTableList = getConnector().getProject().getConfig().getDBDescription().getTableList();
        for (EtkDatabaseTable table : dbTableList) {
            if (!forbiddenTables.contains(table.getName())) {
                List<String> fieldNames = new DwList<String>();
                for (EtkDatabaseField dbField : table.getFieldList()) {
                    if (dbField.isMultiLanguage()) {
                        fieldNames.add(dbField.getName());
                    }
                }
                if (!fieldNames.isEmpty()) {
                    tableList.put(table.getName(), fieldNames);
                }
            }
        }
        Set<String> tables = tableList.keySet();
        List<String> sortedTables = new DwList<String>(tables.size());
        for (String str : tables) {
            sortedTables.add(str);
        }
        Collections.sort(sortedTables, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toUpperCase().compareTo(o2.toUpperCase());
            }
        });
        mainWindow.comboboxTable.switchOffEventListeners();
        mainWindow.comboboxTable.removeAllItems();
        for (String str : sortedTables) {
            mainWindow.comboboxTable.addItem(tableList.get(str), str);
        }
        mainWindow.comboboxTable.switchOnEventListeners();
        mainWindow.comboboxField.removeAllItems();
        enableButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
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

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public String getSelectedTableDotFieldName() {
        return TableAndFieldName.make(mainWindow.comboboxTable.getSelectedItem(), mainWindow.comboboxField.getSelectedItem());
    }

    private void onChangeTable(Event event) {
        List<String> fieldList = (List<String>)mainWindow.comboboxTable.getSelectedUserObject();
        mainWindow.comboboxField.removeAllItems();
        if (fieldList != null) {
            mainWindow.comboboxField.switchOffEventListeners();
            for (String str : fieldList) {
                mainWindow.comboboxField.addItem(str);
            }
            mainWindow.comboboxField.setSelectedIndex(-1);
            mainWindow.comboboxField.switchOnEventListeners();
        }
        enableButtons();
    }

    private void onChangeField(Event event) {
        enableButtons();
    }

    private void enableButtons() {
        boolean isSelected = (mainWindow.comboboxTable.getSelectedIndex() != -1) && (mainWindow.comboboxField.getSelectedIndex() != -1);
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isSelected);
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
        private de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel equaldimensionpanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTable;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelTable;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> comboboxTable;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelField;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelField;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> comboboxField;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(600);
            this.setHeight(180);
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
            equaldimensionpanel = new de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel();
            equaldimensionpanel.setName("equaldimensionpanel");
            equaldimensionpanel.__internal_setGenerationDpi(96);
            equaldimensionpanel.registerTranslationHandler(translationHandler);
            equaldimensionpanel.setScaleForResolution(true);
            equaldimensionpanel.setMinimumWidth(10);
            equaldimensionpanel.setMinimumHeight(10);
            equaldimensionpanel.setHorizontal(true);
            de.docware.framework.modules.gui.layout.LayoutAbsolute equaldimensionpanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutAbsolute();
            equaldimensionpanel.setLayout(equaldimensionpanelLayout);
            panelTable = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTable.setName("panelTable");
            panelTable.__internal_setGenerationDpi(96);
            panelTable.registerTranslationHandler(translationHandler);
            panelTable.setScaleForResolution(true);
            panelTable.setMinimumWidth(0);
            panelTable.setMinimumHeight(0);
            panelTable.setMaximumWidth(2147483647);
            panelTable.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutBorder panelTableLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelTable.setLayout(panelTableLayout);
            labelTable = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelTable.setName("labelTable");
            labelTable.__internal_setGenerationDpi(96);
            labelTable.registerTranslationHandler(translationHandler);
            labelTable.setScaleForResolution(true);
            labelTable.setMinimumWidth(10);
            labelTable.setMinimumHeight(10);
            labelTable.setPaddingTop(4);
            labelTable.setPaddingLeft(4);
            labelTable.setPaddingBottom(4);
            labelTable.setText("!!Tabelle");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelTableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelTableConstraints.setPosition("north");
            labelTable.setConstraints(labelTableConstraints);
            panelTable.addChild(labelTable);
            comboboxTable = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            comboboxTable.setName("comboboxTable");
            comboboxTable.__internal_setGenerationDpi(96);
            comboboxTable.registerTranslationHandler(translationHandler);
            comboboxTable.setScaleForResolution(true);
            comboboxTable.setMinimumWidth(0);
            comboboxTable.setMinimumHeight(0);
            comboboxTable.setMaximumWidth(2147483647);
            comboboxTable.setMaximumHeight(2147483647);
            comboboxTable.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeTable(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder comboboxTableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            comboboxTable.setConstraints(comboboxTableConstraints);
            panelTable.addChild(comboboxTable);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panelTableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, 0);
            panelTable.setConstraints(panelTableConstraints);
            equaldimensionpanel.addChild(panelTable);
            panelField = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelField.setName("panelField");
            panelField.__internal_setGenerationDpi(96);
            panelField.registerTranslationHandler(translationHandler);
            panelField.setScaleForResolution(true);
            panelField.setMinimumWidth(0);
            panelField.setMinimumHeight(0);
            panelField.setMaximumWidth(2147483647);
            panelField.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutBorder panelFieldLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelField.setLayout(panelFieldLayout);
            labelField = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelField.setName("labelField");
            labelField.__internal_setGenerationDpi(96);
            labelField.registerTranslationHandler(translationHandler);
            labelField.setScaleForResolution(true);
            labelField.setMinimumWidth(10);
            labelField.setMinimumHeight(10);
            labelField.setPaddingTop(4);
            labelField.setPaddingLeft(4);
            labelField.setPaddingBottom(4);
            labelField.setText("!!Feld");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelFieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelFieldConstraints.setPosition("north");
            labelField.setConstraints(labelFieldConstraints);
            panelField.addChild(labelField);
            comboboxField = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            comboboxField.setName("comboboxField");
            comboboxField.__internal_setGenerationDpi(96);
            comboboxField.registerTranslationHandler(translationHandler);
            comboboxField.setScaleForResolution(true);
            comboboxField.setMinimumWidth(0);
            comboboxField.setMinimumHeight(0);
            comboboxField.setMaximumWidth(2147483647);
            comboboxField.setMaximumHeight(2147483647);
            comboboxField.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeField(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder comboboxFieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            comboboxField.setConstraints(comboboxFieldConstraints);
            panelField.addChild(comboboxField);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panelFieldConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, 0);
            panelField.setConstraints(panelFieldConstraints);
            equaldimensionpanel.addChild(panelField);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder equaldimensionpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            equaldimensionpanel.setConstraints(equaldimensionpanelConstraints);
            panelMain.addChild(equaldimensionpanel);
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