/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.mailbox.iPartsMailboxHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsMailboxChangedEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.Map;

/**
 * Importer für DIALOG-Fußnoten. Daten werden nicht in der Datenbank gespeichert, sondern es wird pro Datensatz eine
 * Nachricht an Benutzer mit Rolle Admin gesendet. Nachrichten werden nur gesendet, wenn die Fußnote zur Fußnotennummer
 * nicht schon in der Datenbank vorhanden ist.
 * Mit den Informationen in der Nachricht kann dann über das Lexikon eine neue Fußnote angelegt werden.
 */
public class VFNDataImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    public static final String DIALOG_TABLENAME = "VFN";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    public static final String IMPORT_NAME = "!!DIALOG Fußnoten";

    public static final String VFN_FNNR = "VFN_FNNR";
    public static final String VFN_FN = "VFN_FN";
    public static final String VFN_DATUHR = "VFN_DATUHR";

    private final String[] mustHaveFields = new String[]{ VFN_FNNR, VFN_FN, VFN_DATUHR };

    // MailboxEvent, um nach dem Import über die neuen Fußnoten zu informieren
    private iPartsMailboxChangedEvent mailboxChangedEvent;

    public VFNDataImporter(EtkProject project) {
        super(project, IMPORT_NAME, new FilesImporterFileListType(DIALOG_TABLENAME, IMPORT_NAME, false, false, true,
                                                                  new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        mailboxChangedEvent = new iPartsMailboxChangedEvent(iPartsMailboxChangedEvent.MailboxItemState.NEW);
    }

    @Override
    protected boolean skipRecord(AbstractKeyValueRecordReader importer, Map<String, String> importRec) {
        // Überspringe den Datensatz, wenn er über den Urladungskanal kommt
        if (isDIALOGInitialDataImport()) {
            return true;
        }
        return super.skipRecord(importer, importRec);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        VFNDataImportHelper importHelper = new VFNDataImportHelper(getProject(), null, DIALOG_TABLENAME);
        String fnId = importHelper.handleValueOfSpecialField(VFN_FNNR, importRec.get(VFN_FNNR));

        iPartsFootNoteId footNoteId = new iPartsFootNoteId(fnId);
        iPartsDataFootNote footNote = new iPartsDataFootNote(getProject(), footNoteId);
        if (!footNote.existsInDB()) {
            // Informationen für die Nachricht sammeln
            String fnText = importRec.get(VFN_FN);
            String date = importRec.get(VFN_DATUHR);
            String formattedDateTime = XMLImportExportDateHelper.getDateStringFromXMLDateWithouSeconds(date);

            String languageDE = Language.DE.getCode();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(TranslationHandler.translateForLanguage("!!FN-Nummer:", languageDE)).append(" ").append(fnId).append("\n");
            stringBuilder.append(TranslationHandler.translateForLanguage("!!Fußnote:", languageDE)).append(" ").append(fnText).append("\n");
            stringBuilder.append(TranslationHandler.translateForLanguage("!!Datum:", languageDE)).append(" ").append(formattedDateTime).append("\n");

            String subject = TranslationHandler.translateForLanguage("!!Neue Fußnote", languageDE);

            // Nachricht erstellen
            iPartsMailboxChangedEvent event = iPartsMailboxHelper.createMessageForCreateDialogFootnote(getProject(), subject,
                                                                                                       stringBuilder.toString(),
                                                                                                       footNoteId);
            if (event != null) {
                mailboxChangedEvent.addAllRecipients(event.getMailboxItems());
            }
        } else {
            increaseSkippedRecord();
        }
    }

    @Override
    public boolean finishImport() {
        boolean finishImportResult = super.finishImport();

        //jetzt sind auch wirklich alle Daten in der Datenbank gelandet
        if (finishImportResult && !isCancelled()) {
            // wenn nicht abgebrochen wurde, wird jetzt der MailboxEvent verschickt
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(mailboxChangedEvent);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(DIALOG_TABLENAME)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustHaveData(mustHaveFields);
        importer.setMustExists(mustHaveFields);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }

    @Override
    protected void logImportRecordsFinished(int importRecordCount) {
        if (isDIALOGInitialDataImport()) {
            getMessageLog().fireMessage(translateForLog("!!VFN Verarbeitung ist für den Urladungskanal deaktiviert!"),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
        if (isSingleCall) {
            if (skippedRecords > 0) {
                int importRecordCountWithoutSkipped = Math.max(0, importRecordCount - skippedRecords);
                getMessageLog().fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                                            getDatasetTextForLog(importRecordCount)) +
                                            ", " + translateForLog("!!%1 %2 versendet", String.valueOf(importRecordCountWithoutSkipped),
                                                                   getMessageTextForLog(importRecordCountWithoutSkipped)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            } else {
                getMessageLog().fireMessage(translateForLog("!!%1 %2 versendet", String.valueOf(importRecordCount),
                                                            getMessageTextForLog(importRecordCount)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
        } else {
            getMessageLog().fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                                        getMessageTextForLog(importRecordCount)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    protected String getMessageTextForLog(int recordCount) {
        if (recordCount == 1) {
            return translateForLog("!!Nachricht");
        } else {
            return translateForLog("!!Nachrichten");
        }
    }

    private static class VFNDataImportHelper extends DIALOGImportHelper {

        public VFNDataImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            // Beginnt Fußnotennummer mit mehr als 2 Nullen wird die Nummer
            // ohne die führenden Nullen weitergereicht
            if (sourceField.equals(VFN_FNNR)) {
                if (value.startsWith("000")) {
                    value = StrUtils.removeLeadingCharsFromString(value.trim(), '0');
                }
            }
            return value;
        }
    }
}
