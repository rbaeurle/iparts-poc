/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.tests;

import de.docware.apps.etk.base.webservice.AbstractTestRESTfulWebservice;
import de.docware.apps.etk.plugins.customer.daimler.iparts.application.iPartsFrameworkMain;
import de.docware.apps.etk.plugins.customer.daimler.iparts.application.iPartsJavaViewerApplication;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.viewer.FrameworkMain;
import de.docware.framework.modules.config.ConfigBase;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.ContentTypes;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.startparameter.StartParameter;
import de.docware.framework.modules.webservice.restful.RESTfulEndpoint;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.os.OsUtils;
import de.docware.util.test.AbstractTest;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Abstrakte Klasse für Unit-Tests von iParts Webservices.
 */
public abstract class AbstractTestiPartsWSEndpoint extends AbstractTestRESTfulWebservice {

    // Per Java Umgebungsvariable Tests aus lokalem Verzeichnis ausführen
    public static final String LOCAL_DIR = "D:/DataNoS/JAVA_UnitTests";
    public static final String VM_PARAMETER_USE_LOCAL_DIR = "dw_TestiPartsWebservices_useLocalDir";

    private static final String WEBSERVICE_HOST = "localhost";
    private static final int WEBSERVICE_PORT = 1235; // ist laut Wikipedia normalerweise frei

    protected AbstractTestiPartsWSEndpoint() {
        setUseGlobalTest(true);
        addExcludedDirs();
    }

    protected AbstractTestiPartsWSEndpoint(AbstractTest globalTest, String methodName) {
        super(globalTest, methodName);
        addExcludedDirs();
    }

    @Override
    protected FrameworkMain createApplicationFrameworkMain() {
        return new iPartsFrameworkMain(new File(OsUtils.getBaseDirPath()));
    }

    @Override
    protected void createApplicationInstance(ConfigBase configuration) {
        iPartsJavaViewerApplication.createInstance(configuration);
    }

    @Override
    protected String getConfigFileName() {
        return "etk_viewer.config";
    }

    @Override
    protected String getDwkFileName() {
        return null; // DWK aus Konfiguration übernehmen
    }

    @Override
    protected String getWebserviceHost() {
        return WEBSERVICE_HOST;
    }

    @Override
    protected int getWebservicePort() {
        return WEBSERVICE_PORT;
    }

    // createTempDir() überschreiben, wenn man die Daten direkt von einem lokalen Verzeichnis ohne Kopieren in ein temporäres
    // Testverzeichnis verwenden will
    @Override
    protected DWFile createTempDir() {
        if (StartParameter.getSystemPropertyBoolean(VM_PARAMETER_USE_LOCAL_DIR, false)) {
            setDeleteTmpBaseDir(false);
            return DWFile.get(LOCAL_DIR).getChild(getTestCaseRelativeDir());
        } else {
            useTestCaseFilesFromVCS();
            return super.createTempDir();
        }
    }

    @Override
    protected String getTestCaseRelativeDir() {
        return ("de_docware_apps_etk_plugins_customer_daimler_iparts_webservice_iPartsWebservicePlugin" + "_V" + getTestVersion());
    }

    @Override
    protected String getTestSuite() {
        return iPartsPlugin.INTERNAL_PLUGIN_NAME;
    }

    @Override
    public PostgresCloneParams getPostgresCloneParams() {
        return AbstractTestSuiteWithIPartsProject.createPostgresCloneParamsForIPartsWebserviceUnittestDB(getDefaultCloneAlias());
    }

    @Override
    protected boolean isReadOnlyCloneDB() {
        return true;
    }

    protected void addExcludedDirs() {
        addExcludedFileOrDirForCopyToTmpDir("importLogs");
        addExcludedFileOrDirForCopyToTmpDir("logs");
        if (isUsingClone()) {
            addExcludedFileOrDirForCopyToTmpDir("Data"); // H2 nicht kopieren, wenn mit Oracle-Klonen gearbeitet wird
        }
    }

    @Override
    protected int getConnectTimeoutMs() {
        return 300000; // 5 Minuten wegen langsamer H2 DB bei komplexen Suchen
    }

    @Override
    public void globalSetUp() throws Exception {
        super.globalSetUp();

        // Interessante Logkanäle aktivieren
        Logger.getLogger().addChannel(LogChannels.SQL_LONG_DURATION);
        Logger.getLogger().addChannel(LogChannels.DB_PERFORMANCE);
    }

