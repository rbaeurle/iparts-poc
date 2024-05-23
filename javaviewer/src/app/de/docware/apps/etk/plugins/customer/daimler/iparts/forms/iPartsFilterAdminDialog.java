/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.forms.events.OnEnableButtonsEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TwoGridValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.list.gui.RList;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;
import java.util.Set;

public class iPartsFilterAdminDialog extends AbstractJavaViewerForm {

    public static void showFilterAdminDialog(AbstractJavaViewerForm parentForm) {
        iPartsFilterAdminDialog dlg = new iPartsFilterAdminDialog(parentForm.getConnector(), parentForm);
        dlg.showModal();
    }

    private RList<AbstractDataCard> listDataCard;
    private ToolbarButtonMenuHelper toolbarHelper;
    private iPartsAdminFilterValuesPanel filterValuesPanel;
    private OnChangeEvent onLoadVehicleDataCardEventMarker;
    private OnChangeEvent onLoadAggregateDataCardEventMarker;
    private OnEnableButtonsEvent onEnableButtonsEventMarker;

    private int lastSelectedIndex = -1;
    private int lastSelectedIndexBeforeLoad = -1;

    /**
     * Erzeugt eine Instanz von iPartsFilterAdminDialog.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsFilterAdminDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        this(dataConnector, parentForm, null, null, null);
    }

    public iPartsFilterAdminDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                   OnChangeEvent onLoadVehicleDataCardEvent, OnChangeEvent onLoadAggregateDataCardEvent,
                                   OnEnableButtonsEvent onEnableButtonsEvent) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.onLoadVehicleDataCardEventMarker = onLoadVehicleDataCardEvent;
        this.onLoadAggregateDataCardEventMarker = onLoadAggregateDataCardEvent;
        this.onEnableButtonsEventMarker = onEnableButtonsEvent;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        listDataCard = RList.replaceGuiList(mainWindow.listDatacard);
        toolbarHelper = new ToolbarButtonMenuHelper(getConnector(), mainWindow.toolbarDatacard);
        toolbarManager = toolbarHelper.getToolbarManager();
        addToolbarButtonsAndContextMenuItems(toolbarHelper, contextmenu);

        OnChangeEvent onLoadVehicleDataCardEvent = new OnChangeEvent() {
            @Override
            public void onChange() {
                if (onLoadVehicleDataCardEventMarker != null) {
                    onLoadVehicleDataCardEventMarker.onChange();
                }
            }
        };
        OnChangeEvent onLoadAggregateDataCardEvent = new OnChangeEvent() {
            @Override
            public void onChange() {
                if (onLoadAggregateDataCardEventMarker != null) {
                    lastSelectedIndexBeforeLoad = listDataCard.getSelectedIndex();
                    onLoadAggregateDataCardEventMarker.onChange();
                    lastSelectedIndexBeforeLoad = -1;
                }
            }
        };
        OnEnableButtonsEvent onEnableButtonsEvent = new OnEnableButtonsEvent() {
            @Override
            public void doEnableButtons() {
                if (onEnableButtonsEventMarker != null) {
                    onEnableButtonsEventMarker.doEnableButtons();
                }
                refreshSpecValidity();
            }
        };
        filterValuesPanel = new iPartsAdminFilterValuesPanel(getConnector(), this, onLoadVehicleDataCardEvent,
                                                             onLoadAggregateDataCardEvent, onEnableButtonsEvent);
        ConstraintsBorder panelLeftConstraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        filterValuesPanel.getGui().setConstraints(panelLeftConstraints);
        filterValuesPanel.setViewing(false);
        mainWindow.splitpane_secondChild.addChild(filterValuesPanel.getGui());
        //todo: DAIMLER-3592 Breite des Filterdialogs muss dynamisch berechnet werden (fontspezifisch)
        mainWindow.setWidth(820);
    }

    public AbstractGuiControl getFocusComponent() {
        return filterValuesPanel.getFocusComponent();
    }

    private void addToolbarButtonsAndContextMenuItems(ToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {

        ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
        holder = toolbarHelper.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.EDIT_NEW, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doAddDatacard(event);
            }
        });
        contextMenu.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.EDIT_DELETE, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doDelDatacard(event);
            }
        });
        contextMenu.addChild(holder.menuItem);

        holder = toolbarHelper.addToolbarButtonAndCreateMenu(iPartsToolbarButtonAlias.EDIT_DELETEALL, getUITranslationHandler(), new MenuRunnable() {
            public void run(Event event) {
                doDelAllDatacards(event);
            }
        });
        contextMenu.addChild(holder.menuItem);

        listDataCard.setContextMenu(contextMenu);
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

    public int getDatacardsCount() {
        return listDataCard.getItemCount();
    }

    public List<AbstractDataCard> getDatacards() {
        return listDataCard.getUserObjects();
    }

    public AbstractDataCard getSelectedDatacard() {
        int index = listDataCard.getSelectedIndex();
        if (index >= 0) {
            return listDataCard.getUserObject(index);
        }
        return null;
    }

    public void saveValuesOfLastSelectedDatacard() {
        int index = listDataCard.getSelectedIndex();
        if (index >= 0) {
            AbstractDataCard dataCard = listDataCard.getUserObject(index);
            dataCard = guiToDataCard(dataCard);
            listDataCard.setUserObject(index, dataCard);
        }
    }

    public void clearDatacards() {
        listDataCard.removeAllItems();
        lastSelectedIndex = -1;
        filterValuesPanel.initDialog();
    }

    public void initDialog(AbstractDataCard dataCard) {
        initDialog(dataCard, false);
    }

    public void initDialog(AbstractDataCard dataCard, boolean resetDataCard) {
        if (dataCard != null) {
            if (dataCard.isVehicleDataCard()) {
                AbstractDataCard selectedDatacard = null;
                int index = listDataCard.getSelectedIndex();
                if (index >= 0) {
                    selectedDatacard = listDataCard.getUserObject(index);
                    if (!selectedDatacard.isVehicleDataCard()) {
                        selectedDatacard = null;
                    }

                }
                listDataCard.switchOffEventListeners();
                if (selectedDatacard != null) {
                    listDataCard.setUserObject(index, dataCard);
                } else {
                    listDataCard.addItem(dataCard, getDataCardDescription(dataCard));

                    //addDatacardToList(dataCard, false);
                    index = listDataCard.getItemCount() - 1;
                }

                VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                for (AggregateDataCard aggregateDataCard : vehicleDataCard.getActiveAggregates()) {
                    int searchIndex = findDatacard(aggregateDataCard);
                    if (searchIndex >= 0) {
                        listDataCard.setUserObject(searchIndex, aggregateDataCard);
                    } else {
                        addDatacardToList(aggregateDataCard, false);
                    }
                }
                lastSelectedIndex = -1;
                listDataCard.setSelectedIndex(index);
                listDataCard.switchOnEventListeners();
                refreshDataCardList();
            } else {
                listDataCard.switchOffEventListeners();
                int searchIndex = lastSelectedIndexBeforeLoad;
                if (searchIndex < 0) {
                    searchIndex = findDatacard(dataCard);
                }
                if (searchIndex >= 0) {
                    listDataCard.setUserObject(searchIndex, dataCard);
                } else {
                    addDatacardToList(dataCard, false);
                    searchIndex = listDataCard.getItemCount() - 1;
                }
                lastSelectedIndex = -1;
                listDataCard.setSelectedIndex(searchIndex);
                listDataCard.switchOnEventListeners();
                refreshDataCardList();
            }
        } else {
            filterValuesPanel.clearDialog(true, resetDataCard);
        }
    }

    // Methoden aus filterValuesPanel
    public String getModelNo() {
        return filterValuesPanel.getModelNo();
    }

    public AggregateIdent getAggregateIdent() {
        return filterValuesPanel.getAggregateIdent();
    }

    public TwoGridValues getDatacardSaaNumbers() {
        return filterValuesPanel.getDatacardSaaNumbers();
    }

    public TwoGridValues getCodes() {
        return filterValuesPanel.getCodes();
    }

    /**
     * Liefert den ausgewählten Event-Wert aus dem Filter-Dialog
     *
     * @return
     */
    public iPartsEvent getEvent() {
        return filterValuesPanel.getEvent();
    }

