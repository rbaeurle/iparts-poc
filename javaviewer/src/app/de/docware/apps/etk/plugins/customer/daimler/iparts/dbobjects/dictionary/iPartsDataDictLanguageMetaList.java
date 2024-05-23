/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Liste von {@link iPartsDataDictLanguageMeta}s (Liste der Sprachen zu einer TextId).
 */
public class iPartsDataDictLanguageMetaList extends EtkDataObjectList<iPartsDataDictLanguageMeta> implements iPartsConst {

    public iPartsDataDictLanguageMetaList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictLanguageMeta}s, die neu angelegt wurden
     * gesucht wird nach textId iPARTS.* in der Sprache DE und nicht besetzter Job-Id
     *
     * @param project
     * @return
     */
    public static iPartsDataDictLanguageMetaList loadCreatedLanguageMetaList(EtkProject project, Language lang) {
        iPartsDataDictLanguageMetaList list = new iPartsDataDictLanguageMetaList();
        if (lang == null) {
            list.loadCreatedLanguageMetaFromDB(project, null, DBActionOrigin.FROM_DB);
            Map<String, iPartsDataDictLanguageMeta> deMap = new HashMap<>();
            for (iPartsDataDictLanguageMeta dictLanguageMeta : list) {
                deMap.put(dictLanguageMeta.getAsId().getTextId(), dictLanguageMeta);
            }
            list.clear(DBActionOrigin.FROM_DB);
            list.addAll(deMap.values(), DBActionOrigin.FROM_DB);
        } else {
            list.loadCreatedLanguageMetaFromDB(project, lang, DBActionOrigin.FROM_DB);
        }
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictLanguageMeta}s, die der übergebenen TextId zugeordnet sind.
     *
     * @param project
     * @param textId
     * @return
     */
    public static iPartsDataDictLanguageMetaList loadLanguageMetaList(EtkProject project, String textId) {
        iPartsDataDictLanguageMetaList list = new iPartsDataDictLanguageMetaList();
        list.loadLanguageMetaFromDB(project, textId, DBActionOrigin.FROM_DB);
        return list;
    }

    public iPartsDataDictLanguageMeta findLanguage(String language) {
        for (iPartsDataDictLanguageMeta dataDictSprache : this) {
            if (dataDictSprache.getAsId().getLanguage().equals(language)) {
                return dataDictSprache;
            }
        }
        return null;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictLanguageMeta}s, die neu angelegt wurden
     * gesucht wird nach textId iPARTS.* in der Sprache DE und nicht besetzter Job-Id
     *
     * @param project
     * @param origin
     */
    private void loadCreatedLanguageMetaFromDB(EtkProject project, Language lang, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_SPRACHE_TEXTID,
                                             FIELD_DA_DICT_SPRACHE_TRANS_JOBID };
        String[] whereValues = new String[]{ DictHelper.buildIPARTSDictTextId("*"),
                                             "" };
        String[] sortFields = new String[]{ FIELD_DA_DICT_SPRACHE_TEXTID };

        if (lang != null) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DA_DICT_SPRACHE_SPRACH });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ lang.getCode() });
        }
        searchSortAndFillWithLike(project, TABLE_DA_DICT_SPRACHE, null, whereFields,
                                  whereValues, sortFields, false, LoadType.COMPLETE,
                                  false, origin);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataDictLanguageMeta}s, die der übergebenen TextId zugeordnet sind.
     *
     * @param project
     * @param textId
     * @param origin
     */
    private void loadLanguageMetaFromDB(EtkProject project, String textId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_DICT_SPRACHE_TEXTID };
        String[] whereValues = new String[]{ textId };

        searchAndFill(project, TABLE_DA_DICT_SPRACHE, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataDictLanguageMeta getNewDataObject(EtkProject project) {
        return new iPartsDataDictLanguageMeta(project, null);
    }
}
