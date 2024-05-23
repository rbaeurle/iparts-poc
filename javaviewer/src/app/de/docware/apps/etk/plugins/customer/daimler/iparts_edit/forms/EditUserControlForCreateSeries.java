/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * UserControlForCreate für Baureihe (Series)
 */
public class EditUserControlForCreateSeries extends EditUserControlForCreate implements iPartsConst {

    public EditUserControlForCreateSeries(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        EventListener listener = new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doEnableButtons(event);
            }
        };
        iPartsEditUserControlsHelper.modifySeriesFormEditControls(getProject(), ctrl, field, initialValue, parentForm, id, listener);
    }

    private iPartsGuiSeriesSOPField getSeriesSOPField() {
        EditControl controlByFeldIndex = getEditControlByTableAndFieldName(TABLE_DA_SERIES, MasterDataSeriesForm.FIELD_DS_SOP);
        if ((controlByFeldIndex != null) && (controlByFeldIndex.getEditControl().getControl() instanceof iPartsGuiSeriesSOPField)) {
            return (iPartsGuiSeriesSOPField)controlByFeldIndex.getEditControl().getControl();
        }
        return null;
    }

    public iPartsDataSeries getDataFromSOPField() {
        iPartsGuiSeriesSOPField seriesSOPField = getSeriesSOPField();
        if (seriesSOPField != null) {
            return seriesSOPField.getDataSeries();
        }
        return null;
    }

    @Override
    protected boolean checkPkValuesForModified() {
        boolean isModified = super.checkPkValuesForModified();
        if (!isModified) {
            iPartsGuiSeriesSOPField seriesSOPField = getSeriesSOPField();
            if (seriesSOPField != null) {
                isModified = seriesSOPField.isModified();
            }
        }
        if (!id.allValuesFilled()) {
            EditControl controlByFeldIndex = getEditControlByTableAndFieldName(TABLE_DA_SERIES, FIELD_DS_SERIES_NO);
            iPartsGuiSeriesSOPField seriesSOPField = getSeriesSOPField();
            if ((controlByFeldIndex != null) && (seriesSOPField != null)) {
                String seriesNo = controlByFeldIndex.getEditControl().getText();
                if (StrUtils.isValid(seriesNo) && !seriesNo.equals(seriesSOPField.getDataSeries().getAsId().getSeriesNumber())) {
                    seriesSOPField.setSeriesId(getProject(), new iPartsSeriesId(seriesNo));
                }
            }

        }
        return isModified;
    }

    @Override
    protected void collectEditValues() {
        iPartsEditUserControlsHelper.collectEditValuesForSeriesForm(getProject(), attributes, editFields, editControls, id);
    }

}
