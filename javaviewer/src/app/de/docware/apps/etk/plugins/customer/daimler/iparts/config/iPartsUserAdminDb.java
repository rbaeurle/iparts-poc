/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.useradmin.EtkUserAdminDbActions;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ldap.LdapUser;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.combimodules.useradmin.db.*;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLStatement;
import de.docware.util.sql.dbobjects.DbInternalDbObject;
import de.docware.util.sql.pool.ConnectionPool;

import java.sql.SQLException;
import java.util.List;

/**
 * Hilfsklasse inkl. Konstanten für die Benutzerverwaltung in iParts.
 */
public class iPartsUserAdminDb extends EtkUserAdminDbActions {

    public static final String VIRTUAL_ROLE_ID_AUTHOR_ALL = "[IPARTS.Authors]"; // Virtuelle Rollen-ID für alle Autoren-Rollen
    public static final String VIRTUAL_ROLE_ID_QUALITY_INSPECTOR_ALL = "[IPARTS.QualityInspectors]"; // Virtuelle Rollen-ID für alle Qualitätsprüfer-Rollen

    public static final String APP_ID = iPartsPlugin.INTERNAL_PLUGIN_NAME + "_app";
    public static final String APP_NAME = "!!iParts";

    public static final String USER_ID_GUEST = iPartsPlugin.INTERNAL_PLUGIN_NAME + "_guest";
    public static final String USER_NAME_GUEST = "Guest";

    public static final String ORGANISATION_ID_IPARTS_USERS = iPartsPlugin.INTERNAL_PLUGIN_NAME + "_user";
    public static final String ORGANISATION_ID_DAIMLER = "DAIMLER";

    public static final String ROLE_ID_READER = "IPARTS.Reader";
    public static final String ROLE_ID_AUTHOR_INT = "IPARTS.Author";
    public static final String ROLE_ID_AUTHOR_INT_EXTENDED = "IPARTS.AuthorExtended";
    public static final String ROLE_ID_AUTHOR_EXT = "IPARTS.AuthorExt";
    public static final String ROLE_ID_AUTHOR_EXT_EXTENDED = "IPARTS.AuthorExtExtended";
    public static final String ROLE_ID_ADMIN = "IPARTS.Admin";
    public static final String ROLE_ID_PRODUCT_ADMIN = "IPARTS.ProductAdmin";
    public static final String ROLE_ID_DATA_ADMIN = "IPARTS.DataAdmin";
    public static final String ROLE_ID_QUALITY_INSPECTOR = "IPARTS.QualityInspector";
    public static final String ROLE_ID_EXCEL_IMPORTER = "IPARTS.ExcelImporter";

    public static final String PROPERTY_ID_SUPPLIER_ID = "SupplierID";
    public static final String PROPERTY_ID_INTERNAL_ORGANISATION = "InternalOrganisation";
    public static final String PROPERTY_ID_VIRTUAL_USER_GROUP = "VirtualUserGroups";
    public static final String PROPERTY_ID_EDITABLE_PRODUCTS = "EditableProducts";
    public static final String PROPERTY_ID_PSK = "PSK";
    public static final String PROPERTY_ID_CAR_VAN = "CarAndVan";
    public static final String PROPERTY_ID_TRUCK_BUS = "TruckAndBus";
    public static final String PROPERTY_ID_LDAP_USER = "LDAPUser";
    public static final String PROPERTY_ID_COST_UNIT = "CostUnit"; // Kostenstelle
    public static final String PROPERTY_ID_CORPORATE_AFFILIATION = "CorpAffiliation"; // Unternehmenszugehörigkeit

    public static iPartsUserAdminDb createInstance(ConfigBase config, String dbAlias, EtkProject project) {
        UserAdminDbActions.LOG_CHANNEL = iPartsPlugin.LOG_CHANNEL_USER_ADMIN;

        // Wir wollen keine Meldungen über gelöschte Benutzer usw. sehen
        UserAdminDbActions.showCacheDataModifiedMessage = false;

        // Verhindern, dass die Tabellen der Benutzerverwaltung automatisch erzeugt bzw. aktualisiert werden
        UserAdminDb.CREATE_OR_UPDATE_TABLES_ALLOWED = false;

        activateMD5forUserAdmin(config);

        return new iPartsUserAdminDb(config, dbAlias, project);
    }

    public static void clearAllUserAdminCaches() {
        iPartsUserAdminCache.clearCache();
        iPartsUserAdminOrgCache.clearCache();
        UserAdminRoleCache.clearCache();
    }

    @Override
    protected void registerMyAppInUserAdminDB(ConnectionPool userAdminConnectionPool, SQLStatement userAdminStatement) throws SQLException {
        super.registerMyAppInUserAdminDB(userAdminConnectionPool, userAdminStatement);

        // Katalog Online auch als App hinzufügen
        AppDbObject.addApp(userAdminConnectionPool, userAdminStatement, true, EtkUserAdminDbActions.APP_ID, EtkUserAdminDbActions.APP_NAME);
    }

