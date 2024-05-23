/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects.iPartsWSactiveAssignedFpd;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects.iPartsWSactiveProductDate;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects.iPartsWSaggregateSubType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EnrichReasons;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TwoGridValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductHelper;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collection;
import java.util.List;

/**
 * Aggregate-Datenkarte
 */
public class AggregateDataCard extends AbstractDataCard {

    @JsonProperty
    protected DCAggregateTypes aggregateType;
    @JsonProperty
    protected DCAggregateTypeOf aggregateTypeOf;
    @JsonProperty
    protected String aggregateIdent;
    @JsonProperty
    protected AggregateIdent aggIdent = null;  // müsste eigentlich aggregateIdent heißen; der Name ist aber schon vergeben
    @JsonProperty
    protected String objectNo;
    @JsonProperty
    protected String objectNoVariant;
    @JsonProperty
    protected boolean dataCardAvailable;
    protected VehicleDataCard parentDatacard;
    @JsonProperty
    protected boolean isApprovalDateEnriched;
    @JsonProperty
    protected String factorySign = "";
    @JsonProperty
    protected boolean useVehicleFactoryNumber;
    @JsonProperty
    protected String endNumberString = "";
    @JsonProperty
    protected int endNumber = FinId.INVALID_SERIAL_NUMBER;  // Seriennummer ohne Millionenüberlauf
    @JsonProperty
    protected int endNumberLength = -1; // für die Mio-Überlauf-Berechnung nötig
    @JsonProperty
    protected String aggregateTypeFromModel;
    @JsonProperty
    protected FinId source;
    @JsonProperty
    protected boolean isOldIdentSpecification = false;
    @JsonProperty
    protected volatile boolean isEldasAggregateInDIALOGVehicle;
    @JsonProperty
    protected volatile boolean isEldasAggregateInDIALOGVehicleCalculated;

    /**
     * Erzeugt eine neue Aggregate-Datenkarte für den übergebenen Ident-Code und Aggregatetyp
     * Ist das Flag <i>loadDataCard</i> gesetzt, wird zuerst versucht die Datenkarte per Webservice zu laden.
     * Schlägt entweder das Laden fehl, oder das Flag <i>loadDataCard</i> war nicht gesetzt oder es exisitiert keine
     * Datenkarte für den Ident und Typ, und das Flag <i>createModelDataCard</i> ist gesetzt, wird versucht eine Baumuster-
     * Datenkarte zu erzeugen. Handelt es sich bei <i>identCode</i> nicht um ein 6-stelliges Baumuster mit oder ohne
     * Sachnummernkennbuchstabe, oder um einen Aggregate-Ident (länger 6 Zeichen), wird keine Datenkarte erstellt.
     *
     * @param dcAggregateType      Aggregatetyp
     * @param identCode            Aggregate-Ident oder 6-stelliges Baumuster mit oder ohne Sachnummernkennbuchstabe
     * @param loadDataCard         gibt an, ob die Datenkarte per Webservice geladen werden soll
     * @param createModelDataCard  gibt an, ob eine Baumuster-Datenkarte mit dem identCode erstellt werden soll
     * @param loadDataCardCallback Optionaler Callback beim Laden der Datenkarte
     * @param project
     * @throws DataCardRetrievalException
     */
    public static AggregateDataCard getAggregateDataCard(DCAggregateTypes dcAggregateType, String identCode, boolean loadDataCard,
                                                         boolean createModelDataCard,
                                                         iPartsDataCardRetrievalHelper.LoadDataCardCallback loadDataCardCallback,
                                                         EtkProject project) throws DataCardRetrievalException {
        // Bei vorhandener Fahrzeug-Datenkarte ist ein Nachladen der Aggregate-Datenkarten über den separaten Webservice
        // nicht gewünscht. D.h. normalerweise ist loadDataCard false.
        // Der Aggregatedatenkarten Webservice soll nur für eigenständige Aggregate-Datenkarten aufgerufen werden
        if (loadDataCard && (dcAggregateType != null)) {
            AggregateDataCard aggregateDataCard = iPartsDataCardRetrievalHelper.getAggregateDataCard(project, dcAggregateType,
                                                                                                     identCode, loadDataCardCallback);
            if ((aggregateDataCard != null) && aggregateDataCard.isLoaded) {
                return aggregateDataCard;
            }
        }

        // Optional eine Baumuster-Datenkarte erzeugen falls keine Datenkarte gefunden wurde bzw. identCode gar kein Aggregate-Ident ist
        AggregateDataCard aggregateDataCard = new AggregateDataCard();
        if (createModelDataCard) {
            // Ist identCode ein Aggregate-Ident?
            String aggregateIdent = identCode;
            if (iPartsModelId.isModelNumberValid(identCode, false)) {
                // identCode ist nur ein Baumuster mit oder ohne Sachnummernkennbuchstabe und kein vollständiger Aggregate-Ident
                aggregateIdent = null;
            }

            // Baumuster aus identCode extrahieren
            String modelFromIdent = identCode;
            if (!iPartsModel.isModelNumberValid(modelFromIdent)) {
                // identCode ist kein Baumuster mit Sachnummernkennbuchstabe -> entweder Baumuster ohne Sachnummernkennbuchstabe
                // oder Aggregate-Ident, wobei in beiden Fällen AggregateIdent.getModelFromIdent() die Baumuster-Nummer zurückliefert
                modelFromIdent = iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + AggregateIdent.getModelFromIdent(identCode);
            }
            if (iPartsModel.isModelNumberValid(modelFromIdent)) { // 6-stelliges Baumuster mit Sachnummernkennbuchstabe
                aggregateDataCard.fillByModel(project, modelFromIdent);
                if (aggregateIdent != null) {
                    aggregateDataCard.setAggregateIdent(identCode);
                }
            }
        }
        return aggregateDataCard;
    }

