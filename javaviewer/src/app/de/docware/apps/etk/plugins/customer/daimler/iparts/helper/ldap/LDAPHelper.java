/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ldap;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDateTimeHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsTimeInterval;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.combimodules.useradmin.db.*;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.framework.modules.gui.misc.startparameter.UrlParameter;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.misc.ldap.DN;
import de.docware.util.misc.ldap.LdapException;
import de.docware.util.misc.ldap.LdapJndi;
import de.docware.util.security.PasswordString;
import de.docware.util.sql.SQLStatement;
import de.docware.util.sql.pool.ConnectionPool;
import de.docware.util.sql.pool.ConnectionPoolException;

import javax.naming.NamingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helfer zum Herstellen einer LDAP Verbindung und Abfragen von Rollen/Rechten
 */
public class LDAPHelper {

    public static final int INVALID_PORT = -1;
    public static final String SITEMINDER_USER_ID_KEY = "SM_USER";
    public static final String OPENID_USER_ID_KEY = "OIDC_CLAIM_sub";
    public static final String LDAP_ATTRIBUTE_KEY_ROLE = "dcxIapEntGrps"; // offizieller Attributname für Rollen
    public static final String LDAP_ATTRIBUTE_KEY_COST_UNIT = "dcxCostCenterGlobal"; // offizieller Attributname für Kostenstelle
    public static final String LDAP_ATTRIBUTE_KEY_CORPORATE_AFFILIATION = "dcxGroupType"; // offizieller Attributname für Unternehmenszugehörigkeit (0 = MB, 1 = DT)
    public static final String LDAP_ATTRIBUTE_KEY_UID = "uid"; // offizieller Attributname für App-Berechtigungen
    public static final String LDAP_ATTRIBUTE_KEY_GIVEN_NAME = "givenName"; // offizieller Attributname für Vornamen
    public static final String LDAP_ATTRIBUTE_KEY_SURNAME = "sn"; // offizieller Attributname für Nachnamen
    public static final String LDAP_ATTRIBUTE_KEY_COMMON_NAME = "cn"; // offizieller Attributname für "common name"
    public static final String LDAP_ATTRIBUTE_KEY_EMAIL = "mail"; // offizieller Attributname für Email
    public static final String LDAP_FALLBACK_NODE = "o=iapdir"; // der erste Knoten im Verzeichnisbaum
    public static final String LDAP_FACTORY_IMPLEMENTATION = "AUTO";
    public static final String LDAP_UID_PREFIX = LDAP_ATTRIBUTE_KEY_UID + "=";
    public static final String LDAP_SEARCH_WILDCARD = "*";

    private static LDAPHelper instance;

    private FrameworkThread syncThread;
    private iPartsDateTimeHelper dateTimeHelper;

    private LDAPHelper() {
    }

    public static LDAPHelper getInstance() {
        if (instance == null) {
            instance = new LDAPHelper();
        }
        return instance;
    }

