/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.*;

public class MADApplicationListImporter extends AbstractDataImporter implements iPartsConst, EtkDbConst {

    final static String PRODUCT_NO = "PRODUCT_NO";
    final static String PRODUCT_GRP = "PRODUCT_GRP";
    final static String AREA_CODE = "ADREA_CODE";
    final static String ASSORTMENT_CLASS = "ASS_CLASS";
    final static String DOC_METHOD = "DOC_METHOD";
    final static String COMMENT = "COMMENT";
    final static String EPC_RELEVANT = "VISIBLE";
    final static String KATALOG_STATUS = "STATE";
    final static String POWER_SYSTEMS = "POWER_SYS";
    final static String EPC_DATE = "EPC_DATE";
    final static String JAPAN = "JP";
    final static String RUSSIAN = "RU";
    final static String TURKISH = "TU";
    final static String CHINESE = "CH";
    final static String EDAT = "EDAT";
    final static String ADAT = "ADAT";

    private String[] headerNames = new String[]{
            PRODUCT_NO,
            PRODUCT_GRP,
            AREA_CODE,
            ASSORTMENT_CLASS,
            DOC_METHOD,
            COMMENT,
            EPC_RELEVANT,
            KATALOG_STATUS,
            POWER_SYSTEMS,
            EPC_DATE,
            JAPAN,
            RUSSIAN,
            TURKISH,
            CHINESE,
            EDAT,
            ADAT
    };

    private String tableName = TABLE_DA_PRODUCT;
    private String[] primaryKeysAppListImport;
    private HashMap<String, String> mappingAppListData;
    private boolean importToDB = true;
    private boolean doBufferSave = true;
    private Map<String, Set<String>> areaCodeToCountryCodesMapping;
    private Map<String, String> ac2pcMappingList; // Zuordnungstabelle von Sortimentsklasse ==> After Sales Produktklasse

    /**
     * Constructor für XML-Datei und MQMessage Import
     *
     * @param project
     */
    public MADApplicationListImporter(EtkProject project) {
        super(project, "MAD Applikationsliste",
              new FilesImporterFileListType(TABLE_DA_PRODUCT, "!!MAD Applikationsliste", true, false, false,
                                            new String[]{ FILE_EXTENSION_NO_HEADER, MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_CSV,
                                                          MimeTypes.EXTENSION_ALL_FILES }));
        initMapping();
    }

    public MADApplicationListImporter(EtkProject project, Date datasetDate) {
        this(project);
        if (datasetDate != null) {
            setDatasetDate(datasetDate);
        }
    }

    private void initMapping() {
        primaryKeysAppListImport = new String[]{ PRODUCT_NO };
        mappingAppListData = new HashMap<>();
        mappingAppListData.put(FIELD_DP_EPC_RELEVANT, EPC_RELEVANT);
        mappingAppListData.put(FIELD_DP_COMMENT, COMMENT);
        ac2pcMappingList = new HashMap<>();
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
        importer.setMustExists(StrUtils.mergeArrays(primaryKeysAppListImport, new String[]{ COMMENT, EPC_RELEVANT }));
        importer.setMustHaveData(primaryKeysAppListImport);
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        if (!importer.isRecordValid(importRec, errors)) {
            return false;
        }
        return true;
    }

