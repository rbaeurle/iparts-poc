/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVehicleDatacardCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVehicleDatacardCodeList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashSet;
import java.util.Set;

/**
 * Cache für spezielle Fahrzeugdatenkarten-Code, die bei der Anreicherung von Motordatenkarten geprüft und übernommen
 * werden sollen
 */
public class iPartsVehicleToAggregateCodeCache {

    private static ObjectInstanceStrongLRUList<Object, iPartsVehicleToAggregateCodeCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static synchronized iPartsVehicleToAggregateCodeCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsVehicleToAggregateCodeCache.class, "VehicleToAggregateCodeCache", false);
        iPartsVehicleToAggregateCodeCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsVehicleToAggregateCodeCache();
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

    private Set<String> vehicleCodes;

    private void load(EtkProject project) {
        vehicleCodes = new HashSet<>();
        iPartsDataVehicleDatacardCodeList vehicleCodesFromDB = iPartsDataVehicleDatacardCodeList.loadAllVehicleCodes(project);
        for (iPartsDataVehicleDatacardCode vehicleCode : vehicleCodesFromDB) {
            vehicleCodes.add(vehicleCode.getAsId().getVehicleCode());
        }
    }

    public boolean isVehicleToAggregateCode(String code) {
        return vehicleCodes.contains(code);
    }

}
