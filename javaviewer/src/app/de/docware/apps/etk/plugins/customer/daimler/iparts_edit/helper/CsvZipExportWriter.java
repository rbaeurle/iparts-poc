package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.file.DWOutputStream;
import de.docware.util.os.OsUtils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Hilfsklasse für den Export einer gezippten CSV-Datei; incl. dem Download-Dialog
 *
 * Beispiel
 * CsvZipExportWriter exportWriter = new CsvZipExportWriter();
 * try {
 * if (exportWriter.open(fileName)) {
 * exportWriter.writeHeader(getHeader());
 * for (Anzahl Lines) {
 * exportWriter.writeToZipStream(line);
 * }
 * exportWriter.closeOutputStreams();
 * exportWriter.downloadExportFile();
 * }
 * } finally {
 * if (exportWriter != null) {
 * exportWriter.closeOutputStreams();
 * exportWriter.clearAfterDownload();
 * }
 * }
 */
public class CsvZipExportWriter {

    public static final String CSV_DELIMITER = "\t";

    protected DWFileCoding fileCoding;
    protected DWFile exportFile;
    protected DWOutputStream outputStream;
    protected ZipOutputStream zipOutputStream;

    public CsvZipExportWriter() {
        this.fileCoding = DWFileCoding.UTF8_BOM;
        this.exportFile = null;
    }

    public DWFileCoding getFileCoding() {
        return fileCoding;
    }

    /**
     * FileCoding wird nur übernommen, solange die Datei noch nicht offen ist
     *
     * @param fileCoding
     */
    public void setFileCoding(DWFileCoding fileCoding) {
        if (!isInit()) {
            this.fileCoding = fileCoding;
        }
    }

    public boolean isInit() {
        return exportFile != null;
    }

    /**
     * Öffnen der gezippten CSV-Datei im temporären Verzeichnis
     *
     * @param exportFileName
     * @return
     */
    public boolean open(String exportFileName) {
        if (isInit()) {
            return true;
        }

        if (open_intern(exportFileName)) {
            if (!writeZipEntry(exportFileName)) {
                // Zip-Stream schließen
                closeOutputStreams();
                clearAfterDownload();
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Schließen der Streams
     */
    public void closeOutputStreams() {
        // Zip-Stream schließen
        if (zipOutputStream != null) {
            try {
                zipOutputStream.closeEntry();
                zipOutputStream.finish();
                ((FilterOutputStream)zipOutputStream).flush();
                zipOutputStream.close();
                zipOutputStream = null;
            } catch (IOException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            }
        }
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            }
        }
    }

    /**
     * CSV-Datei zum Download anbieten
     */
    public void downloadExportFile() {
        if (!isInit() || (outputStream != null)) {
            return;
        }
        exportFile.downloadFile();
    }

    /**
     * Temporäre Datei(en) aufräumen
     */
    public void clearAfterDownload() {
        if (exportFile != null) {
            // temp-Verzeichnis löschen
            exportFile.getParentDWFile().deleteRecursivelyWithRepeat();
            exportFile = null;
        }
    }

    /**
     * Header der CSV-Datei schreiben
     *
     * @param header
     * @return
     */
    public boolean writeHeader(List<String> header) {
        if (isInit()) {
            // ggf BOM schreiben
            byte[] fileBOM = fileCoding.getBom();
            if (fileBOM.length > 0) {
                writeToZipStream(fileBOM);
            }

            // Ausgabe Header
            writeToZipStream(header);
            return true;
        }
        return false;
    }

    /**
     * Eine String schreiben
     * Handlet es sich um eine ganze Zeile, sollte OsUtils.NEWLINE am Ende stehen (wird nicht überprüft)
     * Der String wird automatisch mit dem fileCoding versehen
     *
     * @param line
     */
    public void writeToZipStream(String line) {
        if (isInit()) {
            try {
                writeToZipStream(line.getBytes(fileCoding.getJavaCharsetName()));
            } catch (IOException e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            }
        }
    }

    public void writeToZipStream(List<String> elements) {
        writeToZipStream(convertToLine(elements));
    }

    public void writeToZipStream(StringBuilder str) {
        writeToZipStream(str.toString());
    }

    public void writeToZipStream(CsvStringBuilder str) {
        writeToZipStream(str.toStringWithNewLine());
    }

    protected boolean open_intern(String exportFileName) {
        if (isInit()) {
            return true;
        }
        // temp Verzeichnis anlegen
        // DWFile mit Zip-Stream öffnen
        DWFile tempDir = DWFile.createTempDirectory("daim");
        if (tempDir == null) {
            return false;
        }
        exportFile = tempDir.getChild(exportFileName + "." + MimeTypes.EXTENSION_ZIP);
        outputStream = exportFile.getOutputStream();
        zipOutputStream = new ZipOutputStream(outputStream, Charset.forName(fileCoding.getJavaCharsetName()));
        return true;
    }

    /**
     * Eintrag in der Zip-Datei für die CSV-Datei vornehemn
     *
     * @param exportFileName
     * @return
     */
    protected boolean writeZipEntry(String exportFileName) {
        ZipEntry entry = new ZipEntry(exportFileName + "." + MimeTypes.EXTENSION_CSV);
        entry.setTime(Calendar.getInstance().getTimeInMillis());
        try {
            zipOutputStream.putNextEntry(entry);
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            return false;
        }
        return true;
    }

    private CsvStringBuilder convertToLine(List<String> elements) {
        CsvStringBuilder str = new CsvStringBuilder();
        // speziell für Ausgabe CSV-Datei: auch leere Einträge erzeugen ein 'feld'
        for (String cell : elements) {
            cell = StrUtils.replaceNewlinesWithSpaces(cell);
            cell = cell.replaceAll(CSV_DELIMITER, " ");
            str.append(cell);
        }
        return str;
    }

    /**
     * zentrale Routine zum Schreiben
     *
     * @param bytes
     */
    private void writeToZipStream(byte[] bytes) {
        try {
            zipOutputStream.write(bytes, 0, bytes.length);
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
        }
    }

    public DWFile getExportFile() {
        return exportFile;
    }


    /**
     * Gekapselter StringBuilder mit CSV-Delimiter zwischen einzelnen Werten
     */
    public static class CsvStringBuilder {

        private StringBuilder str;
        private int index;

        public CsvStringBuilder() {
            clear();
        }

        public void clear() {
            this.str = new StringBuilder();
            this.index = 0;
        }

        public void append(String value) {
            if (index > 0) {
                str.append(CsvZipExportWriter.CSV_DELIMITER);
            }
            str.append(value);
            index++;
        }

        public String toString() {
            return str.toString();
        }

        public String toStringWithNewLine() {
            return str.toString() + OsUtils.NEWLINE;
        }
    }
}
