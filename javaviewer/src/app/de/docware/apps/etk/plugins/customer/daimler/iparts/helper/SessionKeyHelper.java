/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

/**
 * Hilfsroutinen zur Verwaltung der EDS/MBS-Konstruktions-Baumuster in den Session-Attributen (iPartsPlugin)
 */
public class SessionKeyHelper implements iPartsConst {

    private static final String EMPTY_MBS_CONSTRUCTION_DATE = "<empty>";
    private static final String DICT_PRESELECT_INDEX = "iparts_dict_preselect_index";

    private static boolean isSessionValid() {
        Session session = Session.get();
        return session != null;
    }

    private static Map<String, Set<String>> getSelectedModelMap(String sessionKey) {
        Session session = Session.get();
        if (session == null) {
            return null;
        }
        return (Map<String, Set<String>>)Session.get().getAttribute(sessionKey);
    }

    private static boolean setSelectedModelMap(String sessionKey, Map<String, Set<String>> currenModelValues) {
        Session session = Session.get();
        if (session == null) {
            return false;
        }
        session.setAttribute(sessionKey, currenModelValues);
        return true;
    }

    public static Map<String, Set<String>> getSelectedEDSModelMap() {
        return getSelectedModelMap(iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL);
    }

    public static boolean setSelectedEDSModelMap(Map<String, Set<String>> currentModelValues) {
        return setSelectedModelMap(iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL, currentModelValues);
    }

    /**
     * Liefert die eingestellten Baumuster mit zusätzlicher Prüfung, ob Baumuster in den Benutzereinstellungen hinterlegt wurden
     *
     * @param project
     * @return
     */
    public static Map<String, Set<String>> getSelectedMBSModelMapWithUserSettingsCheck(EtkProject project) {
        Map<String, Set<String>> models = getSelectedMBSModelMap();
        if ((project != null) && (models == null) && Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            models = iPartsUserSettingsHelper.getSelectedMBSConstModels(project);
            if (models != null) {
                setSelectedMBSModelMap(models);
            }
        }
        return models;
    }

    /**
     * Liefert die eingestellten Baumuster mit zusätzlicher Prüfung, ob Baumuster in den Benutzereinstellungen hinterlegt wurden
     *
     * @param project
     * @return
     */
    public static Map<String, Set<String>> getSelectedCTTModelMapWithUserSettingsCheck(EtkProject project) {
        Map<String, Set<String>> models = getSelectedCTTModelMap();
        if ((project != null) && (models == null) && Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
            models = iPartsUserSettingsHelper.getSelectedCTTConstModels(project);
            if (models != null) {
                setSelectedCTTModelMap(models);
            }
        }
        return models;
    }

    public static Map<String, Set<String>> getSelectedMBSModelMap() {
        return getSelectedModelMap(iPartsPlugin.SESSION_KEY_SELECT_MBS_CONST_MODEL);
    }

    public static boolean setSelectedMBSModelMap(Map<String, Set<String>> currentModelValues) {
        return setSelectedModelMap(iPartsPlugin.SESSION_KEY_SELECT_MBS_CONST_MODEL, currentModelValues);
    }

    public static Map<String, Set<String>> getSelectedCTTModelMap() {
        return getSelectedModelMap(iPartsPlugin.SESSION_KEY_SELECT_CTT_CONST_MODEL);
    }

    public static boolean setSelectedCTTModelMap(Map<String, Set<String>> currentModelValues) {
        return setSelectedModelMap(iPartsPlugin.SESSION_KEY_SELECT_CTT_CONST_MODEL, currentModelValues);
    }

