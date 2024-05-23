/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSpringMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSpringMappingList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Cache für das Mapping Federbein zu Feder in die Tabelle [DA_SPRING_MAPPING]
 * <p>
 * ACHTUNG: Es kann nur eine geben ... 1:1-Relation: jedem Federbein kann per Definition nur eine Feder zugeordnet sein!
 */
public class iPartsSpringMapping implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsSpringMapping> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    // Map mit ZB Federbein Teilenummern auf DataObjects. Die Objects enthalten die zugehörigen Feder Teilenummern.
    protected Map<String, iPartsDataSpringMapping> springLegPartNumberToDataMap = new HashMap<>();
    // Mapping von Feder auf mögliche ZB Federbeine
    protected Map<String, Set<String>> springToSpringLegsMap = new HashMap<>();

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsSpringMapping getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsSpringMapping.class, "SpringMapping", false);
        iPartsSpringMapping result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsSpringMapping();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    /**
     * Lesen einer Feder zum übergebenen Federbein.
     *
     * @param springLegId
     * @return
     */
    public iPartsDataSpringMapping getSpringMappingForSpringLeg(String springLegId) {
        return springLegPartNumberToDataMap.get(springLegId);
    }

    /**
     * Überprüfung, ob das Federbein existiert.
     *
     * @param springLegId
     * @return
     */
    public boolean getSpringLegExists(String springLegId) {
        iPartsDataSpringMapping item = springLegPartNumberToDataMap.get(springLegId);
        return item != null;
    }

    /**
     * Den Cache mit Daten füllen.
     *
     * @param project
     */
    private void load(EtkProject project) {
        iPartsDataSpringMappingList springMappings = iPartsDataSpringMappingList.loadEntireMapping(project);

        for (iPartsDataSpringMapping dataSpringMapping : springMappings) {
            // Cache für ZB Federbein auf das ganze Datenbankobjekt
            String springLegPartNumber = dataSpringMapping.getAsId().getZBSpringLeg();
            springLegPartNumberToDataMap.put(springLegPartNumber, dataSpringMapping);
            // Cache von Feder auf alle möglichen ZB Federbeine
            String springPartNumber = dataSpringMapping.getFieldValue(FIELD_DSM_SPRING);
            Set<String> springLegsForSpringPartNumber = springToSpringLegsMap.get(springPartNumber);
            if (springLegsForSpringPartNumber == null) {
                springLegsForSpringPartNumber = new TreeSet<>();
                springToSpringLegsMap.put(springPartNumber, springLegsForSpringPartNumber);
            }
            springLegsForSpringPartNumber.add(springLegPartNumber);
        }
    }

    public Set<String> getSpringLegsForSpringPartNumber(String springPartNumber) {
        return springToSpringLegsMap.get(springPartNumber);
    }
}


