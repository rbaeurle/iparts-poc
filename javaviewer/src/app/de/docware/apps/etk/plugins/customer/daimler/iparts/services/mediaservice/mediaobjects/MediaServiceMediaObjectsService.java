/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.mediaobjects;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.BaseiPartsOAuthServiceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.MediaServiceWebserviceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.mediaservice.partnumbers.MediaServicePartNumbersService;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.http.client.HttpClient;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.j2ee.EC;

import java.io.IOException;
import java.util.Set;

/**
 * Service-Klasse für die Anzeige von Einzelteilbildern vom Mediaservice
 */
public class MediaServiceMediaObjectsService {

    public static final String WEBSERVICE_NAME = "media/partnumber";
    private static Genson genson = JSONUtils.createGenson(true);

    private static ObjectInstanceLRUList<String, FrameworkImage> mediaObjectsCache = new ObjectInstanceLRUList<>(100, 8 * 60 * 60, true); // 100 MediaObjects für 8 Stunden

    public static void clearCache() {
        mediaObjectsCache.clear();
    }

    /**
     * MediaObjects für die Einzelteilbilder vom xentryAPI Mediaservice laden und als Set von {@link MediaServiceMediaObjectResponse}
     * zurückgeben.
     *
     * @param partListEntry
     * @param project
     * @return {@code null} falls der Aufruf nicht erfolgreich war
     */
    public static Set<MediaServiceMediaObjectResponse> getMediaObjectsFromMediaService(EtkDataPartListEntry partListEntry, final EtkProject project) throws CallWebserviceException {
        EtkDataPart dataPart = partListEntry.getPart();
        String matNr = dataPart.getAsId().getMatNr();
        String es1;
        String es2;
        boolean oldLogLoadFieldIfNeeded = dataPart.isLogLoadFieldIfNeeded();
        try {
            dataPart.setLogLoadFieldIfNeeded(false); // Felder M_AS_ES_1 und M_AS_ES_2 sind evtl. nicht mitgeladen worden am Material
            es1 = dataPart.getFieldValue(iPartsConst.FIELD_M_AS_ES_1);
            es2 = dataPart.getFieldValue(iPartsConst.FIELD_M_AS_ES_2);
        } finally {
            dataPart.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
        }
        // Falls ein ES1 und/oder ein ES2 Schlüssel existiert: M_BASE_MATNR als MatNr ergänzt um ES1/ES2
        if (StrUtils.isValid(es1) || StrUtils.isValid(es2)) {
            matNr = dataPart.getFieldValue(iPartsConst.FIELD_M_BASE_MATNR);
            if (StrUtils.isValid(es1)) {
                matNr += es1;
            }
            if (StrUtils.isValid(es2)) {
                matNr += es2;
            }
        }

        // Die BasisURL ist gleichzeitig die Webservice URL
        String mediaServiceMediaObjectsUrl = iPartsPlugin.getWebservicesSinglePicPartsBaseURI();
        mediaServiceMediaObjectsUrl = StrUtils.removeLastCharacterIfCharacterIs(mediaServiceMediaObjectsUrl, '/' + MediaServicePartNumbersService.WEBSERVICE_NAME);
        mediaServiceMediaObjectsUrl = StrUtils.addCharacterIfLastCharacterIsNot(mediaServiceMediaObjectsUrl, '/');
        mediaServiceMediaObjectsUrl += WEBSERVICE_NAME + "/" + EC.encodePath(matNr);

        String mediaObjectsJson = MediaServiceWebserviceUtils.getJsonFromPartNumbersWebservice(mediaServiceMediaObjectsUrl,
                                                                                               WEBSERVICE_NAME, project.getDBLanguage());
        if (mediaObjectsJson != null) {
            MediaServiceMediaObjectsResponse mediaObjectsResponse = genson.deserialize(mediaObjectsJson, MediaServiceMediaObjectsResponse.class);
            if ((mediaObjectsResponse == null) || (mediaObjectsResponse.getItems() == null)) {
                throw new CallWebserviceException(TranslationHandler.translate("!!Der JSON-String ist semantisch ungültig: %2",
                                                                               TranslationHandler.translate("!!Deserialisierung nicht möglich")));
            }

            return mediaObjectsResponse.getItems();
        }

        return null;
    }

