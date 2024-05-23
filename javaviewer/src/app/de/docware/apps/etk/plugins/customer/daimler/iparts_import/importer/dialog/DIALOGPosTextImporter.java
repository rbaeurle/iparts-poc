/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataDialogPosText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogPosTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für POSX (Postionstexte zu DIALOG Stücklisten)
 * Abgeschaut von X2E Import (MasterDataDialogSeriesImporter)
 */
public class DIALOGPosTextImporter extends AbstractDIALOGDataImporter {

    public static final String DIALOG_TABLENAME = "POSX";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    public static final boolean IMPORT_HISTORY = true;

    // Elementnamen aus AS/PLM XML
    public static final String POSX_BR = "POSX_BR";
    public static final String POSX_RAS = "POSX_RAS";
    public static final String POSX_POSE = "POSX_POSE";
    public static final String POSX_SESI = "POSX_SESI";
    public static final String POSX_SPS = "POSX_SPS";
    public static final String POSX_SDA = "POSX_SDA";
    public static final String POSX_SDB = "POSX_SDB";
    public static final String POSX_BEN = "POSX_BEN";

    private String tableName;
    private HashMap<String, String> dialogMapping;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?

    public DIALOGPosTextImporter(EtkProject project) {
        super(project, "!!DIALOG-POS-Texte (POSX)",
              new FilesImporterFileListType(TABLE_DA_DIALOG_POS_TEXT, DD_POSITION_TEXT_NAME, false,
                                            false, true,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        this.tableName = TABLE_DA_DIALOG_POS_TEXT;

        // Mapping XML-Elementnamen -> Tabellenfelder
        dialogMapping = new HashMap<>();
        dialogMapping.put(FIELD_DD_POS_POS, POSX_POSE);
        dialogMapping.put(FIELD_DD_POS_SESI, POSX_SESI);
        dialogMapping.put(FIELD_DD_POS_SDATA, POSX_SDA);
        dialogMapping.put(FIELD_DD_POS_SDATB, POSX_SDB);
        dialogMapping.put(FIELD_DD_POS_TEXTNR, POSX_BEN);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExists = new String[]{ POSX_BR, POSX_RAS, POSX_POSE, POSX_SESI, POSX_SDA };
        String[] mustHaveData = new String[]{ POSX_BR, POSX_RAS, POSX_POSE, POSX_SESI, POSX_SDA };

        importer.setMustExists(mustExists);
        importer.setMustHaveData(mustHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME)
                   || importer.getTableNames().get(0).equals(tableName)
                   || importer.getTableNames().get(0).equals(DIALOG_TABLENAME);
        }
        return false;
    }


    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        if (IMPORT_HISTORY || isFinalStateDateTime(importRec.get(POSX_SDB))) {
            PosTextImportHelper importHelper = new PosTextImportHelper(getProject(), dialogMapping, tableName);
            // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
            if (!importHelper.checkImportRelevanceForSeries(POSX_BR, importRec, getInvalidSeriesSet(), this)) {
                return;
            }
            iPartsDialogPosTextId testId = getDialogPosTextId(importRec, importHelper);

            iPartsDataDialogPosText posText = new iPartsDataDialogPosText(getProject(), testId);
            if (!posText.existsInDB()) {
                posText.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }

            // Sprachdefinition des Records holen
            iPartsDIALOGLanguageDefs langDef = getLanguageDefinition(importRec);

            boolean doImport = false;
            if (langDef == iPartsDIALOGLanguageDefs.DIALOG_DE) {
                //kompletten Datensatz mit Werten füllen oder überschreiben
                importHelper.fillOverrideCompleteDataForDIALOGReverse(posText, importRec, langDef);
                doImport = true;
            } else if (langDef != iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN) {
                //nur die Sprachtexte übernehmen bzw überschreiben; dahinter steckt wohl die Annahme dass die sprachunabh.
                // Texte für alle Sprachen gleich sind. Die werden also aus dem deutschen Record übernommen. Dahinter
                // steckt jetzt die Annahme dass ein deutscher Record immer existiert
                importHelper.fillOverrideLanguageTextForDIALOGReverse(posText, importRec, langDef);
                doImport = true;
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Sprachkennung: %2)",
                                                            String.valueOf(recordNo), importRec.get(POSX_SPS)), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }

            if (importToDB && doImport) {
                saveToDB(posText);
            }
        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit %2 \"%3\" übersprungen", String.valueOf(recordNo),
                                                        "POSX_SDB", importRec.get(POSX_SDB)), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }

    }

    /**
     * SprachDefinition aus den Importdaten holen
     *
     * @param importRec
     * @return
     */
    private iPartsDIALOGLanguageDefs getLanguageDefinition(Map<String, String> importRec) {
        return iPartsDIALOGLanguageDefs.getType(importRec.get(POSX_SPS));
    }

    private iPartsDialogPosTextId getDialogPosTextId(Map<String, String> importRec, DIALOGImportHelper importHelper) {
        HmMSmId hmMSm = HmMSmId.getIdFromRaster(importHelper.handleValueOfSpecialField(POSX_BR, importRec),
                                                importHelper.handleValueOfSpecialField(POSX_RAS, importRec));
        return new iPartsDialogPosTextId(hmMSm,
                                         importHelper.handleValueOfSpecialField(POSX_POSE, importRec),
                                         importHelper.handleValueOfSpecialField(POSX_SDA, importRec)
        );
    }


    /**
     * Löscht alle vorhandenen Daten für den übergebenen Import-Dateilisten-Typ.
     *
     * @param importFileType
     * @return {@code true} falls das Entfernen erfolgreich war
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return removeAllExistingDataForTable(importFileType, tableName);
    }

    /**
     * Importiert die Datei für den übergebenen Import-Dateilisten-Typ.
     *
     * @param importFileType
     * @param importFile
     * @return {@code true} falls der Import erfolgreich war
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class PosTextImportHelper extends DIALOGImportHelper {

        public PosTextImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(POSX_SDA) || sourceField.equals(POSX_SDB)) {
                value = getDIALOGDateTimeValue(value);
            }
            if (sourceField.equals(POSX_BR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }
    }

}
