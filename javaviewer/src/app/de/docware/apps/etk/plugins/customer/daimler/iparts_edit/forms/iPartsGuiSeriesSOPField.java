/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EingabeFeld für Start of Production (SOP)
 */
public class iPartsGuiSeriesSOPField extends GuiPanel {

    public static final String TYPE = "iPartsGuiSeriesSOPField";
    // Dummy Feld für Expire-Date
    public static final String FIELD_DSP_EXPIRE_DATE = "DSP_EXPIRE_DATE";

    private iPartsGuiSeriesAAComboBox rComboBox;
    private GuiButton button;
    private iPartsDataSeries dataSeries;
    private boolean enabled = true;

    public iPartsGuiSeriesSOPField() {
        super(); // Default-Initialisierung des "Panels"
        type = TYPE; // Überlagern des tatsächlichen Typs
        __internal_initializeChildComponents();
        __internal_setTestNameOnControl();
    }

    private void __internal_initializeChildComponents() {
        setLayout(new LayoutGridBag());
        // Initialisierung der Eingabekomponente
        rComboBox = new iPartsGuiSeriesAAComboBox();
        rComboBox.setEditable(false);
        button = new GuiButton();
        setButtonText("...");
        button.setMinimumWidth(8);
        button.setMaximumWidth(21);
        button.setMinimumHeight(rComboBox.getPreferredHeight());

        rComboBox.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 0, 0, 0));
        button.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 0, 0, 0));

        this.addChild(rComboBox);
        this.addChild(button);

        // OnChange Event des GuiTextfields an die GuiFileChooserTextfield Komponente weitergeben, die es dann intern verwerten und dispatchen kann
        // Es ist notwendig, dass ein OnChange-Event vom GuiFileChooserTextfield geworfen wird (und nicht nur von den Kind-Controls),
        // wenn man manuell das Eingabefeld editiert
        final iPartsGuiSeriesSOPField _self = this;
        rComboBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            public void fire(Event event) {
                updateButtonEnabled();

                // Die Kind-Komponente aktualisiert ihren eigenen Zustand. Ist sie aktualisiert, holen wir uns den neuen Wert aus dem Kind
                // iPartsGuiSeriesSOPField muss seinen eigenen Zustand nach der Änderung im Kind aktualisieren. Dabei dürfen keine Events ausgelöst werden.
                fireEvent(EventCreator.createOnChangeEvent(_self.getEventHandlerComponent(), _self.getUniqueId())); // Registrierte Listener informieren
            }
        });
    }

    public void init(final AbstractJavaViewerForm parentForm, iPartsSeriesId seriesId) {
        if (parentForm != null) {
            setSeriesId(parentForm.getProject(), seriesId);
            button.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
                @Override
                public void fire(Event event) {
                    doButtonClick(parentForm);
                }
            });
        } else {
            this.dataSeries = null;
            rComboBox.removeAllItems();
            button.removeEventListeners(Event.ACTION_PERFORMED_EVENT);
        }
    }

    @Override
    protected void __internal_setTestNameOnControl() {
        if (!Constants.DEVELOPMENT_QFTEST) {
            return;
        }
        super.__internal_setTestNameOnControl();
        String fullName = __internal_getFullTestNameForControl();
        rComboBox.__internal_setFullName(fullName + "_combobox");
        button.__internal_setFullName(fullName + "_button");
    }

    public void setSeriesId(EtkProject project, iPartsSeriesId seriesId) {
        if (seriesId.isValidId()) {
            button.setEnabled(true);
            dataSeries = new iPartsDataSeries(project, seriesId);
            if (!dataSeries.existsInDB()) {
                dataSeries.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
        } else {
            button.setEnabled(false);
            dataSeries = new iPartsDataSeries(project, seriesId);
            dataSeries.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
        rComboBox.fillSeriesAAEnumValues(project, seriesId, iPartsConst.TABLE_DA_DIALOG, iPartsConst.FIELD_DD_AA);
        markExistingSOPValues();
    }

    public iPartsDataSeries getDataSeries() {
        return dataSeries;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateButtonEnabled();
    }

    private void updateButtonEnabled() {
        boolean enableButton = enabled && !rComboBox.getActToken().isEmpty();
        if (!enabled) {
            String aa = getActToken();
            if (!aa.isEmpty()) {
                iPartsDataSeriesSOP dataSeriesSOP = findSOP_AA(aa);
                if (dataSeriesSOP != null) {
                    enableButton = true;
                }
            }
        }
        button.setEnabled(enableButton);
    }

    private void markExistingSOPValues() {
        List<String> enumTexts = new DwList<>();
        String actToken = getActToken();
        String setToken = "";
        for (String token : rComboBox.getTokens()) {
            token = StrUtils.removeAllLastCharacterIfCharacterIs(token, " *");
            if (!token.isEmpty()) {
                iPartsDataSeriesSOP dataSeriesSOP = findSOP_AA(token);
                if (actToken.equals(token)) {
                    if (dataSeriesSOP == null) {
                        token = token + " *";
                    }
                    setToken = token;
                } else {
                    if (dataSeriesSOP == null) {
                        token = token + " *";
                    }
                }
                enumTexts.add(token);
            }
        }
        rComboBox.setEnumTexte(enumTexts);
        rComboBox.setActToken(setToken);
    }

    public void setButtonText(String text) {
        this.button.setText(text);
        this.button.setMaximumWidth(button.getPreferredWidth());
    }

    /**
     * Klick auf den Button. Kann von ableitenden Komponenten überschrieben werden, falls die Gui Komponente selbst auf
     * das Event reagieren soll
     */
    private void doButtonClick(AbstractJavaViewerForm parentForm) {
        EtkEditFields editFields = getEditFields(parentForm.getConnector().getProject());

        String aa = getActToken();
        boolean isNew = false;
        iPartsDataSeriesSOP dataSeriesSOP = findSOP_AA(aa);
        if (dataSeriesSOP == null) {
            isNew = true;
            iPartsSeriesSOPId seriesSOPId = new iPartsSeriesSOPId(dataSeries.getAsId().getSeriesNumber(), aa);
            dataSeriesSOP = new iPartsDataSeriesSOP(parentForm.getProject(), seriesSOPId);
            dataSeriesSOP.initAttributesWithDefaultValues(DBActionOrigin.FROM_DB);
            // damit der OK-Button enabled ist
            dataSeriesSOP.setFieldValueAsBoolean(iPartsConst.FIELD_DSP_ACTIVE, !dataSeriesSOP.getFieldValueAsBoolean(iPartsConst.FIELD_DSP_ACTIVE), DBActionOrigin.FROM_EDIT);
            dataSeriesSOP.setFieldValueAsBoolean(iPartsConst.FIELD_DSP_ACTIVE, !dataSeriesSOP.getFieldValueAsBoolean(iPartsConst.FIELD_DSP_ACTIVE), DBActionOrigin.FROM_EDIT);
        }
        DBDataObjectAttributes attributes = dataSeriesSOP.getAttributes().cloneMe(DBActionOrigin.FROM_DB);
        if (editFields.getFeldByName(iPartsConst.TABLE_DA_SERIES_SOP, FIELD_DSP_EXPIRE_DATE) != null) {
            attributes.addField(FIELD_DSP_EXPIRE_DATE, "", DBActionOrigin.FROM_DB);
        }
        EditUserControlForSOP eCtrl = new EditUserControlForSOP(parentForm.getConnector(), parentForm, iPartsConst.TABLE_DA_SERIES_SOP, dataSeriesSOP.getAsId(), attributes, editFields);
        eCtrl.setExpiredDateList(dataSeries.getSeriesExpireDateForAAList(aa).getAsList());
        String title = "!!Start of Production (SOP) editieren";
        if (!enabled) {
            title = "!!Start of Production (SOP) anzeigen";
        } else if (isNew) {
            title = "!!Start of Production (SOP) anlegen";
        }
        eCtrl.setTitle(title);
        if (!enabled) {
            eCtrl.setReadOnly(true);
        } else if (!isNew) {
            eCtrl.addButton(GuiButtonOnPanel.ButtonType.NEXT, ModalResult.NO_TO_ALL, "!!Löschen", null);
        }
        ModalResult modalResult = eCtrl.showModal();
        if (modalResult == ModalResult.OK) {
            List<iPartsDataSeriesExpireDate> seriesExpireDateList = eCtrl.getExpireDatesAsList();
            dataSeriesSOP.setAttributes(eCtrl.getAttributes(), DBActionOrigin.FROM_EDIT);
            dataSeriesSOP.removeForeignTablesAttributes();
            if (isNew) {
                dataSeries.getSeriesSOPList().add(dataSeriesSOP, DBActionOrigin.FROM_EDIT);
                dataSeries.getSeriesExpireDateList().addAll(seriesExpireDateList, DBActionOrigin.FROM_EDIT);
            } else {
                updateExpireLists(dataSeries.getSeriesExpireDateList(), seriesExpireDateList);
            }
            markExistingSOPValues();
        } else if (modalResult == ModalResult.NO_TO_ALL) {
            dataSeries.getSeriesSOPList().delete(dataSeriesSOP, true, DBActionOrigin.FROM_EDIT);
            dataSeries.getSeriesExpireDateList().deleteAll(DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            markExistingSOPValues();
        }
    }

    private void updateExpireLists(DBDataObjectList<iPartsDataSeriesExpireDate> currentSeriesExpireDateList,
                                   List<iPartsDataSeriesExpireDate> seriesExpireDateList) {
        Map<iPartsSeriesExpireDateId, iPartsDataSeriesExpireDate> existingExpireDateMap = new HashMap<>();
        // vorhandene Einträge sammeln
        for (iPartsDataSeriesExpireDate currentDataSeriesExpireDate : currentSeriesExpireDateList) {
            existingExpireDateMap.put(currentDataSeriesExpireDate.getAsId(), currentDataSeriesExpireDate);
        }
        // neue Einträge einpflegen
        for (iPartsDataSeriesExpireDate dataSeriesExpireDate : seriesExpireDateList) {
            iPartsDataSeriesExpireDate currentDataSeriesExpireDate = currentSeriesExpireDateList.getById(dataSeriesExpireDate.getAsId());
            if (currentDataSeriesExpireDate == null) {
                // ist neu hinzugekommen
                currentSeriesExpireDateList.add(dataSeriesExpireDate, DBActionOrigin.FROM_EDIT);
            } else {
                // existiert schon => update und aus Map entfernen
                currentDataSeriesExpireDate.getAttributes().assign(dataSeriesExpireDate.getAttributes(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                existingExpireDateMap.remove(dataSeriesExpireDate.getAsId());
            }
        }
        if (!existingExpireDateMap.isEmpty()) {
            // bereits vorher gelöschte nicht nochmal Löschen
            for (iPartsDataSeriesExpireDate deletedDataSeriesExpireDate : currentSeriesExpireDateList.getDeletedList()) {
                existingExpireDateMap.remove(deletedDataSeriesExpireDate.getAsId());
            }
            // vorhandene Einträge wurden durch Edit entfernt => ebenfalls entfernen
            for (iPartsDataSeriesExpireDate currentDataSeriesExpireDate : existingExpireDateMap.values()) {
                currentSeriesExpireDateList.delete(currentDataSeriesExpireDate, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            }
        }
    }

    private iPartsDataSeriesSOP findSOP_AA(String aa) {
        for (iPartsDataSeriesSOP dataSeriesSOP : dataSeries.getSeriesSOPList()) {
            if (dataSeriesSOP.getAsId().getSeriesAA().equals(aa)) {
                return dataSeriesSOP;
            }
        }
        return null;
    }

    private EtkEditFields getEditFields(EtkProject project) {
        EtkEditFields editFields = new EtkEditFields();
//        editFields.load(dataConnector.getConfig(), iPartsEditConfigConst.iPARTS_EDIT_MASTER_SERIES_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        return createEditFields(project, editFields);
    }

    private EtkEditFields createEditFields(EtkProject project, EtkEditFields editFields) {
        if (editFields.size() == 0) {
            EtkEditField editField = MasterDataSeriesForm.createEditField(project, iPartsConst.TABLE_DA_SERIES_SOP, iPartsConst.FIELD_DSP_SERIES_NO, false);
            editField.setMussFeld(true);
            editField.setEditierbar(false);
            editFields.addFeld(editField);
            editField = MasterDataSeriesForm.createEditField(project, iPartsConst.TABLE_DA_SERIES_SOP, iPartsConst.FIELD_DSP_AA, false);
            editField.setMussFeld(true);
            editField.setEditierbar(false);
            editFields.addFeld(editField);
            editFields.addFeld(MasterDataSeriesForm.createEditField(project, iPartsConst.TABLE_DA_SERIES_SOP, iPartsConst.FIELD_DSP_START_OF_PROD, false));
            editField = MasterDataSeriesForm.createEditField(project, iPartsConst.TABLE_DA_SERIES_SOP, FIELD_DSP_EXPIRE_DATE, false);
            editField.setDefaultText(false);
            EtkMultiSprache text = new EtkMultiSprache("!!Auslauftermin/Werk", project.getConfig().getDatabaseLanguages());
            editField.setText(text);
            editFields.addFeld(editField);

            editFields.addFeld(MasterDataSeriesForm.createEditField(project, iPartsConst.TABLE_DA_SERIES_SOP, iPartsConst.FIELD_DSP_KEM_TO, false));
            editFields.addFeld(MasterDataSeriesForm.createEditField(project, iPartsConst.TABLE_DA_SERIES_SOP, iPartsConst.FIELD_DSP_ACTIVE, false));
        }
        return editFields;
    }

    public boolean isModified() {
        return dataSeries.isModifiedWithChildren();
    }

    public void setActToken(String value) {
        rComboBox.setActToken(value);
    }

    public String getActToken() {
        return StrUtils.removeAllLastCharacterIfCharacterIs(rComboBox.getActToken(), " *");
    }

    public void setSelectedIndex(int index) {
        rComboBox.setSelectedIndex(index);
    }
}
