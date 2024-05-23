/*
 * Copyright (c) 2020 Quanos
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsCountryInvalidPartsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataCountryInvalidParts;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataCountryInvalidPartsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.*;

/**
 * StarParts-Teile nur noch in erlaubten Ländern ausgeben, Cache für Bauteile pro Land, die (!)NICHT(!) ausgegeben werden dürfen!
 * Die Sprache ist der ISO-Sprachschlüssel als String.
 */
public class iPartsCountryInvalidPartsCache implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsCountryInvalidPartsCache> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE, MAX_CACHE_LIFE_TIME_CORE);

    Map<String, Set<String>> partToInvalidCountryCodesMap = new HashMap<>();

    public static synchronized void clearCache() {
        instances.clear();
        iPartsLanguage.resetDaimlerIsoCountryCodesLoaded();
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized iPartsCountryInvalidPartsCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsCountryInvalidPartsCache.class,
                                                             "CountryInvalidParts", false);
        iPartsCountryInvalidPartsCache result = instances.get(hashObject);
        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsCountryInvalidPartsCache();
            result.load(project);
            instances.put(hashObject, result);
        }
        return result;
    }

    private iPartsCountryInvalidPartsCache() {
    }

    private void load(EtkProject project) {
        partToInvalidCountryCodesMap.clear();
        iPartsDataCountryInvalidPartsList invalidPartsList = iPartsDataCountryInvalidPartsList.getAllCountryInvalidParts(project);
        for (iPartsDataCountryInvalidParts item : invalidPartsList) {
            iPartsCountryInvalidPartsId id = item.getAsId();
            if (StrUtils.isValid(id.getPartNo()) && id.isValidCountryCode(project)) {
                Set<String> countryCodeSet = partToInvalidCountryCodesMap.computeIfAbsent(id.getPartNo(), partNo -> new HashSet());
                countryCodeSet.add(id.getCountryCode().toUpperCase());
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Invalid part number and/or country code in table '"
                                                                          + TABLE_DA_COUNTRY_INVALID_PARTS + "': " + id.toString());
            }
        }
    }

    /**
     * Sind StarParts für die übergebene Teilenummer im angegebenen Land gültig?
     *
     * @param partNo
     * @param countryCode
     * @return
     */
    public boolean isValidPart(String partNo, String countryCode) {
        return !isInvalidPart(partNo, countryCode);
    }

    /**
     * Sind StarParts für die übergebene Teilenummer im angegebenen Land ungültig?
     *
     * @param partNo
     * @param countryCode
     * @return
     */
    public boolean isInvalidPart(String partNo, String countryCode) {
        Set<String> countryCodeSet = partToInvalidCountryCodesMap.get(partNo);
        if (countryCodeSet != null) {
            return countryCodeSet.contains(countryCode.toUpperCase());
        }
        return false;
    }

    /**
     * Liefert alle ungültigen Länder für StarParts für die übergebene Teilenummer zurück.
     *
     * @param partNo
     * @return
     */
    public Set<String> getInvalidCountryCodesForPart(String partNo) {
        Set<String> invalidCountryCodes = new TreeSet<>();
        Set<String> countryCodeSet = partToInvalidCountryCodesMap.get(partNo);
        if (countryCodeSet != null) {
            invalidCountryCodes.addAll(countryCodeSet);
        }
        return invalidCountryCodes;
    }
}
