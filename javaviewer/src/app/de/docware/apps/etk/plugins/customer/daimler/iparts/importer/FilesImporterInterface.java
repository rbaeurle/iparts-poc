/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.importer;

import de.docware.util.file.DWFile;

import java.util.List;

/**
 * Interface zum Importieren von mehreren Dateilisten.
 */
public interface FilesImporterInterface extends GenericImporterInterface {

    /**
     * Importiert die angegebenen Importdateien mit dem entsprechenden Import-Dateilisten-Typ und löscht abhängig von
     * <i>removeAllExistingData</i> vorher alle vorhandenen Daten.
     *
     * @param importFileType
     * @param importFiles
     * @param removeAllExistingData
     * @return
     */
    boolean importFiles(FilesImporterFileListType importFileType, List<DWFile> importFiles, boolean removeAllExistingData);

    /**
     * Diese Import-Dateilisten-Typen kennt der Importer.
     *
     * @return
     */
    FilesImporterFileListType[] getImportFileTypes();
}
