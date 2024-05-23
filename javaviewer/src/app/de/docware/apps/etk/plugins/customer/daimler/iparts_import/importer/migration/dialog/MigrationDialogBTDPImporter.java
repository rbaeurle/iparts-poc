/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataKgTuAfterSalesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrations DIALOG BTDP-Importer
 */
public class MigrationDialogBTDPImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    //final static String BTDP_HERK = "BTDP_HERK";
    final static String BTDP_NR = "BTDP_NR";
    final static String BTDP_KEY1 = "BTDP_KEY1";
    final static String BTDP_KEY2 = "BTDP_KEY2";
    final static String BTDP_KEY3 = "BTDP_KEY3";
    final static String BTDP_POSV = "BTDP_POSV";
    final static String BTDP_ETZ = "BTDP_ETZ";
    final static String BTDP_WW = "BTDP_WW";
    final static String BTDP_SDA = "BTDP_SDA";
    final static String BTDP_KAT = "BTDP_KAT";
    final static String BTDP_BT = "BTDP_BT";
    final static String BTDP_BILD = "BTDP_BILD";
    final static String BTDP_SDB = "BTDP_SDB";
    final static String BTDP_BTKZ = "BTDP_BTKZ";
    final static String BTDP_EDAT = "BTDP_EDAT";
    final static String BTDP_ADAT = "BTDP_ADAT";
    final static String ELBT_ADAT = "ELBT_ADAT";
