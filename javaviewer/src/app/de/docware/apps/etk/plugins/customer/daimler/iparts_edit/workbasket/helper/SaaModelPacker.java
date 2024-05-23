package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchResultGrid;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Verdichtung der Baumuster für die SAA-Arbeitsvorräte (EDS/MBS)
 */
public class SaaModelPacker extends AbstractAttribValuePacker {

    protected Set<String> presetKeyFieldNames;
    protected boolean isRebuildDisabled;  // Wiederhestellen der nicht verdichteten Liste unterbinden
    protected Map<String, Integer> gridRowMap;
    protected int modelColNo;
    protected Map<String, List<DBDataObjectAttributes>> restoreHitMap; // Map für die Attribut-Felder vor der Verdichtung
    protected String extraPackFieldName; // zusätzliche Feld, das für die Wiederherstellung der Liste benötigt wird

    public SaaModelPacker(String arrayIdDelimiter, String modelDelimiter) {
        this(arrayIdDelimiter, modelDelimiter, null);
    }

    public SaaModelPacker(String arrayIdDelimiter, String modelDelimiter, String extraPackFieldName) {
        super(null);
        // Aufbau der packMap (welche Attributewerte sollen verdichtet werden)
        Map<String, String> ownPackMap = new HashMap<>();
        ownPackMap.put(getArrayIdFieldName(), arrayIdDelimiter);
        ownPackMap.put(getModelFieldName(), modelDelimiter);
        setPackMap(ownPackMap);
        this.extraPackFieldName = extraPackFieldName;
        isRebuildDisabled = false;
        gridRowMap = new HashMap<>();
        modelColNo = -1;
        restoreHitMap = new LinkedHashMap<>();
    }

    /**
     * Übergabe der Feldnamen für die Bildung des Keys
     *
     * @param keyFieldName
     */
    public void presetKeyFieldNames(String... keyFieldName) {
        presetKeyFieldNames = new LinkedHashSet<>();

        for (String keyName : keyFieldName) {
            presetKeyFieldNames.add(keyName);
        }
    }

    public void disableRebuild(boolean disabled) {
        isRebuildDisabled = disabled;
    }

    @Override
    public void clear() {
        super.clear();
        gridRowMap.clear();
        modelColNo = -1;
        attributesPackMap.clear();
        restoreHitMap.clear();
    }

    /**
     * eigene Verdichtungs-Routine, die sofort das Model-Feld im Grid aktualisiert
     *
     * @param attributes
     * @param grid
     * @param table
     * @return
     */
    public boolean wasPacked(DBDataObjectAttributes attributes, SimpleMasterDataSearchResultGrid grid, GuiTable table) {
        boolean isPacked = false;
        doFirstCheck(attributes);
        if (isDisabled) {
            return isPacked;
        }
        if (isRebuildDisabled) {
            return super.wasPacked(attributes);
        }
        String key = buildKey(attributes, getKeyFieldNames());
        if (attributesPackMap.containsKey(key)) {
            // merge Models, accumulateArrayIds
            if (accumulateValues(attributes, attributesPackMap.get(key))) {
                isPacked = true;
                isChanged = true;
                Integer rowNo = gridRowMap.get(key);
                if (rowNo != null) {
                    int colNo = getModelColNo(grid);
                    if (colNo != -1) {
                        updateOneGridCell(table, rowNo, colNo, getModelFieldName());
                    }
                }
            }
        } else {
            attributesPackMap.put(key, attributes);
            gridRowMap.put(key, table.getRowCount());
        }
        storeDeltaValues(key, attributes);
        return isPacked;
    }

    /**
     * Berchnung, welche coloumn im Grid der Model-Spalte entspricht (einmalig)
     *
     * @param grid
     * @return
     */
    protected int getModelColNo(SimpleMasterDataSearchResultGrid grid) {
        if (modelColNo == -1) {
            modelColNo = calcColNoForField(grid, getModelFieldName());
        }
        return modelColNo;
    }

