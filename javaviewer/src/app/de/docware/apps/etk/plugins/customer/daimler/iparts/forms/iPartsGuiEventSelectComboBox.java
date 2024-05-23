/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * {@link RComboBox} zur Auswahl von einem {@link iPartsEvent}.
 */
public class iPartsGuiEventSelectComboBox extends RComboBox<String> {

    private iPartsDialogSeries series;
    private EtkProject project;

    /**
     * Initialisiert diese {@link iPartsGuiEventSelectComboBox} mit der übergebenen Baureihe.
     *
     * @param project
     * @param seriesId        Kann auch {@code null} sein -> dann wird nur die optionale {@code selectedEventId} hinzugefügt
     * @param selectedEventId Optionale ausgewählte Ereignis-ID
     */
    public void init(EtkProject project, iPartsSeriesId seriesId, String selectedEventId) {
        this.project = project;
        setMaximumRowCount(20);
        removeAllItems();
        addItem("", "");

        if (seriesId != null) {
            series = iPartsDialogSeries.getInstance(project, seriesId);
            String dbLanguage = project.getDBLanguage();
            List<String> dbFallbackLanguages = project.getDataBaseFallbackLanguages();
            for (iPartsEvent event : series.getEventsMap().values()) {
                addItem(event.getEventId(), event.getEventId() + " - " + event.getTitle().getTextByNearestLanguage(dbLanguage,
                                                                                                                   dbFallbackLanguages));
            }
        } else {
            series = null;

            // Ohne Baureihe zumindest die ausgewählte Ereignis-ID hinzufügen
            if (StrUtils.isValid(selectedEventId)) {
                addItem(selectedEventId, selectedEventId);
            }
        }

        if (StrUtils.isValid(selectedEventId)) {
            setSelectedUserObject(selectedEventId);
        } else {
            setSelectedIndex(-1);
        }
    }

    /**
     * Initialisiert die Combobox speziell für die Ereignis-Felder an Farben. Diese haben ein zusätzliches, künstliches
     * Ereignis mit dem sie die Ereignisse der Konstruktion aushebeln können.
     *
     * @param project
     * @param seriesId
     * @param eventId
     */
    public void initForColorEvents(EtkProject project, iPartsSeriesId seriesId, String eventId) {
        init(project, seriesId, eventId);

        // Spezial-Event "Nicht relevant" direkt nach dem leeren Eintrag an zweiter Stelle hinzufügen
        iPartsEvent notRelevantEvent = iPartsEvent.createNotRelevantEvent(project);
        addItem(notRelevantEvent.getEventId(), notRelevantEvent.getTitle().getTextByNearestLanguage(project.getDBLanguage(),
                                                                                                    project.getDataBaseFallbackLanguages()),
                null, 1, false);

        if (iPartsEvent.isNotRelevantEventId(eventId)) {
            setSelectedUserObject(eventId);
        }
    }

    /**
     * Liefert das ausgewählte {@link iPartsEvent} zurück.
     *
     * @return {@code null} falls kein {@link iPartsEvent} ausgewählt ist oder die ausgewählte Ereignis-ID sich nicht in
     * der Ereigniskette dieser Baureihe befindet
     */
    public iPartsEvent getSelectedEvent() {
        String selectedEventId = getSelectedUserObject();
        // Handelt es sich um das künstliche Ereignis, dann liefer dieses zurück (hängt ja nicht an der Baureihe)
        if (iPartsEvent.isNotRelevantEventId(selectedEventId) && (project != null)) {
            return iPartsEvent.createNotRelevantEvent(project);
        }

        if (series == null) {
            return null;
        }

        if (selectedEventId != null) {
            return series.getEvent(selectedEventId);
        } else {
            return null;
        }
    }

    /**
     * Setzt den ausgewählten {@link iPartsEvent}.
     *
     * @param event
     */
    public void setSelectedEvent(iPartsEvent event) {
        if (event != null) {
            setSelectedEventByEventId(event.getEventId());
        } else {
            setSelectedIndex(-1);
        }
    }

    public void setSelectedEventByEventId(String eventId) {
        if (StrUtils.isValid(eventId)) {
            setSelectedUserObject(eventId);
        } else {
            setSelectedIndex(-1);
        }
    }
}