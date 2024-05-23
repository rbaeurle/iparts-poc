/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFieldKeyNormal;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.*;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.filter.EtkFilterTyp;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.misc.DWCursor;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.controls.table.GuiTableRowSorterInterface;
import de.docware.framework.modules.gui.controls.table.TableRowInterface;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerFireOnce;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Erweiterung des {@link SimpleMasterDataSearchResultGrid}s mit Spaltenfiltern
 * Solange der Inhalt des {@link SimpleMasterDataSearchResultGrid}s nicht verändert wird, sind keine weiteren Aktionen nötig
 */
public class SimpleMasterDataSearchFilterGrid extends SimpleMasterDataSearchResultGrid {

    /**
     * Gefundene Sprachen für (R)ComboBox und Tabellen-Spaltenfilter umsortieren
     *
     * @param control
     */
    public static void modifyLangTableFilterComboBox(EtkProject project, AbstractGuiControl control) {
        modifyLangTableFilterComboBox(control, iPartsLanguage.getAvailDaimlerLanguages(project));
    }

    public static void modifyLangTableFilterComboBox(EtkProject project, AbstractGuiControl control, boolean withLangText) {
        modifyLangTableFilterComboBox(control, iPartsLanguage.getAvailDaimlerLanguages(project), withLangText);
    }

    public static void modifyLangTableFilterComboBox(AbstractGuiControl control, List<Language> languagesForSort) {
        modifyLangTableFilterComboBox(control, languagesForSort, true);
    }

    public static void modifyLangTableFilterComboBox(AbstractGuiControl control, List<Language> languagesForSort, boolean withLangText) {
        // Sprachen umsortieren
        if (control instanceof EnumCheckComboBox) {
            EnumCheckComboBox comboBox = (EnumCheckComboBox)control;
            List<String> items = comboBox.getItems();
            if (!items.isEmpty()) {
                List<String> tokens = new DwList(comboBox.getTokens());
                comboBox.removeAllItems();
                for (Language lang : languagesForSort) {
                    int index = items.indexOf(lang.getCode());
                    if (index != -1) {
                        addElemToComboBox(comboBox, tokens.get(index), getComboTextForLangTableFilter(lang, withLangText));
                    }
                }
            }
        } else if (control instanceof EnumCheckRComboBox) {
            EnumCheckRComboBox comboBox = (EnumCheckRComboBox)control;
            List<String> items = comboBox.getItems();
            if (!items.isEmpty()) {
                List<String> tokens = new DwList(comboBox.getTokens());
                comboBox.removeAllItems();
                for (Language lang : languagesForSort) {
                    int index = items.indexOf(lang.getCode());
                    if (index != -1) {
                        addElemToComboBox(comboBox, tokens.get(index), getComboTextForLangTableFilter(lang, withLangText));
                    }
                }
            }
        }
    }

    public static String getComboTextForLangTableFilter(Language lang) {
        return getComboTextForLangTableFilter(lang, true);
    }

    public static String getComboTextForLangTableFilter(Language lang, boolean withName) {
        if (withName) {
            return lang.getCode() + " - " + TranslationHandler.translate(lang.getDisplayName());
        } else {
            return lang.getCode();
        }
    }

    public static void addElemToComboBox(EnumCheckRComboBox comboBox, String token, String item) {
        comboBox.addItem(token, item);
        comboBox.getTokens().add(token);
    }

    public static void addElemToComboBox(EnumCheckComboBox comboBox, String token, String item) {
        comboBox.addItem(token, item);
        comboBox.getTokens().add(token);
    }


