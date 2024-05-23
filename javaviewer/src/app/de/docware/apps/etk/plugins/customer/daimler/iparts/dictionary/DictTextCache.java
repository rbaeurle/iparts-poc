/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Cache für alle Lexikon-Texte einer bestimmten Textart {@link DictTextKindTypes} und Sprache inkl. Text-IDs.
 */
public class DictTextCache implements iPartsConst {

    // LinkedHashSet für die Reihenfolge, da EnumSet alphabetisch sortiert
    public static final Set<DictTextKindTypes> TEXT_KIND_TYPES_FOR_WARM_UP = new LinkedHashSet<>();
    public static final Set<DictTextKindTypes> TEXT_KIND_TYPES_FOR_RESTRICTED_SEARCH = new LinkedHashSet<>();
    public static final Set<Language> LANGUAGES_FOR_WARM_UP = new LinkedHashSet<>();

    private static final EnumSet<iPartsImportDataOrigin> VALID_CAR_AND_VAN_SOURCES_FOR_DICTIONARY = EnumSet.of(iPartsImportDataOrigin.IPARTS_MB,
                                                                                                               iPartsImportDataOrigin.IPARTS_GENVO,
                                                                                                               iPartsImportDataOrigin.CONNECT,
                                                                                                               iPartsImportDataOrigin.IPARTS_SPK);
    private static final EnumSet<iPartsImportDataOrigin> VALID_TRUCK_AND_BUS_SOURCES_FOR_DICTIONARY = EnumSet.of(iPartsImportDataOrigin.IPARTS_TRUCK);

    static {
        TEXT_KIND_TYPES_FOR_WARM_UP.add(DictTextKindTypes.ADD_TEXT);
        TEXT_KIND_TYPES_FOR_WARM_UP.add(DictTextKindTypes.NEUTRAL_TEXT);
        TEXT_KIND_TYPES_FOR_WARM_UP.add(DictTextKindTypes.FOOTNOTE);

        TEXT_KIND_TYPES_FOR_RESTRICTED_SEARCH.add(DictTextKindTypes.ADD_TEXT);

        LANGUAGES_FOR_WARM_UP.add(Language.DE);
        LANGUAGES_FOR_WARM_UP.add(Language.EN);
        LANGUAGES_FOR_WARM_UP.add(Language.PT);
    }

    public enum CompanyType {
        ONLY_MBAG,
        ONLY_DTAG,
        ALL
    }

    private static CompanyType getCompanyType(boolean onlyCarAndVan, boolean onlyTruckAndBus) {
        if (onlyCarAndVan) {
            if (onlyTruckAndBus) {
                return CompanyType.ALL;
            } else {
                return CompanyType.ONLY_MBAG;
            }
        } else {
            if (onlyTruckAndBus) {
                return CompanyType.ONLY_DTAG;
            }
        }
        return CompanyType.ALL;
    }

    private static Map<String, DictTextCache> instances = new HashMap<>();
    private static Object warmUpDictTextCacheSyncObject = new Object();

    private static final String VM_PARAMETER_NO_WARMUP = "DW_iparts_no_dicttextcache_warmup";

    private volatile Map<CompanyType, Map<String, DictTextCacheEntry>> texts;
    private volatile boolean isDirty;
    private volatile boolean loadingTexts;
    private volatile boolean isCacheWithAllTextStates; // Handelt es sich um einen Cache, der auch nicht freigegebene Texte berücksichtigt
    private volatile String language;
    private volatile DictTextKindTypes textKind;

    public static boolean isTextKindWithCache(DictTextKindTypes textKind) {
        return (textKind != null) && TEXT_KIND_TYPES_FOR_WARM_UP.contains(textKind);
    }

    public static boolean isTextKindWithCacheAndRestrictedSearch(DictTextKindTypes textKind) {
        if (isTextKindWithCache(textKind)) {
            return TEXT_KIND_TYPES_FOR_RESTRICTED_SEARCH.contains(textKind);
        }
        return false;
    }

    private static String createHashObject(DictTextKindTypes textKind, String language, boolean isCacheWithAllTextStates) {
        if (textKind == DictTextKindTypes.NEUTRAL_TEXT) { // Bei sprachneutralen Texten immer Deutsch als Sprache nehmen
            language = Language.DE.getCode();
        }

        // Immer das globale EtkProject verwenden (dort kann auch nie ein ChangeSet aktiv sein)
        EtkDbs etkDbs = null;
        EtkProject project = iPartsPlugin.getMqProject();
        if (project != null) {
            etkDbs = project.getEtkDbs();
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "EtkProject is null for dictionary cache");
        }

