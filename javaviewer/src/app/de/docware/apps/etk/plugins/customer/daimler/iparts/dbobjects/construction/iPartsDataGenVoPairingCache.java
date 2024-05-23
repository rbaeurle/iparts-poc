/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.util.cache.ObjectInstanceLRUList;

import java.util.*;

public class iPartsDataGenVoPairingCache implements iPartsConst {

    private static ObjectInstanceLRUList<Object, iPartsDataGenVoPairingCache> instances =
            new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_SERIES, MAX_CACHE_LIFE_TIME_CORE);

    private Map<String, Set<String>> genvoLeftToRightMap;
    private Map<String, Set<String>> genvoRightToLeftMap;

    public static synchronized iPartsDataGenVoPairingCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataGenVoPairingCache.class, "GenVoPairing", false);
        iPartsDataGenVoPairingCache result = instances.get(hashObject);

        // Noch nicht geladen? -> lade aus der Datenbank
        if (result == null) {
            result = new iPartsDataGenVoPairingCache();
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

    private iPartsDataGenVoPairingCache() {
    }

    private void load(EtkProject project) {
        genvoLeftToRightMap = new HashMap<>();
        genvoRightToLeftMap = new HashMap<>();
        for (iPartsDataGenVoPairing dataGenVoPairing : iPartsDataGenVoPairingList.loadAllGenVoPairings(project)) {
            String genvoLeft = dataGenVoPairing.getGenVoLeft();
            String genvoRight = dataGenVoPairing.getGenVoRight();
            // Auf die beiden Maps verteilen
            putPairingToMap(genvoLeftToRightMap, genvoLeft, genvoRight);
            putPairingToMap(genvoRightToLeftMap, genvoRight, genvoLeft);
        }
    }

    private void putPairingToMap(Map<String, Set<String>> map, String key, String value) {
        Set<String> pairingSet = map.get(key);
        if (pairingSet == null) {
            pairingSet = new HashSet<>();
            map.put(key, pairingSet);
        }
        pairingSet.add(value);
    }

    public Set<String> getCorrespondingRightGenVOs(String genvoLeft) {
        return getCorrespondingGenVOs(genvoLeftToRightMap, genvoLeft);
    }

    public Set<String> getCorrespondingLeftGenVOs(String genvoRight) {
        return getCorrespondingGenVOs(genvoRightToLeftMap, genvoRight);
    }

    /**
     * Sucht in der DA_GENVO_PAIRING nach einem Eintrag, egal ob Links oder Rechts
     *
     * @param genvoLeftOrRight
     * @return
     */
    public Set<String> getCorrespondingGenVOs(String genvoLeftOrRight) {
        Set<String> result = new TreeSet<>();
        result.addAll(getCorrespondingLeftGenVOs(genvoLeftOrRight));
        result.addAll(getCorrespondingRightGenVOs(genvoLeftOrRight));
        return result;
    }

    private Set<String> getCorrespondingGenVOs(Map<String, Set<String>> map, String genvo) {
        Set<String> resultSet = map.get(genvo);
        if (resultSet == null) {
            resultSet = new HashSet<>();
        }
        return resultSet;
    }
}
