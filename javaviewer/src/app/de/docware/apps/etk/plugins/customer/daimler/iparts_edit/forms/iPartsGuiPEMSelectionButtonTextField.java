/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPem;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.PEMDataHelper;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.util.StrUtils;

import java.util.Map;

/**
 * Control zum Auswählen von PEMs
 */
public class iPartsGuiPEMSelectionButtonTextField extends GuiButtonTextField {

    public static final String TYPE = "ipartsPEMSelectionButtonTextField";

    protected EtkProject project;
    private String currentFactory;
    private EventListener listener;
    private EventListener backgroundColorListener;
    private Map<String, iPartsDataPem> pemsForFactory; // Alle möglichen PEMs zum aktuellen Werk

    public iPartsGuiPEMSelectionButtonTextField(EtkProject project, String initialValue) {
        super();
        this.project = project;
        setType(TYPE);
        setBackgroundColorCheck(true);
        if (StrUtils.isValid(initialValue)) {
            setText(initialValue);
        }
    }

    /**
     * Initialisiert das Control mit dem übergebenen Werk
     *
     * @param activeForm
     * @param factory
     * @param pemsForFactory
     * @param pemDataOrigin
     */
    public void init(final AbstractJavaViewerForm activeForm, final String factory, Map<String, iPartsDataPem> pemsForFactory,
                     PEMDataHelper.PEMDataOrigin pemDataOrigin) {
        setCurrentFactory(factory, pemsForFactory, pemDataOrigin);
        if (listener == null) {
            // Listener für die Anlage und Recherche von PEMs
            listener = new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    String currentPem = getText();
                    PEMDataHelper.PEMDataOrigin currentPEMOrigin = pemDataOrigin;
                    if (StrUtils.isValid(currentPem)) {
                        iPartsDataPem currentDataPem = pemsForFactory.get(currentPem);
                        if ((currentDataPem != null) && (pemDataOrigin != null)) {
                            currentPEMOrigin = new PEMDataHelper.PEMDataOrigin(currentDataPem.getFieldValue(iPartsConst.FIELD_DPM_PRODUCT_NO));
                        }
                    }
                    String pem = MasterDataPEMForm.showMasterDataForEdit(activeForm, currentFactory, currentPem, currentPEMOrigin);
                    if (pem == null) {
                        return;
                    }
                    // Bei Änderungen an einer PEM (z.B. Code) würde der Text nicht noch einmal gesetzt, da die PEM
                    // sich ja nicht verändert hat (nur die Daten an der PEM). Da diese Werte aber über einen OnChange
                    // Eventlistener gesetzt werden, muss hier der Textinhalt erst geleert werden, bevor die PEM
                    // gesetzt wird.
                    if (pem.equals(getText())) {
                        switchOffEventListeners();
                        setText("");
                        switchOnEventListeners();
                    }
                    setText(pem);
                }
            };
            super.addEventListener(listener);
        }
    }

    /**
     * Gibt zurück, ob der aktuelle Text im Textfield für das übergebene Werk auf ein valides {@link iPartsDataPem} Objekt zeigt.
     *
     * @param factory
     * @return
     */
    public boolean isPemInputValidForFactory(String factory) {
        if (factory.equals(currentFactory) && StrUtils.isValid(getText()) && (pemsForFactory != null)) {
            return pemsForFactory.get(getText()) != null;
        }
        return true;
    }

    /**
     * Validierung der eingegebenen PEM erzwingen.
     */
    public void doValidation() {
        if (currentFactory != null) { // damit keine Validierung vor der Initialisierung stattfindet
            if (!isPemInputValidForFactory(currentFactory)) {
                setTextfieldBackgroundColor(Colors.clDesignErrorBackground);
            } else {
                setTextfieldBackgroundColor(Colors.clDesignTextFieldEnabledBackground);
            }
        }
    }

    /**
     * Setzt das aktuelle Werk und die dazugehörigen PEMs. Werden keine PEMs übergeben, wird versucht die PEMS aus der
     * DB zu laden.
     *
     * @param factory
     * @param pemsForFactory
     * @param pemDataOrigin
     */
    private void setCurrentFactory(String factory, Map<String, iPartsDataPem> pemsForFactory, PEMDataHelper.PEMDataOrigin pemDataOrigin) {
        this.currentFactory = factory;
        if (pemsForFactory == null) {
            this.pemsForFactory = PEMDataHelper.getPEMsForPEMOrigin(project, factory, pemDataOrigin);
        } else {
            this.pemsForFactory = pemsForFactory;
        }
    }

    public String getCurrentFactory() {
        return currentFactory;
    }

    private void setBackgroundColorCheck(boolean enabled) {
        if (enabled) {
            enableBackgroundColorCheck();
        } else {
            disableBackgroundColorCheck();
        }
    }

    /**
     * Aktiviert die Prüfung, ob der aktuelle Dateninhalt zu einer geladenen PEM passt.
     */
    private void enableBackgroundColorCheck() {
        if (backgroundColorListener == null) {
            // Listener für die Hintergrundfarbe
            backgroundColorListener = new EventListener(Event.ON_CHANGE_EVENT) {
                @Override
                public void fire(Event event) {
                    doValidation();
                }
            };
            addEventListener(backgroundColorListener);
        }
    }

    /**
     * Deaktiviert die Prüfung, ob der aktuelle Dateninhalt zu einer geladenen PEM passt.
     */
    private void disableBackgroundColorCheck() {
        if (backgroundColorListener != null) {
            removeEventListener(backgroundColorListener);
            backgroundColorListener = null;
        }
    }

    /**
     * Aktiviert bzw. Deaktiviert das Anlegen, Recherchieren und Löschen von PEMs via Stammdatendialog
     *
     * @param useMasterData
     */
    public void setUsePEMMasterData(boolean useMasterData) {
        setBackgroundColorCheck(useMasterData);
        setButtonVisible(useMasterData);
    }
}
