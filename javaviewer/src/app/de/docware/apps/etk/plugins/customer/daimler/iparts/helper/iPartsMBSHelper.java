/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSPartlistList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructureList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.mbs.iPartsMBSPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.db.*;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.SQLQuery;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.*;

import java.text.ParseException;
import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Helfer für MBS spezifische Daten
 */
public class iPartsMBSHelper implements iPartsConst {

    public static final boolean SHOW_ALL_ELEMENTS_IN_MBS_TREE = true; //true : keine Filterung im MBS-Baum nach MbsConstructionDBDate
    //false: Filterung des MBS-Baums nach MbsConstructionDBDate
    private static final char ALIAS_FOR_DB_JOIN = 'P';

    public static DBSQLQuery createQueryForValidMBSPartsListData(EtkProject project, String partNo) {
        String validationDate = SessionKeyHelper.getMbsConstructionDBDate();
        return createQueryForValidMBSData(project, TABLE_DA_PARTSLIST_MBS, new String[]{ FIELD_DPM_SNR },
                                          FIELD_DPM_SUB_SNR, partNo, FIELD_DPM_RELEASE_FROM,
                                          FIELD_DPM_RELEASE_TO, validationDate);
    }

    public static DBSQLQuery createQueryForValidMBSStructureData(EtkProject project, String modelNumber, String validationDate) {
        // Query mit zeitlicher Bedingungen für die Verknüpfung Baumuster -> ListNumber (1.Ebene)
        DBSQLQuery query = createQueryForValidMBSData(project, TABLE_DA_STRUCTURE_MBS, new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR },
                                                      FIELD_DSM_SNR, modelNumber, FIELD_DSM_RELEASE_FROM, FIELD_DSM_RELEASE_TO,
                                                      validationDate);
        String subPartsAliasName = "SUBDATA";
        // Bedingungen für die Verknüpfung ListNumber -> ConGroup (2.Ebene)
        addDateTimeConditions(query, subPartsAliasName, FIELD_DSM_RELEASE_FROM, FIELD_DSM_RELEASE_TO, validationDate);
        // Bedingungen für die Verknüpfung ConGroup -> Stücklisten
        addDateTimeConditions(query, TABLE_DA_PARTSLIST_MBS, FIELD_DPM_RELEASE_FROM, FIELD_DPM_RELEASE_TO, validationDate);

