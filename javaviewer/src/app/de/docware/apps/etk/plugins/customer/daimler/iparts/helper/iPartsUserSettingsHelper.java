/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserSettingsConst;
import de.docware.apps.etk.viewer.usersettings.EtkUserSettings;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.util.StrUtils;

import java.util.*;

/**
 * Helper-Klasse für Zugriffe auf die Bool-Werte {@link EtkUserSettings}
 */
public class iPartsUserSettingsHelper {

    public static boolean getBoolValue(EtkProject project, String key) {
        return project.getUserSettings().getBoolValues(key);
    }

    public static String getStringValue(EtkProject project, String key) {
        return project.getUserSettings().getStrValues(key);
    }

    public static Integer getIntValue(EtkProject project, String key) {
        return project.getUserSettings().getIntValues(key);
    }

    public static void setIntValue(EtkProject project, String key, int value) {
        project.getUserSettings().setIntValues(key, value);
    }

    public static void setStringValue(EtkProject project, String key, String value) {
        project.getUserSettings().setStrValues(key, value);
    }

    public static void setBoolValue(EtkProject project, String key, boolean value) {
        project.getUserSettings().setBoolValues(key, value);
    }

    public static boolean isHideEmptyTUs(EtkProject project) {
        return getBoolValue(project, iPartsUserSettingsConst.REL_HIDE_EMPTY_TUS);
    }

    public static boolean isShowHiddenHmMSmNodes(EtkProject project) {
        return getBoolValue(project, iPartsUserSettingsConst.REL_SHOW_HIDDEN_HMMSM_NODES);
    }

    public static boolean isShowNonASRelEntries(EtkProject project) {
        return !isHideNonASRelEntries(project);
    }

    public static boolean isHideNonASRelEntries(EtkProject project) {
        return getBoolValue(project, iPartsUserSettingsConst.REL_DIALOG_HIDE_NON_AS_REL);
    }

    public static boolean isShowOnlyLastApprovedEntries(EtkProject project) {
        return getBoolValue(project, iPartsUserSettingsConst.REL_DIALOG_SHOW_LAST_APPROVED);
    }

    public static boolean isSingleEdit(EtkProject project) {
        return getBoolValue(project, iPartsUserSettingsConst.REL_SINGLE_EDIT_VIEW);
    }

    public static boolean isMatrixEdit(EtkProject project) {
        if (Constants.DEVELOPMENT) {
            return getBoolValue(project, iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW);
        }
        return iPartsUserSettingsConst.ULTRA_EDIT_VIEW_DEFAULT;
    }

    public static boolean isMatrixEditEmptyCols(EtkProject project) {
        if (Constants.DEVELOPMENT) {
            return getBoolValue(project, iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW_COLS);
        }
        return iPartsUserSettingsConst.ULTRA_EDIT_VIEW_COLS_DEFAULT;
    }

    public static boolean isMatrixEditEmptyRows(EtkProject project) {
        if (Constants.DEVELOPMENT) {
            return getBoolValue(project, iPartsUserSettingsConst.REL_ULTRA_EDIT_VIEW_ROWS);
        }
        return iPartsUserSettingsConst.ULTRA_EDIT_VIEW_ROWS_DEFAULT;
    }

    public static String getDIALOGPartListTextKinds(EtkProject project) {
        return getStringValue(project, iPartsUserSettingsConst.REL_DIALOG_PARTLIST_TEXT_KINDS);
    }

    public static String getEDSBCSPartListTextKinds(EtkProject project) {
        return getStringValue(project, iPartsUserSettingsConst.REL_EDS_BCS_PARTLIST_TEXT_KINDS);
    }

    public static String getCTTPartListTextKinds(EtkProject project) {
        return getStringValue(project, iPartsUserSettingsConst.REL_CTT_PARTLIST_TEXT_KINDS);
    }

    public static void setDIALOGPartListTextKinds(EtkProject project, String textTypesStr) {
        setStringValue(project, iPartsUserSettingsConst.REL_DIALOG_PARTLIST_TEXT_KINDS, textTypesStr);
    }

    public static void setEDSBCSPartListTextKinds(EtkProject project, String textTypesStr) {
        setStringValue(project, iPartsUserSettingsConst.REL_EDS_BCS_PARTLIST_TEXT_KINDS, textTypesStr);
    }

