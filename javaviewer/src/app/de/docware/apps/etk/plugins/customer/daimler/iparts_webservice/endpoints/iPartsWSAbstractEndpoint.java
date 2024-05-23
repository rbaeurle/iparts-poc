/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObjectInterface;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentErrorResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPermissions;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSUserInfo;
import de.docware.framework.modules.config.defaultconfig.webservice.WebserviceSettings;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.http.server.HttpServerRequest;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.RESTfulErrorResponse;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.modules.webservice.restful.WSErrorResponse;
import de.docware.util.StrUtils;
import de.docware.util.java1_1.UTF8;
import de.docware.util.security.PasswordString;

import java.io.UTFDataFormatException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstrakte Klasse für iParts Endpoints.
 * <br/>Der Generic {@code REQUEST_CLASS} gibt die Klasse an, die für das Request-Objekt von diesem Endpoint erwartet und automatisch
 * aus einem JSON-String erzeugt wird. In anderen JAVA-Klassen heißen Generics meistens {@code <T>}.
 * Diese Klasse macht also die Annahme, dass der Request-Body genau ein JSON-Objekt enthält, welches zur Klasse REQUEST_CLASS
 * konvertiert werden kann.
 */
public abstract class iPartsWSAbstractEndpoint<REQUEST_CLASS extends WSRequestTransferObjectInterface> extends WSAbstractEndpoint<REQUEST_CLASS> {

    public static final String HEADER_ATTR_COUNTRY = "Country";
    public static final String HEADER_ATTR_PERMISSIONS = "Permissions";
    public static final String HEADER_ATTR_LANGUAGES = "Accept-Language";
    public static final String HEADER_ATTR_ISSUER = "Issuer";

    /**
     * Prüft ob die Tokengültigeit erfüllt wird (country, brand, branches). Falls nicht gibts eine exception
     * {@link #throwPermissionsError()}. Wenn nur retail-relevante Produkte verwendet werden sollen wird anschließend
     * geprüft ob das übergebene Produkt retail-relevant ist. Falls nicht, gibt es keine Exception. Die Behandlung
     * erfolgt dann im aufrufenden Webservice.
     *
     * @param project
     * @param product
     * @param userInfo
     * @return {@code false} wenn die Tokengültigkeit verletzt wird oder das Produkt nicht retail-relevant ist obwohl
     * nur retail-relevante Produkte verwendet werden sollen, sonst {@code true}
     */
    public static boolean checkRetailRelevantAndPermissionWithException(EtkProject project, iPartsProduct product, iPartsWSUserInfo userInfo) {
        if (product == null) {
            return false;
        }
        if (!product.isValidForPermissions(project, userInfo.getCountryForValidation(), userInfo.getPermissionsAsMapForValidation())) {
            // die Gültigkeitsprüfung bezüglich des JWT Token ist fehlgeschlagen -> exception
            throwPermissionsError();
        }
        boolean onlyRetailRelevantProducts = iPartsWebservicePlugin.isOnlyRetailRelevantProducts();
        if (onlyRetailRelevantProducts && !product.isRetailRelevant()) {
            // Produkt ist retail-relevant und es sollen nur retail-relevante bzw. gültige Produkte berücksichtigt werden -> leere Trefferliste
            return false;
        }
        return true;
    }


    public iPartsWSAbstractEndpoint(String endpointUri) {
        super(endpointUri, iPartsWebservicePlugin.LOG_CHANNEL_DEBUG, iPartsWebservicePlugin.LOG_CHANNEL_PERFORMANCE);
        logChannelSecure = iPartsWebservicePlugin.LOG_CHANNEL_TOKEN;
    }