    @Override
    protected void executeInitUserAdminDbStatements(ConnectionPool userAdminConnectionPool, SQLStatement userAdminStatement) throws SQLException {
        registerMyAppInUserAdminDB(userAdminConnectionPool, userAdminStatement);

        ExtendedPropertyType stringPropertyType = new ExtendedPropertyType(PropertyType.STRING);
        ExtendedPropertyType booleanPropertyType = new ExtendedPropertyType(PropertyType.BOOLEAN);

        // Benutzer
        boolean guestUserAdded = UserDbObject.addUser(userAdminConnectionPool, userAdminStatement, true, USER_ID_GUEST,
                                                      USER_NAME_GUEST, USER_NAME_GUEST, true);
        if (guestUserAdded) {
            setEmailAddress(userAdminConnectionPool, userAdminStatement, USER_ID_GUEST, "support@iparts.de");
        }

        // Organisationen
        OrganisationDbObject.addOrganisation(userAdminConnectionPool, userAdminStatement, true, ORGANISATION_ID_IPARTS_USERS,
                                             "!!iParts-Benutzer", OrganisationDbObject.CONST_ORGANISATION_ID_ROOT_SIMPLE);
        OrganisationDbObject.addOrganisation(userAdminConnectionPool, userAdminStatement, true, ORGANISATION_ID_DAIMLER,
                                             "DAIMLER", ORGANISATION_ID_IPARTS_USERS);

        // Organisationen zu Anwendungen
        OrganisationAppsDbObject.addOrganisationApp(userAdminConnectionPool, userAdminStatement, true, OrganisationDbObject.CONST_ORGANISATION_ID_ROOT_SIMPLE, APP_ID);
        OrganisationAppsDbObject.addOrganisationApp(userAdminConnectionPool, userAdminStatement, true, ORGANISATION_ID_IPARTS_USERS, APP_ID);
        OrganisationAppsDbObject.addOrganisationApp(userAdminConnectionPool, userAdminStatement, true, ORGANISATION_ID_DAIMLER, APP_ID);

        // Anwendung "Katalog Online" auch zu den Organisationen hinzufügen
        OrganisationAppsDbObject.addOrganisationApp(userAdminConnectionPool, userAdminStatement, true, OrganisationDbObject.CONST_ORGANISATION_ID_ROOT_SIMPLE, EtkUserAdminDbActions.APP_ID);
        OrganisationAppsDbObject.addOrganisationApp(userAdminConnectionPool, userAdminStatement, true, ORGANISATION_ID_IPARTS_USERS, EtkUserAdminDbActions.APP_ID);
        OrganisationAppsDbObject.addOrganisationApp(userAdminConnectionPool, userAdminStatement, true, ORGANISATION_ID_DAIMLER, EtkUserAdminDbActions.APP_ID);

        // Benutzer zu Organisationen
        if (guestUserAdded) {
            UserOrganisationsDbObject.addUserOrganisation(userAdminConnectionPool, userAdminStatement, true,
                                                          USER_ID_GUEST, ORGANISATION_ID_DAIMLER);
        }

        // Rechte
        iPartsRight.init(userAdminConnectionPool, userAdminStatement);

        // Rollen
        // ROLE_ID_READER
        if (RoleDbObject.addRole(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_READER, ROLE_ID_READER,
                                 false, PropertyChangeLevel.GLOBAL)) {
            // Initialwerte für Organisation zur Rolle
            OrganisationRolesDbObject.addOrganisationRole(userAdminConnectionPool, userAdminStatement, true,
                                                          ORGANISATION_ID_IPARTS_USERS, ROLE_ID_READER);
            OrganisationRolesDbObject.addOrganisationRole(userAdminConnectionPool, userAdminStatement, true,
                                                          ORGANISATION_ID_DAIMLER, ROLE_ID_READER);

            // Initialwerte für Rechte zur Rolle
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_READER,
                                            iPartsRight.VIEW_PARTS_DATA.getAlias(), RightScope.NA);

            // Initialwerte für Benutzer zur Rolle
            UserRolesDbObject.addUserRole(userAdminConnectionPool, userAdminStatement, true, USER_ID_GUEST, ROLE_ID_READER);
        }

        // ROLE_ID_AUTHOR_INT
        createAuthorRole(ROLE_ID_AUTHOR_INT, false, userAdminConnectionPool, userAdminStatement);

        // ROLE_ID_AUTHOR_INT_EXTENDED
        createAuthorRole(ROLE_ID_AUTHOR_INT_EXTENDED, true, userAdminConnectionPool, userAdminStatement);

        // ROLE_ID_AUTHOR_EXT
        createAuthorRole(ROLE_ID_AUTHOR_EXT, false, userAdminConnectionPool, userAdminStatement);

        // ROLE_ID_AUTHOR_EXT_EXTENDED
        createAuthorRole(ROLE_ID_AUTHOR_EXT_EXTENDED, true, userAdminConnectionPool, userAdminStatement);

