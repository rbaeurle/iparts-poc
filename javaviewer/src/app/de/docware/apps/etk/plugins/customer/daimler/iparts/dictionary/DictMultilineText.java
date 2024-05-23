/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.file.DWWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rootinen zur Behandlung der Lexikon-Texte bzgl Umbruch und mehrfachen Leerzeichen
 */
public class DictMultilineText {

    public static final String SPLIT_DELIMITER = "\n";

    private static final int KG_TU_NAME_SPLIT_LEN = 40;
    private static final int ELDAS_MODEL_NAME_SPLIT_LEN = 80;
    private static final int BMADD_TEXT_SPLIT_LEN = 40;
    private static final int FOOTNOTE_SPLIT_LEN = 126;
    private static final int SA_NAME_SPLIT_LEN = 70;
    private static final int SAA_NAME_SPLIT_LEN = 42;


    private enum SPLIT_TYPE {NOSPLIT, SPLIT, SPLIT_COMPLEX, SPLIT_COMPLEX_NODELIM}
    /* NOSPLIT: Text unverändert zurückgeben
     * SPLIT  : Text nach den Splitregeln in {@link https://confluence.docware.de/confluence/display/DAIM/Migration%3A+Mehrzeilige+Texte?focusedCommentId=21365421&refresh=1467365716470#comment-21365421} auftrennen
     * SPLIT_COMPLEX: Text Wortweise zusammenbauen mit den Splitgrenzen
     * SPLIT_COMPLEX_NODELIM: es werden nur mehrfache, hintereinanderstehende Leerzeichen entfernt
     */


    private static DictMultilineText instance = null;
    private static Map<DictTextKindTypes, SPLIT_TYPE> howToSplit = new HashMap<DictTextKindTypes, SPLIT_TYPE>();
    private static Map<DictTextKindTypes, SPLIT_TYPE> howToSplitLong = new HashMap<DictTextKindTypes, SPLIT_TYPE>();

    static {
        //Lookup-Table für die einzelnen Textarten
        howToSplit.put(DictTextKindTypes.CODE_NAME, SPLIT_TYPE.NOSPLIT);
        howToSplit.put(DictTextKindTypes.COLORS, SPLIT_TYPE.NOSPLIT);
        howToSplit.put(DictTextKindTypes.MODEL_NAME, SPLIT_TYPE.NOSPLIT);
        howToSplit.put(DictTextKindTypes.SA_NAME, SPLIT_TYPE.SPLIT_COMPLEX_NODELIM);
        howToSplit.put(DictTextKindTypes.SAA_NAME, SPLIT_TYPE.SPLIT_COMPLEX_NODELIM);
        howToSplit.put(DictTextKindTypes.KG_TU_NAME, SPLIT_TYPE.SPLIT_COMPLEX_NODELIM);
        howToSplit.put(DictTextKindTypes.MAT_NAME, SPLIT_TYPE.NOSPLIT);
        howToSplit.put(DictTextKindTypes.ADD_TEXT, SPLIT_TYPE.NOSPLIT);
        howToSplit.put(DictTextKindTypes.EVO_CK, SPLIT_TYPE.NOSPLIT);
        howToSplit.put(DictTextKindTypes.DIALOG_MODEL_ADDTEXT, SPLIT_TYPE.SPLIT_COMPLEX_NODELIM);
        howToSplit.put(DictTextKindTypes.FOOTNOTE, SPLIT_TYPE.SPLIT);
        howToSplit.put(DictTextKindTypes.ELDAS_MODEL_ADDTEXT, SPLIT_TYPE.SPLIT_COMPLEX_NODELIM);
        howToSplit.put(DictTextKindTypes.NEUTRAL_TEXT, SPLIT_TYPE.NOSPLIT);
        howToSplit.put(DictTextKindTypes.UNKNOWN, SPLIT_TYPE.NOSPLIT);

        //Lookup-Table für die einzelnen Textarten bei Langen Texten
        howToSplitLong.put(DictTextKindTypes.CODE_NAME, SPLIT_TYPE.NOSPLIT);
        howToSplitLong.put(DictTextKindTypes.COLORS, SPLIT_TYPE.NOSPLIT);
        howToSplitLong.put(DictTextKindTypes.MODEL_NAME, SPLIT_TYPE.NOSPLIT);
        howToSplitLong.put(DictTextKindTypes.SA_NAME, SPLIT_TYPE.SPLIT);
        howToSplitLong.put(DictTextKindTypes.SAA_NAME, SPLIT_TYPE.SPLIT);
        howToSplitLong.put(DictTextKindTypes.KG_TU_NAME, SPLIT_TYPE.SPLIT);
        howToSplitLong.put(DictTextKindTypes.MAT_NAME, SPLIT_TYPE.NOSPLIT);
        howToSplitLong.put(DictTextKindTypes.ADD_TEXT, SPLIT_TYPE.NOSPLIT);
        howToSplitLong.put(DictTextKindTypes.EVO_CK, SPLIT_TYPE.NOSPLIT);
        howToSplitLong.put(DictTextKindTypes.DIALOG_MODEL_ADDTEXT, SPLIT_TYPE.SPLIT);
        howToSplitLong.put(DictTextKindTypes.FOOTNOTE, SPLIT_TYPE.SPLIT);
        howToSplitLong.put(DictTextKindTypes.ELDAS_MODEL_ADDTEXT, SPLIT_TYPE.SPLIT);
        howToSplitLong.put(DictTextKindTypes.NEUTRAL_TEXT, SPLIT_TYPE.NOSPLIT);
        howToSplitLong.put(DictTextKindTypes.UNKNOWN, SPLIT_TYPE.NOSPLIT);
    }

