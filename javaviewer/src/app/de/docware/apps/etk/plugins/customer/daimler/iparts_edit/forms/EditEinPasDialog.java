/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;

/**
 * Dialog zur Eingabe eines EinPas-Knotens
 */
public class EditEinPasDialog extends AbstractJavaViewerForm {

    private iPartsGuiEinPasPanel einPasPanel;

    /**
     * Methode zur Anzeige und Ausführung des Dialogs mit der skipOption
     *
     * @param dataConnector
     * @param parentForm
     * @param startEinPasId Vorbesetzung der EinPasId oder null
     * @param withSkip      true: der Cancel-Button zeigt 'Überspringen' und liefert ModalResult.OK
     *                      damit kann der Returnwert auch null sein
     * @return gültige EinPasId oder null (Vorsicht bei Option withSkip)
     */
    public static EinPasId showEinPasDialogWithSkipOption(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                          EinPasId startEinPasId, boolean withSkip) {
        EditEinPasDialog einPasDlg = new EditEinPasDialog(dataConnector, parentForm, null);
        if (withSkip) {
            einPasDlg.setCancelButtonText("!!Überspringen", ModalResult.OK);
        }
        einPasDlg.init(dataConnector.getProject());
        einPasDlg.setStartEinPasId(startEinPasId);
        if (einPasDlg.showModal() == ModalResult.OK) {
            return einPasDlg.getEinPasId();
        }
        return null;
    }

