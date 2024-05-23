/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.ArrayFileImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLMixedTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTableDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus.iPartsXMLPrimusDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.primus.iPartsXMLSRMDataset;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWInputStream;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * KeyValueReader für MQ-Message und MQ-XML-Datei
 */
public class iPartsKeyValueRecordReader extends AbstractKeyValueRecordReader {

    public enum RECORD_IMPORTER_TYPE {MQFile_Importer, MQ_Importer, BIG_DATA_XML, unknown}

    private RECORD_IMPORTER_TYPE importType = RECORD_IMPORTER_TYPE.unknown;

    // je nach Inhalt von mqMessage ist entweder xmlTable null und xmlMixedTable not null oder umgekehrt
    private AbstractMQMessage mqMessage;
    private iPartsXMLTable xmlTable;
    private iPartsXMLMixedTable xmlMixedTable;

    private MQChannelType channelType;
    private String currentTableName;
    protected int currentTableNamesIndex = 0;

    /**
     * Constructor für MQ File Importer
     *
     * @param xmlFile != null => XML-Datei Lesen und MQ-Empfang simulieren
     */
    public iPartsKeyValueRecordReader(DWFile xmlFile, MQChannelType channelType) {
        super(xmlFile, null);
        if (inputFile != null) {
            this.importType = RECORD_IMPORTER_TYPE.MQFile_Importer;
            this.channelType = channelType;
        }
    }

    /**
     * Constructor für BIG DATA XML Importer
     *
     * @param xmlFile
     */
    public iPartsKeyValueRecordReader(DWFile xmlFile, String tablename) {
        super(xmlFile, tablename);
        if (inputFile != null) {
            this.importType = RECORD_IMPORTER_TYPE.BIG_DATA_XML;
        }
    }

    /**
     * Constructor für MQ Importer
     *
     * @param mqMessage != null => direkt aus der MQ-Queue
     */
    public iPartsKeyValueRecordReader(AbstractMQMessage mqMessage) {
        super(null, null);
        if (mqMessage != null) {
            this.mqMessage = mqMessage;
            this.importType = RECORD_IMPORTER_TYPE.MQ_Importer;
            this.tableNames = new DwList<String>();
        }
    }

    /**
     * Abfrage, ob der Importer initialisiert ist (open)
     *
     * @return
     */
    @Override
    public boolean isInit() {
        switch (importType) {
            case MQ_Importer:
                return (xmlTable != null) || (xmlMixedTable != null);
        }
        return false;
    }

    /**
     * liefert den Inputnamen (für LogDatei)
     *
     * @param logLanguage
     * @return
     */
    @Override
    public String getImportName(String logLanguage) {
        switch (importType) {
            case MQFile_Importer:
            case BIG_DATA_XML:
                return inputFile.getName();
            case MQ_Importer:
                if (isInit() && (currentTableName != null)) {
                    return currentTableName;
                } else if (mqMessage != null) {
                    return TranslationHandler.translateForLanguage("!!vom MQ-Kanal \"%1\"", logLanguage,
                                                                   mqMessage.getMQChannelType().getChannelName().getTypeName());
                }
                break;
        }
        return "No import name for import type!";
    }

    @Override
    public String getCurrentTableName() {
        return currentTableName;
    }

    /**
     * ist MQ-Nachricht eine Mixed-Table?
     *
     * @return
     */
    @Override
    public boolean isXMLMixedTable() {
        return xmlMixedTable != null;
    }

    /**
     * Öffnen (Initialisieren) des Record-Importers
     * Beim FileImporter wird die Datei geöffnet
     * Beim MQ-File-Importer wird die XML-Datei gelesen und eine MQMessage gebildet
     * Beim MQ-Importer wird die MQMessage analysiert
     *
     * @return
     * @throws java.io.IOException, SAXException
     */
    @Override
    public boolean open() throws IOException, SAXException {
        boolean result = false;
        currentTableNamesIndex = 0;
        switch (importType) {
            case MQFile_Importer:
                result = loadMQMessage(channelType);
                break;
            case MQ_Importer:
                result = checkMQType();
                break;
            case BIG_DATA_XML:
                // todo: Validieren!
                result = inputFile.exists();
                break;
        }
        return result;
    }


    @Override
    public int getRecordCount() {
        if (isInit()) {
            switch (importType) {
                case MQ_Importer:
                    if (xmlTable != null) {
                        return xmlTable.getNumberOfDatasets();
                    } else {
                        int count = 0;
                        for (String table : xmlMixedTable.getTableNames()) {
                            count += xmlMixedTable.getNumberOfDatasetsForTable(table);
                        }
                        return count;
                    }
            }
        }
        return super.getRecordCount();
    }

    @Override
    public Map<String, String> getNextRecord() {
        return getNextRecord(currentTableName);
    }

    /**
     * Nur für MQ Mixed-Table!
     * Liefert den nächsten Record für eine Table oder null, falls keiner mehr vorhanden ist
     *
     * @param tableName
     * @return
     */
    @Override
    public Map<String, String> getNextRecord(String tableName) {
        if (isInit()) {
            switch (importType) {
                case MQ_Importer:
                    return internalGetNextMQRecord(tableName);
            }
        }
        return null;
    }

    @Override
    public boolean saveFile(DWFile dir, String prefix) {
        if (dir != null) {
            if (!dir.exists()) {
                dir.mkDirsWithRepeat();
            }
            if ((mqMessage != null) && (mqMessage.getFileContent() != null)) {
                return mqMessage.saveToFile(dir, prefix);
            } else if (inputFile != null) {
                ArrayFileImporter.saveSourceFile(inputFile, dir, prefix);
            }
        }
        return false;
    }

