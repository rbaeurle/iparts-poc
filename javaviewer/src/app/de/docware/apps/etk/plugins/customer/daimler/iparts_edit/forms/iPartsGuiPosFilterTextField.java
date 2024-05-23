package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.framework.modules.gui.controls.AbstractGuiButtonTextField;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.design.DesignImage;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.color.Colors;

import java.awt.event.KeyEvent;

// GuiElement zur Eingabe von Pos in DIALOG-Stückliste + Filter ein-/ausschalten
public class iPartsGuiPosFilterTextField extends AbstractGuiButtonTextField {

    public static final String TYPE = "ipartsposfiltertextfield";

    private boolean isFilterActive;
    private iPartsGuiConstPosTextField posNoTextField;

    public iPartsGuiPosFilterTextField() {
        super();
        setType(TYPE);
        setMinimumWidth(80);
        isFilterActive = false;
        setAltButtonVisible(true);
        setAltButtonText("");
        setAltButtonImage(DesignImage.boolTrueGray.getImage());
        setAltButtonBackground(Colors.clDesignComboBoxEnabledBackground.getColor());
        setAltButtonTooltip("!!Filter einschalten");

        setButtonText("");
        setButtonImage(DesignImage.clearFilter.getImage());
        setButtonBackground(Colors.clDesignComboBoxEnabledBackground.getColor());
        setButtonTooltip("!!Filter ausschalten");

        setSameSizeAsButton();
    }

    public boolean isFilterActive() {
        return isFilterActive;
    }

    public String getFilterText() {
        return getText();
    }

    @Override
    protected AbstractGuiControl createEditControl() {
        posNoTextField = new iPartsGuiConstPosTextField();
        posNoTextField.setMinimumWidth(40);
        posNoTextField.addEventListener(new EventListener(Event.KEY_RELEASED_EVENT) {
            @Override
            public void fire(Event event) {
                int keyCode = event.getIntParameter(Event.EVENT_PARAMETER_KEY_CODE);
                if (keyCode == KeyEvent.VK_ENTER) {
                    doAltButtonClick();
                } else if (keyCode == KeyEvent.VK_ESCAPE) {
                    doButtonClick();
                }
            }
        });

        return posNoTextField;
    }

    @Override
    public void doAltButtonClick() {
        if (!isFilterActive) {
            if (getText().isEmpty()) {
                return;
            }
            isFilterActive = true;
            setAltButtonTooltip("!!Filter übernehmen");
            setAltButtonImage(DesignImage.boolTrue.getImage());
        } else {
            if (getText().isEmpty()) {
                doButtonClick();
                return;
            }
        }
        super.doAltButtonClick();
    }

    @Override
    protected void doButtonClick() {
        if (isFilterActive) {
            setAltButtonTooltip("!!Filter einschalten");
            setAltButtonImage(DesignImage.boolTrueGray.getImage());
        }
        isFilterActive = false;
        super.doButtonClick();
    }
}