    public String getSteeringValue() {
        return filterValuesPanel.getSteeringValue();
    }

    public String getGearboxValue() {
        return filterValuesPanel.getGearboxValue();
    }

    public iPartsAdminFilterValuesPanel.SpringFields getSpringFields() {
        return filterValuesPanel.getSpringFields();
    }

    public TwoGridValues getAggModelsNumbers() {
        return filterValuesPanel.getAggModelsNumbers();
    }

    public boolean isAutoSelectedProductsPSK() {
        return filterValuesPanel.isAutoSelectedProductsPSK();
    }

    public Set<String> getPSKVariants() {
        return filterValuesPanel.getPSKVariants();
    }

    public Set<String> getSpecValidity() {
        return filterValuesPanel.getSpecValidity();
    }

    public String getCountry() {
        return filterValuesPanel.getCountry();
    }

    public boolean isLoadedDataCardValid() {
        return filterValuesPanel.isLoadedDataCardValid();
    }

    public boolean isVehicleDatacardLoaded() {
        return filterValuesPanel.isVehicleDatacardLoaded();
    }

    public void setLastLoadedFin(FinId lastFinId) {
        filterValuesPanel.setLastLoadedDatacardNumber(lastFinId);
    }

    public boolean checkModelNo() {
        return filterValuesPanel.checkModelNo();
    }

