/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.forms.common.EditControlDateTimeEditPanel;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.framework.modules.gui.app.DWLayoutManager;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.calendar.GuiCalendar;
import de.docware.framework.modules.gui.controls.formattedfields.AbstractGuiDateTimeEditTextField;
import de.docware.framework.modules.gui.controls.formattedfields.GuiDateTimeEditPanel;
import de.docware.framework.modules.gui.controls.formattedfields.GuiTimeEditTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;

import java.util.Calendar;

/**
 * Helfer um ein Panel mit Datum von/bis anzuzeigen. Die Uhrzeit ist dabei optional. Die Klasse {@link EditFilterDateObject}
 * wird als Datencontainer für die Zugriffe auf die Datumswerte verwendet. Entweder via SessionKey (Speicherung in Session)
 * oder via Callback
 */
public class EditFilterDateGuiHelper {

    public static final String DEFAULT_TIME_VALUE = "000000";
    private GuiPanel filterDatePanel;
    private GuiPanel filterDateFromPanel;
    private GuiPanel filterDateToPanel;
    private String labelFromText;
    private String labelToText;
    private GuiCalendar guiCalendarFrom;
    private GuiCalendar guiCalendarTo;
    private EditControlDateTimeEditPanel guiDateTimeCalendarFrom;
    private EditControlDateTimeEditPanel guiDateTimeCalendarTo;
    private final boolean verticalArrangement; // Flag um die GuiElemente neben- oder untereinander zu platzieren

    public EditFilterDateGuiHelper(boolean verticalArrangement) {
        this.verticalArrangement = verticalArrangement;
        init();
    }

    public EditFilterDateGuiHelper() {
        this(false);
    }

    public void setLabelFromText(String value) {
        this.labelFromText = value;
    }

    public void setLabelToText(String value) {
        this.labelToText = value;
    }

    public GuiCalendar getCalendarFrom() {
        return guiCalendarFrom;
    }

    public GuiCalendar getCalendarTo() {
        return guiCalendarTo;
    }

    public EditControlDateTimeEditPanel getDateTimeCalendarFrom() {
        return guiDateTimeCalendarFrom;
    }

    public EditControlDateTimeEditPanel getDateTimeCalendarTo() {
        return guiDateTimeCalendarTo;
    }

    /**
     * Erzeugt ein {@link GuiPanel} mit Datum-Elementen (ohne Uhrzeit) für die in der Session gespeicherten Werte zum
     * übergebenen <code>sessionKey</code>
     *
     * @param sessionKey
     * @param onChangeEventFrom
     * @param onChangeEventTo
     * @return
     */
    public GuiPanel createFilterDatePanelWithSessionValues(String sessionKey, OnChangeEvent onChangeEventFrom,
                                                           OnChangeEvent onChangeEventTo, boolean setCurrentMonth) {
        // Session-Werte holen
        EditFilterDateObject filterDateObject = getDateFilterObjectFromSession(sessionKey);
        if (filterDateObject == null) {
            filterDateObject = new EditFilterDateObject();
            if (setCurrentMonth) {
                Calendar firstOfMonth = Calendar.getInstance();
                firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
                firstOfMonth.set(Calendar.HOUR_OF_DAY, 0);
                firstOfMonth.set(Calendar.MINUTE, 0);
                firstOfMonth.set(Calendar.SECOND, 0);
                firstOfMonth.set(Calendar.MILLISECOND, 0);
                filterDateObject.setDateFrom(DateUtils.toyyyyMMddHHmmss_Calendar(firstOfMonth));
                filterDateObject.setDateTo(DateUtils.toyyyyMMddHHmmss_Calendar(Calendar.getInstance()));
            }
        }
        // Labels und Controls anlegen und befüllen
        createLabelsAndDateControls(filterDateObject.getDateFrom(), filterDateObject.getDateTo());
        // EventListener setzen
        setDateEventsForSessionValues(sessionKey, guiCalendarFrom, guiCalendarTo, onChangeEventFrom, onChangeEventTo);
        return filterDatePanel;
    }

