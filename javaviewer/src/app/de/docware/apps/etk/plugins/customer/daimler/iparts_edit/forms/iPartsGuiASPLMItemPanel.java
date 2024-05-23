/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsASPLMItemId;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;

/**
 * Einfaches Control zur Darstellung einer {@link iPartsASPLMItemId} (nur readonly)
 */
public class iPartsGuiASPLMItemPanel extends GuiPanel {

    // Weitere benötigte Variablen
    private GuiTextField asplmItemIdTextField;
    private GuiTextField asplmItemRevisionTextField;
    private iPartsASPLMItemId asPlmItemId;

    public iPartsGuiASPLMItemPanel() {
        initGui();
    }

    private void initGui() {
        setMinimumWidth(100);
        setMinimumHeight(20);
        setLayout(new LayoutGridBag(false));
        asplmItemIdTextField = new GuiTextField();
        asplmItemIdTextField.setName("asplmItemIdTextField");
        asplmItemIdTextField.setBackgroundColor(Colors.clDesignBackground.getColor());
        asplmItemIdTextField.setEditable(false);
        ConstraintsGridBag guiConstraints = new ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL, 8, 8, 8, 8);
        asplmItemIdTextField.setConstraints(guiConstraints);
        addChild(asplmItemIdTextField);

        asplmItemRevisionTextField = new GuiTextField();
        asplmItemRevisionTextField.setName("asplmItemRevisionTextField");
        asplmItemRevisionTextField.setBackgroundColor(Colors.clDesignBackground.getColor());
        asplmItemRevisionTextField.setMinimumWidth(50);
        asplmItemRevisionTextField.setEditable(false);
        asplmItemRevisionTextField.setVisible(false);
        guiConstraints = new ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_WEST, ConstraintsGridBag.FILL_HORIZONTAL, 8, 0, 8, 8);
        asplmItemRevisionTextField.setConstraints(guiConstraints);
        addChild(asplmItemRevisionTextField);

    }

    public iPartsASPLMItemId getAsPlmItemId() {
        return asPlmItemId;
    }

    public void setAsPlmItemId(iPartsASPLMItemId asPlmItemId) {
        this.asPlmItemId = asPlmItemId;
        asplmItemRevisionTextField.setVisible((asPlmItemId != null) && !asPlmItemId.getMcItemRevId().isEmpty());
        if (asPlmItemId != null) {
            asplmItemIdTextField.setText(asPlmItemId.getMcItemId());
            asplmItemRevisionTextField.setText(asPlmItemId.getMcItemRevId());
        } else {
            asplmItemIdTextField.setText("");
            asplmItemRevisionTextField.setText("");
        }
    }

    public void setAsPlmItemId(String mcItemId, String mcItemRevId) {
        setAsPlmItemId(new iPartsASPLMItemId(mcItemId, mcItemRevId));
    }
}
