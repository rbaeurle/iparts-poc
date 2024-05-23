/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAAList;
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
import de.docware.util.sql.terms.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SaaConstMissingSearchHelper extends ConstMissingSearchHelper implements iPartsConst {

    protected static final String CONFIG_KEY_WORK_BASKET_MISSING_CONST_SAA = "Plugin/iPartsEdit/WorkBasketSAAsWithoutConstData";

    private boolean needsSAAData;

    public SaaConstMissingSearchHelper(EtkProject project, WorkbasketSearchCallback callback,
                                       WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper) {
        super(project, callback, nutzDokRemarkHelper, iPartsWorkBasketTypes.MISS_SAA_WB);
    }

    @Override
    protected EtkDisplayFields buildDisplayFields() {
        displayFields = new EtkDisplayFields();
        displayFields.load(project.getConfig(), CONFIG_KEY_WORK_BASKET_MISSING_CONST_SAA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_CONST_DESC, true, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_DOCU_START_DATE, false, false);
            displayField.setColumnFilterEnabled(true);
            displayFields.addFeld(displayField);
            displayField = new EtkDisplayField(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_PROCESSING_STATE, false, false);
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

    protected String[] getSortFields() {
        return new String[]{ TableAndFieldName.make(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA) };
    }

    @Override
    protected String getSaaBkNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DNS_SAA);
    }

    @Override
    protected boolean hasInternalText(DBDataObjectAttributes attributes) {
        String saaBkNo = getSaaBkNo(attributes);
        return WorkBasketInternalTextCache.hasInternalText(project, getWbType(), saaBkNo);
    }

    @Override
    protected String getFirstInternalText(DBDataObjectAttributes attributes) {
        String saaBkNo = getSaaBkNo(attributes);
        return WorkBasketInternalTextCache.getFirstInternalText(project, getWbType(), saaBkNo, true, isExport);
    }

    @Override
    protected String getFollowUpDate(DBDataObjectAttributes attributes) {
        String saaBkNo = getSaaBkNo(attributes);
        return WorkBasketInternalTextCache.getFollowUpDate(project, getWbType(), saaBkNo);
    }

    @Override
    protected String getEtsUnconfirmedFieldName() {
        return FIELD_DNS_ETS_UNCONFIRMED;
    }

    @Override
    protected String getEtsUnconfirmedTableName() {
        return TABLE_DA_NUTZDOK_SAA;
    }

    @Override
    protected String getPKValuesString(DBDataObjectAttributes attributes) {
        return getSaaBkNo(attributes);
    }

    @Override
    protected EtkDataObjectList<?> getDataObjectListForJoin() {
        return new iPartsDataNutzDokSAAList();
    }

    protected void buildSelectedFieldSet() {
        if (selectFields == null) {
            needsSAAData = false;
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
                if (displayField.getKey().getTableName().equals(TABLE_DA_SAA)) {
                    needsSAAData = true;
                }
                if (displayField.isMultiLanguage()) {
                    needsMultiLang = true;
                }
            }

            // es müssen auf jeden Fall alle Felder der TABLE_DA_NUTZDOK_SAA vorhanden sein
            addAllDbFieldsToSelectedFields(project.getConfig(), selectFields, TABLE_DA_NUTZDOK_SAA);
            if (FILL_REMARK_CACHE_BY_JOIN) {
                addAllDbFieldsToSelectedFields(project.getConfig(), selectFields, TABLE_DA_NUTZDOK_REMARK);
            }
        }
    }

    protected EtkDataObjectList.JoinData[] buildJoinDatas(boolean useFasterJoin) {
        String sourceTableName = TABLE_DA_NUTZDOK_SAA;
        List<EtkDataObjectList.JoinData> helper = new DwList<>();
        if (useFasterJoin) {
            helper.add(getJoinData(edsStructureHelper.getStructureTableName(), sourceTableName));
            helper.add(getJoinData(TABLE_DA_STRUCTURE_MBS, sourceTableName));
        }
        if (needsSAAData) {
            helper.add(getJoinData(TABLE_DA_SAA, sourceTableName));
        }
        if (helper.isEmpty()) {
            return null;
        }
        return ArrayUtil.toArray(helper);
    }

    @Override
    protected void loadWithFasterJoin(VarParam<Integer> counter, DBDataObjectAttributesList hitAttributesList, EtkDataObjectList.JoinData[] joinDatas) {
        EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(project, TABLE_DA_NUTZDOK_SAA);
        // select *
        //     from da_nutzdok_saa
        //     where  not (dns_saa in (select distinct dsm_snr from da_structure_mbs))
        //       and  not (dns_saa in (select distinct dmeu_sub_element from da_model_element_usage
        //                  inner join public.da_product_models on (dmeu_modelno = dpm_model_no )))
        //       and  not (dns_saa in (select distinct dhsm_saa from da_hmo_saa_mapping
        //                  inner join da_wb_saa_calculation on (wsc_source = 'CTT' and wsc_saa = dhsm_saa) ))
        //       and dns_processing_state = 'NEW'
        //     order by dns_saa
        List<String> fromFields = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        for (EtkDisplayField field : selectFields.getFields()) {
            fromFields.add(field.getKey().getName());
            fields.add(field.getKey().getFieldName());
        }
        sqlSelect.getQuery().select(new Fields(fromFields));

        sqlSelect.getQuery().from(new Tables(TABLE_DA_NUTZDOK_SAA));

        //      not (dns_saa in (select distinct dsm_snr from da_structure_mbs))
        sqlSelect.getQuery().andNot(new Condition(FIELD_DNS_SAA, Condition.OPERATOR_IN, getDistinctSQLQueryForTableAndFieldName(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR)));
        // and  not (dns_saa in (select distinct dmeu_sub_element from da_model_element_usage
        //           inner join public.da_product_models on (dmeu_modelno = dpm_model_no )))
        sqlSelect.getQuery().andNot(new Condition(FIELD_DNS_SAA, Condition.OPERATOR_IN, getDistinctSQLQueryForTableAndFieldNameWithJoin(edsStructureHelper.getStructureTableName(), edsStructureHelper.getSubElementField(),
                                                                                                                                        TABLE_DA_PRODUCT_MODELS, edsStructureHelper.getModelNumberField(), FIELD_DPM_MODEL_NO)));
        // and  not (dns_saa in (select distinct dhsm_saa from da_hmo_saa_mapping
        //           inner join da_wb_saa_calculation on (wsc_source = 'CTT' and wsc_saa = dhsm_saa and not wsc_model_no = '') ))
        sqlSelect.getQuery().andNot(new Condition(FIELD_DNS_SAA, Condition.OPERATOR_IN, getDistinctSQLQueryForTableAndFieldNameWithJoinExtra(TABLE_DA_HMO_SAA_MAPPING, FIELD_DHSM_SAA,
                                                                                                                                             TABLE_DA_WB_SAA_CALCULATION)));
        // and dns_processing_state = 'NEW'
        sqlSelect.getQuery().and(new Condition(FIELD_DNS_PROCESSING_STATE, Condition.OPERATOR_EQUALS, getAccentuatedNutzDokProcessingState()));
        sqlSelect.getQuery().orderBy(FIELD_DNS_SAA);

        Set<String> tableNames = new HashSet<>();
        if (needsSAAData) {
            tableNames.add(TABLE_DA_SAA);
        }
        addJoinDatas(sqlSelect, joinDatas, tableNames);

        executeQuery(sqlSelect, fields, createFoundAttributesCallback(counter, hitAttributesList));
    }

    protected SQLQuery getDistinctSQLQueryForTableAndFieldNameWithJoinExtra(String tableName, String fieldName,
                                                                            String joinTableName) {
        EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(project, tableName);
        sqlSelect.getQuery().selectDistinct(fieldName);
        sqlSelect.getQuery().from(new Tables(tableName));
        List<AbstractCondition> conditions = new DwList<>();
        // da_wb_saa_calculation on (wsc_saa = dhsm_saa and wsc_source = 'CTT' and not wsc_model_no = '')
        conditions.add(new Condition(TableAndFieldName.make(tableName, fieldName),
                                     Condition.OPERATOR_EQUALS,
                                     new Fields(TableAndFieldName.make(joinTableName, FIELD_WSC_SAA))));
        conditions.add(new Condition(TableAndFieldName.make(joinTableName, FIELD_WSC_SOURCE),
                                     Condition.OPERATOR_EQUALS,
                                     iPartsImportDataOrigin.SAP_CTT.getOrigin()));
        // DAIMLER-16460: ohne diese Abfrage (and not wsc_model_no = '')
//        conditions.add(new Condition(TableAndFieldName.make(joinTableName, FIELD_WSC_MODEL_NO),
//                                     Condition.OPERATOR_NOT_EQUALS,
//                                     ""));
        // inner join da_wb_saa_calculation on (wsc_source = 'CTT' and wsc_saa = dhsm_saa and not wsc_model_no = '')
        sqlSelect.getQuery().join(new InnerJoin(joinTableName, new ConditionList(conditions)));

        return sqlSelect.getQuery();
    }


    @Override
    protected String getRemarkRefId(DBDataObjectAttributes attributes) {
        return getSaaBkNo(attributes);
    }

}
