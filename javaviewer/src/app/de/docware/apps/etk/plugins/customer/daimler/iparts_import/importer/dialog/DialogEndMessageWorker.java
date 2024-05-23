/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.ImportFactoryDataAutomationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.BigDataXMLHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsKeyValueRecordReader;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Klasse zum Verarbeiten und loggen von DIALOG Endenachrichten
 */
public class DialogEndMessageWorker extends AbstractSAXPushDataImporter implements iPartsConst {

    public static final String XML_TABLENAME = "ENDESATZ";
    public static final String DIALOG_TABLENAME = "ENDE";

    public static final String DIALOG_DIRECT_ANZAHL = "ENDE_ANZAHL";
    public static final String DIALOG_DIRECT_DATUM = "ENDE_DATUM";

    private static final String ELEMENT_KEY_DATASET = "Dataset";
    private static final String ELEMENT_KEY_RECORD_TYPE = "RecordType";
    private static final String ELEMENT_KEY_RECORD_TYPE_DATASET_TYPE_ATTRIBUTE = "datasettype";
    private static final String ELEMENT_KEY_TOTAL_NUMBER_XML_MESSAGES = "TotalNumOfXMLMessages";
    private static final String ELEMENT_KEY_TOTAL_NUMBER_RECORDS = "TotalNumOfDataRecords";
    private static final String ELEMENT_KEY_NUMBER_XML_MESSAGES = "NumOfXMLMessages";
    private static final String ELEMENT_KEY_NUMBER_DATA_RECORDS = "NumOfDataRecords";
    private static final Set<String> UNNECESSARY_ATTRIBUTES = new HashSet<>();

    static {
        UNNECESSARY_ATTRIBUTES.add("xmlns:xsi");
        UNNECESSARY_ATTRIBUTES.add("xsi:noNamespaceSchemaLocation");
        UNNECESSARY_ATTRIBUTES.add("name");
    }

    private List<RecordType> recordTypes;
    private int totalNumberXMLMessages;
    private int totalNumberDataRecords;