//    final static String BTDP_MF_BTKZ = "BTDP_MF_BTKZ";
//    final static String BTDP_BT_AEND = "BTDP_BT_AEND";

    private String[] headerNames = new String[]{
            BTDP_NR,
            BTDP_KEY1,
            BTDP_KEY2,
            BTDP_KEY3,
            BTDP_POSV,
            BTDP_ETZ,
            BTDP_WW,
            BTDP_SDA,
            BTDP_KAT,
            BTDP_BT,
            BTDP_BILD,
            BTDP_SDB,
            BTDP_BTKZ,
            BTDP_EDAT,
            BTDP_ADAT,
            ELBT_ADAT
    };

    private HashMap<String, String> mappingBTDPData;
    private String[] primaryKeysBTDPImport;
    private String tableName = "BTDP";  // = TABLE_DA_PRODUCT;
    private boolean isSingleCall = false;

    public MigrationDialogBTDPImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG BTDP", withHeader,
              new FilesImporterFileListType("BTDP", "!!DIALOG BTDP", true, false, false, new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    private void initMapping() {
        primaryKeysBTDPImport = new String[]{};
        mappingBTDPData = new HashMap<String, String>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysBTDPImport);
        importer.setMustHaveData(primaryKeysBTDPImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //nur für den EinzelTest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
            isSingleCall = true;
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
        BTDPImportHelper importHelper = new BTDPImportHelper(getProject(), mappingBTDPData, tableName);
        String originalProductNo = importHelper.handleValueOfSpecialField(BTDP_KAT, importRec);
        String productNo = originalProductNo;

        if (getCatalogImportWorker().isMergingProducts()) {
            // Produktnummer vom zusammengeführten Produkt ermitteln
            productNo = getCatalogImportWorker().getOriginalProductsToMergedProductsMap().get(productNo);
            if (productNo == null) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen",
                                                            String.valueOf(recordNo),
                                                            translateForLog("!!kein zusammengeführtes Produkt für das Produkt \"%1\" gefunden",
                                                                            originalProductNo)),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                reduceRecordCount();
                return;
            }
        }

        iPartsDataProduct dataProduct;
        if (isSingleCall) {
            dataProduct = getCatalogImportWorker().createProductData(this, productNo, DBActionOrigin.FROM_EDIT);
        } else {
            dataProduct = getCatalogImportWorker().getProductData(productNo);
        }
        if (dataProduct != null) {
            iPartsDataKgTuAfterSalesId kgTuASId = importHelper.getKGTUId(importRec);
            KgTuId kgTuId = new KgTuId(kgTuASId.getKg(), kgTuASId.getTu());
            List<String> warnings = new DwList<>();
            iPartsDataAssembly dataAssembly = getCatalogImportWorker().createAndAddAssembly(this, dataProduct, kgTuId, DBActionOrigin.FROM_EDIT, warnings);
            if (dataAssembly != null) {
                iPartsDialogBCTEPrimaryKey btdpPrimaryKey = importHelper.getPrimaryBCTEKey(this, importRec, recordNo);
                if (btdpPrimaryKey != null) {
                    iPartsCatalogImportWorker.DataObjectBTDP dataObjectBTDP = getCatalogImportWorker().getBTDPElement(dataProduct, kgTuId);
                    String hotspot = "";
                    String hotspotLfdNr = importHelper.getHotspotLfdNr(importRec);
                    // Teilepositionen mit BTDT_BTKZ != "X" sollen keinen Hotspot erhalten, d.h. der hotspot String bleibt leer
                    if (importHelper.handleValueOfSpecialField(BTDP_BTKZ, importRec).equals("X")) {
                        hotspot = importHelper.getHotspot(importRec);
                    }
                    dataObjectBTDP.addPictureNameAndHotspot(btdpPrimaryKey, importHelper.getPictureName(importRec),
                                                            importHelper.handleValueOfSpecialField(ELBT_ADAT, importRec),
                                                            hotspot, hotspotLfdNr, originalProductNo);
                    dataObjectBTDP.addElemValues(btdpPrimaryKey,
                                                 importHelper.handleValueOfSpecialField(BTDP_SDB, importRec),
                                                 importHelper.handleValueOfSpecialField(BTDP_BTKZ, importRec),
                                                 "" /*entityAttribut*/);

                    if (getCatalogImportWorker().isMergingProducts()) {
                        dataObjectBTDP.addOriginalProductForBCTEKeyAndHotspot(btdpPrimaryKey, hotspot, new iPartsProductId(originalProductNo));
                    }
                } else {
                    importHelper.cancelImporterDueToIncorrectBCTEKey(this, recordNo);
                    return;
                }
            } else {
                for (String str : warnings) {
                    getMessageLog().fireMessage(translateForLog("!!Record %1 wegen \"%2\" übersprungen", String.valueOf(recordNo), str),
                                                MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                }
                reduceRecordCount();
                return;
            }
        } else {
            if (!Thread.currentThread().isInterrupted()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen, Produkt \"%2\" existiert nicht in der DB",
                                                            String.valueOf(recordNo), productNo),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            }
            reduceRecordCount();
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (isSingleCall) {
                cancelImport(translateForLog("!!Abbruch/Rollback wegen Menü-Aufruf"));
            } else {
                getCatalogImportWorker().handleDeletedModules(this);
            }
        }
        super.postImportTask();
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', withHeader, headerNames));
        }
        return false;
    }

    private class BTDPImportHelper extends MADImportHelper {

        private btdtPictureKey pictureKey = null;

        public BTDPImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (sourceField.equals(BTDP_SDA) || sourceField.equals(BTDP_SDB)) {
                value = getMADDateTimeValue(value);
            } else if (sourceField.equals(ELBT_ADAT)) {
                value = getMADDateTimeValue(value);
                value = StrUtils.cutIfLongerThan(value, 8);
            }
            return value.trim();
        }

        public iPartsDialogBCTEPrimaryKey getPrimaryBCTEKey(AbstractDataImporter importer, Map<String, String> importRec, int recordNo) {
            return getPartListPrimaryBCTEKey(importer, recordNo, handleValueOfSpecialField(BTDP_NR, importRec),
                                             handleValueOfSpecialField(BTDP_KEY1, importRec),
                                             handleValueOfSpecialField(BTDP_POSV, importRec),
                                             handleValueOfSpecialField(BTDP_WW, importRec),
                                             handleValueOfSpecialField(BTDP_ETZ, importRec),
                                             "", handleValueOfSpecialField(BTDP_SDA, importRec));
        }

        public btdtPictureKey getPictureKey(Map<String, String> importRec) {
            if (pictureKey == null) {
                pictureKey = new btdtPictureKey(handleValueOfSpecialField(BTDP_BT, importRec));
            }
            return pictureKey;
        }

        public iPartsDataKgTuAfterSalesId getKGTUId(Map<String, String> importRec) {
            btdtPictureKey pictureKey = getPictureKey(importRec);
            iPartsDataKgTuAfterSalesId kgTuAfterSalesId = new iPartsDataKgTuAfterSalesId(handleValueOfSpecialField(BTDP_KAT, importRec),
                                                                                         pictureKey.kg, pictureKey.tu);
            return kgTuAfterSalesId;
        }

        public String getPictureName(Map<String, String> importRec) {
            btdtPictureKey pictureKey = getPictureKey(importRec);
            return pictureKey.getPictureName();
        }

        public String getHotspot(Map<String, String> importRec) {
            String bild = handleValueOfSpecialField(BTDP_BILD, importRec);
            if (!bild.isEmpty()) {
                bild = bild.substring(0, 3);
            }
            return bild;
        }

        public String getHotspotLfdNr(Map<String, String> importRec) {
            String bild = handleValueOfSpecialField(BTDP_BILD, importRec);
            if (!bild.isEmpty()) {
                bild = bild.substring(3, 5);
            }
            return bild;
        }
    }

    public class btdtPictureKey {

        public String kg;
        public String tu;
        public String znr;

        public btdtPictureKey(String kg, String tu, String znr) {
            this.kg = kg;
            this.tu = tu;
            this.znr = znr;
        }

        public btdtPictureKey(String bt) {
            this(bt.substring(0, 2), bt.substring(2, 5), bt.substring(5, 10));
        }

        public String getPictureName() {
            return "B" + kg + tu + "0" + znr;
        }
    }
}