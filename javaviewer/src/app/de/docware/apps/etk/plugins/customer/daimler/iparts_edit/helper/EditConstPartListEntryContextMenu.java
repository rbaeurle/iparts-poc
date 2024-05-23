/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualCalcFieldDocuRel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.iPartsVirtualDocuRelStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditShowDocuRelevantCalculationForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditTransferPartlistEntriesWithPredictionForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserMultiChangeControlsForConstPartlistEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserPartListEntryControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;

/**
 * Funktionen zum Bearbeiten innerhalb der Konstruktions Stückliste, inkl. Anzeige der Doku Relevanz und Übernahme in AS
 */
public class EditConstPartListEntryContextMenu {

    private static final String MENUITEM_TRANSFER_SEPARATOR = "iPartsMenuItemSeparatorTransferToASPartList";
    private static final String MENUITEM_TRANSFER_TO_PARTLIST = "iPartsMenuItemTransferToASPartList";
    private static final String MENUITEM_TRANSFER_TO_SA = "iPartsMenuItemTransferToFreeSA";
    private static final String MENUITEM_CHANGE_SEPARATOR = "iPartsMenuItemSeparatorChangeValues";
    private static final String MENUITEM_CHANGE_EDIT = "iPartsMenuItemChangeEdit";
    private static final String MENUITEM_CHANGE_EDIT_UNIFY = "iPartsMenuItemChangeEditUnify";
    private static final String MENUITEM_SHOW_DOCU_RELEVANT_CALC = "iPartsMenuItemDocuRelevantCalc";
    private static final String MENUITEM_CREATE_SEPARATOR = "iPartsMenuItemSeparatorCreateConstPartListEntry";
    private static final String MENUITEM_WORK = "iPartsMenuItemWorkConstPartListEntry";
    private static final String MENUITEM_CREATE = "iPartsMenuItemCreateConstPartListEntry";
    private static final String MENUITEM_EDIT = "iPartsMenuItemEditConstPartListEntry";
    private static final String MENUITEM_DELETE = "iPartsMenuItemDeleteCreateConstPartListEntry";

