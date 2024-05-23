/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketSearchCallback;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketSupplierMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Helper Klasse für die Suche in KEM Arbeitsvorräten
 */
public abstract class AbstractSearchWorkBasketKEMHelper implements iPartsConst {

    protected final WorkbasketSearchCallback searchCallback;
    protected EtkProject project;
    protected iPartsWorkBasketTypes wbType;
    protected EtkDisplayFields selectFields = null;
    protected EtkDisplayFields displayFields;
    protected boolean needsKEMData;
    protected boolean needsProductData;
    protected boolean needsSAAData;
    protected boolean needsNutzDokData;
    protected WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper;
    protected boolean needsMultiLang;
    protected String[] whereTableAndFields;
    protected String[] whereValues;
    protected Map<String, Boolean> saaBkDocuRelMap;       // Map ob SAA/BK DocuRel ist (SA-Flag)
    protected Map<String, Boolean> saDocuRelMap;          // Map ob SA DocuRel ist
    protected Map<String, Boolean> ePepKemMap;            // Map, ob zur KEM Einträge in ePep existieren
    protected boolean isExport;
    protected WorkBasketHelper wbh;

    public AbstractSearchWorkBasketKEMHelper(EtkProject project, WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper, WorkbasketSearchCallback callback, iPartsWorkBasketTypes wbType) {
        this.project = project;
        this.wbType = wbType;

        this.nutzDokRemarkHelper = nutzDokRemarkHelper;
        this.searchCallback = callback;
        this.saaBkDocuRelMap = new HashMap<>();
        this.saDocuRelMap = new HashMap<>();
        this.ePepKemMap = new HashMap<>();
        this.isExport = false;

        this.wbh = new WorkBasketHelper(project);

        buildDisplayFields();
    }

    protected abstract EtkDisplayFields buildDisplayFields();

    public EtkDisplayFields getDisplayFields() {
        return displayFields;
    }

    public iPartsWorkBasketTypes getWbType() {
        return wbType;
    }

    public void setForExport(boolean value) {
        this.isExport = value;
    }

    /**
     * selectedFields nur einmal aufbauen und überprüfen
     */
    protected void buildSelectedFieldSet() {
        if (selectFields == null) {
            needsKEMData = false;
            needsProductData = false;
            needsSAAData = false;
            needsNutzDokData = true;
            needsMultiLang = false;

            selectFields = new EtkDisplayFields();
            for (EtkDisplayField displayField : displayFields.getFields()) {
                if (VirtualFieldsUtils.isVirtualField(displayField.getKey().getFieldName())) {
                    continue;
                }
                if (selectFields.getFeldByName(displayField.getKey().getName(), displayField.isUsageField()) == null) {
                    EtkDisplayField selectField = new EtkDisplayField(displayField.getKey().getTableName(), displayField.getKey().getFieldName(),
                                                                      displayField.isMultiLanguage(), displayField.isArray());
                    selectFields.addFeld(selectField);
                }
                if (displayField.getKey().getTableName().equals(TABLE_DA_NUTZDOK_KEM)) {
                    needsKEMData = true;
                } else if (displayField.getKey().getTableName().equals(TABLE_DA_PRODUCT)) {
                    needsProductData = true;
                } else if (displayField.getKey().getTableName().equals(TABLE_DA_SAA)) {
                    needsSAAData = true;
                }
                if (displayField.isMultiLanguage()) {
                    needsMultiLang = true;
                }
            }
            selectFields.addFeldIfNotExists(new EtkDisplayField(TABLE_DA_NUTZDOK_KEM, FIELD_DNK_ETS_UNCONFIRMED, false, false));

            // es müssen auf jeden Fall alle Felder der Haupt-AV Tabelle vorhanden sein
            addAllDbFieldsToResultFields(project.getConfig(), selectFields, getWorkbasketTable());
        }
    }