    /**
     * Lädt das Einzelteilbild aus der übergebenen {@link MediaServiceMediaObjectResponse} herunter und gibt es als {@link FrameworkImage}
     * zurück.
     *
     * @param mediaObjectResponse
     * @return {@code null} falls das Einzelteilbild nicht gefunden werden konnte oder der Inhalt leer ist
     */
    public static FrameworkImage downloadMediaObject(MediaServiceMediaObjectResponse mediaObjectResponse) throws CallWebserviceException {
        String url = mediaObjectResponse.getUrl();
        if (StrUtils.isEmpty(url)) {
            return null;
        }

        String redirectUrl = mediaObjectResponse.getRedirectUrl();
        if (StrUtils.isEmpty(redirectUrl)) {
            // Bei der Original-Anfrage ohne Redirect zunächst im Cache nachsehen
            FrameworkImage image = mediaObjectsCache.get(mediaObjectResponse.getUrl());
            if (image != null) {
                return image;
            }
        } else {
            url = redirectUrl;
        }

        // URL absolut machen falls notwendig
        if (url.startsWith("/")) {
            // Basis-URL (Server) ermitteln über gemeinsamen Pfad von konfigurierter URL und URL vom Einzelteilbild (beginnt
            // laut Story mit /mediaservice)
            String mediaServiceBaseUrl = iPartsPlugin.getWebservicesSinglePicPartsBaseURI();
            String[] urlPaths = StrUtils.toStringArray(url.substring(1), "/", false, true); // hat mindestens Länge 1
            mediaServiceBaseUrl = StrUtils.stringUpToCharacter(mediaServiceBaseUrl, "/" + urlPaths[0]);
            url = mediaServiceBaseUrl + url;
        }

        final HttpClient client = new HttpClient(url);
        client.setParseCookiesEnabled(false);

        try {
            if (StrUtils.isEmpty(redirectUrl)) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.DEBUG, "Downloading media object of web service mediaservice/"
                                                                                     + WEBSERVICE_NAME + " with URL: " + url);
            }
            client.connect("");

            // Token erzeugen und setzen (hier wird auf einen zweiten Aufruf bei HTTP_STATUS_UNAUTHORIZED verzichtet, weil
            // kurz vorher das Token bereits für den Abruf der Einzelteilbilder-JSONs valide erzeugt wurde mit Fallback)
            UniversalConfiguration pluginConfig = iPartsPlugin.getPluginConfig();
            String tokenUrl = pluginConfig.getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_URL);
            String clientId = pluginConfig.getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_CLIENT_ID);
            String clientSecret = pluginConfig.getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_CLIENT_SECRET);
            String headerName = pluginConfig.getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_NAME);
            String tokenType = pluginConfig.getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_SINGLE_PIC_PARTS_TOKEN_TYPE);
            String token = BaseiPartsOAuthServiceUtils.retrieveAccessToken(TranslationHandler.getUiLanguage(), false, MediaServiceWebserviceUtils.accessTokenUtils,
                                                                           tokenUrl, clientId, clientSecret, "!!Einzelteilbilder-Webservice",
                                                                           iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS);

            String tokenHeaderString = tokenType + " " + token;
            client.setRequestProperty(headerName, tokenHeaderString);

            client.setRequestMethod(HttpConstants.METHOD_GET);

            // ResponseString bzw. ResponseMessage empfangen
            int httpResponseCode = client.getResponseCode();
            if (httpResponseCode == HttpConstants.HTTP_STATUS_OK) {
                byte[] responseBytes = Utils.readByteArray(client.getInputStream());
                if ((responseBytes != null) && (responseBytes.length > 0)) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_SINGLE_PIC_PARTS, LogType.DEBUG, "Download finished with " + responseBytes.length
                                                                                         + " bytes for media object of web service mediaservice/"
                                                                                         + WEBSERVICE_NAME + " with URL: " + url);
                    FrameworkImage image = FrameworkImage.getFromByteArray(responseBytes);
                    mediaObjectsCache.put(mediaObjectResponse.getUrl(), image);
                    return image;
                } else {
                    httpResponseCode = HttpConstants.HTTP_STATUS_NOT_FOUND;
                }
            }

            // Ab hier nur noch Fehlerbehandlung und Redirect
            if (httpResponseCode == HttpConstants.HTTP_STATUS_NOT_FOUND) {
                return null;
            } else {
                String message;
                if (httpResponseCode == HttpConstants.HTTP_STATUS_TEMPORARY_REDIRECT) {
                    // Einzelteilbilder haben zunächst immer einen Redirect mit neuer Location
                    String redirectLocation = client.getHeader(HttpConstants.HEADER_FIELD_LOCATION);
                    if (StrUtils.isValid(redirectLocation)) {
                        mediaObjectResponse.setRedirectUrl(redirectLocation);
                        return downloadMediaObject(mediaObjectResponse);
                    } else {
                        message = TranslationHandler.translate("!!HTTP Redirect ohne Location für das Einzelteilbild mit der URL \"%1\"", url);
                    }
                } else if (httpResponseCode == HttpConstants.HTTP_STATUS_UNAUTHORIZED) {
                    message = TranslationHandler.translate("!!Authorisierungsfehler");
                } else if (httpResponseCode == HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT) {
                    message = TranslationHandler.translate("!!Timeout");
                } else {
                    String responseMessage = client.getResponseMessage();
                    message = TranslationHandler.translate("!!Fehlerantwort beim Download vom Einzelteilbild mit der URL \"%1\": %2", url,
                                                           httpResponseCode + (StrUtils.isValid(responseMessage)
                                                                               ? " - " + responseMessage : ""));
                }

                CallWebserviceException callMediaServiceException = new CallWebserviceException(message);
                callMediaServiceException.setHttpResponseCode(httpResponseCode);
                throw callMediaServiceException;
            }
        } catch (IOException e) {
            CallWebserviceException callMediaServiceException = new CallWebserviceException(TranslationHandler.translate("!!Fehler/Timeout beim Download vom Einzelteilbild"), e);
            callMediaServiceException.setHttpResponseCode(HttpConstants.HTTP_STATUS_INTERNAL_SERVER_ERROR);
            throw callMediaServiceException;
        } finally {
            client.disconnect();
        }
    }
}
