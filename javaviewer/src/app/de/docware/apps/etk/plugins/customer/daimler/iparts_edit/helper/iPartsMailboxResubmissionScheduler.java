/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsDataMailboxItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsDataMailboxRecipient;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsDataMailboxRecipientList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsMailboxItemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMailboxChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.date.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scheduler für den Postkorb für die Wiedervorlage
 */
public class iPartsMailboxResubmissionScheduler extends AbstractDayOfWeekHandler implements iPartsConst {

    public iPartsMailboxResubmissionScheduler(EtkProject project, Session session) {
        super(project, session, iPartsPlugin.LOG_CHANNEL_MAILBOX, "mailbox resubmission");
    }

    @Override
    protected void executeLogic() {
        EtkProject project = getProject();
        iPartsDataMailboxRecipientList mailboxRecipientList = new iPartsDataMailboxRecipientList();
        mailboxRecipientList.loadAllMailboxMessagesWithResubmissionDate(project);
        if (!mailboxRecipientList.isEmpty()) {
            // Map von Message-GUID auf Message-Empfänger aufbauen, wenn das Wiedervorlagedatum erreicht ist
            Map<String, List<iPartsDataMailboxRecipient>> messageToRecipientsMap = new HashMap<>();
            String currentDate = DateUtils.toyyyyMMdd_currentDate();
            for (iPartsDataMailboxRecipient dataMailboxRecipient : mailboxRecipientList) {
                String resubmissionDate = dataMailboxRecipient.getFieldValue(FIELD_DMSG_RESUBMISSION_DATE);
                if (!resubmissionDate.isEmpty() && (resubmissionDate.compareTo(currentDate) <= 0)) { // Wiedervorlagedatum erreicht
                    List<iPartsDataMailboxRecipient> recipients = messageToRecipientsMap.computeIfAbsent(dataMailboxRecipient.getMsgID(),
                                                                                                         msgId -> new ArrayList<>());
                    recipients.add(dataMailboxRecipient);
                }
            }

            Logger.log(iPartsPlugin.LOG_CHANNEL_MAILBOX, LogType.DEBUG, "Found " + messageToRecipientsMap.size() + " mailbox messages for resubmission");

            if (messageToRecipientsMap.isEmpty()) {
                return;
            }

            // Daten in der DB anpassen
            GenericEtkDataObjectList dataObjectsToBeSaved = new GenericEtkDataObjectList();
            iPartsMailboxChangedEvent event = new iPartsMailboxChangedEvent();
            for (Map.Entry<String, List<iPartsDataMailboxRecipient>> messageEntry : messageToRecipientsMap.entrySet()) {
                iPartsDataMailboxItem dataMailboxItem = new iPartsDataMailboxItem(project, new iPartsMailboxItemId(messageEntry.getKey()));
                if (dataMailboxItem.existsInDB()) {
                    dataMailboxItem.setFieldValue(FIELD_DMSG_RESUBMISSION_DATE, "", DBActionOrigin.FROM_EDIT);
                    dataObjectsToBeSaved.add(dataMailboxItem, DBActionOrigin.FROM_EDIT);

                    // Nachricht für jeden Empfänger als ungelesen markieren und Nachricht dafür erzeugen
                    for (iPartsDataMailboxRecipient dataMailboxRecipient : messageEntry.getValue()) {
                        dataMailboxRecipient.setMessageRead(false);
                        event.addMailboxRecipient(dataMailboxRecipient.getFieldValue(FIELD_DMSG_CREATION_USER_ID), dataMailboxRecipient);
                        dataMailboxRecipient.removeForeignTablesAttributes();
                        dataObjectsToBeSaved.add(dataMailboxRecipient, DBActionOrigin.FROM_EDIT);
                    }
                }
            }

            EtkDbs etkDbs = getProject().getEtkDbs();
            etkDbs.startTransaction(); // BatchStatement wird innerhalb von dataObjectsToBeSaved.saveToDB() sowieso verwendet
            try {
                dataObjectsToBeSaved.saveToDB(getProject());
                etkDbs.commit();
            } catch (Exception e) {
                etkDbs.rollback();
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MAILBOX, LogType.ERROR, e);
                return;
            }

            // Nachrichten verschicken
            event.setMailboxItemReadState(false);
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(event);
        }
    }
}