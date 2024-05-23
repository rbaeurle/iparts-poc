/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.mechanic.treeview.forms.AssemblyTreeFormIConnector;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataBadCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * RelatedInfoForm für die BAD-Code
 */
public class iPartsRelatedInfoBadCodeDataForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_BAD_CODE = "iPartsMenuItemShowBadCode";

    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.CONSTRUCTION_SERIES);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_BAD_CODE, iPartsConst.RELATED_INFO_BAD_CODE_TEXT,
                                DefaultImages.module.getImage(), iPartsConst.CONFIG_KEY_RELATED_INFO_BAD_CODE_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste "Structure" oder nicht virtuell?
        boolean isValidAssemblyForShowModel = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            EtkDataAssembly assembly = connector.getCurrentAssembly();
            if (assembly instanceof iPartsDataAssembly) {
                if (((iPartsDataAssembly)assembly).getAsId().isVirtual()) {
                    List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
                    if (virtualNodesPath != null) {
                        if (iPartsVirtualNode.isStructureNode(virtualNodesPath)) {
                            // in dem Strukturknoten den selektierten Stücklisteneintrag überprüfen, ob dessen Assembly
                            // eine Baureihe darstellt
                            List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
                            if ((selectedPartListEntries != null) && !selectedPartListEntries.isEmpty()) {
                                EtkDataPartListEntry partListEntry = selectedPartListEntries.get(0);
                                if (partListEntry instanceof iPartsDataPartListEntry) {
                                    virtualNodesPath = ((iPartsDataPartListEntry)partListEntry).getVirtualNodesPathForDestinationAssembly();
                                    if (virtualNodesPath != null) {
                                        if (iPartsVirtualNode.isSeriesNode(virtualNodesPath)) {
                                            isValidAssemblyForShowModel = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_BAD_CODE, isValidAssemblyForShowModel);
    }

    public static void modifyTreePopupMenu(GuiContextMenu menu, AbstractJavaViewerForm formWithTree) {
        modifyTreePopupMenu(menu, formWithTree, IPARTS_MENU_ITEM_SHOW_BAD_CODE, iPartsConst.RELATED_INFO_BAD_CODE_TEXT,
                            iPartsConst.CONFIG_KEY_RELATED_INFO_BAD_CODE_DATA);
    }

    public static void updateTreePopupMenu(GuiContextMenu popupMenu, AssemblyTreeFormIConnector connector) {
        updateTreePopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_BAD_CODE, VALID_MODULE_TYPES);
    }

    protected iPartsRelatedInfoBadCodeDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, iPartsEditConfigConst.iPARTS_EDIT_MASTER_BAD_CODE_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              iPartsConst.RELATED_INFO_BAD_CODE_TEXT);
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        if (getConnector().getActiveRelatedSubForm() == this) {
            super.updateData(sender, forceUpdateAll);
        }
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        String seriesNumber = iPartsVirtualNode.getSeriesNumberFromAssemblyId(getConnector().getRelatedInfoData().getSachAssemblyId());
        return iPartsDataBadCodeList.loadAllBadCodesForSeriesNotExpired(getProject(), seriesNumber);
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_BAD_CODE, FIELD_DBC_CODE_ID, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_BAD_CODE, FIELD_DBC_AA, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_BAD_CODE, FIELD_DBC_EXPIRY_DATE, true, false);
        defaultDisplayFields.add(displayField);
        displayField = new EtkDisplayField(TABLE_DA_BAD_CODE, FIELD_DBC_PERMANENT_BAD_CODE, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }
}
