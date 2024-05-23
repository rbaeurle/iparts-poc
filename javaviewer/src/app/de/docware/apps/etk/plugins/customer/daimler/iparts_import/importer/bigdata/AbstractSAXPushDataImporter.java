/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.AbstractXMLHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.BigDataXMLHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler.AbstractMappedHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler.MappingHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsKeyValueRecordReader;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWInputStream;
import de.docware.util.file.DWTarGzHelper;
import de.docware.util.xml.XMLUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Basisklasse für einen SAX PUSH-Importer (XML-File).
 * Im Importrecord wird als Key der gesamte XML-Pfad angegeben. Die Attribute kommen mit ':' dazu.
 * Ohne Attributanhang ist es das Textelement. Grundsätzlich werden alle Tags geliefert.
 * Der Importer muss deshalb in {@link #checkSkipRecord} die nicht benötigten Tags rausfiltern.
 * Das Textelement ist immer da und dient als Kennzeichen in welchem Tag wir gerade sind.
 * <p>
 * Bsp:
 * <res><text> -> Textelement
 * <res><text>:lang -> Attribut lang
 * <p>
 * Bsp: Erkennung, in welchem Tag ich bin:
 * importRec.containsKey(<res><text>)
 * <p>
 * Zu beachten ist, dass die Tags erst geliefert werden, wenn sie geschlossen wurden. Nur dann sind die vollständig.
 * Das bedeutet, dass der Hauptknoten erst am Ende kommt und nicht, wenn er beginnt.
 */
public abstract class AbstractSAXPushDataImporter extends AbstractDataImporter {

    private String xsdFile;
    protected int importedRecords;
    protected int skippedTags;
    protected String tableName;
    private SAXParser parser;
    private AbstractXMLHandler currentHandler;

    public AbstractSAXPushDataImporter(EtkProject project, String importName, String xsdFile, FilesImporterFileListType... importFileTypes) {
        super(project, importName, importFileTypes);
        this.xsdFile = xsdFile;
    }

    /**
     * Vorbereitung des KeyValueRecordReaders via {@link SAXParser}
     *
     * @param importer
     * @param importFile
     * @param tablename
     * @return
     */
    protected BigDataXMLHandler preparePushImporter(AbstractSAXPushDataImporter importer, DWFile importFile, String tablename) {
        return new BigDataXMLHandler(importer, new iPartsKeyValueRecordReader(importFile, tablename));
    }


    /**
     * Vorbereitung des KeyValueRecordReaders via {@link SAXParser}
     *
     * @param importer
     * @return
     */
    protected BigDataXMLHandler preparePushImporter(AbstractSAXPushDataImporter importer, AbstractMQMessage mqMessage) {
        return new BigDataXMLHandler(importer, new iPartsKeyValueRecordReader(mqMessage));
    }

    protected boolean importMasterData(BigDataXMLHandler handler) {
        return importMasterData(handler, handler.getReader());
    }

    /**
     * @param dataHandlers
     * @param reader
     * @return
     */
    protected boolean importMasterData(AbstractMappedHandler[] dataHandlers, AbstractKeyValueRecordReader reader) {
        if ((dataHandlers == null) || (dataHandlers.length == 0)) {
            return false;
        }
        MappingHandler mappingHandler = new MappingHandler(dataHandlers);
        // Hier MappingHandler setzen, damit z.B. bei einem cancel() die Info an alle Subhandler geschickt werden kann
        this.currentHandler = mappingHandler;
        mappingHandler.setMessageLog(getMessageLog());
        try {
            parser = initParser(mappingHandler);
            mappingHandler.setParser(parser);
        } catch (SAXException | ParserConfigurationException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.equals(AbstractXMLHandler.IMPORT_CANCELLED_MESSAGE)) {
                cancelImport();
            } else {
                cancelImport(translateForLog("!!Fehler beim Import:") + " " + e.getMessage(), MessageLogType.tmlError);
                Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, errorMessage);
            }
            return false;
        }
        return importMasterData(mappingHandler, reader);
    }

    /**
     * Main-Routine für den PUSH-Importer
     * nimmt die Log-Ausgaben und Standardabfragen vor
     *
     * @param handler
     * @return
     */
    protected boolean importMasterData(AbstractXMLHandler handler, AbstractKeyValueRecordReader reader) {
        this.currentHandler = handler;
        try {
            logImportStarted(importName);
            if (!openAndSave(reader)) {
                return false;
            }
            if (!checkTableValidity(reader)) {
                return false;
            }
            getMessageLog().fireProgress(0, -1, "", true, false);
            importedRecords = 0;
            skippedRecords = 0;
            skippedTags = 0;
            preImportTask();
            try {
                if (parser == null) {
                    parser = initParser(getCurrentHandler());
                    if (parser == null) {
                        return false;
                    }
                }
                try (DWInputStream inputStream = reader.getContentAsInputStream()) {
                    DWFile file = reader.getInputFile();
                    if ((file != null) && MimeTypes.hasExtension(file, MimeTypes.EXTENSION_GZ)) {
                        try (GZIPInputStream zipInputStream = new GZIPInputStream(inputStream)) {
                            parser.parse(zipInputStream, getCurrentHandler());
                        }
                    } else {
                        parser.parse(inputStream, getCurrentHandler());
                    }
                }
            } catch (SAXException e) {
                cancelImport();
                String errorMessage = e.getMessage();
                if (!errorMessage.equals(AbstractXMLHandler.IMPORT_CANCELLED_MESSAGE)) {
                    Logger.log(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.DEBUG, errorMessage);
                }
                return false;
            } catch (ParserConfigurationException | IOException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String fullStacktraceWithSuppressed = sw.toString();
                cancelImport(translateForLog("!!Fehler beim Import:") + "\n"
                             + fullStacktraceWithSuppressed, MessageLogType.tmlError);
            } finally {
                parser = null;
            }
            postImportTask();
            logImportRecordsFinished(getCurrentHandler().getTagCounter());
        } finally {
            reader.close();
            getMessageLog().hideProgress();
        }
        return true;
    }

    public AbstractXMLHandler getCurrentHandler() {
        return currentHandler;
    }

    @Override
    public void cancelImport(String message, MessageLogType messageLogType) {
        if (getCurrentHandler() != null) {
            getCurrentHandler().setCancelled(true);
        }
        super.cancelImport(message, messageLogType);
    }

    @Override
    public boolean saveToDB(EtkDataObject dataObject) {
        incImportedRecords();
        return super.saveToDB(dataObject);
    }

    @Override
    protected void logImportRecordsFinished(int importRecordCount) {
        if (isSingleCall) {
            StringBuilder msg = new StringBuilder();
            if (skippedTags > 0) {
                msg.append(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount), getTagTextForLog(importRecordCount)));
                msg.append(", ");
                msg.append(translateForLog("!!%1 %2 übernommen", String.valueOf(importRecordCount - skippedTags), getTagTextForLog(importRecordCount - skippedTags)));
            } else {
                msg.append(translateForLog("!!%1 %2 erfolgreich übernommen", String.valueOf(importRecordCount), getTagTextForLog(importRecordCount)));
            }
            msg.append(", ");
            if (skippedRecords > 0) {
                msg.append(translateForLog("!!%1 %2 bearbeitet", String.valueOf(importedRecords), getDatasetTextForLog(importedRecords)));
                msg.append(", ");
                int importRecordCountWithoutSkipped = Math.max(0, importedRecords - skippedRecords);
                msg.append(translateForLog("!!%1 %2 importiert.", String.valueOf(importRecordCountWithoutSkipped),
                                           getDatasetTextForLog(importRecordCountWithoutSkipped)));
            } else {
                msg.append(translateForLog("!!%1 %2 erfolgreich importiert", String.valueOf(importedRecords), getDatasetTextForLog(importedRecords)));
            }
            getMessageLog().fireMessage(msg.toString(), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        } else {
            getMessageLog().fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                                        getTagTextForLog(importRecordCount)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    private String getTagTextForLog(int recordCount) {
        if (recordCount == 1) {
            return translateForLog("!!Tag");
        } else {
            return translateForLog("!!Tags");
        }
    }

    private void incImportedRecords() {
        importedRecords++;
    }

    protected void reduceTagCount() {
        skippedTags++;
    }

    /**
     * Erstellt und initialiisert {@link SAXParser}
     * Falls keine xsdfile angegeben wurde, so wird nicht validiert
     *
     * @param handler
     * @return
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private SAXParser initParser(AbstractXMLHandler handler) throws SAXException, ParserConfigurationException {
        DWFile schemaFile = null;
        if (!StrUtils.isEmpty(xsdFile)) {
            schemaFile = DWFile.get(iPartsPlugin.XML_SCHEMA_PATH).getChild(xsdFile);
            if (!schemaFile.isFile() || !schemaFile.getExtension().equalsIgnoreCase("xsd")) {
                cancelImport(translateForLog("XML schema file is not valid or does not exist."), MessageLogType.tmlError);
                return null;
            }
            handler.setSchemaFile(schemaFile);
        }
        return XMLUtils.getStandardSaxParser(schemaFile);
    }

    @Override
    protected boolean isMQMessageValid(AbstractMQMessage mqMessage) {
        return (tableName != null) && super.isMQMessageValid(mqMessage);
    }

    @Override
    protected boolean importMasterDataFromMQMessage(AbstractMQMessage mqMessage) {
        return importMasterData(preparePushImporter(this, mqMessage));
    }

    /**
     * Importiert den vom Parser gepushten Datensatz
     *
     * @param reader
     * @param importRec
     * @param recordNo
     */
    public void importPushRecord(AbstractKeyValueRecordReader reader, Map<String, String> importRec, int recordNo) {
        if (Thread.currentThread().isInterrupted()) {
            cancelImport("!!Import-Thread wurde frühzeitig beendet");
        }
        if (cancelled) {
            return;
        }
        checkDataAtBeginningOfDataset(reader, recordNo);

        if (checkSkipRecord(reader, importRec)) {
            reduceTagCount();
            return;
        }

        if (!checkDatasetConsistency(reader, importRec)) {
            return;
        }
        //eigentlicher Import
        importRecord(importRec, recordNo);
        updateProgress(recordNo, -1);
    }

    /**
     * Extrahiert aus der übergebenen GZ Datei die enthaltenen XML Dateien. Typischer Aufbau solcher Archive: GZ -> tar -> XML/XSD.
     * Vorher wird geprüft, ob es sich um eine XML-GZ Datei handelt (aus RFTSx).
     *
     * @param gzTarArchive
     * @return
     */
    protected List<DWFile> extractXMLFilesFromGZArchive(DWFile gzTarArchive) {
        if ((gzTarArchive == null) || !MimeTypes.hasExtension(gzTarArchive, MimeTypes.EXTENSION_GZ)) {
            return null;
        }
        List<DWFile> xmlFiles;
        // Check, ob es sich um eine XML GZ Datei aus RFTSx handelt
        if (isXMLFileAsGZFile(gzTarArchive)) {
            xmlFiles = new ArrayList<>();
            xmlFiles.add(gzTarArchive);
            return xmlFiles;
        }
        try {
            xmlFiles = DWTarGzHelper.unpackAllTarEntries(gzTarArchive, DWFile.createTempDirectory("daim"), true);
            for (int i = xmlFiles.size() - 1; i >= 0; i--) {
                if (!isXMLFileAsGZFile(xmlFiles.get(i))) {
                    xmlFiles.remove(i);
                }
            }
            return xmlFiles;
        } catch (IOException e) {
            Logger.getLogger().handleRuntimeException(e);
            return null;
        }

    }

    /**
     * Check, ob es sich um eine XML GZ Datei aus RFTSx handelt
     *
     * @param gzFile
     * @return
     */
    private boolean isXMLFileAsGZFile(DWFile gzFile) {
        if ((gzFile == null) || !MimeTypes.hasExtension(gzFile, MimeTypes.EXTENSION_GZ)) {
            return false;
        }
        DWFile tempTestFile = DWFile.get(gzFile.getParentDWFile(), gzFile.extractFileName(false));
        return MimeTypes.hasExtension(tempTestFile, MimeTypes.EXTENSION_XML);

    }

    /**
     * Spezieller Fall für XML PUSH Importer (aktuell TAL95M und TAL47S).
     * <p>
     * Es können hier 3 Arten von Import Dateien kommen:
     * 1. manueller Import: TALXXX.tar.gz (GZ->TAR->XML/XSD)
     * 2. über RFTSx: eine GZ-XML Datei (GZ->XML) (GZ weil XML Dateien, die einen PUSH Importer brauchen sehr
     * groß sein können)
     * 3. die eigentliche XML Datei
     * <p>
     * Bei 1. und 2. wird davon ausgegangen, dass die Archive nur genau eine XML Datei enthalten (die eigentliche Importdatei)
     *
     * @param importFile
     * @return
     */
    protected boolean importXMLOrArchiveWithMultipleDifferentFiles(DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            List<DWFile> importFiles = extractXMLFilesFromGZArchive(importFile);
            if ((importFiles == null) || (importFiles.size() != 1)) {
                return false;
            }
            boolean res = importMasterData(preparePushImporter(this, importFiles.get(0), tableName));
            if (importFiles.get(0).getParentDWFile() != null) {
                importFiles.get(0).getParentDWFile().deleteDirContentRecursivelyWithRepeat();
                importFiles.get(0).getParentDWFile().deleteRecursivelyWithRepeat();
            }
            return res;
        } else if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_XML)) {
            return importMasterData(preparePushImporter(this, importFile, tableName));
        }
        return false;
    }
}
