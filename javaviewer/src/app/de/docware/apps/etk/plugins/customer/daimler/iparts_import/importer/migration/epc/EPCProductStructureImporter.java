/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSales;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSalesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEPCLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes.MODEL_DICTIONARY;

/**
 * EPC Importer für die Produktstruktur aus
 * - CATSinIPARTS_BM_SUBGRP.csv,
 * - und CATSinIPARTS_BM_GROUP.csv
 */

public class EPCProductStructureImporter extends AbstractEPCDataImporter implements iPartsConst, EtkDbConst {

    // Zum Checken der zu importierenden Dateien/Daten
    private enum IMPORT_TYPE {
        IMPORT_UNKNOWN, IMPORT_BM_GROUP, IMPORT_BM_SUBGROUP
    }

    private static final String DEST_TABLENAME = TABLE_DA_KGTU_AS;// Die Zieltabelle
    private static final String IMPORT_TABLENAME_BM_GROUP = "CATS_IN_IPARTS_BM_GROUP";      // KG-Struktur
    private static final String IMPORT_TABLENAME_SUBGRP = "CATS_IN_PARTS_BM_SUBGRP";        // TU-Struktur

    // Gemeinsame Spalten
    private final static String CATNUM = "CATNUM";
    private final static String GROUPNUM = "GROUPNUM";
    // Die Spalten der Importdatei [GROUP] (=KG)
    // CATNUM¬GROUPNUM¬DESCIDX
    private final static String GROUP_DESCIDX = "DESCIDX";
    // Als Array
    private String[] groupHeaderNames = new String[]{
            CATNUM,
            GROUPNUM,
            GROUP_DESCIDX
    };

    // Die Spalten der Importdatei [SUBGROUP] (=TU)
    // CATNUM¬GROUPNUM¬LANG¬SUBGRP¬TEXT
    private final static String SUBGROUP_LANG = "LANG";
    private final static String SUBGROUP_SUBGRP = "SUBGRP";
    private final static String SUBGROUP_TEXT = "TEXT";
    // Als Array
    private String[] subgroupHeaderNames = new String[]{
            CATNUM,
            GROUPNUM,
            SUBGROUP_LANG,
            SUBGROUP_SUBGRP,
            SUBGROUP_TEXT
    };


    // Zur Unterscheidung der Aufrufe:
    private IMPORT_TYPE importType;
    // Die Primärschlüssel für die jeweiligen Import-Daten
    private String[] primaryKeys_KG_ImportData;
    private String[] primaryKeys_TU_ImportData;

    // Ein kleiner Cache, damit die Produkte nicht immer wieder aus der DB geladen werden.
    private Map<iPartsProductId, Boolean> productRelevanceCache = new HashMap<>();

    // Der Helper, nur ein mal.
    private DictImportTextIdHelper dictHelper;

    // Wird beim KG/TU-Import benutzt um den Wechsel zwischen den TUs mitzubekommen.
    private iPartsDataKgTuAfterSales currentKgTuObject = null;
    private iPartsDataKgTuAfterSalesId idToSkip;

    private boolean doBufferSave = true;
    private boolean importToDB = true;

    /**
     * Importer für zwei Importdateien
     *
     * @param project
     */
    public EPCProductStructureImporter(EtkProject project) {
        super(project, DEST_TABLENAME,

              new FilesImporterFileListType(IMPORT_TABLENAME_BM_GROUP, EPC_PROD_STRUCT_KG, true, true, true,
                                            new String[]{ MimeTypes.EXTENSION_CSV }, false),
              new FilesImporterFileListType(IMPORT_TABLENAME_SUBGRP, EPC_PROD_STRUCT_TU, true, true, true,
                                            new String[]{ MimeTypes.EXTENSION_CSV }, false)
        );
        // Den Helper nur einmalig anlegen.
        this.dictHelper = new DictImportTextIdHelper(project);
        this.importType = IMPORT_TYPE.IMPORT_UNKNOWN;
        setTablename(DEST_TABLENAME);
    }


    /**
     * Ermittelt die Spaltenüberschriften passend zur Importdatei.
     *
     * @return
     */
    @Override
    protected String[] getHeaderNames() {
        if (importType.equals(IMPORT_TYPE.IMPORT_BM_GROUP)) {
            return groupHeaderNames;
        } else if (importType.equals(IMPORT_TYPE.IMPORT_BM_SUBGROUP)) {
            return subgroupHeaderNames;
        } else {
            return new String[]{};
        }
    }

