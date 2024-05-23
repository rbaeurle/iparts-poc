/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import com.owlike.genson.annotation.JsonProperty;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.enums.EnumUtils;

import java.util.*;

/**
 * Hilfsklasse zum bearbeiten von Werten in einem TwoGrid Dialog
 */
public class TwoGridValues implements RESTfulTransferObjectInterface {

    public static class ValueState implements RESTfulTransferObjectInterface {

        @JsonProperty
        public String value;
        @JsonProperty
        public boolean checked;
        @JsonProperty
        public EnumSet<EnrichReasons> enrichReasons;
        @JsonProperty
        public String sourceSeriesNumber;  // todo das gehört in eine allgemeine Hilfsklasse?
        @JsonProperty
        public String sourceProductGroup;  // todo das gehört in eine allgemeine Hilfsklasse?

        public ValueState() {
        }

        public ValueState(String value) {
            this(value, true, null);
        }

        public ValueState(String value, EnrichReasons enrichReason) {
            this(value, true, enrichReason);
        }

        public ValueState(String value, boolean checked) {
            this(value, checked, null);
        }

        public ValueState(String value, boolean checked, EnrichReasons enrichReason) {
            this.value = value;
            this.checked = checked;
            this.enrichReasons = EnumSet.noneOf(EnrichReasons.class);
            if (enrichReason != null) {
                addEnrichReason(enrichReason);
            }
        }

        public ValueState cloneMe() {
            ValueState result = new ValueState(value, checked);
            for (EnrichReasons reason : EnrichReasons.values()) {
                if (enrichReasons.contains(reason)) {
                    result.addEnrichReason(reason);
                }
            }
            result.sourceSeriesNumber = sourceSeriesNumber;
            result.sourceProductGroup = sourceProductGroup;
            return result;
        }

        public void addEnrichReason(EnrichReasons enrichReason) {
            enrichReasons = EnumUtils.plus(enrichReasons, enrichReason);
        }

        public void addEnrichReasons(EnumSet<EnrichReasons> enrichReasons) {
            for (EnrichReasons reason : EnrichReasons.values()) {
                if (enrichReasons.contains(reason)) {
                    addEnrichReason(reason);
                }
            }
        }

        public String getEnrichText() {
            if (!enrichReasons.isEmpty()) {
                List<String> list = new ArrayList<String>();
                for (EnrichReasons reason : EnrichReasons.values()) {
                    if (enrichReasons.contains(reason)) {
                        list.add(TranslationHandler.translate(reason.getDescription()));
                    }
                }
                return StrUtils.stringListToString(list, ",");
            }
            return "";
        }
    }


    @JsonProperty
    private Map<String, ValueState> topGridValues;
    @JsonProperty
    private Map<String, ValueState> bottomGridValues;

    public TwoGridValues(Set<String> topGridValues, Set<String> bottomGridValues) {
        this.topGridValues = new LinkedHashMap<>();
        if (topGridValues != null) {
            for (String value : topGridValues) {
                this.topGridValues.put(value, new ValueState(value));
            }
        }
        this.bottomGridValues = new LinkedHashMap<>();
        if (bottomGridValues != null) {
            for (String value : bottomGridValues) {
                this.bottomGridValues.put(value, new ValueState(value));
            }
        }
    }

    public TwoGridValues(Collection<ValueState> topGridValues, Collection<ValueState> bottomGridValues) {
        this.topGridValues = new LinkedHashMap<>();
        if (topGridValues != null) {
            for (ValueState valueState : topGridValues) {
                this.topGridValues.put(valueState.value, valueState);
            }
        }
        this.bottomGridValues = new LinkedHashMap<>();
        if (bottomGridValues != null) {
            for (ValueState valueState : bottomGridValues) {
                this.bottomGridValues.put(valueState.value, valueState);
            }
        }
    }

    public TwoGridValues(Map<String, ValueState> topGridValuesMap, Map<String, ValueState> bottomGridValuesMap) {
        topGridValues = topGridValuesMap;
        if (topGridValues == null) {
            topGridValues = new LinkedHashMap<>();
        }
        bottomGridValues = bottomGridValuesMap;
        if (bottomGridValues == null) {
            bottomGridValues = new LinkedHashMap<>();
        }
    }

    public TwoGridValues() {
        this.topGridValues = new LinkedHashMap<>();
        this.bottomGridValues = new LinkedHashMap<>();
    }

    public Collection<ValueState> getTopGridValues() {
        return topGridValues.values();
    }

    public Collection<ValueState> getBottomGridValues() {
        return bottomGridValues.values();
    }

    /**
     * Liefert alle ausgewählten Einträge aus dem oberen und unteren Grid zurück.
     *
     * @return
     */
    public Set<String> getAllCheckedValues() {
        if (topGridValues.isEmpty() && bottomGridValues.isEmpty()) {
            return new TreeSet<>(); // beide sind leer
        } else {
            Set<String> result = new TreeSet<>();
            for (ValueState valueState : topGridValues.values()) {
                if (valueState.checked) {
                    result.add(valueState.value);
                }
            }
            for (ValueState valueState : bottomGridValues.values()) {
                if (valueState.checked) {
                    result.add(valueState.value);
                }
            }
            return Collections.unmodifiableSet(result);
        }
    }

    /**
     * Liefert alle ausgewählten Einträge aus dem oberen bzw. unteren Grid zurück je nach Parameter <i>topGrid</i>.
     *
     * @param topGrid
     * @return
     */
    public Set<String> getCheckedValues(boolean topGrid) {
        Collection<ValueState> gridValues = topGrid ? topGridValues.values() : bottomGridValues.values();
        if (gridValues.isEmpty()) {
            return new TreeSet<>(); // leer
        } else {
            Set<String> result = new TreeSet<>();
            for (ValueState valueState : gridValues) {
                if (valueState.checked) {
                    result.add(valueState.value);
                }
            }
            return Collections.unmodifiableSet(result);
        }
    }

