/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.timer.AbstractDayOfWeekHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadExportExecutor;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadExportMessageFileHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.helper.multithreadexporthelper.MultiThreadKeyValueDBManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.action.ActionCanceller;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.BigZip;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Timer für den nach Baumustern/AS-Produktklassen gefilterten Stücklisten Exporter
 */
public class iPartsModelPartsListsExportTimer extends AbstractDayOfWeekHandler {

    public static final String TITLE = "!!Automatischer Export der nach Baumustern gefilterten Stücklisten";
    private static final String KEY_EXPORT_PREFIX = "iParts_export_model_partslists_";
    private static final String KEY_EXPORT_RUNNING = KEY_EXPORT_PREFIX + "running";
    private static final String KEY_EXPORT_CANCELED = KEY_EXPORT_PREFIX + "canceled";
    private static final String KEY_EXPORT_TOTAL_MODELS_COUNT = KEY_EXPORT_PREFIX + "total_models_count";
    private static final String KEY_EXPORT_DONE_MODELS_COUNT = KEY_EXPORT_PREFIX + "done_models_count"; // wird mit exported_ oder finished_ zu lang
    private static final String S3_PREFIX = "MODEL_PARTSLISTS_EXPORT";
    private static final String EXPORT_ROOT_NAME_PREFIX = S3_PREFIX + "_";
    private static final String DATEFORMAT_EXPORT_FILE = DateUtils.simpleTimeFormatyyyyMMddHHmmss;

    private static final boolean TEST_MODE = false;  // wenn true, dann wird als Json-Datei nur die ModelNo geschrieben

    public enum CompressionMode {
        SINGLE_FILE("!!Jede JSON-Datei gepackt"),   // Jedes JSON-File einzeln (Produkt - Baumuster)
        PRODUCT_FILES("!!Alle JSON-Dateien für ein Produkt gepackt"), // Alle JSON-Dateien für ein Produkt
        ALL_FILES("!!Alle JSON-Dateien gepackt");      // Alle JSON-Dateien (wird angepasst auch für den S3 Upload verwendet)

        private String displayName;

        CompressionMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static String[] getDisplayNames() {
            String[] displayNames = new String[values().length];
            int index = 0;
            for (CompressionMode compressionMode : values()) {
                displayNames[index] = compressionMode.getDisplayName();
                index++;
            }
            return displayNames;
        }

        public static CompressionMode getFromDisplayName(String displayName) {
            for (CompressionMode compressionMode : values()) {
                if (compressionMode.getDisplayName().equals(displayName)) {
                    return compressionMode;
                }
            }
            return CompressionMode.SINGLE_FILE;
        }
    }

    private boolean isExportWithGUI;
    private final MultiThreadKeyValueDBManager kvc;
    private final boolean testMode;
    private int testModeSmallDataCount;
    private CompressionMode exportMode;
    private boolean uploadToS3Active;
    private volatile int currentModelCount;
    private int threadCount;
    private DWFile tempDir;
    private DWFile exportTempRootDir;
    private DWFile destRootDir;
    private MultiThreadExportMessageFileHelper messageHelper;

    public static boolean isModelExportRunning(EtkProject project) {
        MultiThreadKeyValueDBManager kvc = new MultiThreadKeyValueDBManager(project, KEY_EXPORT_RUNNING, KEY_EXPORT_CANCELED, KEY_EXPORT_TOTAL_MODELS_COUNT, KEY_EXPORT_DONE_MODELS_COUNT);
        return kvc.isRunning();
    }

    public static void doRunModelDataScheduler(EtkProject project, Session session) {
        iPartsModelPartsListsExportTimer scheduler = new iPartsModelPartsListsExportTimer(project, session, true);
        scheduler.executeLogic();
        scheduler.stopThread();
    }

    public iPartsModelPartsListsExportTimer(EtkProject project, Session session) {
        this(project, session, false);
    }

