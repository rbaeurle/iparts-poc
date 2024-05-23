/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.*;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * UserControl für Autoren-Auftrag
 */
public class EditUserControlForAuthorOrder extends EditUserControlForCreate implements iPartsConst {

    private static final String[] readOnlyFieldNames = new String[]{ FIELD_DAO_GUID, FIELD_DAO_STATUS, FIELD_DAO_CREATION_DATE,
                                                                     FIELD_DAO_CREATION_USER_ID, FIELD_DAO_CREATOR_GRP_ID,
                                                                     FIELD_DAO_CURRENT_USER_ID, FIELD_DAO_CURRENT_GRP_ID,
                                                                     FIELD_DAO_CHANGE_SET_ID, FIELD_DAO_BST_SUPPLIED, FIELD_DAO_BST_ERROR,
                                                                     FIELD_DAO_RELDATE };
    private static final String[] mustHaveValueFieldNames = new String[]{ FIELD_DAO_NAME, FIELD_DAO_BST_ID };
    private static final String[] invisibleFieldNamesForCreate = new String[]{ FIELD_DAO_CURRENT_USER_ID, FIELD_DAO_CURRENT_GRP_ID,
                                                                               FIELD_DAO_BST_SUPPLIED, FIELD_DAO_BST_ERROR, FIELD_DAO_RELDATE };

    private static final String BUTTON_TEXT_OK_ACTIVE = "!!OK und Aktivieren";

    public static iPartsDataAuthorOrder showPartialAuthorOrderForUpdatePSKModule(AbstractJavaViewerFormIConnector dataConnector,
                                                                                 AbstractJavaViewerForm parentForm, boolean isMultiNode) {
        EtkProject project = dataConnector.getProject();
        EtkEditFields editFields = new EtkEditFields();
        iPartsAuthorOrderId aoId = new iPartsAuthorOrderId(StrUtils.makeGUID());

        editFields.addField(new EtkEditField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_BST_ID, false));

        DBDataObjectAttributes attributes = new DBDataObjectAttributes();
        attributes.addField(FIELD_DAO_BST_ID, "", DBActionOrigin.FROM_DB);
        attributes.addField(FIELD_DAO_GUID, aoId.getAuthorGuid(), DBActionOrigin.FROM_DB);

