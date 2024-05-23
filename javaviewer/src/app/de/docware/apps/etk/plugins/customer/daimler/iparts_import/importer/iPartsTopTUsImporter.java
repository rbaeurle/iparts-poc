/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataTopTU;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsTopTUId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.AbstractCatalogDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer für die Referenz auf hoch frequentierte TUs aus einer CSV-Datei.
 */
public class iPartsTopTUsImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    private static final String TABLE_NAME = TABLE_DA_TOP_TUS;

    // Feldnamen in CSV-Header
    private static final String COUNTRY = "country";  // ISO 3166
    private static final String PRODUCT_NO = "identContext.productId";
    private static final String KG = "KG";
    private static final String TU = "TU";
    private static final String RANK = "Rank";

    private final String[] headerNames = new String[]{
            COUNTRY,
            PRODUCT_NO,
            KG,
            TU,
            RANK };

    private final boolean importToDB = true;
    private final boolean doBufferSave = true;
    private boolean tableIsEmpty = false;
    private HashMap<String, String> mapping;

    private TopTUsImportHelper importHelper;

    public iPartsTopTUsImporter(EtkProject project) {
        super(project, "!!ShoppingCart, Import Referenz auf hoch-frequentierte TUs", true,
              new FilesImporterFileListType(TABLE_NAME, "!!ShoppingCart, Import Referenz auf hoch-frequentierte TUs",
                                            true, true, false,
                                            new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        mapping = new HashMap<>();
        // Alle Felder vom Primärschlüssel werden über diesen gesetzt und müssen nicht ins Mapping aufgenommen werden
        mapping.put(FIELD_DTT_RANK, RANK);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        // Hier bemerkte Fehler führen zum Abbruch, das ist aber nicht gewünscht.
        importer.setMustExists(headerNames);
        importer.setMustHaveData(headerNames);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        boolean fehler = false;
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            if (!importer.getTableNames().get(0).equals(TABLE_NAME)) {
                getMessageLog().fireMessage(translateForLog("!!Falscher Importtabellenname %2 statt %3",
                                                            importer.getTableNames().get(0),
                                                            TABLE_NAME),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                fehler = true;
            }
        }
        return !fehler;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    @Override
    protected void preImportTask() {
        importHelper = new TopTUsImportHelper(getProject(), mapping, TABLE_NAME);
        setBufferedSave(doBufferSave);
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        if ((recordNo == 1) && (!tableIsEmpty)) {
            cancelImport(translateForLog("!!Tabelle %1 wurde vor dem Import nicht geleert. Abbruch!", TABLE_NAME));
            return;
        }

        // Erst mal die Werte aus dem Import-Record extrahieren ...
        iPartsTopTUId topTUId = importHelper.buildTopTuId(importRec);
        String rank = importHelper.handleValueOfSpecialField(RANK, importRec);

        // ... und dann die logische Prüfung der Importdaten anstoßen.
        if (!importHelper.checkValues(topTUId, rank, recordNo)) {
            reduceRecordCount();
            return;
        }

        // Ohne Fehler die Werte übernehmen und speichern (bufferedSave !)
        iPartsDataTopTU topTU = new iPartsDataTopTU(getProject(), topTUId);

        // Immer Vollversorgung, die Tabelle wurde vor dem Importieren geleert.
        // ==> ALLE einzulesenden Datensätze sind neu!
        // ==> kein "if (!topTU.existsInDB()) ..." nötig!
        topTU.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

        // Den einzigen Wert, der nicht im Schlüssel enthalten ist, übernehmen.
        topTU.setRank(rank, DBActionOrigin.FROM_EDIT);

        if (importToDB) {
            saveToDB(topTU);
        } else {
            reduceRecordCount();
        }
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        tableIsEmpty = removeAllExistingDataForTable(importFileType, TABLE_NAME);
        return tableIsEmpty;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_NAME)) {
            return importMasterData(prepareImporterKeyValue(importFile, TABLE_NAME, ',', withHeader, headerNames));
        }
        return false;
    }

    @Override
    public boolean importFiles(FilesImporterFileListType importFileType, List<DWFile> importFiles, boolean removeAllExistingData) {
        // Egal ob über das Menü oder via RFTSx, die Tabelle soll vorher gelöscht werden
        return super.importFiles(importFileType, importFiles, true);
    }

    /**
     * Der Helper
     */
    private class TopTUsImportHelper extends iPartsMainImportHelper {

        public TopTUsImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(KG) || sourceField.equals(TU) || sourceField.equals(RANK)) {
                if (StrUtils.isEmpty(value)) {
                    return "";
                }
                // Strings, die von Excel bzw. POI Lib als numerisch erkannt wurden, als String behandeln. Das kann nur die Felder
                // KG, TU und RANK betreffen.
                if (value.contains(".")) {
                    value = StrUtils.stringUpToCharacter(value, ".");
                }

                // KG und TU passend mit führenden Nullen auffüllen.
                if (StrUtils.isInteger(value)) {
                    int number = Integer.parseInt(value);
                    if (sourceField.equals(KG)) {
                        value = String.format("%02d", number);
                    } else if (sourceField.equals(TU)) {
                        value = String.format("%03d", number);
                    } else {
                        value = String.format("%d", number);
                    }
                }
            }
            return value;
        }

        public iPartsTopTUId buildTopTuId(Map<String, String> importRec) {
            return new iPartsTopTUId(handleValueOfSpecialField(PRODUCT_NO, importRec),
                                     handleValueOfSpecialField(COUNTRY, importRec),
                                     handleValueOfSpecialField(KG, importRec),
                                     handleValueOfSpecialField(TU, importRec));
        }

        /**
         * Logische Prüfung der Importdaten
         *
         * @param topTUId
         * @param rank
         * @param recordNo
         * @return
         */
        public boolean checkValues(iPartsTopTUId topTUId, String rank, int recordNo) {
            boolean isValid = topTUId.isValidId();
            if (isValid) {
                // ISO 3166 Ländercode
                String countryCode = topTUId.getCountryCode();
                // gegen das Enum [CountryISO3166] checken und bei unbekannten Sprachen eine Fehlermeldung ausgeben.
                if (!(iPartsLanguage.isValidDaimlerIsoCountryCode(getProject(), countryCode))) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültigem Länderode \"%2\" übersprungen.",
                                                                String.valueOf(recordNo), countryCode),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    isValid = false;
                }
            } else {
                // Produktnummer
                if (StrUtils.isEmpty(topTUId.getProductNo())) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerem Produkt übersprungen.",
                                                                String.valueOf(recordNo)),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }
                // ISO 3166 Ländercode
                String countryCode = topTUId.getCountryCode();
                if (StrUtils.isEmpty(countryCode)) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerem Länderode übersprungen.",
                                                                String.valueOf(recordNo)),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                } else {
                    // gegen das Enum [CountryISO3166] checken und bei unbekannten Sprachen eine Fehlermeldung ausgeben.
                    if (!(iPartsLanguage.isValidDaimlerIsoCountryCode(getProject(), countryCode))) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültigem Länderode \"%2\" übersprungen.",
                                                                    String.valueOf(recordNo), countryCode),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }
                }
                // KG
                if (StrUtils.isEmpty(topTUId.getKG())) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerer KG übersprungen.",
                                                                String.valueOf(recordNo)),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }

                // TU
                if (StrUtils.isEmpty(topTUId.getTU())) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerer TU übersprungen.",
                                                                String.valueOf(recordNo)),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }
            }
            // Rank
            if (!StrUtils.isValid(rank)) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit leerem Rang übersprungen.",
                                                            String.valueOf(recordNo)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                isValid = false;
            }
            return isValid;
        }
    }
}
