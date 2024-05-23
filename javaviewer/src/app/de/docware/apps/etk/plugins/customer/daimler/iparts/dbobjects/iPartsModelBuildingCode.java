/*
 * Copyright (c) 2019 Docware GmbH
 *
 * Klasse für den Cache der bm-bildende Codes aus der Tabelle [DA_MODEL_BUILDING_CODE].
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cache für die zusätzlichen BM-bildende Code von Baureihen unter Berücksichtigung der Ausführungsart in iParts.
 */
public class iPartsModelBuildingCode implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsModelBuildingCode> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    // Map der zusätzlichen BM-bildenden Code zu einer Baureihe unter Berücksichtigung der Ausführungsart
    private Map<iPartsSeriesId, Map<String, Set<String>>> seriesToModelBuildingCodeMap;

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    /**
     * Holt die Liste der zusätzlichen BM-bildenden Code für alle Baureihen aus dem Cache oder lädt sie aus der Datenbank
     * und speichert sie im Cache.
     *
     * @param project
     * @return
     */
    public static synchronized iPartsModelBuildingCode getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsModelBuildingCode.class,
                                                             "iPartsModelBuildingCode", false);
        iPartsModelBuildingCode result = instances.get(hashObject);

        if (result == null) {
            result = new iPartsModelBuildingCode();
            result.load(project);
            instances.put(hashObject, result);
        }
        return result;
    }

    /**
     * Baut eine Map aller zusätzlichen BM-bildenden Code zu allen Baureihen unter Berücksichtigung der Ausführungsart auf.
     *
     * @param project
     */
    private void load(EtkProject project) {
        seriesToModelBuildingCodeMap = new HashMap<>();
        iPartsDataModelBuildingCodeList modelBuildingCodeList = iPartsDataModelBuildingCodeList.load(project);
        for (iPartsDataModelBuildingCode dataModelBuildingCode : modelBuildingCodeList) {
            iPartsSeriesId seriesId = new iPartsSeriesId(dataModelBuildingCode.getAsId().getSeriesNo());

            // Map AA auf Set von Code
            Map<String, Set<String>> modelBuildingCodeMap = seriesToModelBuildingCodeMap.get(seriesId);
            if (modelBuildingCodeMap == null) {
                modelBuildingCodeMap = new HashMap<>();
                seriesToModelBuildingCodeMap.put(seriesId, modelBuildingCodeMap);
            }

            // Set von Code für die Baureihe und AA
            String aa = dataModelBuildingCode.getAsId().getAA();
            Set<String> modelBuildingCodeSet = modelBuildingCodeMap.get(aa);
            if (modelBuildingCodeSet == null) {
                modelBuildingCodeSet = new TreeSet<>();
                modelBuildingCodeMap.put(aa, modelBuildingCodeSet);
            }
            modelBuildingCodeSet.add(dataModelBuildingCode.getAsId().getCode());
        }
    }

    /**
     * Liefert die zur Baureihe und Ausführungsart passenden zusätzlichen BM-bildenden Code zurück.
     *
     * @param seriesId
     * @param aa
     * @return {@code null} falls keine vorhanden sind
     */
    public Set<String> getAdditionalModelBuildingCode(iPartsSeriesId seriesId, String aa) {
        if (seriesId.isValidId() && StrUtils.isValid(aa)) {
            Map<String, Set<String>> modelBuildingCodeMap = seriesToModelBuildingCodeMap.get(seriesId);
            if (modelBuildingCodeMap != null) {
                return modelBuildingCodeMap.get(aa);
            }
        }

        return null;
    }
}