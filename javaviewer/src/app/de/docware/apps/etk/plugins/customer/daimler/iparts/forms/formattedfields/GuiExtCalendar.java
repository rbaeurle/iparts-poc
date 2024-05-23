package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.formattedfields;

import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiImage;
import de.docware.framework.modules.gui.controls.calendar.DatePicker;
import de.docware.framework.modules.gui.controls.calendar.GuiCalendar;
import de.docware.framework.modules.gui.controls.formattedfields.GuiDateEditTextField;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.controls.misc.DWPoint;
import de.docware.framework.modules.gui.controls.subcomponents.DWDropDownIcon;
import de.docware.framework.modules.gui.controls.subcomponents.DWDropDownIconListener;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.guiapps.guidesigner.controls.GUIDesignerProperty;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.output.swing.SwingHandler;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;

import java.awt.*;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

/**
 * Eine Kalenderimplementierung (Datumsauswahl)
 *
 * Kalendar ist ein GuiPanel mit Eingabefeld und Button. Der Button öffnet den modalen Dialog für die Datumsauswahl
 * Eingabe erfolgt mit Pattern für Filter
 */
public class GuiExtCalendar extends GuiCalendar {

    public static final String TYPE = "extcalendar";

    protected GuiExtDateEditTextField dateExtField; // Eingabefeld für das Datum. Wenn nicht editierbar ist Auswahl nur über Picker möglich

    public GuiExtCalendar() {
        super();
        type = TYPE; // Überlagern des tatsächlichen Typs
    }

    @Override
    public void requestFocus() {
        dateExtField.requestFocus();
    }

