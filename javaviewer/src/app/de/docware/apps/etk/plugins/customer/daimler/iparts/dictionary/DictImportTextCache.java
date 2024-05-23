/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.sql.TableAndFieldName;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache für das Verwenden von DIALOG/ELDAS-IDs und das Halten von Texten zu Textarten
 */
public class DictImportTextCache implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, DictImportTextCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private Map<DictTextKindTypes, Map<String, String>> textEntryCaches = Collections.synchronizedMap(new HashMap<DictTextKindTypes, Map<String, String>>());
    private Map<String, Map<String, String>> foreignIdCaches = Collections.synchronizedMap(new HashMap<String, Map<String, String>>());

    public static synchronized DictImportTextCache getInstance(EtkProject project) {
        return getInstance(project, true);
    }

    public static synchronized DictImportTextCache getInstance(EtkProject project, boolean createCacheIfNotExists) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), DictImportTextCache.class, "DictTextCache", false);
        DictImportTextCache result = instances.get(hashObject);

        if ((result == null) && createCacheIfNotExists) {
            result = new DictImportTextCache();
            instances.put(hashObject, result);
        }

        return result;
    }

    public static synchronized void clearAllCaches() {
        instances.clear();
    }

    /**
     * Löscht für alle Instanzen den Cache, der mit der übergebenen Textart verknüpft ist. Sollte die Textart nicht
     * gültig oder <code>UNKNOWN</code> sein, dann werden alle Caches gelöscht.
     *
     * @param project
     * @param textKindType
     * @param clearForeignIdCaches Flag, ob die Caches für die Fremd-IDs auch gelöscht werden sollen
     */
    public static synchronized void clearCacheForType(EtkProject project, DictTextKindTypes textKindType, boolean clearForeignIdCaches) {
        DictImportTextCache cacheInstance = getInstance(project, false);
        if (cacheInstance != null) {
            if ((textKindType == null) || (textKindType == DictTextKindTypes.UNKNOWN)) {
                cacheInstance.clearCaches();
            } else {
                cacheInstance.clearCacheForTextKind(textKindType);
                if (clearForeignIdCaches) {
                    cacheInstance.clearForeignIdCache();
                }
            }
        }
    }

    /**
     * Liefert den Cache für Texte der angegebenen Textart zurück und initialisiert ihn bei Bedarf.
     *
     * @param project
     * @param textKindType
     * @param infos
     * @return
     */
    public Map<String, String> getTextCache(EtkProject project, DictTextKindTypes textKindType, List<String> infos) {
        Map<String, String> textEntryCache = textEntryCaches.get(textKindType);
        if (textEntryCache != null) {
            return textEntryCache;
        }

        // Cache neu erzeugen
        textEntryCache = Collections.synchronizedMap(new HashMap<String, String>());
        if (textKindType == DictTextKindTypes.UNKNOWN) {
            textEntryCaches.put(textKindType, textEntryCache);
            return textEntryCache;
        }

        EtkDisplayFields selectFields = new EtkDisplayFields();

        EtkDisplayField field = new EtkDisplayField(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN), false, false);
        selectFields.addFeld(field);
        boolean dbHasLongTexts = project.getConfig().getDataBaseVersion() >= 6.2;
        if (dbHasLongTexts) {
            field = new EtkDisplayField(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN_LANG), false, false);
            selectFields.addFeld(field);
        }

        field = new EtkDisplayField(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID), false, false);
        selectFields.addFeld(field);

        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(project).getTxtKindId(textKindType);

        // Join auf die DA_DICT_META (DA_DICT_META.DA_DICT_META_TEXTID = SPRACHE.S_TEXTID) und Einschränkung auf die
        // Textart "txtKindId". Über den Callback werden die nötigen Informationen extrahiert ohne neue DBObjects zu erzeugen
        EtkDataTextEntryList list = new EtkDataTextEntryList();
        list.setSearchWithoutActiveChangeSets(true);
        EtkDataObjectList.JoinData joinData = new EtkDataObjectList.JoinData(TABLE_DA_DICT_META, new String[]{ FIELD_S_TEXTID },
                                                                             new String[]{ FIELD_DA_DICT_META_TEXTID }, false, false);
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = getFoundAttrbiutesCallBackForTexts(textEntryCache,
                                                                                                               dbHasLongTexts, infos);
        list.searchSortAndFillWithJoin(project, null, selectFields,
                                       new String[]{ TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TXTKIND_ID),
                                                     TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH) },
                                       new String[]{ txtKindId.getTextKindId(), Language.DE.getCode() }, false, null, false,
                                       foundAttributesCallback, joinData);

        textEntryCaches.put(textKindType, textEntryCache);
        return textEntryCache;
    }

    private EtkDataObjectList.FoundAttributesCallback getFoundAttrbiutesCallBackForTexts(final Map<String, String> textEntryCache,
                                                                                         final boolean dbHasLongTexts, final List<String> infos) {
        return new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String foundDictID = attributes.getFieldValue(FIELD_S_TEXTID);
                String foundText = attributes.getFieldValue(FIELD_S_BENENN);

                // Ab DBVersion 6.2 die Langtexte beachten
                if (dbHasLongTexts) {
                    String foundTextLong = attributes.getFieldValue(FIELD_S_BENENN_LANG);
                    if (!foundTextLong.isEmpty()) {
                        foundText = foundTextLong;
                    }
                }
                String currentIdForText = textEntryCache.get(foundText);
                if (currentIdForText != null) {
                    if (infos != null) {
                        if (!currentIdForText.equals(foundDictID)) {
                            String msg = TranslationHandler.translate("!!Gleicher Text mit unterschiedlichen Text-IDs! Text: \"%1\", aktuelle Text-ID: \"%2\", neue Text-ID: \"%3\"",
                                                                      foundText, currentIdForText, foundDictID);
                            if (DictHelper.isGuidTextId(currentIdForText)) {
                                if (!DictHelper.isGuidTextId(foundDictID)) {
                                    // Text mit GUID-TextId gegen Lexikon-DictId tauschen
                                    textEntryCache.put(foundText, foundDictID);
                                    msg += " " + TranslationHandler.translate("!!(Text-ID wurde getauscht)");
                                }
                            }
                            infos.add(msg);
                        }
                    }
                } else {
                    textEntryCache.put(foundText, foundDictID);
                }
                return false;
            }
        };
    }

    /**
     * Liefert für die übergebene Textart und ELDAS-ID die dazugehörige Lexikon Text-ID zurück.
     *
     * @param project
     * @param textKindType Kann auch {@code null} sein
     * @param eldasId
     * @param warnings
     * @return {@code null} falls keine Lexikon Text-ID für die übergebene Textart und ELDAS-ID gefunden werden konnte.
     */
    public String getDictTextIdForEldasId(EtkProject project, DictTextKindTypes textKindType, String eldasId, List<String> warnings) {
        return getForeignIdCache(project, textKindType, FIELD_DA_DICT_META_ELDASID, warnings).get(eldasId);
    }

    /**
     * Liefert für die übergebene Textart und DIALOG-ID die dazugehörige Lexikon Text-ID zurück.
     *
     * @param project
     * @param textKindType Kann auch {@code null} sein
     * @param dialogId
     * @param warnings
     * @return {@code null} falls keine Lexikon Text-ID für die übergebene Textart und DIALOG-ID gefunden werden konnte.
     */
    public String getDictTextIdForDialogId(EtkProject project, DictTextKindTypes textKindType, String dialogId, List<String> warnings) {
        return getForeignIdCache(project, textKindType, FIELD_DA_DICT_META_DIALOGID, warnings).get(dialogId);
    }

    /**
     * Liefert den Cache für Fremd-IDs der angegebenen Textart zurück und initialisiert ihn bei Bedarf.
     *
     * @param project
     * @param textKindType       Kann auch {@code null} sein
     * @param foreignIdFieldName
     * @param warnings
     */
    public Map<String, String> getForeignIdCache(EtkProject project, DictTextKindTypes textKindType, String foreignIdFieldName,
                                                 List<String> warnings) {
        String foreignIdCacheKey = ((textKindType != null) ? textKindType.getMadId() : "") + "@" + foreignIdFieldName;
        Map<String, String> foreignIdCache = foreignIdCaches.get(foreignIdCacheKey);
        if (foreignIdCache != null) {
            return foreignIdCache;
        }

        // Cache neu erzeugen
        foreignIdCache = Collections.synchronizedMap(new HashMap<String, String>());

        String[] whereFields = null;
        String[] whereValues = null;
        if (textKindType != null) {
            iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(project).getTxtKindId(textKindType);
            whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TXTKIND_ID) };
            whereValues = new String[]{ textKindId.getTextKindId() };
        }
        String[] whereNotFields = new String[]{ foreignIdFieldName };
        String[] whereNotValues = new String[]{ "" }; // alle Einträge für die Fremd-IDs holen

        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        list.setSearchWithoutActiveChangeSets(true);
        EtkDataObjectList.FoundAttributesCallback callback = getCallBackForForeignIdCache(foreignIdCache, foreignIdFieldName,
                                                                                          warnings);
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(FIELD_DA_DICT_META_TXTKIND_ID, false, false));
        selectFields.addFeld(new EtkDisplayField(FIELD_DA_DICT_META_TEXTID, false, false));
        selectFields.addFeld(new EtkDisplayField(foreignIdFieldName, false, false));
        list.searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues, whereNotFields,
                                       whereNotValues, false, null, false, false, callback);

        foreignIdCaches.put(foreignIdCacheKey, foreignIdCache);
        return foreignIdCache;
    }

    private EtkDataObjectList.FoundAttributesCallback getCallBackForForeignIdCache(final Map<String, String> foreignIdCache,
                                                                                   final String foreignIdFieldName, final List<String> warnings) {
        return new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String foreignIDs = attributes.getFieldValue(foreignIdFieldName);
                String foundDictID = attributes.getFieldValue(FIELD_DA_DICT_META_TEXTID);

                // foreignIDs kann aus mehreren IDs getrennt durch Komma bestehen
                String[] foreignIDsArray = StrUtils.toStringArray(foreignIDs, ",", false, true);
                for (String foreignID : foreignIDsArray) {
                    String currentIDForForeignID = foreignIdCache.get(foreignID);
                    if (currentIDForForeignID != null) {
                        if (warnings != null) {
                            if (!currentIDForForeignID.equals(foundDictID)) {
                                warnings.add(TranslationHandler.translate("!!Gleiche Fremd-ID für das Feld \"%1\" mit unterschiedlichen Text-IDs! Fremd-ID: \"%2\", aktuelle Text-ID: \"%3\", neue Text-ID: \"%4\"",
                                                                          foreignIdFieldName, foreignID, currentIDForForeignID, foundDictID));
                            }
                        }
                    } else {
                        foreignIdCache.put(foreignID, foundDictID);
                    }
                }
                return false;
            }
        };
    }

    /**
     * Löscht den Cache für alle Textarten und Fremd-IDs
     */
    public void clearCaches() {
        textEntryCaches.clear();
        clearForeignIdCache();
    }

    private void clearForeignIdCache() {
        foreignIdCaches.clear();
    }

    /**
     * Löscht den Cacheeintrag für die übergebene Textart
     *
     * @param textKindType
     */
    private void clearCacheForTextKind(DictTextKindTypes textKindType) {
        textEntryCaches.remove(textKindType);
    }
}
