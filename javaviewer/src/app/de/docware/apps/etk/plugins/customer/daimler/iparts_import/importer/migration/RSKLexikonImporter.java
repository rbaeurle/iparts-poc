/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.fixedlength.*;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTermIdHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindRSKTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.hash.RSKHash;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataSet;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.collections.diskmappedlist.compare.DiskMappedKeyValueListCompare;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.misc.csv.CsvWriter;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.TableAndFieldName;

import java.io.IOException;
import java.util.*;

/**
 * RSK-Lexikon Importer
 */
public class RSKLexikonImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    // Baumuster-Grunddaten
    final static String SATZART = "SATZART";

    final static int RSK_SATZART_LENGTH = 700;
    final static String RSK_SATZARTKENNER = "S001";
    final static String RSK_ISOLANGAUGE = "SPRACHE";
    final static String RSK_RSKID = "IDRDNRK";
    final static String RSK_KG = "KG";
    final static String RSK_KGU = "KGU";
    final static String RSK_END_NUMBER = "ENDNR";
    final static String RSK_BENENNUNG = "BENENNUNG-RSK";
    final static String RSK_PG_BENENNUNG = "BENENNUNG-PG";
    final static String RSK_TERMID = "TERMID";
    final static String RSK_ET_BENENNUNG = "ETBEN";
    final static String RSK_TEILE_SPEKTRUM = "TEILESPEKTRUM";
    final static String RSK_LOESCH_KENNZEICHEN = "LOESCHKZ";
    final static String RSK_FILLER = "FILLER";

    // Flag zur Auswahl von verschiedenen Implementierungen mit unterschiedlicher Performance; Importart 2 ist die schnellste
    private static final int importArt = 2;
    private static final boolean withTermIdHandling = true;

    private DWFile exportFile;
    private CsvWriter writer = null;
    private Map<String, RSKRecord> mapLang;
    private List<Language> languages;
    private boolean importToDB = true;
    private boolean doBufferSave = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public RSKLexikonImporter(EtkProject project) {
        super(project, "RSK Lexikon",
              new FilesImporterFileListType("RSK Lexikon", "!!RSK Lexikon", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForRSK(getMessageLog(), getLogLanguage(),
                                                                                   DictTextKindRSKTypes.MAT_AFTER_SALES,
                                                                                   DictTextKindRSKTypes.MAT_CONSTRUCTION)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (importer instanceof KeyValueRecordFixedLengthFileReader) {
            KeyValueRecordFixedLengthFileReader flImporter = (KeyValueRecordFixedLengthFileReader)importer;
            if (flImporter.isInvalidLine()) {
                errors.add(translateForLog("!!Fehler in Record %1: Ungültige Zeilenlänge", String.valueOf(flImporter.getRecordNo())));
                return false;
            }
        }
        return true;
    }

    @Override
    protected void preImportTask() {
        setBufferedSave(doBufferSave);
        List<String> langList = getProject().getConfig().getDatabaseLanguages();
        languages = new ArrayList<Language>(langList.size());
        for (String lang : langList) {
            languages.add(Language.findLanguage(lang));
        }

        switch (importArt) {
            case 0:
                writer = new CsvWriter(exportFile, DWFileCoding.UTF8, '\t');
                break;
            case 1:
                mapLang = new LinkedHashMap<String, RSKRecord>();
                break;
            case 2:
                mapLang = new LinkedHashMap<String, RSKRecord>();
                progressMessageType = ProgressMessageType.READING;
                break;
        }
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        switch (importArt) {
            case 0:
                importRecordPlain(importRec, recordNo);
                break;
            case 1:
                importRecordHash(importRec, recordNo);
                break;
            case 2:
                importRecordHash(importRec, recordNo);
                break;
        }
    }

    private void importRecordPlain(Map<String, String> importRec, int recordNo) {
        try {
            writeByRecord(importRec);
            String loeschKZ = importRec.get(TableAndFieldName.make(SATZART, RSK_LOESCH_KENNZEICHEN));
            if (!loeschKZ.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit LoeschKZ \"%2\" bei TermId %3 Sprache \"%4\"",
                                                            String.valueOf(recordNo), loeschKZ,
                                                            importRec.get(TableAndFieldName.make(SATZART, RSK_TERMID)),
                                                            importRec.get(TableAndFieldName.make(SATZART, RSK_ISOLANGAUGE))),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
        }
    }

    private void importRecordHash(Map<String, String> importRec, int recordNo) {
        String termId = importRec.get(TableAndFieldName.make(SATZART, RSK_TERMID));
        if (termId.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerer TermId übersprungen", String.valueOf(recordNo)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return;
        }
        if (withTermIdHandling) {
            termId = iPartsTermIdHandler.removeLeadingZerosFromTermId(termId);
        }

        String insertCDText = importRec.get(TableAndFieldName.make(SATZART, RSK_BENENNUNG));
        String insertASText = importRec.get(TableAndFieldName.make(SATZART, RSK_ET_BENENNUNG));
        String insertLang = importRec.get(TableAndFieldName.make(SATZART, RSK_ISOLANGAUGE));
        RSKRecord rec = mapLang.get(termId);
        if (rec == null) {
            rec = new RSKRecord(termId, languages);
            mapLang.put(termId, rec);
            if (insertCDText.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 TermId %2 Sprache \"%3\" %4 ist leer", String.valueOf(recordNo),
                                                            termId, insertLang, TranslationHandler.translate("!!Konstruktions-Bezeichnung")),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            } else {
                rec.setCDText(insertLang, insertCDText);
            }
            if (insertASText.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 TermId %2 Sprache \"%3\" %4 ist leer", String.valueOf(recordNo),
                                                            termId, insertLang, TranslationHandler.translate("!!After-Sales-Bezeichnung")),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            } else {
                rec.setASText(insertLang, insertASText);
            }
            rec.setLoesch(insertLang, importRec.get(TableAndFieldName.make(SATZART, RSK_LOESCH_KENNZEICHEN)));
        } else {
            checkAndInsertCDText(recordNo, rec, termId, insertCDText, insertLang);
            checkAndInsertASText(recordNo, rec, termId, insertASText, insertLang, importRec.get(TableAndFieldName.make(SATZART, RSK_LOESCH_KENNZEICHEN)));
        }
    }

    private void checkAndInsertCDText(int recordNo, RSKRecord rec, String termId, String insertText, String insertLang) {
        if (insertText.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 TermId %2 Sprache \"%3\" %4 ist leer", String.valueOf(recordNo),
                                                        termId, insertLang, TranslationHandler.translate("!!Konstruktions-Bezeichnung")),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        } else {
            String existingCDText = rec.getCDText(insertLang);
            if (!existingCDText.isEmpty()) {
//                getMessageLog().fireMessage(translateForLog("!!Record %1 TermId %2 Sprache \"%3\" ist doppelt", String.valueOf(recordNo),
//                                                            key, insertLang),
//                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);

                if (!existingCDText.equals(insertText)) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 TermId %2 Sprache \"%3\" %4: Texte sind unterschiedlich", String.valueOf(recordNo),
                                                                termId, insertLang, TranslationHandler.translate("!!Konstruktions-Bezeichnung")),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
            }
            rec.setCDText(insertLang, insertText);
        }
    }

    private void checkAndInsertASText(int recordNo, RSKRecord rec, String termId, String insertText, String insertLang, String loesch) {
        if (insertText.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 TermId %2 Sprache \"%3\" %4 ist leer", String.valueOf(recordNo),
                                                        termId, insertLang, TranslationHandler.translate("!!After-Sales-Bezeichnung")),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        } else {
            String existingASText = rec.getASText(insertLang);
            if (!existingASText.isEmpty()) {
//                getMessageLog().fireMessage(translateForLog("!!Record %1 TermId %2 Sprache \"%3\" ist doppelt", String.valueOf(recordNo),
//                                                            key, insertLang),
//                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);

                if (!existingASText.equals(insertText)) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 TermId %2 Sprache \"%3\" %4: Texte sind unterschiedlich", String.valueOf(recordNo),
                                                                termId, insertLang, TranslationHandler.translate("!!After-Sales-Bezeichnung")),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
            }
            rec.setASText(insertLang, insertText);
            rec.setLoesch(insertLang, loesch);
        }
    }

    @Override
    public void postImportTask() {
        try {
            if (!isCancelled()) {
                switch (importArt) {
                    case 1:
                        writeSortedByTermId();
                        break;
                    case 2:
                        compareAndSave();
                        break;
                }
            }
            if (writer != null) {
                writer.close();
            }
            super.postImportTask();
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            cancelImport(e.getMessage());
        }
    }

    private void writeByRecord(Map<String, String> importRec) throws IOException {
        String[] result = new String[5];
        result[0] = importRec.get(TableAndFieldName.make(SATZART, RSK_TERMID));
        if (withTermIdHandling) {
            result[0] = iPartsTermIdHandler.removeLeadingZerosFromTermId(result[0]);
        }
        result[1] = importRec.get(TableAndFieldName.make(SATZART, RSK_ISOLANGAUGE));
        result[2] = importRec.get(TableAndFieldName.make(SATZART, RSK_BENENNUNG));
        result[3] = importRec.get(TableAndFieldName.make(SATZART, RSK_ET_BENENNUNG));
        result[4] = importRec.get(TableAndFieldName.make(SATZART, RSK_LOESCH_KENNZEICHEN));

        writer.writeNext(result);
    }

    private void writeSortedByTermId() throws IOException {
        getMessageLog().fireMessage(translateForLog("!!Anzahl gefundene TermIds: %1", String.valueOf(mapLang.size())),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        getMessageLog().fireMessage(translateForLog("!!Speichere Ergebnisse"),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        writer = new CsvWriter(exportFile, DWFileCoding.UTF8, '\t');
        int cnt = 0;
        for (Map.Entry<String, RSKRecord> entry : mapLang.entrySet()) {
            RSKRecord rec = entry.getValue();
            rec.prepareRSKRecord();
            if (!rec.analyzeLoesch()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 variierendes LoeschKZ \"%2\" bei \"%3\"",
                                                            String.valueOf(cnt), rec.getDbLoesch(),
                                                            entry.getKey()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                List<String> liste = rec.buildLoeschOutput();
                for (String elem : liste) {
                    getMessageLog().fireMessage("    " + elem, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
            }
            Set<String> allLanguages = new LinkedHashSet<String>();
            allLanguages.addAll(rec.getCDMultiLang().getSprachen());
            allLanguages.addAll(rec.getASMultiLang().getSprachen());
            for (String lang : allLanguages) {
                String[] result = new String[5];
                result[0] = entry.getKey();
                result[1] = lang;
                result[2] = rec.getCDText(lang);
                result[3] = rec.getASText(lang);
                result[4] = rec.getDbLoesch();
                writer.writeNext(result);
                cnt++;
            }
        }
        getMessageLog().fireMessage(translateForLog("!!Anzahl geschriebener Records: %1", String.valueOf(cnt)),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
    }

    private void compareAndSave() {
        getMessageLog().fireMessage(translateForLog("!!Anzahl gefundene TermIds: %1", String.valueOf(mapLang.size())),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        doCompareAndSave();
    }

    private boolean doCompareAndSave() {
        //bestimme TextKindId
        Map<String, RSKRecord> dbMapLang;
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        iPartsDictTextKindId textKindASId = dictTxtKindIdByMADId.getRSKTxtKindId(DictTextKindRSKTypes.MAT_AFTER_SALES);
        iPartsDictTextKindId textKindCDId = dictTxtKindIdByMADId.getRSKTxtKindId(DictTextKindRSKTypes.MAT_CONSTRUCTION);
        if ((textKindASId == null) || (textKindCDId == null)) {
            cancelImport(translateForLog("!!Fehlende Lexikon-Textart"));
            return false;
        }
        dbMapLang = readRSKEntriesFromDB(textKindASId, textKindCDId);
        progressMessageType = ProgressMessageType.IMPORTING;
        if (!dbMapLang.isEmpty()) {
            for (RSKRecord rskRecord : mapLang.values()) {
                rskRecord.prepareRSKRecord();
            }
            getMessageLog().fireMessage(translateForLog("!!Vergleiche After-Sales-Bezeichnungen..."),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (!doCompare(dbMapLang, textKindASId, DictTextKindRSKTypes.MAT_AFTER_SALES)) {
                return false;
            }
            getMessageLog().fireMessage(translateForLog("!!Vergleiche Konstruktions-Bezeichnungen..."),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (!doCompare(dbMapLang, textKindCDId, DictTextKindRSKTypes.MAT_CONSTRUCTION)) {
                return false;
            }
        } else {
            //keine Einträge in DB gefunden
            getMessageLog().fireMessage(translateForLog("!!Importiere neue Einträge (%1)", String.valueOf(mapLang.size())),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            int cnt = 0;
            for (RSKRecord rskRecord : mapLang.values()) {
                if (threadCancelled()) {
                    return false;
                }
                rskRecord.prepareRSKRecord();
                //erzeuge in SPRACHE, DA_DICT_SPRACHE, DA_DICT_META neuen Eintrag
                createOrUpdateEntry(rskRecord, textKindASId, DictTextKindRSKTypes.MAT_AFTER_SALES);
                createOrUpdateEntry(rskRecord, textKindCDId, DictTextKindRSKTypes.MAT_CONSTRUCTION);
                cnt++;
                updateProgress(cnt, mapLang.size());
            }
        }
        return true;
    }

    private boolean doCompare(Map<String, RSKRecord> dbMapLang, iPartsDictTextKindId textKindId, DictTextKindRSKTypes rskType) {
        //no Equals
        DiskMappedKeyValueListCompare listComp = new DiskMappedKeyValueListCompare(MAX_ROWS_IN_MEMORY, true, false, true);
        int totalRecordCount = fillComparer(listComp, mapLang, dbMapLang, rskType);
        if (totalRecordCount > 0) {
            int currentRecordCounter = 0;
            if (listComp.getOnlyInFirstItems().size() > 0) {
                // lege neue Einträge an
                getMessageLog().fireMessage(translateForLog("!!Importiere neue Einträge (%1)", String.valueOf(listComp.getOnlyInFirstItems().size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                Iterator<DiskMappedKeyValueEntry> iter = listComp.getOnlyInFirstItems().getIterator();
                while (iter.hasNext()) {
                    if (threadCancelled()) {
                        return false;
                    }

                    DiskMappedKeyValueEntry entry = iter.next();
                    RSKRecord rec = mapLang.get(entry.getKey());
                    //erzeuge in SPRACHE, DA_DICT_SPRACHE, DA_DICT_META neuen Eintrag
                    createOrUpdateEntry(rec, textKindId, rskType);
                    currentRecordCounter++;
                    updateProgress(currentRecordCounter, totalRecordCount);
                }
            }
            if (listComp.getOnlyInSecondItems().size() > 0) {
                //Lösche Einträge
                getMessageLog().fireMessage(translateForLog("!!Lösche Einträge (%1)", String.valueOf(listComp.getOnlyInSecondItems().size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                Iterator<DiskMappedKeyValueEntry> iter = listComp.getOnlyInSecondItems().getIterator();
                while (iter.hasNext()) {
                    if (threadCancelled()) {
                        return false;
                    }
                    DiskMappedKeyValueEntry entry = iter.next();
                    RSKRecord rec = dbMapLang.get(entry.getKey());
                    //delete from SPRACHE, DA_DICT_SPRACHE, DA_DICT_META
                    deleteEntry(rec, textKindId, rskType);
                    currentRecordCounter++;
                    updateProgress(currentRecordCounter, totalRecordCount);
                }
            }
            if (listComp.getDifferentItems().size() > 0) {
                // update der bestehenden Einträge
                getMessageLog().fireMessage(translateForLog("!!Aktualisiere Einträge (%1)", String.valueOf(listComp.getDifferentItems().size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                Iterator<DiskMappedKeyValueEntry> iter = listComp.getDifferentItems().getIterator();
                while (iter.hasNext()) {
                    if (threadCancelled()) {
                        return false;
                    }

                    DiskMappedKeyValueEntry entry = iter.next();
                    RSKRecord rec = mapLang.get(entry.getKey());
                    //update in SPRACHE, DA_DICT_SPRACHE, DA_DICT_META diesen Eintrag
                    createOrUpdateEntry(rec, textKindId, rskType);
                    currentRecordCounter++;
                    updateProgress(currentRecordCounter, totalRecordCount);
                }
            }
        } else {
            getMessageLog().fireMessage(translateForLog("!!Keine Änderungen"),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
        listComp.cleanup();

        return true;
    }

    private int fillComparer(DiskMappedKeyValueListCompare listComp, Map<String, RSKRecord> mapSlave, Map<String, RSKRecord> mapMaster,
                             DictTextKindRSKTypes rskType) {
        if (rskType == DictTextKindRSKTypes.MAT_AFTER_SALES) {
            for (Map.Entry<String, RSKRecord> entry : mapSlave.entrySet()) {
                RSKRecord rec = entry.getValue();
                if (rec.getASHashValue() != null) {
                    listComp.putFirst(entry.getKey(), rec.getASHashValue());
                }
            }
            for (Map.Entry<String, RSKRecord> entry : mapMaster.entrySet()) {
                RSKRecord rec = entry.getValue();
                if (rec.getASHashValue() != null) {
                    listComp.putSecond(entry.getKey(), rec.getASHashValue());
                }
            }
        } else {
            for (Map.Entry<String, RSKRecord> entry : mapSlave.entrySet()) {
                RSKRecord rec = entry.getValue();
                if (rec.getCDHashValue() != null) {
                    listComp.putFirst(entry.getKey(), rec.getCDHashValue());
                }
            }
            for (Map.Entry<String, RSKRecord> entry : mapMaster.entrySet()) {
                RSKRecord rec = entry.getValue();
                if (rec.getCDHashValue() != null) {
                    listComp.putSecond(entry.getKey(), rec.getCDHashValue());
                }
            }
        }
        return listComp.getDifferentItems().size() + listComp.getOnlyInFirstItems().size() + listComp.getOnlyInSecondItems().size();
    }

    private void createOrUpdateEntry(RSKRecord rec, iPartsDictTextKindId textKindId, DictTextKindRSKTypes rskType) {

        iPartsDictMetaId id;
        if (rskType == DictTextKindRSKTypes.MAT_AFTER_SALES) {
            id = new iPartsDictMetaId(textKindId.getTextKindId(), rec.getASTextId());
        } else {
            id = new iPartsDictMetaId(textKindId.getTextKindId(), rec.getCDTextId());
        }
        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), id);
        if (!dataDictMeta.loadFromDB(id)) {
            dataDictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataDictMeta.setActCreationDate(DBActionOrigin.FROM_EDIT);
            dataDictMeta.setState(rec.getDbLoesch(), DBActionOrigin.FROM_EDIT);
        } else {
            dataDictMeta.setActChangeDate(DBActionOrigin.FROM_EDIT);
        }
        dataDictMeta.setForeignId(rec.getTermId(), DBActionOrigin.FROM_EDIT);
        dataDictMeta.setSource(DictHelper.getRSKForeignSource(), DBActionOrigin.FROM_EDIT);
        dataDictMeta.setUserId(DictHelper.getRSKUserId(), DBActionOrigin.FROM_EDIT);

        if (rskType == DictTextKindRSKTypes.MAT_AFTER_SALES) {
            dataDictMeta.setNewMultiLang(rec.getASMultiLang());
        } else {
            dataDictMeta.setNewMultiLang(rec.getCDMultiLang());
        }

        if (importToDB) {
            if (saveToDB(dataDictMeta)) {
                autoCommitAfterMaxEntries(MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT);
            }
        }
    }

    private boolean threadCancelled() {
        if (Thread.currentThread().isInterrupted()) {
            cancelImport("!!Import-Thread wurde frühzeitig beendet");
            return true;
        }
        return false;
    }


    private void deleteEntry(RSKRecord rec, iPartsDictTextKindId textKindId, DictTextKindRSKTypes rskType) {
        DictImportTextIdHelper helper = new DictImportTextIdHelper(getProject());
        if (rskType == DictTextKindRSKTypes.MAT_AFTER_SALES) {
            helper.deleteDictEntry(textKindId, rec.getASTextId());
        } else {
            helper.deleteDictEntry(textKindId, rec.getCDTextId());
        }
    }

    private Map<String, RSKRecord> readRSKEntriesFromDB(iPartsDictTextKindId textKindASId, iPartsDictTextKindId textKindCDId) {
        Map<String, RSKRecord> dbMapLang = new LinkedHashMap<>();

        // Alle Felder der Tabelle bestimmen
        String[] fields;
        EtkDatabaseTable tableDef = getProject().getEtkDbs().getConfigBase().getDBDescription().findTable(TABLE_SPRACHE);
        if (tableDef != null) {
            fields = getProject().getEtkDbs().getExistingFieldNamesWithoutBlobsArray(TABLE_SPRACHE);
        } else {
            Logger.getLogger().throwRuntimeException("No table definition for '" + TABLE_SPRACHE + "'");
            return dbMapLang; // egal, weil durch RuntimeException sowieso unerreichbar -> nur für die Code-Analyse
        }

        getMessageLog().fireMessage(translateForLog("!!Lese After-Sales-Bezeichnungen aus der Datenbank..."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        readRSKTypeEntriesFromDB(dbMapLang, DictTextKindRSKTypes.MAT_AFTER_SALES, textKindASId.getTextKindId(), fields);
        getMessageLog().fireMessage(translateForLog("!!Lese Konstruktions-Bezeichnungen aus der Datenbank..."),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        readRSKTypeEntriesFromDB(dbMapLang, DictTextKindRSKTypes.MAT_CONSTRUCTION, textKindCDId.getTextKindId(), fields);
        return dbMapLang;
    }

    private void readRSKTypeEntriesFromDB(Map<String, RSKRecord> dbMapLang, DictTextKindRSKTypes rskType, String textKindId, String[] fields) {
        String whereValue;
        Set<String> textIdList = getTextIdListFast(textKindId);

        switch (rskType) {
            case MAT_AFTER_SALES:
                whereValue = SQLUtils.wildcardExpressionToSQLLike(DictHelper.buildDictRSKTextId("*"), false, false, false);
                break;
            case MAT_CONSTRUCTION:
                whereValue = SQLUtils.wildcardExpressionToSQLLike(DictHelper.buildDictRSKCDTextId("*"), false, false, false);
                break;
            default:
                //error
                return;
        }
        DBDataSet dataSet = getProject().getDB().createQueryEx(TABLE_SPRACHE, fields,
                                                               new String[]{ FIELD_S_TEXTID },
                                                               new String[]{ whereValue },
                                                               new String[]{ FIELD_S_TEXTID },
                                                               -1, true, false);
        try {
            int cnt = 0;
            String previousTextId = "";
            boolean skipRecords = false;
            RSKRecord rec = null;
            while (dataSet.next()) {
                EtkRecord record = dataSet.getRecord(fields);
                String textId = record.getField(FIELD_S_TEXTID).getAsString();
                if (!previousTextId.equals(textId)) { // neue TextId? -> alten RSKRecord abschließen und neuen holen bzw. erzeugen
                    previousTextId = textId;
                    String termId = DictHelper.getTermIdFromRSKTextId(rskType, textId);
                    if (rec != null) {
                        switch (rskType) {
                            case MAT_AFTER_SALES:
                                rec.prepareRSKRecordAS(); // Hashwert für After-Sales berechnen
                                rec.clearASTexts(); // After-Sales-Texte können jetzt gelöscht werden, um Speicher zu sparen
                                break;
                            case MAT_CONSTRUCTION:
                                rec.prepareRSKRecordCD(); // Hashwert für Konstruktion berechnen
                                rec.clearCDTexts(); // Konstruktions-Texte können jetzt gelöscht werden, um Speicher zu sparen
                                break;
                        }
                    }
                    skipRecords = !textIdList.contains(textId);
                    if (!skipRecords) {
                        textIdList.remove(textId);
                        rec = dbMapLang.get(termId);
                        if (rec == null) {
                            rec = new RSKRecord(termId, languages);
                            dbMapLang.put(termId, rec);
                        }
                    } else {
                        rec = null;
                    }
                }
                if (!skipRecords && (rec != null)) {
                    switch (rskType) {

                        case MAT_AFTER_SALES:
                            rec.setASText(record.getField(FIELD_S_SPRACH).getAsString(), getProject().getEtkDbs().getLongTextFromRecord(record));
                            break;
                        case MAT_CONSTRUCTION:
                            rec.setCDText(record.getField(FIELD_S_SPRACH).getAsString(), getProject().getEtkDbs().getLongTextFromRecord(record));
                            break;
                    }
                }
                cnt++;
                updateProgress(cnt, -1);
            }
            if (rec != null) {
                switch (rskType) {
                    case MAT_AFTER_SALES:
                        rec.prepareRSKRecordAS(); // Hashwert für After-Sales berechnen
                        rec.clearASTexts(); // After-Sales-Texte können jetzt gelöscht werden, um Speicher zu sparen
                        break;
                    case MAT_CONSTRUCTION:
                        rec.prepareRSKRecordCD(); // Hashwert für Konstruktion berechnen
                        rec.clearCDTexts(); // Konstruktions-Texte können jetzt gelöscht werden, um Speicher zu sparen
                        break;
                }
            }
        } finally {
            dataSet.close();
        }
        if (!textIdList.isEmpty()) {
            //es gibt noch Lexikon-Einträge, zu denen es keinen Text gibt (wie auch immer)
            for (String textId : textIdList) {
                boolean doInsert = false;
                switch (rskType) {
                    case MAT_AFTER_SALES:
                        doInsert = DictHelper.isDictRSKTextId(textId);
                        break;
                    case MAT_CONSTRUCTION:
                        doInsert = DictHelper.isDictRSKCDTextId(textId);
                        break;
                }
                if (doInsert) {
                    String termId = DictHelper.getTermIdFromRSKTextId(rskType, textId);
                    RSKRecord rec = new RSKRecord(termId, languages);
                    dbMapLang.put(termId, rec);
                    switch (rskType) {
                        case MAT_AFTER_SALES:
                            rec.setASText(Language.DE.getCode(), "##Dictionary-Entry without Text##");
                            rec.prepareRSKRecordAS(); // Hashwert für After-Sales berechnen
                            rec.clearASTexts(); // After-Sales-Texte können jetzt gelöscht werden, um Speicher zu sparen
                            break;
                        case MAT_CONSTRUCTION:
                            rec.setCDText(Language.DE.getCode(), "##Dictionary-Entry without Text##");
                            rec.prepareRSKRecordCD(); // Hashwert für Konstruktion berechnen
                            rec.clearCDTexts(); // Konstruktions-Texte können jetzt gelöscht werden, um Speicher zu sparen
                            break;
                    }
                }
            }
        }
    }

    private Set<String> getTextIdListFast(String textKindId) {
        Set<String> textIdList = new HashSet<String>();
        String[] fields = new String[]{ FIELD_DA_DICT_META_TEXTID };
        DBDataSet dataSet = getProject().getDB().createQueryEx(TABLE_DA_DICT_META,
                                                               fields,
                                                               new String[]{ FIELD_DA_DICT_META_TXTKIND_ID },
                                                               new String[]{ textKindId },
                                                               null,
                                                               -1, false, false);
        try {
            while (dataSet.next()) {
                EtkRecord record = dataSet.getRecord(fields);
                String textId = record.getField(FIELD_DA_DICT_META_TEXTID).getAsString();
                textIdList.add(textId);
            }
        } finally {
            dataSet.close();
        }
        return textIdList;
    }

    private Set<String> getTextIdList(String textKindId) {
        iPartsDataDictMetaList dataDictMetaList = iPartsDataDictMetaList.loadMetaIdsFromTextKindList(getProject(), textKindId);
        Set<String> textIdList = new HashSet<String>(dataDictMetaList.size());
        for (iPartsDataDictMeta dataDictMeta : dataDictMetaList) {
            textIdList.add(dataDictMeta.getTextId());
        }
        return textIdList;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        exportFile = DWFile.get(importFile.getParentDWFile(), "Output.csv");
        return importMasterData(prepareImporterFixedLength(importFile));
    }

    protected AbstractKeyValueRecordReader prepareImporterFixedLength(DWFile xmlImportFile) {
        FixedLenRecordType[] recordTypes = new FixedLenRecordType[]{
                new FixedLenRecordType(SATZART,
                                       new FixedLenRecordTypeIdentifier[]{
                                               new FixedLenRecordTypeIdentifier(1, 4, RSK_SATZARTKENNER)
                                       },
                                       new FixedLenFieldDescription[]{
                                               new FixedLenFieldDescription(5, 7, RSK_ISOLANGAUGE),
                                               //new FixedLenFieldDescription(8, 17, RSK_RSKID),
                                               //new FixedLenFieldDescription(18, 19, RSK_KG),
                                               //new FixedLenFieldDescription(20, 20, RSK_KGU),
                                               //new FixedLenFieldDescription(21, 23, RSK_END_NUMBER),
                                               new FixedLenFieldDescription(24, 223, RSK_BENENNUNG),
                                               //new FixedLenFieldDescription(224, 423, RSK_PG_BENENNUNG),
                                               new FixedLenFieldDescription(424, 433, RSK_TERMID),
                                               new FixedLenFieldDescription(434, 633, RSK_ET_BENENNUNG),
                                               //new FixedLenFieldDescription(634, 635, RSK_TEILE_SPEKTRUM),
                                               new FixedLenFieldDescription(636, 636, RSK_LOESCH_KENNZEICHEN)
                                       }
                )
        };

        // Daimler definiert seine Recordlänge als echte Recordlänge, also Nutzdaten + Zeilenende; da Daimler immer Unix-Zeileende hat
        // gilt also: Nutzdatenlänge = Daimler-Record-Länge - 1
        return new KeyValueRecordFixedLengthGZFileReader(xmlImportFile, "", recordTypes, RSK_SATZART_LENGTH - 1, DWFileCoding.UTF8);
    }


    private class RSKRecord {

        private String termId;
        private EtkMultiSprache constructionMultiLang;
        private EtkMultiSprache aftersalesMultiLang;
        private String cdHashValue;
        private String asHashValue;
        private Map<String, String> loeschValues = new LinkedHashMap<String, String>();

        public RSKRecord(String termId, List<Language> languages) {
            this.termId = termId;
            this.constructionMultiLang = new EtkMultiSprache(languages);
            this.constructionMultiLang.setTextId(DictHelper.buildDictRSKCDTextId(termId));
            this.aftersalesMultiLang = new EtkMultiSprache(languages);
            this.aftersalesMultiLang.setTextId(DictHelper.buildDictRSKTextId(termId));
        }

        public String getCDTextId() {
            return constructionMultiLang.getTextId();
        }

        public String getASTextId() {
            return aftersalesMultiLang.getTextId();
        }

        public String getTermId() {
            return termId;
        }

        public void setLoesch(String sprache, String text) {
            loeschValues.put(sprache.toUpperCase(), text);
        }

        public boolean analyzeLoesch() {
//            if (getLoesch().isEmpty()) {
//                for (String text : loeschValues.values()) {
//                    if (!text.isEmpty()) {
//                        return false;
//                    }
//                }
//            } else {
//                for (String text : loeschValues.values()) {
//                    if (text.isEmpty()) {
//                        return false;
//                    }
//                }
//            }
            return true;
        }

        public List<String> buildLoeschOutput() {
            List<String> result = new DwList<String>();
            for (String lang : loeschValues.keySet()) {
                result.add(lang + ": \"" + loeschValues.get(lang) + "\"");
            }
            return result;
        }

        public void setCDText(String sprache, String text) {
            constructionMultiLang.setText(sprache, text);
        }

        public String getCDText(String sprache) {
            return constructionMultiLang.getText(sprache);
        }

        public EtkMultiSprache getCDMultiLang() {
            return constructionMultiLang;
        }

        public void setASText(String sprache, String text) {
            aftersalesMultiLang.setText(sprache, text);
        }

        public String getASText(String sprache) {
            return aftersalesMultiLang.getText(sprache);
        }

        public EtkMultiSprache getASMultiLang() {
            return aftersalesMultiLang;
        }

        public void prepareRSKRecordAS() {
            getASMultiLang().removeLanguagesWithEmptyTexts();
            if (!getASMultiLang().allStringsAreEmpty()) {
                //im Auganblick noch ohne LoeschKennzeichen
                //hashValue = RSKHash.calcHashValue(getMultiLang(), getDbLoesch());
                asHashValue = RSKHash.calcHashValue(getASMultiLang());
            } else {
                asHashValue = null;
            }

            Map<String, String> actLoeschValues = new LinkedHashMap<String, String>();
            for (String lang : getASMultiLang().getSprachen()) {
                actLoeschValues.put(lang, loeschValues.get(lang));
            }
            loeschValues = actLoeschValues;
        }

        public void prepareRSKRecordCD() {
            getCDMultiLang().removeLanguagesWithEmptyTexts();
            if (!getCDMultiLang().allStringsAreEmpty()) {
                //im Auganblick noch ohne LoeschKennzeichen
                //hashValue = RSKHash.calcHashValue(getMultiLang(), getDbLoesch());
                cdHashValue = RSKHash.calcHashValue(getCDMultiLang());
            } else {
                cdHashValue = null;
            }
        }

        public void prepareRSKRecord() {
            prepareRSKRecordAS();
            prepareRSKRecordCD();
        }

        public void clearTexts() {
            clearASTexts();
            clearCDTexts();
        }

        public void clearASTexts() {
            getASMultiLang().removeAllLanguages();
        }

        public void clearCDTexts() {
            getCDMultiLang().removeAllLanguages();
        }

        public String getCDHashValue() {
            return cdHashValue;
        }

        public String getASHashValue() {
            return asHashValue;
        }

        public String getDbLoesch() {
            return DictHelper.getMADDictStatus();
        }
    }
}