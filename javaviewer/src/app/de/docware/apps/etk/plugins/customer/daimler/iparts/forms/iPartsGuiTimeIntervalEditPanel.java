/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.formattedfields.GuiTimeEditTextField;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.util.StrUtils;

import java.awt.*;
import java.util.Date;

/**
 * Gui Element für die Eingabe eines täglichen Zeitintervalls
 */
public class iPartsGuiTimeIntervalEditPanel extends GuiPanel {

    public static final String TYPE = "timeIntervalPanel";
    private static final boolean isEditableDefaultValue = true;
    private static boolean showSecondsDefaultValue = false;

    private GuiLabel startLabel;
    private GuiTimeEditTextField startTimeEditTextField;
    private GuiLabel startTimeEndLabel;
    private GuiLabel endLabel;
    private GuiTimeEditTextField endTimeEditTextField;
    private GuiLabel endTimeEndLabel;
    private boolean isEditable = isEditableDefaultValue; // Darf der Benutzer manuell eingeben?
    private String timeLanguage;
    private boolean showSeconds = showSecondsDefaultValue;

    public iPartsGuiTimeIntervalEditPanel() {
        super(); // Default-Initialisierung des "Panels"
        type = TYPE; // Überlagern des tatsächlichen Typs
        __internal_initializeChildComponents();
        __internal_setTestNameOnControl();
        setMinimumHeightMobile(DEFAULT_MINIMUM_SIZE_MOBILE);
    }

    public void alignLeft() {
        alignControls(ConstraintsGridBag.ANCHOR_WEST);
    }

    public void alignRight() {
        alignControls(ConstraintsGridBag.ANCHOR_EAST);
    }

    public void alignCenter() {
        alignControls(ConstraintsGridBag.ANCHOR_CENTER);
    }

    /**
     * Richtet die Darstellung innerhalb des Panels aus (mit Hilfe der Anchor Constraints aus{@link ConstraintsGridBag})
     *
     * @param direction
     */
    private void alignControls(String direction) {
        ConstraintsGridBag constraintsStartLabel = (ConstraintsGridBag)startLabel.getConstraints();
        constraintsStartLabel.setWeightx(0);
        startLabel.setConstraints(constraintsStartLabel);
        ConstraintsGridBag constraintsEndTimeEditField = (ConstraintsGridBag)endTimeEndLabel.getConstraints();
        constraintsEndTimeEditField.setWeightx(0);
        endTimeEndLabel.setConstraints(constraintsEndTimeEditField);
        if (direction.equals(ConstraintsGridBag.ANCHOR_EAST)) {
            constraintsStartLabel.setWeightx(100);
            startLabel.setConstraints(constraintsStartLabel);
        } else if (direction.equals(ConstraintsGridBag.ANCHOR_WEST)) {
            constraintsEndTimeEditField.setWeightx(100);
            endTimeEndLabel.setConstraints(constraintsEndTimeEditField);
        }
    }

    @Override
    public void __internal_setTestNameOnControl() {
        if (!Constants.DEVELOPMENT_QFTEST) {
            return;
        }
        super.__internal_setTestNameOnControl();
        String fullName = __internal_getFullTestNameForControl();
        startTimeEditTextField.__internal_setFullName(fullName + "_startTime");
        endTimeEditTextField.__internal_setFullName(fullName + "_endTime");
    }

    /**
     * Initialisiert alle Elemente innerhalb des Darstellungspanels
     */
    private void __internal_initializeChildComponents() {
        setLayout(new LayoutGridBag());
        // Initialisierung der Eingabekomponente
        startLabel = new GuiLabel("!!Start:");
        startTimeEditTextField = new GuiTimeEditTextField(new Date(0));
        startTimeEndLabel = new GuiLabel("!!Uhr");
        endLabel = new GuiLabel("!!Ende:");
        endTimeEditTextField = new GuiTimeEditTextField(new Date(60 * 60 * 1000)); // Standardmäßig Ende 1h nach Start
        endTimeEndLabel = new GuiLabel("!!Uhr");


        ConstraintsGridBag constraintsGridBag = new ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                                                       0, 0, 0, 4);
        startLabel.setConstraints(constraintsGridBag);

