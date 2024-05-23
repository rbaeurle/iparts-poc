package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Liste von {@link iPartsDataMBSPartlist}.
 */
public class iPartsDataMBSPartlistList extends EtkDataObjectList<iPartsDataMBSPartlist> implements iPartsConst {

    private static final String[] FIELDS_FOR_GET_PARENTS = new String[]{ FIELD_DPM_SNR, FIELD_DPM_POS, FIELD_DPM_SORT,
                                                                         FIELD_DPM_KEM_FROM, FIELD_DPM_KEM_TO,
                                                                         FIELD_DPM_RELEASE_FROM, FIELD_DPM_RELEASE_TO };
    private static final String[] FIELDS_FOR_GET_PARENTS_STRUCTURE = new String[]{ FIELD_DSM_SNR, FIELD_DSM_POS, FIELD_DSM_SORT,
                                                                                   FIELD_DSM_KEM_FROM, FIELD_DSM_KEM_TO,
                                                                                   FIELD_DSM_RELEASE_FROM, FIELD_DSM_RELEASE_TO };
    private static final int MAX_OR_PER_QUERY = 100;

    public iPartsDataMBSPartlistList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Sucht in der Tabelle DA_PARTSLIST_MBS nach allen Vorkommen von {@param kem} in den Spalten DPM_KEM_FROM oder DPM_KEM_TO
     * (sortiert nach PK-Values)
     *
     * @param project
     * @param kemNo
     * @return
     */
    public static iPartsDataMBSPartlistList loadFromNutzDokKEMForWorkBasket(EtkProject project, String kemNo) {
        iPartsDataMBSPartlistList list = new iPartsDataMBSPartlistList();
        list.loadAllKemFromToFromDBForWorkbasket(project, kemNo, kemNo);
        return list;
    }

    /**
     * Sucht in der Tabelle DA_PARTSLIST_MBS nach allen Vorkommen von {@param kemNoSet} in den Spalten DPM_KEM_FROM oder DPM_KEM_TO
     * für {@param kemNoSet}.size() > 100
     * (sortiert nach PK-Values)
     *
     * @param project
     * @param kemNoSet
     * @return
     */
    public static Set<String> getUsedKemNosFromMBSPartlistBig(EtkProject project, Set<String> kemNoSet) {
        Set<String> kemNoValues = new HashSet<>();
        if (kemNoSet.isEmpty()) {
            return kemNoValues;
        }
        int extra = 0;
        if ((kemNoSet.size() % MAX_OR_PER_QUERY) != 0) {
            extra = 1;
        }

        int loop = (kemNoSet.size() / MAX_OR_PER_QUERY) + extra;
        if (loop <= 1) {
            return getUsedKemNosFromMBSPartlist(project, kemNoSet);
        } else {
            List<String> searchList = new DwList<>(kemNoSet);
            for (int loopCounter = 0; loopCounter < loop; loopCounter++) {
                int toIndex = Math.min((loopCounter + 1) * MAX_OR_PER_QUERY, searchList.size());
                List<String> currentList = searchList.subList(loopCounter * MAX_OR_PER_QUERY, toIndex);
                Set<String> currentSet = new HashSet<>(currentList);
                kemNoValues.addAll(getUsedKemNosFromMBSPartlist(project, currentSet));
            }
            return kemNoValues;
        }
    }

    /**
     * Sucht in der Tabelle DA_PARTSLIST_MBS nach allen Vorkommen von {@param kemNoSet} in den Spalten DPM_KEM_FROM oder DPM_KEM_TO
     * (sortiert nach PK-Values)
     *
     * @param project
     * @param kemNoSet
     * @return
     */
    public static Set<String> getUsedKemNosFromMBSPartlist(EtkProject project, Set<String> kemNoSet) {
        Set<String> kemNoValues = new HashSet<>();
        if (!kemNoSet.isEmpty()) {
            iPartsDataMBSPartlistList list = new iPartsDataMBSPartlistList();
            list.loadOrValuesFromFieldName(project, FIELD_DPM_KEM_FROM, kemNoSet, true);
            list.forEach((dataMBSPartlist) -> {
                kemNoValues.add(dataMBSPartlist.getKemFrom());
            });
            list.loadOrValuesFromFieldName(project, FIELD_DPM_KEM_TO, kemNoSet, true);
            list.forEach((dataMBSPartlist) -> {
                kemNoValues.add(dataMBSPartlist.getKemTo());
            });
        }
        return kemNoValues;
    }

