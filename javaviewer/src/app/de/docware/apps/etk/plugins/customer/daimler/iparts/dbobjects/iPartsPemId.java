package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;

/**
 * ID für die Tabelle DA_PEM_MASTERDATA
 */
public class iPartsPemId extends IdWithType {

    public static final String TYPE = "DA_iPartsPemId";
    public static String DESCRIPTION = "!!PEM";

    private enum INDEX {PEM, FACTORY}

    /**
     * Der normale Konstruktor
     *
     * @param pem
     * @param factoryNo
     */
    public iPartsPemId(String pem, String factoryNo) {
        super(TYPE, new String[]{ pem, factoryNo });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPemId() {
        this("", "");
    }

    /**
     * Für PEM basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsPemId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsPemId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    public String getPEM() {
        return id[iPartsPemId.INDEX.PEM.ordinal()];
    }

    public String getFactoryNo() {
        return id[iPartsPemId.INDEX.FACTORY.ordinal()];
    }
}
