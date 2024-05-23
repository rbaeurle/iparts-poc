package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.ppua;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.KeyValueRecordCSVFileReader;
import de.docware.apps.etk.base.importer.base.model.KeyValueRecordGzCSVFileReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPPUA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPPUAList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPPUAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractListComparerDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.VarParam;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.collections.diskmappedlist.compare.DiskMappedKeyValueListCompare;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Alternativer Importer für {@link PPUAImporter}
 * In der Importdatei sind ca 15Mio Datensätze enthalten, die in der Datenbank 15Min * 28 Datensätze erzeugen
 * 28 sind dabei die Anzahl der Jahre-Spalten (1995 - 2022)
 * Um die Anzahl der Werte für den {@link DiskMappedKeyValueListCompare} zu verkleinern,
 * wird als Key weiterhin die PK-Werte OHNE das Feld DA_PPUA_YEAR benutzt
 * als Value wird aus den Jahr- und Jahrwerten ein String zusammengebaut (1995=wert&1996=wert&...&2022=wert)
 * Hierbei muss sowohl bei putFirst() (vorhandene Daten aus der DB), ebenso bei putSecond() (Werte aus der Importdatei)
 * und bei der Auswertung anders vorgegangen werden.
 *
 * Durch die Datenreduktion ist der AlternateImporter im Schnitt 40-50% schneller
 * neu beim Einfügen einer neuen Jahr-Spalte ist er um ca 9% langsamer
 * (Grundlage für die Messungen: 100.000 Datensätze in der Importdatei)
 */
public class PPUAAlternateImporter extends AbstractListComparerDataImporter {

    private static final String PARTNO = "Partnr";
    private static final String REGION = "Region";
    private static final String ENTITY = "Entity";
    private static final String VEHBR = "VehBR";
    private static final String TYPE = "Type";

    private static final String YEAR_VALUE_DELIMITER = "=";
    private static final String KEY_PPUA_YEARS_LAST_IMPORT = "iParts_ppua_years_last_import";

    private static final String[] MUST_HEADERNAMES = new String[]{ PARTNO, REGION, ENTITY, VEHBR, TYPE };

    private Set<String> yearHeaderSet;
    private int insertedCount;
    private GenericEtkDataObjectList extraDeleteList; // extra Liste bei dontImportEmptyValues = true zum Löschen von Elementen

    private boolean dontImportEmptyValues = true;  // true: leere Jahrwerte werden nicht importiert