    /**
     * Führt den Webservice mit Request-Methode POST aus mit dem übergebenen <i>requestString</i> (bzw. mit GET falls
     * dieser {@code null} ist) und vergleicht das Ergebnis mit der übergebenen Datei (UTF-8 ohne BOM) unter der Annahme,
     * dass der HTTP Ergebniscode {@link HttpConstants#HTTP_STATUS_OK} ist.
     *
     * @param endpointURI        Relative Endpoint-URI
     * @param requestString      JSON Request; Request-Methode GET falls {@code null} oder leer, sonst POST
     * @param expectedResultFile JSON Response-Inhalt (UTF-8 ohne BOM) in einer Datei ohne \n bei {@link HttpConstants#HTTP_STATUS_OK},
     *                           ansonsten Response-Message
     */
    protected void executeWebservice(String endpointURI, String requestString, DWFile expectedResultFile) {
        String expectedResponseString = getExpectedResponseString(expectedResultFile);
        executeWebservice(endpointURI, requestString, expectedResponseString);
    }

    /**
     * Liefert abhängig vom Inhalt der übergebenen Datei (UTF-8 ohne BOM), die erwartete Antwort des Webservices
     *
     * @param expectedResultFile JSON Response-Inhalt (UTF-8 ohne BOM) in einer Datei ohne \n bei {@link HttpConstants#HTTP_STATUS_OK},
     *                           ansonsten Response-Message
     * @return
     */
    public String getExpectedResponseString(DWFile expectedResultFile) {
        if ((expectedResultFile != null) && expectedResultFile.exists()) {
            try {
                return StrUtils.removeLastCharacterIfCharacterIs(expectedResultFile.readTextFile(DWFileCoding.UTF8), '\n');
            } catch (IOException e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        } else {
            Logger.getLogger().throwRuntimeException("File not valid: " + expectedResultFile);
        }
        return "";
    }

    /**
     * Führt den Webservice mit Request-Methode POST aus mit dem übergebenen <i>requestString</i> (bzw. mit GET falls
     * dieser {@code null} ist) und vergleicht das Ergebnis mit dem übergebenen <i>expectedResponseString</i> unter
     * der Annahme, dass der HTTP Ergebniscode {@link HttpConstants#HTTP_STATUS_OK} ist.
     *
     * @param endpointURI            Relative Endpoint-URI
     * @param requestString          JSON Request; Request-Methode GET falls {@code null} oder leer, sonst POST
     * @param expectedResponseString JSON Response-Inhalt bei {@link HttpConstants#HTTP_STATUS_OK}, ansonsten Response-Message
     */
    protected void executeWebservice(String endpointURI, String requestString, String expectedResponseString) {
        executeWebservice(endpointURI, requestString, expectedResponseString, HttpConstants.HTTP_STATUS_OK);
    }

    /**
     * Führt den Webservice mit Request-Methode POST aus mit dem übergebenen <i>requestString</i> (bzw. mit GET falls
     * dieser {@code null} ist) und vergleicht das Ergebnis mit dem übergebenen <i>expectedResponseString</i> sowie
     * den Ergebniscode mit <i>expectedResponseCode</i>.
     *
     * @param endpointURI            Relative Endpoint-URI
     * @param requestString          JSON Request; Request-Methode GET falls {@code null} oder leer, sonst POST
     * @param expectedResponseString JSON Response-Inhalt bei {@link HttpConstants#HTTP_STATUS_OK}, ansonsten Response-Message
     * @param expectedResponseCode   Erwarteter HTTP Ergebniscode
     */
    protected void executeWebservice(String endpointURI, String requestString, String expectedResponseString, int expectedResponseCode) {
        executeWebservice(endpointURI, requestString, null, expectedResponseString, expectedResponseCode);
    }

    protected void executeWebservice(String endpointURI, String requestString, Map<String, String> additionalRequestProperties,
                                     String expectedResponseString, int expectedResponseCode) {
        executeWebservice(endpointURI, null, requestString, additionalRequestProperties, expectedResponseString, expectedResponseCode);
    }


    /**
     * Führt den Webservice mit Request-Methode POST aus mit dem übergebenen <i>requestString</i> (bzw. mit GET falls
     * dieser {@code null} ist) und vergleicht das Ergebnis mit dem übergebenen <i>expectedResponseString</i> sowie
     * den Ergebniscode mit <i>expectedResponseCode</i>.
     *
     * @param endpointURI                 Relative Endpoint-URI
     * @param httpMethod                  Optionale HTTP-Methode
     * @param requestString               JSON Request; Request-Methode GET falls {@code null} oder leer, sonst POST
     * @param additionalRequestProperties Map mit optionalen zusätzlichen Request-Eigenschaften (z.B. für JWT Token)
     * @param expectedResponseString      JSON Response-Inhalt bei {@link HttpConstants#HTTP_STATUS_OK}, ansonsten Response-Message
     * @param expectedResponseCode        Erwarteter HTTP Ergebniscode
     */
    protected void executeWebservice(String endpointURI, RESTfulEndpoint.HttpMethod httpMethod, String requestString,
                                     Map<String, String> additionalRequestProperties, String expectedResponseString,
                                     int expectedResponseCode) {
        long startTime = System.currentTimeMillis();

        executeWebservice(endpointURI, httpMethod, ContentTypes.get(MimeTypes.MIME_TYPE_JSON, ContentTypes.Charset.UTF8), requestString,
                          MimeTypes.MIME_TYPE_JSON, additionalRequestProperties, expectedResponseString, expectedResponseCode);

        String durationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true, false,
                                                                   Language.EN.getCode());
        System.out.println("Duration for webservice '" + endpointURI + "': " + durationString + "; request: " + requestString);
    }

