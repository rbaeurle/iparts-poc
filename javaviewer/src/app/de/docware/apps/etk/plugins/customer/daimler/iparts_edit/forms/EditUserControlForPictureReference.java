/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.drawing.DataImageId;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiEventSelectComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiPSKVariantsSelectTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.utils.EtkDataArray;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Editor zum Bearbeiten der Attribute einer Bildreferenz
 */
public class EditUserControlForPictureReference extends EditUserControls implements iPartsConst {

    private static final Set<String> NO_EDIT_ALLOWED = new HashSet<String>(Arrays.asList(new String[]{ FIELD_I_IMAGES, FIELD_I_PVER }));


    public static boolean editPictureReferenceAttributes(EditModuleFormIConnector connector, EditAssemblyImageForm parentForm, DataImageId imageId) {
        EditUserControlForPictureReference pictureReferenceUserControl = new EditUserControlForPictureReference(connector, parentForm, imageId);
        pictureReferenceUserControl.setMainTitle("!!Bildreferenz Editor");
        pictureReferenceUserControl.setTitle("!!Gültigkeiten editieren");
        if (pictureReferenceUserControl.showModal() == ModalResult.OK) {
            return pictureReferenceUserControl.storePicturesData();
        }
        return false;
    }

    public EditUserControlForPictureReference(EditModuleFormIConnector dataConnector, AbstractJavaViewerForm parentForm, DataImageId imageId) {
        super(dataConnector, parentForm, TABLE_IMAGES, imageId);
    }

    /**
     * Speichert die veränderten Werte im Changeset
     *
     * @return
     */
    private boolean storePicturesData() {
        DataImageId imageId = getImageId();
        if ((imageId != null) && (getConnectorWithAssembly() != null) && isModified()) {
            DBDataObjectAttributes attributes = getCurrentAttributes();
            EtkDataAssembly assembly = getConnectorWithAssembly().getCurrentAssembly();
            if (assembly != null) {
                assembly.getAttributes().markAsModified();
                EtkDataImage image = getCurrentImageFromAssembly();
                if (image != null) {
                    image.setAttributes(attributes, DBActionOrigin.FROM_EDIT);
                }
                assembly.getRevisionsHelper().addDataObjectToActiveChangeSetForEdit(assembly);
                return true;
            }
        }
        return false;
    }

