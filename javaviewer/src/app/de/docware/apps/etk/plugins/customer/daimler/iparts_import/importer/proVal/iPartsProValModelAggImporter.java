/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.proVal;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelsAggsList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsModelsAggsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.modelswithaggregates.iPartsProValAggDesignRef;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.modelswithaggregates.iPartsProValDesignNumber;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.modelswithaggregates.iPartsProValModelWithAggs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.proVal.models.modelswithaggregates.iPartsProValModelsWithAggsResponse;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractListComparerDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.JSONUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.file.DWFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ProVal Importer für die Baubarkeiten
 */
public class iPartsProValModelAggImporter extends AbstractListComparerDataImporter implements iPartsConst {

    private static final String TABLE_NAME = TABLE_DA_MODELS_AGGS;

    private String currentModel;    // Das aktuelle Baumuster
    int maxRecord;                  // Anzahl aller Baumuster
    int recordCount;                // Anzahl verarbeiteter Baumuster

    public iPartsProValModelAggImporter(EtkProject project) {
        super(project, TRUCK_BOM_FOUNDATION_PART_IMPORT_NAME, TABLE_NAME, false);
    }

    @Override
    protected void preImportTask() {
        setMaxEntriesForCommit(MIN_MEMORY_ROWS_LIMIT);
        progressMessageType = ProgressMessageType.READING;
        super.preImportTask();
    }

    /**
     * Startet den Import für den übergebene JSON Response
     *
     * @param response
     * @param messageLog
     */
    public void startJSONImportFromWSResponse(String response, DWFile jobLogFile, EtkMessageLog messageLog) {
        if (jobLogFile == null) {
            importJobRunning(); // ruft intern setLogFile() auf
        } else {
            setLogFile(jobLogFile, false);
        }
        if (!initImport(messageLog)) {
            cancelImport(translateForLog("!!Das Initialisieren des JSON Importers ist fehlgeschlagen. Import wird abgebrochen"));
            finishImport();
            return;
        }
        fireMessage("!!Starte den Import der JSON Nachricht");
        // Response importieren
        boolean result;
        preImportTask();
        result = importProValJSONResponse(response);
        postImportTask();
        // Logdatei nach dem Import verschieben
        boolean importFinishedOK = finishImport() && result;
        moveLogFile(importFinishedOK);
    }

    /**
     * Importiert die JSON Antwort der Webservices
     *
     * @param response
     * @return
     */
    private boolean importProValJSONResponse(String response) {
        Genson genson = JSONUtils.createGenson(true);
        try {
            // In das richtige DTO umwandeln
            iPartsProValModelsWithAggsResponse modelsWithAggsResponse = genson.deserialize(response, iPartsProValModelsWithAggsResponse.class);
            // Map mit Aggregate Id (DesignNumber) auf das Aggregat
            Map<String, String> idToAggShortName = modelsWithAggsResponse.getDesignnumbers().stream()
                    .collect(Collectors.toMap(iPartsProValDesignNumber::getId, iPartsProValDesignNumber::getShortName,
                                              (firstValue, nextValue) -> firstValue));
            if (!idToAggShortName.isEmpty()) {
                // Jetzt jedes Baumuster durchgehen und jede Beziehung prüfen
                List<iPartsProValModelWithAggs> models = modelsWithAggsResponse.getModels();
                maxRecord = models.size();
                models.forEach(model -> {
                    if (cancelled) {
                        return;
                    }
                    // Präfix setzen
                    String modelName = addPrefixIfNotExists(model.getVehicleDesignNumber().getShortName(), MODEL_NUMBER_PREFIX_CAR);
                    currentModel = modelName;
                    // Alle Aggregate Referenzen zum BM bestimmen
                    List<iPartsProValAggDesignRef> aggRefs = model.getAggregateDesignRefs();
                    if ((aggRefs != null) && !aggRefs.isEmpty()) {
                        // Alle Einträge für das C Baumuster bestimmen
                        int totalDBCount = getTotalDBCountForModel(modelName);
                        // Compare Objekt für dieses Baumuster erzeugen
                        createComparer(100, 500, totalDBCount, true, false, false);
                        // Daten in das Compare Objekt laden, die in der DB vorhanden sind
                        loadExistingDataFromDB();
                        clearEndMessageList(); // Damit keine unnötigen Texte gehalten werden
                        VarParam<Integer> insertedCount = new VarParam<>(0); // Kenner für Datensätze bei einer Erstbefüllung für das Baumuster
                        // Alle Aggregate BM durchlaufen und die Referenz zum C-Baumuster prüfen
                        aggRefs.forEach(aggregateModel -> {
                            if (cancelled) {
                                return;
                            }
                            // Hole zur ID das Baumuster
                            String aggModelWithPrefix = idToAggShortName.get(aggregateModel.getId());
                            if (StrUtils.isValid(aggModelWithPrefix)) {
                                // Präfix setzen
                                aggModelWithPrefix = addPrefixIfNotExists(aggModelWithPrefix, MODEL_NUMBER_PREFIX_AGGREGATE);
                                iPartsModelsAggsId modelsAggsId = new iPartsModelsAggsId(currentModel, aggModelWithPrefix);
                                if (totalDBCount > 0) {
                                    // Sind zum C-Baumuster Daten vorhanden, dann lege die Referenzen in den Vergleicher
                                    putSecond(modelsAggsId.toDBString(), iPartsImportDataOrigin.PROVAL.getOrigin());
                                } else {
                                    // Hier handelt es sich um die Erstbefüllung (in der DB sind keine Records vorhanden)
                                    iPartsDataModelsAggs dataModelsAggs = new iPartsDataModelsAggs(getProject(), modelsAggsId);
                                    // Da Erstbefüllung und weil die Id geprüft wird, reicht es hier einfach ein init zu machen ohne existsInDB()
                                    dataModelsAggs.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                                    dataModelsAggs.setFieldValue(FIELD_DMA_SOURCE, iPartsImportDataOrigin.PROVAL.getOrigin(), DBActionOrigin.FROM_EDIT);
                                    if (doSaveToDB(dataModelsAggs)) {
                                        insertedCount.setValue(insertedCount.getValue() + 1);
                                    }
                                }
                            }
                        });
                        fireMessageToFile("!!Für das Baumuster \"%1\" wurden %2 Referenzen verarbeitet", modelName,
                                          String.valueOf(aggRefs.size()));
                        if (!cancelled) {
                            // Gab es initial Daten in der DB -> Vergleich fahren und nur die neuen übernehmen. Gab es
                            // keine Daten, dann wurden die Objekte weiter oben importiert
                            if (totalDBCount > 0) {
                                compareAndSaveData(false);
                            } else {
                                fireMessageToFile("!!Für das Baumuster \"%1\" wurden \"%2\" Datensätze importiert",
                                                  modelName, String.valueOf(insertedCount));
                            }
                        }
                        // Vergleicher für das aktuelle BM aufräumen
                        if (totalDBCount > 0) {
                            cleanup();
                        }
                    } else {
                        fireMessage("!!Für das Baumuster \"%1\" wurden keine Aggregat-Referenzen geliefert!", modelName);
                    }
                    recordCount++;
                    doUpdateProgress();
                });
            }
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireError("!!Fehler beim Importieren der Baubarkeit aus ProVal");
            return false;
        }
        return true;
    }

