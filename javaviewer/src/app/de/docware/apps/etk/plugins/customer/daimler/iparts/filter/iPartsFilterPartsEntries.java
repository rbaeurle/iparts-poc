/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Alle zu einem PartsListEntry für die Filterung relevanten Entries
 * Für den Baumusterfilter sind das z.B. die Positionsvarianten und für den Federfilter alle mit gleichem Hotspot + die Positionsvarianten
 * Die Klasse dient dazu den aktuellen Filterstatus der Teile zu speichern und kann später als Cache verwendet werden
 */
public class iPartsFilterPartsEntries {


    private boolean finished; // Flag ob die iPartsFilterPartsEntries noch weiter ausgewertet werden müssen
    private boolean isRelevantForSameHotSpotFilters = false;
    private iPartsDataAssembly partListOwnerAssembly;

    private Map<PartListEntryId, EntryStatus> entryStatus = new HashMap<>();
    private List<List<iPartsDataPartListEntry>> allPositionVariants = new ArrayList<>();

    static public iPartsFilterPartsEntries getInstance(iPartsDataPartListEntry entry) {
        if (entry.getOwnerAssembly().isSpringRelevant() || entry.getOwnerAssembly().isSpecialZBFilterRelevant()) {
            // Bei bestimmten Filter müssen alle Teile mit dem gleichen Hotspot ermittelt werden (Feder, Spezialfilter ZB-Sachnummer)
            // und damit die Filterung funktioniert natürlich für diese ganzen Teile auch alle Positionsvarianten
            iPartsFilterPartsEntries entries = new iPartsFilterPartsEntries(entry.getOwnerAssembly(), entry.getFieldValue(iPartsConst.FIELD_K_POS));
            entries.setIsRelevantForSameHotSpotFilters(true); // Die Sammlung der Entries ist relevant für die Filter auf Basis der Hotspots
            return entries;
        } else {
            return new iPartsFilterPartsEntries(entry);
        }
    }

    private iPartsFilterPartsEntries(iPartsDataAssembly assembly, String hotspot) {
        partListOwnerAssembly = assembly;
        for (EtkDataPartListEntry entry : partListOwnerAssembly.getPartListUnfiltered(null)) {
            if (entry instanceof iPartsDataPartListEntry) {
                if (entry.getFieldValue(iPartsConst.FIELD_K_POS).equals(hotspot)) {
                    if (!isInOnePositionVariant(entry.getAsId())) {
                        addPositionVariants(getPosVariantsForEntry((iPartsDataPartListEntry)entry));
                    }
                }
            }
        }
    }

    /**
     * Konstruktor NUR für Unittests.
     */
    protected iPartsFilterPartsEntries() {
    }

    private iPartsFilterPartsEntries(iPartsDataPartListEntry entry) {
        super();
        partListOwnerAssembly = entry.getOwnerAssembly();
        addPositionVariants(getPosVariantsForEntry(entry));
    }

    public boolean isRelevantForSameHotSpotFilters() {
        return isRelevantForSameHotSpotFilters;
    }

    public void setIsRelevantForSameHotSpotFilters(boolean isRelevantForSameHotSpotFilters) {
        this.isRelevantForSameHotSpotFilters = isRelevantForSameHotSpotFilters;
    }

    /**
     * Prüft, ob das Teil schon in einer der Positionsvarianten dabei ist
     *
     * @param id
     * @return
     */
    private boolean isInOnePositionVariant(PartListEntryId id) {
        // Alle Teile der geladenen Poisitionvarianten sind auch in der Map
        return (entryStatus.get(id) != null);
    }


    private List<iPartsDataPartListEntry> getPosVariantsForEntry(iPartsDataPartListEntry partListEntry) {
        List<EtkDataPartListEntry> cachedVariants = partListEntry.getOwnerAssembly().getPositionVariants(partListEntry, false);

        List<iPartsDataPartListEntry> result = new ArrayList<iPartsDataPartListEntry>();
        if (cachedVariants != null) {
            for (EtkDataPartListEntry entry : cachedVariants) {
                if (entry instanceof iPartsDataPartListEntry) {
                    result.add((iPartsDataPartListEntry)entry);
                }
            }
        }
        if (result.isEmpty()) {
            result.add(partListEntry);
        }

        return Collections.unmodifiableList(result);
    }


