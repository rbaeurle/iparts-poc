/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.relatedinfo.main.connectors.RelatedInfoFormConnector;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsResponseDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiIdentTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiModelTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo.iPartsResponseDataForm;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiComboBoxMode;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * UserControl zum Bearbeiten von Rückmeldedaten (DAIMLER-4870, DAIMLER-5244)
 *
 * Regeln DIALOG:
 * Pflichtangaben: Werk, Baureihe, AA, Fahrzeugidentnummer
 * Optionale Felder: WHC, PEM, BR, Lenkung, Baumuster, Status, Quelle und Vorläufer/Nachzügler.
 * => aktuell hauptsächlich über Konfiguration in WB gelöst
 *
 * Regeln ELDAS:
 * Pflichtfelder: Ident, Baumuster-Art
 * Optionale Felder: Werk, PEM, Eldas-Typ, Status, Quelle
 * => bei ELDAS ist eine Bearbeitung nur bei iParts Datensätzen möglich, bei migrierten Daten kann nur der Status geändert werden
 */
public class EditUserControlsForResponseData extends EditUserControlForCreate implements iPartsConst {

    // diese Felder kommen entweder von den Werkseinsatzdaten oder werden automatisch gesetzt (ADAT) und dürfen deshalb
    // nicht bearbeitet werden ( hiermit wird die WB Konfig überschrieben )
    // Ausnahme: FIELD_DRD_AA darf nur bei Neuanlage editiert werden, wird dann im Konstruktor nachträglich wieder enabled
    private static final String[] readOnlyFieldNames = new String[]{ FIELD_DRD_FACTORY, FIELD_DRD_SERIES_NO, FIELD_DRD_AA, FIELD_DRD_PEM,
                                                                     FIELD_DRD_ADAT, FIELD_DRD_SOURCE, FIELD_DRD_AS_DATA, FIELD_DRD_STATUS };
    private static final String[] eldasReadOnlyFieldNames = new String[]{ FIELD_DRD_SERIES_NO, FIELD_DRD_AA, FIELD_DRD_PEM,
                                                                          FIELD_DRD_ADAT, FIELD_DRD_SOURCE, FIELD_DRD_AS_DATA, FIELD_DRD_STATUS };
    private static final String[] mustHaveValueFieldNames = new String[]{ FIELD_DRD_PEM, FIELD_DRD_IDENT };
    private static final String[] eldasMustFields = new String[]{ FIELD_DRD_IDENT, FIELD_DRD_TYPE };
    private static final String[] invisibleFieldNames = new String[]{ FIELD_DRD_BMAA, FIELD_DRD_ADAT, FIELD_DRD_TEXT, FIELD_DRD_VALID, FIELD_DRD_AS_DATA };
    private static final String[] eldasInvisibleFields = new String[]{ FIELD_DRD_SERIES_NO, FIELD_DRD_AA, FIELD_DRD_STEERING, FIELD_DRD_AS_DATA, FIELD_DRD_FACTORY };
    private static final String[] dialogInvisibleFields = new String[]{ FIELD_DRD_AGG_TYPE, FIELD_DRD_WHC, FIELD_DRD_TYPE, iPartsDataVirtualFieldsDefinition.DRD_RESPONSE_SPIKES_AVAILABLE };
    private static final String[] allowedEmptyPKFields = new String[]{ FIELD_DRD_SERIES_NO, FIELD_DRD_AA, FIELD_DRD_BMAA };


