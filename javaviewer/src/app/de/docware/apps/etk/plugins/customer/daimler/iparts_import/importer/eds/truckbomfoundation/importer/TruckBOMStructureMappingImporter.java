/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelElementUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelElementUsageList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsModelElementUsageId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Importer für das Mapping der alten EDS/BCS Struktur auf die neue aus TruckBOM.foundation
 */
public class TruckBOMStructureMappingImporter extends AbstractDataImporter implements iPartsConst {

    private static final String TABLE_NAME = TABLE_DA_MODEL_ELEMENT_USAGE;

    private static final String MODEL = "MODEL__IDENTIFIER";
    private static final String MODULE_CATEGORY = "MODULE_CATEGORY__IDENTIFIER";
    private static final String SUB_MODULE_CATEGORY = "SUB_MODULE_CATEGORY__IDENTIFIER";
    private static final String LEGACY_DIFFERENTIATION_NUMBER = "LEGACY_DIFFERENTIATION_NUMBER";
    private static final String POSITION = "POSITION";
    private static final String VERSION = "VERSION";
    private static final String LEGACY_SCOPE = "LEGACY_SCOPE";
    private static final String MODEL_ELEMENT = "MODEL_ELEMENT__IDENTIFIER";

    private static final String[] HEADER_NAMES = new String[]{
            MODEL,
            MODULE_CATEGORY,
            SUB_MODULE_CATEGORY,
            POSITION,
            VERSION,
            LEGACY_SCOPE,
            MODEL_ELEMENT
    };

    private static final String[] MUST_HAVE_DATA = new String[]{
            MODEL,
            MODULE_CATEGORY,
            SUB_MODULE_CATEGORY,
            POSITION,
            VERSION,
            MODEL_ELEMENT
    };

    // Mapping der EDS_MODEL Felder auf die MODEL_USAGE_ELEMENT Felder
    private static final Map<String, String> DB_FIELD_MAPPING = new HashMap<>();

