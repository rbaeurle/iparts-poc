/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler.AbstractMappedHandlerWithRecord;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsKeyValueRecordReader;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.file.DWFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Abstrakte Klasse für SAX Push Importer, die ihre XML Tags mit Handler abarbeiten
 */
public abstract class AbstractSAXPushHandlerImporter extends AbstractSAXPushDataImporter implements iPartsConst {

    private AbstractMappedHandlerWithRecord[] dataHandlers;

    public AbstractSAXPushHandlerImporter(EtkProject project, String importName, String xsdFile, FilesImporterFileListType... importFileTypes) {
        super(project, importName, xsdFile, importFileTypes);
    }

    public void setDataHandlers(AbstractMappedHandlerWithRecord... dataHandlers) {
        this.dataHandlers = dataHandlers;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if ((dataHandlers == null) || (dataHandlers.length == 0)) {
            writeNoHandlerMessage();
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
    protected void importRecord(Map<String, String> importRec, int recordNo) {
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        for (AbstractMappedHandlerWithRecord dataHandler : dataHandlers) {
            dataHandler.onPreImportTask();
        }
    }

    @Override
    protected void postImportTask() {
        // Hinweis ausgeben, wenn die Importdatei zu einem ausgewählten Handler keine Daten hatte
        for (AbstractMappedHandlerWithRecord dataHandler : dataHandlers) {
            if (dataHandler.getTagCounter() < 1) {
                getMessageLog().fireMessage(translateForLog("!!Importdatei enthielt keine Datensätze zu %1",
                                                            translateForLog(dataHandler.getHandlerName())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
        }
        super.postImportTask();
        super.setBufferedSave(true);
        for (AbstractMappedHandlerWithRecord dataHandler : dataHandlers) {
            dataHandler.onPostImportTask();
        }
        super.postImportTask();
    }


    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getType())) {
            iPartsKeyValueRecordReader reader = new iPartsKeyValueRecordReader(importFile, "");
            String selectedHandler = getSelectedHandlerText();
            getMessageLog().fireMessage(translateForLog("!!Ausgewählte Datentypen: %1", selectedHandler),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            return importMasterData(dataHandlers, reader);
        }
        return false;
    }

    private String getSelectedHandlerText() {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(dataHandlers).forEach(handler -> {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(translateForLog(handler.getHandlerName()));
        });
        return builder.toString();
    }


    /**
     * Schreibt eine optionale Nachricht, wenn keine Handler vorhanden sind
     */
    protected void writeNoHandlerMessage() {

    }

    protected abstract String getType();
}
