/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sort.SortUtils;

import java.util.*;

/**
 * Hilfsklasse zur Bestimmung der Wahlweise-Teile mit und ohne Berücksichtigung der aktuellen Filterung.
 * Echte Wahlweise- und Extra-Wahlweise-Teile können separat abgefragt werden.
 */
public class iPartsWWPartsHelper {

    public static String K_LFDNR_NOT_IN_MODULE = "-1";

    /**
     * Bestimmt alle echten Wahlweise-Teile zu einem Stücklisteneintrag. {@link iPartsConst#FIELD_K_WW)
     * Es werden Doppeleinträge verhindert.
     *
     * @param entry
     * @param withFilter Bei {@code true} werden nur Wahlweise-Teile berücksichtigt, die auch nach der Filterung noch in
     *                   der Stückliste enthalten sind
     * @return
     */
    public static Collection<EtkDataPartListEntry> getRealWWParts(iPartsDataPartListEntry entry, iPartsFilter filter) {
        Set<EtkDataPartListEntry> realWWParts = new LinkedHashSet<EtkDataPartListEntry>();
        if (entry != null) {
            String wwGuid = entry.getFieldValue(iPartsConst.FIELD_K_WW);
            List<EtkDataPartListEntry> wwPartListEntries = entry.getOwnerAssembly().getWWPartListEntries(wwGuid);
            if (wwPartListEntries != null) {
                for (EtkDataPartListEntry wwPartListEntry : wwPartListEntries) {
                    if (!wwPartListEntry.getAsId().equals(entry.getAsId()) && ((filter == null) || filter.checkFilter(wwPartListEntry))) {
                        realWWParts.add(wwPartListEntry);
                    }
                }
            }
        }

        return realWWParts;
    }

    public static Collection<EtkDataPartListEntry> getRealWWParts(iPartsDataPartListEntry entry, boolean withFilter) {
        return getRealWWParts(entry, withFilter ? iPartsFilter.get() : null);
    }

    public static boolean isZZ(String ddGes) {
        if (!ddGes.isEmpty()) {
            String zzFlag = StrUtils.stringBetweenIndizes(ddGes, 2, 4); // Stellen 3 + 4 bei 1-basiertem Index
            return zzFlag.equals("ZZ");
        }
        return false;
    }

    /**
     * Bestimmt alle echten Wahlweise-Teile zu einem Stücklisteneintrag. {@link iPartsConst#FIELD_K_WW)
     * Es werden alle Einträge genommen.
     *
     * @param entry
     * @param withFilter Bei {@code true} werden nur Wahlweise-Teile berücksichtigt, die auch nach der Filterung noch in
     *                   der Stückliste enthalten sind
     * @return
     */
    public static Collection<EtkDataPartListEntry> getAllRealWWParts(iPartsDataPartListEntry entry, boolean withFilter) {
        Set<EtkDataPartListEntry> realWWParts = new LinkedHashSet<EtkDataPartListEntry>();
        if (entry != null) {
            String wwGuid = entry.getFieldValue(iPartsConst.FIELD_K_WW);
            List<EtkDataPartListEntry> wwPartListEntries = entry.getOwnerAssembly().getWWPartListEntries(wwGuid);
            if (wwPartListEntries != null) {
                iPartsFilter filter = iPartsFilter.get();
                for (EtkDataPartListEntry wwPartListEntry : wwPartListEntries) {
                    if (!withFilter || filter.checkFilter(wwPartListEntry)) {
                        realWWParts.add(wwPartListEntry);
                    }
                }
            }
        }

        return realWWParts;
    }

    /**
     * Hat der Stücklisteneintrag echte Wahlweise-Teile? {@link iPartsConst#FIELD_K_WW)
     *
     * @param entry
     * @param withFilter Bei {@code true} werden nur Wahlweise-Teile berücksichtigt, die auch nach der Filterung noch in
     *                   der Stückliste enthalten sind
     * @return
     */
    public static boolean hasRealWWParts(iPartsDataPartListEntry entry, boolean withFilter) {
        Collection<EtkDataPartListEntry> realWWParts = getRealWWParts(entry, withFilter);
        return !realWWParts.isEmpty();
    }

    /**
     * Bestimmt alle Teilenummern der Extra-Wahlweise-Teile zu einem Stücklisteneintrag. {@link iPartsConst#FIELD_K_WW_EXTRA_PARTS)
     * Es werden Doppeleinträge verhindert.
     *
     * @param entry
     * @return
     */
    public static Set<String> getExtraWWPartNumbers(iPartsDataPartListEntry entry) {
        Set<String> result = new TreeSet<String>();
        String wwExtraPartNumbers = entry.getFieldValue(iPartsConst.FIELD_K_WW_EXTRA_PARTS);
        if (wwExtraPartNumbers.isEmpty()) {
            return result;
        }

        List<String> wwExtraPartNumbersList = StrUtils.toStringList(wwExtraPartNumbers, ",", false, true);
        result.addAll(wwExtraPartNumbersList);
        return result;
    }

