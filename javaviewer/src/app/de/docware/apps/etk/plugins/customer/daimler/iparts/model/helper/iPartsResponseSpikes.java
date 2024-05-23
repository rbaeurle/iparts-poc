/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpike;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataResponseSpikeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsResponseSpikeId;
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
 * Cache für alle Ausreißer (Vorläufer/Nachzügler) aus der Tabelle DA_RESPONSE_SPIKES.
 */
public class iPartsResponseSpikes implements CacheForGetCacheDataEvent<iPartsResponseSpikes>, iPartsConst {

    // PEM -> Ident -> Set von Ausreißern
    protected Map<String, Map<String, Set<iPartsDataResponseSpike>>> responseSpikesByPEM = new HashMap<>();
    protected iPartsResponseSpikes sourceCache;
    @JsonProperty
    private Map<String, Map<String, Set<List<String>>>> responseSpikesByPEMasStringList; // Nur für die JSON-Serialisierung

    // Lebensdauer für die ChangeSets ist iPartsPlugin.getCachesLifeTime(); Lebensdauer im Cache aber MAX_CACHE_LIFE_TIME_CORE
    private static AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsResponseSpikes>> cacheWithChangeSets =
            new AbstractCacheWithChangeSets<ObjectInstanceStrongLRUList<Object, iPartsResponseSpikes>>(MAX_CACHE_SIZE_CHANGE_SETS, iPartsPlugin.getCachesLifeTime()) {
                @Override
                protected ObjectInstanceStrongLRUList<Object, iPartsResponseSpikes> createNewCache() {
                    return new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE, MAX_CACHE_LIFE_TIME_CORE);
                }
            };

    private static Comparator<iPartsDataResponseSpike> responseSpikesComparator = new Comparator<iPartsDataResponseSpike>() {
        @Override
        public int compare(iPartsDataResponseSpike o1, iPartsDataResponseSpike o2) {
            iPartsResponseSpikeId o1Id = o1.getAsId();
            iPartsResponseSpikeId o2Id = o2.getAsId();
            int identCompare = o1Id.getSpikeIdent().compareTo(o2Id.getSpikeIdent());
            // Zuerst nach Ident sortieren
            if (identCompare == 0) {
                // bei gleichem Ident zusätzlich nach ADAT
                return o1Id.getAdatAttribute().compareTo(o2Id.getAdatAttribute());
            }
            return identCompare;
        }
    };

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        cacheWithChangeSets.clearCache();
    }

    /**
     * Entfernt den Cache-Eintrag für die gerade aktiven {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s.
     *
     * @param project
     */
    public static synchronized void removeCacheForActiveChangeSets(EtkProject project) {
        cacheWithChangeSets.removeCacheForActiveChangeSets(project);
    }

    private static String getInstanceHashObject(EtkProject project) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsResponseSpikes.class, "ResponseSpikes", false);
    }

    public static synchronized iPartsResponseSpikes getInstance(final EtkProject project) {
        ObjectInstanceStrongLRUList<Object, iPartsResponseSpikes> cache = cacheWithChangeSets.getCacheInstance(project);

        // Hier nun keine ChangeSets mehr verwenden für das hashObject, weil dies ja bereits über cacheWithChangeSets
        // gelöst wurde
        Object hashObject = getInstanceHashObject(project);
        iPartsResponseSpikes result = cache.get(hashObject);

        if (result == null) {
            if (cache == cacheWithChangeSets.getNormalCache()) {
                result = iPartsPlugin.createCacheInstanceWithCachesProvider(new iPartsResponseSpikes(), null, cache, hashObject);
                if (result != null) {
                    return result;
                }
            }

            result = new iPartsResponseSpikes();

            if (project.getEtkDbs().isRevisionChangeSetActive()) {
                // Den normalen Cache zunächst laden falls notwendig
                iPartsResponseSpikes normalCacheData = cacheWithChangeSets.getNormalCache().get(hashObject);
                if (normalCacheData == null) {
                    normalCacheData = new iPartsResponseSpikes();
                    final iPartsResponseSpikes normalCacheDataFinal = normalCacheData;

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
                    Collection<SerializedDBDataObject> serializedResponseSpikesList = changeSet.getSerializedObjectsByTable(TABLE_DA_RESPONSE_SPIKES);
                    if (serializedResponseSpikesList != null) {
                        if (pkFields == null) { // Primärschlüsselfelder nur einmal bestimmen
                            EtkDatabaseTable dbTable = project.getConfig().getDBDescription().findTable(TABLE_DA_RESPONSE_SPIKES);
                            if (dbTable != null) {
                                pkFields = ArrayUtil.toStringArray(dbTable.getPrimaryKeyFields());
                            }
                        }
                        if (pkFields != null) {
                            for (SerializedDBDataObject serializedResponseSpike : serializedResponseSpikesList) {
                                String pem = serializedResponseSpike.getAttributeValue(FIELD_DRS_PEM, true, project);
                                String ident = serializedResponseSpike.getAttributeValue(FIELD_DRS_IDENT, true, project);
                                iPartsResponseSpikeId responseSpikeId = new iPartsResponseSpikeId(serializedResponseSpike.getPkValuesForPkFields(pkFields));
                                iPartsDataResponseSpike responseSpike = new iPartsDataResponseSpike(project, responseSpikeId);

                                switch (serializedResponseSpike.getState()) {
                                    case NEW: // Fallthrough beabsichtigt
                                    case REPLACED: // Fallthrough beabsichtigt
                                    case MODIFIED:
                                        responseSpike.__internal_setNew(serializedResponseSpike.getState() == SerializedDBDataObjectState.NEW);
                                        if (responseSpike.existsInDB()) {
                                            iPartsResponseSpikeId oldResponseSpikeId = new iPartsResponseSpikeId(serializedResponseSpike.internalGetOriginPkValues(pkFields));
                                            responseSpike.__internal_setOldId(oldResponseSpikeId);
                                            responseSpike.setDeleteOldId(serializedResponseSpike.isDeleteOldId());
                                            result.updateCacheForPEMAndIdent(pem, ident, responseSpike);
                                        }
                                        break;
                                    case DELETED:
                                        result.deleteCacheForPEMAndIdent(pem, ident, responseSpike);
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
     * @see #reuseCache(iPartsResponseSpikes, iPartsResponseSpikes)
     */
    private static synchronized void referenceCache(iPartsResponseSpikes sourceData, iPartsResponseSpikes destData) {
        // Cache-Daten von der Quelle verwenden
        destData.responseSpikesByPEM = sourceData.responseSpikesByPEM;
        destData.sourceCache = sourceData;
    }

    /**
     * Verwendet den Inhalt vom Cache {@code sourceData} im Cache {@code destData} in einer eigenen Datenstruktur (ohne
     * die einzelnen Ausreißer zu klonen).
     *
     * @param sourceData
     * @param destData
     * @see #referenceCache(iPartsResponseSpikes, iPartsResponseSpikes)
     */
    private static synchronized void reuseCache(iPartsResponseSpikes sourceData, iPartsResponseSpikes destData) {
        // Cache-Daten von der Quelle in einer eigenen Map ablegen
        destData.responseSpikesByPEM = new HashMap<>();
        for (Map.Entry<String, Map<String, Set<iPartsDataResponseSpike>>> responseSpikesEntry : sourceData.responseSpikesByPEM.entrySet()) {
            Map<String, Set<iPartsDataResponseSpike>> sourceResponseSpikesMap = responseSpikesEntry.getValue();
            Map<String, Set<iPartsDataResponseSpike>> destResponseSpikesMap = new HashMap<>(sourceResponseSpikesMap.size());
            for (Map.Entry<String, Set<iPartsDataResponseSpike>> sourceResponseSpikesEntry : sourceResponseSpikesMap.entrySet()) {
                Set<iPartsDataResponseSpike> sourceResponseSpikesSet = sourceResponseSpikesEntry.getValue();
                Set<iPartsDataResponseSpike> destResponseSpikesSet = new TreeSet<>(responseSpikesComparator);
                destResponseSpikesSet.addAll(sourceResponseSpikesSet);
                destResponseSpikesMap.put(sourceResponseSpikesEntry.getKey(), destResponseSpikesSet);
            }
            destData.responseSpikesByPEM.put(responseSpikesEntry.getKey(), destResponseSpikesMap);
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
        ObjectInstanceStrongLRUList<Object, iPartsResponseSpikes> destCache = cacheWithChangeSets.getCacheForChangeSetsInstance(destinationCacheKey);
        Object hashObject = getInstanceHashObject(project);
        iPartsResponseSpikes destData = destCache.get(hashObject);
        if (destData == null) {
            destData = new iPartsResponseSpikes();
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

    /**
     * Klont die einzelnen Ausreißer aus der übergebenen Collection
     *
     * @param project
     * @param originalResponseSpikes
     * @return
     */
    public static Set<iPartsDataResponseSpike> cloneResponseSpikes(EtkProject project, Collection<iPartsDataResponseSpike> originalResponseSpikes) {
        Set<iPartsDataResponseSpike> clonedResponseSpikes = new TreeSet<>(iPartsResponseSpikes.responseSpikesComparator);
        for (iPartsDataResponseSpike responseSpike : originalResponseSpikes) {
            clonedResponseSpikes.add(responseSpike.cloneMe(project));
        }
        return clonedResponseSpikes;
    }


    @Override
    public void fillCacheData(SetCacheDataEvent setCacheDataEvent) {
        // Daten in simplere Datenstrukturen konvertieren für die Serialisierung
        responseSpikesByPEMasStringList = getResponseSpikesByPEMasStringList();
        CacheForGetCacheDataEvent.super.fillCacheData(setCacheDataEvent);
        responseSpikesByPEMasStringList = null;
    }

    @Override
    public iPartsResponseSpikes createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        iPartsResponseSpikes responseSpikes = createInstance(setCacheDataEvent, cacheWithChangeSets.getNormalCache(), getInstanceHashObject(project));

        // Daten aus simpleren Datenstrukturen wiederherstellen für die Deserialisierung
        responseSpikes.setResponseSpikesByPEMasStringList(responseSpikes.responseSpikesByPEMasStringList);
        responseSpikes.responseSpikesByPEMasStringList = null;
        return responseSpikes;

    }

    /**
     * Liefert die Ausreißer als einfache Datenstrukturen zurück für die Serialisierung.
     *
     * @return
     */
    private Map<String, Map<String, Set<List<String>>>> getResponseSpikesByPEMasStringList() {
        // iPartsDataResponseSpikes in simple Listen von String-Werten konvertieren für die schnelle Übertragung per IAC
        Set<String> fieldNamesNoBlob = iPartsPlugin.getMqProject().getDB().getExistingFieldNamesWithoutBlobs(TABLE_DA_RESPONSE_SPIKES);
        Map<String, Map<String, Set<List<String>>>> result = new HashMap<>(); // PEM -> Ident -> Set von Liste von String-Werten der Ausreißer
        for (Map.Entry<String, Map<String, Set<iPartsDataResponseSpike>>> responseSpikesEntry : responseSpikesByPEM.entrySet()) {
            Map<String, Set<List<String>>> responseSpikesEntryMap = new HashMap<>();
            for (Map.Entry<String, Set<iPartsDataResponseSpike>> responseSpikesForIdentEntry : responseSpikesEntry.getValue().entrySet()) {
                Set<List<String>> responseSpikesList = new LinkedHashSet<>();
                for (iPartsDataResponseSpike responseSpikes : responseSpikesForIdentEntry.getValue()) {
                    responseSpikesList.add(responseSpikes.getAttributesAsStringValueList(fieldNamesNoBlob));
                }
                responseSpikesEntryMap.put(responseSpikesForIdentEntry.getKey(), responseSpikesList);
            }
            result.put(responseSpikesEntry.getKey(), responseSpikesEntryMap);
        }
        return result;
    }

    /**
     * Setzt die Ausreißer aufgrund der übergebenen einfachen Datenstrukturen durch Deserialisierung.
     *
     * @param responseSpikesByPEM
     */
    private void setResponseSpikesByPEMasStringList(Map<String, Map<String, Set<List<String>>>> responseSpikesByPEM) {
        // iPartsDataResponseSpikes aus den simplen Listen von String-Werten erzeugen
        EtkProject project = iPartsPlugin.getMqProject();
        Set<String> fieldNamesNoBlob = project.getDB().getExistingFieldNamesWithoutBlobs(TABLE_DA_RESPONSE_SPIKES);
        Map<String, Map<String, Set<iPartsDataResponseSpike>>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Set<List<String>>>> responseSpikesEntry : responseSpikesByPEM.entrySet()) {
            Map<String, Set<iPartsDataResponseSpike>> responseSpikesEntryMap = new HashMap<>();
            for (Map.Entry<String, Set<List<String>>> responseSpikesForIdentEntry : responseSpikesEntry.getValue().entrySet()) {
                Set<iPartsDataResponseSpike> responseSpikesList = new TreeSet<>(responseSpikesComparator);
                for (List<String> responseSpikesValues : responseSpikesForIdentEntry.getValue()) {
                    iPartsDataResponseSpike responseSpikes = new iPartsDataResponseSpike(project, null);
                    responseSpikes.setAttributesByStringValueList(fieldNamesNoBlob, responseSpikesValues);
                    responseSpikesList.add(responseSpikes);
                }
                responseSpikesEntryMap.put(responseSpikesEntry.getKey(), responseSpikesList);
            }
            result.put(responseSpikesEntry.getKey(), responseSpikesEntryMap);
        }
        this.responseSpikesByPEM = result;
    }

    private void load(EtkProject project) {
        iPartsDataResponseSpikeList dataResponseSpikeList = new iPartsDataResponseSpikeList();
        dataResponseSpikeList.load(project);
        for (iPartsDataResponseSpike dataResponseSpike : dataResponseSpikeList) {
            String pem = dataResponseSpike.getAsId().getPem().toUpperCase();
            Map<String, Set<iPartsDataResponseSpike>> responseSpikesByIdent = responseSpikesByPEM.get(pem);
            if (responseSpikesByIdent == null) {
                responseSpikesByIdent = new HashMap<>();
                responseSpikesByPEM.put(pem, responseSpikesByIdent);
            }

            String ident = dataResponseSpike.getAsId().getIdent().toUpperCase();
            Set<iPartsDataResponseSpike> responseSpikesForIdent = responseSpikesByIdent.get(ident);
            if (responseSpikesForIdent == null) {
                responseSpikesForIdent = new TreeSet<>(responseSpikesComparator);
                responseSpikesByIdent.put(ident, responseSpikesForIdent);
            }
            responseSpikesForIdent.add(dataResponseSpike);
        }
    }

    /**
     * Verwendet den Inhalt vom gemerkten Quell-Cache in diesem Cache in einer eigenen Datenstruktur (ohne die einzelnen
     * Ausreißer zu klonen).
     *
     * @see #reuseCache(iPartsResponseSpikes, iPartsResponseSpikes)
     */
    private synchronized void reuseSourceCache() {
        if (sourceCache != null) {
            reuseCache(sourceCache, this);
            sourceCache = null; // Verwendung vom sourceCache darf nur einmal stattfinden
        }
    }

    public synchronized Set<iPartsDataResponseSpike> getResponseSpikes(String pem, String ident) {
        Map<String, Set<iPartsDataResponseSpike>> responseSpikesByIdent = responseSpikesByPEM.get(pem.toUpperCase());
        if (responseSpikesByIdent != null) {
            return responseSpikesByIdent.get(ident.toUpperCase());
        } else {
            return null;
        }
    }

    /**
     * Falls es im Cache zur {@code pem} und {@code ident} schon einen Eintrag für {@code responseSpike} gibt, wird dieser
     * aktualisiert, sonst wird {@code responseSpike} neu hinzugefügt.
     *
     * @param pem
     * @param ident
     * @param responseSpike Neue oder zu aktualisierende Ausreißer
     */
    public synchronized void updateCacheForPEMAndIdent(String pem, String ident, iPartsDataResponseSpike responseSpike) {
        reuseSourceCache(); // Bei Veränderungen muss eine eigene Datenstruktur verwendet werden

        pem = pem.toUpperCase();
        ident = ident.toUpperCase();
        Map<String, Set<iPartsDataResponseSpike>> responseSpikesByIdent = responseSpikesByPEM.get(pem);
        if (responseSpikesByIdent == null) {
            responseSpikesByIdent = new HashMap<>();
            responseSpikesByPEM.put(pem, responseSpikesByIdent);
        }

        Set<iPartsDataResponseSpike> cacheData = responseSpikesByIdent.get(ident);
        if (cacheData == null) {
            cacheData = new TreeSet<>(responseSpikesComparator);
            responseSpikesByIdent.put(ident, cacheData);
        }

        // Beim Warmup werden die Daten im Cache mit dem MQProject erzeugt. Für den Fall dass später nochmal Daten
        // nachgeladen werden müssten (was bei Rückmeldedaten aktuell nicht der Fall ist) sollte hier für den Klon
        // das gleiche Projekt verwendet werden
        responseSpike = responseSpike.cloneMe(iPartsPlugin.getMqProject());

        // Alte ID merken und danach zurücksetzen
        IdWithType id = responseSpike.getAsId();
        IdWithType oldId = responseSpike.getOldId();
        boolean idChanged = responseSpike.isIdChanged();
        responseSpike.updateOldId();

        boolean idFound = false;
        boolean oldIdFound = !idChanged;

        // Ausreißer aktualisieren (temporär über eine Liste gehen, um Einträge auch ersetzen zu können)
        List<iPartsDataResponseSpike> responseSpikeList = new DwList<>(cacheData);
        for (int i = 0; i < responseSpikeList.size(); i++) {
            iPartsResponseSpikeId cacheDataId = responseSpikeList.get(i).getAsId();
            if (cacheDataId.equals(id)) {
                responseSpikeList.set(i, responseSpike);
                idFound = true;
            } else if (idChanged && cacheDataId.equals(oldId)) {
                responseSpikeList.remove(i);
                i--; // Index korrigieren
                oldIdFound = true;
            }

            if (idFound && oldIdFound) { // Alles gefunden
                cacheData.clear();
                cacheData.addAll(responseSpikeList);
                return;
            }
        }

        if (idFound || (idChanged && oldIdFound)) {
            cacheData.clear();
            cacheData.addAll(responseSpikeList);
        }

        // Ausreißer hinzufügen falls nicht gefunden oder bei Primärschlüsseländerungen; auch kann es sein, dass oldId
        // nicht gefunden wird -> dann würden wir die responseSpike nochmals hinzufügen, weil oben kein return gemacht wird
        if (!idFound) {
            cacheData.add(responseSpike);
        }
    }

    /**
     * Den übergebenen Eintrag im Cache suchen und falls gefunden löschen.
     *
     * @param pem
     * @param ident
     * @param responseSpike
     */
    public synchronized void deleteCacheForPEMAndIdent(String pem, String ident, iPartsDataResponseSpike responseSpike) {
        reuseSourceCache(); // Bei Veränderungen muss eine eigene Datenstruktur verwendet werden

        pem = pem.toUpperCase();
        ident = ident.toUpperCase();
        Set<iPartsDataResponseSpike> cacheData = getResponseSpikes(pem, ident);
        if (cacheData != null) {
            for (iPartsDataResponseSpike cachedResponseSpike : cacheData) {
                if (cachedResponseSpike.getAsId().equals(responseSpike.getAsId())) {
                    cacheData.remove(cachedResponseSpike);
                    break;
                }
            }
        }
    }
}