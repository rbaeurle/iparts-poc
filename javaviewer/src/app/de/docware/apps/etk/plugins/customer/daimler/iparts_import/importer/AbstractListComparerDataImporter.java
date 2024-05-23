/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.VarParam;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.collections.diskmappedlist.compare.DiskMappedKeyValueListCompare;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.terms.Condition;

import java.util.Iterator;
import java.util.List;

/**
 * Basisklasse für Differenz-Importer mit {@link DiskMappedKeyValueListCompare}
 */
public abstract class AbstractListComparerDataImporter extends AbstractDataImporter {

    protected static final int MIN_MEMORY_ROWS_LIMIT = 1000000;
    protected static final int MAX_MEMORY_ROWS_LIMIT = 2 * MIN_MEMORY_ROWS_LIMIT;

    protected String destTable;

    private List<String> endMessageList;
    private DiskMappedKeyValueListCompare listComp;
    private int maxEntriesForCommit;
    private boolean checkForDoubles;
    private Integer onlyInFirstItemSize;
    private Integer onlyInSecondItemSize;
    private Integer differentItemSize;

    protected int totalDBCount;

    protected boolean doBufferSave = true;
    protected boolean saveToDB = true;

    public AbstractListComparerDataImporter(EtkProject project, String importName, String tableName, boolean withHeader, FilesImporterFileListType... importFileTypes) {
        super(project, importName, withHeader, importFileTypes);
        this.destTable = tableName;
        this.maxEntriesForCommit = -1;
        this.checkForDoubles = true;
        this.onlyInFirstItemSize = null;
        this.onlyInSecondItemSize = null;
        this.differentItemSize = null;

        initImporter();
    }

    protected void initImporter() {
        setBufferedSave(doBufferSave);
        setClearCachesAfterImport(false);
        endMessageList = new DwList<>();
    }

    protected void setMaxEntriesForCommit(int maxEntriesForCommit) {
        this.maxEntriesForCommit = maxEntriesForCommit;
    }

    protected void setCheckForDoubles(boolean value) {
        this.checkForDoubles = value;
    }

    /**
     * Legt das magische Comparer-Objekt (huhuhu...) an und initialisiert es passend.
     *
     * @return
     */
    protected void createComparer(int maxKeyLength, int maxValueLength) {
        setTotalDBCount();
        createComparer(maxKeyLength, maxValueLength, totalDBCount);
    }

    /**
     * Legt das Comparer-Objekt an und initialisiert es passend inkl. der Anzahl der Objekte in der DB.
     *
     * @return
     */
    protected void createComparer(int maxKeyLength, int maxValueLength, int totalDBCount) {
        createComparer(maxKeyLength, maxValueLength, totalDBCount, true, false, true);
    }

    /**
     * Legt das Comparer-Objekt an und initialisiert es passend inkl. der Anzahl der Objekte in der DB und den gewünschten
     * Vergleichsoperationen
     *
     * @return
     */
    protected void createComparer(int maxKeyLength, int maxValueLength, int totalDBCount,
                                  boolean fillNewList, boolean fillEqualsList, boolean fillDiffList) {
        this.totalDBCount = totalDBCount;
        // Mit Platz nach oben auf zwei verschiedene Anzahlen Datensätze beschränken.
        int maxMemoryRows = (totalDBCount < MIN_MEMORY_ROWS_LIMIT) ? MIN_MEMORY_ROWS_LIMIT : MAX_MEMORY_ROWS_LIMIT;
        // Das Magic Object anlegen:
        createListComparer(maxKeyLength, maxValueLength, maxMemoryRows, fillNewList, fillEqualsList, fillDiffList);
    }

    protected void setTotalDBCount() {
        totalDBCount = getProject().getEtkDbs().getRecordCount(destTable);
    }

    protected boolean isInitialFilling() {
        return totalDBCount == 0;
    }

    protected void createListComparer(int maxKeyLength, int maxValueLength, int maxMemoryRows, boolean fillNewList,
                                      boolean fillEqualsList, boolean fillDiffList) {
        listComp = new DiskMappedKeyValueListCompare(maxKeyLength, maxValueLength, maxMemoryRows, fillNewList, fillEqualsList, fillDiffList);
        listComp.cleanup();
    }

