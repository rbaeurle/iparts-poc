/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsKEMWorkBasketMBSId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsEDSSaaCase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataKEMWorkBasketMBSList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketSearchCallback;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketHintMsgs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.Set;

/**
 * Konkrete Helper Klasse für die Suche im MBS-KEM-Arbeitsvorrat
 */
public class SearchWorkBasketMBSKEMHelper extends AbstractSearchWorkBasketKEMHelper {

    private static final String CONFIG_KEY_WORK_BASKET_KEM_MBS = "Plugin/iPartsEdit/WorkBasket_KEM_MBS";

    public SearchWorkBasketMBSKEMHelper(EtkProject project, WorkbasketSearchCallback callback, WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper) {
        super(project, nutzDokRemarkHelper, callback, iPartsWorkBasketTypes.MBS_KEM_WB);
    }

    /**
     * Konfigurierte DisplayFields laden oder vorbesetzen
     *
     * @return
     */
    protected EtkDisplayFields buildDisplayFields() {
        // Anzeigefelder aus Config laden
        displayFields = new EtkDisplayFields();
        EtkConfig config = project.getConfig();
        displayFields.load(config, CONFIG_KEY_WORK_BASKET_KEM_MBS + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);

        if (displayFields.size() == 0) {
            // alle Felder aus TABLE_DA_KEM_WORK_BASKET_MBS hinzufügen
            String tableName = TABLE_DA_KEM_WORK_BASKET_MBS;
            addAllDbFieldsToResultFields(project.getConfig(), displayFields, tableName);

            // DB-Feld für Dokurelevanz nicht anzeigen, stattdessen wird das virtuelle, berechnete Feld angezeigt
            EtkDisplayField unifyField = displayFields.getFeldByName(tableName, getFieldNameUnify(), false);
            if (unifyField != null) {
                unifyField.setVisible(false);
            }

            EtkDisplayField displayField;
            // SAA Benennung nach SAA/BK-Nummer einfügen
            EtkDisplayField saaField = displayFields.getFeldByName(tableName, FIELD_DKWM_SAA, false);
            if ((saaField != null) && saaField.isVisible()) {
                int saaIndex = displayFields.getIndexOfFeld(saaField);
                displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_DESC, true, false);
                displayFields.addFeld(saaIndex + 1, displayField);

                displayField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_CONST_DESC, true, false);
                displayFields.addFeld(saaIndex + 2, displayField);
            }

            // virtuelle Felder hinzufügen
            displayField = new EtkDisplayField(tableName, getVirtualFieldNameCalcDocuRel(), false, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(tableName, getVirtualFieldNameCase(), false, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET, getVirtualFieldNameEtsExtension(), false, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(getVirtualWorkbasketTableName(), getVirtualFieldNameRemarksAvailable(), false, false);
            displayFields.addFeld(displayField);

            displayField = new EtkDisplayField(getVirtualWorkbasketTableName(), getVirtualFieldNameInternalTextAvailable(), false, false);
            displayFields.addFeld(displayField);

            displayFields.loadStandards(project.getConfig());

            for (EtkDisplayField field : displayFields.getVisibleFields()) {
                field.setColumnFilterEnabled(true);
            }
        }
        return displayFields;
    }

