/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;

import java.awt.*;

/**
 * {@link de.docware.framework.modules.gui.controls.GuiButtonTextField} mit der Möglichkeit die Hintergrundfarbe je nach Valid-Zustand zu setzen
 * dazu nach dem Erzeugen einen neuen {@link de.docware.framework.modules.gui.misc.validator.GuiControlValidator} setzen
 * in der Methode 'validate(AbstractGuiControl control)' einfach einen ValidationState mit Valid liefern.
 * Beispiel:
 * guiCtrl.setValidator(new GuiControlValidator() {
 *
 * public ValidationState validate(AbstractGuiControl control) {
 * // Eingabe ist Valid, wenn leer oder Text-Length == 5 gültig
 * boolean isValid = getTrimmedText().isEmpty() || (getTrimmedText().Length() == 5);
 * return new ValidationState(isValid);
 * }
 * });
 */
public class iPartsGuiTextFieldBackgroundToggle extends GuiTextField {

    public static final String TYPE = "ipartstextfieldbackgroundtoggle";

    // Die Instanz die die onChangeEvents Aktionen abarbeitet
    protected EventListeners eventOnChangeListeners;
    private Color originalBackgroundColor = null;
    private boolean ignoreEditableForCheck = false;
    protected boolean isAlphaNumAllowed = false;

    public iPartsGuiTextFieldBackgroundToggle() {
        this(false);
    }

    public iPartsGuiTextFieldBackgroundToggle(boolean isAplhaNumAllowed) {
        super();
        setType(TYPE);
        this.eventOnChangeListeners = new EventListeners();
        this.isAlphaNumAllowed = isAplhaNumAllowed;
        super.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onTextChanged(event);
            }
        });
    }

    public iPartsGuiTextFieldBackgroundToggle(String text) {
        this(text, false);
    }

    public iPartsGuiTextFieldBackgroundToggle(String text, boolean isAplhaNumAllowed) {
        this(isAplhaNumAllowed);

        // Beim Initialwert keine Events feuern, da es unter Umständen zu Problemen mit den eventOnChangeListeners
        // geben könnte (siehe Multi-Edit)
        if (StrUtils.isValid(text)) {
            switchOffEventListeners();
            setText(controlText(text));
            switchOnEventListeners();
        }
    }

    public boolean isIgnoreEditableForCheck() {
        return ignoreEditableForCheck;
    }

    public void setIgnoreEditableForCheck(boolean ignoreEditableForCheck) {
        this.ignoreEditableForCheck = ignoreEditableForCheck;
    }

    public boolean isEditableForCheck() {
        if (!ignoreEditableForCheck) {
            return isEditable();
        }
        return ignoreEditableForCheck;
    }

    public boolean isValueValid() {
        return getValidationState().isValid();
    }

    protected void onTextChanged(Event event) {
        if (isEditableForCheck()) {
            toggleBackColor(isValueValid());
            fireChangeEvents();
        }
    }

    @Override
    protected String updateTextWithCaseModeConversion(boolean withCutHtmlTagsCheck, String newText) {
        newText = super.updateTextWithCaseModeConversion(withCutHtmlTagsCheck, newText);
        final String correctedText = controlText(newText);
        if (!newText.equals(correctedText)) {
            switchOffEventListeners();
            setText(correctedText);
            setCursorPosition(correctedText.length());
            switchOnEventListeners();
            fireEvent(EventCreator.createOnChangeEvent(this, getUniqueId()));
            return correctedText;
        } else {
            return newText;
        }
    }

    /**
     * Kontrolliert und korrigiert den übergebenen Text.
     *
     * @param text Korrigierter Text
     * @return
     */
    protected String controlText(String text) {
        return text;
    }

    protected void toggleBackColor(boolean isValid) {
        if (!isValid) {
            if (originalBackgroundColor == null) {
                originalBackgroundColor = getBackgroundColor();
                if (Colors.clDefault.getColor().equals(originalBackgroundColor)) {
                    if (isVisuallyEnabled()) {
                        originalBackgroundColor = Colors.clDesignTextFieldEnabledBackground.getColor();
                    } else {
                        originalBackgroundColor = Colors.clDesignTextFieldDisabledBackground.getColor();
                    }
                }
            }
            setBackgroundColor(Colors.clDesignErrorBackground.getColor());
        } else {
            // die Hintergrundfarbe des Textfelds wiederherstellen
            if (originalBackgroundColor != null) {
                setBackgroundColor(originalBackgroundColor);
                originalBackgroundColor = null;
            }
        }
    }

    /**
     * Fügt einen Eventlistener hinzu
     *
     * @param eventListener den hinzuzufügenden Eventlistener
     */
    @Override
    public void addEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(Event.ON_CHANGE_EVENT)) {
            eventOnChangeListeners.addEventListener(eventListener);
        } else {
            super.addEventListener(eventListener);
        }
    }

    /**
     * Entferne den übergebenen Eventlistener
     */
    @Override
    public void removeEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(Event.ON_CHANGE_EVENT)) {
            eventOnChangeListeners.removeEventListener(eventListener);
        } else {
            super.removeEventListener(eventListener);
        }
    }

    @Override
    public void switchOffEventListeners() {
        super.switchOffEventListeners();
        eventOnChangeListeners.setActive(false);
    }

    /**
     * Schaltet (z.B. nach Befüllen einer Combobox) die registrierten EventListener wieder ein.
     * Diese Methode sollte grundsätzlich im Finally eines Try-Blocks gerufen werden, der vor der
     * Abschaltung der Listener beginnt.
     */
    @Override
    public void switchOnEventListeners() {
        super.switchOnEventListeners();
        eventOnChangeListeners.setActive(true);
    }


    protected void fireChangeEvents() {
        final Session session = Session.get();
        if (session == null) {
            return;
        }

        // Eigenen Kind-Thread starten, weil der aufrufende (Warte-)Thread ansonsten zwischendrin abgebrochen werden könnte,
        // was z.B. bei IO-Aktionen Exceptions verursacht
        session.startChildThread(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                if (eventOnChangeListeners.isActive()) {
                    final java.util.List<EventListener> listeners = eventOnChangeListeners.getListeners(Event.ON_CHANGE_EVENT);
                    if (listeners.size() > 0) {
                        session.invokeThreadSafe(new Runnable() {
                            @Override
                            public void run() {
                                for (EventListener listener : listeners) {
                                    listener.fire(new Event(Event.ON_CHANGE_EVENT));
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
