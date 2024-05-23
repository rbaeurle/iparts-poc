/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMConstKitTextId;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hilfsklasse für alle Stücklisten- und Textpositionen zu einem Baukasten
 */
public class SaaPartsListConstKitData implements iPartsConst {

    private final String level;
    private final List<SaaPartsListRowData> partsListRowData;
    private Map<iPartsBOMConstKitTextId, DBDataObjectAttributes> textForConstructionKitMap;
    private Map<String, EtkMultiSprache> remarks;
    private Map<String, String> wwTexts;

    public SaaPartsListConstKitData(List<SaaPartsListRowData> highLevelRows,
                                    Map<iPartsBOMConstKitTextId, DBDataObjectAttributes> textForConstructionKitMap, String level) {
        this.partsListRowData = highLevelRows;
        this.level = level;
        this.textForConstructionKitMap = textForConstructionKitMap;
        sortTexts();
    }

    public List<SaaPartsListRowData> getPartsListRowData() {
        return partsListRowData;
    }

    public Map<iPartsBOMConstKitTextId, DBDataObjectAttributes> getTextForConstructionKitMap() {
        return textForConstructionKitMap;
    }

    /**
     * Sortiert die Texte nach ihrer Positionsnummer
     */
    public void sortTexts() {
        if (textForConstructionKitMap == null) {
            return;
        }

        // Map nach Positionsnummern in den Values sortieren
        textForConstructionKitMap = textForConstructionKitMap.entrySet().stream()
                .sorted(Comparator.comparing(o -> o.getValue().getFieldValue(FIELD_DCP_PARTPOS)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existingKey, newKey) -> newKey, LinkedHashMap::new));
    }

    public Map<String, EtkMultiSprache> getRemarks() {
        return remarks;
    }

    public void setRemarks(Map<String, EtkMultiSprache> remarks) {
        this.remarks = remarks;
    }

    public Map<String, String> getWwTexts() {
        return wwTexts;
    }

    public void setWWTexts(Map<String, String> wwTexts) {
        this.wwTexts = wwTexts;
    }

    public EtkMultiSprache getRemarkForRemarkNo(String remarkNo) {
        if (remarks != null) {
            return remarks.get(remarkNo);
        }
        return null;
    }

    public String getWWTextForWWFlag(String wwFlag) {
        if (wwTexts != null) {
            return StrUtils.getEmptyOrValidString(wwTexts.get(wwFlag));
        }
        return "";
    }

    public String getLevel() {
        return level;
    }
}