        constraintsGridBag = new ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE,
                                                    0, 0, 0, 0);
        startTimeEditTextField.setConstraints(constraintsGridBag);

        constraintsGridBag = new ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE,
                                                    0, 4, 0, 8);
        startTimeEndLabel.setConstraints(constraintsGridBag);
        GuiSeparator separator = new GuiSeparator(DWOrientation.VERTICAL);
        constraintsGridBag = new ConstraintsGridBag(3, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE,
                                                    0, 0, 0, 0);
        separator.setMinimumWidth(10);
        separator.setConstraints(constraintsGridBag);

        constraintsGridBag = new ConstraintsGridBag(4, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                                    0, 8, 0, 4);
        endLabel.setConstraints(constraintsGridBag);

        constraintsGridBag = new ConstraintsGridBag(5, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE,
                                                    0, 0, 0, 0);
        endTimeEditTextField.setConstraints(constraintsGridBag);

        constraintsGridBag = new ConstraintsGridBag(6, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_NONE,
                                                    0, 4, 0, 0);
        endTimeEndLabel.setConstraints(constraintsGridBag);

        this.addChild(startLabel);
        this.addChild(startTimeEditTextField);
        this.addChild(startTimeEndLabel);
        this.addChild(separator);
        this.addChild(endLabel);
        this.addChild(endTimeEditTextField);
        this.addChild(endTimeEndLabel);

        // OnChange Event des GuiCalendars und GuiTimeEditTextFields an die GuiDateTimePanel Komponente weitergeben, die
        // es dann intern verwerten und dispatchen kann
        // Es ist notwendig, dass ein OnChange-Event vom GuiDateTimePanel geworfen wird (und nicht nur von den Kind-Controls),
        // wenn man manuell das Eingabefeld editiert
        EventListener onChangeEventListener = new EventListener(Event.ON_CHANGE_EVENT) {
            public void fire(Event event) {
                // Die Kind-Komponente aktualisiert ihren eigenen Zustand. Ist sie aktualisiert, holen wir uns den neuen Wert aus dem Kind
                // GuiFileChooserTextfield muss seinen eigenen Zustand nach der Änderung im Kind aktualisieren. Dabei dürfen keine Events ausgelöst werden.
                fireEvent(EventCreator.createOnChangeEvent(iPartsGuiTimeIntervalEditPanel.this.getEventHandlerComponent(), iPartsGuiTimeIntervalEditPanel.this.getUniqueId())); // Registrierte Listener informieren
            }
        };
        startTimeEditTextField.addEventListener(onChangeEventListener);
        endTimeEditTextField.addEventListener(onChangeEventListener);
    }

    @Override
    public void setMinimumHeightMobile(int minimumHeightMobile) {
        super.setMinimumHeightMobile(minimumHeightMobile);
        startLabel.setMinimumHeightMobile(minimumHeightMobile);
        endLabel.setMinimumHeightMobile(minimumHeightMobile);
        startTimeEditTextField.setMinimumHeightMobile(minimumHeightMobile);
        endTimeEditTextField.setMinimumHeightMobile(minimumHeightMobile);
        startTimeEndLabel.setMinimumHeightMobile(minimumHeightMobile);
        endTimeEndLabel.setMinimumHeightMobile(minimumHeightMobile);
    }

    @Override
    public void setFireNearestNeighbour(boolean fireNearestNeighbour) {
        super.setFireNearestNeighbour(fireNearestNeighbour);
        startLabel.setFireNearestNeighbour(fireNearestNeighbour);
        endLabel.setFireNearestNeighbour(fireNearestNeighbour);
        startTimeEditTextField.setFireNearestNeighbour(fireNearestNeighbour);
        endTimeEditTextField.setFireNearestNeighbour(fireNearestNeighbour);
        startTimeEndLabel.setFireNearestNeighbour(fireNearestNeighbour);
        endTimeEndLabel.setFireNearestNeighbour(fireNearestNeighbour);
    }

    /**
     * Löscht das Datum und die Uhrzeit.
     */
    public void clearDateTime() {
        startTimeEditTextField.clearDateTime();
        endTimeEditTextField.clearDateTime();
    }

    public void setShowSeconds(boolean showSeconds) {
        if (this.showSeconds == showSeconds) {
            return;
        }

        this.showSeconds = showSeconds;
        startTimeEditTextField.setShowSeconds(showSeconds);
        endTimeEditTextField.setShowSeconds(showSeconds);
        __internal_setDimensionDirty();
    }

    public GuiTimeEditTextField getStartTimeEditTextField() {
        return startTimeEditTextField;
    }

    public GuiTimeEditTextField getEndTimeEditTextField() {
        return endTimeEditTextField;
    }

    public boolean isEditable() {
        return isEditable;
    }


    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
        startTimeEditTextField.setEditable(isEditable);
        endTimeEditTextField.setEditable(isEditable);
    }

    public boolean isShowSeconds() {
        return showSeconds;
    }

    public void setStartTime(String startTime) {
        setTime(startTimeEditTextField, startTime);
    }

    private void setTime(GuiTimeEditTextField timeEditField, String time) {
        if (StrUtils.isEmpty(time)) {
            timeEditField.clearDateTime();
        } else {
            timeEditField.setDateTime(time);
        }
    }

    public void setEndTime(String endTime) {
        setTime(endTimeEditTextField, endTime);
    }

    /**
     * Control ist zusammengesetzt aus mehreren Subcontrols. Subcontrols dürfen z.B. im GuiDesigner nicht eigens erfasst werden
     *
     * @return
     */
    @Override
    public boolean isCompositeControl() {
        return true;
    }

    @Override
    public void setFontSize(final int size) {
        super.setFontSize(size);
        startTimeEditTextField.setFontSize(size);
        endTimeEditTextField.setFontSize(size);
    }

    @Override
    public void setFontStyle(DWFontStyle style) {
        super.setFontStyle(style);
        startTimeEditTextField.setFontStyle(style);
        endTimeEditTextField.setFontStyle(style);
    }

    @Override
    public void setFontName(String name) {
        super.setFontName(name);
        startTimeEditTextField.setFontName(name);
        endTimeEditTextField.setFontName(name);
    }

    @Override
    public void setForegroundColor(Color foregroundColor) {
        super.setForegroundColor(foregroundColor);
        startTimeEditTextField.setForegroundColor(foregroundColor);
        endTimeEditTextField.setForegroundColor(foregroundColor);
    }

    /**
     * Klont die Attribute sowie Kind-Objekte, die zur Komponente gehören
     *
     * @param control
     */
    protected void cloneProperties(AbstractGuiControl control) {
        if (control.isOfType(TYPE)) {
            iPartsGuiTimeIntervalEditPanel timeIntervalPanel = (iPartsGuiTimeIntervalEditPanel)control;
            timeIntervalPanel.timeLanguage = timeLanguage;
            timeIntervalPanel.showSeconds = showSeconds;

            // Kind-Komponenten müssen ungeachtet des cloneMe() Parameters auf zusammengesetzten Controls immer übernommen werden,
            // ansonsten gehen Darstellungs-Informationen verloren
            timeIntervalPanel.removeChild(timeIntervalPanel.startTimeEditTextField);
            timeIntervalPanel.removeChild(timeIntervalPanel.endTimeEditTextField);
            timeIntervalPanel.removeChild(timeIntervalPanel.startLabel);
            timeIntervalPanel.removeChild(timeIntervalPanel.endLabel);
            timeIntervalPanel.removeChild(timeIntervalPanel.startTimeEndLabel);
            timeIntervalPanel.removeChild(timeIntervalPanel.endTimeEndLabel);

            timeIntervalPanel.startTimeEditTextField = (GuiTimeEditTextField)startTimeEditTextField.cloneMe();
            timeIntervalPanel.endTimeEditTextField = (GuiTimeEditTextField)endTimeEditTextField.cloneMe();
            timeIntervalPanel.startLabel = (GuiLabel)startLabel.cloneMe();
            timeIntervalPanel.endLabel = (GuiLabel)endLabel.cloneMe();
            timeIntervalPanel.startTimeEndLabel = (GuiLabel)startTimeEndLabel.cloneMe();
            timeIntervalPanel.endTimeEndLabel = (GuiLabel)endTimeEndLabel.cloneMe();
            timeIntervalPanel.addChild(timeIntervalPanel.startTimeEditTextField);
            timeIntervalPanel.addChild(timeIntervalPanel.endTimeEditTextField);
            timeIntervalPanel.addChild(timeIntervalPanel.startLabel);
            timeIntervalPanel.addChild(timeIntervalPanel.endLabel);
            timeIntervalPanel.addChild(timeIntervalPanel.startTimeEndLabel);
            timeIntervalPanel.addChild(timeIntervalPanel.endTimeEndLabel);

            copyEventListenerToComponent(startTimeEditTextField, timeIntervalPanel.startTimeEditTextField, Event.ON_CHANGE_EVENT);
            copyEventListenerToComponent(endTimeEditTextField, timeIntervalPanel.endTimeEditTextField, Event.ON_CHANGE_EVENT);
            copyEventListenerToComponent(startLabel, timeIntervalPanel.startLabel, Event.ON_CHANGE_EVENT);
            copyEventListenerToComponent(endLabel, timeIntervalPanel.endLabel, Event.ON_CHANGE_EVENT);
            copyEventListenerToComponent(startTimeEndLabel, timeIntervalPanel.startTimeEndLabel, Event.ON_CHANGE_EVENT);
            copyEventListenerToComponent(endTimeEndLabel, timeIntervalPanel.endTimeEndLabel, Event.ON_CHANGE_EVENT);
        }
    }

    public String getStartTimeAsRawString() {
        return startTimeEditTextField.getDateTimeAsRawString();
    }

    public String getEndTimeAsRawString() {
        return endTimeEditTextField.getDateTimeAsRawString();
    }
}
