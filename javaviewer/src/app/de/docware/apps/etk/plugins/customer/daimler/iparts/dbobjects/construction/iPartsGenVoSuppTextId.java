package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Generischer Verbauort-ErgänzungstextID (Tabelle DA_GENVO_SUPP_TEXT) im iParts Plug-in.
 */
public class iPartsGenVoSuppTextId extends IdWithType {

    public static String TYPE = "DA_iPartsGenVoSuppText";

    protected enum INDEX {
        GENVO_NO
    }

    public static iPartsGenVoSuppTextId getFromDBString(String dbValue) {
        IdWithType id = IdWithType.fromDBString(TYPE, dbValue);
        if (id != null) {
            return new iPartsGenVoSuppTextId(id.toStringArrayWithoutType());
        }
        return null;
    }


    /**
     * Der normale Konstruktor
     */
    public iPartsGenVoSuppTextId(String genVoId) {
        super(TYPE, new String[]{ genVoId });
    }

    /**
     * Für iPartsGenVoSuppTextId basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsGenVoSuppTextId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsGenVoSuppTextId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsGenVoSuppTextId() {
        this("");
    }

    public String getGenVoNo() {
        return id[INDEX.GENVO_NO.ordinal()];
    }

}
