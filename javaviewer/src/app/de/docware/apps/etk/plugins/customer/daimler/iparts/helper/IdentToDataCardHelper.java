/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.FilterChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.VinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;

/**
 * Hilfsklasse, um für einen Ident (FIN/VIN/Baumuster) die passende Datenkarte zu erzeugen und alle passenden Filter zu aktivieren.
 */
public class IdentToDataCardHelper {

    /**
     * Erzeugt die Datenkarte für den übergebenen Ident (FIN/VIN/Baumuster) falls möglich mit Laden der Fahrzeug-Datenkarte
     * und aktiviert alle passenden Filter. Ohne neue gültige (Baumuster-)Datenkarte bleibt eine evtl. aktive Filterung bestehen.
     *
     * @param ident
     * @param connector
     * @return
     */
    public static AbstractDataCard activateFilterForIdentWithDataCard(String ident, AbstractJavaViewerFormIConnector connector) {
        EtkProject project = connector.getProject();
        AbstractDataCard dataCard = createDataCardForIdent(ident, project);
        if (dataCard != null) {
            iPartsFilter.get().setAllRetailFilterActiveForDataCard(project, dataCard, true);
            project.fireProjectEvent(new FilterChangedEvent());
            iPartsPlugin.updateFilterButton(connector, null);
        }
        return dataCard;
    }

    /**
     * Erzeugt die Datenkarte für den übergebenen Ident (FIN/VIN/Baumuster) falls möglich mit Laden der Fahrzeug-Datenkarte.
     *
     * @param ident
     * @param project
     * @return
     */
    public static AbstractDataCard createDataCardForIdent(String ident, EtkProject project) {
        AbstractDataCard dataCard = null;
        if ((new FinId(ident).isValidId()) || (new VinId(ident).isValidId())) { // FIN oder VIN
            dataCard = loadVehicleDataCard(ident, project);
            if ((dataCard != null) && !dataCard.isDataCardLoaded() && StrUtils.isValid(dataCard.getModelNo())) {
                MessageDialog.showWarning(TranslationHandler.translate("!!Datenkarte für FIN/VIN \"%1\" ist nicht vorhanden.",
                                                                       ident) + "\n"
                                          + TranslationHandler.translate("!!Datenkarte wird aus Baumuster \"%1\" erzeugt.",
                                                                         dataCard.getModelNo()),
                                          "!!Datenkarte");
            }
        } else if (iPartsModelId.isModelNumberValid(ident, true)) { // Baumuster
            dataCard = createModelDataCard(ident, project);
        } else {
            MessageDialog.showWarning("!!Ungültige FIN/VIN/Baumuster.", "!!Datenkarte");
        }

        if ((dataCard != null) && !dataCard.isDataCardLoaded() && !dataCard.isModelLoaded()) {
            if (StrUtils.isValid(dataCard.getModelNo())) {
                MessageDialog.showWarning(TranslationHandler.translate("!!Baumuster nicht gefunden: %1", dataCard.getModelNo()),
                                          "!!Datenkarte");
            } else {
                MessageDialog.showWarning("!!Ungültige FIN/VIN/Baumuster.", "!!Datenkarte");
            }
            return null;
        }
        return dataCard;
    }

    /**
     * Lade die Fahrzeug-Datenkarte zur angegebenen FIN/VIN mit Fallback auf Baumuster-Datenkarte mit einem Warte-Dialog.
     *
     * @param finOrVin
     * @return
     * @throws DataCardRetrievalException
     */
    public static VehicleDataCard loadVehicleDataCard(final String finOrVin, EtkProject project) {
        final VarParam<Boolean> cancelledVarParam = new VarParam<>(false);
        iPartsDataCardRetrievalHelper.LoadDataCardCallback loadDataCardCallback = loadDataCardRunnable -> {
            final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Lade Fahrzeug-Datenkarte",
                                                                           TranslationHandler.translate("!!FIN/VIN: %1", finOrVin),
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

        // Befestigungsteile direkt mit der Datenkarte mitladen
        boolean tryToCreateModelDataCard = false;
        VehicleDataCard vehicleDataCard = null;
        try {
            vehicleDataCard = VehicleDataCard.getVehicleDataCard(finOrVin, false, true, true, loadDataCardCallback,
                                                                 project, true);
        } catch (DataCardRetrievalException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
            MessageDialog.showError(TranslationHandler.translate("!!Fehler beim Laden der Datenkarte:") + " " + e.getMessage(),
                                    "!!Lade Fahrzeug-Datenkarte");
            tryToCreateModelDataCard = true;
        }

        if (!cancelledVarParam.getValue()) { // Laden wurde abgebrochen
            if (tryToCreateModelDataCard) {
                try {
                    return VehicleDataCard.getVehicleDataCard(finOrVin, false, false, true, null, project, true);
                } catch (DataCardRetrievalException e) {
                    // Kann hier gar nicht kommen
                }
            } else {
                return vehicleDataCard;
            }
        }
        return null;
    }

    /**
     * Erzeuge die Datenkarte zum angegebenen Baumuster.
     *
     * @param modelNumber
     * @return
     * @throws DataCardRetrievalException
     */
    private static AbstractDataCard createModelDataCard(final String modelNumber, EtkProject project) {
        try {
            if (iPartsModel.isVehicleModel(modelNumber)) {
                return VehicleDataCard.getVehicleDataCard(modelNumber, false, false, true, null, project, true);
            } else if (iPartsModel.isAggregateModel(modelNumber)) {
                return AggregateDataCard.getAggregateDataCard(null, modelNumber, false, true, null, project);
            }
        } catch (DataCardRetrievalException e) {
            // Kann hier gar nicht kommen
        }
        return null;
    }
}