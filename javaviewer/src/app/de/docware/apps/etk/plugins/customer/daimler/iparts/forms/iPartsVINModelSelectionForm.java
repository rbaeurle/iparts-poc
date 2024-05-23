/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.iPartsVINModelMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

/**
 * Dialog für die Auswahl der Baumuster beim VIN-Baumuster Fallback
 */
public class iPartsVINModelSelectionForm {

    public static String showModelSelection(EtkProject project, String vinPrefix) {
        iPartsVINModelSelectionForm modelSelectionForm = new iPartsVINModelSelectionForm(project, vinPrefix);
        if (modelSelectionForm.showModal() == ModalResult.OK) {
            return modelSelectionForm.getSelectedModel();
        }
        return "";
    }

    private String vinPrefix = "";

    /**
     * Erzeugt eine Instanz von iPartsVINModelSelectionForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsVINModelSelectionForm(EtkProject project, String vinPrefix) {
        $$internalCreateGui$$(null);
        this.vinPrefix = vinPrefix;
        postCreateGui();
        setModels(project);
    }

    /**
     * Setzt die möglichen Baumuster
     *
     * @param project
     */
    private void setModels(EtkProject project) {
        mainWindow.comboboxModels.switchOffEventListeners();
        for (String model : iPartsVINModelMappingCache.getInstance(project).getVisibleModelsForVINPrefix(project, vinPrefix)) {
            String salesTitle = iPartsModel.getInstance(project, new iPartsModelId(model)).getModelSalesTitle(project).getTextByNearestLanguage(project.getDBLanguage(),
                                                                                                                                                project.getDataBaseFallbackLanguages());
            String itemValue = model + " - " + salesTitle;
            mainWindow.comboboxModels.addItem(model, itemValue);
        }
        mainWindow.comboboxModels.switchOnEventListeners();
    }

    public String getSelectedModel() {
        return (String)mainWindow.comboboxModels.getSelectedUserObject();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.labelInfo.setText(TranslationHandler.translate("!!* Zu der VIN \"%1\" wurde keine Datenkarte gefunden.", vinPrefix) + "\n" +
                                     TranslationHandler.translate("!!Bitte eines der möglichen Baumuster auswählen."));
        mainWindow.labelInfo.setForegroundColor(Colors.clBlue);

    }

    public ModalResult showModal() {
        mainWindow.pack();
        return mainWindow.showModal();
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelCombobox;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelModelSelect;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelModels;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<Object> comboboxModels;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelInfo;

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
            title.setTitle("!!VIN Fallback - Baumuster Auswahl");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelCombobox = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelCombobox.setName("panelCombobox");
            panelCombobox.__internal_setGenerationDpi(96);
            panelCombobox.registerTranslationHandler(translationHandler);
            panelCombobox.setScaleForResolution(true);
            panelCombobox.setMinimumWidth(10);
            panelCombobox.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelComboboxLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelCombobox.setLayout(panelComboboxLayout);
            panelModelSelect = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelModelSelect.setName("panelModelSelect");
            panelModelSelect.__internal_setGenerationDpi(96);
            panelModelSelect.registerTranslationHandler(translationHandler);
            panelModelSelect.setScaleForResolution(true);
            panelModelSelect.setMinimumWidth(10);
            panelModelSelect.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelModelSelectLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelModelSelect.setLayout(panelModelSelectLayout);
            labelModels = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelModels.setName("labelModels");
            labelModels.__internal_setGenerationDpi(96);
            labelModels.registerTranslationHandler(translationHandler);
            labelModels.setScaleForResolution(true);
            labelModels.setMinimumWidth(10);
            labelModels.setMinimumHeight(10);
            labelModels.setText("!!Baumuster: *");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelModelsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 8, 8, 4);
            labelModels.setConstraints(labelModelsConstraints);
            panelModelSelect.addChild(labelModels);
            comboboxModels = new de.docware.framework.modules.gui.controls.GuiComboBox<Object>();
            comboboxModels.setName("comboboxModels");
            comboboxModels.__internal_setGenerationDpi(96);
            comboboxModels.registerTranslationHandler(translationHandler);
            comboboxModels.setScaleForResolution(true);
            comboboxModels.setMinimumWidth(10);
            comboboxModels.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxModelsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "w", "h", 8, 4, 8, 8);
            comboboxModels.setConstraints(comboboxModelsConstraints);
            panelModelSelect.addChild(comboboxModels);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelModelSelectConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "w", "h", 0, 0, 0, 0);
            panelModelSelect.setConstraints(panelModelSelectConstraints);
            panelCombobox.addChild(panelModelSelect);
            labelInfo = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelInfo.setName("labelInfo");
            labelInfo.__internal_setGenerationDpi(96);
            labelInfo.registerTranslationHandler(translationHandler);
            labelInfo.setScaleForResolution(true);
            labelInfo.setMinimumWidth(10);
            labelInfo.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelInfoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "w", "n", 8, 8, 8, 8);
            labelInfo.setConstraints(labelInfoConstraints);
            panelCombobox.addChild(labelInfo);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelComboboxConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelCombobox.setConstraints(panelComboboxConstraints);
            this.addChild(panelCombobox);
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