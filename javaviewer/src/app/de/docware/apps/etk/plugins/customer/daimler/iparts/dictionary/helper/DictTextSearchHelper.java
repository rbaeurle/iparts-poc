/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataTextEntry;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Hilfsklasse zum Suchen von Texten im Lexikon
 */
public class DictTextSearchHelper implements iPartsConst {

    public static Set<String> searchTextIdsInTableSprache(EtkProject project, String searchText, Language language) {
        DictTextSearchHelper searchHelper = new DictTextSearchHelper(project);
        return searchHelper.findTextIdsInTableSprache(searchText, language, DictHelper.buildDictTextId("*"));
    }

    public static EtkDataTextEntryList searchTextAttributesInTableSprache(EtkProject project, String searchText,
                                                                          String textId, Language language) {
        DictTextSearchHelper searchHelper = new DictTextSearchHelper(project);
        return searchHelper.searchTextInTableSprache(searchText, language, null, null,
                                                     textId, iPartsImportDataOrigin.UNKNOWN, false);
    }

    public static EtkDataTextEntryList searchDictionaryTextAttributesInTableSprache(EtkProject project, String searchText,
                                                                                    String[] extraTableAndFieldnames, String[] extraValues,
                                                                                    Language language) {
        return searchTextAttributesInTableSprache(project, searchText, extraTableAndFieldnames, extraValues,
                                                  DictHelper.buildDictTextId("*"), language, false);

    }

    public static EtkDataTextEntryList searchTextAttributesInTableSprache(EtkProject project, String searchText,
                                                                          String[] extraTableAndFieldnames,
                                                                          String[] extraValues, String searchTextIdMask,
                                                                          Language language, boolean allowTextWithoutSource) {
        DictTextSearchHelper searchHelper = new DictTextSearchHelper(project);
        return searchHelper.searchTextInTableSprache(searchText, language, extraTableAndFieldnames, extraValues,
                                                     searchTextIdMask, iPartsImportDataOrigin.UNKNOWN, allowTextWithoutSource);
    }