    public static DictMultilineText getInstance() {
        if (instance == null) {
            instance = new DictMultilineText();
        }
        return instance;
    }

    private boolean writeResults = false;  // Ausgabe der Änderungen bei convertDictTextKinds in Datei


    private DictMultilineText() {
    }

    public int getSplitLenByTextKindType(DictTextKindTypes txtKindType) {
        int splitLen = 0;
        switch (txtKindType) {
            case KG_TU_NAME:  //("B", "!!KG/TU-Benennung") KG/TU Produktbezogen
                //Zeilen Deutsch 2 Länge Deutsch 40 Zeilen Fremdsprache 2 Länge Fremdsprache 40
                splitLen = KG_TU_NAME_SPLIT_LEN;
                break;
            case ELDAS_MODEL_ADDTEXT:  //("b", "!!ELDAS Baumusterbenennung")
                //Zeilen Deutsch 99 Länge Deutsch 80 Zeilen Fremdsprache 99 Länge Fremdsprache 80
                splitLen = ELDAS_MODEL_NAME_SPLIT_LEN;
                break;
            case DIALOG_MODEL_ADDTEXT:  //("J", "!!Baumuster Zusatztexte")  Dialog Baumuster-Zusatztext
                //Zeilen Deutsch 8 Länge Deutsch 40 Zeilen Fremdsprache 8 Länge Fremdsprache 40
                splitLen = BMADD_TEXT_SPLIT_LEN;
                break;
            case FOOTNOTE:  //("Q", "!!Fußnoten") Dialog, ELDAS Fussnote kurz/lang
                //Zeilen Deutsch 2/9 Länge Deutsch 126 Zeilen Fremdsprache 2/9 Länge Fremdsprache 126
                splitLen = FOOTNOTE_SPLIT_LEN;
                break;
            case SA_NAME:  //("r", "!!SA-Benennung")
                //Zeilen Deutsch 2 Länge Deutsch 70 Zeilen Fremdsprache 2 Länge Fremdsprache 70
                splitLen = SA_NAME_SPLIT_LEN;
                break;
            case SAA_NAME:   //("s", "!!SAA-Benennung")
                //Zeilen Deutsch 3 Länge Deutsch 42 Zeilen Fremdsprache 3 Länge Fremdsprache 42
                splitLen = SAA_NAME_SPLIT_LEN;
                break;
        }
        return splitLen;
    }

    public String convertDictText(DictTextKindTypes txtKindType, String currentText) {
        return convertDictText(txtKindType, currentText, howToSplit);
    }

    public String convertDictTextLong(DictTextKindTypes txtKindType, String currentText) {
        return convertDictText(txtKindType, currentText, howToSplitLong);
    }

    private String convertDictText(DictTextKindTypes txtKindType, String currentText, Map<DictTextKindTypes, SPLIT_TYPE> howToSplitText) {
        if (StrUtils.isEmpty(currentText)) {
            return currentText;
        }

        SPLIT_TYPE type = howToSplitText.get(txtKindType);
        if ((type == null) || type == SPLIT_TYPE.NOSPLIT) {
            return currentText;
        }
        String value = currentText;
        int splitLen = getSplitLenByTextKindType(txtKindType);
        if ((splitLen > 0) && !value.contains(SPLIT_DELIMITER)) {
            switch (type) {
                case SPLIT:
                    value = splitNormal(value, splitLen);
                    break;
                case SPLIT_COMPLEX:
                    value = splitComplex(value, splitLen);
                    break;
                case SPLIT_COMPLEX_NODELIM:
                    value = splitComplex(value, 40000); // kein Umbruch
                    break;
            }
        }
        return value;
    }

