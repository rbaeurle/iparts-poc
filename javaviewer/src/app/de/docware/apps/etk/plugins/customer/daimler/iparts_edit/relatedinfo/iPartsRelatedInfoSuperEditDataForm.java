/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.AbstractRelatedInfoMainForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.SessionKeyHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Related Edit für die Daten von einem Stücklisteneintrag inkl. zusätzlicher Editoren.
 */
public class iPartsRelatedInfoSuperEditDataForm extends AbstractRelatedInfoPartlistDataForm implements iPartsConst {

    public enum PartListEditType {
        REPLACEMENTS_FACTORY_DATA(iPartsConst.RELATED_INFO_SUPER_EDIT_DATA_TEXT, iPartsPlugin.SESSION_KEY_SUPER_EDIT_REPLACEMENTS_FACTORY_DATA_POS),
        PART_LIST_ENTRY_DATA(iPartsConst.RELATED_INFO_EDIT_DETAILS_DATA_TEXT, iPartsPlugin.SESSION_KEY_SUPER_EDIT_PART_LIST_ENTRY_DATA_POS);

        private String description;
        private String sessionKey;

        PartListEditType(String description, String sessionKey) {
            this.description = description;
            this.sessionKey = sessionKey;
        }

        public String getDescription() {
            return description;
        }

        public int getSessionDividerPos() {
            return SessionKeyHelper.getSuperEditDividerPos(sessionKey);
        }

        public boolean setSessionDividerPos(int pos) {
            return SessionKeyHelper.setSuperEditDividerPos(sessionKey, pos);
        }
    }

