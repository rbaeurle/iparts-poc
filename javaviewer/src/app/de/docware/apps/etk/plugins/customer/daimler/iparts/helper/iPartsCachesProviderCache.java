/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.iPartsAggCodeMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHashtagTextsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsDIALOGFootNotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.footnotes.iPartsPartFootnotesCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.misc.CompressionUtils;
import de.docware.util.number.DocwareDouble;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Cache für die JSON-Daten vom iParts Caches-Provider
 */
public class iPartsCachesProviderCache implements iPartsConst {

    private static ObjectInstanceLRUList<Object, iPartsCachesProviderCache> instances =
            new ObjectInstanceLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE, true);

    private byte[] cacheDataZipped;

    private static void checkCachesForFillSetCacheDataEvent(SetCacheDataEvent setCacheDataEvent, EtkProject project) {
        // Alle relevanten Cache-Instanzen prüfen
        if (checkCacheForFillSetCacheDataEvent(iPartsAggCodeMappingCache.getInstance(project), setCacheDataEvent)) {
        } else if (checkCacheForFillSetCacheDataEvent(iPartsCustomProperty.getInstance(project), setCacheDataEvent)) {
        } else if (checkCacheForFillSetCacheDataEvent(iPartsDIALOGFootNotesCache.getInstance(project), setCacheDataEvent)) {
        } else if (checkCacheForFillSetCacheDataEvent(DictHashtagTextsCache.getInstance(project), setCacheDataEvent)) {
        } else if (checkCacheForFillSetCacheDataEvent(iPartsPartFootnotesCache.getInstance(project), setCacheDataEvent)) {
        } else if (checkCacheForFillSetCacheDataEvent(iPartsPRIMUSReplacementsCache.getInstance(project), setCacheDataEvent)) {
        } else if (checkCacheForFillSetCacheDataEvent(iPartsResponseData.getInstance(project), setCacheDataEvent)) {
        } else if (checkCacheForFillSetCacheDataEvent(iPartsResponseSpikes.getInstance(project), setCacheDataEvent)) {
        } else if (checkCacheForFillSetCacheDataEvent(iPartsWireHarness.getInstance(project), setCacheDataEvent)) {
        } else if (checkCacheForFillSetCacheDataEvent(iPartsWireHarnessSimplifiedParts.getInstance(project), setCacheDataEvent)) {
        } else if (setCacheDataEvent.getCacheName().equals(iPartsModel.class.getSimpleName())) { // Spezialbehandlung für iPartsModel
            Map<String, String> cacheParametersMap = setCacheDataEvent.getCacheParametersMap();
            if ((cacheParametersMap != null) && StrUtils.isValid(cacheParametersMap.get(iPartsModelId.TYPE))) {
                // Cache für das gewünschte Baumuster befüllen
                iPartsModel.getInstance(project, new iPartsModelId(cacheParametersMap.get(iPartsModelId.TYPE))).fillCacheData(setCacheDataEvent);
            } else {
                // Cache-Daten für alle Baumuster befüllen
                iPartsModel.fillCacheDataForAllModels(setCacheDataEvent, project);
            }
        } else if (setCacheDataEvent.getCacheName().equals(iPartsProduct.class.getSimpleName())) { // Spezialbehandlung für iPartsProduct
            Map<String, String> cacheParametersMap = setCacheDataEvent.getCacheParametersMap();
            if ((cacheParametersMap != null) && StrUtils.isValid(cacheParametersMap.get(iPartsProductId.TYPE))) {
                // Cache für das gewünschte Produkt befüllen
                iPartsProduct.getInstance(project, new iPartsProductId(cacheParametersMap.get(iPartsProductId.TYPE))).fillCacheData(setCacheDataEvent);
            } else {
                // Cache-Daten für alle Produkte befüllen
                iPartsProduct.fillCacheDataForAllProducts(setCacheDataEvent, project);
            }
        }
    }

    /**
     * Erzeugt und befüllt den in dem {@link SetCacheDataEvent} angegeben Cache aufgrund der String-Repräsentation in dem
     * {@link SetCacheDataEvent}.
     *
     * @param setCacheDataEvent
     * @param project
     */
    public static void checkCachesForCreateCacheInstance(SetCacheDataEvent setCacheDataEvent, EtkProject project) {
        // Alle relevanten Cache-Klassem mit Hilfe von einem leerem Cache-Objekt prüfen
        if (checkCacheForCreateCacheInstance(new iPartsAggCodeMappingCache(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsCustomProperty(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsDIALOGFootNotesCache(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new DictHashtagTextsCache(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsModel(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsPartFootnotesCache(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsPRIMUSReplacementsCache(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsProduct(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsResponseData(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsResponseSpikes(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsWireHarness(), setCacheDataEvent, project)) {
        } else if (checkCacheForCreateCacheInstance(new iPartsWireHarnessSimplifiedParts(), setCacheDataEvent, project)) {
        }
    }

    /**
     * Liefert die {@link iPartsCachesProviderCache}-Instanz für den Cache zurück, der in dem {@link SetCacheDataEvent} angegeben
     * ist, und befüllt diesen {@link SetCacheDataEvent} mit der String-Repräsentation des entsprechenden Caches.
     *
     * @param project
     * @param setCacheDataEvent
     * @return
     */
    public static synchronized iPartsCachesProviderCache getInstanceAndFillCacheData(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        String setCacheDataEventCacheKey = setCacheDataEvent.getCacheKey();
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsCachesProviderCache.class, setCacheDataEventCacheKey, false);
        iPartsCachesProviderCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsCachesProviderCache();

            checkCachesForFillSetCacheDataEvent(setCacheDataEvent, project);

            result.cacheDataZipped = setCacheDataEvent.getCacheDataZipped();
            instances.put(hashObject, result);
            if (result.getCacheDataZipped() != null) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, "iPartsCachesProviderCache: JSON data for cache name \""
                                                                                + setCacheDataEventCacheKey + "\" created with zipped size "
                                                                                + result.getSizeZippedInMB() + " MB ("
                                                                                + result.getSizeAsStringInMB() + " MB unzipped)");
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_PERFORMANCE, LogType.ERROR, "iPartsCachesProviderCache: No cache found to create the JSON data for cache name \""
                                                                                + setCacheDataEventCacheKey + "\"");
            }
        } else {
            setCacheDataEvent.setCacheDataZipped(result.getCacheDataZipped());
            Logger.log(iPartsPlugin.LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, "iPartsCachesProviderCache: provided cached JSON data for cache name \""
                                                                            + setCacheDataEventCacheKey + "\" with size : "
                                                                            + result.getSizeZippedInMB() + " MB ("
                                                                            + result.getSizeAsStringInMB() + " MB unzipped)");
        }

        return result;
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    private static boolean checkCacheForFillSetCacheDataEvent(CacheForGetCacheDataEvent cache, SetCacheDataEvent setCacheDataEvent) {
        if (setCacheDataEvent.getCacheName().equals(cache.getCacheName())) {
            cache.fillCacheData(setCacheDataEvent);
            return true;
        } else {
            return false;
        }
    }

    private static boolean checkCacheForCreateCacheInstance(CacheForGetCacheDataEvent cache, SetCacheDataEvent setCacheDataEvent,
                                                            EtkProject project) {
        if (setCacheDataEvent.getCacheName().equals(cache.getCacheName())) {
            cache.createInstance(project, setCacheDataEvent);
            return true;
        } else {
            return false;
        }
    }


    private iPartsCachesProviderCache() {
    }

    public byte[] getCacheDataZipped() {
        return cacheDataZipped;
    }

    public String getSizeZippedInMB() {
        if (cacheDataZipped != null) {
            return new DocwareDouble((double)cacheDataZipped.length / 1024 / 1024).roundMercantileToString();
        }
        return "N/A";
    }

    public String getSizeAsStringInMB() {
        if (cacheDataZipped != null) {
            try {
                String cacheDataAsString = new String(CompressionUtils.getGUnzipped(cacheDataZipped), StandardCharsets.UTF_8);
                return new DocwareDouble(2 * (double)cacheDataAsString.length() / 1024 / 1024).roundMercantileToString(); // 2 Bytes pro Char
            } catch (IOException e) {
                // Ist hier egal
            }
        }
        return "N/A";
    }
}