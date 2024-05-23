/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSPartsBaseCache;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * ReplacmenetPart Data Transfer Object für die iParts Webservices
 */
public class iPartsWSReplacementPart extends iPartsWSPartBase {

    private List<iPartsWSSupplementalPart> supplementalParts;
    private List<iPartsWSAlternativePart> alternativeParts;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSReplacementPart() {
    }

    public iPartsWSReplacementPart(EtkProject project, iPartsReplacement replacement, boolean includePartContext, boolean withExtendedDescriptions,
                                   boolean reducedInformation, boolean isSuccessor, boolean withIncludeParts) {
        //Werte aus PartBase müssen mit den entsprechenden Werten den Nachfolgers gefüllt werden
        iPartsDataPartListEntry replacementPartAsiPartsEntry = replacement.getReplacementPartAsiPartsEntry(isSuccessor);
        if (replacementPartAsiPartsEntry != null) {
            assignRMIValues(project, replacementPartAsiPartsEntry, includePartContext, reducedInformation);
            assignNonRMIValues(project, replacementPartAsiPartsEntry, withExtendedDescriptions);
        } else {
            if (isSuccessor) {
                // Alle Daten zur gemappten Teilenummer anzeigen (ist laut Daimler OK und erspart das nachträgliche Setzen
                // der gemappten Teilenummer inkl. ES1 und ES2 für das Originalteil aus dem Cache)
                assign(iPartsWSPartsBaseCache.getPartBaseFromCache(project, replacement.successorMappedPartNumber, withExtendedDescriptions));
                //PartContext bleibt hier leer, weil es das Nachfolger Teil nicht in der Stückliste gibt
            }
        }

        // Mitlieferteile
        if (withIncludeParts) {
            Collection<iPartsReplacement.IncludePart> includeParts = replacement.getIncludeParts(project);
            if (includeParts != null) {
                this.supplementalParts = new DwList<>(includeParts.size());
                for (iPartsReplacement.IncludePart includePart : includeParts) {
                    this.supplementalParts.add(new iPartsWSSupplementalPart(project, includePart, withExtendedDescriptions));
                }
            }
        }

        // Alternativteile bei PRIMUS-Ersetzungen ausgeben
        if (isSuccessor) {
            if ((replacement.source == iPartsReplacement.Source.PRIMUS) && iPartsPlugin.isShowAlternativePartsForPRIMUS()) {
                Set<EtkDataPart> alternativePartsList = iPartsPRIMUSReplacementsCache.getInstance(project).getAlternativeParts(replacement.successorPartNumber,
                                                                                                                               iPartsFilter.get(), project);
                if (alternativePartsList != null) {
                    alternativeParts = new DwList<>(alternativePartsList.size());
                    for (EtkDataPart alternativePart : alternativePartsList) {
                        alternativeParts.add(new iPartsWSAlternativePart(project, alternativePart, withExtendedDescriptions));
                    }
                }
            }
        }
    }

    // Getter and Setter
    public List<iPartsWSSupplementalPart> getSupplementalParts() {
        return supplementalParts;
    }

    public void setSupplementalParts(List<iPartsWSSupplementalPart> supplementalParts) {
        this.supplementalParts = supplementalParts;
    }

    public List<iPartsWSAlternativePart> getAlternativeParts() {
        return alternativeParts;
    }

    public void setAlternativeParts(List<iPartsWSAlternativePart> alternativeParts) {
        this.alternativeParts = alternativeParts;
    }
}