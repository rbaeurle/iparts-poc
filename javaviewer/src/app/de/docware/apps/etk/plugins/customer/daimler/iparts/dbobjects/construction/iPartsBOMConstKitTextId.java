package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert die PK-Values aus der Tabelle TABLE_DA_EDS_CONST_PROPS im iParts Plug-in.
 */
public class iPartsBOMConstKitTextId extends IdWithType {

    public static String TYPE = "DA_iPartsBOMConstKitTextId";

    protected enum INDEX {CONST_KIT_NO, PARTPOS, TEXT_TYPE, REV_FROM}

    public iPartsBOMConstKitTextId(String constKitNumber, String partPos, String textType, String revFrom) {
        super(TYPE, new String[]{ constKitNumber, partPos, textType, revFrom });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsBOMConstKitTextId() {
        this("", "", "", "");
    }

    /**
     * Liegt eine gültige ID vor
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getConstKitNo().isEmpty() && !getPartPos().isEmpty() && !getRevFrom().isEmpty();
    }

    public String getConstKitNo() {
        return id[INDEX.CONST_KIT_NO.ordinal()];
    }

    public String getPartPos() {
        return id[INDEX.PARTPOS.ordinal()];
    }

    public String getTextType() {
        return id[INDEX.TEXT_TYPE.ordinal()];
    }

    public String getRevFrom() {
        return id[INDEX.REV_FROM.ordinal()];
    }

}
