/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Ursprung für mögliche Importdaten
 */
public enum iPartsImportDataOrigin {

    EDS("BOM-DB", false, true),
    DIALOG("DIALOG", true, false),
    MAD("MAD", true, true),
    ELDAS("ELDAS", true, true),
    PRIMUS("PRIMUS", true, true),
    SRM("SRM", true, true),
    APP_LIST("APP_LIST", true, true),
    IPARTS("IPARTS", true, true),
    IPARTS_MB("IPARTS-MB", true, false),
    IPARTS_TRUCK("IPARTS-TRUCK", false, true),
    EPC("EPC", true, true),
    PROVAL("PROVAL", true, true),
    SAP_MBS("MBS", false, true),
    SAP_CTT("CTT", false, true),
    NUTZDOK("NUTZDOK", false, true),
    PSK("PSK", true, true),
    CONNECT("Connect", true, false),
    IPARTS_GENVO("IPARTS-GENVO", true, false),
    IPARTS_SPK("IPARTS-SPK", true, false),
    UNKNOWN("unknown", true, true);

    protected static Set<iPartsImportDataOrigin> validSourcesCarAndVan = new HashSet<>();
    protected static Set<iPartsImportDataOrigin> validSourcesTruckAndBus = new HashSet<>();
    protected static Set<iPartsImportDataOrigin> invalidSourcesForDictionaryCache = new HashSet<>();

    static {
        for (iPartsImportDataOrigin importOrigin : iPartsImportDataOrigin.values()) {
            if (importOrigin.isCarAndVan()) {
                validSourcesCarAndVan.add(importOrigin);
            }
            if (importOrigin.isTruckAndBus()) {
                validSourcesTruckAndBus.add(importOrigin);
            }
        }
        invalidSourcesForDictionaryCache.add(CONNECT);
        invalidSourcesForDictionaryCache.add(IPARTS_SPK);
        invalidSourcesForDictionaryCache.add(UNKNOWN);
    }

    private String origin;
    private boolean isCarAndVan;
    private boolean isTruckAndBus;

    public static iPartsImportDataOrigin getTypeFromCode(String originValue) {
        if (originValue != null) {
            originValue = originValue.trim();
            for (iPartsImportDataOrigin type : values()) {
                if (type.getOrigin().equals(originValue)) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }

    /**
     * Liefert die passende iParts-Quelle abhängig von den Eigenschaften des eingeloggten Benutzers der aktuellen Session
     * zurück.
     *
     * @return
     */
    public static String getIPartsSourceForCurrentSession() {
        if (iPartsRight.checkCarAndVanInSession()) { // IPARTS-MB soll auch zurückgegeben werden, wenn ein Benutzer beide Eigenschaften hat
            return iPartsImportDataOrigin.IPARTS_MB.getOrigin();
        } else if (iPartsRight.checkTruckAndBusInSession()) {
            return iPartsImportDataOrigin.IPARTS_TRUCK.getOrigin();
        } else { // Fallback falls der eingeloggte Benutzer keine der beiden Eigenschaften hat
            return iPartsImportDataOrigin.IPARTS.getOrigin();
        }
    }

    /**
     * Überprüft, ob die Quelle mit den übergebenen Benutzer-Eigenschaften sichtbar ist.
     *
     * @param sourceKey
     * @param carAndVanInSession
     * @param truckAndBusInSession
     * @return
     */
    public static boolean isSourceVisible(String sourceKey, boolean carAndVanInSession, boolean truckAndBusInSession) {
        if (carAndVanInSession && truckAndBusInSession) {
            return true;
        }
        if (carAndVanInSession) {
            return isSourceValidForCarAndVan(sourceKey);
        } else if (truckAndBusInSession) {
            return isSourceValidForTruckAndBus(sourceKey);
        }
        return false;
    }

    public static boolean isSourceValidForCarAndVan(String source) {
        return isSourceValidForCarAndVan(getTypeFromCode(source));
    }

    public static boolean isSourceValidForCarAndVan(iPartsImportDataOrigin source) {
        return validSourcesCarAndVan.contains(source);
    }

    public static boolean isSourceValidForTruckAndBus(String source) {
        return isSourceValidForTruckAndBus(getTypeFromCode(source));
    }

    public static boolean isSourceValidForTruckAndBus(iPartsImportDataOrigin source) {
        return validSourcesTruckAndBus.contains(source);
    }

    public static iPartsImportDataOrigin getiPartsSourceForRights(boolean isCarOrVan, boolean isTruckOrBus) {
        if (isCarOrVan) {
            if (isTruckOrBus) {
                return iPartsImportDataOrigin.IPARTS;
            } else {
                return iPartsImportDataOrigin.IPARTS_MB;
            }
        } else if (isTruckOrBus) {
            return iPartsImportDataOrigin.IPARTS_TRUCK;
        }
        return null;
    }

    public static boolean isSourceValidForDictCacheCombTexts(iPartsImportDataOrigin source) {
        return !invalidSourcesForDictionaryCache.contains(source);
    }

    iPartsImportDataOrigin(String origin, boolean isCarAndVan, boolean isTruckAndBus) {
        this.origin = origin;
        this.isCarAndVan = isCarAndVan;
        this.isTruckAndBus = isTruckAndBus;
    }


    public String getOrigin() {
        return origin;
    }

    public boolean isCarAndVan() {
        return isCarAndVan;
    }

    public boolean isTruckAndBus() {
        return isTruckAndBus;
    }
}
