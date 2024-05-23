/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataPicOrderModules}.
 */
public class iPartsDataPicOrderModulesList extends EtkDataObjectList<iPartsDataPicOrderModules> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrderModules} für die übergebene Auftrags-GUID.
     *
     * @param project
     * @param orderGuid
     * @return
     */
    public static iPartsDataPicOrderModulesList loadPicOrderModulesListForOrder(EtkProject project, String orderGuid) {
        iPartsDataPicOrderModulesList list = new iPartsDataPicOrderModulesList();
        list.loadPicOrderModulesFromDBForOrder(project, orderGuid, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrderModules} für die übergebene Modulnummer.
     *
     * @param project
     * @param moduleNumber
     * @return
     */
    public static iPartsDataPicOrderModulesList loadPicOrderModulesListForModule(EtkProject project, String moduleNumber) {
        iPartsDataPicOrderModulesList list = new iPartsDataPicOrderModulesList();
        list.loadPicOrderModulesFromDBForModule(project, moduleNumber, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataPicOrderModules} für die übergebene Auftrags-GUID.
     *
     * @param project
     * @param orderGuid
     * @param origin
     */
    public void loadPicOrderModulesFromDBForOrder(EtkProject project, String orderGuid, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_POM_ORDER_GUID };
        String[] whereValues = new String[]{ orderGuid };
        searchAndFill(project, TABLE_DA_PICORDER_MODULES, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataPicOrderModules} für die übergebene Modulnummer.
     *
     * @param project
     * @param moduleNumber
     * @param origin
     */
    public void loadPicOrderModulesFromDBForModule(EtkProject project, String moduleNumber, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_POM_MODULE_NO };
        String[] whereValues = new String[]{ moduleNumber };
        searchAndFill(project, TABLE_DA_PICORDER_MODULES, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataPicOrderModules getNewDataObject(EtkProject project) {
        return new iPartsDataPicOrderModules(project, null);
    }
}