    public void setIgnoreAggIdentChangeEvent(boolean ignoreChangeEvents) {
        filterValuesPanel.setIgnoreAggIdentChangeEvent(ignoreChangeEvents);
    }

    public VinId getVinId() {
        return filterValuesPanel.getVinId();
    }

    public FinId getFinId() {
        return filterValuesPanel.getFinIdFromIdentOrderElement();
    }

    public DatacardIdentOrderTypes getAggregateType() {
        return filterValuesPanel.getAggregateType();
    }

    // Methoden aus filterValuesPanel Ende


    public AbstractDataCard getFilledDataCard() {
        List<String> warnings = new DwList<String>();
        List<String> errors = new DwList<String>();
        if (checkDataCards(warnings, errors)) {
            List<AbstractDataCard> dataCardList = getAllNonEmptyDataCards();
            VehicleDataCard vehicleDataCard = null;
            // befindet sich eine Fahrzeugdatenkarte in der NonEmpty-Liste?
            int vehicleIndex = getIndexOfVehicleDataCard(dataCardList);
            if (vehicleIndex < 0) {
                // gibt es eine leere Fahrzeugdatenkarte?
                vehicleIndex = getIndexOfVehicleDataCard(null);
                if (vehicleIndex >= 0) {
                    // festhalten
                    vehicleDataCard = (VehicleDataCard)listDataCard.getUserObjects().get(vehicleIndex);
                    vehicleIndex = -1;
                }
            } else {
                // Fahrzeugdatenkarte in der NonEmpty-Liste
                vehicleDataCard = (VehicleDataCard)dataCardList.get(vehicleIndex);
            }
            if (vehicleDataCard != null) {
                // Vehicle-DataCard vorbereiten
                vehicleDataCard = addAllAggregateDataCards(vehicleDataCard, vehicleIndex, dataCardList);
                return vehicleDataCard;
            } else {
                // Aggregate-DataCard zurückliefern
                if (dataCardList.isEmpty()) {
                    return listDataCard.getUserObjects().get(0);
                } else {
                    AbstractDataCard aggregateDataCard = dataCardList.get(0);
                    if (aggregateDataCard.isAggregateDataCard()) {
                        // Prüfen ob für den Ident die alt/neu Systematik mit der an den Produkten hinterlegten übereinstimmt
                        boolean identChanged = ((AggregateDataCard)aggregateDataCard).verifyIdentOldNew(getProject(), false);
                        if (identChanged) {
                            setDatacardValuesForIdent(getProject(), ((AggregateDataCard)aggregateDataCard));
                        }
                    }
                    return aggregateDataCard;
                }
            }
        }
        return null;
    }