    /**
     * Constructor
     *
     * @param project
     * @param session
     */
    public iPartsModelPartsListsExportTimer(EtkProject project, Session session, boolean isExportWithGUI) {
        super(project, session, iPartsExportPlugin.LOG_CHANNEL_WS_EXPORT_MODEL_PARTSLISTS, "model parts lists export timer");
        this.isExportWithGUI = isExportWithGUI;
        this.testMode = TEST_MODE;
        this.kvc = new MultiThreadKeyValueDBManager(project, KEY_EXPORT_RUNNING, KEY_EXPORT_CANCELED,
                                                    KEY_EXPORT_TOTAL_MODELS_COUNT, KEY_EXPORT_DONE_MODELS_COUNT);
    }

    @Override
    protected void executeLogic() {
        messageHelper = new MultiThreadExportMessageFileHelper(TITLE, null, isExportWithGUI, getLogChannel(), "!!Baumuster");
        messageHelper.resetErrorCount();
        this.currentModelCount = 0;

        // In der KEYVALUE Tabelle nachschauen, ob der Export noch läuft
        boolean exportAlreadyRunning = kvc.isRunning();
        if (!exportAlreadyRunning) {
            boolean doCalc = messageHelper.showStartExportConfirmationMessage(kvc);
            if (!doCalc) {
                return;
            }
            doCalcAndExportModelOrPCFilteredPartsLists();
            messageHelper.showProgress(false, kvc);
        } else {
            // Wird schon ausgeführt -> Meldung, dass Export jetzt nicht möglich ist
            messageHelper.showProgress(true, kvc);
        }
    }

    private void doCalcAndExportModelOrPCFilteredPartsLists() {
        // Eintrag isRunning in DB
        kvc.setRunning(true);

        // Log-Datei und MessageHelper initialisieren
        DWFile logFile = iPartsJobsManager.getInstance().exportJobRunning("MODEL_PARTS_LISTS");
        messageHelper.setLogFile(logFile);
        messageHelper.logMsg("Calculation and export of model parts lists started...");

        // Runnable für den kompletten Export
        FrameworkRunnable runnable = (thread) -> {
            String endMessage = "";
            try {
                // Export durchführen
                endMessage = calcAndExportModelOrPCFilteredPartsLists();
            } finally {
                // Eintrag isRunning zurücknehmen
                kvc.setRunning(false);

                // Nachricht am Ende anzeigen
                String showMsg;
                if (messageHelper.getErrorCount() > 0) {
                    showMsg = TranslationHandler.translate(TITLE) + " " + TranslationHandler.translate("!!ist mit Fehlern beendet (siehe Log-Datei).");
                    if (!kvc.isCanceled()) {
                        messageHelper.logMsg("Calculation and export of model parts lists finished with errors");
                    }
                    iPartsJobsManager.getInstance().jobError(logFile);
                } else {
                    showMsg = TranslationHandler.translate(TITLE) + " " + TranslationHandler.translate("!!ist beendet.");
                    if (!kvc.isCanceled()) {
                        messageHelper.logMsg("Calculation and export of model parts lists finished successfully");
                        iPartsJobsManager.getInstance().jobProcessed(logFile);
                    } else {
                        iPartsJobsManager.getInstance().jobError(logFile);
                    }
                }
                messageHelper.showMessage(showMsg + "\n\n" + endMessage);
            }
        };

        if (isExportWithGUI) {
            Session session = Session.get();
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
     * Alle Baumuster-gefilterten Stücklisten erzeugen, speichern und je nach Modus packen
     */
    private String calcAndExportModelOrPCFilteredPartsLists() {
        if (!iPartsPlugin.isWebservicePluginPresent()) {
            messageHelper.logError(iPartsConst.PLUGIN_CLASS_NAME_IPARTS_WEBSERVICE + " not found");
            return "!!Das DAIMLER iParts Webservice Plug-in wurde nicht gefunden!";
        }

        // Bei aktivem S3 Upload wird der exportMode ALL_FILES mit Spezialbehandlung forciert, wobei alle JSON-Dateien
        // mit speziellem Dateinamen inkl. Baumusternummer, Produkt und Konzernzugehörigkeit flach in das Export-Verzeichnis
        // exportiert und am Ende für den Upload in den S3 Bucket in ein großes ZIP-Archiv gepackt werden.
        uploadToS3Active = iPartsExportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsExportPlugin.CONFIG_MODEL_DATA_FAST_SEARCH_S3_ACTIVE);
        exportMode = uploadToS3Active ? CompressionMode.ALL_FILES : iPartsExportPlugin.getSelectedCompressionMode();
        messageHelper.logMsg("Compression mode: " + (uploadToS3Active ? "S3 bucket upload of single ZIP file with flat structure" : exportMode.name()));

        testModeSmallDataCount = iPartsExportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsExportPlugin.CONFIG_MODEL_DATA_EXPORT_PRODUCT_COUNT);
        if (testModeSmallDataCount > 0) {
            messageHelper.logMsg("Maximum number of products to export for each company: " + testModeSmallDataCount);
        }

        messageHelper.resetErrorCount();
        currentModelCount = 0;
        threadCount = iPartsExportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsExportPlugin.CONFIG_MODEL_DATA_EXPORT_THREAD_COUNT);
        StringBuilder endMessage = new StringBuilder();


