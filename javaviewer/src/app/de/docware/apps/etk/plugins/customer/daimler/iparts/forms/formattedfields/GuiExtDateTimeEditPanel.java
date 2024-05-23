package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.formattedfields;

import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.calendar.GuiCalendar;
import de.docware.framework.modules.gui.controls.formattedfields.GuiDateTimeEditPanel;
import de.docware.framework.modules.gui.controls.formattedfields.GuiTimeEditTextField;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.util.StrUtils;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;

import java.awt.*;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

/**
 * Eine Komponente mit {@link GuiExtDateEditTextField} und {@link GuiExtTimeEditTextField}
 * für die Eingabe von Datum und Uhrzeit mit Pattern für Filter
 */
public class GuiExtDateTimeEditPanel extends GuiDateTimeEditPanel {

    public static final String TYPE = "extdateTimePanel";

    protected GuiExtCalendar extCalendar;
    protected GuiExtTimeEditTextField timeExtEditTextField;

    public GuiExtDateTimeEditPanel() {
        super();
        ThemeManager.get().render(extCalendar);
        ThemeManager.get().render(timeExtEditTextField);
    }

    @Override
    public void requestFocus() {
        extCalendar.requestFocus();
    }

    @Override
    protected void __internal_initializeChildComponents() {
        setLayout(new LayoutGridBag());
        // Initialisierung der Eingabekomponente
        calendar = new GuiCalendar();
        calendar.clearDate();
        timeEditTextField = new GuiTimeEditTextField();

        extCalendar = new GuiExtCalendar();
        extCalendar.setEditable(isEditable);
        timeExtEditTextField = new GuiExtTimeEditTextField();

        extCalendar.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                                          0, 0, 0, 0));
        timeExtEditTextField.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE,
                                                                   0, 8, 0, 0));

        this.addChild(extCalendar);
        this.addChild(timeExtEditTextField);

        // Sicherstellen, dass das Uhrzeit-Textfeld immer leer ist und nicht editierbar ist, wenn das Datum leer ist bzw.
        // 00:00:00 enthält, wenn das Datum nicht leer ist und die Uhrzeit vorher leer war
        final EventListener focusLostEventListener = new EventListener(Event.ON_FOCUS_LOST_EVENT) {
            @Override
            public void fire(Event event) {
                boolean isDateEmpty = extCalendar.getEnteredText().isEmpty();
                if (isDateEmpty) {
                    timeExtEditTextField.setDateTime(null, true);
                }
                timeExtEditTextField.setEnabled(!isDateEmpty);
            }
        };
        extCalendar.getDateField().addEventListener(focusLostEventListener);
        timeExtEditTextField.addEventListener(focusLostEventListener);

        // OnChange Event des GuiCalendars und GuiTimeEditTextFields an die GuiDateTimePanel Komponente weitergeben, die
        // es dann intern verwerten und dispatchen kann
        // Es ist notwendig, dass ein OnChange-Event vom GuiDateTimePanel geworfen wird (und nicht nur von den Kind-Controls),
        // wenn man manuell das Eingabefeld editiert
        EventListener onChangeEventListener = new EventListener(Event.ON_CHANGE_EVENT) {
            public void fire(Event event) {
                focusLostEventListener.fire(event);

                // Die Kind-Komponente aktualisiert ihren eigenen Zustand. Ist sie aktualisiert, holen wir uns den neuen Wert aus dem Kind
                // GuiFileChooserTextfield muss seinen eigenen Zustand nach der Änderung im Kind aktualisieren. Dabei dürfen keine Events ausgelöst werden.
                fireEvent(EventCreator.createOnChangeEvent(GuiExtDateTimeEditPanel.this.getEventHandlerComponent(), GuiExtDateTimeEditPanel.this.getUniqueId())); // Registrierte Listener informieren
            }
        };
        extCalendar.addEventListener(onChangeEventListener);
        timeExtEditTextField.addEventListener(onChangeEventListener);
    }

    @Override
    public void setFontSize(final int size) {
        super.setFontSize(size);
        extCalendar.setFontSize(size);
        timeExtEditTextField.setFontSize(size);
    }

    @Override
    public void setFontStyle(DWFontStyle style) {
        super.setFontStyle(style);
        extCalendar.setFontStyle(style);
        timeExtEditTextField.setFontStyle(style);
    }

    @Override
    public void setFontName(String name) {
        super.setFontName(name);
        extCalendar.setFontName(name);
        timeExtEditTextField.setFontName(name);
    }

    @Override
    public void setForegroundColor(Color foregroundColor) {
        super.setForegroundColor(foregroundColor);
        extCalendar.setForegroundColor(foregroundColor);
        timeExtEditTextField.setForegroundColor(foregroundColor);
    }

    @Override
    protected void cloneProperties(AbstractGuiControl control) {
        // TODO
        super.cloneProperties(control);
    }

    @Override
    public void __internal_setTestNameOnControl() {
        if (!Constants.DEVELOPMENT_QFTEST) {
            return;
        }
        super.__internal_setTestNameOnControl();
        String fullName = __internal_getFullTestNameForControl();
        extCalendar.__internal_setFullName(fullName + "_extdate");
        timeExtEditTextField.__internal_setFullName(fullName + "_exttime");
    }

    @Override
    public void setEditable(boolean isEditable) {
        super.setEditable(isEditable);
        extCalendar.setEditable(isEditable);
        timeExtEditTextField.setEditable(isEditable);
    }

    @Override
    public void setDateFieldEditable(boolean isEditable) {
        extCalendar.setDateFieldEditable(isEditable);
    }

    public void setShowSeconds(boolean showSeconds) {
        super.setShowSeconds(showSeconds);

        timeExtEditTextField.setShowSeconds(showSeconds);
        __internal_setDimensionDirty();
    }

    public GuiExtCalendar getCalendar() {
        return extCalendar;
    }

    @Override
    public void setMinimumHeightMobile(int minimumHeightMobile) {
        super.setMinimumHeightMobile(minimumHeightMobile);
        extCalendar.setMinimumHeightMobile(minimumHeightMobile);
        timeExtEditTextField.setMinimumHeightMobile(minimumHeightMobile);
    }

    @Override
    public void setFireNearestNeighbour(boolean fireNearestNeighbour) {
        super.setFireNearestNeighbour(fireNearestNeighbour);
        extCalendar.setFireNearestNeighbour(fireNearestNeighbour);
        timeExtEditTextField.setFireNearestNeighbour(fireNearestNeighbour);
    }

    /**
     * Löscht das Datum und die Uhrzeit.
     */
    public void clearDateTime() {
        extCalendar.clearDate();
        timeExtEditTextField.clearDateTime();
    }

    /**
     * Setzt das Datum und die Uhrzeit auf den angegeben Wert.
     *
     * @param dateTime
     */
    public void setDateTime(Date dateTime) {
        extCalendar.setDate(dateTime);
        timeExtEditTextField.setDateTime(dateTime);
    }

    /**
     * Setzt das Datum und die Uhrzeit auf den angegeben Wert.
     *
     * @param dateTime
     */
    public void setDateTime(Calendar dateTime) {
        extCalendar.setDate(dateTime);
        timeExtEditTextField.setDateTime(dateTime);
    }

    /**
     * Liefert das eingestellte Datum und die eingestellte Uhrzeit zurück.
     *
     * @return
     */
    public Date getDateTime() {
        Date date = extCalendar.getDate();
        Date time = timeExtEditTextField.getDateTime();
        if ((date == null) && (time == null)) {
            return null;
        }

        if (date == null) {
            date = new Date(0);
        }
        String dateTimeString = DateUtils.toISO_Date(date);
        if (time != null) {
            dateTimeString += "T" + DateUtils.toISO_Time(time);
        } else {
            dateTimeString += "T00:00:00"; // new Date(0) liefert 01:00:00 zurück
        }
        try {
            return DateUtils.toDate_ISODateTime(dateTimeString);
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(LogChannels.APPLICATION, LogType.ERROR, e);
            return null;
        }
    }

    /**
     * Liefert das letzte gültige Datum und Uhrzeit im zusammengesetzten Format {@link DateUtils#simpleDateFormatyyyyMMdd}
     * und {@link DateUtils#simpleTimeFormatHHmmss} zurück bzw. nur {@link DateUtils#simpleDateFormatyyyyMMdd} falls das
     * Datum nicht gesetzt ist.
     *
     * @return
     */
    public String getDateTimeAsRawString() {
        String sdate = extCalendar.getDateAsString();
        String stime = timeExtEditTextField.getDateTimeAsString();
        if (StrUtils.isEmpty(sdate, stime)) {
            return "";
        }
        if (!StrUtils.isValid(stime)) {
            stime = timeExtEditTextField.getDateTimeAsRawString();
        }
        if (!StrUtils.isValid(sdate)) {
            sdate = extCalendar.getDateAsRawString();
        }
        String dateTimeString = sdate + " " + stime;
        return dateTimeString;
    }

    @Override
    public String getDateTimeAsFilterString() {
        String sdate = extCalendar.getDateTimeAsFilterString();
        String stime = timeExtEditTextField.getDateTimeAsFilterString();
        String dateTimeString = sdate + stime;
        return dateTimeString;
    }

    /**
     * Setzt das Datum und die Uhrzeit (optional). Bei invalidem Datum wird der String in den Datums- und Uhrzeit-Textfeldern
     * angezeigt, das interne Datumsobjekt wird nicht verändert.
     *
     * @param yyyyMMddHHmmss Kann leer oder invalid sein
     */
    public void setDateTime(String yyyyMMddHHmmss) {
        String date = "";
        String time = "";
        int yyyMMddLength = DateUtils.simpleDateFormatyyyyMMdd.length();
        if (yyyyMMddHHmmss.length() >= yyyMMddLength) {
            date = yyyyMMddHHmmss.substring(0, yyyMMddLength);
            if (date.equals(DateUtils.toyyyyMMdd_Date(new Date(0)))) { // ungültiges Datum -> nur Uhrzeit ist gesetzt
                date = "";
            }
        }
        if (yyyyMMddHHmmss.length() > yyyMMddLength) {
            time = yyyyMMddHHmmss.substring(yyyMMddLength);
        }
        if (DateUtils.isValidDate_yyyyMMdd(date) && DateUtils.isValidTime_HHmmss(time)) {
            try {
                setDateTime(DateUtils.toCalendar_yyyyMMddHHmmss(yyyyMMddHHmmss).getTime());
            } catch (DateException e) {
                // kann nicht auftreten
            } catch (ParseException e) {
                // kann nicht auftreten
            }
        } else {
            extCalendar.setDate(date);
            if (!date.isEmpty() && time.isEmpty()) {
                time = "000000";
            }
            timeExtEditTextField.setDateTime(time);
        }
        // Kein fireEvent für ON_CHANGE notwendig. OnChangeListener werden über calendar bzw. timeEditTextField notifiziert.
    }

    /**
     * Setzt eigene Datums- und Uhrzeit-Pattern (Sprache -> Java Pattern).
     *
     * Die aktuelle Darstellung wird zurückgesetzt und der bisherige Datums- und Uhrzeit-Wert verworfen
     *
     * @param dateCustomPatterns
     * @param timeCustomPatterns
     */
    public void setCustomPatterns(Properties dateCustomPatterns, Properties timeCustomPatterns) {
        extCalendar.setCustomPatterns(dateCustomPatterns);
        timeExtEditTextField.setCustomPatterns(timeCustomPatterns);
    }


    /**
     * Setzt eine explizite Sprache für die Darstellung vom Datum bzw. der Uhrzeit.
     *
     * @param dateTimeLanguage Kann auch {@code null} sein, um die aktuelle Oberflächensprache zu verwenden.
     */
    @Override
    public void setDateTimeLanguage(String dateTimeLanguage) {
        super.setDateTimeLanguage(dateTimeLanguage);

        extCalendar.setDateLanguage(dateTimeLanguage);
        timeExtEditTextField.setDateTimeLanguage(dateTimeLanguage);
    }
}
