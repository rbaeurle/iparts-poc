/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSAModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Product-SA-ID (Tabelle DA_PRODUCT_SAS) im iParts Plug-in.
 */
public class iPartsProductSAsId extends IdWithType {

    public static String TYPE = "DA_iPartsProductSAsId";
    public static String DESCRIPTION = "Produkt-SA Zuordnung";

    protected enum INDEX {PRODUCT_NUMBER, SA_NUMBER, KG}

    /**
     * Der normale Konstruktor
     *
     * @param productNumber
     * @param saNumber
     * @param kg
     */
    public iPartsProductSAsId(String productNumber, String saNumber, String kg) {
        super(TYPE, new String[]{ productNumber, saNumber, kg });
    }

    /**
     * Convenience Konstruktur
     *
     * @param productId
     * @param saModulesId
     * @param kg
     */
    public iPartsProductSAsId(iPartsProductId productId, iPartsSAModulesId saModulesId, String kg) {
        this(productId.getProductNumber(), saModulesId.getSaNumber(), kg);
    }

    /**
     * Für Produkt basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsProductSAsId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != iPartsProductSAsId.INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsProductSAsId doesn't have length "
                                               + iPartsProductSAsId.INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsProductSAsId() {
        this("", "", "");
    }

    public String getProductNumber() {
        return id[INDEX.PRODUCT_NUMBER.ordinal()];
    }

    public String getSaNumber() {
        return id[INDEX.SA_NUMBER.ordinal()];
    }

    public String getKG() {
        return id[INDEX.KG.ordinal()];
    }
}
