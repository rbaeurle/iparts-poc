/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaaHistoryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMSingleKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartslist.TruckBOMPartsListData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartslist.TruckBOMPartsListVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartslist.TruckBOMSinglePartsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.helper.TruckBOMFoundationDataCorrectionHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.BCSMasterDataSaaUpdateImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.EDSSaaMasterDataUpdateImporter;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Importer für die SAA-Stammdaten aus TruckBOM.foundation
 */
public class TruckBOMPartsListImporter extends AbstractTruckBOMFoundationJSONImporter {

    public TruckBOMPartsListImporter(EtkProject project) {
        super(project, TRUCK_BOM_FOUNDATION_PARTS_LIST_IMPORT_NAME, TABLE_DA_SAA + " & " + TABLE_DA_SAA_HISTORY);
    }

    /**
     * Konvertiert ein {@link TruckBOMSinglePartsList} Objekt in einen ImportRecord (Map) für den
     * {@link BCSMasterDataSaaUpdateImporter} und den {@link EDSSaaMasterDataUpdateImporter}
     *
     * @param saaNumber
     * @param singlePartsListVersion
     * @param kemFromForParts
     * @param kemToForParts
     * @return
     */
    private RecordData convertPartsListToImportRecord(String saaNumber, TruckBOMPartsListVersion singlePartsListVersion,
                                                      Map<String, TruckBOMSingleKEM> kemFromForParts,
                                                      Map<String, TruckBOMSingleKEM> kemToForParts) {
        RecordData recordData = new RecordData();
        // SAA setzen
        addValueIfExists(BCSMasterDataSaaUpdateImporter.SAA_PARTLIST_NUMBER, saaNumber, recordData);
        // Die "einfachen" String basierten Daten befüllen
        addPlainData(recordData, singlePartsListVersion);
        // Texte setzen (Description und Remark)
        addTexts(recordData, singlePartsListVersion);
        // KEM ab Daten setzen
        addKEMData(recordData, singlePartsListVersion.getId(), kemFromForParts, EDSSaaMasterDataUpdateImporter.SAAE_ECO_FROM,
                   EDSSaaMasterDataUpdateImporter.SAAE_VAKZ_AB, EDSSaaMasterDataUpdateImporter.SAAE_RELEASE_FROM);
        // KEM bis Daten setzen
        addKEMData(recordData, singlePartsListVersion.getId(), kemToForParts, EDSSaaMasterDataUpdateImporter.SAAE_ECO_TO,
                   EDSSaaMasterDataUpdateImporter.SAAE_VAKZ_BIS, EDSSaaMasterDataUpdateImporter.SAAE_RELEASE_TO);

        return recordData;
    }

