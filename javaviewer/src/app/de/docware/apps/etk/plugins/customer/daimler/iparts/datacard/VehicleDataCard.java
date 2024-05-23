/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsSpecType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EnrichReasons;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TwoGridValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbSpec.iPartsMbSpecData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Fahrzeug-Datenkarte
 */
public class VehicleDataCard extends AbstractDataCard implements iPartsConst {

    enum EnrichTypes {
        ENRICH_FOR_CAR,
        ONLY_FALLBACK,
        ALL
    }

    @JsonProperty
    protected FinId finId;
    @JsonProperty
    protected VinId vinId;  // hier mglw entweder VinId oder FinId
    @JsonProperty
    protected String productGroupIndication; // VeDoc Sparte (ist NICHT die normale Produktgruppe!)
    @JsonProperty
    protected String dateOfTechnicalState;
    @JsonProperty
    protected iPartsDocumentationType documentationType;
    @JsonProperty
    protected List<String> activeSpringLegFront; // VPD: 00064: ZB-Federbein vorn
    @JsonProperty
    protected List<String> activeSpringRear; // VPD: 00065: Feder hinten
    @JsonProperty
    protected List<String> activeSpringShimRear; // VPD: 00066: Federbeilage hinten
    @JsonProperty
    protected TwoGridValues aggregateModelNumbers;
    @JsonProperty
    protected Set<String> modelNumbersFromAllDatacards;
    @JsonProperty
    protected boolean doEnrichment;

    @JsonProperty
    private LinkedHashMap<String, List<AggregateDataCard>> activeAggregates;  // Map nicht direkt modifizieren, sondern nur über add-/clearActiveAggregates()
    @JsonProperty
    private Map<String, AggregateDataCard> cachedModelAggregates;  // Map nicht direkt modifizieren (nur als Cache)
    @JsonProperty
    private boolean fixingPartsAvailable;
    @JsonProperty
    private boolean prodOrderTextAvailable;
    @JsonProperty
    private boolean fieldOrganisationTextAvailable;
    @JsonProperty
    private boolean fixingPartsLoaded;
    @JsonProperty
    private Set<String> fixingParts;

    @JsonProperty
    private List<iPartsWSsalesAreaInformationObject> salesAreaInformation;

    @JsonProperty
    private String orderNumber;

    @JsonProperty
    private String country;
    @JsonProperty
    private Map<String, Map<String, iPartsMbSpecData>> specValiditiesAndQuantities; // iPartsSpecType -> Spec -> iPartsMbSpecData

    /**
     * Erzeugt eine neue Fahrzeug-Datenkarte für den übergebenen Ident-Code
     * Ist das Flag <i>loadDataCard</i> gesetzt, wird zuerst versucht die Datenkarte per Webservice zu laden.
     * Schlägt entweder das Laden fehl, oder das Flag <i>loadDataCard</i> war nicht gesetzt oder es exisitiert keine
     * Datenkarte für die übergebene FIN, und das Flag <i>createDataCard</i> ist gesetzt, wird versucht eine Baumuster
     * Datenkarte zu erzeugen. Handelt es sich bei <i>identCode</i> entweder um eine gültige FIN oder um ein 6-stelliges
     * Baumuster mit oder ohne Sachnummernkennbuchstabe, wird mit diesen Informationen eine Baumuster-Datenkarte erstellt.
     *
     * @param identCode               FIN oder 6-stelliges Baumuster mit oder ohne Sachnummernkennbuchstabe
     * @param returnForbiddenDataCard Gibt an, ob auch die Datenkarte für ein gestohlenes/verschrottetes Fahrzeug zurückgeliefert
     *                                werden soll:
     *                                - Bei {@code false} altes Verhalten mit optional BM-Datenkarte erzeugen etc.
     *                                - Bei {@code true } neues Verhalten mit Zurückliefern der gefundenen Datenkarte;
     *                                ob sie weiter verarbeitet werden soll, kann die aufrufende Funktion entscheiden
     * @param loadDataCard            gibt an, ob die Datenkarte per Webservice geladen werden soll
     * @param createModelDataCard     gibt an, ob eine Baumuster-Datenkarte mit dem identCode erstellt werden soll
     * @param loadDataCardCallback    Optionaler Callback beim Laden der Datenkarte
     * @param project
     * @param loadFixingParts         Bestimmt ob Befestigungsteile mit der Datenkarte initial geladen werden sollen
     * @throws DataCardRetrievalException
     */
    public static VehicleDataCard getVehicleDataCard(String identCode, boolean returnForbiddenDataCard, boolean loadDataCard, boolean createModelDataCard,
                                                     iPartsDataCardRetrievalHelper.LoadDataCardCallback loadDataCardCallback,
                                                     EtkProject project, boolean loadFixingParts) throws DataCardRetrievalException {
        FinId finFromIdentCode = new FinId(identCode);
        VinId vinId = null;
        LifeCycleStatus datacardLifeCycleStatus = LifeCycleStatus.unknown;
        loadDataCard &= (identCode.length() == 17); // IdentCode muss FIN oder VIN sein, die beide 17 Stellen haben
        if (loadDataCard) {
            VehicleDataCard vehicleDataCard = iPartsDataCardRetrievalHelper.getVehicleDataCard(project, finFromIdentCode,
                                                                                               loadDataCardCallback, loadFixingParts);
            if ((vehicleDataCard != null) && vehicleDataCard.isLoaded) {
                datacardLifeCycleStatus = vehicleDataCard.getLifeCycleStatus();
                if (!vehicleDataCard.hasForbiddenLifeCycleStatus() || returnForbiddenDataCard) {
                    return vehicleDataCard;
                }
            }
        }

        // Optional eine Baumuster-Datenkarte erzeugen falls keine Datenkarte gefunden wurde, oder sie nicht verwendet werden darf, bzw. identCode gar keine FIN ist,
        VehicleDataCard vehicleDataCard = new VehicleDataCard();
        if (createModelDataCard || isForbiddenLifeCycleStatus(datacardLifeCycleStatus)) {
            vehicleDataCard.setLifeCycleStatus(datacardLifeCycleStatus);

            String modelNumber = null;

            // Ist identCode ein Baumuster oder eine FIN?
            if (iPartsModelId.isModelNumberValid(identCode, false)) {
                // identCode ist nur ein Baumuster mit oder ohne Sachnummernkennbuchstabe und keine vollständige FIN
                finFromIdentCode = null;

                if (iPartsModel.isModelNumberValid(identCode)) { // 6-stelliges Baumuster mit Sachnummernkennbuchstabe
                    modelNumber = identCode;
                } else { // 6-stelliges Baumuster ohne Sachnummernkennbuchstabe
                    modelNumber = iPartsConst.MODEL_NUMBER_PREFIX_CAR + modelNumber;
                }
            } else if (finFromIdentCode.isModelNumberValid()) {
                // Bei gültigem Baumuster in der FIN handelt es sich um ein Fahrzeug-Baumuster
                modelNumber = finFromIdentCode.getFullModelNumber();
            } else {
                vinId = new VinId(identCode);
                if (vinId.isValidForModelMapping()) {
                    List<String> models = iPartsVINModelMappingCache.getInstance(project).getVisibleModelsForVINPrefix(project, vinId.getVIN());
                    if (!models.isEmpty()) {
                        modelNumber = models.get(0);
                    }
                }
                if (modelNumber == null) {
                    vinId = null;
                } else {
                    finFromIdentCode = null;
                }
            }

            if (modelNumber != null) {
                vehicleDataCard.fillByModel(project, modelNumber);
                if (finFromIdentCode != null) {
                    vehicleDataCard.setFinId(finFromIdentCode, project);
                } else if (vinId != null) {
                    vehicleDataCard.setVin(vinId.getVIN());
                }
            }
        }

        return vehicleDataCard;
    }

    public VehicleDataCard() {
        super();
    }

    public VehicleDataCard(boolean isVirtual) {
        super(isVirtual);
    }

    /**
     * Eine Datenkarte mit diesem Status darf nicht verwendet werden
     *
     * @return
     */
    public static boolean isForbiddenLifeCycleStatus(LifeCycleStatus lifeCycleStatus) {
        return (lifeCycleStatus == LifeCycleStatus.scrapped) || (lifeCycleStatus == LifeCycleStatus.stolen);
    }

    @Override
    public void clearCache() {
        super.clearCache();
        cachedModelAggregates = Collections.synchronizedMap(new HashMap<>());
        if (activeAggregates != null) {
            for (List<AggregateDataCard> aggregateDataCards : activeAggregates.values()) {
                for (AggregateDataCard aggregateDataCard : aggregateDataCards) {
                    aggregateDataCard.clearCache();
                }
            }
        }
    }

    @Override
    protected void clear() {
        super.clear();
        finId = new FinId();
        vinId = new VinId();
        productGroupIndication = ""; // Sparte
        dateOfTechnicalState = "";
        documentationType = null;
        activeSpringLegFront = new DwList<>(); // VPD: 00064: ZB-Federbein vorn
        activeSpringRear = new DwList<>(); // VPD: 00065: Feder hinten
        activeSpringShimRear = new DwList<>(); // VPD: 00066: Federbeilage hinten
        country = "";
        specValiditiesAndQuantities = null;
        clearActiveAggregates();
        activeAggregates = new LinkedHashMap<>(); // Reihenfolge ist wichtig (muss aber nicht synchronized sein)
        cachedModelAggregates = Collections.synchronizedMap(new HashMap<>());
        setAggregateModelNumbers(null);
        doEnrichment = true;
    }

    @Override
    protected boolean isValid() {
        if (super.isValid()) {
            return isFinOrVinValid();
        }
        return false;
    }

    public boolean isFinOrVinValid() {
        return ((finId != null) && finId.isValidId()) || ((vinId != null) && vinId.isValidId());
    }

    @Override
    public boolean isVehicleDataCard() {
        return true;
    }

    @Override
    public boolean isAggregateDataCard() {
        return false;
    }

    public boolean isEnriched() {
        return codes.isEnriched();
    }

    @Override
    public Set<String> getFilterModelNumbers(EtkProject project) {
        Set<String> modelNumbers = super.getFilterModelNumbers(project);
        modelNumbers.addAll(activeAggregates.keySet());
        return modelNumbers;
    }

    public boolean areVehicleDatacardsEnriched() {
        for (AggregateDataCard aggDataCard : getAllAggregateDatacards()) {
            if (aggDataCard.isEnriched()) {
                return true;
            }
        }
        return false;
    }

