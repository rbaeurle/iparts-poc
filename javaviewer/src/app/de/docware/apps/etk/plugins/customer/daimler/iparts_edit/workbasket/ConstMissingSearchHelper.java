/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractSearchQueryCancelable;
import de.docware.apps.etk.base.db.EtkDbsSearchQueryCancelable;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.config.iPartsNutzDokProcessingState;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.VarParam;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLQuery;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Helper Klasse für die Suche "Fehlende Konstruktions-Elemente" Arbeitsvorrat.
 */
public abstract class ConstMissingSearchHelper implements iPartsConst {

    protected static final boolean FILL_REMARK_CACHE_BY_JOIN = false;  // true: NutzDok_Remark-Cache wird im Join gefüllt

    protected static final String TABLE_WORK_BASKET = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET;
    protected static final String FIELD_KEM_NUTZDOK_REMARKS_AVAILABLE = iPartsDataVirtualFieldsDefinition.WB_REMARK_AVAILABLE;
    protected static final String FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE = iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT_AVAILABLE;
    protected static final String FIELD_KEM_NUTZDOK_INTERNAL_TEXT = iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT;
    protected static final String FIELD_KEM_FOLLOWUP_DATE = iPartsDataVirtualFieldsDefinition.WB_FOLLOWUP_DATE;
    protected static final String FIELD_KEM_NUTZDOK_ETS_EXTENSION = iPartsDataVirtualFieldsDefinition.WB_ETS_EXTENSION;

    protected EtkProject project;
    iPartsWorkBasketTypes wbType;
    protected EtkDisplayFields selectFields = null;
    protected EtkDisplayFields displayFields;
    protected boolean needsMultiLang;
    protected WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper;
    protected iPartsEdsStructureHelper edsStructureHelper;
    protected String[] whereTableAndFields;
    protected String[] whereValues;
    protected boolean isExport;

    private final WorkbasketSearchCallback searchCallback;

    public ConstMissingSearchHelper(EtkProject project, WorkbasketSearchCallback callback,
                                    WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper, iPartsWorkBasketTypes wbType) {
        this.project = project;
        this.searchCallback = callback;
        this.nutzDokRemarkHelper = nutzDokRemarkHelper;
        this.edsStructureHelper = iPartsEdsStructureHelper.getInstance();
        this.wbType = wbType;
        this.isExport = false;
        buildDisplayFields();
    }

    protected abstract EtkDisplayFields buildDisplayFields();

    public iPartsWorkBasketTypes getWbType() {
        return wbType;
    }

    public void setForExport(boolean value) {
        this.isExport = value;
    }

    /**
     * Joins zusammenbauen und via searchSortAndFillWithJoin() suchen
     */
    protected void loadFromDB(DBDataObjectAttributesList resultAttributesList) {
        WorkBasketInternalTextCache.updateWorkBasketCache(project, getWbType());
        buildSelectedFieldSet();

        String dbLanguage = null;
        if (needsMultiLang) {
            dbLanguage = project.getDBLanguage();
        }
        VarParam<Integer> counter = new VarParam<>(0);
        boolean useFasterJoin = (whereValues != null) && (whereValues.length > 0) && getAccentuatedNutzDokProcessingState().equals(whereValues[0]);
        nutzDokRemarkHelper.clear();
        EtkDataObjectList.JoinData[] joinDatas = buildJoinDatas(useFasterJoin);
        if (useFasterJoin) {
            loadWithFasterJoin(counter, resultAttributesList, joinDatas);
        } else {
            EtkDataObjectList.FoundAttributesCallback callback = createFoundAttributesCallback(counter, resultAttributesList);

            EtkDataObjectList<?> dataObjectListForJoin = getDataObjectListForJoin();
            dataObjectListForJoin.searchSortAndFillWithJoin(project, dbLanguage,
                                                            selectFields,
                                                            whereTableAndFields, whereValues,
                                                            false, getSortFields(), true, null, false, true,
                                                            false, callback, true, joinDatas);
        }
    }

