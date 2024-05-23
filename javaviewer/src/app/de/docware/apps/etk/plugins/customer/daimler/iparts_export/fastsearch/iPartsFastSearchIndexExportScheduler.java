/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuForProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.helper.FastSearchConnectionHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.helper.FastSearchExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.helper.FastSearchHashHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.helper.FastSearchTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.ElasticSearchCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.fastsearch.transferobjects.ElasticSearchIndex;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.iPartsModelPartsListsFastSearchS3Helper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.iPartsProductModelExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadExportExecutor;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadExportMessageFileHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadKeyValueDBManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.booleanfunctionparser.model.Conjunction;
import de.docware.util.misc.booleanfunctionparser.model.Disjunction;
import de.docware.util.misc.booleanfunctionparser.model.PositiveAndNegativeTerms;
import de.docware.util.os.OsUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.concurrent.ExecutorService;


/**
 * Scheduler für den Export der ElasticSearch-Indizes
 */
public class iPartsFastSearchIndexExportScheduler extends AbstractDayOfWeekHandler {

    // Konfiguration für Testmodus
    private static final boolean LOAD_JSONS_FROM_DIR = false;
    private static final String LOCAL_DIR_FOR_JSON_TEST_DATA = "D:\\Temp\\FastSearch";

    public static final String TITLE = "!!Automatischer Export der ElasticSearch-Indizes";
    private static final String KEY_EXPORT_INDEX_PREFIX = "iParts_export_fast_search_";
    private static final String KEY_EXPORT_INDEX_RUNNING = KEY_EXPORT_INDEX_PREFIX + "running";
    private static final String KEY_EXPORT_INDEX_CANCELED = KEY_EXPORT_INDEX_PREFIX + "canceled";
    private static final String KEY_EXPORT_INDEX_TOTAL_JSON_COUNT = KEY_EXPORT_INDEX_PREFIX + "total_json_count";
    private static final String KEY_EXPORT_INDEX_DONE_JSON_COUNT = KEY_EXPORT_INDEX_PREFIX + "done_json_count"; // wird mit exported_ oder finished_ zu lang
    private static final String S3_PREFIX = "FAST_SEARCH_INDEX";
    private static final boolean USE_EINPAS_COMPACT_NOTATION = true;

    private final MultiThreadKeyValueDBManager keyValueDBManager;
    private boolean uploadToS3Active;
    private iPartsModelPartsListsFastSearchS3Helper s3Helper;
    private String s3ObjectStoreDir;
    private MultiThreadExportMessageFileHelper messageHelper;
    private final boolean isExportWithGUI;
    private Session guiSession;
    private volatile int currentModelCount;
    private boolean exportOnlyModifiedProducts;

    public iPartsFastSearchIndexExportScheduler(EtkProject project, Session session) {
        this(project, session, false);
    }

    public iPartsFastSearchIndexExportScheduler(EtkProject project, Session session, boolean isExportWithGUI) {
        super(project, session, iPartsExportPlugin.LOG_CHANNEL_FAST_SEARCH, "fast search index export timer");
        this.keyValueDBManager = new MultiThreadKeyValueDBManager(project, KEY_EXPORT_INDEX_RUNNING, KEY_EXPORT_INDEX_CANCELED,
                                                                  KEY_EXPORT_INDEX_TOTAL_JSON_COUNT, KEY_EXPORT_INDEX_DONE_JSON_COUNT);
        this.isExportWithGUI = isExportWithGUI;
    }

    public static boolean isFastSearchIndexExportRunning(EtkProject project) {
        MultiThreadKeyValueDBManager kvc = new MultiThreadKeyValueDBManager(project, KEY_EXPORT_INDEX_RUNNING, KEY_EXPORT_INDEX_CANCELED,
                                                                            KEY_EXPORT_INDEX_TOTAL_JSON_COUNT, KEY_EXPORT_INDEX_DONE_JSON_COUNT);
        return kvc.isRunning();
    }

    /**
     * Startet den Export aus der Oberfläche heraus
     *
     * @param project
     * @param session
     */
    public static void doRunFastSearchIndexExportWithMessages(EtkProject project, Session session) {
        iPartsFastSearchIndexExportScheduler scheduler = new iPartsFastSearchIndexExportScheduler(project, session, true);
        scheduler.executeLogic();
        scheduler.stopThread();
    }

    @Override
    protected void executeLogic() {
        messageHelper = new MultiThreadExportMessageFileHelper(TITLE, null, isExportWithGUI, getLogChannel(), "!!Teil-Indizes");
        messageHelper.resetErrorCount();
        this.currentModelCount = 0;

        // In der KEYVALUE Tabelle nachschauen, ob der Export noch läuft
        boolean exportAlreadyRunning = keyValueDBManager.isRunning();
        if (!exportAlreadyRunning) {
            // Benutzer fragen, ob die Berechnung durchgeführt werden soll
            boolean doCalc = messageHelper.showStartExportConfirmationMessage(keyValueDBManager);
            if (!doCalc) {
                return;
            }
            doIndexExport();
            messageHelper.showProgress(false, keyValueDBManager);
        } else {
            // Wird schon ausgeführt -> Meldung, dass Export jetzt nicht möglich ist
            messageHelper.showProgress(true, keyValueDBManager);
        }
    }

    /**
     * Export der ElasticSearch-Indizes für jedes Baumuster pro Produkt
     */
    private void doIndexExport() {
        // Eintrag isRunning in DB
        keyValueDBManager.setRunning(true);

        // Log-Datei und MessageHelper initialisieren
        DWFile logFile = iPartsJobsManager.getInstance().exportJobRunning("FAST_SEARCH_INDEX");
        messageHelper.setLogFile(logFile);

        uploadToS3Active = iPartsExportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsExportPlugin.CONFIG_MODEL_DATA_FAST_SEARCH_S3_ACTIVE);
        if (uploadToS3Active) {
            messageHelper.logInfoMsg("Upload of fast search index to S3 bucket is active");
            s3Helper = new iPartsModelPartsListsFastSearchS3Helper(iPartsExportPlugin.LOG_CHANNEL_FAST_SEARCH, "exported fast search index");
            if (!s3Helper.isConnectionValid()) {
                messageHelper.logError("Connection settings for S3 bucket are invalid! Upload of fast search index to S3 bucket not possible!");
                uploadToS3Active = false;
            }
        }

        // Anhand vom Modus den Text bestimmen
        String modifiedText = exportOnlyModifiedProducts ? "modified " : "";
        messageHelper.logMsg("Calculation and export of fast search index for all " + modifiedText + "products started...");

        // Runnable für den kompletten Export
        FrameworkRunnable runnable = createExportRunnable(logFile);

