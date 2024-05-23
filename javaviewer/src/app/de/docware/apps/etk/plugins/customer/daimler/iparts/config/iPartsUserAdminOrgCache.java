/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.combimodules.useradmin.db.UserAdminOrganisationCache;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cache für die Organisationen in der Benutzerverwaltung inkl. deren Benutzer und Eigenschaften sowie virtuellen Benutzergruppen.
 */
public class iPartsUserAdminOrgCache extends UserAdminOrganisationCache {

    public static synchronized iPartsUserAdminOrgCache getInstance(String orgId) {
        UserAdminOrganisationCache userAdminOrgCache = UserAdminOrganisationCache.getInstance(orgId);
        if (userAdminOrgCache instanceof iPartsUserAdminOrgCache) {
            return (iPartsUserAdminOrgCache)userAdminOrgCache;
        } else { // Kann eigentlich nicht passieren
            return new iPartsUserAdminOrgCache(orgId);
        }
    }

    /**
     * Liefert den Organisations-Cache für die übergebene virtuelle Benutzergruppe zurück
     *
     * @param virtualUserGroupId
     * @return
     */
    public static iPartsUserAdminOrgCache getOrgByVirtualUserGroupId(String virtualUserGroupId) {
        String orgId = iPartsVirtualUserGroup.getOrgIdFromVirtualUserGroupId(virtualUserGroupId);
        if (StrUtils.isValid(orgId)) {
            return getInstance(orgId);
        } else {
            return null;
        }
    }

    /**
     * Liefert den (ersten gefundenen) Organisations-Cache für die übergebene BST Supplier-ID (Lieferant) zurück.
     *
     * @param bstSupplierId
     * @return {@code null} falls keine Organisation für die übergebene BST Supplier-ID gefunden werden konnte
     */
    public static iPartsUserAdminOrgCache getCacheByBSTSupplierId(String bstSupplierId) {
        return getCacheByBSTSupplierId(bstSupplierId, true);
    }

    /**
     * Liefert den (ersten gefundenen) Organisations-Cache für die übergebene BST Supplier-ID (Lieferant) zurück.
     *
     * @param bstSupplierId
     * @param silent:       true: im Fehlerfall KEIN MessageDialog
     * @return {@code null} falls keine Organisation für die übergebene BST Supplier-ID gefunden werden konnte
     */
    public static iPartsUserAdminOrgCache getCacheByBSTSupplierId(String bstSupplierId, boolean silent) {
        if (StrUtils.isEmpty(bstSupplierId)) {
            return null;
        }

        List<iPartsUserAdminOrgCache> resultOrgCaches = new DwList<>();
        for (UserAdminOrganisationCache orgCache : getAllOrgCaches()) {
            if (orgCache instanceof iPartsUserAdminOrgCache) {
                iPartsUserAdminOrgCache iPartsOrgCache = (iPartsUserAdminOrgCache)orgCache;
                if (iPartsOrgCache.getBSTSupplierId().equals(bstSupplierId)) {
                    resultOrgCaches.add(iPartsOrgCache);
                }
            }
        }

        if (resultOrgCaches.isEmpty()) {
            return null;
        }

        if (resultOrgCaches.size() > 1) { // Organisation ist nicht eindeutig
            Set<String> organisationNames = new TreeSet<>();
            String dbLanguage = iPartsUserAdminDb.get().getConfig().getCurrentDatabaseLanguage();
            for (iPartsUserAdminOrgCache orgCache : resultOrgCaches) {
                organisationNames.add(TranslationHandler.translate(orgCache.getOrgName(dbLanguage)));
            }
            String organisationNamesString = StrUtils.stringListToString(organisationNames, ", ");
            Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.ERROR, "There is more than one organisation for the BST supplier ID \""
                                                                           + bstSupplierId + "\": " + organisationNamesString);

            if (!silent) {
                MessageDialog.showWarning(TranslationHandler.translate("!!Es wurde mehr als eine Organisation für die BST Supplier-ID \"%1\" gefunden: %2",
                                                                       bstSupplierId, organisationNamesString), "!!Warnung");
            }
        }

        // Ersten (im Idealfall einzigen) Organisations-Cache zurückliefern
        return resultOrgCaches.get(0);
    }


    protected iPartsUserAdminOrgCache(String orgId) {
        super(orgId);
    }

    /**
     * Liefert die BST Supplier-ID (Lieferant) für diese Organisation zurück.
     *
     * @return
     */
    public String getBSTSupplierId() {
        return (String)getOrgPropertyValue(iPartsUserAdminDb.PROPERTY_ID_SUPPLIER_ID);
    }

    /**
     * Handelt es sich um eine interne Organisation?
     *
     * @return
     */
    public boolean isInternalOrganisation() {
        return Utils.objectEquals(getOrgPropertyValue(iPartsUserAdminDb.PROPERTY_ID_INTERNAL_ORGANISATION), Boolean.TRUE);
    }

    /**
     * Hat diese Organisation virtuelle Benutzergruppen?
     *
     * @return
     */
    public boolean hasVirtualUserGroups() {
        return Utils.objectEquals(getOrgPropertyValue(iPartsUserAdminDb.PROPERTY_ID_VIRTUAL_USER_GROUP), Boolean.TRUE);
    }

    /**
     * Liefert die virtuelle Benutzergruppe für Autoren (egal ob intern, extern und/oder Extended) für diese Organisation zurück.
     * Es wird hier nicht geprüft, ob für diese Organisation virtuelle Benutzergruppen überhaupt zulässig sind.
     *
     * @return
     */
    public String getVirtualUserGroupIdForAuthors() {
        return iPartsVirtualUserGroup.getVirtualUserGroupId(orgId, iPartsUserAdminDb.VIRTUAL_ROLE_ID_AUTHOR_ALL);
    }

    /**
     * Liefert die virtuelle Benutzergruppe für Qualitätsprüfer zurück.
     * Es wird hier nicht geprüft, ob für diese Organisation virtuelle Benutzergruppen überhaupt zulässig sind.
     *
     * @return
     */
    public String getVirtualUserGroupIdForQualityInspectors() {
        return iPartsVirtualUserGroup.getVirtualUserGroupId(orgId, iPartsUserAdminDb.VIRTUAL_ROLE_ID_QUALITY_INSPECTOR_ALL);
    }

    /**
     * Liefert die Benennung für die virtuelle Benutzergruppe zur übergebenen Rolle zurück.
     * Es wird hier nicht geprüft, ob für diese Organisation virtuelle Benutzergruppen überhaupt zulässig sind.
     *
     * @param roleId
     * @return
     */
    public String getVirtualUserGroupName(String roleId) {
        String virtualUserGroupName = "[" + getOrgName(iPartsUserAdminDb.get().getConfig().getCurrentDatabaseLanguage())
                                      + " - ";
        switch (roleId) {
            case iPartsUserAdminDb.VIRTUAL_ROLE_ID_AUTHOR_ALL:
                virtualUserGroupName += TranslationHandler.translate("!!Autoren");
                break;
            case iPartsUserAdminDb.VIRTUAL_ROLE_ID_QUALITY_INSPECTOR_ALL:
                virtualUserGroupName += TranslationHandler.translate("!!Qualitätsprüfer");
                break;
            default:
                virtualUserGroupName += TranslationHandler.translate(roleId);
                break;
        }

        virtualUserGroupName += "]";
        return virtualUserGroupName;
    }
}