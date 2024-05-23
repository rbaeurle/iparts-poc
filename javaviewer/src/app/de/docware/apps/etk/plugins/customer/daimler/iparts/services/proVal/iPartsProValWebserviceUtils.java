/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.BaseiPartsOAuthServiceUtils;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenUtils;

/**
 * Service-Klasse für alle Aufrufe von ProVal Webservices
 */
public class iPartsProValWebserviceUtils {

    private static final OAuthAccessTokenUtils accessTokenUtils = new OAuthAccessTokenUtils("ProVal", iPartsPlugin.LOG_CHANNEL_PROVAL);

    public static void clearCache() {
        accessTokenUtils.clearAccessToken();
    }

    /**
     * Methode zum Aufruf von einem Webservice der ProVal.
     *
     * @param proValWebserviceUrl
     * @param webserviceName
     * @param language
     */
    public static String getJsonFromWebservice(String proValWebserviceUrl, String webserviceName, String language) throws CallWebserviceException {
        String tokenUrl = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_PROVAL_TOKEN_URL);
        String clientId = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_PROVAL_TOKEN_CLIENT_ID);
        String clientSecret = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_PROVAL_TOKEN_CLIENT_SECRET);
        String token = BaseiPartsOAuthServiceUtils.retrieveAccessToken(language, false, accessTokenUtils, tokenUrl, clientId,
                                                                       clientSecret, "!!ProVal-Webservice", iPartsPlugin.LOG_CHANNEL_PROVAL);

        try {
            return createClientAndExecuteRequest(proValWebserviceUrl, webserviceName, token, language);
        } catch (CallWebserviceException callWebserviceException) {
            // Wenn der Response-Code 401 Unauthorized ist, dann soll der Access Token neu generiert und EINMALIG (!)
            // (deshalb nicht rekursiv) der Request neu ausgeführt werden.
            if (callWebserviceException.getHttpResponseCode() == HttpConstants.HTTP_STATUS_UNAUTHORIZED) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_PROVAL, LogType.INFO, "Received HTTP error code 401 (unauthorized). Forcing access token generation.");
                token = BaseiPartsOAuthServiceUtils.retrieveAccessToken(language, true, accessTokenUtils, tokenUrl, clientId,
                                                                        clientSecret, "!!ProVal-Webservice", iPartsPlugin.LOG_CHANNEL_PROVAL);
                return createClientAndExecuteRequest(proValWebserviceUrl, webserviceName, token, language);
            } else {
                throw callWebserviceException;
            }
        }
    }

    /**
     * Erzeugt einen HTTP-Client und führt den Request aus
     *
     * @param proValWebserviceUrl
     * @param webserviceName
     * @param token
     * @param language
     * @return
     * @throws CallWebserviceException
     */
    private static String createClientAndExecuteRequest(String proValWebserviceUrl, String webserviceName, String token,
                                                        String language) throws CallWebserviceException {
        String headerName = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_PROVAL_TOKEN_NAME);
        String tokenType = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_PROVAL_TOKEN_TYPE);
        return BaseiPartsOAuthServiceUtils.createClientAndExecuteRequest(proValWebserviceUrl, webserviceName, token, language,
                                                                         headerName, tokenType, "models/", false, iPartsPlugin.LOG_CHANNEL_PROVAL,
                                                                         "!!ProVal-Webservice");
    }
}