/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.EPCFootnoteType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataEPCFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsEPCFootNoteContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEPCLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public abstract class AbstractEPCFootnoteImporter extends AbstractDataImporter implements iPartsConst {

    private static final String FOOTNOTE_DESCIDX = "DESCIDX";
    private static final String FOOTNOTE_LANG = "LANG";
    private static final String FOOTNOTE_SEQNUM = "SEQNUM";
    private static final String FOOTNOTE_ABBR = "ABBR";
    private static final String FOOTNOTE_TEXT = "TEXT";
    private static final String TABLENAME = TABLE_DA_EPC_FN_CONTENT;

    private String[] headerNames = {
            FOOTNOTE_DESCIDX,
            FOOTNOTE_LANG,
            FOOTNOTE_SEQNUM,
            FOOTNOTE_ABBR,
            FOOTNOTE_TEXT
    };

    private boolean doBufferSave = true;
    private boolean importToDB = true;
    private iPartsDictTextKindId txtKindId;
    private TextIdAndTexts currentTextIdAndTexts;

    public AbstractEPCFootnoteImporter(EtkProject project, String importName, String fileListName) {
        super(project, importName,
              new FilesImporterFileListType(TABLENAME, fileListName, true, true, true,
                                            new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_GZ,
                                                          MimeTypes.EXTENSION_ALL_FILES }));
        txtKindId = initTextKindIdForImport();
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
        if (txtKindId == null) {
            return false;
        }

        // Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForEPC(getMessageLog(), getLogLanguage(),
                                                                                   getEPCFootnoteTextKindType())) {
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
        EPCFootnoteImportHelper importHelper = new EPCFootnoteImportHelper(getProject());
        iPartsEPCLanguageDefs langDef = iPartsEPCLanguageDefs.getType(importRec.get(FOOTNOTE_LANG));
        if ((langDef == iPartsEPCLanguageDefs.EPC_UNKNOWN)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Sprache \"%2\" übersprungen", String.valueOf(recordNo),
                                                        importRec.get(FOOTNOTE_LANG)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        // Die Original EPC ID
        String foreignTextId = importHelper.handleValueOfSpecialField(FOOTNOTE_DESCIDX, importRec);
        // Text
        String text = importHelper.handleValueOfSpecialField(FOOTNOTE_TEXT, importRec);
        // #-Platzhalter, wie z.B. #AF
        String placeholderSign = importHelper.handleValueOfSpecialField(FOOTNOTE_ABBR, importRec);
        // Sequenznummer für mehrzeilige Fußnotentexte
        String seqNumber = importHelper.handleValueOfSpecialField(FOOTNOTE_SEQNUM, importRec);

        if (currentTextIdAndTexts != null) {
            // Check, ob eine neue ID kommt und der bisherige Datensatz gespeichert werden muss.
            if (!currentTextIdAndTexts.epcTextId.equals(foreignTextId)) {
                storeCurrentText();
                currentTextIdAndTexts = new TextIdAndTexts(foreignTextId);
            }
        } else {
            currentTextIdAndTexts = new TextIdAndTexts(foreignTextId);
        }
        currentTextIdAndTexts.addText(seqNumber, langDef, text, placeholderSign);

    }

    /**
     * Speichert den aufgesammelten Fußnotentext in der DB
     */
    private void storeCurrentText() {
        String termID = currentTextIdAndTexts.getEpcTextId();
        DictImportTextIdHelper dictImportTextIdHelper = new DictImportTextIdHelper(getProject());
        EtkMultiSprache multiLangObject = new EtkMultiSprache();
        // Set für Sprachen, die in einer Zeile keinen neutralen und keinen sprachspezifischen Text haben.
        // Für solche Sprachen werden keine Texte angelegt.
        Set<iPartsEPCLanguageDefs> languagesWithNoContent = new HashSet<>();
        // Durchlaufe alle Sequenznummern (Nummer an denen man erkennt welche Zeile pro Sprache welchen Text erhält)
        for (Map.Entry<String, Map<iPartsEPCLanguageDefs, String>> seqNumberToTextEntry : currentTextIdAndTexts.getSortedSeqNumberToLangMap().entrySet()) {
            Map<iPartsEPCLanguageDefs, String> langToOriginalSeqNumberMap = seqNumberToTextEntry.getValue();
            // Falls die Sequenznummer einen neutralen Text hat, dann wird er hier bestimmt. Neutrale Texte dienen als
            // Fallback für Sprachen, die keinen sprachspezifischen Text haben (pro Sequenznummer bzw Zeile).
            String neutalText = langToOriginalSeqNumberMap.get(iPartsEPCLanguageDefs.EPC_NEUTRAL);
            // Durchlaufe alle Sprachen und befülle sie mit dem sprachspezifischen oder neutralen Text
            for (iPartsEPCLanguageDefs languageDef : iPartsEPCLanguageDefs.values()) {
                if ((languageDef == iPartsEPCLanguageDefs.EPC_NEUTRAL) || (languageDef == iPartsEPCLanguageDefs.EPC_UNKNOWN)
                    || languagesWithNoContent.contains(languageDef)) {
                    continue;
                }
                // Bestimme den sprachspezifischen Text.
                String textForLanguage = langToOriginalSeqNumberMap.get(languageDef);
                // Der aktuelle bzw. schon aufgesammelte Text
                String currentTextForLanguage = multiLangObject.getText(languageDef.getDbValue().getCode());
                if (!StrUtils.isEmpty(textForLanguage, neutalText)) {
                    // Wenn der neutrale oder der sprachspezifische Text existiert, dann hänge den jeweiligen Text an
                    // den bestehenden Text an.
                    currentTextForLanguage = addFootnoteText(currentTextForLanguage, textForLanguage, neutalText);
                    // Kann eigentlich nicht passieren
                    if (StrUtils.isEmpty(currentTextForLanguage)) {
                        getMessageLog().fireMessage(translateForLog("!!Für die TextId \"%1\" und Sprache \"%2\" " +
                                                                    "konnte kein Texte erzeugt werden. Text zur Sprache: %3, " +
                                                                    "Neutraler Text: %4", termID, languageDef.getLangEPC(),
                                                                    textForLanguage, neutalText),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }
                } else {
                    // Existiert zur aktuellen Sprache kein Text (weder neutral, noch sprachspezifisch), dann lösche für
                    // die aktuelle Sprache die bisherigen Textbausteine und lege die Sprache ins Set der Sprachen ohne
                    // Text.
                    multiLangObject.removeLanguage(languageDef.getDbValue().getCode());
                    languagesWithNoContent.add(languageDef);
                    continue;
                }
                multiLangObject.setText(languageDef.getDbValue(), currentTextForLanguage);
            }
        }
        if (!multiLangObject.allStringsAreEmpty()) {
            // Lege den Text im Lexikon an
            if (dictImportTextIdHelper.handleDictTextIdForEPC(getEPCFootnoteTextKindType(), multiLangObject, termID,
                                                              DictHelper.getEPCForeignSource(),
                                                              TableAndFieldName.make(TABLE_DA_EPC_FN_CONTENT, FIELD_DEFC_TEXT))) {
                // Im Lexikon anlegen führte zu keinen Problemen -> Eigentliches Dataobject anlegen
                iPartsEPCFootNoteContentId footNoteContentId = new iPartsEPCFootNoteContentId(getEPCFootnoteType(), termID, 1);
                iPartsDataEPCFootNoteContent footNoteContent = new iPartsDataEPCFootNoteContent(getProject(), footNoteContentId);
                if (!footNoteContent.existsInDB()) {
                    footNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                } else {
                    // Nachladen der MultiuLang
                    footNoteContent.getFieldValueAsMultiLanguage(FIELD_DEFC_TEXT);
                }
                footNoteContent.setFieldValueAsMultiLanguage(FIELD_DEFC_TEXT, multiLangObject, DBActionOrigin.FROM_EDIT);

                // Eigentlich bräuchten wir die Platzhalter nicht, da sie direkt an den Text geschrieben werden (damit unsere
                // bisherige Logik greift). Leider gibt es auch einen Platzhalter für Farbtabellen mit dem Wert "F". Da noch
                // nicht klar ist, was damit passiert, werden alle Platzhalter einer Fußnote in einer kommaseparierten Liste
                // gespeichert.
                if (currentTextIdAndTexts.hasPlaceholderSigns()) {
                    footNoteContent.setFieldValue(FIELD_DEFC_ABBR, currentTextIdAndTexts.getPlaceholderSignsAsString(), DBActionOrigin.FROM_EDIT);
                }
                if (importToDB) {
                    if (!saveToDB(footNoteContent)) {
                        skippedRecords += multiLangObject.getSprachenCount() - 1;
                    }
                }
            }
        } else {
            skippedRecords += multiLangObject.getSprachenCount() - 1;
        }
    }


    /**
     * Fügt dem aktuellen Text den neuen Textbaustein hinzu
     *
     * @param currentTextForLanguage
     * @param textForLanguage
     * @param neutalText
     * @return
     */
    private String addFootnoteText(String currentTextForLanguage, String textForLanguage, String neutalText) {
        String textToAdd = null;
        if (StrUtils.isValid(textForLanguage)) {
            textToAdd = textForLanguage;
        } else if (StrUtils.isValid(neutalText)) {
            textToAdd = neutalText;
        }
        if (StrUtils.isValid(textToAdd)) {
            if (StrUtils.isEmpty(currentTextForLanguage)) {
                return textToAdd;
            } else {
                return currentTextForLanguage + "\n" + textToAdd;
            }
        }
        return currentTextForLanguage;
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (currentTextIdAndTexts != null) {
                storeCurrentText();
            }
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLENAME)) {
            getProject().getDbLayer().delete(TABLENAME, new String[]{ FIELD_DEFC_TYPE }, new String[]{ getEPCFootnoteType().getDBValue() });
            return true;
        } else {
            return false;
        }
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

    protected abstract DictTextKindEPCTypes getEPCFootnoteTextKindType();

    protected abstract EPCFootnoteType getEPCFootnoteType();

    private class EPCFootnoteImportHelper extends EPCImportHelper {

        public EPCFootnoteImportHelper(EtkProject project) {
            super(project, new HashMap<String, String>(), TABLENAME);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }
            return value;
        }
    }

    private class TextIdAndTexts {

        // Aktuelle EPC TextId
        private String epcTextId;
        // Map mit EPC Sequenznummer auf eine Map mit Sprache auf Text
        private Map<String, Map<iPartsEPCLanguageDefs, String>> seqNumberToLangMap;
        // Map mit Sequenznummer (Zeile) auf ABBR Wert (#-Platzhalter). Da pro Fußnote mehrere vokommen können, hier eine Map
        private Map<String, String> seqPlaceHolderSignMap;

        public TextIdAndTexts(String epcTextId) {
            this.epcTextId = epcTextId;
            seqNumberToLangMap = new TreeMap<>();
            seqPlaceHolderSignMap = new TreeMap<>();
        }

        /**
         * Füge einen Text zur richtigen Sequenznummer und Sprache hinzu.
         * WICHTIG: Um die Reihenfolge richtig zu halten werden SortStrings verwendet (aus {@link Utils}).
         * Weil eine TreeMap 10000 direkt hinter 1000 platziert und nicht hinter z.B. 2000.
         *
         * @param originalSeqNumber
         * @param langDef
         * @param text
         * @param placeholderSign
         */
        public void addText(String originalSeqNumber, iPartsEPCLanguageDefs langDef, String text, String placeholderSign) {
            if (StrUtils.isEmpty(originalSeqNumber)) {
                reduceRecordCount();
                return;
            }
            // Nach der initialen Logik hat man die Zeilenzugehörigkeit bei verschiedenen Sprachen über die ersten vier
            // Stellen der Sequenznummer hergestellt. Das hat bei gleich langen Sequenznummern sehr gut und bei ungleich
            // langen gar nicht funktioniert. Jetzt werden die letzten zwei Stellen der Sequenznummer entfernt und aus dem
            // Ergebnis ein sortierbarer String gemacht, z.B.
            //  100000 N (sprachneutral)    -> erste Zeile, weil 1000
            //  100007 J (japanisch)        -> erste Zeile, weil 1000
            //  200000 N (sprachneutral)    -> zweite Zeile, weil 2000
            //  200007 J (japanisch)        -> zweite Zeile, weil 2000
            // 1000000 N (sprachneutral)    -> dritte Zeile, weil 10000
            // ----------------------------------------------------------
            // Der mehrzeilige Text wäre dementsprechend für J:
            //  100007
            //  200007
            // 1000000
            // Für alle anderen Sprachen:
            //  100000
            //  200000
            // 1000000
            String shortSeqNumber = Utils.toSortString(StrUtils.copySubString(originalSeqNumber, 0, (originalSeqNumber.length() - 2)));
            Map<iPartsEPCLanguageDefs, String> langToTextMap = seqNumberToLangMap.get(shortSeqNumber);
            if (langToTextMap == null) {
                langToTextMap = new TreeMap<>();
                seqNumberToLangMap.put(shortSeqNumber, langToTextMap);
            }

            // Es werden alle möglichen Platzhalter an den Datensatz geschrieben
            if (StrUtils.isValid(placeholderSign)) {
                seqPlaceHolderSignMap.put(originalSeqNumber, placeholderSign);
            }
            // In den Text werden aber nur die #-Platzhalter geschrieben
            if (isHashTagPlaceholder(placeholderSign)) {
                // Existiert ein ABBR Wert (#-Platzhalter), dann schreibe ihn direkt vor den eigentlichen Text
                if (StrUtils.stringStartsWith(text, ' ', true)) {
                    langToTextMap.put(langDef, placeholderSign + text);
                } else {
                    // Check, ob es sich nur um einen #-Platzhalter handelt. Falls ja, dann kein Leerzeichen ranhängen
                    text = StrUtils.makeDelimitedString(" ", placeholderSign, text);
                    if (text.trim().equals(placeholderSign)) {
                        text = placeholderSign;
                    }
                    langToTextMap.put(langDef, text);
                }
            } else {
                langToTextMap.put(langDef, text);
            }
        }

        private boolean isHashTagPlaceholder(String placeholderSign) {
            return StrUtils.isValid(placeholderSign) && (placeholderSign.length() == 3) && placeholderSign.startsWith("#");
        }

        public String getEpcTextId() {
            return epcTextId;
        }

        public Map<String, Map<iPartsEPCLanguageDefs, String>> getSortedSeqNumberToLangMap() {
            return seqNumberToLangMap;
        }

        public String getPlaceholderSignsAsString() {
            if (!seqPlaceHolderSignMap.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (String placeholder : seqPlaceHolderSignMap.values()) {
                    if (builder.length() != 0) {
                        builder.append("|");
                    }
                    builder.append(placeholder);
                }
                return builder.toString();
            }
            return "";
        }

        public boolean hasPlaceholderSigns() {
            return !seqPlaceHolderSignMap.isEmpty();
        }
    }
}