    public DialogEndMessageWorker(EtkProject project) {
        super(project, DIALOG_END_MESSAGE, null,
              new FilesImporterFileListType(XML_TABLENAME, DIALOG_END_MESSAGE, true,
                                            false, false,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
        this.tableName = XML_TABLENAME;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        recordTypes = new ArrayList<>();
    }

    @Override
    protected void postImportTask() {
        processCollectedData(isImporterForMQChannel(iPartsMQChannelTypeNames.DIALOG_IMPORT));
        super.postImportTask();
    }

    /**
     * Verarbeitet die Informationen, die via MQ aufgesammelt wurden
     *
     * @param isInitialImport
     */
    private void processCollectedData(boolean isInitialImport) {
        if (!isCancelled()) {
            getMessageLog().fireMessage(translateForLog("!!Inhalt der gesamten Endenachricht: %1" +
                                                        " Nachrichten und %2 Datensätze",
                                                        String.valueOf(totalNumberXMLMessages),
                                                        String.valueOf(totalNumberDataRecords)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            for (RecordType recordType : recordTypes) {
                int numberXMLMessages = recordType.getNumberXMLMessages();
                int numberDataRecords = recordType.getNumberDataRecords();
                getMessageLog().fireMessage(translateForLog("!!Tabellenname: %1 - %2 XML-%3 - %4 %5",
                                                            recordType.getDatasetType(),
                                                            String.valueOf(numberXMLMessages),
                                                            (numberXMLMessages == 1) ? "Nachricht" : "Nachrichten",
                                                            String.valueOf(numberDataRecords),
                                                            (numberDataRecords == 1) ? "Datensatz" : "Datensätze"),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }

            // Bei DIALOG Delta Import die automatische Freigabe der Werksdaten starten
            if (isInitialImport) {
                // Für DIALOG-Urladungsimporter die Konfigurationseinstellung für "Caches löschen" explizit setzen.
                // In allen anderen Fällen werden die Caches nach einer gewissen Zeit gelöscht (AbstractGenericImporter.finishImport())
                setClearCachesAfterImport(iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_DIALOG_INITIAL_IMPORT_CLEAR_CACHES));
            } else if (iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_IMPORT_EXECUTE_AUTORELEASE)) {
                setClearCachesAfterImport(false); // Caches sollen erst geleert werden nachdem der AutoRelease gelaufen ist
                final EtkMessageLog messageLog = new EtkMessageLog();
                final DWFile logFile = iPartsJobsManager.getInstance().addDefaultLogFileToMessageLog(messageLog, translateForLog(ImportFactoryDataAutomationHelper.TITLE),
                                                                                                     iPartsPlugin.LOG_CHANNEL_MQ);
                Session.startChildThreadInSession(
                        thread -> {
                            ImportFactoryDataAutomationHelper.autoReleaseFactoryDataWithLogFile(getProject(), messageLog);
                            clearCaches();
                            iPartsJobsManager.getInstance().jobProcessed(logFile);
                        }
                );
            }
        }
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
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
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(new EndMessageHandler(this, new iPartsKeyValueRecordReader(importFile, tableName)));
        }
        return false;
    }

    @Override
    protected BigDataXMLHandler preparePushImporter(AbstractSAXPushDataImporter importer, AbstractMQMessage mqMessage) {
        return new EndMessageHandler(importer, new iPartsKeyValueRecordReader(mqMessage));
    }

    public boolean handleEndMessageFromMQ(AbstractMQMessage xmlMQMessage) {
        return startImportFromMQMessage(xmlMQMessage);
    }

    /**
     * Verarbeitet eine ENDE Nachricht, die via Direktanbindung an DIALOG gekommen ist
     *
     * @param importData
     * @param channelType
     * @return
     */
    public boolean handleEndMessageFromDIALOGDirect(List<Map<String, String>> importData, MQChannelType channelType) {
        importJobRunning();
        if (!initImport(new EtkMessageLog())) {
            cancelImport(translateForLog("!!Das Initialisieren des Workers für den ENDE Datensatz \"%1\"" +
                                         " ist fehlgeschlagen.", getImportName(getProject().getDBLanguage())));
        }
        if (!isCancelled()) {
            setExternalImport(true);
            preImportTask();
            // Eigentlich kann es hie nur einen ENDE-Datensatz geben. Zur Sicherheit geben wir alle möglichen aus
            VarParam<Integer> allFiles = new VarParam<>(0);
            importData.forEach(importRec -> {
                RecordType recordType = new RecordType();
                recordType.setDatasetType(DIALOG_TABLENAME);
                int count = StrUtils.strToIntDef(importRec.get(DIALOG_DIRECT_ANZAHL), 0);
                recordType.setNumberDataRecords(count);
                recordType.setNumberXMLMessages(count);
                allFiles.setValue(allFiles.getValue() + count);
                recordTypes.add(recordType);
            });
            totalNumberDataRecords = allFiles.getValue();
            totalNumberXMLMessages = allFiles.getValue();

            processCollectedData(channelType.getChannelName() == iPartsMQChannelTypeNames.DIALOG_DIRECT_IMPORT);
            super.postImportTask();
        }
        return finishImport() && !isCancelled();
    }

    public int getTotalNumberXMLMessages() {
        return totalNumberXMLMessages;
    }

    public int getTotalNumberDataRecords() {
        return totalNumberDataRecords;
    }

    public List<RecordType> getRecordTypes() {
        return recordTypes;
    }

    /**
     * Hilfsobjekt zum Halten von XML Datensätzen
     */
    private static class RecordType {

        private String datasetType;
        private int numberXMLMessages;
        private int numberDataRecords;

        public String getDatasetType() {
            return datasetType;
        }

        public void setDatasetType(String datasetType) {
            this.datasetType = datasetType;
        }

        public int getNumberXMLMessages() {
            return numberXMLMessages;
        }

        public void setNumberXMLMessages(int numberXMLMessages) {
            this.numberXMLMessages = numberXMLMessages;
        }

        public int getNumberDataRecords() {
            return numberDataRecords;
        }

        public void setNumberDataRecords(int numberDataRecords) {
            this.numberDataRecords = numberDataRecords;
        }
    }

    private class EndMessageHandler extends BigDataXMLHandler {

        private RecordType currentRecordType;

        public EndMessageHandler(AbstractSAXPushDataImporter importer, AbstractKeyValueRecordReader reader) {
            super(importer, reader);
        }

        @Override
        protected void onStartElement(String uri, String localName, String qName, Map<String, String> attributes) {
            switch (localName) {
                // Neues Datenobjekt anlegen
                case ELEMENT_KEY_RECORD_TYPE:
                    if (attributesLoaded(attributes) && attributes.containsKey(ELEMENT_KEY_RECORD_TYPE_DATASET_TYPE_ATTRIBUTE)) {
                        currentRecordType = new RecordType();
                        currentRecordType.setDatasetType(attributes.get(ELEMENT_KEY_RECORD_TYPE_DATASET_TYPE_ATTRIBUTE));
                    }
                    break;
                // Meta-Informationen aus dem "Dataset"-Element ausgeben
                case ELEMENT_KEY_DATASET:
                    if (attributesLoaded(attributes)) {
                        logMetaInfo(attributes);
                    }
                    break;
            }
        }

        @Override
        protected void onEndElement(String uri, String localName, String qName) {
            // Wenn ein RecordType Datensatz durch ist, diesen ablegen
            if (localName.equals(ELEMENT_KEY_RECORD_TYPE)) {
                if (currentRecordType != null) {
                    recordTypes.add(currentRecordType);
                    currentRecordType = null;
                }
            }
        }

        @Override
        protected void onTextElement(String tagName, char[] ch, int start, int length) {
            String value = new String(ch, start, length);
            switch (tagName) {
                // Anzahl alle XML Nachrichten über alle Tabellen hinweg
                case ELEMENT_KEY_TOTAL_NUMBER_XML_MESSAGES:
                    totalNumberXMLMessages = StrUtils.strToIntDef(value, -1);
                    break;
                // Anzahl alle Datensätze über alle Tabellen hinweg
                case ELEMENT_KEY_TOTAL_NUMBER_RECORDS:
                    totalNumberDataRecords = StrUtils.strToIntDef(value, -1);
                    break;
                // XML Nachrichten pro Tabelle
                case ELEMENT_KEY_NUMBER_XML_MESSAGES:
                    if (currentRecordType != null) {
                        currentRecordType.setNumberXMLMessages(StrUtils.strToIntDef(value, -1));
                    }
                    break;
                // Datensätze pro Tabelle
                case ELEMENT_KEY_NUMBER_DATA_RECORDS:
                    if (currentRecordType != null) {
                        currentRecordType.setNumberDataRecords(StrUtils.strToIntDef(value, -1));
                    }
                    break;
            }
        }
    }

    /**
     * Logt die Meta-Informationen aus dem Header-Element der XML Datei
     *
     * @param attributes
     */
    private void logMetaInfo(Map<String, String> attributes) {
        getMessageLog().fireMessage(translateForLog("!!Meta-Informationen zum Endedatensatz:"),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> attributeEntry : attributes.entrySet()) {
            if (UNNECESSARY_ATTRIBUTES.contains(attributeEntry.getKey())) {
                continue;
            }
            if (builder.length() != 0) {
                builder.append(" - ");
            }
            builder.append(attributeEntry.getKey());
            builder.append(": ");
            builder.append(attributeEntry.getValue());
        }
        getMessageLog().fireMessage(translateForLog("!!%1", builder.toString()),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

}
