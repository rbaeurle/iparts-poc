/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert die PK-Values aus der Tabelle TABLE_DA_EDS_CONST_KIT im iParts Plug-in.
 */
public class iPartsBOMConstKitContentId extends IdWithType {

    public static String TYPE = "DA_iPartsBOMConstKitContentId";

    protected enum INDEX {CONST_KIT_NO, PARTPOS, REV_FROM}

    /**
     * Der normale Konstruktor
     *
     * @param constKitNumber
     * @param partPos
     * @param revFrom
     */
    public iPartsBOMConstKitContentId(String constKitNumber, String partPos, String revFrom) {
        super(TYPE, new String[]{ constKitNumber, partPos, revFrom });
    }

    /**
     * ID aus Attributen
     *
     * @param attributes
     */
    public iPartsBOMConstKitContentId(DBDataObjectAttributes attributes) {
        this(attributes.getField(iPartsConst.FIELD_DCK_SNR).getAsString(),
             attributes.getField(iPartsConst.FIELD_DCK_PARTPOS).getAsString(),
             attributes.getField(iPartsConst.FIELD_DCK_REVFROM).getAsString());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsBOMConstKitContentId() {
        this("", "", "");
    }

    /**
     * Liegt eine gültige ID vor
     *
     * @return true, falls gültige Id
     */
    public boolean isValidId() {
        return !getConstKitNo().isEmpty() && !getPartPos().isEmpty() && !getRevFrom().isEmpty();
    }

    public String getConstKitNo() {
        return id[INDEX.CONST_KIT_NO.ordinal()];
    }

    public String getPartPos() {
        return id[INDEX.PARTPOS.ordinal()];
    }

    public String getRevFrom() {
        return id[INDEX.REV_FROM.ordinal()];
    }


}
