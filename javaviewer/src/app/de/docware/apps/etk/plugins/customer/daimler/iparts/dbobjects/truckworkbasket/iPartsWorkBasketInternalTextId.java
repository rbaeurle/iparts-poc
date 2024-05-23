/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket;

import de.docware.util.misc.id.IdWithType;

/**
 * Dummy-Id für die Internen Texte in allen WorkBaskets (Arbeitsvorräten)
 */
public class iPartsWorkBasketInternalTextId extends IdWithType {

    public static final String TYPE = "iPartsWorkBasketInternalTextId";

    protected enum INDEX {WBTYPE, SAA_KB_KEM_NO}

    /**
     * Der normale Konstruktor
     *
     * @param wbType
     * @param saaBkKemNo
     */
    public iPartsWorkBasketInternalTextId(iPartsWorkBasketTypes wbType, String saaBkKemNo) {
        super(TYPE, new String[]{ wbType.name(), saaBkKemNo });
    }

    public iPartsWorkBasketInternalTextId(String wbType, String saaBkKemNo) {
        super(TYPE, new String[]{ wbType, saaBkKemNo });
    }

    public iPartsWorkBasketInternalTextId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for WorkBasketInternalTextId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsWorkBasketInternalTextId() {
        this("", "");
    }

    public String getWbTypeValue() {
        return id[INDEX.WBTYPE.ordinal()];
    }

    public iPartsWorkBasketTypes getWbType() {
        return iPartsWorkBasketTypes.getFromDbValue(getWbTypeValue());
    }

    public String getSaaBkKemValue() {
        return id[INDEX.SAA_KB_KEM_NO.ordinal()];
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
