/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSeriesEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSeriesEventList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSeriesCodesDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping.MappingHmMSmToEinPas;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.db.DBDataSet;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.SQLParameterList;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.Tables;

import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Repräsentation einer Baureihe der Konstruktionsstückliste DIALOG (Tabelle DA_DIALOG).
 */
public class iPartsDialogSeries implements iPartsConst {

    private static ObjectInstanceLRUList<Object, iPartsDialogSeries> instances = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_SERIES,
                                                                                                             iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private EtkMultiSprache seriesTitle;
    private String hierarchyForCalcASRel;
    private boolean eventFlag;
    private boolean isAlternativeDocuCalc;
    private boolean mergeProducts;
    private boolean isAutoCalcAndExport;
    private boolean isVPositionCheckAndLinkingActive;
    private Map<iPartsBadCodeId, iPartsDataBadCode> badCodeMap;
    private Map<String, String> activeAAToSOPMap;
    private Map<String, String> activeAAToKEMDueDateMap;
    private Map<String, Map<String, String>> activeAAToFactoryToExpireDate;
    private Map<String, iPartsEvent> eventsMap;
    private volatile List<HmMSmId> subModuleIds;
    private volatile Set<String> validAAForSeries;
    private volatile Set<String> aaValuesWithoutFactoryDataCheck;
    protected iPartsSeriesId seriesId;
    protected String aggregateType;
    protected String productGroup;
    protected volatile Set<String> modelNumbers;

