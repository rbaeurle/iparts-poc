/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminOrgCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderSupplierId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWbSupplierMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWbSupplierMappingList;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Cache für die Datenbanktabelle DA_WB_SUPPLIER_MAPPING
 * Wird während dem Export der offenen Arbeitsvorräte verwendet um die exportierten Daten um die Liefenranten bzw.
 * deren Organisations-Benennung zu ergänzen.
 */
public class WorkbasketSupplierMapping {

    private EtkProject project;
    private HashMap<String, List<iPartsDataWbSupplierMapping>> mappingForModelType;
    private Map<String, iPartsWorkOrderSupplierId> supplierMap;
    // zum Debuggen
    private Set<String> searchedModelTypes;

    public WorkbasketSupplierMapping(EtkProject project) {
        this.project = project;
        load(project);
    }

    public void load(EtkProject project) {
        mappingForModelType = new HashMap<>();
        iPartsDataWbSupplierMappingList completeMapping = iPartsDataWbSupplierMappingList.loadCompleteMapping(project);
        for (iPartsDataWbSupplierMapping mappingData : completeMapping) {
            String modelType = mappingData.getAsId().getModelType();
            mappingForModelType.computeIfAbsent(modelType, s -> new DwList<>());
            mappingForModelType.get(modelType).add(mappingData);
        }
        // DAIMLER-10937: der Organisationsname wird aus der Benutzerverwaltung bestimmt
        supplierMap = null;
        searchedModelTypes = new HashSet<>();
    }

    public List<iPartsDataWbSupplierMapping> getMappingForModelType(EtkProject project, String modelType) {
        if (modelType == null) {
            return new DwList<>();
        }
        if (mappingForModelType == null) {
            if (project != null) {
                load(project);
            } else {
                return new DwList<>();
            }
        }
        searchedModelTypes.add(modelType);
        return mappingForModelType.get(modelType);
    }

    public String getSupplierName(String supplierNo, String supplierDefault) {
        if (StrUtils.isValid(supplierNo)) {
            if (StrUtils.isValid(supplierDefault) && supplierDefault.equals(supplierNo)) {
                return supplierNo;
            }
            iPartsUserAdminOrgCache cache = iPartsUserAdminOrgCache.getCacheByBSTSupplierId(supplierNo);
            if (cache != null) {
                String cachedName = cache.getOrgName(project.getDBLanguage());
                if (StrUtils.isValid(cachedName)) {
                    return cachedName;
                }
            }

            if (supplierMap != null) {
                iPartsWorkOrderSupplierId supplierId = supplierMap.get(supplierNo);
                if (supplierId != null) {
                    return supplierId.getSupplierName();
                }
            }
        }
        return supplierNo;
    }

