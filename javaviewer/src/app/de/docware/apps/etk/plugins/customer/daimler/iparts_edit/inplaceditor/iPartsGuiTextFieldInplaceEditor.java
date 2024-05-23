/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.InplaceEdit.InplaceEditor;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiTextField;

import java.util.List;

/**
 * Implementierung des Inplace Editors für ein simples {@link GuiTextField} in der Stückliste
 */
public class iPartsGuiTextFieldInplaceEditor extends InplaceEditor {

    public iPartsGuiTextFieldInplaceEditor() {
        super(new GuiTextField());
    }

    protected iPartsGuiTextFieldInplaceEditor(AbstractGuiControl editorControl) {
        super(editorControl);
    }

    @Override
    public void init(AbstractJavaViewerFormIConnector connector) {
    }

    @Override
    public boolean updateEditor(Object cellContent, List<EtkDataObject> dataObjects, int width) {
        GuiTextField textField = getTextFieldControl();
        if (textField != null) {
            String cellText = getDBCellContentText(dataObjects, cellContent);
            textField.setText(cellText);
            textField.selectAll(); // funktioniert nicht, vermutlich weil der OPENED Event nie an der Textbox ankommt
        }
        return true;
    }

    @Override
    public boolean isModified(AbstractGuiControl inPlaceEditor, Object originalCellContent) {
        GuiTextField textField = getTextFieldControl();
        if ((textField != null) && (inPlaceEditor == textField)) {
            String originalText = getDBCellContentText(null, originalCellContent);
            String newText = textField.getText();
            newText = modifyNewText(newText);
            if (!originalText.equals(newText)) { // Wert hat sich auch wirklich geändert
                return true;
            }
        }
        return false;
    }

    protected String modifyNewText(String newText) {
        return newText;
    }

    @Override
    public String getEditResult(EtkDataObject dataObject) {
        GuiTextField textField = getTextFieldControl();
        if (textField != null) {
            return textField.getText();
        }
        return "";
    }

    private GuiTextField getTextFieldControl() {
        if ((editorControl instanceof GuiTextField)) {
            return (GuiTextField)this.editorControl;
        }
        return null;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        GuiTextField textFieldControl = getTextFieldControl();
        if (textFieldControl != null) {
            textFieldControl.selectAll();
        }
    }
}
