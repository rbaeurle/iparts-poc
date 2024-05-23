/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc.helper.EPCImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.iPartsMigrationFootnotesHandler;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;

import java.util.*;

/**
 * EPC Importer für SAA-Stammdaten (SA_STROKES.)
 */
public class EPCSaaMasterDataImporter extends AbstractEPCDataImporter implements iPartsConst, EtkDbConst {

    private static final String SAA_SANUM = "SANUM";
    private static final String SAA_SEQNUM = "SEQNUM";
    private static final String SAA_STRKVER = "STRKVER";
    private static final String SAA_MODEL = "MODEL";
    private static final String SAA_CONSAS = "CONSAS";
    private static final String SAA_SUBMOD = "SUBMOD";
    private static final String SAA_DESCIDX = "DESCIDX";
    private static final String SAA_FNOTES = "FNOTES";
    private static final String SAA_IS_COMPONENT = "IS_COMPONENT";

    private static final String DEST_TABLENAME = TABLE_DA_SAA; //TABLE_DA_EDS_SAA_MODELS;// Die Zieltabelle

    private Set<String> allModelNumbers;
    private Set<String> invalidModelNumbers;
    private Set<String> notUniqueModelNumbers;
    private Set<String> saNumbers;
    private Map<String, EtkMultiSprache> descriptionMap;
    private String currentSANumber;
    private Map<iPartsSaaId, iPartsDataSaa> currentSaaMap;
    private Set<iPartsSaaId> createdSaas;
    private Map<String, List<String>> doubleModelNoMap;
    private iPartsMigrationFootnotesHandler fnMigrationHandler;
    private Map<String, iPartsFootNoteId> fnCountMap;

    private boolean withCreateSA = false; // soll eine SA angelegt werden, falls sie nicht existiert
    private boolean withCreateSAA = true; // soll eine SAA angelegt werden, falls sie nicht existiert
    private boolean withSearchInProductModelTab = false; // bei mehrdeutigen ModelNumbers: Suche in Product-BM-Tabelle

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    protected EPCSaaMasterDataImporter(EtkProject project) {
        super(project, "EPC SAA-Masterdata", "!!EPC SAA-Masterdata (SA_STROKES)", DEST_TABLENAME, true, true);
    }

