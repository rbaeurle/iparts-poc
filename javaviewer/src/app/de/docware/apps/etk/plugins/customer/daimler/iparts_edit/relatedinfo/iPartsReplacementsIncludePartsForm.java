/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectFilterGrid;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.AbstractSimpleDataObjectGridForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsIncludePartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.collections.dwlist.DwList;

import java.util.ArrayList;
import java.util.List;

/**
 * Anzeige der Mitlieferteile zu einer Ersetzung
 */
public class iPartsReplacementsIncludePartsForm extends AbstractSimpleDataObjectGridForm implements iPartsConst {

    public static final String CONFIG_KEY_INCLUDE_PARTS = "Plugin/iPartsEdit/ReplaceIncludeParts";

    private final iPartsReplacement replacement;
    private final boolean isEditRelatedInfo;

    public static EtkDisplayFields modifyDisplayFields(EtkDisplayFields includePartDisplayFields) {
        EtkDisplayFields modifiedDisplayFields = new EtkDisplayFields();
        for (EtkDisplayField field : includePartDisplayFields.getFields()) {
            if (field.getKey().getTableName().equals(iPartsConst.TABLE_DA_INCLUDE_PART)) {
                EtkDisplayField modifiedDisplayField = iPartsIncludePartsHelper.convertToPartListDisplayField(field);
                if (modifiedDisplayField != null) {
                    modifiedDisplayFields.addFeld(modifiedDisplayField);
                }
            } else {
                modifiedDisplayFields.addFeld(field);
            }
        }
        return modifiedDisplayFields;
    }

    /**
     * Erzeugt einen neuen Dialog für die Anzeige der Mitlieferteile zu einer Ersetzung
     *
     * @param dataConnector
     * @param parentForm
     * @param relatedInfo
     */
    public iPartsReplacementsIncludePartsForm(RelatedInfoFormConnector dataConnector, AbstractJavaViewerForm parentForm,
                                              IEtkRelatedInfo relatedInfo, iPartsReplacement replacement, boolean forceNoEditAllowed) {
        super(dataConnector, parentForm, relatedInfo, CONFIG_KEY_INCLUDE_PARTS, "");
        this.replacement = replacement;

        if (!forceNoEditAllowed) {
            this.isEditRelatedInfo = replacement.isIncludePartsEditable() && (getConnector().getActiveForm() instanceof EditModuleForm)
                                     && AbstractRelatedInfoPartlistDataForm.isEditContext(getConnector(), true);
        } else {
            this.isEditRelatedInfo = false;
        }
        scaleFromParentForm(getWindow());
        grid.setDisplayFields(getDisplayFields(CONFIG_KEY_INCLUDE_PARTS));
        dataToGrid();
        doEnableButtons();
    }

    @Override
    protected List<EtkDisplayField> createDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<>();
        addDisplayField(defaultDisplayFields, TABLE_DA_INCLUDE_PART, FIELD_DIP_INCLUDE_MATNR, false, false, true);
        addDisplayField(defaultDisplayFields, TABLE_MAT, FIELD_M_TEXTNR, true, false, false);
        addDisplayField(defaultDisplayFields, TABLE_DA_INCLUDE_PART, FIELD_DIP_INCLUDE_QUANTITY, false, false, false);

        // virtuelle Felder:
        addDisplayField(defaultDisplayFields, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSE, false, false, false); // virtuelle DIALOG POSE
        addDisplayField(defaultDisplayFields, iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_AS_DIALOG_POSV, false, false, false); // virtuelle DIALOG POSV