    /**
     * Setzt Werte in der übergebenen Datenkarte je nach Ident
     * Das wird z.B. gebraucht wenn die Datenkarte angepasst wird, weil die falsche Ident-Systematik (Alt/Neu)
     * angegeben wurde.
     *
     * @param project
     * @param dataCard die zu ändernde Datenkarte
     */
    public void setDatacardValuesForIdent(EtkProject project, AggregateDataCard dataCard) {
        if (dataCard.getAggregateIdent().isEmpty() || (dataCard.getAggregateType() == DCAggregateTypes.UNKNOWN)) {
            return;
        }
        switch (dataCard.getAggregateType()) {
            case ENGINE:
                EngineIdent engineIdent = new EngineIdent(project, dataCard.getAggregateIdent());
                if (dataCard.isOldIdentSpecification()) {
                    engineIdent.setIdentSpecificationOld();
                } else {
                    engineIdent.setIdentSpecificationNew();
                }
                dataCard.setSteeringValue(engineIdent.getSteeringEnumKey());
                dataCard.setGearboxValue(engineIdent.getTransmissionEnumKey());

                // Werksnummer über die WMI-Mapping-Tabelle aus dem Werkskennbuchstaben ermitteln
                String factorySign = engineIdent.getFactorySign();
                String engineDBType = DatacardIdentOrderTypes.ENGINE_NEW.getDbValue();
                String factoryNumber = iPartsFactoryModel.getInstance(getProject()).getFactoryNumberForAggregate(engineDBType,
                                                                                                                 factorySign,
                                                                                                                 new iPartsModelId(dataCard.getModelNo()));
                dataCard.setFactoryNumber(factoryNumber);
                break;
            case TRANSMISSION:
            case TRANSFER_CASE:
            case AXLE:
            case CAB:
            case AFTER_TREATMENT_SYSTEM:
            case STEERING:
            case EXHAUST_SYSTEM:
            case PLATFORM:
            case HIGH_VOLTAGE_BATTERY:
            case ELECTRO_ENGINE:
            case FUEL_CELL:
                break;
        }
    }

    public boolean checkDataCards(List<String> warnings, List<String> errors) {
        warnings.clear();
        errors.clear();
        int index = listDataCard.getSelectedIndex();
        if (index >= 0) {
            AbstractDataCard dataCard = guiToDataCard(listDataCard.getUserObject(index));
            listDataCard.setUserObject(index, dataCard);
        } else if (getDatacardsCount() == 0) {
            AbstractDataCard dataCard = guiToDataCard(listDataCard.getUserObject(index));
            listDataCard.addItem(dataCard, getDataCardDescription(dataCard));
        }
        List<AbstractDataCard> dataCardList = getAllNonEmptyDataCards();
        boolean vehicleDataCardExists = false;
        for (AbstractDataCard abstractDataCard : listDataCard.getUserObjects()) {
            if (abstractDataCard.isVehicleDataCard()) {
                if (vehicleDataCardExists) {
                    errors.add("!!Mehrere Fahrzeug-Datenkarten vorhanden");
                    return false;
                }
                vehicleDataCardExists = true;
            }
        }
        if (dataCardList.isEmpty()) {
            if (listDataCard.getUserObjects().isEmpty()) {
                warnings.add("!!Keine Datenkarten vorhanden");
            } else {
                //warnings.add("!!Keine besetzten Datenkarten vorhanden");
            }
        } else {
            if (!vehicleDataCardExists) {
                if (dataCardList.size() > 1) {
                    errors.add("!!Mehrere Aggregate-Datenkarten ohne Fahrzeug-Datenkarte vorhanden");
                    return false;
                }
            } else {
                // weitere Abprüfungen auf die Aggregate-Datenkarten (doppelte usw)
            }
        }
        return (warnings.size() + errors.size()) == 0;
    }

    private List<AbstractDataCard> getAllNonEmptyDataCards() {
        List<AbstractDataCard> dataCardList = new DwList<>();
        for (AbstractDataCard abstractDataCard : listDataCard.getUserObjects()) {
            if (!isEmptyDatacard(abstractDataCard)) {
                dataCardList.add(abstractDataCard);
            }
        }
        return dataCardList;
    }