    /**
     * Methode zur Anzeige und Ausführung des Dialogs
     *
     * @param dataConnector
     * @param parentForm
     * @param startEinPasId Vorbesetzung der EinPasId oder null
     * @return gültige EinPasId oder null
     */
    public static EinPasId showEinPasDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                            EinPasId startEinPasId) {
        return showEinPasDialogWithSkipOption(dataConnector, parentForm, startEinPasId, false);
    }

    /**
     * Methode zur Anzeige und Ausführung des Dialogs mit skip
     *
     * @param dataConnector
     * @param parentForm
     * @param startEinPasId Vorbesetzung der EinPasId oder null
     * @return gültige EinPasId oder null
     */
    public static EinPasId showEinPasDialogWithSkip(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                    EinPasId startEinPasId) {
        return showEinPasDialogWithSkipOption(dataConnector, parentForm, startEinPasId, true);
    }

    /**
     * Erzeugt eine Instanz von EditEinPasDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditEinPasDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, EinPasId startEinPasId) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui(startEinPasId);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(EinPasId startEinPasId) {
        setTitle("!!EinPAS Verortung festlegen");
        einPasPanel = new iPartsGuiEinPasPanel();
        AbstractConstraints einPasConstraints = mainWindow.panel_Replacement.getConstraints();
        einPasPanel.setConstraints(einPasConstraints);
        einPasPanel.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                enableButtons();
            }
        });
        mainWindow.panelMain.removeChild(mainWindow.panel_Replacement);
        mainWindow.panelMain.addChild(einPasPanel);
        einPasPanel.setStartEinPasId(startEinPasId);

        enableButtons();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    /**
     * Überschreiben des Cancel-Button Textes
     *
     * @param text
     */
    public void setCancelButtonText(String text) {
        mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setText(text);
    }

    /**
     * Überschreiben des Cancel-Button Textes und des Modal-Results
     *
     * @param text
     * @param modalResult
     */
    public void setCancelButtonText(String text, ModalResult modalResult) {
        mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.CANCEL).setText(text);
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.CANCEL, modalResult);
    }

    /**
     * Initialisierung (da EtkProject gebraucht wird)
     *
     * @param project
     */
    public void init(EtkProject project) {
        einPasPanel.init(project);
        enableButtons();
    }

    public boolean isInit() {
        return einPasPanel.isInit();
    }

    public boolean isValid() {
        return einPasPanel.isValid();
    }

    public EinPasId getEinPasId() {
        return einPasPanel.getEinPasId();
    }

    public void setStartEinPasId(EinPasId startEinPasId) {
        einPasPanel.setStartEinPasId(startEinPasId);
    }

    private void enableButtons() {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isValid());
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
        private de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel equaldimensionpanel_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_HG;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_HG;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_G;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_G;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_TU;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_TU;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_Replacement;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(450);
            this.setHeight(200);
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
            panelMain.setPaddingTop(8);
            panelMain.setPaddingLeft(8);
            panelMain.setPaddingRight(8);
            panelMain.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            equaldimensionpanel_0 = new de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel();
            equaldimensionpanel_0.setName("equaldimensionpanel_0");
            equaldimensionpanel_0.__internal_setGenerationDpi(96);
            equaldimensionpanel_0.registerTranslationHandler(translationHandler);
            equaldimensionpanel_0.setScaleForResolution(true);
            equaldimensionpanel_0.setMinimumWidth(10);
            equaldimensionpanel_0.setMinimumHeight(20);
            equaldimensionpanel_0.setHorizontal(true);
            de.docware.framework.modules.gui.layout.LayoutAbsolute equaldimensionpanel_0Layout =
                    new de.docware.framework.modules.gui.layout.LayoutAbsolute();
            equaldimensionpanel_0.setLayout(equaldimensionpanel_0Layout);
            panel_HG = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_HG.setName("panel_HG");
            panel_HG.__internal_setGenerationDpi(96);
            panel_HG.registerTranslationHandler(translationHandler);
            panel_HG.setScaleForResolution(true);
            panel_HG.setMinimumWidth(0);
            panel_HG.setMinimumHeight(0);
            panel_HG.setMaximumWidth(2147483647);
            panel_HG.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_HGLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_HG.setLayout(panel_HGLayout);
            label_HG = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_HG.setName("label_HG");
            label_HG.__internal_setGenerationDpi(96);
            label_HG.registerTranslationHandler(translationHandler);
            label_HG.setScaleForResolution(true);
            label_HG.setMinimumWidth(10);
            label_HG.setMinimumHeight(10);
            label_HG.setText("!!Hauptgruppe");
            label_HG.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder label_HGConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            label_HG.setConstraints(label_HGConstraints);
            panel_HG.addChild(label_HG);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panel_HGConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, 0);
            panel_HG.setConstraints(panel_HGConstraints);
            equaldimensionpanel_0.addChild(panel_HG);
            panel_G = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_G.setName("panel_G");
            panel_G.__internal_setGenerationDpi(96);
            panel_G.registerTranslationHandler(translationHandler);
            panel_G.setScaleForResolution(true);
            panel_G.setMinimumWidth(0);
            panel_G.setMinimumHeight(0);
            panel_G.setMaximumWidth(2147483647);
            panel_G.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_GLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_G.setLayout(panel_GLayout);
            label_G = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_G.setName("label_G");
            label_G.__internal_setGenerationDpi(96);
            label_G.registerTranslationHandler(translationHandler);
            label_G.setScaleForResolution(true);
            label_G.setMinimumWidth(10);
            label_G.setMinimumHeight(10);
            label_G.setText("!!Gruppe");
            label_G.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder label_GConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            label_G.setConstraints(label_GConstraints);
            panel_G.addChild(label_G);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panel_GConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, 0);
            panel_G.setConstraints(panel_GConstraints);
            equaldimensionpanel_0.addChild(panel_G);
            panel_TU = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_TU.setName("panel_TU");
            panel_TU.__internal_setGenerationDpi(96);
            panel_TU.registerTranslationHandler(translationHandler);
            panel_TU.setScaleForResolution(true);
            panel_TU.setMinimumWidth(0);
            panel_TU.setMinimumHeight(0);
            panel_TU.setMaximumWidth(2147483647);
            panel_TU.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_TULayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_TU.setLayout(panel_TULayout);
            label_TU = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_TU.setName("label_TU");
            label_TU.__internal_setGenerationDpi(96);
            label_TU.registerTranslationHandler(translationHandler);
            label_TU.setScaleForResolution(true);
            label_TU.setMinimumWidth(10);
            label_TU.setMinimumHeight(10);
            label_TU.setText("!!Technischer Umfang");
            label_TU.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder label_TUConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            label_TU.setConstraints(label_TUConstraints);
            panel_TU.addChild(label_TU);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panel_TUConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, 0);
            panel_TU.setConstraints(panel_TUConstraints);
            equaldimensionpanel_0.addChild(panel_TU);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag equaldimensionpanel_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            equaldimensionpanel_0.setConstraints(equaldimensionpanel_0Constraints);
            panelMain.addChild(equaldimensionpanel_0);
            panel_Replacement = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_Replacement.setName("panel_Replacement");
            panel_Replacement.__internal_setGenerationDpi(96);
            panel_Replacement.registerTranslationHandler(translationHandler);
            panel_Replacement.setScaleForResolution(true);
            panel_Replacement.setMinimumWidth(10);
            panel_Replacement.setMinimumHeight(20);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_ReplacementLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_Replacement.setLayout(panel_ReplacementLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_ReplacementConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            panel_Replacement.setConstraints(panel_ReplacementConstraints);
            panelMain.addChild(panel_Replacement);
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