/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiEventSelectComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsRelatedInfoVariantsToPartDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiMultiLangEdit;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * UserControl zum Bearbeiten von Variantentabellen (DAIMLER-1248)
 * <p>
 * Pflichtangaben: Position, Farbvariante (bei der Eintragung der Variante, muss geprüft werden ob diese bereits exisitert)
 * Optionale Felder: Codebedingung-AS, Event-ab, Event-bis, PEM-ab, PEM-bis
 * <p>
 * Das Feld Codebedingung ist bei der Neuanlage einer Position gesperrt. Das Feld Benennung wird auf Basis der eingetragenen Variante automatisch befüllt.
 */
public class EditUserControlsForVariants extends EditUserControlForCreate implements iPartsConst {

    private static final String[] READ_ONLY_FIELD_NAMES = new String[]{ TableAndFieldName.make(TABLE_DA_COLOR_NUMBER, FIELD_DCN_DESC),
                                                                        TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_CODE),
                                                                        TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_SOURCE),
                                                                        TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_FROM),
                                                                        TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_EVENT_TO),
                                                                        TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_STATUS) };
    private static final String[] MUST_HAVE_VALUE_FIELD_NAMES = new String[]{ TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_POS),
                                                                              TableAndFieldName.make(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_COLOR_VAR) };
    private static final String[] INVISIBLE_FIELD_NAMES = new String[]{};
    private static final String[] ALLOWED_EMPTY_PK_FIELDS = new String[]{};
    // Felder mit AS Informationen, die editiert werden dürfen. Alle anderen Informationen dürfen wir nicht editieren, da sie aus Fremdquellen stammen
    private static final List<String> FIELDS_FOR_AS_EDIT = new ArrayList<>();

    static {
        FIELDS_FOR_AS_EDIT.add(FIELD_DCTC_CODE_AS);
        FIELDS_FOR_AS_EDIT.add(FIELD_DCTC_EVENT_FROM_AS);
        FIELDS_FOR_AS_EDIT.add(FIELD_DCTC_EVENT_TO_AS);
        //DAIMLER-7795 Bearbeitung PEMab/PEMbis-Auswerteflags erlaubt
        FIELDS_FOR_AS_EDIT.add(FIELD_DCTC_EVAL_PEM_FROM);
        FIELDS_FOR_AS_EDIT.add(FIELD_DCTC_EVAL_PEM_TO);
    }

    public static iPartsDataColorTableContent editVariantsData(RelatedInfoBaseFormIConnector dataConnector,
                                                               iPartsDocumentationType documentationType, AbstractJavaViewerForm parentForm,
                                                               iPartsDataColorTableContent dataColorTableContent, boolean isReadOnly) {
        String seriesNo = ColorTableHelper.extractSeriesNumberFromTableId(dataColorTableContent.getAsId().getColorTableId());
        EtkEditFields editFields = modifyEditFields(dataConnector.getProject(), TABLE_DA_COLORTABLE_CONTENT, true);
        EditUserControlsForVariants eCtrl = new EditUserControlsForVariants(dataConnector, parentForm, TABLE_DA_COLORTABLE_CONTENT,
                                                                            dataColorTableContent.getAsId(),
                                                                            dataColorTableContent.getAttributes(),
                                                                            editFields, seriesNo, documentationType, true);
        eCtrl.setReadOnly(isReadOnly);
        if (isReadOnly) {
            eCtrl.setMainTitle("!!Variante zu Variantentabelle anzeigen");
        } else {
            eCtrl.setMainTitle("!!Variante zu Variantentabelle editieren");
        }
        eCtrl.setTitle(TranslationHandler.translate("!!Variantentabelle: %1", dataColorTableContent.getAsId().getColorTableId()));

        ModalResult modalResult = eCtrl.showModal();
        if (modalResult == ModalResult.OK) {
            // Es dürfen nur AS Felder editiert werden, daher setze auch nur die AS Felder
            for (String asEditField : FIELDS_FOR_AS_EDIT) {
                String valueFromControl = eCtrl.getAttributes().getFieldValue(asEditField);
                dataColorTableContent.setFieldValue(asEditField, valueFromControl, DBActionOrigin.FROM_EDIT);
            }
            return dataColorTableContent;
        }
        return null;
    }

    public static iPartsDataColorTableContent showCreateVariantsData(RelatedInfoBaseFormIConnector dataConnector,
                                                                     iPartsDocumentationType documentationType,
                                                                     AbstractJavaViewerForm parentForm,
                                                                     iPartsColorTableToPartId colorTableToPartId, String productGroup) {
        EtkProject project = dataConnector.getProject();

        String pos = ""; // pos muss vom Benutzer angegeben werden
        String sdata = ""; // aktueller Zeitstempel?
        iPartsColorTableContentId colorTableContentId = new iPartsColorTableContentId(colorTableToPartId.getColorTableId(), pos, sdata);
        iPartsDataColorTableContent colorTableContent = new iPartsDataColorTableContent(project, colorTableContentId);

        colorTableContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

        colorTableContent.setFieldValue(FIELD_DCTC_SOURCE, iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);
        colorTableContent.setFieldValue(FIELD_DCTC_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
        colorTableContent.setFieldValue(FIELD_DCTC_SDATA, DateUtils.toyyyyMMddHHmmss_Calendar(Calendar.getInstance()), DBActionOrigin.FROM_EDIT);

        // Nur bei neu angelegten Farbvarianten darf DCTC_CODE_AS mit ";" initialisiert werden!
        colorTableContent.setFieldValue(FIELD_DCTC_CODE_AS, DaimlerCodes.beautifyCodeString(""), DBActionOrigin.FROM_EDIT);

        if (StrUtils.isValid(productGroup)) {
            colorTableContent.setFieldValue(FIELD_DCTC_PGRP, productGroup, DBActionOrigin.FROM_EDIT);
        }

        String tableName = TABLE_DA_COLORTABLE_CONTENT;
        EtkEditFields editFields = modifyEditFields(project, tableName, false);

        // Hier die Baureihe der gewählten Farbtabelle ermitteln, damit deren Codes verglichen werden können.
        String seriesNo = ColorTableHelper.extractSeriesNumberFromTableId(colorTableToPartId.getColorTableId());
        EditUserControlsForVariants eCtrl = new EditUserControlsForVariants(dataConnector, parentForm, tableName,
                                                                            colorTableContent.getAsId(),
                                                                            colorTableContent.getAttributes(),
                                                                            editFields, seriesNo, documentationType, false);
        eCtrl.setMainTitle("!!Variante zu Variantentabelle anlegen");
        eCtrl.setTitle(TranslationHandler.translate("!!Variantentabelle: %1", colorTableToPartId.getColorTableId()));

        ModalResult modalResult = eCtrl.showModal();
        if (modalResult == ModalResult.OK) {
            colorTableContent.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
            // positions nummern auf 5-stelligen Nummernkreis erweitern damit die Werte nicht mit Importern kollidieren können
            String posValue = colorTableContent.getFieldValue(FIELD_DCTC_POS);
            if (StrUtils.isValid(posValue)) {
                colorTableContent.setFieldValue(FIELD_DCTC_POS, iPartsGuiConstPosTextField.makeIPartsCreatedValue(posValue),
                                                DBActionOrigin.FROM_EDIT);
            }
            colorTableContent.updateIdFromPrimaryKeys();
            return colorTableContent;
        }
        return null;
    }

    private static EtkEditFields modifyEditFields(EtkProject project, String tableName, boolean isForASEdit, String... extraReadOnlyFieldNames) {
        EtkEditFields editFields = modifyEditFieldsTableAndFieldname(project, iPartsRelatedInfoVariantsToPartDataForm.CONFIG_KEY_COLOR_VARIANTS_DATA,
                                                                     tableName, MUST_HAVE_VALUE_FIELD_NAMES, ALLOWED_EMPTY_PK_FIELDS,
                                                                     INVISIBLE_FIELD_NAMES, READ_ONLY_FIELD_NAMES, extraReadOnlyFieldNames);
        EtkEditField editFieldColorNumber = editFields.getFeldByName(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_COLOR_VAR);
        if (editFieldColorNumber != null) {
            if (editFields.getFeldByName(TABLE_DA_COLOR_NUMBER, FIELD_DCN_DESC) == null) {
                int indexForDesc = editFields.getIndexOfTableAndFeldName(editFieldColorNumber.getKey());
                editFields.addFeld((indexForDesc + 1), new EtkEditField(TABLE_DA_COLOR_NUMBER, FIELD_DCN_DESC, true));

            }
        }

        if (isForASEdit) {
            for (EtkEditField editField : editFields.getVisibleEditFields()) {
                if (!FIELDS_FOR_AS_EDIT.contains(editField.getKey().getFieldName())) {
                    editField.setEditierbar(false);
                }
            }
        }

        return editFields;
    }

    private Map<String, Object> specialFields;
    private boolean colorNumberValid;
    private String seriesNo;    // Die Baureihe zur Farbtabelle, deren Variante bearbeitet wird
    private boolean isForASEdit;
    private iPartsDocumentationType documentationType;

    private EditUserControlsForVariants(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                        String tableName, iPartsColorTableContentId id, DBDataObjectAttributes attributes,
                                        EtkEditFields externalEditFields, String seriesNo,
                                        iPartsDocumentationType documentationType, boolean isForASEdit) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        setWindowName("variantsDataEdit");
        this.colorNumberValid = false;
        this.seriesNo = seriesNo;
        this.documentationType = documentationType;
        setIsForASEdit(isForASEdit);

        // Die Benennung der Farbvariante im EditControl füllen
        if (attributes.fieldExists(FIELD_DCTC_COLOR_VAR)) {
            fillVariantDescriptionControlByColorNumber(attributes.getFieldValue(FIELD_DCTC_COLOR_VAR));
        }
    }

    private void setIsForASEdit(boolean isForASEdit) {
        this.isForASEdit = isForASEdit;
        doEnableButtons(null);
    }

    /**
     * Überschreiben damit in diesem Dialog leere Primärschlüsselfelder zugelassen werden
     *
     * @return (true / false)
     */
    @Override
    protected boolean checkValues() {
        boolean result = super.checkValues();
        if (result) {
            if ((attributes != null) && (tableName != null)) {
                int index = editFields.getIndexOfVisibleTableAndFeldName(new EtkDisplayFieldKeyNormal(TABLE_DA_COLORTABLE_CONTENT, FIELD_DCTC_CODE_AS));
                iPartsGuiCodeTextField codeTextField = iPartsEditUserControlsHelper.getCodeFieldFromEditControls(index, editControls);
                if (codeTextField != null) {
                    String productGroup = attributes.getField(FIELD_DCTC_PGRP).getAsString();
                    codeTextField.init(getProject(), documentationType, seriesNo, productGroup, "", "", iPartsGuiCodeTextField.CODE_TEST_TYPE.PRODUCTGRP_ONLY);
                    result = iPartsEditUserControlsHelper.checkCodeFieldWithErrorMessage(codeTextField);
                }
            }
        }
        return result;
    }

    @Override
    protected void modifyEditControl(final EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        if (specialFields == null) {
            specialFields = new LinkedHashMap<String, Object>();
            specialFields.put(FIELD_DCTC_COLOR_VAR, null);
            specialFields.put(FIELD_DCN_DESC, null);
            specialFields.put(FIELD_DCTC_CODE_AS, null);
        }
        String fieldName = field.getKey().getFieldName();
        if (specialFields.containsKey(fieldName)) {
            if (fieldName.equals(FIELD_DCN_DESC)) {
                specialFields.put(FIELD_DCN_DESC, ctrl);
            } else if (fieldName.equals(FIELD_DCTC_COLOR_VAR)) {
                if (ctrl.getEditControl().getControl() instanceof GuiTextField) {
                    ((GuiTextField)ctrl.getEditControl().getControl()).setCaseMode(GuiTextField.CaseMode.UPPERCASE);
                }
                // prüfen ob das Feld für die Benennung auch als sichtbar konfiguriert ist
                final EtkEditField descrField = externalEditFields.getFeldByName(TABLE_DA_COLOR_NUMBER, FIELD_DCN_DESC, false);
                if (descrField != null) {
                    ctrl.getEditControl().getControl().addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                        @Override
                        public void fire(Event event) {
                            fillVariantDescriptionControlByColorNumber(ctrl.getEditControl().getText());
                        }
                    });
                }
            } else if (fieldName.equals(FIELD_DCTC_CODE_AS)) {
                // Bei der AS-Codebedingung die Einstellung zur Klein-/Großschreibung aus der WB übernehmen.
                if (ctrl.getEditControl().getControl() instanceof iPartsGuiCodeTextField) {
                    iPartsGuiCodeTextField codeTextField = (iPartsGuiCodeTextField)ctrl.getEditControl().getControl();
                    codeTextField.setEditCaseMode(field.getEditCaseMode());
                    codeTextField.setText(initialValue);
                }
            }
        } else if (fieldName.equals(FIELD_DCTC_EVENT_FROM_AS) || fieldName.equals(FIELD_DCTC_EVENT_TO_AS)) {
            // Event-AS-Attribute
            String tableId = attributes.getFieldValue(FIELD_DCTC_TABLE_ID);
            if (StrUtils.isValid(tableId)) {
                String seriesNo = ColorTableHelper.extractSeriesNumberFromTableId(tableId);
                // Hier muss die Series separat extrahiert werden, da die Klassenvariable erst später besetzt wird
                iPartsSeriesId seriesId = new iPartsSeriesId(seriesNo);
                if (iPartsDialogSeries.getInstance(getProject(), seriesId).isEventTriggered()) {
                    if (ctrl.getEditControl().getControl() instanceof iPartsGuiEventSelectComboBox) {
                        String eventId;
                        if (fieldName.equals(FIELD_DCTC_EVENT_FROM_AS)) {
                            eventId = attributes.getFieldValue(FIELD_DCTC_EVENT_FROM_AS);
                        } else {
                            eventId = attributes.getFieldValue(FIELD_DCTC_EVENT_TO_AS);
                        }
                        iPartsGuiEventSelectComboBox eventSelectComboBox = (iPartsGuiEventSelectComboBox)ctrl.getEditControl().getControl();
                        eventSelectComboBox.initForColorEvents(getProject(), seriesId, eventId);
                    }
                }
            }
        } else if (fieldName.equals(FIELD_DCTC_POS)) {
            if (!iPartsGuiConstPosTextField.isIPartsCreatedValue(initialValue)) {
                iPartsGuiConstPosTextField posTextField = new iPartsGuiConstPosTextField();
                posTextField.setText(initialValue);
                ctrl.getEditControl().setControl(posTextField);
            }
        }
    }

    private void fillVariantDescriptionControlByColorNumber(String colorNumber) {
        // Gui Control für die Beschreibung holen
        Object descrControl = specialFields.get(FIELD_DCN_DESC);
        if (descrControl instanceof EditControl) {
            AbstractGuiControl control = ((EditControl)descrControl).getEditControl().getControl();
            if (control instanceof GuiMultiLangEdit) {
                GuiMultiLangEdit decriptionControl = (GuiMultiLangEdit)control;
                // Beschreibung zur Farbnummer aus der DB lesen und im entsprechenden Feld anzeigen
                EtkProject project = getProject();
                iPartsColorNumberId requestColorId = new iPartsColorNumberId(colorNumber);
                iPartsDataColorNumber requestColorNumber = new iPartsDataColorNumber(project, requestColorId);
                if (requestColorNumber.existsInDB()) {
                    colorNumberValid = true;
                    EtkMultiSprache colorDesc = requestColorNumber.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DCN_DESC);
                    decriptionControl.setMultiLanguage(colorDesc);
                } else {
                    colorNumberValid = false;
                    decriptionControl.setMultiLanguage(new EtkMultiSprache());
                }
                doEnableButtons(null);
            }
        }
    }

    @Override
    protected void doEnableButtons(Event event) {
        if (readOnly || isForASEdit) {
            // Wenn nur die AS-Felder editiert werden, dann gibt es aktuell nur die Prüfung für Code und diese wird in checkValues() gemacht
            enableOKButton(readOnly || checkForModified());
            setOKButtonTooltip("");
        } else {
            boolean enabled = !checkMustFieldsHaveValues() && checkValidEntries();
            enableOKButton(enabled);
            String toolTip = "";
            if (!enabled) {
                toolTip = calculateToolTip();
            }
            setOKButtonTooltip(toolTip);
        }
    }

    private String calculateToolTip() {
        int index = 0;
        if (attributes != null) {
            String result = "";
            for (EtkEditField field : editFields.getVisibleEditFields()) {
                if (field.isMussFeld()) {
                    DBDataObjectAttribute attrib = getCurrentAttribByEditControlValue(index, field);
                    if (isMandatoryAttributeValueEmpty(field, attrib)) { // hier keine Logausgabe machen, wenn attrib null ist, weil das in collectEditValues() bereits gemacht wird
                        result = addToolTipText(result, "!!Feld \"%1\" hat keinen gültigen Wert", field.getText().getText(getProject().getViewerLanguage()));
                    } else {
                        if (field.getKey().getFieldName().equals(FIELD_DCTC_COLOR_VAR)) {
                            if (!colorNumberValid) {
                                result = addToolTipText(result, "!!Feld \"%1\" hat einen ungültigen Wert: \"%2\"",
                                                        field.getText().getText(getProject().getViewerLanguage()), attrib.getAsString());
                            }
                        }
                    }
                }
                index++;
            }
            return result;
        }
        return "";
    }

    private String addToolTipText(String toolTip, String key, String... placeHolderTexts) {
        if (!toolTip.isEmpty()) {
            toolTip += "\n";
        }
        toolTip += TranslationHandler.translate(key, placeHolderTexts);
        return toolTip;
    }

    private boolean checkValidEntries() {
        // zusätzlich zu den MUST_HAVE_VALUE_FIELD_NAMES muss hier nur auf existierende Farbnummer geprüft werden
        // das passiert beim onChange callback für das zugehörige EditField
        return colorNumberValid;
    }
}