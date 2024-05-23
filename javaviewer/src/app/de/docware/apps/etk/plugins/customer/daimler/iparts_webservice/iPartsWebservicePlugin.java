/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice;

import de.docware.apps.etk.base.project.events.AbstractEtkProjectEvent;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.events.NotesChangedEvent;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.plugins.AbstractJavaViewerSimpleEndpointPlugin;
import de.docware.apps.etk.plugins.EtkPluginConstants;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPublishingEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.datacardsSimulation.iPartsWSDatacardsSimulationEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialnav.iPartsWSGetMaterialNavEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialpartinfo.iPartsWSGetMaterialPartInfoEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmaterialparts.iPartsWSGetMaterialPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmedia.iPartsWSGetMediaEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodels.iPartsWSGetModelsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getmodeltypes.iPartsWSGetModelTypesEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getnavopts.iPartsWSGetNavOptsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getpartinfo.iPartsWSGetPartInfoEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getparts.iPartsWSGetPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductclasses.iPartsWSGetProductClassesEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.getproductgroups.iPartsWSGetProductGroupsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.iPartsWSAbstractEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ident.iPartsWSIdentEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist.iPartsWSPartsListEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.ppua.iPartsWSPPUAEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchcomponent.iPartsWSsearchComponentEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchmaterialparts.iPartsWSSearchMaterialPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchparts.iPartsWSSearchPartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.searchpartswocontext.iPartsWSSearchPartsWOContextEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.validateparts.iPartsWSValidatePartsEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.versioninfo.iPartsWSVersionInfoEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.visualNav.iPartsWSVisualNavEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSFilteredPartListsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSPartsBaseCache;
import de.docware.apps.etk.plugins.interfaces.ReceiveEtkProjectEventInterface;
import de.docware.apps.etk.viewer.FrameworkMain;
import de.docware.framework.combimodules.config_gui.ConfigurationWindow;
import de.docware.framework.combimodules.config_gui.UniversalConfigurationPanel;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.config.license.LicenseConfig;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.misc.endpoint.FrameworkSimpleEndpoint;
import de.docware.framework.modules.gui.misc.http.server.HttpCallback;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.misc.validator.default_validators.GuiControlEndpointUriPrefixValidator;
import de.docware.framework.modules.interappcom.ClusterEventInterface;
import de.docware.framework.modules.webservice.restful.jwt.JWTKeystore;
import de.docware.framework.modules.webservice.restful.jwt.JWTKeystoreManager;
import de.docware.util.security.PasswordString;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * iParts Plug-in für die Webservices.
 */
