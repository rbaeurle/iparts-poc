/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.DatacardIdentOrderElem;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableHeader;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.util.List;

public class iPartsShowAggIdentDialog extends AbstractJavaViewerForm {

    public static void showIdentMembers(AbstractJavaViewerForm parentForm, DatacardIdentOrderElem identOrderElem) {

        iPartsShowAggIdentDialog dlg = new iPartsShowAggIdentDialog(parentForm.getConnector(), parentForm,
                                                                    identOrderElem.getAggregateIdent().getDisplayGridValues());
        dlg.setWindowTitle(identOrderElem.getDescription(),
                           TranslationHandler.translate("!!Gruppierter Ident: \"%1\"", identOrderElem.getAggregateIdent().getFormattedIdent()));
        dlg.showModal();
    }

    private static final String[] HEADERTEXT = new String[]{ "!!Bedeutung", "!!Wert", "!!Weitere Erklärung" };

    protected List<String[]> displayGridValues; // anzuzeigende Informationen zum Ident

    /**
     * Erzeugt eine Instanz von iPartsShowAggIdentDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsShowAggIdentDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, List<String[]> displayGridValues) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.displayGridValues = displayGridValues;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        addCopyContextMenuItem();
        getTable().setContextMenu(contextMenuTable);
        setGridHeader();
        dataToGrid();
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public List<String[]> getDisplayGridValues() {
        return displayGridValues;
    }

    public void setDisplayGridValues(List<String[]> displayGridValues) {
        this.displayGridValues = displayGridValues;
        dataToGrid();
    }

    public GuiTable getTable() {
        return mainWindow.table;
    }

    public void setWindowTitle(String windowTitle, String subTitle) {
        mainWindow.setTitle(windowTitle);
        mainWindow.title.setTitle(subTitle);
    }

    public void clear() {
        clearGrid();
        setGridHeader();
    }

    /**
     * Entfernt alle Zeilen aus dem Grid.
     */
    public void clearGrid() {
        getTable().removeRows();
    }

    /**
     * Fügt dem Kontextmenü den Standard-Kontextmenüeintrag für das Kopieren des Inhalts hinzu.
     */
    public void addCopyContextMenuItem() {
        if (!contextMenuTable.getChildren().isEmpty()) {
            GuiSeparator separator = new GuiSeparator();
            separator.setName("menuSeparator");
            contextMenuTable.addChild(separator);
        }
        ToolbarButtonMenuHelper toolbarHelper = new ToolbarButtonMenuHelper(getConnector(), null);
        GuiMenuItem menuItemCopy = toolbarHelper.createCopyMenuForTable(getTable(), getUITranslationHandler());
        contextMenuTable.addChild(menuItemCopy);
    }

    protected void setGridHeader() {
        GuiTableHeader tableHeader = new GuiTableHeader();
        for (String header : HEADERTEXT) {
            GuiLabel label = new GuiLabel(header);
            tableHeader.addChild(label);
        }
        getTable().setHeader(tableHeader);
    }

    protected void dataToGrid() {
        clearGrid();
        if ((displayGridValues != null) && !displayGridValues.isEmpty()) {
            for (String[] rowValues : displayGridValues) {
                List<String> rowElems = StrUtils.toStringArrayList(rowValues);

                // Länge der Daten an Grid angleichen
                while (rowElems.size() < getTable().getHeader().size()) {
                    rowElems.add("");
                }
                while (rowElems.size() > getTable().getHeader().size()) {
                    rowElems.remove(rowElems.size() - 1);
                }

                GuiTableRow row = new GuiTableRow();
                for (String text : rowElems) {
                    GuiLabel label = new GuiLabel(text);
                    row.addChild(label);
                }
                getTable().addRow(row);
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
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextMenuTable;

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable table;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextMenuTable = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextMenuTable.setName("contextMenuTable");
            contextMenuTable.__internal_setGenerationDpi(96);
            contextMenuTable.registerTranslationHandler(translationHandler);
            contextMenuTable.setScaleForResolution(true);
            contextMenuTable.setMinimumWidth(10);
            contextMenuTable.setMinimumHeight(10);
            contextMenuTable.setMenuName("contextMenuTable");
            contextMenuTable.setParentControl(this);
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setName("ShowAggIdentDialog");
            this.setWidth(600);
            this.setHeight(350);
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            scrollpane = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpane.setName("scrollpane");
            scrollpane.__internal_setGenerationDpi(96);
            scrollpane.registerTranslationHandler(translationHandler);
            scrollpane.setScaleForResolution(true);
            scrollpane.setMinimumWidth(10);
            scrollpane.setMinimumHeight(10);
            table = new de.docware.framework.modules.gui.controls.table.GuiTable();
            table.setName("table");
            table.__internal_setGenerationDpi(96);
            table.registerTranslationHandler(translationHandler);
            table.setScaleForResolution(true);
            table.setMinimumWidth(10);
            table.setMinimumHeight(10);
            table.setHtmlTablePageSplitMode(HtmlTablePageSplitMode.NO_SPLIT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tableConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            table.setConstraints(tableConstraints);
            scrollpane.addChild(table);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpane.setConstraints(scrollpaneConstraints);
            panelMain.addChild(scrollpane);
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