/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.util.StrUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Dialog zum Eintippen der DIALOG Baureihen für die Suche nach MAD Daten nach einem DIALOG-Import
 */
public class SearchMADDataAfterDIALOGImportForm extends AbstractJavaViewerForm {


    public static Set<String> getSeriesFromUser(AbstractJavaViewerForm owner) {
        SearchMADDataAfterDIALOGImportForm form = new SearchMADDataAfterDIALOGImportForm(owner);
        if (form.showModal() == ModalResult.OK) {
            return form.getSelectedSeriesNos();
        }
        return new HashSet<>();
    }

    private Set<String> getSelectedSeriesNos() {
        Set<String> result = new HashSet<>();
        String input = mainWindow.textfieldSeriesInput.getTrimmedText();
        if (StrUtils.isValid(input)) {
            input = input.toUpperCase();
            result.addAll(StrUtils.toStringList(input, ",", false, true));
        }
        return result;
    }


    /**
     * Erzeugt eine Instanz von DeleteMADDataAfterDIALOGImportForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public SearchMADDataAfterDIALOGImportForm(AbstractJavaViewerForm parentForm) {
        super(parentForm.getConnector(), parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.textfieldSeriesInput.requestFocus();
        mainWindow.pack();
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        close();
        return modalResult;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.mainPanel;
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
        private de.docware.framework.modules.gui.controls.GuiLabel labelSeries;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldSeriesInput;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelInfo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelInfoText;

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
            title.setTitle("!!MAD Daten nach DIALOG Import bereinigen");
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
            mainPanel.setMinimumWidth(10);
            mainPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            mainPanel.setLayout(mainPanelLayout);
            labelSeries = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelSeries.setName("labelSeries");
            labelSeries.__internal_setGenerationDpi(96);
            labelSeries.registerTranslationHandler(translationHandler);
            labelSeries.setScaleForResolution(true);
            labelSeries.setMinimumWidth(10);
            labelSeries.setMinimumHeight(10);
            labelSeries.setText("!!DIALOG Baureihen:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelSeriesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 8, 4, 4);
            labelSeries.setConstraints(labelSeriesConstraints);
            mainPanel.addChild(labelSeries);
            textfieldSeriesInput = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldSeriesInput.setName("textfieldSeriesInput");
            textfieldSeriesInput.__internal_setGenerationDpi(96);
            textfieldSeriesInput.registerTranslationHandler(translationHandler);
            textfieldSeriesInput.setScaleForResolution(true);
            textfieldSeriesInput.setMinimumWidth(200);
            textfieldSeriesInput.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldSeriesInputConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 8, 4, 4, 8);
            textfieldSeriesInput.setConstraints(textfieldSeriesInputConstraints);
            mainPanel.addChild(textfieldSeriesInput);
            panelInfo = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelInfo.setName("panelInfo");
            panelInfo.__internal_setGenerationDpi(96);
            panelInfo.registerTranslationHandler(translationHandler);
            panelInfo.setScaleForResolution(true);
            panelInfo.setMinimumWidth(10);
            panelInfo.setMinimumHeight(10);
            panelInfo.setTitle("Hinweis");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelInfoLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelInfo.setLayout(panelInfoLayout);
            labelInfoText = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelInfoText.setName("labelInfoText");
            labelInfoText.__internal_setGenerationDpi(96);
            labelInfoText.registerTranslationHandler(translationHandler);
            labelInfoText.setScaleForResolution(true);
            labelInfoText.setMinimumWidth(10);
            labelInfoText.setMinimumHeight(10);
            labelInfoText.setText("!!Baureihen bitte kommasepariert eingeben!");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelInfoTextConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 8, 8, 8, 8);
            labelInfoText.setConstraints(labelInfoTextConstraints);
            panelInfo.addChild(labelInfoText);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelInfoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 2, 1, 0.0, 0.0, "c", "h", 4, 8, 8, 8);
            panelInfo.setConstraints(panelInfoConstraints);
            mainPanel.addChild(panelInfo);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            this.addChild(mainPanel);
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