    /**
     * Direkter Update der ModelSpalte während der Suche, falls die modelColNo bestimmt werden konnte
     *
     * @param grid
     * @param table
     */
    @Override
    public void updateGrid(SimpleMasterDataSearchResultGrid grid, GuiTable table) {
        if (modelColNo == -1) {
            super.updateGrid(grid, table, getModelFieldName());
        } else {
            //clear();
        }
    }

    /**
     * Vor der Verdichtung die Attributewerte, die verdichtet werden, speichern
     *
     * @param key
     * @param attributes
     */
    protected void storeDeltaValues(String key, DBDataObjectAttributes attributes) {
        if (packMap == null) {
            return;
        }
        List<DBDataObjectAttributes> deltaList = restoreHitMap.get(key);
        if (deltaList == null) {
            deltaList = new DwList<>();
            restoreHitMap.put(key, deltaList);
        }
        DBDataObjectAttributes deltaAttributes = new DBDataObjectAttributes();
        for (Map.Entry<String, String> entry : packMap.entrySet()) {
            String fieldName = entry.getKey();
            if (attributes.fieldInUppercaseExists(fieldName)) {
                deltaAttributes.addField(fieldName, attributes.getFieldValue(fieldName), DBActionOrigin.FROM_DB);
            }
        }
        if (StrUtils.isValid(extraPackFieldName)) {
            if (attributes.fieldInUppercaseExists(extraPackFieldName)) {
                deltaAttributes.addField(extraPackFieldName, attributes.getFieldValue(extraPackFieldName), DBActionOrigin.FROM_DB);
            }
        }
        deltaList.add(deltaAttributes);
    }

    /**
     * Aus der verdichteten Attribute-Liste zumindest angenähert die unverdichtete Liste restaurieren
     * funktioniert NICHT, falls Spalten-Filter(-Sortierung aktiv)
     *
     * @param sortFields
     * @return
     */
    public DBDataObjectAttributesList rebuildHitList(String[] sortFields) {
        DBDataObjectAttributesList result = new DBDataObjectAttributesList();
        if (!isRebuildDisabled) {
            if (!attributesPackMap.isEmpty() && !gridRowMap.isEmpty() && !restoreHitMap.isEmpty()) {
                for (Map.Entry<String, List<DBDataObjectAttributes>> entry : restoreHitMap.entrySet()) {
                    String key = entry.getKey();
                    DBDataObjectAttributes sourceAttributes = attributesPackMap.get(key);
                    if (sourceAttributes != null) {
                        if (entry.getValue().size() > 1) {
                            // bilde aus den gemerkten Attributen die nicht verdichteten Attribute nach
                            for (DBDataObjectAttributes deltaAttributes : entry.getValue()) {
                                DBDataObjectAttributes restoredAttributes = sourceAttributes.cloneMe(DBActionOrigin.FROM_DB);
                                for (DBDataObjectAttribute deltaAttrib : deltaAttributes.getFields()) {
                                    restoredAttributes.addField(deltaAttrib, DBActionOrigin.FROM_DB);
                                }
                                result.add(restoredAttributes);
                            }
                        } else {
                            // nur ein Eintrag => kein restore nötig
                            result.add(sourceAttributes.cloneMe(DBActionOrigin.FROM_DB));
                        }
                    }
                }
                if (sortFields != null) {
                    for (int lfdNr = 0; lfdNr < sortFields.length; lfdNr++) {
                        sortFields[lfdNr] = TableAndFieldName.getFieldName(sortFields[lfdNr]);
                    }
                    result.sortBetterSort(sortFields);
                }
            }
            attributesPackMap.clear();
        }
        return result;
    }

    @Override
    protected Set<String> setKeyFieldNames() {
        return presetKeyFieldNames;
    }

    protected String getArrayIdFieldName() {
        return iPartsConst.FIELD_DWA_ARRAYID;
    }

    protected String getModelFieldName() {
        return iPartsConst.FIELD_DM_MODEL_NO;
    }
}
