/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldsHelper;
import de.docware.apps.etk.base.db.AbstractSearchQueryCancelable;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.ArrayUtil;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.CaseMode;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.Tables;

import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Erweiterung von {@link SimpleSelectSearchResultGrid} zur Suche nach TextId's in der SPRACH-Tabelle über die kurzen Texte.
 */
public class SelectSearchGridText extends SimpleSelectSearchResultGrid implements EtkDbConst {

    public static final int MIN_CHAR_FOR_SEARCH_FOR_DICTIONARY = 2;
    public static final int RESTART_TIMER_INTERVAL_FOR_DICTIONARY = 300;
    public static final int RESTART_TIMER_INTERVAL_FOR_DICTIONARY_WITH_CACHE = 10;

    public static EtkDisplayFields getDefaultDisplayFields(EtkProject project) {
        EtkDisplayFields displayResultFields = EtkDisplayFieldsHelper.createDefaultDisplayResultFields(project, TABLE_SPRACHE);
        int index = displayResultFields.getIndexOfFeld(TABLE_SPRACHE, FIELD_S_FELD, false);
        displayResultFields.getFeld(index).setVisible(false);
        index = displayResultFields.getIndexOfFeld(TABLE_SPRACHE, FIELD_S_TEXTNR, false);
        displayResultFields.getFeld(index).setVisible(false);
        index = displayResultFields.getIndexOfFeld(TABLE_SPRACHE, FIELD_S_BENENN_LANG, false);
        displayResultFields.getFeld(index).setVisible(false);
        index = displayResultFields.getIndexOfFeld(TABLE_SPRACHE, FIELD_S_TEXTID, false);
        if (!Constants.DEVELOPMENT) {
            displayResultFields.getFeld(index).setVisible(false);
        }
        index = displayResultFields.getIndexOfFeld(TABLE_SPRACHE, FIELD_S_SPRACH, false);
        displayResultFields.getFeld(index).setVisible(false);
        return displayResultFields;
    }

    private DictTextKindTypes textKind;
    private Language searchLanguage;
    private Set<String> foundTextIds = new HashSet<>();

    public SelectSearchGridText(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, TABLE_SPRACHE, FIELD_S_BENENN);

