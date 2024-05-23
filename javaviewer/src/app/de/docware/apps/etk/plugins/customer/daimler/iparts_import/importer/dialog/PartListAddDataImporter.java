/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog;

import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDialogDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogAddData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogAddDataList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogAddDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsDIALOGLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDIALOGDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataSet;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.ConditionList;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.Tables;

import java.util.*;

public class PartListAddDataImporter extends AbstractDIALOGDataImporter implements iPartsConst {

    // Die Zusatzdaten zur Konstruktionsstückliste aus DIALOG (VBCA/VBRT)
    public static final String VBCA_PREFIX = "VBCA";
    public static final String VBRT_PREFIX = "VBRT";
    public static final String IMPORT_TABLENAME_VBCA = TABLE_NAME_PREFIX + VBCA_PREFIX;
    public static final String IMPORT_TABLENAME_VBRT = TABLE_NAME_PREFIX + VBRT_PREFIX;

    // Die zu importierenden Spalten der Konstruktionsstückliste
    public static final String BR = "BR";
    public static final String RAS = "RAS";
    public static final String POSE = "POSE";
    public static final String PV = "PV";
    public static final String WW = "WW";
    public static final String ETZ = "ETZ";
    public static final String SDATA = "SDATA";
    public static final String SDATB = "SDATB";
    public static final String ERGK = "ERGK";
    public static final String SPRN = "SPRN";
    public static final String STR = "STR";
    public static final String CODE = "CODE";
    public static final String ITEXT = "ITEXT";

    public static final String VBRT_EREIA = "VBRT_EREIA";  // nur in VBRT
    public static final String VBRT_EREIB = "VBRT_EREIB";

    private final String importTableInXML;
    private String prefixForImporterInstance;
    private String[] primaryKeysImport;
    private String tableName;
    private HashMap<String, String> mapping;
    private HashSet<Long> adatAsLongSet;
    private Map<String, iPartsDataDialogAddData> dataWithHighestAdat;
    private DictImportTextIdHelper dictImportTextIdHelper;
    private Map<iPartsDialogAddDataId, String> objectToTextID;
    private Map<iPartsDialogAddDataId, iPartsDataDialogAddData> objectsToStore;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean doBufferdSave = true; //sollen die Daten abgespeichert werden?

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public PartListAddDataImporter(EtkProject project, String xmlTableName) {
        super(project, "Invalid Importer");
        // Tabellenname aus der XML Datei
        this.importTableInXML = xmlTableName;
        initImporter();
    }

