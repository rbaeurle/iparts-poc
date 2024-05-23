/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWInputStream;

import java.util.List;
import java.util.Map;

/**
 * Abstrakte Klasse für Importer, die JSON Dateien importieren
 */
public abstract class AbstractJSONDataImporter extends AbstractDataImporter implements iPartsConst {

    private boolean doBufferedSave = true;

    public AbstractJSONDataImporter(EtkProject project, String importName, FilesImporterFileListType fileListType) {
        super(project, importName, fileListType);
        setBufferedSave(true);
    }

    public AbstractJSONDataImporter(EtkProject project, String importName, String tableName) {
        super(project, importName, new FilesImporterFileListType(tableName, importName, true,
                                                                 false, false,
                                                                 new String[]{ MimeTypes.EXTENSION_JSON }));
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferedSave);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        // nichts zu tun, hier kommt man nie hin.
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        // nichts zu tun, hier kommt man nie hin.
        return false;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec,
                                          List<String> errors) {
        // nichts zu tun, hier kommt man nie hin.
        return false;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // nichts zu tun, hier kommt man nie hin.
    }

    /********************* Standard-Logfile-Ausgabe-Hilfs-Funktionen *********************/


    protected void fireMessage(String translationsKey, String... placeHolderTexts) {
        doFireMessage(MessageLogType.tmlMessage, false, translationsKey, placeHolderTexts);
    }

    protected void fireMessageLF(String translationsKey, String... placeHolderTexts) {
        doFireMessage(MessageLogType.tmlMessage, true, translationsKey, placeHolderTexts);
    }

    protected void fireMessageLFnoDate(String translationsKey, String... placeHolderTexts) {
        getMessageLog().fireMessage(translateForLog(translationsKey, placeHolderTexts),
                                    MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
    }

    protected void fireWarning(String translationsKey, String... placeHolderTexts) {
        doFireMessage(MessageLogType.tmlWarning, false, translationsKey, placeHolderTexts);
    }

    protected void fireWarningLF(String translationsKey, String... placeHolderTexts) {
        doFireMessage(MessageLogType.tmlWarning, true, translationsKey, placeHolderTexts);
    }

    protected void fireError(String translationsKey, String... placeHolderTexts) {
        doFireMessage(MessageLogType.tmlError, false, translationsKey, placeHolderTexts);
    }

    protected void fireErrorLF(String translationsKey, String... placeHolderTexts) {
        doFireMessage(MessageLogType.tmlError, true, translationsKey, placeHolderTexts);
    }

    protected void doFireMessage(MessageLogType logType, boolean writeOnlyLogFile, String translationsKey,
                                 String... placeHolderTexts) {
        if (writeOnlyLogFile) {
            getMessageLog().fireMessage(translateForLog(translationsKey, placeHolderTexts),
                                        logType, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
        } else {
            getMessageLog().fireMessage(translateForLog(translationsKey, placeHolderTexts),
                                        logType, MessageLogOption.TIME_STAMP);
        }
    }

    protected void fireProgress(int pos, int maxPos) {
        getMessageLog().fireProgress(pos, maxPos, "", true, true);
    }


    /**
     * Eigene Routine zum Einlesen der Datei mit Ausgabe der Dauer.
     * Hat am Anfang ziemlich lange gedauert, nun nicht mehr, nur die Ausgabe der Dauer ist drin geblieben.
     *
     * @param genson
     * @param inputStream
     * @return
     */
    protected <T> T deserializeFromInputStream(Genson genson, DWInputStream inputStream, String filename, Class<T> tClass) {
        long startTime = System.currentTimeMillis();

        // Hier passiert die Umwandlung der Importdaten im JSON-Format in die zu durchsuchende Struktur.
        T result = genson.deserialize(inputStream, tClass);

        String timeDurationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true,
                                                                       false, Language.DE.getCode());
        fireMessage("!!Deserialisierung der Importdaten in \"%1\" abgeschlossen in %2", filename, timeDurationString);
        return result;
    }


    /**
     * Eigene Routine zum Einlesen der Datei mit Ausgabe der Dauer.
     * Hat am Anfang ziemlich lange gedauert, nun nicht mehr, nur die Ausgabe der Dauer ist drin geblieben.
     *
     * @param genson
     * @param response
     * @return
     */
    protected <T> T deserializeFromString(Genson genson, String response, String filename, Class<T> tClass) {
        long startTime = System.currentTimeMillis();

        // Hier passiert die Umwandlung der Importdaten im JSON-Format in die zu durchsuchende Struktur.
        T result = genson.deserialize(response, tClass);

        String timeDurationString = DateUtils.formatTimeDurationString(System.currentTimeMillis() - startTime, true,
                                                                       false, Language.DE.getCode());
        fireMessage("!!Deserialisierung der Importdaten in \"%1\" abgeschlossen in %2", filename, timeDurationString);
        return result;
    }
}
