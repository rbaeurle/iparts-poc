/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMSingleKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getModelElementUsage.TruckBOMModelElementUsageData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getModelElementUsage.TruckBOMModelElementUsageVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getModelElementUsage.TruckBOMSingleModelElementUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSWbSaaCalculationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.helper.TruckBOMFoundationDataCorrectionHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.EDSModelMasterContentUpdateImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.iPartsBomDBFactoriesHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.JSONUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sort.SortStringCache;

import java.util.*;

/**
 * Importer für die Produktstrukturdaten aus TruckBOM.foundation
 */
public class TruckBOMModelElementUsageImporter extends AbstractTruckBOMFoundationJSONImporter {

    protected static final String EMPTY_MATURITY_VALUE = "_";

    private boolean useNewStructure; // Sollen die Daten als neue Struktur importiert werden
    private Set<IdWithType> usedIds;
    private SortStringCache sortStringCache = new SortStringCache();
    private EDSWbSaaCalculationHelper wbCalcHelper;

    public TruckBOMModelElementUsageImporter(EtkProject project) {
        super(project, TRUCK_BOM_FOUNDATION_MODEL_ELEMENT_USAGE_IMPORT_NAME, TABLE_DA_EDS_MODEL);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected Map<String, AbstractBOMXMLDataImporter> createSubImporter() {
        Map<String, AbstractBOMXMLDataImporter> importer = new HashMap<>();
        importer.put(TABLE_DA_EDS_MODEL, new EDSModelMasterContentUpdateImporter(getProject()));
        return importer;
    }

    @Override
    protected void preImportTask() {
        this.usedIds = new HashSet<>();
        this.useNewStructure = iPartsEdsStructureHelper.getInstance().isNewStructureActive();
        wbCalcHelper = new EDSWbSaaCalculationHelper(getProject(), this);
        super.preImportTask();
    }

    @Override
    protected void postImportTask() {
        // Arbeitsvorrat Vorverdichtung mit anschließendem Speichern
        wbCalcHelper.handleSaaAVEntries();
        wbCalcHelper = null;
        super.postImportTask();
    }

    @Override
    protected boolean importJSONResponse(String response) {
        String fileName = getImportName(getProject().getDBLanguage());
        Genson genson = JSONUtils.createGenson(true);
        try {
            TruckBOMModelElementUsageData truckBOMModelElementUsageData = deserializeFromString(genson, response, fileName, TruckBOMModelElementUsageData.class);
            if (truckBOMModelElementUsageData == null) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine gültigen Werte.", fileName);
                return false;
            }
            if (!checkObjectCount(truckBOMModelElementUsageData)) {
                return true;
            }
            // Alle Produktstruktur-Stammdaten
            List<TruckBOMSingleModelElementUsage> singleModelElementUsageList = truckBOMModelElementUsageData.getModelElementUsage();
            if ((singleModelElementUsageList == null) || singleModelElementUsageList.isEmpty()) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine Produktstrukturdaten.", fileName);
                return false;
            }

            // Verknüpfung Produktstruktur zu KEM ab Daten
            Map<String, TruckBOMSingleKEM> idsToKemFromData = checkKEMFromData(truckBOMModelElementUsageData, fileName);
            if (idsToKemFromData == null) {
                return false;
            }
            // Verknüpfung Produktstruktur zu KEM bis Daten
            Map<String, TruckBOMSingleKEM> idsToKemToData = checkKEMToData(truckBOMModelElementUsageData, fileName);

            // Verknüpfung BM zu Produktgruppe zu KEM ab Daten
            Map<String, String> modelIdsToProductGroup = truckBOMModelElementUsageData.getAssociationFromIDsToProductGroupDataMap();
            if ((modelIdsToProductGroup == null) || modelIdsToProductGroup.isEmpty()) {
                fireErrorLF("!!Die Importdatei \"%1\" enthält keine Informationen zu Produktgruppen. " +
                            "Import wird abgebrochen", fileName);
                return false;
            }

            Map<String, Set<String>> assocIdToDistributionTask = checkDistributionTasksData(truckBOMModelElementUsageData, fileName);
            ModelElementUsageValueReader modelElementUsageValueReader = new ModelElementUsageValueReader(getSavedJSONFile(),
                                                                                                         singleModelElementUsageList,
                                                                                                         idsToKemFromData,
                                                                                                         idsToKemToData,
                                                                                                         modelIdsToProductGroup,
                                                                                                         assocIdToDistributionTask);
            if (useNewStructure) {
                // Als neue Struktur in der neuen Tabelle ablegen
                importAsNewStructureDirect(singleModelElementUsageList, modelIdsToProductGroup, assocIdToDistributionTask,
                                           idsToKemFromData, idsToKemToData);
            } else {
                // SubImporter starten
                if (startSubImporter(TABLE_DA_EDS_MODEL, modelElementUsageValueReader)) {
                    // Nach dem Import die Daten aktualisieren
                    updateEDSModelData(singleModelElementUsageList);
                } else {
                    logSkipImport(TABLE_DA_EDS_MODEL);
                }
            }

        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireErrorLF("!!Fehler beim Importieren der Produktstrukturdaten aus TruckBOM.foundation");
            return false;
        }
        return true;
    }

    /**
     * Importiert die Daten als neuen Struktur für die EDS/BCS Konstruktion
     *
     * @param singleModelElementUsageList
     * @param modelIdsToProductGroup
     * @param assocIdToDistributionTask
     * @param idsToKemFromData
     * @param idsToKemToData
     */
    private void importAsNewStructureDirect(List<TruckBOMSingleModelElementUsage> singleModelElementUsageList,
                                            Map<String, String> modelIdsToProductGroup,
                                            Map<String, Set<String>> assocIdToDistributionTask, Map<String, TruckBOMSingleKEM> idsToKemFromData,
                                            Map<String, TruckBOMSingleKEM> idsToKemToData) {
        int recordCount = singleModelElementUsageList.size();
        fireMessage("!!Verarbeite %1 Strukturdatensätze", String.valueOf(recordCount));
        VarParam<Integer> importRecordCount = new VarParam<>(0);
        iPartsEDSDateTimeHandler dtHandler = new iPartsEDSDateTimeHandler("");
        // Alle Haupt-Datensätze durchlaufen
        singleModelElementUsageList.forEach(singleModelElementUsage -> {
            if (singleModelElementUsage.hasVersions()) {
                // Schlüsselelemente des Hauptdatensatzes
                String modelIdentifier = singleModelElementUsage.getModelIdentifier();
                String position = getPositionValue(singleModelElementUsage.getPosition());
                String moduleCategoryIdentifier = singleModelElementUsage.getModuleCategoryIdentifier();
                String subModuleCategoryIdentifier = singleModelElementUsage.getSubModuleCategoryIdentifier();
                if (StrUtils.isValid(modelIdentifier, position, moduleCategoryIdentifier, subModuleCategoryIdentifier)) {
                    String legacyDifferentiationNumber = singleModelElementUsage.getLegacyDifferentiationNumber();
                    // Die einzelnen Versionen eines Haupdatensatzes sortieren und verarbeiten
                    List<TruckBOMModelElementUsageVersion> allVersions = singleModelElementUsage.getModelElementUsageVersion();
                    allVersions.sort((o1, o2) -> {
                        String firstCompareString = Utils.toSortString(o1.getVersion());
                        String secondCompareString = Utils.toSortString(o2.getVersion());
                        return firstCompareString.compareTo(secondCompareString);
                    });
                    allVersions.forEach(singleModelElementUsageVersion -> {
                        iPartsModelElementUsageId modelElementUsageId = new iPartsModelElementUsageId(modelIdentifier,
                                                                                                      moduleCategoryIdentifier,
                                                                                                      subModuleCategoryIdentifier,
                                                                                                      position,
                                                                                                      getValidValue(legacyDifferentiationNumber),
                                                                                                      getValidValue(singleModelElementUsageVersion.getVersion()));
                        iPartsDataModelElementUsage modelElementUsage = new iPartsDataModelElementUsage(getProject(), modelElementUsageId);
                        boolean existsInDb = true;
                        if (!modelElementUsage.existsInDB()) {
                            modelElementUsage.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                            existsInDb = false;
                        }
                        fillPlainData(modelElementUsage, singleModelElementUsageVersion);
                        // ID der Version -> landet nicht im DataObject! Wird aber für die Verknüpfungen gebraucht
                        String id = getValidValue(singleModelElementUsageVersion.getId());
                        // Produktgruppe setzen
                        String productGroup = (modelIdsToProductGroup == null) ? "" : modelIdsToProductGroup.get(id);
                        modelElementUsage.setFieldValue(FIELD_DMEU_PGKZ, getValidValue(productGroup), DBActionOrigin.FROM_EDIT);

                        addKemDataToNewStructureObject(modelElementUsage, idsToKemFromData.get(id), FIELD_DMEU_KEMFROM, FIELD_DMEU_RELEASE_FROM, dtHandler);
                        addKemDataToNewStructureObject(modelElementUsage, idsToKemToData.get(id), FIELD_DMEU_KEMTO, FIELD_DMEU_RELEASE_TO, dtHandler);

                        // Werksverteiler Daten setzen
                        String plantSupplies = createPlantSuppliesValue(assocIdToDistributionTask, singleModelElementUsageVersion);
                        modelElementUsage.setFieldValue(FIELD_DMEU_PLANTSUPPLY, plantSupplies, DBActionOrigin.FROM_EDIT);

                        wbCalcHelper.addToMap(existsInDb, modelElementUsage);
                        if (saveToDB(modelElementUsage)) {
                            importRecordCount.setValue(importRecordCount.getValue() + 1);
                        }
                        String subElement = modelElementUsage.getFieldValue(FIELD_DMEU_SUB_ELEMENT);
                        saveRelatedData(modelIdentifier, subElement);
                    });
                }
            }
        });

        saveBufferListToDB(true);
        fireMessage("!!Korrigiere Ab- und Bis-Werte für alle %1 verarbeitete Revisionen...", String.valueOf(importRecordCount.getValue()));
        updateModelElementUsageData(singleModelElementUsageList);
        logImportRecordsFinished(importRecordCount.getValue());
    }

    /**
     * Setzt die verknüpften Daten in der DB
     *
     * @param modelIdentifier
     * @param subElement
     */
    private void saveRelatedData(String modelIdentifier, String subElement) {
        // Beziehung SAAs zu Baumuster
        iPartsDataSAAModels saaModels = iPartsMainImportHelper.createSaaModelEntryIfNotExists(getProject(), subElement, modelIdentifier, usedIds, iPartsImportDataOrigin.EDS);
        if (saaModels != null) {
            saveToDB(saaModels);
        }
        // Zuordnung Aggregate-BM zu BM
        iPartsDataModelsAggs modelAgg = iPartsMainImportHelper.createModelsAggsEntryIfNotExists(getProject(), modelIdentifier, subElement, usedIds, iPartsImportDataOrigin.EDS);
        if (modelAgg != null) {
            saveToDB(modelAgg);
        }
    }

    /**
     * Befüllt das Objekt mit den "einfachen" Daten direkt am Objekt
     *
     * @param modelElementUsage
     * @param singleModelElementUsageVersion
     */
    private void fillPlainData(iPartsDataModelElementUsage modelElementUsage,
                               TruckBOMModelElementUsageVersion singleModelElementUsageVersion) {
        modelElementUsage.setFieldValue(FIELD_DMEU_SUB_ELEMENT, getValidValue(singleModelElementUsageVersion.getModelElementIdentifier()), DBActionOrigin.FROM_EDIT);
        modelElementUsage.setFieldValue(FIELD_DMEU_STEERING, getValidValue(TruckBOMFoundationDataCorrectionHelper.extractSteeringValue(singleModelElementUsageVersion)), DBActionOrigin.FROM_EDIT);
        modelElementUsage.setFieldValue(FIELD_DMEU_CODE, getValidValue(singleModelElementUsageVersion.getCodeRule()), DBActionOrigin.FROM_EDIT);
        modelElementUsage.setFieldValue(FIELD_DMEU_QUANTITY, getValidValue(singleModelElementUsageVersion.getQuantity()), DBActionOrigin.FROM_EDIT);

        // Beim Reifegrad wird von "_" auf leer gemappt
        String maturityLevel = singleModelElementUsageVersion.getMaturityLevel();
        if (maturityLevel.equals(EMPTY_MATURITY_VALUE)) {
            maturityLevel = StrUtils.replaceSubstring(maturityLevel, EMPTY_MATURITY_VALUE, "");
        }
        modelElementUsage.setFieldValue(FIELD_DMEU_RFG, getValidValue(maturityLevel), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Setzt die KEM Daten der neuen Strukturdaten
     *
     * @param modelElementUsage
     * @param kemData
     * @param kemField
     * @param releaseDateField
     * @param dtHandler
     */
    private void addKemDataToNewStructureObject(iPartsDataModelElementUsage modelElementUsage, TruckBOMSingleKEM kemData, String kemField,
                                                String releaseDateField, iPartsEDSDateTimeHandler dtHandler) {
        if (modelElementUsage == null) {
            return;
        }
        if ((kemData != null) && StrUtils.isValid(kemData.getIdentifier())) {
            modelElementUsage.setFieldValue(kemField, kemData.getIdentifier(), DBActionOrigin.FROM_EDIT);
            String value = kemData.getReleaseDate();
            if ((dtHandler != null) && (value != null)) {
                modelElementUsage.setFieldValue(releaseDateField, dtHandler.convertASPLMISOToDBDateTime(value), DBActionOrigin.FROM_EDIT);
            } else {
                modelElementUsage.setFieldValue(releaseDateField, "", DBActionOrigin.FROM_EDIT);
            }
        } else {
            modelElementUsage.setFieldValue(kemField, "", DBActionOrigin.FROM_EDIT);
            modelElementUsage.setFieldValue(releaseDateField, "", DBActionOrigin.FROM_EDIT);
        }
    }

    private String getValidValue(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    /**
     * Aktualisiert die Verknüpfung zwischen bestehenden und importierten Daten (KEM bis, AS bis und Datum bis) für
     * die Tabelle DA_EDS_MODEL
     *
     * @param singleModelElementUsageList
     */
    private void updateEDSModelData(List<TruckBOMSingleModelElementUsage> singleModelElementUsageList) {
        updateFromAndToData(singleModelElementUsageList, FIELD_EDS_MODEL_REVFROM, FIELD_EDS_MODEL_REVTO,
                            FIELD_EDS_MODEL_KEMFROM, FIELD_EDS_MODEL_KEMTO, FIELD_EDS_MODEL_RELEASE_FROM,
                            FIELD_EDS_MODEL_RELEASE_TO);
    }

    private void updateModelElementUsageData(List<TruckBOMSingleModelElementUsage> singleModelElementUsageList) {
        updateFromAndToData(singleModelElementUsageList, FIELD_DMEU_REVFROM, FIELD_DMEU_REVTO,
                            FIELD_DMEU_KEMFROM, FIELD_DMEU_KEMTO, FIELD_DMEU_RELEASE_FROM,
                            FIELD_DMEU_RELEASE_TO);
    }

    private void updateFromAndToData(List<TruckBOMSingleModelElementUsage> singleModelElementUsageList,
                                     String revFromField, String revToField,
                                     String kemFromField, String kemToField,
                                     String releaseFromField, String releaseToField) {
        TruckBOMFoundationDataCorrectionHelper dataTransferHelper = new TruckBOMFoundationDataCorrectionHelper(this,
                                                                                                               revFromField,
                                                                                                               revToField,
                                                                                                               EDSImportHelper.EDS_AS_BIS_UNENDLICH);
        dataTransferHelper.addFields(kemFromField, kemToField);
        dataTransferHelper.addFields(releaseFromField, releaseToField);

        singleModelElementUsageList.forEach(singleModelElementUsageObject -> {
            String modelNumber = singleModelElementUsageObject.getModelIdentifier();
            String module = singleModelElementUsageObject.getModuleCategoryIdentifier();
            String subModule = singleModelElementUsageObject.getSubModuleCategoryIdentifier();
            String position = getPositionValue(singleModelElementUsageObject.getPosition());
            Set<String> alreadyHandledSteeringType = new HashSet<>();
            // Eigentlich reicht es, wenn man sich nur das "singleModelElementUsageObject" anschaut aber die Lenkung
            // existiert nur in der jeweiligen Version. Die Lenkung ist zugleich auch Teil des Schlüssels. Somit muss man
            // jede vorhandene Schlüsselkonstellation korrigieren. Wenn es L, R und leer gibt muss man die Ketten für
            // diese drei Option separat korrigieren, daher das Set mit den Lenkungswerten
            singleModelElementUsageObject.getModelElementUsageVersion().forEach(singleModelElementUsageVersionObject -> {
                // Falls keine Struktur existiert, brauchen wir nicht zu korrigieren
                if (!StrUtils.isValid(modelNumber, position, module, subModule)) {
                    return;
                }
                // Alle Stände zu einer Struktur laden (sortiert nach AS ab)
                EtkDataObjectList<? extends EtkDataObject> allDataObjects;
                if (useNewStructure) {
                    allDataObjects
                            = iPartsDataModelElementUsageList.loadAllRevFromVariants(getProject(), modelNumber, module, subModule,
                                                                                     position, new String[]{ revFromField },
                                                                                     false);
                } else {
                    // Lenkung ist kein Kriterium für eine Gruppierung, daher hier alle Datensätze unabhängig von der Lenkung laden
                    allDataObjects
                            = iPartsDataEDSModelContentList.loadAllRevFromVariants(getProject(), modelNumber, module, subModule,
                                                                                   position, "", new String[]{ revFromField },
                                                                                   false);
                }
                dataTransferHelper.correctDBDataRevisionChain(allDataObjects);
            });
        });
    }

    /**
     * Erzeugt pro {@link TruckBOMModelElementUsageVersion} zum übergebenen {@link TruckBOMSingleModelElementUsage} ein
     * {@link RecordData} Objekt
     *
     * @param modelIdentifier
     * @param position
     * @param module
     * @param scope
     * @param allSingleModelElementUsageVersions
     * @param kemFromForParts
     * @param kemToForParts
     * @param modelElementIdsToProductGroup
     * @param assocIdToDistributionTask
     * @return
     */
    private List<RecordData> createRecordDataForSingleModelElementUsage(String modelIdentifier, String position,
                                                                        String module, String scope,
                                                                        List<TruckBOMModelElementUsageVersion> allSingleModelElementUsageVersions,
                                                                        Map<String, TruckBOMSingleKEM> kemFromForParts,
                                                                        Map<String, TruckBOMSingleKEM> kemToForParts,
                                                                        Map<String, String> modelElementIdsToProductGroup,
                                                                        Map<String, Set<String>> assocIdToDistributionTask) {
        List<RecordData> createdRecords = new LinkedList<>();
        allSingleModelElementUsageVersions.sort((o1, o2) -> {
            String firstCompareString = sortStringCache.getSortString(o1.getVersion(), false);
            String secondCompareString = sortStringCache.getSortString(o2.getVersion(), false);
            return firstCompareString.compareTo(secondCompareString);
        });
        allSingleModelElementUsageVersions.forEach(singleModelElementUsageVersion ->
                                                           createdRecords.add(convertModelElementUsageToImportRecord(modelIdentifier,
                                                                                                                     position,
                                                                                                                     module,
                                                                                                                     scope,
                                                                                                                     singleModelElementUsageVersion,
                                                                                                                     kemFromForParts,
                                                                                                                     kemToForParts,
                                                                                                                     modelElementIdsToProductGroup,
                                                                                                                     assocIdToDistributionTask)));
        return createdRecords;
    }


    /**
     * Konvertiert ein {@link TruckBOMSingleModelElementUsage} Objekt in einen ImportRecord (Map) für den
     * {@link EDSModelMasterContentUpdateImporter}
     *
     * @param modelIdentifier
     * @param position
     * @param module
     * @param scope
     * @param singleModelElementUsageVersion
     * @param kemFromForParts
     * @param kemToForParts
     * @param modelElementIdsToProductGroup
     * @param assocIdToDistributionTask
     * @return
     */
    private RecordData convertModelElementUsageToImportRecord(String modelIdentifier, String position,
                                                              String module, String scope,
                                                              TruckBOMModelElementUsageVersion singleModelElementUsageVersion,
                                                              Map<String, TruckBOMSingleKEM> kemFromForParts,
                                                              Map<String, TruckBOMSingleKEM> kemToForParts,
                                                              Map<String, String> modelElementIdsToProductGroup,
                                                              Map<String, Set<String>> assocIdToDistributionTask) {
        RecordData recordData = new RecordData();
        // Baumuster, Position, Gruppe und Umfang  setzen
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_MODEL, modelIdentifier, recordData);
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_POSITION, position, recordData);
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_MODULE, module, recordData);
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_SCOPE, scope, recordData);
        // Die "einfachen" String basierten Daten befüllen
        addPlainData(recordData, singleModelElementUsageVersion);
        // Produktgruppe setzen
        String productGroup = (modelElementIdsToProductGroup == null) ? null : modelElementIdsToProductGroup.get(singleModelElementUsageVersion.getId());
        if (productGroup == null) {
            productGroup = "";
        }
        recordData.put(EDSModelMasterContentUpdateImporter.B2I_PRODUCT_GROUP, productGroup);
        // KEM ab Daten setzen
        addKEMData(recordData, singleModelElementUsageVersion.getId(), kemFromForParts, EDSModelMasterContentUpdateImporter.B2I_ECO_FROM,
                   EDSModelMasterContentUpdateImporter.B2I_STATUS_FROM, EDSModelMasterContentUpdateImporter.B2I_RELEASE_FROM);
        // KEM bis Daten setzen
        addKEMData(recordData, singleModelElementUsageVersion.getId(), kemToForParts, EDSModelMasterContentUpdateImporter.B2I_ECO_TO,
                   EDSModelMasterContentUpdateImporter.B2I_STATUS_TO, EDSModelMasterContentUpdateImporter.B2I_RELEASE_TO);
        addPlantSupplyIfExists(recordData, assocIdToDistributionTask, singleModelElementUsageVersion.getId(),
                               EDSModelMasterContentUpdateImporter.B2I_PLANTSUPPLIES, EDSModelMasterContentUpdateImporter.B2I_PLANTSUPPLY);

        return recordData;
    }

    /**
     * Erzeugt den String mit den unterschiedlichen Werken zur ID
     *
     * @param assocIdToDistributionTask
     * @param singleModelElementUsageVersion
     * @return
     */
    private String createPlantSuppliesValue(Map<String, Set<String>> assocIdToDistributionTask,
                                            TruckBOMModelElementUsageVersion singleModelElementUsageVersion) {
        String plantSuppliesDBValue = "";
        if ((assocIdToDistributionTask != null) && !assocIdToDistributionTask.isEmpty()) {
            Set<String> plantSupplies = assocIdToDistributionTask.get(singleModelElementUsageVersion.getId());
            if (plantSupplies != null) {
                plantSuppliesDBValue = iPartsBomDBFactoriesHelper.getDBValueForPlantSupplies(plantSupplies);
            }
        }
        return plantSuppliesDBValue;
    }

    /**
     * Setzt die "einfachen" String basierten Daten
     *
     * @param recordData
     * @param newestModelElementUsage
     */
    private void addPlainData(RecordData recordData, TruckBOMModelElementUsageVersion newestModelElementUsage) {
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_VERSION_FROM, newestModelElementUsage.getVersion(), recordData);
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_ITEM, newestModelElementUsage.getModelElementIdentifier(), recordData);
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_CODE_RULE, newestModelElementUsage.getCodeRule(), recordData);
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_QUANTITY, newestModelElementUsage.getQuantity(), recordData);
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_STEERING_TYPE, TruckBOMFoundationDataCorrectionHelper.extractSteeringValue(newestModelElementUsage), recordData);
        // Beim Reifegrad wird von "_" auf leer gemappt
        String maturityLevel = newestModelElementUsage.getMaturityLevel();
        if (maturityLevel.equals(EMPTY_MATURITY_VALUE)) {
            maturityLevel = StrUtils.replaceSubstring(maturityLevel, EMPTY_MATURITY_VALUE, "");
        }
        addValueIfExists(EDSModelMasterContentUpdateImporter.B2I_MATURITY_LEVEL, maturityLevel, recordData);
        // AA ist immer leer ist aber im Schlüssel -> BodyTypes benutzen
        recordData.put(EDSModelMasterContentUpdateImporter.B2I_BODYTYPES, "");
        recordData.put(EDSModelMasterContentUpdateImporter.B2I_BODYTYPE, "");
    }

    private String getPositionValue(String position) {
        if (useNewStructure) {
            return (position == null) ? "" : position;
        }
        return StrUtils.leftFill(position, 3, '0');
    }

    /**
     * KeyValue-Reader für den Produktstruktur Sub-Importer (getT43RB2I)
     */
    private class ModelElementUsageValueReader extends AbstractTruckBOMKeyValueJSONReaderWithKEMData {

        private final List<TruckBOMSingleModelElementUsage> singleModelElementUsageList;
        private final Map<String, String> modelElementIdsToProductGroup;
        private final Map<String, Set<String>> assocIdToDistributionTask;

        public ModelElementUsageValueReader(DWFile savedJSONFile, List<TruckBOMSingleModelElementUsage> singleModelElementUsageList,
                                            Map<String, TruckBOMSingleKEM> modelElementIdsToKemFromData,
                                            Map<String, TruckBOMSingleKEM> modelElementIdsToKemToData,
                                            Map<String, String> modelElementIdsToProductGroup,
                                            Map<String, Set<String>> assocIdToDistributionTask) {
            super(savedJSONFile, modelElementIdsToKemFromData, modelElementIdsToKemToData,
                  singleModelElementUsageList.size(), TABLE_DA_EDS_MODEL);
            this.singleModelElementUsageList = singleModelElementUsageList;
            this.modelElementIdsToProductGroup = modelElementIdsToProductGroup;
            this.assocIdToDistributionTask = assocIdToDistributionTask;
        }

        @Override
        protected List<RecordData> getNextRecordData() {
            if (!isCancelled()) {
                // Den nächsten Datensatz bestimmen und den Zähler um eins erhöhen
                TruckBOMSingleModelElementUsage singleModelElementUsage = getSingleModelElementUsageList().get(getImportRecCount());
                if (singleModelElementUsage.hasVersions()) {
                    String modelIdentifier = singleModelElementUsage.getModelIdentifier();
                    String position = getPositionValue(singleModelElementUsage.getPosition());
                    String moduleCategoryIdentifier = singleModelElementUsage.getModuleCategoryIdentifier();
                    String subModuleCategoryIdentifier = singleModelElementUsage.getSubModuleCategoryIdentifier();
                    if (StrUtils.isValid(modelIdentifier, position, moduleCategoryIdentifier, subModuleCategoryIdentifier)) {
                        List<TruckBOMModelElementUsageVersion> allVersions = singleModelElementUsage.getModelElementUsageVersion();
                        return createRecordDataForSingleModelElementUsage(modelIdentifier,
                                                                          position,
                                                                          moduleCategoryIdentifier,
                                                                          subModuleCategoryIdentifier,
                                                                          allVersions,
                                                                          getKemFromForParts(),
                                                                          getKemToForParts(),
                                                                          modelElementIdsToProductGroup,
                                                                          assocIdToDistributionTask);
                    }
                }
            }
            return null;
        }

        @Override
        protected void postProcessRecordData(RecordData record) {

        }

        @Override
        protected String getOriginalTableName() {
            return EDSModelMasterContentUpdateImporter.IMPORT_TABLENAME;
        }

        public List<TruckBOMSingleModelElementUsage> getSingleModelElementUsageList() {
            return singleModelElementUsageList;
        }
    }
}
