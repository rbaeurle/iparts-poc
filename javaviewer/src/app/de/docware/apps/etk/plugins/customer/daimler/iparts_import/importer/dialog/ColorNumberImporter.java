/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorNumberId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorNumber;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für Farbnummern (FNR)
 */
public class ColorNumberImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    public static final String DIALOG_TABLENAME = "FNR";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    public static final String FNR_FNR = "FNR_FNR";
    public static final String FNR_SDB = "FNR_SDB";
    public static final String FNR_SPS = "FNR_SPS";
    public static final String FNR_BEN = "FNR_BEN";

    private HashMap<String, String> mappingColorNumberData;
    private String[] primaryKeysColorNumberImport;

    private final boolean importToDB = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public ColorNumberImporter(EtkProject project) {
        super(project, "!!DIALOG-Farbnummer (FNR)",
              new FilesImporterFileListType(TABLE_DA_COLOR_NUMBER, DCN_COLOR_NUMBER, false, false, true, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysColorNumberImport = new String[]{ FNR_FNR };
        mappingColorNumberData = new HashMap<>();
        mappingColorNumberData.put(FIELD_DCN_COLOR_NO, FNR_FNR);
        mappingColorNumberData.put(FIELD_DCN_DESC, FNR_BEN);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysColorNumberImport, FNR_BEN, FNR_SDB, FNR_SPS));
        importer.setMustHaveData(primaryKeysColorNumberImport);
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
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        DIALOGImportHelper helper = new DIALOGImportHelper(getProject(), mappingColorNumberData, TABLE_DA_COLOR_NUMBER);
        // nur der zuletzt freigegebene Stand soll übernommen werden
        String sdatb = helper.getDIALOGDateTimeValue(importRec.get(FNR_SDB));
        if (!sdatb.isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                        "FTS_SDB", sdatb),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        // Sprachkenner aus XML holen
        iPartsDIALOGLanguageDefs lang = iPartsDIALOGLanguageDefs.getType(importRec.get(FNR_SPS));

        // DataObject und ID bauen
        iPartsColorNumberId id = new iPartsColorNumberId(helper.handleValueOfSpecialField(FNR_FNR, importRec));
        iPartsDataColorNumber dataColorNumber = new iPartsDataColorNumber(getProject(), id);

        // Die Farbbenennung extrahieren
        String farbBenennung = helper.handleValueOfSpecialField(FNR_BEN, importRec);

        // Ungültige Benennungen unterschiedlich behandeln
        if (farbBenennung.isEmpty()) {
            // Der deutsche Text  M-U-S-S  existieren und führt zum Abbruch des Importes falls er fehlen sollte.
            if (lang == iPartsDIALOGLanguageDefs.DIALOG_DE) {
                cancelImport(translateForLog("!!Fehler in den Importdaten! Record Nr. %1, Sprache: %2 enthält keine Farbbenennung!",
                                             String.valueOf(recordNo),
                                             TranslationHandler.translateForLanguage(lang.getDbValue().getDisplayName(), getLogLanguage())));
                return;
            }
            // Fehlende, fremdsprachigen Farbbenennungen sollen nur eine Logmessage erzeugen und dürfen nicht zum Abbruch des Importes führen.
            else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, Sprache: %2 enthält keine Farbbenennung!",
                                                            String.valueOf(recordNo), TranslationHandler.translateForLanguage(lang.getDbValue().getDisplayName(), getLogLanguage())),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }
        }

        if (!dataColorNumber.loadFromDB(id)) {
            // Wenn Farbstamm in DB noch nicht existiert -> neu Anlegen
            dataColorNumber.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            helper.fillOverrideCompleteDataForDIALOGReverse(dataColorNumber, importRec, lang);
        } else {
            helper.deleteContentIfMADSource(dataColorNumber, FIELD_DCN_SOURCE, false);
            //ungültige Sprache aussortieren
            if (lang != iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN) {
                // Falls ein Datensatz schon existiert -> Prüfen, ob Benennung zur übergebenene Sprache existiert
                EtkMultiSprache langInDB = dataColorNumber.getFieldValueAsMultiLanguage(FIELD_DCN_DESC);
                langInDB.removeLanguagesWithEmptyTexts();
                if (StrUtils.isEmpty(langInDB.getText(lang.getDbValue().getCode()))) {
                    // Benennung zur Sprache existiert noch nicht -> sprachspezifisch Anlegen
                    helper.fillOverrideOneLanguageTextForDIALOG(dataColorNumber, lang, FIELD_DCN_DESC, importRec.get(FNR_BEN));
                } else {
                    // Benennung zur Sprache existiert schon -> nicht verarbeiten (siehe Wiki: Farbnummern urladen)
                    getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (Benennung für Sprache: %2 existiert bereits. Aktuell: %3; Neu: %4)",
                                                                String.valueOf(recordNo), TranslationHandler.translateForLanguage(lang.getDbValue().getDisplayName(), getLogLanguage()),
                                                                langInDB.getText(lang.getDbValue().getCode()),
                                                                importRec.get(FNR_BEN)),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    reduceRecordCount();
                    return;
                }
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Sprachkennung: %2)", String.valueOf(recordNo),
                                                            importRec.get(FNR_SPS)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }
        }
        dataColorNumber.setFieldValue(FIELD_DCN_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin(), DBActionOrigin.FROM_EDIT);
        if (importToDB) {
            saveToDB(dataColorNumber);
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_COLOR_NUMBER)) {
            // Löschen der Spracheinträge (Löscht Einträge ohne Text-ID)
            deleteLanguageEntriesOfTable(TABLE_DA_COLOR_NUMBER);
            getProject().getDbLayer().delete(TABLE_DA_COLOR_NUMBER, new String[]{ FIELD_DCN_SOURCE }, new String[]{ iPartsImportDataOrigin.DIALOG.getOrigin() });
            return true;
        }
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_DA_COLOR_NUMBER)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

}
