/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsMailboxHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMailboxChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Scheduler für die Erinnerungsfunktion zum Veröffentlichen von AS-Baumustern
 */
public class iPartsPublishModelReminderScheduler extends AbstractDayOfWeekHandler {

    public iPartsPublishModelReminderScheduler(EtkProject project, Session session) {
        super(project, session, iPartsEditPlugin.LOG_CHANNEL_PUBLISH_REMINDER_SCHEDULER, "publish AS model reminder");
    }

    @Override
    protected void executeLogic() {
        EtkProject project = getProject();
        int publishModelReminderDaysAfterValidFrom = iPartsEditPlugin.getPluginConfig().getConfigValueAsInteger(iPartsEditPlugin.CONFIG_PUBLISH_MODEL_REMINDER_DAYS_AFTER_VALID_FROM);
        Calendar publishModelReminderValidFrom = Calendar.getInstance();
        publishModelReminderValidFrom.add(Calendar.DAY_OF_MONTH, -publishModelReminderDaysAfterValidFrom); // "Tage nach Gültig ab" abziehen für den späteren Vergleich
        long publishModelReminderValidFromDate = StrUtils.strToLongDef(DateUtils.toyyyyMMddHHmmss_Calendar(publishModelReminderValidFrom),
                                                                       Integer.MAX_VALUE);

        iPartsDataModelList dataModelList = iPartsDataModelList.loadAllDataModelList(project, DBDataObjectList.LoadType.ONLY_IDS);
        Logger.log(getLogChannel(), LogType.DEBUG, "Checking " + dataModelList.size() + " models to be published with "
                                                   + publishModelReminderDaysAfterValidFrom + " days after model valid from date...");

        // Alle relevanten Baumuster ermitteln und überprüfen (nicht retail-relevant, Produkt-Zuordnung (aber kein PSK-Produkt),
        // nur PKW/Van, Produkt sichtbar)
        Map<iPartsModelId, Long> unpublishedModelIds = calculateUnpublishedModelIds(project, dataModelList, publishModelReminderValidFromDate);

        if (!unpublishedModelIds.isEmpty()) {
            String unpublishedModelNumbers = buildModelNoString(unpublishedModelIds);
            Logger.log(getLogChannel(), LogType.DEBUG, "Found " + unpublishedModelIds.size() + " models to be published: "
                                                       + unpublishedModelNumbers);

            // Doppelte Nachrichten vermeiden
            Map<iPartsModelId, Long> modelIdsForReminderMessage = avoidDoubleMessages(project, unpublishedModelIds);

            // Erinnerungs-Nachrichten erzeugen
            if (!modelIdsForReminderMessage.isEmpty()) {
                createAndSendPublishModelReminderMessages(modelIdsForReminderMessage);
            } else {
                Logger.log(getLogChannel(), LogType.DEBUG, "No unpublished models found for a new reminder message");
            }
        } else {
            Logger.log(getLogChannel(), LogType.DEBUG, "No models found to be published");
        }
    }

    private Map<iPartsModelId, Long> calculateUnpublishedModelIds(EtkProject project, iPartsDataModelList dataModelList,
                                                                  long publishModelReminderValidFromDate) {
        // Alle relevanten und gültigen Baumuster ermitteln und überprüfen (nicht retail-relevant, Produkt-Zuordnung (aber
        // kein PSK-Produkt), nur PKW/Van, Produkt sichtbar)
        Map<iPartsModelId, Long> unpublishedModelIds = new TreeMap<>();
        for (iPartsDataModel dataModel : dataModelList) {
            iPartsModel model = iPartsModel.getInstance(project, dataModel.getAsId());
            if (!model.isModelVisible() && !model.isModelInvalid()) {
                // Ist das AS-Baumuster einem gültigen sichtbaren Nicht-PSK-PKW/Van-Produkt zugeordnet?
                List<iPartsProduct> productsForModel = iPartsProductHelper.getProductsForModel(project, model.getModelId(), null, null, false);
                boolean validProductFound = false;
                for (iPartsProduct product : productsForModel) {
                    if (!product.isPSK() && product.isCarAndVanProduct() && product.isRetailRelevantFromDB()) {
                        validProductFound = true;
                        break;
                    }
                }

                if (validProductFound) {
                    // Muss das Baumuster publiziert werden?
                    String validFromString = model.getValidFrom();
                    if (!validFromString.isEmpty()) { // Gültiges Datum-ab ist Voraussetzung
                        long validFromDate = StrUtils.strToLongDef(validFromString, Integer.MAX_VALUE);
                        if (validFromDate < publishModelReminderValidFromDate) {
                            unpublishedModelIds.put(dataModel.getAsId(), validFromDate);
                        }
                    }
                }
            }
        }
        return unpublishedModelIds;
    }

