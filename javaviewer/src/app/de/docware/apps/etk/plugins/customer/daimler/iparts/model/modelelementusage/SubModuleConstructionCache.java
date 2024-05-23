/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.modelelementusage;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSubModuleCategory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSubModuleCategoryList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache für alle Sub-Module aus TruckBOM.foundation für die EDS/BCS Konstruktion.
 */
public class SubModuleConstructionCache {

    private static ObjectInstanceStrongLRUList<Object, SubModuleConstructionCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static synchronized SubModuleConstructionCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), SubModuleConstructionCache.class, "SubModules", false);
        SubModuleConstructionCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new SubModuleConstructionCache();
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

    private Map<String, iPartsDataSubModuleCategory> subModulesAsMap;

    private SubModuleConstructionCache() {
    }

    private void load(EtkProject project) {
        iPartsDataSubModuleCategoryList list = iPartsDataSubModuleCategoryList.loadAllSubModuleCategories(project);
        subModulesAsMap = new HashMap<>(list.size());
        for (iPartsDataSubModuleCategory subModuleObject : list) {
            subModulesAsMap.put(subModuleObject.getAsId().getSubModuleCategory(), subModuleObject);
        }
    }

    public EtkMultiSprache getDescriptionForSubModule(String subModule) {
        iPartsDataSubModuleCategory subModuleObject = subModulesAsMap.get(subModule);
        if (subModuleObject != null) {
            return subModuleObject.getDescription();
        }
        return null;
    }

    public String getPictureNameForSubModule(String subModule) {
        iPartsDataSubModuleCategory subModuleObject = subModulesAsMap.get(subModule);
        if (subModuleObject != null) {
            return subModuleObject.getPictureName();
        }
        return "";
    }
}