    private VehicleDataCard addAllAggregateDataCards(VehicleDataCard vehicleDataCard, int vehicleIndex, List<AbstractDataCard> dataCardList) {
        vehicleDataCard.clearActiveAggregates();
        for (int index = 0; index < dataCardList.size(); index++) {
            if (index == vehicleIndex) {
                continue;
            }
            AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCardList.get(index);
            // Prüfen ob für den Ident die alt/neu Systematik mit der an den Produkten hinterlegten übereinstimmt
            boolean identChanged = aggregateDataCard.verifyIdentOldNew(getProject(), false);
            if (identChanged) {
                setDatacardValuesForIdent(getProject(), aggregateDataCard);
            }
            vehicleDataCard.addActiveAggregate(aggregateDataCard);
        }
        return vehicleDataCard;
    }

    private int getIndexOfVehicleDataCard(List<AbstractDataCard> dataCardList) {
        if (dataCardList == null) {
            dataCardList = listDataCard.getUserObjects();
        }
        for (int index = 0; index < dataCardList.size(); index++) {
            if (dataCardList.get(index).isVehicleDataCard()) {
                return index;
            }
        }
        return -1;
    }

    private int findDatacard(AbstractDataCard searchDatacard) {
        List<AbstractDataCard> dataCardList = listDataCard.getUserObjects();
        for (int index = 0; index < dataCardList.size(); index++) {
            if (searchDatacard.isVehicleDataCard() && dataCardList.get(index).isVehicleDataCard()) {
                if (compareVehicleDatacards((VehicleDataCard)dataCardList.get(index), (VehicleDataCard)searchDatacard)) {
                    return index;
                }
            } else if (searchDatacard.isAggregateDataCard() && dataCardList.get(index).isAggregateDataCard()) {
                if (compareAggregateDatacards((AggregateDataCard)dataCardList.get(index), (AggregateDataCard)searchDatacard)) {
                    return index;
                }
            }
        }
        return -1;
    }

    private boolean compareVehicleDatacards(VehicleDataCard currentDatacard, VehicleDataCard searchDatacard) {
        if (!currentDatacard.getFinId().isEmpty()) {
            if (searchDatacard.getFinId().isEmpty()) {
                return false;
            }
            if (currentDatacard.getFinId().equals(searchDatacard.getFinId())) {
                return true;
            }
        } else {
            if (!searchDatacard.getFinId().isEmpty()) {
                return false;
            }
            if (currentDatacard.getModelNo().isEmpty()) {
                return false;
            } else {
                if (!searchDatacard.getModelNo().isEmpty()) {
                    return currentDatacard.getModelNo().equals(searchDatacard.getModelNo());
                }
            }
        }
        return false;
    }

    private boolean compareAggregateDatacards(AggregateDataCard currentDatacard, AggregateDataCard searchDatacard) {
        if (!currentDatacard.getAggregateIdent().isEmpty()) {
            if (searchDatacard.getAggregateIdent().isEmpty()) {
                return false;
            }
            if (currentDatacard.getAggregateIdent().equals(searchDatacard.getAggregateIdent())) {
                return true;
            }
        } else {
            if (!searchDatacard.getAggregateIdent().isEmpty()) {
                return false;
            }
            if (currentDatacard.getModelNo().isEmpty()) {
                return false;
            } else {
                if (!searchDatacard.getModelNo().isEmpty()) {
                    return currentDatacard.getModelNo().equals(searchDatacard.getModelNo());
                }
            }
        }
        return false;
    }

