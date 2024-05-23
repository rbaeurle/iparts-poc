/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor;

import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiHotSpotTextField;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerUtils;

/**
 * Implementierung des Inplace Editors für den Hotspot in der Stückliste
 */
public class iPartsGuiPosInplaceEditor extends iPartsGuiTextFieldInplaceEditor {

    public iPartsGuiPosInplaceEditor() {
        super(new iPartsGuiHotSpotTextField());
    }

    @Override
    protected String modifyNewText(String newText) {
        return GuiViewerUtils.buildPosNumberValue(GuiViewerUtils.splitPosNumber(newText)); // saubere Formatierung
    }
}
