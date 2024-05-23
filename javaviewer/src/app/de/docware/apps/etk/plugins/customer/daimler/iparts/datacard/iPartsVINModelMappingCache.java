/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVINModelMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVINModelMappingList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.VinId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache für die Tabelle DA_VIN_MODEL_MAPPING (Mapping VIN Prefix auf Baumusterprefix)
 */
public class iPartsVINModelMappingCache {

    private static ObjectInstanceStrongLRUList<Object, iPartsVINModelMappingCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private Map<String, List<String>> vinModelMap;

    public static synchronized iPartsVINModelMappingCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsVINModelMappingCache.class, "VINModelMappingCache", false);
        iPartsVINModelMappingCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsVINModelMappingCache(project);
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

    private iPartsVINModelMappingCache(EtkProject project) {
        vinModelMap = new HashMap<String, List<String>>();
        List<iPartsDataVINModelMapping> vinModelMappingList = iPartsDataVINModelMappingList.loadCompleteMapping(project).getAsList();
        for (iPartsDataVINModelMapping mappingEntry : vinModelMappingList) {
            String vinPrefix = mappingEntry.getAsId().getVINPrefix();
            String modelPrefix = mappingEntry.getAsId().getModelPrefix();
            List<String> modelPrefixList = vinModelMap.get(vinPrefix);
            if (modelPrefixList == null) {
                modelPrefixList = new DwList<String>();
                vinModelMap.put(vinPrefix, modelPrefixList);
            }
            modelPrefixList.add(modelPrefix);
        }
    }

    public Map<String, List<String>> getVinModelMap() {
        return Collections.unmodifiableMap(vinModelMap);
    }

    /**
     * Liefert für die ersten 7 Stellen der VIN (VIN-Prefix) die möglichen Baumuster. Wenn <i>project</i> nicht null ist,
     * werden nur im After-Sales sichtbare Baumuster zurückgeliefert.
     * Mapping Regeln:
     * 1. Erste 5 Stellen mappen auf Fahrzeug-Prefixe (Tabelle: DA_VIN_MODEL_MAPPING)
     * 2. Fahrezeugprefix "C" wird vorangestellt
     * 3. Stelle 6 und 7 der VIN ergeben den Suffix des Baumusters
     * Bsp.:
     * VIN: 4JGAB72E3G3138415
     * 1. 4JGAB mappt auf 1631, 1641, 2511
     * 2. Aus 1631, 1641, 2511 wird C1631, C1641, C2511
     * 3. 6. und 7. Stelle der VIN: 72 - aus C1631, C1641, C2511 wird C163172, C164172, C251172
     *
     * @param project
     * @param vin
     * @return
     */
    private List<String> getModelsForVINPrefix(EtkProject project, String vin) {
        VinId vinId = new VinId(vin);
        List<String> result = new DwList<String>();
        if (StrUtils.isEmpty(vin) || (vin.length() < 7) || !vinId.isValidForModelMapping()) {
            return result;
        }

        List<String> models = vinModelMap.get(vinId.getPrefixForModelMapping());
        if ((models == null) || models.isEmpty()) {
            return result;
        }

        for (String modelPrefix : models) {
            String modelNumber = iPartsConst.MODEL_NUMBER_PREFIX_CAR + modelPrefix + vinId.getSuffixForModelMapping();
            boolean valid = iPartsModelId.isModelNumberValid(modelNumber, true);
            if (valid && (project != null)) {
                iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNumber));
                valid = model.isModelVisible(); // <== Hier gibt es noch kein Produkt, hier kann DPM_MODEL_VISIBLE nicht überprüft werden.
            }
            if (valid) {
                result.add(modelNumber);
            }
        }
        return result;
    }

    /**
     * Liefert für die ersten 7 Stellen der VIN (VIN-Prefix) alle möglichen Baumuster - auch wenn diese im After-Sales
     * nicht sichtbar sind.
     *
     * @param vin
     * @return
     */
    public List<String> getAllModelsForVINPrefix(String vin) {
        return getModelsForVINPrefix(null, vin);
    }

    /**
     * Liefert für die ersten 7 Stellen der VIN (VIN-Prefix) die möglichen Baumuster. Wenn <i>project</i> nicht null ist,
     * werden nur im After-Sales sichtbare Baumuster zurückgeliefert.
     *
     * @param project
     * @param vin
     * @return
     */
    public List<String> getVisibleModelsForVINPrefix(EtkProject project, String vin) {
        return getModelsForVINPrefix(project, vin);
    }

    /**
     * Bestimmt, ob für die ersten 7 Stellen der VIN (VIN-Prefix) es mehrere mögliche Baumuster gibt. Wenn <i>project</i>
     * nicht null ist, werden nur im After-Sales sichtbare Baumuster berücksichtigt.
     *
     * @param project
     * @param vin
     * @return
     */
    public boolean hasMultipleMappedModels(EtkProject project, String vin) {
        return getModelsForVINPrefix(project, vin).size() > 1;
    }

    /**
     * Bestimmt, ob für die ersten 7 Stellen der VIN (VIN-Prefix) es mindestens ein mögliches Baumuster gibt. Wenn <i>project</i>
     * nicht null ist, werden nur im After-Sales sichtbare Baumuster berücksichtigt.
     *
     * @param project
     * @param vin
     * @return
     */
    public boolean hasAtLeastOneMappedModel(EtkProject project, String vin) {
        return getModelsForVINPrefix(project, vin).size() >= 1;
    }
}