/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.util.collections.dwlist.DwList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper für das Update von Grid-zeilen nach Vereinheitlichen von mehreren Zeilen in einem Grid
 */
public class UpdateGridHelper {

    private final EtkProject project;
    private final GuiTable table;
    private final EtkDisplayFields displayResultFields;
    private final OnUpdateGridEvent onUpdateGridEvent;

    public UpdateGridHelper(EtkProject project, GuiTable table, EtkDisplayFields displayResultFields, OnUpdateGridEvent onUpdateGridEvent) {
        this.project = project;
        this.table = table;
        this.displayResultFields = displayResultFields;
        this.onUpdateGridEvent = onUpdateGridEvent;
    }

    public EtkProject getProject() {
        return project;
    }

    public GuiTable getTable() {
        return table;
    }

    /**
     * Update von selektierten Grid-Zeilen nach dem Vereinheitlichen
     *
     * @param editFieldsFromUnify   // Felder vom Vereinheitlichen-Dialog
     * @param attributesFromUnify   // Werte, die der Vereinheitlichen Dialog liefert
     * @param virtualFieldNamesList // Angabe virtueller Felder, die berechnet und aktualisiert werden
     * @param extraFieldNamesList   // zusätzliche FeldNamen der Spalten, die ebenfalls aktualisiert werden sollen
     */
    public void updateGrid(EtkEditFields editFieldsFromUnify, DBDataObjectAttributes attributesFromUnify,
                           List<String> virtualFieldNamesList, List<String> extraFieldNamesList) {
        updateGrid(editFieldsFromUnify, attributesFromUnify, virtualFieldNamesList, extraFieldNamesList,
                   null);
    }

    /**
     * Update von selektierten Grid-Zeilen nach dem Vereinheitlichen
     *
     * @param editFieldsFromUnify   // Felder vom Vereinheitlichen-Dialog
     * @param attributesFromUnify   // Werte, die der Vereinheitlichen Dialog liefert
     * @param virtualFieldNamesList // Angabe virtueller Felder, die berechnet und aktualisiert werden
     * @param extraFieldNamesList   // zusätzliche FeldNamen der Spalten, die ebenfalls aktualisiert werden sollen
     * @param selectedRows          // welche Rows sollen upgedated werden; null: getTable().getSelectedRows()
     */
    public void updateGrid(EtkEditFields editFieldsFromUnify, DBDataObjectAttributes attributesFromUnify,
                           List<String> virtualFieldNamesList, List<String> extraFieldNamesList,
                           List<GuiTableRow> selectedRows) {
        if (onUpdateGridEvent == null) {
            // nichts tun, falls Callback nicht gesetzt ist
            return;
        }
        if (virtualFieldNamesList == null) {
            virtualFieldNamesList = new DwList<>();
        }
        Map<String, UpdateGridContainer> columnMap = calcColumnMap(editFieldsFromUnify, attributesFromUnify,
                                                                   virtualFieldNamesList, extraFieldNamesList);
        if (!columnMap.isEmpty() || !virtualFieldNamesList.isEmpty()) {
            // wenn es keine sichtbaren Felder zum Update gibt (columnMap.isEmpty()), dann aber auf jeden Fall die
            // virtuellen Felder (!virtualFieldNamesList.isEmpty()) aktualisieren
            if (selectedRows == null) {
                selectedRows = getTable().getSelectedRows();
            }
            for (GuiTableRow row : selectedRows) {
                if (row instanceof SimpleSelectSearchResultGrid.GuiTableRowWithAttributes) {
                    // Attribute-Grid
                    SimpleSelectSearchResultGrid.GuiTableRowWithAttributes currentRow = (SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)row;
                    DBDataObjectAttributes currentAttrib = currentRow.attributes;
                    DBDataObjectAttributes newAttributes = setUnifyAttribsAndCalcVirtFields(currentAttrib, columnMap,
                                                                                            editFieldsFromUnify, attributesFromUnify);
                    // Die virtuellen Felder mit den neuen Werten berechnen
                    updateOneRow(currentRow, columnMap, newAttributes);
                    currentRow.attributes = newAttributes;
                } else if (row instanceof DataObjectGrid.GuiTableRowWithObjects) {
                    // DataObject-Grid
                    DataObjectGrid.GuiTableRowWithObjects currentRow = (DataObjectGrid.GuiTableRowWithObjects)row;
                    // In einem Grid können mehrere DataObjects aus verschiedenen Tabellen vorkommen. Hier alle Felder
                    // nach ihren Tabellen ordnen
                    Map<String, EtkEditFields> tableNameToField = new HashMap<>();
                    editFieldsFromUnify.getFields().forEach(editField -> {
                        String tableName = editField.getKey().getTableName();
                        EtkEditFields editFields = tableNameToField.get(tableName);
                        if (editFields == null) {
                            editFields = new EtkEditFields();
                            tableNameToField.put(tableName, editFields);
                        }
                        editFields.addFeld(editField);
                    });

                    // Für jede Tabelle, die Edit-Felder hat, die Anpassung im Objekt und in der Zeile machen
                    tableNameToField.forEach((tableName, editFields) -> {
                        EtkDataObject dataObject = currentRow.getObjectForTable(tableName);
                        if (dataObject != null) {
                            DBDataObjectAttributes currentAttrib = dataObject.getAttributes();
                            DBDataObjectAttributes newAttributes = setUnifyAttribsAndCalcVirtFields(currentAttrib, columnMap,
                                                                                                    editFields, attributesFromUnify);
                            updateOneRow(currentRow, columnMap, newAttributes);
                            dataObject.setAttributes(newAttributes, DBActionOrigin.FROM_DB);
                        }
                    });
                }
            }
        }
    }

