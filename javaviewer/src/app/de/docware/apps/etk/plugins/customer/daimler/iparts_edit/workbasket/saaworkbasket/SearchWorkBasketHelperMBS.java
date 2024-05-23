/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsEDSSaaCase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class SearchWorkBasketHelperMBS extends AbstractSearchWorkBasketHelper implements iPartsConst {

    private static final String CONFIG_KEY_WORK_BASKET_MBS = "Plugin/iPartsEdit/WorkBasket_MBS";
    private static final String TABLE_WORK_BASKET_MBS = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_MBS;

    public SearchWorkBasketHelperMBS(EtkProject project, String virtualTableName, WorkbasketSAASearchCallback callback,
                                     WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper) {
        super(project, virtualTableName, callback, nutzDokRemarkHelper, iPartsWorkBasketTypes.MBS_SAA_WB);
    }

    /**
     * Konfigurierte DisplayFields laden oder vorbesetzen
     *
     * @return
     */
    @Override
    protected EtkDisplayFields buildDisplayFields() {
        // Anzeigefelder definieren
        displayFields = new EtkDisplayFields();
        displayFields.load(project.getConfig(), CONFIG_KEY_WORK_BASKET_MBS + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            addDisplayField(displayFields, TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO,
                            false, false, true);
            // zum Testen eines weiteren Joins
            addDisplayField(displayFields, TABLE_DA_MODEL, FIELD_DM_NAME, true, false, false);
            // zum Testen eines weiteren Joins
            addDisplayField(displayFields, TABLE_DA_NUTZDOK_SAA, FIELD_DNS_DOCU_START_DATE, false, false, false);

            addDisplayField(displayFields, TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SAA,
                            false, false, true);
            addDisplayField(displayFields, TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MIN_RELEASE_FROM,
                            false, false, false);
            addDisplayField(displayFields, TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MAX_RELEASE_TO,
                            false, false, false);
            addDisplayField(displayFields, TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO,
                            false, false, true);
            addDisplayField(displayFields, TABLE_WORK_BASKET_MBS, getFieldDocuRel(),
                            false, false, true);
            addDisplayField(displayFields, TABLE_WORK_BASKET_MBS, getFieldSaaCase(),
                            false, false, true);


            addDisplayField(displayFields, TABLE_WORK_BASKET_MBS, getFieldModelStatus(),
                            false, false, false);
            addDisplayField(displayFields, TABLE_WORK_BASKET_MBS, getFieldSaaBkStatus(),
                            false, false, false);
            addDisplayField(displayFields, TABLE_WORK_BASKET_MBS, getFieldManualStatus(),
                            false, false, true);
            addDisplayField(displayFields, TABLE_WORK_BASKET_MBS, getFieldAuthorOrder(),
                            false, false, false);

            // zum Testen eines weiteren Joins
            addDisplayField(displayFields, TABLE_DA_NUTZDOK_SAA, FIELD_DNS_GROUP, false, false, false);

            addDisplayField(displayFields, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET, getFieldSaaNutzdokRemarksAvailable(),
                            false, false, true);
            addDisplayField(displayFields, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET, getVirtualFieldNameEtsExtension(),
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

        return displayFields;
    }

    @Override
    protected void buildSelectedFieldSet(EtkDisplayFields externalSelectFields) {
        if (selectFieldSet == null) {
            needsModelJoin = false;
            needsProductJoin = false;
            needsSAAJoin = false;
            needsNutzDokJoin = true;
            needsMultiLang = false;
            selectFields = new EtkDisplayFields();
            EtkDisplayField selectField;
            selectField = new EtkDisplayField(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MIN_RELEASE_FROM, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MAX_RELEASE_TO, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_SAA, false, false);
            selectFields.addFeld(selectField);

            selectField = new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_NO, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_MODEL_VISIBLE, false, false);
            selectFields.addFeld(selectField);

            addArraySelectFields();

            // In DisplayFields angeforderte Spalten hinzufügen
            for (EtkDisplayField displayField : displayFields.getFields()) {
                String tableName = displayField.getKey().getTableName();
                if (tableName.equals(virtualTableName) || tableName.equals(virtualRemarksTableName)) {
                    continue;
                }

                // Die Baumustertexte aus dem Cache holen -> aus den selectFields entfernen
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
    protected boolean addProductModelAttributes(DBDataObjectAttributes attributes, Map<String, iPartsDataProductModels> allProductModels,
                                                Map<String, iPartsDataProduct> dataProducts) {
        // productModels und product hinzufügen
        if (allProductModels != null) {
            String model = attributes.getFieldValue(getModelField());
            iPartsDataProductModels dataProductModelsForModel = allProductModels.get(model);
            if (dataProductModelsForModel == null) {
                // Abbruch falls kein Produkt zum Baumuster gefunden werden kann
                return false;
            }
            attributes.addFields(dataProductModelsForModel.getAttributes(), DBActionOrigin.FROM_DB);
        }
        if (dataProducts != null) {
            String productNo = attributes.getFieldValue(FIELD_DPM_PRODUCT_NO);
            iPartsDataProduct dataProduct = dataProducts.get(productNo);
            if (dataProduct == null) {
                // Abbruch falls es das Produkt nicht gibt
                return false;
            }
            attributes.addFields(dataProduct.getAttributes(), DBActionOrigin.FROM_DB);
        }
        return true;
    }

    @Override
    protected boolean isMBSConditionValid(DBDataObjectAttributes attributes) {
        // Grundstücklisten nicht berücksichtigen
        String saaFieldValue = attributes.getFieldValue(getFieldSaa());
        if (StrUtils.isValid(saaFieldValue)) {
            MBSStructureId testId = new MBSStructureId(saaFieldValue, "");
            if (testId.isBasePartlistId()) {
                return false;
            }
        } else {
            // Texte (SUB_SNR leer) nicht berücksichtigen
            return false;
        }
        return true;
    }


    /**
     * DAIMLER-11363: Gültigkeitsdatum Ab = Bis bedeutet, das die entsprechenden SAAs bereits in SAP gelöscht wurden bzw. nie gebaut wurden
     * => ignorieren
     *
     * @param attributes
     * @return
     */
    @Override
    protected boolean isMBSEqualStartEndDate(DBDataObjectAttributes attributes) {
        String startDate = attributes.getFieldValue(FIELD_WSC_MIN_RELEASE_FROM);
        String endDate = attributes.getFieldValue(FIELD_WSC_MAX_RELEASE_TO);
        if (StrUtils.isValid(startDate, endDate) && startDate.equals(endDate)) {
            return true;
        }
        return false;
    }

    public void loadFromDB(Set<String> modelNumbers, Set<iPartsEDSSaaCase> searchSaaCase,
                           Set<String> usedModelNumbers, Map<String, EtkDataObject> attribJoinMap, EtkDisplayFields externalSelectFields) {

        WorkBasketInternalTextCache.updateWorkBasketCache(project, getWbType());
        Set<String> modelNumbersForProduct = new HashSet<>();
        if (StrUtils.isValid(searchProductNo)) {
            Set<String> modelList = getProduct(searchProductNo).getModelNumbers(project);
            modelNumbersForProduct = new HashSet<>(modelList);
        }

        resultCount = 0;
        buildSelectedFieldSet(externalSelectFields);

        String dbLanguage = null;
        if (needsMultiLang) {
            dbLanguage = project.getDBLanguage();
        }
        VarParam<Integer> counter = new VarParam<>(0);
        EtkDataObjectList.JoinData[] joinDatas = buildJoinDatas(searchSaaCase);

        if (StrUtils.isValid(searchProductNo)) {
            Set<String> relevantModelsForSearch = new HashSet<>();
            // soll zusätzlich auch nach einem konkreten Baumuster gesucht werden?
            String modelSearchValue = searchValuesAndFields.get(TableAndFieldName.make(getTableName(), getModelField()));
            if (StrUtils.isEmpty(modelSearchValue)) {
                // suche nach allen Baumustern zum Produkt
                relevantModelsForSearch.addAll(modelNumbersForProduct);
            } else {
                // explizite Suche nach Baumuster
                WildCardSettings wildCardSettings = new WildCardSettings();
                wildCardSettings.addWildCardEnd();
                wildCardSettings.addSpaceToWildCard();
                String modelSearchValueEx = wildCardSettings.makeWildCard(modelSearchValue);

                modelNumbersForProduct.forEach((modelNo) -> {
                    if (StrUtils.matchesSqlLike(modelSearchValueEx, modelNo, false)) {
                        relevantModelsForSearch.add(modelNo);
                    }
                });
            }

            // pro Baumuster und Produktnummer den Eintrag aus DA_PRODUCT_MODELS ermitteln
            Map<String, iPartsDataProductModels> allProductModels = new HashMap<>();
            for (String modelNumber : relevantModelsForSearch) {
                iPartsDataProductModels productModels = new iPartsDataProductModels(project,
                                                                                    new iPartsProductModelsId(searchProductNo, modelNumber));
                if (productModels.existsInDB()) {
                    allProductModels.put(modelNumber, productModels);
                }
            }

            // wenn allProductModels leer ist kann vorzeitig abgebrochen werden
            if (allProductModels.isEmpty()) {
                return;
            }

            // den Eintrag für DA_PRODUCT auch direkt aus der DB Laden
            Map<String, iPartsDataProduct> products = new HashMap<>();
            iPartsDataProduct dataProduct = new iPartsDataProduct(project, new iPartsProductId(searchProductNo));
            if (!dataProduct.existsInDB()) {
                products = null;
            } else {
                products.put(searchProductNo, dataProduct);
            }

            EtkDataObjectList<? extends EtkDataObject> list = getDataObjectListForSearch();
            list.setSearchWithoutActiveChangeSets(true);
            list.clear(DBActionOrigin.FROM_DB);
            EtkDataObjectList.FoundAttributesCallback callback = createFoundAttributesCallback(
                    modelNumbersForProduct, counter, allProductModels, products, searchSaaCase,
                    usedModelNumbers, attribJoinMap);

            // Quelle
            String sourceValue = searchValuesAndFields.get(FIELD_WSC_SOURCE);

            // soll auch nach SAAs gesucht werden?
            String saaSearchValue = searchValuesAndFields.get(TableAndFieldName.make(getTableName(), getFieldSaa()));

            // Suche mit 2D Array Werten: (evtl. SAA-Nummer UND) Quelle UND (Baumuster1 ODER Baumuster2 ODER ...)
            // alle Werte die mit UND gesucht werden (SAA, Quelle)
            int sizeAnd = 0;
            if (!StrUtils.isEmpty(saaSearchValue)) {
                sizeAnd++;
            }
            if (!StrUtils.isEmpty(sourceValue)) {
                sizeAnd++;
            }
            String[][] whereFields = new String[sizeAnd + 1][];
            String[][] whereValues = new String[sizeAnd + 1][];
            List<String> whereFieldList = new DwList<>();
            List<String> whereValueList = new DwList<>();
            int indexAnd = 0;
            if (!StrUtils.isEmpty(saaSearchValue)) {
                EtkDataObjectList.addElemsTo2dArray(whereFields, indexAnd, TableAndFieldName.make(getTableName(), getFieldSaa()));
                EtkDataObjectList.addElemsTo2dArray(whereValues, indexAnd, saaSearchValue);
                indexAnd++;
            }
            if (!StrUtils.isEmpty(sourceValue)) {
                EtkDataObjectList.addElemsTo2dArray(whereFields, indexAnd, TableAndFieldName.make(getTableName(), FIELD_WSC_SOURCE));
                EtkDataObjectList.addElemsTo2dArray(whereValues, indexAnd, sourceValue);
                indexAnd++;
            }

            // alle Werte die mit ODER gesucht werden (alle Baumuster)
            String fieldNameSearch = TableAndFieldName.make(getTableName(), getModelField());
            for (String model : allProductModels.keySet()) {
                whereFieldList.add(fieldNameSearch);
                whereValueList.add(model);
            }
            EtkDataObjectList.addElemsTo2dArray(whereFields, indexAnd, ArrayUtil.toArray(whereFieldList));
            EtkDataObjectList.addElemsTo2dArray(whereValues, indexAnd, ArrayUtil.toArray(whereValueList));

            list.searchSortAndFillWithJoin(project, dbLanguage, selectFields,
                                           whereFields, whereValues,
                                           false, getSortFields(), null, false,
                                           null, false, true, false,
                                           callback,
                                           false, joinDatas);
        } else {
            String[] whereTableAndFields = getWhereFields();
            String[] whereValues = getWhereValues();

            nutzDokRemarkHelper.clear();
            EtkDataObjectList<? extends EtkDataObject> list = getDataObjectListForSearch();
            list.setSearchWithoutActiveChangeSets(true);
            list.clear(DBActionOrigin.FROM_DB);

            EtkDataObjectList.FoundAttributesCallback callback = createFoundAttributesCallback(
                    modelNumbersForProduct, counter, null, null, searchSaaCase,
                    usedModelNumbers, attribJoinMap);

            list.searchSortAndFillWithJoin(project, dbLanguage,
                                           selectFields,
                                           whereTableAndFields, whereValues,
                                           false, getSortFields(), true, null, false, true,
                                           false, callback, true, joinDatas);
        }
    }

    /**
     * neue JoinDatas pro Tabelle bilden
     *
     * @param searchSaaCase
     * @param tableName
     * @return
     */
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
                                                      new String[]{ FIELD_DWA_TOKEN },
                                                      true, false);
            case TABLE_DA_MODEL:
                return new EtkDataObjectList.JoinData(TABLE_DA_MODEL,
                                                      new String[]{ TableAndFieldName.make(getTableName(), getModelField()) },
                                                      new String[]{ FIELD_DM_MODEL_NO },
                                                      false, false);
            case TABLE_DA_PRODUCT:
                return new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_MODELS, FIELD_DPM_PRODUCT_NO) },
                                                      new String[]{ FIELD_DP_PRODUCT_NO },
                                                      false, false);
            case TABLE_DA_SAA:
                return new EtkDataObjectList.JoinData(TABLE_DA_SAA,
                                                      new String[]{ TableAndFieldName.make(getTableName(), getFieldSaa()) },
                                                      new String[]{ FIELD_DS_SAA },
                                                      false, false);
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
        }
        return null;
    }


    // ===== Implementierung der abstrakten Getter aus der Basisklasse ====
    @Override
    protected String getFieldDocuRel() {
        return iPartsDataVirtualFieldsDefinition.WBM_DOCU_REL;
    }

    @Override
    protected String getFieldDocuRelReason() {
        return VirtualFieldsUtils.addVirtualFieldMask("WBM-DOCUREL_REASON");
    }

    @Override
    protected String getFieldPartEntryId() {
        return VirtualFieldsUtils.addVirtualFieldMask("WBM-PART_ENTRY_ID");
    }

    @Override
    public String getFieldSaaCase() {
        return iPartsDataVirtualFieldsDefinition.WBM_SAA_CASE;
    }

    @Override
    protected String getFieldModelStatus() {
        return iPartsDataVirtualFieldsDefinition.WBM_MODEL_STATUS;
    }

    @Override
    protected String getFieldSaaBkStatus() {
        return iPartsDataVirtualFieldsDefinition.WBM_SAA_BK_STATUS;
    }

    @Override
    protected String getFieldManualStatus() {
        return iPartsDataVirtualFieldsDefinition.WBM_MANUAL_STATUS;
    }

    @Override
    protected String getFieldAuthorOrder() {
        return iPartsDataVirtualFieldsDefinition.WBM_AUTHOR_ORDER;
    }

    @Override
    protected boolean isSaaDisplayFieldName(String fieldName) {
        return fieldName.equals(getFieldSaa()) || fieldName.equals(FIELD_DS_SAA) || fieldName.equals(FIELD_DNS_SAA);
    }

}
