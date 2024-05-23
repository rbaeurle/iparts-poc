/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.iPartsDictConst;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.Arrays;
import java.util.HashMap;

import static de.docware.apps.etk.base.config.db.EtkDbConst.*;
import static de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst.*;

/**
 * Hilfsklasse für schnelle Suchen nach Lexikon Texten
 */
public class DictMetaSearchHelper {

    private final EtkProject project;
    private final iPartsDictTextKindId textKindId;
    private final EtkDisplayFields selectFields;

    private final EtkDataObjectList.FoundAttributesCallback foundAttributesCallback;
    private final EtkDataObjectList.JoinData joinData;
    private String[] whereTableAndFieldsBase;
    private String[] whereValuesBase;
    private String[] whereTableAndFieldsForSearch;
    private String[] whereValuesForSearch;
    private boolean[] searchCaseInsensitives;


    public DictMetaSearchHelper(EtkProject project, iPartsDictTextKindId textKindId, Language searchLang) {
        this.project = project;
        this.textKindId = textKindId;

        selectFields = new EtkDisplayFields();
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_DICT_META));
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_SPRACH, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_BENENN, false, false));

        whereTableAndFieldsBase = new String[]{ TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TXTKIND_ID),
                                                TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_STATE),
                                                TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH) };
        whereValuesBase = new String[]{ textKindId.getTextKindId(),
                                        iPartsDictConst.DICT_STATUS_RELEASED,
                                        searchLang.getCode() };

        whereTableAndFieldsForSearch = new String[whereTableAndFieldsBase.length];
        System.arraycopy(whereTableAndFieldsBase, 0, whereTableAndFieldsForSearch, 0, whereTableAndFieldsBase.length);

        whereValuesForSearch = new String[whereValuesBase.length];
        System.arraycopy(whereValuesBase, 0, whereValuesForSearch, 0, whereValuesBase.length);

        searchCaseInsensitives = new boolean[whereTableAndFieldsForSearch.length];
        Arrays.fill(searchCaseInsensitives, false);

        appendWhereFieldAndValueForSearch(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN), "placeholder", true);

        joinData = new EtkDataObjectList.JoinData(TABLE_SPRACHE,
                                                  new String[]{ FIELD_DA_DICT_META_TEXTID },
                                                  new String[]{ FIELD_S_TEXTID },
                                                  false, false);
        foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String source = attributes.getFieldValue(FIELD_DA_DICT_META_SOURCE);
                DictTextCache.DictTextCacheEntry entry = new DictTextCache.DictTextCacheEntry(attributes.getFieldValue(FIELD_DA_DICT_META_TEXTID),
                                                                                              source);
                DictTextCache.CompanyType companyType = entry.getCompanyType();
                return (companyType != DictTextCache.CompanyType.ONLY_DTAG);
            }
        };
    }

    public iPartsDictTextKindId getTextKindId() {
        return textKindId;
    }

    private boolean[] mergeArrays(boolean[] array1, boolean... array2) {
        boolean[] output = new boolean[array1.length + array2.length];
        System.arraycopy(array1, 0, output, 0, array1.length);
        System.arraycopy(array2, 0, output, array1.length, array2.length);
        return output;
    }

    public void appendWhereFieldAndValueBase(String additionalWhereField, String additionalWhereValue) {
        whereTableAndFieldsBase = StrUtils.mergeArrays(whereTableAndFieldsBase, additionalWhereField);
        whereValuesBase = StrUtils.mergeArrays(whereValuesBase, additionalWhereValue);
    }

    public void appendWhereFieldAndValueForSearch(String additionalWhereField, String additionalWhereValue) {
        appendWhereFieldAndValueForSearch(additionalWhereField, additionalWhereValue, false);
    }

    public void appendWhereFieldAndValueForSearch(String additionalWhereField, String additionalWhereValue, boolean searchCaseInsensitive) {
        whereTableAndFieldsForSearch = StrUtils.mergeArrays(whereTableAndFieldsForSearch, additionalWhereField);
        whereValuesForSearch = StrUtils.mergeArrays(whereValuesForSearch, additionalWhereValue);
        searchCaseInsensitives = mergeArrays(searchCaseInsensitives, searchCaseInsensitive);
    }

    public void appendWhereFieldAndValue(String additionalWhereField, String additionalWhereValue) {
        appendWhereFieldAndValueBase(additionalWhereField, additionalWhereValue);
        appendWhereFieldAndValueForSearch(additionalWhereField, additionalWhereValue);
    }

    public iPartsDataDictMeta searchTextInDictionary(String descr) {
        return searchTextInDictionary(descr, false);
    }

    public iPartsDataDictMeta searchTextInDictionary(String descr, boolean searchCaseInsensitive) {
        if (StrUtils.isEmpty(descr)) {
            return null; //new iPartsDataDictMeta();
        }
        whereValuesForSearch[3] = descr;
        boolean[] caseInsensitives = null;
        if (searchCaseInsensitive) {
            caseInsensitives = searchCaseInsensitives;
        }

        iPartsDataDictMetaList searchList = new iPartsDataDictMetaList();
        searchList.setSearchWithoutActiveChangeSets(true);
        searchList.searchSortAndFillWithJoin(project, null, selectFields, whereTableAndFieldsForSearch, whereValuesForSearch,
                                             false, null, false, caseInsensitives, false, false, false,
                                             foundAttributesCallback, false, joinData);

        if (searchList.isEmpty()) {
            return null;
        }
        iPartsDataDictMeta dataDictMeta = searchList.get(0);
        dataDictMeta.removeForeignTablesAttributes();
        return dataDictMeta;
    }


    public HashMap<String, iPartsDataDictMeta> loadAllTextsWithTextId() {
        iPartsDataDictMetaList searchList = new iPartsDataDictMetaList();
        searchList.setSearchWithoutActiveChangeSets(true);
        searchList.searchSortAndFillWithJoin(project, null, selectFields, whereTableAndFieldsBase, whereValuesBase,
                                             false, null, false,
                                             foundAttributesCallback, joinData);

        HashMap<String, iPartsDataDictMeta> allTexts = new HashMap<>();
        for (iPartsDataDictMeta dictMeta : searchList) {
            String langAS = dictMeta.getFieldValue(FIELD_S_BENENN);
            dictMeta.removeForeignTablesAttributes();
            allTexts.put(langAS, dictMeta);
        }
        return allTexts;
    }

}
