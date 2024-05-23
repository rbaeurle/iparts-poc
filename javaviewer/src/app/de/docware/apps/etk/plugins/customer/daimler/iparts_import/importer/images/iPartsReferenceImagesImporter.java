/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.images;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.drawing.*;
import de.docware.apps.etk.base.project.mechanic.ids.PoolEntryId;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReference;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReferenceList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPicReferenceId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQPicScheduler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.ImageFileImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.os.OsUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer für das manuelle Importieren von Referenzzeichnungen (DASTi Zeichnungen)
 */
public class iPartsReferenceImagesImporter extends ImageFileImporter {

    private static final String STORAGE_NAME = "DASTi_Import";

    private DWFile storageLocation;
    private EtkMessageLog currentMessageLog;
    private boolean doBufferedSave = true;

    /**
     * Konstruktor für den Fall, wenn man den Bildreferenzenimporter für das Suchen von Zeichnungen während eines Stücklisten-Imports
     * oder zum Nachfordern von Zeichnungen verwendet.
     *
     * @param project
     * @param messageLog
     */
    public iPartsReferenceImagesImporter(EtkProject project, EtkMessageLog messageLog) {
        super(project);
        initImageReferencesImport();
        currentMessageLog = messageLog;
    }

    /**
     * Konstruktor für den Fall, wenn man den Bildreferenzenimporter als eigenständigen Importer starten möchte (z.B. via Menü-Punkt).
     *
     * @param project
     */
    public iPartsReferenceImagesImporter(EtkProject project) {
        super(project);
        initForSingleImport();
    }

