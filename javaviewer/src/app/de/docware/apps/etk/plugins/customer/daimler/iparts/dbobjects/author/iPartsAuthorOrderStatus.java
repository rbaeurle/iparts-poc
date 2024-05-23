/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;

import java.util.EnumSet;

/**
 * Statuswerte für Author Orders.
 */
public enum iPartsAuthorOrderStatus {

    CREATED("CREATED"),
    ORDERED("ORDERED"),
    WORKING("WORKING"),
    DOCUMENTED("DOCUMENTED"),
    APPROVED("APPROVED"),
    UNKNOWN("");

    private static final String ENUM_KEY = "AuthorOrderStatus";

    private String dbValue;

    iPartsAuthorOrderStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDBValue() {
        return dbValue;
    }

    public String getDescription(EtkProject project) {
        if (project != null) {
            EnumValue enumValue = project.getEtkDbs().getEnumValue(ENUM_KEY);
            if (enumValue != null) {
                EnumEntry enumEntry = enumValue.get(name());
                if (enumEntry != null) {
                    return enumEntry.getEnumText().getText(project.getViewerLanguage());
                }
            }
        }

        return name(); // Fallback
    }

    private static EnumSet<iPartsAuthorOrderStatus> fromCreated = EnumSet.of(CREATED, ORDERED);
    private static EnumSet<iPartsAuthorOrderStatus> fromOrdered = EnumSet.of(CREATED, ORDERED, WORKING);
    private static EnumSet<iPartsAuthorOrderStatus> fromWorking = EnumSet.of(ORDERED, WORKING, DOCUMENTED);
    private static EnumSet<iPartsAuthorOrderStatus> fromDocumented = EnumSet.of(WORKING, DOCUMENTED, APPROVED);
    private static EnumSet<iPartsAuthorOrderStatus> fromApproved = EnumSet.noneOf(iPartsAuthorOrderStatus.class);

    private static EnumSet<iPartsAuthorOrderStatus> activateableStates = EnumSet.of(ORDERED, WORKING);

    public static iPartsAuthorOrderStatus getFromDBValue(String dbValue) {
        for (iPartsAuthorOrderStatus aoStatus : values()) {
            if (aoStatus.getDBValue().equalsIgnoreCase(dbValue)) {
                return aoStatus;
            }
        }
        return UNKNOWN;
    }

    public static EnumSet<iPartsAuthorOrderStatus> getGoToStatesForGivenState(iPartsAuthorOrderStatus currentState) {
        switch (currentState) {
            case CREATED:
                return fromCreated;
            case ORDERED:
                return fromOrdered;
            case WORKING:
                return fromWorking;
            case DOCUMENTED:
                return fromDocumented;
            case APPROVED:
                return fromApproved;
        }
        return EnumSet.noneOf(iPartsAuthorOrderStatus.class);
    }

    public static iPartsAuthorOrderStatus getNextState(iPartsAuthorOrderStatus currentState) {
        switch (currentState) {
            case CREATED:
                return ORDERED;
            case ORDERED:
                return WORKING;
            case WORKING:
                return DOCUMENTED;
            case DOCUMENTED:
                return APPROVED;
            case APPROVED:
                return APPROVED;
            case UNKNOWN:
                return CREATED;
        }
        return UNKNOWN;
    }

    public static iPartsAuthorOrderStatus getPrevState(iPartsAuthorOrderStatus currentState) {
        switch (currentState) {
            case CREATED:
                return CREATED;
            case ORDERED:
                return CREATED;
            case WORKING:
                return ORDERED;
            case DOCUMENTED:
                return WORKING;
            case APPROVED:
                return APPROVED;
        }
        return UNKNOWN;
    }

    public static iPartsAuthorOrderStatus getRealPrevState(iPartsAuthorOrderStatus currentState) {
        switch (currentState) {
            case CREATED:
                return UNKNOWN;
            case ORDERED:
                return CREATED;
            case WORKING:
                return ORDERED;
            case DOCUMENTED:
                return WORKING;
            case APPROVED:
                return DOCUMENTED;
        }
        return UNKNOWN;
    }

    public static boolean isStartState(iPartsAuthorOrderStatus currentState) {
        return currentState == CREATED;
    }

    public static boolean isEndState(iPartsAuthorOrderStatus currentState) {
        return currentState == APPROVED;
    }

    public static iPartsAuthorOrderStatus getEndState() {
        return APPROVED;
    }

    public static iPartsAuthorOrderStatus getStartState() {
        return CREATED;
    }

    public static EnumSet<iPartsAuthorOrderStatus> getActivateableStates() {
        return activateableStates;
    }

    public static EnumSet<iPartsAuthorOrderStatus> getAllNonActivateableStates() {
        EnumSet<iPartsAuthorOrderStatus> result = EnumSet.noneOf(iPartsAuthorOrderStatus.class);
        for (iPartsAuthorOrderStatus aoStatus : iPartsAuthorOrderStatus.values()) {
            if (aoStatus == iPartsAuthorOrderStatus.UNKNOWN) {
                continue;
            }
            if (!activateableStates.contains(aoStatus)) {
                result.add(aoStatus);
            }
        }
        return result;
    }

    public static boolean isNormalState(iPartsAuthorOrderStatus currentState) {
        return !isStartState(currentState) && !isEndState(currentState) && (currentState != UNKNOWN);
    }

    public static boolean isAssignUserAllowed(iPartsAuthorOrderStatus currentState) {
        return !isEndState(currentState) && (currentState != UNKNOWN);
    }

    public static boolean isChangeToPreviousState(iPartsAuthorOrderStatus currentState, iPartsAuthorOrderStatus destinationState) {
        return getRealPrevState(currentState) == destinationState;
    }

    public static boolean isChangeToNextState(iPartsAuthorOrderStatus currentState, iPartsAuthorOrderStatus destinationState) {
        return getNextState(currentState) == destinationState;
    }

    public static boolean isWorkState(iPartsAuthorOrderStatus currentState) {
        return (currentState == WORKING);
    }

    public static boolean isActivateableState(iPartsAuthorOrderStatus currentState) {
        return activateableStates.contains(currentState);
    }

    public static boolean isDocumentedState(iPartsAuthorOrderStatus currentState) {
        return (currentState == DOCUMENTED);
    }

    public static boolean isStateBefore(iPartsAuthorOrderStatus fixState, iPartsAuthorOrderStatus currentState) {
        return currentState.ordinal() < fixState.ordinal();
    }

    public static boolean isStateBeforeWorkState(iPartsAuthorOrderStatus currentState) {
        return isStateBefore(iPartsAuthorOrderStatus.WORKING, currentState);
    }
}