    /**
     * speziell für Fußnoten beim Import
     * Tabellen-Fußnoten werden gesplittet; alle anderen nur mehrfache Leerzeichen entfernt
     *
     * @param currentText
     * @param isTableFootNote
     * @return
     */
    public String convertFootNoteForImport(String currentText, boolean isTableFootNote) {
        if (isTableFootNote) {
            return convertDictText(DictTextKindTypes.FOOTNOTE, currentText);
        }
        return splitComplex(currentText, 40000); // kein Umbruch
    }

    /**
     * Spezailfall für Fußnoten: Nur Table-Fußnoten werden umgebrochen
     * bereits umgebrochene allgemeine Fußnoten werden zurückgewandelt
     *
     * @param currentText
     * @return
     */
    private String convertFootNoteDictText(String currentText, boolean isTableFootNote) {
        SPLIT_TYPE type = howToSplit.get(DictTextKindTypes.FOOTNOTE);
        if ((type == null) || type == SPLIT_TYPE.NOSPLIT) {
            return currentText;
        }
        if (!isTableFootNote) {
            if (currentText.contains(SPLIT_DELIMITER)) {
                currentText = currentText.replace(SPLIT_DELIMITER.charAt(0), ' ');
            }
            return splitComplex(currentText, 40000); // kein Umbruch;
        }
        return convertDictText(DictTextKindTypes.FOOTNOTE, currentText);
    }

    /**
     * @param value
     * @param splitLen
     * @return
     */
    private String splitNormal(String value, int splitLen) {
        List<String> splitList = StrUtils.splitStringIntoSubstrings(value, splitLen);
        if (!splitList.isEmpty()) {
            StringBuilder str = new StringBuilder(StrUtils.trimRight(splitList.get(0)));
            for (int lfdNr = 1; lfdNr < splitList.size(); lfdNr++) {
                str.append(SPLIT_DELIMITER);
                str.append(StrUtils.trimRight(splitList.get(lfdNr)));
            }
            value = str.toString();
        } else {
            value = "";
        }
        return value;
    }

    /**
     * @param value
     * @param splitLen
     * @return
     */
    private String splitComplex(String value, int splitLen) {
        List<String> wordList = StrUtils.toStringList(value, " ");
        if (!wordList.isEmpty()) {
            StringBuilder str = new StringBuilder(wordList.get(0));
            StringBuilder line = new StringBuilder(wordList.get(0));
            for (int lfdNr = 1; lfdNr < wordList.size(); lfdNr++) {
                String nextWord = wordList.get(lfdNr);
                if ((line.length() + nextWord.length() + 1) < splitLen) {
                    str.append(" ");
                    str.append(nextWord);
                    line.append(" ");
                    line.append(nextWord);
                } else {
                    str.append(SPLIT_DELIMITER);
                    line.setLength(0);
                    str.append(nextWord);
                    line.append(nextWord);
                }
            }
            value = str.toString();
        }
        return value;
    }

