/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTermIdHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Importer für die Ergänzungstexte aus MAD.
 */
public class MADETexteImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String ETXT_NR = "ETXT_NR";
    final static String ETXT_TXT = "ETXT_TXT";
    final static String ETXT_TEXT_ID = "ETXT_TEXT_ID";
    final static String ETXT_ELDAS_NR = "ETXT_ELD_NR";
    final static String ETXT_EDAT = "ETXT_EDAT";
    final static String ETXT_ADAT = "ETXT_ADAT";

    private static final int MAX_ENTRIES_FOR_COMMIT = 1000;

    private String[] headerNames = new String[]{
            ETXT_NR,
            ETXT_TXT,
            ETXT_TEXT_ID,
            ETXT_ELDAS_NR,
            ETXT_EDAT,
            ETXT_ADAT
    };

    //private HashMap<String, String> mappingKGTUData;
    //private String[] primaryKeysKGTUImport;
    private String tableName = "table";
    private Map<String, ETxtItem> mappingELDAS;
    private Map<String, ETxtItem> mappingDIALOG;
    private int recordCount = 0;
    private int maxRecords = 0;

    //private Set<iPartsProductId> productVisited;

    private boolean importToDB = true;
    private boolean doBufferSave = true;

    public MADETexteImporter(EtkProject project) {
        super(project, "MAD E-Texte",
              new FilesImporterFileListType("table", "!!MAD-E-Texte", true, false, false,
                                            new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ,
                                                          MimeTypes.EXTENSION_ALL_FILES }));

        initMapping();
    }

    private void initMapping() {
        //da es ausser der '.del'-Datei keine Importdatei gibt bleibt das mapping leer
        mappingELDAS = new HashMap<String, ETxtItem>();
        mappingDIALOG = new HashMap<String, ETxtItem>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        //importer.setMustExists(StrUtils.mergeArrays(primaryKeysKGTUImport, new String[]{ KGTU_FTXT_TEXT_ID, KGTU_KGTX_ADAT, KGTU_KGTX_EDAT }));
        //importer.setMustHaveData(new String[]{ KGTU_KATALOG, KGTU_KG });
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   DictTextKindTypes.ADD_TEXT)) {
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
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ETxtImportHelper helper = new ETxtImportHelper(getProject(), null, tableName);

        if (helper.isDIALOG_TextNo(ETXT_NR, importRec)) {
            ETxtItem etxtItem = new ETxtItem();
            etxtItem.fillByDIALOG(helper, importRec);
            if (mappingDIALOG.get(etxtItem.txtNr_DIALOG) != null) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit doppelter DIALOG TextNr \"%2\" übersprungen", String.valueOf(recordNo),
                                                            importRec.get(ETXT_NR)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }
            mappingDIALOG.put(etxtItem.txtNr_DIALOG, etxtItem);
        } else if (helper.isELDAS_TextNo(ETXT_NR, importRec)) {
            ETxtItem etxtItem = new ETxtItem();
            etxtItem.fillByELDAS(helper, importRec);
            if (mappingELDAS.get(etxtItem.txtNr_ELDAS) != null) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 mit doppelter ELDAS TextNr \"%2\" übersprungen", String.valueOf(recordNo),
                                                            importRec.get(ETXT_NR)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }
            mappingELDAS.put(etxtItem.txtNr_ELDAS, etxtItem);
        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger ETxt_Nr \"%2\" übersprungen", String.valueOf(recordNo),
                                                        importRec.get(ETXT_NR)),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            List<ETxtItem> importRecs = new ArrayList<ETxtItem>();
            List<ETxtItem> noTermIdList = new ArrayList<ETxtItem>();
            List<ETxtItem> noELDASIdList = new ArrayList<ETxtItem>();
            List<ETxtItem> noDIALOGIdList = new ArrayList<ETxtItem>();
            Map<ETxtItem, String> twoDIALOGIdOneEldasIdList = new HashMap<ETxtItem, String>();
            mergeMappings(importRecs, noTermIdList, noELDASIdList, noDIALOGIdList, twoDIALOGIdOneEldasIdList);
            List<ETxtItem> onlyELDASIdList = new ArrayList<ETxtItem>(mappingELDAS.values());
            maxRecords = importRecs.size() + noELDASIdList.size() + noDIALOGIdList.size() + onlyELDASIdList.size();
            if (!isCancelled()) {
                checkAndImport(importRecs, twoDIALOGIdOneEldasIdList);
            }
            if (!isCancelled()) {
                checkAndImport(noELDASIdList, null);
            }
            if (!isCancelled()) {
                checkAndImport(noDIALOGIdList, null);
            }
            if (!isCancelled()) {
                checkAndImport(onlyELDASIdList, null);
            }
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
        }

        super.postImportTask();
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.ADD_TEXT));
    }

    private void checkAndImport(List<ETxtItem> importRecs, Map<ETxtItem, String> twoDIALOGOneELDASMap) {
        DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
        for (ETxtItem item : importRecs) {
            if (Thread.currentThread().isInterrupted()) {
                cancelImport("!!Import-Thread wurde frühzeitig beendet");
                return;
            }
            EtkMultiSprache multiEdit = new EtkMultiSprache();
            multiEdit.setText(Language.DE, item.text);

            String dialogId = item.txtNr_DIALOG;
            if ((twoDIALOGOneELDASMap != null) && !twoDIALOGOneELDASMap.isEmpty()) {
                String tempDialog = twoDIALOGOneELDASMap.get(item);
                if (!StrUtils.isEmpty(tempDialog)) {
                    dialogId = tempDialog;
                }
            }
            importHelper.handleDictTextId(DictTextKindTypes.ADD_TEXT, multiEdit, item.termId, DictHelper.getMADForeignSource(), false,
                                          TableAndFieldName.make(TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT), item.txtNr_ELDAS, dialogId);
            recordCount++;
            updateProgress(recordCount, maxRecords);
            autoCommitAfterMaxEntries(MAX_ENTRIES_FOR_COMMIT);
        }
    }

    /**
     * Überprüft die eingelesenen Datensätz auf ELDAS, DIALOG und ELDAS*DIALOG Zugehörigkeit. Jeder Datensatz wird eingeteilt in:
     * - zu importierender Datensatz
     * - Datensatz ohne TermID
     * - Datensatz ohne ELDAS ID
     * - Datensatz ohne DIALOG ID
     * - Datensatz mit einer ELDAS ID und zwei DIALOG IDs
     *
     * @param importRecs
     * @param noTermIdList
     * @param noELDASIdList
     * @param noDIALOGIdList
     * @param twoDIALOGIdOneEldasIdList
     */
    private void mergeMappings(List<ETxtItem> importRecs, List<ETxtItem> noTermIdList, List<ETxtItem> noELDASIdList, List<ETxtItem> noDIALOGIdList, Map<ETxtItem, String> twoDIALOGIdOneEldasIdList) {
        Map<String, ETxtItem> doneELDASList = new HashMap<String, ETxtItem>();

        for (Map.Entry<String, ETxtItem> entry : mappingDIALOG.entrySet()) {
            ETxtItem etxtDIALOGItem = entry.getValue();
            if (!StrUtils.isEmpty(etxtDIALOGItem.txtNr_ELDAS)) {
                ETxtItem etxtELDASItem = mappingELDAS.get(etxtDIALOGItem.txtNr_ELDAS);
                if (etxtELDASItem != null) {
                    etxtELDASItem.txtNr_DIALOG = etxtDIALOGItem.txtNr_DIALOG;
                    if (!StrUtils.isEmpty(etxtDIALOGItem.termId)) {
                        if (!StrUtils.isEmpty(etxtELDASItem.termId)) {
                            if (!etxtDIALOGItem.termId.equals(etxtELDASItem.termId)) {
                                //Fehler: termIds unterscheiden sich
                                getMessageLog().fireMessage(translateForLog("!!Unterschiedliche TermId (DIALOG: %1 <-> ELDAS: %2)",
                                                                            etxtDIALOGItem.termId, etxtELDASItem.termId),
                                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                                noDIALOGIdList.add(etxtDIALOGItem);
                            } else {
                                importRecs.add(etxtELDASItem);
                                mappingELDAS.remove(entry.getValue().txtNr_ELDAS);
                                doneELDASList.put(etxtELDASItem.txtNr_ELDAS, etxtELDASItem);
                            }
                        } else {
                            //übernehme DIALOG termId
                            etxtELDASItem.termId = etxtDIALOGItem.termId;
                            importRecs.add(etxtELDASItem);
                            mappingELDAS.remove(entry.getValue().txtNr_ELDAS);
                            doneELDASList.put(etxtELDASItem.txtNr_ELDAS, etxtELDASItem);
                        }
                    } else {
                        if (!StrUtils.isEmpty(etxtELDASItem.termId)) {
                            importRecs.add(etxtELDASItem);
                        } else {
                            //sowohl DIALOG-Entry als auch ELDAS-Entry besitzen keine termId
                            noTermIdList.add(etxtELDASItem);
                        }
                        mappingELDAS.remove(entry.getValue().txtNr_ELDAS);
                        doneELDASList.put(etxtELDASItem.txtNr_ELDAS, etxtELDASItem);
                    }
                } else {
                    etxtELDASItem = doneELDASList.get(etxtDIALOGItem.txtNr_ELDAS);
                    if (etxtELDASItem == null) {
                        //DIALOG-Entry mit ELDAS-Nummer, aber ELDAS-Eintrag fehlt
                        getMessageLog().fireMessage(translateForLog("!!kein ELDAS-Eintrag bei DIALOG-Eintrag (ELDAS TextNr %1)",
                                                                    etxtDIALOGItem.txtNr_ELDAS),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);

                        noELDASIdList.add(etxtDIALOGItem);
                    } else {
                        //2 DIALOG-Entries mit gleicher ELDAS TextNr
                        String dialogIds = twoDIALOGIdOneEldasIdList.get(etxtELDASItem);
                        if (dialogIds == null) {
                            dialogIds = etxtELDASItem.txtNr_DIALOG;
                        }
                        etxtELDASItem.txtNr_DIALOG = dialogIds;
                        twoDIALOGIdOneEldasIdList.put(etxtELDASItem, StrUtils.makeDelimitedString(",", etxtDIALOGItem.txtNr_DIALOG, dialogIds));
                    }
                }
            } else {
                //DIALOG-Entry ohne ELDAS-Nummer
                getMessageLog().fireMessage(translateForLog("!!DIALOG-Entry ohne ELDAS-Text Nummer (DIALOG TextNr %1)",
                                                            etxtDIALOGItem.txtNr_DIALOG),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                noELDASIdList.add(etxtDIALOGItem);
            }
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
//            isNewImportStyle = true;
            return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNames));
        } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNames));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', true, null));
        }
    }

    private class ETxtImportHelper extends MADImportHelper {

        public ETxtImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        public boolean isELDAS_TextNo(String sourceField, Map<String, String> importRec) {
            String textNo = handleValueOfSpecialField(sourceField, importRec);
            return textNo.length() == 8;
        }

        public boolean isDIALOG_TextNo(String sourceField, Map<String, String> importRec) {
            String textNo = handleValueOfSpecialField(sourceField, importRec);
            return textNo.length() == 13;
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(ETXT_EDAT) || sourceField.equals(ETXT_ADAT)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(ETXT_TXT)) {
                value = DictMultilineText.getInstance().convertDictText(DictTextKindTypes.ADD_TEXT, value);
            } else if (sourceField.equals(ETXT_TEXT_ID)) {
                value = iPartsTermIdHandler.removeLeadingZerosFromTermId(value);
            }
            return value;
        }
    }

    private class ETxtItem {

        public String txtNr_ELDAS;
        public String txtNr_DIALOG;
        public String termId;
        public String text;
        public String eDat;
        public String aDat;

        public void fillByELDAS(ETxtImportHelper importHelper, Map<String, String> importRec) {
            this.txtNr_ELDAS = importHelper.handleValueOfSpecialField(ETXT_NR, importRec);
            this.termId = importHelper.handleValueOfSpecialField(ETXT_TEXT_ID, importRec);
            this.text = importHelper.handleValueOfSpecialField(ETXT_TXT, importRec);
            this.eDat = importHelper.handleValueOfSpecialField(ETXT_EDAT, importRec);
            this.aDat = importHelper.handleValueOfSpecialField(ETXT_ADAT, importRec);
        }

        public void fillByDIALOG(ETxtImportHelper importHelper, Map<String, String> importRec) {
            this.txtNr_DIALOG = importHelper.handleValueOfSpecialField(ETXT_NR, importRec);
            this.txtNr_ELDAS = importHelper.handleValueOfSpecialField(ETXT_ELDAS_NR, importRec);
            this.termId = importHelper.handleValueOfSpecialField(ETXT_TEXT_ID, importRec);
            this.text = importHelper.handleValueOfSpecialField(ETXT_TXT, importRec);
            this.eDat = importHelper.handleValueOfSpecialField(ETXT_EDAT, importRec);
            this.aDat = importHelper.handleValueOfSpecialField(ETXT_ADAT, importRec);
        }
    }
}