    private String buildModelNoString(Map<iPartsModelId, Long> modelIdMap) {
        return modelIdMap.keySet().stream()
                .map(iPartsModelId::getModelNumber)
                .collect(Collectors.joining(", "));
    }

    private Map<iPartsModelId, Long> avoidDoubleMessages(EtkProject project, Map<iPartsModelId, Long> unpublishedModelIds) {
        // Doppelte Nachrichten vermeiden
        Map<iPartsModelId, Long> modelIdsForReminderMessage = new TreeMap<>();
        for (Map.Entry<iPartsModelId, Long> modelEntry : unpublishedModelIds.entrySet()) {
            if (!iPartsMailboxHelper.mailboxItemExists(project, iPartsMailboxHelper.MailboxMessageType.PUBLISH_MODEL,
                                                       modelEntry.getKey())) {
                modelIdsForReminderMessage.put(modelEntry.getKey(), modelEntry.getValue());
            }
        }
        return modelIdsForReminderMessage;
    }


    private void createAndSendPublishModelReminderMessages(Map<iPartsModelId, Long> modelIdsForReminderMessage) {
        String modelNumbersForReminderMessage = buildModelNoString(modelIdsForReminderMessage);
        Logger.log(getLogChannel(), LogType.DEBUG, "Creating reminder messages for " + modelIdsForReminderMessage.size() + " models to be published: "
                                                   + modelNumbersForReminderMessage);
        iPartsMailboxChangedEvent mailboxChangedEvent = new iPartsMailboxChangedEvent(iPartsMailboxChangedEvent.MailboxItemState.NEW);
        String languageDE = Language.DE.getCode();
        for (Map.Entry<iPartsModelId, Long> modelEntry : modelIdsForReminderMessage.entrySet()) {
            iPartsModelId modelId = modelEntry.getKey();
            String subject = TranslationHandler.translateForLanguage("!!Veröffentlichung Baumuster prüfen: %1", languageDE,
                                                                     modelId.getModelNumber());
            String formattedValidFrom = DateUtils.formatDateTime(String.valueOf(modelEntry.getValue()),
                                                                 DateUtils.simpleTimeFormatyyyyMMddHHmmss,
                                                                 DateUtils.simpleDateTimeFormatddDOTMMDOTyyyy);
            String message = TranslationHandler.translateForLanguage("!!Bitte prüfen, ob das Baumuster \"%1\" veröffentlicht werden muss.%2Baumuster besitzt \"Gültig ab\"-Datum %3.",
                                                                     languageDE, modelId.getModelNumber(),
                                                                     "\n", formattedValidFrom);
            iPartsMailboxChangedEvent event = iPartsMailboxHelper.createMessageForPublishModelReminder(getProject(), modelId,
                                                                                                       subject, message);
            if (event != null) {
                mailboxChangedEvent.addAllRecipients(event.getMailboxItems());
            }
        }

        // MailboxEvent verschicken
        if (!mailboxChangedEvent.getMailboxItems().isEmpty()) {
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(mailboxChangedEvent);
        }
    }
}