    public PPUAAlternateImporter(EtkProject project) {
        super(project, iPartsConst.PPUA_POTENTIAL_USAGE_ANALYSIS, iPartsConst.TABLE_DA_PPUA, true,
              new FilesImporterFileListType(iPartsConst.TABLE_DA_PPUA, iPartsConst.PPUA_POTENTIAL_USAGE_ANALYSIS, true,
                                            false, false,
                                            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
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
    protected void preImportTask() {
        // WICHTIG: für eventuelle Caches NUR org.apache.commons.collections.map.LRUMap benutzen!!
        super.preImportTask();
        progressMessageType = ProgressMessageType.READING;
        setMaxEntriesForCommit(MIN_MEMORY_ROWS_LIMIT);
        setCheckForDoubles(false);

        extraDeleteList = new GenericEtkDataObjectList();

        createComparer(150, 500);
        if (saveToDB) {
            loadExistingDataFromDB();
        }
        if (totalDBCount == 0) {
            fireMessage("!!Erstbefüllung");
            clearEndMessageList();
            insertedCount = 0;
        } else {
            fireMessage("!!Einlesen der Daten aus Datei");
        }
    }

    @Override
    protected boolean loadExistingDataFromDB() {
        // hole aus DB alle vorhandenen Records und rufe putFirst() auf
        // Da über alle PK-Werte sortiert wird (bei 15Mio * 28 Datensätzen) wird ein 2-stufiges Vorgehen gemacht:
        fireMessage("!!Laden der Daten aus der Datenbank");
        // Stufe 1 Bestimme distinct alle PARTNOs
        String[] fields = new String[]{ iPartsConst.FIELD_DA_PPUA_PARTNO };
        String[] whereFields = new String[]{ iPartsConst.FIELD_DA_PPUA_PARTNO };
        String[] whereValues = new String[]{ "*" };
        Set<String> partNos = new HashSet<>();
        try {
            DBDataObjectAttributesList attList = getProject().getDbLayer().getAttributesListCancelable(destTable, fields, whereFields,
                                                                                                       whereValues, ExtendedDataTypeLoadType.MARK,
                                                                                                       true, true);
            for (DBDataObjectAttributes attributes : attList) {
                String partNo = attributes.getFieldValue(iPartsConst.FIELD_DA_PPUA_PARTNO);
                partNos.add(partNo);
            }
        } catch (CanceledException e) {
            cancelImport(e.getMessage());
            return false;
        }

        // Stufe 2: Hole die sortierten Werte für eine PARTNO
        // damit ergeben sich Blöcke a 28 * 4 * (Anzahl der SERIES)
        // 4 sind die Ausprägungen für TYPE
        // somit ergeben sich für 1 Baureihe 112 zu sortierende Datensätze
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(destTable, iPartsConst.FIELD_DA_PPUA_PARTNO) };
        EtkDataObjectList<? extends EtkDataObject> list = getDataListForCompare();
        VarParam<Integer> readCounter = new VarParam<>(0);
        Map<iPartsPPUAId, List<String>> ppuaMap = new HashMap<>();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = createCallback(ppuaMap, readCounter);
        String[] sortFields = new String[]{ iPartsConst.FIELD_DA_PPUA_PARTNO, iPartsConst.FIELD_DA_PPUA_REGION,
                                            iPartsConst.FIELD_DA_PPUA_SERIES, iPartsConst.FIELD_DA_PPUA_ENTITY,
                                            iPartsConst.FIELD_DA_PPUA_TYPE, iPartsConst.FIELD_DA_PPUA_YEAR };


        for (String partNo : partNos) {
            whereValues = new String[]{ partNo };
            list.clear(DBActionOrigin.FROM_DB);
            list.searchSortAndFillWithJoin(getProject(), null, null, whereTableAndFields,
                                           whereValues, false, sortFields, true, null,
                                           false, false, false, foundAttributesCallback,
                                           false);

            handlePutFirstFromMap(ppuaMap);
        }
        getMessageLog().hideProgress();
        return true;
    }

    private EtkDataObjectList.FoundAttributesCallback createCallback(Map<iPartsPPUAId, List<String>> ppuaMap, VarParam<Integer> readCounter) {
        return new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                EtkDataObject data = buildDataFromAttributes(attributes);
                iPartsDataPPUA ppuaData = (iPartsDataPPUA)data;
                iPartsPPUAId id = new iPartsPPUAId(ppuaData.getAsId(), "");
                List<String> years = ppuaMap.get(id);
                if (years == null) {
                    handlePutFirstFromMap(ppuaMap);
                    years = new LinkedList<>();
                    ppuaMap.put(id, years);
                }
                years.add(ppuaData.getAsId().getYear() + YEAR_VALUE_DELIMITER + getValuesForListComp(data));
                readCounter.setValue(readCounter.getValue() + 1);
                updateProgress(readCounter.getValue(), totalDBCount);
                return false;
            }
        };
    }

    private void handlePutFirstFromMap(Map<iPartsPPUAId, List<String>> ppuaMap) {
        if (!ppuaMap.isEmpty()) {
            for (Map.Entry<iPartsPPUAId, List<String>> entry : ppuaMap.entrySet()) {
                String value = StrUtils.stringListToString(entry.getValue(), IdWithType.DB_ID_DELIMITER);
                putFirst(entry.getKey().toDBString(), value);
            }
            ppuaMap.clear();
        }
    }