        return defaultDisplayFields;
    }

    @Override
    protected DBDataObjectList<? extends EtkDataObject> createDataObjectList() {
        return null;
    }

    @Override
    protected boolean saveDataObjectsWithUpdate(EtkDataObjectList<? extends EtkDataObject> dataObjectList) {
        replacement.clearIncludeParts();
        return super.saveDataObjectsWithUpdate(dataObjectList);
    }

    @Override
    public void dataToGrid() {
        String dbLanguage = getProject().getDBLanguage();
        setWindowTitle(iPartsConst.RELATED_INFO_INCLUDE_PARTS_TEXT, replacement.getAsStringForTitle(dbLanguage));

        EtkDataAssembly assembly = getConnector().getRelatedInfoData().getAsPartListEntry(getProject()).getOwnerAssembly();
        List<EtkDataPartListEntry> filteredPartList = assembly.getPartList(assembly.getEbene());

        grid.clearGrid();

        // Nur bei editierbaren Mitlieferteilen diese direkt aus der DB laden über getIncludePartsAsDataIncludePartList(),
        // indem diese vorher aus der Ersetzung entfernt werden
        if (replacement.isIncludePartsEditable()) {
            replacement.clearIncludeParts();
        }

        iPartsDataIncludePartList includeParts = replacement.getIncludePartsAsDataIncludePartList(getProject(), true, DBActionOrigin.FROM_EDIT);
        for (iPartsDataIncludePart includePart : includeParts) {
            EtkDataPartListEntry partListEntryForIncludePart = iPartsIncludePartsHelper.getPartListEntryForIncludePart(replacement, includePart,
                                                                                                                       filteredPartList, FIELD_DIP_INCLUDE_MATNR);
            grid.addObjectToGrid(includePart, partListEntryForIncludePart);
        }

        grid.showNoResultsLabel(includeParts.isEmpty());
    }

    @Override
    protected DataObjectFilterGrid createGrid() {
        DataObjectFilterGrid dataGrid = new DataObjectFilterGrid(getConnector(), this) {
            @Override
            protected void createToolbarButtons(GuiToolbar toolbar) {
                ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
                holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(),
                                                                     new MenuRunnable() {
                                                                         @Override
                                                                         public void run(Event event) {
                                                                             doAdd();
                                                                         }
                                                                     });
                getContextMenu().addChild(holder.menuItem);
                holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(),
                                                                     new MenuRunnable() {
                                                                         @Override
                                                                         public void run(Event event) {
                                                                             doEdit();
                                                                         }
                                                                     });
                getContextMenu().addChild(holder.menuItem);
                holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(),
                                                                     new MenuRunnable() {
                                                                         @Override
                                                                         public void run(Event event) {
                                                                             doRemove();
                                                                         }
                                                                     });
                getContextMenu().addChild(holder.menuItem);
                holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_MAT, "!!Auswahl Material", getUITranslationHandler(),
                                                                     new MenuRunnable() {
                                                                         @Override
                                                                         public void run(Event event) {
                                                                             doAddMat();
                                                                         }
                                                                     });
                getContextMenu().addChild(holder.menuItem);
            }

            @Override
            protected void onTableSelectionChanged(Event event) {
                doEnableButtons();
            }

            @Override
            protected void onCellDblClicked(int row, int col, Event event) {
                doEdit();
            }
        };

        return dataGrid;
    }

    @Override
    public void setModifiedByEdit(boolean modifiedByEdit) {
        if (parentForm.getParentForm() instanceof RelatedInfoBaseForm) {
            ((RelatedInfoBaseForm)parentForm.getParentForm()).setModifiedByEdit(modifiedByEdit);
        } else {
            super.setModifiedByEdit(modifiedByEdit);
        }
    }

    private void doAdd() {
        if (!isEditRelatedInfo) {
            return;
        }

        EtkDataPartListEntry partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();
        List<EtkDataPartListEntry> partList = ownerAssembly.getPartList(ownerAssembly.getEbene());
        List<EtkDataPartListEntry> initialSelectedPartlistEntries = new DwList<>();
        iPartsDataIncludePartList dataIncludePartsBefore = replacement.getIncludePartsAsDataIncludePartList((getProject()));
        for (iPartsDataIncludePart includePart : dataIncludePartsBefore) {
            EtkDataPartListEntry partListEntryForIncludePart = iPartsIncludePartsHelper.getPartListEntryForIncludePart(replacement, includePart,
                                                                                                                       partList, FIELD_DIP_INCLUDE_MATNR);
            iPartsIncludePartsHelper.mergeValuesIntoPartListEntry(includePart, partListEntryForIncludePart);
            initialSelectedPartlistEntries.add(partListEntryForIncludePart);
        }

        iPartsDataReplacePart dataReplacement = replacement.getAsDataReplacePart(getProject());
        EtkDisplayFields displayFieldsTop = getDisplayFields(CONFIG_KEY_INCLUDE_PARTS);
        displayFieldsTop = modifyDisplayFields(displayFieldsTop);
        EditModuleFormIConnector editConnector = iPartsRelatedEditHelper.createEditConnector(getConnector(), partListEntry.getOwnerAssembly());
        iPartsDataIncludePartList updatedIncludePartList = EditSelectIncludePartsForm.showEditSelectIncludePartsForm(editConnector, this,
                                                                                                                     partListEntry, replacement,
                                                                                                                     initialSelectedPartlistEntries,
                                                                                                                     displayFieldsTop);
        if (editConnector != getConnector()) {
            editConnector.dispose();
        }
        if (updatedIncludePartList == null) {
            return;
        }

        iPartsDataIncludePartList includePartsToDelete = new iPartsDataIncludePartList();
        for (iPartsDataIncludePart dataIncludePartBefore : dataIncludePartsBefore) {
            if (!iPartsIncludePartsHelper.containsSameIncludePart(dataIncludePartBefore, updatedIncludePartList)) {
                includePartsToDelete.delete(dataIncludePartBefore, true, DBActionOrigin.FROM_EDIT);
            }
        }
        saveDataObjectsWithUpdate(includePartsToDelete);

        iPartsDataIncludePartList includePartsToSave = handleSelectedIncludeParts(updatedIncludePartList, dataIncludePartsBefore, dataReplacement);

        // Evtl. vorhandene Primärschlüssel-Reservierungen für die gelöschten Mitlieferteile ebenfalls löschen beim Speichern
        // der RelatedEdit
        if (!includePartsToDelete.getDeletedList().isEmpty()) {
            addSaveEditRunnable(() -> {
                for (iPartsDataIncludePart deletedIncludePart : includePartsToDelete.getDeletedList()) {
                    iPartsDataReservedPKList.deleteReservedPrimaryKey(getProject(), deletedIncludePart.getAsId());
                }
            });
        }

        for (iPartsDataIncludePart includePart : includePartsToSave) {
            addEditRunnablesForReservedPK(includePart.getAsId());
        }
    }

    private void doAddMat() {
        if (!isEditRelatedInfo) {
            return;
        }
        EtkDataPartListEntry partListEntry = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
        EtkDataAssembly ownerAssembly = partListEntry.getOwnerAssembly();

        EditModuleFormIConnector editConnector = iPartsRelatedEditHelper.createEditConnector(getConnector(), ownerAssembly);
        iPartsDataIncludePartList updatedIncludePartList = EditMaterialEditIncludePartForm.showEditMaterialEditIncludePartFormDirect(editConnector,
                                                                                                                                     this,
                                                                                                                                     replacement);
        if (editConnector != getConnector()) {
            editConnector.dispose();
        }
        if (updatedIncludePartList == null) {
            return;
        }

        iPartsDataIncludePartList dataIncludePartsBefore = replacement.getIncludePartsAsDataIncludePartList((getProject()));
        iPartsDataReplacePart dataReplacement = replacement.getAsDataReplacePart(getProject());
        iPartsDataIncludePartList includePartsToSave = handleSelectedIncludeParts(updatedIncludePartList, dataIncludePartsBefore, dataReplacement);
        for (iPartsDataIncludePart includePart : includePartsToSave) {
            addEditRunnablesForReservedPK(includePart.getAsId());
        }
    }

    /**
     * Verarbeitet die selektierten Mitlieferteile
     *
     * @param updatedIncludePartList
     * @param dataIncludePartsBefore
     * @param dataReplacement
     * @return
     */
    private iPartsDataIncludePartList handleSelectedIncludeParts(iPartsDataIncludePartList updatedIncludePartList,
                                                                 iPartsDataIncludePartList dataIncludePartsBefore,
                                                                 iPartsDataReplacePart dataReplacement) {
        iPartsDataIncludePartList includePartsToSave = new iPartsDataIncludePartList();
        for (iPartsDataIncludePart updatedIncludePart : updatedIncludePartList) {
            if (!iPartsIncludePartsHelper.containsSameIncludePart(updatedIncludePart, dataIncludePartsBefore)) {
                String seqNo = iPartsIncludePartsHelper.getNextIncludeSeqNo(getProject(), dataReplacement);
                updatedIncludePart.setFieldValue(FIELD_DIP_SEQNO, seqNo, DBActionOrigin.FROM_EDIT);
                updatedIncludePart.updateOldId(); // _DIP_SEQNO ist Teil vom Primärschlüssel!
                includePartsToSave.add(updatedIncludePart, DBActionOrigin.FROM_EDIT);
            }
        }
        saveDataObjectsWithUpdate(includePartsToSave);
        return includePartsToSave;
    }


    private void doEdit() {
        if (!isEditRelatedInfo) {
            return;
        }
        List<List<EtkDataObject>> selection = grid.getMultiSelection();
        if (selection.size() != 1) {
            return;
        }
        iPartsDataIncludePart selectedIncludePart = null;
        for (EtkDataObject selectedDataObject : selection.get(0)) {
            if (selectedDataObject instanceof iPartsDataIncludePart) {
                selectedIncludePart = (iPartsDataIncludePart)selectedDataObject;
            }
        }
        if (selectedIncludePart == null) {
            return;
        }

        EtkEditFields editFields = new EtkEditFields();
        editFields.addField(new EtkEditField(TABLE_DA_INCLUDE_PART, FIELD_DIP_INCLUDE_QUANTITY, false));
        EditUserControls eCtrl = new EditUserControls(getConnector(), this, TABLE_DA_INCLUDE_PART, selectedIncludePart.getAsId(),
                                                      selectedIncludePart.getAttributes(), editFields);
        String includePartNo = getVisObject().asString(TABLE_DA_INCLUDE_PART, FIELD_DIP_INCLUDE_MATNR,
                                                       selectedIncludePart.getFieldValue(FIELD_DIP_INCLUDE_MATNR),
                                                       getProject().getDBLanguage());
        eCtrl.setTitle(TranslationHandler.translate("!!Mitlieferteil %1", includePartNo));
        if (eCtrl.showModal() == ModalResult.OK) {
            String quantity = eCtrl.getAttributes().getFieldValue(FIELD_DIP_INCLUDE_QUANTITY);
            selectedIncludePart.setFieldValue(FIELD_DIP_INCLUDE_QUANTITY, quantity, DBActionOrigin.FROM_EDIT);
            iPartsDataIncludePartList saveList = new iPartsDataIncludePartList();
            saveList.add(selectedIncludePart, DBActionOrigin.FROM_EDIT);
            saveDataObjectsWithUpdate(saveList);
        }
    }

    private void doRemove() {
        if (!isEditRelatedInfo) {
            return;
        }
        iPartsDataIncludePartList deleteList = new iPartsDataIncludePartList();
        List<List<EtkDataObject>> selection = grid.getMultiSelection();
        for (List<EtkDataObject> selectedRow : selection) {
            for (EtkDataObject selectedDataObject : selectedRow) {
                if (selectedDataObject instanceof iPartsDataIncludePart) {
                    deleteList.delete((iPartsDataIncludePart)selectedDataObject, true, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        if (!deleteList.getDeletedList().isEmpty()) {
            saveDataObjectsWithUpdate(deleteList);

            // Evtl. vorhandene Primärschlüssel-Reservierungen für die gelöschten Mitlieferteile ebenfalls löschen beim
            // Speichern der RelatedEdit
            addSaveEditRunnable(() -> {
                for (iPartsDataIncludePart deletedIncludePart : deleteList.getDeletedList()) {
                    iPartsDataReservedPKList.deleteReservedPrimaryKey(getProject(), deletedIncludePart.getAsId());
                }
            });
        }
    }

    private void doEnableButtons() {
        if (isEditRelatedInfo) {
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW, true);
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, grid.isSingleSelected());
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE, grid.isSomethingSelected());
            grid.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAT, true);
        } else {
            grid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_NEW);
            grid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK);
            grid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_DELETE);
            grid.hideToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_MAT);
        }
    }

}