    /**
     * Bestimmt alle Extra-Wahlweise-Teile zu einem Stücklisteneintrag. {@link iPartsConst#FIELD_K_WW_EXTRA_PARTS)
     * Es werden Doppeleinträge verhindert. Im Gegensatz zu {@link iPartsWWPartsHelper#getExtraWWPartNumbers }
     * werden hier die echten Stücklisteneintrage bestimmt.
     * Falls die Teilenummer nicht in der Stückliste enthalten ist, wird für die Ergbenisliste ein neuer Eintrag angelegt.
     *
     * @param entry
     * @param withFilter Bei {@code true} werden nur Wahlweise-Teile berücksichtigt, die auch nach der Filterung noch in
     *                   der Stückliste enthalten sind
     * @return
     */
    public static Collection<EtkDataPartListEntry> getExtraWWParts(iPartsDataPartListEntry entry, boolean withFilter) {
        Collection<EtkDataPartListEntry> result = new DwList<>();
        Set<String> wwExtraPartNumbersList = getExtraWWPartNumbers(entry);
        if (wwExtraPartNumbersList.isEmpty()) { // Abkürzung bei nicht vorhandenen Extra-Wahlweise-Teilen
            return result;
        }

        List<EtkDataPartListEntry> partList;
        if (withFilter) {
            partList = entry.getOwnerAssembly().getPartList(null);
        } else {
            DBDataObjectList<EtkDataPartListEntry> partListUnfiltered = entry.getOwnerAssembly().getPartListUnfiltered(null);
            partList = partListUnfiltered.getAsList();
        }

        DBDataObjectList<EtkDataPartListEntry> partListUnfiltered = null;
        for (String wwExtraPartNumber : wwExtraPartNumbersList) {
            // Zunächst alle möglichen Stücklisteneinträge mit passender Wahlweise-Teilenummer bestimmen
            List<EtkDataPartListEntry> possibleWWPartListEntries = new DwList<>();
            for (EtkDataPartListEntry wwPartListEntry : partList) {
                if (wwPartListEntry.getFieldValue(iPartsConst.FIELD_K_MATNR).equalsIgnoreCase(wwExtraPartNumber)) {
                    possibleWWPartListEntries.add(wwPartListEntry);
                }
            }

            if (possibleWWPartListEntries.isEmpty()) { // Teil befindet sich nicht in der Stückliste -> Dummy-Stücklisteneintrag hinzufügen
                boolean addDummyEntry = true;

                // Bei Filterung überprüfen, ob der Stücklisteneintrag in der ungefilterten Stückliste enthalten wäre
                if (withFilter) {
                    if (partListUnfiltered == null) {
                        partListUnfiltered = entry.getOwnerAssembly().getPartListUnfiltered(null);
                    }
                    for (EtkDataPartListEntry wwPartListEntry : partListUnfiltered) {
                        if (wwPartListEntry.getFieldValue(iPartsConst.FIELD_K_MATNR).equalsIgnoreCase(wwExtraPartNumber)) {
                            // Stücklisteneintrag ist in der ungefilterten Stückliste enthalten -> keinen Dummy-Eintrag
                            // erzeugen, da ausgefiltert
                            addDummyEntry = false;
                            break;
                        }
                    }
                }

                if (addDummyEntry) {
                    PartListEntryId dummyWWPartListEntryId = new PartListEntryId(entry.getAsId());
                    dummyWWPartListEntryId.setKLfdNr(K_LFDNR_NOT_IN_MODULE);

                    EtkDataPartListEntry dummyWWPartListEntry = EtkDataObjectFactory.createDataPartListEntry(entry.getEtkProject(),
                                                                                                             dummyWWPartListEntryId);
                    dummyWWPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    dummyWWPartListEntry.setFieldValue(iPartsConst.FIELD_K_MATNR, wwExtraPartNumber, DBActionOrigin.FROM_DB);

                    result.add(dummyWWPartListEntry);
                }
            } else if (possibleWWPartListEntries.size() == 1) { // Teil befindet sich genau einmal in der Stückliste
                result.add(possibleWWPartListEntries.get(0));
            } else { // Teil befindet sich mehrmals in der Stückliste -> besten Stücklisteneintrag über Scoring suchen
                String matNr = entry.getFieldValue(iPartsConst.FIELD_K_MATNR).toUpperCase(); // 1000 (enthalten in FIELD_K_WW_EXTRA_PARTS)
                // Scoring durchführen
                int maxScore = -1;
                EtkDataPartListEntry bestPartListEntry = null;
                for (EtkDataPartListEntry possibleWWPartListEntry : possibleWWPartListEntries) {
                    int score = iPartsScoringHelper.calculatePartListEntryEqualityScore(entry, possibleWWPartListEntry);
                    if (possibleWWPartListEntry.getFieldValue(iPartsConst.FIELD_K_WW_EXTRA_PARTS).toUpperCase().contains(matNr)) {
                        score += 1000;
                    }
                    if (score > maxScore) {
                        maxScore = score;
                        bestPartListEntry = possibleWWPartListEntry;
                    }
                }

                // Besten Stücklisteneintrag zur Liste hinzufügen
                result.add(bestPartListEntry);
            }
        }
        return result;
    }

