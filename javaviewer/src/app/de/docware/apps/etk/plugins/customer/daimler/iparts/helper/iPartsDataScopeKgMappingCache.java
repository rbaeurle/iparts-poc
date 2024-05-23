/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataScopeKgMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataScopeKgMappingList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.Map;
import java.util.Set;

/**
 * Cache für das Mapping von Scope-ID zu KGs.
 * Für jede Scope-ID kann es mehrere KGs geben. Diese werden sortiert gespeichert.
 * Cortex liefert für die EDS-Umfänge nicht mehr die KG, sondern den neuen Wert "ScopeID" auf EDS/BCS.
 */
public class iPartsDataScopeKgMappingCache implements iPartsConst {

    private static final ObjectInstanceStrongLRUList<Object, iPartsDataScopeKgMappingCache> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    // Das ist die Liste für den Cache <ScopeId>, <Set<KG>>
    private Map<String, Set<String>> kgsPerScopeMap;

    public static synchronized iPartsDataScopeKgMappingCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataScopeKgMapping.class,
                                                             "iPartsDataScopeKgMappingCache", false);
        iPartsDataScopeKgMappingCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsDataScopeKgMappingCache();
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
     * Ermittelt zur ScopeID die eine relevante KG über das gecachte Mapping. Falls er mehrere KGs im Mapping gibt, wird nur die erste,
     * kleinste KG verwendet. Falls es keine KG zur ScopeID im Mapping gibt, oder die übergebene ScopeID leer ist, wird ein leerer String zurückgeliefert.
     *
     * @param project
     * @param scopeID
     * @return
     */
    public static String getKGForScopeId(EtkProject project, String scopeID) {
        String kg = "";
        if (StrUtils.isValid(scopeID)) {
            Set<String> kGsForScopeId = iPartsDataScopeKgMappingCache.getInstance(project).getKGsForScopeId(scopeID);
            if ((kGsForScopeId != null) && !kGsForScopeId.isEmpty()) {
                kg = kGsForScopeId.iterator().next();
            }
        }
        return kg;
    }

    /**
     * Den Cache mit Daten füllen.
     *
     * @param project
     */
    private void load(EtkProject project) {
        kgsPerScopeMap = iPartsDataScopeKgMappingList.loadCompleteMappingAsMap(project);
    }

    /**
     * Alle KGs zur ScopeID in aufsteigender Reihenfolge
     *
     * @param scopeId
     * @return
     */
    public Set<String> getKGsForScopeId(String scopeId) {
        return kgsPerScopeMap.get(scopeId);
    }
}
