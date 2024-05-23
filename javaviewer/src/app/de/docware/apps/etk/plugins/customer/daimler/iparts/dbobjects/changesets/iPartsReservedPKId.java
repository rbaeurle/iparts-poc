/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Primärschlüssel-Reservierung für die Tabelle DA_RESERVED_PK im iParts Plug-in.
 */
public class iPartsReservedPKId extends IdWithType {

    public static final String TYPE = "DA_iPartsReplacePKId";

    protected enum INDEX {DO_TYPE, DO_ID}

    /**
     * Der normale Konstruktor
     *
     * @param dataObjectType
     * @param dataObjectId
     */
    public iPartsReservedPKId(String dataObjectType, String dataObjectId) {
        super(TYPE, new String[]{ dataObjectType, dataObjectId });
    }

    /**
     * Konstruktor für einen beliebigen Primärschlüssel als ID.
     *
     * @param primaryKey
     */
    public iPartsReservedPKId(IdWithType primaryKey) {
        this(primaryKey.getType(), primaryKey.toDBString());
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsReservedPKId() {
        this("", "");
    }

    public String getDataObjectType() {
        return id[INDEX.DO_TYPE.ordinal()];
    }

    public String getDataObjectId() {
        return id[INDEX.DO_ID.ordinal()];
    }

    /**
     * Erzeugt eine generische ID für den Primärschlüssel dieser Primärschlüssel-Reservierung.
     *
     * @return
     */
    public IdWithType getAsDataObjectId() {
        return IdWithType.fromDBString(getDataObjectType(), getDataObjectId());
    }
}