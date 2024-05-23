/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModule;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.List;

/**
 * UserControl für die Neuanlage einer freien SA
 */
public class EditUserControlForCreateSA extends EditUserControlForCreate implements iPartsConst {

    private static final String[] readOnlyFieldNames = new String[]{ FIELD_DM_MODULE_NO };
    private static final String[] mustHaveValueFieldNames = new String[]{ FIELD_DM_DOCUTYPE };
    private static final String[] invisibleFieldNamesForCreate = new String[]{};
    private static final String[] invisibleFieldNamesForView = new String[]{};

    public static iPartsDataModule showCreateSA(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                iPartsModuleId saId, AssemblyId assemblyId, String saNumber) {
        String tableName = TABLE_DA_MODULE;
        EtkProject project = dataConnector.getProject();
        EtkEditFields editFields = modifyEditFields(project, tableName, false);

        iPartsDataModule dataModule = new iPartsDataModule(project, saId);
        if (dataModule.existsInDB()) {
            MessageDialog.showError(TranslationHandler.translate("!!Modul \"%1\" existiert bereits in der Datenbank", saId.getModuleNumber()));
            return null;
        }
        // so früh wie möglich den Primärschlüssel reservieren und falls das nicht möglich ist abbrechen
        boolean reservationSuccessful = iPartsDataReservedPKList.reservePrimaryKey(project, assemblyId);
        if (!reservationSuccessful) {
            MessageDialog.showError(TranslationHandler.translate("!!Modul \"%1\" wurde bereits in einem anderen Autorenauftrag angelegt",
                                                                 saId.getModuleNumber()));
            return null;
        }

        dataModule.initWithDefaultValues();
        EditUserControlForCreateSA eCtrl = new EditUserControlForCreateSA(dataConnector, parentForm, tableName, saId,
                                                                          dataModule.getAttributes(), editFields);

        eCtrl.setMainTitle("!!Freie SA anlegen");
        String formattedNumber = iPartsNumberHelper.formatPartNo(project, saNumber);
        eCtrl.setTitle(TranslationHandler.translate("!!SA: \"%1\"", formattedNumber));

        ModalResult result = eCtrl.showModal();
        if (result == ModalResult.OK) {
            dataModule.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
            return dataModule;
        }
        // Bei Abbruch die Primärschlüssel Reservierung wieder löschen
        iPartsDataReservedPKList.deleteReservedPrimaryKey(project, assemblyId);
        return null;
    }

    private static EtkEditFields modifyEditFields(EtkProject project, String tableName, boolean isCreate, String... extraReadOnlyFieldNames) {
        // alle Felder der Tabelle DA_MODULE anzeigen
        EtkEditFields editFields = new EtkEditFields();
        EtkEditFieldHelper.getDescriptionAsEditFields(project, iPartsConst.TABLE_DA_MODULE, editFields);
        // überprüfe die Edit-Konfig
        editFields = modifyEditFields(editFields, tableName, mustHaveValueFieldNames, null,
                                      isCreate ? invisibleFieldNamesForCreate : invisibleFieldNamesForView,
                                      readOnlyFieldNames, extraReadOnlyFieldNames);
        return editFields;
    }

    public EditUserControlForCreateSA(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                      IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        setWindowName("userControlForCreateSA");
    }

    @Override
    protected void doEnableButtons(Event event) {
        List<String> warnings = new DwList<String>();
        boolean isOK = checkData(warnings);
        String hint = "";
        if (!isOK) {
            hint = TranslationHandler.translate("!!Fehler:");
            for (String msg : warnings) {
                hint += "\n";
                hint += TranslationHandler.translate(msg);
            }
        }

        setOKButtonTooltip(hint);
        enableOKButton(isOK);
    }

    /**
     * Überprüfung der eingegebenen Daten
     *
     * @param warnings
     * @return
     */
    private boolean checkData(List<String> warnings) {
        iPartsDocumentationType docuType = getDocuType();
        if (docuType == iPartsDocumentationType.UNKNOWN) {
            if (warnings != null) {
                warnings.add("!!Dokumentationstyp darf nicht leer sein.");
            }
            return false;
        }
        return true;
    }

    private iPartsDocumentationType getDocuType() {
        EditControl editControlByFieldName = getEditControlByFieldName(iPartsConst.FIELD_DM_DOCUTYPE);
        if (editControlByFieldName != null) {
            String docuType = editControlByFieldName.getText();
            if (StrUtils.isValid(docuType)) {
                return iPartsDocumentationType.getFromDBValue(docuType);
            }
        }
        return iPartsDocumentationType.UNKNOWN;
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        String fieldName = field.getKey().getFieldName();
        if (fieldName.equals(FIELD_DM_DOCUTYPE)) {
            iPartsEditUserControlsHelper.modifyDocuRelevantControlForSA(ctrl);
        } else {
            super.modifyEditControl(ctrl, field, initialValue, initialDataArray);
        }
    }
}