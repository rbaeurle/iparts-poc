/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsHashHelper;
import de.docware.util.misc.id.IdWithType;

import java.security.MessageDigest;

/**
 * Repräsentiert die PK-Values aus der Tabelle DA_DIALOG_CHANGES im iParts Plug-in.
 */
public class iPartsDialogChangesId extends IdWithType {

    public static String TYPE = "DA_iPartsDialogChangesId";
    public static String DESCRIPTION = "!!DIALOG-Änderung";

    protected enum INDEX {DO_TYPE, DO_ID, HASH}

    /**
     * Erzeugt den MD5-Hashwert für die übergebenen Parameter unter Verwendung des übergebenen {@link MessageDigest}.
     *
     * @param seriesNo  Baureihennummer
     * @param bcteKey   BCTE-Schlüssel (dann ist <i>matNo</i> und <i>katalogId</i> {@code null} bzw. leer)
     * @param matNo     Materialnummer (dann ist <i>bcteKey</i> und <i>katalogId</i> {@code null} bzw. leer)
     * @param katalogId die ID in der Katalog-Tabelle (immer in Kombination mit dem BCTE-Schlüssel)
     *                  (dann ist <i>matNo</i> {@code null} bzw. leer)
     * @return
     */
    private static String createLocationKeyHash(String seriesNo, String bcteKey, String matNo, String katalogId) {
        if (bcteKey == null) {
            bcteKey = "";
        }
        if (matNo == null) {
            matNo = "";
        }
        if (katalogId == null) {
            katalogId = "";
        }
        return iPartsHashHelper.getInstance().createMD5Hash(seriesNo + "&" + bcteKey + "&" + matNo + "&" + katalogId);
    }

    public iPartsDialogChangesId(String doType, String doId, String hash) {
        super(TYPE, new String[]{ doType, doId, hash });
    }

    /**
     * Erzeugt eine neue ID mit den übergebenen Parametern unter Verwendung eines eigenen {@link MessageDigest} für die
     * Erzeugung vom MD5-Hashwert.
     *
     * @param changeType Änderungstyp
     * @param changeId   {@link IdWithType} mit der ID des geänderten Datensatzes
     * @param seriesNo   Baureihennummer
     * @param bcteKey    BCTE-Schlüssel (dann ist <i>matNo</i> und <i>katalogId</i> {@code null} bzw. leer)
     * @param matNo      Materialnummer (dann ist <i>bcteKey</i> und <i>katalogId</i> {@code null} bzw. leer)
     * @param katalogId  die ID in der Katalog-Tabelle (immer in Kombination mit dem BCTE-Schlüssel)
     *                   (dann ist <i>matNo</i> {@code null} bzw. leer)
     */
    public iPartsDialogChangesId(iPartsDataDIALOGChange.ChangeType changeType, IdWithType changeId,
                                 String seriesNo, String bcteKey, String matNo, String katalogId) {
        this(changeType.getDbKey(), changeId.toDBString(), createLocationKeyHash(seriesNo, bcteKey, matNo, katalogId));
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsDialogChangesId() {
        this("", "", "");
    }

    /**
     * Für iPartsDialogChangesId basierend auf einem Primärschlüssel-Array der passenden Länge
     *
     * @param primaryKeys
     */
    public iPartsDialogChangesId(String[] primaryKeys) {
        super(TYPE, primaryKeys);
        if (primaryKeys.length != INDEX.values().length) {
            throw new IllegalArgumentException("Array with primary keys for iPartsDialogChangesId doesn't have length "
                                               + INDEX.values().length);
        }
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public String getDoType() {
        return id[INDEX.DO_TYPE.ordinal()];
    }

    public String getDoId() {
        return id[INDEX.DO_ID.ordinal()];
    }

    public String getHash() {
        return id[INDEX.HASH.ordinal()];
    }
}
