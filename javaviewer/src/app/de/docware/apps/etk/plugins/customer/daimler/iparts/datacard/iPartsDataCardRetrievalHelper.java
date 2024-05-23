/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import com.owlike.genson.JsonBindingException;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.FinId;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.http.client.HttpClient;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.logger.TimeSpanLogger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.misc.BrowserInfo;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenContainer;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenException;
import de.docware.framework.modules.webservice.restful.oauth.OAuthAccessTokenUtils;
import de.docware.framework.utils.JSONUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFileCoding;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.SetParams;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Hilfsklasse, um über Webservices Datenkarten abzufragen
 */
public class iPartsDataCardRetrievalHelper {

    public enum DatacardType {
        VEHICLE,
        AGGREGATE,
        FIXING_PARTS
    }

    private static String REDIS_DATACARD_PREFIX = "datacard";

    private static final Charset CHARSET_UTF8 = Charset.forName(DWFileCoding.UTF8.getJavaCharsetName());

    private static final String SPRINGLEG_FRONT = "00064"; // ZB-Federbein vorn
    private static final String SPRINGLEG_REAR = "00065"; // Feder hinten
    private static final String SPRINGSHIM_REAR = "00066"; // Federbeilage hinten

    private static final String VPD_IDENT_FUEL_CELL = "10193"; // Brennstoffzelle aus VPD Daten

    private static final OAuthAccessTokenUtils accessTokenUtils = new OAuthAccessTokenUtils("VIS v3", iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE);

    private static ObjectInstanceLRUList<Object, AbstractDataCard> dataCards;
    private static Integer cacheLifeTime;
    private static Genson genson = JSONUtils.createGenson(true, false, true);

    // Teilpfad der jeweiligen Version der Datenkarten-Webservices
    public static final String VIS_URI_V2 = "/v2";
    public static final String VIS_URI_V3 = "/v3";

    // Parameter der gesetzt wird, wenn "Aftersalebenennungen anfordern" gesetzt bzw. nicht gesetzt ist
    public static final String VIS_DONT_USE_RETAIL_NAME = "";
    public static final String VIS_USE_RETAIL_NAME = "?force-aftersaledesignation=false";

    public static synchronized void clearCache() {
        dataCards = null;
        cacheLifeTime = null;
        accessTokenUtils.clearAccessToken();
    }

    private static int getCacheLifeTime() {
        Integer cacheLifeTimeLocal = cacheLifeTime;
        if (cacheLifeTimeLocal == null) {
            cacheLifeTimeLocal = iPartsPlugin.getPluginConfig().getConfigValueAsInteger(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_CACHE_LIFE_TIME);
            cacheLifeTime = cacheLifeTimeLocal;
        }
        return cacheLifeTimeLocal;
    }

