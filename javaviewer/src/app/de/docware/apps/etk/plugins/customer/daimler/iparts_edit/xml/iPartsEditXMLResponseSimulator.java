/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.xml;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsXMLResponseSimulator;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractMediaOrderRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLMediaContainerCreateOrModifyResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderPicture;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderPicturesList;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * Hilfsklasse zum Erzeugen von erwarteten Antworten für {@link iPartsXMLMediaMessage}s im Edit für Simulationszwecke.
 */
public class iPartsEditXMLResponseSimulator extends iPartsXMLResponseSimulator {

    /**
     * Erzeugt eine Antwort für eine {@link iPartsTransferNodeTypes#CORRECT_MEDIA_ORDER} Anfrage
     *
     * @param mediaMessage
     * @param pictures
     * @return
     */
    public static iPartsXMLMediaMessage createCorrectMediaOrderResponse(iPartsXMLMediaMessage mediaMessage, iPartsDataPicOrderPicturesList pictures) {
        iPartsXMLMediaMessage messageResponse = createMessageResponseWithCorrectionSuccess(mediaMessage);
        if ((messageResponse.getRequestOperationTypeFromResponse() == iPartsTransferNodeTypes.CORRECT_MEDIA_ORDER)) {
            if (messageResponse.getResponse().getSuccess().isErrorFree()) {
                // iPartsXMLResCreateMediaOrder nur bei Erfolg hinzufügen
                iPartsXMLCorrectMediaOrder como = (iPartsXMLCorrectMediaOrder)mediaMessage.getRequest().getOperation();
                iParstXMLResCorrectMediaOrder rcomo = new iParstXMLResCorrectMediaOrder(null);
                iPartsXMLMediaContainer mc = new iPartsXMLMediaContainer(como.getMcItemId(), como.getMcItemRevId());

                addMediaOrder(rcomo);
                List<iPartsXMLMediaVariant> variants = como.getMediaVariants();
                if ((variants != null) && !variants.isEmpty()) {
                    String mcKey = makeMCKey(como.getMcItemId(), como.getMcItemRevId());
                    variantsForCorrection.put(mcKey, variants);
                    for (iPartsXMLMediaVariant requestVariant : variants) {
                        iPartsXMLMediaVariant responseVariant = new iPartsXMLMediaVariant();
                        responseVariant.setItemId(requestVariant.getItemId());
                        responseVariant.setItemRevId(requestVariant.getItemRevId());
                        mc.addMediaVariant(responseVariant);
                    }
                } else {
                    // Wenn keine MediaVariants mitgegeben wurden, dann müssen alle neu gezeichnet werden
                    for (iPartsDataPicOrderPicture picture : pictures) {
                        iPartsXMLMediaVariant responseVariant = new iPartsXMLMediaVariant();
                        responseVariant.setItemId(picture.getAsId().getPicItemId());
                        responseVariant.setItemRevId(picture.getAsId().getPicItemRevId());
                        mc.addMediaVariant(responseVariant);
                    }
                }
                rcomo.setMContainer(mc);
                messageResponse.getResponse().setResult(rcomo);
            }
            return messageResponse;
        }
        return null;
    }

    /**
     * Erzeugt eine Antwort für eine {@link iPartsTransferNodeTypes#ACCEPT_MEDIA_CONTAINER} Anfrage
     *
     * @param xmlMessage
     * @return
     */
    public static iPartsXMLMediaMessage createAcceptMediaContainerResponse(iPartsXMLMediaMessage xmlMessage) {
        iPartsXMLMediaMessage messageResponse = createMessageResponseWithImageSuccess(xmlMessage);
        if ((messageResponse.getRequestOperationTypeFromResponse() == iPartsTransferNodeTypes.ACCEPT_MEDIA_CONTAINER)) {
            return messageResponse;
        }
        return null;
    }

