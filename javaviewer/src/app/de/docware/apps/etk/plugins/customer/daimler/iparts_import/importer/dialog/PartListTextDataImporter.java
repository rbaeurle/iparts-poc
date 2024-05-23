/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataDialogPartListText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataDialogPartListTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPartlistTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogPartListTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * DIALOG Importer für Stücklistentexte in der Konstruktionsstückliste (BCTX)
 */
public class PartListTextDataImporter extends AbstractDIALOGDataImporter implements iPartsConst, EtkDbConst {

    public static final String DIALOG_TABLENAME = "BCTX";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    // Elementnamen aus AS/PLM XML
    public static final String BCTX_PG = "BCTX_PG";
    public static final String BCTX_BR = "BCTX_BR";
    public static final String BCTX_RAS = "BCTX_RAS";
    public static final String BCTX_POSE = "BCTX_POSE";
    public static final String BCTX_PV = "BCTX_PV";
    public static final String BCTX_WW = "BCTX_WW";
    public static final String BCTX_ETZ = "BCTX_ETZ";
    public static final String BCTX_SPS = "BCTX_SPS";
    public static final String BCTX_SDATA = "BCTX_SDATA";
    public static final String BCTX_SDATB = "BCTX_SDATB";
    public static final String BCTX_FED = "BCTX_FED";
    public static final String BCTX_AATAB = "BCTX_AATAB";
    public static final String BCTX_STR = "BCTX_STR";
    public static final String BCTX_TXTART = "BCTX_TXTART";
    public static final String BCTX_FS = "BCTX_FS";
    public static final String BCTX_TEXT = "BCTX_TEXT";
    public static final String BCTX_RFG = "BCTX_RFG";

    private final PartListTextDataImportHelper importHelper;

    private String tableName;
    private HashMap<String, String> dialogMapping;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean doBufferedSave = true; //sollen die Daten blockweise (gepuffert) abgespeichert werden?
    private Map<iPartsDialogPartListTextId, Map<iPartsDialogPartListTextId, iPartsDataDialogPartListText>> idWithoutSdataToGermanData;
    private Map<iPartsDialogPartListTextId, Map<iPartsDialogPartListTextId, iPartsDataDialogPartListText>> idWithoutSdataToOtherData;
    private Map<iPartsDialogPartListTextId, TextForPartListId> idToGermanTextMap;
    private Map<iPartsDialogPartListTextId, TextForPartListId> idToForeignTextMap;


