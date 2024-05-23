/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataModuleEinPAS}.
 */
public class iPartsDataModuleEinPASList extends EtkDataObjectList<iPartsDataModuleEinPAS> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModuleEinPAS}s, die zum übergebenen Produkt gehören.
     *
     * @param project
     * @param productId
     * @return
     */
    public static iPartsDataModuleEinPASList loadForProduct(EtkProject project, iPartsProductId productId) {
        iPartsDataModuleEinPASList list = new iPartsDataModuleEinPASList();
        list.loadForProductFromDB(project, productId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModuleEinPAS}s, die zum übergebenen Modul gehören.
     *
     * @param project
     * @param moduleId
     * @return
     */
    public static iPartsDataModuleEinPASList loadForModule(EtkProject project, AssemblyId moduleId) {
        iPartsDataModuleEinPASList list = new iPartsDataModuleEinPASList();
        list.loadForModuleFromDB(project, moduleId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModuleEinPAS}s, die zum übergebenen Produkt und Modul gehören.
     *
     * @param project
     * @param productId
     * @param moduleId
     * @return
     */
    public static iPartsDataModuleEinPASList loadForProductAndModule(EtkProject project, iPartsProductId productId, AssemblyId moduleId) {
        iPartsDataModuleEinPASList list = new iPartsDataModuleEinPASList();
        list.loadForProductAndModuleFromDB(project, productId, moduleId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModuleEinPAS}s für ein bestimmtes Produkt und eine bestimmte EinPAS-Ebene
     *
     * @param project
     * @param productId
     * @param einPasId
     * @return
     */
    public static iPartsDataModuleEinPASList loadForEinPas(EtkProject project, iPartsProductId productId, EinPasId einPasId) {
        iPartsDataModuleEinPASList list = new iPartsDataModuleEinPASList();
        list.loadForEinPasFromDB(project, productId, einPasId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataModuleEinPAS}s für ein bestimmtes Produkt und eine bestimmte KG/TU-Ebene
     *
     * @param project
     * @param productId
     * @param kgTuId
     * @return
     */
    public static iPartsDataModuleEinPASList loadForKgTu(EtkProject project, iPartsProductId productId, KgTuId kgTuId) {
        iPartsDataModuleEinPASList list = new iPartsDataModuleEinPASList();
        list.loadForKgTuFromDB(project, productId, kgTuId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModuleEinPAS}s, die zum übergebenen Produkt gehören.
     *
     * @param project
     * @param productId
     * @param origin
     * @return
     */
    public void loadForProductFromDB(EtkProject project, iPartsProductId productId, DBActionOrigin origin) {
        clear(origin);

        String productNumber = productId.getProductNumber();
        String[] whereFields = new String[]{ FIELD_DME_PRODUCT_NO };
        String[] whereValues = new String[]{ productNumber };
        searchAndFillComplete(project, whereFields, whereValues, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModuleEinPAS}s, die zum übergebenen Modul gehören.
     *
     * @param project
     * @param moduleId
     * @param origin
     * @return
     */
    public void loadForModuleFromDB(EtkProject project, AssemblyId moduleId, DBActionOrigin origin) {
        clear(origin);

        String moduleNumber = moduleId.getKVari();
        String[] whereFields = new String[]{ FIELD_DME_MODULE_NO };
        String[] whereValues = new String[]{ moduleNumber };
        searchAndFillComplete(project, whereFields, whereValues, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModuleEinPAS}s, die zum übergebenen Produkt und Modul gehören.
     *
     * @param project
     * @param productId
     * @param moduleId
     * @param origin
     * @return
     */
    public void loadForProductAndModuleFromDB(EtkProject project, iPartsProductId productId, AssemblyId moduleId, DBActionOrigin origin) {
        clear(origin);

        String productNumber = productId.getProductNumber();
        String moduleNumber = moduleId.getKVari();
        String[] whereFields = new String[]{ FIELD_DME_PRODUCT_NO, FIELD_DME_MODULE_NO };
        String[] whereValues = new String[]{ productNumber, moduleNumber };
        searchAndFillComplete(project, whereFields, whereValues, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModuleEinPAS}s für ein bestimmtes Produkt und eine bestimmte EinPAS-Ebene.
     *
     * @param project
     * @param productId
     * @param einPasId
     * @param origin
     * @return
     */
    public void loadForEinPasFromDB(EtkProject project, iPartsProductId productId, EinPasId einPasId, DBActionOrigin origin) {
        clear(origin);

        String productNumber = productId.getProductNumber();
        String[] whereFields = new String[]{ FIELD_DME_PRODUCT_NO, FIELD_DME_EINPAS_HG, FIELD_DME_EINPAS_G, FIELD_DME_EINPAS_TU };
        String[] whereValues = new String[]{ productNumber, einPasId.getHg(), einPasId.getG(), einPasId.getTu() };
        searchAndFillComplete(project, whereFields, whereValues, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModuleEinPAS}s für ein bestimmtes Produkt und eine bestimmte KG/TU-Ebene.
     *
     * @param project
     * @param productId
     * @param kgTuId
     * @param origin
     * @return
     */
    public void loadForKgTuFromDB(EtkProject project, iPartsProductId productId, KgTuId kgTuId, DBActionOrigin origin) {
        clear(origin);

        String productNumber = productId.getProductNumber();
        String[] whereFields;
        String[] whereValues;
        if (kgTuId.isKgNode()) {
            whereFields = new String[]{ FIELD_DME_PRODUCT_NO, FIELD_DME_SOURCE_KG };
            whereValues = new String[]{ productNumber, kgTuId.getKg() };
        } else {
            whereFields = new String[]{ FIELD_DME_PRODUCT_NO, FIELD_DME_SOURCE_KG, FIELD_DME_SOURCE_TU };
            whereValues = new String[]{ productNumber, kgTuId.getKg(), kgTuId.getTu() };
        }
        searchAndFillComplete(project, whereFields, whereValues, origin);
    }

    private void searchAndFillComplete(EtkProject project, String[] whereFields, String[] whereValues, DBActionOrigin origin) {
        searchAndFill(project, TABLE_DA_MODULES_EINPAS, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataModuleEinPAS getNewDataObject(EtkProject project) {
        return new iPartsDataModuleEinPAS(project, null);
    }
}
