/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Helper, um Hash-Werte zu erzeugen
 */
public class iPartsHashHelper {

    private static iPartsHashHelper instance;

    private MessageDigest md5Instance; // MD5 Hashfunktion

    public static iPartsHashHelper getInstance() {
        if (instance == null) {
            instance = new iPartsHashHelper();
        }
        return instance;
    }

    public iPartsHashHelper() {
        try {
            // Hier alle notwendigen Algorithmen instanziieren
            md5Instance = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) { // Dürfte in der Praxis nie vorkommen
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            Logger.getLogger().throwRuntimeException(e);
        }
    }

    /**
     * Liefert einen MD5 Hash-Wert für den übergebenen Input
     *
     * @param input
     * @return
     */
    public synchronized String createMD5Hash(String input) {
        return createHash(md5Instance, input);
    }

    /**
     * Liefert einen Hash-Wert für den übergebenen Input mit Hilfe des übergebenen {@link MessageDigest} Algorithmus
     *
     * @param messageDigest
     * @param input
     * @return
     */
    private synchronized String createHash(MessageDigest messageDigest, String input) {
        return Hex.toString(messageDigest.digest(input.getBytes()));
    }

    /**
     * Liefert einen eindeutigen Hash-Wert (BOM-Schlüssel) basierend auf den DIALOG Strukturen "HM", "M", "SM" und "POSE"
     * zurück.
     *
     * @param bctePrimaryKey
     * @param hashValuesCache
     * @return
     */
    public String getHashValueForBCTEPrimaryKey(iPartsDialogBCTEPrimaryKey bctePrimaryKey, Map<String, String> hashValuesCache) {
        if (bctePrimaryKey == null) {
            return "";
        }

        String hm = bctePrimaryKey.hm;
        String m = bctePrimaryKey.m;
        String sm = bctePrimaryKey.sm;
        String pos = bctePrimaryKey.posE;

        // Damit es nicht zu einfach wird, ist die Reihenfolge ungleich der "natürlichen" Reihenfolge in DIALOG (HM/M/SM und POS)
        String tempGroupNumber = m + pos + sm + hm;
        String result = hashValuesCache.get(tempGroupNumber);
        if (result == null) {
            String md5HashValue = createMD5Hash(tempGroupNumber);
            hashValuesCache.put(tempGroupNumber, md5HashValue);
            result = md5HashValue;
        }
        return result;
    }
}
