/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsAggCodeMappingId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsAggCodeMappingList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAggCodeMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.*;

/**
 * Cache für die Tabelle DA_AGG_PART_CODES (Beziehung zwischen ASachnummer und zusätzlichen AggregateCode)
 */
public class iPartsAggCodeMappingCache implements CacheForGetCacheDataEvent<iPartsAggCodeMappingCache> {

    private static ObjectInstanceStrongLRUList<Object, iPartsAggCodeMappingCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    @JsonProperty
    private Map<String, ObjectNoData> objectNoToObjectNoData;

    private static String getInstanceHashObject(EtkProject project) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsAggCodeMappingCache.class, "AggCodeMapping", false);
    }

    public static synchronized iPartsAggCodeMappingCache getInstance(EtkProject project) {
        Object hashObject = getInstanceHashObject(project);
        iPartsAggCodeMappingCache result = instances.get(hashObject);

        if (result == null) {
            result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsAggCodeMappingCache(), null, instances, hashObject);
            if (result != null) {
                return result;
            }

            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsAggCodeMappingCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }


    @Override
    public iPartsAggCodeMappingCache createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        return createInstance(setCacheDataEvent, instances, getInstanceHashObject(project));
    }

    private void load(EtkProject project) {
        objectNoToObjectNoData = new HashMap<>();
        iPartsAggCodeMappingList list = iPartsAggCodeMappingList.loadAllCodesForPartNoList(project);
        for (iPartsDataAggCodeMapping dataAggCodeMapping : list) {
            iPartsAggCodeMappingId aggCodeMappingId = dataAggCodeMapping.getAsId();
            String objectNo = aggCodeMappingId.getAggPartNo();
            ObjectNoData objectNoData = objectNoToObjectNoData.get(objectNo);
            if (objectNoData == null) {
                objectNoData = new ObjectNoData(objectNo);
                objectNoToObjectNoData.put(objectNo, objectNoData);
            }
            // Einen Datensatz für die aktuelle Sachnummer erzeugen
            ObjectNoDataItem dataItem = new ObjectNoDataItem(aggCodeMappingId.getCode(), aggCodeMappingId.getDateFrom(),
                                                             aggCodeMappingId.getDateTo(), aggCodeMappingId.getFactory(),
                                                             aggCodeMappingId.getFactorySign(), aggCodeMappingId.getSeries());
            objectNoData.addObjectDataItem(dataItem);


        }
    }

    public Collection<String> getAggCodeMappingIdsByPartNo(String partNo) {
        ObjectNoData objectNoData = getAggCodeMappingDataByPartNo(partNo);
        if (objectNoData != null) {
            return Collections.unmodifiableCollection(objectNoData.getCode());
        }
        return null;
    }

    public ObjectNoData getAggCodeMappingDataByPartNo(String partNo) {
        return objectNoToObjectNoData.get(partNo);
    }

    /**
     * Liefert zurück, ob für die Sachnummer EInträge vorhanden sind
     *
     * @param objectNoAsAPartNo
     * @return
     */
    public boolean hasMappingData(String objectNoAsAPartNo) {
        return objectNoToObjectNoData.containsKey(objectNoAsAPartNo);
    }

    /**
     * Liefert zurück, ob zur gleichen Sachnummer unterschiedliche Werke existieren
     *
     * @param objectNoData
     * @return
     */
    public boolean hasMultipleDataWithDifferentFactories(ObjectNoData objectNoData) {
        return objectNoData.factories.size() > 1;
    }

    /**
     * Liefert die Code zur ZB Sachnummer abhängig vom Aggregatetyp, Werkskennbuchstaben und dem Aggregatedatum.
     * <p>
     * Logik zur Anreicherung: DAIMLER-8890
     *
     * @param aPartNo
     * @param aggregateType
     * @param factorySign
     * @param aggDataCardDate
     * @return
     */
    public Collection<String> getCodeForObjectNo(String aPartNo, DCAggregateTypes aggregateType, String factorySign, String aggDataCardDate) {
        // Check, ob es ein Motor, ein Getriebe, eine Brennstoffzelle oder eine Hochvoltbatterie ist
        if (aggregateType.isZBEnrichmentType()) {
            //  Ermittle über die ZB Sachnummer die Datensätze aus DA_AGG_PART_CODES
            iPartsAggCodeMappingCache.ObjectNoData objectNoData = getAggCodeMappingDataByPartNo(aPartNo);
            if (objectNoData == null) {
                return null;
            }
            // Prüfe ob es Datensätze zu unterschiedlichen Werken gibt
            if (hasMultipleDataWithDifferentFactories(objectNoData)) {
                // Falls Ja , ermittle die Datensätze passend zum Werk (Herstellerwerk des Aggregats=Werksnummer (IdentKB) aus DA_AGG_PART_CODES)
                List<ObjectNoDataItem> dataWithSameFactorySign = objectNoData.getDataForFactorySign(factorySign);
                // Prüfe ob Code mehrfach vorkommen (Mehrere Datensätze mit gleichem Code)
                if (hasMultipleDataWithSameCodes(dataWithSameFactorySign)) {
                    // Falls Ja, ermittle die Datensätze die passend zum Datum und Herstellerwerk (1-stelliger Kennbuchstabe
                    // des Idents) sind und schreibe diese auf die Aggregatedatenkarte:
                    // TERMA <= Datum Aggregatedatenkarte < TERMB oder TERMB=“leer/99.99.99"
                    return getCodeForDateAndFactorySign(dataWithSameFactorySign, factorySign, aggDataCardDate);
                } else {
                    // Falls Nein, prüfe ob bei allen Datensätzen die folgenden Attribute gleich sind:
                    // BR, WKB, IDENTKB, TERMA, TERMB
                    if (hasSameData(dataWithSameFactorySign)) {
                        // Falls Ja, schreibe alle Zusteuercode an die Aggregate-Datenkarte (unteres Grid für techn. Code)
                        Set<String> codes = new HashSet<>();
                        for (ObjectNoDataItem objectNoDataItem : dataWithSameFactorySign) {
                            codes.add(objectNoDataItem.getCode());
                        }
                        return codes;
                    } else {
                        // Falls Nein, ermittle die Datensätze die passend zum Datum und Herstellerwerk (1-stelliger
                        // Kennbuchstabe des Idents) sind und schreibe diese auf die Aggregatedatenkarte:
                        // TERMA <= Datum Aggregatedatenkarte < TERMB oder TERMB=“leer/99.99.99"
                        return getCodeForDateAndFactorySign(dataWithSameFactorySign, factorySign, aggDataCardDate);
                    }
                }
            } else {
                //  Falls Nein (somit nur ein Werk), prüfe ob Code mehrfach vorkommen (Mehrere Datensätze mit gleichem Code)
                if (hasMultipleDataWithSameCodes(objectNoData.allObjectNoItems)) {
                    // Falls Ja (Code kommt mehrfach vor), ermittle die Datensätze die passend zum Datum sind und
                    // schreibe diese auf die Aggregatedatenkarte
                    return getCodeForDate(objectNoData.allObjectNoItems, aggDataCardDate);
                } else {
                    // Falls Nein, prüfe ob bei allen Datensätzen die folgenden Attribute gleich sind: BR, WKB, IDENTKB, TERMA, TERMB
                    if (hasSameData(objectNoData.allObjectNoItems)) {
                        // Falls Ja, schreibe alle Zusteuercode an die Aggregate-Datenkarte (unteres Grid für techn. Code)
                        return objectNoData.getCode();
                    } else {
                        // Falls Nein, ermittle die Datensätze die passend zum Datum sind und schreibe diese auf die
                        // Aggregatedatenkarte:
                        // TERMA <= Datum Aggregatedatenkarte < TERMB oder TERMB=“leer/99.99.99“
                        return getCodeForDate(objectNoData.allObjectNoItems, aggDataCardDate);
                    }
                }
            }
        } else {
            return getAggCodeMappingIdsByPartNo(aPartNo);

        }
    }

    /**
     * Sammelt alle Code aller übergebener {@link ObjectNoDataItem} Objekte, die zum übergebenen Werkennbuchstaben und
     * zum übergebenen Datum passen.
     *
     * @param objectNoItems
     * @param factorySign
     * @param aggDataCardDate
     * @return
     */
    private Collection<String> getCodeForDateAndFactorySign(List<ObjectNoDataItem> objectNoItems, String factorySign, String aggDataCardDate) {
        Set<String> result = new HashSet<>();
        String dataCardDate = StrUtils.isValid(aggDataCardDate) ? StrUtils.padStringWithCharsUpToLength(aggDataCardDate, '0', 14) : "";
        for (ObjectNoDataItem objectNoDataItem : objectNoItems) {
            addIfValidCode(result, objectNoDataItem.getCodeForDateAndFactorySign(dataCardDate, factorySign));
        }
        return result;
    }

    /**
     * Sammelt alle Code aller übergebener {@link ObjectNoDataItem} Objekte, die zum übergebenen Datum passen.
     *
     * @param objectNoItems
     * @param aggDataCardDate
     * @return
     */
    private Collection<String> getCodeForDate(List<ObjectNoDataItem> objectNoItems, String aggDataCardDate) {
        Set<String> result = new HashSet<>();
        String dataCardDate = StrUtils.isValid(aggDataCardDate) ? StrUtils.padStringWithCharsUpToLength(aggDataCardDate, '0', 14) : "";
        for (ObjectNoDataItem objectNoDataItem : objectNoItems) {
            addIfValidCode(result, objectNoDataItem.getCodeForDate(dataCardDate));
        }
        return result;
    }

    private void addIfValidCode(Set<String> result, String code) {
        if (StrUtils.isValid(code)) {
            result.add(code);
        }
    }

    /**
     * LIefert zurück, ob unterschiedliche {@link ObjectNoDataItem} Objekte gleiche Code besitzen
     *
     * @param objectNoItems
     * @return
     */
    public boolean hasMultipleDataWithSameCodes(List<ObjectNoDataItem> objectNoItems) {
        Set<String> allCodes = new HashSet<>();
        for (ObjectNoDataItem objectDataItem : objectNoItems) {
            if (!allCodes.add(objectDataItem.getCode())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSameData(List<ObjectNoDataItem> objectNoItems) {
        String sameDataString = null;
        for (ObjectNoDataItem objectDataItem : objectNoItems) {
            if (sameDataString == null) {
                sameDataString = objectDataItem.getAsString();
                continue;
            }
            if (!sameDataString.equals(objectDataItem.getAsString())) {
                return false;
            }
        }
        return true;
    }


    public static class ObjectNoData implements RESTfulTransferObjectInterface {

        @JsonProperty
        private String objectNo;
        @JsonProperty
        private Set<String> codes;
        @JsonProperty
        private Set<String> factories;
        @JsonProperty
        private Set<String> seriesForObjectNo;
        @JsonProperty
        private List<ObjectNoDataItem> allObjectNoItems;

        public ObjectNoData() {
        }

        public ObjectNoData(String objectNo) {
            this.objectNo = objectNo;
            this.codes = new TreeSet<>();
            this.seriesForObjectNo = new HashSet<>();
            this.allObjectNoItems = new ArrayList<>();
            this.factories = new HashSet<>();
        }

        public Set<String> getCode() {
            return codes;
        }

        public String getObjectNo() {
            return objectNo;
        }

        public void addObjectDataItem(ObjectNoDataItem objectNoDataItem) {
            if (objectNoDataItem != null) {
                allObjectNoItems.add(objectNoDataItem);
                addIfNotEmtpy(codes, objectNoDataItem.getCode());
                addIfNotEmtpy(factories, objectNoDataItem.getFactoryNumber());
                addIfNotEmtpy(seriesForObjectNo, objectNoDataItem.getSeries());
            }
        }

        private void addIfNotEmtpy(Set<String> setForData, String data) {
            if (StrUtils.isValid(data)) {
                setForData.add(data);
            }
        }

        public List<ObjectNoDataItem> getDataForFactorySign(String factorySign) {
            List<ObjectNoDataItem> result = new ArrayList<>();
            for (ObjectNoDataItem objectDataItem : allObjectNoItems) {
                if (objectDataItem.getFactorySign().equals(factorySign)) {
                    result.add(objectDataItem);
                }
            }
            return result;
        }
    }

    public static class ObjectNoDataItem implements RESTfulTransferObjectInterface {

        @JsonProperty
        private String code;
        @JsonProperty
        private String dateFrom;
        @JsonProperty
        private String dateTo;
        @JsonProperty
        private String factorySign;
        @JsonProperty
        private String factoryNumber;
        @JsonProperty
        private String series;

        public ObjectNoDataItem() {
        }

        public ObjectNoDataItem(String code, String dateFrom, String dateTo, String factoryNumber, String factorySign, String series) {
            this.code = code;
            this.dateFrom = dateFrom;
            this.dateTo = dateTo;
            this.factoryNumber = factoryNumber;
            this.factorySign = factorySign;
            this.series = series;
        }

        public String getCode() {
            return code;
        }

        public String getDateFrom() {
            return dateFrom;
        }

        public String getDateTo() {
            return dateTo;
        }

        public String getFactorySign() {
            return factorySign;
        }

        public String getFactoryNumber() {
            return factoryNumber;
        }

        public String getSeries() {
            return series;
        }

        public String getAsString() {
            return series + factorySign + factoryNumber + dateFrom + dateTo;
        }

        /**
         * Liefert alle Code die gültig sind zum übergebenen Datum
         *
         * @param dataCardDate
         * @return
         */
        public String getCodeForDate(String dataCardDate) {
            String dateFrom = StrUtils.isValid(getDateFrom()) ? StrUtils.padStringWithCharsUpToLength(getDateFrom(), '0', 14) : "";
            String dateTo = StrUtils.isValid(getDateTo()) ? StrUtils.padStringWithCharsUpToLength(getDateTo(), '0', 14) : "";
            if ((dateFrom.compareTo(dataCardDate) <= 0) && ((dataCardDate.compareTo(dateTo) < 0) || dateTo.isEmpty())) {
                return getCode();
            }
            return null;
        }

        /**
         * Liefert alle Code die gültig sind zum übergebenen Datum und die den übergebenen Werkskennbuchstaben besitzen
         *
         * @param dataCardDate
         * @param factorySign
         * @return
         */
        public String getCodeForDateAndFactorySign(String dataCardDate, String factorySign) {
            if (getFactorySign().equals(factorySign)) {
                return getCodeForDate(dataCardDate);
            }
            return null;
        }
    }
}
