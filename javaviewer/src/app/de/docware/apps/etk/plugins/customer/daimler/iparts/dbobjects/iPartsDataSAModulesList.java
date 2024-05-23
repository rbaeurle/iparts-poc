/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataSAModules}.
 */
public class iPartsDataSAModulesList extends EtkDataObjectList<iPartsDataSAModules> implements iPartsConst {

    /**
     * Erzeugt und lädt die komplette DA_SA_MODULES (nur IDs).
     *
     * @param project
     * @return
     */
    public static iPartsDataSAModulesList loadAllData(EtkProject project) {
        iPartsDataSAModulesList list = new iPartsDataSAModulesList();
        list.loadAllDataFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataSAModules} für die übergebene SA.
     *
     * @param project
     * @param saId
     * @return
     */
    public static iPartsDataSAModulesList loadDataForSA(EtkProject project, iPartsSAId saId) {
        iPartsDataSAModulesList list = new iPartsDataSAModulesList();
        list.loadDataForSAFromDB(project, saId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataSAModules} für das übergebene Modul.
     *
     * @param project
     * @param moduleId
     * @return
     */
    public static iPartsDataSAModulesList loadDataForModule(EtkProject project, iPartsModuleId moduleId) {
        iPartsDataSAModulesList list = new iPartsDataSAModulesList();
        list.loadDataForModuleFromDB(project, moduleId, LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt die komplette DA_SA_MODULES (nur IDs).
     *
     * @param project
     * @param origin
     */
    private void loadAllDataFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_SA_MODULES, null, null, LoadType.ONLY_IDS, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataSAModules} für die übergebene SA.
     *
     * @param project
     * @param saId
     * @param loadType
     * @param origin
     */
    private void loadDataForSAFromDB(EtkProject project, iPartsSAId saId, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ FIELD_DSM_SA_NO };
        String[] whereValues = new String[]{ saId.getSaNumber() };
        searchAndFill(project, TABLE_DA_SA_MODULES, whereFields, whereValues, loadType, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataSAModules} für das übergebene Modul.
     *
     * @param project
     * @param moduleId
     * @param loadType
     * @param origin
     */
    private void loadDataForModuleFromDB(EtkProject project, iPartsModuleId moduleId, LoadType loadType, DBActionOrigin origin) {
        clear(origin);
        String[] whereFields = new String[]{ iPartsConst.FIELD_DSM_MODULE_NO };
        String[] whereValues = new String[]{ moduleId.getModuleNumber() };
        searchAndFill(project, TABLE_DA_SA_MODULES, whereFields, whereValues, loadType, origin);
    }

    @Override
    protected iPartsDataSAModules getNewDataObject(EtkProject project) {
        return new iPartsDataSAModules(project, null);
    }
}
