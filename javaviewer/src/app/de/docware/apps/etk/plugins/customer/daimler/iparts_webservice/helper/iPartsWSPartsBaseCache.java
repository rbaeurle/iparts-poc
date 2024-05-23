/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartBase;
import de.docware.util.cache.ObjectInstanceLRUList;

/**
 * Cache zur Verwaltung von {@link iPartsWSPartBase}-Objekten basierend auf Materialnummern und den DB-(Rückfall-)Sprachen
 * zur Verwendung in iParts Webservices
 */
public class iPartsWSPartsBaseCache {

    private static ObjectInstanceLRUList<String, iPartsWSPartBase> partsBaseCache = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_PARTS,
                                                                                                                iPartsPlugin.getCachesLifeTime());

    private static final String CACHE_KEY_DELIMITER = "|";

    public static synchronized void clearCaches() {
        partsBaseCache.clear();
    }

    public static synchronized void removePartBaseFromCache(String partNumber) {
        for (String hashKey : partsBaseCache.getKeys()) {
            if (hashKey.startsWith(partNumber + CACHE_KEY_DELIMITER)) {
                partsBaseCache.removeKey(hashKey);
            }
        }
    }

    /**
     * Liefert das {@link iPartsWSPartBase}-Objekt für die übergebene Teilenummer und die DB-(Rückfall-)Sprachen des übergebenen
     * {@link EtkProject}s aus dem Cache zurück bzw. erzeugt ein neues {@link iPartsWSPartBase}-Objekt und legt es in den Cache.
     *
     * @param partNumber
     * @return
     */
    public static synchronized iPartsWSPartBase getPartBaseFromCache(EtkProject project, String partNumber, boolean withExtendedDescription) {
        String cacheKey = partNumber + CACHE_KEY_DELIMITER + project.getDataBaseLanguagesCacheKey() + CACHE_KEY_DELIMITER + withExtendedDescription;
        iPartsWSPartBase wsPartBase = partsBaseCache.get(cacheKey);
        if (wsPartBase == null) {
            wsPartBase = new iPartsWSPartBase();
            wsPartBase.setPartBaseValues(project, partNumber, withExtendedDescription);
            partsBaseCache.put(cacheKey, wsPartBase);
        }
        return wsPartBase;
    }
}