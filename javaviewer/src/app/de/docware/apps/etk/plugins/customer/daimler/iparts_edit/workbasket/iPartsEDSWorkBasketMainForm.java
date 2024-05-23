/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.MasterDataEDSWorkBasketKEMForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.MasterDataMBSWorkBasketKEMForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket.MasterDataCTTWorkBasketForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket.MasterDataEDSWorkBasketForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket.MasterDataMBSWorkBasketForm;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonImages;

public class iPartsEDSWorkBasketMainForm extends AbstractJavaViewerMainFormContainer {

    // da die Toolbars nach Alias sortiert werden, muss ALIAS mit iPartsEditWork beginnen, damit hinter iPartsEditWork
    public static final String MAIN_TOOLBAR_ALIAS = "iParts3EditWorkEDSWorkBasket";

    private MasterDataEDSWorkBasketForm dlgEdsSaa;
    private MasterDataEDSWorkBasketKEMForm dlgEdsKEM;

    private MasterDataMBSWorkBasketForm dlgMbsSaa;
    private MasterDataMBSWorkBasketKEMForm dlgMbsKEM;

    private MasterDataCTTWorkBasketForm dlgCttSaa;

    private MasterDataSaaKemConstMissingForm dlgSaaKemMissing;

    /**
     * Erzeugt eine Instanz von iPartsEDSWorkBasketMainForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsEDSWorkBasketMainForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        dlgEdsSaa = MasterDataEDSWorkBasketForm.getNewEDSWorkBasketInstance(getConnector(), parentForm);
        mainWindow.contentPanel.addChildBorderCenter(dlgEdsSaa.getGui());

        dlgEdsKEM = MasterDataEDSWorkBasketKEMForm.getNewEDSKEMWorkBasketInstance(getConnector(), parentForm);
        mainWindow.contentPanelKEM.addChildBorderCenter(dlgEdsKEM.getGui());

        dlgMbsSaa = MasterDataMBSWorkBasketForm.getNewMBSWorkBasketInstance(getConnector(), parentForm);
        mainWindow.contentPanelMBS_SAA.addChildBorderCenter(dlgMbsSaa.getGui());

        dlgMbsKEM = MasterDataMBSWorkBasketKEMForm.getNewMBSKEMWorkBasketInstance(getConnector(), parentForm);
        mainWindow.contentPanelMBS_KEM.addChildBorderCenter(dlgMbsKEM.getGui());

        dlgCttSaa = MasterDataCTTWorkBasketForm.getNewCTTWorkBasketInstance(getConnector(), parentForm);
        mainWindow.contentPanelCTT.addChildBorderCenter(dlgCttSaa.getGui());

        dlgSaaKemMissing = new MasterDataSaaKemConstMissingForm(getConnector(), parentForm);
        mainWindow.tabbedpaneentrySAA_KEM_Diff.setTitle("!!Fehlende Konstruktions-Elemente für SAA/KEM");
        mainWindow.tabbedpaneentrySAA_KEM_Diff.addChild(dlgSaaKemMissing.getGui());
//        mainWindow.tabbedpaneentrySAA_KEM_Diff.removeFromParent();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.mainPanel;
    }

    @Override
    public boolean isSecondToolbarVisible() {
        return false;
    }

    @Override
    public void activeFormChanged(AbstractJavaViewerForm newActiveForm, AbstractJavaViewerForm lastActiveForm) {
        if (newActiveForm == this) {
            if (dlgEdsSaa != null) {

            }
        }
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (forceUpdateAll || getConnector().isFlagUserInterfaceLanguageChanged() || getConnector().isFlagDatabaseLanguageChanged()) {
//            dlg.updateData();
        }
    }

    public ToolButtonImages getMainToolbarIcon() {
        boolean isResponsive = DWLayoutManager.get().isResponsiveMode();
        return new ToolButtonImages(isResponsive ? EditDefaultImages.edit_workBasket_ToolbarButtonGray.getImage()
                                                 : EditDefaultImages.edit_workBasket_ToolbarButton.getImage(),
                                    isResponsive ? EditDefaultImages.edit_workBasket_ToolbarButton.getImage()
                                                 : null,
                                    isResponsive ? EditDefaultImages.edit_workBasket_ToolbarButton.getImage()
                                                 : EditDefaultImages.edit_workBasket_ToolbarButtonWhite.getImage(),
                                    null);
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
        private de.docware.framework.modules.gui.controls.GuiTabbedPane tabbedpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentrySAA;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedpaneentrySAA_content;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel topPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel contentPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel bottomPanel;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentryKEM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedpaneentryKEM_content;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel topPanelKEM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel contentPanelKEM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel bottomPanelKEM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentryMBS_SAA;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedpaneentryMBS_SAA_content;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel topPanelMBS_SAA;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel contentPanelMBS_SAA;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel bottomPanelMBS_SAA;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentryMBS_KEM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedpaneentryMBS_KEM_content;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel topPanelMBS_KEM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel contentPanelMBS_KEM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel bottomPanelMBS_KEM;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentryCTT;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedpaneentryCTT_content;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel topPanelCTT;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel contentPanelCTT;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel bottomPanelCTT;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry tabbedpaneentrySAA_KEM_Diff;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel tabbedpaneentrySAA_KEM_Diff_content;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel topPanelSAA_KEM_Diff;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel contentPanelSAA_KEM_Diff;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel bottomPanelSAA_KEM_Diff;

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
            title.setTitle("!!Truck Arbeitsvorrat");
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
            mainPanel.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder mainPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            mainPanel.setLayout(mainPanelLayout);
            tabbedpane = new de.docware.framework.modules.gui.controls.GuiTabbedPane();
            tabbedpane.setName("tabbedpane");
            tabbedpane.__internal_setGenerationDpi(96);
            tabbedpane.registerTranslationHandler(translationHandler);
            tabbedpane.setScaleForResolution(true);
            tabbedpane.setMinimumWidth(10);
            tabbedpane.setMinimumHeight(10);
            tabbedpaneentrySAA = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentrySAA.setName("tabbedpaneentrySAA");
            tabbedpaneentrySAA.__internal_setGenerationDpi(96);
            tabbedpaneentrySAA.registerTranslationHandler(translationHandler);
            tabbedpaneentrySAA.setScaleForResolution(true);
            tabbedpaneentrySAA.setMinimumWidth(10);
            tabbedpaneentrySAA.setMinimumHeight(10);
            tabbedpaneentrySAA.setTitle("!!EDS SAA Arbeitsvorrat");
            tabbedpaneentrySAA_content = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedpaneentrySAA_content.setName("tabbedpaneentrySAA_content");
            tabbedpaneentrySAA_content.__internal_setGenerationDpi(96);
            tabbedpaneentrySAA_content.registerTranslationHandler(translationHandler);
            tabbedpaneentrySAA_content.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedpaneentrySAA_contentLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedpaneentrySAA_content.setLayout(tabbedpaneentrySAA_contentLayout);
            topPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            topPanel.setName("topPanel");
            topPanel.__internal_setGenerationDpi(96);
            topPanel.registerTranslationHandler(translationHandler);
            topPanel.setScaleForResolution(true);
            topPanel.setMinimumWidth(10);
            topPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder topPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            topPanel.setLayout(topPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder topPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            topPanelConstraints.setPosition("north");
            topPanel.setConstraints(topPanelConstraints);
            tabbedpaneentrySAA_content.addChild(topPanel);
            contentPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            contentPanel.setName("contentPanel");
            contentPanel.__internal_setGenerationDpi(96);
            contentPanel.registerTranslationHandler(translationHandler);
            contentPanel.setScaleForResolution(true);
            contentPanel.setMinimumWidth(10);
            contentPanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder contentPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            contentPanel.setLayout(contentPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder contentPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            contentPanel.setConstraints(contentPanelConstraints);
            tabbedpaneentrySAA_content.addChild(contentPanel);
            bottomPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
            bottomPanel.setName("bottomPanel");
            bottomPanel.__internal_setGenerationDpi(96);
            bottomPanel.registerTranslationHandler(translationHandler);
            bottomPanel.setScaleForResolution(true);
            bottomPanel.setMinimumWidth(10);
            bottomPanel.setMinimumHeight(10);
            bottomPanel.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder bottomPanelLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            bottomPanel.setLayout(bottomPanelLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder bottomPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            bottomPanelConstraints.setPosition("south");
            bottomPanel.setConstraints(bottomPanelConstraints);
            tabbedpaneentrySAA_content.addChild(bottomPanel);
            tabbedpaneentrySAA.addChild(tabbedpaneentrySAA_content);
            tabbedpane.addChild(tabbedpaneentrySAA);
            tabbedpaneentryKEM = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentryKEM.setName("tabbedpaneentryKEM");
            tabbedpaneentryKEM.__internal_setGenerationDpi(96);
            tabbedpaneentryKEM.registerTranslationHandler(translationHandler);
            tabbedpaneentryKEM.setScaleForResolution(true);
            tabbedpaneentryKEM.setMinimumWidth(10);
            tabbedpaneentryKEM.setMinimumHeight(10);
            tabbedpaneentryKEM.setTitle("!!EDS KEM Arbeitsvorrat");
            tabbedpaneentryKEM_content = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedpaneentryKEM_content.setName("tabbedpaneentryKEM_content");
            tabbedpaneentryKEM_content.__internal_setGenerationDpi(96);
            tabbedpaneentryKEM_content.registerTranslationHandler(translationHandler);
            tabbedpaneentryKEM_content.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedpaneentryKEM_contentLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedpaneentryKEM_content.setLayout(tabbedpaneentryKEM_contentLayout);
            topPanelKEM = new de.docware.framework.modules.gui.controls.GuiPanel();
            topPanelKEM.setName("topPanelKEM");
            topPanelKEM.__internal_setGenerationDpi(96);
            topPanelKEM.registerTranslationHandler(translationHandler);
            topPanelKEM.setScaleForResolution(true);
            topPanelKEM.setMinimumWidth(10);
            topPanelKEM.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder topPanelKEMLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            topPanelKEM.setLayout(topPanelKEMLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder topPanelKEMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            topPanelKEMConstraints.setPosition("north");
            topPanelKEM.setConstraints(topPanelKEMConstraints);
            tabbedpaneentryKEM_content.addChild(topPanelKEM);
            contentPanelKEM = new de.docware.framework.modules.gui.controls.GuiPanel();
            contentPanelKEM.setName("contentPanelKEM");
            contentPanelKEM.__internal_setGenerationDpi(96);
            contentPanelKEM.registerTranslationHandler(translationHandler);
            contentPanelKEM.setScaleForResolution(true);
            contentPanelKEM.setMinimumWidth(10);
            contentPanelKEM.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder contentPanelKEMLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            contentPanelKEM.setLayout(contentPanelKEMLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder contentPanelKEMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            contentPanelKEM.setConstraints(contentPanelKEMConstraints);
            tabbedpaneentryKEM_content.addChild(contentPanelKEM);
            bottomPanelKEM = new de.docware.framework.modules.gui.controls.GuiPanel();
            bottomPanelKEM.setName("bottomPanelKEM");
            bottomPanelKEM.__internal_setGenerationDpi(96);
            bottomPanelKEM.registerTranslationHandler(translationHandler);
            bottomPanelKEM.setScaleForResolution(true);
            bottomPanelKEM.setMinimumWidth(10);
            bottomPanelKEM.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder bottomPanelKEMLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            bottomPanelKEM.setLayout(bottomPanelKEMLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder bottomPanelKEMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            bottomPanelKEMConstraints.setPosition("south");
            bottomPanelKEM.setConstraints(bottomPanelKEMConstraints);
            tabbedpaneentryKEM_content.addChild(bottomPanelKEM);
            tabbedpaneentryKEM.addChild(tabbedpaneentryKEM_content);
            tabbedpane.addChild(tabbedpaneentryKEM);
            tabbedpaneentryMBS_SAA = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentryMBS_SAA.setName("tabbedpaneentryMBS_SAA");
            tabbedpaneentryMBS_SAA.__internal_setGenerationDpi(96);
            tabbedpaneentryMBS_SAA.registerTranslationHandler(translationHandler);
            tabbedpaneentryMBS_SAA.setScaleForResolution(true);
            tabbedpaneentryMBS_SAA.setMinimumWidth(10);
            tabbedpaneentryMBS_SAA.setMinimumHeight(10);
            tabbedpaneentryMBS_SAA.setTitle("!!MBS SAA Arbeitsvorrat");
            tabbedpaneentryMBS_SAA_content = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedpaneentryMBS_SAA_content.setName("tabbedpaneentryMBS_SAA_content");
            tabbedpaneentryMBS_SAA_content.__internal_setGenerationDpi(96);
            tabbedpaneentryMBS_SAA_content.registerTranslationHandler(translationHandler);
            tabbedpaneentryMBS_SAA_content.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedpaneentryMBS_SAA_contentLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedpaneentryMBS_SAA_content.setLayout(tabbedpaneentryMBS_SAA_contentLayout);
            topPanelMBS_SAA = new de.docware.framework.modules.gui.controls.GuiPanel();
            topPanelMBS_SAA.setName("topPanelMBS_SAA");
            topPanelMBS_SAA.__internal_setGenerationDpi(96);
            topPanelMBS_SAA.registerTranslationHandler(translationHandler);
            topPanelMBS_SAA.setScaleForResolution(true);
            topPanelMBS_SAA.setMinimumWidth(10);
            topPanelMBS_SAA.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder topPanelMBS_SAALayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            topPanelMBS_SAA.setLayout(topPanelMBS_SAALayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder topPanelMBS_SAAConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            topPanelMBS_SAAConstraints.setPosition("north");
            topPanelMBS_SAA.setConstraints(topPanelMBS_SAAConstraints);
            tabbedpaneentryMBS_SAA_content.addChild(topPanelMBS_SAA);
            contentPanelMBS_SAA = new de.docware.framework.modules.gui.controls.GuiPanel();
            contentPanelMBS_SAA.setName("contentPanelMBS_SAA");
            contentPanelMBS_SAA.__internal_setGenerationDpi(96);
            contentPanelMBS_SAA.registerTranslationHandler(translationHandler);
            contentPanelMBS_SAA.setScaleForResolution(true);
            contentPanelMBS_SAA.setMinimumWidth(10);
            contentPanelMBS_SAA.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder contentPanelMBS_SAALayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            contentPanelMBS_SAA.setLayout(contentPanelMBS_SAALayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder contentPanelMBS_SAAConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            contentPanelMBS_SAA.setConstraints(contentPanelMBS_SAAConstraints);
            tabbedpaneentryMBS_SAA_content.addChild(contentPanelMBS_SAA);
            bottomPanelMBS_SAA = new de.docware.framework.modules.gui.controls.GuiPanel();
            bottomPanelMBS_SAA.setName("bottomPanelMBS_SAA");
            bottomPanelMBS_SAA.__internal_setGenerationDpi(96);
            bottomPanelMBS_SAA.registerTranslationHandler(translationHandler);
            bottomPanelMBS_SAA.setScaleForResolution(true);
            bottomPanelMBS_SAA.setMinimumWidth(10);
            bottomPanelMBS_SAA.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder bottomPanelMBS_SAALayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            bottomPanelMBS_SAA.setLayout(bottomPanelMBS_SAALayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder bottomPanelMBS_SAAConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            bottomPanelMBS_SAAConstraints.setPosition("south");
            bottomPanelMBS_SAA.setConstraints(bottomPanelMBS_SAAConstraints);
            tabbedpaneentryMBS_SAA_content.addChild(bottomPanelMBS_SAA);
            tabbedpaneentryMBS_SAA.addChild(tabbedpaneentryMBS_SAA_content);
            tabbedpane.addChild(tabbedpaneentryMBS_SAA);
            tabbedpaneentryMBS_KEM = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentryMBS_KEM.setName("tabbedpaneentryMBS_KEM");
            tabbedpaneentryMBS_KEM.__internal_setGenerationDpi(96);
            tabbedpaneentryMBS_KEM.registerTranslationHandler(translationHandler);
            tabbedpaneentryMBS_KEM.setScaleForResolution(true);
            tabbedpaneentryMBS_KEM.setMinimumWidth(10);
            tabbedpaneentryMBS_KEM.setMinimumHeight(10);
            tabbedpaneentryMBS_KEM.setTitle("!!MBS KEM Arbeitsvorrat");
            tabbedpaneentryMBS_KEM_content = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedpaneentryMBS_KEM_content.setName("tabbedpaneentryMBS_KEM_content");
            tabbedpaneentryMBS_KEM_content.__internal_setGenerationDpi(96);
            tabbedpaneentryMBS_KEM_content.registerTranslationHandler(translationHandler);
            tabbedpaneentryMBS_KEM_content.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedpaneentryMBS_KEM_contentLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedpaneentryMBS_KEM_content.setLayout(tabbedpaneentryMBS_KEM_contentLayout);
            topPanelMBS_KEM = new de.docware.framework.modules.gui.controls.GuiPanel();
            topPanelMBS_KEM.setName("topPanelMBS_KEM");
            topPanelMBS_KEM.__internal_setGenerationDpi(96);
            topPanelMBS_KEM.registerTranslationHandler(translationHandler);
            topPanelMBS_KEM.setScaleForResolution(true);
            topPanelMBS_KEM.setMinimumWidth(10);
            topPanelMBS_KEM.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder topPanelMBS_KEMLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            topPanelMBS_KEM.setLayout(topPanelMBS_KEMLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder topPanelMBS_KEMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            topPanelMBS_KEMConstraints.setPosition("north");
            topPanelMBS_KEM.setConstraints(topPanelMBS_KEMConstraints);
            tabbedpaneentryMBS_KEM_content.addChild(topPanelMBS_KEM);
            contentPanelMBS_KEM = new de.docware.framework.modules.gui.controls.GuiPanel();
            contentPanelMBS_KEM.setName("contentPanelMBS_KEM");
            contentPanelMBS_KEM.__internal_setGenerationDpi(96);
            contentPanelMBS_KEM.registerTranslationHandler(translationHandler);
            contentPanelMBS_KEM.setScaleForResolution(true);
            contentPanelMBS_KEM.setMinimumWidth(10);
            contentPanelMBS_KEM.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder contentPanelMBS_KEMLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            contentPanelMBS_KEM.setLayout(contentPanelMBS_KEMLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder contentPanelMBS_KEMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            contentPanelMBS_KEM.setConstraints(contentPanelMBS_KEMConstraints);
            tabbedpaneentryMBS_KEM_content.addChild(contentPanelMBS_KEM);
            bottomPanelMBS_KEM = new de.docware.framework.modules.gui.controls.GuiPanel();
            bottomPanelMBS_KEM.setName("bottomPanelMBS_KEM");
            bottomPanelMBS_KEM.__internal_setGenerationDpi(96);
            bottomPanelMBS_KEM.registerTranslationHandler(translationHandler);
            bottomPanelMBS_KEM.setScaleForResolution(true);
            bottomPanelMBS_KEM.setMinimumWidth(10);
            bottomPanelMBS_KEM.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder bottomPanelMBS_KEMLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            bottomPanelMBS_KEM.setLayout(bottomPanelMBS_KEMLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder bottomPanelMBS_KEMConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            bottomPanelMBS_KEMConstraints.setPosition("south");
            bottomPanelMBS_KEM.setConstraints(bottomPanelMBS_KEMConstraints);
            tabbedpaneentryMBS_KEM_content.addChild(bottomPanelMBS_KEM);
            tabbedpaneentryMBS_KEM.addChild(tabbedpaneentryMBS_KEM_content);
            tabbedpane.addChild(tabbedpaneentryMBS_KEM);
            tabbedpaneentryCTT = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentryCTT.setName("tabbedpaneentryCTT");
            tabbedpaneentryCTT.__internal_setGenerationDpi(96);
            tabbedpaneentryCTT.registerTranslationHandler(translationHandler);
            tabbedpaneentryCTT.setScaleForResolution(true);
            tabbedpaneentryCTT.setMinimumWidth(10);
            tabbedpaneentryCTT.setMinimumHeight(10);
            tabbedpaneentryCTT.setTitle("CTT SAA Arbeitsvorrat");
            tabbedpaneentryCTT_content = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedpaneentryCTT_content.setName("tabbedpaneentryCTT_content");
            tabbedpaneentryCTT_content.__internal_setGenerationDpi(96);
            tabbedpaneentryCTT_content.registerTranslationHandler(translationHandler);
            tabbedpaneentryCTT_content.setScaleForResolution(true);
            tabbedpaneentryCTT_content.setMinimumWidth(10);
            tabbedpaneentryCTT_content.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedpaneentryCTT_contentLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedpaneentryCTT_content.setLayout(tabbedpaneentryCTT_contentLayout);
            topPanelCTT = new de.docware.framework.modules.gui.controls.GuiPanel();
            topPanelCTT.setName("topPanelCTT");
            topPanelCTT.__internal_setGenerationDpi(96);
            topPanelCTT.registerTranslationHandler(translationHandler);
            topPanelCTT.setScaleForResolution(true);
            topPanelCTT.setMinimumWidth(10);
            topPanelCTT.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder topPanelCTTLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            topPanelCTT.setLayout(topPanelCTTLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder topPanelCTTConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            topPanelCTTConstraints.setPosition("north");
            topPanelCTT.setConstraints(topPanelCTTConstraints);
            tabbedpaneentryCTT_content.addChild(topPanelCTT);
            contentPanelCTT = new de.docware.framework.modules.gui.controls.GuiPanel();
            contentPanelCTT.setName("contentPanelCTT");
            contentPanelCTT.__internal_setGenerationDpi(96);
            contentPanelCTT.registerTranslationHandler(translationHandler);
            contentPanelCTT.setScaleForResolution(true);
            contentPanelCTT.setMinimumWidth(10);
            contentPanelCTT.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder contentPanelCTTLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            contentPanelCTT.setLayout(contentPanelCTTLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder contentPanelCTTConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            contentPanelCTT.setConstraints(contentPanelCTTConstraints);
            tabbedpaneentryCTT_content.addChild(contentPanelCTT);
            bottomPanelCTT = new de.docware.framework.modules.gui.controls.GuiPanel();
            bottomPanelCTT.setName("bottomPanelCTT");
            bottomPanelCTT.__internal_setGenerationDpi(96);
            bottomPanelCTT.registerTranslationHandler(translationHandler);
            bottomPanelCTT.setScaleForResolution(true);
            bottomPanelCTT.setMinimumWidth(10);
            bottomPanelCTT.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder bottomPanelCTTLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
            bottomPanelCTT.setLayout(bottomPanelCTTLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder bottomPanelCTTConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            bottomPanelCTTConstraints.setPosition("south");
            bottomPanelCTT.setConstraints(bottomPanelCTTConstraints);
            tabbedpaneentryCTT_content.addChild(bottomPanelCTT);
            tabbedpaneentryCTT.addChild(tabbedpaneentryCTT_content);
            tabbedpane.addChild(tabbedpaneentryCTT);
            tabbedpaneentrySAA_KEM_Diff = new de.docware.framework.modules.gui.controls.GuiTabbedPaneEntry();
            tabbedpaneentrySAA_KEM_Diff.setName("tabbedpaneentrySAA_KEM_Diff");
            tabbedpaneentrySAA_KEM_Diff.__internal_setGenerationDpi(96);
            tabbedpaneentrySAA_KEM_Diff.registerTranslationHandler(translationHandler);
            tabbedpaneentrySAA_KEM_Diff.setScaleForResolution(true);
            tabbedpaneentrySAA_KEM_Diff.setMinimumWidth(10);
            tabbedpaneentrySAA_KEM_Diff.setMinimumHeight(10);
            tabbedpaneentrySAA_KEM_Diff.setTitle("!!SAA KEM Differenzen");
            tabbedpaneentrySAA_KEM_Diff_content = new de.docware.framework.modules.gui.controls.GuiPanel();
            tabbedpaneentrySAA_KEM_Diff_content.setName("tabbedpaneentrySAA_KEM_Diff_content");
            tabbedpaneentrySAA_KEM_Diff_content.__internal_setGenerationDpi(96);
            tabbedpaneentrySAA_KEM_Diff_content.registerTranslationHandler(translationHandler);
            tabbedpaneentrySAA_KEM_Diff_content.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder tabbedpaneentrySAA_KEM_Diff_contentLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            tabbedpaneentrySAA_KEM_Diff_content.setLayout(tabbedpaneentrySAA_KEM_Diff_contentLayout);
            topPanelSAA_KEM_Diff = new de.docware.framework.modules.gui.controls.GuiPanel();
            topPanelSAA_KEM_Diff.setName("topPanelSAA_KEM_Diff");
            topPanelSAA_KEM_Diff.__internal_setGenerationDpi(96);
            topPanelSAA_KEM_Diff.registerTranslationHandler(translationHandler);
            topPanelSAA_KEM_Diff.setScaleForResolution(true);
            topPanelSAA_KEM_Diff.setMinimumWidth(10);
            topPanelSAA_KEM_Diff.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder topPanelSAA_KEM_DiffLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            topPanelSAA_KEM_Diff.setLayout(topPanelSAA_KEM_DiffLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder topPanelSAA_KEM_DiffConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            topPanelSAA_KEM_DiffConstraints.setPosition("north");
            topPanelSAA_KEM_Diff.setConstraints(topPanelSAA_KEM_DiffConstraints);
            tabbedpaneentrySAA_KEM_Diff_content.addChild(topPanelSAA_KEM_Diff);
            contentPanelSAA_KEM_Diff = new de.docware.framework.modules.gui.controls.GuiPanel();
            contentPanelSAA_KEM_Diff.setName("contentPanelSAA_KEM_Diff");
            contentPanelSAA_KEM_Diff.__internal_setGenerationDpi(96);
            contentPanelSAA_KEM_Diff.registerTranslationHandler(translationHandler);
            contentPanelSAA_KEM_Diff.setScaleForResolution(true);
            contentPanelSAA_KEM_Diff.setMinimumWidth(10);
            contentPanelSAA_KEM_Diff.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder contentPanelSAA_KEM_DiffLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            contentPanelSAA_KEM_Diff.setLayout(contentPanelSAA_KEM_DiffLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder contentPanelSAA_KEM_DiffConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            contentPanelSAA_KEM_Diff.setConstraints(contentPanelSAA_KEM_DiffConstraints);
            tabbedpaneentrySAA_KEM_Diff_content.addChild(contentPanelSAA_KEM_Diff);
            bottomPanelSAA_KEM_Diff = new de.docware.framework.modules.gui.controls.GuiPanel();
            bottomPanelSAA_KEM_Diff.setName("bottomPanelSAA_KEM_Diff");
            bottomPanelSAA_KEM_Diff.__internal_setGenerationDpi(96);
            bottomPanelSAA_KEM_Diff.registerTranslationHandler(translationHandler);
            bottomPanelSAA_KEM_Diff.setScaleForResolution(true);
            bottomPanelSAA_KEM_Diff.setMinimumWidth(10);
            bottomPanelSAA_KEM_Diff.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder bottomPanelSAA_KEM_DiffLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            bottomPanelSAA_KEM_Diff.setLayout(bottomPanelSAA_KEM_DiffLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder bottomPanelSAA_KEM_DiffConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            bottomPanelSAA_KEM_DiffConstraints.setPosition("south");
            bottomPanelSAA_KEM_Diff.setConstraints(bottomPanelSAA_KEM_DiffConstraints);
            tabbedpaneentrySAA_KEM_Diff_content.addChild(bottomPanelSAA_KEM_Diff);
            tabbedpaneentrySAA_KEM_Diff.addChild(tabbedpaneentrySAA_KEM_Diff_content);
            tabbedpane.addChild(tabbedpaneentrySAA_KEM_Diff);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tabbedpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tabbedpane.setConstraints(tabbedpaneConstraints);
            mainPanel.addChild(tabbedpane);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder mainPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            mainPanel.setConstraints(mainPanelConstraints);
            this.addChild(mainPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}