package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.common.SimpleSelectSearchResultGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchResultGrid;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Abstrakte Klasse für das Verdichten von Attributewerten vor der Ausgabe
 */
public abstract class AbstractAttribValuePacker {

    public static List<String> getValueAsList(String value, String delimiter) {
        return StrUtils.toStringList(value, delimiter, false, false);
    }

    public static String getFirstValueFromList(String value, String delimiter) {
        List<String> list = getValueAsList(value, delimiter);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return "";
    }

    public static String getValueFromList(List<String> valueListe, String delimiter) {
        return StrUtils.stringListToString(valueListe, delimiter);
    }


    protected Set<String> keyFieldNames;
    protected Map<String, DBDataObjectAttributes> attributesPackMap;
    protected Map<String, String> packMap;  //<FieldName, Delimiter>
    protected boolean isChanged;
    protected boolean isDisabled;
    protected boolean isFirstCheckDone;
    protected Map<String, Boolean> validityMap;
    protected ExcludePackingContainer excludePackingField; // Angabe für die Elemente, die NICHT verdichtet werden sollen

    public AbstractAttribValuePacker(Map<String, String> packMap) {
        this.attributesPackMap = new HashMap<>();
        this.isChanged = false;
        this.isDisabled = false;
        this.isFirstCheckDone = false;
        this.keyFieldNames = null;
        this.validityMap = new HashMap<>();
        setPackMap(packMap);
        excludePackingField = null;
    }

    /**
     * Welche Attribute-Werte sollen mit welchem Delimiter verdichtet werdedn
     * paraMap<FieldName, Delimiter>
     *
     * @param packMap
     */
    public void setPackMap(Map<String, String> packMap) {
        this.packMap = packMap;
        validityMap.clear();
    }

    /**
     * Freigeben der Zwischenergebniss
     */
    public void clear() {
        attributesPackMap.clear();
        validityMap.clear();
        isChanged = false;
        isFirstCheckDone = false;
    }

    /**
     * Versichten komplett aus/einschalten
     *
     * @param disabled
     */
    public void disablePacking(boolean disabled) {
        isDisabled = disabled;
    }

    public boolean isPackingDisabled() {
        return isDisabled;
    }

    /**
     * Festlegung des FeldNamens und des Wertes, die nicht verdichtet werden soll
     * insteadFieldName ist dabei der Ersatzwert für Bildung des Keys
     *
     * @param excludeFieldName
     * @param excludeValue
     * @param insteadFieldName
     */
    public void setExcludeValue(String excludeFieldName, String excludeValue, String insteadFieldName) {
        excludePackingField = new ExcludePackingContainer(excludeFieldName, excludeValue, insteadFieldName);
        if (!excludePackingField.isInit) {
            excludePackingField = null;
        }
    }

    protected void doFirstCheck(DBDataObjectAttributes attributes) {
        if (!isFirstCheckDone) {
            if (packMap != null) {
                for (String fieldName : packMap.keySet()) {
                    if (!attributes.containsKey(fieldName)) {
                        isDisabled = true;
                        break;
                    }
                }
            } else {
                isDisabled = true;
            }
            isFirstCheckDone = true;
        }
    }

    /**
     * Zentrale Routine. Bildet den Key und überprüft, od die aktuelle Attributwerte bereits vorkommen
     * Wenn ja, dann werden (via packMap) die Attributwerte aufakkumuliert (keine doppelten)
     *
     * @param attributes
     * @return fals: der Key ist neu (keine Verdichtung); true: Key bereits vorhanden und Verdichtung ausgeführt
     */
    public boolean wasPacked(DBDataObjectAttributes attributes) {
        boolean isPacked = false;
        doFirstCheck(attributes);
        if (isDisabled) {
            return isPacked;
        }
        String key = buildKey(attributes, getKeyFieldNames());
        if (attributesPackMap.containsKey(key)) {
            // merge Models, accumulateArrayIds
            if (accumulateValues(attributes, attributesPackMap.get(key))) {
                isPacked = true;
                isChanged = true;
            }
        } else {
            attributesPackMap.put(key, attributes);
        }
        return isPacked;
    }

    /**
     * Update des gesamten Grids, damit die versichteten Werte auch dargestellt werden
     *
     * @param grid
     * @param table
     */
    public void updateGrid(SimpleMasterDataSearchResultGrid grid, GuiTable table) {
        updateGrid(grid, table, null);
    }

    /**
     * Update des gesamten Grids
     * Je nach Aufruf, wird entweder das gesamte Grid neu geladen, oder nur eine Spalte refreshed
     *
     * @param grid
     * @param table
     * @param fieldName
     */
    public void updateGrid(SimpleMasterDataSearchResultGrid grid, GuiTable table, String fieldName) {
        if (isChanged) {
            int colNo = calcColNoForField(grid, fieldName);

            table.switchOffEventListeners();
            try {
                if (colNo == -1) {
                    // die aktuellen Einträge holen
                    DBDataObjectAttributesList attributesList = grid.getAttributesListFromTable();
                    table.removeRows();
                    // Grid wieder füllen
                    grid.addAttributesListToGrid(attributesList);
                } else {
                    for (int rowNo = 0; rowNo < table.getRowCount(); rowNo++) {
                        updateOneGridCell(table, rowNo, colNo, fieldName);
                    }
                }
            } finally {
                table.switchOnEventListeners();
            }
        }
        clear();
    }

