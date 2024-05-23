/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.truckworkbasket.iPartsDataWorkBasketCalcList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductSAsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevantTruck;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsEDSSaaCase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsWBSaaStatesManagement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.WorkbasketSupplierMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketHintMsgs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.helper.WorkBasketInternalTextCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectAttribute;
import de.docware.framework.modules.db.serialization.SerializedDbDataObjectAsJSON;
import de.docware.framework.modules.db.serialization.SerializedEtkDataArray;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public abstract class AbstractSearchWorkBasketHelper implements iPartsConst {

    protected final EtkProject project;
    protected iPartsWorkBasketTypes wbType;
    protected final String virtualTableName;
    protected final String virtualRemarksTableName;
    protected final boolean withArrayJoin = true;          // Mit DWARRAY-Tabelle im Join
    protected final boolean removeForeignProducts = false;  // Treffer über DWARRAY in 'fremde' Produkte entfernen

    protected int resultCount;
    protected Set<String> modelAndSAAFieldNames;
    protected Set<String> keyFieldNames;
    protected EtkDisplayFields selectFields = null;
    protected EtkDisplayFields displayFields = null;
    protected Set<String> selectFieldSet = null;
    protected String searchProductNo;                   // eingegebene ProductNumber
    // Daten aus MasterDataAbstractWorkBasketForm
    private Map<String, KatalogData> attributesKatMap;
    private Map<AssemblyId, String> assemblyProductMap;
    private iPartsWBSaaStatesManagement stateManager;

    // Merker für das Bilden von weiteren Joins
    protected boolean needsModelJoin;
    protected boolean needsProductJoin;
    protected boolean needsSAAJoin;
    protected boolean needsNutzDokJoin;
    protected boolean needsMultiLang;

    protected Map<String, String> searchValuesAndFields = new HashMap<>();

    WorkbasketSAASearchCallback searchCallback;
    protected WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper;

    private Map<String, ModelContainer> modelMap;     // Map für Models
    private Map<String, ProductContainer> productMap; // Map für Products
    private Map<String, Boolean> saaBkDocuRelMap;       // Map ob SAA/BK DocuRel ist (SA-Flag)
    private Map<String, Boolean> saDocuRelMap;          // Map ob SA DocuRel ist
    private Map<String, Boolean> nutzDokSaaMap;

    protected EtkMultiSprache modelValidityText;
    protected boolean isExport;
    protected WorkBasketHelper wbh;

    private Map<String, Map<String, Map<String, Map<String, WorkBasketAuthorOrderData>>>> workBasketItemToAuthorOrdersMap;

    public AbstractSearchWorkBasketHelper(EtkProject project, String virtualTableName,
                                          WorkbasketSAASearchCallback callback, WorkBasketNutzDokRemarkHelper nutzDokRemarkHelper,
                                          iPartsWorkBasketTypes wbType) {
        this.project = project;
        this.wbType = wbType;
        this.virtualTableName = virtualTableName;
        this.virtualRemarksTableName = iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET;
        this.isExport = false;

        this.wbh = new WorkBasketHelper(project);

        modelAndSAAFieldNames = new LinkedHashSet<>();
        modelAndSAAFieldNames.add(getModelField());
        modelAndSAAFieldNames.add(getFieldSaa());

        keyFieldNames = buildKeyFieldNames();
        buildDisplayFields();

        this.searchCallback = callback;
        this.nutzDokRemarkHelper = nutzDokRemarkHelper;

        modelMap = Collections.synchronizedMap(new HashMap<>());
        productMap = Collections.synchronizedMap(new HashMap<>());
        saaBkDocuRelMap = Collections.synchronizedMap(new HashMap<>());
        saDocuRelMap = Collections.synchronizedMap(new HashMap<>());
        nutzDokSaaMap = Collections.synchronizedMap(new HashMap<>());
        workBasketItemToAuthorOrdersMap = Collections.synchronizedMap(new HashMap<>());

        EtkDisplayField displayField = new EtkDisplayField(TABLE_KATALOG, FIELD_K_MODEL_VALIDITY, true, false);
        displayField.loadStandards(project.getConfig());
        modelValidityText = displayField.getText();
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

    protected EtkDisplayField addDisplayField(EtkDisplayFields displayFields, String tableName, String fieldName, boolean isMultiLang,
                                              boolean isArray, boolean enableFilter) {
        EtkDisplayField displayField = new EtkDisplayField(tableName, fieldName, isMultiLang, isArray);
        if (enableFilter) {
            displayField.setColumnFilterEnabled(true);
        }
        if (displayFields != null) {
            displayFields.addFeld(displayField);
        }
        return displayField;
    }

    protected String getKemToDateField() {
        return FIELD_WSC_MAX_RELEASE_TO;
    }

    protected String getKemFromDateField() {
        return FIELD_WSC_MIN_RELEASE_FROM;
    }

    protected String getModelField() {
        return FIELD_WSC_MODEL_NO;
    }

    protected String getTableName() {
        return TABLE_DA_WB_SAA_CALCULATION;
    }

    protected Set<String> buildKeyFieldNames() {
        Set<String> keyFieldNames = new HashSet<>();

        keyFieldNames.add(iPartsConst.FIELD_DPM_PRODUCT_NO);
        keyFieldNames.add(getModelField());
        keyFieldNames.add(getFieldSaa());

        return keyFieldNames;
    }

    protected EtkDataObjectList<? extends EtkDataObject> getDataObjectListForSearch() {
        return new iPartsDataWorkBasketCalcList();
    }

    /**
     * Hilfs-Key für die Bestimmung gleicher EDS_MODEL/DA_MODEL_ELEMENT_USAGE Einträge bzgl. {@code fieldNames} bestimmen
     *
     * @param attributes
     * @param fieldNames
     * @return
     */
    private String buildKey(DBDataObjectAttributes attributes, Collection<String> fieldNames) {
        StringBuilder str = new StringBuilder();
        for (String fieldName : fieldNames) {
            if (str.length() > 0) {
                str.append("&");
            }
            str.append(attributes.getFieldValue(fieldName));
        }
        return str.toString();
    }

    /**
     * Joins zusammenbauen und via searchSortAndFillWithJoin() suchen
     */
    public void loadFromDB(Set<String> modelNumbers, Set<iPartsEDSSaaCase> searchSaaCase, Set<String> usedModelNumbers,
                           Map<String, EtkDataObject> attribJoinMap, EtkDisplayFields externalSelectFields) {
        WorkBasketInternalTextCache.updateWorkBasketCache(project, getWbType());
        resultCount = 0;

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
        list.searchSortAndFillWithJoin(project, dbLanguage,
                                       selectFields,
                                       whereTableAndFields, whereValues,
                                       false, getSortFields(), true, null, false, true,
                                       false, callback, true, joinDatas);
    }

    /**
     * selectedFields nur einmal aufbauen und überprüfen
     *
     * @param externalSelectFields
     */
    protected abstract void buildSelectedFieldSet(EtkDisplayFields externalSelectFields);

    protected String[] getSortFields() {
        return new String[]{ TableAndFieldName.make(getTableName(), getModelField()),
                             TableAndFieldName.make(getTableName(), getFieldSaa()) };
    }

    protected void addArraySelectFields() {
        if (withArrayJoin) {
            EtkDisplayField selectField = new EtkDisplayField(TABLE_DWARRAY, FIELD_DWA_ARRAYID, false, false);
            selectFields.addFeld(selectField);
            selectField = new EtkDisplayField(TABLE_DWARRAY, FIELD_DWA_FELD, false, false);
            selectFields.addFeld(selectField);
        }
    }

    protected void updateData() {
        selectFieldSet = null;
    }

    /**
     * Callback für searchSortAndFillWithJoin() erzeugen
     *
     * @param modelNumbers
     * @param counter
     * @param dataProducts
     * @param searchSaaCase
     * @param attribJoinMap
     * @return
     */
    protected EtkDataObjectList.FoundAttributesCallback createFoundAttributesCallback(
            final Set<String> modelNumbers, final VarParam<Integer> counter,
            Map<String, iPartsDataProductModels> allProductModels, Map<String, iPartsDataProduct> dataProducts,
            Set<iPartsEDSSaaCase> searchSaaCase, Set<String> usedModelNumbers, Map<String, EtkDataObject> attribJoinMap) {
        String today = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());

        final VarParam<String> lastModelAndSAA = new VarParam<>("");
        final VarParam<Long> lastAddResultsTime = new VarParam<>(System.currentTimeMillis());
        final VarParam<Long> lastUpdateResultsCountTime = new VarParam<>(System.currentTimeMillis());
        final VarParam<String> lastKey = new VarParam<>("");

        return new EtkDataObjectList.FoundAttributesCallback() {

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                counter.setValue(counter.getValue() + 1);
                if (searchCallback.searchWasCanceled()) {
                    return false;
                }
                if (isMBSEqualStartEndDate(attributes)) {
                    return false;
                }

                // Baumuster und/oder SAA haben sich geändert -> bisherige (nach Baumuster und SAA sortierte) Ergebnisse
                // anzeigen sobald es mindestens ROW_COUNT_FOR_ADD_RESULTS Ergebnisse gibt oder man schon mehr als
                // MAX_WAIT_TIME_MS_FOR_ADD_RESULTS warten musste
                String modelAndSAA = buildKey(attributes, modelAndSAAFieldNames);
                if (!modelAndSAA.equals(lastModelAndSAA.getValue())) {
                    lastModelAndSAA.setValue(modelAndSAA);
                    if ((attribJoinMap.size() >= MasterDataAbstractWorkBasketForm.ROW_COUNT_FOR_ADD_RESULTS)
                        || (!attribJoinMap.isEmpty() && ((System.currentTimeMillis() - lastAddResultsTime.getValue()) > MasterDataAbstractWorkBasketForm.MAX_WAIT_TIME_MS_FOR_ADD_RESULTS))) {
                        // Wird sonst erst zu spät weiter unten gemacht
                        preSelectSaaCase(lastKey.getValue(), today);
                        lastKey.setValue("");

                        if (!attribJoinMap.isEmpty()) {
                            searchCallback.addResults(false, usedModelNumbers, attribJoinMap);
                        }

                        // lastAddResultsTime erst nach der synchronen Ausführung setzen, um danach wieder MAX_WAIT_TIME_MS_FOR_ADD_RESULTS
                        // zu warten
                        lastAddResultsTime.setValue(System.currentTimeMillis());
                    }
                }

                // es können Treffer aus der MODEL_VALIDITY mit auftauchen => entfernen
                if (!isValidArrayId(attributes)) {
                    return false;
                }
                // Nur gültige SAA- oder BK-Nummern zulassen
                if (!isValidSaaBkNo(attributes)) {
                    return false;
                }
                if (!isMBSConditionValid(attributes)) {
                    return false;
                }
                if (!addProductModelAttributes(attributes, allProductModels, dataProducts)) {
                    return false;
                }

                // ArrayIds aufakkumulieren (KEM Dates werden bereits in der Vorverdichtung akkumuliert)
                String key = buildKey(attributes, keyFieldNames);
                EtkDataObject storedData = attribJoinMap.get(key);
                if (storedData == null) {
                    accumulateArrayIds(attributes, null, "");
                    // bereits gefundene/ abgehandelte BM aus der Liste entfernen damit am Ende bestimmt werden kann welche
                    // BM noch dazu gemischt werden müssen
                    String modelNo = getModelNumber(attributes);
                    if (modelNumbers != null) {
                        modelNumbers.remove(modelNo);
                    }
                    usedModelNumbers.add(modelNo);
                    preSelectSaaCase(lastKey.getValue(), today);

                    iPartsDataProductModels dataProductModels = new iPartsDataProductModels(project, null);
                    dataProductModels.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
                    attribJoinMap.put(key, dataProductModels);
                    resultCount++;
                    lastKey.setValue(key);
                    // wegen BM Verdichten nicht aufrufen
//                    searchCallback.showProgress(resultCount, lastUpdateResultsCountTime);
                } else {
                    accumulateArrayIds(attributes, storedData.getAttributes(), "");
                }
                return false;
            }

            private boolean preSelectSaaCase(String lastKey, String today) {
                boolean result = false;
                if (((searchSaaCase != null) && !searchSaaCase.isEmpty()) && !lastKey.isEmpty()) {
                    EtkDataObject lastDataObject = attribJoinMap.get(lastKey);
                    // ModelNo und saaBkNo müssen valide und Docu-relevant sein
                    String modelNo = getModelNumber(lastDataObject.getAttributes());
                    String saaBkNo = getSaaBkNo(lastDataObject.getAttributes());
                    if (StrUtils.isValid(modelNo, saaBkNo)) {
                        // Ist Model doku-relevant?
                        boolean isModelValid = getModelValidFlag(modelNo);
                        // Ist SA zur SAA doku-relevant?
                        boolean isSaaBkValid = getSaaBkValidFlag(saaBkNo);
                        if (!isModelValid || !isSaaBkValid) {
                            result = true;
                        }
                    } else {
                        result = true;
                    }
                    if (!result) {
                        String arrayId = getArrayId(lastDataObject.getAttributes());
                        int caseMatches = 0;
                        for (iPartsEDSSaaCase searchCase : searchSaaCase) {
                            switch (searchCase) {
                                case EDS_CASE_NEW:
                                    // im Retail benutzt?
                                    if (StrUtils.isValid(arrayId)) {
                                        // fliegt raus
                                        caseMatches++;
                                    } else {
                                        // nicht im Retail benutzt
                                        // valide saaBkNo bereits getestest
                                        // in NutzDok_SAA vorhanden?
                                        if (!existsSaaInNutzDok(saaBkNo, lastDataObject.getAttributes())) {
                                            // nicht in NutzDok_SAA => kann raus
                                            caseMatches++;
                                        }
                                    }
                                    break;
                                case EDS_CASE_VALIDITY_EXPANSION:
                                    if (!StrUtils.isValid(arrayId)) {
                                        // nicht im Retail benutzt => kann raus
                                        caseMatches++;
                                    } else {
                                        List<ArrayProductModelContainer> modelInvalidList = new DwList<>();
                                        String currentProductNo = getProductNo(lastDataObject.getAttributes());
                                        Map<String, List<ArrayProductModelContainer>> productModelMap = buildArrayProductModelContainer(arrayId, saaBkNo,
                                                                                                                                        modelNo, currentProductNo,
                                                                                                                                        attributesKatMap, null,
                                                                                                                                        modelInvalidList, null);
                                        // gibt es Verwendungen zum aktuellen Produkt?
                                        List<ArrayProductModelContainer> containerList = productModelMap.get(currentProductNo);
                                        if (containerList != null) {
                                            // Es sind Verwendungen im Retail für das aktuelle Produkt vorhanden
                                            // Prüfen, ob für keinen TU des Produkts Baumuster-Erweiterungen notwendig sind
                                            caseMatches++;
                                            if (!modelInvalidList.isEmpty()) {
                                                iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNo));
                                                if (isRealModelExtension(saaBkNo, model, modelInvalidList, lastDataObject.getAttributes(),
                                                                         today)) {
                                                    caseMatches--;
                                                }
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                        if (caseMatches == searchSaaCase.size()) {
                            result = true;
                        }
                    }
                    if (result) {
                        if (attribJoinMap.remove(lastKey) != null) {
                            resultCount--;
                        }
                    }
                }
                return result;
            }

            /**
             * Überprüfung, ob richtiges Array getroffen wurde
             *
             * @param attributes
             * @return
             */
            private boolean isValidArrayId(DBDataObjectAttributes attributes) {
                if (!withArrayJoin) {
                    attributes.addField(EtkDbConst.FIELD_DWA_ARRAYID, "", DBActionOrigin.FROM_DB);
                    attributes.addField(EtkDbConst.FIELD_DWA_FELD, "", DBActionOrigin.FROM_DB);
                }

                if (StrUtils.isValid(getArrayId(attributes))) {
                    String arrayName = getArrayFeld(attributes);
                    if (StrUtils.isValid(arrayName) && !arrayName.equals(TableAndFieldName.make(DBConst.TABLE_KATALOG, iPartsConst.FIELD_K_SA_VALIDITY))) {
                        return false;
                    }
                }
                return true;
            }

            /**
             * In der EDS_MODEL/DA_MODEL_ELEMENT_USAGE Tabelle stehen auch D-Baumuster drin
             * Es werden nur gültige SAA-oder Baukasten-Nummern ausgewertet
             *
             * @param attributes
             * @return
             */
            private boolean isValidSaaBkNo(DBDataObjectAttributes attributes) {
                String saaBkNo = getSaaBkNo(attributes);
                iPartsNumberHelper helper = new iPartsNumberHelper();
                return helper.isValidSaaOrBk(saaBkNo, false);
            }
        };
    }

    /**
     * Rechnet die ArrayIds zu KatalogData um und speichert sie in eine Map mit den dazugehörigen AssemblyIds als Key
     * Im Set werden die AssemblyIds gesammelt, die zu der angegeben Produkt-Nummer gehören
     * Die TUs sind alphabetisch sotiert
     *
     * @param idListe
     * @param masterProductNo
     * @param masterAssemblyIdSet Set mit dem zum Produkt gehörenden AssemblyIds
     * @param assemblyMap         Map mit AssemblyId zu PartListEntry unabhängig vom Produkt
     */
    protected void fillAssemblyToKatalogDataMapAndAssemblyIdSet(List<String> idListe, String masterProductNo, Set<AssemblyId> masterAssemblyIdSet,
                                                                Map<AssemblyId, KatalogData> assemblyMap) {
        Map<AssemblyId, KatalogData> katalogDataMap = new HashMap<>();
        boolean checkProduct = StrUtils.isValid(masterProductNo);

        // Umrechnung von ArrayId zu partListEntry
        for (String arrayId : idListe) {
            KatalogData katalogData = attributesKatMap.get(arrayId);
            if (katalogData != null) {
                AssemblyId assemblyId = katalogData.getPartListEntryId().getOwnerAssemblyId();
                KatalogData storedKatalogData = katalogDataMap.get(assemblyId);
                if (storedKatalogData != null) {
                    // gleicher TU, schon behandelt. Bestimme kleinste kLfdNr und merke sie
                    if (katalogData.getPartListEntryId().getKLfdnr().compareTo(storedKatalogData.getPartListEntryId().getKLfdnr()) < 0) {
                        katalogDataMap.put(assemblyId, katalogData);
                    }
                    continue;
                }

                katalogDataMap.put(assemblyId, katalogData);
                // suche die TU's, die dem angezeigten Produkt entsprechen
                if (checkProduct && !masterAssemblyIdSet.contains(assemblyId)) {
                    String productNoFromPartList = getProductNumberFromPLE(assemblyId, null, project);
                    if (masterProductNo.equals(productNoFromPartList)) {
                        masterAssemblyIdSet.add(assemblyId);
                    }
                }
            }
        }

        if (!katalogDataMap.isEmpty()) {
            List<KatalogData> katalogDataList = new DwList<>();
            katalogDataList.addAll(katalogDataMap.values());

            // Sortieren nach partListEntryId
            Collections.sort(katalogDataList, (KatalogData o1, KatalogData o2) -> {
                return o1.getPartListEntryId().compareTo(o2.getPartListEntryId());
            });

            // Umschaufeln in die Map
            for (KatalogData katalogData : katalogDataList) {
                AssemblyId assemblyId = katalogData.getPartListEntryId().getOwnerAssemblyId();
                if (assemblyMap.containsKey(assemblyId)) {
                    // nur ein Eintrag pro AssemblyId
                    continue;
                }
                assemblyMap.put(assemblyId, katalogData);
            }
        }
    }

    protected boolean addProductModelAttributes(DBDataObjectAttributes attributes, Map<String, iPartsDataProductModels> allProductModels, Map<String, iPartsDataProduct> dataProducts) {
        // absichtlich leer
        return true;
    }

    protected boolean isMBSConditionValid(DBDataObjectAttributes attributes) {
        return true;
    }

    protected boolean isMBSEqualStartEndDate(DBDataObjectAttributes attributes) {
        return false;
    }

    /**
     * JoinDatas für searchSortAndFillWithJoin() bilden
     *
     * @param searchSaaCase
     * @return
     */
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

    protected void addJoinDataIfNotNull(List<EtkDataObjectList.JoinData> helper, EtkDataObjectList.JoinData joinData) {
        if ((joinData != null) && (helper != null)) {
            helper.add(joinData);
        }
    }


    /**
     * neue JoinDatas pro Tabelle bilden
     *
     * @param searchSaaCase
     * @param tableName
     * @return
     */
    protected abstract EtkDataObjectList.JoinData getJoinData(Set<iPartsEDSSaaCase> searchSaaCase, String tableName);

    protected String[] mergeArrays(String[] array1, String[] array2) {
        if (array1 != null) {
            return StrUtils.mergeArrays(array1, array2);
        }
        return array2;
    }

    public void setSearchValuesAndFields(Map<String, String> searchValuesAndFields) {
        this.searchValuesAndFields = searchValuesAndFields;
    }

    public void addSourceToSearchValueAndFields(String origin) {
        searchValuesAndFields.put(FIELD_WSC_SOURCE, origin);
    }

    public String[] getWhereFields() {
        return searchValuesAndFields.keySet().toArray(new String[]{});
    }

    public String[] getWhereValues() {
        return searchValuesAndFields.values().toArray(new String[]{});
    }

    /**
     * Überprüft, ob sich nur SA-TU-ArrayIds in der arrayId befinden
     *
     * @param arrayId
     * @param attributesKatMap
     * @return
     */
    protected boolean areOnlySaTuIdsInArrayIds(String arrayId, Map<String, KatalogData> attributesKatMap) {
        for (String id : getArrayIdList(arrayId)) {
            KatalogData katalogData = attributesKatMap.get(id);
            if (katalogData != null) {
                if (!katalogData.partListEntryId.getOwnerAssemblyId().getKVari().startsWith(SA_MODULE_PREFIX)) {
                    return false;
                }
            } else {
                // Rückfallposition (sollte nie auftreten)
                if (!id.startsWith(SA_MODULE_PREFIX)) {
                    return false;
                }
            }
        }
        return true;
    }

    public DBDataObjectAttributes calculateVirtualFields(DBDataObjectAttributes attributes, Map<String, KatalogData> attributesKatMap,
                                                         Map<AssemblyId, String> assemblyProductMap, String today) {
        VarParam<iPartsDocuRelevantTruck> docuRel = new VarParam<>(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED);
        VarParam<iPartsEDSSaaCase> saaCase = new VarParam<>(iPartsEDSSaaCase.EDS_CASE_NOT_SPECIFIED);
        StringBuilder strDocuRelReason = new StringBuilder();
        String saaBkNo = getSaaBkNo(attributes);
        String arrayId = getArrayId(attributes);
        String productNo = getProductNo(attributes);
        String modelNo = getModelNumber(attributes);
        iPartsModel model = getModel(modelNo);
        boolean addAttributes = true;
        iPartsDocuRelevantTruck manualStatus = getManualStatus(modelNo, productNo, saaBkNo);
        boolean hasEtsExtension = hasEtsExtension(attributes);
        // Erstmal die Attributewerte sichern
        attributes = buildAttrClone(attributes, null, false);

        boolean manualStatusValid = wbh.isManualStatusValid(manualStatus, docuRel, strDocuRelReason);
        if (manualStatusValid) { // Manueller Status nicht gesetzt
            if (StrUtils.isValid(arrayId)) {
                // wird im Retail benutzt
                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_USED_IN_RETAIL);
                // damit erstmal 'dokumentiert'
                docuRel.setValue(iPartsDocuRelevantTruck.DOCU_DOCUMENTED_TRUCK);
            } else {
                // wird im Retail nicht benutzt
                // Ist Model doku-relevant?
                boolean isModelValid = getModelValidFlag(modelNo);
                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_NOT_USED_IN_RETAIL);
                if (StrUtils.isValid(saaBkNo)) {
                    // gültige SAA/BK-Nummer
                    if (!isModelValid) {
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MODEL_NOT_DOCU_REL, modelNo);
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    }
                    // Ist SA zur SAA doku-relevant?
                    boolean isSaaBkValid = getSaaBkValidFlag(saaBkNo);
                    if (!isSaaBkValid) {
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SAA_NOT_DOCU_REL,
                                                  iPartsNumberHelper.formatPartNo(project, saaBkNo));
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    }
                    if (isModelValid && isSaaBkValid) {
                        // Model und SA sind doku-relevant => erstmal DOCU_RELEVANT_EDS_YES
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES);

                        String kemToDate = getKemToDate(attributes);
                        String kemFromDate = getKemFromDate(attributes);
                        String validFrom = model.getValidFrom();
                        String validTo = model.getValidTo();
                        // Überprüfung des ZeitIntervall BM und SAA
                        checkTimeIntervall(strDocuRelReason, kemFromDate, kemToDate, validFrom, validTo, today, docuRel);
                        if (docuRel.getValue() == iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES) {
                            // Intervall-Überprüfung war positiv
                            // Ist Saa in NutzDok hinterlegt?
                            if (existsSaaInNutzDok(saaBkNo, attributes)) {
                                // setze Geschäftsfallauf Neu
                                saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_NEW);
                                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_USED_IN_NUTZDOK);
                            } else {
                                docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                            }
                        }
                    }
                } else {
                    // keine Saa/BK-Nummer
                    if (!isModelValid) {
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MODEL_NOT_DOCU_REL, modelNo);
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    }
                    wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SAA_NO_NOT_FOUND);
                    // Status bleibt auf NOT_SPECIFIED
                }
            }

            if (StrUtils.isValid(arrayId)) {
                // wird im Retail benutzt weitere Verfeinerung
                addAttributes = false;
                List<ArrayProductModelContainer> modelInvalidList = new DwList<>();
                List<ArrayProductModelContainer> saTUList = new DwList<>();
                String currentProductNo = getProductNo(attributes);
                // ordne Verwendungen Produktspezifisch (mit Überprüfung BM)
                Map<String, List<ArrayProductModelContainer>> productModelMap =
                        buildArrayProductModelContainer(arrayId, saaBkNo, modelNo, currentProductNo,
                                                        attributesKatMap,
                                                        strDocuRelReason, modelInvalidList, saTUList);

                // gibt es Verwendungen zum aktuellen Produkt?
                List<ArrayProductModelContainer> containerList = productModelMap.get(currentProductNo);
                // bilde Liste der restlichen Produkte für Ausgabe
                String products = buildProductList(productModelMap.keySet(), currentProductNo);
                DBDataObjectAttributes extraAttributes;
                boolean isDocumented = false;
                if (containerList != null) {
                    isDocumented = true;

                    // Prüfen, ob für keinen TU des Produkts Baumuster-Erweiterungen notwendig sind
                    if (!modelInvalidList.isEmpty()) {
                        isDocumented = !isRealModelExtension(saaBkNo, model, modelInvalidList, attributes, today);
                    }
                }

                if (isDocumented) {
                    // Es sind Verwendungen im Retail für das aktuelle Produkt vorhanden
                    extraAttributes = buildAttrClone(attributes, containerList, true);

                    ArrayProductModelContainer container = containerList.get(0);
                    if (StrUtils.isValid(products)) {
                        wbh.appendToDocuRelReason(container.strExtraDocuRelReason, WorkBasketHintMsgs.WBH_USED_ALSO_IN_OTHER_PRODUCTS, products);
                    }
                    // muss noch geklärt werden, ob so gewollt
//                        boolean isModelValid = getModelValidFlag(modelNo);
//                        if (isModelValid) {
//                            boolean isSaaBkValid = getSaaBkValidFlag(saaBkNo);
//                            if (isSaaBkValid) {
//                                // Ist Saa in NutzDok hinterlegt?
//                                if (existsSaaInNutzDok(saaBkNo)) {
//                                    // setze Geschäftsfallauf Neu
//                                    docuRel.setValue(iPartsDocuRelevantEDS.DOCU_RELEVANT_EDS_YES);
//                                    saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_NEW);
//                                    appendToDocuRelReason(container.strExtraDocuRelReason, "!!Eintrag in NutzDok");
//                                }
//                            } else {
//                                appendToDocuRelReason(container.strExtraDocuRelReason, "!!SAA/BK \"%1\" ist als nicht-dokurelevant gekennzeichnet",
//                                                      iPartsNumberHelper.formatPartNo(getProject(), saaBkNo));
//                            }
//                        } else {
//                            // das Model ist nicht Doku-relevant
//                            appendToDocuRelReason(container.strExtraDocuRelReason, "!!Baumuster \"%1\" ist als nicht-dokurelevant gekennzeichnet", modelNo);
//                        }
                    wbh.setAttribValue(extraAttributes, getFieldDocuRel(), docuRel.getValue().getDbValue());
                    wbh.setAttribValue(extraAttributes, getFieldSaaCase(), saaCase.getValue().getDbValue());
                    if (!saTUList.isEmpty()) {
                        // Meldung für SA-TUs übernehmen
                        wbh.appendToDocuRelReason(container.strExtraDocuRelReason, saTUList.get(0).strExtraDocuRelReason.toString());
                    }
                    wbh.setAttribValue(extraAttributes, getFieldDocuRelReason(), container.strExtraDocuRelReason.toString());
                } else {
                    if (modelInvalidList.isEmpty()) {
                        // es gibt keine Verwendungen zum aktuellen Produkt
                        extraAttributes = buildAttrClone(attributes, null, true);
                        if (!StrUtils.isValid(currentProductNo)) {
                            // Die Beziehung Product-Models ist nicht vorhanden
                            wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_NO_RELATED_PRODUCT_FOUND);
                            if (StrUtils.isValid(products)) {
                                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_USED_IN_OTHER_PRODUCTS, products);
                            }
                            docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                        } else {
                            if (StrUtils.isValid(products)) {
                                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_USED_ONLY_IN_OTHER_PRODUCTS, products);
                            }
                            boolean isModelValid = getModelValidFlag(modelNo);
                            if (!isModelValid) {
                                // das Model ist nicht Doku-relevant
                                wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MODEL_NOT_DOCU_REL, modelNo);
                                docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                            } else {
                                boolean isSaaBkValid = getSaaBkValidFlag(saaBkNo);
                                if (!isSaaBkValid) {
                                    wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_SAA_NOT_DOCU_REL,
                                                              iPartsNumberHelper.formatPartNo(project, saaBkNo));
                                    docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                                } else {
                                    // wird nur in anderen Produkten benutzt
                                    String kemToDate = getKemToDate(attributes);
                                    String kemFromDate = getKemFromDate(attributes);
                                    String validFrom = model.getValidFrom();
                                    String validTo = model.getValidTo();
                                    // Überprüfung des ZeitIntervall BM und SAA
                                    if (checkTimeIntervall(strDocuRelReason, kemFromDate, kemToDate, validFrom, validTo, today, docuRel)) {
                                        // Wenn nur SA-TUs in der Liste, dann bleibt der bisher berechnete Status erhalten
                                        if (saTUList.isEmpty() || !areOnlySaTuIdsInArrayIds(arrayId, attributesKatMap)) {
                                            // Nicht nur SA-TUs in den ArrayIds
                                            docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES);
                                            saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_SAA_VALIDITY_EXPANSION);
                                        }
                                        if (!saTUList.isEmpty()) {
                                            // Meldung für SA-TUs übernehmen
                                            wbh.appendToDocuRelReason(strDocuRelReason, saTUList.get(0).strExtraDocuRelReason.toString());
                                        }
                                    }
                                }
                            }
                        }

                        wbh.setAttribValue(extraAttributes, getFieldDocuRel(), docuRel.getValue().getDbValue());
                        wbh.setAttribValue(extraAttributes, getFieldSaaCase(), saaCase.getValue().getDbValue());
                        wbh.setAttribValue(extraAttributes, getFieldDocuRelReason(), strDocuRelReason.toString());
                        wbh.setAttribValue(extraAttributes, FIELD_DWA_ARRAYID, "");
                        wbh.setAttribValue(extraAttributes, FIELD_DPM_PRODUCT_NO, currentProductNo);
                    } else {
                        // es gibt Verwendungen zum aktuellen Produkt, jedoch passt das Model nicht zur Validity
                        extraAttributes = buildAttrClone(attributes, modelInvalidList, true);
                        StringBuilder strExtraDocuRelReason = modelInvalidList.get(0).strExtraDocuRelReason;
                        if (!StrUtils.isValid(currentProductNo)) {
                            // Die Beziehung Product-Models ist nicht vorhanden
                            wbh.appendToDocuRelReason(strExtraDocuRelReason, WorkBasketHintMsgs.WBH_NO_RELATED_PRODUCT_FOUND);
                            if (StrUtils.isValid(products)) {
                                wbh.appendToDocuRelReason(strExtraDocuRelReason, WorkBasketHintMsgs.WBH_USED_IN_OTHER_PRODUCTS, products);
                            }
                            docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NOT_SPECIFIED);
                        } else {
                            boolean isModelValid = getModelValidFlag(modelNo);
                            if (!isModelValid) {
                                // das Model ist nicht Doku-relevant
                                wbh.appendToDocuRelReason(strExtraDocuRelReason, WorkBasketHintMsgs.WBH_MODEL_NOT_DOCU_REL, modelNo);
                                docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                            } else {
                                // wird nur in anderen Produkten benutzt
                                String kemToDate = getKemToDate(attributes);
                                String kemFromDate = getKemFromDate(attributes);
                                String validFrom = model.getValidFrom();
                                String validTo = model.getValidTo();
                                // Überprüfung des ZeitIntervall BM und SAA
                                if (checkTimeIntervall(strExtraDocuRelReason, kemFromDate, kemToDate, validFrom, validTo, today, docuRel)) {
                                    // Wenn nur SA-TUs in der Liste, dann bleibt der bisher berechnete Status erhalten
                                    if (saTUList.isEmpty() || !areOnlySaTuIdsInArrayIds(arrayId, attributesKatMap)) {
                                        // Nicht nur SA-TUs in den ArrayIds
                                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES);
                                        saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_MODEL_VALIDITY_EXPANSION);
                                    }
                                    if (!saTUList.isEmpty()) {
                                        // Meldung für SA-TUs übernehmen
                                        wbh.appendToDocuRelReason(strExtraDocuRelReason, saTUList.get(0).strExtraDocuRelReason.toString());
                                    }
                                }
                            }
                        }

                        wbh.setAttribValue(extraAttributes, getFieldDocuRel(), docuRel.getValue().getDbValue());
                        wbh.setAttribValue(extraAttributes, getFieldSaaCase(), saaCase.getValue().getDbValue());
                        wbh.setAttribValue(extraAttributes, getFieldDocuRelReason(), strExtraDocuRelReason.toString());
                    }

                }
                wbh.setAttribValue(extraAttributes, getFieldModelStatus(), getModelValidFlag(modelNo));
                wbh.setAttribValue(extraAttributes, getFieldSaaBkStatus(), getSaaBkValidFlag(saaBkNo));
                wbh.setAttribValue(extraAttributes, getFieldManualStatus(), manualStatus.getDbValue());
                wbh.setAttribValue(extraAttributes, getFieldAuthorOrder(), "");
                // wegen DAIMLER-10196 wieder alle Verwendungen setzen
                wbh.setAttribValue(extraAttributes, FIELD_DWA_ARRAYID, arrayId);
                attributes = extraAttributes;
            }
        }
        // Falls die Doku-Relevanz nicht DOCU_DOCUMENTED_TRUCK und die SAA/BK-Nummer gültig ist, in der Datenstruktur von
        // den ChangeSet-Einträgen nach Autoren-Aufträgen suchen, die zum Produkt, SAA/BK-Nummer und Baumuster passen
        addAuthorNames(attributes, saaBkNo, productNo, modelNo, docuRel, saaCase,
                       manualStatus, addAttributes, strDocuRelReason);

