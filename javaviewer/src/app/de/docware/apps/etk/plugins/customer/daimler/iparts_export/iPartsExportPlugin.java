/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_export;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.misc.MenuManager;
import de.docware.apps.etk.plugins.AbstractJavaViewerSimpleEndpointPlugin;
import de.docware.apps.etk.plugins.EtkPluginConstants;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.exportpartslist.iPartsWSExportPartsListEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.iPartsWSAbstractExportEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.versioninfo.iPartsWSVersionInfoExportEndpoint;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsSaaDataXMLExporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsXMLDataExporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.helper.FastSearchConnectionHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.iPartsFastSearchIndexExportScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.iPartsExportScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.iPartsModelPartsListsExportTimer;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.iPartsSaaDataExportTimer;
import de.docware.apps.etk.plugins.interfaces.ModifyMenuInterface;
import de.docware.apps.etk.viewer.FrameworkMain;
import de.docware.framework.combimodules.config_gui.ConfigurationWindow;
import de.docware.framework.combimodules.config_gui.UniversalConfigurationPanel;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.defaultconfig.UniversalConfigOption;
import de.docware.framework.modules.config.defaultconfig.UniversalConfiguration;
import de.docware.framework.modules.config.license.LicenseConfig;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.filechooser.FileChooserPurpose;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserDialog;
import de.docware.framework.modules.gui.controls.menu.GuiMenuBarEntry;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.endpoint.FrameworkSimpleEndpoint;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.misc.validator.default_validators.GuiControlEndpointUriPrefixValidator;
import de.docware.framework.modules.webservice.restful.jwt.JWTKeystore;
import de.docware.framework.modules.webservice.restful.jwt.JWTKeystoreManager;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.security.PasswordString;

import java.io.File;
import java.util.*;

/**
 * iParts Plug-in für die Exporte.
 */
public class iPartsExportPlugin extends AbstractJavaViewerSimpleEndpointPlugin implements iPartsConst, ModifyMenuInterface {

    public static final String INTERNAL_PLUGIN_NAME = "iPartsExport";
    public static final String OFFICIAL_PLUGIN_NAME = "DAIMLER iParts Export Plug-in"; // absichtlich kein Übersetzungstext
    public static final String PLUGIN_VERSION = "1.0";

    public static final LogChannels LOG_CHANNEL_DEBUG = new LogChannels("DEBUG", true, true);

    public static final LogChannels LOG_CHANNEL_EXPORT = new LogChannels("EXPORT", true, true);

    public static final LogChannels LOG_CHANNEL_WS_DEBUG = new LogChannels("WS_DEBUG", false, true);
    public static final LogChannels LOG_CHANNEL_WS_PERFORMANCE = new LogChannels("WS_PERFORMANCE", false, true);
    public static final LogChannels LOG_CHANNEL_WS_TOKEN = new LogChannels("WS_TOKEN", false, true);
    public static final LogChannels LOG_CHANNEL_WS_EXPORT_MODEL_PARTSLISTS = new LogChannels("WS_EXPORT_MODEL_PARTSLISTS", true, true);
    public static final LogChannels LOG_CHANNEL_FAST_SEARCH = new LogChannels("FAST_SEARCH", true, true);

    // Export Webservices Authentifizierung
    public static final UniversalConfigOption CONFIG_EXPORT_HEADER_TOKEN_NAME = UniversalConfigOption.getStringOption("/exportHeaderTokenName", "authentication");
    public static final UniversalConfigOption CONFIG_EXPORT_HEADER_TOKEN_TYPE = UniversalConfigOption.getStringOption("/exportHeaderTokenType", "Bearer");
    public static final UniversalConfigOption CONFIG_EXPORT_HEADER_TOKEN_PASSWORD = UniversalConfigOption.getPasswordOption("/exportHeaderTokenPassword", PasswordString.EMPTY);
    public static final UniversalConfigOption CONFIG_EXPORT_HEADER_TOKEN_PUB_KEY_DIR = UniversalConfigOption.getFileOption("/exportHeaderTokenPubKeyDir", new File("jwtPublicKeysExport"));
    public static final UniversalConfigOption CONFIG_EXPORT_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING = UniversalConfigOption.getBooleanOption("/exportHeaderTokenPubKeyDirEnforcePolling", false);
    public static final UniversalConfigOption CONFIG_EXPORT_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME = UniversalConfigOption.getIntegerOption("/exportHeaderTokenPubKeyDirPollingTime", 10);
    public static final UniversalConfigOption CONFIG_EXPORT_HEADER_TOKEN_EXPIRES = UniversalConfigOption.getIntegerOption("/exportHeaderTokenExpires", 60 * 60); // 1 Stunde

