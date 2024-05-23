/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.DatacardIdentOrderTypes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.enums.EnumUtils;

import java.util.EnumSet;
import java.util.List;

/**
 * Dialog zur Festlegung einer Datenkarte
 */
public class iPartsFilterDataCardSelectDialog extends AbstractJavaViewerForm {

    public static AbstractDataCard showSelectDialog(AbstractJavaViewerForm parentForm) {
        iPartsFilterDataCardSelectDialog dlg = new iPartsFilterDataCardSelectDialog(parentForm.getConnector(), parentForm);
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getDataCard();
        }
        return null;
    }

    /**
     * Erzeugt eine Instanz von iPartsFilterDataCardSelectDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsFilterDataCardSelectDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        fillDataCardComboBox();
        mainWindow.pack();
        getComboboxDatacardType().setSelectedUserObject(DCAggregateTypes.VEHICLE);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public AbstractDataCard getDataCard() {
        AbstractDataCard result = null;
        DCAggregateTypes aggType = getSelectedDatacardType();
        if ((aggType != null) && (aggType != DCAggregateTypes.UNKNOWN)) {
            switch (aggType) {
                case VEHICLE:
                    result = new VehicleDataCard(true);
                    break;
                default:
                    AggregateDataCard aggregateDataCard = new AggregateDataCard(true);
                    DCAggregateTypeOf aggTypeOf = DCAggregateTypeOf.NONE;
                    if (getComboboxDatacardExtended().getSelectedIndex() >= 0) {
                        aggTypeOf = getSelectedAggregateTypeOf();
                    }
                    aggregateDataCard.setAggregateBasicType(aggType, aggTypeOf);
                    DatacardIdentOrderTypes selectedIdentType = getComboboxIdentType().getSelectedUserObject();
                    if ((selectedIdentType != null) && (selectedIdentType.isOldIdent())) {
                        aggregateDataCard.setOldIdentSpecification(true);
                    }
                    result = aggregateDataCard;
                    break;
            }
        }
        return result;
    }

    /**
     * Befüllt die {@link GuiComboBox} für den Datenkartentyp
     */
    private void fillDataCardComboBox() {
        getComboboxDatacardType().removeAllItems();
        getComboboxDatacardType().switchOffEventListeners();

        for (DCAggregateTypes aggType : DCAggregateTypes.values()) {
            if (aggType != DCAggregateTypes.UNKNOWN) {
                DatacardIdentOrderTypes identOrderType = DatacardIdentOrderTypes.getIdentOrderTypeByAggregateTypes(aggType);
                if ((identOrderType != DatacardIdentOrderTypes.UNKNOWN) && identOrderType.isVisible()) {
                    getComboboxDatacardType().addItem(aggType, aggType.getDescription());
                }
            }
        }
        getComboboxDatacardType().switchOnEventListeners();
    }

    /**
     * Befüllt die {@link GuiComboBox} für den Datenkarten Zusatztyp
     *
     * @param aggType
     * @param identOrderType
     */
    private void fillDatacardExtendedType(DCAggregateTypes aggType, DatacardIdentOrderTypes identOrderType) {
        getComboboxDatacardExtended().removeAllItems();
        getComboboxDatacardExtended().switchOffEventListeners();

        EnumSet<DCAggregateTypeOf> aggTypeOfs = EnumSet.noneOf(DCAggregateTypeOf.class);
        switch (aggType) {
            case VEHICLE:
                break;
            case ENGINE:
                break;
            case TRANSMISSION:
                break;
            case TRANSFER_CASE:
                break;
            case AXLE:
                aggTypeOfs = getAggsTypeOfForAxle(identOrderType);
                break;
            case CAB:
                break;
            case AFTER_TREATMENT_SYSTEM:
                break;
            case ELECTRO_ENGINE:
                aggTypeOfs = DCAggregateTypeOf.getElectroEngineAggregateTypes();
                break;
            case FUEL_CELL:
                aggTypeOfs = DCAggregateTypeOf.getFuelCellAggregateTypes();
                break;
            case HIGH_VOLTAGE_BATTERY:
                aggTypeOfs = DCAggregateTypeOf.getBatteryAggregateTypes();
                break;
            case STEERING:
                break;
            case EXHAUST_SYSTEM:
                break;
            case PLATFORM:
                break;

        }
        if (!aggTypeOfs.isEmpty()) {
            for (DCAggregateTypeOf aggTypeOf : aggTypeOfs) {
                getComboboxDatacardExtended().addItem(aggTypeOf, aggTypeOf.getDescription());
            }
        }
        getComboboxDatacardExtended().setEnabled(getComboboxDatacardExtended().getItemCount() > 0);
        getComboboxDatacardExtended().switchOnEventListeners();
    }

    /**
     * Gibt die Zusatztypen für "Achse" zurück
     *
     * @param identOrderType
     * @return
     */
    private EnumSet<DCAggregateTypeOf> getAggsTypeOfForAxle(DatacardIdentOrderTypes identOrderType) {
        switch (identOrderType) {
            case AXLE_FRONT_NEW:
            case AXLE_FRONT_OLD:
                return DCAggregateTypeOf.getFrontAggregateTypes();
            case AXLE_REAR_NEW:
            case AXLE_REAR_OLD:
                return DCAggregateTypeOf.getRearAggregateTypes();
            default:
                return EnumUtils.plus(DCAggregateTypeOf.getFrontAggregateTypes(), DCAggregateTypeOf.getRearAggregateTypes());
        }
    }

    /**
     * Befüllt die {@link GuiComboBox} für den Identtyp
     *
     * @param aggType
     */
    private void fillDatacardIdentType(DCAggregateTypes aggType) {
        getComboboxIdentType().removeAllItems();
        getComboboxIdentType().switchOffEventListeners();
        List<DatacardIdentOrderTypes> identOrderTypesList = new DwList<DatacardIdentOrderTypes>();
        switch (aggType) {
            case VEHICLE:
                break;
            case ENGINE:
                identOrderTypesList.add(DatacardIdentOrderTypes.ENGINE_NEW);
                identOrderTypesList.add(DatacardIdentOrderTypes.ENGINE_OLD);
                break;
            case TRANSMISSION:
                identOrderTypesList.add(DatacardIdentOrderTypes.TRANSMISSION);
                identOrderTypesList.add(DatacardIdentOrderTypes.TRANSMISSION_AUTOMATED);
                identOrderTypesList.add(DatacardIdentOrderTypes.TRANSMISSION_MECHANICAL);
                break;
            case TRANSFER_CASE:
                identOrderTypesList.add(DatacardIdentOrderTypes.TRANSFER_CASE);
                break;
            case AXLE:
                identOrderTypesList.add(DatacardIdentOrderTypes.AXLE_FRONT_NEW);
                identOrderTypesList.add(DatacardIdentOrderTypes.AXLE_FRONT_OLD);
                identOrderTypesList.add(DatacardIdentOrderTypes.AXLE_REAR_NEW);
                identOrderTypesList.add(DatacardIdentOrderTypes.AXLE_REAR_OLD);
                break;
            case CAB:
                break;
            case AFTER_TREATMENT_SYSTEM:
                break;
            case ELECTRO_ENGINE:
                identOrderTypesList.add(DatacardIdentOrderTypes.ELECTRO_ENGINE);
                break;
            case FUEL_CELL:
                identOrderTypesList.add(DatacardIdentOrderTypes.FUEL_CELL);
                break;
            case HIGH_VOLTAGE_BATTERY:
                identOrderTypesList.add(DatacardIdentOrderTypes.HIGH_VOLTAGE_BATTERY);
                break;
            case STEERING:
                break;
            case EXHAUST_SYSTEM:
                break;
            case PLATFORM:
                break;
        }
        for (DatacardIdentOrderTypes identOrderType : identOrderTypesList) {
            if ((identOrderType != DatacardIdentOrderTypes.UNKNOWN) && identOrderType.isVisible()) {
                getComboboxIdentType().addItem(identOrderType, getDescription(identOrderType));
            }
        }
        getComboboxIdentType().setEnabled(getComboboxIdentType().getItemCount() > 0);
        getComboboxIdentType().switchOnEventListeners();
    }

    /**
     * Gibt die Beschreibung für den übergebenen Identtyp zurück
     *
     * @param identOrderType
     * @return
     */
    private String getDescription(DatacardIdentOrderTypes identOrderType) {
        if (identOrderType != null) {
            switch (identOrderType) {
                case VIN:
                    return TranslationHandler.translate("!!VIN");
                case FIN:
                    return TranslationHandler.translate("!!FIN");
                default:
                    return getDescriptionForIdentOrderType(identOrderType);
            }
        }
        return "";
    }

    private String getDescriptionForIdentOrderType(DatacardIdentOrderTypes identOrderType) {
        String descr = getProject().getEnumText(iPartsConst.ENUM_KEY_AGGREGATE_TYPE,
                                                identOrderType.getDbValue(), getProject().getViewerLanguage(), true);
        switch (identOrderType.getSpecification()) {
            case 1:
                descr = TranslationHandler.translate("!!%1 (%2)", descr, TranslationHandler.translate("!!neu"));
                break;
            case 2:
                descr = TranslationHandler.translate("!!%1 (%2)", descr, TranslationHandler.translate("!!alt"));
                break;
        }
        return descr;
    }

    private void onDatacardTypeChanged(Event event) {
        DCAggregateTypes aggType = getSelectedDatacardType();
        fillDatacardExtendedType(aggType, DatacardIdentOrderTypes.UNKNOWN);
        fillDatacardIdentType(aggType);
        if (getComboboxIdentType().getItemCount() > 0) {
            getComboboxIdentType().setSelectedIndex(0);
        }
    }

    private void onDatacardExtendedChanged(Event event) {

    }

    private void onIdentTypeChanged(Event event) {
        fillDatacardExtendedType(getSelectedDatacardType(), getSelectedIdentType());
    }

    private GuiComboBox<DCAggregateTypes> getComboboxDatacardType() {
        return mainWindow.comboboxDatacardType;
    }

    private GuiComboBox<DCAggregateTypeOf> getComboboxDatacardExtended() {
        return mainWindow.comboboxDatacardExtended;
    }

    private GuiComboBox<DatacardIdentOrderTypes> getComboboxIdentType() {
        return mainWindow.comboboxIdentType;
    }

    private DCAggregateTypes getSelectedDatacardType() {
        return getComboboxDatacardType().getSelectedUserObject();
    }

    private DCAggregateTypeOf getSelectedAggregateTypeOf() {
        return getComboboxDatacardExtended().getSelectedUserObject();
    }

    private DatacardIdentOrderTypes getSelectedIdentType() {
        return getComboboxIdentType().getSelectedUserObject();
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDatacardType;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<DCAggregateTypes> comboboxDatacardType;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelIdentType;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<DatacardIdentOrderTypes> comboboxIdentType;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelDatacardExtended;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<DCAggregateTypeOf> comboboxDatacardExtended;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel label_0;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("!!Datenkarte auswählen");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            labelDatacardType = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDatacardType.setName("labelDatacardType");
            labelDatacardType.__internal_setGenerationDpi(96);
            labelDatacardType.registerTranslationHandler(translationHandler);
            labelDatacardType.setScaleForResolution(true);
            labelDatacardType.setMinimumWidth(10);
            labelDatacardType.setMinimumHeight(10);
            labelDatacardType.setText("!!Datenkarten Typ");
            labelDatacardType.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelDatacardTypeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "e", "n", 8, 8, 4, 4);
            labelDatacardType.setConstraints(labelDatacardTypeConstraints);
            panelMain.addChild(labelDatacardType);
            comboboxDatacardType = new de.docware.framework.modules.gui.controls.GuiComboBox<DCAggregateTypes>();
            comboboxDatacardType.setName("comboboxDatacardType");
            comboboxDatacardType.__internal_setGenerationDpi(96);
            comboboxDatacardType.registerTranslationHandler(translationHandler);
            comboboxDatacardType.setScaleForResolution(true);
            comboboxDatacardType.setMinimumWidth(10);
            comboboxDatacardType.setMinimumHeight(10);
            comboboxDatacardType.setMaximumRowCount(15);
            comboboxDatacardType.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onDatacardTypeChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxDatacardTypeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 8, 4, 4, 8);
            comboboxDatacardType.setConstraints(comboboxDatacardTypeConstraints);
            panelMain.addChild(comboboxDatacardType);
            labelIdentType = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelIdentType.setName("labelIdentType");
            labelIdentType.__internal_setGenerationDpi(96);
            labelIdentType.registerTranslationHandler(translationHandler);
            labelIdentType.setScaleForResolution(false);
            labelIdentType.setMinimumWidth(10);
            labelIdentType.setMinimumHeight(10);
            labelIdentType.setText("!!Ident Typ");
            labelIdentType.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelIdentTypeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 4, 4);
            labelIdentType.setConstraints(labelIdentTypeConstraints);
            panelMain.addChild(labelIdentType);
            comboboxIdentType = new de.docware.framework.modules.gui.controls.GuiComboBox<DatacardIdentOrderTypes>();
            comboboxIdentType.setName("comboboxIdentType");
            comboboxIdentType.__internal_setGenerationDpi(96);
            comboboxIdentType.registerTranslationHandler(translationHandler);
            comboboxIdentType.setScaleForResolution(true);
            comboboxIdentType.setMinimumWidth(10);
            comboboxIdentType.setMinimumHeight(10);
            comboboxIdentType.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onIdentTypeChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxIdentTypeConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 8);
            comboboxIdentType.setConstraints(comboboxIdentTypeConstraints);
            panelMain.addChild(comboboxIdentType);
            labelDatacardExtended = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelDatacardExtended.setName("labelDatacardExtended");
            labelDatacardExtended.__internal_setGenerationDpi(96);
            labelDatacardExtended.registerTranslationHandler(translationHandler);
            labelDatacardExtended.setScaleForResolution(true);
            labelDatacardExtended.setMinimumWidth(10);
            labelDatacardExtended.setMinimumHeight(10);
            labelDatacardExtended.setText("!!Datenkarten Zusatztyp");
            labelDatacardExtended.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelDatacardExtendedConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 8, 8, 4);
            labelDatacardExtended.setConstraints(labelDatacardExtendedConstraints);
            panelMain.addChild(labelDatacardExtended);
            comboboxDatacardExtended = new de.docware.framework.modules.gui.controls.GuiComboBox<DCAggregateTypeOf>();
            comboboxDatacardExtended.setName("comboboxDatacardExtended");
            comboboxDatacardExtended.__internal_setGenerationDpi(96);
            comboboxDatacardExtended.registerTranslationHandler(translationHandler);
            comboboxDatacardExtended.setScaleForResolution(true);
            comboboxDatacardExtended.setMinimumWidth(10);
            comboboxDatacardExtended.setMinimumHeight(10);
            comboboxDatacardExtended.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onDatacardExtendedChanged(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxDatacardExtendedConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 8, 8);
            comboboxDatacardExtended.setConstraints(comboboxDatacardExtendedConstraints);
            panelMain.addChild(comboboxDatacardExtended);
            label_0 = new de.docware.framework.modules.gui.controls.GuiLabel();
            label_0.setName("label_0");
            label_0.__internal_setGenerationDpi(96);
            label_0.registerTranslationHandler(translationHandler);
            label_0.setScaleForResolution(true);
            label_0.setMinimumWidth(10);
            label_0.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag label_0Constraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 0.0, 100.0, "c", "n", 0, 0, 0, 0);
            label_0.setConstraints(label_0Constraints);
            panelMain.addChild(label_0);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}