    public boolean isDoEnrichment() {
        return doEnrichment;
    }

    public void setDoEnrichment(boolean doEnrichment) {
        this.doEnrichment = doEnrichment;
    }

    public iPartsDocumentationType getDocumentationType() {
        return documentationType;
    }

    public void resetDocumentationType() {
        documentationType = null;
    }

    private iPartsDocumentationType getDocumentationType(EtkProject project) {
        if (documentationType == null) {
            if (isLoaded && !isModelLoaded()) {
                // Produkt zum Model laden, denn nur das Produkt kennt seinen Dokumentationstyp
                iPartsProduct product = iPartsModel.getFirstAPSFilteredProduct(project, new iPartsModelId(getModelNo()), getFinId(), this);
                if (product == null) {
                    // kein Produkt gefunden => keine Anreicherung
                    documentationType = iPartsDocumentationType.UNKNOWN;
                } else {
                    // Der Dokumentationstyp spielt eine essentielle Rolle:
                    documentationType = product.getDocumentationType();
                }
            } else {
                documentationType = iPartsDocumentationType.UNKNOWN;
            }
        }
        return documentationType;
    }

    /**
     * Die gemeinsamen Werte werden über die Funktion in der abstrakten Superklasse erledigt
     * {@see AbstractDataCard#copyValues(AbstractDataCard)}
     *
     * @return
     */
    @Override
    public VehicleDataCard cloneMe() {
        VehicleDataCard result = new VehicleDataCard();
        result.finId = new FinId(finId.getFIN());
        result.vinId = new VinId(vinId.getVIN());
        result.productGroupIndication = productGroupIndication;
        result.dateOfTechnicalState = dateOfTechnicalState;
        result.activeSpringLegFront = new DwList<>(activeSpringLegFront); // VPD: 00064: ZB-Federbein vorn
        result.activeSpringRear = new DwList<>(activeSpringRear); // VPD: 00065: Feder hinten
        result.activeSpringShimRear = new DwList<>(activeSpringShimRear); // VPD: 00066: Federbeilage hinten
        result.country = country;
        if (specValiditiesAndQuantities != null) { // Spezifikationen kopieren falls schon geladen (ansonsten werden sie im Klon nachgeladen)
            result.specValiditiesAndQuantities = new TreeMap<>(specValiditiesAndQuantities);
        }
        copyAggregateDataCards(result);
        result.aggregateModelNumbers = getAggregateModelNumbers().cloneMe();
        if (modelNumbersFromAllDatacards != null) {
            result.modelNumbersFromAllDatacards = new TreeSet<>(modelNumbersFromAllDatacards);
        } else {
            result.modelNumbersFromAllDatacards = null;
        }
        result.documentationType = documentationType;
        super.copyValues(result);
        return result;
    }

    public void copyAggregateDataCards(VehicleDataCard destDataCard) {
        destDataCard.clearActiveAggregates();
        for (AggregateDataCard aggDataCard : getAllAggregateDatacards()) {
            AggregateDataCard currentAggDataCard = (AggregateDataCard)aggDataCard.cloneMe();
            destDataCard.addActiveAggregate(currentAggDataCard);
        }
    }

    @Override
    public VehicleDataCard cloneMeAsVirtual() {
        VehicleDataCard result = cloneMe();
        result.isVirtual = true;
        for (AggregateDataCard aggDataCard : result.getAllAggregateDatacards()) {
            aggDataCard.isVirtual = true;
        }
        return result;
    }

    /*===== Getter and Setter =====*/

    public FinId getFinId() {
        return finId;
    }

    public void setFinId(FinId finId, EtkProject project) {
        if (!Utils.objectEquals(this.finId, finId)) {
            clearFactoryAndEndNumber();
            specValiditiesAndQuantities = null;
            if (finId != null) {
                setModelNo(finId.getFullModelNumber(), project);
                // Setze nur valide Links- oder Rechtslenkungswerte
                setSteeringValue(finId.getLeftOrRightSteeringAsEnumKey());
            } else {
                setModelNo("", null);
                setSteeringValue("");
            }
        }
        this.finId = finId;
    }

    @Override
    public iPartsFactoryModel.SerialNoAndFactory getSerialNumberWithOverflowAndFactory(EtkProject project) {
        if ((finId == null) || (modelNo == null)) {
            return new iPartsFactoryModel.SerialNoAndFactory();
        }
        String wmi = finId.getWorldManufacturerIdentifier();
        String factorySign = finId.getFactorySign();
        // Für die weitere Filterung muss hier der echte Lenkungswert aus der FIN weitergegeben werden.
        String steeringNumberFromFIN = finId.getSteering();
        int endNumber = getEndNumber(project);
        return super.getSerialNumberWithOverflowAndFactory(wmi, iPartsConst.AGGREGATE_TYPE_CAR, factorySign, endNumber, steeringNumberFromFIN, project);
    }

    /**
     * Liefert die VIN als String.
     * Ist die VIN leer, so wird {@code null} zurückgegeben.
     *
     * @return
     */
    public String getVin() {
        String vin = vinId.getVIN();
        if (vin.isEmpty()) {
            return null;
        }
        return vin;
    }

    public VinId getVinId() {
        return vinId;
    }

    public void setVin(String vin) {
        if (StrUtils.isEmpty(vin)) {
            this.vinId = new VinId();
        } else {
            this.vinId = new VinId(vin);
        }
    }

    public String getProductGroupIndication() {
        return productGroupIndication;
    }

    public void setProductGroupIndication(String productGroupIndication) {
        this.productGroupIndication = productGroupIndication;
    }

    /**
     * Gibt das [dateOfTechnicalState] zurück. Auf Wunsch passend formatiert.
     *
     * @param formatted
     * @return
     */
    public String getDateOfTechnicalState(boolean formatted) {
        if (formatted) {
            return formatDateForIdent(dateOfTechnicalState);
        } else {
            return dateOfTechnicalState;
        }
    }

    public String getDateOfTechnicalState() {
        return dateOfTechnicalState;
    }

    public void setDateOfTechnicalState(String dateOfTechnicalState) {
        this.dateOfTechnicalState = StrUtils.getEmptyOrValidString(dateOfTechnicalState);
    }

    public List<iPartsPartId> getSpringLegFront() {
        return buildPartIdList(activeSpringLegFront);
    }

    public List<String> getActiveSpringLegFront() {
        return activeSpringLegFront;
    }

    public void setActiveSpringLegFront(List<String> activeSpringLegFront) {
        this.activeSpringLegFront = activeSpringLegFront;
    }

    public List<iPartsPartId> getSpringRear() {
        return buildPartIdList(activeSpringRear);
    }

    public List<String> getActiveSpringRear() {
        return activeSpringRear;
    }

    public void setActiveSpringRear(List<String> activeSpringRear) {
        this.activeSpringRear = activeSpringRear;
    }

    public List<iPartsPartId> getSpringShimRear() {
        return buildPartIdList(activeSpringShimRear);
    }

    public List<String> getActiveSpringShimRear() {
        return activeSpringShimRear;
    }

    public void setActiveSpringShimRear(List<String> activeSpringShimRear) {
        this.activeSpringShimRear = activeSpringShimRear;
    }

    public boolean isDateOfTechnicalStateValid() {
        return StrUtils.isValid(getDateOfTechnicalState());
    }

    public Collection<AggregateDataCard> getActiveAggregates() {
        return Collections.unmodifiableCollection(getAllAggregateDatacards());
    }

    public void addActiveAggregate(AggregateDataCard aggregateDataCard) {
        String modelNo = aggregateDataCard.getModelNo();
        List<AggregateDataCard> aggregateDataCards = activeAggregates.get(modelNo);
        if (aggregateDataCards == null) {
            aggregateDataCards = new DwList<>();
            activeAggregates.put(modelNo, aggregateDataCards);
        }
        aggregateDataCards.add(aggregateDataCard);
        aggregateDataCard.setParentDatacard(this);
    }

    public void setParentDatacardInAllActiveAggregates(EtkProject project) {
        for (List<AggregateDataCard> aggregateDataCards : activeAggregates.values()) {
            for (AggregateDataCard aggregateDataCard : aggregateDataCards) {
                aggregateDataCard.setParentDatacard(this);
                AggregateIdent aggIdent = aggregateDataCard.getAggIdent(project);
                if (aggIdent != null) {
                    aggIdent.setProject(project);
                }
            }
        }
    }

    public void clearActiveAggregates() {
        if (activeAggregates != null) {
            for (AggregateDataCard aggregateDataCard : getAllAggregateDatacards()) {
                aggregateDataCard.setParentDatacard(null);
            }
            activeAggregates.clear();
        }
    }

    public List<AggregateDataCard> getAllAggregateDatacards() {
        List<AggregateDataCard> result = new DwList<>();
        if (activeAggregates != null) {
            for (List<AggregateDataCard> aggregateDataCardsForModel : activeAggregates.values()) {
                result.addAll(aggregateDataCardsForModel);
            }
        }
        return result;
    }

    /**
     * Relevant für Filter
     *
     * @return
     */
    public TwoGridValues getAggregateModelNumbers() {
        if (aggregateModelNumbers == null) {
            Set<String> aggregateModelNumbersSet = new TreeSet<>(activeAggregates.keySet());
            setAggregateModelNumbers(new TwoGridValues(aggregateModelNumbersSet, null));
        }
        return aggregateModelNumbers;
    }

    /**
     * Relevant für Filter-Dialog
     *
     * @param aggregateModelNumbers
     */
    public void setAggregateModelNumbers(TwoGridValues aggregateModelNumbers) {
        this.aggregateModelNumbers = aggregateModelNumbers;
        modelNumbersFromAllDatacards = null;
    }

    public AggregateDataCard getAggregateDataCard(DCAggregateTypes aggregateType, DCAggregateTypeOf aggregateTypeOf) {
        for (AggregateDataCard aggDataCard : getAllAggregateDatacards()) {
            if ((aggDataCard.getAggregateType() == aggregateType) && (aggDataCard.getAggregateTypeOf() == aggregateTypeOf)) {
                return aggDataCard;
            }
        }
        return null;
    }

    public List<AggregateDataCard> getAllAggregateDataCardOfOneType(DCAggregateTypes aggregateType, DCAggregateTypeOf aggregateTypeOf) {
        List<AggregateDataCard> result = new DwList<>();
        for (AggregateDataCard aggDataCard : getAllAggregateDatacards()) {
            if ((aggDataCard.getAggregateType() == aggregateType) && (aggDataCard.getAggregateTypeOf() == aggregateTypeOf)) {
                result.add(aggDataCard);
            }
        }
        return result;
    }

