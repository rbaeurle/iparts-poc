/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.RowContentForAutoTransferToAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.TransferToASElement;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// Code analog zu EditTransferPartlistEntriesWithPredictionForm, nur stark vereinfacht
public class EditAutoTransferPartlistEntriesForm extends AbstractJavaViewerForm implements iPartsConst {

    public static List<TransferToASElement> doAutoTransferToASPartlist(AbstractJavaViewerFormIConnector connector, AbstractJavaViewerForm parentForm,
                                                                       Map<String, List<TransferToASElement>> moduleToTransferElementsMap,
                                                                       iPartsProduct masterProduct, VarParam<Boolean> openModulesInEdit) {
        EditAutoTransferPartlistEntriesForm form = new EditAutoTransferPartlistEntriesForm(connector, parentForm);
        form.init(moduleToTransferElementsMap);
        if (masterProduct != null) {
            openModulesInEdit.setValue(false);
            return form.getAllTransferItems();
        }
        if (form.showModal() == ModalResult.OK) {
            openModulesInEdit.setValue(form.isShowModuleSelected());
            return form.getAllTransferItems();
        } else {
            return null;
        }
    }

    private EditAutoTransferPartlistGrid grid;

    /**
     * Erzeugt eine Instanz von EditAutoTransferPartlistEntriesForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditAutoTransferPartlistEntriesForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
        getGui().setName("AutoTransferToASForm");
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    protected void postCreateGui() {
        grid = new EditAutoTransferPartlistGrid(getConnector(), this, EditAutoTransferPartlistGrid.TABLE_PSEUDO);

        mainWindow.contentTablePanel.addChildBorderCenter(grid.getGui());
        mainWindow.setSize(600, 500);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow;
    }

    public GuiPanel getContentTablePanel() {
        return mainWindow.contentTablePanel;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.updateData(sender, forceUpdateAll);
        enableButtons();
    }

    public boolean isShowModuleSelected() {
        return mainWindow.checkboxShowModule.isSelected();
    }

    public void setShowModuleSelected(boolean isSelected) {
        mainWindow.checkboxShowModule.setSelected(isSelected);
    }

    public List<TransferToASElement> getAllTransferItems() {
        return grid.getAllTransferItems();
    }

    public void init(Map<String, List<TransferToASElement>> moduleToTransferElementsMap) {
        // Checkbox TU öffnen soll selektiert sein
        setShowModuleSelected(true);

        List<RowContentForAutoTransferToAS> rowContents = new ArrayList<>(moduleToTransferElementsMap.size());
        for (Map.Entry<String, List<TransferToASElement>> transferEntry : moduleToTransferElementsMap.entrySet()) {
            RowContentForAutoTransferToAS rowContent = new RowContentForAutoTransferToAS();
            rowContent.setAssemblyId(new AssemblyId(transferEntry.getKey(), ""));
            rowContent.setTransferElements(transferEntry.getValue());
            rowContent.setTransferMark(true); // Initial alle übernehmen
            rowContents.add(rowContent);
        }

        fillGrid(rowContents);
    }

    protected void fillGrid(Collection<RowContentForAutoTransferToAS> rowContents) {
        grid.switchOffEventListeners();

        // Tabelle mit Inhalt füllen
        for (RowContentForAutoTransferToAS rowContent : rowContents) {
            grid.addToGrid(rowContent);
        }
        grid.getEntries(); // Zum Merken der ungefilterten Inhalte

        grid.switchOnEventListeners();
        grid.showNoResultsLabel(grid.getTable().getRowCount() == 0, false);
    }

    public ModalResult showModal() {
        ModalResult result = mainWindow.showModal();
        dispose();
        return result;
    }

    private void onOKbuttonClick(Event event) {
        List<TransferToASElement> allTransferItems = grid.getAllTransferItems();
        if (!allTransferItems.isEmpty()) {

            // Prüfen, ob zur Übernahme markierte Zeilen ausgeblendet sind
            List<TransferToASElement> allVisibleTransferItems = grid.getAllVisibleTransferItems();
            if (allVisibleTransferItems.size() != allTransferItems.size()) {
                String msg = TranslationHandler.translate("!!Es sind gefilterte Zeilen zur Übernahme markiert.") + "\n\n" +
                             TranslationHandler.translate("!!Trotzdem fortfahren?");
                ModalResult modalResult = MessageDialog.showYesNo(msg, "!!Übernahme");
                if (modalResult == ModalResult.NO) {
                    return;
                }
            }

            mainWindow.setModalResult(ModalResult.OK);
            mainWindow.setVisible(false);
        }
    }

    private void enableButtons() {
        if (grid != null) {
            List<TransferToASElement> transferItems = grid.getAllTransferItems();
            mainWindow.buttonPanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, !transferItems.isEmpty());
        }
    }

    public void setGrid(EditAutoTransferPartlistGrid grid) {
        this.grid = grid;
    }

    public EditAutoTransferPartlistGrid getGrid() {
        return grid;
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