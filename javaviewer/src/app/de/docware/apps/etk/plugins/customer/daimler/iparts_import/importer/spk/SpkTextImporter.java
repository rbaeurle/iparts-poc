/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.spk;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSpkMapping;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSpkMappingList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.helper.DictMetaTechChangeSetHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.AbstractSAXPushDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.BigDataXMLHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.DictMetaSearchHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.iPartsKeyValueRecordReader;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

public class SpkTextImporter extends AbstractSAXPushDataImporter implements iPartsConst {

    private static final String TYPE = "SpkTextImportType";
    private static final String IMPORT_TITLE = "!!SPK-Texte";
    private static final String DICT_META_SOURCE = iPartsImportDataOrigin.IPARTS_SPK.getOrigin();

    private static final String SPK_XML_ELEMENT = "SPK";
    private static final String SHORT_NAME_XML_ELEMENT = "ShortName";
    private static final String LONG_NAME_XML_ELEMENT = "LongName";
    private static final String PUE_XML_ELEMENT = "PUE";

/*
    <SPK id="PSK00000000000000000000000000001" state="g" company="MBAG">
    <ShortName>B11/4</ShortName>
    <LongName lang="de">Kühlmittel-Temperatursensor</LongName>
    <LongName lang="en">Coolant temperature sensor</LongName>
    <LongName lang="fr">Capteur de température du liquide de refroidissement</LongName>
    <LongName lang="zh">????????</LongName>
    <Validity>
    <Or>
    <PUE>47E26E7B76B84E669C7D647B3EDA62CE-197.377</PUE>
    <PUE>B5C7DD4F24A63EAC012569B8BA5B29C4-204.000</PUE>
    </Or>
    </Validity>
    </SPK>
*/

    private boolean saveToDB = true;
    private final boolean doBufferSave = true;
    private boolean deleteDictEntries = false;

    private SpkImportTextDictSearchHelper dictSearchHelper;
    private DictMetaTechChangeSetHelper dictChangeSetHelper; // Hilfsklasse für neu angelegte Lexikon-Einträge
    private SpkTextDataHandler dataHandler;
    private iPartsDictTextKindId textKindId;

    // Texte, die während des Imports im Lexikon gefunden/neu angelegt wurden, damit man nicht mehrmals für den gleichen Text in der DB suchen muss
    private Map<String, iPartsDataDictMeta> foundTextMap;    // Map für gefundene Lexikon-Einträge
    private Set<iPartsDictMetaId> possibleDictEntriesForDelete;    // Map für Lexikon-Einträge die ggf gelöscht werden können

    public SpkTextImporter(EtkProject project) {

        super(project, IMPORT_TITLE, null, new FilesImporterFileListType(TABLE_DA_SPK_MAPPING, IMPORT_TITLE,
                                                                         true, false, false,
                                                                         new String[]{ MimeTypes.EXTENSION_XML }));

        tableName = TABLE_DA_SPK_MAPPING;
    }

    protected String getType() {
        return TYPE;
    }

    @Override
    public boolean isAutoImport() {
        return false;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
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
        return importer.isRecordValid(importRec, errors);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
        // Texte, die während des Imports im Lexikon gefunden/neu angelegt wurden, damit man nicht mehrmals für den gleichen Text in der DB suchen muss
        foundTextMap = new HashMap<>();
        possibleDictEntriesForDelete = new HashSet<>();
        // Helfer für die Suche
        dictSearchHelper = new SpkImportTextDictSearchHelper(getProject(), textKindId, Language.DE);
        // Helfer für die ChangeSets
        dictChangeSetHelper = new DictMetaTechChangeSetHelper(getProject(), textKindId, Language.DE);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        // Tut hier nichts, das Speichern wird im Handler im onEndElement() erledigt.
    }

    @Override
    protected void postImportTask() {
        if (!cancelled) {
            setClearCachesAfterImport(true);
        }
        dataHandler.reset();
        super.postImportTask();
        // Neu angelegte Lexikon-Einträge im Tech ChangeSet speichern
        updateCreatedDataDictMetaList(null, 0);
    }

