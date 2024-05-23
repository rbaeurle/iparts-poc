/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkMessageLog;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMAssociatedData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMBaseData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMSingleKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportDateHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLTableDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractJSONDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlNode;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.os.OsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Abstrakte Klasse für alle TruckBOM.foundation Importer, die JSON Dateien importieren
 */
public abstract class AbstractTruckBOMFoundationJSONImporter extends AbstractJSONDataImporter {

    private static final String JSON_LANG_CODE_FOR_DE = "0";
    protected static final String EMPTY_STATUS_VALUE = "__";

    // Ein JSON Response kann von mehreren bestehenden EDS/BCS Importer verarbeitet werden. Diese Importer werden hier
    // verwaltet
    private Map<String, AbstractBOMXMLDataImporter> subImporters;
    private DWFile savedJSONFile;
    private String tableName;

    public AbstractTruckBOMFoundationJSONImporter(EtkProject project, String importName, String tableName) {
        super(project, importName, tableName);
        this.tableName = tableName;
    }

    /**
     * Startet den Import für den übergebene JSON Response
     *
     * @param response
     */
    public void startJSONImportFromTruckBOMResponse(String response, DWFile jobLogFile) {
        if (jobLogFile == null) {
            importJobRunning(); // ruft intern setLogFile() auf
        } else {
            setLogFile(jobLogFile, false);
        }
        if (!initImport(response)) {
            cancelImport(translateForLog("!!Das Initialisieren des JSON Importers ist fehlgeschlagen. Import wird abgebrochen"));
            finishImport();
            return;
        }
        // Sub-Importer erzeugen lassen
        subImporters = createSubImporter();
        fireMessage("!!Starte den Import der JSON Nachricht");
        // Response importieren
        boolean result = false;
        // Check, ob der Dateiname gültig ist
        if (checkFileName()) {
            preImportTask();
            result = importJSONResponse(response);
            postImportTask();
        }
        // Import- und Logdatei nach dem Import verschieben
        boolean importFinishedOK = finishImport() && result;
        moveInputAndLogFileAfterImport(savedJSONFile, importFinishedOK, "JSON", (jobLogFile != null));
    }

    /**
     * Überprüft, ob man aus dem Importnamen einen gültigen Dateinamen erzeugen kann
     *
     * @return
     */
    private boolean checkFileName() {
        String fileName = getImportName(getProject().getDBLanguage());
        return !StrUtils.isEmpty(fileName);
    }


    /**
     * Initialisiert den JSON Importer
     *
     * @param response
     * @return
     */
    public boolean initImport(String response) {
        // Erst das "normale" initImport() aufrufen
        if (super.initImport(new EtkMessageLog())) {
            // Hier den Hauptpfad für die JSON Importe setzen
            setDirForSourceFiles(iPartsImportPlugin.getPluginConfig().getConfigValueAsDWFile(iPartsImportPlugin.CONFIG_IMPORT_FILE_DIR).
                                         getChild("TruckBOMFoundation" + OsUtils.FILESEPARATOR + iPartsConst.SUBDIR_RUNNING), "", false);
            // Die eingelesene JSON Nachricht in das "running" Verzeichnis speichern
            return saveRunningJSONFile(response);
        }
        return false;
    }


    /**
     * Speichert die übergebene JSON Nachricht als Datei ab
     *
     * @param response
     * @return
     */
    private boolean saveRunningJSONFile(String response) {
        DWFile directory = getDirForSrcFiles();
        if (directory != null) {
            // Verzeichnis anlegen
            if (!directory.exists()) {
                directory.mkDirsWithRepeat();
            }
            if ((response != null) && directory.exists()) {
                // Zeitstempel und den Importnamen als Dateinamen verwenden (wir bekommen ja nur den String und haben keine "Originaldatei")
                String prefixForFile = XMLImportExportDateHelper.getFormattedDateTimeForMessageCopies();
                String importName = DWFile.convertToValidFileName(getImportName(getProject().getDBLanguage()));
                savedJSONFile = directory.getChild(prefixForFile + "_" + importName + "." + MimeTypes.EXTENSION_JSON);
                int i = 0;
                while (savedJSONFile.exists()) {
                    String newPrefix = prefixForFile + "_" + i;
                    savedJSONFile = directory.getChild(newPrefix + "_" + importName + "." + MimeTypes.EXTENSION_JSON);
                    i++;
                }
                try {
                    savedJSONFile.writeTextFile(response.getBytes(DWFileCoding.UTF8.getJavaCharsetName()));
                    return true;
                } catch (IOException e) {
                    Logger.getLogger().handleRuntimeException(e);
                }
            }
        } else {
            Logger.log(LogChannels.APPLICATION, LogType.DEBUG, "No save directory for import selected.");
        }
        return false;
    }