//        // DAIMLER-10346 existieren Bemerkungen in NutzDok Remarks mit dieser SAA
        addAdditionalAttributeValues(attributes, saaBkNo, hasEtsExtension, manualStatus);
        if (manualStatusValid) {
            wbh.checkProductStatus(attributes, productNo, getFieldDocuRel(), getFieldSaaCase(), getFieldDocuRelReason());
        }
        return attributes;
    }

    protected void addAuthorNames(DBDataObjectAttributes attributes, String saaBkNo, String productNo, String modelNo,
                                  VarParam<iPartsDocuRelevantTruck> docuRel, VarParam<iPartsEDSSaaCase> saaCase,
                                  iPartsDocuRelevantTruck manualStatus, boolean addAttributes, StringBuilder strDocuRelReason) {
        // Falls die Doku-Relevanz nicht DOCU_DOCUMENTED_TRUCK und die SAA/BK-Nummer gültig ist, in der Datenstruktur von
        // den ChangeSet-Einträgen nach Autoren-Aufträgen suchen, die zum Produkt, SAA/BK-Nummer und Baumuster passen
        String authorOrderNames = "";
        // bei allen DocuRel-Stati überprüfen, ob es einen Autoren-Auftrag gibt
        if ((docuRel.getValue() != iPartsDocuRelevantTruck.DOCU_DOCUMENTED_TRUCK) && StrUtils.isValid(saaBkNo)) {
            Set<WorkBasketAuthorOrderData> authorOrderDataSet = getWorkBasketAuthorOrderData(productNo, saaBkNo, modelNo);
            if (authorOrderDataSet != null) {
                Set<String> authorOrderNamesSet = new TreeSet<>();
                for (WorkBasketAuthorOrderData workBasketAuthorOrderData : authorOrderDataSet) {
                    if (workBasketAuthorOrderData.isModelValidityMatches()) {
                        authorOrderNamesSet.add(workBasketAuthorOrderData.getAuthorOrderName());
                    }
                }

                // Es gibt Autoren-Aufträge mit Stücklisteneinträgen für das Produkt, SAA/BK und Baumuster
                if (!authorOrderNamesSet.isEmpty()) {
                    docuRel.setValue(iPartsDocuRelevantTruck.DOCU_DOCUMENTED_IN_AUTHOR_ORDER_TRUCK);
                    saaCase.setValue(iPartsEDSSaaCase.EDS_CASE_NOT_SPECIFIED);
                    wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_USED_IN_NOT_RELEASED_ORDERS);
                    authorOrderNames = StrUtils.stringListToString(authorOrderNamesSet, ", ");

                    if (!addAttributes) { // Bei addAttributes werden die Attribute unten gesetzt
                        wbh.setAttribValue(attributes, getFieldDocuRel(), docuRel.getValue().getDbValue());
                        wbh.setAttribValue(attributes, getFieldDocuRelReason(), strDocuRelReason.toString());
                        wbh.setAttribValue(attributes, getFieldAuthorOrder(), authorOrderNames);
                    }
                }
            }
        }
        if (addAttributes) {
            wbh.setAttribValue(attributes, getFieldDocuRel(), docuRel.getValue().getDbValue());
            wbh.setAttribValue(attributes, getFieldSaaCase(), saaCase.getValue().getDbValue());
            wbh.setAttribValue(attributes, getFieldDocuRelReason(), strDocuRelReason.toString());
            wbh.setAttribValue(attributes, getFieldPartEntryId(), "");

            wbh.setAttribValue(attributes, getFieldModelStatus(), getModelValidFlag(modelNo));
            wbh.setAttribValue(attributes, getFieldSaaBkStatus(), getSaaBkValidFlag(saaBkNo));
            wbh.setAttribValue(attributes, getFieldManualStatus(), manualStatus.getDbValue());
            wbh.setAttribValue(attributes, getFieldAuthorOrder(), authorOrderNames);
        }

    }

    protected void addAdditionalAttributeValues(DBDataObjectAttributes attributes, String saaBkNo, boolean hasEtsExtension,
                                                iPartsDocuRelevantTruck manualStatus) {
        // DAIMLER-10346 existieren Bemerkungen in NutzDok Remarks mit dieser SAA
        boolean remarksToSaaExists = nutzDokRemarkHelper.isNutzDokRemarkRefIdAvailabe(saaBkNo);
        wbh.setAttribValue(attributes, getFieldSaaNutzdokRemarksAvailable(), remarksToSaaExists);

        boolean hasInternalText = WorkBasketInternalTextCache.hasInternalText(project, getWbType(), saaBkNo);
        wbh.setAttribValue(attributes, getVirtualFieldNameInternalTextAvailable(), hasInternalText);
        String firstIntText;
        if (hasInternalText) {
            firstIntText = WorkBasketInternalTextCache.getFirstInternalText(project, getWbType(), saaBkNo, true, isExport);
        } else {
            firstIntText = "";
        }
        wbh.setAttribValue(attributes, getVirtualFieldNameInternalText(), firstIntText);
//        attributes.addField(getVirtualFieldNameInternalText(), firstIntText, true, DBActionOrigin.FROM_DB);
        // Attribut für den Wiedervorlage-Termin hinzufügen
        WorkBasketInternalTextCache.addFollowUpDateAttribute(project, attributes, getWbType(), saaBkNo);
        wbh.setAttribValue(attributes, getVirtualFieldNameEtsExtension(), hasEtsExtension);

        if (hasEtsExtension) {
            // Statusänderung durch ET-Sichtenerweiterung nur falls der manueller Status = DOCU_RELEVANT_TRUCK_NO (NR) ist
            if (manualStatus == iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO) {
                wbh.setAttribValue(attributes, getFieldDocuRel(), iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_YES.getDbValue());
                wbh.setAttribValue(attributes, getFieldSaaCase(), iPartsEDSSaaCase.EDS_CASE_VALIDITY_EXPANSION.getDbValue());
            }
            wbh.appendToDocuRelReason(attributes, getFieldDocuRelReason(), WorkBasketHintMsgs.WBH_ET_VIEW_EXTENSION);
        }
    }

    /**
     * zukünftige Funktion
     *
     * @param modelNo
     * @param saaBkNo
     * @return
     */
    protected iPartsDocuRelevantTruck getManualStatus(String modelNo, String productNo, String saaBkNo) {
        return stateManager.getManualDocuRel(modelNo, productNo, saaBkNo);
    }

    /**
     * ArrayIds aufakkumulieren (SAA_VALIDITY)
     *
     * @param currentAttributes
     * @param storedAttributes
     * @param searchProductNo
     */
    private void accumulateArrayIds(DBDataObjectAttributes currentAttributes, DBDataObjectAttributes storedAttributes,
                                    String searchProductNo) {
        String currentArrayId = getArrayId(currentAttributes);
        PartListEntryId partListEntryId = getPartListEntryIdFromArrayId(currentArrayId);
        if (removeForeignProducts) {
            if (partListEntryId != null) {
                String usedProductNo = getProductNumberFromPLE(partListEntryId.getOwnerAssemblyId(), null, project);
                if (StrUtils.isValid(searchProductNo)) {
                    if (!searchProductNo.equals(usedProductNo)) {
                        // ArrayId zeigt in anderes Product => löschen
                        setArrayId(currentAttributes, "");
                    }
                }
            }
        }
        if (storedAttributes != null) {
            if (partListEntryId != null) {
                String storedArrayId = getArrayId(storedAttributes);

                // Schnellprüfung, ob die Array-ID schon in storedArrayId enthalten ist -> falls nicht, dann diese Array-ID
                // zur storedArrayId hinzufügen
                // Diese seltsame Art der Prüfung OHNE Verwendung von getArrayIdList() und getArrayIdFromList() ist notwendig,
                // da diese Methoden bei der Häufigkeit des Aufrufs von accumulateArrayIds() Unmengen an temporären Byte[]-Daten
                // erzeugen, was die GC extrem belastet
                if (StrUtils.isValid(storedArrayId) && !storedArrayId.equals(currentArrayId) && !storedArrayId.startsWith(currentArrayId + MasterDataAbstractWorkBasketForm.ARRAYID_DELIMITER)
                    && !storedArrayId.contains(MasterDataAbstractWorkBasketForm.ARRAYID_DELIMITER + currentArrayId + MasterDataAbstractWorkBasketForm.ARRAYID_DELIMITER)
                    && !storedArrayId.endsWith(MasterDataAbstractWorkBasketForm.ARRAYID_DELIMITER + currentArrayId)) {
                    setArrayId(storedAttributes, storedArrayId + MasterDataAbstractWorkBasketForm.ARRAYID_DELIMITER + currentArrayId); // storedArrayId kann nicht leer sein
                }
            }
        } else {
            if (partListEntryId == null) {
                // ArrayId zeigt in anderes Product => löschen
                setArrayId(currentAttributes, "");
            }
        }
    }

    protected List<String> getArrayIdList(String arrayId) {
        return StrUtils.toStringList(arrayId, MasterDataAbstractWorkBasketForm.ARRAYID_DELIMITER, false, false);
    }

    protected String getArrayIdFromList(List<String> idListe) {
        return StrUtils.stringListToString(idListe, MasterDataAbstractWorkBasketForm.ARRAYID_DELIMITER);
    }

    /**
     * Ermittelt die Produktnummer für die übergebene {@link AssemblyId} und optionale {@code changeSetGUID} für Module aus
     * ChangeSets.
     *
     * @param assemblyId
     * @param changeSetGUID
     * @param project
     * @return
     */
    protected String getProductNumberFromPLE(AssemblyId assemblyId, String changeSetGUID, EtkProject project) {
        if (assemblyId.getKVari().startsWith(SA_MODULE_PREFIX)) { // Sonderbehandlung für SA-TUs
            String productNumber = assemblyProductMap.get(assemblyId);
            if (productNumber == null) {
                // Falls der SA-TU im gleichen Autoren-Auftrag verortet wurde, kann das Produkt bestimmt werden
                String saNumber = StrUtils.removeCharsFromString(assemblyId.getKVari(), SA_MODULE_PREFIX.toCharArray());
                iPartsDataChangeSetEntryList bindModulesInCurrentChangeset = new iPartsDataChangeSetEntryList();
                if (StrUtils.isValid(changeSetGUID)) {
                    // nur suchen, wenn ChangeSet-GUID angegeben
                    bindModulesInCurrentChangeset.searchAndFill(project, TABLE_DA_CHANGE_SET_ENTRY,
                                                                new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_TYPE },
                                                                new String[]{ changeSetGUID, iPartsProductSAsId.TYPE },
                                                                DBDataObjectList.LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
                    if (!bindModulesInCurrentChangeset.isEmpty()) {
                        // den richtigen Eintrag suchen
                        for (iPartsDataChangeSetEntry bindEntry : bindModulesInCurrentChangeset) {
                            IdWithType productModulesObjectId = bindEntry.getAsId().getDataObjectIdWithType();
                            iPartsProductSAsId productSAId = IdWithType.fromStringArrayWithTypeFromClass(
                                    iPartsProductSAsId.class, productModulesObjectId.toStringArrayWithoutType());
                            if (productSAId.getSaNumber().equals(saNumber)) {
                                // gefunden und fertig
                                productNumber = productSAId.getProductNumber();
                                assemblyProductMap.put(assemblyId, productNumber);
                                break;
                            }
                        }
                    }
                }
            }
            return productNumber;
        }

        // Produktnummer über Modulnummer (Verwendung im Produkt) bestimmen mit Cache
        String productNumber = assemblyProductMap.get(assemblyId);
        if (productNumber == null) {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
            if (assembly instanceof iPartsDataAssembly) {
                iPartsProductId productId = ((iPartsDataAssembly)assembly).getProductIdFromModuleUsage();
                if (productId != null) {
                    productNumber = productId.getProductNumber();
                } else {
                    productNumber = "";
                    if (StrUtils.isValid(changeSetGUID)) {
                        // Falls auf diesem Weg kein Produkt bestimmt werden konnte, gehen wir davon aus, dass
                        // das Modul erst im Autorenauftrag angelegt wurde, d.h. wir suchen mit der ChangesetGUID
                        // nach neuen ProductModulesIds
                        iPartsDataChangeSetEntryList createdModulesInCurrentChangeset = new iPartsDataChangeSetEntryList();
                        createdModulesInCurrentChangeset.searchAndFill(project, TABLE_DA_CHANGE_SET_ENTRY,
                                                                       new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_TYPE },
                                                                       new String[]{ changeSetGUID, iPartsProductModulesId.TYPE },
                                                                       DBDataObjectList.LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);

                        for (iPartsDataChangeSetEntry moduleEntry : createdModulesInCurrentChangeset) {
                            IdWithType productModulesObjectId = moduleEntry.getAsId().getDataObjectIdWithType();
                            iPartsProductModulesId productModulesId = IdWithType.fromStringArrayWithTypeFromClass(
                                    iPartsProductModulesId.class, productModulesObjectId.toStringArrayWithoutType());

                            if (productModulesId.getModuleNumber().equals(assemblyId.getKVari())) {
                                // nur wenn die Modulnummer übereinstimmt haben wir den richtigen Eintrag gefunden
                                productNumber = productModulesId.getProductNumber();
                                break;
                            }
                        }
                    }
                }
                assemblyProductMap.put(assemblyId, productNumber);
            }
        }
        return productNumber;
    }

    /**
     * {@link PartListEntryId} aus SA_VALIDITY-ArrayId bestimmen und merken
     *
     * @param arrayId
     * @return
     */
    private PartListEntryId getPartListEntryIdFromArrayId(String arrayId) {
        if (StrUtils.isValid(arrayId)) {
            PartListEntryId partListEntryId = null;
            KatalogData katalogData = attributesKatMap.get(arrayId);
            if (katalogData == null) {
                if (!attributesKatMap.containsKey(arrayId)) {
                    String[] selectFields = new String[]{ FIELD_K_VARI, FIELD_K_LFDNR, FIELD_K_MODEL_VALIDITY };
                    String[] whereFields = new String[]{ FIELD_K_SA_VALIDITY };
                    String[] whereValues = new String[]{ arrayId };
                    DBDataObjectAttributes attributesKat = project.getDbLayer().getAttributes(TABLE_KATALOG, selectFields,
                                                                                              whereFields, whereValues);
                    if ((attributesKat != null) && !attributesKat.isEmpty()) {
                        partListEntryId = new PartListEntryId(attributesKat.getFieldValue(FIELD_K_VARI), "", attributesKat.getFieldValue(FIELD_K_LFDNR));
                        attributesKatMap.put(arrayId, new KatalogData(partListEntryId, attributesKat.getFieldValue(FIELD_K_MODEL_VALIDITY)));
                    } else {
                        // Kein Treffer für die Array-ID (kann eigentlich nicht sein bei korrekten Daten)
                        attributesKatMap.put(arrayId, null);
                    }
                }
            } else {
                partListEntryId = katalogData.partListEntryId;
            }
            return partListEntryId;
        }
        return null;
    }

    /**
     * ordne Verwendungen Produktspezifisch (mit Überprüfung BM)
     * Pro ArrayId wird über den Stücklisteneintrag das Produkt bestimmt.
     * Handelt es sich um die {@param currentProductNo} wird zusätzlich die BM-Gültigkeit überprüft.
     * Ist das BM nicht im Validitycheck, dann wird die ArrayId in die Liste {@param modelInvalidList} einsortiert
     *
     * @param arrayId
     * @param saaBkNo
     * @param modelNo
     * @param currentProductNo
     * @param attributesKatMap
     * @param strDocuRelReason
     * @param modelInvalidList
     * @param saTuList         Liste der SA-TUs
     * @return
     */
    protected Map<String, List<ArrayProductModelContainer>> buildArrayProductModelContainer(String arrayId, String saaBkNo,
                                                                                            String modelNo, String currentProductNo,
                                                                                            Map<String, KatalogData> attributesKatMap,
                                                                                            StringBuilder strDocuRelReason,
                                                                                            List<ArrayProductModelContainer> modelInvalidList,
                                                                                            List<ArrayProductModelContainer> saTuList) {
        Map<String, List<ArrayProductModelContainer>> productModelMap = new HashMap<>();
        modelInvalidList.clear();
        Map<AssemblyId, List<ArrayProductModelContainer>> assemblyIdProductModelMap = new HashMap<>();
        List<String> idListe = getArrayIdList(arrayId);
        for (String id : idListe) {
            ArrayProductModelContainer container = new ArrayProductModelContainer(id, strDocuRelReason, project);
            if (container.isProductValid(attributesKatMap)) {
                boolean doAdd = true;
                if (container.productNoFromPartList.equals(currentProductNo)) {
                    if (!container.isModelUsed(modelNo, attributesKatMap)) {
                        modelInvalidList.add(container);
                        doAdd = false;
                    }
                } else {
                    // Baumusterüberprüfung auf jeden Fall durchführen
                    container.isModelUsed(modelNo, attributesKatMap);
                }
                if (doAdd) {
                    if (container.isSaTu()) {
                        // es ist ein SA-TU => Meldung und in saTuList ablegen
                        container.strExtraDocuRelReason = new StringBuilder();
                        wbh.appendToDocuRelReason(container.strExtraDocuRelReason, WorkBasketHintMsgs.WBH_RETAIL_USAGE_IN_SATU,
                                                  container.katalogData.getPartListEntryId().getOwnerAssemblyId().getKVari());
                        if (saTuList != null) {
                            saTuList.add(container);
                        }
                        continue;
                    }
                    // 'normaler' TU
                    List<ArrayProductModelContainer> containerList = productModelMap.computeIfAbsent(container.productNoFromPartList,
                                                                                                     key -> new DwList<>());
                    containerList.add(container);
                }

                PartListEntryId partListEntryId = container.katalogData.getPartListEntryId();
                if (partListEntryId != null) {
                    AssemblyId assemblyId = partListEntryId.getOwnerAssemblyId();
                    List<ArrayProductModelContainer> assemblyContainerList = assemblyIdProductModelMap.computeIfAbsent(assemblyId,
                                                                                                                       key -> new DwList());
                    assemblyContainerList.add(container);
                }
            } else {
                // kein Produkt gefunden => wenn es sich um SA-TU handelt in saTuList ablegen
                if (container.isSaTu()) {
                    container.strExtraDocuRelReason = new StringBuilder();
                    wbh.appendToDocuRelReason(container.strExtraDocuRelReason, WorkBasketHintMsgs.WBH_RETAIL_USAGE_IN_SATU,
                                              container.katalogData.getPartListEntryId().getOwnerAssemblyId().getKVari());
                    if (saTuList != null) {
                        saTuList.add(container);
                    }
                }
            }
        }

        if (!assemblyIdProductModelMap.isEmpty()) {
            for (List<ArrayProductModelContainer> productModelContainers : assemblyIdProductModelMap.values()) {
                boolean isRealModelExtension = true;
                for (ArrayProductModelContainer container : productModelContainers) {
                    if (container.modelValidityIsEmpty) {
                        isRealModelExtension = false;
                        break;
                    }
                    if (container.modelIsExplicitInValidity) {
                        isRealModelExtension = false;
                        break;
                    }
                }
                for (ArrayProductModelContainer container : productModelContainers) {
                    container.katalogData.setTUModelValidityExtension(saaBkNo, modelNo, isRealModelExtension);
                }
            }
        }
        return productModelMap;
    }


    protected Set<WorkBasketAuthorOrderData> getWorkBasketAuthorOrderData(String productNo, String saaBkNo, String modelNo) {
        Map<String, Map<String, Map<String, WorkBasketAuthorOrderData>>> saasToAuthorOrderNamesMaps = workBasketItemToAuthorOrdersMap.get(productNo);
        if (saasToAuthorOrderNamesMaps != null) { // SAA/BK-Gültigkeiten für das Produkt
            Map<String, Map<String, WorkBasketAuthorOrderData>> modelsToAuthorOrderNamesMaps = saasToAuthorOrderNamesMaps.get(saaBkNo);
            if (modelsToAuthorOrderNamesMaps != null) { // Baumuster-Gültigkeiten für das Produkt und SAA/BK
                Set<WorkBasketAuthorOrderData> authorOrderDataSet = null; // Stücklisteneinträge ohne Baumuster-Gültigkeit
                Map<String, WorkBasketAuthorOrderData> authorOrderDataMap = modelsToAuthorOrderNamesMaps.get("");
                if (authorOrderDataMap != null) {
                    authorOrderDataSet = new TreeSet<>(authorOrderDataMap.values());
                }
                if (StrUtils.isValid(modelNo)) {
                    // Stücklisteneinträge mit passender Baumuster-Gültigkeit
                    Map<String, WorkBasketAuthorOrderData> authorOrderDataMapWithModelValidity = modelsToAuthorOrderNamesMaps.get(modelNo);
                    if (authorOrderDataMapWithModelValidity != null) {
                        if (authorOrderDataSet != null) {
                            authorOrderDataSet.addAll(authorOrderDataMapWithModelValidity.values());
                        } else {
                            authorOrderDataSet = new TreeSet<>(authorOrderDataMapWithModelValidity.values());
                        }
                    }
                }
                return authorOrderDataSet;
            }
        }

        return null;
    }

    public FrameworkRunnable createChangeSetSearchThread(Map<AssemblyId, String> assemblyProductMap, String productNumberSearchValue, String saaSearchValue,
                                                         String modelNumberSearchValue, iPartsImportDataOrigin source) {
        return new FrameworkRunnable() {

            @Override
            public void run(FrameworkThread thread) {
                doSearchChangesets(assemblyProductMap, productNumberSearchValue, saaSearchValue, modelNumberSearchValue, source);
            }
        };
    }

    public void doSearchChangesets(Map<AssemblyId, String> assemblyProductMap, String productNumberSearchValue, String saaSearchValue,
                                   String modelNumberSearchValue, iPartsImportDataOrigin source) {
        if (StrUtils.stringContainsWildcards(productNumberSearchValue)) {
            return; // Fehlermeldung kommt im anderen Thread
        }

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_GUID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_NAME, false, false));

        iPartsDataChangeSetEntryList changeSetEntryList = new iPartsDataChangeSetEntryList();
        changeSetEntryList.searchSortAndFillWithJoin(project, null, selectFields,
                                                     new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_TYPE),
                                                                   TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_EDIT_INFO) },
                                                     new String[]{ PartListEntryId.TYPE, CHANGE_SET_ENTRY_EDIT_INFO.SAA_WORK_BASKET_RELEVANT.name() },
                                                     new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET, FIELD_DCS_STATUS) },
                                                     new String[]{ iPartsChangeSetStatus.COMMITTED.name() },
                                                     false, null, false, false, null,
                                                     new EtkDataObjectList.JoinData(TABLE_DA_CHANGE_SET,
                                                                                    new String[]{ FIELD_DCE_GUID },
                                                                                    new String[]{ FIELD_DCS_GUID },
                                                                                    false, false),
                                                     new EtkDataObjectList.JoinData(TABLE_DA_AUTHOR_ORDER,
                                                                                    new String[]{ FIELD_DCE_GUID },
                                                                                    new String[]{ FIELD_DAO_CHANGE_SET_ID },
                                                                                    false, false));
        if (!changeSetEntryList.isEmpty()) {
            SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();

            // SAA-Gültigkeit auslesen
            String saaSearchMask = saaSearchValue;
            if (StrUtils.isValid(saaSearchMask)) {
                saaSearchMask = numberHelper.unformatSaaBkForEdit(project, saaSearchMask);

                // TODO Einschränkung auf MBS entfernen sobald auch in EDS "Z0*" durch "Z *" ersetzt wurde
                if (source == iPartsImportDataOrigin.SAP_MBS) {
                    // Unterschiedliche Retail-SAA-Nummer vorhanden für die Konstruktions-SAA-Nummer?
                    String retailSAA = numberHelper.getDifferentRetailSAA(saaSearchMask);
                    if (retailSAA != null) {
                        saaSearchMask = retailSAA;
                    }
                }

                saaSearchMask = StrUtils.addCharacterIfLastCharacterIsNot(saaSearchMask, '*');
            }

            // Baumustergültigkeiten auslesen
            String modelNumberSearchMask = modelNumberSearchValue;
            if (StrUtils.isValid(modelNumberSearchMask)) {
                modelNumberSearchMask = StrUtils.addCharacterIfLastCharacterIsNot(modelNumberSearchMask, '*');
            }

            for (iPartsDataChangeSetEntry dataChangeSetEntry : changeSetEntryList) {
                IdWithType objectId = dataChangeSetEntry.getAsId().getDataObjectIdWithType();
                PartListEntryId partListEntryId = IdWithType.fromStringArrayWithTypeFromClass(PartListEntryId.class,
                                                                                              objectId.toStringArrayWithoutType());
                String changeSetGUID = dataChangeSetEntry.getAsId().getGUID();
                String productNumber = getProductNumberFromPLE(partListEntryId.getOwnerAssemblyId(), changeSetGUID, project);
                if (StrUtils.isEmpty(productNumber)) { // Stücklisteneintrag ohne Produkt ignorieren
                    continue;
                }

                // Kann auf das gesuchte Produkt eingeschränkt werden?
                if (StrUtils.isValid(productNumberSearchValue) && !productNumberSearchValue.equals(productNumber)) {
                    continue;
                }

                // Wenn das Produkt passt (oder es keinen Suchwert dafür gibt), dann muss das JSON vom ChangeSetEntry
                // in ein SerializedDBDataObject umgewandelt und daraus die SAA- sowie Baumuster-Gültigkeiten ausgelesen
                // werden
                // Dazu das BLOB-Feld DCE_CURRENT_DATA zunächst explizit als Attribut hinzufügen, damit der BLOB dann
                // nachgeladen werden kann (dadurch wird das komplette Nachladen aller Attribute über DBDataObject.loadFieldIfNeeded()
                // vermieden); direkt im JOIN würden unnötig viele BLOBs geladen werden müssen
                dataChangeSetEntry.getAttributes().addField(new DBDataObjectAttribute(FIELD_DCE_CURRENT_DATA, DBDataObjectAttribute.TYPE.BLOB,
                                                                                      false, false), DBActionOrigin.FROM_DB);
                String currentData = dataChangeSetEntry.getFieldValueAsStringFromZippedBlob(FIELD_DCE_CURRENT_DATA);
                SerializedDBDataObject serializedDBDataObject = serializedDbDataObjectAsJSON.getFromJSON(currentData);
                VarParam<EtkDataPartListEntry> partListEntry = new VarParam<>(null);

                // SAA-Gültiglkeit überprüfen
                VarParam<Collection<String>> saaValidities = new VarParam<>(null);
                if (!getAndCheckArrayAttribute(FIELD_K_SA_VALIDITY, saaValidities, serializedDBDataObject,
                                               partListEntryId, partListEntry, saaSearchMask, project)) {
                    continue; // SAA passt nicht
                }
                if (saaValidities.getValue() == null) {
                    continue; // Ohne SAA-Gültigkeiten macht der ChangeSetEntry im SAA-Arbeitsvorrat keinen Sinn
                }

                // Baumustergültigkeiten überprüfen
                VarParam<Collection<String>> modelValidities = new VarParam<>(null);
                boolean modelValidityMatches = getAndCheckArrayAttribute(FIELD_K_MODEL_VALIDITY, modelValidities,
                                                                         serializedDBDataObject, partListEntryId,
                                                                         partListEntry, modelNumberSearchMask,
                                                                         project);

                // ChangeSetEntry passt zu den Suchkriterien -> Einträge in der Map machen
                String authorOrderName = dataChangeSetEntry.getFieldValue(FIELD_DAO_NAME);
                Map<String, Map<String, Map<String, WorkBasketAuthorOrderData>>> saasToAuthorOrderNamesMap = workBasketItemToAuthorOrdersMap.computeIfAbsent(productNumber,
                                                                                                                                                             key -> new HashMap<>());
                for (String saaValidity : saaValidities.getValue()) {
                    // Map-Eintrag pro passender SAA-Gültigkeit
                    Map<String, Map<String, WorkBasketAuthorOrderData>> modelsToAuthorOrderNamesMap = saasToAuthorOrderNamesMap.computeIfAbsent(saaValidity,
                                                                                                                                                key -> new HashMap<>());
                    if (modelValidities.getValue() != null) { // Baumuster-Gültigkeiten vorhanden
                        for (String modelValidity : modelValidities.getValue()) {
                            // Map-Eintrag pro passender Baumuster-Gültigkeit
                            addWorkBasketItemToAuthorOrderEntry(partListEntryId, changeSetGUID, authorOrderName,
                                                                modelValidity, modelValidityMatches, modelsToAuthorOrderNamesMap);
                        }

                        // Dummy-Eintrag für die Gültigkeit für alle Baumuster (aber mit modelValidityMatches = false)
                        addWorkBasketItemToAuthorOrderEntry(partListEntryId, changeSetGUID, authorOrderName, "",
                                                            false, modelsToAuthorOrderNamesMap);
                    } else { // Dummy-Eintrag für die Gültigkeit für alle Baumuster
                        addWorkBasketItemToAuthorOrderEntry(partListEntryId, changeSetGUID, authorOrderName, "",
                                                            modelValidityMatches, modelsToAuthorOrderNamesMap);
                    }
                }
            }
        }
    }

    private void addWorkBasketItemToAuthorOrderEntry(PartListEntryId partListEntryId, String changeSetGUID,
                                                     String authorOrderName, String modelValidity, boolean modelValidityMatches,
                                                     Map<String, Map<String, WorkBasketAuthorOrderData>> modelsToAuthorOrderNamesMap) {
        Map<String, WorkBasketAuthorOrderData> authorOrderNamesMap = modelsToAuthorOrderNamesMap.computeIfAbsent(modelValidity,
                                                                                                                 key -> new TreeMap<>());
        WorkBasketAuthorOrderData workBasketAuthorOrderData =
                authorOrderNamesMap.computeIfAbsent(changeSetGUID,
                                                    key -> new WorkBasketAuthorOrderData(changeSetGUID,
                                                                                         authorOrderName,
                                                                                         modelValidity.isEmpty()));
        if (modelValidityMatches) {
            workBasketAuthorOrderData.setModelValidityMatches(true);
        }
        workBasketAuthorOrderData.addPLEIdWithoutModelValidityMatch(partListEntryId);
    }

    private boolean getAndCheckArrayAttribute(String attributeName, VarParam<Collection<String>> arrayValues, SerializedDBDataObject serializedDBDataObject,
                                              PartListEntryId partListEntryId, VarParam<EtkDataPartListEntry> partListEntry,
                                              String searchMask, EtkProject project) {
        if (serializedDBDataObject == null) {
            return false;
        }

        // Wert zunächst versuchen, aus SerializedDBDataObject auszulesen; mit Fallback auf den Wert aus dem geladenen
        // Stücklisteneintrag
        SerializedDBDataObjectAttribute attribute = serializedDBDataObject.getAttribute(attributeName);
        if (attribute != null) { // Array ist im SerializedDBDataObject
            SerializedEtkDataArray array = attribute.getArray();
            if ((array != null) && !array.isEmpty()) {
                arrayValues.setValue(array.getValues());
            }
        } else { // Array als Fallback aus dem Stücklisteneintrag laden
            if (partListEntry.getValue() == null) {
                partListEntry.setValue(EtkDataObjectFactory.createDataPartListEntry(project, partListEntryId));
            }
            EtkDataArray array = partListEntry.getValue().getFieldValueAsArray(attributeName);
            if ((array != null) && !array.isEmpty()) {
                arrayValues.setValue(array.getArrayAsStringList());
            }
        }

        if (StrUtils.isValid(searchMask)) {
            // Passen die Array-Werte zu der optionalen Suchmaske?
            if (arrayValues.getValue() != null) {
                Collection<String> filteredArrayValues = new ArrayList<>(arrayValues.getValue().size());
                for (String arrayValue : arrayValues.getValue()) {
                    if (StrUtils.matchesSqlLike(searchMask, arrayValue, false)) {
                        filteredArrayValues.add(arrayValue);
                    }
                }

                if (!filteredArrayValues.isEmpty()) {
                    arrayValues.setValue(filteredArrayValues);
                    return true;
                } else {
                    return false; // Suchmaske passt nicht
                }
            }
        }

        return true;
    }


    //======= Getter für Tabellen und Feldnamen ====
    // virtuelle Felder
    protected abstract String getFieldDocuRel();

    protected abstract String getFieldDocuRelReason();

    protected abstract String getFieldPartEntryId();

    public abstract String getFieldSaaCase();

    protected abstract String getFieldModelStatus();

    protected abstract String getFieldSaaBkStatus();

    protected abstract String getFieldManualStatus();

    protected abstract String getFieldAuthorOrder();

    // echte Felder
    protected String getSaaModelTable() {
        return TABLE_DA_WB_SAA_CALCULATION;
    }

    protected String getFieldSaa() {
        return FIELD_WSC_SAA;
    }

    protected abstract boolean isSaaDisplayFieldName(String fieldName);

    protected String getFieldModelReleaseFrom() {
        return FIELD_WSC_MIN_RELEASE_FROM;
    }

    protected String getFieldModelReleaseTo() {
        return FIELD_WSC_MAX_RELEASE_TO;
    }

    //====== viele Getter und Setter zur Vereinfachung =====

    protected static String getFieldSaaNutzdokRemarksAvailable() {
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

    protected String getSaaBkNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldSaa());
    }

    protected String getArrayId(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DWA_ARRAYID);
    }

    protected void setArrayId(DBDataObjectAttributes attributes, String value) {
        attributes.addField(FIELD_DWA_ARRAYID, value, DBActionOrigin.FROM_DB);
    }

    protected String getArrayFeld(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DWA_FELD);
    }

    protected String getProductNo(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(FIELD_DPM_PRODUCT_NO);
    }

    protected String getKemToDate(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldModelReleaseTo());
    }

    protected String getKemFromDate(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldModelReleaseFrom());
    }

    protected String getModelNumber(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getModelField());
    }

    protected String getPartListEntryPos(DBDataObjectAttributes attributes) {
        return attributes.getFieldValue(getFieldPartEntryId());
    }

    protected boolean isInternalTextSet(DBDataObjectAttributes attributes) {
        return attributes.getField(getVirtualFieldNameInternalTextAvailable()).getAsBoolean();
    }

    protected String getFirstInternalText(DBDataObjectAttributes attributes) {
        return attributes.getField(getVirtualFieldNameInternalText()).getAsString();
    }

    protected String getFollowUpDate(DBDataObjectAttributes attributes) {
        return attributes.getField(getVirtualFieldNameFollowUpDate()).getAsString();
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
        return FIELD_DNS_ETS_UNCONFIRMED;
    }

    protected String getEtsUnconfirmedTableName() {
        return TABLE_DA_NUTZDOK_SAA;
    }

    protected EtkEditField getEditFieldForEtsUnconfirmed() {
        return new EtkEditField(getEtsUnconfirmedTableName(), getEtsUnconfirmedFieldName(), false);
    }

    /**
     * iPartsModel über ModelContainer bestimmen und merken
     *
     * @param modelNo
     * @return
     */
    protected iPartsModel getModel(String modelNo) {
        return getModelContainer(modelNo).model;
    }

    private ModelContainer getModelContainer(String modelNo) {
        ModelContainer modelContainer = modelMap.get(modelNo);
        if (modelContainer == null) {
            iPartsModel model = iPartsModel.getInstance(project, new iPartsModelId(modelNo));
            modelContainer = new ModelContainer(model);
            modelMap.put(modelNo, modelContainer);
        }
        return modelContainer;
    }

    /**
     * Baumuster Gültigkeit auslesen
     *
     * @param modelNo
     * @return
     */
    protected boolean getModelValidFlag(String modelNo) {
        iPartsModel model = getModel(modelNo);
        if (model != null) {
            return model.isDocuRelevant();
        }
        return false;
    }

    /**
     * iPartsProduct über ProductContainer bestimmen und merken
     *
     * @param productNo
     * @return
     */
    protected iPartsProduct getProduct(String productNo) {
        ProductContainer productContainer = productMap.get(productNo);
        if (productContainer == null) {
            iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNo));
            productContainer = new ProductContainer(product);
            productMap.put(productNo, productContainer);
        }
        return productContainer.product;
    }

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

    /**
     * existiert SAA in NutzDok-SAA-Tabelle
     *
     * @param saaBkNo
     * @param loadedAttributes
     * @return
     */
    protected boolean existsSaaInNutzDok(String saaBkNo, DBDataObjectAttributes loadedAttributes) {
        Boolean result = nutzDokSaaMap.get(saaBkNo);
        if (result == null) {
            result = !loadedAttributes.getFieldValue(FIELD_DNS_SAA).isEmpty();
            nutzDokSaaMap.put(saaBkNo, result);
        }
        return result;
    }

    protected boolean isRealModelExtension(String saaBkNo, iPartsModel model, List<ArrayProductModelContainer> modelContainerList,
                                           DBDataObjectAttributes attributes, String today) {
        boolean isModelValid = getModelValidFlag(model.getModelId().getModelNumber());
        if (isModelValid) {
            String kemToDate = getKemToDate(attributes);
            String kemFromDate = getKemFromDate(attributes);
            String validFrom = model.getValidFrom();
            String validTo = model.getValidTo();
            // Überprüfung des Zeitintervalls für das Baumuster (StringBuilder und iPartsDocuRelevantTruck sind hier irrelevant,
            // es zählt nur das Ergebnis)
            if (checkTimeIntervall(new StringBuilder(), kemFromDate, kemToDate, validFrom, validTo, today, new VarParam<>())) {
                for (ArrayProductModelContainer modelContainer : modelContainerList) {
                    if (modelContainer.katalogData.hasTUModelValidityExtension(saaBkNo, model.getModelId().getModelNumber())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Überprüft, ob sich die BM-Zeitscheibe mit der Kem-Zeitscheibe überschneidet
     * Zusätzlich Einträge im Flyer in Bezug auf {@param today}
     *
     * @param str
     * @param kemFromDate
     * @param kemToDate
     * @param modelFromDate
     * @param modelToDate
     * @param today
     * @param docuRel
     * @return
     */
    protected boolean checkTimeIntervall(StringBuilder str, String kemFromDate, String kemToDate,
                                         String modelFromDate, String modelToDate,
                                         String today, VarParam<iPartsDocuRelevantTruck> docuRel) {
        boolean checkResult = true;
        if (!StrUtils.isValid(modelFromDate)) {
            // BM gilt von -Unendlich an
            if (!StrUtils.isValid(modelToDate)) {
                // BM gilt bis +Unendlich => keine Überprüfung des Kem-Intervalls nötig
            } else {
                // BM-Intervall von -Unendlich bis zu einem End-Termin
                // Überprüfe, ob Kem-Intervall eine Überschneidung hat
                if (StrUtils.isValid(kemFromDate)) {
                    if (kemFromDate.compareTo(modelToDate) > 0) {
                        // Keine Überschneidung der Intervalle
                        checkResult = false;
                        wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_MODEL_KEM_INTERVALL_NO_OVERLAPPING);
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    }
                }
                if (checkResult) {
                    // für Anzeige im Flyer
                    if (modelToDate.compareTo(today) < 0) {
                        wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_MODEL_RELEASE_DATE_EXCEEDED,
                                                  searchCallback.getVisualValueOfDbValue(getSaaModelTable(), getFieldModelReleaseTo(),
                                                                                         modelToDate));
                    }
                }
            }
        } else {
            // BM gilt ab einem StartTermin
            if (StrUtils.isValid(modelToDate)) {
                // BM gilt bis zu einem EndTermin
                // Überprüfe, ob Kem-Intervall eine Überschneidung hat
                if (StrUtils.isValid(kemFromDate)) {
                    if (kemFromDate.compareTo(modelToDate) > 0) {
                        // Keine Überschneidung der Intervalle
                        // Kem startet erst nach dem Ende des BM
                        checkResult = false;
                        wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_MODEL_KEM_INTERVALL_NO_OVERLAPPING);
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    }
                } else if (StrUtils.isValid(kemToDate)) {
                    if (kemToDate.compareTo(modelFromDate) < 0) {
                        // Keine Überschneidung der Intervalle
                        // Kem endet vor Beginn des BM
                        checkResult = false;
                        wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_MODEL_KEM_INTERVALL_NO_OVERLAPPING);
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    }
                }
                if (checkResult) {
                    // für Anzeige im Flyer
                    if (modelFromDate.compareTo(today) > 0) {
                        wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_MODEL_START_DATE_NOT_REACHED,
                                                  searchCallback.getVisualValueOfDbValue(getSaaModelTable(), getFieldModelReleaseFrom(),
                                                                                         modelFromDate));
                    }
                    if (modelToDate.compareTo(today) < 0) {
                        wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_MODEL_RELEASE_DATE_EXCEEDED,
                                                  searchCallback.getVisualValueOfDbValue(getSaaModelTable(), getFieldModelReleaseTo(),
                                                                                         modelToDate));
                    }
                }
            } else {
                // BM gilt bis +Unendlich
                // Überprüfe, ob Kem-Intervall eine Überschneidung hat
                if (StrUtils.isValid(kemToDate)) {
                    if (kemToDate.compareTo(modelFromDate) < 0) {
                        // Keine Überschneidung der Intervalle
                        // Kem endet bereits vor dem BM Starttermin
                        checkResult = false;
                        wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_MODEL_KEM_INTERVALL_NO_OVERLAPPING);
                        docuRel.setValue(iPartsDocuRelevantTruck.DOCU_RELEVANT_TRUCK_NO);
                    }
                }
                if (checkResult) {
                    // für Anzeige im Flyer
                    if (modelFromDate.compareTo(today) > 0) {
                        wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_MODEL_START_DATE_NOT_REACHED,
                                                  searchCallback.getVisualValueOfDbValue(getSaaModelTable(), getFieldModelReleaseFrom(),
                                                                                         modelFromDate));
                    }
                }
            }
        }
        // Nur Abprüfung für Flyer Text
        if (StrUtils.isValid(kemFromDate)) {
            if (kemFromDate.compareTo(today) > 0) {
                // für Flyer: Freigabedatum beginnt erst in der Zukunft
                wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_RELEASE_DATE_STARTS_LATER,
                                          searchCallback.getVisualValueOfDbValue(getSaaModelTable(), getFieldModelReleaseTo(),
                                                                                 kemFromDate));
            }
        }
        if (StrUtils.isValid(kemToDate)) {
            if (kemToDate.compareTo(today) <= 0) {
                // für Flyer: Freigabedatum bereits abgelaufen
                wbh.appendToDocuRelReason(str, WorkBasketHintMsgs.WBH_RELEASE_DATE_EXCEEDED,
                                          searchCallback.getVisualValueOfDbValue(getSaaModelTable(), getFieldModelReleaseTo(),
                                                                                 kemToDate));
            }
        }
        return checkResult;
    }

    /**
     * bildet aus der Liste der Produkt-Nummern einen komma-separierten String
     * Sind mehr als 10 Produkte vorhanden, wird der String mit '...' beendet
     *
     * @param productNoSet
     * @param excludeProductNo
     * @return
     */
    protected String buildProductList(Set<String> productNoSet, String excludeProductNo) {
        StringBuilder products = new StringBuilder();
        int count = 0;
        for (String productNo : productNoSet) {
            if (StrUtils.isValid(excludeProductNo) && productNo.equals(excludeProductNo)) {
                continue;
            }
            if (count > 0) {
                products.append(", ");
            }
            products.append(productNo);
            count++;
            if (count > iPartsMainImportHelper.MAX_ELEMS_FOR_SHOW) {
                products.append("...");
                break;
            }
        }
        return products.toString();
    }


    /**
     * Clone die übergenene {@param attributes} und setzt einige der virtuellen Felder
     *
     * @param attributes
     * @param containerList
     * @param modifyAttributes
     * @return
     */
    protected DBDataObjectAttributes buildAttrClone(DBDataObjectAttributes attributes, List<ArrayProductModelContainer> containerList, boolean modifyAttributes) {
        DBDataObjectAttributes extraAttributes = attributes.cloneMe(DBActionOrigin.FROM_DB);
        if ((containerList != null) && !containerList.isEmpty()) {
            String arrayId = buildArrayIdListFromContainerList(containerList);
            ArrayProductModelContainer container = containerList.get(0);
            extraAttributes.addField(getFieldDocuRelReason(), container.strExtraDocuRelReason.toString(), DBActionOrigin.FROM_DB);
            extraAttributes.addField(FIELD_DWA_ARRAYID, arrayId, DBActionOrigin.FROM_DB);
            extraAttributes.addField(getFieldPartEntryId(), "", DBActionOrigin.FROM_DB);
            String productNo = container.productNoFromPartList;
            extraAttributes.addField(FIELD_DPM_PRODUCT_NO, productNo, DBActionOrigin.FROM_DB);
        } else {
            if (modifyAttributes) {
                extraAttributes.addField(getFieldDocuRelReason(), "", DBActionOrigin.FROM_DB);
                extraAttributes.addField(FIELD_DWA_ARRAYID, "", DBActionOrigin.FROM_DB);
                extraAttributes.addField(getFieldPartEntryId(), "", DBActionOrigin.FROM_DB);
//            extraAttributes.addField(FIELD_DPM_PRODUCT_NO, container.productNoFromPartList, DBActionOrigin.FROM_DB);
            }
        }
        return extraAttributes;
    }


    /**
     * Sortiert die übergebene {@param containerList} nach PartListEntryId.
     * Bildet aus den zugehörigen ArrayIds eine komma-separierte Liste
     *
     * @param containerList
     * @return
     */
    private String buildArrayIdListFromContainerList(List<ArrayProductModelContainer> containerList) {
        String arrayId = "";
        if ((containerList != null) && !containerList.isEmpty()) {
            Collections.sort(containerList, new Comparator<ArrayProductModelContainer>() {
                @Override
                public int compare(ArrayProductModelContainer o1, ArrayProductModelContainer o2) {
                    PartListEntryId id1 = o1.katalogData.getPartListEntryId();
                    PartListEntryId id2 = o2.katalogData.getPartListEntryId();
                    int compareResult = id1.getKVari().compareTo(id2.getKVari());
                    if (compareResult == 0) {
                        compareResult = id1.getKLfdnr().compareTo(id2.getKLfdnr());
                    }
                    return compareResult;
                }
            });
            List<String> idListe = new DwList<>();
            for (ArrayProductModelContainer container : containerList) {
                idListe.add(container.arrayId);
            }
            arrayId = getArrayIdFromList(idListe);
        }
        return arrayId;
    }

    public String getModelValueForHint(String fieldName, DBDataObjectAttributes attributes) {
        String value;
        ModelContainer modelContainer = getModelContainer(getModelNumber(attributes));
        if (fieldName.equals(FIELD_DM_NAME)) {
            value = modelContainer.getModelName(project);
        } else if (fieldName.equals(FIELD_DM_SALES_TITLE)) {
            value = modelContainer.getModelSalesTitle(project);
        } else {
            value = modelContainer.getModelAddText(project);
        }
        return value;
    }

    public void reset(Map<AssemblyId, String> assemblyProductMap, Map<String, KatalogData> attributesKatMap,
                      iPartsWBSaaStatesManagement stateManager) {
        this.assemblyProductMap = assemblyProductMap;
        this.attributesKatMap = attributesKatMap;
        this.stateManager = stateManager;

        modelMap.clear();
        productMap.clear();
        saaBkDocuRelMap.clear();
        saDocuRelMap.clear();
        nutzDokSaaMap.clear();
        workBasketItemToAuthorOrdersMap.clear();
    }

    /**
     * Berechnung der Lieferanten Info pro Zeile
     */
    public DBDataObjectAttributesList calculateSupplier(DBDataObjectAttributes attributes, String kgFieldname,
                                                        String supplierFieldname, String supplierDefault,
                                                        WorkbasketSupplierMapping supplierMapping) {
        Set<String> kgs = calculateKGs(attributes);
        DBDataObjectAttributesList results = new DBDataObjectAttributesList();

        // Typkennzahl des ermittelten Baumusters für weitere Logik verwenden
        String modelType = new iPartsModelId(getModelNumber(attributes)).getModelTypeNumber();
        String productNo = getProductNo(attributes);

        if (iPartsPlugin.isUseProductSupplier()) {
            // Lieferantennummer ohne DA_WB_SUPPLIER_MAPPING (falls Truck)
            if (StrUtils.isValid(productNo)) {
                iPartsProduct product = iPartsProduct.getInstance(project, new iPartsProductId(productNo));
                // Lieferantennummer nur bei Truck direkt aus Produkt anzeigen
                if (!product.getDocumentationType().isPKWDocumentationType()) {
                    String supplierNo = product.getProductSupplierText(project, Language.DE.getCode());
                    if (StrUtils.isValid(supplierNo)) {
                        // damit setSupplierAttributes() richtig arbeitet
                        supplierDefault = supplierNo;
                    } else {
                        supplierNo = "";
                    }
                    results.add(supplierMapping.setSupplierAttributes(attributes, supplierNo, kgs, kgFieldname,
                                                                      supplierFieldname, supplierDefault));
                    return results;
                }
            }
        }
        // Lieferanten ermitteln
        Map<String, Set<String>> supplierKgMap = supplierMapping.buildSupplierKgMap(project, kgs, modelType, productNo, supplierDefault);
        for (Map.Entry<String, Set<String>> entry : supplierKgMap.entrySet()) {
            String supplierNo = entry.getKey();
            Set<String> kgList = entry.getValue();
            results.add(supplierMapping.setSupplierAttributes(attributes, supplierNo, kgList, kgFieldname,
                                                              supplierFieldname, supplierDefault));
        }
        return results;
    }

    /**
     * Bestimmung der KGs für diese Ausgabe-Zeile
     * kgs darf, außer im Fehlerfall, niemals leer sein!! Sonst wird die Ausgabe der Zeile unterdrückt!
     *
     * @param attributes
     * @return
     */
    private Set<String> calculateKGs(DBDataObjectAttributes attributes) {
        iPartsEDSSaaCase caseValue = iPartsEDSSaaCase.getFromDBValue(attributes.getFieldValue(getFieldSaaCase()));
        // kgs darf, außer im Fehlerfall, niemals leer sein!! Sonst wird die Ausgabe der Zeile unterdrückt!
        Set<String> kgs = new TreeSet<>();
        Boolean forOwnProduct = null;
        switch (caseValue) {
            case EDS_CASE_MODEL_VALIDITY_EXPANSION:
                // KGs über die TUs des aktuellen Produktes zur SAA ermitteln
                forOwnProduct = true;
                break;
            case EDS_CASE_SAA_VALIDITY_EXPANSION:
                // KGs über die TUs der anderen Produkte zur SAA ermitteln
                forOwnProduct = false;
                break;
            case EDS_CASE_NEW:
                // KG aus Nutzdok verwenden
                break;
            case EDS_CASE_VALIDITY_EXPANSION:
                // gleiches Verhaltien wie bei EDS_CASE_SAA_VALIDITY_EXPANSION
                // KGs über die TUs der anderen Produkte zur SAA ermitteln
                forOwnProduct = false;
                break;
            default:
                // Fehlerfall => Ausgabe wird unterdrückt
                return kgs;
        }
        if (forOwnProduct != null) {
            // KGs über die TUs der anderen Produkte zur SAA ermitteln
            // oder KGs über die TUs des aktuellen Produktes zur SAA ermitteln
            kgs.addAll(findKgsForCurrentEntry(attributes, forOwnProduct));
        }
        if (kgs.isEmpty()) {
            // KG aus Nutzdok verwenden (als Rückfallposition)
            kgs.add(attributes.getFieldValue(FIELD_DNS_GROUP));
        }
        return kgs;
    }

    private Set<String> findKgsForCurrentEntry(DBDataObjectAttributes attributes, boolean forOwnProduct) {
        String arrayIds = getArrayId(attributes);
        if (StrUtils.isValid(arrayIds)) {
            List<String> idListe = getArrayIdList(arrayIds);
            String masterProductNo = getProductNo(attributes);

            // Set mit dem zum Produkt gehörenden AssemblyIds
            Set<AssemblyId> masterAssemblyIdSet = new TreeSet<>();
            //  Map mit AssemblyId zu PartListEntry unabhängig vom Produkt
            Map<AssemblyId, KatalogData> assemblyMap = new LinkedHashMap<>();

            fillAssemblyToKatalogDataMapAndAssemblyIdSet(idListe, masterProductNo, masterAssemblyIdSet, assemblyMap);
            Set<String> kgNodes = new TreeSet<>();
            if (forOwnProduct) {
                for (AssemblyId assemblyId : masterAssemblyIdSet) {
                    String kgForAssemblyId = getKgForAssemblyId(assemblyId);
                    if (kgForAssemblyId != null) {
                        kgNodes.add(kgForAssemblyId);
                    }
                }
            } else {
                for (AssemblyId assemblyId : assemblyMap.keySet()) {
                    String kgForAssemblyId = getKgForAssemblyId(assemblyId);
                    if (StrUtils.isValid(kgForAssemblyId)) {
                        kgNodes.add(kgForAssemblyId);
                    }
                }
            }
            return kgNodes;
        }
        return new TreeSet<>();
    }

    private String getKgForAssemblyId(AssemblyId assemblyId) {
        EtkDataAssembly etkDataAssembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
        if (etkDataAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly assembly = (iPartsDataAssembly)etkDataAssembly;
            iPartsProductId productId = assembly.getProductIdFromModuleUsage();
            if (productId != null) {
                iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.
                        loadForProductAndModule(project, productId, assemblyId);
                if (!moduleEinPASList.isEmpty()) {
                    iPartsDataModuleEinPAS dataModuleEinPAS = moduleEinPASList.get(0);
                    return dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG);
                }
            }
        }
        return null;
    }

    public void setNeedsNutzDokJoin(boolean needsNutzDokJoin) {
        this.needsNutzDokJoin = needsNutzDokJoin;
    }

    public boolean getNeedsNutzDokJoin() {
        return needsNutzDokJoin;
    }

    /**
     * Container für die Beziehung zwischen ArrayId, partListEntry Product und Model-Validity
     */
    protected class ArrayProductModelContainer {

        private String arrayId;
        private String productNoFromPartList;
        private boolean productIsTested;
        private KatalogData katalogData;
        private String modelNoForTest;
        private boolean modelIsUsedInValidity;
        private boolean modelIsExplicitInValidity;
        private boolean modelValidityIsEmpty;
        private StringBuilder strExtraDocuRelReason;
        private EtkProject project;
        private boolean isSaTu;

        public ArrayProductModelContainer(String arrayId, StringBuilder strDocuRelReason, EtkProject project) {
            this.arrayId = arrayId;
            this.productNoFromPartList = null;
            this.productIsTested = false;
            this.katalogData = null;
            this.modelNoForTest = null;
            this.modelIsUsedInValidity = false;
            this.modelIsExplicitInValidity = false;
            this.modelValidityIsEmpty = false;
            this.strExtraDocuRelReason = null;
            if (strDocuRelReason != null) {
                this.strExtraDocuRelReason = new StringBuilder(strDocuRelReason);
            } else {
                this.strExtraDocuRelReason = new StringBuilder();
            }
            this.project = project;
            this.isSaTu = false;
        }

        public boolean isSaTu() {
            return isSaTu;
        }

        public boolean isProductValid(Map<String, KatalogData> attributesKatMap) {
            if (!productIsTested) {
                productIsTested = true;
                katalogData = attributesKatMap.get(arrayId);
                if (katalogData != null) {
                    productNoFromPartList = getProductNumberFromPLE(katalogData.getPartListEntryId().getOwnerAssemblyId(),
                                                                    null, project);
                    isSaTu = katalogData.getPartListEntryId().getOwnerAssemblyId().getKVari().startsWith(SA_MODULE_PREFIX);
                }
            }
            return StrUtils.isValid(productNoFromPartList);
        }

        public boolean isModelUsed(String modelNo, Map<String, KatalogData> attributesKatMap) {
            if (StrUtils.isValid(modelNo)) {
                if (isProductValid(attributesKatMap)) {
                    if (!StrUtils.isValid(modelNoForTest)) {
                        modelNoForTest = modelNo;
                        modelIsUsedInValidity = isModelUsedInValidity(katalogData, productNoFromPartList, modelNo, strExtraDocuRelReason);
                    }
                    return modelIsUsedInValidity;
                }
            }
            return false;
        }

        /**
         * Überprüfung, ob modelNo in MODEL_VALIDITY oder Product-BM-Liste enthalten ist
         *
         * @param katalogData
         * @param productNoFromPartList
         * @param modelNo
         * @param strDocuRelReason
         * @return
         */
        private boolean isModelUsedInValidity(KatalogData katalogData, String productNoFromPartList,
                                              String modelNo, StringBuilder strDocuRelReason) {
            boolean isModelUsed = false;
            EtkDataArray modelArray = katalogData.getModelValidityArray(project);
            if ((modelArray != null) && !modelArray.isEmpty()) {
                modelValidityIsEmpty = false;
                Set<String> modelList = new HashSet<>(modelArray.getArrayAsStringList());

                // besitzt explizite BM-Gültigkeiten
                if (modelList.contains(modelNo)) {
                    if (strDocuRelReason != null) {
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MODEL_EXISTS_IN,
                                                  modelNo, modelValidityText.getText(project.getViewerLanguage()));
                    }
                    modelIsExplicitInValidity = true;
                    isModelUsed = true;
                } else {
                    wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MODEL_EXISTS_NOT_IN,
                                              modelNo, modelValidityText.getText(project.getViewerLanguage()));
                }
            } else {
                modelValidityIsEmpty = true;
                if (StrUtils.isValid((productNoFromPartList))) {
                    Set<String> modelList = getProduct(productNoFromPartList).getModelNumbers(project);
                    if (modelList.contains(modelNo)) {
                        // gilt für alle BM des Produktes
                        if (strDocuRelReason != null) {
                            wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MODEL_IMPLICIT_IN,
                                                      modelNo, modelValidityText.getText(project.getViewerLanguage()));
                        }
                        isModelUsed = true;
                    }
                } else { // wird eigentlich nicht durchlaufen
                    // gilt für alle BM des Produktes
                    if (strDocuRelReason != null) {
                        wbh.appendToDocuRelReason(strDocuRelReason, WorkBasketHintMsgs.WBH_MODEL_IMPLICIT_IN,
                                                  modelNo, modelValidityText.getText(project.getViewerLanguage()));
                    }
                    isModelUsed = true;
                }
            }
            return isModelUsed;
        }
    }


    /**
     * Datenklasse für einen Autoren-Auftrag zu einem Arbeitsvorrats-Eintrag
     */
    protected static class WorkBasketAuthorOrderData implements Comparable<WorkBasketAuthorOrderData> {

        private String changeSetId;
        private String authorOrderName;
        private boolean withoutModelValidity; // Daten ohne BM-Gültigkeit?
        private boolean modelValidityMatches; // Passt für mindestens einen Stücklisteneintrag neben der SAA-Gültigkeit auch die BM-Gültigkeit?

        // Alle zur SAA passenden Stücklisteneinträge des Autoren-Auftrags (unabhängig von der BM-Gültigkeit, damit die
        // BM-Gültigkeitserweiterung funktioniert)
        private List<PartListEntryId> pleIdsWithoutModelValidityMatch = new DwList<>();

        public WorkBasketAuthorOrderData(String changeSetId, String authorOrderName, boolean withoutModelValidity) {
            this.changeSetId = changeSetId;
            this.authorOrderName = authorOrderName;
            this.withoutModelValidity = withoutModelValidity;
        }

        public String getChangeSetId() {
            return changeSetId;
        }

        public String getAuthorOrderName() {
            return authorOrderName;
        }

        public boolean isModelValidityMatches() {
            return modelValidityMatches;
        }

        public void setModelValidityMatches(boolean modelValidityMatches) {
            this.modelValidityMatches = modelValidityMatches;
        }

        public void addPLEIdWithoutModelValidityMatch(PartListEntryId pleIdWithoutModelValidityMatch) {
            pleIdsWithoutModelValidityMatch.add(pleIdWithoutModelValidityMatch);
        }

        public List<PartListEntryId> getPLEIdsWithoutModelValidityMatch() {
            return pleIdsWithoutModelValidityMatch;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass())) {
                return false;
            }
            WorkBasketAuthorOrderData that = (WorkBasketAuthorOrderData)o;
            return Objects.equals(changeSetId, that.changeSetId) && Objects.equals(authorOrderName, that.authorOrderName)
                   && Objects.equals(withoutModelValidity, that.withoutModelValidity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(changeSetId, authorOrderName, withoutModelValidity);
        }

        @Override
        public int compareTo(WorkBasketAuthorOrderData o) {
            int result = changeSetId.compareTo(o.changeSetId);
            if (result != 0) {
                return result;
            } else {
                return String.valueOf(withoutModelValidity).compareTo(String.valueOf(o.withoutModelValidity));
            }
        }
    }


    /**
     * Container-Klasse für iPartsProduct. die DBDataObjectAttributes werden gebraucht,
     * falls in den DisplayFields Spalten aus der DA_PRODUCT angesprochen werden und
     * neue Trefferzeilen gebildet werden
     */
    private static class ProductContainer {

        iPartsProduct product;
        DBDataObjectAttributes productAttributes;

        public ProductContainer(iPartsProduct product) {
            this.product = product;
            this.productAttributes = null;
        }
    }


    /**
     * Container-Klasse für iPartsModel. Die DBDataObjectAttributes werden gebraucht,
     * falls in den DisplayFields Spalten aus der DA_MODEL angesprochen werden und
     * neue Trefferzeilen gebildet werden
     */
    private static class ModelContainer {

        iPartsModel model;
        DBDataObjectAttributes modelAttributes;
        String modelName;
        String modelSalesTitle;
        String modelAddText;

        public ModelContainer(iPartsModel model) {
            this.model = model;
            this.modelAttributes = null;
        }

        public String getModelName(EtkProject project) {
            if (modelName == null) {
                modelName = model.getModelName(project).getTextByNearestLanguage(project.getDBLanguage(), project.getDataBaseFallbackLanguages());
            }
            return modelName;
        }

        public String getModelSalesTitle(EtkProject project) {
            if (modelSalesTitle == null) {
                modelSalesTitle = model.getModelSalesTitle(project).getTextByNearestLanguage(project.getDBLanguage(), project.getDataBaseFallbackLanguages());
            }
            return modelSalesTitle;
        }

        public String getModelAddText(EtkProject project) {
            if (modelAddText == null) {
                modelAddText = model.getModelAddText(project).getTextByNearestLanguage(project.getDBLanguage(), project.getDataBaseFallbackLanguages());
            }
            return modelAddText;
        }
    }


    /**
     * Datenklasse für die relevanten Daten aus der Tabelle KATALOG.
     */
    public static class KatalogData {

        private PartListEntryId partListEntryId;
        private String modelValidityArrayId;
        private EtkDataArray modelValidityArray;
        private Map<String, Map<String, Boolean>> modelValidityExtensionMap; // SAA/BK auf Baumuster auf Gültigkeitserweiterung

        public KatalogData(PartListEntryId partListEntryId, String modelValidityArrayId) {
            this.partListEntryId = partListEntryId;
            this.modelValidityArrayId = modelValidityArrayId;
            this.modelValidityExtensionMap = new HashMap<>();
        }

        public PartListEntryId getPartListEntryId() {
            return partListEntryId;
        }

        public EtkDataArray getModelValidityArray(EtkProject project) {
            if (modelValidityArrayId.isEmpty()) {
                return null;
            }

            if (modelValidityArray == null) {
                modelValidityArray = project.getEtkDbs().getArrayById(TABLE_KATALOG, FIELD_K_MODEL_VALIDITY, modelValidityArrayId);
            }

            return modelValidityArray;
        }

        public boolean hasTUModelValidityExtension(String saaBkNo, String modelNo) {
            Map<String, Boolean> modelValidityExtensionMapForSaaBk = modelValidityExtensionMap.get(saaBkNo);
            if (modelValidityExtensionMapForSaaBk != null) {
                Boolean hasModelValidityExtension = modelValidityExtensionMapForSaaBk.get(modelNo);
                if (hasModelValidityExtension != null) {
                    return hasModelValidityExtension;
                }
            }
            return false;
        }

        public void setTUModelValidityExtension(String saaBkNo, String modelNo, boolean tuHasModelValidityExtension) {
            modelValidityExtensionMap.computeIfAbsent(saaBkNo, saaBk -> new HashMap<>()).put(modelNo, tuHasModelValidityExtension);
        }
    }
}