    @Override
    protected HashMap<String, String> initMapping() {
        // Die Primärschlüssel (in der Datei)
        primaryKeys_KG_ImportData = new String[]{ CATNUM, GROUPNUM };
        // Die Primärschlüssel (in der Datei)
        primaryKeys_TU_ImportData = new String[]{ CATNUM, GROUPNUM, SUBGROUP_LANG, SUBGROUP_SUBGRP };
        return new HashMap<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustFields = new String[0];
        if (importType.equals(IMPORT_TYPE.IMPORT_BM_GROUP)) {
            mustFields = StrUtils.mergeArrays(primaryKeys_KG_ImportData, new String[]{ GROUP_DESCIDX });
        } else if (importType.equals(IMPORT_TYPE.IMPORT_BM_SUBGROUP)) {
            mustFields = StrUtils.mergeArrays(primaryKeys_TU_ImportData, new String[]{ SUBGROUP_TEXT });
        }
        importer.setMustExists(mustFields);
        importer.setMustHaveData(mustFields);
    }

    /**
     * Setzt den Import-Typ passend zur Importdatei.
     *
     * @param importTableName
     * @return
     */
    private IMPORT_TYPE getImportType(String importTableName) {
        if (importTableName.equals(IMPORT_TABLENAME_BM_GROUP)) {
            return IMPORT_TYPE.IMPORT_BM_GROUP;
        } else if (importTableName.equals(IMPORT_TABLENAME_SUBGRP)) {
            return IMPORT_TYPE.IMPORT_BM_SUBGROUP;
        } else {
            return IMPORT_TYPE.IMPORT_UNKNOWN;
        }
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
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
    protected void preImportTask() {
        setBufferedSave(doBufferSave);
    }

    /**
     * ImportRecord halt.
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Datensatz entsprechend der Importdatei interpretieren.
        if (importType.equals(IMPORT_TYPE.IMPORT_BM_GROUP)) {
            importKgRecord(importRec, recordNo);
        } else if (importType.equals(IMPORT_TYPE.IMPORT_BM_SUBGROUP)) {
            importTuRecord(importRec, recordNo);
        }
    }

    /**
     * Für die Werte aus der ...BM_GROUP-Datei
     *
     * @param importRec
     * @param recordNo
     */
    private void importKgRecord(Map<String, String> importRec, int recordNo) {

        EpcKgTuImportHelper helper = new EpcKgTuImportHelper(getProject(), DEST_TABLENAME);
        String productNo = helper.handleValueOfSpecialField(CATNUM, importRec).trim();

        // Logische Prüfung mit Ausgabe von Meldungen
        if (!helper.isProductRelevantForImport(this, productNo, productRelevanceCache, recordNo)) {
            reduceRecordCount();
            return;
        }

        iPartsDataKgTuAfterSalesId kgTuId = new iPartsDataKgTuAfterSalesId(productNo,
                                                                           helper.handleValueOfSpecialField(GROUPNUM, importRec),
                                                                           "");
        if (kgTuId.getProduct().isEmpty() || kgTuId.getKg().isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Id \"%2\" übersprungen",
                                                        String.valueOf(recordNo), kgTuId.toStringForLogMessages()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        iPartsDataKgTuAfterSales kgTuDataObject = new iPartsDataKgTuAfterSales(getProject(), kgTuId);
        if (!initCurrentObjectWithDB(kgTuDataObject)) {
            reduceRecordCount();
            return;
        }
        String textId = helper.handleValueOfSpecialField(GROUP_DESCIDX, importRec);

        EtkMultiSprache multiSprache = dictHelper.searchEPCTextWithEPCId(MODEL_DICTIONARY, textId);
        if (multiSprache != null) {
            kgTuDataObject.setFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC, multiSprache, DBActionOrigin.FROM_EDIT);
        } else {
            getMessageLog().fireMessage(translateForLog("!!Zur Text-ID \"%1\" konnte kein Text in \"%2\" ermittelt werden!",
                                                        textId, MODEL_DICTIONARY.getTextKindName()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                        MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }

        if (importToDB) {
            saveToDB(kgTuDataObject);
        }
    }

    /**
     * Für die Werte aus der ...BM_SUBGROUP-Datei.
     * Die Datei enthält jeweils eine Zeile für jede Sprache einer KG/TU.
     * Alle Datensätze einer KG/TU kommen hintereinander.
     * Beim Wechsel zu einem anderen KG/TU oder am Ende (letzter DS) wird gespeichert.
     *
     * Nur Produkte mit dreistelliger Produktnummer zum Import zulassen,
     * falls sie nicht bereits aus MAD migriert wurden.
     * ==> Nur Quelle == "APPL_LIST" oder "EPC" übernehmen.
     *
     * @param importRec
     * @param recordNo
     */
    private void importTuRecord(Map<String, String> importRec, int recordNo) {
        EpcKgTuImportHelper helper = new EpcKgTuImportHelper(getProject(), DEST_TABLENAME);
        String productNo = helper.handleValueOfSpecialField(CATNUM, importRec).trim();

        // Logische Prüfung mit Ausgabe von Meldungen
        if (!helper.isProductRelevantForImport(this, productNo, productRelevanceCache, recordNo)) {
            reduceRecordCount();
            return;
        }
        // Die Sprache ermitteln und prüfen.
        iPartsEPCLanguageDefs langDef = iPartsEPCLanguageDefs.getType(helper.handleValueOfSpecialField(SUBGROUP_LANG, importRec));
        if ((langDef == iPartsEPCLanguageDefs.EPC_UNKNOWN) || (langDef == iPartsEPCLanguageDefs.EPC_NEUTRAL)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Sprache \"%2\" übersprungen",
                                                        String.valueOf(recordNo),
                                                        helper.handleValueOfSpecialField(SUBGROUP_LANG, importRec)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
            return;
        }

        iPartsDataKgTuAfterSalesId kgTuId = new iPartsDataKgTuAfterSalesId(productNo,
                                                                           helper.handleValueOfSpecialField(GROUPNUM, importRec),
                                                                           helper.handleValueOfSpecialField(SUBGROUP_SUBGRP, importRec));
        if (idToSkip != null) {
            if (idToSkip.equals(kgTuId)) {
                reduceRecordCount();
                return;
            } else {
                idToSkip = null;
            }
        }
        if (kgTuId.getProduct().isEmpty() || kgTuId.getKg().isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Id \"%2\" übersprungen",
                                                        String.valueOf(recordNo), kgTuId.toStringForLogMessages()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                        MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            reduceRecordCount();
            return;
        }

        // Beim ersten Durchlauf ein neues, leeres Objekt erzeugen, das so nach und nach gefüllt wird.
        if (currentKgTuObject == null) {
            if (!initAndFillCurrentObject(kgTuId)) {
                return;
            }
        } else {
            if (!checkAndStoreCurrentDataset(kgTuId, helper, importRec)) {
                return;
            }
        }

        String foreignText = helper.handleValueOfSpecialField(SUBGROUP_TEXT, importRec);
        currentKgTuObject.setMultLangValue(langDef.getDbValue().getCode(), FIELD_DA_DKM_DESC, foreignText, null, null,
                                           true, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Überprüft, ob der neue Datensatz ein anderer ist als der, der aktuell aufgesammelt wird. Falls ja, dann wird
     * das speichern des aktuellen Datensatzes durchgeführt.
     *
     * @param kgTuId
     * @param helper
     * @param importRec
     */
    private boolean checkAndStoreCurrentDataset(iPartsDataKgTuAfterSalesId kgTuId, EpcKgTuImportHelper helper,
                                                Map<String, String> importRec) {
        if (importType == IMPORT_TYPE.IMPORT_BM_SUBGROUP) {
            // Wenn eine neue KG/TU gefunden wurde, die bis hierhin gesammelten Werte speichern.
            if (!currentKgTuObject.getAsId().equals(kgTuId)) {
                storeCurrentDataset();
                // Nach dem Speichern ein neues Objekt leer anlegen
                if (!initAndFillCurrentObject(kgTuId)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Speichert den aktuellen Datensatz in der DB. Via EPC-TextId wird der dazugehörige Text in der DB gesucht. Falls
     * keiner gefunden wird, wird der neue Text angelegt.
     */
    private void storeCurrentDataset() {
        if ((currentKgTuObject != null) && (importType == IMPORT_TYPE.IMPORT_BM_SUBGROUP)) {
            // Vor dem Speichern das MultiSprachobjekt noch einmal durch den DictHelper für EPC anpassen lassen.
            EtkMultiSprache multiSprache = currentKgTuObject.getFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC);
            if (dictHelper.handleDictTextIdForEPC(MODEL_DICTIONARY, multiSprache, "", DictHelper.getEPCForeignSource(),
                                                  TableAndFieldName.make(TABLE_DA_KGTU_AS, FIELD_DA_DKM_DESC))) {
                currentKgTuObject.setFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC, multiSprache, DBActionOrigin.FROM_EDIT);
                if (importToDB) {
                    saveToDB(currentKgTuObject);
                    // Anzeige geradeziehen
                    skippedRecords += multiSprache.getSprachenCount() - 1;
                }
            } else {
                if (dictHelper.hasWarnings()) {
                    getMessageLog().fireMessage(translateForLog("!!Fehler beim Importieren der Bezeichnung für " +
                                                                "den KGTU Knoten \"%1\". Genauere Fehlerbeschreibung im Log.",
                                                                currentKgTuObject.getAsId().toString("|")),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    for (String message : dictHelper.getWarnings()) {
                        getMessageLog().fireMessage(translateForLog("!!Warnung zu \"%1\": %2",
                                                                    currentKgTuObject.getAsId().toString("|"), message),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
                                                    MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                    }
                }
            }
        }
    }

    /**
     * Initialisiert und befüllt das aktuelle Objekt mit dem aktuellen Datensatz.
     *
     * @param kgTuId
     * @return
     */
    private boolean initAndFillCurrentObject(iPartsDataKgTuAfterSalesId kgTuId) {
        if (importType != IMPORT_TYPE.IMPORT_BM_SUBGROUP) {
            return false;
        }
        currentKgTuObject = new iPartsDataKgTuAfterSales(getProject(), kgTuId);
        // Mit den Daten aus der DB initialisieren
        if (!initCurrentObjectWithDB(currentKgTuObject)) {
            currentKgTuObject = null;
            idToSkip = kgTuId;
            reduceRecordCount();
            return false;
        }
        return true;
    }

    /**
     * Initialisiert das aktuelle Objekt mit den Werten aus der DB (falls ein DB Datensatz vorhanden ist) oder diekt mit
     * leeren Werten. Zusätzlich wird die Quelle gesetzt.
     *
     * @param kgTuDataObject
     * @return
     */
    private boolean initCurrentObjectWithDB(iPartsDataKgTuAfterSales kgTuDataObject) {
        if (!kgTuDataObject.existsInDB()) {
            kgTuDataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        } else {
            iPartsImportDataOrigin currentSource = iPartsImportDataOrigin.getTypeFromCode(kgTuDataObject.getFieldValue(FIELD_DA_DKM_SOURCE));
            if ((currentSource != iPartsImportDataOrigin.EPC) && (currentSource != iPartsImportDataOrigin.APP_LIST)) {
                getMessageLog().fireMessage(translateForLog("!!Zur KG/TU Id \"%1\" existiert schon ein Eintrag " +
                                                            "mit der Quelle \"%2\". Record wird übersprungen",
                                                            kgTuDataObject.getAsId().toString(), currentSource.getOrigin()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return false;
            }
        }
        kgTuDataObject.setFieldValue(FIELD_DA_DKM_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
        // MultiLang nachladen
        kgTuDataObject.getFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC);
        return true;
    }

    @Override
    protected void postImportTask() {
        // Das letzte Objekt (den letzten Datensatz) speichern.
        if (!isCancelled()) {
            storeCurrentDataset();
        }
        productRelevanceCache.clear();
        super.postImportTask();
    }


    /**
     * @param importFileType
     * @return
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return true;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        importType = getImportType(importFileType.getFileListType());
        // Da wir erst hier den Typ bestimmen können, werden die bestehenden Daten auch erst hier entfernt. Durch die
        // zwei-Datei-Import-Variante können hier nicht den normalen Lösch-Mechanismus wählen, da beim Import von nur
        // einer Datei, die Daten der anderen Datei trotzdem gelöscht werden würden (sofern dort ein Häkchen gesetzt wurde)
        removeAllExistingDataForType(importFileType);
        setHeaderNames(getHeaderNames());
        setFieldMapping(initMapping());
        return super.importFile(importFileType, importFile);
    }

    /**
     * Entfernt abhängig vom Importtyp die bisherigen Daten in der DB
     *
     * @param importFileType
     */
    private void removeAllExistingDataForType(FilesImporterFileListType importFileType) {
        if (!isRemoveAllExistingData()) {
            return;
        }
        switch (importType) {
            case IMPORT_UNKNOWN:
                getMessageLog().fireMessage(translateForLog("!!Fehler beim Löschen der Daten zum " +
                                                            "übergebenen Typ \"%1\"", importType.name()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                cancelImport();
                break;
            case IMPORT_BM_GROUP:
                getProject().getDbLayer().delete(DEST_TABLENAME, new String[]{ FIELD_DA_DKM_TU, FIELD_DA_DKM_SOURCE },
                                                 new String[]{ "", iPartsImportDataOrigin.EPC.getOrigin() });
                break;
            case IMPORT_BM_SUBGROUP:
                getProject().getDbLayer().delete(DEST_TABLENAME, new String[]{ FIELD_DA_DKM_SOURCE },
                                                 new String[]{ iPartsImportDataOrigin.EPC.getOrigin() },
                                                 new String[]{ FIELD_DA_DKM_TU }, new String[]{ "" }, false);
                break;
        }
    }

    /**
     * Der unvermeidliche Helper
     */
    private class EpcKgTuImportHelper extends EPCImportHelper {

        public EpcKgTuImportHelper(EtkProject project, String tableName) {
            super(project, new HashMap<String, String>(), tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }
            return value;
        }
    }
}