    private String addPrefixIfNotExists(String model, String prefix) {
        if (!model.startsWith(prefix)) {
            return prefix + model;
        }
        return model;
    }

    @Override
    protected void hideProgress() {
        // ProgressBar nicht verschwinden lassen
    }

    @Override
    protected void fireStandardMessage(String message) {
        // Die Standard-Nachrichten nicht abfeuern, da hier pro Baumuster verglichen wird
    }

    private void doUpdateProgress() {
        // Bei dem Update ist es nur wichtig, wieviele C Baumuster verarbeitet wurden. Da wir den Vergleicher pro BM
        // aufrufen, muss hier jede BM Progress Änderung unterbunden werden
        super.updateProgress(recordCount, maxRecord);
    }

    @Override
    protected void updateProgress(int recordNo, int maxRecord) {
        doUpdateProgress();
    }

    @Override
    protected boolean deleteEntriesFromListComp(SaveCounterContainer counterContainer) {
        // Es soll nichts gelöscht werden
        return true;
    }

    @Override
    protected boolean updateEntriesFromListComp(SaveCounterContainer counterContainer) {
        // Es soll nichts aktualisiert werden (Bis auf Quelle gibts aber auch kein anderes Feld)
        return true;
    }

    @Override
    protected void postImportTask() {
        if (!cancelled) {
            setClearCachesAfterImport(true);
        }
        cleanup();
        super.postImportTask();
    }

    /**
     * Liefert die Anzahlt an Datensätzen in der DB zum übergebenen Baumuster
     *
     * @param modelName
     * @return
     */
    private int getTotalDBCountForModel(String modelName) {
        return getTotalDBCountForCondition(FIELD_DMA_MODEL_NO, modelName);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        return false;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {

    }

    @Override
    protected EtkDataObject buildDataFromAttributes(DBDataObjectAttributes attributes) {
        iPartsDataModelsAggs data = new iPartsDataModelsAggs(getProject(), null);
        data.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
        return data;
    }

    @Override
    protected String getValuesForListComp(EtkDataObject data) {
        if (data instanceof iPartsDataModelsAggs) {
            return ((iPartsDataModelsAggs)data).getSource();
        }
        return null;
    }

    @Override
    protected EtkDataObjectList<? extends EtkDataObject> getDataListForCompare() {
        return new iPartsDataModelsAggsList();
    }

    @Override
    protected String[] getWhereFieldsForLoadExistingData() {
        return new String[]{ FIELD_DMA_MODEL_NO };
    }

    @Override
    protected String[] getWhereValuesForLoadExistingData() {
        return new String[]{ currentModel };
    }

    @Override
    protected EtkDataObject buildDataFromEntry(DiskMappedKeyValueEntry entry) {
        // Hier wird für alle drei Operationen (Insert, Delete, Update) aus einem Schlüssel/Wert Paar (DiskMappedKeyValueEntry)
        // das richtige DB-Objekt erzeugt
        iPartsModelsAggsId modelsAggsId = iPartsModelsAggsId.getFromDBString(entry.getKey());
        if (modelsAggsId != null) {
            return new iPartsDataModelsAggs(getProject(), modelsAggsId);
        }
        return null;
    }

    @Override
    protected void addDataFromMappedKeyValueEntry(DiskMappedKeyValueEntry entry, EtkDataObject data) {
        // Hier werden dem in buildDataFromEntry() erzeugten Objekt die Nutzdaten gesetzt. Ebenfalls für alle drei Operationen
        // und aus dem übergebenen Schlüssel/Wert Paar (DiskMappedKeyValueEntry)
        if (data instanceof iPartsDataModelsAggs) {
            iPartsDataModelsAggs dataModelsAggs = (iPartsDataModelsAggs)data;
            if (!dataModelsAggs.existsInDB()) {
                dataModelsAggs.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            String source = entry.getValue();
            dataModelsAggs.setFieldValue(FIELD_DMA_SOURCE, source, DBActionOrigin.FROM_EDIT);
        }
    }
}