    public static EtkDataTextEntryList searchFootNoteTextAttributesInTableSprache(EtkProject project, String searchText,
                                                                                  Language language) {
        return searchDictionaryTextAttributesInTableSprache(project, searchText,
                                                            new String[]{ TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_FELD) },
                                                            new String[]{ TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT) },
                                                            language);
    }

    private final EtkProject project;
    private boolean isCancelled; // Zum Beenden einer laufenden Suche

    public DictTextSearchHelper(EtkProject project) {
        this.project = project;
    }

    public EtkProject getProject() {
        return project;
    }

    /**
     * Sucht nach einem Suchtext im Lexikon und liefert {@link iPartsDataDictMeta} Objekte zurück, die komplett befüllt
     * wurden. D.h. es sind alle {@link iPartsDataDictLanguageMeta} Objekte zum befüllten {@link EtkMultiSprache} Objekt
     * vorhanden, das wiederum innerhalb des {@link iPartsDataDictMeta} platziert wird.
     * <p>
     * Somit muss weder das {@link iPartsDataDictMeta} noch die darin enthaltenen Objekte neu geladen werden
     *
     * @param searchText
     * @param searchTextLanguage
     * @param source
     * @param dictTextKindType
     * @param tableAndFieldName
     * @return
     */
    public iPartsDataDictMetaList searchDictTextsWithSource(String searchText, Language searchTextLanguage,
                                                            iPartsImportDataOrigin source, DictTextKindTypes dictTextKindType,
                                                            String tableAndFieldName) {
        // Textart Objekt bestimmen um zu prüfen, ob die Textart gültig ist
        iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(dictTextKindType, tableAndFieldName);
        if ((textKindId == null) || !textKindId.isValidId()) {
            return null;
        }
        // Text auf max. Größe des Feldes begrenzen. Das muss gemacht werden, weil wir für die schnelle Suche nur in
        // S_BENENN suchen. Sollte der eigentliche Text länger sein (länger als 300 Zeichen), dann ist er ja in S_BENENN_LANG
        // vorhanden.
        String searchTextShort = getProject().getEtkDbs().textTruncateToFieldSize(searchText, TABLE_SPRACHE, FIELD_S_BENENN);
        if (StrUtils.isEmpty(searchTextShort)) {
            return null;
        }
        iPartsDataDictMetaList dictMetaList = new iPartsDataDictMetaList();
        // Alle nicht BLOB Felder aus DA_DICT_META (iPartsDataDictMeta), DA_DICT_SPRACHE (iPartsDataDictLanguageMeta)
        // und SPRACHE (EtkMultiSprache) als Select Felder verwenden
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFelder(getProject().getAllDisplayFieldsForTable(TABLE_DA_DICT_META));
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_TEXTID, false, false));


        // Wir suchen nach einer festen Textart, einer festen Quelle,  einem festen Feld und einem festen Suchtext zu
        // einer festen Sprache. Durch einen Join von DA_DICT_META auf SPRACHE bekommen wir raus, ob es zu diesen
        // festen Kriterien Treffer gibt. Um für diesen sehr spezifischen Treffer jedoch alle Texte in allen Sprachen zu
        // erhalten, wird danach ein separates SQL-Statement mit Join auf SPRACHE benötigt auf Basis der Text-Id im spezifischen
        // Treffer (ein Join von SPRACHE auf sich selbst über die Text-ID direkt im ersten Join ist bei Daimler leider extrem langsam).
        // Dadurch erhalten wir jeden Treffer komplett mit allen Sprachen. Um das iPartsDataDictMeta komplett zu befüllen
        // wir zum Schluss noch ein Join auf die DA_DICT_META_SPRACH Tabelle gemacht.
        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TXTKIND_ID),
                                             TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_SOURCE),
                                             TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_FELD),
                                             TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN),
                                             TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH) };
        String[] whereValues = new String[]{ textKindId.getTextKindId(), source.getOrigin(), tableAndFieldName, searchTextShort, searchTextLanguage.getCode() };

        dictMetaList.searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues, false, null, false,
                                               false, null,
                                               new EtkDataObjectList.JoinData(TABLE_SPRACHE,
                                                                              new String[]{ FIELD_DA_DICT_META_TEXTID },
                                                                              new String[]{ FIELD_S_TEXTID },
                                                                              false, false));

        // Map mit allen TextIds auf die zugehörigen Textobjekte
        Map<String, EtkMultiSprache> multiLangMap = new HashMap<>();
        // Map mit allen iPartsDictMetaId auf ihre iPartsDataDictmeta Objekte
        Map<iPartsDictMetaId, iPartsDataDictMeta> allMetaObjects = new HashMap<>();

        // Callback für jeden Treffer erzeugen
        iPartsDataDictMetaList dictMetaListWithAllLanguages = new iPartsDataDictMetaList();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback
                = createAttributesCallbackForSearchWithSource(searchText, textKindId, allMetaObjects, multiLangMap, dictMetaListWithAllLanguages);

        // Alle nicht BLOB Felder aus SPRACHE und DA_DICT_SPRACHE (iPartsDataDictLanguageMeta) als Select Felder verwenden
        selectFields.clear();
        selectFields.addFelder(getProject().getAllDisplayFieldsForTable(TABLE_SPRACHE));
        selectFields.addFelder(getProject().getAllDisplayFieldsForTable(TABLE_DA_DICT_SPRACHE));

        // Alle Sprachen pro Text-ID laden
        for (iPartsDataDictMeta dataDictMeta : dictMetaList) {
            if (isCancelled) {
                break;
            }

            String textID = dataDictMeta.getFieldValue(FIELD_S_TEXTID);
            if (!textID.isEmpty()) {
                EtkDataTextEntryList textEntryList = new EtkDataTextEntryList();
                textEntryList.searchSortAndFillWithJoin(project, null, selectFields,
                                                        new String[]{ FIELD_S_FELD, FIELD_S_TEXTID },
                                                        new String[]{ tableAndFieldName, textID },
                                                        false, null, false, false, null,
                                                        new EtkDataObjectList.JoinData(TABLE_DA_DICT_SPRACHE,
                                                                                       new String[]{ FIELD_S_SPRACH,
                                                                                                     FIELD_S_TEXTID },
                                                                                       new String[]{ FIELD_DA_DICT_SPRACHE_SPRACH,
                                                                                                     FIELD_DA_DICT_SPRACHE_TEXTID },
                                                                                       false, false));

                // Für jede Sprache einen neuen Datensatz anlegen, der auch alle Metadaten aus dataDictMeta enthält
                for (EtkDataTextEntry textEntry : textEntryList) {
                    if (isCancelled) {
                        break;
                    }

                    iPartsDataDictMeta dataDictMetaWithTextEntry = dataDictMeta.cloneMe(project);
                    dataDictMetaWithTextEntry.assignAttributesValues(project, textEntry.getAttributes(), false, DBActionOrigin.FROM_DB);

                    // dictMetaListWithAllLanguages wird vom foundAttributesCallback befüllt
                    foundAttributesCallback.foundAttributes(dataDictMetaWithTextEntry.getAttributes());
                }
            }
        }

        for (iPartsDataDictMeta dictMeta : dictMetaListWithAllLanguages) {
            if (isCancelled) {
                break;
            }
            // Die erzeugten EtkMultiLang Objekte bei den zugehörigen iPartsDataDictMeta Objekten setzen
            dictMeta.setNewMultiLangFromDB(multiLangMap.get(dictMeta.getAsId().getTextId()));
        }

        return dictMetaListWithAllLanguages;
    }

    /**
     * Erzeugt einen {@link de.docware.apps.etk.base.project.base.EtkDataObjectList.FoundAttributesCallback} in dem jeder
     * Suchtreffer verarbeitet wird.
     * <p>
     * Jeder Treffer repräsentiert einen Treffer pro Sprache zum Suchtext. Zusätzlich werden {@link iPartsDataDictMeta}
     * und {@link iPartsDataDictLanguageMeta} Objekte erzeugt und in einzelnen {@link iPartsDataDictMeta} Objekten
     * zusammengeführt.
     *
     * @param searchText
     * @param textKindId
     * @param allMetaObjects
     * @param multiLangMap
     * @param dictMetaList
     * @return
     */
    private EtkDataObjectList.FoundAttributesCallback createAttributesCallbackForSearchWithSource(String searchText, iPartsDictTextKindId textKindId,
                                                                                                  Map<iPartsDictMetaId, iPartsDataDictMeta> allMetaObjects,
                                                                                                  Map<String, EtkMultiSprache> multiLangMap,
                                                                                                  iPartsDataDictMetaList dictMetaList) {
        return new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                if (isCancelled) {
                    return false;
                }
                // Den echten Text aus dem Treffer bestimmen (Langtext oder max. 300 Zeichen lang). Hier unbedingt den
                // deutschen Text für den JOIN auf DA_DICT_META mit dem Suchtext vergleichen. Sonst werden Texte zu
                // unterschiedlichen Texten verglichen
                String longTextFromAttributes = getProject().getEtkDbs().getLongTextFromAttributes(attributes, FIELD_S_BENENN,
                                                                                                   FIELD_S_BENENN_LANG);

                // Wenn der Suchtext nicht übereinstimmt -> Datensatz nicht beachten
                if (!searchText.equals(longTextFromAttributes)) {
                    return false;
                }
                // Text-Id aus der SPRACHE Tabellen bestimmen
                String textId = attributes.getFieldValue(FIELD_S_TEXTID);
                if (StrUtils.isEmpty(textId)) {
                    return false;
                }
                // Ein zentrales iPartsDataDictMeta Objekt für alle Treffer zur gefundenen Text-Id
                iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), textId);
                iPartsDataDictMeta dictMeta = allMetaObjects.get(dictMetaId);
                if (dictMeta == null) {
                    // Objekt erzeugen und ablegen (in der Ergebnisliste und in der Map)
                    dictMeta = dictMetaList.addAndFillCompleteDictMetaFromAttributes(getProject(), attributes);
                    allMetaObjects.put(dictMetaId, dictMeta);
                }
                // iPartsDataDictLanguageMeta erzeugen und setzen
                String language = attributes.getFieldValue(FIELD_S_SPRACH);
                addDictLanguageObject(textId, language, attributes, dictMeta);

                // Zur Text-Id ein EtkMultiSprache Objekt erzeugen und in der Map ablegen
                EtkMultiSprache multiLang = multiLangMap.computeIfAbsent(textId, t -> new EtkMultiSprache(textId));
                String realTextForLanguage = getProject().getEtkDbs().getLongTextFromAttributes(attributes);
                if (StrUtils.isValid(realTextForLanguage, language)) {
                    multiLang.setText(Language.findLanguage(language), realTextForLanguage);
                }
                return false;
            }
        };
    }

    /**
     * Erzeugt auf Basis eines Suchtreffers ein {@link iPartsDataDictLanguageMeta} und fügt es dem zugehörigen
     * {@link iPartsDataDictMeta} Objekt hinzu.
     *
     * @param textId
     * @param language
     * @param attributes
     * @param dictMeta
     */
    private void addDictLanguageObject(String textId, String language, DBDataObjectAttributes attributes, iPartsDataDictMeta dictMeta) {
        iPartsDataDictLanguageMeta dictLanguageMeta = new iPartsDataDictLanguageMeta(getProject(), new iPartsDictLanguageMetaId(textId, language));
        dictLanguageMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        dictLanguageMeta.setFieldValue(FIELD_DA_DICT_SPRACHE_CREATE, attributes.getFieldValue(FIELD_DA_DICT_SPRACHE_CREATE), DBActionOrigin.FROM_DB);
        dictLanguageMeta.setFieldValue(FIELD_DA_DICT_SPRACHE_CHANGE, attributes.getFieldValue(FIELD_DA_DICT_SPRACHE_CHANGE), DBActionOrigin.FROM_DB);
        dictLanguageMeta.setState(attributes.getFieldValue(FIELD_DA_DICT_SPRACHE_STATUS), DBActionOrigin.FROM_DB);
        dictLanguageMeta.setFieldValue(FIELD_DA_DICT_SPRACHE_TRANS_JOBID, attributes.getFieldValue(FIELD_DA_DICT_SPRACHE_TRANS_JOBID), DBActionOrigin.FROM_DB);
        dictLanguageMeta.setFieldValue(FIELD_DA_DICT_SPRACHE_TRANS_STATE, attributes.getFieldValue(FIELD_DA_DICT_SPRACHE_TRANS_STATE), DBActionOrigin.FROM_DB);
        dictMeta.addLanguageFromDB(dictLanguageMeta);
    }

    public Set<String> findTextIdsInTableSprache(String searchText, Language language, String searchTextIdMask) {
        EtkDataTextEntryList list = searchTextInTableSprache(searchText, language, searchTextIdMask);
        Set<String> result = new HashSet<>();
        for (EtkDataTextEntry textEntry : list) {
            String textId = textEntry.getFieldValue(FIELD_S_TEXTID);
            if (!textId.isEmpty()) {
                result.add(textId);
            }
        }
        return result;
    }


    public Set<String> findRegularTextIdsInTableSprache(String searchText, Language language) {
        return findTextIdsInTableSprache(searchText, language, DictHelper.buildDictTextId("*"));
    }

    public Set<String> findTextIdsInTableSprache(String searchText, Language language, DictTextKindRSKTypes importType) {
        String searchTextId = DictHelper.getRSKTextId(importType, "*");
        if (searchTextId != null) {
            return findTextIdsInTableSprache(searchText, language, searchTextId);
        }
        return new HashSet<>();
    }

    public Set<String> findPRIMUSTextIdsInTableSprache(String searchText, Language language) {
        String searchTextId = DictHelper.buildDictPRIMUSTextId("*");
        if (searchTextId != null) {
            return findTextIdsInTableSprache(searchText, language, searchTextId);
        }
        return new HashSet<>();
    }

    public Set<String> findTextIdsInTableSpracheForEPC(String searchText, Language language, DictTextKindEPCTypes importType) {
        String searchTextId = DictHelper.buildEPCTextId(importType, "*");
        if (searchTextId != null) {
            return findTextIdsInTableSprache(searchText, language, searchTextId);
        }
        return new HashSet<>();
    }

    private EtkDataTextEntryList searchTextInTableSprache(String searchText, Language language, String searchTextIdMask) {
        return searchTextInTableSprache(searchText, language, null, null, searchTextIdMask,
                                        iPartsImportDataOrigin.UNKNOWN, false);
    }

    private EtkDataTextEntryList searchTextInTableSprache(String searchText, Language language,
                                                          String[] extraTableAndFieldnames, String[] extraValues,
                                                          String searchTextIdMask, iPartsImportDataOrigin source,
                                                          boolean allowTextWithoutSource) {

        EtkDisplayFields selectFields = new EtkDisplayFields();
        // Benötigte Felder der SPRACHE-Tabelle
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_TEXTID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_BENENN, false, false));
        if (getProject().getConfig().getDataBaseVersion() >= 6.2) {
            selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_BENENN_LANG, false, false));
        }

        // whereFields für die Sprache und den Suchtext; die Text-Id wird erst im Callback geprüft, damit keine like-Abfrage
        // (v.a. für den Suchtext) benötigt wird
        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH),
                                             TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN) };

        // Zusätzliche Where-Felder hinzufügen
        if ((extraTableAndFieldnames != null) && (extraTableAndFieldnames.length > 0)) {
            whereFields = StrUtils.mergeArrays(whereFields, extraTableAndFieldnames);
        }

        // Suchtext auf max. Größe des Feldes begrenzen
        final EtkDbs etkDbs = getProject().getEtkDbs();
        String searchTextShort = etkDbs.textTruncateToFieldSize(searchText, TABLE_SPRACHE, FIELD_S_BENENN);

        // Where-Werte aufsammeln
        String[] whereValues = new String[]{ language.getCode(), searchTextShort };

        // Zusätzliche Where-Werte hinzufügen
        if ((extraValues != null) && (extraValues.length > 0)) {
            whereValues = StrUtils.mergeArrays(whereValues, extraValues);
        }

        Set<String> foundTextIds = new HashSet<>();
        EtkDatabaseTable table = getProject().getConfig().getDBDescription().findTable(TABLE_SPRACHE);
        Set<String> spracheFieldnames = new HashSet<>(table.getFieldListAsStringList(true));

        // (Un)gültige Lexikon-Quellen bestimmen
        Set<String> validDictMetaSources;
        Set<String> invalidDictMetaSources;
        if ((source != null) && (source != iPartsImportDataOrigin.UNKNOWN)) {
            validDictMetaSources = new HashSet<>();
            validDictMetaSources.add(source.getOrigin());
            invalidDictMetaSources = null;
        } else if (OMIT_PROVAL_CODES || OMIT_MBS_CODES) {
            validDictMetaSources = null;
            invalidDictMetaSources = new HashSet<>();
            if (OMIT_PROVAL_CODES) {
                invalidDictMetaSources.add(iPartsImportDataOrigin.PROVAL.getOrigin());
            }
            if (OMIT_MBS_CODES) {
                invalidDictMetaSources.add(iPartsImportDataOrigin.SAP_MBS.getOrigin());
            }
        } else {
            validDictMetaSources = null;
            invalidDictMetaSources = null;
        }

        // Callback um pro SPRACHE Id nur ein Objekt zu liefern
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                if (isCancelled) {
                    return false;
                }
                String textId = attributes.getFieldValue(FIELD_S_TEXTID);
                if (foundTextIds.contains(textId)) {
                    return false;
                }
                foundTextIds.add(textId);

                // Passt die Text-ID zur gewünschten Suchmaske? Die Abfrage ist absichtlich NICHT im SQL Select enthalten,
                // weil wir dadurch autoLike verwenden müssten, was bei Suchtexten, die * oder ? enthalten, aber auch für
                // den Suchtext zu einem like führen würde (im Worst Case mit * am Anfang und somit ohne Index).
                if (!StrUtils.matchesSqlLike(searchTextIdMask, textId)) {
                    return false;
                }

                // Den echten Text aus dem Treffer bestimmen (Langtext oder max. 300 Zeichen lang)
                String longTextFromAttributes = etkDbs.getLongTextFromAttributes(attributes);
                // Wenn der Suchtext nicht übereinstimmt -> Datensatz nicht beachten
                if (!searchText.equals(longTextFromAttributes)) {
                    return false;
                }

                // Bei einem Treffer die Lexikon-Quelle prüfen falls notwendig (wäre bei einem Join auf DA_DICT_META nicht
                // notwendig, aber ein Join wäre langsamer)
                if ((validDictMetaSources != null) || (invalidDictMetaSources != null)) {
                    DBDataObjectAttributesList dataDictMetaList = etkDbs.getAttributesList(TABLE_DA_DICT_META,
                                                                                           new String[]{ FIELD_DA_DICT_META_SOURCE },
                                                                                           new String[]{ FIELD_DA_DICT_META_TEXTID },
                                                                                           new String[]{ textId });
                    if (!dataDictMetaList.isEmpty() || !allowTextWithoutSource) {
                        boolean foundValidSource = false;
                        for (DBDataObjectAttributes dataDictMeta : dataDictMetaList) {
                            String source = dataDictMeta.getFieldValue(FIELD_DA_DICT_META_SOURCE);
                            if ((validDictMetaSources != null) && validDictMetaSources.contains(source)) {
                                foundValidSource = true;
                                break;
                            }
                            if ((invalidDictMetaSources != null) && !invalidDictMetaSources.contains(source)) {
                                foundValidSource = true;
                                break;
                            }
                        }

                        if (!foundValidSource) {
                            return false; // Quelle ist ungültig
                        }
                    }
                }

                // Felder, die nicht zur SPRACHE Tabelle gehören entfernen
                attributes.entrySet().removeIf(entry -> !spracheFieldnames.contains(entry.getKey()));
                return true;
            }
        };

        EtkDataTextEntryList dataTextEntries = new EtkDataTextEntryList();
        boolean[] searchCaseInsensitives = new boolean[whereFields.length];
        Arrays.fill(searchCaseInsensitives, false);

        // Es soll aktiv nicht in den Langtexten gesucht werden
        // searchSortAndFillWithJoin() wird nur wegen dem foundAttributesCallback verwendet, da ein echter Join auf DA_DICT_META
        // leider langsamer ist als ein zweites SQL Select im Callback bei passendem Treffer)
        dataTextEntries.searchSortAndFillWithJoin(project, null, selectFields, whereFields, whereValues,
                                                  false, null, false, searchCaseInsensitives, false,
                                                  false, false, foundAttributesCallback, false);
        return dataTextEntries;
    }

    public void cancelSearch() {
        isCancelled = true;
    }

}
