package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Primus Ersetzung aus der Tabelle {@link iPartsConst#TABLE_DA_PRIMUS_REPLACE_PART} im iParts Plug-in.
 */
public class iPartsPrimusReplacePartId extends IdWithType {

    public static final String TYPE = "DA_iPartsPrimusReplacePartId";

    protected enum INDEX {PART_NO}

    public iPartsPrimusReplacePartId(String partNo) {
        super(TYPE, new String[]{ partNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPrimusReplacePartId() {
        this("");
    }

    public String getPartNo() {
        return id[INDEX.PART_NO.ordinal()];
    }

}
