/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects.iPartsWSactiveAssignedFpd;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EnrichReasons;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.TwoGridValues;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Abstrakte Datenkarte als Grundlage
 * Es gibt grundsätzlich eine Trennung zwischen den Originalwerten der Datenkarte und den vom Benutzer ggf. veränderten Werten
 * im Filterdialog. Die vom Benutzer angepassten Werte werden in der inneren Klasse ExtraFilterValues gehalten.
 * Für die Filterung (iPartsFilter) sind immer die getFilterXXX() Getter zu verwenden. In vielen Fällen verweisen
 * diese einfach auf die getXXX(), in anderen Fällen werden diese mit den manuellen Werten überschrieben.
 * Potenziell können dann aber alle Datenkarteninhalte mal f.d. Filterung manipuliert werden.
 * <p>
 * Es gilt folgende Konvention für die Methoden:
 * - get|has|isXXX() liefert den Originalwert der Datenkarte
 * - get|has|isSelectedXXX() liefert den vom Benutzer eingegeben oder manipulierten Wert
 * - get|has|isFilterXXX liefert den effektiven Wert f.d. Filterung
 */
public abstract class AbstractDataCard implements RESTfulTransferObjectInterface {

    /**
     * Kennzeichen für Lebenszyklusstatus eines Objektes
     * Die Enum-Wert stammen aus einer Daimler-Antwort in DAIMLER-4626
     */
    public enum LifeCycleStatus {
        noFinalAcceptYet, inFinalAccept, delivered, deleted, scrapped, stolen, retrofitted, unknown;

        public static LifeCycleStatus fromString(String statusStr) {
            try {
                return LifeCycleStatus.valueOf(statusStr);
            } catch (Exception e) {
                return unknown;
            }
        }
    }

    @JsonProperty
    protected boolean isVirtual; // true wenn die Datenkarte potenziell vom Original abweicht oder wenn sie völlig manuell erzeugt wurde.
    // die im Filter gespeicherte Datenkarte ist virtuell; vom Datenkarten WS geladene Datenkarten sind nicht virtuell
    @JsonProperty
    protected boolean isLoaded;  // true wenn aus Webservice geladen; isLoaded und isModelLoaded schließen sich wechselseitig aus
    @JsonProperty
    protected boolean isModelLoaded; // true wenn aus Baumuster geladen; isLoaded und isModelLoaded schließen sich wechselseitig aus
    @JsonProperty
    protected String modelNo;  // inkl. Sachnummernkennbuchstabe (C, D)
    @JsonProperty
    protected String productGroup = "";
    // original Code + SAA/BK aus Datenkarte
    @JsonProperty
    protected TwoGridValues codes;
    @JsonProperty
    protected iPartsEvent event;
    @JsonProperty
    protected TwoGridValues saas;
    @JsonProperty
    private Set<String> dataCardSaNumbers; // gültige freie SA zu einem BM basierend auf saaNumbers
    @JsonProperty
    private Set<String> pskVariants;
    @JsonProperty
    protected String technicalApprovalDate;
    @JsonProperty
    protected String steeringValue = "";
    @JsonProperty
    protected String gearboxValue = "";
    @JsonProperty
    protected String dbLanguage;
    @JsonProperty
    protected iPartsFactoryModel.SerialNoAndFactory serialNumberWithOverflowAndFactory = new iPartsFactoryModel.SerialNoAndFactory();
    @JsonProperty
    protected boolean factoryAndEndNumberLoaded = false;
    @JsonProperty
    protected String factoryNumber = "";
    @JsonProperty
    protected LifeCycleStatus lifeCycleStatus = LifeCycleStatus.unknown;

    public AbstractDataCard() {
        clear();
    }

    public AbstractDataCard(boolean isVirtual) {
        this();
        this.isVirtual = isVirtual;
    }