    /**
     * Erzeugt ein {@link GuiPanel} mit Datum-Elementen (ohne Uhrzeit) für die übergebenen Datums-Werte
     *
     * @param onChangeEvent
     * @param dateFrom
     * @param dateTo
     * @return
     */
    public GuiPanel createFilterDatePanel(OnFilterDateChangeEvent onChangeEvent, Calendar dateFrom, Calendar dateTo) {
        // Initialwerte bestimmen
        String fromDate = (dateFrom == null) ? null : DateUtils.toyyyyMMddHHmmss_Calendar(dateFrom);
        String toDate = (dateTo == null) ? null : DateUtils.toyyyyMMddHHmmss_Calendar(dateTo);
        // Labels und Controls anlegen und befüllen
        createLabelsAndDateControls(fromDate, toDate);
        // EventListener setzen
        setDateEvents(guiCalendarFrom, guiCalendarTo, onChangeEvent);
        return filterDatePanel;
    }

    /**
     * Erzeugt die Labels und die Datum-Elemente (ohne Uhrzeit)
     *
     * @param dateFrom
     * @param dateTo
     */
    private void createLabelsAndDateControls(String dateFrom, String dateTo) {
        createLabels();
        // Datum-Ab (ohne Uhrzeit) Controller
        guiCalendarFrom = createFilterFromToCalendar(dateFrom, "guiCalendarFrom");
        // Datum-Bis (ohne Uhrzeit) Control
        guiCalendarTo = createFilterFromToCalendar(dateTo, "guiCalendarTo");
        // Controls hinzufügen
        addControlsToPanels(guiCalendarFrom, guiCalendarTo);
    }

    /**
     * Erzeugt ein {@link GuiPanel} mit Datum-Elementen (inkl. Uhrzeit) für die in der Session gespeicherten Werte zum
     * übergebenen <code>sessionKey</code>
     *
     * @param sessionKey
     * @param onChangeEventFrom
     * @param onChangeEventTo
     * @return
     */
    public GuiPanel createFilterDateTimePanelWithSessionValues(String sessionKey, OnChangeEvent onChangeEventFrom,
                                                               OnChangeEvent onChangeEventTo) {
        // Session-Werte holen
        EditFilterDateObject filterDateObject = getDateFilterObjectFromSession(sessionKey);
        if (filterDateObject == null) {
            filterDateObject = new EditFilterDateObject();
        }
        // Labels und Controls anlegen und befüllen
        createLabelsAndDateTimeControls(filterDateObject.getDateFrom(), filterDateObject.getDateTo());
        // EventListener setzen
        setDateTimeEventsForSessionValues(sessionKey, guiDateTimeCalendarFrom, guiDateTimeCalendarTo, onChangeEventFrom,
                                          onChangeEventTo);
        return filterDatePanel;
    }

    /**
     * Erzeugt ein {@link GuiPanel} mit Datum-Elementen (inkl. Uhrzeit) für die übergebenen Datums-Werte. Sollten die
     * übergebenen Datum-Werte leer oder null sein, wird für die Uhrzeit "00:00:00" als Initialwert gesetzt
     *
     * @param onChangeEvent
     * @param dateTimeFrom
     * @param dateTimeTo
     * @return
     */
    public GuiPanel createFilterDateTimePanel(OnFilterDateChangeEvent onChangeEvent, Calendar dateTimeFrom,
                                              Calendar dateTimeTo) {
        // Initialwerte bestimmen
        String fromDateTime = (dateTimeFrom == null) ? null : DateUtils.toyyyyMMddHHmmss_Calendar(dateTimeFrom);
        String toDateTime = (dateTimeFrom == null) ? null : DateUtils.toyyyyMMddHHmmss_Calendar(dateTimeTo);
        // Labels und Controls anlegen und befüllen
        createLabelsAndDateTimeControls(fromDateTime, toDateTime);
        // EventListener setzen
        setDateTimeEvents(guiDateTimeCalendarFrom, guiDateTimeCalendarTo, onChangeEvent);
        return filterDatePanel;
    }

