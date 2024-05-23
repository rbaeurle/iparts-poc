/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumEntry;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumValue;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.util.collections.dwlist.DwList;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum iPartsDataReleaseState {

    NEW_READONLY("NEW_READONLY", null),
    NEW("NEW", NEW_READONLY),
    RELEASED_READONLY("RELEASED_READONLY", null),
    RELEASED("RELEASED", RELEASED_READONLY),
    NOT_RELEVANT_READONLY("NOT_RELEVANT_READONLY", null),
    NOT_RELEVANT("NOT_RELEVANT", NOT_RELEVANT_READONLY),
    CHECK_NOT_RELEVANT_READONLY("CHECK_NOT_RELEVANT_READONLY", null),
    CHECK_NOT_RELEVANT("CHECK_NOT_RELEVANT", CHECK_NOT_RELEVANT_READONLY),
    CHECK_DELETION_READONLY("CHECK_DELETION_READONLY", null),
    CHECK_DELETION("CHECK_DELETION", CHECK_DELETION_READONLY),
    DELETED_READONLY("DELETED_READONLY", null),
    DELETED("DELETED", DELETED_READONLY),
    UNKNOWN("UNKNOWN", null),

    // Interimslösung: Im ersten Wurf sollen alle übernommenen Werte auf "freigegeben" gesetzt werden.
    // DAIMLER-4807, Todo: Wenn die Editierbarkeit des Freigabestatus gegeben ist, muss nur der Importer-Default auf "NEU" geändert werden.
    IMPORT_DEFAULT(RELEASED.getDbValue(), RELEASED.getReadOnlyState()) // NICHT für Migrations-Importer verwenden -> dort ist RELEASED richtig
    // Vorbereitet für später:
//    IMPORT_DEFAULT(NEW.getDbValue())
    ;

    private static final EnumSet<iPartsDataReleaseState> notEditRelevantReleaseStates = EnumSet.of(NEW_READONLY, RELEASED_READONLY,
                                                                                                   NOT_RELEVANT_READONLY, CHECK_DELETION_READONLY,
                                                                                                   CHECK_DELETION, DELETED_READONLY,
                                                                                                   DELETED, UNKNOWN);

    private static final EnumSet<iPartsDataReleaseState> finalReleaseStates = EnumSet.of(RELEASED, DELETED);

    private static final EnumSet<iPartsDataReleaseState> deletedReleaseStates = EnumSet.of(CHECK_DELETION, CHECK_DELETION_READONLY, DELETED, DELETED_READONLY);

    // Statuswerte, bei denen der Ersetzungs-Datensatz beim Laden der Stückliste berücksichtigt werden muss
    public static final EnumSet<iPartsDataReleaseState> replacementRelevantStates = EnumSet.of(RELEASED, RELEASED_READONLY, CHECK_NOT_RELEVANT, CHECK_NOT_RELEVANT_READONLY);

    private String dbValue;
    private iPartsDataReleaseState readOnlyState;
    private static final String ENUM_KEY = "ModifiedDataReleaseState";

    iPartsDataReleaseState(String dbValue, iPartsDataReleaseState readOnlyState) {
        this.dbValue = dbValue;
        this.readOnlyState = readOnlyState;
    }

    public String getDbValue() {
        return dbValue;
    }

    public iPartsDataReleaseState getReadOnlyState() {
        return readOnlyState;
    }

    public boolean isReadOnly() {
        return readOnlyState == null;
    }

    public static iPartsDataReleaseState getTypeByDBValue(String value) {
        if (value != null) {
            value = value.trim();
            for (iPartsDataReleaseState type : values()) {
                if (type.getDbValue().equals(value)) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }

    public static boolean isReleased(String value) {
        return getTypeByDBValue(value) == RELEASED;
    }

    public String getDescription(EtkProject project) {
        if (project != null) {
            EnumValue enumValue = project.getEtkDbs().getEnumValue(ENUM_KEY);
            if (enumValue != null) {
                EnumEntry enumEntry = enumValue.get(name());
                if (enumEntry != null) {
                    return enumEntry.getEnumText().getTextByNearestLanguage(project.getViewerLanguage(), project.getDataBaseFallbackLanguages());
                }
            }
        }

        return name(); // Fallback
    }

    public boolean isNotEditRelevantReleaseState() {
        return notEditRelevantReleaseStates.contains(this);
    }

    public boolean isFinalReleaseState() {
        return finalReleaseStates.contains(this);
    }

    public boolean isDeletedReleaseState() {
        return deletedReleaseStates.contains(this);
    }

    /**
     * Liefert zurück, ob der Status "neu" oder "freigegeben" ist
     *
     * @return
     */
    public boolean isReleasedOrNew() {
        return (this == RELEASED) || (this == NEW);
    }

    public static List<String> getNotEditRelevantReleaseStatesDBValues() {
        List<String> result = new DwList<>();
        for (iPartsDataReleaseState state : notEditRelevantReleaseStates) {
            result.add(state.getDbValue());
        }
        return result;
    }

    public static Set<String> getReplacementRelevantStatesDBValues() {
        Set<String> result = new HashSet<>();
        for (iPartsDataReleaseState state : replacementRelevantStates) {
            result.add(state.getDbValue());
        }
        return result;
    }

    public static boolean isNotEditRelevantReleaseState(String state) {
        return getTypeByDBValue(state).isNotEditRelevantReleaseState();
    }

    /**
     * Ermittelt die nächsten möglichen Status zum aktuellen Status
     *
     * @return Liste der nächsten möglich Status
     */
    public List<iPartsDataReleaseState> getNextEditStates() {
        List<iPartsDataReleaseState> nextStates = new DwList<>();
        switch (this) {
            case NEW:
            case DELETED:
                nextStates.add(RELEASED);
                nextStates.add(NOT_RELEVANT);
                break;
            case CHECK_DELETION:
                nextStates.add(DELETED);
                nextStates.add(RELEASED);
                nextStates.add(NOT_RELEVANT);
                break;
            case RELEASED:
                nextStates.add(NOT_RELEVANT);
                break;
            case NOT_RELEVANT:
                nextStates.add(RELEASED);
                break;
            case CHECK_NOT_RELEVANT:
                nextStates.add(RELEASED);
                nextStates.add(NOT_RELEVANT);
                break;
        }
        return nextStates;
    }

    /**
     * Ermittelt die nächsten möglichen Status zum aktuellen als DB-Werte
     *
     * @return DB-Werte aller möglichen Status
     */
    public List<String> getNextEditStatesDBValues() {
        List<String> dbValues = new DwList<>();
        for (iPartsDataReleaseState nextEditState : getNextEditStates()) {
            dbValues.add(nextEditState.getDbValue());
        }
        return dbValues;
    }
}
