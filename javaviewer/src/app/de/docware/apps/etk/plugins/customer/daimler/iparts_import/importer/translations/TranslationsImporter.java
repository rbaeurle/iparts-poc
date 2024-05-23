/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.translations;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.iPartsDictPrefixAndSuffix;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.DictTextKindTransitTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.iPartsTransitMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.BigDataXMLHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsKeyValueRecordReader;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TranslationsImporter extends AbstractSAXPushDataImporter implements iPartsConst {

    private static final String HEADER_INFORMATION_XML_ELEMENT = "XPRT-translation";
    private static final String SINGLE_DATASET_XML_ELEMENT = "translation";
    private static final String TRANSLATED_TEXT_MODULE_XML_ELEMENT = "ContentTranslate";
    private static final String SOURCE_TEXT_MODULE_XML_ELEMENT = "ContentNoTranslate";

    private static final String ELEMENT_KEY_BASIS = "<XPRT-translation><translations>";
    private static final String ELEMENT_KEY_BASIS_TRANSLATION = ELEMENT_KEY_BASIS + "<translation>";
    private static final String ELEMENT_KEY_INLINE_TEXT = ELEMENT_KEY_BASIS_TRANSLATION + "<text><ContentTranslate><ContNoTrans_Inline>";
    private static final String ELEMENT_KEY_MULTLINE_TEXT = ELEMENT_KEY_BASIS_TRANSLATION + "<text><ContentTranslate><br>";
    private static final String ELEMENT_KEY_TEXT_ID = ELEMENT_KEY_BASIS_TRANSLATION + "<text>:textObjectID";
    private static final String ELEMENT_KEY_TEXT_KIND_META_INFO = ELEMENT_KEY_BASIS_TRANSLATION + "<metaInformation>:type";
    private static final String ELEMENT_KEY_MULTILINE_SOURCE_TEXT = ELEMENT_KEY_BASIS_TRANSLATION + "<sourceText><ContentNoTranslate><br>";
    private static final String ELEMENT_KEY_COMMENT = ELEMENT_KEY_BASIS_TRANSLATION + "<comment><ContentReadOnly>";

    private static final String NEWLINE_DB_VALUE = "\n";

    private TranslationsDataHandler dataHandler;
    private TranslationObject currentTranslationObject;
    private Set<DictTextKindTransitTypes> existingDictionaries;

    private final boolean importToDB = true;
    private final boolean doBufferSave = true;

    public TranslationsImporter(EtkProject project, String importerName) {
        super(project, importerName, null,
              new FilesImporterFileListType(TABLE_DA_DICT_META, importerName, true,
                                            false, false,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
        tableName = TABLE_DA_DICT_META;
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
        return importer.isRecordValid(importRec, errors);
    }


    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
        existingDictionaries = new HashSet<>();
    }

    @Override
    public boolean isAutoImport() {
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        if (!dataHandler.isValid()) {
            cancelImport(translateForLog("!!Keine gültige Übersetzungsdatei. Header Element <%1> nicht vorhanden.", HEADER_INFORMATION_XML_ELEMENT));
            return;
        }
        if (currentTranslationObject != null) {
            for (Map.Entry<String, String> entry : importRec.entrySet()) {
                switch (entry.getKey()) {
                    case ELEMENT_KEY_INLINE_TEXT:
                        currentTranslationObject.addTextModule(entry.getValue());
                        break;
                    case ELEMENT_KEY_COMMENT:
                        currentTranslationObject.setComment(entry.getValue());
                        break;
                    case ELEMENT_KEY_TEXT_ID:
                        String textId = entry.getValue();
                        if (StrUtils.isValid(textId)) {
                            currentTranslationObject.setTextId(textId);
                        } else {
                            skipDataSet(translateForLog("!!Text-ID darf nicht leer sein!"));
                        }
                        break;
                    case ELEMENT_KEY_TEXT_KIND_META_INFO:
                        currentTranslationObject.setTextKind(entry.getValue());
                        // Hat ein <Translation> Datensatz keine definierte Textart -> Datensatz überspringen
                        if (currentTranslationObject.getTextKind() == DictTextKindTransitTypes.UNKNOWN) {
                            skipDataSet(translateForLog("!!Ungültige Textart \"%1\"", entry.getValue()));
                            return;
                        }
                        // Lexikon-Check
                        if (!existingDictionaries.contains(currentTranslationObject.getTextKind())) {
                            DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
                            if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForiParts(getMessageLog(),
                                                                                                          getLogLanguage(),
                                                                                                          currentTranslationObject.getTextKind())) {
                                skipDataSet(translateForLog("!!Lexikon für Textart \"%1\" existiert nicht.", entry.getValue()));
                                return;
                            }
                        }
                        existingDictionaries.add(currentTranslationObject.getTextKind());
                        break;
                    case ELEMENT_KEY_MULTLINE_TEXT:
                        currentTranslationObject.addTextModule(NEWLINE_DB_VALUE);
                        break;
                    case ELEMENT_KEY_MULTILINE_SOURCE_TEXT:
                        currentTranslationObject.addSourceTextModule(NEWLINE_DB_VALUE);
                        break;
                }
            }
        }
    }

    @Override
    protected void postImportTask() {
        importCurrentTranslationObject();
        getMessageLog().fireMessage(translateForLog("!!%1 Translation-Datensätze verarbeitet",
                                                    String.valueOf(importedRecords)),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        currentTranslationObject = null;
        existingDictionaries = null;
        super.postImportTask();
    }

    private void skipDataSet(String message) {
        reduceRecordCount();
        getMessageLog().fireMessage(translateForLog("!!Datensatz mit TextId \"%1\" übersprungen. %2",
                                                    currentTranslationObject.getTextId(), message),
                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        currentTranslationObject = null;
    }

    /**
     * Importiert einen "<Translation>" Datensatz
     */
    private void importCurrentTranslationObject() {
        if (currentTranslationObject != null) {
            currentTranslationObject.saveAsMetaData();
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            dataHandler = new TranslationsDataHandler(this, new iPartsKeyValueRecordReader(importFile, tableName));
            return importMasterData(dataHandler);
        }
        return false;
    }

    private class TranslationObject {

        public static final String SUPPLEMENTARY_TEXTKIND_FALLBACK_VALUE = "supplementray text";

        private final StringBuilder translatedAndInlineTexts;
        private final StringBuilder sourceText;
        private String textId;
        private String comment;
        private DictTextKindTransitTypes textKind;

        public TranslationObject() {
            translatedAndInlineTexts = new StringBuilder();
            sourceText = new StringBuilder();
        }

        public void addTextModule(String translatedText) {
            if (translatedText != null) {
                this.translatedAndInlineTexts.append(translatedText);
            }
        }

        public void addSourceTextModule(String sourceText) {
            if (sourceText != null) {
                this.sourceText.append(sourceText);
            }
        }

        public String getCompleteTranslatedText() {
            return translatedAndInlineTexts.toString();
        }

        public String getTextId() {
            return textId;
        }

        public void setTextId(String textId) {
            this.textId = textId;
        }

        public String getSourceText() {
            return sourceText.toString();
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public DictTextKindTransitTypes getTextKind() {
            return textKind;
        }

        public void setTextKind(String xmlMetaInfo) {
            this.textKind = DictTextKindTransitTypes.getXMLType(xmlMetaInfo);
            if ((textKind == DictTextKindTransitTypes.UNKNOWN) && StrUtils.isValid(xmlMetaInfo) && xmlMetaInfo.endsWith(SUPPLEMENTARY_TEXTKIND_FALLBACK_VALUE)) {
                this.textKind = DictTextKindTransitTypes.SUPPLEMENTARY_TEXT;
            }
        }

        /**
         * Erzeugt aus den aufgesammelten Daten ein {@link iPartsDataDictMeta} Objekt und speichert es in der DB.
         */
        public void saveAsMetaData() {
            if ((getTextKind() != null) && StrUtils.isValid(getTextId())) {
                List<iPartsDictTextKindId> textKindIds = DictTxtKindIdByMADId.getInstance(getProject()).getiPartsTxtKinds(getTextKind());
                if ((textKindIds != null) && !textKindIds.isEmpty()) {
                    String iPartsDictId = getTextId();
                    // Enthält die eingelesene Datei eine in iParts erzeugte ID (Präfix "IPARTS")
                    boolean isIPartsTextId = StrUtils.isValid(iPartsDictId) && iPartsDictId.startsWith(iPartsDictPrefixAndSuffix.DICT_IPARTS_PREFIX.getPrefixValue());
                    if (!isIPartsTextId) {
                        iPartsDictId = DictHelper.makeIPARTSDictId(getTextKind(), getTextId());
                    }
                    Language targetLang = iPartsTransitMappingCache.getInstance(getProject()).getIsoLang(dataHandler.getTargetLang());
                    Language sourceLang = iPartsTransitMappingCache.getInstance(getProject()).getIsoLang(dataHandler.getSourceLang());
                    EtkMultiSprache multiLang = getProject().getDbLayer().getLanguagesTextsByTextId(iPartsDictId);
                    if (multiLang == null) {
                        multiLang = new EtkMultiSprache();
                        multiLang.setTextId(iPartsDictId);
                    }
                    boolean textChanged = false;
                    // Erst den übersetzen Text setzen
                    if ((targetLang != null)) {
                        if (translatedAndInlineTexts.length() != 0) {
                            textChanged = handleText(multiLang, targetLang, getCompleteTranslatedText().trim(), iPartsDictId, "übersetzte Text");
                        } else {
                            getMessageLog().fireMessage(translateForLog("!!Der übersetzte Text für Text-ID \"%1\" und Sprache \"%2\" ist leer!",
                                                                        iPartsDictId, dataHandler.getTargetLang()),
                                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        }
                    } else {
                        getMessageLog().fireMessage(translateForLog("!!Für Text-ID \"%1\" konnte die " +
                                                                    "Zielsprache nicht gefunden werden. Eintrag in XML Datei: %2",
                                                                    iPartsDictId, dataHandler.getTargetLang()),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                    MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }
                    // Jetzt den Ausgangstext setzen
                    if (sourceLang != null) {
                        textChanged |= handleText(multiLang, sourceLang, getSourceText(), iPartsDictId, "Ausgangstext");
                    } else {
                        getMessageLog().fireMessage(translateForLog("!!Für Text-ID \"%1\" konnte die " +
                                                                    "Ausgangssprache nicht gefunden werden. Eintrag in XML Datei: %2",
                                                                    iPartsDictId, dataHandler.getSourceLang()),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                    MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }
                    // Statt dem spezifischen TranslationJob alle TranslationJobs zur TextId und JobId laden
                    iPartsDataDictTransJobList transJobList = iPartsDataDictTransJobList.getJobsByTextAndJobId(getProject(), iPartsDictId, dataHandler.getJobId());
                    iPartsDataDictTransJob transJob = null;
                    if ((targetLang != null) && (sourceLang != null)) {
                        // Den für die Zielsprache spezifischen Eintrag suchen
                        transJob = transJobList.getAsList()
                                .stream()
                                .filter(jobEntry -> jobEntry.hasSameLanguages(sourceLang, targetLang))
                                .findFirst()
                                .orElse(null);
                    } else {
                        getMessageLog().fireMessage(translateForLog("!!Für Text-ID \"%1\" konnte kein Eintrag " +
                                                                    "in DA_DICT_TRANS_JOB gesucht werden, weil beide Sprachen " +
                                                                    "nicht extrahiert werden konnten. Zielsprache: %2, Ausgangssprache %3",
                                                                    iPartsDictId,
                                                                    ((targetLang == null) ? "" : targetLang.getCode()),
                                                                    ((sourceLang == null) ? "" : sourceLang.getCode())),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }

                    boolean multiLangAdded = false;
                    for (iPartsDictTextKindId textKindId : textKindIds) {
                        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), iPartsDictId);
                        iPartsDataDictMeta dictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
                        if (!dictMeta.existsInDB()) {
                            // Wenn eine von iParts generierte ID gefunden wurde, dann muss ein bestehender Text ergänzt
                            // werden. Das bedeutet, dass neue Einträge für gemappte Textarten nicht erzeugt werden sollen.
                            if (isIPartsTextId) {
                                getMessageLog().fireMessage(translateForLog("!!Für iParts Text-ID \"%1\" " +
                                                                            "existiert zur Textart \"%2\" kein Eintrag " +
                                                                            "in der DB. DictMeta ID. %3.",
                                                                            iPartsDictId, textKindId.getTextKindId(),
                                                                            dictMetaId.toStringForLogMessages()),
                                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                                continue;
                            }
                            dictMeta.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                            dictMeta.setForeignId(getTextId(), DBActionOrigin.FROM_EDIT);
                            dictMeta.setSource(DictHelper.getIPartsSourceForCurrentSession(), DBActionOrigin.FROM_EDIT);
                            dictMeta.setActCreationDate(DBActionOrigin.FROM_EDIT);
                            dictMeta.setActChangeDate(DBActionOrigin.FROM_EDIT);
                            dictMeta.setState(DictHelper.getMADDictStatus(), DBActionOrigin.FROM_EDIT);
                        }
                        if (textChanged) {
                            dictMeta.setActChangeDate(DBActionOrigin.FROM_EDIT);
                        }
                        // Das neue MultiLang Objekt, darf nur bei einem MetaDict Objekt gesetzt werden, da es sonst beim
                        // gemeinsamen Speichern zu INSERT Problemen kommen würde
                        // {@link iPartsDictLanguageMetaId} besteht nur aus der TextId und der Sprache
                        if (!multiLangAdded) {
                            dictMeta.setNewMultiLang(multiLang);
                            dictMeta.setTranslationStateForLanguage(iPartsDictSpracheTransState.TRANSLATION_RECEIVED, targetLang, DBActionOrigin.FROM_EDIT);
                            if ((transJob != null) && importToDB) {
                                if (transJob.existsInDB()) {
                                    transJob.updateTranslationJob(iPartsDictTransJobStates.TRANSLATED);
                                    transJob.saveToDB();
                                    // Nachdem der DA_DICT_TRANS_JOB Eintrag aktualisiert wurde, muss geprüft werden, ob
                                    // der Text schon in alle Sprachen übersetzt wurde. Falls ja, wird der DE Eintrag auf
                                    // "TRANSLATION_RECEIVED" gesetzt
                                    checkIfAllTextTranslated(dictMeta, transJobList);
                                } else {
                                    getMessageLog().fireMessage(translateForLog("!!Für Text-ID \"%1\" wurde" +
                                                                                " keine Eintrag in DA_DICT_TRANS_JOB gefunden." +
                                                                                " Zielsprache: %2, Ausgangssprache %3," +
                                                                                " DictMeta ID. %4.",
                                                                                iPartsDictId,
                                                                                ((targetLang == null) ? "" : targetLang.getCode()),
                                                                                ((sourceLang == null) ? "" : sourceLang.getCode()),
                                                                                dictMetaId.toStringForLogMessages()),
                                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                                MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                                }
                            } else {
                                getMessageLog().fireMessage(translateForLog("!!Für iParts Text-ID \"%1\" " +
                                                                            "wird der Datensatz in DA_DICT_TRANS_JOB " +
                                                                            "nicht aktualisiert, weil er nicht erzeugt werden konnte.",
                                                                            iPartsDictId, textKindId.getTextKindId()),
                                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                            }
                            multiLangAdded = true;
                        }
                        if (importToDB) {
                            // alle Verwendungen speichern/aktualisieren
                            dictMeta.setSaveAllUsages(true);
                            saveToDB(dictMeta);
                        }
                    }
                } else {
                    getMessageLog().fireMessage(translateForLog("!!Für Text-ID \"%1\" konnten keine Textarten " +
                                                                "gefunden werden. Textart in XML: %2",
                                                                getTextId(), getTextKind().getDbValue()),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                }
            } else {
                getMessageLog().fireMessage(translateForLog("!!Textart für Text-ID \"%1\" konnten aus der" +
                                                            " Importdatei nicht extrahiert werden!",
                                                            getTextId()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
        }

        /**
         * Setzt den deutschen Spracheintrag auf "TRANSLATION_RECEIVED" falls die Texte zu allen Sprachen übersetzt
         * bzw. die Antworten zu allen Sprachen empfangen wurden
         *
         * @param dictMeta
         * @param transJobList
         */
        private void checkIfAllTextTranslated(iPartsDataDictMeta dictMeta, iPartsDataDictTransJobList transJobList) {
            if (allTextTranslated(transJobList)) {
                dictMeta.setTranslationStateForLanguage(iPartsDictSpracheTransState.TRANSLATION_RECEIVED, Language.DE, DBActionOrigin.FROM_EDIT);
            }
        }

        /**
         * Liefert zurück, ob alle der zu übersetzende Spracheinträge vom Übersetzer übersetzt wurden bzw. zurückgekommen
         * sind.
         *
         * @param transJobList
         * @return
         */
        private boolean allTextTranslated(iPartsDataDictTransJobList transJobList) {
            return transJobList.getAsList().stream().allMatch(transJobEntry -> iPartsDictTransJobStates.isTranslationCompletedState(transJobEntry.getTranslationState()));
        }

        /**
         * Setzt den übergebenen Text zur übergebenen Sprache. Erzeugt eine Fehlermeldung, falls ein Text schon vorhanden
         * ist und vom neuen Text abweicht.
         *
         * @param multiLang
         * @param targetLang
         * @param importText
         * @param iPartsDictId
         * @param placeholder
         */
        private boolean handleText(EtkMultiSprache multiLang, Language targetLang, String importText, String iPartsDictId, String placeholder) {
            boolean textChanged = false;
            String currentText = multiLang.getText(targetLang.getCode());
            if (StrUtils.isValid(currentText) && !currentText.equals(importText)) {
                getMessageLog().fireMessage(translateForLog("!!Für Text-ID \"%1\" weicht der %2 vom vorhandenen " +
                                                            "Text ab! Aktuell: \"%3\", Neu: \"%4\"",
                                                            iPartsDictId, placeholder, currentText, importText),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                textChanged = true;
            }
            multiLang.setText(targetLang, importText);
            return textChanged;
        }
    }

    private class TranslationsDataHandler extends BigDataXMLHandler {

        private static final String ATT_TARGET_LANG = "TargetLang";
        private static final String ATT_SOURCE_LANG = "SourceLang";
        private static final String ATT_JOB_ID = "jobid";

        private Map<String, String> translationsAttrbutes;

        public TranslationsDataHandler(AbstractSAXPushDataImporter importer, AbstractKeyValueRecordReader reader) {
            super(importer, reader);
        }

        @Override
        protected void onStartElement(String uri, String localName, String qName, Map<String, String> attributes) {
            // Beginnt ein neuer Datensatz, dann muss der bisherige gespeichert werden
            if (qName.equals(SINGLE_DATASET_XML_ELEMENT)) {
                importCurrentTranslationObject();
                currentTranslationObject = new TranslationObject();
            } else if (qName.equals(HEADER_INFORMATION_XML_ELEMENT) && (attributes != null) && !attributes.isEmpty()) {
                translationsAttrbutes = attributes;
            }
        }

        @Override
        protected void onTextElement(String tagName, char[] ch, int start, int length) {
            // Der übersetze Text kann wiederum XML-Elemente enthalten, die sprachneutrale Texte wiederspiegeln.
            // In so einem Falls muss der sprachneutrale Text aus dem Unterelement an den eigentlichen Text gehängt werden.
            if (currentTranslationObject != null) {
                if (tagName.equals(TRANSLATED_TEXT_MODULE_XML_ELEMENT) && (ch.length > 0)) {
                    currentTranslationObject.addTextModule(new String(ch, start, length));
                } else if (tagName.equals(SOURCE_TEXT_MODULE_XML_ELEMENT) && (ch.length > 0)) {
                    currentTranslationObject.addSourceTextModule(new String(ch, start, length));
                }
            }
        }

        @Override
        protected void onEndElement(String uri, String localName, String qName) {
        }

        public String getTargetLang() {
            return getAttributeValue(ATT_TARGET_LANG, translationsAttrbutes);
        }

        public String getSourceLang() {
            return getAttributeValue(ATT_SOURCE_LANG, translationsAttrbutes);
        }

        public String getJobId() {
            return getAttributeValue(ATT_JOB_ID, translationsAttrbutes);
        }

        public boolean isValid() {
            return attributesLoaded(translationsAttrbutes);
        }
    }
}
