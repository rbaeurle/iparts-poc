/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.CodeHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.CodeRule;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Liste mit {@link iPartsDataCode} Objekten
 */
public class iPartsDataCodeList extends EtkDataObjectList<iPartsDataCode> implements iPartsConst {

    public static boolean SHOW_PROVAL_CODE_DESC = true;

    /**
     * MBS Code sollen im Moment nicht ausgegeben werden. Deshalb das Laden von MBS Coden,
     * über die whereFields und whereValues, fest verriegeln. PROVAL Code müssen im zum Teil geladen werden,
     * da deren Beschreibung ggf. anstatt der des Nicht-Proval Codes verwendet wird.
     *
     * @param importDataOrigin Der gesuchte Wert für dass Source Feld. Fall gesetzt, dann wird nach dieser expliziten
     *                         Quelle gesucht. Falls {@code null} oder {@link iPartsImportDataOrigin#UNKNOWN},
     *                         wird das Laden von PROVAL und MBS Code fest verriegelt.
     * @param omitProvalCodes
     * @param whereFields
     * @return
     */
    private static String[] getWhereFields(iPartsImportDataOrigin importDataOrigin, boolean omitProvalCodes, String... whereFields) {
        if ((importDataOrigin != null) && (importDataOrigin != iPartsImportDataOrigin.UNKNOWN)) {
            return StrUtils.mergeArrays(whereFields, FIELD_DC_SOURCE);
        }
        String[] allWhereFields = whereFields;
        if (omitProvalCodes) {
            allWhereFields = StrUtils.mergeArrays(allWhereFields, FIELD_DC_SOURCE);
        }
        if (OMIT_MBS_CODES) {
            allWhereFields = StrUtils.mergeArrays(allWhereFields, FIELD_DC_SOURCE);
        }
        return allWhereFields;
    }

    /**
     * Analog zu {@link #getWhereFields}.
     *
     * @param importDataOrigin
     * @param omitProvalCodes
     * @param whereValues
     * @return
     */
    private static String[] getWhereValues(iPartsImportDataOrigin importDataOrigin, boolean omitProvalCodes, String... whereValues) {
        if ((importDataOrigin != null) && (importDataOrigin != iPartsImportDataOrigin.UNKNOWN)) {
            return StrUtils.mergeArrays(whereValues, importDataOrigin.getOrigin());
        }
        String[] allWhereValues = whereValues;
        if (omitProvalCodes) {
            allWhereValues = StrUtils.mergeArrays(allWhereValues, getNotWhereValue(iPartsImportDataOrigin.PROVAL.getOrigin()));
        }
        if (OMIT_MBS_CODES) {
            allWhereValues = StrUtils.mergeArrays(allWhereValues, getNotWhereValue(iPartsImportDataOrigin.SAP_MBS.getOrigin()));
        }
        return allWhereValues;
    }

