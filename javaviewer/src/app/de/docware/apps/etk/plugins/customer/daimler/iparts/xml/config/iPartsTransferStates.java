/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config;

import java.util.EnumMap;
import java.util.EnumSet;

/**
 * Werte für den Status eines Bildauftrages
 */
public enum iPartsTransferStates {

    ANGELEGT("angelegt", "CREATED"), // Bildauftrag angelegt aber noch nicht versendet (nur in der DB vorhanden)
    VERSORGT("versorgt", "SENT_TO"), // Bildauftrag an AS-PLM geschickt aber noch keine Bestätigung erhalten
    ANGENOMMEN("angenommen", "ACCEPTED"), // Von AS-PLM die Bestätigung erhalten, dass Bildauftrag angekommen ist
    ABGELEHNT("abgelehnt", "DENIED"), // Antwort von AS-PLM, dass Bildauftrag angekommen ist, es aber einen Fehler gegeben hat
    REJECTED("zurückgewiesen", "REJECTED"), // Bildauftrag wurde aktiv von AS-PLM zurückgewiesen (fehlende Infos)
    UPDATE_ERROR("Aktualisieren fehlgeschlagen", "UPDATE_ERROR"), // Aktualisierung eines zurückgewiesenen Bildauftrags ist fehlgeschlagen
    IN_BEARBEIT("in Bearbeitung", "PROCESSING"), // Wenn sich der AS-PLM User nach einer Korrekturanfrage/Bildauftrag ändert
    RELEASED_ASPLM("freigegeben", "RELEASED"), // Wird gesetzt, wenn von AS-PLM der Hinweis kommt, dass die Bilder fertig sind
    MEDIACONTENTS_REQUESTED("", "MEDIA_REQUESTED"), // Bildanfrage für fertige Zeichnungen abgeschickt
    MEDIACONTENTS_RECEIVED("", "MEDIA_RECEIVED"), // Fertige Bilder wurde von AS-PLM geliefert
    MEDIACONTENTS_RECEIVED_ERROR("Bildanfrage abgelehnt", "MEDIA_ERROR"), // Beim Anfragen der fertigen Bilder gab es bei AS-PLM einen Fehler
    CORRECTION_MO_REQUESTED("Korrekturauftrag verschickt", "COR_MO_REQUESTED"), // Bildkorrektur an AS-PLM geschickt aber noch keine Bestätigung erhalten
    CORRECTION_MO_RECEIVED("Korrekturauftrag angenommen", "COR_MO_RECEIVED"), // Von AS-PLM die Bestätigung erhalten, dass Bildkorrekturanfrage angekommen ist
    CORRECTION_MO_RECEIVED_ERROR("Korrekturauftrag abgelehnt", "COR_MO_ERROR"), // Antwort von AS-PLM, dass Bildkorrekturanfrage angekommen ist, es aber einen Fehler gegeben hat
    ABGESCHLOSSEN("abgeschlossen", "CLOSED"), // Bildauftrag abgeschlossen (Vorschaubilder noch nicht angefragt)
    ATTACHMENTS_SENT("Anhänge verschickt", "ATTACHMENTS_SENT"), // Anhänge zum Bildauftrag verschickt
    ATTACHMENTS_ACCEPTED("mit Anhängen angenommen", "ATTACHMENTS_ACCEPTED"), // Anhänge zum Bildauftrag wurden von AS-PLM akzeptiert
    ATTACHMENTS_DENIED("fehlerhafte Anhängen abgelehnt", "ATTACHMENTS_DENIED"), // Anhänge zum Bildauftrag sind bei AS-PLM angekommen, haben aber zu einem Fehler geführt
    CHANGE_CREATED("", "CHANGE_CREATED"), // Änderungsauftrag erzeugt aber noch nicht verschickt
    CHANGE_REQUESTED("", "CHANGE_REQUESTED"), // Änderungsauftrag an AS-PLM geschickt aber noch keine Bestätigung erhalten
    CHANGE_RECEIVED("", "CHANGE_RECEIVED"), // Von AS-PLM die Bestätigung erhalten, dass Änderungsauftrag angekommen ist
    CHANGE_ERROR("", "CHANGE_ERROR"), // Antwort von AS-PLM, dass Änderungsauftrag angekommen ist, es aber einen Fehler gegeben hat
    REPLACED_BY_CHANGE("", "REPLACED_BY_CHANGE"), // Bestehender Bild- oder Änderungsauftrag wurde durch einen neuen Änderungsauftrag ersetzt
    PREVIEW_REQUESTED("", "PREVIEW_REQUESTED"), // Vorschaubildanfrage zu den Zeichnungen im Bildauftrag abgeschickt
    PREVIEW_RECEIVED("", "PREVIEW_RECEIVED"), // Vorschaubilder wurde von AS-PLM geliefert
    PREVIEW_RECEIVED_ERROR("Vorschauanfrage abgelehnt", "PREVIEW_ERROR"), // Beim Anfragen der Vorschaubilder gab es bei AS-PLM einen Fehler
    MC_NUMBER_REQUESTED("Mediacontainer-Anfrage verschickt", "MC_NUMBER_REQUESTED"), // Zu einer Dasti Bildnummer wurde eine AS-PLM Bildnummeranfrage losgeschickt
    MC_NUMBER_RECEIVED("Mediacontainer erhalten", "MC_NUMBER_RECEIVED"), // Zu einer Bildnummeranfrage (mit Dasti Bildnummer) wurde eine AS-PLM Bildnummer geliefert
    MC_NUMBER_REQUEST_ERROR("Mediacontainer-Anfrage fehlerhaft", "MC_NUMBER_REQUEST_ERROR"), // Antwort von AS-PLM, dass Bildnummeranfrage angekommen ist, es aber einen Fehler gegeben hat
    MC_NUMBER_NOT_FOUND("Mediacontainer nicht gefunden", "MC_NUMBER_NOT_FOUND"), // Antwort von AS-PLM, dass zur Dasti Bildnummer keine AS-PLM Bildnummer gefunden werden konnte
    CONFIRMATION_SEND("", "CONFIRMATION_SEND"), // Bestätigung, dass iParts die Bilder übernommen hat
    CONFIRMATION_RECEIVED("", "CONFIRMATION_RECEIVED"), // Bestätigung, dass AS-PLM die Bestätigung von iParts erhalten hat
    CONFIRMATION_ERROR("", "CONFIRMATION_ERROR"), // Fehler bei der Bestätigungs-Kommunikation
    CANCEL_REQUEST("", "CANCEL_REQUEST"), // Stornierung wurde angefragt
    CANCEL_RESPONSE("", "CANCEL_RESPONSE"), // Stornierungsanfrage wurde akzeptiert
    CANCEL_ERROR("", "CANCEL_ERROR"), // Stornierungsanfrage war fehlerhaft
    CANCEL_CONFIRMATION("", "CANCEL_CONFIRMATION"), // Stornierung bestätigt
    COPY_CREATED("", "COPY_CREATED"), // Kopierauftrag erzeugt aber noch nicht verschickt
    COPY_REQUESTED("", "COPY_REQUESTED"), // Kopierauftrag an AS-PLM geschickt aber noch keine Bestätigung erhalten
    COPY_RECEIVED("", "COPY_RECEIVED"), // Von AS-PLM die Bestätigung erhalten, dass Kopierauftrag angekommen ist
    COPY_ERROR("", "COPY_ERROR"), // Antwort von AS-PLM, dass Kopierauftrag angekommen ist, es aber einen Fehler gegeben hat
    // Status nur für die Ansicht der Autorenaufträge
    OVERDUE("", "OVERDUE");