    @Override
    protected EtkDataObject buildDataFromAttributes(DBDataObjectAttributes attributes) {
        iPartsDataPPUA data = new iPartsDataPPUA(getProject(), null);
        data.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
        return data;
    }

    @Override
    protected String getValuesForListComp(EtkDataObject data) {
        if (data instanceof iPartsDataPPUA) {
            return ((iPartsDataPPUA)data).getYearValueToDbString();
        }
        return "";
    }

    @Override
    protected EtkDataObjectList<? extends EtkDataObject> getDataListForCompare() {
        return new iPartsDataPPUAList();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        PPUAImportHelper importHelper = new PPUAImportHelper(getProject(), null, destTable);
        iPartsPPUAId basicId = importHelper.buildBaseIdFromImportRecord(importRec, recordNo);
        if (basicId.isValidId()) {
            if (totalDBCount > 0) {
                // den value aus den Importdaten zusammenbauen
                List<String> years = new LinkedList<>();
                for (String yearName : yearHeaderSet) {
                    String yearValue = importHelper.getValuesFromImport(yearName, importRec, recordNo);
                    if (dontImportEmptyValues && StrUtils.isEmpty(yearValue)) {
                        continue;
                    }
                    years.add(yearName.trim() + YEAR_VALUE_DELIMITER + yearValue);
                }
                String value = StrUtils.stringListToString(years, IdWithType.DB_ID_DELIMITER);
                putSecond(basicId.toDBString(), value);
            } else {
                // hier handelt es sich um die Erstbefüllung (in der DB sind keine Records vorhanden)
                for (String yearName : yearHeaderSet) {
                    String yearValue = importHelper.getValuesFromImport(yearName, importRec, recordNo);
                    if (dontImportEmptyValues && StrUtils.isEmpty(yearValue)) {
                        continue;
                    }
                    iPartsPPUAId ppuaId = new iPartsPPUAId(basicId, yearName.trim());
                    iPartsDataPPUA dataPPUA = new iPartsDataPPUA(getProject(), ppuaId);
                    dataPPUA.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                    dataPPUA.setYearValueFromDbString(yearValue, DBActionOrigin.FROM_EDIT);
                    doSaveToDB(dataPPUA);
                    insertedCount++;
                }
            }
        } else {
            // Warning
            fireWarning("!!In Zeile %1: Ungültige Id %2. Wird ignoriert!", String.valueOf(recordNo), basicId.toStringForLogMessages());
            increaseSkippedRecord();
        }
    }

    /**
     * Aus dem Value eine Map mit <Year, Value> zusammenbauen
     *
     * @param entryValue
     * @return
     */
    private Map<String, String> getYearValueMap(String entryValue) {
        List<String> yearValueList = StrUtils.toStringList(entryValue, IdWithType.DB_ID_DELIMITER, true, false);
        return getYearValueMap(yearValueList);
    }

    private Map<String, String> getYearValueMap(List<String> yearValueList) {
        Map<String, String> yearValueMap = new LinkedHashMap<>();
        for (String yearValue : yearValueList) {
            List<String> yearValuePair = StrUtils.toStringList(yearValue, YEAR_VALUE_DELIMITER, true, false);
            if (yearValuePair.size() >= 2) {
                yearValueMap.put(yearValuePair.get(0), yearValuePair.get(1));
            }
        }
        return yearValueMap;
    }