    protected void cleanup() {
        if (listComp != null) {
            listComp.cleanup();
            listComp = null;
        }
        onlyInFirstItemSize = null;
        onlyInSecondItemSize = null;
        differentItemSize = null;
    }

    /**
     * Das ist die Liste der vorhandenen Daten aus der Datenbank.
     * Hole aus der DB alle vorhandenen Datensätze und speichere sie in der ersten Liste mit putFirst() im Comparer ab.
     *
     * @return
     */
    protected boolean loadExistingDataFromDB() {
        // hole aus DB alle vorhandene Records und rufe putFirst() auf
        fireStandardMessage("!!Laden der Daten aus der Datenbank");
        EtkDataObjectList<? extends EtkDataObject> list = getDataListForCompare();
        VarParam<Integer> readCounter = new VarParam<>(0);
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                EtkDataObject data = buildDataFromAttributes(attributes);
                String key = data.getAsId().toDBString();
                putFirst(key, getValuesForListComp(data));
                readCounter.setValue(readCounter.getValue() + 1);
                updateProgress(readCounter.getValue(), totalDBCount);
                return false;
            }
        };
        list.searchSortAndFillWithJoin(getProject(), getDBLanguageForLoadExistingData(), null, getWhereFieldsForLoadExistingData(), getWhereValuesForLoadExistingData(),
                                       false, null,
                                       false, false, foundAttributesCallback);
        hideProgress();
        return true;
    }

    protected void fireStandardMessage(String message) {
        fireMessage(message);
    }

    protected String getDBLanguageForLoadExistingData() {
        return null;
    }

    protected String[] getWhereValuesForLoadExistingData() {
        return null;
    }

    protected String[] getWhereFieldsForLoadExistingData() {
        return null;
    }


    /**
     * Befüllt ein {@link EtkDataObject} Datenobjekt mit den übergebenen Attributen.
     *
     * @param attributes
     * @return
     */
    protected abstract EtkDataObject buildDataFromAttributes(DBDataObjectAttributes attributes);

    /**
     * Liefert den Werte für den {@link DiskMappedKeyValueListCompare}
     *
     * @param data
     * @return
     */
    protected abstract String getValuesForListComp(EtkDataObject data);

    /**
     * Liefert die DataObjectList für die Liste der vorhandenen Daten aus der Datenbank.
     *
     * @return
     */
    protected abstract EtkDataObjectList<? extends EtkDataObject> getDataListForCompare();

    protected void clearEndMessageList() {
        endMessageList.clear();
    }

    protected void addToEndMessageList(String translationsKey, String... placeHolderTexts) {
        endMessageList.add(translateForLog(translationsKey, placeHolderTexts));
    }

    /**
     * Bestehende Daten dem {@link DiskMappedKeyValueListCompare} hinzufügen
     *
     * @param key
     * @param value
     */
    protected void putFirst(String key, String value) {
        if (saveToDB) {
            listComp.putFirst(key, value);
        }
    }

    /**
     * Bestehende Daten dem {@link DiskMappedKeyValueListCompare} hinzufügen
     *
     * @param key
     * @param value
     */
    protected void putSecond(String key, String value) {
        if (saveToDB) {
            listComp.putSecond(key, value);
        }
    }

    /**
     * Jetzt müssen die DB Daten mit den eingelesenen Daten abgeglichen werden.
     * - Die nicht mehr vorhandenen müssen gelöscht werden.
     * - Die neuen müssen gespeichert werden.
     * - Die veränderten müssen aktualisiert werden.
     *
     * @return
     */
    protected boolean compareAndSaveData(boolean showEndMessagesDirect) {
        SaveCounterContainer counterContainer = new SaveCounterContainer(getListCompTotalCount());
        fireStandardMessage("!!Abgleich der Daten");
        clearEndMessageList();
        // ---------
        // [DELETE] getOnlyInFirstItems(), die nur in der DB-Liste (= nicht mehr) vorhandene Datensätze löschen:
        // ---------
        if (!deleteEntriesFromListComp(counterContainer)) {
            return false;
        }
        showEndMessage(showEndMessagesDirect);

        // ---------
        // [INSERT] getOnlyInSecondItems(), nur in der Importdateidatenliste (= neue) Datensätze importieren:
        // ---------
        if (!insertEntriesFromListComp(counterContainer)) {
            return false;
        }
        showEndMessage(showEndMessagesDirect);

        // ---------
        // [UPDATE] getDifferentItems(), geänderte Datensätze aktualisieren:
        // ---------
        if (!updateEntriesFromListComp(counterContainer)) {
            return false;
        }
        showEndMessage(showEndMessagesDirect);
        doAfterCompareAndSave(counterContainer);
        if ((counterContainer.deletedCount + counterContainer.insertedCount + counterContainer.updatedCount) < 1) {
            addToEndMessageList("!!Keine Änderungen");
            showEndMessage(showEndMessagesDirect);
        } else {
            fireStandardMessage("!!Speichern beendet");
        }
        hideProgress();
        return true;
    }

    protected void hideProgress() {
        getMessageLog().hideProgress();
    }

    protected void doAfterCompareAndSave(SaveCounterContainer counterContainer) {

    }

    /**
     * Meldungen aus dem Vergleich/Import direkt ausgeben, oder aufsammeln
     *
     * @param showEndMessagesDirect
     */
    protected void showEndMessage(boolean showEndMessagesDirect) {
        if (showEndMessagesDirect) {
            for (String msg : endMessageList) {
                fireStandardMessage(msg);
            }
            endMessageList.clear();
        }
    }

    protected int getOnlyInFirstItemSize() {
        if (listComp != null) {
            if (onlyInFirstItemSize == null) {
                onlyInFirstItemSize = listComp.getOnlyInFirstItems().size();
            }
            return onlyInFirstItemSize;
        }
        return -1;
    }

    protected int getOnlyInSecondItemSize() {
        if (listComp != null) {
            if (onlyInSecondItemSize == null) {
                onlyInSecondItemSize = listComp.getOnlyInSecondItems().size();
            }
            return onlyInSecondItemSize;
        }
        return -1;
    }

    protected int getDifferentItemSize() {
        if (listComp != null) {
            if (differentItemSize == null) {
                differentItemSize = listComp.getDifferentItems().size();
            }
            return differentItemSize;
        }
        return -1;
    }

    protected int getListCompTotalCount() {
        return getOnlyInFirstItemSize() + getOnlyInSecondItemSize() + getDifferentItemSize();
    }

    protected boolean deleteEntriesFromListComp(SaveCounterContainer counterContainer) {
        // ---------
        // [DELETE] getOnlyInFirstItems(), die nur in der DB-Liste (= nicht mehr) vorhandene Datensätze löschen:
        // ---------
        fireStandardMessage("!!Löschen veralteter Daten");
        if (getOnlyInFirstItemSize() > 0) {
            GenericEtkDataObjectList list = new GenericEtkDataObjectList();
            Iterator<DiskMappedKeyValueEntry> iter = listComp.getOnlyInFirstItems().getIterator();
            while (iter.hasNext()) {
                if (isCancelled()) {
                    return false;
                }
                DiskMappedKeyValueEntry entry = iter.next();
                // Jetzt das Objekt anlegen, zum Löschen reicht es die ID zu setzen.
                handleKeyValueForDelete(entry, list, counterContainer);
            }
            // Für die letzten Elemente auch noch das Löschen aufrufen.
            updateDeleteDataObjectList(list, 0);
        }
        addToEndMessageList("!!%1 Datensätze gelöscht", String.valueOf(counterContainer.deletedCount));
        if ((maxEntriesForCommit > 0) && (counterContainer.deletedCount >= maxEntriesForCommit)) {
            autoCommitAfterMaxEntries(0);
        }
        return true;
    }

    protected void handleKeyValueForDelete(DiskMappedKeyValueEntry entry, GenericEtkDataObjectList list, SaveCounterContainer counterContainer) {
        EtkDataObject item = buildDataFromEntry(entry);
        if (item != null) {
            list.delete(item, true, DBActionOrigin.FROM_EDIT);
            counterContainer.deletedCount++;
            // Den Fortschrittsbalken füttern.
            counterContainer.doUpdateProgress();

            // Zyklisch löschen, damit es nicht zu viele Einträge in der Liste werden.
            updateDeleteDataObjectList(list, (MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT * 10));
        }
    }

    protected boolean insertEntriesFromListComp(SaveCounterContainer counterContainer) {
        // ---------
        // [INSERT] getOnlyInSecondItems(), nur in der Importdateidatenliste (= neue) Datensätze importieren:
        // ---------
        fireStandardMessage("!!Importieren neuer Daten");
        if (getOnlyInSecondItemSize() > 0) {
            Iterator<DiskMappedKeyValueEntry> iter = listComp.getOnlyInSecondItems().getIterator();
            while (iter.hasNext()) {
                if (isCancelled()) {
                    return false;
                }
                DiskMappedKeyValueEntry entry = iter.next();
                // Jetzt das Objekt anlegen ...
                handleKeyValueForInsert(entry, counterContainer);
            }
        }
        addToEndMessageList("!!%1 Datensätze importiert", String.valueOf(counterContainer.insertedCount));
        if (counterContainer.duplicatesCount > 0) {
            addToEndMessageList("!!%1 doppelte Datensätze übersprungen <=== siehe Job-Log", String.valueOf(counterContainer.duplicatesCount));
        }
        return true;
    }

    protected void handleKeyValueForInsert(DiskMappedKeyValueEntry entry, SaveCounterContainer counterContainer) {
        EtkDataObject item = buildDataFromEntry(entry);
        if (item != null) {
            if (checkForDoubles) {
                if (!item.existsInDB()) {
                    // ...  leer initialisieren ...
                    item.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    // ...  füllen ...
                    addDataFromMappedKeyValueEntry(entry, item);
                    counterContainer.insertedCount++;
                    // ... und speichern.
                    doSaveToDB(item);
                } else {
                    fireWarningToFile("!!Doppelter Eintrag %1 wird übersprungen", item.getAsId().toStringForLogMessages());
                    counterContainer.duplicatesCount++;
                }
            } else {
                // ...  leer initialisieren ...
                item.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                // ...  füllen ...
                addDataFromMappedKeyValueEntry(entry, item);
                counterContainer.insertedCount++;
                // ... und speichern.
                doSaveToDB(item);
            }

            // Den Fortschrittsbalken füttern.
            counterContainer.doUpdateProgress();
        }
    }

    protected boolean updateEntriesFromListComp(SaveCounterContainer counterContainer) {
        // ---------
        // [UPDATE] getDifferentItems(), geänderte Datensätze aktualisieren:
        // ---------
        fireStandardMessage("!!Aktualisieren der Daten");
        if (getDifferentItemSize() > 0) {
            Iterator<DiskMappedKeyValueEntry> iter = listComp.getDifferentItems().getIterator();
            while (iter.hasNext()) {
                if (isCancelled()) {
                    return false;
                }
                DiskMappedKeyValueEntry entry = iter.next();
                // Jetzt das Objekt anlegen ...
                handleKeyValueForUpdate(entry, counterContainer);
            }
        }
        addToEndMessageList("!!%1 Datensätze aktualisiert", String.valueOf(counterContainer.updatedCount));
        return true;
    }

    protected void handleKeyValueForUpdate(DiskMappedKeyValueEntry entry, SaveCounterContainer counterContainer) {
        EtkDataObject item = buildDataFromEntry(entry);
        if ((item != null) && itemValidForUpdate(item, entry)) {
            // ...  leer initialisieren ...
            if (!item.existsInDB()) {
                item.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            // ...  füllen ...
            addDataFromMappedKeyValueEntry(entry, item);
            fireWarningToFile("!!Geänderter Eintrag %1", item.getAsId().toStringForLogMessages());
            counterContainer.updatedCount++;
            // ... und speichern.
            doSaveToDB(item);
            // Den Fortschrittsbalken füttern.
            counterContainer.doUpdateProgress();
        }
    }

    /**
     * Check, ob ein DatenObjekt für die bevorstehende Update-Operation gültig ist
     *
     * @param item
     * @param entry
     */
    protected boolean itemValidForUpdate(EtkDataObject item, DiskMappedKeyValueEntry entry) {
        return true;
    }

    /**
     * Das Erzeugen eines {@link EtkDataObject}-Objekts aus einer der Listen wird mehrfach gebraucht.
     * ACHTUNG: hier wird nur die Id gesetzt!
     *
     * @param entry
     * @return
     */
    protected abstract EtkDataObject buildDataFromEntry(DiskMappedKeyValueEntry entry);

    /**
     * Zyklisch löschen, damit es nicht zu viele Einträge in der Liste werden.
     *
     * @param list
     * @param maxCount
     */
    protected void updateDeleteDataObjectList(GenericEtkDataObjectList list, int maxCount) {
        if (list.getDeletedList().size() >= maxCount) {
            if (saveToDB) {
                list.saveToDB(getProject());
            }
            list.clear(DBActionOrigin.FROM_DB);
        }
    }

    protected abstract void addDataFromMappedKeyValueEntry(DiskMappedKeyValueEntry entry, EtkDataObject data);

    /**
     * Liefert die Anzahl an Datensätzen in der DB zum übergebenen fieldName und fieldValue (OPERATOR_EQUALS)
     *
     * @param fieldName
     * @param fieldValue
     * @return
     */
    protected int getTotalDBCountForCondition(String fieldName, String fieldValue) {
        DBSQLQuery query = getProject().getEtkDbs().getDBForDomain(DBDatabaseDomain.MAIN).getNewQuery();
        query.select("count(*)").from(destTable);
        if (StrUtils.isValid(fieldName)) {
            query.where(new Condition(fieldName, Condition.OPERATOR_EQUALS, fieldValue));
        }
        try (DBDataSetCancelable dataSet = query.executeQueryCancelable()) { // Query ausführen
            if ((dataSet != null) && dataSet.next()) {
                List<String> test = dataSet.getStringList();
                if ((test != null) && !test.isEmpty()) {
                    return Integer.parseInt(test.get(0));
                }
            }
        } catch (CanceledException ignored) {
        }
        return 0;
    }

    public boolean doSaveToDB(EtkDataObject dataObject) {
        if (!saveToDB) {
            return true;
        }
        boolean result = saveToDB(dataObject);
        if (result && (maxEntriesForCommit > 0)) {
            if (autoCommitAfterMaxEntries(maxEntriesForCommit)) {
                fireMessageToFile("!!Intermediate Commit");
            }
        }
        return result;
    }


    @Override
    public boolean finishImport() {
        boolean result = super.finishImport();
        // finishImport() wird im Gegensatz zu postImportTask() immer aufgerufen => zur Sicherheit
        cleanup();
        return result;
    }

    protected void fireMessage(String key, String... placeHolderTexts) {
        fireMessage(MessageLogType.tmlMessage, key, placeHolderTexts);
    }

    protected void fireMessageToFile(String key, String... placeHolderTexts) {
        fireToFileOnly(MessageLogType.tmlMessage, key, placeHolderTexts);
    }

    protected void fireWarning(String key, String... placeHolderTexts) {
        fireMessage(MessageLogType.tmlWarning, key, placeHolderTexts);
    }

    protected void fireWarningToFile(String key, String... placeHolderTexts) {
        fireToFileOnly(MessageLogType.tmlWarning, key, placeHolderTexts);
    }

    protected void fireError(String key, String... placeHolderTexts) {
        fireMessage(MessageLogType.tmlError, key, placeHolderTexts);
    }

    protected void fireToFileOnly(MessageLogType type, String key, String... placeHolderTexts) {
        getMessageLog().fireMessage(translateForLog(key, placeHolderTexts), type, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
    }

    protected void fireMessage(MessageLogType type, String key, String... placeHolderTexts) {
        getMessageLog().fireMessage(translateForLog(key, placeHolderTexts), type, MessageLogOption.TIME_STAMP);
    }


    protected class SaveCounterContainer {

        public int totalRecordCount;
        public int currentRecordCount;
        public int deletedCount;
        public int insertedCount;
        public int updatedCount;
        public int duplicatesCount;

        public SaveCounterContainer(int totalRecordCount) {
            reset();
            this.totalRecordCount = totalRecordCount;
        }

        public void reset() {
            totalRecordCount = 0;
            currentRecordCount = 0;
            deletedCount = 0;
            insertedCount = 0;
            updatedCount = 0;
            duplicatesCount = 0;
        }

        public void doUpdateProgress() {
            currentRecordCount++;
            updateProgress(currentRecordCount, totalRecordCount);
        }
    }
}
