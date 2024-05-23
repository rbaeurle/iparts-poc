/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.list.gui.RList;

public class iPartsDataCardDialog extends AbstractJavaViewerForm {

    public static String VEHICLE_NAME = "!!Fahrzeug";
    public static String AGGREGATE_NAME = "!!Aggregat";

    private RList<AbstractDataCard> listDataCard;
    private iPartsAdminFilterValuesPanel filterValuesPanel;  // für die Anzeige der Inhalte
    protected AbstractDataCard dataCard;

    public static String buildVehicleItemString(VehicleDataCard vehicleDataCard) {
        String itemString = TranslationHandler.translate(VEHICLE_NAME);
        if (!vehicleDataCard.getFinId().isValidId()) {
            if (vehicleDataCard.isModelLoaded()) {
                itemString = TranslationHandler.translate("!!%1 (%2)", itemString, TranslationHandler.translate("!!aus Baumuster"));
            } else {
                itemString = TranslationHandler.translate("!!%1 (%2)", itemString, TranslationHandler.translate("!!virtuell"));
            }
        }
        return itemString;
    }

    public static String buildAggregateItemString(EtkProject project, AggregateDataCard aggregateDataCard) {
        String aggregateType;
        if (aggregateDataCard.getAggregateType() == DCAggregateTypes.UNKNOWN) {
            aggregateType = TranslationHandler.translate(AGGREGATE_NAME);
        } else {
            aggregateType = TranslationHandler.translate(aggregateDataCard.getAggregateType().getDescription());
        }
        String itemString;
        if (aggregateDataCard.getAggregateIdent().isEmpty()) {
            if (aggregateDataCard.isModelLoaded()) {
                itemString = TranslationHandler.translate("!!%1 (%2)", aggregateType, TranslationHandler.translate("!!aus Baumuster"));
            } else {
                itemString = TranslationHandler.translate("!!%1 %2",
                                                          aggregateType,
                                                          TranslationHandler.translate(aggregateDataCard.getAggregateTypeOf().getDescription())).trim();
            }
        } else {
            AggregateIdent ident = null;
            switch (aggregateDataCard.getAggregateType()) {
                case ENGINE:
                    EngineIdent engineIdent = new EngineIdent(project, aggregateDataCard.getAggregateIdent());
                    if (aggregateDataCard.isOldIdentSpecification()) {
                        engineIdent.setIdentSpecificationOld();
                    } else {
                        engineIdent.setIdentSpecificationNew();
                    }
                    ident = engineIdent;
                    break;
                case TRANSMISSION:
                case TRANSFER_CASE:
                    ident = new TransmissionIdent(project, aggregateDataCard.getAggregateIdent());
                    break;
                case AXLE:
                    ident = new AxleIdent(project, aggregateDataCard.getAggregateIdent());
                    break;
                case CAB:
                    ident = new CabIdent(project, aggregateDataCard.getAggregateIdent());
                    break;
                case AFTER_TREATMENT_SYSTEM:
                    ident = new ATSIdent(project, aggregateDataCard.getAggregateIdent());
                    break;
                case STEERING:
                    ident = new SteeringIdent(project, aggregateDataCard.getAggregateIdent());
                    break;
                case ELECTRO_ENGINE:
                    ident = new ElectroEngineIdent(project, aggregateDataCard.getAggregateIdent());
                    break;
                case HIGH_VOLTAGE_BATTERY:
                    ident = new HighVoltageBatIdent(project, aggregateDataCard.getAggregateIdent());
                    break;
                case FUEL_CELL:
                    ident = new FuelCellIdent(project, aggregateDataCard.getAggregateIdent());
                    break;
                case EXHAUST_SYSTEM:
                case PLATFORM:
                    break;
                default:
                    break;
            }
            String extra = "";
            if ((ident != null) && ident.isExchangeAggregate()) {
                extra = (aggregateDataCard.getAggregateType() == DCAggregateTypes.ENGINE) ?
                        TranslationHandler.translate(DCAggregateTypes.EXCHANGE_ENGINE) : TranslationHandler.translate(DCAggregateTypes.EXCHANGE_TRANSMISSION);
                extra = "(" + extra + ")";
            }
            itemString = TranslationHandler.translate("!!%1 %2 %3",
                                                      aggregateType,
                                                      TranslationHandler.translate(aggregateDataCard.getAggregateTypeOf().getDescription()),
                                                      extra).trim();
        }
        return itemString;
    }

