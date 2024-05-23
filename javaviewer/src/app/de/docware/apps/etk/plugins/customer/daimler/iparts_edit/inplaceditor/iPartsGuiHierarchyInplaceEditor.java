/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.base.forms.common.InplaceEdit.InplaceEditor;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditEqualizeFieldsHelper;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;
import java.util.Set;

public class iPartsGuiHierarchyInplaceEditor extends InplaceEditor {

    public static final String DEFAULT_TABLE_AND_FIELD_NAME = TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_HIERARCHY);

    public iPartsGuiHierarchyInplaceEditor() {
        super(new EnumRComboBox());
    }

    @Override
    public void init(AbstractJavaViewerFormIConnector connector) {
        EnumRComboBox editControl = getEditControl();
        if (editControl != null) {
            String tableAndFieldname = getTableAndFieldname();
            editControl.setEnumTexte(connector.getProject(), TableAndFieldName.getTableName(tableAndFieldname),
                                     TableAndFieldName.getFieldName(tableAndFieldname), connector.getProject().getDBLanguage(), true);
        }
    }

    @Override
    public boolean updateEditor(Object cellContent, List<EtkDataObject> dataObjects, int width) {
        EnumRComboBox editControl = getEditControl();
        if (editControl != null) {
            editControl.setForwardESCKeyReleasedEvent(true);
            editControl.setForwardENTERKeyReleasedEvent(true);
            String cellText = getDBCellContentText(dataObjects, cellContent);
            editControl.setActToken(cellText);
        }
        return true;
    }

    @Override
    public boolean isModified(AbstractGuiControl inPlaceEditor, Object originalCellContent) {
        EnumRComboBox editControl = getEditControl();
        if ((editControl != null) && (inPlaceEditor == editControl)) {
            String originalText = getDBCellContentText(null, originalCellContent);
            String actToken = editControl.getActToken();
            if (!originalText.equals(actToken)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getEditResult(EtkDataObject dataObject) {
        EnumRComboBox editControl = getEditControl();
        if (editControl != null) {
            return editControl.getActToken();
        }
        return "";
    }

    private EnumRComboBox getEditControl() {
        if (editorControl instanceof EnumRComboBox) {
            return (EnumRComboBox)editorControl;
        }
        return null;
    }

    @Override
    public Set<AssemblyId> saveAdditionalDataFromInplaceControl(EtkProject project, EtkDataPartListEntry partListEntry) {
        return EditEqualizeFieldsHelper.doEqualizeForInplaceEditor(project, partListEntry);
    }
}
