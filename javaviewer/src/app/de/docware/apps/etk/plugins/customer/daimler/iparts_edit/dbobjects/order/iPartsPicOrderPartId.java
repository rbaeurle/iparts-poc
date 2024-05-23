/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order;

import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Bildauftrags-Stücklisten-ID im iParts Plug-in.
 */
public class iPartsPicOrderPartId extends IdWithType {

    public static String TYPE = "DA_iPartsPicOrderPartId";

    protected enum INDEX {ORDER_GUID, KATALOG_VARI, KATALOG_VER, KATALOG_LFDNR, HOTSPOT, PART_NUMBER}

    /**
     * Der normale Konstruktor
     *
     * @param orderGuid
     */
    public iPartsPicOrderPartId(String orderGuid, String kVari, String kVer, String kLfdNr, String hotspot, String partNumber) {
        super(TYPE, new String[]{ orderGuid, kVari, kVer, kLfdNr, hotspot, partNumber });
    }

    /**
     * Konstruktor für EinPAS-Verwendung vom Bildauftrag
     *
     * @param orderGuid
     */
    public iPartsPicOrderPartId(String orderGuid, PartListEntryId partListEntryId, String hotspot, String partNumber) {
        this(orderGuid, partListEntryId.getKVari(), partListEntryId.getKVer(), partListEntryId.getKLfdnr(), hotspot, partNumber);
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsPicOrderPartId() {
        this("", "", "", "", "", "");
    }

    /**
     * Liegt eine gültige ID vor (orderGuid und EinPAS bzw. KG/TU sind nicht leer)
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getOrderGuid().isEmpty() && !getKatalogVari().isEmpty() && !getKatalogVer().isEmpty() && !getKatalogLfdNr().isEmpty();
    }

    public String getOrderGuid() {
        return id[INDEX.ORDER_GUID.ordinal()];
    }

    public String getKatalogVari() {
        return id[INDEX.KATALOG_VARI.ordinal()];
    }

    public String getKatalogVer() {
        return id[INDEX.KATALOG_VER.ordinal()];
    }

    public String getKatalogLfdNr() {
        return id[INDEX.KATALOG_LFDNR.ordinal()];
    }

    public String getHotSpot() {
        return id[INDEX.HOTSPOT.ordinal()];
    }

    public String getPartNumber() {
        return id[INDEX.PART_NUMBER.ordinal()];
    }

    public PartListEntryId getPartListEntryId() {
        return new PartListEntryId(getKatalogVari(), getKatalogVer(), getKatalogLfdNr());
    }
}
