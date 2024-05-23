/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeriesSOP;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSeriesSOPId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsMailboxHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMailboxChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Scheduler für die Erinnerungsfunktion zum Veröffentlichen von Produkten
 */
public class iPartsPublishProductReminderScheduler extends AbstractDayOfWeekHandler {

    public iPartsPublishProductReminderScheduler(EtkProject project, Session session) {
        super(project, session, iPartsEditPlugin.LOG_CHANNEL_PUBLISH_REMINDER_SCHEDULER, "publish product reminder");
    }

    @Override
    protected void executeLogic() {
        EtkProject project = getProject();
        int publishProductReminderDaysAfterSOP = iPartsEditPlugin.getPluginConfig().getConfigValueAsInteger(iPartsEditPlugin.CONFIG_PUBLISH_PRODUCT_REMINDER_DAYS_AFTER_SOP);
        Calendar publishProductReminderSOP = Calendar.getInstance();
        publishProductReminderSOP.add(Calendar.DAY_OF_MONTH, -publishProductReminderDaysAfterSOP); // "Tage nach SOP" abziehen für den späteren Vergleich
        long publishProductReminderSOPDate = StrUtils.strToLongDef(DateUtils.toyyyyMMddHHmmss_Calendar(publishProductReminderSOP),
                                                                   Integer.MAX_VALUE);

        List<iPartsProduct> allProducts = iPartsProduct.getAllProducts(project);
        Logger.log(getLogChannel(), LogType.DEBUG, "Checking " + allProducts.size() + " products to be published with "
                                                   + publishProductReminderDaysAfterSOP + " days after SOP...");
        Map<iPartsProductId, Long> unpublishedProductIds = calculateUnpublishedProductIds(project, allProducts, publishProductReminderSOPDate);

        if (!unpublishedProductIds.isEmpty()) {
            String unpublishedProductNumbers = buildProductNoString(unpublishedProductIds);
            Logger.log(getLogChannel(), LogType.DEBUG, "Found " + unpublishedProductIds.size() + " products to be published: "
                                                       + unpublishedProductNumbers);

            // Doppelte Nachrichten vermeiden
            Map<iPartsProductId, Long> productIdsForReminderMessage = avoidDoubleMessages(project, unpublishedProductIds);

            // Erinnerungs-Nachrichten erzeugen
            if (!productIdsForReminderMessage.isEmpty()) {
                createAndSendPublishProductReminderMessages(productIdsForReminderMessage);
            } else {
                Logger.log(getLogChannel(), LogType.DEBUG, "No unpublished products found for a new reminder message");
            }
        } else {
            Logger.log(getLogChannel(), LogType.DEBUG, "No products found to be published");
        }
    }

    private Map<iPartsProductId, Long> avoidDoubleMessages(EtkProject project, Map<iPartsProductId, Long> unpublishedProductIds) {
        // Doppelte Nachrichten vermeiden
        Map<iPartsProductId, Long> productIdsForReminderMessage = new TreeMap<>();
        for (Map.Entry<iPartsProductId, Long> productEntry : unpublishedProductIds.entrySet()) {
            if (!iPartsMailboxHelper.mailboxItemExists(project, iPartsMailboxHelper.MailboxMessageType.PUBLISH_PRODUCT,
                                                       productEntry.getKey())) {
                productIdsForReminderMessage.put(productEntry.getKey(), productEntry.getValue());
            }
        }
        return productIdsForReminderMessage;
    }

    private String buildProductNoString(Map<iPartsProductId, Long> productIdMap) {
        return productIdMap.keySet().stream()
                .map(iPartsProductId::getProductNumber)
                .collect(Collectors.joining(", "));

    }

