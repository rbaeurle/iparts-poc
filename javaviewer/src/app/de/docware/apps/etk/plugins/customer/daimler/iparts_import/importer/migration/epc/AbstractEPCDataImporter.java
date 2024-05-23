/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Superklasse, für alle einfachen EPC Importer (keine Dictionary Importer)
 */
public abstract class AbstractEPCDataImporter extends AbstractDataImporter implements iPartsConst {

    private String[] headerNames;

    private String tablename;

    private HashMap<String, String> fieldMapping;

    public AbstractEPCDataImporter(EtkProject project, String importName, String fileListName, String tablename, boolean removeExistingData) {
        this(project, importName, fileListName, tablename, removeExistingData, true);
    }

    public AbstractEPCDataImporter(EtkProject project, String importName, String fileListName, String tablename, boolean removeExistingData, boolean removeExistingDataSelectable) {
        super(project, importName,
              new FilesImporterFileListType(tablename, fileListName, true, removeExistingData, removeExistingDataSelectable,
                                            new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_GZ,
                                                          MimeTypes.EXTENSION_ALL_FILES }));
        this.tablename = tablename;
        headerNames = getHeaderNames();
        fieldMapping = initMapping();
    }

    public AbstractEPCDataImporter(EtkProject project, String importName, FilesImporterFileListType... importFileTypes) {
        super(project, importName, importFileTypes);
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public void setHeaderNames(String[] headerNames) {
        this.headerNames = headerNames;
    }

    public void setFieldMapping(HashMap<String, String> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }


    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(true);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (StrUtils.isEmpty(tablename) || (fieldMapping == null)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            return importMasterData(prepareImporterKeyValueGZ(importFile, tablename, EPCImportHelper.EPC_IMPORT_DATA_SEPARATOR, true, headerNames, '\0'));
        } else if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_CSV)) {
            return importMasterData(prepareImporterKeyValue(importFile, tablename, EPCImportHelper.EPC_IMPORT_DATA_SEPARATOR, true, headerNames, '\0'));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, tablename, EPCImportHelper.EPC_IMPORT_DATA_SEPARATOR, false, null));
        }
    }

    protected HashMap<String, String> getFieldMapping() {
        return fieldMapping;
    }

    protected abstract HashMap<String, String> initMapping();

    protected abstract String[] getHeaderNames();

}