    /**
     * Versuche die Spaltennummer aus dem Feldnamen zu bestimmen
     *
     * @param grid
     * @param fieldName
     * @return
     */
    protected int calcColNoForField(SimpleMasterDataSearchResultGrid grid, String fieldName) {
        int colNo = -1;
        if (StrUtils.isValid(fieldName) && (grid != null)) {
            int intColNo = 0;
            for (EtkDisplayField displayField : grid.getDisplayResultFields().getVisibleFields()) {
                if (displayField.getKey().getFieldName().equals(fieldName)) {
                    colNo = intColNo;
                    break;
                }
                intColNo++;
            }
        }
        return colNo;
    }

    /**
     * Eine Grid-Telle direkt updaten
     * Vorsicht: hier wird nicht typrichtig gearbeitet, sondern nur als String
     *
     * @param table
     * @param rowNo
     * @param colNo
     * @param fieldName
     */
    protected void updateOneGridCell(GuiTable table, int rowNo, int colNo, String fieldName) {
        GuiTableRow row = table.getRow(rowNo);
        if (row instanceof SimpleSelectSearchResultGrid.GuiTableRowWithAttributes) {
            DBDataObjectAttributes attributes = ((SimpleSelectSearchResultGrid.GuiTableRowWithAttributes)row).attributes;
            String value = attributes.getFieldValue(fieldName);
            GuiLabel label = new GuiLabel(value);
            row.replaceChild(colNo, label);
        }
    }

    /**
     * Verdichtung der hinterlegten Attribute-Werte (packMap)
     *
     * @param currentAttributes
     * @param storedAttributes
     * @return
     */
    protected boolean accumulateValues(DBDataObjectAttributes currentAttributes, DBDataObjectAttributes storedAttributes) {
        if (packMap == null) {
            return false;
        }
        boolean somethingWasPacked = false;
        for (Map.Entry<String, String> entry : packMap.entrySet()) {
            String fieldName = entry.getKey();
            if (validityMap.get(fieldName)) {
                String delimiter = entry.getValue();
                Set<String> mergedValues = new LinkedHashSet<>(getValueAsList(storedAttributes.getFieldValue(fieldName), delimiter));
                mergedValues.addAll(getValueAsList(currentAttributes.getFieldValue(fieldName), delimiter));
                String packedValue = getValueFromList(mergedValues, delimiter);
                storedAttributes.addField(fieldName, packedValue, DBActionOrigin.FROM_DB);

                somethingWasPacked = true;
            }
        }
        return somethingWasPacked;
    }

    /**
     * Hilfs-Key für die Bestimmung des GleichheitsKeys {@code fieldNames} bestimmen
     *
     * @param attributes
     * @param fieldNames
     * @return
     */
    public String buildKey(DBDataObjectAttributes attributes, Collection<String> fieldNames) {
        setValidityMap(attributes, fieldNames);

        StringBuilder str = new StringBuilder();
        for (String fieldName : fieldNames) {
            if (str.length() > 0) {
                str.append("&");
            }
            if (validityMap.get(fieldName)) {
                if (excludePackingField != null) {
                    str.append(excludePackingField.getValueFromAttributes(attributes, fieldName));
                } else {
                    str.append(attributes.getFieldValue(fieldName));
                }
            }
        }
        return str.toString();
    }

    /**
     * einmalige Bestimmung, ob die angeforderten Felder auch in den Attributen vorhanden sind
     *
     * @param attributes
     * @param fieldNames
     */
    private void setValidityMap(DBDataObjectAttributes attributes, Collection<String> fieldNames) {
        if (validityMap.isEmpty()) {
            // damit nicht jedesmal attributes.containsKey(fieldName) aufgerufen werden muss
            for (String fieldName : fieldNames) {
                validityMap.put(fieldName, attributes.containsKey(fieldName));
            }
            if (packMap != null) {
                for (String fieldName : packMap.keySet()) {
                    if (validityMap.get(fieldName) == null) {
                        validityMap.put(fieldName, attributes.containsKey(fieldName));
                    }
                }
            }
            if (excludePackingField != null) {
                String fieldName = excludePackingField.insteadFieldName;
                if (validityMap.get(fieldName) == null) {
                    validityMap.put(fieldName, attributes.containsKey(fieldName));
                }
            }
        }
    }

    protected String getValueFromList(Set<String> valueListe, String delimiter) {
        return getValueFromList(new DwList<>(valueListe), delimiter);
    }

    protected Set<String> getKeyFieldNames() {
        if (keyFieldNames == null) {
            keyFieldNames = setKeyFieldNames();
        }
        return keyFieldNames;
    }

    protected abstract Set<String> setKeyFieldNames();

    /**
     * Container für die Werte, welcher Zustand nicht verdichtet werden soll.
     * Attribut excludeFieldName und exludeValue (kann auch für mehrere Werte aufgebohrt werden)
     * insteadFieldName ist dabei der Ersatzwert für Bildung des Keys
     */
    private class ExcludePackingContainer {

        private String excludeFieldName;
        private List<String> excludeValues;
        private String insteadFieldName;
        private boolean isInit;

        public ExcludePackingContainer(String excludeFieldName, String excludeValue, String insteadFieldName) {
            this.isInit = StrUtils.isValid(excludeFieldName, excludeValue, insteadFieldName);
            if (this.isInit) {
                this.excludeFieldName = excludeFieldName;
                this.excludeValues = new DwList<>();
                this.excludeValues.add(excludeValue);
                this.insteadFieldName = insteadFieldName;
            }
        }

        public String getValueFromAttributes(DBDataObjectAttributes attributes, String fieldName) {
            String value = attributes.getFieldValue(fieldName);
            if (isInit) {
                if (fieldName.equals(excludeFieldName) && excludeValues.contains(value)) {
                    value = attributes.getFieldValue(insteadFieldName);
                }
            }
            return value;
        }
    }
}

