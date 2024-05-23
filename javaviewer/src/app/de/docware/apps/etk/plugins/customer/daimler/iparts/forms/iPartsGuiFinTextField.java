/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.misc.validator.GuiControlValidator;
import de.docware.framework.modules.gui.misc.validator.ValidationState;

/**
 * {@link GuiButtonTextField} zur Eingabe einer FIN.
 */
public class iPartsGuiFinTextField extends iPartsGuiAlphaNumTextField {

    public static final String TYPE = "ipartsfintextfield";

    // Die Instanz die die onChangeEvents Aktionen abarbeitet
    private boolean isMandatory;

    public iPartsGuiFinTextField() {
        super();
        setWithWhitspaces(false);
        setType(TYPE);
        this.eventOnChangeListeners = new EventListeners();
        this.setValidator(new GuiControlValidator() {
            @Override
            public ValidationState validate(AbstractGuiControl control) {
                boolean isValid = true;
                // ist Überprüfung notwendig?
                if (isMandatory()) {
                    // Eingabe ist Valid, wenn leer oder FIN gültig
                    isValid = getFinId().getFIN().isEmpty() || isFinValid();
                }
                return new ValidationState(isValid);
            }
        });
    }

    public iPartsGuiFinTextField(boolean isMandatory) {
        this();
        this.isMandatory = isMandatory;
    }

    public void setMandatoryInput(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public boolean isFinValid() {
        return getFinId().isValidId();
    }

    public FinId getFinId() {
        return new FinId(getTrimmedText().toUpperCase());
    }

    public void setFinId(FinId finId) {
        setText(finId.getFIN());
        if (!eventListeners.isActive()) {
            // falls die EventListener ausgeschaltet sind: auf jeden Fall die Backgroundcolor nachziehen
            toggleBackColor(getValidationState().isValid());
        }
    }

    public void setFin(String fin) {
        setFinId(new FinId(fin));
    }

    /**
     * Wird überschrieben, damit es von außen nicht mehr gesetzt werden kann
     * Es werden nie Leerzeichen erlaubt
     *
     * @param withWhitspaces
     */
    @Override
    public void setWithWhitspaces(boolean withWhitspaces) {
        super.setWithWhitspaces(false);
    }
}
