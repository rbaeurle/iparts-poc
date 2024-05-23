/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCodeMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCodeMappingList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.*;

/**
 * Cache für die Tabelle DA_CODE_MAPPING (Beziehung zwischen VedocCategory zusätzlichen AggregateCode, sowie
 * zwischen ModelType (BR) und zusätzlichen AggregateCode)
 */
public class iPartsCodeMappingCache {

    private static ObjectInstanceStrongLRUList<Object, iPartsCodeMappingCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private List<iPartsDataCodeMapping> codeMappingList;
    private Map<String, List<Integer>> categoryMap;
    private Map<String, List<Integer>> modelTypeMap;

    public static synchronized iPartsCodeMappingCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsCodeMappingCache.class, "CodeMapping", false);
        iPartsCodeMappingCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsCodeMappingCache();
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

    private iPartsCodeMappingCache() {

    }

    private void load(EtkProject project) {
        categoryMap = new HashMap<String, List<Integer>>();
        modelTypeMap = new HashMap<String, List<Integer>>();
        codeMappingList = iPartsDataCodeMappingList.loadAllCodeMappings(project).getAsList();
        for (Integer lfdNr = 0; lfdNr < codeMappingList.size(); lfdNr++) {
            iPartsDataCodeMapping dataCodeMapping = codeMappingList.get(lfdNr);
            String category = dataCodeMapping.getAsId().getVedocCategory();
            String modelType = dataCodeMapping.getAsId().getModelTypeId();
            List<Integer> categoryList = categoryMap.get(category);
            if (categoryList == null) {
                categoryList = new ArrayList<Integer>();
                categoryMap.put(category, categoryList);
            }
            categoryList.add(lfdNr);

            List<Integer> modelTypeList = modelTypeMap.get(modelType);
            if (modelTypeList == null) {
                modelTypeList = new ArrayList<Integer>();
                modelTypeMap.put(modelType, modelTypeList);
            }
            modelTypeList.add(lfdNr);

        }
    }

    public Collection<iPartsDataCodeMapping> getCodeMappingsByVedocCategory(String vedocCategory) {
        List<Integer> categoryList = categoryMap.get(vedocCategory);
        if (categoryList != null) {
            List<iPartsDataCodeMapping> result = new ArrayList<iPartsDataCodeMapping>();
            for (Integer lfdNr : categoryList) {
                result.add(codeMappingList.get(lfdNr));
            }
            return Collections.unmodifiableCollection(result);
        }
        return null;
    }

    public Collection<iPartsDataCodeMapping> getCodeMappingsByModelType(String modelType) {
        List<Integer> modelTypeList = modelTypeMap.get(modelType);
        if (modelTypeList != null) {
            List<iPartsDataCodeMapping> result = new ArrayList<iPartsDataCodeMapping>();
            for (Integer lfdNr : modelTypeList) {
                result.add(codeMappingList.get(lfdNr));
            }
            return Collections.unmodifiableCollection(result);
        }
        return null;
    }
}
