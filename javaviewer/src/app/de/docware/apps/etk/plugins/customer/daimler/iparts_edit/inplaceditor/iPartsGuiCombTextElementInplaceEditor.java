/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.InplaceEdit.InplaceEditor;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.AbstractGuiCombTextButtonField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiCombTextCompleteEditControl;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditCombTextHelper;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.util.Utils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Abstrakte Klasse für die Inline-Editoren für die einzelnen Bausteine eines kombinierten Textes
 */
public abstract class iPartsGuiCombTextElementInplaceEditor extends InplaceEditor {

    private iPartsGuiCombTextCompleteEditControl combTextCompleteEditControl;
    private boolean textChanged;

    protected iPartsGuiCombTextElementInplaceEditor(AbstractGuiControl editorControl) {
        super(editorControl);
    }

    @Override
    public void init(AbstractJavaViewerFormIConnector connector) {
        AbstractGuiCombTextButtonField elementControl = getElementControl();
        if (elementControl != null) {
            elementControl.setConnector(connector);
            elementControl.setButtonVisible(true);
            elementControl.setCombTextChangeCallback(new AbstractGuiCombTextButtonField.CombTextChangeCallback() {
                @Override
                public void onButtonClick() {
                    getCombTextCompleteEditControl().setSearchTextKindTypes(getSearchType());
                    String originalText = getCombTextCompleteEditControl().getText();
                    getElementControlFromCombEditor().getCombTextChangeCallback().onButtonClick();
                    iPartsDataCombTextList combText = getElementControlFromCombEditor().getDataCombTextList();
                    if (Utils.isValid(combText) || combTextCompleteEditControl.isEditedByEditor()) {
                        getElementControl().setDataCombTextList(combText);
                    } else {
                        getElementControl().setDataCombTextList(null);
                    }
                    getElementControl().setText(getElementControlFromCombEditor().getText());
                    textChanged = !getCombTextCompleteEditControl().getText().equals(originalText);
                }

                @Override
                public int getNextSeqNo() {
                    return getElementControlFromCombEditor().getCombTextChangeCallback().getNextSeqNo();
                }

                @Override
                public void textChangeFromSearch(boolean newDataObjectCreated) {
                    getElementControlFromCombEditor().setDataCombTextList(getElementControl().getDataCombTextList());
                    getElementControlFromCombEditor().setText(getElementControl().getText());
                    getElementControlFromCombEditor().getCombTextChangeCallback().textChangeFromSearch(newDataObjectCreated);
                }
            });
        }
    }

    protected abstract EnumSet<DictTextKindTypes> getSearchType();


    protected abstract AbstractGuiCombTextButtonField getElementControl();

    @Override
    public boolean updateEditor(Object cellContent, List<EtkDataObject> dataObjects, int width) {
        if (dataObjects == null) {
            return false;
        }

        AbstractGuiCombTextButtonField textFieldControl = getElementControl();
        if (textFieldControl != null) {
            textFieldControl.setForwardESCKeyReleasedEvent(true);
            textFieldControl.setForwardENTERKeyReleasedEvent(true);
            for (EtkDataObject dataObject : dataObjects) {
                if (dataObject instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)dataObject;
                    textFieldControl.setPartListEntryId(partListEntry.getAsId());
                    getCombTextCompleteEditControl().setPartListEntryId(partListEntry.getAsId());
                    break;
                }
            }
            getElementControl().setDataCombTextList(getElementControlFromCombEditor().getDataCombTextList());
            String cellText = getDBCellContentText(dataObjects, cellContent);
            textFieldControl.resetFilterText();
            textFieldControl.setText(cellText);
            getElementControlFromCombEditor().setText(cellText);
        }
        return true;
    }

    protected abstract AbstractGuiCombTextButtonField getElementControlFromCombEditor();

    @Override
    public boolean isModified(AbstractGuiControl inPlaceEditor, Object originalCellContent) {
        AbstractGuiCombTextButtonField textField = getElementControl();
        if ((textField != null) && (inPlaceEditor == textField)) {
            String originalText = getDBCellContentText(null, originalCellContent);
            String newText = textField.getText();
            if (!originalText.equals(newText) || textChanged) { // Wert hat sich auch wirklich geändert
                return true;
            }
        }
        return false;
    }

    @Override
    public String getEditResult(EtkDataObject dataObject) {
        AbstractGuiCombTextButtonField textFieldControl = getElementControl();
        if (textFieldControl != null) {
            return textFieldControl.getEditResult();
        }
        return null;
    }

    public void setCompleteControl(iPartsGuiCombTextCompleteEditControl combTextCompleteEditControl) {
        this.combTextCompleteEditControl = combTextCompleteEditControl;
    }

    public iPartsGuiCombTextCompleteEditControl getCombTextCompleteEditControl() {
        return combTextCompleteEditControl;
    }

    @Override
    public Set<AssemblyId> saveAdditionalDataFromInplaceControl(EtkProject project, EtkDataPartListEntry partListEntry) {
        return iPartsEditCombTextHelper.storeDataCombList(project, getCombTextCompleteEditControl(), partListEntry);
    }

}
