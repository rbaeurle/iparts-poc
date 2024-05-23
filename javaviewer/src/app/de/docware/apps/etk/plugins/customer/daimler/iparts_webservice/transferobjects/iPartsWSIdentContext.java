/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint;
import de.docware.apps.etk.base.webservice.endpoints.helper.WSHelper;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsSpecType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects.iPartsWSsalesAreaInformationObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelOil;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelOilList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsModelOilId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsProductModelHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.VinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbSpec.iPartsMbSpecData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSIdentContextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;
import java.util.stream.Collectors;

import static de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint.throwResourceNotFoundError;
import static de.docware.framework.modules.gui.misc.http.HttpConstants.HTTP_STATUS_OK;

/**
 * IdentContext Data Transfer Object für die iParts Webservices
 */
public class iPartsWSIdentContext extends WSRequestTransferObject implements iPartsConst {

    public static boolean IN_UNITTEST_MODE = false;

    private String aggregateNumber;
    private String aggTypeId;
    private String aggSubType;
    private Collection<String> productClassIds;
    private List<String> productClassNames;
    private String modelId;
    private String modelName;
    private String modelRemarks;
    private String productId;  // Produktnummer
    private boolean connectWireHarnessEnabled;
    private Set<String> validCountries;
    private Set<String> invalidCountries;
    private String productRemarks;
    private String modelDesc;
    private List<iPartsWSIdentContext> aggregates;
    private List<iPartsWSSaCode> saCodes;
    private String modelTypeId;
    private String typeVersion;
    private boolean datacardExists;
    private List<iPartsWSNote> notes;
    private String marketId;
    private String fin;
    private String vin;
    private iPartsWSFilterOptions filterOptions;
    private AbstractDataCard.LifeCycleStatus lifeCycleStatus;
    private boolean rebuiltAggregate;
    private String errorText;
    private List<iPartsWSMbSpec> mbOilSpecs;
    private List<iPartsWSMbSpec> mbSpecs;

    // DAIMLER-9770, Webservice Ident um filterrelevante Codes, SAA, Daten erweitern
    private List<String> codeValidities;
    private List<String> saaValidities;
    private String factory;
    private String dateOfTechnicalState;
    private String technicalApprovalDate;
    private List<iPartsWSsalesAreaInformationObject> salesAreaInformation;

    // DAIMLER-11339: Ident Context um orderNumber erweitern; erweiternd zu DAIMLER-9770
    private String orderNumber;

    // Attribute die vom VIS Service weitergegeben werden
    private boolean fixingPartsAvailable;
    private boolean prodOrderTextAvailable;
    private boolean fieldOrganisationTextAvailable;

    private boolean isAggregate;

    // DAIMLER-13158: Integrierte Gesamtnavigation
    private boolean integratedNavigationAvailable;

    private String country; // Gewähltes Land
    private boolean visualNavAvailable;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSIdentContext() {
    }

    /**
     * Konstruktor für eine bestimmte FIN, AS-Baumuster und Produkt sowie Sprache
     *
     * @param fin
     * @param model
     * @param product
     * @param loadFixingParts
     * @param userInfo
     * @param tryToLoadDatacard Flag, ob versucht werden soll, die Datenkarte über die VIN zu laden
     * @param project
     * @param includeValidities
     */
    public iPartsWSIdentContext(FinId fin, iPartsModel model, iPartsProduct product, boolean loadFixingParts, iPartsWSUserInfo userInfo,
                                boolean tryToLoadDatacard, EtkProject project, boolean includeValidities) {
        this(model, product, false, userInfo, project, (fin == null) || !fin.isValidId());
        if (fin != null) {
            setFin(fin.getFIN());
        }
        setDatacardExists(tryToLoadDatacard); // datacardExists bei tryToLoadDatacard zunächst auf true setzen, damit die Datenkarte über getDataCard() versucht wird zu laden
        VehicleDataCard vehicleDataCard = null;
        try {
            vehicleDataCard = iPartsWSIdentContextHelper.getVehicleDataCard(false, false, this.fin, modelId, datacardExists,
                                                                            getClass().getSimpleName(), project,
                                                                            loadFixingParts);
        } catch (DataCardRetrievalException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
            setErrorTextByDataCardRetrievalException(e);
        }

        setDatacardExists((vehicleDataCard != null) && vehicleDataCard.isDataCardLoaded());
        boolean canFillFromDatacard = false;
        if (datacardExists) {
            setLifeCycleStatus(vehicleDataCard.getLifeCycleStatus());
            setVin(vehicleDataCard.getVin());
            canFillFromDatacard = !vehicleDataCard.hasForbiddenLifeCycleStatus();
        } else {
            setVisualNavAvailable(false);
            setErrorTextIfEmpty("No datacard found" + ((fin != null) ? " for FIN: " + fin.getFIN() : ""));
        }
        if (canFillFromDatacard) {
            iPartsWSIdentContextHelper.fillVehicleWithAggregatesFromDatacard(project, this, vehicleDataCard, userInfo, includeValidities);
        } else {
            iPartsWSIdentContextHelper.fillVehicleWithAggregatesFromDB(project, this, modelId, userInfo);
        }

        // Bei RMI-WebService Aufrufen, leerer / nicht vorhandener Datenkarte und nicht gesetztem Schalter dürfen die Infos nicht ausgegeben werden.
        if (includeValidities && !iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI() && (vehicleDataCard != null)) {
            setValidities(project, vehicleDataCard);
        }

        setSpecValidities(vehicleDataCard, userInfo, project);
    }