    /**
     * Liefert alle ausgewählten {@link ValueState}s aus dem oberen bzw. unteren Grid zurück je nach Parameter <i>topGrid</i>.
     *
     * @param topGrid
     * @return
     */
    public List<ValueState> getCheckedValueStates(boolean topGrid) {
        Collection<ValueState> gridValues = topGrid ? topGridValues.values() : bottomGridValues.values();
        if (gridValues.isEmpty()) {
            return new DwList<>(0); // leer
        } else {
            List<ValueState> result = new DwList<>(gridValues.size());
            for (ValueState valueState : gridValues) {
                if (valueState.checked) {
                    result.add(valueState);
                }
            }
            return Collections.unmodifiableList(result);
        }
    }

    public void addSingleValue(String value, boolean top) {
        addSingleValue(value, null, top);
    }

    public void addSingleValue(String value, EnrichReasons enrichReason, boolean top) {
        if (top) {
            topGridValues.put(value, new ValueState(value, enrichReason));
        } else {
            bottomGridValues.put(value, new ValueState(value, enrichReason));
        }
    }

    public void addSingleValue(String value, boolean checked, boolean top) {
        if (top) {
            topGridValues.put(value, new ValueState(value, checked));
        } else {
            bottomGridValues.put(value, new ValueState(value, checked));
        }
    }

    public void addSingleValue(ValueState valueState, boolean top) {
        if (top) {
            topGridValues.put(valueState.value, valueState.cloneMe());
        } else {
            bottomGridValues.put(valueState.value, valueState.cloneMe());
        }
    }

    private ValueState findValue(Map<String, ValueState> gridValues, String value) {
        return gridValues.get(value);
    }

    public ValueState getValueState(String value, boolean top) {
        if (top) {
            return findValue(topGridValues, value);
        } else {
            return findValue(bottomGridValues, value);
        }
    }

    public void modifySingleValue(String value, boolean checked, boolean top) {
        ValueState valueState;
        if (top) {
            valueState = findValue(topGridValues, value);
        } else {
            valueState = findValue(bottomGridValues, value);
        }
        if (valueState != null) {
            valueState.checked = checked;
        }
    }

    public void toggleSingleValue(String value, boolean top) {
        ValueState valueState;
        if (top) {
            valueState = findValue(topGridValues, value);
        } else {
            valueState = findValue(bottomGridValues, value);
        }
        if (valueState != null) {
            valueState.checked = !valueState.checked;
        }
    }

    public boolean removeSingleValue(String value, boolean top) {
        if (top) {
            return topGridValues.remove(value) != null;
        } else {
            return bottomGridValues.remove(value) != null;
        }
    }

    public boolean isEmpty() {
        return topGridValues.isEmpty() && bottomGridValues.isEmpty();
    }

    public boolean isEmptyWithoutEnriched() {
        if (!isEmpty()) {
            for (ValueState valueState : topGridValues.values()) {
                if (!valueState.enrichReasons.isEmpty()) {
                    return false;
                }
            }
            for (ValueState valueState : bottomGridValues.values()) {
                if (!valueState.enrichReasons.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Überprüft, ob das übergeben {@link TwoGridValues} Objekt die gleichen Werte hält wie dieses Objekt
     *
     * @param values
     * @return
     */
    public boolean hasSameContent(TwoGridValues values) {
        if ((topGridValues.size() == values.topGridValues.size()) && (bottomGridValues.size() == values.bottomGridValues.size())) {
            return getAllCheckedValues().equals(values.getAllCheckedValues());
        }
        return false;
    }

    /**
     * Überprüft, ob der übergebene Wert in diesem Objekt vorhanden ist
     *
     * @param value
     * @return
     */
    public boolean contains(String value) {
        if (findValue(topGridValues, value) != null) {
            return true;
        }
        if (findValue(bottomGridValues, value) != null) {
            return true;
        }
        return false;
    }

    public TwoGridValues cloneMe() {
        Map<String, ValueState> destTopGridValues = new LinkedHashMap<>();
        for (ValueState valueState : topGridValues.values()) {
            destTopGridValues.put(valueState.value, valueState.cloneMe());
        }
        Map<String, ValueState> destBottomGridValues = new LinkedHashMap<>();
        for (ValueState valueState : bottomGridValues.values()) {
            destBottomGridValues.put(valueState.value, valueState.cloneMe());
        }
        return new TwoGridValues(destTopGridValues, destBottomGridValues);
    }

    public boolean isEnriched() {
        if (isEmpty()) {
            return false; // beide sind leer
        } else {
            if (isEnriched(true)) {
                return true;
            }
            if (isEnriched(false)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEnriched(boolean topGrid) {
        Collection<ValueState> gridValues = topGrid ? topGridValues.values() : bottomGridValues.values();
        for (ValueState valueState : gridValues) {
            if (!valueState.enrichReasons.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fügt die übergebenen Werte zu dem jeweiligen Grid hinzu
     *
     * @param saasToAdd
     */
    public void addTwoGridValues(TwoGridValues saasToAdd) {
        for (ValueState topSaa : saasToAdd.getTopGridValues()) {
            addSingleValue(topSaa, true);
        }

        for (ValueState bottomSaa : saasToAdd.getBottomGridValues()) {
            addSingleValue(bottomSaa, false);
        }
    }
}
