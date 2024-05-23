package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.transit.iPartsDataTransitLangMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.transit.iPartsDataTransitLangMappingList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.transit.iPartsTransitLangMappingId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache für das Mapping der ISO-Sprachschlüssel auf CLM-Portal-Standard-Sprachencodes
 */
public class iPartsTransitMappingCache implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsTransitMappingCache> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE, MAX_CACHE_LIFE_TIME_CORE);

    // Map für Hashtag-Platzhalter auf mehrsprachige Texte
    protected Map<String, Language> transitToISOMappingMap = new HashMap<>(); // Transit auf ISO, z.B. DEU -> DE
    protected Map<Language, String> isoToTransitMappingMap = new HashMap<>(); // ISO auf Transit, z.B. DE -> DEU
    protected Map<String, String> transitLanguageIdMappingMap = new HashMap<>(); // Transit Text auf Sprach-ID, z.B. DEU -> 4

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsTransitMappingCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsTransitMappingCache.class,
                                                             "TransitMapping", false);
        iPartsTransitMappingCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsTransitMappingCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private iPartsTransitMappingCache() {
    }

    private void load(EtkProject project) {
        transitToISOMappingMap.clear();
        isoToTransitMappingMap.clear();
        transitLanguageIdMappingMap.clear();
        iPartsDataTransitLangMappingList mappingList = iPartsDataTransitLangMappingList.getAllMapping(project);
        for (iPartsDataTransitLangMapping dataMapping : mappingList) {
            String neutralLang = dataMapping.getAsId().getNeutralTransitLanguage();
            Language isoLang = Language.findLanguage(dataMapping.getIsoLang());
            String languageId = dataMapping.getFieldValue(iPartsConst.FIELD_DA_TLM_LANG_ID);
            transitToISOMappingMap.put(neutralLang, isoLang);
            isoToTransitMappingMap.put(isoLang, neutralLang);
            transitLanguageIdMappingMap.put(neutralLang, languageId);
        }
    }

    public Language getIsoLang(String transitLang) {
        iPartsTransitLangMappingId id = new iPartsTransitLangMappingId(transitLang);
        return transitToISOMappingMap.get(id.getNeutralTransitLanguage());
    }

    public boolean hasIsoMapping(String transitLang) {
        return getIsoLang(transitLang) != null;
    }

    public Map<String, Language> getIsoClmMapping() {
        return transitToISOMappingMap;
    }

    public String getTransitLangForIsoLang(Language isoLanguage) {
        return isoToTransitMappingMap.get(isoLanguage);
    }

    public String getLanguageId(String transitNeutralLang) {
        return transitLanguageIdMappingMap.get(transitNeutralLang);
    }

    public String getLanguageId(Language isoLanguage) {
        return getLanguageId(getTransitLangForIsoLang(isoLanguage));
    }
}