    private SimpleMasterDataSearchFilterFactory columnFilterFactory;
    private List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries;
    private EventListener resetSortEvent;
    private GridFilterAndSortStorage filterAndSortSettings;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public SimpleMasterDataSearchFilterGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                            String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        setColumnFilterFactory(new SimpleMasterDataSearchFilterFactory(getProject()));
        this.entries = null;
        this.resetSortEvent = null;
        installDefaultResetSortEvent();
        getTable().addEventListener(new EventListener(Event.TABLE_COLUMN_FILTER_CHANGED_EVENT) {
            @Override
            public void fire(Event event) {
                if (isSelectCountVisible()) {
                    showResultCount();
                }
            }
        });
    }

    @Override
    public void setDisplayResultFields(EtkDisplayFields displayFields, boolean useConfiguredColumnWidths) {
        super.setDisplayResultFields(displayFields, useConfiguredColumnWidths);
        // Implementierung vom Spalten-Filter
        getTable().setColumnFilterFactory(columnFilterFactory);
        setHeaderForFilter(displayFields);
    }

    protected void setFilterAndSortSettings() {
        filterAndSortSettings = new GridFilterAndSortStorage(SimpleMasterDataSearchFilterGrid.this, true);
    }

    @Override
    protected void endSearch() {
        super.endSearch();
        if (filterAndSortSettings != null) {
            filterAndSortSettings.restoreGridFilterAndSort(this);
            filterAndSortSettings = null;
            DBDataObjectAttributes attributesSelected = getSelectedAttributes();
            if ((attributesSelected != null) && !attributesSelected.isEmpty()) {
                setSelection(true, attributesSelected);
            }
        }
    }

    public void doEndSearch() {
        endSearch();
    }

    public void setColumnFilterFactory(SimpleMasterDataSearchFilterFactory columnFilterFactory) {
        this.columnFilterFactory = columnFilterFactory;
    }

    public void setResetSortEvent(EventListener resetSortEvent) {
        disableResetSortEvent();
        this.resetSortEvent = resetSortEvent;
        if (this.resetSortEvent != null) {
            getTable().addEventListener(this.resetSortEvent);
        }
    }

    public void disableResetSortEvent() {
        if (resetSortEvent != null) {
            getTable().removeEventListener(resetSortEvent);
        }
    }

    protected void installDefaultResetSortEvent() {
        disableResetSortEvent();
        resetSortEvent = new EventListenerFireOnce(Event.TABLE_COLUMN_SORTED_EVENT, getTable()) {
            @Override
            public boolean isFireOnceValid(Event event) {
                return (getTable().getRowCount() > 0) && (getTable().getSortColumn() < 0);
            }

            @Override
            public void fireOnce(Event event) {
                try {
                    if ((getTable().getRowCount() > 0) && (getTable().getSortColumn() < 0)) {
                        // Sortierung aufheben geklickt
                        // Da in den sortFields mehrere Spalten vorkommen können und auch die Suchordnung pro
                        // Spalte unterschiedlich sein kann, wird ein neuer SimpleGridTableRowSorter benutzt,
                        // der dies berücksichtigt.
                        // Der alte SimpleGridTableRowSorter wird deswegen gesichert und anschliesend wieder gesetzt
                        if ((sortFields != null) && !sortFields.isEmpty()) {
                            GuiTableRowSorterInterface oldRowSorter = getTable().getRowSorter();
                            // der Sorter registriert sich selbst innerhalb des Konstruktors bei der Table
                            new ResetSortTableRowSorter(getProject(), getTable(), displayResultFields, sortFields);
                            // 1. Spalte der Sortierung bestimmen für sortRowsAccordingToColumn()
                            // den Rest übernimmt ResetSortTableRowSorter
                            String tableName = searchTable;

                            // nimm die Angaben aus dem ersten SortField
                            Map.Entry<String, Boolean> sortField = sortFields.entrySet().iterator().next();
                            String fieldName = sortField.getKey();
                            boolean descending = sortField.getValue();
                            String sortFieldTableName = TableAndFieldName.getTableName(fieldName);
                            if (!sortFieldTableName.isEmpty()) {
                                tableName = sortFieldTableName;
                                fieldName = TableAndFieldName.getFieldName(fieldName);
                            }
                            if (StrUtils.isValid(fieldName)) {
                                int column = displayResultFields.getIndexOfVisibleFeld(tableName, fieldName, false);
                                if (column >= 0) {
                                    getTable().sortRowsAccordingToColumn(column, !descending);
                                    getTable().clearSort();
                                }
                            }
                            // alten rowSorter wieder herstellen
                            getTable().setRowSorter(oldRowSorter);
                        }
                    }
                } finally {
                    // wieder TABLE_COLUMN_SORTED_EVENT-Events zulassen
                    resetFired();
                    getTable().addEventListener(resetSortEvent);
                }
            }
        };
        getTable().addEventListener(resetSortEvent);
    }

    // eigener Sorter um die Basis-Sortierung nach den sortFields wieder herzustellen
    // Bis auf addMoreCompareFields() und der eigentlichen Sortiertroutine (Collections.sort)
    // entspricht er dem SimpleGridTableRowSorter
    private class ResetSortTableRowSorter extends SimpleGridTableRowSorter {

        protected LinkedHashMap<String, Boolean> sortFields;

        public ResetSortTableRowSorter(EtkProject project, GuiTable table, EtkDisplayFields displayResultFields,
                                       LinkedHashMap<String, Boolean> sortFields) {
            super(project, table, displayResultFields);
            this.sortFields = sortFields;
        }

        @Override
        protected void doSort(List<AbstractSortingObject> sortedPartListEntries, List<EtkDisplayFieldKeyNormal> compareFields,
                              boolean sortAscending, boolean sortCaseInsensitive) {
            // Sortieren
            Collections.sort(sortedPartListEntries, new Comparator<AbstractSortingObject>() {
                @Override
                public int compare(AbstractSortingObject o1, AbstractSortingObject o2) {
                    for (EtkDisplayFieldKeyNormal fieldName : compareFields) {
                        if (!fieldName.getFieldName().isEmpty()) {
                            String s1 = o1.getFieldValue(project, fieldName.getTableName(), fieldName.getFieldName());
                            String s2 = o2.getFieldValue(project, fieldName.getTableName(), fieldName.getFieldName());

                            // Sortierung nach der Automatischen Sortierung
                            s1 = sortStringCache.getSortString(s1, sortCaseInsensitive);
                            s2 = sortStringCache.getSortString(s2, sortCaseInsensitive);

                            int result = s1.compareTo(s2);
                            if (result != 0) {
                                // Sortier-Ordnung aus den sortFields holen
                                Boolean doSortDescending = sortFields.get(fieldName.getFieldName());
                                if (doSortDescending == null) {
                                    doSortDescending = false;
                                }
                                return doSortDescending ? -result : result;
                            }
                        }
                    }
                    return 0;
                }
            });
        }

        @Override
        protected AbstractSortingObject getSortingObject(EtkProject project, String sortTableName, TableRowInterface row, int lfdNr) {
            return super.getSortingObject(project, sortTableName, row, lfdNr);
        }

        @Override
        protected void addMoreCompareFields(List<EtkDisplayFieldKeyNormal> compareFields) {
            // weitere Felder (Spalten) aus den sortFields für die Sortierung hinzufügen
            // Die erste Spalte ist bereits bestimmt
            boolean firstValue = true;
            for (Map.Entry<String, Boolean> sortEntry : sortFields.entrySet()) {
                if (firstValue) {
                    firstValue = false;
                    continue;
                }
                String tableName = searchTable;
                String fieldName = sortEntry.getKey();
                if (!TableAndFieldName.getTableName(fieldName).isEmpty()) {
                    tableName = TableAndFieldName.getTableName(fieldName);
                    fieldName = TableAndFieldName.getFieldName(fieldName);
                }
                int sortColumnIndex = displayResultFields.getIndexOfVisibleFeld(tableName, fieldName, false);
                if (sortColumnIndex != -1) {
                    EtkDisplayField displayField = getRealDisplayField(sortColumnIndex);
                    compareFields.add(displayField.getKey());
                }
            }
        }
    }

    protected void setHeaderForFilter(EtkDisplayFields displayFields) {
        if (displayFields != null) {
            // Filterung für Stücklistentabelle aus DWK auslesen
            int col = 0;
            for (EtkDisplayField field : displayFields.getFields()) {
                if (field.isVisible()) {
                    if (field.isColumnFilterEnabled()) {
                        getTable().setFilterForColumn(col, true);
                    }
                    col++;
                }
            }
        }
    }

    @Override
    protected void setGridHeader(EtkDisplayFields resultFields, boolean useConfiguredColumnWidths) {
        super.setGridHeader(resultFields, useConfiguredColumnWidths);
        setHeaderForFilter(resultFields);
    }

    @Override
    protected void prepareGuiForSearch() {
        super.prepareGuiForSearch();

        // clearEntries muss vor UND nach dem Aufruf von getTable().clearAllFilterValues() aufgerufen werden, weil es
        // ansonsten zu doppelten Einträgen kommen würde (wenn der Aufruf vorher fehlt) oder bei erneuter Spaltenfilterung die
        // Tabelle immer leer wäre (wenn der Aufruf nachher fehlt), da durch getTable().clearAllFilterValues() indirekt
        // die Methode getEntries() aufgerufen wird, die entries aufgrund der leeren Tabelle nun auch leer initialisieren würde
        clearEntries();
        getTable().clearAllFilterValues();
        clearEntries();
    }

    protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> getEntries() {
        if (entries == null) {
            entries = getEntriesAsList();
        }
        return entries;
    }

    protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> getEntriesAsList() {
        int rowCount = getTable().getRowCount();
        List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entriesAsList = new DwList<>(rowCount);
        for (int rowNo = 0; rowNo < rowCount; rowNo++) {
            entriesAsList.add((SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)getTable().getRow(rowNo));
        }
        return entriesAsList;
    }

    public List<GuiTableRow> getRowsWithFieldValueEquals(String fieldName, String fieldValueToCompare) {
        int rowCount = getTable().getRowCount();
        List<GuiTableRow> selectedRows = new DwList<>(rowCount);
        for (int row = 0; row < rowCount; row++) {
            SimpleSelectSearchResultGrid.GuiTableRowWithAttributes tableRow = (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)getTable().getRow(row);
            if (tableRow.attributes.getFieldValue(fieldName).equals(fieldValueToCompare)) {
                selectedRows.add(tableRow);
            }
        }

        if (selectedRows.isEmpty()) {
            selectedRows = null;
        }
        return selectedRows;
    }

    public List<GuiTableRow> getRowsWithFieldValueEquals(Map<String, String> fieldNamesValuesMap) {
        int rowCount = getTable().getRowCount();
        List<GuiTableRow> selectedRows = new DwList<>(rowCount);
        if ((fieldNamesValuesMap != null) && !fieldNamesValuesMap.isEmpty()) {
            for (int row = 0; row < rowCount; row++) {
                SimpleSelectSearchResultGrid.GuiTableRowWithAttributes tableRow = (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)getTable().getRow(row);
                if (areValuesEqual(tableRow.attributes, fieldNamesValuesMap)) {
                    selectedRows.add(tableRow);
                }
            }
        }

        if (selectedRows.isEmpty()) {
            selectedRows = null;
        }
        return selectedRows;
    }

    public boolean setSelectionByTableRows(List<GuiTableRow> selectedRows) {
        if ((selectedRows != null) && !selectedRows.isEmpty()) {
            List<Integer> selectedIndices = new DwList<>();
            for (GuiTableRow tableRow : selectedRows) {
                int index = getTable().getRowIndex(tableRow);
                if (index >= 0) {
                    selectedIndices.add(index);
                }
            }
            if (!selectedIndices.isEmpty()) {
                getTable().setSelectedRows(((DwList<Integer>)selectedIndices).toIntArray(), true, true);
                return true;
            }
        }
        return false;
    }

    protected boolean areValuesEqual(DBDataObjectAttributes attributes, Map<String, String> fieldNamesValuesMap) {
        if (fieldNamesValuesMap.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, String> entry : fieldNamesValuesMap.entrySet()) {
            String fieldName = entry.getKey();
            if (!attributes.getFieldValue(fieldName).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    protected void clearEntries() {
        entries = null;
    }

    public List<Integer> getFilteredColumns() {
        List<Integer> columnList = new DwList<>();
        columnList.addAll(columnFilterFactory.getColumnFilterEditControlsMap().keySet());
        return columnList;
    }

    public void setFilteredColumns(List<Integer> columnList) {
        clearEntries();
        columnFilterFactory.setFilterFromExtern(getTable(), columnList);
    }

    public void updateFilters() {
        if (getTable().getColumnFilterFactory() != null) {
            if (!columnFilterFactory.getColumnFilterEditControlsMap().isEmpty()) {
                List<Integer> columnList = getFilteredColumns();
                setFilteredColumns(columnList);
            } else {
                clearEntries();
            }
        }
    }

    protected void updateHeaderForColumnFilter() {
        int colCount = getTable().getColCount();
        for (int col = 0; col < colCount; col++) {
            if (getTable().hasFilterForColumn(col)) {
                AbstractGuiControl control = null;
                EditControlFactory editControl = columnFilterFactory.getColumnFilterEditControlsMap().get(col);
                if (editControl != null) {
                    control = columnFilterFactory.setFilterValue(col, editControl.getControl(), false);
                }
                getTable().updateHeaderForColumnFilter(col, control);
            }
        }
    }

    /**
     * Überprüft, ob in den übergebenen <code>editField</code> Primärschlüssel Felder enthalten sind. Falls ja, werden
     * diese als "Muss-Felder" deklariert und auf nicht editierbar gesetzt.
     *
     * @param dataConnector
     * @param editFields
     * @param tableName     Tabelle aus der die Editierfelder bezogen wurden
     */
    protected static void disablePKFieldsForEdit(AbstractJavaViewerFormIConnector dataConnector, EtkEditFields editFields,
                                                 String tableName) {
        if (StrUtils.isValid(tableName) && (editFields.size() > 0)) {
            EtkDatabaseTable tableDef = dataConnector.getConfig().getDBDescription().findTable(tableName);
            List<String> pkFields = tableDef.getPrimaryKeyFields();
            for (EtkEditField eField : editFields.getFields()) {
                if (pkFields.contains(eField.getKey().getFieldName())) {
                    eField.setMussFeld(true);
                    eField.setEditierbar(false);  // ist PK-Value => nicht editierbar
                }
            }
        }
    }

    public AbstractGuiTableColumnFilterFactory storeFilterFactory(Map<Integer, Object> columnFilterValuesMap) {
        AbstractGuiTableColumnFilterFactory copyColumnFilterFactory = null;
        AbstractGuiTableColumnFilterFactory realColumnFilterFactory = null;
        if (getTable().getColumnFilterFactory() instanceof SimpleMasterDataSearchFilterFactory) {
            realColumnFilterFactory = (SimpleMasterDataSearchFilterFactory)getTable().getColumnFilterFactory();
        }
        if ((columnFilterValuesMap != null) && (realColumnFilterFactory != null) &&
            !realColumnFilterFactory.getColumnFilterEditControlsMap().isEmpty()) {
            columnFilterValuesMap.clear();
            for (Map.Entry<Integer, EditControlFactory> entry : realColumnFilterFactory.getColumnFilterEditControlsMap().entrySet()) {
                columnFilterValuesMap.put(entry.getKey(), entry.getValue().getControl());
            }
            copyColumnFilterFactory = new SimpleMasterDataSearchFilterFactory(null);
            copyColumnFilterFactory.assign(realColumnFilterFactory);
        }
        return copyColumnFilterFactory;
    }

    public void restoreFilterFactory(AbstractGuiTableColumnFilterFactory copyColumnFilterFactory, Map<Integer, Object> columnFilterValuesMap) {
        AbstractGuiTableColumnFilterFactory realColumnFilterFactory = null;
        if (getTable().getColumnFilterFactory() instanceof SimpleMasterDataSearchFilterFactory) {
            realColumnFilterFactory = (SimpleMasterDataSearchFilterFactory)getTable().getColumnFilterFactory();
        }
        if ((copyColumnFilterFactory != null) && (columnFilterValuesMap != null) &&
            (realColumnFilterFactory != null) && (copyColumnFilterFactory instanceof SimpleMasterDataSearchFilterFactory)) {
            realColumnFilterFactory.assign(copyColumnFilterFactory);
            for (Map.Entry<Integer, Object> entry : columnFilterValuesMap.entrySet()) {
                getTable().setFilterValueForColumn(entry.getKey(), entry.getValue(), false, true);
            }
            realColumnFilterFactory.doFilterTableEntries();
        }
    }

    /**
     * Object mit den gespeicherten Filter- und Sortier-Settings
     * Erzeugen VOR clearGrid(); Reset mit restoreFilterAndSortSettings() nach dem Füllen des Grid.
     */
    protected static class GridFilterAndSortStorage {

        private int sortColumn;
        private boolean isSortAscending;
        private Map<Integer, Object> columnFilterValuesMap;
        private AbstractGuiTableColumnFilterFactory storedFilterFactory;


        private GridFilterAndSortStorage() {
            sortColumn = -1;
            isSortAscending = false;
            columnFilterValuesMap = null;
            storedFilterFactory = null;
        }

        public GridFilterAndSortStorage(SimpleMasterDataSearchFilterGrid grid, boolean withFilterFactory) {
            this();
            init(grid, withFilterFactory);
        }

        private void init(SimpleMasterDataSearchFilterGrid grid, boolean withFilterFactory) {
            if (grid != null) {
                sortColumn = grid.getTable().getSortColumn();
                isSortAscending = grid.getTable().isSortAscending();
                if (withFilterFactory) {
                    columnFilterValuesMap = new HashMap<>();
                    storedFilterFactory = grid.storeFilterFactory(columnFilterValuesMap);
                }
            }
        }

        /**
         * Übernahme der in dieser {@link GridFilterAndSortStorage} hinterlegten Filer- und Sortier-Settings
         */
        public void restoreGridFilterAndSort(SimpleMasterDataSearchFilterGrid grid) {
            DBDataObjectAttributes selection = grid.getSelection();
            if ((columnFilterValuesMap != null) && (storedFilterFactory != null)) {
                // Spaltenfilter wiederherstellen
                if (!columnFilterValuesMap.isEmpty()) {
                    grid.restoreFilterFactory(storedFilterFactory, columnFilterValuesMap);
                }
            } else {
                grid.updateFilters();
            }
            if (sortColumn != -1) {
                // Sortierung wiederherstellen
                grid.getTable().sortRowsAccordingToColumn(sortColumn, isSortAscending);
            }
            if (selection != null) {
                grid.setSelection(false, selection);
            }
        }
    }


    public class SimpleMasterDataSearchFilterFactory extends AbstractGuiTableColumnFilterFactory {

        public SimpleMasterDataSearchFilterFactory(EtkProject project) {
            super(project);
        }

        /**
         * Plugins können Spalten VOR den eigentlichen localDisplayFields einbauen.
         * Damit ändert sich der Index für die erste Spalte mit Daten aus den localDisplayFields.
         *
         * @return
         */
        @Override
        protected int doCalculateStartColumnIndexForDisplayFields() {
            return 0;
        }

        /**
         * Liefert die aktuellen DisplayFields für das jeweilige Grid
         *
         * @return
         */
        @Override
        protected List<EtkDisplayField> getDisplayFieldList() {
            return getDisplayResultFields().getFields();
        }

        @Override
        protected boolean changeColumnTableFilterValues(int column, EditControlFactory editControl) {
            EtkFieldType fieldType = editControl.getField().getType();
            if (editControl.isHandleAsSetOfEnum() || (fieldType == EtkFieldType.feEnum) ||
                (fieldType == EtkFieldType.feSetOfEnum)) {
                Set<String> valueSet = new TreeSet<>();
                for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes entry : getEntriesAsList()) {
                    String value = entry.attributes.getField(editControl.getField().getName()).getAsString();
                    if ((fieldType == EtkFieldType.feSetOfEnum) && !editControl.isHandleAsSetOfEnum()) {
                        valueSet.addAll(SetOfEnumDataType.parseSetofEnum(value, true, false));
                    } else {
                        valueSet.add(value);
                    }
                }
                setColumnTableFilterValuesForEnum(editControl, valueSet, getProject());
                return true;
            }
            return false;
        }

        /**
         * Die eigentliche Filterung durchführen
         */
        @Override
        public void doFilterTableEntries() {
            GuiWindow currentRootWindow = getGui().getRootWindow();
            if (currentRootWindow != null) {
                currentRootWindow.setCursor(DWCursor.Wait);
            }

            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries = calculateFilteredList();

            //gefilterte oder originale PartListEntries anzeigen
            replaceTableEntries(entries);

            if (currentRootWindow != null) {
                currentRootWindow.setCursor(DWCursor.Default);
            }
        }

        protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> calculateFilteredList() {
            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries;
            if (getAssemblyListFilter().isDBFilterActive()) {
                entries = new DwList<>();
                String language = getProject().getViewerLanguage();
                //filtern
                for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes entry : getEntries()) {
                    if (getAssemblyListFilter().checkFilter(searchTable, entry.attributes, language)) {
                        entries.add(entry); // Eintrag wurde nicht ausgefiltert
                    }
                }
            } else {
                entries = new DwList<>(getEntries());
            }
            return entries;
        }

        /**
         * Falls der Tabellenname des FilterItems nicht searchTable ist
         *
         * @return
         */
        protected List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> calculateFilteredListWithTableNameFromFilterItems() {
            List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries;
            if (getAssemblyListFilter().isDBFilterActive()) {
                entries = new DwList<>();
                String language = getProject().getViewerLanguage();
                //filtern
                for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes entry : getEntries()) {
                    if (getAssemblyListFilter().isDBFilterActive()) {
                        Collection<EtkFilterTyp> activeFilter = getAssemblyListFilter().getActiveFilter();
                        boolean isValid = false;
                        for (EtkFilterTyp filterTyp : activeFilter) {
                            if (filterTyp.getFilterTypus() == EtkFilterTyp.FilterTypus.GRIDFILTER) {
                                String tableName = filterTyp.getFilterItems().get(0).getTableName();
                                if (getAssemblyListFilter().checkFilter(tableName, entry.attributes, language)) {
                                    isValid = true;
                                } else {
                                    isValid = false;
                                    break;
                                }
                            }
                        }
                        if (isValid) {
                            entries.add(entry); // Eintrag wurde nicht ausgefiltert
                        }
                    }
                }
            } else {
                entries = new DwList<>(getEntries());
            }
            return entries;
        }

        protected void replaceTableEntries(List<SimpleSelectSearchResultGrid.GuiTableRowWithAttributes> entries) {
            try {
                getTable().switchOffEventListeners();
                int oldSortColumn = getTable().getSortColumn();
                getTable().removeRows();  // hebt die Sortierung intern auf
                for (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes rowObject : entries) {
                    getTable().addRow(rowObject);
                }

                // Sortierung wiederherstellen falls vorher sortiert war
                if (getTable().isSortEnabled() && (oldSortColumn >= 0)) {
                    getTable().sortRowsAccordingToColumn(oldSortColumn, getTable().isSortAscending());
                }
            } finally {
                getTable().switchOnEventListeners();
            }
            showNoResultsLabel(entries.isEmpty(), false);
        }
    }
}
