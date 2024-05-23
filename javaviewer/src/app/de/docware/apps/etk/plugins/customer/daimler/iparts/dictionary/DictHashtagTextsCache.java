/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.common.EtkDataTextEntry;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.base.project.events.CacheForGetCacheDataEvent;
import de.docware.apps.etk.base.project.events.SetCacheDataEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache für die #-Texte aus dem Lexikon für Fußnoten
 * Speichert die {@link EtkMultiSprache}n für die Texte
 */
public class DictHashtagTextsCache implements CacheForGetCacheDataEvent<DictHashtagTextsCache>, iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, DictHashtagTextsCache> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE, MAX_CACHE_LIFE_TIME_CORE);

    // Map für Hashtag-Platzhalter auf mehrsprachige Texte
    @JsonProperty
    protected Map<String, EtkMultiSprache> hashtagTextsMap = new HashMap<>();

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    private static String getInstanceHashObject(EtkProject project) {
        return CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), DictHashtagTextsCache.class, "HashtagTexts", false);
    }

    public static synchronized DictHashtagTextsCache getInstance(EtkProject project) {
        Object hashObject = getInstanceHashObject(project);
        DictHashtagTextsCache result = instances.get(hashObject);

        if (result == null) {
            result = iPartsPlugin.createCacheInstanceWithCachesProvider(new DictHashtagTextsCache(), null, instances, hashObject);
            if (result != null) {
                return result;
            }

            // Noch nicht geladen -> lade aus der Datenbank
            result = new DictHashtagTextsCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }


    @Override
    public DictHashtagTextsCache createInstance(EtkProject project, SetCacheDataEvent setCacheDataEvent) {
        return createInstance(setCacheDataEvent, instances, getInstanceHashObject(project));
    }

    private void load(EtkProject project) {
        hashtagTextsMap.clear();

        EtkDataTextEntryList textEntryList = new EtkDataTextEntryList();
        textEntryList.setSearchWithoutActiveChangeSets(true);
        textEntryList.searchWithWildCardsSortAndFill(project, new String[]{ FIELD_S_TEXTID, FIELD_S_FELD },
                                                     new String[]{ DictHelper.buildDictTextId(DictHelper.buildHashtagForeignId("*")),
                                                                   TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT) },
                                                     null, DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        for (EtkDataTextEntry textEntry : textEntryList) {
            String textId = textEntry.getFieldValue(FIELD_S_TEXTID);
            String id = DictHelper.getDictId(textId);
            if (id.startsWith(DictHelper.HASHTAG_FOREIGN_ID_PREFIX)) {
                id = id.substring(DictHelper.HASHTAG_FOREIGN_ID_PREFIX.length());
                EtkMultiSprache multi = hashtagTextsMap.get(id);
                if (multi == null) {
                    multi = new EtkMultiSprache();
                    multi.setTextId(textId);
                    hashtagTextsMap.put(id, multi);
                }
                multi.setText(textEntry.getFieldValue(FIELD_S_SPRACH), textEntry.getFieldValue(FIELD_S_BENENN));
            }
        }
    }

    public Map<String, EtkMultiSprache> getHashtagTextsMap() {
        Map<String, EtkMultiSprache> result = new HashMap<>();
        result.putAll(hashtagTextsMap);
        return result;
    }

    public String getText(EtkProject project, String hashtagKey) {
        String language = Language.DE.getCode();
        List<String> fallbackLanguages = null;
        if (project != null) {
            language = project.getDBLanguage();
            fallbackLanguages = project.getDataBaseFallbackLanguages();
        }
        return getText(hashtagKey, language, fallbackLanguages);
    }

    public String getText(EtkProject project, String hashtagKey, String language) {
        if (project != null) {
            return getText(hashtagKey, language, project.getDataBaseFallbackLanguages());
        } else {
            return getText(hashtagKey, language, null);
        }
    }

    public String getText(String hashtagKey, String language, List<String> fallbackLanguages) {
        EtkMultiSprache multi = hashtagTextsMap.get(hashtagKey);
        if (multi != null) {
            return multi.getTextByNearestLanguage(language, fallbackLanguages);
        }
        return "";
    }
}
