/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.common.EditControlDateTimeEditPanel;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeriesExpireDate;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSeriesSOPId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsAAPartsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;
import java.util.Map;

/**
 * EditUserControls für SOP-Field
 */
public class EditUserControlForSOP extends EditUserControls {

    public EditUserControlForSOP(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, String tableName,
                                 IdWithType id, DBDataObjectAttributes attributes, EtkEditFields externalEditFields) {
        super(dataConnector, parentForm, tableName, id, attributes, externalEditFields);
    }

    public void setExpiredDateList(List<iPartsDataSeriesExpireDate> seriesExpireDateList) {
        iPartsGuiFactoryExpireDatePanel expireDatePanel = getExpireDatePanel();
        if (expireDatePanel != null) {
            expireDatePanel.setExistingExpireList(seriesExpireDateList);
        }
    }

    public List<iPartsDataSeriesExpireDate> getExpireDatesAsList() {
        iPartsGuiFactoryExpireDatePanel expireDatePanel = getExpireDatePanel();
        if (expireDatePanel != null) {
            return expireDatePanel.getExpireDatesAsList();
        }
        return new DwList<>();
    }

    @Override
    protected void modifyEditControl(EditControl ctrl, EtkEditField field, String initialValue, EtkDataArray initialDataArray) {
        if (field.getKey().getName().equals(TableAndFieldName.make(TABLE_DA_SERIES_SOP, FIELD_DSP_AA))) {
            iPartsAAPartsHelper.setAAEnumValuesByX4E(ctrl.getEditControl(), getProject(), ((iPartsSeriesSOPId)id).getSeriesNumber());
            if (ctrl.getEditControl().getControl() instanceof EnumRComboBox) {
                EnumRComboBox rComboBox = (EnumRComboBox)ctrl.getEditControl().getControl();
                iPartsAAPartsHelper.setEnumTexteByX4E(getProject(), rComboBox, tableName, FIELD_DSP_AA,
                                                      ((iPartsSeriesSOPId)id).getSeriesNumber(), getProject().getDBLanguage(), true);
                rComboBox.setSelectedItem(initialValue);
            }
        } else if (field.getKey().getName().equals(TableAndFieldName.make(TABLE_DA_SERIES_SOP, iPartsGuiSeriesSOPField.FIELD_DSP_EXPIRE_DATE))) {
            EtkEditField aaField = editFields.getFeldByName(TABLE_DA_SERIES_SOP, FIELD_DSP_AA);
            DBDataObjectAttribute attrib = getAttributeFromKey(aaField);
            String ausfuehrungsart = calculateInitialValue(field, attrib);
            iPartsGuiFactoryExpireDatePanel expireDatePanel = new iPartsGuiFactoryExpireDatePanel(getProject(), new iPartsSeriesId(((iPartsSeriesSOPId)id).getSeriesNumber()), ausfuehrungsart);
            expireDatePanel.setWithButton(true);
            expireDatePanel.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    ShowFactoryExpireDateForm.showFactoryExpireDate(getConnector(), EditUserControlForSOP.this,
                                                                    expireDatePanel.getSeriesId(), expireDatePanel.getSeriesAA(),
                                                                    expireDatePanel.getExpireDatesForShowAsList());
                }
            });
            // Listener für den Check, ob bei Auslaufterminen ein SOP Termin vorhanden ist
            addSOPAndExpDateListener(expireDatePanel);
            // Default Control wird durch iPartsGuiFactoryExpireDatePanel ausgetauscht
            ctrl.getEditControl().setControl(expireDatePanel);
        } else if (field.getKey().getName().equals(TableAndFieldName.make(TABLE_DA_SERIES_SOP, FIELD_DSP_START_OF_PROD))) {
            AbstractGuiControl sopControl = ctrl.getAbstractGuiControl();
            if (sopControl instanceof EditControlDateTimeEditPanel) {
                // Listener für den Check, ob bei Auslaufterminen ein SOP Termin vorhanden ist
                addSOPAndExpDateListener(sopControl);
            }
        }
    }

    /**
     * Fügt einen Listener für den Check, ob bei Auslaufterminen ein SOP Termin vorhanden ist hinzu
     *
     * @param control
     */
    private void addSOPAndExpDateListener(AbstractGuiControl control) {
        control.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                EditControl ctrl = getEditControlByFieldName(iPartsGuiSeriesSOPField.FIELD_DSP_EXPIRE_DATE);
                if (ctrl != null) {
                    boolean isValid = isSOPAndExpDatesValid();
                    ctrl.getLabel().setForegroundColor(isValid ? Colors.clDefault : Colors.clDesignOrderControlErrorColor);
                    String tooltipText = isValid ? "" : "!!Auslauftermine sind nicht zulässig wenn kein SOP Termin vorhanden ist!";
                    ctrl.getLabel().setTooltip(tooltipText);
                    setOKButtonTooltip(tooltipText);
                    ctrl.getAbstractGuiControl().setTooltip(tooltipText);
                }
            }
        });
    }

    @Override
    protected boolean checkForModified() {
        DBDataObjectAttributes clonedAttributes = getCurrentAttributes();
        if (attributes != null) {
            boolean modified = clonedAttributes.isModified();
            // Hat sich etwas an den Attributen von DA_SERIES_SOP verändert hat, muss geprüft werden, ob die Auslauftermine
            // gültig sind. Sind welche vorhanden und ein SOP Termin nicht, darf nicht gespeichert werden
            if (modified && !isSOPAndExpDatesValid()) {
                return false;
            }
            if (!modified) {
                iPartsGuiFactoryExpireDatePanel expireDatePanel = getExpireDatePanel();
                if (expireDatePanel != null) {
                    modified = expireDatePanel.isModified();
                }
            }
            return modified;
        }
        return true;
    }

    /**
     * Check, ob die Konstellation von SOP Datum und Auslaufterminen gültig ist.
     *
     * @return
     */
    private boolean isSOPAndExpDatesValid() {
        DBDataObjectAttribute sopValue = getCurrentAttributeValue(FIELD_DSP_START_OF_PROD);
        if ((sopValue != null) && StrUtils.isEmpty(sopValue.getAsString())) {
            iPartsGuiFactoryExpireDatePanel expireDatePanel = getExpireDatePanel();
            if (expireDatePanel != null) {
                if (!expireDatePanel.getExpireDatesAsList().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected boolean isModified() {
        boolean isModified = super.isModified();
        if (!isModified) {
            iPartsGuiFactoryExpireDatePanel expireDatePanel = getExpireDatePanel();
            if (expireDatePanel != null) {
                isModified = expireDatePanel.isModified();
            }
        }
        return isModified;
    }

    protected boolean checkValues() {
        boolean result = super.checkValues();
        if (result) {
            iPartsGuiFactoryExpireDatePanel expireDatePanel = getExpireDatePanel();
            if (expireDatePanel != null) {
                String sopDate = attributes.getField(FIELD_DSP_START_OF_PROD).getAsString();
                Map<String, String> expireDateMap = expireDatePanel.getCurrentExpiredMap();
                StringBuilder str = new StringBuilder();
                for (Map.Entry<String, String> entry : expireDateMap.entrySet()) {
                    if (StrUtils.isEmpty(entry.getValue())) {
                        continue;
                    }
                    String expireDate = entry.getValue();
                    if (StrUtils.isValid(sopDate)) {
                        if (StrUtils.isValid(expireDate)) {
                            if (sopDate.compareTo(expireDate) >= 0) {
                                if (str.toString().length() > 0) {
                                    str.append("\n");
                                }
                                str.append(TranslationHandler.translate("!!Der Auslauftermin \"%1\" für Werk %2 muss größer sein als der SOP-Termin \"%3\"",
                                                                        getVisualDateTime(expireDate), entry.getKey(), getVisualDateTime(sopDate)));
                                result = false;
                            }
                        }
                    }
                }
                if (!result) {
                    MessageDialog.showError(str.toString());
                }
            }
        }
        return result;
    }

    private iPartsGuiFactoryExpireDatePanel getExpireDatePanel() {
        AbstractGuiControl guiControl = getEditGuiControlByFieldName(iPartsGuiSeriesSOPField.FIELD_DSP_EXPIRE_DATE);
        if ((guiControl instanceof iPartsGuiFactoryExpireDatePanel)) {
            return (iPartsGuiFactoryExpireDatePanel)guiControl;
        }
        return null;
    }

    private String getVisualDateTime(String dbValue) {
        return getProject().getVisObject().asText(TABLE_DA_SERIES_SOP, FIELD_DSP_START_OF_PROD, dbValue, getProject().getViewerLanguage());
    }
}
