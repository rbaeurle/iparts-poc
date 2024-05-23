/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiTextArea;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.java1_1.Java1_1_Utils;

import java.util.List;

/**
 * Einfache Klasse, um die Eingabe bei {@link GuiTextField} und {@link GuiTextArea} zu begrenzen.
 */
public class LengthLimiter {

    private int defaultLengthLimit = 240;

    // Längenlimit
    private int lengthLimit = defaultLengthLimit;

    // Die Instanz die die onChangeEvents Aktionen abarbeitet
    protected EventListeners eventOnChangeListeners;
    // das eigentliche Control
    private AbstractGuiControl guiControl;

    public LengthLimiter(AbstractGuiControl guiControl) {
        this.eventOnChangeListeners = new EventListeners();
        this.guiControl = guiControl;
    }

    public int getLengthLimit() {
        return lengthLimit;
    }

    public void setLengthLimit(int lengthLimit) {
        this.lengthLimit = lengthLimit;
    }

    /**
     * Fügt einen Eventlistener hinzu
     *
     * @param eventListener den hinzuzufügenden Eventlistener
     */
    public boolean addEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(Event.ON_CHANGE_EVENT)) {
            eventOnChangeListeners.addEventListener(eventListener);
            return true;
        }
        return false;
    }

    /**
     * Entferne den übergebenen Eventlistener
     */
    public boolean removeEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(Event.ON_CHANGE_EVENT)) {
            eventOnChangeListeners.removeEventListener(eventListener);
            return true;
        }
        return false;
    }

    public void onTextChanged(Event event) {
        if (lengthLimit > 0) {
            if (guiControl instanceof GuiTextArea) {
                onTextChangeTextArea((GuiTextArea)guiControl);
            } else {
                onTextChangeTextField((GuiTextField)guiControl);
            }
        } else {
            fireChangeEvents();
        }
    }

    private void onTextChangeTextField(final GuiTextField textField) {
        String actText = textField.getText();

        int actLength = actText.length();
        if (actLength >= lengthLimit) {
            textField.switchOffEventListeners();
            final String newText = actText.substring(0, lengthLimit);
            Session.startChildThreadInSession(thread -> {
                Java1_1_Utils.sleep(1);
                textField.setText(newText);
                textField.setCursorPosition(newText.length());
                textField.switchOnEventListeners();
                fireChangeEvents();
            });
        } else {
            fireChangeEvents();
        }
    }

    private void onTextChangeTextArea(final GuiTextArea textArea) {
        String actText = textArea.getText();

        int actLength = actText.length();
        if (actLength >= lengthLimit) {
            textArea.switchOffEventListeners();
            final String newText = actText.substring(0, lengthLimit);
            Session.startChildThreadInSession(thread -> {
                Java1_1_Utils.sleep(1);
                textArea.setText(newText);
                textArea.setCursorPosition(newText.length());
                textArea.switchOnEventListeners();
                fireChangeEvents();
            });
        } else {
            fireChangeEvents();
        }
    }

    private void fireChangeEvents() {

        // Eigenen Kind-Thread starten, weil der aufrufende (Warte-)Thread ansonsten zwischendrin abgebrochen werden könnte,
        // was z.B. bei IO-Aktionen Exceptions verursacht
        Session.startChildThreadInSession(thread -> {
            if (eventOnChangeListeners.isActive()) {
                final List<EventListener> listeners = eventOnChangeListeners.getListeners(Event.ON_CHANGE_EVENT);
                if (listeners.size() > 0) {
                    Session.invokeThreadSafeInSession(() -> {
                        for (EventListener listener : listeners) {
                            listener.fire(new Event(Event.ON_CHANGE_EVENT));
                        }
                    });
                }
            }
        });
    }
}