    /**
     * Startet den zum Schlüssel <code>keyForSubImporter</code> gehörenden Sub-Importer
     */
    protected boolean startSubImporter(String keyForSubImporter, AbstractKeyValueRecordReader keyValueRecordReader) {
        if (isCancelled()) {
            return false;
        }
        AbstractBOMXMLDataImporter subImporter = getSubImporters().get(keyForSubImporter);
        if (subImporter == null) {
            fireWarningLF("!!Zum Schlüssel \"%1\" konnte kein Importer gefunden werden.", keyForSubImporter);
            return false;
        }
        getMessageLog().fireMessage("");
        fireSubImporterStarted(subImporter);
        getMessageLog().fireMessage("");
        // Caches werden nur beim Haupt-Importer gelöscht (JSON Importer)
        subImporter.setClearCachesAfterImport(false);
        subImporter.startImportWithExternalReader(keyValueRecordReader, getLogFile());
        getMessageLog().fireMessage("");
        fireSubImporterEnded(subImporter);
        getMessageLog().fireMessage("");
        return !subImporter.isCancelled();
    }

    protected void addValueIfExists(String xmlElement, String value, RecordData importRecord) {
        if (StrUtils.isValid(xmlElement, value)) {
            importRecord.put(xmlElement, value);
        }
    }

    /**
     * Setzt optionale boolean-Werte
     *
     * @param xmlName
     * @param value
     * @param importRecord
     */
    protected void handleOptionalBooleanValue(String xmlName, String value, RecordData importRecord) {
        if (StrUtils.isValid(xmlName, value)) {
            String importValue = Boolean.getBoolean(value) ? EDSImportHelper.BOM_VALUE_TRUE : EDSImportHelper.BOM_VALUE_FALSE;
            addValueIfExists(xmlName, importValue, importRecord);
        }
    }

    private void fireSubImporterStarted(AbstractBOMXMLDataImporter subImporter) {
        String key = "!!+++++++++++++Starte Subimporter \"%1\"+++++++++++++";
        fireMessageLFnoDate(key, subImporter.getImportName(getLogLanguage()));
    }

    private void fireSubImporterEnded(AbstractBOMXMLDataImporter subImporter) {
        String key = "!!+++++++++++++Subimporter \"%1\" beendet+++++++++++++";
        fireMessageLFnoDate(key, subImporter.getImportName(getLogLanguage()));
    }

    public Map<String, AbstractBOMXMLDataImporter> getSubImporters() {
        return subImporters;
    }

    protected DWFile getSavedJSONFile() {
        return savedJSONFile;
    }

    protected void logSkipImport(String tableName) {
        fireWarningLF("!!Der Import für die Tabelle \"%1\" kann nicht durchgeführt werden und wird übersprungen!", tableName);
    }

    /**
     * Setzt die verknüpften KEM Daten
     *
     * @param recordData
     * @param id
     * @param kemsForParts
     */
    protected void addKEMData(RecordData recordData, String id, Map<String, TruckBOMSingleKEM> kemsForParts,
                              String ecoFromField, String vakzFromField, String releaseFromField) {
        if (StrUtils.isEmpty(id)) {
            return;
        }
        TruckBOMSingleKEM kemFromData = kemsForParts.get(id);
        if (kemFromData != null) {
            addValueIfExists(ecoFromField, kemFromData.getIdentifier(), recordData);
            String statusValue = kemFromData.getStatus();
            statusValue = StrUtils.replaceSubstring(statusValue, EMPTY_STATUS_VALUE, "");
            addValueIfExists(vakzFromField, statusValue, recordData);
            addValueIfExists(releaseFromField, kemFromData.getReleaseDate(), recordData);
        }
    }

    /**
     * Setzt die Texte ins ImportRecord.
     *
     * @param importRecord
     * @param textDataObject
     */
    protected void addTexts(RecordData importRecord, TruckBOMMultiLangData textDataObject) {
        // Konstruktionsbenennung setzen
        importRecord.setDescription(textDataObject.getNomenclatureAsMultiLangObject());
        // Bemerkung setzen
        importRecord.setRemark(textDataObject.getRemarkAsMultiLangObject());
    }

