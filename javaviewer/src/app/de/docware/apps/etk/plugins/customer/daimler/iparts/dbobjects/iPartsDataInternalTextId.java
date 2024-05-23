/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLStringConvert;

import java.util.Calendar;

/**
 * ID für das Datenobjekt {@link iPartsDataInternalText}. Nur dafür darf diese ID verwendet werden.
 * In der Programmlogik muss sonst immer {@link de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataInternalTextId} verwendet werden
 */
public class iPartsDataInternalTextId extends IdWithType {

    public static final String TYPE = "DA_iPartsDataInternalTextId";
    public static String DESCRIPTION = "!!Interner Text";

    private enum INDEX {U_ID, CREATION_TIMESTAMP, DO_TYPE, DO_ID}

    /**
     * Der normale Konstruktor
     *
     * @param userId
     * @param creationTimeStamp
     * @param dataObjectType
     * @param dataObjectId
     */
    public iPartsDataInternalTextId(String userId, String creationTimeStamp, String dataObjectType, String dataObjectId) {
        super(TYPE, new String[]{ userId, creationTimeStamp, dataObjectType, dataObjectId });
    }

    /**
     * Konstruktor ohne {@link IdWithType} und automatischer TimeStamp-Setzung
     *
     * @param userId
     */
    public iPartsDataInternalTextId(String userId) {
        this(userId, SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance()), "", "");
    }

    /**
     * Konstruktor mit {@link IdWithType}
     *
     * @param userId
     * @param creationTimeStamp
     * @param idWithType
     */
    public iPartsDataInternalTextId(String userId, String creationTimeStamp, IdWithType idWithType) {
        this(userId, creationTimeStamp, idWithType.getType(), idWithType.toDBString());
    }

    /**
     * Konstruktor mit {@link IdWithType} und automatischer TimeStamp-Setzung
     *
     * @param userId
     * @param idWithType
     */
    public iPartsDataInternalTextId(String userId, IdWithType idWithType) {
        this(userId, SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance()), idWithType);
    }

    /**
     * Für PEM basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsDataInternalTextId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsDataInternalTextId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDataInternalTextId() {
        this("", "", "", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getUserId() {
        return id[INDEX.U_ID.ordinal()];
    }

    public String getTimeStamp() {
        return id[INDEX.CREATION_TIMESTAMP.ordinal()];
    }

    public String getDataObjectType() {
        return id[INDEX.DO_TYPE.ordinal()];
    }

    public String getDataObjectId() {
        return id[INDEX.DO_ID.ordinal()];
    }
}
