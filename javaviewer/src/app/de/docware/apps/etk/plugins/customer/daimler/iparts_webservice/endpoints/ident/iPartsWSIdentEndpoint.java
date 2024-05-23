/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsWSError;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.AggregateIdent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.VinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.iPartsNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSIdentContextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Endpoint für den Ident-Webservice
 */
public class iPartsWSIdentEndpoint extends iPartsWSAbstractEndpoint<iPartsWSIdentRequest> {

    public static final String DEFAULT_ENDPOINT_URI = "/ident/Ident";

    private String identCodeAttributeName = "identCode";

    public iPartsWSIdentEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    public iPartsWSIdentResponse executeWebservice(EtkProject project, iPartsWSIdentRequest requestObject) {
        return executeWebservice(project, requestObject, false);
    }

    private iPartsWSIdentResponse executeWebservice(EtkProject project, iPartsWSIdentRequest requestObject, boolean forceVIN) {
        iPartsWSIdentResponse response = new iPartsWSIdentResponse();
        List<iPartsWSIdentContext> identContexts = new DwList<>();
        String identCode = requestObject.getIdentCode();

        // Zunächst identCode auf FIN überprüfen, dann auf VIN, dann auf Baumuster, zuletzt auf Aggregate-Ident mit und ohne Aggregatetyp
        FinId finId = new FinId(identCode);
        VinId vinId = null;
        boolean finIsValid = false;
        boolean onlyVinIsValid = false;
        List<String> modelNumbers = new DwList<>();
        AbstractDataCard dataCard = null;
        boolean isAggregateIdent = false;
        DCAggregateTypes dcAggregateType = null;
        int identLength = identCode.length();
        DataCardRetrievalException dataCardRetrievalException = null;
        String aggTypeId = requestObject.getAggTypeId();

        // Abhängig von der Länge vom identCode verschiedene Überprüfungen durchführen, um die relevanten Baumuster zu ermitteln
        if ((identLength == 6) || (identLength == 9)) {
            // 1. Baumuster (ohne C/D) überprüfen
            if (iPartsModelId.isModelNumberValid(identCode, false)) {
                modelNumbers.add(identCode);
            }
        } else if (identLength == 7) {
            // 2. Baumuster (mit C/D) überprüfen
            if (iPartsModelId.isModelNumberValid(identCode, true)) {
                modelNumbers.add(identCode);
            } else {
                // 3. VIN-Fallback überprüfen
                modelNumbers = iPartsVINModelMappingCache.getInstance(project).getVisibleModelsForVINPrefix(project, identCode);
                if (modelNumbers.isEmpty()) { // Explizite Fehlermeldung für nicht verfügbarem VIN-Fallback
                    throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "No VIN model fallback available for '" + identCodeAttributeName + "': "
                                                                          + identCode, null);
                    return null;
                }
            }
        } else if (identLength == 10) {
            // 4. Aggregate-Baumuster (mit D) überprüfen
            if (iPartsModelId.isModelNumberValid(identCode, true)) {
                modelNumbers.add(identCode);
            }
        } else if (((identLength >= 12) && (identLength <= 15)) || ((DCAggregateTypes.getDCAggregateTypeByAggregateType(aggTypeId) == DCAggregateTypes.PLATFORM) // Ident für PLATFORM ist kürzer
                                                                    && (identLength >= 6))) {
            // 5. Aggregate-Ident prüfen (für einen Aggregate-Ident muss der IdentCode 12-15 Zeichen lang sein: 6 Zeichen
            // für Baumuster und mindestens 6 Zeichen für Endnummer und andere Daten)
            isAggregateIdent = true;
            if (!StrUtils.isEmpty(aggTypeId)) {
                dcAggregateType = DCAggregateTypes.getDCAggregateTypeByAggregateType(aggTypeId);
                if ((dcAggregateType == DCAggregateTypes.VEHICLE) || (dcAggregateType == DCAggregateTypes.UNKNOWN)) {
                    throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Attribute 'aggTypeId' contains invalid aggregate type: "
                                                                          + aggTypeId, null);
                    return null;
                }
            }

