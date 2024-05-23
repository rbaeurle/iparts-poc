/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenContainer;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenException;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenUtils;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFileCoding;
import de.docware.util.transport.TransportUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/**
 * Service-Klasse für alle Webservice-Aufrufe der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWebserviceUtils {

    private static final Charset CHARSET_UTF8 = Charset.forName(DWFileCoding.UTF8.getJavaCharsetName());
    private static OAuthAccessTokenUtils accessTokenUtils = new OAuthAccessTokenUtils("TruckBomFoundation", iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION);

    public static void clearCache() {
        accessTokenUtils.clearAccessToken();
    }

    /**
     * Methode zum Aufruf von einem Webservice der TruckBOM.foundation.
     *
     * @param webserviceName
     * @param requestBodyJson
     * @param project
     */
    public static String getJsonFromWebservice(String webserviceName, String requestBodyJson, EtkProject project) throws iPartsTruckBOMFoundationWebserviceException {
        String tokenUrl = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_URL);
        String clientId = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_CLIENT_ID);
        String clientSecret = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_CLIENT_SECRET);
        String webserviceBaseUrl = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_BASE_URI);
        LogChannels logChannel = iPartsPlugin.LOG_CHANNEL_TRUCK_BOM_FOUNDATION;
        String language = project.getDBLanguage();

        // MOCK-Service
        if (webserviceBaseUrl.contains("localhost") && (requestBodyJson != null) && !requestBodyJson.isEmpty()) {
            Logger.log(logChannel, LogType.INFO, "Using TruckBOM.foundation " + webserviceName + " simulation");
            Map<String, String> additionalHeaders = Collections.singletonMap("authentication", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJNVVJNQU5QIiwiaXNzIjoieGVudHJ5LXBvcnRhbCIsImNvdW50cnkiOiJERSIsImJyYW5kIjoiTUIiLCJicmFuY2giOiJQIiwiY29udHJhY3QiOnRydWUsImxhbmcxIjoiZGUiLCJsYW5nMiI6ImZyIiwibGFuZzMiOiJlcyIsImlhdCI6MTUxNjU3NTYwMCwiZXhwIjoxNzE5NzcyNDAwLCJwZXJtaXNzaW9ucyI6eyJNQiI6WyJUUlVDSyJdfX0.otYXLd7qIm62SPwXuYZKMaOvMXk6t8bvli0tNo860bi2qbyabp4mwiANF-PsLVTSWLE1X-kmS3PigKTgKLcey2HEeRq-sNByXeOUp-6uRtDhwGfDwfEYB_zCenTMXHmJiqWA_QM8qiaWqrWCfiHxLvWHwr253XYXfxUOLN6dh2M");
            // webserviceBaseUrl als vollständig annehmen und nicht webserviceName hinzufügen
            return iPartsTruckBOMFoundationWebserviceUtils.createClientAndExecuteRequest(webserviceBaseUrl, webserviceName,
                                                                                         requestBodyJson, additionalHeaders,
                                                                                         "", language, logChannel);
        }

        String completeUrl = StrUtils.addCharacterIfLastCharacterIsNot(webserviceBaseUrl, '/') + webserviceName;
        String token = iPartsTruckBOMFoundationWebserviceUtils.retrieveAccessToken(tokenUrl, clientId, clientSecret, language,
                                                                                   logChannel, false);

        try {
            return iPartsTruckBOMFoundationWebserviceUtils.createClientAndExecuteRequest(completeUrl, webserviceName, requestBodyJson,
                                                                                         null, token, language, logChannel);
        } catch (iPartsTruckBOMFoundationWebserviceException iPartsTruckBOMFoundationWebserviceException) {
            // Wenn der Response-Code 401 Unauthorized ist, dann soll der Access-Token neu generiert und der Request
            // EINMALIG (! deshalb nicht rekursiv) neu ausgeführt werden.
            if (iPartsTruckBOMFoundationWebserviceException.getHttpResponseCode() == HttpConstants.HTTP_STATUS_UNAUTHORIZED) {
                Logger.log(logChannel, LogType.INFO, "Received HTTP error code 401 (unauthorized). Forcing access token generation.");
                token = iPartsTruckBOMFoundationWebserviceUtils.retrieveAccessToken(tokenUrl, clientId, clientSecret, language,
                                                                                    logChannel, true);
                return iPartsTruckBOMFoundationWebserviceUtils.createClientAndExecuteRequest(completeUrl, webserviceName,
                                                                                             requestBodyJson, null, token, language, logChannel);
            } else {
                throw iPartsTruckBOMFoundationWebserviceException;
            }
        }
    }

    /**
     * Methode zur Erstellung eines HTTP-Clients für einen TruckBOM.foundation Webservice.
     * Nach Erstellung wird ein Request erzeugt, übermittelt und die Response im Erfolgsfall zurückgegeben.
     * Ansonsten Errorhandling.
     *
     * @param truckBOMFoundationWebserviceUrl  URL des TruckBOM.foundation Ziel-Webservices
     * @param truckBOMFoundationWebserviceName Name des TruckBOM.foundation Ziel-Webservices
     * @param additionalRequestHeaders
     * @param token                            Der Access-Token
     * @param language                         Die Sprache
     * @param logChannel                       Der zu verwendende Kanal für Log-Einträge
     * @return
     * @throws iPartsTruckBOMFoundationWebserviceException
     */
    public static String createClientAndExecuteRequest(String truckBOMFoundationWebserviceUrl, String truckBOMFoundationWebserviceName,
                                                       String requestBodyJson, Map<String, String> additionalRequestHeaders,
                                                       String token, String language, LogChannels logChannel) throws iPartsTruckBOMFoundationWebserviceException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            byte[] requestBodyJsonBytes = null;
            if ((requestBodyJson != null) && !requestBodyJson.isEmpty()) {
                requestBodyJsonBytes = requestBodyJson.getBytes(CHARSET_UTF8);
            }

            HttpUriRequestBase requestBase;
            if (requestBodyJsonBytes != null) {
                requestBase = new HttpPost(truckBOMFoundationWebserviceUrl);
                requestBase.setEntity(new ByteArrayEntity(requestBodyJsonBytes, null));
            } else {
                requestBase = new HttpGet(truckBOMFoundationWebserviceUrl);
            }

            RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(StandardCookieSpec.IGNORE).build();
            requestBase.setConfig(requestConfig);

            String headerName = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_NAME);
            String tokenType = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_TRUCK_BOM_FOUNDATION_TOKEN_TYPE);
            String tokenHeaderString = tokenType + " " + token;
            Logger.log(logChannel, LogType.DEBUG, "Using token for " + truckBOMFoundationWebserviceName + " Web service call: "
                                                  + headerName + "=" + tokenHeaderString);
            requestBase.setHeader(headerName, tokenHeaderString);
            requestBase.setHeader(HttpConstants.HEADER_FIELD_ACCEPT, MimeTypes.MIME_TYPE_JSON);
            requestBase.setHeader(HttpConstants.HEADER_FIELD_ACCEPT_LANGUAGE, language.toUpperCase());
            requestBase.setHeader(HttpConstants.HEADER_FIELD_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

            if ((additionalRequestHeaders != null) && !additionalRequestHeaders.isEmpty()) {
                for (Map.Entry<String, String> additionalHeader : additionalRequestHeaders.entrySet()) {
                    requestBase.setHeader(additionalHeader.getKey(), additionalHeader.getValue());
                }
            }

            final HttpClientResponseHandler<iPartsTruckBOMFoundationWebserviceHttpResponse> responseHandler = httpResponse -> {
                int status = httpResponse.getCode();
                HttpEntity entity = httpResponse.getEntity();
                ContentType contentType = TransportUtils.getOrDefault(entity);
                Charset charset = contentType.getCharset();
                if (charset == null) {
                    charset = StandardCharsets.UTF_8; // Fallback auf UTF-8
                }
                return new iPartsTruckBOMFoundationWebserviceHttpResponse(status, entity != null ? EntityUtils.toString(entity, charset) : null);
            };

            iPartsTruckBOMFoundationWebserviceHttpResponse response = httpClient.execute(requestBase, responseHandler);

            // Successhandling
            int httpResponseCode = response.getResponseStatusCode();
            if (httpResponseCode == HttpConstants.HTTP_STATUS_OK) {
                if (response.getResponseContent() != null) {
                    return response.getResponseContent();
                } else {
                    httpResponseCode = HttpConstants.HTTP_STATUS_NOT_FOUND;
                }
            }

            // Errorhandling
            if (httpResponseCode == HttpConstants.HTTP_STATUS_NOT_FOUND) {
                return null;
            } else {
                String message;
                if (httpResponseCode == HttpConstants.HTTP_STATUS_UNAUTHORIZED) {
                    message = TranslationHandler.translate("!!Authorisierungsfehler");
                } else if (httpResponseCode == HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT) {
                    message = TranslationHandler.translate("!!Timeout");
                } else {
                    message = TranslationHandler.translate("!!Fehlerantwort vom Webservice \"%1\": %2", truckBOMFoundationWebserviceUrl,
                                                           httpResponseCode + " - " + response.getResponseContent());
                }

                iPartsTruckBOMFoundationWebserviceException iPartsTruckBOMFoundationWebserviceException = new iPartsTruckBOMFoundationWebserviceException(message);
                iPartsTruckBOMFoundationWebserviceException.setHttpResponseCode(httpResponseCode);
                throw iPartsTruckBOMFoundationWebserviceException;
            }
        } catch (IOException e) {
            iPartsTruckBOMFoundationWebserviceException iPartsTruckBOMFoundationWebserviceException = new iPartsTruckBOMFoundationWebserviceException(TranslationHandler.translate("!!Fehler/Timeout beim Aufruf vom TruckBOM.foundation Webservice \"%1\"",
                                                                                                                                                                                   truckBOMFoundationWebserviceUrl)
                                                                                                                                                      + ":\n" + e.getMessage(), e);
            iPartsTruckBOMFoundationWebserviceException.setHttpResponseCode(HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT);
            throw iPartsTruckBOMFoundationWebserviceException;
        }
    }

    /**
     * Methode zum Erhalt eines Acces-Tokens für IAM-basierte Webservices von Daimler.
     * Generiert einen neuen Access-Token, falls nicht vorhanden oder ein neuer Access-Token zwingend neu generiert werden soll.
     *
     * @param tokenUrl                   Die Token-URL
     * @param clientId                   Die Client ID
     * @param clientSecret               Das Client Secret
     * @param language                   Sprache
     * @param logChannel                 Der zu verwendende Kanal für Log-Einträge
     * @param forceGeneratingAccessToken Gibt an, ob der Access-Token zwingend neu generiert werden soll
     * @return
     * @throws iPartsTruckBOMFoundationWebserviceException
     */
    public static String retrieveAccessToken(String tokenUrl, String clientId, String clientSecret, String language, LogChannels logChannel,
                                             boolean forceGeneratingAccessToken) throws iPartsTruckBOMFoundationWebserviceException {
        String token = null;
        // Prüfen ob der aktuelle Access-Token gültig ist und ob zwingend ein neuer Token generiert werden soll, dann generieren
        if (!forceGeneratingAccessToken && accessTokenUtils.isAccessTokenValid()) {
            token = accessTokenUtils.getAccessToken().getAccessToken();
        } else {
            OAuthAccessTokenContainer accessToken = generateAccessToken(tokenUrl, clientId, clientSecret, language, logChannel);
            if (accessToken != null) {
                token = accessToken.getAccessToken();
            }
            if (StrUtils.isEmpty(token)) {
                throw new iPartsTruckBOMFoundationWebserviceException(TranslationHandler.translate("!!Access-Token für TruckBOM.foundation Webservice konnte nicht erzeugt werden."));
            }
        }
        return token;
    }

    /**
     * Methode zur Erzeugung eines Acces-Tokens für IAM-basierte Webservices von Daimler
     *
     * @param tokenUrl     Die Token-URL
     * @param clientId     Die Client ID
     * @param clientSecret Das Client Secret
     * @param language     Sprache
     * @param logChannel   Der zu verwendende Kanal für Log-Einträge
     * @return
     * @throws iPartsTruckBOMFoundationWebserviceException
     */
    private static OAuthAccessTokenContainer generateAccessToken(String tokenUrl, String clientId, String clientSecret, String language,
                                                                 LogChannels logChannel) throws iPartsTruckBOMFoundationWebserviceException {
        OAuthAccessTokenContainer accessToken;
        try {
            accessToken = accessTokenUtils.generateAccessToken(tokenUrl, clientId, clientSecret, language);
            Logger.log(logChannel, LogType.DEBUG, "New access token has been generated: " + accessToken.getAccessToken());
        } catch (OAuthAccessTokenException e) {
            throw new iPartsTruckBOMFoundationWebserviceException(TranslationHandler.translate("!!Access-Token für TruckBOM.foundation Webservice konnte nicht erzeugt werden."), e);
        }
        return accessToken;
    }
}
