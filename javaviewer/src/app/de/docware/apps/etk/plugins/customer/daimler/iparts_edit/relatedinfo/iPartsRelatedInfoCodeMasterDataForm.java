/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModulesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsCodeMatrixDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.date.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

public class iPartsRelatedInfoCodeMasterDataForm extends AbstractRelatedInfoPartlistDataForm {

    public static final String IPARTS_MENU_ITEM_SHOW_CODE_MASTER_DATA = "iPartsMenuItemShowCodeMasterData";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.EDSRetail, iPartsModuleTypes.SA_TU,
                                                                                   iPartsModuleTypes.WorkshopMaterial,
                                                                                   iPartsModuleTypes.DialogRetail,
                                                                                   iPartsModuleTypes.CAR_PERSPECTIVE,
                                                                                   iPartsModuleTypes.Dialog_SM_Construction,
                                                                                   iPartsModuleTypes.CONSTRUCTION_MODEL_MBS,
                                                                                   iPartsModuleTypes.MBS_LIST_NUMBER_Construction,
                                                                                   iPartsModuleTypes.MBS_CON_GROUP_Construction,
                                                                                   iPartsModuleTypes.EDS_SAA_SCOPE_Construction,
                                                                                   iPartsModuleTypes.EDS_SAA_SUB_MODULE_Construction);

    private iPartsCodeMatrixDialog codeMatrixDialog;

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_CODE_MASTER_DATA, "!!Code-Stammdaten anzeigen",
                                EditDefaultImages.edit_code.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_CODE_MASTER_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        if (!isEditContext(connector, false)) {
            // In der Stücklistenanzeige die Codeerklärung anzeigen
            updatePartListPopupMenuForEdit(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_CODE_MASTER_DATA, VALID_MODULE_TYPES);
        } else {
            // DAIMLER-3981: Codeerklärung wird ausgeblendet wenn ein Autorenauftrag aktiviert ist, um Kontextmenü übersichtlich zu halten
            // DAIMLER-8189: Edit hat selbst einen Toolbar/Contextmenu-Eintrag => ausblenden
            updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_CODE_MASTER_DATA, false);
        }
    }

    public static boolean relatedInfoIsVisible(EtkDataAssembly assembly) {
        return relatedInfoIsVisible(assembly, VALID_MODULE_TYPES);
    }

    public iPartsRelatedInfoCodeMasterDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        codeMatrixDialog = new iPartsCodeMatrixDialog(getConnector(), this, null);
        codeMatrixDialog.showCodeDNF(false);
        codeMatrixDialog.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelMain.removeAllChildren();
        mainWindow.panelMain.addChild(codeMatrixDialog.getGui());
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    /**
     * Holt sich die Haupt-Gui-Komponente
     *
     * @return Die Hautp-Gui-Komponente (form oder window)
     */
    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CODE, iPartsConst.FIELD_DC_CODE_ID, false, false);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_CODE, iPartsConst.FIELD_DC_DESC, false, false);
        defaultDisplayFields.add(displayField);
        return defaultDisplayFields;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().getActiveRelatedSubForm() == this) {
            EtkDataPartListEntry entry = getConnector().getRelatedInfoData().getAsPartListEntry(getConnector().getProject());

            // Feldname für den Code bestimmen
            boolean isDIALOGConstructionPartList = false;
            boolean isMBSConstructionPartList = false;
            boolean isEDSConstructionPartList = false;
            String codeFieldName = null;
            EtkDataAssembly ownerAssembly = entry.getOwnerAssembly();
            if (relatedInfoIsVisible(ownerAssembly, iPartsModuleTypes.EDITABLE_MODULE_TYPES)) {
                // Im Edit die Original-Code-Regel in der Code-Matrix anzeigen und ansonsten die (evtl.) reduzierte Code-Regel
                // über das virtuelle Feld
                codeFieldName = ((sender != null) && isEditContext(sender.getConnector(), false)) ? iPartsConst.FIELD_K_CODES
                                                                                                  : iPartsDataVirtualFieldsDefinition.RETAIL_CODES_FILTERED;
            } else if (isDIALOGConstructionPartList(ownerAssembly)) {
                isDIALOGConstructionPartList = true;
                codeFieldName = iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES;
            } else if (isMBSAssemblyLevelWithCode(ownerAssembly)) {
                isMBSConstructionPartList = true;
                codeFieldName = iPartsDataVirtualFieldsDefinition.MBS_CODE;
            } else if (isEDSAssemblyLevelWithCode(ownerAssembly)) {
                isEDSConstructionPartList = true;
                codeFieldName = iPartsEdsStructureHelper.getInstance().getVirtualCodeField();
            }

            if (codeFieldName == null) {
                codeMatrixDialog.showCodeMasterDataDefault("");
                return;
            }

            String codeString = entry.getFieldValue(codeFieldName);

            // Wir brauchen Produktgruppe und Baureihe für den aktuellen Stücklisteneintrag
            String productGroup = "";
            String seriesNumber = "";
            String compareDate = "";
            if (!isDIALOGConstructionPartList && !isMBSConstructionPartList && !isEDSConstructionPartList) {
                // Produktgruppe, Baureihe und Vergleichsdatum aus dem Stücklisteneintrag auslesen
                iPartsSeriesId seriesId;

                // Hier keine Logausgabe wegen dem Nachladen von Feldern
                boolean oldLogLoadFieldIfNeeded = entry.isLogLoadFieldIfNeeded();
                entry.setLogLoadFieldIfNeeded(false);
                try {
                    productGroup = entry.getFieldValue(iPartsConst.FIELD_K_PRODUCT_GRP);
                    compareDate = entry.getFieldValue(iPartsConst.FIELD_K_DATEFROM);

                    seriesId = EditConstructionToRetailHelper.getSeriesIdFromDIALOGSourceContext(entry.getFieldValue(iPartsConst.FIELD_K_SOURCE_CONTEXT));
                } finally {
                    entry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
                }

                if (seriesId != null) {
                    seriesNumber = seriesId.getSeriesNumber();
                }

                // Fallback auf das Produkt, wenn Produktgruppe oder Baureihe nicht aus dem Stücklisteneintrag bestimmt werden können
                if (productGroup.isEmpty() || seriesNumber.isEmpty()) {
                    // Produkt für Modul herausfinden (ist speziell auch dann wichtig, wenn ein Aggregate-Modul einem Fahrzeug-Produkt
                    // zugemischt wurde)
                    iPartsDataProductModulesList productModulesList = iPartsDataProductModulesList.loadDataProductModulesList(getProject(), ownerAssembly.getAsId());
                    if (!productModulesList.isEmpty()) {
                        String productNumber = productModulesList.get(0).getAsId().getProductNumber();
                        Logger.log(iPartsPlugin.LOG_CHANNEL_CODES, LogType.DEBUG, "Series number and/or product group unknown for part list entry \""
                                                                                  + entry.getAsId().toString(", ") + "\" (series number: \""
                                                                                  + seriesNumber + "\"; product group: \""
                                                                                  + productGroup + "\"). Fallback to surrounding product \""
                                                                                  + productNumber + "\".");
                        iPartsProduct product = iPartsProduct.getInstance(getProject(), new iPartsProductId(productNumber));
                        if (productGroup.isEmpty()) {
                            productGroup = product.getProductGroup();
                        }
                        if (seriesNumber.isEmpty()) {
                            seriesId = product.getReferencedSeries();
                            if (seriesId != null) {
                                seriesNumber = seriesId.getSeriesNumber();
                            }
                        }
                    }
                }
            } else if (isDIALOGConstructionPartList) { // DIALOG Konstruktions-Stückliste
                productGroup = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_PRODUCT_GRP);
                seriesNumber = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO);
                compareDate = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA);
            } else if (isMBSConstructionPartList) { // MBS Konstruktions-Stückliste
                compareDate = entry.getFieldValue(iPartsDataVirtualFieldsDefinition.MBS_RELEASE_FROM);
                codeMatrixDialog.showCodeMasterDataMBS(codeString, compareDate);
                return;
            } else if (isEDSConstructionPartList) { // EDS Konstruktions-Stückliste
                iPartsEdsStructureHelper structureHelper = iPartsEdsStructureHelper.getInstance();
                productGroup = entry.getFieldValue(structureHelper.getVirtualProductGroupSignField());
                compareDate = entry.getFieldValue(structureHelper.getVirtualReleaseFromField());
                // Ist kein gültiges Datum gesetzt, soll das aktuelle Datum verwendet werden
                if (!DateUtils.isValidDateTime_yyyyMMddHHmmss(compareDate)) {
                    compareDate = DateUtils.toyyyyMMddHHmmss_Calendar(Calendar.getInstance());
                }
                codeMatrixDialog.showCodeMasterDataEDS(codeString, productGroup, compareDate);
                return;
            }
            codeMatrixDialog.showCodeMasterDataDefault(codeString, seriesNumber, productGroup, compareDate);
        }
    }

    public boolean isMBSAssemblyLevelWithCode(EtkDataAssembly assemblyParam) {
        boolean result = relatedInfoIsVisible(assemblyParam, EnumSet.of(iPartsModuleTypes.MBS_LIST_NUMBER_Construction,
                                                                        iPartsModuleTypes.MBS_CON_GROUP_Construction));
        if (!result) {
            if (relatedInfoIsVisible(assemblyParam, EnumSet.of(iPartsModuleTypes.CONSTRUCTION_MODEL_MBS))) {
                return assemblyParam.getEbeneName().equals(iPartsModuleTypes.CONSTRUCTION_MODEL_MBS.getDbValue());
            }
        }
        return result;
    }

    public boolean isEDSAssemblyLevelWithCode(EtkDataAssembly assemblyParam) {
        return relatedInfoIsVisible(assemblyParam, EnumSet.of(iPartsModuleTypes.EDS_SAA_SCOPE_Construction,
                                                              iPartsModuleTypes.EDS_SAA_SUB_MODULE_Construction));
    }

    public boolean isPSKAssemblyLevelWithCode(EtkDataAssembly assemblyParam) {
        return relatedInfoIsVisible(assemblyParam, EnumSet.of(iPartsModuleTypes.PSK_PKW,
                                                              iPartsModuleTypes.PSK_TRUCK));
    }

    private void closeWindow(Event event) {
        close();
        mainWindow.setVisible(false);
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneMainTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCodeString;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollPaneCodeString;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextArea textareaCodeString;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneMainBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCodeExplanation;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpaneCode;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneCodeTop;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpaneCodeBottom;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setTitle("!!Code-Stammdaten");
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            panelMain.setPaddingLeft(8);
            panelMain.setPaddingRight(8);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitpaneMain = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneMain.setName("splitpaneMain");
            splitpaneMain.__internal_setGenerationDpi(96);
            splitpaneMain.registerTranslationHandler(translationHandler);
            splitpaneMain.setScaleForResolution(true);
            splitpaneMain.setMinimumWidth(10);
            splitpaneMain.setMinimumHeight(10);
            splitpaneMain.setHorizontal(false);
            splitpaneMain.setDividerPosition(100);
            splitpaneMainTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneMainTop.setName("splitpaneMainTop");
            splitpaneMainTop.__internal_setGenerationDpi(96);
            splitpaneMainTop.registerTranslationHandler(translationHandler);
            splitpaneMainTop.setScaleForResolution(true);
            splitpaneMainTop.setMinimumWidth(0);
            splitpaneMainTop.setMinimumHeight(50);
            splitpaneMainTop.setPaddingTop(4);
            splitpaneMainTop.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneMainTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneMainTop.setLayout(splitpaneMainTopLayout);
            labelCodeString = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCodeString.setName("labelCodeString");
            labelCodeString.__internal_setGenerationDpi(96);
            labelCodeString.registerTranslationHandler(translationHandler);
            labelCodeString.setScaleForResolution(true);
            labelCodeString.setMinimumWidth(10);
            labelCodeString.setMinimumHeight(10);
            labelCodeString.setPaddingBottom(4);
            labelCodeString.setText("!!Codebedingung:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelCodeStringConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelCodeStringConstraints.setPosition("north");
            labelCodeString.setConstraints(labelCodeStringConstraints);
            splitpaneMainTop.addChild(labelCodeString);
            scrollPaneCodeString = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollPaneCodeString.setName("scrollPaneCodeString");
            scrollPaneCodeString.__internal_setGenerationDpi(96);
            scrollPaneCodeString.registerTranslationHandler(translationHandler);
            scrollPaneCodeString.setScaleForResolution(true);
            scrollPaneCodeString.setMinimumWidth(0);
            scrollPaneCodeString.setMinimumHeight(0);
            scrollPaneCodeString.setBorderWidth(1);
            scrollPaneCodeString.setBorderColor(new de.docware.framework.modules.gui.misc.color.FrameworkConstantColor("clDesignTextFieldBorder"));
            textareaCodeString = new de.docware.framework.modules.gui.controls.GuiTextArea();
            textareaCodeString.setName("textareaCodeString");
            textareaCodeString.__internal_setGenerationDpi(96);
            textareaCodeString.registerTranslationHandler(translationHandler);
            textareaCodeString.setScaleForResolution(true);
            textareaCodeString.setMinimumWidth(0);
            textareaCodeString.setMinimumHeight(0);
            textareaCodeString.setEditable(false);
            textareaCodeString.setLineWrap(true);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder textareaCodeStringConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            textareaCodeString.setConstraints(textareaCodeStringConstraints);
            scrollPaneCodeString.addChild(textareaCodeString);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder scrollPaneCodeStringConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            scrollPaneCodeString.setConstraints(scrollPaneCodeStringConstraints);
            splitpaneMainTop.addChild(scrollPaneCodeString);
            splitpaneMain.addChild(splitpaneMainTop);
            splitpaneMainBottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneMainBottom.setName("splitpaneMainBottom");
            splitpaneMainBottom.__internal_setGenerationDpi(96);
            splitpaneMainBottom.registerTranslationHandler(translationHandler);
            splitpaneMainBottom.setScaleForResolution(true);
            splitpaneMainBottom.setMinimumWidth(0);
            splitpaneMainBottom.setMinimumHeight(0);
            splitpaneMainBottom.setPaddingTop(8);
            splitpaneMainBottom.setPaddingBottom(8);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneMainBottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneMainBottom.setLayout(splitpaneMainBottomLayout);
            labelCodeExplanation = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCodeExplanation.setName("labelCodeExplanation");
            labelCodeExplanation.__internal_setGenerationDpi(96);
            labelCodeExplanation.registerTranslationHandler(translationHandler);
            labelCodeExplanation.setScaleForResolution(true);
            labelCodeExplanation.setMinimumWidth(10);
            labelCodeExplanation.setMinimumHeight(10);
            labelCodeExplanation.setPaddingBottom(4);
            labelCodeExplanation.setText("!!Codeerklärung:");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelCodeExplanationConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelCodeExplanationConstraints.setPosition("north");
            labelCodeExplanation.setConstraints(labelCodeExplanationConstraints);
            splitpaneMainBottom.addChild(labelCodeExplanation);
            splitpaneCode = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpaneCode.setName("splitpaneCode");
            splitpaneCode.__internal_setGenerationDpi(96);
            splitpaneCode.registerTranslationHandler(translationHandler);
            splitpaneCode.setScaleForResolution(true);
            splitpaneCode.setMinimumWidth(10);
            splitpaneCode.setMinimumHeight(10);
            splitpaneCode.setHorizontal(false);
            splitpaneCode.setDividerPosition(147);
            splitpaneCodeTop = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneCodeTop.setName("splitpaneCodeTop");
            splitpaneCodeTop.__internal_setGenerationDpi(96);
            splitpaneCodeTop.registerTranslationHandler(translationHandler);
            splitpaneCodeTop.setScaleForResolution(true);
            splitpaneCodeTop.setMinimumWidth(0);
            splitpaneCodeTop.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneCodeTopLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneCodeTop.setLayout(splitpaneCodeTopLayout);
            splitpaneCode.addChild(splitpaneCodeTop);
            splitpaneCodeBottom = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpaneCodeBottom.setName("splitpaneCodeBottom");
            splitpaneCodeBottom.__internal_setGenerationDpi(96);
            splitpaneCodeBottom.registerTranslationHandler(translationHandler);
            splitpaneCodeBottom.setScaleForResolution(true);
            splitpaneCodeBottom.setMinimumWidth(0);
            splitpaneCodeBottom.setMinimumHeight(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpaneCodeBottomLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpaneCodeBottom.setLayout(splitpaneCodeBottomLayout);
            splitpaneCode.addChild(splitpaneCodeBottom);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneCodeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneCode.setConstraints(splitpaneCodeConstraints);
            splitpaneMainBottom.addChild(splitpaneCode);
            splitpaneMain.addChild(splitpaneMainBottom);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpaneMain.setConstraints(splitpaneMainConstraints);
            panelMain.addChild(splitpaneMain);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCloseActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    closeWindow(event);
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