    public List<AbstractDataCard> getAggregateDataCards(EtkProject project, String modelNo) {
        List<AbstractDataCard> result = new DwList<>();
        if (this.modelNo.equals(modelNo)) {
            result.add(this);
            return result;
        }

        // Zuerst bei den realen aktiven Aggregate-Datenkarten nachsehen
        if (activeAggregates != null) {
            for (AggregateDataCard activeAggregate : getAllAggregateDatacards()) {
                if (activeAggregate.getModelNo().equals(modelNo)) {
                    result.add(activeAggregate);
                }
            }
            if (!result.isEmpty()) {
                return result;
            }
        }

        // Dann im Cache für die Baumuster-Datenkarten nachsehen (cachedModelAggregates kann hier nicht mehr null sein)
        AggregateDataCard cachedAggregate = cachedModelAggregates.get(modelNo);
        if (cachedAggregate != null) {
            result.add(cachedAggregate);
            return result;
        }

        // Zuletzt neue Baumuster-Datenkarte erzeugen und in den Cache legen
        AggregateDataCard modelDatacard = new AggregateDataCard(this.isVirtual);
        modelDatacard.fillByModel(project, modelNo);
        modelDatacard.setParentDatacard(this);
        modelDatacard.source = getFinId();
        cachedModelAggregates.put(modelNo, modelDatacard);
        result.add(modelDatacard);
        return result;
    }

    public boolean isFixingPartsAvailable() {
        return fixingPartsAvailable;
    }

    public boolean isProdOrderTextAvailable() {
        return prodOrderTextAvailable;
    }

    public boolean isFieldOrganisationTextAvailable() {
        return fieldOrganisationTextAvailable;
    }

    /*===== Getter and Setter End =====*/

    @Override
    public boolean hasAggregateModels() {
        return !getAggregateModelNumbers().getAllCheckedValues().isEmpty();
    }

    @Override
    public boolean hasFilterAggregateModels() {
        return hasAggregateModels();
    }

    private List<iPartsPartId> buildPartIdList(List<String> partNumberList) {
        List<iPartsPartId> result = new DwList<>();
        if (partNumberList != null) {
            for (String partNumber : partNumberList) {
                result.add(new iPartsPartId(partNumber, ""));
            }
        }
        return result;
    }

