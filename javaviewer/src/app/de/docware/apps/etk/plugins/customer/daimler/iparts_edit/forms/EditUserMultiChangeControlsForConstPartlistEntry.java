/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;

/**
 * Dialog um in einer Tabelle in der Konstruktion mehrere Zellenwerte zu vereinheitlichen
 */
public class EditUserMultiChangeControlsForConstPartlistEntry extends EditUserMultiChangeControls {

    public static DBDataObjectAttributes showEditUserMultiChangeControlsForConstPartListEntries(AbstractJavaViewerFormIConnector dataConnector, EtkEditFields externalEditFields,
                                                                                                List<EtkDataPartListEntry> selectedEntries) {
        DBDataObjectAttributes initValues = getInitialAttributesForUnify(selectedEntries, getFieldsAndDefaultValuesForConstPartListUnify());
        EditUserMultiChangeControlsForConstPartlistEntry multiControl = new EditUserMultiChangeControlsForConstPartlistEntry(dataConnector, dataConnector.getActiveForm(), externalEditFields, initValues);
        return showEditUserMultiChangeControls(dataConnector, externalEditFields, initValues, multiControl, UnifySource.CONSTURCTION);

    }


    /**
     * Sammelt die Stücklisteneintrag-Felder zusammen, die vordefinierte Default-Werte haben sollen
     *
     * @return
     */
    public static List<FieldAndDefaultValue> getFieldsAndDefaultValuesForConstPartListUnify() {
        List<FieldAndDefaultValue> result = new DwList<>();
        result.add(new FieldAndDefaultValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT, iPartsDocuRelevant.DOCU_RELEVANT_NOT_SPECIFIED.getDbValue()));
        result.add(new FieldAndDefaultValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_STATUS, iPartsDataReleaseState.RELEASED.getDbValue()));
        return result;
    }

    public EditUserMultiChangeControlsForConstPartlistEntry(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                            EtkEditFields externalEditFields, DBDataObjectAttributes initialAttributes) {
        super(dataConnector, parentForm, externalEditFields, initialAttributes, UnifySource.CONSTURCTION);
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        super.modifyEditControl(ctrl, field, initialValue, initialDataArray);
        if (field.getKey().getTableName().equals(iPartsConst.TABLE_KATALOG)) {
            if (field.getKey().getFieldName().equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT)) {
                iPartsEditUserControlsHelper.handleDocuRelevantControl(getProject(), ctrl, initialValue);
            } else if (field.getKey().getFieldName().equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_STATUS)) {
                iPartsEditUserControlsHelper.handleDIALOGStatusControl(ctrl, initialValue);
            }
        }
    }

    @Override
    protected boolean checkBoxEnabled(EditControlFactory ctrl, GuiCheckbox checkbox) {
        if (ctrl.getTableName().equals(iPartsConst.TABLE_KATALOG) &&
            ctrl.getFieldName().equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_DOCU_RELEVANT)) {
            iPartsDocuRelevant docuRel = iPartsDocuRelevant.getFromDBValue(ctrl.getRawText());
            if ((iPartsDocuRelevant.isDocumented(docuRel))) {
                return false;
            }
        }
        return super.checkBoxEnabled(ctrl, checkbox);
    }
}
