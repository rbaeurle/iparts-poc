/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.EtkPluginApi;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsLockEntryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsMatrixEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsMatrixEditFields;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.sql.TableAndFieldName;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Matrix-EditUserControl für {@link EtkDataPartListEntry}
 */
public class EditMatrixUserPartListEntryControls extends EditMatrixUserControls implements iPartsConst {

    private EtkDataPartListEntry originalPartListEntry;
    private EtkDataPartListEntry partListEntry;
    private Set<String> virtualFieldsForExplicitEdit;

    private EditMatrixUserPartListEntryControls(EditFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                EtkDataPartListEntry partListEntry) {
        this(dataConnector, parentForm, partListEntry, null, null, MATRIX_LAYOUT.DEFAULT);
    }

    private EditMatrixUserPartListEntryControls(EditFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                EtkDataPartListEntry partListEntry, iPartsMatrixEditFields externalMatrixEditFields,
                                                Set<String> virtualFieldsForExplicitEdit, MATRIX_LAYOUT matrixLayout) {
        super(dataConnector, parentForm, "", partListEntry.getAsId(), externalMatrixEditFields, matrixLayout);

        // Explizit alle fehlenden Attribute vom Stücklisteneintrag und Material ergänzen, damit durch ein späteres Nachladen
        // die Attribute nicht plötzlich neue Objekte werden
        partListEntry.loadMissingAttributesFromDB(null, false, false, false);
        partListEntry.getPart().loadMissingAttributesFromDB(null, false, false, false);

        this.originalPartListEntry = partListEntry;
        this.partListEntry = partListEntry.cloneMe(dataConnector.getProject());
        if (virtualFieldsForExplicitEdit == null) {
            virtualFieldsForExplicitEdit = new TreeSet<>();
        }
        this.virtualFieldsForExplicitEdit = virtualFieldsForExplicitEdit;
        postPostCreateGui();
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
        if (partListEntry == null) {
            PartListEntryId id = new PartListEntryId("TestOmittedParts_00_010_00001",
                                                     "",
                                                     EtkDbsHelper.formatLfdNr(1));
            partListEntry = EtkDataObjectFactory.createDataPartListEntry(dataConnector.getProject(), id);
            if (partListEntry != null) {
                partListEntry.loadFromDB(id);
            } else {
                return false;
            }
        }
        EditMatrixUserPartListEntryControls eCtrl = createPartListEntryControls(dataConnector, parentForm, isEditable, partListEntry, null, MATRIX_LAYOUT.DEFAULT);
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
     * @param matrixLayout
     * @return {@link EditMatrixUserPartListEntryControls} für die weitere Verwendung
     */
    public static EditMatrixUserPartListEntryControls createPartListEntryControls(EditFormIConnector dataConnector,
                                                                                  AbstractJavaViewerForm parentForm, boolean isEditable,
                                                                                  EtkDataPartListEntry partListEntry, String relatedInfoName, MATRIX_LAYOUT matrixLayout) {
        EditMatrixUserPartListEntryControls eCtrl;
        iPartsMatrixEditFields externalMatrixEditFields = new iPartsMatrixEditFields();
        iPartsDocumentationType docuType = getDocuTypeFromPartListEntry(dataConnector.getProject(), partListEntry);
        EtkDataAssembly assembly = dataConnector.getCurrentAssembly();
        String assemblyTyp = dataConnector.getCurrentAssembly().getEbeneName();
        EtkEditFieldHelper.getEditFieldsForDocuType(dataConnector.getProject(), assemblyTyp, docuType,
                                                    relatedInfoName, externalMatrixEditFields);


        parentForm.startPseudoTransactionForActiveChangeSet(true);
        try {
            eCtrl = new EditMatrixUserPartListEntryControls(dataConnector, parentForm, partListEntry, externalMatrixEditFields, null, matrixLayout);
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

    private static iPartsDocumentationType getDocuTypeFromPartListEntry(EtkProject project, EtkDataPartListEntry partListEntry) {
        iPartsDocumentationType documentationType = iPartsDocumentationType.UNKNOWN;
        EtkDataAssembly assembly = partListEntry.getOwnerAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            // Dokumethode vom Stücklisteneintrag vor dem des Produkts bevorzugen
            documentationType = ((iPartsDataAssembly)assembly).getDocumentationType();
            if (documentationType == iPartsDocumentationType.UNKNOWN) {
                iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
                if (productId != null) {
                    iPartsProduct product = iPartsProduct.getInstance(project, productId);
                    documentationType = product.getDocumentationType();
                }
            }
        }
        return documentationType;
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
    public EditFormIConnector getConnector() {
        return (EditFormIConnector)super.getConnector();
    }

    public DBDataObjectAttribute getEditAttributeValue(String tableAndfieldName) {
        iPartsMatrixEditField field = matrixEditFields.getFeldByName(TableAndFieldName.getTableName(tableAndfieldName),
                                                                     TableAndFieldName.getFieldName(tableAndfieldName));
        if (field != null) {
            return getAttributeFromKey(field);
        }
        return null;
    }

    public boolean addExtraRowElement(AbstractGuiControl control) {
        int newHeight = ultraEditHelper.addExtraRowElement(control);
        if (newHeight != -1) {
            int totalHeight = getHeight() + newHeight;
            setHeight(totalHeight);
            setCalculatedPanelSize(getWidth(), totalHeight);
            return true;
        }
        return false;
    }

    protected void postPostCreateGui() {
        clearEditFieldsPanel();
        matrixEditFields = new iPartsMatrixEditFields();
        if (partListEntry != null) {
            if (externalMatrixEditFields != null) {
                matrixEditFields.assign(externalMatrixEditFields);
            } else {
                iPartsDocumentationType docuType = getDocuTypeFromPartListEntry(getProject(), partListEntry);
                String assemblyTyp = getConnector().getCurrentAssembly().getEbeneName();
                // MatrixEditFelder zum docuTyp laden
                EtkEditFieldHelper.getEditFieldsForDocuType(getProject(), assemblyTyp, docuType, null, matrixEditFields);
            }

            // Virtuelle Felder dürfen generell nicht editierbar sein, Ausnahme: es wird ein Editor zur Verfügung gestellt
            for (EtkEditField editField : matrixEditFields.getFields()) {
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
            ultraEditHelper.calculateHeader(matrixEditFields);
            prepareControls(matrixEditFields);
            // Verknüpfe die Editcontrols für die Baumuster- und die SAA/BK Gültigkeiten.
            iPartsEditUserControlsHelper.connectModelAndSaaBkValidityControls(editControls, iPartsConst.FIELD_K_MODEL_VALIDITY, iPartsConst.FIELD_K_SA_VALIDITY);
            doEnableButtons(null);
        } else {
            GuiLabel noMaterialLabel = new GuiLabel("!!Keine Stammdaten vorhanden.");
            noMaterialLabel.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 100, 100, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_VERTICAL, 0, 0, 0, 0));
            getPanelElements().setLayout(new LayoutGridBag());
            getPanelElements().addChild(noMaterialLabel);
            setReadOnly(true);
        }
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        if (EditUserPartListEntryControls.modifyEditControl(ctrl, field, initialValue, getPartListEntry(), getConnector(), this)) {
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
        partListEntry = originalPartListEntry.cloneMe(getProject());
        for (iPartsMatrixEditField field : matrixEditFields.getVisibleEditFields()) {
            if (!field.isEditierbar()) { // Nicht editierbare Felder ignorieren
                continue;
            }

            DBDataObjectAttribute attrib = getAttributeFromKey(field);
            if (attrib != null) {
                fillAttribByEditControlValue(field, attrib);
            }
        }
        return isModified();
    }

    @Override
    protected void collectEditValues() {
        partListEntry = originalPartListEntry.cloneMe(getProject());
        super.collectEditValues();
        originalPartListEntry.assignRecursively(getProject(), partListEntry, DBActionOrigin.FROM_EDIT);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        checkLockedEntry();
    }

    /**
     * Überprüft, ob die ausgewählte Position für den Edit gesperrt ist. Falls ja, werden die Editoren für die direkten
     * Attribute gesperrt. Indirekte Attribute können weiterhin editiert werden (z.b. Interner Text, kombinierte Texte, usw.)
     */
    private void checkLockedEntry() {
        if (!this.readOnly) {
            // EditControl ist editierbar
            if ((partListEntry != null) && iPartsLockEntryHelper.isLockedWithDBCheck(partListEntry)) {
                // Position ist gesperrt -> Alle EditFelder sperren außer kombinierte Texte
                super.setReadOnly(true);
                // Control für kombinierte Texte heraussuchen und freischalten
                Optional<EditControl> combTextControl = editControls.stream().filter(editControl -> editControl.getEditControl().getFieldName().equals(iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT)).findFirst();
                combTextControl.ifPresent(control -> {
                    control.getEditControl().setReadOnly(false);
                });
            }
        }
    }


}
