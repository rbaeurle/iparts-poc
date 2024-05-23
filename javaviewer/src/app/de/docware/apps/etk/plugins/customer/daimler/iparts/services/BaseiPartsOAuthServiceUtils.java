package de.docware.apps.etk.plugins.customer.daimler.iparts.services;

import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.http.client.HttpClient;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenContainer;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenException;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.file.DWFileCoding;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Basismethoden zum Erstellen eines OAUTH2 Webservices mit Abfrage der Daten
 */
public class BaseiPartsOAuthServiceUtils {

    private static final Charset CHARSET_UTF8 = Charset.forName(DWFileCoding.UTF8.getJavaCharsetName());

    /**
     * Gibt nach ergolreicher Webservice Anfrage eine Response im JSON Format zurück.
     *
     * @param basisUrl                   Aufzurufende URL
     * @param webserviceName             Name des Webservices
     * @param token                      Token für OAUTH2 Authorization
     * @param language                   Language
     * @param headerName                 Name für Header
     * @param tokenType                  Typ des Tokens
     * @param endpointName               Name des Endpoints
     * @param addItemsWrapper            Wrapper {@code items} hinzufügen?
     * @param logChannel                 Log-Channel
     * @param webserviceNameForException Name des Webservices auf deutsch für die Exception
     * @return Antwort-JSON
     * @throws CallWebserviceException
     */
    public static String createClientAndExecuteRequest(String basisUrl, String webserviceName, String token,
                                                       String language, String headerName, String tokenType,
                                                       String endpointName, boolean addItemsWrapper, LogChannels logChannel,
                                                       String webserviceNameForException) throws CallWebserviceException {
        final HttpClient client = new HttpClient(basisUrl);
        client.setParseCookiesEnabled(false);

        CallWebserviceException callWebserviceException;
        try {
            client.connect("");

            String tokenHeaderString = tokenType + " " + token;
            Logger.log(logChannel, LogType.DEBUG, "Using token for " + endpointName + webserviceName
                                                  + " web service call: " + headerName
                                                  + "=" + tokenHeaderString);
            client.setRequestProperty(headerName, tokenHeaderString);

            // An dieser Stelle muss der Token gültig sein (wird durch obige Prüfungen sichergestellt)
            client.setRequestProperty(HttpConstants.HEADER_FIELD_ACCEPT, MimeTypes.MIME_TYPE_JSON);
            client.setRequestProperty(HttpConstants.HEADER_FIELD_ACCEPT_LANGUAGE, language.toUpperCase());
            client.setRequestMethod(HttpConstants.METHOD_GET);

            // ResponseString bzw. ResponseMessage empfangen
            int httpResponseCode = client.getResponseCode();
            if (httpResponseCode == HttpConstants.HTTP_STATUS_OK) {
                byte[] responseBytes = Utils.readByteArray(client.getInputStream());
                if (responseBytes != null) {
                    String responseString = new String(responseBytes, CHARSET_UTF8);
                    if (addItemsWrapper) {
                        return "{\"items\":" + responseString + "}";
                    } else {
                        return responseString;
                    }
                } else {
                    httpResponseCode = HttpConstants.HTTP_STATUS_NOT_FOUND;
                }
            }

            // Ab hier nur noch Fehlerbehandlung
            if (httpResponseCode == HttpConstants.HTTP_STATUS_NOT_FOUND) {
                return null;
            } else {
                String message;
                if (httpResponseCode == HttpConstants.HTTP_STATUS_UNAUTHORIZED) {
                    message = TranslationHandler.translate("!!Authorisierungsfehler");
                } else if (httpResponseCode == HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT) {
                    message = TranslationHandler.translate("!!Timeout");
                } else {
                    message = TranslationHandler.translate("!!Fehlerantwort vom Webservice \"%1\": %2", basisUrl,
                                                           httpResponseCode + " - " + client.getResponseMessage());
                }

                callWebserviceException = new CallWebserviceException(message);
                callWebserviceException.setHttpResponseCode(httpResponseCode);
                throw callWebserviceException;
            }

        } catch (IOException e) {
            callWebserviceException = new CallWebserviceException(TranslationHandler.translate("!!Fehler/Timeout beim " +
                                                                                               "Abrufen der Daten vom %1.",
                                                                                               TranslationHandler.translate(webserviceNameForException)), e);
            callWebserviceException.setHttpResponseCode(HttpConstants.HTTP_STATUS_INTERNAL_SERVER_ERROR);
            throw callWebserviceException;
        } finally {
            client.disconnect();
        }
    }

    public static String retrieveAccessToken(String language, boolean forceGeneratingAccessToken,
                                             OAuthAccessTokenUtils accessTokenUtils,
                                             String tokenUrl, String clientId, String clientSecret,
                                             String webserviceNameForException, LogChannels logChannel) throws CallWebserviceException {
        String token = null;
        // Prüfen ob der AccessToken gültig ist und ob zwingend ein neuer Token generiert werden soll, dann generieren
        if (!forceGeneratingAccessToken && accessTokenUtils.isAccessTokenValid()) {
            token = accessTokenUtils.getAccessToken().getAccessToken();
        } else {
            OAuthAccessTokenContainer accessToken = generateAccessToken(language, accessTokenUtils, tokenUrl, clientId, clientSecret,
                                                                        webserviceNameForException, logChannel);
            if (accessToken != null) {
                token = accessToken.getAccessToken();
            }
            if (StrUtils.isEmpty(token)) {
                throw new CallWebserviceException(TranslationHandler.translate("!!Access-Token für %1 konnte nicht erzeugt werden.",
                                                                               TranslationHandler.translate(webserviceNameForException)));
            }
        }
        return token;
    }

    private static OAuthAccessTokenContainer generateAccessToken(String language, OAuthAccessTokenUtils accessTokenUtils,
                                                                 String tokenUrl, String clientId, String clientSecret,
                                                                 String webserviceNameForException, LogChannels logChannel) throws CallWebserviceException {
        OAuthAccessTokenContainer accessToken;
        try {
            accessToken = accessTokenUtils.generateAccessToken(tokenUrl, clientId, clientSecret, language);
            Logger.log(logChannel, LogType.DEBUG, "New access token has been generated: " + accessToken.getAccessToken());
        } catch (OAuthAccessTokenException e) {
            throw new CallWebserviceException(TranslationHandler.translate("!!Access-Token für %1 konnte nicht erzeugt werden.",
                                                                           TranslationHandler.translate(webserviceNameForException)), e);
        }
        return accessToken;
    }
}
