/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataOPSDesc}.
 * Deprecated, weil die OPS Struktur nun in zwei verschiedenen Tabellen gehalten wird (DA_OPS_GROUP und DA_OPS_SCOPE)
 */
@Deprecated
public class iPartsDataOPSDescList extends EtkDataObjectList<iPartsDataOPSDesc> implements iPartsConst {

    /**
     * lädt alle {@link iPartsDataOPSDesc}s, die zu einer Gruppe gehören
     * sortiert nach Gruppe und Umfang
     *
     * @param project
     * @param opsGroup
     * @return
     */
    public static iPartsDataOPSDescList loadAllGroupValues(EtkProject project, String opsGroup) {
        iPartsDataOPSDescList list = new iPartsDataOPSDescList();
        list.loadAllGroupValuesFromDB(project, opsGroup, DBActionOrigin.FROM_DB);
        return list;
    }


    /**
     * lädt alle {@link iPartsDataOPSDesc}s, die zu einer Gruppe gehören
     * sortiert nach Gruppe und Umfang
     *
     * @param project
     * @param opsGroup
     * @param origin
     */
    private void loadAllGroupValuesFromDB(EtkProject project, String opsGroup, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_OPS_GROUP };
        String[] whereValues = new String[]{ opsGroup };
        String[] sortFields = new String[]{ FIELD_OPS_GROUP, FIELD_OPS_SCOPE };
        searchSortAndFill(project, TABLE_DA_OPSDESC, whereFields, whereValues, sortFields, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataOPSDesc getNewDataObject(EtkProject project) {
        return new iPartsDataOPSDesc(project, null);
    }
}
