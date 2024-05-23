/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für TABLE_DA_VEHICLE_DATACARD_CODES
 */
public class iPartsDataVehicleDatacardCode extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DVDC_CODE };

    public iPartsDataVehicleDatacardCode(EtkProject project, iPartsVehicleDatacardCodeId id) {
        super(KEYS);
        tableName = TABLE_DA_VEHICLE_DATACARD_CODES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsVehicleDatacardCodeId createId(String... idValues) {
        return new iPartsVehicleDatacardCodeId(idValues[0]);
    }

    @Override
    public iPartsVehicleDatacardCodeId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsVehicleDatacardCodeId)id;
    }

}
