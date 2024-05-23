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
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiCombTextCompleteEditControl;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditCombTextHelper;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;
import java.util.Set;

public class iPartsGuiCombTextInplaceEditor extends InplaceEditor {

    public static final String DEFAULT_TABLE_AND_FIELD_NAME = TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.RETAIL_COMB_TEXT);

    public iPartsGuiCombTextInplaceEditor() {
        super(new iPartsGuiCombTextCompleteEditControl());
    }

    @Override
    public void init(AbstractJavaViewerFormIConnector connector) {
        iPartsGuiCombTextCompleteEditControl textFieldControl = getTextFieldControl();
        if (textFieldControl != null) {
            textFieldControl.setConnector(connector);
        }
    }

    @Override
    public boolean updateEditor(Object cellContent, List<EtkDataObject> dataObjects, int width) {
        if (dataObjects == null) {
            return false;
        }

        iPartsGuiCombTextCompleteEditControl textFieldControl = getTextFieldControl();
        if (textFieldControl != null) {
            textFieldControl.setForwardESCKeyReleasedEvent(true);
            textFieldControl.setForwardENTERKeyReleasedEvent(true);
            for (EtkDataObject dataObject : dataObjects) {
                if (dataObject instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)dataObject;
                    textFieldControl.setPartListEntryId(partListEntry.getAsId());
                    break;
                }
            }
            String cellText = getDBCellContentText(dataObjects, cellContent);
            textFieldControl.setText(cellText);
            textFieldControl.setDividerPosition((int)(width * 0.5));

        }
        return true;
    }

    @Override
    public boolean isModified(AbstractGuiControl inPlaceEditor, Object originalCellContent) {
        iPartsGuiCombTextCompleteEditControl textFieldControl = getTextFieldControl();
        if ((textFieldControl != null) && (inPlaceEditor == textFieldControl)) {
            String editedCombText = textFieldControl.getText();
            String originalCombText = getDBCellContentText(null, originalCellContent);
            if (!originalCombText.equals(editedCombText)) { // DataObject nur aktualisieren wenn der kombinierte Text tatsächlich geändert wurde
                return true;
            }
        }
        return false;
    }

    @Override
    public String getEditResult(EtkDataObject dataObject) {
        iPartsGuiCombTextCompleteEditControl textFieldControl = getTextFieldControl();
        if (textFieldControl != null) {
            return textFieldControl.getText();
        }
        return null;
    }

    public iPartsGuiCombTextCompleteEditControl getTextFieldControl() {
        if ((editorControl instanceof iPartsGuiCombTextCompleteEditControl)) {
            return (iPartsGuiCombTextCompleteEditControl)this.editorControl;
        }
        return null;
    }

    @Override
    public Set<AssemblyId> saveAdditionalDataFromInplaceControl(EtkProject project, EtkDataPartListEntry partListEntry) {
        return iPartsEditCombTextHelper.storeDataCombList(project, getTextFieldControl(), partListEntry);
    }
}