    public static Set<String> getAllUsedKemNo(EtkProject project) {
        iPartsDataMBSPartlistList list = new iPartsDataMBSPartlistList();
        final Set<String> kemSet = new HashSet<>();

        list.searchSortAndFillWithJoin(project, null, null, null, null, false,
                                       null, false, new FoundAttributesCallback() {

                    @Override
                    public boolean foundAttributes(DBDataObjectAttributes attributes) {
                        String kemFrom = attributes.getFieldValue(iPartsConst.FIELD_DPM_KEM_FROM);
                        if (StrUtils.isValid(kemFrom)) {
                            kemSet.add(kemFrom);
                        }
                        String kemTo = attributes.getFieldValue(iPartsConst.FIELD_DPM_KEM_TO);
                        if (StrUtils.isValid(kemTo)) {
                            kemSet.add(kemTo);
                        }
                        return false;
                    }
                });
        return kemSet;
    }

    /**
     * Liefert zu einer Liste von KemNo {@param kemNoList} und unter Überprüfung des jeweiligen kemDates
     * alle obersten SAA-Nummern und ConGroups {@param MBSStructureId}
     * Dabei sind zu einer kemNo die jeweilig gültigen obersten Saa/ConGroups (MBSStructureId) zugeordnet
     *
     * @param project
     * @param kemNoList
     * @param withTimeCheck
     * @return
     */
    public static Map<String, Set<MBSStructureId>> getValidSaasFromKEM(EtkProject project, Set<String> kemNoList, boolean withTimeCheck) {
        Map<String, Set<MBSStructureId>> kemNoSaaMap = new HashMap<>();
        for (String kemNo : kemNoList) {
            Set<MBSStructureId> saaMbsStructureIdList = getValidSaasFromKEM(project, kemNo, withTimeCheck);
            Set<MBSStructureId> storedSaaList = kemNoSaaMap.get(kemNo);
            if (storedSaaList == null) {
                storedSaaList = new HashSet<>();
                kemNoSaaMap.put(kemNo, storedSaaList);
            }
            storedSaaList.addAll(saaMbsStructureIdList);

        }
        return kemNoSaaMap;
    }

    /**
     * Liefert zu einer {@param kemNo} und unter Überprüfung des kemDate
     * alle obersten SAA-Nummern/ConGroups (MBSStructureId)
     *
     * @param project
     * @param kemNo
     * @param withTimeCheck
     * @return
     */
    public static Set<MBSStructureId> getValidSaasFromKEM(EtkProject project, String kemNo, boolean withTimeCheck) {
        Set<MBSStructureId> saaMbsStructureIdList = new HashSet<>();

        iPartsDataMBSPartlistList list = loadFromNutzDokKEMForWorkBasket(project, kemNo);
        if (!list.isEmpty()) {
            Map<iPartsMBSPartlistId, Boolean> validMap = new HashMap<>();
            Set<String> visitedEntries = new HashSet<>();
            for (iPartsDataMBSPartlist dataMBSPartListEntry : list) {
                String kemDate;
                if (kemNo.equals(dataMBSPartListEntry.getKemFrom())) {
                    kemDate = dataMBSPartListEntry.getReleaseFrom();
                } else {
                    kemDate = dataMBSPartListEntry.getReleaseTo();
                }
                String constKitNo = dataMBSPartListEntry.getAsId().getUpperNo();
                String visitedKey = constKitNo + "|" + kemDate;
                if (visitedEntries.add(visitedKey)) {
                    Set<String> parents = getAllParentNumbersOfMbsMat(project, constKitNo, kemDate, validMap, withTimeCheck, 0);
                    if (!parents.isEmpty()) {
                        for (String conGroup : parents) {
                            DBDataObjectAttributesList mbsAttributesList = project.getDbLayer().getAttributesList(
                                    TABLE_DA_STRUCTURE_MBS, FIELDS_FOR_GET_PARENTS_STRUCTURE, new String[]{ FIELD_DSM_SUB_SNR }, new String[]{ conGroup });
                            // in der STRUCTURE keine Zeitüberprüfung!!
//                            if (withTimeCheck) {
//                                // alle Einträge die außerhalb des Gültigkeitsbereiches liegen aus der Liste entfernen
//                                mbsAttributesList.removeIf(mbsAttributes -> !isKemDateValid(mbsAttributes, kemDate, validMap, false));
//                            }
                            if (!mbsAttributesList.isEmpty()) {
                                for (DBDataObjectAttributes mbsAttributes : mbsAttributesList) {
                                    String parentPartNo = mbsAttributes.getField(FIELD_DSM_SNR).getAsString();
                                    MBSStructureId mbsStructureId = new MBSStructureId(parentPartNo, conGroup);
                                    saaMbsStructureIdList.add(mbsStructureId);
                                }
                            } else {
                                MBSStructureId mbsStructureId = new MBSStructureId("", conGroup);
                                saaMbsStructureIdList.add(mbsStructureId);
                            }
                        }
                    }
                }
            }
        }
        return saaMbsStructureIdList;
    }


