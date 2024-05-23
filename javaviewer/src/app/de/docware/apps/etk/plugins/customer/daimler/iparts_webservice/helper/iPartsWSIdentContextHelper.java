/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.AggregateIdent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.DatacardIdentOrderTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.TimeSpanLogger;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint.throwResourceNotFoundError;

/**
 * Hilfsklasse für das Erstellen und Füllen von IdentContext Objekten für die  Webservices.
 */
public class iPartsWSIdentContextHelper {

    /**
     * Überprüft, ob der übergebene <i>identContext</i> gültig ist,
     * wobei er auch fehlen oder leer sein darf, wenn <i>requireFinOrVin</i> NICHT gesetzt ist.
     * DAIMLER-10034, Falls es sich um eine Anfrage von einem RMI Typzulassung Service handelt, darf nur mit einer
     * gültigen FIN oder VIN auf die Daten zugegriffen werden. In diesem Fall muss die Datenkarte auch existieren.
     *
     * @param identContext
     */
    public static void checkIfIdentContextValid(iPartsWSIdentContext identContext) {

        // Nur prüfen, wenn FIN oder VIN vorhanden sein müssen.
        if (iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI()) {
            // Ohne Context oder es existiert die Datenkarte nicht (oder ver VIS antwortete nicht)
            // ODER es ist weder die FIN noch die VIN gültig, dann wirf' einen Fehler!
            if ((identContext == null)
                || (!identContext.isDatacardExists())
                || (StrUtils.isEmpty(identContext.getFin(), identContext.getVin()))) {
                throwResourceNotFoundError("Datacard not found");
            }
        }
    }


    /**
     * Befüllt den übergebenen Fahrzeug {@link iPartsWSIdentContext} mit den IdentContext der verknüpften Aggregate aus
     * der Datenkarte
     *
     * @param project
     * @param vehicleIdentContext
     * @param vehicleDataCard
     * @param userInfo
     * @param includeValidities
     */
    public static void fillVehicleWithAggregatesFromDatacard(EtkProject project, iPartsWSIdentContext vehicleIdentContext,
                                                             VehicleDataCard vehicleDataCard, iPartsWSUserInfo userInfo, boolean includeValidities) {
        List<iPartsWSIdentContext> aggregateIdentContexts = new ArrayList<iPartsWSIdentContext>();
        Collection<String> vehicleASProductClassIds = vehicleIdentContext.getProductClassIds();
        // Ist keine AS Produktklasse am Fahrzeug angegeben können auch keine dazu passenden Aggregate bestimmt werden
        if ((vehicleASProductClassIds != null) && !vehicleASProductClassIds.isEmpty()) {
            for (AggregateDataCard aggregate : vehicleDataCard.getActiveAggregates()) {
                addToAggregateIdentContexts(aggregateIdentContexts, aggregate, aggregate.getModelNo(), vehicleASProductClassIds, project, userInfo, includeValidities);
            }

            if (!aggregateIdentContexts.isEmpty()) {
                vehicleIdentContext.setAggregates(aggregateIdentContexts);
            }
            // Attribute die vom VIS Service weitergegeben werden
            vehicleIdentContext.setFixingPartsAvailable(vehicleDataCard.isFixingPartsAvailable());
            vehicleIdentContext.setProdOrderTextAvailable(vehicleDataCard.isProdOrderTextAvailable());
            vehicleIdentContext.setFieldOrganisationTextAvailable(vehicleDataCard.isFieldOrganisationTextAvailable());
        }
    }