    /**
     * Erzeugt eine Antwort für eine {@link iPartsTransferNodeTypes#CHANGE_MEDIA_ORDER} Anfrage
     *
     * @param project
     * @param xmlMessage
     * @param picOrder
     * @return
     */
    public static iPartsXMLMediaMessage createChangeMediaOrderResponse(EtkProject project, iPartsXMLMediaMessage xmlMessage,
                                                                       iPartsDataPicOrder picOrder) {
        iPartsXMLMediaMessage messageResponse = createMessageResponseWithImageSuccess(xmlMessage);
        if ((messageResponse.getRequestOperationTypeFromResponse() == iPartsTransferNodeTypes.CHANGE_MEDIA_ORDER)) {
            if (messageResponse.getResponse().getSuccess().isErrorFree()) {
                iPartsXMLResChangeMediaOrder rcmo = new iPartsXMLResChangeMediaOrder(null);
                iPartsXMLChangeMediaOrder changeMediaOrder = (iPartsXMLChangeMediaOrder)xmlMessage.getRequest().getOperation();
                String mcItemId = changeMediaOrder.getMcItemId();
                String mcItemRevId;
                // Bei einem Änderungsauftrag die Revision abhängig von der Anzahl der Vorgänger setzen
                if (picOrder.isChangeOrder() && !picOrder.hasFakeOriginalPicOrder() && StrUtils.isValid(picOrder.getOriginalPicOrder())) {
                    // Alle Nachfolger laden und in Abhängigkeit davon die Revision setzen
                    iPartsDataPicOrderList listWithOrders = iPartsDataPicOrderList.loadChangeOrdersForOriginalPicOrder(project, picOrder.getOriginalPicOrder());
                    // Die höchste Revision von bestehenden Aufträgen bestimmen
                    int revision = listWithOrders.getAsList().stream().map(picorder -> StrUtils.strToIntDef(picorder.getOrderRevisionExtern(), 0)).reduce(0, (picOrder1, picOrder2) -> {
                        if (picOrder1 > picOrder2) {
                            return picOrder1;
                        }

                        if (picOrder2 > picOrder1) {
                            return picOrder2;
                        }
                        return picOrder2;
                    });
                    revision++;
                    mcItemRevId = String.valueOf(revision);
                } else if (picOrder.isCopy()) {
                    // Bei einer Kopie wird eine neue MC Nummer geliefert mit der initialen Revision
                    mcItemId = getRandomMCItemId();
                    mcItemRevId = "1";
                } else {
                    mcItemRevId = increaseRevValue(changeMediaOrder.getMcItemRevId());
                }
                mcItemRevId = StrUtils.prefixStringWithCharsUpToLength(mcItemRevId, '0', 3);
                List<iPartsXMLMediaVariant> changedVariants = changeMediaOrder.getMediaVariants();
                // Wenn keine Varianten IDs mitgegeben wurden, dann betrifft die Änderungen alle verknüpften Varianten.
                // Die Antwort enthält aber wiederum alle verknüpften Varianten. Um das zu simulieren, werden alle Bilder
                // eines Auftrags mitgegeben und zu MediaVarianten umgewandelt.
                iPartsDataPicOrderPicturesList pictures = picOrder.getPictures();
                if (changedVariants.isEmpty() && (pictures != null) && !pictures.isEmpty()) {
                    for (iPartsDataPicOrderPicture picture : pictures) {
                        // Bei migrierten Bilder existiert keine Mediavariante aber es hängen trotzdem Bilder am
                        // Bildauftrag. Migrierte Bilder dürfen nicht evrarbeitet werden.
                        if (XMLImportExportHelper.isASPLMPictureNumber(picture.getAsId().getPicItemId())) {
                            changedVariants.add(picture.getAsMediaVariant(""));
                        }
                    }
                }
                // Bei einem Kopierauftrag sollen neue PVs erzeugt werden
                if (!picOrder.isCopy()) {
                    String mcKey = makeMCKey(mcItemId, mcItemRevId);
                    variantsForCorrection.put(mcKey, changedVariants);
                }
                addMediaContainerWithVariants(rcmo, mcItemId, mcItemRevId, changedVariants);
                addMediaOrder(rcmo);
                messageResponse.getResponse().setResult(rcmo);
            }
            return messageResponse;
        }
        return null;
    }

