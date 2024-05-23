/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSAlternativePart;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;
import java.util.Set;

/**
 * Hilfsklasse für Alternativteile
 */
public class iPartsWSAlternativePartsHelper {

    /**
     * Erzeugt und befüllt eine Liste mit {@link iPartsWSAlternativePart}-Objekten
     *
     * @param alternativeMaterials
     * @param project
     * @return
     */
    public static List<iPartsWSAlternativePart> fillAlternativeParts(Set<EtkDataPart> alternativeMaterials, EtkProject project) {
        if (!alternativeMaterials.isEmpty()) {
            List<iPartsWSAlternativePart> alternativeParts = new DwList<>(alternativeMaterials.size());
            for (EtkDataPart alternativePart : alternativeMaterials) {
                alternativeParts.add(new iPartsWSAlternativePart(project, alternativePart, false));
            }
            return alternativeParts;
        }
        return null;
    }

}
