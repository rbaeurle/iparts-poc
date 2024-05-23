/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWWPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoWWPartsDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * Formular, um aus der Stückliste ein Wahlweise Set zusammenzustellen
 */
public class EditWWToPartlistEntryForm extends EditSelectPartlistEntryForm {

    public static Map<PartListEntryId, String> showWWToPartlistForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                    List<EtkDataPartListEntry> wwList, iPartsDataPartListEntry partListEntry) {
        EditWWToPartlistEntryForm dlg = new EditWWToPartlistEntryForm(dataConnector, parentForm, wwList, partListEntry);
        dlg.setTitle(iPartsRelatedInfoWWPartsDataForm.getPartListExtraText(dataConnector.getProject(), "!!Wahlweise Teile bearbeiten \"%1\"", partListEntry));
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getWWSetResultMap();
        }
        return null;
    }

    private String currentWWSetNo;
    private String initialWWSetNo;
    private Map<PartListEntryId, String> wwSetMap;

    /**
     * Erzeugt eine Instanz von EditWWToPartlistEntryForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    private EditWWToPartlistEntryForm(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                      List<EtkDataPartListEntry> wwList, iPartsDataPartListEntry partListEntry) {
        super(dataConnector, parentForm, partListEntry, wwList, null, null, true, false);
        setName("SelectPartlistForWWForm");
    }

    @Override
    protected void setupGrids() {
        selectedDataGrid = new EditDataObjectGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                refreshToolbar(this);
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doRemove(event);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {
                GuiMenuItem menuItem = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.IMG_DOWN, "!!Entfernen", getUITranslationHandler(), new de.docware.framework.modules.gui.event.EventListener(de.docware.framework.modules.gui.event.Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(de.docware.framework.modules.gui.event.Event event) {
                        doRemove(event);
                    }
                });
                contextMenu.addChild(menuItem);
            }

            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                // damit neu gewählte Wahlweise Teile die reichtige wwSetNo besitzen (ohne die partListEntries zu modifizieren)
                if (fieldName.equals(iPartsConst.FIELD_K_WW)) {
                    String value;
                    if (objectForTable != null) {
                        DBDataObjectAttribute attrib = new DBDataObjectAttribute(objectForTable.getAttributeForVisObject(fieldName));
                        attrib.setValueAsString(currentWWSetNo, DBActionOrigin.FROM_DB);
                        value = getVisObject().asHtml(tableName, fieldName, attrib,
                                                      getProject().getDBLanguage(), true).getStringResult();
                    } else {
                        value = currentWWSetNo;
                    }
                    return value;
                } else {
                    return super.getVisualValueOfField(tableName, fieldName, objectForTable);
                }
            }
        };

        actualDataGrid = new EditDataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void onTableSelectionChanged(Event event) {
                refreshToolbar(this);
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doAdd(event);
            }

            @Override
            protected void createContextMenuItems(GuiContextMenu contextMenu) {

                GuiMenuItem menuItem = getToolbarHelper().createContextMenuEntry(EditToolbarButtonAlias.IMG_UP, "!!Übernehmen", getUITranslationHandler(), new de.docware.framework.modules.gui.event.EventListener(de.docware.framework.modules.gui.event.Event.MENU_ITEM_EVENT) {
                    @Override
                    public void fire(de.docware.framework.modules.gui.event.Event event) {
                        doAdd(event);
                    }
                });
                contextMenu.addChild(menuItem);

            }

            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                // damit entfernte Wahlweise Teile keine wwSetNo besitzen (ohne die partListEntries zu modifizieren)
                if (fieldName.equals(iPartsConst.FIELD_K_WW)) {
                    if (objectForTable != null) {
                        if (objectForTable.getFieldValue(fieldName).equals(currentWWSetNo)) {
                            if (getSelectedPartListEntries().size() <= 1) {
                                return "";
                            }
                        }
                    }
                }
                return super.getVisualValueOfField(tableName, fieldName, objectForTable);
            }
        };
    }

    public Map<PartListEntryId, String> getWWSetResultMap() {
        wwSetMap.put(currentPartListEntry.getAsId(), currentWWSetNo);
        return wwSetMap;
    }

    @Override
    protected void initSelectionLists(List<EtkDataPartListEntry> initialSelectedPartlistEntries, boolean includeOriginalEntry) {
        super.initSelectionLists(initialSelectedPartlistEntries, includeOriginalEntry);
        currentWWSetNo = "";
        if (!initialSelection.isEmpty()) {
            currentWWSetNo = currentPartListEntry.getFieldValue(iPartsConst.FIELD_K_WW);
        }
        if (StrUtils.isEmpty(currentWWSetNo)) {
            Set<String> wwGUIDs = new HashSet<>();
            for (EtkDataPartListEntry partListEntry : allPartListEntries) {
                String wwSetNo = partListEntry.getFieldValue(iPartsConst.FIELD_K_WW);
                if (!wwSetNo.isEmpty()) {
                    wwGUIDs.add(wwSetNo);
                }
            }
            currentWWSetNo = iPartsWWPartsHelper.getNextUnusedWWGUID(wwGUIDs);
        }
        initialWWSetNo = currentWWSetNo;
    }

    @Override
    protected void modifySelection(List<List<EtkDataObject>> selectedList, boolean defaultSelectedFlag) {
        Set<String> wwSetNoSet = null;
        editedSelection = null;
        if (defaultSelectedFlag) {
            EtkDataPartListEntry partListEntry = findPartListEntry(currentPartListEntry);
            if (partListEntry != null) {
                wwSetNoSet = getSelectedWWSetNos(selectedList);
                if (!wwSetNoSet.isEmpty()) {
                    String wwSetNo = partListEntry.getFieldValue(iPartsConst.FIELD_K_WW);
                    if (StrUtils.isEmpty(wwSetNo)) {
                        currentWWSetNo = (String)wwSetNoSet.toArray()[0];
                    }
                    if (!wwSetNoSet.contains(currentWWSetNo)) {
                        if (MessageDialog.showYesNo("!!Sollen die Wahlweise-Sets verschmolzen werden?") != ModalResult.YES) {
                            return;
                        }
                    }
                }
            }
        }
        for (List<EtkDataObject> dataObjectList : selectedList) {
            for (EtkDataObject dataObject : dataObjectList) {
                EtkDataPartListEntry partListEntry = findPartListEntry((EtkDataPartListEntry)dataObject);
                if (partListEntry != null) {
                    if (partListEntry.getAsId().equals(currentPartListEntry.getAsId())) {
                        // angezeigtes PartListEntry kann nicht gelöscht werden
                        selectedIds.add(partListEntry.getAsId());
                    } else {
                        if (defaultSelectedFlag) {
                            selectedIds.add(partListEntry.getAsId());
                        } else {
                            selectedIds.remove(partListEntry.getAsId());
                        }
                        if (!defaultSelectedFlag) {
                            String wwSetNoToRemove = partListEntry.getFieldValue(iPartsConst.FIELD_K_WW);
                            if (StrUtils.isValid(wwSetNoToRemove) && !wwSetNoToRemove.equals(currentWWSetNo)) {
                                partListEntry.setFieldValue(iPartsConst.FIELD_K_WW, currentWWSetNo, DBActionOrigin.FROM_EDIT);
                            }
                        }
                    }
                }
            }
        }
        if (wwSetNoSet != null) {
            for (String wwSetNo : wwSetNoSet) {
                for (EtkDataPartListEntry partListEntry : allPartListEntries) {
                    if (!selectedIds.contains(partListEntry.getAsId())) {
                        if (partListEntry.getFieldValue(iPartsConst.FIELD_K_WW).equals(wwSetNo)) {
                            selectedIds.add(partListEntry.getAsId());
                        }
                    }
                }
            }
        } else if (!defaultSelectedFlag) {
            if (selectedDataGrid.getTable().getRowCount() == selectedList.size()) {
                // die letzten Einträge im selectedGrid werden gelöscht
                EtkDataPartListEntry partListEntry = findPartListEntry(currentPartListEntry);
                if (partListEntry != null) {
                    currentWWSetNo = initialWWSetNo;
                }
            }
        }
        dataToGrid();
    }

    private Set<String> getSelectedWWSetNos(List<List<EtkDataObject>> selectedList) {
        Set<String> wwSetNoSet = new TreeSet<String>();
        for (List<EtkDataObject> dataObjectList : selectedList) {
            for (EtkDataObject dataObject : dataObjectList) {
                EtkDataPartListEntry partListEntry = findPartListEntry((EtkDataPartListEntry)dataObject);
                if (partListEntry != null) {
                    if (!StrUtils.isEmpty(partListEntry.getFieldValue(iPartsConst.FIELD_K_WW))) {
                        wwSetNoSet.add(partListEntry.getFieldValue(iPartsConst.FIELD_K_WW));
                    }
                }
            }
        }
        return wwSetNoSet;
    }

    @Override
    protected boolean buildTransferData() {
        wwSetMap = new HashMap<>();
        for (EtkDataPartListEntry partListEntry : allPartListEntries) {
            if (selectedIds.contains(partListEntry.getAsId())) {
                wwSetMap.put(partListEntry.getAsId(), currentWWSetNo);
            } else {
                String wwSetNo = partListEntry.getFieldValue(iPartsConst.FIELD_K_WW);
                if (StrUtils.isValid(wwSetNo) && wwSetNo.equals(currentWWSetNo)) {
                    wwSetMap.put(partListEntry.getAsId(), "");
                }
            }
        }
        return true;
    }
}