    /**
     * Erzeugt eine Instanz von iPartsDataCardMasterDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsDataCardDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                int dividerPosition) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
        initDialog();
        mainWindow.splitpane.setDividerPosition(dividerPosition);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        listDataCard = RList.replaceGuiList(mainWindow.listDatacard);
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
        mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setText("!!Übernehmen");

        filterValuesPanel = new iPartsAdminFilterValuesPanel(getConnector(), this);
        ConstraintsBorder panelLeftConstraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        filterValuesPanel.getGui().setConstraints(panelLeftConstraints);
        filterValuesPanel.setViewing(true);
        mainWindow.splitpane_secondChild.addChild(filterValuesPanel.getGui());

        // Optimale Breite und Höhe berechnen
        mainWindow.setWidth(mainWindow.splitpane_firstChild.getPreferredWidth() + filterValuesPanel.getGui().getPreferredWidth());
        mainWindow.setHeight(mainWindow.title.getPreferredHeight() + filterValuesPanel.getGui().getPreferredHeight() + mainWindow.buttonpanel.getPreferredHeight());
    }

    public AbstractDataCard getDataCard() {
        return listDataCard.getSelectedUserObject();
    }

    public void setDataCard(AbstractDataCard dataCard) {
        listDataCard.removeAllItems();
        if (dataCard != null) {
            listDataCard.switchOffEventListeners();
            if (dataCard.isVehicleDataCard()) {
                VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                //initDialog(vehicleDataCard);
                addVehicleDataCard(vehicleDataCard);
                for (AggregateDataCard aggregateDataCard : vehicleDataCard.getActiveAggregates()) {
                    addAggregateDataCard(aggregateDataCard);
                }
            } else {
                AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCard;
                //initDialog(aggregateDataCard);
                addAggregateDataCard(aggregateDataCard);
            }
            listDataCard.switchOnEventListeners();
            listDataCard.setSelectedIndex(0);
        } else {
            initDialog();
        }
        this.dataCard = dataCard;
    }

    private void initDialog() {
        filterValuesPanel.initDialog();
    }

    private void initDialog(VehicleDataCard vehicleDataCard) {
        mainWindow.splitpane_secondChild.setTitle(buildVehicleItemString(vehicleDataCard));
        filterValuesPanel.initDialog(vehicleDataCard);
    }

    private void initDialog(AggregateDataCard aggregateDataCard) {
        mainWindow.splitpane_secondChild.setTitle(buildAggregateItemString(getProject(), aggregateDataCard));
        filterValuesPanel.initDialog(aggregateDataCard);
    }

    private void addVehicleDataCard(VehicleDataCard vehicleDataCard) {
        listDataCard.addItem(vehicleDataCard, buildVehicleItemString(vehicleDataCard));
    }

    private void addAggregateDataCard(AggregateDataCard aggregateDataCard) {
        listDataCard.addItem(aggregateDataCard, buildAggregateItemString(getProject(), aggregateDataCard));
    }

    public ModalResult showModal() {
        return mainWindow.showModal();
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    private void onButtonOkClick(Event event) {
        mainWindow.setModalResult(ModalResult.OK);
        close();
    }

    private void onChangeEvent(Event event) {
        AbstractDataCard selectedDataCard = listDataCard.getSelectedUserObject();
        if (selectedDataCard != null) {
            if (selectedDataCard.isVehicleDataCard()) {
                VehicleDataCard vehicleDataCard = (VehicleDataCard)selectedDataCard;
                initDialog(vehicleDataCard);
            } else {
                AggregateDataCard aggregateDataCard = (AggregateDataCard)selectedDataCard;
                initDialog(aggregateDataCard);
            }
        }
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
        private de.docware.framework.modules.gui.controls.GuiSplitPane splitpane;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_firstChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiList<AbstractDataCard> listDatacard;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(900);
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
            title.setTitle("!!Datenkarte");
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
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            splitpane = new de.docware.framework.modules.gui.controls.GuiSplitPane();
            splitpane.setName("splitpane");
            splitpane.__internal_setGenerationDpi(96);
            splitpane.registerTranslationHandler(translationHandler);
            splitpane.setScaleForResolution(true);
            splitpane.setMinimumWidth(10);
            splitpane.setMinimumHeight(10);
            splitpane.setDividerPosition(207);
            splitpane_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_firstChild.setName("splitpane_firstChild");
            splitpane_firstChild.__internal_setGenerationDpi(96);
            splitpane_firstChild.registerTranslationHandler(translationHandler);
            splitpane_firstChild.setScaleForResolution(true);
            splitpane_firstChild.setMinimumWidth(0);
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_firstChild.setLayout(splitpane_firstChildLayout);
            listDatacard = new de.docware.framework.modules.gui.controls.GuiList<AbstractDataCard>();
            listDatacard.setName("listDatacard");
            listDatacard.__internal_setGenerationDpi(96);
            listDatacard.registerTranslationHandler(translationHandler);
            listDatacard.setScaleForResolution(true);
            listDatacard.setMinimumWidth(200);
            listDatacard.setMinimumHeight(100);
            listDatacard.setPaddingTop(4);
            listDatacard.setPaddingLeft(4);
            listDatacard.setPaddingRight(4);
            listDatacard.setPaddingBottom(4);
            listDatacard.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onChangeEvent(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder listDatacardConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            listDatacard.setConstraints(listDatacardConstraints);
            splitpane_firstChild.addChild(listDatacard);
            splitpane.addChild(splitpane_firstChild);
            splitpane_secondChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_secondChild.setName("splitpane_secondChild");
            splitpane_secondChild.__internal_setGenerationDpi(96);
            splitpane_secondChild.registerTranslationHandler(translationHandler);
            splitpane_secondChild.setScaleForResolution(true);
            splitpane_secondChild.setMinimumWidth(0);
            splitpane_secondChild.setTitle("!!Fahrzeug");
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_secondChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_secondChild.setLayout(splitpane_secondChildLayout);
            splitpane.addChild(splitpane_secondChild);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder splitpaneConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            splitpane.setConstraints(splitpaneConstraints);
            panelMain.addChild(splitpane);
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
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonOkClick(event);
                }
            });
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