/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import java.util.EnumSet;

/**
 * Status für den Übersetzungsprozeß mit CLM
 */
public enum iPartsDictTransJobStates {

    FOR_TRANS,                  // zur Übersetzung (aktuell unbenutzt)
    TRANS_EXPORTED,             // Übersetzungsauftrag exportiert
    TRANSLATING,                // in Übersetzung
    TRANSLATED,                 // Übersetzt
    TRANS_ERROR_CLM,            // Übersetzungsauftrag fehlerhaft bei CLM
    TRANS_ERROR_TRANSLATION,    // Übersetzungsauftrag mit Übersetzungs-Fehlern importiert bei iParts
    TRANS_ERROR_TECHNICAL,      // Übersetzungsauftrag mit Technischen Fehlern importiert bei iParts
    TRANS_UNKNOWN;

    public static final EnumSet<iPartsDictTransJobStates> TRANSLATION_COMPLETED_STATES = java.util.EnumSet.of(TRANS_ERROR_CLM, TRANS_ERROR_TRANSLATION, TRANS_ERROR_TECHNICAL,
                                                                                                              TRANSLATED);

    iPartsDictTransJobStates() {

    }

    public String getDbValue() {
        return name();
    }

    public static iPartsDictTransJobStates getValue(String dbValue) {
        for (iPartsDictTransJobStates transJobState : iPartsDictTransJobStates.values()) {
            if (transJobState.getDbValue().equals(dbValue)) {
                return transJobState;
            }
        }
        return TRANS_UNKNOWN;
    }

    public static boolean isTranslationCompletedState(iPartsDictTransJobStates currentState) {
        return TRANSLATION_COMPLETED_STATES.contains(currentState);
    }
}
