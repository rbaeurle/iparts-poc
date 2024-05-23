/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.genvo;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenVoSuppText;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataGenVoSuppTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsGenVoSuppTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.helper.DictMetaTechChangeSetHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractListComparerDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.DictMetaSearchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Differenz-Importer für die Generischen Verbauorte Zusatztexte
 */
public class GenVoSuppTextImporter extends AbstractListComparerDataImporter implements iPartsConst {

    private static final String GENVO_SUPPLEMENT_TEXTS = "!!GenVo Ergänzungstexte";

    private static String[] headerNames = new String[]{ "Code", "Bezeichnung" };
    private int insertedCount;
    private Language searchLang;
    private Map<String, iPartsDataDictMeta> foundTextMap;    // Map für gefundene Lexikon-Einträge
    private DictMetaSearchHelper dictSearchHelper;         // Hilfsklasse für LexikonSuche
    private DictMetaTechChangeSetHelper dictChangeSetHelper; // Hilfsklasse für neu angelegte Lexikon-Einträge

    public GenVoSuppTextImporter(EtkProject project) {
        super(project, GENVO_SUPPLEMENT_TEXTS, TABLE_DA_GENVO_SUPP_TEXT, true,
              new FilesImporterFileListType(TABLE_DA_GENVO_SUPP_TEXT, GENVO_SUPPLEMENT_TEXTS, false,
                                            false, false,
                                            new String[]{ MimeTypes.EXTENSION_EXCEL_XLSX, MimeTypes.EXTENSION_EXCEL_XLS, MimeTypes.EXTENSION_ALL_FILES }));
        this.searchLang = Language.DE;  // Texte aus Importdatei sind in DE
        this.foundTextMap = new HashMap<>();
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
    protected EtkDataObject buildDataFromEntry(DiskMappedKeyValueEntry entry) {
        iPartsGenVoSuppTextId mappingId = iPartsGenVoSuppTextId.getFromDBString(entry.getKey());
        if (mappingId != null) {
            return new iPartsDataGenVoSuppText(getProject(), mappingId);
        }
        return null;
    }

    @Override
    protected void addDataFromMappedKeyValueEntry(DiskMappedKeyValueEntry entry, EtkDataObject data) {
        if (data instanceof iPartsDataGenVoSuppText) {
            iPartsDataGenVoSuppText dataGenVoSupp = (iPartsDataGenVoSuppText)data;
            // hier Text suchen und Multilang eintragen
            if (!dataGenVoSupp.existsInDB()) {
                dataGenVoSupp.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            String descr = entry.getValue();
            setGenVoDescr(dataGenVoSupp, descr);
        }
    }

    protected boolean checkTableValidity(AbstractKeyValueRecordReader reader) {
        boolean result = super.checkTableValidity(reader);
        if (result) {
            iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.ADD_TEXT);
            if (textKindId == null) {
                cancelImport(translateForLog("Textart %1 nicht gefunden zum Laden des Lexikons", translateForLog(DictTextKindTypes.ADD_TEXT.getTextKindName())));
                return false;
            }
            dictSearchHelper = new DictMetaSearchHelper(getProject(), textKindId, searchLang);
            dictChangeSetHelper = new DictMetaTechChangeSetHelper(getProject(), textKindId, searchLang);
        }
        return result;
    }

    @Override
    protected void preImportTask() {
        // WICHTIG: für eventuelle Caches NUR org.apache.commons.collections.map.LRUMap benutzen!!
        super.preImportTask();
        progressMessageType = ProgressMessageType.READING;
        setMaxEntriesForCommit(MIN_MEMORY_ROWS_LIMIT);
        foundTextMap.clear();
        dictChangeSetHelper.clear();

        createComparer(50, 500);
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
        // überschrieben wegen laden der vorhandenen GenVo MultiLangs
        // hole aus DB alle vorhandenen Records und rufe putFirst() auf
        fireMessage("!!Laden der Daten aus der Datenbank");
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
        list.searchSortAndFillWithJoin(getProject(), searchLang.getCode(), null, null, null,
                                       false, null,
                                       false, false, foundAttributesCallback);
        getMessageLog().hideProgress();

        return true;
    }

    @Override
    protected EtkDataObject buildDataFromAttributes(DBDataObjectAttributes attributes) {
        iPartsDataGenVoSuppText data = new iPartsDataGenVoSuppText(getProject(), null);
        data.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
        return data;
    }

    @Override
    protected String getValuesForListComp(EtkDataObject data) {
        if (data instanceof iPartsDataGenVoSuppText) {
            // Überprüfen, ob der Text im Lexikon vorhanden ist
            EtkMultiSprache multiLang = data.getFieldValueAsMultiLanguage(FIELD_DA_GENVO_DESCR);
            if (StrUtils.isValid(multiLang.getTextId())) {
                String descr = multiLang.getText(searchLang.getCode());
                if (StrUtils.isValid(descr)) {
                    iPartsDataDictMeta dataDictMeta = foundTextMap.get(descr);
                    if (dataDictMeta == null) {
                        dataDictMeta = dictSearchHelper.searchTextInDictionary(descr);
                        if (dataDictMeta != null) {
                            foundTextMap.put(descr, dataDictMeta);
                            return descr;
                        }
                    } else {
                        return descr;
                    }
                }
            }
        }
        return "";
    }

    @Override
    protected EtkDataObjectList<? extends EtkDataObject> getDataListForCompare() {
        return new iPartsDataGenVoSuppTextList();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        GenVoSuppImportHelper importHelper = new GenVoSuppImportHelper(getProject(), null, destTable);
        iPartsGenVoSuppTextId genVoSuppId = importHelper.getSuppId(importRec, recordNo);
        if (genVoSuppId == null) {
            return;
        }
        if (totalDBCount > 0) {
            // den value aus den Importdaten zusammenbauen
            putSecond(genVoSuppId.toDBString(), importHelper.getGenVoDescr(importRec, recordNo));
        } else {
            // hier handelt es sich um die Erstbefüllung (in der DB sind keine Records vorhanden)
            iPartsDataGenVoSuppText dataGenVoSupp = new iPartsDataGenVoSuppText(getProject(), genVoSuppId);
            dataGenVoSupp.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            // Lexikonsuche
            setGenVoDescr(dataGenVoSupp, importHelper.getGenVoDescr(importRec, recordNo));
            doSaveToDB(dataGenVoSupp);
            insertedCount++;
        }
    }

    private void setGenVoDescr(iPartsDataGenVoSuppText dataGenVoSupp, String descr) {
        if (StrUtils.isValid(descr)) {
            // check foundTextMap, ob Lexikon-Eintrag bereits vorhanden
            iPartsDataDictMeta dataDictMeta = foundTextMap.get(descr);
            if (dataDictMeta != null) {
                // bereits im Cache
                // MultiLang in dataGenVoSupp eintragen
                dataGenVoSupp.setFieldValueAsMultiLanguage(FIELD_DA_GENVO_DESCR, dataDictMeta.getMultiLang(), DBActionOrigin.FROM_EDIT);
            } else {
                // Suche im Lexikon
                dataDictMeta = dictSearchHelper.searchTextInDictionary(descr);
                if (dataDictMeta == null) {
                    // Neuanlage DictMeta
                    dataDictMeta = createAndInitDataDictMeta(descr);
                    // DictMeta für späteres Speichern merken
                    updateCreatedDataDictMetaList(dataDictMeta, MAX_ENTRIES_FOR_TECH_CHANGE_SET);
                }
                // MultiLang in dataGenVoSupp eintragen
                dataGenVoSupp.setFieldValueAsMultiLanguage(FIELD_DA_GENVO_DESCR, dataDictMeta.getMultiLang(), DBActionOrigin.FROM_EDIT);
                // DictMeta in foundTextMap merken
                foundTextMap.put(descr, dataDictMeta);
            }
        }
    }

    /**
     * Merken der neu erzeugten Lexikon-Einträe, bzw alles Speichern bei dataDictMeta= =n ull und maxCount == 0
     *
     * @param dataDictMeta
     * @param maxCount
     */
    protected void updateCreatedDataDictMetaList(iPartsDataDictMeta dataDictMeta, int maxCount) {
        if (dataDictMeta == null) {
            if (dictChangeSetHelper.getTotalDictMetaSavedCounter() > 0) {
                fireMessage("!!Speichern der Lexikon-Einträge");
            }
        }
        dictChangeSetHelper.updateCreatedDataDictMetaList(dataDictMeta, maxCount, saveToDB);
    }

    /**
     * neuen Lerxikon-Eintrag (dataDictMeta und EtkMultiSprache) anlegen
     *
     * @param descr
     * @return
     */
    private iPartsDataDictMeta createAndInitDataDictMeta(String descr) {
        EtkMultiSprache multiLang = new EtkMultiSprache();
        multiLang.setText(searchLang, descr);
        multiLang.setTextId(DictHelper.buildIPARTSDictTextId());

        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(dictSearchHelper.getTextKindId().getTextKindId(), multiLang.getTextId());
        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
        dataDictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        String state = DictHelper.getMADDictStatus();
        dataDictMeta.setState(state, DBActionOrigin.FROM_EDIT);
//                dataDictMeta.setForeignId("", DBActionOrigin.FROM_EDIT);
        String source = iPartsImportDataOrigin.IPARTS_GENVO.getOrigin();
        dataDictMeta.setSource(source, DBActionOrigin.FROM_EDIT);
        dataDictMeta.setActCreationDate(DBActionOrigin.FROM_EDIT);
        dataDictMeta.setUserId(iPartsUserAdminDb.getLoginUserName(), DBActionOrigin.FROM_EDIT);
        dataDictMeta.setNewMultiLang(multiLang);
        return dataDictMeta;
    }

    /**
     * Da bei Update auch neue Records erzeugt werden können hier nochmal die Ausgabe der realen Werte
     *
     * @param counterContainer
     */
    protected void doAfterCompareAndSave(SaveCounterContainer counterContainer) {
        clearEndMessageList();

        // Neu angelegte Lexikon-Einträge im Tech ChangeSet speichern
        updateCreatedDataDictMetaList(null, 0);

        addToEndMessageList("!!%1 Datensätze gelöscht", String.valueOf(counterContainer.deletedCount));
        addToEndMessageList("!!%1 Datensätze importiert", String.valueOf(counterContainer.insertedCount));
        addToEndMessageList("!!%1 Datensätze aktualisiert", String.valueOf(counterContainer.updatedCount));
        addDictEndMessages();
    }

    private void addDictEndMessages() {
        if (dictChangeSetHelper.getTotalDictMetaSavedCounter() > 0) {
            String key = "!!%1 Lexikoneinträge angelegt";
            if (dictChangeSetHelper.getTotalDictMetaSavedCounter() == 1) {
                key = "!!%1 Lexikoneintrag angelegt";
            }
            addToEndMessageList(key, String.valueOf(dictChangeSetHelper.getTotalDictMetaSavedCounter()));
            key = "!!%1 Technisches Änderungssets angelegt";
            if (dictChangeSetHelper.getTotalChangeSetCounter() == 1) {
                key = "!!%1 Technisches Änderungsset angelegt";
            }
            addToEndMessageList(key, String.valueOf(dictChangeSetHelper.getTotalChangeSetCounter()));
        } else {
            addToEndMessageList("!!Keine Lexikoneinträge angelegt");
        }
    }

    @Override
    protected void postImportTask() {
        if (!cancelled) {
            if (saveToDB) {
                if (totalDBCount > 0) {
                    compareAndSaveData(false);
                } else {
                    // Neu angelegte Lexikon-Einträge im Tech ChangeSet speichern
                    updateCreatedDataDictMetaList(null, 0);
                    addToEndMessageList("!!%1 Datensätze importiert", String.valueOf(insertedCount));
                    addDictEndMessages();
                }
                setClearCachesAfterImport(dictChangeSetHelper.getTotalDictMetaSavedCounter() > 0);
            }
        }
        cleanup();
        super.postImportTask();
    }

    @Override
    protected void clearCaches() {
        // alle Caches brauchen nicht gelöscht zu werden
        //super.clearCaches();
        if (dictChangeSetHelper.getTotalDictMetaSavedCounter() > 0) {
            // Lexikon-Cache löschen für die Textart Ergänzungstexte
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.ADD_TEXT));
        }
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

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(destTable)) {
            return importMasterData(prepareImporterKeyValue(importFile, destTable, true, headerNames));
        }
        return false;
    }

    private class GenVoSuppImportHelper extends MADImportHelper {

        public GenVoSuppImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public iPartsGenVoSuppTextId getSuppId(Map<String, String> importRec, int recordNo) {
            String genVoCode = handleValueOfSpecialField(headerNames[0], importRec);
            if (StrUtils.isEmpty(genVoCode)) {
                fireWarning("!!In Zeile %1: Leere Id. Wird ignoriert!", String.valueOf(recordNo));
                return null;
            }
            return new iPartsGenVoSuppTextId(genVoCode);
        }

        public String getGenVoDescr(Map<String, String> importRec, int recordNo) {
            String value = handleValueOfSpecialField(headerNames[1], importRec);
            if (StrUtils.isEmpty(value)) {
                fireWarning("!!In Zeile %1: Leere Beschreibung. Wird ignoriert!", String.valueOf(recordNo));
            }
            return value;
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            value = StrUtils.getEmptyOrValidString(value);
            return value;
        }

    }
}


