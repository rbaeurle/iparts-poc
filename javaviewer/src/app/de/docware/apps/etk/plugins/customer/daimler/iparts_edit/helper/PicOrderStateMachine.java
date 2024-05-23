/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferStates;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;

/**
 * State machine für die möglichen States des Bildauftrags (für den Fall, dass ASPLM States zurückschickt
 */
public class PicOrderStateMachine {

    private static PicOrderStateMachine instance;

    public static PicOrderStateMachine getInstance() {
        if (instance == null) {
            instance = new PicOrderStateMachine();
        }
        return instance;
    }

    private PicOrderStateMachine() {

    }

    public iPartsTransferStates getNextState(iPartsDataPicOrder dataPicOrder, boolean success) {
        iPartsTransferStates currentState = dataPicOrder.getStatus();
        switch (currentState) {
            case ANGELEGT:
                if (success) {
                    return iPartsTransferStates.VERSORGT;
                }
                break;
            case CHANGE_CREATED:
                if (success) {
                    return iPartsTransferStates.CHANGE_REQUESTED;
                }
                break;
            case COPY_CREATED:
                if (success) {
                    return iPartsTransferStates.COPY_REQUESTED;
                }
                break;
            case VERSORGT:
                if (success) {
                    return iPartsTransferStates.ANGENOMMEN;
                } else {
                    return iPartsTransferStates.ABGELEHNT;
                }
            case CHANGE_REQUESTED:
                if (success) {
                    return iPartsTransferStates.CHANGE_RECEIVED;
                } else {
                    return iPartsTransferStates.CHANGE_ERROR;
                }
            case COPY_REQUESTED:
                if (success) {
                    return iPartsTransferStates.COPY_RECEIVED;
                } else {
                    return iPartsTransferStates.COPY_ERROR;
                }
            case ANGENOMMEN:
            case CHANGE_RECEIVED:
            case COPY_RECEIVED:
                if (success) {
                    if (dataPicOrder.hasAttachments()) {
                        return iPartsTransferStates.ATTACHMENTS_SENT;
                    }
                }
                break;
            case CORRECTION_MO_REQUESTED:
                if (success) {
                    return iPartsTransferStates.CORRECTION_MO_RECEIVED;
                } else {
                    return iPartsTransferStates.CORRECTION_MO_RECEIVED_ERROR;
                }
            case ATTACHMENTS_SENT:
                if (success) {
                    return iPartsTransferStates.ATTACHMENTS_ACCEPTED;
                } else {
                    return iPartsTransferStates.ATTACHMENTS_DENIED;
                }
            case RELEASED_ASPLM:
                if (success) {
                    return iPartsTransferStates.MEDIACONTENTS_REQUESTED;
                }
            case MEDIACONTENTS_REQUESTED:
                if (success) {
                    return iPartsTransferStates.MEDIACONTENTS_RECEIVED;
                } else {
                    return iPartsTransferStates.MEDIACONTENTS_RECEIVED_ERROR;
                }
            case PREVIEW_REQUESTED:
                if (success) {
                    return iPartsTransferStates.PREVIEW_RECEIVED;
                } else {
                    return iPartsTransferStates.PREVIEW_RECEIVED_ERROR;
                }
            case MC_NUMBER_REQUESTED:
                if (success) {
                    return iPartsTransferStates.MC_NUMBER_RECEIVED;
                } else {
                    return iPartsTransferStates.MC_NUMBER_REQUEST_ERROR;
                }
            case CONFIRMATION_SEND:
                if (success) {
                    return iPartsTransferStates.CONFIRMATION_RECEIVED;
                } else {
                    return iPartsTransferStates.CONFIRMATION_ERROR;
                }
            case CANCEL_REQUEST:
                if (success) {
                    return iPartsTransferStates.CANCEL_RESPONSE;
                } else {
                    return iPartsTransferStates.CANCEL_ERROR;
                }
        }
        // Als default wird immer der aktuelle Zustand zurückgegeben
        return currentState;
    }
}
