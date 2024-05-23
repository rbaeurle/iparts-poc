/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.timeslice;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterPartsEntries;

import java.util.List;

/**
 * Zeitscheibenliste der Positionsvarianten. Über die Werkseinsatzdaten sind zu verschiedenen Zeitpunkten verschiedene Positionsvarianten zu bestimmten Zeiten gültig.
 * Bei jeder Änderung enthält diese Liste einen neuen Eintrag mit den dann gültigen Positionsvarianten.
 * Für einen bestimmten Stichtag muss einfach die gültige Zeitscheibe gefunden werden.
 * Ermittelt werden die Zeitscheiben mit der Funktion {@link iPartsFilterTimeSliceHelper#calcTimeSliceMapFromPVList(List, iPartsFilterPartsEntries, boolean)}.
 */
public class iPartsPositionVariantsTimeLine {

    private long fromDate;
    private long toDate;
    private List<EtkDataPartListEntry> positionVariants;

    public iPartsPositionVariantsTimeLine(long fromDate, long toDate, List<EtkDataPartListEntry> positionVariants) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.positionVariants = positionVariants;
    }

    public boolean isValidDate(long buildDate) {
        return (buildDate >= fromDate) && (buildDate < toDate);
    }

    public long getFromDate() {
        return fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public List<EtkDataPartListEntry> getPositionVariants() {
        return positionVariants;
    }
}