    @Override
    public boolean isModified(AbstractDataCard abstractDataCard, boolean includeSubDataCards) {
        if (super.isModified(abstractDataCard, includeSubDataCards)) {
            return true;
        }
        if (isVehicleDataCard() && abstractDataCard.isVehicleDataCard()) {
            VehicleDataCard vehicleDataCard = (VehicleDataCard)abstractDataCard;
            if (!finId.equals(vehicleDataCard.finId)) {
                return true;
            }
            if (!vinId.equals(vehicleDataCard.vinId)) {
                return true;
            }
            if (!aggregateModelNumbers.hasSameContent(vehicleDataCard.aggregateModelNumbers)) {
                return true;
            }
            if (activeAggregates.size() != vehicleDataCard.activeAggregates.size()) {
                return true;
            }

            if (!Utils.objectEquals(productGroupIndication, vehicleDataCard.productGroupIndication)) {
                return true;
            }
            if (!Utils.objectEquals(dateOfTechnicalState, vehicleDataCard.dateOfTechnicalState)) {
                return true;
            }
            if (activeSpringLegFront.size() != vehicleDataCard.activeSpringLegFront.size()) {
                return true;
            }
            if (!collectionContainsAll(activeSpringLegFront, vehicleDataCard.activeSpringLegFront)) {
                return true;
            }
            if (activeSpringRear.size() != vehicleDataCard.activeSpringRear.size()) {
                return true;
            }
            if (!collectionContainsAll(activeSpringRear, vehicleDataCard.activeSpringRear)) {
                return true;
            }
            if (activeSpringShimRear.size() != vehicleDataCard.activeSpringShimRear.size()) {
                return true;
            }
            if (!collectionContainsAll(activeSpringShimRear, vehicleDataCard.activeSpringShimRear)) {
                return true;
            }
            if (!Utils.objectEquals(country, vehicleDataCard.country)) {
                return true;
            }
            // Öl-Spezifikationen können nicht aktiv gesetzt werden, sondern ergeben sich aus den anderen Daten

            // Aggregate überprüfen
            if (includeSubDataCards) {
                if (activeAggregates.size() != vehicleDataCard.activeAggregates.size()) {
                    return true;
                }

                Iterator<AggregateDataCard> activeAggregatesIterator = getAllAggregateDatacards().iterator();
                Iterator<AggregateDataCard> vehicleAggregatesIterator = vehicleDataCard.getAllAggregateDatacards().iterator();
                while (activeAggregatesIterator.hasNext() && vehicleAggregatesIterator.hasNext()) {
                    if (activeAggregatesIterator.next().isModified(vehicleAggregatesIterator.next(), false)) {
                        return true;
                    }
                }
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean collectionContainsAll(Collection<String> list, Collection<String> otherList) {
        if (list.isEmpty() && otherList.isEmpty()) {
            return true;
        } else {
            return list.containsAll(otherList);
        }
    }

    /**
     * Lädt die Befestigungsteile zur aktuellen Fahrzeugdatenkarte über separaten Webservice bei Bedarf nach.
     * Dazu wird geprüft, ob die Datenkarte eine gültige FIN hat, und ob das Flag {@link #fixingPartsAvailable} auf true gesetzt ist.
     */
    public void loadFixingParts() throws DataCardRetrievalException {
        if (!fixingPartsLoaded) {
            if (fixingPartsAvailable && StrUtils.isValid(dbLanguage) && finId.isValidId()) {
                try {
                    fixingParts = iPartsDataCardRetrievalHelper.getFixingPartsForFin(finId, dbLanguage);

                    // Befestigungsteile zu den SAAs hinzufügen
                    if (!fixingParts.isEmpty()) {
                        for (String fixingPart : fixingParts) {
                            saas.addSingleValue(fixingPart, EnrichReasons.FIXING_PART, true);
                        }
                    }
                } catch (DataCardRetrievalException e) {
                    fixingParts = null;
                    throw e;
                }
            }
            fixingPartsLoaded = true; // Zukünftiges Nachladen bis zum nächsten Löschen des Caches vermeiden
        }
    }

    public boolean isFixingPartsLoaded() {
        return fixingPartsLoaded;
    }

    /**
     * Die gültigen Spezifikationen ermitteln:
     * - Alle Aggregatebaumuster zur Fahrzeugdatenkarte ermitteln
     * - Zum Aggregatebaumuster Spezifikationen ermitteln
     * - Code der Spezifikationen mit den Code der Fahrzeugdatenkarte abgleichen
     * - Übriggebliebene Spezifikationen an die Datenkarte hängen
     * <p>
     * Die Füllmenge in der Map wird für den Ident Webservice benötigt
     *
     * @param project EtkProject
     */
    public void loadValidSpecifications(EtkProject project) {
        Map<String, Map<String, iPartsMbSpecData>> specValiditiesAndQuantitiesLocal = new TreeMap<>();
        // Für jeden Spezifikationstypen den zugehörigen Aggregatetyp identifizieren, die relevanten Aggregate-Datenkarten ermitteln
        // und die Spezifikation über deren Baumuster ermitteln
        for (iPartsSpecType specType : iPartsSpecType.RELEVANT_TYPES) {
            Map<String, iPartsMbSpecData> specValiditiesAndQuantitiesForSpecType = new TreeMap<>();
            DCAggregateTypes aggregateType = DCAggregateTypes.getDCAggregateTypeBySpecType(specType);
            if (aggregateType.equals(DCAggregateTypes.VEHICLE)) {
                specValiditiesAndQuantitiesForSpecType = loadSpecificationsForDatacard(project, this, specType);
            } else {
                List<AggregateDataCard> aggregateDataCards;
                // DAIMLER-15552: Für Getriebeöletyp "GEAR_OIL" sollen ALLE Aggregatedatenkarten einbezogen werden.
                if (aggregateType.equals(DCAggregateTypes.TRANSMISSION)) {
                    aggregateDataCards = getAllAggregateDatacards();
                } else {
                    aggregateDataCards = getAllAggregateDataCardOfOneType(aggregateType, DCAggregateTypeOf.NONE);
                }
                if ((aggregateDataCards != null) && !aggregateDataCards.isEmpty()) {
                    for (AggregateDataCard aggregateDataCard : aggregateDataCards) {
                        specValiditiesAndQuantitiesForSpecType.putAll(loadSpecificationsForDatacard(project, aggregateDataCard, specType));
                    }
                }
            }

            if (!specValiditiesAndQuantitiesForSpecType.isEmpty()) {
                specValiditiesAndQuantitiesLocal.put(specType.getDbValue(), specValiditiesAndQuantitiesForSpecType);
            }
        }

        specValiditiesAndQuantities = specValiditiesAndQuantitiesLocal;
    }

    /**
     * Methode zum Laden und Filtern von Spezifikationen anhand einer Fahrzeug- oder Aggregate-Datenkarte
     *
     * @param project
     * @param dataCard
     * @param specType
     * @return
     */
    private Map<String, iPartsMbSpecData> loadSpecificationsForDatacard(EtkProject project, AbstractDataCard dataCard, iPartsSpecType specType) {
        Map<String, iPartsMbSpecData> specValiditiesAndQuantitiesForSpecType = new TreeMap<>();
        Set<String> dataCardCodes = getFilterCodes().getAllCheckedValues();
        if (dataCard != this) { // Die Code der Aggregate-Datenkarte ebenfalls berücksichtigen
            dataCardCodes = new TreeSet<>(dataCardCodes);
            dataCardCodes.addAll(dataCard.getFilterCodes().getAllCheckedValues());
        }
        String modelNo = dataCard.getModelNo();
        iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNo));
        Map<String, iPartsDataModelOil> specValidityAndCode = model.getSpecValidityAndCode(project, specType);
        List<iPartsDataModelOilQuantity> quantityAndValidities = model.getQuantityAndValidities(project, specType);

        // Ident ab/bis von den iPartsDataModelOilQuantity-Datensätzen mit der Motoridentnummer filtern
        int datacardIdent = dataCard.getEndNumber(project);
        List<iPartsDataModelOilQuantity> quantityAndValiditiesForIdent;
        if (datacardIdent != FinId.INVALID_SERIAL_NUMBER) {
            quantityAndValiditiesForIdent = new DwList<>();
            for (iPartsDataModelOilQuantity modelQuantity : quantityAndValidities) {
                int identTo = modelQuantity.getFieldValueAsInteger(FIELD_DMOQ_IDENT_TO);
                if ((identTo > 0) && (datacardIdent >= identTo)) { // Ident bis ist exklusiv
                    continue;
                }

                int identFrom = modelQuantity.getFieldValueAsInteger(FIELD_DMOQ_IDENT_FROM);
                if ((identFrom > 0) && (datacardIdent < identFrom)) { // Ident ab ist inklusiv
                    continue;
                }

                quantityAndValiditiesForIdent.add(modelQuantity);
            }
        } else {
            quantityAndValiditiesForIdent = quantityAndValidities;
        }

        if (!Utils.isValid(dataCardCodes)) {
            iPartsDataModelOilQuantity singleModelOilQuantity = checkOnlyOneModelOilQuantity(quantityAndValiditiesForIdent, modelNo, project);
            // Keine Code auf der Datenkarte -> alle Spezifikationen und Mengen mit passender Endnummer sind gültig
            for (String specValidity : specValidityAndCode.keySet()) {
                iPartsMbSpecData mbSpecData = new iPartsMbSpecData();
                mbSpecData.setSpec(specValidity);
                mbSpecData.setQuantity(singleModelOilQuantity.getFieldValue(FIELD_DMOQ_QUANTITY));
                specValiditiesAndQuantitiesForSpecType.put(specValidity, mbSpecData);
            }
            return specValiditiesAndQuantitiesForSpecType; // theoretisch können es mehrere Motoren sein
        }

        // Filterung der Mengen-Datensätze nach Code
        List<iPartsDataModelOilQuantity> quantityAndValiditiesMatchingCode = new DwList<>();
        for (iPartsDataModelOilQuantity modelOilQuantity : quantityAndValiditiesForIdent) {
            if (iPartsFilterHelper.basicCheckCodeFilterForDatacard(modelOilQuantity.getFieldValue(FIELD_DMOQ_CODE_VALIDITY),
                                                                   dataCardCodes)) {
                quantityAndValiditiesMatchingCode.add(modelOilQuantity);
            }
        }
        // Bei mehreren Treffer wird geprüft, ob sie alle die gleiche Nachfüllmenge besitzen. Egal, ob die Mengen gleich
        // sind oder nicht. Hier wird sichergestellt, dass nur ein Datensatz übrig bleibt. Entweder mit der Nachfüllmenge,
        // die bei allen gleich ist oder bei unterschiedlichen Nachfüllmengen ein Datensatz mit einer leeren Nachfüllmenge.
        iPartsDataModelOilQuantity singleModelOilQuantity = checkOnlyOneModelOilQuantity(quantityAndValiditiesMatchingCode, modelNo, project);

        // Filterung der Spezifikationen-Datensätze nach Code
        for (Map.Entry<String, iPartsDataModelOil> specEntry : specValidityAndCode.entrySet()) {
            if (iPartsFilterHelper.basicCheckCodeFilterForDatacard(specEntry.getValue().getFieldValue(FIELD_DMO_CODE_VALIDITY), dataCardCodes)) {
                String specValidity = specEntry.getKey();
                iPartsMbSpecData mbSpecData = new iPartsMbSpecData();
                mbSpecData.setSpec(specValidity);
                mbSpecData.setQuantity(singleModelOilQuantity.getFieldValue(FIELD_DMOQ_QUANTITY));
                mbSpecData.setSaeClass(specEntry.getValue().getSAEClass());
                specValiditiesAndQuantitiesForSpecType.put(specValidity, mbSpecData);
            }
        }
        return specValiditiesAndQuantitiesForSpecType;
    }

    /**
     * Bei mehreren {@link iPartsDataModelOilQuantity} wird geprüft, ob sie alle die gleiche Nachfüllmenge besitzen.
     * Egal, ob die Mengen gleich sind oder nicht, wird hier sichergestellt, dass nur ein Datensatz übrig bleibt.
     * Entweder einer mit der Nachfüllmenge, die bei allen gleich ist oder bei unterschiedlichen Nachfüllmengen ein
     * Datensatz mit einer leeren Nachfüllmenge.
     */
    private iPartsDataModelOilQuantity checkOnlyOneModelOilQuantity(List<iPartsDataModelOilQuantity> quantityAndValidities,
                                                                    String aggregateModel, EtkProject project) {
        if (quantityAndValidities.size() != 1) {
            // Prüfen, ob alle Datensätze dieselbe Nachfüllmenge haben
            boolean hasSingleModelOilQuantity = true;
            String singleModelOilQuantity = "";
            for (iPartsDataModelOilQuantity modelOilQuantity : quantityAndValidities) {
                String currentModelOilQuantity = modelOilQuantity.getFieldValue(FIELD_DMOQ_QUANTITY);
                if (!singleModelOilQuantity.isEmpty() && !singleModelOilQuantity.equals(currentModelOilQuantity)) {
                    hasSingleModelOilQuantity = false;
                    break;
                }
                singleModelOilQuantity = currentModelOilQuantity;
            }
            if (!hasSingleModelOilQuantity) {
                singleModelOilQuantity = ""; // Unterschiedliche Nachfüllmenge -> leeren String zurückgeben
            }

            iPartsDataModelOilQuantity dummyModelOilQuantity = new iPartsDataModelOilQuantity(project, new iPartsModelOilQuantityId(aggregateModel,
                                                                                                                                    "", "", "", ""));
            dummyModelOilQuantity.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            dummyModelOilQuantity.setFieldValue(FIELD_DMOQ_QUANTITY, singleModelOilQuantity, DBActionOrigin.FROM_DB);
            return dummyModelOilQuantity;
        }
        return quantityAndValidities.get(0);
    }

    // Lademethode

    /**
     * Befüllt die Parameter der Fahrzeug Datenkarte mit Werten aus dem übergebenen JSON Objekt. Bei nicht vorhandenen Daten
     * wird {@code null} gesetzt. Füllt auch die entsprechenden enthaltenen Aggregate Datenkarten.
     *
     * @param jsonObject
     * @param project
     * @param loadFixingParts Bestimmt ob Befestigungsteile mit der Datenkarte initial geladen werden sollen
     * @return
     */
    public boolean loadFromJSONObject(iPartsWSvehicleInclMasterData jsonObject, EtkProject project, boolean loadFixingParts) throws DataCardRetrievalException {
        clear();
        dbLanguage = project.getDBLanguage();
        String fin = jsonObject.getFin();
        if (StrUtils.isValid(fin)) {
            isLoaded = true;
            // Ohne FIN macht die weitere Verarbeitung wahrscheinlich keinen Sinn
            FinId finId = new FinId(fin);
            setFinId(finId, project);

            setModelNo(iPartsConst.MODEL_NUMBER_PREFIX_CAR + jsonObject.getVehicleModelDesignation(), project);
            setProductGroupIndication(jsonObject.getProductGroupIndication());

            iPartsWSactiveAssignedFpd fpdData = jsonObject.getActiveAssignedFpd();
            loadFromJSONActiveAssignedFpd(fpdData, project, true);

            // Baureihe und Produktgruppe für angereicherte Aggregate-Datenkarten setzen
            String seriesNo = iPartsModel.getInstance(project, new iPartsModelId(getModelNo())).getSeriesId().getSeriesNumber();
            for (TwoGridValues.ValueState valueState : getCodes().getBottomGridValues()) {
                valueState.sourceSeriesNumber = seriesNo;
                valueState.sourceProductGroup = getProductGroup();
            }

            iPartsWSactiveProductDate productDate = jsonObject.getActiveProductDate();
            if (productDate != null) {
                setTechnicalApprovalDate(productDate.getTechnicalApprovalDate());
            }

            iPartsWSactiveState activeState = jsonObject.getActiveState();
            if (activeState != null) {
                setVin(activeState.getVin());
                setDateOfTechnicalState(activeState.getDateOfTechnicalState());
                setLifeCycleStatus(LifeCycleStatus.fromString(activeState.getStatus()));
                setOrderNumber(activeState.getOrderNumber());

                // vpd = Federn und Brennstoffzelle
                if (jsonObject.getActiveAssignedFpd() != null) {
                    List<iPartsWSvpd> vpdList = jsonObject.getActiveAssignedFpd().getVpd();
                    iPartsDataCardRetrievalHelper.SpringData springData = iPartsDataCardRetrievalHelper.convertVpdListToStringList(vpdList);
                    if (springData != null) {
                        setActiveSpringLegFront(springData.springLegFront);
                        setActiveSpringRear(springData.springRear);
                        setActiveSpringShimRear(springData.springShimRear);
                    }

                    // Datenkarte für Brennstoffzelle aus VPD Daten erzeugen, falls vorhanden.
                    AggregateDataCard fuelCellDatacard = iPartsDataCardRetrievalHelper.createFuelCellDatacardFromVpdList(vpdList, project);
                    if (fuelCellDatacard != null) {
                        addActiveAggregate(fuelCellDatacard);
                    }
                }

                // Aggregate
                addAggregateIfNotNull(DCAggregateTypes.ENGINE, activeState.getEngine(), project, finId);
                AggregateDataCard transmissionDataCard = addAggregateIfNotNull(DCAggregateTypes.TRANSMISSION, activeState.getTransmission(), project, finId);
                addAggregateIfNotNull(DCAggregateTypes.TRANSFER_CASE, activeState.getTransferCase(), project, finId);
                addAggregateListIfNotNull(DCAggregateTypes.AXLE, activeState.getAxle(), project, finId);
                addAggregateIfNotNull(DCAggregateTypes.AFTER_TREATMENT_SYSTEM, activeState.getAfterTreatmentSystem(), project, finId);
                addAggregateListIfNotNull(DCAggregateTypes.ELECTRO_ENGINE, activeState.getElectroEngine(), project, finId);
                addAggregateListIfNotNull(DCAggregateTypes.FUEL_CELL, activeState.getFuelCell(), project, finId);
                addAggregateListIfNotNull(DCAggregateTypes.HIGH_VOLTAGE_BATTERY, activeState.getHighVoltageBattery(), project, finId);

                // Sonderbehandlung für Aufbau/Fahrerhaus
                AggregateDataCard cabDatacard = addAggregateIfNotNull(DCAggregateTypes.CAB, activeState.getCab(), project, finId);
                if (cabDatacard == null) {
                    boolean doCabFallback = false;

                    // Passende Fahrzeug-Produkte ermitteln und prüfen, ob dort bei mind. einem der Fallback aktiv ist
                    List<iPartsProduct> productsForVehicleModel = iPartsFilterHelper.getAutoSelectProducts(getModelNo(), getFinId(),
                                                                                                           getCodes(), project);
                    if (productsForVehicleModel != null) {
                        for (iPartsProduct product : productsForVehicleModel) {
                            if (product.isUseProductionAggregates()) {
                                doCabFallback = true;
                                break;
                            }
                        }
                    }

                    if (doCabFallback) {
                        iPartsWSactiveProductionInfo productionInfo = jsonObject.getActiveProductionInfo();
                        if (productionInfo != null) {
                            String originCabId = productionInfo.getOriginCabId();
                            if (StrUtils.isValid(originCabId)) {
                                String cabModel = StrUtils.copySubString(originCabId, 0, 6);
                                if (!cabModel.isEmpty()) {
                                    cabDatacard = new AggregateDataCard(false);
                                    cabDatacard.fillByModel(project, iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + cabModel);
                                    addActiveAggregate(cabDatacard);
                                }
                            }
                        }
                    }
                }

                // Pritsche benötigt eine Sonderbehandlung, da es nur ein String ist und keine eigene Datenstruktur
                String pallet = activeState.getPallet();
                if (StrUtils.isValid(pallet)) {
                    PlatformIdent platformIdent = new PlatformIdent(project, pallet);
                    AggregateDataCard platformDatacard = new AggregateDataCard(false);
                    platformDatacard.isLoaded = true;
                    platformDatacard.source = finId;
                    platformDatacard.setAggregateBasicType(DCAggregateTypes.PLATFORM, null);
                    platformDatacard.setAggregateIdent(platformIdent.getPlatformIdent());
                    platformDatacard.setModelNo(platformIdent.getFullModelNo().toUpperCase(), project);
                    addActiveAggregate(platformDatacard);
                }

                // Pseudeo-Aggregat für die Lenkung
                if (StrUtils.isValid(activeState.getSteeringInfoId())) {
                    SteeringIdent steeringIdent = new SteeringIdent(project, activeState.getSteeringInfoId());
                    AggregateDataCard steeringDataCard = new AggregateDataCard(false);
                    steeringDataCard.isLoaded = true;
                    steeringDataCard.source = finId;
                    steeringDataCard.setAggregateBasicType(DCAggregateTypes.STEERING, null);
                    steeringDataCard.setAggregateIdent(steeringIdent.getSteeringIdent());
                    steeringDataCard.setObjectNo(activeState.getSteeringInfoObjectNo());
                    steeringDataCard.setModelNo(steeringIdent.getFullModelNo().toUpperCase(), project);
                    addActiveAggregate(steeringDataCard);
                }

                // Wenn Getriebe-Datenkarte vorhanden, dann Getriebeart von dieser Datenkarte in die Fahrzeug-Datenkarte übernehmen
                if (transmissionDataCard != null) {
                    gearboxValue = transmissionDataCard.getGearboxValue();
                }

                // Sales Area Information
                if (activeState.getSalesAreaInformation() != null) {
                    setSalesAreaInformation(activeState.getSalesAreaInformation());
                }
            }
            // Korrektur der SAAs in Form einer speziellen Anreicherung unabhängig von den restlichen Anreicherungen
            iPartsProduct product = iPartsModel.getFirstAPSFilteredProduct(project, new iPartsModelId(getModelNo()), getFinId(), this);
            correctDatacardSAAs(product);
            enrichDataCard(project);

            // Befestigungsteile nur nachladen wenn das entsprechende Flag in der Datenkarte gesetzt ist,
            // sonst weitere Ladeversuche unterbinden
            fixingPartsAvailable = StrUtils.toBoolean(jsonObject.getFixingPartsAvailable());
            if (loadFixingParts && fixingPartsAvailable) {
                loadFixingParts();
            }

            prodOrderTextAvailable = StrUtils.toBoolean(jsonObject.getProdOrderTextAvailable());
            fieldOrganisationTextAvailable = (activeState != null) && (activeState.getFieldOrganisationText() != null);

            // Ereignis für das Fahrzeug und alle Aggregate (erst nach der Anreicherung) bestimmen
            setEvent(getEventFromDatacardCodes(project));
            if (activeAggregates != null) {
                for (List<AggregateDataCard> aggregateDataCards : activeAggregates.values()) {
                    for (AggregateDataCard aggregateDataCard : aggregateDataCards) {
                        aggregateDataCard.setEvent(aggregateDataCard.getEventFromDatacardCodes(project));
                    }
                }
            }

            // Valide Spezifikationen mit den Aggregatebaumuster ermitteln und an der Datenkarte speichern
            loadValidSpecifications(project);
            return true;
        } else {
            specValiditiesAndQuantities = new TreeMap<>(); // Leere Map, da ohne FIN beim Laden keine Öl-Spezifikationen ermittelt werden sollen
        }
        return false;
    }

    private void enrichDataCard(EtkProject project) {
        iPartsProduct product = iPartsModel.getFirstAPSFilteredProduct(project, new iPartsModelId(getModelNo()), getFinId(), this);
        if (product == null) {
            enrichDataCardRegular(project);
            return;
        }
        iPartsDocumentationType documentationType = product.getDocumentationType();
        if (!documentationType.isPKWDocumentationType()) {
            enrichDataCardRegular(project);
            return;
        }
        Set<String> asClasses = product.getAsProductClasses();
        if ((asClasses == null) || asClasses.isEmpty()) {
            enrichDataCardRegular(project);
            return;
        }
        List<AggregateDataCard> specialDataCards = new ArrayList<>(); //Motor, Getriebe, Brennstoffzelle
        List<AggregateDataCard> otherDataCards = new ArrayList<>();
        for (AggregateDataCard aggregateDataCard : getAllAggregateDatacards()) {
            // Check, ob es ein Motor, ein Getriebe, eine Brennstoffzelle oder eine Hochvoltbatterie ist
            if (aggregateDataCard.getAggregateType().isZBEnrichmentType()) {
                specialDataCards.add(aggregateDataCard);
            } else {
                otherDataCards.add(aggregateDataCard);
            }
        }
        if (specialDataCards.isEmpty()) {
            enrichDataCardRegular(project, otherDataCards);
            return;
        }
        enrichDataCardSpecialDIALOGLogic(project, otherDataCards, specialDataCards, asClasses);
    }

    /**
     * Anreicherung nach neue DIALOG Sonderlogik (DAIMLER-8890)
     *
     * @param project
     * @param regularAggDataCards
     * @param specialAggDataCards
     * @param productASClasses
     */
    private void enrichDataCardSpecialDIALOGLogic(EtkProject project, List<AggregateDataCard> regularAggDataCards,
                                                  List<AggregateDataCard> specialAggDataCards,
                                                  Set<String> productASClasses) {
        // Prüfung: Handelt es sich um eine Motor- oder Getriebe-Datenkarte und welche AS-Produktklasse?

        // Falls Nein > -- keine ZB-Sachnummernanreicherung durchführen  > Fallback-Verfahren (wie bisher)
        // Nicht Getriebe und nicht Motor Datenkarten sind in der Liste "regularAggDataCards" und müssen wie bisher verarbeitet werden
        enrichDataCardRegular(project, regularAggDataCards);


        // Falls ja, ermittle die ZB Sachnummer (Achtung: Sachnummernkennbuchstaben „A“ voranstellen, wenn dieser fehlt)
        // Bei Fahrzeug ist PKW nur Motor-, Brennstoffzelle- und Hochvoltbatterie-ZB-Anreicherung, sonst Fallback; -> EnrichTypes.ENRICH_FOR_CAR
        // Bei Fahrzeug Transporter ZB-Motor, ZB-Getriebe, ZB-Brennstoffelle und ZB-Hochvoltbatterie -> EnrichTypes.ALL
        // Bei Fahrzeug Smart Nur Fallback -> EnrichTypes.ONLY_FALLBACK
        // Bei Fahrzeug Geländewagen ZB-Motor, ZB-Getriebe, ZB-Brennstoffelle und ZB-Hochvoltbatterie -> EnrichTypes.ALL
        EnrichTypes enrichTypes = EnrichTypes.ALL;
        if (productASClasses.contains(iPartsConst.AS_PRODUCT_CLASS_CAR)) {
            enrichTypes = EnrichTypes.ENRICH_FOR_CAR;
        } else if (productASClasses.contains(iPartsConst.AS_PRODUCT_CLASS_SMART)) {
            enrichTypes = EnrichTypes.ONLY_FALLBACK;
        }
        List<AggregateDataCard> dataCardsForCodeToCodeMapping = new ArrayList<>();
        for (AggregateDataCard aggDataCard : specialAggDataCards) {
            // TTZ Anreicherung wie bisher
            aggDataCard.enrichDataCardByDate(technicalApprovalDate);
            switch (enrichTypes) {
                case ALL:
                    enrichAPartNoToCodeForSpecialAggDataCard(project, aggDataCard, productASClasses, dataCardsForCodeToCodeMapping);
                    break;
                case ENRICH_FOR_CAR:
                    // Check, ob es ein Motor, eine Brennstoffzelle oder eine Hochvoltbatterie ist
                    if (aggDataCard.getAggregateType().isTypeForCarZBEnrichment()) {
                        enrichAPartNoToCodeForSpecialAggDataCard(project, aggDataCard, productASClasses, dataCardsForCodeToCodeMapping);
                    } else {
                        enrichAPartNoToCodeFallback(aggDataCard, productASClasses, dataCardsForCodeToCodeMapping);
                    }
                    break;
                case ONLY_FALLBACK:
                    enrichAPartNoToCodeFallback(aggDataCard, productASClasses, dataCardsForCodeToCodeMapping);
                    break;
            }
        }
        // F-Code Anreicherung soll wie bisher durchgeführt werden (inkl. F-Code entfernen und aus FIN des Fahrzeugs ableiten)
        enrichFCode(project, specialAggDataCards);
        if (!dataCardsForCodeToCodeMapping.isEmpty()) {
            enrichCodeToCode(project, dataCardsForCodeToCodeMapping);
        }
    }

    /**
     * Fallback für die ZB-Sachnummern-Anreicherung (Nur bei Motor- und Getriebedatenkarten)
     *
     * @param dataCard
     * @param productASClasses
     * @param dataCardsForCodeToCodeMapping
     */
    private void enrichAPartNoToCodeFallback(AggregateDataCard dataCard, Set<String> productASClasses, List<AggregateDataCard> dataCardsForCodeToCodeMapping) {
        if (productASClasses.contains(iPartsConst.AS_PRODUCT_CLASS_TRANSPORTER)) {
            if (dataCard.getAggregateType() == DCAggregateTypes.TRANSMISSION) {
                dataCard.enrichDataCardByVehicleCodesList(codes, false);
            } else {
                dataCardsForCodeToCodeMapping.add(dataCard);
            }
        } else {
            // Fall es sich um einen PKW und eine Getriebe-Datenkarte handelt, wird geprüft, ob das Getriebe den
            // Code "ET" hat. Falls ja, werden die Code des Fahrzeugs auf die Getriebe Datenkarte geschrieben.
            boolean enrichEvenWithExistingCodes = productASClasses.contains(iPartsConst.AS_PRODUCT_CLASS_CAR)
                                                  && (dataCard.getAggregateType() == DCAggregateTypes.TRANSMISSION)
                                                  && dataCard.getFilterCodes().contains("ET");
            dataCard.enrichDataCardByVehicleCodesList(codes, enrichEvenWithExistingCodes);
        }
    }

    /**
     * ZB-Sachnummern-Anreicherung für Motor-, Brennstoffzelle- und Getriebedatenkarten mit Bestimmung von Codes über die spezielle
     * Mapping-Tabelle.
     *
     * @param project
     * @param aggDataCard
     * @param productASClasses
     * @param dataCardsForCodeToCodeMapping
     */
    private void enrichAPartNoToCodeForSpecialAggDataCard(EtkProject project, AggregateDataCard aggDataCard,
                                                          Set<String> productASClasses, List<AggregateDataCard> dataCardsForCodeToCodeMapping) {
        // ZB Sachnummer ist leer bzw. existiert nicht --> keine ZB-Sachnummernanreicherung durchführen --> neues Fallback-Verfahren
        if (StrUtils.isEmpty(aggDataCard.getObjectNoAsAPartNo())) {
            enrichAPartNoToCodeFallback(aggDataCard, productASClasses, dataCardsForCodeToCodeMapping);
            return;
        }
        if (!iPartsAggCodeMappingCache.getInstance(project).hasMappingData(aggDataCard.getObjectNoAsAPartNo())) {
            enrichAPartNoToCodeFallback(aggDataCard, productASClasses, dataCardsForCodeToCodeMapping);
            return;
        }
        // Code bestimmen und anreichern
        aggDataCard.enrichAPartNoToCode(project);
        // Spezielle Code, die auf der Fahrzeugdatenkarte stehen, müssen auf Motordatenkarten übernommen werden
        addAdditionalVehicleCodesToEngineAggregate(project, aggDataCard);
    }

    /**
     * Fügt einer Motordatenkarte vorgegebene Fahrzeugcode hinzu, wenn diese Code auf der Fahrzeugdatenkarte vorkommen
     *
     * @param project
     * @param aggDataCard
     */
    private void addAdditionalVehicleCodesToEngineAggregate(EtkProject project, AggregateDataCard aggDataCard) {
        if (aggDataCard.getAggregateType() == DCAggregateTypes.ENGINE) {
            iPartsVehicleToAggregateCodeCache cache = iPartsVehicleToAggregateCodeCache.getInstance(project);
            List<TwoGridValues.ValueState> additionalCodesForEngine = getVehicleCodesForEngineIfExists(cache, codes.getTopGridValues());
            additionalCodesForEngine.addAll(getVehicleCodesForEngineIfExists(cache, codes.getBottomGridValues()));
            aggDataCard.enrichDataCardByVehicleCodesList(additionalCodesForEngine);
        }
    }

    private List<TwoGridValues.ValueState> getVehicleCodesForEngineIfExists(iPartsVehicleToAggregateCodeCache cache, Collection<TwoGridValues.ValueState> gridValues) {
        List<TwoGridValues.ValueState> additionalCodesForEngine = new ArrayList<>();
        for (TwoGridValues.ValueState newCode : gridValues) {
            if (cache.isVehicleToAggregateCode(newCode.value)) {
                additionalCodesForEngine.add(newCode);
            }
        }
        return additionalCodesForEngine;
    }

    private void addAggregateListIfNotNull(DCAggregateTypes type, List<iPartsWSaggregateSubType> aggregateJSONList, EtkProject project, FinId finId) {
        if ((aggregateJSONList != null) && !aggregateJSONList.isEmpty() && (type != null)) {
            for (iPartsWSaggregateSubType aggregateJSON : aggregateJSONList) {
                addAggregateIfNotNull(type, aggregateJSON, project, finId);
            }
        }
    }

    private AggregateDataCard addAggregateIfNotNull(DCAggregateTypes type, iPartsWSaggregateSubType aggregateJSON, EtkProject project, FinId finId) {
        if ((aggregateJSON != null) && (type != null)) {
            AggregateDataCard dataCard = new AggregateDataCard(false);
            dataCard.loadFromJSONObject(type, aggregateJSON, project, finId);
            addActiveAggregate(dataCard);
            return dataCard;
        }

        return null;
    }

    @Override
    public void fillByModel(EtkProject project, String modelNoToLoad) {
        super.fillByModel(project, modelNoToLoad);
        if (isModelLoaded) {
            if (iPartsModel.isVehicleModel(modelNoToLoad)) {
                List<iPartsDataModelsAggs> modelsAggsList = iPartsDataModelsAggsList.loadDataModelsAggsListForModel(project,
                                                                                                                    modelNoToLoad).getAsList();
                TwoGridValues values = new TwoGridValues();
                for (iPartsDataModelsAggs modelsAggs : modelsAggsList) {
                    values.addSingleValue(modelsAggs.getAsId().getAggregateModelNumber(), true);
                }
                setAggregateModelNumbers(values);
            }
        }
    }

    /**
     * Anreicherung der Datenkarte in der Reihenfolge
     * 1. Aggregatedatenkarten mit den Daten aus der Fahrzeugkarte ergänzen DAIMLER-3343
     * 2. F-Code aus FIN/C-Baumuster DAIMLER-3438
     * 3. Mapping Sachnummer auf Code DAIMLER-3418
     * 4. Mapping Code auf Code DAIMLER-3419
     *
     * @param project
     */
    public void enrichDataCardRegular(EtkProject project) {
        if (doEnrichment) {
            List<AggregateDataCard> aggregateDataCards = getAllAggregateDatacards();
            enrichDataCardRegular(project, aggregateDataCards);
        }
    }

    public void enrichDataCardRegular(EtkProject project, List<AggregateDataCard> aggregateDataCards) {
        if (doEnrichment) {
            enrichAggsDataCardsWithVehicleDataCardData(project, aggregateDataCards);
            enrichFCode(project, aggregateDataCards);
            enrichAPartNoToCode(project, aggregateDataCards);
            enrichCodeToCode(project, aggregateDataCards);
        }
    }

    /**
     * Die leeren Aggregatedatenkarten mit den Daten aus der Fahrzeugkarte ergänzen, wenn die Bedingungen passen.
     *
     * @param project
     * @param aggregateDataCards
     */
    public void enrichAggsDataCardsWithVehicleDataCardData(EtkProject project, List<AggregateDataCard> aggregateDataCards) {
        // Manipulation nur ausführen, wenn Fahrzeug-Datenkarte via WebService geladen
        if (isLoaded && !isModelLoaded()) {
            // Der Dokumentationstyp spielt eine essentielle Rolle:
            iPartsDocumentationType documentationType = getDocumentationType(project);
            if (documentationType == iPartsDocumentationType.UNKNOWN) {
                return;
            }

            // Über die Liste der Aggregate der Fahrzeugdatenkarte iterieren:
            for (AggregateDataCard aggDataCard : aggregateDataCards) {
                if (documentationType.isTruckDocumentationType()) {
                    // --------------
                    // ELDAS
                    // --------------
                    aggDataCard.enrichDataCardBySAAList(saas);
                    aggDataCard.enrichDataCardByVehicleCodesList(codes, false);

                } else if (documentationType.isPKWDocumentationType()) {
                    // --------------
                    // DIALOG
                    // --------------
                    aggDataCard.enrichDataCardByDate(technicalApprovalDate);
                    aggDataCard.enrichDataCardByVehicleCodesList(codes, false);
                }
            }
        }
    }

    /**
     * DAIMLER-3438: Mit Aggregate-Datenkarte DIALOG/PKW (PLA) - F-CODE der Fahrgestell-Datenkarte übernehmen
     *
     * @param project
     * @param aggregateDataCards
     */
    public void enrichFCode(EtkProject project, List<AggregateDataCard> aggregateDataCards) {
        if (isLoaded && !isModelLoaded()) {
            // Der Dokumentationstyp spielt eine essentielle Rolle:
            iPartsDocumentationType documentationType = getDocumentationType(project);
            if (!documentationType.isPKWDocumentationType()) {
                return;
            }
            if (!getFinId().isModelTypeValid()) {
                return;
            }
            String fCode = "F" + getFinId().getModelType();
            for (AggregateDataCard aggregateDataCard : aggregateDataCards) {
                aggregateDataCard.enrichFCode(fCode);
            }
        }
    }

    /**
     * DAIMLER-3418: Codeanreicherung von Aggregate-Datenkarten über Mapping "Sachnummer zu Code"
     *
     * @param project
     * @param aggregateDataCards
     */
    public void enrichAPartNoToCode(EtkProject project, List<AggregateDataCard> aggregateDataCards) {
        if (isLoaded && !isModelLoaded()) {
            for (AggregateDataCard aggregateDataCard : aggregateDataCards) {
                aggregateDataCard.enrichAPartNoToCode(project);
            }
        }
    }


    /**
     * Diese Teilenummern sind für den Federfilter relevant.
     * <p>
     * activeSpringLegFront - sind die vorderen Federbeinnummern. Dahinter stecken andere echte Materialnummern (Cache)
     * <p>
     * activeSpringRear - sind die hinteren Federbeinnummern. Dahinter stecken eventuell andere echte Materialnummern (Cache).
     * Ist das nicht der Fall, werden die hinteren Federbeinnummern verwendet.
     * <p>
     * activeSpringShimRear - Federbeilage hinten. Wird direkt zur Filterung verwendet
     *
     * @return
     */
    @Override
    public Collection<String> getSpringPartNumbers(EtkProject project) {
        List<String> result = new ArrayList<>();
        for (String value : activeSpringLegFront) {
            String matNo = getSpringForSpringLegFromCache(project, value);
            if (StrUtils.isValid(matNo)) {
                result.add(matNo);
            }
        }

        for (String value : activeSpringRear) {
            // Seit DAIMLER-11178 soll zu jedem hinteren Federbein (VPD-IDent 65) geprüft werden, ob es gemappte Federn
            // gibt. Falls ja, sollen die gefundenen Federn für den weiteren Filterverlauf verwendet werden. Falls nicht,
            // soll wie bisher das hintere Federbein herangezogen werden.
            String matNo = getSpringForSpringLegFromCache(project, value);
            if (StrUtils.isValid(matNo)) {
                result.add(matNo);
            } else {
                result.add(value);
            }
        }

        result.addAll(activeSpringShimRear);

        return result;
    }

    /**
     * Liefert aus dem Cache die gemappte Feder zum übergebenen Federbein
     *
     * @param project
     * @param value
     * @return
     */
    private String getSpringForSpringLegFromCache(EtkProject project, String value) {
        iPartsSpringMapping springMappingCache = iPartsSpringMapping.getInstance(project);
        // Liste aus dem Cache holen
        iPartsDataSpringMapping springMapping = springMappingCache.getSpringMappingForSpringLeg(value);

        if (springMapping != null) {
            return springMapping.getAttribute(iPartsConst.FIELD_DSM_SPRING).getAsString();
        }
        return null;
    }


    /**
     * DAIMLER-3419: Codeanreicherung von Aggregate-Datenkarten über "Mapping Code zu Code"
     *
     * @param project
     * @param aggregateDataCards
     */
    public void enrichCodeToCode(EtkProject project, List<AggregateDataCard> aggregateDataCards) {
        if (isLoaded && !isModelLoaded()) {
            if (getFinId().isModelTypeValid()) {
                String modelType = getFinId().getModelType();
                Collection<iPartsDataCodeMapping> codeMappingList = iPartsCodeMappingCache.getInstance(project).getCodeMappingsByModelType(modelType);
                if ((codeMappingList != null) && !codeMappingList.isEmpty()) {
                    Map<String, Set<String>> mappingSet = new HashMap<>();
                    for (iPartsDataCodeMapping dataCodeMapping : codeMappingList) {
                        String initialCode = dataCodeMapping.getAsId().getInitialCode();
                        Set<String> newCodesSet = mappingSet.computeIfAbsent(initialCode, k -> new TreeSet<>());
                        newCodesSet.add(dataCodeMapping.getAsId().getTargetCode());
                    }

                    // BM-bildende Code anreichern
                    Set<String> enrichedCodesSet = new TreeSet<>();
                    for (TwoGridValues.ValueState value : codes.getTopGridValues()) {
                        Set<String> newCodesSet = mappingSet.get(value.value);
                        if (newCodesSet != null) {
                            enrichedCodesSet.addAll(newCodesSet);
                        }
                    }
                    if (!enrichedCodesSet.isEmpty()) {
                        for (AggregateDataCard aggregateDataCard : aggregateDataCards) {
                            aggregateDataCard.enrichCodeToCodeByList(enrichedCodesSet, true);
                        }
                    }

                    // Technische Code anreichern
                    enrichedCodesSet.clear();
                    for (TwoGridValues.ValueState value : codes.getBottomGridValues()) {
                        Set<String> newCodesSet = mappingSet.get(value.value);
                        if (newCodesSet != null) {
                            enrichedCodesSet.addAll(newCodesSet);
                        }
                    }
                    if (!enrichedCodesSet.isEmpty()) {
                        for (AggregateDataCard aggregateDataCard : aggregateDataCards) {
                            aggregateDataCard.enrichCodeToCodeByList(enrichedCodesSet, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public TwoGridValues getSaasForFilter() {
        TwoGridValues result = new TwoGridValues();
        result.addTwoGridValues(getSaas());
        // Für den Filter müssen neben den Fahrzeug SAAs auch die SAAs aller Aggregate-Datenkarten berücksichtigt werden
        for (AggregateDataCard aggDataCard : getActiveAggregates()) {
            result.addTwoGridValues(aggDataCard.getSaas());
        }
        return result;
    }

    @Override
    public String getFactorySign(EtkProject project) {
        if ((getFinId() == null) || !getFinId().isValidId()) {
            return "";
        }
        return getFinId().getFactorySign();
    }

    @Override
    public String getFactoryNumber(EtkProject project) {
        if (!factoryAndEndNumberLoaded) {
            // Werksnummer nur dann bestimmen falls sie nicht bereits gesetzt ist
            if (StrUtils.isEmpty(factoryNumber) && (getFinId() != null) && getFinId().isWMIValid() && getFinId().isFactorySignValid()) {
                iPartsFactoryModel factoryModelInstance = iPartsFactoryModel.getInstance(project);
                factoryNumber = factoryModelInstance.getFactoryNumberForWMI(getFinId().getWorldManufacturerIdentifier(),
                                                                            getFinId().getFactorySign(),
                                                                            new iPartsModelId(getFinId().getFullModelNumber()),
                                                                            getFinId().getSteering());
            }
            factoryAndEndNumberLoaded = true;
        }
        return factoryNumber;
    }

    @Override
    public String getSpikeIdent(EtkProject project) {
        if (finId == null) {
            return "";
        } else {
            return finId.getIdent();
        }
    }

    @Override
    public int getEndNumber(EtkProject project) {
        return finId.getSerialNumber();
    }

    @Override
    public int getEndNumberLength(EtkProject project) {
        return FinId.IDENT_NO_LENGTH;
    }


    /**
     * Ermittelt zur allen übergebenen Baumustern die passenden Datenkarten aus dieser Fahrzeugdatenkarte.
     * Wenn die Datenkarte selbst aus einem Baumuster erzeugt wurde, dann wird pro Aggregate-Baumuster-Nr. eine
     * Aggregate Datenkarte erzeugt und zurück gegeben.
     *
     * @param project
     * @param models  Liste der zu suchenden Baumusternummern
     * @return Liste der passenden Aggregatedatenkarten
     */
    @Override
    public List<AbstractDataCard> getRelevantDatacardsForModels(EtkProject project, Collection<String> models) {
        List<AbstractDataCard> result = new DwList<>();

        Set<String> allDatacardsModelNos = getModelNumbersFromAllDatacards();

        for (String datacardsModelNo : allDatacardsModelNos) {
            for (String model : models) {
                if (model.equals(datacardsModelNo)) {
                    result.addAll(getAggregateDataCards(project, datacardsModelNo));
                }
            }
        }
        return result;
    }

    @Override
    public List<AbstractDataCard> getFilterRelevantDatacardsForModels(EtkProject project, Collection<String> models) {
        return getRelevantDatacardsForModels(project, models);
    }

    /**
     * Liefert eine Liste aller Datenkarten die zur aktuellen Datenkarte gehören.
     * Wurde die Datenkarte über den Webservice geladen werden die echten unter Aggregatedatenkarten geliefert,
     * wenn die Datenkarte über das Baumuster gefüllt wurde, werden virtuelle Baumusterdatenkarte geliefert.
     * In der Ergebnisliste ist auch die aktuelle Fahrzeugdatenkarte enthalten.
     *
     * @param project
     * @return Liste aller Datenkarten die zur aktuellen gehören
     */
    @Override
    public List<AbstractDataCard> getAllSelectedSubDatacards(EtkProject project) {
        List<AbstractDataCard> result = new DwList<>();

        Set<String> allDatacardsModelNos = getModelNumbersFromAllDatacards();

        for (String datacardsModelNo : allDatacardsModelNos) {
            result.addAll(getAggregateDataCards(project, datacardsModelNo));
        }
        return result;
    }

    @Override
    public List<AbstractDataCard> getRelevantDatacardsForAssembly(iPartsDataAssembly assembly) {
        // Finde zum übergebenen Modul das Produkt und damit auch die (meistens 1) gültigen Baumuster.
        // Dann wird in der geladenen Fahrzeugdatenkarte geprüft, ob es eine passende Datenkarte gibt.
        // Wenn es keine passende Datenkarte gibt, wird eine Baumuster-Aggregatedatenkarte erzeugt.

        EtkProject project = assembly.getEtkProject();

        if (assembly.isSAAssembly()) {
            // Wenn der Eintrag eine freie SA ist, werden zunächst über DA_SA_MODULES die SA-Nummer(n) zum Modul bestimmt
            // und dann über DA_PRODUCT_SAS die zugehörigen Produkte ermittelt
            List<AbstractDataCard> relevantDataCardsForSA = new DwList<>();

            iPartsDataSAModulesList dataSAModulesList = iPartsDataSAModulesList.loadDataForModule(project, new iPartsModuleId(assembly.getAsId().getKVari()));
            for (iPartsDataSAModules dataSAModules : dataSAModulesList) {
                String saNo = dataSAModules.getAsId().getSaNumber();
                if (!saNo.isEmpty()) {
                    Set<String> modelNumbersFromAllDatacards = getModelNumbersFromAllDatacards();
                    Map<String, String> saProductNumbers = new HashMap<>(); // Map von Produkt auf Baumuster
                    iPartsSAId saId = new iPartsSAId(saNo);
                    Set<iPartsProductId> productIds = iPartsSA.getInstance(project, saId).getProductIdsToKGsMap(project).keySet();
                    for (iPartsProductId productId : productIds) {
                        if (!saProductNumbers.containsKey(productId.getProductNumber())) {
                            iPartsProduct product = iPartsProduct.getInstance(project, productId);

                            // Passt das Produkt überhaupt zu einem Baumuster der aktiven (Aggregate-)Datenkarten?
                            Set<String> modelNumbersForProduct = product.getModelNumbers(project);
                            for (String modelNumber : modelNumbersFromAllDatacards) {
                                if (modelNumber.isEmpty() || modelNumbersForProduct.contains(modelNumber)) {
                                    saProductNumbers.put(productId.getProductNumber(), modelNumber);
                                    // 1 Produkt pro Baumuster genügt, da in getRelevantDatacardsForProduct() sowieso wieder
                                    // alle Produkte zum Baumuster bestimmt werden
                                    break;
                                }
                            }
                        }
                    }

                    for (Map.Entry<String, String> productModelEntry : saProductNumbers.entrySet()) {
                        iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productModelEntry.getKey()));
                        relevantDataCardsForSA.addAll(getRelevantDatacardsForProduct(product, productModelEntry.getValue(),
                                                                                     project));
                    }
                }
            }

            return relevantDataCardsForSA;
        }

        boolean virtual = assembly.getAsId().isVirtual();
        if (!virtual) { // hier landet man nur bei echten Stücklisten
            iPartsProductId productIdFromModuleUsage = assembly.getProductIdFromModuleUsage();
            if (productIdFromModuleUsage != null) {
                iPartsProduct product = iPartsProduct.getInstance(project, productIdFromModuleUsage);
                Set<String> modelNumbersForProduct = product.getModelNumbers(project);
                for (String modelNumber : getModelNumbersFromAllDatacards()) {
                    if (modelNumber.isEmpty() || modelNumbersForProduct.contains(modelNumber)) {
                        return getRelevantDatacardsForProduct(product, modelNumber, project);
                    }
                }
            }

            return new DwList<>(0);
        } else {
            List<iPartsVirtualNode> virtualNodesPath = assembly.getVirtualNodesPath();
            if (iPartsVirtualNode.isStructureNode(virtualNodesPath)) { // sind alle oberen Knoten (Aggregate, Fahrzeuge, Konstruktion, Spezial) + nächste Ebene
                // Bei Struktur-Knoten alle Datenkarten zurückliefern, damit die mehrstufige Filterung im Baum funktioniert

                return getAllSelectedSubDatacards(project);
            } else {
                if ((virtualNodesPath != null) && (virtualNodesPath.size() == 1) && virtualNodesPath.get(0).getType().equals(iPartsNodeType.ROOT)) {
                    List<AbstractDataCard> result = new DwList<>(1);
                    result.add(this);
                    return result;
                }

                // Produkt- und Hierarchie-Knoten wie z.B. alles unterhalb von Motoren
                String productNumberFromAssemblyId = iPartsVirtualNode.getProductNumberFromAssemblyId(assembly.getAsId());
                if (productNumberFromAssemblyId != null) {
                    iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNumberFromAssemblyId));
                    Set<String> modelNumbersForProduct = product.getModelNumbers(project);
                    for (String modelNumber : getModelNumbersFromAllDatacards()) {
                        if (modelNumber.isEmpty() || modelNumbersForProduct.contains(modelNumber)) {
                            return getRelevantDatacardsForProduct(product, modelNumber, project);
                        }
                    }
                }

                return new DwList<>(0);
            }
        }
    }

    /**
     * Liefert ein Set mit den Baumustern von allen aktiven (Aggregate-)Datenkarten zurück.
     *
     * @return
     */
    public Set<String> getModelNumbersFromAllDatacards() {
        if (modelNumbersFromAllDatacards == null) {
            Set<String> modelNumbersFromAllDatacardsLocal = new TreeSet<>(getAggregateModelNumbers().getAllCheckedValues());
            modelNumbersFromAllDatacardsLocal.add(getModelNo());
            modelNumbersFromAllDatacards = Collections.unmodifiableSet(modelNumbersFromAllDatacardsLocal);
        }
        return modelNumbersFromAllDatacards;
    }

    private List<AbstractDataCard> getRelevantDatacardsForProduct(iPartsProduct product, String modelNumber, EtkProject project) {
        iPartsModelId modelId = new iPartsModelId(modelNumber);
        List<iPartsProduct> productsForModel;
        if (!modelNumber.isEmpty()) {
            // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster sollen beim Edit aber nicht bei den
            // Webservices berücksichtigt werden.
            productsForModel = iPartsProductHelper.getProductsForModelAndSessionType(project, modelId, null, null);
        } else {
            productsForModel = new DwList<>(1);
            productsForModel.add(product);
        }

        // DAIMLER-6814: Ist ein Baumuster nur einem Produkt zugeordnet, dann wird das Produkt zur Filterung
        // herangezogen, auch wenn die Ident-AB/BIS-Werte und/oder die Coderegel nicht zutreffen.
        int visibleProductCount = 0;
        for (iPartsProduct productForModel : productsForModel) {
            if (productForModel.isRetailRelevant()) { // == FIELD_DP_PRODUCT_VISIBLE
                visibleProductCount++;
                if (visibleProductCount > 1) {
                    break;
                }
            }
        }

        // Check, ob das Produkt für die FIN gültig ist
        // APS-Check nur durchführen bei mehr als einem sichtbaren Produkt und bei nur unsichtbaren Produkten; bei nur einem
        // (unsichtbaren) Produkt in der Liste -> kein APS-Check durchführen wegen Anzeige in iPartsEdit
        if (((visibleProductCount == 1) && product.isRetailRelevant()) || (productsForModel.size() == 1)
            || ((visibleProductCount != 1) && isProductAutoSelectValid(project, product, modelId))) {
            Set<String> models = product.getModelNumbers(project);
            return getRelevantDatacardsForModels(project, models);
        } else {
            return new DwList<>(0);
        }
    }

    @Override
    public Collection<iPartsProduct> getSubDatacardsProducts(EtkProject project) {
        Set<iPartsProduct> additionalAggregatesSet = new TreeSet<>(Comparator.comparing(o -> o.getAsId().getProductNumber()));

        for (String aggregateModelNo : getAggregateModelNumbers().getAllCheckedValues()) {
            // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster sollen beim Edit aber nicht bei den
            // Webservices berücksichtigt werden.
            List<iPartsProduct> productsForModel = iPartsProductHelper.getProductsForModelAndSessionType(project, new iPartsModelId(aggregateModelNo),
                                                                                                         null, null);
            additionalAggregatesSet.addAll(productsForModel);
        }

        if (additionalAggregatesSet.isEmpty()) {
            additionalAggregatesSet = null;
        }
        return additionalAggregatesSet;
    }

    private boolean isProductAutoSelectValid(EtkProject project, iPartsProduct product, iPartsModelId modelId) {
        return iPartsFilterHelper.isProductValidAfterAutoProdSelect(project, getFinId(), modelId, product,
                                                                    hasTechnicalCodes() ? getCodes().getAllCheckedValues() : null);
    }

    /**
     * Eine Datenkarte mit diesem Status darf nicht verwendet werden
     *
     * @return
     */
    public boolean hasForbiddenLifeCycleStatus() {
        return isForbiddenLifeCycleStatus(lifeCycleStatus);
    }

    /**
     * Liefert zurück, ob diese Datenkarte gültig ist für den Endnummern-Filter
     * Wir müssen hier die Fälle für Aufruf aus dem Endnummernfilter sowie aus dem Filterdialog berücksichtigen.
     * Bei Aufruf aus dem Endnummernfilter für Stücklisteneintrag gibt es ein eindeutiges Produkt. Bei Aufruf aus dem Filterdialog kann es mehrere
     * Produkte geben, die über den APS-Mechanismus ermittelt wurden.
     * Das Valid-Kriterium unterscheidet sich für TTZ-Produkte von normalen Produkten. Wenn wir bei Aufruf aus dem Filterdialog
     * mehrere Produkte haben, nehmen wir "valid" an wenn es mindestens ein valides Produkt gibt.
     *
     * @param products ein Produkt o. null für Aufruf aus Endnummmernfilter; ggf. mehrere Produkte für Aufruf aus Filterdialog
     * @param project
     * @return
     */
    @Override
    public boolean isValidForEndNumberFilter(List<iPartsProduct> products, EtkProject project) {
        if ((project == null) || (products == null) || products.isEmpty()) {
            return super.isValidForEndNumberFilter(products, project);
        }
        for (iPartsProduct product : products) {
            boolean isValid;
            // bei ungültigem TTZ Rückfall auf das Standardverfahren
            if (product.isTtzFilter() && StrUtils.isValid(getDateOfTechnicalState())) {
                isValid = true;
            } else {
                List<iPartsProduct> singleProductList = new ArrayList<>(1);
                singleProductList.add(product);
                isValid = super.isValidForEndNumberFilter(singleProductList, project);
            }
            if (isValid) {
                return true;
            }
        }
        return false;
    }

    public List<iPartsWSsalesAreaInformationObject> getSalesAreaInformation() {
        return salesAreaInformation;
    }

    public void setSalesAreaInformation(List<iPartsWSsalesAreaInformationObject> salesAreaInformation) {
        this.salesAreaInformation = salesAreaInformation;
    }

    /**
     * Bestellnummer
     *
     * @return Kann auch {@code null} sein
     */
    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * Land für den Ländergültigkeits-Filter
     *
     * @return Kann auch {@code null} sein, falls kein Land ausgewählt wurde
     */
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = StrUtils.getEmptyOrValidString(country).toUpperCase();
    }

    /**
     * Spezifikationen für den Spezifikations-Filter aufsteigend sortiert
     * inkl. SAE Klassen (falls vorhanden)
     *
     * @param project
     * @return
     */
    public Set<String> getSpecValidities(EtkProject project) {
        Set<String> filterDialogSpecs = new TreeSet<>();
        getSpecValiditiesAndQuantities(project).values().forEach(
                specs -> specs.forEach((spec, mbSpecData) -> {
                    String saeClass = mbSpecData.getSaeClass();
                    String filterDialogSingleSpec = spec + (StrUtils.isValid(saeClass) ? " (" + saeClass + ")" : "");
                    if (StrUtils.isValid(filterDialogSingleSpec)) {
                        filterDialogSpecs.add(filterDialogSingleSpec);
                    }
                })
        );
        return filterDialogSpecs;
    }

    /**
     * Map mit Spezifikationen für den Spezifikations-Filter
     *
     * @param project
     * @return
     */
    public Map<String, Map<String, iPartsMbSpecData>> getSpecValiditiesAndQuantities(EtkProject project) {
        if (specValiditiesAndQuantities == null) {
            loadValidSpecifications(project);
        }
        return specValiditiesAndQuantities;
    }

    /**
     * Liefert Spezifikationen für einen Spezifikationstypen
     *
     * @param project
     * @return
     */
    public Map<String, iPartsMbSpecData> getSpecValiditiesAndQuantitiesForSpecType(EtkProject project, iPartsSpecType specType) {
        Map<String, iPartsMbSpecData> specValiditiesAndQuantitiesForSpecType = null;
        Map<String, Map<String, iPartsMbSpecData>> specValiditiesAndQuantitiesLocal = getSpecValiditiesAndQuantities(project);
        if (specValiditiesAndQuantitiesLocal != null) {
            specValiditiesAndQuantitiesForSpecType = specValiditiesAndQuantitiesLocal.get(specType.getDbValue());
        }
        if (specValiditiesAndQuantitiesForSpecType == null) {
            specValiditiesAndQuantitiesForSpecType = new TreeMap<>();
        }
        return specValiditiesAndQuantitiesForSpecType;
    }
}