/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.helper;

import de.docware.util.StrUtils;

import java.nio.charset.StandardCharsets;

/**
 * Hilfsklasse für den FNV1A-Hash-Algorithmus
 */
public class FastSearchHashHelper {

    private static final int FNV_32_INIT = 0x811c9dc5;
    //    private static final int FNV_32_PRIME = 0x01000193;
    private static final int FNV_32_PRIME = 16777619;

    /**
     * Hash Berechnung nach FNV1A
     *
     * @param textBytes
     * @return
     */
    public static long hash32(byte[] textBytes) {
        int hash = FNV_32_INIT;
        int len = textBytes.length;
        for (int i = 0; i < len; i++) {
            // XOR
            hash ^= textBytes[i];
            // Mit der Primzahl multiplizieren
            hash *= FNV_32_PRIME;
        }
        return Integer.toUnsignedLong(hash);
    }

    /**
     * Berechnet den spezifischen POS-Hash für den ElasticSearch Index
     *
     * @param cleanModelId
     * @param kg
     * @param tu
     * @param partNo
     * @param hotspot
     * @return
     */
    public static long createPOSHash(String cleanModelId, String kg, String tu, String partNo, String hotspot) {
        // Beispiel: BM 223155, KG 21, TU 050, Teilenummer A2569980000, Hotspot 10
        // String: 223155_21_050_A2569980000_10 -> Hash: 1801291542
        String delimiter = FastSearchExportHelper.TEXT_DELIMITER_FOR_HASH;
        String textValue = StrUtils.getEmptyOrValidString(cleanModelId) + delimiter + StrUtils.getEmptyOrValidString(kg)
                           + delimiter + StrUtils.getEmptyOrValidString(tu) + delimiter + StrUtils.getEmptyOrValidString(partNo)
                           + delimiter + StrUtils.getEmptyOrValidString(hotspot);
        return hash32(textValue.getBytes(StandardCharsets.UTF_8));
    }
}