    /**
     * Befüllt den übergebenen Fahrzeug {@link iPartsWSIdentContext} mit den IdentContext der verknüpften Aggregate aus
     * der Datenbank
     *
     * @param project
     * @param vehicleIdentContext
     * @param modelNumber
     * @param userInfo
     */
    public static void fillVehicleWithAggregatesFromDB(EtkProject project, iPartsWSIdentContext vehicleIdentContext,
                                                       String modelNumber, iPartsWSUserInfo userInfo) {
        // Aggregate-IdentContexte bestimmen (über Tabelle DA_MODELS_AGGS für Fahrzeug-Baumuster zu Aggregate-Baumuster)
        DBDataObjectAttributesList modelAggsAttributesList = project.getDbLayer().getAttributesList(iPartsConst.TABLE_DA_MODELS_AGGS,
                                                                                                    new String[]{ iPartsConst.FIELD_DMA_AGGREGATE_NO },
                                                                                                    new String[]{ iPartsConst.FIELD_DMA_MODEL_NO },
                                                                                                    new String[]{ modelNumber });
        if (!modelAggsAttributesList.isEmpty()) {
            Collection<String> vehicleASProductClassIds = vehicleIdentContext.getProductClassIds();
            // Ist keine AS Produktklasse am Fahrzeug angegeben können auch keine dazu passenden Aggregate bestimmt werden
            if ((vehicleASProductClassIds != null) && !vehicleASProductClassIds.isEmpty()) {
                List<iPartsWSIdentContext> aggregateIdentContexts = new ArrayList<iPartsWSIdentContext>(modelAggsAttributesList.size());
                for (DBDataObjectAttributes attributes : modelAggsAttributesList) {
                    String aggregateModelNumber = attributes.getField(iPartsConst.FIELD_DMA_AGGREGATE_NO).getAsString();
                    addToAggregateIdentContexts(aggregateIdentContexts, null, aggregateModelNumber, vehicleASProductClassIds, project, userInfo, false);
                }
                if (!aggregateIdentContexts.isEmpty()) {
                    vehicleIdentContext.setAggregates(aggregateIdentContexts);
                }
            }
        }
    }

    /**
     * Liste der iPartsWSIdentContext um Liste der iPartsWSIdentContext eines Aggregats ergänzen.
     * Dabei werden die mit dem Aggregat verknüpften Produkte bestimmt und für gültige Produkte jeweils ein iPartsWSIdentContext erzeugt.
     *
     * @param aggregateIdentContexts   wird hier weiter befüllt
     * @param aggregate                wenn null wird iPartsWSIdentContext aus Aggregate-Baumuster ermittelt
     * @param aggModelNo
     * @param vehicleASProductClassIds AS Produktklasse des Aggregates muss mit der des Fahrzeuges überlappen
     * @param project
     * @param userInfo
     * @param includeValidities
     */
    private static void addToAggregateIdentContexts(List<iPartsWSIdentContext> aggregateIdentContexts, AggregateDataCard aggregate,
                                                    String aggModelNo, Collection<String> vehicleASProductClassIds,
                                                    EtkProject project, iPartsWSUserInfo userInfo, boolean includeValidities) {
        List<iPartsProduct> aggregateProducts = iPartsProductHelper.getProductsForModelAndSessionType(project, new iPartsModelId(aggModelNo),
                                                                                                      null, iPartsProductHelper.PRODUCTS_FOR_SERIES_MODE.FILTER_ALL);
        for (iPartsProduct aggregateProduct : aggregateProducts) {
            if (isPermittedAggregateProduct(aggregateProduct, vehicleASProductClassIds, userInfo, project, null)) {
                // productId und aggTypeId müssen befüllt werden können
                if (StrUtils.isValid(aggregateProduct.getAsId().getProductNumber(), aggregateProduct.getAggregateType())) {
                    // IdentContexte erstellen und dem Ergebnis hinzufügen
                    iPartsWSIdentContext aggregateIdentContext;

                    if (aggregate != null) {
                        iPartsModel aggregateModel = iPartsModel.getInstance(project, new iPartsModelId(aggregate.getModelNo()));
                        aggregateIdentContext = new iPartsWSIdentContext(aggregate, aggregateModel, aggregateProduct, userInfo, project, includeValidities);
                    } else {
                        iPartsModel aggregateModel = iPartsModel.getInstance(project, new iPartsModelId(aggModelNo));
                        aggregateIdentContext = new iPartsWSIdentContext(aggregateModel, aggregateProduct, false, userInfo, project, true);
                    }
                    aggregateIdentContexts.add(aggregateIdentContext);
                }
            }
        }
    }