    private void initImageReferencesImport() {
        importName = "!!DASTi Referenzzeichnungen";
        setBufferedSave(doBufferedSave);
        storageLocation = iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_IMPORT_FILE_DIR).
                getChild(DWFile.convertToValidFileName(STORAGE_NAME));
        storageLocation.mkDirsWithRepeat();
    }

    /**
     * Initialisiert den Importer für den manuellen Import von Referenzzeichnungen
     */
    private void initForSingleImport() {
        initImageReferencesImport();
        setLogFile(iPartsJobsManager.getInstance().jobRunning(STORAGE_NAME), true);
    }

    public DWFile getImportFileDir() {
        return iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_IMPORT_IMAGES_SERVER_DIR);
    }

    /**
     * Startet das Importieren der Referenzzeichnungen aus dem voreingestellten Verzeichnis. Über <i>withMessageDialog</i>
     * kann entschieden werden, ob der Import im Hintergrund laufen oder in einem Logfenster angezeigt werden soll.
     *
     * @param withMessageLogDialog
     */
    public void startImageReferencesImport(boolean withMessageLogDialog) {
        FrameworkRunnable runnable = new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                initImport(getMessageLog()); // Import initiieren um BufferedSave vorzubereiten und MessageLog anlegen
                DWFile allFileDir = getImportFileDir();
                List<DWFile> foldersForImport = new DwList<>();
                // Unterverzeichnis für den aktuellen Import. Name des Unterordners enthält das aktuelle Datum
                storageLocation = storageLocation.getChild("Import_" + DateUtils.getCurrentDateFormatted(iPartsJobsManager.JOB_FILE_DATE_TIME_FORMAT));
                if (!allFileDir.isEmpty()) {
                    String tempSubfolderName = "";
                    foldersForImport.addAll(importImagesReferences(allFileDir, tempSubfolderName));
                    // Alle Unterordner pro Ebene durchlaufen und Zeichnungen, die darin enthalten sind abarbeiten
                    for (int i = 0; i < foldersForImport.size(); i++) {
                        if (isCancelled()) {
                            break;
                        }
                        // Unterordner im Ausgangsverzeichnis werden auch im Zielverzeichnis angelegt. So bleibt die initiale
                        // Struktur erhalten
                        DWFile subDirectory = foldersForImport.get(i);
                        try {
                            tempSubfolderName = OsUtils.FILESEPARATOR + subDirectory.getRelativePath(allFileDir);
                            foldersForImport.addAll(importImagesReferences(subDirectory, tempSubfolderName));
                        } catch (IOException e) {
                            getMessageLog().fireMessage(translateForLog("!!Der relative Pfad aus \"%1\" zum Hauptverzeichnis \"%2\" " +
                                                                        "konnte nicht extrahiert werden. Verzeichnis wird übersprungen.",
                                                                        subDirectory.getAbsolutePath(), allFileDir.getAbsolutePath()),
                                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        }
                    }
                } else {
                    getMessageLog().fireMessage(translateForLog("!!Das voreingestellte Verzeichnis \"%1\" ist leer",
                                                                allFileDir.getName()),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }
                postImportTask();
            }
        };
        if (withMessageLogDialog) {
            EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!DASTi Referenzzeichnungen", "!!Importiere Referenzzeichnungen " +
                                                                                                    "aus dem eingestellten Verzeichnis.", null) {
                @Override
                protected void cancel(Event event) {
                    cancelImport();
                    super.cancel(event);
                }
            };
            currentMessageLog = messageLogForm.getMessageLog();
            messageLogForm.showModal(runnable);
        } else {
            Session.startChildThreadInSession(runnable);
        }
    }

    @Override
    protected void postImportTask() {
        try {
            super.postImportTask();
        } finally {
            finishImport();
            moveJobsFile(); // Verschiebe die JobsLog Datei
            if (!isCancelled()) {
                DWFile allFileDir = iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_IMPORT_IMAGES_SERVER_DIR);
                cleanDirectory(allFileDir.listDWFiles()); // Lösche alle leeren Ordner
            }
        }
    }

    /**
     * Löscht leere (Unter-)Verzeichnisse
     *
     * @param folders
     */
    private void cleanDirectory(List<DWFile> folders) {
        for (DWFile folder : folders) {
            if (folder.isDirectory()) {
                cleanDirectory(folder.listDWFiles());
                if (folder.isEmpty()) {
                    folder.deleteRecursively();
                }
            }
        }
    }

    /**
     * Verschiebt die Jobs-Log-Datei in das zugehörige Verzeichnis
     */
    private void moveJobsFile() {
        if (getErrorCount() > 0) {
            iPartsJobsManager.getInstance().jobError(getLogFile());
        } else if (isCancelled()) {
            iPartsJobsManager.getInstance().jobCancelled(getLogFile(), false);
        } else {
            iPartsJobsManager.getInstance().jobProcessed(getLogFile());
        }
    }

    @Override
    public EtkMessageLog getMessageLog() {
        if (currentMessageLog != null) {
            return currentMessageLog;
        } else {
            return super.getMessageLog();
        }
    }

    /**
     * Importiert die im übergebenen Verzeichnis vorhandenen Zeichnungen samt Hotspot Dateien und Vorschaubilder.
     *
     * @param fileDir
     * @param subFolderName
     * @return
     */
    protected List<DWFile> importImagesReferences(DWFile fileDir, String subFolderName) {
        List<DWFile> folders = new DwList<DWFile>();
        if (fileDir.isDirectory() && !fileDir.isEmpty()) {
            List<DWFile> allFiles = fileDir.listDWFiles();
            Map<String, ImportFileWithDate> images = new LinkedHashMap<String, ImportFileWithDate>();
            Map<String, ImportFileWithDate> hotspots = new LinkedHashMap<String, ImportFileWithDate>();
            Map<String, ImportFileWithDate> imagePreviews = new LinkedHashMap<String, ImportFileWithDate>();
            // Verzeichnisse auslesen und Maps mit Dateien füllen
            fillFileMaps(allFiles, folders, images, hotspots, imagePreviews);

            getMessageLog().fireMessage(translateForLog("!!Im Verzeichnis \"%1\" wurden %2 Zeichnungen, %3 Hotspot-Dateien, " +
                                                        "%4 Vorschaubilder und %5 Unterverzeichnise gefunden.",
                                                        fileDir.getPath(), String.valueOf(images.size()),
                                                        String.valueOf(hotspots.size()), String.valueOf(imagePreviews.size()),
                                                        String.valueOf(folders.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            getMessageLog().fireMessage(translateForLog("!!Starte Import der %1 Referenzzeichnungen im Verzeichnis \"%2\"...",
                                                        String.valueOf(images.size()), fileDir.getPath()), MessageLogType.tmlMessage, MessageLogOption.SINGLE_MESSAGE, MessageLogOption.TIME_STAMP);
            // Importiert und verschiebt die Zeichnungen
            handleImportFiles(images, hotspots, imagePreviews, subFolderName);

            getMessageLog().fireMessage(translateForLog("!!%1 von %2 Zeichnungen erfolgreich importiert",
                                                        String.valueOf(Math.max(0, images.size() - skippedRecords)), String.valueOf(images.size())),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            // Es wurden Hotspot-Dateien bzw. Vorschaubilder gefunden, die zu keiner Zeichnung passen
            if (!hotspots.isEmpty() || !imagePreviews.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!%1 Hotspot-Dateien und %2 Vorschaubilder konnten keiner Zeichnung zugeordnet werden",
                                                            String.valueOf(hotspots.size()), String.valueOf(imagePreviews.size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                String unknownSubfolder = subFolderName + OsUtils.FILESEPARATOR + iPartsConst.SUBDIR_UNKNOWN;
                if (!hotspots.isEmpty()) {
                    logMissingImages(hotspots.values(), "Zur Hotspot-Datei", unknownSubfolder);
                }
                if (!imagePreviews.isEmpty()) {
                    logMissingImages(imagePreviews.values(), "Zum Vorschaubild", unknownSubfolder);
                }
            }
        }
        return folders;
    }

    /**
     * Füllt die übergebenen Maps mit den jeweiligen Zeichnungs-, Hotspot- und Vorschaubilder-Dateien
     *
     * @param allFiles
     * @param folders
     * @param images
     * @param hotspots
     * @param imagePreviews
     */
    private void fillFileMaps(List<DWFile> allFiles, List<DWFile> folders, Map<String, ImportFileWithDate> images,
                              Map<String, ImportFileWithDate> hotspots, Map<String, ImportFileWithDate> imagePreviews) {
        for (DWFile file : allFiles) {
            if (isCancelled()) {
                break;
            }
            // Unterordner werden für später aufgesammelt
            if (file.isDirectory()) {
                folders.add(file);
                continue;
            }
            String fileExtension = file.getExtension();
            if (iPartsTransferConst.MediaFileTypes.isHotspotFile(fileExtension) || iPartsTransferConst.MediaFileTypes.isValidPreviewExtension(fileExtension)) { // Ist es eine erlaubte Dateiendung?
                String[] fileNameToken = StrUtils.toStringArray(file.extractFileName(false), "_");
                // Nach dem splitten, können maximal 3 und minimal 1 Token existieren -> Sonst Fehler
                if ((fileNameToken.length > 3) || (fileNameToken.length < 1)) {
                    getMessageLog().fireMessage(translateForLog("!!Dateiname entspricht nicht der vorgegebenen Struktur:" +
                                                                " \"%1\". Datei wird überprungen", file.extractFileName(true)),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    storeProcessedFile(file, iPartsConst.SUBDIR_UNKNOWN);

                } else {
                    String date = "";
                    boolean isPreview = false;
                    if (fileNameToken.length > 1) {
                        // Sonderfall: Der Dateiname hat kein Datum, z.B.: B21230000091_thumbnail.png oder B21230000091.sen
                        if (fileNameToken[1].equalsIgnoreCase(iPartsTransferConst.SUFFIX_THUMBNAIL_IMAGE_IMPORT)) {
                            isPreview = true;
                        } else {
                            date = StrUtils.replaceSubstring(fileNameToken[1], ".", "");
                            if (!DateUtils.isValidDate_yyyyMMdd(date)) {
                                getMessageLog().fireMessage(translateForLog("!!Datumsangabe im Dateinamen \"%1\" ist " +
                                                                            "entweder nicht gültig oder an falscher Stelle.",
                                                                            file.extractFileName(true)),
                                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                                storeProcessedFile(file, iPartsConst.SUBDIR_UNKNOWN);
                            }
                            isPreview = (fileNameToken.length == 3) && fileNameToken[2].equalsIgnoreCase(iPartsTransferConst.SUFFIX_THUMBNAIL_IMAGE_IMPORT);
                        }
                    }
                    // Log-Meldung, da Sonderfall
                    if (StrUtils.isEmpty(date)) {
                        getMessageLog().fireMessage(translateForLog("!!Dateiname \"%1\" enthält kein Referenzdatum. Datei wird ohne Datum importiert", file.extractFileName(true)),
                                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    }

                    ImportFileWithDate importFileWithDate = new ImportFileWithDate(fileNameToken[0].toUpperCase(), date, file);
                    if (isPreview) {
                        imagePreviews.put(importFileWithDate.getImageNumber(), importFileWithDate);
                    } else {
                        if (iPartsTransferConst.MediaFileTypes.isHotspotFile(fileExtension)) {
                            hotspots.put(importFileWithDate.getImageNumber(), importFileWithDate);
                        } else {
                            images.put(importFileWithDate.getImageNumber(), importFileWithDate);
                        }
                    }
                }
            } else {
                getMessageLog().fireMessage(translateForLog("!!Datei \"%1\" enthält kein gültige Dateiendung. Datei wird übersprungen", file.extractFileName(true)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                storeProcessedFile(file, iPartsConst.SUBDIR_UNKNOWN);
            }
        }
    }

    /**
     * Verarbeitet die zusammengesuchten Dateien eines Verzeichnisses. Die Zeichnungen, Hotspot-Dateien und Vorschaubilder
     * werden importiert und danach in das voreingestellte Verzeichnis verschoben.
     *
     * @param images
     * @param hotspots
     * @param imagePreviews
     * @param subFolderName
     */
    private void handleImportFiles(Map<String, ImportFileWithDate> images, Map<String, ImportFileWithDate> hotspots,
                                   Map<String, ImportFileWithDate> imagePreviews, String subFolderName) {
        skippedRecords = 0;
        int imageCount = 0;
        int mapSize = images.size();
        for (String imageNumber : images.keySet()) {
            if (Thread.currentThread().isInterrupted()) {
                cancelImport("!!Thread wurde frühzeitig beendet");
                break;
            }
            if (isCancelled()) {
                break;
            }
            getMessageLog().fireMessage(translateForLog("!!Importiere Zeichnung mit der Zeichnungsnummer %1",
                                                        imageNumber),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            ImportFileWithDate picFile = images.get(imageNumber);
            // Check, ob Hotspot-Datei vorhanden und valide ist
            ImportFileWithDate senFile = hotspots.remove(imageNumber);
            boolean senValid = checkAdditionalFile(senFile, imageNumber, "keine Hotspot-Datei");
            // Check, ob Vorschaubild vorhanden und valide ist
            ImportFileWithDate previewFile = imagePreviews.remove(imageNumber);
            boolean previewValid = checkAdditionalFile(previewFile, imageNumber, "kein Vorschaubild");

            boolean importSuccessful = false;
            // Nur importieren, wenn es ein Bild gibt (egal, ob eine Hotspot-Datei oder ein Vorschaubild vorhanden ist)
            if (picFile != null) {
                imageCount++;
                importSuccessful = importImageFromFile(picFile.getImportFile(), (senValid ? senFile.getImportFile() : null),
                                                       (previewValid ? previewFile.getImportFile() : null), imageNumber, "",
                                                       "", ImageVariant.iv2D, false, true, false).importSuccessful();
                adjustPicReferencesInDB(picFile);
            }
            String subDir;
            if (importSuccessful) {
                subDir = iPartsConst.SUBDIR_PROCESSED;
            } else {
                subDir = iPartsConst.SUBDIR_ERROR;
                reduceRecordCount();
            }
            if (StrUtils.isValid(subFolderName)) {
                subDir = subDir + OsUtils.FILESEPARATOR + subFolderName;
            }
            storeProcessedFiles(subDir, picFile, senFile, previewFile);
            if (getMessageLog().progressNeedsUpdate() || (imageCount == mapSize)) {
                getMessageLog().fireProgress(imageCount, mapSize, "", true, false);
            }
        }
        getMessageLog().hideProgress();
    }

    /**
     * Aktualisiert die Referenzen in der Datenbank. Sollte in der Referenz schon die Varianten ID stehen, wird diese durch
     * die aktuelle DASTi Id ausgetauscht.
     *
     * @param picFile
     */
    private void adjustPicReferencesInDB(ImportFileWithDate picFile) {
        // Hole alle Zeichnungsreferenzen
        iPartsDataPicReferenceList picReferencesList = iPartsDataPicReferenceList.loadPicReferencesWithoutDate(getProject(), new iPartsPicReferenceId(picFile.getImageNumber(), ""));
        if (!picReferencesList.isEmpty()) {
            List<iPartsDataPicReference> adjustedPicRefList;
            // Altdatenbedingt kann es sein, dass in der DB noch gleiche Referenzen mit unterschiedlichen Datumsangaben existieren.
            // Diese werden vor dem verarbeiten zusammengelegt.
            if (picReferencesList.size() > 1) {
                getMessageLog().fireMessage(translateForLog("!!Für die Zeichnungsreferenz \"%1\" wurden %2 Datensätze " +
                                                            "in der Datenbank gefunden. Gefundene Datensätze werden zu " +
                                                            "einem zusammengelegt.",
                                                            picFile.getImageNumber(), String.valueOf(picReferencesList.size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                adjustedPicRefList = MQPicScheduler.getInstance().adjustPicReferenceDatasets(picReferencesList, getProject());
            } else {
                adjustedPicRefList = picReferencesList.getAsList();
            }
            if (adjustedPicRefList.isEmpty() || (adjustedPicRefList.size() > 1)) {
                getMessageLog().fireMessage(translateForLog("!!Fehler beim Zusammenlegen der Zeichnungsreferenzen aus " +
                                                            "der Datenbank für die Zeichnungsnummer \"%1\". Anzahl Referenzen " +
                                                            "in der DB: %2; Nach dem Zusammenlegen: %3;",
                                                            picFile.getImageNumber(), String.valueOf(picReferencesList.size()),
                                                            String.valueOf(adjustedPicRefList.size())), MessageLogType.tmlMessage,
                                            MessageLogOption.TIME_STAMP);
                return;
            }
            iPartsDataPicReference dataPicReference = adjustedPicRefList.get(0);
            // Ist eine Varianten ID vorhanden, dann wurde diese für die eigentliche Referenz in IMAGES genutzt -> Referenz anpassen
            if (dataPicReference.hasVariantIds()) {
                // Hol die eigentlichen Referenzen mit der Varianten ID
                EtkDataImageList imageList = EtkDataObjectFactory.createDataImageList();
                imageList.loadImagesForImageNumber(getProject(), dataPicReference.getVarId());
                if (!imageList.isEmpty()) {
                    StringBuilder builder = new StringBuilder();
                    for (EtkDataImage image : imageList) {
                        if (StrUtils.isValid(builder.toString())) {
                            builder.append(", ");
                        }
                        builder.append(image.getTiffName());
                        // Setze die richtige Bildnummer (von Varianten ID zu DASTi Nummer)
                        image.setFieldValue(EtkDbConst.FIELD_I_IMAGES, picFile.getImageNumber(), DBActionOrigin.FROM_EDIT);
                        image.setFieldValue(EtkDbConst.FIELD_I_PVER, "", DBActionOrigin.FROM_EDIT);
                        image.setFieldValue(iPartsConst.FIELD_I_IMAGEDATE, picFile.getDate(), DBActionOrigin.FROM_EDIT);
                        saveToDB(image);
                    }
                    getMessageLog().fireMessage(translateForLog("!!Für folgende Module wird die Zeichnungsreferenz von %1 zu %2 aktualisiert: %3",
                                                                dataPicReference.getVarId(), picFile.getImageNumber(),
                                                                builder.toString()), MessageLogType.tmlMessage,
                                                MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
                deleteImageFromPool(dataPicReference);
            }
            // Gefundene Referenz zurücksetzen (GUID wird behalten)
            resetPicReference(picFile, dataPicReference);
            saveToDB(dataPicReference);
            getMessageLog().fireMessage(translateForLog("!!Die Zeichnungsreferenz mit der DASTi Nummer \"%1\" wurde zurückgesetzt.",
                                                        picFile.getImageNumber()), MessageLogType.tmlMessage,
                                        MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);

        }

    }

    /**
     * Löscht Zeichnungen aus dem Pool, die als Schlüsselfelder die Varianten ID besitzen. Diese werden im späteren Workflow
     * durch die Original-DASTi-Bildnummern ersetzt.
     *
     * @param dataPicReference
     */
    private void deleteImageFromPool(iPartsDataPicReference dataPicReference) {
        EtkDataPoolEntry poolEntry = EtkDataObjectFactory.createDataPoolEntry();
        poolEntry.init(getProject());
        if (poolEntry.loadFromDB(new PoolEntryId(dataPicReference.getVarId(), dataPicReference.getVarRevId()))) {
            poolEntry.deleteFromDB();
            EtkDataPool dataPool = EtkDataObjectFactory.createDataPool();
            dataPool.init(getProject());
            PoolId poolId = new PoolId(dataPicReference.getVarId(), dataPicReference.getVarRevId(), "", ImageVariant.iv2D.getUsage());
            if (dataPool.loadFromDB(poolId)) {
                // Hotspots zum Bild löschen und Bild löschen
                EtkDataHotspotList.deleteHotspots(getProject(), poolId.getPImages(), poolId.getPVer(), poolId.getPSprach(), poolId.getPUsage());
                dataPool.deleteFromDB();
                getMessageLog().fireMessage(translateForLog("!!Die Zeichnung mit der Zeichnungsnummer \"%1\" (Version %2) wurde aus der Datenbank gelöscht.",
                                                            dataPicReference.getVarId(), dataPicReference.getVarRevId()), MessageLogType.tmlMessage,
                                            MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }
    }

    /**
     * Setzt die übergebene Referenz zurück und übergibt das neue Datum aus der Zeichnungsreferenz
     *
     * @param picFile
     * @param dataPicReference
     */
    private void resetPicReference(ImportFileWithDate picFile, iPartsDataPicReference dataPicReference) {
        try {
            dataPicReference.resetData();
            if (StrUtils.isValid(picFile.getDate())) {
                dataPicReference.setRefDate(DateUtils.toCalendar_yyyyMMdd(picFile.getDate()));
            } else {
                dataPicReference.setRefDateAsString("");
            }
        } catch (ParseException e) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Could not parse date while resetting pic reference");
            Logger.getLogger().handleRuntimeException(e);
        } catch (DateException e) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Could not parse date while resetting pic reference");
            Logger.getLogger().handleRuntimeException(e);
        }
    }

    /**
     * Überpüft die zur Zeichnung gehörende Datei (hierbei kann es sich um eine Hotspot-Datei oder ein Vorschaubild handeln)
     *
     * @param additionalFile
     * @param imageNumber
     * @param textForFile
     */
    private boolean checkAdditionalFile(ImportFileWithDate additionalFile, String imageNumber, String textForFile) {
        if ((additionalFile == null) || (additionalFile.getImportFile() == null)) {
            getMessageLog().fireMessage(translateForLog("!!Zur Zeichnungsnummer %1 existiert %2",
                                                        imageNumber, textForFile),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return false;
        }
        return true;
    }

    /**
     * Logt und verschiebt die übergebenen, nicht verknüpfbaren Dateien
     *
     * @param files
     * @param textForFile
     * @param subDir
     */
    private void logMissingImages(Collection<ImportFileWithDate> files, String textForFile, String subDir) {
        for (ImportFileWithDate fileObject : files) {
            getMessageLog().fireMessage(translateForLog("!!%1 %2 existiert keine Zeichnung",
                                                        textForFile, fileObject.getFilenameWithExtension()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            storeProcessedFile(fileObject, subDir);
        }
    }

    /**
     * Legt die übergebenen Dateien in einem eingestellten Verzeichnis ab.
     *
     * @param subDir
     * @param filesToStore
     */
    private void storeProcessedFiles(String subDir, ImportFileWithDate... filesToStore) {
        for (ImportFileWithDate fileObject : filesToStore) {
            storeProcessedFile(fileObject, subDir);
        }
    }

    /**
     * Legt die übergebene Dateie in einem eingestellten Verzeichnis ab.
     *
     * @param fileObject
     * @param subDir
     */
    private void storeProcessedFile(ImportFileWithDate fileObject, String subDir) {
        if (fileObject == null) {
            return;
        }
        storeProcessedFile(fileObject.getImportFile(), subDir);
    }

    private void storeProcessedFile(DWFile processedFile, String subDir) {
        if (storageLocation == null) {
            return;
        }
        DWFile currentLocation;
        if (StrUtils.isValid(subDir)) {
            currentLocation = storageLocation.getChild(subDir);
            currentLocation.mkDirsWithRepeat();
        } else {
            currentLocation = storageLocation;
        }
        if (processedFile != null) {
            if (processedFile.move(currentLocation)) {
                getMessageLog().fireMessage(translateForLog("!!Verschiebe die Datei %1 nach %2", processedFile.extractFileName(true),
                                                            currentLocation.getPath()), MessageLogType.tmlMessage,
                                            MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Fehler beim Verschieben der Datei %1 nach %2", processedFile.extractFileName(true),
                                                            currentLocation.getPath()), MessageLogType.tmlMessage,
                                            MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }

        }
    }

    public void enableBufferedSave() {
        setBufferedSave(true);
    }

    public void disableBufferedSave() {
        setBufferedSave(false);
    }

    private class ImportFileWithDate {

        private String date;
        private DWFile importFile;
        private String imageNumber;
        private String filenameWithoutExtension;
        private String filenameWithExtension;

        public ImportFileWithDate(String imageNumber, String date, DWFile importFile) {
            this.imageNumber = imageNumber;
            this.date = date;
            this.importFile = importFile;
            this.filenameWithoutExtension = importFile.extractFileName(false);
            this.filenameWithExtension = importFile.extractFileName(true);
        }

        public String getDate() {
            return date;
        }

        public DWFile getImportFile() {
            return importFile;
        }

        public String getImageNumber() {
            return imageNumber;
        }

        public String getFilenameWithoutExtension() {
            return filenameWithoutExtension;
        }

        public String getFilenameWithExtension() {
            return filenameWithExtension;
        }
    }
}