        // Unterscheidung, ob mit oder ohne Oberfläche
        if (isExportWithGUI) {
            guiSession = Session.get();
            Session session = getSession();
            if (session != null) {
                session.startChildThread(runnable);
            } else {
                runnable.run(null);
            }
        } else {
            runnable.run(null);
        }
    }

    /**
     * Liefert das Runnable für den kompletten Prozess
     *
     * @param logFile
     * @return
     */
    private FrameworkRunnable createExportRunnable(DWFile logFile) {
        return (thread) -> {
            String endMessage = "";
            try {
                // Export durchführen
                endMessage = exportData();
            } finally {
                // Eintrag isRunning zurücknehmen
                keyValueDBManager.setRunning(false);

                // Nachricht am Ende anzeigen
                String showMsg;
                if (messageHelper.getErrorCount() > 0) {
                    showMsg = TranslationHandler.translate(TITLE) + " " + TranslationHandler.translate("!!ist mit Fehlern beendet (siehe Log-Datei).");
                    if (!keyValueDBManager.isCanceled()) {
                        messageHelper.logMsg("Calculation and export of fast search index finished with errors");
                    }
                    iPartsJobsManager.getInstance().jobError(logFile);
                } else {
                    showMsg = TranslationHandler.translate(TITLE) + " " + TranslationHandler.translate("!!ist beendet.");
                    if (!keyValueDBManager.isCanceled()) {
                        messageHelper.logMsg("Calculation and export of fast search index finished successfully");
                        iPartsJobsManager.getInstance().jobProcessed(logFile);
                    } else {
                        iPartsJobsManager.getInstance().jobError(logFile);
                    }
                }
                showMessage(showMsg + "\n\n" + endMessage);
            }
        };
    }

    /**
     * Bestimmt die Sprachen, in denen die Produkte exportiert werden sollen.
     * Das hat Auswirkungen auf die anzulegende Verzeichnisstruktur.
     *
     * @param calculatedVisibleProducts
     */
    private Set<String> determineExportLanguages(iPartsProductModelExportHelper.CalculatedVisibleProducts calculatedVisibleProducts) {
        Set<String> exportLanguages = new HashSet<>();
        if (calculatedVisibleProducts != null) {
            // Überprüfen, ob Produkte nur EPC-Sprachen verwenden und ob Produkte alle Sprachen verwenden.
            boolean fullLanguageSupport = false;
            boolean epcLanguageSupport = false;
            for (Map.Entry<iPartsProductId, Set<String>> productModelEntry : calculatedVisibleProducts.getMergedProductMaps().entrySet()) {
                iPartsProductId productId = productModelEntry.getKey();
                iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                if (product.isFullLanguageSupport()) {
                    fullLanguageSupport = true;
                } else {
                    epcLanguageSupport = true;
                }
                if (fullLanguageSupport && epcLanguageSupport) {
                    break;
                }
            }

            // Wenn Produkte alle Sprachen verwenden, diese Sprachen als zu exportierende Sprachen setzten.
            if (fullLanguageSupport) {
                exportLanguages.addAll(iPartsLanguage.getCompleteDaimlerLanguageList(getProject()));
            }

            // Wenn Produkte die EPC-Sprachen verwenden, diese Sprachen ggf. zusätzlich als zu exportierende Sprachen setzten.
            if (epcLanguageSupport) {
                exportLanguages.addAll(iPartsLanguage.getEPCLanguageList());
            }
        }
        return exportLanguages;
    }

    /**
     * Exportiert die aus den Stücklisten erzeugten ElasticSearch Indizes aller Produkt-Baumuster Verknüpfungen
     *
     * @return
     */
    private String exportData() {
        if (!iPartsPlugin.isWebservicePluginPresent()) {
            messageHelper.logError(iPartsConst.PLUGIN_CLASS_NAME_IPARTS_WEBSERVICE + " not found");
            return "!!Das DAIMLER iParts Webservice Plug-in wurde nicht gefunden!";
        }
        // Check, ob via Admin-Option eine maximale Anzahl an Export Produkten eingestellt wurde
        int testModeSmallDataCount = iPartsExportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsExportPlugin.CONFIG_FAST_SEARCH_EXPORT_PRODUCT_COUNT);
        if (testModeSmallDataCount > 0) {
            messageHelper.logMsg("Maximum number of products to export for each company: " + testModeSmallDataCount);
        }

        // Werte resetten
        messageHelper.resetErrorCount();
        currentModelCount = 0;

        // Anhand vom Modus alle sichtbaren oder nur die modifizierten Produkte bestimmen
        iPartsProductModelExportHelper.CalculatedVisibleProducts calculatedVisibleProducts;
        if (exportOnlyModifiedProducts) {
            calculatedVisibleProducts
                    = iPartsProductModelExportHelper.calculateVisibleModifiedProducts(getProject(), false, testModeSmallDataCount);
        } else {
            calculatedVisibleProducts
                    = iPartsProductModelExportHelper.calculateVisibleProducts(getProject(), false, testModeSmallDataCount);
        }

        int totalProductsCount = calculatedVisibleProducts.getTotalProductCount();
        // Export Sprachen nur bestimmen, wenn das Herausschreiben in Dateien gewünscht ist
        boolean isExportToFile = iPartsExportPlugin.isFastSearchExportToFileActive();
        Set<String> exportLanguages = null;
        if (isExportToFile) {
            exportLanguages = determineExportLanguages(calculatedVisibleProducts);
        }

        // Normaler Betrieb - pro BM zum Produkt Indizes erzeugen
        StringBuilder endMessage = new StringBuilder();
        if (totalProductsCount > 0) {
            long startTime = System.currentTimeMillis();
            String currentDateFormatted = DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss);
            DWFile rootDir = null;
            if (isExportToFile) {
                // Verzeichnis aus der Admin-Option holen
                DWFile destDir = iPartsExportPlugin.getDirForFastSearchIndexExport();
                // Unterverzeichnis mit dem Zeitstempel anlegen
                rootDir = (destDir == null) ? null : messageHelper.createRootDir(destDir, currentDateFormatted);
                if (rootDir == null) {
                    return "!!Fehler beim Erzeugen der Export-Verzeichnisse (siehe Log-Datei)!";
                }
                // Unterhalb des Zeitstempels für jede Sprache ein Verzeichnis anlegen und in den jeweiligen
                // Sprachen-Verzeichnissen die Firmen-Verzeichnisse anlegen (MBAG, DTAG und all), sofern das Herausschreiben
                // in Dateien gewünscht ist
                if (!createLanguageAndCompanyDirs(rootDir, exportLanguages)) {
                    messageHelper.logError("Could not create sub directories for " + rootDir.getAbsolutePath());
                    return "!!Fehler beim Erzeugen der Export-Verzeichnisse (siehe Log-Datei)!";
                }
            }
            if (uploadToS3Active) {
                s3ObjectStoreDir = S3_PREFIX + "-" + currentDateFormatted;
            }
            // Max. Anzahl BM bestimmen
            int totalModelsCount = calculatedVisibleProducts.getTotalModelOrPCCount();
            // Max. Anzahl Threads via Admin-Option bestimmen
            int threadCount = iPartsExportPlugin.getFastSearchIndexExportThreadCount();
            messageHelper.logInfoMsg("Starting export of " + totalProductsCount + " products ("
                                     + calculatedVisibleProducts.getCarProductModelsOrPCMapSize() + " MBAG; "
                                     + calculatedVisibleProducts.getTruckProductModelsOrPCMapSize() + " DTAG; "
                                     + calculatedVisibleProducts.getBothCompaniesModelsOrPCMapSize() + " both companies; "
                                     + totalModelsCount + " models in total) with "
                                     + threadCount + " threads...");
            if (isExportToFile) {
                messageHelper.logInfoMsg("Export languages: " + String.join(", ", exportLanguages));
            }
            // In der Key-Value Tabelle die max. Anzahl BM ablegen
            keyValueDBManager.setKeyDataTotalCount(totalModelsCount);
            // Ein Verbindungshelfer pro Produkt, weil die Daten des Produkts am Stück verarbeitet werden
            FastSearchConnectionHelper connectionHelper = new FastSearchConnectionHelper();
            boolean finished = false;
            if (exportElasticSearchIndicesForCompany(connectionHelper, calculatedVisibleProducts.getBothCompaniesModelsOrPCMap(),
                                                     iPartsTransferConst.COMPANY_VALUE_ELASTIC_SEARCH_INDEX_BOTH, rootDir)) {
                if (exportElasticSearchIndicesForCompany(connectionHelper, calculatedVisibleProducts.getCarProductModelsOrPCMap(),
                                                         iPartsTransferConst.COMPANY_VALUE_MBAG, rootDir)) {
                    finished = exportElasticSearchIndicesForCompany(connectionHelper, calculatedVisibleProducts.getTruckProductModelsMapOrPCMap(),
                                                                    iPartsTransferConst.COMPANY_VALUE_DTAG, rootDir);
                }
            }

            // Infos zum Export ausgeben
            long exportDuration = System.currentTimeMillis() - startTime;
            if (finished) {
                messageHelper.writeFinishedEndMessage(keyValueDBManager, endMessage, totalProductsCount, totalModelsCount,
                                                      currentModelCount, exportDuration, rootDir);
            } else {
                messageHelper.writeNotFinishedMessage(endMessage, totalProductsCount, totalModelsCount, exportDuration);
            }
        } else {
            // Es wurden keine Produkte gefunden
            keyValueDBManager.setKeyDataTotalCount(0);
            messageHelper.logMsg("No products and models found.");
            endMessage.append(TranslationHandler.translate("!!Keine Produkte gefunden!"));
        }
        return endMessage.toString();
    }

    /**
     * Exportiert alle aus den Stücklisten erzeugten ElasticSearch Indizes zur übergebenen Firmenzugehörigkeit
     *
     * @param connectionHelper
     * @param productModelsMap
     * @param companyName
     * @param rootDir
     * @return
     */
    private boolean exportElasticSearchIndicesForCompany(FastSearchConnectionHelper connectionHelper, Map<iPartsProductId, Set<String>> productModelsMap,
                                                         String companyName, DWFile rootDir) {
        if (keyValueDBManager.isCanceled()) {
            return false;
        }
        // Info welche Firmenzugehörigkeit abgearbeitet wird
        boolean bothCompanies = companyName.equals(iPartsTransferConst.COMPANY_VALUE_ELASTIC_SEARCH_INDEX_BOTH);
        messageHelper.logMsg("Calculating " + (bothCompanies ? "both MBAG and DTAG" : companyName));
        // Unterscheidung Testmodus <-> realer Betrieb (Testmodus: partsList Antworten von MB einlesen, Indizes erzeugen
        // und mit den Indizes von MB vergleichen)
        if (LOAD_JSONS_FROM_DIR) {
            // Testmodus
            DWFile dir = DWFile.get(LOCAL_DIR_FOR_JSON_TEST_DATA);
            if (dir.isDirectory()) {
                // Original JSON-Dateien aus dem Testverzeichnis lesen
                List<DWFile> testFiles = dir.listDWFiles((file, name) -> name.endsWith("." + MimeTypes.EXTENSION_JSON));
                for (DWFile file : testFiles) {
                    if (keyValueDBManager.isCanceled()) {
                        return false;
                    }
                    // Pro Datei den Namen extrahieren und Suffix und Firmenzugehörigkeit entfernen
                    String fileName = file.extractFileName(false);
                    fileName = fileName.replace("_plst", "");
                    String tempFileName = fileName.replace(("_" + companyName), "");
                    if (tempFileName.equals(fileName)) {
                        continue;
                    } else {
                        fileName = tempFileName;
                    }
                    // Aus dem Dateinamen Baumuster und Produkt extrahieren
                    String[] token = fileName.replaceFirst(FastSearchExportHelper.TEXT_DELIMITER_FOR_HASH, "%%").split("%%");
                    if (token.length == 2) {
                        String modelNo = token[0];
                        String productNo = token[1];
                        Set<String> models = new HashSet<>();
                        models.add(modelNo);
                        // Indizes erzeugen
                        if (!exportElasticSearchIndicesForOneProduct(connectionHelper, new iPartsProductId(productNo), models,
                                                                     companyName, rootDir)) {
                            return false;
                        }
                    }
                }
            }
        } else {
            // Normaler Betrieb - pro BM zum Produkt Indizes erzeugen
            for (Map.Entry<iPartsProductId, Set<String>> productModelEntry : productModelsMap.entrySet()) {
                if (keyValueDBManager.isCanceled()) {
                    return false;
                }
                // Zeitstempel vor dem Import holen
                Date exportDate = DateUtils.toDate_currentDate();
                iPartsProductId productId = productModelEntry.getKey();
                if (!exportElasticSearchIndicesForOneProduct(connectionHelper, productId, productModelEntry.getValue(),
                                                             companyName, rootDir)) {
                    return false;
                }
                // Zeitstempel beim erfolgreichen Export setzen
                saveExportTimeStamp(getProject(), productId, exportDate);
            }
        }
        messageHelper.logMsg("Finished exporting " + (bothCompanies ? "both MBAG and DTAG" : companyName));
        return true;
    }

    /**
     * Setzt den Zeitstempel am Produkt
     *
     * @param project
     * @param productId
     * @param exportDate
     */
    private void saveExportTimeStamp(EtkProject project, iPartsProductId productId, Date exportDate) {
        iPartsDataProduct dataProduct = new iPartsDataProduct(getProject(), productId);
        if (dataProduct.existsInDB()) {
            dataProduct.setExportTimeStamp(exportDate);
            project.getDbLayer().startTransaction();
            try {
                dataProduct.saveToDB();
                project.getDbLayer().commit();
            } catch (Exception e) {
                project.getDbLayer().rollback();
                Logger.getLogger().handleRuntimeException(e);
            }
        }
    }

    /**
     * Erzeugt für jede Sprache ein Verzeichnis und legt in den jeweiligen Sprachen-Verzeichnissen die Firmen-Verzeichnisse
     * an (MBAG, DTAG und all)
     *
     * @param rootDir
     * @param exportLanguages
     * @return
     */
    private boolean createLanguageAndCompanyDirs(DWFile rootDir, Set<String> exportLanguages) {
        for (String language : exportLanguages) {
            if (!createSingleLanguageAndCompanyDir(rootDir, iPartsTransferConst.COMPANY_VALUE_MBAG, language)) {
                return false;
            }
            if (!createSingleLanguageAndCompanyDir(rootDir, iPartsTransferConst.COMPANY_VALUE_DTAG, language)) {
                return false;
            }
            if (!createSingleLanguageAndCompanyDir(rootDir, iPartsTransferConst.COMPANY_VALUE_ELASTIC_SEARCH_INDEX_BOTH, language)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Erzeugt das Unterverzeichnis für die übergebene Sprache und Firmenzugehörigkeit
     *
     * @param rootDir
     * @param companyName
     * @param language
     * @return
     */
    private boolean createSingleLanguageAndCompanyDir(DWFile rootDir, String companyName, String language) {
        String path = language.toLowerCase() + OsUtils.FILESEPARATOR + companyName;
        return messageHelper.createSubDir(rootDir, path, "in destination directory") != null;
    }

    /**
     * Erzeugt aus dem übergebenen Baumuster zum übergebenen Produkt die dazugehörigen ElasticSearch Indizes
     *
     * @param connectionHelper
     * @param productId
     * @param models
     * @param companyName
     * @param rootDir
     * @return
     */
    private boolean exportElasticSearchIndicesForOneProduct(FastSearchConnectionHelper connectionHelper,
                                                            iPartsProductId productId, Set<String> models,
                                                            String companyName, DWFile rootDir) {
        // Check, ob eine Verbindung aufgebaut werden kann
        boolean skipSendFilesToServer = false;
        if (uploadToS3Active && !Utils.isValid(connectionHelper.getAllBaseURLs())) {
            messageHelper.logMsg("Exporting indices only to S3 bucket for product " + productId.getProductNumber());
            skipSendFilesToServer = true;
        } else if (!connectionHelper.initConnection()) {
            messageHelper.logError("Could not connect to Server. No valid connection found!");
            if (!iPartsExportPlugin.isFastSearchExportToFileActive()) {
                return false;
            }
            messageHelper.logError("Exporting indices only to file system!");
            skipSendFilesToServer = true;
        } else {
            messageHelper.logMsg("Successfully established a connection for product " + productId.getProductNumber()
                                 + " with " + connectionHelper.getCurrentBaseURL());
        }
        int modelsCount = models.size();
        iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
        boolean isSpecialProduct = product.isSpecialCatalog();
        messageHelper.logMsg("Calculating product " + productId.getProductNumber() + " with " + modelsCount
                             + (isSpecialProduct ? " AS product classes" : " models") + "...");
        KgTuForProduct kgTuForProduct = KgTuForProduct.getInstance(getProject(), productId);
        boolean successfulExport = true;
        // ExecutorService anlegen und pro Produkt alle BM (Stücklisten) als Runnable dem Service hinzufügen
        ExecutorService executorService = MultiThreadExportExecutor.createExecutor(iPartsExportPlugin.getFastSearchIndexExportThreadCount());
        try {
            // Map mit Sprache auf Baumuster zu StringBuilder
            Map<String, Map<String, StringBuilder>> langToModelJSON = new HashMap<>();
            // Batch an BM für die nächste Versorgung
            Set<String> modelsForNextExecution = new LinkedHashSet<>();
            int modelCountForBatch = iPartsExportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsExportPlugin.CONFIG_FAST_SEARCH_EXPORT_MODEL_COUNT);
            for (String modelNo : models) {
                if (keyValueDBManager.isCanceled()) {
                    break;
                }
                Runnable calculationRunnable = createElasticSearchForProductExportRunnable(productId, modelNo, kgTuForProduct,
                                                                                           companyName, rootDir, langToModelJSON);
                executorService.execute(calculationRunnable);
                modelsForNextExecution.add(modelNo);
                // Haben wir die maximal Anzahl BM für eine Versorgung erreicht, werden die JSONs an den Server geschickt
                if (modelsForNextExecution.size() >= modelCountForBatch) {
                    successfulExport = executeServiceAndSendData(productId, executorService, connectionHelper, langToModelJSON,
                                                                 modelsForNextExecution, skipSendFilesToServer);
                    // Verarbeitung war erfolgreich -> Infos und Service zurücksetzen
                    if (successfulExport) {
                        modelsForNextExecution.clear();
                        langToModelJSON.clear();
                        executorService = MultiThreadExportExecutor.createExecutor(iPartsExportPlugin.getFastSearchIndexExportThreadCount());
                    } else {
                        break;
                    }

                }
            }
            // Falls weniger BM als eingestellt verarbeitet wurden, dann müssen diese hier abgeschickt werden
            if (successfulExport && !modelsForNextExecution.isEmpty()) {
                successfulExport = executeServiceAndSendData(productId, executorService, connectionHelper, langToModelJSON,
                                                             modelsForNextExecution, skipSendFilesToServer);
            }
        } finally {
            executorService.shutdown(); // executeServiceAndSendData() ruft bei korrekter Ausführung intern shutdown() schon auf, aber ein zweiter Aufruf wird einfach ignoriert
        }

        return successfulExport && !keyValueDBManager.isCanceled();
    }

    private boolean executeServiceAndSendData(iPartsProductId productId, ExecutorService executorService,
                                              FastSearchConnectionHelper connectionHelper,
                                              Map<String, Map<String, StringBuilder>> langToModelJSON,
                                              Set<String> models, boolean skipSendFilesToServer) {
        String modelString = String.join(", ", models);
        messageHelper.logMsg("Models for Product " + productId.getProductNumber() + " scheduled. Creating indices for models " + modelString + " and product " + productId.getProductNumber() + "...");
        boolean finished = MultiThreadExportExecutor.executorAwaitTermination(executorService);
        if (finished) {
            messageHelper.logMsg("Indices for models " + modelString + " and product " + productId.getProductNumber() + " created");
            // Falls die Daten ins Verzeichnis geschrieben werden sollen, hier das Senden an den Server unterbinden
            if (!skipSendFilesToServer) {
                boolean updateResult = connectionHelper.updateProduct(productId, langToModelJSON, messageHelper);
                if (updateResult) {
                    messageHelper.logMsg("Models " + modelString + " for Product " + productId.getProductNumber() + " updated!");
                } else {
                    messageHelper.logError("Error while sending product data for " + productId.getProductNumber());
                }
            }
        } else {
            messageHelper.logError("Error while creating indices for Product " + productId.getProductNumber() + ". Product will not be updated!");
        }
        return finished;
    }

    /**
     * Erzeugt ein {@link Runnable} Objekt für das übergebene Produkt und Baumuster, in dem aus den Stücklisten zur Produkt-BM
     * Verknüpfung ElasticSearch Indizes pro Sprache erzeugt werden
     *
     * @param productId
     * @param modelNo
     * @param kgTuForProduct
     * @param companyName
     * @param rootDir
     * @param langToModelJSON
     * @return
     */
    private Runnable createElasticSearchForProductExportRunnable(iPartsProductId productId, String modelNo,
                                                                 KgTuForProduct kgTuForProduct, String companyName,
                                                                 DWFile rootDir, Map<String, Map<String, StringBuilder>> langToModelJSON) {
        return () -> {
            try {
                if (keyValueDBManager.isCanceled()) {
                    return;
                }

                String productNumber = DWFile.convertToValidFileName(productId.getProductNumber());
                // Hier die Response des WS holen oder bei einem Test, die Original JSON von MB einlesen
                de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist.iPartsWSPartsListResponse response;
                if (LOAD_JSONS_FROM_DIR) {
                    String delimiter = FastSearchExportHelper.TEXT_DELIMITER_FOR_HASH;
                    // Der Dateiname ist immer gleich aufgebaut, z.B. 223155_C223_FV_MBAG_plst.json -> 223155 + _ + C223_FV + _ + MBAG + _ + plst
                    String fileName = messageHelper.buildJsonFileName(FastSearchTextHelper.getCleanModelNo(modelNo)
                                                                      + delimiter + productNumber + delimiter + companyName
                                                                      + delimiter + "plst");
                    DWFile file = DWFile.get(LOCAL_DIR_FOR_JSON_TEST_DATA).getChild(fileName);
                    if (file.isFile()) {
                        Genson genson = JSONUtils.createGensonWithOmittedFields(true);
                        String content = file.readTextFile(DWFileCoding.UTF8);
                        response = genson.deserialize(content, de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.endpoints.partslist.iPartsWSPartsListResponse.class);
                    } else {
                        return;
                    }
                } else {
                    // partsList WS Logik direkt abfragen
                    response = iPartsProductModelExportHelper.calculateResponseForModelOrPCPartsLists(productId, modelNo,
                                                                                                      messageHelper,
                                                                                                      getLogChannel(),
                                                                                                      getProject());
                }

                if (response != null) {
                    // Ist eine gültige Response vorhanden, versuche die Attribute auf das neue Export-DTO zu mappen.
                    // Die Idee: Wir erzeugen pro Sprache einen FileWriter. Es wird aber nur ein default-DTO angelegt,
                    // der pro Stücklistenposition mit den aktuellen Werten befüllt wird, z.B. KG Werte ändern sich erst,
                    // wenn alle Positionen zu allen TUs verarbeitet wurden. Also braucht man die KG nicht bei jeder
                    // Position zu setzen. Teilenummer ändert sich aber bei jeder Position, usw.
                    // Wenn die nicht-sprachabhängigen Werte gesetzt sind, werden alle Sprachen durchlaufen und wir setzen
                    // alle Texte für die aktuelle Sprache. Danach wird der FileWriter für die Sprache geholt, der den
                    // aktuellen Zustand des DTOs rausschreibt. Danach wird das DTO mit den Texten der nächsten Sprache
                    // befüllt und rausgeschrieben usw. Sind alle Sprachen durch, wird die nächste Position auf das
                    // DTO gemappt und danach folgt der gleiche Sprachen-FileWriter Prozess wie oben erwähnt.
                    de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext identContext = response.getIdentContext();
                    if (identContext == null) {
                        return;
                    }
                    // Alle KGs bestimmen
                    List<de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartsListNavNode> kgNodes = response.getNextNodes();
                    if (kgNodes != null) {

                        // Um pro Sprache einen Index zu erzeugen, müssen hier erst einmal die gewünschten Sprachen bestimmt werden.
                        iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
                        List<String> languages = product.getSupportedLanguages(getProject());

                        // Helper erzeugen und befüllen
                        FastSearchExportHelper exportHelper = new FastSearchExportHelper(getProject(), languages, messageHelper);
                        // Unterverzeichnisse mit ".../Produkt/" für alle Sprachen erzeugen, in dem alle Indizes für die
                        // jeweilige Sprache in einer Datei pro Baumuster erzeugt werden
                        if ((rootDir != null) && !exportHelper.createProductDir(rootDir, productNumber, companyName)) {
                            return;
                        }
                        String modelId = identContext.getModelId();
                        // Baumuster nach Vorgabe in iparts.js
                        String cleanModelId = FastSearchTextHelper.getCleanModelNo(modelId);
                        // Ein default DTO erzeugen, das mit den aktuellen Werten befüllt wird
                        ElasticSearchIndex defaultPartIndex = createDefaultPartIndexDTO(cleanModelId, companyName, identContext);
                        // Pro Sprache, Firmenzugehörigkeit und Produkt eine JSON Datei via eigenen StringBuilder erzeugen
                        Map<String, StringBuilder> builderForLanguage = exportHelper.initBuilder();
                        // Durchlaufe alle KGs
                        for (de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartsListNavNode kgNode : kgNodes) {
                            if (keyValueDBManager.isCanceled()) { // Abbruch aus Performancegründen nur pro KG prüfen
                                return;
                            }
                            convertKGNode(defaultPartIndex, kgNode, kgTuForProduct, cleanModelId, exportHelper);
                        }
                        // Damit wir später auf das BM zur JSON Datei zugreifen können, hier eine Verknüpfung BM zu JSON Datei
                        builderForLanguage.forEach((lang, builder) -> {
                            if (builder.length() > 0) {
                                Map<String, StringBuilder> modelToBuilders = langToModelJSON.computeIfAbsent(lang, k -> new HashMap<>());
                                modelToBuilders.put(modelNo, builder);
                            } else {
                                System.out.println(productNumber + " - " + modelNo);
                            }
                        });

                        if (!keyValueDBManager.isCanceled()) {
                            // Am Ende die JSONs herausschreiben, wenn gewünscht
                            if ((rootDir != null) && iPartsExportPlugin.isFastSearchExportToFileActive()) {
                                exportHelper.exportJSONToFile(cleanModelId, identContext.getProductId(), companyName, rootDir);
                            }

                            // S3 Upload der JSONs, wenn gewünscht
                            if (uploadToS3Active) {
                                exportHelper.exportJSONToS3Bucket(cleanModelId, identContext.getProductId(), companyName,
                                                                  s3ObjectStoreDir, s3Helper);
                            }
                        }
                    }
                } else {
                    messageHelper.logMsg("Couldn't calculate part list data for model \"" + modelNo + "\" and product \"" + productNumber + "\"");
                }

                // Check, ob der Export abgebrochen wurde
                if (keyValueDBManager.isCanceled()) {
                    messageHelper.logMsg("Index for model " + modelNo + " and product " + productId.getProductNumber() + " canceled");
                    return;
                }
                // Anzahl Baumuster erhöhen (inkl DB Wert)
                incCurrentModelCount();
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(getLogChannel(), LogType.ERROR, e);
                messageHelper.addErrorToLog(Utils.exceptionToString(e));
            }
        };
    }

    /**
     * Erzeugt ein default {@link ElasticSearchIndex} Objekt für die übergebenen Parameter. Diese bleiben bis zum Schließen
     * der DWWriter gleich.
     *
     * @param cleanModelId
     * @param companyName
     * @param identContext
     * @return
     */
    private ElasticSearchIndex createDefaultPartIndexDTO(String cleanModelId, String companyName,
                                                         de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSIdentContext identContext) {
        // Baureihe analog zum Original in iparts.js bestimmen
        String series = StrUtils.copySubString(cleanModelId, 0, 3);
        // Wert für modelid bestimmen (iparts.js)
        String modeIdForIndex = cleanModelId + "_" + identContext.getProductId().replace("\r", "");
        // Ein default DTO erzeugen, das mit den aktuellen Werten befüllt wird
        ElasticSearchIndex defaultPartIndex = new ElasticSearchIndex();
        // Werte, die für alle Positionen in allen KG/TUs zum aktuellen BM und Produkt gleich sind (also nur einmal bestimmen)
        // branch: Hier gibt es nur die Werte "mbag", "dtag" und "all" (für beide gültig)
        defaultPartIndex.setBranch(companyName.toLowerCase());
        // Baureihe ohne Prefix
        defaultPartIndex.setSeries(series);
        // Spezielle Darstellung von BM und Produkt
        defaultPartIndex.setModelid(modeIdForIndex);
        // aggtype
        defaultPartIndex.setAggtype(identContext.getAggTypeId());
        // prod
        defaultPartIndex.setProd(identContext.getProductId());
        return defaultPartIndex;
    }


    /**
     * Konvertiert die Daten der KG Knoten der Webservice Response in Daten für den {@link ElasticSearchIndex}
     *
     * @param defaultPartIndex
     * @param kgNode
     * @param kgTuForProduct
     * @param cleanModelId
     * @param exportHelper
     * @throws BooleanFunctionSyntaxException
     */
    private void convertKGNode(ElasticSearchIndex defaultPartIndex, de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartsListNavNode kgNode,
                               KgTuForProduct kgTuForProduct, String cleanModelId, FastSearchExportHelper exportHelper) throws BooleanFunctionSyntaxException {
        if (kgNode.getType().equals(de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode.TYPE.cg_group.name())) {
            String kg = kgNode.getId();
            // cgtext: Pro KG-Text wird der Text in der aktuellen Sprache ausgegeben. Zusätzlich gibt es ein Extra-Feld,
            // in das explizit der deutsche Text geschrieben wird (Meta-Suche). Der deutsche Text kann hier gesetzt werden
            // und ist für alle Sprachen, alle Positionen und alle darunterliegenden TUs gültig.
            // Hier wird das EtkMultiSprach bestimmt, das alle Texte für alle Sprachen enthält.
            // Gesetzt wird der Text erst ganz zum Schluss, wenn der Datensatz pro Sprache erzeugt wird.
            EtkMultiSprache kgText = null;
            KgTuNode kgForProduct = kgTuForProduct.getKgNode(kg);
            if (kgForProduct != null) {
                kgText = kgForProduct.getTitle();
                if (kgText != null) {
                    // cgtext_de
                    defaultPartIndex.setCgtext_de(kgText.getText(Language.DE.getCode()));
                }
            }

            // cg: (KG) setzen, weil gültig für alles, was darunter kommt
            defaultPartIndex.setCg(kg);

            // Alle TUs bestimmen
            List<de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartsListNavNode> tuNodes = kgNode.getNextNodes();
            if (tuNodes != null) {
                for (de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartsListNavNode tuNode : tuNodes) {
                    convertTUNode(defaultPartIndex, kg, kgText, tuNode, kgTuForProduct, cleanModelId, exportHelper);
                }
            }
        }
    }

    /**
     * Konvertiert die Daten der TU Knoten der Webservice Response KG Knoten in Daten für den {@link ElasticSearchIndex}
     *
     * @param defaultPartIndex
     * @param kg
     * @param kgText
     * @param tuNode
     * @param kgTuForProduct
     * @param cleanModelId
     * @param exportHelper
     * @throws BooleanFunctionSyntaxException
     */
    private void convertTUNode(ElasticSearchIndex defaultPartIndex, String kg, EtkMultiSprache kgText, de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartsListNavNode tuNode,
                               KgTuForProduct kgTuForProduct, String cleanModelId, FastSearchExportHelper exportHelper) throws BooleanFunctionSyntaxException {
        if (tuNode.getType().equals(de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode.TYPE.cg_subgroup.name())
            || tuNode.getType().equals(de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSNavNode.TYPE.sa_number.name())) {
            // csg (TU) setzen, weil gültig für alles, was darunter kommt
            String tu = tuNode.getId();
            defaultPartIndex.setCsg(tu);

            // csgtext: Pro TU Text wird der Text in der aktuellen Sprache ausgegeben. Zusätzlich gibt es ein Extra-Feld,
            // in das explizit der deutsche Text geschrieben wird (Meta-Suche). Der deutsche Text kann hier gesetzt werden
            // und ist für alle Sprachen und alle Positionen gültig.
            // Hier wird das EtkMultiSprach bestimmt, das alle Texte für alle Sprachen enthält.
            // Gesetzt wird der Text erst ganz zum Schluss, wenn der Datensatz pro Sprache erzeugt wird.
            EtkMultiSprache tuText = null;
            KgTuNode tuForProduct = kgTuForProduct.getTuNode(kg, tu);
            if (tuForProduct != null) {
                tuText = tuForProduct.getTitle();
                if (tuText != null) {
                    // csgtext_de:
                    defaultPartIndex.setCsgtext_de(tuText.getText(Language.DE.getCode()));
                }
            }

            // Die Stückliste bestimmen
            de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartsListModule module = tuNode.getModule();
            convertModuleNode(defaultPartIndex, kg, kgText, tu, tuText, module, cleanModelId, exportHelper);
        }
    }

    /**
     * Konvertiert die Daten der Stückliste der Webservice Response TU Knoten in Daten für den {@link ElasticSearchIndex}
     *
     * @param defaultPartIndex
     * @param kg
     * @param kgText
     * @param tu
     * @param tuText
     * @param module
     * @param cleanModelId
     * @param exportHelper
     * @throws BooleanFunctionSyntaxException
     */
    private void convertModuleNode(ElasticSearchIndex defaultPartIndex, String kg, EtkMultiSprache kgText, String tu, EtkMultiSprache tuText,
                                   de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPartsListModule module,
                                   String cleanModelId, FastSearchExportHelper exportHelper) throws BooleanFunctionSyntaxException {
        if (module != null) {
            // Stücklistenpositionen bestimmen
            List<de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPart> parts = module.getParts();
            if (parts != null) {
                FastSearchTextHelper textHelper = exportHelper.getFastSearchTextHelper();
                // Alle Stücklistenpositionen durchlaufen
                for (de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPart part : parts) {
                    // partno: Teilenummer
                    String partNo = part.getPartNo();
                    defaultPartIndex.setPartno(partNo);
                    // calloutid: Hotspot (nur setzen, wenn ein Wert existiert - iparts.js)
                    String calloutId = part.getCalloutId();
                    // DAIMLER-16208: 1. Leere / fehlende Hotspots sind als leere Zeichenketten abgebildet
                    defaultPartIndex.setCalloutid(StrUtils.getEmptyOrValidString(calloutId));
                    // pos: Hash über bestimmte Werte des aktuellen Datensatzes (siehe Methode)
                    defaultPartIndex.setPos(FastSearchHashHelper.createPOSHash(cleanModelId, kg, tu, partNo, calloutId));

                    // partname: Teilebenennung
                    String partNameTextId = part.getNameRef();
                    EtkMultiSprache partName = null;
                    if (StrUtils.isValid(partNameTextId)) {
                        // Hier wird das EtkMultiSprach bestimmt, das alle Texte für alle Sprachen enthält. Gesetzt wird
                        // der Text erst ganz zum Schluss, wenn der Datensatz pro Sprache erzeugt wird.
                        partName = textHelper.getTextForId(partNameTextId, TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR));
                        if (!partName.isEmpty()) {
                            // Der deutsche Text für die Meta-Suche kann hier schon gesetzt werden
                            defaultPartIndex.setPartname_de(partName.getText(Language.DE.getCode()));
                        }
                    } else {
                        // Existiert keine Text-Id aber eine Benennung (alte Fahrzeuge/Aggregate), dann soll die Benennung
                        // nur für DE gesetzt werden. Der deutsche Text für die Meta-Suche kann hier schon gesetzt werden.
                        defaultPartIndex.setPartname_de(part.getName());
                    }

                    // partdesc: Alle Text-IDs für den kombinierten Text bestimmen. Hier werden alle EtkMultiSprach bestimmt,
                    // die alle Texte für alle Sprachen enthalten. Gesetzt wird der Text erst ganz zum Schluss, wenn der
                    // Datensatz pro Sprache erzeugt wird.
                    List<String> additionalDescRefs = part.getAdditionalDescRefs();
                    List<EtkMultiSprache> addTexts = textHelper.handleAddTexts(defaultPartIndex, additionalDescRefs,
                                                                               exportHelper.getFallbackLanguages());
                    // matdesc: neutraler Text
                    String materialDescRef = part.getMaterialDescRef();
                    EtkMultiSprache matDesc = textHelper.handleMatDesc(defaultPartIndex, materialDescRef);
                    // codes: Code haben eine eigene JSON Struktur, die in der Methode erzeugt wird
                    String codes = part.getCodeValidity();
                    setCodes(codes, defaultPartIndex);
                    // saacodes: Setzt die SAA-Gültigkeiten nachdem die SAA angepasst wurde (siehe Methode)
                    List<String> saaValidity = part.getSaaValidity();
                    setSaaValidity(saaValidity, defaultPartIndex);
                    // assemblylocations: Fehlerorte (nur setzen, wenn ein Wert existiert - iparts.js)
                    defaultPartIndex.setAssemblylocations(part.getDamageCodes());
                    // steering: Lenkung (nur setzen, wenn ein Wert existiert - iparts.js)
                    defaultPartIndex.setSteering(part.getSteering());
                    // einpas: EinPAS Knoten
                    setEinPASNodes(defaultPartIndex, part);
                    // bomKey
                    // DAIMLER-16208: 5. Enthält den Bomkey aus Dialog (nur setzen, wenn ein Wert existiert - iparts.js)
                    defaultPartIndex.setBomkey(part.getBomKey());
                    // DAIMLER-16208: Array über Nachfolger-Teile ("ersetzt durch")
                    setSuccessorParts(defaultPartIndex, part);

                    // Hier haben wir nun alle Werte für einen NDJSON Eintrag erzeugt
                    createNDJSONIndexEntry(defaultPartIndex, kgText, tuText, part, partName, addTexts, matDesc, exportHelper);
                }
            }
        }
    }

    /**
     * Erzeugt ein NDJSON Eintrag in jeder ElasticSearch Index Datei pro verfügbaren Sprache
     *
     * @param defaultPartIndex
     * @param kgText
     * @param tuText
     * @param part
     * @param partName
     * @param addTexts
     * @param matDesc
     * @param exportHelper
     */
    private void createNDJSONIndexEntry(ElasticSearchIndex defaultPartIndex, EtkMultiSprache kgText, EtkMultiSprache tuText,
                                        de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPart part,
                                        EtkMultiSprache partName, List<EtkMultiSprache> addTexts, EtkMultiSprache matDesc,
                                        FastSearchExportHelper exportHelper) {
        FastSearchTextHelper textHelper = exportHelper.getFastSearchTextHelper();
        List<String> exportLanguages = exportHelper.getExportLanguages();
        List<String> fallbackLanguages = exportHelper.getFallbackLanguages();
        for (String language : exportLanguages) {
            // KG-Text für aktuelle Sprache ausgeben
            if (kgText != null) {
                defaultPartIndex.setCgtext(kgText.getTextByNearestLanguage(language, fallbackLanguages));
            }
            // TU-Text für aktuelle Sprache ausgeben
            if (tuText != null) {
                defaultPartIndex.setCsgtext(tuText.getTextByNearestLanguage(language, fallbackLanguages));
            }
            // Teilebenennung für aktuelle Sprache ausgeben
            if ((partName != null) && !partName.isEmpty()) {
                defaultPartIndex.setPartname(partName.getTextByNearestLanguage(language, fallbackLanguages));
            } else if (language.equalsIgnoreCase(Language.DE.getCode())) {
                // Existiert keine Text-Id aber eine Benennung (alte Fahrzeuge/Aggregate), dann soll die Benennung nur für
                // DE gesetzt werden
                defaultPartIndex.setPartname(part.getName());
            }
            // Kombinierten Text für aktuelle Sprache ausgeben
            // DAIMLER-16208: 3. Enthält - wenn vorhanden - die kombinierten Texte / Ergänzungstexte
            if (!addTexts.isEmpty()) {
                String addText = textHelper.createAddTextForLanguage(addTexts, language, fallbackLanguages);
                if (StrUtils.isValid(addText)) {
                    defaultPartIndex.setPartdesc(addText);
                }
            }
            // Neutralen Text für aktuelle Sprache ausgeben (sollte bei allen Sprachen gleich sein)
            if ((matDesc != null) && !matDesc.isEmpty()) {
                defaultPartIndex.setMatdesc(matDesc.getTextByNearestLanguage(language, fallbackLanguages));
            }

            // Daten des DTO rausschreiben
            exportHelper.writeDataForLanguage(defaultPartIndex, language);
        }
    }

    private void setEinPASNodes(ElasticSearchIndex defaultPartIndex, de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPart part) {
        List<de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSEinPAS> einPASNodes = part.getEinPAS();
        // DAIMLER-16208: 6. Enthält den / die zugeordneten EinPAS-Knoten
        // 8. Leere / fehlende EinPAS-Knoten oder Nachfolger-Teile können weggelassen werden
        defaultPartIndex.setEinpas(null);
        if (Utils.isValid(einPASNodes)) {
            Set<String> result = new LinkedHashSet<>();
            if (USE_EINPAS_COMPACT_NOTATION) {
                einPASNodes.forEach(einPASNode -> result.add(einPASNode.getHg() + "." + einPASNode.getG() + "." + einPASNode.getTu())); // Ohne EinPAS-Version
            } else {
                einPASNodes.forEach(einPASNode -> result.add(einPASNode.getVersion() + "." + einPASNode.getHg() + "." + einPASNode.getG() + "." + einPASNode.getTu())); // Mit EinPAS-Version
            }
            defaultPartIndex.setEinpas(result);
        }
    }

    private void setSuccessorParts(ElasticSearchIndex defaultPartIndex, de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSPart part) {
        List<de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSReplacementPart> replacedByList = part.getReplacedBy();
        // DAIMLER-16208: 4. Enthält Array über Nachfolger-Teile ("ersetzt durch")
        // 8. Leere / fehlende EinPAS-Knoten oder Nachfolger-Teile können weggelassen werden
        defaultPartIndex.setSuccessor(null);
        if (Utils.isValid(replacedByList)) {
            List<String> result = new DwList<>(replacedByList.size());
            for (de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects.iPartsWSReplacementPart replPart : replacedByList) {
                result.add(replPart.getPartNo());
            }
            defaultPartIndex.setSuccessor(result);
        }
    }

    /**
     * Setzt die SAA Gültigkeiten (siehe iparts.js)
     *
     * @param saaValidity
     * @param defaultPartIndex
     */
    private void setSaaValidity(List<String> saaValidity, ElasticSearchIndex defaultPartIndex) {
        // DAIMLER-16208: 2. Leere / fehlende SAA-Codes sind als leeres Array / Feld abgebildet
        Set<String> cleanedSaas = new TreeSet<>();
        if ((saaValidity != null) && !saaValidity.isEmpty()) {
            // Existiert eine SAA die nicht mit Z anfängt wird nichts ausgegeben
            boolean findSaaWithoutPrefix = saaValidity.stream().anyMatch(saa -> saa.indexOf('Z') != 0);
            if (!findSaaWithoutPrefix) {
                // Durchlaufe alle SAAs und entferne all "Z", ".", "/" und " "
                saaValidity.forEach(saa -> {
                    String cleanedSaa = StrUtils.removeCharsFromString(saa.toLowerCase(), new char[]{ 'z', '.', '/', ' ' });
                    if (cleanedSaa.length() == 7) {
                        // Bei der Länge 7 wird eine "0" vorangestellt
                        cleanedSaas.add("0" + cleanedSaa);
                    } else {
                        cleanedSaas.add(cleanedSaa);
                    }
                });
            }
        }
        defaultPartIndex.setSaacodes(cleanedSaas);
    }

    /**
     * Setzt die Code in einer eigenen Unterstruktur im Export-JSON
     *
     * @param codes
     * @param defaultPartIndex
     * @throws BooleanFunctionSyntaxException
     */
    private void setCodes(String codes, ElasticSearchIndex defaultPartIndex) throws BooleanFunctionSyntaxException {
        // DAIMLER-16208: 7. Fehlende codes-Attribute werden als leere Coderegel ausgegeben: [{"must":[],"not":[]}]
        if (StrUtils.isValid(codes)) {
            List<ElasticSearchCodes> result = new ArrayList<>();
            Disjunction disjunction = DaimlerCodes.getDnfCode(codes);
            // Pro Konjunktion werden die positiven Code im "must" Tag und alle negativen Code im "not" Tag gesetzt
            for (Conjunction conjunction : disjunction) {
                PositiveAndNegativeTerms positiveAndNegativeTerms = conjunction.getPositiveAndNegativeTerms(false);
                ElasticSearchCodes elasticSearchCodes = new ElasticSearchCodes();
                elasticSearchCodes.setMust(positiveAndNegativeTerms.getPositiveTerms());
                elasticSearchCodes.setNot(positiveAndNegativeTerms.getNegativeTerms());
                result.add(elasticSearchCodes);
            }
            if (!result.isEmpty()) {
                defaultPartIndex.setCodes(result);
            }
        } else {
            // Sind keine Code vorhanden muss explizit eine leere Unterstruktur gesetzt werden
            defaultPartIndex.createEmptyCodes();
        }
    }

    private synchronized void incCurrentModelCount() {
        currentModelCount++;
        keyValueDBManager.setKeyExportedDataCount(currentModelCount);
    }

    public void setExportOnlyModifiedProducts(boolean exportOnlyModifiedProducts) {
        this.exportOnlyModifiedProducts = exportOnlyModifiedProducts;
    }

    private void showMessage(String message) {
        if ((guiSession != null) && guiSession.isActive()) {
            guiSession.invokeThreadSafeInSessionThread(() -> messageHelper.showMessage(message));
        }
    }
}