    /**
     * Liefert den Wert für die Suche von Benutzern im LDAP-Verzeichnis zurück.
     *
     * @return
     */
    public static String getLdapSearchValueForUsers() {
        String searchValue = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_SEARCH_VALUE);
        StrUtils.addCharacterIfLastCharacterIsNot(searchValue, '*');
        return searchValue;
    }

    /**
     * Initialisiert die Verbindung zum Server
     *
     * @return
     */
    private LdapJndi initConnection() {
        if (iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_LDAP_AUTH_ACTIVE)) {
            String host = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_HOST);
            String port = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_PORT);
            String ldapUser = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_USER);
            PasswordString ldapPassword = iPartsPlugin.getPluginConfig().getConfigValueAsPassword(iPartsPlugin.CONFIG_LDAP_PASSWORD);
            LdapSecurityOptions securityLevel = LdapSecurityOptions.NONE.getFromDescription(iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_SECURITY));

            int portNumber = INVALID_PORT;
            if (StrUtils.isInteger(port)) {
                portNumber = Integer.valueOf(port);
            }

            if (StrUtils.isValid(host, port) && (portNumber != INVALID_PORT) && checkSecurityParameter(securityLevel,
                                                                                                       ldapUser,
                                                                                                       ldapPassword.decrypt(), host,
                                                                                                       port)) {

                boolean secureConnectionActive = isSecureConnectionActive();
                try {
                    LdapJndi currentLdapConnection = new LdapJndi(LDAP_FACTORY_IMPLEMENTATION, host, portNumber, ldapUser,
                                                                  ldapPassword, securityLevel.getContextValue(), "",
                                                                  secureConnectionActive);
                    Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Successfully established LDAP connection to host \""
                                                                             + LdapJndi.makeLdapURL(host, port, secureConnectionActive)
                                                                             + "\". User: " + ldapUser + "; Security level: "
                                                                             + TranslationHandler.translate(securityLevel.getDescription()));
                    return currentLdapConnection;
                } catch (NamingException e) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "Error while connecting to LDAP directory \"" +
                                                                             LdapJndi.makeLdapURL(host, port, secureConnectionActive)
                                                                             + "\". " + "Original exception: " + e.toString());
                    return null;
                }
            }
        }
        return null;
    }

    private boolean isSecureConnectionActive() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_LDAP_USE_SECURE_CONNECTION);
    }

    /**
     * Überprüft, ob Benutzername und Passwort gesetzt sind, sofern eine Sicherheitsstufe > "None" gesetzt wurde
     *
     * @param securityLevel
     * @param ldapUser
     * @param ldapPassword
     * @param host
     * @param port
     * @return
     */
    private boolean checkSecurityParameter(LdapSecurityOptions securityLevel, String ldapUser, String ldapPassword, String host,
                                           String port) {
        if (securityLevel == LdapSecurityOptions.NONE) {
            return true;
        } else if (StrUtils.isValid(ldapUser, ldapPassword)) {
            return true;
        }
        Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "Security parameters not valid for LDAP Connection "
                                                                 + LdapJndi.makeLdapURL(host, port, isSecureConnectionActive()));
        return false;
    }

    /**
     * Erstellt eine LDAP URL für die im Admin-Modus konfigurierten Werte.
     *
     * @return
     */
    public String makeLdapURL() {
        String host = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_HOST);
        String port = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_PORT);
        return LdapJndi.makeLdapURL(host, port, isSecureConnectionActive());
    }

    /**
     * Schließt die Verbindung zum LDAP Server und löscht alle dazugehörigen Parameter
     */
    private void closeConnection(LdapJndi currentLdapConnection) {
        if (currentLdapConnection != null) {
            currentLdapConnection.release();
            Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Successfully closed LDAP connection to host \""
                                                                     + makeLdapURL() + "\".");
        }
    }

    /**
     * Überprüft die Rechte eines einzelnen Benutzers bei der Anmeldung mit externer Benutzer-ID (OpenID oder Siteminder).
     *
     * @return
     */
    public synchronized boolean checkExternalLdapUser(LdapUser ldapUser, String externalUserId) {
        if (ldapUser == null) {
            return false;
        }
        boolean checkOK = true;
        // Hat der LDAP Benutzer eine UID, dann wurde er im LDAP gefunden. Hat er keine, dann enthält das LdapUser nur den
        // Statustext
        String ldapUserId = ldapUser.getUid();
        boolean existsInLdap = StrUtils.isValid(ldapUserId);
        SQLStatement statement = null;
        try {
            ConnectionPool connectionPool = iPartsUserAdminDb.get().getConnectionPool(false);
            statement = connectionPool.getNewStatementNoAutoCommit();
            try {
                if (existsInLdap) {
                    // Benutzer existiert im LDAP Verzeichnis
                    UserDbObject userDbObject = UserDbObject.getUserDbByName(connectionPool, statement, ldapUserId);
                    if (userDbObject == null) {
                        // Benutzer in LDAP und nicht in der Benutzerverwaltung: Falls iParts-Rollen vorhanden sind -> anlegen
                        if (ldapUser.hasIPartsRoles()) {
                            addOrUpdateLdapUser(connectionPool, statement, ldapUser, false);
                        } else {
                            checkOK = false;
                        }
                    } else {
                        // Benutzer in LDAP und in der Benutzerverwaltung:
                        // 1. iParts-Rollen vorhanden -> Benutzer updaten
                        // 2. iParts-Rollen nicht vorhanden -> Benutzer deaktivieren
                        if (ldapUser.hasIPartsRoles()) {
                            addOrUpdateLdapUser(connectionPool, statement, ldapUser, true);
                        } else {
                            deactivateUser(connectionPool, statement, userDbObject);
                            checkOK = false;
                        }
                    }
                    iPartsUserAdminCache.removeUserFromCache(ldapUserId);
                } else {
                    // Existiert nicht im LDAP Verzeichnis, obwohl nach der uid gesucht wurde. Das sollte nicht passieren.
                    // Falls schon, dann wird überprüft, ob der Benutzer in der Benutzerverwaltung enthalten ist. Ist er enthalten,
                    // dann wird er deaktiviert.
                    if (StrUtils.isValid(externalUserId)) {
                        UserDbObject userDbObject = UserDbObject.getUserDbByName(connectionPool, statement, externalUserId);
                        if (userDbObject != null) {
                            deactivateUser(connectionPool, statement, userDbObject);
                            checkOK = false;
                        }
                        iPartsUserAdminCache.removeUserFromCache(externalUserId);
                    }
                }
                statement.commit();
                return checkOK;
            } catch (Exception e) {
                statement.rollback();
                Logger.getLogger().throwRuntimeException(e);
                return false;
            }
        } catch (Exception e) {
            String userId = existsInLdap ? ldapUserId : externalUserId;

            // Fehlgeschlagenen LDAP-Login aufgrund von einer Exception im Admin-Modus ignorieren
            if (userId == null) {
                Session session = Session.get();
                if ((session != null) && session.getStartParameter().getParameterBoolean(Constants.FRAMEWORK_PARAMETER_ADMIN, false)) {
                    if (iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_LOGIN_NEEDED_FOR_ADMIN_MODE)) {
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR,
                                                           new RuntimeException("Error while validating user login with LDAP information for accessing the admin monde", e));
                    }
                    return false;
                }
            }

            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR,
                                               new RuntimeException("Error while validating user login \""
                                                                    + userId + "\" with LDAP information", e));
            MessageDialog.showError(TranslationHandler.translate("!!Fehler bei der Validierung des Logins für Benutzer \"%1\"!",
                                                                 userId), "!!Fehler beim Login");
            return false;
        } finally {
            if (statement != null) {
                statement.release();
            }
        }
    }

    /**
     * Sucht einen LDAP Benutzer im LDAP Directory. Gesucht wird im Teilbaum, der über die Adminoptionen vorgegeben werden
     * kann. Sollte die Suche zu keinem Ergebnis führen wird im ganzen Baum gesucht.
     *
     * @param userId
     * @param ldapConnection Bei {@code null} wird innerhalb dieser Methode eine temporäre LDAP-Verbindung auf- und auch
     *                       wieder abgebaut.
     * @return
     */
    private LdapUser searchUserInDirectory(String userId, LdapJndi ldapConnection) {
        if (StrUtils.isValid(userId)) {
            boolean isTempLDAPConnection = false;
            if (ldapConnection == null) {
                isTempLDAPConnection = true;
                ldapConnection = initConnection();
            }
            try {
                if (ldapConnection != null) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Retrieving user with userId \"" + userId + "\"");
                    String searchSubtree = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_SUB_SEARCHTREE);
                    List<DN> foundUsers = retrieveUserFromDirectory(userId, searchSubtree, ldapConnection, false);
                    boolean fallbackSearch = iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_LDAP_SEARCH_WITH_FALLBACK);
                    // Sollte die Einschränkung auf den Teilbaum zu keinem Erfolg führen -> Suche nochmal ab dem ersten
                    // Knoten (sofern die Option im Adminmodus gesetzt wurde und der Teilbaum nicht eh schon der ganze Baum war)
                    if (foundUsers.isEmpty() && !searchSubtree.isEmpty() && fallbackSearch) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Fallback: Start searching user \""
                                                                                 + userId + "\" from main node \""
                                                                                 + LDAP_FALLBACK_NODE + "\"");
                        foundUsers = retrieveUserFromDirectory(userId, LDAP_FALLBACK_NODE, ldapConnection, false);
                    }
                    if (!foundUsers.isEmpty()) {
                        if (foundUsers.size() > 1) {
                            // Sollte eigentlich nicht passieren. Steig aus, weil nicht eindeutig
                            Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Failed LDAP user search attempt! " +
                                                                                     "Found more than one user with userid \"" +
                                                                                     userId + "\"");

                        } else if (foundUsers.size() == 1) {
                            return new LdapUser(foundUsers.get(0).getAttributes());
                        }
                    }
                }
            } catch (LdapException e) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "Error while looking up user \"" +
                                                                         userId + "\" in directory.");
            } finally {
                if (isTempLDAPConnection) {
                    closeConnection(ldapConnection);
                }
            }
        }
        return null;
    }

    /**
     * Sucht im übergebenen Teilbaum des LDAP Verzeichnisses nach dem LDAP Bentutzer der zur übergebenen User-ID passt
     * und gibt seine Attribute zurück.
     * Zuerst wird angenommen, dass es sich bei der User-ID um einen "common name" handelt und die Suche startet mit dem
     * Parameter "cn=userId". Sollte diese Suche kein Ergebnis liefern, wird als Fallback geprüft, ob es sich bei der
     * User-ID villeicht um eine LDAP spezifische "uid" handelt.
     *
     * @param userId
     * @param subtree
     * @param ldapConnection   Bei {@code null} wird innerhalb dieser Methode eine temporäre LDAP-Verbindung auf- und auch
     *                         wieder abgebaut.
     * @param wildcardsAllowed Darf die userId Wildcards enthalten?
     * @return
     * @throws LdapException
     */
    private List<DN> retrieveUserFromDirectory(String userId, String subtree, LdapJndi ldapConnection, boolean wildcardsAllowed) throws LdapException {
        List<DN> foundUsers = new DwList<DN>();
        if (!wildcardsAllowed && StrUtils.stringContains(userId, LDAP_SEARCH_WILDCARD)) {
            // Wildcards dürfen nicht verwendet werden, weil dadurch LDAP Injection möglich wäre
            Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Failed LDAP user search attempt! It is not " +
                                                                     "allowed to use wildacrds within the userId \"" +
                                                                     userId + "\"");
        } else {
            boolean isTempLDAPConnection = false;
            if (ldapConnection == null) {
                isTempLDAPConnection = true;
                ldapConnection = initConnection();
            }
            if (ldapConnection != null) {
                try {
                    foundUsers.addAll(ldapConnection.searchRecursively(subtree, LDAP_UID_PREFIX + userId));
                    if (foundUsers.isEmpty()) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Could not find user \"" + userId + "\" in directory \"" +
                                                                                 makeLdapURL() + "\". " +
                                                                                 "Parameters: userId \"" + userId + "\"; search " +
                                                                                 "subtree \"" + subtree + "\";");
                    }
                } finally {
                    if (isTempLDAPConnection) {
                        closeConnection(ldapConnection);
                    }
                }
            }
        }
        return foundUsers;
    }

    /**
     * Extrahiert den LDAP User aus den übergebenen Startparameter
     * Zuerst wird versucht, den OpenID User zu extrahieren mit Fallback auf den Siteminder User.
     *
     * @param startParameter
     * @return
     */
    public String extractLdapUserFromStartRequest(StartParameter startParameter) {
        if (startParameter instanceof UrlParameter) {
            UrlParameter urlParameter = (UrlParameter)startParameter;
            String openIdUser = getUserIdByParameter(urlParameter, OPENID_USER_ID_KEY);
            if (StrUtils.isValid(openIdUser)) {
                return openIdUser;
            }

            String siteminderUserId = getUserIdByParameter(urlParameter, SITEMINDER_USER_ID_KEY);
            if (StrUtils.isValid(siteminderUserId)) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Request header does not contain OpenID userId, " +
                                                                         "using Siteminder ID: " + siteminderUserId);
                return siteminderUserId;
            }
        }
        Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Request header does not contain LDAP userId! Header " +
                                                                 "parameters: " + startParameter.toString());
        return null;
    }


    private String getUserIdByParameter(UrlParameter urlParameter, String userIdParameterName) {
        List<String> userIds = urlParameter.getHeaders().get(userIdParameterName);
        if ((userIds == null) || userIds.isEmpty()) {
            // Nochmal mit kleingeschriebenen Attributnamen versuchen
            userIds = urlParameter.getHeaders().get(userIdParameterName.toLowerCase());
        }

        if ((userIds != null) && !userIds.isEmpty()) {
            return userIds.get(0);
        }
        return null;
    }


    /**
     * Überprüft, ob die User-Id in den Startparameter zu einem LDAP Benutzer führt, der der übergebenen Rolle
     * zugewiesen ist
     *
     * @param startParameter
     * @param roleToCheck
     * @return
     */
    public boolean userHasRole(StartParameter startParameter, String roleToCheck) {
        return userHasRole(searchUserInDirectory(extractLdapUserFromStartRequest(startParameter), null), roleToCheck);
    }

    /**
     * Überprüft, ob die übergebene User-Id zu einem LDAP Benutzer führt, der der übergebenen Rolle zugewiesen ist
     *
     * @param userId
     * @param roleToCheck
     * @return
     */
    public boolean userHasRole(String userId, String roleToCheck) {
        return userHasRole(searchUserInDirectory(userId, null), roleToCheck);
    }

    /**
     * Überprüft, ob der übergebene LDAP Benutzer der übergebenen Rolle zugewiesen ist
     *
     * @param ldapUser
     * @param roleToCheck
     * @return
     */
    public boolean userHasRole(LdapUser ldapUser, String roleToCheck) {
        if ((ldapUser == null) || !ldapUser.hasRoles()) {
            return false;
        }
        for (String role : ldapUser.getRoles()) {
            if (StrUtils.stringContains(role, roleToCheck, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Liefert den LDAP Benutzer als {@link LdapUser} in Abhängigkeit der übergebenen Parameter zurück. Aus den übergebenen
     * {@link StartParameter} wird die User-ID extrahiert. Zu dieser User-ID wird der passende LDAP Benutzer im LDAP
     * Verzeichnis gesucht. Falls vorhanden, erfolgt eine Abprüfung der Rollen. Abhängig vom Ergebnis wird ein {@link LdapUser}
     * zurückgeliefert.
     *
     * @param siteminderId
     * @param ldapRole
     * @param ldapConnection Bei {@code null} wird innerhalb dieser Methode eine temporäre LDAP-Verbindung auf- und auch
     *                       wieder abgebaut.
     * @return
     */
    public LdapUser getiPartsLdapUserFromDirectory(String siteminderId, String ldapRole, LdapJndi ldapConnection) {
        boolean isTempLDAPConnection = false;
        if (ldapConnection == null) {
            isTempLDAPConnection = true;
            ldapConnection = initConnection();
        }
        try {
            if (ldapConnection == null) {
                return new LdapUser("!!Verbindung zu LDAP Verzeichnis \"%1\" nicht möglich.", makeLdapURL());
            }
            return getLdapUserFromDirectory(siteminderId, ldapRole, ldapConnection);
        } finally {
            if (isTempLDAPConnection) {
                closeConnection(ldapConnection);
            }
        }
    }

    /**
     * Liefert den LDAP Benutzer als {@link LdapUser} in Abhängigkeit der übergebenen Parameter zurück. Zu der übergebenen
     * User-ID wird der passende LDAP Benutzer im LDAP Verzeichnis gesucht. Falls vorhanden, erfolgt eine Abprüfung der
     * Rollen. Abhängig vom Ergebnis wird ein wird ein {@link LdapUser} zurückgeliefert.
     *
     * @param userId
     * @param ldapRole
     * @param ldapConnection Bei {@code null} wird innerhalb dieser Methode eine temporäre LDAP-Verbindung auf- und auch
     *                       wieder abgebaut.
     * @return
     */
    public LdapUser getLdapUserFromDirectory(String userId, String ldapRole, LdapJndi ldapConnection) {
        if (StrUtils.isValid(userId)) {
            LdapUser ldapUser = searchUserInDirectory(userId, ldapConnection);
            return setLdapUserStatusText(ldapUser, ldapRole, userId);
        }
        return new LdapUser("!!Keine gültige LDAP User-Id (Fallback auf Benutzer \"%1\")", FrameworkUtils.getUserName());
    }

    /**
     * Setzt den Statustext eines LDAP Benutzers in Abhängigkeit der übergebenen Parameter. Sofern beide Parameter gültig
     * sind, erfolgt eine Abprüfung der Rollen. Abhängig vom Ergebnis wird ein Statustext gesetzt.
     *
     * @param ldapUser
     * @param ldapRole
     * @param userId
     * @return
     */
    public LdapUser setLdapUserStatusText(LdapUser ldapUser, String ldapRole, String userId) {
        String statusText;
        String[] statusTextPlaceHolders;
        if (ldapUser != null) {
            if (StrUtils.isValid(ldapRole)) {
                if (userHasRole(ldapUser, ldapRole)) {
                    statusText = "!!Benutzer: %1 - Rolle: %2";
                    statusTextPlaceHolders = new String[]{ ldapUser.buildNameFromAttributes(), ldapRole };
                } else {
                    statusText = "!!Benutzer \"%1\" ist der Rolle \"%2\" nicht zugeordnet.";
                    statusTextPlaceHolders = new String[]{ ldapUser.buildNameFromAttributes(), ldapRole };
                }
            } else {
                statusText = "!!Benutzer: %1";
                statusTextPlaceHolders = new String[]{ ldapUser.buildNameFromAttributes() };
            }
            ldapUser.setStatusText(statusText, statusTextPlaceHolders);
            return ldapUser;
        } else {
            LdapUser user = new LdapUser("!!Benutzer mit User-Id \"%1\" konnte nicht gefunden werden!", userId);
            return user;
        }
    }

    public boolean isLdapAdminOptionActivated() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_LDAP_AUTH_ACTIVE);
    }

    /**
     * Initialisiert den Thread, der die Synchronisierung der Benutzer mit LDAP übernimmt und beendet vorher einen evtl.
     * bereits laufenden Thread.
     */
    public synchronized void startLdapSyncThread(EtkProject project, Session session) {
        if ((project == null) || (session == null)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "EtkProject or session for LDAPHelper.startLdapSyncThread() is null.");
            return;
        }

        stopSyncThread();
        final UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
        if (!pluginConfig.getConfigValueAsBoolean(iPartsPlugin.CONFIG_LDAP_AUTH_ACTIVE)) {
            return;
        }

        if (!pluginConfig.getConfigValueAsBoolean(iPartsPlugin.CONFIG_USER_ADMIN_ENABLED)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "LDAP synchronization thread not started because user administration is disabled");
            return;
        }

        dateTimeHelper = new iPartsDateTimeHelper();
        syncThread = session.startChildThread(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Starting LDAP synchronization thread with " +
                                                                         pluginConfig.getConfigValueAsInteger(iPartsPlugin.CONFIG_LDAP_SYNC_DELAY)
                                                                         + " minutes delay...");
                while (AbstractApplication.getApplication().isRunning()) {
                    long waitingTime = dateTimeHelper.checkInterval((iPartsTimeInterval)pluginConfig.getConfigValue(iPartsPlugin.CONFIG_LDAP_SYNC_INTERVAL),
                                                                    pluginConfig.getConfigValueAsInteger(iPartsPlugin.CONFIG_LDAP_SYNC_DELAY) * 60);
                    if (waitingTime >= 0) { // Aktueller Zeitpunkt befindet sich im Intervall -> Syncen und danach warten
                        syncLdapDirectoryWithIParts();
                    } else { // Aktueller Zeitpunkt befindet sich nicht im Intervall -> 1 Minute warten
                        waitingTime = 60 * 1000; // 1 Minute
                    }

                    if (Java1_1_Utils.sleep(waitingTime)) {
                        break;
                    }
                }
                Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "LDAP synchronization thread finished");
            }
        });
        syncThread.setName("LDAP synchronization thread");
    }

    /**
     * Beendet den Thread, der die Synchronisierung der Benutzer mit LDAP übernimmt.
     */
    public synchronized void stopSyncThread() {
        if (syncThread != null) {
            syncThread.cancel();
            syncThread = null;
            dateTimeHelper = null;
        }
    }

    /**
     * Synchronisiert die LDAP Benutzer mit der Benutzerverwaltung
     *
     * @return
     */
    public synchronized boolean syncLdapDirectoryWithIParts() {
        if (!iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_USER_ADMIN_ENABLED)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "LDAP synchronization not possible because user administration is disabled");
            return false;
        }

        Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "Starting LDAP synchronization...");
        LdapJndi ldapConnection = initConnection();
        try {
            if (ldapConnection != null) {
                String attName = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_SEARCH_ATTRIBUTE);
                String searchValue = getLdapSearchValueForUsers();
                String searchSubtree = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_LDAP_SUB_SEARCHTREE);
                if (!StrUtils.isValid(attName, searchValue)) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "LDAP search attribute name and search value must not " +
                                                                             "be null or empty! Attribute name: " + attName +
                                                                             "; value: " + searchValue);
                    return false;
                }
                String searchString = attName + "=" + searchValue;
                if (StrUtils.isEmpty(searchSubtree)) {
                    searchSubtree = LDAP_FALLBACK_NODE;
                }

                List<DN> foundUsers = ldapConnection.searchRecursively(searchSubtree, searchString);
                if (foundUsers.isEmpty()) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "Could not find users with iParts roles in LDAP directory \"" +
                                                                             makeLdapURL() + "\". " +
                                                                             "Parameters: search string \"" + searchString + "\";" +
                                                                             "subtree \"" + LDAP_FALLBACK_NODE + "\"");
                    return false;
                }
                Map<String, LdapUser> foundUserMap = new HashMap<String, LdapUser>();
                for (DN foundUser : foundUsers) {
                    LdapUser ldapUser = new LdapUser(foundUser.getAttributes());
                    foundUserMap.put(ldapUser.getUid(), ldapUser);
                }
                syncUsers(foundUserMap);
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "LDAP synchronization failed because no connection to the LDAP directory could be established");
                return false;
            }
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, new RuntimeException("Error during LDAP synchronization with iParts", e));
            return false;
        } finally {
            closeConnection(ldapConnection);
        }

        Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "LDAP synchronization finished successfully");
        return true;
    }

    /**
     * Synchronisiert die aus LDAP ausgelesenen Benutzer mit der Benutzerverwaltung
     *
     * @param ldapUserMap
     * @throws SQLException
     */
    private synchronized void syncUsers(Map<String, LdapUser> ldapUserMap) throws SQLException, ConnectionPoolException, IOException, NoSuchAlgorithmException {
        ConnectionPool connectionPool = iPartsUserAdminDb.get().getConnectionPool(false);
        List<UserDbObject> existingLdapUsers = iPartsUserAdminDb.getAllLdapCreatedUsers();
        // Falls schon LDAP Benutzer in der Benutzerverwaltung existieren, dann vergleiche die LDAP Benutzer mit den Benutzern in der DB:
        // 1. Benutzer in beiden Listen -> Update
        // 2. Benutzer in DB aber nicht in neuer Liste -> Deaktivieren
        // 3. Benutzer nicht in DB -> Anlegen
        for (UserDbObject existingUser : existingLdapUsers) {
            if (ldapUserMap.containsKey(existingUser.getUserName())) {
                addOrUpdateLdapUser(connectionPool, null, ldapUserMap.remove(existingUser.getUserName()), true);
            } else {
                deactivateUser(connectionPool, null, existingUser);
            }
        }
        if (!ldapUserMap.isEmpty()) {
            for (LdapUser ldapUser : ldapUserMap.values()) {
                addOrUpdateLdapUser(connectionPool, null, ldapUser, false);
            }
        }

        iPartsUserAdminDb.createUserListCache(true); // Benutzer-Cache neu aufbauen
        iPartsUserAdminDb.clearAllUserAdminCaches();
    }

    /**
     * Deaktiviert einen Benutzer und löscht seine iParts-Rollen (innerhalb eines Statements)
     *
     * @param connectionPool
     * @param previousStatement
     * @param userDbObject
     * @throws ConnectionPoolException
     * @throws SQLException
     */
    private synchronized void deactivateUser(ConnectionPool connectionPool, SQLStatement previousStatement, UserDbObject userDbObject) throws ConnectionPoolException, SQLException {
        boolean logDeactivatedMessage = userDbObject.isActive();

        SQLStatement statement = previousStatement;
        boolean hasPreviousStatement = previousStatement != null;
        try {
            if (!hasPreviousStatement) {
                statement = connectionPool.getNewStatementNoAutoCommit();
            }
            try {
                String userId = userDbObject.getUserId();
                List<RoleDbObject> userRoles = UserRolesDbObject.getRoleDbs(connectionPool, statement, userId);
                String searchValue = getLdapSearchValueForUsers();
                for (RoleDbObject userRole : userRoles) {
                    if (StrUtils.matchesSqlLike(searchValue, userRole.getRoleName())) {
                        UserRolesDbObject.removeUserRole(connectionPool, statement, userId, userRole.getRoleId());
                        logDeactivatedMessage = true;
                    }
                }
                if (userDbObject.isActive()) { // Ist das Deaktivieren notwendig?
                    UserDbObject.deactivateUser(connectionPool, statement, userId);
                }
                if (logDeactivatedMessage) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "User \"" + userDbObject.getUserName() + "\" deactivated and all iParts roles removed in the DB from LDAP");
                }
                if (!hasPreviousStatement) {
                    statement.commit();
                }
            } catch (Exception e) {
                if (!hasPreviousStatement) {
                    statement.rollback();
                }
                Logger.getLogger().throwRuntimeException(e);
            }
        } finally {
            if (!hasPreviousStatement && (statement != null)) {
                statement.release();
            }
        }
    }

    /**
     * Fügt einen neuen Benutzer hinzu. Bei einem bestehenden Benutzer werden die iParts-Rollen aktualisiert.
     *
     * @param connectionPool
     * @param previousStatement
     * @param ldapUser
     * @param isUserWithLdapProperty
     */
    private synchronized void addOrUpdateLdapUser(ConnectionPool connectionPool, SQLStatement previousStatement, LdapUser ldapUser,
                                                  boolean isUserWithLdapProperty) throws NoSuchAlgorithmException, SQLException,
                                                                                         IOException, ConnectionPoolException {
        String userId = ldapUser.getUid();
        Set<String> ldapIPartsRoles = ldapUser.getIPartsRoles();
        if (StrUtils.isEmpty(userId) || ldapIPartsRoles.isEmpty()) {
            // Sollte nicht passieren, da vorher explizit nach Benutzer mit iParts-Rollen gesucht wird
            Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "Error while trying to add user with invalid ID or no iParts roles from LDAP!");
            return;
        }
        SQLStatement statement = previousStatement;
        boolean hasPreviousStatement = previousStatement != null;
        try {
            if (!hasPreviousStatement) {
                statement = connectionPool.getNewStatementNoAutoCommit();
            }
            String dbUserId = "";
            try {
                UserDbObject userDbObject = UserDbObject.getUserDbByName(connectionPool, statement, userId);
                if (!isUserWithLdapProperty && (userDbObject == null)) {
                    // Existiert noch nicht -> Anlegen
                    dbUserId = UserDbObject.addUser(connectionPool, statement, userId, userId, true);
                    if (dbUserId == null) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "Error while adding user \"" + userId + "\" to the DB from LDAP!");
                        return;
                    }
                    userDbObject = UserDbObject.getUserDb(connectionPool, statement, dbUserId);
                    UserRolesDbObject.addUserRole(connectionPool, statement, true, dbUserId, RoleDbObject.CONST_ROLE_ID_REGISTERED_USER);
                    Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "New user \"" + userId + "\" added to the DB from LDAP");
                } else {
                    // Existiert schon -> iParts-Rollen aktualisieren
                    dbUserId = userDbObject.getUserId();
                    List<RoleDbObject> userRoles = UserRolesDbObject.getRoleDbs(connectionPool, statement, dbUserId);
                    String searchValue = getLdapSearchValueForUsers();
                    for (RoleDbObject userRole : userRoles) {
                        String roleName = userRole.getRoleName();
                        if (StrUtils.matchesSqlLike(searchValue, roleName)) {
                            if (!ldapUser.hasRole(roleName)) {
                                UserRolesDbObject.removeUserRole(connectionPool, statement, dbUserId, userRole.getRoleId());
                                Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "iParts role \"" + roleName + "\" removed for user \""
                                                                                         + userId + "\" in the DB from LDAP");
                            } else {
                                ldapIPartsRoles.remove(roleName);
                            }
                        }
                    }

                    // Ist der Benutzer inaktiv und bekommt neue iParts-Rollen zugewiesen bzw. besitzt bereits alle iParts-Rollen
                    // aus dem LDAP Verzeichnis, dann wird er auf "aktiv" gesetzt.
                    boolean hasNewIPartsRoles = !ldapIPartsRoles.isEmpty();
                    boolean hasAlreadyAllLdapIPartsRoles = ldapIPartsRoles.isEmpty() && ldapUser.hasIPartsRoles();
                    if (!userDbObject.isActive() && (hasNewIPartsRoles || hasAlreadyAllLdapIPartsRoles)) {
                        // Unterscheidung, ob altes Passwort-Verfahren mit MD5 oder neues mit SHA und Salt
                        if (userDbObject.hasSalt()) {
                            UserDbObject.editUserWithPasswordAlreadyAsSHA(connectionPool, statement, dbUserId, userDbObject.getUserName(),
                                                                          userDbObject.getPassword(), userDbObject.getSalt(), true);
                        } else {
                            UserDbObject.editUserWithPasswordAlreadyAsMD5IfNecessary(connectionPool, statement, dbUserId, userDbObject.getUserName(),
                                                                                     userDbObject.getPassword(), true);
                        }
                        Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "User \"" + userId + "\" reactivated in the DB from LDAP");
                    }
                }

                // iParts-Rollen aus LDAP mit Benutzer verknüpfen
                addLdapRolesToUser(connectionPool, statement, ldapIPartsRoles, userDbObject);

                // Eigenschaften setzen
                setLdapUserProperties(connectionPool, statement, ldapUser, dbUserId);

                if (!hasPreviousStatement) {
                    statement.commit();
                }
            } catch (Exception e) {
                if (!hasPreviousStatement) {
                    statement.rollback();
                }
                throw new RuntimeException("LDAP synchronization error for user ID: " + dbUserId, e);
            }
        } finally {
            if (!hasPreviousStatement && (statement != null)) {
                statement.release();
            }
        }
    }

    /**
     * Setzt die LDAP Eigenschaften eines Benutzers
     *
     * @param connectionPool
     * @param statement
     * @param ldapUser
     * @param dbUserId
     * @throws SQLException
     */
    private void setLdapUserProperties(ConnectionPool connectionPool, SQLStatement statement, LdapUser ldapUser, String dbUserId) throws SQLException {
        UserAdminDbActions.setFirstName(connectionPool, statement, dbUserId, ldapUser.getGivenName());
        UserAdminDbActions.setLastName(connectionPool, statement, dbUserId, ldapUser.getSurname());
        UserAdminDbActions.setEmailAddress(connectionPool, statement, dbUserId, ldapUser.getEmail());
        UserPropertiesDbObject.setUserProperty(connectionPool, statement, dbUserId, iPartsUserAdminDb.APP_ID,
                                               UserPropertiesDbObject.CONST_ORG_ID_FOR_ALL_ORGS, iPartsUserAdminDb.PROPERTY_ID_LDAP_USER,
                                               new ExtendedPropertyType(PropertyType.BOOLEAN), Boolean.TRUE);
        UserPropertiesDbObject.setUserProperty(connectionPool, statement, dbUserId, iPartsUserAdminDb.APP_ID,
                                               UserPropertiesDbObject.CONST_ORG_ID_FOR_ALL_ORGS, iPartsUserAdminDb.PROPERTY_ID_CORPORATE_AFFILIATION,
                                               new ExtendedPropertyType(PropertyType.STRING), ldapUser.getCorporateAffiliation());
        UserPropertiesDbObject.setUserProperty(connectionPool, statement, dbUserId, iPartsUserAdminDb.APP_ID,
                                               UserPropertiesDbObject.CONST_ORG_ID_FOR_ALL_ORGS, iPartsUserAdminDb.PROPERTY_ID_COST_UNIT,
                                               new ExtendedPropertyType(PropertyType.STRING), ldapUser.getCostUnit());
    }

    /**
     * Weist einem Benutzer die aus LDAP übergebenen iParts-Rollen zu.
     *
     * @param connectionPool
     * @param statement
     * @param ldapRoles
     * @param userDbObject
     * @throws SQLException
     */
    private void addLdapRolesToUser(ConnectionPool connectionPool, SQLStatement statement, Set<String> ldapRoles, UserDbObject userDbObject) throws SQLException {
        if (ldapRoles.isEmpty()) { // Keine neuen Rollen
            return;
        }

        List<String> addedRoles = new DwList<>(ldapRoles.size());
        for (String roleName : ldapRoles) {
            RoleDbObject roleObject = RoleDbObject.getRoleDbByName(connectionPool, statement, roleName);
            if (roleObject == null) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.ERROR, "iParts role \"" + roleName + "\" does not exist in the DB and will be ignored for user \""
                                                                         + userDbObject.getUserName() + "\"");
            } else {
                String roleId = roleObject.getRoleId();
                UserRolesDbObject.addUserRole(connectionPool, statement, true, userDbObject.getUserId(), roleId);
                addedRoles.add(roleName);
            }
        }

        if (!addedRoles.isEmpty()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_LDAP, LogType.DEBUG, "iParts roles [" + StrUtils.stringListToString(addedRoles, ", ")
                                                                     + "] added for user \"" + userDbObject.getUserName()
                                                                     + "\" in the DB from LDAP");
        }
    }
}