        query.join(new InnerJoin((TABLE_DA_STRUCTURE_MBS + " as " + subPartsAliasName).toLowerCase(),
                                 new Condition(TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR).toLowerCase(), Condition.OPERATOR_EQUALS,
                                               new Fields(TableAndFieldName.make(subPartsAliasName, FIELD_DSM_SNR).toLowerCase()))));
        query.join(new InnerJoin((TABLE_DA_PARTSLIST_MBS).toLowerCase(),
                                 new Condition(TableAndFieldName.make(subPartsAliasName, FIELD_DSM_SUB_SNR).toLowerCase(), Condition.OPERATOR_EQUALS,
                                               new Fields(TableAndFieldName.make(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR).toLowerCase()))));
        return query;
    }

    public static DBSQLQuery createQueryForValidMBSData(EtkProject project, String tablename, String[] selectFields,
                                                        String whereField, String whereValue, String releaseFromField,
                                                        String releaseToField, String dateForValidation) {
        DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        for (int i = 0; i < selectFields.length; i++) {
            selectFields[i] = TableAndFieldName.make(tablename, selectFields[i]);
        }
        query.selectDistinct(selectFields).from(new Tables(tablename.toLowerCase()));
        query.where(new Condition(TableAndFieldName.make(tablename, whereField), Condition.OPERATOR_EQUALS, whereValue));
        // Hier die Bedingungen für gültige Datensätze angeben
        addDateTimeConditions(query, tablename, releaseFromField, releaseToField, dateForValidation);
        return query;
    }

    /**
     * Liefert alle MBS Baumuster aus der Tabelle DA_STRUCTURE_MBS
     *
     * @param project
     * @return
     */
    public static Set<iPartsModelId> getAllModels(EtkProject project) {
        Set<iPartsModelId> result = new LinkedHashSet<>();
        DBSQLQuery query = project.getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        query.selectDistinct(FIELD_DSM_SNR).from(TABLE_DA_STRUCTURE_MBS);
        query.where(new Condition(FIELD_DSM_SNR, Condition.OPERATOR_LIKE, SQLUtils.wildcardExpressionToSQLLike(MODEL_NUMBER_PREFIX_AGGREGATE, false, true, false)));
        query.or(new Condition(FIELD_DSM_SNR, Condition.OPERATOR_LIKE, SQLUtils.wildcardExpressionToSQLLike(MODEL_NUMBER_PREFIX_CAR, false, true, false)));
        query.orderByDescending(FIELD_DSM_SNR);

        DBDataSet dataSet = null;
        try {
            dataSet = query.executeQuery();
            if (dataSet != null) {
                while (dataSet.next()) {
                    // Pseudo-Baumuster, die nur ein Mapping zwischen C- und identischem D-Baumuster bzw. C-Baumuster mit
                    // Suffix sind, sollen hier nicht berücksichtigt werden
                    String modelNumber = dataSet.getStringList().get(0);
                    if (!modelNumber.endsWith(MBS_VEHICLE_AGGREGATE_MAPPING_SUFFIX)) {
                        result.add(new iPartsModelId(modelNumber));
                    }
                }
            }
        } finally {
            // Verbindung schließen
            if (dataSet != null) {
                dataSet.close();
            }
        }
        return result;
    }

    /**
     * Alle KEM Datum ab/bis zu einer oberen Sachnummer und deren Unterstruktur laden und in einem Set speichern
     * Keine doppelten Vorkommnisse, kein unendlich und absteigend sortiert
     *
     * @param project
     * @param conGroup      aktuelle Sachnummer
     * @param subStructures Merkt sich die Sachnummern mit Unterstrukturen
     * @return
     */
    public static NavigableSet<String> getAllKemDatesForConGroup(EtkProject project, String conGroup, Set<String> subStructures) {
        NavigableSet<String> resultSet = new TreeSet<>();
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR) };
        String[] whereValues = new String[]{ conGroup };

        EtkDisplayFields selectedFields = new EtkDisplayFields();
        EtkDisplayField selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_RELEASE_FROM, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_RELEASE_TO, false, false);
        selectedFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SUB_SNR, false, false);
        selectedFields.addFeld(selectField);

        iPartsDataMBSPartlistList list = new iPartsDataMBSPartlistList();
        list.searchSortAndFillWithJoin(project, null, selectedFields, whereTableAndFields, whereValues,
                                       true, null, false, true, new EtkDataObjectList.FoundAttributesCallback() {

                    @Override
                    public boolean foundAttributes(DBDataObjectAttributes attributes) {
                        String subSnr = attributes.getFieldValue(FIELD_DPM_SUB_SNR);
                        // Texte nicht für die Datumssuche verwenden
                        if (StrUtils.isValid(subSnr)) {
                            String releaseFrom = attributes.getFieldValue(FIELD_DPM_RELEASE_FROM);
                            String releaseTo = attributes.getFieldValue(FIELD_DPM_RELEASE_TO);
                            if (StrUtils.isValid(releaseFrom)) {
                                resultSet.add(releaseFrom);
                            }
                            if (StrUtils.isValid(releaseTo)) {
                                resultSet.add(releaseTo);
                            }

                            // Falls das Feld gefüllt ist, gibt es Unterstrukturen deren Kem Datums auch beachtet werden müssen
                            String snrOfSubStructure = attributes.getFieldValue(ALIAS_FOR_DB_JOIN + "_" + FIELD_DPM_SNR);
                            if (StrUtils.isValid(snrOfSubStructure)) {
                                subStructures.add(snrOfSubStructure);
                            }
                        }
                        return false;
                    }
                }, new EtkDataObjectList.JoinData(TABLE_DA_PARTSLIST_MBS,
                                                  new String[]{ FIELD_DPM_SUB_SNR },
                                                  new String[]{ FIELD_DPM_SNR }, true, false, ALIAS_FOR_DB_JOIN));
        return resultSet;
    }

    /**
     * Überprüft, ob der Datensatz bezüglich seines Freigabedatums gültig ist.
     * <p>
     * Laut DAIMLER-10079: Structure.ReleaseDateTo>Date() and Structure.ReleaseDateFrom<=Date()
     *
     * @param dataMBSStructure
     * @return
     */
    public static boolean isValidDataset(iPartsDataMBSStructure dataMBSStructure) {
        if (!SHOW_ALL_ELEMENTS_IN_MBS_TREE) {
            // Filterung nach MbsConstructionDBDate aktiv
            if (dataMBSStructure != null) {
                String releaseFrom = dataMBSStructure.getFieldValue(FIELD_DSM_RELEASE_FROM);
                String releaseTo = dataMBSStructure.getFieldValue(FIELD_DSM_RELEASE_TO);
                // Den gesetzten Datumswert holen. Falls keiner gesetzt ist, wird das aktuelle Datum verwendet
                return ConstructionValidationDateHelper.isValidMbsDataset(releaseFrom, releaseTo);
            }
        }
        return true;
    }

