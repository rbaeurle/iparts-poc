/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsSpecType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Liste von {@link iPartsDataModule}.
 */
public class iPartsDataModuleList extends EtkDataObjectList<iPartsDataModule> implements iPartsConst {

    /**
     * Liefert die Baumusternummern für alle Produkte, in denen ein TU vom Typ {@link iPartsSpecType} vorkommt
     *
     * @param project
     * @param specType
     * @return
     */
    public static Set<String> getModelsForSpecType(EtkProject project, iPartsSpecType specType) {
        iPartsDataModuleList list = new iPartsDataModuleList();
        list.setSearchWithoutActiveChangeSets(true);
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_MODULE, FIELD_DM_SPEC) };
        String[] whereValues = new String[]{ specType.getDbValue() };
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_PRODUCT_MODULES, FIELD_DPM_PRODUCT_NO, false, false));

        Set<String> modelSet = new HashSet<>();
        Set<String> productSet = new HashSet<>();
        JoinData joinData = new EtkDataObjectList.JoinData(TABLE_DA_PRODUCT_MODULES,
                                                           new String[]{ FIELD_DM_MODULE_NO },
                                                           new String[]{ FIELD_DPM_MODULE_NO },
                                                           false, false);
        FoundAttributesCallback foundAttributesCallback = new FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String productNo = attributes.getFieldValue(FIELD_DPM_PRODUCT_NO);
                if (StrUtils.isValid(productNo)) {
                    if (productSet.add(productNo)) {
                        modelSet.addAll(iPartsProduct.getInstance(project, new iPartsProductId(productNo)).getModelNumbers(project));
                    }
                }
                return false;
            }
        };
        String[] sortFields = null;
        list.searchSortAndFillWithJoin(project, null, selectFields, whereTableAndFields, whereValues,
                                       false, sortFields, false,
                                       foundAttributesCallback, joinData);
        return modelSet;
    }

    /**
     * Erzeugt und lädt die komplette DA_MODULE (nur IDs).
     *
     * @param project
     * @return
     */
    public static iPartsDataModuleList loadAllData(EtkProject project) {
        iPartsDataModuleList list = new iPartsDataModuleList();
        list.loadAllDataFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModule} (nur IDs) für die übergebene Modulnummer inkl. möglicher Wildcards.
     *
     * @param project
     * @param moduleNumber
     * @return
     */
    public static iPartsDataModuleList loadDataForModuleNumber(EtkProject project, String moduleNumber) {
        iPartsDataModuleList list = new iPartsDataModuleList();
        list.loadDataForModuleNumberFromDB(project, moduleNumber, LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt die komplette DA_MODULE (nur IDs).
     *
     * @param project
     * @param origin
     */
    private void loadAllDataFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_MODULE, null, null, LoadType.ONLY_IDS, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModule} (nur IDs) für die übergebene Modulnummer inkl. möglicher Wildcards.
     *
     * @param project
     * @param moduleNumber
     * @param loadType
     * @param origin
     */
    private void loadDataForModuleNumberFromDB(EtkProject project, String moduleNumber, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DM_MODULE_NO };
        String[] whereValues = new String[]{ moduleNumber };
        searchWithWildCardsSortAndFill(project, whereFields, whereValues, null, loadType, origin);
    }

    @Override
    protected iPartsDataModule getNewDataObject(EtkProject project) {
        return new iPartsDataModule(project, null);
    }

    /**
     * Alle Module zum Produkt bestimmen für Fahrzeugprodukt + alle Aggregate-Produkte zum Fahrzeug
     *
     * @param project
     * @param selectedFields
     * @param productNo
     * @param addOrderBy
     * @param foundAttributesCallback
     */
    public static iPartsDataModuleList searchAllModulesForAllProducts(EtkProject project, EtkDisplayFields selectedFields, String productNo, boolean addOrderBy,
                                                                      FoundAttributesCallback foundAttributesCallback) {
        // Alle Produkte bestimmen: Fahrzeugprodukt mit dem der Dialog geöffnet wurde + alle Aggregate-Produkte zum Fahrzeug
        Set<String> products = new LinkedHashSet<>();
        products.add(productNo);
        products.addAll(iPartsProduct.getInstance(project, new iPartsProductId(productNo)).getAggregates(project).stream().map(product -> product.getAsId().getProductNumber()).collect(Collectors.toSet()));
        // Pro Produkt wird eine DB Abfrage gemacht. Es werden alle Module zum Produkt bestimmt und pro Modul wird
        // geprüft, ob das Modul gültig ist.
        iPartsDataModuleList dataModuleList = new iPartsDataModuleList();
        for (String productNumber : products) {
            dataModuleList.searchAllModulesForProduct(project, selectedFields, productNumber,
                                                      addOrderBy, foundAttributesCallback);
        }
        return dataModuleList;
    }

    /**
     * Alle Module zu einem Produkt suchen
     *
     * @param project
     * @param selectedFields
     * @param productNo
     * @param addOrderBy
     * @param foundAttributesCallback
     */
    public void searchAllModulesForProduct(EtkProject project, EtkDisplayFields selectedFields, String productNo, boolean addOrderBy,
                                           FoundAttributesCallback foundAttributesCallback) {
        FoundAttributesCallback foundAttributesCallbackWithProductModels = new FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String moduleNo = attributes.getFieldValue(FIELD_DM_MODULE_NO);
                iPartsProductModulesId partsProductModulesId = new iPartsProductModulesId(productNo, moduleNo);
                iPartsDataProductModules dataProductModules = new iPartsDataProductModules(project, partsProductModulesId);
                if (!dataProductModules.existsInDB()) { // Produkt passt nicht
                    return false;
                }

                if (foundAttributesCallback != null) {
                    attributes.addField(FIELD_DPM_PRODUCT_NO, productNo, DBActionOrigin.FROM_DB);
                    return foundAttributesCallback.foundAttributes(attributes);
                }

                return true;
            }
        };

        // Um Pseudo-Transaktionen zu vermeiden nur einen einfachen Join verwenden und dafür im foundAttributesCallbackWithProductModels
        // die exakte Korrektheit bzgl. DA_PRODUCT_MODELS prüfen
        searchSortAndFillWithJoin(project, null, selectedFields,
                                  new String[]{ TableAndFieldName.make(TABLE_MAT, FIELD_M_BESTNR),
                                                TableAndFieldName.make(TABLE_MAT, FIELD_M_ASSEMBLY) },
                                  new String[]{ productNo + EditModuleHelper.IPARTS_MODULE_NAME_DELIMITER + "*",
                                                SQLStringConvert.booleanToPPString(true) },
                                  false, new String[]{ FIELD_DM_MODULE_NO }, addOrderBy,
                                  new boolean[]{ true, false }, false, true, false, foundAttributesCallbackWithProductModels, true,
                                  new EtkDataObjectList.JoinData(TABLE_MAT,
                                                                 new String[]{ FIELD_DM_MODULE_NO },
                                                                 new String[]{ FIELD_M_MATNR },
                                                                 false, false));
    }
}
