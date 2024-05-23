/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Liste mit {@link iPartsDataBOMConstKitContent} Objekten
 */
public class iPartsDataBOMConstKitContentList extends EtkDataObjectList<iPartsDataBOMConstKitContent> implements iPartsConst {

    public iPartsDataBOMConstKitContentList() {
        setSearchWithoutActiveChangeSets(true);
    }

    private static final String[] FIELDS_FOR_GET_PARENTS = new String[]{ FIELD_DCK_SNR, FIELD_DCK_PARTPOS,
                                                                         FIELD_DCK_RELEASE_FROM, FIELD_DCK_RELEASE_TO,
                                                                         FIELD_DCK_REVFROM, FIELD_DCK_REVTO };

    /**
     * Liefert zu einer Liste von KemNo {@param kemNoList} und unter Überprüfung des jeweiligen kemDates
     * alle obersten SAA-Nummern
     * Dabei sind zu einer kemNo die jeweilig gültigen obersten Saa zugeordnet
     *
     * @param project
     * @param kemNoList
     * @return
     */
    public static Map<String, Set<String>> getValidSaasFromKEM(EtkProject project, Set<String> kemNoList) {
        Map<String, Set<String>> kemNoSaaMap = new HashMap<>();
        for (String kemNo : kemNoList) {
            Set<String> saaList = getValidSaasFromKEM(project, kemNo);
            Set<String> storedSaaList = kemNoSaaMap.get(kemNo);
            if (storedSaaList == null) {
                storedSaaList = new HashSet<>();
                kemNoSaaMap.put(kemNo, saaList);
            }
            saaList.addAll(storedSaaList);
        }
        return kemNoSaaMap;
    }

    /**
     * Liefert zu einer {@param kemNo} und unter Überprüfung des kemDate
     * alle obersten SAA-Nummern
     *
     * @param project
     * @param kemNo
     * @return
     */
    public static Set<String> getValidSaasFromKEM(EtkProject project, String kemNo) {
        Set<String> saaList = new HashSet<>();
        iPartsDataBOMConstKitContentList list = loadFromNutzDokKEMForWorkBasket(project, kemNo);
        if (!list.isEmpty()) {
            Map<iPartsBOMConstKitContentId, Boolean> validMap = new HashMap<>();
            Set<String> visitedEntries = new HashSet<>();
            for (iPartsDataBOMConstKitContent dataBOMConstKitContent : list) {
                String kemDate;
                if (kemNo.equals(dataBOMConstKitContent.getKemFrom())) {
                    kemDate = dataBOMConstKitContent.getReleaseDateFrom();
                } else {
                    kemDate = dataBOMConstKitContent.getReleaseDateTo();
                }
                String constKitNo = dataBOMConstKitContent.getAsId().getConstKitNo();
                String visitedKey = constKitNo + "|" + kemDate;
                if (visitedEntries.add(visitedKey)) {
                    Set<String> parents = getAllParentNumbersOfEdsMat(project, constKitNo, kemDate, validMap, 0);
                    if (!parents.isEmpty()) {
                        saaList.addAll(parents);
                    }
                }
            }
        }
        return saaList;
    }

    /**
     * Sucht in der Tabelle DA_EDS_CONST_KIT nach allen Vorkommen von {@param kem} in den Spalten DCK_KEMFROM oder DCK_KEMTO
     * (sortiert nach PK-Values)
     *
     * @param project
     * @param kemNo
     * @return
     */
    public static iPartsDataBOMConstKitContentList loadFromNutzDokKEMForWorkBasket(EtkProject project, String kemNo) {
        iPartsDataBOMConstKitContentList list = new iPartsDataBOMConstKitContentList();
        list.loadAllKemFromToFromDBForWorkbasket(project, kemNo, kemNo);
        return list;
    }

    /**
     * Hangelt sich rekursiv in der EDS-Konstruktion bis zur obersten SAA und überprüft dabei das {@param }kemDate)
     *
     * @param project
     * @param matNo
     * @param kemDate
     * @param validMap
     * @return
     */
    public static Set<String> getAllParentNumbersOfEdsMat(EtkProject project, String matNo,
                                                          String kemDate,
                                                          Map<iPartsBOMConstKitContentId, Boolean> validMap,
                                                          int recursionDepth) {
        //Notfallausstieg (max Strukturstufe ist 9, also dürfte 15 nie erreicht werden, außer es gilt Kreisbezüge)
        if (recursionDepth > 15) {
            return new HashSet<>();
        }

        DBDataObjectAttributesList edsAttributesList = project.getDbLayer().getAttributesList(
                TABLE_DA_EDS_CONST_KIT, FIELDS_FOR_GET_PARENTS, new String[]{ FIELD_DCK_SUB_SNR }, new String[]{ matNo });

        // alle Einträge die außerhalb des Gültigkeitsbereiches liegen aus der Liste entfernen
        edsAttributesList.removeIf(edsAttributes -> !isKemDateValid(edsAttributes, kemDate, validMap));

        Set<String> result = new HashSet<>();
        if (edsAttributesList.isEmpty()) {
            // es gibt keine weitere gültige obere SNR mehr, also sind wir am Ziel angekommen
            result.add(matNo);
        } else {
            if (edsAttributesList.size() > 1) {
                // eigentlich sollte hier pro SNR nur ein Stand getroffen werden, für den Fall dass es mehrere gibt, nur
                // den neusten behalten
                HashMap<String, List<DBDataObjectAttributes>> groupByRev = new HashMap<>();
                for (DBDataObjectAttributes edsAttributes : edsAttributesList) {
                    String currentPartNo = edsAttributes.getField(FIELD_DCK_SNR).getAsString();
                    groupByRev.putIfAbsent(currentPartNo, new DwList<>());
                    groupByRev.get(currentPartNo).add(edsAttributes);
                }

                if (groupByRev.size() != edsAttributesList.size()) {
                    edsAttributesList.clear();
                    for (List<DBDataObjectAttributes> attributesList : groupByRev.values()) {
                        if (!attributesList.isEmpty()) {
                            // die Liste nach REVTO sortieren, und nur den Eintrag mit höchster REVTO behalten
                            attributesList.sort(Comparator.comparingInt((DBDataObjectAttributes o) -> o.getField(FIELD_DCK_REVTO).getAsInteger()).reversed());
                            edsAttributesList.add(attributesList.get(0));
                        }
                    }
                }
            }

            for (DBDataObjectAttributes edsAttributes : edsAttributesList) {
                String parentPartNo = edsAttributes.getField(FIELD_DCK_SNR).getAsString();
                Set<String> allParentNumbersOfEdsMat = getAllParentNumbersOfEdsMat(project, parentPartNo, kemDate, validMap,
                                                                                   recursionDepth + 1);
                result.addAll(allParentNumbersOfEdsMat);
            }
        }
        return result;
    }

