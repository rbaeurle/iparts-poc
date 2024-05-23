/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEMList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLQuery;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Exists;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.Tables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KemConstMissingSearchHelper extends ConstMissingSearchHelper {

    protected static final String CONFIG_KEY_WORK_BASKET_MISSING_CONST_KEM = "Plugin/iPartsEdit/WorkBasketKEMsWithoutConstData";

    public KemConstMissingSearchHelper(EtkProject project, WorkbasketSearchCallback callback,
                                       WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper) {
        super(project, callback, nutzDokRemarkHelper, iPartsWorkBasketTypes.MISS_KEM_WB);
    }

    @Override
    protected EtkDisplayFields buildDisplayFields() {
        displayFields = new EtkDisplayFields();
        displayFields.load(project.getConfig(), CONFIG_KEY_WORK_BASKET_MISSING_CONST_KEM + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_KEM, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_DOCU_START_DATE, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_PROCESSING_STATE, false, false);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_ETS_EXTENSION, false, false);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_REMARKS_AVAILABLE, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_INTERNAL_TEXT, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_WORK_BASKET, FIELD_KEM_NUTZDOK_INTERNAL_TEXT_AVAILABLE, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_WORK_BASKET, FIELD_KEM_FOLLOWUP_DATE, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);

            displayFields.loadStandards(project.getConfig());
        }
        return displayFields;
    }

    @Override
    protected String[] getSortFields() {
        return new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_KEM) };
    }

    protected String getKemNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DNK_KEM);
    }

    @Override
    protected String getSaaBkNo(DBDataObjectAttributes attributes) {
        return null;
    }

    @Override
    protected boolean hasInternalText(DBDataObjectAttributes attributes) {
        String kemNo = getKemNo(attributes);
        return WorkBasketInternalTextCache.hasInternalText(project, getWbType(), kemNo);
    }

    @Override
    protected String getFirstInternalText(DBDataObjectAttributes attributes) {
        String kemNo = getKemNo(attributes);
        return WorkBasketInternalTextCache.getFirstInternalText(project, getWbType(), kemNo, true, isExport);
    }

    @Override
    protected String getFollowUpDate(DBDataObjectAttributes attributes) {
        String kemNo = getKemNo(attributes);
        return WorkBasketInternalTextCache.getFollowUpDate(project, getWbType(), kemNo);
    }

    @Override
    protected String getEtsUnconfirmedFieldName() {
        return FIELD_DNK_ETS_UNCONFIRMED;
    }

    @Override
    protected String getEtsUnconfirmedTableName() {
        return TABLE_DA_NUTZDOK_KEM;
    }

    @Override
    protected String getPKValuesString(DBDataObjectAttributes attributes) {
        return getKemNo(attributes);
    }

    @Override
    protected EtkDataObjectList<?> getDataObjectListForJoin() {
        return new iPartsDataNutzDokKEMList();
    }

    @Override
    protected void buildSelectedFieldSet() {
        if (selectFields == null) {
            needsMultiLang = false;

            selectFields = new EtkDisplayFields();
            if (displayFields == null) {
                buildDisplayFields();
            }
            for (EtkDisplayField displayField : displayFields.getFields()) {
                if (VirtualFieldsUtils.isVirtualField(displayField.getKey().getFieldName())) {
                    continue;
                }
                if (selectFields.getFeldByName(displayField.getKey().getName(), displayField.isUsageField()) == null) {
                    EtkDisplayField selectField = new EtkDisplayField(displayField.getKey().getTableName(), displayField.getKey().getFieldName(),
                                                                      displayField.isMultiLanguage(), displayField.isArray());
                    selectFields.addFeld(selectField);
                }
                if (displayField.isMultiLanguage()) {
                    needsMultiLang = true;
                }
            }

            // es müssen auf jeden Fall alle Felder der TABLE_DA_NUTZDOK_KEM vorhanden sein
            addAllDbFieldsToSelectedFields(project.getConfig(), selectFields, TABLE_DA_NUTZDOK_KEM);
            if (FILL_REMARK_CACHE_BY_JOIN) {
                addAllDbFieldsToSelectedFields(project.getConfig(), selectFields, TABLE_DA_NUTZDOK_REMARK);
            }
        }
    }

    @Override
    protected EtkDataObjectList.JoinData[] buildJoinDatas(boolean useFasterJoin) {
        String sourceTableName = TABLE_DA_NUTZDOK_KEM;
        List<EtkDataObjectList.JoinData> helper = new DwList<>();
        if (useFasterJoin) {
            helper.add(getJoinData(TABLE_DA_EDS_CONST_KIT, sourceTableName));
            helper.add(getJoinData(TABLE_DA_PARTSLIST_MBS, sourceTableName));
        }
        if (helper.isEmpty()) {
            return null;
        }
        return ArrayUtil.toArray(helper);
    }

    @Override
    protected void loadWithFasterJoin(VarParam<Integer> counter, DBDataObjectAttributesList hitAttributesList, EtkDataObjectList.JoinData[] joinDatas) {
        EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(project, TABLE_DA_NUTZDOK_KEM);
        List<String> fromFields = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        for (EtkDisplayField field : selectFields.getFields()) {
            fromFields.add(field.getKey().getName());
            fields.add(field.getKey().getFieldName());
        }
        sqlSelect.getQuery().select(new Fields(fromFields));

        sqlSelect.getQuery().from(new Tables(TABLE_DA_NUTZDOK_KEM));
        SQLQuery extraQueryEds = getSQLQueryForTableAndFieldName(FIELD_DNK_KEM, TABLE_DA_EDS_CONST_KIT,
                                                                 FIELD_DCK_KEMFROM, FIELD_DCK_KEMTO);
        SQLQuery extraQueryMbs = getSQLQueryForTableAndFieldName(FIELD_DNK_KEM, TABLE_DA_PARTSLIST_MBS,
                                                                 FIELD_DPM_KEM_FROM, FIELD_DPM_KEM_TO);
        sqlSelect.getQuery().whereNot(new Exists(extraQueryEds).andNot(new Exists(extraQueryMbs)))
                .and(new Condition(whereTableAndFields[0], Condition.OPERATOR_EQUALS, whereValues[0]));
        sqlSelect.getQuery().orderBy(FIELD_DNK_KEM);

        Set<String> tableNames = new HashSet<>();
        addJoinDatas(sqlSelect, joinDatas, tableNames);

        executeQuery(sqlSelect, fields, createFoundAttributesCallback(counter, hitAttributesList));
    }

    @Override
    protected String getRemarkRefId(DBDataObjectAttributes attributes) {
        return getKemNo(attributes);
    }

}
