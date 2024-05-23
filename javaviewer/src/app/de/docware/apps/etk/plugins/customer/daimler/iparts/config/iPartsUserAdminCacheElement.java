package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.framework.combimodules.useradmin.db.RightScope;
import de.docware.framework.combimodules.useradmin.db.UserAdminOrganisationCache;
import de.docware.framework.combimodules.useradmin.db.UserAdminUserCacheElement;
import de.docware.util.StrUtils;

import java.util.*;

public class iPartsUserAdminCacheElement extends UserAdminUserCacheElement {

    private Map<String, Boolean> memberOfVirtualUserGroupsMap = new HashMap<>();

    protected iPartsUserAdminCacheElement(String userId) {
        super(userId);
    }

    /**
     * Liefert alle gültigen Organisations-IDs des Benutzers für die Zuweisung an Benutzer oder virtuelle Benutzergruppen
     * zurück.
     *
     * @param right Recht für die Ermittlung der Benutzer und virtuellen Benutzergruppen (speziell bzgl. Gültigkeitsbereich)
     * @return
     */
    public Set<String> getValidOrgIdsForAssignUserOrGroup(iPartsRight right) {
        RightScope rightScope = getUserRightScope(right);
        Set<String> validOrgIds = new HashSet<>();
        switch (rightScope) {
            case CURRENT_ORGANISATION:
                // Nur die Organisation des Benutzers
                validOrgIds.add(getOrgId());
                break;
            case CURRENT_ORGANISATION_TREE:
                // Organisation des Benutzers und alle Unter-Organisationen
                String orgId = getOrgId();
                validOrgIds.add(orgId);
                validOrgIds.addAll(iPartsUserAdminOrgCache.getInstance(orgId).getSubOrgIds());
                break;
            case USER_ORGANISATIONS:
                // Alle Organisationen des Benutzers
                validOrgIds.addAll(getAssignedOrgIds());
                break;
            case USER_ORGANISATIONS_TREE:
                // Alle Organisationen des Benutzers und deren Unter-Organisationen
                Set<String> userOrgIds = getAssignedOrgIds();
                for (String userOrgId : userOrgIds) {
                    validOrgIds.add(userOrgId);
                    validOrgIds.addAll(iPartsUserAdminOrgCache.getInstance(userOrgId).getSubOrgIds());
                }
                break;
            case GLOBAL:
                // Alle Organisationen
                for (UserAdminOrganisationCache orgCache : iPartsUserAdminOrgCache.getAllOrgCaches()) {
                    validOrgIds.add(orgCache.getOrgId());
                }
                break;
        }
        return validOrgIds;
    }

    /**
     * Liefert eine {@link Map} mit allen vollständigen Namen und IDs der für den Benutzer bei einer Zuweisung zulässigen
     * Benutzern zurück.
     *
     * @param roleIds Optionale Rollen-IDs für die Filterung, wobei die Benutzer mindestens eine der geforderten Rollen
     *                haben müssen
     * @param right   Recht für die Ermittlung der Benutzer und virtuellen Benutzergruppen (speziell bzgl. Gültigkeitsbereich)
     * @return
     */
    public Map<String, String> getAssignableUsers(Set<String> roleIds, iPartsRight right) {
        Map<String, String> assignableUsersMap = new TreeMap<>();

        // Den Benutzer selbst zur Map hinzufügen falls er mindestens eine der geforderten Rollen hat
        boolean userHasRole = true;
        if ((roleIds != null) && !roleIds.isEmpty()) {
            userHasRole = false;
            for (String roleId : roleIds) {
                if (isUserRole(roleId)) {
                    userHasRole = true;
                    break;
                }
            }
        }
        String dbLanguage = iPartsUserAdminDb.get().getConfig().getCurrentDatabaseLanguage();
        if (userHasRole) {
            assignableUsersMap.put(getUserFullName(dbLanguage), userId);
        }

        Set<String> validOrgIds = getValidOrgIdsForAssignUserOrGroup(right);

        // Benutzer für die gültigen Organisationen hinzufügen
        for (String validOrgId : validOrgIds) {
            iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(validOrgId);
            Set<String> assignableUserIds;
            if ((roleIds != null) && !roleIds.isEmpty()) {
                assignableUserIds = new TreeSet<>();
                for (String roleId : roleIds) { // Der Benutzer muss mindestens eine der geforderten Rollen haben
                    assignableUserIds.addAll(orgCache.getUserIdsWithRole(roleId));
                }
            } else {
                assignableUserIds = orgCache.getUserIds();
            }

            for (String assignableUserId : assignableUserIds) {
                if (!assignableUserId.equals(userId)) {
                    assignableUsersMap.put(iPartsUserAdminCache.getInstance(assignableUserId).getUserFullName(dbLanguage),
                                           assignableUserId);
                }
            }
        }
        return assignableUsersMap;
    }

    /**
     * Überprüft, ob der Benutzer die übergebene (virtuelle) Rolle hat.
     *
     * @param roleId
     * @return
     */
    @Override
    public boolean isUserRole(String roleId) {
        if (roleId.equals(iPartsUserAdminDb.VIRTUAL_ROLE_ID_AUTHOR_ALL)) { // Spezialverhalten für die virtuelle Autoren-Rolle
            for (String userRoleId : getRoleIds()) {
                if (userRoleId.startsWith(iPartsUserAdminDb.ROLE_ID_AUTHOR_INT)) {
                    return true;
                }
            }
        } else if (roleId.equals(iPartsUserAdminDb.VIRTUAL_ROLE_ID_QUALITY_INSPECTOR_ALL)) { // Spezialverhalten für die virtuelle Qualitätsprüfer-Rolle
            for (String userRoleId : getRoleIds()) {
                if (userRoleId.startsWith(iPartsUserAdminDb.ROLE_ID_QUALITY_INSPECTOR)) {
                    return true;
                }
            }
        } else {
            return super.isUserRole(roleId);
        }

        return false;
    }

    /**
     * Überprüft, ob dieser Benutzer Mitglied der übergebenen virtuellen Benutzergruppe ist.
     *
     * @param virtualUserGroupId
     * @return
     */
    public boolean isMemberOfVirtualUserGroup(String virtualUserGroupId) {
        if (StrUtils.isEmpty(virtualUserGroupId)) {
            return false;
        }

        synchronized (memberOfVirtualUserGroupsMap) {
            Boolean isMemberOfVirtualUserGroup = memberOfVirtualUserGroupsMap.get(virtualUserGroupId);
            if (isMemberOfVirtualUserGroup == null) {
                isMemberOfVirtualUserGroup = false;
                iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getOrgByVirtualUserGroupId(virtualUserGroupId);
                if (orgCache != null) {
                    if (orgCache.getUserIds().contains(userId)) {
                        String roleId = iPartsVirtualUserGroup.getRoleIdFromVirtualUserGroupId(virtualUserGroupId);
                        isMemberOfVirtualUserGroup = isUserRole(roleId);
                    }
                }

                memberOfVirtualUserGroupsMap.put(virtualUserGroupId, isMemberOfVirtualUserGroup);
            }
            return isMemberOfVirtualUserGroup;
        }
    }
}
