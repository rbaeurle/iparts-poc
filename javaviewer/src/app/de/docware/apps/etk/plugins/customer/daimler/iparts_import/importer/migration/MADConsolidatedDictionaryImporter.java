/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.KeyValueRecordGzCSVFileReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictLanguageMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;

import java.util.*;

/**
 * Konsolidierter Text Importer (für MAD Lexikon)  testweise
 * Die CSV-Datei enthält nutr die termId und den DE-Text
 */
public class MADConsolidatedDictionaryImporter extends AbstractDataImporter implements iPartsConst {

    final static String CIMP_TEXT_ID = "Text-ID";
    final static String CIMP_TEXT = "konsolidierter Text";

    private String[] headerNames = new String[]{
            CIMP_TEXT_ID,
            CIMP_TEXT };

    private HashMap<String, String> mappingData;
    private String[] primaryKeysImport;
    private Set<String> knownTextKindIds;
    private boolean isUTF8 = false;
    private boolean importToDB = true;
    private boolean doBufferSave = true;

    public MADConsolidatedDictionaryImporter(EtkProject project, boolean withHeader) {
        super(project, "MAD Consolidated Lexikon (Test)", withHeader,
              new FilesImporterFileListType(TABLE_DA_DICT_META, "!!CSV-Datei mit konsolidierten Texten (Test)", true, false, true, new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysImport = new String[]{ CIMP_TEXT_ID, CIMP_TEXT };
        mappingData = new HashMap<String, String>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysImport);
        importer.setMustHaveData(primaryKeysImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.getCurrentTableName().equals(TABLE_DA_DICT_META)) {
            return false;
        }
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   DictTextKindTypes.CODE_NAME, DictTextKindTypes.COLORS,
                                                                                   DictTextKindTypes.MODEL_NAME, DictTextKindTypes.SA_NAME,
                                                                                   DictTextKindTypes.SAA_NAME, DictTextKindTypes.KG_TU_NAME,
                                                                                   DictTextKindTypes.MAT_NAME, DictTextKindTypes.ADD_TEXT,
                                                                                   DictTextKindTypes.EVO_CK, DictTextKindTypes.DIALOG_MODEL_ADDTEXT,
                                                                                   DictTextKindTypes.FOOTNOTE, DictTextKindTypes.ELDAS_MODEL_ADDTEXT)) {
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
    protected void preImportTask() {
        knownTextKindIds = new TreeSet<String>();
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();

        if (!isCancelled()) {
            // Cache für Lexikon-Texte leeren und neu aufbauen
            DictTextCache.clearCache();
            DictTextCache.warmUpCache();
        }
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        String termId = importRec.get(CIMP_TEXT_ID);
        String textDE = importRec.get(CIMP_TEXT);
        // leerer Text ist erlaubt
        if (StrUtils.isEmpty(termId) || (textDE == null)) {
            if (StrUtils.isEmpty(termId)) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger TermId übersprungen",
                                                            String.valueOf(recordNo)),
                                            MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerem Text und TermId \"%2\" übersprungen",
                                                            String.valueOf(recordNo), termId),
                                            MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            }
            reduceRecordCount();
            return;
        }

        List<iPartsDataDictMeta> dataDictMetaList = findTxtKindExtra(termId);
        if (dataDictMetaList.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 keinen Lexikoneintrag zu TermId \"%2\" gefunden",
                                                        String.valueOf(recordNo), termId),
                                        MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
