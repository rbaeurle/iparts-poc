/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter.timeslice;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterPartsEntries;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Klasse für die Bestimmung der Zeitscheiben unter Berücksichtigung '0 == unendlich'
 */
public class iPartsFilterTimeSliceHelper {

    /**
     * 2 Datumswerte auf <= vergleichen
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareLessEqualDate(long date1, long date2) {
        return date1 <= date2;
    }

    /**
     * 2 Datumswerte auf < vergleichen (mit Berücksichtigung von unendlich)
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareLessDate(long date1, long date2) {
        if (((date1 == 0) && (date2 == 0)) || ((date1 == Long.MAX_VALUE) && (date2 == Long.MAX_VALUE))) { // unendlich < unendlich
            return true;
        } else {
            return date1 < date2;
        }
    }

    /**
     * 2 Datumswerte auf >= vergleichen
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareGreaterEqualDate(long date1, long date2) {
        return date1 >= date2;
    }

    /**
     * 2 Datumswerte auf > vergleichen (mit Berücksichtigung von unendlich)
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean compareGreaterDate(long date1, long date2) {
        if (((date1 == 0) && (date2 == 0)) || ((date1 == Long.MAX_VALUE) && (date2 == Long.MAX_VALUE))) { // unendlich > unendlich
            return true;
        } else {
            return date1 > date2;
        }
    }

    /**
     * 2 Datumsbereiche überprüfen, ob <i>date1</i> innerhalb (auch teilweise) von <i>date2</i> ist (mit Berücksichtigung
     * von 0 == unendlich bei <i>date1To</i> bzw. <i>date2To</i>)
     * Regel:
     * (date1From <= date2From) und (date1To > date2From) oder
     * (date1From < date2To) und (date1To > date2From)
     *
     * @param date1From
     * @param date1To
     * @param date2From
     * @param date2To
     * @return
     */
    public static boolean isInTimeSlice(long date1From, long date1To, long date2From, long date2To) {
        // Intern muss beim Enddatum mit Long.MAX_VALUE gerechnet werden für unendlich anstatt mit 0
        if (date1To == 0) {
            date1To = Long.MAX_VALUE;
        }
        if (date2To == 0) {
            date2To = Long.MAX_VALUE;
        }

        return (compareLessEqualDate(date1From, date2From) && compareGreaterDate(date1To, date2From))
               || (compareLessDate(date1From, date2To) && compareGreaterDate(date1To, date2From));
    }

    /**
     * 2 Datumsbereiche für den Abgleich mit der Baumuster-Zeitscheibe überprüfen, ob <i>date</i> innerhalb (auch teilweise)
     * von <i>modelDate</i> ist (mit Berücksichtigung von 0 == unendlich bei <i>dateTo</i> bzw. <i>modelDateTo</i>)
     * Regel:
     * (dateFrom <= modelDateFrom) und (dateTo >= modelDateFrom) oder
     * (dateFrom <= modelDateTo) und (dateTo >= modelDateFrom)
     *
     * @param dateFrom
     * @param dateTo
     * @param modelDateFrom
     * @param modelDateTo
     * @return
     */
    public static boolean isInModelTimeSlice(long dateFrom, long dateTo, long modelDateFrom, long modelDateTo) {
        // Intern muss beim Enddatum mit Long.MAX_VALUE gerechnet werden für unendlich anstatt mit 0
        if (dateTo == 0) {
            dateTo = Long.MAX_VALUE;
        }
        if (modelDateTo == 0) {
            modelDateTo = Long.MAX_VALUE;
        }

        return (compareLessEqualDate(dateFrom, modelDateFrom) && compareGreaterEqualDate(dateTo, modelDateFrom))
               || (compareLessEqualDate(dateFrom, modelDateTo) && compareGreaterEqualDate(dateTo, modelDateFrom));
    }

