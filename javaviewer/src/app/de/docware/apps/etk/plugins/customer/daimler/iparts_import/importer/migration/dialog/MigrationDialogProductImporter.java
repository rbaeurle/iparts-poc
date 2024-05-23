/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.MADImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

/**
 * Migrations DIALOG Katalog-Importer für Produkte
 */
public class MigrationDialogProductImporter extends AbstractCatalogDataImporter implements iPartsConst, EtkDbConst {

    final static String KAT_BRKT_NR = "BRKT_NR";                     //Baureihe
    final static String KAT_BRKT_KAT = "BRKT_KAT";                   //Katalog-Nr
    final static String KAT_KATE_AKZ = "KATE_AKZ";                   //Aggregate-KZ Enum: Aggregat
    final static String KAT_APPL_PG = "APPL_PG";                     //Produktgruppe
    final static String KAT_APPL_BCOD = "APPL_BCOD";                 //Bereichscode Enum: AreaCode SetOfEnum
    final static String KAT_APPL_SK = "APPL_SK";                     //Sortimentsklassen Enum: AssortmentClasses SetOfEnum
    final static String KAT_APPL_DOKUMETHODE = "APPL_DOKUMETHODE";   //
    final static String KAT_APPL_BEM = "APPL_BEM";                   //
    final static String KAT_APPL_EPC_RELEVANT = "APPL_EPC_RELEVANT"; //
    final static String KAT_APPL_KZ_DELTA = "APPL_KZ_DELTA";         //
    final static String KAT_APPL_KAT_KENNER = "APPL_KAT_KENNER";     //

    // Sortimentsklasse für den Spezialfall SMART
    final static String SMART_ASSORTMENT_CLASS = "F";

    private String[] headerNames = new String[]{
            KAT_BRKT_NR,
            KAT_BRKT_KAT,
            KAT_KATE_AKZ,
            KAT_APPL_PG,
            KAT_APPL_BCOD,
            KAT_APPL_SK,
            KAT_APPL_DOKUMETHODE,
            KAT_APPL_BEM,
            KAT_APPL_EPC_RELEVANT,
            KAT_APPL_KZ_DELTA,
            KAT_APPL_KAT_KENNER };

    private HashMap<String, String> mappingKATData;
    private String[] primaryKeysKATImport;
    private String tableName = TABLE_DA_PRODUCT;
    private Map<String, String> aggsMapping;

    // Zuordnungstabelle von Sortimentsklasse ==> After Sales Produktklasse
    private Map<String, String> ac2pcMappingList;

