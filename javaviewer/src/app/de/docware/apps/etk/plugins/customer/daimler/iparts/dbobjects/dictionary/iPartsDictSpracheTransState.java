/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

public enum iPartsDictSpracheTransState {

    IN_TRANSLATION_WORKFLOW("IN_TRANSLATION_WORKFLOW"), // Übersetzungsauftrag wurde exportiert, der Text darf nicht mehr verändert werden
    TRANSLATION_RECEIVED("TRANSLATION_RECEIVED"),   // Übersetzung für diese Sprache wurde importiert
    RELEVANT_FOR_TRANSLATION(""); // Noch nicht übersetzt (default, falls Feld leer z.B. bei neuen Texten)

    iPartsDictSpracheTransState(String dbValue) {
        this.dbValue = dbValue;
    }

    private String dbValue;

    public String getDbValue() {
        return dbValue;
    }

    public static iPartsDictSpracheTransState getValue(String dbValue) {
        for (iPartsDictSpracheTransState transJobState : iPartsDictSpracheTransState.values()) {
            if (transJobState.getDbValue().equals(dbValue)) {
                return transJobState;
            }
        }
        return RELEVANT_FOR_TRANSLATION;
    }


}
