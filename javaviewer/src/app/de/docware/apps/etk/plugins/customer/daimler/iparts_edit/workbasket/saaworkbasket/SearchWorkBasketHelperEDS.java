/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsEDSSaaCase;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.Set;

public class SearchWorkBasketHelperEDS extends AbstractSearchWorkBasketHelper implements iPartsConst {

    private static final String CONFIG_KEY_WORK_BASKET_EDS = "Plugin/iPartsEdit/WorkBasket_EDS";
    private static final String TABLE_WORK_BASKET_EDS = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_EDS;

    public SearchWorkBasketHelperEDS(EtkProject project, String virtualTableName, WorkbasketSAASearchCallback callback,
                                     WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper) {
        super(project, virtualTableName, callback, nutzDokRemarkHelper, iPartsWorkBasketTypes.EDS_SAA_WB);
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
        displayFields.load(project.getConfig(), CONFIG_KEY_WORK_BASKET_EDS + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
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
            addDisplayField(displayFields, TABLE_WORK_BASKET_EDS, getFieldDocuRelReason(),
                            false, false, true);
            addDisplayField(displayFields, TABLE_WORK_BASKET_EDS, getFieldSaaCase(),
                            false, false, true);


            addDisplayField(displayFields, TABLE_WORK_BASKET_EDS, getFieldModelStatus(),
                            false, false, false);
            addDisplayField(displayFields, TABLE_WORK_BASKET_EDS, getFieldSaaBkStatus(),
                            false, false, false);
            addDisplayField(displayFields, TABLE_WORK_BASKET_EDS, getFieldManualStatus(),
                            false, false, true);
            addDisplayField(displayFields, TABLE_WORK_BASKET_EDS, getFieldAuthorOrder(),
                            false, false, false);

            // zum Testen eines weiteren Joins
            addDisplayField(displayFields, TABLE_DA_NUTZDOK_SAA, FIELD_DNS_GROUP,
                            false, false, false);

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
                                                      new String[]{ EtkDbConst.FIELD_DWA_TOKEN },
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
        }
        return null;
    }

    // ===== Implementierung der abstrakten Getter aus der Basisklasse ====

    @Override
    protected String getFieldDocuRel() {
        return iPartsDataVirtualFieldsDefinition.WBE_DOCU_REL;
    }

    @Override
    protected String getFieldDocuRelReason() {
        return VirtualFieldsUtils.addVirtualFieldMask("WBE-DOCUREL_REASON");
    }

    @Override
    protected String getFieldPartEntryId() {
        return VirtualFieldsUtils.addVirtualFieldMask("WBE-PART_ENTRY_ID");
    }

    @Override
    public String getFieldSaaCase() {
        return iPartsDataVirtualFieldsDefinition.WBE_SAA_CASE;
    }

    @Override
    protected String getFieldModelStatus() {
        return iPartsDataVirtualFieldsDefinition.WBE_MODEL_STATUS;
    }

    @Override
    protected String getFieldSaaBkStatus() {
        return iPartsDataVirtualFieldsDefinition.WBE_SAA_BK_STATUS;
    }

    @Override
    protected String getFieldManualStatus() {
        return iPartsDataVirtualFieldsDefinition.WBE_MANUAL_STATUS;
    }

    @Override
    protected String getFieldAuthorOrder() {
        return iPartsDataVirtualFieldsDefinition.WBE_AUTHOR_ORDER;
    }

    @Override
    protected boolean isSaaDisplayFieldName(String fieldName) {
        return fieldName.equals(getFieldSaa()) || fieldName.equals(FIELD_DS_SAA) || fieldName.equals(FIELD_DNS_SAA);
    }

}
