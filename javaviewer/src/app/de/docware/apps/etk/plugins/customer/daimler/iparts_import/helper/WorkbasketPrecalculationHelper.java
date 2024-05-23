/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket.iPartsDataWorkBasketCalc;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket.iPartsDataWorkBasketCalcList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket.iPartsWorkBasketSaaCalcId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelElementUsageList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCTTHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLParameterList;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper für die Vorverdichtung der SAA-Arbeitsvorräte über Testmenüpunkte
 */
public class WorkbasketPrecalculationHelper implements iPartsConst {

    /**
     * Die eigentliche Vorverdichtung entweder für MBS, EDS oder CTT - SAAs in den Tabellen DA_STRUCTURE_MBS und DA_EDS_MODEL/DA_MODEL_ELEMENT_USAGE.
     *
     * @param project
     * @param messageLogForm
     * @param cancelVarParam
     * @param dataSource
     */
    public static void doCalculateSingleWorkbasket(EtkProject project, EtkMessageLogForm messageLogForm, VarParam<Boolean> cancelVarParam,
                                                   iPartsImportDataOrigin dataSource) {
        WorkbasketPrecalculationHelper helper = new WorkbasketPrecalculationHelper(project, messageLogForm, dataSource, cancelVarParam);
        helper.doCalculateSingleWorkbasket();
    }

    public static void doCalculateSingleWorkbasket(EtkProject project, LogChannels logChannel, VarParam<Boolean> cancelVarParam,
                                                   iPartsImportDataOrigin dataSource, boolean deleteData) {
        WorkbasketPrecalculationHelper helper = new WorkbasketPrecalculationHelper(project, logChannel, dataSource, cancelVarParam, deleteData);
        helper.doCalculateSingleWorkbasket();
    }

    /**
     * Die eigentliche Vorverdichtung der MBS + EDS + CTT - SAAs in den Tabellen DA_STRUCTURE_MBS und DA_EDS_MODEL/DA_MODEL_ELEMENT_USAGE.
     *
     * @param project
     * @param messageLogForm
     * @param cancelVarParam
     */
    public static void doCalculateAllWorkbaskets(EtkProject project, EtkMessageLogForm messageLogForm, VarParam<Boolean> cancelVarParam) {
        WorkbasketPrecalculationHelper helper = new WorkbasketPrecalculationHelper(project, messageLogForm, null, cancelVarParam);
        helper.doCalculateAllWorkbaskets();
    }

    public static String getDisplayname(iPartsImportDataOrigin source) {
        if (source.equals(iPartsImportDataOrigin.EDS)) {
            return "EDS";
        } else if (source.equals(iPartsImportDataOrigin.SAP_MBS)) {
            return "MBS";
        } else if (source.equals(iPartsImportDataOrigin.SAP_CTT)) {
            return "CTT";
        }
        return null;
    }

    private EtkProject project;
    private EtkMessageLogForm messageLogForm;
    private LogChannels logChannel;
    private VarParam<Boolean> cancelVarParam;
    private iPartsImportDataOrigin dataSource;
    private Boolean deleteData;

    public WorkbasketPrecalculationHelper(EtkProject project, EtkMessageLogForm messageLogForm, iPartsImportDataOrigin source, VarParam<Boolean> cancelVarParam) {
        this.project = project;
        this.messageLogForm = messageLogForm;
        this.logChannel = null;
        this.dataSource = source;
        this.cancelVarParam = cancelVarParam;
        this.deleteData = null;
    }

    public WorkbasketPrecalculationHelper(EtkProject project, LogChannels logChannel, iPartsImportDataOrigin source, VarParam<Boolean> cancelVarParam,
                                          boolean deleteData) {
        this(project, (EtkMessageLogForm)null, source, cancelVarParam);
        this.logChannel = logChannel;
        this.deleteData = deleteData;
    }

    private boolean checkSource() {
        if (dataSource == null) {
            fireMsg("!!Ungültige Quelle");
            return false;
        } else if (!dataSource.equals(iPartsImportDataOrigin.EDS) && !dataSource.equals(iPartsImportDataOrigin.SAP_MBS) && !dataSource.equals(iPartsImportDataOrigin.SAP_CTT)) {
            fireMsg("!!Ungültige Quelle %1.", dataSource.getOrigin());
            return false;
        }
        return true;
    }

    private int doDeleteWorkbasketCalculation(DBBase db) {
        int allRows = db.getRecordCount(TABLE_DA_WB_SAA_CALCULATION);
        String[] whereFields = new String[]{ FIELD_WSC_SOURCE };
        String[] whereValues = new String[]{ dataSource.getOrigin() };
        db.delete(TABLE_DA_WB_SAA_CALCULATION, whereFields, whereValues);
        int rowsAfterDelete = db.getRecordCount(TABLE_DA_WB_SAA_CALCULATION);
        return allRows - rowsAfterDelete;
    }

