/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.common.EditControlForArraysInterface;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.gui.controls.GuiButtonGroup;
import de.docware.framework.modules.gui.controls.GuiButtonTextField;
import de.docware.framework.modules.gui.controls.GuiRadioButton;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventCreator;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.utils.EtkDataArray;
import de.docware.util.StrUtils;
import de.docware.util.Utils;

import java.util.Collection;

/**
 * Abstrakte Klasse für die Textfelder mit denen Arraywerte ausgewählt werden können
 */
public abstract class iPartsGuiArraySelectionTextField extends GuiButtonTextField implements EditControlForArraysInterface {

    protected EtkDataArray dataArray = new EtkDataArray();
    private int maxTextLength = 100;
    protected iPartsProductId productId;
    protected EtkProject project;
    private boolean withRadioButtons = false;
    private iPartsGuiArraySelectionTextFieldWithRadioButtons selectionTextFieldWithRadioButtons;

    public iPartsGuiArraySelectionTextField(EtkProject project, String type) {
        this.project = project;
        setType(type);
        super.setEditable(false);
    }

    /**
     * Hier werden die RadioButtons hinzugefügt, falls sie gewünscht sind
     *
     * @param textRadioButtonInitialSelected
     * @param textRadioButtonInitialUnselected
     */
    public void addRadioButtons(String textRadioButtonInitialSelected, String textRadioButtonInitialUnselected) {
        if (withRadioButtons) {
            selectionTextFieldWithRadioButtons =
                    new iPartsGuiArraySelectionTextFieldWithRadioButtons(textRadioButtonInitialSelected,
                                                                         textRadioButtonInitialUnselected,
                                                                         true);
        }
    }

    @Override
    public void setEditable(boolean isEditable) {
        this.getButton().setEnabled(isEditable);
    }

    /**
     * @param dataArray
     * @return false: kein ChangeEvent gefeuert
     */
    @Override
    public boolean setArray(EtkDataArray dataArray) {
        if (dataArray == null) {
            dataArray = new EtkDataArray();
        }
        if (Utils.objectEquals(this.dataArray, dataArray)) {
            return false;
        }

        this.dataArray = dataArray;

        String newText = StrUtils.makeAbbreviation(getArrayAsFormattedString(), maxTextLength);
        if (!newText.equals(getText())) {
            setText(newText);
            return false;
        } else {
            // Der Text im Textfeld bleibt durch makeAbbreviation() unverändert, aber das Array hat sich geändert
            // -> explizit einen ChangeEvent feuern
            fireEvent(EventCreator.createOnChangeEvent(eventHandlerComponent, uniqueId));
            return true;
        }
    }

    @Override
    public EtkDataArray getArray() {
        return dataArray;
    }

    public void addDataArrayFromSelection(Collection<String> selection) {
        String arrayId = dataArray.getArrayId();
        EtkDataArray newDataArray = new EtkDataArray(arrayId);
        newDataArray.add(selection);
        setArray(newDataArray);
        if (selectionTextFieldWithRadioButtons != null) {
            selectionTextFieldWithRadioButtons.setDataForSelectedRadioButton(newDataArray);
        }
    }

    public boolean isFirstRadioButtonSelected() {
        if (selectionTextFieldWithRadioButtons != null) {
            return selectionTextFieldWithRadioButtons.getFirstRadioButton().isSelected();
        }
        return false;
    }

    public boolean isSecondRadioButtonSelected() {
        if (selectionTextFieldWithRadioButtons != null) {
            return selectionTextFieldWithRadioButtons.getSecondRadioButton().isSelected();
        }
        return false;
    }

    /**
     * Liefrt die {@link iPartsProductId} zurück für eine Baumusterauswahl mit allen verfügbaren Baumustern zu diesem Produkt.
     *
     * @return
     */
    public iPartsProductId getProductId() {
        return productId;
    }