        iPartsProductModelExportHelper.CalculatedVisibleProducts calculatedVisibleProducts = iPartsProductModelExportHelper.calculateVisibleProducts(getProject(),
                                                                                                                                                     true, testModeSmallDataCount);
        int totalProductsCount = calculatedVisibleProducts.getTotalProductCount();
        if (totalProductsCount > 0) {
            int totalModelsCount = calculatedVisibleProducts.getTotalModelOrPCCount();
            long startTime = System.currentTimeMillis();
            if (!createRootDirs()) {
                return "!!Fehler beim Erzeugen der Export-Verzeichnisse (siehe Log-Datei)!";
            }
            messageHelper.logInfoMsg("Starting export of " + totalProductsCount + " products ("
                                     + calculatedVisibleProducts.getCarProductModelsOrPCMapSize() + " MBAG; "
                                     + calculatedVisibleProducts.getTruckProductModelsOrPCMapSize() + " DTAG; "
                                     + calculatedVisibleProducts.getBothCompaniesModelsOrPCMapSize() + " both companies; "
                                     + totalModelsCount + " models or AS product classes for special products in total) with "
                                     + threadCount + " threads...");
            kvc.setKeyDataTotalCount(totalModelsCount);
            ExecutorService executorService = null;
            boolean finished;
            try {
                if (exportMode == CompressionMode.SINGLE_FILE) {
                    executorService = MultiThreadExportExecutor.createExecutor(threadCount);
                }

                if (exportModelsOrPCForCompany(executorService, calculatedVisibleProducts.getBothCompaniesModelsOrPCMap(), null)) {
                    if (exportModelsOrPCForCompany(executorService, calculatedVisibleProducts.getCarProductModelsOrPCMap(),
                                                   iPartsTransferConst.COMPANY_VALUE_MBAG)) {
                        exportModelsOrPCForCompany(executorService, calculatedVisibleProducts.getTruckProductModelsMapOrPCMap(),
                                                   iPartsTransferConst.COMPANY_VALUE_DTAG);
                    }
                }
            } finally {
                // Alle gewünschten Stücklisten der Baumuster wurden zum ExecutorService hinzugefügt -> Shutdown aufrufen und warten bis alle
                // Tasks abgearbeitet wurden
                finished = MultiThreadExportExecutor.executorAwaitTermination(executorService);
            }

            long exportDuration = System.currentTimeMillis() - startTime;
            if (finished) {
                boolean wasCanceled = kvc.isCanceled();
                messageHelper.writeFinishedEndMessage(kvc, endMessage, totalProductsCount, totalModelsCount, currentModelCount,
                                                      exportDuration, destRootDir);
                if (!wasCanceled) {
                    zipFilesForAllFiles(endMessage);

                    // Upload in den S3 Bucket
                    if (uploadToS3Active) {
                        messageHelper.logMsg("Uploading exported model parts lists to S3 bucket...");
                        iPartsModelPartsListsFastSearchS3Helper s3Helper = new iPartsModelPartsListsFastSearchS3Helper(iPartsExportPlugin.LOG_CHANNEL_WS_EXPORT_MODEL_PARTSLISTS,
                                                                                                                       "exported model parts lists");

                        DWFile zipFile = buildZipFile(destRootDir, getS3UploadFileName());
                        if (zipFile.exists() && s3Helper.uploadFile(zipFile.getName(), zipFile)) {
                            messageHelper.logMsg("Upload of exported model parts lists to S3 bucket successful. Local files will be deleted.");
                            destRootDir.deleteRecursively();
                        } else {
                            messageHelper.logError("Upload of exported model parts lists to S3 bucket failed. Local files are not deleted.");
                        }
                    }
                }
            } else {
                messageHelper.writeNotFinishedMessage(endMessage, totalProductsCount, totalModelsCount, exportDuration);
            }
            if (tempDir != null) {
                tempDir.deleteRecursively();
            }
        } else {
            kvc.setKeyDataTotalCount(0);
            messageHelper.logMsg("No products and models found.");
            endMessage.append(TranslationHandler.translate("!!Keine Produkte gefunden!"));
        }