    private void doAskForDelete(DBBase db) {
        if (messageLogForm != null) {
            if (dataSource == null) {
                // Die Tabelle vorher komplett leeren, falls gewünscht.
                if (MessageDialog.showYesNo("!!Möchten Sie die vorhandenen Daten löschen?") == ModalResult.YES) {
                    int rowsToDelete = db.getRecordCount(TABLE_DA_WB_SAA_CALCULATION);
                    db.delete(TABLE_DA_WB_SAA_CALCULATION);
                    fireMsg("!!%1 Datensätze wurden gelöscht.", String.valueOf(rowsToDelete));
                }
            } else {
                // Die passenden Einträge aus der Tabelle löschen, ohne Batch Statement, damit gezählt werden kann
                if (MessageDialog.showYesNo(TranslationHandler.translate("!!Möchten Sie die vorhandenen Daten zur Quelle %1 löschen?", getDisplayname(dataSource))) == ModalResult.YES) {
                    int deletedRows = doDeleteWorkbasketCalculation(db);
                    fireMsg("!!%1 Datensätze wurden gelöscht.", String.valueOf(deletedRows));
                }
            }
        } else if (logChannel != null) {
            if ((deleteData != null) && deleteData) {
                if (dataSource == null) {
                    int rowsToDelete = db.getRecordCount(TABLE_DA_WB_SAA_CALCULATION);
                    db.delete(TABLE_DA_WB_SAA_CALCULATION);
                    fireMsg("!!%1 Datensätze wurden gelöscht.", String.valueOf(rowsToDelete));
                } else {
                    int deletedRows = doDeleteWorkbasketCalculation(db);
                    fireMsg("!!%1 Datensätze wurden gelöscht.", String.valueOf(deletedRows));
                }
            }
        }
    }

    public void doCalculateSingleWorkbasket() {
        if (!checkSource()) {
            return;
        }

        DBBase db = project.getDB();
        // Die DB potenziell auf einen Rollback vorbereiten.
        db.startTransaction();

        try {
            // Die passenden Einträge aus der Tabelle löschen, ohne Batch Statement, damit gezählt werden kann
            doAskForDelete(db);

            fireMsg("!!Start der Vorverdichtung.");

            iPartsEdsStructureHelper edsStructureHelper = null;

            db.startBatchStatement();
            // 1. Die DISTINCT-Listen der Baumuster-SAA-Kombinationen holen.
            List<iPartsWorkBasketSaaCalcId> calculationList;
            if (dataSource.equals(iPartsImportDataOrigin.EDS)) {
                edsStructureHelper = iPartsEdsStructureHelper.getInstance();
                if (edsStructureHelper.isNewStructureActive()) {
                    fireMsg("!!Verarbeitung für TB.f.");
                } else {
                    fireMsg("!!Verarbeitung für BOM-DB.");
                }
                calculationList = loadDistinctEdsModelSAAs(edsStructureHelper);
            } else if (dataSource.equals(iPartsImportDataOrigin.SAP_MBS)) {
                calculationList = loadDistinctMbsModelSAAs();
            } else { // dataSource.equals(iPartsImportDataOrigin.SAP_CTT)
                calculationList = loadDistinctCttModelSAAs();
            }

            fireMsg("!!%1 %2-Baumuster-SAAs ermittelt.", String.valueOf(calculationList.size()), getDisplayname(dataSource));

            int max = calculationList.size();
            int totalCount = 0;
            fireMsg("!!%1 zu betrachtende Datensätze.", String.valueOf(max));
            if (max > 0) {
                boolean calculationResult;
                if (dataSource.equals(iPartsImportDataOrigin.EDS)) {
                    calculationResult = doCalculateEDS(calculationList, edsStructureHelper, totalCount, max);
                } else if (dataSource.equals(iPartsImportDataOrigin.SAP_MBS)) {
                    calculationResult = doCalculateMBS(calculationList, totalCount, max);
                } else { //dataSource.equals(iPartsImportDataOrigin.SAP_CTT)
                    calculationResult = doCalculateCTT(calculationList, totalCount, max);
                }

                if (!calculationResult) {
                    db.cancelBatchStatement();
                    db.rollback();
                    return;
                }
            }
            // Die Endenachricht absetzen
            fireProgress(100, 100);
            // In der Datenbank verankern.
            db.endBatchStatement();
            db.commit();

            fireMsgWithSeparators("!!Ende der Vorverdichtung.", String.valueOf(max));
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireException(e);

            // Bei Fehlern die Rolle rückwärts machen
            db.cancelBatchStatement();
            db.rollback();
        }
    }

