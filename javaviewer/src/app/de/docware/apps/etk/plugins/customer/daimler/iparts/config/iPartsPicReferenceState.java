/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReference;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Status für Bildreferenzen
 */
public enum iPartsPicReferenceState {
    NEW("NEW"),
    SEARCH_SENT("SEARCH_SENT"),
    SEARCH_RECEIVED("SEARCH_RECEIVED"),
    NOT_FOUND("NOT_FOUND"),
    SEARCH_ERROR("SEARCH_ERROR"),
    MEDIA_REQUESTED("MEDIA_REQUESTED"),
    MEDIA_RECEIVED("MEDIA_RECEIVED"),
    MEDIA_ERROR("MEDIA_ERROR"),
    PREVIEW_REQUESTED("PREVIEW_REQUESTED"),
    PREVIEW_RECEIVED("PREVIEW_RECEIVED"),
    PREVIEW_ERROR("PREVIEW_ERROR"),
    DONE("DONE");

    private static EnumSet<iPartsPicReferenceState> errorStates = EnumSet.of(SEARCH_ERROR, MEDIA_ERROR, PREVIEW_ERROR);

    private String dbValue;

    iPartsPicReferenceState(String dbValue) {
        this.dbValue = dbValue;
    }

    public static iPartsPicReferenceState getFromDBValue(String dbValue) {
        for (iPartsPicReferenceState state : values()) {
            if (state.dbValue.equalsIgnoreCase(dbValue)) {
                return state;
            }
        }
        return null;
    }

    public static List<iPartsPicReferenceState> getErrorAndNotFoundStates() {
        List<iPartsPicReferenceState> result = new ArrayList<iPartsPicReferenceState>(errorStates);
        result.add(NOT_FOUND);
        return result;
    }

    public static boolean isStateValidForTransitionWithoutMediaMessage(iPartsDataPicReference picReference) {
        iPartsPicReferenceState currState = picReference.getStatus();
        return (currState == iPartsPicReferenceState.NEW) || (currState == iPartsPicReferenceState.SEARCH_RECEIVED)
               || (currState == iPartsPicReferenceState.MEDIA_RECEIVED) || (currState == iPartsPicReferenceState.PREVIEW_RECEIVED)
               || (currState == iPartsPicReferenceState.DONE);
    }

    public static boolean canSendSearch(iPartsPicReferenceState currentState) {
        return (currentState == iPartsPicReferenceState.NEW) || (currentState == iPartsPicReferenceState.SEARCH_ERROR)
               || (currentState == iPartsPicReferenceState.NOT_FOUND);
    }

    public static boolean canSendGetMediaContents(iPartsPicReferenceState currentState) {
        return (currentState == iPartsPicReferenceState.SEARCH_RECEIVED);
    }

    public static boolean canSendGetMediaPreviews(iPartsPicReferenceState currentState) {
        return (currentState == iPartsPicReferenceState.MEDIA_RECEIVED);
    }

    public static boolean hasFinalState(iPartsDataPicReference picReference) {
        return picReference.getStatus() == iPartsPicReferenceState.DONE;
    }

    public static boolean hasError(iPartsPicReferenceState currentState) {
        return errorStates.contains(currentState);
    }

    public static boolean isNew(iPartsPicReferenceState currentState) {
        return (currentState == iPartsPicReferenceState.NEW);
    }

    public static boolean isNotFound(iPartsPicReferenceState currentState) {
        return (currentState == iPartsPicReferenceState.NOT_FOUND);
    }

    public static boolean isSendError(iPartsPicReferenceState currentState) {
        return (currentState == iPartsPicReferenceState.SEARCH_ERROR);
    }

    public String getDbValue() {
        return dbValue;
    }
}
