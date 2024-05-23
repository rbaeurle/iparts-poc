/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCTTHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsEDSSaaCase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductStructures;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketHintMsgs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class SearchWorkBasketHelperCTT extends AbstractSearchWorkBasketHelper implements iPartsConst {

    private static final String CONFIG_KEY_WORK_BASKET_CTT = "Plugin/iPartsEdit/WorkBasket_CTT";
    private static final String TABLE_WORK_BASKET_CTT = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_CTT;

    private Map<String, List<String>> cttModelsMap = new HashMap<>();
    private Map<String, AssemblyId> saTuMap = new HashMap<>();
    private Map<String, AssemblyId> saaTuMap = new HashMap<>();
    // Map<saaBkNo, Map<BM, Map<productNo, List<kg>>>>
    private Map<String, Map<String, Map<String, List<String>>>> saaModelProductsKgMap = new HashMap<>();
    private Set<iPartsEDSSaaCase> searchSaaCaseForSearch;

    public SearchWorkBasketHelperCTT(EtkProject project, String virtualTableName, WorkbasketSAASearchCallback callback,
                                     WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper) {
        super(project, virtualTableName, callback, nutzDokRemarkHelper, iPartsWorkBasketTypes.CTT_SAA_WB);
        wbh = new WorkBasketHelper(project);
    }

    @Override
    protected EtkDisplayFields buildDisplayFields() {
        // Anzeigefelder definieren
        boolean isConfigured = true;
        displayFields = new EtkDisplayFields();
        displayFields.load(project.getConfig(), CONFIG_KEY_WORK_BASKET_CTT + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            isConfigured = false;
            addDisplayField(displayFields, TABLE_WORK_BASKET_CTT, getFieldSaaCase(),
                            false, false, true);
            addDisplayField(displayFields, TABLE_WORK_BASKET_CTT, getFieldDocuRel(),
                            false, false, true);
            addDisplayField(displayFields, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET, getVirtualFieldNameEtsExtension(),
                            false, false, true);
            addDisplayField(displayFields, TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO,
                            false, false, true);
//            addDisplayField(displayFields, TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO,
//                            false, false, true);
            addDisplayField(displayFields, TABLE_DA_MODEL, FIELD_DM_MODEL_NO,
                            false, false, true);
            // zum Testen eines weiteren Joins
            addDisplayField(displayFields, TABLE_DA_MODEL, FIELD_DM_NAME, true, false, false);
            addDisplayField(displayFields, TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SAA,
                            false, false, true);

            addDisplayField(displayFields, TABLE_DA_SAA, FIELD_DS_DESC, true, false, false);
            addDisplayField(displayFields, TABLE_DA_SAA, FIELD_DS_CONST_DESC, true, false, false);
            addDisplayField(displayFields, TABLE_WORK_BASKET_CTT, getFieldAuthorOrder(),
                            false, false, false);
            addDisplayField(displayFields, TABLE_WORK_BASKET_CTT, getFieldModelStatus(),
                            false, false, false);
            addDisplayField(displayFields, TABLE_WORK_BASKET_CTT, getFieldSaaBkStatus(),
                            false, false, false);
            addDisplayField(displayFields, TABLE_DA_SAA, FIELD_DS_KG, false, false, false);
            addDisplayField(displayFields, TABLE_DA_NUTZDOK_SAA, FIELD_DNS_TO_FROM_FLAG,
                            false, false, false);
            addDisplayField(displayFields, TABLE_DA_NUTZDOK_SAA, FIELD_DNS_ETS,
                            false, false, false);
            addDisplayField(displayFields, TABLE_DA_NUTZDOK_SAA, FIELD_DNS_MANUAL_START_DATE,
                            false, false, false);
            addDisplayField(displayFields, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET, getFieldSaaNutzdokRemarksAvailable(),
                            false, false, true);
            addDisplayField(displayFields, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET, getVirtualFieldNameInternalTextAvailable(), false, false, true);
            addDisplayField(displayFields, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET, getVirtualFieldNameFollowUpDate(),
                            false, false, true);
        }
        // Muss-Felder für die Sortierung hinzufügen
        if (displayFields.getFeldByName(TableAndFieldName.make(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO), false) == null) {
            EtkDisplayField displayField = addDisplayField(displayFields, TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO,
                                                           false, false, false);
            displayField.setVisible(false);
        }
        if (displayFields.getFeldByName(TableAndFieldName.make(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SAA), false) == null) {
            EtkDisplayField displayField = addDisplayField(displayFields, TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SAA,
                                                           false, false, false);
            displayField.setVisible(false);
        }
        displayFields.loadStandards(project.getConfig());
        if (!isConfigured) {
            for (EtkDisplayField displayField : displayFields.getFields()) {
                displayField.setWidth(0);
            }
        }
        return displayFields;
    }

    public void loadFromDB(Set<String> modelNumbers, Set<iPartsEDSSaaCase> searchSaaCase, Set<String> usedModelNumbers,
                           Map<String, EtkDataObject> attribJoinMap, EtkDisplayFields externalSelectFields) {
        WorkBasketInternalTextCache.updateWorkBasketCache(project, getWbType());
        resultCount = 0;
        cttModelsMap.clear();
        saTuMap.clear();
        saaTuMap.clear();
        saaModelProductsKgMap.clear();
        searchSaaCaseForSearch = searchSaaCase;
        searchSaaCase = null;

        buildSelectedFieldSet(externalSelectFields);

        String dbLanguage = null;
        if (needsMultiLang) {
            dbLanguage = project.getDBLanguage();
        }
        VarParam<Integer> counter = new VarParam<>(0);
        EtkDataObjectList.FoundAttributesCallback callback = createFoundAttributesCallback(
            modelNumbers, counter, null, null, searchSaaCase,
            usedModelNumbers, attribJoinMap);

        String[] whereTableAndFields = getWhereFields();
        String[] whereValues = getWhereValues();

        EtkDataObjectList.JoinData[] joinDatas = buildJoinDatas(searchSaaCase);
        EtkDataObjectList<? extends EtkDataObject> list = getDataObjectListForSearch();
        nutzDokRemarkHelper.clear();
        list.setSearchWithoutActiveChangeSets(true);
        list.clear(DBActionOrigin.FROM_DB);
//        callback = null;
//        joinDatas = null;
        list.searchSortAndFillWithJoin(project, dbLanguage,
                                       selectFields,
                                       whereTableAndFields, whereValues,
                                       false, getSortFields(), true, null, false, true,
                                       false, callback, true, joinDatas);
    }

    @Override
    protected void buildSelectedFieldSet(EtkDisplayFields externalSelectFields) {
        if (selectFieldSet == null) {
            // todo ???
            needsModelJoin = false;
            needsProductJoin = false;
            needsSAAJoin = false;
            needsNutzDokJoin = true;
            needsMultiLang = false;

            selectFields = new EtkDisplayFields();
            EtkDisplayField selectField;
            selectField = new EtkDisplayField(getTableName(), getKemFromDateField(), false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(getTableName(), getKemToDateField(), false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(getTableName(), getFieldSaa(), false, false);
            selectFields.addFeld(selectField);

            selectField = new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_NO, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_VISIBLE, false, false);
            selectFields.addFeld(selectField);

//            selectField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO, false, false);
//            selectFields.addFeld(selectField);
//            selectField = new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_MODEL_NO, false, false);
//            selectFields.addFeld(selectField);

            addArraySelectFields();

            // In DisplayFields angeforderte Spalten hinzufügen
            for (EtkDisplayField displayField : displayFields.getFields()) {
                String tableName = displayField.getKey().getTableName();
                if (tableName.equals(virtualTableName) || tableName.equals(virtualRemarksTableName)) {
                    continue;
                }

                // Die Baumusterbenennung aus dem Cache holen -> aus den selectFields entfernen
                String fieldName = displayField.getKey().getFieldName();
                if (tableName.equals(TABLE_DA_MODEL) && (fieldName.equals(FIELD_DM_NAME) || fieldName.equals(FIELD_DM_SALES_TITLE)
                                                         || fieldName.equals(FIELD_DM_ADD_TEXT))) {
                    continue;
                }

                selectFields.addFeldIfNotExists(new EtkDisplayField(tableName, fieldName, displayField.isMultiLanguage(),
                                                                    displayField.isArray()));

                if (tableName.equals(TABLE_DA_MODEL)) {
                    needsModelJoin = true;
                }
                if (tableName.equals(TABLE_DA_PRODUCT)) {
                    needsProductJoin = true;
                }
                if (tableName.equals(TABLE_DA_SAA)) {
                    needsSAAJoin = true;
                }
                if (displayField.isMultiLanguage()) {
                    needsMultiLang = true;
                }
            }
            selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_SAA, false, false));
            selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_DA_NUTZDOK_SAA, FIELD_DNS_ETS_UNCONFIRMED, false, false));
            // für die Verdichtung nötig
            if (selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_DA_MODEL, FIELD_DM_MODEL_NO, false, false))) {
                // FIELD_DM_MODEL_NO war nicht konfiguriert
                needsModelJoin = true;
            }
            if (externalSelectFields != null) {
                for (EtkDisplayField externalSelectField : externalSelectFields.getFields()) {
                    selectFields.addFeldIfNotExists(externalSelectField);
                }
            }
            selectFieldSet = new HashSet<>();
            for (EtkDisplayField selField : selectFields.getFields()) {
                selectFieldSet.add(selField.getKey().getName());
            }
        }
    }

    @Override
    protected EtkDataObjectList.JoinData[] buildJoinDatas(Set<iPartsEDSSaaCase> searchSaaCase) {
        List<EtkDataObjectList.JoinData> helper = new DwList<>();
        addJoinDataIfNotNull(helper, getJoinData(searchSaaCase, TABLE_DA_PRODUCT_MODELS));
        if (withArrayJoin) {
            addJoinDataIfNotNull(helper, getJoinData(searchSaaCase, TABLE_DWARRAY));
        }
        if (needsModelJoin) {
            addJoinDataIfNotNull(helper, getJoinData(searchSaaCase, TABLE_DA_MODEL));
        }
        if (needsProductJoin) {
            addJoinDataIfNotNull(helper, getJoinData(searchSaaCase, TABLE_DA_PRODUCT));
        }
        if (needsSAAJoin) {
            addJoinDataIfNotNull(helper, getJoinData(searchSaaCase, TABLE_DA_SAA));
        }
        if (needsNutzDokJoin) {
            addJoinDataIfNotNull(helper, getJoinData(searchSaaCase, TABLE_DA_NUTZDOK_SAA));
        }
        return ArrayUtil.toArray(helper);
    }

    @Override
    protected EtkDataObjectList.JoinData getJoinData(Set<iPartsEDSSaaCase> searchSaaCase, String tableName) {
        switch (tableName) {
            case TABLE_DA_PRODUCT_MODELS:
                return new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT_MODELS,
                                                      new String[]{ TableAndFieldName.make(getTableName(), getModelField()) },
                                                      new String[]{ FIELD_DPM_MODEL_NO },
                                                      true, false);
            case TABLE_DWARRAY:
                return new EtkDataObjectList.JoinData(TABLE_DWARRAY,
                                                      new String[]{ TableAndFieldName.make(getTableName(), getFieldSaa()) },
                                                      new String[]{ EtkDbConst.FIELD_DWA_TOKEN },
                                                      true, false);
            case TABLE_DA_MODEL:
                return new EtkDataObjectList.JoinData(TABLE_DA_MODEL,
                                                      new String[]{ TableAndFieldName.make(getTableName(), getModelField()) },
                                                      new String[]{ FIELD_DM_MODEL_NO },
                                                      true, false);
            case TABLE_DA_PRODUCT:
                return new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO) },
                                                      new String[]{ FIELD_DP_PRODUCT_NO },
                                                      false, false);
            case TABLE_DA_SAA:
                return new EtkDataObjectList.JoinData(TABLE_DA_SAA,
                                                      new String[]{ TableAndFieldName.make(getTableName(), getFieldSaa()) },
                                                      new String[]{ FIELD_DS_SAA },
                                                      true, false);
            case TABLE_DA_NUTZDOK_SAA:
                boolean isLeftOuterJoin = true;
                if ((searchSaaCase != null) && (searchSaaCase.size() == 1) && searchSaaCase.contains(iPartsEDSSaaCase.EDS_CASE_NEW)) {
                    isLeftOuterJoin = false;
                }
                return new EtkDataObjectList.JoinData(TABLE_DA_NUTZDOK_SAA,
                                                      new String[]{ TableAndFieldName.make(getTableName(), getFieldSaa()) },
                                                      new String[]{ FIELD_DNS_SAA },
                                                      isLeftOuterJoin, false);
            case TABLE_DA_NUTZDOK_REMARK:
                return new EtkDataObjectList.JoinData(TABLE_DA_NUTZDOK_REMARK,
                                                      new String[]{ TableAndFieldName.make(getTableName(), getFieldSaa()),
                                                                    TableAndFieldName.make(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_REF_TYPE) },
                                                      new String[]{ FIELD_DNR_REF_ID, EtkDataObjectList.JOIN_TABLE_FIELD_VALUE + iPartsWSWorkBasketItem.TYPE.SAA.name() },
                                                      true, false);
            case TABLE_DA_EDS_SAA_MODELS:
                return new EtkDataObjectList.JoinData(TABLE_DA_EDS_SAA_MODELS,
                                                      new String[]{ TableAndFieldName.make(getTableName(), getFieldSaa()),
                                                                    TableAndFieldName.make(getTableName(), getModelField()) },
                                                      new String[]{ FIELD_DA_ESM_SAA_NO, FIELD_DA_ESM_MODEL_NO },
                                                      true, false);
        }
        return null;
    }

    @Override
    public DBDataObjectAttributes calculateVirtualFields(DBDataObjectAttributes attributes, Map<String, KatalogData> attributesKatMap,
                                                         Map<AssemblyId, String> assemblyProductMap, String today) {
        VarParam<iPartsDocuRelevantTruck> docuRel = new VarParam<>(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED);
        VarParam<iPartsEDSSaaCase> saaCase = new VarParam<>(iPartsEDSSaaCase.EDS_CASE_NOT_SPECIFIED);
        StringBuilder strDocuRelReason = new StringBuilder();
        String saaBkNo = getSaaBkNo(attributes);
        String arrayId = getArrayId(attributes);
        String productNo = getProductNo(attributes);
        String modelNo = getModelNumber(attributes);
        boolean addAttributes = true;
        iPartsDocuRelevantTruck manualStatus = getManualStatus(modelNo, productNo, saaBkNo);
        boolean hasEtsExtension = hasEtsExtension(attributes);

        // Erstmal die Attributewerte sichern
        attributes = buildAttrClone(attributes, null, false);

        /*
        aus DAIMLER-14556:
        Folgende Status und Geschäftsfälle sollen berücksichtigt werden:
        Nicht relevant (NR): Wenn die SAA noch keine Baumuster-Verwendung hat (in der DA_EDS_SAA_MODELS), dann soll der Geschäftsfall "NR" ausgegeben werden.
        Nicht relevant (NR): Wenn die SAA bereits eine Baumuster-Verwendung in der DA_EDS_SAA_MODELS hat, aber das Baumuster noch keinem Produkt zugeordnet ist.
        Offen (O): Prüfe, ob der SA-TU schon vorhanden ist, dann setze den Status "Offen" und den geschäftsfall "Neu"
        Dokumentiert (D) bzw. Offen (O): Prüfe, ist die SAA im dem SA-TU dokumentiert. Falls ja, ermittle alle Produkte, zu den Baumustern der SAA.
        Prüfe, ob zu allen gefundenen Produkten der SA-TU zugeordnet ist. Falls ja, soll der Status Dokumentiert (D) angezeigt werden.
        Falls nein, wird Offen (O) angezeigt. Für jedes Baumuster, bei dem die zugeordnete (SA-TU noch nicht beim Produkt hinterlegt ist,
        wird der Geschäftsfall Baumustergültigkeitserweiterung angezeigt.
        Offen (O): Ist die SAA im TU nicht dokumentiert, dann wird ebenfalls Offen angezeigt und der Geschäftsfall SAA-Gültigkeitserweiterung angezeigt.
        */
        boolean manualStatusValid = wbh.isManualStatusValid(manualStatus, docuRel, strDocuRelReason);
        if (manualStatusValid) { // Manueller Status nicht gesetzt
            if (StrUtils.isValid(saaBkNo)) {
                boolean isSAADocuRel = getSaaBkValidFlag(saaBkNo);
                if (isSAADocuRel) {
                    if (hasCttModelUsage(saaBkNo, modelNo)) {
                        if (StrUtils.isValid(productNo)) {
                            // Prüfe, ob der SA-TU schon vorhanden ist
                            AssemblyId saTuAssemblyId = getSaTuModule(saaBkNo);
                            if ((saTuAssemblyId != null) && saTuAssemblyId.isValidId()) {
                                // setze den Status "Offen" und den geschäftsfall "Neu"
                                String saNumber = getSaNoFromSAA(saaBkNo);
                                // Map<saaBkNo, Map<BM, Map<productNo, List<kg>>>>
                                calculateMap(saaBkNo, saNumber, modelNo, productNo);
                                // Falls der SA-TU in den Produkt eingehängt ist, die KG ermitteln und anzeigen
                                String kg = getKGForSAinProductN(saaBkNo, modelNo, productNo);
                                if (StrUtils.isValid(kg)) {
                                    attributes.addField(FIELD_DS_KG, kg, DBActionOrigin.FROM_DB);
                                }
                                // Prüfe, ist die SAA im dem SA-TU dokumentiert
                                if (StrUtils.isValid(arrayId)) {
                                    wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_RETAIL_USAGE_IN_SATU, saTuAssemblyId.getKVari());
                                    // ist der SA-TU einem Produkt zugewiesen?
                                    if (StrUtils.isValid(kg)) {
                                        // ist der SA-TU allen Produkten zu allen Baumustern dieser SA zugewiesen
                                        Set<String> undocumentedProducts = isSaDocumentedInAllProductsN(saaBkNo);
                                        if (undocumentedProducts.isEmpty()) {
                                            // ja -> Dokumentiert
                                            docuRel.setValue(iPartsDocuRelevantTruck.DOCU_DOCUMENTED_TRUCK);
                                            saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_NOT_SPECIFIED);
                                        } else {
                                            // nein -> offen; bei allen fehlenden BM zusätzlich noch BM Gültigkeitserweiterung setzen
                                            docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES);
                                            // absichtlich kein saaCase gesetzt
                                            wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_UNDOCUMENTED_PRODUCTS, StrUtils.stringListToString(undocumentedProducts, ", "));
                                        }
                                    } else {
                                        //SA-TU ist diesem Produkt nicht zugewiesen
                                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES);
                                        saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_MODEL_VALIDITY_EXPANSION);
                                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SATU_NOT_CONNECTED, saTuAssemblyId.getKVari());
                                    }
                                } else {
                                    // Die SAA ist nicht im SA-TU dokumentiert
                                    docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES);
                                    saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_SAA_VALIDITY_EXPANSION);
                                    wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_NO_SAA_IN_SATU, saTuAssemblyId.getKVari());
                                }
                            } else {
                                // SA-TU ist nicht vorhanden
                                docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES);
                                saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_NEW);
                                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_NO_USAGE_IN_SATU, saaBkNo);
                            }
                        } else {
                            // Wenn die SAA bereits eine Baumuster-Verwendung in der DA_EDS_SAA_MODELS hat, aber das Baumuster noch keinem Produkt zugeordnet ist.
                            docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                            wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_NO_RELATED_PRODUCT_FOUND);
                        }
                    } else {
                        // Wenn die SAA noch keine Baumuster-Verwendung hat (in der DA_EDS_SAA_MODELS), dann soll der Geschäftsfall "NR" ausgegeben werden.
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SAA_NO_MODEL_USAGE, saaBkNo);
                    }
                } else {
                    wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SAA_NOT_DOCU_REL);
                }
            } else {
                docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SAA_NO_NOT_FOUND);
            }
            wbh.setAttribValue(attributes, getFieldDocuRel(), docuRel.getValue().getDbValue());
            wbh.setAttribValue(attributes, getFieldSaaCase(), saaCase.getValue().getDbValue());
            wbh.setAttribValue(attributes, getFieldDocuRelReason(), strDocuRelReason.toString());
        } else {
            // Manueller Status gesetzt
            wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MANUAL_STATUS);
            docuRel.setValue(manualStatus);
            wbh.setAttribValue(attributes, getFieldDocuRel(), docuRel.getValue().getDbValue());
            wbh.setAttribValue(attributes, getFieldSaaCase(), saaCase.getValue().getDbValue());
            wbh.setAttribValue(attributes, getFieldDocuRelReason(), strDocuRelReason.toString());
            wbh.checkProductStatus(attributes, productNo, getFieldDocuRel(), getFieldSaaCase(), getFieldDocuRelReason());
        }

        // Falls die Doku-Relevanz nicht DOCU_DOCUMENTED_TRUCK und die SAA/BK-Nummer gültig ist, in der Datenstruktur von
        // den ChangeSet-Einträgen nach Autoren-Aufträgen suchen, die zum Produkt, SAA/BK-Nummer und Baumuster passen
        addAuthorNames(attributes, saaBkNo, productNo, modelNo, docuRel, saaCase,
                       manualStatus, addAttributes, strDocuRelReason);

        addAdditionalAttributeValues(attributes, saaBkNo, hasEtsExtension, manualStatus);

        if ((searchSaaCaseForSearch != null) && !searchSaaCaseForSearch.isEmpty()) {
            boolean caseMatched = false;
            for (iPartsEDSSaaCase searchCase : searchSaaCaseForSearch) {
                switch (searchCase) {
                    case EDS_CASE_NEW:
                        caseMatched = saaCase.getValue() == searchCase;
                        break;
                    case EDS_CASE_VALIDITY_EXPANSION:
                        caseMatched = (saaCase.getValue() == iPartsEDSSaaCase.EDS_CASE_MODEL_VALIDITY_EXPANSION) ||
                                      (saaCase.getValue() == iPartsEDSSaaCase.EDS_CASE_SAA_VALIDITY_EXPANSION);
                        break;
                    case EDS_CASE_MODEL_VALIDITY_EXPANSION:
                        caseMatched = saaCase.getValue() == searchCase;
                        break;
                    case EDS_CASE_SAA_VALIDITY_EXPANSION:
                        caseMatched = saaCase.getValue() == searchCase;
                        break;
                }
                if (caseMatched) {
                    break;
                }
            }
            if (!caseMatched) {
                attributes = null;
            }
        }
        return attributes;
    }

    @Override
    protected String getFieldDocuRel() {
        return iPartsDataVirtualFieldsDefinition.WBC_DOCU_REL;
    }

    @Override
    protected String getFieldDocuRelReason() {
        return iPartsDataVirtualFieldsDefinition.WBC_DOCUREL_REASON;
    }

    @Override
    protected String getFieldPartEntryId() {
        return iPartsDataVirtualFieldsDefinition.WBC_PART_ENTRY_ID;
    }

    @Override
    public String getFieldSaaCase() {
        return iPartsDataVirtualFieldsDefinition.WBC_SAA_CASE;
    }

    @Override
    protected String getFieldModelStatus() {
        return iPartsDataVirtualFieldsDefinition.WBC_MODEL_STATUS;
    }

    @Override
    protected String getFieldSaaBkStatus() {
        return iPartsDataVirtualFieldsDefinition.WBC_SAA_BK_STATUS;
    }

    @Override
    protected String getFieldManualStatus() {
        return iPartsDataVirtualFieldsDefinition.WBC_MANUAL_STATUS;
    }

    @Override
    protected String getFieldAuthorOrder() {
        return iPartsDataVirtualFieldsDefinition.WBC_AUTHOR_ORDER;
    }

    @Override
    protected boolean isSaaDisplayFieldName(String fieldName) {
        return fieldName.equals(getFieldSaa()) || fieldName.equals(FIELD_DS_SAA) || fieldName.equals(FIELD_DNS_SAA);
    }

    @Override
    protected boolean getModelValidFlag(String modelNo) {
        if (StrUtils.isEmpty(modelNo)) {
            return false;
        }
        return super.getModelValidFlag(modelNo);
    }

    protected boolean hasCttModelUsage(String saaBkNo, String modelNo) {
        if (StrUtils.isValid(saaBkNo, modelNo)) {
            List<String> saaModels = getAllCttModelsForSAA(saaBkNo);
            return saaModels.contains(modelNo);
        }
        return false;
    }

    protected List<String> getAllCttModelsForSAA(String saaBkNo) {
        if (StrUtils.isValid(saaBkNo)) {
            List<String> saaModels = cttModelsMap.get(saaBkNo);
            if (saaModels == null) {
                DBDataObjectAttributesList attribList = iPartsCTTHelper.loadCTTModelsForSaaWithHmoCheck(project, saaBkNo, true);
                saaModels = new DwList<>();
                for (DBDataObjectAttributes attrib : attribList) {
                    String cttModelNo = attrib.getFieldValue(FIELD_DA_ESM_MODEL_NO);
                    saaModels.add(cttModelNo);
//                    if (cttModelNo.startsWith(iPartsConst.MODEL_NUMBER_PREFIX_CAR)) {
//                        saaModels.add(cttModelNo);
//                    }
                }
                cttModelsMap.put(saaBkNo, saaModels);
            }
            return saaModels;
        }
        return null;
    }

    protected AssemblyId getSaTuModule(String saaBkNo) {
        String saNumber = getSaNoFromSAA(saaBkNo);
        if (StrUtils.isValid(saNumber)) {
            AssemblyId assemblyId = saTuMap.get(saNumber);
            if (assemblyId == null) {
                String moduleNo = SA_MODULE_PREFIX + saNumber;
                assemblyId = new AssemblyId();
                if (project.getEtkDbs().getRecordExists(TABLE_KATALOG, new String[]{ FIELD_K_VARI }, new String[]{ moduleNo })) {
                    assemblyId = new AssemblyId(moduleNo, "");
                }
                saTuMap.put(saNumber, assemblyId);
                saaTuMap.put(saaBkNo, assemblyId);
            }
            return assemblyId;
        }
        return null;
    }

    protected String getSaNoFromSAA(String saaBkNo) {
        if (StrUtils.isValid(saaBkNo)) {
            return iPartsNumberHelper.convertSAAtoSANumber(saaBkNo);
        }
        return null;
    }

    protected String getKGForSAinProductN(String saaBkNo, String modelNo, String productNo) {
        if (StrUtils.isValid(productNo, modelNo, saaBkNo)) {
            // Map<saaBkNo, Map<BM, Map<productNo, List<kg>>>>
            Map<String, Map<String, List<String>>> modelProductsKgMap = saaModelProductsKgMap.get(saaBkNo);
            if (modelProductsKgMap != null) {
                Map<String, List<String>> productKgMap = modelProductsKgMap.get(modelNo);
                if (productKgMap != null) {
                    List<String> kgList = productKgMap.get(productNo);
                    if (Utils.isValid(kgList)) {
                        return StrUtils.stringListToString(kgList, ",");
                    }
                }
            }
        }
        return null;
    }

    protected Set<String> isSaDocumentedInAllProductsN(String saaBkNo) {
        Set<String> undocumentedProducts = new TreeSet<>();
        // modelProductsKgMap: BM -> Produkt -> KGs
            Map<String, Map<String, List<String>>> modelProductsKgMap = saaModelProductsKgMap.get(saaBkNo);
            if (modelProductsKgMap != null) {
                // productKgMap: alle Produkte und deren KGs
                for (Map<String, List<String>> productKgMap : modelProductsKgMap.values()) {
                    for (Map.Entry<String, List<String>> entry : productKgMap.entrySet()) {
                        String product = entry.getKey();
                        List<String> kgList = entry.getValue();
                        if (kgList == null || kgList.isEmpty()) {
                            // sobald zu einem Produkt die KG Liste leer ist, ist diese SA nicht in allen Produkten dokumentiert
                            undocumentedProducts.add(product);
                        }
                    }
                }
            }
        return undocumentedProducts;

    }

    protected String getKGForSAinProduct(String productNo, String saNumber) {
        if (StrUtils.isValid(productNo, saNumber)) {
            iPartsSA sa = iPartsSA.getInstance(project, new iPartsSAId(saNumber));
            iPartsProductId currentProductId = new iPartsProductId(productNo);
            List<String> kgList = iPartsProductStructures.getInstance(project, currentProductId).getSAs(project).get(sa);
            if (Utils.isValid(kgList)) {
                return kgList.get(0);
            }
        }
        return null;
    }

    protected Set<String> findAllProductsForSA(String saaBkNumber, Map<String, Map<String, Map<String, List<String>>>> saaModelProductsKgMap) {
        Set<String> allProducts = new HashSet<>();
        // alle BM zur SAA holen
        List<String> allCttModelsForSAA = getAllCttModelsForSAA(saaBkNumber);
        for (String model : allCttModelsForSAA) {
            // alle Produkte zu den BM holen
            Set<iPartsDataProductModels> products = iPartsProductModels.getInstance(project).getProductModelsByModel(project, model);
            if (products != null) {
                for (iPartsDataProductModels product : products) {
                    allProducts.add(product.getAsId().getProductNumber());
                    if (saaModelProductsKgMap != null) {
                        Map<String, Map<String, List<String>>> modelProductsKgMap = saaModelProductsKgMap.computeIfAbsent(saaBkNumber, k -> new HashMap<>());
                        Map<String, List<String>> productsKgMap = modelProductsKgMap.computeIfAbsent(model, k -> new HashMap<>());
                        productsKgMap.put(product.getAsId().getProductNumber(), new DwList<>());
                    }
                }
            }
        }
        return allProducts;
    }

    protected void calculateMap(String saaBkNumber, String saNumber, String modelNo, String productNo) {
        // Map<saaBkNo, Map<BM, Map<productNo, List<kg>>>>
        if (StrUtils.isValid(saaBkNumber, saNumber, modelNo, productNo)) {
            if ((saaModelProductsKgMap.get(saaBkNumber) != null) && (saaModelProductsKgMap.get(saaBkNumber).get(modelNo) != null) &&
                (saaModelProductsKgMap.get(saaBkNumber).get(modelNo).get(productNo) != null)) {
                return;
            }
            // alle Produkte zu allen Baumustern der SAA holen
            for (String productNumber : findAllProductsForSA(saaBkNumber, saaModelProductsKgMap)) {
                // jeweils die SA-TU Verortung prüfen
                String kg = getKGForSAinProduct(productNumber, saNumber);
                if (StrUtils.isValid(kg)) {
                    Map<String, Map<String, List<String>>> modelProductKgMap = saaModelProductsKgMap.get(saaBkNumber);
                    if (modelProductKgMap != null) {
                        for (Map<String, List<String>> productKgMap : modelProductKgMap.values()) {
                            List<String> kgList = productKgMap.get(productNumber);
                            if (kgList != null) {
                                kgList.add(kg);
                            }
                        }
                    }
                }
            }
        }
    }
}
