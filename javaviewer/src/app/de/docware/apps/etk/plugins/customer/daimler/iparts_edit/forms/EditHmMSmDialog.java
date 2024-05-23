package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;

public class EditHmMSmDialog extends AbstractJavaViewerForm {

    public static HmMSmId showHmMSmDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          HmMSmId startHmMSmId) {
        EditHmMSmDialog hmMSmDlg = new EditHmMSmDialog(dataConnector, parentForm, null);
        hmMSmDlg.init(dataConnector.getProject(), new iPartsSeriesId(startHmMSmId.getSeries()));
        hmMSmDlg.setStartHmMSmId(startHmMSmId);
        if (hmMSmDlg.showModal() == ModalResult.OK) {
            return hmMSmDlg.getHmMSmId();
        }
        return null;
    }

    private iPartsGuiHmMSMPanel hmMSmPanel;

    /**
     * Erzeugt eine Instanz von EditHmMSmDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditHmMSmDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, HmMSmId startHmMSmId) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui(startHmMSmId);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(HmMSmId startHmMSmId) {
        setTitle("!!HmMSm Verortung festlegen");
        hmMSmPanel = new iPartsGuiHmMSMPanel();
        AbstractConstraints einPasConstraints = mainWindow.panel_Replacement.getConstraints();
        hmMSmPanel.setConstraints(einPasConstraints);
        hmMSmPanel.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                enableButtons();
            }
        });
        mainWindow.panelMain.removeChild(mainWindow.panel_Replacement);
        mainWindow.panelMain.addChild(hmMSmPanel);
        hmMSmPanel.setStartHmMSmId(startHmMSmId);

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
     * Initialisierung (da EtkProject gebraucht wird)
     *
     * @param project
     */
    public void init(EtkProject project, iPartsSeriesId seriesId) {
        hmMSmPanel.init(project, seriesId);
        enableButtons();
    }

    public boolean isInit() {
        return hmMSmPanel.isInit();
    }

    public boolean isValid() {
        return hmMSmPanel.isValid();
    }

    public HmMSmId getHmMSmId() {
        return hmMSmPanel.getHmMSmId();
    }

    public void setStartHmMSmId(HmMSmId startHmMSmId) {
        hmMSmPanel.setStartHmMSmId(startHmMSmId);
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
        private de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel equaldimensionpanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_HM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_HM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_M;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_M;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_SM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_SM;

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
            equaldimensionpanel = new de.docware.framework.modules.gui.controls.GuiEqualDimensionPanel();
            equaldimensionpanel.setName("equaldimensionpanel");
            equaldimensionpanel.__internal_setGenerationDpi(96);
            equaldimensionpanel.registerTranslationHandler(translationHandler);
            equaldimensionpanel.setScaleForResolution(true);
            equaldimensionpanel.setMinimumWidth(10);
            equaldimensionpanel.setMinimumHeight(20);
            equaldimensionpanel.setHorizontal(true);
            de.docware.framework.modules.gui.layout.LayoutAbsolute equaldimensionpanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutAbsolute();
            equaldimensionpanel.setLayout(equaldimensionpanelLayout);
            panel_HM = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_HM.setName("panel_HM");
            panel_HM.__internal_setGenerationDpi(96);
            panel_HM.registerTranslationHandler(translationHandler);
            panel_HM.setScaleForResolution(true);
            panel_HM.setMinimumWidth(0);
            panel_HM.setMinimumHeight(0);
            panel_HM.setMaximumWidth(2147483647);
            panel_HM.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_HMLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_HM.setLayout(panel_HMLayout);
            label_HM = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_HM.setName("label_HM");
            label_HM.__internal_setGenerationDpi(96);
            label_HM.registerTranslationHandler(translationHandler);
            label_HM.setScaleForResolution(true);
            label_HM.setMinimumWidth(10);
            label_HM.setMinimumHeight(10);
            label_HM.setText("!!Hauptmodul");
            label_HM.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder label_HMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            label_HM.setConstraints(label_HMConstraints);
            panel_HM.addChild(label_HM);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panel_HMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, -1, -1, -1, false);
            panel_HM.setConstraints(panel_HMConstraints);
            equaldimensionpanel.addChild(panel_HM);
            panel_M = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_M.setName("panel_M");
            panel_M.__internal_setGenerationDpi(96);
            panel_M.registerTranslationHandler(translationHandler);
            panel_M.setScaleForResolution(true);
            panel_M.setMinimumWidth(0);
            panel_M.setMinimumHeight(0);
            panel_M.setMaximumWidth(2147483647);
            panel_M.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_MLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_M.setLayout(panel_MLayout);
            label_M = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_M.setName("label_M");
            label_M.__internal_setGenerationDpi(96);
            label_M.registerTranslationHandler(translationHandler);
            label_M.setScaleForResolution(true);
            label_M.setMinimumWidth(10);
            label_M.setMinimumHeight(10);
            label_M.setText("!!Modul");
            label_M.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder label_MConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            label_M.setConstraints(label_MConstraints);
            panel_M.addChild(label_M);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panel_MConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, -1, -1, -1, false);
            panel_M.setConstraints(panel_MConstraints);
            equaldimensionpanel.addChild(panel_M);
            panel_SM = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_SM.setName("panel_SM");
            panel_SM.__internal_setGenerationDpi(96);
            panel_SM.registerTranslationHandler(translationHandler);
            panel_SM.setScaleForResolution(true);
            panel_SM.setMinimumWidth(0);
            panel_SM.setMinimumHeight(0);
            panel_SM.setMaximumWidth(2147483647);
            panel_SM.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_SMLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_SM.setLayout(panel_SMLayout);
            label_SM = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_SM.setName("label_SM");
            label_SM.__internal_setGenerationDpi(96);
            label_SM.registerTranslationHandler(translationHandler);
            label_SM.setScaleForResolution(true);
            label_SM.setMinimumWidth(10);
            label_SM.setMinimumHeight(10);
            label_SM.setText("!!Submodul");
            label_SM.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.CENTER);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder label_SMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            label_SM.setConstraints(label_SMConstraints);
            panel_SM.addChild(label_SM);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute panel_SMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsAbsolute(0, 0, 0, 0, -1, -1, -1, false);
            panel_SM.setConstraints(panel_SMConstraints);
            equaldimensionpanel.addChild(panel_SM);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag equaldimensionpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0);
            equaldimensionpanel.setConstraints(equaldimensionpanelConstraints);
            panelMain.addChild(equaldimensionpanel);
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