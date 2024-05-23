/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.framework.combimodules.useradmin.db.UserAdminUserCache;
import de.docware.framework.combimodules.useradmin.db.UserAdminUserCacheElement;

/**
 * Cache für die Rechte und Eigenschaften einzelner Benutzer in der Benutzerverwaltung.
 */
public class iPartsUserAdminCache extends UserAdminUserCache {

    public static synchronized iPartsUserAdminCacheElement getInstance(String userId) {
        UserAdminUserCacheElement userAdminUserCache = UserAdminUserCache.getInstance(userId);
        if (userAdminUserCache instanceof iPartsUserAdminCacheElement) {
            return (iPartsUserAdminCacheElement)userAdminUserCache;
        } else { // Kann eigentlich nicht passieren
            return new iPartsUserAdminCacheElement(userId);
        }
    }

    /**
     * Liefert den Benutzer-Cache für den übergebenen Benutzer-Namen zurück.
     *
     * @param userName
     * @return {@code null} falls kein Benutzer mit dem übergebenen Benutzer-Namen eindeutig gefunden werden konnte
     */
    public static iPartsUserAdminCacheElement getCacheByUserName(String userName) {
        return (iPartsUserAdminCacheElement)UserAdminUserCache.getCacheByUserName(userName);
    }
}
