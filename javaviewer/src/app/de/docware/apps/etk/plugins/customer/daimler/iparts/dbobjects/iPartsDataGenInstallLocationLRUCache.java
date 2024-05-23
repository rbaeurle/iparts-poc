/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenInstallLocation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenInstallLocationList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.Collections;
import java.util.Map;

/**
 * Klasse für einen Cache der generischen Verbauorte für ({@link iPartsConst#MAX_CACHED_ERROR_AND_GEN_LOCATION_SERIES})
 * Baureihen aus der Tabelle DA_GENERIC_INSTALL_LOCATION
 */
public class iPartsDataGenInstallLocationLRUCache implements iPartsConst {

    // Das ist der eigentliche LRU-Cache, hier werden die genersichen Verbauorte zu n Baureihen gesammelt und verwaltet.
    private static ObjectInstanceLRUList<Object, iPartsDataGenInstallLocationLRUCache> instances =
            new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHED_ERROR_AND_GEN_LOCATION_SERIES, iPartsConst.MAX_CACHE_LIFE_TIME_CORE, true);

    // Map der generischen Verbauorte für eine Baureihe, die dann letztendlich über eine Instanz dieser Klasse gecacht wird.
    private Map<String, iPartsDataGenInstallLocation> genInstallLocationMap;

    public static synchronized void clearCache() {
        instances.clear();
    }

    /**
     * Holt die generischen Verbauorte zu einer Baureihe aus dem Cache oder lädt sie aus der Datenbank und packt sie in den Cache.
     *
     * @param project
     * @param seriesId
     * @return
     */
    public static synchronized iPartsDataGenInstallLocationLRUCache getInstance(EtkProject project, iPartsSeriesId seriesId) {
        // Suche nach der Baureihe im Cache
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataGenInstallLocationLRUCache.class,
                                                             seriesId.getSeriesNumber(), false);
        iPartsDataGenInstallLocationLRUCache result = instances.get(hashObject);

        // Noch nicht geladen -> lade aus der Datenbank
        if (result == null) {
            result = new iPartsDataGenInstallLocationLRUCache();
            result.load(project, seriesId);
            instances.put(hashObject, result);
        }
        return result;
    }

    /**
     * Baut eine Map aller generischen Verbauorte aus {@link iPartsDataGenInstallLocation}-Objekten zum Key für einen generischen
     * Verbauort für eine Baureihe auf.
     *
     * @param project  Das Projekt
     * @param seriesId Die Baureihe, für die der Cache benötigt wird.
     */
    private void load(EtkProject project, iPartsSeriesId seriesId) {
        genInstallLocationMap = iPartsDataGenInstallLocationList.loadAllReleasedDataForSeriesAsMap(project, seriesId);
    }

    public Map<String, iPartsDataGenInstallLocation> getGenInstallLocationMap() {
        return Collections.unmodifiableMap(genInstallLocationMap);
    }
}