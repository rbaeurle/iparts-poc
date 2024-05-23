/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper für das Vergleichen und Scoring von Objekten
 */
public class iPartsScoringHelper implements iPartsConst {

    /**
     * Vergleicht die übergebenen Stücklisteneinträge und gibt einen "Ähnlichkeits-Score" zurück
     *
     * @param firstPartListEntry
     * @param secondPartListEntry
     * @return
     */
    public static int calculatePartListEntryEqualityScore(EtkDataPartListEntry firstPartListEntry, EtkDataPartListEntry secondPartListEntry) {
        if ((firstPartListEntry == null) || (secondPartListEntry == null)) {
            return -1;
        }
        int score = 0;
        if (firstPartListEntry.getFieldValue(FIELD_K_POS).equals(secondPartListEntry.getFieldValue(FIELD_K_POS))) {
            score += 500;
        }
        if (firstPartListEntry.getFieldValue(FIELD_K_MENGE).equals(secondPartListEntry.getFieldValue(FIELD_K_MENGE))) {
            score += 5;
        }
        if (firstPartListEntry.getFieldValue(FIELD_K_AA).equals(secondPartListEntry.getFieldValue(FIELD_K_AA))) {
            score += 10;
        }
        if (firstPartListEntry.getFieldValue(FIELD_K_CODES).equals(secondPartListEntry.getFieldValue(FIELD_K_CODES))) {
            score += 100;
        }
        if (firstPartListEntry.getFieldValue(FIELD_K_HIERARCHY).equals(secondPartListEntry.getFieldValue(FIELD_K_HIERARCHY))) {
            score += 1;
        }
        if (firstPartListEntry.getFieldValue(FIELD_K_STEERING).equals(secondPartListEntry.getFieldValue(FIELD_K_STEERING))) {
            score += 10;
        }
        if (firstPartListEntry.getFieldValue(FIELD_K_PRODUCT_GRP).equals(secondPartListEntry.getFieldValue(FIELD_K_PRODUCT_GRP))) {
            score += 10;
        }
        if (firstPartListEntry.getFieldValueAsArrayOriginal(FIELD_K_SA_VALIDITY).equals(secondPartListEntry.getFieldValueAsArrayOriginal(FIELD_K_SA_VALIDITY))) {
            score += 100;
        }
        if (firstPartListEntry.getFieldValueAsArrayOriginal(FIELD_K_MODEL_VALIDITY).equals(secondPartListEntry.getFieldValueAsArrayOriginal(FIELD_K_MODEL_VALIDITY))) {
            score += 100;
        }
        if (firstPartListEntry.getFieldValue(FIELD_K_GEARBOX_TYPE).equals(secondPartListEntry.getFieldValue(FIELD_K_GEARBOX_TYPE))) {
            score += 10;
        }
        return score;
    }

    /**
     * Bestimmt aus der übergebenen Liste den Stücklisteneintrag der am ähnlichsten zum übergebenen Originalstücklisteneintrag ist.
     * Bei gleichem Score werden mehrere Stücklisteneinträge zurückgegeben.
     *
     * @param originalEntry
     * @param possibleEqualEntries
     * @return
     */
    public static List<EtkDataPartListEntry> getMostEqualPartListEntries(EtkDataPartListEntry originalEntry,
                                                                         List<? extends EtkDataPartListEntry> possibleEqualEntries) {
        List<EtkDataPartListEntry> equalEntries = new ArrayList<>();
        if (possibleEqualEntries.isEmpty()) {
            return equalEntries;
        } else if (possibleEqualEntries.size() == 1) {
            equalEntries.addAll(possibleEqualEntries);
        } else {
            int maxScore = -1;
            EtkDataPartListEntry bestPartListEntry = null;
            for (EtkDataPartListEntry possibleEqualPartListEntry : possibleEqualEntries) {
                int score = iPartsScoringHelper.calculatePartListEntryEqualityScore(originalEntry, possibleEqualPartListEntry);
                if (score > maxScore) {
                    maxScore = score;
                    bestPartListEntry = possibleEqualPartListEntry;
                    equalEntries.clear();
                } else if (score == maxScore) {
                    equalEntries.add(possibleEqualPartListEntry);
                }
            }
            if (bestPartListEntry != null) {
                equalEntries.add(bestPartListEntry);
            }
        }
        return equalEntries;
    }

}
