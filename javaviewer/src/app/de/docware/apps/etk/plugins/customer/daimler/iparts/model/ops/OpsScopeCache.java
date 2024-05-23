/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataOPSScope;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataOPSScopeList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache für alle OPS Umfänge.
 */
public class OpsScopeCache {

    private static ObjectInstanceStrongLRUList<Object, OpsScopeCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static synchronized OpsScopeCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), OpsScopeCache.class, "OPSScope", false);
        OpsScopeCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new OpsScopeCache();
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

    private Map<String, iPartsDataOPSScope> scopesAsMap;

    private OpsScopeCache() {

    }

    private void load(EtkProject project) {
        iPartsDataOPSScopeList list = iPartsDataOPSScopeList.loadAllOPSScopes(project);
        scopesAsMap = new HashMap<String, iPartsDataOPSScope>(list.size());
        for (iPartsDataOPSScope scopeDBObject : list) {
            scopesAsMap.put(scopeDBObject.getAsId().getScope(), scopeDBObject);
        }
    }

    public EtkMultiSprache getDescriptionForScope(String scope) {
        iPartsDataOPSScope scopeDBObject = scopesAsMap.get(scope);
        if (scopeDBObject != null) {
            return scopeDBObject.getDescription();
        }
        return null;
    }

    public String getPictureNameForScope(String scope) {
        iPartsDataOPSScope scopeDBObject = scopesAsMap.get(scope);
        if (scopeDBObject != null) {
            return scopeDBObject.getPictureName();
        }
        return "";
    }
}