    private volatile iPartsCatalogNode cachedEinPasStructure;
    private volatile iPartsCatalogNode cachedHmMSmStructure;

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized void removeSeriesFromCache(EtkProject project, iPartsSeriesId seriesId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDialogSeries.class, seriesId.getSeriesNumber(), false);
        instances.removeKey(hashObject);
    }

    public static synchronized iPartsDialogSeries getInstance(EtkProject project, iPartsSeriesId seriesId) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDialogSeries.class, seriesId.getSeriesNumber(), false);
        iPartsDialogSeries result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsDialogSeries(project, seriesId);
            instances.put(hashObject, result);
        }

        return result;
    }

    protected iPartsDialogSeries(EtkProject project, iPartsSeriesId seriesId) {
        this.seriesId = seriesId;
        loadHeader(project);
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public EtkMultiSprache getSeriesTitle() {
        return seriesTitle;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getProductGroup() {
        return productGroup;
    }

    public boolean isEventTriggered() {
        return eventFlag;
    }

    public boolean isAggregateSeries() {
        return seriesId.isAggregateSeries();
    }

    public boolean isAutoCalcAndExport() {
        return isAutoCalcAndExport;
    }

    public boolean isVPositionCheckAndLinkingActive() {
        return isVPositionCheckAndLinkingActive;
    }

    public Set<String> getAAValuesWithoutFactoryDataCheck() {
        return aaValuesWithoutFactoryDataCheck;
    }

    public Collection<HmMSmId> getSubModuleIds(EtkProject project) {
        loadIfNeeded(project);
        return Collections.unmodifiableCollection(subModuleIds);
    }

    public Collection<String> getValidAAForSeries(EtkProject project) {
        loadIfNeeded(project);
        return Collections.unmodifiableCollection(validAAForSeries);
    }

    /**
     * Fügt die übergebene HmMSmId zur Liste der Submodule dieser Assembly hinzu.
     * Dadurch wird dann z.B. ein neu erstelltes Modul auch im Navigationsbaum angezeigt
     *
     * @param hmMSmId
     */
    public void addSubModuleIfNotExists(HmMSmId hmMSmId) {
        if (!subModuleIds.contains(hmMSmId)) {
            subModuleIds.add(hmMSmId);
            cachedHmMSmStructure = null;
        }
    }

    /**
     * Löscht die übergebene HmMSmId aus der Liste der Submodule dieser Assembly.
     * Dadurch wird dann z.B. ein Modul aus dem Navigationsbaum entfernt, dass nach dem Löschen des letzten Eintrags leer ist
     *
     * @param hmMSmId
     */
    public void removeSubModuleIfExists(HmMSmId hmMSmId) {
        if (subModuleIds.contains(hmMSmId)) {
            subModuleIds.remove(hmMSmId);
            cachedHmMSmStructure = null;
        }
    }

    private void loadHeader(EtkProject project) {
        eventsMap = new LinkedHashMap<>();
        badCodeMap = new HashMap<>();
        String seriesNumber = seriesId.getSeriesNumber();
        DBDataObjectAttributes attributes = project.getDbLayer().getAttributes(TABLE_DA_SERIES, new String[]{ FIELD_DS_SERIES_NO },
                                                                               new String[]{ seriesNumber });
        if (attributes != null) {
            seriesTitle = project.getEtkDbs().getMultiLanguageByTextNr(TableAndFieldName.make(TABLE_DA_SERIES, FIELD_DS_NAME),
                                                                       attributes.getField(FIELD_DS_NAME).getAsString());
            aggregateType = attributes.getFieldValue(FIELD_DS_TYPE);
            productGroup = attributes.getFieldValue(FIELD_DS_PRODUCT_GRP);
            eventFlag = attributes.getField(FIELD_DS_EVENT_FLAG).getAsBoolean();
            isAlternativeDocuCalc = attributes.getField(FIELD_DS_ALTERNATIVE_CALC).getAsBoolean();
            hierarchyForCalcASRel = attributes.getFieldValue(FIELD_DS_HIERARCHY);
            mergeProducts = attributes.getField(FIELD_DS_MERGE_PRODUCTS).getAsBoolean();
            isAutoCalcAndExport = attributes.getField(FIELD_DS_AUTO_CALCULATION).getAsBoolean();
            isVPositionCheckAndLinkingActive = attributes.getField(FIELD_DS_V_POSITION_CHECK).getAsBoolean();
        } else {
            seriesTitle = new EtkMultiSprache("!!Baureihe '%1' nicht gefunden",
                                              project.getConfig().getDatabaseLanguages(),
                                              seriesNumber);
            aggregateType = "";
            productGroup = "";
            hierarchyForCalcASRel = "";
            aaValuesWithoutFactoryDataCheck = Collections.emptySet();
            return;
        }
        // Bei bestimmten AA sollen die Werksdaten bei der Doku-Relevanz nicht berücksichtigt werden
        aaValuesWithoutFactoryDataCheck = new HashSet<>(SetOfEnumDataType.parseSetofEnum(attributes.getFieldValue(FIELD_DS_AA_WO_FACTORY_DATA), false, false));
        // BAD-Code
        for (iPartsDataBadCode badCodeData : iPartsDataBadCodeList.loadAllBadCodesForSeries(project, seriesNumber)) {
            badCodeMap.put(badCodeData.getAsId(), badCodeData);
        }
        // SOP Daten
        activeAAToSOPMap = new HashMap<>();
        activeAAToKEMDueDateMap = new HashMap<>();
        iPartsDataSeriesSOPList seriesSOPList = iPartsDataSeriesSOPList.getAllSeriesSOP(project, seriesId);
        for (iPartsDataSeriesSOP seriesSOP : seriesSOPList) {
            if (seriesSOP.getFieldValueAsBoolean(FIELD_DSP_ACTIVE)) {
                String sop = seriesSOP.getFieldValue(FIELD_DSP_START_OF_PROD);
                String kemDueDate = seriesSOP.getFieldValue(FIELD_DSP_KEM_TO);
                if (StrUtils.isValid(sop)) {
                    activeAAToSOPMap.put(seriesSOP.getAsId().getSeriesAA(), sop);
                }
                if (StrUtils.isValid(kemDueDate)) {
                    activeAAToKEMDueDateMap.put(seriesSOP.getAsId().getSeriesAA(), kemDueDate);
                }
            }
        }

        // Auslauftermine
        activeAAToFactoryToExpireDate = new HashMap<>();
        iPartsDataSeriesExpireDateList seriesExpireDatesList = iPartsDataSeriesExpireDateList.getAllSeriesExpireDate(project, seriesId);
        seriesExpireDatesList.forEach(seriesExpireDateData -> {
            String aaValue = seriesExpireDateData.getAsId().getSeriesAA();
            String factory = seriesExpireDateData.getAsId().getSeriesFactoryNo();
            String expDate = seriesExpireDateData.getDbExpireDate();
            if (StrUtils.isValid(aaValue, factory, expDate)) {
                Map<String, String> factoryToExpDateMap = activeAAToFactoryToExpireDate.computeIfAbsent(aaValue, k -> new HashMap<>());
                factoryToExpDateMap.put(factory, expDate);
            }
        });

        // Ereignisse laden
        if (eventFlag) {
            int ordinal = 0;
            List<iPartsDataSeriesEvent> eventsList = iPartsDataSeriesEventList.loadEventGraphForSeries(project, seriesNumber, false);
            for (iPartsDataSeriesEvent dataEvent : eventsList) {
                String eventID = dataEvent.getEventId();
                eventsMap.put(eventID, new iPartsEvent(dataEvent, ordinal));
                ordinal++;
            }
        }
    }

    // Module der Dialog Hm/M/Sm Struktur laden
    private void loadIfNeeded(EtkProject project) {
        if (subModuleIds == null) {
            List<HmMSmId> newSubModuleIds = new DwList<>();

            DBSQLQuery query = project.getDB().getDBForDomain(MAIN).getNewQuery();
            SQLParameterList params = new SQLParameterList();

            List<String> resultFields = new ArrayList<>();
            resultFields.add(FIELD_DD_SERIES_NO.toLowerCase());
            resultFields.add(FIELD_DD_HM.toLowerCase());
            resultFields.add(FIELD_DD_M.toLowerCase());
            resultFields.add(FIELD_DD_SM.toLowerCase());

            query.selectDistinct(new Fields(resultFields)).from(new Tables(TABLE_DA_DIALOG.toLowerCase())).where(new Condition(params, FIELD_DD_SERIES_NO.toLowerCase(), "=", seriesId.getSeriesNumber()));

            query.orderBy(ArrayUtil.toStringArray(resultFields));

            DBDataSet dbSet = query.executeQuery(params);

            while (dbSet.next()) {
                EtkRecord rec = dbSet.getRecord(new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM });
                String series = rec.getField(FIELD_DD_SERIES_NO).getAsString();
                String hm = rec.getField(FIELD_DD_HM).getAsString();
                String m = rec.getField(FIELD_DD_M).getAsString();
                String sm = rec.getField(FIELD_DD_SM).getAsString();
                newSubModuleIds.add(new HmMSmId(series, hm, m, sm));
            }
            dbSet.close();

            synchronized (this) {
                if (subModuleIds == null) {
                    subModuleIds = newSubModuleIds;
                }
            }
        }
        if (validAAForSeries == null) {
            validAAForSeries = iPartsSeriesCodesDataList.loadAllAusfuehrungsarten(project, seriesId.getSeriesNumber());
        }
    }

    public iPartsCatalogNode getCompleteHmMSmStructure(EtkProject project) {
        if (cachedHmMSmStructure != null) {
            return cachedHmMSmStructure;
        }

        loadIfNeeded(project);

        iPartsCatalogNode result = new iPartsCatalogNode(seriesId, true);

        for (HmMSmId subModuleId : subModuleIds) {
            iPartsCatalogNode.getOrCreateHmMSmNode(result, subModuleId);
        }

        synchronized (this) {
            if (cachedHmMSmStructure == null) {
                cachedHmMSmStructure = result;
            }
        }

        return cachedHmMSmStructure;
    }

    public iPartsCatalogNode getCompleteEinPasStructureFromHmMSm(EtkProject project) {
        if (cachedEinPasStructure != null) {
            return cachedEinPasStructure;
        }

        loadIfNeeded(project);

        MappingHmMSmToEinPas mapping = MappingHmMSmToEinPas.getInstance(project, seriesId);

        iPartsCatalogNode result = new iPartsCatalogNode(seriesId, true);

        for (HmMSmId hmMSmId : subModuleIds) {
            // Alle EinPAS-Knoten, an das das Modul direkt eingehängt wurde
            List<EinPasId> einPasNodes = new DwList<>();
            List<HmMSmId> hmMSmIds = new DwList<>();
            List<EinPasId> mappedValues = mapping.get(hmMSmId);
            if (mappedValues != null) {
                einPasNodes.addAll(mappedValues);
            } else {
                // Kein Mapping vorhanden -> Hänge den HM/M/SM-Knoten direkt ein
                hmMSmIds.add(hmMSmId);
            }

            // Jetzt die Knoten einfügen
            for (EinPasId einPasId : einPasNodes) {
                iPartsCatalogNode.getOrCreateEinPasNode(result, einPasId);
            }

            // Und die nicht gemappten
            for (HmMSmId actHmMSmId : hmMSmIds) {
                // Knoten für die fehlenden finden oder erstellen
                iPartsCatalogNode missingNode = result.getOrCreateChild(new EinPasId(TranslationHandler.translate("!!Fehlendes EinPAS-Mapping"), "", ""), true);
                // Und den nicht gemappten Knoten an diese drunterhängen
                iPartsCatalogNode.getOrCreateHmMSmNode(missingNode, actHmMSmId);
            }
        }

        synchronized (this) {
            if (cachedEinPasStructure == null) {
                cachedEinPasStructure = result;
            }
        }

        return cachedEinPasStructure;
    }

    public Map<iPartsBadCodeId, iPartsDataBadCode> getBadCodeMap() {
        return badCodeMap;
    }

    private Map<iPartsBadCodeId, iPartsDataBadCode> getBadCodeMapForAA(String ausfuehrungsArt) {
        Map<iPartsBadCodeId, iPartsDataBadCode> result = new HashMap<>();
        for (iPartsDataBadCode badCode : badCodeMap.values()) {
            if (checkBadCodeAusfuehrungsart(badCode.getAsId().getAusfuehrungsart(), ausfuehrungsArt)) {
                result.put(badCode.getAsId(), badCode);
            }
        }
        return result;
    }

    /**
     * Liefert alle gültigen Bad-Code zum aktuellen Datum und der übergebenen Ausführungsart.
     * Also dauerhafte oder aktuell gültige (aktuelles Datum <= Verfallsdatum der BAD-Coderegel) BAD-Code.
     *
     * @param ausfuehrungsArt
     * @return
     */
    public Set<iPartsBadCodeId> getBadCodesForCurrentDateAndAA(String ausfuehrungsArt, Map<String, String> errorMsgList) {

        Set<iPartsBadCodeId> result = new HashSet<>();
        for (iPartsDataBadCode badCode : getBadCodeMapForAA(ausfuehrungsArt).values()) {
            if (StrUtils.isEmpty(badCode.getExpiryDate())) {
                result.add(badCode.getAsId());
                continue;
            }
            String currentDate = DateUtils.getCurrentDateFormatted(DateUtils.simpleDateFormatyyyyMMdd);
            boolean badCodeIsValid = false;
            try {
                badCodeIsValid = DateUtils.dateIsAfterOrEqualDate_yyyyMMdd(badCode.getExpiryDate(), currentDate);
            } catch (DateException e) {
                String msg = "BAD CODE: " + badCode.getAsId().toStringWithDescription() + ", " + e.getMessage();
                // Der Schlüssel für die Map ist eine Kombination aus Modulnummer und dem falschen Datum.
                String keyStr = badCode.getAsId().toStringWithDescription() + "," + badCode;
                if (!errorMsgList.containsKey(keyStr)) {
                    errorMsgList.put(keyStr, msg);
                    Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Error while loading Bad Codes! " +
                                                                              badCode.getAsId().toStringWithDescription() + "; " +
                                                                              e.getMessage() + "; Valid format for expiry dates: " +
                                                                              DateUtils.simpleDateFormatyyyyMMdd);
                }
            }
            if (badCodeIsValid) {
                result.add(badCode.getAsId());
            }
        }
        return result;
    }

    /**
     * Liefert alle permanenten Bad-Code ({@link iPartsConst#FIELD_DBC_PERMANENT_BAD_CODE} == {@code true}) zur übergebenen Ausführungsart
     *
     * @param ausfuehrungsArt
     * @return
     */
    public Set<iPartsBadCodeId> getPermanentBadCodeForAA(String ausfuehrungsArt) {

        Set<iPartsBadCodeId> result = new HashSet<>();
        for (iPartsDataBadCode badCode : getBadCodeMapForAA(ausfuehrungsArt).values()) {
            if (badCode.isPermanent()) {
                result.add(badCode.getAsId());
            }
        }
        return result;
    }

    /**
     * Überprüft, ob die übergebene Ausführungsart des Bad-Code der übergebenen Ausführungsart entspricht. Wenn die
     * Ausführungsart des Code leer ist, ist der Bad-Code gültig.
     *
     * @param ausfuehrungsartFromCode
     * @param ausfuehrungsArt
     * @return
     */
    private boolean checkBadCodeAusfuehrungsart(String ausfuehrungsartFromCode, String ausfuehrungsArt) {
        if (StrUtils.isEmpty(ausfuehrungsartFromCode)) {
            return true;
        }
        return ausfuehrungsartFromCode.equals(ausfuehrungsArt);
    }

    private boolean hasStartOfProductionDates() {
        return (activeAAToSOPMap != null) && !activeAAToSOPMap.isEmpty();
    }

    public boolean hasKEMDueDates() {
        return (activeAAToKEMDueDateMap != null) && !activeAAToKEMDueDateMap.isEmpty();
    }

    public String getKemDueDateForAA(String aaValue) {
        if (StrUtils.isValid(aaValue) && hasKEMDueDates() && activeAAToKEMDueDateMap.containsKey(aaValue)) {
            return activeAAToKEMDueDateMap.get(aaValue);
        }
        return "";
    }

    public String getStartOfProductionDateForAA(String aaValue) {
        if (StrUtils.isValid(aaValue) && hasStartOfProductionDates() && activeAAToSOPMap.containsKey(aaValue)) {
            return activeAAToSOPMap.get(aaValue);
        }
        return "";
    }

    private boolean hasExpirationDates() {
        return (activeAAToFactoryToExpireDate != null) && !activeAAToFactoryToExpireDate.isEmpty();
    }

    public boolean hasExpirationDatesForAA(String aaValue) {
        if (hasExpirationDates()) {
            Map<String, String> factoryToExpDate = activeAAToFactoryToExpireDate.get(aaValue);
            return (factoryToExpDate != null) && !factoryToExpDate.isEmpty();
        }
        return false;
    }

    /**
     * Liefert alle Werke inkl ihren Auslauftermine für die übergebenen Ausführungsart
     *
     * @param aaValue
     * @return
     */
    public Map<String, String> getExpirationDatesForAA(String aaValue) {
        if (StrUtils.isValid(aaValue) && hasExpirationDatesForAA(aaValue)) {
            return activeAAToFactoryToExpireDate.get(aaValue);
        }
        return null;
    }

    /**
     * Liefert den Auslauftermin für das übergebene Werk und die übergebene Ausführungsart
     *
     * @param aaValue
     * @param factory
     * @return
     */
    public String getExpirationDateForFactoryAndAA(String aaValue, String factory) {
        Map<String, String> faytoryToExpDate = getExpirationDatesForAA(aaValue);
        if ((faytoryToExpDate != null) && faytoryToExpDate.containsKey(factory)) {
            return faytoryToExpDate.get(factory);
        }
        return "";
    }


    /**
     * Liefert die Map von Ereignis-IDs auf {@link iPartsEvent}s dieser Baureihe zurück, wobei nur die jeweils neuesten
     * Ereignisse mit leerem KEM-Datum-bis berücksichtigt werden.
     *
     * @return
     */
    public Map<String, iPartsEvent> getEventsMap() {
        return Collections.unmodifiableMap(eventsMap);
    }

    /**
     * Liefert den {@link iPartsEvent} zur übergebenen Ereignis-ID.
     *
     * @param eventId
     * @return {@code null} falls die Ereignis-ID sich nicht in der Ereigniskette dieser Baureihe befindet
     */
    public iPartsEvent getEvent(String eventId) {
        return eventsMap.get(eventId);
    }

    /**
     * Liefert die Reihenfolge der übergebenen Ereignis-ID innerhalb der Ereigniskette dieser Baureihe zurück beginnend
     * bei {@code 0}.
     *
     * @param eventId
     * @return {@code -1} falls die Ereignis-ID sich nicht in der Ereigniskette dieser Baureihe befindet
     */
    public int getEventOrdinal(String eventId) {
        iPartsEvent event = getEvent(eventId);
        if (event != null) {
            return event.getOrdinal();
        } else {
            return -1;
        }
    }

    public boolean isAlternativeDocuCalc() {
        return isAlternativeDocuCalc;
    }

    public String getHierarchyForCalcASRel() {
        return hierarchyForCalcASRel;
    }

    public boolean isMergeProducts() {
        return mergeProducts;
    }

    /**
     * Liefert alle Baumusternummern für diese Baureihe zurück.
     *
     * @param project
     * @return
     */
    public Set<String> getModelNumbers(EtkProject project) {
        if (modelNumbers == null) {
            Set<String> modelNumbersLocal = new TreeSet<>();
            iPartsDataModelList dataModelList = iPartsDataModelList.loadDataModelList(project, seriesId.getSeriesNumber(),
                                                                                      DBDataObjectList.LoadType.ONLY_IDS);
            for (iPartsDataModel dataModel : dataModelList) {
                modelNumbersLocal.add(dataModel.getAsId().getModelNumber());
            }
            modelNumbers = modelNumbersLocal;
        }
        return modelNumbers;
    }

    /**
     * Löscht den Cache für die Baumusternummern dieser Baureihe.
     */
    public void clearModelNumbersCache() {
        modelNumbers = null;
    }
}