    public DBDataObjectAttributes setSupplierAttributes(DBDataObjectAttributes attributes, String supplierNo, Set<String> kgList,
                                                        String kgFieldname, String supplierFieldname, String supplierDefault) {
        DBDataObjectAttributes clonedAttributes = attributes.cloneMe(DBActionOrigin.FROM_DB);
        clonedAttributes.addField(kgFieldname, StrUtils.stringListToString(kgList, ", "), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        // Lieferantenname bestimmen
        supplierNo = getSupplierName(supplierNo, supplierDefault);
        clonedAttributes.addField(supplierFieldname, supplierNo, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        return clonedAttributes;
    }


    public Map<String, Set<String>> buildSupplierKgMap(EtkProject project, Set<String> kgs, Set<String> modelTypeSet,
                                                       String productNo, String supplierDefault) {
        Map<String, Set<String>> supplierKgMap = new HashMap<>();
        if ((modelTypeSet == null) || modelTypeSet.isEmpty()) {
            buildSupplierKgMap(project, supplierKgMap,
                               kgs, "", productNo, supplierDefault);
        } else {
            for (String modelType : modelTypeSet) {
                buildSupplierKgMap(project, supplierKgMap,
                                   kgs, modelType, productNo, supplierDefault);
            }
        }
        return supplierKgMap;
    }

    public Map<String, Set<String>> buildSupplierKgMap(EtkProject project, Set<String> kgs, String modelType,
                                                       String productNo, String supplierDefault) {
        Map<String, Set<String>> supplierKgMap = new HashMap<>();
        buildSupplierKgMap(project, supplierKgMap,
                           kgs, modelType, productNo, supplierDefault);
        return supplierKgMap;
    }

    private void buildSupplierKgMap(EtkProject project, Map<String, Set<String>> supplierKgMap,
                                    Set<String> kgs, String modelType, String productNo, String supplierDefault) {
        List<String> currentKGs = new DwList<>(kgs);
        List<String> usedKgs = new DwList<>();
        if (!modelType.isEmpty()) {
            List<iPartsDataWbSupplierMapping> mappingForModelType = getMappingForModelType(project, modelType);
            if (mappingForModelType != null) {
                Map<String, Set<String>> supplierWithProductMatch = new HashMap<>();
                Map<String, Set<String>> supplierWithoutProductMatch = new HashMap<>();
                // Schritt 1 und 2 gemeinsam bestimmen. In beiden Fällen muss die KG ab <= KG-AV <= KG bis sein
                for (iPartsDataWbSupplierMapping mappingData : mappingForModelType) {
                    String mappingKgFrom = mappingData.getFieldValue(iPartsConst.FIELD_DWSM_KG_FROM);
                    String mappingKgTo = mappingData.getFieldValue(iPartsConst.FIELD_DWSM_KG_TO);
                    String mappingProductNo = mappingData.getFieldValue(iPartsConst.FIELD_DWSM_PRODUCT_NO);

                    for (String kg : currentKGs) {
                        if (checkKgCondition(mappingKgFrom, mappingKgTo, kg)) {
                            String supplierNo = mappingData.getFieldValue(iPartsConst.FIELD_DWSM_SUPPLIER_NO);
                            if (mappingProductNo.isEmpty()) {
                                addToMap(supplierWithoutProductMatch, supplierNo, kg);
                            }
                            if (!productNo.isEmpty() && mappingProductNo.equals(productNo)) {
                                addToMap(supplierWithProductMatch, supplierNo, kg);
                            }
                        }
                    }
                }
                // Zuerst die KGs mit Produktgleichheit übernehmen
                for (Map.Entry<String, Set<String>> entry : supplierWithProductMatch.entrySet()) {
                    String currentSupplierNo = entry.getKey();
                    Set<String> currentKgSet = entry.getValue();
                    usedKgs.addAll(currentKgSet);
                    addToMap(supplierKgMap, currentSupplierNo, currentKgSet);
                }
                // Jetzt die restlichen mit leerem Produkt hinzufügen
                for (Map.Entry<String, Set<String>> entry : supplierWithoutProductMatch.entrySet()) {
                    String currentSupplierNo = entry.getKey();
                    Set<String> currentKgSet = entry.getValue();
                    for (String currentKg : currentKgSet) {
                        if (!usedKgs.contains(currentKg)) {
                            addToMap(supplierKgMap, currentSupplierNo, currentKg);
                            usedKgs.add(currentKg);
                        }
                    }
                }
            }
        }
        // bereits zugewiesen KGs entfernen
        currentKGs.removeIf(kg -> {
            return usedKgs.contains(kg);
        });
        usedKgs.clear();
        if (!currentKGs.isEmpty()) {
            // Schritt 3: mit leerer Typkennzahl prüfen
            List<iPartsDataWbSupplierMapping> mappingForEmptyModelType = getMappingForModelType(project, "");
            if (mappingForEmptyModelType != null) {
                for (iPartsDataWbSupplierMapping mappingData : mappingForEmptyModelType) {
                    String mappingKgFrom = mappingData.getFieldValue(iPartsConst.FIELD_DWSM_KG_FROM);
                    String mappingKgTo = mappingData.getFieldValue(iPartsConst.FIELD_DWSM_KG_TO);
                    for (String kg : currentKGs) {
                        if (checkKgCondition(mappingKgFrom, mappingKgTo, kg)) {
                            String supplierNo = mappingData.getFieldValue(iPartsConst.FIELD_DWSM_SUPPLIER_NO);
                            addToMap(supplierKgMap, supplierNo, kg);
                            usedKgs.add(kg);
                        }
                    }
                }
                // alle in Schritt 3 gefundenen KGs entfernen
                currentKGs.removeIf(kg -> {
                    return usedKgs.contains(kg);
                });
            }
        }
        if (!currentKGs.isEmpty()) {
            // für diese KGs gibt es keine Lieferantenzuordnung
            Set<String> kgList = new TreeSet<>(currentKGs);
            addToMap(supplierKgMap, supplierDefault, kgList);
        }
    }

    private void addToMap(Map<String, Set<String>> mapping, String key, String value) {
        Set<String> kgList = mapping.get(key);
        if (kgList == null) {
            kgList = new TreeSet<>();
            mapping.put(key, kgList);
        }
        kgList.add(value);
    }

    private void addToMap(Map<String, Set<String>> mapping, String key, Set<String> valueSet) {
        Set<String> kgList = mapping.get(key);
        if (kgList == null) {
            kgList = new TreeSet<>();
            mapping.put(key, kgList);
        }
        kgList.addAll(valueSet);
    }

    private boolean checkKgCondition(String kgFrom, String kgTo, String kg) {
        String mappingKgFrom = StrUtils.leftFill(kgFrom, 2, '0');
        String mappingKgTo = StrUtils.leftFill(kgTo, 2, '0');
        String currentKg = StrUtils.leftFill(kg, 2, '0');
        return (mappingKgFrom.compareTo(currentKg) <= 0) && (currentKg.compareTo(mappingKgTo) <= 0);
    }


}
