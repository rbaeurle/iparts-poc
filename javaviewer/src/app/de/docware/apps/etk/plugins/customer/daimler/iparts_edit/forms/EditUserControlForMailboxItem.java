/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.datatypes.date.DateConfig;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EditControls;
import de.docware.apps.etk.base.forms.common.EditCreateMode;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.EtkPluginApi;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsDataMailboxItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsDataMailboxRecipient;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsMailboxHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsMailboxItemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiUserSelectComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMailboxChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.images.EditDefaultImages;
import de.docware.framework.combimodules.useradmin.db.OrganisationDbObject;
import de.docware.framework.combimodules.useradmin.db.RightScope;
import de.docware.framework.combimodules.useradmin.db.RoleDbObject;
import de.docware.framework.combimodules.useradmin.db.UserAdminRoleCache;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiComboBoxMode;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.pool.ConnectionPool;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserControl für Nachrichten im Postkorb (Mailbox)
 */
public class EditUserControlForMailboxItem extends EditUserControlForCreate implements iPartsConst {

    private static final int SELECT_USER_OR_GROUP_INDEX = 0;
    private static final int SELECT_ROLES_INDEX = 1;
    private static final int SELECT_ORGANISATION_INDEX = 2;
    private static final int MESSAGE_TO_SEPARATE_RECEIVERS_INDEX = 1; // Index 3 falls auch Rollen und Organisationen ausgewählt werden können sollen

    private static final String[] readOnlyFieldNames = new String[]{ FIELD_DMSG_GUID, FIELD_DMSG_CREATION_USER_ID, FIELD_DMSG_CREATION_DATE };
    private static final String[] editableFieldNames = new String[]{ FIELD_DMSG_RESUBMISSION_DATE };
    private static final String[] mustHaveValueFieldNames = new String[]{ FIELD_DMSG_TYPE, FIELD_DMSG_SUBJECT };
    private static final String[] invisibleFieldNamesForCreate = new String[]{ FIELD_DMSG_GUID };
    private static final String[] invisibleFieldNamesForView = new String[]{ FIELD_DMSG_GUID };

    private String creationUserId;
    private boolean isMessageRead;
    private boolean isRelevantForResubmission;

    /**
     * Zeigt einen Dialog zum Erzeugen einer neuen Nachricht.
     *
     * @param title
     * @param dataConnector
     * @param parentForm
     * @return
     */
    public static iPartsDataMailboxItem createMailboxItem(String title, AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        // Neue Nachricht
        iPartsMailboxItemId mailboxItemId = new iPartsMailboxItemId(FrameworkUtils.createUniqueId(true));
        iPartsDataMailboxItem mailboxItem = new iPartsDataMailboxItem(dataConnector.getProject(), mailboxItemId);
        mailboxItem.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        mailboxItem.setFieldValue(FIELD_DMSG_TYPE, iPartsMailboxHelper.MailboxMessageType.INFO.name(), DBActionOrigin.FROM_EDIT);
        return createMailboxItem(mailboxItem, null, title, dataConnector, parentForm);
    }

