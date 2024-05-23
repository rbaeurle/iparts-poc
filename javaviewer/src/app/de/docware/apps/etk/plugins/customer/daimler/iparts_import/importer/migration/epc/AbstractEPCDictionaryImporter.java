/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEPCLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEPCDictionaryImporter extends AbstractDataImporter implements iPartsConst {

    private static final String TABLENAME = "table";

    private String[] headerNames;
    private String externDictIdFieldname;
    private String languageFieldname;
    private String textFieldname;
    private boolean doBufferSave = true;
    private boolean importToDB = true;
    private iPartsDictTextKindId txtKindId;
    private TextAndId currentTextAndId;

    public AbstractEPCDictionaryImporter(EtkProject project, String importName, String fileListName) {
        super(project, importName,
              new FilesImporterFileListType(TABLENAME, fileListName, true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_GZ,
                                                          MimeTypes.EXTENSION_ALL_FILES }));
        setHeaderNames(getHeaderNames());
        txtKindId = initTextKindIdForImport();
    }

    private void setHeaderNames(String[] headerNames) {
        if ((headerNames != null) && (headerNames.length == 3)) {
            externDictIdFieldname = headerNames[0];
            languageFieldname = headerNames[1];
            textFieldname = headerNames[2];
            this.headerNames = headerNames;
        }
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
        progressMessageType = ProgressMessageType.READING;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!StrUtils.isValid(externDictIdFieldname, languageFieldname, textFieldname)) {
            return false;
        }
        if (txtKindId == null) {
            return false;
        }

        // Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForEPC(getMessageLog(), getLogLanguage(),
                                                                                   getTextKindTypes())) {
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

        if (recordNo % 10000 == 0) {
            getMessageLog().fireMessage(translateForLog("!!Importiere Record No %1", String.valueOf(recordNo)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }

        EPCDictImportHelper importHelper = new EPCDictImportHelper(getProject());
        iPartsEPCLanguageDefs langDef = iPartsEPCLanguageDefs.getType(importRec.get(languageFieldname));
        if ((langDef == iPartsEPCLanguageDefs.EPC_UNKNOWN) || (langDef == iPartsEPCLanguageDefs.EPC_NEUTRAL)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Sprache \"%2\" übersprungen", String.valueOf(recordNo),
                                                        importRec.get(languageFieldname)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                        MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
            return;
        }
        // Die Original EPC ID
        String foreignTextId = importHelper.handleValueOfSpecialField(externDictIdFieldname, importRec);
        // Text
        String text = importHelper.handleValueOfSpecialField(textFieldname, importRec);

        // Lexikon Id mit Prefix und Original ID aus EPC
        String textId = getTextId(foreignTextId);
        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(getTxtKindId().getTextKindId(), textId);

        if (currentTextAndId != null) {
            // Check, ob eine neue ID kommt und der bisherige Datensatz gespeichert werden muss
            checkAndStoreCurrentText(dictMetaId);
        } else {
            currentTextAndId = new TextAndId(dictMetaId);
        }
        EtkMultiSprache multiLang = currentTextAndId.getMultilang();
        if (multiLang.spracheExists(langDef.getDbValue())) {
            if (multiLang.getText(langDef.getDbValue().getCode()).equals(text)) {
                getMessageLog().fireMessage(translateForLog("!!Importdatei enthält für die ID \"%1\" und" +
                                                            " Sprache \"%2\" doppelte Einträge mit gleichen Texten.",
                                                            foreignTextId, langDef.getLangEPC()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Importdatei enthält für die gleiche ID \"%1\" " +
                                                            "und Sprache \"%2\" doppelte Einträge mit verschiedenen Texten. Bisher: %3, Neu: %4",
                                                            foreignTextId, langDef.getLangEPC(),
                                                            multiLang.getText(langDef.getDbValue().getCode()), text),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }
        multiLang.setText(langDef.getDbValue(), text);
    }


    private void checkAndStoreCurrentText(iPartsDictMetaId newDictMetaId) {
        if (currentTextAndId != null) {
            iPartsDictMetaId dictMetaId = currentTextAndId.getDictMetaId();
            if (!dictMetaId.equals(newDictMetaId)) {
                storeCurrentText();
                currentTextAndId = new TextAndId(newDictMetaId);
            }
        }
    }

    /**
     * Speichert den gecachten Text
     */
    private void storeCurrentText() {
        // Nur Speichern, wenn wirklich ein Text vorhanden ist
        if (!currentTextAndId.getMultilang().allStringsAreEmpty()) {
            iPartsDictMetaId dictMetaId = currentTextAndId.getDictMetaId();
            iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
            if (!dataDictMeta.existsInDB()) {
                // Zum Initialisieren wird die bisherige Methode aus DictImportTextIdHelper genutzt
                DictImportTextIdHelper.initDataDictMeta(dataDictMeta, DictHelper.getDictId(dictMetaId.getTextId()),
                                                        DictHelper.getEPCForeignSource(), currentTextAndId.getMultilang());
            } else {
                dataDictMeta.setNewMultiLang(currentTextAndId.getMultilang());
            }
            if (importToDB) {
                if (!saveToDB(dataDictMeta)) {
                    skippedRecords += currentTextAndId.getMultilang().getSprachenCount() - 1;
                }
            }
        } else {
            skippedRecords += currentTextAndId.getMultilang().getSprachenCount();
        }
    }

    protected iPartsDictTextKindId getTxtKindId() {
        return txtKindId;
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            storeCurrentText();
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            return importMasterData(prepareImporterKeyValueGZ(importFile, TABLENAME, EPCImportHelper.EPC_IMPORT_DATA_SEPARATOR, true, headerNames, '\0'));
        } else if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_CSV)) {
            return importMasterData(prepareImporterKeyValue(importFile, TABLENAME, EPCImportHelper.EPC_IMPORT_DATA_SEPARATOR, true, headerNames, '\0'));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, TABLENAME, EPCImportHelper.EPC_IMPORT_DATA_SEPARATOR, false, null));
        }
    }

    protected abstract iPartsDictTextKindId initTextKindIdForImport();

    protected abstract String[] getHeaderNames();

    protected abstract DictTextKindEPCTypes[] getTextKindTypes();

    protected abstract String getTextId(String foreignTextId);

    private class EPCDictImportHelper extends EPCImportHelper {

        public EPCDictImportHelper(EtkProject project) {
            super(project, new HashMap<String, String>(), TABLENAME);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }
            if (sourceField.equals(externDictIdFieldname)) {
                value = value.trim();
            }

            return value;
        }
    }

    private class TextAndId {

        private EtkMultiSprache multilang;
        private iPartsDictMetaId dictMetaId;

        public TextAndId(iPartsDictMetaId dictMetaId) {
            this.multilang = new EtkMultiSprache();
            this.multilang.setTextId(dictMetaId.getTextId());
            this.dictMetaId = dictMetaId;
        }

        public EtkMultiSprache getMultilang() {
            return multilang;
        }

        public iPartsDictMetaId getDictMetaId() {
            return dictMetaId;
        }
    }
}
