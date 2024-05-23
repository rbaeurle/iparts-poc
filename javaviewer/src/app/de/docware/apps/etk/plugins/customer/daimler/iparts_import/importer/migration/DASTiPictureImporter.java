/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images.DASTiPictureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.file.DWFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Spezieller Importer, der die zu importierenden Bilddateien in einer speziellen Verzeichnisstuktur ablegt.
 * Die Logik zur Ablage liegt im Helper.
 * Einen Konfiguration in den Admin-Optionen für den Root-Knoten der Verzeichnisstruktur gibt es auch noch:
 * CONFIG_IMPORT_RFTSX_IMAGES_UNZIP_ROOT_DIR
 */

public class DASTiPictureImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    public DASTiPictureImporter(EtkProject project) {
        super(project, "DASTi Bildarchive",
              new FilesImporterFileListType("DASTi", "!!DASTi Bildarchive", false, false, false, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ }));
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    /**
     * Funktion, die die eingehenden Dateien in der Verzeichnisstruktur ablegt.
     *
     * @param importFileType
     * @param importFile
     * @return
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        DASTiPictureHelper helper = new DASTiPictureHelper(getProject());
        List<DWFile> fileList = new ArrayList<>();
        fileList.add(importFile);
        try {
            helper.putPictures(fileList);
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            return false;
        }
        return true;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return false;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return false;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
    }

    @Override
    public boolean isAutoImport() {
        return true; // Vermeidet häufige Cache-Löschungen bei mehreren DASTi-Importdateien
    }
}
