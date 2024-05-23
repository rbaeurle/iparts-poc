package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enums und Zugriffsfunktionen für die verschiedenen iParts User-Gruppen
 */
public enum iPartsUserTypes {

    ASSIGN("!!Zuweisen"),
    AUTHOR("!!Autor festlegen"),
    QA("!!Qualitätsprüfer festlegen");

    private String title;

    iPartsUserTypes(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Liefert eine Map mit allen für den übergebenen Benutzer bei einer Zuweisung gültigen vollständigen Benutzernamen bzw.
     * virtuellen Benutzergruppen als Schlüssel und ID als Wert zurück.
     *
     * @param userId
     * @param right         Recht für die Ermittlung der Benutzer und virtuellen Benutzergruppen (speziell bzgl. Gültigkeitsbereich)
     * @param filterByRoles Filtert die Benutzer anhand der passenden Rollen
     * @return
     */
    public Map<String, String> getUsersMap(String userId, iPartsRight right, boolean filterByRoles) {
        Map<String, String> usersMap = new LinkedHashMap<>(); // Die virtuellen Benutzergruppen und Benutzer sind bereits sortiert
        if (userId != null) {
            iPartsUserAdminCacheElement userCache = iPartsUserAdminCache.getInstance(userId);
            boolean virtualUserGroupsForAuthors = false;
            boolean virtualUserGroupsForQualityInspectors = false;
            Set<String> roleIds = new HashSet<>();
            switch (this) {
                case ASSIGN:
                    virtualUserGroupsForAuthors = true;
                    virtualUserGroupsForQualityInspectors = true;
                    roleIds.add(iPartsUserAdminDb.VIRTUAL_ROLE_ID_AUTHOR_ALL);
                    roleIds.add(iPartsUserAdminDb.VIRTUAL_ROLE_ID_QUALITY_INSPECTOR_ALL);
                    break;
                case AUTHOR:
                    virtualUserGroupsForAuthors = true;
                    roleIds.add(iPartsUserAdminDb.VIRTUAL_ROLE_ID_AUTHOR_ALL);
                    break;
                case QA:
                    virtualUserGroupsForQualityInspectors = true;
                    roleIds.add(iPartsUserAdminDb.VIRTUAL_ROLE_ID_QUALITY_INSPECTOR_ALL);
                    break;
            }

            if (virtualUserGroupsForAuthors || virtualUserGroupsForQualityInspectors) {
                usersMap.putAll(iPartsVirtualUserGroup.getVirtualUserGroupsForUserId(userId, virtualUserGroupsForAuthors,
                                                                                     virtualUserGroupsForQualityInspectors,
                                                                                     right));
            }
            usersMap.putAll(userCache.getAssignableUsers(filterByRoles ? roleIds : null, right));
        }
        return usersMap;
    }
}