    /**
     * Zeigt einen Dialog zum Erzeugen einer neuen Nachricht auf Basis der übergebenen Nachricht und der optionalen übergebenen
     * Empfänger-Benutzer-ID.
     *
     * @param mailboxItem
     * @param toUserId
     * @param title
     * @param dataConnector
     * @param parentForm
     * @return
     */
    public static iPartsDataMailboxItem createMailboxItem(iPartsDataMailboxItem mailboxItem, String toUserId, String title,
                                                          AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        String tableName = TABLE_DA_MESSAGE;

        mailboxItem.setFieldValue(FIELD_DMSG_CREATION_USER_ID, FrameworkUtils.getUserName(), DBActionOrigin.FROM_EDIT);
        mailboxItem.setFieldValueAsDateTime(FIELD_DMSG_CREATION_DATE, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);

        mailboxItem.setFieldValue(FIELD_DMSG_RESUBMISSION_DATE, "", DBActionOrigin.FROM_EDIT);

        // Alle relevanten Felder für Tabelle DA_MESSAGE_TO explizit zum mailboxItem hinzufügen
        mailboxItem.getAttributes().addField(iPartsDataVirtualFieldsDefinition.MESSAGE_TO_SEPARATE_RECEIVERS, SQLStringConvert.booleanToPPString(false),
                                             true, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        mailboxItem.getAttributes().addField(FIELD_DMT_USER_ID, "", DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        mailboxItem.getAttributes().addField(FIELD_DMT_GROUP_ID, "", DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        mailboxItem.getAttributes().addField(FIELD_DMT_ROLE_ID, "", DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        mailboxItem.getAttributes().addField(FIELD_DMT_ORGANISATION_ID, "", DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);

        EtkProject project = dataConnector.getProject();
        EtkEditFields editFields = modifyEditFields(project, tableName, true, false);

        // Benutzer und virtuelle Benutzergruppen
        iPartsGuiUserSelectComboBox selectUserOrGroupsComboBox = new iPartsGuiUserSelectComboBox();
        selectUserOrGroupsComboBox.setName("selectUserOrGroupsComboBox");
        selectUserOrGroupsComboBox.setMode(GuiComboBoxMode.Mode.CHECKBOX);
        selectUserOrGroupsComboBox.setTooltip("!!Jeder ausgewählte Benutzer bzw. Gruppe erhält eine separate Nachricht mit identischem Inhalt");
        String loginUserId = iPartsUserAdminDb.getLoginUserIdForSession();
        selectUserOrGroupsComboBox.init(iPartsUserTypes.ASSIGN.getUsersMap(loginUserId, iPartsRight.CREATE_MAILBOX_MESSAGES, false),
                                        false, null, loginUserId);

        if (StrUtils.isValid(toUserId)) {
            selectUserOrGroupsComboBox.setSelectedUserObject(toUserId);
        }

        // Vorbereitung für Rollen
        RComboBox<RoleDbObject> selectRolesComboBox = new RComboBox<>();
        selectRolesComboBox.setName("selectRolesComboBox");
        selectRolesComboBox.setTooltip("!!Jede Rolle erhält eine separate Nachricht mit identischem Inhalt (eingeschränkt pro ausgewählte Organisation falls mindestens eine Organisation ausgewählt ist)");
        selectRolesComboBox.setMode(GuiComboBoxMode.Mode.CHECKBOX);

        // Vorbereitung für Organisationen
        RComboBox<OrganisationDbObject> selectOrganisationsComboBox = new RComboBox<>();
        selectOrganisationsComboBox.setName("selectOrganisationsComboBox");
        selectOrganisationsComboBox.setTooltip("!!Jede Organisation erhält eine separate Nachricht mit identischem Inhalt (eingeschränkt pro ausgewählte Rolle falls mindestens eine Rolle ausgewählt ist)");
        selectOrganisationsComboBox.setMode(GuiComboBoxMode.Mode.CHECKBOX);

        try {
            // Rollen
            ConnectionPool userAdminConnectionPool = iPartsUserAdminDb.get().getConnectionPool(false);
            List<RoleDbObject> roleDbs = RoleDbObject.getAllRoleDbs(userAdminConnectionPool, null);
            Collections.sort(roleDbs, RoleDbObject.COMPARATOR_ROLE_NAME);
            for (RoleDbObject roleDb : roleDbs) {
                selectRolesComboBox.addItem(roleDb, TranslationHandler.translate(roleDb.getRoleName()));
            }

            // Organisationen
            iPartsUserAdminCacheElement user = iPartsUserAdminCache.getInstance(loginUserId);
            RightScope scopeForCreateMessagesRight = user.getUserRightScope(iPartsRight.CREATE_MAILBOX_MESSAGES);
            if (scopeForCreateMessagesRight.ordinal() >= RightScope.CURRENT_ORGANISATION.ordinal()) {
                Set<String> validOrgIds = user.getValidOrgIdsForAssignUserOrGroup(iPartsRight.CREATE_MAILBOX_MESSAGES);
                for (String organisationId : validOrgIds) {
                    OrganisationDbObject organisationDbObject = OrganisationDbObject.getOrganisationDb(userAdminConnectionPool,
                                                                                                       null, organisationId);
                    if (organisationDbObject != null) {
                        selectOrganisationsComboBox.addItem(organisationDbObject, TranslationHandler.translate(organisationDbObject.getOrganisationName()));
                    }
                }
            }
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.ERROR, e);
            MessageDialog.showError("!!Fehler beim Laden von Daten aus der Benutzerverwaltung.", "!!Fehler");
        }

        // Spezial-EditControls
        EditControl selectUserOrGroupsControl = createMessageToEditControl(SELECT_USER_OR_GROUP_INDEX, "!!Benutzer und/oder Gruppen",
                                                                           FIELD_DMT_USER_ID, selectUserOrGroupsComboBox,
                                                                           project);
        EditControl selectRolesEditControl = createMessageToEditControl(SELECT_ROLES_INDEX, "!!Rollen",
                                                                        FIELD_DMT_ROLE_ID, selectRolesComboBox,
                                                                        project);

        EditControl selectOrgsEditControl = createMessageToEditControl(SELECT_ORGANISATION_INDEX, "!!Organisationen",
                                                                       FIELD_DMT_ORGANISATION_ID, selectOrganisationsComboBox,
                                                                       project);

        // EditControls-Instanz mit Spezial-Controls
        EditControls editUserControls = new EditControls() {
            @Override
            public EditControl createForEdit(AbstractGuiControl parent, EtkProject project, String tableName, String fieldName,
                                             String dbLanguage, String viewerLanguage, String initialValue, String labelText,
                                             int indexInList) {
                if (tableName.equals(TABLE_DA_MESSAGE)) {
                    if (fieldName.equals(FIELD_DMSG_TYPE)) {
                        // Beim Erzeugen von Nachrichten alle Nachrichten-Typen von automatisch erzeugten Nachrichten entfernen
                        EditControl editControl = super.createForEdit(parent, project, tableName, fieldName, dbLanguage, viewerLanguage,
                                                                      initialValue, labelText, indexInList);
                        if (editControl.getEditControl().getControl() instanceof RComboBox) {
                            RComboBox<String> messageTypeComboBox = (RComboBox)editControl.getEditControl().getControl();
                            for (iPartsMailboxHelper.MailboxMessageType messageType : iPartsMailboxHelper.MailboxMessageType.values()) {
                                if (messageType.isAutomaticallyCreated()) {
                                    messageTypeComboBox.removeItemByUserObject(messageType.name());
                                }
                            }
                            messageTypeComboBox.setSelectedUserObject(iPartsMailboxHelper.MailboxMessageType.INFO.name());
                        }
                        return editControl;
                    }
                } else if (tableName.equals(TABLE_DA_MESSAGE_TO)) {
                    if (fieldName.equals(FIELD_DMT_USER_ID)) {
                        add(selectUserOrGroupsControl);
                        return selectUserOrGroupsControl;
                    } else if (fieldName.equals(FIELD_DMT_ROLE_ID)) {
                        add(selectRolesEditControl);
                        return selectRolesEditControl;
                    } else if (fieldName.equals(FIELD_DMT_ORGANISATION_ID)) {
                        add(selectOrgsEditControl);
                        return selectOrgsEditControl;
                    }
                }

                return super.createForEdit(parent, project, tableName, fieldName, dbLanguage, viewerLanguage, initialValue,
                                           labelText, indexInList);
            }
        };

        // Dialog mit Businesslogic für Attribute von Spezial-Controls
        EditUserControlForMailboxItem eCtrl = new EditUserControlForMailboxItem(dataConnector, parentForm, tableName,
                                                                                mailboxItem.getAsId(), mailboxItem.getAttributes(),
                                                                                editFields, editUserControls) {
            @Override
            protected void fillAttribByEditControlValue(int index, EtkEditField field, DBDataObjectAttribute attrib) {
                String fieldName = field.getKey().getFieldName();
                if (field.getKey().getTableName().equals(TABLE_DA_MESSAGE_TO)) {
                    if (fieldName.equals(FIELD_DMT_USER_ID)) {
                        attrib.setValueAsString(SetOfEnumDataType.getSetOfEnumString(selectUserOrGroupsComboBox.getSelectedUserObjects()),
                                                DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                        return;
                    } else if (fieldName.equals(FIELD_DMT_ROLE_ID)) {
                        attrib.setValueAsString(SetOfEnumDataType.getSetOfEnumString(selectRolesComboBox.getSelectedUserObjects().stream()
                                                                                             .map(roleDb -> roleDb.getRoleId())
                                                                                             .collect(Collectors.toList())),
                                                DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                        return;
                    } else if (fieldName.equals(FIELD_DMT_ORGANISATION_ID)) {
                        attrib.setValueAsString(SetOfEnumDataType.getSetOfEnumString(selectOrganisationsComboBox.getSelectedUserObjects().stream()
                                                                                             .map(orgDb -> orgDb.getOrganisationId())
                                                                                             .collect(Collectors.toList())),
                                                DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                        return;
                    }
                }

                super.fillAttribByEditControlValue(index, field, attrib); // Normale Felder
            }

            @Override
            protected boolean checkAllMustFieldsFilled(boolean checkClonedAttributesModified) {
                if (!super.checkAllMustFieldsFilled(checkClonedAttributesModified)) {
                    return false;
                }

                return !selectUserOrGroupsComboBox.getSelectedUserObjects().isEmpty() || !selectRolesComboBox.getSelectedUserObjects().isEmpty()
                       || !selectOrganisationsComboBox.getSelectedUserObjects().isEmpty();
            }

            @Override
            public void setReadOnly(boolean readOnly) {
                super.setReadOnly(readOnly);

                selectUserOrGroupsControl.getEditControl().setReadOnly(readOnly || selectUserOrGroupsComboBox.getItemCount() == 0);
                selectRolesEditControl.getEditControl().setReadOnly(readOnly || selectRolesComboBox.getItemCount() == 0);
                selectOrgsEditControl.getEditControl().setReadOnly(readOnly || selectOrganisationsComboBox.getItemCount() == 0);

                // Nachrichten-Typ disablen falls es nur einen Typ gibt
                EditControl editControl = getEditControlByFieldName(FIELD_DMSG_TYPE);
                if ((editControl != null) && (editControl.getEditControl().getControl() instanceof RComboBox)) {
                    RComboBox<String> messageTypeComboBox = (RComboBox)editControl.getEditControl().getControl();
                    messageTypeComboBox.setEnabled(!readOnly && (messageTypeComboBox.getItemCount() > 1));
                }
            }

            @Override
            public EditResult stopAndStoreEdit() {
                EditResult editResult = super.stopAndStoreEdit();
                if (editResult == EditResult.STORED) {
                    // Nachricht versenden falls EditResult OK
                    if (!iPartsMailboxHelper.sendMailBoxItem(mailboxItem, title, project)) {
                        return EditResult.ERROR; // Führt dazu, dass der Dialog nicht geschlossen wird
                    }
                }

                return editResult;
            }
        };

        eCtrl.setReadOnly(false);
        eCtrl.setMainTitle(title);

        // In iParts sollen aktuell keine Rollen oder Organisationen ausgewählt werden können
//        eCtrl.setTitle("!!Bitte mindestens einen Benutzer, Gruppe, Rolle oder Organisation als Empfänger auswählen");
        eCtrl.setTitle("!!Bitte mindestens einen Benutzer oder Gruppe als Empfänger auswählen");

        ModalResult result = eCtrl.showModal();
        if (result == ModalResult.OK) {
            return mailboxItem;
        }
        return null;
    }

    private static EditControl createMessageToEditControl(int index, String label, String fieldName, AbstractGuiControl guiControl,
                                                          EtkProject project) {
        EditControl editControl = new EditControl(label, index);
        editControl.setEditControl(new EditControlFactory(project, TABLE_DA_MESSAGE_TO, fieldName,
                                                          project.getDBLanguage(), project.getViewerLanguage(),
                                                          "", null, EditCreateMode.ecmEdit, false, false) {

            @Override
            protected void createSpecialEditor() {
                control = guiControl;
            }
        });
        return editControl;
    }

    public static boolean showMailboxItem(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                          iPartsDataMailboxItem mailboxItem, iPartsDataMailboxRecipient mailboxRecipient, boolean isSentMessage) {
        String tableName = TABLE_DA_MESSAGE;
        EtkProject project = dataConnector.getProject();
        EtkEditFields editFields = modifyEditFields(project, tableName, false, mailboxItem.isRelevantForResubmission());

        EditUserControlForMailboxItem eCtrl = new EditUserControlForMailboxItem(dataConnector, parentForm, tableName,
                                                                                mailboxItem.getAsId(), mailboxItem.getAttributes(),
                                                                                editFields, null);

        eCtrl.setOKButtonText("!!Schließen");
        eCtrl.setRelevantForResubmission(mailboxItem.isRelevantForResubmission());
        eCtrl.setReadOnly(true);
        eCtrl.setMainTitle("!!Nachricht anzeigen");

        if (!isSentMessage) {
            eCtrl.setMessageRead((mailboxRecipient != null) && mailboxRecipient.isMessageRead());

            if (mailboxItem.isAnswerPossible()) {
                GuiButtonOnPanel answerButton = eCtrl.addButton(GuiButtonOnPanel.ButtonType.BACK, ModalResult.CONTINUE, "!!Antworten",
                                                                EditDefaultImages.edit_mailbox_answer.getImage());
                answerButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                    @Override
                    public void fire(Event event) {
                        eCtrl.close();
                        answerMailboxItem(mailboxItem, mailboxRecipient, dataConnector, parentForm);
                    }
                });
            }
        }

        if (mailboxItem.isForwardPossible()) {
            GuiButtonOnPanel forwardButton = eCtrl.addButton(GuiButtonOnPanel.ButtonType.NEXT, ModalResult.CONTINUE, "!!Weiterleiten",
                                                             EditDefaultImages.edit_mailbox_forward.getImage());
            forwardButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    eCtrl.close();
                    forwardMailboxItem(mailboxItem, mailboxRecipient, dataConnector, parentForm);
                }
            });
        }

        // Die Möglichkeit zur Wiedervorlage
        VarParam<Boolean> isResubmissionTimestampOk = new VarParam<>(true);
        if (eCtrl.isRelevantForResubmission() && (mailboxRecipient != null) && mailboxRecipient.isMessageRead()) {
            GuiButtonOnPanel resubmissionButton = eCtrl.addButton(GuiButtonOnPanel.ButtonType.START, ModalResult.NONE, "!!Wiedervorlagedatum ändern",
                                                                  EditDefaultImages.edit_btn_refresh.getImage());
            resubmissionButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    isResubmissionTimestampOk.setValue(eCtrl.checkAndSetResubmissionDate(mailboxItem));
                    if (isResubmissionTimestampOk.getValue()) {
                        eCtrl.close();

                        if (mailboxRecipient != null) {
                            iPartsMailboxChangedEvent mbcEvent = new iPartsMailboxChangedEvent();
                            mbcEvent.addMailboxRecipient(mailboxItem.getFieldValue(FIELD_DMSG_CREATION_USER_ID), mailboxRecipient);
                            ApplicationEvents.fireEventInAllProjectsAndAllClusters(mbcEvent);
                        }
                    }
                }
            });
            eCtrl.doEnableButtons(null);
        }

        ModalResult result = eCtrl.showModal();

        // Wiedervorlagetermin speichern beim Setzen auf gelesen
        if ((result == ModalResult.YES) && eCtrl.isRelevantForResubmission && !eCtrl.isMessageRead) {
            isResubmissionTimestampOk.setValue(eCtrl.checkAndSetResubmissionDate(mailboxItem));
        }

        if (!isResubmissionTimestampOk.getValue()) {
            result = ModalResult.NONE;
        }

        if (result == ModalResult.YES) {
            return true;
        }
        return false;
    }

    /**
     * Zeigt einen Dialog zum Beantworten der übergebenen Nachricht.
     *
     * @param mailboxItem
     * @param mailboxRecipient
     * @param dataConnector
     * @param parentForm
     * @return
     */
    public static iPartsDataMailboxItem answerMailboxItem(iPartsDataMailboxItem mailboxItem, iPartsDataMailboxRecipient mailboxRecipient,
                                                          AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        iPartsUserAdminCacheElement creationUser = iPartsUserAdminCache.getCacheByUserName(mailboxItem.getFieldValue(FIELD_DMSG_CREATION_USER_ID));
        if (creationUser == null) {
            return null;
        }

        EtkProject project = dataConnector.getProject();
        iPartsMailboxItemId mailboxItemId = new iPartsMailboxItemId(FrameworkUtils.createUniqueId(true));
        iPartsDataMailboxItem answerMailBoxItem = new iPartsDataMailboxItem(project, mailboxItemId);
        answerMailBoxItem.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        answerMailBoxItem.setFieldValue(FIELD_DMSG_TYPE, iPartsMailboxHelper.MailboxMessageType.INFO.name(), DBActionOrigin.FROM_EDIT); // Nachrichten-Typ ANSWER gibt es bei iParts aktuell nicht
        String subject = mailboxItem.getFieldValue(FIELD_DMSG_SUBJECT);
        answerMailBoxItem.setFieldValue(FIELD_DMSG_SUBJECT, TranslationHandler.translate("!!AW:") + " " + subject,
                                        DBActionOrigin.FROM_EDIT);
        answerMailBoxItem.setFieldValue(FIELD_DMSG_SERIES_NO, mailboxItem.getFieldValue(FIELD_DMSG_SERIES_NO), DBActionOrigin.FROM_EDIT);

        String message = createMessageTextForAnswerOrForward(mailboxItem, mailboxRecipient, subject, project);
        answerMailBoxItem.setFieldValue(FIELD_DMSG_MESSAGE, message, DBActionOrigin.FROM_EDIT);

        return createMailboxItem(answerMailBoxItem, creationUser.getUserId(), "!!Nachricht beantworten", dataConnector, parentForm);
    }

    /**
     * Zeigt einen Dialog zum Weiterleiten der übergebenen Nachricht.
     *
     * @param mailboxItem
     * @param mailboxRecipient
     * @param dataConnector
     * @param parentForm
     * @return
     */
    public static iPartsDataMailboxItem forwardMailboxItem(iPartsDataMailboxItem mailboxItem, iPartsDataMailboxRecipient mailboxRecipient,
                                                           AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        EtkProject project = dataConnector.getProject();
        iPartsMailboxItemId mailboxItemId = new iPartsMailboxItemId(FrameworkUtils.createUniqueId(true));
        iPartsDataMailboxItem forwardMailBoxItem = new iPartsDataMailboxItem(project, mailboxItemId);
        forwardMailBoxItem.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

        // Gültigen Nachrichten-Typ ermitteln
        iPartsMailboxHelper.MailboxMessageType messageType = iPartsMailboxHelper.MailboxMessageType.valueOf(mailboxItem.getFieldValue(FIELD_DMSG_TYPE));
        if ((messageType == null) || messageType.isAutomaticallyCreated()) {
            messageType = iPartsMailboxHelper.MailboxMessageType.INFO;
        }
        forwardMailBoxItem.setFieldValue(FIELD_DMSG_TYPE, messageType.name(), DBActionOrigin.FROM_EDIT);

        String subject = mailboxItem.getFieldValue(FIELD_DMSG_SUBJECT);
        forwardMailBoxItem.setFieldValue(FIELD_DMSG_SUBJECT, TranslationHandler.translate("!!WG:") + " " + subject,
                                         DBActionOrigin.FROM_EDIT);
        forwardMailBoxItem.setFieldValue(FIELD_DMSG_SERIES_NO, mailboxItem.getFieldValue(FIELD_DMSG_SERIES_NO), DBActionOrigin.FROM_EDIT);

        String message = createMessageTextForAnswerOrForward(mailboxItem, mailboxRecipient, subject, project);
        forwardMailBoxItem.setFieldValue(FIELD_DMSG_MESSAGE, message, DBActionOrigin.FROM_EDIT);

        return createMailboxItem(forwardMailBoxItem, null, "!!Nachricht weiterleiten", dataConnector, parentForm);
    }

    public boolean checkAndSetResubmissionDate(iPartsDataMailboxItem mailboxItem) {
        // Hier das Wiedervorlagedatum checken und setzen
        EditControl editControl = getEditControlByFieldName(FIELD_DMSG_RESUBMISSION_DATE);
        if (editControl == null) {
            return false;
        }

        // Das Wiedervorlagedatum muss leer oder größer sein als das aktuelle Datum
        String resubmissionDate = editControl.getEditControl().getText();
        if (StrUtils.isValid(resubmissionDate)) {
            if (resubmissionDate.compareTo(DateUtils.toyyyyMMdd_currentDate()) <= 0) {
                return false;
            } else {
                Calendar resubmissionDateCalendar;
                try {
                    resubmissionDateCalendar = DateUtils.toCalendar_yyyyMMdd(resubmissionDate);
                } catch (Exception e) {
                    return false;
                }
                if (mailboxItem != null) {
                    mailboxItem.setFieldValueAsDate(FIELD_DMSG_RESUBMISSION_DATE, resubmissionDateCalendar, DBActionOrigin.FROM_EDIT);
                    mailboxItem.removeForeignTablesAttributes();
                    mailboxItem.saveToDB();
                }
            }
        } else if (mailboxItem != null) {
            mailboxItem.setFieldValue(FIELD_DMSG_RESUBMISSION_DATE, "", DBActionOrigin.FROM_EDIT);
            mailboxItem.removeForeignTablesAttributes();
            mailboxItem.saveToDB();
        }
        return true;
    }


    private static String createMessageTextForAnswerOrForward(iPartsDataMailboxItem mailboxItem, iPartsDataMailboxRecipient mailboxRecipient,
                                                              String subject, EtkProject project) {
        // Nachrichten-Text
        String messageDateTime = DateConfig.getInstance(project.getConfig()).formatDateTime(project.getViewerLanguage(),
                                                                                            mailboxItem.getFieldValue(FIELD_DMSG_CREATION_DATE));
        String message = "\n\n____________________\n\n";
        String dbLanguage = project.getDBLanguage();
        String creationUserId = mailboxItem.getFieldValue(FIELD_DMSG_CREATION_USER_ID);
        iPartsUserAdminCacheElement creationUser = iPartsUserAdminCache.getCacheByUserName(creationUserId);
        message += TranslationHandler.translate("!!Von:") + " " + ((creationUser != null) ? creationUser.getUserFullName(dbLanguage)
                                                                                          : creationUserId) + "\n";
        message += TranslationHandler.translate("!!Gesendet:") + " " + messageDateTime + "\n";

        if (mailboxRecipient != null) {
            iPartsUserAdminCacheElement currentUser = null;
            String userId = mailboxRecipient.getFieldValue(FIELD_DMT_USER_ID);
            if (StrUtils.isValid(userId)) {
                currentUser = iPartsUserAdminCache.getCacheByUserName(userId);
            }
            if (currentUser != null) { // An Benutzer
                message += TranslationHandler.translate("!!An:") + " " + currentUser.getUserFullName(dbLanguage) + "\n";
            } else {
                String groupId = mailboxRecipient.getFieldValue(FIELD_DMT_GROUP_ID);
                if (StrUtils.isValid(groupId)) { // An Gruppe
                    message += TranslationHandler.translate("!!An Gruppe:") + " " + iPartsVirtualUserGroup.getVirtualUserGroupName(groupId) + "\n";
                } else { // An Rolle und/oder Organisation
                    String roleId = mailboxRecipient.getFieldValue(FIELD_DMT_ROLE_ID);
                    if (StrUtils.isValid(roleId)) { // Für Rolle
                        String roleName = UserAdminRoleCache.getInstance(roleId).getRoleName(dbLanguage);
                        message += TranslationHandler.translate("!!Für Rolle:") + " " + roleName + "\n";
                    }

                    String orgId = mailboxRecipient.getFieldValue(FIELD_DMT_ORGANISATION_ID);
                    if (StrUtils.isValid(orgId)) { // An Organisation
                        String orgName = iPartsUserAdminOrgCache.getInstance(orgId).getOrgName(dbLanguage);
                        message += TranslationHandler.translate("!!An Organisation:") + " " + orgName + "\n";
                    }
                }
            }
        }

        message += TranslationHandler.translate("!!Betreff:") + " " + subject + "\n\n";
        message += mailboxItem.getFieldValue(FIELD_DMSG_MESSAGE);
        return message;
    }

    private static EtkEditFields modifyEditFields(EtkProject project, String tableName, boolean isCreate, boolean isRelevantForResubmission,
                                                  String... extraReadOnlyFieldNames) {
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(project.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MAILBOX_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        if (editFields.size() == 0) {
            EtkEditField editField;

            editField = new EtkEditField(TABLE_DA_MESSAGE, FIELD_DMSG_SUBJECT, false);
            editFields.addFeld(editField);

            editField = new EtkEditField(TABLE_DA_MESSAGE, FIELD_DMSG_MESSAGE, false);
            editFields.addFeld(editField);

            editField = new EtkEditField(TABLE_DA_MESSAGE, FIELD_DMSG_TYPE, false);
            editFields.addFeld(editField);

            if (!isCreate) { // Die Baureihe beim Erstellen von neuen Nachrichten standardmäßig nicht anzeigen
                editField = new EtkEditField(TABLE_DA_MESSAGE, FIELD_DMSG_SERIES_NO, false);
                editFields.addFeld(editField);
            }

            editField = new EtkEditField(TABLE_DA_MESSAGE, FIELD_DMSG_CREATION_USER_ID, false);
            editFields.addFeld(editField);

            editField = new EtkEditField(TABLE_DA_MESSAGE, FIELD_DMSG_CREATION_DATE, false);
            editFields.addFeld(editField);

            editField = new EtkEditField(TABLE_DA_MESSAGE, FIELD_DMSG_RESUBMISSION_DATE, false);
            editFields.addFeld(editField);
        }
        // überprüfe die Edit-Konfig
        editFields = modifyEditFields(editFields, tableName, mustHaveValueFieldNames, null,
                                      isCreate ? invisibleFieldNamesForCreate : invisibleFieldNamesForView,
                                      readOnlyFieldNames, extraReadOnlyFieldNames);

        if (isCreate) {
            // Feld für die Benutzer/Gruppen
            EtkEditField selectUserOrGroupEditField = new EtkEditField(TABLE_DA_MESSAGE_TO, FIELD_DMT_USER_ID, false);
            editFields.addFeld(SELECT_USER_OR_GROUP_INDEX, selectUserOrGroupEditField);

            // In iParts sollen aktuell keine Rollen oder Organisationen ausgewählt werden können
//            // Feld für die Rollen
//            EtkEditField selectRoleEditField = new EtkEditField(TABLE_DA_MESSAGE_TO, FIELD_DMT_ROLE_ID, false);
//            editFields.addFeld(SELECT_ROLES_INDEX, selectRoleEditField);
//
//            // Feld für die Organisationen
//            EtkEditField selectOrganisationEditField = new EtkEditField(TABLE_DA_MESSAGE_TO, FIELD_DMT_ORGANISATION_ID, false);
//            editFields.addFeld(SELECT_ORGANISATION_INDEX, selectOrganisationEditField);

            EtkEditField messageToSeparateReceiversEditField = new EtkEditField(TABLE_DA_MESSAGE_TO, iPartsDataVirtualFieldsDefinition.MESSAGE_TO_SEPARATE_RECEIVERS, false);
            messageToSeparateReceiversEditField.setDefaultText(false);
            messageToSeparateReceiversEditField.setText(new EtkMultiSprache("!!An alle Empfänger einzeln versenden", project.getConfig().getViewerLanguages()));
            editFields.addFeld(MESSAGE_TO_SEPARATE_RECEIVERS_INDEX, messageToSeparateReceiversEditField);
        }

        EtkEditField resubmissionDateField = editFields.getFeldByName(TABLE_DA_MESSAGE, FIELD_DMSG_RESUBMISSION_DATE);
        if (!isRelevantForResubmission) {
            if (resubmissionDateField != null) {
                resubmissionDateField.setVisible(false);
            }
        } else {
            if (resubmissionDateField == null) {
                EtkEditField editField = new EtkEditField(TABLE_DA_MESSAGE, FIELD_DMSG_RESUBMISSION_DATE, false);
                editFields.addFeld(editField);
            }
        }
        return editFields;
    }


    public EditUserControlForMailboxItem(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                         IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields,
                                         EditControls editControls) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields, editControls);
        isRelevantForResubmission = false;
        setWindowName("mailboxEditView");
        setWidth(1000);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        if (readOnly) {
            for (String fieldName : editableFieldNames) {
                EditControl ctrl = getEditControlByFieldName(fieldName);
                if (ctrl != null) {
                    ctrl.getEditControl().setReadOnly(fieldName.equals(FIELD_DMSG_RESUBMISSION_DATE) && !isRelevantForResubmission);
                }
            }
        }
    }

    public void setMessageRead(boolean isMessageRead) {
        this.isMessageRead = isMessageRead;
        String buttonText;
        FrameworkImage buttonIcon;
        if (isMessageRead) {
            setTitle("!!Nachrichten-Status: gelesen");
            buttonText = "!!Als ungelesen markieren";
            buttonIcon = EditDefaultImages.edit_mailbox_unread.getImage();
        } else {
            setTitle("!!Nachrichten-Status: ungelesen");
            buttonText = "!!Als gelesen markieren";
            buttonIcon = EditDefaultImages.edit_mailbox_read.getImage();
        }
        addButton(GuiButtonOnPanel.ButtonType.RESET, ModalResult.YES, buttonText, buttonIcon);
    }

    public boolean isRelevantForResubmission() {
        return isRelevantForResubmission;
    }

    public void setRelevantForResubmission(boolean relevantForResubmission) {
        isRelevantForResubmission = relevantForResubmission;
    }

    @Override
    protected void setAttributes() {
        super.setAttributes();
        if (creationUserId == null) {
            creationUserId = attributes.getFieldValue(FIELD_DMSG_CREATION_USER_ID);
        }
    }

    @Override
    protected void doEnableButtons(Event event) {
        if (!isReadOnly()) {
            super.doEnableButtons(event);
        } else {
            enableOKButton(true);
        }

        if (isRelevantForResubmission) {
            boolean resubmissionDateOK = checkAndSetResubmissionDate(null);

            // Wiedervorlage-Button enablen
            GuiButtonOnPanel buttonResubmission = getButton(GuiButtonOnPanel.ButtonType.START, "");
            if ((buttonResubmission != null) && buttonResubmission.isVisible()) {
                buttonResubmission.setEnabled(resubmissionDateOK);
            }

            // Gelesen-Button enablen
            if (!isMessageRead) {
                GuiButtonOnPanel buttonSetRead = getButton(GuiButtonOnPanel.ButtonType.RESET, "");
                if ((buttonSetRead != null) && buttonSetRead.isVisible()) {
                    buttonSetRead.setEnabled(resubmissionDateOK);
                }
            }
        }
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        String fieldName = field.getKey().getFieldName();
        if (fieldName.equals(FIELD_DMSG_MESSAGE)) {
            if (ctrl.getAbstractGuiControl() != null) {
                ctrl.getAbstractGuiControl().setMinimumHeight(200);
            }
        } else if (fieldName.equals(FIELD_DMSG_TYPE)) {
            if (ctrl.getAbstractGuiControl() instanceof RComboBox) {
                RComboBox messageTypeComboBox = (RComboBox)ctrl.getAbstractGuiControl();
                messageTypeComboBox.removeItemByUserObject("");
            }
        } else if (fieldName.equals(FIELD_DMSG_CREATION_USER_ID) && (creationUserId != null)) {
            ctrl.getEditControl().setText(iPartsMailboxHelper.getFullUserNameOrTechnicalUser(creationUserId, getProject().getDBLanguage()));
        } else {
            super.modifyEditControl(ctrl, field, initialValue, initialDataArray);
        }
    }

    @Override
    protected void fillAttribByEditControlValue(int index, EtkEditField field, DBDataObjectAttribute attrib) {
        if (field.getKey().getFieldName().equals(FIELD_DMSG_CREATION_USER_ID) && (creationUserId != null)) {
            attrib.setValueAsString(creationUserId, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            return;
        }

        super.fillAttribByEditControlValue(index, field, attrib);
    }

    @Override
    protected boolean isVirtualFieldEditable(String tableName, String fieldName) {
        if (tableName.equals(TABLE_DA_MESSAGE_TO) && fieldName.equals(iPartsDataVirtualFieldsDefinition.MESSAGE_TO_SEPARATE_RECEIVERS)) {
            return true;
        }

        return EtkPluginApi.hasEditorForVirtualField(tableName, fieldName);
    }
}