    /**
     * Für die Bestimmung der Werkseinsatzdaten wird die Länge der Endnummer basierend auf dem Aggregate-Typ benötigt.
     * Diese ist aber normalerweise vom konkreten Aggregate-Ident abhängig und je nach Alt/Neu-Systematik unterschiedlich.
     * Da an dieser Stelle aber der konkrete Ident nicht bestimmt werden kann, wird in dieser Funktion die Länge der
     * Endnummer anhand eines Dummy Idents ermittelt.
     *
     * @param project
     * @param aggregateType Aggregatetyp
     * @return
     */
    public static int getEndNumberLengthForAggregateType(EtkProject project, String aggregateType) {
        AggregateDataCard aggregateDataCard = new AggregateDataCard(true);
        aggregateDataCard.setAggregateIdent("123456789012"); // 12-stelliger Dummy-Ident
        aggregateDataCard.setAggregateBasicType(DCAggregateTypes.getDCAggregateTypeByAggregateType(aggregateType), DCAggregateTypeOf.NONE);
        return aggregateDataCard.getEndNumberLength(project);
    }

    public AggregateDataCard() {
        super();
    }

    public AggregateDataCard(boolean isVirtual) {
        super(isVirtual);
    }

    @Override
    public void clearCache() {
        super.clearCache();
        isEldasAggregateInDIALOGVehicleCalculated = false;
    }

    @Override
    public void setDataCardFlagsByDataCard(AbstractDataCard dataCard) {
        super.setDataCardFlagsByDataCard(dataCard);
        if ((dataCard != null) && dataCard.isAggregateDataCard()) {
            AggregateDataCard aggregateDataCard = (AggregateDataCard)dataCard;
            dataCardAvailable = aggregateDataCard.dataCardAvailable;
            isApprovalDateEnriched = aggregateDataCard.isApprovalDateEnriched;
            source = aggregateDataCard.source;
        }
    }


    @Override
    protected void clear() {
        super.clear();
        aggregateType = DCAggregateTypes.UNKNOWN;
        aggregateTypeOf = DCAggregateTypeOf.NONE;
        aggregateIdent = "";
        objectNo = "";
        objectNoVariant = "";
        dataCardAvailable = false;
        isApprovalDateEnriched = false;
        aggregateTypeFromModel = "";
        source = new FinId();
        isEldasAggregateInDIALOGVehicleCalculated = false;
    }

    @Override
    protected void clearFactoryAndEndNumber() {
        super.clearFactoryAndEndNumber();
        factorySign = "";
        endNumberString = "";
        endNumber = FinId.INVALID_SERIAL_NUMBER;
        endNumberLength = -1;
    }

    @Override
    protected boolean isValid() {
        if (super.isValid()) {
            return (aggregateType != DCAggregateTypes.UNKNOWN) && (aggregateTypeOf != DCAggregateTypeOf.UNKNOWN);
        }
        return false;
    }

    @Override
    public boolean hasAggregateModels() {
        return false;
    }

    @Override
    public boolean hasFilterAggregateModels() {
        return hasAggregateModels();
    }

    @Override
    public boolean isVehicleDataCard() {
        return false;
    }

    @Override
    public boolean isAggregateDataCard() {
        return true;
    }

    /**
     * Handelt es sich um eine echte eigenständige Aggregate-Datenkarte?
     * Bei {@code false} ist es eine Kind-Datenkarte einer Fahrzeug Datenkarte die keine eigenen Code oder SAAs hat
     * Bei {@code true} ist es entweder eine komplett eigenständige Aggregate Datenkarte, oder eine Kind-Datenkarte mit
     * eigenen Code oder SAAs
     *
     * @return
     */
    public boolean isRealAggDataCard() {
        if (source.isValidId()) {
            return isDataCardAvailable();
        }
        return true;
    }

    public boolean isEnriched() {
        return saas.isEnriched() || codes.isEnriched() || isApprovalDateEnriched;
    }

    public boolean isApprovalDateEnriched() {
        return isApprovalDateEnriched;
    }

    /**
     * Die gemeinsamen Werte werden über die Funktion in der abstrakten Superklasse erledigt
     * {@see AbstractDataCard#copyValues(AbstractDataCard)}
     *
     * @return
     */
    @Override
    public AbstractDataCard cloneMe() {
        AggregateDataCard result = new AggregateDataCard();
        result.aggregateType = aggregateType;
        result.aggregateTypeOf = aggregateTypeOf;
        result.aggregateIdent = aggregateIdent;
        result.objectNo = objectNo;
        result.objectNoVariant = objectNoVariant;
        result.dataCardAvailable = dataCardAvailable;
        result.isApprovalDateEnriched = isApprovalDateEnriched;
        result.setOldIdentSpecification(isOldIdentSpecification());
        result.source = source;
        super.copyValues(result);

        return result;
    }

