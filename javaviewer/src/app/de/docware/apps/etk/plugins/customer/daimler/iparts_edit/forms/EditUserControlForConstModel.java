/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsAAPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

/**
 * UserControl für Konstruktive Baumuster (Const Model)
 */
public class EditUserControlForConstModel extends EditUserControls implements iPartsConst {

    public EditUserControlForConstModel(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        if (field.getKey().getName().equals(TableAndFieldName.make(TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_AA))) {
            if (ctrl.getEditControl().getControl() instanceof EnumRComboBox) {
                EnumRComboBox rComboBox = (EnumRComboBox)ctrl.getEditControl().getControl();
                String seriesNo = StrUtils.copySubString(id.getValue(1), 0, 4);
                iPartsAAPartsHelper.setEnumTexteByX4E(getProject(), rComboBox, TABLE_DA_MODEL_PROPERTIES, FIELD_DMA_AA,
                                                      seriesNo, getProject().getDBLanguage(), true);
                rComboBox.setSelectedItem(initialValue);
            }
        }
    }


}