    /**
     * Liefert die Fahrzeug-Datenkarte für diesen {@link iPartsWSIdentContext} zurück (entweder eine echte Datenkarte über den Webservice
     * wenn eine FIN/VIN vorhanden und {@code #datacardExists} gesetzt ist oder bei gesetztem Flag <i>createModelDataCard</i>
     * auch eine Baumuster-Datenkarte für das Baumuster, wenn keine Datenkarte für die FIN/VIN existiert oder keine FIN/VIN dafür
     * aber ein Baumuster mit Sachnummernkennbuchstabe im {@link iPartsWSIdentContext} vorhanden ist)
     *
     * @param returnForbiddenDataCard Gibt an, ob auch die Datenkarte für ein gestohlenes/verschrottetes Fahrzeug zurückgeliefert
     *                                werden soll:
     *                                - Bei {@code false} altes Verhalten mit optional BM-Datenkarte erzeugen etc.
     *                                - Bei {@code true } neues Verhalten mit Zurückliefern der gefundenen Datenkarte;
     *                                ob sie weiter verarbeitet werden soll, kann die aufrufende Funktion entscheiden
     * @param createModelDataCard
     * @param finOrVin
     * @param modelNumber
     * @param datacardExists
     * @param logPrefix               Präfix für die Performance-Logausgabe
     * @param project
     * @param loadFixingParts         Bestimmt ob Befestigungsteile mit der Datenkarte initial geladen werden sollen
     * @return
     */
    public static VehicleDataCard getVehicleDataCard(boolean returnForbiddenDataCard,
                                                     boolean createModelDataCard, String finOrVin,
                                                     String modelNumber, boolean datacardExists, String logPrefix, EtkProject project,
                                                     boolean loadFixingParts) throws DataCardRetrievalException {
        String identCode;
        TimeSpanLogger getDataCardLogger = null;
        if (!StrUtils.isEmpty(finOrVin)) {
            identCode = finOrVin;
            if (datacardExists) {
                getDataCardLogger = new TimeSpanLogger(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.DEBUG,
                                                       logPrefix + ".getVehicleDataCardFromWebservice(" + finOrVin
                                                       + ") for session " + Session.get().getId(), -1, false);
            }
        } else {
            identCode = modelNumber;
        }

        final VarParam<Boolean> dataCardLoadedUsingWebservice = new VarParam<>(false);
        iPartsDataCardRetrievalHelper.LoadDataCardCallback loadDataCardCallback = new iPartsDataCardRetrievalHelper.LoadDataCardCallback() {
            @Override
            public void loadDataCard(Runnable loadDataCardRunnable) {
                dataCardLoadedUsingWebservice.setValue(true);
                loadDataCardRunnable.run();
            }
        };

        try {
            VehicleDataCard vehicleDataCard = VehicleDataCard.getVehicleDataCard(identCode, returnForbiddenDataCard, datacardExists, createModelDataCard,
                                                                                 loadDataCardCallback, project, loadFixingParts);
            if (vehicleDataCard.isDataCardLoaded() || vehicleDataCard.isModelLoaded()) {
                return vehicleDataCard;
            }
        } finally {
            if ((getDataCardLogger != null) && dataCardLoadedUsingWebservice.getValue()) {
                getDataCardLogger.logFinal();
            }
        }

        return null;
    }

