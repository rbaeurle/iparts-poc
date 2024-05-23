/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Gültigkeitsbereich bzgl. der Benutzer-Eigenschaften für PKW/Van und Truck/Bus
 */
public enum iPartsValidityScope {

    IPARTS("IPARTS", true, true),
    IPARTS_MB("IPARTS-MB", true, false),
    IPARTS_TRUCK("IPARTS-TRUCK", false, true),
    UNUSED("UNUSED", false, false);

    protected static Set<iPartsValidityScope> validSourcesCarAndVan = new HashSet<>();
    protected static Set<iPartsValidityScope> validSourcesTruckAndBus = new HashSet<>();

    static {
        for (iPartsValidityScope importOrigin : iPartsValidityScope.values()) {
            if (importOrigin.isCarAndVan()) {
                validSourcesCarAndVan.add(importOrigin);
            }
            if (importOrigin.isTruckAndBus()) {
                validSourcesTruckAndBus.add(importOrigin);
            }
        }
    }

    private String scopeKey;
    private boolean isCarAndVan;
    private boolean isTruckAndBus;

    public static iPartsValidityScope getValidityScope(String scopeKey) {
        if (scopeKey != null) {
            scopeKey = scopeKey.trim();
            for (iPartsValidityScope type : values()) {
                if (type.getScopeKey().equals(scopeKey)) {
                    return type;
                }
            }
        }
        return UNUSED;
    }

    /**
     * Überprüft, ob der Gültigkeitsbereich mit den übergebenen Benutzer-Eigenschaften gültig ist.
     *
     * @param scopeKey
     * @param carAndVanInSession
     * @param truckAndBusInSession
     * @return
     */
    public static boolean isScopeValid(String scopeKey, boolean carAndVanInSession, boolean truckAndBusInSession) {
        if (carAndVanInSession && truckAndBusInSession) {
            return true;
        }
        if (carAndVanInSession) {
            return isScopeValidForCarAndVan(scopeKey);
        } else if (truckAndBusInSession) {
            return isScopeValidForTruckAndBus(scopeKey);
        }
        return false;
    }

    public static boolean isScopeValidForCarAndVan(String scopeKey) {
        return isScopeValidForCarAndVan(getValidityScope(scopeKey));
    }

    public static boolean isScopeValidForCarAndVan(iPartsValidityScope scope) {
        return validSourcesCarAndVan.contains(scope);
    }

    public static boolean isScopeValidForTruckAndBus(String scopeKey) {
        return isScopeValidForTruckAndBus(getValidityScope(scopeKey));
    }

    public static boolean isScopeValidForTruckAndBus(iPartsValidityScope scope) {
        return validSourcesTruckAndBus.contains(scope);
    }

    iPartsValidityScope(String scopeKey, boolean isCarAndVan, boolean isTruckAndBus) {
        this.scopeKey = scopeKey;
        this.isCarAndVan = isCarAndVan;
        this.isTruckAndBus = isTruckAndBus;
    }

    public String getScopeKey() {
        return scopeKey;
    }

    public boolean isCarAndVan() {
        return isCarAndVan;
    }

    public boolean isTruckAndBus() {
        return isTruckAndBus;
    }
}