    /**
     * Neue Werte aus dem Unify setzen und die virtuellen Felder neu berechnen lassen
     *
     * @param currentAttrib
     * @param columnMap
     * @param editFieldsFromUnify
     * @param attributesFromUnify
     * @return
     */
    private DBDataObjectAttributes setUnifyAttribsAndCalcVirtFields(DBDataObjectAttributes currentAttrib, Map<String, UpdateGridContainer> columnMap,
                                                                    EtkEditFields editFieldsFromUnify, DBDataObjectAttributes attributesFromUnify) {
        boolean checkFieldNamesFromUnify = (editFieldsFromUnify != null) && (editFieldsFromUnify.size() > 0) && (attributesFromUnify != null);
        if (checkFieldNamesFromUnify) {
            // Die Vereinheitlichen-Werte in die aktuellen DBDataObjectAttributes schreiben
            for (EtkEditField editField : editFieldsFromUnify.getFields()) {
                String fieldName = editField.getKey().getFieldName();
                UpdateGridContainer container = columnMap.get(fieldName);
                if (container != null) {
                    container.updateUnifyValue(currentAttrib);
                }
            }
        }
        // die Virtuellen Felder mit den neuen Werten berechnen
        return onUpdateGridEvent.doCalculateVirtualFields(getProject(), currentAttrib);
    }

    /**
     * Den Inhalt betroffene Zellen neu berechnen und direkt austauschen
     *
     * @param row
     * @param fieldColMap
     * @param attributes
     */
    private void updateOneRow(GuiTableRow row, Map<String, UpdateGridContainer> fieldColMap,
                              DBDataObjectAttributes attributes) {
        fieldColMap.values().forEach(container -> container.updateOneCell(row, attributes));
    }

