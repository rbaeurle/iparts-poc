/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsEditUserControlsHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

/**
 * UserControl für Baureihe (Series)
 */
public class EditUserControlForSeries extends EditUserControls implements iPartsConst {

    // Flag, ob es sich um einen "Sonder"-Edit handelt. Eine Baureihe darf mit dem Recht "ADD_SERIES_TO_AUTO_CALC_AND_EXPORT"
    // für die automatische Berechnung und Bereitstellung markiert werden. Wenn EDIT_MASTER_DATA nicht vorhanden ist,
    // darf nur dieses eine Flag an der Baureihe editierbar sein.
    private boolean isOnlyAutoCalcAndExportEditAllowed;

    public EditUserControlForSeries(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName, IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
        isOnlyAutoCalcAndExportEditAllowed = iPartsRight.ADD_SERIES_TO_AUTO_CALC_AND_EXPORT.checkRightInSession() && !iPartsRight.EDIT_MASTER_DATA.checkRightInSession();
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
    protected void collectEditValues() {
        iPartsEditUserControlsHelper.collectEditValuesForSeriesForm(getProject(), attributes, editFields, editControls, id);
    }

    protected boolean isModified() {
        boolean isModified = super.isModified();
        if (!isModified) {
            iPartsGuiSeriesSOPField seriesSOPField = getSeriesSOPField();
            if (seriesSOPField != null) {
                isModified = seriesSOPField.isModified();
            }
        }
        return isModified;
    }


    @Override
    protected boolean checkForModified() {
        boolean isModified = super.checkForModified();
        if (!isModified) {
            iPartsGuiSeriesSOPField seriesSOPField = getSeriesSOPField();
            if (seriesSOPField != null) {
                isModified = seriesSOPField.isModified();
            }
        }
        return isModified;
    }

    @Override
    protected boolean getReadOnlyValueForControl(EditControlFactory editControl, boolean readOnly) {
        // EDIT_MASTER_DATA Recht ist nicht vorhanden aber der Benutzer darf Baureihen für den automatischen Export
        // markieren (Recht: ADD_SERIES_TO_AUTO_CALC_AND_EXPORT). Somit darf nur das eine Flag an der Baureihe editierbar
        // sein.
        if (isOnlyAutoCalcAndExportEditAllowed && !editControl.getTableFieldName().equals(TableAndFieldName.make(TABLE_DA_SERIES, FIELD_DS_AUTO_CALCULATION))) {
            return true;
        }
        return super.getReadOnlyValueForControl(editControl, readOnly);
    }
}
