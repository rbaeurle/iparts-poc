/*
 * Copyright (c) 2020 Quanos
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsCountryValidSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataCountryValidSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataCountryValidSeriesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.*;

/**
 * Cache um StarParts-Teile nur noch in erlaubten Ländern auszugeben (Baumuster-Präfix + Land), bei denen die StarParts
 * grundsätzlich ausgegeben werden dürfen. Die Sprache ist der ISO-Sprachschlüssel.
 */
public class iPartsCountryValidSeriesCache implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsCountryValidSeriesCache> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE, MAX_CACHE_LIFE_TIME_CORE);

    Map<String, Set<String>> countryCodeToValidModelPrefixesMap = new HashMap<>();

    public static synchronized void clearCache() {
        instances.clear();
        iPartsLanguage.resetDaimlerIsoCountryCodesLoaded();
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized iPartsCountryValidSeriesCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsCountryValidSeriesCache.class,
                                                             "CountryValidSeries", false);
        iPartsCountryValidSeriesCache result = instances.get(hashObject);
        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsCountryValidSeriesCache();
            result.load(project);
            instances.put(hashObject, result);
        }
        return result;
    }

    private iPartsCountryValidSeriesCache() {
    }

    private void load(EtkProject project) {
        countryCodeToValidModelPrefixesMap.clear();
        iPartsDataCountryValidSeriesList validModelPrefixesList = iPartsDataCountryValidSeriesList.getAllCountryValidSeries(project);
        for (iPartsDataCountryValidSeries item : validModelPrefixesList) {
            iPartsCountryValidSeriesId id = item.getAsId();
            if (StrUtils.isValid(id.getSeriesNo()) && id.isValidCountryCode(project)) {
                Set<String> validModelPrefixSet = countryCodeToValidModelPrefixesMap.computeIfAbsent(id.getCountryCode().toUpperCase(),
                                                                                                     countryCode -> new HashSet<>());
                validModelPrefixSet.add(id.getSeriesNo().toUpperCase());
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Invalid model number prefix and/or country code in table '"
                                                                          + TABLE_DA_COUNTRY_VALID_SERIES + "': " + id.toString());
            }
        }
    }

    /**
     * Sind StarParts für den übergebenen Baumuster-Präfix im angegebenen Land gültig?
     *
     * @param modelPrefix
     * @param countryCode
     * @return
     */
    public boolean isValidModelPrefix(String modelPrefix, String countryCode) {
        Set<String> validModelPrefixSet = countryCodeToValidModelPrefixesMap.get(countryCode.toUpperCase());
        if (validModelPrefixSet != null) {
            modelPrefix = modelPrefix.toUpperCase();
            for (String validModelPrefix : validModelPrefixSet) {
                if (modelPrefix.startsWith(validModelPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Liefert alle gültigen Länder für StarParts für den übergebenen Baumuster-Präfix zurück.
     *
     * @param modelPrefix
     * @return
     */
    public Set<String> getValidCountryCodesForModelPrefix(String modelPrefix) {
        Set<String> validCountryCodes = new TreeSet<>();
        modelPrefix = modelPrefix.toUpperCase();
        for (Map.Entry<String, Set<String>> validModelPrefixEntry : countryCodeToValidModelPrefixesMap.entrySet()) {
            for (String validModelPrefix : validModelPrefixEntry.getValue()) {
                if (modelPrefix.startsWith(validModelPrefix)) {
                    validCountryCodes.add(validModelPrefixEntry.getKey());
                    break;
                }
            }
        }
        return validCountryCodes;
    }
}
