/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldWithAbstractKey;
import de.docware.apps.etk.base.db.EtkDbs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.swing.SwingCheckboxListCellRenderer;
import de.docware.util.sql.TableAndFieldName;

import java.util.List;

/**
 * EtkEditField
 */
public class EtkEditField extends EtkDisplayFieldWithAbstractKey<EtkDisplayFieldKeyNormal> {

    public static enum SORTTYPE {
        NONE, DATA, TEXT, BOTH
    }

    public static enum TABLEFIELDNAMEDISPLAYMODE {
        SHOWDBNAMES, SHOWDENOMINATIONS, SHOWDISPLAYALIASES
    }

    // Fürs Sortieren
    protected int sortOrderNumber;
    protected SwingCheckboxListCellRenderer.SORT sortDirection;
    protected SORTTYPE sortType;
    protected boolean isReadOnly;

    // für Multisprachfelder: Wenn True, soll die ActDatabase/Dokusprache verwendet werden,
    //        sonst die Sprache des EditFelds}
    protected boolean useActDatabaseLanguage;
    protected boolean ignoreAtWildCardSearch;
    protected boolean isEditTextIdAllowed;    // Feld nur mit TextId editierbar, keine Freitexte erlaubt (z. Zt. nur MultisprachEditWithTextId)
    protected boolean noManEdit;              // Feld nicht editierbar bei manuellem Einfügen


    public EtkEditField() {
        this("", "", false);
    }

    public EtkEditField(String name, boolean multiLang) {
        this(TableAndFieldName.getTableName(name), TableAndFieldName.getFieldName(name), multiLang);
    }


    public EtkEditField(String tablename, String fieldname, boolean multiLang) {
        this(tablename, fieldname, multiLang, false);
    }

    public EtkEditField(String tablename, String fieldname, boolean multiLang, boolean array) {
        super(new EtkDisplayFieldKeyNormal(tablename, fieldname), multiLang);
        setArray(array);
        init();
    }

    @Override
    public void load(ConfigBase configBase, String fieldKey, String rootKey, int saveVersion) {
        super.load(configBase, fieldKey, rootKey, saveVersion);
        loadIntern(configBase, fieldKey, saveVersion);
    }

    @Override
    protected void loadIntern(ConfigBase configBase, String fieldKey, int saveVersion) {
        super.loadIntern(configBase, fieldKey, saveVersion);
        noManEdit = configBase.getBoolean(fieldKey + "/NoManEdit", false);
        isEditTextIdAllowed = configBase.getBoolean(fieldKey + "/OnlyTextIdAllowed", false);
    }

    //==== Getter and Setter ===

    public int getSortOrderNumber() {
        return sortOrderNumber;
    }

    public void setSortOrderNumber(int sortOrderNumber) {
        this.sortOrderNumber = sortOrderNumber;
    }

    public SwingCheckboxListCellRenderer.SORT getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(SwingCheckboxListCellRenderer.SORT sortDirection) {
        this.sortDirection = sortDirection;
    }

    public SORTTYPE getSortType() {
        return sortType;
    }

