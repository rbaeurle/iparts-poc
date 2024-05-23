/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.rftsx;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.timer.DayOfTheWeekTimer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.events.RFTSxEnabledChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDateTimeHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsTimeInterval;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.events.RFTSxRunningChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.cemat.CematModuleDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.connect.WireHarnessDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DialogKgTuPredictionImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.epep.update.ePEPIdentResponseImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsTopTUsImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.AbstractMBSDataHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.MBSDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.helper.MBSImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.structure.AbstractMBSStructureHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.MigrationDialogSeriesMainImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.srm.SrmSupplierPartNoMappingDataImporter;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.monitor.directory.AbstractDirectoryMonitor;
import de.docware.framework.modules.gui.misc.monitor.directory.DirectoryAgent;
import de.docware.framework.modules.gui.misc.monitor.directory.DirectoryEntryType;
import de.docware.framework.modules.gui.misc.monitor.directory.DirectoryMonitorFactory;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.os.OsUtils;

import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Stellt die Verbindung zwischen Datei und Importer her und ruft diesen auf
 */
public class RFTSXImportHelper extends ImportExportLogHelper {

    public static final String RFTSX_DELIMITER = "@RFTSX@";
    private static RFTSXImportHelper instance = null;

    private Map<ImporterTypes, RFTSXImportFunction> importerList;
    private EtkProject project;
    private long startTime;
    private boolean rftsxImportEnabled;
    private boolean rftsxIsRunning;
    private int errorCount;
    private boolean isRftsxImportCanceled;
    private String cancelMsg;
    private RFTSXImportFunction currentRFTSxImportHelper;
    private Date datasetDate;
    private FileMonitorHandler fileProcessor;
    private String monitorID;
    private Set<String> validImporterTypes;
    private DayOfTheWeekTimer rftsxDailyStartTimer;
    private DayOfTheWeekTimer rftsxDailyEndTimer;


    public static RFTSXImportHelper createInstance(EtkProject project) {
        instance = new RFTSXImportHelper(project);
        return instance;
    }

    public static RFTSXImportHelper getInstance() {
        return instance;
    }

