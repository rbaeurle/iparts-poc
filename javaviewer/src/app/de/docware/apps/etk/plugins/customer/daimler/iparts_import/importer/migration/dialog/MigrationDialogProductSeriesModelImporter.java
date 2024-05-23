/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelProperties;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelPropertiesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.DIALOGModelsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelPropertiesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für die Zuordnung Produkt (=Katalog) zu bm-bildende Codes im After Sales.
 */
public class MigrationDialogProductSeriesModelImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    final static String BMRE_NR = "BMRE_NR";
    final static String BMRE_AAO = "BMRE_AAO";
    final static String BMRE_BM = "BMRE_BM";
    final static String BMRE_LKG = "BMRE_LKG";
    final static String BMRE_PG = "BMRE_PG";
    final static String BMRE_CB = "BMRE_CB";
    final static String BMRE_EDAT = "BMRE_EDAT";
    final static String BMRE_ADAT = "BMRE_ADAT";

    public final static String MAD_START_DATE = "1900-01-01";

    private String[] headerNames = new String[]{
            // BMRE_HERK,   // <== Spalte zwar in der Beschreibung, aber nicht in der zu importierenden Datei enthalten
            BMRE_NR,
            BMRE_AAO,
            BMRE_BM,
            // BMRE_ART,    // <== Spalte zwar in der Beschreibung, aber nicht in der zu importierenden Datei enthalten
            BMRE_LKG,
            BMRE_PG,
            BMRE_CB,
            BMRE_EDAT,
            BMRE_ADAT
            // BMRE_FLG,    // <== Spalte zwar in der Beschreibung, aber nicht in der zu importierenden Datei enthalten
            // BMRE_MSPALTE // <== Spalte zwar in der Beschreibung, aber nicht in der zu importierenden Datei enthalten
            // BMRE_DISPLAY // <== Spalte zwar in der Beschreibung, aber nicht in der zu importierenden Datei enthalten
    };


    private HashMap<String, String> mappingSeriesModelData;
    private Map<iPartsModelPropertiesId, iPartsDataModelProperties> importObjectsMap;
    private String tableName;
    private String currentSeries;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    public MigrationDialogProductSeriesModelImporter(EtkProject project, boolean withHeader) {
        super(project, MIGRATION_BMRE_NAME, withHeader,
              new FilesImporterFileListType(TABLE_DA_MODEL_PROPERTIES, MIGRATION_BMRE_NAME, true, false, false, new String[]{ FILE_EXTENSION_NO_HEADER }));

        initMapping();
    }

    private void initMapping() {

        this.tableName = TABLE_DA_MODEL_PROPERTIES;

        mappingSeriesModelData = new HashMap<>();

        // MasterDataDialogModelSeriesImporter:
        mappingSeriesModelData.put(FIELD_DMA_SERIES_NO, BMRE_NR); //    entspricht (X2E_BR)   Fahrzeug- oder Aggregatbaureihe
        mappingSeriesModelData.put(FIELD_DMA_AA, BMRE_AAO); // entspricht (X2E_AA)   Ausführungsart der Baureihe
        mappingSeriesModelData.put(FIELD_DMA_MODEL_NO, BMRE_BM); //     entspricht (X2E_BMAA) Baumuster                         <== DB-PK(Feld 1)
        mappingSeriesModelData.put(FIELD_DMA_STEERING, BMRE_LKG); //    entspricht (X2E_LK)   Lenkung
        mappingSeriesModelData.put(FIELD_DMA_PRODUCT_GRP, BMRE_PG); //  entspricht (X2E_PGKZ) Produktgruppe
        mappingSeriesModelData.put(FIELD_DMA_CODE, BMRE_CB); //         entspricht (X2E_CBED) Codebedingung
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {

        String[] mustExistsAndHaveData = new String[]{ BMRE_NR, BMRE_BM };
        importer.setMustExists(mustExistsAndHaveData);
        importer.setMustHaveData(mustExistsAndHaveData);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //nur für den EinzelTest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
            isSingleCall = true;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }

        // Ggf. hier weitere Prüfungen einfügen
        return true;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
        importObjectsMap = new TreeMap<>();
    }


    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SeriesModelImportHelper importHelper = new SeriesModelImportHelper(getProject(), mappingSeriesModelData, tableName);

        iPartsModelPropertiesId id = getModelPropertyId(importRec, importHelper);
        // Das zu füllende Datenobjekt
        iPartsDataModelProperties dataModelProperties = null;
        boolean existInDB = false;

        // Existiert zu einem Konstruktionsbaumuster ein AS-Baumuster, dann sollen die Datumswerte aus dem AS-Baumuster
        // übernommen werden, da die Werte aus der Migration eher unsinnig sind ("19000101000000").
        // Weil "Datum ab" aber im Schlüssel vorkommt, muss zuerst geprüft werden, ob zu dem Datensatz mit dem Datum aus AS
        // schon ein DB Eintrag existiert. Falls ja, soll dieser herangezogen werden.
        //
        // AS Baumuster in DB suchen
        String constructionBM = importHelper.handleValueOfSpecialField(BMRE_BM, importRec);
        iPartsModelDataId modelId = new iPartsModelDataId(constructionBM);
        iPartsDataModel afterSalesModel = new iPartsDataModel(getProject(), DIALOGModelsHelper.getAfterSalesModelId(modelId));
        if (afterSalesModel.existsInDB()) {
            // Bei vorhandenem AS-Baumuster prüfen, ob ein passender Eintrag in der DB existiert.
            String asModelData = afterSalesModel.getFieldValue(FIELD_DM_DATA);
            // Temporäre Id und DataObject
            iPartsModelPropertiesId tempId = new iPartsModelPropertiesId(id.getModelNumber(), asModelData);
            iPartsDataModelProperties tempDataModelProperties = new iPartsDataModelProperties(getProject(), tempId);
            if (tempDataModelProperties.existsInDB()) {
                // Datensatz existiert schon -> diesen Datensatz nutzen
                tempDataModelProperties.setFieldValue(FIELD_DMA_DATB, afterSalesModel.getFieldValue(FIELD_DM_DATB), DBActionOrigin.FROM_EDIT);
                dataModelProperties = tempDataModelProperties;
                id = tempId; // Ansonsten wird dieser Datensatz mit tempId später in postImportTask() gelöscht, aber kein neues Konstruktions-Baumuster für id angelegt
                existInDB = true;
            }
        }

        // Zum Konstruktionsbaumuster wurde kein AS-Baumuster gefunden -> Datensatz mit den Importdaten anlegen
        if (dataModelProperties == null) {
            dataModelProperties = new iPartsDataModelProperties(getProject(), id);
            existInDB = dataModelProperties.existsInDB();
        }

        if (!existInDB) {
            // Existiert noch nicht in der DB -> mit leeren Feldern initialisieren
            dataModelProperties.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            // initial sind alle neuen Baumuster nicht AS-relevant, da das Flag vom Autor im Edit manuell gesetzt wird: Siehe DAIMLER-7565
            dataModelProperties.setFieldValueAsBoolean(FIELD_DMA_AS_RELEVANT, false, DBActionOrigin.FROM_EDIT);
            dataModelProperties.setFieldValue(FIELD_DMA_DATB, importHelper.getMADDateTimeValue(null), DBActionOrigin.FROM_EDIT); // SDB Wert soll laut Beschreibung hart auf *leer* gesetzt werden (DAIMLER-1943)
            dataModelProperties.setFieldValue(FIELD_DMA_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
        } else {
            // Datenquelle überprüfen
            String origin = dataModelProperties.getFieldValue(FIELD_DMA_SOURCE);
            iPartsImportDataOrigin dataOrigin = iPartsImportDataOrigin.getTypeFromCode(origin);
            if (dataOrigin != iPartsImportDataOrigin.MAD) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 aus anderem Import (%2) wird übersprungen",
                                                            String.valueOf(recordNo), origin),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }
        }

        // Datensatz mit den Werten füllen
        importHelper.fillOverrideCompleteDataForMADReverse(dataModelProperties, importRec, null);
        dataModelProperties.setFieldValue(FIELD_DMA_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
        if (StrUtils.isEmpty(currentSeries)) {
            currentSeries = dataModelProperties.getFieldValue(FIELD_DMA_SERIES_NO);
        }
        // Lege das Properties-Objekt mit seiner Id in der Map für später ab
        importObjectsMap.put(id, dataModelProperties);
    }

    private iPartsModelPropertiesId getModelPropertyId(Map<String, String> importRec, SeriesModelImportHelper importHelper) {
        return new iPartsModelPropertiesId(importHelper.handleValueOfSpecialField(BMRE_BM, importRec),
                                           importHelper.getMADDateTimeValue(MAD_START_DATE)); // SDA Wert soll laut Beschreibung hart auf "01.01.1900" gesetzt werden (DAIMLER-1943)
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
                return;
            }
            if (importToDB) {
                // Liste zum Sammeln von Objekten, die aus der DB gelöscht werden sollen
                iPartsDataModelPropertiesList deleteList = new iPartsDataModelPropertiesList();

                // Lade alle Konstruktionsbaumuster zur Baureihe und Quelle = MAD aus der DB
                iPartsDataModelPropertiesList modelPropertiesList
                        = iPartsDataModelPropertiesList.loadDataModelPropertiesListForSeriesAndSource(getProject(), currentSeries,
                                                                                                      iPartsImportDataOrigin.MAD,
                                                                                                      DBDataObjectList.LoadType.ONLY_IDS);
                Set<String> asModelsToBeSynced = new TreeSet<>();
                for (iPartsDataModelProperties modelProperties : modelPropertiesList) {
                    // 1. Wenn ein Konstruktionsbaumuster aus der DB in der aktuellen Importdatei nicht vorkommt, dann ...
                    if (!importObjectsMap.containsKey(modelProperties.getAsId())) {
                        // 2. Bestimme das dazugehörige AS-Baumuster
                        String asModel = DIALOGModelsHelper.getAfterSalesModelNumber(modelProperties.getAsId());

                        // 3a. Wenn ein Konstruktionsbaumuster gefunden wurde, das ein anderes AS-Baumuster erzeugen würde,
                        // dann merke dir das dazugehörige AS-Baumuster und stoße später den Sync dafür an
                        asModelsToBeSynced.add(asModel);

                        // 3b. Lege das aktuelle Konstruktionsbaumuster in die Delete-Liste
                        deleteList.delete(modelProperties, true, DBActionOrigin.FROM_EDIT);
                    }
                }

                if (!deleteList.getDeletedList().isEmpty()) { // Sollen Konstruktionsbaumuster gelöscht werden?
                    int deletedModelsCount = deleteList.getDeletedList().size(); // Größe merken, da durch saveToDB() die deletedList geleert wird
                    deleteList.saveToDB(getProject());
                    getMessageLog().fireMessage(translateForLog("!!Es wurden %1 Konstruktions-Baumuster aus der Datenbank entfernt.",
                                                                String.valueOf(deletedModelsCount)),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }

                // Konstruktionsbaumuster, die im Import nicht vorkommen aber in der DB existiert haben, wurden nun gelöscht.
                // Jetzt können die neuen Konstruktionsbaumuster importiert/geupdatet werden.
                for (iPartsDataModelProperties modelProperties : importObjectsMap.values()) {
                    if (Thread.currentThread().isInterrupted()) {
                        cancelImport("!!Import-Thread wurde frühzeitig beendet");
                        return;
                    }

                    saveToDB(modelProperties);

                    // Bestimme das dazugehörige AS-Baumuster für das gespeicherte Konstruktionsbaumuster für den späteren Sync
                    String asModel = DIALOGModelsHelper.getAfterSalesModelNumber(modelProperties.getAsId());
                    asModelsToBeSynced.add(asModel);
                }

                super.postImportTask(); // Jetzt schon die restliche bufferList speichern

                // Zuletzt noch alle relevanten AS-Baumuster synchronisieren
                for (String asModel : asModelsToBeSynced) {
                    // iPartsModelDataId kann auch ein AS-Baumuster enthalten für die Methode synchronizeConstructionAndASModels()
                    DIALOGModelsHelper.synchronizeConstructionAndASModels(new iPartsModelDataId(asModel), false,
                                                                          false, false, getProject());
                }
            }
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', withHeader, headerNames));
        }
        return false;
    }

    private class SeriesModelImportHelper extends MADImportHelper {

        public SeriesModelImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(BMRE_BM) || sourceField.equals(BMRE_NR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            }
            return value;
        }
    }
}