    @Override
    protected WebserviceSettings setConfiguredCacheSettings() {
        setResponseCacheSize(iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_JSON_RESPONSE_CACHE_SIZE));
        setResponseCacheLifetime(iPartsPlugin.getCachesLifeTime());
        return null; // WebserviceSettings werden bei iParts nicht benötigt
    }

    /**
     * Wird aufgerufen, wenn ein {@link iPartsDataChangedEventByEdit} gefeuert wurde und sich somit Daten über Edit-
     * Funktionalitäten (z.B. Stammdateneditor) geändert haben. Es muss überprüft werden, ob der Response-Cache aufgrund
     * des Datentyps der Änderung nicht gelöscht werden muss.
     *
     * @param dataType
     */
    public abstract void dataChangedByEdit(iPartsDataChangedEventByEdit.DataType dataType);

    @Override
    protected SecureResult isValidRequestSignature(HttpServerRequest request) {
        // Header-Attribute für Berechtigungen zulässig?
        if (iPartsWebservicePlugin.getPluginConfig().getConfigValueAsBoolean(iPartsWebservicePlugin.CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS)) {
            String country = request.getHeader(HEADER_ATTR_COUNTRY);
            if (country != null) {
                country = country.trim();
            }
            String permissions = request.getHeader(HEADER_ATTR_PERMISSIONS);
            if (permissions != null) {
                permissions = permissions.trim();
            }
            String languages = request.getHeader(HEADER_ATTR_LANGUAGES);
            if (languages != null) {
                languages = languages.trim();
            }
            String issuer = request.getHeader(HEADER_ATTR_ISSUER);
            if (issuer != null) {
                issuer = issuer.trim();
            }

            // Alle Header-Attribute müssen vorhanden und nicht leer sein
            if (StrUtils.isValid(country, permissions, languages, issuer)) {
                // UserInfo erstellen aus den Header-Attributen und als SecurePayload setzen, damit die weitere Verarbeitung
                // identisch ist zum bisherigen Token
                iPartsWSUserInfo userInfo = new iPartsWSUserInfo();

                // Land
                userInfo.setCountry(country.toUpperCase());

                // Berechtigungen ermitteln (Beispiel: "MB.PASSENGER-CAR, MB.TRUCK, MB.VAN, SMT.PASSENGER-CAR")
                iPartsWSPermissions permissionsDTO = new iPartsWSPermissions();
                userInfo.setPermissions(permissionsDTO);
                List<String> brandAndBranches = StrUtils.toStringList(permissions, ",", false, true);
                for (String brandAndBranch : brandAndBranches) {
                    String brand = StrUtils.stringUpToCharacter(brandAndBranch, ".").trim().toUpperCase();
                    String branch = StrUtils.stringAfterCharacter(brandAndBranch, ".").trim().toUpperCase();
                    if (!StrUtils.isValid(brand, branch)) {
                        continue;
                    }

                    switch (brand) {
                        case iPartsConst.BRAND_MERCEDES_BENZ:
                            Set<String> branchesMercedesBenz = permissionsDTO.getBranchesMercedesBenz();
                            if (branchesMercedesBenz == null) {
                                branchesMercedesBenz = new HashSet<>();
                                permissionsDTO.setBranchesMercedesBenz(branchesMercedesBenz);
                            }
                            branchesMercedesBenz.add(branch);
                            break;
                        case iPartsConst.BRAND_SMART:
                            Set<String> branchesSmart = permissionsDTO.getBranchesSmart();
                            if (branchesSmart == null) {
                                branchesSmart = new HashSet<>();
                                permissionsDTO.setBranchesSmart(branchesSmart);
                            }
                            branchesSmart.add(branch);
                            break;
                        case iPartsConst.BRAND_MAYBACH:
                            Set<String> branchesMaybach = permissionsDTO.getBranchesMaybach();
                            if (branchesMaybach == null) {
                                branchesMaybach = new HashSet<>();
                                permissionsDTO.setBranchesMaybach(branchesMaybach);
                            }
                            branchesMaybach.add(branch);
                            break;
                    }
                }

                // Sprachen ermitteln (Beispiel: "DE, EN, FR")
                List<String> languagesList = StrUtils.toStringList(languages, ",", false, true);
                if (languagesList.size() >= 1) {
                    userInfo.setLang1(languagesList.get(0).toUpperCase());
                }
                if (languagesList.size() >= 2) {
                    userInfo.setLang2(languagesList.get(1).toUpperCase());
                }
                if (languagesList.size() >= 3) {
                    userInfo.setLang3(languagesList.get(2).toUpperCase());
                }

                // Aufrufer
                userInfo.setIss(issuer);

                // Unendliche Gültigkeit
                userInfo.setExp(-1);

                // UserInfo serialisieren und als SecurePayload setzen
                String userInfoFromHeaderAttributes = getGenson().serialize(userInfo);
                try {
                    request.setAttribute(REQUEST_ATTRIB_SECURE_PAYLOAD, UTF8.fromUTF8(userInfoFromHeaderAttributes));
                } catch (UTFDataFormatException e) {
                    Logger.logExceptionWithoutThrowing(logChannelSecure, LogType.ERROR, e);
                    return new SecureResult(SecureReturnCode.TOKEN_WRONG_FORMAT, "Header attributes have no valid UTF-8 encoding", e);
                }
                return new SecureResult(SecureReturnCode.SUCCESS);
            }
        }

        // Frühere Token-Validierung
        final String headerName = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsString(iPartsWebservicePlugin.CONFIG_HEADER_TOKEN_NAME);
        final String authorizationType = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsString(iPartsWebservicePlugin.CONFIG_HEADER_TOKEN_TYPE);

        // Passwort für HS256 Verfahren; wenn leer ist das Verfahren nicht zugelassen
        final PasswordString secret = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsPassword(iPartsWebservicePlugin.CONFIG_HEADER_TOKEN_PASSWORD);

        // Token validieren
        final int expiryDelay = iPartsWebservicePlugin.getPluginConfig().getConfigValueAsInteger(iPartsWebservicePlugin.CONFIG_HEADER_TOKEN_EXPIRES);

        // Benutzerdaten im Payload sind nur noch im DEVELOPMENT-Modus z.B. für die Webservice-Unittests zulässig
        final boolean userFallback = Constants.DEVELOPMENT && iPartsWebservicePlugin.getPluginConfig().getConfigValueAsBoolean(iPartsWebservicePlugin.CONFIG_USER_IN_PAYLOAD_FALLBACK);

        long currentTime = System.currentTimeMillis() / 1000;
        SecureResult secureResult = isValidJWT(request, headerName, authorizationType, secret, currentTime, expiryDelay,
                                               iPartsWebservicePlugin.getKeystores());

        // bei fehlgeschlagener Validierung gibt es ggf. einen Fallback auf Validierung per User DTO im Webservice.
        // wir geben dann in jedem Fall "Success" zurück
        // der Webservice erkennt die fehlgeschlagene JWT-Validierung an der fehlenden Payload im Request
        if ((secureResult.getReturnCode() != SecureReturnCode.SUCCESS) && userFallback) {
            secureResult = new SecureResult(SecureReturnCode.SUCCESS);
        }

        return secureResult;
    }

    /**
     * Gewünschte Sprache und Rückfallsprachen aus WS Request für Datenbankabfragen in EtkProject setzen.
     *
     * @param project
     * @param userInfo
     */
    protected void setCurrentDatabaseLanguageAndFallbackLanguages(EtkProject project, iPartsWSUserInfo userInfo) {
        // Rückfallsprachen zuerst setzen, die den Standardrückfallsprachen aus der DWK Konfiguration vorangestellt werden
        // sollen, weil diese in userInfo.getLanguage() als Fallback bei leerem lang1 verwendet werden
        project.setCustomDataBaseFallbackLanguages(getDatabaseFallbackLanguages(userInfo));

        // Gewünschte Sprache setzen
        String language = userInfo.getLanguage();
        project.getConfig().setCurrentDatabaseLanguage(language);
    }

    /**
     * DB Rückfallsprachen aus iPartsWSUserInfo setzen.
     * iPartsWSUserInfo enthält die Sprachen lang1, lang2, lang3.
     * lang1 ist die gewünschte Sprache. Im Sinne der PP Terminologie sind dann lang2 und lang3 die Rückfallsprachen.
     *
     * @param userInfo
     * @return
     */
    private static List<String> getDatabaseFallbackLanguages(iPartsWSUserInfo userInfo) {
        List<String> databaseFallbackLanguages = null;
        if (StrUtils.isValid(userInfo.getLang2())) {
            databaseFallbackLanguages = new ArrayList<>();
            databaseFallbackLanguages.add(userInfo.getLang2().toUpperCase());
        }
        if (StrUtils.isValid(userInfo.getLang3())) {
            if (databaseFallbackLanguages == null) {
                databaseFallbackLanguages = new ArrayList<>();
            }
            databaseFallbackLanguages.add(userInfo.getLang3().toUpperCase());
        }
        return databaseFallbackLanguages;
    }

    /**
     * Fehlermeldung für den Fall, dass die Tokengültigkeit verletzt wird inkl. Rückgabe fehlender Permissions.
     *
     * @throws RESTfulWebApplicationException
     */
    protected static void throwMissingPermissionsError(Set<String> missingPermissions) throws RESTfulWebApplicationException {
        WSErrorResponse errorResponse = new iPartsWSIdentErrorResponse(WSError.NOT_AUTHORIZED, "You are not authorized to access this information",
                                                                       missingPermissions);
        RESTfulErrorResponse restfulResponse = new RESTfulErrorResponse(HttpConstants.HTTP_STATUS_FORBIDDEN, errorResponse,
                                                                        MimeTypes.MIME_TYPE_JSON);
        throw new RESTfulWebApplicationException(restfulResponse);
    }
}