    public void setSortType(SORTTYPE sortType) {
        this.sortType = sortType;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    public boolean isUseActDatabaseLanguage() {
        return useActDatabaseLanguage;
    }

    public void setUseActDatabaseLanguage(boolean useActDatabaseLanguage) {
        this.useActDatabaseLanguage = useActDatabaseLanguage;
    }

    public boolean isIgnoreAtWildCardSearch() {
        return ignoreAtWildCardSearch;
    }

    public void setIgnoreAtWildCardSearch(boolean ignoreAtWildCardSearch) {
        this.ignoreAtWildCardSearch = ignoreAtWildCardSearch;
    }

    public boolean isEditTextIdAllowed() {
        return isEditTextIdAllowed;
    }

    public void setEditTextIdAllowed(boolean editTextIdAllowed) {
        isEditTextIdAllowed = editTextIdAllowed;
    }

    public boolean isNoManEdit() {
        return noManEdit;
    }

    public void setNoManEdit(boolean noManEdit) {
        this.noManEdit = noManEdit;
    }


    //==== Getter and Setter End ===


    public boolean isFieldWithSelectionValues() {
        return EtkEditFieldHelper.isEnumField(getKey().getName());
    }

    public boolean existsInConfig(EtkConfig config) {
        EtkDatabaseTable table = config.getDBDescription().findTable(getKey().getTableName());
        if (table != null) {
            return table.fieldExists(getKey().getFieldName());
        }
        return false;
    }

    public boolean existsInDB(EtkDbs database) {
        return database.fieldExists(getKey().getTableName(), getKey().getFieldName());
    }

    /**
     * Liefert in der gewünschten Sprache die Bezeichnung des Feldes;
     * If ForceFieldnames, wird der FeldName in der zugehörigen Datenbanktabelle
     * geliefert.
     *
     * @param config
     * @param language
     * @param forcedFieldNames
     * @return
     */
    public String getNotation(EtkConfig config, String language, boolean forcedFieldNames) {
        String result;
        if (!forcedFieldNames) {
            result = getText().getText(language);
            if (result.isEmpty()) {
                if (isMultiLanguage()) {
                    result = getKey().getFieldName();
                } else {
                    // wie in Konfiguration der Tabellenfelder
                    //???
                    //result = project.getConfig().getLongFieldName(getKey().getTableName(), getKey().getFieldName(), project.getConfig().getDBDescription(), project.getViewerLanguage());
                    result = config.getDisplayAliasForField(getKey().getTableName(), getKey().getFieldName());
                }
            }
        } else {
            result = getKey().getFieldName();
        }
        return result;
    }

    public String getNotation(EtkConfig config, String language, TABLEFIELDNAMEDISPLAYMODE tableFieldNameDisplayMode) {
        String result = "";
        switch (tableFieldNameDisplayMode) {
            case SHOWDBNAMES:
                result = getKey().getFieldName();
                break;
            case SHOWDENOMINATIONS:
                result = getText().getText(language);
                if (result.isEmpty()) {
                    if (isMultiLanguage()) {
                        result = getKey().getFieldName();
                    } else {
                        // wie in Konfiguration der Tabellenfelder
                        //???
                        result = config.getDisplayAliasForField(getKey().getTableName(), getKey().getFieldName());
                    }
                }
                break;
            case SHOWDISPLAYALIASES:
                config.getDisplayAliasForField(getKey().getTableName(), getKey().getFieldName());
                break;
            default:
                result = getKey().getFieldName();
                break;
        }
        return result;
    }

    public String getEditTextFromAttributes(EtkConfig config, DBDataObjectAttributes objAttribs) {
        if (objAttribs.fieldExists(getKey().getFieldName())) {
            return objAttribs.getField(getKey().getFieldName()).getAsString();
        }
        return "";
    }

    public void getSelectionDisplayValues(EtkConfig config, List<String> values) {
        values.clear();
        if (isFieldWithSelectionValues()) {
            EtkEditFieldHelper.getEnumEditFieldDisplayValues(config, getKey().getTableName(), getKey().getFieldName(), values);
        }
    }

    public String getDBValueOfDisplayValue(String displayValue) {
        return EtkEditFieldHelper.getEnumDBValueOfDisplayValue(displayValue, getKey().getTableName(), getKey().getFieldName());
    }

    public boolean hasFieldInAttributes(DBDataObjectAttributes objAttribs) {
        return objAttribs.fieldExists(getKey().getFieldName());
    }

    private void initSortInfo() {
        sortOrderNumber = -1;
        sortDirection = SwingCheckboxListCellRenderer.SORT.ASCENDING;
        sortType = SORTTYPE.NONE;
        isReadOnly = false;
    }

    protected void init() {
        initSortInfo();
        useActDatabaseLanguage = true;
        ignoreAtWildCardSearch = false;
        isEditTextIdAllowed = true;
        noManEdit = false;
    }

    public void assign(EtkEditField source) {
        super.assign(source);
        sortOrderNumber = source.getSortOrderNumber();
        sortDirection = source.getSortDirection();
        sortType = source.getSortType();
        isReadOnly = source.isReadOnly();
        useActDatabaseLanguage = source.isUseActDatabaseLanguage();
        isEditTextIdAllowed = source.isEditTextIdAllowed();
        noManEdit = source.isNoManEdit();
    }

    public void assign(EtkDisplayFieldWithAbstractKey source) {
        if (source instanceof EtkEditField) {
            assign((EtkEditField)source);
        } else {
            super.assign(source);
            init();
        }
    }

}