    public PartListTextDataImporter(EtkProject project) {
        super(project, "!!DIALOG-Konstruktionsstückliste Zusatztexte (BCTX)", new FilesImporterFileListType(TABLE_DA_DIALOG_PARTLIST_TEXT, DD_PL_TEXT_DATA, false, false, true, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
        importHelper = new PartListTextDataImportHelper(getProject(), dialogMapping, tableName);
    }

    private void initMapping() {
        this.tableName = TABLE_DA_DIALOG_PARTLIST_TEXT;

        // Mapping XML-Elementnamen -> Tabellenfelder
        dialogMapping = new HashMap<>();
        dialogMapping.put(FIELD_DD_PLT_PG, BCTX_PG);
        dialogMapping.put(FIELD_DD_PLT_POSE, BCTX_POSE);
        dialogMapping.put(FIELD_DD_PLT_POSV, BCTX_PV);
        dialogMapping.put(FIELD_DD_PLT_WW, BCTX_WW);
        dialogMapping.put(FIELD_DD_PLT_ETZ, BCTX_ETZ);
        dialogMapping.put(FIELD_DD_PLT_SDATA, BCTX_SDATA);
        dialogMapping.put(FIELD_DD_PLT_SDATB, BCTX_SDATB);
        dialogMapping.put(FIELD_DD_PLT_FED, BCTX_FED);
        dialogMapping.put(FIELD_DD_PLT_AATAB, BCTX_AATAB);
        dialogMapping.put(FIELD_DD_PLT_STR, BCTX_STR);
        dialogMapping.put(FIELD_DD_PLT_TEXTKIND, BCTX_TXTART);
        dialogMapping.put(FIELD_DD_PLT_RFG, BCTX_RFG);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustExists = new String[]{ BCTX_BR, BCTX_RAS, BCTX_POSE, BCTX_TXTART };
        String[] mustHaveData = new String[]{ BCTX_BR, BCTX_RAS, BCTX_POSE, BCTX_TXTART };

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
    protected void preImportTask() {
        super.preImportTask();
        idToGermanTextMap = new HashMap<>();
        idToForeignTextMap = new HashMap<>();
        idWithoutSdataToGermanData = new HashMap<>();
        idWithoutSdataToOtherData = new HashMap<>();
        setBufferedSave(doBufferedSave);

    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        iPartsDialogPartListTextId partListTextId = getDialogPartListAddDataId(importRec, importHelper);
        if (partListTextId == null) {
            String msg;
            if (importRec.get(BCTX_BR).isEmpty()) {
                msg = translateForLog("!!Record %1 fehlerhaft (ungültige Baureihe: %2)",
                                      String.valueOf(recordNo), importRec.get(BCTX_BR));
            } else {
                msg = translateForLog("!!Record %1 fehlerhaft (ungültige HM/M/SM Struktur: %2)",
                                      String.valueOf(recordNo), importRec.get(BCTX_RAS));
            }
            getMessageLog().fireMessage(msg, MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!importHelper.checkImportRelevanceForSeries(BCTX_BR, importRec, getInvalidSeriesSet(), this)) {
            return;
        }

        // Ein Text kann über mehrere Records verteilt sein. Der erste Record hat FS=01; Folgesätze dann 02, 03, usw.
        int continuationSequenceNo = StrUtils.strToIntDef(importRec.get(BCTX_FS), -1);
        // Kleiner als 1 -> Fehler in Importdaten
        if (continuationSequenceNo < 1) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 fehlerhaft (ungültige Textfolgenummer: %2)",
                                                        String.valueOf(recordNo), importRec.get(BCTX_FS)));
            setRecordInvalid(partListTextId);
            reduceRecordCount();
            return;
        } else {
            // Sprache aus dem Datensatz
            iPartsDIALOGLanguageDefs langDef = getLanguageDefinition(importRec);
            // Um ähnliche Datensätze zu aggregieren, wird eine abstrakte ID erzeugt
            iPartsDialogPartListTextId idWithoutSdata = partListTextId.getIdWithoutSdata();
            fillMapWithGermanDBObjects(idWithoutSdata);
            // Äbhängig von der Datensatzsprache müssen unterschiedliche Sets und Maps verwendet werden
            // Im Allgemeinen wird unterschieden zwischen einem deutschen und einem fremndsprachigen Datensatz
            Map<iPartsDialogPartListTextId, Map<iPartsDialogPartListTextId, iPartsDataDialogPartListText>> currentIdWithoutSdatToIdMap; // ID ohne Sdata -> Map mit echter ID auf DBObject
            Map<iPartsDialogPartListTextId, TextForPartListId> currentIdToTextMap; // echte ID auf zusammengesetzten Text
            switch (langDef) {
                case DIALOG_DE:
                    currentIdWithoutSdatToIdMap = idWithoutSdataToGermanData;
                    currentIdToTextMap = idToGermanTextMap;
                    break;
                case DIALOG_UNKNOWN:
                    getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (unbekannter Sprachschlüssel: %2)",
                                                                String.valueOf(recordNo), importRec.get(BCTX_SPS)));
                    reduceRecordCount();
                    return;
                default:
                    currentIdWithoutSdatToIdMap = idWithoutSdataToOtherData;
                    currentIdToTextMap = idToForeignTextMap;
                    break;
            }

            // Das Map mit allen Datensätzen, die via abstrakter ID aggregiert wurden ("abstrakte ID" bedeutet normale ID ohne SDATA)
            Map<iPartsDialogPartListTextId, iPartsDataDialogPartListText> realIdToObjectMap = currentIdWithoutSdatToIdMap.computeIfAbsent(idWithoutSdata, k -> new HashMap<>());
            iPartsDataDialogPartListText partListText;
            if (realIdToObjectMap.containsKey(partListTextId)) {
                // Hol das Objekt aus dem Cache bzw. der DB (Cache wurde ja mit DB Daten befüllt)
                partListText = realIdToObjectMap.get(partListTextId);
            } else {
                partListText = new iPartsDataDialogPartListText(getProject(), partListTextId);
                // Weil der Datensatz nicht im Cache existiert muss er neu angelegt werden (Cache wird ja mti DB Datensätzen befüllt)
                partListText.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                realIdToObjectMap.put(partListTextId, partListText);
            }
            // Befülle das Objekt nur, wenn er sich um den ersten Datensatz in einer Folge handelt. Hier wird nicht bezüglich der
            // Sprache unterschieden, weil für die späteren Berechnungen die SDATA und SDATB Werte gebraucht werden
            if (continuationSequenceNo == 1) {
                importHelper.fillOverrideCompleteDataForDIALOGReverse(partListText, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
            }
            // Hier werden die alle Folgesätze zu einem Text zusammengeführt (egal welche Sprache)
            // Hole den eigentlichen Text und speicher ihn samt Schlüssel, Sprache und Folgenummer
            String text = importHelper.handleValueOfSpecialField(BCTX_TEXT, importRec);
            TextForPartListId textForPartListId = currentIdToTextMap.get(partListTextId);
            if (textForPartListId == null) {
                textForPartListId = new TextForPartListId(partListTextId);
                currentIdToTextMap.put(partListTextId, textForPartListId);
            }
            textForPartListId.addText(langDef, continuationSequenceNo, text);
        }
    }

