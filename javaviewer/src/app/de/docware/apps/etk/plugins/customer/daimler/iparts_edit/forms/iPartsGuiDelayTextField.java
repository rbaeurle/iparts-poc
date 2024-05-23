/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.java1_1.Java1_1_Utils;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Erweiterung des GuiTextFields um einstellbare Verzögerung beim Senden des onChangeEvents
 */
public class iPartsGuiDelayTextField extends GuiTextField {

    public static final String TYPE = "ipartsdelaytextfield";
    public static final long DEFAULT_DELAYTIME = 500;
    public static final int DEFAULT_MINCHARFORSEARCH = 1;

    // Die Instanz die die onChangeEvents Aktionen abarbeitet
    protected EventListeners eventOnChangeListeners;

    private FrameworkThread thread = null;
    private long delayMilliSec = DEFAULT_DELAYTIME;
    protected int minCharForSearch = DEFAULT_MINCHARFORSEARCH;
    private boolean ignoreMinCharForSearch = false;
    private boolean withoutWildcards = false;
    private boolean allowStarSearch = false;
    private String lastFiredText = "";
    private int minLengthForStarSearch = 0;

    public iPartsGuiDelayTextField() {
        super();
        setType(TYPE);
        this.eventOnChangeListeners = new EventListeners();
        super.addEventListener(new EventListener(Event.KEY_TYPED_EVENT) {
            @Override
            public void fire(Event event) {
                onKeyTyped(event);
            }
        });
        super.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                onTextChanged(event);
            }
        });
    }

    public long getDelayMilliSec() {
        return delayMilliSec;
    }

    /**
     * -1 schaltet den DelayTimer aus
     *
     * @param delayMilliSec
     */
    public void setDelayMilliSec(long delayMilliSec) {
        this.delayMilliSec = delayMilliSec;
    }

    public int getMinCharForSearch() {
        return minCharForSearch;
    }

    public void setMinCharForSearch(int minCharForSearch) {
        this.minCharForSearch = minCharForSearch;
    }

    public boolean isIgnoreMinCharForSearch() {
        return ignoreMinCharForSearch;
    }

    public void setIgnoreMinCharForSearch(boolean ignoreMinCharForSearch) {
        this.ignoreMinCharForSearch = ignoreMinCharForSearch;
    }

    public boolean isWithoutWildcards() {
        return withoutWildcards;
    }

    /**
     * ist #minCharForSearch > 1 und ignoreMinCharForSearch nicht gesetzt,
     * dann werden bei withoutWildCars = true bei der Längenüberprüfung eingegebene Wildcrads nicht berücksichtigt
     * Beispiel: eingegebener Text: *a5 liefert die Länge 2
     *
     * @param withoutWildcards
     */
    public void setWithoutWildcards(boolean withoutWildcards) {
        this.withoutWildcards = withoutWildcards;
    }

    public boolean isAllowStarSearch() {
        return allowStarSearch;
    }

    public void setAllowStarSearch(boolean allowStarSearch, int minLengthForStarSearch) {
        this.allowStarSearch = allowStarSearch;
        setMinLengthForStarSearch(minLengthForStarSearch);
    }

    /**
     * ist #minCharForSearch > 1 und ignoreMinCharForSearch nicht gesetzt,
     * dann ist auch eine Sternsuche am Ende mit mindestens einem Zeichen erlaubt
     *
     * @param allowStarSearch
     */
    public void setAllowStarSearch(boolean allowStarSearch) {
        this.allowStarSearch = allowStarSearch;
        setMinLengthForStarSearch(1);
    }

    public void setMinLengthForStarSearch(int minLengthForStarSearch) {
        this.minLengthForStarSearch = minLengthForStarSearch;
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

    private void onKeyTyped(Event event) {
        if (event.getParameter(Event.EVENT_PARAMETER_KEY_CODE).equals(KeyEvent.VK_ENTER) && !lastFiredText.equals(getText().trim())) {
            if (thread != null) {
                thread.cancel();
            }

            fireChangeEvents();
        }
    }

    /**
     * eigentlicher Callback für Texteingaben
     * diese werden nicht weitergeleitet, sondern nur der DelayTimer gestartet
     *
     * @param event
     */
    private void onTextChanged(Event event) {
        if (!lastFiredText.equals(getText().trim())) {
            restartDelayTimer();
        }
    }

    /**
     * Neustart des DelayTimers und Ausführungsroutine
     */
    protected void restartDelayTimer() {
        if (thread != null) {
            thread.cancel();
        }

        if (delayMilliSec > 0) {
            thread = Session.startChildThreadInSession(thread -> {
                // Wir warten dei DelayTime bis der onChangeEvent ausgelöst wird. Wenn mittlerweile eine
                // neue Eingabe gemacht wurde, wurde der thread gecanceled
                if (!Java1_1_Utils.sleep(delayMilliSec)) {
                    fireChangeEvents();
                }
            });
        } else {
            fireChangeEvents();
        }
    }

    private void fireChangeEvents() {
        lastFiredText = getText().trim();

        // Eigenen Kind-Thread starten, weil der aufrufende (Warte-)Thread ansonsten zwischendrin abgebrochen werden könnte,
        // was z.B. bei IO-Aktionen Exceptions verursacht
        Session.startChildThreadInSession(thread -> {
            boolean doFireEvents;
            if (!ignoreMinCharForSearch) {
                if (withoutWildcards) {
                    doFireEvents = lastFiredText.replace("*", "").replace("?", "").length() >= minCharForSearch;
                } else {
                    doFireEvents = lastFiredText.length() >= minCharForSearch;
                }
                if (allowStarSearch && !doFireEvents) {
                    doFireEvents = (lastFiredText.length() > minLengthForStarSearch) && lastFiredText.endsWith("*");
                }
            } else {
                doFireEvents = true;
            }
            if (doFireEvents) {
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
            }
        });
    }


}