    /**
     * Alle Daten löschen, die beim Löschen der Caches neu berechnet werden müssen.
     */
    public void clearCache() {
        clearSerialNumberWithOverflow();
    }

    public abstract boolean isVehicleDataCard();

    public abstract boolean isAggregateDataCard();

    public boolean isVirtualDataCard() {
        return isVirtual;
    }

    public boolean isDataCardLoaded() {
        return isLoaded;
    }

    public void setDataCardFlagsByDataCard(AbstractDataCard dataCard) {
        if (dataCard != null) {
            isLoaded = dataCard.isLoaded;
            isModelLoaded = dataCard.isModelLoaded;
        } else {
            isModelLoaded = iPartsModelId.isModelNumberValid(modelNo, true);
        }
    }

    public boolean isModelLoaded() {
        return isModelLoaded;
    }

    public abstract boolean isEnriched();

    public abstract AbstractDataCard cloneMe();

    public AbstractDataCard cloneMeAsVirtual() {
        AbstractDataCard result = cloneMe();
        result.isVirtual = true;
        return result;
    }

    public void reset() {
        clear();
    }

    protected void clear() {
        isLoaded = false;
        isModelLoaded = false;
        setModelNo("", null);
        productGroup = "";
        codes = new TwoGridValues();
        event = null;
        saas = new TwoGridValues();
        dataCardSaNumbers = null; // gültige freie SA zu einem BM basierend auf saaNumbers
        pskVariants = new HashSet<>();
        technicalApprovalDate = "";
        dbLanguage = "";
        steeringValue = "";
        gearboxValue = "";
        clearFactoryAndEndNumber();
    }

    protected boolean isValid() {
        return !StrUtils.isEmpty(modelNo) && !StrUtils.isEmpty(technicalApprovalDate);
    }

    protected void copyValues(AbstractDataCard dataCard) {
        dataCard.modelNo = modelNo;
        dataCard.productGroup = productGroup;
        dataCard.codes = new TwoGridValues(codes.getTopGridValues(), codes.getBottomGridValues());
        dataCard.event = event;
        dataCard.saas = new TwoGridValues(saas.getTopGridValues(), saas.getBottomGridValues());
        dataCard.dataCardSaNumbers = null;
        dataCard.pskVariants = new HashSet<>(pskVariants);
        dataCard.gearboxValue = gearboxValue;
        dataCard.steeringValue = steeringValue;
        dataCard.technicalApprovalDate = technicalApprovalDate;
        dataCard.dbLanguage = dbLanguage;
        dataCard.isVirtual = isVirtual;
        dataCard.isLoaded = isLoaded;
        dataCard.isModelLoaded = isModelLoaded;
        dataCard.lifeCycleStatus = lifeCycleStatus;
    }

    /*===== Getter and Setter =====*/
    public String getModelNo() {
        return modelNo;
    }

    public String getFilterModelNo() {
        return getModelNo();
    }

    /**
     * Liefert alle für den Filter relevanten Baumusternummern, also das Baumuster von dieser Datenkarte und die
     * Baumuster von evtl. vorhandenen Kind-Datenkarten.
     *
     * @param project
     * @return
     */
    public Set<String> getFilterModelNumbers(EtkProject project) {
        Set<String> modelNumbers = new TreeSet<>();
        modelNumbers.add(getModelNo());
        return modelNumbers;
    }