    private void enableButtons() {
        AbstractDataCard selectedDataCard = listDataCard.getSelectedUserObject();
        boolean isSelected = selectedDataCard != null;
        toolbarHelper.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.EDIT_DELETE, contextmenu, isSelected);
        toolbarHelper.enableToolbarButtonAndMenu(iPartsToolbarButtonAlias.EDIT_DELETEALL, contextmenu, listDataCard.getItemCount() > 0);
    }

    private void onListDatacardChangeEvent(Event event) {
        enableButtons();
        int index = listDataCard.getSelectedIndex();
        if (index >= 0) {
            if ((lastSelectedIndex >= 0) && (lastSelectedIndex != index)) {
                AbstractDataCard dataCard = guiToDataCard(listDataCard.getUserObject(lastSelectedIndex));
                listDataCard.setUserObject(lastSelectedIndex, dataCard);
                String dataCardDescription = getDataCardDescription(dataCard);
                if (!listDataCard.getText(lastSelectedIndex).equals(dataCardDescription)) {
                    listDataCard.setText(lastSelectedIndex, dataCardDescription);
                }
            }
            dataCardToGui(listDataCard.getUserObject(index));
            mainWindow.splitpane_secondChild.setTitle(listDataCard.getText(index));
            lastSelectedIndex = index;
        }
        refreshSpecValidity();
    }

    private String getDataCardDescription(AbstractDataCard dataCard) {
        if (dataCard.isVehicleDataCard()) {
            return iPartsDataCardDialog.buildVehicleItemString((VehicleDataCard)dataCard);
        } else {
            return iPartsDataCardDialog.buildAggregateItemString(getProject(), (AggregateDataCard)dataCard);
        }
    }

    private void refreshDataCardList() {
        int selectedIndex = listDataCard.getSelectedIndex();
        List<AbstractDataCard> userObjects = listDataCard.getUserObjects();
        List<AbstractDataCard> allObjects = new DwList<>(userObjects);
        listDataCard.switchOffEventListeners();
        listDataCard.removeAllItems();
        for (AbstractDataCard dataCard : allObjects) {
            listDataCard.addItem(dataCard, getDataCardDescription(dataCard));
        }
        listDataCard.switchOnEventListeners();
        listDataCard.setSelectedIndex(selectedIndex);
    }

    public AbstractDataCard guiToDataCard(AbstractDataCard dataCard) {
        return filterValuesPanel.fillDataCard(dataCard);
    }

    private void dataCardToGui(AbstractDataCard dataCard) {
        filterValuesPanel.initDialog(dataCard);
    }

    private boolean isEmptyDatacard(AbstractDataCard dataCard) {
        if (dataCard.isDataCardLoaded() || dataCard.isModelLoaded()) {
            return false;
        }
        if (dataCard.isProductGroupValid() || dataCard.isTechnicalApprovalDateValid() || dataCard.isSteeringValueValid() || dataCard.isGearboxValueValid()
            || dataCard.isFactoryNumberValid(getProject())) {
            return false;
        }
        if (!dataCard.getCodes().isEmpty() || !dataCard.getSaas().isEmpty() || !dataCard.getModelNo().isEmpty()) {
            return false;
        }
        if (dataCard.isVehicleDataCard()) {
            VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
            if (!vehicleDataCard.getFinId().isEmpty()) {
                return false;
            }
            if (!vehicleDataCard.getActiveAggregates().isEmpty()) {
                return false;
            }
            if (!vehicleDataCard.getActiveSpringLegFront().isEmpty() || !vehicleDataCard.getActiveSpringRear().isEmpty() ||
                !vehicleDataCard.getActiveSpringShimRear().isEmpty()) {
                return false;
            }
            if (vehicleDataCard.isDateOfTechnicalStateValid()) {
                return false;
            }
        } else {
            AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCard;
            if (!aggregateDataCard.getAggregateIdent().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void doAddDatacard(Event event) {
        AbstractDataCard dataCard = iPartsFilterDataCardSelectDialog.showSelectDialog(this);
        addDatacardToList(dataCard, true);
    }

    private void addDatacardToList(AbstractDataCard dataCard, boolean withSelectIndex) {
        AbstractDataCard firstDataCard = null;
        if (dataCard != null) {
            if (listDataCard.getItemCount() == 0) {
                firstDataCard = guiToDataCard(firstDataCard);
                if (isEmptyDatacard(firstDataCard)) {
                    firstDataCard = null;
                }
            }
            listDataCard.switchOffEventListeners();
            if (firstDataCard != null) {
                listDataCard.addItem(firstDataCard, getDataCardDescription(firstDataCard));
            }
            listDataCard.addItem(dataCard, getDataCardDescription(dataCard));
            listDataCard.switchOnEventListeners();
            if (withSelectIndex) {
                listDataCard.setSelectedIndex(listDataCard.getItemCount() - 1);
            }
        }
    }

    private void doDelDatacard(Event event) {
        int index = listDataCard.getSelectedIndex();
        if (index >= 0) {
            listDataCard.removeItem(index);
            lastSelectedIndex = -1;
        }
        if (listDataCard.getItemCount() == 0) {
            filterValuesPanel.initDialog();
        } else {
            if (index >= listDataCard.getItemCount()) {
                index--;
            }
            listDataCard.setSelectedIndex(index);
        }
    }

    private void doDelAllDatacards(Event event) {
        ModalResult dialogResult = MessageDialog.showYesNo(TranslationHandler.translate("!!Sollen wirklich alle Datenkarten gelöscht werden?"),
                                                           TranslationHandler.translate("!!Alle Datenkarten löschen"));
        if (dialogResult == ModalResult.YES) {
            clearDatacards();
        }
    }

    private void onButtonOKClick(Event event) {
        List<String> warnings = new DwList<String>();
        List<String> errors = new DwList<String>();
        if (checkDataCards(warnings, errors)) {
            getFilledDataCard();
        }
    }

    private void refreshSpecValidity() {
        if (isVehicleDatacardLoaded()) {
            AbstractDataCard filledDataCard = getFilledDataCard();
            if (filledDataCard instanceof VehicleDataCard) {
                VehicleDataCard filledVehicleDataCard = (VehicleDataCard)filledDataCard;
                filledVehicleDataCard.loadValidSpecifications(getProject()); // Spezifikationen explizit neu laden
                filterValuesPanel.setSpecValidity(filledVehicleDataCard.getSpecValidities(getProject()));
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
    private de.docware.framework.modules.gui.controls.menu.GuiContextMenu contextmenu;

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
        private de.docware.framework.modules.gui.controls.toolbar.GuiToolbar toolbarDatacard;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiList<AbstractDataCard> listDatacard;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel splitpane_secondChild;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            contextmenu = new de.docware.framework.modules.gui.controls.menu.GuiContextMenu();
            contextmenu.setName("contextmenu");
            contextmenu.__internal_setGenerationDpi(96);
            contextmenu.registerTranslationHandler(translationHandler);
            contextmenu.setScaleForResolution(true);
            contextmenu.setMinimumWidth(10);
            contextmenu.setMinimumHeight(10);
            contextmenu.setMenuName("contextmenu");
            contextmenu.setParentControl(this);
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
            title.setTitle("!!Filter");
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
            splitpane.setDividerPosition(244);
            splitpane_firstChild = new de.docware.framework.modules.gui.controls.GuiPanel();
            splitpane_firstChild.setName("splitpane_firstChild");
            splitpane_firstChild.__internal_setGenerationDpi(96);
            splitpane_firstChild.registerTranslationHandler(translationHandler);
            splitpane_firstChild.setScaleForResolution(true);
            splitpane_firstChild.setMinimumWidth(0);
            splitpane_firstChild.setTitle("!!Datenkarten");
            de.docware.framework.modules.gui.layout.LayoutBorder splitpane_firstChildLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            splitpane_firstChild.setLayout(splitpane_firstChildLayout);
            toolbarDatacard = new de.docware.framework.modules.gui.controls.toolbar.GuiToolbar();
            toolbarDatacard.setName("toolbarDatacard");
            toolbarDatacard.__internal_setGenerationDpi(96);
            toolbarDatacard.registerTranslationHandler(translationHandler);
            toolbarDatacard.setScaleForResolution(true);
            toolbarDatacard.setMinimumWidth(10);
            toolbarDatacard.setMinimumHeight(10);
            toolbarDatacard.setBackgroundImage(new de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage("imgDesignToolbarBigBackground"));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder toolbarDatacardConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            toolbarDatacardConstraints.setPosition("north");
            toolbarDatacard.setConstraints(toolbarDatacardConstraints);
            splitpane_firstChild.addChild(toolbarDatacard);
            listDatacard = new de.docware.framework.modules.gui.controls.GuiList<AbstractDataCard>();
            listDatacard.setName("listDatacard");
            listDatacard.__internal_setGenerationDpi(96);
            listDatacard.registerTranslationHandler(translationHandler);
            listDatacard.setScaleForResolution(true);
            listDatacard.setMinimumWidth(200);
            listDatacard.setMinimumHeight(100);
            listDatacard.addEventListener(new de.docware.framework.modules.gui.event.EventListener("onChangeEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onListDatacardChangeEvent(event);
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
                    onButtonOKClick(event);
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