    protected void addAllDbFieldsToResultFields(EtkConfig config, EtkDisplayFields resultFields, String tableName) {
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
                if (resultFields.getFeldByName(tableName, dbField.getName(), false) == null) {
                    EtkDisplayField selectField = new EtkDisplayField(tableName, dbField.getName(),
                                                                      dbField.isMultiLanguage(), dbField.isArray());
                    resultFields.addFeld(selectField);
                }
            }
        }
    }

    protected abstract String[] getSortFields();

    protected abstract EtkDataObjectList<?> getDataObjectListForJoin();

    /**
     * Callback für searchSortAndFillWithJoin() erzeugen
     *
     * @param counter
     * @return
     */
    protected EtkDataObjectList.FoundAttributesCallback createFoundAttributesCallback(final VarParam<Integer> counter,
                                                                                      final DBDataObjectAttributesList hitAttributesList) {

        return new EtkDataObjectList.FoundAttributesCallback() {

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                counter.setValue(counter.getValue() + 1);
                if (searchCallback.searchWasCanceled()) {
                    return false;
                }
                if (isOnlyKemNoSet(attributes)) {
                    return false;
                }
                calculateVirtualFields(attributes);
                hitAttributesList.add(attributes);
                searchCallback.showProgress(hitAttributesList.size());

                return false;
            }
        };
    }

    protected abstract boolean isOnlyKemNoSet(DBDataObjectAttributes attributes);

    /**
     * Liefert einen kombinierten String zurück, der alle Primärschlüssel enthält.
     *
     * @param attributes
     * @return
     */
    protected abstract String getPKValuesString(DBDataObjectAttributes attributes);

    /**
     * Joins zusammenbauen und via searchSortAndFillWithJoin() suchen
     *
     * @return
     */
    public void loadFromDB(DBDataObjectAttributesList attributesList) {
        WorkBasketInternalTextCache.updateWorkBasketCache(project, getWbType());
        saaBkDocuRelMap = new HashMap<>();
        saDocuRelMap = new HashMap<>();
        ePepKemMap = new HashMap<>();

        buildSelectedFieldSet();

        String dbLanguage = null;
        if (needsMultiLang) {
            dbLanguage = project.getDBLanguage();
        }
        VarParam<Integer> counter = new VarParam<>(0);
        EtkDataObjectList.FoundAttributesCallback callback = createFoundAttributesCallback(counter, attributesList);
        EtkDataObjectList.JoinData[] joinDatas = buildJoinDatas();

        nutzDokRemarkHelper.clear();
        EtkDataObjectList<?> dataKEMWorkBasketList = getDataObjectListForJoin();
        dataKEMWorkBasketList.setSearchWithoutActiveChangeSets(true);
        dataKEMWorkBasketList.clear(DBActionOrigin.FROM_DB);
        boolean searchCaseInsensitive = false;
        // Flag searchCaseInsensitive für alle whereTableAndFields setzen
        boolean[] searchCaseInsensitives = null;
        if (whereTableAndFields != null) {
            searchCaseInsensitives = new boolean[whereTableAndFields.length];
            Arrays.fill(searchCaseInsensitives, searchCaseInsensitive);
        }

        dataKEMWorkBasketList.searchSortAndFillWithJoin(project, dbLanguage,
                                                        selectFields,
                                                        whereTableAndFields, whereValues,
                                                        false, getSortFields(), true, searchCaseInsensitives, false, true,
                                                        false, callback, true, joinDatas);
    }

    protected abstract void calculateVirtualFields(DBDataObjectAttributes attributes);

    /**
     * JoinDatas für searchSortAndFillWithJoin() bilden
     *
     * @return
     */
    EtkDataObjectList.JoinData[] buildJoinDatas() {
        List<EtkDataObjectList.JoinData> helper = new DwList<>();
        if (needsKEMData) {
            helper.add(getJoinData(TABLE_DA_NUTZDOK_KEM));
        }
        if (needsProductData) {
            helper.add(getJoinData(TABLE_DA_PRODUCT));
        }
        if (needsSAAData) {
            helper.add(getJoinData(TABLE_DA_SAA));
        }
        if (needsNutzDokData && !needsKEMData) {
            helper.add(getJoinData(TABLE_DA_NUTZDOK_KEM));
        }
        return ArrayUtil.toArray(helper);
    }

    protected abstract EtkDataObjectList.JoinData getJoinData(String tableName);

    public void setWhereFieldsAndValues(String[] whereTableAndFields, String[] whereValues) {
        this.whereTableAndFields = whereTableAndFields;
        this.whereValues = whereValues;
    }

    // Getter für die Unterschiede zwischen MBS und EDS
    protected abstract String getWorkbasketTable();

    protected abstract String getKemNo(DBDataObjectAttributes attributes);

    protected boolean isInternalTextSet(DBDataObjectAttributes attributes) {
        return attributes.getField(getVirtualFieldNameInternalTextAvailable()).getAsBoolean();
    }

    protected String getFirstInternalText(DBDataObjectAttributes attributes) {
        return attributes.getField(getVirtualFieldNameInternalText()).getAsString();
    }

    protected String getFollowUpDate(DBDataObjectAttributes attributes) {
        return attributes.getField(getVirtualFieldNameFollowUpDate()).getAsString();
    }

    protected abstract String getSaaBkNo(DBDataObjectAttributes attributes);

    protected abstract String getProductNo(DBDataObjectAttributes attributes);

    protected abstract String getModuleNo(DBDataObjectAttributes attributes);

    protected abstract String getDocuRelDB(DBDataObjectAttributes attributes);

    protected abstract iPartsDocuRelevantTruck getDocuRel(DBDataObjectAttributes attributes);

    protected abstract String getDocuRelReason(DBDataObjectAttributes attributes);

    protected abstract String getSaaBkKemValue(DBDataObjectAttributes attributes);

    protected String getVirtualWorkbasketTableName() {
        return iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET;
    }

    protected abstract String getVirtualFieldNameCalcDocuRel();

    protected abstract String getVirtualFieldNameDocuRelReason();

    protected abstract String getVirtualFieldNameCase();

    protected String getVirtualFieldNameRemarksAvailable() {
        return iPartsDataVirtualFieldsDefinition.WB_REMARK_AVAILABLE;
    }

    protected String getVirtualFieldNameInternalTextAvailable() {
        return iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT_AVAILABLE;
    }

    protected String getVirtualFieldNameInternalText() {
        return iPartsDataVirtualFieldsDefinition.WB_INTERNAL_TEXT;
    }

    protected String getVirtualFieldNameFollowUpDate() {
        return iPartsDataVirtualFieldsDefinition.WB_FOLLOWUP_DATE;
    }

    protected String getVirtualFieldNameEtsExtension() {
        return iPartsDataVirtualFieldsDefinition.WB_ETS_EXTENSION;
    }

    protected abstract String getFieldNameUnify();

    protected abstract String getFieldNameSaaBkKem();


    /**
     * Prüft ob an der SA zur aktuellen SAA das Flag für "nicht Doku-relevant" gesetzt wurde
     *
     * @param saaBkNo SAA im DB Format
     * @return <code>true</code> falls das Flag nicht gesetzt wurde, oder es sich nicht um eine gültige SAA handelt (z.B. BK-Nummer)
     */
    protected boolean getSaaBkValidFlag(String saaBkNo) {
        if (!StrUtils.isValid(saaBkNo)) {
            return true;
        }
        // Aufbau Saa/Bk Map + Abfrage DocuRelFlag
        Boolean isDocuRel = saaBkDocuRelMap.get(saaBkNo);
        if (isDocuRel == null) {
            String saNo = iPartsNumberHelper.convertSAAtoSANumber(saaBkNo);
            if (!StrUtils.isValid(saNo)) {
                // könnte Bk-Number sein => Dummy-Eintrag
                isDocuRel = true;
            } else {
                isDocuRel = isSADocuRel(saNo);
            }
            saaBkDocuRelMap.put(saaBkNo, isDocuRel);
        }
        return isDocuRel;
    }

    private boolean isSADocuRel(String saNo) {
        Boolean isSaDocuRel = saDocuRelMap.get(saNo);
        if (isSaDocuRel == null) {
            isSaDocuRel = iPartsSA.getInstance(project, new iPartsSAId(saNo)).isDocuRelevant(project);
            saDocuRelMap.put(saNo, isSaDocuRel);
        }
        return isSaDocuRel;
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

    protected String getEtsUnconfirmedFieldName() {
        return FIELD_DNK_ETS_UNCONFIRMED;
    }

    protected String getEtsUnconfirmedTableName() {
        return TABLE_DA_NUTZDOK_KEM;
    }

    protected EtkEditField getEditFieldForEtsUnconfirmed() {
        return new EtkEditField(getEtsUnconfirmedTableName(), getEtsUnconfirmedFieldName(), false);
    }


    /**
     * Nur Datensätze mit Status = Offen (iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES) für den Export verwenden
     * {@link iPartsDocuRelevantTruck}
     *
     * @param results
     * @return gefiltertes Ergebnis
     */
    public DBDataObjectAttributesList filterResultsForExport(DBDataObjectAttributesList results) {
        DBDataObjectAttributesList filteredResults = new DBDataObjectAttributesList();
        for (DBDataObjectAttributes result : results) {
            iPartsDocuRelevantTruck docuRel = iPartsDocuRelevantTruck.getFromDBValue(result.getFieldValue(getVirtualFieldNameCalcDocuRel()));
            if (docuRel == iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES) {
                filteredResults.add(result);
            }
        }
        return filteredResults;
    }

    public DBDataObjectAttributesList calculateSupplier(DBDataObjectAttributesList attributesList, String kgFieldname,
                                                        String supplierFieldname, String supplierDefault,
                                                        WorkbasketSupplierMapping supplierMapping) {
        DBDataObjectAttributesList results = new DBDataObjectAttributesList();
        boolean isUseProductSupplier = iPartsPlugin.isUseProductSupplier();
        for (DBDataObjectAttributes attributes : attributesList) {
            Set<String> kgsForCurrentEntry = findKgsForCurrentEntry(attributes, false);

            String productNo = getProductNo(attributes);
            Set<String> allModelTypes = null;
            if (StrUtils.isValid(productNo)) {
                iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNo));
                if (isUseProductSupplier) {
                    // Lieferantennummer nur bei Truck direkt aus Produkt anzeigen
                    if (!product.getDocumentationType().isPKWDocumentationType()) {
                        String supplierNo = product.getProductSupplierText(project, Language.DE.getCode());
                        String supplierProductDefault = supplierDefault;
                        if (StrUtils.isValid(supplierNo)) {
                            // damit setSupplierAttributes() richtig arbeitet
                            supplierProductDefault = supplierNo;
                        } else {
                            supplierNo = "";
                        }
                        results.add(supplierMapping.setSupplierAttributes(attributes, supplierNo, kgsForCurrentEntry, kgFieldname,
                                                                          supplierFieldname, supplierProductDefault));
                        continue;
                    }
                }
                allModelTypes = product.getAllModelTypes(project);
            }
            Map<String, Set<String>> supplierKgMap = supplierMapping.buildSupplierKgMap(project, kgsForCurrentEntry,
                                                                                        allModelTypes, productNo, supplierDefault);
            for (Map.Entry<String, Set<String>> entry : supplierKgMap.entrySet()) {
                String supplierNo = entry.getKey();
                Set<String> kgList = entry.getValue();
                results.add(supplierMapping.setSupplierAttributes(attributes, supplierNo, kgList, kgFieldname,
                                                                  supplierFieldname, supplierDefault));
            }
        }
        return results;
    }

    public abstract Set<String> findKgsForCurrentEntry(DBDataObjectAttributes attributes, boolean forOwnProduct);
}