    protected abstract EtkDataObjectList<?> getDataObjectListForJoin();

    protected EtkDataObjectList.FoundAttributesCallback createFoundAttributesCallback(VarParam<Integer> counter,
                                                                                      DBDataObjectAttributesList hitAttributesList) {
        return new EtkDataObjectList.FoundAttributesCallback() {

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                counter.setValue(counter.getValue() + 1);
                if (searchCallback.searchWasCanceled()) {
                    return false;
                }
                calculateVirtualFields(attributes);
                hitAttributesList.add(attributes);

                searchCallback.showProgress(hitAttributesList.size());

                return false;
            }
        };
    }

    protected abstract String getSaaBkNo(DBDataObjectAttributes attributes);

    protected abstract boolean hasInternalText(DBDataObjectAttributes attributes);

    protected abstract String getFirstInternalText(DBDataObjectAttributes attributes);

    protected abstract String getFollowUpDate(DBDataObjectAttributes attributes);

    protected String getFormattedFollowUpDate(DBDataObjectAttributes attributes) {
        String followUpDate = getFollowUpDate(attributes);
        if (StrUtils.isValid(followUpDate)) {
            followUpDate = project.getVisObject().asText(TABLE_DA_INTERNAL_TEXT, FIELD_DIT_CHANGE_DATE, followUpDate, project.getViewerLanguage());
        }
        return followUpDate;
    }

    protected boolean hasEtsExtension(DBDataObjectAttributes attributes) {
        return StrUtils.isValid(getEtsUnconfirmedAttribute(attributes).getAsString());
    }

    protected DBDataObjectAttribute getEtsUnconfirmedAttribute(DBDataObjectAttributes attributes) {
        DBDataObjectAttribute result = attributes.getField(getEtsUnconfirmedFieldName(), false);
        if (result == null) {
            result = new DBDataObjectAttribute(getEtsUnconfirmedFieldName(), DBDataObjectAttribute.TYPE.STRING, false, true);
        }
        return result;
    }

    protected EtkEditField getEditFieldForEtsExtension() {
        return new EtkEditField(getEtsUnconfirmedTableName(), getEtsUnconfirmedFieldName(), false);
    }


    protected abstract String getEtsUnconfirmedFieldName();

    protected abstract String getEtsUnconfirmedTableName();

    /**
     * Liefert einen kombinierten String zurück, der alle Primärschlüssel enthält.
     *
     * @param attributes
     * @return
     */
    protected abstract String getPKValuesString(DBDataObjectAttributes attributes);

    protected DBDataObjectAttributes calculateVirtualFields(DBDataObjectAttributes attributes) {
        WorkBasketHelper wbh = new WorkBasketHelper(project);
        // Daimler-10346 Gibt es Bemerkungen in Nutzdok-Remarks für diese KEM/SAA
        boolean hasInternalText = hasInternalText(attributes);
        wbh.setAttribValue(attributes, FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE, hasInternalText);
        String firstIntText = "";
        if (hasInternalText) {
            firstIntText = getFirstInternalText(attributes);
        }
        wbh.setAttribValue(attributes, FIELD_KEM_NUTZDOK_INTERNAL_TEXT, firstIntText);
        String followUpDate = getFollowUpDate(attributes);
        wbh.setAttribValue(attributes, FIELD_KEM_FOLLOWUP_DATE, followUpDate);

        wbh.setAttribValue(attributes, FIELD_KEM_NUTZDOK_REMARKS_AVAILABLE, nutzDokRemarkHelper.isNutzDokRemarkRefIdAvailabe(getRemarkRefId(attributes)));
        boolean etsExtension = hasEtsExtension(attributes);
        wbh.setAttribValue(attributes, FIELD_KEM_NUTZDOK_ETS_EXTENSION, etsExtension);
        return attributes;
    }

    protected abstract String[] getSortFields();

    protected abstract void buildSelectedFieldSet();

    protected void addAllDbFieldsToSelectedFields(EtkConfig config, EtkDisplayFields selectedFields, String tableName) {
        // es müssen auf jeden Fall alle Felder von tableName vorhanden sein
        EtkDatabaseTable tableDef = config.getDBDescription().findTable(tableName);
        if (tableDef != null) {
            List<String> fieldNamesBlob = tableDef.getBlobFields();
            for (EtkDatabaseField dbField : tableDef.getFieldList()) {
                if (dbField.getName().equals(DBConst.FIELD_STAMP)) {
                    continue;
                }
                if (fieldNamesBlob.contains(dbField.getName())) {
                    continue;
                }
                if (selectedFields.getFeldByName(tableName, dbField.getName(), false) == null) {
                    EtkDisplayField selectField = new EtkDisplayField(tableName, dbField.getName(),
                                                                      dbField.isMultiLanguage(), dbField.isArray());
                    selectedFields.addFeld(selectField);
                }
            }
        }
    }

    protected String getAccentuatedNutzDokProcessingState() {
        return iPartsNutzDokProcessingState.NEW.getDBValue();
    }

    protected abstract EtkDataObjectList.JoinData[] buildJoinDatas(boolean useFasterJoin);

    protected EtkDataObjectList.JoinData getJoinData(String joinDestinationTableName, String joinSourceTableName) {
        switch (joinDestinationTableName) {
            case TABLE_DA_EDS_MODEL:
                return new EtkDataObjectList.JoinData(TABLE_DA_EDS_MODEL,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA) },
                                                      new String[]{ FIELD_EDS_MODEL_MSAAKEY },
                                                      true, false);
            case TABLE_DA_MODEL_ELEMENT_USAGE:
                return new EtkDataObjectList.JoinData(TABLE_DA_MODEL_ELEMENT_USAGE,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA) },
                                                      new String[]{ edsStructureHelper.getSubElementField() },
                                                      true, false);
            case TABLE_DA_STRUCTURE_MBS:
                return new EtkDataObjectList.JoinData(TABLE_DA_STRUCTURE_MBS,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA) },
                                                      new String[]{ FIELD_DSM_SNR },
                                                      true, false);
            case TABLE_DA_SAA:
                return new EtkDataObjectList.JoinData(TABLE_DA_SAA,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA) },
                                                      new String[]{ FIELD_DS_SAA },
                                                      true, false);
            case TABLE_DA_NUTZDOK_REMARK:
                if (joinSourceTableName.equals(TABLE_DA_NUTZDOK_SAA)) {
                    return new EtkDataObjectList.JoinData(TABLE_DA_NUTZDOK_REMARK,
                                                          new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA),
                                                                        TableAndFieldName.make(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_REF_TYPE) },
                                                          new String[]{ FIELD_DNR_REF_ID, EtkDataObjectList.JOIN_TABLE_FIELD_VALUE + iPartsWSWorkBasketItem.TYPE.SAA.name() },
                                                          true, false);
                } else {
                    return new EtkDataObjectList.JoinData(TABLE_DA_NUTZDOK_REMARK,
                                                          new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_KEM),
                                                                        TableAndFieldName.make(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_REF_TYPE) },
                                                          new String[]{ FIELD_DNR_REF_ID, EtkDataObjectList.JOIN_TABLE_FIELD_VALUE + iPartsWSWorkBasketItem.TYPE.KEM.name() },
                                                          true, false);
                }
            case TABLE_DA_EDS_CONST_KIT:
                return new EtkDataObjectList.JoinData(TABLE_DA_EDS_CONST_KIT,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_KEM),
                                                                    TableAndFieldName.make(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_KEM) },
                                                      new String[]{ FIELD_DCK_KEMFROM, FIELD_DCK_KEMTO },
                                                      true, true);
            case TABLE_DA_PARTSLIST_MBS:
                return new EtkDataObjectList.JoinData(TABLE_DA_PARTSLIST_MBS,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_KEM),
                                                                    TableAndFieldName.make(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_KEM) },
                                                      new String[]{ FIELD_DPM_KEM_FROM, FIELD_DPM_KEM_TO },
                                                      true, true);
