/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * DataObjectList für {@link iPartsDataProductModules}
 */
public class iPartsDataProductModulesList extends EtkDataObjectList<iPartsDataProductModules> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProductModules}
     *
     * @param project
     * @return
     */
    public static iPartsDataProductModulesList loadAllDataProductModulesList(EtkProject project) {
        iPartsDataProductModulesList list = new iPartsDataProductModulesList();
        list.loadAllDataProductModulesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProductModules} für das angegebene Produkt
     *
     * @param project
     * @param productId
     * @return
     */
    public static iPartsDataProductModulesList loadDataProductModulesList(EtkProject project, iPartsProductId productId) {
        iPartsDataProductModulesList list = new iPartsDataProductModulesList();
        list.loadDataProductModulesFromDB(project, productId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataProductModules} für die angegebene Modul
     *
     * @param project
     * @param moduleId
     * @return
     */
    public static iPartsDataProductModulesList loadDataProductModulesList(EtkProject project, AssemblyId moduleId) {
        iPartsDataProductModulesList list = new iPartsDataProductModulesList();
        list.loadDataProductModulesFromDB(project, moduleId, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle {@link iPartsDataProductModules} aus der DB
     *
     * @param project
     * @param origin
     */
    private void loadAllDataProductModulesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);

        searchAndFill(project, TABLE_DA_PRODUCT_MODULES, null, null, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataProductModules} aus der DB für das angegebene Produkt
     *
     * @param project
     * @param productId
     * @param origin
     */
    private void loadDataProductModulesFromDB(EtkProject project, iPartsProductId productId, DBActionOrigin origin) {
        clear(origin);

        String productNumber = productId.getProductNumber();
        searchAndFill(project, TABLE_DA_PRODUCT_MODULES, new String[]{ FIELD_DPM_PRODUCT_NO }, new String[]{ productNumber },
                      LoadType.COMPLETE, origin);
    }

    /**
     * Lädt alle {@link iPartsDataProductModules} aus der DB für die angegebene Modul
     *
     * @param project
     * @param moduleId
     * @param origin
     */
    private void loadDataProductModulesFromDB(EtkProject project, AssemblyId moduleId, DBActionOrigin origin) {
        clear(origin);

        String moduleNumber = moduleId.getKVari();
        searchAndFill(project, TABLE_DA_PRODUCT_MODULES, new String[]{ FIELD_DPM_MODULE_NO }, new String[]{ moduleNumber },
                      LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataProductModules getNewDataObject(EtkProject project) {
        return new iPartsDataProductModules(project, null);
    }
}