    /**
     * Setzt die Tags PlantSupply und PlantSupplies ins ImportRecord, sodass die Importer es
     * später interpretieren können
     *
     * @param importRecord              Anzupassender ImportRecord
     * @param assocIdToDistributionTask Die Werksverteiler, die jeweils ein PlantSupply darstellen
     * @param assocIdFrom               Id um festzustellen welche Werksverteiler zu welchen Datensatz gehören
     * @param plantSuppliesTagName      TagName für Importer
     * @param plantSupplyTagName        TagName für Importer
     */
    protected void addPlantSupplyIfExists(RecordData importRecord, Map<String, Set<String>> assocIdToDistributionTask,
                                          String assocIdFrom, String plantSuppliesTagName, String plantSupplyTagName) {
        if ((assocIdToDistributionTask != null) && !assocIdToDistributionTask.isEmpty()) {
            Set<String> distributionTasks = assocIdToDistributionTask.get(assocIdFrom);
            if ((distributionTasks != null) && !distributionTasks.isEmpty()) {
                iPartsXMLTableDataset xmlTableDataset = new iPartsXMLTableDataset(tableName, (DwXmlNode)null);
                xmlTableDataset.addTagAndValue(plantSuppliesTagName, iPartsXMLTableDataset.SUB_DATASETS, new ArrayList<>());
                for (String distributionTask : distributionTasks) {
                    xmlTableDataset.addTagAndValue(plantSupplyTagName, distributionTask, new ArrayList<>());
                }
                importRecord.getRecord().putAll(xmlTableDataset.getTagsAndValues());
            }
        }
    }

    /**
     * Überprüft, ob das übergebene {@link TruckBOMAssociatedData} Objekt Verknüpfungen zu Werksverteiler Daten besitzt.
     * Falls ja, werden diese zurückgegeben.
     *
     * @param associatedData
     * @param fileName
     * @return
     */
    protected Map<String, Set<String>> checkDistributionTasksData(TruckBOMAssociatedData associatedData, String fileName) {
        // Verknüpfung Werksverteiler zu Association Daten
        Map<String, Set<String>> assocIdToDistributionTasks = associatedData.getAssociationFromIDsToDistributionTaskDataMap();
        if ((assocIdToDistributionTasks == null) || assocIdToDistributionTasks.isEmpty()) {
            fireMessageLF("!!Die Importdatei \"%1\" enthält keine Informationen zum Werksverteiler.", fileName);
        }
        return assocIdToDistributionTasks;
    }

    /**
     * Setzt die vom BOM-DB Importer benötigten Meta-Daten bezüglich mehrsprachigen Texten
     *
     * @param importRecord
     * @param langDataField
     * @param langAttribute
     * @param language
     */
    protected void addLangDataMetaInfo(RecordData importRecord, String langDataField, String langAttribute, iPartsEDSLanguageDefs language) {
        addValueIfExists(langDataField, iPartsXMLTableDataset.SUB_DATASETS, importRecord);
        importRecord.put(iPartsXMLTableDataset.createAttributeValue(langDataField, langAttribute),
                         language.getXmlValue());
    }

    protected Map<String, TruckBOMSingleKEM> checkKEMFromData(TruckBOMAssociatedData baseKEMDataObject, String fileName) {
        Map<String, TruckBOMSingleKEM> idsToKemFromData = baseKEMDataObject.getAssociationFromIDsToKEMFromDataMap();
        // KEM ab Daten müssen vorhanden sein, daher Abbruch, wenn sie fehlen
        if ((idsToKemFromData == null) || idsToKemFromData.isEmpty()) {
            fireErrorLF("!!Die Importdatei \"%1\" enthält keine KEM ab Daten. Import wird abgebrochen", fileName);
            return null;
        }
        return idsToKemFromData;
    }

    protected Map<String, TruckBOMSingleKEM> checkKEMToData(TruckBOMAssociatedData baseKEMDataObject, String fileName) {
        Map<String, TruckBOMSingleKEM> idsToKemToData = baseKEMDataObject.getAssociationToIDsToKEMToDataMap();
        // KEM bis Daten sind optional. Kein abbruch, wenn sie fehlen
        if ((idsToKemToData == null) || idsToKemToData.isEmpty()) {
            fireMessageLF("!!Die Importdatei \"%1\" enthält keine KEM bis Daten", fileName);
        }
        return idsToKemToData;
    }

    @Override
    public int getErrorCount() {
        int errorsSubImporter = getSubImporters()
                .values()
                .stream()
                .map(AbstractGenericImporter::getErrorCount)
                .reduce(0, Integer::sum);
        return super.getErrorCount() + errorsSubImporter;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        return true;
    }

    @Override
    public void cancelImport(String message, MessageLogType messageLogType) {
        super.cancelImport(message, messageLogType);
        // Alle Sub-Importer canceln
        getSubImporters().values().forEach(subImporter -> cancelImport());
    }

    protected boolean checkObjectCount(TruckBOMBaseData baseData) {
        if (baseData.getObjectCount() <= 0) {
            fireMessage("!!Die Antwort enthält keine Daten für den angegeben Zeitraum!");
            return false;
        }
        return true;
    }

    /**
     * Erzeugt die benötigten Sub-Importer
     *
     * @return
     */
    protected abstract Map<String, AbstractBOMXMLDataImporter> createSubImporter();

    /**
     * Importiert den übergebenen JSON Response
     *
     * @param response
     * @return
     */
    protected abstract boolean importJSONResponse(String response);
}