    /**
     * Hat der Stücklisteneintrag Extra-Wahlweise-Teile? {@link iPartsConst#FIELD_K_WW_EXTRA_PARTS)
     *
     * @param entry
     * @param withFilter Bei {@code true} werden nur Wahlweise-Teile berücksichtigt, die auch nach der Filterung noch in
     *                   der Stückliste enthalten sind
     * @return
     */
    public static boolean hasExtraWWParts(iPartsDataPartListEntry entry, boolean withFilter) {
        Set<String> wwExtraPartNumbersList = getExtraWWPartNumbers(entry);
        if (!wwExtraPartNumbersList.isEmpty()) {
            Collection<EtkDataPartListEntry> extraWWParts = getExtraWWParts(entry, withFilter);
            return !extraWWParts.isEmpty();
        } else {
            return false;
        }
    }

    /**
     * Hat der Stücklisteneintrag echte oder Extra-Wahlweise-Teile?
     *
     * @param entry
     * @param withFilter Bei {@code true} werden nur Wahlweise-Teile berücksichtigt, die auch nach der Filterung noch in
     *                   der Stückliste enthalten sind
     * @return
     */
    public static boolean hasWWParts(iPartsDataPartListEntry entry, boolean withFilter) {
        return (hasRealWWParts(entry, withFilter) || hasExtraWWParts(entry, withFilter));
    }

    /**
     * Bestimmt alle Wahlweise-Teile (echte und Extra-Wahlweise) zu einem Stücklisten Eintrag.
     *
     * @param entry
     * @param withFilter Bei {@code true} werden nur Wahlweise-Teile berücksichtigt, die auch nach der Filterung noch in
     *                   der Stückliste enthalten sind
     * @return
     */
    public static Collection<EtkDataPartListEntry> getWWParts(iPartsDataPartListEntry entry, boolean withFilter) {
        Collection<EtkDataPartListEntry> result = getRealWWParts(entry, withFilter);
        Collection<EtkDataPartListEntry> extraWWParts = getExtraWWParts(entry, withFilter);
        result.addAll(extraWWParts);
        return result;
    }

    /**
     * Bestimmt alle Wahlweise-Teile (echte und Extra-Wahlweise) zu einem Stücklisten Eintrag für den XML Export. Der Export
     * hat seinen eigenen Filter, der hier als Parameter übergeben wird.
     *
     * @param entry
     * @param filter
     * @return
     */
    public static Collection<EtkDataPartListEntry> getWWPartsForExport(iPartsDataPartListEntry entry, iPartsFilter filter) {
        Collection<EtkDataPartListEntry> result = getRealWWParts(entry, filter);
        // false, damit der eigentliche iPartsFilter nicht anspringt
        Collection<EtkDataPartListEntry> extraWWParts = getExtraWWParts(entry, false);
        // Durchlaufen und schauen, ob die Wahlweiseteile eventuell durch den Export-Filter herausgefiltert werden
        for (EtkDataPartListEntry possiblePartListEntry : extraWWParts) {
            String klfdNummer = possiblePartListEntry.getAsId().getKLfdnr();
            if (klfdNummer.equals(K_LFDNR_NOT_IN_MODULE) || (filter == null) || filter.checkFilter(possiblePartListEntry)) {
                result.add(possiblePartListEntry);
            }
        }
        return result;
    }

    /**
     * Ermittelt die nächste freie Wahlweise GUID (Nummer).
     * Dabei wird einfach die maximale verwendete Nummer ermittelt und 1 addiert. Vorhandene Lücken werden
     * nicht gefüllt. Im Fehlerfall wird {@code 99} zurückgeliefert
     *
     * @param wwGUIDs
     * @return
     */
    public static String getNextUnusedWWGUID(Collection<String> wwGUIDs) {
        if (wwGUIDs.isEmpty()) {
            return "1";
        }

        List<String> usedWWGUIDsList = new DwList<>(wwGUIDs);
        SortUtils.sortList(usedWWGUIDsList, false, true, true);
        String lastUsedWWGUID = usedWWGUIDsList.get(usedWWGUIDsList.size() - 1);
        if (lastUsedWWGUID.isEmpty()) {
            return "1";
        }
        try {
            return Integer.toString(Integer.valueOf(lastUsedWWGUID) + 1);
        } catch (NumberFormatException e) {
            return "99";
        }
    }
}