        EditUserControlForAuthorOrder eCtrl = new EditUserControlForAuthorOrder(dataConnector, parentForm, TABLE_DA_AUTHOR_ORDER,
                                                                                aoId, attributes, editFields, false);
        setTitleForUpdatePSKModule(eCtrl, isMultiNode, project);
        ModalResult result = eCtrl.showModal();
        if (result == ModalResult.OK) {
            iPartsDataAuthorOrder aoData = new iPartsDataAuthorOrder(project, aoId);
            aoData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            aoData.setStatus(iPartsAuthorOrderStatus.CREATED, DBActionOrigin.FROM_EDIT);
            aoData.setCurrentCreationDate(DBActionOrigin.FROM_EDIT);
            aoData.setCreationUser(iPartsDataAuthorOrder.getLoginAcronym(), DBActionOrigin.FROM_EDIT);

            // Virtuelle Benutzergruppe für Autoren der Organisation des eingeloggten Benutzers bestimmen
            String loginUserId = iPartsUserAdminDb.getLoginUserIdForSession();
            if (loginUserId != null) {
                String orgId = iPartsUserAdminCache.getInstance(loginUserId).getOrgId();
                if (StrUtils.isValid(orgId)) {
                    iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(orgId);
                    if (orgCache.hasVirtualUserGroups()) {
                        aoData.setCreationUserGroupId(orgCache.getVirtualUserGroupIdForAuthors(), DBActionOrigin.FROM_EDIT);
                    }
                }
            }
            aoData.setBstId(eCtrl.getAttributes().getFieldValue(FIELD_DAO_BST_ID), DBActionOrigin.FROM_EDIT);
            return aoData;
        }
        return null;
    }

    private static void setTitleForUpdatePSKModule(EditUserControlForAuthorOrder eCtrl, boolean isMultiNode, EtkProject project) {
        String fieldName;
        EtkDatabaseField dbField = project.getConfig().findField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_BST_ID);
        if (dbField != null) {
            fieldName = dbField.getDisplayText(project.getViewerLanguage(), project.getDataBaseFallbackLanguages());
        } else {
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_BST_ID, false, false);
            displayField.loadStandards(project.getConfig());
            fieldName = displayField.getText().getTextByNearestLanguage(project.getViewerLanguage(), project.getDataBaseFallbackLanguages());
        }
        String windowTitle = isMultiNode ? EditModuleForm.IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT_MULTI
                                         : EditModuleForm.IPARTS_MENU_ITEM_UPDATE_PSK_MODULE_TEXT;
        eCtrl.setMainTitle(windowTitle);
        eCtrl.setTitle(TranslationHandler.translate("!!Zum Abgleich %1 auswählen", fieldName));
    }

    public static iPartsDataAuthorOrder showCreateAuthorOrder(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                              VarParam<Boolean> setActive) {
        String tableName = TABLE_DA_AUTHOR_ORDER;
        EtkProject project = dataConnector.getProject();
        EtkEditFields editFields = modifyEditFields(project, tableName, true);

        iPartsAuthorOrderId aoId = new iPartsAuthorOrderId(StrUtils.makeGUID());
        iPartsDataAuthorOrder aoData = new iPartsDataAuthorOrder(project, aoId);
        aoData.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        aoData.setStatus(iPartsAuthorOrderStatus.ORDERED, DBActionOrigin.FROM_DB);
        aoData.setCurrentCreationDate(DBActionOrigin.FROM_DB);
        String currentUser = iPartsDataAuthorOrder.getLoginAcronym();
        aoData.setCreationUser(currentUser, DBActionOrigin.FROM_DB);
        aoData.setCurrentUser(currentUser, DBActionOrigin.FROM_DB);
        aoData.setChangeSetId(new iPartsChangeSetId(StrUtils.makeGUID()));

        // Virtuelle Benutzergruppe für Autoren der Organisation des eingeloggten Benutzers bestimmen
        String loginUserId = iPartsUserAdminDb.getLoginUserIdForSession();
        if (loginUserId != null) {
            String orgId = iPartsUserAdminCache.getInstance(loginUserId).getOrgId();
            iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(orgId);
            if (orgCache.hasVirtualUserGroups()) {
                aoData.setCreationUserGroupId(orgCache.getVirtualUserGroupIdForAuthors(), DBActionOrigin.FROM_DB);
            }
        }

        EditUserControlForAuthorOrder eCtrl = new EditUserControlForAuthorOrder(dataConnector, parentForm, tableName, aoId,
                                                                                aoData.getAttributes(), editFields, true);
        eCtrl.addButton(GuiButtonOnPanel.ButtonType.CUSTOM, ModalResult.YES, BUTTON_TEXT_OK_ACTIVE, null);
        eCtrl.doEnableButtons(null);
        eCtrl.setMainTitle("!!Autoren Auftrag erstellen");
        ModalResult result = eCtrl.showModal();
        if (result == ModalResult.OK) {
            aoData.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
            return aoData;
        } else if (result == ModalResult.YES) {
            eCtrl.onButtonOKAction(null);
            aoData.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
            if (setActive != null) {
                setActive.setValue(true);
            }
            return aoData;
        }
        return null;
    }

    public static iPartsDataAuthorOrder showEditAuthorOrder(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                            iPartsAuthorOrderId aoId, boolean isActive, boolean readOnly) {
        String tableName = TABLE_DA_AUTHOR_ORDER;
        EtkProject project = dataConnector.getProject();
        EtkEditFields editFields;
        if (!readOnly && isActive) {
            editFields = modifyEditFields(project, tableName, false, FIELD_DAO_NAME);
        } else {
            editFields = modifyEditFields(project, tableName, false);
        }

        EditUserControlForAuthorOrder eCtrl;
        iPartsDataAuthorOrder aoData = new iPartsDataAuthorOrder(project, aoId);
        if (aoData.loadFromDB(aoId)) {
            eCtrl = new EditUserControlForAuthorOrder(dataConnector, parentForm, tableName, aoId,
                                                      aoData.getAttributes(), editFields, false);
        } else {
            eCtrl = new EditUserControlForAuthorOrder(dataConnector, parentForm, tableName, aoId);
        }
        eCtrl.setReadOnly(readOnly);
        if (readOnly) {
            eCtrl.setMainTitle("!!Autoren Auftrag anzeigen");
        } else {
            eCtrl.setMainTitle("!!Autoren Auftrag bearbeiten");
        }
        ModalResult result = eCtrl.showModal();
        if (!readOnly && (result == ModalResult.OK)) {
            aoData.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
            return aoData;
        }
        return null;
    }

    private static EtkEditFields modifyEditFields(EtkProject project, String tableName, boolean isCreate, String... extraReadOnlyFieldNames) {
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(project.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_AUTHOR_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            EtkEditFieldHelper.getEditFields(project, tableName, editFields, false);
        }
        // überprüfe die Edit-Konfig
        editFields = modifyEditFields(editFields, tableName, mustHaveValueFieldNames, null,
                                      isCreate ? invisibleFieldNamesForCreate : null,
                                      readOnlyFieldNames, extraReadOnlyFieldNames);

        if (!Constants.DEVELOPMENT) {
            EtkEditField editField = editFields.getFeldByName(tableName, FIELD_DAO_GUID);
            if (editField != null) {
                editField.setVisible(false);
            }

            editField = editFields.getFeldByName(tableName, FIELD_DAO_CHANGE_SET_ID);
            if (editField != null) {
                editField.setVisible(false);
            }
        }

        return editFields;
    }


    private boolean isNewForm;
    private Map<String, Object> specialFields;


    public EditUserControlForAuthorOrder(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                         IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                         boolean isNewForm) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        this.isNewForm = isNewForm;
        setWindowName("authoredit");
    }

    public EditUserControlForAuthorOrder(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id) {
        super(dataConnector, parentForm, tableName, id);
        this.isNewForm = false;
        setWindowName("authoredit");
    }

    @Override
    protected String calculateInitialValue(EtkEditField field, DBDataObjectAttribute attrib) {
        String initialValue = super.calculateInitialValue(field, attrib);
        String fieldName = field.getKey().getFieldName();

        // Benennungen der virtuellen Benutzergruppen anzeigen anstatt deren IDs
        if (fieldName.equals(FIELD_DAO_CREATOR_GRP_ID) || fieldName.equals(FIELD_DAO_CURRENT_GRP_ID)) {
            return iPartsVirtualUserGroup.getVirtualUserGroupName(initialValue);
        } else {
            return initialValue;
        }
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        // für die Verknüpfung zum Change-Set
        if (specialFields == null) {
            specialFields = new LinkedHashMap<>();
            specialFields.put(FIELD_DAO_CHANGE_SET_ID, null);
            specialFields.put(FIELD_DAO_CREATION_USER_ID, null);
        }
        String fieldName = field.getKey().getFieldName();
        AbstractGuiControl editControl = ctrl.getEditControl().getControl();
        if (specialFields.containsKey(fieldName)) {
            Object obj = specialFields.get(fieldName);
            if (fieldName.equals(FIELD_DAO_CHANGE_SET_ID)) {
                GuiButtonTextField changeSetSelect;
                if (obj == null) {
                    changeSetSelect = new GuiButtonTextField();
                    changeSetSelect.setName("changeSetSelect");
                    changeSetSelect.setButtonVisible(true);
                    changeSetSelect.setButtonTooltip("!!Änderungsset anlegen");
                    changeSetSelect.addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                        @Override
                        public void fire(Event event) {
                            createNewChangeSet(event);
                        }
                    });
                    specialFields.put(FIELD_DAO_CHANGE_SET_ID, changeSetSelect);
                } else {
                    changeSetSelect = (GuiButtonTextField)obj;
                }
                ctrl.getEditControl().setControl(changeSetSelect);
                changeSetSelect.setText(initialValue);
                changeSetSelect.setEditable(false);
                changeSetSelect.setButtonVisible(initialValue.isEmpty());
            } else if (fieldName.equals(FIELD_DAO_CREATION_USER_ID)) {
                GuiTextField userIdField;
                if (obj == null) {
                    userIdField = new GuiTextField();
                    userIdField.setName(("userIdField"));
                    userIdField.setVisible(true);
                    specialFields.put(FIELD_DAO_CURRENT_USER_ID, userIdField);
                } else {
                    userIdField = (GuiTextField)obj;
                }
                ctrl.getEditControl().setControl(userIdField);
                userIdField.setText(initialValue);
                userIdField.setEditable(false);
            }
        } else {
            if (editControl instanceof GuiScrollPane) {
                // als Bypass für JFRAME-1019
                if (!editControl.getChildren().isEmpty() && (editControl.getChildren().get(0) instanceof GuiTextArea)) {
                    GuiTextArea textArea = (GuiTextArea)editControl.getChildren().get(0);
                    textArea.addEventListener(new EventListener(Event.KEY_RELEASED_EVENT) {
                        @Override
                        public void fire(Event event) {
                            doEnableButtons(event);
                        }
                    });
                }

            }
        }

        // Im DEVELOPMENT-Modus automatisch ein paar Felder befüllen falls diese leer sind, damit man schnell neue Test-
        // Autoren-Aufträge anlegen kann
        if (Constants.DEVELOPMENT && StrUtils.isEmpty(initialValue)) {
            if (fieldName.equals(FIELD_DAO_NAME)) {
                if (editControl instanceof GuiTextField) {
                    ((GuiTextField)editControl).setText("Test " + FrameworkUtils.getUserName().toUpperCase() + " "
                                                        + DateUtils.getCurrentDateFormatted(DateUtils.simpleDateTimeFormatddDOTMMDOTyyyy));
                }
            } else if (fieldName.equals(FIELD_DAO_BST_ID)) {
                if (editControl instanceof iPartsGuiWorkOrderSelectComboBox) {
                    iPartsGuiWorkOrderSelectComboBox bstIdComboBox = (iPartsGuiWorkOrderSelectComboBox)editControl;
                    if (bstIdComboBox.getItemCount() > 1) {
                        bstIdComboBox.setSelectedIndex(1); // An Index 0 ist der leere Eintrag
                    }
                }
            }
        }
    }

    private void createNewChangeSet(Event event) {
        Object obj = specialFields.get(FIELD_DAO_CHANGE_SET_ID);
        if (obj != null) {
            GuiButtonTextField changeSetSelect = (GuiButtonTextField)obj;
            if (changeSetSelect.isButtonVisible()) {
                //neuen ChangeSet-ID anlegen
                iPartsChangeSetId changeSetId = new iPartsChangeSetId(StrUtils.makeGUID());
                changeSetSelect.setText(changeSetId.getGUID());
                changeSetSelect.setButtonVisible(false);
            }
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        Object obj = specialFields.get(FIELD_DAO_CHANGE_SET_ID);
        if (obj != null) {
            GuiButtonTextField changeSetSelect = (GuiButtonTextField)obj;
            if (changeSetSelect.isButtonVisible()) {
                changeSetSelect.setButtonVisible(!isReadOnly());
            }
        }
    }

    @Override
    protected void doEnableButtons(Event event) {
        boolean enabled = !checkMustFieldsHaveValues();
        if (!isNewForm && enabled) {
            enabled = checkForModified();
        }
        enableOKButton(readOnly || enabled);
        if (isNewForm) {
            GuiButtonOnPanel button = getButton(GuiButtonOnPanel.ButtonType.CUSTOM, BUTTON_TEXT_OK_ACTIVE);
            if (button != null) {
                button.setEnabled(enabled);
            }
        }
    }
}