    /**
     * [DELETE] getOnlyInFirstItems(), die nur in der DB-Liste (= nicht mehr) vorhandene Datensätze löschen:
     *
     * @param entry
     * @param list
     * @param counterContainer
     */
    @Override
    protected void handleKeyValueForDelete(DiskMappedKeyValueEntry entry, GenericEtkDataObjectList list, SaveCounterContainer counterContainer) {
        iPartsPPUAId basicId = iPartsPPUAId.getFromDBString(entry.getKey());
        if (basicId.isValidId()) {
            Map<String, String> yearValueMap = getYearValueMap(entry.getValue());
            if (dontImportEmptyValues) {
                // nur die in der DB real vorkommenden Records löschen
                Map<iPartsPPUAId, iPartsDataPPUA> dbMap = getDatasForBasicId(basicId);
                for (iPartsDataPPUA data : dbMap.values()) {
                    list.delete(data, true, DBActionOrigin.FROM_EDIT);
                    counterContainer.deletedCount++;
                }
            } else {
                for (String year : yearValueMap.keySet()) {
                    iPartsPPUAId ppuaId = new iPartsPPUAId(basicId, year);
                    iPartsDataPPUA data = new iPartsDataPPUA(getProject(), ppuaId);
                    list.delete(data, true, DBActionOrigin.FROM_EDIT);
                    counterContainer.deletedCount++;
                }
            }
            // Den Fortschrittsbalken füttern.
            counterContainer.doUpdateProgress();
            // Zyklisch löschen, damit es nicht zu viele Einträge in der Liste werden.
            updateDeleteDataObjectList(list, (MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT * 10));
        }
    }

    /**
     * [INSERT] getOnlyInSecondItems(), nur in der Importdateidatenliste (= neue) Datensätze importieren:
     *
     * @param entry
     * @param counterContainer
     */
    @Override
    protected void handleKeyValueForInsert(DiskMappedKeyValueEntry entry, SaveCounterContainer counterContainer) {
        iPartsPPUAId basicId = iPartsPPUAId.getFromDBString(entry.getKey());
        if (basicId.isValidId()) {
            // hier ohne Abfrage der doppelten Einträge (Performance)
            Map<String, String> yearValueMap = getYearValueMap(entry.getValue());
            for (Map.Entry<String, String> yearEntry : yearValueMap.entrySet()) {
                String yearValue = yearEntry.getValue();
                boolean doAdd = true;
                if (dontImportEmptyValues && StrUtils.isEmpty(yearValue)) {
                    doAdd = false;
                }

                if (doAdd) {
                    iPartsPPUAId ppuaId = new iPartsPPUAId(basicId, yearEntry.getKey());
                    iPartsDataPPUA data = new iPartsDataPPUA(getProject(), ppuaId);
                    // ...  leer initialisieren ...
                    data.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    // ...  füllen ...
                    data.setYearValueFromDbString(yearValue, DBActionOrigin.FROM_EDIT);

                    counterContainer.insertedCount++;
                    // ... und speichern.
                    doSaveToDB(data);
                }
            }
            // Den Fortschrittsbalken füttern.
            counterContainer.doUpdateProgress();
        }
    }

    /**
     * Datensätze für eine basicId (ohne YEAR) aus der DB holen und daraus eine Map bauen
     *
     * @param basicId
     * @return
     */
    private Map<iPartsPPUAId, iPartsDataPPUA> getDatasForBasicId(iPartsPPUAId basicId) {
        iPartsDataPPUAList list = (iPartsDataPPUAList)getDataListForCompare();
        String[] whereFields = new String[]{ iPartsConst.FIELD_DA_PPUA_PARTNO, iPartsConst.FIELD_DA_PPUA_REGION,
                                             iPartsConst.FIELD_DA_PPUA_SERIES, iPartsConst.FIELD_DA_PPUA_ENTITY,
                                             iPartsConst.FIELD_DA_PPUA_TYPE };
        String[] whereValues = new String[]{ basicId.getPartNo(), basicId.getRegion(),
                                             basicId.getSeries(), basicId.getEntity(),
                                             basicId.getHitType() };
        String[] sortFields = null;

        list.searchSortAndFill(getProject(), destTable, whereFields, whereValues, sortFields,
                               DBDataObjectList.LoadType.COMPLETE, DBActionOrigin.FROM_DB);
        Map<iPartsPPUAId, iPartsDataPPUA> resultMap = new HashMap<>();
        for (iPartsDataPPUA data : list) {
            resultMap.put(data.getAsId(), data);
        }
        return resultMap;
    }

