/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMSingleKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodel.TruckBOMModelData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodel.TruckBOMModelVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodel.TruckBOMSingleAggregateModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getmodel.TruckBOMSingleVehicleModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.EDSModelUpdateImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für die Baumuster-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMModelImporter extends AbstractTruckBOMFoundationJSONImporter {

    public TruckBOMModelImporter(EtkProject project) {
        super(project, TRUCK_BOM_FOUNDATION_MODEL_IMPORT_NAME, TABLE_DA_MODEL);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected Map<String, AbstractBOMXMLDataImporter> createSubImporter() {
        Map<String, AbstractBOMXMLDataImporter> importer = new HashMap<>();
        importer.put(TABLE_DA_MODEL, new EDSModelUpdateImporter(getProject()));
        return importer;
    }

    @Override
    protected boolean importJSONResponse(String response) {
        String fileName = getImportName(getProject().getDBLanguage());
        Genson genson = JSONUtils.createGenson(true);
        try {
            TruckBOMModelData truckBOMModelData = deserializeFromString(genson, response, fileName, TruckBOMModelData.class);
            if (truckBOMModelData == null) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine gültigen Werte.", fileName);
                return false;
            }
            if (!checkObjectCount(truckBOMModelData)) {
                return true;
            }
            // Alle Baumusterstammdaten
            List<TruckBOMSingleVehicleModel> singleVehicleModels = truckBOMModelData.getVehicle();
            List<TruckBOMSingleAggregateModel> singleAggregateModels = truckBOMModelData.getMajorComponent();
            if (!Utils.isValid(singleVehicleModels) && !Utils.isValid(singleAggregateModels)) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine Baumuster-Stammdaten.", fileName);
                return false;
            }
            // Verknüpfung BM zu Produktgruppe zu KEM ab Daten
            Map<String, String> modelIdsToProductGroup = truckBOMModelData.getAssociationFromIDsToProductGroupDataMap();
            if ((modelIdsToProductGroup == null) || modelIdsToProductGroup.isEmpty()) {
                fireErrorLF("!!Die Importdatei \"%1\" enthält keine Informationen zu Produktgruppen. " +
                            "Import wird abgebrochen", fileName);
                return false;
            }
            // Verknüpfung BM zu KEM ab Daten
            Map<String, TruckBOMSingleKEM> modelIdsToKemFromData = checkKEMFromData(truckBOMModelData, fileName);
            if (modelIdsToKemFromData == null) {
                return false;
            }
            // Verknüpfung BM zu KEM bis Daten
            Map<String, TruckBOMSingleKEM> modelIdsToKemToData = checkKEMToData(truckBOMModelData, fileName);
            // SubImporter starten
            ModelKeyValueReader modelKeyValueReader = new ModelKeyValueReader(getSavedJSONFile(), singleVehicleModels,
                                                                              singleAggregateModels, modelIdsToKemFromData,
                                                                              modelIdsToKemToData, modelIdsToProductGroup,
                                                                              TABLE_DA_MODEL, getSubImporters().get(TABLE_DA_MODEL));
            if (startSubImporter(TABLE_DA_MODEL, modelKeyValueReader)) {
                updateModelDate(singleVehicleModels, singleAggregateModels, modelIdsToKemFromData);
            } else {
                logSkipImport(TABLE_DA_MODEL);
            }

        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireErrorLF("!!Fehler beim Importieren der Baumusterstammdaten aus TruckBOM.foundation");
            return false;
        }
        return true;
    }

    /**
     * Setzt das Datum des Änderungsstand 1 als Freigabedatum am Baumuster
     *
     * @param singleVehicleModels
     * @param singleAggregateModels
     * @param modelIdsToKemFromData
     */
    private void updateModelDate(List<TruckBOMSingleVehicleModel> singleVehicleModels, List<TruckBOMSingleAggregateModel> singleAggregateModels,
                                 Map<String, TruckBOMSingleKEM> modelIdsToKemFromData) {
        Map<String, String> modelToDate = new HashMap<>();
        // Alle Fahrzeug-BM heraussuchen, die in der Versorgung Daten zum 1. Änderungsstand haben
        if (Utils.isValid(singleVehicleModels)) {
            singleVehicleModels.forEach(vehicleModel -> {
                Optional<TruckBOMModelVersion> modelVersionWithFirstRev = vehicleModel.getVehicleVersion()
                        .stream()
                        .filter(modelVersion -> modelVersion.getVersion().equals("1"))
                        .findFirst();
                modelVersionWithFirstRev.ifPresent(modelVersion -> handleModelVersion(modelVersion, modelIdsToKemFromData,
                                                                                      modelToDate, vehicleModel.getIdentifier()));
            });
        }
        // Alle Aggregate-BM heraussuchen, die in der Versorgung Daten zum 1. Änderungsstand haben
        if (Utils.isValid(singleAggregateModels)) {
            singleAggregateModels.forEach(aggModel -> {
                Optional<TruckBOMModelVersion> modelVersionWithFirstRev = aggModel.getMajorComponentVersion()
                        .stream()
                        .filter(modelVersion -> modelVersion.getVersion().equals("1"))
                        .findFirst();
                modelVersionWithFirstRev.ifPresent(modelVersion -> handleModelVersion(modelVersion, modelIdsToKemFromData,
                                                                                      modelToDate, aggModel.getIdentifier()));
            });
        }

        // Alle BM mit Datum zum 1. Änderungsstand aus der DB laden und prüfen, ob das Freigabedatum gesetzt ist. Falls
        // nein, das Datum des 1. Änderungsstand setzen
        if (!modelToDate.isEmpty()) {
            modelToDate.forEach((model, date) -> {
                iPartsModelId modelId = new iPartsModelId(model);
                iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
                if (dataModel.existsInDB()) {
                    String currentDateValue = dataModel.getFieldValue(FIELD_DM_DATA);
                    if (StrUtils.isEmpty(currentDateValue)) {
                        dataModel.setFieldValue(FIELD_DM_DATA, date, DBActionOrigin.FROM_EDIT);
                        saveToDB(dataModel);
                    }
                }
            });
        }
    }

    private void handleModelVersion(TruckBOMModelVersion modelVersionData,
                                    Map<String, TruckBOMSingleKEM> modelIdsToKemFromData,
                                    Map<String, String> modelToDate, String modelIdentifier) {
        TruckBOMSingleKEM kemData = modelIdsToKemFromData.get(modelVersionData.getId());
        if (kemData != null) {
            String dateFrom = kemData.getReleaseDate();
            if (StrUtils.isValid(dateFrom)) {
                iPartsEDSDateTimeHandler dateTimeHandler = new iPartsEDSDateTimeHandler(dateFrom);
                modelToDate.put(modelIdentifier, dateTimeHandler.getBomDbDateValue());
            }
        }
    }

    @Override
    protected void clearCaches() {
        // Assembly-Caches löschen
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.MODEL,
                                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                  (AssemblyId)null, true));

    }

    /**
     * Konvertiert ein {@link TruckBOMModelVersion} Objekt in einen ImportRecord (Map) für den
     * {@link EDSModelUpdateImporter}
     *
     * @param modelNumber
     * @param modelVersion
     * @param kemFromForParts
     * @param kemToForParts
     * @param modelIdsToProductGroup
     * @return
     */
    private ModelRecordData convertModelToImportRecord(String modelNumber, TruckBOMModelVersion modelVersion,
                                                       Map<String, TruckBOMSingleKEM> kemFromForParts,
                                                       Map<String, TruckBOMSingleKEM> kemToForParts,
                                                       Map<String, String> modelIdsToProductGroup) {
        ModelRecordData recordData = new ModelRecordData();
        // Baumuster setzen
        addValueIfExists(EDSModelUpdateImporter.EDSU_BM_SNR, modelNumber, recordData);
        // AS ab setzen
        addValueIfExists(EDSModelUpdateImporter.EDSU_BM_AS_AB, modelVersion.getVersion(), recordData);
        // Produktgruppe
        String productGroup = (modelIdsToProductGroup == null) ? null : modelIdsToProductGroup.get(modelVersion.getId());
        if (productGroup == null) {
            productGroup = "";
        }
        recordData.put(EDSModelUpdateImporter.EDSU_BM_PGKZ, productGroup);
        // Texte setzen (Description und Remark)
        addTexts(recordData, modelVersion);
        // Zusätzliche Texte setzen (technicalData und salesDescription)
        recordData.setTechnicalData(TruckBOMMultiLangData.convertTextsToMultiLang(modelVersion.getTechnicalData()));
        recordData.setSalesDescription(TruckBOMMultiLangData.convertTextsToMultiLang(modelVersion.getSalesDescription()));
        // KEM ab Daten setzen
        addKEMData(recordData, modelVersion.getId(), kemFromForParts, null,
                   EDSModelUpdateImporter.EDSU_BM_VAKZ_AB, null);
        // KEM bis Daten setzen
        addKEMData(recordData, modelVersion.getId(), kemToForParts, null,
                   EDSModelUpdateImporter.EDSU_BM_VAKZ_BIS, null);

        return recordData;
    }

    private class ModelKeyValueReader extends AbstractTruckBOMKeyValueJSONReaderWithKEMData {

        private final List<TruckBOMSingleVehicleModel> singleVehicleModels;
        private final List<TruckBOMSingleAggregateModel> singleAggregateModels;
        private final Map<String, String> modelIdsToProductGroup;
        private final AbstractBOMXMLDataImporter importer;

        public ModelKeyValueReader(DWFile savedJSONFile, List<TruckBOMSingleVehicleModel> singleVehicleModels,
                                   List<TruckBOMSingleAggregateModel> singleAggregateModels,
                                   Map<String, TruckBOMSingleKEM> modelIdsToKemFromData,
                                   Map<String, TruckBOMSingleKEM> modelIdsToKemToData,
                                   Map<String, String> modelIdsToProductGroup, String tableName,
                                   AbstractBOMXMLDataImporter importer) {
            super(savedJSONFile, modelIdsToKemFromData, modelIdsToKemToData, calculateRecordCount(singleVehicleModels, singleAggregateModels),
                  tableName);
            this.singleVehicleModels = (singleVehicleModels == null) ? null : new LinkedList<>(singleVehicleModels);
            this.singleAggregateModels = (singleAggregateModels == null) ? null : new LinkedList<>(singleAggregateModels);
            this.modelIdsToProductGroup = modelIdsToProductGroup;
            this.importer = importer;
        }

        @Override
        protected List<RecordData> getNextRecordData() {
            if (!isCancelled()) {
                if (Utils.isValid(singleVehicleModels)) {
                    TruckBOMSingleVehicleModel singleVehicleModel = singleVehicleModels.remove(0);
                    String vehicleModelNumber = singleVehicleModel.getIdentifier();
                    // Falls kein Fahrzeug-Baumuster existiert, brauchen wir nicht zu importieren
                    if (StrUtils.isEmpty(vehicleModelNumber)) {
                        return null;
                    }
                    List<TruckBOMModelVersion> allVersions = singleVehicleModel.getVehicleVersion();
                    return createRecordForModelVersion(allVersions, vehicleModelNumber);
                } else if (Utils.isValid(singleAggregateModels)) {
                    TruckBOMSingleAggregateModel singleAggregateModel = singleAggregateModels.remove(0);
                    String aggregateModelNumber = singleAggregateModel.getIdentifier();
                    // Falls kein Aggregate-Baumuster existiert, brauchen wir nicht zu importieren
                    if (StrUtils.isEmpty(aggregateModelNumber)) {
                        return null;
                    }
                    List<TruckBOMModelVersion> allVersions = singleAggregateModel.getMajorComponentVersion();
                    return createRecordForModelVersion(allVersions, aggregateModelNumber);
                }
            }
            return null;
        }

        private List<RecordData> createRecordForModelVersion(List<TruckBOMModelVersion> allVersions, String modelNumber) {
            // Falls kein Baumuster existiert, brauchen wir nicht zu importieren
            if (StrUtils.isEmpty(modelNumber)) {
                return null;
            }
            List<RecordData> createdRecords = new ArrayList<>(allVersions.size());
            allVersions.sort(Comparator.comparing(TruckBOMModelVersion::getVersion));
            // Pro Version einen ImportRecord erzeugen
            allVersions.forEach(modelVersion -> createdRecords.add(convertModelToImportRecord(modelNumber,
                                                                                              modelVersion,
                                                                                              getKemFromForParts(),
                                                                                              getKemToForParts(),
                                                                                              modelIdsToProductGroup)));
            return createdRecords;
        }

        @Override
        protected void postProcessRecordData(RecordData record) {
            // Texte setzen sofern welche vorhanden sind
            Map<String, EtkMultiSprache> textsForRecord = new HashMap<>();
            addTextObjectIfNotNull(EDSModelUpdateImporter.EDSU_BM_BEN, record.getDescription(), textsForRecord);
            addTextObjectIfNotNull(EDSModelUpdateImporter.EDSU_BM_BEM, record.getRemark(), textsForRecord);
            if (record instanceof ModelRecordData) {
                addTextObjectIfNotNull(EDSModelUpdateImporter.EDSU_BM_TEDAT, ((ModelRecordData)record).getTechnicalData(), textsForRecord);
                addTextObjectIfNotNull(EDSModelUpdateImporter.EDSU_BM_VBEZ, ((ModelRecordData)record).getSalesDescription(), textsForRecord);
            }
            importer.setTextsForMultiLangFields(textsForRecord);
        }

        @Override
        protected String getOriginalTableName() {
            return EDSModelUpdateImporter.IMPORT_TABLENAME;
        }
    }

    private static int calculateRecordCount(List<TruckBOMSingleVehicleModel> singleVehicleModels, List<TruckBOMSingleAggregateModel> singleAggregateModels) {
        return ((singleVehicleModels == null) ? 0 : singleVehicleModels.size()) + ((singleAggregateModels == null) ? 0 : singleAggregateModels.size());
    }

    private static class ModelRecordData extends RecordData {

        private EtkMultiSprache technicalData;
        private EtkMultiSprache salesDescription;


        public EtkMultiSprache getTechnicalData() {
            return technicalData;
        }


        public EtkMultiSprache getSalesDescription() {
            return salesDescription;
        }

        public void setTechnicalData(EtkMultiSprache technicalData) {
            if ((technicalData != null) && !technicalData.isEmpty()) {
                this.technicalData = technicalData;
            }
        }

        public void setSalesDescription(EtkMultiSprache salesDescription) {
            if ((salesDescription != null) && !salesDescription.isEmpty()) {
                this.salesDescription = salesDescription;
            }
        }
    }
}