    /**
     * Hilfsmethode, die angibt, ob ein Baumuster für die EDS/BCS Konstruktion ausgewählt wurde
     *
     * @param sessionKey
     * @return
     */
    private static boolean isConstructionModelSelected(String sessionKey) {
        Map<String, Set<String>> selectedModelsMap = getSelectedModelMap(sessionKey);
        if ((selectedModelsMap != null) && !selectedModelsMap.isEmpty()) {
            for (Set<String> models : selectedModelsMap.values()) {
                if ((models != null) && !models.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Hilfsmethode, die angibt, ob ein Baumuster für die EDS/BCS Konstruktion ausgewählt wurde
     *
     * @return
     */
    public static boolean isConstructionModelForEDSBCSSelected() {
        return isConstructionModelSelected(iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL);
//        Map<String, Set<String>> selectedModelsMap = getSelectedEDSModelMap();
//        if ((selectedModelsMap != null) && !selectedModelsMap.isEmpty()) {
//            for (Set<String> models : selectedModelsMap.values()) {
//                if (!models.isEmpty()) {
//                    return true;
//                }
//            }
//        }
//        return false;
    }

    /**
     * Hilfsmethode, die angibt, ob ein Baumuster für die MBS Konstruktion ausgewählt wurde
     *
     * @return
     */
    public static boolean isConstructionModelForMBSSelected() {
        return isConstructionModelSelected(iPartsPlugin.SESSION_KEY_SELECT_MBS_CONST_MODEL);
    }

    /**
     * Hilfsmethode, die angibt, ob ein Baumuster für die CTT Konstruktion ausgewählt wurde
     *
     * @return
     */
    public static boolean isConstructionModelForCTTSelected() {
        return isConstructionModelSelected(iPartsPlugin.SESSION_KEY_SELECT_CTT_CONST_MODEL);
    }

    /**
     * Liefert, falls definiert, die Baumuster-Nummern-Liste in Abhängigkeit von isAggregate
     *
     * @param isAggregate
     * @param sessionKey
     * @return
     */
    private static Set<String> getSelectedModelSet(boolean isAggregate, String sessionKey) {
        Map<String, Set<String>> currentModelValues = getSelectedModelMap(sessionKey);
        if (currentModelValues != null) {
            return currentModelValues.get(getPrefixForModelId(isAggregate));
        }
        return null;
    }

    public static Set<String> getEdsSelectedModelSet(boolean isAggregate) {
        return getSelectedModelSet(isAggregate, iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL);
    }

    public static Set<String> getMbsSelectedModelSet(boolean isAggregate) {
        return getSelectedModelSet(isAggregate, iPartsPlugin.SESSION_KEY_SELECT_MBS_CONST_MODEL);
    }

    public static Set<String> getCTTSelectedModelSet(boolean isAggregate) {
        return getSelectedModelSet(isAggregate, iPartsPlugin.SESSION_KEY_SELECT_CTT_CONST_MODEL);
    }

    public static String getPrefixForModelId(boolean isAggregate) {
        return isAggregate ? MODEL_NUMBER_PREFIX_AGGREGATE : MODEL_NUMBER_PREFIX_CAR;
    }


    /**
     * Fügt das übergebene Baumuster zum Filter für die Konstruktions-Baumuster hinzu.
     *
     * @param modelNumber
     * @param sessionKey
     * @return {@code true} falls dieses Baumuster nicht bereits im Filter für die Konstruktions-Baumuster enthalten war
     */
    private static boolean addConstructionModelToFilter(String modelNumber, String sessionKey) {
        if (StrUtils.isValid(modelNumber) && isSessionValid()) {
            Map<String, Set<String>> currentModelValues = getSelectedModelMap(sessionKey);
            if (currentModelValues == null) {
                currentModelValues = new HashMap<>();
            }

            iPartsModelId modelId = new iPartsModelId(modelNumber);
            String prefix = getPrefixForModelId(modelId.isAggregateModel());
            Set<String> modelNumbersSet = currentModelValues.get(prefix);
            if (modelNumbersSet == null) {
                modelNumbersSet = new HashSet<>();
                currentModelValues.put(prefix, modelNumbersSet);
            }
            if (modelNumbersSet.add(modelId.getModelNumber())) {
                return setSelectedModelMap(sessionKey, currentModelValues);
            }
        }
        return false;
    }

    /**
     * Fügt das übergebene Baumuster zum Filter für die EDS-Konstruktions-Baumuster hinzu.
     *
     * @param modelNumber
     * @return {@code true} falls dieses Baumuster nicht bereits im Filter für die Konstruktions-Baumuster enthalten war
     */
    public static boolean addEDSConstructionModelToFilter(String modelNumber) {
        return addConstructionModelToFilter(modelNumber, iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL);
//        if (StrUtils.isValid(modelNumber) && isSessionValid()) {
//            Map<String, Set<String>> currentModelValues = getSelectedEDSModelMap();
//            if (currentModelValues == null) {
//                currentModelValues = new HashMap<>();
//            }
//
//            iPartsModelId modelId = new iPartsModelId(modelNumber);
//            String key = modelId.isAggregateModel() ? iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE
//                                                    : iPartsConst.MODEL_NUMBER_PREFIX_CAR;
//            Set<String> modelNumbersSet = currentModelValues.get(key);
//            if (modelNumbersSet == null) {
//                modelNumbersSet = new HashSet<>();
//                currentModelValues.put(key, modelNumbersSet);
//            }
//            if (modelNumbersSet.add(modelId.getModelNumber())) {
//                return setSelectedEDSModelMap(currentModelValues);
//            }
//        }
//        return false;
    }

    /**
     * Fügt das übergebene Baumuster zum Filter für die MBS-Konstruktions-Baumuster hinzu.
     *
     * @param modelNumber
     * @return {@code true} falls dieses Baumuster nicht bereits im Filter für die Konstruktions-Baumuster enthalten war
     */
    public static boolean addMBSConstructionModelToFilter(String modelNumber) {
        return addConstructionModelToFilter(modelNumber, iPartsPlugin.SESSION_KEY_SELECT_MBS_CONST_MODEL);
    }

    /**
     * Fügt das übergebene Baumuster zum Filter für die CTT-Konstruktions-Baumuster hinzu.
     *
     * @param modelNumber
     * @return {@code true} falls dieses Baumuster nicht bereits im Filter für die Konstruktions-Baumuster enthalten war
     */
    public static boolean addCTTConstructionModelToFilter(String modelNumber) {
        return addConstructionModelToFilter(modelNumber, iPartsPlugin.SESSION_KEY_SELECT_CTT_CONST_MODEL);
    }

    /**
     * Abhängig vom Typ (Aggregat oder Fahrzeug) Baumuster für den Filter setzen
     *
     * @param modelNoSet
     * @param isAggregate
     * @param sessionKey
     * @return
     */
    private static boolean setConstructionModelSetToFilter(Set<String> modelNoSet, boolean isAggregate, String sessionKey) {
        if (isSessionValid()) {
            Map<String, Set<String>> currentModelValues = getSelectedModelMap(sessionKey);
            if (currentModelValues == null) {
                currentModelValues = new HashMap<>();
            }
            String prefix = getPrefixForModelId(isAggregate);

            // Ausgewählte Liste ist leer -> lösche alle bisherigen Baumuster zu dem Prefix (Aggregate oder Fahrzeug)
            currentModelValues.put(prefix, modelNoSet);
            return setSelectedModelMap(sessionKey, currentModelValues);
        }
        return false;
    }

    /**
     * Abhängig vom Typ (Aggregat oder Fahrzeug) EDS-Baumuster für den Filter setzen
     *
     * @param modelNoSet
     * @param isAggregate
     * @return
     */
    public static boolean setEdsConstructionModelSetToFilter(Set<String> modelNoSet, boolean isAggregate) {
        return setConstructionModelSetToFilter(modelNoSet, isAggregate, iPartsPlugin.SESSION_KEY_SELECT_EDS_CONST_MODEL);
    }

    /**
     * Abhängig vom Typ (Aggregat oder Fahrzeug) MBS-Baumuster für den Filter setzen
     *
     * @param modelNoSet
     * @param isAggregate
     * @return
     */
    public static boolean setMbsConstructionModelSetToFilter(Set<String> modelNoSet, boolean isAggregate) {
        return setConstructionModelSetToFilter(modelNoSet, isAggregate, iPartsPlugin.SESSION_KEY_SELECT_MBS_CONST_MODEL);
    }

    /**
     * Abhängig vom Typ (Aggregat oder Fahrzeug) CTT-Baumuster für den Filter setzen
     *
     * @param modelNoSet
     * @param isAggregate
     * @return
     */
    public static boolean setCttConstructionModelSetToFilter(Set<String> modelNoSet, boolean isAggregate) {
        return setConstructionModelSetToFilter(modelNoSet, isAggregate, iPartsPlugin.SESSION_KEY_SELECT_CTT_CONST_MODEL);
    }

    /**
     * Überprüft, ob das übergebene Baumuster vom Benutzer für die Konstruktion ausgewählt wurde
     *
     * @param project
     * @param modelId
     * @return
     */
    public static boolean isSelectedModel(EtkProject project, iPartsModelId modelId) {
        Map<String, Set<String>> userFilterValues = SessionKeyHelper.getSelectedMBSModelMapWithUserSettingsCheck(project);
        if ((userFilterValues == null) || userFilterValues.isEmpty()) {
            return false;
        }
        Set<String> selectedModels = null;
        if (iPartsModel.isAggregateModel(modelId.getModelNumber())) {
            selectedModels = userFilterValues.get(MODEL_NUMBER_PREFIX_AGGREGATE);
        }
        if (iPartsModel.isVehicleModel(modelId.getModelNumber())) {
            selectedModels = userFilterValues.get(MODEL_NUMBER_PREFIX_CAR);
        }
        return (selectedModels != null) && selectedModels.contains(modelId.getModelNumber());
    }

    public static String getMbsConstructionDBDate() {
        String sessionValue = "";
        Session session = Session.get();
        if (session != null) {
            sessionValue = (String)Session.get().getAttribute(iPartsPlugin.SESSION_KEY_DBDATE_FOR_MBS_CONSTRUCTION);
        }
        if (StrUtils.isEmpty(sessionValue)) {
            return SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
        } else if (sessionValue.equals(EMPTY_MBS_CONSTRUCTION_DATE)) {
            sessionValue = "";
        }
        return sessionValue;
    }

    public static Calendar getMbsConstructionDate() {
        return SQLStringConvert.ppDateTimeStringToCalendar(getMbsConstructionDBDate());
    }

    public static boolean setMbsConstructionDBDate(String dbDate) {
        Session session = Session.get();
        if (session == null) {
            return false;
        }
        session.setAttribute(iPartsPlugin.SESSION_KEY_DBDATE_FOR_MBS_CONSTRUCTION, dbDate);
        return true;
    }

    public static boolean setMbsConstructionDate(Calendar calendar) {
        return setMbsConstructionDBDate(SQLStringConvert.calendarToPPDateTimeString(calendar));
    }

    public static boolean setMbsConstructionDate(Date chosenDate) {
        Calendar chosenDateCalendar = DateUtils.toCalendar_Date(chosenDate);
        return setMbsConstructionDate(chosenDateCalendar);
    }

    /**
     * Da ein leerer Sessionwert auf das aktuelle Datum gesetzt wird, muss der Umweg über
     * EMPTY_MBS_CONSTRUCTION_DATE gegangen werden
     * (sieh auch {@link #getMbsConstructionDBDate()})
     *
     * @return
     */
    public static boolean setMbsConstructionDateEmpty() {
        return setMbsConstructionDBDate(EMPTY_MBS_CONSTRUCTION_DATE);
    }

    public static boolean setSuperEditDividerPos(String key, int dividerPos) {
        Session session = Session.get();
        if (session == null) {
            return false;
        }
        session.setAttribute(key, dividerPos);
        return true;
    }

    public static int getSuperEditDividerPos(String key) {
        Session session = Session.get();
        if (session != null) {
            Integer sessionValue = (Integer)session.getAttribute(key);
            if (sessionValue != null) {
                return sessionValue;
            }
        }
        return -1;
    }

    public static boolean setDictionaryPreSelectIndex(String textKindId) {
        if (isSessionValid()) {
            Session.get().setAttribute(DICT_PRESELECT_INDEX, textKindId);
            return true;
        }
        return false;
    }

    public static String getDictionaryPreSelectIndex() {
        if (isSessionValid()) {
            return (String)Session.get().getAttribute(DICT_PRESELECT_INDEX);
        }
        return "";
    }
}
