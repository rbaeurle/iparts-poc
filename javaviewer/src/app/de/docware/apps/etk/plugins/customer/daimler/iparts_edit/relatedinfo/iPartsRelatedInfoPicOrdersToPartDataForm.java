/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPASList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderPartsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.picorder.PicOrderMainForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Formular für die Anzeige der Bildaufträge zu einer Sachnummer innerhalb der RelatedInfo.
 */
public class iPartsRelatedInfoPicOrdersToPartDataForm extends AbstractRelatedInfoPartlistDataForm {

    public static final String IPARTS_MENU_ITEM_SHOW_PICORDERS_TO_PART_DATA = "iPartsMenuItemShowPicOrdersToPartData";
    public static final String PICORDERS_TO_PART_DATA_NAME = "/PicOrdersToPart";
    public static final String CONFIG_KEY_PICORDERS_TO_PART_DATA = "Plugin/iPartsEdit" + PICORDERS_TO_PART_DATA_NAME;

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = iPartsModuleTypes.RETAIL_MODULE_TYPES;

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        //siehe {@link iPartsRelatedInfoPicOrdersToPartData} .isBlocked
        if (!iPartsRelatedInfoPicOrdersToPartData.isBlocked) {
            modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_PICORDERS_TO_PART_DATA, "!!Bildauftragzuordnung zu Teil",
                                    EditToolbarButtonAlias.IMG_PICORDER.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_PICORDERS_TO_PART_DATA);
        }
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        if (iPartsRelatedInfoPicOrdersToPartData.isBlocked) {
            return;
        }

        if (isEditContext(connector, false)) {
            updatePartListPopupMenuForEdit(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_PICORDERS_TO_PART_DATA, VALID_MODULE_TYPES);
        } else {
            updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_PICORDERS_TO_PART_DATA, false);
        }
    }

    public static boolean relatedInfoIsVisible(EtkDataAssembly assembly, RelatedInfoBaseFormIConnector connector) {
        if (isEditContext(connector, false)) {
            return relatedInfoIsVisible(assembly, VALID_MODULE_TYPES);
        } else {
            return false;
        }
    }


    private EditDataObjectGrid dataGrid;
    private PartListEntryId loadedPartListEntryId;
    private boolean withOriginalRevisionNo = false;

    /**
     * Erzeugt eine Instanz von iPartsRelatedInfoPicOrdersToPartDataForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    protected iPartsRelatedInfoPicOrdersToPartDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        dataGrid = new EditDataObjectGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                doEnableButtons();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doShowPicOrder(event);
            }

            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
                ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
                holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                    @Override
                    public void run(Event event) {
                        doShowPicOrder(event);
                    }
                });
                mainWindow.contextmenuTable.addChild(holder.menuItem);
                holder.toolbarButton.setTooltip("!!Anzeigen");
                holder.menuItem.setText("!!Anzeigen");
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                List<AbstractGuiControl> menuList = new DwList<AbstractGuiControl>(mainWindow.contextmenuTable.getChildren());
                for (AbstractGuiControl menu : menuList) {
                    mainWindow.contextmenuTable.removeChild(menu);
                    contextMenu.addChild(menu);
                }
            }
        };
        dataGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        dataGrid.getGui().setConstraints(constraints);
        panelMain.panelTableGrid.addChild(dataGrid.getGui());
        doEnableButtons();
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        PartListEntryId currentPartListEntryId = getConnector().getRelatedInfoData().getAsPartListEntryId();
        if ((getConnector().getActiveRelatedSubForm() == this) && !Utils.objectEquals(currentPartListEntryId, loadedPartListEntryId)) {
            loadedPartListEntryId = currentPartListEntryId;
            picOrderTablesDataToGrid();
        }
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields(String configKey) {
        if (configKey.equals(CONFIG_KEY_PICORDERS_TO_PART_DATA)) {
            return createPicOrderTablesDataDefaultDisplayFields();
        }

        return null;
    }

    @Override
    public AbstractGuiControl getGui() {
        return panelMain;
    }

    private List<EtkDisplayField> createPicOrderTablesDataDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER, iPartsConst.FIELD_DA_PO_ORDER_ID_EXTERN, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER, iPartsConst.FIELD_DA_PO_ORDER_REVISION_EXTERN, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER, iPartsConst.FIELD_DA_PO_PROPOSED_NAME, true, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER, iPartsConst.FIELD_DA_PO_STATUS, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    private void picOrderTablesDataToGrid() {
        if (dataGrid.getDisplayFields() == null) {
            EtkDisplayFields displayFields = getDisplayFields(CONFIG_KEY_PICORDERS_TO_PART_DATA);
            if (withOriginalRevisionNo) {
                if (!displayFields.contains(iPartsConst.TABLE_DA_PICORDER, iPartsConst.FIELD_DA_PO_ORIGINAL_PICORDER, false)) {
                    EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_PICORDER, iPartsConst.FIELD_DA_PO_ORIGINAL_PICORDER, false, false);
                    displayField.loadStandards(getConfig());
                    displayField.setDefaultText(false);
                    displayField.setText(new EtkMultiSprache("!!Original Revisionsnummer", new String[]{ TranslationHandler.getUiLanguage() }));
                    displayFields.addFeld(displayField);
                }
            }
            dataGrid.setDisplayFields(displayFields);
        }

        EtkDataPartListEntry entry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        List<iPartsPicOrderId> guidList = iPartsDataPicOrderPartsList.loadPicOrderListByPart(getProject(), entry.getAsId());
        dataGrid.clearGrid();
        List<iPartsDataPicOrder> picOrderList = new DwList<iPartsDataPicOrder>(guidList.size());
        for (iPartsPicOrderId id : guidList) {
            iPartsDataPicOrder picOrder = new iPartsDataPicOrder(getProject(), id);
            if (picOrder.loadFromDB(id)) {
                picOrderList.add(picOrder);
            }
        }
        Collections.sort(picOrderList, new Comparator<iPartsDataPicOrder>() {
            @Override
            public int compare(iPartsDataPicOrder o1, iPartsDataPicOrder o2) {
                String orderNo1 = o1.getFieldValue(iPartsConst.FIELD_DA_PO_ORDER_ID_EXTERN);
                String orderNo2 = o2.getFieldValue(iPartsConst.FIELD_DA_PO_ORDER_ID_EXTERN);
                if (orderNo1.equals(orderNo2)) {
                    return o1.getFieldValue(iPartsConst.FIELD_DA_PO_ORDER_REVISION_EXTERN).compareTo(o2.getFieldValue(iPartsConst.FIELD_DA_PO_ORDER_REVISION_EXTERN));
                } else {
                    return orderNo1.compareTo(orderNo2);
                }
            }
        });

        if (withOriginalRevisionNo) {
            for (iPartsDataPicOrder picOrder : picOrderList) {
                if (!picOrder.getOriginalPicOrder().isEmpty() && !picOrder.hasFakeOriginalPicOrder()) {
                    iPartsPicOrderId originalPicOrderId = new iPartsPicOrderId(picOrder.getOriginalPicOrder());
                    if (guidList.contains(originalPicOrderId)) {
                        String revNo = "";
                        for (iPartsDataPicOrder picOrder2 : picOrderList) {
                            if (picOrder2.getAsId().equals(originalPicOrderId)) {
                                revNo = picOrder2.getFieldValue(iPartsConst.FIELD_DA_PO_ORDER_REVISION_EXTERN);
                                break;
                            }
                        }
                        picOrder.setOriginalOrder(new iPartsPicOrderId(revNo));
                    } else {
                        picOrder.setOriginalOrder(new iPartsPicOrderId(""));
                    }
                }
            }
        }
        for (iPartsDataPicOrder picOrder : picOrderList) {
            dataGrid.addObjectToGrid(picOrder);
        }
        dataGrid.showNoResultsLabel(picOrderList.isEmpty());
    }

    private void doEnableButtons() {
        List<EtkDataObject> selectedList = dataGrid.getSelection();
        boolean singleSelect = (selectedList != null) && !selectedList.isEmpty();
//        boolean multiSelect = false;
//        if (singleSelect) {
//            multiSelect = !dataGrid.getMultiSelection().isEmpty();
//        }
        dataGrid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, singleSelect);
    }

    private void doShowPicOrder(Event event) {
        List<EtkDataObject> selectedList = dataGrid.getSelection();
        if (!selectedList.isEmpty()) {
            iPartsDataPicOrder dataPicOrder = (iPartsDataPicOrder)selectedList.get(0);
            iPartsConst.PRODUCT_STRUCTURING_TYPE productType = iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU;
            EtkDataPartListEntry entry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
            // SeriesId und EinPASId/KgTuId eintragen, falls möglich
            EtkDataAssembly ownerAssembly = entry.getOwnerAssembly();
            iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForModule(getProject(), ownerAssembly.getAsId());
            if ((moduleEinPASList != null) && (moduleEinPASList.size() == 1)) {
                iPartsProductId productId = new iPartsProductId(moduleEinPASList.get(0).getAsId().getProductNumber());
                iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                productType = product.getProductStructuringType();
            }
            PicOrderMainForm.viewPictureOrderDialog(getConnector(), this, ownerAssembly, dataPicOrder, productType);
        }

    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
        panelMain = new PanelMainClass(translationHandler);
        panelMain.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextmenuTable;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutFlow mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutFlow();
            this.setLayout(mainWindowLayout);
            contextmenuTable = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextmenuTable.setName("contextmenuTable");
            contextmenuTable.__internal_setGenerationDpi(96);
            contextmenuTable.registerTranslationHandler(translationHandler);
            contextmenuTable.setScaleForResolution(true);
            contextmenuTable.setMinimumWidth(10);
            contextmenuTable.setMinimumHeight(10);
            contextmenuTable.setMenuName("contextmenuTable");
            contextmenuTable.setParentControl(this);
        }

    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected PanelMainClass panelMain;

    private class PanelMainClass extends de.docware.framework.modules.gui.controls.GuiPanel {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelTableTitle;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTableGrid;

        private PanelMainClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(panelMainLayout);
            labelTableTitle = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelTableTitle.setName("labelTableTitle");
            labelTableTitle.__internal_setGenerationDpi(96);
            labelTableTitle.registerTranslationHandler(translationHandler);
            labelTableTitle.setScaleForResolution(true);
            labelTableTitle.setMinimumWidth(10);
            labelTableTitle.setMinimumHeight(10);
            labelTableTitle.setPaddingTop(4);
            labelTableTitle.setPaddingLeft(8);
            labelTableTitle.setPaddingRight(8);
            labelTableTitle.setPaddingBottom(4);
            labelTableTitle.setText("!!Zugeordnete Bildaufträge");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder labelTableTitleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            labelTableTitleConstraints.setPosition("north");
            labelTableTitle.setConstraints(labelTableTitleConstraints);
            this.addChild(labelTableTitle);
            panelTableGrid = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTableGrid.setName("panelTableGrid");
            panelTableGrid.__internal_setGenerationDpi(96);
            panelTableGrid.registerTranslationHandler(translationHandler);
            panelTableGrid.setScaleForResolution(true);
            panelTableGrid.setMinimumWidth(10);
            panelTableGrid.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelTableGridLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelTableGrid.setLayout(panelTableGridLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelTableGridConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelTableGrid.setConstraints(panelTableGridConstraints);
            this.addChild(panelTableGrid);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}