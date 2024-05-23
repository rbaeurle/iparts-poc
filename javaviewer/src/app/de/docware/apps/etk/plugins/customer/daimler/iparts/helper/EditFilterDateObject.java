/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.framework.modules.gui.controls.calendar.GuiCalendar;
import de.docware.framework.modules.gui.controls.formattedfields.GuiDateTimeEditPanel;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;


public class EditFilterDateObject {

    private String dateFrom;
    private String dateTo;

    public EditFilterDateObject() {
    }

    public EditFilterDateObject(GuiCalendar guiCalendarFrom, GuiCalendar guiCalendarTo) {
        String rawString = guiCalendarFrom.getDateAsRawString();
        if (StrUtils.isValid(rawString)) {
            rawString += EditFilterDateGuiHelper.DEFAULT_TIME_VALUE;
        }
        this.dateFrom = rawString;
        rawString = guiCalendarTo.getDateAsRawString();
        if (StrUtils.isValid(rawString)) {
            rawString += EditFilterDateGuiHelper.DEFAULT_TIME_VALUE;
        }
        this.dateTo = rawString;
    }

    public EditFilterDateObject(GuiDateTimeEditPanel dateTimePanelFrom, GuiDateTimeEditPanel dateTimePanelTo) {
        dateFrom = checkDateTimeValue(dateTimePanelFrom);
        dateTo = checkDateTimeValue(dateTimePanelTo);
    }

    /**
     * Überprüft, ob das übergebene Datum inkl. Uhrzeit gültig ist. Falls ja, wird das eingetragene Datum zurückgeliefert
     *
     * @param guiDateTimePanel
     * @return
     */
    private String checkDateTimeValue(GuiDateTimeEditPanel guiDateTimePanel) {
        String rawString = guiDateTimePanel.getDateTimeAsRawString();
        String timeValue = guiDateTimePanel.getTimeEditTextField().getDateTimeAsRawString();
        if (StrUtils.isValid(rawString)) {
            Date dateFromCalendar = guiDateTimePanel.getCalendar().getDate();
            if ((dateFromCalendar == null) && timeValue.equals(EditFilterDateGuiHelper.DEFAULT_TIME_VALUE)) {
                rawString = "";
            }
        }
        // 00:00:00 setzen, falls die Zeit leer ist
        if (timeValue.isEmpty()) {
            EditFilterDateGuiHelper.setDefaultTime(guiDateTimePanel);
        }
        return rawString;
    }

    public boolean datesAreValid() {
        return StrUtils.isValid(getDateFrom(), getDateTo());
    }

    public boolean isDateFromLessOrEqualDateTo() {
        if (datesAreValid()) {
            return getDateFrom().compareTo(getDateTo()) <= 0;
        }
        return false;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public String getDateFromForDbSearch() {
        return dateFrom;
    }

    public String getDateToForDbSearch() {
        // Der Tag muss um 1 erhöht werden damit die Daten <= Datum-Bis mitkommen
        String dateToDB = getDateTo();
        if (DateUtils.isValidDateTime_yyyyMMddHHmmss(dateToDB)) {
            try {
                Calendar calendar = DateUtils.toCalendar_yyyyMMddHHmmss(dateToDB);
                calendar.add(Calendar.DATE, 1);
                dateToDB = DateUtils.toyyyyMMddHHmmss_Calendar(calendar);
            } catch (DateException | ParseException e) {
                // Wird aufgrund der prüfenden Bedingung nicht auftreten
                Logger.getLogger().throwRuntimeException(e);
            }
        }
        return dateToDB;
    }

}
