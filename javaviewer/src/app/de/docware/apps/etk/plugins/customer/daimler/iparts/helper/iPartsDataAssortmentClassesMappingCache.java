/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssortmentClassesMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssortmentClassesMappingList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Cache für das Mapping von Sortimentsklassengültigkeiten zur Produktklasse
 */
public class iPartsDataAssortmentClassesMappingCache implements iPartsConst {

    private static final ObjectInstanceStrongLRUList<Object, iPartsDataAssortmentClassesMappingCache> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    public static synchronized iPartsDataAssortmentClassesMappingCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataAssortmentClassesMapping.class,
                                                             "iPartsDataAssortmentClassesMappingCache", false);
        iPartsDataAssortmentClassesMappingCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsDataAssortmentClassesMappingCache();
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

    private final Map<String, String> assortmentClassIdToPcMap = new HashMap<>();
    private final Map<String, Set<String>> productClassToAcMap = new HashMap<>();

    /**
     * Den Cache mit Daten füllen.
     *
     * @param project
     */
    private void load(EtkProject project) {
        iPartsDataAssortmentClassesMappingList acPcMappings = iPartsDataAssortmentClassesMappingList.loadAllAcPcMappings(project);

        for (iPartsDataAssortmentClassesMapping acPcMapping : acPcMappings) {
            String asProductClass = acPcMapping.getASProductClass();
            String assortmentClassId = acPcMapping.getAsId().getAssortmentClass();
            // Produktklasse zur AssortmentClassId
            // AssortmentClassId ist der Schlüssel-> Es kann nur einen Eintrag geben in der Tabelle
            assortmentClassIdToPcMap.put(assortmentClassId, asProductClass);
            // AssortmentClassIds zur Produktklasse gibt es mehrere
            Set<String> assortmentClasses = productClassToAcMap.computeIfAbsent(asProductClass, k -> new HashSet<>());
            assortmentClasses.add(assortmentClassId);
        }
    }

    public String getAsProductClassForAssortmentClassId(String assortmentClassId) {
        return assortmentClassIdToPcMap.get(assortmentClassId);
    }

    public Set<String> getAssortmentClassIdsForProductClass(String productClass) {
        return productClassToAcMap.get(productClass);
    }

}