            String modelNumber = AggregateIdent.getModelFromIdent(identCode);
            modelNumbers.add(modelNumber);
            if (!iPartsModelId.isModelNumberValid(modelNumber, false)) {
                throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, "Model number is invalid in attribute '" + identCodeAttributeName
                                                                      + "' (first 6 digits): " + modelNumber, null);
                return null;
            }
        } else if (identLength == 17) {
            // 6. FIN überprüfen
            if (finId.isValidId() && !forceVIN) {
                finIsValid = true;
                // Um auf Nummer sicher zu gehen, bestimmen wir das BM aus der Datenkarte, weil es VINs gibt, die als
                // gültige FINs erkannt werden und bei diesen dann das extrahierte BM unter Umständen auch valide ist!
                // Auf dem normalen Weg wird die Datenkarte bei einem gültigen (falschen) BM weiter unten geladen. Jetzt
                // wird die Datenkarte hier geladen inkl. richtigen BM. Das Laden weiter unten wird übersprungen, weil die
                // Datenkarte ja schon hier geladen wurde.
                // -> DAIMLER-16113
                String modelNoFromDB = finId.getFullModelNumber();
                try {
                    dataCard = iPartsWSIdentContextHelper.getVehicleDataCard(true, false, identCode, modelNoFromDB, true, getClass().getSimpleName(),
                                                                             project, false);
                } catch (DataCardRetrievalException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
                    dataCardRetrievalException = e;
                }
                // Das BM aus der Datenkarte setzen oder als Fallback, das BM aus der FIN
                if ((dataCard != null) && dataCard.isDataCardLoaded()) {
                    modelNumbers.add(dataCard.getModelNo());
                } else {
                    // Falls die Datenkarte nicht bestimmt werden konnte -> Bestimmung BM wie bisher
                    modelNumbers.add(modelNoFromDB);
                }
            } else {
                // 7. VIN überprüfen
                vinId = new VinId(identCode);
                if (vinId.isValidId()) {
                    try {
                        // Neues Verhalten bzgl. gestohlener und verschrotteter Fahrzeuge mit Fehlerbehandlung
                        dataCard = iPartsWSIdentContextHelper.getVehicleDataCard(true, false, identCode, "", true, getClass().getSimpleName(),
                                                                                 project, false);
                    } catch (DataCardRetrievalException e) {
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
                        dataCardRetrievalException = e;
                    }

                    if ((dataCard != null) && !checkIsDataCardValid(dataCard)) {
                        return null;
                    }

                    if ((dataCard != null) && dataCard.isDataCardLoaded()) {
                        finId = ((VehicleDataCard)dataCard).getFinId();
                        modelNumbers.add(dataCard.getModelNo());
                        finIsValid = true;
                    } else { // Fehlermeldung falls die Datenkarte nicht geladen werden konnte

                        // DAIMLER-10034, Falls es sich um eine Anfrage von einem RMI Typzulassung Service handelt, darf nur mit einer
                        // gültigen FIN oder VIN auf die Daten zugegriffen werden.
                        // Kein Fallback auf Baumuster zulässig!
                        if (iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI()) {
                            throwResourceNotFoundError("Datacard not found");
                        }

                        // Noch das Fallback Baumuster zur VIN versuchen zu ermitteln.
                        modelNumbers = iPartsVINModelMappingCache.getInstance(project).getVisibleModelsForVINPrefix(project, vinId.getVIN());
                        onlyVinIsValid = true;

                        // Jetzt gibt es auch keine Fallback-Baumuster zur VIN ==> Debug-Meldung und raus
                        if ((modelNumbers == null) || modelNumbers.isEmpty()) {
                            String logMsg = "No datacard or fallback models found for VIN: " + identCode;
                            Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, logMsg);
                            throwBadRequestError(WSError.REQUEST_PARAMETER_WRONG, logMsg, null);
                            return null;
                        }
                    }
                }
            }
        }

        // DAIMLER-10033 Falls es sich um eine Anfrage von einem RMI Typzulassung Service handelt, darf nur mit einer
        // gültigen FIN oder VIN auf die Daten zugegriffen werden
        // Kein Baumuster Fallback bei gestohlener und verschrotteter Fahrzeuge
        if (iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI()) {
            if (!finIsValid) {
                throwError(HttpConstants.HTTP_STATUS_NOT_FOUND, WSError.REQUEST_OBJECT_NOT_FOUND, "Datacard not found", null);
            }
        }

        // Harte Prüfung auf existierende Rechte notwendig, bei aktiviertem Permission Check
        Map<String, Set<String>> userPermissionsMap = null;
        if (iPartsWebservicePlugin.isCheckTokenPermissions()) {
            if (requestObject.getUser().getPermissions() == null) {
                throwPermissionsError();
            } else {
                userPermissionsMap = requestObject.getUser().getPermissionsAsMapForValidation();
            }
        }

        // Jetzt identCode auf gültiges 6-stelliges Baumuster (optional mit Sachnummernkennbuchstabe) überprüfen
        if (checkModels(modelNumbers)) { // eigentlich sollten hier nur noch gültige Baumusternummern ankommen
            List<iPartsModel> modelList = new DwList<>();
            for (String modelNumber : modelNumbers) {
                if (finIsValid || onlyVinIsValid) {
                    // Bei gültiger FIN handelt es sich um ein Fahrzeug-Baumuster und modelNumber hat keinen Sachnummernkennbuchstaben
                    // Bei gültiger VIN (ohne gültige FIN) handelt es sich um ein oder mehrere Fallback-Fahrzeug-Baumuster (mit Sachnummernkennbuchstaben)
                    modelList.add(iPartsModel.getInstance(project, new iPartsModelId(modelNumber)));

                    // Datenkarte für Auto-Product-Select laden (nicht laden, wenn nur die VIN gültig ist)
                    if ((dataCard == null) && !onlyVinIsValid) {
                        try {
                            // Neues Verhalten bzgl. gestohlener und verschrotteter Fahrzeuge mit Fehlerbehandlung
                            dataCard = iPartsWSIdentContextHelper.getVehicleDataCard(true, false, finId.getFIN(), modelNumber, true,
                                                                                     getClass().getSimpleName(), project, false);
                        } catch (DataCardRetrievalException e) {
                            // Hier kann man nur einmal reinkommen, weil es nur für den VIN-Baumuster-Fallback mehrere
                            // Baumuster gibt. In diesem Fall ist dann aber onlyVinIsValid gesetzt, so dass dieser Code
                            // nicht durchlaufen wird.
                            dataCardRetrievalException = e;
                        }

                        if ((dataCard != null) && !checkIsDataCardValid(dataCard)) {
                            return null;
                        }
                    }
                } else {
                    if (iPartsModelId.isModelNumberValid(modelNumber, true)) { // Baumuster mit Sachnummernkennbuchstabe
                        modelList.add(iPartsModel.getInstance(project, new iPartsModelId(modelNumber)));
                    } else { // Baumuster ohne Sachnummernkennbuchstabe -> nach Baumuster suchen
                        iPartsDataModelList dataModels = iPartsDataModelList.loadForModelNumberWithoutPrefix(project, modelNumber);
                        for (iPartsDataModel dataModel : dataModels) {
                            modelList.add(iPartsModel.getInstance(project, dataModel.getAsId(), dataModel.getAttributes()));
                        }
                    }
                }
            }

            boolean onlyRetailRelevantProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();
            boolean checkModelVisibility = iPartsPlugin.isCheckModelVisibility();
            List<String> dataBaseFallbackLanguages = project.getDataBaseFallbackLanguages();
            for (iPartsModel model : modelList) {
                if (!model.existsInDB()) {
                    continue;
                }

                if (checkModelVisibility) {
                    // Abwärtskompatibilität für nicht gefüllte Felder, was "ja" entsprechen soll
                    if (!model.isModelVisible()) {
                        continue;
                    }
                }


                iPartsWSUserInfo userInfo = requestObject.getUser();
                String language = userInfo.getLanguage();
                setCurrentDatabaseLanguageAndFallbackLanguages(project, userInfo);

                String productNumber = requestObject.getProductId();
                // Alle Produkte zum Baumuster bestimmen.
                List<iPartsProduct> products = iPartsProductHelper.getProductsForModelAndSessionType(project, model.getModelId(),
                                                                                                     null, iPartsProductHelper.PRODUCTS_FOR_SERIES_MODE.FILTER_ALL);

                // Auto-Product-Select
                if (finIsValid) {
                    products = iPartsFilterHelper.getAutoSelectProductsForFIN(project, products, finId, model.getModelId(),
                                                                              (dataCard != null) ? dataCard.getCodes().getAllCheckedValues() : null);
                }

                if (!products.isEmpty()) {
                    List<iPartsProduct> relevantProducts = new DwList<>();
                    Set<DCAggregateTypes> relevantDCAggregateTypes = new HashSet<>();
                    // Flag, ob bei einem der getesteten Produkte die Berechtigungsprüfung fehlgeschlagen ist
                    boolean permissionErrorDetected = false;
                    for (iPartsProduct product : products) {
                        if (onlyRetailRelevantProducts && !product.isRetailRelevant()) {
                            continue;
                        }
                        // Token Gültigkeitsprüfung
                        boolean validForPermissions = product.isValidForPermissions(project, userInfo.getCountryForValidation(),
                                                                                    userPermissionsMap);
                        permissionErrorDetected |= !validForPermissions;
                        if (!validForPermissions) {
                            continue;
                        }

                        if (product.isOneProductClassValid(requestObject.getProductClassIds())
                            && ((productNumber == null) || product.getAsId().getProductNumber().equals(productNumber))) {
                            // Bei Aggregate-Idents auch noch prüfen, ob der Aggregate-Typ vom Produkt ähnlich zu dem aus dem
                            // Request ist falls er dort enthalten war
                            if (isAggregateIdent) {
                                DCAggregateTypes dcAggregateTypeFromProduct = DCAggregateTypes.getDCAggregateTypeByAggregateType(product.getAggregateType());
                                if ((dcAggregateType != null) && (dcAggregateType != dcAggregateTypeFromProduct)) {
                                    continue;
                                }
                                relevantDCAggregateTypes.add(dcAggregateTypeFromProduct);
                            }
                            relevantProducts.add(product);
                        }
                    }
                    // Es hätte Produkte gegeben, aber es wurden alle wegen ungültiger Permissions ausgefiltert
                    if ((!products.isEmpty() && relevantProducts.isEmpty()) && permissionErrorDetected) {
                        // hier wird abgebrochen, d.h. wenn es in Aggregaten zu einem der Produkte Treffer gegeben hätte,
                        // aber der Benutzer keine Berechtigung für das Fahrzeugprodukt hat, werden diese auch nicht mit
                        // ausgegeben. Zusätzlich wird ausgegeben, welche Brands und Branches gültig gewesen wären
                        Set<String> missingPermissions = products.stream()
                                .flatMap(product -> product.getAllValidPermissions(project).stream())
                                .collect(Collectors.toCollection(TreeSet::new));
                        if (missingPermissions.isEmpty()) {
                            throwPermissionsError();
                        } else {
                            throwMissingPermissionsError(missingPermissions);
                        }
                    }
                    if (!relevantProducts.isEmpty()) {
                        if (isAggregateIdent) {
                            // Muss der Aggregatetyp noch bestimmt werden und ist der gefundene Aggregatetyp für die relevanten
                            // Produkte eindeutig?
                            if ((dcAggregateType == null) && (relevantDCAggregateTypes.size() == 1)) {
                                dcAggregateType = relevantDCAggregateTypes.iterator().next();
                            }
                        }

                        for (iPartsProduct relevantProduct : relevantProducts) {
                            // IdentContexte erstellen und dem Ergebnis hinzufügen
                            iPartsWSIdentContext identContext;
                            if (finIsValid && (vinId == null)) { // Fahrzeug mit FIN
                                identContext = new iPartsWSIdentContext((dataCard != null) ? ((VehicleDataCard)dataCard).getFinId() : finId,
                                                                        model, relevantProduct, false, userInfo, dataCard != null,
                                                                        project, requestObject.getIncludeValidities());
                            } else if ((vinId != null) && vinId.isValidId()) { // Fahrzeug mit VIN inkl. VIN-Baumuster-Fallback
                                identContext = new iPartsWSIdentContext((dataCard != null) ? ((VehicleDataCard)dataCard).getVinId() : vinId,
                                                                        model, relevantProduct, false, userInfo, dataCard != null,
                                                                        project, requestObject.getIncludeValidities());
                            } else if (isAggregateIdent && (dcAggregateType != null)) { // Aggregat mit Aggregate-Ident und Aggregatetyp
                                identContext = new iPartsWSIdentContext(identCode, dcAggregateType, model, relevantProduct,
                                                                        userInfo, project, requestObject.getIncludeValidities());

                            } else { // AS-Baumuster und Produkt
                                identContext = new iPartsWSIdentContext(model, relevantProduct, !isAggregateIdent, userInfo, project, true);
                            }

                            if (identContext.hasForbiddenStatus()) {
                                // Ident-Context darf nicht herausgegeben werden weil verschrottet oder gestohlen
                                // Hier keine Fehlermeldung generieren für LifeCycleStatus.scrapped && LifeCycleStatus.stolen
                                continue;
                            }

                            // Falls die Datenkarte nicht gefunden wurde, wurde ein Baumuster-Fallback gemacht -> nicht zulassen bei RMI
                            if (iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI()) {
                                if (!identContext.isDatacardExists()) {
                                    throwError(HttpConstants.HTTP_STATUS_NOT_FOUND, WSError.REQUEST_OBJECT_NOT_FOUND, "Datacard not found", null);
                                }
                            }

                            // Ein evtl. gemerkte DataCardRetrievalException im IdentContext setzen
                            if (dataCardRetrievalException != null) {
                                identContext.setErrorText(null); // Die dataCardRetrievalException muss als Fehlertext gewinnen
                                identContext.setErrorTextByDataCardRetrievalException(dataCardRetrievalException);
                            }

                            String productVirtualIdString = iPartsVirtualNode.getVirtualIdString(relevantProduct, relevantProduct.isStructureWithAggregates(),
                                                                                                 project);
                            List<iPartsNote> notes = iPartsNote.getNotes(new iPartsPartId(productVirtualIdString, ""), project);
                            identContext.setNotes(iPartsWSNote.convertToWSNotes(notes, language, dataBaseFallbackLanguages));

                            identContexts.add(identContext);
                        }
                    }
                }
            }

            // Eine evtl. gemerkte DataCardRetrievalException jetzt noch loggen
            if (dataCardRetrievalException != null) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, dataCardRetrievalException);
            }
        } else {
            throwBadRequestError(WSError.REQUEST_SYNTAX_ERROR, "Invalid format of attribute '" + identCodeAttributeName + "': "
                                                               + identCode, null);
        }

        // Falls es mit FIN kein Ergebnis mit geladener Datenkarte gibt, nochmal explizit als VIN versuchen
        if (finIsValid && (vinId == null) && (identContexts.isEmpty() || !identContexts.get(0).isDatacardExists())) {
            try {
                iPartsWSIdentResponse responseForVIN = executeWebservice(project, requestObject, true);
                if ((responseForVIN != null) && !responseForVIN.getIdentContexts().isEmpty() && responseForVIN.getIdentContexts().get(0).isDatacardExists()) {
                    return responseForVIN; // Datenkarte für VIN gefunden
                }
            } catch (RESTfulWebApplicationException e) {
                // REQUEST_PARAMETER_WRONG ignorieren, weil dieser Fehlercode bei einer ungültigen VIN kommt -> es könnte
                // ein Baumuster-Fallback für die FIN vorhanden sein
                if (e.getErrorCode() != WSError.REQUEST_PARAMETER_WRONG.getCode()) {
                    throw e;
                }
            }
        }

        response.setIdentContexts(identContexts);
        return response;
    }

    /**
     * Überprüft, ob die übergebene Datenkarte gültig ist (speziell das Fahrzeug nicht verschrottet oder gestohlen wurde).
     *
     * @param dataCard Darf nicht {@code null} sein
     * @return
     */
    private boolean checkIsDataCardValid(AbstractDataCard dataCard) {
        // Bei verschrotteten oder gestohlenen Fahrzeugen eine entsprechende Fehlermeldung generieren und die
        // Datenkarte NICHT zurückliefern.
        switch (dataCard.getLifeCycleStatus()) {
            case scrapped:
                throwBadRequestError(iPartsWSError.REQUEST_VEHICLE_SCRAPPED, iPartsConst.WS_VEHICLE_SCRAPPED_MSG, null);
                return false;
            case stolen:
                throwBadRequestError(iPartsWSError.REQUEST_VEHICLE_STOLEN, iPartsConst.WS_VEHICLE_STOLEN_MSG, null);
                return false;
        }

        return true;
    }

    /**
     * Check, ob alle Baumuster gültig sind
     *
     * @param modelNumbers
     * @return
     */
    private boolean checkModels(List<String> modelNumbers) {
        if ((modelNumbers == null) || modelNumbers.isEmpty()) {
            return false;
        }
        for (String modelNumber : modelNumbers) {
            if (!iPartsModelId.isModelNumberValid(modelNumber, false)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType) {
        if ((dataType == iPartsDataChangedEventByEdit.DataType.PRODUCT) || (dataType == iPartsDataChangedEventByEdit.DataType.SERIES)
            || (dataType == iPartsDataChangedEventByEdit.DataType.MODEL) || (dataType == iPartsDataChangedEventByEdit.DataType.SA)) {
            clearCaches();
        }
    }


    public void setIdentCodeAttributeName(String identCodeAttributeName) {
        this.identCodeAttributeName = identCodeAttributeName;
    }
}