    private void setSpecValidities(VehicleDataCard vehicleDataCard, iPartsWSUserInfo userInfo, EtkProject project) {
        // Specs bei RMI und bei nicht geladener Datenkarte nicht setzen
        if (!iPartsWebservicePlugin.isRMIActive() && (vehicleDataCard != null) && datacardExists) {
            // Die Texte der Specs über das Baumuster ermitteln
            Map<iPartsSpecType, Map<iPartsModelOilId, iPartsDataModelOil>> modelOilTextsMapTotal = new HashMap<>();
            Map<iPartsSpecType, List<? extends AbstractDataCard>> aggregateDataCardsTotal = new HashMap<>();
            for (iPartsSpecType specType : iPartsSpecType.RELEVANT_TYPES) {
                DCAggregateTypes aggregateType = DCAggregateTypes.getDCAggregateTypeBySpecType(specType);
                Map<iPartsModelOilId, iPartsDataModelOil> modelOilTextsMapForSpecType = new HashMap<>();
                List<? extends AbstractDataCard> aggregateDataCardsForSpecType;
                if (aggregateType.equals(DCAggregateTypes.VEHICLE)) {
                    aggregateDataCardsForSpecType = Arrays.asList(vehicleDataCard);
                    // DAIMLER-15552: Für Getriebeöletyp "GEAR_OIL" sollen ALLE Aggregatedatenkarten einbezogen werden.
                } else if (aggregateType.equals(DCAggregateTypes.TRANSMISSION)) {
                    aggregateDataCardsForSpecType = vehicleDataCard.getAllAggregateDatacards();
                } else {
                    aggregateDataCardsForSpecType = vehicleDataCard.getAllAggregateDataCardOfOneType(aggregateType,
                                                                                                     DCAggregateTypeOf.NONE);
                }
                if (!aggregateDataCardsForSpecType.isEmpty()) {
                    aggregateDataCardsTotal.put(specType, aggregateDataCardsForSpecType);
                    for (AbstractDataCard aggregateDataCard : aggregateDataCardsForSpecType) {
                        String aggregateModelNo = aggregateDataCard.getModelNo();
                        iPartsDataModelOilList modelOilList = iPartsDataModelOilList.loadDataModelOilForModelAndSpecType(project, new iPartsModelId(aggregateModelNo), specType);
                        modelOilList.forEach(modelOil -> {
                            modelOilTextsMapForSpecType.put(new iPartsModelOilId(aggregateModelNo, modelOil.getAsId().getSpecValidity(), specType.getDbValue()), modelOil);
                        });
                    }
                    if (!modelOilTextsMapForSpecType.isEmpty()) {
                        modelOilTextsMapTotal.put(specType, modelOilTextsMapForSpecType);
                    }
                }
            }

            List<String> dataBaseFallbackLanguages = project.getDataBaseFallbackLanguages();
            List<iPartsWSMbSpec> mbSpecs = new DwList<>();
            Map<String, Map<String, iPartsMbSpecData>> specValiditiesAndQuantities = vehicleDataCard.getSpecValiditiesAndQuantities(project);
            for (iPartsSpecType specType : iPartsSpecType.RELEVANT_TYPES) {
                Map<String, iPartsMbSpecData> specValiditiesAndQuantitiesForSpecType = specValiditiesAndQuantities.get(specType.getDbValue());
                if ((specValiditiesAndQuantitiesForSpecType != null) && !specValiditiesAndQuantitiesForSpecType.isEmpty()) {
                    specValiditiesAndQuantitiesForSpecType.forEach((spec, mbSpecData) -> { // specValiditiesAndQuantities ist nach spec sortiert
                        iPartsWSMbSpec mbSpec = new iPartsWSMbSpec();
                        mbSpec.setSpec(WSHelper.getNullForEmptyString(spec));
                        mbSpec.setType(WSHelper.getNullForEmptyString(specType.toString()));
                        mbSpec.setQuantity(WSHelper.getNullForEmptyString(mbSpecData.getQuantity()));
                        mbSpec.setSaeClass(WSHelper.getNullForEmptyString(mbSpecData.getSaeClass()));
                        // Text für alle Motor-Baumuster suchen
                        List<? extends AbstractDataCard> aggregateDataCards = aggregateDataCardsTotal.get(specType);
                        if ((aggregateDataCards != null) && !aggregateDataCards.isEmpty()) {
                            for (AbstractDataCard aggregateDataCard : aggregateDataCards) {
                                if (modelOilTextsMapTotal.get(specType) != null) {
                                    iPartsDataModelOil modelOil = modelOilTextsMapTotal.get(specType).get(new iPartsModelOilId(aggregateDataCard.getModelNo(), spec, specType.getDbValue()));
                                    if (modelOil != null) {
                                        // TEXT
                                        EtkMultiSprache modelOilText = modelOil.getText();
                                        if (modelOilText != null) {
                                            String text = modelOilText.getTextByNearestLanguage(userInfo.getLanguage(), dataBaseFallbackLanguages);
                                            mbSpec.setText(WSHelper.getNullForEmptyString(text));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        mbSpecs.add(mbSpec);
                    });
                    setMbOilSpecs(mbSpecs);
                    setMbSpecs(mbSpecs);
                }
            }

        }
    }

    /**
     * Konstruktor für eine bestimmte VIN, AS-Baumuster und Produkt sowie Sprache
     *
     * @param vin
     * @param model
     * @param product
     * @param loadFixingParts
     * @param userInfo
     * @param tryToLoadDatacard Flag, ob versucht werden soll, die Datenkarte über die VIN zu laden
     * @param project
     * @param includeValidities
     */
    public iPartsWSIdentContext(VinId vin, iPartsModel model, iPartsProduct product, boolean loadFixingParts, iPartsWSUserInfo userInfo,
                                boolean tryToLoadDatacard, EtkProject project, boolean includeValidities) {
        this(model, product, false, userInfo, project, (vin == null) || !vin.isValidId());
        if (vin != null) {
            setVin(vin.getVIN());
        }
        setDatacardExists(tryToLoadDatacard); // datacardExists bei tryToLoadDatacard zunächst auf true setzen, damit die Datenkarte über getDataCard() versucht wird zu laden
        VehicleDataCard vehicleDataCard = null;
        try {
            vehicleDataCard = iPartsWSIdentContextHelper.getVehicleDataCard(false, false, this.vin, modelId, datacardExists,
                                                                            getClass().getSimpleName(), project,
                                                                            loadFixingParts);
        } catch (DataCardRetrievalException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
            setErrorTextByDataCardRetrievalException(e);
        }

        setDatacardExists((vehicleDataCard != null) && vehicleDataCard.isDataCardLoaded());
        boolean canFillFromDatacard = false;
        if (datacardExists) {
            setLifeCycleStatus(vehicleDataCard.getLifeCycleStatus());
            setFin(vehicleDataCard.getFinId().getFIN());
            canFillFromDatacard = !vehicleDataCard.hasForbiddenLifeCycleStatus();
        } else {
            setVisualNavAvailable(false);
            setErrorTextIfEmpty("No datacard found" + ((vin != null) ? " for VIN: " + vin.getVIN() : ""));
        }
        if (canFillFromDatacard) {
            iPartsWSIdentContextHelper.fillVehicleWithAggregatesFromDatacard(project, this, vehicleDataCard, userInfo, includeValidities);
        } else {
            iPartsWSIdentContextHelper.fillVehicleWithAggregatesFromDB(project, this, modelId, userInfo);
        }

        // Bei RMI-WebService Aufrufen, leerer / nicht vorhandener Datenkarte und nicht gesetztem Schalter dürfen die Infos nicht ausgegeben werden.
        if (includeValidities && !(iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI()) && (vehicleDataCard != null)) {
            setValidities(project, vehicleDataCard);
        }

        setSpecValidities(vehicleDataCard, userInfo, project);
    }

    /**
     * Konstruktor für Aggregate-Datenkarte, Produkt und User Info
     *
     * @param aggregate
     * @param product
     * @param userInfo
     * @param project
     * @param includeValidities
     */
    public iPartsWSIdentContext(AggregateDataCard aggregate, iPartsModel model, iPartsProduct product, iPartsWSUserInfo userInfo,
                                EtkProject project, boolean includeValidities) {
        this(model, product, false, userInfo, project, aggregate.getAggregateIdent() == null);
        String aggSubType = aggregate.getAggregateTypeOf().getJsonName();
        if (!StrUtils.isEmpty(aggSubType)) {
            setAggSubType(aggSubType);
        }
        String aggregateIdent = aggregate.getAggregateIdent();
        if (StrUtils.isValid(aggregateIdent)) {
            setAggregateNumber(aggregateIdent);
            setRebuiltAggregate(aggregate.getAggIdent(project).isExchangeAggregate());
        }
        // Bei RMI-WebService Aufrufen, leerer / nicht vorhandener Datenkarte und nicht gesetztem Schalter dürfen die Infos nicht ausgegeben werden.
        if (includeValidities && !iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI()) {
            setValidities(project, aggregate);
        }
    }

    /**
     * DAIMLER-9770, Webservice Ident um filterrelevante Codes, SAA, Daten erweitern
     * Die Daten dürfen bei RMI-Webservice-Aufrufen NICHT ausgegeben werden.
     * Es gibt auch einen Schalter, ob die Infos überhaupt ausgegeben werden sollen.
     *
     * @param project
     * @param dataCard
     */
    private void setValidities(EtkProject project, AbstractDataCard dataCard) {
        setCodeValidities(dataCard.getCodeNumbers());
        setSaaValidities(dataCard.getDataCardSaaNumbers());
        setFactory(dataCard.getFactoryNumber(project));
        setTechnicalApprovalDate(dataCard.getTechnicalApprovalDate(true));
        // Nur Fahrzeugdatenkarten haben ein [dateOfTechnicalState] und [salesAreaInformation]
        if (dataCard.isVehicleDataCard()) {
            VehicleDataCard vehicleDataCard = (VehicleDataCard)dataCard;
            setDateOfTechnicalState(vehicleDataCard.getDateOfTechnicalState(true));
            setSalesAreaInformation(vehicleDataCard.getSalesAreaInformation());
            // existiert zwar auch in Aggregatdatenkarten, ist dort aber nicht relevant und
            // soll nur in Fahrzeugdatenkarten angezeigt werden
            setOrderNumber(vehicleDataCard.getOrderNumber());
        } else {
            setDateOfTechnicalState(null);
        }
    }

    /**
     * Konstruktor für einen bestimmten Aggregate-Ident, Aggregatetyp, AS-Baumuster und Produkt sowie Sprache
     *
     * @param aggregateIdent
     * @param dcAggregateType
     * @param model
     * @param product
     * @param userInfo
     * @param project
     */
    public iPartsWSIdentContext(String aggregateIdent, DCAggregateTypes dcAggregateType, iPartsModel model, iPartsProduct product,
                                iPartsWSUserInfo userInfo, EtkProject project, boolean includeValidities) {
        this(model, product, false, userInfo, project, aggregateIdent == null);
        if (aggregateIdent != null) {
            setAggregateNumber(aggregateIdent);
        }
        setDatacardExists(true); // datacardExists zunächst auf true setzen, damit die Datenkarte über getDataCard() versucht wird zu laden
        AggregateDataCard aggregateDataCard = null;
        try {
            aggregateDataCard = iPartsWSIdentContextHelper.getAggregateDataCard(false, dcAggregateType, aggregateIdent,
                                                                                modelId, datacardExists,
                                                                                getClass().getSimpleName(), project);
        } catch (DataCardRetrievalException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
            setErrorTextByDataCardRetrievalException(e);
        }

        setDatacardExists((aggregateDataCard != null) && aggregateDataCard.isDataCardLoaded());
        if (!datacardExists) {
            setVisualNavAvailable(false);
            setErrorTextIfEmpty("No datacard found for aggregate ident: " + aggregateIdent);
        }
        // Bei RMI-WebService Aufrufen, leerer / nicht vorhandener Datenkarte und nicht gesetztem Schalter dürfen die Infos nicht ausgegeben werden.
        if (includeValidities && !(iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI()) && (aggregateDataCard != null)) {
            setValidities(project, aggregateDataCard);
        }
    }

    /**
     * Konstruktur für ein bestimmtes AS-Baumuster und Produkt sowie Sprache
     *
     * @param model                          null für Verwendung bei GetMaterialPartInfo
     * @param product
     * @param fillAggregatesWithValuesFromDB true Lädt die Aggregate aus der DB für das übergebene Fahrzeug
     * @param userInfo
     * @param project
     * @param isModelCall                    Direkter Baumustereinstieg?
     */
    public iPartsWSIdentContext(iPartsModel model, iPartsProduct product,
                                boolean fillAggregatesWithValuesFromDB, iPartsWSUserInfo userInfo,
                                EtkProject project, boolean isModelCall) {
        if (product == null) {
            String errStr = "Product is null";
            if (model != null) {
                errStr += " for model number: " + model.getModelId().getModelNumber();
            }
            throwRequestError(WSError.INTERNAL_ERROR, errStr, null);
            return;
        }

        setAggTypeId(product.getAggregateType());

        Map<String, Set<String>> userPermissionsMap = userInfo.getPermissionsAsMapForValidation();
        Set<String> filteredAsProductClasses;
        if (iPartsWebservicePlugin.isCheckTokenPermissions() && (userPermissionsMap != null)) {
            filteredAsProductClasses = product.getAsProductClasses().stream()
                    .filter(asProductClass -> product.isASProductClassValidForAssortmentPermissions(project, asProductClass,
                                                                                                    userPermissionsMap))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            filteredAsProductClasses = product.getAsProductClasses();
        }
        // Liste von AS-Produktklassen ...
        setProductClassIds(filteredAsProductClasses);
        // ... und Liste mit Benennungen erzeugen.
        setProductClassNames(product.getASProductClassesTitles(project, userInfo.getLanguage(), filteredAsProductClasses));
        setProductId(product.getAsId().getProductNumber());
        setConnectWireHarnessEnabled(product.isWireHarnessDataVisible());

        // Produktbemerkungen mit Fallback auf Produktkommentar
        List<String> dataBaseFallbackLanguages = project.getDataBaseFallbackLanguages();
        String remark = product.getProductRemarkWithCommentFallback(project).getTextByNearestLanguage(userInfo.getLanguage(),
                                                                                                      dataBaseFallbackLanguages);

        // Nur gültig in Ländern
        Set<String> validCountries = product.getValidCountries();
        if ((validCountries != null) && !validCountries.isEmpty()) {
            setValidCountries(validCountries);
        }

        // Nicht gültig in Ländern
        Set<String> invalidCountries = product.getInvalidCountries();
        if ((invalidCountries != null) && !invalidCountries.isEmpty()) {
            setInvalidCountries(invalidCountries);
        }

        if (StrUtils.isValid(remark)) {
            setProductRemarks(remark);
        }

        // Notizen
        //setNotes();

        //setMarketId();

        // Indikator für Fahrzeugperspektive
        setVisualNavAvailable(product.containsVisibleCarPerspectiveTU(project, isModelCall));

        /*
         * Ab hier wird ein Baumuster benötigt. Bei GetMaterialPartInfo ist dieses nicht vorhanden, sodass wir hier
         * abbrechen.
         */

        if (model == null) {
            return;
        }

        String modelNumber = model.getModelId().getModelNumber();
        setModelId(modelNumber);

        String modelName = model.getModelName(project).getTextByNearestLanguage(userInfo.getLanguage(), dataBaseFallbackLanguages);
        if (!modelName.isEmpty()) {
            setModelName(modelName);
        }

        // Den Baumusterzusatztext ermitteln
        EtkMultiSprache additionalTexts = iPartsProductModelHelper.getModelAddText(project, model.getModelId().getModelNumber(),
                                                                                   product.getAsId().getProductNumber());
        String modelRemarks = additionalTexts.getTextByNearestLanguage(userInfo.getLanguage(), dataBaseFallbackLanguages);
        if (!modelRemarks.isEmpty()) {
            setModelRemarks(modelRemarks);
        }

        String modelDesc = model.getModelSalesTitle(project).getTextByNearestLanguage(userInfo.getLanguage(), dataBaseFallbackLanguages);
        if (!modelDesc.isEmpty()) {
            setModelDesc(modelDesc);
        }
        if (fillAggregatesWithValuesFromDB) {
            iPartsWSIdentContextHelper.fillVehicleWithAggregatesFromDB(project, this, modelNumber, userInfo);
        }

        String modelTypeId = model.getModelTypeNumber();
        if (!modelTypeId.isEmpty()) {
            setModelTypeId(modelTypeId);
        }

        if (!IN_UNITTEST_MODE) {
            String typeVersion = model.getAusfuehrungsArt();
            if (!typeVersion.isEmpty()) {
                setTypeVersion(typeVersion);
            }
        }
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        iPartsWSIdentContextHelper.checkIfIdentContextValid(this);
        checkAttribValid(path, "aggTypeId", aggTypeId);

        // Baumusternummer überprüfen
        checkAttribValid(path, "modelId", modelId);
        if (!iPartsModel.isModelNumberValid(modelId)) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Attribute 'modelId' is no valid model number: " + modelId, path);
        }

        if (!isAggregate) { // bei Aggregaten kann productClassIds auch leer sein
            checkAttribValid(path, "productClassIds", productClassIds);
        }
        checkAttribValid(path, "productId", productId);

        // FIN überprüfen
        if (!StrUtils.isEmpty(fin) && !(new FinId(fin).isValidId())) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Attribute 'fin' is no valid FIN: " + fin, path);
        }

        // TODO VIN überprüfen?

        // aggregates muss gültig sein falls vorhanden
        if (aggregates != null) {
            checkAttribListValid(path, "aggregates", aggregates);
        }

        // saCodes muss gültig sein falls vorhanden
        if (saCodes != null) {
            checkAttribListValid(path, "saCodes", saCodes);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ getAggregateNumber(), getAggTypeId(), getProductClassIds(), getModelId(), getProductId(), getModelTypeId(),
                             getTypeVersion(), isDatacardExists(), getMarketId(), getFin(), getVin(), getAggregates(), getSaCodes(),
                             getCountry(), getFilterOptions() };
    }

    /**
     * Überprüft, ob das Baumuster in diesem {@link iPartsWSIdentContext} für das angegebene Produkt gültig ist und wirft
     * bei Ungültigkeit eine {@link RESTfulWebApplicationException}. Davor wird bereits überprüft, ob das Produkt selbst
     * überhaupt gültig ist.
     *
     * @param path
     * @param project
     */
    public void checkIfModelValid(String path, EtkProject project, iPartsWSUserInfo userInfo) {
        checkAttribValid(path, "modelId", modelId);
        checkAttribValid(path, "productId", productId);

        iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productId));
        // Token Gültigkeiten prüfen
        if (!product.isValidForPermissions(project, userInfo.getCountryForValidation(), userInfo.getPermissionsAsMapForValidation())) {
            WSAbstractEndpoint.throwPermissionsError();
        }

        boolean onlyRetailRelevantProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();
        if (onlyRetailRelevantProducts && !product.isRetailRelevant()) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Invalid product '" + productId + "'", null);
        }

        Set<String> modelNumbers = iPartsPlugin.isCheckModelVisibility() ? product.getVisibleModelNumbers(project) : product.getModelNumbers(project);
        if (modelNumbers.contains(modelId)) {
            return;
        }

        throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Model '" + modelId + "' is invalid for product '" + productId + "'", path);
    }