    public iPartsDataCodeList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataCode}s, die dem übergebenen iPartsCodeDataId.getCodeId() zugeordnet
     * und nach SDATA sortiert sind.
     *
     * @param project
     * @param dbLanguage
     * @param codeId
     * @return
     */
    public static iPartsDataCodeList loadCodeDataSorted(EtkProject project, String dbLanguage, iPartsCodeDataId codeId) {
        iPartsDataCodeList list = new iPartsDataCodeList();
        list.loadData(project, dbLanguage, codeId.getCodeId(), codeId.getSeriesNo(), true, OMIT_PROVAL_CODES, false, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataCode}s, die dem übergebenen iPartsCodeDataId.getCodeId() zugeordnet
     * und nach SDATA sortiert sind.
     * Ist {@link iPartsCodeDataId#getSource()} nicht {@link iPartsImportDataOrigin#UNKNOWN}, so wird die Suche auf diesen Source eingeschräkt.
     *
     * @param project
     * @param codeId
     * @return
     */
    public static iPartsDataCodeList loadCodeDataSortedWithoutJoin(EtkProject project, iPartsCodeDataId codeId) {
        iPartsDataCodeList list = new iPartsDataCodeList();
        list.loadDataWithoutJoin(project, codeId.getCodeId(), codeId.getSeriesNo(), codeId.getProductGroup(), codeId.getSource(), true, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataCode}s, die dem übergebenen iPartsCodeDataId.getCodeId() zugeordnet.
     * Ist {@link iPartsCodeDataId#getSource()} nicht {@link iPartsImportDataOrigin#UNKNOWN}, so wird die Suche auf diesen Source eingeschräkt.
     *
     * @param project
     * @param codeId
     * @return
     */
    public static iPartsDataCodeList loadCodeDataSortedWithoutJoinForCheckProductGroupCode(EtkProject project, iPartsCodeDataId codeId) {
        iPartsDataCodeList list = new iPartsDataCodeList();
        list.loadDataWithoutJoin(project, codeId.getCodeId(), codeId.getProductGroup(), codeId.getSource(), false, LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataCode}s für die übergebene Baureihe und Quelle
     *
     * @param project
     * @param seriesNo
     * @param dataOrigin
     * @return
     */
    public static iPartsDataCodeList loadCodeDataSortedWithoutJoinForSeriesAndSource(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin) {
        iPartsDataCodeList list = new iPartsDataCodeList();
        list.loadDataWithoutJoinForSeriesAndOrigin(project, seriesNo, dataOrigin, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Code-Informationenn (in allen Sprachen) zur übergebenen Baureihe und Produktgruppe
     *
     * @param project
     * @param seriesNo
     * @param productGroup
     * @return
     */
    public static iPartsDataCodeList loadCodesForSeriesAndProductGroupAllLanguages(EtkProject project, String seriesNo, String productGroup,
                                                                                   iPartsImportDataOrigin dataOrigin, boolean withHistoryData) {
        iPartsDataCodeList list = new iPartsDataCodeList();
        String[] whereFields = new String[]{ FIELD_DC_SERIES_NO, FIELD_DC_PGRP, FIELD_DC_SOURCE };
        String[] whereValues = new String[]{ seriesNo, productGroup, dataOrigin.getOrigin() };
        list.loadDataForWhereFieldsAndValues(project, whereFields, whereValues, DBActionOrigin.FROM_DB, withHistoryData, null);
        return list;
    }

    /**
     * Lädt zuerst alle aktuellen Codedaten (SDATB = leer), die keinen Baureihenbezug haben und danach alle aktuellen Codedaten
     * (SDATB = leer) zur übergebenen Baureihe. Als Ergebnis wird eine Liste mit allen gefundenen Codedaten zurückgeliefert.
     * Zusätzlich kann eine Code-Importquelle übergeben werden.
     *
     * @param project
     * @param seriesNo
     * @param importDataOrigin
     * @return
     */
    public static iPartsDataCodeList loadAllUniversalCodesAndAllCodesForSeries(EtkProject project, String seriesNo, iPartsImportDataOrigin importDataOrigin) {
        String[] whereFields = new String[]{ FIELD_DC_SERIES_NO, FIELD_DC_SDATB };
        String[] whereValues = new String[]{ "", "" };
        if ((importDataOrigin != null) && (importDataOrigin != iPartsImportDataOrigin.UNKNOWN)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DC_SOURCE });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ importDataOrigin.getOrigin() });
        }
        // Erst die universellen Code laden
        iPartsDataCodeList universalList = new iPartsDataCodeList();
        universalList.loadDataForWhereFieldsAndValues(project, whereFields, whereValues, DBActionOrigin.FROM_DB, false, project.getDBLanguage());
        // Und nun die baureihenbezogenen Daten laden
        whereValues[0] = seriesNo;
        iPartsDataCodeList seriesList = new iPartsDataCodeList();
        seriesList.loadDataForWhereFieldsAndValues(project, whereFields, whereValues, DBActionOrigin.FROM_DB, false, project.getDBLanguage());
        universalList.addAll(seriesList, DBActionOrigin.FROM_DB);
        return universalList;
    }

    /**
     * Lädt alle Code-Informationen (in allen Sprachen) passend zur Baureihe und zur Quelle
     *
     * @param project
     * @param seriesNo
     * @param source
     * @return
     */
    public static iPartsDataCodeList loadCodeForSeriesAndSourceAllLanguages(EtkProject project, String seriesNo, iPartsImportDataOrigin source) {
        iPartsDataCodeList list = new iPartsDataCodeList();
        String[] whereFields = new String[]{ FIELD_DC_SERIES_NO, FIELD_DC_SOURCE };
        String[] whereValues = new String[]{ seriesNo, source.getOrigin() };
        list.loadDataForWhereFieldsAndValues(project, whereFields, whereValues, DBActionOrigin.FROM_DB, false, null);
        return list;
    }

    private void loadDataForWhereFieldsAndValues(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin,
                                                 boolean withHistoryData, String language) {
        clear(origin);

        String[] tempWhereFields;
        String[] tempWhereValues;

        if (withHistoryData) {
            tempWhereFields = whereFields;
            tempWhereValues = whereValues;
        } else {
            tempWhereFields = StrUtils.mergeArrays(whereFields, FIELD_DC_SDATB);
            tempWhereValues = StrUtils.mergeArrays(whereValues, "");
        }
        // PROVAL und MBS Code sollen im Moment nicht ausgegeben werden
        // Check, ob das Source Feld vorkommt. Fall ja, dann wird nach einer expliziten Quelle gesucht. Falls nein,
        // dann muss das Laden von PROVAL und MBS Code fest verriegelt werden
        if ((OMIT_PROVAL_CODES || OMIT_MBS_CODES) && !Arrays.asList(tempWhereFields).contains(FIELD_DC_SOURCE)) {
            tempWhereFields = getWhereFields(null, OMIT_PROVAL_CODES, tempWhereFields);
            tempWhereValues = getWhereValues(null, OMIT_PROVAL_CODES, tempWhereValues);
        }
        String[] sortFields = new String[]{ FIELD_DC_CODE_ID, FIELD_DC_SDATA };

        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_CODE_ID, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_SERIES_NO, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_PGRP, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_SDATA, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_SDATB, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_CODE, FIELD_DC_DESC, true, false);
        displayFields.addFeld(displayField);

        if (!StrUtils.isValid(language)) {
            searchSortAndFillWithMultiLangValueForAllLanguages(project, displayFields, FIELD_DC_DESC, tempWhereFields, tempWhereValues,
                                                               false, sortFields, false);
        } else {
            searchSortAndFillWithMultiLangValues(project, language, displayFields, tempWhereFields, tempWhereValues,
                                                 false, sortFields, false);
        }
    }

    private void loadDataWithoutJoinForSeriesAndOrigin(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin, DBActionOrigin origin) {
        internalSearchSortAndFill(project, null, seriesNo, null, dataOrigin, false, LoadType.COMPLETE, origin);
    }

    /**
     * Liefert zu iPartsCodeDataId (codeId, seriesNo, productGroup) das passende {@link iPartsDataCode}
     * Falls beim ersten Durchlauf keine passende Code gefunden wurde, nochmal ohne Bauhreihe suchen
     *
     * @param project      EtkProject
     * @param dbLanguage   DBLanguage
     * @param codeId       Id der gesuchten Code
     * @param seriesNo     Bauhreihe der gesuchten Code
     * @param productGroup Produktgruppe der gesuchten Code
     * @param compareDate  Vergleichsdatum für Regel Datum-Ab-Code <= Vergleichsdatum < Datum-Bis-Code
     * @return Gefundene Code
     */
    public static iPartsDataCode getFittingDateTimeCodeWithAddSearch(EtkProject project, String dbLanguage, String codeId, String seriesNo,
                                                                     String productGroup, String compareDate) {
        iPartsDataCode codeData = iPartsDataCodeList.getFittingDateTimeCode(project, dbLanguage, codeId, seriesNo, productGroup,
                                                                            compareDate);
        // Die Baureihe kann bei den Codes auch manchmal fehlen, da der DIALOG Code-Importer die Baureihe nicht
        // kennt -> nochmal ohne Baureihe nach dem Code suchen
        if ((codeData == null) && !seriesNo.isEmpty()) {
            codeData = iPartsDataCodeList.getFittingDateTimeCode(project, dbLanguage, codeId, "", productGroup,
                                                                 compareDate);
            if (codeData != null) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_CODES, LogType.DEBUG, "Code \"" + codeId + "\" not found for series number \""
                                                                          + seriesNo + "\" and product group \""
                                                                          + productGroup + "\"). Fallback to code without series number.");
            }
        }
        return codeData;
    }

    /**
     * Liefert zu iPartsCodeDataId (codeId, seriesNo, productGroup) das passende {@link iPartsDataCode}
     * Regel Datum-Ab-Code <= Vergleichsdatum < Datum-Bis-Code.
     * Es wird taggenau verglichen
     *
     * @param project
     * @param dbLanguage
     * @param codeId
     * @param compareDate Vergleichsdatum im Format yyyyMMdd oder yyyyMMdd+Time
     * @return null wenn kein Treffer in Datenbank
     */
    public static iPartsDataCode getFittingDateTimeCode(EtkProject project, String dbLanguage, String codeId, String seriesNo,
                                                        String productGroup, String compareDate) {
        iPartsDataCodeList list = iPartsDataCodeList.loadDataProvalSpecial(project, dbLanguage, codeId, seriesNo, false);
        if (list.isEmpty()) {
            return null;
        }
        return calculateFittingDateTimeCode(list, compareDate, productGroup, true);
    }

    /**
     * Falls PROVAL-Code mitgeladen werden sollen, muss berücksichtigt werden, dass Baureihen nur 4 Zeichen lang sind
     * Ist sie länger muss sie gekürzt wreden. Die Suche wird nochmal angestoßen
     *
     * @param project    EtkProjekt
     * @param dbLanguage Datenbanksprache
     * @param codeId     Gesuchte CodeId
     * @param seriesNo   Gesuchte Baureihe
     * @param isAutoLike Wird mit Wildcard gesucht, muss like eingesetzt werden
     * @return Gefundene {@link iPartsDataCode}
     */
    public static iPartsDataCodeList loadDataProvalSpecial(EtkProject project, String dbLanguage, String codeId, String seriesNo, boolean isAutoLike) {
        iPartsDataCodeList list = new iPartsDataCodeList();
        // wenn die PROVAL-Benennungen nicht verwendet werden sollen, werden die PROVAL Datensätze gar nicht mitgeladen.
        list.loadData(project, dbLanguage, codeId, seriesNo, false, !SHOW_PROVAL_CODE_DESC, isAutoLike, DBActionOrigin.FROM_DB);

        // Bei Proval ist die Baureihennummer nur 4 Zeichen lang
        // Bei MAD kann sie länger sein. PROVAL nochmal mit der gekürzten Baureihennummer laden
        if ((seriesNo.length() > 4) && SHOW_PROVAL_CODE_DESC) {
            String seriesNoRightLength = seriesNo.substring(0, 4);
            iPartsDataCodeList provalListAggregate = new iPartsDataCodeList();
            provalListAggregate.loadData(project, dbLanguage, codeId, seriesNoRightLength, false, !SHOW_PROVAL_CODE_DESC, isAutoLike, DBActionOrigin.FROM_DB);
            list.addAll(provalListAggregate, DBActionOrigin.FROM_DB);
        }
        return list;
    }

    public static iPartsDataCode getFittingDateTimeCodeMBS(EtkProject project, String dbLanguage, String codeId, String compareDate) {
        if (StrUtils.isEmpty(compareDate)) {
            return null;
        }
        iPartsDataCodeList list = new iPartsDataCodeList();
        // Suche nach MBS-Codes
        list.loadDataWithOrigin(project, dbLanguage, codeId, null, false, iPartsImportDataOrigin.SAP_MBS, DBActionOrigin.FROM_DB);
        iPartsDataCode dataCode = calcFittingDateTimeCodeWithDescription(list, compareDate);
        if (dataCode != null) {
            return dataCode;
        }
        CodeHelper.CodeWithPrefix codeKey = CodeHelper.createCodeInfoFromOriginalCode(codeId);
        if (codeKey.isCodeWithValidPrefix()) {
            list.loadDataWithOrigin(project, dbLanguage, codeKey.getCode(), codeKey.getProductGroup(), false, iPartsImportDataOrigin.SAP_MBS, DBActionOrigin.FROM_DB);
            dataCode = calcFittingDateTimeCodeWithDescription(list, compareDate);
            if (dataCode != null) {
                // Hier den Code setzen, wie er in der Regel auftaucht
                dataCode.setFieldValue(FIELD_DC_CODE_ID, codeId, DBActionOrigin.FROM_DB);
                dataCode.updateIdFromPrimaryKeys();
                return dataCode;
            }
        }
        // Suche nach PROVAL-Codes
        list.loadDataWithOrigin(project, dbLanguage, codeId, null, false, iPartsImportDataOrigin.PROVAL, DBActionOrigin.FROM_DB);
        dataCode = calcFittingDateTimeCodeWithDescription(list, compareDate);
        if (dataCode != null) {
            return dataCode;
        }
        // Suche restliche Code
        String productGrp = null;
        if (codeKey.isCodeWithValidPrefix()) {
            productGrp = codeKey.getProductGroup();
        }
        list.loadDataWithOrigin(project, dbLanguage, codeId, productGrp, false, null, DBActionOrigin.FROM_DB);
        return calcFittingDateTimeCodeWithDescription(list, compareDate);
    }

    /**
     * @param list
     * @param compareDate
     * @param productGroup
     * @param withDescription
     * @return
     */
    public static iPartsDataCode calculateFittingDateTimeCode(iPartsDataCodeList list, String compareDate, String productGroup,
                                                              boolean withDescription) {
        if (list.isEmpty() || !StrUtils.isValid(compareDate)) {
            return null;
        }

        // iPartsDataCodeList in einfache Java-Objekte zerlegen, damit wir leichter damit arbeiten und testen können
        // (Ich kann keine Unittests machen für Methoden die auf EtkDataObjects gehen, ohne mit AbstractTestEtkProject
        // ein aufwändiges Testprojekt zu erstellen)
        List<CodeRule> codeRulesNonProval = new ArrayList<>();
        List<CodeRule> codeRulesProval = new ArrayList<>();
        for (iPartsDataCode dataCode : list) {
            iPartsCodeDataId dataCodeId = dataCode.getAsId();
            String toDate = dataCode.getFieldValue(iPartsConst.FIELD_DC_SDATB);
            EtkMultiSprache desc = new EtkMultiSprache();
            if (withDescription) {
                desc = dataCode.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DC_DESC);
            }
            CodeRule codeRule = new CodeRule(dataCodeId, toDate, desc);
            iPartsImportDataOrigin source = dataCodeId.getSource();
            if (source == iPartsImportDataOrigin.PROVAL) {
                codeRulesProval.add(codeRule);
            } else {
                // PROVAL Code haben keine productGroup, andere Code müssen allerdings zur Produktgruppe passen.
                if (dataCodeId.getProductGroup().equals(productGroup)) {
                    codeRulesNonProval.add(codeRule);
                }
            }
        }

        CodeRule codeRule;
        if (SHOW_PROVAL_CODE_DESC && !codeRulesProval.isEmpty()) {
            codeRule = getMatchingCodeRule(codeRulesProval, compareDate);
        } else {
            codeRule = getMatchingCodeRule(codeRulesNonProval, compareDate);
        }

        if (codeRule != null) {
            // gefundene Code-Regel in der iPartsDataCodeList suchen, weil wir beim Aufrufer ein iPartsDataCode brauchen
            return list.getById(codeRule.getCodeDataId());
        } else {
            return null;
        }
    }

    public static iPartsDataCode calcFittingDateTimeCodeWithDescription(iPartsDataCodeList list, String compareDate) {
        return calcFittingDateTimeCode(list, compareDate, true, null);

    }

    public static iPartsDataCode calcFittingDateTimeCode(iPartsDataCodeList list, String compareDate,
                                                         boolean withDescription, iPartsImportDataOrigin codeOrigin) {
        if (list.isEmpty() || !StrUtils.isValid(compareDate)) {
            return null;
        }

        List<CodeRule> codeRules = new DwList<>(list.size());
        list.forEach((dataCode) -> {
            iPartsCodeDataId dataCodeId = dataCode.getAsId();
            if ((codeOrigin == null) || (codeOrigin == dataCodeId.getSource())) {
                String toDate = dataCode.getFieldValue(iPartsConst.FIELD_DC_SDATB);
                EtkMultiSprache desc = new EtkMultiSprache();
                if (withDescription) {
                    desc = dataCode.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DC_DESC);
                }
                CodeRule codeRule = new CodeRule(dataCodeId, toDate, desc);
                codeRules.add(codeRule);
            }
        });
        CodeRule codeRule = null;
        if (!codeRules.isEmpty()) {
            codeRule = getMatchingCodeRule(codeRules, compareDate);
        }
        if (codeRule != null) {
            // gefundene Code-Regel in der iPartsDataCodeList suchen, weil wir beim Aufrufer ein iPartsDataCode brauchen
            return list.getById(codeRule.getCodeDataId());
        } else {
            return null;
        }
    }

    /**
     * Filtert die passende Code-Regel zu einem Vergleichsdatum
     * Regel Datum-Ab-Code <= Vergleichsdatum < Datum-Bis-Code.
     * Es wird taggenau verglichen
     *
     * @param codeRules
     * @param compareDate
     * @return
     */
    public static CodeRule getMatchingCodeRule(List<CodeRule> codeRules, String compareDate) {
        CodeRule codeRuleWithMinimumFromDate = new CodeRule(new iPartsCodeDataId("", "", "", "99999999", iPartsImportDataOrigin.UNKNOWN), "", new EtkMultiSprache());
        if ((compareDate == null) || compareDate.isEmpty()) { // bei leerem Vergleichsdatum das aktuelle Datum nehmen
            compareDate = DateUtils.toyyyyMMdd_currentDate();
        } else if (compareDate.length() > 8) {
            compareDate = compareDate.substring(0, 8);
        }

        for (CodeRule codeRule : codeRules) {
            String fromDateTime = codeRule.getCodeDataId().getSdata();
            String toDateTime = codeRule.getToDate();

            if (fromDateTime.length() > 8) {
                fromDateTime = fromDateTime.substring(0, 8);
            }
            if (fromDateTime.compareTo(codeRuleWithMinimumFromDate.getCodeDataId().getSdata()) < 0) {
                codeRuleWithMinimumFromDate = codeRule;
            }
            if (toDateTime.length() > 8) {
                toDateTime = toDateTime.substring(0, 8);
            } else if (toDateTime.isEmpty()) {
                toDateTime = "99999999"; // unendlich
            }
            if ((fromDateTime.compareTo(compareDate) <= 0) && (compareDate.compareTo(toDateTime) < 0)) {
                return codeRule;
            }
        }

        // wenn keine Regel passt nehmen wir laut Hr. Müller den mit dem kleinstem von-Datum
        return codeRuleWithMinimumFromDate;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataCode)s für die übergebenen Parameter.
     *
     * @param project         EtkProjekt
     * @param dbLanguage      Datenbanksprache
     * @param codeid          Gesuchte CodeId
     * @param seriesNo        Gesuchte Baureihennummer
     * @param sorted          Soll sortiert werden?
     * @param omitProvalCodes Sollen PROVAL-Code mitgeladen werden?
     * @param isAutoLike      Muss mit like gesucht werden?
     * @param origin
     */
    public void loadData(EtkProject project, String dbLanguage, String codeId, String seriesNo, boolean sorted,
                         boolean omitProvalCodes, boolean isAutoLike, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = getWhereFields(null, omitProvalCodes, FIELD_DC_CODE_ID, FIELD_DC_SERIES_NO);
        String[] whereValues = getWhereValues(null, omitProvalCodes, codeId, seriesNo);
        String[] sortFields = null;
        if (sorted) {
            sortFields = new String[]{ FIELD_DC_SDATA };
        }
        searchSortAndFillWithJoin(project, dbLanguage, null, whereFields, whereValues, false, sortFields, false, isAutoLike, null);
    }

    public void loadDataWithOrigin(EtkProject project, String dbLanguage, String codeId, String productGrp, boolean sorted, iPartsImportDataOrigin codeOrigin, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields;
        String[] whereValues;
        // Wurde eine Quelle angegeben, dann nur Code zur Quelle ausgeben. Ansonsten alle Code ausgeben
        if ((codeOrigin != null) && (codeOrigin != iPartsImportDataOrigin.UNKNOWN)) {
            whereFields = new String[]{ FIELD_DC_CODE_ID, FIELD_DC_SOURCE };
            whereValues = new String[]{ codeId, codeOrigin.getOrigin() };
        } else {
            whereFields = getWhereFields(null, OMIT_PROVAL_CODES, FIELD_DC_CODE_ID);
            whereValues = getWhereValues(null, OMIT_PROVAL_CODES, codeId);
        }
        if (StrUtils.isValid(productGrp)) {
            whereFields = mergeArrays(whereFields, new String[]{ FIELD_DC_PGRP });
            whereValues = mergeArrays(whereValues, new String[]{ productGrp });
        }
        String[] sortFields = null;
        if (sorted) {
            sortFields = new String[]{ FIELD_DC_SDATA };
        }
        searchSortAndFillWithMultiLangValues(project, dbLanguage, null, whereFields, whereValues, false, sortFields, false);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataCode)s für die übergebenen Parameter.
     *
     * @param project
     * @param codeId
     * @param seriesNo
     * @param productGroup
     * @param sorted
     * @param dataOrigin
     * @param origin
     */
    public void loadDataWithoutJoin(EtkProject project, String codeId, String seriesNo, String productGroup,
                                    iPartsImportDataOrigin dataOrigin, boolean sorted, DBActionOrigin origin) {
        internalSearchSortAndFill(project, codeId, seriesNo, productGroup, dataOrigin, sorted, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataCode)s für die übergebenen Parameter.
     *
     * @param project
     * @param codeId
     * @param productGroup
     * @param dataOrigin
     * @param sorted
     * @param loadType
     * @param origin
     */
    public void loadDataWithoutJoin(EtkProject project, String codeId, String productGroup,
                                    iPartsImportDataOrigin dataOrigin, boolean sorted, LoadType loadType, DBActionOrigin origin) {
        internalSearchSortAndFill(project, codeId, null, productGroup, dataOrigin, sorted, loadType, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataCode)s für die übergebenen Parameter.
     * Ist {@link iPartsCodeDataId#getSource()} nicht {@link iPartsImportDataOrigin#UNKNOWN},
     * so wird die Suche auf diesen Source eingeschräkt.
     *
     * @param project
     * @param codeId
     * @param seriesNo
     * @param productGroup
     * @param dataOrigin
     * @param sorted
     * @param loadType
     * @param origin
     */
    private void internalSearchSortAndFill(EtkProject project, String codeId, String seriesNo, String productGroup,
                                           iPartsImportDataOrigin dataOrigin,
                                           boolean sorted, LoadType loadType, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = null;
        String[] whereValues = null;
        String[] whereNotFields = null;
        String[] whereNotValues = null;
        String[] sortFields = null;

        if (codeId != null) {
            whereFields = mergeArrays(whereFields, FIELD_DC_CODE_ID);
            whereValues = mergeArrays(whereValues, codeId);
        }
        if (seriesNo != null) {
            whereFields = mergeArrays(whereFields, FIELD_DC_SERIES_NO);
            whereValues = mergeArrays(whereValues, seriesNo);
        }
        if (productGroup != null) {
            whereFields = mergeArrays(whereFields, FIELD_DC_PGRP);
            whereValues = mergeArrays(whereValues, productGroup);
        }

        if ((dataOrigin != null) && (dataOrigin != iPartsImportDataOrigin.UNKNOWN)) {
            whereFields = mergeArrays(whereFields, FIELD_DC_SOURCE);
            whereValues = mergeArrays(whereValues, dataOrigin.getOrigin());
        } else {
            if (OMIT_PROVAL_CODES) {
                whereNotFields = mergeArrays(whereNotFields, FIELD_DC_SOURCE);
                whereNotValues = mergeArrays(whereNotValues, iPartsImportDataOrigin.PROVAL.getOrigin());
            }
            if (OMIT_MBS_CODES) {
                whereNotFields = mergeArrays(whereNotFields, FIELD_DC_SOURCE);
                whereNotValues = mergeArrays(whereNotValues, iPartsImportDataOrigin.SAP_MBS.getOrigin());
            }
        }
        if (sorted) {
            sortFields = new String[]{ FIELD_DC_SDATA };
        }

        searchSortAndFill(project, TABLE_DA_CODE, whereFields, whereValues,
                          whereNotFields, whereNotValues, sortFields, loadType, origin);
    }

    private String[] mergeArrays(String[] array, String... values) {
        if (array == null) {
            if (values == null) {
                return null;
            } else {
                array = new String[]{};
            }
        } else if (values == null) {
            return array;
        }
        return StrUtils.mergeArrays(array, values);
    }

    @Override
    protected iPartsDataCode getNewDataObject(EtkProject project) {
        return new iPartsDataCode(project, null);
    }
}