    @Override
    protected HashMap<String, String> initMapping() {
        // Hier kein Mapping notwendig, da alle Werte im Schlüssel sind
        return new HashMap<>();
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{
                SAA_SANUM,
                SAA_SEQNUM,
                SAA_STRKVER,
                SAA_MODEL,
                SAA_CONSAS,
                SAA_SUBMOD,
                SAA_DESCIDX,
                SAA_FNOTES,
                SAA_IS_COMPONENT
        };
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        String[] mustFields = new String[]{ SAA_SANUM, SAA_SEQNUM };
        importer.setMustExists(mustFields);
        importer.setMustHaveData(mustFields);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        setBufferedSave(doBufferSave);
        allModelNumbers = new HashSet<>();
        iPartsDataModelList modelList = iPartsDataModelList.loadAllDataModelList(getProject(), DBDataObjectList.LoadType.ONLY_IDS);
        for (iPartsDataModel dataModel : modelList) {
            allModelNumbers.add(dataModel.getAsId().getModelNumber());
        }

        invalidModelNumbers = new HashSet<>();
        notUniqueModelNumbers = new HashSet<>();
        doubleModelNoMap = new HashMap<>();
        saNumbers = new HashSet<>();
        descriptionMap = new HashMap<>();
        currentSANumber = "";
        fnMigrationHandler = new iPartsMigrationFootnotesHandler(getProject(), this, iPartsDataFootNote.FOOTNOTE_PREFIX_EPC,
                                                                 iPartsImportDataOrigin.EPC);
        fnCountMap = new HashMap<>();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        SaaMasterDataImportHelper importHelper = new SaaMasterDataImportHelper(getProject(), importRec);
        String saNumber = importHelper.handleValueOfSpecialField(SAA_SANUM, importRec);
        if (!handleSaData(importHelper, importRec, recordNo, saNumber)) {
            return;
        }
        String saaVersion = importHelper.handleValueOfSpecialField(SAA_STRKVER, importRec);
        iPartsDataSaa saaData = checkCreateSaaNumber(importHelper, importRec, recordNo, saNumber, saaVersion);
        if (saaData == null) {
            reduceRecordCount();
            return;
        }
        // Baumusterbeziehungen bestimmen
        Set<String> modelNumbersWithPrefix = getModelNumberWithPrefix(importHelper, importRec, recordNo);

        // Fußnoten anlegen
        updateCreateSAAFootNotes(importHelper, saaData, saNumber, importRec, recordNo);

        // Verknüpfungen anlegen
        for (String modelNumber : modelNumbersWithPrefix) {
            iPartsSAAModelsId saaModelsId = new iPartsSAAModelsId(saaData.getAsId(), new iPartsModelId(modelNumber));
            iPartsDataSAAModels dataSAAModel = new iPartsDataSAAModels(getProject(), saaModelsId);
            if (!dataSAAModel.existsInDB()) {
                dataSAAModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                dataSAAModel.setFieldValue(FIELD_DA_ESM_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
                saveToDB(dataSAAModel);
            }
        }
        if (!createdSaas.contains(saaData.getAsId())) {
            saveToDB(saaData);
            createdSaas.add(saaData.getAsId());
        } else {
            reduceRecordCount();
        }
    }

    private void updateCreateSAAFootNotes(EPCImportHelper importHelper, iPartsDataSaa saaData, String saNumber,
                                          Map<String, String> importRec, int recordNo) {
        List<String> fnEPCidList = getEPCFootnoteIds(importHelper, importRec, recordNo);
        if (!fnEPCidList.isEmpty()) {
            // Beispiel: saNumber: "Z 10406", saaNumber: "Z 1040608", fnEPCId: "010"
            // iParts-spezifische Fußnotennummern zusammenbauen: "EPC_Z 10406_010"
            Set<iPartsFootNoteId> footNoteIdList = fnMigrationHandler.handleFootnotesForSA(fnEPCidList, "", saNumber, true);
            if (!footNoteIdList.isEmpty()) {
                int lfdNr = 0;
                for (iPartsFootNoteId footNoteId : footNoteIdList) {
                    String epcFnNumber = fnEPCidList.get(lfdNr);
                    iPartsFootNoteId storedFootNoteId = fnCountMap.get(epcFnNumber);
                    if ((storedFootNoteId == null) || storedFootNoteId.isEmpty()) {
                        // Suchen in DA_FN_CONTENT, ob hier scho so eine Fußnote existiert.
                        iPartsDataFootNoteContentList footNoteContentList = iPartsDataFootNoteContentList.loadFootNote(getProject(), footNoteId.getFootNoteId());
                        if (!footNoteContentList.isEmpty()) {
                            // Existiert bereits: in DA_FN_SAA_REF den Eintrag mit „Z 1040608“ und „EPC_Z 10406_010“ anlegen
                            createAndSaveFootNoteSAARef(saaData.getAsId().getSaaNumber(), footNoteId.getFootNoteId(), lfdNr + 1);
                            fnCountMap.put(epcFnNumber, footNoteId);
                        } else {
                            // kein Eintrag in DA_FN_CONTENT
                            // Mit „Z 10406“ und „010“ in DA_EPC_FN_SA_REF nach der Original-TextId suchen -> 11112
                            iPartsEPCFootNoteSaRefId refId = new iPartsEPCFootNoteSaRefId(saNumber, epcFnNumber);
                            iPartsDataEPCFootNoteSaRef dataEPCFootNoteSaRef = new iPartsDataEPCFootNoteSaRef(getProject(), refId);
                            if (dataEPCFootNoteSaRef.existsInDB()) {
                                String epcTextId = dataEPCFootNoteSaRef.getFieldValue(FIELD_DEFS_TEXT_ID);
                                // In DA_EPC_FN_CONTENT mit „SA“ und „11112“ nach der Text ID aus dem Lexikon suchen -> EPC_FN_SA.11112
                                iPartsEPCFootNoteContentId contentId = new iPartsEPCFootNoteContentId(EPCFootnoteType.SA, epcTextId, 1);
                                iPartsDataEPCFootNoteContent dataEPCFootNoteContent = new iPartsDataEPCFootNoteContent(getProject(), contentId);
                                if (dataEPCFootNoteContent.existsInDB()) {
                                    EtkMultiSprache multi = dataEPCFootNoteContent.getFieldValueAsMultiLanguage(FIELD_DEFC_TEXT);
                                    // IN DA_FN_CONTENT einen Eintrag anlegen mit „EPC_Z 10406_010“,Zeilennummer „00001“ (ist immer die gleiche Nummer, da EPC Fußnoten nicht Zeilenweise kommen), Text = EPC_FN_SA.11113 und sprachneutraler Text ist leer
                                    createAndSaveFootNoteContent(footNoteId.getFootNoteId(), multi);
                                    // In DA_FN_SAA_REF den Eintrag mit „Z 1040611“ und „EPC_Z 10406_010“ anlegen
                                    createAndSaveFootNoteSAARef(saaData.getAsId().getSaaNumber(), footNoteId.getFootNoteId(), lfdNr + 1);
                                    // In DA_FN den Eintrag mit „EPC_Z 10406_010“ und "010" anlegen
                                    createAndSaveFootNote(footNoteId.getFootNoteId(), epcFnNumber);
                                } else {
                                    footNoteId = new iPartsFootNoteId();
                                }
                            } else {
                                // Meldung
                                footNoteId = new iPartsFootNoteId();
                            }
                            fnCountMap.put(epcFnNumber, footNoteId);
                        }
                    } else {
                        // In DA_FN_SAA_REF den Eintrag mit „Z 1040611“ und „EPC_Z 10406_011“ anlegen
                        createAndSaveFootNoteSAARef(saaData.getAsId().getSaaNumber(), storedFootNoteId.getFootNoteId(), lfdNr + 1);
                    }
                    lfdNr++;
                }
            }
        }
    }

    private void createAndSaveFootNoteContent(String footNoteId, EtkMultiSprache multi) {
        iPartsFootNoteContentId id = new iPartsFootNoteContentId(footNoteId, EtkDbsHelper.formatLfdNr(1));
        iPartsDataFootNoteContent dataFootNoteContent = new iPartsDataFootNoteContent(getProject(), id);
        if (!dataFootNoteContent.existsInDB()) {
            dataFootNoteContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        dataFootNoteContent.setFieldValueAsMultiLanguage(FIELD_DFNC_TEXT, multi, DBActionOrigin.FROM_EDIT);
        saveToDB(dataFootNoteContent);
    }

    private void createAndSaveFootNoteSAARef(String saaNumber, String footNoteId, int seqNo) {
        iPartsFootNoteSaaRefId saaRefId = new iPartsFootNoteSaaRefId(saaNumber, footNoteId);
        iPartsDataFootNoteSaaRef dataFootNoteSaaRef = new iPartsDataFootNoteSaaRef(getProject(), saaRefId);
        if (!dataFootNoteSaaRef.existsInDB()) {
            dataFootNoteSaaRef.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        dataFootNoteSaaRef.setFieldValue(FIELD_DFNS_FN_SEQNO, EtkDbsHelper.formatLfdNr(seqNo), DBActionOrigin.FROM_EDIT);
        saveToDB(dataFootNoteSaaRef);
    }

    private void createAndSaveFootNote(String footNoteId, String epcFnNumber) {
        iPartsFootNoteId fnId = new iPartsFootNoteId(footNoteId);
        iPartsDataFootNote dataFootNote = new iPartsDataFootNote(getProject(), fnId);
        if (!dataFootNote.existsInDB()) {
            dataFootNote.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        dataFootNote.setFieldValue(FIELD_DFN_NAME, epcFnNumber, DBActionOrigin.FROM_EDIT);
        saveToDB(dataFootNote);
    }

    private List<String> getEPCFootnoteIds(EPCImportHelper importHelper, Map<String, String> importRec, int recordNo) {
        String fnString = importHelper.handleValueOfSpecialField(SAA_FNOTES, importRec);
        if (StrUtils.isValid(fnString)) {
            if ((fnString.length() % 3) == 0) {
                return StrUtils.splitStringIntoSubstrings(fnString, 3);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1: Fehler in den Importdaten. FNotes konnten nicht interpretiert werden: %2. Sie werden nicht importiert",
                                                            String.valueOf(recordNo), fnString),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            }
        }
        return new DwList<>();
    }

    /**
     * Verarbeitet SA-abhängige Daten, wie z.B. das Laden von allen SAAs zu einer SA oder das optionale Anlegen von
     * SAs.
     *
     * @param importHelper
     * @param importRec
     * @param recordNo
     * @param saNumber
     * @return
     */
    private boolean handleSaData(EPCImportHelper importHelper, Map<String, String> importRec, int recordNo, String saNumber) {
        if (!StrUtils.isValid(saNumber)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültige SA-Nummer \"%2\" übersprungen",
                                                        String.valueOf(recordNo), saNumber),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return false;
        }
        if (!saNumbers.contains(saNumber)) {
            // SA-Nummer bisher beim Import noch nicht aufgetaucht
            if (withCreateSA) { // Nur anlegen, wenn erlaubt
                if (!createSa(importHelper, importRec, saNumber, recordNo)) {
                    return false;
                }
            }
            // SA-Nummer merken
            saNumbers.add(saNumber);
        }
        if (!currentSANumber.equals(saNumber)) {
            // bei SA-Nummern Wechsel: alle SAA's zu dieser SA laden, falls vorhanden
            iPartsDataSaaList saaList = iPartsDataSaaList.loadAllSaasForSa(getProject(), saNumber);
            currentSaaMap = new HashMap<>();
            createdSaas = new HashSet<>();
            for (iPartsDataSaa dataSaa : saaList) {
                currentSaaMap.put(dataSaa.getAsId(), dataSaa);
            }
            currentSANumber = saNumber;
        }
        return true;
    }

    /**
     * Legt eine SA mit der Quelle "EPC" an.
     *
     * @param importHelper
     * @param importRec
     * @param saNumber
     * @param recordNo
     * @return
     */
    private boolean createSa(EPCImportHelper importHelper, Map<String, String> importRec, String saNumber, int recordNo) {
        if (withCreateSA) {
            // Anlegen einer SA, falls nicht vorhanden
            boolean saveSA = importHelper.isSARelevantForImport(this, saNumber, recordNo, false);
            if (saveSA) {
                String epcTextId = importHelper.handleValueOfSpecialField(SAA_DESCIDX, importRec);
                EtkMultiSprache epcText = importHelper.searchEPCTextWithEPCId(DictTextKindEPCTypes.SA_DICTIONARY, epcTextId);

                if (StrUtils.isValid(epcTextId)) {
                    iPartsSaId saId = new iPartsSaId(saNumber);
                    iPartsDataSa saData = new iPartsDataSa(getProject(), saId);
                    if (!saData.existsInDB()) {
                        saData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                        saData.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
                    } else {
                        // MultiLang nachladen
                        saData.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
                    }
                    setSaFieldValues(saData, epcText);
                    saveToDB(saData);
                } else {
//                        getMessageLog().fireMessage(translateForLog("!!Record %1 Werte nicht gültig um einen" +
//                                                                    " Datensatz zu erzeugen. SA: %2, EPC TextId: %3",
//                                                                    String.valueOf(recordNo), saNumber, epcTextId),
//                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
//                    reduceRecordCount();
                    return false;
                }
            }
        }
        return true;
    }

    private void setSaFieldValues(iPartsDataSa saData, EtkMultiSprache epcText) {
        saData.setFieldValueAsMultiLanguage(FIELD_DS_DESC, epcText, DBActionOrigin.FROM_EDIT);
        saData.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
        String aDat = saData.getFieldValue(FIELD_DS_ADAT);
        if (saData.isNew() || aDat.isEmpty() || !aDat.equals(saData.getFieldValue(FIELD_DS_EDAT))) {
            String currentTime = DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss);
            saData.setFieldValue(FIELD_DS_ADAT, currentTime, DBActionOrigin.FROM_EDIT);
            saData.setFieldValue(FIELD_DS_EDAT, currentTime, DBActionOrigin.FROM_EDIT);
        }
    }


    private iPartsDataSaa checkCreateSaaNumber(EPCImportHelper importHelper, Map<String, String> importRec, int recordNo, String saNumber, String saaVersion) {
        if (StrUtils.isEmpty(saaVersion)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültigen Strichausführungen \"%2\" übersprungen",
                                                        String.valueOf(recordNo), saaVersion),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return null;
        }
        String currentSAANumber = saNumber + saaVersion;
        iPartsSaaId saaId = new iPartsSaaId(currentSAANumber);
        boolean existsInDB;
        // SAA-Eintrag bereits in DB vorhanden?
        iPartsDataSaa saaData = currentSaaMap.get(saaId);
        if (saaData == null) {
            saaData = new iPartsDataSaa(getProject(), saaId);
            saaData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            existsInDB = false;
        } else {
            existsInDB = true;
        }
        if (existsInDB) {
            // Alle Daten nur importieren, falls die entsprechende SA nicht aus MAD migriert wurde.
            iPartsImportDataOrigin saaSource = iPartsImportDataOrigin.getTypeFromCode(saaData.getFieldValue(FIELD_DS_SOURCE));

            // Für SAs zu denen bereits Daten aus MAD migriert wurden, werden keine Daten importiert
            if ((saaSource != iPartsImportDataOrigin.EPC) && (saaSource != iPartsImportDataOrigin.APP_LIST) &&
                (saaSource != iPartsImportDataOrigin.UNKNOWN)) {
//                getMessageLog().fireMessage(translateForLog("!!Record %1 SAA-Nummer \"%2\" mit Quelle \"%3\"" +
//                                                            " wird nicht überschrieben!", String.valueOf(recordNo),
//                                                            currentSAANumber, saaSource.getOrigin()),
//                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP,
//                                            MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return null;
            } else if (saaSource == iPartsImportDataOrigin.APP_LIST) {
                // SAAs werden von der Applikationsliste eigentlich nicht angelegt. Sollte das in Zukunft aber passieren,
                // wären wir hier auf der sicheren Seite.
                saaData.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
                return saaData;
            }
        }

        if (withCreateSAA) {
            // SAA-Eintrag anlegen
            if (existsInDB) {
                // MultiLang nachladen
                saaData.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
            }
//            saaData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            String epcTextId = importHelper.handleValueOfSpecialField(SAA_DESCIDX, importRec);

            if (StrUtils.isValid(epcTextId)) {
                EtkMultiSprache epcText = descriptionMap.get(epcTextId);
                if (epcText == null) {
                    epcText = importHelper.searchEPCTextWithEPCId(DictTextKindEPCTypes.SA_DICTIONARY, epcTextId);
                    if (epcText == null) {
                        epcText = new EtkMultiSprache();
                    }
                    descriptionMap.put(epcTextId, epcText);
                }
                String connectedSAs = importHelper.handleValueOfSpecialField(SAA_CONSAS, importRec);
                if (!connectedSAs.isEmpty()) {
                    if ((connectedSAs.length() % 6) != 0) {
                        getMessageLog().fireMessage(translateForLog("!!Fehler in den Importdaten. Connected SAs konnten nicht interpretiert werden: %1. Sie werden nicht importiert",
                                                                    connectedSAs),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                        connectedSAs = "";
                    }
                    List<String> connectedSAsList = StrUtils.splitStringIntoSubstrings(connectedSAs, 6);
                    connectedSAs = StrUtils.stringListToString(connectedSAsList, EDS_CONNECTED_SAS_DELIMITER);
                }
                setSaaFieldValues(saaData, connectedSAs, epcText);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1 Werte nicht gültig um einen" +
                                                            " Datensatz zu erzeugen. SAA: %2, EPC TextId: %3",
                                                            String.valueOf(recordNo), currentSAANumber, epcTextId),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
//                    reduceRecordCount();
                return null;
            }
        } else {
            // damit später nicht gesepichert wird
            currentSaaMap.put(saaId, saaData);
        }
        return saaData;
    }