        this.searchLanguage = Language.DE;
        setMinCharForSearch(MIN_CHAR_FOR_SEARCH_FOR_DICTIONARY);
        setRestartTimerInterval(RESTART_TIMER_INTERVAL_FOR_DICTIONARY);
        setAutoSelectSingleSearchResult(true);
        setDisplayResultFields(getDefaultDisplayFields(parentForm.getConnector().getProject()));
    }

    /**
     * Zeigt den Suchdialog an und startet dabei direkt eine Suche nach dem angegebenen Initialwert, in der
     * angegebenen Sprache.
     *
     * @param searchLanguage
     * @param initialSearchValue
     * @return
     */
    public DBDataObjectAttributes doSimpleSelectGridTextDialog(Language searchLanguage, String initialSearchValue) {
        DBDataObjectAttributes selected = null;

        setSearchLanguage(searchLanguage);
        setTitle(TranslationHandler.translate("!!Wiederverwendbare Texte suchen (%1)",
                                              TranslationHandler.translate(getSearchLanguage().getDisplayName())));
        setMaxResults(getMaxSelectResultSize(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE, true));

        if (!StrUtils.isEmpty(initialSearchValue)) {
            setSearchValue(initialSearchValue);
        }
        if (showModal() == ModalResult.OK) {
            selected = getSelectedAttributes();
        }
        close();
        return selected;
    }

    public Language getSearchLanguage() {
        return searchLanguage;
    }

    public void setSearchLanguage(Language searchLanguage) {
        if (searchLanguage != null) {
            this.searchLanguage = searchLanguage;
        }
    }

    public Language getSearchLanguageForTextKind() {
        if (textKind == DictTextKindTypes.NEUTRAL_TEXT) {
            return Language.DE; // Bei sprachneutralen Texten immer Deutsch als Sprache nehmen
        } else {
            return searchLanguage;
        }
    }

    public DictTextKindTypes getTextKind() {
        return textKind;
    }

    public void setTextKind(DictTextKindTypes textKind) {
        this.textKind = textKind;
    }

    @Override
    protected int getRestartTimerInterval() {
        if (DictTextCache.isTextKindWithCache(textKind)) { // Bei Textarten mit Cache nicht lange warten mit der Suche
            return RESTART_TIMER_INTERVAL_FOR_DICTIONARY_WITH_CACHE;
        } else {
            return super.getRestartTimerInterval();
        }
    }

    @Override
    protected EtkSqlCommonDbSelect buildQuery(String searchValue) {
        EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(getProject(), searchTable);
        //sqlSelect.setUpperCanBeApplied(caseInsensitive);
        boolean searchCaseInsensitive = caseInsensitive && getProject().getDB().getDatabaseType(MAIN).isUseUpperCaseMode();

        List<String> fromFields = new ArrayList<>();
        for (EtkDisplayField field : displayResultFields.getFields()) {
            fromFields.add(field.getKey().getName());
        }
        sqlSelect.getQuery().select(new Fields(fromFields));

        sqlSelect.getQuery().from(new Tables(searchTable));

        Condition condition = buildConditionWithValue(sqlSelect, FIELD_S_SPRACH, Condition.OPERATOR_EQUALS, getSearchLanguageForTextKind().getCode());
        condition.and(buildConditionWithValue(sqlSelect, FIELD_S_TEXTID, Condition.OPERATOR_NOT_EQUALS, ""));

        CaseMode caseMode;
        if (searchCaseInsensitive) {
            if (StrUtils.isUniCase(searchValue)) {
                caseMode = CaseMode.NOTHING;
            } else {
                caseMode = CaseMode.UPPERCASE;
            }
        } else {
            caseMode = CaseMode.NOTHING;
        }
        condition.and(buildConditionWithValue(sqlSelect, searchTable, searchColumn, Condition.OPERATOR_LIKE, searchValue, caseMode));

        sqlSelect.getQuery().where(condition);
        return sqlSelect;
    }

    @Override
    protected synchronized void startSearch() {
        foundTextIds.clear();
        super.startSearch();
    }

    @Override
    protected AbstractSearchQueryCancelable executeQuery(EtkSqlCommonDbSelect sqlSelect) throws CanceledException {
        if (DictTextCache.isTextKindWithCache(textKind)) {
            final String searchLanguageForTextKind = getSearchLanguageForTextKind().getCode();
            DictTextCache textCache = DictTextCache.getInstance(textKind, searchLanguageForTextKind);
            Map<String, String> textsMap = textCache.searchTexts(lastSearchValue, DictTextCache.isTextKindWithCacheAndRestrictedSearch(textKind));
            final Iterator<Map.Entry<String, String>> textIdsIterator = textsMap.entrySet().iterator();

            // Eine SearchQuery mit den Text-IDs aus dem Cache simulieren
            return new AbstractSearchQueryCancelable() {
                @Override
                public boolean next() throws CanceledException {
                    return textIdsIterator.hasNext();
                }

                @Override
                public void closeQuery() {
                }

                @Override
                public boolean eof() {
                    return !textIdsIterator.hasNext();
                }

                @Override
                public DBDataObjectAttributes loadAttributes(EtkProject project, String[] fields) {
                    // Künstliches DBDataObjectAttributes-Objekt für die Suchergebnisse erstellen mit den kurzen Texten
                    // aus dem Suchergebnis, um unnötige DB-Zugriffe zu vermeiden
                    Map.Entry<String, String> textEntry = textIdsIterator.next();
                    DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                    for (String field : fields) {
                        if (field.equals(FIELD_S_SPRACH)) {
                            attributes.addField(FIELD_S_SPRACH, searchLanguageForTextKind, DBActionOrigin.FROM_DB);
                        } else if (field.equals(FIELD_S_BENENN)) {
                            attributes.addField(FIELD_S_BENENN, textEntry.getKey(), DBActionOrigin.FROM_DB);
                        } else if (field.equals(FIELD_S_TEXTID)) {
                            attributes.addField(FIELD_S_TEXTID, textEntry.getValue(), DBActionOrigin.FROM_DB);
                        } else {
                            attributes.addField(field, "", DBActionOrigin.FROM_DB);
                        }
                    }
                    return attributes;
                }

                @Override
                public DBDataObjectAttributes loadAttributes(EtkProject project, List<String> fields) {
                    return loadAttributes(project, ArrayUtil.toArray(fields));
                }
            };
        } else {
            return super.executeQuery(sqlSelect);
        }
    }

    @Override
    public DBDataObjectAttributes getSelectedAttributes() {
        DBDataObjectAttributes selectedAttributes = super.getSelectedAttributes();
        if (selectedAttributes == null) {
            return null;
        }

        if (DictTextCache.isTextKindWithCache(textKind)) {
            // Echten Datensatz aus der DB laden
            return getProject().getDbLayer().getAttributes(TABLE_SPRACHE, null, new String[]{ FIELD_S_TEXTID, FIELD_S_SPRACH },
                                                           new String[]{ selectedAttributes.getFieldValue(FIELD_S_TEXTID),
                                                                         selectedAttributes.getFieldValue(FIELD_S_SPRACH) });
        } else {
            return selectedAttributes;
        }
    }

    @Override
    protected boolean doValidAttributes(DBDataObjectAttributes attributes) {
        // Datensätze mit leerer TextId oder bereits gefundener TextId überspringen
        String textId = attributes.getField(FIELD_S_TEXTID).getAsString();
        if (textId.isEmpty() || foundTextIds.contains(textId)) {
            return false;
        }
        foundTextIds.add(textId);

        String benennLang = attributes.getField(FIELD_S_BENENN_LANG).getAsString();
        if (!benennLang.isEmpty()) {
            attributes.addField(FIELD_S_BENENN, benennLang, DBActionOrigin.FROM_DB);
        }
        return true;
    }

    protected Condition buildConditionWithValue(EtkSqlCommonDbSelect sqlSelect, String fieldName, String operator, String fieldValue) {
        return buildConditionWithValue(sqlSelect, searchTable, fieldName, operator, fieldValue, CaseMode.NOTHING);
    }

    protected Condition buildConditionWithValue(EtkSqlCommonDbSelect sqlSelect, String tableName, String fieldName, String operator,
                                                String fieldValue, CaseMode caseMode) {
        Condition condition = new Condition(TableAndFieldName.make(tableName, fieldName), operator, Condition.PARAMETER_SIGN,
                                            caseMode);
        sqlSelect.addParamValue(tableName, fieldName, fieldValue, caseMode);
        return condition;
    }
}
