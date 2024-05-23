/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataPrimusReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsPrimusReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementCacheObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsLoader;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.sql.SQLStringConvert;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 * Formular für die Anzeige der Primus Ersetzungskette innerhalb der Related Info (inkl. Mitlieferdaten)
 */
public class iPartsRelatedInfoPrimusReplacementChainForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    public static final String IPARTS_MENU_ITEM_SHOW_PRIMUS_REPLACEMENT_CHAIN = "iPartsMenuItemShowPrimusReplacementChain";
    public static final String CONFIG_KEY_PRIMUS_REPLACEMENT_CHAIN = "Plugin/iPartsEdit/PrimusReplaceParts";
    public static final EnumSet<iPartsModuleTypes> VALID_MODULE_TYPES = EnumSet.of(iPartsModuleTypes.EDS_SAA_Construction,
                                                                                   iPartsModuleTypes.CTT_SAA_Construction,
                                                                                   iPartsModuleTypes.MBS_CON_GROUP_Construction,
                                                                                   iPartsModuleTypes.Dialog_SM_Construction,
                                                                                   iPartsModuleTypes.EDSRetail,
                                                                                   iPartsModuleTypes.DialogRetail,
                                                                                   iPartsModuleTypes.PSK_PKW,
                                                                                   iPartsModuleTypes.PSK_TRUCK);

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        modifyPartListPopupMenu(popupMenu, connector, IPARTS_MENU_ITEM_SHOW_PRIMUS_REPLACEMENT_CHAIN,
                                iPartsConst.RELATED_INFO_PRIMUS_REPLACE_CHAIN_DATA_TEXT,
                                EditDefaultImages.edit_rep_chain.getImage(),
                                iPartsConst.CONFIG_KEY_RELATED_INFO_PRIMUS_REPLACE_CHAIN_DATA);
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        boolean menuItemVisible = false;
        if (connector.getProject().isEditModeActive() && (connector.getSelectedPartListEntries() != null) && (connector.getSelectedPartListEntries().size() == 1)) {
            boolean isEditRelatedInfo = isEditContext(connector, false);
            if (!isEditRelatedInfo) {
                menuItemVisible = relatedInfoIsVisible(connector.getSelectedPartListEntries().get(0), connector.getProject());
            }
        }

        // Separator und Menüeintrag aktualisieren
        updatePartListPopupMenu(popupMenu, IPARTS_MENU_ITEM_SHOW_PRIMUS_REPLACEMENT_CHAIN, menuItemVisible);
    }

    public static boolean relatedInfoIsVisible(EtkDataPartListEntry entry, EtkProject project) {
        if ((entry != null) && relatedInfoIsVisible(entry.getOwnerAssembly(), VALID_MODULE_TYPES)) {
            // Sollen Primus Ersetzungen überhaupt gezeigt werden?
            iPartsReplacementHelper replacementHelper = new iPartsReplacementHelper();
            if (replacementHelper.isHandlePrimusHints(project, (iPartsDataAssembly)entry.getOwnerAssembly())) {
                iPartsPRIMUSReplacementsCache primusReplacementsCache = iPartsPRIMUSReplacementsCache.getInstance(project);
                iPartsPRIMUSReplacementCacheObject predecessor = primusReplacementsCache.getReplacementCacheObjectForMatNr(entry.getPart().getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR));
                // nur anzeigen, falls es einen potentiellen Nachfolger gibt
                if (predecessor != null) {
                    String successor = predecessor.getSuccessorPartNo();
                    return StrUtils.isValid(successor);
                }
            }
        }
        return false;
    }

    public static boolean hasKatalogField(DataObjectFilterGrid grid) {
        for (EtkDisplayField etkDisplayField : grid.getDisplayFields().getFields()) {
            if (etkDisplayField.getKey().getTableName().equals(TABLE_KATALOG)) {
                return true;
            }
        }
        return false;
    }

    protected EtkDataPartListEntry partListEntry;
    protected PartListEntryId loadedPartListEntryId;
    protected GuiMenuItem includePartsMenuItem;

    protected iPartsRelatedInfoPrimusReplacementChainForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo, CONFIG_KEY_PRIMUS_REPLACEMENT_CHAIN, RELATED_INFO_PRIMUS_REPLACE_CHAIN_DATA_TEXT);
    }

    @Override
    protected DataObjectFilterGrid createGrid() {
        DataObjectFilterGrid myGrid = new DataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                includePartsMenuItem = new GuiMenuItem();
                includePartsMenuItem.setText(AbstractRelatedInfoReplacementDataForm.INCLUDE_IPARTSMENU_TEXT);
                includePartsMenuItem.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(Event event) {
                        iPartsDataPrimusReplacePart primusReplacePart = getSelectedPrimusReplacePart();
                        if (primusReplacePart != null) {
                            // Um Stücklisteneinträge zu den Mitlieferteilen zu finden muss Stückliste bekannt sein und aus der Primusersetzung eine iParts-Ersetzung erzeugt werden
                            EtkDataAssembly assembly = iPartsRelatedInfoPrimusReplacementChainForm.this.getConnector().getRelatedInfoData().getAsPartListEntry(getProject()).getOwnerAssembly();
                            List<EtkDataPartListEntry> filteredPartList = assembly.getPartList(assembly.getEbene());
                            DBDataObjectList<EtkDataPartListEntry> partlist = new DBDataObjectList<>();
                            partlist.addAll(filteredPartList, DBActionOrigin.FROM_DB);
                            iPartsPRIMUSReplacementsLoader primusReplacementsLoader = new iPartsPRIMUSReplacementsLoader(getConnector().getProject(), partlist);

                            iPartsPRIMUSReplacementsCache primusReplacementsCache = iPartsPRIMUSReplacementsCache.getInstance(getProject());
                            iPartsPRIMUSReplacementCacheObject predecessor = primusReplacementsCache.getReplacementCacheObjectForMatNr(primusReplacePart.getFieldValue(FIELD_PRP_PART_NO));
                            iPartsReplacement replacement = null;
                            if (predecessor != null) {
                                EtkDataPartListEntry predecessorPartListEntry = getPartListEntryForPrimusReplacement(filteredPartList, predecessor.getPredecessorPartNo());
                                replacement = iPartsReplacement.createPrimusReplacement(predecessor, (iPartsDataPartListEntry)predecessorPartListEntry, primusReplacementsLoader.getPartNoToPartlistEntryMap());
                            }
                            iPartsPrimusReplacementsIncludePartsForm form = new iPartsPrimusReplacementsIncludePartsForm(parentForm.getConnector(), parentForm, primusReplacePart, replacement, filteredPartList);
                            form.showModal();
                        }
                    }
                });
                contextMenu.addChild(includePartsMenuItem);
                includePartsMenuItem.setEnabled(false);
            }


            @Override
            protected void onTableSelectionChanged(Event event) {
                boolean isSingleSelected = isSingleSelected();
                iPartsDataPrimusReplacePart primusReplacePart = getSelectedPrimusReplacePart();
                if (primusReplacePart != null) {
                    includePartsMenuItem.setEnabled(isSingleSelected && primusReplacePart.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.DPRP_INLCUDE_PARTS_AVAILABLE));
                }
            }

        };
        return myGrid;
    }


    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        PartListEntryId currentPartListEntryId = getConnector().getRelatedInfoData().getAsPartListEntryId();
        if ((getConnector().getActiveRelatedSubForm() == this) && !Utils.objectEquals(currentPartListEntryId, loadedPartListEntryId)) {
            loadedPartListEntryId = currentPartListEntryId;
            partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());

            dataToGrid();
        }
    }


    @Override
    protected void dataToGrid() {
        if (grid.getDisplayFields() == null) {
            grid.setDisplayFields(getDisplayFields());
        }

        EtkDataAssembly assembly = getConnector().getRelatedInfoData().getAsPartListEntry(getProject()).getOwnerAssembly();
        List<EtkDataPartListEntry> filteredPartList = assembly.getPartList(assembly.getEbene());

        grid.clearGrid();
        DBDataObjectList<EtkDataPartListEntry> partlist = new DBDataObjectList<>();
        partlist.addAll(filteredPartList, DBActionOrigin.FROM_DB);
        iPartsPRIMUSReplacementsLoader primusReplacementsLoader = new iPartsPRIMUSReplacementsLoader(getConnector().getProject(), partlist);
        List<iPartsPRIMUSReplacementCacheObject> primusChain = primusReplacementsLoader.getReplacementChain(partListEntry.getPart().getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR));

        if (primusChain != null) {
            boolean hasKatalogFields = hasKatalogField(grid);

            for (iPartsPRIMUSReplacementCacheObject primusReplacementCacheObject : primusChain) {
                iPartsDataPrimusReplacePart primusReplacePart = createAndCopyFromCacheData(primusReplacementCacheObject);
                EtkDataPart part = iPartsPrimusReplacementsIncludePartsForm.createAndSetPartForReplacementChain(getProject(), primusReplacementCacheObject.getPredecessorPartNo());
                if (hasKatalogFields) {
                    EtkDataPartListEntry partListEntry = getPartListEntryForPrimusReplacement(filteredPartList, primusReplacementCacheObject.getPredecessorPartNo());
                    grid.addObjectToGrid(part, primusReplacePart, partListEntry);
                } else {
                    grid.addObjectToGrid(part, primusReplacePart);
                }
            }
            // Wenn der letzte in der Kette noch einen Nachfolger hat, dann muss dieser noch aus MAT Daten erstellt werden
            // Außer der letzte Eintrag hat die Hinweiscode 23. Dann endet die Kette
            iPartsPRIMUSReplacementCacheObject lastPrimusReplacementCacheObject = primusChain.get(primusChain.size() - 1);
            String lastSuccessorPartNo = lastPrimusReplacementCacheObject.getSuccessorPartNo();
            String lastSuccessorForwardCode = lastPrimusReplacementCacheObject.getCodeForward();
            if (StrUtils.isValid(lastSuccessorPartNo) && !lastSuccessorForwardCode.equals(iPartsPRIMUSReplacementsLoader.PRIMUS_CODE_HAS_INCLUDE_PARTS)) {
                EtkDataPart part = iPartsPrimusReplacementsIncludePartsForm.createAndSetPartForReplacementChain(getProject(), lastSuccessorPartNo);
                if (hasKatalogFields) {
                    EtkDataPartListEntry partListEntry = getPartListEntryForPrimusReplacement(filteredPartList, lastSuccessorPartNo);
                    grid.addObjectToGrid(part, partListEntry);
                } else {
                    grid.addObjectToGrid(part);
                }
            }
        }
        grid.showNoResultsLabel(grid.getTable().getRowCount() == 0);
    }

    private EtkDataPartListEntry getPartListEntryForPrimusReplacement(List<EtkDataPartListEntry> partList, String matNrOfPrimus) {
        for (EtkDataPartListEntry partListEntry : partList) {
            if (partListEntry.getPart().getAsId().getMatNr().equals(matNrOfPrimus)) {
                return partListEntry;
            }
        }
        // Falls Stücklisteneintrag nicht vorhanden einen Dummy erstellen
        PartListEntryId dummyPartListEntryId = new PartListEntryId();
        EtkDataPartListEntry resultPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(),
                                                                                                dummyPartListEntryId);
        resultPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        resultPartListEntry.setFieldValue(EtkDbConst.FIELD_K_MATNR, matNrOfPrimus, DBActionOrigin.FROM_DB);
        return resultPartListEntry;
    }

    private iPartsDataPrimusReplacePart createAndCopyFromCacheData(iPartsPRIMUSReplacementCacheObject
                                                                           primusReplacementCacheObject) {
        iPartsPrimusReplacePartId primusReplacePartId = new iPartsPrimusReplacePartId(primusReplacementCacheObject.getPredecessorPartNo());
        iPartsDataPrimusReplacePart primusReplacePart = new iPartsDataPrimusReplacePart(getProject(), primusReplacePartId);
        primusReplacePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);

        primusReplacePart.setFieldValue(FIELD_PRP_SUCCESSOR_PARTNO, primusReplacementCacheObject.getSuccessorPartNo(), DBActionOrigin.FROM_DB);
        primusReplacePart.setFieldValue(FIELD_PRP_PSS_CODE_BACK, primusReplacementCacheObject.getCodeBackward(), DBActionOrigin.FROM_DB);
        primusReplacePart.setFieldValue(FIELD_PRP_PSS_CODE_FORWARD, primusReplacementCacheObject.getCodeForward(), DBActionOrigin.FROM_DB);
        primusReplacePart.setFieldValue(FIELD_PRP_PSS_INFO_TYPE, primusReplacementCacheObject.getInfoType(), DBActionOrigin.FROM_DB);
        primusReplacePart.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DPRP_INLCUDE_PARTS_AVAILABLE,
                                                   SQLStringConvert.booleanToPPString(primusReplacementCacheObject.getIncludeParts() != null), DBActionOrigin.FROM_DB);
        return primusReplacePart;
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false);
        displayField.setColumnFilterEnabled(true);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_PSS_CODE_FORWARD, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRIMUS_REPLACE_PART, FIELD_PRP_PSS_CODE_BACK, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(TABLE_DA_PRIMUS_REPLACE_PART, iPartsDataVirtualFieldsDefinition.DPRP_INLCUDE_PARTS_AVAILABLE, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }


    /**
     * Erzeugt die darzustellenden Daten in Form einer {@link DBDataObjectList} bestehend aus {@link EtkDataObject}s.
     *
     * @return
     */
    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        return null;
    }

    private iPartsDataPrimusReplacePart getSelectedPrimusReplacePart() {
        List<EtkDataObject> etkDataObjectList = grid.getSelection();
        if (etkDataObjectList != null) {
            for (EtkDataObject etkDataObject : etkDataObjectList) {
                if (etkDataObject instanceof iPartsDataPrimusReplacePart) {
                    return (iPartsDataPrimusReplacePart)etkDataObject;
                }
            }
        }
        return null;
    }

}
