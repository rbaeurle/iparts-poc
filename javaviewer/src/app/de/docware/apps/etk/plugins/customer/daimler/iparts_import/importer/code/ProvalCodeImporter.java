/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.code;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractFilesImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.code.Series;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.code.SeriesCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWInputStream;
import de.docware.util.misc.CompressionUtils;
import org.apache.commons.collections4.map.LRUMap;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Importer für Code samt Benennungen aus dem PROVAL System
 */
public class ProvalCodeImporter extends AbstractFilesImporter implements iPartsConst {

    public ProvalCodeImporter(EtkProject project) {
        super(project, PROVAL_CODE_IMPORT_NAME, new FilesImporterFileListType(TABLE_DA_CODE, PROVAL_CODE_IMPORT_NAME, false, false, true, new String[]{ MimeTypes.EXTENSION_ZIP }));
        setBufferedSave(true);
    }


    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_CODE)) {
            getMessageLog().fireMessage(translateForLog("!!Lösche alle Code zur Quelle %1...", iPartsImportDataOrigin.PROVAL.getOrigin()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            getProject().getDB().delete(TABLE_DA_CODE, new String[]{ FIELD_DC_SOURCE }, new String[]{ iPartsImportDataOrigin.PROVAL.getOrigin() });
            return true;
        }
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        // Caches nicht löschen
        setClearCachesAfterImport(false);
        String fileName = importFile.extractFileName(true);
        try {
            Genson genson = JSONUtils.createGenson(true);
            DictImportTextIdHelper dictImportTextIdHelper = new DictImportTextIdHelper(getProject());
            DWFile destDir = DWFile.createTempDirectory("daim");
            if (destDir != null) {
                if (CompressionUtils.unzipFile(destDir.getAbsolutePath(), importFile.getAbsolutePath(), "UTF-8")) {
                    List<DWFile> allSeries = destDir.listDWFilesRecursively((dir, name) -> StrUtils.stringEndsWith(name, MimeTypes.EXTENSION_JSON, true));
                    getMessageLog().fireMessage(translateForLog("!!Starte Import der Code-Benennungen für %1 Baureihen", String.valueOf(allSeries.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    Map<String, TextWithCode> alreadyImportedText = new LRUMap(10000);
                    for (DWFile seriesJsonFile : allSeries) {
                        if (isCancelled()) {
                            dictImportTextIdHelper.cancelRunningSearch();
                            break;
                        }
                        importSeriesJSONFile(seriesJsonFile, genson, dictImportTextIdHelper, alreadyImportedText);
                    }
                    // Hier muss Post aufgerufen werden, da der Importer nicht über die normale importRecord() Methode
                    // läuft. Die letzten <100 Code werden sonst nicht importiert.
                    postImportTask();
                    // Temporäres Verzeichnis löschen
                    if (!destDir.deleteRecursivelyWithRepeat(5000)) {
                        getMessageLog().fireMessage(translateForLog("!!Fehler beim Löschen des temporären Verzeichnis \"%1\"", destDir.getAbsolutePath()), MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                        return false;
                    }
                    return true;
                } else {
                    cancelImport(translateForLog("!!Fehler beim Entpacken der Code-Benennung Datei \"%1\"", fileName), MessageLogType.tmlError);
                }
            }
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            getMessageLog().fireMessage(translateForLog("!!Fehler beim Importieren der Datei \"%1\"", fileName));
        }
        return false;
    }

    /**
     * Importiert die Code Benennungen für eine komplette Baureihe
     *
     * @param jsonFile
     * @param genson
     * @param dictImportHelper
     * @param alreadyImportedText
     */
    private void importSeriesJSONFile(DWFile jsonFile, Genson genson, DictImportTextIdHelper dictImportHelper, Map<String, TextWithCode> alreadyImportedText) {
        if (jsonFile == null) {
            return;
        }
        String seriesNo = "";
        try (DWInputStream inputStream = jsonFile.getInputStream()) {
            Series series = genson.deserialize(inputStream, Series.class);
            seriesNo = getSeriesNo(series, jsonFile.extractFileName(true));
            getMessageLog().fireMessage(translateForLog("!!Starte Import der Code-Benennungen für Baureihe" +
                                                        " \"%1\"", seriesNo),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            if (seriesNo == null) {
                return;
            }
            List<SeriesCode> codes = getSeriesCodes(series, seriesNo);
            if (codes == null) {
                return;
            }
            int counter = 0;
            for (SeriesCode seriesCode : codes) {
                if (isCancelled()) {
                    dictImportHelper.cancelRunningSearch();
                    break;
                }
                counter++;
                String sdata = getSdataValue(seriesCode, seriesNo);
                String sdatb = getSdatbValue(seriesCode, seriesNo);
                if ((sdata == null) || (sdatb == null)) {
                    continue;
                }

                iPartsDataCode dataCode = getDataCodeObject(seriesCode, seriesNo, sdata, sdatb);
                EtkMultiSprache multiLang = createFilledMultiLang(seriesCode);
                String searchText = multiLang.getText(Language.DE.getCode());
                TextWithCode existingMultiLang = alreadyImportedText.get(searchText);
                if (existingMultiLang != null) {
                    String codeId = dataCode.getAsId().getCodeId();
                    correctTextContent(searchText, dictImportHelper, existingMultiLang, multiLang, codeId);
                    existingMultiLang.addCode(codeId);
                    dataCode.setFieldValueAsMultiLanguage(FIELD_DC_DESC, existingMultiLang.getText(), DBActionOrigin.FROM_EDIT);
                } else {
                    // Dictionary Eintrag anlegen bzw aktualisieren
                    boolean dictSuccessful = dictImportHelper.handlePROVALCodeTextId(multiLang);
                    if (dictSuccessful) {
                        dataCode.setFieldValueAsMultiLanguage(FIELD_DC_DESC, multiLang, DBActionOrigin.FROM_EDIT);
                        alreadyImportedText.putIfAbsent(searchText, new TextWithCode(multiLang, dataCode.getAsId().getCodeId()));
                    } else {
                        getMessageLog().fireMessage(translateForLog("!!Fehler beim importieren der Benennung für " +
                                                                    "Code \"%1\". Benennungen werden nicht importiert.",
                                                                    seriesCode.getShortName()));
                    }
                }
                handleDictionaryMessages(dictImportHelper, seriesCode);
                saveToDB(dataCode);
                getMessageLog().fireProgress(counter, codes.size(), "", true, true);
            }
            getMessageLog().fireMessage(translateForLog("!!Für die Baureihe \"%1\" wurden %2 Code verarbeitet.", seriesNo, String.valueOf(codes.size())), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        } catch (IOException e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            getMessageLog().fireMessage(translateForLog("!!Fehler beim Importieren der Code die Baureihe \"%1\"", seriesNo));
        }
    }

    /**
     * Erzeugt ein mit den Importtexten befülltes {@link EtkMultiSprache} Objekt
     *
     * @param seriesCode
     * @return
     */
    private EtkMultiSprache createFilledMultiLang(SeriesCode seriesCode) {
        EtkMultiSprache multiLang = new EtkMultiSprache();
        seriesCode.getDescriptions().forEach(codeDescription -> {
            multiLang.setText(Language.findLanguage(codeDescription.getLang()), codeDescription.getContent());
        });
        return multiLang;
    }

    /**
     * Gibt Warnungen oder Fehler aus, die während dem Lexikon-Prozess aufgetreten sind
     *
     * @param dictImportHelper
     * @param seriesCode
     */
    private void handleDictionaryMessages(DictImportTextIdHelper dictImportHelper, SeriesCode seriesCode) {
        if (dictImportHelper.hasWarnings()) {
            getMessageLog().fireMessage(translateForLog("!!Import der Baureihe \"%1\" enthielt Warnungen. " +
                                                        "Siehe Import-Log für mehr Informationen.", seriesCode.getShortName()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            dictImportHelper.getWarnings().forEach(warning -> getMessageLog().fireMessage(warning, MessageLogType.tmlWarning, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP));
        }
        if (dictImportHelper.hasInfos()) {
            dictImportHelper.getInfos().forEach(info -> getMessageLog().fireMessage(info, MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP));
        }
        dictImportHelper.clearWarningsAndInfos();
    }

    /**
     * Erzeugt und lädt das {@link iPartsDataCode} Objekt
     *
     * @param seriesCode
     * @param seriesNo
     * @param sdata
     * @param sdatb
     * @return
     */
    private iPartsDataCode getDataCodeObject(SeriesCode seriesCode, String seriesNo, String sdata, String sdatb) {
        iPartsCodeDataId codeDataId = new iPartsCodeDataId(seriesCode.getShortName(), seriesNo, "", sdata, iPartsImportDataOrigin.PROVAL);
        iPartsDataCode dataCode = new iPartsDataCode(getProject(), codeDataId);
        if (!dataCode.existsInDB()) {
            dataCode.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        }
        dataCode.setFieldValue(FIELD_DC_SDATB, sdatb, DBActionOrigin.FROM_EDIT);
        return dataCode;
    }

    /**
     * Liefert die Code zur übergebenen Baureihe
     *
     * @param series
     * @param seriesNo
     * @return
     */
    private List<SeriesCode> getSeriesCodes(Series series, String seriesNo) {
        List<SeriesCode> codes = series.getCodes();
        if ((codes == null) || codes.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Die Baureihe \"%1\" enthält keine Code. Die " +
                                                        "Baureihe wird übersprungen.", seriesNo),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            return null;
        }
        return codes;
    }

    /**
     * Liefert die Baureihennummer zum übergebenen {@link Series} Objekt
     *
     * @param series
     * @param filename
     * @return
     */
    private String getSeriesNo(Series series, String filename) {
        String seriesNo = series.getSeries();
        if (seriesNo == null) {
            getMessageLog().fireMessage(translateForLog("!!Aus der Datei \"%1\" konnte die Baureihe " +
                                                        "nicht extrahiert werden. Die Datei wird übersprungen.",
                                                        filename),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            return null;
        }
        return seriesNo;
    }

    /**
     * Liefert das "Datum ab" aus den Importdaten
     *
     * @param seriesCode
     * @param seriesNo
     * @return
     */
    private String getSdataValue(SeriesCode seriesCode, String seriesNo) {
        String sdata = seriesCode.getValidFrom();
        sdata = formatDateTimeValue(sdata);
        if (sdata == null) {
            getMessageLog().fireMessage(translateForLog("!!Für den Code \"%1\" der Baureihe \"%2\" " +
                                                        "konnte das \"Datum ab\" nicht geparst werden. Wert: \"%3\"",
                                                        seriesCode.getShortName(), seriesNo, seriesCode.getValidFrom()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return null;
        }
        return sdata;
    }

    /**
     * Liefert das "Datum bis" aus den Importdaten
     *
     * @param seriesCode
     * @param seriesNo
     * @return
     */
    private String getSdatbValue(SeriesCode seriesCode, String seriesNo) {
        String sdatb = seriesCode.getValidTo();
        sdatb = formatDateTimeValue(sdatb);
        if (sdatb == null) {
            getMessageLog().fireMessage(translateForLog("!!Für den Code \"%1\" der Baureihe \"%2\" " +
                                                        "konnte das \"Datum bis\" nicht geparst werden. Wert: \"%3\"",
                                                        seriesCode.getShortName(), seriesNo, seriesCode.getValidTo()),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return null;
        }
        return sdatb;
    }

    /**
     * Formatiert das PROVAL Eingabedatum in unser DB Datum
     * Vorher: 1980-12-31
     * Nachher: 19801231000000
     *
     * @param dateValue
     * @return
     */
    private String formatDateTimeValue(String dateValue) {
        String result = "";
        if (StrUtils.isValid(dateValue)) {
            result = DateUtils.formatDateTime(dateValue, DateUtils.simpleDateFormatIso, DateUtils.simpleTimeFormatyyyyMMddHHmmss);
            if (StrUtils.isEmpty(result)) {
                return null;
            }
        }
        return result;
    }

    /**
     * Überprüft, ob die Textobjekte den gleichen Textinhalt haben. Falls nicht, wird das bisherige Textobjekt angepasst.
     *
     * @param searchText
     * @param dictImportHelper
     * @param textWithCode
     * @param importMultiLang
     * @param newCode
     */
    private void correctTextContent(String searchText, DictImportTextIdHelper dictImportHelper, TextWithCode textWithCode,
                                    EtkMultiSprache importMultiLang, String newCode) {
        EtkMultiSprache existingMultiLang = textWithCode.getText();
        if (!existingMultiLang.equalText(importMultiLang)) {
            // Check, ob ein Text für eine nicht vorhandene Sprache importiert werden soll
            Map<String, String> additionalTexts = dictImportHelper.findAdditionalText(importMultiLang, existingMultiLang);
            additionalTexts.forEach((lang, text) -> {
                existingMultiLang.setText(lang, text);
                getMessageLog().fireMessage(TranslationHandler.translate("!!Die Benennung zum Code \"%1\" enthält " +
                                                                         "einen sprachspezifischen Texteintrag, den " +
                                                                         "andere Code-Benennungen mit dem gleichen " +
                                                                         "Suchtext nicht enthalten. Fehlende Benennungen" +
                                                                         " aus \"%1\" werden übernommen! Suchtext: \"%2\", " +
                                                                         "Sprache: %3, Text: \"%4\", Code ohne Text: %5",
                                                                         newCode, searchText, lang, text,
                                                                         textWithCode.getCodesAsString()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);

            });
            // Check, ob ein Text für eine Sprache existiert, der nicht im Importdatensatz vorkommt
            additionalTexts = dictImportHelper.findAdditionalText(existingMultiLang, importMultiLang);
            additionalTexts.forEach((lang, text) -> {
                getMessageLog().fireMessage(TranslationHandler.translate("!!Der Benennung zum Code \"%1\" fehlt ein " +
                                                                         "sprachspezifischer Texteintrag, den andere " +
                                                                         "Code-Benennungen mit dem gleichen Suchtext " +
                                                                         "enthalten. Fehlender Texteintrag wird übernommen! " +
                                                                         "Suchtext: \"%2\", Sprache: %3, Text: \"%4\"," +
                                                                         " Code mit Text: %5",
                                                                         newCode, searchText, lang, text,
                                                                         textWithCode.getCodesAsString()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            });
            // Texte, die schon existierten aber nicht neu hinzugekommen sind, hinzufügen
            Set<String> langWithDifferentTexts = dictImportHelper.findLanguagesWithDifferentTexts(existingMultiLang, importMultiLang);
            langWithDifferentTexts.forEach(lang -> {
                String oldText = existingMultiLang.getText(lang);
                String newText = importMultiLang.getText(lang);
                existingMultiLang.setText(lang, newText);
                getMessageLog().fireMessage(TranslationHandler.translate("!!Zum Suchtext \"%1\" und Sprache \"%2\" " +
                                                                         "wurden unterschiedliche Texte in der " +
                                                                         "Importdatei gefunden! Es wird der neue Text " +
                                                                         "übernommen! Neuer Text: \"%3\" für Code %4, " +
                                                                         "bisheriger Text: \"%5\" für Code %6",
                                                                         searchText, lang, newText, newCode, oldText,
                                                                         textWithCode.getCodesAsString()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP,
                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);

            });
        }
    }

    /**
     * Hilfsobjekt mit einem Textobjekte und allen Code, in denen es vorkommt
     */
    private class TextWithCode {

        private Set<String> codes;
        private EtkMultiSprache text;

        public TextWithCode(EtkMultiSprache text, String code) {
            this.text = text;
            this.codes = new HashSet<>();
            addCode(code);
        }

        public void addCode(String code) {
            if (StrUtils.isValid(code)) {
                codes.add(code);
            }
        }

        public EtkMultiSprache getText() {
            return text;
        }

        public String getCodesAsString() {
            return String.join(",", codes);
        }
    }
}
