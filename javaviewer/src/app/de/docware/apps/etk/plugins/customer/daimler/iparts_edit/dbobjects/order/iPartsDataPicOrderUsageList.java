/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataPicOrderUsage}.
 */
public class iPartsDataPicOrderUsageList extends EtkDataObjectList<iPartsDataPicOrderUsage> implements iPartsConst {

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataPicOrderUsage}s für die übergebene Auftrags-GUID.
     *
     * @param project
     * @param orderGuid
     * @return
     */
    public static iPartsDataPicOrderUsageList loadPicOrderUsageList(EtkProject project, String orderGuid) {
        iPartsDataPicOrderUsageList list = new iPartsDataPicOrderUsageList();
        list.loadPicOrderUsageFromDB(project, orderGuid, DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataPicOrderUsage}s für die übergebene Auftrags-GUID.
     *
     * @param project
     * @param orderGuid
     * @param origin
     */
    public void loadPicOrderUsageFromDB(EtkProject project, String orderGuid, DBActionOrigin origin) {
        clear(origin);

        String[] whereFields = new String[]{ FIELD_DA_POU_ORDER_GUID };
        String[] whereValues = new String[]{ orderGuid };
        searchAndFill(project, TABLE_DA_PICORDER_USAGE, whereFields, whereValues, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataPicOrderUsage getNewDataObject(EtkProject project) {
        return new iPartsDataPicOrderUsage(project, null);
    }
}
