package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

public class iPartsPPUAId extends IdWithType {

    public static String TYPE = "DA_iPartsPPUA";

    // DA_PPUA_PARTNO, DA_PPUA_REGION, A_PPUA_SERIES, DA_PPUA_ENTITY, DA_PPUA_TYPE, DA_PPUA_YEAR
    protected enum INDEX {
        PARTNO, REGION, SERIES, ENTITY, HITTYPE, YEAR
    }

    /**
     * Der normale Konstruktor
     */
    public iPartsPPUAId(String partNo, String region, String series, String entity, String hitType, String year) {
        super(TYPE, new String[]{ partNo, region, series, entity, hitType, year });
    }

    public iPartsPPUAId(iPartsPPUAId basicId, String year) {
        this(basicId.getPartNo(), basicId.getRegion(), basicId.getSeries(), basicId.getEntity(), basicId.getHitType(), year);
    }

    /**
     * Für Stücklisteneintrag basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsPPUAId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsPPUAId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    public static iPartsPPUAId getFromDBString(String dbValue) {
        IdWithType id = IdWithType.fromDBString(TYPE, dbValue);
        if (id != null) {
            return new iPartsPPUAId(id.toStringArrayWithoutType());
        }
        return null;
    }

    @Override
    public boolean isValidId() {
        if (idType.isEmpty() || (id.length == 0)) {
            return false;
        }
        return StrUtils.isValid(getPartNo(), getRegion(), getEntity(), getSeries(), getHitType());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPPUAId() {
        this("", "", "", "", "", "");
    }

    public String getPartNo() {
        return id[INDEX.PARTNO.ordinal()];
    }

    public String getRegion() {
        return id[INDEX.REGION.ordinal()];
    }

    public String getEntity() {
        return id[INDEX.ENTITY.ordinal()];
    }

    public String getSeries() {
        return id[INDEX.SERIES.ordinal()];
    }

    public String getHitType() {
        return id[INDEX.HITTYPE.ordinal()];
    }

    public String getYear() {
        return id[INDEX.YEAR.ordinal()];
    }

}