    /**
     * Erzeugt die Labels und die Datum-Elemente (inkl. Uhrzeit)
     *
     * @param fromDateTime
     * @param toDateTime
     */
    private void createLabelsAndDateTimeControls(String fromDateTime, String toDateTime) {
        createLabels();
        // Datum-Ab (inkl. Uhrzeit) Controller
        guiDateTimeCalendarFrom = createFilterFromToDateTimeCalendar(fromDateTime, "guiCalendarDateTimeFrom");
        // Datum-Bis (inkl. Uhrzeit) Control
        guiDateTimeCalendarTo = createFilterFromToDateTimeCalendar(toDateTime, "guiCalendarDateTimeTo");
        addControlsToPanels(guiDateTimeCalendarFrom, guiDateTimeCalendarTo);
    }

    /**
     * Setzt den default Wert für die Uhrzeit: "00:00:00"
     *
     * @param guiDateTimeCalendar
     */
    public static void setDefaultTime(GuiDateTimeEditPanel guiDateTimeCalendar) {
        GuiTimeEditTextField fromDate = guiDateTimeCalendar.getTimeEditTextField();
        fromDate.switchOffEventListeners();
        fromDate.setDateTime(DEFAULT_TIME_VALUE);
        fromDate.switchOnEventListeners();
    }

    /**
     * Initialisisert die Hautp-Panels auf denen die Datum-Elemente liegen
     */
    private void init() {
        filterDatePanel = createFilterMainPanel();
        // Unterscheidung horizontal <-> vertikal
        if (verticalArrangement) {
            filterDateToPanel = null;
            filterDateFromPanel = null;
        } else {
            // Auf diesem Panel liegt das Datum-Ab Label und der Datum-Ab Controller
            filterDateFromPanel = createFilterFromToPanel(true, "filterDateFromPanel");
            // Auf diesem Panel liegt das Datum-Bis Label und das Datum-Bis Control
            filterDateToPanel = createFilterFromToPanel(false, "filterDateToPanel");
            filterDatePanel.addChildGridBag(filterDateFromPanel, 0, 0, 1, 1, 0.0,
                                            0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                            0, 0, 0, 4);
            filterDatePanel.addChildGridBag(filterDateToPanel, 1, 0, 1, 1, 0.0,
                                            0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                            0, 0, 0, 0);
        }
        setLabelFromText("!!Datum ab");
        setLabelToText("!!Datum bis");
    }


    /**
     * Legt die Labels an und fügt sie hinzu
     */
    private void createLabels() {
        if (filterDateFromPanel == null) {
            if (!filterDatePanel.getChildren().isEmpty()) {
                filterDatePanel.removeAllChildren();
            }
            // Datum-Ab Label
            GuiLabel calenderFromLabel = createFilterFromToLabel(labelFromText, "calenderFromLabel");
            calenderFromLabel.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
            filterDatePanel.addChildGridBag(calenderFromLabel, 0, 0, 1, 1, 0.0,
                                            0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                            0, 0, 4, 0);
            // Datum-Bis Label
            GuiLabel calenderToLabel = createFilterFromToLabel(labelToText, "calenderToLabel");
            calenderToLabel.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
            filterDatePanel.addChildGridBag(calenderToLabel, 0, 1, 1, 1, 0.0,
                                            0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                            0, 0, 0, 0);
        } else {
            if (!filterDateFromPanel.getChildren().isEmpty()) {
                filterDateFromPanel.removeAllChildren();
            }
            // Datum-Ab Label
            GuiLabel calenderFromLabel = createFilterFromToLabel(labelFromText, "calenderFromLabel");
            filterDateFromPanel.addChildGridBag(calenderFromLabel, 0, 0, 1, 1, 100.0,
                                                0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                                0, 0, 0, 4);
            // Datum-Bis Label
            if (!filterDateToPanel.getChildren().isEmpty()) {
                filterDateToPanel.removeAllChildren();
            }
            GuiLabel calenderToLabel = createFilterFromToLabel(labelToText, "calenderToLabel");
            filterDateToPanel.addChildGridBag(calenderToLabel, 0, 0, 1, 1, 100.0,
                                              0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                              0, 0, 0, 4);
        }
    }

