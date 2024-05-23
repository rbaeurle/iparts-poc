/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste von {@link iPartsDataModel}.
 */
public class iPartsDataKgTuAfterSalesList extends EtkDataObjectList<iPartsDataKgTuAfterSales> implements iPartsConst {

    /**
     * Erzeugt und läd eine Liste aller {@link iPartsDataKgTuAfterSales} für das übergebene Baumuster
     *
     * @param project
     * @param productId
     * @return
     */
    public static iPartsDataKgTuAfterSalesList loadKgTuForProductList(EtkProject project, iPartsProductId productId) {
        iPartsDataKgTuAfterSalesList list = new iPartsDataKgTuAfterSalesList();
        list.loadKgTuForProductFromDB(project, productId, DBActionOrigin.FROM_DB);
        return list;
    }

    public static iPartsDataKgTuAfterSalesList loadKgTuForProductListWithTexts(EtkProject project, iPartsProductId productId) {
        iPartsDataKgTuAfterSalesList list = new iPartsDataKgTuAfterSalesList();
        list.loadKgTuForProductFromDBWithTexts(project, productId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataKgTuAfterSales} für das übergebene Product
     *
     * @param project
     * @param productId
     * @param origin
     */
    private void loadKgTuForProductFromDB(EtkProject project, iPartsProductId productId, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DA_DKM_PRODUCT };
        String[] whereValues = new String[]{ productId.getProductNumber() };
        searchAndFill(project, TABLE_DA_KGTU_AS, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    private void loadKgTuForProductFromDBWithTexts(EtkProject project, iPartsProductId productId, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_KGTU_AS, FIELD_DA_DKM_PRODUCT) };
        String[] whereValues = new String[]{ productId.getProductNumber() };
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_KGTU_AS, FIELD_DA_DKM_DESC, true, false);
        displayFields.addFeld(displayField);

        searchSortAndFillWithMultiLangValueForAllLanguages(project, displayFields,
                                                           TableAndFieldName.make(TABLE_DA_KGTU_AS, FIELD_DA_DKM_DESC),
                                                           whereFields, whereValues,
                                                           false, null, false);
    }

    @Override
    protected iPartsDataKgTuAfterSales getNewDataObject(EtkProject project) {
        return new iPartsDataKgTuAfterSales(project, null);
    }
}