    /**
     * Erzeugt eine Antwort für eine {@link iPartsTransferNodeTypes#UPDATE_MEDIA_ORDER} Anfrage
     *
     * @param xmlMessage
     * @return
     */
    public static iPartsXMLMediaMessage createUpdateMediaOrderResponse(iPartsXMLMediaMessage xmlMessage) {
        iPartsXMLMediaMessage messageResponse = createMessageResponseWithImageSuccess(xmlMessage);
        if ((messageResponse.getRequestOperationTypeFromResponse() == iPartsTransferNodeTypes.UPDATE_MEDIA_ORDER)) {
            if (messageResponse.getResponse().getSuccess().isErrorFree()) {
                iPartsXMLResUpdateMediaOrder rumo = new iPartsXMLResUpdateMediaOrder(null);
                iPartsXMLUpdateMediaOrder updateMediaOrder = (iPartsXMLUpdateMediaOrder)xmlMessage.getRequest().getOperation();
                String mcItemId;
                String mcItemRevId;
                if (updateMediaOrder != null) {
                    mcItemId = updateMediaOrder.getMcItemId();
                    // Bei einem Update wird die Revision hochgezählt
                    int revision = StrUtils.strToIntDef(updateMediaOrder.getMcItemRevId(), 0);
                    revision++;
                    mcItemRevId = String.valueOf(revision);
                } else {
                    mcItemId = getRandomMCItemId();
                    mcItemRevId = getRandomMCItemRevId();
                }
                mcItemRevId = StrUtils.prefixStringWithCharsUpToLength(mcItemRevId, '0', 3);
                addMediaContainer(rumo, mcItemId, mcItemRevId);
                addMediaOrder(rumo);
                messageResponse.getResponse().setResult(rumo);
            }
            return messageResponse;
        }
        return null;
    }

    private static String getRandomMCItemRevId() {
        Random r = new Random();
        return StrUtils.prefixStringWithCharsUpToLength(String.valueOf(r.nextInt(1000)), '0', 3);
    }

    private static String getRandomMCItemId() {
        Random r = new Random();
        return "MC" + r.nextInt(1000) + "." + r.nextInt(1000) + "." + r.nextInt(1000) + "." + r.nextInt(1000);
    }

    /**
     * Erzeugt eine Antwort für eine {@link iPartsTransferNodeTypes#CREATE_MEDIA_ORDER} Anfrage
     *
     * @param xmlMessage Bildauftrag
     */
    public static iPartsXMLMediaMessage createMediaOrderResponse(iPartsXMLMediaMessage xmlMessage) {
        iPartsXMLMediaMessage messageResponse = createMessageResponseWithImageSuccess(xmlMessage);
        if ((messageResponse.getRequestOperationTypeFromResponse() == iPartsTransferNodeTypes.CREATE_MEDIA_ORDER)) {
            if (messageResponse.getResponse().getSuccess().isErrorFree()) {
                iPartsXMLResCreateMediaOrder rcmo = new iPartsXMLResCreateMediaOrder(null);
                addMediaContainer(rcmo, getRandomMCItemId(), getRandomMCItemRevId());
                addMediaOrder(rcmo);
                messageResponse.getResponse().setResult(rcmo);
            }
            return messageResponse;
        }
        return null;
    }

