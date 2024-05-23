/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.importer;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsCacheType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.db.DBDatabaseEventListener;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.java1_1.Java1_1_Utils;
import de.docware.util.misc.id.IdWithType;

import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Abstrakte Superklasse für einen generischen Importer.
 */
public abstract class AbstractGenericImporter implements GenericImporterInterface, MessageEvent, ProgressEvent {

    private static final EtkMessageLog MESSAGE_LOG_NONE = new EtkMessageLog() { // add* Methoden überschreiben, um keine Listener aufzunehmen
        @Override
        public void addMessageEventListener(MessageEvent eventObject) {
        }

        @Override
        public void addProgressEventListener(ProgressEvent eventObject) {
        }

        @Override
        public void addProgressEventProgressListener(ProgressEventProgress eventObject) {
        }

        @Override
        public void addRemainingTimeUpdateListener(RemainingTimeUpdateListener listener) {
        }
    };

    protected static Map<String, FrameworkThread> invalidateCachesWaitThreadMap = new HashMap<>();
    public static final int MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT = 100;
    private static final int LOG_PERCENTAGE_INTERVAL = 5;

    protected String importName;
    private EtkProject project;
    private EtkMessageLog messageLog;
    private String logLanguage = iPartsConst.LOG_FILES_LANGUAGE;
    private TranslationHandler translationHandler;
    private DWFile logFile;
    private int lastLogFilePercentage = -1;
    protected int warningCount;
    protected int errorCount;
    protected boolean errorInStartTransaction;
    protected boolean cancelled;
    protected boolean finished;
    private boolean clearCachesAfterImport = true;
    protected DWFile saveDirectoryForSourceFiles;
    protected String saveFilePrefix;
    protected boolean isSingleCall;  // true für Einzeltest außerhalb Master-Importer; in diesem Fall speichern die Importer die Daten nicht ab;
    // wenn man das für Debug-Testzwecke doch will, muss man den entsprechenden Code auskommentieren
    protected long commitCounter;
    protected long startTime;
    protected DBDataObjectList bufferList;
    protected Set<IdWithType> bufferListIds;
    protected Set<IdWithType> oldBufferListIds;
    protected int skippedRecords = 0;
    protected boolean isImportWithOnlyNewDataObjects;
    protected DBDatabaseEventListener databaseEventListener;

    public AbstractGenericImporter(EtkProject project, String importName) {
        this.project = project;
        this.importName = importName;
        this.isSingleCall = true;
        translationHandler = TranslationHandler.getUiTranslationHandler();
    }

    @Override
    public String getImportName(String language) {
        return translateForLog(importName, language);
    }

