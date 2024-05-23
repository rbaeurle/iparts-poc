/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert ein Primus-Wahlweise-Teil aus der Tabelle {@link iPartsConst#TABLE_DA_PRIMUS_WW_PART} im iParts Plug-in.
 */
public class iPartsPrimusWWPartId extends IdWithType {

    public static final String TYPE = "DA_iPartsPrimusWWPartId";

    protected enum INDEX {PART_NO, WW_ID, WW_PART_NO}

    public iPartsPrimusWWPartId(String partNo, String wwID, String wwPartNo) {
        super(TYPE, new String[]{ partNo, wwID, wwPartNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPrimusWWPartId() {
        this("", "", "");
    }

    public String getPartNo() {
        return id[INDEX.PART_NO.ordinal()];
    }

    public String getWWID() {
        return id[INDEX.WW_ID.ordinal()];
    }

    public String getWWPartNo() {
        return id[INDEX.WW_PART_NO.ordinal()];
    }
}
