/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.events.OnChangeEvent;
import de.docware.apps.etk.base.forms.events.OnEnableButtonsEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterSwitchboard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TwoGridValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Der Filterdialog
 */
public class iPartsFilterDialog extends AbstractJavaViewerForm {

    private iPartsFilterSwitchboardPanel switchboardPanel; // für die Darstellung des Filter-Hauptschalters und der Checkboxen f.d. Einzelfilter
    private iPartsFilterAdminDialog filterValuesPanel;  // für Anzeige der Filterwerte
    private boolean hasChanged;
    private FinId lastFinId;
    private String lastCountry;
    private boolean isLoading;

    /**
     * Erzeugt eine Instanz von iPartsSetFilterDialogZwei.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public iPartsFilterDialog(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
        initDialog();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);

        switchboardPanel = new iPartsFilterSwitchboardPanel(getConnector(), this, getProject());
        ConstraintsBorder panelLeftConstraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        switchboardPanel.setOnChangeEvent(new OnChangeEvent() {
            @Override
            public void onChange() {
                doOnUpdateSwitchboardPanel();
            }
        });
        switchboardPanel.getGui().setConstraints(panelLeftConstraints);
//        activateFilterDialog.setActivatableFilters(new iPartsActivatableFilterContainer());
        mainWindow.panelLeft.addChild(switchboardPanel.getGui());
        switchboardPanel.setPanelTitle(mainWindow.panelLeft.getTitle());
        mainWindow.panelLeft.setTitle("");

        /* Buttons für Alles auswählen/abwählen erstellen */
        GuiButtonOnPanel button = mainWindow.buttonpanel.addCustomButton("!!Alle auswählen");
        button.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doOnSelectAllSwitchboardEntries(true);
            }
        });
        button = mainWindow.buttonpanel.addCustomButton("!!Alle abwählen");
        button.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doOnSelectAllSwitchboardEntries(false);
            }
        });

        button = mainWindow.buttonpanel.setButtonVisible(GuiButtonOnPanel.ButtonType.RESET, true);
        button.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                lastFinId = null;
                // Nur der RESET-Button löscht über das zweite Flag die Datenkarte.
                initDialog(null, true);
                filterValuesPanel.saveValuesOfLastSelectedDatacard();
                switchboardPanel.onSelectAllEntries(true);
                doOnUpdateSwitchboardPanel();
                mainWindow.title.setTitle("!!Filter ändern");
            }
        });

        OnChangeEvent onLoadVehicleDataCardEvent = new OnChangeEvent() {
            @Override
            public void onChange() {
                onLoadVehicleDataCard();
            }
        };
        OnChangeEvent onLoadAggregateDataCardEvent = new OnChangeEvent() {
            @Override
            public void onChange() {
                onLoadAggregateDataCard();
            }
        };
        OnEnableButtonsEvent onEnableButtonsEvent = new OnEnableButtonsEvent() {
            @Override
            public void doEnableButtons() {
                enableButtons();
            }
        };
        filterValuesPanel = new iPartsFilterAdminDialog(getConnector(), this, onLoadVehicleDataCardEvent, onLoadAggregateDataCardEvent, onEnableButtonsEvent);

        panelLeftConstraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        filterValuesPanel.getGui().setConstraints(panelLeftConstraints);
        mainWindow.panelForFilterValues.addChild(filterValuesPanel.getGui());
        filterValuesPanel.getFocusComponent().requestFocus();
    }

    private void doOnSelectAllSwitchboardEntries(boolean isSelect) {
        switchboardPanel.onSelectAllEntries(isSelect);
        doOnUpdateSwitchboardPanel();
    }

    private void doOnUpdateSwitchboardPanel() {
        Map<iPartsFilterSwitchboard.FilterTypes, String> warnings = new HashMap<>();
        checkIfInputIsValid(switchboardPanel.getFilterSelection(), warnings);
        switchboardPanel.setWarnings(warnings);
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public void setDataCard(AbstractDataCard dataCard) {
        if (dataCard != null) {
            if (dataCard.isVehicleDataCard()) {
                VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
                initDialog(vehicleDataCard);
            } else {
                AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCard;
                initDialog(aggregateDataCard);
            }
        } else {
            initDialog();
        }
    }

    private void initSwitchboard() {
        hasChanged = false;
        lastCountry = null;
        iPartsFilter filter = iPartsFilter.get();
        iPartsFilterSwitchboard switchboardState = filter.getSwitchboardState();
        if (switchboardState == null) {
            switchboardState = new iPartsFilterSwitchboard();
        }
        switchboardPanel.setSwitchboardState(switchboardState.cloneMe());
        EnumSet<iPartsFilterSwitchboard.FilterTypes> filterSet = switchboardState.getAllActivated(false);
        if (filterSet.isEmpty()) {
            doOnSelectAllSwitchboardEntries(true);
        }
        switchboardPanel.getGui().setMinimumWidth(switchboardPanel.getPreferredWith() + 12);
    }

    private void initDialog() {
        isLoading = true;
        try {
            initSwitchboard();
            iPartsFilter filter = iPartsFilter.get();
            filterValuesPanel.initDialog(filter.getCurrentDataCard());
        } finally {
            isLoading = false;
        }
        enableButtons();
    }

    /**
     * Dialog mit Inhalt Datenkarte befüllen.
     * z.B. nach Klick auf Übernehmen in Anzeige Datenkarte
     *
     * @param dataCard
     */
    private void initDialog(AbstractDataCard dataCard) {
        initDialog(dataCard, false);
    }

    private void initDialog(AbstractDataCard dataCard, boolean resetDataCard) {
        //initActivatableDialog();
        filterValuesPanel.initDialog(dataCard, resetDataCard);
        enableButtons();
    }

    public ModalResult showModal() {
        enableButtons();
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

    /* ======== Check-Routinen für die einzelnen Filter ========*/

    private boolean checkInput(boolean isActive, String valueToCheck, List<String> errors, String errorMsg) {
        if (!isActive || (isActive && !StrUtils.isEmpty(valueToCheck))) {
            return true;
        }
        errors.add(TranslationHandler.translate(errorMsg));
        return false;
    }

    private boolean checkInput(boolean isActive, boolean inputValid, List<String> errors, String errorMsg) {
        if (!isActive || (isActive && inputValid)) {
            return true;
        }
        errors.add(TranslationHandler.translate(errorMsg));
        return false;
    }

    private boolean checkInput(boolean isActive, TwoGridValues gridValues, List<String> errors, String errorMsg) {
        if (!isActive || (isActive && !gridValues.getAllCheckedValues().isEmpty())) {
            return true;
        }
        errors.add(TranslationHandler.translate(errorMsg));
        return false;
    }

    private boolean checkInput(boolean isActive, TwoGridValues gridValues, boolean topGrid, List<String> errors, String errorMsg) {
        if (!isActive || (isActive && !gridValues.getCheckedValues(topGrid).isEmpty())) {
            return true;
        }
        errors.add(TranslationHandler.translate(errorMsg));
        return false;
    }

    ///////////////////////
    private boolean checkInput(boolean isActive, String valueToCheck, iPartsFilterSwitchboard.FilterTypes activatableFilter, Map<iPartsFilterSwitchboard.FilterTypes, String> errors, String errorMsg) {
        if (!isActive || (isActive && !StrUtils.isEmpty(valueToCheck))) {
            return true;
        }
        errors.put(activatableFilter, TranslationHandler.translate(errorMsg));
        return false;
    }

    private boolean checkInput(boolean isActive, boolean inputValid, iPartsFilterSwitchboard.FilterTypes activatableFilter, Map<iPartsFilterSwitchboard.FilterTypes, String> errors, String errorMsg) {
        if (!isActive || (isActive && inputValid)) {
            return true;
        }
        errors.put(activatableFilter, TranslationHandler.translate(errorMsg));
        return false;
    }

    private boolean checkInput(boolean isActive, TwoGridValues gridValues, iPartsFilterSwitchboard.FilterTypes activatableFilter, Map<iPartsFilterSwitchboard.FilterTypes, String> errors, String errorMsg) {
        if (!isActive || (isActive && !gridValues.getAllCheckedValues().isEmpty())) {
            return true;
        }
        errors.put(activatableFilter, TranslationHandler.translate(errorMsg));
        return false;
    }

    private boolean checkInput(boolean isActive, TwoGridValues gridValues, iPartsEvent event, boolean topGrid, iPartsFilterSwitchboard.FilterTypes activatableFilter, Map<iPartsFilterSwitchboard.FilterTypes, String> errors, String errorMsg) {
        if (!isActive || (!gridValues.getCheckedValues(topGrid).isEmpty()) || (event != null)) {
            return true;
        }
        errors.put(activatableFilter, TranslationHandler.translate(errorMsg));
        return false;
    }

    /* ======== Check-Routinen für die einzelnen Filter Ende ========*/

    private void checkIfInputIsValid(iPartsFilterSwitchboard switchboardState, Map<iPartsFilterSwitchboard.FilterTypes, String> errors) {
        for (Map.Entry<iPartsFilterSwitchboard.FilterTypes, iPartsFilterSwitchboard.FilterItem> entry : switchboardState.getFilterItemMap().entrySet()) {
            boolean isActivated = entry.getValue().isActivated();
            EtkProject project = getProject();
            switch (entry.getKey()) {
                case MODEL:
                    checkInput(isActivated, filterValuesPanel.getModelNo(), entry.getKey(), errors,
                               "!!Kein Baumuster für Baumuster-Filter ausgewählt!");
                    break;
                case END_NUMBER:
                    String extra = "";
                    if (!isActivated) {
                        break;
                    } else {
                        // Temporäre Datenkarte erzeugen
                        List<iPartsProduct> autoSelectProducts = iPartsFilterHelper.getAutoSelectProducts(filterValuesPanel.getModelNo(),
                                                                                                          filterValuesPanel.getFinId(), filterValuesPanel.getCodes(), project);
                        AbstractDataCard dataCard = filterValuesPanel.guiToDataCard(null);
                        if (dataCard.isAggregateDataCard()) {
                            AggregateDataCard aggDataCard = (AggregateDataCard)dataCard;
                            AggregateIdent ident = null;
                            switch (aggDataCard.getAggregateType()) {
                                case ENGINE:
                                    EngineIdent engineIdent = new EngineIdent(project, aggDataCard.getAggregateIdent());
                                    if (aggDataCard.isOldIdentSpecification()) {
                                        engineIdent.setIdentSpecificationOld();
                                    } else {
                                        engineIdent.setIdentSpecificationNew();
                                    }
                                    ident = engineIdent;
                                    break;
                                case TRANSMISSION:
                                    ident = new TransmissionIdent(project, aggDataCard.getAggregateIdent());
                                    break;
                            }
                            if ((ident != null) && ident.isExchangeAggregate()) {
                                extra = "!!Endnummernfilter ist deaktiviert!";
                            }
                        }
                        if (dataCard.isValidForEndNumberFilter(autoSelectProducts, project)) {
                            if (!extra.isEmpty()) {
                                // Fehlermeldung: Endnummern-Filter deaktiviert wegen TauschMotor/Getriebe
                                errors.put(entry.getKey(), TranslationHandler.translate(extra));
                            }
                            break;
                        }
                    }
                    if (!extra.isEmpty()) {
                        // Fehlermeldung: Endnummern-Filter deaktiviert wegen TauschMotor/Getriebe
                        errors.put(entry.getKey(), TranslationHandler.translate(extra));
                    } else {
                        // Fehlermeldung: Endnummern-Filter gesetzt aber Datenkarte nicht gültig für Endnummern-Filter
                        errors.put(entry.getKey(), TranslationHandler.translate("!!Kein gültiges Werk sowie Endnummer oder Termin für Endnummern-Filter ausgewählt!"));
                    }
                    break;
//                case DATACARD_SA: // ungültig mit DAIMLER-6216
//                    if (isActivated) {
//                        if (filterValuesPanel.getDatacardSaaNumbers().isEmpty() && filterValuesPanel.getCodes().isEmpty()) {
//                            errors.put(entry.getKey(), TranslationHandler.translate("!!Keine SAAs oder Code für Datenkarten-SA-Filter ausgewählt!"));
//                            break;
//                        }
//                    }
//                    break;
                case SA_STRICH:
                    checkInput(isActivated, filterValuesPanel.getDatacardSaaNumbers(), entry.getKey(), errors,
                               "!!Keine SAAs für SA-Strich-Filter ausgewählt!");
                    break;
                case STEERING:
                    checkInput(isActivated, filterValuesPanel.getSteeringValue(), entry.getKey(), errors,
                               "!!Kein gültiger Lenkungswert für Lenkungs-Filter ausgewählt!");
                    break;
                case GEARBOX:
                    checkInput(isActivated, filterValuesPanel.getGearboxValue(), entry.getKey(), errors,
                               "!!Keine gültige Getriebeart für Getriebe-Filter ausgewählt!");
                    break;
                case SPRING:
                    checkInput(isActivated, filterValuesPanel.getSpringFields().isValid(), entry.getKey(), errors,
                               "!!Keine Teilenummer für Feder-Filter ausgewählt!");
                    break;
                case DATACARD_CODE:
                    // Datenkarten-Code-Filter kann angewandt werden, wenn Code oder ein Event vorhanden sind
                    checkInput(isActivated, filterValuesPanel.getCodes(), filterValuesPanel.getEvent(), false, entry.getKey(), errors,
                               "!!Keine Code und kein Ereignis für Datenkarten-Code-Filter ausgewählt!");
                    break;
                case EXTENDED_CODE:
                    checkInput(isActivated, filterValuesPanel.getCodes(), entry.getKey(), errors,
                               "!!Keine Code für erweiterte Code-Auswertung ausgewählt!");
                    break;
                case EXTENDED_COLOR:
                    checkInput(isActivated, filterValuesPanel.getCodes(), entry.getKey(), errors,
                               "!!Keine Code für erweiterten Farb-Filter ausgewählt!");
                    break;
                case REMOVE_DUPLICATES:
                    // aktuell nicht klar, ob Datums-Filter und Termin-Filter unterschiedliche Filter sind
                    //checkInput(entry.getValue().isActivated(), filterValueDialog.getFactoryValue(), errors,
                    //           "!!Kein gültiges Werk für Termin-Filter ausgewählt!");
                    break;
                case AGG_MODELS:
                    checkInput(isActivated, filterValuesPanel.getAggModelsNumbers(), entry.getKey(), errors,
                               "!!Keine Aggregatebaumuster für Aggregate-Filter ausgewählt!");
                    break;
                case PSK_VARIANTS:
                    boolean isAutoSelectedProductsPSK = filterValuesPanel.isAutoSelectedProductsPSK();
                    if (!isAutoSelectedProductsPSK) {
                        // In allen Datenkarten prüfen, ob es dort ein PSK-Produkt gibt
                        Set<iPartsProductId> pskProductIdsOfAssociatedDataCard = new TreeSet<>();
                        List<AbstractDataCard> dataCards = filterValuesPanel.getDatacards();
                        for (AbstractDataCard dataCard : dataCards) {
                            FinId finId;
                            if (dataCard instanceof VehicleDataCard) {
                                finId = ((VehicleDataCard)dataCard).getFinId();
                            } else if (dataCard instanceof AggregateDataCard) {
                                finId = new FinId(((AggregateDataCard)dataCard).getAggregateIdent());
                            } else {
                                finId = new FinId();
                            }
                            iPartsAdminFilterValuesPanel.getProductNumbers(dataCard.getModelNo(), dataCard.getCodes(), finId,
                                                                           pskProductIdsOfAssociatedDataCard, project);
                        }

                        if (!pskProductIdsOfAssociatedDataCard.isEmpty()) {
                            isAutoSelectedProductsPSK = true;
                        }
                    }

                    // Abhängig von PSK-Produkten im Filterwerte-Panel bzw. allen Datenkarten muss der Filter im Switchboard
                    // aktivierbar sein oder nicht
                    if (!isAutoSelectedProductsPSK) {
                        // Regelt später in den Events, ob der Filter aktiviert werden darf
                        entry.getValue().setCanBeActivated(false);
                        OnChangeEvent oldOnChangeEvent = switchboardPanel.getOnChangeEvent();
                        switchboardPanel.setOnChangeEvent(null);
                        switchboardPanel.enableCheckBox(entry.getKey(), false);
                        switchboardPanel.selectCheckBox(entry.getKey(), false);
                        switchboardPanel.setOnChangeEvent(oldOnChangeEvent);
                    } else {
                        boolean oldEntryCanBeActivated = entry.getValue().isCanBeActivated();
                        entry.getValue().setCanBeActivated(true);
                        OnChangeEvent oldOnChangeEvent = switchboardPanel.getOnChangeEvent();
                        switchboardPanel.setOnChangeEvent(null);

                        // Checkbox nur enablen falls auch der Hauptfilter-Button aktiviert ist
                        if (switchboardState.isMainSwitchActive()) {
                            switchboardPanel.enableCheckBox(entry.getKey(), true);
                        }

                        if (!oldEntryCanBeActivated) {
                            // PSK-Varianten-Filter automatisch selektieren falls mindestens ein anderer Filter aktiv ist
                            if (!switchboardPanel.getFilterSelection().getAllActivated(true).isEmpty()) {
                                isActivated = true;
                            }
                            switchboardPanel.selectCheckBox(entry.getKey(), isActivated);
                        }
                        switchboardPanel.setOnChangeEvent(oldOnChangeEvent);

                        checkInput(isActivated, !filterValuesPanel.getPSKVariants().isEmpty(), entry.getKey(), errors,
                                   "!!Keine PSK-Variantengültigkeiten für PSK-Varianten-Filter ausgewählt");
                    }
                    break;
                case COUNTRY_VALIDITY_FILTER:
                    String newCountry = filterValuesPanel.getCountry();
                    checkInput(isActivated, newCountry, entry.getKey(), errors,
                               "!!Keine Ländergültigkeit für Ländergültigkeits-Filter ausgewählt!");
                    if (!isActivated && StrUtils.isValid(newCountry) && !Utils.objectEquals(lastCountry, newCountry)) {
                        switchboardPanel.selectCheckBox(entry.getKey(), true);
                        lastCountry = newCountry;
                    }
                    break;
                case SPECIFICATION_FILTER:
                    checkInput(isActivated, !filterValuesPanel.getSpecValidity().isEmpty(), entry.getKey(), errors,
                               "!!Keine Spezifikationen für Spezifikations-Filter ausgewählt!");
                    break;

            }
        }
    }

    /**
     * Speichert die eingegebenen Filterwerte im {@link iPartsFilter}
     */
    private void fillFilterWithValues(iPartsFilterSwitchboard switchboardState) {
        iPartsFilter filter = iPartsFilter.get();
        hasChanged = switchboardState.isModified(filter.getSwitchboardState());
        filter.setSwitchboardState(switchboardState);
        AbstractDataCard newFilterDataCard = filterValuesPanel.getFilledDataCard();
        if (!hasChanged) {
            hasChanged = filter.getCurrentDataCard().isModified(newFilterDataCard, true);
        }
        filter.setCurrentDataCard(newFilterDataCard, getProject());
    }

    private void buttonOkClick(Event event) {
        List<String> errors = new DwList<String>();
        List<String> warnings = new DwList<String>();
        iPartsFilterSwitchboard switchboardState = switchboardPanel.getFilterSelection();
        //checkIfInputIsValid(switchboardState, errors);
        if (filterValuesPanel.checkDataCards(warnings, errors)) {
            fillFilterWithValues(switchboardState);
            mainWindow.setModalResult(ModalResult.OK);
            close();
        } else {
            if (!errors.isEmpty()) {
                errors.add("");
                for (String str : warnings) {
                    errors.add(TranslationHandler.translate("!!Warnung: %1", TranslationHandler.translate(str)));
                }
                MessageDialog.showError(errors);
            } else {
                MessageDialog.showWarning(warnings);
            }
        }
    }

    private void buttonCancelClick(Event event) {
        mainWindow.setModalResult(ModalResult.CANCEL);
        close();
    }


    private void enableButtons() {
        doOnUpdateSwitchboardPanel();
        filterValuesPanel.setLastLoadedFin(lastFinId);
    }

    private void onLoadVehicleDataCard() {
        filterValuesPanel.setIgnoreAggIdentChangeEvent(true);
        String newTitle = "!!Filter ändern";

        VinId vinId = filterValuesPanel.getVinId();
        FinId finId = filterValuesPanel.getFinId();
        if (!finId.isValidId() && !vinId.isValidId() && (lastFinId != null)) {
            finId = lastFinId;
        }
        String modelNo = filterValuesPanel.getModelNo();
        iPartsAskFinModelDialog.FinModelNoContainer fmCont = iPartsAskFinModelDialog.askForFinIdOrModelNo(this, finId, modelNo, vinId);
        if (fmCont.isContainerValid()) {
            List<String> messageList = new DwList<>();
            if (fmCont.isModelNoValid()) {
                lastFinId = null;
                // Baumuster ist gültig
                AbstractDataCard dataCard = AbstractDataCard.createModelDatacardByModelType(getProject(), fmCont.modelNo);

                // hier versuchen ein DC aus dem Baumuster anzulegen
                if (dataCard.isModelLoaded()) {
                    messageList.add(TranslationHandler.translate("!!Datenkarte wird aus Baumuster \"%1\" erzeugt.", fmCont.modelNo));
                    MessageDialog.show(messageList);
                    // DatenKarte setzen
                    iPartsDataCardDialog dlg = new iPartsDataCardDialog(getConnector(), this, getDataCardMasterDialogDividerPosition());
                    dlg.setDataCard(dataCard);
                    if (dlg.showModal() == ModalResult.OK) {
                        // Übernehmen der Datenkarte; Abgleich unnötig, da Baumuster-Datenkarte
                        // und anzeigen
                        initDialog(dataCard);
                    }
                } else {
                    messageList.add(TranslationHandler.translate("!!Datenkarte konnte nicht aus Baumuster \"%1\" erzeugt werden.", fmCont.modelNo));
                    MessageDialog.showWarning(messageList);
                }
            } else {
                // FIN/VIN ist gültig
                boolean currentNoIsVin = false;
                try {
                    finId = fmCont.finId;
                    vinId = fmCont.vinId;
                    if (!finId.isValidId()) {
                        finId = new FinId(fmCont.vinId.getVIN());
                        currentNoIsVin = true;
                    }
                    // todo lastFinId erste setzen, nachdem DC geladen ist
                    lastFinId = finId;
                    VehicleDataCard dataCard = loadVehicleDataCard(finId);
                    if (dataCard == null) { // Warte-Dialog wurde abgebrochen
                        return;
                    } else if (dataCard.isDataCardLoaded()) { // Datenkarte wurde erfolgreich geladen
                        // DatenKarte setzen
                        iPartsDataCardDialog dlg = new iPartsDataCardDialog(getConnector(), this, getDataCardMasterDialogDividerPosition());
                        dlg.setDataCard(dataCard);
                        if (dlg.showModal() == ModalResult.OK) {
                            // Übernehmen der DC in Filter-DC (Abgleich der SAAs, Agg-BMs und Codes mit dem Baumuster)
                            AbstractDataCard newDataCard = dlg.getDataCard().cloneMe();
                            if (newDataCard.isVehicleDataCard()) {
                                dataCard = (VehicleDataCard)newDataCard;

                                // Datenkarte aus Baumuster erzeugen
                                VehicleDataCard modelDataCard = new VehicleDataCard(true);
                                modelNo = dataCard.getFinId().getFullModelNumber();
                                modelDataCard.fillByModel(getProject(), modelNo);

                                // ggf. darf die Datenkarte nicht verwendet werden und soll stattdessen nur das BM verwendet werden
                                if (dataCard.hasForbiddenLifeCycleStatus()) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(TranslationHandler.translate("!!Datenkarte für FIN \"%1\" ist nicht vorhanden.", dataCard.getFinId().getFIN()) + " ");
                                    if (dataCard.getLifeCycleStatus() == AbstractDataCard.LifeCycleStatus.scrapped) {
                                        sb.append(TranslationHandler.translate("!!Das Fahrzeug wurde verschrottet."));
                                    } else if (dataCard.getLifeCycleStatus() == AbstractDataCard.LifeCycleStatus.stolen) {
                                        sb.append(TranslationHandler.translate("!!Das Fahrzeug wurde gestohlen."));
                                    } else {
                                        sb.append(TranslationHandler.translate("!!Der Grund ist unbekannt."));
                                    }
                                    sb.append(" ");
                                    sb.append(TranslationHandler.translate("!!Datenkarte wird aus Baumuster \"%1\" erzeugt.", modelNo));
                                    messageList.add(sb.toString());
                                    MessageDialog.show(messageList);
                                    dataCard = modelDataCard;
                                } else {
                                    // Daten aus BM-Datenkarte in Fahrzeug-Datenkarte übernehmen
                                    iPartsFilterHelper.adoptDCToModelDC(getProject(), modelDataCard, dataCard);
                                }

                                boolean doAsk;
                                AbstractDataCard selectedDataCard = filterValuesPanel.getSelectedDatacard();
                                if (selectedDataCard != null) {
                                    doAsk = selectedDataCard.isAggregateDataCard();
                                } else {
                                    doAsk = filterValuesPanel.getDatacardsCount() > 0;
                                }
                                if (doAsk) {
                                    if (MessageDialog.showYesNo("!!Bisherige Datenkarten löschen?") == ModalResult.YES) {
                                        filterValuesPanel.clearDatacards();
                                    } else {
                                        // Werte der letzten Datenkarte sichern
                                        filterValuesPanel.saveValuesOfLastSelectedDatacard();
                                    }
                                }
                                // und anzeigen
                                initDialog(dataCard);
                            } else {
                                //es ist eine Aggregate-Datenkarte
                                AggregateDataCard aggregateDataCard = (AggregateDataCard)newDataCard;
                                AggregateDataCard modelDataCard = new AggregateDataCard(true);
                                modelNo = aggregateDataCard.getModelNo();
                                modelDataCard.fillByModel(getProject(), modelNo);
                                //Model- und Vehicle-Datacard zusammenführen
                                iPartsFilterHelper.adoptDCToModelDC(modelDataCard, aggregateDataCard);
                                if (filterValuesPanel.getDatacardsCount() > 0) {
                                    if (MessageDialog.showYesNo("!!Bisherige Datenkarten löschen?") == ModalResult.YES) {
                                        filterValuesPanel.clearDatacards();
                                    } else {
                                        // Werte der letzten Datenkarte sichern
                                        filterValuesPanel.saveValuesOfLastSelectedDatacard();
                                    }
                                }
                                // und anzeigen
                                initDialog(aggregateDataCard);
                                newTitle = TranslationHandler.translate("!!Filter ändern (FIN: %1)", finId.getFIN());
                            }
                        } else {
                            // auch beim Abbruch der Anzeige der Datenkarte wird die lastFinId übernommen
                            enableButtons();
                        }
                    } else {
                        // keine Datenkarte gefunden
                        messageList.add(TranslationHandler.translate("!!Datenkarte für %1 \"%2\" ist nicht vorhanden.",
                                                                     currentNoIsVin ? "VIN" : "FIN", finId.getFIN()));
                        if (currentNoIsVin) {
                            handleVINWithoutDatacard(vinId, messageList);
                        } else if (finId.isModelNumberValid() && !currentNoIsVin) {
                            messageList.add(TranslationHandler.translate("!!Datenkarte wird aus Baumuster \"%1\" erzeugt.", finId.getFullModelNumber()));
                            MessageDialog.show(messageList);
                            // hier versuchen ein DC aus dem Baumuster anzulegen
                            fillFromModel(finId.getFullModelNumber(), finId, vinId);
                        } else {
                            MessageDialog.showWarning(messageList);
                        }
                    }
                } catch (DataCardRetrievalException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
                    messageList.add(TranslationHandler.translate("!!Fehler beim Laden der Datenkarte:"));
                    messageList.add(e.getLocalizedMessage());
                    if (currentNoIsVin) {
                        handleVINWithoutDatacard(vinId, messageList);
                    } else if (finId.isModelNumberValid()) {
                        messageList.add(TranslationHandler.translate("!!Datenkarte wird aus Baumuster \"%1\" erzeugt.", finId.getFullModelNumber()));
                        MessageDialog.show(messageList);
                        // hier versuchen ein DC aus dem Baumuster anzulegen
                        fillFromModel(finId.getFullModelNumber(), finId, vinId);
                    } else {
                        MessageDialog.showWarning(messageList);
                    }
                }
            }
        }

        mainWindow.title.setTitle(newTitle);
        filterValuesPanel.setIgnoreAggIdentChangeEvent(false);
    }

    /**
     * Verarbeitet die eingegebene VIN, die zu keiner Datenkarte führte. In diesem Fall muss geprüft werden, ob via
     * VIN-Baumuster Fallback die Datenkarte mit einem verknüpften Baumuster gefüllt werden kann.
     *
     * @param vinId
     * @param messageList
     */
    private void handleVINWithoutDatacard(VinId vinId, List<String> messageList) {
        // Es handelt sich um eine VIN zu der keine Datenkarte gefunden wurde
        if (vinId.isValidId() && iPartsVINModelMappingCache.getInstance(getProject()).hasAtLeastOneMappedModel(getProject(), vinId.getVIN())) {
            String modelNo;
            boolean hasMoreThanOneFallbackModel = iPartsVINModelMappingCache.getInstance(getProject()).hasMultipleMappedModels(getProject(), vinId.getVIN());
            // Existiert nur ein gemapptes Baumuster, dann wird es direkt gesetzt. Bei >1 wird der Auswahldialog angezeigt
            if (hasMoreThanOneFallbackModel) {
                modelNo = iPartsVINModelSelectionForm.showModelSelection(getProject(), vinId.getVIN());
            } else {
                modelNo = iPartsVINModelMappingCache.getInstance(getProject()).getVisibleModelsForVINPrefix(getProject(), vinId.getVIN()).get(0);
            }
            if (StrUtils.isValid(modelNo)) {
                // Wenn der Benutzer schon den Dialog für die auswahl der möglichen Baumuster vor sich hatte, dann soll
                // nicht zusätzlich der Info-Dialog angezeigt werden
                if (!hasMoreThanOneFallbackModel) {
                    messageList.add(TranslationHandler.translate("!!Datenkarte wird aus dem VIN Fallback-Baumuster \"%1\" erzeugt.", modelNo));
                    MessageDialog.show(messageList);
                }
                if (!fillFromModel(modelNo, null, vinId)) {
                    // Sollte eigentlich nie passieren, da die Baumuster ja vorher schon geprüft werden
                    MessageDialog.showWarning(TranslationHandler.translate("!!Datenkarte konnte nicht aus Baumuster \"%1\" erzeugt werden.", modelNo));
                }
            }
        } else {
            if (vinId.isSuffixForModelMappingValid()) {
                messageList.add(TranslationHandler.translate("!!Fallback auf Baumuster nicht möglich, da kein VIN-Baumuster-Mapping für den VIN-Präfix \"%1\" existiert.",
                                                             vinId.getPrefixForModelMapping()));
            } else {
                messageList.add(TranslationHandler.translate("!!Fallback auf Baumuster nicht möglich, da \"%1\" kein gültiger Baumuster-Suffix ist (6. und 7. Stelle der VIN).",
                                                             vinId.extractModelSuffixForMapping()));
            }
            MessageDialog.showWarning(messageList);
        }
    }

    public int getDataCardMasterDialogDividerPosition() {
        return mainWindow.panelLeft.getPreferredWidth() - 5; // 5px für Divider-Breite;
    }

    private boolean fillFromModel(String modelNo, FinId finId, VinId vinId) {
        VehicleDataCard dataCard = new VehicleDataCard(true);
        dataCard.fillByModel(getProject(), modelNo);
        if (dataCard.isModelLoaded()) {
            if ((vinId != null) && vinId.isValidId()) {
                dataCard.setVin(vinId.getVIN());
            }
            if ((finId != null) && finId.isValidId()) {
                dataCard.setFinId(finId, getProject());
            }
            iPartsDataCardDialog dlg = new iPartsDataCardDialog(getConnector(), this, getDataCardMasterDialogDividerPosition());
            dlg.setDataCard(dataCard);
            if (dlg.showModal() == ModalResult.OK) {
                // Übernehmen der Datenkarte; Abgleich unnötig, da Baumuster-Datenkarte
                // und anzeigen
                initDialog(dataCard);
            }
            return true;
        } else {
            MessageDialog.showWarning(TranslationHandler.translate("!!Datenkarte konnte nicht aus Baumuster \"%1\" erzeugt werden.", modelNo));
            return false;
        }
    }

    /**
     * Laden der Aggregate-Datenkarte
     */
    private void onLoadAggregateDataCard() {
        lastFinId = null;
        filterValuesPanel.setIgnoreAggIdentChangeEvent(true);
        String newTitle = "!!Filter ändern";
        // Aggregate Typ bestimmen
        AggregateIdent aggIdent = filterValuesPanel.getAggregateIdent();
        DCAggregateTypes aggType = aggIdent.getAggType();
        List<String> messageList = new DwList<String>();
        int dataCardMasterDialogDividerPosition = mainWindow.panelLeft.getPreferredWidth() - 5; // 5px für Divider-Breite
        try {
            AggregateDataCard aggregateDataCard = loadAggregateDataCard(aggType, aggIdent.getIdent());
            if (aggregateDataCard == null) { // Warte-Dialog wurde abgebrochen
                return;
            } else if (aggregateDataCard.isDataCardLoaded()) { // Datenkarte wurde erfolgreich geladen
                // Datenkarte setzen
                iPartsDataCardDialog dlg = new iPartsDataCardDialog(getConnector(), this, dataCardMasterDialogDividerPosition);
                dlg.setDataCard(aggregateDataCard);
                if (dlg.showModal() == ModalResult.OK) {
                    // Übernehmen der DC in Filter-DC (Abgleich der SAAs, Agg-BMs und Codes mit dem Baumuster)
                    AggregateDataCard newDataCard = (AggregateDataCard)dlg.getDataCard().cloneMe();
                    AggregateDataCard modelDataCard = new AggregateDataCard(true);
                    String modelNo = newDataCard.getModelNo();
                    modelDataCard.fillByModel(getProject(), modelNo);
                    //Model- und Vehicle-Datacard zusammenführen
                    iPartsFilterHelper.adoptDCToModelDC(modelDataCard, newDataCard);
                    // und anzeigen
                    initDialog(newDataCard);
                    newTitle = TranslationHandler.translate("!!Filter ändern (Aggregat %1: %2)", TranslationHandler.translate(aggType.getDescription()),
                                                            aggIdent.getIdent());

                } else {
                    // auch beim Abbruch der Anzeige der Datenkarte wird die lastFinId übernommen
                    enableButtons();
                }
            } else {
                // Für die Lenkung gibt es keine Datenkarten ==> eine Meldung ausgeben.
                if (aggType.equals(DCAggregateTypes.STEERING)) {
                    messageList.add(TranslationHandler.translate("!!Aggregate-Datenkarten für \"%1\" existieren nicht.",
                                                                 TranslationHandler.translate(aggType.getDescription())));
                } else {
                    // keine Datenkarte gefunden
                    messageList.add(TranslationHandler.translate("!!Aggregate-Datenkarte für %1 mit Ident \"%2\" ist nicht vorhanden.",
                                                                 TranslationHandler.translate(aggType.getDescription()), aggIdent.getIdent()));
                    if (aggIdent.isModelNumberValid()) {
                        // hier versuchen ein DC aus dem Baumuster anzulegen
                        AggregateDataCard dataCard = new AggregateDataCard(true);
                        String modelNo = aggIdent.getFullModelNo();
                        dataCard.fillByModel(getProject(), modelNo);
                        if (dataCard.isModelLoaded()) {
                            messageList.add(TranslationHandler.translate("!!Aggregate-Datenkarte wird aus Baumuster \"%1\" erzeugt.", modelNo));
                            MessageDialog.show(messageList);
                            // Datenkarte setzen
                            iPartsDataCardDialog dlg = new iPartsDataCardDialog(getConnector(), this, dataCardMasterDialogDividerPosition);
                            // den Ident aus den ursprünglichen Anfrage in die Datenkarte übernehmen auch wenn er ungültig war
                            dataCard.setAggregateIdent(aggIdent.getIdent());
                            dlg.setDataCard(dataCard);
                            if (dlg.showModal() == ModalResult.OK) {
                                // Übernehmen der Datenkarte; Abgleich unnötig, da Baumuster-Datenkarte und anzeigen
                                initDialog(dataCard);
                            }
                        } else {
                            messageList.add(TranslationHandler.translate("!!Aggregate-Datenkarte konnte nicht aus Baumuster \"%1\" erzeugt werden.", modelNo));
                            MessageDialog.showWarning(messageList);
                        }
                    } else {
                        messageList.add(TranslationHandler.translate("!!Baumuster \"%1\" des Aggregats ist ebenfalls ungültig.", aggIdent.getFullModelNo()));
                        MessageDialog.showWarning(messageList);
                    }
                }
                MessageDialog.showWarning(messageList);
            }
        } catch (DataCardRetrievalException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
            messageList.add(TranslationHandler.translate("!!Fehler beim Laden der Aggregate-Datenkarte:"));
            messageList.add(e.getLocalizedMessage());
            MessageDialog.showError(messageList);
            if (aggIdent.isModelNumberValid()) {
                String modelNo = aggIdent.getFullModelNo();
                messageList.clear();
                messageList.add(TranslationHandler.translate("!!Aggregate-Datenkarte aus Baumuster \"%1\" erzeugen?", modelNo));
                if (MessageDialog.showYesNo(messageList.get(0)) == ModalResult.YES) {
                    // hier versuchen ein DC aus dem Baumuster anzulegen
                    AggregateDataCard dataCard = new AggregateDataCard(true);
                    dataCard.fillByModel(getProject(), modelNo);
                    if (dataCard.isModelLoaded()) {
                        iPartsDataCardDialog dlg = new iPartsDataCardDialog(getConnector(), this, dataCardMasterDialogDividerPosition);
                        if (aggIdent.isValidId()) {
                            dataCard.setAggregateIdent(aggIdent.getIdent());
                        }
                        dlg.setDataCard(dataCard);
                        if (dlg.showModal() == ModalResult.OK) {
                            // Übernehmen der Datenkarte; Abgleich unnötig, da Baumuster-Datenkarte und anzeigen
                            initDialog(dataCard);
                        }
                    } else {
                        messageList.clear();
                        messageList.add(TranslationHandler.translate("!!Aggregate-Datenkarte konnte nicht aus Baumuster \"%1\" erzeugt werden.", modelNo));
                        MessageDialog.showWarning(messageList);
                    }
                }
            }
        }

        mainWindow.title.setTitle(newTitle);
        filterValuesPanel.setIgnoreAggIdentChangeEvent(false);
    }

    /**
     * Lade die Fahrzeug-Datenkarte zur angegebenen FIN mit einem Warte-Dialog falls die Datenkarte nicht im Cache liegt.
     *
     * @param finId
     * @return
     * @throws DataCardRetrievalException
     */
    private VehicleDataCard loadVehicleDataCard(final FinId finId) throws DataCardRetrievalException {
        final VarParam<Boolean> cancelledVarParam = new VarParam<Boolean>(false);
        iPartsDataCardRetrievalHelper.LoadDataCardCallback loadDataCardCallback = new iPartsDataCardRetrievalHelper.LoadDataCardCallback() {
            @Override
            public void loadDataCard(final Runnable loadDataCardRunnable) {
                final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Lade Fahrzeug-Datenkarte",
                                                                               TranslationHandler.translate("!!FIN/VIN: %1", finId.getFIN()),
                                                                               null, true) {

                    @Override
                    protected void cancel(Event event) {
                        cancelledVarParam.setValue(true);
                        Session.invokeThreadSafeInSession(() -> closeWindow(ModalResult.CANCEL, false));
                    }
                };

                messageLogForm.showModal(thread -> {
                    loadDataCardRunnable.run();
                    Session.invokeThreadSafeInSession(() -> messageLogForm.closeWindow(ModalResult.OK, false));
                });
            }
        };

        // Befestigungsteile direkt mit der Datenkarte mitladen
        VehicleDataCard vehicleDataCard = iPartsDataCardRetrievalHelper.getVehicleDataCard(getProject(), finId, loadDataCardCallback, true);

        if (cancelledVarParam.getValue()) { // Laden wurde abgebrochen
            return null;
        } else {
            // Zur Unterscheidung, ob das Laden abgebrochen oder keine Datenkarte gefunden wurde, eine neue Datenkarte
            // erzeugen, wo das Flag isLoaded aber nicht gesetzt ist
            if (vehicleDataCard == null) {
                vehicleDataCard = new VehicleDataCard();
            }
            return vehicleDataCard;
        }
    }

    /**
     * Lade die Aggregate-Datenkarte zum angegebenen Typ und Ident mit einem Warte-Dialog falls die Datenkarte nicht im Cache liegt.
     *
     * @param type
     * @param ident
     * @return
     * @throws DataCardRetrievalException
     */
    private AggregateDataCard loadAggregateDataCard(final DCAggregateTypes type, final String ident) throws DataCardRetrievalException {
        final VarParam<Boolean> cancelledVarParam = new VarParam<Boolean>(false);
        iPartsDataCardRetrievalHelper.LoadDataCardCallback loadDataCardCallback = loadDataCardRunnable -> {
            final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Lade Aggregate-Datenkarte",
                                                                           TranslationHandler.translate("!!Typ: %1, Ident: %2",
                                                                                                        TranslationHandler.translate(type.getDescription()), ident),
                                                                           null, true) {

                @Override
                protected void cancel(Event event) {
                    cancelledVarParam.setValue(true);
                    Session.invokeThreadSafeInSession(() -> closeWindow(ModalResult.CANCEL, false));
                }
            };

            messageLogForm.showModal(thread -> {
                loadDataCardRunnable.run();
                Session.invokeThreadSafeInSession(() -> messageLogForm.closeWindow(ModalResult.OK, false));
            });
        };

        // Aggregatedatenkarte laden
        AggregateDataCard aggregateDataCard = iPartsDataCardRetrievalHelper.getAggregateDataCard(getProject(), type, ident, loadDataCardCallback);

        if (cancelledVarParam.getValue()) { // Laden wurde abgebrochen
            return null;
        } else {
            // Zur Unterscheidung, ob das Laden abgebrochen oder keine Datenkarte gefunden wurde, eine neue Datenkarte
            // erzeugen, wo das Flag isLoaded aber nicht gesetzt ist
            if (aggregateDataCard == null) {
                aggregateDataCard = new AggregateDataCard();
            }

            return aggregateDataCard;
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
        private de.docware.framework.modules.gui.controls.GuiPanel panelLeft;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelForFilterValues;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setName("FilterDialog");
            this.setWidth(1020);
            this.setHeight(700);
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
            title.setTitle("!!Filter ändern");
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
            panelLeft = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelLeft.setName("panelLeft");
            panelLeft.__internal_setGenerationDpi(96);
            panelLeft.registerTranslationHandler(translationHandler);
            panelLeft.setScaleForResolution(true);
            panelLeft.setMinimumWidth(100);
            panelLeft.setMinimumHeight(10);
            panelLeft.setTitle("!!Aktive Filter");
            de.docware.framework.modules.gui.layout.LayoutBorder panelLeftLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelLeft.setLayout(panelLeftLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelLeftConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelLeftConstraints.setPosition("west");
            panelLeft.setConstraints(panelLeftConstraints);
            panelMain.addChild(panelLeft);
            panelForFilterValues = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelForFilterValues.setName("panelForFilterValues");
            panelForFilterValues.__internal_setGenerationDpi(96);
            panelForFilterValues.registerTranslationHandler(translationHandler);
            panelForFilterValues.setScaleForResolution(true);
            panelForFilterValues.setMinimumWidth(10);
            panelForFilterValues.setMinimumHeight(10);
            panelForFilterValues.setTitle("!!Filterwerte");
            de.docware.framework.modules.gui.layout.LayoutBorder panelForFilterValuesLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelForFilterValues.setLayout(panelForFilterValuesLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelForFilterValuesConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelForFilterValues.setConstraints(panelForFilterValuesConstraints);
            panelMain.addChild(panelForFilterValues);
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
                    buttonOkClick(event);
                }
            });
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonCancelActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    buttonCancelClick(event);
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