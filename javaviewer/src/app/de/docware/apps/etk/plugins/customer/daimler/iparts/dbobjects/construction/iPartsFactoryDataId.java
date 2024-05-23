/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.util.misc.id.IdWithType;

/**
 * Repräsentiert eine Werkseinsatzdaten-GUID aus der Tabelle TABLE_DA_FACTORY_DATA im iParts Plug-in.
 */
public class iPartsFactoryDataId extends IdWithType {

    public static final String TYPE = "DA_iPartsFactoryDataId";
    public static final String DESCRIPTION = "!!Werkseinsatzdaten";

    protected enum INDEX {GUID, FACTORY, SPKZ, ADAT, DATA_ID, SEQ_NO}

    /**
     * Konstruktor für DIALOG
     *
     * @param guid    DIALOG GUID
     * @param factory
     * @param spkz
     * @param adat
     * @param dataId
     */
    public iPartsFactoryDataId(String guid, String factory, String spkz, String adat, String dataId) {

        super(TYPE, new String[]{ guid, factory, spkz, adat, dataId, "" });
    }

    /**
     * Konstruktor für ELDAS
     *
     * @param guid       ELDAS GUID
     * @param sequenceNo zur Erzeugung eines eindeutigen PK
     */
    public iPartsFactoryDataId(String guid, String sequenceNo) {
        super(TYPE, new String[]{ guid, "", "", "", "", sequenceNo });
    }


    /**
     * Konstruktor für ELDAS, wenn Werksdaten kopiert werden
     * Hier ist ADAT noch nötg
     *
     * @param guid       ELDAS GUID
     * @param adat       ADAT
     * @param sequenceNo zur Erzeugung eines eindeutigen PK
     */
    public iPartsFactoryDataId(String guid, String adat, String sequenceNo) {
        super(TYPE, new String[]{ guid, "", "", adat, "", sequenceNo });
    }


    /**
     * Konstruktor für den Compiler ;-)
     *
     * @param guid       DIALOG GUID
     * @param factory
     * @param spkz
     * @param adat
     * @param dataId
     * @param eldasSeqNo
     */
    public iPartsFactoryDataId(String guid, String factory, String spkz, String adat, String dataId, String eldasSeqNo) {

        super(TYPE, new String[]{ guid, factory, spkz, adat, dataId, eldasSeqNo });
    }

    /**
     * Für Werkseinsatzdaten basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsFactoryDataId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsFactoryDataId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsFactoryDataId() {
        this("", "");
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getGuid() {
        return id[INDEX.GUID.ordinal()];
    }

    public String getFactory() {
        return id[INDEX.FACTORY.ordinal()];
    }

    public String getSplitAttribute() {
        return id[INDEX.SPKZ.ordinal()];
    }

    public String getAdat() {
        return id[INDEX.ADAT.ordinal()];
    }

    public String getDataId() {
        return id[INDEX.DATA_ID.ordinal()];
    }

    public String getSeqNo() {
        return id[INDEX.SEQ_NO.ordinal()];
    }

    public iPartsDialogBCTEPrimaryKey getBCTEPrimaryKey() {
        return iPartsDialogBCTEPrimaryKey.createFromDialogGuid(getGuid());
    }
}
