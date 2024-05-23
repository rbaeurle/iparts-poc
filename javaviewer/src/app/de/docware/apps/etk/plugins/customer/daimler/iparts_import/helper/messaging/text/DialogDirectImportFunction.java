/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.text;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DialogEndMessageWorker;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.AbstractEtkFunctionImportHelper;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.os.OsUtils;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper für das Importieren von DIALOG Daten als FixedLength Dateien
 */
public abstract class DialogDirectImportFunction extends AbstractEtkFunctionImportHelper {

    private final List<Map<String, String>> importData = new ArrayList<>(); // Alle ImportRecords, die in einer Datei vorkommen
    private AbstractDIALOGDataImporter currentDIALOGImporter; // Der aktuelle DIALOG Importer
    private String dialogType; // DIALOG Tabellenname
    private MQChannelType channelType;
    private boolean isRunning; // Flag, ob der aktuelle Import gerade läuft
    // Liste mit allen Zeilen (also Datensätzen), die innerhalb eines kompletten Importlaufs importiert wurden
    private final List<String> textContentsPerFile = new ArrayList<>();
    private final Map<String, DWFile> existingDirectories = new HashMap<>(); // Map mit allen bestehenden Verzeichnissen

    public DialogDirectImportFunction(String importAliasName) {
        super(importAliasName);
    }

    /**
     * Initialisiert den aktuellen Importer passend zum übergebenen <code>dialogType</code>
     *
     * @return
     */
    private boolean initImporter() {
        // Den aktuellen Importer erzeugen
        if (channelType != null) {
            AbstractDataImporter importer = createImporter();
            if (importer instanceof AbstractDIALOGDataImporter) {
                currentDIALOGImporter = (AbstractDIALOGDataImporter)importer;
                currentDIALOGImporter.setChannelType(channelType);
                return true;
            }
        }
        return false;
    }

