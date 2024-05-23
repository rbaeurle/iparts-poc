/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.mainview.connector.MechanicFormConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.events.GotoPartWithPartialPathEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.NavigationPath;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.base.viewermain.forms.JavaViewerMainWindow;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditTransferPartlistEntriesWithPredictionForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.collections.dwlist.DwList;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * RelatedInfoForm für den Verwendungsnachweis von DIALOG-Konstruktions-Stücklistenpositionen im AS
 */
public class iPartsRelatedInfoDialogUsageDataForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_DIALOG_USAGE = "iPartsMenuItemShowDialogUsage";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.Dialog_SM_Construction);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_DIALOG_USAGE, iPartsConst.RELATED_INFO_DIALOG_USAGE_TEXT,
                                DefaultImages.usageAssemblyToPart.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_DIALOG_USAGE_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste gültig für den gewünschten Modultyp?
        boolean isValidAssemblyForDataObjectGrid = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            EtkDataAssembly assembly = connector.getCurrentAssembly();
            isValidAssemblyForDataObjectGrid = relatedInfoIsVisible(assembly);
        }
        if (isValidAssemblyForDataObjectGrid) {
            EtkDataPartListEntry partListEntry = connector.getSelectedPartListEntries().get(0);
            isValidAssemblyForDataObjectGrid = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(RETAIL_ASSIGNED);
        }
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_DIALOG_USAGE, isValidAssemblyForDataObjectGrid);
    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, AbstractJavaViewerForm formWithTree) {
        // kein Menu im Tree vorhanden
//        modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_DIALOG_USAGE, iPartsConst.RELATED_INFO_DIALOG_USAGE_TEXT,
//                            iPartsConst.CONFIG_KEY_RELATED_INFO_DIALOG_USAGE_DATA);
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        // keine Action nötig, da kein eigenes Menu vorhanden
//        updateTreePopupMenu(popupMenu, currentTreeNode, IPARTS_MENU_ITEM_SHOW_DIALOG_USAGE, VALID_MODULE_TYPES);
    }

    public static boolean relatedInfoIsVisible(EtkDataAssembly assembly) {
        return relatedInfoIsVisible(assembly, VALID_MODULE_TYPES) && iPartsRight.EDIT_PARTS_DATA.checkRightInSession();
    }

    // Dummy Feldnamen
    private static final String FIELD_DPM_PSEUDO_EINPAS_HG = "DPM_PSEUDO_EINPAS_HG";
    private static final String FIELD_DPM_PSEUDO_EINPAS_G = "DPM_PSEUDO_EINPAS_G";
    private static final String FIELD_DPM_PSEUDO_EINPAS_TU = "DPM_PSEUDO_EINPAS_TU";
    private static final String FIELD_DPM_PSEUDO_KG = "DPM_PSEUDO_KG";
    private static final String FIELD_DPM_PSEUDO_TU = "DPM_PSEUDO_TU";
    private static final String FIELD_DPM_PSEUDO_DESCR = "DPM_PSEUDO_DESCR";
    private static final String FIELD_DPM_PSEUDO_KLFDNR = "DPM_PSEUDO_KLFDNR";
    private static final String FIELD_DPM_PSEUDO_PRODUCT_DESCR = "DPM_PSEUDO_PRODUCT_DESCR";

    boolean isEinPAS = false;
    private GuiButton buttonTransferToASPartList;

    protected iPartsRelatedInfoDialogUsageDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, "", iPartsConst.RELATED_INFO_DIALOG_USAGE_TEXT);
        grid.getTable().addEventListener(new EventListener(Event.MOUSE_DOUBLECLICKED_EVENT) {
            @Override
            public void fire(Event event) {
                gotoPartOrEdit(event, false);
            }
        });
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        List<EtkDataPartListEntry> retailPartListEntries = new DwList<EtkDataPartListEntry>();
        String sourceGuid;
        EtkDataPartListEntry entry = getConnector().getRelatedInfoData().getAsPartListEntry(getConnector().getProject());
        EtkDataAssembly assembly = entry.getOwnerAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            if (((iPartsDataAssembly)assembly).getAsId().isVirtual()) {
                sourceGuid = entry.getFieldValue(FIELD_K_LFDNR);
                retailPartListEntries = EditConstructionToRetailHelper.getRetailSourceGuidPartListEntries(iPartsEntrySourceType.DIALOG,
                                                                                                          sourceGuid, null, getProject());
            }
        }
        if (!retailPartListEntries.isEmpty()) {
            DBDataObjectList<iPartsDataProductModules> resultList = new DBDataObjectList<iPartsDataProductModules>();
            for (EtkDataPartListEntry retailPartListEntry : retailPartListEntries) {
                EtkDataAssembly retailAssembly = retailPartListEntry.getOwnerAssembly();
                iPartsDataProductModulesList products = iPartsDataProductModulesList.loadDataProductModulesList(getProject(), retailAssembly.getAsId());
                if (!products.isEmpty()) {
                    for (iPartsDataProductModules dataProductModules : products) {
                        String value;
                        String dbLanguage = getConfig().getCurrentDatabaseLanguage();
                        List<String> dataBaseFallbackLanguages = getProject().getDataBaseFallbackLanguages();
                        iPartsProductId productId = new iPartsProductId(dataProductModules.getAsId().getProductNumber());
                        KgTuNode kgTuNode = getKgTuNode(productId, retailAssembly.getAsId());
                        if ((kgTuNode != null) && (kgTuNode.getParent() instanceof KgTuNode)) {
                            KgTuNode kgNode = (KgTuNode)kgTuNode.getParent();
                            if (kgNode != null) {
                                value = kgNode.getNumberAndTitle(dbLanguage, dataBaseFallbackLanguages);
                            } else {
                                value = ""; // leere KG
                            }
                            dataProductModules.getAttributes().addField(FIELD_DPM_PSEUDO_KG, value, DBActionOrigin.FROM_DB);
                            dataProductModules.getAttributes().addField(FIELD_DPM_PSEUDO_TU, kgTuNode.getNumberAndTitle(dbLanguage, dataBaseFallbackLanguages), DBActionOrigin.FROM_DB);
                        } else {
                            dataProductModules.getAttributes().addField(FIELD_DPM_PSEUDO_KG, "", DBActionOrigin.FROM_DB);
                            dataProductModules.getAttributes().addField(FIELD_DPM_PSEUDO_TU, "", DBActionOrigin.FROM_DB);
                        }
                        value = retailAssembly.getPart().getFieldValue(EtkDbConst.FIELD_M_TEXTNR, dbLanguage, true);
                        dataProductModules.getAttributes().addField(FIELD_DPM_PSEUDO_DESCR, value, DBActionOrigin.FROM_DB);
                        String productName = iPartsProduct.getInstance(getProject(), productId).getProductTitle(getProject()).getTextByNearestLanguage(dbLanguage, dataBaseFallbackLanguages);
                        dataProductModules.getAttributes().addField(FIELD_DPM_PSEUDO_PRODUCT_DESCR, productName, DBActionOrigin.FROM_DB);
                        dataProductModules.getAttributes().addField(FIELD_DPM_PSEUDO_KLFDNR, retailPartListEntry.getAsId().getKLfdnr(), DBActionOrigin.FROM_DB);
                        resultList.add(dataProductModules, DBActionOrigin.FROM_DB);
                    }
                }
            }
            return resultList;
        }
        return null;
    }

    @Override
    protected void postCreateGui() {
        title = null;
        super.postCreateGui();
        addAdditionalElements();
    }

    /**
     * Erstellt den Button zum Absrpingen in die Übernahme der aktuellen Konstruktionsposition in AS
     */
    private void addAdditionalElements() {
        GuiPanel panel = new GuiPanel();
        buttonTransferToASPartList = new GuiButton();
        buttonTransferToASPartList.setText("!!In AS-Stückliste übernehmen");
        buttonTransferToASPartList.setName("buttonTransferToASPartList");
        panel.setBorderWidth(8);
        panel.setLayout(new LayoutBorder());
        panel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_SOUTH));
        buttonTransferToASPartList.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_EAST));
        panel.addChild(buttonTransferToASPartList);
        getGui().addChild(panel);
        buttonTransferToASPartList.setVisible(iPartsRight.EDIT_PARTS_DATA.checkRightInSession());
        buttonTransferToASPartList.setEnabled(getConnector().getProject().getEtkDbs().isRevisionChangeSetActiveForEdit());
        buttonTransferToASPartList.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                startTransferToASPartList(event);
            }
        });
    }

    @Override
    protected DataObjectFilterGrid createGrid() {
        DataObjectFilterGrid myGrid = new DataObjectFilterGrid(getConnector(), this) {
            private GuiMenuItem gotoMenuItem;
            private GuiMenuItem editMenuItem;

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                gotoMenuItem = new GuiMenuItem();
                gotoMenuItem.setText("!!Gehe zu Verwendung");
                gotoMenuItem.setIcon(DefaultImages.usageAssemblyToPart.getImage());
                gotoMenuItem.setName("gotoMenuItem");
                gotoMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        gotoPartOrEdit(event, false);
                    }
                });
                contextMenu.addChild(gotoMenuItem);

                editMenuItem = new GuiMenuItem();
                editMenuItem.setText("!!Modul bearbeiten");
                editMenuItem.setIcon(DefaultImages.module.getImage());
                editMenuItem.setName("editMenuItem");
                editMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        gotoPartOrEdit(event, true);
                    }
                });
                contextMenu.addChild(editMenuItem);
            }


            @Override
            protected void onTableSelectionChanged(Event event) {
                boolean isSingleSelected = isSingleSelected();
                gotoMenuItem.setEnabled(isSingleSelected);
                editMenuItem.setEnabled(isSingleSelected);
            }

        };
        return myGrid;
    }

    /**
     * Sprung zu "Modul bearbeiten" oder direkt in die AS-Stückliste
     *
     * @param event
     * @param isEdit
     */
    private void gotoPartOrEdit(Event event, boolean isEdit) {
        List<EtkDataObject> selection = grid.getSelection();
        if ((selection != null) && (selection.size() == 1)) {
            iPartsDataProductModules dataProductModules = (iPartsDataProductModules)selection.get(0);
            AssemblyId assemblyId = new AssemblyId(dataProductModules.getAsId().getModuleNumber(), "");
            if (!closeRelatedInfoFormIfNotEmbedded()) { // Erst schließen, dann springen
                return;
            }
            String kLfdNr = dataProductModules.getFieldValue(FIELD_DPM_PSEUDO_KLFDNR);
            if (isEdit) {
                JavaViewerMainWindow mainWindow = getConnector().getMainWindow();
                List<AbstractJavaViewerMainFormContainer> editModuleForms = mainWindow.getFormsFromClass(EditModuleForm.class);
                if (!editModuleForms.isEmpty()) {
                    EditModuleForm editModuleForm = (EditModuleForm)editModuleForms.get(0);
                    mainWindow.displayForm(editModuleForm);
                    editModuleForm.loadModule(assemblyId.getKVari(), kLfdNr);
                } else {
//                    MessageDialog.showError("!!Funktion \"Module bearbeiten\" nicht gefunden!");
                }
            } else {
                GotoPartWithPartialPathEvent partWithPartialPathEvent = new GotoPartWithPartialPathEvent(new NavigationPath(),
                                                                                                         assemblyId,
                                                                                                         kLfdNr,
                                                                                                         false, false, true, this);
                getProject().fireProjectEvent(partWithPartialPathEvent);
            }
        }
    }

    /**
     * Sprung zur Übernahme von Kosntruktionsstücklistenposition in AS-Stücklisten
     *
     * @param event
     */
    private void startTransferToASPartList(Event event) {
        if (!closeRelatedInfoFormIfNotEmbedded()) { // Erst schließen, dann springen
            return;
        }
        AssemblyListFormIConnector connector = new MechanicFormConnector(getConnector());
        EtkDataPartListEntry entry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        EtkDataAssembly assembly = entry.getOwnerAssembly();
        connector.setCurrentAssembly(assembly);
        List<EtkDataPartListEntry> partListEntryList = new DwList<EtkDataPartListEntry>();
        partListEntryList.add(entry);
        connector.setSelectedPartListEntries(partListEntryList);
        EditTransferPartlistEntriesWithPredictionForm.startTransferToASPartList(connector, EditTransferPartlistEntriesWithPredictionForm.TransferMode.PARTLIST);
    }

    /**
     * Liefert den KGTU Knoten für das übergebene Modul im übergebenen Produkt
     *
     * @param productId
     * @param assemblyId
     * @return
     */
    private KgTuNode getKgTuNode(iPartsProductId productId, AssemblyId assemblyId) {
        KgTuForProduct kgTuProduct = KgTuForProduct.getInstance(getProject(), productId);
        iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForProductAndModule(getProject(), productId, assemblyId);
        KgTuId kgTuId = new KgTuId(moduleEinPASList.get(0).getFieldValue(FIELD_DME_SOURCE_KG),
                                   moduleEinPASList.get(0).getFieldValue(FIELD_DME_SOURCE_TU));
        return kgTuProduct.getTuNode(kgTuId.getKg(), kgTuId.getTu());
    }

    /**
     * Überschrieben, damit keine DisplayFields via configKey geladen werden
     *
     * @return
     */
    @Override
    protected EtkDisplayFields getDisplayFields() {
        EtkDisplayFields displayResultFields = new EtkDisplayFields();
        List<EtkDisplayField> defaultDisplayFields = createDefaultDisplayFields();
        if (defaultDisplayFields != null) {
            for (EtkDisplayField defaultDisplayField : defaultDisplayFields) {
                displayResultFields.addFeld(defaultDisplayField);
            }
        }
        return displayResultFields;
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        List<String> dbLanguages = getProject().getConfig().getDatabaseLanguages();

        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PSEUDO_PRODUCT_DESCR, false, false);
        //displayField.setColumnFilterEnabled(true);
        displayField.setDefaultText(false);
        EtkMultiSprache multi = new EtkMultiSprache("!!Produkt", dbLanguages);
        displayField.setText(multi);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PSEUDO_EINPAS_HG, false, false);
        displayField.setDefaultText(false);
        multi = new EtkMultiSprache("!!EinPAS HG", dbLanguages);
        displayField.setText(multi);
        displayField.setVisible(isEinPAS);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PSEUDO_EINPAS_G, false, false);
        displayField.setDefaultText(false);
        multi = new EtkMultiSprache("!!EinPAS G", dbLanguages);
        displayField.setText(multi);
        displayField.setVisible(isEinPAS);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PSEUDO_EINPAS_TU, false, false);
        displayField.setDefaultText(false);
        multi = new EtkMultiSprache("!!EinPAS TU", dbLanguages);
        displayField.setText(multi);
        displayField.setVisible(isEinPAS);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PSEUDO_KG, false, false);
        displayField.setDefaultText(false);
        multi = new EtkMultiSprache("!!KG", dbLanguages);
        displayField.setText(multi);
        displayField.setVisible(!isEinPAS);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PSEUDO_TU, false, false);
        displayField.setDefaultText(false);
        multi = new EtkMultiSprache("!!TU", dbLanguages);
        displayField.setText(multi);
        displayField.setVisible(!isEinPAS);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_MODULE_NO, false, false);
//        displayField.setColumnFilterEnabled(true);
        displayField.setDefaultText(false);
        multi = new EtkMultiSprache("!!Modul", dbLanguages);
        displayField.setText(multi);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PSEUDO_DESCR, true, false);
        displayField.setDefaultText(false);
        multi = new EtkMultiSprache("!!Benennung", dbLanguages);
        displayField.setText(multi);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PSEUDO_KLFDNR, true, false);
        displayField.setVisible(false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }
}