/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsResponseDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.AbstractCacheWithChangeSets;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.util.ArrayUtil;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

/**
 * Cache für alle Rückmeldedaten aus der Tabelle DA_RESPONSE_DATA.
 */
public class iPartsResponseData implements CacheForGetCacheDataEvent<iPartsResponseData>, iPartsConst {

    // Lebensdauer für die ChangeSets ist iPartsPlugin.getCachesLifeTime(); Lebensdauer im Cache aber MAX_CACHE_LIFE_TIME_CORE
    private static AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsResponseData>> cacheWithChangeSets =
            new AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsResponseData>>(MAX_CACHE_SIZE_CHANGE_SETS, iPartsPlugin.getCachesLifeTime()) {
                @Override
                protected ObjectInstanceStrongLRUList<Object, iPartsResponseData> createNewCache() {
                    return new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE, MAX_CACHE_LIFE_TIME_CORE);
                }
            };

    protected Map<String, List<iPartsDataResponseData>> responseDataByPEM = new HashMap<>();
    protected iPartsResponseData sourceCache;
    @JsonProperty
    private Map<String, List<List<String>>> responseDataByPEMasStringList; // Nur für die JSON-Serialisierung

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        cacheWithChangeSets.clearCache();
    }

    /**
     * Entfernt den Cache-Eintrag für die gerade aktiven {@link AbstractRevisionChangeSet}s.
     *
     * @param project
     */
    public static synchronized void removeCacheForActiveChangeSets(EtkProject project) {
        cacheWithChangeSets.removeCacheForActiveChangeSets(project);
    }

    private static String getInstanceHashObject(EtkProject project) {
        // Hier nun keine ChangeSets mehr verwenden für das hashObject, weil dies ja bereits über cacheWithChangeSets
        // gelöst wurde
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsResponseData.class, "ResponseData", false);
    }

    public static synchronized iPartsResponseData getInstance(final EtkProject project) {
        ObjectInstanceStrongLRUList<Object, iPartsResponseData> cache = cacheWithChangeSets.getCacheInstance(project);
        Object hashObject = getInstanceHashObject(project);
        iPartsResponseData result = cache.get(hashObject);

        if (result == null) {
            if (cache == cacheWithChangeSets.getNormalCache()) {
                result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsResponseData(), null, cache, hashObject);
                if (result != null) {
                    return result;
                }
            }

            result = new iPartsResponseData();

            if (project.getEtkDbs().isRevisionChangeSetActive()) {
                // Den normalen Cache zunächst laden falls notwendig
                iPartsResponseData normalCacheData = cacheWithChangeSets.getNormalCache().get(hashObject);
                if (normalCacheData == null) {
                    normalCacheData = new iPartsResponseData();
                    final iPartsResponseData normalCacheDataFinal = normalCacheData;

                    // Der normale Cache muss ohne aktive ChangeSets geladen werden
                    project.getRevisionsHelper().executeWithoutActiveChangeSets(new Runnable() {
                        @Override
                        public void run() {
                            normalCacheDataFinal.load(project);
                        }
                    }, false, project);

                    cacheWithChangeSets.getNormalCache().put(hashObject, normalCacheData);
                }

                // Zunächst nur den normalen Cache referenzieren
                referenceCache(normalCacheData, result);

                // Danach alle Änderungen an den Rückmeldedaten aus den aktiven ChangeSets simulieren
                String[] pkFields = null;
                for (AbstractRevisionChangeSet changeSet : project.getEtkDbs().getActiveRevisionChangeSets()) {
                    Collection<SerializedDBDataObject> serializedResponseDataList = changeSet.getSerializedObjectsByTable(TABLE_DA_RESPONSE_DATA);
                    if (serializedResponseDataList != null) {
                        if (pkFields == null) { // Primärschlüsselfelder nur einmal bestimmen
                            EtkDatabaseTable dbTable = project.getConfig().getDBDescription().findTable(TABLE_DA_RESPONSE_DATA);
                            if (dbTable != null) {
                                pkFields = ArrayUtil.toStringArray(dbTable.getPrimaryKeyFields());
                            }
                        }
                        if (pkFields != null) {
                            for (SerializedDBDataObject serializedResponseData : serializedResponseDataList) {
                                String pem = serializedResponseData.getAttributeValue(FIELD_DRD_PEM, true, project);
                                iPartsResponseDataId responseDataId = new iPartsResponseDataId(serializedResponseData.getPkValuesForPkFields(pkFields));
                                iPartsDataResponseData responseData = new iPartsDataResponseData(project, responseDataId);

                                switch (serializedResponseData.getState()) {
                                    case NEW: // Fallthrough beabsichtigt
                                    case REPLACED: // Fallthrough beabsichtigt
                                    case MODIFIED:
                                        responseData.__internal_setNew(serializedResponseData.getState() == SerializedDBDataObjectState.NEW);
                                        if (responseData.existsInDB()) {
                                            iPartsResponseDataId oldResponseDataId = new iPartsResponseDataId(serializedResponseData.internalGetOriginPkValues(pkFields));
                                            responseData.__internal_setOldId(oldResponseDataId);
                                            responseData.setDeleteOldId(serializedResponseData.isDeleteOldId());
                                            result.updateCacheForPEM(pem, responseData);
                                        }
                                        break;
                                    case DELETED:
                                        result.deleteCacheForPEM(pem, responseData);
                                        break;
                                }
                            }
                        }
                    }
                }
            } else {
                // Noch nicht geladen -> lade aus der Datenbank
                result.load(project);
            }
            cache.put(hashObject, result);
        }

        return result;
    }

    /**
     * Verwendet den Cache {@code sourceData} auch als Cache {@code destData} (also dieselben Cache-Daten).
     *
     * @param sourceData
     * @param destData
     * @see #reuseCache(iPartsResponseData, iPartsResponseData)
     */
    private static synchronized void referenceCache(iPartsResponseData sourceData, iPartsResponseData destData) {
        // Cache-Daten von der Quelle verwenden
        destData.responseDataByPEM = sourceData.responseDataByPEM;
        destData.sourceCache = sourceData;
    }

    /**
     * Verwendet den Inhalt vom Cache {@code sourceData} im Cache {@code destData} in einer eigenen Datenstruktur (ohne
     * die einzelnen Rückmeldedaten zu klonen).
     *
     * @param sourceData
     * @param destData
     * @see #referenceCache(iPartsResponseData, iPartsResponseData)
     */
    private static synchronized void reuseCache(iPartsResponseData sourceData, iPartsResponseData destData) {
        // Cache-Daten von der Quelle in einer eigenen Map ablegen
        destData.responseDataByPEM = new HashMap<>();
        for (Map.Entry<String, List<iPartsDataResponseData>> responseDataEntry : sourceData.responseDataByPEM.entrySet()) {
            List<iPartsDataResponseData> sourceResponseDataList = responseDataEntry.getValue();
            List<iPartsDataResponseData> destResponseDataList = new DwList<>(sourceResponseDataList);
            destData.responseDataByPEM.put(responseDataEntry.getKey(), destResponseDataList);
        }
    }

    /**
     * Verwendet den Cache der aktuell aktiven {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s auch als Cache,
     * der mit {@code destinationCacheKey} referenziert wird.
     *
     * @param project
     * @param destinationCacheKey
     */
    public static synchronized void referenceActiveChangeSetCache(EtkProject project, String destinationCacheKey) {
        // Ziel-Cache bestimmen
        ObjectInstanceStrongLRUList<Object, iPartsResponseData> destCache = cacheWithChangeSets.getCacheForChangeSetsInstance(destinationCacheKey);
        Object hashObject = getInstanceHashObject(project);
        iPartsResponseData destData = destCache.get(hashObject);
        if (destData == null) {
            destData = new iPartsResponseData();
            destCache.put(hashObject, destData);
        }

        referenceCache(getInstance(project), destData);
    }

    /**
     * Siehe {@link AbstractCacheWithChangeSets#moveActiveChangeSetCache(EtkProject, String)}.
     *
     * @param project
     * @param destinationCacheKey
     */
    public static synchronized void moveActiveChangeSetCache(EtkProject project, String destinationCacheKey) {
        cacheWithChangeSets.moveActiveChangeSetCache(project, destinationCacheKey);
    }


    @Override
    public void fillCacheData(SetCacheDataEvent setCacheDataEvent) {
        // Daten in simplere Datenstrukturen konvertieren für die Serialisierung
        responseDataByPEMasStringList = getResponseDataByPEMasStringList();
        CacheForGetCacheDataEvent.super.fillCacheData(setCacheDataEvent);
        responseDataByPEMasStringList = null;
    }

    @Override
    public iPartsResponseData createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        iPartsResponseData responseData = createInstance(setCacheDataEvent, cacheWithChangeSets.getNormalCache(), getInstanceHashObject(project));

        // Daten aus simpleren Datenstrukturen wiederherstellen für die Deserialisierung
        responseData.setResponseDataByPEMasStringList(responseData.responseDataByPEMasStringList);
        responseData.responseDataByPEMasStringList = null;
        return responseData;
    }

    /**
     * Liefert die Rückmeldedaten als einfache Datenstrukturen zurück für die Serialisierung.
     *
     * @return
     */
    private Map<String, List<List<String>>> getResponseDataByPEMasStringList() {
        // iPartsDataResponseData in simple Listen von String-Werten konvertieren für die schnelle Übertragung per IAC
        Set<String> fieldNamesNoBlob = iPartsPlugin.getMqProject().getDB().getExistingFieldNamesWithoutBlobs(TABLE_DA_RESPONSE_DATA);
        Map<String, List<List<String>>> result = new HashMap<>();
        for (Map.Entry<String, List<iPartsDataResponseData>> responseDataEntry : responseDataByPEM.entrySet()) {
            List<List<String>> responseDataList = new ArrayList<>(responseDataEntry.getValue().size());
            for (iPartsDataResponseData responseData : responseDataEntry.getValue()) {
                responseDataList.add(responseData.getAttributesAsStringValueList(fieldNamesNoBlob));
            }
            result.put(responseDataEntry.getKey(), responseDataList);
        }
        return result;
    }

    /**
     * Setzt die Rückmeldedaten aufgrund der übergebenen einfachen Datenstrukturen durch Deserialisierung.
     *
     * @param responseDataByPEM
     */
    private void setResponseDataByPEMasStringList(Map<String, List<List<String>>> responseDataByPEM) {
        // iPartsDataResponseData aus den simplen Listen von String-Werten erzeugen
        EtkProject project = iPartsPlugin.getMqProject();
        Set<String> fieldNamesNoBlob = project.getDB().getExistingFieldNamesWithoutBlobs(TABLE_DA_RESPONSE_DATA);
        Map<String, List<iPartsDataResponseData>> result = new HashMap<>();
        for (Map.Entry<String, List<List<String>>> responseDataEntry : responseDataByPEM.entrySet()) {
            List<iPartsDataResponseData> responseDataList = new ArrayList<>(responseDataEntry.getValue().size());
            for (List<String> responseDataValues : responseDataEntry.getValue()) {
                iPartsDataResponseData responseData = new iPartsDataResponseData(project, null);
                responseData.setAttributesByStringValueList(fieldNamesNoBlob, responseDataValues);
                responseDataList.add(responseData);
            }
            result.put(responseDataEntry.getKey(), responseDataList);
        }
        this.responseDataByPEM = result;
    }

    private void load(EtkProject project) {
        iPartsDataResponseDataList dataResponseDataList = new iPartsDataResponseDataList();
        dataResponseDataList.load(project);
        for (iPartsDataResponseData dataResponseData : dataResponseDataList) {
            String pem = dataResponseData.getAsId().getPem().toUpperCase();
            List<iPartsDataResponseData> responseDataList = responseDataByPEM.get(pem);
            if (responseDataList == null) {
                responseDataList = new DwList<>(1);
                responseDataByPEM.put(pem, responseDataList);
            }
            responseDataList.add(dataResponseData);
        }
    }

    /**
     * Verwendet den Inhalt vom gemerkten Quell-Cache in diesem Cache in einer eigenen Datenstruktur (ohne die einzelnen
     * Rückmeldedaten zu klonen).
     *
     * @see #reuseCache(iPartsResponseData, iPartsResponseData)
     */
    private synchronized void reuseSourceCache() {
        if (sourceCache != null) {
            reuseCache(sourceCache, this);
            sourceCache = null; // Verwendung vom sourceCache darf nur einmal stattfinden
        }
    }

    public synchronized List<iPartsDataResponseData> getResponseData(String pem) {
        return responseDataByPEM.get(pem.toUpperCase());
    }

    /**
     * Falls es im Cache zur {@code pem} schon einen Eintrag für {@code responseData} gibt, wird dieser aktualisiert,
     * sonst wird {@code responseData} neu hinzugefügt.
     *
     * @param pem
     * @param responseData Neue oder zu aktualisierende Rückmeldedaten
     */
    public synchronized void updateCacheForPEM(String pem, iPartsDataResponseData responseData) {
        reuseSourceCache(); // Bei Veränderungen muss eine eigene Datenstruktur verwendet werden

        pem = pem.toUpperCase();
        List<iPartsDataResponseData> cacheData = getResponseData(pem);
        if (cacheData == null) {
            cacheData = new DwList<>(1);
            responseDataByPEM.put(pem, cacheData);
        }

        // Beim Warmup werden die Daten im Cache mit dem MQProject erzeugt. Für den Fall dass später nochmal Daten
        // nachgeladen werden müssten (was bei Rückmeldedaten aktuell nicht der Fall ist) sollte hier für den Klon
        // das gleiche Projekt verwendet werden
        responseData = responseData.cloneMe(iPartsPlugin.getMqProject());

        // Alte ID merken und danach zurücksetzen
        IdWithType id = responseData.getAsId();
        IdWithType oldId = responseData.getOldId();
        boolean idChanged = responseData.isIdChanged();
        responseData.updateOldId();

        boolean idFound = false;
        boolean oldIdFound = !idChanged;

        // Rückmeldedaten aktualisieren
        for (int i = 0; i < cacheData.size(); i++) {
            iPartsResponseDataId cacheDataId = cacheData.get(i).getAsId();
            if (cacheDataId.equals(id)) {
                cacheData.set(i, responseData);
                idFound = true;
            } else if (idChanged && cacheDataId.equals(oldId)) {
                cacheData.remove(i);
                i--; // Index korrigieren
                oldIdFound = true;
            }

            if (idFound && oldIdFound) { // Alles gefunden
                return;
            }
        }

        // Rückmeldedaten hinzufügen falls nicht gefunden oder bei Primärschlüsseländerungen; auch kann es sein, dass oldId
        // nicht gefunden wird -> dann würden wir die responseData nochmals hinzufügen, weil oben kein return gemacht wird
        if (!idFound) {
            cacheData.add(responseData);
        }
    }

    /**
     * Den übergebenen Eintrag im Cache suchen und falls gefunden löschen.
     *
     * @param pem
     * @param responseData
     */
    public synchronized void deleteCacheForPEM(String pem, iPartsDataResponseData responseData) {
        reuseSourceCache(); // Bei Veränderungen muss eine eigene Datenstruktur verwendet werden

        pem = pem.toUpperCase();
        List<iPartsDataResponseData> cacheData = getResponseData(pem);
        if (cacheData != null) {
            for (iPartsDataResponseData cachedResponseData : cacheData) {
                if (cachedResponseData.getAsId().equals(responseData.getAsId())) {
                    cacheData.remove(cachedResponseData);
                    break;
                }
            }
        }
    }
}