    /**
     * Setzt die {@link iPartsProductId} für eine Baumusterauswahl mit allen verfügbaren Baumustern zu diesem Produkt.
     *
     * @param productId
     */
    public void setProductId(iPartsProductId productId) {
        this.productId = productId;
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    public boolean isWithRadioButtons() {
        return withRadioButtons;
    }

    public void setWithRadioButtons(boolean withRadioButtons) {
        this.withRadioButtons = withRadioButtons;
    }

    /**
     * Liefert einen formatierten String für das {@link EtkDataArray} der aktuellen Auswahl zurück.
     *
     * @return
     */
    protected abstract String getArrayAsFormattedString();


    /**
     * Klasse mit der {@link iPartsGuiArraySelectionTextField} um zwei radioButtons erweitert werden kann
     * Zu jedem RadioButton wird sich der ausgewählte Datensatz im Textfeld gemerkt
     * Es bleibt immer nur ein RadioButton aktiviert
     */
    private class iPartsGuiArraySelectionTextFieldWithRadioButtons {

        private RadioButtonWithArray firstRadioButtonArray;
        private RadioButtonWithArray secondRadioButtonArray;

        public iPartsGuiArraySelectionTextFieldWithRadioButtons(String radioButtonATitle, String radioButtonBTitle,
                                                                boolean selectFirstButton) {
            addRadioButtonControls(radioButtonATitle, radioButtonBTitle, selectFirstButton);
            addEventsForRadioButton();

            // Für den initial selektierten Datensatz müssen die selektierten Daten gespeichert werden
            if (selectFirstButton) {
                setDataForFirstRadioButton(getArray());
            } else {
                setDataForSecondRadioButton(getArray());
            }
        }

        private void addRadioButtonControls(String text1, String text2, boolean selectFirstButton) {
            GuiButtonGroup radioGroup = new GuiButtonGroup();
            radioGroup.setGroupName("ButtonGroupSelected");
            firstRadioButtonArray = new RadioButtonWithArray(text1, selectFirstButton);
            GuiRadioButton radioButton = firstRadioButtonArray.getRadioButton();
            radioButton.setButtonGroup(radioGroup);
            addChildGridBag(radioButton, 4, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE,
                            0, 5, 0, 0);


            secondRadioButtonArray = new RadioButtonWithArray(text2, !selectFirstButton);
            radioButton = secondRadioButtonArray.getRadioButton();
            radioButton.setButtonGroup(radioGroup);
            addChildGridBag(radioButton, 5, 0, 1, 1, 0.0, 0.0, ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_NONE,
                            0, 5, 0, 0);
        }


        private void createAndAddEventListenerForRadioButton(final RadioButtonWithArray radioButtonArray) {
            getRadioButton(radioButtonArray).addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {

                @Override
                public void fire(Event event) {
                    iPartsGuiArraySelectionTextField.this.switchOffEventListeners();
                    setArray(radioButtonArray.getRadioButtonData());
                    iPartsGuiArraySelectionTextField.this.switchOnEventListeners();
                }
            });
        }

        private void addEventsForRadioButton() {
            createAndAddEventListenerForRadioButton(firstRadioButtonArray);
            createAndAddEventListenerForRadioButton(secondRadioButtonArray);
        }

        private GuiRadioButton getRadioButton(RadioButtonWithArray radioButtonWithArray) {
            return radioButtonWithArray.getRadioButton();
        }

        private EtkDataArray getDataFromRadioButton(RadioButtonWithArray radioButtonWithArray) {
            return radioButtonWithArray.getRadioButtonData();
        }

        private void setDataForRadioButton(RadioButtonWithArray radioButtonWithArray, EtkDataArray dataArray) {
            radioButtonWithArray.setRadioButtonData(dataArray);
        }

        public EtkDataArray getDataFromFirstRadioButton() {
            return getDataFromRadioButton(firstRadioButtonArray);
        }

        public void setDataForFirstRadioButton(EtkDataArray dataForRadioButtonInitialSelected) {
            setDataForRadioButton(firstRadioButtonArray, dataForRadioButtonInitialSelected);
        }

        public EtkDataArray getDataFromSecondRadioButton() {
            return getDataFromRadioButton(secondRadioButtonArray);
        }

        public void setDataForSecondRadioButton(EtkDataArray dataForRadioButtonInitialUnselected) {
            setDataForRadioButton(secondRadioButtonArray, dataForRadioButtonInitialUnselected);
        }

        public void setDataForSelectedRadioButton(EtkDataArray dataForRadioButton) {
            if (firstRadioButtonArray.isSelected()) {
                firstRadioButtonArray.setRadioButtonData(dataForRadioButton);
            } else if (secondRadioButtonArray.isSelected()) {
                secondRadioButtonArray.setRadioButtonData(dataForRadioButton);
            }
        }

        public GuiRadioButton getFirstRadioButton() {
            return getRadioButton(firstRadioButtonArray);
        }

        public GuiRadioButton getSecondRadioButton() {
            return getRadioButton(secondRadioButtonArray);
        }
    }

    private class RadioButtonWithArray {

        private GuiRadioButton radioButton;
        private EtkDataArray dataForRadioButton;

        public RadioButtonWithArray(String text, boolean selected) {
            radioButton = new GuiRadioButton(text, selected);
            dataForRadioButton = new EtkDataArray();
        }

        public GuiRadioButton getRadioButton() {
            return radioButton;
        }

        public boolean isSelected() {
            return radioButton.isSelected();
        }

        public EtkDataArray getRadioButtonData() {
            return dataForRadioButton;
        }

        public void setRadioButtonData(EtkDataArray dataArry) {
            dataForRadioButton = dataArry;
        }
    }
}