    /**
     * Zerlegt die PV-Liste der übergebenen {@code pvPartListEntries} in TimeSlices mit jeweils den gültigen PartListEntries
     * (sofern diese noch nicht ausgefiltert sind).
     * Das Datum 0 (für unendlich) wird als {@link Long#MAX_VALUE} geliefert.
     *
     * @param pvPartListEntries                  Liste mit den Positionsvarianten zu einer DIALOG-Position
     * @param filterEntries                      {@link iPartsFilterPartsEntries} zur Bestimmung, ob ein Stücklisteneintrag bereits ausgefiltert wurde
     * @param configOptionIgnoreInvalidFactories sollen Werkseinsatzdaten zu ungültigen Werke ignoriert werden?
     * @return
     */
    public static List<iPartsPositionVariantsTimeLine> calcTimeSliceMapFromPVList(List<iPartsDataPartListEntry> pvPartListEntries,
                                                                                  iPartsFilterPartsEntries filterEntries,
                                                                                  boolean configOptionIgnoreInvalidFactories) {
        // zuerst sortieren
        Set<TimeCmpElement> timeCmpList = new TreeSet<>(new Comparator<TimeCmpElement>() {
            @Override
            public int compare(TimeCmpElement o1, TimeCmpElement o2) {
                if (o1.date == o2.date) {
                    if (o1.isStartDate) {
                        if (o2.isStartDate) {
                            return o1.partListEntry.getAsId().compareTo(o2.partListEntry.getAsId());
                        } else {
                            return -1;
                        }
                    } else {
                        if (o2.isStartDate) {
                            return 1;
                        } else {
                            return o1.partListEntry.getAsId().compareTo(o2.partListEntry.getAsId());
                        }
                    }
                }
                if (o1.date < o2.date) {
                    return -1;
                }
                return 1;
            }
        });

        // Nur jeweils die Positionsvarianten zu einzelnen DIALOG-Positionen miteinander vergleichen
        for (iPartsDataPartListEntry pvListEntry : pvPartListEntries) {
            // Seit DAIMLER-6701 nur noch die zu diesem Zeitpunkt noch nicht ausgefilterten Stücklisteneinträge berücksichtigen
            if (filterEntries.isEntryVisible(pvListEntry)) {
                boolean calculateTimeSlices;
                if (configOptionIgnoreInvalidFactories) {
                    // Werkseinsatzdaten Zeitscheiben immer bestimmen auch wenn es nur ungültige Werkseinsatzdaten gibt,
                    // weil Stücklisteneinträge mit nur ungültigen Werkseinsatzdaten im Zeitscheiben Filter gültig sind
                    // für Werkseinsatzdaten für ungültige Werke ergibt sich eine Zeitscheibe von -inf bis +inf
                    calculateTimeSlices = true;
                } else {
                    // Nur dann die Zeitscheiben berechnen wenn es entweder Werkseinsatzdaten zu gültigen Werken gibt
                    // oder gar keine (auch nicht für ungültige Werke)
                    calculateTimeSlices = pvListEntry.hasValidFactoryDataForRetailUnfiltered() || (pvListEntry.getFactoryDataForRetailUnfiltered() == null);
                }

                if (calculateTimeSlices) {
                    timeCmpList.add(new TimeCmpElement(pvListEntry, pvListEntry.getTimeSliceDateFrom(true), true));
                    timeCmpList.add(new TimeCmpElement(pvListEntry, pvListEntry.getTimeSliceDateTo(true), false));
                }
            }
        }

        // nun die Map aufbauen
        Map<Long, List<EtkDataPartListEntry>> timeSliceMap = new LinkedHashMap<>();
        List<EtkDataPartListEntry> currentList = new LinkedList<>();
        for (TimeCmpElement timeElem : timeCmpList) {
            if (timeElem.isStartDate) {
                currentList.add(timeElem.partListEntry);
                List<EtkDataPartListEntry> elems = new LinkedList<>(currentList);
                timeSliceMap.put(timeElem.date, elems);
            } else {
                List<EtkDataPartListEntry> elems = timeSliceMap.get(timeElem.date);
                if (elems != null) {
                    if (timeElem.date != Long.MAX_VALUE) {
                        currentList.remove(timeElem.partListEntry);
                        elems.remove(timeElem.partListEntry);
                    }
                } else {
                    if (timeElem.date != Long.MAX_VALUE) {
                        currentList.remove(timeElem.partListEntry);
                    }
                    elems = new LinkedList<>(currentList);
                    timeSliceMap.put(timeElem.date, elems);
                }
            }
        }

        // Ergebnis zusammenstellen
        List<iPartsPositionVariantsTimeLine> resultList = new DwList<>();
        Map.Entry<Long, List<EtkDataPartListEntry>> lastTimeSliceEntry = null;
        for (Map.Entry<Long, List<EtkDataPartListEntry>> timeSliceEntry : timeSliceMap.entrySet()) {
            if (lastTimeSliceEntry != null) {
                iPartsPositionVariantsTimeLine pvTimeElem = new iPartsPositionVariantsTimeLine(lastTimeSliceEntry.getKey(),
                                                                                               timeSliceEntry.getKey(),
                                                                                               lastTimeSliceEntry.getValue());
                resultList.add(pvTimeElem);
            }
            lastTimeSliceEntry = timeSliceEntry;
        }
        return resultList;
    }

