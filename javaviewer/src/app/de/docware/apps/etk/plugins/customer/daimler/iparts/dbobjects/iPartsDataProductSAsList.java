/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.sql.TableAndFieldName;

/**
 * Liste von {@link iPartsDataProductSAs}.
 */
public class iPartsDataProductSAsList extends EtkDataObjectList<iPartsDataProductSAs> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProductSAs} für die übergebene {@link iPartsProductId} (liefert
     * also eine Liste aller SAs für das Produkt inkl. dazugehöriger KG-Verortung zurück).
     *
     * @param project
     * @param productId
     * @return
     */
    public static iPartsDataProductSAsList loadDataForProduct(EtkProject project, iPartsProductId productId) {
        iPartsDataProductSAsList list = new iPartsDataProductSAsList();
        list.loadDataForProductFromDB(project, productId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProductSAs} für die übergebene {@link iPartsSAId} (liefert
     * also eine Liste aller Produkte, in denen die SA vorhanden ist, inkl. dazugehöriger KG-Verortung zurück).
     *
     * @param project
     * @param saId
     * @return
     */
    public static iPartsDataProductSAsList loadDataForSA(EtkProject project, iPartsSAId saId) {
        iPartsDataProductSAsList list = new iPartsDataProductSAsList();
        list.loadDataForSAFromDB(project, saId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataProductSAs} zur übergebenen {@link iPartsDataAssembly}.
     *
     * @param project
     * @param ownerAssembly
     * @return
     */
    public static iPartsDataProductSAsList loadProductSasForSaAssembly(EtkProject project, iPartsDataAssembly ownerAssembly) {
        iPartsDataProductSAsList list = new iPartsDataProductSAsList();
        list.loadDataWithAssemblyJoin(project, ownerAssembly, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadDataWithAssemblyJoin(EtkProject project, iPartsDataAssembly ownerAssembly, DBActionOrigin origin) {
        // Nur laden, wenn es sich wirklich um eine SA Stückliste handelt
        if (!ownerAssembly.isSAAssembly()) {
            return;
        }
        clear(origin);
        String[] whereFields = new String[]{ TableAndFieldName.make(TABLE_DA_SA_MODULES, FIELD_DSM_MODULE_NO) };
        String[] whereValues = new String[]{ ownerAssembly.getAsId().getKVari() };
        searchSortAndFillWithJoin(project, null, null,
                                  new String[]{ FIELD_DPS_SA_NO },
                                  TABLE_DA_SA_MODULES,
                                  new String[]{ FIELD_DSM_SA_NO },
                                  false, false,
                                  whereFields, whereValues,
                                  false, null, false);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataProductSAs} für die übergebene {@link iPartsProductId} (liefert also eine
     * Liste aller SAs für das Produkt inkl. dazugehöriger KG-Verortung zurück).
     *
     * @param project
     * @param productId
     * @param loadType
     * @param origin
     */
    private void loadDataForProductFromDB(EtkProject project, iPartsProductId productId, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DPS_PRODUCT_NO };
        String[] whereValues = new String[]{ productId.getProductNumber() };
        searchAndFill(project, TABLE_DA_PRODUCT_SAS, whereFields, whereValues, loadType, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataProductSAs} für die übergebene {@link iPartsSAId} (liefert also eine Liste
     * aller Produkte, in denen die SA vorhanden ist, inkl. dazugehöriger KG-Verortung zurück).
     *
     * @param project
     * @param saId
     * @param loadType
     * @param origin
     */
    private void loadDataForSAFromDB(EtkProject project, iPartsSAId saId, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DPS_SA_NO };
        String[] whereValues = new String[]{ saId.getSaNumber() };
        searchAndFill(project, TABLE_DA_PRODUCT_SAS, whereFields, whereValues, loadType, origin);
    }

    @Override
    protected iPartsDataProductSAs getNewDataObject(EtkProject project) {
        return new iPartsDataProductSAs(project, null);
    }
}
