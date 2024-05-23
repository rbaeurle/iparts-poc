/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssortmentClassesMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssortmentPermissionMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssortmentPermissionsMappingList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.*;

/**
 * Cache für das Mapping von Berechtigung auf eine AS-Produktklasse in der Tabelle DA_AC_PC_PERMISSION_MAPPING
 * um die Inhalte von GetProductClasses auf Basis des Tokens filtern zu können.
 */
public class iPartsDataAssortmentPermissionsMappingCache implements iPartsConst {

    private static final ObjectInstanceStrongLRUList<Object, iPartsDataAssortmentPermissionsMappingCache> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    // Das ist die Liste für den Cache <Brand + '.' + AssortmentClass>, <Set<Aftersales Produktklasse>>
    private final Map<String, Set<String>> permissionToAsProductClassMap = new HashMap<>();

    // Map mit AS-Produktklassen auf Brand und Branches, z.B. P -> MB.PASSENGER-CAR
    private final Map<String, Map<String, Set<String>>> asProductClassesToPermissions = new HashMap<>();

    public static synchronized iPartsDataAssortmentPermissionsMappingCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataAssortmentClassesMapping.class,
                                                             "iPartsDataAssortmentPermissionsMappingCache", false);
        iPartsDataAssortmentPermissionsMappingCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsDataAssortmentPermissionsMappingCache();
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
     * Erzeugt aus den übergebenen Parametern einen Permission-String
     *
     * @param brand
     * @param branch
     * @return
     */
    public static String getPermission(String brand, String branch) {
        return brand + "." + branch;
    }

    /**
     * Den Cache mit Daten füllen.
     *
     * @param project
     */
    private void load(EtkProject project) {
        // Alle Daten aus der Datenbank lesen
        iPartsDataAssortmentPermissionsMappingList mappings = iPartsDataAssortmentPermissionsMappingList.loadAllAcPcPermissionMappings(project);
        // Und jetzt die Daten in den Cache übertragen
        for (iPartsDataAssortmentPermissionMapping oneMapping : mappings) {
            // Die Berechtigung gibt es nur einmal
            String permission = oneMapping.getPermission();

            // Aber zur Berechtigung kann es mehrere AS-Produktklassen geben!
            String asProductClass = oneMapping.getASProductClass();
            Set<String> asProductClasses = permissionToAsProductClassMap.computeIfAbsent(permission, k -> new HashSet<>());
            asProductClasses.add(asProductClass);

            // AS-Produktklasse auf Brand und Branches
            Map<String, Set<String>> brandsToBranches = asProductClassesToPermissions.computeIfAbsent(asProductClass, k -> new TreeMap<>());
            String brand = oneMapping.getBrand();
            Set<String> branches = brandsToBranches.computeIfAbsent(brand, k -> new TreeSet<>());
            branches.add(oneMapping.getAssortmentClass());
        }
    }

    /**
     * Ermittelt zu einer übergebenen Berechtigung die dafür zulässigen Aftersales Produktklassen.
     *
     * @param permission
     * @return
     */
    public Set<String> getAsProductClassesForPermission(String permission) {
        return permissionToAsProductClassMap.get(permission);
    }

    /**
     * Liefert alle Brand und Branches zu den übergebenen Produktklassen in einer {@link Map}.
     *
     * @param asProductClasses
     * @return
     */
    public Map<String, Set<String>> getBrandsAndBranchesForASProductClasses(Set<String> asProductClasses) {
        Map<String, Set<String>> result = new TreeMap<>();
        if (!Utils.isValid(asProductClasses)) {
            return result;
        }

        asProductClasses.forEach(asProductClass -> {
            Map<String, Set<String>> brandsAndBranches = asProductClassesToPermissions.get(asProductClass);
            if (Utils.isValid(brandsAndBranches)) {
                brandsAndBranches.forEach((brand, branches) -> {
                    Set<String> resultBranches = result.computeIfAbsent(brand, k -> new TreeSet<>());
                    resultBranches.addAll(branches);
                });
            }
        });
        return result;
    }
}