    /**
     * Routine, die bereits bestehende Lexikon-Texte umbricht
     *
     * @param project
     * @param messageLogForm
     * @return
     */
    public boolean convertDictTextKinds(EtkProject project, EtkMessageLogForm messageLogForm) {
        messageLogForm.getMessageLog().hideProgress();
        messageLogForm.setTitle("!!Lexikon Texte bearbeiten");
        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lexikon Texte bearbeiten"),
                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        for (Map.Entry<DictTextKindTypes, SPLIT_TYPE> entry : howToSplit.entrySet()) {
            if (entry.getValue() != SPLIT_TYPE.NOSPLIT) {
                String txtKindName = TranslationHandler.translate(entry.getKey().getTextKindName());
                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Bearbeite %1", txtKindName),
                                                           MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                // FootNotes brauchen ein Extra-Handling
                if (entry.getKey() == DictTextKindTypes.FOOTNOTE) {
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Hole Lexikoneinträge zu %1", txtKindName),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    iPartsDictTextKindId dictTextKindId = DictTxtKindIdByMADId.getInstance(project).getTxtKindId(entry.getKey());
                    iPartsDataDictMetaList dictMetaList = iPartsDataDictMetaList.loadMetaFromTextKindList(project, dictTextKindId);
                    Set<String> tableFootNoteTextIds = iPartsDataFootNoteContentList.loadAllDIALOGTableFootNoteTextIds(project);
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!bearbeite %1 Lexikoneinträge zu %2",
                                                                                            String.valueOf(dictMetaList.size()), txtKindName),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    messageLogForm.getMessageLog().fireProgress(0, dictMetaList.size(), "", true, false);
                    int cnt = 0;
                    try {
                        project.getDbLayer().startTransaction();
                        project.getDbLayer().startBatchStatement();

                        for (iPartsDataDictMeta dataDictMeta : dictMetaList) {
                            EtkMultiSprache multi = dataDictMeta.getMultiLang();
                            String textId = multi.getTextId();
                            if (!DictHelper.isLongTextId(textId)) {
                                boolean isTableFootNote = tableFootNoteTextIds.contains(textId);
                                for (Map.Entry<String, String> langEntry : multi.getLanguagesAndTexts().entrySet()) {
                                    String text = langEntry.getValue();
                                    text = convertFootNoteDictText(text, isTableFootNote);
                                    multi.setText(langEntry.getKey(), text);
                                }
                                dataDictMeta.setNewMultiLang(multi);
                            }
                            cnt++;
                            messageLogForm.getMessageLog().fireProgress(cnt, dictMetaList.size(), "", true, false);
                        }
                        messageLogForm.getMessageLog().hideProgress();


                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Speichere Lexikoneinträge zu %1", txtKindName),
                                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        dictMetaList.saveToDB(project);
                        project.getDbLayer().endBatchStatement();
                        project.getDbLayer().commit();
                    } catch (Exception e) {
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                        messageLogForm.getMessageLog().fireMessage(Logger.getLogger().exceptionToString(e), MessageLogType.tmlError);
                        project.getDbLayer().cancelBatchStatement();
                        project.getDbLayer().rollback();
                        return false;
                    }
                } else {
                    if (!handleTexts(project, messageLogForm, entry, txtKindName)) {
                        return false;
                    }
                }

            }
        }
        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Beendet"),
                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        return true;
    }

    private boolean handleTexts(EtkProject project, EtkMessageLogForm messageLogForm, Map.Entry<DictTextKindTypes, SPLIT_TYPE> entry, String txtKindName) {
        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Hole Lexikoneinträge zu %1", txtKindName),
                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        iPartsDictTextKindId dictTextKindId = DictTxtKindIdByMADId.getInstance(project).getTxtKindId(entry.getKey());
        iPartsDataDictMetaList dictMetaList = iPartsDataDictMetaList.loadMetaFromTextKindList(project, dictTextKindId);
        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!bearbeite %1 Lexikoneinträge zu %2",
                                                                                String.valueOf(dictMetaList.size()), txtKindName),
                                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        messageLogForm.getMessageLog().fireProgress(0, dictMetaList.size(), "", true, false);
        int cnt = 0;
        DWFile tmpFile = null;
        if (writeResults) {
            tmpFile = DWFile.createTempDirectory("daim");
        }
        try {
            project.getDbLayer().startTransaction();
            project.getDbLayer().startBatchStatement();

            DWWriter writer = null;
            if (writeResults) {
                tmpFile.mkDirsWithRepeat();
                DWFile outFile = DWFile.get(tmpFile, txtKindName.replace('/', '-') + ".txt");
                writer = outFile.getWriter(DWFileCoding.UTF8);
            }

            for (iPartsDataDictMeta dataDictMeta : dictMetaList) {
                EtkMultiSprache multi = dataDictMeta.getMultiLang();
                for (Map.Entry<String, String> langEntry : multi.getLanguagesAndTexts().entrySet()) {
                    String text = langEntry.getValue();
                    String modifiedText = convertDictText(entry.getKey(), text);
                    multi.setText(langEntry.getKey(), modifiedText);
                    if (writeResults) {
                        if (langEntry.getKey().equals(Language.DE.getCode()) && !text.equals(modifiedText)) {
                            writer.writeln("");
                            writer.writeln(txtKindName + ": " + multi.getTextId());
                            writer.writeln(text);
                            writer.writeln(modifiedText);
                        }
                    }
                }
                dataDictMeta.setNewMultiLang(multi);
                cnt++;
                messageLogForm.getMessageLog().fireProgress(cnt, dictMetaList.size(), "", true, false);
            }
            if (writeResults) {
                writer.close();
            }
            messageLogForm.getMessageLog().hideProgress();


            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Speichere Lexikoneinträge zu %1", txtKindName),
                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            dictMetaList.saveToDB(project);
            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            messageLogForm.getMessageLog().fireMessage(Logger.getLogger().exceptionToString(e), MessageLogType.tmlError);
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            return false;
        } finally {
            // Aufräumen
            if (tmpFile != null) {
                tmpFile.deleteDirContentRecursivelyWithRepeat();
                tmpFile.deleteRecursivelyWithRepeat();
            }
        }
        return true;
    }

    /**
     * Routine, die bereits bestehende Lexikon-Texte umbricht mit eigener {@link EtkMessageLogForm}
     *
     * @param project
     */
    public void convertDictTextKinds(final EtkProject project) {
        final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("", "!!Lexikon Texte bearbeiten", null);
        messageLogForm.showModal(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                convertDictTextKinds(project, messageLogForm);
            }
        });
    }
}
