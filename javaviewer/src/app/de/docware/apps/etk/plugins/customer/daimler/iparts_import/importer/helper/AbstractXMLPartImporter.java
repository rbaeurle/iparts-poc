/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler.AbstractMappedHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsKeyValueRecordReader;
import de.docware.util.file.DWFile;

import java.util.List;
import java.util.Map;

/**
 * Abstrakter Importer für XML Teilestamm Importer (SRM u. PRIMUS)
 */
public abstract class AbstractXMLPartImporter extends AbstractSAXPushDataImporter implements iPartsConst, EtkDbConst {

    private boolean doBufferSave = true;

    public AbstractXMLPartImporter(EtkProject project, String importName, String xsdFile, FilesImporterFileListType... importFileTypes) {
        super(project, importName, xsdFile, importFileTypes);
        tableName = TABLE_MAT;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }


    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return true;
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
    }

    @Override
    protected boolean importMasterDataFromMQMessage(AbstractMQMessage mqMessage) {
        AbstractMappedHandler handler = getHandler();
        iPartsKeyValueRecordReader reader = new iPartsKeyValueRecordReader(mqMessage);
        return importMasterData(new AbstractMappedHandler[]{ handler }, reader);
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            AbstractMappedHandler handler = getHandler();
            iPartsKeyValueRecordReader reader = new iPartsKeyValueRecordReader(importFile, "");
            return importMasterData(new AbstractMappedHandler[]{ handler }, reader);
        }
        return false;
    }


    protected abstract AbstractMappedHandler getHandler();
}
