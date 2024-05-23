/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataAssortmentPermissionMapping}.
 */
public class iPartsDataAssortmentPermissionsMappingList extends EtkDataObjectList<iPartsDataAssortmentPermissionMapping> implements iPartsConst {

    public void iPartsDataAssortmentPermissionMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataAssortmentClassesMapping}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataAssortmentPermissionsMappingList loadAllAcPcPermissionMappings(EtkProject project) {
        iPartsDataAssortmentPermissionsMappingList list = new iPartsDataAssortmentPermissionsMappingList();
        list.loadAllAcPcPermissionMappingsFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataAssortmentClassesMapping}s.
     *
     * @param project
     * @param origin
     */
    private void loadAllAcPcPermissionMappingsFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_AC_PC_PERMISSION_MAPPING, null, null, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataAssortmentPermissionMapping getNewDataObject(EtkProject project) {
        return new iPartsDataAssortmentPermissionMapping(project, null);
    }
}