    /**
     * Führt den Webservice mit Request-Methode POST aus mit dem übergebenen <i>requestString</i> (bzw. mit GET falls
     * dieser {@code null} ist) und vergleicht das Ergebnis mit dem Inhalt der übergebenen <i>expectedResponseFile</i> Datei
     * (UTF-8 ohne BOM).
     *
     * @param endpointURI                 Relative Endpoint-URI
     * @param requestString               JSON Request; Request-Methode GET falls {@code null} oder leer, sonst POST
     * @param additionalRequestProperties Map mit optionalen zusätzlichen Request-Eigenschaften (z.B. für JWT Token)
     * @param expectedResponseFile        Datei mit JSON Response-Inhalt (UTF-8 ohne BOM) bei {@link HttpConstants#HTTP_STATUS_OK},
     *                                    ansonsten Response-Message
     */
    protected void executeWebservice(String endpointURI, String requestString, Map<String, String> additionalRequestProperties,
                                     DWFile expectedResponseFile) {
        String expectedResponseString = getExpectedResponseString(expectedResponseFile);
        executeWebservice(endpointURI, requestString, additionalRequestProperties, expectedResponseString, HttpConstants.HTTP_STATUS_OK);
    }

    /**
     * Führt den Webservice mit Request-Methode POST aus mit dem übergebenen <i>requestString</i> (bzw. mit GET falls
     * dieser {@code null} ist) und vergleicht das Ergebnis mit dem Inhalt der übergebenen <i>expectedResponseFile</i> Datei
     * (UTF-8 ohne BOM).
     *
     * @param endpointURI                 Relative Endpoint-URI
     * @param requestString               JSON Request; Request-Methode GET falls {@code null} oder leer, sonst POST
     * @param additionalRequestProperties Map mit optionalen zusätzlichen Request-Eigenschaften (z.B. für JWT Token)
     * @param expectedResponseString      String mit JSON Response-Inhalt (UTF-8 ohne BOM) bei {@link HttpConstants#HTTP_STATUS_OK},
     *                                    ansonsten Response-Message
     */
    protected void executeWebservice(String endpointURI, String requestString, Map<String, String> additionalRequestProperties,
                                     String expectedResponseString) {
        executeWebservice(endpointURI, requestString, additionalRequestProperties, expectedResponseString, HttpConstants.HTTP_STATUS_OK);
    }

    /**
     * Führt den Webservice mit Request-Methode POST aus mit dem übergebenen <i>requestString</i> (bzw. mit GET falls
     * dieser {@code null} ist) und vergleicht das Ergebnis mit dem Inhalt der übergebenen <i>expectedResponseFile</i> Datei
     * (UTF-8 ohne BOM).
     *
     * @param endpointURI                 Relative Endpoint-URI
     * @param requestString               JSON Request; Request-Methode GET falls {@code null} oder leer, sonst POST
     * @param additionalRequestProperties Map mit optionalen zusätzlichen Request-Eigenschaften (z.B. für JWT Token)
     * @param expectedResponseFile        Datei mit JSON Response-Inhalt (UTF-8 ohne BOM) bei {@link HttpConstants#HTTP_STATUS_OK},
     *                                    ansonsten Response-Message
     * @param expectedResponseCode        Der erwartete Rückgabewert kann mit übergeben werden.
     */
    protected void executeWebservice(String endpointURI, String requestString, Map<String, String> additionalRequestProperties,
                                     DWFile expectedResponseFile, int expectedResponseCode) {
        String expectedResponseString = getExpectedResponseString(expectedResponseFile);
        executeWebservice(endpointURI, requestString, additionalRequestProperties, expectedResponseString, expectedResponseCode);
    }
}
