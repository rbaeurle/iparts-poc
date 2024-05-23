/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataModuleCategory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataModuleCategoryList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache für alle Module aus TruckBOM.foundation für die EDS/BCS Konstruktion.
 */
public class ModuleConstructionCache {

    private static ObjectInstanceStrongLRUList<Object, ModuleConstructionCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static synchronized ModuleConstructionCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), ModuleConstructionCache.class, "Modules", false);
        ModuleConstructionCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new ModuleConstructionCache();
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

    private Map<String, iPartsDataModuleCategory> modulesAsMap;

    private ModuleConstructionCache() {
    }

    private void load(EtkProject project) {
        iPartsDataModuleCategoryList list = iPartsDataModuleCategoryList.loadAllModuleCategories(project);
        modulesAsMap = new HashMap<>(list.size());
        for (iPartsDataModuleCategory moduleObject : list) {
            modulesAsMap.put(moduleObject.getAsId().getModuleCategory(), moduleObject);
        }
    }

    public EtkMultiSprache getDescriptionForModule(String module) {
        iPartsDataModuleCategory moduleObject = modulesAsMap.get(module);
        if (moduleObject != null) {
            return moduleObject.getDescription();
        }
        return null;
    }

    public String getPictureNameForModule(String module) {
        iPartsDataModuleCategory moduleObject = modulesAsMap.get(module);
        if (moduleObject != null) {
            return moduleObject.getPictureName();
        }
        return "";
    }
}