    /*===== Getter and Setter =====*/
    public void setAggregateBasicType(DCAggregateTypes aggregateType, DCAggregateTypeOf aggregateTypeOf) {
        // hier werden aggregateType und aggregateTypeOf versorgt
        if (!Utils.objectEquals(this.aggregateType, aggregateType) || !Utils.objectEquals(this.aggregateTypeOf, aggregateTypeOf)) {
            clearFactoryAndEndNumber();
        }

        if (aggregateType == null) {
            aggregateType = DCAggregateTypes.UNKNOWN;
        }
        this.aggregateType = aggregateType;

        if (aggregateTypeOf == null) {
            aggregateTypeOf = DCAggregateTypeOf.NONE;
        }
        this.aggregateTypeOf = aggregateTypeOf;
    }

    public DCAggregateTypes getAggregateType() {
        return aggregateType;
    }

    public DCAggregateTypeOf getAggregateTypeOf() {
        return aggregateTypeOf;
    }

    public String getAggregateIdent() {
        return aggregateIdent;
    }

    public void setAggregateIdent(String aggregateIdent) {
        if (!Utils.objectEquals(this.aggregateIdent, aggregateIdent)) {
            clearFactoryAndEndNumber();
        }
        this.aggregateIdent = aggregateIdent;
    }

    /**
     * @param project
     * @return null möglich für bestimmte seltenere Aggregatearten
     */
    public AggregateIdent getAggIdent(EtkProject project) {
        if (!factoryAndEndNumberLoaded) {
            loadSerialNumberAndFactory(project);
        }
        return aggIdent;
    }

    public String getObjectNo() {
        return objectNo;
    }

    public void setObjectNo(String objectNo) {
        this.objectNo = objectNo;
    }

    public String getObjectNoVariant() {
        return objectNoVariant;
    }

    public void setObjectNoVariant(String objectNoVariant) {
        this.objectNoVariant = objectNoVariant;
    }

    public boolean isDataCardAvailable() {
        return dataCardAvailable;
    }

    public void setDataCardAvailable(boolean dataCardAvailable) {
        this.dataCardAvailable = dataCardAvailable;
        isEldasAggregateInDIALOGVehicleCalculated = false;
    }

    @Override
    public iPartsFactoryModel.SerialNoAndFactory getSerialNumberWithOverflowAndFactory(EtkProject project) {
        String factorySign = getFactorySign(project);
        int endNumber = getEndNumber(project);
        return getSerialNumberWithOverflowAndFactory("", getAggregateTypeFromModel(project), factorySign, endNumber, "", project);
    }