    /**
     * Import eines Records
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        ApplicationListHelper helper = new ApplicationListHelper(getProject(), mappingAppListData, tableName);
        String productNo = helper.handleValueOfSpecialField(PRODUCT_NO, importRec);
        if (productNo.trim().length() != 3) {
            // Nur Produkte mit 3-stelliger Nr. importieren (keine SA-Kataloge)
            reduceRecordCount();
            return;
        }

        iPartsProductId productId = new iPartsProductId(productNo);
        iPartsDataProduct product = new iPartsDataProduct(getProject(), productId);
        boolean isNew = !product.loadFromDB(productId);
        if (isNew) {
            // Produkt existiert nicht in DB => anlegen
            product.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
            product.setFieldValue(FIELD_DP_STRUCTURING_TYPE, PRODUCT_STRUCTURING_TYPE.KG_TU.toString(), DBActionOrigin.FROM_EDIT);

            // Migrationsquelle auf Applikationsliste setzen für spätere Unterscheidung ob die Daten überschrieben werden dürfen
            product.setFieldValue(FIELD_DP_SOURCE, iPartsImportDataOrigin.APP_LIST.getOrigin(), DBActionOrigin.FROM_EDIT);
        } else {
            iPartsDocumentationType documentationType = product.getDocumentationType();
            if (documentationType.isPKWDocumentationType()) {
                getMessageLog().fireMessage(translateForLog("!!Record %1 übersprungen. Produkt \"%2\" existiert bereits mit Dokumentationsmethode \"%3\"",
                                                            String.valueOf(recordNo), productNo, documentationType.name()),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                reduceRecordCount();
                return;
            }
        }

        // Produktnummer als Titel setzen, wenn Quelle EPC oder APPLIKATIONSLISTE
        iPartsImportDataOrigin dataOrigin = iPartsImportDataOrigin.getTypeFromCode(product.getFieldValue(FIELD_DP_SOURCE));
        if ((dataOrigin == iPartsImportDataOrigin.APP_LIST) || (dataOrigin == iPartsImportDataOrigin.EPC)) {
            helper.setProductTitle(product, "");
        }


        // Migrationsdatum und Datenstand solange überschreiben bis bei der Quelle etwas anderes als APP_LIST eingetragen wurde
        String dataSource = product.getFieldValue(FIELD_DP_SOURCE);
        if (dataSource.equals(iPartsImportDataOrigin.APP_LIST.getOrigin())) {
            product.setMigrationDate(getMigrationDate());
            product.setDatasetDate(getDatasetDate());
        }
        // Die Werte sollen von der Applikationsliste immer überschrieben werden
        helper.fillOverrideCompleteDataForMADReverse(product, importRec, iPartsMADLanguageDefs.MAD_DE);

        Set<String> asProductClasses = new HashSet<>(product.getFieldValueAsSetOfEnum(FIELD_DP_ASPRODUCT_CLASSES));
        // DAIMLER-7191: AS Produktklasse wird befüllt bei neu angelegten Produkten, oder bei
        // EPC-Produkten bei denen die AS Produktklasse bisher leer war
        boolean isEPC = dataSource.equals(iPartsImportDataOrigin.EPC.getOrigin());
        if (isNew || (isEPC && asProductClasses.isEmpty())) {
            asProductClasses.clear(); // sollte hier eigentlich schon leer sein
            Set<String> assortmentClassList = helper.getAssortmentClasses(importRec);
            if (assortmentClassList != null) {
                for (String assortmentClass : assortmentClassList) {
                    String asProductClass = ac2pcMappingList.get(assortmentClass);
                    if (asProductClass == null) {
                        asProductClass = helper.convertAssortmentClassToReferencingASProductClass(assortmentClass);
                        ac2pcMappingList.put(assortmentClass, asProductClass);
                    }
                    asProductClasses.add(asProductClass);
                }
            }
        }

        // Powersystems soll zusätzlich auch als AS Produktklasse hinzugefügt werden
        boolean isPowerSytems = helper.isPowerSystems(importRec);
        if (asProductClasses.contains(AS_PRODUCT_CLASS_POWERSYSTEMS)) {
            // in der DB ist Powersystems gesetzt
            if (!isPowerSytems) {
                // aber der neue Datensatz enthält es nicht => löschen
                asProductClasses.remove(AS_PRODUCT_CLASS_POWERSYSTEMS);
            }
        } else {
            // in der DB ist Powersystems nicht gesetzt
            if (isPowerSytems) {
                // aber der neue Datensatz enthält es => hinzufügen
                asProductClasses.add(AS_PRODUCT_CLASS_POWERSYSTEMS);
            }
        }
        product.setFieldValueAsSetOfEnum(FIELD_DP_ASPRODUCT_CLASSES, asProductClasses, DBActionOrigin.FROM_EDIT);

        // Alle Bereichscodes aus AREA_CODE über areaCodeToCountryCodesMapping in Länder-Codes konvertieren und diese am Produkt setzen
        String areaCodes = helper.handleValueOfSpecialField(AREA_CODE, importRec).trim();
        if (!areaCodes.isEmpty() && !areaCodes.contains("1")) { // Wenn 1 im AREA_CODE enthalten ist, dann sind alle Länder gültig
            Set<String> resultingCountryCodes = new TreeSet<String>();
            for (int i = 0; i < areaCodes.length(); i++) { // AREA_CODE besteht aus einzelnen Buchstaben (AreaCodes) ohne Trennzeichen
                String areaCode = areaCodes.substring(i, i + 1);
                Set<String> countryCodesForAreaCode = areaCodeToCountryCodesMapping.get(areaCode);
                if (countryCodesForAreaCode != null) {
                    resultingCountryCodes.addAll(countryCodesForAreaCode);
                }
            }
            product.setFieldValueAsSetOfEnum(FIELD_DP_VALID_COUNTRIES, resultingCountryCodes, DBActionOrigin.FROM_EDIT);
        } else { // alle Länder gültig
            product.setFieldValueAsSetOfEnum(FIELD_DP_VALID_COUNTRIES, new DwList<String>(), DBActionOrigin.FROM_EDIT);
        }

        // Überprüfung der Dokumentationsmethode und bei Änderung Vererbung an die Module
        iPartsDocumentationType docuType = iPartsDocumentationType.getFromDBValue(helper.handleValueOfSpecialField(DOC_METHOD, importRec));
        if (docuType != iPartsDocumentationType.UNKNOWN) {
            if (isNew) {
                product.setDocuMethod(docuType.getDBValue());
            } else {
                // Vererbung des DokumentationsTypen vom Produkt an bestehende Module, falls nötig
                product.setDocumentationTypeAndInheritToModules(docuType);
            }
        }
        if (product.isModifiedWithChildren()) {
            product.refreshModificationTimeStamp(); // DAIMLER-4841: Änderungszeitstempel am Produkt setzen
        }
        if (importToDB) {
            saveToDB(product);
        }
    }

    /**
     * Löscht alle vorhandenen Daten für den übergebenen Import-Dateilisten-Typ.
     * Diese Methode betrifft nur den Import per Dialog, nicht per MQ.
     * Wenn das Löschen der vorhandenen Daten nicht erlaubt sein sein, gibt man false zurück.
     * Für Testdaten sollte die Methode implementiert werden.
     *
     * @param importFileType
     * @return {@code true} falls das Entfernen erfolgreich war (allerdings werden wir bei Fehler in eine Exception laufen und nicht nach false; so ist jedenfalls überall implementiert)
     */
    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        // durch diesen Importer soll nichts gelöscht werden können
        return false;
    }

    /**
     * Importiert die Datei für den übergebenen Import-Dateilisten-Typ.
     *
     * @param importFileType
     * @param importFile
     * @return {@code true} falls der Import erfolgreich war
     */
    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(tableName)) {
            if (MimeTypes.hasExtension(importFile, MimeTypes.EXTENSION_GZ)) {
                return importMasterData(prepareImporterKeyValueGZ(importFile, tableName, headerNames));
            } else if (MimeTypes.hasExtension(importFile, FILE_EXTENSION_NO_HEADER)) {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', false, headerNames));
            } else {
                return importMasterData(prepareImporterKeyValue(importFile, tableName, ';', true, null));
            }
        }
        return false;
    }

    @Override
    protected void preImportTask() {
        areaCodeToCountryCodesMapping = new HashMap<String, Set<String>>();
        DBDataObjectAttributesList regionToCountryCodesList = getProject().getDbLayer().getAttributesList(TABLE_DA_COUNTRY_CODE_MAPPING);
        for (DBDataObjectAttributes regionToCountryCodes : regionToCountryCodesList) {
            String areaCode = regionToCountryCodes.getFieldValue(FIELD_DCM_REGION_CODE);
            Set<String> countryCodes = areaCodeToCountryCodesMapping.get(areaCode);
            if (countryCodes == null) {
                countryCodes = new TreeSet<String>();
                areaCodeToCountryCodesMapping.put(areaCode, countryCodes);
            }
            countryCodes.addAll(SetOfEnumDataType.parseSetofEnum(regionToCountryCodes.getFieldValue(FIELD_DCM_COUNTRY_CODES), false, false));
        }

        super.preImportTask();
        setBufferedSave(doBufferSave);
    }

    private class ApplicationListHelper extends MADImportHelper {

        public ApplicationListHelper(EtkProject project, HashMap<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }

        @Override
        protected String handleValueOfSpecialField(String sourceField, String value) {
            return value;
        }

        protected boolean isPowerSystems(String value) {
            if (!StrUtils.isEmpty(value)) {
                if ((value.trim().length() == 1) && (value.toLowerCase().charAt(0) == 'p')) {
                    return true;
                }
            }
            return false;
        }

        protected boolean isPowerSystems(Map<String, String> importRec) {
            return isPowerSystems(importRec.get(POWER_SYSTEMS));
        }

        protected Set<String> getAssortmentClasses(Map<String, String> importRec) {
            String assortmentClasses = handleValueOfSpecialField(ASSORTMENT_CLASS, importRec);
            if (assortmentClasses != null) {
                Set<String> result = new HashSet<>();
                for (int lfdNr = 0; lfdNr < assortmentClasses.length(); lfdNr++) {
                    String enumValue = String.valueOf(assortmentClasses.charAt(lfdNr));
                    result.add(enumValue);
                }
                return result;
            }
            return null;
        }
    }
}