    private void skipDataSet(String message, MessageLogOption logOption) {
        reduceRecordCount();
        getMessageLog().fireMessage(translateForLog("!!Datensatz übersprungen. %1",
                                                    message),
                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, logOption);
        dataHandler.reset();
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

    /**
     * Übernahme der in der Importdatei zum ShortName passenden Baureihen, die in der Datenbank enthalten sind.
     *
     * @param textDataObject
     * @return
     */
    private iPartsDataSpkMappingList filterUsedSeries(SpkTextDataObject textDataObject) {
        // Alle zum ShortName passenden Baureihen-Mappings aus der Datenbank laden.
        iPartsDataSpkMappingList spkMappingList =
                iPartsDataSpkMappingList.loadMappingForShortNameAS(getProject(), textDataObject.getShortName());

        // Immer eine Liste anlegen, zur Not ist sie leer.
        iPartsDataSpkMappingList resultList = new iPartsDataSpkMappingList();

        // Nur übernehmen, wenn die Baureihe des Mappings aus der Datenbank zu den Importdaten passt.
        for (iPartsDataSpkMapping dbSpkMapping : spkMappingList) {
            String dbSeriesNo = dbSpkMapping.getAsId().getSeries();
            if (textDataObject.getSeriesValidity().contains(dbSeriesNo)) {
                resultList.add(dbSpkMapping, DBActionOrigin.FROM_DB);
            }
        }
        return resultList;
    }

    /**
     * Neues mehrsprachiges Objekt anlegen und mit den Daten aus der XML-Datei füttern
     *
     * @return
     */
    private EtkMultiSprache createNewEtkMultiSprach(EtkMultiSprache longNameText) {
        EtkMultiSprache obj = new EtkMultiSprache();
        obj.assign(longNameText);
        obj.setTextId(DictHelper.buildIPARTSDictTextId());

        return obj;
    }

    /**
     * Objekt für einen neuen Lexikoneintrag anlegen und füllen
     *
     * @param dbLangTextAS
     * @return
     */
    private iPartsDataDictMeta createDataDictMeta(EtkMultiSprache dbLangTextAS) {
        // Neuen Lexikoneintrag anlegen
        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), dbLangTextAS.getTextId());
        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
        dataDictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);

        String state = DictHelper.getMADDictStatus();
        dataDictMeta.setState(state, DBActionOrigin.FROM_EDIT);
        dataDictMeta.setSource(DICT_META_SOURCE, DBActionOrigin.FROM_EDIT);
        dataDictMeta.setActCreationDate(DBActionOrigin.FROM_EDIT);
        dataDictMeta.setUserId(iPartsUserAdminDb.getLoginUserName(), DBActionOrigin.FROM_EDIT);
        dataDictMeta.setNewMultiLang(dbLangTextAS);

        return dataDictMeta;
    }

    /**
     * Importiert einen "<SPK>" Datensatz
     */
    private void importCurrentSpkDataObject(SpkTextDataObject textDataObject) {
        if ((textDataObject == null) || !textDataObject.isEntryValid()) {
            if (textDataObject != null) {
                skipDataSet(translateForLog("!!Import-Datensatz %1 besitzt den \"ungültig\" Status", textDataObject.getShortName()),
                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
            return;
        }

        EtkMultiSprache importTextMulti = textDataObject.getLongNameMulti();
        // Nur loslaufen, wenn ein deutscher LongName im Importdatensatz enthalten ist.
        if (importTextMulti.containsLanguage(Language.DE, true)) {

            // Neue Arbeitsliste erzeugen mit allen Objekten aus der Datenbank, die zur den Baureihen in der seriesValidity passen.
            iPartsDataSpkMappingList usedSpkMappingList = filterUsedSeries(textDataObject);
            String importLangTextDE = importTextMulti.getText(Language.DE.getCode());

            // Über die vorgefilterte Arbeitsliste gehen.
            for (iPartsDataSpkMapping dbSpkMapping : usedSpkMappingList) {
                EtkMultiSprache dbLangTextAS = dbSpkMapping.getFieldValueAsMultiLanguage(FIELD_SPKM_LANG_AS);
                VarParam<Boolean> doSaveDictMeta = new VarParam<>(true);
                // Wenn der Text schon einmal gefunden wurde, das gemerkte DictMeta-Objekt verwenden.
                iPartsDataDictMeta dataDictMeta = foundTextMap.get(importLangTextDE);
                if (dataDictMeta != null) {
                    // Text wurde bereits benutzt
                    doSaveDictMeta.setValue(false);
                } else {
                    // Text kam noch nicht vor => check dbLangTextAS
                    if (StrUtils.isValid(dbLangTextAS.getTextId()) && dbLangTextAS.containsLanguage(Language.DE, true)) {
                        // es existiert bereits ein Lexikoneintrag => überprüfe den DE-Text
                        String dbTextDE = dbLangTextAS.getText(Language.DE.getCode());
                        if (dbTextDE.equalsIgnoreCase(importLangTextDE)) {
                            // vorhandener DE-Text ist bis auf Groß-/Kleinschreibung identisch mit importiertem DE-Text
                            iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), dbLangTextAS.getTextId());
                            dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
                            if (dataDictMeta.existsInDB()) {
                                EtkMultiSprache dest = dataDictMeta.getMultiLang();
                                if (!dest.getText(Language.DE.getCode()).equals(importLangTextDE)) {
                                    // Schreibweise von DE-Text ist unterschiedlich => alles übernehmen
                                    doSaveDictMeta.setValue(assignDifferentLanguageTexts(dest, importTextMulti, false));
                                } else {
                                    // Auffüllen der restlichen Sprachen
                                    doSaveDictMeta.setValue(assignDifferentLanguageTexts(dest, importTextMulti));
                                }
                                if (doSaveDictMeta.getValue()) {
                                    dataDictMeta.setNewMultiLang(dest);
                                }
                            } else {
                                // es gibt keinen Lexikoneintrag (sehr komisch) => Suche in Lexikon
                                dataDictMeta = searchDictOrCreateDictMeta(importLangTextDE, importTextMulti, false, doSaveDictMeta);
                            }

                        } else {
                            // DE-Text ist unterschiedlich => Text in Lexikon suchen oder neu anlegen
                            dataDictMeta = searchDictOrCreateDictMeta(importLangTextDE, importTextMulti, false, doSaveDictMeta);
                        }
                    } else {
                        // Suche in Lexikon oder neu anlegen
                        dataDictMeta = searchDictOrCreateDictMeta(importLangTextDE, importTextMulti, false, doSaveDictMeta);
                    }
                }
                // iPartsDataDictMeta-Objekt merken, falls es noch einmal vorkommen sollte.
                foundTextMap.put(importLangTextDE, dataDictMeta);
                EtkMultiSprache dictMetaLang = dataDictMeta.getMultiLang();
                dictMetaLang.removeLanguagesWithEmptyTexts();
                if (!dbSpkMapping.getFieldValueAsMultiLanguage(FIELD_SPKM_LANG_AS).equalContent(dictMetaLang)) {
                    dbSpkMapping.setFieldValueAsMultiLanguage(FIELD_SPKM_LANG_AS, dataDictMeta.getMultiLang(), DBActionOrigin.FROM_EDIT);
                }

                if (!doSaveDictMeta.getValue()) {
                    dataDictMeta = null;
                }
                doSaveToDB(dbSpkMapping, dataDictMeta);
            }
        }
    }

    private iPartsDataDictMeta searchDictOrCreateDictMeta(String importLangTextDE, EtkMultiSprache importTextMulti,
                                                          boolean searchCaseInsensitive, VarParam<Boolean> doSaveDictMeta) {
        iPartsDataDictMeta dataDictMeta = dictSearchHelper.searchTextInDictionary(importLangTextDE, searchCaseInsensitive);
        if (dataDictMeta != null) {
            EtkMultiSprache dest = dataDictMeta.getMultiLang();
            if (!dest.getText(Language.DE.getCode()).equals(importLangTextDE)) {
                // Schreibweise von DE-Text ist unterschiedlich => alles übernehmen
                doSaveDictMeta.setValue(assignDifferentLanguageTexts(dest, importTextMulti, false));
            } else {
                // Auffüllen der restlichen Sprachen
                doSaveDictMeta.setValue(assignDifferentLanguageTexts(dest, importTextMulti));
            }
            if (doSaveDictMeta.getValue()) {
                dataDictMeta.setNewMultiLang(dest);
            }
        } else {
            // Neuen Lexikoneintrag anlegen
            EtkMultiSprache dbLangTextAS = createNewEtkMultiSprach(importTextMulti);
            dataDictMeta = createDataDictMeta(dbLangTextAS);
            doSaveDictMeta.setValue(true);
        }
        return dataDictMeta;
    }

    private void doSaveToDB(iPartsDataSpkMapping dbSpkMapping, iPartsDataDictMeta dataDictMeta) {
        if (saveToDB) {
            saveToDB(dbSpkMapping);
            if (dataDictMeta != null) {
                // DictMeta für späteres Speichern merken
                updateCreatedDataDictMetaList(dataDictMeta, MAX_ENTRIES_FOR_TECH_CHANGE_SET);
            }
        }
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
                getMessageLog().fireMessage(translateForLog("!!Speichern der Lexikon-Einträge"), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
//                fireMessage("!!Speichern der Lexikon-Einträge");
            }
            if (!possibleDictEntriesForDelete.isEmpty()) {
                getMessageLog().fireMessage(translateForLog("!!Überprüfung von %1 Lexikon-Einträgen, ob löschbar", String.valueOf(possibleDictEntriesForDelete.size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                iPartsDataDictMetaList deleteList = new iPartsDataDictMetaList();
                iPartsDataDictTextKind dataTextKind = buildDataTextKind();
                for (iPartsDictMetaId dictMetaId : possibleDictEntriesForDelete) {
                    dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
                    if (dataDictMeta.existsInDB()) {
                        if (!dataTextKind.getUsages().isTextIdUsed(dictMetaId.getTextId()) /*!isTextIdUsed(dictMetaId.getTextId())*/) {
                            // kann gelöscht werden
                            deleteList.add(dataDictMeta, DBActionOrigin.FROM_EDIT);
                        }
                        if (isCancelled()) {
                            deleteList.clear(DBActionOrigin.FROM_DB);
                            break;
                        }
                    }
                }
                getMessageLog().fireMessage(translateForLog("!!Es können %1 Lexikon-Einträge gelöscht werden", String.valueOf(deleteList.size())),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
        }
        dictChangeSetHelper.updateCreatedDataDictMetaList(dataDictMeta, maxCount, saveToDB);
    }

    private iPartsDataDictTextKind buildDataTextKind() {
        iPartsDictTextKindId kindId = new iPartsDictTextKindId(textKindId.getTextKindId());
        iPartsDataDictTextKind dataTextKind = new iPartsDataDictTextKind(getProject(), kindId);
        if (!dataTextKind.getUsages().containsField(TableAndFieldName.make(TABLE_DA_SPK_MAPPING, FIELD_SPKM_LANG_AS))) {
            dataTextKind.addUsage(TABLE_DA_SPK_MAPPING, FIELD_SPKM_LANG_AS, iPartsUserAdminDb.getLoginUserName(), DBActionOrigin.FROM_DB);
        }
        return dataTextKind;
    }

    private boolean assignDifferentLanguageTexts(EtkMultiSprache dest, EtkMultiSprache longNameText) {
        return assignDifferentLanguageTexts(dest, longNameText, true);
    }

    /**
     * Übertrage alle unterschiedlichen, sprachabhängigen Texte außer deutsche Texte in das Ziel.
     *
     * @param dest
     */
    private boolean assignDifferentLanguageTexts(EtkMultiSprache dest, EtkMultiSprache longNameText, boolean skipLangDE) {
        boolean isModified = false;
        dest.removeLanguagesWithEmptyTexts();
        // Über alle Sprachen des zu importierenden Textobjekts gehen ...
        for (Language lang : longNameText.getLanguages()) {
            // ... DE überspringen ...
            if (skipLangDE && lang.getCode().equals(Language.DE.getCode())) {
                continue;
            }
            // ... und den Text am Zielobjekt setzen, wenn der Text unterschiedlich, oder nicht gesetzt ist.
            String importText = longNameText.getText(lang.getCode());
            if (!StrUtils.isValid(importText)) {
                continue;
            }
            String dbText = dest.getText(lang.getCode());

            if (StrUtils.isValid(dbText)) {
                if (!dbText.equals(importText)) {
                    dest.setText(lang, importText);
                    isModified = true;
                }
            } else {
                dest.setText(lang, importText);
                isModified = true;
            }
        }
        return isModified;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            dataHandler = new SpkTextDataHandler(this, new iPartsKeyValueRecordReader(importFile, tableName));
            return importMasterData(dataHandler);
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------
    // Das Datenobjekt für einen kompletten <SPK>-Datensatz
    // ------------------------------------------------------------------------------------------------------------------------------------

    private class SpkTextDataObject {

        /*
                 <SPK id="PSK00000000000000000000000000001" state="g" company="MBAG">
                 <ShortName>B11/4</ShortName>
                 <LongName lang="de">Kühlmittel-Temperatursensor</LongName>
                 <Validity>
                 <Or>
                 <PUE>47E26E7B76B84E669C7D647B3EDA62CE-197.377</PUE>
                 <PUE>B5C7DD4F24A63EAC012569B8BA5B29C4-204.000</PUE>
                 </Or>
                 </Validity>
                 </SPK>
        */
        private static final String VALID_VALUE = "g";

        private String spkId;
        private String validFlag;
        private String shortName;
        private EtkMultiSprache longNameMulti;
        private Set<String> seriesValidity;

        public SpkTextDataObject() {
            longNameMulti = new EtkMultiSprache();
            seriesValidity = new HashSet<>();
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName.toUpperCase();
        }

        public EtkMultiSprache getLongNameMulti() {
            return longNameMulti;
        }

        public void setLongNameMulti(EtkMultiSprache multi) {
            longNameMulti = multi;
        }

        public void setLongNameText(String lang, String text) {
            longNameMulti.setText(Language.findLanguage(lang), text);
        }

        public Set<String> getSeriesValidity() {
            return seriesValidity;
        }

        public void setSeriesValidity(Set<String> seriesValidity) {
            this.seriesValidity = seriesValidity;
        }

        public String getSpkId() {
            return spkId;
        }

        public void setSpkId(String spkId) {
            this.spkId = spkId;
        }

        public void setValidFlag(String validFlag) {
            this.validFlag = validFlag;
        }

        public boolean isEntryValid() {
            return StrUtils.isValid(validFlag) && validFlag.equalsIgnoreCase(VALID_VALUE);
        }

        public void addModelNo(String pueString) {
            // Die ModelNo aus dem übergebenen Gesamtstring ausschneiden und vorne ein 'C' dranstellen.
            // B5C7DD4F2385EEB001240F3D41200E16-117.301 ==> C117
            // 47E26E7B76B84E669C7D647B3EDA62CE-197.377 ==> C197
            // B5C7DD4F24A63EAC012569B8BA5B29C4-204.000 ==> C204
            String modelNo = StrUtils.stringBetweenStrings(pueString, "-", ".");
            if (StrUtils.isValid(modelNo)) {
                seriesValidity.add(MODEL_NUMBER_PREFIX_CAR + modelNo);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------
    // HANDLER
    // ------------------------------------------------------------------------------------------------------------------------------------

    private class SpkTextDataHandler extends BigDataXMLHandler {

        private static final String ATT_SPK_ID = "id";
        private static final String ATT_SPK_STATE = "state";
        private static final String ATT_LANG = "lang";

        private SpkTextDataObject currentTextDataObject;
        private String currentLongNameLang;

        public SpkTextDataHandler(AbstractSAXPushDataImporter importer, AbstractKeyValueRecordReader reader) {
            super(importer, reader);
            reset();
        }

        @Override
        protected void onStartElement(String uri, String localName, String qName, Map<String, String> attributes) {
            if (qName.equals(SPK_XML_ELEMENT) && isValid(attributes)) {
                // Aktuellen erst im onEndElement() speichern
                currentTextDataObject = new SpkTextDataObject();
                currentTextDataObject.setSpkId(getAttributeValue(ATT_SPK_ID, attributes));
                currentTextDataObject.setValidFlag(getAttributeValue(ATT_SPK_STATE, attributes));
            } else if (qName.equals(SHORT_NAME_XML_ELEMENT)) {
                // NOOP
            } else if (qName.equals(LONG_NAME_XML_ELEMENT) && isValid(attributes)) {
                currentLongNameLang = getCurrentTextLang(attributes);
            } else if (qName.equals(PUE_XML_ELEMENT) && isValid(attributes)) {
                // NOOP
            }
        }

        @Override
        protected void onTextElement(String tagName, char[] ch, int start, int length) {
            // Der übersetze Text ist für eine Liste an Baureihen gültig, die aus dem PUE-String extrahiert werden müssen.
            if (currentTextDataObject != null) {
                if (ch.length > 0) {
                    if (tagName.equals(SHORT_NAME_XML_ELEMENT)) {
                        currentTextDataObject.setShortName(new String(ch, start, length));
                    } else if (tagName.equals(LONG_NAME_XML_ELEMENT)) {
                        currentTextDataObject.setLongNameText(currentLongNameLang, new String(ch, start, length));
                    } else if (tagName.equals(PUE_XML_ELEMENT)) {
                        currentTextDataObject.addModelNo(new String(ch, start, length));
                    }
                }
            }
        }

        @Override
        protected void onEndElement(String uri, String localName, String qName) {

            if (qName.equals(SPK_XML_ELEMENT)) {
                // Aktuellen Datensatz entweder hier speichern, oder schon im onStartElement()
                importCurrentSpkDataObject(currentTextDataObject);
                reset();
            }
        }

        public String getCurrentTextLang(Map<String, String> attributes) {
            return getAttributeValue(ATT_LANG, attributes);
        }

        public boolean isValid(Map<String, String> attributes) {
            return attributesLoaded(attributes);
        }

        public void reset() {
            currentTextDataObject = null;
        }
    }

    protected static class SpkImportTextDictSearchHelper extends DictMetaSearchHelper {

        public SpkImportTextDictSearchHelper(EtkProject project, iPartsDictTextKindId textKindId, Language searchLang) {
            super(project, textKindId, searchLang);
            appendWhereFieldAndValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_SOURCE), DICT_META_SOURCE);
        }
    }
}
