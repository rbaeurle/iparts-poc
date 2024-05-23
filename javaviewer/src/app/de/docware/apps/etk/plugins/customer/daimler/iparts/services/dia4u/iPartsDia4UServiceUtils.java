package de.docware.apps.etk.plugins.customer.daimler.iparts.services.dia4u;

import com.owlike.genson.Genson;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.BaseiPartsOAuthServiceUtils;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.dia4u.partUsages.iPartsDia4UPartUsagesGetLongCodeRuleObjectsResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.dia4u.partUsages.iPartsDia4UPartUsagesGetLongCodeRuleResponse;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.webservice.restful.CallWebserviceException;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenUtils;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;

import java.util.Set;

/**
 * Service-Klasse für alle Webservice-Aufrufe vom Dia4U-Service
 */
public class iPartsDia4UServiceUtils {

    private static final OAuthAccessTokenUtils accessTokenUtils = new OAuthAccessTokenUtils("Dia4U", iPartsPlugin.LOG_CHANNEL_DIA_4_U);

    public static void clearCache() {
        accessTokenUtils.clearAccessToken();
    }

    /**
     * Zusammenbauen des Service-Aufrufs zur Abfrage der Lange Code Regeln
     *
     * @param matNr
     * @param series
     * @param aa
     * @param language
     * @return
     * @throws CallWebserviceException
     */
    public static Set<iPartsDia4UPartUsagesGetLongCodeRuleResponse> getLongCodeRuleObjectFromDia4UService(String matNr,
                                                                                                          String series, String aa, String language)
            throws CallWebserviceException {

        String dia4UServiceUrl = buildDia4UServiceURL(matNr, series, aa);
        if (StrUtils.isValid(dia4UServiceUrl)) {
            String longCodeRuleObjectsJson = getJsonFromDia4UService(dia4UServiceUrl, language);
            if (StrUtils.isValid(longCodeRuleObjectsJson)) {
                Genson genson = JSONUtils.createGenson(true);
                iPartsDia4UPartUsagesGetLongCodeRuleObjectsResponse longCodeRuleObjectsResponse =
                        genson.deserialize(longCodeRuleObjectsJson, iPartsDia4UPartUsagesGetLongCodeRuleObjectsResponse.class);
                if ((longCodeRuleObjectsResponse == null) || (longCodeRuleObjectsResponse.getContent() == null)) {
                    throw new CallWebserviceException(TranslationHandler.translate("!!Der JSON-String ist semantisch ungültig: Deserialisierung nicht möglich"));
                }
                return longCodeRuleObjectsResponse.getContent();
            }
        } else {
            throw new CallWebserviceException(TranslationHandler.translate("!!Die Basis-URI für Dia4U ist ungültig"));
        }
        return null;
    }

    private static String buildDia4UServiceURL(String matNr, String series, String aa) {
        String dia4UServiceUrl = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DIA_4_U_BASE_URI);
        dia4UServiceUrl = dia4UServiceUrl.trim();
        if (StrUtils.isEmpty(dia4UServiceUrl)) {
            return "";
        }
        dia4UServiceUrl = StrUtils.addCharacterIfLastCharacterIsNot(dia4UServiceUrl, '/');
        StringBuilder str = new StringBuilder(dia4UServiceUrl);
        str.append("partusages/v2/DIA4U/Parts/");
        str.append(matNr);
        str.append("/usages?bodyType=");
        str.append(aa);
        str.append("&showLongCodeRule=true&allStates=true&modelSeries=");
        str.append(series);
        return str.toString();
    }

    /**
     * Gibt nach erfolgreicher Webservice Anfrage eine Response im JSON Format zurück.
     *
     * @param language
     * @return
     */
    public static String getJsonFromDia4UService(String dia4UServiceUrl, String language) throws CallWebserviceException {
        String tokenUrl = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DIA_4_U_TOKEN_URL);
        String clientId = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DIA_4_U_TOKEN_CLIENT_ID);
        String clientSecret = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DIA_4_U_TOKEN_CLIENT_SECRET);
        String token = BaseiPartsOAuthServiceUtils.retrieveAccessToken(language, false, accessTokenUtils, tokenUrl, clientId,
                                                                       clientSecret, "!!Dia4U-Service",
                                                                       iPartsPlugin.LOG_CHANNEL_DIA_4_U);


        try {
            return createClientAndExecuteRequest(dia4UServiceUrl, token, language);
        } catch (CallWebserviceException callWebserviceException) {
            // Wenn der Response-Code 401 Unauthorized ist, dann soll der Access Token neu generiert und EINMALIG (!)
            // (deshalb nicht rekursiv) der Request neu ausgeführt werden.
            if (callWebserviceException.getHttpResponseCode() == HttpConstants.HTTP_STATUS_UNAUTHORIZED) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DIA_4_U, LogType.INFO, "Received HTTP error code 401 (unauthorized). Forcing access token generation.");
                token = BaseiPartsOAuthServiceUtils.retrieveAccessToken(language, true, accessTokenUtils, tokenUrl, clientId, clientSecret,
                                                                        "!!Dia4U-Service", iPartsPlugin.LOG_CHANNEL_DIA_4_U);
                return createClientAndExecuteRequest(dia4UServiceUrl, token, language);
            } else {
                throw callWebserviceException;
            }
        }
    }

    private static String createClientAndExecuteRequest(String serviceUrl, String token, String language) throws CallWebserviceException {
        String headerName = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DIA_4_U_TOKEN_NAME);
        String tokenType = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DIA_4_U_TOKEN_TYPE);
        return BaseiPartsOAuthServiceUtils.createClientAndExecuteRequest(serviceUrl, "dia4U", token, language, headerName,
                                                                         tokenType, "", false, iPartsPlugin.LOG_CHANNEL_DIA_4_U,
                                                                         "!!Dia4U-Service");
    }
}
