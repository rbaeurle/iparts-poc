/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.spk;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSpkMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSpkMappingList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSpkMappingId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.helper.DictMetaTechChangeSetHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractListComparerDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.DictMetaSearchHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.diskmappedlist.DiskMappedKeyValueEntry;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class SpkMappingImporter extends AbstractListComparerDataImporter implements iPartsConst {

    private static final String TABLE_NAME = TABLE_DA_SPK_MAPPING;
    private static final String IMPORT_TITLE = "!!SPK-Mapping";
    private static final String DICT_META_SOURCE = iPartsImportDataOrigin.IPARTS_SPK.getOrigin();

    // Wenn die Importdatei Header enthält und keine Header übergeben werden müssen die Header so heißen.
    private static final String SUPPLEMENTAL_HM_M = "Supplemental";                     // Spalte B Modul
    private static final String ENG_CONNECTOR_CODE_KURZ_E = "Engineering Connector Code"; // Spalte D SPK-KURZ-E
    private static final String ENG_COMPONENT_NAME_LANG_E = "Engineering Component Name"; // Spalte E SPK-LANG-E
    private static final String COMPONENT_CODE_KURZ_AS = "Component Code";             // Spalte H SPK-KURZ-AS
    private static final String COMPONENT_NAME_LANG_AS = "Component Name";             // Spalte I SPK-LANG-AS
    private static final String ENG_CONNECTOR_AS = "Connector Code"; // Spalte J SPK-KURZ-E
    private static final String ENG_CONNECTOR_E = "ENG_CONNECTOR_E"; // Spalte D SPK-KURZ-E
    private static final String ENG_STEERING = "ENG_STEERING";       // Spalte B nach Modul

    private static final Language SEARCH_LANG = Language.DE;
    private static final String DATA_DELIMITER = "|&|"; // Trennzeichen für die Nutzdaten (Langbenennungen)

    private static final Map<String, String> IMPORTREC_PREP_MAP = new LinkedHashMap<>();
    private static final Map<String, String> IMPORT_MAPPING = new LinkedHashMap<>();

    static {
        // doppelt auszuwertende Importfelder
        IMPORTREC_PREP_MAP.put(ENG_CONNECTOR_E, ENG_CONNECTOR_CODE_KURZ_E);
        IMPORTREC_PREP_MAP.put(ENG_STEERING, SUPPLEMENTAL_HM_M);

        // Liste der Felder für den Value des Comparers
        IMPORT_MAPPING.put(FIELD_SPKM_LANG_E, ENG_COMPONENT_NAME_LANG_E);
        IMPORT_MAPPING.put(FIELD_SPKM_LANG_AS, COMPONENT_NAME_LANG_AS);
        IMPORT_MAPPING.put(FIELD_SPKM_CONNECTOR_AS, ENG_CONNECTOR_AS);
        IMPORT_MAPPING.put(FIELD_SPKM_CONNECTOR_E, ENG_CONNECTOR_E);
    }

    private String[] primaryKeys;
    private String seriesNo;
    private SpkMappingImportHelper importHelper;
    private Map<String, iPartsDataDictMeta> foundTextMap;    // Map für gefundene Lexikon-Einträge
    private SpkImportDictSearchHelper dictSearchHelper;
    private DictMetaTechChangeSetHelper dictChangeSetHelper; // Hilfsklasse für neu angelegte Lexikon-Einträge
    private iPartsDictTextKindId textKindId;
    private int insertedCount; // Zähler, wie viele Datensätze bei einer Erstbefüllung angelegt wurden
    private Set<String> importedData; // Set mit allen Ids die in einem Durchlauf schon einmal verarbeitet wurden


    public SpkMappingImporter(EtkProject project) {
        super(project, IMPORT_TITLE, TABLE_DA_SPK_MAPPING, true,
              new FilesImporterFileListType(TABLE_DA_SPK_MAPPING, IMPORT_TITLE, false,
                                            false, false,
                                            new String[]{ MimeTypes.EXTENSION_EXCEL_XLSX, MimeTypes.EXTENSION_EXCEL_XLS, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeys = new String[]{ SUPPLEMENTAL_HM_M, ENG_CONNECTOR_CODE_KURZ_E, ENG_COMPONENT_NAME_LANG_E, COMPONENT_CODE_KURZ_AS, COMPONENT_NAME_LANG_AS };
    }


    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeys);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        textKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.ADD_TEXT);
        if (textKindId == null) {
            cancelImport(translateForLog("Textart %1 nicht gefunden zum Laden des Lexikons", translateForLog(DictTextKindTypes.ADD_TEXT.getTextKindName())));
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setMaxEntriesForCommit(MIN_MEMORY_ROWS_LIMIT);
        progressMessageType = ProgressMessageType.READING;
        importedData = new HashSet<>();
        foundTextMap = new HashMap<>(); // Texte, die während dem Import im Lexikon gesucht wurden (damit man nicht mehrmals für den gleichen Text in die DB muss)
        // Helfer für den Import
        importHelper = new SpkMappingImportHelper(getProject(), TABLE_NAME);
        // Helfer für die Suche
        dictSearchHelper = new SpkImportDictSearchHelper(getProject(), textKindId, Language.DE);
        // Helfer für die ChangeSets
        dictChangeSetHelper = new DictMetaTechChangeSetHelper(getProject(), textKindId, Language.DE);
        createComparer(100, 500); // Schlüssel für dei Daten ist immer iPartsSpkMappingId als DB String
        if (saveToDB) {
            // Bestehende Daten zu Baureihe aus der DB laden und in die erste Liste legen. Der Schlüssel ist die
            // iPartsSpkMappingId als DB String und der Wert sind die Nutzdaten mit einem Trennzeichen verbunden. Nutzdaten
            // sind in der Tabelle die Langtexte (da nicht im Schlüssel) bzw. nur die deutschen Langtexte
            loadExistingDataFromDB();
        }
        // Wenn es keine Daten in der DB zur Baureihe gibt, dann ist es eine Erstbefüllung
        if (isInitialFilling()) {
            fireMessage("!!Erstbefüllung");
            clearEndMessageList();
            insertedCount = 0;
        } else {
            fireMessage("!!Einlesen der Daten aus Datei");
        }
    }

    @Override
    protected void setTotalDBCount() {
        // Die Anzahl von DB Objekten in der DB für die Baureihe bestimmen
        totalDBCount = getTotalDBCountForCondition(FIELD_SPKM_SERIES_NO, seriesNo);
    }

    @Override
    protected String[] getWhereFieldsForLoadExistingData() {
        return new String[]{ FIELD_SPKM_SERIES_NO };
    }

    @Override
    protected String[] getWhereValuesForLoadExistingData() {
        return new String[]{ seriesNo };
    }

    @Override
    protected String getDBLanguageForLoadExistingData() {
        return SEARCH_LANG.getCode();
    }

    /**
     * Die doppelt auszuwertenden Import-Felder setzen
     *
     * @param importRec
     */
    private void prepareImportRec(Map<String, String> importRec) {
        for (Map.Entry<String, String> entry : IMPORTREC_PREP_MAP.entrySet()) {
            importRec.put(entry.getKey(), importRec.get(entry.getValue()));
        }
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        prepareImportRec(importRec);
        HmMSmId hmMSmId = importHelper.getHmMSmId(SUPPLEMENTAL_HM_M, importRec);
        if (hmMSmId == null) {
            fireWarning("!!Record %1: %2. Wird übersprungen.", recordNo, "!!Ungültiger Hm/M-Knoten");
            return;
        }
        // Id aus den Importdaten bestimmen
        iPartsSpkMappingId spkId = importHelper.getSpkId(hmMSmId, ENG_CONNECTOR_CODE_KURZ_E, COMPONENT_CODE_KURZ_AS, ENG_STEERING, importRec);
        String dbKey = spkId.toDBString();
        // Gab es zu dieser Id schon einen Datensatz, dann gewinnt der erste und die restlichen werden nicht betrachtet (siehe DAIMLER-14463)
        if (importedData.contains(dbKey)) {
            increaseSkippedRecord();
            return;
        }
        if (!isInitialFilling()) {
            // Da wir schon Datensätze zur Baureihe in der DB haben, werden die Datensätze aus der Importdatei in die
            // zweite Liste gelegt. Der Schlüssel ist die iPartsSpkMappingId als DB String und der Wert sind die Nutzdaten
            // mit einem Trennzeichen verbunden. Nutzdaten sind in der Importdatei die Langtexte (da nicht im Schlüssel)
            // bzw. nur die deutschen Langtexte. Beim Hinzufügen, wird geprüft, ob der Key schon in der ersten Liste existiert.
            // ist das der Fall und die Nutzdaten sind unterschiedlich -> Datensatz kommt in eine Diff-Liste und ist
            // für ein potentielles Update relevant. Sind die Nutzdaten gleich, wird der Datensatz aus der ersten Liste
            // entfernt -> es passiert also nichts damit. Kommt der Key in der ersten Liste nicht vor, handelt es sich
            // um einen neuen Datensatz -> neu anlegen
            putSecond(spkId.toDBString(), importHelper.getImportDatasAsValue(importRec));
            importedData.add(dbKey);
        } else {
            // Hier handelt es sich um die Erstbefüllung (in der DB sind keine Records vorhanden)
            iPartsDataSpkMapping dataSpkMapping = new iPartsDataSpkMapping(getProject(), spkId);
            // Da Erstbefüllung und weil die Id geprüft wird, reicht es hier eifnach ein init zu machen ohne existsInDB()
            dataSpkMapping.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            setDatasFromRecord(dataSpkMapping, importRec, true);
            if (doSaveToDB(dataSpkMapping)) {
                importedData.add(dbKey);
                insertedCount++;
            }
        }

    }

    /**
     * Extrahiert aus dem Dateinamen die Baureihe
     *
     * @param importFileName
     * @return
     */
    private String extractSeriesFromFileName(String importFileName) {
        // Überprüfung importFileName
        if (StrUtils.isValid(importFileName)) {
            String firstPart = StrUtils.stringUpToLastCharacter(importFileName, " ");
            if (StrUtils.isValid(firstPart)) {
                String secondPart = StrUtils.stringUpToLastCharacter(firstPart, " ");
                if (StrUtils.isValid(secondPart)) {
                    secondPart = StrUtils.replaceSubstring(firstPart, secondPart + " ", "");
                }
                if (StrUtils.isValid(secondPart) && (secondPart.length() == 3)) {
                    // hier ggf Series überprüfen, ob vorhanden
                    if (!secondPart.startsWith(MODEL_NUMBER_PREFIX_CAR)) {
                        secondPart = MODEL_NUMBER_PREFIX_CAR + secondPart;
                    }
                    return secondPart;
                }
            }
        }
        return null;
    }

    private void fireWarning(String translationsKey, int recordNo, String secondTranslationsKey, String... placeHolderTexts) {
        String extraMsg = translateForLog(secondTranslationsKey, placeHolderTexts);
        String msg = translateForLog(translationsKey, String.valueOf(recordNo), extraMsg);
        fireWarning(msg);
    }

    private void setSpkMappingLongtextAS(iPartsDataSpkMapping dataSpkMapping, String longTextAS) {
        if (StrUtils.isValid(longTextAS)) {
            // Wenn es keine Erstbefüllung ist, prüfen, ob eine AS Langbenennung existiert. Ist das der Fall, wird diese
            // nicht überschrieben, auch wenn sie unterschiedlich ist (siehe Story)
            if (!isInitialFilling()) {
                String currentLongTextAS = dataSpkMapping.getFieldValueAsMultiLanguage(FIELD_SPKM_LANG_AS).getText(SEARCH_LANG.getCode());
                if (StrUtils.isValid(currentLongTextAS)) {
                    return;
                }
            }
            // Prüfen, ob der Text schon einmal während dem Import vorkam (Eintrag in foundTextMap)
            iPartsDataDictMeta dataDictMeta = foundTextMap.get(longTextAS);
            if (dataDictMeta != null) {
                // Kam bereits vor. Trage das MultiLang Objekt in das DatenObjekt ein
                dataSpkMapping.setFieldValueAsMultiLanguage(FIELD_SPKM_LANG_AS, dataDictMeta.getMultiLang(), DBActionOrigin.FROM_EDIT);
            } else {
                // Text kam noch nicht vor -> Suche im Lexikon
                dataDictMeta = dictSearchHelper.searchTextInDictionary(longTextAS);
                if (dataDictMeta == null) {
                    // text wurde nicht gefunden -> Neuanlage DictMeta damit der Text im Lexikon auftaucht
                    dataDictMeta = createAndInitDataDictMeta(longTextAS);
                    // DictMeta für späteres Speichern merken
                    updateCreatedDataDictMetaList(dataDictMeta, MAX_ENTRIES_FOR_TECH_CHANGE_SET);
                }
                // Trage das MultiLang Objekt in das DatenObjekt ein
                dataSpkMapping.setFieldValueAsMultiLanguage(FIELD_SPKM_LANG_AS, dataDictMeta.getMultiLang(), DBActionOrigin.FROM_EDIT);
                // DictMeta in foundTextMap merken, falles er nochmal vorkommt
                foundTextMap.put(longTextAS, dataDictMeta);
            }
        }
    }

    /**
     * Neuen Lexikon-Eintrag (iPartsDataDictMeta und EtkMultiSprache) anlegen
     *
     * @param cnLongtext
     * @return
     */
    private iPartsDataDictMeta createAndInitDataDictMeta(String cnLongtext) {
        EtkMultiSprache multiLang = new EtkMultiSprache();
        multiLang.setText(Language.DE, cnLongtext);
        multiLang.setTextId(DictHelper.buildIPARTSDictTextId());
        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(dictSearchHelper.getTextKindId().getTextKindId(), multiLang.getTextId());
        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
        dataDictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        String state = DictHelper.getMADDictStatus();
        dataDictMeta.setState(state, DBActionOrigin.FROM_EDIT);
        dataDictMeta.setSource(DICT_META_SOURCE, DBActionOrigin.FROM_EDIT);
        dataDictMeta.setActCreationDate(DBActionOrigin.FROM_EDIT);
        dataDictMeta.setUserId(iPartsUserAdminDb.getLoginUserName(), DBActionOrigin.FROM_EDIT);
        dataDictMeta.setNewMultiLang(multiLang);
        return dataDictMeta;
    }

    /**
     * Merken der neu erzeugten Lexikon-Einträge bzw. alles Speichern bei dataDictMeta == null und maxCount == 0
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
     * Da bei Update auch neue Records erzeugt werden können hier nochmal die Ausgabe der realen Werte
     *
     * @param counterContainer
     */
    protected void doAfterCompareAndSave(SaveCounterContainer counterContainer) {
        clearEndMessageList();

        // Neu angelegte Lexikon-Einträge im Tech ChangeSet speichern
        updateCreatedDataDictMetaList(null, 0);
        addToEndMessageList(buildEndMsg("!!%1 %2 gelöscht", "!!Datensatz", "!!Datensätze", counterContainer.deletedCount));
        addToEndMessageList(buildEndMsg("!!%1 %2 importiert", "!!Datensatz", "!!Datensätze", counterContainer.insertedCount));
        addToEndMessageList(buildEndMsg("!!%1 %2 aktualisiert", "!!Datensatz", "!!Datensätze", counterContainer.updatedCount));
        addDictEndMessages();
    }

    private String buildEndMsg(String key, String singleKey, String multipleKey, int count) {
        String placeHolder = multipleKey;
        if (count == 1) {
            placeHolder = singleKey;
        }
        return translateForLog(key, String.valueOf(count), translateForLog(placeHolder));
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
                if (!isInitialFilling()) {
                    compareAndSaveData(false);
                } else {
                    // Neu angelegte Lexikon-Einträge im Tech ChangeSet speichern
                    updateCreatedDataDictMetaList(null, 0);
                    addToEndMessageList("!!%1 Datensätze importiert", String.valueOf(insertedCount));
                    addDictEndMessages();
                    importedData.clear();
                }
                setClearCachesAfterImport(dictChangeSetHelper.getTotalDictMetaSavedCounter() > 0);
            }
        }
        cleanup();
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        String importFileName = importFile.extractFileName(false);
        // Series bestimmen
        seriesNo = extractSeriesFromFileName(importFileName);
        if (StrUtils.isEmpty(seriesNo)) {
            cancelImport(translateForLog("!!Dateiname \"%1\" enthält keine gültige Baureihe", importFileName));
            return false;
        }
        fireMessage("!!Import für Baureihe %1", seriesNo);
        // Excel-Import (unterscheidbar nur an der Methodensignatur)
        return importMasterData(prepareImporterKeyValue(importFile, TABLE_NAME, true, primaryKeys));
    }

    @Override
    protected void clearCaches() {
        // Alle Caches brauchen nicht gelöscht zu werden. Hier werden nur die Ergänzungstexte importiert/angepasst. Also
        // auch nur diesen Cache löschen
        if (dictChangeSetHelper.getTotalDictMetaSavedCounter() > 0) {
            // Lexikon-Cache löschen für die Textart Ergänzungstexte
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.ADD_TEXT));
        }
        super.clearCaches();
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
    protected EtkDataObject buildDataFromAttributes(DBDataObjectAttributes attributes) {
        // Aus den gefundenen Daten in loadExistingDataFromDB das richtige Datenobjekt erzeugen. Daraus wird später über
        // getAsId() der Eintrag für die erste Liste geamcht (erste Liste = Daten aus der DB)
        iPartsDataSpkMapping data = new iPartsDataSpkMapping(getProject(), null);
        data.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
        return data;
    }

    @Override
    protected String getValuesForListComp(EtkDataObject data) {
        // Aus den gefundenen Daten in loadExistingDataFromDB wird hier nun der Wert für die erste Liste erzeugt. Dieser
        // Wert sind die Nutzdaten des DB-Objekts.
        if (data instanceof iPartsDataSpkMapping) {
            // Überprüfen, ob der Text im Lexikon vorhanden ist
            EtkMultiSprache multiLang = data.getFieldValueAsMultiLanguage(FIELD_SPKM_LANG_AS);
            if (StrUtils.isValid(multiLang.getTextId())) {
                String cnLongText = multiLang.getText(SEARCH_LANG.getCode());
                if (StrUtils.isValid(cnLongText)) {
                    // Da wir hier den Langtext für AS schon haben, können wir ihn direkt in den Cache für die zu suchenden
                    // Texte (während dem Import) ablegen
                    iPartsDataDictMeta dataDictMeta = foundTextMap.get(cnLongText);
                    if (dataDictMeta == null) {
                        dataDictMeta = dictSearchHelper.searchTextInDictionary(cnLongText);
                        if (dataDictMeta != null) {
                            foundTextMap.put(cnLongText, dataDictMeta);
                        }
                    }
                    return getRecordDatasAsValue(data, cnLongText);
                }
            }
        }
        return "";
    }

    public String createCompareValue(List<String> values) {
        return StrUtils.stringListToString(values, DATA_DELIMITER);
    }

    public String getRecordDatasAsValue(EtkDataObject data, String cnLongText) {
        List<String> values = new DwList<>();
        DBDataObjectAttributes attributes = data.getAttributes();
        for (String fieldName : IMPORT_MAPPING.keySet()) {
            String value = "";
            if (fieldName.equals(FIELD_SPKM_LANG_E)) {
                value = data.getFieldValueAsMultiLanguage(fieldName).getText(SEARCH_LANG.getCode());
            } else if (fieldName.equals(FIELD_SPKM_LANG_AS)) {
                if (cnLongText == null) {
                    value = data.getFieldValueAsMultiLanguage(fieldName).getText(SEARCH_LANG.getCode());
                } else {
                    value = cnLongText;
                }
            } else {
                if (attributes.containsKey(fieldName)) {
                    value = attributes.getFieldValue(fieldName);
                }
            }
            values.add(value);
        }
        return createCompareValue(values);
    }

    public void setDatasFromValue(iPartsDataSpkMapping dataSpkMapping, String longTexts) {
        Map<String, String> importRec = new HashMap<>();
        List<String> longTextsList = StrUtils.toStringListContainingDelimiterAndBlanks(longTexts, DATA_DELIMITER, true);
        int lfdNr = 0;
        for (String recordName : IMPORT_MAPPING.values()) {
            String value = "";
            if (lfdNr < longTextsList.size()) {
                value = longTextsList.get(lfdNr);
            }
            importRec.put(recordName, value);
            lfdNr++;
        }
        setDatasFromRecord(dataSpkMapping, importRec, false);
    }

    public void setDatasFromRecord(iPartsDataSpkMapping dataSpkMapping, Map<String, String> importRec, boolean theseAreImportedDatas) {
        for (Map.Entry<String, String> entry : IMPORT_MAPPING.entrySet()) {
            String fieldName = entry.getKey();
            String recordName = entry.getValue();
            String value;
            if (theseAreImportedDatas) {
                value = importHelper.handleValueOfSpecialField(recordName, importRec);
            } else {
                value = importRec.get(recordName);
            }
            if (fieldName.equals(FIELD_SPKM_LANG_E)) {
                EtkMultiSprache multiLang_LangE = new EtkMultiSprache();
                multiLang_LangE.setText(Language.DE, value);
                dataSpkMapping.setFieldValueAsMultiLanguage(FIELD_SPKM_LANG_E, multiLang_LangE, DBActionOrigin.FROM_EDIT);
            } else if (fieldName.equals(FIELD_SPKM_LANG_AS)) {
                setSpkMappingLongtextAS(dataSpkMapping, value);
            } else {
                dataSpkMapping.setFieldValue(fieldName, value, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    @Override
    protected EtkDataObjectList<? extends EtkDataObject> getDataListForCompare() {
        return new iPartsDataSpkMappingList();
    }

    @Override
    protected EtkDataObject buildDataFromEntry(DiskMappedKeyValueEntry entry) {
        // Hier wird für alle drei Operationen (Insert, Delete, Update) aus einem Schlüssel/Wert Paar (DiskMappedKeyValueEntry)
        // das richtige DB-Objekt erzeugt
        iPartsSpkMappingId mappingId = iPartsSpkMappingId.getFromDBString(entry.getKey());
        if (mappingId != null) {
            return new iPartsDataSpkMapping(getProject(), mappingId);
        }
        return null;
    }

    @Override
    protected void addDataFromMappedKeyValueEntry(DiskMappedKeyValueEntry entry, EtkDataObject data) {
        // Hier werden dem in buildDataFromEntry() erzeugten Objekt die Nutzdaten gesetzt. Ebenfalls für alle drei Operationen
        // und aus dem übergebenen Schlüssel/Wert Paar (DiskMappedKeyValueEntry)
        if (data instanceof iPartsDataSpkMapping) {
            iPartsDataSpkMapping dataSpkMapping = (iPartsDataSpkMapping)data;
            setDatasFromValue(dataSpkMapping, entry.getValue());
        }
    }

    @Override
    protected boolean itemValidForUpdate(EtkDataObject item, DiskMappedKeyValueEntry entry) {
        if ((item instanceof iPartsDataSpkMapping) && item.existsInDB()) {
            return compareItemValues(item, entry);
        }
        return true;
    }

    private boolean compareItemValues(EtkDataObject item, DiskMappedKeyValueEntry keyValueEntry) {
        List<String> longTextsList = StrUtils.toStringListContainingDelimiterAndBlanks(keyValueEntry.getValue(), DATA_DELIMITER, true);
        int lfdNr = 0;
        for (String fieldName : IMPORT_MAPPING.keySet()) {
            String value = "";
            if (lfdNr < longTextsList.size()) {
                value = longTextsList.get(lfdNr);
            }
            if (fieldName.equals(FIELD_SPKM_LANG_E)) {
                // Ein bestehender Datensatz darf nur verändert werden, wenn sich die Langbenennung Entwicklung ändert.
                // Wenn diese gleich ist, dann hat sich die Langbenennung AS geändert. Diese Änderung soll aber laut Story
                // nicht berücksichtigt werden (Datensatz nicht aktualisieren, wenn die AS Langbenennung unterschiedlich ist)
                String dbValue = item.getFieldValueAsMultiLanguage(fieldName).getText(SEARCH_LANG.getCode());
                if (!StrUtils.stringEquals(value, dbValue)) {
                    return true;
                }
            } else if (fieldName.equals(FIELD_SPKM_LANG_AS)) {
            } else {
                String dbValue = item.getFieldValue(fieldName);
                if (!StrUtils.stringEquals(value, dbValue)) {
                    return true;
                }
            }
            lfdNr++;
        }
        return false;
    }

    private class SpkMappingImportHelper extends DIALOGImportHelper {

        public SpkMappingImportHelper(EtkProject project, String tableName) {
            super(project, new HashMap<>(), tableName);
        }

        public HmMSmId getHmMSmId(String sourceField, Map<String, String> importRec) {
            String value = handleValueOfSpecialField(sourceField, importRec);
            if (StrUtils.isValid(value) && (value.length() >= 4)) {
                HmMSmId hmMSmId = HmMSmId.getIdFromRaster(seriesNo, value);
                if (StrUtils.isValid(hmMSmId.getHm(), hmMSmId.getM())) {
                    // nochmal neu wegen Steering
                    hmMSmId = new HmMSmId(seriesNo, hmMSmId.getHm(), hmMSmId.getM(), "");
                    return hmMSmId;
                }
            }
            return null;
        }

        public iPartsSpkMappingId getSpkId(HmMSmId spkHmMSmId, String spkKurzESourceField, String spkKurzASSourceField,
                                           String spkSteeringField, Map<String, String> importRec) {
            return new iPartsSpkMappingId(spkHmMSmId, handleValueOfSpecialField(spkKurzESourceField, importRec),
                                          handleValueOfSpecialField(spkKurzASSourceField, importRec),
                                          handleValueOfSpecialField(spkSteeringField, importRec));
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            value = StrUtils.getEmptyOrValidString(value);
            // DAIMLER-15149: auch COMPONENT_CODE_KURZ_AS anpassen
            if (sourceField.equals(ENG_CONNECTOR_CODE_KURZ_E) || sourceField.equals(COMPONENT_CODE_KURZ_AS)) {
                if (StrUtils.stringContains(value, "*")) {
                    value = StrUtils.stringUpToCharacter(value, "*");
                }
            } else if (sourceField.equals(ENG_CONNECTOR_E)) {
                if (StrUtils.stringContains(value, "*")) {
                    String helper = StrUtils.stringAfterCharacter(value, "*");
                    if (StrUtils.stringContains(helper, "-")) {
                        value = StrUtils.stringUpToCharacter(helper, "-");
                    } else {
                        value = "";
                    }
                } else {
                    value = "";
                }
            } else if (sourceField.equals(ENG_STEERING)) {
                if (value.length() > 4) {
                    value = value.substring(4);
                } else {
                    value = "";
                }
            }
            return value;
        }

        /**
         * Liefert die verknüpften Nutzdaten aus dem übergebenen ImportRecord
         *
         * @param importRec
         * @return
         */
        public String getImportDatasAsValue(Map<String, String> importRec) {
            List<String> values = new DwList<>();
            for (String name : IMPORT_MAPPING.values()) {
                values.add(handleValueOfSpecialField(name, importRec));
            }
            return createCompareValue(values);
        }
    }

    protected static class SpkImportDictSearchHelper extends DictMetaSearchHelper {

        public SpkImportDictSearchHelper(EtkProject project, iPartsDictTextKindId textKindId, Language searchLang) {
            super(project, textKindId, searchLang);
            appendWhereFieldAndValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_SOURCE), DICT_META_SOURCE);
        }
    }
}