    /**
     * Liefert die aktuell zu bearbeitende Bildreferenz aus der aktuellen Assembly
     *
     * @return
     */
    private EtkDataImage getCurrentImageFromAssembly() {
        if ((getConnectorWithAssembly() != null) && (getImageId() != null)) {
            for (EtkDataImage image : getConnectorWithAssembly().getCurrentAssembly().getUnfilteredImages()) {
                if (image.getAsId().equals(getImageId())) {
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * Liefert einen Connector, der auch ein Assembly enthält.
     *
     * @return
     */
    public EditModuleFormIConnector getConnectorWithAssembly() {
        if ((super.getConnector() instanceof EditModuleFormIConnector)) {
            return (EditModuleFormIConnector)super.getConnector();
        }
        return null;
    }

    @Override
    protected void postCreateGui() {
        setEditFields();
        super.postCreateGui();
        // Verknüpfung zwischen den EditFeldern "Baumustergültigkeit" und "Saa/BK Gültigkeit"
        iPartsEditUserControlsHelper.connectModelAndSaaBkValidityControls(editControls, FIELD_I_MODEL_VALIDITY, FIELD_I_SAA_CONSTKIT_VALIDITY);
    }

    private void setEditFields() {
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(getConfig(), CONFIG_KEY_PICTURE_REFERENCES_EDIT_FIELDS);
        if (editFields.size() == 0) {
            EtkEditField editField = new EtkEditField(TABLE_IMAGES, FIELD_I_CODES, false);
            editFields.addFeld(editField);
            editField = new EtkEditField(TABLE_IMAGES, FIELD_I_MODEL_VALIDITY, false);
            editFields.addFeld(editField);
            editField = new EtkEditField(TABLE_IMAGES, FIELD_I_SAA_CONSTKIT_VALIDITY, false);
            editFields.addFeld(editField);
            editField = new EtkEditField(TABLE_IMAGES, FIELD_I_EVENT_FROM, false);
            editFields.addFeld(editField);
            editField = new EtkEditField(TABLE_IMAGES, FIELD_I_EVENT_TO, false);
            editFields.addFeld(editField);
            editField = new EtkEditField(TABLE_IMAGES, FIELD_I_PSK_VARIANT_VALIDITY, false);
            editFields.addFeld(editField);
            editFields.loadStandards(getConfig());
        }

        for (EtkEditField field : editFields.getVisibleEditFields()) {
            // Nur anzeigen, wenn die Baureihe zum Produkt Event-gesteuert ist
            String fieldName = field.getKey().getFieldName();
            if (fieldName.equals(FIELD_I_EVENT_FROM) || fieldName.equals(FIELD_I_EVENT_TO)) {
                if (!isSeriesFromProductEventControlled()) {
                    field.setVisible(false);
                }
            }
            // Nur anzeigen, wenn es sich um ein PSK-Produkt handelt
            if (fieldName.equals(FIELD_I_PSK_VARIANT_VALIDITY)) {
                if (!isPSKAssembly()) {
                    field.setVisible(false);
                }
            }
        }
        // DAIMLER-15226: Feld I_NAVIGATION_PERSPECTIVE anpassen
        boolean isCarPerspective = EditModuleHelper.isCarPerspectiveAssembly(getAssembly());
        EtkEditField editField = editFields.getFeldByName(TABLE_IMAGES, FIELD_I_NAVIGATION_PERSPECTIVE);
        if (isCarPerspective) {
            if (editField == null) {
                editFields.addFeld(new EtkEditField(TABLE_IMAGES, FIELD_I_NAVIGATION_PERSPECTIVE, false));
            }
        } else {
            if (editField != null) {
                editField.setVisible(false);
            }
        }
        setExternalEditFields(editFields);
    }

    /**
     * Liefert das Produkt zu Modul.
     *
     * @return
     */
    private iPartsProduct getProductFromAssembly() {
        iPartsDataAssembly assembly = getAssembly();
        if (assembly != null) {
            iPartsProductId productId = assembly.getProductIdFromModuleUsage();
            if (productId != null) {
                return iPartsProduct.getInstance(getProject(), productId);
            }
        }
        return null;
    }

    /**
     * Liefert das aktuelle {@link iPartsDataAssembly} Objekt zurück
     *
     * @return
     */
    private iPartsDataAssembly getAssembly() {
        if (getConnectorWithAssembly().getCurrentAssembly() instanceof iPartsDataAssembly) {
            return (iPartsDataAssembly)getConnectorWithAssembly().getCurrentAssembly();
        }
        return null;
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {

        // PK Felder dürfen nicht editiert werden
        EtkDatabaseTable tableDef = getProject().getConfig().getDBDescription().findTable(tableName);
        String fieldName = field.getKey().getFieldName();
        List<String> pkFields = tableDef.getPrimaryKeyFields();
        if (pkFields.contains(field.getKey().getFieldName()) || NO_EDIT_ALLOWED.contains(field.getKey().getFieldName())) {
            field.setEditierbar(false);
        }

        iPartsDataAssembly assembly = getAssembly();
        if (fieldName.equals(iPartsConst.FIELD_I_CODES)) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiCodeTextField) {
                iPartsGuiCodeTextField guiCodeTextField = (iPartsGuiCodeTextField)ctrl.getEditControl().getControl();
                if (assembly != null) {
                    iPartsProduct product = getProductFromAssembly();
                    if (product != null) {
                        String series = "";
                        if (product.getReferencedSeries() != null) {
                            series = product.getReferencedSeries().getSeriesNumber();
                        }
                        guiCodeTextField.init(getConnector().getProject(), assembly.getDocumentationType(), series, product.getProductGroup(), "", "", iPartsGuiCodeTextField.CODE_TEST_TYPE.PRODUCTGRP_ONLY);
                    }
                }
            }
        } else if (fieldName.equals(iPartsConst.FIELD_I_MODEL_VALIDITY)) {
            // Setze das Produkt für die Baumustergültigkeit
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiModelSelectTextField) {
                iPartsGuiModelSelectTextField modelTextField = (iPartsGuiModelSelectTextField)ctrl.getEditControl().getControl();
                iPartsProductId productId = null;
                if (assembly != null) {
                    productId = assembly.getProductIdFromModuleUsage();
                }
                modelTextField.init(this);
                modelTextField.setProductId(productId);
            }
        } else if (fieldName.equals(iPartsConst.FIELD_I_SAA_CONSTKIT_VALIDITY)) {
            // Setze die Baumuster aus der Baumustergültigkeit
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiSAABkSelectTextField) {
                iPartsGuiSAABkSelectTextField saaTextField = (iPartsGuiSAABkSelectTextField)ctrl.getEditControl().getControl();
                iPartsProductId productId = null;
                if (assembly != null) {
                    productId = assembly.getProductIdFromModuleUsage();
                }
                EtkDataImage image = getCurrentImageFromAssembly();
                if (image != null) {
                    saaTextField.init(this, productId, image.getFieldValueAsArray(iPartsConst.FIELD_I_MODEL_VALIDITY).getArrayAsStringList());
                }
            }
        } else if (fieldName.equals(iPartsConst.FIELD_I_PSK_VARIANT_VALIDITY)) {
            if (isPSKAssembly()) {
                if (ctrl.getEditControl().getControl() instanceof iPartsGuiPSKVariantsSelectTextField) {
                    iPartsGuiPSKVariantsSelectTextField pskVariantsSelectTextField = (iPartsGuiPSKVariantsSelectTextField)ctrl.getEditControl().getControl();
                    iPartsProductId productId = null;
                    if (assembly != null) {
                        productId = assembly.getProductIdFromModuleUsage();
                    }
                    pskVariantsSelectTextField.init(this, productId);
                }
            }
        } else if ((fieldName.equals(iPartsConst.FIELD_I_EVENT_FROM) || fieldName.equals(iPartsConst.FIELD_I_EVENT_TO))
                   && isSeriesFromProductEventControlled()) {
            if (ctrl.getEditControl().getControl() instanceof iPartsGuiEventSelectComboBox) {
                String eventId;
                EtkDataImage currentImage = getCurrentImageFromAssembly();
                if (currentImage != null) {
                    if (fieldName.equals(iPartsConst.FIELD_I_EVENT_FROM)) {
                        eventId = currentImage.getFieldValue(iPartsConst.FIELD_I_EVENT_FROM);
                    } else {
                        eventId = currentImage.getFieldValue(iPartsConst.FIELD_I_EVENT_TO);
                    }
                    iPartsProduct product = getProductFromAssembly();
                    if (product != null) {
                        iPartsSeriesId seriesId = product.getReferencedSeries();
                        iPartsGuiEventSelectComboBox eventSelectComboBox = (iPartsGuiEventSelectComboBox)ctrl.getEditControl().getControl();
                        eventSelectComboBox.init(getProject(), seriesId, eventId);
                    }
                }
            }
        }
        super.modifyEditControl(ctrl, field, initialValue, initialDataArray);
    }

    /**
     * Gibt zurück, ob die Baureihe am Produkt zum Modul eventgesteuert ist.
     *
     * @return
     */
    private boolean isSeriesFromProductEventControlled() {
        iPartsDataAssembly assembly = getAssembly();
        if (assembly != null) {
            return assembly.isSeriesFromProductModuleUsageEventControlled();
        }
        return false;
    }

    private boolean isPSKAssembly() {
        iPartsDataAssembly assembly = getAssembly();
        if (assembly != null) {
            return assembly.isPSKAssembly();
        }
        return false;
    }

    /**
     * Liefert die id aus der Superklasse als {@link DataImageId}
     *
     * @return
     */
    public DataImageId getImageId() {
        if ((id != null) && id.getType().equals(DataImageId.TYPE)) {
            return (DataImageId)id;
        }
        return null;
    }

    @Override
    protected boolean checkCompletionOfFormValues() {
        boolean result = super.checkCompletionOfFormValues();
        if (result) {
            EditControl codeControl = iPartsEditUserControlsHelper.findControlByFieldname(editControls, FIELD_I_CODES);
            if ((codeControl != null) && (codeControl.getEditControl().getControl() instanceof iPartsGuiCodeTextField)) {
                iPartsGuiCodeTextField codeTextField = (iPartsGuiCodeTextField)codeControl.getEditControl().getControl();
                result = iPartsEditUserControlsHelper.checkCodeFieldWithErrorMessage(codeTextField);
            }
        }
        return result;
    }
}