    /**
     * Dummyobjekt nur für den Test erzeugen
     *
     * @param positionVariants
     * @return
     */
    public static iPartsFilterPartsEntries getInstanceForDataCardTest(List<iPartsDataPartListEntry> positionVariants) {
        iPartsFilterPartsEntries result = new iPartsFilterPartsEntries();
        result.partListOwnerAssembly = positionVariants.get(0).getOwnerAssembly();
        result.addPositionVariants(positionVariants);
        return result;
    }

    private void addPositionVariants(List<iPartsDataPartListEntry> positionVariants) {
        allPositionVariants.add(positionVariants);

        // Alle Entries am Anfang als visible in die Statusliste eintragen
        for (iPartsDataPartListEntry entry : positionVariants) {
            entryStatus.put(entry.getAsId(), new EntryStatus(true, false));
        }
    }


    public Collection<List<iPartsDataPartListEntry>> getAllPositionsVariants() {
        return Collections.unmodifiableList(allPositionVariants);
    }

    /**
     * "hide" heißt im Prinzip ausgefiltert. Der Eintrag bleibt aber drin weil er noch für andere
     * Zwecke (z.B. Filtergrund) benötigt wird.
     *
     * @param partListEntry
     */
    public void hideEntry(EtkDataPartListEntry partListEntry) {
        entryStatus.put(partListEntry.getAsId(), new EntryStatus(false, false));
    }

    /**
     * Ist der Stücklisteneintrag noch sichtbar (wurde also noch nicht ausgefiltert)?
     *
     * @param partListEntry
     * @return
     */
    public boolean isEntryVisible(EtkDataPartListEntry partListEntry) {
        EntryStatus status = entryStatus.get(partListEntry.getAsId());
        if (status != null) {
            return status.visible;
        }
        return true;
    }