    /**
     * Alle sich überlappenden Zeitscheiben gruppieren. Das Ergebnis ist eine Liste aller so ermittelten Gruppen.
     * Zeitscheiben gelten als überlappt wenn t2.start < t1.ende und t1.start < t2.start
     *
     * @param allTimeSlices
     * @return
     */
    public static List<List<EtkDataPartListEntry>> mergeAnyOverlappingTimeSlices(List<TimeSliceContent> allTimeSlices) {
        List<TimeSliceContent> mergeResult = new DwList<TimeSliceContent>(allTimeSlices);

        // nach Startzeit sortieren, damit potentielle Überlappungen direkt nebeneinander liegen
        Collections.sort(mergeResult, TimeSliceContent.compareStartTime);

        int counter = 0;
        boolean resultChanged = true;
        while (resultChanged && (mergeResult.size() > 1)) {
            resultChanged = false;
            // immer jeweils zwei benachbarte Zeitscheiben vergleichen
            Set<TimeSliceContent> innerResult = new HashSet<TimeSliceContent>();
            for (int i = 0; i < mergeResult.size() - 1; i++) {
                TimeSliceContent sliceA = mergeResult.get(i);
                TimeSliceContent sliceB = mergeResult.get(i + 1);
                // bei Überlappung beide Listen zusammenfügen
                boolean overlap = iPartsFilterTimeSliceHelper.isInTimeSlice(sliceA.startTime, sliceA.endTime, sliceB.startTime, sliceB.endTime);
                if (overlap) {
                    if (sliceA.content.containsAll(sliceB.content)) {
                        innerResult.add(sliceA);
                    } else if (sliceB.content.containsAll(sliceA.content)) {
                        innerResult.add(sliceB);
                    } else {
                        TimeSliceContent merged = new TimeSliceContent(sliceA.startTime, sliceB.endTime);
                        merged.content.addAll(sliceA.content);
                        merged.content.addAll(sliceB.content);
                        innerResult.add(merged);
                        resultChanged = true;
                    }
                } else {
                    innerResult.add(sliceA);
                    innerResult.add(sliceB);
                }
            }
            // wenn beide Listen gleich sind kann abgebrochen werden
            if (equalLists(innerResult, mergeResult)) {
                resultChanged = false;
            }
            mergeResult = iPartsFilterTimeSliceHelper.removeAlreadyIncludedTimeSlices(innerResult);
            Collections.sort(mergeResult, TimeSliceContent.compareStartTime);

            // zur Sicherheit nur maximal so viele Iterationen zulassen wie es ursprünglich Elemente in der Liste gab
            if (counter++ > allTimeSlices.size()) {
                resultChanged = false;
            }
        }

        List<List<EtkDataPartListEntry>> result = new DwList<List<EtkDataPartListEntry>>();
        for (TimeSliceContent sliceContent : mergeResult) {
            DwList<EtkDataPartListEntry> subResult = new DwList<EtkDataPartListEntry>();
            subResult.addAll(sliceContent.content);
            result.add(subResult);
        }

        return result;
    }