public class iPartsWebservicePlugin extends AbstractJavaViewerSimpleEndpointPlugin implements ReceiveEtkProjectEventInterface,
                                                                                              iPartsConst {

    public static final String INTERNAL_PLUGIN_NAME = "iPartsWebservice";
    public static final String OFFICIAL_PLUGIN_NAME = "DAIMLER iParts Webservice Plug-in";
    public static final String PLUGIN_VERSION = "1.0";

    public static final LogChannels LOG_CHANNEL_DEBUG = new LogChannels("DEBUG", false, true);
    public static final LogChannels LOG_CHANNEL_PERFORMANCE = new LogChannels("PERFORMANCE", false, true);
    public static final LogChannels LOG_CHANNEL_TOKEN = new LogChannels("TOKEN", false, true);

    public static final UniversalConfigOption CONFIG_JSON_RESPONSE_CACHE_SIZE = UniversalConfigOption.getIntegerOption("/jsonResponseCacheSize", 1000);

    public static final UniversalConfigOption CONFIG_HEADER_TOKEN_NAME = UniversalConfigOption.getStringOption("/headerTokenName", "authentication");
    public static final UniversalConfigOption CONFIG_HEADER_TOKEN_TYPE = UniversalConfigOption.getStringOption("/headerTokenType", "Bearer");
    public static final UniversalConfigOption CONFIG_HEADER_TOKEN_PASSWORD = UniversalConfigOption.getPasswordOption("/headerTokenPassword", new PasswordString("topsecret"));
    public static final UniversalConfigOption CONFIG_HEADER_TOKEN_PUB_KEY_DIR = UniversalConfigOption.getFileOption("/headerTokenPubKeyDir", new File("jwtPublicKeys"));
    public static final UniversalConfigOption CONFIG_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING = UniversalConfigOption.getBooleanOption("/headerTokenPubKeyDirEnforcePolling", false);
    public static final UniversalConfigOption CONFIG_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME = UniversalConfigOption.getIntegerOption("/headerTokenPubKeyDirPollingTime", 10);
    public static final UniversalConfigOption CONFIG_HEADER_TOKEN_EXPIRES = UniversalConfigOption.getIntegerOption("/headerTokenExpires", 60 * 60); // 1 Stunde
    public static final UniversalConfigOption CONFIG_USER_IN_PAYLOAD_FALLBACK = UniversalConfigOption.getBooleanOption("/userInPayloadFallback", false);
    public static final UniversalConfigOption CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS = UniversalConfigOption.getBooleanOption("/headerAttributesForPermissions", false);

    public static final UniversalConfigOption CONFIG_ONLY_RETAIL_RELEVANT_PRODUCTS = UniversalConfigOption.getBooleanOption("/onlyRetailRelevantProducts", true);

    public static final UniversalConfigOption CONFIG_CHECK_TOKEN_PERMISSIONS = UniversalConfigOption.getBooleanOption("/checkTokenPermissions", false);
    public static final UniversalConfigOption CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY = UniversalConfigOption.getBooleanOption("/checkTokenCountryValidity", true);

    public static final UniversalConfigOption CONFIG_REDUCE_RESPONSE_DATA_RMI = UniversalConfigOption.getBooleanOption("/reduceResponseDataRMI", false);
    public static final UniversalConfigOption CONFIG_ONLY_FIN_BASED_REQUESTS_RMI = UniversalConfigOption.getBooleanOption("/onlyFinBasedRequestsRMI", false);

    public static final UniversalConfigOption CONFIG_URI_VERSION_INFO = UniversalConfigOption.getStringOption("/versioninfoURI", iPartsWSVersionInfoEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_NAV_OPTS = UniversalConfigOption.getStringOption("/getNavOptsURI", iPartsWSGetNavOptsEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_PARTS = UniversalConfigOption.getStringOption("/getPartsURI", iPartsWSGetPartsEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_PART_INFO = UniversalConfigOption.getStringOption("/getPartInfoURI", iPartsWSGetPartInfoEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_GET_PART_INFO_MAX_PARALLEL_REQUESTS = UniversalConfigOption.getIntegerOption("/getPartInfoMaxParallelRequests", 10);
    public static final UniversalConfigOption CONFIG_GET_PART_INFO_TIMEOUT_PARALLEL_REQUESTS = UniversalConfigOption.getIntegerOption("/getPartInfoTimeoutParallelRequests", 120);
    public static final UniversalConfigOption CONFIG_SHOW_LATEST_EINPAS_NODE_IN_RESPONSE = UniversalConfigOption.getBooleanOption("/showLatestEinPasNodeInResponse", false);

    public static final UniversalConfigOption CONFIG_URI_SEARCH_PARTS = UniversalConfigOption.getStringOption("/searchPartsURI", iPartsWSSearchPartsEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_SEARCH_PARTS_WO_CONTEXT = UniversalConfigOption.getStringOption("/searchPartsWOContextURI", iPartsWSSearchPartsWOContextEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_MIN_CHAR_FOR_SEARCH_PARTS_SEARCH_TEXTS = UniversalConfigOption.getIntegerOption("/minCharForSearchPartsSearchTexts", 3);
    public static final UniversalConfigOption CONFIG_MAX_RESULTS_SEARCH_PARTS = UniversalConfigOption.getIntegerOption("/searchPartsMaxResults", iPartsWSSearchPartsEndpoint.DEFAULT_MAX_RESULTS);
    public static final UniversalConfigOption CONFIG_TIMEOUT_SEARCH_PARTS = UniversalConfigOption.getIntegerOption("/searchPartsTimeout", iPartsWSSearchPartsEndpoint.DEFAULT_TIMEOUT);
    public static final UniversalConfigOption CONFIG_URI_VALIDATE_PARTS = UniversalConfigOption.getStringOption("/validatePartsURI", iPartsWSValidatePartsEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_VALIDATE_PARTS_THREAD_COUNT = UniversalConfigOption.getIntegerOption("/validatePartsThreadCount", 4);
    public static final UniversalConfigOption CONFIG_HIDE_EMPTY_TUS_IN_RESPONSE = UniversalConfigOption.getBooleanOption("/hideEmptyTUsInResponse", false);

    public static final UniversalConfigOption CONFIG_URI_GET_MEDIA = UniversalConfigOption.getStringOption("/getMediaURI", iPartsWSGetMediaEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_BASE_URL_GET_MEDIA = UniversalConfigOption.getStringOption("/getMediaBaseURL", iPartsWSGetMediaEndpoint.DEFAULT_BASE_URL);
    public static final UniversalConfigOption CONFIG_CACHE_LIFETIME_GET_MEDIA = UniversalConfigOption.getIntegerOption("/getMediaCacheLifetime", iPartsWSGetMediaEndpoint.DEFAULT_CACHE_LIFETIME);
    public static final UniversalConfigOption CONFIG_EXPIRE_HEADER_OVERRIDE_GET_MEDIA = UniversalConfigOption.getIntegerOption("/getMediaExpireHeaderOverride", -1);
    public static final UniversalConfigOption CONFIG_GRAPHICS_MAX_THUMBNAIL_HEIGHT_GET_MEDIA = UniversalConfigOption.getIntegerOption("/getMediaGraphicsMaxThumbnailHeight", iPartsWSGetMediaEndpoint.DEFAULT_GRAPHICS_MAX_THUMBNAIL_HEIGHT);
    public static final UniversalConfigOption CONFIG_NO_AUTHENTIFICATION_GET_MEDIA = UniversalConfigOption.getBooleanOption("/getMediaNoAuthentification", true);

    public static final UniversalConfigOption CONFIG_MIN_CHAR_FOR_IDENT_SEARCH_TEXTS = UniversalConfigOption.getIntegerOption("/minCharForIdentSearchTexts", 3);
    public static final UniversalConfigOption CONFIG_URI_IDENT = UniversalConfigOption.getStringOption("/getIdentURI", iPartsWSIdentEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_PRODUCT_GROUPS = UniversalConfigOption.getStringOption("/getProductGroupsURI", iPartsWSGetProductGroupsEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_PRODUCT_CLASSES = UniversalConfigOption.getStringOption("/getProductClassesURI", iPartsWSGetProductClassesEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_MODEL_TYPES = UniversalConfigOption.getStringOption("/getModelTypesURI", iPartsWSGetModelTypesEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_MODELS = UniversalConfigOption.getStringOption("/getModelsURI", iPartsWSGetModelsEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_MATERIAL_NAV = UniversalConfigOption.getStringOption("/getMaterialNavURI", iPartsWSGetMaterialNavEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_MATERIAL_PARTS = UniversalConfigOption.getStringOption("/getMaterialPartsURI", iPartsWSGetMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_GET_MATERIAL_PART_INFO = UniversalConfigOption.getStringOption("/getMaterialPartInfoURI", iPartsWSGetMaterialPartInfoEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_SEARCH_MATERIAL_PARTS = UniversalConfigOption.getStringOption("/searchMaterialPartsURI", iPartsWSSearchMaterialPartsEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_SEARCH_COMPONENT = UniversalConfigOption.getStringOption("/searchComponentURI", iPartsWSsearchComponentEndpoint.DEFAULT_ENDPOINT_URI);

    public static final UniversalConfigOption CONFIG_PARTS_LIST_ACTIVE = UniversalConfigOption.getBooleanOption("/partsListActive", false);
    public static final UniversalConfigOption CONFIG_URI_PARTS_LIST = UniversalConfigOption.getStringOption("/partsListURI", iPartsWSPartsListEndpoint.DEFAULT_ENDPOINT_URI);

    public static final UniversalConfigOption CONFIG_URI_DATACARDS_SIM_BASE = UniversalConfigOption.getStringOption("/datacardsSimBaseURI", iPartsWSDatacardsSimulationEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_DATACARDS_SIM_DIR = UniversalConfigOption.getFileOption("/datacardsSimDir", new File("DataCards"));
    public static final UniversalConfigOption CONFIG_DATACARDS_SIM_ACTIVE = UniversalConfigOption.getBooleanOption("/datacardsSimActive", false);
    public static final UniversalConfigOption CONFIG_DATACARDS_SIM_DELAY = UniversalConfigOption.getIntegerOption("/datacardsSimDelay", 3000);
    public static final UniversalConfigOption<String> CONFIG_URI_PPUA_DATA = UniversalConfigOption.getStringOption("/ppuaDataURI", iPartsWSPPUAEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption<String> CONFIG_URI_VISUAL_NAV = UniversalConfigOption.getStringOption("/visualNavURI", iPartsWSVisualNavEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption<Integer> CONFIG_MAX_PART_NUMBERS_INPUT_PPUA_DATA = UniversalConfigOption.getIntegerOption("/ppuaDataMaxPartNumbersInput", iPartsWSPPUAEndpoint.DEFAULT_MAX_PART_NUMBERS_INPUT);
    private static UniversalConfiguration pluginConfig;

    private static JWTKeystoreManager keystoreManager;

    private static int minCharForIdentSearchTexts;
    private static int minCharForSearchPartsSearchTexts;
    private static boolean isCheckTokenPermissions;
    private static boolean isCheckTokenCountryValidity;
    private static boolean isOnlyRetailRelevantProducts;
    private static boolean isOnlyFinBasedRequestsRMI;
    private static boolean isRMIActive;
    private static boolean isShowLatestEinPasNodeInResponse;
    private static boolean isPartsListActive;
    private String host;
    private int port;

    private iPartsWSVersionInfoEndpoint versionInfoEndpoint;

    public static UniversalConfiguration getPluginConfig() {
        return pluginConfig;
    }

    public static List<JWTKeystore> getKeystores() {
        return keystoreManager.getKeystores();
    }

    public static void initConfigurationSettingsVariables() {
        if (pluginConfig != null) {
            minCharForIdentSearchTexts = pluginConfig.getConfigValueAsInteger(CONFIG_MIN_CHAR_FOR_IDENT_SEARCH_TEXTS);
            minCharForSearchPartsSearchTexts = pluginConfig.getConfigValueAsInteger(CONFIG_MIN_CHAR_FOR_SEARCH_PARTS_SEARCH_TEXTS);
            isCheckTokenPermissions = pluginConfig.getConfigValueAsBoolean(CONFIG_CHECK_TOKEN_PERMISSIONS);
            isCheckTokenCountryValidity = pluginConfig.getConfigValueAsBoolean(CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY);
            isOnlyRetailRelevantProducts = pluginConfig.getConfigValueAsBoolean(CONFIG_ONLY_RETAIL_RELEVANT_PRODUCTS);
            isOnlyFinBasedRequestsRMI = pluginConfig.getConfigValueAsBoolean(CONFIG_ONLY_FIN_BASED_REQUESTS_RMI);
            isRMIActive = pluginConfig.getConfigValueAsBoolean(iPartsWebservicePlugin.CONFIG_REDUCE_RESPONSE_DATA_RMI);
            isShowLatestEinPasNodeInResponse = pluginConfig.getConfigValueAsBoolean(CONFIG_SHOW_LATEST_EINPAS_NODE_IN_RESPONSE);
            isPartsListActive = pluginConfig.getConfigValueAsBoolean(CONFIG_PARTS_LIST_ACTIVE);
        }
    }

    public static int getMinCharForIdentSearchTexts() {
        return minCharForIdentSearchTexts;
    }

    public static int getMinCharForSearchPartsSearchTexts() {
        return minCharForSearchPartsSearchTexts;
    }

    public static boolean isCheckTokenPermissions() {
        return isCheckTokenPermissions;
    }

    public static boolean isCheckTokenCountryValidity() {
        return isCheckTokenCountryValidity;
    }

    public static boolean isOnlyRetailRelevantProducts() {
        return isOnlyRetailRelevantProducts;
    }

    public static boolean isOnlyFinBasedRequestsRMI() {
        return isOnlyFinBasedRequestsRMI;
    }

    public static boolean isRMIActive() {
        return isRMIActive;
    }

    public static boolean isShowLatestEinPASNodeInResponse() {
        return isShowLatestEinPasNodeInResponse;
    }

    /**
     * Standardkonstruktor für die normale Verwendung als Plug-in.
     */
    public iPartsWebservicePlugin() {
    }

    /**
     * Konstruktor für die Unit-Tests.
     *
     * @param host
     * @param port
     */
    public iPartsWebservicePlugin(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public UniversalConfigurationPanel getConfigurationPanel(ConfigurationWindow host) {
        UniversalConfigurationPanel configurationPanel = new UniversalConfigurationPanel(host, pluginConfig, OFFICIAL_PLUGIN_NAME, true);
        configurationPanel.addIntegerSpinnerOption(CONFIG_JSON_RESPONSE_CACHE_SIZE, "!!Maximale Größe vom JSON-Response-Cache (Anzahl Objekte; pro Webservice)",
                                                   false, 0, 1000000, 100).setTooltip("!!0 für \"kein Cache\"");

        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!Webservice-Token im Request-Header");
        configurationPanel.addStringOption(CONFIG_HEADER_TOKEN_NAME, "!!Name", true);
        configurationPanel.addStringOption(CONFIG_HEADER_TOKEN_TYPE, "!!Typ", true);
        configurationPanel.addPasswordOption(CONFIG_HEADER_TOKEN_PASSWORD, "!!Passwort (für HS256 Verfahren; wenn leer ist HS256 nicht zugelassen)", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_HEADER_TOKEN_EXPIRES, "!!Erlaubte Differenz in Sekunden", true,
                                                   1, 100000, 10);
        {
            configurationPanel.startGroup("!!Überwachtes Verzeichnis für Public Key Definitionen im JSON-Format (für RS256 Verfahren)");
            configurationPanel.addFileOption(CONFIG_HEADER_TOKEN_PUB_KEY_DIR, "!!Verzeichnis", false,
                                             GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.OPEN, "!!Auswählen");
            configurationPanel.addBooleanOption(CONFIG_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING, "!!Polling erzwingen (nötig z.B. für NFS Shares)", false);
            configurationPanel.addIntegerSpinnerOption(CONFIG_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME, "!!Polling Intervall (in min)", false, 1, 1000, 1);
            configurationPanel.endGroup();
        }


        configurationPanel.addSeparator();
        configurationPanel.addBooleanOption(CONFIG_HEADER_ATTRIBUTES_FOR_PERMISSIONS, "!!Header-Attribute für Berechtigungen zulassen anstatt Webservice-Token", false)
                .setTooltip("!!Achtung! Bei vorhandenen Header-Attributen für die Berechtigungen findet keine Token-Validierung mehr statt!");
        configurationPanel.endGroup();

        configurationPanel.addSeparator();
        configurationPanel.addBooleanOption(CONFIG_ONLY_RETAIL_RELEVANT_PRODUCTS, "!!Produkt-Sichtbarkeit berücksichtigen", false).
                setTooltip("!!Falls deaktiviert: Es werden ALLE Produkte berücksichtigt unabhängig vom Flag \"Produkt sichtbar\"");
        configurationPanel.addBooleanOption(CONFIG_CHECK_TOKEN_PERMISSIONS, "!!Gültigkeiten für Marke und Produktgruppe auswerten", false);
        configurationPanel.addBooleanOption(CONFIG_CHECK_TOKEN_COUNTRY_VALIDITY, "!!Ländergültigkeiten auswerten", false);
        configurationPanel.addBooleanOption(CONFIG_HIDE_EMPTY_TUS_IN_RESPONSE, "Leere TUs ausblenden bei den Webservices GetNavOpts und partsList", false);

        configurationPanel.addSeparator();
        configurationPanel.startGroup("!!RMI Typzulassung");
        configurationPanel.addBooleanOption(CONFIG_REDUCE_RESPONSE_DATA_RMI, "!!Antwortdaten für RMI Typzulassung reduzieren", false).
                setTooltip("!!Muss in Webservices für die RMI Typzulassung aktiv sein, für die nur bestimmte Stücklisteninhalte bereitgestellt werden dürfen.");
        configurationPanel.addBooleanOption(CONFIG_ONLY_FIN_BASED_REQUESTS_RMI, "!!Nur Anfragen mit FIN/VIN und Datenkarte für RMI Typzulassung verarbeiten", false).
                setTooltip("!!Muss in Webservices für die RMI Typzulassung aktiv sein, für die nur FIN/VIN mit Datenkarte zugelassen sind.");
        configurationPanel.endGroup();

        configurationPanel.addSeparator();
        configurationPanel.addLabel(TranslationHandler.translate("!!Einstellungen für Webservices \"%1\":", "Parts"), null).setFontStyle(DWFontStyle.BOLD);
        addEndpointURIConfigOption(CONFIG_URI_VERSION_INFO, "VersionInfo", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_GET_NAV_OPTS, "GetNavOpts", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_GET_PARTS, "GetParts", configurationPanel);
        configurationPanel.addBooleanOption(CONFIG_SHOW_LATEST_EINPAS_NODE_IN_RESPONSE, "!!Aktuellsten EinPAS-Knoten in den Antwortdaten mit ausgegeben", false);

        configurationPanel.startGroup("!!Webservice GetPartInfo");
        addEndpointURIConfigOption(CONFIG_URI_GET_PART_INFO, "GetPartInfo", configurationPanel);
        configurationPanel.addIntegerSpinnerOption(CONFIG_GET_PART_INFO_MAX_PARALLEL_REQUESTS, "!!Maximale Anzahl paralleler ähnlicher Requests",
                                                   false, -1, 999999, 1).setTooltip("!!0 oder -1 für \"unendlich\"; ein Request ist ähnlich bei identischem Benutzer und TU");
        configurationPanel.addIntegerSpinnerOption(CONFIG_GET_PART_INFO_TIMEOUT_PARALLEL_REQUESTS, "!!Timeout für parallele ähnliche Requests in Sekunden",
                                                   false, 1, 999999, 1).setTooltip("!!0 oder -1 für \"kein Timeout\"");
        configurationPanel.endGroup();

        configurationPanel.startGroup("!!Webservices 'SearchParts', 'SearchMaterialParts', 'SearchPartsWOContext' und 'SearchComponent'");
        addEndpointURIConfigOption(CONFIG_URI_SEARCH_PARTS, "SearchParts", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_SEARCH_MATERIAL_PARTS, "SearchMaterialParts", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_SEARCH_PARTS_WO_CONTEXT, "SearchPartsWOContext", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_SEARCH_COMPONENT, "SearchComponent", configurationPanel);
        configurationPanel.addIntegerSpinnerOption(CONFIG_MIN_CHAR_FOR_SEARCH_PARTS_SEARCH_TEXTS, "!!Minimale Anzahl Zeichen für Suchtexte",
                                                   false, 1, 100, 1);
        configurationPanel.addIntegerSpinnerOption(CONFIG_MAX_RESULTS_SEARCH_PARTS, "!!Maximale Anzahl Suchergebnisse",
                                                   false, -1, 999999, 1).setTooltip("!!-1 für \"unendlich\"");
        configurationPanel.addIntegerSpinnerOption(CONFIG_TIMEOUT_SEARCH_PARTS, "!!Timeout für Suchen in Sekunden",
                                                   false, -1, 999999, 1).setTooltip("!!0 oder -1 für \"kein Timeout\"");
        configurationPanel.endGroup();

        configurationPanel.startGroup("!!Webservice 'Validate'");
        addEndpointURIConfigOption(CONFIG_URI_VALIDATE_PARTS, "Validate", configurationPanel);
        configurationPanel.addIntegerSpinnerOption(CONFIG_VALIDATE_PARTS_THREAD_COUNT, "!!Maximale Anzahl an Such-Threads",
                                                   true, 1, 128, 1);
        configurationPanel.endGroup();

        configurationPanel.startGroup("!!Webservice 'PPUA'");
        addEndpointURIConfigOption(CONFIG_URI_PPUA_DATA, "PPUA", configurationPanel);
        configurationPanel.addIntegerSpinnerOption(CONFIG_MAX_PART_NUMBERS_INPUT_PPUA_DATA, "!!Maximale Anzahl übergebener Teilenummern",
                                                   false, -1, 999999, 1).setTooltip("!!0 oder -1 für \"unendlich\"");
        configurationPanel.endGroup();

        configurationPanel.startGroup("!!Webservice 'GetMedia'");
        addEndpointURIConfigOption(CONFIG_URI_GET_MEDIA, "GetMedia", configurationPanel);
        configurationPanel.addStringOption(CONFIG_BASE_URL_GET_MEDIA, TranslationHandler.translate("!!Basis-URL für Aufrufe vom Webservice '%1'", "GetMedia"), false);
        configurationPanel.addBooleanOption(CONFIG_NO_AUTHENTIFICATION_GET_MEDIA, "!!Aufruf ohne Authentifizierung möglich", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_CACHE_LIFETIME_GET_MEDIA, TranslationHandler.translate("!!Cache-Lebensdauer für Webservice '%1' in Sekunden", "GetMedia"),
                                                   false, -1, 999999, 1).setTooltip("!!-1 für \"unendlich\"");
        configurationPanel.addIntegerSpinnerOption(CONFIG_EXPIRE_HEADER_OVERRIDE_GET_MEDIA, TranslationHandler.translate("!!\"Expires\" im Response Header setzen auf \"Jetzt\" + ... Sekunden"),
                                                   false, -1, 99999, 1).setTooltip("!!0 bedeutet, dass kein Caching erlaubt ist (\"Expires\" steht auf 1970) und -1 bedeutet, dass kein \"Expires\" im Response Header gesetzt wird");
        configurationPanel.addIntegerSpinnerOption(CONFIG_GRAPHICS_MAX_THUMBNAIL_HEIGHT_GET_MEDIA, "!!Maximale Thumbnailhöhe für Zusatzgrafiken",
                                                   false, 1, 999999, 1);
        configurationPanel.endGroup();

        configurationPanel.addSeparator();
        configurationPanel.addLabel(TranslationHandler.translate("!!Einstellungen für Webservices \"%1\":", "Ident"), null).setFontStyle(DWFontStyle.BOLD);
        configurationPanel.addIntegerSpinnerOption(CONFIG_MIN_CHAR_FOR_IDENT_SEARCH_TEXTS, "!!Minimale Anzahl Zeichen für Ident-Suchtexte", false, 1, 100, 1);
        addEndpointURIConfigOption(CONFIG_URI_IDENT, "Ident", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_GET_PRODUCT_GROUPS, "GetProductGroups", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_GET_PRODUCT_CLASSES, "GetProductClasses", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_GET_MODEL_TYPES, "GetModelTypes", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_GET_MODELS, "GetModels", configurationPanel);

        configurationPanel.addSeparator();
        configurationPanel.addLabel(TranslationHandler.translate("!!Einstellungen für Webservices \"%1\":", "Material"), null).setFontStyle(DWFontStyle.BOLD);
        addEndpointURIConfigOption(CONFIG_URI_GET_MATERIAL_NAV, "GetMaterialNav", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_GET_MATERIAL_PARTS, "GetMaterialParts", configurationPanel);
        addEndpointURIConfigOption(CONFIG_URI_GET_MATERIAL_PART_INFO, "GetMaterialPartInfo", configurationPanel);

        configurationPanel.startGroup(TranslationHandler.translate("!!Webservice \"%1\"", "partsList"));
        configurationPanel.addBooleanOption(CONFIG_PARTS_LIST_ACTIVE, "!!Aktiv", false);
        addEndpointURIConfigOption(CONFIG_URI_PARTS_LIST, "partsList", configurationPanel);
        configurationPanel.endGroup();

        configurationPanel.startGroup("!!Simulation für Datenkarten-Webservice");
        configurationPanel.addBooleanOption(CONFIG_DATACARDS_SIM_ACTIVE, "!!Simulation aktiv", false);
        configurationPanel.addStringOption(CONFIG_URI_DATACARDS_SIM_BASE, TranslationHandler.translate("!!Basis-URI für die simulierten Datenkarten-Webservices"), true).
                setTooltip("!!Für Fahrzeug-Datenkarten wird automatisch \"/vehicledatacards\" an die Basis-URI angehängt und für Aggregate-Datenkarten \"/aggregatedatacards\"");
        configurationPanel.addFileOption(CONFIG_DATACARDS_SIM_DIR, "!!Ordner mit Datenkarten", false,
                                         GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.OPEN, "!!Auswählen");
        configurationPanel.addIntegerSpinnerOption(CONFIG_DATACARDS_SIM_DELAY, "!!Antwortverzögerung in Millisekunden",
                                                   false, 0, 100000, 1).setTooltip("!!Antwortverzögerung in Millisekunden bei aktiver Simulation");
        configurationPanel.endGroup();
        return configurationPanel;
    }

    @Override
    public void initPlugin(ConfigBase config) {
        initPluginBase(config, EtkPluginConstants.XML_CONFIG_PATH_BASE + '/' + INTERNAL_PLUGIN_NAME);
        pluginConfig = new UniversalConfiguration(config, getConfigPath());
        initConfigurationSettingsVariables();
        keystoreManager = new JWTKeystoreManager("Retail webservices", pluginConfig, CONFIG_HEADER_TOKEN_PUB_KEY_DIR,
                                                 CONFIG_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING,
                                                 CONFIG_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME, LOG_CHANNEL_TOKEN);
    }

    @Override
    public void applicationStarted(boolean firstInit) {
        super.applicationStarted(firstInit);
        if (isActive() && AbstractApplication.isOnline()) {
            keystoreManager.startKeystoreDirectoryMonitor();
        }
        initConfigurationSettingsVariables();
    }

    @Override
    public void configurationChanged() {
        super.configurationChanged();
        keystoreManager.stopKeystoreDirectoryMonitor();
        if (isActive() && AbstractApplication.isOnline()) {
            keystoreManager.startKeystoreDirectoryMonitor();
        }
        initConfigurationSettingsVariables();
    }

    @Override
    public void releaseReferences() {
        if (keystoreManager != null) {  // Kann null sein, wenn z.B. über den GuiDesigner kein initPlugin aufgerufen wurde
            keystoreManager.stopKeystoreDirectoryMonitor();
        }
        super.releaseReferences();
    }

    @Override
    public List<FrameworkSimpleEndpoint> createSimpleEndpoints() {
        List<FrameworkSimpleEndpoint> endpoints = new ArrayList<>();

        versionInfoEndpoint = new iPartsWSVersionInfoEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_VERSION_INFO));
        addEndpoint(versionInfoEndpoint, CONFIG_URI_VERSION_INFO, endpoints);
        addEndpoint(new iPartsWSGetNavOptsEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_NAV_OPTS)), CONFIG_URI_GET_NAV_OPTS, endpoints);
        addEndpoint(new iPartsWSGetPartsEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_PARTS)), CONFIG_URI_GET_PARTS, endpoints);
        addEndpoint(new iPartsWSGetPartInfoEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_PART_INFO)), CONFIG_URI_GET_PART_INFO, endpoints);
        addEndpoint(new iPartsWSSearchPartsEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_SEARCH_PARTS)), CONFIG_URI_SEARCH_PARTS, endpoints);
        addEndpoint(new iPartsWSSearchPartsWOContextEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_SEARCH_PARTS_WO_CONTEXT)), CONFIG_URI_SEARCH_PARTS_WO_CONTEXT, endpoints);
        addEndpoint(new iPartsWSValidatePartsEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_VALIDATE_PARTS)), CONFIG_URI_VALIDATE_PARTS, endpoints);
        addEndpoint(new iPartsWSGetMediaEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_MEDIA)), CONFIG_URI_GET_MEDIA, endpoints);
        addEndpoint(new iPartsWSIdentEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_IDENT)), CONFIG_URI_IDENT, endpoints);
        addEndpoint(new iPartsWSGetProductGroupsEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_PRODUCT_GROUPS)), CONFIG_URI_GET_PRODUCT_GROUPS, endpoints);
        addEndpoint(new iPartsWSGetProductClassesEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_PRODUCT_CLASSES)), CONFIG_URI_GET_PRODUCT_CLASSES, endpoints);
        addEndpoint(new iPartsWSGetModelTypesEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_MODEL_TYPES)), CONFIG_URI_GET_MODEL_TYPES, endpoints);
        addEndpoint(new iPartsWSGetModelsEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_MODELS)), CONFIG_URI_GET_MODELS, endpoints);
        addEndpoint(new iPartsWSGetMaterialNavEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_MATERIAL_NAV)), CONFIG_URI_GET_MATERIAL_NAV, endpoints);
        addEndpoint(new iPartsWSGetMaterialPartsEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_MATERIAL_PARTS)), CONFIG_URI_GET_MATERIAL_PARTS, endpoints);
        addEndpoint(new iPartsWSGetMaterialPartInfoEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_GET_MATERIAL_PART_INFO)), CONFIG_URI_GET_MATERIAL_PART_INFO, endpoints);
        addEndpoint(new iPartsWSSearchMaterialPartsEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_SEARCH_MATERIAL_PARTS)), CONFIG_URI_SEARCH_MATERIAL_PARTS, endpoints);
        addEndpoint(new iPartsWSsearchComponentEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_SEARCH_COMPONENT)), CONFIG_URI_SEARCH_COMPONENT, endpoints);
        addEndpoint(new iPartsWSPPUAEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_PPUA_DATA)), CONFIG_URI_PPUA_DATA, endpoints);
        addEndpoint(new iPartsWSVisualNavEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_VISUAL_NAV)), CONFIG_URI_VISUAL_NAV, endpoints);

        if (pluginConfig.getConfigValueAsBoolean(CONFIG_PARTS_LIST_ACTIVE)) {
            addEndpoint(new iPartsWSPartsListEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_PARTS_LIST)), CONFIG_URI_PARTS_LIST, endpoints);
        }

        if (pluginConfig.getConfigValueAsBoolean(CONFIG_DATACARDS_SIM_ACTIVE)) {
            String datacardsSimURI = pluginConfig.getConfigValueAsString(CONFIG_URI_DATACARDS_SIM_BASE);
            addEndpoint(new iPartsWSDatacardsSimulationEndpoint(datacardsSimURI), CONFIG_URI_DATACARDS_SIM_BASE, endpoints);
            Logger.log(LOG_CHANNEL_DEBUG, LogType.INFO, "Simulation of datacards webservice started with URI: " + datacardsSimURI);
        }
        return endpoints;
    }

    @Override
    public String getInternalPluginName() {
        return INTERNAL_PLUGIN_NAME;
    }

    @Override
    public String getOfficialPluginName() {
        return OFFICIAL_PLUGIN_NAME;
    }

    @Override
    public String getPluginVersion() {
        return PLUGIN_VERSION;
    }

    @Override
    public String getRequiredInternalAppName() {
        return FrameworkMain.INTERNAL_APP_NAME;
    }

    @Override
    public boolean checkLicense() {
        // iParts Lizenzoption
        return (LicenseConfig.getInstance() == null) || LicenseConfig.getInstance().licenseFunctionExists(LICENSE_KEY_IPARTS);
    }

    @Override
    public boolean checkApplicationMode(AbstractApplication.ApplicationMode applicationMode) {
        return applicationMode == AbstractApplication.ApplicationMode.J2EE_ONLINE;
    }

    @Override
    public Collection<String> getRequiredPluginClassNames() {
        return Arrays.asList(PLUGIN_CLASS_NAME_IPARTS);
    }

    @Override
    public LogChannels[] getPluginLogChannels() {
        return new LogChannels[]{ LOG_CHANNEL_DEBUG, LOG_CHANNEL_PERFORMANCE, LOG_CHANNEL_TOKEN };
    }

    @Override
    public void clearPluginCaches(ClusterEventInterface event) {
        super.clearPluginCaches(event);
        clearEndpointCaches();
    }

    @Override
    public void receiveProjectEvent(AbstractEtkProjectEvent event) {
        if (event instanceof DataChangedEvent) {
            clearEndpointCaches();
        } else if (event instanceof iPartsDataChangedEventByEdit) {
            for (FrameworkSimpleEndpoint simpleEndpoint : getSimpleEndpoints()) {
                iPartsDataChangedEventByEdit editEvent = (iPartsDataChangedEventByEdit)event;
                HttpCallback callback = simpleEndpoint.getCallback();
                if (callback instanceof iPartsWSAbstractEndpoint) {
                    ((iPartsWSAbstractEndpoint)callback).dataChangedByEdit(editEvent.getDataType());
                }

                // Ein oder mehrere Materialien wurden verändert -> iPartsWSPartsBaseCache aktualisieren
                if (editEvent.getDataType() == iPartsDataChangedEventByEdit.DataType.MATERIAL) {
                    if (editEvent.isClearDataTypeCache()) {
                        iPartsWSPartsBaseCache.clearCaches();
                    } else if ((editEvent.getElementIds() != null) && (editEvent.getAction() != iPartsDataChangedEventByEdit.Action.NEW)) {
                        for (PartId id : ((iPartsDataChangedEventByEdit<PartId>)event).getElementIds()) {
                            iPartsWSPartsBaseCache.removePartBaseFromCache(id.getMatNr());
                        }
                    }
                }
            }
        } else if (event instanceof NotesChangedEvent) {
            clearEndpointCaches();
        } else if (event instanceof iPartsPublishingEvent) {
            if (versionInfoEndpoint != null) {
                versionInfoEndpoint.clearCaches();
            }
        }
    }

    private void clearEndpointCaches() {
        if (isActiveState()) {
            for (FrameworkSimpleEndpoint simpleEndpoint : getSimpleEndpoints()) {
                HttpCallback callback = simpleEndpoint.getCallback();
                if (callback instanceof iPartsWSAbstractEndpoint) {
                    ((iPartsWSAbstractEndpoint)callback).clearCaches();
                }
            }
            iPartsWSFilteredPartListsCache.clearCaches();
            iPartsWSPartsBaseCache.clearCaches();
            iPartsFilterHelper.clearCache();
        }
    }

    private void addEndpointURIConfigOption(UniversalConfigOption endpointURIConfigOption, String endpointName, UniversalConfigurationPanel configPanel) {
        String webserviceUriText = TranslationHandler.translate("!!URI für Webservice '%1'", TranslationHandler.translate(endpointName));
        configPanel.addStringOption(endpointURIConfigOption, webserviceUriText, true).setValidator(new GuiControlEndpointUriPrefixValidator(webserviceUriText, true));
    }

    private void addEndpoint(iPartsWSAbstractEndpoint endpoint, UniversalConfigOption configOption, List<FrameworkSimpleEndpoint> endpoints) {
        FrameworkSimpleEndpoint.addEndpointWithEmptyURICheck(endpoint, configOption, endpoints, host, port, LOG_CHANNEL_DEBUG);
    }

    public static boolean isPartsListWSActive() {
        return isPartsListActive;
    }
}