    // Export Webservices URI-Konfiguration
    public static final UniversalConfigOption CONFIG_URI_EXPORT_VERSION_INFO = UniversalConfigOption.getStringOption("/exportVersionInfoURI", iPartsWSVersionInfoExportEndpoint.DEFAULT_ENDPOINT_URI);
    public static final UniversalConfigOption CONFIG_URI_EXPORT_PARTS_LIST_INFO = UniversalConfigOption.getStringOption("/exportPartsListURI", iPartsWSExportPartsListEndpoint.DEFAULT_ENDPOINT_URI);

    // Export-Scheduler-Konfiguration
    public static final UniversalConfigOption CONFIG_EXPORT_SCHEDULER_ACTIVE = UniversalConfigOption.getBooleanOption("/exportSchedulerActive", false);
    public static final UniversalConfigOption CONFIG_EXPORT_THREAD_COUNT = UniversalConfigOption.getIntegerOption("/exportThreadCount", 8);
    public static final UniversalConfigOption CONFIG_EXPORT_FILES_DIR = UniversalConfigOption.getFileOption("/exportFilesDir", new File("exportFiles"));
    public static final UniversalConfigOption CONFIG_EXPORT_VALID_PSK_CUSTOMERS = UniversalConfigOption.getStringAreaOption("/exportValidPSKCustomers", "");

    // Automatischer Export der SAAs
    public static final UniversalConfigOption CONFIG_SAA_DATA_EXPORT_ACTIVE = UniversalConfigOption.getBooleanOption("/saaDataExportActive", false);
    public static final UniversalConfigOption CONFIG_SAA_DATA_EXPORT_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/saaDataExportDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_SAA_DATA_EXPORT_TIME = UniversalConfigOption.getTimeOption("/saaDataExportTime", new Date(0)); // initial 00:00 GMT
    public static final UniversalConfigOption CONFIG_SAA_DATA_EXPORT_DIR = UniversalConfigOption.getFileOption("/saaDataExportDir", new File("saaDataExport"));

    // Automatischer Export der nach Baumustern gefilterten Stücklisten
    public static final UniversalConfigOption CONFIG_MODEL_DATA_EXPORT_ACTIVE = UniversalConfigOption.getBooleanOption("/modelDataExportActive", false);
    public static final UniversalConfigOption CONFIG_MODEL_DATA_EXPORT_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/modelDataExportDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_MODEL_DATA_EXPORT_TIME = UniversalConfigOption.getTimeOption("/modelDataExportTime", new Date(0)); // initial 00:00 GMT
    public static final UniversalConfigOption CONFIG_MODEL_DATA_EXPORT_DIR = UniversalConfigOption.getFileOption("/modelDataExportDir", new File("modelPartsListsExport"));
    public static final UniversalConfigOption CONFIG_MODEL_DATA_EXPORT_COMPRESSION_MODE = UniversalConfigOption.getStringListOptionSingleSelection("/modelDataExportCompressionMode", iPartsModelPartsListsExportTimer.CompressionMode.PRODUCT_FILES.getDisplayName()); // initial PRODUCT_FILES
    public static final UniversalConfigOption CONFIG_MODEL_DATA_EXPORT_PRODUCT_COUNT = UniversalConfigOption.getIntegerOption("/modelDataExportProductCount", 0);
    public static final UniversalConfigOption CONFIG_MODEL_DATA_EXPORT_THREAD_COUNT = UniversalConfigOption.getIntegerOption("/modelDataExportThreadCount", 4);

    // FastSearch Einstellungen (ElasticSearch)
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_EXPORT_ACTIVE = UniversalConfigOption.getBooleanOption("/fastSearchExportActive", false);
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_EXPORT_DAYS = UniversalConfigOption.getStringListOptionMultipleSelection("/fastSearchExportDays", new String[]{ DateUtils.DayOfWeek.SUNDAY.getDisplayName() }); // initial Sonntag
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_EXPORT_TIME = UniversalConfigOption.getTimeOption("/fastSearchExportTime", new Date(0)); // initial 00:00 GMT
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_INDEX_EXPORT_DIR = UniversalConfigOption.getFileOption("/fastSearchIndexExportDir", new File("fastSearchIndexExport"));
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_EXPORT_PRODUCT_COUNT = UniversalConfigOption.getIntegerOption("/fastSearchExportProductCount", 0);
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_EXPORT_MODEL_COUNT = UniversalConfigOption.getIntegerOption("/fastSearchExportModelCount", 10);
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_EXPORT_THREAD_COUNT = UniversalConfigOption.getIntegerOption("/fastSearchExportThreadCount", 4);
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_SUPPLY_THREAD_COUNT = UniversalConfigOption.getIntegerOption("/fastSearchSupplyThreadCount", 10);
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_EXPORT_BASE_URLS = UniversalConfigOption.getStringOption("/fastSearchExportBaseUrls", "");
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_EXPORT_FILES = UniversalConfigOption.getBooleanOption("/fastSearchExportFiles", false);
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_USE_REINDEX_METHOD = UniversalConfigOption.getBooleanOption("/fastSearchUseReindexMethod", false);
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_USE_TEST_MODE = UniversalConfigOption.getBooleanOption("/fastSearchUseTestMode", true);
    public static final UniversalConfigOption CONFIG_FAST_SEARCH_REINDEX_REFRESH_RATE = UniversalConfigOption.getIntegerOption("/fastSearchReindexRefreshRate", 10);