    /**
     * Entferne alle Zeitscheiben deren beinhaltete Stücklisteneinträge komplett in anderen Zeitscheiben enthalten sind
     * D.h. es bleiben nur noch die übergeordneten Zeitscheiben übrig
     *
     * @param dataList
     * @return
     */
    public static List<TimeSliceContent> removeAlreadyIncludedTimeSlices(Collection<TimeSliceContent> dataList) {
        List<TimeSliceContent> result = new DwList<TimeSliceContent>(dataList);
        if (result.size() > 1) {
            List<TimeSliceContent> tempDataList = new DwList<TimeSliceContent>(dataList);
            for (int i = 0; i < tempDataList.size(); i++) {
                TimeSliceContent objectToSearchIn = tempDataList.get(i);
                for (int j = 0; j < tempDataList.size(); j++) {
                    if (i != j) {
                        int objectToSearchCounter = 0;
                        TimeSliceContent objectToSearch = tempDataList.get(j);
                        if (objectToSearch.content.size() <= objectToSearchIn.content.size()) {
                            for (EtkDataPartListEntry objectToSearchContentItem : objectToSearch.content) {
                                if (objectToSearchIn.content.contains(objectToSearchContentItem)) {
                                    objectToSearchCounter++;
                                }
                            }
                            if (objectToSearchCounter == objectToSearch.content.size()) {
                                // objectToSearch ist komplett in objectToSearchIn enthalten
                                result.remove(objectToSearch);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static boolean equalLists(Collection<TimeSliceContent> list1, Collection<TimeSliceContent> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        List<TimeSliceContent> copyList1 = new DwList<>(list1);
        List<TimeSliceContent> copyList2 = new DwList<>(list2);

        Collections.sort(copyList1, TimeSliceContent.compareStartTime);
        Collections.sort(copyList2, TimeSliceContent.compareStartTime);

        // Listen sind gleich lange (siehe Anfang)
        for (int i = 0; i < copyList1.size(); i++) {
            TimeSliceContent item1 = copyList1.get(i);
            TimeSliceContent item2 = copyList2.get(i);

            if (item1.startTime != item2.startTime) {
                return false;
            }
            if (item1.endTime != item2.endTime) {
                return false;
            }
            if (!item1.content.containsAll(item2.content)) {
                return false;
            }
            if (!item2.content.containsAll(item1.content)) {
                return false;
            }
        }

        return true;
    }


    public static class TimeSliceContent {

        public long startTime;
        public long endTime;
        public Set<EtkDataPartListEntry> content = new HashSet<>();

        public TimeSliceContent(long start, long end) {
            this.startTime = start;
            this.endTime = end;
        }

        public TimeSliceContent(long start, long end, EtkDataPartListEntry partlistEntry) {
            this.startTime = start;
            this.endTime = end;
            this.content.add(partlistEntry);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass())) {
                return false;
            }
            TimeSliceContent that = (TimeSliceContent)o;
            return (startTime == that.startTime) && (endTime == that.endTime) && (content.equals(that.content));
        }

        @Override
        public int hashCode() {
            return Objects.hash(startTime, endTime, content);
        }

        public static Comparator<TimeSliceContent> compareStartTime = (o1, o2) -> (int)(o1.startTime - o2.startTime);
    }


    private static class TimeCmpElement {

        public EtkDataPartListEntry partListEntry;
        public boolean isStartDate;
        public long date;

        public TimeCmpElement(EtkDataPartListEntry partListEntry, long date, boolean isStartDate) {
            this.partListEntry = partListEntry;
            this.isStartDate = isStartDate;
            if (!isStartDate && (date == 0)) { // 0 bedeutet unendlich -> Long.MAX_VALUE für Enddatum
                date = Long.MAX_VALUE;
            }
            this.date = date;
        }
    }

}