    /**
     * Liefert alle Positionsvarianten, die nicht unsichtbar sind (d.h. nicht ausgefiltert wurden)
     *
     * @return
     */
    public List<iPartsDataPartListEntry> getVisibleEntries() {
        List<iPartsDataPartListEntry> result = new ArrayList<iPartsDataPartListEntry>();

        for (Collection<iPartsDataPartListEntry> positionVariants : getAllPositionsVariants()) {
            for (iPartsDataPartListEntry entry : positionVariants) {
                if (isEntryVisible(entry)) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }


    /**
     * Müssen die iPartsFilterPartsEntries noch weiter ausgewertet werden?
     * Vor jeder Verarbeitung von iPartsFilterPartsEntries ist dieses Flag zu prüfen. Bei false entfällt die Prüfung
     *
     * @return
     */
    public boolean isFinished() {
        return finished;
    }

    public void setHighPrio(iPartsDataPartListEntry entry) {
        entryStatus.put(entry.getAsId(), new EntryStatus(true, true));
    }

    public boolean hasHighPrioEntries() {
        for (EntryStatus status : entryStatus.values()) {
            if (status.highPrio) {
                return true;
            }
        }
        return false;
    }

    public void setHigherScoringThanOmittedPart(iPartsDataPartListEntry entry, boolean higherScoringThanOmittedPart) {
        EntryStatus entryStatusOfPLE = entryStatus.get(entry.getAsId());
        if (entryStatusOfPLE == null) {
            entryStatusOfPLE = new EntryStatus(true, false);
        }
        entryStatusOfPLE.setHigherScoringThanOmittedPart(higherScoringThanOmittedPart);
    }

    public boolean isHigherScoringThanOmittedPart(iPartsDataPartListEntry entry) {
        EntryStatus entryStatusOfPLE = entryStatus.get(entry.getAsId());
        if (entryStatusOfPLE != null) {
            return entryStatusOfPLE.isHigherScoringThanOmittedPart();
        } else {
            return false;
        }
    }

    public void hideAllExceptHighPrioEntries(iPartsFilter filter, iPartsFilterSwitchboard.FilterTypes filterType, String filterReason) {
        for (Map.Entry<PartListEntryId, EntryStatus> status : entryStatus.entrySet()) {
            if (!status.getValue().highPrio) {
                status.getValue().visible = false;
                if ((filter != null) && filter.isWithFilterReason()) {
                    iPartsDataPartListEntry entry = getEntryById(status.getKey());
                    if (entry != null) {
                        filter.setFilterReasonForPartListEntry(entry, filterType, filterReason);
                    }
                }
            }
        }
    }

    public void hideAll(iPartsFilter filter, iPartsFilterSwitchboard.FilterTypes filterType, String filterReason) {
        for (Map.Entry<PartListEntryId, EntryStatus> status : entryStatus.entrySet()) {
            status.getValue().visible = false;
            if ((filter != null) && filter.isWithFilterReason()) {
                iPartsDataPartListEntry entry = getEntryById(status.getKey());
                if (entry != null) {
                    filter.setFilterReasonForPartListEntry(entry, filterType, filterReason);
                }
            }
        }
    }

    public iPartsDataAssembly getPartListEntriesOwnerAssembly() {
        return partListOwnerAssembly;
    }

    public List<iPartsDataPartListEntry> getAllPositionsVariantsAsSingleList() {
        List<iPartsDataPartListEntry> result = new DwList<>();

        for (Collection<iPartsDataPartListEntry> positionVariants : getAllPositionsVariants()) {
            result.addAll(positionVariants);
        }
        return result;
    }


    public iPartsDataPartListEntry getEntryById(PartListEntryId id) {
        for (Collection<iPartsDataPartListEntry> positionVariants : getAllPositionsVariants()) {
            for (iPartsDataPartListEntry entry : positionVariants) {
                if (entry.getAsId().equals(id)) {
                    return entry;
                }
            }
        }
        return null;
    }


    /**
     * Setzt alle Entries dieses {@link iPartsFilterPartsEntries} Objekts, die nicht in dem übergebenen Set enthalten
     * sind auf "nicht sichtbar". Entries, die in dem übergebenen Set enthalten sind und zuvor den Status "nicht sichtbar"
     * hatten, werden weiterhin als "nicht sichtbar" deklariert.
     *
     * @param validEntries
     * @param filter
     * @param filterType
     * @param filterReasonTranslationKey
     * @param filterReasonPlaceholders
     */
    public void setVisibleEntries(Set<EtkDataPartListEntry> validEntries, iPartsFilter filter, iPartsFilterSwitchboard.FilterTypes filterType,
                                  String filterReasonTranslationKey, String... filterReasonPlaceholders) {
        for (iPartsDataPartListEntry partListEntry : getVisibleEntries()) {
            if (!validEntries.contains(partListEntry)) {
                if ((filter != null) && filter.isWithFilterReason()) {
                    filter.setFilterReasonForPartListEntry(partListEntry, filterType, filterReasonTranslationKey, filterReasonPlaceholders);
                }
                hideEntry(partListEntry);
            } else {
                // Falls Stücklisteneintrag bisher sichtbar war, einen evtl. zwischendurch gesetzten Filtergrund (z.B.
                // beim Iterieren über mehrere Baumuster/Datenkarten) wieder entfernen
                if ((filter != null) && filter.isWithFilterReason()) {
                    filter.clearFilterReasonForDataObject(partListEntry, true);
                }
            }
        }
    }

    public iPartsFilterPartsEntries cloneMe() {
        iPartsFilterPartsEntries clone = new iPartsFilterPartsEntries();
        clone.finished = finished;
        clone.isRelevantForSameHotSpotFilters = isRelevantForSameHotSpotFilters;
        clone.partListOwnerAssembly = partListOwnerAssembly;

        for (Map.Entry<PartListEntryId, EntryStatus> entryStatusEntry : entryStatus.entrySet()) {
            EntryStatus entry = entryStatusEntry.getValue();
            EntryStatus entryClone = new EntryStatus(entry.visible, entry.highPrio);
            clone.entryStatus.put(entryStatusEntry.getKey(), entryClone);
        }

        clone.allPositionVariants.addAll(allPositionVariants);
        return clone;
    }

    public EtkProject getEtkProject() {
        return getPartListEntriesOwnerAssembly().getEtkProject();
    }

    private class EntryStatus {

        boolean visible;  // false heißt im Prinzip "ausgefiltert"
        boolean highPrio;
        boolean higherScoringThanOmittedPart;

        EntryStatus(boolean visible, boolean highPrio) {
            this.visible = visible;
            this.highPrio = highPrio;
        }

        public boolean isHigherScoringThanOmittedPart() {
            return higherScoringThanOmittedPart;
        }

        public void setHigherScoringThanOmittedPart(boolean higherScoringThanOmittedPart) {
            this.higherScoringThanOmittedPart = higherScoringThanOmittedPart;
        }
    }

}