    /**
     * [UPDATE] getDifferentItems(), geänderte Datensätze aktualisieren:
     * hier muss unterschieden werden, ob sich der Wert für ein Jahr geändert hat, oder
     * ob ein neues Jahr mit einem Wert hinzugekommen ist
     *
     * @param entry
     * @param counterContainer
     */
    @Override
    protected void handleKeyValueForUpdate(DiskMappedKeyValueEntry entry, SaveCounterContainer counterContainer) {
        iPartsPPUAId basicId = iPartsPPUAId.getFromDBString(entry.getKey());
        if (basicId.isValidId()) {
            // Jahr, Value-Map aus dem Import
            Map<String, String> yearValueMap = getYearValueMap(entry.getValue());
            // Map der vorhandenen Records in der DB
            Map<iPartsPPUAId, iPartsDataPPUA> dbMap = getDatasForBasicId(basicId);
            for (String year : yearHeaderSet) {
                iPartsPPUAId ppuaId = new iPartsPPUAId(basicId, year);
                iPartsDataPPUA data = dbMap.get(ppuaId);
                if (data == null) {
                    // Eintrag in der DB nicht vorhanden => Neu anlegen, falls nicht leer
                    String yearValue = yearValueMap.get(year);
                    boolean doAdd = true;
                    if (dontImportEmptyValues && StrUtils.isEmpty(yearValue)) {
                        doAdd = false;
                    }
                    if (doAdd) {
                        // ein Neues Jahr => lege neuen DB-Record an
                        data = new iPartsDataPPUA(getProject(), ppuaId);
                        // ...  leer initialisieren ...
                        data.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        // ...  füllen ...
                        data.setYearValueFromDbString(yearValue, DBActionOrigin.FROM_EDIT);
                        // ... und speichern.
                        doSaveToDB(data);
                        counterContainer.insertedCount++;
                    }
                } else {
                    // Eintrag in der DB bereits vorhanden => entweder updaten, oder löschen, falls Importwert leer ist
                    if (dontImportEmptyValues) {
                        String yearValue = yearValueMap.get(year);
                        if (StrUtils.isValid(yearValue)) {
                            // nur Jahrwerte aus der Importdatei mit einem Wert abspeichern bzw. aktualisieren
                            data.setYearValueFromDbString(yearValue, DBActionOrigin.FROM_EDIT);
                            if (data.isModified()) {
                                // der Wert eines vorhandenen Jahres hat sich geändert
                                doSaveToDB(data);
                                counterContainer.updatedCount++;
                            }
                        } else {
                            // in der ImportDatei steht kein Jahrwert =>_Löschen in der DB
                            extraDeleteList.delete(data, true, DBActionOrigin.FROM_EDIT);
                            counterContainer.deletedCount++;
                        }
                    } else {
                        data.setYearValueFromDbString(yearValueMap.get(year), DBActionOrigin.FROM_EDIT);
                        if (data.isModified()) {
                            // der Wert eines vorhandenen Jahres hat sich geändert
                            doSaveToDB(data);
                            counterContainer.updatedCount++;
                        }
                    }
                }
            }
            // Den Fortschrittsbalken füttern.
            counterContainer.doUpdateProgress();
            if (dontImportEmptyValues) {
                // Zyklisch löschen, damit es nicht zu viele Einträge in der Liste werden.
                updateDeleteDataObjectList(extraDeleteList, (MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT * 10));
            }
        }
    }

    /**
     * Da bei Update auch neue Records erzeugt werden können hier nochmal die Ausgabe der realen Werte
     *
     * @param counterContainer
     */
    protected void doAfterCompareAndSave(SaveCounterContainer counterContainer) {
        clearEndMessageList();
        addToEndMessageList("!!%1 Datensätze gelöscht", String.valueOf(counterContainer.deletedCount));
        addToEndMessageList("!!%1 Datensätze importiert", String.valueOf(counterContainer.insertedCount));
        addToEndMessageList("!!%1 Datensätze aktualisiert", String.valueOf(counterContainer.updatedCount));
    }


