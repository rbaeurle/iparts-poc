/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TwoGridValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListeners;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;

import java.util.List;
import java.util.Set;

/**
 * Erweiterung eines {@link GuiButtonTextField}s für alle {@link iPartsFilterGridForm.DIALOG_TYPES}
 */
public class iPartsGuiDIALOGTypesButtonTextField extends GuiButtonTextField {

    public static final String TYPE = "ipartsdialogtypesbuttontextfield";

    // Die Instanz die die onChangeEvents Aktionen abarbeitet
    protected EventListeners eventOnChangeListeners;
    protected iPartsFilterGridForm.DIALOG_TYPES dialogType;
    private AbstractJavaViewerForm parentForm;
    private boolean directTransfer;
    private boolean accessWithoutModel;
    private iPartsModel currentModel;
    private String productGroupEnumValue;
    private TwoGridValues twoGridValues;
    private boolean valuesChanged;
    private boolean isViewing;
    private boolean withUserDefinedValues = false;

    public iPartsGuiDIALOGTypesButtonTextField(AbstractJavaViewerForm parentForm, iPartsFilterGridForm.DIALOG_TYPES dialogType) {
        super();
        setType(TYPE);
        this.parentForm = parentForm;
        this.dialogType = dialogType;
        this.directTransfer = false;
        this.accessWithoutModel = true;
        this.currentModel = null;
        this.productGroupEnumValue = "";
        this.valuesChanged = false;
        this.isViewing = false;
        this.twoGridValues = new TwoGridValues();
        this.eventOnChangeListeners = new EventListeners();
        super.addEventListener(new EventListener(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                onSelectionChanged(event);
            }
        });
    }

    public TwoGridValues getTwoGridValues() {
        return twoGridValues;
    }

    public boolean isValuesChanged() {
        return valuesChanged;
    }

    public boolean isViewing() {
        return isViewing;
    }

    public void setViewing(boolean isViewing) {
        this.isViewing = isViewing;
    }

    public iPartsFilterGridForm.DIALOG_TYPES getDialogType() {
        return dialogType;
    }

    public boolean isDirectTransfer() {
        return directTransfer;
    }

    public void setDirectTransfer(boolean directTransfer) {
        this.directTransfer = directTransfer;
    }

    public boolean isAccessWithoutModel() {
        return accessWithoutModel;
    }

    public void setAccessWithoutModel(boolean accessWithoutModel) {
        this.accessWithoutModel = accessWithoutModel;
    }

    public iPartsModel getCurrentModel() {
        return currentModel;
    }

    public void setCurrentModel(iPartsModel currentModel) {
        this.currentModel = currentModel;
        if (true /*isDirectTransfer() && checkModelNo()*/) {
            switch (dialogType) {
                case SAA:
                    // SAAs
                    Set<String> saaList = currentModel.getSaas(parentForm.getConnector().getProject());
                    TwoGridValues values;
                    if (withUserDefinedValues) {
                        values = new TwoGridValues(null, this.twoGridValues.getBottomGridValues());
                    } else {
                        values = new TwoGridValues();
                    }
                    for (String saa : saaList) {
                        values.addSingleValue(saa, true);
                    }
                    setTwoGridValues(values);
                    break;
                case AGG_MODELS:
                    // Laut Confluence nur nach abhängigen Aggregaten suchen, wenn das Baumuster mit "C" beginnt
                    if (currentModel.getModelId().getModelNumber().startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR)) {
                        List<iPartsDataModelsAggs> modelsAggsList = iPartsDataModelsAggsList.loadDataModelsAggsListForModel(parentForm.getConnector().getProject(),
                                                                                                                            currentModel.getModelId().getModelNumber()).getAsList();
                        if (withUserDefinedValues) {
                            values = new TwoGridValues(null, this.twoGridValues.getBottomGridValues());
                        } else {
                            values = new TwoGridValues();
                        }
                        for (iPartsDataModelsAggs modelsAggs : modelsAggsList) {
                            values.addSingleValue(modelsAggs.getAsId().getAggregateModelNumber(), true);
                        }
                        setTwoGridValues(values);
                    } else {
                        if (withUserDefinedValues) {
                            values = new TwoGridValues(null, this.twoGridValues.getBottomGridValues());
                        } else {
                            values = new TwoGridValues();
                        }
                        setTwoGridValues(values);
                    }
                    break;
                case CODE:
                    // Code
                    values = new TwoGridValues(DaimlerCodes.getCodeSet(currentModel.getCodes()), null);
                    if (!currentModel.getAusfuehrungsArt().isEmpty()) {
                        values.addSingleValue(currentModel.getAusfuehrungsArt(), true);
                    }
                    if (withUserDefinedValues) {
                        if (!this.twoGridValues.getBottomGridValues().isEmpty()) {
                            for (TwoGridValues.ValueState valueState : this.twoGridValues.getBottomGridValues()) {
                                values.addSingleValue(valueState, false);
                            }
                        }
                    }
                    setTwoGridValues(values);
                    break;
            }
        } else {
            setText(StrUtils.stringListToString(this.twoGridValues.getAllCheckedValues(), ", "));
        }
    }

    public boolean checkModelNo() {
        return (currentModel != null) && currentModel.existsInDB();
    }

    public String getProductGroupEnumValue() {
        return productGroupEnumValue;
    }

    public void setProductGroupEnumValue(String productGroupEnumValue) {
        this.productGroupEnumValue = productGroupEnumValue;
    }

    public void clear() {
        setText("");
        twoGridValues = new TwoGridValues();
    }

    public boolean isEmpty() {
        return getTwoGridValues().isEmpty();
    }

    public void setTwoGridValues(TwoGridValues twoGridValues) {
        if (twoGridValues == null) {
            return;
        }
        if (this.twoGridValues.isEmpty()) {
            this.twoGridValues = twoGridValues;
        } else if (!this.twoGridValues.hasSameContent(twoGridValues) || (this.twoGridValues.hasSameContent(twoGridValues) && valuesChanged)) {
            this.twoGridValues = twoGridValues;
            valuesChanged = true;
        } else {
            valuesChanged = false;
        }
        setText(StrUtils.stringListToString(this.twoGridValues.getAllCheckedValues(), ", "));
    }

    /**
     * ALT-Button gedrückt
     *
     * @param event
     */
    private void onSelectionChanged(Event event) {
        if (accessWithoutModel || checkModelNo()) {
            iPartsModelId modelId;
            if (currentModel == null) {
                modelId = new iPartsModelId("");
            } else {
                modelId = currentModel.getModelId();
            }
            switch (dialogType) {
                case SAA:
                    setTwoGridValues(iPartsFilterGridForm.showSaaSelectionTwoGridDialog(parentForm.getConnector(), parentForm,
                                                                                        modelId, twoGridValues, isViewing()));
                    break;
                case AGG_MODELS:
                    setTwoGridValues(iPartsFilterGridForm.showAggModelsSelectionTwoGridDialog(parentForm.getConnector(), parentForm,
                                                                                              modelId, twoGridValues, isViewing()));
                    break;
                case CODE:
                    setTwoGridValues(iPartsFilterGridForm.showCodeSelectionTwoGridDialog(parentForm.getConnector(), parentForm,
                                                                                         productGroupEnumValue,
                                                                                         modelId, twoGridValues, isViewing()));
                    break;

            }
            fireChangeEvents();
        }
    }

    /**
     * Fügt einen Eventlistener hinzu
     *
     * @param eventListener den hinzuzufügenden Eventlistener
     */
    @Override
    public void addEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT)) {
            eventOnChangeListeners.addEventListener(eventListener);
        } else {
            super.addEventListener(eventListener);
        }
    }

    /**
     * Entferne den übergebenen Eventlistener
     */
    @Override
    public void removeEventListener(EventListener eventListener) {
        if (eventListener.getType().equals(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT)) {
            eventOnChangeListeners.removeEventListener(eventListener);
        } else {
            super.removeEventListener(eventListener);
        }
    }

    private void fireChangeEvents() {

        // Eigenen Kind-Thread starten, weil der aufrufende (Warte-)Thread ansonsten zwischendrin abgebrochen werden könnte,
        // was z.B. bei IO-Aktionen Exceptions verursacht
        Session.startChildThreadInSession(thread -> {
            if (eventOnChangeListeners.isActive()) {
                final List<EventListener> listeners = eventOnChangeListeners.getListeners(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT);
                if (listeners.size() > 0) {
                    Session.invokeThreadSafeInSession(() -> {
                        for (EventListener listener : listeners) {
                            listener.fire(new Event(GuiButtonTextField.BUTTON_ACTION_PERFORMED_EVENT));
                        }
                    });
                }
            }
        });
    }
}
