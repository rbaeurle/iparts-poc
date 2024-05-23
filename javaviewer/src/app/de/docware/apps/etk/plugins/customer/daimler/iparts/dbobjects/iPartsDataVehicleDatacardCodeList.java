/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste von {@link iPartsDataVehicleDatacardCode}.
 */
public class iPartsDataVehicleDatacardCodeList extends EtkDataObjectList<iPartsDataVehicleDatacardCode> implements iPartsConst {


    public iPartsDataVehicleDatacardCodeList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Erzeugt und lädt eine Liste aller {@link iPartsDataVehicleDatacardCode}s.
     *
     * @param project
     * @return
     */
    public static iPartsDataVehicleDatacardCodeList loadAllVehicleCodes(EtkProject project) {
        iPartsDataVehicleDatacardCodeList list = new iPartsDataVehicleDatacardCodeList();
        list.loadAllVehicleCodesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllVehicleCodesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_VEHICLE_DATACARD_CODES, null, null, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataVehicleDatacardCode getNewDataObject(EtkProject project) {
        return new iPartsDataVehicleDatacardCode(project, null);
    }
}