    private static int MINIMUM_DOCKING_PANEL_HEIGHT = 130;
    private static int DEFAULT_DOCKING_PANEL_HEIGHT = 300;

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry entry, boolean isEditRelatedInfo) {
        if ((entry != null) && relatedInfoIsVisible(entry.getOwnerAssembly(), iPartsModuleTypes.getEditableModuleTypes())) {
            if (isEditRelatedInfo) {
                return true;
            }
        }
        return false;
    }

    public static void refreshSuperEdit(RelatedInfoBaseForm relatedInfoForm) {
        relatedInfoForm.getConnector().updateAllViews(relatedInfoForm, false);
    }

    private PartListEditType type;
    private PartListEntryId loadedPartListEntryId;

    private iPartsRelatedInfoEditPartListEntryDataForm partListEntryEditDialog;
    private List<Management> managementList;
    private int mainPanelHeight = -1;
    private boolean suppressDataChanged;

    /**
     * Erzeugt eine Instanz von iPartsRelatedInfoSuperEditDataForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    protected iPartsRelatedInfoSuperEditDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 IEtkRelatedInfo relatedInfo, PartListEditType type) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        this.type = type;

        // Bei allen hier aufgelisteten RelatedInfos muss sowohl in der Methode updateData() bei einem forceUpdateAll als
        // auch in der Methode dataChanged() bei Änderungen an den betreffenden Daten die GUI neu aufgebaut werden
        managementList = new DwList<>();
        switch (type) {
            case REPLACEMENTS_FACTORY_DATA:
                managementList.add(new Management(new iPartsRelatedInfoReplacementsData()));
                managementList.add(new Management(new iPartsRelatedInfoFactoryData()));
                break;
            case PART_LIST_ENTRY_DATA:
                managementList.add(new Management(new iPartsRelatedInfoFootNote()));
                managementList.add(new Management(new iPartsRelatedInfoWWPartsData()));
                break;
        }

        postCreateGui();
        partListEntryEditDialog.setReadOnly(!dataConnector.isEditContext());
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        // Umbau
        mainWindow.panelTop.setVisible(false);  // for Future use
        mainWindow.panelPartListEntry.removeFromParent();
        mainWindow.dockingpanelPartListEntry.removeFromParent();
        mainWindow.dockingpanelPartListEntry.setVisible(false);
        mainWindow.splitpaneElements.addChild(mainWindow.panelPartListEntry);

        partListEntryEditDialog = new iPartsRelatedInfoEditPartListEntryDataForm(getConnector(), this, relatedInfo) {
            @Override
            protected GuiPanel createSouthPanel() {
                GuiPanel panel = super.createSouthPanel();
                createOptimizeSizeButtons(panel);
                return panel;
            }
        };

        partListEntryEditDialog.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelPartListEntry.addChild(partListEntryEditDialog.getGui());

        mainWindow.panel_secondChild.removeAllChildren();
        int y = 0;
        String namePrefix = "panel";
        for (Management management : managementList) {
            AbstractGuiControl separator = createSeparator(y);
            separator.setName("separator_" + y);
            mainWindow.panel_secondChild.addChild(separator);
            y++;

            management.newDisplayFormInstance(getConnector(), this);
            GuiPanel panel = createElementForRelatedInfoForm(y, mainWindow.panel_secondChild, management, namePrefix);
            mainWindow.panel_secondChild.addChild(panel);
            management.extendDockingPanelText();
            y++;
        }
        GuiLabel label = createEndLabel(y);
        mainWindow.panel_secondChild.addChild(label);

        getGui().addEventListener(new EventListenerFireOnce(Event.ON_RESIZE_EVENT, getGui()) {
            @Override
            public void fireOnce(Event event) {
                int width = event.getIntParameter(Event.EVENT_PARAMETER_NEWWIDTH) - 18;
                mainPanelHeight = event.getIntParameter(Event.EVENT_PARAMETER_NEWHEIGHT);
                int height = mainPanelHeight / 2;
                height = Math.min(height, partListEntryEditDialog.getCalculatedPanelSize().height);
                if (!managementList.isEmpty()) {
                    int storedDividerPos = type.getSessionDividerPos();
                    if (storedDividerPos != -1) {
                        height = storedDividerPos;
                    }
                    setMainDividerPos(height);

                    for (iPartsRelatedInfoSuperEditDataForm.Management management : managementList) {
                        management.getForm().getGui().setMaximumWidth(width);
                    }
                } else {
                    setMainDividerPos(mainPanelHeight - 1);
                    mainWindow.splitpaneElements.setDividerSize(0);
                }
            }
        });

        ThemeManager.get().render(mainWindow);
    }

    private void createOptimizeSizeButtons(GuiPanel parentPanel) {
        GuiDockingPanel dummyDockingPanel = createDockingPanel("dummy");
        int dockingPanelHeight = dummyDockingPanel.getPreferredButtonSize().height + 1;

        GuiContextMenu showContextMenu = new GuiContextMenu();
        showContextMenu.setName("superEditDividerPosContextMenu");
        showContextMenu.__internal_setGenerationDpi(96);
        showContextMenu.registerTranslationHandler(getUITranslationHandler());
        showContextMenu.setScaleForResolution(true);

        Runnable optimizePartListEditRunnable = () -> {
            int totalHeight = mainPanelHeight;
            if (mainPanelHeight < 0) {
                totalHeight = mainWindow.getHeight();
            }
            totalHeight -= DWLayoutManager.get().isResponsiveMode() ? 105 : 20 + managementList.size() * dockingPanelHeight; // tu so, als ob alle Dockingpanles geschlossen sind
            int height = Math.min(partListEntryEditDialog.getCalculatedPanelSize().height, totalHeight - 100); // 100 px Mindesthöhe für die restlichen Daten
            setMainDividerPos(height);
        };

        GuiButton optimizePartListDataButton = new GuiButton();
        optimizePartListDataButton.setName("optimizePartListDataSize");
        optimizePartListDataButton.setIcon(EditDefaultImages.edit_btn_optimize_north.getImage());
        optimizePartListDataButton.setTooltip("!!Anzeigeoptimierung für Stücklistendaten");
        optimizePartListDataButton.setMaximumWidth(optimizePartListDataButton.getPreferredHeight());
        optimizePartListDataButton.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_VERTICAL,
                                                                         0, 8, 0, 0));
        optimizePartListDataButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                optimizePartListEditRunnable.run();
            }
        });
        parentPanel.addChild(optimizePartListDataButton);

        GuiButton optimizeRestButton = new GuiButton();
        optimizeRestButton.setName("optimizeRestSize");
        optimizeRestButton.setIcon(EditDefaultImages.edit_btn_optimize_south.getImage());
        optimizeRestButton.setTooltip("!!Anzeigeoptimierung für restliche Anzeige");
        optimizeRestButton.setMaximumWidth(optimizeRestButton.getPreferredHeight());
        optimizeRestButton.setConstraints(new ConstraintsGridBag(2, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_VERTICAL,
                                                                 0, 8, 0, 0));
        optimizeRestButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                if (areAllManagementDockingPanelsClosed()) {
                    optimizePartListEditRunnable.run();
                    return;
                }
                int totalHeight = mainPanelHeight;
                if (mainPanelHeight < 0) {
                    totalHeight = mainWindow.getHeight();
                }
                int height = DWLayoutManager.get().isResponsiveMode() ? 105 : 20;
                for (Management management : managementList) {
                    if (management.getDockingPanel().isShowing()) {
                        height += management.getMinimumHeightForDockingPanel();
                    }
                    height += dockingPanelHeight;
                }
                height = Math.min(totalHeight - 100, height); // 100 px Mindesthöhe für die Stücklistendaten
                setMainDividerPos(totalHeight - height);
            }
        });
        parentPanel.addChild(optimizeRestButton);
    }

    private void setMainDividerPos(int dividerPosition) {
        mainWindow.splitpaneElements.setDividerPosition(dividerPosition);
    }

    private boolean areAllManagementDockingPanelsClosed() {
        for (Management management : managementList) {
            if (management.getDockingPanel().isShowing()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        for (Management management : managementList) {
            RelatedInfoBaseForm form = management.getForm();
            if (form != null) {
                form.dispose();
            }
        }

        super.dispose();
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        return null;
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    @Override
    public void dataChanged() {
        if (suppressDataChanged) {
            return;
        }

        boolean isActiveRelatedInfo = getConnector().getActiveRelatedSubForm() == this;
        if (isActiveRelatedInfo) {
            suppressDataChanged = true;
            try {
                hideRelatedInfo();
            } finally {
                suppressDataChanged = false;
            }
        }

        loadSubForms();

        if (isActiveRelatedInfo) {
            showRelatedInfo();
        }
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        PartListEntryId currentPartListEntryId = getConnector().getRelatedInfoData().getAsPartListEntryId();
        if ((getConnector().getActiveRelatedSubForm() == this) && (forceUpdateAll || !Utils.objectEquals(currentPartListEntryId, loadedPartListEntryId))) {
            loadedPartListEntryId = currentPartListEntryId;
            EtkDataPartListEntry currentPartListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
            if (currentPartListEntry instanceof iPartsDataPartListEntry) {
                loadSubForms();
            }
        }
    }

    @Override
    public void afterUpdateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        super.afterUpdateData(sender, forceUpdateAll);
        for (Management management : managementList) {
            management.calculateOptimalHeight();
            management.getPanelInsideDockingPanel().setMinimumHeight(management.getMinimumHeightForDockingPanel());
            handleDockingPanel(management.getDockingPanel(), management.getPanelInsideDockingPanel(), mainWindow.panel_secondChild);
        }
    }

    @Override
    public void showRelatedInfo() {
        super.showRelatedInfo();

        partListEntryEditDialog.showRelatedInfo();

        for (Management management : managementList) {
            management.getForm().showRelatedInfo();
        }
    }

    @Override
    protected boolean hideRelatedInfoCalled() {
        if (!getConnector().isEditContext()) {
            return true;
        }

        boolean result = partListEntryEditDialog.hideRelatedInfo();
        boolean isModifiedByEdit = partListEntryEditDialog.isModifiedByEdit();

        for (Management management : managementList) {
            result &= management.getForm().hideRelatedInfo();
            isModifiedByEdit |= management.getForm().isModifiedByEdit();
        }

        if (isModifiedByEdit) {
            setModifiedByEdit(true);
        }
        if (result && !managementList.isEmpty()) {
            type.setSessionDividerPos(mainWindow.splitpaneElements.getDividerPosition());
        }
        return result;
    }

    private void loadSubForms() {
        partListEntryEditDialog.updateData(this, true);

        for (Management management : managementList) {
            management.getForm().updateData(this, true);
            management.extendDockingPanelText();
        }

        hideShowDockingPanels();
    }

    private void hideShowDockingPanels() {
        for (Management mangement : managementList) {
            mangement.getDockingPanel().setShowing(mangement.getForm().hasElementsToShow());
        }
    }

    private void closeWindow(Event event) {
        close();
        mainWindow.setVisible(false);
    }

    private void handleDockingPanel(final GuiDockingPanel dockingPanel, final GuiPanel child, final GuiPanel parent) {
        final Session session = Session.get();
        session.startChildThread(thread -> session.invokeThreadSafe(() -> {
            if (dockingPanel.isShowing()) {
                dockingPanel.setMaximumHeight(dockingPanel.getPreferredHeight() + child.getPreferredHeight());
            } else {
                dockingPanel.setMaximumHeight(dockingPanel.getPreferredHeight());
            }
            parent.relayout();
        }));
    }

    private GuiPanel createElementForRelatedInfoForm(int y, final GuiPanel parent, Management management, String namePrefix) {
        String displayTextKey = management.getDisplayTextKey();
        GuiPanel parentPanel = createParentPanelForDockingPanel(y);
        parentPanel.setName(namePrefix + "_parent_" + displayTextKey);

        final GuiDockingPanel dockingPanel = createDockingPanel(displayTextKey);
        dockingPanel.setName(namePrefix + "_docking_" + displayTextKey);

        final GuiPanel panelInsideDockingPanel = createPanelForInsideDockingPanel(management.getMinimumHeightForDockingPanel());
        panelInsideDockingPanel.setName(namePrefix + "_panel_" + displayTextKey);

        management.getForm().getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        panelInsideDockingPanel.addChild(management.getForm().getGui());
        management.setPanelInsideDockingPanel(panelInsideDockingPanel);

        dockingPanel.addChild(panelInsideDockingPanel);
        management.setDockingPanel(dockingPanel);

        dockingPanel.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            public void fire(de.docware.framework.modules.gui.event.Event event) {
                handleDockingPanel(dockingPanel, panelInsideDockingPanel, parent);
            }
        });

        parentPanel.addChild(dockingPanel);
        return parentPanel;
    }

    private GuiPanel createParentPanelForDockingPanel(int y) {
        GuiPanel result = createPanel();
        ConstraintsGridBag constraints = new ConstraintsGridBag(0, y, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER,
                                                                ConstraintsGridBag.FILL_HORIZONTAL, 0, 0, 0, 0);
        result.setConstraints(constraints);
        return result;
    }

    private GuiDockingPanel createDockingPanel(String displayTextKey) {
        GuiDockingPanel result = new GuiDockingPanel();
        result.setMinimumHeight(0);
        result.__internal_setGenerationDpi(96);
        result.registerTranslationHandler(getUITranslationHandler());
        result.setScaleForResolution(true);
        result.setTextHide(TranslationHandler.translate("!!%1 verbergen", TranslationHandler.translate(displayTextKey)));
        result.setTextShow(TranslationHandler.translate("!!%1 anzeigen", TranslationHandler.translate(displayTextKey)));
        result.setImageHide(DesignImage.dockingPanelSouth.getImage());
        result.setImageShow(DesignImage.dockingPanelNorth.getImage());
        result.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
        result.setButtonFill(true);
        setDockingPanelColors(result);
        ConstraintsBorder constraints = new ConstraintsBorder();
        result.setConstraints(constraints);
        return result;
    }

    private void setDockingPanelColors(GuiDockingPanel dockingPanel) {
        dockingPanel.setButtonBackgroundColor(Colors.clDesignButtonDisabledBackgroundGradient1.getColor());
        dockingPanel.setButtonForegroundColor(Colors.clDesignButtonBorderSelected.getColor());
    }

    private GuiPanel createPanelForInsideDockingPanel(int minimumHeight) {
        GuiPanel result = createPanel();
        if (minimumHeight < 0) {
            minimumHeight = DEFAULT_DOCKING_PANEL_HEIGHT;
        }
        result.setMinimumHeight(minimumHeight);
        ConstraintsBorder constraints = new ConstraintsBorder();
        result.setConstraints(constraints);
        return result;
    }

    private GuiPanel createPanel() {
        GuiPanel result = new GuiPanel();
        result.setMinimumHeight(0);
        result.__internal_setGenerationDpi(96);
        result.registerTranslationHandler(getUITranslationHandler());
        result.setScaleForResolution(true);
        LayoutBorder layout = new LayoutBorder();
        result.setLayout(layout);
        return result;
    }

    private GuiPanel createSeparator(int y) {
        // Ein (im iParts-Design weißes) schmales GuiPanel als Separator verwenden, weil dies hübscher aussieht als ein
        // GuiSeparator
        GuiPanel result = new GuiPanel();
        result.__internal_setGenerationDpi(96);
        result.registerTranslationHandler(getUITranslationHandler());
        result.setScaleForResolution(true);
        result.setMinimumHeight(1);
        ConstraintsGridBag constraints = new ConstraintsGridBag(0, y, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER,
                                                                ConstraintsGridBag.FILL_HORIZONTAL, 0, 0, 0, 0);
        result.setConstraints(constraints);
        return result;
    }

    private GuiLabel createEndLabel(int y) {
        GuiLabel label = new GuiLabel();
        label.setName("endLabel" + y);
        label.__internal_setGenerationDpi(96);
        label.registerTranslationHandler(getUITranslationHandler());
        label.setScaleForResolution(true);
        ConstraintsGridBag constraints = new ConstraintsGridBag(0, y, 1, 1, 100.0, 100.0, ConstraintsGridBag.ANCHOR_CENTER,
                                                                ConstraintsGridBag.FILL_BOTH, 0, 0, 0, 0);
        label.setConstraints(constraints);
        return label;
    }


    private class Management {

        private EtkRelatedInfoBaseImpl parent;
        private RelatedInfoBaseForm form;
        private GuiDockingPanel dockingPanel;
        private GuiPanel panelInsideDockingPanel;
        private int minimumHeightForDockingPanel;

        public Management(EtkRelatedInfoBaseImpl parent) {
            this.parent = parent;
            this.minimumHeightForDockingPanel = DEFAULT_DOCKING_PANEL_HEIGHT;
        }

        public Management(EtkRelatedInfoBaseImpl parent, int minimumHeightForDockingPanel) {
            this(parent);
            this.minimumHeightForDockingPanel = minimumHeightForDockingPanel;
        }

        public int getMinimumHeightForDockingPanel() {
            return minimumHeightForDockingPanel;
        }

        public RelatedInfoBaseForm newDisplayFormInstance(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            if (form == null) {
                form = parent.newDisplayFormInstance(dataConnector, parentForm);
            }
            return form;
        }

        public void calculateOptimalHeight() {
            if (form instanceof iPartsSuperEditRelatedInfoInterface) {
                minimumHeightForDockingPanel = Math.max(MINIMUM_DOCKING_PANEL_HEIGHT, ((iPartsSuperEditRelatedInfoInterface)form).calculateOptimalHeight());
            }
        }

        public RelatedInfoBaseForm getForm() {
            return form;
        }

        public String getDisplayTextKey() {
            return parent.getDisplayText();
        }

        public GuiDockingPanel getDockingPanel() {
            return dockingPanel;
        }

        public void setDockingPanel(GuiDockingPanel dockingPanel) {
            this.dockingPanel = dockingPanel;
        }

        public GuiPanel getPanelInsideDockingPanel() {
            return panelInsideDockingPanel;
        }

        public void setPanelInsideDockingPanel(GuiPanel panelInsideDockingPanel) {
            this.panelInsideDockingPanel = panelInsideDockingPanel;
        }

        public void extendDockingPanelText() {
            if ((form != null) && (dockingPanel != null)) {
                if (form.isModifiedByEdit() && !dockingPanel.getTextHide().endsWith(AbstractRelatedInfoMainForm.MODIFIED_BY_EDIT_MARKER)) {
                    dockingPanel.setTextHide(dockingPanel.getTextHide() + AbstractRelatedInfoMainForm.MODIFIED_BY_EDIT_MARKER);
                    dockingPanel.setTextShow(dockingPanel.getTextShow() + AbstractRelatedInfoMainForm.MODIFIED_BY_EDIT_MARKER);
                }
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneElements;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanelPartListEntry;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelPartListEntry;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneElements_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpane_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separator_FN;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_Footnotes;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanel_FootNotes;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_Docking_FootNotes;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separator_FN_WWParts;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_WW_Parts;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanel_WWParts;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_Docking_WWParts;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separator_WWParts_Replace;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_ReplaceParts;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanel_Replace;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_Docking_Replace;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSeparator separator_Replace_Factory;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_Factory;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiDockingPanel dockingpanel_Factory;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_Docking_Factory;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panel_0;

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
            panelTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTop.setName("panelTop");
            panelTop.__internal_setGenerationDpi(96);
            panelTop.registerTranslationHandler(translationHandler);
            panelTop.setScaleForResolution(true);
            panelTop.setMinimumWidth(10);
            panelTop.setMinimumHeight(80);
            de.docware.framework.modules.gui.layout.LayoutBorder panelTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelTop.setLayout(panelTopLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelTopConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelTopConstraints.setPosition("north");
            panelTop.setConstraints(panelTopConstraints);
            panelMain.addChild(panelTop);
            splitpaneElements = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneElements.setName("splitpaneElements");
            splitpaneElements.__internal_setGenerationDpi(96);
            splitpaneElements.registerTranslationHandler(translationHandler);
            splitpaneElements.setScaleForResolution(true);
            splitpaneElements.setMinimumWidth(10);
            splitpaneElements.setMinimumHeight(10);
            splitpaneElements.setHorizontal(false);
            splitpaneElements.setDividerPosition(286);
            splitpaneElements.setDividerSize(8);
            dockingpanelPartListEntry = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanelPartListEntry.setName("dockingpanelPartListEntry");
            dockingpanelPartListEntry.__internal_setGenerationDpi(96);
            dockingpanelPartListEntry.registerTranslationHandler(translationHandler);
            dockingpanelPartListEntry.setScaleForResolution(true);
            dockingpanelPartListEntry.setMinimumWidth(10);
            dockingpanelPartListEntry.setMinimumHeight(10);
            dockingpanelPartListEntry.setTextHide("!!Stücklistendaten verbergen");
            dockingpanelPartListEntry.setTextShow("!!Stücklistendaten anzeigen");
            dockingpanelPartListEntry.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            dockingpanelPartListEntry.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            dockingpanelPartListEntry.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            dockingpanelPartListEntry.setButtonFill(true);
            panelPartListEntry = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelPartListEntry.setName("panelPartListEntry");
            panelPartListEntry.__internal_setGenerationDpi(96);
            panelPartListEntry.registerTranslationHandler(translationHandler);
            panelPartListEntry.setScaleForResolution(true);
            panelPartListEntry.setMinimumWidth(10);
            panelPartListEntry.setMinimumHeight(75);
            de.docware.framework.modules.gui.layout.LayoutBorder panelPartListEntryLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelPartListEntry.setLayout(panelPartListEntryLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelPartListEntryConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelPartListEntry.setConstraints(panelPartListEntryConstraints);
            dockingpanelPartListEntry.addChild(panelPartListEntry);
            splitpaneElements.addChild(dockingpanelPartListEntry);
            splitpaneElements_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneElements_secondChild.setName("splitpaneElements_secondChild");
            splitpaneElements_secondChild.__internal_setGenerationDpi(96);
            splitpaneElements_secondChild.registerTranslationHandler(translationHandler);
            splitpaneElements_secondChild.setScaleForResolution(true);
            splitpaneElements_secondChild.setMinimumWidth(0);
            splitpaneElements_secondChild.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneElements_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneElements_secondChild.setLayout(splitpaneElements_secondChildLayout);
            scrollpane_secondChild = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpane_secondChild.setName("scrollpane_secondChild");
            scrollpane_secondChild.__internal_setGenerationDpi(96);
            scrollpane_secondChild.registerTranslationHandler(translationHandler);
            scrollpane_secondChild.setScaleForResolution(true);
            scrollpane_secondChild.setMinimumWidth(10);
            scrollpane_secondChild.setMinimumHeight(10);
            panel_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_secondChild.setName("panel_secondChild");
            panel_secondChild.__internal_setGenerationDpi(96);
            panel_secondChild.registerTranslationHandler(translationHandler);
            panel_secondChild.setScaleForResolution(true);
            panel_secondChild.setMinimumWidth(10);
            panel_secondChild.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panel_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panel_secondChild.setLayout(panel_secondChildLayout);
            separator_FN = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator_FN.setName("separator_FN");
            separator_FN.__internal_setGenerationDpi(96);
            separator_FN.registerTranslationHandler(translationHandler);
            separator_FN.setScaleForResolution(true);
            separator_FN.setMinimumHeight(6);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag separator_FNConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            separator_FN.setConstraints(separator_FNConstraints);
            panel_secondChild.addChild(separator_FN);
            panel_Footnotes = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_Footnotes.setName("panel_Footnotes");
            panel_Footnotes.__internal_setGenerationDpi(96);
            panel_Footnotes.registerTranslationHandler(translationHandler);
            panel_Footnotes.setScaleForResolution(true);
            panel_Footnotes.setMinimumWidth(10);
            panel_Footnotes.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_FootnotesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_Footnotes.setLayout(panel_FootnotesLayout);
            dockingpanel_FootNotes = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanel_FootNotes.setName("dockingpanel_FootNotes");
            dockingpanel_FootNotes.__internal_setGenerationDpi(96);
            dockingpanel_FootNotes.registerTranslationHandler(translationHandler);
            dockingpanel_FootNotes.setScaleForResolution(true);
            dockingpanel_FootNotes.setMinimumWidth(10);
            dockingpanel_FootNotes.setMinimumHeight(10);
            dockingpanel_FootNotes.setTextHide("!!Fußnoten verbergen");
            dockingpanel_FootNotes.setTextShow("!!Fußnoten anzeigen");
            dockingpanel_FootNotes.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            dockingpanel_FootNotes.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            dockingpanel_FootNotes.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            dockingpanel_FootNotes.setButtonFill(true);
            panel_Docking_FootNotes = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_Docking_FootNotes.setName("panel_Docking_FootNotes");
            panel_Docking_FootNotes.__internal_setGenerationDpi(96);
            panel_Docking_FootNotes.registerTranslationHandler(translationHandler);
            panel_Docking_FootNotes.setScaleForResolution(true);
            panel_Docking_FootNotes.setMinimumWidth(10);
            panel_Docking_FootNotes.setMinimumHeight(50);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_Docking_FootNotesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_Docking_FootNotes.setLayout(panel_Docking_FootNotesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_Docking_FootNotesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_Docking_FootNotes.setConstraints(panel_Docking_FootNotesConstraints);
            dockingpanel_FootNotes.addChild(panel_Docking_FootNotes);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder dockingpanel_FootNotesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            dockingpanel_FootNotes.setConstraints(dockingpanel_FootNotesConstraints);
            panel_Footnotes.addChild(dockingpanel_FootNotes);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_FootnotesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            panel_Footnotes.setConstraints(panel_FootnotesConstraints);
            panel_secondChild.addChild(panel_Footnotes);
            separator_FN_WWParts = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator_FN_WWParts.setName("separator_FN_WWParts");
            separator_FN_WWParts.__internal_setGenerationDpi(96);
            separator_FN_WWParts.registerTranslationHandler(translationHandler);
            separator_FN_WWParts.setScaleForResolution(true);
            separator_FN_WWParts.setMinimumHeight(6);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag separator_FN_WWPartsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            separator_FN_WWParts.setConstraints(separator_FN_WWPartsConstraints);
            panel_secondChild.addChild(separator_FN_WWParts);
            panel_WW_Parts = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_WW_Parts.setName("panel_WW_Parts");
            panel_WW_Parts.__internal_setGenerationDpi(96);
            panel_WW_Parts.registerTranslationHandler(translationHandler);
            panel_WW_Parts.setScaleForResolution(true);
            panel_WW_Parts.setMinimumWidth(10);
            panel_WW_Parts.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_WW_PartsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_WW_Parts.setLayout(panel_WW_PartsLayout);
            dockingpanel_WWParts = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanel_WWParts.setName("dockingpanel_WWParts");
            dockingpanel_WWParts.__internal_setGenerationDpi(96);
            dockingpanel_WWParts.registerTranslationHandler(translationHandler);
            dockingpanel_WWParts.setScaleForResolution(true);
            dockingpanel_WWParts.setMinimumWidth(10);
            dockingpanel_WWParts.setMinimumHeight(10);
            dockingpanel_WWParts.setTextHide("!!Wahlweise-Teile verbergen");
            dockingpanel_WWParts.setTextShow("!!Wahlweise-Teile anzeigen");
            dockingpanel_WWParts.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            dockingpanel_WWParts.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            dockingpanel_WWParts.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            dockingpanel_WWParts.setButtonFill(true);
            panel_Docking_WWParts = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_Docking_WWParts.setName("panel_Docking_WWParts");
            panel_Docking_WWParts.__internal_setGenerationDpi(96);
            panel_Docking_WWParts.registerTranslationHandler(translationHandler);
            panel_Docking_WWParts.setScaleForResolution(true);
            panel_Docking_WWParts.setMinimumWidth(10);
            panel_Docking_WWParts.setMinimumHeight(50);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_Docking_WWPartsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_Docking_WWParts.setLayout(panel_Docking_WWPartsLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_Docking_WWPartsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_Docking_WWParts.setConstraints(panel_Docking_WWPartsConstraints);
            dockingpanel_WWParts.addChild(panel_Docking_WWParts);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder dockingpanel_WWPartsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            dockingpanel_WWParts.setConstraints(dockingpanel_WWPartsConstraints);
            panel_WW_Parts.addChild(dockingpanel_WWParts);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_WW_PartsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            panel_WW_Parts.setConstraints(panel_WW_PartsConstraints);
            panel_secondChild.addChild(panel_WW_Parts);
            separator_WWParts_Replace = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator_WWParts_Replace.setName("separator_WWParts_Replace");
            separator_WWParts_Replace.__internal_setGenerationDpi(96);
            separator_WWParts_Replace.registerTranslationHandler(translationHandler);
            separator_WWParts_Replace.setScaleForResolution(true);
            separator_WWParts_Replace.setMinimumHeight(6);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag separator_WWParts_ReplaceConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            separator_WWParts_Replace.setConstraints(separator_WWParts_ReplaceConstraints);
            panel_secondChild.addChild(separator_WWParts_Replace);
            panel_ReplaceParts = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_ReplaceParts.setName("panel_ReplaceParts");
            panel_ReplaceParts.__internal_setGenerationDpi(96);
            panel_ReplaceParts.registerTranslationHandler(translationHandler);
            panel_ReplaceParts.setScaleForResolution(true);
            panel_ReplaceParts.setMinimumWidth(10);
            panel_ReplaceParts.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_ReplacePartsLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_ReplaceParts.setLayout(panel_ReplacePartsLayout);
            dockingpanel_Replace = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanel_Replace.setName("dockingpanel_Replace");
            dockingpanel_Replace.__internal_setGenerationDpi(96);
            dockingpanel_Replace.registerTranslationHandler(translationHandler);
            dockingpanel_Replace.setScaleForResolution(true);
            dockingpanel_Replace.setMinimumWidth(10);
            dockingpanel_Replace.setMinimumHeight(10);
            dockingpanel_Replace.setTextHide("!!Ersetzungen verbergen");
            dockingpanel_Replace.setTextShow("!!Ersetzungen anzeigen");
            dockingpanel_Replace.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            dockingpanel_Replace.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            dockingpanel_Replace.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            dockingpanel_Replace.setButtonFill(true);
            panel_Docking_Replace = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_Docking_Replace.setName("panel_Docking_Replace");
            panel_Docking_Replace.__internal_setGenerationDpi(96);
            panel_Docking_Replace.registerTranslationHandler(translationHandler);
            panel_Docking_Replace.setScaleForResolution(true);
            panel_Docking_Replace.setMinimumWidth(10);
            panel_Docking_Replace.setMinimumHeight(50);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_Docking_ReplaceLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_Docking_Replace.setLayout(panel_Docking_ReplaceLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_Docking_ReplaceConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_Docking_Replace.setConstraints(panel_Docking_ReplaceConstraints);
            dockingpanel_Replace.addChild(panel_Docking_Replace);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder dockingpanel_ReplaceConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            dockingpanel_Replace.setConstraints(dockingpanel_ReplaceConstraints);
            panel_ReplaceParts.addChild(dockingpanel_Replace);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_ReplacePartsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 5, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            panel_ReplaceParts.setConstraints(panel_ReplacePartsConstraints);
            panel_secondChild.addChild(panel_ReplaceParts);
            separator_Replace_Factory = new de.docware.framework.modules.gui.controls.GuiSeparator();
            separator_Replace_Factory.setName("separator_Replace_Factory");
            separator_Replace_Factory.__internal_setGenerationDpi(96);
            separator_Replace_Factory.registerTranslationHandler(translationHandler);
            separator_Replace_Factory.setScaleForResolution(true);
            separator_Replace_Factory.setMinimumHeight(6);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag separator_Replace_FactoryConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 6, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            separator_Replace_Factory.setConstraints(separator_Replace_FactoryConstraints);
            panel_secondChild.addChild(separator_Replace_Factory);
            panel_Factory = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_Factory.setName("panel_Factory");
            panel_Factory.__internal_setGenerationDpi(96);
            panel_Factory.registerTranslationHandler(translationHandler);
            panel_Factory.setScaleForResolution(true);
            panel_Factory.setMinimumWidth(10);
            panel_Factory.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_FactoryLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_Factory.setLayout(panel_FactoryLayout);
            dockingpanel_Factory = new de.docware.framework.modules.gui.controls.GuiDockingPanel();
            dockingpanel_Factory.setName("dockingpanel_Factory");
            dockingpanel_Factory.__internal_setGenerationDpi(96);
            dockingpanel_Factory.registerTranslationHandler(translationHandler);
            dockingpanel_Factory.setScaleForResolution(true);
            dockingpanel_Factory.setMinimumWidth(10);
            dockingpanel_Factory.setMinimumHeight(10);
            dockingpanel_Factory.setTextHide("!!Werkseinsatzdaten verbergen");
            dockingpanel_Factory.setTextShow("!!Werkseinsatzdaten anzeigen");
            dockingpanel_Factory.setImageHide(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelNorth"));
            dockingpanel_Factory.setImageShow(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignDockingPanelSouth"));
            dockingpanel_Factory.setDockPosition(de.docware.framework.modules.gui.controls.misc.DWBorderPosition.NORTH);
            dockingpanel_Factory.setButtonFill(true);
            panel_Docking_Factory = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_Docking_Factory.setName("panel_Docking_Factory");
            panel_Docking_Factory.__internal_setGenerationDpi(96);
            panel_Docking_Factory.registerTranslationHandler(translationHandler);
            panel_Docking_Factory.setScaleForResolution(true);
            panel_Docking_Factory.setMinimumWidth(10);
            panel_Docking_Factory.setMinimumHeight(50);
            de.docware.framework.modules.gui.layout.LayoutBorder panel_Docking_FactoryLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panel_Docking_Factory.setLayout(panel_Docking_FactoryLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_Docking_FactoryConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_Docking_Factory.setConstraints(panel_Docking_FactoryConstraints);
            dockingpanel_Factory.addChild(panel_Docking_Factory);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder dockingpanel_FactoryConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            dockingpanel_Factory.setConstraints(dockingpanel_FactoryConstraints);
            panel_Factory.addChild(dockingpanel_Factory);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panel_FactoryConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 7, 1, 1, 0.0, 0.0, "c", "h", 0, 0, 0, 0);
            panel_Factory.setConstraints(panel_FactoryConstraints);
            panel_secondChild.addChild(panel_Factory);
            label_0 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_0.setName("label_0");
            label_0.__internal_setGenerationDpi(96);
            label_0.registerTranslationHandler(translationHandler);
            label_0.setScaleForResolution(true);
            label_0.setMinimumWidth(10);
            label_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 8, 1, 1, 100.0, 100.0, "c", "n", 0, 0, 0, 0);
            label_0.setConstraints(label_0Constraints);
            panel_secondChild.addChild(label_0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_secondChildConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_secondChild.setConstraints(panel_secondChildConstraints);
            scrollpane_secondChild.addChild(panel_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollpane_secondChildConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollpane_secondChild.setConstraints(scrollpane_secondChildConstraints);
            splitpaneElements_secondChild.addChild(scrollpane_secondChild);
            splitpaneElements.addChild(splitpaneElements_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneElementsConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneElements.setConstraints(splitpaneElementsConstraints);
            panelMain.addChild(splitpaneElements);
            panel_0 = new de.docware.framework.modules.gui.controls.GuiPanel();
            panel_0.setName("panel_0");
            panel_0.__internal_setGenerationDpi(96);
            panel_0.registerTranslationHandler(translationHandler);
            panel_0.setScaleForResolution(true);
            panel_0.setMinimumWidth(10);
            panel_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panel_0Layout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panel_0.setLayout(panel_0Layout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panel_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panel_0Constraints.setPosition("south");
            panel_0.setConstraints(panel_0Constraints);
            panelMain.addChild(panel_0);
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