    /**
     * Fügt die Datum-Controls hinzu
     *
     * @param guiFrom
     * @param guiTo
     */
    private void addControlsToPanels(GuiPanel guiFrom, GuiPanel guiTo) {
        if (filterDateFromPanel == null) {
            filterDatePanel.addChildGridBag(guiFrom, 1, 0, 1, 1, 100.0,
                                            0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                                            0, 4, 4, 0);

            filterDatePanel.addChildGridBag(guiTo, 1, 1, 1, 1, 100.0,
                                            0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL,
                                            0, 4, 0, 0);
        } else {
            filterDateFromPanel.addChildGridBag(guiFrom, 1, 0, 1, 1, 0.0,
                                                0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_HORIZONTAL,
                                                0, 4, 0, 0);

            filterDateToPanel.addChildGridBag(guiTo, 1, 0, 1, 1, 0.0,
                                              0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_HORIZONTAL,
                                              0, 4, 0, 0);
        }
    }

    private GuiPanel createFilterMainPanel() {
        // Auf diesem Panel liegt das Panel für das Datum-Ab Control und für das Datum-Bis Control
        GuiPanel panelForFilterDate = new GuiPanel();
        panelForFilterDate.setName("filterDatePanel");
        panelForFilterDate.__internal_setGenerationDpi(96);
        panelForFilterDate.setScaleForResolution(true);
        panelForFilterDate.setMinimumWidth(10);
        panelForFilterDate.setMinimumHeight(10);
        LayoutGridBag filterPanelLayout = new LayoutGridBag();
        panelForFilterDate.setLayout(filterPanelLayout);

        panelForFilterDate.setPaddingTop(4);
        panelForFilterDate.setPaddingBottom(4);
        return panelForFilterDate;
    }

    private GuiPanel createFilterFromToPanel(boolean isFrom, String name) {
        // Auf diesem Panel liegt das Datum-Ab Label und der Datum-Ab Controller
        GuiPanel filterDateFromToPanel = new GuiPanel();
        filterDateFromToPanel.setName(name);
        filterDateFromToPanel.setLayout(new LayoutGridBag());

        ConstraintsBorder filterDateConstraints;
        if (isFrom) {
            filterDateConstraints = new ConstraintsBorder(ConstraintsBorder.POSITION_WEST);
        } else {
            filterDateConstraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        }
        filterDateFromToPanel.setConstraints(filterDateConstraints);
        return filterDateFromToPanel;
    }

    private GuiLabel createFilterFromToLabel(String text, String name) {
        GuiLabel calenderFromToLabel = new GuiLabel();
        calenderFromToLabel.setName(name);
        calenderFromToLabel.__internal_setGenerationDpi(96);
//        calenderFromToLabel.registerTranslationHandler(getUITranslationHandler());
        calenderFromToLabel.setScaleForResolution(true);
        calenderFromToLabel.setMinimumWidth(10);
        calenderFromToLabel.setMinimumHeight(10);
        calenderFromToLabel.setText(text);
        calenderFromToLabel.setPaddingTop(DWLayoutManager.get().isResponsiveMode() ? 11 : 4);
        calenderFromToLabel.setPaddingRight(4);
        return calenderFromToLabel;
    }

    private EditControlDateTimeEditPanel createFilterFromToDateTimeCalendar(String dateFromTo, String name) {
        EditControlDateTimeEditPanel guiCalendarFromTo = new EditControlDateTimeEditPanel();
        guiCalendarFromTo.setName(name);
        guiCalendarFromTo.registerTranslationHandler(null);
        guiCalendarFromTo.setPaddingRight(8);
        guiCalendarFromTo.clearDateTime();
        guiCalendarFromTo.setShowSeconds(true);
        if (StrUtils.isValid(dateFromTo)) {
            guiCalendarFromTo.setDateTime(dateFromTo);
        } else {
            setDefaultTime(guiCalendarFromTo);
        }
        return guiCalendarFromTo;
    }

    private GuiCalendar createFilterFromToCalendar(String dateFromTo, String name) {
        GuiCalendar guiCalendarFromTo = new GuiCalendar();
        guiCalendarFromTo.setName(name);
        guiCalendarFromTo.registerTranslationHandler(null);
        guiCalendarFromTo.setPaddingRight(8);
        if (StrUtils.isValid(dateFromTo)) {
            guiCalendarFromTo.setDate(dateFromTo);
        }
        return guiCalendarFromTo;
    }