    // S3 Bucket für BM-Stücklisten-Export und FastSearch
    public static final UniversalConfigOption CONFIG_MODEL_DATA_FAST_SEARCH_S3_ACTIVE = UniversalConfigOption.getBooleanOption("/modelDataFastSearchS3Active", false);
    public static final UniversalConfigOption CONFIG_MODEL_DATA_FAST_SEARCH_S3_BUCKET_NAME = UniversalConfigOption.getStringOption("/modelDataFastSearchS3BucketName", "iparts-data-exchange-dev");
    public static final UniversalConfigOption CONFIG_MODEL_DATA_FAST_SEARCH_S3_ACCESS_KEY = UniversalConfigOption.getStringOption("/modelDataFastSearchS3AccessKey", "");
    public static final UniversalConfigOption CONFIG_MODEL_DATA_FAST_SEARCH_S3_SECRET_ACCESS_KEY = UniversalConfigOption.getPasswordOption("/modelDataFastSearchS3SecretAccessKey", PasswordString.EMPTY);

    private static UniversalConfiguration pluginConfig;

    private static JWTKeystoreManager keystoreManager;
    private static iPartsSaaDataExportTimer saaDataExportTimer;
    private static iPartsModelPartsListsExportTimer modelPartsListsExportTimer;
    private static iPartsFastSearchIndexExportScheduler fastSearchIndexExportTimer;

    private String host;
    private int port;

    /**
     * Liefert den {@link JWTKeystoreManager} für die Export Webservices zurück. Wird nur für die Unit-Tests benötigt.
     *
     * @return
     */
    public static JWTKeystoreManager getKeystoreManager() {
        return keystoreManager;
    }

    public static List<JWTKeystore> getKeystores() {
        return keystoreManager.getKeystores();
    }

    public static UniversalConfiguration getPluginConfig() {
        return pluginConfig;
    }

    public iPartsExportPlugin() {
    }

