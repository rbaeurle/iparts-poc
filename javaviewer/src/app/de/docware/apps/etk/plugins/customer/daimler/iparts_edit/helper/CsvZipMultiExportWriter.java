package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

/**
 * Hilfsklasse für den Export mehrerer gezippten CSV-Dateien; incl. dem Download-Dialog
 * Hinweis: Um das Schließen der einzelnen Zip-Entries muss man sich nicht kümmern
 *
 * Beispiel
 * CsvZipMultiExportWriter exportWriter = new CsvZipMultiExportWriter();
 * try {
 * if (exportWriter.open(Zip_FileName)) {
 * if (exportWriter.openZipEntry(FileName_One)) {
 * exportWriter.writeHeader(getHeaderForFileOne());
 * for (Anzahl Lines File One) {
 * exportWriter.writeToZipStream(line);
 * }
 * }
 * if (exportWriter.openZipEntry(FileName_Two)) {
 * exportWriter.writeHeader(getHeaderForFileTwo());
 * for (Anzahl Lines File Two) {
 * exportWriter.writeToZipStream(line);
 * }
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
public class CsvZipMultiExportWriter extends CsvZipExportWriter {

    public CsvZipMultiExportWriter() {
        super();
    }

    public boolean open(String exportFileName) {
        if (isInit()) {
            return true;
        }
        return open_intern(exportFileName);
    }

    public boolean openZipEntry(String entryFileName) {
        return writeZipEntry(entryFileName);
    }
}