        // ROLE_ID_ADMIN
        if (RoleDbObject.addRole(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN, ROLE_ID_ADMIN,
                                 false, PropertyChangeLevel.GLOBAL)) {
            // Initialwerte für Organisation zur Rolle
            OrganisationRolesDbObject.addOrganisationRole(userAdminConnectionPool, userAdminStatement, true,
                                                          ORGANISATION_ID_DAIMLER, ROLE_ID_ADMIN);

            // Initialwerte für Rechte zur Rolle
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EDIT_MASTER_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.DELETE_MASTER_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EDIT_VEHICLE_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.ASSIGN_VEHICLE_AGGS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EDIT_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.VIEW_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.IMPORT_MASTER_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.CREATE_MAILBOX_MESSAGES.getAlias(), RightScope.GLOBAL);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.VIEW_LOG_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EXPORT_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EDIT_TEXT_PRODUCT_ADMIN.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EDIT_TEXT_DATA_ADMIN.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.ASSIGN_USER_OR_GROUP.getAlias(), RightScope.GLOBAL);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.FORCE_ASSIGN_USER_OR_GROUP.getAlias(), RightScope.GLOBAL);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.VIEW_AUTHOR_ORDERS.getAlias(), RightScope.GLOBAL);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.REQUEST_TRANSLATIONS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.IGNORE_ERRORS_IN_AO_RELEASE_CHECKS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.SUPPLY_AUTHOR_ORDER_TO_BST.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.ADD_SERIES_TO_AUTO_CALC_AND_EXPORT.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.COPY_TUS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.DELETE_EMPTY_TUS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EXPORT_IMPORT_AUTHOR_ORDER.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.VIEW_CONSTRUCTION_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EDIT_INTERNAL_TEXT_CONSTRUCTION.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.VIEW_EDS_WORK_BASKET.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.DELETE_RETAIL_CACHES.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.RETRIEVE_PICTURES.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.RETRIEVE_PICTURES_FROM_PIC_ORDER.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.CREATE_DELETE_PEM_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.AUTO_MODEL_VALIDITY_EXTENSION.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.VIEW_DATABASE_TOOLS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.AUTO_TRANSFER_PART_LIST_ENTRIES.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.AUTO_TRANSFER_PART_LIST_ENTRIES_EXTENDED.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.LOCK_PART_LIST_ENTRIES_FOR_EDIT.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EDIT_DELETE_INTERNAL_TEXT.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.CREATE_FOOTNOTE.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EXECUTE_EXCEL_IMPORT.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.EDIT_OMIT_FOR_SPECIAL_PARTS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.CREATE_DELETE_CAR_PERSPECTIVE.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.REPORT_EDIT_OF_AUTO_TRANSFER_ENTRIES.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.REPORT_TU_VALIDATION_FOR_PRODUKT.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.SIMPLIFIED_QUALITY_CHECKS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_ADMIN,
                                            iPartsRight.PURGE_DATABASE.getAlias(), RightScope.NA);
        }

        // ROLE_ID_PRODUCT_ADMIN
        if (RoleDbObject.addRole(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN, ROLE_ID_PRODUCT_ADMIN,
                                 false, PropertyChangeLevel.GLOBAL)) {
            // Initialwerte für Organisation zur Rolle
            OrganisationRolesDbObject.addOrganisationRole(userAdminConnectionPool, userAdminStatement, true,
                                                          ORGANISATION_ID_DAIMLER, ROLE_ID_PRODUCT_ADMIN);

            // Initialwerte für Rechte zur Rolle
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.EDIT_MASTER_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.VIEW_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.CREATE_MAILBOX_MESSAGES.getAlias(), RightScope.CURRENT_ORGANISATION);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.EDIT_TEXT_PRODUCT_ADMIN.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.VIEW_CONSTRUCTION_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.EDIT_INTERNAL_TEXT_CONSTRUCTION.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.ASSIGN_VEHICLE_AGGS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.AUTO_MODEL_VALIDITY_EXTENSION.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.LOCK_PART_LIST_ENTRIES_FOR_EDIT.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.EDIT_DELETE_INTERNAL_TEXT.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.EDIT_OMIT_FOR_SPECIAL_PARTS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.CREATE_DELETE_CAR_PERSPECTIVE.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.REPORT_EDIT_OF_AUTO_TRANSFER_ENTRIES.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.REPORT_TU_VALIDATION_FOR_PRODUKT.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.SIMPLIFIED_QUALITY_CHECKS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_PRODUCT_ADMIN,
                                            iPartsRight.AUTO_TRANSFER_PLES_EXTENDED_WHOLE_PRODUCT.getAlias(), RightScope.NA);
        }

        // ROLE_ID_DATA_ADMIN
        if (RoleDbObject.addRole(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN, ROLE_ID_DATA_ADMIN,
                                 false, PropertyChangeLevel.GLOBAL)) {
            // Initialwerte für Organisation zur Rolle
            OrganisationRolesDbObject.addOrganisationRole(userAdminConnectionPool, userAdminStatement, true,
                                                          ORGANISATION_ID_DAIMLER, ROLE_ID_DATA_ADMIN);

            // Initialwerte für Rechte zur Rolle
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.EDIT_MASTER_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.DELETE_MASTER_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.VIEW_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.IMPORT_MASTER_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.CREATE_MAILBOX_MESSAGES.getAlias(), RightScope.CURRENT_ORGANISATION);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.VIEW_LOG_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.EXPORT_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.EDIT_TEXT_PRODUCT_ADMIN.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.EDIT_TEXT_DATA_ADMIN.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.REQUEST_TRANSLATIONS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.DELETE_EMPTY_TUS.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.DELETE_RETAIL_CACHES.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_DATA_ADMIN,
                                            iPartsRight.EDIT_DELETE_INTERNAL_TEXT.getAlias(), RightScope.NA);
        }

        // ROLE_ID_QUALITY_INSPECTOR
        if (RoleDbObject.addRole(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_QUALITY_INSPECTOR, ROLE_ID_QUALITY_INSPECTOR,
                                 false, PropertyChangeLevel.GLOBAL)) {
            // Initialwerte für Organisation zur Rolle
            OrganisationRolesDbObject.addOrganisationRole(userAdminConnectionPool, userAdminStatement, true,
                                                          ORGANISATION_ID_DAIMLER, ROLE_ID_QUALITY_INSPECTOR);

            // Initialwerte für Rechte zur Rolle
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_QUALITY_INSPECTOR,
                                            iPartsRight.EDIT_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_QUALITY_INSPECTOR,
                                            iPartsRight.VIEW_PARTS_DATA.getAlias(), RightScope.NA);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_QUALITY_INSPECTOR,
                                            iPartsRight.CREATE_MAILBOX_MESSAGES.getAlias(), RightScope.CURRENT_ORGANISATION);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_QUALITY_INSPECTOR,
                                            iPartsRight.ASSIGN_USER_OR_GROUP.getAlias(), RightScope.CURRENT_ORGANISATION);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_QUALITY_INSPECTOR,
                                            iPartsRight.VIEW_AUTHOR_ORDERS.getAlias(), RightScope.CURRENT_ORGANISATION);
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_QUALITY_INSPECTOR,
                                            iPartsRight.VIEW_EDS_WORK_BASKET.getAlias(), RightScope.NA);
        }

        // ROLE_ID_EXCEL_IMPORTER
        if (RoleDbObject.addRole(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_EXCEL_IMPORTER, ROLE_ID_EXCEL_IMPORTER,
                                 false, PropertyChangeLevel.GLOBAL)) {
            // Initialwerte für Organisation zur Rolle
            OrganisationRolesDbObject.addOrganisationRole(userAdminConnectionPool, userAdminStatement, true,
                                                          ORGANISATION_ID_DAIMLER, ROLE_ID_EXCEL_IMPORTER);

            // Initialwerte für Rechte zur Rolle
            RoleRightsDbObject.addRoleRight(userAdminConnectionPool, userAdminStatement, true, ROLE_ID_EXCEL_IMPORTER,
                                            iPartsRight.EXECUTE_EXCEL_IMPORT.getAlias(), RightScope.NA);

        }

        // Organisations-Eigenschaften
        if (UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                           PROPERTY_ID_SUPPLIER_ID, "!!* BST Supplier-ID für die Organisation",
                                                           stringPropertyType,
                                                           PropertyChangeLevel.ORG, false, "")) {
            // Initialwerte für Organisation zur Organisations-Eigenschaft PROPERTY_ID_SUPPLIER_ID
            OrganisationPropertiesDbObject.addOrganisationProperty(userAdminConnectionPool, userAdminStatement,
                                                                   true, ORGANISATION_ID_DAIMLER, APP_ID, PROPERTY_ID_SUPPLIER_ID,
                                                                   stringPropertyType, PropertyInheritance.FINAL, "");
        }
        if (UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                           PROPERTY_ID_INTERNAL_ORGANISATION, "!!* Interne Organisation",
                                                           booleanPropertyType,
                                                           PropertyChangeLevel.ORG, false, Boolean.FALSE)) {
            // Initialwerte für Organisation zur Organisations-Eigenschaft PROPERTY_ID_INTERNAL_ORGANISATION
            OrganisationPropertiesDbObject.addOrganisationProperty(userAdminConnectionPool, userAdminStatement,
                                                                   true, ORGANISATION_ID_DAIMLER, APP_ID, PROPERTY_ID_INTERNAL_ORGANISATION,
                                                                   booleanPropertyType, PropertyInheritance.FINAL, Boolean.TRUE);
        }
        if (UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                           PROPERTY_ID_VIRTUAL_USER_GROUP, "!!* Virtuelle Benutzergruppen",
                                                           booleanPropertyType,
                                                           PropertyChangeLevel.ORG, false, Boolean.FALSE)) {
            // Initialwerte für Organisation zur Organisations-Eigenschaft PROPERTY_ID_VIRTUAL_USER_GROUP
            OrganisationPropertiesDbObject.addOrganisationProperty(userAdminConnectionPool, userAdminStatement,
                                                                   true, ORGANISATION_ID_DAIMLER, APP_ID, PROPERTY_ID_VIRTUAL_USER_GROUP,
                                                                   booleanPropertyType, PropertyInheritance.FINAL, Boolean.TRUE);
        }

        // Benutzer-Eigenschaften
        if (UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                           PROPERTY_ID_EDITABLE_PRODUCTS, "!!Editierbare Produkte (mit Wildcards getrennt durch |)",
                                                           stringPropertyType,
                                                           PropertyChangeLevel.ORG, true, "")) {
            // Initialwerte für Organisation zur Benutzer-Eigenschaft PROPERTY_ID_EDITABLE_PRODUCTS
            OrganisationPropertiesDbObject.addOrganisationProperty(userAdminConnectionPool, userAdminStatement,
                                                                   true, ORGANISATION_ID_DAIMLER, APP_ID, PROPERTY_ID_EDITABLE_PRODUCTS,
                                                                   stringPropertyType, PropertyInheritance.INHERITABLE, "*");
        }

        if (UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                           PROPERTY_ID_PSK, "!!PSK-Produkte ansehen und editieren",
                                                           booleanPropertyType,
                                                           PropertyChangeLevel.ORG, true, Boolean.FALSE)) {
            // Initialwerte für Organisation zur Benutzer-Eigenschaft PROPERTY_ID_PSK
            OrganisationPropertiesDbObject.addOrganisationProperty(userAdminConnectionPool, userAdminStatement,
                                                                   true, ORGANISATION_ID_DAIMLER, APP_ID, PROPERTY_ID_PSK,
                                                                   booleanPropertyType, PropertyInheritance.INHERITABLE,
                                                                   Boolean.FALSE);
        }

        if (UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                           PROPERTY_ID_CAR_VAN, "!!Mercedes-Benz AG (PKW/Van-Daten ansehen und editieren)",
                                                           booleanPropertyType,
                                                           PropertyChangeLevel.GLOBAL, true, Boolean.FALSE)) {
            // Initialwerte für Organisation zur Benutzer-Eigenschaft PROPERTY_ID_CAR_VAN
            OrganisationPropertiesDbObject.addOrganisationProperty(userAdminConnectionPool, userAdminStatement,
                                                                   true, ORGANISATION_ID_DAIMLER, APP_ID, PROPERTY_ID_CAR_VAN,
                                                                   booleanPropertyType, PropertyInheritance.INHERITABLE,
                                                                   Boolean.TRUE);
        }

        if (UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                           PROPERTY_ID_TRUCK_BUS, "!!Daimler Truck AG (Truck/Bus-Daten ansehen und editieren)",
                                                           booleanPropertyType,
                                                           PropertyChangeLevel.GLOBAL, true, Boolean.FALSE)) {
            // Initialwerte für Organisation zur Benutzer-Eigenschaft PROPERTY_ID_TRUCK_BUS
            OrganisationPropertiesDbObject.addOrganisationProperty(userAdminConnectionPool, userAdminStatement,
                                                                   true, ORGANISATION_ID_DAIMLER, APP_ID, PROPERTY_ID_TRUCK_BUS,
                                                                   booleanPropertyType, PropertyInheritance.INHERITABLE,
                                                                   Boolean.TRUE);
        }

        // LDAP-Benutzer Eigenschaft
        UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                       PROPERTY_ID_LDAP_USER, "!!LDAP-Benutzer",
                                                       booleanPropertyType,
                                                       PropertyChangeLevel.GLOBAL, true, Boolean.FALSE);
        // Unternehmenszugehörigkeit Eigenschaft
        UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                       PROPERTY_ID_CORPORATE_AFFILIATION, "!!Unternehmenszugehörigkeit",
                                                       stringPropertyType,
                                                       PropertyChangeLevel.GLOBAL, true, "");
        // Kostenstelle Eigenschaft
        UserPropertiesTemplateDbObject.addUserProperty(userAdminConnectionPool, userAdminStatement, APP_ID,
                                                       PROPERTY_ID_COST_UNIT, "!!Kostenstelle",
                                                       stringPropertyType,
                                                       PropertyChangeLevel.GLOBAL, true, "");
    }

    private static void createAuthorRole(String roleId, boolean isExtendedAuthorRole, ConnectionPool connectionPool, SQLStatement statement) throws SQLException {
        if (RoleDbObject.addRole(connectionPool, statement, true, roleId, roleId, false, PropertyChangeLevel.GLOBAL)) {
            // Initialwerte für Organisation zur Rolle
            OrganisationRolesDbObject.addOrganisationRole(connectionPool, statement, true, ORGANISATION_ID_DAIMLER, roleId);

            // Initialwerte für Rechte zur Rolle
            RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.EDIT_VEHICLE_PARTS_DATA.getAlias(),
                                            RightScope.NA);
            RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.EDIT_PARTS_DATA.getAlias(),
                                            RightScope.NA);
            RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.VIEW_PARTS_DATA.getAlias(),
                                            RightScope.NA);
            RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.CREATE_MAILBOX_MESSAGES.getAlias(),
                                            RightScope.CURRENT_ORGANISATION);
            RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.ASSIGN_USER_OR_GROUP.getAlias(),
                                            RightScope.CURRENT_ORGANISATION);
            RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.VIEW_AUTHOR_ORDERS.getAlias(),
                                            RightScope.CURRENT_ORGANISATION);
            RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.EDIT_INTERNAL_TEXT_CONSTRUCTION.getAlias(),
                                            RightScope.NA);
            RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.VIEW_EDS_WORK_BASKET.getAlias(),
                                            RightScope.NA);


            if (isExtendedAuthorRole) {
                RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.EDIT_TEXT_PRODUCT_ADMIN.getAlias(),
                                                RightScope.NA);
                RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.IGNORE_ERRORS_IN_AO_RELEASE_CHECKS.getAlias(),
                                                RightScope.NA);
                RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.COPY_TUS.getAlias(),
                                                RightScope.NA);
                RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.ADD_SERIES_TO_AUTO_CALC_AND_EXPORT.getAlias(),
                                                RightScope.NA);
                RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.CREATE_DELETE_PEM_DATA.getAlias(),
                                                RightScope.NA);
                RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.AUTO_MODEL_VALIDITY_EXTENSION.getAlias(),
                                                RightScope.NA);

                if (roleId.equals(ROLE_ID_AUTHOR_INT_EXTENDED)) {
                    RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.AUTO_TRANSFER_PART_LIST_ENTRIES.getAlias(),
                                                    RightScope.NA);
                    RoleRightsDbObject.addRoleRight(connectionPool, statement, true, roleId, iPartsRight.EDIT_DELETE_INTERNAL_TEXT.getAlias(),
                                                    RightScope.NA);
                }
            }
        }
    }

    /**
     * Login in der Benutzerverwaltung mit dem angegebenen (LDAP-)Benutzernamen.
     *
     * @param userName
     * @return {@link UserDbObject} aus der Benutzerverwaltung für den eingeloggten Benutzer
     */
    public static UserDbObject login(String userName) {
        Session session = Session.get();
        if (session == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.ERROR, "Error during login for user name \"" + userName
                                                                           + "\" because session is null");
            return null;
        }

        session.removeAttribute(SESSION_KEY_LOGIN_USER); // Benutzer aus der Session entfernen

        boolean adminMode = session.getStartParameter().getParameterBoolean(Constants.FRAMEWORK_PARAMETER_ADMIN, false);
        ConnectionPool userAdminConnectionPool = null;

        // Nur bei GUI-Sessions einloggen
        try {
            userAdminConnectionPool = get().getConnectionPool(false);
            if (session.canHandleGui()) {
                UserDbObject userDbObject = UserDbObject.getUserDbByName(userAdminConnectionPool, null, userName);
                if (userDbObject != null) {
                    String userId = loginForSession(userAdminConnectionPool, userName, userName, true, APP_ID,
                                                    getOrganisationIdForUserId(userDbObject.getUserId()));
                    if (userId != null) {
                        OrganisationDbObject organisationDbObject = showOrganisationSelectionWindow(userAdminConnectionPool,
                                                                                                    userId, APP_ID, true);
                        if (organisationDbObject != null) {
                            String loginMessage = "Login successful for user name \"" + userName + "\" in organisation \""
                                                  + TranslationHandler.translateForLanguage(organisationDbObject.getOrganisationName(),
                                                                                            Language.EN.getCode()) + "\"";
                            return loginUserAndCheckViewRight(userId, userDbObject, LogType.DEBUG, loginMessage);
                        } else {
                            Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.DEBUG, "No organisation found for user name \""
                                                                                           + userName + "\"");
                            return null;
                        }
                    }
                }
            }

            // Admin-Modus
            if (adminMode) {
                // Wenn der Login für den Admin-Modus benötigt ist, jetzt den Login-Dialog zeigen
                if (iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_LOGIN_NEEDED_FOR_ADMIN_MODE)) {
                    String userId = get().getLoginPanel(userAdminConnectionPool, APP_ID, false).showLoginPopup();
                    if (userId != null) {
                        UserDbObject userDbObject = UserDbObject.getUserDb(userAdminConnectionPool, null, userId);
                        if (userDbObject != null) {
                            OrganisationDbObject organisationDbObject = showOrganisationSelectionWindow(userAdminConnectionPool,
                                                                                                        userId, AppDbObject.CONST_APP_ID_USER_ADMIN, true);
                            if (organisationDbObject != null) {
                                Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.DEBUG, "Login successful for admin mode with user name \""
                                                                                               + userName + "\" in organisation \""
                                                                                               + TranslationHandler.translateForLanguage(organisationDbObject.getOrganisationName(),
                                                                                                                                         Language.EN.getCode()) + "\"");
                                iPartsUserAdminCache.removeUserFromCache(userId);
                                return userDbObject;
                            } else {
                                Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.DEBUG, "No organisation found for admin mode with user name \""
                                                                                               + userName + "\"");
                                return null;
                            }
                        }
                    }

                    // Wurde der Login abgebrochen, eine entsprechende Meldung anzeigen, da ein weiterer Fallback nicht erlaubt ist
                    Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.DEBUG, "Login cancelled for admin mode with user name \""
                                                                                   + userName + "\"");
                    MessageDialog.show("!!Login abgebrochen für den Zugang zum Administrator-Modus", "!!Login");
                    return null;
                } else {
                    // Fallback auf Superuser, wenn kein Login für den Admin-Modus benötigt wird
                    UserDbObject userDbObject = UserDbObject.getUserDb(userAdminConnectionPool, null, UserDbObject.CONST_USER_ID_SUPERUSER);
                    if (userDbObject != null) {
                        String userId = loginForSession(userAdminConnectionPool, userDbObject.getUserName(), userDbObject.getPassword(),
                                                        false, APP_ID, getOrganisationIdForUserId(UserDbObject.CONST_USER_ID_SUPERUSER));
                        if (userId != null) {
                            Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.INFO, "Fallback login successful for admin mode as superuser for user name \""
                                                                                          + userName + "\"");
                            iPartsUserAdminCache.removeUserFromCache(userId);
                            return userDbObject;
                        }
                    }
                }
            }

            // Letzter Fallback auf Gast-Benutzer (falls erlaubt)
            if (iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_GUEST_LOGIN)) {
                UserDbObject userDbObject = UserDbObject.getUserDb(userAdminConnectionPool, null, USER_ID_GUEST);
                if (userDbObject != null) {
                    String userId = loginForSession(userAdminConnectionPool, userDbObject.getUserName(), userDbObject.getPassword(),
                                                    false, APP_ID, getOrganisationIdForUserId(USER_ID_GUEST));
                    if (userId != null) {
                        LdapUser ldapUser = iPartsPlugin.getLdapUserForSession();
                        if (ldapUser != null) {
                            if (StrUtils.isValid(ldapUser.getUid())) { // Kann eigentlich durch den LDAP-Sync nicht passieren
                                ldapUser.setStatusText("!!LDAP User-Id nicht gefunden (Fallback auf Gast für Benutzer \"%1\")",
                                                       ldapUser.getUid());
                            } else {
                                ldapUser.setStatusText("!!Keine gültige LDAP User-Id (Fallback auf Gast für Benutzer \"%1\")",
                                                       FrameworkUtils.getUserName());
                            }
                        }

                        OrganisationDbObject organisationDbObject = showOrganisationSelectionWindow(userAdminConnectionPool,
                                                                                                    userId, APP_ID, true);
                        if (organisationDbObject != null) {
                            String loginMessage = "Fallback login successful as guest for user name \"" + userName + "\" in organisation \""
                                                  + TranslationHandler.translateForLanguage(organisationDbObject.getOrganisationName(),
                                                                                            Language.EN.getCode()) + "\"";
                            return loginUserAndCheckViewRight(userId, userDbObject, LogType.DEBUG, loginMessage);
                        } else {
                            Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.DEBUG, "No organisation found for fallback user \""
                                                                                           + USER_ID_GUEST + "\"");
                            return null;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.ERROR, e);

            // Bei vorhandenem ConnectionPool überprüfen, ob die interne Verwaltungstabelle für DB-Schemas existiert
            // -> falls nicht: createDb() aufrufen, um die fehlenden Tabellen zu loggen
            if (userAdminConnectionPool != null) {
                DbInternalDbObject internalDbObject = new DbInternalDbObject();
                try {
                    if (!internalDbObject.tableExists(userAdminConnectionPool)) {
                        ConnectionPool tempUserAdminConnectionPool = userAdminConnectionPool;
                        userAdminConnectionPool = null;
                        iPartsUserAdminDb.get().getSchema().createDb(tempUserAdminConnectionPool);
                    }
                } catch (SQLException e2) {
                    // nichts machen, da lediglich die fehlenden Tabellen geloggt werden sollen
                    userAdminConnectionPool = null;
                }
            }

            // Bei fehlgeschlagener DB-Verbindung und Aufruf vom Admin-Modus als Superuser einloggen, da man ansonsten nicht
            // mal mehr in den Admin-Modus kommt
            if (adminMode && (userAdminConnectionPool == null)) {
                MessageDialog.showError(TranslationHandler.translate("!!Benutzerverwaltung: %1", e.getMessage()));

                // Passwort ist hier irrelevant und wird nicht benötigt
                // iPartsUserAdminCache löschen ist hier irrelevant und würde auch keinen Sinn machen
                return new UserDbObject(UserDbObject.CONST_USER_ID_SUPERUSER, UserDbObject.CONST_USER_ID_SUPERUSER, "", true);
            }
        }

        Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.ERROR, "Login failed for user name \"" + userName + "\"");
        if (session.canHandleGui()) {
            MessageDialog.showError(TranslationHandler.translate("!!Login fehlgeschlagen für den Benutzer \"%1\".", userName),
                                    "!!Login");
        }
        return null;
    }

    private static UserDbObject loginUserAndCheckViewRight(String userId, UserDbObject userDbObject, LogType logType, String loginMessage) {
        iPartsUserAdminCache.removeUserFromCache(userId);
        if (iPartsRight.VIEW_PARTS_DATA.checkRightInSession()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, logType, loginMessage);
            return userDbObject;
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, logType, loginMessage + " but missing right \"" + iPartsRight.VIEW_PARTS_DATA.getAlias() + "\"");
            MessageDialog.showWarning(TranslationHandler.translate("!!Der Benutzer \"%1\" hat kein Recht für \"%2\".", userDbObject.getUserName(),
                                                                   TranslationHandler.translate(iPartsRight.VIEW_PARTS_DATA.getCaption())));
            return null;
        }
    }

    /**
     * Liefert die (erste) Organisation für den übergebenen Benutzer zurück mit Fallback auf {@link #ORGANISATION_ID_IPARTS_USERS}.
     *
     * @param userId
     * @return
     */
    public static String getOrganisationIdForUserId(String userId) {
        String organisationId = getSomeOrgIdForUserAndApp(userId, APP_ID);
        if (organisationId != null) {
            return organisationId;
        } else if (userId.equals(UserDbObject.CONST_USER_ID_SUPERUSER)) {
            return OrganisationDbObject.CONST_ORGANISATION_ID_ADMINS_SIMPLE;
        } else {
            return ORGANISATION_ID_IPARTS_USERS;
        }
    }

    /**
     * Liefert alle Benutzer, die via LDAP angelegt wurden (Eigenschaft {@link #PROPERTY_ID_LDAP_USER} in UA_USER_PROPERTIES).
     *
     * @return
     */
    public static List<UserDbObject> getAllLdapCreatedUsers() {
        List<UserDbObject> users = new DwList<>();
        try {
            ConnectionPool userAdminConnectionPool = get().getConnectionPool(false);
            List<UserPropertiesDbObject> foundUsers = getAllLdapUserProperties(userAdminConnectionPool);
            for (UserPropertiesDbObject userPropertiesDbObject : foundUsers) {
                UserDbObject userFromDB = UserDbObject.getUserDb(userAdminConnectionPool, null, userPropertiesDbObject.getUserId());
                if (userFromDB == null) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.ERROR, "Error while retrieving all " +
                                                                                   "LDAP users via their properties: " +
                                                                                   "Found properties for not existing user \""
                                                                                   + userPropertiesDbObject.getUserId() + "\"");
                    continue;
                }
                users.add(userFromDB);
            }
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_USER_ADMIN, LogType.ERROR, e);
        }
        return users;
    }

    /**
     * Liefert alle {@link UserPropertiesDbObject}s mit gesetztem LDAP-Benutzer-Flag (Eigenschaft {@link #PROPERTY_ID_LDAP_USER}
     * in UA_USER_PROPERTIES).
     *
     * @param connectionPool
     * @return
     * @throws SQLException
     */
    private static List<UserPropertiesDbObject> getAllLdapUserProperties(ConnectionPool connectionPool) throws SQLException {
        UserPropertiesDbObject userPropertyDb = new UserPropertiesDbObject(null, null, null, PROPERTY_ID_LDAP_USER,
                                                                           new ExtendedPropertyType(PropertyType.BOOLEAN),
                                                                           Boolean.TRUE, null);
        return (List<UserPropertiesDbObject>)userPropertyDb.loadObjects(connectionPool, null, false, UserPropertiesDbObject.UP_KEY);
    }

    /**
     * Liefert den Benutzernamen aus der aktuellen Session bzw. im Fallback des übergebenen {@link EtkProject}s zurück für
     * Logausgaben.
     *
     * @param project
     * @return
     */
    public static String getUserNameForLogging(EtkProject project) {
        if (iPartsUserAdminDb.getLoginUserIdForSession() != null) {
            if (iPartsUserAdminDb.isActive()) {
                return "user " + getLoginUserName();
            } else {
                return "unknown user";
            }
        } else {
            if (project != null) {
                if (project == iPartsPlugin.getMqProject()) {
                    return "system or automatic MQ import";
                } else if (iPartsPlugin.isImportPluginActive() && (project == de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.getRFTSxProject())) {
                    return "automatic RFTS/x import";
                } else {
                    return "system";
                }
            } else {
                return "system";
            }
        }
    }


    protected iPartsUserAdminDb(ConfigBase config, String dbAlias, EtkProject project) {
        // DB Alias für die Anwendung auch in der Benutzerverwaltung verwenden
        super(config, dbAlias, project);
    }

    @Override
    public String getAppId() {
        return APP_ID;
    }

    @Override
    public String getAppName() {
        return APP_NAME;
    }

    @Override
    protected iPartsUserAdminCacheElement createUserAdminUserCache(String userId) {
        return new iPartsUserAdminCacheElement(userId);
    }

    @Override
    protected iPartsUserAdminOrgCache createUserAdminOrganisationCache(String orgId) {
        return new iPartsUserAdminOrgCache(orgId);
    }

    @Override
    public boolean showToolbarLoginButton() {
        return false;
    }

    @Override
    public void loginAsSuperUser() {
        // nichts tun da IParts selbst Logik hat wie sie mit dem automatischen Login als superuser umgehen.
    }
}