    /**
     * Befüllt den Cache mit Datensätzen aus der Datenbank. Gesucht werden "ähnliche" Datensätze (Schlüsselfelder bis auf SDATA gleich)
     *
     * @param idWithoutSdata
     */
    private void fillMapWithGermanDBObjects(iPartsDialogPartListTextId idWithoutSdata) {
        if (!idWithoutSdataToGermanData.containsKey(idWithoutSdata)) {
            iPartsDataDialogPartListTextList partListTextList = iPartsDataDialogPartListTextList.loadAllRelatedDataForPartListTextId(getProject(), idWithoutSdata);
            if (!partListTextList.isEmpty()) {
                Map<iPartsDialogPartListTextId, iPartsDataDialogPartListText> realIdToObjectMap = new HashMap<>();
                for (iPartsDataDialogPartListText partListTextFromDB : partListTextList) {
                    realIdToObjectMap.put(partListTextFromDB.getAsId(), partListTextFromDB);
                }
                idWithoutSdataToGermanData.put(idWithoutSdata, realIdToObjectMap);
            }
        }
    }

    @Override
    protected void postImportTask() {
        // zwischengespeicherten Record in DB speichern
        if (!isCancelled()) {
            fillDataObjectsWithText();
        }
        super.postImportTask();
        idToForeignTextMap = null;
        idToGermanTextMap = null;
        idWithoutSdataToGermanData = null;
        idWithoutSdataToOtherData = null;
    }

    /**
     * Verarbeitet die aufgesammelten Textinformationen und speichert diese in der DB
     */
    private void fillDataObjectsWithText() {
        // Zuerst müssen die fremdsprachigen Texte den deutschen Texten hinzugefügt werden
        // Es wird zu jedem deutschen Text die möglichen fremdsprachigen Texte geholt und für die Bestimmung der
        // passenden Texte weitergegeben
        for (Map.Entry<iPartsDialogPartListTextId, Map<iPartsDialogPartListTextId, iPartsDataDialogPartListText>> germanTextDatasetsMap : idWithoutSdataToGermanData.entrySet()) {
            // Pro ID ohne SDATA aus der Map mit deutschen Texten werden die dazugehörigen fremsprachigen Texte geholt
            Map<iPartsDialogPartListTextId, iPartsDataDialogPartListText> relatedForeignTextIds = idWithoutSdataToOtherData.get(germanTextDatasetsMap.getKey());
            if (relatedForeignTextIds == null) {
                continue;
            }
            for (iPartsDataDialogPartListText germanPartLIstTextObject : germanTextDatasetsMap.getValue().values()) {
                addForeignTextToGermanDataset(germanPartLIstTextObject, relatedForeignTextIds.values());
            }
        }
        if (importToDB) {
            for (Map<iPartsDialogPartListTextId, iPartsDataDialogPartListText> allPartListTexts : idWithoutSdataToGermanData.values()) {
                for (iPartsDataDialogPartListText partListText : allPartListTexts.values()) {
                    TextForPartListId textForPartListId = idToGermanTextMap.get(partListText.getAsId());
                    if (textForPartListId != null) {
                        partListText.setFieldValueAsMultiLanguage(FIELD_DD_PLT_TEXT, textForPartListId.getAsMultilangText(), DBActionOrigin.FROM_EDIT);
                        saveToDB(partListText);
                    }
                }
            }
        }
    }