    /**
     * Aus den editFieldsFromUnify, DisplayFields und extraFieldNamesList die betroffenen Spalten bestimmen
     *
     * @param editFieldsFromUnify
     * @param attributesFromUnify
     * @param virtualFieldNamesList
     * @param extraFieldNamesList
     * @return
     */
    private Map<String, UpdateGridContainer> calcColumnMap(EtkEditFields editFieldsFromUnify, DBDataObjectAttributes attributesFromUnify,
                                                           List<String> virtualFieldNamesList, List<String> extraFieldNamesList) {
        Map<String, UpdateGridContainer> fieldColMap = new HashMap<>();
        boolean checkExtraFieldNames = (extraFieldNamesList != null) && !extraFieldNamesList.isEmpty();
        boolean checkFieldNamesFromUnify = (editFieldsFromUnify != null) && (editFieldsFromUnify.size() > 0) && (attributesFromUnify != null);
        int column = 0;
        for (EtkDisplayField displayField : displayResultFields.getFields()) {
            String fieldName = displayField.getKey().getFieldName();
            if (virtualFieldNamesList.contains(displayField.getKey().getName())) {
                if (displayField.isVisible()) {
                    fieldColMap.put(fieldName, new UpdateGridContainer(column, displayField));
                } else {
                    fieldColMap.put(fieldName, new UpdateGridContainer(-1, displayField));
                }
            }
            if (checkExtraFieldNames) {
                if (extraFieldNamesList.contains(displayField.getKey().getName())) {
                    if (fieldColMap.get(fieldName) == null) {
                        if (displayField.isVisible()) {
                            fieldColMap.put(fieldName, new UpdateGridContainer(column, displayField));
                        } else {
                            fieldColMap.put(fieldName, new UpdateGridContainer(-1, displayField));
                        }
                    }
                }
            }
            if (displayField.isVisible()) {
                column++;
            }
        }
        if (checkFieldNamesFromUnify) {
            // zur Sicherheit noch überprüfen, ob die Vereinheitlichen-Felder überhaupt in den DisplayFields konfiguriert sind
            for (EtkEditField editField : editFieldsFromUnify.getFields()) {
                String fieldName = editField.getKey().getFieldName();
                if (fieldColMap.get(fieldName) == null) {
                    int index = displayResultFields.getIndexOfVisibleFeld(editField.getKey().getTableName(), fieldName, false);
                    EtkDisplayField displayField = null;
                    if (index >= 0) {
                        displayField = displayResultFields.getVisibleFields().get(index);
                    }
                    String value = null;
                    DBDataObjectAttribute attrib = attributesFromUnify.getField(fieldName, false);
                    if (attrib != null) {
                        value = attrib.getAsString();
                    }
                    if (value != null) {
                        UpdateGridContainer container = new UpdateGridContainer(index, displayField, value);
                        if (displayField == null) {
                            // Das Unify-Feld ist in den DisplayFields nicht konfiguriert => trotzdem updaten
                            container.fieldName = fieldName;
                        }
                        fieldColMap.put(fieldName, container);
                    }
                }
            }
        }
        return fieldColMap;
    }

    private class UpdateGridContainer {

        private final int column;
        private final String value;
        private final EtkDisplayField displayField;
        private String fieldName;

        public UpdateGridContainer(int column, EtkDisplayField displayField) {
            this(column, displayField, null);
        }

        public UpdateGridContainer(int column, EtkDisplayField displayField, String value) {
            this.column = column;
            this.displayField = displayField;
            this.value = value;
            this.fieldName = null;
            if ((displayField != null) && (value != null)) {
                this.fieldName = displayField.getKey().getFieldName();
            }
        }

        public void updateUnifyValue(DBDataObjectAttributes currentAttributes) {
            if ((fieldName == null) || (value == null)) {
                return;
            }
            DBDataObjectAttribute attrib = currentAttributes.getField(fieldName, false);
            if (attrib != null) {
                attrib.setValueAsString(value, DBActionOrigin.FROM_DB);
            }
        }

        public void updateOneCell(GuiTableRow row, DBDataObjectAttributes currentAttributes) {
            if (column != -1) {
                if (displayField != null) {
                    row.replaceChild(column, onUpdateGridEvent.doCalcGuiElemForCell(getProject(), displayField, currentAttributes));
                }
            }
        }
    }


}
