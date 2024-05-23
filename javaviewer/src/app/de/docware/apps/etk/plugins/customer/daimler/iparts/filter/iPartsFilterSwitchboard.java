/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.filter;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.Enums;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.util.enums.EnumUtils;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Verwaltung der schaltbaren Filter
 * Modelklasse für iPartsFilterSwitchboardPanel
 */
public class iPartsFilterSwitchboard {

    private Map<FilterTypes, FilterItem> filterItemMap;
    private boolean mainSwitchActive;

    public iPartsFilterSwitchboard() {
        this.mainSwitchActive = false;
        filterItemMap = new LinkedHashMap<FilterTypes, FilterItem>();
        for (FilterTypes activatableFilter : FilterTypes.values()) {
            if (activatableFilter == FilterTypes.PSK_VARIANTS) {
                // PSK-Varianten-Filter darf man nur mit richtiger Eigenschaft sehen
                if (!iPartsRight.checkPSKInSession()) {
                    continue;
                }
            }
            filterItemMap.put(activatableFilter, new FilterItem(true));
        }
    }

    public iPartsFilterSwitchboard cloneMe() {
        iPartsFilterSwitchboard result = new iPartsFilterSwitchboard();
        for (Map.Entry<FilterTypes, FilterItem> entry : getFilterItemMap().entrySet()) {
            result.getFilterItemMap().put(entry.getKey(), entry.getValue().cloneMe());
        }
        result.mainSwitchActive = this.mainSwitchActive;
        return result;
    }

    public boolean isMainSwitchActive() {
        return mainSwitchActive;
    }

    public void setMainSwitchActive(boolean mainSwitchActive) {
        this.mainSwitchActive = mainSwitchActive;
        // Wegfallsachnummern-Filter immer mit dem Hauptschalter (de-)aktivieren
        filterItemMap.get(FilterTypes.OMITTED_PARTS).setActivated(mainSwitchActive);

        // Entfallpositions-Filter immer mit dem Hauptschalter (de-)aktivieren
        filterItemMap.get(FilterTypes.OMITTED_PART_LIST_ENTRIES).setActivated(mainSwitchActive);

        // Filter für zusätzliche Stücklisteneinträge zu einer DIALOG-Position, die nur im Baumuster-Filter
        // berücksichtigt werden sollen, immer mit dem Hauptschalter (de-)aktivieren
        filterItemMap.get(FilterTypes.ONLY_MODEL_FILTER).setActivated(mainSwitchActive);

        // Leitungssatz-Baukasten-Filter immer mit dem Hauptschalter (de-)aktivieren
        filterItemMap.get(FilterTypes.WIRE_HARNESS).setActivated(mainSwitchActive);
    }

    public boolean isModified(iPartsFilterSwitchboard activatableFilterContainer) {
        if (this.mainSwitchActive != activatableFilterContainer.mainSwitchActive) {
            return true;
        }
        if (this.filterItemMap.size() != activatableFilterContainer.getFilterItemMap().size()) {
            return true;
        }
        for (Map.Entry<FilterTypes, FilterItem> entry : filterItemMap.entrySet()) {
            FilterItem filterElem = activatableFilterContainer.getFilterItemMap().get(entry.getKey());
            if (filterElem == null) {
                return true;
            }
            if (entry.getValue().isModified(filterElem)) {
                return true;
            }
        }
        return false;
    }

    public void reset() {
        for (FilterItem entry : filterItemMap.values()) {
            entry.reset();
        }
    }

    public Map<FilterTypes, FilterItem> getFilterItemMap() {
        return filterItemMap;
    }

    public boolean isFilterActivated(FilterTypes activatableFilter) {
        if (mainSwitchActive) {
            return isOnlyFilterActivated(activatableFilter);
        }
        return false;
    }

    /**
     * nur für den Filter-Dialog!
     * liefert, unabhängig ob der Filter aktiv ist, den Zustand des angesprochenen Filters
     *
     * @param activatableFilter
     * @return
     */
    public boolean isOnlyFilterActivated(FilterTypes activatableFilter) {
        FilterItem activatableFilterElem = filterItemMap.get(activatableFilter);
        if (activatableFilterElem != null) {
            return activatableFilterElem.isActivated();
        }
        return false;
    }

    public boolean isFilterCanBeActivated(FilterTypes activatableFilter) {
        FilterItem activatableFilterElem = filterItemMap.get(activatableFilter);
        if (activatableFilterElem != null) {
            return activatableFilterElem.isCanBeActivated();
        }
        return false;
    }

    public void setFilterActivated(FilterTypes activatableFilter, boolean activated) {
        FilterItem activatableFilterElem = filterItemMap.get(activatableFilter);
        if (activatableFilterElem != null) {
            activatableFilterElem.setActivated(activated);
        }
    }