        return CacheHelper.getPermanentDBCacheIdentifier(etkDbs, DictTextCache.class, textKind.name() + "@" + language + (isCacheWithAllTextStates ? "@WITH_ALL_TEXT_STATES" : ""), false);
    }

    /**
     * Wärmt den {@link DictTextCache} nach dem Neustart der WebApp bzw. dem Verändern von Texten für die definierten Textarten
     * in {@link #TEXT_KIND_TYPES_FOR_WARM_UP} und Sprachen in {@link #LANGUAGES_FOR_WARM_UP} in einem Hintergrund-Thread auf.
     */
    public static void warmUpCache() {
        // iPartsPlugin.isWarmUpCaches() stellt sicher, dass iPartsPlugin.getMqProject() != null
        if (!StartParameter.getSystemPropertyBoolean(VM_PARAMETER_NO_WARMUP, false) && iPartsPlugin.isWarmUpCaches()) {
            final EtkProject project = iPartsPlugin.getMqProject();
            iPartsPlugin.getMqSession().startChildThread(new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    synchronized (warmUpDictTextCacheSyncObject) { // Sicherstellen, dass nicht mehrere WarmUps parallel stattfinden
                        try {
                            Logger.log(iPartsPlugin.LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, "Warming up dictionary cache...");
                            long startTime = System.currentTimeMillis();
                            iPartsPlugin.assertProjectDbIsActive(project, "Dictionary cache warm up", iPartsPlugin.LOG_CHANNEL_DEBUG);

                            // Alle Instanzen für die gewünschten Textarten und Sprachen erzeugen falls notwendig
                            for (DictTextKindTypes textKind : TEXT_KIND_TYPES_FOR_WARM_UP) {
                                Map<String, DictTextCache> missingLanguages = new LinkedHashMap<>();
                                Map<String, DictTextCache> missingLanguagesWithAllTextStates = new LinkedHashMap<>();
                                synchronized (DictTextCache.class) { // entspricht dem synchronized von statischen Methoden
                                    for (Language language : LANGUAGES_FOR_WARM_UP) {
                                        boolean isNeutralText = textKind == DictTextKindTypes.NEUTRAL_TEXT;
                                        if (!isNeutralText || (language == Language.DE)) { // Bei sprachneutralen Texten nur Deutsch als Sprache nehmen
                                            DictTextCache textCache = instances.get(createHashObject(textKind, language.getCode(), false));

                                            // Noch nicht geladen oder nicht mehr aktuell (und nicht gerade schon beim Laden)
                                            // -> lade aus der Datenbank
                                            if ((textCache == null) || (((textCache.texts == null) || textCache.isDirty) && !textCache.loadingTexts)) {
                                                if (textCache != null) {
                                                    textCache.loadingTexts = true;
                                                }
                                                missingLanguages.put(language.getCode(), textCache);
                                            }

                                            if (isNeutralText) {
                                                textCache = instances.get(createHashObject(textKind, language.getCode(), true));

                                                // Noch nicht geladen oder nicht mehr aktuell (und nicht gerade schon beim Laden)
                                                // -> lade aus der Datenbank
                                                if ((textCache == null) || (((textCache.texts == null) || textCache.isDirty) && !textCache.loadingTexts)) {
                                                    if (textCache != null) {
                                                        textCache.loadingTexts = true;
                                                    }
                                                    missingLanguagesWithAllTextStates.put(language.getCode(), textCache);
                                                }
                                            }
                                        }
                                    }
                                }

                                // Texte für die fehlenden Sprachen laden
                                loadTextsForMissingLanguages(textKind, missingLanguages, false);
                                loadTextsForMissingLanguages(textKind, missingLanguagesWithAllTextStates, true);
                            }

                            String timeDurationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true,
                                                                                           false, Language.EN.getCode());
                            Logger.log(iPartsPlugin.LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, "Warming up dictionary cache finished in " + timeDurationString);
                        } catch (Exception e) {
                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                        }
                    }
                }

                private void loadTextsForMissingLanguages(DictTextKindTypes textKind, Map<String, DictTextCache> missingLanguages,
                                                          boolean isWithAllTextStates) {
                    if (!missingLanguages.isEmpty()) {
                        try {
                            // Freigegebene bzw. noch nicht freigegebene Texte
                            Map<String, Map<CompanyType, Map<String, DictTextCacheEntry>>> textsForLanguages = load(textKind,
                                                                                                                    missingLanguages.keySet(),
                                                                                                                    isWithAllTextStates);
                            if (textsForLanguages != null) {
                                for (Map.Entry<String, DictTextCache> missingLanguageEntry : missingLanguages.entrySet()) {
                                    boolean isNewTextCache = false;
                                    DictTextCache textCache = missingLanguageEntry.getValue();
                                    String missingLanguage = missingLanguageEntry.getKey();
                                    if (textCache == null) {
                                        isNewTextCache = true;
                                        textCache = new DictTextCache(textKind, missingLanguage);
                                        textCache.isCacheWithAllTextStates = isWithAllTextStates;
                                    }

                                    synchronized (DictTextCache.class) { // entspricht dem synchronized von statischen Methoden
                                        // Aktuelle Texte setzen und alte Texte entfernen
                                        textCache.texts = textsForLanguages.get(missingLanguage);
                                        textCache.isDirty = false;

                                        if (isNewTextCache) {
                                            instances.put(createHashObject(textKind, missingLanguage, isWithAllTextStates),
                                                          textCache);
                                        }
                                    }
                                }
                            }
                        } finally {
                            for (DictTextCache textCache : missingLanguages.values()) {
                                if (textCache != null) {
                                    textCache.loadingTexts = false;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Markiert den gesamten Cache für alle Textarten als ungültig, damit die Texte aktualisiert werden können.
     */
    public static synchronized void clearCache() {
        Set<String> cachesWithAllTextStates = new HashSet<>();
        for (DictTextCache textCache : instances.values()) {
            textCache.isDirty = true;
            cachesWithAllTextStates.add(createHashObject(textCache.textKind, textCache.language, true));
        }
        // Wird ein Cache auf ungültig gesetzt, sind die zusätzlichen Caches, die alle Texte enthalten auch ungültig
        // und müssen entfernt werden
        cachesWithAllTextStates.forEach(cacheWithAllTextStates -> instances.remove(cacheWithAllTextStates));
    }

    /**
     * Entfernt den {@link DictTextCache} aus der Sammlung aller Caches
     *
     * @param textKind
     * @param language
     * @param isCacheWithAllTextStates
     * @return
     */
    public static synchronized boolean removeCache(DictTextKindTypes textKind, String language, boolean isCacheWithAllTextStates) {
        return instances.remove(createHashObject(textKind, language, isCacheWithAllTextStates)) != null;
    }

    /**
     * Markiert den Cache für die übergebene Textart als ungültig, damit die Texte aktualisiert werden können, und startet
     * optional auch gleich den Cache Warm-Up.
     *
     * @param textKind
     * @param warmUpIfNecessary Flag, ob {@link #warmUpCache()} aufgerufen werden soll, wenn eine Textart aus dem WarmUp
     */
    public static synchronized void clearCacheForTextKind(DictTextKindTypes textKind, boolean warmUpIfNecessary) {
        String hashObjectPrefix = createHashObject(textKind, "", false);

        Set<String> cachesWithAllTextStates = new HashSet<>();
        for (Map.Entry<String, DictTextCache> dictTextCacheEntry : instances.entrySet()) {
            if (dictTextCacheEntry.getKey().startsWith(hashObjectPrefix)) {
                DictTextCache dictTextCache = dictTextCacheEntry.getValue();
                dictTextCache.isDirty = true;
                cachesWithAllTextStates.add(createHashObject(dictTextCache.textKind, dictTextCache.language, true));
            }
        }

        // Wird ein Cache auf ungültig gesetzt, sind die zusätzlichen Caches, die alle texte enthalten auch ungültig
        // und müssen entfernt werden
        cachesWithAllTextStates.forEach(cacheWithAllTextStates -> instances.remove(cacheWithAllTextStates));

        // Falls eine Textart aus dem WarmUp aus dem Cache entfernt wurde, den WarmUp erneut starten
        if (warmUpIfNecessary && TEXT_KIND_TYPES_FOR_WARM_UP.contains(textKind)) {
            warmUpCache();
        }
    }

    public static synchronized boolean hasInstance(DictTextKindTypes textKind, String language, boolean isCacheWithAllTextStates) {
        return instances.containsKey(createHashObject(textKind, language, isCacheWithAllTextStates));
    }

    /**
     * Liefert einen {@link DictTextCache}, der alle Texte zur Textart hält, unabhängig vom Status und erzeugt den Cache
     * falls er noch nicht existiert. Für die freigegebenen Texte wird intern ein "normaler" {@link DictTextCache} gehalten.
     * Alle anderen Texte werden zusätzlich geladen
     *
     * @param textKind
     * @param language
     * @return
     */
    public static DictTextCache getInstanceWithAllTextStates(DictTextKindTypes textKind, String language) {
        return getInstanceWithAllTextStates(textKind, language, true);
    }

    /**
     * Liefert einen {@link DictTextCache}, der alle Texte zur Textart hält, unabhängig vom Status. Für die freigegebenen
     * Texte wird intern ein "normaler" {@link DictTextCache} gehalten. Alle anderen Texte werden zusätzlich geladen
     *
     * @param textKind
     * @param language
     * @param createIfNotExists
     * @return
     */
    public static DictTextCache getInstanceWithAllTextStates(final DictTextKindTypes textKind, final String language, boolean createIfNotExists) {
        // Erst den "normalen" Cache laden
        getInstance(textKind, language);
        String hashObject = createHashObject(textKind, language, true);
        DictTextCache result;
        synchronized (DictTextCache.class) { // entspricht dem synchronized von statischen Methoden
            result = instances.get(hashObject);
        }
        if (createIfNotExists && (result == null)) {
            result = new DictTextCache(textKind, language);
            result.isCacheWithAllTextStates = true;
            instances.put(hashObject, result);
            result.loadNotReleasedTexts();
        }
        return result;
    }

    /**
     * Liefert einen {@link DictTextCache}, der alle freigegebenen Texte zur Textart hält und erzeugt den Cache falls er
     * noch nicht existiert.
     *
     * @param textKind
     * @param language
     * @return
     */
    public static DictTextCache getInstance(DictTextKindTypes textKind, String language) {
        return getInstance(textKind, language, true);
    }

    /**
     * Liefert einen {@link DictTextCache}, der alle freigegebenen Texte zur Textart hält.
     *
     * @param textKind
     * @param language
     * @param createIfNotExists
     * @return
     */
    // Die Methode nicht vollständig synchronized machen, weil das Laden aus der DB recht lange dauern kann und solange
    // alle anderen Aufrufe z.B. von getInstance() warten müssten
    public static DictTextCache getInstance(DictTextKindTypes textKind, String language, boolean createIfNotExists) {
        final String hashObject = createHashObject(textKind, language, false);
        DictTextCache result;
        synchronized (DictTextCache.class) { // entspricht dem synchronized von statischen Methoden
            result = instances.get(hashObject);
        }

        // Noch nicht geladen oder nicht mehr aktuell (und nicht gerade schon beim Laden) -> lade aus der Datenbank
        if (createIfNotExists) {
            if ((result == null) || (((result.texts == null) || result.isDirty) && !result.loadingTexts)) {
                final boolean isNewTextCache;
                if (result == null) {
                    isNewTextCache = true;
                    result = new DictTextCache(textKind, language);
                } else {
                    isNewTextCache = false;
                }
                loadTexts(textKind, language, isNewTextCache, hashObject, result, false);
            }
        }
        return result;
    }

    private static void loadTexts(DictTextKindTypes textKind, String language, boolean isNewTextCache, String hashObject,
                                  DictTextCache result, boolean loadNotReleasedTexts) {
        result.loadingTexts = true;
        // Aktualisierungen müssen in einem Thread gestartet werden, weil z.B. bei Suchen im Lexikon häufig die
        // aufrufenden Threads abgebrochen werden und damit auch das Laden der Texte abgebrochen werden würde
        FrameworkThread frameworkThread = Session.startChildThreadInSession(thread -> {
            try {
                // Texte für die übergebene Sprache laden
                Set<String> languages = new HashSet<>();
                languages.add(language);
                Map<String, Map<CompanyType, Map<String, DictTextCacheEntry>>> textsForLanguages = load(textKind, languages, loadNotReleasedTexts);
                if (textsForLanguages != null) {
                    synchronized (DictTextCache.class) { // entspricht dem synchronized von statischen Methoden
                        // Aktuelle Texte setzen und alte Texte entfernen
                        result.texts = textsForLanguages.get(language);
                        result.isDirty = false;

                        if (isNewTextCache) {
                            instances.put(hashObject, result);
                        }
                    }
                }
            } finally {
                result.loadingTexts = false;
            }
        });

        // Bei einem neuen TextCache muss gewartet werden bis dieser erstmalig fertig geladen wurde
        if (isNewTextCache) {
            frameworkThread.waitFinished();
        }
    }

    private static Map<String, Map<CompanyType, Map<String, DictTextCacheEntry>>> load(DictTextKindTypes textKind, Collection<String> languages, boolean loadNotReleasedText) {
        final Map<String, Map<CompanyType, Map<String, DictTextCacheEntry>>> textsForLanguages = new HashMap<>(); // Sprache -> Company -> Text auf Text-ID und Flags für die Sichtbarkeit
        for (String language : languages) {
            Map<CompanyType, Map<String, DictTextCacheEntry>> helpMap = new HashMap<>();
            textsForLanguages.put(language, helpMap);
            helpMap.put(CompanyType.ONLY_MBAG, new TreeMap());
            helpMap.put(CompanyType.ONLY_DTAG, new TreeMap());
            helpMap.put(CompanyType.ALL, new TreeMap());
        }

        // Immer das globale EtkProject verwenden (dort kann auch nie ein ChangeSet aktiv sein)
        final EtkProject project = iPartsPlugin.getMqProject();
        if (project == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "EtkProject is null for loading dictionary cache");
            return textsForLanguages;
        }
        iPartsPlugin.assertProjectDbIsActive(project, "Dictionary cache load", iPartsPlugin.LOG_CHANNEL_DEBUG);

        iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(project).getTxtKindId(textKind);
        if (textKindId == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_PERFORMANCE, LogType.ERROR, "Text kind " + textKind.name() + " not found for loading dictionary cache");
            return textsForLanguages;
        }

        Logger.log(iPartsPlugin.LOG_CHANNEL_PERFORMANCE, LogType.DEBUG, "Loading dictionary cache for text kind " + textKind.name()
                                                                        + " and languages " + StrUtils.stringListToString(languages, ", ")
                                                                        + "...");

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_SPRACH, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_BENENN, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TEXTID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_DICT_META, FIELD_DA_DICT_META_SOURCE, false, false));

        // Bedingung: textKind && metaState && (language1 || language2 || ... || languageN)
        String[][] whereTableAndFields = new String[3][];
        EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 0, TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_TXTKIND_ID));

        EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 1, TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_STATE));

        // Die OR-Bedingungen für die Sprachen gehen alle auf dasselbe Feld S_SPRACH
        List<String> fields = new DwList<>();
        List<String> values = new DwList<>();
        String fieldLanguage = TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH);
        for (String language : languages) {
            fields.add(fieldLanguage);
            values.add(language);
        }
        EtkDataObjectList.addElemsTo2dArray(whereTableAndFields, 2, ArrayUtil.toArray(fields));

        String[][] whereValues = new String[3][];
        EtkDataObjectList.addElemsTo2dArray(whereValues, 0, textKindId.getTextKindId());

        // Wenn alle nicht freigegebenen Texte geladen werden sollen, muss hier die Bedingung lauten: Status <> RELEASED
        String value = iPartsDictConst.DICT_STATUS_RELEASED;
        if (loadNotReleasedText) {
            value = EtkDataObjectList.getNotWhereValue(value);
        }
        EtkDataObjectList.addElemsTo2dArray(whereValues, 1, value);

        EtkDataObjectList.addElemsTo2dArray(whereValues, 2, ArrayUtil.toArray(values));

        EtkDataTextEntryList searchList = new EtkDataTextEntryList();
        searchList.setSearchWithoutActiveChangeSets(true);
        if (searchList.searchSortAndFillWithJoin(project, null, selectFields, whereTableAndFields,
                                                 whereValues, false, null, null, false, null, false, false, false,
                                                 new EtkDataObjectList.FoundAttributesCallback() {
                                                     @Override
                                                     public boolean foundAttributes(DBDataObjectAttributes attributes) {
                                                         String language = attributes.getFieldValue(FIELD_S_SPRACH);
                                                         String text = attributes.getFieldValue(FIELD_S_BENENN);
                                                         String source = attributes.getFieldValue(FIELD_DA_DICT_META_SOURCE);

                                                         Map<CompanyType, Map<String, DictTextCacheEntry>> textsForLanguage = textsForLanguages.get(language);
                                                         if (textsForLanguage != null) { // TreeMap muss für die gewünschten Sprachen existieren
                                                             DictTextCacheEntry entry = new DictTextCacheEntry(attributes.getFieldValue(FIELD_DA_DICT_META_TEXTID),
                                                                                                               source);
                                                             CompanyType companyType = entry.getCompanyType();

                                                             // Text-ID für Quelle iParts hat Vorrang
                                                             boolean addText = false;
                                                             if (!textsForLanguage.get(companyType).containsKey(text)) {
                                                                 addText = true;
                                                             } else {
                                                                 if (source.startsWith(DictHelper.getIPartsSource())) {
                                                                     addText = true;
                                                                 }
                                                             }
                                                             if (addText) {
                                                                 Map<String, DictTextCacheEntry> texts = textsForLanguage.get(companyType);
                                                                 texts.put(text, entry);
                                                             }
                                                         }

                                                         return false;
                                                     }
                                                 }, true,
                                                 new EtkDataObjectList.JoinData(iPartsConst.TABLE_DA_DICT_META,
                                                                                new String[]{ FIELD_S_TEXTID },
                                                                                new String[]{ iPartsConst.FIELD_DA_DICT_META_TEXTID },
                                                                                false, false))) {
            return textsForLanguages;
        } else {
            return null; // Suche wurde abgebrochen
        }
    }

    /**
     * Fügt zur übergebenen Textart die Texte vom übergebenen {@link EtkMultiSprache} für alle bereits vorhandenen Sprachen
     * im Cache hinzu.
     *
     * @param textKind
     * @param multiLang
     * @param source
     */
    public static synchronized void addText(DictTextKindTypes textKind, EtkMultiSprache multiLang, String source) {
        String hashObjectPrefix = createHashObject(textKind, "", false);
        for (Map.Entry<String, DictTextCache> dictTextCacheEntry : instances.entrySet()) {
            if (dictTextCacheEntry.getKey().startsWith(hashObjectPrefix)) {
                String language = dictTextCacheEntry.getKey().substring(hashObjectPrefix.length()); // Der Key endet mit der Sprache
                if (multiLang.spracheExists(language)) {
                    DictTextCache dictTextCache = dictTextCacheEntry.getValue();
                    dictTextCache.addText(multiLang.getText(language), multiLang.getTextId(), source, true);
                }
            }
        }
    }

    public synchronized boolean addText(String text, String textId, iPartsImportDataOrigin source, boolean overwrite) {
        if ((source == null) || (source == iPartsImportDataOrigin.UNKNOWN)) {
            return false;
        }
        return addText(text, textId, source.getOrigin(), overwrite);
    }

    /**
     * Fügt einen Text dem Cache hinzu. Optional kann angegeben werden, dass bisherige Einträge überschrieben werden
     *
     * @param text
     * @param textId
     * @param source
     * @return
     */
    public synchronized boolean addText(String text, String textId, String source, boolean overwrite) {
        if (!StrUtils.isValid(text, textId, source)) {
            return false;
        }
        // in die richtige Company-Cache-Map einhängen
        DictTextCacheEntry cacheEntry = new DictTextCacheEntry(textId, source);
        CompanyType companyType = cacheEntry.getCompanyType();
        Map<CompanyType, Map<String, DictTextCacheEntry>> localTexts = texts;
        if (localTexts != null) {
            Map<String, DictTextCacheEntry> localTextElems = localTexts.get(companyType);
            if ((localTextElems != null) && (overwrite || !localTextElems.containsKey(text))) {
                localTexts = new HashMap<>(localTexts); // Kopie erzeugen und darauf put(), um synchronized zu vermeiden
                localTextElems = new TreeMap<>(localTextElems); // Kopie erzeugen und darauf put(), um synchronized zu vermeiden
                localTextElems.put(text, cacheEntry);
                localTexts.put(companyType, localTextElems);
                texts = localTexts;
                return true;
            }
        }
        return false;
    }

    public synchronized boolean removeText(String text, String textId, iPartsImportDataOrigin source) {
        if ((source == null) || (source == iPartsImportDataOrigin.UNKNOWN)) {
            return false;
        }
        return removeText(text, textId, source.getOrigin());
    }

    public synchronized boolean removeText(String text, String textId, String source) {
        if (!StrUtils.isValid(text, textId, source)) {
            return false;
        }
        DictTextCacheEntry cacheEntry = new DictTextCacheEntry(textId, source);
        CompanyType companyType = cacheEntry.getCompanyType();
        Map<CompanyType, Map<String, DictTextCacheEntry>> localTexts = texts;
        if (localTexts != null) {
            Map<String, DictTextCacheEntry> localTextElems = localTexts.get(companyType);
            if ((localTextElems != null) && localTextElems.containsKey(text)) {
                DictTextCacheEntry entry = localTextElems.get(text);
                if (entry.equals(cacheEntry)) {
                    localTexts = new HashMap<>(localTexts); // Kopie erzeugen und darauf put(), um synchronized zu vermeiden
                    localTextElems = new TreeMap<>(localTextElems); // Kopie erzeugen und darauf put(), um synchronized zu vermeiden
                    localTextElems.remove(text);
                    localTexts.put(companyType, localTextElems);
                    texts = localTexts;
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized boolean changeText(String text, String textId, iPartsImportDataOrigin source) {
        if ((source == null) || (source == iPartsImportDataOrigin.UNKNOWN)) {
            return false;
        }
        return changeText(text, textId, source.getOrigin());
    }

    public synchronized boolean changeText(String text, String textId, String source) {
        if (!StrUtils.isValid(text, textId, source)) {
            return false;
        }
        DictTextCacheEntry cacheEntry = new DictTextCacheEntry(textId, source);
        CompanyType companyType = cacheEntry.getCompanyType();
        Map<CompanyType, Map<String, DictTextCacheEntry>> localTexts = texts;
        if (localTexts != null) {
            Map<String, DictTextCacheEntry> localTextElems = localTexts.get(companyType);
            if (localTextElems != null) {
                String foundText = null;
                // hier nach textId suchen, da sich ja der Text geändert hat
                for (Map.Entry<String, DictTextCacheEntry> entry : localTextElems.entrySet()) {
                    if (entry.getValue().textId.equals(textId)) {
                        if (!entry.getKey().equals(text)) {
                            foundText = entry.getKey();
                        }
                        break;
                    }
                }
                if (foundText != null) {
                    localTexts = new HashMap<>(localTexts); // Kopie erzeugen und darauf put(), um synchronized zu vermeiden
                    localTextElems = new TreeMap<>(localTextElems); // Kopie erzeugen und darauf put(), um synchronized zu vermeiden
                    localTextElems.remove(foundText);
                    localTextElems.put(text, cacheEntry);
                    localTexts.put(companyType, localTextElems);
                    texts = localTexts;
                    return true;
                }
            }
        }
        return false;
    }

    private DictTextCache(DictTextKindTypes textKind, String language) {
        this.textKind = textKind;
        this.language = language;
    }

    /**
     * Liefert alle Texte für die zu diesem Cache gehörenden Textart und Sprache zurück.
     *
     * @return {@link Map} von allen Texten auf deren Text-IDs
     */
    public Map<String, DictTextCacheEntry> getAllTextsToTextIds() {
        List<CompanyType> searchElems = fillSearchElemList(null, null);
        Map<String, DictTextCacheEntry> allTexts;
        if (isCacheWithAllTextStates) {
            // Handelt es sich um einen Cache, der alle Texte enthält (unabhängig vom Status) muss man die Texte
            // des "normalen" Caches mit den Texten aus diesem Cache zusammenlegen
            allTexts = getInstance(textKind, language).getAllTextsToTextIds();
            allTexts = new HashMap<>(allTexts);
            // Alle nicht freigegebenen Texte laden (sofern noch nicht geschehen)
            loadNotReleasedTexts();
        } else {
            allTexts = new HashMap<>();
        }
        Map<CompanyType, Map<String, DictTextCacheEntry>> localTexts = texts;
        if (localTexts != null) {
            for (CompanyType companyType : searchElems) {
                allTexts.putAll(localTexts.get(companyType));
            }
        }
        return Collections.unmodifiableMap(allTexts);
    }

    public Map<String, String> searchTexts(String searchText) {
        return searchTexts(searchText, false);
    }

    /**
     * Sucht in den Texten der zu diesem Cache gehörenden Textart und Sprache nach dem übergebenen Text, der auch Wildcards
     * enthalten kann inkl. Berücksichtigung der Eigenschaften des Benutzers der aktuellen Session.
     *
     * @param searchText
     * @return {@link Map} von gefundenen Texten auf deren Text-IDs
     */
    public Map<String, String> searchTexts(String searchText, boolean restricted) {
        // Falls es sich um einen Cache mit allen Texten handelt, muss zuerst im normalen Cache nach möglichen Treffer
        // gesucht werden. Werden keine gefunden, wird bei den "nicht freigegebenen" Texten gesucht
        Map<String, String> searchResults;
        if (isCacheWithAllTextStates) {
            DictTextCache releasedCache = getInstance(textKind, language);
            searchResults = releasedCache.searchTexts(searchText, restricted);
            if (!searchResults.isEmpty()) {
                return searchResults;
            }
            loadNotReleasedTexts();
        } else {
            searchResults = new TreeMap<>(); // Die Einträge sind bereits sortiert in der Map texts
        }
        String minimumSearchText = searchText.toLowerCase();

        // Pattern einmalig erzeugen für bessere Performance; caseSensitive reicht hier aus, weil wir sowieso mit toLowerCase() arbeiten
        Pattern searchPattern = StrUtils.createPatternSqlLike(minimumSearchText, true, '*', '?');

        // Suche die Wildcard, die am weitesten links steht, um diesen Präfix vom Suchtext mit einem schnelle startsWith()
        // gegen die Texte prüfen zu können
        int multiWildcardIndex = minimumSearchText.indexOf('*');
        if (multiWildcardIndex == -1) { // Ohne Wildcard die gesamte Textlänge betrachten
            multiWildcardIndex = minimumSearchText.length();
        }
        int singleWildcardIndex = minimumSearchText.indexOf('?');
        if (singleWildcardIndex == -1) { // Ohne Wildcard die gesamte Textlänge betrachten
            singleWildcardIndex = minimumSearchText.length();
        }
        int wildcardIndex = Math.min(multiWildcardIndex, singleWildcardIndex);
        if (wildcardIndex > 0) { // Bei Wildcard an erster Stelle, kann kein startsWith() durchgeführt werden
            minimumSearchText = minimumSearchText.substring(0, wildcardIndex);
        }

        Map<CompanyType, Map<String, DictTextCacheEntry>> localTexts = texts;
        if (localTexts == null) { // Kann eigentlich nicht passieren, weil texts immer gesetzt sein muss
            return searchResults;
        }

        Boolean carAndVanInSession = iPartsRight.checkCarAndVanInSession();
        Boolean truckAndBusInSession = iPartsRight.checkTruckAndBusInSession();
        List<CompanyType> searchElems = fillSearchElemList(carAndVanInSession, truckAndBusInSession);
        // alle betroffenen Company-Caches durchsuchen
        for (CompanyType companyType : searchElems) {
            Map<String, DictTextCacheEntry> localTextElems = localTexts.get(companyType);
            for (Map.Entry<String, DictTextCacheEntry> textEntry : localTextElems.entrySet()) {
                String textLowerCase = textEntry.getKey().toLowerCase();

                // Bei Wildcard an erster Stelle, kann kein startsWith() durchgeführt werden; ansonsten zunächst schnelles
                // Prüfen mittels startsWith() für den Präfix bis zur ersten Wildcard und nur bei Erfolg Prüfung mit dem Pattern
                DictTextCacheEntry dictTextCacheEntry = textEntry.getValue();
                if (restricted && !iPartsImportDataOrigin.isSourceValidForDictCacheCombTexts(dictTextCacheEntry.getDataOrigin())) {
                    continue;
                }
                if (((wildcardIndex == 0) || textLowerCase.startsWith(minimumSearchText)) && searchPattern.matcher(textLowerCase).matches()
                    && dictTextCacheEntry.checkVisibilityInSession(carAndVanInSession, truckAndBusInSession)) {
                    searchResults.put(textEntry.getKey(), dictTextCacheEntry.getTextId());
                }
            }
        }

        return searchResults;
    }

    private List<CompanyType> fillSearchElemList(Boolean carAndVanInSession, Boolean truckAndBusInSession) {
        if (carAndVanInSession == null) {
            carAndVanInSession = iPartsRight.checkCarAndVanInSession();
        }
        if (truckAndBusInSession == null) {
            truckAndBusInSession = iPartsRight.checkTruckAndBusInSession();
        }
        List<CompanyType> searchElems = new DwList<>();
        switch (getCompanyType(carAndVanInSession, truckAndBusInSession)) {
            case ONLY_MBAG:
                searchElems.add(CompanyType.ONLY_MBAG);
                searchElems.add(CompanyType.ALL);
                break;
            case ONLY_DTAG:
                searchElems.add(CompanyType.ONLY_DTAG);
                searchElems.add(CompanyType.ALL);
                break;
            case ALL:
                searchElems.add(CompanyType.ONLY_DTAG);
                searchElems.add(CompanyType.ONLY_MBAG); // MBAG vor DTAG bei doppelten (bei Verwendung der searchElemList werden vorhandene DTAG-Entries durch MBAG überschrieben)
                searchElems.add(CompanyType.ALL);
                break;

        }
        return searchElems;
    }

    /**
     * Lädt alle nicht freigegebenen Texte für diesen Cache
     */
    private void loadNotReleasedTexts() {
        if (isCacheWithAllTextStates && (texts == null)) {
            String hashObject = createHashObject(textKind, language, true);
            loadTexts(textKind, language, true, hashObject, this, true);
        }
    }

    /**
     * Ein Text-Cache-Eintrag mit Text-ID und Flags bzgl. der Sichtbarkeit abhängig von Benutzer-Eigenschaften
     */
    public static class DictTextCacheEntry {

        private String textId;
        private iPartsImportDataOrigin dataOrigin;
        private boolean onlyCarAndVan;
        private boolean onlyTruckAndBus;

        public DictTextCacheEntry(String textId, String source) {
            this.textId = textId;
            this.dataOrigin = iPartsImportDataOrigin.getTypeFromCode(source);
            this.onlyCarAndVan = isCarVanSourceValidForDictionary(this.dataOrigin);
            this.onlyTruckAndBus = isTruckBusSourceValidForDictionary(this.dataOrigin);
        }

        public String getTextId() {
            return textId;
        }

        public iPartsImportDataOrigin getDataOrigin() {
            return dataOrigin;
        }

        public boolean checkVisibilityInSession(boolean carAndVanInSession, boolean truckAndBusInSession) {
            if (onlyCarAndVan && !carAndVanInSession) {
                return false;
            }
            if (onlyTruckAndBus && !truckAndBusInSession) {
                return false;
            }

            return true;
        }

        public CompanyType getCompanyType() {
            return DictTextCache.getCompanyType(onlyCarAndVan, onlyTruckAndBus);
        }

        @Override
        public boolean equals(Object obj) {
            if (super.equals(obj)) {
                return true;
            }
            if (obj instanceof DictTextCacheEntry) {
                DictTextCacheEntry testObj = (DictTextCacheEntry)obj;
                return (textId.equals(testObj.textId) && (dataOrigin == testObj.dataOrigin) &&
                        (onlyCarAndVan == testObj.onlyCarAndVan) && (onlyTruckAndBus == testObj.onlyTruckAndBus));
            }
            return false;
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(textId);
            hcb.append(dataOrigin.getOrigin());
            hcb.append(onlyCarAndVan);
            hcb.append(onlyTruckAndBus);
            return hcb.toHashCode();
        }

        private boolean isCarVanSourceValidForDictionary(String dbValue) {
            return isCarVanSourceValidForDictionary(iPartsImportDataOrigin.getTypeFromCode(dbValue));
        }

        private boolean isCarVanSourceValidForDictionary(iPartsImportDataOrigin importDataOrigin) {
            return VALID_CAR_AND_VAN_SOURCES_FOR_DICTIONARY.contains(importDataOrigin);
        }

        private boolean isTruckBusSourceValidForDictionary(String dbValue) {
            return isTruckBusSourceValidForDictionary(iPartsImportDataOrigin.getTypeFromCode(dbValue));
        }

        private boolean isTruckBusSourceValidForDictionary(iPartsImportDataOrigin importDataOrigin) {
            return VALID_TRUCK_AND_BUS_SOURCES_FOR_DICTIONARY.contains(importDataOrigin);
        }

    }
}