    protected void __internal_initializeChildComponents() {
        setLayout(new LayoutBorder());
        // Initialisierung der Eingabekomponente
        dateField = new GuiDateEditTextField();
        dateExtField = new GuiExtDateEditTextField();
        dateExtField.setEditable(isEditable);
        dateExtField.setAllowsInvalid(false);
        dateExtField.clearDateTime();
        // OnChange Event des GuiTextfields an die GuiCalendar Komponente weitergeben, die es dann intern verwerten und dispatchen kann
        // Es ist notwendig, dass ein OnChange-Event vom GuiCalendar geworfen wird (und nicht nur von den Kind-Controls),
        // wenn man manuell das Eingabefeld editiert oder durch den Datepicker gesetzt wird
        final GuiCalendar _self = this;
        dateExtField.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            public void fire(Event event) {
                // Die Kind-Komponente aktualisiert ihren eigenen Zustand. Ist sie aktualisiert, holen wir uns den neuen Datums-Wert aus dem Kind
                // GuiCalendar muss seinen eigenen Zustand nach der Änderung im Kind aktualisieren. Dabei dürfen keine Events ausgelöst werden.
                fireEvent(EventCreator.createOnChangeEvent(_self.getEventHandlerComponent(), _self.getUniqueId())); // Registrierte Listener informieren
            }
        });
        dateExtField.setConstraints(new ConstraintsBorder(LayoutBorder.POSITION_CENTER));
        addChild(dateExtField);

        DWLayoutManager layoutManager = DWLayoutManager.get();
        final FrameworkImage enabledImage = layoutManager.isLegacyMode() ? DesignImage.comboBoxDropDownEnabled.getImage() : DesignImage.datePicker.getImage();
        FrameworkImage disabledImage = layoutManager.isLegacyMode() ? DesignImage.comboBoxDropDownDisabled.getImage() : DesignImage.datePickerDisabled.getImage();
        datePickerImage = new DWDropDownIcon(enabledImage, disabledImage, new DWDropDownIconListener() {
            /**
             * Öffnet das Datepicker Dialogfenster
             *
             * Das neue Dialog Fenster wird immer linksbündig unterhalb des Eingabefeldes positioniert
             */
            public void doDropDownClick(Event event) {
                if (datePicker == null) {
                    datePicker = new DatePicker(_self, translationHandler);
                }
                __internal_setDatePickerDate();

                int defaultPadding = DWLayoutManager.get().getDefaultPadding();

                // Default Location bestimmen: Zentriert unterhalb des Dropdown-Buttons (dateField).
                int datePickerWidth = datePicker.getWidth();
                if (SwingHandler.isSwing()) {
                    // Event Control Position ist relativ zum GuiWindow angegeben. Die Positionierung des Popups erfolgt
                    // relativ zum Screen. Anstatt die Location des Windows zum Screen zu der Control Position zu addieren
                    // verwenden wir direkt die Swing Methoden für die Positionsbestimmung relativ zum Screen
                    // Positioniert unterhalb des Eingabefeldes zentriert unter dem Dropdown-Button.
                    // Falls anders positioniert werden soll, muss das auch für HTML/Ajax umgesetzt werden!
                    int x = (int)swingControl.getLocationOnScreen().getX() + swingControl.getWidth() + defaultPadding;
                    int y = (int)swingControl.getLocationOnScreen().getY();
                    setDatepickerLocation(x, y);

                }
                if (J2EEHandler.isJ2EE()) {
                    // Direkte Verwendung der Control Position (da relativ zum Window / Viewport)
                    DWPoint pos = (DWPoint)event.getParameter(de.docware.framework.modules.gui.event.Event.EVENT_PARAMETER_CONTROL_POSITION);

                    int x = pos.getX();
                    int y = pos.getY();

                    //Im Desktop/Tablet Horizontal wird der DatePicker rechts neben dem CalendarFeld angezeigt
                    if (DWLayoutManager.get().isDesktopOrTabletHorizontal()) {
                        x += datePickerImage.getPreferredWidth() + defaultPadding;
                        //Das Event kann vom Bild aus selbst gefeuert werden oder dem DropdownIcon.
                        //Falls die Eventquelle das Bild ist, muss für Responsive der y-Wert angepasst werden
                        int yTopPadding = (!DWLayoutManager.get().isLegacyMode() && (event.getSource() instanceof GuiImage)) ? (datePickerImage.getPreferredHeight() - enabledImage.getHeight()) : 0;
                        y -= yTopPadding;
                    } else {
                        //Im Mobilen wird der DatePicker unter dem CalendarFeld angzeigt
                        x -= datePickerWidth - datePickerImage.getPreferredWidth();
                        //Auch im Mobil muss der y-Wert im Responsive nach der Eventquelle angepasst werden
                        int yBottomPadding = (!DWLayoutManager.get().isLegacyMode() && (event.getSource() instanceof DWDropDownIcon)) ? (datePickerImage.getPreferredHeight() - enabledImage.getHeight()) : 0;
                        // 2 * defaultpadding weil er sonst am icon klebt und er soll etwas abstand zum textcontrol haben, so wie im desktop auch etwas abstand mit padding nach links ist
                        y += datePickerImage.getPreferredHeight() + (2 * defaultPadding) + yBottomPadding;
                    }

                    if (borderWidth > 1) {
                        y += 2 * (borderWidth - 1);
                    }

                    //Falls der DatePicker über den Rand des Screens hinauslaufen sollte, wird hier nochmal angepasst
                    Dimension screenSize = FrameworkUtils.getScreenSize();
                    x = Math.min(x, screenSize.width - datePickerWidth);
                    y = Math.min(y, screenSize.height - datePicker.getHeight());
                    setDatepickerLocation(x, y);
                }
                setDatePickerVisible(true);
            }
        }, this.getPreferredHeight());

        datePickerImage.setConstraints(new ConstraintsBorder(LayoutBorder.POSITION_EAST));
        datePickerImage.setCursor(DWCursor.Hand);
        addChild(datePickerImage);
    }

    @Override
    public void __internal_setTestNameOnControl() {
        if (!Constants.DEVELOPMENT_QFTEST) {
            return;
        }
        super.__internal_setTestNameOnControl();
        String fullName = __internal_getFullTestNameForControl();
        dateExtField.__internal_setFullName(fullName + "_exttextfield");
    }

    protected void __internal_setDatePickerDate() {
        if (datePicker != null) {
            Date pickedDate = dateExtField.getDateTime();
            if (pickedDate != null) {
                datePicker.setDate(pickedDate, false);
            } else {
                datePicker.setDate(DateUtils.toDate_currentDate(), false);
            }
        } // else : Datepicker noch nicht angezeigt
    }

    public void languageChanged() {
        super.languageChanged();
        dateField.clearDateTime();
        // Sub Komponenten von der Sprachänderung informieren
        dateExtField.languageChanged(); // Tooltip des Eingabefeldes aktualisieren
    }

    @Override
    public void setForegroundColor(Color foregroundColor) {
        super.setForegroundColor(foregroundColor);
        dateExtField.setForegroundColor(foregroundColor);
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        super.setBackgroundColor(backgroundColor);
        dateExtField.setBackgroundColor(backgroundColor);
    }

    @Override
    public void setFontSize(int size) {
        super.setFontSize(size);
        dateExtField.setFontSize(size);
    }

    public void setCustomPatterns(Properties customPatterns) {
        super.setCustomPatterns(customPatterns);
        dateExtField.setCustomPatterns(customPatterns);
    }

    public void setFormattedDate(String formattedDate) {
        super.setFormattedDate(formattedDate);
        dateExtField.setFormattedDateTime(formattedDate);
    }

    @Override
    public void setDate(Date date) {
        dateExtField.setDateTime(date, false);
    }

    @Override
    public void setDate(Date date, boolean force) {
        dateExtField.setDateTime(date, force);
    }

    @Override
    public void setDate(String yyyyMMdd) {
        if (DateUtils.isValidDate_yyyyMMdd(yyyyMMdd)) {
            try {
                setDate(DateUtils.toSqlDate_yyyyMMdd(yyyyMMdd));
            } catch (DateException e) {
                // kann nicht auftreten
            } catch (ParseException e) {
                // kann nicht auftreten
            }
        } else {
            int yyyyMMddLength = DateUtils.simpleDateFormatyyyyMMdd.length();
            if (yyyyMMdd.length() > yyyyMMddLength) {
                dateExtField.setDateTime(yyyyMMdd.substring(0, yyyyMMddLength));
            } else {
                dateExtField.setDateTime(yyyyMMdd);
            }
        }
        // Kein fireEvent für ON_CHANGE notwendig. OnChangeListener auf GuiCalendar werden durch den
        // onChangeListener auf dem dateField Control informiert, dass sich der Wert geändert hat.
    }

    @Override
    public Date getDate() {
        return dateExtField.getDateTime();
    }

    /**
     * Liefert das letzte gültige Datum im Format {@link DateUtils#simpleDateFormatyyyyMMdd} zurück.
     *
     * @return
     */
    @Override
    public String getDateAsRawString() {
        return dateExtField.getDateTimeAsRawString();
    }

    public String getDateAsString() {
        return dateExtField.getDateTimeAsString();
    }

    @Override
    public String getDateTimeAsFilterString() {
        return dateExtField.getDateTimeAsFilterString();
    }

    /**
     * Löscht den aktuellen Datumswert
     */
    @Override
    public void clearDate() {
        dateExtField.clearDateTime();
    }

    @Override
    public void setTooltip(AbstractGuiControl tooltip) {
        super.setTooltip(tooltip); // Nutzen der vorhandenen Implementierung für Speichern/Lesen des Tooltips
        dateExtField.setTooltip(tooltip);
    }

    @GUIDesignerProperty
    @Override
    public boolean isEditable() {
        return dateExtField.isEditable();
    }

    @Override
    public void setEditable(boolean editable) {
        super.setEditable(editable);
        dateExtField.setEditable(editable);
    }

    @Override
    public void setDateFieldEditable(boolean editable) {
        dateExtField.setEditable(editable);
    }

    /**
     * Liefert das zuletzt als gültig eingegebene Datum zurück
     *
     * @return
     */
    @Override
    public Date getLastValidDate() {
        return dateExtField.getLastValidSqlDateTime();
    }

    /**
     * Gibt den aktuell eingegebenen Text zurück
     *
     * @return
     */
    @Override
    public String getEnteredText() {
        return dateExtField.getEnteredText();
    }

    /**
     * Prüft, ob der aktuell eingegebene Text gültig ist und falls nicht wird er durch das zuletzt gültige Datum ersetzt
     */
    @Override
    public void resetDateStringIfInvalid() {
        String text = getEnteredText();
        if (!text.isEmpty() || !dateExtField.allowsNull()) {
            try {
                DateUtils.toSqlDate_yyyyMMdd(text);
            } catch (Exception e) {
                setDate(getLastValidDate());
            }
        }
    }

    /**
     * Setzt eine explizite Sprache für die Darstellung vom Datum.
     *
     * @param dateLanguage Kann auch {@code null} sein, um die aktuelle Oberflächensprache zu verwenden.
     */
    @Override
    public void setDateLanguage(String dateLanguage) {
        if (Utils.objectEquals(this.dateLanguage, dateLanguage)) {
            return;
        }

        this.dateLanguage = dateLanguage;
        dateExtField.setDateTimeLanguage(dateLanguage);
    }
}
