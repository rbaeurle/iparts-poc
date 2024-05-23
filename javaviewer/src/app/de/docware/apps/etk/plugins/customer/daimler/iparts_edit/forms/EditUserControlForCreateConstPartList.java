/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiEventSelectComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.AbstractConstraints;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.Collections;
import java.util.List;

/**
 * EditUserControls für Anlage einer Konstruktiven Stückliste.
 */
public class EditUserControlForCreateConstPartList extends EditUserControls implements iPartsConst {

    private static final String[] INVISIBLE_FIELDS = new String[]{ FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM };
    private static boolean showCombinedHMMSMEditor = true;

    private List<String> isEmptyValueAllowedFields;
    private EditHMMSMControl editHMMSMControl;

    public EditUserControlForCreateConstPartList(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, TABLE_DA_DIALOG, id, attributes, externalEditFields);
        isEmptyValueAllowedFields = new DwList<>();
    }

    public List<String> getIsEmptyValueAllowedFields() {
        return isEmptyValueAllowedFields;
    }

    public void setIsEmptyValueAllowedFields(String[] isEmptyValueAllowedFields) {
        this.isEmptyValueAllowedFields = new DwList<>(isEmptyValueAllowedFields);
    }

    public void modifyAttributesForDirectReturn() {
        if (attributes != null) {
            DBDataObjectAttribute attrib = attributes.getField(FIELD_DD_POSV, false);
            if (attrib != null) {
                attrib.setValueAsString(attrib.getAsString() + "x", DBActionOrigin.FROM_DB);
                doEnableButtons(null);
            }
        }
    }

    public void setHmMSmEditorEnabled(boolean enabled) {
        if (editHMMSMControl != null) {
            editHMMSMControl.setEnabled(false, enabled);
        }
    }

    @Override
    protected void postCreateGui() {
        if (showCombinedHMMSMEditor) {
            // entferne das Panel mit den eigentlichen EditControls aus dem Parent und verwende es im Border Layout als Center
            AbstractGuiControl panelOriginalEditControls = getGui();
            removeChildFromPanelMain(panelOriginalEditControls);
            AbstractConstraints panelOriginalConstraints = panelOriginalEditControls.getConstraints();

            editHMMSMControl = new EditHMMSMControl(getConnector(), this);
            editHMMSMControl.setEnabled(false, true);
            editHMMSMControl.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    doEnableButtons(event);
                }
            });
            // ein neues Haupt-Panel erzeugen das statt dem Originalen verwendet wird
            GuiPanel panelGridMaster = new GuiPanel();
            panelGridMaster.setPadding(4);
            panelGridMaster.setLayout(new LayoutBorder());
            panelGridMaster.addChildBorderCenter(panelOriginalEditControls);
            panelGridMaster.addChildBorderNorth(editHMMSMControl);

            panelGridMaster.setConstraints(panelOriginalConstraints);
            addChildToPanelMain(panelGridMaster);
        }
        super.postCreateGui();

        initHMMSMEditor();
    }

    @Override
    protected void resizeForm(int totalHeight) {
        if (showCombinedHMMSMEditor) {
            // 20 px für Border und zusätzliches Padding gegenüber 3 einzelnen Textfields
            super.resizeForm(totalHeight + 20);
        } else {
            super.resizeForm(totalHeight);
        }
    }

    @Override
    protected void insertEditControl(EditControl ctrl, EtkEditField field, int gridY, EventListener listener) {
        if (showCombinedHMMSMEditor) {
            List<String> invisibleFields = new DwList<>(INVISIBLE_FIELDS);
            String fieldName = field.getKey().getFieldName();
            if (invisibleFields.contains(fieldName)) {
                return;
            }
        }
        super.insertEditControl(ctrl, field, gridY, listener);
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        String tableAndFieldName = field.getKey().getName();
        if (tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_PARTNO))) {
            iPartsGuiMaterialSelectTextField matTextField = new iPartsGuiMaterialSelectTextField();
            if (field.isEditierbar()) {
                matTextField.init(getParentForm(), false);
            }
            matTextField.setText(initialValue);
            ctrl.getEditControl().setControl(matTextField);
        } else if (tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_EVENT_FROM)) ||
                   tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_EVENT_TO))) {
            iPartsGuiEventSelectComboBox eventComboBox = new iPartsGuiEventSelectComboBox();
            iPartsSeriesId seriesId = new iPartsSeriesId(getAttributes().getField(FIELD_DD_SERIES_NO).getAsString());
            if (field.isEditierbar()) {
                eventComboBox.init(getProject(), seriesId, initialValue);
            }
            ctrl.getEditControl().setControl(eventComboBox);
        } else if (tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_RFMEA))) {
            RComboBox<String> comboboxRFMEA = new RComboBox<>();
            if (field.isEditierbar()) {
                comboboxRFMEA.addItems(getEnumList(EditRFMEFlagsForm.ENUM_KEY_RFMEA, true));
                comboboxRFMEA.setMaximumRowCount(Math.min(comboboxRFMEA.getItemCount(), 15));
            }
            comboboxRFMEA.setSelectedItem(initialValue);
            ctrl.getEditControl().setControl(comboboxRFMEA);
        } else if (tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_RFMEN))) {
            RComboBox<String> comboboxRFMEN = new RComboBox<>();
            if (field.isEditierbar()) {
                comboboxRFMEN.addItems(getEnumList(EditRFMEFlagsForm.ENUM_KEY_RFMEN, true));
                comboboxRFMEN.setMaximumRowCount(Math.min(comboboxRFMEN.getItemCount(), 15));
            }
            comboboxRFMEN.setSelectedItem(initialValue);
            ctrl.getEditControl().setControl(comboboxRFMEN);
        } else if (tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_CODES))) {
            iPartsGuiCodeTextField codeTextField = new iPartsGuiCodeTextField();
            if (field.isEditierbar()) {
                String productGroup = getAttributes().getField(FIELD_DD_PRODUCT_GRP).getAsString();
                String ausfuehrungsArt = getAttributes().getField(FIELD_DD_AA).getAsString();
                // Anlegen von Stücklisteneinträgen in der Konstruktion ist derzeit nur für DIALOG möglich, deshalb wird hier
                // der DokuTyp DIALOG für die Validierung der Codebedingung verwendet
                codeTextField.init(getConnector().getProject(), iPartsDocumentationType.DIALOG, getAttributes().getField(FIELD_DD_SERIES_NO).getAsString(), productGroup, "", ausfuehrungsArt, iPartsGuiCodeTextField.CODE_TEST_TYPE.PRODUCTGRP_ONLY);
            }
            codeTextField.setText(initialValue);
            ctrl.getEditControl().setControl(codeTextField);
        } else if (tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_AA))) {
            boolean ignoreDisplayOptions = true;
            boolean enumIgnoreBlankTexts = false;
            if (ctrl.getEditControl().getControl() instanceof EnumRComboBox) {
                EnumRComboBox oldComboBox = (EnumRComboBox)ctrl.getEditControl().getControl();
                ignoreDisplayOptions = oldComboBox.isIgnoreDisplayOptions();
                enumIgnoreBlankTexts = oldComboBox.isIgnoreBlankTexts();
            }
            iPartsGuiAARComboBox comboBox = new iPartsGuiAARComboBox();
            EditControlFactory.setDefaultLayout(comboBox);
            if (field.isEditierbar()) {
                comboBox.setIgnoreDisplayOptions(ignoreDisplayOptions);
                comboBox.setIgnoreBlankTexts(enumIgnoreBlankTexts);
            }
            comboBox.init(getConnector().getProject(), new iPartsSeriesId(getAttributes().getField(FIELD_DD_SERIES_NO).getAsString()),
                          tableName, FIELD_DD_AA, true);
            comboBox.setSelectedItem(initialValue);
            ctrl.getEditControl().setControl(comboBox);
        } else if (tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_DOCU_RELEVANT))) {
            iPartsEditUserControlsHelper.handleDocuRelevantControl(getProject(), ctrl, initialValue);
        } else if (tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_POSE)) ||
                   tableAndFieldName.equals(TableAndFieldName.make(tableName, FIELD_DD_POSV))) {
            iPartsGuiConstPosTextField posTextField = new iPartsGuiConstPosTextField();
            posTextField.setText(initialValue, false);
            ctrl.getEditControl().setControl(posTextField);
        }
    }

    private void initHMMSMEditor() {
        if (editHMMSMControl != null) {
            String series = getAttributes().getFieldValue(FIELD_DD_SERIES_NO);
            String hm = getAttributes().getFieldValue(FIELD_DD_HM);
            String m = getAttributes().getFieldValue(FIELD_DD_M);
            String sm = getAttributes().getFieldValue(FIELD_DD_SM);
            editHMMSMControl.initHmMSmId(new HmMSmId(series, hm, m, sm));

            String hmLabelText = getLabelTextForField(FIELD_DD_HM);
            String mLabelText = getLabelTextForField(FIELD_DD_M);
            String smLabelText = getLabelTextForField(FIELD_DD_SM);
            editHMMSMControl.initLabels(hmLabelText, mLabelText, smLabelText);
        }
    }

    private String getLabelTextForField(String fieldName) {
        int index = editFields.getIndexOfTableAndFeldName(new EtkDisplayFieldKeyNormal(TABLE_DA_DIALOG, fieldName));
        EditControl controlByFeldIndex = editControls.getControlByFeldIndex(index);
        if (controlByFeldIndex != null) {
            return controlByFeldIndex.getLabel().getText();
        }
        return "";
    }

    private List<String> getEnumList(String enumKey, boolean forceBlankEntry) {
        List<String> rfmeTexts = new DwList<String>();
        EnumValue rfmeaEmum = getEtkDbs().getEnumValue(enumKey);
        if (rfmeaEmum != null) {
            for (EnumEntry enumEntry : rfmeaEmum.values()) {
                String enumText = enumEntry.getEnumText().getText(Language.DE.getCode());
                rfmeTexts.add(enumText);
            }
            Collections.sort(rfmeTexts);
            if (forceBlankEntry && !rfmeTexts.contains("")) {
                rfmeTexts.add(0, "");
            }
        }
        return rfmeTexts;
    }

    @Override
    protected boolean checkValues() {
        boolean result = super.checkValues();
        if (result) {
            if ((attributes != null) && (tableName != null)) {
                iPartsGuiCodeTextField codeTextField = getGuiCodeTextField();
                if (codeTextField != null) {
                    String productGroup = getAttributes().getField(FIELD_DD_PRODUCT_GRP).getAsString();
                    String ausfuehrungsArt = getAttributes().getField(FIELD_DD_AA).getAsString();
//                    codeTextField.init(getConnector(), getAttributes().getField(FIELD_DD_SERIES_NO).getAsString(),
//                                       productGroup, "", ausfuehrungsArt, iPartsGuiCodeTextField.CODE_TEST_TYPE.X4E_CODE);
                    result = iPartsEditUserControlsHelper.checkCodeFieldWithErrorMessage(codeTextField);
                }
                if (result) {
                    result = iPartsEditUserControlsHelper.checkMaterialFieldWithErrorMessage(getGuiMaterialSelectTextField());
                }
            }
        }
        return result;
    }

    @Override
    protected void collectEditValues() {
        super.collectEditValues();
        if (editHMMSMControl != null) {
            HmMSmId hmmsmId = editHMMSMControl.getHMMSMId();
            if (hmmsmId != null && hmmsmId.isValidId()) {
                DBDataObjectAttribute field = attributes.getField(FIELD_DD_HM, false);
                if (field != null) {
                    field.setValueAsString(hmmsmId.getHm(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                }
                field = attributes.getField(FIELD_DD_M, false);
                if (field != null) {
                    field.setValueAsString(hmmsmId.getM(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                }
                field = attributes.getField(FIELD_DD_SM, false);
                if (field != null) {
                    field.setValueAsString(hmmsmId.getSm(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                }
            }
        }
    }

    private iPartsGuiCodeTextField getGuiCodeTextField() {
        AbstractGuiControl guiCtrl = getEditGuiControlByFieldName(FIELD_DD_CODES);
        if ((guiCtrl != null) && (guiCtrl instanceof iPartsGuiCodeTextField)) {
            return (iPartsGuiCodeTextField)guiCtrl;
        }
        return null;
    }

    private iPartsGuiMaterialSelectTextField getGuiMaterialSelectTextField() {
        AbstractGuiControl guiCtrl = getEditGuiControlByFieldName(FIELD_DD_PARTNO);
        if ((guiCtrl != null) && (guiCtrl instanceof iPartsGuiMaterialSelectTextField)) {
            return (iPartsGuiMaterialSelectTextField)guiCtrl;
        }
        return null;
    }

    @Override
    protected boolean isFieldPKValueOrMandatory(EtkEditField field, List<String> pkFields) {
        String fieldName = field.getKey().getFieldName();
        if (isEmptyValueAllowedFields.contains(fieldName)) {
            return false;
        }
        return super.isFieldPKValueOrMandatory(field, pkFields);
    }

    @Override
    protected boolean checkCompletionOfFormValues() {
        return super.checkCompletionOfFormValues();
    }

    @Override
    protected boolean checkForModified() {
        return (editHMMSMControl != null && editHMMSMControl.isModified()) || super.checkForModified();
    }
}
