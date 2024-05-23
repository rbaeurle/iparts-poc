/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBOMDataImporter extends AbstractDataImporter implements iPartsConst {

    protected boolean importToDB = true; // sollen die Daten abgespeichert werden?
    protected boolean doBufferSave = true;
    private String destTable;
    private String importTable;
    private HashMap<String, String> mapping;

    public AbstractBOMDataImporter(EtkProject project, String importName, String destTable, String importTable, FilesImporterFileListType... importFileTypes) {
        super(project, importName, importFileTypes);
        initImporter(destTable, importTable);
    }

    private void initImporter(String destTable, String importTable) {
        this.destTable = destTable;
        this.importTable = importTable;
        this.mapping = new HashMap<>();
        initMapping(mapping);
    }

    public AbstractBOMDataImporter(EtkProject project, String importName, boolean withHeader, String destTable, String importTable, FilesImporterFileListType... importFileTypes) {
        this(project, importName, destTable, importTable, importFileTypes);
        this.withHeader = withHeader;
    }

    @Override
    protected void preImportTask() {
        setBufferedSave(doBufferSave);
        super.preImportTask();
    }


    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return Utils.objectEquals(importer.getCurrentTableName(), getImportTable()) ||
                   Utils.objectEquals(importer.getCurrentTableName(), getDestinationTable());
        }
        return false;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExists = getMustExist();
        String[] mustHaveData = getMustHaveData();
        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);
    }

    protected void initMapping(HashMap<String, String> mapping) {
    }

    protected String getDestinationTable() {
        return destTable;
    }

    protected String getImportTable() {
        return importTable;
    }

    protected HashMap<String, String> getMapping() {
        return mapping;
    }

    protected abstract String[] getMustExist();

    protected abstract String[] getMustHaveData();
}