    /**
     * Aktiviert alle passenden Filteroptionen basierend auf den Werten aus dem Parameter {@link #filterOptions} und lädt
     * die Fahrzeug-Datenkarte bei vorhandener FIN (ohne Befestigungsteile), die Aggregate-Datenkarte bei vorhandemem
     * Aggregate-Ident bzw. erzeugt eine Baumuster-Datenkarte aus dem Baumuster und setzt sie im Filter. Bei fehlendem
     * Parameter werden als Fallback alle Filteroptionen aktiviert.
     *
     * @param country              Land für den Ländergültigkeits-Filter (normalerweise aus {@link iPartsWSUserInfo#getCountry()})
     * @param isMaterialWebservice
     * @param project
     * @return
     */
    public iPartsFilter setFilterForIdentContext(String country, boolean isMaterialWebservice, EtkProject project) {
        AbstractDataCard dataCard = null;
        DCAggregateTypes aggregateType = DCAggregateTypes.getDCAggregateTypeByAggregateType(aggTypeId);

        try {
            // Aggregate-Datenkarten dürfen nur geladen werden wenn es keine FIN gibt
            if (StrUtils.isValid(fin) || (aggregateType == DCAggregateTypes.VEHICLE)) {
                // Wenn die FIN gesetzt ist, dann muss die Fahrzeugdatenkarte geladen werden.
                // Innerhalb der mehrstufigen Filterung wird dann die korrekte Aggregate-Datenkarte ausgewählt, falls der
                // NavContext sich auf ein Aggregat bezieht.
                VehicleDataCard vehicleDataCard = iPartsWSIdentContextHelper.getVehicleDataCard(false, true, fin, modelId,
                                                                                                datacardExists, getClass().getSimpleName(),
                                                                                                project, false);
                // Land an der Fahrzeug-Datenkarte (für die Filterung) und im IdentContext (für den Cache-Key) setzen
                if (vehicleDataCard != null) {
                    vehicleDataCard.setCountry(country);
                }
                setCountry(country);
                dataCard = vehicleDataCard;
            } else {
                // Laden der Aggregate-Datenkarte
                if (aggregateType != DCAggregateTypes.UNKNOWN) { // gültiger Aggregatetyp
                    dataCard = iPartsWSIdentContextHelper.getAggregateDataCard(true, aggregateType, aggregateNumber, modelId,
                                                                               datacardExists, getClass().getSimpleName(),
                                                                               project);
                }
            }
        } catch (DataCardRetrievalException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
            setErrorTextByDataCardRetrievalException(e);
        }

        // Datenkarte konnte nicht geladen werden
        if ((dataCard == null) || !dataCard.isDataCardLoaded()) {
            setDatacardExists(false);
        }

        // DAIMLER-10034, Falls es sich um eine Anfrage von einem RMI Typzulassung Service handelt, darf nur mit einer
        // gültigen FIN oder VIN auf die Daten zugegriffen werden. In diesem Fall muss die Datenkarte auch existieren.
        if ((!isDatacardExists()) && (iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI())) {
            throwResourceNotFoundError("Datacard not found");
        }

        // Filter setzen und aktivieren
        iPartsFilter filter = iPartsFilter.get();
        if (isMaterialWebservice) {
            filter.setAllRetailFilterActiveForSpecialProduct(project, dataCard, true);
        } else {
            // Als Fallback und für den eingeschränkten Aufruf (DAIMLER-10034) alle Filteroptionen setzen
            if ((filterOptions == null) || iPartsWebservicePlugin.isOnlyFinBasedRequestsRMI()) {
                filter.setAllRetailFilterActiveForDataCard(project, dataCard, true);
            } else {
                filter.setCurrentDataCard(dataCard, project);
                filter.setSwitchboardState(filterOptions.getAsFilterSwitchboardState());
            }
        }

        return filter;
    }

