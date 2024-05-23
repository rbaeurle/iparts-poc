/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * MAD Fussnoten Texte Stammdaten Importer
 */
public class MADFootNoteTexteImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    private enum FOOTNOTE_TYPES {ELDAS, ELDAS_STAMM, DIALOG, DIALOG_STAMM, TABLE, UNKNOWN}

    final static String FUSS_FN_NR = "FUSS_FN_NR";        //Fussnoten Nummer (DIALOG)	"  T001" oder "000417"
    final static String FUSS_FN_FLG = "FUSS_FN_FLG";      //Folgenummer, wichtig bei mehrzeiligen DIALOG Tabellenfussnoten	1 ,2 ...
    final static String FUSS_FN_TXK = "FUSS_FN_TXK";      //kurze Fussnoten Texte
    final static String FUSS_TEXT_ID = "FUSS_TEXT_ID";    //Referenz auf das MAD Wörterbuch	735221
    final static String FUSS_STANDARD = "FUSS_STANDARD";  //Standard-Fussnote J/N
    final static String FUSS_EDAT = "FUSS_EDAT";          //Erstellungsdatum MAD	"2015-12-03-14.08.40.395746"
    final static String FUSS_ADAT = "FUSS_ADAT";          //Änderungsdatum MAD	"2015-12-03-14.08.40.395746"

    private String[] headerNames = new String[]{
            FUSS_FN_NR,
            FUSS_FN_FLG,
            FUSS_FN_TXK,
            FUSS_TEXT_ID,
            FUSS_STANDARD,
            FUSS_EDAT,
            FUSS_ADAT };

    private HashMap<String, String> mappingFootNoteData;
    private String[] primaryKeysFootNoteImport;
    private String tableName = "table";

    private Set<String> doneFootNoteIdList = new HashSet<String>();
    private Map<iPartsFootNoteId, FootNoteContainer> eldasFootNoteList = new HashMap<iPartsFootNoteId, FootNoteContainer>();
    private Map<iPartsFootNoteId, FootNoteContainer> dialogFootNoteList = new HashMap<iPartsFootNoteId, FootNoteContainer>();
    private List<TableFootNoteContainer> tableFootNoteList = new LinkedList<TableFootNoteContainer>();

    private TableFootNoteContainer tableFootNoteContainer;

    private boolean importToDB = true;
    private boolean doBufferSave = true;
    private boolean isNewImportStyle = false;
    private boolean handleSpecialFootNoteIds = false;

    public MADFootNoteTexteImporter(EtkProject project) {
        super(project, "MAD Fußnotentexte",
              new FilesImporterFileListType(TABLE_DA_KGTU_AS, "!!MAD Fußnoten Texte Stamm", true, false, false,
                                            new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_CSV,
                                                          MimeTypes.EXTENSION_ALL_FILES }));

        initMapping();
    }

    private void initMapping() {
        primaryKeysFootNoteImport = new String[]{ FUSS_FN_NR, FUSS_FN_FLG, FUSS_FN_TXK, FUSS_TEXT_ID, FUSS_STANDARD };
        mappingFootNoteData = new HashMap<String, String>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysFootNoteImport);
        importer.setMustHaveData(primaryKeysFootNoteImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   DictTextKindTypes.FOOTNOTE)) {
            return false;
        } else {
            iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.FOOTNOTE, TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT));
            if (!((txtKindId != null) && txtKindId.isValidId())) {
                getMessageLog().fireMessage(translateForLog("!!Fehlende Textart Zuordnung im Lexikon: %1",
                                                            translateForLog(DictTextKindTypes.FOOTNOTE.getTextKindName())),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                return false;
            }
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
    protected void preImportTask() {
        doneFootNoteIdList.clear();
        eldasFootNoteList.clear();
        dialogFootNoteList.clear();
        tableFootNoteList.clear();
        tableFootNoteContainer = null;
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        FootNoteImportHelper importHelper = new FootNoteImportHelper(getProject(), mappingFootNoteData, tableName);
        FootNoteContainer footNoteContainer = new FootNoteContainer();
        footNoteContainer.initByRecord(importHelper, importRec);
        if (footNoteContainer.footnoteType == FOOTNOTE_TYPES.UNKNOWN) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Fußnoten-ID \"%2\" übersprungen",
                                                        String.valueOf(recordNo), importHelper.handleValueOfSpecialField(FUSS_FN_NR, importRec)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP); //, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return;
//        } else if (footNoteContainer.fnType == FOOTNOTE_TYPE.DIALOG_STAMM) {
//            getMessageLog().fireMessage(translateForLog("!!Record %1 mit DIALOG Stamm Fußnoten-ID \"%2\" übersprungen (wird durch ELDAS ersetzt)",
//                                                        String.valueOf(recordNo), importHelper.handleValueOfSpecialField(FUSS_FN_NR, importRec)),
//                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
//            return;
        }

        if (doneFootNoteIdList.contains(footNoteContainer.footNoteId + "_" + footNoteContainer.footNoteLfdNr + '_' + footNoteContainer.footnoteType.name())) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit Fußnoten-ID \"%2\" ist doppelt und wird übersprungen",
                                                        String.valueOf(recordNo), footNoteContainer.footNoteId.getFootNoteId()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP); //, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
            return;
        }
        if (footNoteContainer.footnoteType == FOOTNOTE_TYPES.TABLE) {
            if (tableFootNoteContainer == null) {
                tableFootNoteContainer = new TableFootNoteContainer();
                tableFootNoteContainer.setFootNoteId(footNoteContainer.footNoteId);
                tableFootNoteList.add(tableFootNoteContainer);
            } else {
                if (!tableFootNoteContainer.footNoteId.equals(footNoteContainer.footNoteId)) {
                    tableFootNoteContainer = new TableFootNoteContainer();
                    tableFootNoteContainer.setFootNoteId(footNoteContainer.footNoteId);
                    tableFootNoteList.add(tableFootNoteContainer);
                }
            }
            tableFootNoteContainer.addFootNoteContainer(footNoteContainer);
        } else {
            if ((footNoteContainer.footnoteType == FOOTNOTE_TYPES.ELDAS) || (footNoteContainer.footnoteType == FOOTNOTE_TYPES.ELDAS_STAMM)) {
                footNoteContainer.loadDataFromDB();
                eldasFootNoteList.put(footNoteContainer.footNoteId, footNoteContainer);
            } else {
                footNoteContainer.loadDataFromDB();
                dialogFootNoteList.put(footNoteContainer.footNoteId, footNoteContainer);
            }
            tableFootNoteContainer = null;
        }

        doneFootNoteIdList.add(footNoteContainer.footNoteId + "_" + footNoteContainer.footNoteLfdNr + '_' + footNoteContainer.footnoteType.name());
    }

    @Override
    protected void postImportTask() {
        doneFootNoteIdList = null;
        //Schritt 1: alle DIALOG-Stamm FN entfernen
//        for (Map.Entry<iPartsFootNoteId, FootNoteContainer> fnEntry : eldasFNList.entrySet()) {
//            FootNoteContainer dialogContainer = dialogFNList.get(fnEntry.getKey());
//            if (dialogContainer != null) {
//                getMessageLog().fireMessage(translateForLog("!!DIALOG-Fussnote \"%1\" entfernt, da ELDAS-Standard Fussnote vorhanden",
//                                                            fnEntry.getKey().getFootNoteId()),
//                                            MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
//                reduceRecordCount();
//                dialogFNList.remove(fnEntry.getKey());
//            }
//        }


        if (!isCancelled()) {

            //Schritt 2: Suche nach TextIds und Speichern
            DictImportTextIdHelper importTextIDHelper = new DictImportTextIdHelper(getProject());
            int overAllCount = eldasFootNoteList.size() + dialogFootNoteList.size();
            for (TableFootNoteContainer tableEntry : tableFootNoteList) {
                overAllCount += tableEntry.dataFootNote.getFootNoteList().size();
            }
            int recordCount = 0;

            getMessageLog().fireMessage(translateForLog("!!Speichere ELDAS-Standard Fußnoten (%1)", String.valueOf(eldasFootNoteList.size())),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            //Schritt 2a: erstmal ELDAS
            for (Map.Entry<iPartsFootNoteId, FootNoteContainer> entry : eldasFootNoteList.entrySet()) {
                if (Thread.currentThread().isInterrupted()) {
                    cancelImport("!!Import-Thread wurde frühzeitig beendet");
                    return;
                }
                if (!calculateAndSave(importTextIDHelper, entry)) {
                    return;
                }
                updateProgress(++recordCount, overAllCount);
            }

            getMessageLog().fireMessage(translateForLog("!!Speichere DIALOG-Fußnoten (%1)", String.valueOf(dialogFootNoteList.size())),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            //Schritt 2b: dann DIALOG
            for (Map.Entry<iPartsFootNoteId, FootNoteContainer> footNoteEntry : dialogFootNoteList.entrySet()) {
                if (Thread.currentThread().isInterrupted()) {
                    cancelImport("!!Import-Thread wurde frühzeitig beendet");
                    return;
                }
                if (!calculateAndSave(importTextIDHelper, footNoteEntry)) {
                    return;
                }
                updateProgress(++recordCount, overAllCount);
            }

            getMessageLog().fireMessage(translateForLog("!!Speichere Tabellen-Fußnoten (%1)", String.valueOf(tableFootNoteList.size())),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            //Schritt 2c: dann Tabellen-Fussnoten
            for (TableFootNoteContainer tableEntry : tableFootNoteList) {
                for (iPartsDataFootNoteContent dataFootNoteContent : tableEntry.dataFootNote.getFootNoteList()) {
                    if (Thread.currentThread().isInterrupted()) {
                        cancelImport("!!Import-Thread wurde frühzeitig beendet");
                        return;
                    }
                    EtkMultiSprache multiEdit = dataFootNoteContent.getMultiText();
                    // Dictionary Eintrag anlegen bzw. aktualisieren
                    boolean dictSuccessful = importTextIDHelper.handleFootNoteTextWithCache(multiEdit, DictHelper.getIdFromDictTextId(multiEdit.getTextId()), DictHelper.getMADForeignSource(),
                                                                                            TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT));

                    if (importTextIDHelper.hasInfos()) {
                        for (String str : importTextIDHelper.getInfos()) {
                            getMessageLog().fireMessage(translateForLog("!!Fußnote %1: %2", dataFootNoteContent.getAsId().getFootNoteId(), str),
                                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        }
                    }

                    // Weil Warnungen bei jeden "handleDictTextId" aufruf gelöscht werden -> Zwischenspeichern der Warnungen
                    if (!dictSuccessful || importTextIDHelper.hasWarnings()) {
                        //Fehler beim Dictionary Eintrag
                        for (String str : importTextIDHelper.getWarnings()) {
                            getMessageLog().fireMessage(translateForLog("!!Fußnote %1 wegen \"%2\" übersprungen", dataFootNoteContent.getAsId().getFootNoteId(), str),
                                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                        }
                        if (importTextIDHelper.hasWarnings()) {
                            reduceRecordCount();
                        }
                        if (!dictSuccessful) {
                            // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                            cancelImport("!!Lexikon Textart nicht gefunden");
                            return;
                        }
                    } else {
                        // Speichern der Fußnote
                        dataFootNoteContent.setFieldValueAsMultiLanguage(FIELD_DFNC_TEXT, multiEdit, DBActionOrigin.FROM_EDIT);
                    }

                    updateProgress(++recordCount, overAllCount);
                }
                if (importToDB) {
                    for (iPartsDataFootNoteContent dataFootNoteContent : tableEntry.dataFootNote.getFootNoteList()) {
                        if (!(dataFootNoteContent.isNew() || dataFootNoteContent.isModifiedWithChildren())) {
                            reduceRecordCount();
                        }
                    }
                    skippedRecords--;
                    saveToDB(tableEntry.dataFootNote);
                }
            }
            importTextIDHelper.clearCache();

            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }
        super.postImportTask();
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.FOOTNOTE));
    }

    private boolean calculateAndSave(DictImportTextIdHelper importTextIDHelper,
                                     Map.Entry<iPartsFootNoteId, FootNoteContainer> footNoteEntry) {
        EtkMultiSprache multiEdit = footNoteEntry.getValue().dataFootNote.getFootNoteList().get(0).getMultiText();
        if (multiEdit == null) {
            multiEdit = new EtkMultiSprache();
            multiEdit.setText(Language.DE.getCode(), footNoteEntry.getValue().footNoteText);
        }
        // Dictionary Eintrag anlegen bzw. aktualisieren
        boolean dictSuccessful = importTextIDHelper.handleFootNoteTextWithCache(multiEdit, footNoteEntry.getValue().footNoteTextId, DictHelper.getMADForeignSource(),
                                                                                TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT));

        if (importTextIDHelper.hasInfos()) {
            for (String str : importTextIDHelper.getInfos()) {
                getMessageLog().fireMessage(translateForLog("!!Fußnote %1: %2", footNoteEntry.getKey().getFootNoteId(), str),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }

        // Weil Warnungen bei jeden "handleDictTextId" aufruf gelöscht werden -> Zwischenspeichern der Warnungen
        if (!dictSuccessful || importTextIDHelper.hasWarnings()) {
            //Fehler beim Dictionary Eintrag
            for (String str : importTextIDHelper.getWarnings()) {
                getMessageLog().fireMessage(translateForLog("!!Fußnote %1 wegen \"%2\" übersprungen", footNoteEntry.getKey().getFootNoteId(), str),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);  //, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
            if (importTextIDHelper.hasWarnings()) {
                reduceRecordCount();
            }
            if (!dictSuccessful) {
                // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                cancelImport("!!Lexikon Textart nicht gefunden");
                return false;
            }
        } else {
            // Speichern der Fußnote
            footNoteEntry.getValue().dataFootNote.getFootNoteList().get(0).setFieldValueAsMultiLanguage(FIELD_DFNC_TEXT, multiEdit, DBActionOrigin.FROM_EDIT);
            if (importToDB) {
                saveToDB(footNoteEntry.getValue().dataFootNote);
            }
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
            isNewImportStyle = true;
            return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNames));
        } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNames));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', true, null));
        }
    }


    private class FootNoteImportHelper extends MADImportHelper {

        public FootNoteImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(FUSS_EDAT) || sourceField.equals(FUSS_ADAT)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(FUSS_FN_FLG)) {
                if (value.startsWith("+")) {
                    value = value.substring(1);
                }
                if (value.endsWith(("."))) {
                    value = value.substring(0, value.length() - 1);
                }
                if (StrUtils.isInteger(value)) {
                    value = EtkDbsHelper.formatLfdNr(Integer.valueOf(value));
                }
            }
            return value;
        }

        public String getFootNoteId(Map<String, String> importRec) {
            String footNoteId = handleValueOfSpecialField(FUSS_FN_NR, importRec).trim();
            if (!StrUtils.isEmpty(footNoteId)) {
                if (footNoteId.length() > 3) {
                    if (handleSpecialFootNoteIds) {
                        if (footNoteId.startsWith("FN")) {
                            footNoteId = footNoteId.substring(2);
                            footNoteId = StrUtils.leftFill(footNoteId, 6, '0');
                        }
                    }
                    return footNoteId;
                } else {
                    return footNoteId;
                }
            }
            return null;
        }

        public FOOTNOTE_TYPES getFootNoteType(Map<String, String> importRec) {
            return getFootNoteType(handleValueOfSpecialField(FUSS_FN_NR, importRec).trim(), handleValueOfSpecialField(FUSS_STANDARD, importRec).trim());
        }

        public FOOTNOTE_TYPES getFootNoteType(String footNoteId, String isStandard) {
            if (!StrUtils.isEmpty(footNoteId)) {
                if (handleSpecialFootNoteIds) {
                    if (footNoteId.startsWith("FN")) {
                        footNoteId = footNoteId.substring(2);
                        footNoteId = StrUtils.leftFill(footNoteId, 6, '0');
                        return getFootNoteType(footNoteId, isStandard);
                    }
                }
                if (footNoteId.length() > 3) {
                    if (footNoteId.startsWith("T")) {
                        return FOOTNOTE_TYPES.TABLE;
                    } else {
                        return FOOTNOTE_TYPES.DIALOG;
                    }
                } else {
                    if (SQLStringConvert.ppStringToBoolean(isStandard)) {
                        return FOOTNOTE_TYPES.ELDAS_STAMM;
                    }
                    return FOOTNOTE_TYPES.ELDAS;
                }
            }
            return FOOTNOTE_TYPES.UNKNOWN;
        }
    }

    private class FootNoteContainer {

        public FOOTNOTE_TYPES footnoteType;
        public String footNoteLfdNr;
        public String footNoteText;
        public String footNoteTextId;
        public String footNoteName;
        public boolean isStandard;
        public String footNote_eDat;
        public String footNote_aDat;
        public iPartsFootNoteId footNoteId;
        public iPartsDataFootNote dataFootNote;

        public void initByRecord(FootNoteImportHelper importHelper, Map<String, String> importRec) {
            footNoteId = new iPartsFootNoteId(importHelper.getFootNoteId(importRec));
            // Bei den MAD-Fußnoten ist der Name immer gleich der ID
            footNoteName = footNoteId.getFootNoteId();
            footnoteType = importHelper.getFootNoteType(importRec);
            footNoteLfdNr = importHelper.handleValueOfSpecialField(FUSS_FN_FLG, importRec);
            footNoteText = DictMultilineText.getInstance().convertFootNoteForImport(importRec.get(FUSS_FN_TXK), footnoteType == FOOTNOTE_TYPES.TABLE);
            footNoteTextId = importHelper.handleValueOfSpecialField(FUSS_TEXT_ID, importRec);
            isStandard = SQLStringConvert.ppStringToBoolean(importHelper.handleValueOfSpecialField(FUSS_STANDARD, importRec));
            footNote_eDat = importHelper.handleValueOfSpecialField(FUSS_EDAT, importRec);
            footNote_aDat = importHelper.handleValueOfSpecialField(FUSS_ADAT, importRec);
        }

        public boolean loadDataFromDB() {
            dataFootNote = new iPartsDataFootNote(getProject(), footNoteId);
            if (!dataFootNote.loadFromDB(footNoteId)) {
                dataFootNote.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            dataFootNote.setFieldValueAsBoolean(FIELD_DFN_STANDARD, isStandard, DBActionOrigin.FROM_EDIT);
            dataFootNote.setFieldValue(FIELD_DFN_NAME, footNoteName, DBActionOrigin.FROM_EDIT);
            iPartsFootNoteContentId footNoteContentId = new iPartsFootNoteContentId(footNoteId.getFootNoteId(), footNoteLfdNr);
            iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(getProject(), footNoteContentId);
            if (dataFootNote.getFootNoteList().containsId(footNoteContentId)) {
                //Vergleich nicht bei DIALOG-Stamm
                if (footnoteType != FOOTNOTE_TYPES.DIALOG_STAMM) {
                    for (iPartsDataFootNoteContent currentFootNoteContent : dataFootNote.getFootNoteList().getAsList()) {
                        if (currentFootNoteContent.getAsId().equals(footNoteContentId)) {
                            if (!currentFootNoteContent.getMultiText().getText(Language.DE.getCode()).equals(footNoteText)) {
                                //Fehler
                                return false;
                            }
                            break;
                        }
                    }
                }
            } else {
                if (!dataFootNoteContent.loadFromDB(footNoteContentId)) {
                    dataFootNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                setMultiLangText(dataFootNoteContent);
                dataFootNote.getFootNoteList().add(dataFootNoteContent, DBActionOrigin.FROM_EDIT);
            }
            return true;
        }

        public void setMultiLangText(iPartsDataFootNoteContent dataFootNoteContent) {
            EtkMultiSprache multiLang = dataFootNoteContent.getMultiText();
            if (multiLang != null) {
                multiLang.setTextId(DictHelper.buildDictTextId(footNoteTextId));
                multiLang.setText(Language.DE, footNoteText);
                dataFootNoteContent.setFieldValueAsMultiLanguage(FIELD_DFNC_TEXT, multiLang, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    private class TableFootNoteContainer {

        private iPartsFootNoteId footNoteId;
        private iPartsDataFootNote dataFootNote = null;

        public void setFootNoteId(iPartsFootNoteId footNoteId) {
            this.footNoteId = footNoteId;
        }

        public iPartsFootNoteId getFootNoteId() {
            return footNoteId;
        }

        public iPartsDataFootNote getDataFootNote() {
            return dataFootNote;
        }

        public boolean addFootNoteContainer(FootNoteContainer container) {
            if (dataFootNote == null) {
                dataFootNote = new iPartsDataFootNote(getProject(), footNoteId);
                if (!dataFootNote.loadFromDB(footNoteId)) {
                    dataFootNote.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                dataFootNote.setFieldValueAsBoolean(FIELD_DFN_STANDARD, container.isStandard, DBActionOrigin.FROM_EDIT);
                dataFootNote.setFieldValue(FIELD_DFN_NAME, container.footNoteName, DBActionOrigin.FROM_EDIT);
            }
            iPartsFootNoteContentId footNoteContentId = new iPartsFootNoteContentId(footNoteId.getFootNoteId(), container.footNoteLfdNr);
            iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(getProject(), footNoteContentId);
            if (dataFootNote.getFootNoteList().containsId(footNoteContentId)) {
                //Vergleich
                for (iPartsDataFootNoteContent currentFoteNoteContent : dataFootNote.getFootNoteList().getAsList()) {
                    if (currentFoteNoteContent.getAsId().equals(footNoteContentId)) {
                        if (!currentFoteNoteContent.getMultiText().getText(Language.DE.getCode()).equals(container.footNoteText)) {
                            //Fehler
                            return false;
                        }
                        break;
                    }
                }
            } else {
                if (!dataFootNoteContent.loadFromDB(footNoteContentId)) {
                    dataFootNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                }
                container.setMultiLangText(dataFootNoteContent);
                dataFootNote.getFootNoteList().add(dataFootNoteContent, DBActionOrigin.FROM_EDIT);
            }
            return true;
        }
    }
}