    public void doCalculateAllWorkbaskets() {
        DBBase db = project.getDB();
        // Die DB potentiell auf einen Rollback vorbereiten.
        db.startTransaction();
        db.startBatchStatement();

        try {
            // Die Tabelle vorher komplett leeren, falls gewünscht.
            doAskForDelete(db);

            fireMsg("!!Start der Vorverdichtung.");

            // 1. Die DISTINCT-Listen der Baumuster-SAA-Kombinationen holen.
            iPartsEdsStructureHelper edsStructureHelper = iPartsEdsStructureHelper.getInstance();
            if (edsStructureHelper.isNewStructureActive()) {
                fireMsg("!!Verarbeitung für TB.f.");
            } else {
                fireMsg("!!Verarbeitung für BOM-DB.");
            }
            List<iPartsWorkBasketSaaCalcId> mbsList = loadDistinctMbsModelSAAs();
            fireMsg("!!%1 MBS-Baumuster-SAAs ermittelt.", String.valueOf(mbsList.size()));

            List<iPartsWorkBasketSaaCalcId> edsList = loadDistinctEdsModelSAAs(edsStructureHelper);
            fireMsg("!!%1 EDS-Baumuster-SAAs ermittelt.", String.valueOf(edsList.size()));

            List<iPartsWorkBasketSaaCalcId> cttList = loadDistinctCttModelSAAs();
            fireMsg("!!%1 CTT-Baumuster-SAAs ermittelt.", String.valueOf(cttList.size()));

            // 2. Die Listen in einer Schleife mit Fortschrittsdialog abarbeiten, falls etwas gefunden wurde.
            // *** for DEBUG only
//            mbsList.clear();
//            edsList.clear();
            // *** for DEBUG only
            int max = mbsList.size() + edsList.size() + cttList.size();
            int totalCount = 0;
            fireMsg("!!%1 zu betrachtende Datensätze.", String.valueOf(max));
            if (max > 0) {
                // Die MBS-Daten (aus DA_STRUCTURE_MBS) vorverdichten:
                if (!doCalculateMBS(mbsList, totalCount, max)) {
                    db.cancelBatchStatement();
                    db.rollback();
                    return;
                }

                // Die EDS-Daten (aus DA_EDS_MODEL/DA_MODEL_ELEMENT_USAGE) vorverdichten:
                if (!doCalculateEDS(edsList, edsStructureHelper, totalCount, max)) {
                    db.cancelBatchStatement();
                    db.rollback();
                    return;
                }

                // Die CTT-Daten (aus DA_EDS_CONST_KIT) vorverdichten:
                if (!doCalculateCTT(cttList, totalCount, max)) {
                    db.cancelBatchStatement();
                    db.rollback();
                    return;
                }
            }
            // Die Endenachricht absetzen
            fireProgress(100, 100);
            fireMsg("!!%1 Datensätze wurden bearbeitet", String.valueOf(max));
            // In der Datenbank verankern.
            db.endBatchStatement();
            db.commit();

            fireMsgWithSeparators("!!Ende der Vorverdichtung.", String.valueOf(max));
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireException(e);

            // Bei Fehlern die Rolle rückwärts machen
            db.cancelBatchStatement();
            db.rollback();
        }
    }

