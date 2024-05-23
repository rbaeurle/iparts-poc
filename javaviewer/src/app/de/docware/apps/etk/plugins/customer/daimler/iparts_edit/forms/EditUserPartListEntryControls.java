/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.mechanic.listview.forms.AssemblyListFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.EtkPluginApi;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiEventSelectComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiPSKVariantsSelectTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsAAPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;

import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class EditUserPartListEntryControls extends EditUserControls {

    private EtkDataPartListEntry originalPartListEntry;
    private EtkDataPartListEntry partListEntry;
    private Set<String> virtualFieldsForExplicitEdit;

    public static EtkDataPartListEntry doShowDIALOGPartListEntry(AssemblyListFormIConnector connector, AbstractJavaViewerForm parentForm,
                                                                 EtkEditFields externalEditFields, String[] virtualFieldsForExplicitEdit,
                                                                 EtkDataPartListEntry partListEntry) {
        EditUserPartListEntryControls eCtrl;
        EditModuleFormIConnector dataConnector = new EditModuleFormConnector(connector);
        try {
            Set<String> virtFieldsForExplicitEdit = null;
            if (virtualFieldsForExplicitEdit != null) {
                virtFieldsForExplicitEdit = new TreeSet<>();
                for (String tableAndFieldName : virtualFieldsForExplicitEdit) {
                    virtFieldsForExplicitEdit.add(tableAndFieldName);
                }
            }
            eCtrl = new EditUserPartListEntryControls(dataConnector, parentForm, partListEntry, externalEditFields, virtFieldsForExplicitEdit);
            eCtrl.setTitle("!!DIALOG-Stücklisteneintrag bearbeiten");
            eCtrl.setWindowName("DIALOGPartListEntryEditor");
            if (eCtrl.showModal() == ModalResult.OK) {
                if (eCtrl.isModified()) {
                    return eCtrl.getPartListEntry();
                }
            }
            return null;
        } finally {
            dataConnector.dispose();
        }
    }

    /**
     * Editieren/Anzeigen eines {@link EtkDataPartListEntry}s über einen modalen Dialog.
     * Beim Editieren sind die Änderungen direkt im {@link EtkDataPartListEntry} hinterlegt.
     *
     * @param dataConnector
     * @param parentForm
     * @param isEditable
     * @param partListEntry
     * @return Wurde der Dialog mit OK geschlossen?
     */
    public static boolean doShowPartListEntry(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                              boolean isEditable, EtkDataPartListEntry partListEntry) {
        EditUserPartListEntryControls eCtrl = createPartListEntryControls(dataConnector, parentForm, isEditable, partListEntry, null);
        if (eCtrl.showModal() == ModalResult.OK) {
            return true;
        }
        return false;
    }

    /**
     * Editieren/Anzeigen eines {@link EtkDataPartListEntry}s.
     * Beim Editieren sind die Änderungen direkt im {@link EtkDataPartListEntry} hinterlegt.
     *
     * @param dataConnector
     * @param parentForm
     * @param isEditable
     * @param partListEntry
     * @param relatedInfoName
     * @return {@link EditUserPartListEntryControls} für die weitere Verwendung
     */
    public static EditUserPartListEntryControls createPartListEntryControls(EditFormIConnector dataConnector,
                                                                            AbstractJavaViewerForm parentForm, boolean isEditable,
                                                                            EtkDataPartListEntry partListEntry, String relatedInfoName) {
        EditUserPartListEntryControls eCtrl = null;

        parentForm.startPseudoTransactionForActiveChangeSet(true);
        try {
            if (StrUtils.isValid(relatedInfoName) && relatedInfoName.equals(iPartsConst.CONFIG_KEY_RELATED_INFO_EDIT_DETAILS_DATA)) {
                // Auswertung des relatedInfoiName entfällt hier, da keine eigene Konfig (anders bei Matrix)
                EtkEditFields externalEditFields = new EtkEditFields();
                externalEditFields.load(dataConnector.getProject().getConfig(), "");
                if (externalEditFields.size() > 0) {
                    eCtrl = new EditUserPartListEntryControls(dataConnector, parentForm, partListEntry, externalEditFields, null);
                }
            }
            if (eCtrl == null) {
                eCtrl = new EditUserPartListEntryControls(dataConnector, parentForm, partListEntry);
            }
        } finally {
            parentForm.stopPseudoTransactionForActiveChangeSet();
        }

        if (isEditable) {
            eCtrl.setReadOnly(false);
            eCtrl.setMainTitle("!!Stücklistendaten ändern");
        } else {
            eCtrl.setReadOnly(true);
            eCtrl.setMainTitle("!!Stücklistendaten Anzeige");
        }
        eCtrl.setWindowName("PartListEntryEditor");

        return eCtrl;
    }

    protected static boolean modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataPartListEntry partListEntry,
                                               EditFormIConnector connector, AbstractJavaViewerForm parentForm) {
        String fieldName = field.getKey().getFieldName();
        if (fieldName.equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiCombTextCompleteEditControl) {
                iPartsGuiCombTextCompleteEditControl combTextControl = (iPartsGuiCombTextCompleteEditControl)ctrl.getEditControl().getControl();
                combTextControl.setConnector(connector);
                combTextControl.setPartListEntryId(partListEntry.getAsId());
                combTextControl.setText(initialValue);
                return true;
            }
        } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT)) {
            iPartsEditUserControlsHelper.handleDocuRelevantControl(parentForm.getProject(), ctrl, initialValue);
            return true;
        } else if (fieldName.equals(iPartsConst.FIELD_K_MODEL_VALIDITY)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiModelSelectTextField) {
                iPartsGuiModelSelectTextField modelTextField = (iPartsGuiModelSelectTextField)ctrl.getEditControl().getControl();
                iPartsProductId productId = null;
                if (connector.getCurrentAssembly() instanceof iPartsDataAssembly) {
                    productId = ((iPartsDataAssembly)connector.getCurrentAssembly()).getProductIdFromModuleUsage();
                }
                modelTextField.init(parentForm);
                modelTextField.setProductId(productId);
                return true;
            }
        } else if (fieldName.equals(iPartsConst.FIELD_K_SA_VALIDITY)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiSAABkSelectTextField) {
                iPartsGuiSAABkSelectTextField saaTextField = (iPartsGuiSAABkSelectTextField)ctrl.getEditControl().getControl();
                iPartsProductId productId = null;
                if (connector.getCurrentAssembly() instanceof iPartsDataAssembly) {
                    productId = ((iPartsDataAssembly)connector.getCurrentAssembly()).getProductIdFromModuleUsage();
                }
                saaTextField.init(parentForm, productId, partListEntry.getFieldValueAsArray(iPartsConst.FIELD_K_MODEL_VALIDITY).getArrayAsStringList());
                return true;
            }
        } else if (fieldName.equals(iPartsConst.FIELD_K_PSK_VARIANT_VALIDITY)) {
            boolean isPSK = false;
            if (connector.getCurrentAssembly() instanceof iPartsDataAssembly) {
                iPartsDataAssembly assembly = (iPartsDataAssembly)connector.getCurrentAssembly();
                isPSK = assembly.isPSKAssembly();
            }
            if (isPSK) {
                if (ctrl.getEditControl().getControl() instanceof iPartsGuiPSKVariantsSelectTextField) {
                    iPartsGuiPSKVariantsSelectTextField pskVariantsTextField = (iPartsGuiPSKVariantsSelectTextField)ctrl.getEditControl().getControl();
                    iPartsProductId productId = ((iPartsDataAssembly)connector.getCurrentAssembly()).getProductIdFromModuleUsage();
                    pskVariantsTextField.init(parentForm, productId);
                    return true;
                }
            }
        } else if (field.getKey().getFieldName().equals(iPartsConst.FIELD_K_MENGE)) {
            if (ctrl.getEditControl().getControl() instanceof GuiTextField) {
                GuiTextField guiTextField = (GuiTextField)(ctrl.getEditControl().getControl());
                guiTextField.setCaseMode(GuiTextField.CaseMode.UPPERCASE);
                return true;
            }
        } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_STATUS)) {
            iPartsEditUserControlsHelper.handleDIALOGStatusControl(ctrl, initialValue);
            return true;
        } else if (fieldName.equals(iPartsConst.FIELD_K_AA)) {
            if (connector.getCurrentAssembly() instanceof iPartsDataAssembly) {
                iPartsAAPartsHelper.setAAEnumValuesByProduct(ctrl.getEditControl(), parentForm.getProject(),
                                                             (iPartsDataAssembly)connector.getCurrentAssembly());
            }
            return true;
        } else if (fieldName.equals(iPartsConst.FIELD_K_EVENT_FROM) || fieldName.equals(iPartsConst.FIELD_K_EVENT_TO)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiEventSelectComboBox) {
                String eventId;
                if (fieldName.equals(iPartsConst.FIELD_K_EVENT_FROM)) {
                    eventId = partListEntry.getFieldValue(iPartsConst.FIELD_K_EVENT_FROM);
                } else {
                    eventId = partListEntry.getFieldValue(iPartsConst.FIELD_K_EVENT_TO);
                }

                iPartsSeriesId seriesId = null;
                if (partListEntry instanceof iPartsDataPartListEntry) {
                    seriesId = ((iPartsDataPartListEntry)partListEntry).getSeriesId();
                }

                iPartsGuiEventSelectComboBox eventSelectComboBox = (iPartsGuiEventSelectComboBox)ctrl.getEditControl().getControl();
                eventSelectComboBox.init(parentForm.getProject(), seriesId, eventId);
                return true;
            }
        }
        return false;
    }

    private EditUserPartListEntryControls(EditFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          EtkDataPartListEntry partListEntry) {
        this(dataConnector, parentForm, partListEntry, null, null);
    }

    private EditUserPartListEntryControls(EditFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          EtkDataPartListEntry partListEntry, EtkEditFields externalEditFields, Set<String> virtualFieldsForExplicitEdit) {
        super(dataConnector, parentForm, "", partListEntry.getAsId(), externalEditFields);

        // Explizit alle fehlenden Attribute vom Stücklisteneintrag und Material ergänzen, damit durch ein späteres Nachladen
        // die Attribute nicht plötzlich neue Objekte werden
        partListEntry.loadMissingAttributesFromDB(null, false, false, false);
        partListEntry.getPart().loadMissingAttributesFromDB(null, false, false, false);

        this.originalPartListEntry = partListEntry;
        this.partListEntry = partListEntry.cloneMe(dataConnector.getProject());
        if (virtualFieldsForExplicitEdit == null) {
            virtualFieldsForExplicitEdit = new TreeSet<String>();
        }
        this.virtualFieldsForExplicitEdit = virtualFieldsForExplicitEdit;
        postPostCreateGui();
    }

    @Override
    protected void setAttributes() {
    }

    @Override
    public DBDataObjectAttributes getAttributes() {
        if (originalPartListEntry != null) {
            return originalPartListEntry.getAttributes();
        } else {
            return null;
        }
    }

    public EtkDataPartListEntry getPartListEntry() {
        return originalPartListEntry;
    }

    @Override
    protected void postCreateGui() {

    }

    @Override
    public EditModuleFormIConnector getConnector() {
        return (EditModuleFormIConnector)super.getConnector();
    }

    public DBDataObjectAttribute getEditAttributeValue(String fieldName) {
        return getCurrentAttributeValue(fieldName);
    }

    public DBDataObjectAttribute getEditAttributeValueByTableAndFieldName(String tableAndFieldName) {
        return getCurrentAttributeValueByTableAndFieldName(tableAndFieldName);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        checkLockedEntry();
    }

    private void checkLockedEntry() {
        if (!this.readOnly) {
            // EditControl ist editierbar
            if ((partListEntry != null) && iPartsLockEntryHelper.isLockedWithDBCheck(partListEntry)) {
                // Position ist gesperrt -> Alle EditFelder sperren außer kombinierte Texte
                super.setReadOnly(true);
                // Control für kombinierte Texte heraussuchen und freischalten
                EditControl combTextControl = getEditControlByFieldName(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);
                if (combTextControl != null) {
                    combTextControl.getEditControl().setReadOnly(false);
                }
            }
        }
    }

    protected void postPostCreateGui() {
        clearEditFieldsPanel();
        editFields = new EtkEditFields();
        if (partListEntry != null) {
            if (externalEditFields != null) {
                editFields.assign(externalEditFields);
            } else {
                String assemblyTyp = getConnector().getCurrentAssembly().getEbeneName();
                // EditFelder zum StüLiTyp laden
                EtkEditFieldHelper.getEditFieldsForProductEdit(getProject(), assemblyTyp, editFields);
            }

            // Virtuelle Felder dürfen generell nicht editierbar sein, Ausnahme: es wird ein Editor zur Verfügung gestellt
            for (EtkEditField editField : editFields.getFields()) {
                String fieldName = editField.getKey().getFieldName();
                if (VirtualFieldsUtils.isVirtualField(fieldName)) {
                    if (editField.isEditierbar()) {
                        // soll das Virtuelle Feld auch ohne Plugin-Editor editierbar sein?
                        if (!virtualFieldsForExplicitEdit.contains(editField.getKey().getName())) {
                            editField.setEditierbar(EtkPluginApi.hasEditorForVirtualField(editField.getKey().getTableName(), fieldName));
                        }
                    }
                }
            }

            prepareControls(editFields);
            // Verknüpfe die Editcontrols für die Baumuster- und die SAA/BK Gültigkeiten.
            iPartsEditUserControlsHelper.connectModelAndSaaBkValidityControls(editControls, iPartsConst.FIELD_K_MODEL_VALIDITY, iPartsConst.FIELD_K_SA_VALIDITY);
            doEnableButtons(null);
        } else {
            GuiLabel noMaterialLabel = new GuiLabel(TranslationHandler.translate("!!Stücklistendaten nicht gefunden!"));
            noMaterialLabel.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 100, 0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE, 8, 8, 8, 8));
            addEditFieldChild(noMaterialLabel);
            setReadOnly(true);
        }
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        if (modifyEditControl(ctrl, field, initialValue, getPartListEntry(), getConnector(), this)) {
            return;
        }
        super.modifyEditControl(ctrl, field, initialValue, initialDataArray);
    }

    @Override
    protected boolean checkReadOnlyValue(boolean readOnly) {
        if (partListEntry == null) {
            readOnly = true;
        }
        return readOnly;
    }

    private EtkDataObject getDataObjectByTable(String tableName) {
        if (partListEntry != null) {
            return partListEntry.getDataObjectByTableName(tableName, false);
        }
        return null;
    }

    @Override
    protected DBDataObjectAttribute getAttributeFromKey(EtkEditField field) {
        EtkDataObject dataObject = getDataObjectByTable(field.getKey().getTableName());
        if (dataObject != null) {
            boolean oldLogLoadFieldIfNeeded = dataObject.isLogLoadFieldIfNeeded();
            dataObject.setLogLoadFieldIfNeeded(false);
            try {
                return dataObject.getAttribute(field.getKey().getFieldName(), false);
            } finally {
                dataObject.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
        }
        return null;
    }

    protected DBDataObjectAttributes getAttributesByTable(String tableName) {
        EtkDataObject dataObject = getDataObjectByTable(tableName);
        if (dataObject != null) {
            return dataObject.getAttributes();
        }
        return null;
    }

    @Override
    protected boolean isModified() {
        boolean modified = false;
        if (partListEntry != null) {
            modified = partListEntry.isModifiedWithChildren();
        }
//        EtkDataObject dataObject = partListEntry.getDataObjectByTabName(EtkDbConst.TABLE_KATALOG, false);
//        if (dataObject != null) {
//            modified = dataObject.getAttributes().isModified();
//        }
//        if (!modified) {
//            dataObject = partListEntry.getDataObjectByTabName(EtkDbConst.TABLE_MAT, false);
//            if (dataObject != null) {
//                modified = dataObject.getAttributes().isModified();
//            }
//        }
        return modified;
    }

    @Override
    protected boolean checkForModified() {
        int index = 0;
        partListEntry = originalPartListEntry.cloneMe(getProject());
        for (EtkEditField field : editFields.getVisibleEditFields()) {
            if (!field.isEditierbar()) { // Nicht editierbare Felder ignorieren
                index++;
                continue;
            }

            DBDataObjectAttribute attrib = getAttributeFromKey(field);
            if (attrib != null) {
                fillAttribByEditControlValue(index, field, attrib);
            }
            index++;
        }
        return isModified();
    }

    @Override
    protected void collectEditValues() {
        partListEntry = originalPartListEntry.cloneMe(getProject());
        super.collectEditValues();
        originalPartListEntry.assignRecursively(getProject(), partListEntry, DBActionOrigin.FROM_EDIT);
    }
}