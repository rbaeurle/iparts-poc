/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.util.misc.id.IdWithType;

public class iPartsSpkMappingId extends IdWithType {

    public static String TYPE = "DA_iPartsSpkMapping";

    protected enum INDEX {SERIES_NO, HM, M, KURZ_E, KURZ_AS, STEERING}

    /**
     * Der normale Konstruktor
     */
    public iPartsSpkMappingId(String seriesNo, String hm, String m, String spkKurzE, String spkKurzAS, String steering) {
        super(TYPE, new String[]{ seriesNo, hm, m, spkKurzE, spkKurzAS, steering });
    }

    public iPartsSpkMappingId(HmMSmId hmMSmId, String spkKurzE, String spkKurzAS, String steering) {
        this(hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(), spkKurzE, spkKurzAS, steering);
    }

    public iPartsSpkMappingId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsSpkMappingId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsSpkMappingId() {
        this("", "", "", "", "", "");
    }

    public String getSeries() {
        return id[INDEX.SERIES_NO.ordinal()];
    }

    public String getHm() {
        return id[INDEX.HM.ordinal()];
    }

    public String getM() {
        return id[INDEX.M.ordinal()];
    }

    public HmMSmId getHmMSmId() {
        return new HmMSmId(getSeries(), getHm(), getM(), "");
    }

    public String getKurzE() {
        return id[INDEX.KURZ_E.ordinal()];
    }

    public String getKurzAS() {
        return id[INDEX.KURZ_AS.ordinal()];
    }

    public String getSteering() {
        return id[INDEX.STEERING.ordinal()];
    }

    public static iPartsSpkMappingId getFromDBString(String dbValue) {
        IdWithType id = IdWithType.fromDBString(TYPE, dbValue);
        if (id != null) {
            return new iPartsSpkMappingId(id.toStringArrayWithoutType());
        }
        return null;
    }
}