    /**
     * Hangelt sich rekursiv in der MBS-Konstruktion bis zur obersten SAA und überprüft dabei das {@param }kemDate)
     *
     * @param project
     * @param matNo
     * @param kemDate
     * @param validMap
     * @param withTimeCheck
     * @return
     */
    public static Set<String> getAllParentNumbersOfMbsMat(EtkProject project, String matNo,
                                                          String kemDate,
                                                          Map<iPartsMBSPartlistId, Boolean> validMap,
                                                          boolean withTimeCheck, int recursionDepth) {
        // Notfallausstieg (max Strukturstufe ist 9, also dürfte 15 nie erreicht werden, außer es gilt Kreisbezüge)
        if (recursionDepth > 15) {
            return new HashSet<>();
        }

        DBDataObjectAttributesList mbsAttributesList = project.getDbLayer().getAttributesList(
                TABLE_DA_PARTSLIST_MBS, FIELDS_FOR_GET_PARENTS, new String[]{ FIELD_DPM_SUB_SNR }, new String[]{ matNo });

        if (withTimeCheck) {
            // alle Einträge die außerhalb des Gültigkeitsbereiches liegen aus der Liste entfernen
            mbsAttributesList.removeIf(mbsAttributes -> !isKemDateValid(mbsAttributes, kemDate, validMap, true));
        }

        Set<String> result = new HashSet<>();
        if (mbsAttributesList.isEmpty()) {
            // es gibt keine weitere gültige obere SNR mehr, also sind wir am Ziel angekommen
            result.add(matNo);
        } else {
            if (mbsAttributesList.size() > 1) {
                // eigentlich sollte hier pro SNR nur ein Stand getroffen werden, für den Fall dass es mehrere gibt, nur
                // den neusten behalten
                HashMap<String, List<DBDataObjectAttributes>> groupByRev = new HashMap<>();
                for (DBDataObjectAttributes mbsAttributes : mbsAttributesList) {
                    String currentPartNo = mbsAttributes.getField(FIELD_DPM_SNR).getAsString();
                    groupByRev.putIfAbsent(currentPartNo, new DwList<>());
                    groupByRev.get(currentPartNo).add(mbsAttributes);
                }

                if (groupByRev.size() != mbsAttributesList.size()) {
                    mbsAttributesList.clear();
                    for (List<DBDataObjectAttributes> attributesList : groupByRev.values()) {
                        if (!attributesList.isEmpty()) {
                            mbsAttributesList.add(attributesList.get(0));
                        }
                    }
                }
            }

            for (DBDataObjectAttributes mbsAttributes : mbsAttributesList) {
                String parentPartNo = mbsAttributes.getField(FIELD_DPM_SNR).getAsString();
                Set<String> allParentNumbersOfMbsMat = getAllParentNumbersOfMbsMat(project, parentPartNo, kemDate, validMap,
                                                                                   withTimeCheck, recursionDepth + 1);
                result.addAll(allParentNumbersOfMbsMat);
            }
        }
        return result;
    }

    /**
     * Überprüft KEM-DATUM-Ab (DPM_RELEASE_FROM) <= {@param kemDate} und
     * KEM-DATUM-BIS (DPM_RELEASE_TO)  > {@param kemDate}
     *
     * @param mbsAttributes
     * @param kemDate
     * @param validMap
     * @param isPartslist
     * @return
     */
    private static boolean isKemDateValid(DBDataObjectAttributes mbsAttributes, String kemDate,
                                          Map<iPartsMBSPartlistId, Boolean> validMap, boolean isPartslist) {
        iPartsMBSPartlistId mbsPartListId;
        if (isPartslist) {
            mbsPartListId = new iPartsMBSPartlistId(mbsAttributes.getField(iPartsConst.FIELD_DPM_SNR).getAsString(),
                                                    mbsAttributes.getField(iPartsConst.FIELD_DPM_POS).getAsString(),
                                                    mbsAttributes.getField(iPartsConst.FIELD_DPM_SORT).getAsString(),
                                                    mbsAttributes.getField(iPartsConst.FIELD_DPM_KEM_FROM).getAsString());
        } else {
            mbsPartListId = new iPartsMBSPartlistId(mbsAttributes.getField(iPartsConst.FIELD_DSM_SNR).getAsString(),
                                                    mbsAttributes.getField(iPartsConst.FIELD_DSM_POS).getAsString(),
                                                    mbsAttributes.getField(iPartsConst.FIELD_DSM_SORT).getAsString(),
                                                    mbsAttributes.getField(iPartsConst.FIELD_DSM_KEM_FROM).getAsString());
        }
        Boolean isValid = validMap.get(mbsPartListId);
        if (isValid == null) {
            String releaseFrom;
            String releaseTo;
            if (isPartslist) {
                releaseFrom = mbsAttributes.getField(FIELD_DPM_RELEASE_FROM).getAsString();
                releaseTo = mbsAttributes.getField(FIELD_DPM_RELEASE_TO).getAsString();
            } else {
                releaseFrom = mbsAttributes.getField(FIELD_DSM_RELEASE_FROM).getAsString();
                releaseTo = mbsAttributes.getField(FIELD_DSM_RELEASE_TO).getAsString();
            }
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
            validMap.put(mbsPartListId, isValid);
        }
        return isValid;
    }


