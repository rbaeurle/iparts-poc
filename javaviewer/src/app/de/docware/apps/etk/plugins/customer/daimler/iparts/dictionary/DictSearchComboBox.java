/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.misc.EtkDBSearchComboBox;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.sql.TableAndFieldName;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ComboBox zur Suche und Auswahl von freigegebenen Texten für eine bestimmte Textart im Lexikon.
 */
public class DictSearchComboBox extends EtkDBSearchComboBox<String> implements iPartsConst {

    public static final int MIN_CHAR_FOR_SEARCH_FOR_DICTIONARY = 2;
    public static final int WAIT_TIME_FOR_SEARCH_FOR_DICTIONARY = 300;
    public static final int WAIT_TIME_FOR_SEARCH_FOR_DICTIONARY_WITH_CACHE = 10;

    private DictTextKindTypes textKind;
    private boolean textKindWithCache;

    protected static EtkDisplayFields createSelectFields() {
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_SPRACHE, FIELD_S_BENENN, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TEXTID, false, false));
        return selectFields;
    }

    protected static String[] createWhereTableAndFields(DictTextKindTypes textKind) {
        if (textKind != null) {
            return new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_TXTKIND_ID),
                                 TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_STATE),
                                 TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH),
                                 TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN) };
        } else {
            return new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_STATE),
                                 TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH),
                                 TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN) };
        }
    }

    protected static String[] createWhereValues(DictTextKindTypes textKind, EtkProject project) {
        if (textKind != null) {
            if (project == null) { // DB-ID der Textart kann ohne EtkProject nicht bestimmt werden
                return null;
            }

            // DB-ID der Textart bestimmen
            String textKindDBValue;
            iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(project).getTxtKindId(textKind);
            if (textKindId != null) {
                textKindDBValue = textKindId.getTextKindId();
            } else {
                textKindDBValue = "*"; // Alle Textarten als Fallback zulassen
            }

            return new String[]{ textKindDBValue, iPartsDictConst.DICT_STATUS_RELEASED, project.getDBLanguage(), SEARCH_VALUE };
        } else {
            return new String[]{ iPartsDictConst.DICT_STATUS_RELEASED, project.getDBLanguage(), SEARCH_VALUE };
        }
    }

    protected static boolean[] createSearchCaseInsensitives(DictTextKindTypes textKind) {
        if (textKind != null) {
            return new boolean[]{ false, false, false, true };
        } else {
            return new boolean[]{ false, false, true };
        }
    }

    /**
     * Erzeugt eine ComboBox zur Suche nach Texten im Lexikon mit der übergebenen optionalen Textart.
     *
     * @param project
     * @param textKind Bei {@code null} wird in allen Textarten gesucht.
     */
    public DictSearchComboBox(EtkProject project, DictTextKindTypes textKind) {
        this(project, Mode.STANDARD, textKind);
    }

    /**
     * Erzeugt eine ComboBox mit dem angegebenen {@link de.docware.framework.modules.gui.controls.GuiComboBoxMode.Mode} zur
     * Suche nach Texten im Lexikon mit der übergebenen optionalen Textart.
     *
     * @param project
     * @param mode
     * @param textKind Bei {@code null} wird in allen Textarten gesucht.
     */
    public DictSearchComboBox(EtkProject project, Mode mode, DictTextKindTypes textKind) {
        super(project, mode, TABLE_SPRACHE, null, createSelectFields(), createWhereTableAndFields(textKind), createWhereValues(textKind, project),
              false, null, createSearchCaseInsensitives(textKind), false, null,
              new EtkDataObjectList.JoinData(iPartsConst.TABLE_DA_DICT_META,
                                             new String[]{ FIELD_S_TEXTID },
                                             new String[]{ iPartsConst.FIELD_DA_DICT_META_TEXTID },
                                             false, false));
        this.textKind = textKind;
        textKindWithCache = DictTextCache.isTextKindWithCache(textKind);
        setMinCharForSearch(MIN_CHAR_FOR_SEARCH_FOR_DICTIONARY);
        setWaitTimeForSearch(textKindWithCache ? WAIT_TIME_FOR_SEARCH_FOR_DICTIONARY_WITH_CACHE : WAIT_TIME_FOR_SEARCH_FOR_DICTIONARY);
        setMaxResults(iPartsPlugin.getPluginConfig().getConfigValueAsInteger(iPartsPlugin.CONFIG_MAX_RESULTS_FOR_COMBOBOX_SEARCH));
    }

    @Override
    public String[] getWhereValues() {
        String[] whereValues = super.getWhereValues();
        if ((whereValues == null) && (getProject() != null)) {
            whereValues = createWhereValues(getTextKind(), getProject());
            setWhereValues(whereValues);
        }

        return whereValues;
    }

    @Override
    protected String getUserObjectForFoundItem(DBDataObjectAttributes attributes) {
        return attributes.getField(FIELD_DA_DICT_META_TEXTID).getAsString();
    }

    @Override
    protected String getTextForFoundItem(DBDataObjectAttributes attributes) {
        return getMultiLineText(attributes.getField(FIELD_S_BENENN).getAsString());
    }

    public DictTextKindTypes getTextKind() {
        return textKind;
    }

    @Override
    protected void executeSearch(EtkProject project, String[] whereTableAndFields, String[] whereValuesWithSearchValue,
                                 String searchValue) {
        if (textKindWithCache) { // Textart mit Cache?
            Map<String, String> searchResults = DictTextCache.getInstance(textKind, project.getDBLanguage()).searchTexts(searchValue, DictTextCache.isTextKindWithCacheAndRestrictedSearch(textKind));
            int numResults = 0;
            Map<String, String> items = new LinkedHashMap<>();
            for (Map.Entry<String, String> searchResult : searchResults.entrySet()) {
                String text = getMultiLineText(searchResult.getKey());
                items.put(searchResult.getValue(), text); // Text-ID = getValue() ist das UserObject
                numResults++;
                if ((getMaxResults() > 0) && (numResults >= getMaxResults())) {
                    break;
                }

                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
            }
            addFoundItems(items);
        } else {
            super.executeSearch(project, whereTableAndFields, whereValuesWithSearchValue, searchValue);
        }
    }

    @Override
    public void setFilterText(String filterText, String filterUserObject, boolean startSearch) {
        super.setFilterText(getMultiLineText(filterText), filterUserObject, startSearch);
    }

    protected String getMultiLineText(String text) {
        return text.replace('\n', ' ');
    }
}