    private void initImporter() {
        mapping = new HashMap<>();
        prefixForImporterInstance = "";
        String nameForImport = "";
        tableName = TABLE_DA_DIALOG_ADD_DATA;
        // Unterscheidung VBCA - VBRT
        if (importTableInXML.equals(IMPORT_TABLENAME_VBCA)) {
            prefixForImporterInstance = VBCA_PREFIX + "_";
            nameForImport = DD_PL_ADD_DATA;
            importName = "!!Zusatzdaten zu Konstruktionsstückliste (VBCA)";
        } else if (importTableInXML.equals(IMPORT_TABLENAME_VBRT)) {
            prefixForImporterInstance = VBRT_PREFIX + "_";
            nameForImport = DD_PLR_ADD_DATA;
            importName = "!!Zusatzdaten zu Konstruktionsstückliste (VBRT)";
            mapping.put(FIELD_DAD_EVENT_FROM, VBRT_EREIA);
            mapping.put(FIELD_DAD_EVENT_TO, VBRT_EREIB);
        }
        mapping.put(FIELD_DAD_SERIES_NO, prefixForImporterInstance + BR);
        mapping.put(FIELD_DAD_POSE, prefixForImporterInstance + POSE);
        mapping.put(FIELD_DAD_POSV, prefixForImporterInstance + PV);
        mapping.put(FIELD_DAD_WW, prefixForImporterInstance + WW);
        mapping.put(FIELD_DAD_ETZ, prefixForImporterInstance + ETZ);
        mapping.put(FIELD_DAD_SDATA, prefixForImporterInstance + SDATA);
        mapping.put(FIELD_DAD_SDATB, prefixForImporterInstance + SDATB);
        mapping.put(FIELD_DAD_HIERARCHY, prefixForImporterInstance + STR);
        mapping.put(FIELD_DAD_CODE, prefixForImporterInstance + CODE);
        mapping.put(FIELD_DAD_INTERNAL_TEXT, prefixForImporterInstance + ITEXT);

        // Setzen des FileListTypes für den Importdialog
        importFileTypes = new FilesImporterFileListType[]{ new FilesImporterFileListType(TABLE_DA_DIALOG_ADD_DATA,
                                                                                         nameForImport, false, false, true,
                                                                                         new String[]{ MimeTypes.EXTENSION_XML }) };
        primaryKeysImport = new String[]{ prefixForImporterInstance + BR, prefixForImporterInstance + RAS, prefixForImporterInstance + POSE };
    }


    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysImport);
        importer.setMustHaveData(primaryKeysImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        if (!importer.isXMLMixedTable() && !importer.getTableNames().isEmpty()) {
            return importer.getTableNames().get(0).equals(IMPORT_TABLENAME_VBCA)
                   || importer.getTableNames().get(0).equals(IMPORT_TABLENAME_VBRT)
                   || importer.getTableNames().get(0).equals(VBRT_PREFIX);
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        adatAsLongSet = new HashSet<>();
        dataWithHighestAdat = new HashMap<>();
        dictImportTextIdHelper = new DictImportTextIdHelper(getProject());
        objectToTextID = new HashMap<>();
        objectsToStore = new HashMap<>();
        setBufferedSave(doBufferdSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        PartListAddDataImportHelper importHelper = new PartListAddDataImportHelper(getProject(), mapping, tableName);
        // Wenn die Baureihe nicht als "versorgungsrelevant" markiert ist, abbrechen.
        if (!importHelper.checkImportRelevanceForSeries(prefixForImporterInstance + BR, importRec, getInvalidSeriesSet(), this)) {
            return;
        }

        iPartsDialogBCTEPrimaryKey primaryBCTEKey = importHelper.getPrimaryBCTEKey(this, importRec, recordNo);
        if (primaryBCTEKey == null) {
            importHelper.cancelImporterDueToIncorrectBCTEKey(this, recordNo);
            return;
        }

        String bcteGUID = primaryBCTEKey.createDialogGUID();
        iPartsDataDialogAddData objectWithHighestData = dataWithHighestAdat.get(bcteGUID);
        if (objectWithHighestData == null) {
            iPartsDataDialogAddDataList objectsInDB = iPartsDataDialogAddDataList.loadAllDataForBCTEKey(getProject(), bcteGUID);
            if (!objectsInDB.isEmpty()) {
                objectWithHighestData = objectsInDB.get(0);
                dataWithHighestAdat.put(bcteGUID, objectWithHighestData);
            }
        }
        if (objectWithHighestData != null) {
            // Check, ob das höchste Adat die gleichen Daten hat, die importiert werden
            iPartsDataDialogAddData cloneOfHighestAdat = objectWithHighestData.cloneMe(getProject());
            cloneOfHighestAdat.getAttributes().setLoaded(true);
            importHelper.fillOverrideCompleteDataForDIALOGReverse(cloneOfHighestAdat, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
            if (!cloneOfHighestAdat.isModified()) {
                reduceRecordCount();
                return;
            }
        }
        iPartsDialogAddDataId dialogAddDataId = new iPartsDialogAddDataId(bcteGUID,
                                                                          iPartsDialogDateTimeHandler.getNextDBDateTimeForExistingDateTimes(adatAsLongSet));
        iPartsDataDialogAddData dialogAddData = new iPartsDataDialogAddData(getProject(), dialogAddDataId);
        if (!dialogAddData.existsInDB()) {
            dialogAddData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        importHelper.fillOverrideCompleteDataForDIALOGReverse(dialogAddData, importRec, iPartsDIALOGLanguageDefs.DIALOG_DE);
        // Setzen der separaten HMMSM Felder
        importHelper.setHmMSmFields(dialogAddData, importRec, prefixForImporterInstance + BR, prefixForImporterInstance + RAS, FIELD_DAD_HM, FIELD_DAD_M, FIELD_DAD_SM);
        // Status setzen
        importHelper.setDIALOGStateByImportTypeWithDefault(dialogAddData, FIELD_DAD_STATUS, isDIALOGInitialDataImport());

        String dialogId = importHelper.handleValueOfSpecialField(prefixForImporterInstance + ERGK, importRec);
        if (StrUtils.isValid(dialogId)) {
            // Hier hätte man für jede TextId "getProject().getDbLayer().getLanguagesTextsByTextId(textId);" aufrufen können
            // und das Ergebnis in einer Map (Text-Id zu EtkMultiSprach) speichern. Um sich die mehrfachen DB Zugriffe
            // zu sparen, werden alle Text-IDs aufgesammelt und später mit einem SQL Statement abgefragt (siehe getTextCache()).
            String textId = dictImportTextIdHelper.getDictTextIdForDialogId(DictTextKindTypes.ADD_TEXT, dialogId);
            if (StrUtils.isValid(textId)) {
                objectToTextID.put(dialogAddData.getAsId(), textId);
            }
        }
        String neutralText = importHelper.handleValueOfSpecialField(prefixForImporterInstance + SPRN, importRec);
        if (StrUtils.isValid(neutralText)) {
            EtkMultiSprache neutralTextObject = new EtkMultiSprache();
            neutralTextObject.setText(iPartsDIALOGLanguageDefs.DIALOG_DE.getDbValue(), neutralText);
            if (dictImportTextIdHelper.handleNeutralTextWithCache(neutralTextObject, TableAndFieldName.make(TABLE_DA_DIALOG_ADD_DATA, FIELD_DAD_TEXT_NEUTRAL))) {
                dialogAddData.setFieldValueAsMultiLanguage(FIELD_DAD_TEXT_NEUTRAL, neutralTextObject, DBActionOrigin.FROM_EDIT);
            }
        }
        dataWithHighestAdat.put(bcteGUID, dialogAddData);
        if (importToDB) {
            objectsToStore.put(dialogAddData.getAsId(), dialogAddData);
        }
    }

    @Override
    protected void postImportTask() {
        Map<String, EtkMultiSprache> textCache = null;
        if ((objectToTextID != null) && !objectToTextID.isEmpty()) {
            textCache = getTextCache();
        }

        // Durchlaufe alle DBDataObjects, die gspeichert werden sollen.
        for (iPartsDataDialogAddData objectToStore : objectsToStore.values()) {
            iPartsDialogAddDataId id = objectToStore.getAsId();
            // Hatte der Importdatensatz eine Referenz zu einem Text, dann füge dem Objekt das dazugehörige EtkMultiSprache
            // hinzu
            if (textCache != null) {
                String textId = objectToTextID.get(id);
                if (StrUtils.isValid(textId)) {
                    EtkMultiSprache textObject = textCache.get(textId);
                    if (textObject != null) {
                        objectToStore.setFieldValueAsMultiLanguage(FIELD_DAD_ADD_TEXT, textObject, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
            saveToDB(objectToStore);
        }
        super.postImportTask();
        adatAsLongSet = null;
        dataWithHighestAdat = null;
        dictImportTextIdHelper = null;
        objectToTextID = null;
        objectsToStore = null;
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.NEUTRAL_TEXT));
    }

    /**
     * Liefert zu allen gesammelten Text-IDs die dazugehörigen EtkMultiSprache Objekte.
     *
     * @return
     */
    private Map<String, EtkMultiSprache> getTextCache() {
        DBSQLQuery query = getProject().getEtkDbs().getDBForTable(TABLE_SPRACHE).getNewQuery();
        List<String> selectFields = new DwList<>();
        selectFields.add(FIELD_S_FELD);
        selectFields.add(FIELD_S_SPRACH);
        selectFields.add(FIELD_S_TEXTID);
        selectFields.add(FIELD_S_BENENN);
        selectFields.add(FIELD_S_BENENN_LANG);

        query.select(new Fields(selectFields)).from(new Tables(TABLE_SPRACHE));
        List<Condition> orList = new DwList<>();
        String tableAndFieldName = TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID);
        Set<String> textIds = new HashSet<>(objectToTextID.values());
        for (String textId : textIds) {
            Condition condition = new Condition(tableAndFieldName, Condition.OPERATOR_EQUALS, textId);
            orList.add(condition);
        }
        ConditionList conditionList = new ConditionList(orList, true);
        query.where(conditionList);
        query.orderBy(new String[]{ TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_FELD) });

        DBDataSet dbSet = query.executeQuery();
        Map<String, EtkMultiSprache> textCache = new HashMap<>();
        while (dbSet.next()) {
            EtkRecord record = dbSet.getRecord(selectFields);
            String textId = record.getField(FIELD_S_TEXTID).getAsString();
            if (StrUtils.isEmpty(textId)) {
                continue;
            }
            EtkMultiSprache textObject = textCache.get(textId);
            if (textObject == null) {
                textObject = new EtkMultiSprache();
                textObject.setTextId(textId);
                textCache.put(textId, textObject);
            }

            String lang = record.getField(FIELD_S_SPRACH).getAsString();
            String text = getProject().getEtkDbs().getLongTextFromRecord(record);
            if (StrUtils.isValid(lang) && (text != null)) {
                textObject.setText(lang, text);
            }

        }
        dbSet.close();
        return textCache;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterXML(importFile, iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_IMPORT));
        }
        return false;
    }

    private class PartListAddDataImportHelper extends DIALOGImportHelper {

        public PartListAddDataImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(prefixForImporterInstance + SDATA) || sourceField.equals(prefixForImporterInstance + SDATB)) {
                value = getDIALOGDateTimeValue(value);
            } else if (sourceField.equals(prefixForImporterInstance + STR)) {
                value = StrUtils.removeLeadingCharsFromString(value.trim(), '0'); // führende Nullen entfernen
            }
            return value;
        }

        public iPartsDialogBCTEPrimaryKey getPrimaryBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            HmMSmId hmMSmId = HmMSmId.getIdFromRaster(importRec.get(prefixForImporterInstance + BR), importRec.get(prefixForImporterInstance + RAS));
            return getPartListPrimaryBCTEKey(importer, recordNo, hmMSmId,
                                             handleValueOfSpecialField(prefixForImporterInstance + POSE, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + PV, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + WW, importRec),
                                             handleValueOfSpecialField(prefixForImporterInstance + ETZ, importRec),
                                             "",
                                             handleValueOfSpecialField(prefixForImporterInstance + SDATA, importRec));
        }
    }
}
