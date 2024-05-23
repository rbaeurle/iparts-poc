/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert ein Fahrzeugdatenkartencode aus der Tabelle TABLE_DA_VEHICLE_DATACARD_CODES im iParts Plug-in.
 */
public class iPartsVehicleDatacardCodeId extends IdWithType {

    public static String TYPE = "DA_iPartsVehicleDatacardCode";

    protected enum INDEX {VEHICLE_CODE}

    /**
     * Der normale Konstruktor
     *
     * @param vehicleCode
     */
    public iPartsVehicleDatacardCodeId(String vehicleCode) {
        super(TYPE, new String[]{ vehicleCode });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsVehicleDatacardCodeId() {
        this("");
    }

    public String getVehicleCode() {
        return id[INDEX.VEHICLE_CODE.ordinal()];
    }
}
