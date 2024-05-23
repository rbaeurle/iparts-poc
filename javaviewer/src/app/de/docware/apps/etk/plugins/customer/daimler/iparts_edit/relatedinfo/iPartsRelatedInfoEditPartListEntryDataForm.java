/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsSpecType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.images.iPartsDefaultImages;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsRelatedInfoEditContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditEqualizeFieldsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * Form für die Related Info zum Bearbeiten eines Stücklisteneintrags in iParts.
 */
public class iPartsRelatedInfoEditPartListEntryDataForm extends AbstractRelatedInfoEditDataForm {

    private static final int EDIT_CONTROLS_WIDTH = 600;

    private PartListEntryId loadedPartListEntryId;
    private EtkDataPartListEntry partListEntryForEdit;
    private String oldKPos;
    private boolean oldEvalPemFrom;
    private boolean oldEvalPemTo;
    private EditUserControlContainer editControls;
    private AbstractGuiControl editControlsGui;
    private boolean withInternalText = true;
    private iPartsGuiInternalTextField internalTextField;
    private GuiPanel southPanel;
    private GuiButton syncButton;
    private boolean syncButtonWithoutCheckForModified = true; // true: nur Anzeige des SyncButtons, keine Änderungsabfrage
    private boolean readOnly;

    public iPartsRelatedInfoEditPartListEntryDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                      IEtkRelatedInfo relatedInfo) {
        super(dataConnector, parentForm, relatedInfo);
    }

    private void resetEditControls() {
        if (editControlsGui != null) {
            editPanel.removeAllChildren();
            editControlsGui = null;
        }
        editControls = null;
        partListEntryForEdit = null;
        oldKPos = null;
        oldEvalPemFrom = false;
        oldEvalPemTo = false;

        // EditControls nur dann neu erzeugen, wenn diese Related Info gerade sichtbar ist
        if (((getConnector().getActiveRelatedSubForm() == this) || (getConnector().getActiveRelatedSubForm() == getParentForm()))) {
            showRelatedInfo();
        }
    }

    @Override
    public void dataChanged() {
        resetEditControls(); // Notwendig z.B. bei Änderung vom sprachneutralen Text des Materials
    }

    @Override
    public void updateData(AbstractJavaViewerForm sender, boolean forceUpdateAll) {
        PartListEntryId currentPartListEntryId = getConnector().getRelatedInfoData().getAsPartListEntryId();
        if (forceUpdateAll || ((getConnector().getActiveRelatedSubForm() == this) && !Utils.objectEquals(currentPartListEntryId, loadedPartListEntryId))) {
            // Beim ersten Aufruf von updateData() mit loadedPartListEntryId == null die EditControls nicht zurücksetzen
            if (loadedPartListEntryId != null) {
                resetEditControls();
            }
            loadedPartListEntryId = currentPartListEntryId;
        }
    }

    @Override
    public void showRelatedInfo() {
        // EditControls für die Bearbeitung vom Stücklisteneintrag erzeugen und zum editPanel hinzufügen
        if (editControls == null) {
            partListEntryForEdit = getConnector().getRelatedInfoData().getAsPartListEntry(getProject());
            boolean oldLogLoadFieldIfNeeded = partListEntryForEdit.isLogLoadFieldIfNeeded();
            partListEntryForEdit.setLogLoadFieldIfNeeded(false);
            try {
                oldKPos = partListEntryForEdit.getFieldValue(EtkDbConst.FIELD_K_POS);
                oldEvalPemFrom = partListEntryForEdit.getFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_FROM);
                oldEvalPemTo = partListEntryForEdit.getFieldValueAsBoolean(iPartsConst.FIELD_K_EVAL_PEM_TO);
                if (partListEntryForEdit instanceof iPartsDataPartListEntry) {
                    ((iPartsDataPartListEntry)partListEntryForEdit).updatePEMFlagsFromReplacements();
                }
                if (editModuleFormConnector == null) { // dürfte eigentlich nicht passieren
                    editModuleFormConnector = new EditModuleFormConnector(getConnector());
                    addOwnConnector(editModuleFormConnector);
                    editModuleFormConnector.setCurrentAssembly(partListEntryForEdit.getOwnerAssembly());
                }

                editControls = new EditUserControlContainer(editModuleFormConnector, this, !readOnly,
                                                            partListEntryForEdit, relatedInfo.getName());
                editControlsGui = editControls.getGui();
                // 250 Pixel Abzug für Related Info Buttons und Rahmen
                int maxWidth = Math.min(EDIT_CONTROLS_WIDTH, FrameworkUtils.getScreenSize().width - 250);
                if (!editControls.isMatrix) {
                    editPanel.removeAllChildren(); // FillerPanel raus
                    GuiPanel fillerPanel = new GuiPanel();
                    fillerPanel.setLayout(new LayoutGridBag());
                    editPanel.addChildBorderWest(fillerPanel);
                    editControls.setEditControlsWidth(maxWidth);
                    fillerPanel.addChild(editControlsGui);
                } else {
                    editPanel.removeAllChildren(); // FillerPanel raus
                    editPanel.addChildBorderCenter(editControlsGui);
                }
                // Initialisierung der Guis mit checkValue()
                initGuiFieldsWithCheckValue(null);

                if (withInternalText) {
                    GuiPanel internalPanel = new GuiPanel();
                    internalPanel.setLayout(new LayoutBorder());
                    internalPanel.setName("internalTextPanel");
                    internalPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
                    internalPanel.setPadding(DWLayoutManager.get().isResponsiveMode() ? 8 : 4);
                    internalPanel.setTitle("!!Interner Text");
                    internalTextField = new iPartsGuiInternalTextField(this, partListEntryForEdit);
                    internalPanel.addChildBorderCenter(internalTextField);
                    editControls.addExtraRowElement(internalPanel);
                    internalTextField.setEditable(!readOnly);
                }

                southPanel = createSouthPanel();
                iPartsLockEntryHelper.addLockInfoToPartListEditPanel(southPanel, partListEntryForEdit);
                editPanel.addChild(southPanel);
                southPanel.setVisible(!readOnly);

                if (!syncButtonWithoutCheckForModified) {
                    checkValueChanged(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, EtkDbConst.FIELD_K_POS));
                    checkValueChanged(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVAL_PEM_FROM));
                    checkValueChanged(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsConst.FIELD_K_EVAL_PEM_TO));
                    checkValueChanged(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED));
                    checkValueChanged(TableAndFieldName.make(EtkDbConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED));
                }
            } finally {
                partListEntryForEdit.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
        }
    }

    protected GuiPanel createSouthPanel() {
        GuiPanel panel = new GuiPanel();
        panel.setLayout(new LayoutGridBag(false));
        panel.setName("syncButtonPanel");
        panel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_SOUTH));
        panel.setBorderWidth(4);

        syncButton = new GuiButton();
        syncButton.setText("!!Aktualisieren");
        syncButton.setIcon(iPartsDefaultImages.edit_btn_down.getImage());
        syncButton.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_VERTICAL,
                                                         0, 0, 0, 0));
        if (!syncButtonWithoutCheckForModified) {
            syncButton.setEnabled(false);
        }
        syncButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doSynchronize(event);
            }
        });
        panel.addChild(syncButton);
        return panel;
    }

    private void checkValueChanged(final String tableAndFieldName) {
        AbstractGuiControl guiControl = editControls.getEditGuiControlByFieldName(tableAndFieldName);
        if (guiControl != null) {
            guiControl.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    doValueChanged(event, tableAndFieldName);
                }
            });
        }
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    private iPartsGuiCodeTextField getCodeTextField() {
        AbstractGuiControl guiCtrl = editControls.getEditGuiControlByFieldName(TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_CODES));
        if ((guiCtrl != null) && (guiCtrl instanceof iPartsGuiCodeTextField) && (partListEntryForEdit instanceof iPartsDataPartListEntry)) {
            return (iPartsGuiCodeTextField)guiCtrl;
        }
        return null;
    }

    private void initGuiFieldsWithCheckValue(iPartsGuiCodeTextField codeTextField) {
        if (codeTextField == null) {
            codeTextField = getCodeTextField();
        }
        if (codeTextField != null) {
            iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)partListEntryForEdit;
            String aa = partListEntry.getFieldValue(iPartsConst.FIELD_K_AA);
            String dateFrom = partListEntry.getFieldValue(iPartsConst.FIELD_K_DATEFROM);
            String seriesNumber = "";
            iPartsSeriesId seriesId = partListEntry.getSeriesId();
            if (seriesId != null) {
                seriesNumber = seriesId.getSeriesNumber();
            }

            String productGroupForCodeCheck = partListEntryForEdit.getFieldValue(iPartsConst.FIELD_K_PRODUCT_GRP);

            EtkDataAssembly ownerAssembly = partListEntryForEdit.getOwnerAssembly();
            iPartsDocumentationType docuType = iPartsDocumentationType.UNKNOWN;
            if (ownerAssembly instanceof iPartsDataAssembly) {
                docuType = ((iPartsDataAssembly)(ownerAssembly)).getDocumentationType();
            }

            codeTextField.init(getConnector().getProject(), docuType, seriesNumber, productGroupForCodeCheck, dateFrom, aa, iPartsGuiCodeTextField.CODE_TEST_TYPE.PRODUCTGRP_ONLY);
        }

        // wenn M_ETKZ = S dann ist eine Bearbeitung nur mit dem Recht EDIT_OMIT_FOR_SPECIAL_PARTS erlaubt
        if (partListEntryForEdit instanceof iPartsDataPartListEntry) {
            iPartsDataPartListEntry iPartsEntry = (iPartsDataPartListEntry)partListEntryForEdit;
            if (iPartsEntry.isSpecialprotectionETKZ() && !iPartsRight.EDIT_OMIT_FOR_SPECIAL_PARTS.checkRightInSession()) {
                AbstractGuiControl omitControl = editControls.getEditGuiControlByFieldName(TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_OMIT));
                if (omitControl != null) {
                    omitControl.setEnabled(false);
                }
            }
        }
        EtkDataAssembly assembly = partListEntryForEdit.getOwnerAssembly();
        // Bei Stücklisten, die nicht in eine PSK-Produkt verortet sind, darf das PSK Variantengültigkeit Feld nicht
        // gezeigt werden
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsDataAssembly = (iPartsDataAssembly)assembly;
            if (!iPartsDataAssembly.isPSKAssembly()) {
                EditControl eCtrl = editControls.getEditControlByFieldName(TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY));
                if (eCtrl != null) {
                    eCtrl.setVisible(false);
                }
            }
            if (!iPartsSpecType.isSpecTypeRelevant(iPartsDataAssembly.getSpecType())) {
                EditControl eCtrl = editControls.getEditControlByFieldName(TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_SPEC_VALIDITY));
                if (eCtrl != null) {
                    eCtrl.setVisible(false);
                }
            }
        }

    }

    private void doValueChanged(Event event, String tableAndFieldName) {
        DBDataObjectAttribute attrib = editControls.getEditAttributeValue(tableAndFieldName);
        if (attrib != null) {
            boolean hasChanged = false;
            String fieldName = TableAndFieldName.getFieldName(tableAndFieldName);
            if (fieldName.equals(EtkDbConst.FIELD_K_POS)) {
                // Hat sich POS geändert?
                String kPos = attrib.getAsString();
                if ((kPos != null) && (oldKPos != null)) {
                    hasChanged = !oldKPos.equals(kPos);
                }
            } else {
                boolean value = attrib.getAsBoolean();
                if (fieldName.equals(iPartsConst.FIELD_K_EVAL_PEM_FROM) || fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_FROM_CALCULATED)) {
                    hasChanged = oldEvalPemFrom != value;
                } else if (fieldName.equals(iPartsConst.FIELD_K_EVAL_PEM_TO) || fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_EVAL_PEM_TO_CALCULATED)) {
                    hasChanged = oldEvalPemTo != value;
                }
            }
            if (!syncButton.isEnabled()) {
                syncButton.setEnabled(hasChanged);
            }
        }
    }

    private void doSynchronize(Event event) {
        hideRelatedInfo();
        showRelatedInfo();
        if (!syncButtonWithoutCheckForModified) {
            syncButton.setEnabled(false);
        }
    }

    @Override
    protected boolean hideRelatedInfoCalled() {
        if ((editControls == null) || isReadOnly()) {
            return true;
        }

        GenericEtkDataObjectList modifiedDataObjects = new GenericEtkDataObjectList();

        // Internen Text ins EditChangeSet der RelatedInfo übernehmen
        if (withInternalText && internalTextField.isModified()) {
            modifiedDataObjects.add(internalTextField.getAndUpdateDataInternalText(), DBActionOrigin.FROM_EDIT);
        }

        // Erste den spezifischen Check der Werte machen. Sind diese korrekt, können die allgemeinen Prüfungen der
        // EditControls laufen. Wenn diese ebenfalls in Ordnung sind, werden die neuen Werte gespeichert.
        EditUserControls.EditResult editResult = checkInputValues() ? editControls.stopAndStoreEdit() : EditUserControls.EditResult.ERROR;
        if ((editResult == EditUserControls.EditResult.STORED) && (editModuleFormConnector != null)) {
            iPartsEditUserControlsHelper.handleCodeFieldAfterEdit(partListEntryForEdit);
            iPartsDataPartListEntry.resetAutoCreatedFlag(partListEntryForEdit);
            // Stücklisteneintrag ins EditChangeSet der RelatedInfo übernehmen
            modifiedDataObjects.add(partListEntryForEdit, DBActionOrigin.FROM_EDIT);

            // Vereinheitlichung der Attribute in der AS-Verwendung
            iPartsDataCombTextList combTexts = null;
            AbstractGuiControl combTextCtrl = editControls.getEditGuiControlByFieldName(TableAndFieldName.make(iPartsConst.TABLE_KATALOG,
                                                                                                               iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT));
            if (combTextCtrl instanceof iPartsGuiCombTextCompleteEditControl) {
                combTexts = ((iPartsGuiCombTextCompleteEditControl)combTextCtrl).getAllCombTexts(true);
            }
            Set<AssemblyId> equalizedAssemblyIds = EditEqualizeFieldsHelper.doEqualizePartListEntryEdit(getProject(), partListEntryForEdit,
                                                                                                        combTexts, null,
                                                                                                        modifiedDataObjects);

            // Jetzt die Serialisierung durchführen, da partListEntryForEdit danach nochmal verändert wird
            List<SerializedDBDataObject> serializedDBDataObjectList = addDataObjectListToActiveChangeSetForEdit(modifiedDataObjects);
            if ((serializedDBDataObjectList != null) && !serializedDBDataObjectList.isEmpty()) {
                setModifiedByEdit(true);
            }

            iPartsEditUserControlsHelper.clearVirtualFieldsForReload(partListEntryForEdit);

            iPartsRelatedInfoEditContext editContext = null;
            if (getConnector().getEditContext() instanceof iPartsRelatedInfoEditContext) {
                editContext = (iPartsRelatedInfoEditContext)getConnector().getEditContext();

                // Edit-Stückliste updaten
                editContext.setUpdateEditAssemblyData(true);

                // Module mit vererbten Daten merken, damit deren Caches beim Bestätigen der RelatedEdit gelöscht und
                // die Module im Edit neu geladen werden können
                editContext.addModifiedAssemblyIds(equalizedAssemblyIds);

                // Hat sich POS geändert?
                String kPos = partListEntryForEdit.getFieldValue(EtkDbConst.FIELD_K_POS);
                if ((kPos != null) && (oldKPos != null) && !oldKPos.equals(kPos)) {
                    editContext.setUpdateEditAssemblyPosNumber(true);
                }
            }
            iPartsEditUserControlsHelper.handlePemFlagsAfterEdit(partListEntryForEdit, editContext, oldEvalPemFrom, oldEvalPemTo);

            // RelatedInfo-Daten updaten
            getConnector().dataChanged(null);
        } else if (!modifiedDataObjects.isEmpty()) { // Interner Text muss evtl. serialisiert werden
            List<SerializedDBDataObject> serializedDBDataObjectList = addDataObjectListToActiveChangeSetForEdit(modifiedDataObjects);
            if ((serializedDBDataObjectList != null) && !serializedDBDataObjectList.isEmpty()) {
                setModifiedByEdit(true);

                // RelatedInfo-Daten updaten
                getConnector().dataChanged(null);
            }
        }

        if (editResult != EditUserControls.EditResult.ERROR) {
            resetEditControls();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkInputValues() {
        // nur überprüfen, wenn nicht Abbrechen gedrückt
        if (((RelatedInfoBaseForm)getParentForm()).getModalResultOfRelatedInfoMainForm() == ModalResult.CANCEL) {
            return true;
        }
        iPartsGuiCodeTextField codeTextField = getCodeTextField();
        if (codeTextField != null) {
            // bei Änderung der AA neu initialisieren
            if (partListEntryForEdit.getAttribute(iPartsConst.FIELD_K_AA).isModified()) {
                initGuiFieldsWithCheckValue(codeTextField);
            }

            if (!codeTextField.checkInput()) {
                String errorMessage = codeTextField.getErrorMessage();
                if (StrUtils.isValid(errorMessage)) {
                    MessageDialog.showError(errorMessage);
                    codeTextField.requestFocus();
                    return false;
                } else {
                    String warningMessage = codeTextField.getWarningMessage();
                    if (StrUtils.isValid(warningMessage)) {
                        MessageDialog.showWarning(warningMessage);
                    }
                }
            }
        }
        return true;
    }

    public Dimension getCalculatedPanelSize() {
        if (editControls != null) {
            return editControls.getCalculatedPanelSize();
        } else {
            return new Dimension();
        }
    }


    private class EditUserControlContainer {

        private boolean isMatrix;
        private EditUserPartListEntryControls editControls;
        private EditMatrixUserPartListEntryControls editMatrixControls;

        public EditUserControlContainer(EditFormIConnector dataConnector, AbstractJavaViewerForm parentForm, boolean isEditable,
                                        EtkDataPartListEntry partListEntry, String relatedInfoName) {
            isMatrix = iPartsUserSettingsHelper.isMatrixEdit(dataConnector.getProject());
            if (isMatrix) {
                editControls = null;
                editMatrixControls = EditMatrixUserPartListEntryControls.createPartListEntryControls(dataConnector, parentForm, isEditable,
                                                                                                     partListEntry, relatedInfoName, EditMatrixUserControls.MATRIX_LAYOUT.DEFAULT);
            } else {
                editMatrixControls = null;
                editControls = EditUserPartListEntryControls.createPartListEntryControls(dataConnector, parentForm, isEditable, partListEntry, relatedInfoName);
            }
        }

        public AbstractGuiControl getGui() {
            if (isMatrix) {
                return editMatrixControls.getGui();
            } else {
                return editControls.getGui();
            }
        }

        public Dimension getCalculatedPanelSize() {
            if (isMatrix) {
                return editMatrixControls.getCalculatedPanelSize();
            } else {
                return editControls.getCalculatedPanelSize();
            }
        }

        public boolean isMatrix() {
            return isMatrix;
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

        public EditControl getEditControlByFieldName(String tableAndFieldName) {
            if (isMatrix) {
                return editMatrixControls.getEditControlByFieldName(tableAndFieldName);
            } else {
                return editControls.getEditControlByTableAndFieldName(TableAndFieldName.getTableName(tableAndFieldName),
                                                                      TableAndFieldName.getFieldName(tableAndFieldName));
            }
        }

        public AbstractGuiControl getEditGuiControlByFieldName(String tableAndFieldName) {
            if (isMatrix) {
                return editMatrixControls.getEditGuiControlByFieldName(tableAndFieldName);
            } else {
                EditControl eCtrl = getEditControlByFieldName(tableAndFieldName);
                if (eCtrl != null) {
                    return eCtrl.getEditControl().getControl();
                }
                return null;
            }
        }

        public DBDataObjectAttribute getEditAttributeValue(String tableAndfieldName) {
            if (isMatrix) {
                return editMatrixControls.getEditAttributeValue(tableAndfieldName);
            } else {
                return editControls.getEditAttributeValueByTableAndFieldName(tableAndfieldName);
            }
        }

        public boolean addExtraRowElement(AbstractGuiControl control) {
            if (isMatrix) {
                return editMatrixControls.addExtraRowElement(control);
            } else {
                return editControls.addExtraRowElement(control);
            }
        }
    }
}