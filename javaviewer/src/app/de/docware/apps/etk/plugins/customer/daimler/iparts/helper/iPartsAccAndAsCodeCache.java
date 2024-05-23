/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCodeToRemove;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCodeToRemoveList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Cache für ACC und AS Code
 */
public class iPartsAccAndAsCodeCache {

    private static ObjectInstanceStrongLRUList<Object, iPartsAccAndAsCodeCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private Set<String> accCodes;
    private Set<String> asCodes;
    private Set<String> constStatusCodes; // Set mit ACC und AS Code, die nur für die Statusberechnung (Doku-Relevanz) herangezogen werden
    private Set<String> allAccAndAsCodes;

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsAccAndAsCodeCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsAccAndAsCodeCache.class,
                                                             "AccAndAsCodeCache", false);
        iPartsAccAndAsCodeCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsAccAndAsCodeCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        accCodes = iPartsDataCodeToRemoveList.getCodesToRemove(iPartsDataCodeToRemove.iPartsDataCodeToRemoveTables.ACCESSORY_CODES, project);
        asCodes = iPartsDataCodeToRemoveList.getCodesToRemove(iPartsDataCodeToRemove.iPartsDataCodeToRemoveTables.AS_CODES, project);
        constStatusCodes = iPartsDataCodeToRemoveList.getCodesToRemove(iPartsDataCodeToRemove.iPartsDataCodeToRemoveTables.CONST_STATUS_CODES, project);
        allAccAndAsCodes = new HashSet<>();
        allAccAndAsCodes.addAll(accCodes);
        allAccAndAsCodes.addAll(asCodes);
    }

    public Set<String> getAllAccAndAsCodes() {
        return Collections.unmodifiableSet(allAccAndAsCodes);
    }

    public Set<String> getAllAccCodes() {
        return Collections.unmodifiableSet(accCodes);
    }

    public Set<String> getAllAsCodes() {
        return Collections.unmodifiableSet(asCodes);
    }

    public Set<String> getConstStatusCodes() {
        return Collections.unmodifiableSet(constStatusCodes);
    }

    public boolean isAccOrAsCode(String code) {
        return allAccAndAsCodes.contains(code);
    }

    public boolean isAccCode(String code) {
        return accCodes.contains(code);
    }

    public boolean isAsCode(String code) {
        return asCodes.contains(code);
    }
}