//    /**
//     * Überprüft, ob das Zeitintervall passt
//     * <p>
//     * Laut DAIMLER-10079: ReleaseDateTo > Date() and ReleaseDateFrom <= Date()
//     *
//     * @param releaseFrom
//     * @param releaseTo
//     * @param validationDate
//     * @return
//     */
//    public static boolean isValidDataset(String releaseFrom, String releaseTo, String validationDate) {
//        ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getConstructionDateHelper(validationDate);
//        return validationHelper.isValidDataset(releaseFrom, releaseTo);
////!!        if (StrUtils.isValid(releaseFrom)) {
////!!            String compareStringForReleaseFrom = getMBSCompareStringForDateOnly(validationDate, false);
////!!            if (compareStringForReleaseFrom.compareTo(releaseFrom) < 0) { // 23:59:59
////!!                return false;
////!!            }
////!!        }
////!!        if (StrUtils.isValid(releaseTo)) { // 00:00:00
////!!            String compareStringForReleaseTo = getMBSCompareStringForDateOnly(validationDate, true);
////!!            return compareStringForReleaseTo.compareTo(releaseTo) < 0;
////!!        }
////        return true;
//    }

    /**
     * Fügt die zeitliche Bedingungen für die MBS Stücklisten hinzu. Wir steigen mit dem aktuellen Datum ein und müssen
     * daher alle "ungültigen" Einträge ausfiltern.
     * <p>
     * Bedingungen geändert in DAIMLER 10079: Structure.ReleaseDateTo>Date() and Structure.ReleaseDateFrom<=Date()
     *
     * @param query
     * @param table
     * @param releaseFromField
     * @param releaseToField
     * @param currentDate
     */
    private static void addDateTimeConditions(SQLQuery query, String table, String releaseFromField, String releaseToField, String currentDate) {
        if (StrUtils.isValid(currentDate)) {
            ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getConstructionDateHelper(currentDate, true);
            String compareStringForReleaseFrom = validationHelper.getValidationDateStringForFrom(); // 23:59:59
            String compareStringForReleaseTo = validationHelper.getValidationDateStringForTo(); // 00:00:00

            // Structure.ReleaseDateFrom<=Date()
            query.and(new Condition(TableAndFieldName.make(table, releaseFromField), "<=", compareStringForReleaseFrom));
            List<Condition> conditions = new ArrayList<>();
            // Structure.ReleaseDateTo>=Date()
            conditions.add(new Condition(TableAndFieldName.make(table, releaseToField), Condition.OPERATOR_EQUALS, ""));
            conditions.add(new Condition(TableAndFieldName.make(table, releaseToField), ">", compareStringForReleaseTo));
            ConditionList conditionList = new ConditionList(conditions, true);
            query.and(conditionList);
        }
    }

    /**
     * Datums Gültigkeit mit Beschränkung auf das Datum (Uhrzeit ist nicht relevant)
     * Dadurch muss das Datum-ab mit 23:59:59 Uhr verglichen werden
     *
     * @param validationDate
     * @return Calendar mit übergebenem Datum aber modifizierter Uhrzeit auf 23:59:59
     */
    public static Calendar getMBSCompareCalendarForDateFrom(Calendar validationDate) {
        if (validationDate == null) {
            return null;
        }
        Calendar calendar = (Calendar)validationDate.clone();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar;
    }

    /**
     * Datums Gültigkeit mit Beschränkung auf das Datum (Uhrzeit ist nicht relevant)
     * Dadurch muss das Datum-bis mit 00:00:00 Uhr verglichen werden
     *
     * @param validationDate
     * @return Calendar mit übergebenem Datum aber modifizierter Uhrzeit auf 00:00:00
     */
    public static Calendar getMBSCompareCalendarForDateTo(Calendar validationDate) {
        if (validationDate == null) {
            return null;
        }
        Calendar calendar = (Calendar)validationDate.clone();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    /**
     * Datums Gültigkeit mit Beschränkung auf das Datum (Uhrzeit ist nicht relevant)
     * Das Eingabe Datum wird in Calendar umgewandelt und dann je nach Parameter <code>isDateTo</code>
     * an {@link #getMBSCompareCalendarForDateFrom(Calendar)} oder {@link #getMBSCompareCalendarForDateTo(Calendar)} zur
     * Modifikation weitergeben, und anschließend wieder als String zur Verfügung gestellt.
     * <p>
     * Datum-bis wird mit 00:00:00 Uhr verglichen.
     * <p>
     * Datum-ab wird mit 23:59:59 Uhr verglichen.
     *
     * @param validationDate
     * @param isDateTo
     * @return Modifizierter Datums-String je nach Parameter isDateTo
     */
    public static String getMBSCompareStringForDateOnly(String validationDate, boolean isDateTo) {
        String compareString = validationDate;

        try {
            Calendar calendar = DateUtils.toCalendar_yyyyMMddHHmmss(validationDate);
            if (isDateTo) {
                calendar = getMBSCompareCalendarForDateTo(calendar);
            } else {
                calendar = getMBSCompareCalendarForDateFrom(calendar);
            }
            if (calendar != null) {
                compareString = DateUtils.toyyyyMMddHHmmss_Calendar(calendar);
            }
        } catch (ParseException | DateException e) {
            // nichts tun
        }
        return compareString;
    }

    /**
     * Suche in MBS-Konstruktion nach SAA-Nummer ohne Baumuster (für Sprung aus SA-TU)
     *
     * @param project
     * @param saa
     * @param partId
     * @return
     */
    public static PartListEntryId getPathToSaaWithoutValidModelMBS(EtkProject project, String saa, PartId partId) {
        //SA-TU Test
//        String sa_saa = "ZWUR1MA01";
//            String sa_saa = "Z?3802645";
//        partId = new PartId("A0024004802", "");
//        PartId sa_partId = new PartId("A0024004802", "");
//            PartId sa_partId = new PartId("N000000005565", "");
        //SA-TU Test Ende
        PartListEntryId foundPartListEntryId = getPathToSaaInMBS(project, saa, null, partId);
        return foundPartListEntryId;
    }

    /**
     * Suche in MBS-Konstruktion nach SAA-Nummer und mehreren Baumustern (für Sprung aus normalem TU)
     *
     * @param project
     * @param saa
     * @param validModels
     * @param partId
     * @return
     */
    public static PartListEntryId getPathToSaaWithValidModelMBS(EtkProject project, String saa, List<String> validModels, PartId partId) {
        PartListEntryId firstPartlstEntryId = null;
        // Übergebene Baumuster so umsortieren, dass zuerst die bereits in MBS konfigurierten BM untersucht werden
        // => beim Sprung wird die Laufzeit verkürzt
        Map<String, Set<String>> mbsModelMap = SessionKeyHelper.getSelectedMBSModelMap();
        Set<String> sortedModels = new LinkedHashSet<>();
        for (String modelNo : validModels) {
            if (isModelInMBSModelMap(mbsModelMap, modelNo)) {
                sortedModels.add(modelNo);
            }
        }
        sortedModels.addAll(validModels);
        boolean isPartIdValid = (partId != null) && !partId.getMatNr().isEmpty();

        // Alle Baumuster untersuchen
        for (String validModel : sortedModels) {
            // Versuch den NavPath in MBS zu bestimmen
            PartListEntryId foundPartListEntryId = getPathToSaaInMBS(project, saa, validModel, partId);
            if (foundPartListEntryId != null) {
                // gefundenen NavPath merken
                if ((firstPartlstEntryId == null) || !foundPartListEntryId.getKLfdnr().isEmpty()) {
                    firstPartlstEntryId = foundPartListEntryId;
                }
            }
            // wenn ein NavPath gefunden wurde und auch die zugehörige TeilPos => fertig
            if ((firstPartlstEntryId != null) && (!firstPartlstEntryId.getKLfdnr().isEmpty() || !isPartIdValid)) {
                break;
            }
        }
        return firstPartlstEntryId;
    }

    /**
     * Zurückgelieferte Liste beinhaltet die Teilenummer und die Teilenummern der Eltern bis zur
     * Strukturstufe 1
     *
     * @param project
     * @param matNo
     * @return
     */
    private static Set<String> calcPartPossibilities(EtkProject project, String matNo) {
        Set<String> possibleMatNoSet = new HashSet<>();
        Set<String> resultMap = new HashSet<>();
        possibleMatNoSet.add(matNo);
        String[] fields = new String[]{ FIELD_DPM_SNR, FIELD_DPM_SUB_SNR };
        String[] whereFields = new String[]{ FIELD_DPM_SUB_SNR };
        while (!possibleMatNoSet.isEmpty()) {
            String searchMatNo = possibleMatNoSet.iterator().next();
            possibleMatNoSet.remove(searchMatNo);
            resultMap.add(searchMatNo);
            String[] whereValues = new String[]{ searchMatNo };
            DBDataObjectAttributesList attribList = project.getDB().getAttributesList(TABLE_DA_PARTSLIST_MBS, fields, whereFields, whereValues);
            for (DBDataObjectAttributes attrib : attribList) {
                String snr = attrib.getFieldValue(FIELD_DPM_SNR);
                if (!(snr.startsWith("C") || snr.startsWith("D") || snr.startsWith("G") || snr.startsWith("Z"))) {
                    possibleMatNoSet.add(snr);
                }
            }
        }
        return resultMap;
    }

    /**
     * Eigentliche DB-Abfrage zur Bestimmung NavPath und TeilePos für Sprung
     *
     * @param project
     * @param saa
     * @param modelNo
     * @param partId
     * @return
     */
    private static PartListEntryId getPathToSaaInMBS(EtkProject project, String saa, String modelNo, PartId partId) {
        Map<iPartsModelId, Map<MBSStructureId, List<String>>> resultMap = new HashMap<>();
        PartListEntryId firstPartListEntryId = null;

        iPartsDataMBSStructureList structureList = new iPartsDataMBSStructureList();

        // Die Teilenummern der Eltern und ElternEltern usw ermitteln, damit man
        // später die laufende Nummer zusammenbasteln kann
        boolean isPartIdValid = (partId != null) && !partId.getMatNr().isEmpty();
        Set<String> possibleMatNoSet = new HashSet<>();
        if (isPartIdValid) {
            possibleMatNoSet = calcPartPossibilities(project, partId.getMatNr());
        }

        VarParam<Integer> counter = new VarParam<>(0);
        EtkDataObjectList.FoundAttributesCallback callback = getCallbackForSaaWithValidModel(project, resultMap, partId, possibleMatNoSet, counter);

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_STRUCTURE_MBS));
        selectFields.addFelder(project.getAllDisplayFieldsForTable(TABLE_DA_PARTSLIST_MBS));

        boolean isSaTu = true;
        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR) };
        String[] whereValues = new String[]{ saa };
        if (StrUtils.isValid(modelNo)) {
            // wenn ein Baumuster angegeben wurde, dann kann es keine SA_TU sein
            isSaTu = false;
            whereFields = StrUtils.mergeArrays(whereFields, TableAndFieldName.make(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR));
            whereValues = StrUtils.mergeArrays(whereValues, modelNo);
        }
        structureList.searchSortAndFillWithJoin(project, null, selectFields,
                                                whereFields, whereValues,
                                                false,
                                                new String[]{ ALIAS_FOR_DB_JOIN + "_" + FIELD_DSM_SNR,
                                                              ALIAS_FOR_DB_JOIN + "_" + FIELD_DSM_SUB_SNR },
                                                false, true,
                                                callback,
                                                new EtkDataObjectList.JoinData(TABLE_DA_STRUCTURE_MBS,
                                                                               new String[]{ FIELD_DSM_SUB_SNR },
                                                                               new String[]{ FIELD_DSM_SNR },
                                                                               false, false, ALIAS_FOR_DB_JOIN),
                                                new EtkDataObjectList.JoinData(TABLE_DA_PARTSLIST_MBS,
                                                                               new String[]{ TableAndFieldName.make(ALIAS_FOR_DB_JOIN + "", FIELD_DSM_SUB_SNR) },
                                                                               new String[]{ FIELD_DPM_SNR },
                                                                               // LeftOuter Join, um an SAA/GS -> Teilenummern ranzukommen
                                                                               true, false));

        if (!resultMap.isEmpty()) {
            if (isSaTu) {
                // Bei SA-TUs: erst die Baumuster untersuchen, die bereits in MBS konfiguriert sind
                Map<String, Set<String>> mbsModelMap = SessionKeyHelper.getSelectedMBSModelMap();
                for (iPartsModelId modelId : resultMap.keySet()) {
                    if (isModelInMBSModelMap(mbsModelMap, modelId.getModelNumber())) {
                        firstPartListEntryId = buildPartListEntryFromMap(modelId, resultMap.get(modelId), isPartIdValid);
                        if ((firstPartListEntryId != null) && (!firstPartListEntryId.getKLfdnr().isEmpty() || !isPartIdValid)) {
                            return firstPartListEntryId;
                        }
                    }
                }
            }
            // Suche den ersten vollständigen partListEntry über alle Baumuster
            for (Map.Entry<iPartsModelId, Map<MBSStructureId, List<String>>> modelEntry : resultMap.entrySet()) {
                iPartsModelId modelId = modelEntry.getKey();
                firstPartListEntryId = buildPartListEntryFromMap(modelId, modelEntry.getValue(), isPartIdValid);
                if ((firstPartListEntryId != null) && (!firstPartListEntryId.getKLfdnr().isEmpty() || !isPartIdValid)) {
                    break;
                }
            }
            resultMap.clear();
        }
        return firstPartListEntryId;
    }

    private static boolean isModelInMBSModelMap(Map<String, Set<String>> mbsModelMap, String modelNo) {
        if ((mbsModelMap != null) && StrUtils.isValid(modelNo)) {
            Set<String> models = null;
            if (iPartsModel.isAggregateModel(modelNo)) {
                models = mbsModelMap.get(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE);
            }
            if (iPartsModel.isVehicleModel(modelNo)) {
                models = mbsModelMap.get(iPartsConst.MODEL_NUMBER_PREFIX_CAR);
            }
            if (models != null) {
                return models.contains(modelNo);
            }
        }
        return false;
    }

    /**
     * Sucht innerhalb eines Baumusters nach dem ersten vollständigen partListEntry der DB-Suche
     *
     * @param modelId
     * @param structureMap
     * @param isPartIdValid
     * @return
     */
    private static PartListEntryId buildPartListEntryFromMap(iPartsModelId modelId, Map<MBSStructureId, List<String>> structureMap, boolean isPartIdValid) {
        PartListEntryId firstPartlstEntryId = null;
        for (Map.Entry<MBSStructureId, List<String>> entry : structureMap.entrySet()) {
            if (entry.getKey() != null) {
                List<iPartsVirtualNode> nodes = new ArrayList<>();
                nodes.add(new iPartsVirtualNode(iPartsNodeType.MBS_STRUCTURE, modelId));
                nodes.add(new iPartsVirtualNode(iPartsNodeType.MBS, entry.getKey()));
                AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(ArrayUtil.toArray(nodes)), "");
                PartListEntryId pId;
                if (!entry.getValue().isEmpty()) {
                    pId = new PartListEntryId(assemblyId.getKVari(), assemblyId.getKVer(), entry.getValue().get(0));
                } else {
                    pId = new PartListEntryId(assemblyId.getKVari(), assemblyId.getKVer(), "");
                }
                if (firstPartlstEntryId == null) {
                    firstPartlstEntryId = pId;
                } else {
                    if (firstPartlstEntryId.getKLfdnr().isEmpty() && !pId.getKLfdnr().isEmpty()) {
                        firstPartlstEntryId = pId;
                    }
                }
                if (!firstPartlstEntryId.getKLfdnr().isEmpty() || !isPartIdValid) {
                    break;
                }
            }
        }
        return firstPartlstEntryId;
    }

    private static EtkDataObjectList.FoundAttributesCallback getCallbackForSaaWithValidModel(EtkProject project,
                                                                                             Map<iPartsModelId, Map<MBSStructureId, List<String>>> resultMap,
                                                                                             PartId partId, Set<String> possibleMatNoSet,
                                                                                             VarParam<Integer> counter) {
        // Eingestellter Datumswert in MBS-Konstruktion
        ConstructionValidationDateHelper validationHelper = ConstructionValidationDateHelper.getMbsConstructionDateHelper();

        return new EtkDataObjectList.FoundAttributesCallback() {

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                counter.setValue(counter.getValue() + 1);
                String listNumber = attributes.getFieldValue(ALIAS_FOR_DB_JOIN + "_" + FIELD_DSM_SNR);
                // C und D Nummern (Baumuster) dürfen in der Struktur nicht berücksichtigt werden
                if (iPartsModel.isAggregateModel(listNumber) || iPartsModel.isVehicleModel(listNumber)) {
                    return false;
                }
                String conGroup = attributes.getFieldValue(ALIAS_FOR_DB_JOIN + "_" + FIELD_DSM_SUB_SNR);
                if (iPartsModel.isAggregateModel(conGroup) || iPartsModel.isVehicleModel(conGroup)) {
                    return false;
                }

                // Zeitliche Überprüfung des STRUCTURE-Eintrags
                String releaseFrom = attributes.getFieldValue(FIELD_DSM_RELEASE_FROM);
                String releaseTo = attributes.getFieldValue(FIELD_DSM_RELEASE_TO);
                if (!validationHelper.isValidDataset(releaseFrom, releaseTo)) {
                    return false;
                }
                // Flag, ob es eine SAA/GS mit direkten Teilenummern ist (ohne KG Ebene)
                boolean isListNumberWithPartNumber;
                if (StrUtils.isValid(conGroup)) {
                    isListNumberWithPartNumber = !conGroup.startsWith(BASE_LIST_NUMBER_PREFIX) && !conGroup.startsWith(SAA_NUMBER_PREFIX);
                    if (!isListNumberWithPartNumber) {
                        // Check, ob es echte Stücklistendaten gibt (Blick in DA_PARTSLIST_MBS)
                        String partslistData = attributes.getFieldValue(StrUtils.makeDelimitedString(TABLE_DA_PARTSLIST_MBS, FIELD_DPM_SNR));
                        if (StrUtils.isEmpty(partslistData)) {
                            return false;
                        }
                    }
                } else {
                    // Texte ignorieren
                    return false;
                }

                // Zeitliche Überprüfung des PARTSLIST-Eintrags
                releaseFrom = attributes.getFieldValue(FIELD_DPM_RELEASE_FROM);
                releaseTo = attributes.getFieldValue(FIELD_DPM_RELEASE_TO);
                if (!validationHelper.isValidDataset(releaseFrom, releaseTo)) {
                    return false;
                }
                // Baumuster, MBSStructureId und ggf TeilePos Guid merken
                iPartsModelId modelId = new iPartsModelId(attributes.getFieldValue(FIELD_DSM_SNR));
                Map<MBSStructureId, List<String>> structureMap = resultMap.get(modelId);
                if (structureMap == null) {
                    structureMap = new HashMap<>();
                    resultMap.put(modelId, structureMap);
                }
                MBSStructureId structureId;
                if (isListNumberWithPartNumber) {
                    structureId = new MBSStructureId(listNumber, listNumber);
                } else {
                    structureId = new MBSStructureId(listNumber, conGroup);
                }
                List<String> guidList = structureMap.get(structureId);
                if (guidList == null) {
                    guidList = new DwList<>();
                    structureMap.put(structureId, guidList);
                }
                String partlistNumber;
                if (isListNumberWithPartNumber) {
                    partlistNumber = conGroup;
                } else {
                    partlistNumber = attributes.getFieldValue(FIELD_DPM_SUB_SNR);
                }
                if (StrUtils.isValid(partlistNumber)) {
                    if (partId != null) {
                        if (partId.getMatNr().equals(partlistNumber)) {
                            String guid = buildMBSGUIDForPartListSeqNo(attributes, structureId.getConGroup(), isListNumberWithPartNumber, FIELD_DSM_SNR);
                            if (!guidList.contains(guid)) {
                                guidList.add(guid);
                            }
                        } else {
                            // Es handelt sich um eine höhere Strukturstufe als 1
                            if (possibleMatNoSet.contains(partlistNumber)) {
                                String guid = buildGuid(project, attributes, structureId.getConGroup(), isListNumberWithPartNumber, partId.getMatNr(),
                                                        possibleMatNoSet);
                                if (!guid.isEmpty() && !guidList.contains(guid)) {
                                    guidList.add(guid);
                                }
                            }
                        }
                    }
                }
                if (isListNumberWithPartNumber) {
                    partlistNumber = attributes.getFieldValue(FIELD_DPM_SUB_SNR);
                    if (StrUtils.isValid(partlistNumber)) {
                        if (partId != null) {
                            if (partId.getMatNr().equals(partlistNumber)) {
                                String guid = buildMBSGUIDForPartListSeqNo(attributes, structureId.getConGroup(), false, FIELD_DSM_SNR);
                                if (!guidList.contains(guid)) {
                                    guidList.add(guid);
                                }
                            } else {
                                // Es handelt sich um eine höhere Strukturstufe als 1
                                if (possibleMatNoSet.contains(partlistNumber)) {
                                    String guid = buildGuid(project, attributes, structureId.getConGroup(), isListNumberWithPartNumber, partId.getMatNr(),
                                                            possibleMatNoSet);
                                    if (!guid.isEmpty() && !guidList.contains(guid)) {
                                        guidList.add(guid);
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        };
    }

    /**
     * Für Unterstrukturen größer Strukturstufe 1 beinhaltet die GUID den Pfad bis zum Teil
     * Dieser wird hier berechnet
     *
     * @param project
     * @param attributes
     * @param structurePath
     * @param isListNumberWithPartNumber
     * @param searchMatNo
     * @param possibleMatNoSet
     * @return
     */
    private static String buildGuid(EtkProject project, DBDataObjectAttributes attributes, String structurePath, boolean isListNumberWithPartNumber,
                                    String searchMatNo, Set<String> possibleMatNoSet) {
        String guid = "";
        String[] fields = new String[]{ FIELD_DPM_SNR, FIELD_DPM_POS, FIELD_DPM_SORT, FIELD_DPM_KEM_FROM, FIELD_DPM_SUB_SNR };
        String[] whereFields = new String[]{ FIELD_DPM_SNR };
        String[] whereNotFields = new String[]{ FIELD_DPM_SUB_SNR };
        String[] whereNotValues = new String[]{ "" };
        String currentSearchSnr = attributes.getFieldValue(FIELD_DPM_SUB_SNR);
        boolean notFound = true;
        if (currentSearchSnr.equals(searchMatNo)) {
            if (isListNumberWithPartNumber) {
                guid = buildMBSGUIDForPartListSeqNo(attributes, structurePath, isListNumberWithPartNumber, FIELD_DSM_SUB_SNR);

            } else {
                guid = buildMBSGUIDForPartListSeqNo(attributes, structurePath, isListNumberWithPartNumber, FIELD_DSM_SNR);
            }
            iPartsMBSPrimaryKey primaryKey = createPrimaryKeyForStructurePath(attributes.getFieldValue(FIELD_DPM_SNR), attributes);
            guid = guid + K_SOURCE_CONTEXT_DELIMITER + primaryKey.createMBSGUID();
            return guid;
        }

        // Hier wird der Teil der GUID zusammengebaut, der die eigentliche Teileposition beschreibt
        // Die GUID der Teilepositionen in der Stückliste wird in iPartsVirtuakAssemblyMBS.createMBSPartListEntry gebildet.
        // Der Part für die Unterstrukturen in iPartsVirtuakAssemblyMBS.addSubStructureEntries
        while (notFound) {
            String[] whereValues = new String[]{ currentSearchSnr };
            DBDataObjectAttributesList attribList = project.getDB().getAttributesList(TABLE_DA_PARTSLIST_MBS, fields, whereFields, whereValues,
                                                                                      null, null, whereNotFields, whereNotValues, false, -1);
            if (!attribList.isEmpty()) {
                boolean rightPath = false;
                for (DBDataObjectAttributes attrib : attribList) {
                    String subSnr = attrib.getFieldValue(FIELD_DPM_SUB_SNR);
                    if (possibleMatNoSet.contains(subSnr)) {
                        if (searchMatNo.equals(subSnr)) {
                            iPartsMBSPrimaryKey primaryKey = createPrimaryKeyForStructurePath(currentSearchSnr, attrib);
                            guid = guid + K_SOURCE_CONTEXT_DELIMITER + primaryKey.createMBSGUID();
                            notFound = false;
                        } else {
                            // guid zusammenbauen
                            iPartsMBSPrimaryKey primaryKey = createPrimaryKeyForStructurePath(subSnr, attrib);
                            guid = guid + K_SOURCE_CONTEXT_DELIMITER + primaryKey.createMBSGUID();
                            currentSearchSnr = subSnr;
                        }
                        rightPath = true;
                        break;
                    }
                }
                if (!rightPath) {
                    notFound = false;
                }
            } else {
                notFound = false;
            }
        }

        // Hier wird der Teil der GUID zusammengebaut, der den Weg von der ConGroup zum eigentlichen Teil beschreibt
        if (!guid.isEmpty()) {
            String prefixGuid;
            if (isListNumberWithPartNumber) {
                prefixGuid = buildMBSGUIDForPartListSeqNo(attributes, structurePath, isListNumberWithPartNumber, FIELD_DSM_SNR);
            } else {
                DBDataObjectAttributes partlistAttributes = new DBDataObjectAttributes();

                partlistAttributes.addField(FIELD_DPM_SNR, attributes.getField(FIELD_DPM_SUB_SNR).getAsString(), DBActionOrigin.FROM_DB);
                partlistAttributes.addField(FIELD_DPM_POS, attributes.getField(FIELD_DPM_POS).getAsString(), DBActionOrigin.FROM_DB);
                partlistAttributes.addField(FIELD_DPM_SORT, attributes.getField(FIELD_DPM_SORT).getAsString(), DBActionOrigin.FROM_DB);
                partlistAttributes.addField(FIELD_DPM_KEM_FROM, attributes.getField(FIELD_DPM_KEM_FROM).getAsString(), DBActionOrigin.FROM_DB);
                prefixGuid = iPartsMBSPrimaryKey.buildMBSGUIDForPartListSeqNo(partlistAttributes, structurePath);
            }
            guid = prefixGuid + guid;
        }
        return guid;
    }

    private static iPartsMBSPrimaryKey createPrimaryKeyForStructurePath(String subSnr, DBDataObjectAttributes attributes) {
        String pos = attributes.getFieldValue(FIELD_DPM_POS);
        String sort = attributes.getFieldValue(FIELD_DPM_SORT);
        String kemFrom = attributes.getFieldValue(FIELD_DPM_KEM_FROM);
        return new iPartsMBSPrimaryKey(subSnr, pos, sort, kemFrom);
    }

    private static String buildMBSGUIDForPartListSeqNo(DBDataObjectAttributes attributes, String structurePath, boolean isListNumberWithPartNumber, String dsmSnrField) {
        DBDataObjectAttributes partlistAttributes;
        if (isListNumberWithPartNumber) {
            partlistAttributes = new DBDataObjectAttributes();
            partlistAttributes.addField(FIELD_DPM_SNR, attributes.getField(ALIAS_FOR_DB_JOIN + "_" + dsmSnrField).getAsString(), DBActionOrigin.FROM_DB);
            partlistAttributes.addField(FIELD_DPM_POS, attributes.getField(ALIAS_FOR_DB_JOIN + "_" + FIELD_DSM_POS).getAsString(), DBActionOrigin.FROM_DB);
            partlistAttributes.addField(FIELD_DPM_SORT, attributes.getField(ALIAS_FOR_DB_JOIN + "_" + FIELD_DSM_SORT).getAsString(), DBActionOrigin.FROM_DB);
            partlistAttributes.addField(FIELD_DPM_KEM_FROM, attributes.getField(ALIAS_FOR_DB_JOIN + "_" + FIELD_DSM_KEM_FROM).getAsString(), DBActionOrigin.FROM_DB);
        } else {
            partlistAttributes = attributes;
        }
        return iPartsMBSPrimaryKey.buildMBSGUIDForPartListSeqNo(partlistAttributes, structurePath);
    }
}
