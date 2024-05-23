/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCombTextHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Repräsentiert eine Liste von iPartsDataCombText
 */
public class iPartsDataCombTextList extends EtkDataObjectList<iPartsDataCombText> implements iPartsConst {

    @Override
    protected iPartsDataCombText getNewDataObject(EtkProject project) {
        return new iPartsDataCombText(project, null);
    }

    /**
     * Löscht alle CombText-Einträge für ein Modul
     * !!Achtung: gilt nur für DAIMLER, da hier nur TextIds verwendet werden
     *
     * @param project
     * @param assemblyId
     */
    public static void deleteCombTextsForAssembly(EtkProject project, AssemblyId assemblyId) {
        project.getDbLayer().delete(TABLE_DA_COMB_TEXT,
                                    new String[]{ FIELD_DCT_MODULE, FIELD_DCT_MODVER },
                                    new String[]{ assemblyId.getKVari(), assemblyId.getKVer() });
    }

    /**
     * Kombinierte Texte für die ganze übergebene Stückliste bestimmen
     *
     * @param id
     * @param neutralTextsFromPartForModule sprachneutrale Texte am Teil für gesamtes Modul (Key = k_lfdnr); null wenn nicht verwendet
     * @param project
     * @return Map mit Stücklisteneintrag (ldfNr) -> kombinierter Text
     */
    public static Map<String, String> getCombTextsForModule(AssemblyId id, Map<String, String> neutralTextsFromPartForModule, EtkProject project) {
        return loadForModule(id, project).getCombTexts(neutralTextsFromPartForModule, project);
    }

    /**
     * Kombinierte Texte für die {@link iPartsDataCombText}-Objekte in dieser Liste bestimmen
     *
     * @param neutralTextsFromPartForModule
     * @param neutralTextsFromPartForModule sprachneutrale Texte am Teil für gesamtes Modul (Key = k_lfdnr); null wenn nicht verwendet
     * @param project
     * @return Map mit Stücklisteneintrag (ldfNr) -> kombinierter Text
     */
    public Map<String, String> getCombTexts(Map<String, String> neutralTextsFromPartForModule, EtkProject project) {
        return getCombTexts(neutralTextsFromPartForModule, null, project);
    }

    /**
     * Kombinierte Texte für die {@link iPartsDataCombText}-Objekte in dieser Liste bestimmen
     *
     * @param neutralTextsFromPartForModule
     * @param map
     * @param project
     * @return Map mit Stücklisteneintrag (ldfNr) -> kombinierter Text
     */
    public Map<String, String> getCombTexts(Map<String, String> neutralTextsFromPartForModule,
                                            Map<String, List<EtkMultiSprache>> map, EtkProject project) {
        boolean handleMultiLangMap = map != null;
        if (!handleMultiLangMap) {
            map = buildSeqNoCombTextsMap();
        }
        // String-Liste -> String
        return iPartsCombTextHelper.createCombTextToSeqNoMap(map, neutralTextsFromPartForModule, handleMultiLangMap, project);
    }