    @Override
    protected void postImportTask() {
        if (!cancelled) {
            if (saveToDB) {
                if (totalDBCount > 0) {
                    compareAndSaveData(false);
                    if (dontImportEmptyValues) {
                        // Rest löschen
                        updateDeleteDataObjectList(extraDeleteList, 0);
                    }
                } else {
                    addToEndMessageList("!!%1 Datensätze importiert", String.valueOf(insertedCount));
                }
                if (!cancelled) {
                    storeYearList();
                }
            }
        }
        cleanup();
        super.postImportTask();
    }

    @Override
    protected void logImportRecordsFinished(int importRecordCount) {
        if (isSingleCall) {
            if (skippedRecords > 0) {
                fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                            getDatasetTextForLog(importRecordCount)) +
                            ", " + translateForLog("!!%1 %2 übersprungen", String.valueOf(skippedRecords),
                                                   getDatasetTextForLog(skippedRecords)));
            } else {
                fireMessage(translateForLog("!!%1 %2 erfolgreich bearbeitet", String.valueOf(importRecordCount),
                                            getDatasetTextForLog(importRecordCount)));
            }
            showEndMessage(true);
        } else {
            super.logImportRecordsFinished(importRecordCount);
        }
    }

    /**
     * Die Jahresliste aus dem aktuellen Import speichern
     * Da das KeyValue-Feld in der KEYVALUE-Tab nur eine Länge von 50 Zeichen hat, muss hier der erste und letzte Wert
     * des yearHeaderSets gespeichert werden
     */
    private void storeYearList() {
        String storeValue = "";
        if (!yearHeaderSet.isEmpty()) {
            DwList<String> list = new DwList<>(yearHeaderSet);
            storeValue = list.get(0) + IdWithType.DB_ID_DELIMITER + list.get(list.size() - 1);
        }
        getProject().getEtkDbs().setKeyValue(KEY_PPUA_YEARS_LAST_IMPORT, storeValue);
    }

    /**
     * Die gespeicherte Jahresliste des letzten Imports holen
     * Aus dem gespeicherten StartJahr und EndJahr wird wieder das Set über alle Jahre aufgebaut
     *
     * @return
     */
    private Set<String> getStoredYearList() {
        Set<String> storedYears = new TreeSet<>();
        String storedValue = getProject().getEtkDbs().getKeyValue(KEY_PPUA_YEARS_LAST_IMPORT);
        if (StrUtils.isValid(storedValue)) {
            List<String> yearList = StrUtils.toStringList(storedValue, IdWithType.DB_ID_DELIMITER, true, false);
            if (yearList.size() > 1) {
                String firstYear = yearList.get(0);
                String lastYear = yearList.get(1);
                if (StrUtils.isInteger(firstYear) && StrUtils.isInteger(lastYear)) {
                    int startYear = StrUtils.strToIntDef(firstYear, -1);
                    int endYear = StrUtils.strToIntDef(lastYear, -1);
                    for (int year = startYear; year <= endYear; year++) {
                        storedYears.add(String.valueOf(year));
                    }
                }
            }
        }
        return storedYears;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(destTable)) {
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(preparePPUAImporterKeyValueGZ(importFile, destTable, '\t', withHeader, null, Character.MIN_VALUE, DWFileCoding.CP_1252));
            } else {
                return importMasterData(preparePPUAImporterKeyValue(importFile, destTable, '\t', withHeader, null, Character.MIN_VALUE, DWFileCoding.CP_1252));
            }
        }
        return false;
    }

    @Override
    protected EtkDataObject buildDataFromEntry(DiskMappedKeyValueEntry entry) {
        iPartsPPUAId mappingId = iPartsPPUAId.getFromDBString(entry.getKey());
        if (mappingId != null) {
            return new iPartsDataPPUA(getProject(), mappingId);
        }
        return null;
    }

    @Override
    protected void addDataFromMappedKeyValueEntry(DiskMappedKeyValueEntry entry, EtkDataObject data) {
        if (data instanceof iPartsDataPPUA) {
            ((iPartsDataPPUA)data).setYearValueFromDbString(entry.getValue(), DBActionOrigin.FROM_EDIT);
        }
    }

    private boolean checkHeader(Map<String, Integer> headerNameToIndex) {
        yearHeaderSet = new TreeSet<>();
        for (String headerName : MUST_HEADERNAMES) {
            if (headerNameToIndex.get(headerName) == null) {
                // Error missing HeaderName
                fireError("!!Fehlende Import-Spalte %1! Import wird abgebrochen", headerName);
                return false;
            }
        }
        for (String headerName : headerNameToIndex.keySet()) {
            String testHeaderName = headerName.trim();
            if (StrUtils.isInteger(testHeaderName)) {
                int headerValue = StrUtils.strToIntDef(testHeaderName, -1);
                if (headerValue >= 1995) {
                    yearHeaderSet.add(headerName);
                }
            }
        }
        return !yearHeaderSet.isEmpty();
    }

    private AbstractKeyValueRecordReader preparePPUAImporterKeyValueGZ(DWFile importFile, String tableName, char separator, boolean withHeader,
                                                                       String[] headerNames, char quoteSign, DWFileCoding encoding) {
        PPUAKeyValueRecordGzCSVFileReader reader = new PPUAKeyValueRecordGzCSVFileReader(importFile, tableName, withHeader, headerNames, encoding);
        reader.setQuoteSign(quoteSign);
        reader.setSeparator(separator);
        return reader;
    }

    protected AbstractKeyValueRecordReader preparePPUAImporterKeyValue(DWFile importFile, String tableName, char separator, boolean withHeader,
                                                                       String[] headerNames, char quoteSign, DWFileCoding encoding) {
        PPUAKeyValueRecordCSVFileReader reader = new PPUAKeyValueRecordCSVFileReader(importFile, tableName, withHeader, headerNames, separator, encoding);
        reader.setQuoteSign(quoteSign);
        return reader;
    }


    private class PPUAKeyValueRecordGzCSVFileReader extends KeyValueRecordGzCSVFileReader {

        public PPUAKeyValueRecordGzCSVFileReader(DWFile file, String tableName, boolean withHeader, String[] headerNames, DWFileCoding encoding) {
            super(file, tableName, withHeader, headerNames, encoding);
        }

        @Override
        protected void handleHeaderLine() {
            super.handleHeaderLine();
            if (!checkHeader(getHeaderNameToIndex())) {
                cancelImport();
            }
        }
    }

    private class PPUAKeyValueRecordCSVFileReader extends KeyValueRecordCSVFileReader {

        public PPUAKeyValueRecordCSVFileReader(DWFile file, String tableName, boolean withHeader, String[] headerNames, char separator, DWFileCoding encoding) {
            super(file, tableName, withHeader, headerNames, separator, encoding);
        }

        @Override
        protected void handleHeaderLine() {
            super.handleHeaderLine();
            if (!checkHeader(getHeaderNameToIndex())) {
                cancelImport();
            }
        }
    }

    private class PPUAImportHelper extends MADImportHelper {

        public PPUAImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public iPartsPPUAId buildBaseIdFromImportRecord(Map<String, String> importRec, int recordNo) {
            return new iPartsPPUAId(handleValueOfSpecialField(PARTNO, importRec), handleValueOfSpecialField(REGION, importRec),
                                    handleValueOfSpecialField(VEHBR, importRec), handleValueOfSpecialField(ENTITY, importRec),
                                    handleValueOfSpecialField(TYPE, importRec), "");
        }

        public String getValuesFromImport(String yearName, Map<String, String> importRec, int recordNo) {
            return handleValueOfSpecialField(yearName, importRec);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            value = StrUtils.getEmptyOrValidString(value);
            if (sourceField.equals(VEHBR)) {
                if (StrUtils.isValid(value) && !value.startsWith(MODEL_NUMBER_PREFIX_CAR)) {
                    value = MODEL_NUMBER_PREFIX_CAR + value;
                }
            }
            return value;
        }
    }

}