    /**
     * Liefert die Aggregate-Datenkarte für diesen {@link iPartsWSIdentContext} zurück (entweder eine echte Datenkarte über den Webservice
     * wenn ein Aggregate-Ident vorhanden und {@code #datacardExists} gesetzt ist oder bei gesetztem Flag <i>createModelDataCard</i>
     * auch eine Baumuster-Datenkarte für das Baumuster, wenn keine Datenkarte für den Aggregate-Ident existiert oder kein
     * Aggregate-Ident dafür aber ein Baumuster mit Sachnummernkennbuchstabe im {@link iPartsWSIdentContext} vorhanden ist)
     *
     * @param createModelDataCard
     * @param dcAggregateType
     * @param aggregateIdent
     * @param modelNumber
     * @param datacardExists
     * @param logPrefix           Präfix für die Performance-Logausgabe
     * @param project
     * @return
     */
    public static AggregateDataCard getAggregateDataCard(boolean createModelDataCard, DCAggregateTypes dcAggregateType,
                                                         String aggregateIdent, String modelNumber, boolean datacardExists,
                                                         String logPrefix, EtkProject project) throws DataCardRetrievalException {
        String identCode;

        boolean visCallValid = datacardExists;
        if (dcAggregateType != null) {
            visCallValid = datacardExists && DatacardIdentOrderTypes.getIdentOrderTypeByAggregateTypes(dcAggregateType).isVIScallAllowed();
        }
        TimeSpanLogger getDataCardLogger = null;
        if (!StrUtils.isEmpty(aggregateIdent)) {
            if ((dcAggregateType != null) && (dcAggregateType != DCAggregateTypes.VEHICLE) && (dcAggregateType != DCAggregateTypes.UNKNOWN)) {
                identCode = aggregateIdent;
                if (visCallValid) {
                    getDataCardLogger = new TimeSpanLogger(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.DEBUG,
                                                           logPrefix + ".getAggregateDataCardFromWebservice(" + identCode
                                                           + ") for session " + Session.get().getId(), -1, false);
                }
            } else {
                identCode = AggregateIdent.getModelFromIdent(aggregateIdent);
                visCallValid = false;
            }
        } else {
            identCode = modelNumber;
        }

        final VarParam<Boolean> dataCardLoadedUsingWebservice = new VarParam<Boolean>(false);
        iPartsDataCardRetrievalHelper.LoadDataCardCallback loadDataCardCallback = new iPartsDataCardRetrievalHelper.LoadDataCardCallback() {
            @Override
            public void loadDataCard(Runnable loadDataCardRunnable) {
                dataCardLoadedUsingWebservice.setValue(true);
                loadDataCardRunnable.run();
            }
        };

        try {
            AggregateDataCard aggregateDataCard = AggregateDataCard.getAggregateDataCard(dcAggregateType, identCode, visCallValid,
                                                                                         createModelDataCard, loadDataCardCallback,
                                                                                         project);
            if (aggregateDataCard.isDataCardLoaded() || aggregateDataCard.isModelLoaded()) {
                return aggregateDataCard;
            }
        } finally {
            if ((getDataCardLogger != null) && dataCardLoadedUsingWebservice.getValue()) {
                getDataCardLogger.logFinal();
            }
        }

        return null;
    }


    /**
     * Ist die Anzeige des mit dem Aggregat verbundenen Produkt erlaubt?
     *
     * @param aggregateProduct
     * @param vehicleASProductClassIds Produktklassen des Fahrzeugs welches das Aggregat enthält
     * @param userInfo
     * @param project
     * @param permissionErrorDetected  Rückgabewert; false wenn die Token Gültigkeiten verletzt wurden
     *                                 (wird benötigt um später eine entsprechende Exception zu werfen)
     * @return
     */
    public static boolean isPermittedAggregateProduct(iPartsProduct aggregateProduct, Collection<String> vehicleASProductClassIds,
                                                      iPartsWSUserInfo userInfo, EtkProject project, VarParam<Boolean> permissionErrorDetected) {
        boolean onlyRetailRelevantProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();
        boolean validForPermissions = aggregateProduct.isValidForPermissions(project, userInfo.getCountryForValidation(), userInfo.getPermissionsAsMapForValidation());
        if (permissionErrorDetected != null) { // Rückgabewert interessiert hier nicht
            permissionErrorDetected.setValue(!validForPermissions);
        }

        return ((!onlyRetailRelevantProducts || aggregateProduct.isRetailRelevant()) && validForPermissions
                && (aggregateProduct.isOneProductClassValid(vehicleASProductClassIds)));
    }


}