    /**
     * Die deutschen Texte zum übergebenen <i>germanDataset</i> Datensatz werden die übergebenen fremdsprachigen Texte
     * hinzugefügt, sofern die SDATA und SDATB Angaben übereinstimmen.
     *
     * @param germanDataset
     * @param relatedForeignDatasets
     */
    private void addForeignTextToGermanDataset(iPartsDataDialogPartListText germanDataset, Collection<iPartsDataDialogPartListText> relatedForeignDatasets) {
        long sdatbFromGermanText = iPartsPartlistTextHelper.getTextSdatValueAsLong(germanDataset.getFieldValue(FIELD_DD_PLT_SDATB));
        // Hat SDATB den Wert "-1", dann handelt es um fehlerhafte Daten
        if ((sdatbFromGermanText == -1)) {
            return;
        }
        // Hole den deutschen Text
        TextForPartListId germanText = idToGermanTextMap.get(germanDataset.getAsId());
        if (germanText != null) {
            // Sortiere die Daten nach ihrem SDATA (fremdsprachigen Texte)
            Map<String, iPartsDataDialogPartListText> sortedForeignTexts = new TreeMap<>(Comparator.reverseOrder());
            for (iPartsDataDialogPartListText affectedDataset : relatedForeignDatasets) {
                sortedForeignTexts.put(affectedDataset.getAsId().getSdata(), affectedDataset);
            }
            // Temporäres Objekt für die gefundenen fremdsprachigen Texte
            TextForPartListId aggregatedText = new TextForPartListId(germanDataset.getAsId());
            // Durchlaufe alle verknüpften fremdsprachigen Texte
            for (iPartsDataDialogPartListText possibleDataObject : sortedForeignTexts.values()) {
                // Hat SDATA oder SDATB den Wert "-1", dann handelt es um fehlerhafte Daten
                long sdataFromPossibleDataObject = iPartsPartlistTextHelper.getTextSdatValueAsLong(possibleDataObject.getFieldValue(FIELD_DD_PLT_SDATA));
                long sdatbFromPossibleDataObject = iPartsPartlistTextHelper.getTextSdatValueAsLong(possibleDataObject.getFieldValue(FIELD_DD_PLT_SDATB));
                if ((sdatbFromPossibleDataObject == -1) || (sdataFromPossibleDataObject == -1)) {
                    continue;
                }
                // Fremdsprachentext passt zum deutschen Text, wenn:
                // 1. Fremdsprachen SDATA < deutsche Text SDATB
                // 2. Fremdsprachen SDATB >= deutscher Text SDATB
                boolean sdataIsEarlier = sdataFromPossibleDataObject < sdatbFromGermanText;
                boolean sdatbIsLaterOrEqual = sdatbFromPossibleDataObject >= sdatbFromGermanText;
                if (sdataIsEarlier && sdatbIsLaterOrEqual) {
                    TextForPartListId foreignText = idToForeignTextMap.get(possibleDataObject.getAsId());
                    aggregatedText.addAllTexts(foreignText, false);
                }
            }
            // Füge dem deutschen Text, die zugehörigen Fremdsprachentexte hinzu
            germanText.addAllTexts(aggregatedText, true);
        }
    }

    /**
     * Die Sprachdefinition aus den Importdaten holen
     *
     * @param importRec
     * @return
     */
    private iPartsDIALOGLanguageDefs getLanguageDefinition(Map<String, String> importRec) {
        return iPartsDIALOGLanguageDefs.getType(importRec.get(BCTX_SPS));
    }

