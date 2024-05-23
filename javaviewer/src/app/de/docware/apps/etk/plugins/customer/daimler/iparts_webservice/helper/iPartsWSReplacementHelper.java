/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSAlternativePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSReplacementPart;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Hilfsklasse für den Aufbau von Ersetzungsketten.
 */
public class iPartsWSReplacementHelper {

    private final EtkProject project;
    private final boolean includePartContext;
    private final boolean withExtendedDescriptions;
    private final String modelNumber;
    private final String countryCode;

    // zur Vermeidung von Kreisbezügen
    private Set<PartListEntryId> visitedEntriesForSuccessor;
    private Set<PartListEntryId> visitedEntriesForPredecessor;

    public iPartsWSReplacementHelper(EtkProject project, boolean includePartContext, boolean withExtendedDescriptions, String modelNumber,
                                     String countryCode, PartListEntryId partListEntryId) {
        this.project = project;
        this.includePartContext = includePartContext;
        this.withExtendedDescriptions = withExtendedDescriptions;
        this.modelNumber = modelNumber;
        this.countryCode = countryCode;
        visitedEntriesForSuccessor = new HashSet<>();
        visitedEntriesForSuccessor.add(partListEntryId);
        visitedEntriesForPredecessor = new HashSet<>();
        visitedEntriesForPredecessor.add(partListEntryId);
    }

    public List<iPartsWSReplacementPart> buildSuccessorChain(Collection<iPartsReplacement> replacements,
                                                             boolean isSuccessorDirection,
                                                             iPartsPRIMUSReplacementsCache primusReplacementsCache) {
        if (Utils.isValid(replacements)) {
            List<iPartsWSReplacementPart> result = new ArrayList<>(replacements.size());
            for (iPartsReplacement replacement : replacements) {
                iPartsDataPartListEntry iPartsReplacementEntry = replacement.getReplacementPartAsiPartsEntry(isSuccessorDirection);

                if (iPartsReplacementEntry == null) {
                    // keine Prüfung auf Kreis möglich, aber der Eintrag ist eh das Ende der Kette
                    iPartsWSReplacementPart replacementPart = new iPartsWSReplacementPart(
                            project, replacement, includePartContext, withExtendedDescriptions, false,
                            isSuccessorDirection, true);
                    // Ersatzkette auf Hinweis C74 prüfen
                    replacementPart.setPrimusCode74AvailableDependingOnPrimusRepCacheData(primusReplacementsCache);
                    result.add(replacementPart);
                } else {
                    if (!wasAlreadyVisited(iPartsReplacementEntry.getAsId(), isSuccessorDirection)) {
                        // zuerst das Teil selbst anlegen, und einfügen
                        iPartsWSReplacementPart replacementPart = new iPartsWSReplacementPart(
                                project, replacement, includePartContext, withExtendedDescriptions, false,
                                isSuccessorDirection, true);

                        // Ersatzkette auf Hinweis C74 prüfen
                        replacementPart.setPrimusCode74AvailableDependingOnPrimusRepCacheData(primusReplacementsCache);
                        result.add(replacementPart);

                        if (replacement.source != iPartsReplacement.Source.PRIMUS) {
                            addAlternativeParts(iPartsReplacementEntry, replacementPart);
                        }

                        // und jetzt die Kette weiter spinnen
                        List<iPartsReplacement> nextReplacements;
                        if (isSuccessorDirection) {
                            nextReplacements = iPartsReplacementEntry.getSuccessors(true);
                        } else {
                            nextReplacements = iPartsReplacementEntry.getPredecessors(true);
                        }
                        if (Utils.isValid(nextReplacements)) {
                            List<iPartsWSReplacementPart> nextChain = buildSuccessorChain(nextReplacements, isSuccessorDirection,
                                                                                          primusReplacementsCache);
                            replacementPart.addReplacementsToChain(nextChain, isSuccessorDirection);
                        }
                    }
                }
            }
            if (result.isEmpty()) {
                return null;
            }
            return result;
        }
        return null;
    }

    private void addAlternativeParts(iPartsDataPartListEntry replacementEntry, iPartsWSReplacementPart replacementPart) {
        if (replacementEntry != null) {
            Set<EtkDataPart> alternativeParts = replacementEntry.getAlternativePartsFilteredByReplacements(modelNumber, countryCode);
            if (alternativeParts != null) {
                List<iPartsWSAlternativePart> alternativePartsList = new DwList<>(alternativeParts.size());
                for (EtkDataPart alternativePart : alternativeParts) {
                    alternativePartsList.add(new iPartsWSAlternativePart(project, alternativePart, withExtendedDescriptions));
                }
                replacementPart.setAlternativeParts(alternativePartsList);
            }
        }
    }

    private boolean wasAlreadyVisited(PartListEntryId entryId, boolean isSuccessorDirection) {
        if (entryId == null) {
            return false;
        }
        if (isSuccessorDirection) {
            return !visitedEntriesForSuccessor.add(entryId);
        } else {
            return !visitedEntriesForPredecessor.add(entryId);
        }
    }
}
