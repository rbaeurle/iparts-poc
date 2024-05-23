/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Superklasse, für alle DIALOG XML Importer (Urladung oder Änderungsdienst)
 */
public abstract class AbstractDIALOGDataImporter extends AbstractDataImporter implements iPartsConst {

    // Die Mindestlänge von BCTE_RAS, damit daraus HM-M-SM extrahiert werden kann.
    private static final Integer BCTE_RAS_LEN = 6;
    protected static final String TABLE_NAME_PREFIX = "T10R";

    private Set<String> invalidSeriesSet;
    private Set<iPartsMQChannelTypeNames> deactivatedChannels;
    private MQChannelType channelType;

    public AbstractDIALOGDataImporter(EtkProject project, String importName, boolean withHeader, FilesImporterFileListType... importFileTypes) {
        super(project, importName, withHeader, importFileTypes);
    }

    public AbstractDIALOGDataImporter(EtkProject project, String importName, FilesImporterFileListType... importFileTypes) {
        this(project, importName, true, importFileTypes);
    }


    /**
     * Handelt es sich um einen DIALOG Änderungs-Importer
     *
     * @return
     */
    protected boolean isDIALOGDeltaDataImport() {
        boolean result = ((getChannelType() != null) && getChannelType().getChannelName().isDeltaChannel());
        // Eigentlich wird der Kanal beim Import gesetzt. Zur Sicherheit wird hier die MQ Nachricht direkt abgefragt
        if (!result) {
            iPartsMQChannelTypeNames mqChannelNameFromMessage = getMQChannelNameFromMessage();
            if (mqChannelNameFromMessage != null) {
                result = mqChannelNameFromMessage.isDeltaChannel();
            }
        }
        return result;

    }

    /**
     * Handelt es sich um einen DIALOG Urladungs-Importer
     *
     * @return
     */
    public boolean isDIALOGInitialDataImport() {
        boolean result = ((getChannelType() != null) && getChannelType().getChannelName().isInitialChannel());
        // Eigentlich wird der Kanal beim Import gesetzt. Zur Sicherheit wird hier die MQ Nachricht direkt abgefragt
        if (!result) {
            iPartsMQChannelTypeNames mqChannelNameFromMessage = getMQChannelNameFromMessage();
            if (mqChannelNameFromMessage != null) {
                result = mqChannelNameFromMessage.isInitialChannel();
            }
        }
        return result;
    }

    /**
     * Check, ob der DIALOG Import bezüglich seinen Meta-Daten valide ist
     *
     * @param importRecord
     * @return
     */
    public boolean checkIfValidDIALOGDataImport(Map<String, String> importRecord, int recordNo) {
        boolean isInitDataImport = isDIALOGInitialDataImport();
        if (isInitDataImport && DIALOGImportHelper.isDatasetMarkedForDeletion(importRecord)) {
            getMessageLog().fireMessage(translateForLog("!!Datensatz \"%1\" enthält ein " +
                                                        "unzulässiges Löschkennzeichen. Löschkennzeichen" +
                                                        " dürfen in der Urladung nicht vorkommen! Datensatz " +
                                                        "wird übersprungen", String.valueOf(recordNo)),
                                        MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            return false;
        }
        return true;
    }

    @Override
    protected boolean skipRecord(AbstractKeyValueRecordReader importer, Map<String, String> importRec) {
        if (!checkIfValidDIALOGDataImport(importRec, importer.getRecordNo())) {
            return true;
        }
        return super.skipRecord(importer, importRec);
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        invalidSeriesSet = new HashSet<>();

        // Für DIALOG-Urladungsimporter die Konfigurationseinstellung für "Caches löschen" explizit setzen.
        // In allen anderen Fällen werden die Caches nach einer gewissen Zeit gelöscht (AbstractGenericImporter.finishImport())
        if (isDIALOGInitialDataImport()) {
            setClearCachesAfterImport(iPartsImportPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsImportPlugin.CONFIG_DIALOG_INITIAL_IMPORT_CLEAR_CACHES));
        }
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        invalidSeriesSet = null;
    }

    public Set<String> getInvalidSeriesSet() {
        return invalidSeriesSet;
    }

