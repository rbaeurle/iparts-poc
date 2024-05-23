/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.terms.AbstractCondition;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.ConditionList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Liste mit {@link iPartsDataFactoryData} Objekten
 */
public class iPartsDataFactoryDataList extends EtkDataObjectList<iPartsDataFactoryData> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataFactoryData}s für den Import.
     *
     * @param project
     * @param seriesNumber
     * @return
     */
    public static iPartsDataFactoryDataList loadFactoryDataListForImport(EtkProject project, String seriesNumber) {

        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForImportFromDB(project, seriesNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataFactoryData}s aus der Konstruktion.
     *
     * @param project
     * @param dialogPosPrimaryKey Primärschlüssel der Stücklistenpositionsvariante in DIALOG
     * @param withHistoryData     wenn false, werden nur die Daten mit neustem ADAT pro DIALOG Schlüssel UND Status zurückgegeben
     * @return
     */
    public static iPartsDataFactoryDataList loadConstructionFactoryDataListForDialogPositionsVariant(EtkProject project, iPartsDialogBCTEPrimaryKey dialogPosPrimaryKey,
                                                                                                     boolean withHistoryData) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForPositionsVariantFromDB(project, dialogPosPrimaryKey, DBActionOrigin.FROM_DB,
                                                      iPartsFactoryDataTypes.FACTORY_DATA_CONSTRUCTION, withHistoryData);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataFactoryData}s zu der übergebenen Id, aber ignoriert das
     * Änderungsdatum und die Sequenznummer. D.h alle Änderungsstände werden geladen.
     *
     * @param project
     * @param factoryDataId
     * @return
     */
    public static iPartsDataFactoryDataList loadAfterSalesFactoryDataListWithHistoryData(EtkProject project, iPartsFactoryDataId factoryDataId) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        String[] whereFields = new String[]{ FIELD_DFD_GUID, FIELD_DFD_FACTORY, FIELD_DFD_SPKZ, FIELD_DFD_DATA_ID };
        String[] whereValues = new String[]{ factoryDataId.getGuid(), factoryDataId.getFactory(), factoryDataId.getSplitAttribute(),
                                             factoryDataId.getDataId() };
        list.loadFactoryDataInternSortByADAT(project, whereFields, whereValues, DBActionOrigin.FROM_DB);
        return list;
    }


    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataFactoryData}s aus dem After-Sales.
     *
     * @param project
     * @param dialogPosPrimaryKey Primärschlüssel der Stücklistenpositionsvariante in DIALOG
     * @return
     */
    public static iPartsDataFactoryDataList loadAfterSalesFactoryDataListForDialogPositionsVariant(EtkProject project, iPartsDialogBCTEPrimaryKey dialogPosPrimaryKey,
                                                                                                   boolean withHistoryData) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForPositionsVariantFromDB(project, dialogPosPrimaryKey, DBActionOrigin.FROM_DB,
                                                      iPartsFactoryDataTypes.FACTORY_DATA_AS, withHistoryData);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataFactoryData}s aus der Konstruktion.
     *
     * @param project
     * @param dialogGUID GUID der Stücklistenpositionsvariante in DIALOG
     * @return
     */
    public static iPartsDataFactoryDataList loadConstructionFactoryDataListForDialogPositionsVariant(EtkProject project, String dialogGUID,
                                                                                                     boolean withHistoryData) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForPositionsVariantFromDB(project, dialogGUID, DBActionOrigin.FROM_DB,
                                                      iPartsFactoryDataTypes.FACTORY_DATA_CONSTRUCTION, withHistoryData);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataFactoryData}s aus dem After-Sales.
     *
     * @param project
     * @param dialogGUID GUID der Stücklistenpositionsvariante in DIALOG
     * @return
     */
    public static iPartsDataFactoryDataList loadAfterSalesFactoryDataListForDialogPositionsVariant(EtkProject project, String dialogGUID,
                                                                                                   boolean withHistoryData) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForPositionsVariantFromDB(project, dialogGUID, DBActionOrigin.FROM_DB,
                                                      iPartsFactoryDataTypes.FACTORY_DATA_AS, withHistoryData);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataFactoryData}s aus dem After-Sales.
     *
     * @param project
     * @param eldasGUID
     * @return
     */
    public static iPartsDataFactoryDataList loadFactoryDataListForEldasPosition(EtkProject project, String eldasGUID) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForGUIDFromDB(project, eldasGUID, DBActionOrigin.FROM_DB);

        // ggf. Fußnote zu jedem Eintrag dazu laden
        for (iPartsDataFactoryData factoryData : list.getAsList()) {
            String footNoteIdStr = factoryData.getFieldValue(iPartsConst.FIELD_DFD_FN_ID);
            if (StrUtils.isValid(footNoteIdStr)) {
                iPartsFootNoteId footNoteId = new iPartsFootNoteId(footNoteIdStr);
                iPartsDataFootNote dataFootNote = new iPartsDataFootNote(project, footNoteId);
                dataFootNote.loadFromDB(footNoteId);
                factoryData.setAggregatedDataObject(iPartsDataFactoryData.AGGREGATE_ELDAS_FOOTNOTE, dataFootNote);
            }
        }

        return list;
    }

    /**
     * Lädt alle Werksdaten zur übergebenen GUID (egal, ob ELDAS, EPC oder DIALOG)
     *
     * @param project
     * @param guid
     * @return
     */
    public static iPartsDataFactoryDataList loadFactoryDataGUID(EtkProject project, String guid) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForGUIDFromDB(project, guid, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Werkseinsatzdaten zu einem Submodul
     *
     * @param project
     * @param subModuleId
     * @return
     */
    public static iPartsDataFactoryDataList loadFactoryDataForSubModule(EtkProject project, HmMSmId subModuleId, boolean withHistoryData) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForSubModule(project, subModuleId, DBActionOrigin.FROM_DB, withHistoryData);
        return list;
    }

    /**
     * Lädt alle IDs der AS-Werkseinsatzdaten zu einer Werksnummer, Baureihe, Ausführungsart und PEM, mit historischen Daten, aber
     * <b>OHNE</b> Berücksichtigung von {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s.
     *
     * @param project
     * @param factoryNo
     * @param seriesNo
     * @param pem
     * @param aa        Ausführungsart. Kann {@code null} sein, wenn keine Einschränkung gewünscht ist
     * @return
     */
    public static iPartsDataFactoryDataList loadFactoryDataForFactorySeriesAAandPEM(EtkProject project, String factoryNo,
                                                                                    String seriesNo, String pem, String aa) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForSeriesNoAndAAAndPEMFromDB(project, factoryNo, seriesNo, aa, pem, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataFactoryDataList loadReleasedFactoryData(EtkProject project, iPartsFactoryDataId factoryDataId) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        String[] whereFields = new String[]{ FIELD_DFD_GUID, FIELD_DFD_FACTORY, FIELD_DFD_SPKZ, FIELD_DFD_DATA_ID, FIELD_DFD_STATUS };
        String[] whereValues = new String[]{ factoryDataId.getGuid(), factoryDataId.getFactory(), factoryDataId.getSplitAttribute(),
                                             factoryDataId.getDataId(), iPartsDataReleaseState.RELEASED.getDbValue() };
        list.loadFactoryDataSortByADATAndSeqNo(project, whereFields, whereValues, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * LÄdt alle Werksdaten für die übergebenen Bedingungen sortiert nach ADAT und Sequenznummer
     *
     * @param project
     * @param whereFields
     * @param whereValues
     * @param origin
     */
    private void loadFactoryDataSortByADATAndSeqNo(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin) {
        clear(origin);
        String[] sortFields = new String[]{ FIELD_DFD_ADAT, FIELD_DFD_SEQ_NO };
        searchSortAndFill(project, TABLE_DA_FACTORY_DATA, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    private void loadFactoryDataForSeriesNoAndAAAndPEMFromDB(EtkProject project, String factoryNo, String seriesNo, String aa,
                                                             String pem, DBActionOrigin origin) {
        AbstractCondition conditionList = new ConditionList(new Condition(FIELD_DFD_FACTORY, Condition.OPERATOR_EQUALS, factoryNo));
        conditionList.and(new Condition(FIELD_DFD_SERIES_NO, Condition.OPERATOR_EQUALS, seriesNo));
        if (aa != null) {
            conditionList.and(new Condition(FIELD_DFD_AA, Condition.OPERATOR_EQUALS, aa));
        }

        List<AbstractCondition> pemConditionList = new ArrayList<>();
        pemConditionList.add(new Condition(FIELD_DFD_PEMA, Condition.OPERATOR_EQUALS, pem));
        pemConditionList.add(new Condition(FIELD_DFD_PEMB, Condition.OPERATOR_EQUALS, pem));
        AbstractCondition pemCondition = new ConditionList(pemConditionList, true);
        conditionList.and(pemCondition);

        searchAndFill(project, TABLE_DA_FACTORY_DATA, conditionList, LoadType.ONLY_IDS, origin);
    }

    private void searchAndFill(EtkProject project, String tableName, AbstractCondition condition, LoadType loadType, DBActionOrigin origin) {
        DBDataObjectAttributesList attributesList;
        DBDataSet dbSet = null;
        try {
            String[] selectFieldNames;

            // Alle Felder der Tabelle bestimmen
            EtkDatabaseTable tableDef = project.getConfig().getDBDescription().findTable(tableName);
            if (tableDef != null) {
                selectFieldNames = ArrayUtil.toStringArray(tableDef.getAllFieldsNoBlob());
            } else {
                Logger.getLogger().throwRuntimeException("No table definition for '" + tableName + "'");
                return;
            }

            DBSQLQuery query = project.getDB().getDBForTable(tableName).getNewQuery();
            query.select(selectFieldNames)
                    .from(tableName)
                    .where(condition);

            attributesList = new DBDataObjectAttributesList();
            dbSet = query.executeQuery();
            while (dbSet.next()) {
                List<String> strList = dbSet.getStringList();
                DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                for (int iField = 0; iField < selectFieldNames.length; iField++) {
                    attributes.addField(selectFieldNames[iField], strList.get(iField), DBActionOrigin.FROM_DB);
                }
                attributesList.add(attributes);
            }
        } finally {
            if (dbSet != null) {
                dbSet.close();
            }
        }

        fillAndAddDataObjectsFromAttributesList(project, attributesList, loadType, origin);
    }

    /**
     * Lädt alle AS-Werkseinsatzdaten zu einer BR und PEM
     *
     * @param project
     * @param seriesNo
     * @param pem
     * @param source          z.B. DIALOG; null wenn keine Einschränkung
     * @param withHistoryData
     * @return
     */
    public static iPartsDataFactoryDataList loadAfterSalesFactoryDataForSeriesNoAndPEM(EtkProject project, String seriesNo, String pem,
                                                                                       String source, boolean withHistoryData) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadFactoryDataForSeriesNoAndPEM(project, seriesNo, pem, iPartsFactoryDataTypes.FACTORY_DATA_AS, source,
                                              DBActionOrigin.FROM_DB, withHistoryData);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataFactoryData}s für die übergebene Baureihe und Quelle
     *
     * @param project
     * @param seriesNo
     * @param dataOrigin
     * @return
     */
    public static iPartsDataFactoryDataList loadFactoryDataForSeriesAndSource(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin) {
        iPartsDataFactoryDataList list = new iPartsDataFactoryDataList();
        list.loadAllDataForSeriesAndOrigin(project, seriesNo, dataOrigin, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllDataForSeriesAndOrigin(EtkProject project, String seriesNo, iPartsImportDataOrigin dataOrigin, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DFD_SERIES_NO };
        String[] whereValues = new String[]{ seriesNo };
        if ((dataOrigin != null) && (dataOrigin != iPartsImportDataOrigin.UNKNOWN)) {
            whereFields = StrUtils.mergeArrays(whereFields, new String[]{ FIELD_DFD_SOURCE });
            whereValues = StrUtils.mergeArrays(whereValues, new String[]{ dataOrigin.getOrigin() });
        }

        searchAndFill(project, TABLE_DA_FACTORY_DATA, whereFields, whereValues, LoadType.COMPLETE, origin);
    }


    /**
     * Lädt eine Liste aller {@link iPartsDataFactoryData}s für den Import
     *
     * @param project
     * @param seriesNumber
     * @param origin
     */
    public void loadFactoryDataForImportFromDB(EtkProject project, String seriesNumber, DBActionOrigin origin) {
        loadAllDataForSeriesAndOrigin(project, seriesNumber, null, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataFactoryData}s
     *
     * @param project
     * @param dialogPosPrimaryKey Primärschlüssel der Stücklistenpositionsvariante in Dialog
     * @param origin
     */
    public void loadFactoryDataForPositionsVariantFromDB(EtkProject project, iPartsDialogBCTEPrimaryKey dialogPosPrimaryKey,
                                                         DBActionOrigin origin, iPartsFactoryDataTypes type, boolean withHistoryData) {
        loadFactoryDataIntern(project,
                              new String[]{ FIELD_DFD_SERIES_NO,
                                            FIELD_DFD_HM,
                                            FIELD_DFD_M,
                                            FIELD_DFD_SM,
                                            FIELD_DFD_POSE,
                                            FIELD_DFD_POSV,
                                            FIELD_DFD_WW,
                                            FIELD_DFD_ET,
                                            FIELD_DFD_AA,
                                            FIELD_DFD_SDATA,
                                            FIELD_DFD_DATA_ID },
                              new String[]{ dialogPosPrimaryKey.seriesNo,
                                            dialogPosPrimaryKey.hm,
                                            dialogPosPrimaryKey.m,
                                            dialogPosPrimaryKey.sm,
                                            dialogPosPrimaryKey.posE,
                                            dialogPosPrimaryKey.posV,
                                            dialogPosPrimaryKey.ww,
                                            dialogPosPrimaryKey.et,
                                            dialogPosPrimaryKey.aa,
                                            dialogPosPrimaryKey.sData,
                                            type.getDbValue() },
                              origin, withHistoryData);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataFactoryData}s für DIALOG
     *
     * @param project
     * @param dialogGUID GUID der Stücklistenpositionsvariante in DIALOG
     * @param origin
     */
    public void loadFactoryDataForPositionsVariantFromDB(EtkProject project, String dialogGUID,
                                                         DBActionOrigin origin, iPartsFactoryDataTypes type, boolean withHistoryData) {
        loadFactoryDataIntern(project, new String[]{ FIELD_DFD_GUID, FIELD_DFD_DATA_ID },
                              new String[]{ dialogGUID, type.getDbValue() }, origin, withHistoryData);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataFactoryData}s für ELDAS
     *
     * @param project
     * @param guid
     * @param origin
     */
    public void loadFactoryDataForGUIDFromDB(EtkProject project, String guid, DBActionOrigin origin) {
        loadFactoryDataIntern(project, new String[]{ FIELD_DFD_GUID },
                              new String[]{ guid }, origin, true);
    }

    /**
     * Lädt alle Werkseinsatzdaten zu einem Submodul
     *
     * @param project
     * @param subModuleId
     * @param origin
     */
    public void loadFactoryDataForSubModule(EtkProject project, HmMSmId subModuleId, DBActionOrigin origin, boolean withHistoryData) {
        loadFactoryDataIntern(project,
                              new String[]{ FIELD_DFD_SERIES_NO,
                                            FIELD_DFD_HM,
                                            FIELD_DFD_M,
                                            FIELD_DFD_SM },
                              new String[]{ subModuleId.getSeries(),
                                            subModuleId.getHm(),
                                            subModuleId.getM(),
                                            subModuleId.getSm() },
                              origin, withHistoryData);
    }

    /**
     * Lädt alle Werkseinsatzdaten zu einer BR und PEM, aber <b>OHNE</b> Berücksichtigung von {@link de.docware.apps.etk.base.db.AbstractRevisionChangeSet}s.
     *
     * @param project
     * @param seriesNo
     * @param pem
     * @param dataType        z.B. AS
     * @param source          Quelle, z.B. DIALOG oder null wenn keine Einschränkung
     * @param origin
     * @param withHistoryData
     */
    public void loadFactoryDataForSeriesNoAndPEM(EtkProject project, String seriesNo, String pem, iPartsFactoryDataTypes dataType,
                                                 String source, DBActionOrigin origin, boolean withHistoryData) {
        AbstractCondition conditionList = new ConditionList(new Condition(FIELD_DFD_SERIES_NO, Condition.OPERATOR_EQUALS, seriesNo));
        if (dataType != null) {
            conditionList.and(new Condition(FIELD_DFD_DATA_ID, Condition.OPERATOR_EQUALS, dataType.getDbValue()));
        }
        if (source != null) {
            conditionList.and(new Condition(FIELD_DFD_SOURCE, Condition.OPERATOR_EQUALS, source));
        }

        List<AbstractCondition> pemConditionList = new ArrayList<>();
        pemConditionList.add(new Condition(FIELD_DFD_PEMA, Condition.OPERATOR_EQUALS, pem));
        pemConditionList.add(new Condition(FIELD_DFD_PEMB, Condition.OPERATOR_EQUALS, pem));
        AbstractCondition pemCondition = new ConditionList(pemConditionList, true);
        conditionList.and(pemCondition);

        searchAndFill(project, TABLE_DA_FACTORY_DATA, conditionList, LoadType.COMPLETE, origin);

        if (!withHistoryData) {
            list = getListWithoutHistoryData();
        }
    }

    /**
     * Hilfsroutine, die alle Werkseinsatzdaten mit verschiedenen where-Bedingungen lädt
     *
     * @param project
     * @param whereFields
     * @param whereValues
     * @param origin
     */
    private void loadFactoryDataIntern(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin, boolean withHistoryData) {
        clear(origin);
        String[] sortFields = new String[]{ FIELD_DFD_SERIES_NO, FIELD_DFD_HM, FIELD_DFD_M,
                                            FIELD_DFD_SM, FIELD_DFD_POSE, FIELD_DFD_POSV,
                                            FIELD_DFD_WW, FIELD_DFD_ET, FIELD_DFD_AA,
                                            FIELD_DFD_SDATA, FIELD_DFD_FACTORY, FIELD_DFD_SPKZ,
                                            FIELD_DFD_ADAT };

        searchSortAndFill(project, TABLE_DA_FACTORY_DATA, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);

        if (!withHistoryData) {
            list = getListWithoutHistoryData();
        }
    }

    private void loadFactoryDataInternSortByADAT(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin) {
        clear(origin);
        String[] sortFields = new String[]{ FIELD_DFD_ADAT };

        searchSortAndFill(project, TABLE_DA_FACTORY_DATA, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    /**
     * Der Testdatensatz ist nicht mehr aktuell und von einem Anderen überschrieben worden.
     * Die zu vergleichenden Datensätze müssen für die gleiche Positionsvariante sein (iPartsDialogBCTEPrimaryKey) und
     * es muss sich um das gleich Werk handeln von diesen Datensätzen wird der mit dem neusten ADAT genommen
     *
     * @param testValue                      Der zu testende Datensatz
     * @param dataList                       Eine Liste von Datensätzen, mit denen der Testdatensatz verglichen wird.
     * @param ignoreDataTypesAndStatusValues Gibt an, ob unterschiedliche Datentypen und Status-Werte ignoriert werden sollen
     * @return Ob ein neuerer Datensatz existiert
     */
    private boolean isNewerRecordInList(iPartsDataFactoryData testValue, List<iPartsDataFactoryData> dataList, boolean ignoreDataTypesAndStatusValues) {
        for (iPartsDataFactoryData value : dataList) {
            iPartsFactoryDataId id1 = testValue.getAsId();
            iPartsFactoryDataId id2 = value.getAsId();

            // Unterschiedliche Datentypen und Statuswerte sollen nur dann in Betracht gezogen werden, wenn explizit gewollt.
            // Dann muss ignoreDataTypesAndStatusValues false sein.
            if (!ignoreDataTypesAndStatusValues) {
                // Daten vom Werk und Daten von AS müssen getrennt betrachtet werden
                if (!id1.getDataId().equals(id2.getDataId())) {
                    continue;
                }

                // Unterschiedliche Statuswerte müssen getrennt betrachtet werden
                if (!value.getFieldValue(FIELD_DFD_STATUS).equals(testValue.getFieldValue(FIELD_DFD_STATUS))) {
                    continue;
                }
            }

            if (!id1.equals(id2)) {
                // Vergleiche, ob die Datensätze zur gleichen Positionsvariante gehören
                iPartsDialogBCTEPrimaryKey key1 = id1.getBCTEPrimaryKey();
                iPartsDialogBCTEPrimaryKey key2 = id2.getBCTEPrimaryKey();

                if ((key1 != null) && key1.equals(key2)) {
                    // Sind die Datensätze für das gleiche Werk?
                    if (id1.getFactory().equals(id2.getFactory())) {
                        int adat1CompareToAdat2 = id1.getAdat().compareTo(id2.getAdat());
                        if (adat1CompareToAdat2 < 0) {
                            // Der Test-Datensatz ist älter und kann entfernt werden
                            return true;
                        } else if (adat1CompareToAdat2 == 0) {
                            // Wenn das ADAT (Änderungsdatum) Datum gleich ist und ein Datensatz mit höherer Sequenznummer
                            // existiert, kann der aktuelle ebenfalls entfernt werden
                            if ((id1.getSeqNo() != null) && (id1.getSeqNo().compareTo(id2.getSeqNo()) < 0)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Liefert die Liste bereinigt von historischen Daten zurück unter Berücksichtigung von unterschiedlichen Datentypen
     * und Status-Werten.
     *
     * @return Die bereinigte Liste mit historischen Daten
     */
    public List<iPartsDataFactoryData> getListWithoutHistoryData() {
        // Default
        return getListWithoutHistoryData(false);
    }

    /**
     * Liefert die Liste bereinigt von historischen Daten zurück.
     *
     * @param ignoreDataTypesAndStatusValues Gibt an, ob unterschiedliche Datentypen und Status-Werte ignoriert werden sollen
     * @return Die bereinigte Liste mit historischen Daten
     */
    public List<iPartsDataFactoryData> getListWithoutHistoryData(boolean ignoreDataTypesAndStatusValues) {
        // Liste mit BCTE Pos Schlüssel gruppieren
        // Das Gruppieren ist aus Performancegründen notwendig. Fachlich müsste es nicht sein
        Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataFactoryData>> mappedList = new LinkedHashMap<>();

        for (iPartsDataFactoryData elem : this) {
            iPartsDialogBCTEPrimaryKey key = elem.getAsId().getBCTEPrimaryKey();
            if (key != null) {
                List<iPartsDataFactoryData> internList = mappedList.get(key);

                if (internList == null) {
                    internList = new DwList<>();
                    mappedList.put(key, internList);
                }
                internList.add(elem);
            }
        }

        // In den gruppierten Listen die alten Daten suchen und entfernen
        for (List<iPartsDataFactoryData> internList : mappedList.values()) {
            for (int i = internList.size() - 1; i >= 0; i--) {
                if (isNewerRecordInList(internList.get(i), internList, ignoreDataTypesAndStatusValues)) {
                    internList.remove(i);
                }
            }
        }


        // Alle gruppierten Listen wieder zu einer großen zusammenfassen und zurückliefern
        List<iPartsDataFactoryData> result = new DwList<iPartsDataFactoryData>();
        for (List<iPartsDataFactoryData> internList : mappedList.values()) {
            result.addAll(internList);
        }
        return result;
    }


    /**
     * Lädt eine komplette Liste aller ELDAS- und EPC-{@link iPartsDataFactoryData}s zum angegebenen Produkt oder SA.
     *
     * @param project
     * @param productOrSANumber
     * @return
     */
    public void loadELDASAndEPCFactoryDataListForProductOrSAFromDB(EtkProject project, String productOrSANumber) {
        clear(DBActionOrigin.FROM_DB);
        // Pattern ELDAS bzw. EPC GUID ist kVari_*
        String factoryDataIdPattern = productOrSANumber + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER + "*";
        searchWithWildCardsSortAndFill(project, new String[]{ FIELD_DFD_GUID }, new String[]{ factoryDataIdPattern }, null,
                                       LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    @Override
    protected iPartsDataFactoryData getNewDataObject(EtkProject project) {
        return new iPartsDataFactoryData(project, (DBDataObjectAttributes)null);
    }
}