    public static final EnumSet<iPartsTransferStates> VALID_END_STATES = EnumSet.of(ABGESCHLOSSEN, PREVIEW_RECEIVED,
                                                                                    PREVIEW_RECEIVED_ERROR,
                                                                                    MC_NUMBER_REQUEST_ERROR,
                                                                                    MC_NUMBER_NOT_FOUND);
    private static final EnumSet<iPartsTransferStates> MC_NUMBER_DELETE_STATES = EnumSet.of(MC_NUMBER_REQUEST_ERROR,
                                                                                            MC_NUMBER_NOT_FOUND);
    private static final EnumSet<iPartsTransferStates> ATTACHMENT_EDITABLE_STATES = EnumSet.of(ANGELEGT, VERSORGT,
                                                                                               ANGENOMMEN,
                                                                                               CHANGE_CREATED,
                                                                                               COPY_CREATED,
                                                                                               CHANGE_REQUESTED,
                                                                                               COPY_REQUESTED,
                                                                                               CHANGE_RECEIVED,
                                                                                               COPY_RECEIVED,
                                                                                               REJECTED, UPDATE_ERROR);
    private static final EnumSet<iPartsTransferStates> CAN_REQUEST_CHANGE_STATES = EnumSet.of(ABGESCHLOSSEN,
                                                                                              PREVIEW_RECEIVED);
    private static final EnumSet<iPartsTransferStates> SAVE_TO_MQ_ALLOWED_STATES = EnumSet.of(ANGELEGT, CHANGE_CREATED,
                                                                                              COPY_CREATED,
                                                                                              MC_NUMBER_RECEIVED,
                                                                                              REJECTED, UPDATE_ERROR);
    private static final EnumSet<iPartsTransferStates> SAVE_TO_DB_NOT_ALLOWED_STATES = EnumSet.of(VERSORGT,
                                                                                                  CHANGE_REQUESTED,
                                                                                                  COPY_REQUESTED,
                                                                                                  MC_NUMBER_REQUESTED);
    private static final EnumSet<iPartsTransferStates> CAN_RECEIVE_PIC_ORDER_RESPONSE_STATES = EnumSet.of(VERSORGT,
                                                                                                          CHANGE_REQUESTED,
                                                                                                          COPY_REQUESTED,
                                                                                                          CORRECTION_MO_REQUESTED);
    private static final EnumSet<iPartsTransferStates> CAN_RECEIVE_EVENTS_STATES = EnumSet.of(ANGENOMMEN, IN_BEARBEIT,
                                                                                              ATTACHMENTS_ACCEPTED,
                                                                                              ATTACHMENTS_DENIED,
                                                                                              CORRECTION_MO_RECEIVED,
                                                                                              CHANGE_RECEIVED,
                                                                                              COPY_RECEIVED,
                                                                                              MC_NUMBER_RECEIVED,
                                                                                              MC_NUMBER_NOT_FOUND,
                                                                                              CANCEL_RESPONSE,
                                                                                              CANCEL_ERROR);
    private static final EnumSet<iPartsTransferStates> IS_VIEWING_MEDIA_CONTENTS_ALLOWED_STATES = EnumSet.of(CHANGE_CREATED,
                                                                                                             COPY_CREATED,
                                                                                                             MEDIACONTENTS_RECEIVED,
                                                                                                             PREVIEW_RECEIVED,
                                                                                                             PREVIEW_REQUESTED,
                                                                                                             PREVIEW_RECEIVED_ERROR,
                                                                                                             ABGESCHLOSSEN,
                                                                                                             REPLACED_BY_CHANGE);
    private static final EnumSet<iPartsTransferStates> IS_IN_CORRECTION_WORKFLOW_STATES = EnumSet.of(CORRECTION_MO_RECEIVED,
                                                                                                     CORRECTION_MO_REQUESTED);
    private static final EnumSet<iPartsTransferStates> CAN_EDIT_ATTACHMENTS_STATES = EnumSet.of(ANGELEGT, CHANGE_CREATED,
                                                                                                COPY_CREATED,
                                                                                                MC_NUMBER_RECEIVED,
                                                                                                REJECTED, UPDATE_ERROR);
    private static final EnumSet<iPartsTransferStates> CAN_BE_CHANGED_BY_EVENT_STATES = EnumSet.of(CORRECTION_MO_RECEIVED,
                                                                                                   ATTACHMENTS_ACCEPTED,
                                                                                                   IN_BEARBEIT);
    private static final EnumSet<iPartsTransferStates> CAN_SEND_ATTACHMENTS_STATES = EnumSet.of(ANGENOMMEN, IN_BEARBEIT,
                                                                                                CORRECTION_MO_RECEIVED,
                                                                                                CHANGE_RECEIVED,
                                                                                                COPY_RECEIVED);
    private static final EnumSet<iPartsTransferStates> CAN_RESEND_SEARCH_REQUEST = EnumSet.of(MC_NUMBER_REQUESTED,
                                                                                              MC_NUMBER_REQUEST_ERROR,
                                                                                              MC_NUMBER_NOT_FOUND);
    private static final EnumSet<iPartsTransferStates> CAN_RECEIVE_MEDIA_ORDER_RESPONSE = EnumSet.of(VERSORGT,
                                                                                                     CHANGE_REQUESTED,
                                                                                                     COPY_REQUESTED);