    @Override
    public void run(AbstractJavaViewerForm owner) {
        // Unterscheidung zwischen einem "normalen" DIALOG Importer und dem Worker für die ENDE Nachrichten
        boolean isEndMessage = (dialogType != null) && dialogType.equals(DialogEndMessageWorker.DIALOG_TABLENAME);
        if (isEndMessage) {
            handleDIALOGEndMessage();
        } else {
            if ((currentDIALOGImporter == null) && !initImporter()) {
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Error while initializing importer for "
                                                                                + dialogType + " and MQ");
                return;
            }
            startDIALOGDataImport();
        }
    }

    public String getDialogType() {
        return dialogType;
    }

    public AbstractDIALOGDataImporter getCurrentDIALOGImporter() {
        return currentDIALOGImporter;
    }

    public boolean isRunning() {
        return (currentDIALOGImporter != null) && !currentDIALOGImporter.isCancelled() && isRunning;
    }

    /**
     * Startet den Import für eine DIALOG Satzart
     */
    private synchronized void startDIALOGDataImport() {
        isRunning = true;
        // Aktuelle Daten aus dem zentralen Speicher in den lokalen Speicher verschieben
        List<Map<String, String>> importDataLocal = moveImportDataInLocalList();
        if (!importDataLocal.isEmpty()) {
            // KeyValueReader für DIALOG Import aus nicht XML Dateien erzeugen
            DIALOGDirectKeyValueReader keyValueReader = new DIALOGDirectKeyValueReader(dialogType, importDataLocal);
            // Importer mit dem spezifischen KeyValueReader aufrufen
            boolean importResult = currentDIALOGImporter.startImportWithExternalReader(keyValueReader, null);
            // Zum Schluss die Importdatei und die Log-Datei verschieben
            moveLogAndInputFile(currentDIALOGImporter, importResult, currentDIALOGImporter.getChannelType());
        }
        cleanUpAfterImport();
        isRunning = false;
    }

    private void cleanUpAfterImport() {
        // Dateien wurden importiert -> Map mit Dateien löschen
        textContentsPerFile.clear();
        existingDirectories.clear();
        // Nach einem kompletten Import den Importer auf "null" setzen, damit er beim nächsten Lauf erzeugt wird
        currentDIALOGImporter = null;
    }

    /**
     * Verarbeitet eine ENDE Nachricht (keine DIALOG Satzart)
     */
    private synchronized void handleDIALOGEndMessage() {
        isRunning = true;
        // Aktuelle Daten aus dem zentralen Speicher in den lokalen Speicher verschieben
        List<Map<String, String>> importDataLocal = moveImportDataInLocalList();
        AbstractDataImporter importer = createImporter();
        if (importer instanceof DialogEndMessageWorker) {
            DialogEndMessageWorker worker = (DialogEndMessageWorker)importer;
            boolean result = worker.handleEndMessageFromDIALOGDirect(importDataLocal, getChannelType());
            moveLogAndInputFile(worker, result, getChannelType());

        }
        cleanUpAfterImport();
        isRunning = false;
    }

    /**
     * Verschiebt die Log-Datei und die Import-Datei
     *
     * @param importResult
     */
    private void moveLogAndInputFile(AbstractDataImporter importer, boolean importResult, MQChannelType channelType) {
        // Erst die Log Datei verschieben, dann haben wir auch das passende Unterverzeichnis (processed, error, usw.)
        String subDirFromLogFile = importer.moveLogFile(importResult);
        // Jetzt aus allen Zeilen (einzelne Datensätze) eine Datei erzeugen, damit nach dem Import klar ist, welche
        // Datensätze zusammen importiert wurden
        StringBuilder builder = new StringBuilder(textContentsPerFile.size());
        for (int i = 0; i < textContentsPerFile.size(); i++) {
            String line = textContentsPerFile.get(i);
            if (i > 0) {
                builder.append(OsUtils.NEWLINE);
            }
            builder.append(line);
        }
        // Unterverzeichnis mit minutengenauem Stempel
        String dateTimeSubfolder = XMLImportExportDateHelper.getFormattedDateTimeForDIALOGDirectMessages();
        // Hier den DIALOG Typ in den Verzeichnisbaum einsetzen
        String subDir = dialogType + OsUtils.FILESEPARATOR + subDirFromLogFile + OsUtils.FILESEPARATOR + dateTimeSubfolder;
        // Jetzt das Verzeichnis erzeugen, z.B. Kanalname\DIALOG Typ\processed\Zeitstempel\
        DWFile directory = iPartsTextToDIALOGDataHelper.getDirectoryForChannel(channelType, subDir, existingDirectories);
        // Dateinamen erzeugen (inkl Zeitstempel)
        String fileName = XMLImportExportDateHelper.getFormattedDateTimeForMessageCopies() + "_" + dialogType
                          + "_" + textContentsPerFile.size() + "_FILES";
        DWFile destFile = directory.getChild(fileName);
        try {
            destFile.writeTextFile(builder.toString().getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
        } catch (IOException e) {
            Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "Could not create file \""
                                                                            + destFile.getAbsolutePath());
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
        }
        // Pfad zur erzeugten Datei mit den ImportRecords für diesen Import an die Log-Datei schreiben
        importer.addFileLocationToLog(importer.getLogFile(), destFile, "MQ DIALOG Direct");
    }

    /**
     * Erzeugt eine lokale Instanz des ImportRecord Speichers
     *
     * @return
     */
    private List<Map<String, String>> moveImportDataInLocalList() {
        List<Map<String, String>> result;
        synchronized (importData) {
            result = new ArrayList<>(importData);
            importData.clear();
        }
        return result;
    }

    /**
     * Fügt einen ImportRecord hinzu
     *
     * @param importRecord
     * @param textContent  - eine Zeile aus der Importdatei (ein Datensatz)
     */
    public void addExternalImportData(Map<String, String> importRecord, String textContent) {
        synchronized (importData) {
            if ((importRecord != null) && !importRecord.isEmpty()) {
                importData.add(importRecord);
                textContentsPerFile.add(textContent);
            }
        }
    }

    public List<Map<String, String>> getImportData() {
        return importData;
    }

    public void setTypeAndChannel(String dialogType, MQChannelType channelType) {
        // Für den Import relevanten Daten setzen
        this.dialogType = dialogType;
        this.channelType = channelType;
    }

    public MQChannelType getChannelType() {
        return channelType;
    }

    /**
     * Key-Value-Reader für DIALOG Importer, die nicht als XML Dateien via MQ hereinkommen
     */
    private static class DIALOGDirectKeyValueReader extends AbstractKeyValueRecordReader {

        private final List<Map<String, String>> importData;
        private int importRecCount;

        public DIALOGDirectKeyValueReader(String tableName, List<Map<String, String>> importRecs) {
            super(DWFile.get("MQ DIALOG Direct"), tableName);
            this.importData = importRecs;
        }

        protected int getImportRecCount() {
            return importRecCount;
        }

        protected void setImportRecCount(int importRecCount) {
            this.importRecCount = importRecCount;
        }

        protected void incImportRecCount() {
            setImportRecCount(getImportRecCount() + 1);
        }

        public List<Map<String, String>> getImportData() {
            return importData;
        }

        @Override
        public boolean open() throws IOException, SAXException {
            setImportRecCount(0);
            return (getImportData() != null) && !getImportData().isEmpty();
        }

        @Override
        public Map<String, String> getNextRecord() {
            Map<String, String> nextRecord = null;
            if (getImportRecCount() < importData.size()) {
                nextRecord = importData.get(getImportRecCount());
                incImportRecCount();
            }
            return nextRecord;
        }

        @Override
        public Map<String, String> getNextRecord(String tableName) {
            return getNextRecord();
        }

        @Override
        public boolean saveFile(DWFile dir, String prefix) {
            return true;
        }

        @Override
        public void close() {

        }

        @Override
        public int getRecordNo() {
            return getImportRecCount();
        }

        @Override
        public int getRecordCount() {
            return importData.size();
        }
    }
}