    public static void setCTTPartListTextKinds(EtkProject project, String textTypesStr) {
        setStringValue(project, iPartsUserSettingsConst.REL_CTT_PARTLIST_TEXT_KINDS, textTypesStr);
    }

    public static String getConstPartListFilterValue(EtkProject project) {
        return getStringValue(project, iPartsUserSettingsConst.REL_DIALOG_POS_FILTER_VALUE);
    }

    public static void setConstPartListFilterValue(EtkProject project, String filterValue) {
        setStringValue(project, iPartsUserSettingsConst.REL_DIALOG_POS_FILTER_VALUE, filterValue);
    }

    public static int getEdsSaaStructureLevel(EtkProject project) {
        return getIntValue(project, iPartsUserSettingsConst.REL_EDS_SAA_STRUCTURE_LEVEL);
    }

    public static void setEdsSaaStructureLevel(EtkProject project, int structureLevel) {
        setIntValue(project, iPartsUserSettingsConst.REL_EDS_SAA_STRUCTURE_LEVEL, structureLevel);
    }

    public static int getCTTSaaStructureLevel(EtkProject project) {
        return getIntValue(project, iPartsUserSettingsConst.REL_CTT_SAA_STRUCTURE_LEVEL);
    }

    public static void setCTTSaaStructureLevel(EtkProject project, int structureLevel) {
        setIntValue(project, iPartsUserSettingsConst.REL_CTT_SAA_STRUCTURE_LEVEL, structureLevel);
    }

    public static int getMBSStructureLevel(EtkProject project) {
        return getIntValue(project, iPartsUserSettingsConst.REL_MBS_STRUCTURE_LEVEL);
    }

    public static void setMBSStructureLevel(EtkProject project, int structureLevel) {
        setIntValue(project, iPartsUserSettingsConst.REL_MBS_STRUCTURE_LEVEL, structureLevel);
    }

    public static void setEdsMarketEtkz(EtkProject project, String marketEtkz) {
        setStringValue(project, iPartsUserSettingsConst.REL_EDS_MARKET_ETKZ, marketEtkz);
    }

    public static String getEdsMarketEtkz(EtkProject project) {
        return getStringValue(project, iPartsUserSettingsConst.REL_EDS_MARKET_ETKZ);
    }

    public static void setCTTMarketEtkz(EtkProject project, String marketEtkz) {
        setStringValue(project, iPartsUserSettingsConst.REL_CTT_MARKET_ETKZ, marketEtkz);
    }

    public static String getCTTMarketEtkz(EtkProject project) {
        return getStringValue(project, iPartsUserSettingsConst.REL_CTT_MARKET_ETKZ);
    }

    public static void clearConstPartListFilterValue(EtkProject project) {
        setConstPartListFilterValue(project, "");
    }

    public static boolean isConstPartListFilterActive(EtkProject project) {
        return !getConstPartListFilterValue(project).isEmpty();
    }

    public static boolean isThumbnailViewActive(EtkProject project) {
        return getBoolValue(project, iPartsUserSettingsConst.REL_THUMBNAIL_VIEW_ACTIVE);
    }

    public static void setThumbnailViewActive(EtkProject project, boolean isThumbnailViewActive) {
        setBoolValue(project, iPartsUserSettingsConst.REL_THUMBNAIL_VIEW_ACTIVE, isThumbnailViewActive);
    }