    private static final EnumSet<iPartsTransferStates> CAN_CANCEL_MEDIA_ORDER_STATES = EnumSet.of(REJECTED,
                                                                                                  MEDIACONTENTS_RECEIVED_ERROR,
                                                                                                  CORRECTION_MO_RECEIVED_ERROR,
                                                                                                  ATTACHMENTS_DENIED,
                                                                                                  PREVIEW_RECEIVED_ERROR,
                                                                                                  MC_NUMBER_REQUEST_ERROR,
                                                                                                  CONFIRMATION_ERROR,
                                                                                                  CANCEL_ERROR,
                                                                                                  UPDATE_ERROR);
    private static final EnumSet<iPartsTransferStates> CAN_DELETE_PIC_ORDER = EnumSet.of(ANGELEGT, CHANGE_CREATED,
                                                                                         COPY_CREATED, ABGELEHNT,
                                                                                         CHANGE_ERROR, COPY_ERROR,
                                                                                         MC_NUMBER_NOT_FOUND);
    private static final EnumSet<iPartsTransferStates> STATES_FOR_CANCEL_CONFIRMATION = EnumSet.of(CANCEL_REQUEST,
                                                                                                   CANCEL_RESPONSE,
                                                                                                   CANCEL_ERROR);
    private static final EnumSet<iPartsTransferStates> STATES_FOR_RELEASE_OR_REJECTION = EnumSet.of(ANGENOMMEN,
                                                                                                    CHANGE_RECEIVED,
                                                                                                    COPY_RECEIVED,
                                                                                                    ATTACHMENTS_ACCEPTED,
                                                                                                    IN_BEARBEIT);
    private static final EnumSet<iPartsTransferStates> REJECTED_STATES = EnumSet.of(REJECTED, UPDATE_ERROR);