    public static iPartsDataResponseData showEditCreateResponseData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                                    iPartsResponseDataId externResponseDataId, String factory, boolean isNewForm,
                                                                    boolean isReadOnly, boolean isEldasPartlist, Set<String> allWMIs,
                                                                    String productAggregateType) {

        String tableName = TABLE_DA_RESPONSE_DATA;
        EtkProject project = dataConnector.getProject();

        iPartsDataResponseData responseData = null;
        if (externResponseDataId != null) {
            responseData = new iPartsDataResponseData(project, externResponseDataId);
            if (!responseData.existsInDB()) {
                responseData = null;
            }
        }
        String text;
        boolean useExistingData = false;
        if (responseData == null) {
            responseData = new iPartsDataResponseData(project, externResponseDataId);
            responseData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            isNewForm = true;
            text = "!!Rückmeldedaten erstellen";
        } else {
            useExistingData = responseData.getSource() == iPartsImportDataOrigin.IPARTS;
            text = "!!Rückmeldedaten bearbeiten";

        }

        if (isReadOnly) {
            text = "!!Rückmeldedaten anzeigen";
        }

        prepareForEdit(responseData, factory, isNewForm, productAggregateType);
        EtkEditFields editFields = modifyEditFields(project, tableName, factory, isEldasPartlist);
        EditUserControlsForResponseData eCtrl = new EditUserControlsForResponseData(dataConnector, parentForm, tableName,
                                                                                    responseData.getAsId(),
                                                                                    responseData.getAttributes(),
                                                                                    editFields, isNewForm, allWMIs);
        eCtrl.setMainTitle(text);
        eCtrl.setTitle(TranslationHandler.translate("!!PEM: %1", responseData.getAsId().getPem()));
        eCtrl.setReadOnly(isReadOnly);
        ModalResult modalResult = eCtrl.showModal();
        iPartsDataResponseData result = null;
        if (modalResult == ModalResult.OK) {
            if (useExistingData) {
                // Weil das EditControl nur auf den Attributes arbeitet und wir die Beziehung zur alten ID nicht verlieren wollen,
                // muss hier die ID aus den bearbeiteten Attributes heraus synchronisiert werden.
                responseData.updateIdFromPrimaryKeys();
                result = responseData;
            } else {
                result = new iPartsDataResponseData(project, responseData.getAsId());
                result.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
            }
        }
        return result;
    }

    private static EtkEditFields modifyEditFields(EtkProject project, String tableName, String factory, boolean isEldasPartlist) {
        String[] mustHaveValueFieldNamesForEdit = mustHaveValueFieldNames;
        String[] allowedEmptyPKFieldsForEdit = allowedEmptyPKFields;
        String[] invisibleFieldNamesForEdit = invisibleFieldNames;
        String[] readOnlyFieldNamesForEdit = readOnlyFieldNames;
        if (isEldasPartlist) {
            readOnlyFieldNamesForEdit = eldasReadOnlyFieldNames;
        }
        if (StrUtils.isValid(factory)) { // Werk muss befüllt sein
            mustHaveValueFieldNamesForEdit = StrUtils.mergeArrays(mustHaveValueFieldNamesForEdit, new String[]{ FIELD_DRD_FACTORY });
        } else { // Werk darf im Primärschlüssel fehlen
            allowedEmptyPKFieldsForEdit = StrUtils.mergeArrays(allowedEmptyPKFieldsForEdit, new String[]{ FIELD_DRD_FACTORY });
        }

        if (isEldasPartlist) {
            mustHaveValueFieldNamesForEdit = StrUtils.mergeArrays(mustHaveValueFieldNamesForEdit, eldasMustFields);
            invisibleFieldNamesForEdit = StrUtils.mergeArrays(invisibleFieldNamesForEdit, eldasInvisibleFields);
        } else {
            invisibleFieldNamesForEdit = StrUtils.mergeArrays(invisibleFieldNamesForEdit, dialogInvisibleFields);
        }

        EtkEditFields editFields = new EtkEditFields();
        // Konfiguration soll komplett über WB passieren, die unteren Funktionen sind nur noch als Platzhalter
        // vorhanden, falls die Konfiguration doch überschrieben werden soll
        editFields.load(project.getConfig(), iPartsResponseDataForm.CONFIG_KEY_RESPONSE_DATA_TOP + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            EtkEditFieldHelper.getEditFields(project, tableName, editFields, false);
        }

        // jetzt nochmal alle prüfen dass auch alle mustFields vorhanden und sichtbar sind
        for (String fieldName : mustHaveValueFieldNamesForEdit) {
            EtkEditField feldByName = editFields.getFeldByName(tableName, fieldName);
            if (feldByName == null) {
                feldByName = new EtkEditField(tableName, fieldName, false);
                editFields.addField(feldByName);
            }
        }

        return modifyEditFields(editFields, tableName, mustHaveValueFieldNamesForEdit, allowedEmptyPKFieldsForEdit, invisibleFieldNamesForEdit,
                                readOnlyFieldNamesForEdit);
    }


    private static void prepareForEdit(iPartsDataResponseData responseData, String factory, boolean isNew, String productAggregateType) {
        responseData.setFieldValue(FIELD_DRD_ADAT, DateUtils.toyyyyMMddHHmmss_Calendar(Calendar.getInstance()), DBActionOrigin.FROM_EDIT);
        if (!isNew && (responseData.getSource() == iPartsImportDataOrigin.IPARTS)) {
            return;
        }

        // Werk aus den Werkseinsatzdaten verwenden und AS-Flag setzen, damit erkannt werden kann, dass der Datensatz für After-Sales angelegt wurde
        responseData.setFieldValue(FIELD_DRD_FACTORY, factory, DBActionOrigin.FROM_EDIT);
        responseData.setFieldValueAsBoolean(FIELD_DRD_AS_DATA, true, DBActionOrigin.FROM_EDIT);

        responseData.setFieldValue(FIELD_DRD_SOURCE, iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);
        if (isNew) {
            responseData.setFieldValue(FIELD_DRD_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
            // productAggregateType wird nur für ELDAS gebraucht, bei DIALOG es leer
            responseData.setFieldValue(FIELD_DRD_TYPE, productAggregateType, DBActionOrigin.FROM_EDIT);
        }
    }


    private boolean isNewForm;
    private Set<String> allWMIs;

    protected EditUserControlsForResponseData(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                              IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                              boolean isNewForm, Set<String> allWMIs) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        this.isNewForm = isNewForm;
        this.allWMIs = allWMIs;
        // modifyEditControl muss nochmal aufgerufen werden, da erst jetzt isNewForm gesetzt ist
        // RCombobox für AA soll nur bei Anlage editierbar sein
        if (isNewForm) {
            EtkEditField editField = editFields.getFeldByName(TableAndFieldName.make(tableName, FIELD_DRD_AA), false);
            EditControl editControl = getEditControlByFieldName(FIELD_DRD_AA);
            if ((editControl != null) && (editField != null)) {
                this.modifyEditControl(editControl, editField, "", null);
            }
        }
        EtkEditField wmiField = editFields.getFeldByName(TableAndFieldName.make(tableName, FIELD_DRD_WHC), false);
        EditControl wmiControl = getEditControlByFieldName(FIELD_DRD_WHC);
        if ((wmiField != null) && (wmiControl != null)) {
            String initialWMI = attributes.getFieldValue(FIELD_DRD_WHC);
            modifyEditControl(wmiControl, wmiField, initialWMI, null);
        }
        setWindowName("responseDataEdit");
    }

    private iPartsDocumentationType findDocumentationType(AbstractJavaViewerFormIConnector connector) {
        if (connector instanceof RelatedInfoFormConnector) {
            RelatedInfoFormConnector relatedInfoFormConnector = (RelatedInfoFormConnector)connector;
            EtkDataPartListEntry partListEntry = relatedInfoFormConnector.getRelatedInfoData().getAsPartListEntry(getProject());
            if (partListEntry.getAsId().isValidId()) {
                return EditModuleHelper.getDocumentationTypeFromPartListEntry(partListEntry);
            }
        }
        if (connector.getOwnerConnector() != null) {
            return findDocumentationType(connector.getOwnerConnector());
        }
        return null;
    }

    private boolean checkValidEntries() {
        DBDataObjectAttributes currentAttributes = getCurrentAttributes();
        if (currentAttributes != null) {
            iPartsDocumentationType documentationType = findDocumentationType(getConnector());
            if (documentationType != null) {
                if (documentationType.isPKWDocumentationType()) {
                    // Pflichtangaben bei DIALOG:
                    // Werk, Baureihe, AA -> kommen von den Werkseinsatzdaten, daher nicht editierbar
                    // Fahrzeugidentnummer,
                    // Gültigkeit (doch kein Pflichtfeld (Review Sprint 45); ist vermutlich der neue Status)
                    // -> müssen hier geprüft werden
                    DBDataObjectAttribute field = currentAttributes.getField(FIELD_DRD_IDENT, false);
                    if ((field != null) && field.isEmpty()) {
                        return false;
                    }
                } else if (documentationType.isTruckDocumentationType()) {
                    // Pflichtangaben bei ELDAS sind über die Mussfelder abgedeckt
                    // beim WMI noch zusätzlich prüfen ob der ausgewählte Wert einer der zulässigen Werte ist
                    DBDataObjectAttribute field = currentAttributes.getField(FIELD_DRD_WHC, false);
                    if (field != null) {
                        String fieldValue = field.getAsString().toUpperCase();
                        // leere WMI Werte zulassen oder wenn nicht leer dann muss der WMI passen
                        if (!fieldValue.isEmpty() && ((allWMIs != null) && !allWMIs.isEmpty() && !allWMIs.contains(fieldValue))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private iPartsGuiModelTextField getModelTextField() {
        iPartsGuiModelTextField modelTextField = null;
        EditControl editControl = getEditControlByFieldName(FIELD_DRD_BMAA);
        if (editControl != null) {
            if (editControl.getAbstractGuiControl() instanceof iPartsGuiModelTextField) {
                modelTextField = (iPartsGuiModelTextField)editControl.getAbstractGuiControl();
            }
        }
        return modelTextField;
    }

    private iPartsGuiIdentTextField getIdentTextField() {
        iPartsGuiIdentTextField identTextField = null;
        EditControl editControl = getEditControlByFieldName(FIELD_DRD_IDENT);
        if (editControl != null) {
            if (editControl.getAbstractGuiControl() instanceof iPartsGuiIdentTextField) {
                identTextField = (iPartsGuiIdentTextField)editControl.getAbstractGuiControl();
            }
        }
        return identTextField;
    }

    private String getIdentTextDescription() {
        iPartsGuiIdentTextField identTextField = null;
        EditControl editControl = getEditControlByFieldName(FIELD_DRD_IDENT);
        if (editControl != null) {
            return editControl.getLabel().getText();
        }
        return TranslationHandler.translate("!!Fahrzeugidentnummer");
    }

    protected boolean isMandatoryAttributeValueEmpty(EtkEditField field, DBDataObjectAttribute attrib) {
        List<String> emptyPKFields = new DwList<>(allowedEmptyPKFields);
        if (emptyPKFields.contains(field.getKey().getFieldName())) {
            return false;
        }
        return super.isMandatoryAttributeValueEmpty(field, attrib);
    }


    /**
     * Überschreiben damit in diesem Dialog leere Primärschlüsselfelder zugelassen werden
     *
     * @return
     */
    @Override
    protected boolean checkValues() {
        boolean justWarningOrValid = super.checkValues();
        if (justWarningOrValid) {
            StringBuilder errorBuilder = new StringBuilder();
            String warning = "";
            iPartsGuiModelTextField modelField = getModelTextField();
            if (modelField != null) {
                if (!modelField.checkInput(getProject())) {
                    if (modelField.getErrorMessage() != null) {
                        errorBuilder.append(modelField.getErrorMessage());
                        justWarningOrValid = false;
                    } else if (modelField.getWarningMessage() != null) {
                        warning = modelField.getWarningMessage();
                        justWarningOrValid = true;
                    }
                }
            }

            iPartsGuiIdentTextField identField = getIdentTextField();
            if (identField != null) {
                if (!identField.isIdentValid()) {
                    if (errorBuilder.length() > 0) {
                        errorBuilder.append("\n");
                    }
                    errorBuilder.append(TranslationHandler.translate("Die eingegebene \"%1\" \"%2\" ist ungültig!",
                                                                     getIdentTextDescription(), identField.getText()));
                    justWarningOrValid = false;
                }
            }
            if (errorBuilder.length() > 0) {
                MessageDialog.showError(errorBuilder.toString());
            } else {
                if (!warning.isEmpty()) {
                    MessageDialog.showWarning(warning);
                }
            }
        }
        return justWarningOrValid;
    }

    protected boolean isFieldPKValueOrMandatory(EtkEditField field, List<String> pkFields) {
        iPartsDocumentationType documentationType = findDocumentationType(getConnector());
        if (documentationType != null) {
            if (documentationType.isTruckDocumentationType()) {
                return isMandatoryField(field);
            }
        }
        return super.isFieldPKValueOrMandatory(field, pkFields);
    }

    @Override
    protected void doEnableButtons(Event event) {
        boolean enabled = !checkMustFieldsHaveValues() && checkValidEntries();
        if (!isNewForm && enabled) {
            enabled = checkForModified();
        }
        enableOKButton(readOnly || enabled);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        if (readOnly) {
            // der Original Wert, falls er nicht dem Aggregate Enum entspricht, nur anzeigen wenn es sich um die ReadOnly Variante
            // handelt. Das wird gebraucht damit die migrierten Werten (AF, BF) angezeigt werden können.
            // Migrierte Daten können aber nicht bearbeitet werden, und sind damit immer ReadOnly
            int index = editFields.getIndexOfVisibleTableAndFeldName(new EtkDisplayFieldKeyNormal(tableName, FIELD_DRD_TYPE));
            if (index > 0) {
                EditControl editControl = editControls.getControlByFeldIndex(index);
                if ((editControl != null) && (editControl.getAbstractGuiControl() instanceof EnumRComboBox)) {
                    EnumRComboBox comboBox = (EnumRComboBox)editControl.getAbstractGuiControl();
                    String initialValue = attributes.getFieldValue(FIELD_DRD_TYPE);
                    if (StrUtils.isValid(initialValue) && !comboBox.getTokens().contains(initialValue)) {
                        comboBox.addToken(initialValue, initialValue);
                        comboBox.setSelectedItem(initialValue);
                    }
                }
            }
        }
    }

    private String getAA() {
        String aa = null;
        if (id instanceof iPartsResponseDataId) {
            aa = ((iPartsResponseDataId)id).getAusfuehrungsArt();
        }
        return aa;
    }

    private String getSeriesNo() {
        String seriesNo = null;
        if (id instanceof iPartsResponseDataId) {
            seriesNo = ((iPartsResponseDataId)id).getSeriesNo();
        }
        return seriesNo;
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {

        if (field.getKey().getName().equals(TableAndFieldName.make(tableName, FIELD_DRD_IDENT))) {
            iPartsGuiIdentTextField identField = new iPartsGuiIdentTextField();
            identField.setText(initialValue);
            identField.setSeriesNo(getSeriesNo());
            ctrl.getEditControl().setControl(identField);
        } else if (field.getKey().getName().equals(TableAndFieldName.make(tableName, FIELD_DRD_BMAA))) {
            iPartsGuiModelTextField modelField = new iPartsGuiModelTextField();
            modelField.setSeriesNo(getSeriesNo());
            ctrl.getEditControl().setControl(modelField);
        } else if (field.getKey().getName().equals(TableAndFieldName.make(tableName, FIELD_DRD_AA)) && isNewForm) {
            // Ausführungsart editierbar setzen, aber nur bei isNewForm
            ctrl.getEditControl().setReadOnly(false);
            field.setEditierbar(true);
            AbstractGuiControl control = ctrl.getEditControl().getControl();
            if (control instanceof EnumRComboBox) {
                EnumRComboBox enumRComboBox = ((EnumRComboBox)control);
                String aa = getAA();
                if (StrUtils.isValid(aa)) {
                    List<String> enumTexts = new DwList<>();
                    enumTexts.add(aa);
                    enumRComboBox.setEnumTexte(enumTexts);
                } else {
                    enumRComboBox.setEnumTexte(new DwList<>());
                }
            }
        } else if (field.getKey().getName().equals(TableAndFieldName.make(tableName, FIELD_DRD_WHC))) {
            if (allWMIs == null) { // erster Aufruf, hier nur das Control austauschen
                ctrl.getEditControl().setControl(new RComboBox<String>(GuiComboBoxMode.Mode.STANDARD));
            } else if (ctrl.getAbstractGuiControl() instanceof RComboBox) {
                String value = initialValue.toUpperCase();
                // zweiter Aufruf, hier wurden die WMIs berechnet und können eingetragen werden
                RComboBox<String> wmiCombobox = (RComboBox<String>)ctrl.getAbstractGuiControl();
                wmiCombobox.addItems(allWMIs);
                if (!allWMIs.contains(value) && StrUtils.isValid(initialValue)) {
                    // wenn es den initial Wert nicht in den möglichen WMIs gibt, dann wird er trotzdem übernommen
                    // der OK Button wird aber disabled und der Wert gekennzeichnet
                    value = value + " " + TranslationHandler.translate("!!<nicht gültig zum Werk>");
                    wmiCombobox.addItem(value);
                }
                wmiCombobox.setSelectedItem(value);
            }
        }
    }

}