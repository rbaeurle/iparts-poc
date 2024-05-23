/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.xml;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReferenceList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQPicScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsSvgOutlineResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.ImageFileImporterResult;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.event.AbstractXMLChangeEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLMediaContainerCreateOrModifyResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLResponseOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLGetMediaPreview;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.attachments.iPartsDataPicOrderAttachment;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsDataASPLMGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.user.iPartsDataASPLMUser;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.PicOrderStatusChangeEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.PicOrderStateMachine;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;

import java.util.*;

/**
 * Verwaltung aller zentralen iParts Edit {@link AbstractXMLMessageListener}s.
 */
public class iPartsEditXMLMessageListeners implements iPartsConst {

    private static iPartsEditXMLMessageListeners instance;

    private AbstractXMLMessageListener createPicOrderXMLMessageListener;
    private AbstractXMLMessageListener getPicContentXMLMessageListener;
    private AbstractXMLMessageListener updatePicOrderAssingmentListener;
    private AbstractXMLMessageListener updatePicOrderStateListener;

    public static iPartsEditXMLMessageListeners getInstance() {
        if (instance == null) {
            instance = new iPartsEditXMLMessageListeners();
        }
        return instance;
    }

    private iPartsEditXMLMessageListeners() {
    }

    public void registerXMLMessageListeners(final EtkProject project, final Session session) {
        // XMLMessageListener für Bildaufträge
        createPicOrderXMLMessageListener = new AbstractXMLMessageListener(session) {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
                    setPicOrderStatusFromResponse(project, (iPartsXMLMediaMessage)xmlMQMessage);
                }
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).addXMLMessageListenerForMessageTypes(createPicOrderXMLMessageListener,
                                                                                                                             iPartsTransferNodeTypes.CREATE_MEDIA_ORDER,
                                                                                                                             iPartsTransferNodeTypes.CREATE_MC_ATTACHMENTS,
                                                                                                                             iPartsTransferNodeTypes.CORRECT_MEDIA_ORDER,
                                                                                                                             iPartsTransferNodeTypes.CHANGE_MEDIA_ORDER,
                                                                                                                             iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS,
                                                                                                                             iPartsTransferNodeTypes.UPDATE_MEDIA_ORDER,
                                                                                                                             iPartsTransferNodeTypes.ACCEPT_MEDIA_CONTAINER,
                                                                                                                             iPartsTransferNodeTypes.ABORT_MEDIA_ORDER);
        // XMLMessageListener für ResGetMediaContents
        getPicContentXMLMessageListener = new AbstractXMLMessageListener(session) {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
                    savePicContentFromResponse(project, (iPartsXMLMediaMessage)xmlMQMessage);
                }
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).addXMLMessageListenerForMessageTypes(getPicContentXMLMessageListener,
                                                                                                                             iPartsTransferNodeTypes.GET_MEDIA_CONTENTS,
                                                                                                                             iPartsTransferNodeTypes.GET_MEDIA_PREVIEW);
        //XMLMessageListener für EventAssignmentChange
        updatePicOrderAssingmentListener = new AbstractXMLMessageListener(session) {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
                    iPartsXMLMediaMessage mediaMessage = (iPartsXMLMediaMessage)xmlMQMessage;
                    if (mediaMessage.isEvent()) {
                        updatePicOrderAssignmentFromEvent(project, mediaMessage);
                    }
                }
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).addXMLMessageListenerForMessageTypes(updatePicOrderAssingmentListener,
                                                                                                                             iPartsTransferNodeTypes.EVENT_ASSIGNMENT_CHANGE);

        // XMLMessageListener für EventReleaseStatusChange (ToBeDescribed und Released)
        updatePicOrderStateListener = new AbstractXMLMessageListener(session) {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
                    iPartsXMLMediaMessage mediaMessage = (iPartsXMLMediaMessage)xmlMQMessage;
                    if (mediaMessage.isEvent()) {
                        updatePicOrderStateFromEvent(project, mediaMessage);
                    }
                }
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).addXMLMessageListenerForMessageTypes(updatePicOrderStateListener,
                                                                                                                             iPartsTransferNodeTypes.EVENT_RELEASE_STATUS_CHANGE);
    }

    /**
     * Callback für Event-basierten Statusänderungen von AS-PLM an Bildaufträgen
     *
     * @param project
     * @param mediaMessage
     */
    private void updatePicOrderStateFromEvent(EtkProject project, iPartsXMLMediaMessage mediaMessage) {
        if (mediaMessage.isNotificationOnly()) {
            return; // keine DB-Aktionen
        }

        if (mediaMessage.isEvent()) {
            AbstractXMLChangeEvent actualEvent = ((iPartsXMLEvent)mediaMessage.getTypeObject()).getActualEvent();
            if (actualEvent instanceof iPartsXMLEventReleaseStatusChange) {
                iPartsXMLEventReleaseStatusChange event = (iPartsXMLEventReleaseStatusChange)((iPartsXMLEvent)mediaMessage.getTypeObject()).getActualEvent();
                iPartsXMLTcObject tcObject = event.getTcObject();
                if (tcObject.hasAtLeastOneEmptyValue()) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "EventReleaseStatusChange: Received TC object (MediaContainer) contains empty values. MC item ID: '"
                                                                               + tcObject.getMcItemId() + "'; MC item rev ID: '"
                                                                               + tcObject.getMcItemRevId() + "'");
                    return;
                }
                // Neues Objekt, dass den Event auseinander nimmt
                ReleaseEventToTransferState eventToTransfer = new ReleaseEventToTransferState(project, event);
                if (isValidASPLMUserForOrderStateChange(project, iPartsDataASPLMUser.getUserByUserId(project, event.getUserId()))) {
                    if (!eventToTransfer.isKnownEvent()) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Unknown EventReleaseStatusChange! "
                                                                                   + "New: " + event.getNewStatusAsString() + "; "
                                                                                   + "Old: " + event.getOldStatusAsString());
                    } else {
                        handleKnownEventStatus(eventToTransfer);
                        fireOnlyNotifcationMessage(mediaMessage);
                    }
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Invalid user for EventReleaseStatusChange: " + event.getUserId());
                }
            }
        }
    }

    /**
     * Verarbeitet die Event Status, die für den iParts Workflow relevant sind (supplied, rejected und obsolete)
     *
     * @param eventToTransfer
     */
    private void handleKnownEventStatus(ReleaseEventToTransferState eventToTransfer) {
        if (eventToTransfer.hasValidTransferState()) {
            // Verarbeite einen Event, der einem iPartsTransferState zugewiesen ist
            handleValidEventState(eventToTransfer);
        } else {
            // Verarbeite einen Event, der keinem iPartsTransferState zugewiesen ist
            handleKnownEventWithoutTransferState(eventToTransfer);
        }
    }

    private void handleKnownEventWithoutTransferState(ReleaseEventToTransferState eventToTransfer) {
        // Für die Eventtypen "toBeDescribed" und "Released" (wenn neues Verfahren aktiv) gibt es noch keine
        // Verfahrensanweisung, daher werden die Eventname in die DB geschrieben
        iPartsDataPicOrderList list = eventToTransfer.getValidPicordersFromDB();
        for (iPartsDataPicOrder dataPicOrder : list) {
            dataPicOrder.setEventNameForInvalidState(eventToTransfer.getEvent().getNewEventState());
            // Verarbeiten der Kommentare aus einem Event
            transferCommentMessage(eventToTransfer.getProject(), dataPicOrder, eventToTransfer.getEvent().getTcObject(),
                                   eventToTransfer.getEvent().getComments());
            saveOrderToDB(eventToTransfer.getProject(), dataPicOrder);
        }
    }

    /**
     * Feuert die übergebene {@link iPartsXMLMediaMessage} Nachricht als Event in alle Cluster und Projekte. Zuvor wird
     * die Nachricht als "notificationOnly" markiert, damit der original Payload nicht mitgeschickt wird.
     *
     * @param mediaMessage
     */
    private void fireOnlyNotifcationMessage(iPartsXMLMediaMessage mediaMessage) {
        // Mitteilung an alle anderen Cluster verschicken
        mediaMessage.convertToNotificationOnly();
        ApplicationEvents.fireEventInAllProjectsAndClusters(new iPartsXMLMessageEvent(mediaMessage, iPartsMQChannelTypeNames.MEDIA),
                                                            false, false, true, null, null);
    }

    /**
     * Verarbeitet einen validen Eventstatus von AS-PLM. Die dazugehörigen Bildaufträge geladen und der übergebene Status
     * gesetzt. Es wird der Kommentar von AS-PLM gesetzt und die Bildaufträge gespeichert. Zum Schluss wird geprüft, ob
     * irgendwelche vom Status abhängige Operationen abgeschickt werden müssen (z.B. Bilder anfragen).
     *
     * @param eventToTransfer
     */
    private void handleValidEventState(ReleaseEventToTransferState eventToTransfer) {
        iPartsDataPicOrderList list = eventToTransfer.getValidPicordersFromDB();
        for (iPartsDataPicOrder dataPicOrder : list) {
            // Darf der Event den Status des Bildauftrags ändern?
            if (eventToTransfer.canEventChangeOrderState(dataPicOrder)) {
                dataPicOrder.setStatus(eventToTransfer.getTransferState(), DBActionOrigin.FROM_EDIT);
                eventToTransfer.handleStatusChanged(dataPicOrder);
            } else {
                iPartsXMLTcObject tcObject = eventToTransfer.getEvent().getTcObject();
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "EventReleaseStatusChange: Received " +
                                                                           "event is not valid for current picture order state. MC item ID: '" +
                                                                           tcObject.getMcItemId() + "'; MC item rev ID: '" +
                                                                           tcObject.getMcItemRevId() + "'; Picture order state: " +
                                                                           dataPicOrder.getStatus().getDBValue() + "; new event state: " +
                                                                           ((eventToTransfer.getNewEventState() != null) ? eventToTransfer.getNewEventState().getAsplmValue()
                                                                                                                         : eventToTransfer.getEvent().getNewStatusAsString()));
            }
            dataPicOrder.clearEventNameForInvalidState();
            // Verarbeiten der Kommentare aus einem Event
            transferCommentMessage(eventToTransfer.getProject(), dataPicOrder, eventToTransfer.getEvent().getTcObject(), eventToTransfer.getEvent().getComments());
            saveOrderToDB(eventToTransfer.getProject(), dataPicOrder);
            handleMQOperationDependencies(eventToTransfer.getProject(), dataPicOrder);
        }
    }

    /**
     * Hilfsklasse, die alle Informationen zu einen AS-PLM Event hält
     */
    private static class ReleaseEventToTransferState {

        private EtkProject project;
        private iPartsXMLEventReleaseStatusChange event;
        private iPartsTransferStates transferState;

        public ReleaseEventToTransferState(EtkProject project, iPartsXMLEventReleaseStatusChange event) {
            this.project = project;
            this.event = event;
            transferState = getTransferStateForEventState();
        }

        /**
         * Liefert den zugehörigen {@link iPartsTransferStates} für den übergebenen {@link iPartsEventStates}
         *
         * @return
         */
        private iPartsTransferStates getTransferStateForEventState() {
            switch (event.getNewEventState()) {
                case RELEASED:
                    if (MQHelper.getInstance().isUseMQReleasedStateAsSuppliedState()) {
                        return iPartsTransferStates.getReleasedState();
                    }
                    break;
                case SUPPLIED:
                    return iPartsTransferStates.getReleasedState();
                case REJECTED:
                    return iPartsTransferStates.getRejectedState();
                case OBSOLETE:
                    return iPartsTransferStates.getCancelConfirmState();
            }
            return null;
        }

        /**
         * Handelt es sich um einen uns bekannten Event
         *
         * @return
         */
        public boolean isKnownEvent() {
            return getNewEventState() != iPartsEventStates.UNKNOWN;
        }

        /**
         * Liefert den neuen Status aus dem Event
         *
         * @return
         */
        public iPartsEventStates getNewEventState() {
            return getEvent().getNewEventState();
        }

        public iPartsTransferStates getTransferState() {
            return transferState;
        }

        public EtkProject getProject() {
            return project;
        }

        public iPartsXMLEventReleaseStatusChange getEvent() {
            return event;
        }

        /**
         * Ist der Event mit einem {@link iPartsTransferStates} verknüpft
         *
         * @return
         */
        public boolean hasValidTransferState() {
            return getTransferState() != null;
        }

        /**
         * Liefert alle gültigen Bildaufträge zum übergebenen Event
         *
         * @return
         */
        private iPartsDataPicOrderList getValidPicordersFromDB() {
            iPartsXMLTcObject tcObject = event.getTcObject();
            iPartsDataPicOrderList list = iPartsDataPicOrderList.loadValidPicOrderListForTcObject(project, tcObject);
            if (list.isEmpty()) {
                iPartsEventStates newEventState = event.getNewEventState();
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "EventReleaseStatusChange: Received TC object (MediaContainer) does not match an existing MediaOrder in the database. MC item ID: '"
                                                                           + tcObject.getMcItemId() + "'; MC item rev ID: '"
                                                                           + tcObject.getMcItemRevId() + "'; new state: "
                                                                           + ((newEventState != null) ? newEventState.getAsplmValue()
                                                                                                      : event.getNewStatusAsString() + " (unknown)"));
            }
            return list;
        }

        /**
         * Liefert zurück, ob der Event den Status des übergebenen Bildafutrags ändern darf
         *
         * @param dataPicOrder
         * @return
         */
        private boolean canEventChangeOrderState(iPartsDataPicOrder dataPicOrder) {
            iPartsEventStates newEventState = event.getNewEventState();
            iPartsTransferStates currentState = dataPicOrder.getStatus();
            switch (newEventState) {
                case OBSOLETE:
                    return iPartsTransferStates.isCancelState(currentState);
                case SUPPLIED:
                case REJECTED:
                    // Bilder holen und Zurückweisen ist nur möglich bei "angenommen", "mit Anhängen angenommen", "Änderung angenommen", "In Bearbeitung" und "Korrekturanfrage angenommen"
                    return iPartsTransferStates.isReleasedOrRejectionState(currentState) || (currentState == iPartsTransferStates.CORRECTION_MO_RECEIVED);
                case RELEASED:
                    if (MQHelper.getInstance().isUseMQReleasedStateAsSuppliedState()) {
                        return iPartsTransferStates.isReleasedOrRejectionState(currentState);
                    }
                    break;
            }
            return false;
        }

        /**
         * Verarbeitet die Statusänderung am Bildauftrag und passt die Daten des Auftrags an
         *
         * @param dataPicOrder
         */
        public void handleStatusChanged(iPartsDataPicOrder dataPicOrder) {
            if (getNewEventState() == iPartsEventStates.REJECTED) {
                // Erst alle Anhänge aufsammeln, die von anderen Bildaufträgen angehängt wurden
                Set<String> attachmentsFromPreviousPicOrders = new HashSet<>();
                iPartsDataPicOrder previousPicOrder = new iPartsDataPicOrder(getProject(), new iPartsPicOrderId(dataPicOrder.getOriginalPicOrder()));
                while (StrUtils.isValid(previousPicOrder.getAsId().getOrderGuid()) && previousPicOrder.existsInDB()) {
                    for (iPartsDataPicOrderAttachment attachment : previousPicOrder.getAttachments()) {
                        attachmentsFromPreviousPicOrders.add(attachment.getAsId().getAttachmentGuid());
                    }
                    previousPicOrder = new iPartsDataPicOrder(getProject(), new iPartsPicOrderId(previousPicOrder.getOriginalPicOrder()));
                }
                // Nur die Anhänge zurücksetzen, die vom aktuellen Auftrag erzeugt wurden
                for (iPartsDataPicOrderAttachment attachment : dataPicOrder.getAttachments()) {
                    if (!attachmentsFromPreviousPicOrders.contains(attachment.getAsId().getAttachmentGuid())) {
                        attachment.setStatus(iPartsAttachmentStatus.NEW);
                    }
                }
            }
        }
    }

    /**
     * Überprüfung vom optionalen AS-PLM Benutzer auf Berechtigung für einen EventReleaseStatusChange.
     *
     * @param project
     * @param userData
     * @return
     */
    private boolean isValidASPLMUserForOrderStateChange(EtkProject project, iPartsDataASPLMUser userData) {
        // TODO Überprüfen, ob die Berechtigung vom Benutzer passt
        return true;
    }

    /**
     * Callback für EventAssignmentChange
     *
     * @param project
     * @param mediaMessage
     */
    private void updatePicOrderAssignmentFromEvent(EtkProject project, iPartsXMLMediaMessage mediaMessage) {
        if (mediaMessage.isNotificationOnly()) {
            return; // keine DB-Aktionen
        }

        if (mediaMessage.isEvent()) {
            iPartsXMLEventAssingmentChange event = (iPartsXMLEventAssingmentChange)((iPartsXMLEvent)mediaMessage.getTypeObject()).getActualEvent();
            iPartsXMLTcObject tcObject = event.getTcObject();
            if (tcObject.hasAtLeastOneEmptyValue()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "EventAssignmentChange: Received TC object (MediaContainer) contains empty values. MC item ID: '"
                                                                           + tcObject.getMcItemId() + "'; MC item rev ID: '"
                                                                           + tcObject.getMcItemRevId() + "'");
                return;
            }
            String groupId = event.getGroupId();
            String userId = event.getUserId();

            // Gruppen und Benutzer DBObjects aus den IDs bestimmen, um die GUIDs zu bekommen
            iPartsDataASPLMGroup groupData = iPartsDataASPLMGroup.getGroupByGroupId(project, groupId);
            iPartsDataASPLMUser userData = null;
            boolean userIdExists = (userId != null) && !userId.isEmpty();
            if (userIdExists) {
                userData = iPartsDataASPLMUser.getUserByUserId(project, userId);
            }

            if (isValidASPLMGroupAndUserForAssignmentChange(project, groupData, userData, userIdExists)) {
                iPartsDataPicOrderList list = iPartsDataPicOrderList.loadPicOrderListForTcObject(project, tcObject);
                if (list.isEmpty()) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "EventAssignmentChange: Received TC object (MediaContainer) does not match an existing MediaOrder in the database. MC item ID: '"
                                                                               + tcObject.getMcItemId() + "'; MC item rev ID: '"
                                                                               + tcObject.getMcItemRevId() + "'; new groupId / userId: '"
                                                                               + groupId + "' / " + ((userId != null) ?
                                                                                                     "'" + userId + "'" :
                                                                                                     "[ NONE ]"));
                }
                for (iPartsDataPicOrder dataPicOrder : list) {
                    String dateTime = XMLImportExportDateHelper.getISOFormattedDateTimeAsString(GregorianCalendar.getInstance().getTime());
                    dataPicOrder.setInfoTextForAllLanguages(TranslationHandler.translate("!!Event: Auftragnehmer/Gruppe hat sich geändert. Zeitpunkt: %1", dateTime));

                    dataPicOrder.setFieldValue(FIELD_DA_PO_JOB_GROUP, groupData.getAsId().getGroupGuid(), DBActionOrigin.FROM_EDIT);
                    if (userData != null) {
                        dataPicOrder.setFieldValue(FIELD_DA_PO_JOB_USER, userData.getAsId().getUserGuid(), DBActionOrigin.FROM_EDIT);
                    } else {
                        dataPicOrder.setFieldValue(FIELD_DA_PO_JOB_USER, "", DBActionOrigin.FROM_EDIT);
                    }
                    // Verarbeiten der Kommentare aus einem Event
                    transferCommentMessage(project, dataPicOrder, tcObject, event.getComments());

                    saveOrderToDB(project, dataPicOrder);
                }

                // Mitteilung an alle anderen Cluster verschicken
                mediaMessage.convertToNotificationOnly();
                ApplicationEvents.fireEventInAllProjectsAndClusters(new iPartsXMLMessageEvent(mediaMessage, iPartsMQChannelTypeNames.MEDIA),
                                                                    false, false, true, null, null);
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Invalid group and/or user for EventAssignmentChange: '"
                                                                           + groupId + "' / " + ((userId != null) ?
                                                                                                 "'" + userId + "'" :
                                                                                                 "[ NONE ]"));
            }
        }
    }

    /**
     * Überprüfung der AS-PLM Gruppe und vom optionalen Benutzer auf Gültigkeit und Berechtigung für einen EventAssignmentChange.
     *
     * @param project
     * @param groupData
     * @param userData
     * @param checkUser
     * @return
     */
    private boolean isValidASPLMGroupAndUserForAssignmentChange(EtkProject project, iPartsDataASPLMGroup groupData, iPartsDataASPLMUser userData,
                                                                boolean checkUser) {
        // TODO Überprüfen, ob die Gruppe gesetzt ist und der Benutzer (inkl. Berechtigung) passt
        return (groupData != null) && (!checkUser || (userData != null));
    }

    public void deregisterXMLMessageListeners() {
        if (createPicOrderXMLMessageListener != null) {
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).removeXMLMessageListenerForMessageTypes(createPicOrderXMLMessageListener,
                                                                                                                                    iPartsTransferNodeTypes.CREATE_MEDIA_ORDER,
                                                                                                                                    iPartsTransferNodeTypes.CREATE_MC_ATTACHMENTS,
                                                                                                                                    iPartsTransferNodeTypes.CORRECT_MEDIA_ORDER,
                                                                                                                                    iPartsTransferNodeTypes.CHANGE_MEDIA_ORDER,
                                                                                                                                    iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS,
                                                                                                                                    iPartsTransferNodeTypes.UPDATE_MEDIA_ORDER,
                                                                                                                                    iPartsTransferNodeTypes.ACCEPT_MEDIA_CONTAINER,
                                                                                                                                    iPartsTransferNodeTypes.ABORT_MEDIA_ORDER);
            createPicOrderXMLMessageListener = null;
        }
    }

    /**
     * Setzt den Status des Bildauftrages in Abhängigkeit von der Atnwort von AS-PLM.
     *
     * @param project
     * @param message - die OO Repräsentation der Antwort-XML von AS-PLM
     */
    public void setPicOrderStatusFromResponse(EtkProject project, iPartsXMLMediaMessage message) {
        if (message.isNotificationOnly()) {
            return; // keine DB-Aktionen
        }

        if (message.isResponse()) {
            // Hole die GUID aus der Antwort von ASPLM
            String iPartsRequestId = message.getTypeObject().getiPartsRequestID();
            if ((iPartsRequestId != null) && (!iPartsRequestId.isEmpty())) {
                // den Bildauftrag aus der DB laden
                iPartsPicOrderId picOrderId = new iPartsPicOrderId(iPartsRequestId);
                iPartsDataPicOrder dataPicOrder = new iPartsDataPicOrder(project, picOrderId);
                // Falls PicOrder nicht in der DB ist -> Abbruch
                if (dataPicOrder.loadFromDB(picOrderId)) {
                    //Hole das Response und das Success Objekt
                    iPartsXMLResponse response = (iPartsXMLResponse)message.getTypeObject();
                    iPartsXMLSuccess success = response.getSuccess();

                    // Die Antwort kann Warnungen enthalten (diese zuerst setzen, damit evtl. vorhandene Fehler nicht überschrieben werden)
                    transferWarningMessage(dataPicOrder, iPartsRequestId, success);

                    // Folgestatus
                    iPartsTransferStates status = PicOrderStateMachine.getInstance().getNextState(dataPicOrder, success.isErrorFree());
                    AbstractXMLResponseOperation resultOperation = response.getResult();
                    // Die Antwort enthält keine Fehler
                    switch (response.getRequestOperation()) {
                        case ACCEPT_MEDIA_CONTAINER:
                            if (isResultOperationValid(dataPicOrder, iPartsRequestId, resultOperation, success)) {
                                dataPicOrder.setInfoTextForAllLanguages(TranslationHandler.translate("!!Abschluss des Auftrags bestätitgt" +
                                                                                                     " am %1",
                                                                                                     DateUtils.getCurrentDateFormatted(DateUtils.simpleDateTimeFormatIso)));
                            }
                            break;
                        case ABORT_MEDIA_ORDER:
                            if (success.isErrorFree()) {
                                dataPicOrder.setInfoTextForAllLanguages(TranslationHandler.translate("!!Stornierung bestätitgt" +
                                                                                                     " am %1",
                                                                                                     DateUtils.getCurrentDateFormatted(DateUtils.simpleDateTimeFormatIso)));
                            } else {
                                transferErrorFromSuccess(dataPicOrder, iPartsRequestId, success);
                            }
                            break;
                        case CREATE_MEDIA_ORDER:
                        case CHANGE_MEDIA_ORDER:
                        case UPDATE_MEDIA_ORDER:
                            if (!isResultOperationValid(dataPicOrder, iPartsRequestId, resultOperation, success)) {
                                if (response.getRequestOperation() == iPartsTransferNodeTypes.UPDATE_MEDIA_ORDER) {
                                    status = iPartsTransferStates.UPDATE_ERROR;
                                }
                                break;
                            }
                            // Falls eine CreateMediaOrder/ChangeMediaOrder Antwort empfangen wird und der dazugehörige Bildauftrag nicht den
                            // richtigen Status hat ("VERSORGT" / "Änderungsauftrag verschickt" / "Kopierauftrag verschickt"),
                            // dann wird die Nachricht nicht verarbeitet und ihre Informationen
                            // in die Log-Datei geschrieben (z.B. nachdem ein Bidlauftrag zurückgesetzt wurde)
                            // Der Status bleibt wie er war.
                            if (!iPartsTransferStates.canReceiveMediaOrderResponse(dataPicOrder.getStatus())) {
                                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG,
                                           "Received response refers to a picture order with a state that " +
                                           "does not allow for the receiveing of this type of messages."
                                           + "Order ID: " + dataPicOrder.getAsId().getOrderGuid() + "; "
                                           + "Picture order state: " + dataPicOrder.getStatus() + "; "
                                           + "Filename: " + message.getSavedMQXmlFile().extractFileName(true));
                                status = dataPicOrder.getStatus();
                                break;
                            }
                            AbstractXMLMediaContainerCreateOrModifyResponse rcmo = (AbstractXMLMediaContainerCreateOrModifyResponse)resultOperation;
                            dataPicOrder.setAttributeValue(FIELD_DA_PO_ORDER_ID_EXTERN, rcmo.getMContainer().getMcItemId(), DBActionOrigin.FROM_EDIT);
                            dataPicOrder.setAttributeValue(FIELD_DA_PO_ORDER_REVISION_EXTERN, rcmo.getMContainer().getMcItemRevId(), DBActionOrigin.FROM_EDIT);
                            dataPicOrder.setFieldValue(FIELD_DA_PO_CREATEDATE, rcmo.getMOrder().getDateOrderedAsDBValue(), DBActionOrigin.FROM_EDIT);
                            switch (response.getRequestOperation()) {
                                case CHANGE_MEDIA_ORDER:
                                    // Handelt es sich um einen Änderungsauftrag und die Antwort ist positiv, dann wird der "alte"
                                    // Aus der Liste entfernt. Zusätzlich werden die Originale der verändertden Bilder entfernt.
                                    if (response.getRequestOperation() == iPartsTransferNodeTypes.CHANGE_MEDIA_ORDER) {
                                        if (!handleOriginalPicOrderAfterChange(project, dataPicOrder)) {
                                            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Error while changing " +
                                                                                                       "original PicOrder. " +
                                                                                                       "OrderID: " + iPartsRequestId);
                                            break;
                                        }
                                        handlePicturesAfterChange(project, dataPicOrder, resultOperation);
                                    }
                                    break;
                                case UPDATE_MEDIA_ORDER:
                                    dataPicOrder.deleteComments();
                                    break;
                            }

                            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Response with success. Updating values for OrderID: " + iPartsRequestId
                                                                                       + ", ASPLM OrderID: " + rcmo.getMContainer().getMcItemId()
                                                                                       + ", ASPLM OderRevID: " + rcmo.getMContainer().getMcItemRevId()
                                                                                       + ", ASPLM OrderDate: " + rcmo.getMOrder().getDateOrderedAsDBValue()
                                                                                       + ", new status: " + status.getDBValue());
                            break;
                        case CREATE_MC_ATTACHMENTS:
                            List<iPartsDataPicOrderAttachment> attachmentListFromDB = dataPicOrder.getAttachments();
                            if (resultOperation != null) {
                                // Falls ein Result existiert, dann heißt es, dass mind. 1 Anhang nicht akzeptiert wurde
                                iPartsXMLResCreateMcAttachments rcma = (iPartsXMLResCreateMcAttachments)resultOperation;
                                Map<String, iPartsXMLSuccess> successMap = rcma.getSuccessMap();
                                for (iPartsDataPicOrderAttachment attachment : attachmentListFromDB) {
                                    iPartsXMLSuccess successForAttachment = successMap.get(attachment.getAsId().getAttachmentGuid());
                                    if (response.getSuccess().isErrorFree() || (successForAttachment == null) || successForAttachment.isErrorFree()) {
                                        attachment.setFieldValue(FIELD_DPA_STATUS, iPartsAttachmentStatus.ACCEPTED.getDBStatus(), DBActionOrigin.FROM_EDIT);
                                    } else {
                                        attachment.setFieldValue(FIELD_DPA_ERRORTEXT, successForAttachment.getErrors().get(0).getText(), DBActionOrigin.FROM_EDIT);
                                        attachment.setFieldValue(FIELD_DPA_ERRORCODE, String.valueOf(successForAttachment.getErrorCode()), DBActionOrigin.FROM_EDIT);
                                        attachment.setFieldValue(FIELD_DPA_STATUS, iPartsAttachmentStatus.DENIED.getDBStatus(), DBActionOrigin.FROM_EDIT);
                                    }
                                }
                            } else {
                                // Falls kein Result existiert, dann wurden alle Anhänge akzeptiert
                                for (iPartsDataPicOrderAttachment attachment : attachmentListFromDB) {
                                    attachment.setFieldValue(FIELD_DPA_STATUS, iPartsAttachmentStatus.ACCEPTED.getDBStatus(), DBActionOrigin.FROM_EDIT);
                                }
                            }

                            if (!success.isErrorFree()) {
                                // Die Antwort enthält Fehler
                                transferErrorMessage(dataPicOrder, iPartsRequestId, success);
                            }
                            break;
                        case CORRECT_MEDIA_ORDER:
                            if (!isResultOperationValid(dataPicOrder, iPartsRequestId, resultOperation, success)) {
                                break;
                            }
                            iParstXMLResCorrectMediaOrder rcomo = (iParstXMLResCorrectMediaOrder)resultOperation;
                            dataPicOrder.setFieldValue(FIELD_DA_PO_CREATEDATE, rcomo.getMOrder().getDateOrderedAsDBValue(), DBActionOrigin.FROM_EDIT);
                            break;
                        case SEARCH_MEDIA_CONTAINERS:
                            if (!isResultOperationValid(dataPicOrder, iPartsRequestId, resultOperation, success)) {
                                break;
                            }
                            iPartsXMLMediaContainer mContainer = MQPicScheduler.getInstance().getMediaContainerFromSearch(resultOperation, iPartsRequestId);
                            if (mContainer == null) {
                                // Antwort auf unsere Suchanfrage liefert kein Ergebnis
                                // -> Setze den "Nicht gefunden" Status und schreibe eine zusätzliche Info an den
                                // Änderungsauftrag
                                status = iPartsTransferStates.MC_NUMBER_NOT_FOUND;
                                String picNumber = "";
                                if (dataPicOrder.getPictures().size() == 1) {
                                    iPartsDataPicOrderPicture searchPicture = dataPicOrder.getPictures().get(0);
                                    picNumber = "\"" + searchPicture.getAsId().getPicItemId() + "\" ";
                                }
                                dataPicOrder.setInfoTextForAllLanguages(TranslationHandler.translate("!!Zur Bildnummer %1 wurde kein Mediencontainer gefunden.",
                                                                                                     picNumber));
                                break;
                            }
                            dataPicOrder.deleteErrorAndCommentValues();

                            // MC Nummer und Revision setzen
                            dataPicOrder.setFieldValue(FIELD_DA_PO_ORDER_ID_EXTERN, mContainer.getMcItemId(), DBActionOrigin.FROM_EDIT);
                            dataPicOrder.setFieldValue(FIELD_DA_PO_ORDER_REVISION_EXTERN, mContainer.getMcItemRevId(), DBActionOrigin.FROM_EDIT);

                            break;
                    }
                    checkEventStatus(dataPicOrder, status, response.getRequestOperation(), message);
                    dataPicOrder.setStatus(status, DBActionOrigin.FROM_EDIT);
                    // Neue Werte in die DB speichern
                    saveOrderToDB(project, dataPicOrder);
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "PicOrder values updated in DB. Order ID: " + iPartsRequestId);

                    // Mitteilung an alle anderen Cluster verschicken
                    message.convertToNotificationOnly();
                    ApplicationEvents.fireEventInAllProjectsAndClusters(new iPartsXMLMessageEvent(message, iPartsMQChannelTypeNames.MEDIA),
                                                                        false, false, true, null, null);

                    handleMQOperationDependencies(project, dataPicOrder);
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "PicOrder not found in DB. Order ID: " + iPartsRequestId);
                }
            } else {
                Logger.getLogger().throwRuntimeException("RequestID (iParts GUID) must not be null or empty!");
            }
        } else {
            Logger.getLogger().throwRuntimeException("Message type must be an iPartsXMLResponse! Type is: " + message.getTypeObject().getClass().getName());
        }
    }

    /**
     * Verarbeitet MQ Nachrichten-Abhängigkeiten, z.B. Senden der Anhänge nachdem ein Bildauftrag angenommen wurde
     *
     * @param project
     * @param dataPicOrder
     */
    private void handleMQOperationDependencies(EtkProject project, iPartsDataPicOrder dataPicOrder) {
        if ((dataPicOrder != null) && dataPicOrder.isValid()) {
            iPartsTransferStates currentStatus = dataPicOrder.getStatus();
            if (iPartsTransferStates.canSendAttachments(currentStatus) && dataPicOrder.hasAttachmentsReadyToBeSent()) {
                // Bereit für das Senden von Anhängen
                sendAndStorePicOrderData(project, dataPicOrder, dataPicOrder.getAttachmentMessage(), iPartsTransferStates.ATTACHMENTS_SENT);
            } else if (iPartsTransferStates.canSendGetMediaContents(currentStatus)) {
                sendAndStorePicOrderData(project, dataPicOrder, dataPicOrder.getAsMediaContentsObject(), PicOrderStateMachine.getInstance().getNextState(dataPicOrder, true));
            } else if (currentStatus == iPartsTransferStates.CONFIRMATION_RECEIVED) {
                // Vorschaubilder abschicken, wenn die Bestätigung seitens AS-PLM kam
                iPartsXMLMediaMessage previewMessage = getRequestPreviewMessage(dataPicOrder);
                sendAndStorePicOrderData(project, dataPicOrder, previewMessage, iPartsTransferStates.PREVIEW_REQUESTED);
            } else if ((currentStatus == iPartsTransferStates.CANCEL_CONFIRMATION)) {
                // Bildauftrag wurde storniert -> vorherigen Bildauftrag reaktivieren
                reactivatePreviousPicOrderAfterCancelConfirmation(project, dataPicOrder);
            }
        }
    }

    /**
     * Reaktiviert einen Bildauftragsvorgänger nachdem ein Änderungsauftrag storniert wurde
     *
     * @param project
     * @param dataPicOrder
     */
    private void reactivatePreviousPicOrderAfterCancelConfirmation(EtkProject project, iPartsDataPicOrder dataPicOrder) {
        String cancelledPicOrderGUID = dataPicOrder.getAsId().getOrderGuid();
        // Bei einem Fake-Änderungsauftrag (migrierte Bildtafel) gibt es keinen Vorgänger, den man wiederherstellen kann
        if (dataPicOrder.hasFakeOriginalPicOrder()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Fake picture order for existing " +
                                                                       "picture cancelled. Cancelled picture order: " +
                                                                       cancelledPicOrderGUID);
            return;
        }

        // Existiert kein Vorgänger, dann handelt es sich um einen Initial-Bildauftrag oder einen Fehler. Unterscheiden
        // können wir das an dieser Stelle nicht, daher einfach eine Meldung ausgeben
        String previousOrderGUID = dataPicOrder.getOriginalPicOrder();
        if (StrUtils.isEmpty(previousOrderGUID)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Could not reactivate previous " +
                                                                       "picture order since the GUID of the previous" +
                                                                       " order is empty. Cancelled picture order: " +
                                                                       cancelledPicOrderGUID);
            return;
        }
        // Wurde der Bildauftrag storniert, dann muss der vorherige Auftrag aktiv gesetzt werden
        iPartsDataPicOrder previousOrder = new iPartsDataPicOrder(project, new iPartsPicOrderId(previousOrderGUID));
        if (previousOrder.existsInDB()) {
            previousOrder.setStatus(iPartsTransferStates.PREVIEW_RECEIVED, DBActionOrigin.FROM_EDIT);
            saveOrderToDB(project, previousOrder);
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Previous picture order with GUID " +
                                                                       previousOrderGUID +
                                                                       " reactivated. Cancelled picture order: " +
                                                                       cancelledPicOrderGUID);
            // Der Status des vorherigen Auftrags wurde geändert. Alle Anzeigen aktualisieren, die diesen Auftrag
            // enthalten
            fireOrderUpdate(dataPicOrder);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Error while reactivating previous" +
                                                                       " picture order with GUID " + previousOrderGUID +
                                                                       ". Order could not be found in database." +
                                                                       " Cancelled picture order :" + cancelledPicOrderGUID);
        }
    }

    /**
     * Erstellt die Vorschaubild-Anfrage, die an AS-PLM geschickt wird
     *
     * @param dataPicOrder
     * @return
     */
    public iPartsXMLMediaMessage getRequestPreviewMessage(iPartsDataPicOrder dataPicOrder) {
        iPartsXMLGetMediaPreview gmp = new iPartsXMLGetMediaPreview(dataPicOrder.getOrderIdExtern(), dataPicOrder.getOrderRevisionExtern());
        return iPartsPicOrderEditHelper.createMessageFromOperation(gmp, dataPicOrder.getAsId().getOrderGuid(), iPartsConst.AS_PLM_USER_ID);
    }

    /**
     * Entfernt alle Bilderreferenzen eines Änderungsauftrags. Die Referenzen zu den Bildern, die durch den Änderungsauftrag
     * verändert wurden, werden beim eigentlichen Holen der Bilder (GetMediaContents bzw. ResGetMediaContents) erzeugt.
     * So referenziert der Änderungsauftrag nur auf die Bilder, die er auch tatsächlich verändert hat.
     *
     * @param project
     * @param dataPicOrder
     * @param resultOperation
     */
    private void handlePicturesAfterChange(EtkProject project, iPartsDataPicOrder dataPicOrder, AbstractXMLResponseOperation resultOperation) {
        if (resultOperation.getResultType() != iPartsTransferNodeTypes.RES_CHANGE_MEDIA_ORDER) {
            return;
        }
        if (dataPicOrder.isCopy() || (dataPicOrder.isChangeOrder() && !dataPicOrder.hasFakeOriginalPicOrder())) {
            dataPicOrder.getPictures().deleteFromDB(project);
        }
        dataPicOrder.resetUsedPictures();
    }

    /**
     * Setzt nachdem die Bestätigung des Änderungsauftrags ankam, den Original-Bildauftrag auf den Status "Änderung erzeugt".
     * Dadurch taucht der Auftrag nicht mehr in der Oberfläche auf.
     *
     * @param project
     * @param dataPicOrder
     * @return
     */
    private boolean handleOriginalPicOrderAfterChange(EtkProject project, iPartsDataPicOrder dataPicOrder) {
        // Bei einem Fake Vorgänger und einer Kopie so tun, als ob alles OK ist
        if (dataPicOrder.hasFakeOriginalPicOrder() || dataPicOrder.isCopy()) {
            return true;
        }
        iPartsPicOrderId originalPicOrderId = new iPartsPicOrderId(dataPicOrder.getOriginalPicOrder());
        if (originalPicOrderId.isValidId()) {
            iPartsDataPicOrder originalPicOrder = new iPartsDataPicOrder(project, originalPicOrderId);
            if (originalPicOrder.existsInDB()) {
                originalPicOrder.setStatus(iPartsTransferStates.REPLACED_BY_CHANGE, DBActionOrigin.FROM_EDIT);
                saveOrderToDB(project, originalPicOrder);
                // Der Status des vorherigen Auftrags wurde geändert. Alle Anzeigen aktualisieren, die diesen Auftrag
                // enthalten
                fireOrderUpdate(dataPicOrder);
                return true;
            }
        }
        return false;
    }

    /**
     * Feuert einen {@link PicOrderStatusChangeEvent} weil
     *
     * @param dataPicOrder
     */
    private void fireOrderUpdate(iPartsDataPicOrder dataPicOrder) {
        PicOrderStatusChangeEvent event = new PicOrderStatusChangeEvent(true);
        event.setCurrentOrderId(dataPicOrder.getAsId().getOrderGuid());
        event.setPreviousOrderId(dataPicOrder.getOriginalPicOrder());
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(event);
    }

    private void checkEventStatus(iPartsDataPicOrder dataPicOrder, iPartsTransferStates status, iPartsTransferNodeTypes requestOperation, iPartsXMLMediaMessage message) {
        if (iPartsTransferStates.isEventState(status)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG,
                       "Received a MQ response for a picture order with an event state. "
                       + "Order ID: " + dataPicOrder.getAsId().getOrderGuid() + "; "
                       + "Picture order state: " + dataPicOrder.getStatus() + "; "
                       + "Received request operation: " + requestOperation + ";"
                       + "Filename: " + message.getSavedMQXmlFile().extractFileName(true));
        }
    }

    private boolean isResultOperationValid(iPartsDataPicOrder dataPicOrder, String iPartsRequestId, AbstractXMLResponseOperation resultOperation,
                                           iPartsXMLSuccess success) {
        transferErrorFromSuccess(dataPicOrder, iPartsRequestId, success);
        if (resultOperation == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Result operation must not be null!");
            return false;
        }
        return true;
    }

    /**
     * Schreibt die Fehlermeldung des {@link iPartsXMLSuccess} Objekts als Kommentar an den Bildauftrag
     *
     * @param dataPicOrder
     * @param iPartsRequestId
     * @param success
     */
    private void transferErrorFromSuccess(iPartsDataPicOrder dataPicOrder, String iPartsRequestId, iPartsXMLSuccess success) {
        if (!success.isErrorFree()) {
            // Die Antwort enthält Fehler
            transferErrorMessage(dataPicOrder, iPartsRequestId, success);
        }
    }

    /**
     * Callback für ResGetMediaContents
     * Speichert die Bilder in POOL und macht einen Eintrag in POOL_ENTRY
     *
     * @param project
     * @param message
     */
    public void savePicContentFromResponse(EtkProject project, iPartsXMLMediaMessage message) {
        if (message.isNotificationOnly()) {
            return; // keine DB-Aktionen
        }

        if (message.isResponse()) {
            //Hole die GUID aus der Antwort von ASPLM
            String iPartsRequestId = message.getTypeObject().getiPartsRequestID();
            // GetMediaContents kann aktuell über 2 Mechanismen getriggert werden: Über den Bildauftrag und bei der Suche nach
            // DASTI Bildreferenzen. Hier sollen nur die Bildauftragsoperationen verarbeitet werden, daher Check, ob die ID
            // den vorbestimmten Bildreferenz Prefix enthält.
            if (XMLImportExportHelper.isMediaContentFromPicReference(iPartsRequestId)) {
                return;
            }

            // Wurden SVGs über die Produktstammdaten nachgefordert, müssen diese Antworten anders abgearbeitet werden
            // als normale Bildauftrag-Antworten. Hier haben wir keine Bildauftrag-Objekte.
            if (XMLImportExportHelper.isMediaContentFromPicRequest(iPartsRequestId)) {
                handleRequestedProductPictures(project, message);
                return;
            }


            if ((iPartsRequestId != null) && (!iPartsRequestId.isEmpty())) {
                //den Bildauftrag aus der DB laden
                iPartsPicOrderId picOrderId = new iPartsPicOrderId(iPartsRequestId);
                iPartsDataPicOrder dataPicOrder = new iPartsDataPicOrder(project, picOrderId);
                // Falls PicOrder nicht in der DB ist -> Abbruch
                if (dataPicOrder.loadFromDB(picOrderId)) {
                    //Hole das Response und das Success Objekt
                    iPartsXMLResponse response = (iPartsXMLResponse)message.getTypeObject();
                    iPartsXMLSuccess success = response.getSuccess();
                    iPartsTransferStates status = PicOrderStateMachine.getInstance().getNextState(dataPicOrder, success.isErrorFree());
                    dataPicOrder.setStatus(status, DBActionOrigin.FROM_EDIT);
                    AbstractXMLResponseOperation resultOperation = response.getResult();
                    if (success.isErrorFree()) {
                        switch (response.getRequestOperation()) {
                            case GET_MEDIA_CONTENTS:
                                List<PictureProperties> resultList = storePicturesFromGetMediaContentsResponseForPicOrder(project, response, dataPicOrder);
                                if (!resultList.isEmpty()) {
                                    //in Tabelle DA_PICORDER_PICTURES speichern
                                    for (PictureProperties pictureProperties : resultList) {
                                        iPartsDataPicOrderPicture pictureDataObject = dataPicOrder.addPicture(pictureProperties.getItemId(),
                                                                                                              pictureProperties.getItemRevId(),
                                                                                                              DBActionOrigin.FROM_EDIT);
                                        if (!StrUtils.isEmpty(pictureProperties.getDesigner())) {
                                            pictureDataObject.setFieldValue(FIELD_DA_POP_DESIGNER, pictureProperties.getDesigner(), DBActionOrigin.FROM_EDIT);
                                        }
                                        if (pictureProperties.isColorTypeValid()) {
                                            pictureDataObject.setFieldValue(FIELD_DA_POP_VAR_TYPE, pictureProperties.getColorType().getDbValue(), DBActionOrigin.FROM_EDIT);
                                        }
                                        if (pictureProperties.getLastModified() != null) {
                                            pictureDataObject.setFieldValueAsDateTime(FIELD_DA_POP_LAST_MODIFIED, pictureProperties.getLastModified(), DBActionOrigin.FROM_EDIT);
                                        }
                                    }
                                    // Varianten Flags am Bildauftrag setzen
                                    transferVariantFlagsToPicOrder(dataPicOrder, (iPartsXMLResGetMediaContents)resultOperation);
                                }
                                break;
                            case GET_MEDIA_PREVIEW:
                                if (resultOperation != null) {
                                    iPartsXMLResGetMediaPreview rgmp = (iPartsXMLResGetMediaPreview)resultOperation;
                                    boolean importedWithoutErrors = true;
                                    if (rgmp.getBinaryFile() != null) {
                                        // Füge das Vorschaubild allen Bildern des aktuellen Bildauftrags hinzu
                                        for (iPartsDataPicOrderPicture picture : dataPicOrder.getPictures()) {
                                            importedWithoutErrors &= XMLImportExportHelper.importPreviewBinaryFile(project, rgmp.getBinaryFile(),
                                                                                                                   picture.getAsId().getPicItemId(),
                                                                                                                   picture.getAsId().getPicItemRevId());
                                        }
                                    } else {
                                        dataPicOrder.setStatus(iPartsTransferStates.ABGESCHLOSSEN, DBActionOrigin.FROM_EDIT);
                                    }

                                    // Wenn es Fehler beim Importieren gab ist die Vorschauanfrage fehlerhaft
                                    if (!importedWithoutErrors) {
                                        success = new iPartsXMLSuccess(false);
                                        success.addError(new iPartsXMLErrorText("Error while storing preview pictures."));
                                        transferErrorMessage(dataPicOrder, iPartsRequestId, success);
                                        dataPicOrder.setStatus(iPartsTransferStates.PREVIEW_RECEIVED_ERROR, DBActionOrigin.FROM_EDIT);
                                    }
                                }
                                break;
                        }
                        transferWarningMessage(dataPicOrder, iPartsRequestId, success);
                    } else {
                        //Die Antwort enthält Fehler
                        transferErrorMessage(dataPicOrder, iPartsRequestId, success);
                    }
                    //Neue Werte in die DB speichern
                    saveOrderToDB(project, dataPicOrder);
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "PicOrder values updated in DB. Order ID: " + iPartsRequestId);

                    // Mitteilung an alle anderen Cluster verschicken
                    message.convertToNotificationOnly();
                    ApplicationEvents.fireEventInAllProjectsAndClusters(new iPartsXMLMessageEvent(message, iPartsMQChannelTypeNames.MEDIA),
                                                                        false, false, true, null, null);
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "PicOrder not found in DB. Order ID: " + iPartsRequestId);
                }
            } else {
                Logger.getLogger().throwRuntimeException("RequestID (iParts GUID) must not be null or empty!");
            }
        } else {
            Logger.getLogger().throwRuntimeException("Message type must be an iPartsXMLResponse! Type is: " + message.getTypeObject().getClass().getName());
        }
    }

    /**
     * Setzt die Varianten-spezifischen Flags am Bildauftrag. Aktuell "isTemplate" und "AutomationLevel"
     *
     * @param dataPicOrder
     * @param mediaContentsResponse
     */
    private void transferVariantFlagsToPicOrder(iPartsDataPicOrder dataPicOrder, iPartsXMLResGetMediaContents mediaContentsResponse) {
        // Hier die Flags "isTemplate" und "automationLevel" aus der ersten MediaVariante am Bildauftrag setzen
        List<iPartsXMLMediaVariant> mediaVariants = mediaContentsResponse.getmContainer().getMediaVariants();
        iPartsXMLMediaVariant mediaVariant = mediaVariants.get(0);
        String automationLevel = mediaVariant.getAutomationLevel();
        dataPicOrder.setAutomationLevel((automationLevel == null) ? "" : automationLevel, DBActionOrigin.FROM_DB);
        dataPicOrder.setIsTemplate(mediaVariant.isTemplate(), DBActionOrigin.FROM_DB);
    }

    /**
     * Verarbeitet die Antworten von SVG Nachforderungen via Produktstammdaten
     *
     * @param project
     * @param message
     */
    private void handleRequestedProductPictures(EtkProject project, iPartsXMLMediaMessage message) {
        iPartsXMLResponse response = (iPartsXMLResponse)message.getTypeObject();
        iPartsXMLSuccess success = response.getSuccess();
        // Check, ob es einen Fehler gab
        if (success.isErrorFree()) {
            switch (response.getRequestOperation()) {
                case GET_MEDIA_CONTENTS:
                    // Bildtafeln speichern
                    storePicturesFromGetMediaContentsResponse(project, response);
                    // Vorschaubilder anfragen
                    sendProductRelatedPreviewRequest(response);
                    break;
                case GET_MEDIA_PREVIEW:
                    // Vorschaubilder speichern
                    storeProductRequestedPreviewPictures(project, response);
                    break;
            }
        } else {
            // Bei Fehlern, die MC Nummern ausgeben, sofern möglich
            String mcItemId = "";
            String mcItemRevId = "";
            switch (response.getRequestOperation()) {
                case GET_MEDIA_CONTENTS:
                    iPartsXMLMediaContainer mc = ((iPartsXMLResGetMediaContents)response.getResult()).getmContainer();
                    mcItemId = mc.getMcItemId();
                    mcItemRevId = mc.getMcItemRevId();
                    break;
                case GET_MEDIA_PREVIEW:
                    AbstractXMLResponseOperation resultOperation = response.getResult();
                    if (resultOperation != null) {
                        iPartsXMLResGetMediaPreview rgmp = (iPartsXMLResGetMediaPreview)resultOperation;
                        mcItemId = rgmp.getMcItemId();
                        mcItemRevId = rgmp.getMcItemRevId();
                    }
                    break;
            }
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Media response with errors. MC info: "
                                                                       + mcItemId + " - " + mcItemRevId
                                                                       + ", Errorcode: " + success.getErrorCode());
        }
    }

    /**
     * Speichert die Vorschaubilder, die via Produktstammdaten nachgefordert wurden
     *
     * @param project
     * @param response
     */
    private void storeProductRequestedPreviewPictures(EtkProject project, iPartsXMLResponse response) {
        // Um Vorschaubilder speichern zu können muss man die dazugehörigen PV Bilder finden. Wir haben bei den
        // nachgeforderten Bilder keine Verwaltungstabelle und müssen die PV Nummern nun via Bildaufträge oder alte
        // Nachforderungen finden
        AbstractXMLResponseOperation resultOperation = response.getResult();
        if (resultOperation != null) {
            iPartsXMLResGetMediaPreview rgmp = (iPartsXMLResGetMediaPreview)resultOperation;
            // Check, ob es zu der MC Nummer Bildaufträge gibt. Falls ja, durchlaufe alle PVs, die an dem Bildauftrag
            // hängen und lege die Vorschaubilder ab. Im Normalfall findet man in jedem MediaContainer nur eine PV Nummer
            iPartsDataPicOrderList picOrdersForMcItemNumber = iPartsDataPicOrderList.loadPicOrderListForTcObject(project, rgmp.getMcItemId(), rgmp.getMcItemRevId());
            if (!picOrdersForMcItemNumber.isEmpty()) {
                picOrdersForMcItemNumber.forEach(iPartsDataPicOrder -> iPartsDataPicOrder.getPictures().forEach(picture -> {
                    String picItemId = picture.getAsId().getPicItemId();
                    if (XMLImportExportHelper.isASPLMPictureNumber(picItemId)) {
                        String picItemRevId = picture.getAsId().getPicItemRevId();
                        storeSingleProductRequestedPreviewPic(project, rgmp, picItemId, picItemRevId);
                    }
                }));
            } else {
                // Es gab keinen Bildauftrag zur MC Nummer. Theoretisch kann es sein, dass das Bild via Nachfordern oder
                // Migrationsimporter hereingekommen ist. Check, ob es zur MC Nummer eine PV Nummer in DA_PIC_REFERENCE gibt
                iPartsDataPicReferenceList referenceList = new iPartsDataPicReferenceList();
                referenceList.searchSortAndFill(project, TABLE_DA_PIC_REFERENCE,
                                                new String[]{ FIELD_DPR_VAR_ID, FIELD_DPR_VAR_REV_ID },
                                                new String[]{ FIELD_DPR_MC_ID, FIELD_DPR_MC_REV_ID },
                                                new String[]{ rgmp.getMcItemId(), rgmp.getMcItemRevId() },
                                                null, null, null,
                                                DBDataObjectList.LoadType.ONLY_IDS, true,
                                                DBActionOrigin.FROM_DB);
                if (!referenceList.isEmpty()) {
                    referenceList.getAsList().forEach(reference -> {
                        String picItemId = reference.getVarId();
                        String picItemRevId = reference.getVarRevId();
                        storeSingleProductRequestedPreviewPic(project, rgmp, picItemId, picItemRevId);

                    });
                } else {
                    // Die PV Nummer konnte weder via Bildauftrag noch via PIC_REFERENCE bestimmt werden
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Couldn't find PV numbers for "
                                                                               + rgmp.getMcItemId() + " - " + rgmp.getMcItemRevId());
                }
            }
        }
    }

    private void storeSingleProductRequestedPreviewPic(EtkProject project, iPartsXMLResGetMediaPreview rgmp, String picItemId, String picItemRevId) {
        if (!XMLImportExportHelper.importPreviewBinaryFile(project, rgmp.getBinaryFile(),
                                                           picItemId, picItemRevId)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Error while storing preview pictures for "
                                                                       + picItemId + " - " + picItemRevId);
        }
    }

    /**
     * Sendet eine Vorschaubilder Anfrage für Bildtafeln, die via Produktstammdaten nachgefordert wurden
     *
     * @param response
     */
    private void sendProductRelatedPreviewRequest(iPartsXMLResponse response) {
        if (!(response.getResult() instanceof iPartsXMLResGetMediaContents)) {
            return;
        }
        iPartsXMLMediaContainer mc = ((iPartsXMLResGetMediaContents)response.getResult()).getmContainer();
        iPartsXMLGetMediaPreview gmp = new iPartsXMLGetMediaPreview(mc.getMcItemId(), mc.getMcItemRevId());
        iPartsXMLMediaMessage mediaMessage
                = iPartsPicOrderEditHelper.createMessageFromOperation(gmp, XMLImportExportHelper.makePicRequestGUIDForMediaContent(StrUtils.makeGUID()),
                                                                      iPartsConst.AS_PLM_USER_ID);
        sendPicOrderData(mediaMessage, null);
    }

    /**
     * Speichert die Bildtafeln, die via GET_MEDIA_CONTENTS ankommen
     *
     * @param project
     * @param response
     * @return
     */
    private List<PictureProperties> storePicturesFromGetMediaContentsResponseForPicOrder(EtkProject project,
                                                                                         iPartsXMLResponse response,
                                                                                         iPartsDataPicOrder dataPicOrder) {
        iPartsXMLMediaContainer mc = ((iPartsXMLResGetMediaContents)response.getResult()).getmContainer();
        List<iPartsXMLMediaVariant> mediaList = mc.getMediaVariants();
        return searchBestVariantsAndImport(project, mediaList, dataPicOrder);
    }

    private List<PictureProperties> storePicturesFromGetMediaContentsResponse(EtkProject project, iPartsXMLResponse response) {
        return storePicturesFromGetMediaContentsResponseForPicOrder(project, response, null);
    }

    private void transferErrorMessage(iPartsDataPicOrder dataPicOrder, String iPartsRequestId, iPartsXMLSuccess success) {
        dataPicOrder.setLastErrorCode(success.getErrorCode());
        String code = success.getErrorText();
        if (!code.isEmpty()) {
            dataPicOrder.setLastErrorTextForAllLanguages(code);
        }
        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Response with errors. Updating values for OrderID: "
                                                                   + iPartsRequestId + ", Errorcode: " + success.getErrorCode());
    }

    private void transferWarningMessage(iPartsDataPicOrder dataPicOrder, String iPartsRequestId, iPartsXMLSuccess success) {
        String code = getWarningCodeFromSuccessObject(success);
        if (!code.isEmpty()) {
            dataPicOrder.setLastWarningTextForAllLanguages(code);
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Response with warnings. Updating values for OrderID: "
                                                                       + iPartsRequestId + ", Warningtext: " + code);
        }
    }

    private void transferCommentMessage(EtkProject project, iPartsDataPicOrder dataPicOrder, iPartsXMLTcObject tcObject, List<iPartsXMLComment> comments) {
        if ((comments != null) && !comments.isEmpty()) {
            // erst existierenden Kommentar löschen
            dataPicOrder.deleteComments();
            String code = getCommentCodeFromComments(comments, project.getConfig().getViewerLanguages());
            dataPicOrder.setLastCommentTextForAllLanguages(comments.get(0).getUserID(), code);
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Comment. Updating values for ItemId: " + tcObject.getMcItemId()
                                                                       + " " + tcObject.getMcItemRevId() + ", UserId " + comments.get(0).getUserID()
                                                                       + ", Commenttext: " + code);
        }
    }

    /**
     * Suche das geeigneste Bild und importier es in POOL
     *
     * @param project
     * @param mediaList
     * @return
     */
    private List<PictureProperties> searchBestVariantsAndImport(EtkProject project, List<iPartsXMLMediaVariant> mediaList,
                                                                iPartsDataPicOrder dataPicOrder) {
        List<PictureProperties> resultList = new DwList<>();
        if (!mediaList.isEmpty()) {
            for (iPartsXMLMediaVariant variant : mediaList) {
                // Laut aktuellen Stand gehen wir immer erst auf SVG und dann auf die Kombination PNG + SEN Datei
                iPartsXMLMediaBinaryFile svgFile = variant.getSVGBinaryFile();
                iPartsXMLMediaBinaryFile pngFile = variant.getPNGBinaryFile();
                if ((svgFile != null) || (pngFile != null)) {
                    ImageFileImporterResult result = XMLImportExportHelper.importBinaryFileWithResult(project, variant);
                    if (result.importSuccessful()) {
                        //in DB speichern
                        PictureProperties pictureProperties = new PictureProperties(variant.getItemId(), variant.getItemRevId(),
                                                                                    variant.getMediaForBinFile((svgFile != null) ? svgFile : pngFile));
                        pictureProperties.setColorType(variant.getColorType());
                        resultList.add(pictureProperties);
                        iPartsSvgOutlineResult outLineResult = result.getOutlineResult();
                        if ((outLineResult != null) && (dataPicOrder != null)) {
                            dataPicOrder.setFieldValueAsBoolean(FIELD_PO_INVALID_IMAGE_DATA, outLineResult.hasClipPaths(), DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }
        }
        return resultList;
    }

    private String getWarningCodeFromSuccessObject(iPartsXMLSuccess success) {
        if (success.isErrorFree()) {
            List<iPartsXMLWarning> warningList = success.getWarnings();
            if ((warningList != null) && !warningList.isEmpty()) {
                //es wird nur die erste Warnung übernommen
                iPartsXMLWarning warning = warningList.get(0);
                HashMap<String, String> warnings = new HashMap<>();
                for (iPartsXMLWarningText warningText : warning.getWarningTexts()) {
                    String warningLanguage = warningText.getLanguage().toUpperCase();
                    if (!warnings.containsKey(warningLanguage)) {
                        warnings.put(warningLanguage, warningText.getText());
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Warning language: " + warningLanguage
                                                                                   + ", Warning text: " + warningText.getText());
                    } else {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Warningtext with the same language already exists."
                                                                                   + " Warning language: " + warningLanguage
                                                                                   + ", Warning text: " + warningText.getText());

                    }
                }
                return XMLImportExportHelper.createCodeFromMap(warnings);
            }
        }
        return "";
    }

    private String getCommentCodeFromComments(List<iPartsXMLComment> comments, List<String> languages) {
        if ((comments != null) && !comments.isEmpty()) {
            //es wird nur der erste Comment übernommen
            iPartsXMLComment comment = comments.get(0);
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Comment User: " + comment.getUserID()
                                                                       + ", Comment text: " + comment.getComment());
            HashMap<String, String> commentTexte = new HashMap<>();
            for (String language : languages) {
                commentTexte.put(language, comment.getComment());
            }
            return XMLImportExportHelper.createCodeFromMap(commentTexte);
        }
        return "";
    }

    /**
     * Abspeichern eines modifizierten DataPicOrder in DB
     *
     * @param project
     * @param order
     */

    private void saveOrderToDB(EtkProject project, iPartsDataPicOrder order) {
        project.getDbLayer().startTransaction();
        try {
            order.saveToDB();
            project.getDbLayer().commit();
        } catch (Exception e) {
            project.getDbLayer().rollback();
            Logger.getLogger().handleRuntimeException(e);
        }
    }

    /**
     * GetMediaContent absetzen
     *
     * @param project
     * @param picOrder
     */
    private void sendAndStorePicOrderData(EtkProject project, iPartsDataPicOrder picOrder, iPartsXMLMediaMessage xmlMessage, iPartsTransferStates newState) {
        if (AbstractApplication.isOnline()) {
            if (sendPicOrderData(xmlMessage, picOrder)) {
                picOrder.setStatus(newState, DBActionOrigin.FROM_EDIT);
                project.getDbLayer().startTransaction();
                try {
                    picOrder.saveToDB();
                    project.getDbLayer().commit();
                } catch (Exception e) {
                    project.getDbLayer().rollback();
                    Logger.getLogger().handleRuntimeException(e);
                }
            }

        }
    }

    /**
     * Sendet einzelne, mit dem übergebnene Bildauftrag verknüpfte MQ Nachrichten und simuliert bei Bedarf die Antwort
     *
     * @param xmlMessage
     * @param dataPicOrder
     * @return
     */
    private boolean sendPicOrderData(iPartsXMLMediaMessage xmlMessage, iPartsDataPicOrder dataPicOrder) {
        if (xmlMessage == null) {
            return false;
        }
        Throwable mqException = null;
        int simNewPicOrderDelay = iPartsPlugin.getSimAutoResponseDelayForSession(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY) * 1000;
        boolean result = false;
        try {
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).sendXMLMessageWithMQ(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                                                 xmlMessage, simNewPicOrderDelay >= 0);


        } catch (Throwable mqE) {
            // Alle Fehler beim senden von MQ abfangen. Falls hier etwas nicht geht, so soll der Bildauftrag gespeichert werden mit dem Status gespeichert.
            // Falls hier was schief gegangen ist, dann darf auf keinen Fall im Status gesendet gespeichert werden
            mqException = mqE;
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, mqE);
        }
        if (mqException == null) {
            // Erwartete Bildauftrags-Antwort zu Simulationszwecken erzeugen und versenden
            boolean simPicContentXml = iPartsPicOrderEditHelper.simulatePicContent();
            if (simPicContentXml || (simNewPicOrderDelay >= 0)) {
                iPartsXMLMediaMessage expectedResponseXmlMessage = null;
                switch (xmlMessage.getRequest().getOperation().getOperationType()) {
                    case CREATE_MC_ATTACHMENTS:
                        expectedResponseXmlMessage = iPartsEditXMLResponseSimulator.createMcAttachmentResponse(xmlMessage);
                        break;
                    case GET_MEDIA_CONTENTS:
                        boolean hasErrors = (dataPicOrder != null) && hasError(dataPicOrder);
                        boolean hasWarnings = (dataPicOrder != null) && hasWarning(dataPicOrder);
                        boolean hasBttTemplates = (dataPicOrder != null) && hasBttTemplates(dataPicOrder);
                        expectedResponseXmlMessage = iPartsEditXMLResponseSimulator.createPicContentResponse(xmlMessage, hasErrors, hasWarnings, hasBttTemplates);
                        // Damit von vorne gestartet werden kann. Bei einem Korrekturauftrag bleibt der Kommentar nämlich erhalten
                        if (hasBttTemplates) {
                            dataPicOrder.deleteComments();
                            saveOrderToDB(dataPicOrder.getEtkProject(), dataPicOrder);
                        }
                        break;
                    case GET_MEDIA_PREVIEW:
                        boolean forceError = (dataPicOrder != null) && dataPicOrder.getProposedName().toLowerCase().contains("preview_err");
                        expectedResponseXmlMessage = iPartsXMLResponseSimulator.createPicPreviewResponse(xmlMessage, forceError);
                        break;
                }
                if (expectedResponseXmlMessage != null) {
                    iPartsEditXMLResponseSimulator.writeAndSendSimulatedMessageResponseFromXML(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                               expectedResponseXmlMessage,
                                                                                               simPicContentXml,
                                                                                               simNewPicOrderDelay);
                }
            }
            result = true;
        } else {
            //erst jetzt die Meldung, damit die Transaktion nicht so lange offen bleibt, bis die Meldung abgenickt wurde.
            MessageDialog.showError(mqException.getMessage());
        }
        return result;
    }

    private boolean hasWarning(iPartsDataPicOrder dataPicOrder) {
        return containsSpecificText(dataPicOrder, "warning");
    }

    private boolean hasError(iPartsDataPicOrder dataPicOrder) {
        return containsSpecificText(dataPicOrder, "error");
    }

    private boolean hasBttTemplates(iPartsDataPicOrder dataPicOrder) {
        return containsSpecificText(dataPicOrder, "btt");
    }

    private boolean containsSpecificText(iPartsDataPicOrder dataPicOrder, String text) {
        String comment = dataPicOrder.getLastErrorTextForAllLanguages();
        if (comment.toLowerCase().contains(text.toLowerCase())) {
            return true;
        }
        return false;
    }

    protected class PictureProperties {

        private String itemId;
        private String itemRevId;
        private iPartsXMLMedia media;
        private iPartsColorTypes colorType;
        private Calendar lastModified;

        public PictureProperties(String itemId, String itemRevId, iPartsXMLMedia media) {
            this.itemId = itemId;
            this.itemRevId = itemRevId;
            setMedia(media);
        }

        public String getItemId() {
            return itemId;
        }

        public String getItemRevId() {
            return itemRevId;
        }

        public String getDesigner() {
            return media.getDesigner();
        }

        public iPartsColorTypes getColorType() {
            return colorType;
        }

        public void setColorType(iPartsColorTypes colorType) {
            this.colorType = colorType;
        }

        public Calendar getLastModified() {
            return lastModified;
        }

        public boolean isColorTypeValid() {
            return (colorType != null) && !StrUtils.isEmpty(colorType.getDbValue());
        }

        public void setMedia(iPartsXMLMedia media) {
            this.media = media;
            Date lastModifiedDate = media.getLastModifiedAsDateObject();
            if (lastModifiedDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(lastModifiedDate);
                lastModified = cal;
            }
        }
    }
}