    /**
     * Lädt eine UNIQUE-Liste von Baumuster und SAA-Kombinationen aus MBS (TABLE_DA_STRUCTURE_MBS)
     *
     * @return
     */
    private List<iPartsWorkBasketSaaCalcId> loadDistinctMbsModelSAAs() {
        // Immer eine Ergebnisliste zurückgeben
        List<iPartsWorkBasketSaaCalcId> list = new DwList<>();

        DBSQLQuery query = project.getDB().getDBForDomain(DBDatabaseDomain.MAIN).getNewQuery();
        SQLParameterList params = new SQLParameterList();

        List<String> resultFields = new ArrayList<>();
        resultFields.add(FIELD_DSM_SNR.toLowerCase());
        resultFields.add(FIELD_DSM_SUB_SNR.toLowerCase());

        // Nur Fahrzeug- und Aggregatebaumuster berücksichtigen.
        query.selectDistinct(new Fields(resultFields)).
                from(new Tables(TABLE_DA_STRUCTURE_MBS.toLowerCase())).
                where(new Condition(FIELD_DSM_SNR.toLowerCase(), Condition.OPERATOR_LIKE, iPartsConst.MODEL_NUMBER_PREFIX_CAR + "%")).
                or(new Condition(FIELD_DSM_SNR.toLowerCase(), Condition.OPERATOR_LIKE, iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE + "%"));
        query.orderBy(ArrayUtil.toStringArray(resultFields));

        DBDataSet dbSet = query.executeQuery(params);

        while (dbSet.next()) {
            EtkRecord rec = dbSet.getRecord(new String[]{ FIELD_DSM_SNR, FIELD_DSM_SUB_SNR });
            String modelNo = rec.getField(FIELD_DSM_SNR).getAsString();
            String saaNo = rec.getField(FIELD_DSM_SUB_SNR).getAsString();
            // saaNo ist leer bei Texten und "C*", "D*", "G*" überspringen.
            if ((!StrUtils.isValid(modelNo, saaNo))
                || saaNo.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR)
                || saaNo.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_AGGREGATE)
                || saaNo.startsWith(iPartsConst.BASE_LIST_NUMBER_PREFIX)) {
                continue;
            }
            list.add(new iPartsWorkBasketSaaCalcId(iPartsImportDataOrigin.SAP_MBS.getOrigin(), modelNo, saaNo));
        }
        dbSet.close();

        return list;
    }

    /**
     * Lädt eine UNIQUE-Liste von Baumuster und SAA-Kombinationen aus EDS (TABLE_DA_EDS_MODEL/DA_MODEL_ELEMENT_USAGE)
     *
     * @return
     */
    private List<iPartsWorkBasketSaaCalcId> loadDistinctEdsModelSAAs(iPartsEdsStructureHelper edsStructureHelper) {
        // Immer eine Ergebnisliste zurückgeben
        List<iPartsWorkBasketSaaCalcId> list = new DwList<>();

        DBSQLQuery query = project.getDB().getDBForDomain(DBDatabaseDomain.MAIN).getNewQuery();
        SQLParameterList params = new SQLParameterList();

        List<String> resultFields = new ArrayList<>();
        resultFields.add(edsStructureHelper.getModelNumberField().toLowerCase());
        resultFields.add(edsStructureHelper.getSubElementField().toLowerCase());

        query.selectDistinct(new Fields(resultFields)).from(new Tables(edsStructureHelper.getStructureTableName().toLowerCase()));
        query.orderBy(ArrayUtil.toStringArray(resultFields));

        DBDataSet dbSet = query.executeQuery(params);

        while (dbSet.next()) {
            EtkRecord rec = dbSet.getRecord(new String[]{ edsStructureHelper.getModelNumberField(), edsStructureHelper.getSubElementField() });
            String modelNo = rec.getField(edsStructureHelper.getModelNumberField()).getAsString();
            String saaNo = rec.getField(edsStructureHelper.getSubElementField()).getAsString();
            // Ggf. möglich leere saaNo auch hier überspringen.
            if (StrUtils.isValid(modelNo, saaNo)) {
                list.add(new iPartsWorkBasketSaaCalcId(iPartsImportDataOrigin.EDS.getOrigin(), modelNo, saaNo));
            }
        }
        dbSet.close();

        return list;
    }

    /**
     * Lädt eine UNIQUE-Liste von Baumuster und SAA-Kombinationen aus EDS (TABLE_DA_EDS_MODEL)
     *
     * @return
     */
    private List<iPartsWorkBasketSaaCalcId> loadDistinctCttModelSAAs() {
        // Immer eine Ergebnisliste zurückgeben
        List<iPartsWorkBasketSaaCalcId> list = new DwList<>();

        DBSQLQuery query = project.getDB().getDBForDomain(DBDatabaseDomain.MAIN).getNewQuery();
        SQLParameterList params = new SQLParameterList();

        List<String> resultFields = new ArrayList<>();
        resultFields.add(FIELD_DA_ESM_MODEL_NO.toLowerCase());
        resultFields.add(FIELD_DA_ESM_SAA_NO.toLowerCase());
        resultFields.add(FIELD_DHSM_HMO.toLowerCase());
        resultFields.add(FIELD_DHSM_SAA.toLowerCase());

        List<AbstractCondition> conditionHmo = new DwList<>();
        conditionHmo.add(new Condition(TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_SAA),
                                       Condition.OPERATOR_EQUALS,
                                       new Fields(TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO))));
        List<AbstractCondition> conditionContent = new DwList<>();
        conditionContent.add(new Condition(TableAndFieldName.make(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_HMO),
                                           Condition.OPERATOR_EQUALS,
                                           new Fields(TableAndFieldName.make(TABLE_DA_EDS_CONST_KIT, FIELD_DCK_SNR))));

        query.selectDistinct(new Fields(resultFields)).
                from(new Tables(TABLE_DA_HMO_SAA_MAPPING)).
                join(new LeftOuterJoin(TABLE_DA_EDS_SAA_MODELS, new ConditionList(conditionHmo, false))).
                join(new InnerJoin(TABLE_DA_EDS_CONST_KIT, new ConditionList(conditionContent, false))
                );

        query.orderBy(ArrayUtil.toStringArray(resultFields));

        DBDataSet dbSet = query.executeQuery(params);

        while (dbSet.next()) {
            EtkRecord rec = dbSet.getRecord(new String[]{ FIELD_DA_ESM_MODEL_NO, FIELD_DA_ESM_SAA_NO, FIELD_DHSM_HMO, FIELD_DHSM_SAA });
            String modelNo = rec.getField(FIELD_DA_ESM_MODEL_NO).getAsString();
            if (StrUtils.isEmpty(modelNo)) {
                modelNo = "";
            }
            String saaNo = rec.getField(FIELD_DHSM_SAA).getAsString();
            // Leere SAAs in DA_EDS_SAA_MODELS zulassen, damit der Geschäftsfall NR abgebildet werden kann
            if (StrUtils.isEmpty(modelNo) || iPartsModel.isVehicleModel(modelNo) || iPartsModel.isAggregateModel(modelNo)) {
                list.add(new iPartsWorkBasketSaaCalcId(iPartsImportDataOrigin.SAP_CTT.getOrigin(), modelNo, saaNo));
            }
        }
        dbSet.close();

        return list;
    }


    private boolean doCalculateMBS(List<iPartsWorkBasketSaaCalcId> mbsList,
                                   int totalCount, int max) {
        for (iPartsWorkBasketSaaCalcId saaCalcId : mbsList) {
            fireProgress(totalCount++, max);
            if (cancelVarParam.getValue()) {
                return false;
            }

            // Jetzt zu jeder MBS-SAA eines Baumusters die MIN/MAX-RELEASE-FROM/TO berechnen.
            iPartsDataWorkBasketCalc calculatedMBSItem = computeMBSReleaseDates(saaCalcId);
            if (calculatedMBSItem != null) {
                calculatedMBSItem.saveToDB();
            }
            showDataRowCounter(totalCount, max);
        }
        return true;
    }

    private boolean doCalculateEDS(List<iPartsWorkBasketSaaCalcId> edsList, iPartsEdsStructureHelper edsStructureHelper,
                                   int totalCount, int max) {
        for (iPartsWorkBasketSaaCalcId saaCalcId : edsList) {
            fireProgress(totalCount++, max);
            if (cancelVarParam.getValue()) {
                return false;
            }

            // Jetzt zu jeder EDS-SAA eines Baumusters die MIN/MAX-RELEASE-FROM/TO berechnen.
            iPartsDataWorkBasketCalc calculatedEDSItem = computeEDSReleaseDates(saaCalcId, edsStructureHelper);
            if (calculatedEDSItem != null) {
                calculatedEDSItem.saveToDB();
            }
            showDataRowCounter(totalCount, max);
        }
        return true;
    }

    private boolean doCalculateCTT(List<iPartsWorkBasketSaaCalcId> cttList, int totalCount, int max) {
        iPartsDataWorkBasketCalcList wbCalcList = new iPartsDataWorkBasketCalcList();
        Map<String, List<String>> saaToHmoMapping = iPartsCTTHelper.getSaaToHmoMapping(project);
        for (iPartsWorkBasketSaaCalcId saaCalcId : cttList) {
            fireProgress(totalCount++, max);
            if (cancelVarParam.getValue()) {
                return false;
            }

            // Jetzt zu jeder CTT-SAA eines Baumusters die MIN/MAX-RELEASE-FROM/TO berechnen.
            iPartsDataWorkBasketCalc calculatedCTTItem = computeCTTReleaseDates(saaCalcId, saaToHmoMapping);
            if (calculatedCTTItem != null) {
                wbCalcList.add(calculatedCTTItem, DBActionOrigin.FROM_EDIT);
            }
            showDataRowCounter(totalCount, max);
        }
        if (!wbCalcList.isEmpty()) {
            wbCalcList.saveToDB(project);
        }
        return true;
    }

    /**
     * Funktion, die für MBS die Daten vorverdichtet.
     * Es wird das kleinste DSM_RELEASE_FROM und das größte DSM_RELEASE_TO ermittelt, in einem Objekt gespeichert und zurückgegeben.
     *
     * @param saaCalcId
     * @return
     */
    private iPartsDataWorkBasketCalc computeMBSReleaseDates(iPartsWorkBasketSaaCalcId saaCalcId) {
        // Die Liste aller SAAs zum Baumuster mit den Release-(from/to)-Werten laden
        iPartsDataMBSStructureList mbsModelSaaList = iPartsDataMBSStructureList.loadAllModelSaaEntries(project, saaCalcId.getModelNo(), saaCalcId.getSaa());

        // Nur, wenn die Liste auch Elemente enthält, ein Objekt zurückgeben, ansonsten wird <null> zurückgegeben.
        iPartsDataWorkBasketCalc calculatedItem = null;
        if (mbsModelSaaList.size() > 0) {
            // Initialisieren
            boolean infinityDateFromFoundForMin = false;
            boolean infinityDateFromFoundForMax = false;
            boolean infinityDateToFound = false;
            String minReleaseFrom = "9999";
            String maxReleaseFrom = "0000";
            String maxReleaseTo = "0000";
            String currentReleaseFrom;
            String currentReleaseTo;
            String code = "";

            // Über die Liste iterieren und sowohl ReleaseFrom als auch ReleaseTo überprüfen.
            for (iPartsDataMBSStructure mbsModelSaaItem : mbsModelSaaList) {

                // Kleinstes RELEASE_FROM finden
                if (!infinityDateFromFoundForMin) {
                    currentReleaseFrom = mbsModelSaaItem.getFieldValue(FIELD_DSM_RELEASE_FROM);
                    if (isInfinityDateFrom(currentReleaseFrom)) {
                        infinityDateFromFoundForMin = true;
                        minReleaseFrom = "";
                    } else if (currentReleaseFrom.compareTo(minReleaseFrom) < 0) {
                        minReleaseFrom = currentReleaseFrom;
                    }
                }

                // Größtes RELEASE_FROM finden
                if (!infinityDateFromFoundForMax) {
                    currentReleaseFrom = mbsModelSaaItem.getFieldValue(FIELD_DSM_RELEASE_FROM);
                    if (isInfinityDateFrom(currentReleaseFrom)) {
                        infinityDateFromFoundForMax = true;
                        code = mbsModelSaaItem.getFieldValue(FIELD_DSM_CODE);
                        maxReleaseFrom = "";
                    } else if (currentReleaseFrom.compareTo(maxReleaseFrom) > 0) {
                        code = mbsModelSaaItem.getFieldValue(FIELD_DSM_CODE);
                        maxReleaseFrom = currentReleaseFrom;
                    }
                }

                // Größtes RELEASE_TO finden
                if (!infinityDateToFound) {
                    currentReleaseTo = mbsModelSaaItem.getFieldValue(FIELD_DSM_RELEASE_TO);
                    if (isInfinityDateTo(currentReleaseTo)) {
                        infinityDateToFound = true;
                        maxReleaseTo = "";
                    } else if (currentReleaseTo.compareTo(maxReleaseTo) > 0) {
                        maxReleaseTo = currentReleaseTo;
                    }
                }
            }
            // DAIMLER-11363: Gültigkeitsdatum Ab = Bis bedeutet, das die entsprechenden SAAs bereits in SAP gelöscht wurden bzw. nie gebaut wurden
            // => ignorieren
            if (StrUtils.isValid(minReleaseFrom, maxReleaseTo) && minReleaseFrom.equals(maxReleaseTo)) {
                return null;
            }

            iPartsWorkBasketSaaCalcId id = new iPartsWorkBasketSaaCalcId(iPartsImportDataOrigin.SAP_MBS.getOrigin(), saaCalcId.getModelNo(), saaCalcId.getSaa());
            calculatedItem = new iPartsDataWorkBasketCalc(project, id);
            if (!calculatedItem.existsInDB()) {
                calculatedItem.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            // Die gefundenen Werte setzen
            calculatedItem.setMinReleasedFrom(minReleaseFrom, DBActionOrigin.FROM_EDIT);
            calculatedItem.setMaxReleasedTo(maxReleaseTo, DBActionOrigin.FROM_EDIT);
            calculatedItem.setCode(code, DBActionOrigin.FROM_EDIT);
            calculatedItem.setFactories("", DBActionOrigin.FROM_EDIT);
        }
        return calculatedItem;
    }

    /**
     * Funktion, die für EDS die Daten vorverdichtet.
     * Es wird das kleinste EDS_RELEASE_FROM und das größte EDS_RELEASE_TO ermittelt, in einem Objekt gespeichert und zurückgegeben.
     *
     * @param saaCalcId
     * @return
     */
    private iPartsDataWorkBasketCalc computeEDSReleaseDates(iPartsWorkBasketSaaCalcId saaCalcId, iPartsEdsStructureHelper edsStructureHelper) {
        // Die Liste aller SAAs zum Baumuster mit den Release-(from/to)-Werten laden
        EtkDataObjectList workList = new GenericEtkDataObjectList();
        if (edsStructureHelper.isNewStructureActive()) {
            workList.addAll(iPartsDataModelElementUsageList.loadAllModelSaaEntries(project, saaCalcId.getModelNo(), saaCalcId.getSaa()), DBActionOrigin.FROM_DB);
        } else {
            workList.addAll(iPartsDataEDSModelContentList.loadAllModelSaaEntries(project, saaCalcId.getModelNo(), saaCalcId.getSaa()), DBActionOrigin.FROM_DB);
        }

        // Nur, wenn die Liste auch Elemente enthält, ein Objekt zurückgeben, ansonsten wird <null> zurückgegeben.
        iPartsDataWorkBasketCalc calculatedItem = null;
        if (!workList.isEmpty()) {
            // Initialisieren
            boolean infinityDateFromFoundForMin = false;
            boolean infinityDateFromFoundForMax = false;
            boolean infinityDateToFound = false;
            String minReleaseFrom = "9999";
            String maxReleaseFrom = "0000";
            String maxReleaseTo = "0000";
            String currentReleaseFrom;
            String currentReleaseTo;
            String code = "";
            String factories = "";

            List<EtkDataObject> list = workList.getAsList();
            // Über die Liste iterieren und sowohl ReleaseFrom alsoauch ReleaseTo überprüfen.
            for (EtkDataObject edsModelSaaItem : list) {

                // Kleinstes RELEASE_FROM finden
                if (!infinityDateFromFoundForMin) {
                    currentReleaseFrom = edsModelSaaItem.getFieldValue(edsStructureHelper.getReleaseFromField());
                    if (isInfinityDateFrom(currentReleaseFrom)) {
                        infinityDateFromFoundForMin = true;
                        minReleaseFrom = "";
                    } else if (currentReleaseFrom.compareTo(minReleaseFrom) < 0) {
                        minReleaseFrom = currentReleaseFrom;
                    }
                }

                // Größtes RELEASE_FROM finden
                if (!infinityDateFromFoundForMax) {
                    currentReleaseFrom = edsModelSaaItem.getFieldValue(edsStructureHelper.getReleaseFromField());
                    if (isInfinityDateFrom(currentReleaseFrom)) {
                        infinityDateFromFoundForMax = true;
                        maxReleaseFrom = "";
                        code = edsModelSaaItem.getFieldValue(edsStructureHelper.getCodeField());
                        factories = edsModelSaaItem.getFieldValue(edsStructureHelper.getPlantSupplyField());
                    } else if (currentReleaseFrom.compareTo(maxReleaseFrom) > 0) {
                        maxReleaseFrom = currentReleaseFrom;
                        code = edsModelSaaItem.getFieldValue(edsStructureHelper.getCodeField());
                        factories = edsModelSaaItem.getFieldValue(edsStructureHelper.getPlantSupplyField());
                    }
                }

                // Größtes RELEASE_TO finden
                if (!infinityDateToFound) {
                    currentReleaseTo = edsModelSaaItem.getFieldValue(edsStructureHelper.getReleaseToField());
                    if (isInfinityDateTo(currentReleaseTo)) {
                        infinityDateToFound = true;
                        maxReleaseTo = "";
                    } else if (currentReleaseTo.compareTo(maxReleaseTo) > 0) {
                        maxReleaseTo = currentReleaseTo;
                    }
                }
            }
            iPartsWorkBasketSaaCalcId id = new iPartsWorkBasketSaaCalcId(iPartsImportDataOrigin.EDS.getOrigin(), saaCalcId.getModelNo(), saaCalcId.getSaa());
            calculatedItem = new iPartsDataWorkBasketCalc(project, id);
            if (!calculatedItem.existsInDB()) {
                calculatedItem.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            // Die gefundenen Werte setzen
            calculatedItem.setMinReleasedFrom(minReleaseFrom, DBActionOrigin.FROM_EDIT);
            calculatedItem.setMaxReleasedTo(maxReleaseTo, DBActionOrigin.FROM_EDIT);
            calculatedItem.setCode(code, DBActionOrigin.FROM_EDIT);
            calculatedItem.setFactories(factories, DBActionOrigin.FROM_EDIT);
        }
        return calculatedItem;
    }

    private iPartsDataWorkBasketCalc computeCTTReleaseDates(iPartsWorkBasketSaaCalcId saaCalcId,
                                                            Map<String, List<String>> saaToHmoMapping) {
        // Die Liste aller SAAs zum Baumuster mit den Release-(from/to)-Werten laden
        iPartsDataWorkBasketCalc calculatedItem = null;
        iPartsDataBOMConstKitContentList list = new iPartsDataBOMConstKitContentList();
        list.clear(DBActionOrigin.FROM_DB);

        List<String> hmoNumberList = saaToHmoMapping.get(saaCalcId.getSaa());
        if (Utils.isValid(hmoNumberList)) {
            String[] whereFields = new String[]{ FIELD_DCK_SNR };
            // todo ??
            String[] whereValues = new String[]{ hmoNumberList.get(0) };
            String[] sortFields = new String[]{ FIELD_DCK_REVFROM };
            list.searchSortAndFill(project, TABLE_DA_EDS_CONST_KIT, whereFields, whereValues, sortFields, DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
            if (!list.isEmpty()) {
                // Initialisieren
                boolean infinityDateFromFoundForMin = false;
                boolean infinityDateFromFoundForMax = false;
                boolean infinityDateToFound = false;
                String minReleaseFrom = "9999";
                String maxReleaseFrom = "0000";
                String maxReleaseTo = "0000";
                String currentReleaseFrom;
                String currentReleaseTo;
                String code = "";
                String factories = "";

                // Über die Liste iterieren und sowohl ReleaseFrom also auch ReleaseTo überprüfen.
                for (iPartsDataBOMConstKitContent edsModelSaaItem : list) {

                    // Kleinstes RELEASE_FROM finden
                    if (!infinityDateFromFoundForMin) {
                        currentReleaseFrom = edsModelSaaItem.getFieldValue(FIELD_DCK_RELEASE_FROM);
                        if (isInfinityDateFrom(currentReleaseFrom)) {
                            infinityDateFromFoundForMin = true;
                            minReleaseFrom = "";
                        } else if (currentReleaseFrom.compareTo(minReleaseFrom) < 0) {
                            minReleaseFrom = currentReleaseFrom;
                        }
                    }

                    // Größtes RELEASE_FROM finden
                    if (!infinityDateFromFoundForMax) {
                        currentReleaseFrom = edsModelSaaItem.getFieldValue(FIELD_DCK_RELEASE_FROM);
                        if (isInfinityDateFrom(currentReleaseFrom)) {
                            infinityDateFromFoundForMax = true;
                            maxReleaseFrom = "";
                            //code = edsModelSaaItem.getFieldValue(FIELD_EDS_MODEL_CODE);
                            factories = edsModelSaaItem.getFieldValue(FIELD_DCK_FACTORY_IDS);
                        } else if (currentReleaseFrom.compareTo(maxReleaseFrom) > 0) {
                            maxReleaseFrom = currentReleaseFrom;
//                            code = edsModelSaaItem.getFieldValue(FIELD_EDS_MODEL_CODE);
                            factories = edsModelSaaItem.getFieldValue(FIELD_DCK_FACTORY_IDS);
                        }
                    }

                    // Größtes RELEASE_TO finden
                    if (!infinityDateToFound) {
                        currentReleaseTo = edsModelSaaItem.getFieldValue(FIELD_DCK_RELEASE_TO);
                        if (isInfinityDateTo(currentReleaseTo)) {
                            infinityDateToFound = true;
                            maxReleaseTo = "";
                        } else if (currentReleaseTo.compareTo(maxReleaseTo) > 0) {
                            maxReleaseTo = currentReleaseTo;
                        }
                    }
                }
                calculatedItem = new iPartsDataWorkBasketCalc(project, saaCalcId);
                if (!calculatedItem.existsInDB()) {
                    calculatedItem.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                // Die gefundenen Werte setzen
                calculatedItem.setMinReleasedFrom(minReleaseFrom, DBActionOrigin.FROM_EDIT);
                calculatedItem.setMaxReleasedTo(maxReleaseTo, DBActionOrigin.FROM_EDIT);
                calculatedItem.setCode(code, DBActionOrigin.FROM_EDIT);
                calculatedItem.setFactories(factories, DBActionOrigin.FROM_EDIT);
            }
        }
        return calculatedItem;
    }

    /**
     * Ggf. die Datensatznummer ausgeben.
     *
     * @param currentRowNo
     * @param maxRowCount
     */
    private void showDataRowCounter(int currentRowNo, int maxRowCount) {
        if (((currentRowNo % 10000) == 0) || (currentRowNo == maxRowCount)) {
            fireMsg("!!%1 Datensätze wurden bearbeitet", String.valueOf(currentRowNo));
        }
    }


    private void fireMsg(String key, String... placeHolderTexts) {
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate(key, placeHolderTexts),
                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        } else if (logChannel != null) {
            Logger.log(logChannel, LogType.DEBUG, TranslationHandler.translateForLanguage(key, Language.EN.getCode(), placeHolderTexts));
        }
    }

    private void fireMsgWithSeparators(String key, String... placeHolderTexts) {
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireMessageWithSeparators(TranslationHandler.translate(key, placeHolderTexts),
                                                                     MessageLogOption.TIME_STAMP);
        } else if (logChannel != null) {
            Logger.log(logChannel, LogType.DEBUG, TranslationHandler.translateForLanguage(key, Language.EN.getCode(), placeHolderTexts));
        }
    }

    private void fireException(Throwable e) {
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireMessage(Logger.getLogger().exceptionToString(e), MessageLogType.tmlError);
        } else if (logChannel != null) {
            Logger.log(logChannel, LogType.ERROR, Logger.getLogger().exceptionToString(e));
        }
    }

    private void fireProgress(int pos, int maxPos) {
        if (messageLogForm != null) {
            messageLogForm.getMessageLog().fireProgress(pos, maxPos, "", true, true);
        }
    }

    /**
     * Für MBS und EDS werden die gleichen Werte für "gültig BIS unendlich" zugelassen.
     * dbDateTime.isEmpty() || dbDateTime.startsWith("99991231") || dbDateTime.startsWith("18991231")
     *
     * @param dateTime
     * @return
     */
    private static boolean isInfinityDateTo(String dateTime) {
        return iPartsEDSDateTimeHandler.isFinalStateDbDateTime(dateTime);
    }

    /**
     * Als "gültig AB minus unendlich" wird für MBS und EDS der gleiche Wert zugelassen.
     *
     * @param dateTime
     * @return
     */
    private static boolean isInfinityDateFrom(String dateTime) {
        return dateTime.isEmpty();
    }

}