    /**
     * Überprüft, ob der übergebene String mit den Spezialfällen für das Kennzeichen 'letzter Stand' übereinstimmt.
     *
     * @param dateTime
     * @return
     */
    public boolean isFinalStateDateTime(String dateTime) {
        iPartsDialogDateTimeHandler dtHandler = new iPartsDialogDateTimeHandler(dateTime);
        return dtHandler.isFinalStateDateTime();
    }

    @Override
    protected boolean importMasterDataFromMQMessage(AbstractMQMessage mqMessage) {
        if (isChannelDeactivated()) {
            iPartsMQChannelTypeNames channel = getMQChannelNameFromMessage();
            String channelName = (channel != null) ? channel.getTypeName() : iPartsMQChannelTypeNames.UNKNOWN.getTypeName();
            getMessageLog().fireMessage(translateForLog("!!Importer für den Kanal \"%1\" deaktiviert", channelName),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return true;
        }
        return super.importMasterDataFromMQMessage(mqMessage);
    }

    /**
     * Liefert zurück, ob der Kanal, über den die aktuelle Nachricht zum Importer kam, für den Import deaktiviert wurde.
     *
     * @return
     */
    private boolean isChannelDeactivated() {
        if ((deactivatedChannels == null) || deactivatedChannels.isEmpty()) {
            return false;
        }
        for (iPartsMQChannelTypeNames channelTypeName : deactivatedChannels) {
            if (isImporterForMQChannel(channelTypeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected DWFile initMQImportLog(AbstractMQMessage mqMessage, DWFile logFile) {
        // Wenn mit einer MQ Message der Importer initialisiert wird, muss gleich der Kanal gesetzt werden
        DWFile result = super.initMQImportLog(mqMessage, logFile);
        if (mqMessage != null) {
            MQChannelType mqChannelType = mqMessage.getMQChannelType();
            if (mqChannelType != null) {
                setChannelType(mqChannelType);
            }
        }
        return result;
    }

    /**
     * Fügt MQ Kanäle hinzu, für die der Import deaktiviert werden soll
     *
     * @param channelTypeNames
     */
    protected void addDeactivatedChannels(iPartsMQChannelTypeNames... channelTypeNames) {
        if ((channelTypeNames != null) && (channelTypeNames.length > 0)) {
            if (deactivatedChannels == null) {
                deactivatedChannels = new HashSet<>();
            }
            deactivatedChannels.addAll(Arrays.asList(channelTypeNames));
        }
    }

    /**
     * Check, ob die DIALOG-spezifische HM-M-SM Angabe gültig ist (RAS = 201012 = HM 20, M 10, SM 12). Viele Importer
     * halten diese Information in einem XML ELement. Bevor wir die Bestandtteile extrahieren können, muss erst der
     * Weret gültig sein.
     *
     * @param importer
     * @param importRec
     * @param seriesHmMSmFieldName
     * @param errors
     * @return
     */
    protected boolean isDIALOGSpecificHmMSmValueValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec,
                                                      String seriesHmMSmFieldName, List<String> errors) {
        boolean errorOccured = false;
        if (!importer.isRecordValid(importRec, errors)) {
            errorOccured = true;
        }

        // Prüfung, ob das Raster, aus dem später HM-M-SM extrahiert werden, auch wirklich lang genug ist.
        String value = importRec.get(seriesHmMSmFieldName);
        if ((value == null) || (value.length() < BCTE_RAS_LEN)) {
            errors.add(translateForLog("!!Das Feld %1 aus dem Datensatz %2 ist zu kurz. %3 statt %4 Stellen:",
                                       seriesHmMSmFieldName,
                                       Integer.toString(importer.getRecordNo()),
                                       (value == null) ? "0" : Integer.toString(value.length()),
                                       Integer.toString(BCTE_RAS_LEN)));
            errorOccured = true;
        }

        return !errorOccured;
    }

    public MQChannelType getChannelType() {
        return channelType;
    }

    public void setChannelType(MQChannelType channelType) {
        this.channelType = channelType;
    }

    @Override
    protected AbstractKeyValueRecordReader prepareImporterXML(DWFile xmlImportFile, MQChannelType channelType) {
        // Importer über den Menüpunkt sollen immer als Delta Importer starten. Hier gleich den Kanaltyp setzen
        setChannelType(iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_DELTA_IMPORT);
        return super.prepareImporterXML(xmlImportFile, channelType);
    }
}
