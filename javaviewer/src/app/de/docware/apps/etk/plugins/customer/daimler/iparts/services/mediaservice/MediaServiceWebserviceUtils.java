/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.BaseiPartsOAuthServiceUtils;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenUtils;

/**
 * Service-Klasse für alle Webservice-Aufrufe von MediaService-Endpoints
 */
public class MediaServiceWebserviceUtils {

    public static final OAuthAccessTokenUtils accessTokenUtils = new OAuthAccessTokenUtils("MediaService",
                                                                                            iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS);

    public static void clearCache() {
        accessTokenUtils.clearAccessToken();
    }

    /**
     * Gibt nach ergolreicher Webservice Anfrage eine Response im JSON Format zurück.
     *
     * @param mediaServiceUrl
     * @param webserviceName
     * @param language
     * @return
     */
    public static String getJsonFromPartNumbersWebservice(String mediaServiceUrl, String webserviceName, String language) throws CallWebserviceException {
        String tokenUrl = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_URL);
        String clientId = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_CLIENT_ID);
        String clientSecret = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_CLIENT_SECRET);
        String token = BaseiPartsOAuthServiceUtils.retrieveAccessToken(language, false, accessTokenUtils, tokenUrl, clientId,
                                                                       clientSecret, "!!Einzelteilbilder-Webservice", iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS);

        try {
            return createClientAndExecuteRequest(mediaServiceUrl, webserviceName, token, language);
        } catch (CallWebserviceException callWebserviceException) {
            // Wenn der Response-Code 401 Unauthorized ist, dann soll der Access Token neu generiert und EINMALIG (!)
            // (deshalb nicht rekursiv) der Request neu ausgeführt werden.
            if (callWebserviceException.getHttpResponseCode() == HttpConstants.HTTP_STATUS_UNAUTHORIZED) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.INFO, "Received HTTP error code 401 (unauthorized). Forcing access token generation.");
                token = BaseiPartsOAuthServiceUtils.retrieveAccessToken(language, true, accessTokenUtils, tokenUrl, clientId,
                                                                        clientSecret, "!!Einzelteilbilder-Webservice", iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS);
                return createClientAndExecuteRequest(mediaServiceUrl, webserviceName, token, language);
            } else {
                throw callWebserviceException;
            }
        }
    }

    private static String createClientAndExecuteRequest(String mediaServiceUrl, String webserviceName, String token, String language) throws CallWebserviceException {
        String headerName = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_NAME);
        String tokenType = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_TYPE);
        return BaseiPartsOAuthServiceUtils.createClientAndExecuteRequest(mediaServiceUrl, webserviceName, token, language,
                                                                         headerName, tokenType, "mediaservice/", true, iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS,
                                                                         "!!Einzelteilbilder-Webservice");
    }
}
