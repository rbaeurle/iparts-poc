/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsWorkBasketFollowUpDateId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache für die Internen Texte für alle WorkBaskets
 */
public class WorkBasketInternalTextCache {

    private static ObjectInstanceStrongLRUList<Object, WorkBasketInternalTextCache> instances =
            new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE_INFOS, iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    public static synchronized WorkBasketInternalTextCache getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), WorkBasketInternalTextCache.class, "WorkBasketInternalTextCache", false);
        WorkBasketInternalTextCache result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new WorkBasketInternalTextCache();
            // da pro AV und Suche starten, sowie bei Änderungen am Internen AV-Text
            // der Cache aktualisiert wird, ist ein Gesamtladen unnötigt
            instances.put(hashObject, result);
        }

        return result;
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    public static synchronized void updateWorkBasketCache(EtkProject project, iPartsWorkBasketTypes wbType) {
        getInstance(project).updateCache(project, wbType);
    }

    /**
     * Abfrage, ob zu einer SAA/BK-Nummer {@param saaBkKemNo} in einem Arbeitsvorrat {@param wbType} ein Interner-Text vorliegt
     *
     * @param project
     * @param wbType
     * @param saaBkKemNo
     * @return
     */
    public static synchronized boolean hasInternalText(EtkProject project, iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        return getInstance(project).hasInternalText(wbType, saaBkKemNo);
    }

    /**
     * Liefert, falls vorhanden, zu einer SAA/BK-Nummer {@param saaBkKemNo} in einem Arbeitsvorrat {@param wbType}
     * der ersten (aktuellsten) Interner-Text für die Anzeige im Grid
     *
     * @param project
     * @param wbType
     * @param saaBkKemNo
     * @return
     */
    public static synchronized String getFirstInternalText(EtkProject project, iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        return getInstance(project).getFirstInternalText(wbType, saaBkKemNo);
    }

    /**
     * Liefert, falls vorhanden, zu einer SAA/BK-Nummer {@param saaBkKemNo} in einem Arbeitsvorrat {@param wbType}
     * der ersten (aktuellsten) Interner-Text für die Anzeige im Grid {@param forGrid} = true oder für XML-Export {@param forExport} = true
     *
     * @param project
     * @param wbType
     * @param saaBkKemNo
     * @param forGrid
     * @param forExport
     * @return
     */
    public static synchronized String getFirstInternalText(EtkProject project, iPartsWorkBasketTypes wbType, String saaBkKemNo, boolean forGrid, boolean forExport) {
        return getInstance(project).getFirstInternalText(wbType, saaBkKemNo, forGrid, forExport);
    }

    /**
     * Liefert das DataObject zu einem Wiedervorlagedatum zu einer SAA/BK-Nummer {@param saaBkKemNo} in einem Arbeitsvorrat {@param wbType},
     * falls vorhanden
     *
     * @param project
     * @param wbType
     * @param saaBkKemNo
     * @return
     */
    public static synchronized iPartsDataInternalText getFollowUpDataObject(EtkProject project, iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        return getInstance(project).getFollowUpDataObject(wbType, saaBkKemNo);
    }

    /**
     * Liefert, falls vorhanden, zu einer SAA/BK-Nummer {@param saaBkKemNo} in einem Arbeitsvorrat {@param wbType}
     * das Wiedervorlagedatum (in DB-Format)
     *
     * @param project
     * @param wbType
     * @param saaBkKemNo
     * @return
     */
    public static synchronized String getFollowUpDate(EtkProject project, iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        return getInstance(project).getFollowUpDBDate(wbType, saaBkKemNo);
    }


    private Map<iPartsWorkBasketTypes, Map<String, List<iPartsDataInternalText>>> wbInternalTextMap = new HashMap<>();
    private Map<iPartsWorkBasketTypes, Map<String, List<iPartsDataInternalText>>> wbFollowUpDateMap = new HashMap<>();

    private void load(EtkProject project) {
        iPartsWorkBasketInternalTextId wbIntTextId = new iPartsWorkBasketInternalTextId(iPartsWorkBasketTypes.UNKNOWN, "*");
        String[] whereFields = new String[]{ iPartsConst.FIELD_DIT_DO_TYPE };
        String[] whereValues = new String[]{ wbIntTextId.getType() };
        searchAndBuildCache(project, whereFields, whereValues, null);
    }

    private void clearMapInMap(Map<iPartsWorkBasketTypes, Map<String, List<iPartsDataInternalText>>> wbMap,
                               iPartsWorkBasketTypes wbType) {
        if (wbType != null) {
            Map<String, List<iPartsDataInternalText>> wbIntTextMap = wbMap.get(wbType);
            if (wbIntTextMap != null) {
                wbIntTextMap = new HashMap<>();
                wbMap.put(wbType, wbIntTextMap);
            }
        }
    }

    private Map<String, List<iPartsDataInternalText>> addEmptyMapToMap(Map<iPartsWorkBasketTypes, Map<String, List<iPartsDataInternalText>>> wbMap,
                                                                       iPartsWorkBasketTypes wbType) {
        if (wbType != null) {
            Map<String, List<iPartsDataInternalText>> wbIntTextMap = wbMap.get(wbType);
            if (wbIntTextMap == null) {
                wbIntTextMap = new HashMap<>();
                wbMap.put(wbType, wbIntTextMap);
            }
            return wbIntTextMap;
        }
        return null;
    }

    private iPartsDataInternalTextList doSearch(EtkProject project, String[] whereFields, String[] whereValues) {
        iPartsDataInternalTextList list = new iPartsDataInternalTextList();
        list.setSearchWithoutActiveChangeSets(true);
        list.clear(DBActionOrigin.FROM_DB);

        String[] sortFields = new String[]{ iPartsConst.FIELD_DIT_CHANGE_DATE };

        // Absteigend nach Zeitstempel sortieren, der jüngste soll ganz oben in der Liste dargestellt werden.
        list.searchSortAndFillWithLike(project, iPartsConst.TABLE_DA_INTERNAL_TEXT, null,
                                       whereFields, whereValues, sortFields, true,
                                       DBDataObjectList.LoadType.COMPLETE, false, DBActionOrigin.FROM_DB);
        return list;
    }

    private void searchAndBuildCache(EtkProject project, String[] whereFields, String[] whereValues,
                                     iPartsWorkBasketInternalTextId wbIntTextId) {
        iPartsDataInternalTextList list = doSearch(project, whereFields, whereValues);
        if (list.isEmpty()) {
            if (wbIntTextId != null) {
                addEmptyMapToMap(wbInternalTextMap, wbIntTextId.getWbType());
            }
        } else {
            for (iPartsDataInternalText dataInternalText : list) {
                IdWithType id = IdWithType.fromDBString(iPartsWorkBasketInternalTextId.TYPE, dataInternalText.getDataObjectId());
                if (id != null) {
                    iPartsWorkBasketInternalTextId wbInternalTextId = new iPartsWorkBasketInternalTextId(id.toStringArrayWithoutType());
                    addToCacheMap(wbInternalTextId, dataInternalText);
                }
            }
        }
    }

    private void searchAndBuildFollowUpCache(EtkProject project, String[] whereFields, String[] whereValues,
                                             iPartsWorkBasketFollowUpDateId wbFollowUpId) {
        iPartsDataInternalTextList list = doSearch(project, whereFields, whereValues);
        if (list.isEmpty()) {
            if (wbFollowUpId != null) {
                addEmptyMapToMap(wbFollowUpDateMap, wbFollowUpId.getWbType());
            }
        } else {
            for (iPartsDataInternalText dataInternalText : list) {
                IdWithType id = IdWithType.fromDBString(iPartsWorkBasketFollowUpDateId.TYPE, dataInternalText.getDataObjectId());
                if (id != null) {
                    iPartsWorkBasketFollowUpDateId wbFollowUpDateId = new iPartsWorkBasketFollowUpDateId(id.toStringArrayWithoutType());
                    addToFollowCacheMap(wbFollowUpDateId, dataInternalText);
                }
            }
        }
    }

    private void addToCacheMap(Map<iPartsWorkBasketTypes, Map<String, List<iPartsDataInternalText>>> wbMap,
                               iPartsWorkBasketTypes wbType, String saaBkKemNo, iPartsDataInternalText dataInternalText) {
        Map<String, List<iPartsDataInternalText>> wbIntTextMap = addEmptyMapToMap(wbMap, wbType);
        if (wbIntTextMap == null) {
            return;
        }
        List<iPartsDataInternalText> internalTextList = wbIntTextMap.get(saaBkKemNo);
        if (internalTextList == null) {
            internalTextList = new DwList<>();
            wbIntTextMap.put(saaBkKemNo, internalTextList);
        }
        internalTextList.add(dataInternalText);
    }

    private void addToCacheMap(iPartsWorkBasketInternalTextId wbIntTextId, iPartsDataInternalText dataInternalText) {
        addToCacheMap(wbInternalTextMap, wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue(), dataInternalText);
    }

    private void addToFollowCacheMap(iPartsWorkBasketFollowUpDateId wbFollowUpId, iPartsDataInternalText dataInternalText) {
        addToCacheMap(wbFollowUpDateMap, wbFollowUpId.getWbType(), wbFollowUpId.getSaaBkKemValue(), dataInternalText);
    }

    private Map<String, List<iPartsDataInternalText>> getSaaBkKemMap(iPartsWorkBasketTypes wbType) {
        return wbInternalTextMap.get(wbType);
    }

    private List<iPartsDataInternalText> getInternalTextList(iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        Map<String, List<iPartsDataInternalText>> saaBkKemMap = getSaaBkKemMap(wbType);
        if (saaBkKemMap != null) {
            return saaBkKemMap.get(saaBkKemNo);
        }
        return null;
    }

    private iPartsDataInternalText getWbInternalText(iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        List<iPartsDataInternalText> internalTextList = getInternalTextList(wbType, saaBkKemNo);
        if (internalTextList != null) {
            iPartsWorkBasketInternalTextId id = new iPartsWorkBasketInternalTextId(wbType, saaBkKemNo);
            String searchType = id.toDBString();
            for (iPartsDataInternalText dataInternalText : internalTextList) {
                if (dataInternalText.getDataObjectId().equals(searchType)) {
                    return dataInternalText;
                }
            }
        }
        return null;
    }

    private iPartsWorkBasketInternalTextId getIdFromData(iPartsDataInternalText dataInternalText) {
        IdWithType id = IdWithType.fromDBString(iPartsWorkBasketInternalTextId.TYPE, dataInternalText.getDataObjectId());
        return new iPartsWorkBasketInternalTextId(id.toStringArrayWithoutType());
    }

    public boolean hasInternalText(iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        return getInternalTextList(wbType, saaBkKemNo) != null;
    }

    public String getFirstInternalText(iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        return getFirstInternalText(wbType, saaBkKemNo, true, false);
    }

    public String getFirstInternalText(iPartsWorkBasketTypes wbType, String saaBkKemNo, boolean forGrid, boolean forExport) {
        List<iPartsDataInternalText> internalTextList = getInternalTextList(wbType, saaBkKemNo);
        if ((internalTextList != null) && !internalTextList.isEmpty()) {
            String firstText = internalTextList.get(0).getText();
            if (forGrid) {
                firstText = StrUtils.replaceNewlinesWithSpaces(firstText);
            }
            if (forExport && (firstText.length() > 250)) {
                firstText = StrUtils.copySubString(firstText, 0, 250) + "...";
            }
            return firstText;
        }
        return "";
    }

    public List<iPartsDataInternalText> getInternalText(iPartsWorkBasketInternalTextId wbIntTextId) {
        List<iPartsDataInternalText> resultList = getInternalTextList(wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue());
        if (resultList == null) {
            resultList = new DwList<>();
        }
        return resultList;

    }

    private Map<String, List<iPartsDataInternalText>> getSaaBkKemFollowUpMap(iPartsWorkBasketTypes wbType) {
        return wbFollowUpDateMap.get(wbType);
    }

    private List<iPartsDataInternalText> getFollowUpList(iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        Map<String, List<iPartsDataInternalText>> saaBkKemMap = getSaaBkKemFollowUpMap(wbType);
        if (saaBkKemMap != null) {
            return saaBkKemMap.get(saaBkKemNo);
        }
        return null;
    }

    public iPartsDataInternalText getFollowUpDataObject(iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        List<iPartsDataInternalText> followUpList = getFollowUpList(wbType, saaBkKemNo);
        if ((followUpList != null) && !followUpList.isEmpty()) {
            iPartsDataInternalText followUpDate = followUpList.get(0);
            if (followUpDate.getAsId().getDataObjectType().equals(iPartsWorkBasketFollowUpDateId.TYPE)) {
                return followUpDate;
            }
        }
        return null;
    }

    /**
     * Wiedervorlagetermin (in DB-Formatierung) zu einem Arbeitsvorrat {@param wbType} für eine SAA/BK-Nummer {@param saaBkKemNo} bestimmen
     *
     * @param wbType
     * @param saaBkKemNo
     * @return
     */
    public String getFollowUpDBDate(iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        iPartsDataInternalText followUpDate = getFollowUpDataObject(wbType, saaBkKemNo);
        if (followUpDate != null) {
            return followUpDate.getFollowUpDateAsDB();
        }
        return "";
    }

    public void updateCache(EtkProject project, iPartsWorkBasketTypes wbType) {
        iPartsWorkBasketInternalTextId wbIntTextId = new iPartsWorkBasketInternalTextId(wbType, "*");
        // bisherige Einträge im Cache löschen
        clearMapInMap(wbInternalTextMap, wbIntTextId.getWbType());

        String[] whereFields = new String[]{ iPartsConst.FIELD_DIT_DO_TYPE, iPartsConst.FIELD_DIT_DO_ID };
        String[] whereValues = new String[]{ wbIntTextId.getType(), wbIntTextId.toDBString() };
        searchAndBuildCache(project, whereFields, whereValues, wbIntTextId);

        iPartsWorkBasketFollowUpDateId wbFollowUpId = new iPartsWorkBasketFollowUpDateId(wbIntTextId);
        clearMapInMap(wbFollowUpDateMap, wbFollowUpId.getWbType());
        whereValues = new String[]{ wbFollowUpId.getType(), wbFollowUpId.toDBString() };
        searchAndBuildFollowUpCache(project, whereFields, whereValues, wbFollowUpId);
    }

    public static void addFollowUpDateAttribute(EtkProject project, DBDataObjectAttributes attributes,
                                                iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        String followUpDate = WorkBasketInternalTextCache.getFollowUpDate(project, wbType, saaBkKemNo);
        attributes.addField(iPartsDataVirtualFieldsDefinition.WB_FOLLOWUP_DATE, followUpDate, true, DBActionOrigin.FROM_DB);
    }
}