    /**
     * Setzt die {@link EventListener} für die übergebenen Datum-Controls aus der Session (ohne Uhrzeit)
     *
     * @param sessionKey
     * @param guiCalendarFrom
     * @param guiCalendarTo
     * @param onChangeEventFrom
     * @param onChangeEventTo
     */
    private void setDateEventsForSessionValues(String sessionKey, GuiCalendar guiCalendarFrom, GuiCalendar guiCalendarTo,
                                               OnChangeEvent onChangeEventFrom, OnChangeEvent onChangeEventTo) {
        addOnChangeListenersForSessionValues(sessionKey, guiCalendarFrom, guiCalendarTo,
                                             onChangeEventFrom, onChangeEventTo);
    }

    /**
     * Setzt die {@link EventListener} für die übergebenen Datum-Controls aus der Session (inkl. Uhrzeit)
     *
     * @param sessionKey
     * @param dateTimeEditPanelFrom
     * @param dateTimeEditPanelTo
     * @param onChangeEventFrom
     * @param onChangeEventTo
     */
    private void setDateTimeEventsForSessionValues(String sessionKey, GuiDateTimeEditPanel dateTimeEditPanelFrom,
                                                   GuiDateTimeEditPanel dateTimeEditPanelTo, OnChangeEvent onChangeEventFrom,
                                                   OnChangeEvent onChangeEventTo) {
        addOnChangeListenersForSessionValues(sessionKey, dateTimeEditPanelFrom, dateTimeEditPanelTo,
                                             onChangeEventFrom, onChangeEventTo);
    }