    /**
     * Setzt für die übergebene {@link AssemblyId} den Wert, ob die Anzeige der SAA Konstruktionstückliste auf den Serienumfang
     * eingegrenzt werden soll
     *
     * @param project
     * @param assemblyId
     * @param seriesViewActive
     */
    public static void setSeriesViewActive(EtkProject project, AssemblyId assemblyId, boolean seriesViewActive, boolean isEDS) {
        Set<String> singleAssemblies = isEDS ? getEDSAssemblyWithSeriesViewActive(project) : getCTTAssemblyWithSeriesViewActive(project);
        String assemblyValue = assemblyId.toDBString();
        if (seriesViewActive) {
            if (singleAssemblies.isEmpty()) {
                // 1. Fall: Aktuelle Stückliste soll nur Serie anzeigen und aktuell sind keine anderen Stücklisten gesetzt
                // -> nur die aktuelle Stückliste hinzufügen
                singleAssemblies.add(assemblyValue);
            } else {
                // 2. Fall: Aktuelle Stückliste soll nur Serie anzeigen und es gibt schon Stücklisten bei denen die Sicht aktiv ist
                if (!singleAssemblies.contains(assemblyValue)) {
                    // Stückliste ist noch nicht vorhanden -> hinzufügen
                    singleAssemblies.add(assemblyValue);
                } else {
                    // Stückliste ist schon drin (eher unwahrscheinlich)
                    return;
                }
            }
        } else {
            if (singleAssemblies.isEmpty()) {
                // 3. Fall: Aktuelle Stückliste soll nicht nur Serie anzeigen und aktuell sind keine anderen Stücklisten gesetzt
                // -> nichts machen
                return;
            } else {
                // 4. Fall: Aktuelle Stückliste soll nicht nur Serie anzeigen und aktuell sind weitere Stücklisten gesetzt
                // -> Übergebene Stückliste aus den aktiven entfernen
                singleAssemblies.remove(assemblyValue);
            }

        }
        if (isEDS) {
            setEDSAssemblyWithSeriesViewActive(project, singleAssemblies);
        } else {
            setCTTAssemblyWithSeriesViewActive(project, singleAssemblies);
        }
    }

    private static Set<String> getEDSAssemblyWithSeriesViewActive(EtkProject project) {
        String currentActiveAssemblies = getStringValue(project, iPartsUserSettingsConst.REL_EDS_SAA_SERIES_VIEW_ACTIVE);
        return new TreeSet<>(StrUtils.toStringList(currentActiveAssemblies, iPartsUserSettingsConst.SERIES_VIEW_ACTIVE_DELIMITER, false));
    }

    private static Set<String> getCTTAssemblyWithSeriesViewActive(EtkProject project) {
        String currentActiveAssemblies = getStringValue(project, iPartsUserSettingsConst.REL_CTT_SAA_SERIES_VIEW_ACTIVE);
        return new TreeSet<>(StrUtils.toStringList(currentActiveAssemblies, iPartsUserSettingsConst.SERIES_VIEW_ACTIVE_DELIMITER, false));
    }

    private static void setEDSAssemblyWithSeriesViewActive(EtkProject project, Set<String> singleAssemblies) {
        setStringValue(project, iPartsUserSettingsConst.REL_EDS_SAA_SERIES_VIEW_ACTIVE, StrUtils.stringListToString(singleAssemblies, iPartsUserSettingsConst.SERIES_VIEW_ACTIVE_DELIMITER));
    }

    private static void setCTTAssemblyWithSeriesViewActive(EtkProject project, Set<String> singleAssemblies) {
        setStringValue(project, iPartsUserSettingsConst.REL_CTT_SAA_SERIES_VIEW_ACTIVE, StrUtils.stringListToString(singleAssemblies, iPartsUserSettingsConst.SERIES_VIEW_ACTIVE_DELIMITER));
    }

    public static boolean isEDSSeriesViewActiveForAssembly(EtkProject project, AssemblyId assemblyId) {
        return getEDSAssemblyWithSeriesViewActive(project).contains(assemblyId.toDBString());
    }

    public static boolean isCTTSeriesViewActiveForAssembly(EtkProject project, AssemblyId assemblyId) {
        return getCTTAssemblyWithSeriesViewActive(project).contains(assemblyId.toDBString());
    }

    private static void addModelListIfValid(EtkProject project, Map<String, Set<String>> result, String key, String modelTypePrefix) {
        String value = getStringValue(project, key);
        if (StrUtils.isValid(value)) {
            List<String> modelList = StrUtils.toStringList(value, iPartsUserSettingsConst.EDS_CONST_MODELS_DELIMITER, false);
            Set<String> modelSet = new HashSet<>(modelList);
            result.put(modelTypePrefix, modelSet);
        }
    }

    private static Map<String, Set<String>> getSelectedConstModels(EtkProject project, String carKey, String aggKey) {
        Map<String, Set<String>> result = new HashMap<>();
        addModelListIfValid(project, result, carKey, iPartsConst.MODEL_NUMBER_PREFIX_CAR);
        addModelListIfValid(project, result, aggKey, iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE);
        return result;
    }