    private RFTSXImportHelper(EtkProject project) {
        super();
        this.project = project;
        this.rftsxImportEnabled = iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_RFTSX_HANDLE_IMPORT);
        this.rftsxIsRunning = false;
        this.isRftsxImportCanceled = false;
        this.cancelMsg = "";
        this.currentRFTSxImportHelper = null;
        initImporterMapping();
    }

    public EtkProject getProject() {
        return project;
    }


    public boolean isRFTSxImportEnabled() {
        return rftsxImportEnabled;
    }

    public boolean isRftsxPictureImportEnabled() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_IMPORT_RFTSX_IMAGES)
               && !iPartsImportPlugin.getPluginConfig().getConfigValueAsRawString(iPartsImportPlugin.CONFIG_IMPORT_RFTSX_IMAGES_UNZIP_ROOT_DIR, "").isEmpty();
    }

    public void setRFTSxImportEnabled(boolean rftsxImportEnabled) {
        this.rftsxImportEnabled = rftsxImportEnabled;
    }

    public boolean isRFTSxRunning() {
        return rftsxIsRunning;
    }

    public void cancelRFTSxImport(String msg) {
        if (isRFTSxRunning()) {
            isRftsxImportCanceled = true;
            cancelMsg = msg;
            if (currentRFTSxImportHelper != null) {
                currentRFTSxImportHelper.cancelImport();
            }
            iPartsJobsManager.getInstance().jobCancelled(getLogFile(), false);
        }
    }

    private void initImporterMapping() {
        importerList = new HashMap<>();

        // TAL40A-Importer
        importerList.put(ImporterTypes.TAL40A, new RFTSXImportFunction("iPartsTAL40A_Importer", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MadTal40AImporter(getProject());
            }
        });

        // TAL46A-Importer
        importerList.put(ImporterTypes.TAL46A, new RFTSXImportFunction("iPartsTAL46A_Importer", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MadTal46AImporter(getProject());
            }
        });

        // TAL83A-Importer
        importerList.put(ImporterTypes.TAL83A, new RFTSXImportFunction("iPartsTAL83A_Importer", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MadTal83AImporter(getProject());
            }
        });

        // TAL95M-Importer
        importerList.put(ImporterTypes.TAL95M, new RFTSXImportFunction("iPartsTAL95M_Importer", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADTal95AImporter(getProject());
            }
        });

        // TAL31A-Importer
        importerList.put(ImporterTypes.TAL31A, new RFTSXImportFunction("iPartsTAL31A_Importer", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new MADMasterDataMainImporter(getProject());
            }
        });

        // TAL30A-Importer
        importerList.put(ImporterTypes.TAL30A, new RFTSXImportFunction("iPartsTAL30A_Importer", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                MigrationDialogSeriesMainImporter importer = new MigrationDialogSeriesMainImporter(getProject());
                // Weil wir noch keinen TAL30A Importer haben (mehrere Baureihen in einer .gz Datei) muss der Datenstand
                // hier übergeben werden
                if (datasetDate != null) {
                    importer.setDatasetDate(datasetDate);
                }
                return importer;
            }
        });
        // TAL47S-Importer
        importerList.put(ImporterTypes.TAL47S, new RFTSXImportFunction("iPartsTAL47S_Importer", getLogLanguage(), getMessageLog()) {

            @Override
            public AbstractDataImporter createImporter() {
                return new MADTal47SImporter(getProject());
            }
        });

        // RSK-Importer
        importerList.put(ImporterTypes.RSK, new RFTSXImportFunction("iPartsRSK_Importer", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new RSKLexikonImporter(getProject());
            }
        });

        // DASTi-Importer
        importerList.put(ImporterTypes.DASTI, new RFTSXImportFunction("iPartsDASTi_Importer", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new DASTiPictureImporter(getProject());
            }
        });

        // KI Mapping Importer
        importerList.put(ImporterTypes.KI_MAPPING, new RFTSXImportFunction("iPartsKgTuPredictionImporter", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new DialogKgTuPredictionImporter(getProject());
            }
        });

        // ePEP: Import von Ident-Rückmeldungen, (elektronischer Produktions-Einsatz-Prozess)
        importerList.put(ImporterTypes.EPEP, new RFTSXImportFunction("iPartsEPEPIdentResponse_Importer", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new ePEPIdentResponseImporter(getProject());
            }
        });

        // Alle möglichen SAP MBS Stammdaten importieren
        importerList.put(ImporterTypes.MBS_MASTER, new RFTSXImportFunction("iPartsMBSMasterImporter", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                MBSDataImporter importer = new MBSDataImporter(getProject(), "!!MBS Stammdaten");
                List<AbstractMBSDataHandler> handlers = MBSImportHelper.createAllMasterDataHandlers(getProject(), importer);
                importer.setDataHandlers(handlers.toArray(new AbstractMBSDataHandler[handlers.size()]));
                return importer;
            }
        });

        // SAP MBS Strukturdaten
        importerList.put(ImporterTypes.MBS_STRUCT, new RFTSXImportFunction("iPartsMBSStructImporter", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                MBSDataImporter importer = new MBSDataImporter(getProject(), "!!MBS Strukturdaten");
                List<AbstractMBSStructureHandler> handlers = MBSImportHelper.createAllStructureDataHandlers(getProject(),
                                                                                                            importer, true);
                if (!handlers.isEmpty()) {
                    // Bei den MBS-Strukturdaten gibt es einen DistributionHandler, der anstatt der einzelnen SubHandler
                    // registriert werden muss
                    importer.setDataHandlers(handlers.get(0).getDistributionHandler());
                }
                return importer;
            }
        });

        // DAIMLER-11961, CEMaT: Importer für die EinPAS-Knoten aus CEMaT
        importerList.put(ImporterTypes.CEMAT_MAPPING, new RFTSXImportFunction("iPartsCematModuleImporter", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new CematModuleDataImporter(getProject());
            }
        });

        // DAIMLER-12261, Top-TUs in die RFTS/x-Verarbeitung, Importer einbinden
        importerList.put(ImporterTypes.TOP_TUS, new RFTSXImportFunction("iPartsTopTUsImporter", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new iPartsTopTUsImporter(getProject());
            }
        });

        // DAIMLER-12396, Connect-Leitungssätze in die RFTS/x-Verarbeitung, Importer einbinden
        importerList.put(ImporterTypes.CONNECT_WIRE_HARNESS, new RFTSXImportFunction("WireHarnessDataImporter", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new WireHarnessDataImporter(getProject());
            }
        });

        // DAIMLER-13396, Import Sachnummer zu Lieferantennummer aus SRM über RFTS/x
        importerList.put(ImporterTypes.SNR2SUPPSNR_SRM, new RFTSXImportFunction("SnrToSupplierSnrDataImporter", getLogLanguage(), getMessageLog()) {
            @Override
            public AbstractDataImporter createImporter() {
                return new SrmSupplierPartNoMappingDataImporter(getProject());
            }
        });

    }

    /**
     * Bereitet den Import der durch RFTSx detektierten Datei vor und ruft den zugeordneten Importer auf.
     *
     * @param importFile
     * @param originalPath
     * @return Name vom Unterverzeichnis für das Archiv
     */
    public String handleRFTSxInputFile(DWFile importFile, String originalPath) {
        isRftsxImportCanceled = false;
        cancelMsg = "";
        currentRFTSxImportHelper = null;
        startTime = System.currentTimeMillis();
        errorCount = 0;
        boolean result = true;
        String subDirForArchive = "";
        initLogFile(importFile.getAbsolutePath(), originalPath);
        ImporterTypes importType = null;
        try {
            importType = ImporterTypes.getImportType(importFile);
            if (importType == ImporterTypes.UNKNOWN) {
                addLogError(translateForLog("!!Unbekannter Dateiname %1", importFile.getName()));
                result = false;
                return iPartsConst.SUBDIR_UNKNOWN;
            }
            if (!validImporterTypes.contains(importType.name())) {
                addLogMsg(translateForLog("!!Datei \"%1\"wird nicht verarbeitet, weil Importtyp \"%2\" nicht aktiv ist", importFile.getName(), importType.name()));
                result = false;
                return iPartsConst.SUBDIR_NOT_ACTIVE;
            }

            if (importType == ImporterTypes.TAL30A) {
                datasetDate = iPartsMADDateTimeHandler.extractTalXDateFromFilename(importFile.getPath(), null);
            } else if (importType == ImporterTypes.DASTI) {
                Date tmpDate = iPartsMADDateTimeHandler.extractDASTiDateFromFilename(importFile.getPath());
                if (tmpDate == null) {
                    addLogError(translateForLog("!!Ungültiger Dateiname %1", importFile.getName()));
                    result = false;
                    return iPartsConst.FILENAME_INVALID;
                }

                // In der iPartsPlugin-Config ist der automatische Empfang der Bilder abgeschaltet.
                // ==> Die Importdatei soll per Definition unverarbeitet ins Archiv verschoben werden.
                if (!isRftsxPictureImportEnabled()) {
                    // Zur Tranparenz ein Logfile Eintrag.
                    addLogMsg(translateForLog("!!Der Import von DASTi Bildarchiven über RFTS/x ist abgeschaltet."));
                    return iPartsConst.SUBDIR_PROCESSED;
                }
            }

            // Aktiv-Zustand der DB-Verbindung vom RFTS/x EtkProject überprüfen
            iPartsPlugin.assertProjectDbIsActive(project, "RFTS/x", iPartsImportPlugin.LOG_CHANNEL_RFTSX);

            DWFile destUnpackDir = null;
            boolean doImport = true;
            switch (importType) {
                case TAL30A:
                case TAL95M:
                case TAL47S:

                case DASTI:  //  <<=== Das steuert, dass die Dateien hier entpackt werden!

                    // TAL30; 47S und 95 müssen umgepackt werden => Unterverzeichnis anlegen
                    destUnpackDir = DWFile.get(importFile.getParentDWFile(), importFile.extractFileName(false));
                    doImport = destUnpackDir.mkDirWithRepeat();
                    if (!doImport) {
                        //Fehlermeldung
                        addLogError(translateForLog("!!Unterverzeichnis \"%1\" kann nicht erstellt werden", destUnpackDir.getAbsolutePath()));
                        subDirForArchive = iPartsConst.SUBDIR_ERROR + OsUtils.FILESEPARATOR + importType.name();
                        result = false;
                    }
                    break;

            }
            if (isRftsxImportCanceled) {
                subDirForArchive = iPartsConst.SUBDIR_CANCELLED + OsUtils.FILESEPARATOR + importType.name();
                doImport = false;
            }

            if (doImport) {
                // Dateien aus der GZ-Datei ggf umpacken
                List<DWFile> importFiles = ImporterTypes.splitImportFile(importType, importFile, destUnpackDir);
                if (isRftsxImportCanceled) {
                    importFiles.clear();
                }
                if (!importFiles.isEmpty()) {
                    switch (importType) {
                        case TAL30A: // Bewegungsdaten: für jede Datei den Baureihen-Importer aufrufen
                            for (DWFile currentImportFile : importFiles) {
                                callImporterByType(importType, currentImportFile);
                                if (isRftsxImportCanceled) {
                                    break;
                                }
                            }
                            break;
                        case TAL95M:
                        case TAL47S:
                            callMultipleDifferentFilesXMLImporter(importFiles, importType);
                            break;

                        case DASTI:
                            // DASTi-Bilder: für alle Dateien den DASTi-Bild-Importer aufrufen
                            callImporterByType(importType, importFiles);
                            if (isRftsxImportCanceled) {
                                break;
                            }

                            break;

                        default:
                            DWFile currentImportFile = importFiles.get(0);
                            callImporterByType(importType, currentImportFile);
                            break;
                    }
                } else {
                    if (!isRftsxImportCanceled) {
                        // Fehler beim umpacken
                        addLogError("!!Fehler beim Entpacken");
                        subDirForArchive = iPartsConst.SUBDIR_ERROR + OsUtils.FILESEPARATOR + importType.name();
                    } else {
                        subDirForArchive = iPartsConst.SUBDIR_CANCELLED + OsUtils.FILESEPARATOR + importType.name();
                    }
                    result = false;
                }
            }
            if (destUnpackDir != null) {
                destUnpackDir.deleteRecursively();
            }
            if (result && isRftsxImportCanceled) {
                subDirForArchive = iPartsConst.SUBDIR_CANCELLED + OsUtils.FILESEPARATOR + importType.name();
                result = false;
            }
        } catch (Exception e) {
            addLogError(e.getMessage());
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.ERROR, e);
        } finally {
            result &= errorCount == 0;
            if (subDirForArchive.isEmpty()) {
                if (result) {
                    subDirForArchive = iPartsConst.SUBDIR_PROCESSED;
                } else {
                    subDirForArchive = iPartsConst.SUBDIR_ERROR;
                }
                if (importType != null) {
                    subDirForArchive += OsUtils.FILESEPARATOR + importType.name();
                }
            }
            endLogFile(result);
        }
        return subDirForArchive;
    }

    /**
     * Behandelt den Spezialfall von Importer mit mehreren unterschiedlichen Dateien in einem Verzeichnis
     *
     * @param importFiles
     * @param importType
     */
    private void callMultipleDifferentFilesXMLImporter(List<DWFile> importFiles, ImporterTypes importType) {
        if (!ImporterTypes.isImporterWithDifferentFiles(importType)) {
            addLogError(translateForLog("!!Importtyp \"%1\" ist kein RFTSx XML Sonderimporter", importType.name()));
            return;
        }
        // in der TAL95M GZ Datei sind 2 Dateien verpackt (XML -> zum Importer; XSD: wird i.A. ignoriert)
        for (DWFile currentImportFile : importFiles) {
            DWFile workFile = DWFile.get(currentImportFile);
            if (MimeTypes.hasExtension(currentImportFile, MimeTypes.EXTENSION_GZ)) {
                workFile = DWFile.get(workFile.getParentDWFile(), workFile.extractFileName(false));
            }
            if (MimeTypes.hasExtension(workFile, MimeTypes.EXTENSION_XML)) {
                RFTSXImportFunction rftsxImportHelper = importerList.get(importType);
                if (rftsxImportHelper == null) {
                    addLogError(translateForLog("!!Kein Importer für \"%1\" definiert", importType.name()));
                    continue;
                }
                callImporter(rftsxImportHelper, currentImportFile);
            }
            if (isRftsxImportCanceled) {
                break;
            }
        }
    }


    /**
     * Einen Importer via übergebenen {@link ImporterTypes} bestimmen und diesen mit der übergebenen Importdatei aufrufen
     *
     * @param importType
     * @param currentImportFiles
     */
    private void callImporterByType(ImporterTypes importType, List<DWFile> currentImportFiles) {
        RFTSXImportFunction rftsxImportHelper = importerList.get(importType);
        if (rftsxImportHelper != null) {
            callImporter(rftsxImportHelper, currentImportFiles);
        } else {
            // Importer für diesen Typ nicht definiert
            addLogError(translateForLog("!!Kein Importer für \"%1\" definiert", importType.name()));
        }
    }


    /**
     * Einen Importer via übergebenen {@link ImporterTypes} bestimmen und diesen mit der übergebenen Importdatei aufrufen
     *
     * @param importType
     * @param currentImportFile
     */
    private void callImporterByType(ImporterTypes importType, DWFile currentImportFile) {
        List<DWFile> files = new ArrayList<>(1);
        files.add(currentImportFile);
        callImporterByType(importType, files);
    }


    /**
     * Importer mit Inputdatei aufrufen
     *
     * @param rftsxImportHelper
     * @param currentImportFile
     */
    private void callImporter(RFTSXImportFunction rftsxImportHelper, DWFile currentImportFile) {
        List<DWFile> files = new ArrayList<>(1);
        files.add(currentImportFile);
        callImporter(rftsxImportHelper, files);
    }


    /**
     * Importer mit Inputdatei aufrufen
     *
     * @param rftsxImportHelper
     * @param currentImportFiles
     */
    private void callImporter(RFTSXImportFunction rftsxImportHelper, List<DWFile> currentImportFiles) {
        currentRFTSxImportHelper = rftsxImportHelper;
        long startTime = System.currentTimeMillis();
        for (DWFile file : currentImportFiles) {
            rftsxImportHelper.addFileForWork(file);
        }
        rftsxImportHelper.run(null);
        long importDuration = System.currentTimeMillis() - startTime;
        String timeDurationString = DateUtils.formatTimeDurationString(importDuration, false, true, getLogLanguage());
        addLogMsg(translateForLog("!!Importdauer: %1", timeDurationString));
        getMessageLog().fireMessage("");
        currentRFTSxImportHelper = null;
        errorCount += rftsxImportHelper.errorCount;
    }

    /**
     * Sucht in den Importdateien nach einer bestimmten Datei (TAL31A Dateitypen)
     *
     * @param importFiles
     * @param importTAL31AType
     * @return
     */
    private DWFile searchInImportFiles(List<DWFile> importFiles, ImporterTAL31ATypes importTAL31AType) {
        for (DWFile currentFile : importFiles) {
            ImporterTAL31ATypes importType = ImporterTAL31ATypes.getImportType(currentFile);
            if (importType == importTAL31AType) {
                return currentFile;
            }
        }
        return null;
    }

    /**
     * Initialisiert die Log-Datei
     *
     * @param originalPath
     * @parma path
     */
    private void initLogFile(String path, String originalPath) {
        getMessageLog().addMessageEventListener(this);
        // fest iPartsConst.LOG_FILES_LANGUAGE für die Dateinamen verwenden
        DWFile runningLogFile = iPartsJobsManager.getInstance().jobRunning("RFTSx Import " + DWFile.extractFileName(path, true));
        setLogFileWithCheck(runningLogFile, true);
        addLogMsg(translateForLog("!!Importiere %1", DWFile.extractFileName(originalPath, true)));
        rftsxIsRunning = true;
        updateButtonActiveState();
    }

    /**
     * Finalisiert die Log-Datei
     *
     * @param result
     */
    private void endLogFile(boolean result) {
        try {
            long importDuration = System.currentTimeMillis() - startTime;
            String timeDurationString = DateUtils.formatTimeDurationString(importDuration, false, true, getLogLanguage());
            addLogMsg(translateForLog("!!Gesamte RFTS/x Importdauer: %1", timeDurationString));
            getMessageLog().fireMessage("");
            if (result) {
                getMessageLog().fireMessageWithSeparators("!!RFTS/x Import erfolgreich abgeschlossen", MessageLogOption.TIME_STAMP);
                Java1_1_Utils.sleep(1000); // 1 Sekunde warten, damit das RFTS/x Log auf jeden Fall nach den Import-Logs beendet wird
                DWFile processedLogFile = iPartsJobsManager.getInstance().jobProcessed(getLogFile());
                setLogFileWithCheck(processedLogFile, false);
            } else {
                if (isRftsxImportCanceled) {
                    addLogError(cancelMsg);
                }
                getMessageLog().fireMessageWithSeparators("!!RFTS/x Import mit Fehlern abgeschlossen", MessageLogOption.TIME_STAMP);
                Java1_1_Utils.sleep(1000); // 1 Sekunde warten, damit das RFTS/x Log auf jeden Fall nach den Import-Logs beendet wird
                DWFile errorLogFile = iPartsJobsManager.getInstance().jobError(getLogFile());
                setLogFileWithCheck(errorLogFile, false);
            }
        } finally {
            getMessageLog().removeMessageEventListener(this);
            rftsxIsRunning = false;
            isRftsxImportCanceled = false;
            cancelMsg = "";
            updateButtonActiveState();
        }
    }

    private void updateButtonActiveState() {
        ApplicationEvents.fireEventInAllProjects(new RFTSxRunningChangedEvent(), true, true, null);
    }

    public void addLogError(String message) {
        errorCount++;
        super.addLogError(message);
    }

    /**
     * Erstellt mit den Verzeichnis-Monitor und speichert die zurückgegebene Monitor ID.
     */
    public void startRFTSxDirectoryMonitor() {
        if (StrUtils.isEmpty(monitorID) && iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_AUTO_IMPORTS_ENABLED)) {
            RFTSXImportHelper rftsxInstance = RFTSXImportHelper.getInstance();
            if (rftsxInstance == null) {
                monitorID = null;
                return;
            }
            startTimer();
            monitorID = RFTSXHelper.getInstance().startMonitoring(iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_RFTSX_INPUT_DIR),
                                                                  iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_RFTSX_WORK_DIR),
                                                                  iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_RFTSX_ARCHIVE_DIR),
                                                                  rftsxInstance.getFileMonitorHandler(),
                                                                  DirectoryEntryType.CREATE);
            // Valide Importtypen setzen
            validImporterTypes = iPartsImportPlugin.getValidRFTSXImportTypes();
        }
    }

    private void startTimer() {
        iPartsTimeInterval timeInterval = iPartsImportPlugin.getTimeIntervalForRFTSX();
        if (isImportTimeIntervalValid(timeInterval)) {
            Date startAsDate = timeInterval.getStartTimeAsDate();
            Date endAsDate = timeInterval.getEndTimeAsDate();
            EnumSet<DateUtils.DayOfWeek> everyDayOfWeek = DateUtils.DayOfWeek.getDaysOfWeek(DateUtils.DayOfWeek.getDisplayNames());
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.INFO, MonitorTypes.RFTSX + " - Starting timer for activating RFTS/x monitor. Daily activation time: " + DateUtils.toISO_Time(startAsDate));
            if (rftsxDailyStartTimer != null) {
                rftsxDailyStartTimer.reinitTimer(everyDayOfWeek, startAsDate);
            } else {
                rftsxDailyStartTimer = DayOfTheWeekTimer.createTimer(iPartsImportPlugin.getRFTSxSession(),
                                                                     everyDayOfWeek,
                                                                     startAsDate,
                                                                     iPartsImportPlugin.LOG_CHANNEL_RFTSX, "RFTS/x Interval",
                                                                     new FrameworkRunnable() {
                                                                         @Override
                                                                         public void run(FrameworkThread thread) {
                                                                             if (!rftsxImportEnabled) {
                                                                                 ApplicationEvents.fireEventInAllProjectsAndAllClusters(new RFTSxEnabledChangedEvent(true));
                                                                             }
                                                                         }
                                                                     });
            }
            rftsxDailyStartTimer.startTimer();
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.INFO, MonitorTypes.RFTSX + " - Starting timer for deactivating RFTS/x monitor. Daily deactivation time: " + DateUtils.toISO_Time(endAsDate));
            if (rftsxDailyEndTimer != null) {
                rftsxDailyEndTimer.reinitTimer(everyDayOfWeek, endAsDate);
            } else {
                rftsxDailyEndTimer = DayOfTheWeekTimer.createTimer(iPartsImportPlugin.getRFTSxSession(),
                                                                   everyDayOfWeek,
                                                                   endAsDate,
                                                                   iPartsImportPlugin.LOG_CHANNEL_RFTSX, "RFTS/x Interval",
                                                                   new FrameworkRunnable() {
                                                                       @Override
                                                                       public void run(FrameworkThread thread) {
                                                                           if (rftsxImportEnabled) {
                                                                               ApplicationEvents.fireEventInAllProjectsAndAllClusters(new RFTSxEnabledChangedEvent(false));
                                                                           }
                                                                       }
                                                                   });
            }
            rftsxDailyEndTimer.startTimer();
        }
    }

    private boolean isImportTimeIntervalValid(iPartsTimeInterval timeInterval) {
        return (timeInterval != null) && StrUtils.isValid(timeInterval.getEndTime(), timeInterval.getEndTime()) && (timeInterval.getIntervalDuration() >= 0);
    }

    /**
     * Stoppt den Verzeichnis-Monitor.
     */
    public void stopRFTSxDirectoryMonitor() {
        if (StrUtils.isValid(monitorID)) {
            stopTimer();
            RFTSXHelper.getInstance().stopMonitoring(MonitorTypes.RFTSX, monitorID);
            monitorID = null;
        }
        validImporterTypes = null;
    }

    private void stopTimer() {
        if (rftsxDailyStartTimer != null) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.INFO, MonitorTypes.RFTSX + " - Stopping timer for activating RFTS/x monitor. Daily activation time: " + DateUtils.toISO_Time(rftsxDailyStartTimer.getTimeOfDay()));
            rftsxDailyStartTimer.stopTimer();
        }
        if (rftsxDailyEndTimer != null) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.INFO, MonitorTypes.RFTSX + " - Stopping timer for deactivating RFTS/x monitor. Daily deactivation time: " + DateUtils.toISO_Time(rftsxDailyEndTimer.getTimeOfDay()));
            rftsxDailyEndTimer.stopTimer();
        }
    }

    /**
     * Überprüft, ob alle Einstellungen für den Verzeichnis-Monitor vorhanden sind.
     *
     * @return
     */
    public static boolean isRFTSxDirectoryMonitorConfigured() {
        // RFTSXImportHelper muss verfügbar, die Verzeichnisse konfiguriert und entweder eine monitorID vorhanden oder
        // das Monitoring für diesen Cluster-Knoten deaktiviert sein (in diesem Fall gibt es ja keine monitorID)
        return (RFTSXImportHelper.getInstance() != null)
               && StrUtils.isValid(iPartsImportPlugin.getPluginConfig().getConfigValueAsFile(iPartsImportPlugin.CONFIG_RFTSX_INPUT_DIR).getName(),
                                   iPartsImportPlugin.getPluginConfig().getConfigValueAsFile(iPartsImportPlugin.CONFIG_RFTSX_WORK_DIR).getName(),
                                   iPartsImportPlugin.getPluginConfig().getConfigValueAsFile(iPartsImportPlugin.CONFIG_RFTSX_ARCHIVE_DIR).getName())
               && (StrUtils.isValid(RFTSXImportHelper.getInstance().getMonitorID()) || !iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_AUTO_IMPORTS_ENABLED));
    }

    public void haltMonitor() {
        if (!StrUtils.isEmpty(monitorID)) {
            RFTSXHelper.getInstance().stopMonitoring(MonitorTypes.RFTSX, monitorID);
        }
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void configurationChanged() {
        if (StrUtils.isValid(monitorID)) {
            // Den Monitor nur neu erzeugen, wenn die Konfigurationen sich geändert haben
            DWFile shareFromConfig = iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_RFTSX_INPUT_DIR);
            AbstractDirectoryMonitor monitor = RFTSXHelper.getInstance().getMonitor(monitorID);
            if (monitor != null) {
                DirectoryAgent agent = monitor.getDirectoryAgent(shareFromConfig.getAbsolutePath());
                if ((agent == null) || !Utils.objectEquals(agent.getDirectory(), shareFromConfig)
                    || !Utils.objectEquals(agent.getWorkDir(), iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_RFTSX_WORK_DIR))
                    || !Utils.objectEquals(agent.getArchive(), iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_RFTSX_ARCHIVE_DIR))
                    || validTypesChanged()
                    || importTimesChanged()) {
                    stopRFTSxDirectoryMonitor();
                }
            }
        }
    }

    private boolean importTimesChanged() {
        if ((rftsxDailyStartTimer == null) || (rftsxDailyEndTimer == null)) {
            return false;
        }
        iPartsTimeInterval timeInterval = iPartsImportPlugin.getTimeIntervalForRFTSX();
        if (!isImportTimeIntervalValid(timeInterval)) {
            return true;
        }
        String currentStartTime = DateUtils.toISO_Time(rftsxDailyStartTimer.getTimeOfDay());
        String currentEndTime = DateUtils.toISO_Time(rftsxDailyEndTimer.getTimeOfDay());
        String newStartTime = DateUtils.toISO_Time(timeInterval.getStartTimeAsDate());
        String newEndTime = DateUtils.toISO_Time(timeInterval.getEndTimeAsDate());

        return !currentStartTime.equals(newStartTime) || !currentEndTime.equals(newEndTime);
    }

    /**
     * Check, ob sich die Importerarten geändert haben
     *
     * @return
     */
    private boolean validTypesChanged() {
        Set<String> validTypesFromConfig = iPartsImportPlugin.getValidRFTSXImportTypes();
        if (validImporterTypes == null) {
            return true;
        }
        if (validImporterTypes.size() != validTypesFromConfig.size()) {
            return true;
        }
        validTypesFromConfig.removeAll(validImporterTypes);
        return !validTypesFromConfig.isEmpty();
    }

    public String restartMonitor() {
        stopRFTSxDirectoryMonitor();
        if (RFTSXHelper.getInstance().checkPluginProjects()) {
            startRFTSxDirectoryMonitor();
            return TranslationHandler.translate("!!RFTS/x Monitor wurde neu gestartet.") + "\n\n"
                   + RFTSXHelper.getInstance().getMonitorInformation(MonitorTypes.RFTSX, monitorID,
                                                                     iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_RFTSX_INPUT_DIR).getAbsolutePath(),
                                                                     getProject().getViewerLanguage(),
                                                                     true);
        }
        return "";
    }

    public String getMonitorID() {
        return monitorID;
    }

    public FileMonitorHandler getFileMonitorHandler() {
        if (fileProcessor == null) {
            fileProcessor = new FileMonitorHandler() {
                @Override
                public boolean isImportEnabled() {
                    return isRFTSxImportEnabled();
                }

                @Override
                public String processFile(DWFile workFile, String path) {
                    return handleRFTSxInputFile(workFile, path);
                }

                @Override
                public DWFile getMessageLogFile() {
                    return getLogFile();
                }

                @Override
                public FilenameFilter getFileExtensionFilter() {
                    return getFileExtensionFilterForMonitorHandler();

                }

                @Override
                public AbstractDirectoryMonitor getDirectoryMonitor() {
                    AbstractDirectoryMonitor monitor;
                    if (iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_RFTSX_ENFORCE_POLLING)) {
                        int interval = iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(iPartsImportPlugin.CONFIG_RFTSX_POLLING_TIME);
                        interval = interval * 1000 * 60;
                        monitor = DirectoryMonitorFactory.getDirectoryMonitorWithPolling(0, interval, false);
                    } else {
                        monitor = DirectoryMonitorFactory.getDefaultFactory();
                    }
                    return monitor;
                }

                @Override
                public String getFileNameDelimiter() {
                    return RFTSX_DELIMITER;
                }

                @Override
                public MonitorTypes getMonitorType() {
                    return MonitorTypes.RFTSX;
                }

                @Override
                public boolean isArchiveFilesEnabled() {
                    return iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_RFTSX_COPY_TO_ARCHIVE);
                }

                @Override
                public List<DWFile> sortFiles(List<DWFile> files) {
                    // Hole erst alle todo Dateien, wobei sie im todo Ordner bleiben. Der RFTSXHelper kopiert sie dann
                    // automatisch ins Arbeitsverzeichnis
                    List<DWFile> result = new ArrayList<>(getToDoFilesWithCheck(getFileExtensionFilter()));
                    // Set für alle MBS Dateien. Bei MBS sollen erst die Stammdaten und dann die Strukturdaten importiert werden
                    Set<DWFile> mbsFiles = new TreeSet<>((o1, o2) -> {
                        String o1Filename = o1.extractFileName(false);
                        String o2Filename = o2.extractFileName(false);
                        // Check, ob es sich um Stammdaten handelt
                        boolean o1IsMasterData = o1Filename.startsWith(ImporterTypes.MBS_MASTER.getFileNamePrefix());
                        boolean o2IsMasterData = o2Filename.startsWith(ImporterTypes.MBS_MASTER.getFileNamePrefix());
                        // Wenn eine davon Stammdaten enthält, wird sie bevorzugt
                        if (o1IsMasterData && !o2IsMasterData) {
                            return -1;
                        } else if (!o1IsMasterData && o2IsMasterData) {
                            return 1;
                        }
                        // Wenn beide Dateien Stammdaten bzw keine Stammdaten sind, dann wird nach dem Namen bzw dem Datum im Namen sortiert
                        return o1Filename.compareTo(o2Filename);
                    });
                    List<DWFile> otherImportFiles = new ArrayList<>();
                    // Alle Dateien durchlaufen und sortieren (bis auf die im todo Ordner)
                    for (DWFile file : files) {
                        // Kenner, ob es sich um MBS Dateien handelt
                        if (file.extractFileName(false).startsWith("IPARTS_MBS_")) {
                            mbsFiles.add(file);
                        } else {
                            otherImportFiles.add(file);
                        }
                    }

                    // Erst die normalen Dateien hinzufügen und danach die sortierten MBS Dateien (die aus dem todo Ordner
                    // sind ja schon sortiert enthalten)
                    result.addAll(otherImportFiles);
                    result.addAll(mbsFiles);
                    return result;
                }

                @Override
                public void initAdditionalData(AbstractDirectoryMonitor monitor) {
                    // Wenn der RFTSx Mechanismus komplett neugestartet wird, prüft der Monitor initial, ob Dateien im
                    // Verzeichnis liegen. Dateien, die in den todo Ordner verschoben wurden findet er somit nicht.
                    // Die Idee ist, dass wir vor dem Start des Monitors die Dateien in das share Verzeichnis legen. So
                    // So findet er sie sofort beim Start. Die Kennung bzw der Dateiname muss nicht angepasst werden,
                    // da der RFTSxHelper das automatisch macht.
                    if (isRFTSxImportEnabled()) {
                        // Dateien sollen nicht kopiert werden, wenn der RFTSx Mechanismus aus ist (unabhängig vom Interval)
                        boolean isRFTSxImportEnabledConfig = iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_RFTSX_HANDLE_IMPORT);
                        iPartsTimeInterval timeInterval = iPartsImportPlugin.getTimeIntervalForRFTSX();
                        if (!isRFTSxImportEnabledConfig) {
                            return;
                        }
                        // Bestimme das share-Verzeichnis über den Monitor bzw. den DirectoryAgent
                        if (monitor != null) {
                            DirectoryAgent agent = monitor.getDirectoryAgent(iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_RFTSX_INPUT_DIR).getAbsolutePath());
                            if (agent != null) {
                                // Alle todo Dateien holen und in das share Verzeichnis legen
                                List<DWFile> todoFiles = getToDoFilesWithCheck(getFileExtensionFilter());
                                DWFile shareDirectory = agent.getDirectory();
                                try {
                                    for (DWFile file : todoFiles) {
                                        DWFile dextFile = shareDirectory.getChild(file.extractFileName(true));
                                        Files.move(file.toPath(), dextFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    }
                                } catch (IOException ioe) {
                                    Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_RFTSX, LogType.ERROR, ioe);
                                }
                            }
                        }
                    }
                }
            };
        }
        return fileProcessor;
    }

    /**
     * Liefert den {@link FilenameFilter} für den Monitor, damit nur bestimmte Dateien berücksichtigt werden
     *
     * @return
     */
    private FilenameFilter getFileExtensionFilterForMonitorHandler() {
        return (dir, name) -> {
            // Wenn der Schalter "Dateien ohne Endung sind auch zulässig" gesetzt ist ...
            if (iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_RFTSX_ACCEPT_FILES_WITHOUT_EXTENSION)) {
                // ... und die Datei tatsächlich keine Dateiendung hat.
                if (DWFile.removeExtension(name).length() == name.length()) {
                    return true;
                }
            }
            // Ansonsten noch die als zulässig konfigurierten Dateiendungen überprüfen.
            String[] allowedExtensions = StrUtils.toStringArray(iPartsImportPlugin.getPluginConfig().getConfigValueAsString(iPartsImportPlugin.CONFIG_RFTSX_FILE_EXTENSIONS), ";");
            for (String ext : allowedExtensions) {
                if (name.toLowerCase().endsWith("." + ext.trim())) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Liefert alle Dateien, die in den todo Ordner verschoben wurden, sofern RFTSx Importe aktiv sind
     *
     * @param fileExtensionFilter
     * @return
     */
    private List<DWFile> getToDoFilesWithCheck(FilenameFilter fileExtensionFilter) {
        List<DWFile> result = new ArrayList<>();
        if (isRFTSxImportEnabled()) {
            result.addAll(getToDoFiles(fileExtensionFilter));
        }
        return result;
    }

    /**
     * Liefert alle Dateien, die in den todo Ordner verschoben wurden
     *
     * @param fileExtensionFilter
     * @return
     */
    private List<DWFile> getToDoFiles(FilenameFilter fileExtensionFilter) {
        List<DWFile> result = new ArrayList<>();
        // Das eingestellte share Verzeichnis
        DWFile shareFromConfig = iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_RFTSX_INPUT_DIR);
        // relativ dazu das todo-Verzeichnis
        DWFile todoDirectory = shareFromConfig.getChild(iPartsConst.SUBDIR_TO_DO);
        // Wenn das Verzeichnis existiert, die Dateien bestimmen, die dorthin verschoben wurden, weil RFTSx abgeschaltet wurde
        if (todoDirectory.exists(5000) && todoDirectory.isDirectory()) {
            // Gültige Endung und der todo-Kenner
            FilenameFilter filenameFilter = (dir, name) -> name.contains(RFTSXHelper.TO_DO_DELIMITER) && fileExtensionFilter.accept(dir, name);
            result.addAll(todoDirectory.listDWFiles(filenameFilter));
        }
        result.sort(Comparator.comparing(o -> o.extractFileName(false)));
        return result;
    }

    /**
     * Gibt zurück, ob todo-Dateien existieren
     *
     * @return
     */
    public boolean hasToDoFiles() {
        return !getToDoFiles(getFileExtensionFilterForMonitorHandler()).isEmpty();
    }


    /**
     * Startet den RFTSx Monitor mit zusätzlichem Interval-Check. Befinden wir uns nich tim Intervall, wird ein Event
     * gefeuert, der RFTSx ausschaltet
     */
    public void startRFTSxDirectoryMonitorWithIntervalCheck() {
        iPartsTimeInterval timeInterval = iPartsImportPlugin.getTimeIntervalForRFTSX();
        boolean importEnabled = iPartsDateTimeHelper.isWithinInterval(timeInterval.getStartTimeInSeconds(), timeInterval.getEndTimeInSeconds());
        this.rftsxImportEnabled = importEnabled;
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new RFTSxEnabledChangedEvent(importEnabled));

        // Ist eigentlich nicht nötig, weil der Monitor nach feuern der Events im Plugin erzeugt wird. Zur Sicherheit
        // wird das hier aber auch gemacht
        startRFTSxDirectoryMonitor();
    }
}