    private void addPlainData(RecordData recordData, TruckBOMPartsListVersion saaData) {
        addValueIfExists(BCSMasterDataSaaUpdateImporter.SAA_AS_AB, saaData.getVersion(), recordData);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected Map<String, AbstractBOMXMLDataImporter> createSubImporter() {
        Map<String, AbstractBOMXMLDataImporter> importer = new HashMap<>();
        importer.put(TABLE_DA_SAA, new BCSMasterDataSaaUpdateImporter(getProject()));
        importer.put(TABLE_DA_SAA_HISTORY, new EDSSaaMasterDataUpdateImporter(getProject()));
        return importer;
    }

    @Override
    protected boolean importJSONResponse(String response) {
        String fileName = getImportName(getProject().getDBLanguage());
        Genson genson = JSONUtils.createGenson(true);
        try {
            TruckBOMPartsListData truckBOMPartsListData = deserializeFromString(genson, response, fileName, TruckBOMPartsListData.class);
            if (truckBOMPartsListData == null) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine gültigen Werte.", fileName);
                return false;
            }
            if (!checkObjectCount(truckBOMPartsListData)) {
                return true;
            }
            // Alle SAA-Stammdaten
            List<TruckBOMSinglePartsList> singlePartsList = truckBOMPartsListData.getPartsList();
            if ((singlePartsList == null) || singlePartsList.isEmpty()) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine SAA-Stammdaten.", fileName);
                return false;
            }

            // Verknüpfung SAA zu KEM ab Daten
            Map<String, TruckBOMSingleKEM> idsToKemFromData = checkKEMFromData(truckBOMPartsListData, fileName);
            if (idsToKemFromData == null) {
                return false;
            }
            // Verknüpfung SAA zu KEM bis Daten
            Map<String, TruckBOMSingleKEM> idsToKemToData = checkKEMToData(truckBOMPartsListData, fileName);
            // Verknüpfungen zu Werksverteiler Daten
            Map<String, Set<String>> assocIdToDistributionTask = checkDistributionTasksData(truckBOMPartsListData, fileName);
            // SubImporter starten
            SaaKeyValueReader saaKeyValueReader = new SaaKeyValueReader(getSavedJSONFile(), singlePartsList,
                                                                        idsToKemFromData, idsToKemToData,
                                                                        getSubImporters().get(TABLE_DA_SAA));
            if (!startSubImporter(TABLE_DA_SAA, saaKeyValueReader)) {
                logSkipImport(TABLE_DA_SAA);
            }

            SaaHistoryKeyValueReader saaHistoryKeyValueReader = new SaaHistoryKeyValueReader(getSavedJSONFile(),
                                                                                             singlePartsList,
                                                                                             idsToKemFromData,
                                                                                             idsToKemToData,
                                                                                             assocIdToDistributionTask);
            if (startSubImporter(TABLE_DA_SAA_HISTORY, saaHistoryKeyValueReader)) {
                // Die Verknüpfungen zwischen alten und neuen Daten neu setzen (KEM bis, AS bis und Datum bis)
                updateSaaHistoryData(singlePartsList);
            } else {
                logSkipImport(TABLE_DA_SAA_HISTORY);
            }

        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireErrorLF("!!Fehler beim Importieren der SAA-Stammdaten aus TruckBOM.foundation");
            return false;
        }
        return true;
    }


    /**
     * Aktualisiert die Verknüpfung zwischen bestehenden und importierten Daten (KEM bis, AS bis und Datum bis)
     *
     * @param singlePartsList
     */
    private void updateSaaHistoryData(List<TruckBOMSinglePartsList> singlePartsList) {
        TruckBOMFoundationDataCorrectionHelper dataTransferHelper = new TruckBOMFoundationDataCorrectionHelper(this,
                                                                                                               FIELD_DSH_REV_FROM,
                                                                                                               FIELD_DSH_REV_TO,
                                                                                                               EDSImportHelper.EDS_AS_BIS_UNENDLICH);
        dataTransferHelper.addFields(FIELD_DSH_KEM_FROM, FIELD_DSH_KEM_TO);
        dataTransferHelper.addFields(FIELD_DSH_RELEASE_FROM, FIELD_DSH_RELEASE_TO);

        singlePartsList.forEach(singlePartsListObject -> {
            String saaNumber = singlePartsListObject.getIdentifier();
            // Falls keine SAA existiert, brauchen wir nicht zu korrigieren
            if (StrUtils.isEmpty(saaNumber)) {
                return;
            }
            // Alle Stände zu einer SAA laden (sortiert nach AS ab)
            iPartsDataSaaHistoryList saaHistoryList = iPartsDataSaaHistoryList.loadSaaHistoryDataForSaa(getProject(), saaNumber);
            dataTransferHelper.correctDBDataRevisionChain(saaHistoryList);
        });
    }

    /**
     * KeyValue-Reader für den SAA-Stamm Sub-Importer
     */
    private class SaaKeyValueReader extends AbstractTruckBOMKeyValueJSONReaderWithKEMData {

        private final List<TruckBOMSinglePartsList> singlePartsList;
        private final AbstractBOMXMLDataImporter importer;

        public SaaKeyValueReader(DWFile savedJSONFile, List<TruckBOMSinglePartsList> singlePartsList,
                                 Map<String, TruckBOMSingleKEM> kemFromForParts, Map<String, TruckBOMSingleKEM> kemToForParts,
                                 AbstractBOMXMLDataImporter importer) {
            super(savedJSONFile, kemFromForParts, kemToForParts, singlePartsList.size(), TABLE_DA_SAA);
            this.singlePartsList = singlePartsList;
            this.importer = importer;
        }

        @Override
        protected List<RecordData> getNextRecordData() {
            if (!isCancelled()) {
                TruckBOMSinglePartsList singlePartsListObject = singlePartsList.get(getImportRecCount());
                String saaNumber = singlePartsListObject.getIdentifier();
                // Falls keine SAA existiert, brauchen wir nicht importieren
                if (StrUtils.isEmpty(saaNumber)) {
                    return null;
                }
                // Nur den neuesten Datensatz verarbeiten, da in DA_SAA keine unterschiedlichen Stände berücksichtigt werden
                Optional<TruckBOMPartsListVersion> newestPartsListVersion = singlePartsListObject.getNewestPartsListVersion();
                if (newestPartsListVersion.isPresent()) {
                    RecordData recordData = convertPartsListToImportRecord(saaNumber, newestPartsListVersion.get(),
                                                                           getKemFromForParts(), getKemToForParts());
                    List<RecordData> result = new ArrayList<>();
                    result.add(recordData);
                    return result;

                }
            }
            return null;
        }

        @Override
        protected void postProcessRecordData(RecordData record) {
            // Texte setzen sofern welche vorhanden sind
            Map<String, EtkMultiSprache> textsForRecord = new HashMap<>();
            addTextObjectIfNotNull(BCSMasterDataSaaUpdateImporter.SAA_DESCRIPTION, record.getDescription(), textsForRecord);
            addTextObjectIfNotNull(BCSMasterDataSaaUpdateImporter.SAA_REMARK, record.getRemark(), textsForRecord);
            importer.setTextsForMultiLangFields(textsForRecord);
        }

        @Override
        protected String getOriginalTableName() {
            return BCSMasterDataSaaUpdateImporter.IMPORT_TABLENAME;
        }
    }

    /**
     * KeyValue-Reader für den SAA-Änderungsstand Sub-Importer
     */
    private class SaaHistoryKeyValueReader extends AbstractTruckBOMKeyValueJSONReaderWithKEMData {

        private final List<TruckBOMSinglePartsList> singlePartsList;
        private final Map<String, Set<String>> assocIdToDistributionTask;

        public SaaHistoryKeyValueReader(DWFile savedJSONFile, List<TruckBOMSinglePartsList> singlePartsList,
                                        Map<String, TruckBOMSingleKEM> kemFromForParts, Map<String, TruckBOMSingleKEM> kemToForParts,
                                        Map<String, Set<String>> assocIdToDistributionTask) {
            super(savedJSONFile, kemFromForParts, kemToForParts, singlePartsList.size(), TABLE_DA_SAA_HISTORY);
            this.singlePartsList = singlePartsList;
            this.assocIdToDistributionTask = assocIdToDistributionTask;
        }

        @Override
        protected List<RecordData> getNextRecordData() {
            if (!isCancelled()) {
                // Den nächsten Datensatz bestimmen und den Zähler um eins erhöhen
                TruckBOMSinglePartsList singlePartsListObject = singlePartsList.get(getImportRecCount());
                String saaNumber = singlePartsListObject.getIdentifier();
                // Falls keine SAA existiert, brauchen wir nicht importieren
                if (StrUtils.isEmpty(saaNumber)) {
                    return null;
                }
                List<RecordData> createdRecords = new ArrayList<>();
                List<TruckBOMPartsListVersion> singlePartsListVersions = singlePartsListObject.getPartsListVersion();
                singlePartsListVersions.sort(Comparator.comparing(TruckBOMPartsListVersion::getVersion));
                singlePartsListVersions.forEach(singlePartsListVersion -> {
                    RecordData recordData = convertPartsListToImportRecord(saaNumber, singlePartsListVersion,
                                                                           getKemFromForParts(), getKemToForParts());
                    // Werksverteiler setzen
                    addPlantSupplyIfExists(recordData, assocIdToDistributionTask, singlePartsListVersion.getId(),
                                           EDSSaaMasterDataUpdateImporter.SAAE_PLANTSUPPLIES,
                                           EDSSaaMasterDataUpdateImporter.SAAE_PLANTSUPPLY);
                    createdRecords.add(recordData);
                });
                return createdRecords;
            }
            return null;
        }

        @Override
        protected void postProcessRecordData(RecordData record) {

        }

        @Override
        protected String getOriginalTableName() {
            return EDSSaaMasterDataUpdateImporter.IMPORT_TABLENAME;
        }
    }
}
