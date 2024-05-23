/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsReplaceConstPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.SQLStringConvert;

import java.util.HashMap;
import java.util.Map;

/**
 * Importer für die DIALOG-Tabelle (T10RTS7), Konstruktionsdaten Ersetzungen Teilestamm Änderungstexte mit Sprachschlüssel
 */
public class ReplacementPartsConstImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    public static final String DIALOG_TABLENAME = "TS7";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;
    public static final String DEST_TABLENAME = TABLE_DA_REPLACE_CONST_PART;

    //  private static final String TS7_WN = "TS7_WN";          // Werksnummer            ==> wird nicht übernommen
    public static final String TS7_TEIL = "TS7_TEIL";      // Teile-Sachnummer (PK)  ==> [DRCP_PART_NO]
    public static final String TS7_SPS = "TS7_SPS";        // Sprachschluessel       ==> wird nur zum Import benötigt aber nicht übernommen
    public static final String TS7_SDA = "TS7_SDA";        // Datum ab         (PK)  ==> [DRCP_SDATA]
    public static final String TS7_SDB = "TS7_SDB";        // Datum bis              ==> [DRCP_SDATB]
    public static final String TS7_WERKE = "TS7_WERKE";    // Werkskennungen         ==> [DRCP_FACTORY_IDS]
    public static final String TS7_RFME = "TS7_RFME";      // RFME Flags             ==> [DRCP_RFME]
    public static final String TS7_ATEXT = "TS7_ATEXT";    // Änderungstexte         ==> [DRCP_TEXT] 3 x 63 Zeichen müssen zusammengeführt werden
    public static final String TS7_VSNR = "TS7_VSNR";      // Vorgänger Teilenummer  ==> [DRCP_PRE_MATNR]
    public static final String TS7_NSNR = "TS7_NSNR";      // Nachfolger Teilenummer ==> [DRCP_REPLACE_MATNR]
    public static final String TS7_VM = "TS7_VM";          // Vorhandenes Material   ==> [DRCP_AVAILABLE_MATERIAL]
    public static final String TS7_WZ = "TS7_WZ";          // Werkzeugänderung       ==> [DRCP_TOOL_CHANGE]
    public static final String TS7_WS = "TS7_WS";          // Materialänderung       ==> [DRCP_MATERIAL_CHANGE]

    private static final int TS7_ATEXT_SPLIT_LENGTH = 63;
    private static final int HIDE_PROGRESS_LIMIT = 1000;

    private boolean importToDB = true;
    private boolean isBufferedSave = true;
    private Map<String, String> mapping;
    private String[] primaryKeysImport;
    private final Map<iPartsReplaceConstPartId, iPartsDataReplaceConstPart> dataToStore = new HashMap<>();


    public ReplacementPartsConstImporter(EtkProject project) {
        super(project, "!!DIALOG Ersetzungen und Mitlieferteile (TS7)",
              new FilesImporterFileListType(DEST_TABLENAME, DRP_REPLACEMENTS_CONST, false, false,
                                            false, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysImport = new String[]{ TS7_TEIL, TS7_SDA };

        mapping = new HashMap<>();

        mapping.put(FIELD_DRCP_PART_NO, TS7_TEIL);
        mapping.put(FIELD_DRCP_SDATA, TS7_SDA);
        mapping.put(FIELD_DRCP_SDATB, TS7_SDB);
        mapping.put(FIELD_DRCP_FACTORY_IDS, TS7_WERKE);
        mapping.put(FIELD_DRCP_RFME, TS7_RFME);
//        mapping.put(FIELD_DRCP_TEXT, TS7_ATEXT); <<==== Multilang, spezielle Behandlung
        mapping.put(FIELD_DRCP_PRE_MATNR, TS7_VSNR);
        mapping.put(FIELD_DRCP_REPLACE_MATNR, TS7_NSNR);
        mapping.put(FIELD_DRCP_AVAILABLE_MATERIAL, TS7_VM);
        mapping.put(FIELD_DRCP_TOOL_CHANGE, TS7_WZ);
        mapping.put(FIELD_DRCP_MATERIAL_CHANGE, TS7_WS);
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();
        dataToStore.clear();
        setBufferedSave(isBufferedSave);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysImport);
        importer.setMustHaveData(new String[]{});
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
        ReplacementsPartsConstImportHelper importHelper = new ReplacementsPartsConstImportHelper(getProject(), mapping, DEST_TABLENAME);
        // Die ID bauen
        iPartsReplaceConstPartId replaceConstPartId = importHelper.getReplaceConstPartId(importRec, recordNo);
        if (replaceConstPartId == null) {
            reduceRecordCount();
            return;
        }
        iPartsDIALOGLanguageDefs langDef = iPartsDIALOGLanguageDefs.getType(importHelper.handleValueOfSpecialField(TS7_SPS, importRec));
        if (langDef == iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Sprachkennung: %2)", String.valueOf(recordNo),
                                                        importRec.get(TS7_SPS)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        // 1. Wenn enthalten, das bestehende Objekt aus der Liste holen.
        iPartsDataReplaceConstPart replaceConstPart = dataToStore.get(replaceConstPartId);
        if (replaceConstPart == null) {
            // Ansonsten, das Objekt neu anlegen ...
            replaceConstPart = new iPartsDataReplaceConstPart(getProject(), replaceConstPartId);
            // ... und bei Bedarf mit leeren Werten initialisieren.
            if (!replaceConstPart.existsInDB()) {
                replaceConstPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            dataToStore.put(replaceConstPartId, replaceConstPart);
        }

        // Nur beim deutschen Datensatz die Daten übernehnmen.
        if (langDef == iPartsDIALOGLanguageDefs.DIALOG_DE) {
            // Beim deutschen Datensatz alle Werte übernehmen
            importHelper.fillOverrideCompleteDataForDIALOGReverse(replaceConstPart, importRec, langDef);
        }

        // Jetzt das MultiLang-Feld:
        // Den multilang Text holen ...
        EtkMultiSprache multiLangText = replaceConstPart.getFieldValueAsMultiLanguage(FIELD_DRCP_TEXT);
        // ... den Text speziell zusammenbauen ...
        String text = importHelper.handleValueOfSpecialField(TS7_ATEXT, importRec);
        // ... in der angegebenen Sprache direkt setzen ...
        multiLangText.setText(langDef.getDbValue().getCode(), text);
        // ... und den multilang Text wieder am Objekt einhängen.
        replaceConstPart.setFieldValueAsMultiLanguage(FIELD_DRCP_TEXT, multiLangText, DBActionOrigin.FROM_EDIT);
        if (langDef != iPartsDIALOGLanguageDefs.DIALOG_DE) {
            reduceRecordCount();
        }
    }

    @Override
    protected void postImportTask() {
        // Die angezeigte Zahl des aktuellen Import-Datensatzes stimmt zum Ende hin nicht mehr.
        // Refresh-Problem. ==> Den Fortschritt kurzerhand verschwinden lassen.
        getMessageLog().hideProgress();

        if (!isCancelled() && importToDB) {
            if (!dataToStore.isEmpty()) {
                int size = dataToStore.size();
                boolean showProgress = size > HIDE_PROGRESS_LIMIT;
                if (showProgress) {
                    getMessageLog().fireProgress(0, size, "", true, false);
                }
                // MELDUNG # Datensatz/Datensätze werden gespeichert!
                getMessageLog().fireMessage(translateForLog((size == 1) ?
                                                            "!!%1 Datensatz wird gespeichert." :
                                                            "!!%1 Datensätze werden gespeichert.",
                                                            String.valueOf(size)),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                int counter = 0;
                for (iPartsDataReplaceConstPart data : dataToStore.values()) {
                    saveToDB(data);
                    // Den Fortschrittsbalken erst ab einer ### von Einträgen in der Liste anszeigen.
                    if (showProgress) {
                        getMessageLog().fireProgress(++counter, size, "", true, false);
                    }
                    if (isCancelled()) {
                        break;
                    }
                }
                getMessageLog().hideProgress();
            }
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(DEST_TABLENAME)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    /**
     * Der unvermeidliche Helper
     */
    private class ReplacementsPartsConstImportHelper extends DIALOGImportHelper {

        private ReplacementsPartsConstImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        /**
         * Spezielle Behandlung für bestimmte Felder (Modifikation des Wertes aus dem Feld im <i>importRecord</i>).
         * <br/>Diese Methode sollte von Importern beim Import aufgerufen werden.
         *
         * @param sourceField
         * @param value
         * @return
         */
        @Override
        public String handleValueOfSpecialField(String sourceField, String value) {
            if (StrUtils.isEmpty(value)) {
                value = "";
            }
            if ((sourceField.equals(TS7_ATEXT)) && (StrUtils.isValid(value))) {
                return formatLanguageText(value);
            } else if ((sourceField.equals(TS7_VM)) || (sourceField.equals(TS7_WZ)) || (sourceField.equals(TS7_WS))) {
                return SQLStringConvert.booleanToPPString(SQLStringConvert.ppStringToBoolean(value));
            } else if (sourceField.equals(TS7_SDA) || sourceField.equals(TS7_SDB)) {
                return getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(TS7_RFME)) {
                return value; // <<=== ohne Trim(), 7 Stellen, kann Leerzeichen enthalten
            }
            return value.trim();
        }

        /**
         * Funktion, die aus dem übergebenen String die drei Teilstrings extrahiert und mit einem Leerzeichen getrennt
         * wieder an einander hängt und zurückliefert.
         * <p>
         * Die Theorie:
         * TS7_ATEXT, max. 189 Zeichen Änderungstext, 3 x á 63 Zeichen
         * <p>
         * Die Realität:
         * Der String kann aber kürzer und auch länger sein (0-197 Zeichen)!
         * <p>
         * Beispiele:
         * <TS7_ATEXT></TS7_ATEXT>
         * <TS7_ATEXT>KONTAKTAENDERUNG KOSTAL AUF TYCO</TS7_ATEXT>
         * <TS7_ATEXT>AEA: F/VW: C213    160618 1000 0999/                           AA: FS,FV,FW/L: L/CR: (242/222)+(401/873);                     R:  F:  M:  E:  A:X W:</TS7_ATEXT>
         * <TS7_ATEXT>AEA: U / VW: C213    100616 0130                               0990 / AA: FS,FW / L: &quot; &quot; / CR: 421+333;                       TEXT: DER LICHTLEITER CUPHOLDER A2058251610 MUSS VERBAUT</TS7_ATEXT>
         * <TS7_ATEXT>AEA: A                                                         TEXT: ENTGEGEN DER AKTUELLEN ZEICHNUNGEN WIRD DER TUERTRENNS   TELLENSTECKER OHNE CPA VERRIEGELUNG VERBAUT, WELCHER SONST E</TS7_ATEXT>
         * <TS7_ATEXT>AEA: F / VW: C177    080608 2250 001                           0 / AA: FZ / L: &quot; &quot; / CR: 274;                                 TEXT: STECKERKUPPLUNG BISHER NUR FREIGEGEBEN UND BEMUSTERT A</TS7_ATEXT>
         * <TS7_ATEXT>AEA: F / VW: C213    081080 0100 000                           1 / AA: FS,FV,FW / L: &quot; &quot; / CR: ;                              TEXT: BAUTEIL IST NICHT FUER MATTE LACKE FREIGEGEBEN. TEIL K</TS7_ATEXT>
         * <TS7_ATEXT>AEA: F / VW: C213    080816 0400 004                           0 / AA: FW / L: &quot; &quot; / CR: P55/P55+(P31/P15)/P60+P90;           TEXT: TEILE OHNE BEMUSTERUNG, ZUSATZFRAESUNG FUER MONTAGE HI</TS7_ATEXT>
         * <TS7_ATEXT>AEA: F / VW: C213    080620 2000 006                           5 / AA: FS,FW / L: &quot; &quot; / CR: H80+222+(810/811);                TEXT: VERKLEBEN DER LSP-GITTER MIT DEN ZIERTEILEN AN DEN CLI</TS7_ATEXT>
         * <TS7_ATEXT>AEA: F / VW: C213    080420 2100 062                           0 / AA: FS,FW / L: &quot; &quot; / CR: H06+221+(401/873/902);            TEXT: BEIM EINSATZ DER NEUEN KLAMMER ENTFAELLT DIESE NACHARB</TS7_ATEXT>
         * <TS7_ATEXT>CHROMLEISTE AN ZIERTEIL MUSS IM VORD                           EREN, UNTEREN BEREICH (BANK MF) KOEFU UM CA. 0,6MM PARTIELL    ZURUECKGENOMMEN WERDEN.</TS7_ATEXT>
         * <TS7_ATEXT>AEA: F / VW: C213    080420 2100 060                           5 / AA: FS,FW / L: &quot; &quot; / CR: H06+221;                          TEXT: VERKLEBEN DER LSP-GITTER MIT DEN ZIERTEILEN AN DEN CLI</TS7_ATEXT>
         *
         * @param value
         * @return
         */
        private String formatLanguageText(String value) {
            StringBuilder outputBuffer = new StringBuilder();

            String str1 = StrUtils.trimRight(StrUtils.copySubString(value, 0, TS7_ATEXT_SPLIT_LENGTH));
            String str2 = StrUtils.trimRight(StrUtils.copySubString(value, TS7_ATEXT_SPLIT_LENGTH, TS7_ATEXT_SPLIT_LENGTH));
            String str3 = StrUtils.trimRight(StrUtils.copySubString(value, 2 * TS7_ATEXT_SPLIT_LENGTH, value.length()));

            outputBuffer.append(str1);
            if (!str2.isEmpty()) {
                if (!str1.isEmpty()) {
                    outputBuffer.append(" ");
                }
                outputBuffer.append(str2);
                if (!str3.isEmpty()) {
                    outputBuffer.append(" ");
                    outputBuffer.append(str3);
                }
            }
            return outputBuffer.toString();
        }


        /**
         * Erzeugt eine ID für den übergebenen Record
         *
         * @param importRec
         * @return eine gültige ID oder NULL
         */
        public iPartsReplaceConstPartId getReplaceConstPartId(Map<String, String> importRec, int recordNo) {
            String materialNr = handleValueOfSpecialField(TS7_TEIL, importRec);
            String sdata = handleValueOfSpecialField(TS7_SDA, importRec);
            if ((materialNr.isEmpty()) || (sdata.isEmpty())) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 fehlerhaft (ungültiges Teil \"%2\" oder SDA \"%3\")",
                                                            String.valueOf(recordNo), materialNr, sdata),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                return null;
            } else {
                // Die ID bauen
                return new iPartsReplaceConstPartId(materialNr, sdata);
            }
        }
    }
}