    private Map<iPartsProductId, Long> calculateUnpublishedProductIds(EtkProject project, List<iPartsProduct> allProducts,
                                                                      long publishProductReminderSOPDate) {
        Map<iPartsProductId, Long> unpublishedProductIds = new TreeMap<>();

        // Alle relevanten Produkte ermitteln und überprüfen (kein PSK, nicht retail-relevant, nur PKW/Van)
        for (iPartsProduct product : allProducts) {
            if (!product.isPSK() && !product.isRetailRelevantFromDB() && product.isCarAndVanProduct()) {
                long minimumSOP = Long.MAX_VALUE;
                Set<iPartsSeriesSOPId> seriesSOPIds = new HashSet<>();

                // Alle Baumuster des Produkts betrachten und dafür jeweils Baureihe und AA für die SOPs ermitteln
                for (String modelNumber : product.getModelNumbers(project)) {
                    iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNumber));
                    String seriesNumber = model.getSeriesId().getSeriesNumber();
                    String aa = model.getAusfuehrungsArt();
                    seriesSOPIds.add(new iPartsSeriesSOPId(seriesNumber, aa));
                }

                // Ältesten/Minimalen aktiven SOP für das Produkt ermitteln
                for (iPartsSeriesSOPId seriesSOPId : seriesSOPIds) {
                    iPartsDataSeriesSOP dataSeriesSOP = new iPartsDataSeriesSOP(project, seriesSOPId);
                    if (dataSeriesSOP.existsInDB() && dataSeriesSOP.getFieldValueAsBoolean(iPartsConst.FIELD_DSP_ACTIVE)) {
                        Calendar sop = dataSeriesSOP.getFieldValueAsDateTime(iPartsConst.FIELD_DSP_START_OF_PROD);
                        if (sop != null) {
                            long sopDateTime = StrUtils.strToLongDef(DateUtils.toyyyyMMddHHmmss_Calendar(sop), Integer.MAX_VALUE);
                            minimumSOP = Math.min(minimumSOP, sopDateTime);
                        }
                    }
                }

                // Muss das Produkt publiziert werden?
                if (minimumSOP < publishProductReminderSOPDate) {
                    unpublishedProductIds.put(product.getAsId(), minimumSOP);
                }
            }
        }
        return unpublishedProductIds;
    }

    private void createAndSendPublishProductReminderMessages(Map<iPartsProductId, Long> productIdsForReminderMessage) {
        String productNumbersForReminderMessage = buildProductNoString(productIdsForReminderMessage);
        Logger.log(getLogChannel(), LogType.DEBUG, "Creating reminder messages for " + productIdsForReminderMessage.size()
                                                   + " products to be published: " + productNumbersForReminderMessage);
        iPartsMailboxChangedEvent mailboxChangedEvent = new iPartsMailboxChangedEvent(iPartsMailboxChangedEvent.MailboxItemState.NEW);
        String languageDE = Language.DE.getCode();
        for (Map.Entry<iPartsProductId, Long> productEntry : productIdsForReminderMessage.entrySet()) {
            iPartsProductId productId = productEntry.getKey();
            String subject = TranslationHandler.translateForLanguage("!!Veröffentlichung Produkt prüfen: %1", languageDE,
                                                                     productId.getProductNumber());
            String formattedMinimumSOP = DateUtils.formatDateTime(String.valueOf(productEntry.getValue()),
                                                                  DateUtils.simpleTimeFormatyyyyMMddHHmmss,
                                                                  DateUtils.simpleDateTimeFormatddDOTMMDOTyyyy);
            String message = TranslationHandler.translateForLanguage("!!Bitte prüfen, ob das Produkt \"%1\" veröffentlicht werden muss.%2Baureihe-Ausführungsart zum Produkt besitzt den SOP Termin %3.",
                                                                     languageDE, productId.getProductNumber(),
                                                                     "\n", formattedMinimumSOP);
            iPartsMailboxChangedEvent event = iPartsMailboxHelper.createMessageForPublishProductReminder(getProject(),
                                                                                                         productId,
                                                                                                         subject,
                                                                                                         message);
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