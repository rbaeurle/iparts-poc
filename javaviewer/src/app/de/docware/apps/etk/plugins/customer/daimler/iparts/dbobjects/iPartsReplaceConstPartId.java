package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Tabelle DA_REPLACE_CONST_PART
 */
public class iPartsReplaceConstPartId extends IdWithType {

    public static final String TYPE = "DA_iPartsReplaceConstPartId";
    public static final String DESCRIPTION = "!!Ersetzung (Konstruktion TS7)";

    private enum INDEX {PARTNO, SDATA}

    /**
     * Der normale Konstruktor
     *
     * @param partNo
     * @param sDatA
     */
    public iPartsReplaceConstPartId(String partNo, String sDatA) {
        super(TYPE, new String[]{ partNo, sDatA });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsReplaceConstPartId() {
        this("", "");
    }


    public String getPartNo() {
        return id[iPartsReplaceConstPartId.INDEX.PARTNO.ordinal()];
    }

    public String getsDatA() {
        return id[iPartsReplaceConstPartId.INDEX.SDATA.ordinal()];
    }
}
