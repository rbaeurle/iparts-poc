/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketTypes;
import de.docware.util.misc.id.IdWithType;

/**
 * Dummy-Id für die Wiedervorlagetermine in den WorkBaskets "fehlende Konstruktionselemente für SAA/KEM"
 */
public class iPartsWorkBasketFollowUpDateId extends IdWithType {

    public static final String TYPE = "iPartsWorkBasketFollowUpDateId";

    protected enum INDEX {WBTYPE, SAA_BK_KEM_NO}

    /**
     * Der normale Konstruktor
     *
     * @param wbType
     * @param saaBkKemNo
     */
    public iPartsWorkBasketFollowUpDateId(iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        this(wbType.name(), saaBkKemNo);
    }

    public iPartsWorkBasketFollowUpDateId(String wbType, String saaBkKemNo) {
        super(TYPE, new String[]{ wbType, saaBkKemNo });
    }

    public iPartsWorkBasketFollowUpDateId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != iPartsWorkBasketFollowUpDateId.INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for WorkBasketInternalTextId doesn't have length "
                                               + iPartsWorkBasketFollowUpDateId.INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsWorkBasketFollowUpDateId() {
        this("", "");
    }

    public iPartsWorkBasketFollowUpDateId(iPartsWorkBasketInternalTextId wbIntTextId) {
        this(wbIntTextId.getWbType(), wbIntTextId.getSaaBkKemValue());
    }

    public String getWbTypeValue() {
        return id[iPartsWorkBasketFollowUpDateId.INDEX.WBTYPE.ordinal()];
    }

    public iPartsWorkBasketTypes getWbType() {
        return iPartsWorkBasketTypes.getFromDbValue(getWbTypeValue());
    }

    public String getSaaBkKemValue() {
        return id[iPartsWorkBasketFollowUpDateId.INDEX.SAA_BK_KEM_NO.ordinal()];
    }

    public String getKEM() {
        if (isKEM()) {
            return getSaaBkKemValue();
        }
        return null;
    }

    public String getSaaBk() {
        if (isSaaOrBk()) {
            return getSaaBkKemValue();
        }
        return null;
    }

    public boolean isSaaOrBk() {
        return iPartsWorkBasketTypes.isSaaWorkBasket(getWbType());
    }

    public boolean isKEM() {
        return iPartsWorkBasketTypes.isKemWorkBasket(getWbType());
    }
}