    protected String[] getSortFields() {
        return new String[]{ TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_KEM),
                             TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_SAA),
                             TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_GROUP),
                             TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_PRODUCT_NO) };
    }

    @Override
    protected EtkDataObjectList<?> getDataObjectListForJoin() {
        return new iPartsDataKEMWorkBasketMBSList();
    }

    protected void calculateVirtualFields(DBDataObjectAttributes attributes) {
        VarParam<iPartsDocuRelevantTruck> docuRel = new VarParam<>(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED);
        VarParam<iPartsEDSSaaCase> saaCase = new VarParam<>(iPartsEDSSaaCase.EDS_CASE_NOT_SPECIFIED);
        StringBuilder strDocuRelReason = new StringBuilder();
        boolean hasEtsExtension = hasEtsExtension(attributes);
        String productNo = getProductNo(attributes);
        iPartsDocuRelevantTruck currentDocuRel = getDocuRel(attributes);

        boolean manualStatusValid = wbh.isManualStatusValid(currentDocuRel, docuRel, strDocuRelReason);
        if (manualStatusValid) { // Manueller Status nicht gesetzt
            String saaBkNo = getSaaBkNo(attributes);
            if (StrUtils.isValid(saaBkNo)) {
                boolean isSAADocuRel = getSaaBkValidFlag(saaBkNo);
                String moduleNo = getModuleNo(attributes);
                // im Retail benutzt
                if (StrUtils.isValid(productNo)) {
                    if (isSAADocuRel) {
                        if (StrUtils.isValid(moduleNo)) {
                            wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_USED_IN_RETAIL);
                            docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES);
                            if (isBaseList(attributes)) {
                                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_BASIC_PARTLIST);
                            }
                            saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_CHANGED);
                        } else {
                            wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_NOT_USED_IN_RETAIL);
                            wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MODEL_PRODUCT_RELATION_EXISTS);
                            if (isBaseList(attributes)) {
                                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_BASIC_PARTLIST);
                                docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES);
                            } else {
                                docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                            }
                        }
                    } else {
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_USED_IN_RETAIL);
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SAA_GS_NOT_DOCU_REL,
                                                  iPartsNumberHelper.formatPartNo(project, saaBkNo));
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    }
                } else {
                    // nicht im Retail benutzt
                    wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_NOT_USED_IN_RETAIL);
                    if (isSAADocuRel) {
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    } else {
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SAA_GS_NOT_DOCU_REL,
                                                  iPartsNumberHelper.formatPartNo(project, saaBkNo));
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    }
                    if (isBaseList(attributes)) {
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_BASIC_PARTLIST);
                    }
                }
            } else {
                docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SAA_GS_NO_NOT_FOUND);
            }
        }

        wbh.setAttribValue(attributes, getVirtualFieldNameCalcDocuRel(), docuRel.getValue().getDbValue());
        wbh.setAttribValue(attributes, getVirtualFieldNameCase(), saaCase.getValue().getDbValue());
        wbh.setAttribValue(attributes, getVirtualFieldNameDocuRelReason(), strDocuRelReason.toString());

        String kemNo = getKemNo(attributes);
        // Daimler-10346 Gibt es Bemerkungen in Nutzdok-Remarks für diese KEM
        wbh.setAttribValue(attributes, getVirtualFieldNameRemarksAvailable(), nutzDokRemarkHelper.isNutzDokRemarkRefIdAvailabe(kemNo));

        boolean hasInternalText = WorkBasketInternalTextCache.hasInternalText(project, getWbType(), kemNo);
        wbh.setAttribValue(attributes, getVirtualFieldNameInternalTextAvailable(), hasInternalText);
        String firstIntText;
        if (hasInternalText) {
            firstIntText = WorkBasketInternalTextCache.getFirstInternalText(project, getWbType(), kemNo, true, isExport);
        } else {
            firstIntText = "";
        }
        wbh.setAttribValue(attributes, getVirtualFieldNameInternalText(), firstIntText);
        // Attribut für den Wiedervorlage-Termin hinzufügen
        WorkBasketInternalTextCache.addFollowUpDateAttribute(project, attributes, getWbType(), kemNo);
        wbh.setAttribValue(attributes, getVirtualFieldNameEtsExtension(), hasEtsExtension);

        if (hasEtsExtension) {
            // Statusänderung durch ET-Sichtenerweiterung nur falls der manueller Status = DOCU_RELEVANT_TRUCK_NO (NR) ist
            if (currentDocuRel == iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO) {
                wbh.setAttribValue(attributes, getVirtualFieldNameCalcDocuRel(), iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES.getDbValue());
                wbh.setAttribValue(attributes, getVirtualFieldNameCase(), iPartsEDSSaaCase.EDS_CASE_VALIDITY_EXPANSION.getDbValue());
            }
            wbh.appendToDocuRelReason(attributes, getVirtualFieldNameDocuRelReason(), WorkBasketHintMsgs.WBH_ET_VIEW_EXTENSION.getKey());
        }
        if (manualStatusValid) {
            wbh.checkProductStatus(attributes, productNo, getVirtualFieldNameCalcDocuRel(), getVirtualFieldNameCase(), getVirtualFieldNameDocuRelReason());
        }
    }

    /**
     * neue JoinDatas pro Tabelle bilden
     *
     * @param tableName
     * @return
     */
    protected EtkDataObjectList.JoinData getJoinData(String tableName) {
        switch (tableName) {
            case TABLE_DA_PRODUCT:
                return new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_PRODUCT_NO) },
                                                      new String[]{ FIELD_DP_PRODUCT_NO },
                                                      true, false);
            case TABLE_DA_SAA:
                return new EtkDataObjectList.JoinData(TABLE_DA_SAA,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_SAA) },
                                                      new String[]{ FIELD_DS_SAA },
                                                      true, false);
            case TABLE_DA_NUTZDOK_KEM:
                return new EtkDataObjectList.JoinData(TABLE_DA_NUTZDOK_KEM,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_KEM) },
                                                      new String[]{ FIELD_DNK_KEM },
                                                      true, false);
            case TABLE_DA_NUTZDOK_REMARK:
                return new EtkDataObjectList.JoinData(TABLE_DA_NUTZDOK_REMARK,
                                                      new String[]{ TableAndFieldName.make(TABLE_DA_KEM_WORK_BASKET_MBS, FIELD_DKWM_KEM),
                                                                    TableAndFieldName.make(TABLE_DA_NUTZDOK_REMARK, FIELD_DNR_REF_TYPE) },
                                                      new String[]{ FIELD_DNR_REF_ID, EtkDataObjectList.JOIN_TABLE_FIELD_VALUE + iPartsWSWorkBasketItem.TYPE.KEM.name() },
                                                      true, false);
        }
        return null;
    }

    @Override
    protected boolean isOnlyKemNoSet(DBDataObjectAttributes attributes) {
        iPartsKEMWorkBasketMBSId workBasketId = new iPartsKEMWorkBasketMBSId(attributes);
        // sollte zwar niemals vorkommen, aber wer weiss
        return workBasketId.isOnlyKemNoSet();
    }

    @Override
    protected String getPKValuesString(DBDataObjectAttributes attributes) {
        iPartsKEMWorkBasketMBSId workBasketId = new iPartsKEMWorkBasketMBSId(attributes);
        return workBasketId.toString(WorkBasketNutzDokRemarkHelper.DELIMITER_FOR_PKVALUES);
    }

    @Override
    public Set<String> findKgsForCurrentEntry(DBDataObjectAttributes attributes, boolean forOwnProduct) {
        Set<String> result = new HashSet<>();
        // Beim KEM Arbeitsvorrat EDS soll die ermittelte KG verwendet werden.
        iPartsKEMWorkBasketMBSId workBasketId = new iPartsKEMWorkBasketMBSId(attributes);
        String kgValue = workBasketId.getKgNo();
        if (StrUtils.isValid(kgValue)) {
            result.add(kgValue);
        } else {
            // Falls die KG leer ist soll die KG aus der Grundstückliste ermittelt werden
            String extractedKG = workBasketId.extractKgFromGroup();
            if (StrUtils.isValid(extractedKG)) {
                result.add(extractedKG);
            }

        }
        return result;
    }

    //====== viele Getter und Setter zur Vereinfachung =====
    @Override
    protected String getWorkbasketTable() {
        return TABLE_DA_KEM_WORK_BASKET_MBS;
    }

    @Override
    protected String getKemNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DKWM_KEM);
    }

    @Override
    protected String getSaaBkNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DKWM_SAA);
    }

    @Override
    protected String getProductNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DKWM_PRODUCT_NO);
    }

    @Override
    protected String getModuleNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DKWM_MODULE_NO);
    }

    protected String getConGroup(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DKWM_GROUP);
    }

    protected boolean isBaseList(DBDataObjectAttributes attributes) {
        MBSStructureId structureId = new MBSStructureId(getSaaBkNo(attributes), getConGroup(attributes));
        return structureId.isConGroupNode() && structureId.isBasePartlistId();
    }

    @Override
    protected String getDocuRelDB(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DKWM_DOCU_RELEVANT);
    }

    @Override
    protected iPartsDocuRelevantTruck getDocuRel(DBDataObjectAttributes attributes) {
        return iPartsDocuRelevantTruck.getFromDBValue(getDocuRelDB(attributes));
    }

    @Override
    protected String getDocuRelReason(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getVirtualFieldNameDocuRelReason());
    }

    @Override
    protected String getSaaBkKemValue(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldNameSaaBkKem());
    }

    @Override
    protected String getVirtualFieldNameCalcDocuRel() {
        return iPartsDataVirtualFieldsDefinition.DKWM_CALC_DOCU_REL;
    }

    @Override
    protected String getVirtualFieldNameDocuRelReason() {
        return VirtualFieldsUtils.addVirtualFieldMask("DKWM_DOCUREL_REASON");
    }

    @Override
    protected String getVirtualFieldNameCase() {
        return iPartsDataVirtualFieldsDefinition.DKWM_KEM_CASE;
    }

    @Override
    protected String getFieldNameUnify() {
        return FIELD_DKWM_DOCU_RELEVANT;
    }

    @Override
    protected String getFieldNameSaaBkKem() {
        return FIELD_DKWM_KEM;
    }

}
