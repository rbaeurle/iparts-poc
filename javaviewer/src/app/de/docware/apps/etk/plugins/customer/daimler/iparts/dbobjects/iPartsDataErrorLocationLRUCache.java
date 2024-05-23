/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Klasse für einen Cache der Fehlerorte für ({@link iPartsConst#MAX_CACHED_ERROR_AND_GEN_LOCATION_SERIES}) Baureihen aus
 * der Tabelle DA_ERROR_LOCATION
 */
public class iPartsDataErrorLocationLRUCache implements iPartsConst {

    public static final String ERROR_LOCATION_DELIMITER = ",";

    // Das ist der eigentliche LRU-Cache, hier werden die Fehlerortlisten zu n Baureihen gesammelt und verwaltet.
    private static ObjectInstanceLRUList<Object, iPartsDataErrorLocationLRUCache> instances =
            new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHED_ERROR_AND_GEN_LOCATION_SERIES, iPartsConst.MAX_CACHE_LIFE_TIME_CORE, true);

    // Map der Fehlerorte für eine Baureihe, die dann letztendlich über eine Instanz dieser Klasse gecacht wird.
    private Map<iPartsDataErrorLocationCacheId, String> errorLocationMapping;

    /**
     * - ohne Worte -
     */
    public static synchronized void clearCache() {
        instances.clear();
    }

    /**
     * Holt die Fehlerortliste zu einer Baureihe aus dem Cache oder lädt sie aus der Datenbank und packt sie in den Cache.
     *
     * @param project
     * @param seriesId
     * @return
     */
    public static synchronized iPartsDataErrorLocationLRUCache getInstance(EtkProject project, iPartsSeriesId seriesId) {
        // Suche nach der Baureihe im Cache
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataErrorLocationLRUCache.class,
                                                             seriesId.getSeriesNumber(), false);
        iPartsDataErrorLocationLRUCache result = instances.get(hashObject);

        // Noch nicht geladen -> lade aus der Datenbank
        if (result == null) {
            result = new iPartsDataErrorLocationLRUCache();
            result.load(project, seriesId);
            instances.put(hashObject, result);
        }
        return result;
    }

    /**
     * Baut eine Map aller Fehlerorte aus {@link iPartsDataErrorLocation}-Objekten zur {@link iPartsDataErrorLocationCacheId}
     * für eine Baureihe auf.
     *
     * @param project  Das Projekt
     * @param seriesId Die Baureihe für die der Cache benötigt wird.
     */
    private void load(EtkProject project, iPartsSeriesId seriesId) {
        if (seriesId != null) {
            errorLocationMapping = new HashMap<>();
            iPartsDataErrorLocationList errorLocationList = iPartsDataErrorLocationList.loadReleasedDataForSeriesNoFromDB(project,
                                                                                                                          seriesId.getSeriesNumber(),
                                                                                                                          DBActionOrigin.FROM_DB);
            for (iPartsDataErrorLocation errorLocation : errorLocationList) {
                String damagePart = errorLocation.getFieldValue(FIELD_DEL_DAMAGE_PART).trim();

                iPartsDataErrorLocationCacheId cacheId = new iPartsDataErrorLocationCacheId(errorLocation.getAsId());
                // Wenn es den Schlüssel schon gibt, nur die aktuelle ErrorLocation zum existierenden Element hinzufügen.
                String existingDamageParts = errorLocationMapping.get(cacheId);
                if (existingDamageParts != null) {
                    // Den weiteren Fehlerort zum bisherigen Fehlerort hinzufügen
                    // Das Anhängen des Fehlerorts nur machen, wenn der aktuell zu addierende Wert NICHT LEER ist.
                    if (!damagePart.isEmpty()) {
                        // Sollte nie vorkommen, dennoch abgefangen:
                        // Falls ein leerer Fehlerort existiert hätte, den aktuellen Wert nur übernehmen.
                        if (existingDamageParts.isEmpty()) {
                            errorLocationMapping.put(cacheId, damagePart);
                        } else {
                            // Den neuen Fehlerort nur mit ',' getrennt anhängen, wenn es schon einen Wert in den ErrorLocations gibt.
                            // Doppelte Einträge vermeiden
                            List<String> damagePartsList = StrUtils.toStringList(existingDamageParts, ERROR_LOCATION_DELIMITER, false, true);
                            if (!damagePartsList.contains(damagePart)) {
                                damagePartsList.add(damagePart);

                                // Jetzt noch aufsteigend sortieren
                                Collections.sort(damagePartsList);
                                errorLocationMapping.put(cacheId, StrUtils.stringListToString(damagePartsList, ERROR_LOCATION_DELIMITER));
                            }
                        }
                    }
                } else {
                    errorLocationMapping.put(cacheId, damagePart);
                }
            }
        }
    }

    /**
     * Ermittelt zu einer Stücklistenposition die Fehlerorte
     *
     * @param bcteKey Der BCTE-Schlüssel
     * @param partNo  Die Teilenummer, für die die Fehlerorte gesucht werden.
     * @return Fehlerorte oder Leerstring
     */
    public String getErrorLocationForPartListEntry(iPartsDialogBCTEPrimaryKey bcteKey, String partNo) {
        if (bcteKey != null) {
            return getErrorLocationForPartListEntry(new iPartsDataErrorLocationCacheId(bcteKey, partNo));
        }
        return "";
    }

    /**
     * Ermittelt zu einer Stücklistenposition die Fehlerorte
     *
     * @param cacheId Der Schlüssel in den Cache, für den die Fehlerorte gesucht werden.
     * @return Fehlerorte oder Leerstring
     */
    private String getErrorLocationForPartListEntry(iPartsDataErrorLocationCacheId cacheId) {
        if ((errorLocationMapping != null) && (cacheId != null) && !cacheId.getSeriesNo().isEmpty()) {
            // Nachsehen, ob es einen Eintrag in DA_ERROR_LOCATION gibt.
            String errorLocation = errorLocationMapping.get(cacheId);
            if (errorLocation != null) {
                return errorLocation;
            }
        }
        return "";
    }
}