        return endMessage.toString();
    }


    /**
     * Für eine Company alle Produkte und Baumuster/AS-Produktklassen behandlen
     *
     * @param executorService
     * @param productModelsOrPCMap
     * @param companyName
     * @return
     */
    private boolean exportModelsOrPCForCompany(ExecutorService executorService, Map<iPartsProductId, Set<String>> productModelsOrPCMap,
                                               String companyName) {
        if (kvc.isCanceled()) {
            return false;
        }
        boolean bothCompanies = companyName == null;
        if (bothCompanies) {
            companyName = uploadToS3Active ? "common" : iPartsTransferConst.COMPANY_VALUE_MBAG;
        }
        DWFile destCompanyDir;
        if (uploadToS3Active) {
            destCompanyDir = destRootDir;
        } else {
            destCompanyDir = messageHelper.createSubDir(destRootDir, companyName, "in destination directory");
        }
        if (destCompanyDir == null) {
            return false;
        }

        messageHelper.logMsg("Calculating " + (bothCompanies ? "both MBAG and DTAG" : companyName));
        for (Map.Entry<iPartsProductId, Set<String>> entry : productModelsOrPCMap.entrySet()) {
            if (kvc.isCanceled()) {
                return false;
            }
            String productNumber = DWFile.convertToValidFileName(entry.getKey().getProductNumber());
            DWFile destProductCompanyDir = null;

            switch (exportMode) {
                case SINGLE_FILE:
                    destProductCompanyDir = messageHelper.createSubDir(destCompanyDir, productNumber, "in destination directory");
                    break;
                case PRODUCT_FILES:
                    destProductCompanyDir = destCompanyDir;
                    break;
                case ALL_FILES:
                    destProductCompanyDir = destCompanyDir;
                    break;
            }
            if (destProductCompanyDir == null) {
                return false;
            }

            if (!exportModelsOrPCForOneProduct(executorService, entry.getKey(), entry.getValue(), productNumber, companyName,
                                               destCompanyDir, destProductCompanyDir)) {
                return false;
            } else if (bothCompanies && !uploadToS3Active && ((exportMode == CompressionMode.PRODUCT_FILES) || (exportMode == CompressionMode.ALL_FILES))) {
                // Exportierte Produkte vom MBAG-Verzeichnis ins DTAG-Verzeichnis kopieren
                DWFile destDTAGDir = messageHelper.createSubDir(destRootDir, iPartsTransferConst.COMPANY_VALUE_DTAG, "in destination directory");
                if (destDTAGDir == null) {
                    return false;
                }
                DWFile destProductDTAGDir = destDTAGDir;
                if (destProductCompanyDir != destCompanyDir) {
                    destProductDTAGDir = messageHelper.createSubDir(destDTAGDir, productNumber, "in destination directory");
                }
                List<DWFile> mbagFiles = destProductCompanyDir.listDWFiles();
                for (DWFile mbagFile : mbagFiles) {
                    mbagFile.copy(destProductDTAGDir, true);
                }
            }
        }
        messageHelper.logMsg("Finished scheduling " + companyName);
        return true;
    }

    private boolean exportModelsOrPCForOneProduct(ExecutorService executorService, iPartsProductId productId, Set<String> modelNoOrPCSet,
                                                  String productNumber, String companyName, DWFile destCompanyDir, DWFile destProductCompanyDir) {
        int modelsCount = modelNoOrPCSet.size();
        iPartsProduct product = iPartsProduct.getInstance(getProject(), productId);
        boolean isSpecialProduct = product.isSpecialCatalog();
        messageHelper.logMsg("Calculating product " + productId.getProductNumber() + " with " + modelsCount
                             + (isSpecialProduct ? " AS product classes" : " models") + "...");
        BigZip zipper = null;
        boolean ownExecutorService = false;
        try {
            if ((exportMode == CompressionMode.PRODUCT_FILES) || (exportMode == CompressionMode.ALL_FILES)) {
                executorService = MultiThreadExportExecutor.createExecutor(threadCount);
                ownExecutorService = true;
                if (!uploadToS3Active) {
                    DWFile zipFile = buildZipFile(destProductCompanyDir, productNumber);
                    zipper = openAndInitBigZip(zipFile);
                    if (zipper == null) {
                        return false;
                    }
                }
            }

            for (String modelNoOrPC : modelNoOrPCSet) {
                if (kvc.isCanceled()) {
                    break;
                }
                Runnable calculationRunnable = createExportModelOrPCPartsListsRunnable(productId, isSpecialProduct, modelNoOrPC,
                                                                                       companyName, destCompanyDir, destProductCompanyDir,
                                                                                       zipper);
                executorService.execute(calculationRunnable);
            }
            messageHelper.logMsg("Product " + productNumber + " scheduled");
            if ((exportMode == CompressionMode.PRODUCT_FILES) || (exportMode == CompressionMode.ALL_FILES)) {
                // Alle gewünschten Stücklisten der Baumuster wurden zum ExecutorService hinzugefügt -> Shutdown aufrufen und warten bis alle
                // Tasks abgearbeitet wurden
                boolean finished = MultiThreadExportExecutor.executorAwaitTermination(executorService);
                if (finished) {
                    messageHelper.logMsg("Product " + productNumber + " finished");
                    if (zipper != null) {
                        zipper.closeZip();
                    } else if (!uploadToS3Active) {
                        messageHelper.logError("no BigZip defined");
                        return false;
                    }
                } else {
                    messageHelper.logMsg("Product " + productNumber + " canceled");
                }
            }
        } finally {
            if (ownExecutorService) {
                executorService.shutdown();
            }
        }
        if (kvc.isCanceled()) {
            return false;
        }
        return true;
    }

    private Runnable createExportModelOrPCPartsListsRunnable(iPartsProductId productId, boolean isSpecialProduct, String modelNoOrPC,
                                                             String companyName, DWFile destCompanyDir, DWFile destProductCompanyDir,
                                                             BigZip zipper) {
        return () -> {
            String modelOrPCString = isSpecialProduct ? "AS product class " : "model ";
            try {
                if (kvc.isCanceled()) {
                    return;
                }

                messageHelper.logMsg("Exporting " + modelOrPCString + modelNoOrPC + " for product " + productId.getProductNumber());

                if (testMode) {
                    exportFile(createExportFileName(productId, modelNoOrPC, companyName), modelNoOrPC, destProductCompanyDir, zipper);
                } else {
                    String jsonString = iPartsProductModelExportHelper.calculateJsonForModelOrPCPartsLists(productId, modelNoOrPC,
                                                                                                           messageHelper,
                                                                                                           getLogChannel(),
                                                                                                           getProject());
                    if (jsonString != null) {
                        exportFile(createExportFileName(productId, modelNoOrPC, companyName), jsonString, destProductCompanyDir, zipper);
                    }
                }

                String modelOrPCPrefix = isSpecialProduct ? "AS product class " : "Model ";
                if (kvc.isCanceled()) {
                    messageHelper.logMsg(modelOrPCPrefix + modelNoOrPC + " for product " + productId.getProductNumber() + " canceled");
                    return;
                }

                // Exportiertes Baumuster vom MBAG-Verzeichnis ins DTAG-Verzeichnis kopieren
                if (exportMode == CompressionMode.SINGLE_FILE) {
                    DWFile destDTAGDir = messageHelper.createSubDir(destRootDir, iPartsTransferConst.COMPANY_VALUE_DTAG, "in destination directory");
                    if (destDTAGDir == null) {
                        return;
                    }
                    DWFile destProductDTAGDir = destDTAGDir;
                    if (destProductCompanyDir != destCompanyDir) {
                        destProductDTAGDir = messageHelper.createSubDir(destDTAGDir, productId.getProductNumber(), "in destination directory");
                    }
                    List<DWFile> mbagFiles = destProductCompanyDir.listDWFiles();
                    for (DWFile mbagFile : mbagFiles) {
                        mbagFile.copy(destProductDTAGDir, true);
                    }
                }

                messageHelper.logMsg(modelOrPCPrefix + modelNoOrPC + " for product " + productId.getProductNumber() + " finished");
                incCurrentModelCount(1);
            } catch (Exception e) {
                messageHelper.logError("Error during zipping the export of " + modelOrPCString + modelNoOrPC + " for product "
                                       + productId.getProductNumber() + ":");
                Logger.logExceptionWithoutThrowing(getLogChannel(), LogType.ERROR, e);
                messageHelper.addErrorToLog(Utils.exceptionToString(e));
            }
        };
    }

    private String createExportFileName(iPartsProductId productId, String modelNoOrPC, String companyName) {
        if (uploadToS3Active) {
            return modelNoOrPC + "_" + productId.getProductNumber() + "_" + companyName + "_plst";
        } else {
            return modelNoOrPC;
        }
    }

    private boolean exportFile(String fileName, String content, DWFile destProductCompanyDir, BigZip zipper) {
        if (uploadToS3Active) {
            DWFile file = DWFile.get(destProductCompanyDir, messageHelper.buildJsonFileName(fileName));
            try {
                file.writeTextFile(content.getBytes(StandardCharsets.UTF_8));
                return true;
            } catch (IOException e) {
                Logger.logExceptionWithoutThrowing(getLogChannel(), LogType.ERROR, e);
                messageHelper.addErrorToLog(Utils.exceptionToString(e));
                return false;
            }
        } else {
            switch (exportMode) {
                case SINGLE_FILE:
                    return createSingleZip(destProductCompanyDir, fileName, content);
                case PRODUCT_FILES:
                case ALL_FILES:
                    return addEntryToZip(zipper, fileName, content);
            }
            return false;
        }
    }

    private BigZip openAndInitBigZip(DWFile zipFile) {
        return openAndInitBigZip(zipFile, null);
    }

    private BigZip openAndInitBigZip(DWFile zipFile, ActionCanceller actionCanceller) {
        BigZip zipper = new BigZip(zipFile.getAbsolutePath());
        if (!zipper.prepareUTF8Zip(actionCanceller)) {
            logZipErrors(zipper);
            return null;
        }
        return zipper;
    }

    private boolean createSingleZip(DWFile destDir, String fileName, String content) {
        DWFile zipFile = buildZipFile(destDir, fileName);
        BigZip zipper = openAndInitBigZip(zipFile);
        if (zipper == null) {
            return false;
        }
        try {
            return addEntryToZip(zipper, fileName, content);
        } finally {
            zipper.closeZip();
            logZipErrors(zipper);
        }
    }

    private boolean addEntryToZip(BigZip zipper, String entryName, String content) {
        if (zipper == null) {
            messageHelper.logError("no BigZip created");
            return false;
        }
        if (!zipper.addContent(messageHelper.buildJsonFileName(entryName), content)) {
            logZipErrors(zipper);
            return false;
        }
        return true;
    }

    private void zipFilesForAllFiles(StringBuilder endMessage) {
        if (exportMode == CompressionMode.ALL_FILES) {
            long startTime = System.currentTimeMillis();
            messageHelper.logMsg("Packing files");

            if (uploadToS3Active) {
                zipBigFilesFromDir("");
            } else {
                if (zipBigFilesFromDir(iPartsTransferConst.COMPANY_VALUE_MBAG) && !kvc.isCanceled()) {
                    zipBigFilesFromDir(iPartsTransferConst.COMPANY_VALUE_DTAG);
                }
            }

            boolean wasCanceled = kvc.isCanceled();
            if (wasCanceled) {
                messageHelper.logMsg("Packing files was canceled");
                endMessage.append("\n");
                endMessage.append(TranslationHandler.translate("!!Das Packen aller Dateien wurde abgebrochen!"));
            } else {
                String durationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, false,
                                                                           false, Language.EN.getCode());
                messageHelper.logMsg("Finished packing files in " + durationString);
                endMessage.append("\n");
                endMessage.append(TranslationHandler.translate("!!Das Packen aller Dateien dauerte %1.", durationString));
            }
        }
    }

    private boolean zipBigFilesFromDir(String companyName) {
        // Leerer companyName für S3 Upload
        DWFile destCompanyDir = !companyName.isEmpty() ? destRootDir.getChild(companyName) : destRootDir;
        if (!destCompanyDir.isDirectory()) {
            return false;
        }
        DWFile zipFile = buildZipFile(destCompanyDir, !companyName.isEmpty() ? companyName : getS3UploadFileName());
        ActionCanceller actionCanceller = () -> kvc.isCanceled();
        BigZip zipper = openAndInitBigZip(zipFile, actionCanceller);
        if (zipper == null) {
            return false;
        }
        try {
            return zipper.zipCurrentDir(destCompanyDir.getAbsolutePath(), true);
        } finally {
            zipper.closeZip();
            logZipErrors(zipper);
        }
    }

    private String getS3UploadFileName() {
        return S3_PREFIX + "-" + StrUtils.removeFirstCharacterIfCharacterIs(destRootDir.getName(), EXPORT_ROOT_NAME_PREFIX);
    }

    /**
     * Ein temporäres Verzeichnis und das oberste Verzeichnis ({@link #getExportRootDirName()}) erzeugen.
     *
     * @return
     */
    protected boolean createTempDir() {
        try {
            tempDir = DWFile.createTempDirectory("daim");
            if (tempDir != null) {
                exportTempRootDir = messageHelper.createSubDir(tempDir, getExportRootDirName(), "in temp directory");
            } else {
                messageHelper.logError("Cannot create temp directory!");
            }
        } catch (Exception e) {
            messageHelper.logExceptionWithoutThrowing(e);
            return false;
        }
        return exportTempRootDir != null;
    }

    /**
     * Das oberste Verzeichnis ({@link iPartsExportPlugin#getDirForModelPartsListsExport()}) erzeugen.
     * Ebenso ein temporäres Verzteichnis.
     *
     * @return
     */
    private boolean createRootDirs() {
        DWFile destDir = iPartsExportPlugin.getDirForModelPartsListsExport();
        if (destDir == null) {
            return false;
        }
        destRootDir = messageHelper.createRootDir(destDir, getExportRootDirName());
        if (destRootDir == null) {
            return false;
        }
        return true;
    }

    protected String getExportRootDirName() {
        return EXPORT_ROOT_NAME_PREFIX + DateUtils.getCurrentDateFormatted(DATEFORMAT_EXPORT_FILE);
    }

    private void logZipErrors(BigZip zipper) {
        if ((zipper != null) && zipper.hasErrors()) {
            String errors = zipper.getErrorMsg();
            if (StrUtils.isValid(errors)) {
                messageHelper.logError(errors);
            }
            errors = zipper.getExceptionMesssage();
            if (StrUtils.isValid(errors)) {
                messageHelper.logError(errors);
            }
            zipper.clearErrors();
        }
    }

    private synchronized void incCurrentModelCount(int count) {
        currentModelCount += count;
        kvc.setKeyExportedDataCount(currentModelCount);
    }

    private DWFile buildZipFile(DWFile destDir, String fileNameWithouExt) {
        return destDir.getChild(messageHelper.buildFileName(fileNameWithouExt, MimeTypes.EXTENSION_ZIP));
    }
}