    private void setSaaFieldValues(iPartsDataSaa saaData, String connectedSAs, EtkMultiSprache epcText) {
        saaData.setFieldValueAsMultiLanguage(FIELD_DS_DESC, epcText, DBActionOrigin.FROM_EDIT);
        saaData.setFieldValue(FIELD_DS_CONNECTED_SAS, connectedSAs, DBActionOrigin.FROM_EDIT);
        saaData.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EPC.getOrigin(), DBActionOrigin.FROM_EDIT);
        String aDat = saaData.getFieldValue(FIELD_DS_ADAT);
        if (saaData.isNew() || aDat.isEmpty() || !aDat.equals(saaData.getFieldValue(FIELD_DS_EDAT))) {
            String currentTime = DateUtils.getCurrentDateFormatted(DateUtils.simpleTimeFormatyyyyMMddHHmmss);
            saaData.setFieldValue(FIELD_DS_ADAT, currentTime, DBActionOrigin.FROM_EDIT);
            saaData.setFieldValue(FIELD_DS_EDAT, currentTime, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Liefert die existierenden Baumuster aus dem Import-Record samt "C"- oder "D"-Prefix.
     *
     * @param importHelper
     * @param importRec
     * @param recordNo
     * @return
     */
    private Set<String> getModelNumberWithPrefix(EPCImportHelper importHelper, Map<String, String> importRec, int recordNo) {
        Set<String> modelNumbersWithPrefix = new HashSet<>();
        Set<String> invalidModelNumbersForRecord = new TreeSet<>();
        String currentSeriesNumber = importHelper.handleValueOfSpecialField(SAA_MODEL, importRec);
        String currentModelNumbersWithoutSeries = importHelper.handleValueOfSpecialField(SAA_SUBMOD, importRec);
        Set<String> modelNumbersWithoutPrefix = new HashSet<>();
        importHelper.combineSeriesAndModelNumbersForSAA(this, modelNumbersWithoutPrefix, currentSeriesNumber, currentModelNumbersWithoutSeries);
        for (String modelNoWithoutPrefix : modelNumbersWithoutPrefix) {
            if (invalidModelNumbers.contains(modelNoWithoutPrefix) || notUniqueModelNumbers.contains(modelNoWithoutPrefix)) {
                // bereits als nicht vorhandene Model-Number identifiziert
                // für die Anzeige im Log
                invalidModelNumbersForRecord.add(modelNoWithoutPrefix);
                continue;
            }
            String carModelNo = iPartsModel.MODEL_NUMBER_PREFIX_CAR + modelNoWithoutPrefix;
            String aggModelNo = iPartsModel.MODEL_NUMBER_PREFIX_AGGREGATE + modelNoWithoutPrefix;
            boolean carModelExists = allModelNumbers.contains(carModelNo);
            boolean aggModelExists = allModelNumbers.contains(aggModelNo);
            String validModelNumber = "";
            if (carModelExists && aggModelExists) {
                if (withSearchInProductModelTab) {
                    if (handleBothModelTypesExist(carModelNo, aggModelNo, modelNoWithoutPrefix, modelNumbersWithPrefix, currentModelNumbersWithoutSeries, recordNo)) {
                        continue;
                    }
                } else {
                    // C- und D-Baumuster existieren => Meldung ausgeben und als invalid merken
                    getMessageLog().fireMessage(translateForLog("!!Record %1: Doppelbelegung Baumusternummer %2 (C und D). Wird ignoriert. (SUBMOD: %3):",
                                                                String.valueOf(recordNo),
                                                                modelNoWithoutPrefix, currentModelNumbersWithoutSeries),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }
            } else if (carModelExists) {
                // es gibt nur ein C-Baumuster -> als Ergebnis merken
                validModelNumber = carModelNo;
            } else if (aggModelExists) {
                // es gibt ein D-Baumuster -> als Ergebnis merken
                validModelNumber = aggModelNo;
            }
            if (StrUtils.isValid(validModelNumber)) {
                modelNumbersWithPrefix.add(validModelNumber);
                continue;
            }

            // alle oben nicht erkannten Baumuster-Nummern merken als invalid
            if (!invalidModelNumbersForRecord.contains(modelNoWithoutPrefix)) {
                invalidModelNumbersForRecord.add(modelNoWithoutPrefix);
                invalidModelNumbers.add(modelNoWithoutPrefix);
            }
        }
        if (!invalidModelNumbersForRecord.isEmpty()) {
            // Sammelmeldung mit den ungültigen Baumuster-Nummer für einen Import-Record
            StringBuilder str = iPartsMainImportHelper.buildNumberListForLogFile(invalidModelNumbersForRecord);
            getMessageLog().fireMessage(translateForLog("!!Record %1: Folgende Baumusternummern sind nicht vorhanden (%2: %3, %4: %5):",
                                                        String.valueOf(recordNo),
                                                        SAA_MODEL, currentSeriesNumber,
                                                        SAA_SUBMOD, currentModelNumbersWithoutSeries) +
                                        str.toString(),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }

        return modelNumbersWithPrefix;
    }

    /**
     * Bestimmen des Baumuster Prefix über die DA_PRODUCT_MODEL Tabelle, wenn beide Varianten möglich wären (C oder D).
     *
     * @param carModelNo
     * @param aggModelNo
     * @param modelNoWithoutPrefix
     * @param modelNumbersWithPrefix
     * @param currentModelNumbersWithoutSeries
     * @param recordNo
     * @return
     */
    private boolean handleBothModelTypesExist(String carModelNo, String aggModelNo, String modelNoWithoutPrefix,
                                              Set<String> modelNumbersWithPrefix, String currentModelNumbersWithoutSeries,
                                              int recordNo) {
        if (withSearchInProductModelTab) {
            // Suche in ProductModel-Tabelle
            List<String> doubleModelNoList = doubleModelNoMap.get(modelNoWithoutPrefix);
            if (doubleModelNoList == null) {
                doubleModelNoList = new DwList<>();
                // Doppelbelegung
                iPartsDataProductModelsList carProductModelsList = iPartsDataProductModelsList.loadDataProductModelsList(getProject(), new iPartsModelId(carModelNo));
                iPartsDataProductModelsList aggProductModelsList = iPartsDataProductModelsList.loadDataProductModelsList(getProject(), new iPartsModelId(aggModelNo));
                String foundModel = "";
                if (carProductModelsList.isEmpty() && !aggProductModelsList.isEmpty()) {
                    // Aggregatebaumuster gefunden
                    foundModel = aggModelNo;
                } else if (!carProductModelsList.isEmpty() && aggProductModelsList.isEmpty()) {
                    // Fahrzeugbaumuster gefunden
                    foundModel = carModelNo;
                }
                // Baumuster gefunden -> nächstes Baumuster
                if (StrUtils.isValid(foundModel)) {
                    modelNumbersWithPrefix.add(aggModelNo);
                    doubleModelNoList.add(aggModelNo);
                    doubleModelNoMap.put(modelNoWithoutPrefix, doubleModelNoList);
                    return true;
                }

                // kein Baumuster gefunden oder beide Baumuster gefunden
                if (carProductModelsList.isEmpty() && aggProductModelsList.isEmpty()) {
                    // keine Bestimmung möglich => Meldung + merken
                    getMessageLog().fireMessage(translateForLog("!!Record %1: Baumuster \"%2\" wird wegen Doppelbelegung ignoriert. (SUBMOD: %3):",
                                                                String.valueOf(recordNo),
                                                                modelNoWithoutPrefix, currentModelNumbersWithoutSeries),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    notUniqueModelNumbers.add(modelNoWithoutPrefix);
                    return false;
                } else if (!carProductModelsList.isEmpty() && !aggProductModelsList.isEmpty()) {
                    // echte Doppelbelegung => Ausgabe + getrennt merken
                    getMessageLog().fireMessage(translateForLog("!!Record %1: Baumusternummer %2 wird doppelt angelegt. (SUBMOD: %3):",
                                                                String.valueOf(recordNo),
                                                                modelNoWithoutPrefix, currentModelNumbersWithoutSeries),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    modelNumbersWithPrefix.add(carModelNo);
                    modelNumbersWithPrefix.add(aggModelNo);
                    doubleModelNoList.add(carModelNo);
                    doubleModelNoList.add(aggModelNo);
                    doubleModelNoMap.put(modelNoWithoutPrefix, doubleModelNoList);
                    return true;
                }
            } else {
                modelNumbersWithPrefix.addAll(doubleModelNoList);
            }
        }
        return false;
    }

    @Override
    protected void postImportTask() {
        super.postImportTask();
        allModelNumbers = null;
        invalidModelNumbers = null;
        descriptionMap = null;
    }

    /**
     * Ruft saveToDB auf und korrigiert skippedRecords (reduceRecordCount() in saveToDB())
     *
     * @param dataObject
     */
    @Override
    public boolean saveToDB(EtkDataObject dataObject) {
        if (importToDB) {
            boolean isBuffered = super.saveToDB(dataObject);
            if (!(dataObject instanceof iPartsDataSaa) && !isBuffered) {
                skippedRecords--;
            }
            return isBuffered;
        }
        return false;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (isRemoveAllExistingData()) {
            iPartsDataSaaList saaList = iPartsDataSaaList.loadAllSaasForSource(getProject(), iPartsImportDataOrigin.EPC);
            if (!saaList.isEmpty()) {
                Set<String> saNumberList = new HashSet<>();
                for (iPartsDataSaa dataSaa : saaList) {
                    saNumberList.add(StrUtils.copySubString(dataSaa.getAsId().getSaaNumber(), 0, 7));
                }
                DBDataObjectList deleteList = new DBDataObjectList();
                for (String saNumber : saNumberList) {
                    iPartsDataFootNoteSaaRefList footNoteSaaRefList = new iPartsDataFootNoteSaaRefList();
                    footNoteSaaRefList.loadFootNotesForAllSaasOfSaFromDB(getProject(), saNumber);
                    for (iPartsDataFootNoteSaaRef dataFootNoteSaaRef : footNoteSaaRefList) {
                        if (dataFootNoteSaaRef.getAsId().getFootNoteId().startsWith(iPartsDataFootNote.FOOTNOTE_PREFIX_EPC + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER)) {
                            deleteList.delete(dataFootNoteSaaRef, true, DBActionOrigin.FROM_EDIT);
                        }
                    }
                    if (deleteList.getDeletedList().size() > 1000) {
                        deleteList.saveToDB(getProject(), true);
                        deleteList.clear(DBActionOrigin.FROM_DB);
                    }
                }
                // Lösche alle Fußnoteneinträge (außer Standard-Fußnoten)
                deleteList.saveToDB(getProject(), true);
            }
            // alle Einträge von EPC aus TABLE_DA_EDS_SAA_MODELS löschen
            getProject().getDbLayer().delete(TABLE_DA_EDS_SAA_MODELS, new String[]{ FIELD_DA_ESM_SOURCE },
                                             new String[]{ iPartsImportDataOrigin.EPC.getOrigin() });
            saaList.deleteAll(DBActionOrigin.FROM_EDIT);
            saaList.saveToDB(getProject());
        }
        return true;
    }

    private class SaaMasterDataImportHelper extends EPCImportHelper {

        private SaaMasterDataImportHelper(EtkProject project, Map<String, String> mapping) {
            super(project, mapping, DEST_TABLENAME);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (value.equals(EPC_NULL_VALUE)) {
                value = "";
            }
            if (sourceField.equals(SAA_SANUM)) {
                value = makeSANumberFromEPCValue(value);
            }
            return value;
        }
    }
}
