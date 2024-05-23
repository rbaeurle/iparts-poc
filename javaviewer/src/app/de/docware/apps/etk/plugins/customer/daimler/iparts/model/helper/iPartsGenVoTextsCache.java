/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenVoSuppText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenVoSuppTextList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.cache.ObjectInstanceStrongLRUList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache für die Texte von den generischen Verbauorten in allen Sprachen
 */
public class iPartsGenVoTextsCache implements iPartsConst {

    private static ObjectInstanceStrongLRUList<Object, iPartsGenVoTextsCache> instances =
            new ObjectInstanceStrongLRUList<>(MAX_CACHE_SIZE_STRUCTURE_INFOS, MAX_CACHE_LIFE_TIME_CORE);

    // Maps für die Werke
    protected Map<String, EtkMultiSprache> genVoToTextMap = new HashMap<>();

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized iPartsGenVoTextsCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsGenVoTextsCache.class, "GenVoTextsCache", false);
        iPartsGenVoTextsCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsGenVoTextsCache();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    private void load(EtkProject project) {
        iPartsDataGenVoSuppTextList dataGenVoSuppTextsList = new iPartsDataGenVoSuppTextList();
        dataGenVoSuppTextsList.searchSortAndFillWithMultiLangValueForAllLanguages(project, null, FIELD_DA_GENVO_DESCR, null,
                                                                                  null, false, null, false);
        for (iPartsDataGenVoSuppText dataGenVoSuppText : dataGenVoSuppTextsList) {
            genVoToTextMap.put(dataGenVoSuppText.getAsId().getGenVoNo(), dataGenVoSuppText.getFieldValueAsMultiLanguage(FIELD_DA_GENVO_DESCR));
        }
    }

    /**
     * Liefert den Text für den übergebenen generischen Verbauort in der DB-Sprache vom {@link EtkProject} inkl. Rückfallsprachen
     * zurück.
     *
     * @param genVoNumber
     * @param project
     * @return {@code null} falls kein Text für den generischen Verbauort existiert
     */
    public String getGenVoText(String genVoNumber, EtkProject project) {
        return getGenVoText(genVoNumber, project.getDBLanguage(), project.getDataBaseFallbackLanguages());
    }

    /**
     * Liefert den Text für den übergebenen generischen Verbauort in der gewünschten Sprache inkl. Rückfallsprachen zurück.
     *
     * @param genVoNumber
     * @param language
     * @param dataBaseFallbackLanguages
     * @return {@code null} falls kein Text für den generischen Verbauort existiert
     */
    public String getGenVoText(String genVoNumber, String language, List<String> dataBaseFallbackLanguages) {
        EtkMultiSprache genVoSuppText = genVoToTextMap.get(genVoNumber);
        if (genVoSuppText != null) {
            return genVoSuppText.getTextByNearestLanguage(language, dataBaseFallbackLanguages);
        } else {
            return null;
        }
    }
}