    /**
     * Liefert den Aggregatetyp vom Baumuster zurück.
     *
     * @param project
     * @return
     */
    public String getAggregateTypeFromModel(EtkProject project) {
        if (!StrUtils.isValid(aggregateTypeFromModel)) {
            if (StrUtils.isValid(getModelNo())) {
                iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(getModelNo()));
                aggregateTypeFromModel = model.getAggregateType();
            }
        }
        return aggregateTypeFromModel;
    }

    @Override
    public void setModelNo(String modelNo, EtkProject project) {
        aggregateTypeFromModel = "";
        super.setModelNo(modelNo, project);
        isEldasAggregateInDIALOGVehicleCalculated = false;
    }

    @Override
    public TwoGridValues getSaasForFilter() {
        TwoGridValues result = new TwoGridValues();
        result.addTwoGridValues(getSaas());
        // Für den Filter müssen neben den Aggregate-SAAs auch die Fahrzeug-SAAs berücksichtigt werden
        if (parentDatacard != null) {
            result.addTwoGridValues(parentDatacard.getSaas());
        }
        return result;
    }

    public boolean isOldIdentSpecification() {
        return isOldIdentSpecification;
    }

    public boolean isNewIdentSecification() {
        return !isOldIdentSpecification();
    }

    public void setOldIdentSpecification(boolean isOldIdentSpecification) {
        this.isOldIdentSpecification = isOldIdentSpecification;
    }

    /*===== Getter and Setter End =====*/

    @Override
    public boolean isModified(AbstractDataCard abstractDataCard, boolean includeSubDataCards) {
        if (super.isModified(abstractDataCard, includeSubDataCards)) {
            return true;
        }
        if (isAggregateDataCard() && abstractDataCard.isAggregateDataCard()) {
            AggregateDataCard aggregateDataCard = (AggregateDataCard)abstractDataCard;
            if (aggregateType != aggregateDataCard.aggregateType) {
                return true;
            }
            if (aggregateTypeOf != aggregateDataCard.aggregateTypeOf) {
                return true;
            }
            if (!Utils.objectEquals(aggregateIdent, aggregateDataCard.aggregateIdent)) {
                return true;
            }
            if (!Utils.objectEquals(objectNo, aggregateDataCard.objectNo)) {
                return true;
            }
            if (!Utils.objectEquals(objectNoVariant, aggregateDataCard.objectNoVariant)) {
                return true;
            }
            if (isOldIdentSpecification() != aggregateDataCard.isOldIdentSpecification()) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    /**
     * Befüllt die Parameter der Aggregate Datenkarte mit Werten aus dem übergebenen JSON Objekt. Bei nicht vorhandenen Daten
     * wird {@code null} gesetzt. DataCardAvailable wird nicht aus dem JSON übernommen, sondern aus dem Vorhandensein
     * von Code und SAAs ermittelt
     *
     * @param type                  AggregateTyp
     * @param json
     * @param project
     * @param sourceVehicleDatacard <code>true</code> wenn die Aggregatedatenkarte Teil einer Fahrzeugdatenkarte ist
     */
    public void loadFromJSONObject(DCAggregateTypes type, iPartsWSaggregateSubType json, EtkProject project, FinId sourceVehicleDatacard) {
        clear();
        dbLanguage = project.getDBLanguage();
        if ((json != null) && (type != null)) {
            isLoaded = true;
            source = sourceVehicleDatacard;
            setAggregateBasicType(type, DCAggregateTypeOf.getAggregateTypeOfByJson(json.getTypeOf()));
            setAggregateIdent(json.getId());
            setObjectNo(json.getObjectNumber());
            setObjectNoVariant(json.getObjectNumberVariant());
            setModelNo(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + json.getModelDesignation(), project);

            verifyIdentOldNew(project, true);

            iPartsWSactiveAssignedFpd fpdData = json.getActiveAssignedFpd();
            loadFromJSONActiveAssignedFpd(fpdData, project, sourceVehicleDatacard.isValidId());

            iPartsWSactiveProductDate productDate = json.getActiveProductDate();
            if (productDate != null) {
                setTechnicalApprovalDate(productDate.getTechnicalApprovalDate());
            }

            // Laut Confluence ist das Attribut nicht immer korrekt befüllt, daher soll der Inhalt ermittelt werden
            setDataCardAvailable(hasTechnicalCodes() || hasSaaNumbers());

            // Die Getriebeart soll über das Produkt ermittelt werden, weil die Datenkarte nur den allgemeinen Typ "G" liefert.
            if (getAggregateType() == DCAggregateTypes.TRANSMISSION) {
                gearboxValue = iPartsModel.getAggregateTypeFromFirstProduct(project, getModelNo());
            }
            // Korrektur der SAAs in Form einer speziellen Anreicherung unabhängig von den restlichen Anreicherungen
            iPartsProduct product = iPartsModel.getFirstProduct(project, getModelNo());
            correctDatacardSAAs(product);

            // Ereignis bestimmen, wenn die Aggregate-Datenkarte nicht über eine Fahrzeug-Datenkarte geladen wird (bei gültiger source)
            // -> Dort wird es innerhalb der Fahrzeug-Datenkarte nach der Anreicherung gemacht
            if (!sourceVehicleDatacard.isValidId()) {
                setEvent(getEventFromDatacardCodes(project));
            }
        }
    }

    @Override
    public void fillByModel(EtkProject project, String modelNoToLoad) {
        super.fillByModel(project, modelNoToLoad);
        if (iPartsModel.isAggregateModel(modelNoToLoad)) {
            iPartsModel currentModel = iPartsModel.getInstance(project, new iPartsModelId(modelNoToLoad));
            DCAggregateTypes type = DCAggregateTypes.getDCAggregateTypeByAggregateType(currentModel.getAggregateType());
            setAggregateBasicType(type, DCAggregateTypeOf.NONE);

            verifyIdentOldNew(project, true);

            // Die Getriebeart soll primär über das Produkt vom Baumuster ermittelt werden
            if (getAggregateType() == DCAggregateTypes.TRANSMISSION) {
                gearboxValue = iPartsModel.getAggregateTypeFromFirstProduct(project, getModelNo());
            }
        }
    }

    /**
     * DAIMLER-3438: Mit Aggregate-Datenkarte DIALOG/PKW (PLA) - F-CODE der Fahrgestell-Datenkarte übernehmen
     *
     * @param extraCode
     */
    protected void enrichFCode(String extraCode) {
        // "Fremde" F-Codes aus BM-bildenden Code entfernen
        for (TwoGridValues.ValueState codeValue : codes.getTopGridValues()) {
            if (codeValue.value.startsWith("F") && (codeValue.value.length() == 4) && StrUtils.isDigit(codeValue.value.substring(1))) {
                codeValue.checked = false; // anstelle von Löschen
                codeValue.addEnrichReason(EnrichReasons.FCODE);
            }
        }

        // "Fremde" F-Codes aus technischen Code entfernen
        for (TwoGridValues.ValueState codeValue : codes.getBottomGridValues()) {
            if (codeValue.value.startsWith("F") && (codeValue.value.length() == 4) && StrUtils.isDigit(codeValue.value.substring(1))) {
                codeValue.checked = false; // anstelle von Löschen
                codeValue.addEnrichReason(EnrichReasons.FCODE);
            }
        }

        // F-Code anreichern (als technischen Code falls er nicht bereits in den Code existiert hat)
        if (!codes.contains(extraCode)) {
            codes.addSingleValue(extraCode, EnrichReasons.FCODE, false);
        } else {
            TwoGridValues.ValueState valueState = codes.getValueState(extraCode, true);
            if (valueState == null) {
                valueState = codes.getValueState(extraCode, false);
            }
            if (valueState != null) {
                valueState.addEnrichReason(EnrichReasons.FCODE);
                valueState.checked = true;
            }
        }
    }

    /**
     * DAIMLER-8890: Codeanreicherung von Aggregate-Datenkarten über Mapping "Sachnummer zu Code"
     *
     * @param project
     */
    public void enrichAPartNoToCode(EtkProject project) {
        String aPartNo = getObjectNoAsAPartNo();
        if (StrUtils.isValid(aPartNo)) {
            Collection<String> codeForAggDataCard = iPartsAggCodeMappingCache.getInstance(project).getCodeForObjectNo(aPartNo, getAggregateType(), getFactorySign(project), getTechnicalApprovalDate());
            if ((codeForAggDataCard != null) && !codeForAggDataCard.isEmpty()) {
                for (String aggCode : codeForAggDataCard) {
                    if (!codes.contains(aggCode)) {
                        codes.addSingleValue(aggCode, EnrichReasons.APARTNO_TO_CODE, false);
                    }
                }
            }
        }
    }

    /**
     * DAIMLER-3419: Codeanreicherung von Aggregate-Datenkarten über "Mapping Code zu Code"
     *
     * @param newCodeList
     * @param top
     */
    protected void enrichCodeToCodeByList(Collection<String> newCodeList, boolean top) {
        if ((newCodeList != null) && !newCodeList.isEmpty()) {
            for (String newCode : newCodeList) {
                if (!codes.contains(newCode)) {
                    codes.addSingleValue(newCode, EnrichReasons.CODE_TO_CODE, top);
                }
            }
        }
    }

    /**
     * DAIMLER-3343, Filter Fallback bei fehlender Aggregatedatenkarte
     * Die Liste der SAAs (aus der Fahrzeugdatenkarte) übernehmen.
     *
     * @param newSAAsList
     */
    protected void enrichDataCardBySAAList(TwoGridValues newSAAsList) {
        if ((newSAAsList != null) && (!newSAAsList.isEmpty()) && (saas.isEmpty())) {
            for (TwoGridValues.ValueState newSAA : newSAAsList.getTopGridValues()) {
                if (!saas.contains(newSAA.value)) {
                    saas.addSingleValue(newSAA.value, EnrichReasons.VEHICLE_TO_AGGREGAT, true);
                }
            }
        }
    }

    /**
     * DAIMLER-3343, Filter Fallback bei fehlender Aggregatedatenkarte
     * Die Liste der Codes (aus der Fahrzeugdatenkarte) übernehmen.
     *
     * @param newCodeList
     * @param enrichEvenWithExistingCodes true, falls die übergebenen Code trotz existierenden Code dazugemischt werden sollen
     */
    protected void enrichDataCardByVehicleCodesList(TwoGridValues newCodeList, boolean enrichEvenWithExistingCodes) {
        if ((newCodeList != null) && !newCodeList.isEmpty() && (enrichEvenWithExistingCodes || codes.getBottomGridValues().isEmpty())) {
            List<TwoGridValues.ValueState> allCodeValueStates = new DwList<>(newCodeList.getTopGridValues());
            allCodeValueStates.addAll(newCodeList.getBottomGridValues());
            enrichDataCardByVehicleCodesList(allCodeValueStates);
        }
    }

    /**
     * Reichert die übergebenen Fahrzeugcode an, sofern sie nicht schon auf der Datenkarte vorhanden sind.
     *
     * @param allCodeValueStates
     */
    protected void enrichDataCardByVehicleCodesList(List<TwoGridValues.ValueState> allCodeValueStates) {
        // prüfen ob auf der Aggregate Datenkarte durch die Anreicherung "SNR zu Code" schon Modelljahrcode vorhanden sind
        boolean containsModelYearCode = false;
        for (TwoGridValues.ValueState gridValue : codes.getBottomGridValues()) {
            if (iPartsModelYearCode.isModelYearCodeValue(gridValue.value) &&
                gridValue.enrichReasons.contains(EnrichReasons.APARTNO_TO_CODE)) {
                containsModelYearCode = true;
                break;
            }
        }

        for (TwoGridValues.ValueState newCode : allCodeValueStates) {
            if (!codes.contains(newCode.value)) {
                // wenn der aktuelle Code ein Modelljahrcode ist, und es bereits einen durch "SNR zu Code" angereicherten
                // Modelljahrcode auf der Datenkarte gibt, darf der neue Modelljahrcode nicht zusätzlich angereichert werden.
                if (!containsModelYearCode || !iPartsModelYearCode.isModelYearCodeValue(newCode.value)) {
                    TwoGridValues.ValueState codeToAdd = newCode.cloneMe();
                    codeToAdd.addEnrichReason(EnrichReasons.VEHICLE_TO_AGGREGAT);
                    codes.addSingleValue(codeToAdd, false);
                }
            }
        }
    }

    /**
     * DAIMLER-3343, Filter Fallback bei fehlender Aggregatedatenkarte
     * Das Datum (aus der Fahrzeugdatenkarte) übernehmen.
     *
     * @param newDate
     */
    protected void enrichDataCardByDate(String newDate) {
        if ((newDate != null) && (!newDate.isEmpty()) && (technicalApprovalDate.isEmpty())) {
            technicalApprovalDate = newDate;
            isApprovalDateEnriched = true;
        }
    }


    /**
     * Werkskennzeichen, Werksnummer und Endnummer (ohne Millionenüberlauf bestimmen)
     *
     * @param project
     */
    private void loadSerialNumberAndFactory(EtkProject project) {
        if (aggregateIdent.isEmpty() || (aggregateType == DCAggregateTypes.UNKNOWN)) {
            factoryAndEndNumberLoaded = true;
            return;
        }
        switch (aggregateType) {
            case ENGINE:
                EngineIdent engineIdent = new EngineIdent(project, aggregateIdent);
                if (isOldIdentSpecification()) {
                    engineIdent.setIdentSpecificationOld();
                } else {
                    engineIdent.setIdentSpecificationNew();
                }
                factorySign = engineIdent.extractFactorySign(); // ist bei alter Ident-Systematik leer
                endNumberString = engineIdent.extractSerialNumberString(); // ist bei alter Ident-Systematik nur 6-stellig
                endNumber = engineIdent.getSerialNumber(); // ist bei alter Ident-Systematik nur 6-stellig
                endNumberLength = engineIdent.getSerialNumberLength();
                aggIdent = engineIdent;
                break;
            case TRANSMISSION:
            case TRANSFER_CASE:
                TransmissionIdent transmissionIdent = new TransmissionIdent(project, aggregateIdent);
                factorySign = transmissionIdent.extractFactorySign();
                endNumberString = transmissionIdent.extractSerialNumberString();
                endNumber = transmissionIdent.getSerialNumber();
                endNumberLength = transmissionIdent.getSerialNumberLength();
                aggIdent = transmissionIdent;
                break;
            case AXLE:
                AxleIdent axleIdent = new AxleIdent(project, aggregateIdent);
                axleIdent.setIdentSpecification(DatacardIdentOrderTypes.AXLE_FRONT_NEW.getSpecification());
                if (!axleIdent.isValidId()) {
                    axleIdent.setIdentSpecification(DatacardIdentOrderTypes.AXLE_FRONT_OLD.getSpecification());
                }
                factorySign = axleIdent.extractFactorySign();
                endNumberString = axleIdent.extractSerialNumberString();
                endNumber = axleIdent.getSerialNumber();
                endNumberLength = axleIdent.getSerialNumberLength();
                aggIdent = axleIdent;
                break;
            case CAB:
                CabIdent cabIdent = new CabIdent(project, aggregateIdent);
                factorySign = cabIdent.extractFactorySign();
                endNumberString = cabIdent.extractSerialNumberString();
                endNumber = cabIdent.getSerialNumber();
                endNumberLength = cabIdent.getSerialNumberLength();
                aggIdent = cabIdent;
                break;
            case AFTER_TREATMENT_SYSTEM:
                ATSIdent atsIdent = new ATSIdent(project, aggregateIdent);
                factorySign = atsIdent.extractFactorySign();
                endNumberString = atsIdent.extractSerialNumberString();
                endNumber = atsIdent.getSerialNumber();
                endNumberLength = atsIdent.getSerialNumberLength();
                aggIdent = atsIdent;
                break;
            case STEERING:
                SteeringIdent steeringIdent = new SteeringIdent(project, aggregateIdent);
                factorySign = steeringIdent.extractFactorySign();
                endNumberString = steeringIdent.extractSerialNumberString();
                endNumber = steeringIdent.getSerialNumber();
                endNumberLength = steeringIdent.getSerialNumberLength();
                aggIdent = steeringIdent;
                break;
            case EXHAUST_SYSTEM:
                break;
            case PLATFORM:
                PlatformIdent platformIdent = new PlatformIdent(project, aggregateIdent);
                factorySign = platformIdent.extractFactorySign();
                endNumberString = platformIdent.extractSerialNumberString();
                endNumber = platformIdent.getSerialNumber();
                endNumberLength = platformIdent.getSerialNumberLength();
                aggIdent = platformIdent;
                break;
            case HIGH_VOLTAGE_BATTERY:
                HighVoltageBatIdent highVoltageBatIdent = new HighVoltageBatIdent(project, aggregateIdent);
                factorySign = highVoltageBatIdent.extractFactorySign();
                endNumberString = highVoltageBatIdent.extractSerialNumberString();
                endNumber = highVoltageBatIdent.getSerialNumber();
                endNumberLength = highVoltageBatIdent.getSerialNumberLength();
                aggIdent = highVoltageBatIdent;
                break;
            case ELECTRO_ENGINE:
                ElectroEngineIdent electroEngineIdent = new ElectroEngineIdent(project, aggregateIdent);
                factorySign = electroEngineIdent.extractFactorySign();
                endNumberString = electroEngineIdent.extractSerialNumberString();
                endNumber = electroEngineIdent.getSerialNumber();
                endNumberLength = electroEngineIdent.getSerialNumberLength();
                aggIdent = electroEngineIdent;
                break;
            case FUEL_CELL:
                FuelCellIdent fuelCellIdent = new FuelCellIdent(project, aggregateIdent);
                factorySign = fuelCellIdent.getFactorySign();
                endNumberString = fuelCellIdent.extractSerialNumberString();
                endNumber = fuelCellIdent.getSerialNumber();
                endNumberLength = fuelCellIdent.getSerialNumberLength();
                aggIdent = fuelCellIdent;
                break;
        }

        // DAIMLER-15308: Bestimmung des Werks bei zugekauften Elektromotoren optional über das Fahrzeugprodukt bestimmen
        useVehicleFactoryNumber = false;
        VehicleDataCard vehicleDataCard = getParentDatacard();
        if ((vehicleDataCard != null) && vehicleDataCard.isFinOrVinValid()) {
            List<iPartsProduct> productListForDataCard = iPartsFilter.get().getProductListForDataCard(project, this, true);
            if (Utils.isValid(productListForDataCard)) {
                for (iPartsProduct product : productListForDataCard) {
                    if (product.isUseVehicleFactoryNumber()) {
                        useVehicleFactoryNumber = true;
                        break;
                    }
                }
                if (useVehicleFactoryNumber) { // Werksnummer von der Fahrzeugdatenkarte verwenden
                    factoryNumber = vehicleDataCard.getFactoryNumber(project);
                }
            }
        }

        if (StrUtils.isEmpty(factoryNumber)) { // Werksnummer nur dann bestimmen falls sie nicht bereits gesetzt ist
            iPartsFactoryModel factoryModelInstance = iPartsFactoryModel.getInstance(project);
            factoryNumber = factoryModelInstance.getFactoryNumberForAggregate(getAggregateTypeFromModel(project), factorySign,
                                                                              new iPartsModelId(getModelNo()));
        }
        factoryAndEndNumberLoaded = true;
    }

    @Override
    public String getFactorySign(EtkProject project) {
        if (!factoryAndEndNumberLoaded) {
            loadSerialNumberAndFactory(project);
        }
        return factorySign;
    }

    @Override
    public String getFactoryNumber(EtkProject project) {
        if (!factoryAndEndNumberLoaded) {
            loadSerialNumberAndFactory(project);
        }
        return factoryNumber;
    }

    public boolean isUseVehicleFactoryNumber() {
        return useVehicleFactoryNumber;
    }

    public String getEndNumberString(EtkProject project) {
        if (!factoryAndEndNumberLoaded) {
            loadSerialNumberAndFactory(project);
        }
        return endNumberString;
    }

    @Override
    public int getEndNumber(EtkProject project) {
        if (!factoryAndEndNumberLoaded) {
            loadSerialNumberAndFactory(project);
        }
        return endNumber;
    }

    @Override
    public int getEndNumberLength(EtkProject project) {
        if (!factoryAndEndNumberLoaded) {
            loadSerialNumberAndFactory(project);
        }
        return endNumberLength;
    }

    @Override
    public String getSpikeIdent(EtkProject project) {
        String factorySign = getFactorySign(project);
        String endNumberString = getEndNumberString(project);
        if (StrUtils.isValid(endNumberString)) {
            if (StrUtils.isValid(factorySign)) {
                return factorySign + endNumberString;
            } else if ((getAggregateType() == DCAggregateTypes.ENGINE) && isOldIdentSpecification()) {
                // Bei alter Ident-Systematik nur den Endnummern-String zurückliefern ohne Werkskennbuchstaben
                return endNumberString;
            }
        }

        return "";
    }

    /**
     * Ermittelt für die mehrstufige Filterung alle Unterdatenkarten zu gegebenen Baumustern
     * Für eine Aggregatedatenkarte gibt es keine Unterdatenkarten, daher wird nur überprüft ob das Baumuster auf
     * der Datenkarte in der Liste der gesuchten Baumuster vorkommt. Bei Übereinstimmung wird die Datenkarte als
     * einziges Element zur Ergebnisliste hinzugefügt.
     *
     * @param project in diesem Fall nicht benötigt
     * @param models  Liste der gesuchten Baumusternummern
     * @return leere Liste wenn es keine Übereinstimmung gibt
     */
    @Override
    public List<AbstractDataCard> getRelevantDatacardsForModels(EtkProject project, Collection<String> models) {
        List<AbstractDataCard> result = new DwList<>();
        if (StrUtils.isValid(getModelNo())) {
            for (String model : models) {
                if (model.equals(getModelNo())) {
                    result.add(this);
                    return result;
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
     * Ermittelt für die mehrstufige Filterung alle Unterdatenkarten zu gegebenen Baumustern
     * Für eine Aggregatedatenkarte gibt es keine Unterdatenkarten, daher wird nur diese Datenkarten zurückgeliefert
     *
     * @param project in diesem Fall nicht benötigt
     * @return
     */
    @Override
    public List<AbstractDataCard> getAllSelectedSubDatacards(EtkProject project) {
        List<AbstractDataCard> result = new DwList<>();
        result.add(this);
        return result;
    }

    @Override
    public List<AbstractDataCard> getRelevantDatacardsForAssembly(iPartsDataAssembly assembly) {
        List<AbstractDataCard> result = new DwList<>();
        result.add(this);
        return result;
    }

    @Override
    public Collection<iPartsProduct> getSubDatacardsProducts(EtkProject project) {
        return null;
    }

    /**
     * Überprüft ob die Ident-Systematik (Alt/Neu) der Datenkarte mit der am Produkt hinterlegten übereinstimmt.
     * Falls nicht wird bei {@code silent = false} ein Dialog mit der Abfrage ob angepasst werden soll angezeigt
     * Bei {@code silent = true} wird die Anpassung automatisch gemacht (z.B. für Webservices)
     * Werden mehrere Produkte zum Baumuster gefunden, gilt der Ident als neu falls bei einem der Produkte neu angegeben war.
     *
     * @param project
     * @param silent
     * @return {@code true} wenn die Datenkarte angepasst wurde
     */
    public boolean verifyIdentOldNew(EtkProject project, boolean silent) {
        if (aggregateType != DCAggregateTypes.ENGINE) { // Prüfung erfolgt nur bei Motor Datenkarten
            return false;
        }
        // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster ebenfalls berücksichtigen.
        List<iPartsProduct> productsForModel = iPartsProductHelper.getProductsForModel(project, new iPartsModelId(getModelNo()),
                                                                                       null, null, false);
        if (productsForModel.isEmpty()) {
            return false;
        }
        boolean isOneProductNew = false;
        for (iPartsProduct product : productsForModel) {
            if (!product.isIdentOld()) {
                isOneProductNew = true;
                break;
            }
        }

        String productSystem;
        if (isOneProductNew) {
            productSystem = "!!neue Ident-Systematik";
        } else {
            productSystem = "!!alte Ident-Systematik";
        }

        String message = TranslationHandler.translate("!!Ident-Systematik von Produkt und Datenkarte stimmen nicht überein." +
                                                      "%1Soll die Datenkarte an die Produkt-Systematik angepasst werden " +
                                                      "(%2)?", "\n", TranslationHandler.translate(productSystem));

        String title = aggregateType.getDescription() + ": " + aggregateIdent;

        if (isOneProductNew && isOldIdentSpecification()) {
            // Mindestens eines der gefundenen Produkte hat neue Idents aber in der Datenkarte ist alt angegeben
            if (silent) {
                setOldIdentSpecification(false);
                return true;
            } else {
                ModalResult result = MessageDialog.showYesNo(message, title);
                if (result == ModalResult.YES) {
                    setOldIdentSpecification(false);
                    return true;
                }
            }
        } else if (!isOneProductNew && !isOldIdentSpecification()) {
            // Keines der gefundenen Produkte hat neue Idents aber in der Datenkarte ist neu angegeben
            if (silent) {
                setOldIdentSpecification(true);
                return true;
            } else {
                ModalResult result = MessageDialog.showYesNo(message, title);
                if (result == ModalResult.YES) {
                    setOldIdentSpecification(true);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Liefert zurück, ob diese Datenkarte gültig ist für den Endnummern-Filter.
     *
     * @param products ein Produkt o. null für Aufruf aus Endnummmernfilter; ggf. mehrere Produkte für Aufruf aus Filterdialog (ohne Bedeutung für Aggregate)
     * @param project
     * @return
     */
    @Override
    public boolean isValidForEndNumberFilter(List<iPartsProduct> products, EtkProject project) {
        // Für Austauschaggregate soll der Endnummernfilter nicht wirken.
        AggregateIdent aggregateIdent = getAggIdent(project);
        if ((aggregateIdent != null) && aggregateIdent.isExchangeAggregate()) {
            return false;
        }

        // Werkskennbuchstabe und Werksnummer sind bei Motoren mit alter Ident-Systematik nicht vorhanden
        if ((getAggregateType() == DCAggregateTypes.ENGINE) && isOldIdentSpecification()) {
            return (project != null) && ((getEndNumber(project) != FinId.INVALID_SERIAL_NUMBER) || isTechnicalApprovalDateValid());
        }

        return super.isValidForEndNumberFilter(products, project);
    }

    public VehicleDataCard getParentDatacard() {
        return parentDatacard;
    }

    public void setParentDatacard(VehicleDataCard parentDatacard) {
        this.parentDatacard = parentDatacard;
        isEldasAggregateInDIALOGVehicleCalculated = false;
    }

    public boolean isEldasAggregateInDIALOGVehicle(EtkProject project) {
        if (!isEldasAggregateInDIALOGVehicleCalculated) {
            boolean result = false;
            if (!isRealAggDataCard()) { // Keine eigene Aggregate-Datenkarte vorhanden?
                String modelNo = getModelNo();
                if (StrUtils.isValid(modelNo)) {
                    // Aggregat mit Dokumentationsmethode ELDAS (für Edit auch Produkte berücksichtigen, in denen das Baumuster
                    // im AS nicht sichtbar ist -> isEldasAggregate ist true, wenn eines der Produkte Dokumentationsmethode
                    // ELDAS hat)?
                    // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster ebenfalls berücksichtigen.
                    List<iPartsProduct> productsForAggregate = iPartsProductHelper.getProductsForModel(project, new iPartsModelId(modelNo),
                                                                                                       null, null, false);
                    boolean isEldasAggregate = false;
                    for (iPartsProduct product : productsForAggregate) {
                        if (product.getDocumentationType().isELDASDocumentationType()) { // PSK-Produkte hier nicht berücksichtigen
                            isEldasAggregate = true;
                            break;
                        }
                    }

                    if (isEldasAggregate) {
                        VehicleDataCard vehicleDataCard = getParentDatacard();
                        if (vehicleDataCard != null) {
                            // Fahrzeug-Datenkarte mit gültiger FIN/VIN und Datenkarte geladen?
                            if (vehicleDataCard.isFinOrVinValid() && vehicleDataCard.isDataCardLoaded()) {
                                // Fahrzeug mit Dokumentationsmethode DIALOG (inkl. Auto-Product-Select)?
                                // Alle Produkte zum Baumuster bestimmen. Nicht sichtbare Baumuster sollen beim Edit aber
                                // nicht bei den Webservices berücksichtigt werden.
                                List<iPartsProduct> productsForVehicle = iPartsProductHelper.getProductsForModelAndSessionType(project, new iPartsModelId(vehicleDataCard.getModelNo()),
                                                                                                                               null, null);
                                productsForVehicle = iPartsFilterHelper.getAutoSelectProductsForFIN(project, productsForVehicle,
                                                                                                    vehicleDataCard.getFinId(),
                                                                                                    new iPartsModelId(vehicleDataCard.getModelNo()),
                                                                                                    vehicleDataCard.getCodes().getAllCheckedValues());
                                for (iPartsProduct product : productsForVehicle) {
                                    if (product.getDocumentationType().isDIALOGDocumentationType()) { // PSK-Produkte hier nicht berücksichtigen
                                        result = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            isEldasAggregateInDIALOGVehicle = result;
            isEldasAggregateInDIALOGVehicleCalculated = true;
        }

        return isEldasAggregateInDIALOGVehicle;
    }

    public String getObjectNoAsAPartNo() {
        if (StrUtils.isEmpty(getObjectNo())) {
            return null;
        }
        String objectNo = getObjectNo().toUpperCase();
        if (!objectNo.startsWith("A")) {
            objectNo = "A" + objectNo;
        }
        return objectNo;
    }
}