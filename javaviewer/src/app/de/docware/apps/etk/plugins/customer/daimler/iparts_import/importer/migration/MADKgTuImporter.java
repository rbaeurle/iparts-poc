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
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSales;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSalesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSalesList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * MAD Kg/Tu Referenz Importer
 * Die Datei enthält die Kg/Tu Referenzdaten für Retail
 * Diese Datei ist die Zentrale Datei der Migration
 */
public class MADKgTuImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String KGTU_KATALOG = "KATALOG";
    final static String KGTU_KG = "KG";
    final static String KGTU_TU = "TU";
    final static String KGTU_FTXT = "FTXT";
    final static String KGTU_FTXT_TEXT_ID = "FTXT_TEXT_ID";
    final static String KGTU_KGTX_EDAT = "KGTX_EDAT";
    final static String KGTU_KGTX_ADAT = "KGTX_ADAT";

    private String[] headerNames = new String[]{
            KGTU_KATALOG,
            KGTU_KG,
            KGTU_TU,
            KGTU_FTXT,
            KGTU_FTXT_TEXT_ID,
            KGTU_KGTX_EDAT,
            KGTU_KGTX_ADAT
    };

    private HashMap<String, String> mappingKGTUData;
    private String[] primaryKeysKGTUImport;
    private String tableName = TABLE_DA_KGTU_AS;

    private Set<iPartsProductId> productVisited;
    private Map<iPartsDataKgTuAfterSalesId, iPartsDataKgTuAfterSales> dataMap;
    private Map<String, EtkMultiSprache> foreignIdMultiLangMap;

    private boolean importToDB = true;
    private boolean doBufferSave = true;
    private boolean isNewImportStyle;


    public MADKgTuImporter(EtkProject project) {
        super(project, "MAD KG/TU",
              new FilesImporterFileListType(TABLE_DA_KGTU_AS, "!!MAD-KG/TU-Referenzen", true, false, false,
                                            new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_CSV,
                                                          MimeTypes.EXTENSION_ALL_FILES }));

        initMapping();
    }


    private void initMapping() {
        isNewImportStyle = false;
        primaryKeysKGTUImport = new String[]{ KGTU_KATALOG, KGTU_KG, KGTU_TU };
        mappingKGTUData = new HashMap<String, String>();
//        mappingKGTUData.put(FIELD_DA_DKM_PRODUCT, KGTU_KATALOG);  // PK
//        mappingKGTUData.put(FIELD_DA_DKM_KG, KGTU_KG);            // PK
//        mappingKGTUData.put(FIELD_DA_DKM_TU, KGTU_TU);            // PK
        mappingKGTUData.put(FIELD_DA_DKM_ADAT, KGTU_KGTX_ADAT);
        mappingKGTUData.put(FIELD_DA_DKM_EDAT, KGTU_KGTX_EDAT);
    }


    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysKGTUImport, new String[]{ KGTU_FTXT_TEXT_ID, KGTU_KGTX_ADAT, KGTU_KGTX_EDAT }));
        importer.setMustHaveData(new String[]{ KGTU_KATALOG, KGTU_KG });
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());
        if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                   DictTextKindTypes.KG_TU_NAME)) {
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
        productVisited = new HashSet<iPartsProductId>();
        dataMap = new HashMap<>();
        foreignIdMultiLangMap = new HashMap<>();
        setBufferedSave(doBufferSave);
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        KGTUImportHelper helper = new KGTUImportHelper(getProject(), mappingKGTUData, tableName);
        iPartsDataKgTuAfterSalesId kgTuId = new iPartsDataKgTuAfterSalesId(helper.handleValueOfSpecialField(KGTU_KATALOG, importRec),
                                                                           helper.handleValueOfSpecialField(KGTU_KG, importRec),
                                                                           helper.handleValueOfSpecialField(KGTU_TU, importRec));
        if (kgTuId.getProduct().isEmpty() || kgTuId.getKg().isEmpty()) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 mit ungültiger Id \"%2\" übersprungen", String.valueOf(recordNo),
                                                        kgTuId.toStringForLogMessages()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return;
        }

        if (!productVisited.contains(kgTuId.getProductId())) {
            // In diesem Katalog/Produkt waren wir noch nicht. Die alten Einträge erstmal löschen
            deleteAndLoadKgTuEntriesForProduct(kgTuId.getProductId());
            productVisited.add(kgTuId.getProductId());
        }

        iPartsDataKgTuAfterSales kgTuDataObject = dataMap.get(kgTuId);
        if (kgTuDataObject == null) {
            kgTuDataObject = new iPartsDataKgTuAfterSales(getProject(), kgTuId);
            kgTuDataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }

        String foreignTextId = helper.handleValueOfSpecialField(KGTU_FTXT_TEXT_ID, importRec);
        // EDSSAA in DB mit neuen Daten überschreiben
        helper.fillOverrideCompleteDataForMADReverse(kgTuDataObject, importRec, iPartsMADLanguageDefs.MAD_DE);
        // Wegen: DAIMLER-6878: Auch hier die Quelle setzen.
        kgTuDataObject.setFieldValue(FIELD_DA_DKM_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);

        DictTextKindTypes importType = DictTextKindTypes.KG_TU_NAME;
        // Wenn die Text-ID nicht existiert -> keine Verknüpfung zum Lexikon
        if ((foreignTextId != null) && !foreignTextId.isEmpty()) {
            EtkMultiSprache multiEdit = foreignIdMultiLangMap.get(foreignTextId);
            if (multiEdit == null) {
                multiEdit = kgTuDataObject.getFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC);
                if (multiEdit == null) {
                    multiEdit = new EtkMultiSprache();
                    if (isNewImportStyle) {
                        multiEdit.setText(Language.DE, helper.handleValueOfSpecialField(KGTU_FTXT, importRec));
                    }
                }
                DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
                boolean dictSuccessful = importHelper.handleDictTextId(importType, multiEdit, foreignTextId, DictHelper.getMADForeignSource(),
                                                                       false, TableAndFieldName.make(tableName, FIELD_DA_DKM_DESC));

                if (!dictSuccessful || importHelper.hasWarnings()) {
                    //Fehler beim Dictionary Eintrag
                    for (String str : importHelper.getWarnings()) {
                        getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                                    MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                    }

                    if (!dictSuccessful) {
                        // Die Textart ist nicht definiert -> da können nur verhaute Daten importiert werden
                        cancelImport();
                    }
                    return; // Import nicht abbrechen, aber diesen Datensatz aufgrund der Warnungen überspringen
                }

                kgTuDataObject.setFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC, multiEdit, DBActionOrigin.FROM_EDIT);
                foreignIdMultiLangMap.put(foreignTextId, multiEdit.cloneMe());
            } else {
                kgTuDataObject.setFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC, multiEdit.cloneMe(), DBActionOrigin.FROM_EDIT);
            }
        } else {
            getMessageLog().fireMessage(translateForLog("!!Record %1 \"%2\" enthält keine Text-ID.", String.valueOf(recordNo),
                                                        kgTuId.toStringForLogMessages()),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        }

        if (importToDB) {
            saveToDB(kgTuDataObject);
            dataMap.remove(kgTuDataObject.getAsId());
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            } else {
                deleteAndLoadKgTuEntriesForProduct(null);
            }
        }

        super.postImportTask();
        productVisited = null;
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.KG_TU_NAME));
    }

    private void deleteAndLoadKgTuEntriesForProduct(iPartsProductId productId) {
        if (!dataMap.isEmpty()) {
            iPartsDataKgTuAfterSalesList entries = new iPartsDataKgTuAfterSalesList();
            for (iPartsDataKgTuAfterSales dataKgTuAfterSales : dataMap.values()) {
                entries.add(dataKgTuAfterSales, DBActionOrigin.FROM_DB);
            }
            if (importToDB) {
                entries.deleteFromDB(getProject(), true);
            }
        }
        dataMap.clear();
        if (productId != null) {
            iPartsDataKgTuAfterSalesList entries = iPartsDataKgTuAfterSalesList.loadKgTuForProductListWithTexts(getProject(), productId);
            for (iPartsDataKgTuAfterSales dataKgTuAfterSales : entries) {
                dataKgTuAfterSales.removeForeignTablesAttributes();
                dataMap.put(dataKgTuAfterSales.getAsId(), dataKgTuAfterSales);
            }
        }
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return removeAllExistingDataForTable(importFileType, tableName);
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
            isNewImportStyle = true;
            return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNames));
        } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNames));
        } else {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ',', true, null));
        }
    }


    private class KGTUImportHelper extends MADImportHelper {

        public KGTUImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(KGTU_KGTX_ADAT) || sourceField.equals(KGTU_KGTX_EDAT)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(KGTU_FTXT)) {
                //KG/TU Umbruch
                value = DictMultilineText.getInstance().convertDictText(DictTextKindTypes.KG_TU_NAME, value);
            } else if (sourceField.equals(KGTU_FTXT_TEXT_ID)) {
                value = iPartsTermIdHandler.removeLeadingZerosFromTermId(value);
            }
            return value;
        }
    }

}