    /**
     * Lädt die Befestigungsteile nach falls diese vom angegebenen Modul benötigt werden.
     *
     * @param assembly
     * @param partListType
     */
    public void loadFixingPartsIfNeeded(EtkDataAssembly assembly, EtkEbenenDaten partListType) {
        // Falls eine Datenkarte vorhanden ist, muss überprüft werden, ob die Befestigungsteile noch nachgeladen werden müssen
        if (isDatacardExists() && (assembly instanceof iPartsDataAssembly)) {
            if (((iPartsDataAssembly)assembly).hasPartListEntriesWithConstructionKits(partListType)) {
                AbstractDataCard datacard = iPartsFilter.get().getCurrentDataCard();
                if (datacard instanceof VehicleDataCard) {
                    iPartsDataCardRetrievalHelper.loadFixingParts((VehicleDataCard)datacard, getClass().getSimpleName());
                }
            }
        }
    }

    // Getter und Setter
    public String getAggregateNumber() {
        return aggregateNumber;
    }

    public void setAggregateNumber(String aggregateNumber) {
        this.aggregateNumber = aggregateNumber;
    }

    public String getAggTypeId() {
        return aggTypeId;
    }

    public void setAggTypeId(String aggTypeId) {
        this.aggTypeId = aggTypeId;
    }

