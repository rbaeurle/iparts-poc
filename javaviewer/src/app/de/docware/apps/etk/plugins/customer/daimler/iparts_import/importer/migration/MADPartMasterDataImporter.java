/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTermIdHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.iPartsMaterialImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.sql.TableAndFieldName;
import org.apache.commons.collections4.map.LRUMap;

import java.util.*;

/**
 * MAD Teilestammdaten Importer
 */
public class MADPartMasterDataImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    private static final int MAX_ENTRIES_FOR_COMMIT = 100;

    final static String TSTM_SNR = "TSTM_SNR";
    final static String TSTM_SPRN = "TSTM_SPRN";
    final static String TSTM_EHM = "TSTM_EHM";
    final static String TSTM_ETKZ = "TSTM_ETKZ";
    final static String TSTM_FARBKZ = "TSTM_FARBKZ";
    final static String TSTM_BEND = "TSTM_BEND";
    final static String TSTM_TEXT_ID = "TSTM_TEXT_ID";
    final static String TSTM_TERM_ID = "TSTM_TERM_ID";

    private String[] headerNames = new String[]{
            TSTM_SNR,
            TSTM_SPRN,
            TSTM_EHM,
            TSTM_ETKZ,
            TSTM_FARBKZ,
            TSTM_BEND,
            TSTM_TEXT_ID,
            TSTM_TERM_ID };

    private HashMap<String, String> mappingPartData;
    private String[] primaryKeysPartImport;
    private String tableName = TABLE_MAT;
    private String retailFieldName = FIELD_M_TEXTNR;
    private List<String> allWarnings = new ArrayList<String>();

    private Map<String, EtkMultiSprache> partMasterDataIdCache;
    private Map<String, EtkMultiSprache> partMasterDataTextCache;
    private Map<String, EtkMultiSprache> partMasterDataNeutralCache;
    private Set<String> invalidIds;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MADPartMasterDataImporter(EtkProject project) {
        super(project, "MAD Teilestamm",
              new FilesImporterFileListType(TABLE_MAT, "!!MAD Teilestammdaten", true, false, false,
                                            new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysPartImport = new String[]{ TSTM_SNR };
        mappingPartData = new HashMap<>();
//        mappingPartData.put(FIELD_M_MATNR, TSTM_SNR);
//        mappingPartData.put(FIELD_M_ADDTEXT, TSTM_SPRN);
//        mappingPartData.put(FIELD_M_TEXTNR, TSTM_BEND);
        mappingPartData.put(FIELD_M_QUANTUNIT, TSTM_EHM);
//        mappingPartData.put(FIELD_M_ETKZ, TSTM_ETKZ); // DAIMLER-8902: MAD-Teilstamm-Import darf das Feld MAT.M_ETKZ nicht mehr beschreiben
//        mappingPartData.put(FIELD_M_VARIANT_SIGN, TSTM_FARBKZ);
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        partMasterDataIdCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_IMPORTER);
        partMasterDataTextCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_IMPORTER);
        partMasterDataNeutralCache = new LRUMap(iPartsConst.MAX_CACHE_SIZE_IMPORTER);
        invalidIds = new HashSet<>();
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
            if (!invalidIds.isEmpty()) {
                StringBuilder str = new StringBuilder();
                str.append(translateForLog("!!Folgende Fremd-Ids sind nicht im Lexikon vorhanden:"));
                str.append("\n");
                int lfdNr = 1;
                for (String invalidId : invalidIds) {
                    str.append(invalidId);
                    if ((lfdNr % 10) == 0) {
                        str.append("\n");
                    } else {
                        str.append(", ");
                    }
                    lfdNr++;
                }
                getMessageLog().fireMessage(str.toString(), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);

            }
        }

        super.postImportTask();
        partMasterDataIdCache = null;
        partMasterDataTextCache = null;
        partMasterDataNeutralCache = null;
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();

        // Nur ein Cache-WarmUp am Ende ausführen, weil NEUTRAL_TEXT definitiv im WarmUp enthalten ist
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.NEUTRAL_TEXT, false));
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.MAT_NAME, true));
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysPartImport, new String[]{ TSTM_TERM_ID, TSTM_TEXT_ID, TSTM_SPRN }));
        importer.setMustHaveData(primaryKeysPartImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessage(getMessageLog(), getLogLanguage(),
                                                                             EnumSet.of(DictTextKindTypes.NEUTRAL_TEXT, DictTextKindTypes.MAT_NAME),
                                                                             EnumSet.of(DictTextKindRSKTypes.MAT_AFTER_SALES),
                                                                             null, null, null)) {
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        allWarnings.clear();
        PartImportHelper helper = new PartImportHelper(getProject(), mappingPartData, tableName);
        // Sachnummer halten, damit der Importer nicht mehrmals die Nummer konvertieren muss
        String partNo = helper.handleValueOfSpecialField(TSTM_SNR, importRec);
        iPartsPartId id = new iPartsPartId(partNo, "");
        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), id);
        if (!part.existsInDB()) {
            part.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        // Verarbeite mögliche ES1 und ES2 Schlüssel an der Teilenummer
        DIALOGImportHelper.handleESKeysInDataPart(getProject(), part);

        // M_BESTNR immer befüllen, da dieses Feld in der Stückliste angezeigt wird
        part.setFieldValue(FIELD_M_BESTNR, partNo, DBActionOrigin.FROM_EDIT);

        // Teilestamm in DB mit neuen Daten überschreiben
        helper.fillOverrideCompleteDataForMADReverse(part, importRec, iPartsMADLanguageDefs.MAD_DE);

        // DIALOG-importierte Werte dürfen von MAD nicht überschrieben werden.
        if (!part.containsFieldValueSetOfEnumValue(FIELD_M_SOURCE, iPartsImportDataOrigin.DIALOG.getOrigin())) {
            String variantSign = helper.handleValueOfSpecialField(TSTM_FARBKZ, importRec);
            part.setFieldValue(FIELD_M_VARIANT_SIGN, variantSign, DBActionOrigin.FROM_EDIT);
        }

        // Source
        part.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);

        boolean dictSuccessful = true;
        DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
        // DAIMLER-8326: in Edit gepflegten neutralText nicht überschreiben
        boolean isModifiedByEdit = part.getFieldValueAsBoolean(FIELD_M_ADDTEXT_EDITED);
        if (!isModifiedByEdit) {
            // Sprachneutralen Text hinzufügen
            // die MultiLangSprache aus den Importdaten bestimmen
            EtkMultiSprache multiEdit = part.getFieldValueAsMultiLanguage(FIELD_M_ADDTEXT);
            String newText = helper.handleValueOfSpecialField(TSTM_SPRN, importRec);
            if (multiEdit == null) {
                //kein Eintrag vorhanden
                multiEdit = new EtkMultiSprache();
            }
            if (!isValueAlreadyInNeutralCache(newText, part)) {
                if (!newText.isEmpty()) {
                    if (!checkMultiLangValues(multiEdit, newText)) {
                        // Setzen des neutralen Feldes von Hand, da es auch leer sein kann (leer bedeutet in den Originaldaten "(null)")
                        multiEdit.clear();
                        multiEdit.setText(iPartsMADLanguageDefs.MAD_DE.getDbValue().getCode(), newText);
                        // Dictionary Eintrag anlegen bzw. aktualisieren
                        dictSuccessful = importHelper.handleDictTextId(DictTextKindTypes.NEUTRAL_TEXT, multiEdit, "", DictHelper.getMADForeignSource(),
                                                                       true, TableAndFieldName.make(tableName, FIELD_M_ADDTEXT));
                        // Weil Warnungen bei jeden "handleDictTextId" aufruf gelöscht werden -> Zwischenspeichern der Warnungen
                        if (importHelper.hasWarnings()) {
                            // Wenn Warnungen aufkommen, dann setze keinen Text
                            allWarnings.addAll(importHelper.getWarnings());
                            clearMultiLangField(part, FIELD_M_ADDTEXT);
                        } else {
                            partMasterDataNeutralCache.put(newText, multiEdit);
                            // Speichern des Sprachneutralen Textes
                            //zur Sicherheit die TextNr ebenfalls auf TextId setzen
                            String textNr = part.getFieldValue(FIELD_M_ADDTEXT);
                            if (textNr.isEmpty() || !textNr.equals(multiEdit.getTextId())) {
                                part.setFieldValue(FIELD_M_ADDTEXT, multiEdit.getTextId(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                            }
                            part.setFieldValueAsMultiLanguage(FIELD_M_ADDTEXT, multiEdit, DBActionOrigin.FROM_EDIT);
                        }
                    }
                } else {
                    clearMultiLangField(part, FIELD_M_ADDTEXT);
                }

            }
        }
        // Falls es schon beim Import der Sprachneutralen Texte einen Fehler gab (Textart nicht vorhanden), dann brauch man nicht weiter importieren
        // Und nur Texte übernehmen, wenn am Teielstamm keine SRM Textverknüpfung stattgefunden hat
        if (dictSuccessful) {
            if (!iPartsMaterialImportHelper.hasSRMTextId(part)) {
                // Teilebenennung hinzufügen
                String newText = helper.handleValueOfSpecialField(TSTM_BEND, importRec);
                EtkMultiSprache multiEdit = part.getFieldValueAsMultiLanguage(retailFieldName);
                if (multiEdit == null) {
                    //kein Eintrag vorhanden
                    multiEdit = new EtkMultiSprache();
                }
                String foreignID = helper.handleValueOfSpecialField(TSTM_TERM_ID, importRec);
                String textId = helper.handleValueOfSpecialField(TSTM_TEXT_ID, importRec);
                boolean isRSKKey = true;
                // Wenn Term-ID nicht existiert -> nimm Text-ID
                if (StrUtils.isEmpty(foreignID)) {
                    isRSKKey = false;
                    foreignID = helper.handleValueOfSpecialField(TSTM_TEXT_ID, importRec);
                    textId = "";
                }
                dictSuccessful = handleDictionaryEntry(part, dictSuccessful, importHelper, multiEdit, newText, foreignID, textId, isRSKKey, recordNo);
            } else {
                getMessageLog().fireMessage(translateForLog("!!Record %1: Bennenung für Teilenummer %2 konnte nicht übernommen werden, da sie schon von SRM gesetzt wurde!",
                                                            String.valueOf(recordNo), part.getAsId().getMatNr()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }

        }
        if (!dictSuccessful || (allWarnings.size() > 0)) {
            //Fehler beim Dictionary Eintrag
            for (String str : allWarnings) {
                getMessageLog().fireMessage(translateForLog("!!Record %1: \"%2\"", String.valueOf(recordNo), str),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }

            if (!dictSuccessful) {
                // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                cancelImport();
                return;
            }
        }

        if (importToDB) {
            if (saveToDB(part)) {
                autoCommitAfterMaxEntries(MAX_ENTRIES_FOR_COMMIT);
            }
        }
    }

    private boolean handleDictionaryEntry(EtkDataPart part, boolean dictSuccessful, DictImportTextIdHelper importHelper,
                                          EtkMultiSprache multiEdit, String newText, String foreignID, String textId, boolean isRSKKey, int recordNo) {
        if (!isValueAlreadyInNameCache(foreignID, newText, part, recordNo)) {
            if (!foreignID.isEmpty() || !newText.isEmpty()) {
                if (!checkMultiLangValues(multiEdit, newText, foreignID)) {
                    if (multiEdit.getText(iPartsMADLanguageDefs.MAD_DE.getDbValue().getCode()).isEmpty()) {
                        multiEdit.setText(iPartsMADLanguageDefs.MAD_DE.getDbValue().getCode(), newText);
                    }

                    // Falls keine Term-ID vorhanden ist, suche in MAD Lexikon (nach Text-ID oder eigentlichen Text)
                    if (isRSKKey) {
                        dictSuccessful = importHelper.handleDictTextId(DictTextKindRSKTypes.MAT_AFTER_SALES, multiEdit, foreignID, DictHelper.getRSKForeignSource(),
                                                                       TableAndFieldName.make(tableName, retailFieldName));
                    } else {
                        dictSuccessful = importHelper.handleDictTextId(DictTextKindTypes.MAT_NAME, multiEdit, foreignID, DictHelper.getMADForeignSource(),
                                                                       false, TableAndFieldName.make(tableName, retailFieldName));
                    }
                    if (dictSuccessful) {
                        if (importHelper.hasWarnings()) {
                            // Wenn Warnungen aufkommen, dann setze keinen Text
                            if (isRSKKey && !textId.isEmpty()) {
                                for (String str : importHelper.getWarnings()) {
                                    getMessageLog().fireMessage(translateForLog("!!Record %1: \"%2\"", String.valueOf(recordNo), str),
                                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                                }
                                getMessageLog().fireMessage(translateForLog("!!Record %1: Versuche Text-Id %2.", String.valueOf(recordNo), textId),
                                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                                isRSKKey = false;
                                return handleDictionaryEntry(part, dictSuccessful, importHelper, multiEdit, newText, textId, "", isRSKKey, recordNo);
                            } else {
                                allWarnings.addAll(importHelper.getWarnings());
                                invalidIds.add(foreignID);
                            }
                            clearMultiLangField(part, retailFieldName);
                        } else {
                            setIDCacheValue(foreignID, newText, multiEdit);
                            //zur Sicherheit die TextNr ebenfalls auf TextId setzen
                            String textNr = part.getFieldValue(retailFieldName);
                            if (textNr.isEmpty() || !textNr.equals(multiEdit.getTextId())) {
                                part.setFieldValue(retailFieldName, multiEdit.getTextId(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                            }
                            part.setFieldValueAsMultiLanguage(retailFieldName, multiEdit, DBActionOrigin.FROM_EDIT);
                        }
                    }
                } else {
                    setIDCacheValue(foreignID, newText, multiEdit);
                }
            } else {
                clearMultiLangField(part, retailFieldName);
            }
        }
        return dictSuccessful;
    }

    private void clearMultiLangField(EtkDataPart part, String fieldname) {
        EtkMultiSprache multiEdit = part.getFieldValueAsMultiLanguage(fieldname);
        if (multiEdit == null || !multiEdit.isEmpty()) {
            // es sind Werte in der Sprache, oder das Feld wurde noch nicht geladen
            // Lösche die Einträge im Feld und speichere das.
            multiEdit = new EtkMultiSprache();
            part.setFieldValueAsMultiLanguage(fieldname, multiEdit, DBActionOrigin.FROM_EDIT);
        }
    }


    private boolean checkMultiLangValues(EtkMultiSprache multiEdit, String newText, String foreignID) {
        boolean result = checkMultiLangValues(multiEdit, newText);
        if (result) {
            result = DictHelper.getDictId(multiEdit.getTextId()).equals(foreignID);
        }
        return result;
    }

    private boolean checkMultiLangValues(EtkMultiSprache multiEdit, String text) {
        if (StrUtils.isEmpty(multiEdit.getTextId())) {
            return false;
        }
        if (!multiEdit.getText(Language.DE.getCode()).equals(text)) {
            return false;
        }
        return true;
    }

    /**
     * Überprüft, ob der Wert zum Schlüssel "newText" schon im Cache für sprachneutrale Texte liegt. Falls ja, wird dem
     * Teilestamm das bestehende {@link EtkMultiSprache} Objekt übergeben
     *
     * @param newText
     * @param part
     * @return
     */
    private boolean isValueAlreadyInNeutralCache(String newText, EtkDataPart part) {
        if (StrUtils.isEmpty(newText)) {
            return false;
        }
        EtkMultiSprache multiEdit = partMasterDataNeutralCache.get(newText);
        if (multiEdit == null) {
            return false;
        } else {
            EtkMultiSprache addTextMultiLang = part.getFieldValueAsMultiLanguage(FIELD_M_ADDTEXT);
            if ((addTextMultiLang == null) || !addTextMultiLang.equalContent(multiEdit)) {
                part.setFieldValueAsMultiLanguage(FIELD_M_ADDTEXT, multiEdit, DBActionOrigin.FROM_EDIT);
            }

        }
        return true;
    }

    /**
     * Überprüft, ob der Wert zum Schlüssel "foreignId" oder "newText" (falls foreignID leer ist) schon im Cache für AS Benennungen liegt. Falls ja, wird dem
     * Teilestamm das bestehende {@link EtkMultiSprache} Objekt übergeben
     *
     * @param foreignID
     * @param newText
     * @param part
     * @param recordNo
     * @return
     */
    private boolean isValueAlreadyInNameCache(String foreignID, String newText, EtkDataPart part, int recordNo) {
        if (foreignID.isEmpty() && newText.isEmpty()) {
            return false;
        }
        EtkMultiSprache multiEdit = partMasterDataIdCache.get(foreignID);
        if (multiEdit == null) {
            multiEdit = partMasterDataTextCache.get(newText);
        }
        if (multiEdit == null) {
            if (invalidIds.contains(foreignID)) {
                String str = translateForLog("!!Fehlender Lexikon-Eintrag für die Fremd-Id %1 (darf auch nicht neu angelegt werden)", foreignID);
                getMessageLog().fireMessage(translateForLog("!!Record %1: \"%2\"", String.valueOf(recordNo), str),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                return true;
            }
            return false;
        } else {
            EtkMultiSprache textNrMultiLang = part.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR);
            if ((textNrMultiLang == null) || !textNrMultiLang.equalContent(multiEdit)) {
                //zur Sicherheit die TextNr ebenfalls auf TextId setzen
                String textNr = part.getFieldValue(FIELD_M_TEXTNR);
                if (textNr.isEmpty() || !textNr.equals(multiEdit.getTextId())) {
                    part.setFieldValue(FIELD_M_TEXTNR, multiEdit.getTextId(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                }
                part.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, multiEdit, DBActionOrigin.FROM_EDIT);
            }
        }
        return true;
    }

    private void setIDCacheValue(String foreignID, String newText, EtkMultiSprache multiEdit) {
        if (!foreignID.isEmpty()) {
            partMasterDataIdCache.put(foreignID, multiEdit);
        } else {
            if (!newText.isEmpty()) {
                partMasterDataTextCache.put(newText, multiEdit);
            } else {
                partMasterDataTextCache.put(multiEdit.getText(Language.DE.getCode()), multiEdit);
            }
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNames));
            } else if (MimeTypes.hasExtension(importFile, "del")) {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNames));
            } else {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', DWFileCoding.ISO_8859_15, true, null));
            }
        }
        return false;
    }


    private class PartImportHelper extends MADImportHelper {

        public PartImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (StrUtils.stringContains(value, MAD_NULL_VALUE)) {
                value = StrUtils.replaceSubstring(value, MAD_NULL_VALUE, "").trim();
            }

            if (sourceField.equals(TSTM_SNR)) {
                value = checkNumberInputFormat(value, getMessageLog());
            } else if (sourceField.equals(TSTM_TERM_ID)) {
                if (value.equals("0")) {
                    value = "";
                } else {
                    value = iPartsTermIdHandler.removeLeadingZerosFromTermId(value);
                }
            } else if (sourceField.equals(TSTM_TEXT_ID)) {
                value = iPartsTermIdHandler.removeLeadingZerosFromTermId(value);
            } else if (sourceField.equals(TSTM_EHM)) {
                if (value.isEmpty()) {
                    value = QUANTUNIT_STUECK;
                }
            }
            return value;
        }
    }
}