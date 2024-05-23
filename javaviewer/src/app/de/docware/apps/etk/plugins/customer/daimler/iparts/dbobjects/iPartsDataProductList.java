/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsStructureId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.SortType;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * DataObjectList für {@link iPartsDataProduct}
 */
public class iPartsDataProductList extends EtkDataObjectList<iPartsDataProduct> implements iPartsConst {

    public iPartsDataProductList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProduct}s (optional eingeschränkt auf den Produktnamen (ohne Hinzufügen
     * von Wildcards), die Produktgruppe und den Aggregatetyp).
     *
     * @param project
     * @param productName         Wird ignoriert falls {@code null} oder leer.
     * @param productNameLanguage Muss gesetzt sein, wenn <i>productName</i> gesetzt ist.
     * @param productGroup        Wird ignoriert falls {@code null} oder leer.
     * @param aggregateType       Wird ignoriert falls {@code null} oder leer.
     * @return
     */
    public static iPartsDataProductList loadDataProductList(EtkProject project, String productName, String productNameLanguage,
                                                            String productGroup, String aggregateType) {
        iPartsDataProductList list = new iPartsDataProductList();
        list.loadDataProductsFromDB(project, productName, productNameLanguage, productGroup, aggregateType, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProduct}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataProductList loadDataProductList(EtkProject project) {
        iPartsDataProductList list = new iPartsDataProductList();
        list.loadDataProductsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataProductList loadAllEPCProductList(EtkProject project) {
        iPartsDataProductList list = new iPartsDataProductList();
        list.loadAllEPCProductsFromDBWithAdditionalConditions(project, null, null, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataProductList loadAllEPCProductListWithEmptyAggTypes(EtkProject project) {
        iPartsDataProductList list = new iPartsDataProductList();
        list.loadAllEPCProductsFromDBWithAdditionalConditions(project, new String[]{ FIELD_DP_AGGREGATE_TYPE }, new String[]{ "" }, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataProduct} aus der DB (optional eingeschränkt auf den Produktnamen (ohne Hinzufügen von Wildcards),
     * die Produktgruppe und den Aggregatetyp).
     *
     * @param project
     * @param productName         Wird ignoriert falls {@code null} oder leer.
     * @param productNameLanguage Muss gesetzt sein, wenn <i>productName</i> gesetzt ist.
     * @param productGroup        Wird ignoriert falls {@code null} oder leer.
     * @param aggregateType       Wird ignoriert falls {@code null} oder leer.
     * @param origin
     */
    private void loadDataProductsFromDB(EtkProject project, String productName, String productNameLanguage, String productGroup,
                                        String aggregateType, DBActionOrigin origin) {
        clear(origin);

        boolean filterProductName = false;
        ExtendedDataTypeLoadType mllType = ExtendedDataTypeLoadType.MARK;
        if (StrUtils.isValid(productName)) {
            filterProductName = true;
            mllType = ExtendedDataTypeLoadType.LOAD;
        }

        List<String> whereFieldsList = null;
        List<String> whereValuesList = null;
        if (StrUtils.isValid(productGroup)) {
            whereFieldsList = new ArrayList<String>(2);
            whereValuesList = new ArrayList<String>(2);
            whereFieldsList.add(FIELD_DP_PRODUCT_GRP);
            whereValuesList.add(productGroup);
        }
        if (StrUtils.isValid(aggregateType)) {
            if (whereFieldsList == null) {
                whereFieldsList = new ArrayList<String>(2);
                whereValuesList = new ArrayList<String>(2);
            }
            whereFieldsList.add(FIELD_DP_AGGREGATE_TYPE);
            whereValuesList.add(aggregateType);
        }

        String[] whereFields;
        String[] whereValues;
        if (whereFieldsList != null) {
            whereFields = whereFieldsList.toArray(new String[whereFieldsList.size()]);
            whereValues = whereValuesList.toArray(new String[whereValuesList.size()]);
        } else {
            whereFields = null;
            whereValues = null;
        }

        DBDataObjectAttributesList dataProductPropertiesList = project.getDbLayer().getAttributesList(TABLE_DA_PRODUCT, null,
                                                                                                      whereFields, whereValues,
                                                                                                      mllType);

        dataProductPropertiesList.sort(new String[]{ FIELD_DP_PRODUCT_NO, FIELD_DP_STRUCTURING_TYPE }, SortType.AUTOMATIC);

        for (DBDataObjectAttributes attributes : dataProductPropertiesList) {
            boolean addProduct = true;
            if (filterProductName) {
                String productTitleFromDB = attributes.getField(FIELD_DP_TITLE).getMultiLanguageText(productNameLanguage,
                                                                                                     EtkDataObject.getTempExtendedDataTypeProvider(project, TABLE_DA_PRODUCT));
                if (!StrUtils.matchesSqlLike(productName, productTitleFromDB, false)) {
                    addProduct = false;
                }
            }

            if (addProduct) {
                iPartsDataProduct dataProduct = new iPartsDataProduct(project, null);
                dataProduct.assignAttributes(project, attributes, true, DBActionOrigin.FROM_DB);
                add(dataProduct, origin);
            }
        }
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataProduct}s (optional eingeschränkt für die übergebene {@link iPartsStructureId}).
     *
     * @param project
     * @param origin
     */
    public void loadAllEPCProductsFromDBWithAdditionalConditions(EtkProject project, String[] additionalWhereFields, String[] additionalWhereValues, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DP_STRUCTURING_TYPE, FIELD_DP_SOURCE };
        String[] whereValues = new String[]{ iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU.name(), iPartsImportDataOrigin.EPC.getOrigin() };
        if ((additionalWhereFields != null) && (additionalWhereValues != null) && (additionalWhereFields.length == additionalWhereValues.length)) {
            whereFields = StrUtils.mergeArrays(whereFields, additionalWhereFields);
            whereValues = StrUtils.mergeArrays(whereValues, additionalWhereValues);
        }
        searchSortAndFill(project, TABLE_DA_PRODUCT, whereFields, whereValues,
                          new String[]{ FIELD_DP_PRODUCT_NO, FIELD_DP_STRUCTURING_TYPE },
                          LoadType.ONLY_IDS, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataProduct}s (optional eingeschränkt für die übergebene {@link iPartsStructureId}).
     *
     * @param project
     * @param origin
     */
    public void loadDataProductsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = null;
        String[] whereValues = null;
        searchSortAndFill(project, TABLE_DA_PRODUCT, whereFields, whereValues,
                          new String[]{ FIELD_DP_PRODUCT_NO, FIELD_DP_STRUCTURING_TYPE },
                          LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataProduct getNewDataObject(EtkProject project) {
        return new iPartsDataProduct(project, null);
    }
}