    /**
     * Nur für den Filter-Dialog!
     * Liefert, unabhängig ob der Filter aktiv ist, die Liste der aktivierten (sichtbaren) Filter
     *
     * @param onlyVisible Flag, ob nur die sichtbaren Filter berücksichtigt werden sollen
     * @return
     */
    public EnumSet<FilterTypes> getAllActivated(boolean onlyVisible) {
        EnumSet<FilterTypes> result = EnumSet.noneOf(FilterTypes.class);
        for (Map.Entry<FilterTypes, FilterItem> filterEntry : filterItemMap.entrySet()) {
            FilterTypes filter = filterEntry.getKey();
            if (filterEntry.getValue().isActivated() && (!onlyVisible || filter.isVisible())) {
                result = EnumUtils.plus(result, filter);
            }
        }
        return result;
    }

    /**
     * Liefert zurück, ob irgendein Filter aktiv ist (dieser kann auch unsichtbar sein).
     *
     * @return
     */
    public boolean isAnyActivated() {
        if (mainSwitchActive) {
            for (Map.Entry<FilterTypes, FilterItem> filterEntry : filterItemMap.entrySet()) {
                if (filterEntry.getValue().isActivated()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Alle Filter, die ein- oder ausgeschaltet werden können
     */
    public enum FilterTypes {
        MODEL("MDL", true),
        END_NUMBER("ENR", true),
        DATACARD_SA("DKSA", true),
        SA_STRICH("SASTR", true),
        STEERING("ST", true),
        GEARBOX("GB", true),
        SPRING("SPR", true),
        SPECIAL_ZB_NUMBER("SZBN", false),
        DATACARD_CODE("DCC", true),
        EXTENDED_CODE("XTCOD", true),
        EXTENDED_COLOR("XTCOL", true),
        PSK_VARIANTS("VARS", true),
        COUNTRY_VALIDITY_FILTER("CVF", true),
        SPECIFICATION_FILTER("SF", true),
        AGG_MODELS("AGGM", true),
        REMOVE_DUPLICATES("RDUP", true),
        OMITTED_PARTS("OMP", false),
        OMITTED_PART_LIST_ENTRIES("OMPLE", false),
        ONLY_MODEL_FILTER("OMF", false),
        AS_PRODUCT_CLASS("ASPC", false),
        MODULE_HIDDEN("MHID", false),
        HMMSM_HIDDEN("HMHID", false),
        CONFIGURATION("CNFG", false),
        PSK("PSK", false),
        WIRE_HARNESS("WH", false),
        TU_VALIDITIES_FILTER("TUVF", false);

        // Für das Produkt abschaltbare Filter über die [Enum.xls] gelöst. Es sind alle definiert, nur der EnumName ist bei den nicht abschaltbaren ungültig.
//        public static EnumSet<FilterTypes> disengageableProductFilters = EnumSet.of(EXTENDED_COLOR, EXTENDED_CODE, GEARBOX);

        public static EnumSet<FilterTypes> all() {
            return EnumSet.allOf(FilterTypes.class);
        }

        public static EnumSet<FilterTypes> allVisible() {
            EnumSet<FilterTypes> result = all();

            for (FilterTypes currentFilter : FilterTypes.values()) {
                if (!currentFilter.isVisible()) {
                    EnumUtils.minus(result, currentFilter);
                }
            }
            return result;
        }

        private static final String ENUM_KEY = "ActivatableFilter";

        private boolean isVisible;
        private String dbValue;

        FilterTypes(String dbValue, boolean isVisible) {
            this.dbValue = dbValue;
            this.isVisible = isVisible;
        }

        public String getDescription(EtkProject project) {
            return Enums.getDescriptionForEnumToken(ENUM_KEY, name(), project);
        }

        // Nötig??
        //    public void setDescription(String description) {
        //        this.description = description;
        //    }

        public boolean isVisible() {
            return isVisible;
        }

        public String getDBValue() {
            return dbValue;
        }

        public static FilterTypes getFromDBValue(String dbValue) {
            for (FilterTypes filterType : values()) {
                if (filterType.getDBValue().equals(dbValue)) {
                    return filterType;
                }
            }
            return null;
        }
    }


    public static class FilterItem {

        private boolean isActivated;
        private boolean canBeActivated;

        public FilterItem(boolean canBeActivated) {
            this.isActivated = false;
            this.canBeActivated = canBeActivated;
        }

        public FilterItem cloneMe() {
            FilterItem result = new FilterItem(canBeActivated);
            result.setActivated(isActivated);
            return result;
        }

        public void reset() {
            isActivated = false;
        }

        public boolean isModified(FilterItem filterElem) {
            return isActivated != filterElem.isActivated();
        }

        public boolean isActivated() {
            return isActivated;
        }

        public void setActivated(boolean activated) {
            if (canBeActivated) {
                isActivated = activated;
            }
        }

        public void setCanBeActivated(boolean canBeActivated) {
            this.canBeActivated = canBeActivated;
        }

        public boolean isCanBeActivated() {
            return canBeActivated;
        }
    }


}