    public static boolean isDataCardWebservicesV3() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_USE_V3);
    }

    public static boolean isUseRetailNameForDataCardWebservices() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_USE_RETAIL_NAME);
    }

    public static String getDataCardWebservicesVersionURI() {
        if (isDataCardWebservicesV3()) {
            return VIS_URI_V3;
        } else {
            return VIS_URI_V2;
        }
    }

    public static String getUseRetailNameForDataCardWebservicesBasisURIAddon() {
        if (isUseRetailNameForDataCardWebservices()) {
            return VIS_USE_RETAIL_NAME;
        } else {
            return VIS_DONT_USE_RETAIL_NAME;
        }
    }

    /**
     * Liefert die Fahrzeug-Datenkarte zur übergebenen FIN/VIN zurück, wobei diese aus dem Cache kommt
     *
     * @param project
     * @param finOrVin
     * @return
     */
    public static VehicleDataCard getCachedVehicleDataCard(EtkProject project, FinId finOrVin) {
        // Fahrzeug-Datenkarte im Cache suchen
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataCardRetrievalHelper.class, finOrVin.getFIN());
        AbstractDataCard dataCard = getCachedDataCard(hashObject, project);
        if (dataCard instanceof VehicleDataCard) {
            return (VehicleDataCard)dataCard;
        }
        return null;
    }

    /**
     * Liefert die Aggregate-Datenkarte zum übergebenen Aggregatetyp und Ident zurück, wobei diese aus dem Cache kommt
     *
     * @param project
     * @param type
     * @param ident
     * @return
     */
    public static AggregateDataCard getCachedAggregateDataCard(EtkProject project, DCAggregateTypes type, String ident) {
        // Aggregate-Datenkarte im Cache suchen
        final String request = handleJsonName(type) + "/" + ident;
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataCardRetrievalHelper.class, request);
        AbstractDataCard dataCard = getCachedDataCard(hashObject, project);
        if (dataCard instanceof AggregateDataCard) {
            return (AggregateDataCard)dataCard;
        }
        return null;
    }

    /**
     * Liefert die Fahrzeug-Datenkarte zur übergebenen FIN/VIN zurück, wobei diese entweder aus dem Cache kommt oder per
     * Webservice geladen wird.
     *
     * @param project
     * @param finOrVin
     * @param loadDataCardCallback Optionaler Callback beim Laden der Datenkarte
     * @param loadFixingParts      Bestimmt ob Befestigungsteile mit der Datenkarte initial geladen werden sollen
     * @return
     * @throws DataCardRetrievalException
     */
    public static VehicleDataCard getVehicleDataCard(final EtkProject project, final FinId finOrVin,
                                                     LoadDataCardCallback loadDataCardCallback, final boolean loadFixingParts) throws DataCardRetrievalException {
        // Fahrzeug-Datenkarte im Cache suchen
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataCardRetrievalHelper.class, finOrVin.getFIN());
        final VarParam<VehicleDataCard> result = new VarParam<>(null);
        AbstractDataCard dataCard = getCachedDataCard(hashObject, project);
        if (dataCard instanceof VehicleDataCard) {
            result.setValue((VehicleDataCard)dataCard);
        }

        if (result.getValue() == null) {
            // Fahrzeug-Datenkarte noch nicht vorhanden -> lade über den Webservice
            final VarParam<DataCardRetrievalException> dataCardRetrievalException = new VarParam<>(null);
            Runnable loadDataCardRunnable = new Runnable() {
                @Override
                public void run() {
                    String baseURI = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_BASE_URI);
                    try {
                        String datacardJson = getJsonFromWebservice(baseURI, finOrVin.getFIN(), project.getDBLanguage(), DatacardType.VEHICLE);
                        if (datacardJson != null) {
                            iPartsWSvehicleInclMasterData responseAsVehicleDatacardJSONObject = getResponseAsVehicleDatacardJSONObject(datacardJson, finOrVin);
                            if (responseAsVehicleDatacardJSONObject != null) {
                                VehicleDataCard vehicleDataCard = new VehicleDataCard();
                                vehicleDataCard.loadFromJSONObject(responseAsVehicleDatacardJSONObject, project, loadFixingParts);

                                // Falls der Lade-Thread abgebrochen wurde, das Ergebnis nicht setzen
                                if (!Session.currentSessionThreadCancelled()) {
                                    result.setValue(vehicleDataCard);
                                }
                            }
                        }
                    } catch (DataCardRetrievalException e) {
                        dataCardRetrievalException.setValue(e);
                    }
                }
            };

            if (loadDataCardCallback != null) {
                loadDataCardCallback.loadDataCard(loadDataCardRunnable);
            } else {
                loadDataCardRunnable.run();
            }

            if (dataCardRetrievalException.getValue() != null) {
                throw dataCardRetrievalException.getValue();
            }

            VehicleDataCard vehicleDataCard = result.getValue();
            addCachedDataCard(hashObject, vehicleDataCard);

            // Wurde mit einer VIN anstatt FIN gesucht, die Datenkarte auch unter der FIN im Cache ablegen
            if (vehicleDataCard != null) {
                FinId finId = vehicleDataCard.getFinId();
                if ((finId != null) && !finId.getFIN().isEmpty() && !finId.getFIN().equals(finOrVin.getFIN())) {
                    hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataCardRetrievalHelper.class, finId.getFIN());
                    addCachedDataCard(hashObject, vehicleDataCard);
                }
            }
        }

        return result.getValue();
    }

    public static ObjectInstanceLRUList<Object, AbstractDataCard> getCachedDataCards() {
        ObjectInstanceLRUList<Object, AbstractDataCard> dataCardsLocal = dataCards;
        if (dataCardsLocal == null) {
            synchronized (iPartsDataCardRetrievalHelper.class) {
                dataCardsLocal = dataCards;
                if (dataCardsLocal == null) {
                    int cacheLifeTimeLocal = getCacheLifeTime();
                    if (cacheLifeTimeLocal != 0) {
                        dataCardsLocal = new ObjectInstanceLRUList<>(iPartsConst.MAX_CACHE_SIZE_DATACARDS, cacheLifeTimeLocal, true);
                    }
                    dataCards = dataCardsLocal;
                }
            }
        }
        return dataCardsLocal;
    }

    public static AbstractDataCard getCachedDataCard(Object key, EtkProject project) {
        int cacheLifeTimeLocal = getCacheLifeTime();
        if (cacheLifeTimeLocal == 0) {
            return null;
        }

        JedisPooled redisPool = iPartsPlugin.getRedisPool();
        if (redisPool != null) { // Datenkarte aus dem Redis-Cache holen
            try {
                String dataCardJSON = redisPool.get(CacheHelper.getRedisCacheKey(REDIS_DATACARD_PREFIX, key));
                if (StrUtils.isValid(dataCardJSON)) {
                    AbstractDataCard dataCard = genson.deserialize(dataCardJSON, AbstractDataCard.class);
                    if (dataCard instanceof VehicleDataCard) {
                        ((VehicleDataCard)dataCard).setParentDatacardInAllActiveAggregates(project);
                    }
                    return dataCard;
                }
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
            }
            return null;
        } else { // Datenkarte aus dem lokalen Cache holen
            ObjectInstanceLRUList<Object, AbstractDataCard> dataCardsLocal = getCachedDataCards();
            if (dataCardsLocal != null) {
                return dataCardsLocal.get(key);
            } else {
                return null;
            }
        }
    }

    public static void addCachedDataCard(Object key, AbstractDataCard dataCard) {
        int cacheLifeTimeLocal = getCacheLifeTime();
        if (cacheLifeTimeLocal == 0) {
            return;
        }

        JedisPooled redisPool = iPartsPlugin.getRedisPool();
        if (redisPool != null) { // Datenkarte im Redis-Cache ablegen
            // Lebensdauer der Datenkarten im Redis-Cache ermitteln
            int redisCacheLifeTime = iPartsPlugin.getRedisCacheLifeTime();
            if (cacheLifeTimeLocal > 0) {
                redisCacheLifeTime = Math.min(redisCacheLifeTime, cacheLifeTimeLocal);
            }
            SetParams setParams = new SetParams();
            setParams.ex(redisCacheLifeTime);

            redisPool.set(CacheHelper.getRedisCacheKey(REDIS_DATACARD_PREFIX, key), genson.serialize(dataCard), setParams);
        } else { // Datenkarte im lokalen Cache ablegen
            ObjectInstanceLRUList<Object, AbstractDataCard> dataCardsLocal = getCachedDataCards();
            if (dataCardsLocal != null) {
                dataCardsLocal.put(key, dataCard);
            }
        }
    }

    /**
     * Statt des JsonName für TRANSFER_CASE soll der JsonName für TRANSMISSION verwendet werden.
     * In allen anderen Fällen wird der "normale" JsonName des eigenen Typs verwendet.
     *
     * @param type
     * @return
     */
    private static String handleJsonName(final DCAggregateTypes type) {
        switch (type) {
            case TRANSFER_CASE:
                return DCAggregateTypes.TRANSMISSION.getJsonName();
            default:
                return type.getJsonName();
        }
    }

    /**
     * Liefert die Aggregate-Datenkarte zum übergebenen Aggregatetyp und Ident zurück, wobei diese entweder aus dem Cache kommt oder per
     * Webservice geladen wird.
     *
     * @param project
     * @param type
     * @param ident
     * @param loadDataCardCallback Optionaler Callback beim Laden der Datenkarte
     * @return
     * @throws DataCardRetrievalException
     */
    public static AggregateDataCard getAggregateDataCard(final EtkProject project, final DCAggregateTypes type, final String ident,
                                                         LoadDataCardCallback loadDataCardCallback) throws DataCardRetrievalException {
        // Aggregate-Datenkarte im Cache suchen
        final String request = handleJsonName(type) + "/" + ident;
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsDataCardRetrievalHelper.class, request);
        final VarParam<AggregateDataCard> result = new VarParam<>(null);
        AbstractDataCard dataCard = getCachedDataCard(hashObject, project);
        if (dataCard instanceof AggregateDataCard) {
            result.setValue((AggregateDataCard)dataCard);
        }

        if (result.getValue() == null) {
            // Aggregate-Datenkarte noch nicht vorhanden -> lade über den Webservice
            final VarParam<DataCardRetrievalException> dataCardRetrievalException = new VarParam<>(null);
            Runnable loadDataCardRunnable = new Runnable() {
                @Override
                public void run() {
                    String baseURI = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_BASE_URI);
                    FinId sourceId = new FinId();

                    try {
                        String datacardJson = null;
                        AggregateDataCard aggregateDataCard = null;
                        // Für Lenkungen existieren keine Datenkarten ==> den VIS-Aufruf unterdrücken und eine Meldung ausgeben.
                        if (type.equals(DCAggregateTypes.STEERING)) {
                            dataCardRetrievalException.setValue(new DataCardRetrievalException(TranslationHandler.
                                                                                                       translate("!!Es existieren keine Datenkarten für den Aggregatetyp \"%1\".",
                                                                                                                 TranslationHandler.translate(type.getDescription())) + "\n" + TranslationHandler.translate("!!Das Laden wird unterdrückt.")));
                        } else {
                            // Hier hinter versteckt sich u.a. der VIS-Aufruf:
                            datacardJson = getJsonFromWebservice(baseURI, request, project.getDBLanguage(), DatacardType.AGGREGATE);
                        }
                        if (datacardJson != null) {
                            iPartsWSactiveGeneralMajAssy jsonResponse = getResponseAsAggregateDatacardJSONObject(datacardJson, type, ident);
                            if (jsonResponse != null) {
                                aggregateDataCard = new AggregateDataCard();
                                // alle möglichen Typen durchgehen und den ersten nehmen der nicht null ist
                                // Eigentlich darf nur einer der Untertypen gültig sein
                                switch (type) {
                                    case ENGINE:
                                        aggregateDataCard.loadFromJSONObject(DCAggregateTypes.ENGINE, jsonResponse.getEngine(), project, sourceId);
                                        break;
                                    case TRANSFER_CASE: // Statt transferCase wird der Datenkartentyp Transmission vom VIS geliefert/geholt.
                                    case TRANSMISSION:
                                        aggregateDataCard.loadFromJSONObject(DCAggregateTypes.TRANSMISSION, jsonResponse.getTransmission(), project, sourceId);
                                        break;
                                    case AFTER_TREATMENT_SYSTEM:
                                        aggregateDataCard.loadFromJSONObject(DCAggregateTypes.AFTER_TREATMENT_SYSTEM, jsonResponse.getAfterTreatmentSystem(), project, sourceId);
                                        break;
                                    case CAB:
                                        aggregateDataCard.loadFromJSONObject(DCAggregateTypes.CAB, jsonResponse.getCab(), project, sourceId);
                                        break;
                                    case ELECTRO_ENGINE:
                                        aggregateDataCard.loadFromJSONObject(DCAggregateTypes.ELECTRO_ENGINE, jsonResponse.getElectroEngine(), project, sourceId);
                                        break;
                                    case FUEL_CELL:
                                        aggregateDataCard.loadFromJSONObject(DCAggregateTypes.FUEL_CELL, jsonResponse.getFuelCell(), project, sourceId);
                                        break;
                                    case HIGH_VOLTAGE_BATTERY:
                                        aggregateDataCard.loadFromJSONObject(DCAggregateTypes.HIGH_VOLTAGE_BATTERY, jsonResponse.getHighVoltageBattery(), project, sourceId);
                                        break;
                                    case AXLE:
                                        aggregateDataCard.loadFromJSONObject(DCAggregateTypes.AXLE, jsonResponse.getAxle(), project, sourceId);
                                        break;
                                    case STEERING: // DAIMLER-11111, Datenkarten für den Aggregatetyp "STEERING" gibt es nicht  ==> in den default-Exception-Fall 'reinlaufen!
                                    default:
                                        aggregateDataCard = null;
                                        dataCardRetrievalException.setValue(new DataCardRetrievalException(TranslationHandler.
                                                                                                                   translate("!!Ungültiger Aggregatetyp \"%1\" für das Laden einer Aggregate-Datenkarte.",
                                                                                                                             TranslationHandler.translate(type.getDescription()))));
                                }

                                if ((aggregateDataCard != null) && !aggregateDataCard.isDataCardLoaded()) {
                                    aggregateDataCard = null;
                                    dataCardRetrievalException.setValue(new DataCardRetrievalException(TranslationHandler.
                                                                                                               translate("!!Aggregate-Datenkarte enthält keine gültigen Daten für den Aggregatetyp \"%1\".",
                                                                                                                         TranslationHandler.translate(type.getDescription()))));

                                }

                                // Falls der Lade-Thread abgebrochen wurde, das Ergebnis nicht setzen
                                if (!Session.currentSessionThreadCancelled()) {
                                    result.setValue(aggregateDataCard);
                                }
                            }
                        }
                    } catch (DataCardRetrievalException e) {
                        dataCardRetrievalException.setValue(e);
                    }
                }
            };

            if (loadDataCardCallback != null) {
                loadDataCardCallback.loadDataCard(loadDataCardRunnable);
            } else {
                loadDataCardRunnable.run();
            }

            if (dataCardRetrievalException.getValue() != null) {
                throw dataCardRetrievalException.getValue();
            }

            addCachedDataCard(hashObject, result.getValue());
        }

        return result.getValue();
    }

    public static String getJsonFromWebservice(String baseURI, String requestParameter, String language, DatacardType datacardType) throws DataCardRetrievalException {
        // Basis-Uri muss um die Version des verwendeten Webservices (VIS v2 oder v3) erweitert werden
        String dataCardServiceURL = StrUtils.removeLastCharacterIfCharacterIs(baseURI, '/');
        dataCardServiceURL += iPartsDataCardRetrievalHelper.getDataCardWebservicesVersionURI();
        String basisURIAddon = "";
        switch (datacardType) {
            case VEHICLE:
                dataCardServiceURL += iPartsPlugin.WEBSERVICE_URI_VEHICLE_DATACARDS;
                basisURIAddon = getUseRetailNameForDataCardWebservicesBasisURIAddon();
                break;
            case AGGREGATE:
                dataCardServiceURL += iPartsPlugin.WEBSERVICE_URI_AGGREGATE_DATACARDS;
                break;
            case FIXING_PARTS:
                dataCardServiceURL += iPartsPlugin.WEBSERVICE_URI_FIXING_PARTS;
                break;
            default:
                break;
        }
        String token;
        if (!isDataCardWebservicesV3()) {
            // Config auslesen, und ggf. Token als Request Header mitschicken
            token = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_TOKEN).trim();
        } else {
            token = retrieveV3AccessToken(language, false);
        }
        if (!Utils.isURL(dataCardServiceURL)) {
            // relative URL muss mit / beginnen
            if (!dataCardServiceURL.startsWith("/")) {
                dataCardServiceURL = "/" + dataCardServiceURL;
            }

            // wenn es keine absolute URL war (beginnt mit http o.ä.) dann nehmen wir an, es ist eine relative
            BrowserInfo browserInfo = BrowserInfo.get();
            if (browserInfo != null) { // Aufruf aus der GUI
                dataCardServiceURL = StrUtils.removeLastCharacterIfCharacterIs(browserInfo.getOfficialUrl(), '/') + dataCardServiceURL;
            } else if (!Session.get().canHandleGui()) { // Aufruf erfolgt aus einem Webservice heraus -> IAC Client URL als Fallback nehmen
                // Die IAC Client URL ist die URL für die Anwendung -> "/app" muss hinten abgeschnitten werden
                String clientAppURL = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_INTER_APP_COM_CLIENT_URL);
                dataCardServiceURL = StrUtils.removeLastCharacterIfCharacterIs(clientAppURL, "/app") + dataCardServiceURL;
            } else {
                throw new DataCardRetrievalException(TranslationHandler.translate("!!Fehler beim Abfragen der Applikations URL des Browsers"));
            }
        }
        try {
            return createClientAndExecuteRequest(dataCardServiceURL, requestParameter, basisURIAddon, datacardType, token, language);
        } catch (DataCardRetrievalException dataCardRetrievalException) {
            // Wenn der Response-Code 401 Unauthorized ist, dann soll beim VIS V3 der Access Token neu generiert und EINMALIG (!)
            // (deshalb nicht rekursiv) der Request neu ausgeführt werden.
            if ((dataCardRetrievalException.getHttpResponseCode() == HttpConstants.HTTP_STATUS_UNAUTHORIZED) && isDataCardWebservicesV3()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.INFO, "Received HTTP error code 401 (unauthorized) for VIS V3 request. Forcing access token generation.");
                token = retrieveV3AccessToken(language, true);
                return createClientAndExecuteRequest(dataCardServiceURL, requestParameter, basisURIAddon, datacardType, token, language);
            } else {
                throw dataCardRetrievalException;
            }
        }
    }

    private static String createClientAndExecuteRequest(String dataCardServiceURL, String requestParameter, String basisURIAddon,
                                                        DatacardType datacardType, String token, String language) throws DataCardRetrievalException {
        final HttpClient client = new HttpClient(dataCardServiceURL);
        client.setParseCookiesEnabled(false);

        // Timeout setzen
        int timeout = iPartsPlugin.getPluginConfig().getConfigValueAsInteger(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_TIMEOUT);
        client.setConnectTimeoutMs(timeout * 1000);

        try {
            // Verbindung aufbauen
            String basisURI = "/" + requestParameter + basisURIAddon;
            client.connect(basisURI, 0);
            if (datacardType == DatacardType.VEHICLE) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.DEBUG, "Admin configuration option \"Use after-sales naming\" is "
                                                                                     + isUseRetailNameForDataCardWebservices()
                                                                                     + " which results in the URI \"" + client.getUrlAsString() + "\"");
            }

            if (StrUtils.isValid(token)) { // Nur wenn ein Token gesetzt ist die weiteren Werte aus der Config auslesen
                String headerName = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_TOKEN_NAME);
                String tokenType = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_TOKEN_TYPE);
                String tokenHeaderString = tokenType + " " + token;
                Logger.log(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.DEBUG, "Using token for VIS call: " + headerName
                                                                                     + "=" + tokenHeaderString);
                client.setRequestProperty(headerName, tokenHeaderString);
            }

            client.setRequestProperty(HttpConstants.HEADER_FIELD_ACCEPT, MimeTypes.MIME_TYPE_JSON);
            client.setRequestProperty(HttpConstants.HEADER_FIELD_ACCEPT_LANGUAGE, language.toUpperCase());
            client.setRequestMethod(HttpConstants.METHOD_GET);

            // ResponseString bzw. ResponseMessage empfangen
            int httpResponseCode = client.getResponseCode();
            if (httpResponseCode == HttpConstants.HTTP_STATUS_OK) {
                byte[] responseBytes = Utils.readByteArray(client.getInputStream());
                if (responseBytes != null) {
                    return new String(responseBytes, CHARSET_UTF8);
                } else {
                    httpResponseCode = HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT; // InputStream wurde bereits geschlossen
                }
            }

            // Ab hier nur noch Fehlerbehandlung
            if (httpResponseCode == HttpConstants.HTTP_STATUS_NOT_FOUND) {
                // Wird eine Datenkarte nicht gefunden, soll einfach nur null zurückgegeben werden
                return null;
            } else {
                String message;
                if (httpResponseCode == HttpConstants.HTTP_STATUS_UNAUTHORIZED) {
                    message = TranslationHandler.translate("!!Authorisierungsfehler beim Abrufen der Datenkarte für den Anfrageparameter \"%1\".",
                                                           requestParameter);
                } else if (httpResponseCode == HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT) {
                    message = TranslationHandler.translate("!!Timeout beim Abrufen der Datenkarte für den Anfrageparameter \"%1\".",
                                                           requestParameter);
                } else {
                    message = TranslationHandler.translate("!!Fehlerantwort vom Webservice \"%1\": %2", dataCardServiceURL
                                                                                                        + "/" + requestParameter,
                                                           httpResponseCode + " - " + client.getResponseMessage());
                }

                DataCardRetrievalException dataCardRetrievalException = new DataCardRetrievalException(message);
                dataCardRetrievalException.setHttpResponseCode(httpResponseCode);
                throw dataCardRetrievalException;
            }
        } catch (IOException e) {
            DataCardRetrievalException dataCardRetrievalException =
                    new DataCardRetrievalException(TranslationHandler.translate("!!Fehler/Timeout beim Abrufen der Datenkarte für den Anfrageparameter \"%1\".",
                                                                                requestParameter), e);

            // HTTP_STATUS_REQUEST_TIMEOUT ist hier der am besten passendste Fehlercode zumal sowieso nichts anderes übrigbleibt
            // als ein Fallback auf eine Baumuster-Datenkarte
            dataCardRetrievalException.setHttpResponseCode(HttpConstants.HTTP_STATUS_REQUEST_TIMEOUT);
            throw dataCardRetrievalException;
        } finally {
            client.disconnect();
        }
    }

    private static String retrieveV3AccessToken(String language, boolean forceGenerateAccessToken) throws DataCardRetrievalException {
        String token = null;
        // Prüfen ob der AccessToken gültig ist und ob zwingend ein neuer Token generiert werden soll, dann generieren
        if (!forceGenerateAccessToken && accessTokenUtils.isAccessTokenValid()) {
            token = accessTokenUtils.getAccessToken().getAccessToken();
        } else {
            OAuthAccessTokenContainer accessToken = generateAccessToken(language);
            if (accessToken != null) {
                token = accessToken.getAccessToken();
            }
            if (StrUtils.isEmpty(token)) {
                throw new DataCardRetrievalException(TranslationHandler.translate("!!Access-Token für den Vis v3 konnte nicht erzeugt werden"));
            }
        }
        return token;
    }

    private static OAuthAccessTokenContainer generateAccessToken(String language) throws DataCardRetrievalException {
        OAuthAccessTokenContainer accessToken;
        String tokenUrl = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_TOKEN_URL);
        String clientId = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_TOKEN_CLIENT_ID);
        String clientSecret = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_TOKEN_CLIENT_SECRET);
        try {
            accessToken = accessTokenUtils.generateAccessToken(tokenUrl, clientId, clientSecret, language);
            Logger.log(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.DEBUG, "New access token for VIS call has been generated: " + accessToken.getAccessToken());
        } catch (OAuthAccessTokenException e) {
            throw new DataCardRetrievalException(TranslationHandler.translate("!!Access-Token für den Vis v3 konnte nicht erzeugt werden"), e);
        }
        return accessToken;
    }

    public static iPartsWSvehicle getResponseAsFixingPartsJSONObject(String jsonAsString, FinId fin) throws DataCardRetrievalException {
        if (!StrUtils.isEmpty(jsonAsString)) {
            GensonBuilder gensonBuilder = new GensonBuilder();
            Genson genson = gensonBuilder.create();
            try {
                iPartsWSgetFixingPartsResponse datacardResponse = genson.deserialize(jsonAsString, iPartsWSgetFixingPartsResponse.class);
                if (datacardResponse == null) {
                    throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Befestigungsteile zur FIN/VIN \"%1\" ist semantisch ungültig: %2",
                                                                                      fin.getFIN(), TranslationHandler.translate("!!Deserialisierung als Befestigungsteile nicht möglich")));
                }
                iPartsWSvehicleWithFixingParts vehicleWithFixingParts = datacardResponse.getVehicleWithFixingParts();
                if (vehicleWithFixingParts != null) {
                    // Evtl. vorhandene Exception vom Befestigungsteile-Webservice auswerten
                    iPartsWSDataCardException dataCardException = vehicleWithFixingParts.getException();
                    if ((dataCardException != null) && !StrUtils.isEmpty(dataCardException.getId())) { // ID muss vorhanden sein bei einer Exception
                        throw new DataCardRetrievalException(TranslationHandler.translate("!!Fehlermeldung vom Befestigungsteile-Webservice für FIN/VIN \"%1\": %2 - %3",
                                                                                          fin.getFIN(), dataCardException.getId(),
                                                                                          vehicleWithFixingParts.getException().getDescription()));
                    }

                    iPartsWSvehicle vehicle = vehicleWithFixingParts.getVehicle();
                    if (vehicle != null) {
                        return vehicle;
                    } else {
                        throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Befestigungsteile-Datenkarte %1 ist semantisch ungültig: %2",
                                                                                          fin.getFIN(), TranslationHandler.translate("!!\"iPartsWSvehicle\" fehlt")));
                    }
                } else {
                    throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Befestigungsteile-Datenkarte %1 ist semantisch ungültig: %2",
                                                                                      fin.getFIN(), TranslationHandler.translate("!!\"iPartsWSvehicleWithFixingParts\" fehlt")));
                }
            } catch (JsonBindingException exception) {
                throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Befestigungsteile zur FIN/VIN \"%1\" ist syntaktisch fehlerhaft.",
                                                                                  fin.getFIN()), exception);
            }
        } else {
            throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Befestigungsteile zur FIN/VIN \"%1\" ist leer.",
                                                                              fin.getFIN()));
        }
    }

    public static iPartsWSvehicleInclMasterData getResponseAsVehicleDatacardJSONObject(String jsonAsString, FinId fin) throws DataCardRetrievalException {
        if (!StrUtils.isEmpty(jsonAsString)) {
            GensonBuilder gensonBuilder = new GensonBuilder();
            Genson genson = gensonBuilder.create();
            try {
                iPartsWSgetVehicleDatacardResponse datacardResponse = genson.deserialize(jsonAsString, iPartsWSgetVehicleDatacardResponse.class);

                if (datacardResponse == null) {
                    throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Datenkarte zur FIN/VIN \"%1\" ist semantisch ungültig: %2",
                                                                                      fin.getFIN(), TranslationHandler.translate("!!Deserialisierung als Datenkarte nicht möglich")));
                }
                iPartsWSvehicleRestrictedInclMasterDataResponse vehicleRestrictedInclMasterDataResponse = datacardResponse.getVehicleRestrictedInclMasterDataResponse();
                if (vehicleRestrictedInclMasterDataResponse != null) {
                    // Evtl. vorhandene Exception vom Datenkarten-Webservice auswerten
                    iPartsWSDataCardException dataCardException = vehicleRestrictedInclMasterDataResponse.getException();
                    if ((dataCardException != null) && !StrUtils.isEmpty(dataCardException.getId())) { // ID muss vorhanden sein bei einer Exception
                        throw new DataCardRetrievalException(TranslationHandler.translate("!!Fehlermeldung vom Fahrzeug-Datenkarten-Webservice für FIN/VIN \"%1\": %2 - %3",
                                                                                          fin.getFIN(), dataCardException.getId(),
                                                                                          vehicleRestrictedInclMasterDataResponse.getException().getDescription()));
                    }
                    // hier sind wir dann endlich bei den relevanten Daten angekommen
                    iPartsWSvehicleInclMasterData vehicleInclMasterData = vehicleRestrictedInclMasterDataResponse.getVehicleInclMasterData();
                    if (vehicleInclMasterData != null) {
                        return vehicleInclMasterData;
                    } else {
                        throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Fahrzeug-Datenkarte %1 ist semantisch ungültig: %2",
                                                                                          fin.getFIN(), TranslationHandler.translate("!!\"vehicleInclMasterData\" fehlt")));
                    }
                } else {
                    throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Fahrzeug-Datenkarte %1 ist semantisch ungültig: %2",
                                                                                      fin.getFIN(), TranslationHandler.translate("!!\"vehicleRestrictedInclMasterDataResponse\" fehlt")));
                }
            } catch (JsonBindingException exception) {
                throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Datenkarte zur FIN/VIN \"%1\" ist syntaktisch fehlerhaft.",
                                                                                  fin.getFIN()), exception);
            }
        } else {
            throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Datenkarte zur FIN/VIN \"%1\" ist leer.",
                                                                              fin.getFIN()));
        }
    }

    public static iPartsWSactiveGeneralMajAssy getResponseAsAggregateDatacardJSONObject(String jsonAsString, DCAggregateTypes type, String ident) throws DataCardRetrievalException {
        String requestText = "\"" + type.getDescription() + " " + ident + "\"";
        if (!StrUtils.isEmpty(jsonAsString)) {
            GensonBuilder gensonBuilder = new GensonBuilder();
            Genson genson = gensonBuilder.create();
            try {
                iPartsWSgetAggregateDatacardResponse datacardResponse = genson.deserialize(jsonAsString, iPartsWSgetAggregateDatacardResponse.class);
                if (datacardResponse == null) {
                    throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Aggregate-Datenkarte %1 ist semantisch ungültig: %2",
                                                                                      requestText, TranslationHandler.translate("!!Deserialisierung als Aggregate-Datenkarte nicht möglich")));
                }
                iPartsWSmajAssyRestrictedResponse majAssyRestrictedResponse = datacardResponse.getMajAssyRestrictedResponse();
                if (majAssyRestrictedResponse != null) {
                    // Evtl. vorhandene Exception vom Datenkarten-Webservice auswerten
                    iPartsWSDataCardException dataCardException = majAssyRestrictedResponse.getException();
                    if ((dataCardException != null) && !StrUtils.isEmpty(dataCardException.getId())) { // ID muss vorhanden sein bei einer Exception
                        throw new DataCardRetrievalException(TranslationHandler.translate("!!Fehlermeldung vom Aggregate-Datenkarten-Webservice für Aggregate-Datenkarte %1: %2 - %3",
                                                                                          requestText, dataCardException.getId(),
                                                                                          majAssyRestrictedResponse.getException().getDescription()));
                    }

                    iPartsWSactiveGeneralMajAssy activeGeneralMajAssy = majAssyRestrictedResponse.getActiveGeneralMajAssy();
                    if (activeGeneralMajAssy != null) {
                        return activeGeneralMajAssy;
                    } else {
                        throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Aggregate-Datenkarte %1 ist semantisch ungültig: %2",
                                                                                          requestText, TranslationHandler.translate("!!\"activeGeneralMajAssy\" fehlt")));
                    }
                } else {
                    throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Aggregate-Datenkarte %1 ist semantisch ungültig: %2",
                                                                                      requestText, TranslationHandler.translate("!!\"majAssyRestrictedResponse\" fehlt")));
                }
            } catch (JsonBindingException exception) {
                throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Aggregate-Datenkarte %1 ist syntaktisch fehlerhaft.",
                                                                                  requestText), exception);
            }
        } else {
            throw new DataCardRetrievalException(TranslationHandler.translate("!!Der JSON-String für die Aggregate-Datenkarte %1 ist leer.",
                                                                              requestText));
        }
    }

    /**
     * Konvertiert die Liste der JSON Code Objekte in ein Set aus Strings. Das Set enthält dann nur noch den Code
     * selbst, keine Metadaten aus dem JSON.
     *
     * @param codeList
     * @return
     */
    public static Set<String> convertCodeListToStringSet(List<iPartsWSequipmentCodes> codeList) {
        if ((codeList != null) && !codeList.isEmpty()) {
            Set<String> resultSet = new TreeSet<String>();
            for (iPartsWSequipmentCodes code : codeList) {
                resultSet.add(code.getCode());
            }
            return resultSet;
        }
        return null;
    }

    /**
     * Konvertiert die Liste der JSON SAA Objekte in ein Set aus Strings. Das Set enthält dann nur noch die SAAs selbst,
     * keine Metadaten aus dem JSON.
     *
     * @param saaList
     * @return
     */
    public static Set<String> convertSAAListToStringSet(List<iPartsWSsaa> saaList, boolean sourceVehicleDatacard) {
        if ((saaList != null) && !saaList.isEmpty()) {
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            Set<String> resultSet = new TreeSet<String>();
            for (iPartsWSsaa saa : saaList) {
                // typeIndicator (== "R") doch nicht mehr auswerten (DAIMLER-6904)
                String id;
                // Unterschiedliche Strukturen in Fahrzeug- und Aggregatedatenkarten
                if (sourceVehicleDatacard) {
                    id = saa.getId();
                } else {
                    id = saa.getSaaDesignation();
                }

                //eine valide SAA-Nummer?
                String pseudoSaa = "Z" + id;
                if (numberHelper.isValidSaa(pseudoSaa)) {
                    resultSet.add(pseudoSaa);
                } else {
                    //mache A-Sachnummer (Baukasten) daraus
                    resultSet.add(prefixWithAIfNotExists(id));
                }
            }
            return resultSet;
        }
        return null;
    }

    /**
     * Konvertiert eine Liste mit JSON Objekten in die interne Federstruktur. Dabei werden nicht verwendete Idents
     * ausgefiltert. Für Federn sind aktuell nur die folgenden Idents relevant:
     * {@see #SPRINGLEG_FRONT}, {@see #SPRINGLEG_REAR}, {@see SPRINGSHIM_REAR}
     *
     * @param vpdList
     * @return
     */
    public static SpringData convertVpdListToStringList(List<iPartsWSvpd> vpdList) {
        if ((vpdList != null) && !vpdList.isEmpty()) {
            SpringData result = new SpringData();
            for (iPartsWSvpd vpd : vpdList) {
                String ident = vpd.getVpdIdent();
                String content = vpd.getContent();
                if (ident.equals(SPRINGLEG_FRONT)) {
                    result.springLegFront.addAll(handleSpringNumber(content));
                } else if (ident.equals(SPRINGLEG_REAR)) {
                    result.springRear.addAll(handleSpringNumber(content));
                } else if (ident.equals(SPRINGSHIM_REAR)) {
                    result.springShimRear.addAll(handleSpringNumber(content));
                }
            }
            return result;
        }
        return null;
    }

    /**
     * Im Content können sich 2 Sachnummern verbergen. Sachnummer 1 sind die Stellen 1-24, Sachnummer 2 ab Stelle 25.
     * Falls die Sachnummern nicht mit einem A beginnen, wird ein A vorne dran gesetzt.
     *
     * @param content
     * @return
     */
    private static List<String> handleSpringNumber(String content) {
        DwList<String> result = new DwList<String>();
        if (!content.isEmpty()) {
            if (content.length() > 24) {
                result.add(prefixWithAIfNotExists(content.substring(0, 24).trim()));
                result.add(prefixWithAIfNotExists(content.substring(24).trim()));
            } else {
                result.add(prefixWithAIfNotExists(content.trim()));
            }
        }
        return result;
    }

    /**
     * Aggregatedatenkarte für Brennstoffzelle aus einem bestimmten VPD Datensatz erzeugen, falls vorhanden.
     * {@link iPartsDataCardRetrievalHelper#VPD_IDENT_FUEL_CELL}
     *
     * @param vpdList
     * @param project
     * @return
     */
    public static AggregateDataCard createFuelCellDatacardFromVpdList(List<iPartsWSvpd> vpdList, EtkProject project) {
        if ((vpdList != null) && !vpdList.isEmpty()) {
            for (iPartsWSvpd vpd : vpdList) {
                String vpdIdent = vpd.getVpdIdent();
                if ((vpdIdent != null) && vpdIdent.equals(VPD_IDENT_FUEL_CELL)) {
                    String content = vpd.getContent();
                    if ((content != null) && (content.length() >= 28)) {
                        try {
                            // ZB Sachnummer: Stelle 1-11
                            String zbNumber = prefixWithAIfNotExists(content.substring(0, 11).trim());
                            // Aggregateidentnummer: Stelle 14-28
                            String aggregateIdent = content.substring(13, 28).trim();
                            AggregateDataCard aggregateDataCard = AggregateDataCard.getAggregateDataCard(
                                    DCAggregateTypes.FUEL_CELL, aggregateIdent, false, true, null, project);
                            aggregateDataCard.setAggregateBasicType(DCAggregateTypes.FUEL_CELL, DCAggregateTypeOf.FUEL_CELL1); // Aktuell wird fest Brennstoffzelle 1 angenommen
                            aggregateDataCard.setObjectNo(zbNumber); // wird später für die Anreicherung verwendet
                            return aggregateDataCard;
                        } catch (DataCardRetrievalException e) {
                            // Kann nicht auftreten, da wir hier keine Datenkarten laden, sondern nur aus dem BM befüllen
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Prüft den übergebenen String, ob er mit A beginnt. Falls nicht, wird ein A vorne dran geschrieben
     * Wird für die Federsachnummern benötigt
     *
     * @param text
     * @return
     */
    private static String prefixWithAIfNotExists(String text) {
        if ((text != null) && !text.isEmpty()) {
            if (text.toUpperCase().startsWith("A")) {
                return text;
            } else {
                return "A" + text;
            }
        }
        return "";
    }

    public static Set<String> getFixingPartsForFin(FinId fin, String language) throws DataCardRetrievalException {
        TreeSet<String> fixingPartsList = new TreeSet<String>();
        String baseURI = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_WEBSERVICES_DATACARDS_BASE_URI);


        String datacardJson = getJsonFromWebservice(baseURI, fin.getFIN(), language, DatacardType.FIXING_PARTS);
        if (datacardJson != null) {
            iPartsWSvehicle vehicleJSON = getResponseAsFixingPartsJSONObject(datacardJson, fin);

            if ((vehicleJSON != null) && (vehicleJSON.getChassisHoleInformation() != null)) {
                for (iPartsWSchassisHoleInformation chassisHole : vehicleJSON.getChassisHoleInformation()) {
                    if (chassisHole.getFixingParts() != null) {
                        for (iPartsWSfixingParts fixingPart : chassisHole.getFixingParts()) {
                            String partNumber = fixingPart.getPartNumber();
                            if (partNumber != null) {
                                fixingPartsList.add(partNumber);
                            }
                        }
                    }
                }
            }
        }

        return fixingPartsList;
    }

    /**
     * Lädt alle Befestigungsteile zur übergebenen Fahrzeug-Datenkarte inkl. Logging und Fehlerbehandlung.
     *
     * @param vehicleDataCard Fahrzeug-Datenkarte
     * @param logPrefix       Präfix für die Performance-Logausgabe
     * @return
     */
    public static void loadFixingParts(VehicleDataCard vehicleDataCard, String logPrefix) {
        if (!vehicleDataCard.isFixingPartsLoaded()) {
            TimeSpanLogger getDataCardLogger = new TimeSpanLogger(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.DEBUG,
                                                                  logPrefix + ".getFixingPartsFromWebservice(" + vehicleDataCard.getFinId().getFIN()
                                                                  + ") for session " + Session.get().getId(), -1, false);
            try {
                vehicleDataCard.loadFixingParts();
            } catch (DataCardRetrievalException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DATACARD_SERVICE, LogType.ERROR, e);
            }
            getDataCardLogger.logFinal();
        }
    }


    /**
     * Interne Struktur für Federn. Drei getrennte Listen für die unterscheidlichen Typen
     */
    public static class SpringData {

        public List<String> springLegFront = new DwList<String>();
        public List<String> springRear = new DwList<String>();
        public List<String> springShimRear = new DwList<String>();
    }


    /**
     * Interface für einen Callback beim Laden einer Datenkarte.
     */
    public static interface LoadDataCardCallback {

        /**
         * Wird aufgerufen, wenn eine Datenkarte über den Webservice geladen werden muss und nicht im Cache gefunden wurde.
         * <br/>Das <i>loadDataCardRunnable</i> muss <b>synchron</b> ausgeführt werden, also entweder direkt oder in einem
         * modalen Dialog.
         *
         * @param loadDataCardRunnable <b>Synchron</b> auszuführende Lade-Methode für die Datenkarte.
         */
        void loadDataCard(Runnable loadDataCardRunnable);
    }
}