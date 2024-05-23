/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteMatRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteMatRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsPartFootnotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditMatrixUserControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleFormConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsFootnoteEditInlineDialog;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiScrollPane;
import de.docware.framework.modules.gui.controls.GuiSplitPane;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.Utils;

/**
 * Form für die Related Info zum Bearbeiten eines Materials in iParts.
 */
public class iPartsRelatedInfoEditMaterialDataForm extends AbstractRelatedInfoEditDataForm {

    private static final int EDIT_CONTROLS_WIDTH = 600;

    private PartId loadedPartId;
    private EtkDataPartListEntry partListEntryForEdit;
    private PartId partIdForEdit;
    private EditUserControlContainer editControls;
    private AbstractGuiControl editControlsGui;
    private iPartsFootnoteEditInlineDialog footnoteEditor; // Editor für Fußnoten
    private iPartsRelatedInfoInlineEditFootnoteDataForm footnoteForm;

    public iPartsRelatedInfoEditMaterialDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
        footnoteForm = null;
    }

    @Override
    public void dispose() {
        if (footnoteForm != null) {
            footnoteForm.dispose();
        }
        if (footnoteEditor != null) {
            footnoteEditor.dispose();
        }
        super.dispose();
    }

    private void resetEditControls() {
        if (editControlsGui != null) {
            editPanel.removeChild(editControlsGui);
            editControlsGui = null;
        }
        editControls = null;
        partIdForEdit = null;
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        PartId currentPartId = getConnector().getRelatedInfoData().getAsPartListEntry(getProject()).getPart().getAsId();
        if ((getConnector().getActiveRelatedSubForm() == this) && !Utils.objectEquals(currentPartId, loadedPartId)) {
            // Beim ersten Aufruf von updateData() mit loadedPartId == null die EditControls nicht zurücksetzen
            if (loadedPartId != null) {
                resetEditControls();
            }
            loadedPartId = currentPartId;
            if (footnoteForm != null) {
                footnoteForm.updateData(this, true);
            }
        }
    }

    @Override
    public void showRelatedInfo() {
        // EditControls für die Bearbeitung vom Material erzeugen und zum editPanel hinzufügen
        if (editControls == null) {
            partListEntryForEdit = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
            if (partListEntryForEdit.isAssembly()) { // nur echte Materialien sollen bearbeitet werden können, keine Module
                return;
            }

            if (editModuleFormConnector == null) { // dürfte eigentlich nicht passieren
                editModuleFormConnector = new EditModuleFormConnector(getConnector());
                addOwnConnector(editModuleFormConnector);
                editModuleFormConnector.setCurrentAssembly(partListEntryForEdit.getOwnerAssembly());
            }

            editControls = new EditUserControlContainer(editModuleFormConnector, this, EtkDbConst.TABLE_MAT, partListEntryForEdit);
            editControlsGui = editControls.getGui();
            editControls.setReadOnly(!getConnector().isEditContext() || !iPartsEditPlugin.isMaterialEditable());

            GuiSplitPane splitPaneMaster = new GuiSplitPane();
            splitPaneMaster.setName("splitpaneMaterialData");
            splitPaneMaster.setHorizontal(false);
            splitPaneMaster.setDividerSize(6);
            // Editor für fußnoten
            AbstractGuiControl footnoteControl = getFootnoteEditorAndPreviewFromRelInfo();

            if (!editControls.isMatrix()) {
                // 250 Pixel Abzug für Related Info Buttons und Rahmen
                editControls.setEditControlsWidth(Math.min(EDIT_CONTROLS_WIDTH, FrameworkUtils.getScreenSize().width - 250));
                splitPaneMaster.setConstraints(new ConstraintsBorder());
                editControlsGui.removeFromParent();
                editControlsGui.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                editPanel.removeAllChildren(); // FillerPanel raus
                editPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
            } else {
                AbstractConstraints panelGridConstraints = editControlsGui.getConstraints();
                splitPaneMaster.setConstraints(panelGridConstraints);
                editPanel.removeAllChildren(); // FillerPanel raus
                editPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
            }

            splitPaneMaster.setFirstChild(editControlsGui);

            GuiPanel panel = new GuiPanel(); // Panel für die Fußnotenverarbeitung
            panel.setLayout(new LayoutBorder());
            panel.addChildBorderCenter(footnoteControl);
            GuiScrollPane scrollPane = new GuiScrollPane();
            scrollPane.addChild(panel);

            int splitPaneDividerPos = Math.min(editControls.getEditWindowHeight(),
                                               (parentForm.getRootParentWindow().getHeight()) * 2 / 3);
            splitPaneMaster.setDividerPosition(splitPaneDividerPos);
            splitPaneMaster.setSecondChild(scrollPane);
            footnoteForm.setPreviewDividerPosition((parentForm.getRootParentWindow().getHeight() - splitPaneDividerPos) * 2 / 3);

            editPanel.addChild(splitPaneMaster);
        }
    }

    /**
     * Liefert die GUI für die Bearbeitung von Fußnoten wie bei den Related-Infos. Diese beinhaltet den Editor und die
     * Preview-Anzeige.
     *
     * @return
     */
    private AbstractGuiControl getFootnoteEditorAndPreviewFromRelInfo() {
        if (footnoteForm == null) {
            iPartsRelatedInfoFootNote relatedInfo = new iPartsRelatedInfoFootNote();
            RelatedInfoBaseForm form = relatedInfo.newDisplayFormInstance(getConnector(), getParentForm());
            if (form instanceof iPartsRelatedInfoInlineEditFootnoteDataForm) {
                footnoteForm = (iPartsRelatedInfoInlineEditFootnoteDataForm)form;
                footnoteForm.setEditForMaterial(true);
                footnoteEditor = footnoteForm.getFootnoteEditor();
                return form.getGui();
            }
            return null;
        }
        footnoteEditor.reloadFootNotesWithPseudoTransAction(true);
        return footnoteForm.getGui();
    }

    @Override
    protected boolean hideRelatedInfoCalled() {
        if ((editControls == null) || !getConnector().isEditContext()) {
            return true;
        }

        EditUserControls.EditResult editResult = editControls.stopAndStoreEdit();
        EditUserControls.EditResult footnoteResult = EditUserControls.EditResult.UNMODIFIED;
        GenericEtkDataObjectList changeSetDataObjectList = null;
        if (footnoteForm != null) {
            changeSetDataObjectList = footnoteEditor.getDataToSaveToChangeSet();
            if (changeSetDataObjectList != null) {
                footnoteResult = EditUserControls.EditResult.STORED;
            }
        }
        if (((editResult == EditUserControls.EditResult.STORED) || (footnoteResult == EditUserControls.EditResult.STORED)) &&
            (editModuleFormConnector != null)) {
            DBDataObjectAttributes attributes = editControls.getAttributes();
            iPartsPartId newPartId = new iPartsPartId(attributes.getField(EtkDbConst.FIELD_M_MATNR).getAsString(),
                                                      attributes.getField(EtkDbConst.FIELD_M_VER).getAsString());

            if (isRevisionChangeSetActiveForEdit()) { // Speichern im aktiven Änderungsset (ist wohl immer der Fall)
                EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), newPartId);
                part.loadFromDB(newPartId); // Laden, damit das Flag isNew korrekt gesetzt ist
                part.setAttributes(attributes, DBActionOrigin.FROM_EDIT);

                if (footnoteResult == EditUserControls.EditResult.STORED) {
                    addDataObjectListToActiveChangeSetForEdit(changeSetDataObjectList);
                    if (!part.isModified()) {
                        part.getAttributes().markAsModified();
                    }
                }

                if (part.isModifiedWithChildren()) {
                    // addTextModified vor dem Aufruf von addDataObjectToActiveChangeSetForEdit() berechnen, weil alle
                    // modified-Flags in dieser Methode zurückgesetzt werden
                    DBDataObjectAttribute addTextField = part.getAttribute(iPartsConst.FIELD_M_ADDTEXT, false);
                    boolean addTextModified = (addTextField != null) && addTextField.isModified();

                    addDataObjectToActiveChangeSetForEdit(part);
                    setModifiedByEdit(true);

                    // Teil am aktuellen Stücklisteneintrag neu laden
                    if (footnoteResult == EditUserControls.EditResult.STORED) {
                        handleMatFnCache(footnoteEditor.getModifiedMatRefs());
                    }
                    partListEntryForEdit.getPart().loadFromDB(newPartId);

                    // Kombinierten Text bei Bedarf am aktuellen Stücklisteneintrag zurücksetzen und neu laden
                    if (addTextModified) {
                        partListEntryForEdit.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT);
                        partListEntryForEdit.getAttribute(iPartsDataVirtualFieldsDefinition.RETAIL_ADD_TEXT, false);
                        partListEntryForEdit.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL);
                        partListEntryForEdit.getAttribute(iPartsDataVirtualFieldsDefinition.RETAIL_TEXT_NEUTRAL, false);
                        partListEntryForEdit.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);
                        partListEntryForEdit.getAttribute(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT, false);
                    }

                    if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
                        iPartsRelatedInfoEditContext editContext = (iPartsRelatedInfoEditContext)getConnector().getEditContext();
                        editContext.setUpdatePartId(newPartId);
                        editContext.setFireDataChangedEvent(true);
                        editContext.setUpdateEditAssemblyData(true);
                        if (footnoteResult == EditUserControls.EditResult.STORED) {
                            editContext.setUpdateMatFootNotes(true);
                        }
                    }
                }
            } else { // Direktes Speichern in der DB
                getDbLayer().startTransaction();
                try {
                    getDbLayer().saveAttributesWithId(attributes, EtkDbConst.TABLE_MAT, newPartId, partIdForEdit, false,
                                                      true, true, DBDataObject.PrimaryKeyExistsInDB.CHECK,
                                                      EtkDataObject.getTempExtendedDataTypeProvider(getProject(), EtkDbConst.TABLE_MAT),
                                                      false, true);
                    getDbLayer().commit();

                    iPartsDataChangedEventByEdit<PartId> editMaterialEvent = new iPartsDataChangedEventByEdit<PartId>(iPartsDataChangedEventByEdit.DataType.MATERIAL,
                                                                                                                      iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                                      partIdForEdit, false);
                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(editMaterialEvent);
                    ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));

                    // als Ersatz für MaterialChanged-Flag
                    EtkDataAssembly ownerAssembly = partListEntryForEdit.getOwnerAssembly();
                    for (EtkDataPartListEntry partListEntry : ownerAssembly.getPartListUnfiltered(null)) {
                        if (partListEntry.getPart().getAsId().equals(newPartId)) {
                            partListEntry.getPart().loadFromDB(newPartId);
                        }
                    }

                    // zukünftiges TODO hier noch MaterialChanged-Flag setzen
                    ownerAssembly.clearPartListEntriesForEdit();
                    editModuleFormConnector.dataChanged(null);
                } catch (RuntimeException e) {
                    getDbLayer().rollback();
                    Logger.getLogger().handleRuntimeException(e);
                }
            }

            // RelatedInfo-Daten updaten
            getConnector().dataChanged(null);
        }

        if (editResult != EditUserControls.EditResult.ERROR) {
            resetEditControls();

            // EditControls nur dann neu erzeugen, wenn diese Related Info gerade sichtbar ist und die Related Info nicht
            // gerade mit OK geschlossen wird (bei Abbrechen kann der Benutzer ja Veto einlegen!)
            if ((getConnector().getActiveRelatedSubForm() == this) && (getModalResultOfRelatedInfoMainForm() != ModalResult.OK)) {
                showRelatedInfo();
            }
            return true;
        } else {
            return false;
        }
    }

    private void handleMatFnCache(iPartsDataFootNoteMatRefList modifiedMatRefs) {
        if (modifiedMatRefs != null) {
            iPartsPartFootnotesCache footnoteCache = iPartsPartFootnotesCache.getInstance(getProject());
            for (iPartsDataFootNoteMatRef matRef : modifiedMatRefs.getDeletedList()) {
                footnoteCache.deleteCacheForFootNoteMatRef(matRef);
            }
            for (iPartsDataFootNoteMatRef matRef : modifiedMatRefs.getAsList()) {
                footnoteCache.addCacheForFootNoteMatRef(matRef, getProject());
            }
        }
    }

    private class EditUserControlContainer {

        private boolean isMatrix;
        private EditUserControls editControls;
        private EditMatrixUserControls editMatrixControls;

        public EditUserControlContainer(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                        EtkDataPartListEntry partListEntryForEdit) {
            PartId id = partListEntryForEdit.getPart().getAsId();
            boolean masterDataExists = (partListEntryForEdit.getPart().getAttributes() != null);
            String noMasterDataExistsText = "";
            if (!masterDataExists) {
                noMasterDataExistsText = TranslationHandler.translate("!!Keine Stammdaten zur Teilenummer \"%1\" vorhanden",
                                                                      getVisObject().asString(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_BESTNR,
                                                                                              id.getMatNr(), getProject().getDBLanguage()));
            }
            isMatrix = iPartsUserSettingsHelper.isMatrixEdit(dataConnector.getProject());
            if (isMatrix) {
                editControls = null;
                editMatrixControls = new EditMatrixUserControls(dataConnector, parentForm, tableName, id, null, null,
                                                                EditMatrixUserControls.MATRIX_LAYOUT.DEFAULT, noMasterDataExistsText);
            } else {
                editMatrixControls = null;
                editControls = new EditUserControls(dataConnector, parentForm, tableName, id, null, null, false, noMasterDataExistsText);
            }
        }

        public boolean isMatrix() {
            return isMatrix;
        }

        public AbstractGuiControl getGui() {
            if (isMatrix) {
                return editMatrixControls.getGui();
            } else {
                return editControls.getGui();
            }
        }

        public int getEditWindowHeight() {
            if (isMatrix) {
                return editMatrixControls.getParentForm().getRootParentWindow().getHeight();
            } else {
                return editControls.getParentForm().getRootParentWindow().getHeight();
            }
        }

        public void setReadOnly(boolean readOnly) {
            if (isMatrix) {
                editMatrixControls.setReadOnly(readOnly);
            } else {
                editControls.setReadOnly(readOnly);
            }
        }

        public void setEditControlsWidth(int editControlsWidth) {
            if (isMatrix) {
//                editMatrixControls.setEditControlsWidth(editControlsWidth);
            } else {
                editControls.setEditControlsWidth(editControlsWidth);
            }
        }

        public EditUserControls.EditResult stopAndStoreEdit() {
            if (isMatrix) {
                return editMatrixControls.stopAndStoreEdit();
            } else {
                return editControls.stopAndStoreEdit();
            }
        }

        public DBDataObjectAttributes getAttributes() {
            if (isMatrix) {
                return editMatrixControls.getAttributes();
            } else {
                return editControls.getAttributes();
            }
        }

    }
}