    // Status für die Anzeige der Autorenaufträge. Sortiert nach einer vorgegebenen Reihenfolge.
    private static final EnumMap<iPartsTransferStates, Integer> AUTHOR_ORDER_TOTAL_STATES = new EnumMap<>(iPartsTransferStates.class);

    static {
        AUTHOR_ORDER_TOTAL_STATES.put(MEDIACONTENTS_RECEIVED, 1);
        AUTHOR_ORDER_TOTAL_STATES.put(MC_NUMBER_RECEIVED, 2);
        AUTHOR_ORDER_TOTAL_STATES.put(REJECTED, 3);
        AUTHOR_ORDER_TOTAL_STATES.put(UPDATE_ERROR, 4);
        AUTHOR_ORDER_TOTAL_STATES.put(ABGELEHNT, 5);
        AUTHOR_ORDER_TOTAL_STATES.put(MEDIACONTENTS_RECEIVED_ERROR, 6);
        AUTHOR_ORDER_TOTAL_STATES.put(CORRECTION_MO_RECEIVED_ERROR, 7);
        AUTHOR_ORDER_TOTAL_STATES.put(ATTACHMENTS_DENIED, 8);
        AUTHOR_ORDER_TOTAL_STATES.put(CHANGE_ERROR, 9);
        AUTHOR_ORDER_TOTAL_STATES.put(COPY_ERROR, 10);
        AUTHOR_ORDER_TOTAL_STATES.put(PREVIEW_RECEIVED_ERROR, 11);
        AUTHOR_ORDER_TOTAL_STATES.put(MC_NUMBER_REQUEST_ERROR, 12);
        AUTHOR_ORDER_TOTAL_STATES.put(MC_NUMBER_NOT_FOUND, 13);
        AUTHOR_ORDER_TOTAL_STATES.put(CANCEL_ERROR, 14);
        AUTHOR_ORDER_TOTAL_STATES.put(OVERDUE, 15);
        AUTHOR_ORDER_TOTAL_STATES.put(ANGELEGT, 16);
    }

    private final String asplmValue;
    private final String dbValue;