    /**
     * Überprüft KEM-DATUM-Ab (DCK_RELEASE_FROM) <= {@param kemDate} und
     * KEM-DATUM-BIS (DCK_RELEASE_TO)  > {@param kemDate}
     *
     * @param edsAttributes
     * @param kemDate
     * @param validMap
     * @return
     */
    private static boolean isKemDateValid(DBDataObjectAttributes edsAttributes, String kemDate,
                                          Map<iPartsBOMConstKitContentId, Boolean> validMap) {
        iPartsBOMConstKitContentId constKitContentId = new iPartsBOMConstKitContentId(edsAttributes);
        Boolean isValid = validMap.get(constKitContentId);
        if (isValid == null) {
            String releaseFrom = edsAttributes.getField(FIELD_DCK_RELEASE_FROM).getAsString();
            String releaseTo = edsAttributes.getField(FIELD_DCK_RELEASE_TO).getAsString();
            isValid = false;
            // KEM-DATUM-Ab <= 'kem_datum' und KEM-DATUM-BIS > 'kem_datum'
            if (releaseFrom.compareTo(kemDate) <= 0) {
                if (StrUtils.isValid(releaseTo)) {
                    if (releaseTo.compareTo(kemDate) > 0) {
                        isValid = true;
                    }
                } else {
                    isValid = true;
                }
            }
            // Ergebnis der Prüfung merken
            validMap.put(constKitContentId, isValid);
        }
        return isValid;
    }

    /**
     * Lädt alle Revision zu eine Teilenummer und EDS Position (sortiert nach Revision)
     *
     * @param project
     * @param upperPartNumber
     * @param position
     * @return
     */
    public static iPartsDataBOMConstKitContentList loadAllDataForUpperNumberAndPosition(EtkProject project,
                                                                                        String upperPartNumber,
                                                                                        String position) {
        iPartsDataBOMConstKitContentList list = new iPartsDataBOMConstKitContentList();
        list.loadAllDataForUpperNumberAndPositionFromDB(project, upperPartNumber, position, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllDataForUpperNumberAndPositionFromDB(EtkProject project, String upperPartNumber,
                                                            String position, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DCK_SNR, FIELD_DCK_PARTPOS };
        String[] whereValues = new String[]{ upperPartNumber, position };
        String[] sortFields = new String[]{ FIELD_DCK_REVFROM };
        searchSortAndFill(project, TABLE_DA_EDS_CONST_KIT, whereFields, whereValues, sortFields, DBDataObjectList.LoadType.COMPLETE, origin);
    }

    /**
     * Sucht in der Tabelle TABLE_DA_EDS_CONST_KIT nach allen Vorkommen von {@param kemTo} oder {@param kemFrom}
     * (sortiert nach PK-Values)
     *
     * @param project
     * @param kemFrom
     * @param kemTo
     */
    private void loadAllKemFromToFromDBForWorkbasket(EtkProject project, String kemFrom, String kemTo) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_KEMFROM),
                                                     TableAndFieldName.make(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_KEMTO) };
        String[] whereValues = new String[]{ kemFrom, kemTo };
        String[] sortFields = new String[]{ FIELD_DCK_SNR, FIELD_DCK_PARTPOS, FIELD_DCK_REVFROM };
        EtkDisplayFields selectedFields = null;

        selectedFields = new EtkDisplayFields();
        // Felder aus der DA_EDS_CONST_KIT Tabelle
        EtkDisplayField selectField = new EtkDisplayField(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_SNR, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_PARTPOS, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_KEMFROM, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_KEMTO, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_REVFROM, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_REVTO, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_RELEASE_FROM, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_RELEASE_TO, false, false);
        selectedFields.addFeld(selectField);

        searchSortAndFillWithJoin(project, null, selectedFields, whereTableAndFields, whereValues,
                                  true, sortFields, false, null);
    }

    @Override
    protected iPartsDataBOMConstKitContent getNewDataObject(EtkProject project) {
        return new iPartsDataBOMConstKitContent(project, null);
    }

}