    public Map<String, List<EtkMultiSprache>> buildSeqNoCombTextsMap() {
        Map<String, List<EtkMultiSprache>> map = new HashMap<>();  // k_lfdNr -> Einzeltexte
        Iterator<iPartsDataCombText> listIterator = iterator();
        while (listIterator.hasNext()) {
            iPartsDataCombText dataCombText = listIterator.next();
            String seqNo = dataCombText.getFieldValue(FIELD_DCT_SEQNO);
            List<EtkMultiSprache> tokens = map.get(seqNo);
            if (tokens == null) {
                tokens = new DwList<>();
                map.put(seqNo, tokens);
            }
            tokens.add(dataCombText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT));
        }
        return map;
    }

    /**
     * Lädt alle Einträge für das übergebene Modul, z.B. um diese zu löschen.
     *
     * @param id
     * @param project
     * @return
     */
    public static iPartsDataCombTextList loadForModule(AssemblyId id, EtkProject project) {
        iPartsDataCombTextList dataCombTextList = new iPartsDataCombTextList();
        dataCombTextList.loadCombTexts(id, project);
        return dataCombTextList;
    }

    /**
     * Lädt alle Einträge für den übergebenen Stücklisteneintrag inkl. den Texten für die aktuelle DB-Sprache, z.B. nach
     * der Suche.
     *
     * @param id
     * @param project
     * @return
     */
    public static iPartsDataCombTextList loadForPartListEntry(PartListEntryId id, EtkProject project) {
        return loadForPartListEntry(id, project.getDBLanguage(), project);
    }

    /**
     * Lädt alle Einträge für den übergebenen Stücklisteneintrag, z.B. nach der Suche.
     *
     * @param id
     * @param language
     * @param project
     * @return
     */
    public static iPartsDataCombTextList loadForPartListEntry(PartListEntryId id, String language, EtkProject project) {
        iPartsDataCombTextList dataCombTextList = new iPartsDataCombTextList();
        dataCombTextList.loadCombText(id, language, project);
        return dataCombTextList;
    }

    /**
     * Lädt zur übergebenen {@link PartListEntryId} alle mehrsprachigen kombinierten Texte
     *
     * @param project
     * @param id
     * @return
     */
    public static iPartsDataCombTextList loadForPartListEntryAndAllLanguages(PartListEntryId id, EtkProject project) {
        iPartsDataCombTextList dataCombTextList = new iPartsDataCombTextList();
        dataCombTextList.loadCombTextsForAllLanguages(project, id.getKVari(), id.getKVer(), id.getKLfdnr(), DBActionOrigin.FROM_DB);
        return dataCombTextList;
    }

    /**
     * DataObjects für kombinierten Text der übergebenen Stückliste laden
     *
     * @param id
     * @param language
     * @param project
     */
    public void loadCombText(PartListEntryId id, String language, EtkProject project) {
        if (language != null) {
            searchSortAndFillWithMultiLangValues(project, language, null,
                                                 new String[]{ FIELD_DCT_MODULE, FIELD_DCT_MODVER, FIELD_DCT_SEQNO },
                                                 new String[]{ id.getKVari(), id.getKVer(), id.getKLfdnr() }, false,
                                                 new String[]{ FIELD_DCT_TEXT_SEQNO }, false);
        } else { // Kein Join auf die Sprachtabelle notwendig
            searchSortAndFill(project, TABLE_DA_COMB_TEXT, new String[]{ FIELD_DCT_MODULE, FIELD_DCT_MODVER, FIELD_DCT_SEQNO },
                              new String[]{ id.getKVari(), id.getKVer(), id.getKLfdnr() }, new String[]{ FIELD_DCT_TEXT_SEQNO },
                              LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * DataObjects für alle kombinierten Texte der übergebenen Stückliste laden
     *
     * @param id
     * @param project
     */
    public void loadCombTexts(AssemblyId id, EtkProject project) {
        searchSortAndFillWithMultiLangValues(project, project.getDBLanguage(), null,
                                             new String[]{ FIELD_DCT_MODULE, FIELD_DCT_MODVER },
                                             new String[]{ id.getKVari(), id.getKVer() }, false,
                                             new String[]{ FIELD_DCT_SEQNO, FIELD_DCT_TEXT_SEQNO }, false);
    }

    public iPartsDataCombText get(iPartsCombTextId combTextId) {
        Iterator<iPartsDataCombText> listIterator = iterator();
        while (listIterator.hasNext()) {
            iPartsDataCombText dataCombText = listIterator.next();
            if (dataCombText.getAsId().equals(combTextId)) {
                return dataCombText;
            }
        }
        return null;
    }

    /**
     * Lädt zur übergebenen {@link AssemblyId} alle mehrsprachigen kombinierten Texte
     *
     * @param project
     * @param id
     * @return
     */
    public static iPartsDataCombTextList loadForModuleAndAllLanguages(EtkProject project, AssemblyId id) {
        iPartsDataCombTextList dataCombTextList = new iPartsDataCombTextList();
        dataCombTextList.loadCombTextsForAllLanguages(project, id.getKVari(), id.getKVer(), null, DBActionOrigin.FROM_DB);
        return dataCombTextList;
    }

    private void loadCombTextsForAllLanguages(EtkProject project, String kVari, String kVer, String kLfdNr, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DCT_MODULE, FIELD_DCT_MODVER };
        String[] whereValues = new String[]{ kVari, kVer };
        if (StrUtils.isValid(kLfdNr)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DCT_SEQNO });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ kLfdNr });
        }
        String[] sortFields = new String[]{ FIELD_DCT_SEQNO, FIELD_DCT_TEXT_SEQNO };

        searchSortAndFillWithMultiLangValueForAllLanguages(project, null, TableAndFieldName.make(TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT),
                                                           whereFields, whereValues, false, sortFields, false);
    }

}