    private iPartsDialogPartListTextId getDialogPartListAddDataId(Map<String, String> importRec, DIALOGImportHelper importHelper) {

        HmMSmId hmMSmId = HmMSmId.getIdFromRaster(importHelper.handleValueOfSpecialField(BCTX_BR, importRec),
                                                  importHelper.handleValueOfSpecialField(BCTX_RAS, importRec));
        if ((hmMSmId != null) && hmMSmId.isValidId()) {
            return new iPartsDialogPartListTextId(hmMSmId,
                                                  importHelper.handleValueOfSpecialField(BCTX_POSE, importRec),
                                                  importHelper.handleValueOfSpecialField(BCTX_PV, importRec),
                                                  importHelper.handleValueOfSpecialField(BCTX_WW, importRec),
                                                  importHelper.handleValueOfSpecialField(BCTX_ETZ, importRec),
                                                  importHelper.handleValueOfSpecialField(BCTX_TXTART, importRec),
                                                  importHelper.handleValueOfSpecialField(BCTX_SDATA, importRec));
        }
        return null;
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

    public void setRecordInvalid(iPartsDialogPartListTextId invalidRecord) {
        if (idWithoutSdataToGermanData.containsKey(invalidRecord.getIdWithoutSdata())) {
            idWithoutSdataToGermanData.get(invalidRecord.getIdWithoutSdata()).remove(invalidRecord);
        }
        if (idWithoutSdataToOtherData.containsKey(invalidRecord.getIdWithoutSdata())) {
            idWithoutSdataToOtherData.get(invalidRecord.getIdWithoutSdata()).remove(invalidRecord);
        }
        idToGermanTextMap.remove(invalidRecord);
        idToForeignTextMap.remove(invalidRecord);
    }

    private class PartListTextDataImportHelper extends DIALOGImportHelper {

        public PartListTextDataImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(BCTX_SDATA) || sourceField.equals(BCTX_SDATB)) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(BCTX_BR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }
    }

    /**
     * Hilfsklasse für das Aufsammeln von Texten samt Sprache und Folgenummer
     */
    private static class TextForPartListId {

        private final iPartsDialogPartListTextId partListTextId;
        private final Map<iPartsDIALOGLanguageDefs, Map<Integer, String>> languageSortMap;

        public TextForPartListId(iPartsDialogPartListTextId partListTextId) {
            this.partListTextId = partListTextId;
            languageSortMap = new HashMap<>();
        }

        public iPartsDialogPartListTextId getPartListTextId() {
            return partListTextId;
        }

        public void addText(iPartsDIALOGLanguageDefs language, int order, String text) {
            Map<Integer, String> sortedTextMap = languageSortMap.computeIfAbsent(language, k -> new TreeMap<>());
            sortedTextMap.put(order, text);
        }

        /**
         * Liefert die in diesem Objekt enthaltenen Textbausteine als {@link EtkMultiSprache} Objekt
         *
         * @return
         */
        public EtkMultiSprache getAsMultilangText() {
            EtkMultiSprache existingMultisprache = new EtkMultiSprache();
            if (!languageSortMap.isEmpty()) {
                for (Map.Entry<iPartsDIALOGLanguageDefs, Map<Integer, String>> languageEntry : languageSortMap.entrySet()) {
                    existingMultisprache.setText(languageEntry.getKey().getDbValue(), makeText(languageEntry.getValue().values()));
                }
            }
            return existingMultisprache;
        }

        /**
         * Erstellt aus den übergebenen Textbausteinen einen zusammenhängenden Text
         *
         * @param sortedTexts
         * @return
         */
        private String makeText(Collection<String> sortedTexts) {
            StringBuilder text = new StringBuilder();
            if ((sortedTexts != null) && !sortedTexts.isEmpty()) {
                for (String textPart : sortedTexts) {
                    if (text.length() > 0) {
                        text.append(" ");
                    }
                    text.append(textPart);

                }
            }
            return text.toString();
        }

        public Map<iPartsDIALOGLanguageDefs, Map<Integer, String>> getLanguageSortMap() {
            return languageSortMap;
        }

        /**
         * Fügt diesem Text-Objekt die Texte aus dem übergebenen Text-Objekt hinzu.
         *
         * @param otherTexts
         */
        public void addAllTexts(TextForPartListId otherTexts, boolean overwriteTextIfExists) {
            if (otherTexts != null) {
                for (Map.Entry<iPartsDIALOGLanguageDefs, Map<Integer, String>> languageSortedMap : otherTexts.getLanguageSortMap().entrySet()) {
                    iPartsDIALOGLanguageDefs language = languageSortedMap.getKey();
                    if (!overwriteTextIfExists && getLanguageSortMap().containsKey(language)) {
                        continue;
                    }
                    for (Map.Entry<Integer, String> textWithOrder : languageSortedMap.getValue().entrySet()) {
                        int order = textWithOrder.getKey();
                        String text = textWithOrder.getValue();
                        addText(language, order, text);
                    }
                }
            }
        }
    }
}
