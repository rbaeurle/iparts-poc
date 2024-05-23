/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAggsMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAggsMappingList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cache für die Tabelle DA_AGGS_MAPPING
 */
public class iPartsAggTypeMappingCache {

    private static final ObjectInstanceStrongLRUList<Object, iPartsAggTypeMappingCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);
    private Map<String, String> aggTypeMappingMap;

    public static synchronized iPartsAggTypeMappingCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsAggTypeMappingCache.class, "AggTypeMapping", false);
        iPartsAggTypeMappingCache result = instances.get(hashObject);

        if (result == null) {
            result = new iPartsAggTypeMappingCache();
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

    /**
     * Lädt alle {@link iPartsDataAggsMapping}s aus der DB und baut eine Map auf.
     *
     * @param project
     */
    private void load(EtkProject project) {
        // IllegalStateException: bei Schlüsselduplikaten "gewinnt" der erste. First come - first serve.
        // NullPointer ausgeschlossen, da getAsList immer neue Instanz von DwList liefert.
        aggTypeMappingMap = iPartsDataAggsMappingList.loadAllAggTypeMappings(project).getAsList()
                .stream()
                .collect(Collectors.toMap(
                        x -> x.getAsId().getDIALOGType(),
                        iPartsDataAggsMapping::getMADAggType,
                        (x, y) -> x));
    }

    /**
     * Liefert den MAD-Aggregatetyp für den übergebenen DIALOG-Aggregatetyp zurück.
     *
     * @param dialogAggType
     * @return
     */
    public String getAggTypeMapping(String dialogAggType) {
        return aggTypeMappingMap.get(dialogAggType);
    }
}