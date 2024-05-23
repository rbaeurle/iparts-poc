/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Bildauftrags-Verwendungs-ID im iParts Plug-in.
 */
public class iPartsPicOrderUsageId extends IdWithType {

    public static String TYPE = "DA_iPartsPicOrderUsageId";

    protected enum INDEX {ORDER_GUID, PRODUCT_NO, EINPAS_HG, EINPAS_G, EINPAS_TU, KG, TU}

    /**
     * Der normale Konstruktor
     *
     * @param orderGuid
     */
    public iPartsPicOrderUsageId(String orderGuid, String productNo, String einPasHg, String einPasG, String einPasTu, String kg, String tu) {
        super(TYPE, new String[]{ orderGuid, productNo, einPasHg, einPasG, einPasTu, kg, tu });
    }

    /**
     * Konstruktor für EinPAS-Verwendung vom Bildauftrag
     *
     * @param orderGuid
     */
    public iPartsPicOrderUsageId(String orderGuid, iPartsProductId productId, EinPasId einPasId) {
        this(orderGuid, productId.getProductNumber(), einPasId.getHg(), einPasId.getG(), einPasId.getTu(), "", "");
    }

    /**
     * Konstruktor für KG/TU-Verwendung vom Bildauftrag
     *
     * @param orderGuid
     */
    public iPartsPicOrderUsageId(String orderGuid, iPartsProductId productId, KgTuId kgTuId) {
        this(orderGuid, productId.getProductNumber(), "", "", "", kgTuId.getKg(), kgTuId.getTu());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPicOrderUsageId() {
        this("", "", "", "", "", "", "");
    }

    /**
     * Liegt eine gültige ID vor (orderGuid und EinPAS bzw. KG/TU sind nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getOrderGuid().isEmpty() && !getProductNo().isEmpty() &&
               ((!getEinPasHg().isEmpty() && !getEinPasG().isEmpty() && !getEinPasTu().isEmpty() && getKg().isEmpty() && getTu().isEmpty())
                || (getEinPasHg().isEmpty() && getEinPasG().isEmpty() && getEinPasTu().isEmpty() && !getKg().isEmpty() && !getTu().isEmpty()));
    }

    public String getOrderGuid() {
        return id[INDEX.ORDER_GUID.ordinal()];
    }

    public String getProductNo() {
        return id[INDEX.PRODUCT_NO.ordinal()];
    }

    public String getEinPasHg() {
        return id[INDEX.EINPAS_HG.ordinal()];
    }

    public String getEinPasG() {
        return id[INDEX.EINPAS_G.ordinal()];
    }

    public String getEinPasTu() {
        return id[INDEX.EINPAS_TU.ordinal()];
    }

    public String getKg() {
        return id[INDEX.KG.ordinal()];
    }

    public String getTu() {
        return id[INDEX.TU.ordinal()];
    }
}