    static {
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_REVTO, FIELD_DMEU_REVTO);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_STEERING, FIELD_DMEU_STEERING);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_KEMFROM, FIELD_DMEU_KEMFROM);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_KEMTO, FIELD_DMEU_KEMTO);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_RELEASE_FROM, FIELD_DMEU_RELEASE_FROM);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_RELEASE_TO, FIELD_DMEU_RELEASE_TO);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_MSAAKEY, FIELD_DMEU_SUB_ELEMENT);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_RFG, FIELD_DMEU_RFG);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_QUANTITY, FIELD_DMEU_QUANTITY);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_PGKZ, FIELD_DMEU_PGKZ);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_CODE, FIELD_DMEU_CODE);
        DB_FIELD_MAPPING.put(FIELD_EDS_MODEL_PLANTSUPPLY, FIELD_DMEU_PLANTSUPPLY);
    }


    private static final boolean IMPORT_TO_DB = true;
    private static final boolean DO_BUFFER_SAVE = true;

    private Map<String, Map<String, List<ImportRecord>>> modelToRecords;
    private int importedDataCount;

    public TruckBOMStructureMappingImporter(EtkProject project) {
        super(project, TRUCK_BOM_FOUNDATION_STRUCTURE_MAPPING_IMPORT_NAME, true,
              new FilesImporterFileListType(TABLE_NAME, TRUCK_BOM_FOUNDATION_STRUCTURE_MAPPING_IMPORT_NAME,
                                            true, false, true,
                                            new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(TABLE_NAME)) {
            getProject().getDB().delete(TABLE_NAME);
            return true;
        }
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(TABLE_NAME)) {
            return importMasterData(prepareImporterKeyValue(importFile, TABLE_NAME, '|', withHeader, HEADER_NAMES));
        }
        return false;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        // Hier bemerkte Fehler führen zum Abbruch, das ist aber nicht gewünscht.
        importer.setMustExists(HEADER_NAMES);
        importer.setMustHaveData(MUST_HAVE_DATA);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            if (!importer.getTableNames().get(0).equals(TABLE_NAME)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return importer.isRecordValid(importRec, errors);
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        modelToRecords = new LinkedHashMap<>();
        importedDataCount = 0;
        setBufferedSave(DO_BUFFER_SAVE);
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // BM holen
        String model = importRec.get(MODEL);
        if (StrUtils.isValid(model)) {
            // Bei einem BM Wechsel, die bisherigen Daten importieren
            if (!modelToRecords.isEmpty() && !modelToRecords.containsKey(model)) {
                saveData(true);
            }
            ImportRecord importRecord = new ImportRecord(importRec);
            if (importRecord.isValid()) {
                // BM auf Importdaten aus der Mapping-Datei
                Map<String, List<ImportRecord>> recordsForModel = modelToRecords.computeIfAbsent(model, k -> new LinkedHashMap<>());
                // Datenschlüssel auf Importdaten aus der Mapping-Datei
                List<ImportRecord> recordsForDataKey = recordsForModel.computeIfAbsent(importRecord.getDataKey(), k -> new ArrayList<>());
                recordsForDataKey.add(importRecord);
            }
        }
    }

    @Override
    protected void logImportRecordsFinished(int importRecordCount) {
        if (isSingleCall) {
            getMessageLog().fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                                        getDatasetTextForLog(importRecordCount)) +
                                        ", " + translateForLog("!!%1 %2 importiert", String.valueOf(importedDataCount),
                                                               getDatasetTextForLog(importedDataCount)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled() && IMPORT_TO_DB) {
            saveData(false);
        }
        modelToRecords = null;
        super.postImportTask();
    }

    private void saveData(boolean commitData) {
        // Alle BM durchlaufen
        modelToRecords.forEach((model, records) -> {
            if (StrUtils.isValid(model)) {
                // Für das BM alle Altdaten aus EDS_MODEL laden
                iPartsDataEDSModelContentList oldDataForModel = iPartsDataEDSModelContentList.loadAllModelEntries(getProject(), model);
                // Sind keine Altdaten vorhanden, brauchen wir keine neuen anzulegen/zu aktualisieren
                if (oldDataForModel.size() > 0) {
                    // Wenn nicht gewünscht ist, dass man die bestehenden Daten vor dem Import löscht, hier die bestehenden
                    // Neudaten laden, damit diese aktualisiert werden
                    final Map<iPartsModelElementUsageId, iPartsDataModelElementUsage> modelElementUsageMap = new HashMap<>();
                    if (!isRemoveAllExistingData()) {
                        // Id auf DataObject
                        modelElementUsageMap.putAll(iPartsDataModelElementUsageList.loadAllModelEntries(getProject(), model).getAsList()
                                                            .stream()
                                                            .collect(Collectors.toMap(iPartsDataModelElementUsage::getAsId, Function.identity())));
                    }
                    VarParam<Integer> saveCount = new VarParam<>(0);
                    // Alle Altdaten zum BM durchlaufen
                    oldDataForModel.forEach(modelData -> {
                        // Verbindungsschlüssel erzeugen
                        String dataKey = makeDataKey(modelData);
                        // Via Verbindungsschlüssel das Mapping aus der Datei holen
                        List<ImportRecord> importData = records.get(dataKey);
                        // Gibt's zum alten Datensatz kein Mapping -> es kann kein neuer Datensatz angelegt bzw aktualisiert werden
                        if (importData != null) {
                            importData.forEach(data -> {
                                // Zieldatensatz anlegen bzw aus der DB laden
                                iPartsDataModelElementUsage modelElementUsage = createDataObject(data, modelElementUsageMap);
                                // Altdaten in den neuen Datensatz kopieren
                                fillDataObject(modelElementUsage, modelData);
                                // Datensatz speichern
                                if (saveToDB(modelElementUsage)) {
                                    saveCount.setValue(saveCount.getValue() + 1);
                                }
                            });
                        } else {
                            getMessageLog().fireMessage(translateForLog("!!Kein Mapping: %1", dataKey),
                                                        MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                        }
                    });

                    if (commitData) {
                        storeDataInDB(model, saveCount.getValue());
                    } else {
                        getMessageLog().fireMessage(translateForLog("!!Für das Baumuster \"%1\" wurden %2 Datensätze verarbeitet.", model, String.valueOf(saveCount)),
                                                    MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
                    }
                    importedDataCount += saveCount.getValue();
                }
            }
        });
        modelToRecords.clear();
    }

    private void storeDataInDB(String model, Integer saveCount) {
        try {
            // Bisherige Transaktion explizit committen, damit die Daten in der DB landen
            getProject().getDbLayer().commit();
            getProject().getDbLayer().startTransaction();
            getMessageLog().fireMessage(translateForLog("!!Für das Baumuster \"%1\" wurden %2 Datensätze in die Datenbank geschrieben.", model, String.valueOf(saveCount)),
                                        MessageLogType.tmlMessage, MessageLogOption.WRITE_ONLY_IN_LOG_FILE, MessageLogOption.TIME_STAMP);
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            cancelImport(e.getMessage());
        }
    }

    /**
     * Erzeugt einen Datensatz aus DA_MODEL_ELEMENT_USAGE mit den übergebenen Parametern. Existiert keiner in der DB,
     * wird ein neues DataObject angelegt.
     *
     * @param data
     * @param modelElementUsageMap
     * @return
     */
    private iPartsDataModelElementUsage createDataObject(ImportRecord data,
                                                         Map<iPartsModelElementUsageId, iPartsDataModelElementUsage> modelElementUsageMap) {
        iPartsModelElementUsageId modelElementUsageId = new iPartsModelElementUsageId(data.getModelNumber(),
                                                                                      data.getModule(),
                                                                                      data.getSubModule(),
                                                                                      data.getPos(),
                                                                                      data.getLegacyNumber(),
                                                                                      data.getRevisionFrom());
        // Gibts keine Daten in der Zieltabelle, einen neuen anlegen
        if (modelElementUsageMap == null) {
            return createNewDataObject(modelElementUsageId);
        }

        iPartsDataModelElementUsage modelElementUsage = modelElementUsageMap.get(modelElementUsageId);
        if (modelElementUsage == null) {
            modelElementUsage = createNewDataObject(modelElementUsageId);
        }
        return modelElementUsage;
    }

    private iPartsDataModelElementUsage createNewDataObject(iPartsModelElementUsageId modelElementUsageId) {
        iPartsDataModelElementUsage modelElementUsage = new iPartsDataModelElementUsage(getProject(), modelElementUsageId);
        modelElementUsage.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
        return modelElementUsage;
    }

    private void fillDataObject(iPartsDataModelElementUsage modelElementUsage, iPartsDataEDSModelContent modelData) {
        DB_FIELD_MAPPING.forEach((edsField, dmeuField) -> {
            String value = modelData.getFieldValue(edsField);
            if (dmeuField.equals(FIELD_DMEU_REVTO) && value.equals(EDSImportHelper.EDS_AS_BIS_UNENDLICH)) {
                value = "";
            } else if ((dmeuField.equals(FIELD_DMEU_RELEASE_FROM) || dmeuField.equals(FIELD_DMEU_RELEASE_TO)) && iPartsEDSDateTimeHandler.isFinalStateDbDateTime(value)) {
                value = "";
            }
            modelElementUsage.setFieldValue(dmeuField, value, DBActionOrigin.FROM_EDIT);
        });
    }

    /**
     * Erzeugt einen Verbindungsschlüssel aus {@link iPartsDataEDSModelContent} für das Mapping EDS_MODEL Datensatz zu
     * Datensatz aus der Mappingdatei.
     *
     * @param modelData
     * @return
     */
    private String makeDataKey(iPartsDataEDSModelContent modelData) {
        String modelNumber = modelData.getAsId().getModelNumber();
        String group = determineGroupValue(modelData.getAsId().getGroup());
        String scope = modelData.getAsId().getScope();
        String pos = fillValue(modelData.getAsId().getPosition());
        String revisionFrom = fillValue(modelData.getAsId().getRevisionFrom());
        String subElement = modelData.getSaaKey();
        return makeDataKey(modelNumber, group, scope, pos, revisionFrom, subElement);
    }

    private String determineGroupValue(String group) {
        if (StrUtils.isValid(group)) {
            // Wenn die Gruppe mit "L" beginnt und an der zweiten Stelle eine Ziffer hat und an der dritten Stelle einen
            // Buchstaben, dann reduzieren wir die Gruppe auf die zwei Stellen eines Moduls.
            // Original-Logik:
            // if Gruppe[1] = 'L'
            // and Gruppe[2] in ('0','1','2',…,'9')
            // and Gruppe[3] in ('A','B','C',…,'Z')
            // { Gruppe[2-3] }
            // else
            // { trim(Gruppe[1-4]) }
            if ((group.length() >= 3) && group.startsWith("L")
                && StrUtils.isDigit(group.substring(1, 2)) && StrUtils.isNotANumber(group.substring(2, 3))) {
                return group.substring(1, 3);
            } else if (group.length() > 4) {
                return group.substring(0, 4);
            }
        }
        return group;
    }

    /**
     * Erzeugt einen Verbindungsschlüssel aus den übergebenen Parameter für das Mapping EDS_MODEL Datensatz zu
     * Datensatz aus der Mappingdatei.
     *
     * @param modelNumber
     * @param module
     * @param subModule
     * @param pos
     * @param revisionFrom
     * @param subElement
     * @return
     */
    private static String makeDataKey(String modelNumber, String module, String subModule, String pos, String
            revisionFrom, String subElement) {
        return StrUtils.makeDelimitedString("|", modelNumber, module, subModule, pos, revisionFrom, subElement);
    }

    private static String fillValue(String originalValue) {
        return (originalValue == null) ? "" : StrUtils.leftFill(originalValue, 3, '0');
    }


    /**
     * Helfer für die Mapping-Informationen aus der Mapping-Datei
     */
    private static class ImportRecord {

        private String dataKey;
        private String modelNumber;
        private String module;
        private String subModule;
        private String pos;
        private String revisionFrom;
        private String subElement;
        private String legacyScope;
        private final Map<String, String> importRecordData;

        public ImportRecord(Map<String, String> importRecordData) {
            this.importRecordData = importRecordData;
            setData(importRecordData);
        }

        /**
         * Setzt die Infos aus dem Importdatensatz
         *
         * @param importRecordData
         */
        private void setData(Map<String, String> importRecordData) {
            this.modelNumber = importRecordData.get(MODEL);
            this.module = importRecordData.get(MODULE_CATEGORY);
            this.subModule = importRecordData.get(SUB_MODULE_CATEGORY);
            this.pos = importRecordData.get(POSITION);
            this.revisionFrom = importRecordData.get(VERSION);
            this.subElement = importRecordData.get(MODEL_ELEMENT);
            this.legacyScope = importRecordData.get(LEGACY_SCOPE);
            createDataKey();
        }

        /**
         * Erzeugt einen Daten-Schlüssel, um die Mappingdaten auf die Original-Daten zu mappen
         */
        private void createDataKey() {
            String revFromForKey = fillValue(revisionFrom);
            String posForKey = fillValue(pos);
            this.dataKey = makeDataKey(modelNumber, module, legacyScope, posForKey,
                                       revFromForKey, subElement);
        }

        public String getDataKey() {
            return dataKey;
        }

        public String getModelNumber() {
            return modelNumber;
        }

        public String getModule() {
            return module;
        }

        public String getSubModule() {
            return subModule;
        }

        public String getPos() {
            return pos;
        }

        public String getRevisionFrom() {
            return revisionFrom;
        }

        public String getSubElement() {
            return subElement;
        }

        public Map<String, String> getImportRecordData() {
            return importRecordData;
        }

        public boolean isValid() {
            return StrUtils.isValid(dataKey);
        }

        public String getLegacyNumber() {
            return getImportRecordData().get(LEGACY_DIFFERENTIATION_NUMBER);
        }
    }
}
