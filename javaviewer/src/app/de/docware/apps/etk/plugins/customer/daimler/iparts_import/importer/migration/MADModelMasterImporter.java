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
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTermIdHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsProductModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * MAD Baumuster Stammdaten Importer
 * Die Datei enthält die Baumuster Stammdaten für Retail
 * Diese Datei ist die Zentrale Datei der Migration
 */
public class MADModelMasterImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String MODEL_NO = "BM";
    final static String SALES_TITLE = "BM_VBEZ";
    final static String MODEL_TYPE = "BM_ART";
    final static String NOTUSED_1 = "PG";
    final static String NOTUSED_2 = "SK";
    final static String NOTUSED_3 = "AA";
    final static String HORSEPOWER = "PS";
    final static String KILOWATTS = "KW";
    final static String TEXT = "BM_TEXT";
    final static String TEXT_ID = "BM_BEN";
    final static String EDAT = "BMSA_EDAT";
    final static String ADAT = "BMSA_ADAT";
    /*
   BM,BM_VBEZ,BM_ART,PG,SK,AA,PS,KW,BM_BEN,BMSA_EDAT,BMSA_ADAT
    */
    private String[] headerNames = new String[]{
            MODEL_NO,
            SALES_TITLE,
            MODEL_TYPE,
            NOTUSED_1,
            NOTUSED_2,
            NOTUSED_3,
            HORSEPOWER,
            KILOWATTS,
            TEXT,
            TEXT_ID,
            EDAT,
            ADAT
    };

    private HashMap<String, String> mappingASModelData;
    private String[] primaryKeysModelDataImport;
    private Map<iPartsModelId, iPartsDataModel> modelMapToProof;
    private String tableName = TABLE_DA_MODEL;
    private DictTextKindTypes importType = DictTextKindTypes.MODEL_NAME;
    private boolean importToDB = true;
    private boolean doBufferSave = true;
    private boolean isNewImportStyle;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MADModelMasterImporter(EtkProject project) {
        super(project, "MAD Baumuster",
              new FilesImporterFileListType(TABLE_DA_MODEL, "!!MAD-Baumuster", true, false, true, new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        isNewImportStyle = false;
        primaryKeysModelDataImport = new String[]{ MODEL_NO };
        mappingASModelData = new HashMap<String, String>();
        mappingASModelData.put(FIELD_DM_MODEL_NO, MODEL_NO);
        mappingASModelData.put(FIELD_DM_SALES_TITLE, SALES_TITLE);
        mappingASModelData.put(FIELD_DM_MODEL_TYPE, MODEL_TYPE);
        mappingASModelData.put(FIELD_DM_HORSEPOWER, HORSEPOWER);
        mappingASModelData.put(FIELD_DM_KILOWATTS, KILOWATTS);
        mappingASModelData.put(FIELD_DM_DATA, EDAT);
        mappingASModelData.put(FIELD_DM_DATB, ADAT);
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysModelDataImport, new String[]{ SALES_TITLE, MODEL_TYPE, HORSEPOWER, KILOWATTS, TEXT_ID }));
        importer.setMustHaveData(primaryKeysModelDataImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   importType)) {
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
        super.preImportTask();
        setBufferedSave(doBufferSave);
        modelMapToProof = new HashMap<iPartsModelId, iPartsDataModel>();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ModelDataHelper helper = new ModelDataHelper(getProject(), mappingASModelData, tableName);
        String modelNo = helper.handleValueOfSpecialField(MODEL_NO, importRec);
        iPartsModelId id = new iPartsModelId(modelNo);
        iPartsDataModel modelData = new iPartsDataModel(getProject(), id);
        String foreignTextId = helper.handleValueOfSpecialField(TEXT_ID, importRec);
        String oldModelType = null;
        if (!modelData.loadFromDB(id)) {
            // Falls Datensatz noich nicht existiert -> mit leeren Werten anlegen
            modelData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        } else {
            oldModelType = modelData.getModelType();
        }
        // Fülle bzw. überschreibe Felder mit den neuen Werten
        helper.fillOverrideCompleteDataForMADReverse(modelData, importRec, iPartsMADLanguageDefs.MAD_DE);

        // Alle verfügbaren Sprachen mit dem sprachneutralen Text für die Verkaufsbezeichnung füllen
        EtkMultiSprache multiSprache = modelData.getFieldValueAsMultiLanguage(FIELD_DM_SALES_TITLE);
        if (!multiSprache.getText(Language.DE.getCode()).isEmpty()) {
            multiSprache.fillAllLanguages(getProject().getConfig().getDatabaseLanguages(), Language.DE);
        }

        // Wenn die Text-ID nicht existiert -> keine Verknüpfung zum Lexikon
        if (foreignTextId != null && !foreignTextId.isEmpty()) {
            //EtkMultiSprache multiEdit = getProject().getDbLayer().getLanguagesTextsByTextId(DictHelper.buildDictTextId(foreignTextId));
            EtkMultiSprache multiEdit = modelData.getFieldValueAsMultiLanguage(FIELD_DM_NAME);
            if (multiEdit == null) {
                multiEdit = new EtkMultiSprache();
            }
            String text = helper.handleValueOfSpecialField(TEXT, importRec);
            if (!StrUtils.isEmpty(text)) {
                multiEdit.setText(Language.DE, text);
            }
            DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
            boolean dictSuccessful = importHelper.handleDictTextId(importType, multiEdit, foreignTextId, DictHelper.getMADForeignSource(),
                                                                   false, TableAndFieldName.make(tableName, FIELD_DM_NAME));

            if (importHelper.hasInfos()) {
                for (String str : importHelper.getInfos()) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1: \"%2\"", String.valueOf(recordNo), str),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }
            }
            if (!dictSuccessful || importHelper.hasWarnings()) {
                //Fehler beim Dictionary Eintrag
                for (String str : importHelper.getWarnings()) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }

                if (!dictSuccessful) {
                    // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                    cancelImport();
                } else {
                    reduceRecordCount();
                }
                return; // Import nicht abbrechen, aber diesen Datensatz aufgrund der Warnungen überspringen
            }

            modelData.setFieldValueAsMultiLanguage(FIELD_DM_NAME, multiEdit, DBActionOrigin.FROM_EDIT);
        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit Baumuster \"%2\" enthält keine Text-ID.", String.valueOf(recordNo),
                                                        modelNo),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }
        // Datenquelle setzen (MAD)
        modelData.setFieldValue(FIELD_DM_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
        if (oldModelType != null) {
            // Model ist bereits in DB vorhanden
            if (!modelData.getModelType().equals(oldModelType)) {
                // ModelTyp hat sich geändert => testen
                modelMapToProof.put(modelData.getAsId(), modelData);
            } else {
                // ModelTyp ist 'GA'/'GM' => testen
                if (helper.checkIFSpecialAggType(modelData.getModelType())) {
                    modelMapToProof.put(modelData.getAsId(), modelData);
                }
            }
        } else {
            // Model ist neu => testen
            modelMapToProof.put(modelData.getAsId(), modelData);
        }
        if (importToDB) {
            saveToDB(modelData);
        }
    }

    @Override
    public void postImportTask() {
        if (!isCancelled()) {
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            }
            super.postImportTask();
            setBufferedSave(doBufferSave);
            handleModelsToProof();
        }

        super.postImportTask();
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.MODEL_NAME));
    }

    private void handleModelsToProof() {
        if (!modelMapToProof.isEmpty()) {
            int handledRecords = 0;
            getMessageLog().fireMessage(translateForLog("!!Nachbehandlung für Aggregatetyp (%1)", String.valueOf(modelMapToProof.size())),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            getMessageLog().fireProgress(0, modelMapToProof.size(), "", true, false);

            ModelDataHelper helper = new ModelDataHelper(getProject(), mappingASModelData, tableName);
            List<iPartsModelId> proofedModelList = new DwList<>();
            List<iPartsProductId> proofedProductList = new DwList<>();
            // über alle zu testenden Models
            iPartsProductModels productModelsCache = iPartsProductModels.getInstance(getProject());
            for (iPartsDataModel dataModel : modelMapToProof.values()) {
                if (!proofedModelList.contains(dataModel.getAsId())) {
                    // hole Liste der Produkte zum Model
                    List<iPartsProduct> productsByModel = productModelsCache.getProductsByModel(getProject(), dataModel.getAsId().getModelNumber());
                    // über alle Produkte
                    for (iPartsProduct product : productsByModel) {
                        iPartsProductId productId = product.getAsId();
                        // wurde Produkt bereits behandelt?
                        if (!proofedProductList.contains(productId)) {
                            List<String> warnings = new DwList<>();
                            // merke AggregateTyp
                            String aggType = product.getAggregateType();
                            // hole Liste der zum Produkt gehörenden Models
                            Set<String> partsModelList = product.getModelNumbers(getProject());
                            List<String> modelTypeWarnings = new DwList<>();
                            // überprüfe den ModelType der Models
                            String modelType = checkModelTypes(partsModelList, modelTypeWarnings);
                            // ist es 'GA' oder 'GM'?
                            if (helper.checkIFSpecialAggType(modelType)) {
                                // hat das Produkt noch den alten AggregateTyp?
                                if (aggType.equals("G")) {
                                    // lade Produkt, ändere AggregateTyp und speichere
                                    iPartsDataProduct dataProductToChange = new iPartsDataProduct(getProject(), productId);
                                    if (dataProductToChange.loadFromDB(productId) && !modelType.isEmpty()) {
                                        dataProductToChange.setFieldValue(FIELD_DP_AGGREGATE_TYPE, modelType, DBActionOrigin.FROM_EDIT);
                                        if (importToDB) {
                                            saveToDB(dataProductToChange);
                                        }
                                        // Umsetzung melden
                                        warnings.add(translateForLog("!!Produkt \"%1\" Aggregatetyp umgesetzt zu \"%2\"", productId.getProductNumber(), modelType));
                                    }
                                }
                            }
                            // warnings Ausgabe
                            if (!warnings.isEmpty() || !modelTypeWarnings.isEmpty()) {
                                if (!warnings.isEmpty()) {
                                    for (String warn : warnings) {
                                        getMessageLog().fireMessage(warn, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                                    }
                                }
                                if (!modelTypeWarnings.isEmpty()) {
                                    StringBuilder str = new StringBuilder();
                                    str.append(translateForLog("!!Beim Produkt \"%1\" mit Aggregatetyp (%2) besitzen folgende Baumuster einen unterschiedlichen Aggregatetyp:",
                                                               productId.getProductNumber(), product.getAggregateType()));
                                    for (String warn : modelTypeWarnings) {
                                        str.append(" ");
                                        str.append(warn);
                                    }
                                    getMessageLog().fireMessage(str.toString(), MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                                }
                            }
                            proofedProductList.add(productId);
                        }
                    }
                    //proofedModelList.add(dataModel.getAsId());
                }
                handledRecords++;
                getMessageLog().fireProgress(handledRecords, modelMapToProof.size(), "", true, false);
            }
            getMessageLog().hideProgress();
            getMessageLog().fireMessage(translateForLog("!!Nachbehandlung beendet"),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    private String checkModelTypes(Collection<String> partsModelList, List<String> warnings) {
        String modelType = "";
        // überprüfe, ob alle Models den gleichen ModelTyp besitzen
        for (String modelNumber : partsModelList) {
            iPartsModelId modelId = new iPartsModelId(modelNumber);
            iPartsDataModel dataModelToProof = modelMapToProof.get(modelId);
            String modelTypeToProof = "";
            if (dataModelToProof == null) {
                //warning Model nicht vorhanden
                //warnings.add(translateForLog("!!Model \"%1\" nicht in Baumusterstammdaten vorhanden, jedoch im Produkt \"2\" verwendet", modelNumber, productId.getProductNumber()));
                iPartsDataModel dataModelToTest = new iPartsDataModel(getProject(), modelId);
                if (dataModelToTest.loadFromDB(modelId)) {
                    modelTypeToProof = dataModelToTest.getModelType();
                }
            } else {
                modelTypeToProof = dataModelToProof.getModelType();
            }
            if (modelType.isEmpty()) {
                modelType = modelTypeToProof;
            } else {
                if (!modelType.equals(modelTypeToProof)) {
                    //warning
                    warnings.add(translateForLog("!!%1 (%2)", modelNumber, modelTypeToProof));
                }
            }
        }
        return modelType;
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        if (importFileType.getFileListType().equals(tableName)) {
            getProject().getDbLayer().delete(tableName, new String[]{ FIELD_DM_SOURCE }, new String[]{ iPartsImportDataOrigin.MAD.getOrigin() });
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                isNewImportStyle = true;
                return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNames));
            } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNames));
            } else {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ',', true, null));
            }
        }
        return false;
    }

    private class ModelDataHelper extends MADImportHelper {

        public ModelDataHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(MODEL_NO)) {
                value = iPartsNumberHelper.getPlainModelNumber(value);
            } else if (sourceField.equals(EDAT) || sourceField.equals(ADAT)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(MODEL_TYPE)) {
                value = value.trim();
            } else if (sourceField.equals(TEXT)) {
                value = DictMultilineText.getInstance().convertDictText(DictTextKindTypes.DIALOG_MODEL_ADDTEXT, value);
            } else if (sourceField.equals(TEXT_ID)) {
                value = iPartsTermIdHandler.removeLeadingZerosFromTermId(value);
            }
            return value;
        }
    }
}