    private boolean importToDB = false;

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MigrationDialogProductImporter(EtkProject project, boolean withHeader) {
        super(project, "DIALOG Katalog", withHeader,
              new FilesImporterFileListType(TABLE_DA_PRODUCT, "!!DIALOG Katalog", true, false, false, new String[]{ MimeTypes.EXTENSION_CSV, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    public MigrationDialogProductImporter(EtkProject project, boolean withHeader, Date datasetDate) {
        this(project, withHeader);
        if (datasetDate != null) {
            setDatasetDate(datasetDate);
        }
    }

    private void initMapping() {
        aggsMapping = new HashMap<String, String>();

        // Zuordnungstabelle von Sortimentsklasse ==> After Sales Produktklasse
        ac2pcMappingList = new HashMap<String, String>();

        primaryKeysKATImport = new String[]{ KAT_BRKT_NR, KAT_BRKT_KAT };
        mappingKATData = new HashMap<String, String>();
        //Die Felder KAT_APPL_BCOD und KAT_APPL_SK werden wegen SetOfEnum getrennt behandelt
        mappingKATData.put(FIELD_DP_SERIES_REF, KAT_BRKT_NR);
        mappingKATData.put(FIELD_DP_PRODUCT_NO, KAT_BRKT_KAT);
//        mappingKATData.put(FIELD_DP_AGGREGATE_TYPE, KAT_KATE_AKZ);
        mappingKATData.put(FIELD_DP_PRODUCT_GRP, KAT_APPL_PG);
//        mappingKATData.put(FIELD_DP_STRUCTURING_TYPE, KAT_APPL_PG);
        mappingKATData.put(FIELD_DP_DOCU_METHOD, KAT_APPL_DOKUMETHODE);
        // PRODUCT_VISIBLE wird in Applikationsliste gesetzt, und soll nicht überschrieben werden
//        mappingKATData.put(FIELD_DP_PRODUCT_VISIBLE, KAT_APPL_EPC_RELEVANT);
        mappingKATData.put(FIELD_DP_KZ_DELTA, KAT_APPL_KZ_DELTA);
        // wird nicht in die Datenbank geschrieben
//        mappingKATData.put(FIELD, KAT_APPL_KAT_KENNER);

    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(primaryKeysKATImport);
        importer.setMustHaveData(primaryKeysKATImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //nur für den EinzelTest (Menu im Plugin)
        if (getCatalogImportWorker() == null) {
            setCatalogImportWorker(new iPartsCatalogImportWorker(getProject(), getDatasetDate()));
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
    protected void postImportTask() {
        super.postImportTask();
        aggsMapping.clear();
        ac2pcMappingList.clear();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        KATImportHelper importHelper = new KATImportHelper(getProject(), mappingKATData, tableName);
        String catalogNo = importHelper.handleValueOfSpecialField(KAT_BRKT_KAT, importRec);
        iPartsDataProduct dataProduct = getCatalogImportWorker().createProductData(this, catalogNo, DBActionOrigin.FROM_EDIT);
        if (dataProduct == null) {
            reduceRecordCount();
            return;
        }

        importHelper.fillOverrideCompleteDataForMADReverse(dataProduct, importRec, iPartsMADLanguageDefs.MAD_DE);

        // Falls das Produkt bereits existiert, EPC relevant nicht überschreiben. Falls das Produkt neu angelegt wurde, schon
        if (dataProduct.isNew()) {
            String productIsEpcRelevant = importHelper.handleValueOfSpecialField(KAT_APPL_EPC_RELEVANT, importRec);
            dataProduct.setFieldValueAsBoolean(FIELD_DP_EPC_RELEVANT, SQLStringConvert.ppStringToBoolean(productIsEpcRelevant),
                                               DBActionOrigin.FROM_EDIT);
        }

        //Baureihe wird nicht importiert
        //getCatalogImportWorker().addProductSeries(this, dataProduct, seriesNo, DBActionOrigin.FROM_EDIT);

        importHelper.setProductTitle(dataProduct, importHelper.handleValueOfSpecialField(KAT_APPL_BEM, importRec));

        dataProduct.setFieldValue(FIELD_DP_STRUCTURING_TYPE, PRODUCT_STRUCTURING_TYPE.KG_TU.name(), DBActionOrigin.FROM_EDIT);
        // Falls das aktuelle Aggregate-Kennzeichen "GM" oder "GA" ist und das neue ein "G" -> nicht überschreiben
        String newAggType = importHelper.handleValueOfSpecialField(KAT_KATE_AKZ, importRec);
        if (!importHelper.checkIfSpecialAggTypeCase(dataProduct.getAggType(), newAggType)) {
            dataProduct.setFieldValue(FIELD_DP_AGGREGATE_TYPE, newAggType, DBActionOrigin.FROM_EDIT);
        }
        //weitere Attribute setzen
        List<String> sortimentsKlassen = getEnumList(importHelper.handleValueOfSpecialField(KAT_APPL_SK, importRec));
        dataProduct.setFieldValueAsSetOfEnum(FIELD_DP_ASSORTMENT_CLASSES, sortimentsKlassen, DBActionOrigin.FROM_EDIT);

        // Test auf Smart. Bei den entsprechenden Katalog-Kennern explizit die AS-Produktklasse SMART zusätzlich zu den Sortimentsklassen setzen.
        List<String> afterSalesProductClasses = new ArrayList<String>();
        String applKatKenner = importRec.get(KAT_APPL_KAT_KENNER);
        if (applKatKenner != null) {
            if (applKatKenner.equals("MC1") || applKatKenner.equals("MC2")) {
                afterSalesProductClasses.add(SMART_ASSORTMENT_CLASS); //
            }
        }

        // Sortimentsklassen in After Sales Produktklassen umwandeln
        // Über die Liste der Sortimentsklassen iterieren und für jedes Element eine AS-Produktklasse anhängen, falls noch nicht vorhanden.
        DIALOGImportHelper helper = new DIALOGImportHelper(getProject(), null, "");
        for (String assortmentClass : sortimentsKlassen) {
            // Die gecachten Werte:
            // Erst versuchen den übersetzten Wert aus der Mapping-Tabelle zu holen und nur wenn er nicht darin enthalten ist ...
            String mappedValue = ac2pcMappingList.get(assortmentClass);
            if (mappedValue == null) {
                // ... ihn aus der Datenbank lesen ...
                mappedValue = helper.convertAssortmentClassToReferencingASProductClass(assortmentClass);
                // ... und für die nächste Sortimentsklassenzuordnung zwischenpuffern.
                ac2pcMappingList.put(assortmentClass, mappedValue);
            }
            // Zu mehreren Sortimentsklassen gibt es die gleiche AS Produktklasse (also [n:1])
            // Sicherstellen, dass die gefundene AS Produktklasse nur einmal in der Zielliste vorhanden ist.
            if (!afterSalesProductClasses.contains(mappedValue)) {
                afterSalesProductClasses.add(mappedValue);
            }
        }
        // Falls in den original AS Produktklassen Powersystems enthalten ist, nicht überschreiben
        List<String> originalASClasses = dataProduct.getFieldValueAsSetOfEnum(FIELD_DP_ASPRODUCT_CLASSES);
        if (originalASClasses.contains(AS_PRODUCT_CLASS_POWERSYSTEMS)) {
            afterSalesProductClasses.add(AS_PRODUCT_CLASS_POWERSYSTEMS);
        }
        // Die After Sales Produktklassen speichern.
        if (!afterSalesProductClasses.isEmpty()) {
            dataProduct.setFieldValueAsSetOfEnum(FIELD_DP_ASPRODUCT_CLASSES, afterSalesProductClasses, DBActionOrigin.FROM_EDIT);
        }

        dataProduct.setFieldValueAsBoolean(FIELD_DP_MIGRATION, true, DBActionOrigin.FROM_EDIT);
        dataProduct.setFieldValue(FIELD_DP_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
        dataProduct.setDatasetDate(getDatasetDate());
        dataProduct.setMigrationDate(getMigrationDate());
        dataProduct.refreshModificationTimeStamp(); // DAIMLER-4841: Änderungszeitstempel am Produkt setzen
        if (importToDB) {
            saveToDB(dataProduct);
        }
    }

    private List<String> getEnumList(String value) {
        List<String> result = new ArrayList<String>(value.length());
        for (int lfdNr = 0; lfdNr < value.length(); lfdNr++) {
            String enumValue = String.valueOf(value.charAt(lfdNr));
            if (!result.contains(enumValue)) {
                result.add(enumValue);
            }
        }
        return result;
    }

    public static String getStructureParent(String appl_AKZ, String appl_PG) {
        String result = appl_AKZ;
        if (appl_AKZ.equals(iPartsConst.AGGREGATE_TYPE_CAR)) {
            result = STRUCT_PKW_NAME;
            if (!appl_PG.isEmpty() && appl_PG.length() >= 1) {
                switch (appl_PG.charAt(0)) {
                    case 'A':  // AGGREGATE
                        break;
                    case 'B':  // OMNIBUS
                        break;
                    case 'C':  // CHRYSLER
                        break;
                    case 'E':  // MBN
                        break;
                    case 'F':  // MCC SMART
                        break;
                    case 'G':  // GELAENDEWAGEN
                        break;
                    case 'I':  // INDUSTRIEMOTOREN
                        break;
                    case 'L':  // LIGHT TRUCK CONCEPT
                        break;
                    case 'M':  // MOTOREN
                        result = "M";
                        break;
                    case 'N':  // LKW
                        result = STRUCT_LKW_NAME;
                        break;
                    case 'P':  // PKW
                        result = STRUCT_PKW_NAME;
                        break;
                    case 'S':  // STADTBUS
                        break;
                    case 'T':  // TRANSPORTER
                        break;
                    case 'U':  // UNIMOG
                        break;
                    case 'V':  // ??
                        break;
                    case 'X':  // BRASILIEN
                        break;
                }
            }
        }
        return result;
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


    private class KATImportHelper extends MADImportHelper {

        public KATImportHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            if (StrUtils.stringContains(value, MAD_NULL_VALUE)) {
                value = StrUtils.replaceSubstring(value, MAD_NULL_VALUE, "").trim();
            }
            if (sourceField.equals(KAT_KATE_AKZ)) {
                String mapValue = aggsMapping.get(value);
                if (mapValue == null) {
                    mapValue = handleAggTypeValue(value);
                    aggsMapping.put(value, mapValue);
                }
                value = mapValue;
            }
//            if (sourceField.equals(KAT_APPL_EPC_TERMIN)) {
//                value = getMADDateTimeValue(value);
//            }
            return value;
        }

    }
}