    /**
     * Fügt der übergebenen Antwort {@link AbstractXMLMediaContainerCreateOrModifyResponse} ein {@link iPartsXMLMediaOrder}
     * Objekt hinzu.
     *
     * @param rcmo
     */
    private static void addMediaOrder(AbstractXMLMediaContainerCreateOrModifyResponse rcmo) {
        // Das simulierte Auftragsanlagedatum liegt immer 3 Tage und 3 Stunden in der Zukunft
        Calendar c = GregorianCalendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 3);
        c.add(Calendar.HOUR_OF_DAY, 3);
        iPartsXMLMediaOrder mo = new iPartsXMLMediaOrder(c.getTime());
        rcmo.setMOrder(mo);
    }

    /**
     * Fügt der übergebenen Antwort {@link AbstractXMLMediaContainerCreateOrModifyResponse} ein {@link iPartsXMLMediaContainer}
     * Objekt samt enthaltenen {@link iPartsXMLMediaVariant} Objekten hinzu.
     *
     * @param rcmo
     * @param mcItemId
     * @param mcItemRevId
     * @param changedVariants
     */
    private static void addMediaContainerWithVariants(AbstractXMLMediaContainerCreateOrModifyResponse rcmo, String mcItemId,
                                                      String mcItemRevId, List<iPartsXMLMediaVariant> changedVariants) {
        addMediaContainer(rcmo, mcItemId, mcItemRevId);
        if (changedVariants != null) {
            rcmo.getMContainer().addMediaVariants(changedVariants);
        }
    }

    /**
     * Fügt der übergebenen Antwort {@link AbstractXMLMediaContainerCreateOrModifyResponse} ein {@link iPartsXMLMediaContainer}
     * Objekt hinzu.
     *
     * @param rcmo
     * @param mcItemId
     * @param mcItemRevId
     */
    private static void addMediaContainer(AbstractXMLMediaContainerCreateOrModifyResponse rcmo, String mcItemId, String mcItemRevId) {
        rcmo.setMContainer(new iPartsXMLMediaContainer(mcItemId, StrUtils.prefixStringWithCharsUpToLength(mcItemRevId, '0', 3)));
    }

    /**
     * Erzeugt für die übergebenen Anfrage {@link iPartsXMLMediaMessage} ein {@link iPartsXMLResponse} Objekt samt
     * positiven oder negativen {@link iPartsXMLSuccess}
     *
     * @param xmlMessage
     * @param nodeType
     * @return
     */
    private static iPartsXMLResponse createResponse(iPartsXMLMediaMessage xmlMessage, iPartsTransferNodeTypes nodeType) {
        return new iPartsXMLResponse(xmlMessage.getTypeObject().getiPartsRequestID(), xmlMessage.getTypeObject().getTargetClusterID(),
                                     nodeType,
                                     xmlMessage.getTypeObject().getToParticipant(), xmlMessage.getTypeObject().getFromParticipant());
    }

    /**
     * Erzeugt eine befüllte {@link iPartsXMLMediaMessage} OHNE {@link iPartsXMLSuccess}
     *
     * @param xmlMessage
     * @return
     */
    private static iPartsXMLMediaMessage createMessageResponseWithoutSuccess(iPartsXMLMediaMessage xmlMessage) {
        iPartsXMLMediaMessage messageResponse = new iPartsXMLMediaMessage(true);
        addHistory(messageResponse, xmlMessage);
        addResponse(messageResponse, xmlMessage);
        return messageResponse;
    }

    private static iPartsXMLMediaMessage createMessageResponseWithImageSuccess(iPartsXMLMediaMessage xmlMessage) {
        iPartsXMLMediaMessage messageResponse = createMessageResponseWithoutSuccess(xmlMessage);
        addImageSuccess(messageResponse, xmlMessage);
        return messageResponse;
    }

    private static iPartsXMLMediaMessage createMessageResponseWithCorrectionSuccess(iPartsXMLMediaMessage xmlMessage) {
        iPartsXMLMediaMessage messageResponse = createMessageResponseWithoutSuccess(xmlMessage);
        addCorrectionSuccess(messageResponse, xmlMessage);
        return messageResponse;
    }

    private static void addImageSuccess(iPartsXMLMediaMessage messageResponse, iPartsXMLMediaMessage xmlMessage) {
        String name = ((AbstractMediaOrderRequest)((iPartsXMLRequest)xmlMessage.getTypeObject()).getOperation()).getName();
        iPartsXMLSuccess success = createSuccess(name, "Das Bild mag ich nicht!", "I don't like this image!");
        messageResponse.getResponse().setSuccess(success);
    }

    private static void addCorrectionSuccess(iPartsXMLMediaMessage messageResponse, iPartsXMLMediaMessage xmlMessage) {
        AbstractXMLRequestOperation requestOperation = xmlMessage.getRequest().getOperation();
        String reason = ((iPartsXMLCorrectMediaOrder)requestOperation).getReason().getText();
        iPartsXMLSuccess success = createSuccess(reason, "Korrekturauftrag führte zu Fehler!", "Errors occured during correction workflow!");
        messageResponse.getResponse().setSuccess(success);
    }

    private static void addResponse(iPartsXMLMediaMessage messageResponse, iPartsXMLMediaMessage xmlMessage) {
        iPartsTransferNodeTypes nodeType = xmlMessage.getRequest().getOperation().getOperationType();
        iPartsXMLResponse response = createResponse(xmlMessage, nodeType);
        messageResponse.setTypeObject(response);
    }

    private static void addHistory(iPartsXMLMediaMessage messageResponse, iPartsXMLMediaMessage xmlMessage) {
        iPartsXMLHistory history = new iPartsXMLHistory();
        history.setRequest((iPartsXMLRequest)xmlMessage.getTypeObject());
        messageResponse.setHistory(history);
    }

    /**
     * Erzeugt einen simulierten Response zu einem CreateMcAttachments Request
     *
     * @param xmlMessage
     * @return
     */
    public static iPartsXMLMediaMessage createMcAttachmentResponse(iPartsXMLMediaMessage xmlMessage) {
        Random r = new Random();
        String errorText = "Invalid attachment with id %1. Item ID: %2 ItemRev ID: %3";

        AbstractXMLRequestOperation operation = xmlMessage.getRequest().getOperation();
        iPartsTransferNodeTypes nodeType = operation.getOperationType();
        if (nodeType != iPartsTransferNodeTypes.CREATE_MC_ATTACHMENTS) {
            return null;
        }
        // Neuen Response bauen
        iPartsXMLResponse response = createResponse(xmlMessage, nodeType);

        List<iPartsXMLAttachment> attachments = ((iPartsXMLCreateMcAttachments)operation).getAttachments();
        List<iPartsXMLAttachment> failedAttachments = new ArrayList<iPartsXMLAttachment>();
        for (iPartsXMLAttachment attachment : attachments) {
            if (attachment.getDescription().contains("error")) {
                failedAttachments.add(attachment);
            }
        }
        iPartsXMLSuccess success;
        if (failedAttachments.isEmpty()) {
            success = new iPartsXMLSuccess(true);
        } else {
            success = new iPartsXMLSuccess(false);
            success.setErrorCode(r.nextInt(1000));
            success.addError(new iPartsXMLErrorText("At least one attachment could not be created."));
        }
        response.setSuccess(success);

        // Success Objekte für ResCreateMcAttachments bauen (nur wenn ein Fehler auf ASPLM Seite aufkommt)
        if (!failedAttachments.isEmpty()) {
            iPartsXMLResCreateMcAttachments rcma = new iPartsXMLResCreateMcAttachments();
            List<String> values = new ArrayList<String>();
            for (iPartsXMLAttachment attachment : failedAttachments) {
                success = new iPartsXMLSuccess(false);
                success.setErrorCode(r.nextInt(1000));
                success.setTargetId(attachment.getId());
                values.add(attachment.getId());
                values.add(((iPartsXMLCreateMcAttachments)operation).getMcItemId());
                values.add(((iPartsXMLCreateMcAttachments)operation).getMcItemRevId());
                success.addError(new iPartsXMLErrorText(StrUtils.formatNumericPlaceholder(errorText, values)));
                values.clear();
                rcma.addSuccess(success);
            }
            response.setResult(rcma);
        }

        iPartsXMLMediaMessage messageResponse = new iPartsXMLMediaMessage(true);
        messageResponse.setTypeObject(response);

        return messageResponse;
    }

    /**
     * Erzeugt ein {@link iPartsXMLSuccess} mit den übergebenen Texten sofern im übergebenen <code>textToCheck</code>
     * der Trigger "error" enthalten ist
     *
     * @param textToCheck
     * @param deText
     * @param enText
     * @return
     */
    private static iPartsXMLSuccess createSuccess(String textToCheck, String deText, String enText) {
        iPartsXMLSuccess success;
        // Success abhängig vom Namen machen (bei enthaltenem "error" wird es ein Fehler)
        if (StrUtils.isValid(textToCheck) && textToCheck.toLowerCase().contains("error")) {
            success = new iPartsXMLSuccess(false);
            success.setErrorCode(23);

            // ErrorText für Deutsch
            iPartsXMLErrorText errorText = new iPartsXMLErrorText(deText);
            errorText.setTextID("0815");
            errorText.setLanguage("de");
            success.addError(errorText);

            // ErrorText für Englisch
            errorText = new iPartsXMLErrorText(enText);
            errorText.setTextID("0815");
            errorText.setLanguage("en");
            success.addError(errorText);
        } else {
            success = new iPartsXMLSuccess(true);
        }
        return success;
    }
}