    /**
     * Setzt die {@link EventListener} für die übergebenen Datum-Controls aus der Session
     *
     * @param sessionKey
     * @param fromPanel
     * @param toPanel
     * @param onChangeEventFrom
     * @param onChangeEventTo
     */
    private void addOnChangeListenersForSessionValues(String sessionKey, GuiPanel fromPanel, GuiPanel toPanel,
                                                      OnChangeEvent onChangeEventFrom, OnChangeEvent onChangeEventTo) {
        fromPanel.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                // Die aktuellen Werte bestimmen
                EditFilterDateObject filterDateObject = getCurrentFilterDateObject(fromPanel, toPanel);
                handleToolTipByOnChangeEvent(filterDateObject, fromPanel, toPanel);
                setDateFilterSessionObject(sessionKey, filterDateObject);
                if (onChangeEventFrom != null) {
                    onChangeEventFrom.onChange();
                }
            }
        });
        toPanel.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                // Die aktuellen Werte bestimmen
                EditFilterDateObject filterDateObject = getCurrentFilterDateObject(fromPanel, toPanel);
                handleToolTipByOnChangeEvent(filterDateObject, fromPanel, toPanel);
                setDateFilterSessionObject(sessionKey, filterDateObject);
                if (onChangeEventTo != null) {
                    onChangeEventTo.onChange();
                }
            }
        });
        // Die aktuellen Werte bestimmen
        EditFilterDateObject filterDateObject = getCurrentFilterDateObject(fromPanel, toPanel);
        setDateFilterSessionObject(sessionKey, filterDateObject);
    }

    /**
     * Setzt die {@link EventListener} für die übergebenen Datum-Controls (ohne Uhrzeit)
     *
     * @param guiCalendarFrom
     * @param guiCalendarTo
     * @param onChangeEvent
     */
    private void setDateEvents(GuiCalendar guiCalendarFrom, GuiCalendar guiCalendarTo,
                               OnFilterDateChangeEvent onChangeEvent) {
        if (onChangeEvent != null) {
            addOnChangeListener(guiCalendarFrom, guiCalendarTo, onChangeEvent);
        }
    }

    /**
     * Setzt die {@link EventListener} für die übergebenen Datum-Controls (inkl. Uhrzeit)
     *
     * @param dateTimeEditPanelFrom
     * @param dateTimeEditPanelTo
     * @param onChangeEvent
     */
    private void setDateTimeEvents(GuiDateTimeEditPanel dateTimeEditPanelFrom, GuiDateTimeEditPanel dateTimeEditPanelTo,
                                   OnFilterDateChangeEvent onChangeEvent) {
        if (onChangeEvent != null) {
            addOnChangeListener(dateTimeEditPanelFrom, dateTimeEditPanelTo, onChangeEvent);
        }
    }

    /**
     * Setzt die {@link EventListener} für die übergebenen Datum-Controls
     *
     * @param fromPanel
     * @param toPanel
     * @param onChangeEvent
     */
    private void addOnChangeListener(GuiPanel fromPanel, GuiPanel toPanel, OnFilterDateChangeEvent onChangeEvent) {
        EventListener eventListener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                EditFilterDateObject filterDateObject = getCurrentFilterDateObject(fromPanel, toPanel);
                handleToolTipByOnChangeEvent(filterDateObject, fromPanel, toPanel);
                onChangeEvent.onChangeFromDate(filterDateObject);
            }
        };
        fromPanel.addEventListener(eventListener);
        toPanel.addEventListener(eventListener);
    }

    private void handleToolTipByOnChangeEvent(EditFilterDateObject filterDateObject, GuiPanel fromPanel, GuiPanel toPanel) {
        AbstractGuiDateTimeEditTextField dateTimeEditTextField;
        if ((fromPanel instanceof GuiCalendar) && (toPanel instanceof GuiCalendar)) {
            dateTimeEditTextField = guiCalendarTo.getDateEditTextField();
        } else if ((fromPanel instanceof GuiDateTimeEditPanel) && (toPanel instanceof GuiDateTimeEditPanel)) {
            dateTimeEditTextField = guiDateTimeCalendarTo.getCalendar().getDateEditTextField();
        } else {
            return;
        }
        AbstractGuiControl tooltip = dateTimeEditTextField.getTooltip();
        if (tooltip != null) {
            String tooltipText = "";
            if (tooltip instanceof GuiLabel) {
                tooltipText = tooltip.getText();
            }
            if (filterDateObject.datesAreValid() && !filterDateObject.isDateFromLessOrEqualDateTo()) {
                if (StrUtils.isValid(tooltipText) && !tooltipText.contains("\n")) {
                    tooltipText = tooltipText + "\n" +
                                  TranslationHandler.translate("!!\"%1\" muss größer/gleich \"%2\" sein!",
                                                               TranslationHandler.translate(labelToText),
                                                               TranslationHandler.translate(labelFromText));
                } else {
                    tooltipText = "";
                }
            } else {
                if (StrUtils.isValid(tooltipText) && tooltipText.contains("\n")) {
                    tooltipText = StrUtils.copySubString(tooltipText, 0, tooltipText.indexOf("\n"));
                } else {
                    tooltipText = "";
                }
            }
            if (StrUtils.isValid(tooltipText)) {
                dateTimeEditTextField.setTooltip(tooltipText);
            }
        }
    }

    /**
     * Liefert den {@link EditFilterDateObject} Daten-Container mit den aktuellen Werten für die übergeben Panels
     *
     * @param fromPanel
     * @param toPanel
     * @return
     */
    private EditFilterDateObject getCurrentFilterDateObject(GuiPanel fromPanel, GuiPanel toPanel) {
        EditFilterDateObject filterDateObject = null;
        if ((fromPanel instanceof GuiCalendar) && (toPanel instanceof GuiCalendar)) {
            filterDateObject = new EditFilterDateObject(guiCalendarFrom, guiCalendarTo);
        } else if ((fromPanel instanceof GuiDateTimeEditPanel) && (toPanel instanceof GuiDateTimeEditPanel)) {
            filterDateObject = new EditFilterDateObject(guiDateTimeCalendarFrom, guiDateTimeCalendarTo);
        }
        return filterDateObject;
    }

    /**
     * Setzt alle Werte zurück
     */
    public void resetControls() {
        clearCalendar(guiCalendarFrom);
        clearCalendar(guiCalendarTo);
        clearDateTimeCalendar(guiDateTimeCalendarFrom);
        clearDateTimeCalendar(guiDateTimeCalendarTo);
    }

    /**
     * Setzt alle Datum-Controls mit Uhrzeit zurück
     *
     * @param dateTimePanel
     */
    private void clearDateTimeCalendar(EditControlDateTimeEditPanel dateTimePanel) {
        if (dateTimePanel != null) {
            dateTimePanel.clearDateTime();
            setDefaultTime(dateTimePanel);
        }
    }

    /**
     * Setzt alle Datum-Controls ohne Uhrzeit zurück
     *
     * @param guiCalendar
     */
    private void clearCalendar(GuiCalendar guiCalendar) {
        if (guiCalendar != null) {
            guiCalendar.clearDate();
        }
    }

    /**
     * Datums-Panel für die vergangenen Tage in aktuellen Monat erzeugen mit direktem Callback {@link OnFilterDateChangeEvent}
     *
     * @return
     */
    public static GuiPanel createFilterDatePanelForCurrentMonth(OnFilterDateChangeEvent onChangeEvent) {
        EditFilterDateGuiHelper helper = new EditFilterDateGuiHelper();
        Calendar firstOfMonth = Calendar.getInstance();
        firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        firstOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        firstOfMonth.set(Calendar.MINUTE, 0);
        firstOfMonth.set(Calendar.SECOND, 0);
        firstOfMonth.set(Calendar.MILLISECOND, 0);
        return helper.createFilterDatePanel(onChangeEvent, firstOfMonth, Calendar.getInstance());
    }

    /**
     * Datums-Panel (ohne Uhrzeit) für die vergangenen Tage in aktuellen Monat erzeugen mit SessionKey und Callback {@link OnChangeEvent}
     *
     * @param sessionKey
     * @param onChangeEventFrom
     * @param onChangeEventTo
     * @return
     */
    public static GuiPanel createDefaultFilterDatePanelWithSessionValuesForCurrentMonth(String sessionKey, OnChangeEvent onChangeEventFrom,
                                                                                        OnChangeEvent onChangeEventTo) {
        EditFilterDateGuiHelper helper = new EditFilterDateGuiHelper();
        return helper.createFilterDatePanelWithSessionValues(sessionKey, onChangeEventFrom, onChangeEventTo, true);
    }

    /**
     * Datums-Panel (ohne Uhrzeit) erzeugen mit SessionKey und Callback {@link OnChangeEvent}
     *
     * @param sessionKey
     * @param onChangeEventFrom
     * @param onChangeEventTo
     * @return
     */
    public static GuiPanel createDefaultFilterDatePanelWithSessionValues(String sessionKey, OnChangeEvent onChangeEventFrom,
                                                                         OnChangeEvent onChangeEventTo) {
        EditFilterDateGuiHelper helper = new EditFilterDateGuiHelper();
        return helper.createFilterDatePanelWithSessionValues(sessionKey, onChangeEventFrom, onChangeEventTo, false);
    }

    /**
     * Datums-Panel (inkl. Uhrzeit) erzeugen mit SessionKey und Callback {@link OnChangeEvent}
     *
     * @param sessionKey
     * @param onChangeEventFrom
     * @param onChangeEventTo
     * @return
     */
    public static GuiPanel createDefaultFilterDateTimePanelWithSessionValues(String sessionKey, OnChangeEvent onChangeEventFrom, OnChangeEvent onChangeEventTo) {
        EditFilterDateGuiHelper helper = new EditFilterDateGuiHelper();
        return helper.createFilterDateTimePanelWithSessionValues(sessionKey, onChangeEventFrom, onChangeEventTo);
    }

    /**
     * Liefert das aktuelle {@link EditFilterDateObject} Datenobjekt zum übergebenen <code>sessionKey</code> zurück
     *
     * @param sessionKey
     * @return
     */
    public static EditFilterDateObject getDateFilterObjectFromSession(String sessionKey) {
        Session session = Session.get();
        if ((session != null) && session.hasAttribute(sessionKey)) {
            return (EditFilterDateObject)session.getAttribute(sessionKey);
        }
        return null;
    }

    /**
     * Speichert das übergebene {@link EditFilterDateObject} Datenobjekt in der Session zum übergebenen <code>sessionKey</code>
     *
     * @param sessionKey
     * @param filterDateObject
     * @return
     */
    public static boolean setDateFilterSessionObject(String sessionKey, EditFilterDateObject filterDateObject) {
        Session session = Session.get();
        if (session != null) {
            session.setAttribute(sessionKey, filterDateObject);
            return true;
        }
        return false;
    }
}
