/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMailboxChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.combimodules.useradmin.db.UserRolesDbObject;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;

import java.sql.SQLException;
import java.util.*;

/**
 * Hilfsklasse für das Versenden und Filtern von Nachrichten im iParts-Postkorb.
 */
public class iPartsMailboxHelper implements iPartsConst {

    public enum MailboxMessageType {

        INFO(false),
        PUBLISH_PRODUCT(true),
        PUBLISH_MODEL(true),
        CONSTRUCTION_MODEL(true),
        DIALOG_FOOTNOTE(true);

        private boolean isAutomaticallyCreated;

        MailboxMessageType(boolean isAutomaticallyCreated) {
            this.isAutomaticallyCreated = isAutomaticallyCreated;
        }

        /**
         * Ist dies ein Nachrichten-Typ für automatisch erzeugte Nachrichten?
         *
         * @return
         */
        public boolean isAutomaticallyCreated() {
            return isAutomaticallyCreated;
        }
    }


    private String userId = null;
    private String organisationId = null;
    private Set<String> roleIds = null;
    private HashSet<String> virtualUserGroups = null;

    /**
     * Versendet die übergebene Nachricht
     *
     * @param mailboxItem Nachricht
     * @param title       Fenster-Titel
     * @param project
     */
    public static boolean sendMailBoxItem(iPartsDataMailboxItem mailboxItem, String title, EtkProject project) {
        if (mailboxItem != null) {
            iPartsMailboxHelper.MailboxMessageType messageType = iPartsMailboxHelper.MailboxMessageType.valueOf(mailboxItem.getFieldValue(FIELD_DMSG_TYPE));
            if (messageType == null) {
                MessageDialog.showError(TranslationHandler.translate("!!Ungültiger Nachrichtentyp: %1", mailboxItem.getFieldValue(FIELD_DMSG_TYPE)),
                                        title);
                return false;
            }

            String subject = mailboxItem.getFieldValue(FIELD_DMSG_SUBJECT);
            String messageText = mailboxItem.getFieldValue(FIELD_DMSG_MESSAGE);
            String creationUser = mailboxItem.getFieldValue(FIELD_DMSG_CREATION_USER_ID);
            String seriesNo = mailboxItem.getFieldValue(FIELD_DMSG_SERIES_NO);
            boolean messageToSeparateReceivers = mailboxItem.getFieldValueAsBoolean(iPartsDataVirtualFieldsDefinition.MESSAGE_TO_SEPARATE_RECEIVERS);
            Set<String> receiverUserNames = new TreeSet<>();
            iPartsMailboxChangedEvent mailboxChangedEvent = new iPartsMailboxChangedEvent(iPartsMailboxChangedEvent.MailboxItemState.NEW);

            EtkDbs etkDbs = project.getEtkDbs();
            etkDbs.startTransaction();
            etkDbs.startBatchStatement();
            try {
                // Benutzer und Gruppen
                String selectedUserOrGroupIds = mailboxItem.getFieldValue(FIELD_DMT_USER_ID);
                if (!selectedUserOrGroupIds.isEmpty()) {
                    String dbLanguage = project.getDBLanguage();
                    for (String userOrGroupId : SetOfEnumDataType.parseSetofEnum(selectedUserOrGroupIds, false, true)) {
                        if (iPartsVirtualUserGroup.isVirtualUserGroupId(userOrGroupId)) {
                            if (messageToSeparateReceivers) {
                                // Alle Benutzer der virtuellen Benutzergruppe einzeln benachrichtigen
                                String organisationId = iPartsVirtualUserGroup.getOrgIdFromVirtualUserGroupId(userOrGroupId);
                                if (StrUtils.isValid(organisationId)) {
                                    iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(organisationId);
                                    for (String orgUserId : orgCache.getUserIds()) {
                                        iPartsUserAdminCacheElement orgUserCache = iPartsUserAdminCache.getInstance(orgUserId);
                                        if (orgUserCache.isMemberOfVirtualUserGroup(userOrGroupId)) {
                                            receiverUserNames.add(orgUserCache.getUserName(dbLanguage));
                                        }
                                    }
                                }
                            } else {
                                addMailboxChangedEventReceivers(iPartsMailboxHelper.createMessageForVirtualUserGroup(project, userOrGroupId,
                                                                                                                     messageType, subject,
                                                                                                                     messageText, null,
                                                                                                                     seriesNo, creationUser),
                                                                mailboxChangedEvent);
                            }
                        } else {
                            receiverUserNames.add(iPartsUserAdminCache.getInstance(userOrGroupId).getUserName(dbLanguage));
                        }
                    }
                }

                // Rollen (optional eingeschränkt nach Organisationen)
                String selectedRoleIds = mailboxItem.getFieldValue(FIELD_DMT_ROLE_ID);
                String selectedOrganisationIds = mailboxItem.getFieldValue(FIELD_DMT_ORGANISATION_ID);
                try {
                    if (!selectedRoleIds.isEmpty()) {
                        for (String roleId : SetOfEnumDataType.parseSetofEnum(selectedRoleIds, false, true)) {
                            if (selectedOrganisationIds.isEmpty()) {
                                if (messageToSeparateReceivers) {
                                    // Alle Benutzer der Rolle einzeln benachrichtigen
                                    List<String> userIdsForRole = UserRolesDbObject.getAllUserIdsForRole(iPartsUserAdminDb.get().getConnectionPool(false),
                                                                                                         null, roleId);
                                    addUserNamesForUserIdsToReceivers(userIdsForRole, receiverUserNames, project);
                                } else {
                                    addMailboxChangedEventReceivers(iPartsMailboxHelper.createMessageForRole(project, roleId, messageType,
                                                                                                             subject, messageText, null,
                                                                                                             seriesNo, creationUser),
                                                                    mailboxChangedEvent);
                                }
                            } else {
                                for (String organisationId : SetOfEnumDataType.parseSetofEnum(selectedOrganisationIds, false, true)) {
                                    if (messageToSeparateReceivers) {
                                        // Alle Benutzer der Rolle in der Organisation einzeln benachrichtigen
                                        iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(organisationId);
                                        Set<String> userIdsWithRole = orgCache.getUserIdsWithRole(roleId);
                                        addUserNamesForUserIdsToReceivers(userIdsWithRole, receiverUserNames, project);
                                    } else {
                                        addMailboxChangedEventReceivers(iPartsMailboxHelper.createMessageForOrganisationAndRole(project, organisationId,
                                                                                                                                roleId, messageType,
                                                                                                                                subject, messageText,
                                                                                                                                null, seriesNo,
                                                                                                                                creationUser),
                                                                        mailboxChangedEvent);
                                    }
                                }
                            }
                        }
                    } else {
                        // Organisationen (falls keine Rollen ausgewählt sind)
                        if (!selectedOrganisationIds.isEmpty()) {
                            for (String organisationId : SetOfEnumDataType.parseSetofEnum(selectedOrganisationIds, false, true)) {
                                if (messageToSeparateReceivers) {
                                    // Alle Benutzer der Organisation einzeln benachrichtigen
                                    iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(organisationId);
                                    Set<String> userIdsForOrg = orgCache.getUserIds();
                                    addUserNamesForUserIdsToReceivers(userIdsForOrg, receiverUserNames, project);
                                } else {
                                    addMailboxChangedEventReceivers(iPartsMailboxHelper.createMessageForOrganisation(project, organisationId, messageType,
                                                                                                                     subject, messageText, null,
                                                                                                                     seriesNo, creationUser),
                                                                    mailboxChangedEvent);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.ERROR, e);
                    MessageDialog.showError("!!Fehler beim Laden von Daten aus der Benutzerverwaltung.", title);
                }

                receiverUserNames.remove(creationUser); // Nie Nachrichten an sich selbst schicken

                if (messageToSeparateReceivers && receiverUserNames.isEmpty()) {
                    MessageDialog.showError("!!Es konnte kein Empfänger für die Nachricht ermittelt werden.");
                    return false;
                }

                // Sicherheitsabfrage bei mehr als einem Benutzer, falls messageToSeparateReceivers aktiv ist
                if (messageToSeparateReceivers && (receiverUserNames.size() > 1)) {
                    if (MessageDialog.showYesNo(TranslationHandler.translate("!!Die Nachricht wird an %1 einzelne Benutzer versendet. Fortfahren?",
                                                                             String.valueOf(receiverUserNames.size())), title) != ModalResult.YES) {
                        return false;
                    }
                }

                // Alle passenden Benutzer als Empfänger hinzufügen
                for (String receiverUserName : receiverUserNames) {
                    addMailboxChangedEventReceivers(iPartsMailboxHelper.createMessageForUser(project, receiverUserName, messageType,
                                                                                             subject, messageText, null,
                                                                                             seriesNo, creationUser),
                                                    mailboxChangedEvent);
                }

                etkDbs.endBatchStatement();
                etkDbs.commit();
            } catch (Exception e) {
                etkDbs.cancelBatchStatement();
                etkDbs.rollback();
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                MessageDialog.showError("!!Fehler beim Speichern.", title);
                return false;
            }

            // Event mit der Nachricht an alle Empfänger feuern
            if (!mailboxChangedEvent.getMailboxItems().isEmpty()) {
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(mailboxChangedEvent);
            }
        }
        return true;
    }

    private static void addMailboxChangedEventReceivers(iPartsMailboxChangedEvent sourceMailboxChangedEvent, iPartsMailboxChangedEvent destMailboxChangedEvent) {
        if (sourceMailboxChangedEvent != null) {
            destMailboxChangedEvent.addAllRecipients(sourceMailboxChangedEvent.getMailboxItems());
        }
    }

    private static void addUserNamesForUserIdsToReceivers(Collection<String> userIds, Set<String> receiverUserNames, EtkProject project) {
        String dbLanguage = project.getDBLanguage();
        for (String userId : userIds) {
            receiverUserNames.add(iPartsUserAdminCache.getInstance(userId).getUserName(dbLanguage));
        }
    }

    /**
     * Erzeugt einen personalisierten MailboxHelper.
     * Dazu werden passend zum Login Benutzername, die Rollen, die Organisation und die virtuelle(n) Benutzergruppe(n) geladen.
     *
     * @param loginUserId
     */
    public iPartsMailboxHelper(String loginUserId) {
        if (StrUtils.isValid(loginUserId)) {
            this.userId = loginUserId;
            iPartsUserAdminCacheElement cacheByUserName = iPartsUserAdminCache.getCacheByUserName(userId);
            if (cacheByUserName != null) {
                roleIds = cacheByUserName.getRoleIds();
                organisationId = cacheByUserName.getOrgId();

                virtualUserGroups = new HashSet<>();

                iPartsUserAdminOrgCache adminOrgCache = iPartsUserAdminOrgCache.getInstance(organisationId);
                if (adminOrgCache != null) {
                    String virtualUserGroupIdForAuthors = adminOrgCache.getVirtualUserGroupIdForAuthors();
                    String virtualUserGroupIdForQualityInspectors = adminOrgCache.getVirtualUserGroupIdForQualityInspectors();

                    if (cacheByUserName.isMemberOfVirtualUserGroup(virtualUserGroupIdForAuthors)) {
                        virtualUserGroups.add(virtualUserGroupIdForAuthors);
                    }
                    if (cacheByUserName.isMemberOfVirtualUserGroup(virtualUserGroupIdForQualityInspectors)) {
                        virtualUserGroups.add(virtualUserGroupIdForQualityInspectors);
                    }
                }
            }
        }
    }

    public boolean isUserDataValid() {
        return (userId != null) && (organisationId != null) && (virtualUserGroups != null) && (roleIds != null);
    }

    /**
     * Erzeugt eine Nachricht, die alle Benutzer der Rolle ProductAdmin erinnert, das angegebene Produkt zu veröffentlichen.
     *
     * @param project
     * @param productId Das Produkt, das veröffentlicht werden soll
     * @param subject   Betreff
     * @param message   Nachricht
     */
    public static iPartsMailboxChangedEvent createMessageForPublishProductReminder(EtkProject project, iPartsProductId productId,
                                                                                   String subject, String message) {
        return createMessageForRole(project, iPartsUserAdminDb.ROLE_ID_PRODUCT_ADMIN, MailboxMessageType.PUBLISH_PRODUCT,
                                    subject, message, productId, "", TECHNICAL_USER_AUTO_CHECK);
    }

    /**
     * Erzeugt eine Nachricht, die alle Benutzer der Rolle ProductAdmin erinnert, das angegebene Baumuster zu veröffentlichen.
     *
     * @param project
     * @param modelId Das Baumuster, das veröffentlicht werden soll
     * @param subject Betreff
     * @param message Nachricht
     */
    public static iPartsMailboxChangedEvent createMessageForPublishModelReminder(EtkProject project, iPartsModelId modelId,
                                                                                 String subject, String message) {
        return createMessageForRole(project, iPartsUserAdminDb.ROLE_ID_PRODUCT_ADMIN, MailboxMessageType.PUBLISH_MODEL,
                                    subject, message, modelId, "", TECHNICAL_USER_AUTO_CHECK);
    }

    /**
     * Erzeugt eine Nachricht, die alle Benutzer der Rolle Admin über eine neu anzulegende Fußnote informiert.
     * In der Nachricht sind die Fußnotennummer, die Fußnote und das Datum der Fußnote.
     * Versendet wird von einem technischen Nutzer.
     *
     * @param project    EtkProject
     * @param subject    Betreff
     * @param message    Nachricht
     * @param footNoteId Id der anzulegenden Fußnote
     * @return mailboxCahngedEvent zum versenden
     */
    public static iPartsMailboxChangedEvent createMessageForCreateDialogFootnote(EtkProject project, String subject, String message,
                                                                                 iPartsFootNoteId footNoteId) {
        return createMessageForRole(project, iPartsUserAdminDb.ROLE_ID_ADMIN, MailboxMessageType.DIALOG_FOOTNOTE,
                                    subject, message, footNoteId, "", TECHNICAL_USER_DIALOG_DELTA_SUPPLY);
    }

    /**
     * Erzeugt eine Nachricht an alle Mitglieder einer Organisation UND Rolle
     *
     * @param project        wird benötigt, um die DataObjects zu erzeugen
     * @param organisationId Organisations ID (z.B. DAIMLER)
     * @param roleId         Rollen ID (z.B. IPARTS.Admin)
     * @param messageType    Enum {@link MailboxMessageType}
     * @param subject        Nachrichten-Betreff
     * @param message        Nachrichten-Inhalt
     * @param dataObjectId   DataObjectId für spätere Navigation zum Objekt, kann auch leer gelassen werden
     * @param seriesNo       Baureihen-Nummer zur Anzeige
     * @param creationUser   UserId des Erstellers
     */
    public static iPartsMailboxChangedEvent createMessageForOrganisationAndRole(EtkProject project, String organisationId, String roleId,
                                                                                MailboxMessageType messageType, String subject, String message,
                                                                                IdWithType dataObjectId, String seriesNo, String creationUser) {
        return createMessage(project, "", "", organisationId, roleId, messageType, subject, message, dataObjectId, seriesNo, creationUser);
    }

    /**
     * Erzeugt eine Nachricht an alle Benutzer einer Rolle
     *
     * @param project      wird benötigt, um die DataObjects zu erzeugen
     * @param roleId       Rollen ID (z.B. IPARTS.Admin)
     * @param messageType  Enum {@link MailboxMessageType}
     * @param subject      Nachrichten-Betreff
     * @param message      Nachrichten-Inhalt
     * @param dataObjectId DataObjectId für spätere Navigation zum Objekt, kann auch leer gelassen werden
     * @param seriesNo     Baureihen-Nummer zur Anzeige
     * @param creationUser UserId des Erstellers
     */
    public static iPartsMailboxChangedEvent createMessageForRole(EtkProject project, String roleId, MailboxMessageType messageType, String subject,
                                                                 String message, IdWithType dataObjectId, String seriesNo, String creationUser) {
        return createMessage(project, "", "", "", roleId, messageType, subject, message, dataObjectId, seriesNo, creationUser);
    }

    /**
     * Erzeugt eine Nachricht an alle Mitglieder einer Organisation
     *
     * @param project        wird benötigt, um die DataObjects zu erzeugen
     * @param organisationId Organisations ID (z.B. DAIMLER)
     * @param messageType    Enum {@link MailboxMessageType}
     * @param subject        Nachrichten-Betreff
     * @param message        Nachrichten-Inhalt
     * @param dataObjectId   DataObjectId für spätere Navigation zum Objekt, kann auch leer gelassen werden
     * @param seriesNo       Baureihen-Nummer zur Anzeige
     * @param creationUser   UserId des Erstellers
     */
    public static iPartsMailboxChangedEvent createMessageForOrganisation(EtkProject project, String organisationId,
                                                                         MailboxMessageType messageType, String subject,
                                                                         String message, IdWithType dataObjectId, String seriesNo,
                                                                         String creationUser) {
        return createMessage(project, "", "", organisationId, "", messageType, subject, message, dataObjectId, seriesNo, creationUser);
    }

    /**
     * Erzeugt eine Nachricht an alle Mitglieder einer virtuellen Benutzergruppe
     *
     * @param project            wird benötigt, um die DataObjects zu erzeugen
     * @param virtualUserGroupId virtuelle Benutzergruppe {@link de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsVirtualUserGroup}
     * @param messageType        Enum {@link MailboxMessageType}
     * @param subject            Nachrichten-Betreff
     * @param message            Nachrichten-Inhalt
     * @param dataObjectId       DataObject, für spätere Navigation zum Objekt, kann auch leer gelassen werden
     * @param seriesNo           Baureihen-Nummer zur Anzeige
     * @param creationUser       UserId des Erstellers
     */
    public static iPartsMailboxChangedEvent createMessageForVirtualUserGroup(EtkProject project, String virtualUserGroupId,
                                                                             MailboxMessageType messageType, String subject,
                                                                             String message, IdWithType dataObjectId, String seriesNo,
                                                                             String creationUser) {
        return createMessage(project, "", virtualUserGroupId, "", "", messageType, subject, message, dataObjectId, seriesNo, creationUser);
    }

    /**
     * Erzeugt eine Nachricht an einen konkreten Benutzer
     *
     * @param project      wird benötigt, um die DataObjects zu erzeugen
     * @param targetUserId UserId des Empfängers {@link iPartsUserAdminDb#getLoginUserName()}
     * @param messageType  Enum {@link MailboxMessageType}
     * @param subject      Nachrichten-Betreff
     * @param message      Nachrichten-Inhalt
     * @param dataObjectId DataObjectId für spätere Navigation zum Objekt, kann auch leer gelassen werden
     * @param seriesNo     Baureihen-Nummer zur Anzeige
     * @param creationUser UserId des Erstellers
     */
    public static iPartsMailboxChangedEvent createMessageForUser(EtkProject project, String targetUserId, MailboxMessageType messageType, String subject,
                                                                 String message, IdWithType dataObjectId, String seriesNo, String creationUser) {
        return createMessage(project, targetUserId, "", "", "", messageType, subject, message, dataObjectId, seriesNo, creationUser);
    }

    private static iPartsMailboxChangedEvent createMessage(EtkProject project, String targetUserId, String virtualUserGroupId,
                                                           String organisationId, String roleId, MailboxMessageType messageType, String subject,
                                                           String message, IdWithType dataObjectId, String seriesNo, String creationUser) {
        String guid = StrUtils.makeGUID();
        iPartsDataMailboxItem mailboxItem = createMailboxItem(project, guid, messageType, subject, message, dataObjectId,
                                                              seriesNo, creationUser);

        if (mailboxItem != null) {
            iPartsMailboxRecipientId recipientId = new iPartsMailboxRecipientId(guid, targetUserId, virtualUserGroupId,
                                                                                organisationId, roleId);
            iPartsDataMailboxRecipient recipient = new iPartsDataMailboxRecipient(project, recipientId);
            recipient.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);

            project.getDbLayer().startTransaction();
            project.getDbLayer().startBatchStatement();
            try {
                mailboxItem.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
                recipient.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
                project.getDbLayer().endBatchStatement();
                project.getDbLayer().commit();
            } catch (RuntimeException e) {
                project.getDbLayer().cancelBatchStatement();
                project.getDbLayer().rollback();
                throw e;
            }
            return new iPartsMailboxChangedEvent(iPartsMailboxChangedEvent.MailboxItemState.NEW, creationUser, recipient);
        }
        return null;
    }

    private static iPartsDataMailboxItem createMailboxItem(EtkProject project, String guid, MailboxMessageType messageType, String subject,
                                                           String message, IdWithType dataObjectId, String seriesNo, String creationUser) {
        if ((project == null) || StrUtils.isEmpty(guid)) {
            return null;
        }
        iPartsMailboxItemId mailboxId = new iPartsMailboxItemId(guid);
        iPartsDataMailboxItem mailboxItem = new iPartsDataMailboxItem(project, mailboxId);
        mailboxItem.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        mailboxItem.setFieldValue(FIELD_DMSG_TYPE, messageType.name(), DBActionOrigin.FROM_EDIT);
        if (dataObjectId != null) {
            mailboxItem.setFieldValue(FIELD_DMSG_DO_TYPE, dataObjectId.getType(), DBActionOrigin.FROM_EDIT);
            mailboxItem.setFieldValue(FIELD_DMSG_DO_ID, dataObjectId.toDBString(), DBActionOrigin.FROM_EDIT);
        }
        if (StrUtils.isValid(seriesNo)) {
            mailboxItem.setFieldValue(FIELD_DMSG_SERIES_NO, seriesNo, DBActionOrigin.FROM_EDIT);
        }
        mailboxItem.setFieldValue(FIELD_DMSG_SUBJECT, subject, DBActionOrigin.FROM_EDIT);
        mailboxItem.setFieldValue(FIELD_DMSG_MESSAGE, message, DBActionOrigin.FROM_EDIT);
        mailboxItem.setFieldValue(FIELD_DMSG_CREATION_USER_ID, creationUser, DBActionOrigin.FROM_EDIT);
        mailboxItem.setFieldValue(FIELD_DMSG_CREATION_DATE, DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss),
                                  DBActionOrigin.FROM_EDIT);

        return mailboxItem;
    }

    /**
     * Überprüft, ob eine Nachricht mit den übergebenen Daten existiert.
     *
     * @param project
     * @param messageType
     * @param dataObjectId
     * @return
     */
    public static boolean mailboxItemExists(EtkProject project, MailboxMessageType messageType, IdWithType dataObjectId) {
        return project.getEtkDbs().getRecordExists(TABLE_DA_MESSAGE, new String[]{ FIELD_DMSG_TYPE, FIELD_DMSG_DO_TYPE, FIELD_DMSG_DO_ID },
                                                   new String[]{ messageType.name(), dataObjectId.getType(), dataObjectId.toDBString() });
    }

    /**
     * Lässt nur noch Nachrichten übrig, bei denen die Parameter zu den zuvor gesetzten Parametern des Benutzers als Empfänger
     * passen. Falls bei einer Nachricht der entsprechende Parameter leer ist, wird er nicht geprüft.
     *
     * @param inputList Ungefilterte Eingangsliste
     * @return Eine Liste der für den Benutzer gültigen Nachrichten
     */
    public Set<MailboxObject> filterRelevantReceivedMessagesForCurrentUser(Collection<MailboxObject> inputList) {
        Set<MailboxObject> result = createEmptyMailboxObjectsSet();
        if (isUserDataValid() && (inputList != null)) {
            for (MailboxObject mailboxObject : inputList) {
                if (isRelevantForCurrentUserAsReceiver(mailboxObject)) {
                    result.add(mailboxObject);
                }
            }
        }
        return result;
    }

    /**
     * Lässt nur noch Nachrichten übrig, bei denen die Parameter zu den zuvor gesetzten Parametern des Benutzers als Sender
     * passen. Falls bei einer Nachricht der entsprechende Parameter leer ist, wird er nicht geprüft.
     *
     * @param inputList Ungefilterte Eingangsliste
     * @return Eine Liste der für den Benutzer gültigen Nachrichten
     */
    public Set<MailboxObject> filterRelevantSentMessagesForCurrentUser(Collection<MailboxObject> inputList) {
        Set<MailboxObject> result = createEmptyMailboxObjectsSet();
        if (isUserDataValid() && (inputList != null)) {
            for (MailboxObject mailboxObject : inputList) {
                if (isRelevantForCurrentUserAsSender(mailboxObject)) {
                    result.add(mailboxObject);
                }
            }
        }
        return result;
    }

    /**
     * Filtert alle Nachrichten, die im Event als geändert übertragen wurden nach Relevanz für den aktuellen Benutzer als Empfänger.
     * Dazu werden die zuvor gesetzten Parameter (Benutzer-ID, Rolle, Organisation und virtuelle Benutzergruppe) herangezogen.
     *
     * @param event
     * @return
     */
    public Set<String> filterRelevantReceivedMessagesFromEvent(iPartsMailboxChangedEvent event) {
        Set<String> relevantGUIDs = new HashSet<>();
        if (isUserDataValid()) {
            for (Map.Entry<String, List<iPartsMailboxChangedEvent.iPartsMailboxChangedRecipient>> mailboxItems : event.getMailboxItems().entrySet()) {
                String messageGUID = mailboxItems.getKey();
                if (!relevantGUIDs.contains(messageGUID)) {
                    List<iPartsMailboxChangedEvent.iPartsMailboxChangedRecipient> recipients = mailboxItems.getValue();
                    for (iPartsMailboxChangedEvent.iPartsMailboxChangedRecipient recipient : recipients) {
                        if (isRelevantForCurrentUserAsReceiver(recipient.getUserId(), recipient.getVirtualUserGroupId(),
                                                               recipient.getOrganisationId(), recipient.getRoleId())) {
                            relevantGUIDs.add(messageGUID);
                            break;
                        }
                    }
                }
            }
        }
        return relevantGUIDs;
    }

    private boolean isRelevantForCurrentUserAsReceiver(MailboxObject mailboxObject) {
        if (isUserDataValid()) {
            String itemUserId = mailboxObject.recipient.getUserID();
            String itemGroupID = mailboxObject.recipient.getGroupID();
            String itemOrganisationID = mailboxObject.recipient.getOrganisationID();
            String itemRoleID = mailboxObject.recipient.getRoleID();
            return isRelevantForCurrentUserAsReceiver(itemUserId, itemGroupID, itemOrganisationID, itemRoleID);
        }
        return false;
    }

    private boolean isRelevantForCurrentUserAsReceiver(String itemUserId, String itemGroupID, String itemOrganisationID, String itemRoleID) {
        if (isUserDataValid()) {
            if (StrUtils.isEmpty(itemUserId) || itemUserId.equals(userId)) {
                if (StrUtils.isEmpty(itemGroupID) || virtualUserGroups.contains(itemGroupID)) {
                    if (StrUtils.isEmpty(itemOrganisationID) || itemOrganisationID.equals(organisationId)) {
                        if (StrUtils.isEmpty(itemRoleID) || roleIds.contains(itemRoleID)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Filtert alle Nachrichten, die im Event als geändert übertragen wurden nach Relevanz für den aktuellen Benutzer als
     * Sender. Dazu wird die zuvor gesetzte Benutzer-ID herangezogen.
     *
     * @param event
     * @return
     */
    public Set<String> filterRelevantSentMessagesFromEvent(iPartsMailboxChangedEvent event) {
        Set<String> relevantGUIDs = new HashSet<>();
        if (isUserDataValid()) {
            for (Map.Entry<String, List<iPartsMailboxChangedEvent.iPartsMailboxChangedRecipient>> mailboxItems : event.getMailboxItems().entrySet()) {
                String messageGUID = mailboxItems.getKey();
                if (!relevantGUIDs.contains(messageGUID)) {
                    List<iPartsMailboxChangedEvent.iPartsMailboxChangedRecipient> recipients = mailboxItems.getValue();
                    for (iPartsMailboxChangedEvent.iPartsMailboxChangedRecipient recipient : recipients) {
                        if (Utils.objectEquals(recipient.getCreationUserId(), userId)) {
                            relevantGUIDs.add(messageGUID);
                            break;
                        }
                    }
                }
            }
        }
        return relevantGUIDs;
    }

    private boolean isRelevantForCurrentUserAsSender(MailboxObject mailboxObject) {
        if (isUserDataValid()) {
            String creationUserId = mailboxObject.mailboxItem.getFieldValue(FIELD_DMSG_CREATION_USER_ID);
            return Utils.objectEquals(creationUserId, userId);
        }
        return false;
    }

    /**
     * Wandelt alle Einträge aus der <i>inputList</i> in MailboxObjects um und fügt diese der <i>TargetList</i> hinzu
     *
     * @param inputList
     * @param targetList
     */
    public static void addAll(iPartsDataMailboxRecipientList inputList, Collection<MailboxObject> targetList) {
        for (iPartsDataMailboxRecipient mailboxRecipient : inputList) {
            targetList.add(new MailboxObject(mailboxRecipient));
        }
    }

    /**
     * Ermittelt, ob alle in der übergebenen Selektion enthaltenen Nachrichten den gleichen gelesen-Status haben.
     * Falls das der Fall ist, wird der Status in <code>resultIsRead</code> zurückgeliefert.
     *
     * @param multiSelection Die aktuelle mehrzeilige Selektion
     * @param resultIsRead   <code>true</code> falls alle Nachrichten gelesen sind;
     *                       <code>false</code> falls alle Nachrichten ungelesen sind
     *                       bei Mischzustand wird der Parameter nicht gesetzt
     * @return <code>true</code> wenn die Selektion einheitlich ist, sonst <code>false</code>
     */
    public static boolean isDistinctValidReadState(List<List<EtkDataObject>> multiSelection, VarParam<Boolean> resultIsRead) {
        if ((multiSelection == null) || multiSelection.isEmpty()) {
            return false;
        }
        int countRead = 0;
        int countUnread = 0;
        for (List<EtkDataObject> dataObjects : multiSelection) {
            iPartsDataMailboxRecipient mailboxRecipient = iPartsDataMailboxRecipient.getMailboxRecipient(dataObjects);
            if (mailboxRecipient != null) {
                if (mailboxRecipient.isMessageRead()) {
                    countRead++;
                } else {
                    countUnread++;
                }
            }
        }
        if ((countRead == 0) && (countUnread > 0)) {
            if (resultIsRead != null) {
                resultIsRead.setValue(false);
            }
            return true;
        } else if ((countUnread == 0) && (countRead > 0)) {
            if (resultIsRead != null) {
                resultIsRead.setValue(true);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Nachrichten können auch von einem Technischen Benutzer kommen -> Einfach den Namen übernehmen
     * Sonst nach vollständigen Namen des Benutzers suchen
     *
     * @param userName Namens-Kürzel oder technischer Benutzer
     * @param language Datenbank-Sprache
     * @return gefundener Name
     */
    public static String getFullUserNameOrTechnicalUser(String userName, String language) {
        String fullUserName = iPartsUserAdminCache.getUserFullNameByUserName(userName, language);
        if (iPartsConst.TECHNICAL_USERS.contains(userName)) {
            iPartsUserAdminCacheElement userCache = iPartsUserAdminCache.getCacheByUserName(userName);
            if (userCache == null) {
                return userName; // Bei technischen Benutzern Fallback auf den Benutzernamen falls der Benutzer nicht in der Benutzerverwaltung existiert
            }
        }
        return fullUserName;
    }

    /**
     * Lädt alle relevanten Nachrichten für den aktuellen Benutzer absteigend sortiert nach Datum. Je nach Flag inkl. der
     * bereits gelesenen.
     *
     * @param project
     * @param loadReadMessages sollen auch gelesene Nachrichten geladen werden?
     * @return
     */
    public Set<MailboxObject> loadAllMessagesForCurrentUser(EtkProject project, boolean loadReadMessages) {
        Set<MailboxObject> mailboxObjects = createEmptyMailboxObjectsSet();
        if (isUserDataValid()) {
            iPartsDataMailboxRecipientList list = new iPartsDataMailboxRecipientList();
            // Alle Nachrichten laden, die entweder an den Benutzer, eine seiner virtuellen Benutzergruppen oder an seine
            // Organisation adressiert sind
            list.loadAllMailboxMessagesForUserOrGroupOrOrg(project, userId, virtualUserGroups, organisationId, loadReadMessages);
            addAll(list, mailboxObjects);

            // Alle Nachrichten laden, die an die Rolle des Benutzers oder an seine Rolle und die Organisation adressiert sind
            list.loadAllMailboxMessagesForOrgAndOrRole(project, organisationId, roleIds, loadReadMessages);
            addAll(list, mailboxObjects);

            mailboxObjects = filterRelevantReceivedMessagesForCurrentUser(mailboxObjects);
        }
        return mailboxObjects;
    }

    /**
     * Lädt alle vom aktuellen Benutzer gesendeten Nachrichten absteigend sortiert nach Datum. Je nach Flag inkl. der
     * bereits gelesenen.
     *
     * @param project
     * @param loadReadMessages sollen auch gelesene Nachrichten geladen werden?
     * @return
     */
    public Set<MailboxObject> loadAllSentMessagesForCurrentUser(EtkProject project, boolean loadReadMessages) {
        Set<MailboxObject> mailboxObjects = createEmptyMailboxObjectsSet();
        // Gesendete Nachrichten nur laden, wenn der Benutzer das Recht zum Senden hat
        if (isUserDataValid() && iPartsRight.CREATE_MAILBOX_MESSAGES.checkRightInSession()) {
            iPartsDataMailboxRecipientList list = new iPartsDataMailboxRecipientList();
            // Alle Nachrichten laden, die der Benutzer versendet hat
            list.loadAllMailboxMessagesForCreator(project, userId, loadReadMessages);
            addAll(list, mailboxObjects);

            mailboxObjects = filterRelevantSentMessagesForCurrentUser(mailboxObjects);
        }
        return mailboxObjects;
    }

    public Set<MailboxObject> createEmptyMailboxObjectsSet() {
        return new TreeSet<>((o1, o2) -> {
            // Neueste Nachrichten zuerst
            int comparison = -o1.mailboxItem.getFieldValue(FIELD_DMSG_CREATION_DATE).
                    compareTo(o2.mailboxItem.getFieldValue(FIELD_DMSG_CREATION_DATE));
            // bei gleichem Datum nach GUID sortieren, damit die Prüfung auf Gleichheit vom Set korrekt funktioniert
            if (comparison == 0) {
                comparison = o1.mailboxItem.getMsgGuid().compareTo(o2.mailboxItem.getMsgGuid());
            }
            return comparison;
        });
    }


    /**
     * Enthält ein {@link iPartsDataMailboxItem} und ein {@link iPartsDataMailboxRecipient}
     */
    public static class MailboxObject {

        public iPartsDataMailboxItem mailboxItem;
        public iPartsDataMailboxRecipient recipient;

        MailboxObject(iPartsDataMailboxRecipient dataMailboxRecipient) {
            iPartsMailboxItemId iPartsMailboxItemId = new iPartsMailboxItemId(dataMailboxRecipient.getFieldValue(FIELD_DMT_GUID));
            mailboxItem = new iPartsDataMailboxItem(dataMailboxRecipient.getEtkProject(), iPartsMailboxItemId);
            mailboxItem.assignAttributes(dataMailboxRecipient.getEtkProject(), dataMailboxRecipient.getAttributes(), false, DBActionOrigin.FROM_DB);
            mailboxItem.removeForeignTablesAttributes();

            recipient = dataMailboxRecipient;
            recipient.removeForeignTablesAttributes();
        }


        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MailboxObject) {
                MailboxObject mailboxObject = (MailboxObject)(obj);
                return mailboxObject.mailboxItem.getMsgGuid().equals(mailboxItem.getMsgGuid());
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return mailboxItem.getMsgGuid().hashCode();
        }
    }
}
