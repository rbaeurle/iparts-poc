package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiTextFieldBackgroundToggle;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.AbstractVerticalAlignmentControl;
import de.docware.framework.modules.gui.controls.GuiMultiLangEdit;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.HashSet;
import java.util.Set;

public class EditUserControlForCreateFootnote extends EditUserControls {

    private static final String TOOLTIP_EMPTY = "!!Fußnoten-Nummer darf nicht leer sein.";
    private static final String TOOLTIP_WRONG_LENGTH = "!!Fußnoten-Nummer muss entweder 3 oder 6 Zeichen lang sein und darf nur aus Ziffern bestehen.";
    private static final String TOOLTIP_EXISTS = "!!Fußnoten-Nummer ist bereits vergeben.";

    public static iPartsDataFootNoteContent showEditUserControlsForCreateFootnote(AbstractJavaViewerFormIConnector dataConnector,
                                                                                  AbstractJavaViewerForm parentForm, iPartsDataDictMeta dataDictMeta) {
        String tableName = TABLE_DA_FN_CONTENT;

        EtkEditFields editFields = new EtkEditFields();
        EtkEditField editFieldFnId = new EtkEditField(TABLE_DA_FN_CONTENT, FIELD_DFNC_FNID, false);
        editFieldFnId.setDefaultText(false);
        editFieldFnId.setText(new EtkMultiSprache("!!Fußnoten-Nummer", dataConnector.getProject().getConfig().getDatabaseLanguages()));
        editFieldFnId.setMussFeld(true);
        editFields.addFeld(editFieldFnId);

        EtkEditField editFieldText = new EtkEditField(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT, true);
        editFieldText.setEditierbar(false);
        editFieldText.setDefaultText(false);
        editFieldText.setText(new EtkMultiSprache("!!Fußnoten-Text", dataConnector.getProject().getConfig().getDatabaseLanguages()));
        editFields.addFeld(editFieldText);

        iPartsFootNoteContentId fnContentId = new iPartsFootNoteContentId();
        iPartsDataFootNoteContent fnContent = new iPartsDataFootNoteContent(dataConnector.getProject(), fnContentId);
        fnContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        //DFNC_LINE_NO = '00001'
        fnContent.setFieldValue(FIELD_DFNC_LINE_NO, "00001", DBActionOrigin.FROM_EDIT);
        DBDataObjectAttribute footNoteTextAttribute = fnContent.getAttribute(FIELD_DFNC_TEXT, false);
        String textId = dataDictMeta.getTextId();
        footNoteTextAttribute.setTextIdForMultiLanguage(textId, textId, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);

        EditUserControlForCreateFootnote eCtrl = new EditUserControlForCreateFootnote(dataConnector, parentForm, tableName, fnContentId,
                                                                                      fnContent.getAttributes(), editFields);

        eCtrl.setMainTitle("!!Neue DIALOG-Fußnote anlegen");
        eCtrl.setTitle("!!Bitte Fußnoten-Nummer angeben");
        eCtrl.setWidth(800);
        ModalResult result = eCtrl.showModal();
        if (result == ModalResult.OK) {
            fnContent.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
            fnContent.updateIdFromPrimaryKeys();
            return fnContent;
        }
        return null;
    }

    private Set<String> allFnIds;
    private iPartsGuiTextFieldBackgroundToggle footnoteIdTextfield;

    public EditUserControlForCreateFootnote(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        allFnIds = null;
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        if (field.getKey().getFieldName().equals(FIELD_DFNC_FNID)) {
            footnoteIdTextfield = new iPartsGuiTextFieldBackgroundToggle();
            footnoteIdTextfield.setValidator(new GuiControlValidator() {
                @Override
                public ValidationState validate(AbstractGuiControl control) {
                    String text = control.getText();
                    String tooltip;
                    if (StrUtils.isValid(text)) {
                        if (((text.length() == 3) || (text.length() == 6)) && StrUtils.isDigit(text)) {
                            if (!getAllFnIds().contains(text)) {
                                return new ValidationState(true);
                            } else {
                                tooltip = TOOLTIP_EXISTS;
                            }
                        } else {
                            tooltip = TOOLTIP_WRONG_LENGTH;
                        }
                    } else {
                        tooltip = TOOLTIP_EMPTY;
                    }
                    return new ValidationState(false, null, tooltip);
                }
            });

            ctrl.getEditControl().setControl(footnoteIdTextfield);
            return;
        } else if (field.getKey().getFieldName().equals(FIELD_DFNC_TEXT)) {
            if (ctrl.getEditControl().getControl() instanceof GuiMultiLangEdit) {
                ((GuiMultiLangEdit)ctrl.getEditControl().getControl()).setMultiLine(true);
                ctrl.getLabel().setVerticalAlignment(AbstractVerticalAlignmentControl.VerticalAlignment.TOP);
                return;
            }
        }
        super.modifyEditControl(ctrl, field, initialValue, initialDataArray);
    }

    @Override
    protected void enableOKButton(boolean enabled) {
        if (footnoteIdTextfield != null) {
            if (footnoteIdTextfield.getValidationState().isValid()) {
                setValidationTooltip("");
                enabled = true;
            } else {
                setValidationTooltip(footnoteIdTextfield.getValidationState().getMessage());
                enabled = false;
            }
        }
        super.enableOKButton(enabled);
    }

    private void setValidationTooltip(String tooltip) {
        if (footnoteIdTextfield != null) {
            footnoteIdTextfield.setTooltip(tooltip);
        }
        setOKButtonTooltip(tooltip);
    }

    private Set<String> getAllFnIds() {
        if (allFnIds == null) {
            allFnIds = new HashSet<>();
            iPartsDataFootNoteList fnList = iPartsDataFootNoteList.loadFootNoteOverviewList(getProject());
            for (iPartsDataFootNote dataFootNote : fnList) {
                allFnIds.add(dataFootNote.getFieldValue(FIELD_DFN_NAME));
            }
        }
        return allFnIds;
    }
}