    public Collection<String> getProductClassIds() {
        return productClassIds;
    }

    public void setProductClassIds(Collection<String> productClassIds) {
        this.productClassIds = productClassIds;
    }

    public List<String> getProductClassNames() {
        return productClassNames;
    }

    public void setProductClassNames(List<String> productClassNames) {
        this.productClassNames = productClassNames;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelRemarks() {
        return modelRemarks;
    }

    public void setModelRemarks(String modelRemarks) {
        this.modelRemarks = modelRemarks;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public boolean isConnectWireHarnessEnabled() {
        return connectWireHarnessEnabled;
    }

    public void setConnectWireHarnessEnabled(boolean connectWireHarnessEnabled) {
        this.connectWireHarnessEnabled = connectWireHarnessEnabled;
    }

    public Set<String> getValidCountries() {
        return validCountries;
    }

    public void setValidCountries(Set<String> validCountries) {
        this.validCountries = validCountries;
    }

    public Set<String> getInvalidCountries() {
        return invalidCountries;
    }

    public void setInvalidCountries(Set<String> invalidCountries) {
        this.invalidCountries = invalidCountries;
    }

    public String getModelDesc() {
        return modelDesc;
    }

    public void setModelDesc(String modelDesc) {
        this.modelDesc = modelDesc;
    }

    public List<iPartsWSIdentContext> getAggregates() {
        return aggregates;
    }

    public void setAggregates(List<iPartsWSIdentContext> aggregates) {
        if (aggregates != null) { // Aggregate als solche markieren
            for (iPartsWSIdentContext aggregateIdentContext : aggregates) {
                aggregateIdentContext.isAggregate = true;
            }
        }
        this.aggregates = aggregates;

        // Integrierte Gesamtnavigation
        setIntegratedNavigationAvailable(false);
        if (DCAggregateTypes.getDCAggregateTypeByAggregateType(getAggTypeId()).equals(DCAggregateTypes.VEHICLE) && (getAggregates() != null)
            && !getAggregates().isEmpty()) {
            setIntegratedNavigationAvailable(true);
            // Eine Map aufbauen, gruppiert nach Aggregate-Typ
            Map<String, List<iPartsWSIdentContext>> aggregatesByAggTypeId = getAggregates().stream()
                    .collect(Collectors.groupingBy(iPartsWSIdentContext::getAggTypeId));
            Set<String> uniqueAggTypeIds = new HashSet<>();
            // Nun gehen wir alle Aggregate durch und prüfen, ob die Aggregate-Typen einmalig vorkommen
            aggregatesLoop:
            for (iPartsWSIdentContext aggregate : getAggregates()) {
                String aggTypeId = aggregate.getAggTypeId();
                if (!uniqueAggTypeIds.add(aggregate.getAggTypeId())) {
                    // Sollte der Aggregate-Typ mehrfach vorkommen, prüfen, ob die zugeordneten Produkte zu den Aggregaten
                    // dieses Typs einmalig vorkommen
                    Set<String> uniqueProductIds = new HashSet<>();
                    for (iPartsWSIdentContext aggContextForAggType : aggregatesByAggTypeId.get(aggTypeId)) {
                        if (!uniqueProductIds.add(aggContextForAggType.getProductId())) {
                            // Sollten die Aggregate den gleichen Produkten zugeordnet sein -> keine integrierte Navigation
                            setIntegratedNavigationAvailable(false);
                            break aggregatesLoop;
                        }
                    }
                }
            }
        }
    }

    public List<iPartsWSSaCode> getSaCodes() {
        return saCodes;
    }

    public void setSaCodes(List<iPartsWSSaCode> saCodes) {
        this.saCodes = saCodes;
    }

    public String getModelTypeId() {
        return modelTypeId;
    }

    public void setModelTypeId(String modelTypeId) {
        this.modelTypeId = modelTypeId;
    }

    public String getTypeVersion() {
        return typeVersion;
    }

    public void setTypeVersion(String typeVersion) {
        this.typeVersion = typeVersion;
    }

    public boolean isDatacardExists() {
        return datacardExists;
    }

    public void setDatacardExists(boolean datacardExists) {
        this.datacardExists = datacardExists;
    }

    public List<iPartsWSNote> getNotes() {
        return notes;
    }

    public void setNotes(List<iPartsWSNote> notes) {
        this.notes = notes;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    @JsonIgnore
    public AbstractDataCard.LifeCycleStatus getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    @JsonIgnore
    public void setLifeCycleStatus(AbstractDataCard.LifeCycleStatus lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public iPartsWSFilterOptions getFilterOptions() {
        return filterOptions;
    }

    public void setFilterOptions(iPartsWSFilterOptions filterOptions) {
        this.filterOptions = filterOptions;
    }

    public String getAggSubType() {
        return aggSubType;
    }

    public void setAggSubType(String aggSubType) {
        this.aggSubType = aggSubType;
    }

    public String getProductRemarks() {
        return productRemarks;
    }

    public void setProductRemarks(String productRemarks) {
        this.productRemarks = productRemarks;
    }

    public boolean isFixingPartsAvailable() {
        return fixingPartsAvailable;
    }

    public void setFixingPartsAvailable(boolean fixingPartsAvailable) {
        this.fixingPartsAvailable = fixingPartsAvailable;
    }

    public boolean isProdOrderTextAvailable() {
        return prodOrderTextAvailable;
    }

    public void setProdOrderTextAvailable(boolean prodOrderTextAvailable) {
        this.prodOrderTextAvailable = prodOrderTextAvailable;
    }

    public boolean isFieldOrganisationTextAvailable() {
        return fieldOrganisationTextAvailable;
    }

    public void setFieldOrganisationTextAvailable(boolean fieldOrganisationTextAvailable) {
        this.fieldOrganisationTextAvailable = fieldOrganisationTextAvailable;
    }

    /**
     * Ein Ident-Kontext mit diesem Status darf nicht veröffentlicht werden
     *
     * @return
     */
    public boolean hasForbiddenStatus() {
        return (lifeCycleStatus == AbstractDataCard.LifeCycleStatus.scrapped) || (lifeCycleStatus == AbstractDataCard.LifeCycleStatus.stolen);
    }

    public boolean isRebuiltAggregate() {
        return rebuiltAggregate;
    }

    public void setRebuiltAggregate(boolean rebuiltAggregate) {
        this.rebuiltAggregate = rebuiltAggregate;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public List<String> getCodeValidities() {
        return codeValidities;
    }

    public void setCodeValidities(List<String> codeValidities) {
        this.codeValidities = codeValidities;
    }

    public List<String> getSaaValidities() {
        return saaValidities;
    }

    public void setSaaValidities(List<String> saaValidities) {
        this.saaValidities = saaValidities;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getDateOfTechnicalState() {
        return dateOfTechnicalState;
    }

    public void setDateOfTechnicalState(String dateOfTechnicalState) {
        this.dateOfTechnicalState = dateOfTechnicalState;
    }

    public String getTechnicalApprovalDate() {
        return technicalApprovalDate;
    }

    public void setTechnicalApprovalDate(String technicalApprovalDate) {
        this.technicalApprovalDate = technicalApprovalDate;
    }

    public List<iPartsWSsalesAreaInformationObject> getSalesAreaInformation() {
        return salesAreaInformation;
    }

    public void setSalesAreaInformation(List<iPartsWSsalesAreaInformationObject> salesAreaInformation) {
        this.salesAreaInformation = salesAreaInformation;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public boolean isIntegratedNavigationAvailable() {
        return integratedNavigationAvailable;
    }

    public void setIntegratedNavigationAvailable(boolean integratedNavigationAvailable) {
        this.integratedNavigationAvailable = integratedNavigationAvailable;
    }

    @JsonIgnore
    public String getCountry() {
        return country;
    }

    @JsonIgnore
    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isVisualNavAvailable() {
        return visualNavAvailable;
    }

    public void setVisualNavAvailable(boolean visualNavAvailable) {
        this.visualNavAvailable = visualNavAvailable;
    }

    /**
     * Setzt den Fehlertext, falls dieser noch nicht vorher gesetzt wurde (also noch leer ist).
     *
     * @param errorText
     */
    @JsonIgnore
    public void setErrorTextIfEmpty(String errorText) {
        // Einen bereits vorhandenen Fehlertext nicht überschreiben
        if (StrUtils.isValid(getErrorText())) {
            return;
        }

        setErrorText(errorText);
    }

    /**
     * Setzt den Fehlertext aufgrund der übergebenen {@link DataCardRetrievalException}.
     *
     * @param exception
     */
    @JsonIgnore
    public void setErrorTextByDataCardRetrievalException(DataCardRetrievalException exception) {
        int httpResponseCode = exception.getHttpResponseCode();
        if (httpResponseCode != HTTP_STATUS_OK) {
            setErrorTextIfEmpty("HTTP error code from VIS webservice call: " + httpResponseCode + " - " + HttpConstants.getStatusCodeText(httpResponseCode));
        } else {
            setErrorTextIfEmpty("Invalid content for datacard from VIS webservice");
        }
    }

    public List<iPartsWSMbSpec> getMbOilSpecs() {
        return mbOilSpecs;
    }

    public void setMbOilSpecs(List<iPartsWSMbSpec> mbOilSpecs) {
        this.mbOilSpecs = mbOilSpecs;
    }

    public List<iPartsWSMbSpec> getMbSpecs() {
        return mbSpecs;
    }

    public void setMbSpecs(List<iPartsWSMbSpec> mbSpecs) {
        this.mbSpecs = mbSpecs;
    }
}