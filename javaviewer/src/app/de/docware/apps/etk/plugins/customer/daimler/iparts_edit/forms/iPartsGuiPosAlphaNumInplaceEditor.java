/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor.iPartsGuiTextFieldInplaceEditor;
import de.docware.framework.modules.gui.controls.viewer.GuiViewerUtils;

public class iPartsGuiPosAlphaNumInplaceEditor extends iPartsGuiTextFieldInplaceEditor {

    public iPartsGuiPosAlphaNumInplaceEditor() {
        super(new iPartsGuiHotSpotTextField(true));
    }

    @Override
    protected String modifyNewText(String newText) {
        return GuiViewerUtils.buildPosNumberValue(GuiViewerUtils.splitPosNumber(newText)); // saubere Formatierung
    }

}
