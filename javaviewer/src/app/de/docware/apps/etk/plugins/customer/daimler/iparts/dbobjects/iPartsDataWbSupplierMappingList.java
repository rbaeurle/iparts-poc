/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataWbSupplierMapping}.
 */
public class iPartsDataWbSupplierMappingList extends EtkDataObjectList<iPartsDataWbSupplierMapping> implements iPartsConst {

    public iPartsDataWbSupplierMappingList() {
        setSearchWithoutActiveChangeSets(true);
    }


    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataWbSupplierMapping}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataWbSupplierMappingList loadCompleteMapping(EtkProject project) {
        iPartsDataWbSupplierMappingList list = new iPartsDataWbSupplierMappingList();
        list.loadCompleteWbSupplierMappingFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataWbSupplierMapping}s.
     *
     * @param project
     * @param origin
     */
    private void loadCompleteWbSupplierMappingFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_WB_SUPPLIER_MAPPING, null, null, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataWbSupplierMapping getNewDataObject(EtkProject project) {
        return new iPartsDataWbSupplierMapping(project, null);
    }

}
