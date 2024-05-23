/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Liste mit {@link iPartsDataSeriesEvent} Objekten
 */
public class iPartsDataSeriesEventList extends EtkDataObjectList<iPartsDataSeriesEvent> implements iPartsConst {

    public iPartsDataSeriesEventList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataSeriesEvent}s, die der übergebenen Baureihe und mit SDatB unendlich zugeordnet
     * sind (optional für alle Sprachen).
     *
     * @param project
     * @param seriesNumber
     * @param allLanguages
     * @return
     */
    public static iPartsDataSeriesEventList loadYoungestEventsForSeries(EtkProject project, String seriesNumber, boolean allLanguages) {
        iPartsDataSeriesEventList list = new iPartsDataSeriesEventList();
        list.loadYoungestEventsForSeries(project, seriesNumber, allLanguages, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt den Ereignis-Graphen für die übergebene Baureihe.
     *
     * @param project
     * @param seriesNumber
     * @return
     */
    public static List<iPartsDataSeriesEvent> loadEventGraphForSeries(EtkProject project, String seriesNumber, boolean withErrorMessage) {
        List<iPartsDataSeriesEvent> allEvents = loadYoungestEventsForSeries(project, seriesNumber, true).getAsList();
        if (allEvents.isEmpty()) {
            return allEvents;
        }

        Map<String, iPartsDataSeriesEvent> prevEventMap = new HashMap<>();
        List<iPartsDataSeriesEvent> firstElems = new DwList<>();
        for (iPartsDataSeriesEvent dataSeriesEvent : allEvents) {
            String previousEventId = dataSeriesEvent.getPreviousEventId();
            if (previousEventId.isEmpty()) {
                firstElems.add(dataSeriesEvent);
            } else if (!previousEventId.equals(dataSeriesEvent.getEventId())) { // Identische EventIds gar nicht erst berücksichtigen (Rekursion!)
                prevEventMap.put(previousEventId, dataSeriesEvent);
            }
        }
        List<iPartsDataSeriesEvent> eventList = new ArrayList<>();
        if (!firstElems.isEmpty()) {
            List<iPartsDataSeriesEvent> graph = new DwList<>();
            Set<String> visitedEventIds = new HashSet<>();
            for (iPartsDataSeriesEvent dataSeriesEvent : firstElems) {
                graph.add(dataSeriesEvent);
                String currentId = dataSeriesEvent.getEventId();
                while (true) {
                    if (visitedEventIds.contains(currentId)) { // Rekursion entdeckt!
                        break;
                    }
                    visitedEventIds.add(currentId);
                    iPartsDataSeriesEvent currentData = prevEventMap.get(currentId);
                    if (currentData != null) {
                        graph.add(currentData);
                        currentId = currentData.getEventId();
                    } else {
                        break;
                    }
                }
                if (graph.size() > 1) {
                    break;
                }
            }
            eventList.addAll(graph);
        }
        if (allEvents.size() != eventList.size()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Resulting event graph for series " +
                                                                      seriesNumber + " has not the same amount of nodes" +
                                                                      " as the input event graph!");
            if (withErrorMessage) {
                MessageDialog.showError(TranslationHandler.translate("!!Datenfehler beim Erzeugen der Ereigniskette.") +
                                        "\n\n" +
                                        TranslationHandler.translate("!!Die erzeugte Ereigniskette hat %1 Ereignisse obwohl " +
                                                                     "%2 Ereignisse geladen wurden!",
                                                                     String.valueOf(eventList.size()), String.valueOf(allEvents.size())));
            }
        }

        return eventList;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataSeriesEvent}s, die der übergebenen Baureihe und mit SDatB unendlich zugeordnet
     * sind (optional für alle Sprachen).
     *
     * @param project
     * @param seriesNumber
     * @param allLanguages
     * @param origin
     * @return
     */
    private void loadYoungestEventsForSeries(EtkProject project, String seriesNumber, boolean allLanguages, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DSE_SERIES_NO, FIELD_DSE_SDATB };
        String[] whereValues = new String[]{ seriesNumber, "" };

        if (allLanguages) {
            searchSortAndFillWithMultiLangValueForAllLanguages(project, null, FIELD_DSE_DESC, whereFields, whereValues,
                                                               false, null, false);

            // searchSortAndFillWithMultiLangValueForAllLanguages markiert nur das explizit geladene mehrsprachige Attribut
            // (Benennung) als mehrsprachig -> die Bemerkung muss manuell als mehrsprachig gekennzeichnet und beim ersten
            // Zugriff aus der DB nachgeladen werden
            for (iPartsDataSeriesEvent dataEvent : list) {
                dataEvent.getAttribute(FIELD_DSE_REMARK).setTextNrForMultiLanguage(dataEvent.getFieldValue(FIELD_DSE_REMARK),
                                                                                   DBActionOrigin.FROM_DB);
            }
        } else {
            searchSortAndFillWithMultiLangValues(project, project.getDBLanguage(), null, whereFields, whereValues, false,
                                                 null, false);
        }
    }


    @Override
    protected iPartsDataSeriesEvent getNewDataObject(EtkProject project) {
        return new iPartsDataSeriesEvent(project, null);
    }
}