    public static void modifyPartListPopupMenu(GuiContextMenu popupMenu, final AssemblyListFormIConnector connector) {
        // Separator vor Menüeintrag "Vereinheitlichen..." und "Ändern..."
        GuiSeparator menuItemSeparator = new GuiSeparator();
        menuItemSeparator.setUserObject(MENUITEM_CHANGE_SEPARATOR);
        popupMenu.addChild(menuItemSeparator);

        if (iPartsRight.EDIT_PARTS_DATA.checkRightInSession()) {
            // Menüeintrag "Vereinheitlichen..." hinzufügen
            GuiMenuItem menuItemChangeEditUnify = new GuiMenuItem();
            menuItemChangeEditUnify.setUserObject(MENUITEM_CHANGE_EDIT_UNIFY);
            menuItemChangeEditUnify.setName(MENUITEM_CHANGE_EDIT_UNIFY);
            menuItemChangeEditUnify.setText("!!Vereinheitlichen...");
            menuItemChangeEditUnify.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    if (!connector.getProject().getEtkDbs().isRevisionChangeSetActiveForEdit()) {
                        MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
                        return;
                    }

                    if (!iPartsEditPlugin.startEditing()) { // Paralleles Bearbeiten verhindern
                        return;
                    }
                    try {
                        doUnifyConstructionValuesForHmMSm(connector);
                    } finally {
                        iPartsEditPlugin.stopEditing();
                    }
                }
            });
            popupMenu.addChild(menuItemChangeEditUnify);

            // Menüeintrag "Ändern..." hinzufügen
            GuiMenuItem menuItemChangeEdit = new GuiMenuItem();
            menuItemChangeEdit.setUserObject(MENUITEM_CHANGE_EDIT);
            menuItemChangeEdit.setName(MENUITEM_CHANGE_EDIT);
            menuItemChangeEdit.setText("!!Ändern...");
            menuItemChangeEdit.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    if (!connector.getProject().getEtkDbs().isRevisionChangeSetActiveForEdit()) {
                        MessageDialog.showWarning("!!Für diese Edit-Aktion muss ein Autoren-Auftrag aktiv sein.");
                        return;
                    }

                    if (!iPartsEditPlugin.startEditing()) { // Paralleles Bearbeiten verhindern
                        return;
                    }
                    try {
                        doEditConstructionPartListEntryForHmMSm(connector);
                    } finally {
                        iPartsEditPlugin.stopEditing();
                    }
                }
            });
            popupMenu.addChild(menuItemChangeEdit);
        }

        GuiMenuItem menuItemShowDocuRelCalc = new GuiMenuItem();
        menuItemShowDocuRelCalc.setUserObject(MENUITEM_SHOW_DOCU_RELEVANT_CALC);
        menuItemShowDocuRelCalc.setName(MENUITEM_SHOW_DOCU_RELEVANT_CALC);
        menuItemShowDocuRelCalc.setText("!!Anzeige berechnete Doku-Relevanz...");
        menuItemShowDocuRelCalc.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doShowDocuRelCalc(connector);
            }
        });
        popupMenu.addChild(menuItemShowDocuRelCalc);

        if (iPartsRight.EDIT_PARTS_DATA.checkRightInSession()) {
            // Separator vor Menüeintrag "In AS-Stückliste übernehmen"
            menuItemSeparator = new GuiSeparator();
            menuItemSeparator.setUserObject(MENUITEM_TRANSFER_SEPARATOR);
            popupMenu.addChild(menuItemSeparator);

            // Menüeintrag "In AS-Stückliste übernehmen" hinzufügen
            GuiMenuItem menuItemTransferToASPartList = new GuiMenuItem();
            menuItemTransferToASPartList.setUserObject(MENUITEM_TRANSFER_TO_PARTLIST);
            menuItemTransferToASPartList.setName(MENUITEM_TRANSFER_TO_PARTLIST);
            menuItemTransferToASPartList.setText("!!In AS-Stückliste übernehmen...");
            menuItemTransferToASPartList.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    EditTransferPartlistEntriesWithPredictionForm.startTransferToASPartList(
                            connector, EditTransferPartlistEntriesWithPredictionForm.TransferMode.PARTLIST);
                }
            });
            popupMenu.addChild(menuItemTransferToASPartList);

            // Menüeintrag "In freie SA übernehmen" hinzufügen
            GuiMenuItem menuItemTransferToSA = new GuiMenuItem();
            menuItemTransferToSA.setUserObject(MENUITEM_TRANSFER_TO_SA);
            menuItemTransferToSA.setName(MENUITEM_TRANSFER_TO_SA);
            menuItemTransferToSA.setText("!!In freie SA übernehmen...");
            menuItemTransferToSA.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    EditTransferPartlistEntriesWithPredictionForm.startTransferToASPartList(
                            connector, EditTransferPartlistEntriesWithPredictionForm.TransferMode.SA);
                }
            });
            popupMenu.addChild(menuItemTransferToSA);

            // Separator vor Menüeintrag "Konstruktions-Stücklisteneintrag anlegen"
            menuItemSeparator = new GuiSeparator();
            menuItemSeparator.setUserObject(MENUITEM_CREATE_SEPARATOR);
            popupMenu.addChild(menuItemSeparator);

            // Menüeintrag "In AS-Stückliste übernehmen" hinzufügen
            GuiMenuItem menuItemCreateConstPartListEntry = new GuiMenuItem();
            menuItemCreateConstPartListEntry.setUserObject(MENUITEM_WORK);
            menuItemCreateConstPartListEntry.setName(MENUITEM_WORK);
            menuItemCreateConstPartListEntry.setText("!!Konstruktions-Stücklisteneintrag...");
            GuiMenuItem menuItemPartListEntryCreate = new GuiMenuItem();
            menuItemPartListEntryCreate.setUserObject(MENUITEM_CREATE);
            menuItemPartListEntryCreate.setName(MENUITEM_CREATE);
            menuItemPartListEntryCreate.setText("!!anlegen");
            menuItemPartListEntryCreate.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doCreateConstPartlistEntry(connector);
                }
            });
            menuItemCreateConstPartListEntry.addChild(menuItemPartListEntryCreate);
            GuiMenuItem menuItemPartListEntryEdit = new GuiMenuItem();
            menuItemPartListEntryEdit.setUserObject(MENUITEM_EDIT);
            menuItemPartListEntryEdit.setName(MENUITEM_EDIT);
            menuItemPartListEntryEdit.setText("!!bearbeiten");
            menuItemPartListEntryEdit.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doModifyConstPartlistEntry(connector);
                }
            });
            menuItemCreateConstPartListEntry.addChild(menuItemPartListEntryEdit);
            GuiMenuItem menuItemPartListEntryDelete = new GuiMenuItem();
            menuItemPartListEntryDelete.setUserObject(MENUITEM_DELETE);
            menuItemPartListEntryDelete.setName(MENUITEM_DELETE);
            menuItemPartListEntryDelete.setText("!!löschen");
            menuItemPartListEntryDelete.addEventListener(new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doDeleteConstPartlistEntry(connector);
                }
            });
            menuItemCreateConstPartListEntry.addChild(menuItemPartListEntryDelete);
            popupMenu.addChild(menuItemCreateConstPartListEntry);
        }
    }

    public static void updatePartListPopupMenu(GuiContextMenu popupMenu, AssemblyListFormIConnector connector) {
        // Ist die aktuelle Stückliste eine DIALOG HM/M/SM- oder EDS/BCS Struktur- oder eine SAP.MBS-Stückliste?
        boolean isDialogHmMSmAssembly = false;
        boolean isEdsStructureSaaAssembly = false;
        boolean isEdsStructureAssembly = false;
        boolean isCTTSaaAssembly = false;
        boolean isMBSAssembly = false;
        boolean isMBSBasePartlist = false;
        EtkDataAssembly assembly = connector.getCurrentAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            List<iPartsVirtualNode> virtualNodesPath = ((iPartsDataAssembly)assembly).getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsVirtualNode.isHmMSmNode(virtualNodesPath)) {
                    iPartsVirtualNode hmMSmNode = virtualNodesPath.get(1);
                    if ((hmMSmNode.getType() == iPartsNodeType.HMMSM) && hmMSmNode.getId().isValidId()) {
                        isDialogHmMSmAssembly = true;
                    }
                } else if (iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)) {
                    iPartsVirtualNode saaNode = virtualNodesPath.get(2);
                    if ((saaNode.getType() == iPartsNodeType.EDS_SAA) && (saaNode.getId()).isValidId()) {
                        // bei EDS können auch A-SNR direkt unter dem Baumuster hängen. Hier darf keine Übernahme in
                        // freie SAs möglich sein
                        isEdsStructureSaaAssembly = isValidSaaPartsListNode(saaNode);
                        isEdsStructureAssembly = true;
                    }
                } else if (iPartsVirtualNode.isCTTSaaConstNode(virtualNodesPath)) {
                    iPartsVirtualNode saaNode = virtualNodesPath.get(1);
                    if ((saaNode.getType() == iPartsNodeType.EDS_SAA) && (saaNode.getId()).isValidId()) {
                        // bei CTT können auch A-SNR direkt unter dem Baumuster hängen. Hier darf keine Übernahme in
                        // freie SAs möglich sein
                        isCTTSaaAssembly = isValidSaaPartsListNode(saaNode);
                    }
                } else if (iPartsVirtualNode.isMBSNode(virtualNodesPath)) {
                    iPartsVirtualNode structureNode = virtualNodesPath.get(1);
                    if ((structureNode.getType() == iPartsNodeType.MBS) && structureNode.getId().isValidId()) {
                        if (structureNode.getId() instanceof MBSStructureId) {
                            MBSStructureId structureId = (MBSStructureId)(structureNode.getId());
                            isMBSBasePartlist = structureId.isBasePartlistId();
                        }
                        isMBSAssembly = true;
                    }
                }
            }
        }

        boolean isTransferToPartlistEnabled = isDialogHmMSmAssembly || isEdsStructureAssembly || isMBSAssembly;
        boolean isTransferToFreeSaEnabled = isEdsStructureSaaAssembly || isCTTSaaAssembly || (isMBSAssembly && !isMBSBasePartlist);

        // Separator und Menüeintrag "In AS-Stückliste übernehmen", sowie "Vereinheitlichen..." und "Ändern..." aktualisieren
        List<EtkDataPartListEntry> selectedPartlistEntries = connector.getSelectedPartListEntries();
        boolean isSomethingSelected = (selectedPartlistEntries != null) && !selectedPartlistEntries.isEmpty();
        boolean isSomethingSelectedAdjusted = false;
        boolean isMultiSelect = false;
        boolean isChangeSetForEditActive = false;
        if (isDialogHmMSmAssembly) { // Nur bei HM/M/SM auswerten
            isChangeSetForEditActive = connector.getProject().getDbLayer().isRevisionChangeSetActiveForEdit();
            if (isChangeSetForEditActive) {
                isChangeSetForEditActive = isUnifyStatePossible(selectedPartlistEntries);
            }
        }

        // Vereinheitlichen und ändern der Doku-Relevanz ist nicht möglich, wenn es sich nur um eine Stücklistenposition
        // mit dem Doku-Relevanz Wert "dokumentiert" handelt.
        boolean isSingleDocumentedEntry = isDialogHmMSmAssembly && isSingleDocumentedEntry(selectedPartlistEntries);
        if (isSomethingSelected && (isTransferToPartlistEnabled || isTransferToFreeSaEnabled)) {
            isMultiSelect = connector.getSelectedPartListEntries().size() > 1;
            List<EtkDataPartListEntry> adjustedList = getAdjustedSelectedPartListEntries(connector);
            isSomethingSelectedAdjusted = !adjustedList.isEmpty();
        }
        boolean isChangeSetActive = connector.getProject().getEtkDbs().isRevisionChangeSetActiveForEdit();
        for (AbstractGuiControl child : popupMenu.getChildren()) {
            if (child.getUserObject() != null) {
                if (child.getUserObject().equals(MENUITEM_TRANSFER_SEPARATOR)) {
                    child.setVisible((isTransferToPartlistEnabled || isTransferToFreeSaEnabled) && isChangeSetActive);
                } else if (child.getUserObject().equals(MENUITEM_CHANGE_SEPARATOR)) {
                    child.setVisible(isDialogHmMSmAssembly && isChangeSetActive);
                } else if (child.getUserObject().equals(MENUITEM_TRANSFER_TO_PARTLIST)) {
                    child.setVisible(isTransferToPartlistEnabled && isChangeSetActive);
                    child.setEnabled(isSomethingSelectedAdjusted);
                } else if (child.getUserObject().equals(MENUITEM_TRANSFER_TO_SA)) {
                    child.setVisible(isTransferToFreeSaEnabled && isChangeSetActive);
                    child.setEnabled(isSomethingSelectedAdjusted);
                } else if (child.getUserObject().equals(MENUITEM_CHANGE_EDIT_UNIFY)) {
                    child.setVisible(isDialogHmMSmAssembly && isChangeSetActive);
                    child.setEnabled(isSomethingSelectedAdjusted && (!isSingleDocumentedEntry || isChangeSetForEditActive));
                } else if (child.getUserObject().equals(MENUITEM_CHANGE_EDIT)) {
                    child.setVisible(isDialogHmMSmAssembly && isChangeSetActive);
                    child.setEnabled(isSomethingSelectedAdjusted && !isMultiSelect && (!isSingleDocumentedEntry || isChangeSetForEditActive));
                } else if (child.getUserObject().equals(MENUITEM_SHOW_DOCU_RELEVANT_CALC)) {
                    child.setVisible(isDialogHmMSmAssembly);
                    child.setEnabled(isSomethingSelectedAdjusted && !isMultiSelect);
                } else if (child.getUserObject().equals(MENUITEM_CREATE_SEPARATOR)) {
                    child.setVisible(isDialogHmMSmAssembly && isChangeSetActive);
                } else if (child.getUserObject().equals(MENUITEM_WORK)) {
                    child.setVisible(isDialogHmMSmAssembly && isChangeSetActive);
                    if (child.isVisible()) {
                        for (AbstractGuiControl subChild : child.getChildren()) {
                            if (subChild.getUserObject() != null) {
                                if (subChild.getUserObject().equals(MENUITEM_CREATE)) {
                                    subChild.setEnabled(isSomethingSelectedAdjusted && !isMultiSelect);
                                } else if (subChild.getUserObject().equals(MENUITEM_EDIT)) {
                                    GuiMenuItem menuItemPartListEntryEdit = null;
                                    if (subChild instanceof GuiMenuItem) {
                                        menuItemPartListEntryEdit = (GuiMenuItem)subChild;
                                        menuItemPartListEntryEdit.setText("!!bearbeiten");
                                    }
                                    boolean isEnabled = isSomethingSelectedAdjusted && !isMultiSelect;
                                    if (isEnabled) {
                                        isEnabled = isIPartsCreatedPartListEntry(selectedPartlistEntries, true);
                                        if (isEnabled) {
                                            boolean isUsedinAS = selectedPartlistEntries.get(0).getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE).equals(iPartsConst.RETAIL_ASSIGNED);
                                            if (isUsedinAS && (menuItemPartListEntryEdit != null)) {
                                                menuItemPartListEntryEdit.setText("!!anzeigen");
                                            }
                                        }
                                    }
                                    subChild.setEnabled(isEnabled);
                                } else if (subChild.getUserObject().equals(MENUITEM_DELETE)) {
                                    boolean isEnabled = isSomethingSelectedAdjusted;
                                    if (isEnabled) {
                                        isEnabled = isIPartsCreatedPartListEntry(selectedPartlistEntries, false);
                                    }
                                    subChild.setEnabled(isEnabled);
                                }
                            }
                        }
                    }
                } else if (child.getUserObject().equals("menuitemFocus")) {
                    if (isTransferToPartlistEnabled || isTransferToFreeSaEnabled) {
                        child.setVisible(false);
                    } else {
                        child.setVisible(!connector.isSelectionOnly());
                    }
                }
            }
        }
    }

    private static boolean isValidSaaPartsListNode(iPartsVirtualNode saaNode) {
        if (saaNode.getId() instanceof EdsSaaId) {
            EdsSaaId node = (EdsSaaId)(saaNode.getId());
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            if (numberHelper.isValidSaa(node.getSaaNumber())) {
                return true;
            }
        }
        return false;
    }

    private static void doCreateConstPartlistEntry(AssemblyListFormIConnector connector) {
        if (!iPartsEditPlugin.startEditing()) { // Paralleles Bearbeiten verhindern
            return;
        }

        try {
            EditConstPartListEntryHelper helper = new EditConstPartListEntryHelper(connector);
            helper.createConstPartListEntry();
        } finally {
            iPartsEditPlugin.stopEditing();
        }
    }

    private static void doModifyConstPartlistEntry(AssemblyListFormIConnector connector) {
        if (!iPartsEditPlugin.startEditing()) { // Paralleles Bearbeiten verhindern
            return;
        }

        try {
            EditConstPartListEntryHelper helper = new EditConstPartListEntryHelper(connector);
            helper.editConstPartListEntry();
        } finally {
            iPartsEditPlugin.stopEditing();
        }
    }

    private static void doDeleteConstPartlistEntry(AssemblyListFormIConnector connector) {
        if (!iPartsEditPlugin.startEditing()) { // Paralleles Bearbeiten verhindern
            return;
        }

        try {
            EditConstPartListEntryHelper helper = new EditConstPartListEntryHelper(connector);
            helper.deleteConstPartListEntry();
        } finally {
            iPartsEditPlugin.stopEditing();
        }
    }

    private static void doUnifyConstructionValuesForHmMSm(AssemblyListFormIConnector connector) {
        List<EtkDataPartListEntry> allSelectedPartListEntries = getAdjustedSelectedPartListEntries(connector);
        List<EtkDataPartListEntry> validSelectedPartListEntries = new DwList<>();
        // In AS übernommene Stücklistenpositionen dürfen nicht vereinheitlicht werden
        for (EtkDataPartListEntry possibleSelectedPartListEntry : allSelectedPartListEntries) {
            List<String> docuValues = possibleSelectedPartListEntry.getFieldValueAsSetOfEnum(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT);
            iPartsDocuRelevant docuRelevant = iPartsDocuRelevant.getFromDBValue(docuValues.isEmpty() ? "" : docuValues.get(0));
            if (!iPartsDocuRelevant.isDocumented(docuRelevant)) {
                validSelectedPartListEntries.add(possibleSelectedPartListEntry);
            }
        }
        EtkEditFields editFields = new EtkEditFields();
        if (!validSelectedPartListEntries.isEmpty()) {
            boolean isSingleDocumentedEntry = isSingleDocumentedEntry(validSelectedPartListEntries);
            if (!isSingleDocumentedEntry) {
                EtkEditField editField = EtkEditFieldHelper.getEditFieldForVirtualField(connector, iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT,
                                                                                        "!!Doku-relevant");
                editFields.addField(editField);
            }
        }
        if (editFields.size() == 0) {
            MessageDialog.show("!!Es gibt keine Daten, die vereinheitlicht werden können.", "!!Vereinheitlichen");
            return;
        }

        // Anzeige des Unify-Dialogs
        DBDataObjectAttributes attributes =
                EditUserMultiChangeControlsForConstPartlistEntry.showEditUserMultiChangeControlsForConstPartListEntries(connector, editFields,
                                                                                                                        validSelectedPartListEntries);
        if (attributes != null) {
            boolean doUpdate = false;
            for (EtkEditField editField : editFields.getFields()) {
                String editFieldName = editField.getKey().getFieldName();
                if (editFieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT)) {
                    DBDataObjectAttribute attribute = attributes.getField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT, false);
                    doUpdate |= doUpdateDocuRel(connector, validSelectedPartListEntries, attribute);

                }
            }
            if (doUpdate) {
                // Assembly aus dem Cache für das aktive ChangeSet und in allen Cluster-Knoten löschen, da modifiziert
                EtkDataAssembly.removeDataAssemblyFromCache(connector.getProject(), connector.getCurrentAssembly().getAsId());
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                          iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                          connector.getCurrentAssembly().getAsId(), false));
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
            }
        }
    }

    private static void doEditConstructionPartListEntryForHmMSm(AssemblyListFormIConnector connector) {
        if (connector.getSelectedPartListEntries().size() == 1) {
            EtkDataPartListEntry partListEntry = connector.getSelectedPartListEntries().get(0);
            EtkEditFields editFields = new EtkEditFields();

            // Aus den DisplayFields EditFields zusammenbauen
            List<EtkDisplayField> displayFields = connector.getCurrentAssembly().getEbene().getFields();
            String language = connector.getProject().getViewerLanguage();
            for (EtkDisplayField displayField : displayFields) {
                if (displayField.isVisible()) {
                    String fieldName = displayField.getKey().getFieldName();
                    EtkEditField editField = new EtkEditField(displayField.getKey().getTableName(), fieldName,
                                                              displayField.isMultiLanguage());
                    editField.setArray(displayField.isArray());
                    EtkEditFieldHelper.setLabelTextForVirtField(fieldName, editField, language, connector.getCurrentAssembly().getEbeneName(),
                                                                displayField);
                    editField.setEditierbar(false);
                    editFields.addField(editField);
                }
            }

            EtkEditField editField = editFields.getFeldByName(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT);
            if (editField == null) {
                // Doku-Relevant wird nicht angezeigt => editField zusammenbauen
                editField = new EtkEditField(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT, false);
                // Text nicht in DisplayFields enthalten => selber setzen
                EtkMultiSprache multi = new EtkMultiSprache();
                multi.setText(language, TranslationHandler.translate("!!Doku-relevant"));
                editField.setText(multi);
                editField.setDefaultText(false);
            } else {
                // Doku-relevant wird angezeigt => Feld nach oben ziehen
                int index = editFields.getIndexOfFeld(editField);
                editFields.deleteFeld(index);
            }
            editField.setEditierbar(true);
            editField.setMussFeld(true);
            editFields.addFeld(0, editField);

            String[] exceptedVirtualFields = new String[]{ TableAndFieldName.make(iPartsConst.TABLE_KATALOG,
                                                                                  iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT) };
            EtkDataPartListEntry modifiedPartListEntry =
                    EditUserPartListEntryControls.doShowDIALOGPartListEntry(connector, connector.getActiveForm(), editFields,
                                                                            exceptedVirtualFields, partListEntry.cloneMe(connector.getProject()));
            if (modifiedPartListEntry != null) {
                // Daten wurden verändert => neuen Wert an PartListEntry übergeben und in Tabelle DA_DIALOG speichern
                EtkDataObject dataObject = modifiedPartListEntry.getDataObjectByTableName(iPartsConst.TABLE_KATALOG, false);
                if (dataObject != null) {
                    DBDataObjectAttribute attribute = dataObject.getAttribute(iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT, false);
                    List<EtkDataPartListEntry> validSelectedPartListEntries = new DwList<>();
                    validSelectedPartListEntries.add(partListEntry);
                    if (doUpdateDocuRel(connector, validSelectedPartListEntries, attribute)) {
                        // Assembly aus dem Cache für das aktive ChangeSet und in allen Cluster-Knoten löschen, da modifiziert
                        EtkDataAssembly.removeDataAssemblyFromCache(connector.getProject(), connector.getCurrentAssembly().getAsId());
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                  connector.getCurrentAssembly().getAsId(), false));
                        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent());
                    }
                }
            }
        }
    }

    private static void doShowDocuRelCalc(AssemblyListFormIConnector connector) {
        if (connector.getSelectedPartListEntries().size() == 1) {
            EtkDataPartListEntry partListEntry = connector.getSelectedPartListEntries().get(0);
            iPartsVirtualCalcFieldDocuRel calcField = new iPartsVirtualCalcFieldDocuRel(connector.getProject(), partListEntry);
            List<iPartsVirtualDocuRelStates.DocuRelFilterElement> docuRelList = new DwList<>();
            calcField.calculateDocuRelevant(partListEntry, docuRelList);
            String title = TranslationHandler.translate("!!Berechnete Doku-Relevanz: \"%1\"", calcField.getVisGuid(partListEntry.getAsId().getKLfdnr()));
            EditShowDocuRelevantCalculationForm.showDocuRelResults(connector, null, docuRelList, title);
        }
    }

    /**
     * entfernt die Stücklistentexte aus den selektierten PartListEntries
     *
     * @param connector
     * @return
     */
    private static List<EtkDataPartListEntry> getAdjustedSelectedPartListEntries(AssemblyListFormIConnector connector) {
        List<EtkDataPartListEntry> result = new DwList<>();
        List<EtkDataPartListEntry> selectedPartListEntries = connector.getSelectedPartListEntries();
        if ((selectedPartListEntries != null) && !selectedPartListEntries.isEmpty()) {
            for (EtkDataPartListEntry partListEntry : selectedPartListEntries) {
                if (StrUtils.isValid(partListEntry.getFieldValue(EtkDbConst.FIELD_K_MATNR))) {
                    result.add(partListEntry);
                }
            }

        }
        return result;
    }

    /**
     * Check, ob die Liste nur einen Stücklisteneinträg enthält, der übernommen wurde
     *
     * @param selectedPartlistEntries
     * @return
     */
    private static boolean isSingleDocumentedEntry(List<EtkDataPartListEntry> selectedPartlistEntries) {
        if ((selectedPartlistEntries != null) && (selectedPartlistEntries.size() == 1)) {
            EtkDataPartListEntry singleEntry = selectedPartlistEntries.get(0);
            List<String> docuValues = singleEntry.getFieldValueAsSetOfEnum(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT);
            iPartsDocuRelevant docuRelevant = iPartsDocuRelevant.getFromDBValue(docuValues.isEmpty() ? "" : docuValues.get(0));
            return iPartsDocuRelevant.isDocumented(docuRelevant);

        }
        return false;
    }

    private static boolean isIPartsCreatedPartListEntry(EtkDataPartListEntry partListEntry) {
        iPartsDialogBCTEPrimaryKey dialogBCTEPrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
        return (dialogBCTEPrimaryKey != null) && dialogBCTEPrimaryKey.isIPartsCreatedBCTEKey();
    }

    /**
     * Prüft ob die übergebenen Stücklisteneinträge in iParts erzeugt wurden.
     *
     * @param selectedPartlistEntries
     * @param checkSingle             Wenn {@code checkSingle == true} wird außerdem geprüft ob {@code selectedPartListEntries}
     *                                nur aus einem Element besteht.
     */
    private static boolean isIPartsCreatedPartListEntry(List<EtkDataPartListEntry> selectedPartlistEntries, boolean checkSingle) {
        if ((selectedPartlistEntries != null) && !selectedPartlistEntries.isEmpty()) {
            if (selectedPartlistEntries.size() > 1) {
                if (!checkSingle) {
                    for (EtkDataPartListEntry partListEntry : selectedPartlistEntries) {
                        if (isIPartsCreatedPartListEntry(partListEntry)) {
                            return true;
                        }
                    }
                }
            } else {
                EtkDataPartListEntry partListEntry = selectedPartlistEntries.get(0);
                if (isIPartsCreatedPartListEntry(partListEntry)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isUnifyStatePossible(List<EtkDataPartListEntry> selectedPartlistEntries) {
        if ((selectedPartlistEntries != null) && !selectedPartlistEntries.isEmpty()) {
            for (EtkDataPartListEntry partListEntry : selectedPartlistEntries) {
                if (StrUtils.isValid(partListEntry.getFieldValue(EtkDbConst.FIELD_K_MATNR))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean doUpdateDocuRel(final AssemblyListFormIConnector connector, List<EtkDataPartListEntry> validSelectedPartListEntries,
                                           DBDataObjectAttribute attribute) {
        EtkRevisionsHelper revisionsHelper = connector.getProject().getRevisionsHelper();
        if (revisionsHelper == null) {
            return false;
        }
        AbstractRevisionChangeSet activeChangeSet = revisionsHelper.getActiveRevisionChangeSetForEdit();
        if (activeChangeSet == null) {
            return false;
        }

        // geänderte Werte an alle selektierten PartListEntries übergeben und in Tabelle DA_DIALOG speichern
        GenericEtkDataObjectList genericList = new GenericEtkDataObjectList();
        final iPartsDataDialogDataList dataDialogDataList = new iPartsDataDialogDataList();
        VirtualFieldDefinition fieldDefinition = iPartsDataVirtualFieldsDefinition.findField(iPartsConst.TABLE_KATALOG,
                                                                                             iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT);
        if (attribute != null) {
            for (EtkDataPartListEntry partListEntry : validSelectedPartListEntries) {
                iPartsDataDialogData importData = setDocuRelevant(connector, partListEntry, fieldDefinition,
                                                                  iPartsDocuRelevant.getFromDBValue(attribute.getAsString()));
                if (importData != null) {
                    dataDialogDataList.add(importData, DBActionOrigin.FROM_EDIT);
                    genericList.add(importData, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        if (!dataDialogDataList.isEmpty()) {
            final EtkProject project = connector.getProject();

            final EtkDbObjectsLayer dbLayer = project.getDbLayer();

            // Speichern im ChangeSet muss auch innerhalb der Transaktion stattfinden
            dbLayer.startTransaction();
            dbLayer.startBatchStatement();
            activeChangeSet.addDataObjectListCommitted(genericList);

            // Doku-relevant direkt in der Tabelle DA_DIALOG ohne aktive ChangeSets speichern
            project.executeWithoutActiveChangeSets(new Runnable() {
                @Override
                public void run() {
                    try {
                        dataDialogDataList.saveToDB(project);
                        dbLayer.endBatchStatement();
                        dbLayer.commit();

                        // Modul aus dem normalen Cache ohne aktive ChangeSets entfernen
                        EtkDataAssembly.removeDataAssemblyFromCache(project, connector.getCurrentAssembly().getAsId());
                    } catch (Exception e) {
                        dbLayer.cancelBatchStatement();
                        dbLayer.rollback();
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                        MessageDialog.showError("!!Fehler beim Speichern.");
                    }
                }
            }, false); // fireDataChangedEvent ist hier nicht notwendig, weil in den aufrufenden Methoden ein DataChangedEvent gefeuert wird
            return true;
        }
        return false;
    }

    private static iPartsDataDialogData setDocuRelevant(AssemblyListFormIConnector connector, EtkDataPartListEntry partListEntry,
                                                        VirtualFieldDefinition fieldDefinition, iPartsDocuRelevant value) {
        iPartsDataDialogData dialogData = getDialogDataFromPartListEntry(connector, partListEntry);
        if ((dialogData != null) && (fieldDefinition != null)) {
            EtkDataObject dataObject = partListEntry.getDataObjectByTableName(fieldDefinition.getDestinationTable(), false);
            if (dataObject != null) {
                dialogData.setFieldValue(fieldDefinition.getSourceFieldName(), value.getDbValue(), DBActionOrigin.FROM_EDIT);
                // da connector.setPartListEntriesModified(); die Werte der virtuellen Felder NICHT nachlädt
                partListEntry.setFieldValue(fieldDefinition.getVirtualFieldName(), value.getDbValue(), DBActionOrigin.FROM_EDIT);

                // Die Doku-Relevanz einfach aus den Attributes rauslöschen, damit sie automatisch neu berechnet wird
                if (partListEntry.getAttributes().fieldExists(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT)) {
                    partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT,
                                                              DBActionOrigin.FROM_DB);
                }

                // Den Geschäftsfall einfach aus den Attributes rauslöschen, damit er automatisch neu berechnet wird
                if (partListEntry.getAttributes().fieldExists(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_BUSINESS_CASE)) {
                    partListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_BUSINESS_CASE,
                                                              DBActionOrigin.FROM_DB);
                }
                return dialogData;
            }
        }
        return null;
    }

    private static iPartsDataDialogData getDialogDataFromPartListEntry(AssemblyListFormIConnector connector, EtkDataPartListEntry partListEntry) {
        iPartsDialogBCTEPrimaryKey dialogBCTEPrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
        if (dialogBCTEPrimaryKey == null) {
            return null;
        }
        iPartsDialogId id = new iPartsDialogId(dialogBCTEPrimaryKey.createDialogGUID());
        iPartsDataDialogData dialogData = new iPartsDataDialogData(connector.getProject(), id);
        if (dialogData.existsInDB()) {
            return dialogData;
        }
        return null;
    }
}