    /**
     * Konstruktor für die Unit-Tests.
     *
     * @param host
     * @param port
     */
    public iPartsExportPlugin(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static boolean isExportSchedulerActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_EXPORT_SCHEDULER_ACTIVE);
    }

    public static DWFile getDirForExport() {
        if (isExportSchedulerActive()) {
            return iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_EXPORT_FILES_DIR, LOG_CHANNEL_EXPORT);
        }
        return null;
    }

    public static boolean isSaaDataExportActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_SAA_DATA_EXPORT_ACTIVE);
    }

    public static DWFile getDirForSaaDataExport() {
        if (isSaaDataExportActive()) {
            return iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_SAA_DATA_EXPORT_DIR, LOG_CHANNEL_EXPORT);
        }
        return null;
    }

    public static boolean isModelPartsListsExportActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_MODEL_DATA_EXPORT_ACTIVE);
    }

    public static DWFile getDirForModelPartsListsExport() {
        if (isModelPartsListsExportActive()) {
            return iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_MODEL_DATA_EXPORT_DIR, LOG_CHANNEL_EXPORT);
        }
        return null;
    }

    public static iPartsModelPartsListsExportTimer.CompressionMode getSelectedCompressionMode() {
        String compressionMode = getPluginConfig().getConfigValueAsString(CONFIG_MODEL_DATA_EXPORT_COMPRESSION_MODE);
        return iPartsModelPartsListsExportTimer.CompressionMode.getFromDisplayName(compressionMode);
    }

    public static boolean isFastSearchIndexExportActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_FAST_SEARCH_EXPORT_ACTIVE);
    }

    public static boolean isFastSearchExportToFileActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_FAST_SEARCH_EXPORT_FILES);
    }

    public static boolean useFastSearchReindexMethod() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_FAST_SEARCH_USE_REINDEX_METHOD);
    }

    public static boolean isFastSearchTestModeActive() {
        return getPluginConfig().getConfigValueAsBoolean(CONFIG_FAST_SEARCH_USE_TEST_MODE);
    }

    public static DWFile getDirForFastSearchIndexExport() {
        if (isFastSearchIndexExportActive()) {
            return iPartsPlugin.getDirForConfigOption(getPluginConfig(), CONFIG_FAST_SEARCH_INDEX_EXPORT_DIR, LOG_CHANNEL_EXPORT);
        }
        return null;
    }

    public static int getFastSearchIndexExportThreadCount() {
        return getPluginConfig().getConfigValueAsInteger(CONFIG_FAST_SEARCH_EXPORT_THREAD_COUNT);
    }

    public static int getFastSearchIndexSupplyThreadCount() {
        return getPluginConfig().getConfigValueAsInteger(CONFIG_FAST_SEARCH_SUPPLY_THREAD_COUNT);
    }

    public static int getFastSearchReindexRefreshRate() {
        return getPluginConfig().getConfigValueAsInteger(CONFIG_FAST_SEARCH_REINDEX_REFRESH_RATE);
    }

    public static String getFastSearchBaseURLs() {
        return getPluginConfig().getConfigValueAsString(CONFIG_FAST_SEARCH_EXPORT_BASE_URLS);
    }

    public static boolean isExportPSKCustomerID(String customerId) {
        if (StrUtils.isValid(customerId)) {
            String customerIdsString = getPluginConfig().getConfigValueAsString(iPartsExportPlugin.CONFIG_EXPORT_VALID_PSK_CUSTOMERS);
            if (StrUtils.isValid(customerIdsString)) {
                Set<String> customerIds = new HashSet<>(StrUtils.toStringListContainingDelimiterAndBlanks(customerIdsString.toUpperCase(),
                                                                                                        ";", false));
                if (!customerIds.isEmpty()) {
                    return customerIds.contains(customerId.toUpperCase());
                }
            }
        }
        return false;
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
    public UniversalConfigurationPanel getConfigurationPanel(ConfigurationWindow host) {
        UniversalConfigurationPanel configurationPanel = new UniversalConfigurationPanel(host, pluginConfig, OFFICIAL_PLUGIN_NAME, true);

        // Export Webservices Authentifizierung
        configurationPanel.startGroup("!!EXPORT Webservice-Token im Request-Header");
        configurationPanel.addStringOption(CONFIG_EXPORT_HEADER_TOKEN_NAME, "!!Name", true);
        configurationPanel.addStringOption(CONFIG_EXPORT_HEADER_TOKEN_TYPE, "!!Typ", true);
        configurationPanel.addPasswordOption(CONFIG_EXPORT_HEADER_TOKEN_PASSWORD, "!!Passwort (für HS256 Verfahren; wenn leer ist HS256 nicht zugelassen)", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_EXPORT_HEADER_TOKEN_EXPIRES, "!!Erlaubte Differenz in Sekunden", true,
                                                   1, 100000, 10);
        {
            configurationPanel.startGroup("!!Überwachtes Verzeichnis für Public Key Definitionen im JSON-Format (für RS256 Verfahren)");
            configurationPanel.addFileOption(CONFIG_EXPORT_HEADER_TOKEN_PUB_KEY_DIR, "!!Verzeichnis", false,
                                             GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.OPEN, "!!Auswählen");
            configurationPanel.addBooleanOption(CONFIG_EXPORT_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING, "!!Polling erzwingen (nötig z.B. für NFS Shares)", false);
            configurationPanel.addIntegerSpinnerOption(CONFIG_EXPORT_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME, "!!Polling Intervall (in min)", false, 1, 1000, 1);
            configurationPanel.endGroup();
        }
        configurationPanel.endGroup();

        // Export Webservices
        configurationPanel.startGroup("!!Export Webservices");
        addExportEndpointURIConfigOption(CONFIG_URI_EXPORT_VERSION_INFO, "VersionInfo", configurationPanel);
        addExportEndpointURIConfigOption(CONFIG_URI_EXPORT_PARTS_LIST_INFO, "ExportPartsList", configurationPanel);
        configurationPanel.endGroup();

        configurationPanel.startGroup("!!Automatischer Stücklisten-Export");
        configurationPanel.addBooleanOption(CONFIG_EXPORT_SCHEDULER_ACTIVE, "!!Automatisches Verarbeiten von Export-Anfragen", true);
        configurationPanel.addIntegerSpinnerOption(CONFIG_EXPORT_THREAD_COUNT, "!!Maximale Anzahl an Export-Threads", true,
                                                   1, 128, 1);
        configurationPanel.addFileOption(CONFIG_EXPORT_FILES_DIR, "!!Verzeichnis für Export-Dateien", true, GuiFileChooserDialog.FILE_MODE_DIRECTORIES,
                                         FileChooserPurpose.SAVE, "!!Auswählen");
        configurationPanel.addStringAreaOption(CONFIG_EXPORT_VALID_PSK_CUSTOMERS, "!!Kunden-IDs für PSK-Inhalte", false, true)
                .setTooltip("!!Kunden-IDs durch \";\" getrennt");
        configurationPanel.endGroup();

        // Automatischer Export
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Automatischer Export der SA - SAA Benennungen",
                                               CONFIG_SAA_DATA_EXPORT_ACTIVE, CONFIG_SAA_DATA_EXPORT_DAYS, CONFIG_SAA_DATA_EXPORT_TIME, false);
        configurationPanel.addFileOption(CONFIG_SAA_DATA_EXPORT_DIR, "!!Verzeichnis für Export-Dateien", true,
                                         GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.SAVE, "!!Auswählen");
        configurationPanel.endGroup();

        // Automatischer Baumuster-Stücklisten-Export
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, "!!Automatischer Export der nach Baumustern gefilterten Stücklisten",
                                               CONFIG_MODEL_DATA_EXPORT_ACTIVE, CONFIG_MODEL_DATA_EXPORT_DAYS, CONFIG_MODEL_DATA_EXPORT_TIME, false);
        configurationPanel.addFileOption(CONFIG_MODEL_DATA_EXPORT_DIR, "!!Verzeichnis für Export-Dateien", true,
                                         GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.SAVE, "!!Auswählen");
        configurationPanel.addStringListOptionSingleSelection(CONFIG_MODEL_DATA_EXPORT_COMPRESSION_MODE, "!!Kompressions-Methode (irrelevant bei aktivem S3 Object Store)",
                                                              true, iPartsModelPartsListsExportTimer.CompressionMode.getDisplayNames(), false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_MODEL_DATA_EXPORT_PRODUCT_COUNT, "!!Anzahl der Produkte pro MBAG/DTAG, die behandelt werden sollen (0 für alle)",
                                                   true, 0, 10000, 1);
        configurationPanel.addIntegerSpinnerOption(CONFIG_MODEL_DATA_EXPORT_THREAD_COUNT, "!!Maximale Anzahl an Export-Threads",
                                                   true, 1, 128, 1);
        configurationPanel.endGroup();

        // FastSearch Konfiguration
        iPartsPlugin.createDayOfWeekTimerGroup(configurationPanel, iPartsFastSearchIndexExportScheduler.TITLE,
                                               CONFIG_FAST_SEARCH_EXPORT_ACTIVE, CONFIG_FAST_SEARCH_EXPORT_DAYS, CONFIG_FAST_SEARCH_EXPORT_TIME, false);
        configurationPanel.addFileOption(CONFIG_FAST_SEARCH_INDEX_EXPORT_DIR, "!!Verzeichnis für ElasticSearch-Index-Dateien", true,
                                         GuiFileChooserDialog.FILE_MODE_DIRECTORIES, FileChooserPurpose.SAVE, "!!Auswählen");
        configurationPanel.addIntegerSpinnerOption(CONFIG_FAST_SEARCH_EXPORT_PRODUCT_COUNT, "!!Anzahl der Produkte pro MBAG/DTAG, die behandelt werden sollen (0 für alle)",
                                                   true, 0, 10000, 1);
        configurationPanel.addIntegerSpinnerOption(CONFIG_FAST_SEARCH_EXPORT_MODEL_COUNT, "!!Anzahl der Baumuster pro Produkt, die gemeinsam berechnet und versorgt werden sollen (0 für alle zu einem Produkt)",
                                                   true, 0, 10000, 1);
        configurationPanel.addIntegerSpinnerOption(CONFIG_FAST_SEARCH_EXPORT_THREAD_COUNT, "!!Maximale Anzahl an Export-Threads",
                                                   true, 1, 128, 1);
        configurationPanel.addIntegerSpinnerOption(CONFIG_FAST_SEARCH_SUPPLY_THREAD_COUNT, "!!Maximale Anzahl an Threads für die Versorgung",
                                                   true, 1, 128, 1);
        configurationPanel.addStringOption(CONFIG_FAST_SEARCH_EXPORT_BASE_URLS, "!!Cluster Basis-URLs inkl. Ports (kommasepariert)",
                                           false).setTooltip("!!Wenn leer, kann kein Export stattfinden");
        configurationPanel.addBooleanOption(CONFIG_FAST_SEARCH_EXPORT_FILES, "!!JSON Dateien im Verzeichnis ablegen", false);
        configurationPanel.startGroup("!!Reindex Methode");
        configurationPanel.addBooleanOption(CONFIG_FAST_SEARCH_USE_REINDEX_METHOD, "!!Aktiv", false);
        configurationPanel.addIntegerSpinnerOption(CONFIG_FAST_SEARCH_REINDEX_REFRESH_RATE, "!!Anzahl BM nach denen bei der _bulk Operation der Index aktualisiert werden soll",
                                                   false, 0, 10000, 1);
        configurationPanel.endGroup();
        configurationPanel.addBooleanOption(CONFIG_FAST_SEARCH_USE_TEST_MODE, "!!Test-Modus (falls aktiv, werden Daten in temporäre Indizes geschrieben)", false);
        configurationPanel.endGroup();

        // S3 Bucket für BM-Stücklisten-Export und FastSearch
        configurationPanel.startGroup("!!S3 Object Store für automatischen Export der gefilterten Stücklisten und ElasticSearch-Indizes");
        configurationPanel.addBooleanOption(CONFIG_MODEL_DATA_FAST_SEARCH_S3_ACTIVE, "!!Aktiv", false);
        configurationPanel.startGroup("!!Object Store Zugangsdaten");
        configurationPanel.addStringOption(CONFIG_MODEL_DATA_FAST_SEARCH_S3_BUCKET_NAME, "!!Bucket Name", false).setTooltip("!!Bucket Name ohne Prefix");
        configurationPanel.addStringOption(CONFIG_MODEL_DATA_FAST_SEARCH_S3_ACCESS_KEY, "!!Access Key", false);
        configurationPanel.addPasswordOption(CONFIG_MODEL_DATA_FAST_SEARCH_S3_SECRET_ACCESS_KEY, "!!Secret Access Key", false);
        configurationPanel.endGroup();
        configurationPanel.endGroup();

        return configurationPanel;
    }

    @Override
    public Collection<String> getRequiredPluginClassNames() {
        return Arrays.asList(PLUGIN_CLASS_NAME_IPARTS);
    }

    @Override
    public void initPlugin(ConfigBase config) {
        initPluginBase(config, EtkPluginConstants.XML_CONFIG_PATH_BASE + '/' + INTERNAL_PLUGIN_NAME);
        pluginConfig = new UniversalConfiguration(config, getConfigPath());
        keystoreManager = new JWTKeystoreManager("Export webservices", pluginConfig, CONFIG_EXPORT_HEADER_TOKEN_PUB_KEY_DIR,
                                                 CONFIG_EXPORT_HEADER_TOKEN_PUB_KEY_DIR_ENFORCE_POLLING,
                                                 CONFIG_EXPORT_HEADER_TOKEN_PUB_KEY_DIR_POLLING_TIME, LOG_CHANNEL_WS_TOKEN);
    }

    @Override
    public void applicationStarted(boolean firstInit) {
        super.applicationStarted(firstInit);

        if (isActive() && AbstractApplication.isOnline()) {
            keystoreManager.startKeystoreDirectoryMonitor();
            if (isExportSchedulerActive()) {
                iPartsExportScheduler.getInstance().start(10);
            }
            saaDataExportTimer = new iPartsSaaDataExportTimer(iPartsPlugin.getMqProject(), iPartsPlugin.getMqSession());
            if (isSaaDataExportActive()) {
                restartSaaDataExportThread();
            }
            modelPartsListsExportTimer = new iPartsModelPartsListsExportTimer(iPartsPlugin.getMqProject(), iPartsPlugin.getMqSession());
            if (isModelPartsListsExportActive()) {
                restartModelDataExportThread();
            }
            fastSearchIndexExportTimer = new iPartsFastSearchIndexExportScheduler(iPartsPlugin.getMqProject(), iPartsPlugin.getMqSession());
            fastSearchIndexExportTimer.setExportOnlyModifiedProducts(true);
            if (isFastSearchIndexExportActive()) {
                restartFastSearchIndexExportThread();
            }
        }
    }

    @Override
    public void configurationChanged() {
        super.configurationChanged();
        keystoreManager.stopKeystoreDirectoryMonitor();
        iPartsExportScheduler.getInstance().stop();
        if (isActive()) {
            if (AbstractApplication.isOnline()) {
                keystoreManager.startKeystoreDirectoryMonitor();
                if (isExportSchedulerActive()) {
                    iPartsExportScheduler.getInstance().start(10);
                }
            }

            restartSaaDataExportThread();
            restartModelDataExportThread();
            restartFastSearchIndexExportThread();
        }
    }

    @Override
    public void releaseReferences() {
        iPartsExportScheduler.getInstance().stop();
        if (keystoreManager != null) {
            keystoreManager.stopKeystoreDirectoryMonitor();
        }
        super.releaseReferences();
    }

    @Override
    public boolean setActiveState(boolean active) {
        boolean activeStateChanged = super.setActiveState(active);
        if (activeStateChanged) {
            if (saaDataExportTimer != null) {
                if (active) {
                    restartSaaDataExportThread();
                } else {
                    saaDataExportTimer.stopThread();
                }
            }
            if (modelPartsListsExportTimer != null) {
                if (active) {
                    restartModelDataExportThread();
                } else {
                    modelPartsListsExportTimer.stopThread();
                }
            }
            if (fastSearchIndexExportTimer != null) {
                if (active) {
                    restartFastSearchIndexExportThread();
                } else {
                    fastSearchIndexExportTimer.stopThread();
                }
            }
        }

        return activeStateChanged;
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
    public LogChannels[] getPluginLogChannels() {
        return new LogChannels[]{ LOG_CHANNEL_DEBUG, LOG_CHANNEL_EXPORT, LOG_CHANNEL_WS_DEBUG, LOG_CHANNEL_WS_PERFORMANCE,
                                  LOG_CHANNEL_WS_TOKEN, LOG_CHANNEL_WS_EXPORT_MODEL_PARTSLISTS, LOG_CHANNEL_FAST_SEARCH };
    }

    @Override
    public void modifyMenu(MenuManager manager) {
        if (iPartsRight.EXPORT_PARTS_DATA.checkRightInSession()) {
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_EXPORT, "!!Stücklisten (Baumusterauswahl)...", null, false,
                                          exportPartLists(iPartsModelId.TYPE),
                                          MenuManager.MENU_NAME_NOTES);
            manager.addMenuFunction(IPARTS_MENU_NAME_EXPORT, "!!Stücklisten (SA)...", null, false, exportPartLists(iPartsSaId.TYPE));

            // Export der nach Baumustern gefilterten Stücklisten
            GuiMenuBarEntry menuBarEntry = manager.getOrAddMainMenu(IPARTS_MENU_NAME_EXPORT);
            menuBarEntry.addChild(new GuiSeparator());
            manager.addMenuFunction(IPARTS_MENU_NAME_EXPORT, iPartsModelPartsListsExportTimer.TITLE + "...", null, false,
                                    exportModelPartsLists());
            menuBarEntry.addChild(new GuiSeparator());
            manager.addMenuFunction(IPARTS_MENU_NAME_EXPORT, iPartsFastSearchIndexExportScheduler.TITLE + " für alle Produkte...", null, false,
                                    exportFastSearchIndex());
        }
        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!Export-Scheduler neu starten", null, false, restartExportScheduler(),
                                      MenuManager.MENU_NAME_HELP);

        // Separator
        manager.addMenuFunctionAfter(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
        manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!FastSearch Verbindung überprüfen (Basis-URLs)", null, false, checkFastSearchConnection(),
                                      MenuManager.MENU_NAME_HELP);

        if (iPartsRight.EXPORT_PARTS_DATA.checkRightInSession()) {
            // Export der SA - SAA Benennungen
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, null, null, false, null, MenuManager.MENU_NAME_HELP);
            manager.addMenuFunctionBefore(IPARTS_MENU_NAME_TEST, "!!SA - SAA Benennungen exportieren...", null, false, exportSaas(), MenuManager.MENU_NAME_HELP);
        }
    }

    private EtkFunction checkFastSearchConnection() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                FastSearchConnectionHelper.doURLCheckWithMessage();
            }
        };
    }

    private EtkFunction exportModelPartsLists() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                if (iPartsExportPlugin.isModelPartsListsExportActive()) {
                    if (modelPartsListsExportTimer != null) {
                        if (!iPartsModelPartsListsExportTimer.isModelExportRunning(iPartsPlugin.getMqProject())) {
                            modelPartsListsExportTimer.stopThread();
                        }
                    }
                    iPartsModelPartsListsExportTimer.doRunModelDataScheduler(iPartsPlugin.getMqProject(), iPartsPlugin.getMqSession());
                    if (modelPartsListsExportTimer != null) {
                        if (!iPartsModelPartsListsExportTimer.isModelExportRunning(iPartsPlugin.getMqProject())) {
                            restartModelDataExportThread();
                        }
                    }
                } else {
                    MessageDialog.show(TranslationHandler.translate("%1 ist nicht aktiv.", TranslationHandler.translate(iPartsModelPartsListsExportTimer.TITLE)));
                }
            }
        };
    }

    private EtkFunction exportFastSearchIndex() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                if (iPartsExportPlugin.isFastSearchIndexExportActive()) {
                    if (fastSearchIndexExportTimer != null) {
                        if (!iPartsFastSearchIndexExportScheduler.isFastSearchIndexExportRunning(iPartsPlugin.getMqProject())) {
                            fastSearchIndexExportTimer.stopThread();
                        }
                    }
                    iPartsFastSearchIndexExportScheduler.doRunFastSearchIndexExportWithMessages(iPartsPlugin.getMqProject(),
                                                                                                iPartsPlugin.getMqSession());
                    if (fastSearchIndexExportTimer != null) {
                        if (!iPartsFastSearchIndexExportScheduler.isFastSearchIndexExportRunning(iPartsPlugin.getMqProject())) {
                            restartFastSearchIndexExportThread();
                        }
                    }
                } else {
                    MessageDialog.show(TranslationHandler.translate("%1 ist nicht aktiv.",
                                                                    TranslationHandler.translate(iPartsFastSearchIndexExportScheduler.TITLE)));
                }
            }
        };
    }

    private EtkFunction exportSaas() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                iPartsSaaDataXMLExporter exporter = new iPartsSaaDataXMLExporter(owner.getProject());
                exporter.exportWithMessageLogForm();
            }
        };
    }

    @Override
    public List<FrameworkSimpleEndpoint> createSimpleEndpoints() {
        List<FrameworkSimpleEndpoint> endpoints = new ArrayList<>();
        iPartsWSVersionInfoExportEndpoint versionInfoEndpoint = new iPartsWSVersionInfoExportEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_EXPORT_VERSION_INFO));
        addExportEndpoint(versionInfoEndpoint, CONFIG_URI_EXPORT_VERSION_INFO, endpoints);
        iPartsWSExportPartsListEndpoint exportPartsListEndpoint = new iPartsWSExportPartsListEndpoint(pluginConfig.getConfigValueAsString(CONFIG_URI_EXPORT_PARTS_LIST_INFO));
        addExportEndpoint(exportPartsListEndpoint, CONFIG_URI_EXPORT_PARTS_LIST_INFO, endpoints);
        return endpoints;
    }

    private void addExportEndpointURIConfigOption(UniversalConfigOption endpointURIConfigOption, String endpointName, UniversalConfigurationPanel configPanel) {
        String webserviceUriText = TranslationHandler.translate("!!URI für Export Webservice '%1'", TranslationHandler.translate(endpointName));
        configPanel.addStringOption(endpointURIConfigOption, webserviceUriText, false).setValidator(new GuiControlEndpointUriPrefixValidator(webserviceUriText, true));
    }

    private void addExportEndpoint(iPartsWSAbstractExportEndpoint endpoint, UniversalConfigOption configOption, List<FrameworkSimpleEndpoint> endpoints) {
        FrameworkSimpleEndpoint.addEndpointWithEmptyURICheck(endpoint, configOption, endpoints, host, port, LOG_CHANNEL_DEBUG);
    }

    private EtkFunction exportPartLists(final String exportDataObjectType) {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                iPartsXMLDataExporter exporter = new iPartsXMLDataExporter(getProject());
                exporter.exportData(owner, exportDataObjectType);
            }
        };
    }

    private EtkFunction restartExportScheduler() {
        return new EtkFunction() {
            @Override
            public void run(AbstractJavaViewerForm owner) {
                // Scheduler Neustart nur zulassen, wenn er grundsätzlich aktiv sein soll
                if (isExportSchedulerActive()) {
                    iPartsExportScheduler exportScheduler = iPartsExportScheduler.getInstance();
                    if (exportScheduler == null) {
                        MessageDialog.show(TranslationHandler.translate("!!Export-Scheduler konnte nicht neugestartet werden, da er nicht initialisiert wurde."));
                        return;
                    }
                    exportScheduler.stop();
                    exportScheduler.start(0);
                    if (!exportScheduler.isRunning()) {
                        MessageDialog.show(TranslationHandler.translate("!!Export-Scheduler konnte nicht neugestartet werden."));
                    }
                } else {
                    MessageDialog.show(TranslationHandler.translate("!!Export-Scheduler wurde nicht neugestartet da er per " +
                                                                    "Konfiguration abgeschaltet ist."));
                }
            }
        };
    }

    public static void restartSaaDataExportThread() {
        iPartsPlugin.restartTimerThread(saaDataExportTimer, pluginConfig, CONFIG_SAA_DATA_EXPORT_ACTIVE, CONFIG_SAA_DATA_EXPORT_DAYS,
                                        CONFIG_SAA_DATA_EXPORT_TIME, LOG_CHANNEL_EXPORT);
    }

    public static void restartModelDataExportThread() {
        iPartsPlugin.restartTimerThread(modelPartsListsExportTimer, pluginConfig, CONFIG_MODEL_DATA_EXPORT_ACTIVE, CONFIG_MODEL_DATA_EXPORT_DAYS,
                                        CONFIG_MODEL_DATA_EXPORT_TIME, LOG_CHANNEL_WS_EXPORT_MODEL_PARTSLISTS);
    }

    public static void restartFastSearchIndexExportThread() {
        iPartsPlugin.restartTimerThread(fastSearchIndexExportTimer, pluginConfig, CONFIG_FAST_SEARCH_EXPORT_ACTIVE, CONFIG_FAST_SEARCH_EXPORT_DAYS,
                                        CONFIG_FAST_SEARCH_EXPORT_TIME, LOG_CHANNEL_FAST_SEARCH);
    }
}