//        für DEBUG-Zwecke
//        if (dataDictMetaList.size() > 1) {
//            getMessageLog().fireMessage(translateForLog("!!Record %1 mehrere Einträge zu TermId \"%2\" gefunden",
//                                                        String.valueOf(recordNo), termId),
//                                        MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
//        }

        // beim ersten Eintrag Test und Status setzen
        iPartsDataDictMeta dataDictMeta = dataDictMetaList.get(0);
        handleText(recordNo, dataDictMeta, textDE, Language.DE);

        setMetaStatus(dataDictMeta, iPartsDictConst.DICT_STATUS_CONSOLIDATED, Language.DE);
        if (importToDB) {
            saveToDB(dataDictMeta, false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
        }

        // bei allen weiteren nur Status
        for (int lfdNr = 1; lfdNr < dataDictMetaList.size(); lfdNr++) {
            dataDictMeta = dataDictMetaList.get(lfdNr);
            setMetaStatus(dataDictMeta, iPartsDictConst.DICT_STATUS_CONSOLIDATED, Language.DE);
            if (importToDB) {
                saveToDB(dataDictMeta, false, DBDataObject.PrimaryKeyExistsInDB.TRUE);
            }
        }
    }

    /**
     * Text zur Sprache lang in EtkMultiSprache des dataDictMeta-Objects setzen
     * und überprüfen, ob Original-Text '\n' enthält
     *
     * @param recordNo
     * @param dataDictMeta
     * @param text
     * @param lang
     */
    private void handleText(int recordNo, iPartsDataDictMeta dataDictMeta, String text, Language lang) {
        EtkMultiSprache multiLang = dataDictMeta.getMultiLang();
        String oldText = multiLang.getText(lang.getCode());
        if (oldText.contains("\n") && !text.contains("\n")) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 neuer Text mit TermId \"%2\" besitzt keinen Zeilenumbruch",
                                                        String.valueOf(recordNo), dataDictMeta.getForeignId()),
                                        MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);

        }
        multiLang.setText(lang, text);
        dataDictMeta.setNewMultiLang(multiLang);
    }

    /**
     * den Status des dataDictMeta- und dem zugehörigen DataDictLanguageMeta setzen
     *
     * @param dataDictMeta
     * @param status
     * @param lang
     */
    private void setMetaStatus(iPartsDataDictMeta dataDictMeta, String status, Language lang) {
        iPartsDataDictLanguageMeta dataDictLanguageMeta = dataDictMeta.getLanguages().findLanguage(lang.getCode());
        if (dataDictLanguageMeta != null) {
            dataDictLanguageMeta.setState(status, DBActionOrigin.FROM_EDIT);
        }
        dataDictMeta.setState(status, DBActionOrigin.FROM_EDIT);
    }

    /**
     * alle dataDictMeta zu einer termId im Lexikon finden
     *
     * @param termId
     * @return
     */
    private List<iPartsDataDictMeta> findTxtKindExtra(String termId) {
        String dictTextId = DictHelper.buildDictTextId(termId);
        iPartsDataDictMetaList dictMetaList = iPartsDataDictMetaList.loadMetaFromTextIdList(getProject(), dictTextId);
        List<iPartsDataDictMeta> result = new DwList<iPartsDataDictMeta>();
        if (!dictMetaList.isEmpty()) {
            for (iPartsDataDictMeta dataDictMeta : dictMetaList) {
                if (checkDataDictMeta(dataDictMeta, termId)) {
                    result.add(dataDictMeta);
                }
            }
        }
        return result;
    }

    /**
     * überprüfen, ob dataDictMeta zur MAD-Familie gehört
     *
     * @param dataDictMeta
     * @param termId
     * @return
     */
    private boolean checkDataDictMeta(iPartsDataDictMeta dataDictMeta, String termId) {
        if (dataDictMeta.getForeignId().equals(termId) && dataDictMeta.getSource().equals(DictHelper.getMADForeignSource())) {
            if (!knownTextKindIds.contains(dataDictMeta.getAsId().getTextKindId())) {
                if (DictTxtKindIdByMADId.getInstance(getProject()).isTextKindIdPartOfMAD(new iPartsDictTextKindId(dataDictMeta.getAsId().getTextKindId()))) {
                    knownTextKindIds.add(dataDictMeta.getAsId().getTextKindId());
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }


    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        // alle Dateien löschen wurde für die Kenzeichnung, ob UTF-8, missbraucht
        isUTF8 = true;
        getMessageLog().fireMessage(translateForLog("!!Import auf UTF-8 umgestellt"),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        return true;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        String tableName = TABLE_DA_DICT_META;
        if (importFileType.getFileListType().equals(tableName)) {
            DWFileCoding encoding = DWFileCoding.CP_1252;
            if (isUTF8) {
                encoding = DWFileCoding.UTF8;
            }
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(new KeyValueRecordGzCSVFileReader(importFile, tableName, withHeader, headerNames, encoding));
            } else {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', withHeader, headerNames, encoding));
            }
        }
        return false;
    }
}