    iPartsTransferStates(String asplmValue, String dbValue) {
        this.asplmValue = asplmValue;
        this.dbValue = dbValue;
    }

    public static boolean isReplacedByChangeOrder(iPartsTransferStates state) {
        return state == REPLACED_BY_CHANGE;
    }

    // Bildaufträge dürfen mit DAIMLER-10090 nicht mehr gelöscht werden sobald es eine Interaktion mit ASPLM gab
    public static boolean canDeletePicOrder(iPartsTransferStates state) {
        return CAN_DELETE_PIC_ORDER.contains(state);
    }

    public static boolean isMCNumberDeleteState(iPartsTransferStates state) {
        return (state != null) && MC_NUMBER_DELETE_STATES.contains(state);
    }

    public static iPartsTransferStates getCancelConfirmState() {
        return CANCEL_CONFIRMATION;
    }

    public static boolean isCancelState(iPartsTransferStates state) {
        return STATES_FOR_CANCEL_CONFIRMATION.contains(state);
    }

    public static boolean isReleasedOrRejectionState(iPartsTransferStates state) {
        return STATES_FOR_RELEASE_OR_REJECTION.contains(state);
    }

    public static boolean isRejectedState(iPartsTransferStates state) {
        return REJECTED_STATES.contains(state);
    }

    public String getAsplmValue() {
        return asplmValue;
    }

    public String getDBValue() {
        return dbValue;
    }

    public static iPartsTransferStates getStartState() {
        return iPartsTransferStates.values()[0];
    }

    public static iPartsTransferStates getReleasedState() {
        return RELEASED_ASPLM;
    }

    public static iPartsTransferStates getRejectedState() {
        return REJECTED;
    }

    public static iPartsTransferStates getContentSendState() {
        return MEDIACONTENTS_REQUESTED;
    }

    public static boolean isErrorEndState(iPartsTransferStates actState) {
        return (actState == ABGELEHNT) || (actState == CHANGE_ERROR) || (actState == COPY_ERROR);
    }

    public static boolean isValidEndState(iPartsTransferStates actState) {
        return VALID_END_STATES.contains(actState);
    }

    public static boolean canReceiveMediaOrderResponse(iPartsTransferStates actState) {
        return CAN_RECEIVE_MEDIA_ORDER_RESPONSE.contains(actState);
    }

    public static boolean isEndState(iPartsTransferStates actState) {
        return isErrorEndState(actState) || isValidEndState(actState);
    }

    public static boolean isReleasedState(iPartsTransferStates actState) {
        return isEndState(actState) || isReplacedByChangeOrder(actState);
    }

    /**
     * Liefert zurück, ob der Bildauftrag das Löschen des Moduls verhindert oder nicht
     *
     * @param currentState
     * @return
     */
    public static boolean isDeleteModuleAllowedState(iPartsTransferStates currentState) {
        return isReleasedState(currentState) || (currentState == CANCEL_CONFIRMATION);
    }


    public static boolean canRequestChange(iPartsTransferStates currentState) {
        return CAN_REQUEST_CHANGE_STATES.contains(currentState);
    }

    public static boolean isAttachmentEditableState(iPartsTransferStates actState) {
        return ATTACHMENT_EDITABLE_STATES.contains(actState);
    }

    public static boolean isEventState(iPartsTransferStates state) {
        return (state == IN_BEARBEIT);
    }


    public static iPartsTransferStates getFromAlias(String alias) {
        for (iPartsTransferStates result : values()) {
            if (result.asplmValue.equals(alias)) {
                return result;
            }
        }
        return null;
    }

    public static iPartsTransferStates getFromDB(String dbValue) {
        for (iPartsTransferStates result : values()) {
            if (result.dbValue.equals(dbValue)) {
                return result;
            }
        }
        return null;
    }

    public boolean isSaveToMQ_Allowed(String dbState) {
        return isSaveToMQ_Allowed(iPartsTransferStates.getFromDB(dbState));
    }

    public static boolean isSaveToMQ_Allowed(iPartsTransferStates currentState) {
        return (currentState == null) || SAVE_TO_MQ_ALLOWED_STATES.contains(currentState);
    }

    public boolean isSaveToDB_Allowed(String dbState) {
        return isSaveToDB_Allowed(iPartsTransferStates.getFromDB(dbState));
    }

