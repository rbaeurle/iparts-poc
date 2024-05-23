/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Bildauftrags-Picture-ID im iParts Plug-in.
 */
public class iPartsPicOrderPictureId extends IdWithType {

    public static String TYPE = "DA_iPartsPicOrderPictureId";

    protected enum INDEX {ORDER_GUID, PIC_ITEMID, PIC_ITEMREVID}

    /**
     * Der normale Konstruktor
     *
     * @param orderGuid
     */
    public iPartsPicOrderPictureId(String orderGuid, String picItemId, String picItemRevId) {
        super(TYPE, new String[]{ orderGuid, picItemId, picItemRevId });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPicOrderPictureId() {
        this("", "", "");
    }

    /**
     * Liegt eine gültige ID vor (orderGuid und EinPAS bzw. KG/TU sind nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getOrderGuid().isEmpty() && !getPicItemId().isEmpty() && !getPicItemRevId().isEmpty();
    }

    public String getOrderGuid() {
        return id[INDEX.ORDER_GUID.ordinal()];
    }

    public String getPicItemId() {
        return id[INDEX.PIC_ITEMID.ordinal()];
    }

    public String getPicItemRevId() {
        return id[INDEX.PIC_ITEMREVID.ordinal()];
    }
}
