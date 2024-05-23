/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper;

import de.docware.framework.modules.gui.misc.monitor.directory.AbstractDirectoryMonitor;
import de.docware.util.file.DWFile;

import java.io.FilenameFilter;
import java.util.List;

/**
 * Interface für einen Monitor-Handler der beim Verarbeiten von Dateien in einem überwachten Verzeichnis benötigt wird
 */
public interface FileMonitorHandler {

    /**
     * Liefert zurück, ob ein Import überhaupt stattfinden soll
     *
     * @return
     */
    boolean isImportEnabled();

    /**
     * Verarbeitet die aktuelle Importdatei.
     *
     * @param workFile
     * @param path
     * @return
     */
    String processFile(DWFile workFile, String path);

    /**
     * Liefert die Logdatei, in die geschrieben werden soll
     *
     * @return
     */
    DWFile getMessageLogFile();

    FilenameFilter getFileExtensionFilter();

    /**
     * Liefert einen spezifischen Typ von Verzeichnismonitor (Polling oder JAVA7 FileMonitor)
     *
     * @return
     */
    AbstractDirectoryMonitor getDirectoryMonitor();

    /**
     * Liefert den Delimiter für die Bezeichnung der Zwischendateien
     *
     * @return
     */
    String getFileNameDelimiter();

    MonitorTypes getMonitorType();

    boolean isArchiveFilesEnabled();

    /**
     * Sortiert die Dateien, die vom Monitor an den RFTSXHelper übergeben wurden
     *
     * @param files
     * @return
     */
    List<DWFile> sortFiles(List<DWFile> files);

    /**
     * Initialisiert zusätzlich Daten nachdem der Monitor die generelle Prozedur durchgelaufen ist
     *
     * @param monitor
     */
    void initAdditionalData(AbstractDirectoryMonitor monitor);
}
