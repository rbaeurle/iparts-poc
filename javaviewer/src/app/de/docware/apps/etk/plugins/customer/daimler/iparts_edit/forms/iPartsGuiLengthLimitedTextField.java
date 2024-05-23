/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.LengthLimiter;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;

/**
 * {@link GuiTextField}-Erweiterung mit Überprüfung der maximalen Länge des Eingabestrings
 */
public class iPartsGuiLengthLimitedTextField extends GuiTextField {

    public static final String TYPE = "ipartslengthlimitedtextfield";

    // Defaultwerte

    // Spezifische Eigenschaften der Komponente
    private LengthLimiter lengthLimiter;

    public iPartsGuiLengthLimitedTextField() {
        super();
        setType(TYPE);
        lengthLimiter = new LengthLimiter(this);
        super.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                lengthLimiter.onTextChanged(event);
            }
        });
    }

    public int getLengthLimit() {
        return lengthLimiter.getLengthLimit();
    }

    public void setLengthLimit(int lengthLimit) {
        lengthLimiter.setLengthLimit(lengthLimit);
    }

    /**
     * Fügt einen Eventlistener hinzu
     *
     * @param eventListener den hinzuzufügenden Eventlistener
     */
    @Override
    public void addEventListener(EventListener eventListener) {
        if (!lengthLimiter.addEventListener(eventListener)) {
            super.addEventListener(eventListener);
        }
    }

    /**
     * Entferne den übergebenen Eventlistener
     */
    @Override
    public void removeEventListener(EventListener eventListener) {
        if (!lengthLimiter.removeEventListener(eventListener)) {
            super.removeEventListener(eventListener);
        }
    }

}