    @Override
    public void close() {

    }

    /* Routinen für MQ */

    /**
     * aus der XML-Datei eine MQMessage erzeugen
     *
     * @param channelType
     * @return
     * @throws IOException, SAXException
     */
    private boolean loadMQMessage(MQChannelType channelType) throws IOException, SAXException {
        boolean result;
        try {
            DwXmlFile xmlFile = new DwXmlFile(inputFile);
            mqMessage = XMLImportExportHelper.buildMessageFromXMLFile(xmlFile, channelType, true, false);
            if (mqMessage != null) {
                result = checkMQType();
                if (result) {
                    importType = RECORD_IMPORTER_TYPE.MQ_Importer;
                }
            } else {
                importType = RECORD_IMPORTER_TYPE.unknown;
                result = false;
            }
        } catch (IOException e) {
            importType = RECORD_IMPORTER_TYPE.unknown;
            throw e;
        } catch (SAXException e) {
            importType = RECORD_IMPORTER_TYPE.unknown;
            throw e;
        }
        return result;
    }

    /**
     * Überprüfung, ob es sich um eine iPartsXMLTable oder iPartsXMLMixedTable handelt
     *
     * @return
     */
    private boolean checkMQType() {
        boolean result = false;
        if (mqMessage.isOfType(iPartsXMLTable.TYPE)) {
            xmlTable = (iPartsXMLTable)mqMessage;
            xmlMixedTable = null;
            tableNames.add(xmlTable.getTableName());
            currentTableName = xmlTable.getTableName();
            result = true;
        } else if (mqMessage.isOfType(iPartsXMLMixedTable.TYPE)) {
            xmlTable = null;
            xmlMixedTable = (iPartsXMLMixedTable)mqMessage;
            tableNames.addAll(xmlMixedTable.getTableNames());
            if (!getTableNames().isEmpty()) {
                currentTableName = getTableNames().get(0);
            }
            result = true;
        } else if (mqMessage.isOfType(iPartsXMLPrimusDataset.TYPE) || mqMessage.isOfType(iPartsXMLSRMDataset.TYPE)) {
            result = true;
        } else {
            mqMessage = null;
            importType = RECORD_IMPORTER_TYPE.unknown;
        }
        return result;
    }

    /**
     * nächsten Record aus der MQMessage bilden
     *
     * @param tableName
     * @return
     */
    private Map<String, String> internalGetNextMQRecord(String tableName) {
        if ((currentTableName == null) || !currentTableName.equals(tableName)) {
            currentTableName = tableName;
            recordNo = 0;
        }
        Map<String, String> result = null;
        if (xmlTable != null) {
            iPartsXMLTableDataset dataSet = null;
            if (recordNo < xmlTable.getNumberOfDatasets()) {
                dataSet = xmlTable.getDatasets().get(recordNo);
            }
            recordNo++;
            if (dataSet != null) {
                result = convertToKeyValue(dataSet);
            }
        } else if (xmlMixedTable != null) {
            iPartsXMLTableDataset dataSet = null;
            if (recordNo < xmlMixedTable.getNumberOfDatasetsForTable(tableName)) {
                dataSet = xmlMixedTable.getDatasetForTableName(tableName).get(recordNo);
            }
            recordNo++;
            if (dataSet != null) {
                result = convertToKeyValue(dataSet);
            } else {
                // Record aus der nächsten Tabelle zurückliefern (sofern vorhanden)
                currentTableNamesIndex++;
                if (currentTableNamesIndex < tableNames.size()) {
                    return internalGetNextMQRecord(tableNames.get(currentTableNamesIndex));
                }
            }
        }
        return result;
    }

    /**
     * MQ-Daten nach Map<String, String> konvertieren
     *
     * @param dataSet
     * @return
     */
    private Map<String, String> convertToKeyValue(iPartsXMLTableDataset dataSet) {
        Set<String> tags = dataSet.getTags();
        Map<String, String> result = new TreeMap<String, String>();
        for (String tag : tags) {
            String value = dataSet.getValueForTag(tag);
            result.put(tag, value);
            if (value.equals(iPartsXMLTableDataset.SUB_DATASETS)) {
                result.putAll(convertToKeyValue(dataSet.getSubDataset(tag)));
            }
        }
        boolean isDialogChannel = false;
        if (channelType != null) {
            isDialogChannel = channelType.getChannelName().isDialogXMLChannel();
        } else if ((mqMessage != null) && (mqMessage.getMQChannelType() != null)) {
            isDialogChannel = mqMessage.getMQChannelType().getChannelName().isDialogXMLChannel();
        }
        if (isDialogChannel) {
            // Jeder Datensatz in einer DIALOG MQ Datei hat Pflichtattribute am Haupt-XML-Element. Diese werden ebenfalls
            // als Key-Value Paare im ImportRecord übergeben
            result.put(iPartsTransferConst.ATTR_TABLE_SEQUENCE_NO, dataSet.getSeqNo());
            result.put(iPartsTransferConst.ATTR_TABLE_KEM, dataSet.getKem());
            result.put(iPartsTransferConst.ATTR_TABLE_SDB_FLAG, dataSet.getSdbValue().getOriginalValue());
        }
        return result;
    }

    @Override
    public DWInputStream getContentAsInputStream() {
        if ((mqMessage != null) && (mqMessage.getFileContent() != null)) {
            return new DWInputStream(new ByteArrayInputStream(mqMessage.getFileContent().getBytes(StandardCharsets.UTF_8)));
        }
        return super.getContentAsInputStream();
    }

}