    public void setModelNo(String modelNo, EtkProject project) {
        this.modelNo = modelNo;
        if (StrUtils.isValid(modelNo)) {
            // Setze die Produktgruppe des Baumusters
            if (project != null) {
                iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNo));
                setProductGroup(model.getProductGroup());
            }
        } else {
            setProductGroup("");
        }
    }

    /**
     * Zugriff nur beim Abgleich im Filter-Dialog nötig
     *
     * @return
     */
    public TwoGridValues getCodes() {
        return codes;
    }

    public TwoGridValues getFilterCodes() {
        return getCodes();
    }

    /**
     * Zugriff beim Besetzen der Datenkarte
     *
     * @param codes
     */
    public void setCodes(TwoGridValues codes) {
        if (codes == null) {
            codes = new TwoGridValues();
        }
        this.codes = codes.cloneMe();
    }

    /**
     * Gibt die Liste der Codes zurück oder null.
     * Darf keine leere Liste zurückgeben, sonst wird beim WebService [Ident] ein leeres Attribut ausgegeben.
     *
     * @return
     */
    public List<String> getCodeNumbers() {
        List<String> result = null;
        for (String codeNo : getCodes().getAllCheckedValues()) {
            if (result == null) {
                result = new ArrayList<>();
            }
            result.add(codeNo);
        }
        return result;
    }

    public iPartsEvent getEvent() {
        if ((event != null) && event.getEventId().isEmpty()) { // Ein Ereignis mit leerer Ereignis-ID ist ungültig
            return null;
        }
        return event;
    }

    public void setEvent(iPartsEvent event) {
        this.event = event;
    }

    /**
     * Zugriff nur beim Abgleich im Filter-Dialog nötig
     *
     * @return
     */
    public TwoGridValues getSaas() {
        return saas;
    }

    /**
     * Liefert alle SAAs die für den Filter relevant sind
     * - bei Fahrzeugdatenkarten: SAAs der FD und SAAs aller Aggregate-Datenkarten
     * - bei Aggregatedatenkarten: SAAs der AD und alle SAAs der Fahrzeugdatenkarte
     *
     * @return
     */
    public abstract TwoGridValues getSaasForFilter();

    /**
     * Zugriff beim Besetzen der Datenkarte
     *
     * @param saas
     */
    public void setSaas(TwoGridValues saas) {
        if (saas != null) {
            this.saas = saas.cloneMe();
        } else {
            this.saas = new TwoGridValues();
        }
        this.dataCardSaNumbers = null;
    }

    public Set<String> getDataCardSaNumbers() {
        if (dataCardSaNumbers == null) {
            dataCardSaNumbers = retrieveSasFromSaas(saas);
        }
        return Collections.unmodifiableSet(dataCardSaNumbers);
    }

    /**
     * Gibt die Liste der SAAs zurück oder null.
     * Darf keine leere Liste zurückgeben, sonst wird beim WebService [Ident] ein leeres Attribut ausgegeben.
     *
     * @return
     */
    public List<String> getDataCardSaaNumbers() {
        List<String> result = null;
        for (String saaNo : getSaas().getAllCheckedValues()) {
            if (result == null) {
                result = new ArrayList<>();
            }
            result.add(saaNo);
        }
        return result;
    }

    public Set<String> getPskVariants() {
        return pskVariants;
    }

    public void setPskVariants(Set<String> pskVariants) {
        if (pskVariants != null) {
            this.pskVariants = new HashSet<>(pskVariants);
        } else {
            this.pskVariants = new HashSet<>();
        }
    }

    /**
     * Gibt das [technicalApprovalDate] zurück. Auf Wunsch passend formatiert.
     *
     * @param formatted
     * @return
     */
    public String getTechnicalApprovalDate(boolean formatted) {
        if (formatted) {
            return formatDateForIdent(technicalApprovalDate);
        } else {
            return technicalApprovalDate;
        }
    }

    public String getTechnicalApprovalDate() {
        return technicalApprovalDate;
    }

    public void setTechnicalApprovalDate(String technicalApprovalDate) {
        if (technicalApprovalDate != null) {
            this.technicalApprovalDate = technicalApprovalDate;
        } else {
            this.technicalApprovalDate = "";
        }
    }

    public boolean isTechnicalApprovalDateValid() {
        return StrUtils.isValid(getTechnicalApprovalDate());
    }

    public String getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }

    public boolean isProductGroupValid() {
        return StrUtils.isValid(productGroup);
    }

    public String getSteeringValue() {
        return steeringValue;
    }

    public void setSteeringValue(String steeringValue) {
        this.steeringValue = steeringValue;
    }

    public boolean isSteeringValueValid() {
        return StrUtils.isValid(steeringValue);
    }

    public String getGearboxValue() {
        return gearboxValue;
    }

    public void setGearboxValue(String gearboxValue) {
        this.gearboxValue = gearboxValue;
    }

    public boolean isGearboxValueValid() {
        return StrUtils.isValid(gearboxValue);
    }

    public LifeCycleStatus getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public void setLifeCycleStatus(LifeCycleStatus lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    /**
     * Werksnummer ermitteln
     *
     * @param project
     * @return Leerstring wenn Werk nicht ermittelt werden kann
     */
    public abstract String getFactoryNumber(EtkProject project);

    public void setFactoryNumber(String factoryNumber) {
        this.factoryNumber = factoryNumber;
    }

    public boolean isFactoryNumberValid(EtkProject project) {
        return StrUtils.isValid(getFactoryNumber(project));
    }

    /**
     * Liefert die Seriennummer der Datenkarte mit Millionenüberlauf und das Werk zurück (aus WMI-Abfrage).
     * Wird für Endnummernfilter benötigt.
     *
     * @param project
     * @return
     */
    public abstract iPartsFactoryModel.SerialNoAndFactory getSerialNumberWithOverflowAndFactory(EtkProject project);

    /**
     * Liefert die Seriennummer aus einem Ident mit Millionenüberlauf und das Werk zurück.
     *
     * @param wmi             Leerstring für Aggregat
     * @param aggregateDbType
     * @param factorySign
     * @param endNumber
     * @param project
     * @return
     */
    protected iPartsFactoryModel.SerialNoAndFactory getSerialNumberWithOverflowAndFactory(String wmi, String aggregateDbType,
                                                                                          String factorySign, int endNumber,
                                                                                          String steeringNumberFromFIN, EtkProject project) {
        // Seriennummer mit Millionenüberlauf lazy berechnen
        if (!serialNumberWithOverflowAndFactory.isValid() && StrUtils.isValid(modelNo)) {
            iPartsModelId modelId = new iPartsModelId(modelNo);

            // Millionenüberlauf für die Endnummer aus der Datenkarte berechnen und auf Gültigkeit prüfen
            iPartsFactoryModel factoryModelInstance = iPartsFactoryModel.getInstance(project);
            serialNumberWithOverflowAndFactory = factoryModelInstance.getSerialNumberWithOverflowAndFactoryForWMIOrAggregate(
                    wmi, factorySign, modelId, aggregateDbType, endNumber, getEndNumberLength(project), steeringNumberFromFIN);
        }
        return serialNumberWithOverflowAndFactory;
    }

    /**
     * Setzt die Seriennummer mit Millionenüberlauf zurück, damit sie neu berechnet wird.
     */
    public void clearSerialNumberWithOverflow() {
        serialNumberWithOverflowAndFactory = new iPartsFactoryModel.SerialNoAndFactory();
    }

    /**
     * Setzt alle Daten wie Werksnummer und Endnummer zurück, die mit der FIN/Ident und dem Millionenüberlauf in Verbindung
     * stehen.
     */
    protected void clearFactoryAndEndNumber() {
        factoryAndEndNumberLoaded = false;
        factoryNumber = "";
        clearSerialNumberWithOverflow();
    }

    /*===== Getter and Setter End =====*/


    /**
     * Liefert das Ereignis zur Datenkarte bzw. zur Codeliste der Datenkarte zurück.
     * Hierfür werden die gültigen Ereignisse (KEM-BIS-Datum = unendlich) und deren Coderegeln ermittelt. Danach werden
     * aufsteigend in der Reihenfolge die Coderegeln zu den Ereignissen geprüft und das erste gefundene Ereignis, dessen
     * Coderegel {@code true} ergibt, zurückgegeben.
     *
     * @param project
     * @return
     */
    protected iPartsEvent getEventFromDatacardCodes(EtkProject project) {
        if (StrUtils.isEmpty(getModelNo())) { // Das Baumuster ist notwendig für die Bestimmung der Baureihe
            return null;
        }

        iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(getModelNo()));
        if (!model.getSeriesId().isValidId()) { // Baureihe vorhanden?
            return null;
        }

        iPartsDialogSeries series = iPartsDialogSeries.getInstance(project, model.getSeriesId());
        if (!series.isEventTriggered()) {
            return null;
        }
        Map<String, iPartsEvent> events = series.getEventsMap();
        Set<String> allCheckedCodesFromDataCard = getCodes().getAllCheckedValues();
        for (iPartsEvent event : events.values()) {
            // Coderegel des Events gegen die Code auf der Datenkarte prüfen
            if (iPartsFilterHelper.basicCheckCodeFilterForDatacard(event.getCode(), allCheckedCodesFromDataCard)) {
                return event;
            }
        }
        return null;
    }

    public boolean hasTechnicalCodes() {
        // Baumusterbildende Codes (im TopGrid) zählen nicht -> nur ausgewählte Codes vom BottomGrid berücksichtigen
        return ((codes != null) && !codes.getCheckedValues(false).isEmpty());
    }

    public boolean hasFilterTechnicalCodes() {
        return hasTechnicalCodes();
    }

    public boolean hasSaaNumbers() {
        return ((saas != null) && !saas.getAllCheckedValues().isEmpty());
    }

    public boolean hasPSKVariants() {
        return (pskVariants != null) && !pskVariants.isEmpty();
    }

    public abstract boolean hasAggregateModels();

    public abstract boolean hasFilterAggregateModels();

    private Set<String> retrieveSasFromSaas(TwoGridValues saas) {
        // Aus den SAAs gültige freie SAs für das Baumuster bestimmen
        Set<String> saNumbers = new HashSet<String>();
        if ((saas != null) && !saas.isEmpty()) {
            for (String saaNumber : saas.getAllCheckedValues()) {
                saNumbers.add(StrUtils.copySubString(saaNumber, 0, saaNumber.length() - 2)); // letzten beiden Stellen sicher abschneiden
            }
        }
        return saNumbers;
    }

    /**
     * Diese Teilenummern sind für den Federfilter relevant
     * In der VehicledataCard werden die Teile des Federbeins zurückgeliefert
     *
     * @return
     */
    public Collection<String> getSpringPartNumbers(EtkProject project) {
        return new ArrayList<String>();
    }


    public boolean isModified(AbstractDataCard abstractDataCard, boolean includeSubDataCards) {
        if (!Utils.objectEquals(modelNo, abstractDataCard.modelNo)) {
            return true;
        }
        if (!Utils.objectEquals(productGroup, abstractDataCard.productGroup)) {
            return true;
        }
        if (!Utils.objectEquals(technicalApprovalDate, abstractDataCard.technicalApprovalDate)) {
            return true;
        }
        if (!Utils.objectEquals(steeringValue, abstractDataCard.steeringValue)) {
            return true;
        }
        if (!Utils.objectEquals(gearboxValue, abstractDataCard.gearboxValue)) {
            return true;
        }
        if (!codes.hasSameContent(abstractDataCard.codes)) {
            return true;
        }
        if (!Utils.objectEquals(event, abstractDataCard.event)) {
            return true;
        }
        if (!saas.hasSameContent(abstractDataCard.saas)) {
            return true;
        }
        if (!Utils.objectEquals(factoryNumber, abstractDataCard.factoryNumber)) {
            return true;
        }
        if (!Utils.objectEquals(pskVariants, abstractDataCard.pskVariants)) {
            return true;
        }
        return false;
    }

    /**
     * Erzeugt eine Baumuster-Datenkarte. Je nach Typ des Baumusters wird es eine Fahrzeug- oder Aggregate-Datenkarte.
     *
     * @param project
     * @param modelNo
     * @return
     */
    public static AbstractDataCard createModelDatacardByModelType(EtkProject project, String modelNo) {
        // Baumuster-Datenkarte im Cache suchen; hier mit Klonen arbeiten, das bisher auch immer neue Datenkarten als
        // Objekte zurückgegeben wurden und diese evtl. verändert werden
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), AbstractDataCard.class, modelNo);
        AbstractDataCard dataCard = iPartsDataCardRetrievalHelper.getCachedDataCard(hashObject, project);
        if (dataCard != null) {
            return dataCard.cloneMe();
        }

        if (iPartsModel.isVehicleModel(modelNo)) {
            dataCard = new VehicleDataCard(true);
            dataCard.fillByModel(project, modelNo);
        } else {
            dataCard = new AggregateDataCard(true);
            dataCard.fillByModel(project, modelNo);
        }

        iPartsDataCardRetrievalHelper.addCachedDataCard(hashObject, dataCard.cloneMe());
        return dataCard;
    }

    public void fillByModel(EtkProject project, String modelNoToLoad) {
        clear();
        dbLanguage = project.getDBLanguage();
        if (iPartsModel.isModelNumberValid(modelNoToLoad)) {
            iPartsModel currentModel = iPartsModel.getInstance(project, new iPartsModelId(modelNoToLoad));
            setModelNo(modelNoToLoad, project);
            if (currentModel.existsInDB()) {
                isLoaded = false;
                isModelLoaded = true;

                // SAA-Liste
                Set<String> saaList = currentModel.getSaas(project);
                TwoGridValues values = new TwoGridValues(saaList, null);
                setSaas(values);

                // Code
                setCodes(new TwoGridValues(currentModel.getCodeSetWithAA(), null));
            }
        }
    }

    protected void loadFromJSONActiveAssignedFpd(iPartsWSactiveAssignedFpd fpdData, EtkProject project, boolean sourceVehicleDatacard) {
        iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(getModelNo()));
        Set<String> modelBuildingCodes = model.getCodeSetWithAA();
        Set<String> technicalCodes = null;
        if (fpdData != null) {
            technicalCodes = iPartsDataCardRetrievalHelper.convertCodeListToStringSet(fpdData.getEquipmentCodes());
            if (technicalCodes != null) {
                technicalCodes.removeAll(modelBuildingCodes);
            }
            setSaas(new TwoGridValues(iPartsDataCardRetrievalHelper.convertSAAListToStringSet(fpdData.getSaa(), sourceVehicleDatacard), null));
        }
        setCodes(new TwoGridValues(modelBuildingCodes, technicalCodes));
    }

    /**
     * Werkskennbuchstaben ermitteln (wird für Millionenüberlauf benötigt)
     *
     * @param project
     * @return Leerstring wenn nicht ermittelt werden kann
     */
    public abstract String getFactorySign(EtkProject project);

    /**
     * Der Ident für die Prüfung der Rückmeldedaten-Ausreißer setzt sich aus dem Werkskennbuchstaben ({@link #getFactorySign(EtkProject)}
     * und der Endnummer ({@link de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId#getSerialNumber()}
     * bzw. {@link AggregateDataCard#getEndNumberString(EtkProject)}) zusammen.
     *
     * @return Leerstring wenn ungültig oder nicht ermittelbar
     */
    public abstract String getSpikeIdent(EtkProject project);

    /**
     * Liefert die Endnummer/Seriennummer zurück.
     *
     * @param project
     * @return
     */
    public abstract int getEndNumber(EtkProject project);

    /**
     * Für die Millionenüberlaufsberechnung muss man die Länge der Endnummer wissen.
     *
     * @param project
     * @return
     */
    public abstract int getEndNumberLength(EtkProject project);

    /**
     * Ermittelt für die mehrstufige Filterung alle passenden Datenkarten zu den gegebenen Baumustern. Erzeugt gegebenenfalls
     * passende Datenkarten aus Baumustern
     *
     * @param project um Datenkarten aus Baumustern zu erzeugen
     * @param models  Liste der gesuchten Baumuster
     * @return leere Liste wenn keine passende Datenkarte gefunden werden kann
     */
    public abstract List<AbstractDataCard> getRelevantDatacardsForModels(EtkProject project, Collection<String> models);

    public abstract List<AbstractDataCard> getFilterRelevantDatacardsForModels(EtkProject project, Collection<String> models);

    /**
     * Ermittelt für die mehrstufige Filterung alle Unterdatenkarten zu dieser Datenkarte inkl. dieser Datenkarte selbt.
     * Erzeugt gegebenenfalls passende Datenkarten aus Baumustern
     *
     * @param project um Datenkarten aus Baumustern zu erzeugen
     * @return leere Liste wenn keine passende Datenkarte gefunden werden kann
     */
    public abstract List<AbstractDataCard> getAllSelectedSubDatacards(EtkProject project);

    /**
     * Ermittelt für die mehrstufige Filterung alle passenden Datenkarten zum gegebenen Modul. Erzeugt gegebenenfalls passende
     * Datenkarten aus Baumustern
     *
     * @param assembly Modul, für das die passenden Datenkarten ermittelt werden sollen
     * @return leere Liste wenn keine passende Datenkarte gefunden werden kann
     */
    public abstract List<AbstractDataCard> getRelevantDatacardsForAssembly(iPartsDataAssembly assembly);

    /**
     * Ermittelt alle Produkte, die den selektierten Aggregate-Baumustern zugeordnet sind.
     * Nur gültig bei Fahrzeug-Datenkarten. Bei Aggregate-Datenkarten wird {@code null} zurückgegeben.
     *
     * @param project
     * @return {@code null} wenn es keine Aggregate-Baumuster gibt oder es sich um eine Aggregate-Datenkarte handelt.
     */
    public abstract Collection<iPartsProduct> getSubDatacardsProducts(EtkProject project);

    /**
     * Liefert zurück, ob diese Datenkarte gültig ist für den Endnummern-Filter (Kombination aus [gültiger Werkskennbuchstaben
     * oder gültige Werksnummer] mit [gültige Endnummer oder gültiger Termin]).
     *
     * @param products ein Produkt oder null für Aufruf aus Endnummmernfilter; ggf. mehrere Produkte für Aufruf aus Filterdialog (nur für Fzg Datenkarte relevant)
     * @param project
     * @return
     */
    public boolean isValidForEndNumberFilter(List<iPartsProduct> products, EtkProject project) {
        return (project != null) && (StrUtils.isValid(getFactorySign(project)) || isFactoryNumberValid(project))
               && ((getEndNumber(project) != FinId.INVALID_SERIAL_NUMBER) || isTechnicalApprovalDateValid());
    }


    /**
     * Ja nach AS-Produktklasse werden hier unterschiedliche Korrekturen an den SAAs vogenommen.
     * Entweder Z0* durch Z * bei Marktspezifischen SAAs (DAIMLER-6071) oder Z0* durch ZT* bei Traktoren (DAIMLER-7893)
     * Die Korrektur erfolgt als Anreicherung der entsprechenden Kopie. Die originale SAA bleibt auf der Datenkarte stehen
     *
     * @param product
     */
    public void correctDatacardSAAs(iPartsProduct product) {
        // AS Produktklassen über die Produkte zum Baumuster ermitteln
        if (product != null) {
            Set<String> allASProductClasses = product.getAsProductClasses();

            // Korrektur für Marktspezifische SAAs nur wenn die AS-Produktklasse nicht exklusiv PKW ist
            boolean isNotExclusivePKW =
                    !((allASProductClasses.size() == 1) && allASProductClasses.contains(iPartsConst.AS_PRODUCT_CLASS_CAR));

            // Korrektur für T-SAAs für nur bei Fahrzeugdatenkarten und wenn die AS-Produktklasse ausschließlich Traktor ist
            boolean isOnlyTractor = isVehicleDataCard() &&
                                    ((allASProductClasses.size() == 1) && allASProductClasses.contains(iPartsConst.AS_PRODUCT_CLASS_TRACTOR));

            if (isNotExclusivePKW || isOnlyTractor) {
                TwoGridValues saas = getSaas();
                iPartsNumberHelper numberHelper = new iPartsNumberHelper();

                if (isNotExclusivePKW) {
                    addCorrectedSAAs(saas, numberHelper, EnrichReasons.MARKETSPECIFIC_SAA, true);
                    addCorrectedSAAs(saas, numberHelper, EnrichReasons.MARKETSPECIFIC_SAA, false);
                }

                if (isOnlyTractor) {
                    addCorrectedSAAs(saas, numberHelper, EnrichReasons.TSA, true);
                    addCorrectedSAAs(saas, numberHelper, EnrichReasons.TSA, false);
                }
            }
        }
    }

    private void addCorrectedSAAs(TwoGridValues saas, iPartsNumberHelper numberHelper, EnrichReasons enrichReason, boolean top) {
        // umformatierte SAAs sollen zusätzlich zu den originalen hinzugefügt werden, aber nur falls diese nicht schon vorhanden sind
        Collection<TwoGridValues.ValueState> gridValues = top ? saas.getTopGridValues() : saas.getBottomGridValues();
        for (TwoGridValues.ValueState topValue : getModifiedSAAsEnrichReason(gridValues, numberHelper, enrichReason)) {
            if (!saas.contains(topValue.value)) {
                saas.addSingleValue(topValue, top);
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Corrected SAA already exists on datacard " + topValue.value);
            }
        }
    }

    private DwList<TwoGridValues.ValueState> getModifiedSAAsEnrichReason(Collection<TwoGridValues.ValueState> saas,
                                                                         iPartsNumberHelper numberHelper,
                                                                         EnrichReasons enrichReason) {
        DwList<TwoGridValues.ValueState> additionalSaas = new DwList<>();
        if ((numberHelper != null) && ((enrichReason == EnrichReasons.TSA) || (enrichReason == EnrichReasons.MARKETSPECIFIC_SAA))) {
            for (TwoGridValues.ValueState saa : saas) {
                String modifiedSAA;
                if (enrichReason == EnrichReasons.TSA) {
                    modifiedSAA = numberHelper.reformatTractorSAA(saa.value);
                } else {
                    modifiedSAA = numberHelper.reformatMarketSpecificSAA(saa.value);
                }
                if (!StrUtils.isEmpty(modifiedSAA)) {
                    TwoGridValues.ValueState clonedSAA = saa.cloneMe();
                    clonedSAA.value = modifiedSAA;
                    clonedSAA.addEnrichReason(enrichReason);
                    additionalSaas.add(clonedSAA);
                }
            }
        }
        return additionalSaas;
    }

    /**
     * Input yyyyMMdd:
     * ---------------
     * "dateOfTechnicalState": "20171221"
     * "technicalApprovalDate": "20171221"
     * <p>
     * Output [yyyy-MM-dd]:
     * --------------------
     *
     * @return
     */
    protected String formatDateForIdent(String yyyyMMddString) {
        if (!StrUtils.isValid(yyyyMMddString)) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            Date dt = formatter.parse(yyyyMMddString);
            if (dt != null) {
                return new SimpleDateFormat("yyyy-MM-dd").format(dt);
            }
        } catch (Exception e) {
            return null;
        }
        return yyyyMMddString;
    }
}