//            case TABLE_DA_PRODUCT:
//                return new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT,
//                                                      new String[]{ TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_PRODUCT_NO) },
//                                                      new String[]{ FIELD_DP_PRODUCT_NO },
//                                                      false, false);
//            case TABLE_DA_NUTZDOK_KEM:
//                return new EtkDataObjectList.JoinData(TABLE_DA_NUTZDOK_KEM,
//                                                      new String[]{ TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET, FIELD_DKWB_KEM) },
//                                                      new String[]{ FIELD_DNK_KEM },
//                                                      false, false);
        }
        return null;
    }

    /**
     * Wird bei der Suche nach "offenen" Elementen verwendet (DB-Status NEW)
     * Hier handelt es sich um spezielle, optimierte Joins
     *
     * @param counter
     * @param hitAttributesList
     * @param joinDatas
     */
    protected abstract void loadWithFasterJoin(VarParam<Integer> counter, DBDataObjectAttributesList hitAttributesList, EtkDataObjectList.JoinData[] joinDatas);

    protected void executeQuery(EtkSqlCommonDbSelect sqlSelect, List<String> fields, EtkDataObjectList.FoundAttributesCallback foundAttributesCallback) {
        AbstractSearchQueryCancelable searchQueryForThread = null;
        try {
            searchQueryForThread = setQuery(sqlSelect); // hier wird die Query abgeschickt

            while (!searchCallback.searchWasCanceled() && searchQueryForThread.next()) {
                DBDataObjectAttributes attributes = searchQueryForThread.loadAttributes(project, fields);
                if (foundAttributesCallback != null) {
                    foundAttributesCallback.foundAttributes(attributes);
                }
            }
        } catch (CanceledException e) {
            // Query wurde unterbrochen, ist in der Suche ok
        } finally {
            if (searchQueryForThread != null) {
                searchQueryForThread.closeQuery();
            }
        }
    }

    protected AbstractSearchQueryCancelable setQuery(EtkSqlCommonDbSelect sqlSelect) throws CanceledException {
        return new EtkDbsSearchQueryCancelable(sqlSelect.createAbfrageCancelable());
    }

    protected void addJoinDatas(EtkSqlCommonDbSelect sqlSelect, EtkDataObjectList.JoinData[] joinDatas, Set<String> tableNames) {
        if ((joinDatas != null) && (joinDatas.length > 0) && (tableNames != null) && !tableNames.isEmpty()) {
            for (EtkDataObjectList.JoinData joinData : joinDatas) {
                if (tableNames.contains(joinData.joinTable)) {
                    // SQL-Join aus der JoinData erzeugen
                    List<AbstractCondition> conditions = new DwList<>(joinData.sourceJoinFields.length);
                    for (int i = 0; i < joinData.sourceJoinFields.length; i++) {
                        String sourceFieldName = joinData.sourceJoinFields[i];
                        String joinFieldName = joinData.joinTableFields[i];
                        if (joinFieldName.startsWith(EtkDataObjectList.JOIN_TABLE_FIELD_VALUE)) { // Spezialbehandlung für Werte anstatt Feldnamen
                            conditions.add(new Condition(sourceFieldName.toLowerCase(),
                                                         Condition.OPERATOR_EQUALS,
                                                         joinFieldName.substring(EtkDataObjectList.JOIN_TABLE_FIELD_VALUE.length())));
                        } else {
                            conditions.add(new Condition(sourceFieldName.toLowerCase(),
                                                         Condition.OPERATOR_EQUALS,
                                                         new Fields(TableAndFieldName.make(joinData.joinTable, joinFieldName).toLowerCase())));
                        }
                    }

                    if (joinData.isLeftOuterJoin) {
                        sqlSelect.getQuery().join(new LeftOuterJoin(joinData.joinTable.toLowerCase(), new ConditionList(conditions, joinData.isOrForJoinFields)));
                    } else {
                        sqlSelect.getQuery().join(new InnerJoin(joinData.joinTable.toLowerCase(), new ConditionList(conditions, joinData.isOrForJoinFields)));
                    }
                }
            }
        }
    }

    protected SQLQuery getDistinctSQLQueryForTableAndFieldNameWithJoin(String tableName, String fieldName, String joinTableName, String joinFieldName1, String joinFieldName2) {
        EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(project, tableName);
        sqlSelect.getQuery().selectDistinct(fieldName);
        sqlSelect.getQuery().from(new Tables(tableName));
        Condition cond = new Condition(TableAndFieldName.make(tableName, joinFieldName1),
                                       Condition.OPERATOR_EQUALS,
                                       new Fields(TableAndFieldName.make(joinTableName, joinFieldName2)));
        sqlSelect.getQuery().join(new InnerJoin(joinTableName, cond));

        return sqlSelect.getQuery();
    }

    protected SQLQuery getDistinctSQLQueryForTableAndFieldName(String tableName, String fieldName) {
        EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(project, tableName);
        sqlSelect.getQuery().selectDistinct(fieldName);
        sqlSelect.getQuery().from(new Tables(tableName));
        return sqlSelect.getQuery();
    }

    protected SQLQuery getSQLQueryForTableAndFieldName(String sourceFieldName, String tableName,
                                                       String fieldNameFrom, String fieldNameTo) {
        List<String> fromFields = new ArrayList<>();
        fromFields.add(TableAndFieldName.make(tableName, fieldNameFrom));
        fromFields.add(TableAndFieldName.make(tableName, fieldNameTo));

        EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(project, tableName);
        sqlSelect.getQuery().select(new Fields(fromFields));
        sqlSelect.getQuery().from(new Tables(tableName));

        Condition conditionOne = new Condition(sourceFieldName, Condition.OPERATOR_EQUALS, new Fields(fieldNameFrom));
        Condition conditionTwo = new Condition(sourceFieldName, Condition.OPERATOR_EQUALS, new Fields(fieldNameTo));

        sqlSelect.getQuery().where(conditionOne.or(conditionTwo));
        return sqlSelect.getQuery();
    }

    public void setWhereFieldsAndValues(String[] whereTableAndFields, String[] whereValues) {
        this.whereTableAndFields = whereTableAndFields;
        this.whereValues = whereValues;
    }

    /**
     * Hilfsfunktion um für die Suche im KEM AV die gleichen where-Felder zu benutzen wie im SAA AV.
     * Dazu wird nur der entsprechende Tabellen und Feldnamen ausgetauscht.
     *
     * @param searchHelper SAA-Helper
     */
    protected void copyAndModifyWhereFieldsAndValues(ConstMissingSearchHelper searchHelper) {
        String[] whereTableAndFields = new String[searchHelper.whereTableAndFields.length];
        String[] whereValues = new String[searchHelper.whereValues.length];
        for (int lfdNr = 0; lfdNr < searchHelper.whereTableAndFields.length; lfdNr++) {
            // Das Suchfeld ist aus der Tabelle DA_NUTZDOK_SAA, soll aber für beide Tabellen gelten. D.h. für die Suche
            // in DA_NUTZDOK_KEM wird stattdessen das entsprechende Feld aus dieser Tabelle als whereField verwendet
            if (searchHelper.whereTableAndFields[lfdNr].equals(TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_PROCESSING_STATE))) {
                whereTableAndFields[lfdNr] = TableAndFieldName.make(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_PROCESSING_STATE);
            } else {
                whereTableAndFields[lfdNr] = searchHelper.whereTableAndFields[lfdNr];
            }
            whereValues[lfdNr] = searchHelper.whereValues[lfdNr];
        }
        this.whereTableAndFields = whereTableAndFields;
        this.whereValues = whereValues;
    }

    protected abstract String getRemarkRefId(DBDataObjectAttributes attributes);
}