    @Override
    public boolean initImport(EtkMessageLog messageLog) {
        this.messageLog = messageLog;
        if (messageLog != null) {
            messageLog.addMessageEventListener(this);
            messageLog.addProgressEventListener(this);
        }
        lastLogFilePercentage = -1;
        warningCount = 0;
        errorCount = 0;
        cancelled = false;
        finished = false;

        // Bei einem automatischen Import den evtl. vorhandenen Warte-Thread für das Löschen der Caches abbrechen
        if (isAutoImport()) {
            cancelInvalidateCacheWaitThread();
        }

        databaseEventListener = new DBDatabaseEventListener() {

            @Override
            public void transactionStarted() {
            }

            @Override
            public void commitPerformed() {
                clearOldBufferListIds(true);
            }

            @Override
            public void rollbackPerformed() {
                clearOldBufferListIds(true);
            }

            @Override
            public void batchStatementStarted() {
            }

            @Override
            public void batchStatementExecuted(boolean finished) {
                clearOldBufferListIds(false);
            }

            @Override
            public void batchStatementCancelled() {
                clearOldBufferListIds(false);
            }
        };

        startTime = System.currentTimeMillis();
        project.getDbLayer().addEventListener(MAIN, databaseEventListener);

        if (isWithTransaction()) {
            try {
                errorInStartTransaction = true;
                project.getDbLayer().startTransaction();
                errorInStartTransaction = false;
                project.getDbLayer().startBatchStatement();
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                cancelImport(translateForLog("!!Fehler beim Starten der Datenbank-Transaktion:") + " " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean finishImport() {
        return finishImport(true);
    }

    /**
     * Schließt den Import ab.
     *
     * @param invalidateCaches Sollen die Caches nach dem Import gelöscht werden?
     * @return
     */
    public boolean finishImport(boolean invalidateCaches) {
        invalidateCaches &= clearCachesAfterImport;
        if (!cancelled && !Thread.currentThread().isInterrupted()) {
            try {
                if (isWithTransaction()) {
                    project.getDbLayer().endBatchStatement();
                    project.getDbLayer().commit();
                }
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                cancelImport(e.getMessage());
            } finally {
                project.getDbLayer().removeEventListener(MAIN, databaseEventListener);
            }
        }

        // Hier nochmal die gleiche Abfrage, inzwischen kann der Import cancelled sein, wenn z.B. beim endBatchStatement eine Exception aufgetreten ist
        long endTime = 0;
        if (!cancelled && !Thread.currentThread().isInterrupted()) {
            if (isSingleCall) {
                endTime = System.currentTimeMillis();
                // Nach dem Import alle Caches löschen
                if (invalidateCaches) {
                    if (isAutoImport()) { // bei einem automatischen Import erst nach einer Wartezeit
                        int waitTime = 0;
                        try {
                            waitTime = de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.getPluginConfig().getConfigValueAsInteger(de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.CONFIG_CLEAR_CACHE_WAIT_TIME);
                        } catch (NoClassDefFoundError e) {
                            // iParts Import Plug-in nicht vorhanden
                        }
                        if (waitTime > 0) {
                            cancelInvalidateCacheWaitThread();
                            synchronized (invalidateCachesWaitThreadMap) {
                                final int waitTimeFinal = waitTime;
                                FrameworkThread waitThread = Session.startChildThreadInSession(thread -> {
                                    boolean invalidateCaches1 = false;
                                    if (!Java1_1_Utils.sleep(waitTimeFinal * 1000)) {
                                        invalidateCaches1 = true;
                                    }
                                    synchronized (invalidateCachesWaitThreadMap) {
                                        if (!invalidateCachesWaitThreadMap.containsKey(importName)) {
                                            invalidateCaches1 = false;
                                        } else {
                                            invalidateCachesWaitThreadMap.remove(importName);
                                        }
                                    }

                                    // Thread wurde nicht abgebrochen -> Caches nach Wartezeit löschen
                                    if (invalidateCaches1) {
                                        clearCaches();
                                    }
                                });
                                waitThread.setName("InvalidateCachesThread for " + importName);
                                invalidateCachesWaitThreadMap.put(importName, waitThread);
                            }
                        } else {
                            clearCaches();
                        }
                    } else {
                        clearCaches();
                    }
                }
                getMessageLog().fireMessageWithSeparators("!!Import erfolgreich abgeschlossen", MessageLogOption.TIME_STAMP);
            }
        } else {
            try {
                if (isWithTransaction()) {
                    project.getDbLayer().cancelBatchStatement();
                    if (!errorInStartTransaction) { // Rollback macht nur mit einer DB-Transaktion Sinn
                        project.getDbLayer().rollback();
                    }
                }
            } finally {
                project.getDbLayer().removeEventListener(MAIN, databaseEventListener);
            }
            getMessageLog().fireMessageWithSeparators("!!Import abgebrochen", MessageLogOption.TIME_STAMP);
            endTime = System.currentTimeMillis();
        }
        if ((getWarningCount() > 0) || (getErrorCount() > 0)) {
            getMessageLog().fireMessage(translateForLog("!!Der Import wurde mit %1 Fehlern und %2 Warnungen beendet",
                                                        Integer.toString(getErrorCount()), Integer.toString(getWarningCount())),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
        if (isSingleCall) {
            getMessageLog().fireMessage("");
            long importDuration = endTime - startTime;
            String timeDurationString = DateUtils.formatTimeDurationString(importDuration, false, true, getLogLanguage());
            getMessageLog().fireMessage(translateForLog("!!Importdauer: %1", timeDurationString));
        } else {
            getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);
        }

        getMessageLog().removeMessageEventListener(this);
        getMessageLog().removeProgressEventListener(this);

        finished = true;
        return !cancelled;
    }

    protected void clearCaches() {
        // Nur die "kleinen" Caches löschen und nicht die speziellen großen Caches
        iPartsPlugin.fireClearGlobalCaches(EnumSet.of(iPartsCacheType.ALL_SMALL_CACHES));
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public void fireEvent(MessageEventData event) {
        if (event.getMessageLogType() == MessageLogType.tmlWarning) {
            warningCount++;
        } else if (event.getMessageLogType() == MessageLogType.tmlError) {
            errorCount++;
        }

        ImportExportLogHelper.addLogMessageToLogFile(logFile, event.getFormattedMessage(getLogLanguage()), true);
    }

    @Override
    public void fireEvent(ProgressEventData event) {
        int position = event.getPosition();
        int maxPosition = event.getMaxPosition();

        // Wenn maxPosition <= 0, dann gibt es keine Prozentangaben, weil die Anzahl der zu importierenden Datensätze unbekannt ist
        if (maxPosition > 0) {
            int percentage = Math.min(100, position * 100 / maxPosition);
            if (((percentage == 0) && (lastLogFilePercentage < 0)) || ((percentage == 100) && (lastLogFilePercentage < 100))
                || (percentage - lastLogFilePercentage >= LOG_PERCENTAGE_INTERVAL)) { // nur alle LOG_PERCENTAGE_INTERVAL % loggen
                percentage = (percentage / LOG_PERCENTAGE_INTERVAL) * LOG_PERCENTAGE_INTERVAL; // auf saubere LOG_PERCENTAGE_INTERVAL % runden
                lastLogFilePercentage = percentage;
                ImportExportLogHelper.addLogMessageToLogFile(logFile, event.formatMessage(percentage + "%", getLogLanguage()), true);
            }
        }
    }

    /**
     * Anzahl der Warnungen während des Imports.
     *
     * @return
     */
    public int getWarningCount() {
        return warningCount;
    }

    /**
     * Anzahl der Fehler während des Imports.
     *
     * @return
     */
    public int getErrorCount() {
        return errorCount;
    }

    protected EtkProject getProject() {
        return project;
    }

    /**
     * {@link EtkMessageLog}, welches bei {@link #initImport(EtkMessageLog)} übergeben wurde.
     *
     * @return
     */
    public EtkMessageLog getMessageLog() {
        if (messageLog != null) {
            return messageLog;
        } else {
            return MESSAGE_LOG_NONE;
        }
    }

    protected boolean messageLogInitialized() {
        return messageLog != null;
    }

    /**
     * Bricht den Import ohne Meldung ab ohne den Fehlerzähler zu erhöhen.
     */
    public void cancelImport() {
        cancelImport(null, MessageLogType.tmlMessage);
    }

    /**
     * Bricht den Import mit entsprechender Fehlermeldung ab, wenn der Thread gestoppt wurde.
     *
     * @return
     */
    public boolean cancelImportIfInterrupted() {
        if (Thread.currentThread().isInterrupted()) {
            cancelImport("!!Import-Thread wurde frühzeitig beendet");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void cancelImport(String message) {
        cancelImport(message, MessageLogType.tmlError);
    }

    /**
     * Bricht den Import mit der übergebenen Nachricht ab und erhöht den Fehlerzähler falls <i>messageLogType</i> gleich
     * {@link MessageLogType#tmlMessage} ist bzw. den Warnungenzähler bei {@link MessageLogType#tmlWarning}.
     *
     * @param message
     * @param messageLogType
     */
    public void cancelImport(String message, MessageLogType messageLogType) {
        cancelled = true;
        if ((message != null) && !message.isEmpty()) {
            getMessageLog().fireMessage(message, messageLogType, MessageLogOption.TIME_STAMP);
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Liefert die Logdatei zurück, in die sämtliche Logausgaben vom Importer geschrieben werden sollen.
     *
     * @return
     */
    public DWFile getLogFile() {
        return logFile;
    }

    /**
     * Setzt die Logdatei, in die sämtliche Logausgaben vom Importer geschrieben werden sollen.
     *
     * @param logFile
     * @param createLogFile Flag, ob die Logdatei neu erzeugt werden soll (andernfalls werden neue Logausgaben an eine vorhandene
     *                      Logdatei angehängt)
     */
    public void setLogFile(DWFile logFile, boolean createLogFile) {
        this.logFile = logFile;
        if (!ImportExportLogHelper.checkLogFileState(logFile, createLogFile)) {
            this.logFile = null;
        }
    }

    /**
     * Setzt das Verzeichnis in das die Quelldateien gespeichert werden sollen. Zusätzlich kann bestimmt werden, ob im
     * übergebenen Zielverzeichnis ein Unterverzeichnis mit dem Importnamen erstellt werden soll.
     *
     * @param dirFile
     * @param prefix
     * @param createDefaultSubdir
     */
    public void setDirForSourceFiles(DWFile dirFile, String prefix, boolean createDefaultSubdir) {
        this.saveFilePrefix = prefix;
        if (dirFile != null) {
            if (createDefaultSubdir) {
                // Unterverzeichnis mit dem Importnamen (fest iPartsConst.LOG_FILES_LANGUAGE für die Unterverzeichnisse verwenden)
                saveDirectoryForSourceFiles = dirFile.getChild(DWFile.convertToValidFileName(translateForLog(importName, iPartsConst.LOG_FILES_LANGUAGE)));
            } else {
                saveDirectoryForSourceFiles = dirFile;
            }
        }
    }

    public DWFile getDirForSrcFiles() {
        return saveDirectoryForSourceFiles;
    }

    /**
     * Speichert das übergebene {@link EtkDataObject} in der Datenbank. Momentan wird einfach nur {@link EtkDataObject#saveToDB(boolean, DBDataObject.PrimaryKeyExistsInDB)}
     * aufgerufen. Hier könnten zukünftig aber zentral auch noch zusätzliche Fehlerbehandlungen oder andere Aktionen
     * durchgeführt werden.
     *
     * @param dataObject
     * @param checkIfPKExistsInDB
     * @param forcePKExistsInDB
     * @return {@code true} falls das {@link DBDataObject} und/oder Kind-{@link DBDataObject}s gespeichert werden mussten
     */
    public boolean saveToDB(EtkDataObject dataObject, boolean checkIfPKExistsInDB, DBDataObject.PrimaryKeyExistsInDB forcePKExistsInDB) {
        return dataObject.saveToDB(checkIfPKExistsInDB, forcePKExistsInDB);
    }


    public boolean saveToDB(EtkDataObject dataObject) {
        return saveToDB(dataObject, true);
    }

    /**
     * Speichert das {@link EtkDataObject} entweder direkt oder merkt es sich bei aktiviertem BufferedSaveToDB
     * (siehe {@link #setBufferedSave(boolean)} in einer Liste, um dann nur alle {@link #MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT}
     * Einträge alle gemerkten Einträge am Stück zu speichern. Über <code>modifyRecordCount</code> kann bestimmt werden,
     * ob der Record-Zähler beeinflusst werden soll.
     *
     * @param dataObject
     * @return
     */
    public boolean saveToDB(EtkDataObject dataObject, boolean modifyRecordCount) {
        if (isBufferedSave()) {
            return doBufferedSaveToDB(dataObject, modifyRecordCount);
        } else {
            return saveToDB(dataObject, true, DBDataObject.PrimaryKeyExistsInDB.CHECK);
        }
    }

    /**
     * Liefert die Sprache zurück, die für Logausgaben verwendet wird.
     *
     * @return
     */
    public String getLogLanguage() {
        return logLanguage;
    }

    /**
     * Setzt die Sprache, die für Logausgaben verwendet wird.
     *
     * @param logLanguage
     */
    protected void setLogLanguage(String logLanguage) {
        if ((logLanguage == null) || logLanguage.isEmpty()) {
            logLanguage = iPartsConst.LOG_FILES_LANGUAGE; // Standard-Logsprache
        }
        this.logLanguage = logLanguage;
    }

    /**
     * Liefert den übergebenen Übersetzungsschlüssel für die Logsprache zurück inkl. optionaler Platzhaltertexte.
     *
     * @param translationsKey
     * @param placeHolderTexts
     * @return
     */
    public String translateForLog(String translationsKey, String... placeHolderTexts) {
        if (placeHolderTexts != null) {
            for (int i = 0; i < placeHolderTexts.length; i++) {
                placeHolderTexts[i] = translateForLog(placeHolderTexts[i]);
            }
        }
        return translationHandler.getTextForLanguage(translationsKey, getLogLanguage(), placeHolderTexts);
    }

    /**
     * Führt nach <i>maxEntriesForCommit</i> Einträgen automatisch ein Commit durch.
     *
     * @param maxEntriesForCommit Maximale Anzahl Einträge bis zu einem automatischen Commit
     * @return {@code true} falls ein automatisches Commit durchgeführt wurde
     */
    protected boolean autoCommitAfterMaxEntries(long maxEntriesForCommit) {
        // nach maxEntriesForCommit Einträgen automatisch ein Commit machen und eine neue Transaction starten
        commitCounter++;
        if (commitCounter >= maxEntriesForCommit) {
            try {
                getProject().getDbLayer().endBatchStatement();
                getProject().getDbLayer().commit();
                getProject().getDbLayer().startTransaction();
                getProject().getDbLayer().startBatchStatement();
                commitCounter = 0;
                return true;
            } catch (Exception e) {
                getProject().getDbLayer().cancelBatchStatement();
                getProject().getDbLayer().rollback();
                cancelImport(e.getMessage());
            }
        }
        return false;
    }

    /**
     * Liefert zurück, ob es sich um einen automatischen Import handelt oder dieser manuell über einen Dialog angestoßen wurde.
     *
     * @return
     */
    public boolean isAutoImport() {
        return false;
    }

    /**
     * Ist BufferedSaveToDB aktiv?
     *
     * @return
     */
    protected boolean isBufferedSave() {
        return bufferList != null;
    }

    /**
     * Initialisierung von BufferedSaveToDB
     *
     * @param bufferedSave
     */
    protected void setBufferedSave(boolean bufferedSave) {
        if (bufferedSave) {
            if (bufferList == null) {
                bufferList = new DBDataObjectList();
                bufferListIds = new HashSet<>();
                oldBufferListIds = new HashSet<>();
            }
        } else {
            bufferList = null;
            bufferListIds = null;
            oldBufferListIds = null;
        }
    }

    /**
     * Arbeiten nach Verarbeitung aller Records
     * Es gibt z.B. Importe wo Texte über mehrere Records verteilt sind. Hier kann erst gespeichert werden wenn der
     * Text beendet ist. Beim letzen Datensatz also erst wenn alle Records verarbeitet wurden. Das kann man dann hier machen.
     */
    protected void postImportTask() {
        saveBufferListToDB(true);
        bufferList = null;
        bufferListIds = null;
        oldBufferListIds = null;
    }

    /**
     * Ein {@link EtkDataObject} zur BufferedList hinzufügen. Über <code>modifyRecordCount</code> kann bestimmt werden,
     * ob der Record-Zähler beeinflusst werden soll
     *
     * @param dataObject
     */
    protected boolean doBufferedSaveToDB(EtkDataObject dataObject, boolean modifyRecordCount) {
        if (!isBufferedSave()) {
            return false;
        }
        boolean willBeSaved = false;
        if (dataObject != null) {
            if (dataObject.isNew() || dataObject.isModifiedWithChildren()) {
                IdWithType dataObjectId = dataObject.getAsId();

                // Falls dataObjectId und damit der Primärschlüssel schon in bufferListIds vorhanden ist, wurde innerhalb
                // der bufferList der Primärschlüssel schon mal hinzugefügt -> das dataObject darf also auf keinen Fall
                // als neu gekennzeichnet bleiben, weil es ansonsten später beim Speichern eine Primärschlüsselverletzung
                // geben würde
                if (dataObject.isNew() && (bufferListIds.contains(dataObjectId) || oldBufferListIds.contains(dataObjectId))) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_BUFFERED_SAVE, LogType.DEBUG, "A DBDataObject with the primary key \""
                                                                                      + dataObjectId.toString() +
                                                                                      "\" was already added to the buffered save list and the current DBDataObject is marked as new -> clearing \"new\" flag to avoid a primary key violation when saving");
                    dataObject.__internal_setNew(false);
                }

                bufferList.add(dataObject, DBActionOrigin.FROM_EDIT);
                bufferListIds.add(dataObjectId);
                willBeSaved = true;
            } else if (modifyRecordCount) {
                reduceRecordCount();
            }
        }
        if (bufferList.size() >= MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT) { // immer schöne Schritte mit MAX_ENTRIES_FOR_COMMIT Einträgen
            saveBufferListToDB(true);
        }
        return willBeSaved;
    }


    /**
     * Speichert alle zwischengespeicherten {@link EtkDataObject} aus der BufferedList in die DB.
     * Wird nur aufgerufen, wenn {@link #isBufferedSave()} {@code true} zurückliefert und demzufolge {@link #bufferList}
     * auch vorhanden ist.
     *
     * @param clearBuffer Soll der Buffer geleert werden?
     */
    protected void saveBufferListToDB(boolean clearBuffer) {
        // BufferedSaveToDB anstoßen, falls initialisiert
        if (!cancelled && isBufferedSave()) {
            if (!bufferList.isEmpty()) {
                bufferList.saveToDB(getProject(), false);
            }

            oldBufferListIds.addAll(bufferListIds);
            bufferListIds.clear();

            if (clearBuffer) {
                bufferList.clear(DBActionOrigin.FROM_DB);
            }
        }
    }

    protected void clearOldBufferListIds(boolean commitOrRollback) {
        // Nur die oldBufferListIds leeren, weil die EtkDataObjects aus bufferListIds ja noch gar nicht gespeichert worden
        // sind und demzufolge auch noch nicht in einem Commit oder BatchStatement enthalten sein können
        if (oldBufferListIds != null) {
            if (commitOrRollback || (!commitOrRollback && !isImportWithOnlyNewDataObjects)) {
                oldBufferListIds.clear();
            }
        }
    }

    public void reduceRecordCount() {
        skippedRecords++;
    }

    /**
     * Handelt es sich um einen Importer, der zunächst alle relevanten Daten löscht und danach nur noch neue {@link EtkDataObject}s
     * importiert (und damit in der Regel ohne Überprüfung, ob die {@link EtkDataObject}s bereits in der DB existieren)?
     * Falls ja, dann wird bei aktiviertem BufferedSave die Liste der {@link EtkDataObject}-IDs erst bei einem Commit geleert,
     * ansonsten bereits nach jedem BatchStatement.
     *
     * @param isImportWithOnlyNewDataObjects
     */
    public void setIsImportWithOnlyNewDataObjects(boolean isImportWithOnlyNewDataObjects) {
        this.isImportWithOnlyNewDataObjects = isImportWithOnlyNewDataObjects;
    }

    private void cancelInvalidateCacheWaitThread() {
        FrameworkThread waitThread;
        synchronized (invalidateCachesWaitThreadMap) {
            waitThread = invalidateCachesWaitThreadMap.get(importName);
            if (waitThread != null) {
                invalidateCachesWaitThreadMap.remove(importName);
            }
        }

        if (waitThread != null) {
            // Nicht innerhalb vom synchronized aufrufen, weil es sonst zum Deadlock beim Beenden vom Thread kommt!
            waitThread.cancel();
        }
    }

    /**
     * Soll für den Import eine Transaktion inkl. Batch-Statements verwendet werden (standardmäßig ja)?
     *
     * @return
     */
    protected boolean isWithTransaction() {
        return true;
    }

    /**
     * Sollen die Caches nach dem Import gelöscht werden?
     *
     * @param clearCachesAfterImport
     */
    public void setClearCachesAfterImport(boolean clearCachesAfterImport) {
        this.clearCachesAfterImport = clearCachesAfterImport;
    }

    /**
     * Muss aufgerufen werden, wenn ein neuer Import-Job erzeugt wird.
     * <br/>Setzt auch gleich die Logdatei für den laufenden Job im {@link AbstractGenericImporter}.
     */
    public DWFile importJobRunning() {
        // fest iPartsConst.LOG_FILES_LANGUAGE für die Dateinamen verwenden
        String validFileName = DWFile.convertToValidFileName(getImportName(iPartsConst.LOG_FILES_LANGUAGE));
        DWFile runningLogFile = iPartsJobsManager.getInstance().jobRunning("Import " + validFileName);
        setLogFile(runningLogFile, true);
        return runningLogFile;
    }

    public void increaseSkippedRecord() {
        skippedRecords++;
    }

    public void reduceSkippedRecords() {
        skippedRecords--;
    }
}