    /**
     * Sucht mit ODER-Verknüpfung die Werte in {@param whereValues} für ein Feld {@param fieldName} in DA_PARTSLIST_MBS
     * Dabei werden Einträge mit leerer DPM_SUB_SNR (Texte) bereits ausgefiltert
     *
     * @param project
     * @param fieldName
     * @param whereValues
     * @param withSort
     */
    private void loadOrValuesFromFieldName(EtkProject project, String fieldName, Set<String> whereValues, boolean withSort) {
        clear(DBActionOrigin.FROM_DB);

        Set<String> foundValues = new HashSet<>();
        List<String> whereTableAndFields = new DwList<>();
        String[] sortFields = null;

        whereValues.forEach((whereValue) -> {
            whereTableAndFields.add(TableAndFieldName.make(TABLE_DA_PARTSLIST_MBS, fieldName));
        });
        if (withSort) {
            sortFields = new String[]{ fieldName };
        }
        EtkDisplayFields selectedFields = new EtkDisplayFields();
        // Felder aus der DA_PARTSLIST_MBS Tabelle
        EtkDisplayField selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, fieldName, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SUB_SNR, false, false);
        selectedFields.addFeld(selectField);

        searchSortAndFillWithJoin(project, null, selectedFields,
                                  ArrayUtil.toStringArray(whereTableAndFields), ArrayUtil.toStringArray(whereValues),
                                  true, sortFields, false, new FoundAttributesCallback() {

                    @Override
                    public boolean foundAttributes(DBDataObjectAttributes attributes) {
                        String subSNR = attributes.getFieldValue(FIELD_DPM_SUB_SNR);
                        // Texte aussortieren
                        if (StrUtils.isValid(subSNR)) {
                            String foundValue = attributes.getFieldValue(fieldName);
                            return foundValues.add(foundValue);
                        }
                        return false;
                    }
                });
    }

    /**
     * Sucht in der Tabelle TABLE_DA_PARTSLIST_MBS nach allen Vorkommen von {@param kemTo} oder {@param kemFrom}
     * (sortiert nach PK-Values)
     * ACHTUNG: es gibt sehr viele Einträge in DA_PARTSLIST_MBS => sehr kangsam
     *
     * @param project
     * @param kemFrom
     * @param kemTo
     */
    private void loadAllKemFromToFromDBForWorkbasket(EtkProject project, String kemFrom, String kemTo) {
        clear(DBActionOrigin.FROM_DB);

        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_KEM_FROM),
                                                     TableAndFieldName.make(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_KEM_TO) };
        String[] whereValues = new String[]{ kemFrom, kemTo };
        String[] sortFields = new String[]{ FIELD_DPM_SNR, FIELD_DPM_SORT, FIELD_DPM_POS };

        EtkDisplayFields selectedFields = new EtkDisplayFields();
        // Felder aus der DA_PARTSLIST_MBS Tabelle
        EtkDisplayField selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_KEM_FROM, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_KEM_TO, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_RELEASE_FROM, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_RELEASE_TO, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SUB_SNR, false, false);
        selectedFields.addFeld(selectField);

        searchSortAndFillWithJoin(project, null, selectedFields, whereTableAndFields, whereValues,
                                  true, sortFields, false, new FoundAttributesCallback() {

                    @Override
                    public boolean foundAttributes(DBDataObjectAttributes attributes) {
                        String subSNR = attributes.getFieldValue(FIELD_DPM_SUB_SNR);
                        // Texte aussortieren
                        return StrUtils.isValid(subSNR);
                    }
                });
    }


    @Override
    protected iPartsDataMBSPartlist getNewDataObject(EtkProject project) {
        return new iPartsDataMBSPartlist(project, null);
    }
}