    private static String getSelectedConstModels(Map<String, Set<String>> filterValues, String modelTypePrefix) {
        Set<String> modelSet = filterValues.get(modelTypePrefix);
        if ((modelSet != null) && !modelSet.isEmpty()) {
            return StrUtils.stringListToString(modelSet, iPartsUserSettingsConst.EDS_CONST_MODELS_DELIMITER);
        }
        return "";
    }

    private static void setSelectedConstModels(EtkProject project, Map<String, Set<String>> filterValues, String carKey, String aggKey) {
        String carValue = "";
        String aggregateValue = "";
        if ((filterValues != null) && !filterValues.isEmpty()) {
            carValue = getSelectedConstModels(filterValues, iPartsConst.MODEL_NUMBER_PREFIX_CAR);
            aggregateValue = getSelectedConstModels(filterValues, iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE);
        }
        if (StrUtils.isValid(carKey)) {
            setStringValue(project, carKey, carValue);
        }
        if (StrUtils.isValid(aggKey)) {
            setStringValue(project, aggKey, aggregateValue);
        }
    }

    public static Map<String, Set<String>> getSelectedEDSConstModels(EtkProject project) {
        return getSelectedConstModels(project, iPartsUserSettingsConst.REL_EDS_CONST_MODELS_CAR_VALUE,
                                      iPartsUserSettingsConst.REL_EDS_CONST_MODELS_AGGREGATE_VALUE);
    }

    public static void setSelectedEDSConstModels(EtkProject project, Map<String, Set<String>> filterValues) {
        setSelectedConstModels(project, filterValues, iPartsUserSettingsConst.REL_EDS_CONST_MODELS_CAR_VALUE,
                               iPartsUserSettingsConst.REL_EDS_CONST_MODELS_AGGREGATE_VALUE);
    }

    public static Map<String, Set<String>> getSelectedMBSConstModels(EtkProject project) {
        return getSelectedConstModels(project, iPartsUserSettingsConst.REL_MBS_CONST_MODELS_CAR_VALUE,
                                      iPartsUserSettingsConst.REL_MBS_CONST_MODELS_AGGREGATE_VALUE);
    }

    public static void setSelectedMBSConstModels(EtkProject project, Map<String, Set<String>> filterValues) {
        setSelectedConstModels(project, filterValues, iPartsUserSettingsConst.REL_MBS_CONST_MODELS_CAR_VALUE,
                               iPartsUserSettingsConst.REL_MBS_CONST_MODELS_AGGREGATE_VALUE);
    }

    public static Map<String, Set<String>> getSelectedCTTConstModels(EtkProject project) {
        return getSelectedConstModels(project, iPartsUserSettingsConst.REL_CTT_CONST_MODELS_CAR_VALUE, iPartsUserSettingsConst.REL_CTT_CONST_MODELS_AGGREGATE_VALUE);
    }

    public static void setSelectedCTTConstModels(EtkProject project, Map<String, Set<String>> filterValues) {
        setSelectedConstModels(project, filterValues, iPartsUserSettingsConst.REL_CTT_CONST_MODELS_CAR_VALUE, iPartsUserSettingsConst.REL_CTT_CONST_MODELS_AGGREGATE_VALUE);
    }

    public static void setEDSSeriesViewActiveEntryLevel(EtkProject project, int level) {
        setIntValue(project, iPartsUserSettingsConst.REL_EDS_SAA_SERIES_VIEW_ACTIVE_LEVEL, level);
    }

    public static int getEDSSeriesViewActiveEntryLevel(EtkProject project) {
        return getIntValue(project, iPartsUserSettingsConst.REL_EDS_SAA_SERIES_VIEW_ACTIVE_LEVEL);
    }

    public static void setCTTSeriesViewActiveEntryLevel(EtkProject project, int level) {
        setIntValue(project, iPartsUserSettingsConst.REL_CTT_SAA_SERIES_VIEW_ACTIVE_LEVEL, level);
    }

    public static int getCTTSeriesViewActiveEntryLevel(EtkProject project) {
        return getIntValue(project, iPartsUserSettingsConst.REL_CTT_SAA_SERIES_VIEW_ACTIVE_LEVEL);
    }
}