    public static boolean isSaveToDB_Allowed(iPartsTransferStates currentState) {
        return isSaveToMQ_Allowed(currentState) && !SAVE_TO_DB_NOT_ALLOWED_STATES.contains(currentState);
    }

    public static boolean canReceivePicOrderResponse(iPartsTransferStates currentState) {
        return CAN_RECEIVE_PIC_ORDER_RESPONSE_STATES.contains(currentState);
    }

    public static boolean canReceivePicContentResponse(iPartsTransferStates currentState) {
        return currentState == iPartsTransferStates.getContentSendState();
    }

    public static boolean canReceiveEventsFromMQ(iPartsTransferStates currentState) {
        return CAN_RECEIVE_EVENTS_STATES.contains(currentState);
    }

    public static boolean canSendAttachments(iPartsTransferStates currentState) {
        return CAN_SEND_ATTACHMENTS_STATES.contains(currentState);
    }

    public static boolean canSendGetMediaContents(iPartsTransferStates currentState) {
        return currentState == iPartsTransferStates.getReleasedState();
    }

    public static boolean canCancelMediaOrder(iPartsTransferStates currentState) {
        return CAN_CANCEL_MEDIA_ORDER_STATES.contains(currentState);
    }

    public static boolean isViewingMediaContentsAllowed(iPartsTransferStates currentState) {
        return IS_VIEWING_MEDIA_CONTENTS_ALLOWED_STATES.contains(currentState);
    }

    public static boolean isInCorrectionWorkflow(iPartsTransferStates currentState) {
        return IS_IN_CORRECTION_WORKFLOW_STATES.contains(currentState);
    }

    public static boolean canEditAttachments(iPartsTransferStates currentState) {
        return (currentState == null) || CAN_EDIT_ATTACHMENTS_STATES.contains(currentState);
    }

    public static boolean canBeFinished(iPartsTransferStates currentState) {
        return (currentState == iPartsTransferStates.MEDIACONTENTS_RECEIVED);
    }

    public static boolean canRequestCorrection(iPartsTransferStates currentState) {
        return canBeFinished(currentState);
    }

    public static iPartsTransferStates getStateForSaveToDB(iPartsTransferStates state) {
        if (state == null) {
            return iPartsTransferStates.getStartState();
        } else {
            return null;
        }

    }

    public static boolean canBeChangedByEvent(iPartsTransferStates currentState) {
        return CAN_BE_CHANGED_BY_EVENT_STATES.contains(currentState);
    }

    public static boolean canResendMCSearchRequest(iPartsTransferStates currentState) {
        return CAN_RESEND_SEARCH_REQUEST.contains(currentState);
    }

    /**
     * Liefert zurück, ob dieser Status relevant für die Ansicht der Autorenaufträge ist
     *
     * @return
     */
    public boolean isRelevantForAutorOrder() {
        return AUTHOR_ORDER_TOTAL_STATES.containsKey(this);
    }

    /**
     * Ist die eigene Priorität höher als die Priorität vom übergebenen {@link iPartsTransferStates}?
     *
     * @param stateForComparison
     * @return {@code true} auch dann, wenn {@code stateForComparison == null}
     */
    public boolean hasHigherPrioThan(iPartsTransferStates stateForComparison) {
        if (stateForComparison == null) {
            return true;
        }

        if (!isRelevantForAutorOrder() && !stateForComparison.isRelevantForAutorOrder()) {
            return false;
        }
        if (isRelevantForAutorOrder() && !stateForComparison.isRelevantForAutorOrder()) {
            return true;
        }
        if (!isRelevantForAutorOrder()) {
            return false;
        }

        return AUTHOR_ORDER_TOTAL_STATES.get(this) < AUTHOR_ORDER_TOTAL_STATES.get(stateForComparison);
    }

    public boolean hasHighestPrio() {
        if (isRelevantForAutorOrder()) {
            return AUTHOR_ORDER_TOTAL_STATES.get(this) == 1;
        }
        return false;
    }

    public static boolean isCreatedChangeOrCopyOrder(iPartsTransferStates state) {
        return (state == CHANGE_CREATED) || (state == COPY_CREATED);
    }
}