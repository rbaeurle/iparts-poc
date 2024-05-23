/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsCodeDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für Codestamm (RES)
 */
public class CodeMasterDataImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    public static final String DIALOG_TABLENAME = "RES";
    public static final String IMPORT_TABLENAME = TABLE_NAME_PREFIX + DIALOG_TABLENAME;

    public static final String RES_SN = "RES_SN";
    public static final String RES_PGKZ = "RES_PGKZ";
    public static final String RES_SPS = "RES_SPS";
    public static final String RES_BEN = "RES_BEN";
    public static final String RES_SDA = "RES_SDA";
    public static final String RES_SDB = "RES_SDB";

    private HashMap<String, String> mappingCodeData;
    private String[] primaryKeysCodeDataImport;

    private Set<iPartsCodeDataId> alreadyImportedIds;
    private Map<iPartsCodeDataId, iPartsDataCodeList> preMap;

    private final boolean importToDB = true;
    private final boolean doBufferSave = false;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public CodeMasterDataImporter(EtkProject project) {
        super(project, "!!DIALOG-Codestamm (RES)",
              new FilesImporterFileListType(TABLE_DA_CODE, DC_CODE_DATA, false, false, true, new String[]{ MimeTypes.EXTENSION_XML }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysCodeDataImport = new String[]{ RES_SN, RES_PGKZ, RES_SDA };
        mappingCodeData = new HashMap<>();
        mappingCodeData.put(FIELD_DC_PGRP, RES_PGKZ);
        mappingCodeData.put(FIELD_DC_SDATA, RES_SDA);
        mappingCodeData.put(FIELD_DC_SDATB, RES_SDB);
        mappingCodeData.put(FIELD_DC_DESC, RES_BEN);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysCodeDataImport, RES_BEN, RES_SPS));
        importer.setMustHaveData(StrUtils.mergeArrays(primaryKeysCodeDataImport, RES_SPS));

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
    protected void preImportTask() {
        super.preImportTask();
        alreadyImportedIds = new HashSet<>();
        preMap = new HashMap<>();
        setBufferedSave(doBufferSave);
    }


    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        CodeImportHelper helper = new CodeImportHelper(getProject(), mappingCodeData, TABLE_DA_CODE);
        // Aus der Sachnummer die CodeID und die Produktgruppe extrahieren (siehe WikiPage)
        CodeAndProduct codeAndProduct = new CodeAndProduct(helper.handleValueOfSpecialField(RES_SN, importRec));
        if (!codeAndProduct.isValid()) {
            cancelImport(translateForLog("!!Record %1 fehlerhaft (ungültige Codenummer Struktur: %2)",
                                         String.valueOf(recordNo), importRec.get(RES_SN)));
            return;
        }
        // Zusätzlicher Check ob die extrahierte Produktgruppe gleich der Gruppe im Feld ist
        if (!codeAndProduct.getProductGroup().equals(helper.handleValueOfSpecialField(RES_PGKZ, importRec))) {
            cancelImport(translateForLog("!!Record %1 fehlerhaft (Produktgruppe stimmt nicht überein. Codenummer: %2. RES_PGKZ: %3)",
                                         String.valueOf(recordNo), importRec.get(RES_SN), importRec.get(RES_PGKZ)));
            return;
        }
        // DAIMLER-1746: Wegen Codes über MAD Migration wurden die PKs um die Baureihe ergänzt. Beim Dialog-Import von RES XML Dateien bleibt das Feld für Baureihe leer.
        iPartsCodeDataId id = new iPartsCodeDataId(codeAndProduct.getCodeNr(), "", codeAndProduct.getProductGroup(),
                                                   helper.handleValueOfSpecialField(RES_SDA, importRec), iPartsImportDataOrigin.DIALOG);
        iPartsDIALOGLanguageDefs langDef = iPartsDIALOGLanguageDefs.getType(importRec.get(RES_SPS));
        if (langDef == iPartsDIALOGLanguageDefs.DIALOG_UNKNOWN) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (ungültige Sprachkennung: %2)", String.valueOf(recordNo),
                                                        importRec.get(RES_SPS)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        if (langDef == iPartsDIALOGLanguageDefs.DIALOG_DE) {
            // DE Datensatz: lade alle Code-Datensätze und liefere den aktuellsten
            iPartsDataCode codePreData = getPredecessor(id);
            if (codePreData == null) {
                // keinen gefunden => neu anlegen
                codePreData = createCodeData(id);
                helper.fillOverrideCompleteDataForDIALOGReverse(codePreData, importRec, langDef);
                addToPreMap(codePreData);
            } else {
                // Abfrage nach neuem DE-Satz
                int compareResult = codePreData.getFieldValue(FIELD_DC_SDATA).compareTo(id.getSdata());
                if (compareResult > 0) {
                    // ImportRec Datensatz nicht zum aktuellsten in der DB => Suche DB-Datensatz mit gleichem SDatA
                    codePreData = getPredecessorExact(id);
                    if (codePreData == null) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (bereits neueres SDatA in der DB vorhanden)", String.valueOf(recordNo)),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                        reduceRecordCount();
                        return;
                    }
                    // Bestehenden Datensatz aktualisieren
                    helper.fillOverrideCompleteDataForDIALOGReverse(codePreData, importRec, langDef);
                } else if (compareResult < 0) {
                    // neuer DE ImportRec Datensatz => lege neuen Datensatz an
                    // ggf. aktualisiere SDatB des bisherigen
                    if (codePreData.getFieldValue(FIELD_DC_SDATB).isEmpty()) {
                        codePreData.setFieldValue(FIELD_DC_SDATB, id.getSdata(), DBActionOrigin.FROM_EDIT);
                    }
                    // Bereits vorhandene Texte in allen Sprachen merken
                    EtkMultiSprache existingTexts = codePreData.getFieldValueAsMultiLanguage(FIELD_DC_DESC);
                    // Beim neuen Datensatz ein neues Text-Objekt anlegen, alte Texte zuweisen, Text-ID leeren und durch
                    // den Importext hinzufügen
                    EtkMultiSprache newTexts = new EtkMultiSprache();
                    newTexts.assignData(existingTexts);
                    newTexts.setTextId("");
                    // neuen DE Record anlegen
                    codePreData = createCodeData(id);
                    // Die gemerkten Sprachtexte übernehmen
                    codePreData.setFieldValueAsMultiLanguage(FIELD_DC_DESC, newTexts, DBActionOrigin.FROM_EDIT);
                    // ... und das Objekt neu besetzen. Dabei wird der deutsche Text neu gesetzt.
                    helper.fillOverrideCompleteDataForDIALOGReverse(codePreData, importRec, langDef);
                    addToPreMap(codePreData);
                } else {
                    // gleiches SDatA => nur update
                    helper.fillOverrideCompleteDataForDIALOGReverse(codePreData, importRec, langDef);
                }
            }
        } else {
            // Fremdsprache: Suche den zugehörigen DE-Datensatz
            iPartsDataCode codePreData = getNotDEPredecessor(id);
            if (codePreData == null) {
                // sollte nicht passieren: Sprach-Record VOR DE-Record
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen (Fremdsprachen Record vor dem DE Record)", String.valueOf(recordNo)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            } else {
                deleteOldImportData(codePreData, id);
                // nur Fremdsprache übernehmen
                helper.fillOverrideLanguageTextForDIALOGReverse(codePreData, importRec, langDef);
            }
        }
    }

    private iPartsDataCode createCodeData(iPartsCodeDataId id) {
        iPartsDataCode codePreData = new iPartsDataCode(getProject(), id);
        if (!codePreData.existsInDB()) {
            codePreData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        return codePreData;
    }

    private iPartsCodeDataId getPreCodeDataId(iPartsCodeDataId id) {
        return new iPartsCodeDataId(id.getCodeId(), id.getSeriesNo(), id.getProductGroup(), "", id.getSource());
    }

    private iPartsDataCodeList getValueFromMap(iPartsCodeDataId id) {
        return preMap.get(getPreCodeDataId(id));
    }

    private iPartsDataCode getPredecessor(iPartsCodeDataId id) {
        iPartsDataCodeList dataCodeList = getValueFromMap(id);
        if (dataCodeList == null) {
            iPartsCodeDataId preId = getPreCodeDataId(id);
            List<iPartsDataCode> loadList = iPartsDataCodeList.loadCodeDataSortedWithoutJoin(getProject(), preId).getAsList();
            dataCodeList = new iPartsDataCodeList();
            for (iPartsDataCode dataCode : loadList) {
                // nur die Datensätze übernehmen, die einen DE Text besitzen (bisheriger fehlerhafter Import)
                if (!dataCode.getFieldValueAsMultiLanguage(FIELD_DC_DESC).getText(Language.DE.getCode()).isEmpty()) {
                    addToListIfNotAlreadyImported(dataCodeList, dataCode);
                }
            }
            preMap.put(preId, dataCodeList);
        }
        if (!dataCodeList.isEmpty()) {
            return dataCodeList.getLast();
        }
        return null;
    }

    private iPartsDataCode getPredecessorExact(iPartsCodeDataId id) {
        iPartsDataCodeList dataCodeList = getValueFromMap(id);
        if (dataCodeList != null) {
            for (int lfdNr = dataCodeList.size() - 1; lfdNr >= 0; lfdNr--) {
                if (dataCodeList.get(lfdNr).getAsId().equals(id)) {
                    return dataCodeList.get(lfdNr);
                }
            }
        }
        return null;
    }

    private iPartsDataCode getNotDEPredecessor(iPartsCodeDataId id) {
        // falls die Fremdsprache VOR dem DE-Datensatz kommt
        if (getPredecessor(id) != null) {
            iPartsDataCodeList dataCodeList = getValueFromMap(id);
            if (dataCodeList != null) {
                for (int lfdNr = dataCodeList.size() - 1; lfdNr >= 0; lfdNr--) {
                    int compareResult = dataCodeList.get(lfdNr).getFieldValue(FIELD_DC_SDATA).compareTo(id.getSdata());
                    // SDatA des DE DB-Datensatzes muss kleiner/gleich zu aktuellem Fremdsprachen sDatA sein
                    if (compareResult <= 0) {
                        return dataCodeList.get(lfdNr);
                    }
                }
            }
        }
        return null;
    }

    /**
     * ein neues {@link iPartsDataCode} Element zur pre-HashMap hinzufügen
     *
     * @param dataCode
     */
    private void addToPreMap(iPartsDataCode dataCode) {
        iPartsDataCodeList dataCodeList = getValueFromMap(dataCode.getAsId());
        if (dataCodeList == null) {
            iPartsCodeDataId preId = getPreCodeDataId(dataCode.getAsId());
            dataCodeList = new iPartsDataCodeList();
            preMap.put(preId, dataCodeList);
        }
        addToListIfNotAlreadyImported(dataCodeList, dataCode);
    }

    private void deleteOldImportData(iPartsDataCode masterDataCode, iPartsCodeDataId importId) {
        int compareResult = masterDataCode.getAsId().getSdata().compareTo(importId.getSdata());
        if (compareResult != 0) {
            iPartsDataCode importDataCode = new iPartsDataCode(getProject(), importId);
            if (importDataCode.existsInDB()) {
                iPartsDataCodeList dataCodeList = getValueFromMap(masterDataCode.getAsId());
                if (dataCodeList != null) {
                    List<iPartsDataCode> deletedList = dataCodeList.getDeletedList();
                    for (iPartsDataCode deletedDataCode : deletedList) {
                        if (deletedDataCode.getAsId().equals(importDataCode.getAsId())) {
                            // bereits in deletedList
                            return;
                        }
                    }
                    dataCodeList.delete(importDataCode, true, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    private void addToListIfNotAlreadyImported(iPartsDataCodeList dataCodeList, iPartsDataCode dataCode) {
        if (!alreadyImportedIds.contains(dataCode.getAsId())) {
            dataCodeList.add(dataCode, DBActionOrigin.FROM_DB);
            alreadyImportedIds.add(dataCode.getAsId());
        } else {
            getMessageLog().fireMessage(translateForLog("!!Code %1 übersprungen, da der Datensatz bereits in der Importdatei vorkam.",
                                                        dataCode.getAsId().toStringForLogMessages()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                getMessageLog().fireMessage(translateForLog("!!Speichere modifizierte Daten"), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                for (iPartsDataCodeList dataCodeList : preMap.values()) {
                    if (dataCodeList.isModifiedWithChildren()) {
                        int toDelete = dataCodeList.getDeletedList().size();
                        if (toDelete > 0) {
                            getMessageLog().fireMessage(translateForLog("!!%1 Datensätze werden gelöscht bei %2",
                                                                        String.valueOf(toDelete), getPreCodeDataId(dataCodeList.getLast().getAsId()).toString()),
                                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                        }
                    }
                }

                getMessageLog().fireProgress(0, preMap.size(), "", true, false);
                int counter = 0;
                for (Map.Entry<iPartsCodeDataId, iPartsDataCodeList> entry : preMap.entrySet()) {
                    counter++;
                    entry.getValue().saveToDB(getProject(), false);
                    getMessageLog().fireProgress(counter, preMap.size(), "", true, true);
                }
                getMessageLog().hideProgress();
            }
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_DA_CODE)) {
            getProject().getDbLayer().delete(TABLE_DA_CODE, new String[]{ FIELD_DC_SOURCE }, new String[]{ iPartsImportDataOrigin.DIALOG.getOrigin() });
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_DA_CODE)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class CodeImportHelper extends DIALOGImportHelper {

        public CodeImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(RES_SN)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            if (sourceField.equals(RES_SDA) || sourceField.equals(RES_SDB)) {
                value = getDIALOGDateTimeValue(value);
            }
            return value;
        }

    }

    /**
     * Klasse zum Extrahieren der Produktgruppe und der CodeId
     */
    private static class CodeAndProduct {

        private String productGroup;
        private String codeNr;
        private final String unmodifiedCode;

        public CodeAndProduct(String unmodifiedCode) {
            this.unmodifiedCode = unmodifiedCode;
            extractCodeIDAndProductGroup();
        }

        private void extractCodeIDAndProductGroup() {
            if ((unmodifiedCode != null) && (unmodifiedCode.length() >= 3)) {
                productGroup = unmodifiedCode.substring(1, 2).trim();
                codeNr = unmodifiedCode.substring(2).trim();
            }
        }

        public String getProductGroup() {
            return productGroup;
        }

        public String getCodeNr() {
            return codeNr;
        }

        public String getUnmodifiedCode() {
            return unmodifiedCode;
        }

        public boolean isValid() {
            return StrUtils.isValid(getUnmodifiedCode(), getProductGroup(), getCodeNr());
        }
    }
}
