/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Hilfsklasse für virtuelle Benutzergruppen in iParts.
 * Virtuelle Benutzergruppen-IDs haben die Form {@link #VIRTUAL_USER_GROUP_PREFIX}orgID{@link #VIRTUAL_USER_GROUP_DELIMITER}roleId.
 * Beispiel: @@vug@@DAIMLER&IPARTS.QualityInspector
 */
public class iPartsVirtualUserGroup {

    public static final String VIRTUAL_USER_GROUP_PREFIX = "@@vug@@";
    public static final String VIRTUAL_USER_GROUP_DELIMITER = "&"; // Organisations-IDs können nie ein & enthalten -> kein Escaping notwendig

    public static boolean isVirtualUserGroupId(String id) {
        return id.startsWith(VIRTUAL_USER_GROUP_PREFIX);
    }

    /**
     * Liefert die virtuelle Benutzergruppe für die übergebene Organisation und die Rolle zurück.
     *
     * @param roleId
     * @return
     */
    public static String getVirtualUserGroupId(String orgId, String roleId) {
        return VIRTUAL_USER_GROUP_PREFIX + orgId + VIRTUAL_USER_GROUP_DELIMITER + roleId;
    }

    /**
     * Liefert die Benennung für die übergebene virtuelle Benutzergruppe zurück.
     *
     * @param virtualUserGroupId
     * @return
     */
    public static String getVirtualUserGroupName(String virtualUserGroupId) {
        if (StrUtils.isEmpty(virtualUserGroupId)) {
            return "";
        }

        String orgId = getOrgIdFromVirtualUserGroupId(virtualUserGroupId);
        if (StrUtils.isValid(orgId)) {
            String roleId = getRoleIdFromVirtualUserGroupId(virtualUserGroupId);
            if (roleId != null) {
                return iPartsUserAdminOrgCache.getInstance(orgId).getVirtualUserGroupName(roleId);
            }
        }

        return TranslationHandler.translate("!!Ungültige virtuelle Benutzergruppe \"%1\"", virtualUserGroupId);
    }

    /**
     * Liefert die Organisations-ID für die übergebene virtuelle Benutzergruppe zurück.
     *
     * @param virtualUserGroupId
     * @return {@code null} falls es sich nicht um eine virtuelle Benutzergruppe handelt
     */
    public static String getOrgIdFromVirtualUserGroupId(String virtualUserGroupId) {
        if (isVirtualUserGroupId(virtualUserGroupId)) {
            return StrUtils.stringUpToCharacter(virtualUserGroupId.substring(VIRTUAL_USER_GROUP_PREFIX.length()), VIRTUAL_USER_GROUP_DELIMITER);
        } else {
            return null;
        }
    }

    /**
     * Liefert die Rollen-ID für die übergebene virtuelle Benutzergruppe zurück.
     *
     * @param virtualUserGroupId
     * @return {@code null} falls es sich nicht um eine virtuelle Benutzergruppe handelt
     */
    public static String getRoleIdFromVirtualUserGroupId(String virtualUserGroupId) {
        if (isVirtualUserGroupId(virtualUserGroupId)) {
            return StrUtils.stringAfterCharacter(virtualUserGroupId.substring(VIRTUAL_USER_GROUP_PREFIX.length()), VIRTUAL_USER_GROUP_DELIMITER);
        } else {
            return null;
        }
    }

    /**
     * Liefert eine {@link Map} mit allen Benennungen und IDs der gewünschten virtuellen Benutzergruppen für den eingeloggten
     * Benutzer zurück.
     *
     * @param userId
     * @param forAuthors           Virtuelle Benutzergruppen für Autoren zurückliefern?
     * @param forQualityInspectors Virtuelle Benutzergruppen für Qualitätsprüfer zurückliefern?
     * @param right                Recht für die Ermittlung der Benutzer und virtuellen Benutzergruppen (speziell bzgl. Gültigkeitsbereich)
     * @return
     */
    public static Map<String, String> getVirtualUserGroupsForUserId(String userId, boolean forAuthors, boolean forQualityInspectors,
                                                                    iPartsRight right) {
        Map<String, String> virtualUserGroupsMap = new TreeMap<>();
        if (!forAuthors && !forQualityInspectors) {
            return virtualUserGroupsMap; // So ein Quatsch...
        }

        if (userId != null) {
            iPartsUserAdminCacheElement userCache = iPartsUserAdminCache.getInstance(userId);
            Set<String> validOrgIds = userCache.getValidOrgIdsForAssignUserOrGroup(right);

            // Virtuelle Benutzergruppen für die gültigen Organisationen erzeugen
            for (String validOrgId : validOrgIds) {
                iPartsUserAdminOrgCache orgCache = iPartsUserAdminOrgCache.getInstance(validOrgId);

                // Für die Organisation müssen virtuelle Benutzergruppen zulässig sein
                if (orgCache.hasVirtualUserGroups()) {
                    // Autoren
                    if (forAuthors) {
                        virtualUserGroupsMap.put(orgCache.getVirtualUserGroupName(iPartsUserAdminDb.VIRTUAL_ROLE_ID_AUTHOR_ALL),
                                                 orgCache.getVirtualUserGroupIdForAuthors());
                    }

                    // Qualitätsprüfer
                    if (forQualityInspectors) {
                        virtualUserGroupsMap.put(orgCache.getVirtualUserGroupName(iPartsUserAdminDb.VIRTUAL_ROLE_ID_QUALITY_INSPECTOR_ALL),
                                                 orgCache.getVirtualUserGroupIdForQualityInspectors());
                    }
                }
            }
        }

        return virtualUserGroupsMap;
    }
}
