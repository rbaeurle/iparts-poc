/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDataChangedEventByEdit;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMPartHistoryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMSingleKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpart.TruckBOMPartData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpart.TruckBOMPartVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpart.TruckBOMSinglePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.helper.TruckBOMFoundationDataCorrectionHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.BOMPartHistoryUpdateImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.BOMPartMasterDataUpdateImporter;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für die Teile-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMPartImporter extends AbstractTruckBOMFoundationJSONImporter {

    public TruckBOMPartImporter(EtkProject project) {
        super(project, TRUCK_BOM_FOUNDATION_PART_IMPORT_NAME, TABLE_MAT);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }


    /**
     * Konvertiert ein {@link TruckBOMPartVersion} Objekt in einen ImportRecord (Map) für den
     * {@link BOMPartMasterDataUpdateImporter} und den {@link BOMPartHistoryUpdateImporter}
     *
     * @param partNo
     * @param partVersion
     * @param kemFromForParts
     * @param kemToForParts
     * @return
     */
    private RecordData convertPartToImportRecord(String partNo, TruckBOMPartVersion partVersion, Map<String, TruckBOMSingleKEM> kemFromForParts,
                                                 Map<String, TruckBOMSingleKEM> kemToForParts) {
        RecordData recordData = new RecordData();
        // Teilennummer setzen
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_PART_NUMBER, partNo, recordData);
        // Die "einfachen" String basierten Daten befüllen
        addPlainData(recordData, partVersion);
        // Texte setzen (Description und Remark)
        addTexts(recordData, partVersion);
        // KEM ab Daten setzen
        addKEMData(recordData, partVersion.getId(), kemFromForParts, BOMPartMasterDataUpdateImporter.TEIL_ECO_FROM,
                   BOMPartMasterDataUpdateImporter.TEIL_VAKZ_AB, BOMPartMasterDataUpdateImporter.TEIL_RELEASE_FROM);
        // KEM bis Daten setzen
        addKEMData(recordData, partVersion.getId(), kemToForParts, BOMPartMasterDataUpdateImporter.TEIL_ECO_TO,
                   BOMPartMasterDataUpdateImporter.TEIL_VAKZ_BIS, BOMPartMasterDataUpdateImporter.TEIL_RELEASE_TO);

        return recordData;

    }

    /**
     * Setzt die "einfachen" String-basierten Werte ins ImportRecord
     *
     * @param importRecord
     * @param partData
     */
    private void addPlainData(RecordData importRecord, TruckBOMPartVersion partData) {
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_AS_AB, partData.getVersion(), importRecord);
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_PARTS_TYPE, partData.getType(), importRecord);
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_DRAWING_GEOMETRY_VERSION, partData.getGeometryVersion(), importRecord);
        // Es kann entweder einen Typ oder ein Datum geben
        String geometryDate = partData.getGeometryDate();
        if (StrUtils.isValid(geometryDate)) {
            // Beim Datum die Bindestriche entfernen
            addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_DRAWING_DATE_OR_TYPE, StrUtils.replaceSubstring(geometryDate, "-", ""), importRecord);
        } else {
            String masterDataMethod = partData.getMasterDataMethod();
            if (StrUtils.isValid(masterDataMethod)) {
                addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_DRAWING_DATE_OR_TYPE, masterDataMethod, importRecord);
            }
        }
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_REFERENCE_PART, partData.getSourcePartIdentifier(), importRecord);
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_REFERENCE_DRAWING, partData.getReferenceGeometryIdentifier(), importRecord);
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_PART_RELEASE_STATUS, partData.getReleaseIndicator(), importRecord);
        String quantityUnit = partData.getQuantityUnit();
        if (quantityUnit != null) {
            addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_QUANTITY_UNIT, StrUtils.leftFill(quantityUnit, 2, '0'), importRecord);
        }
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_COLOR_ITEM_TYPE, partData.getColoringType(), importRecord);
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_DOCUMENTATION_OBLIGATION, partData.getDocumentationObligation(), importRecord);
        addValueIfExists(BOMPartMasterDataUpdateImporter.TEIL_CALCULATED_WEIGHT, partData.getCalculatedWeight(), importRecord);
        handleOptionalBooleanValue(BOMPartMasterDataUpdateImporter.TEIL_SAFETY_RELEVANT, partData.getSafetyRelevant(), importRecord);
        handleOptionalBooleanValue(BOMPartMasterDataUpdateImporter.TEIL_CERT_RELEVANT, partData.getCertificationRelevant(), importRecord);
        handleOptionalBooleanValue(BOMPartMasterDataUpdateImporter.TEIL_VEHICLE_DOC_RELEVANT, partData.getVehicleDocumentationRelevant(), importRecord);
        handleOptionalBooleanValue(BOMPartMasterDataUpdateImporter.TEIL_THEFT_RELEVANT, partData.getTheftProtectionClass(), importRecord);
    }

    @Override
    protected boolean importJSONResponse(String response) {
        String fileName = getImportName(getProject().getDBLanguage());
        Genson genson = JSONUtils.createGenson(true);
        try {
            TruckBOMPartData truckBOMPartData = deserializeFromString(genson, response, fileName, TruckBOMPartData.class);
            if (truckBOMPartData == null) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine gültigen Werte.", fileName);
                return false;
            }
            if (!checkObjectCount(truckBOMPartData)) {
                return true;
            }
            // Alle Teilestammdaten
            List<TruckBOMSinglePart> singleParts = truckBOMPartData.getPart();
            if ((singleParts == null) || singleParts.isEmpty()) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine Teile-Stammdaten.", fileName);
                return false;
            }

            // Verknüpfung Teil zu KEM ab Daten
            Map<String, TruckBOMSingleKEM> partIdsToKemFromData = checkKEMFromData(truckBOMPartData, fileName);
            if (partIdsToKemFromData == null) {
                return false;
            }
            // Verknüpfung Teil zu KEM bis Daten
            Map<String, TruckBOMSingleKEM> partIdsToKemToData = checkKEMToData(truckBOMPartData, fileName);
            // SubImporter starten
            PartKeyValueReader partKeyValueReader = new PartKeyValueReader(getSavedJSONFile(), singleParts,
                                                                           partIdsToKemFromData, partIdsToKemToData,
                                                                           getSubImporters().get(TABLE_MAT), TABLE_MAT,
                                                                           checkDistributionTasksData(truckBOMPartData, fileName));
            if (!startSubImporter(TABLE_MAT, partKeyValueReader)) {
                logSkipImport(TABLE_MAT);
            }

            PartHistoryKeyValueReader partHistoryKeyValueReader = new PartHistoryKeyValueReader(getSavedJSONFile(),
                                                                                                singleParts,
                                                                                                partIdsToKemFromData,
                                                                                                partIdsToKemToData,
                                                                                                TABLE_DA_BOM_MAT_HISTORY);
            if (startSubImporter(TABLE_DA_BOM_MAT_HISTORY, partHistoryKeyValueReader)) {
                // Importierte Daten in der DB korrigieren
                updateMatHistoryData(singleParts);
            } else {
                logSkipImport(TABLE_DA_BOM_MAT_HISTORY);
            }

        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireErrorLF("!!Fehler beim Importieren der Teilestammdaten aus TruckBOM.foundation");
            return false;
        }
        return true;
    }

    /**
     * Aktualisiert die Verknüpfung zwischen bestehenden und importierten Daten (KEM bis, AS bis und Datum bis)
     *
     * @param singleParts
     */
    private void updateMatHistoryData(List<TruckBOMSinglePart> singleParts) {
        TruckBOMFoundationDataCorrectionHelper dataTransferHelper = new TruckBOMFoundationDataCorrectionHelper(this,
                                                                                                               FIELD_DBMH_REV_FROM,
                                                                                                               FIELD_DBMH_REV_TO,
                                                                                                               EDSImportHelper.EDS_AS_BIS_UNENDLICH);
        dataTransferHelper.addFields(FIELD_DBMH_KEM_FROM, FIELD_DBMH_KEM_TO);
        dataTransferHelper.addFields(FIELD_DBMH_RELEASE_FROM, FIELD_DBMH_RELEASE_TO);

        singleParts.forEach(singlePartObject -> {
            String partNo = singlePartObject.getIdentifier();
            // Falls keine Teilenummer existiert, brauchen wir nicht zu korrigieren
            if (StrUtils.isEmpty(partNo)) {
                return;
            }
            // Alle Stände zu einer Teilenummer laden (sortiert nach AS ab)
            iPartsDataBOMPartHistoryList bomPartHistoryList
                    = iPartsDataBOMPartHistoryList.loadBOMHistoryDataForPartNumber(getProject(), partNo, "");
            dataTransferHelper.correctDBDataRevisionChain(bomPartHistoryList);
        });
    }

    @Override
    protected Map<String, AbstractBOMXMLDataImporter> createSubImporter() {
        Map<String, AbstractBOMXMLDataImporter> importer = new HashMap<>();
        importer.put(TABLE_MAT, new BOMPartMasterDataUpdateImporter(getProject()));
        importer.put(TABLE_DA_BOM_MAT_HISTORY, new BOMPartHistoryUpdateImporter(getProject()));
        return importer;
    }

    @Override
    protected void clearCaches() {
        // Assembly-Caches löschen
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsDataChangedEventByEdit<>(iPartsDataChangedEventByEdit.DataType.PART_LIST,
                                                                                                  iPartsDataChangedEventByEdit.Action.MODIFIED,
                                                                                                  (AssemblyId)null, true));

    }

    /**
     * KeyValue-Reader für den Baukasten Historie Sub-Importer
     */
    private class PartHistoryKeyValueReader extends AbstractTruckBOMKeyValueJSONReaderWithKEMData {

        private final List<TruckBOMSinglePart> singleParts;


        public PartHistoryKeyValueReader(DWFile savedJSONFile, List<TruckBOMSinglePart> singleParts,
                                         Map<String, TruckBOMSingleKEM> kemFromForParts,
                                         Map<String, TruckBOMSingleKEM> kemToForParts,
                                         String tableName) {
            super(savedJSONFile, kemFromForParts, kemToForParts, singleParts.size(), tableName);
            this.singleParts = singleParts;
        }

        public List<TruckBOMSinglePart> getSingleParts() {
            return singleParts;
        }


        @Override
        protected List<RecordData> getNextRecordData() {
            if (!isCancelled()) {
                TruckBOMSinglePart singlePart = getSingleParts().get(getImportRecCount());
                String partNo = singlePart.getIdentifier();
                // Falls keine Teilenummer existiert, brauchen wir nicht importieren
                if (StrUtils.isEmpty(partNo)) {
                    return null;
                }
                List<RecordData> createdRecords = new ArrayList<>();
                List<TruckBOMPartVersion> allVersions = singlePart.getPartVersion();
                allVersions.sort(Comparator.comparing(TruckBOMPartVersion::getVersion));
                // Pro Version einen ImportRecord erzeugen
                allVersions.forEach(partVersion -> createdRecords.add(convertPartToImportRecord(partNo,
                                                                                                partVersion,
                                                                                                getKemFromForParts(),
                                                                                                getKemToForParts())));
                return createdRecords;
            }
            return null;
        }

        @Override
        protected void postProcessRecordData(RecordData record) {
            // hier ist nichts zu tun
        }

        @Override
        protected String getOriginalTableName() {
            // Beide Sub-Importer gehen auf die getT43RTEIL Tabelle
            return BOMPartMasterDataUpdateImporter.IMPORT_TABLENAME;
        }

    }

    /**
     * KeyValue-Reader für den Teilestamm Sub-Importer
     */
    private class PartKeyValueReader extends PartHistoryKeyValueReader {

        private final AbstractBOMXMLDataImporter importer;
        private final Map<String, Set<String>> assocIdToDistributionTask;

        public PartKeyValueReader(DWFile savedJSONFile, List<TruckBOMSinglePart> singleParts,
                                  Map<String, TruckBOMSingleKEM> kemFromForParts,
                                  Map<String, TruckBOMSingleKEM> kemToForParts,
                                  AbstractBOMXMLDataImporter importer, String tableName,
                                  Map<String, Set<String>> assocIdToDistributionTask) {
            super(savedJSONFile, singleParts, kemFromForParts, kemToForParts, tableName);
            this.importer = importer;
            this.assocIdToDistributionTask = assocIdToDistributionTask;
        }

        @Override
        protected List<RecordData> getNextRecordData() {
            if (!isCancelled()) {
                TruckBOMSinglePart singlePart = getSingleParts().get(getImportRecCount());
                String partNo = singlePart.getIdentifier();
                // Falls keine Teilenummer existiert, brauchen wir nicht importieren
                if (StrUtils.isEmpty(partNo)) {
                    return null;
                }
                // Nur den neuesten Datensatz verarbeiten, da in MAT keine unterschiedlichen Stände berücksichtigt werden
                Optional<TruckBOMPartVersion> newestPartVersion = singlePart.getNewestPartVersion();
                if (newestPartVersion.isPresent()) {
                    TruckBOMPartVersion partVersion = newestPartVersion.get();
                    RecordData recordData = convertPartToImportRecord(partNo, partVersion, getKemFromForParts(), getKemToForParts());
                    // Werksverteiler Daten setzen
                    addPlantSupplyIfExists(recordData, assocIdToDistributionTask, partVersion.getId(), BOMPartMasterDataUpdateImporter.TEIL_PLANTSUPPLIES,
                                           BOMPartMasterDataUpdateImporter.TEIL_PLANTSUPPLY);
                    List<RecordData> result = new ArrayList<>();
                    result.add(recordData);
                    return result;

                }
            }
            return null;
        }

        public AbstractBOMXMLDataImporter getImporter() {
            return importer;
        }

        @Override
        protected void postProcessRecordData(RecordData record) {
            // Texte setzen, sofern welche vorhanden sind
            Map<String, EtkMultiSprache> textsForRecord = new HashMap<>();
            addTextObjectIfNotNull(BOMPartMasterDataUpdateImporter.TEIL_DESCRIPTION, record.getDescription(), textsForRecord);
            addTextObjectIfNotNull(BOMPartMasterDataUpdateImporter.TEIL_REMARK, record.getRemark(), textsForRecord);
            importer.setTextsForMultiLangFields(textsForRecord);
        }
    }
}
