/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonDbSelect;
import de.docware.apps.etk.base.misc.EtkEndpointHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsEDSSaaCase;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.config.iPartsNutzDokProcessingState;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsWBSaaStatesManagement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects.iPartsWSWorkBasketItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.SearchWorkBasketEDSKEMHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.SearchWorkBasketMBSKEMHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.WorkBasketNutzDokRemarkHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.saaworkbasket.*;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.controls.calendar.CalendarUtils;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.file.DWFileCoding;
import de.docware.util.misc.CompressionUtils;
import de.docware.util.misc.csv.CsvWriter;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.Tables;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class WorkbasketAutoCalculationHelper implements iPartsConst {

    public static final String WORKBASKET_EXPORT_LANGUAGE = Language.EN.getCode();
    public static final String DOWNLOAD_BUTTON_TEXT = "!!Download vorberechneter Daten";
    public static final String ZIP_FILENAME = "WORKBASKETS_";

    public static final String CALCULATED_KG_FIELDNAME = VirtualFieldsUtils.addVirtualFieldMask("CALCULATED_KG");
    public static final String CALCULATED_KG_NAME = "!!ermittelte KGs";

    public static final String CALCULATED_SUPPLIER_FIELDNAME = VirtualFieldsUtils.addVirtualFieldMask("CALCULATED_SUPPLIER");
    public static final String CALCULATED_SUPPLIER_NAME = "!!Organisations-Benennung";
    public static final String CALCULATED_SUPPLIER_DEFAULT = "!!Fehlende Lieferantenzuordnung";

    public static final List<String> RECENTLY_ADDED_FIELDS = new DwList<>();

    static {
        // Felder, die erst kürzlich hinzugekommen sind und deswegen
        // in der CSV-Datei hinten angefügt werden sollen, falls sie konfiguriert sind
        RECENTLY_ADDED_FIELDS.add(TableAndFieldName.make(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_NUTZDOK_ETS_EXTENSION));
    }

    /**
     * Bietet die neuste Datei im konfigurierten Verzeichnis zum Download an
     * Sollte das Verzeichnis leer sein oder nicht existieren gibt es eine Fehlermeldung
     */
    public static void downloadCalculatedData() {
        DWFile destDir = iPartsEditPlugin.getDirForWorkbasketsCalcAndExport();
        if (destDir == null) {
            MessageDialog.showError(TranslationHandler.translate("!!Verzeichnis nicht konfiguriert. Bitte Konfiguration prüfen."));
            return;
        }
        List<DWFile> files = destDir.listDWFiles();
        if (files.isEmpty()) {
            MessageDialog.showError(TranslationHandler.translate("!!Keine Datei zum Download verfügbar. Bitte Konfiguration prüfen."));
            return;
        }
        // Nach Dateinamen sortieren; nachdem alle Dateien gleich heißen wird nach Datum sortiert
        // wird auch nur gebraucht falls es im Verzeichnis mehrere Dateien gibt, was nicht passieren sollte
        files.sort((o1, o2) -> {
            String filename1 = o1.extractFileName(false);
            String filename2 = o2.extractFileName(false);
            return filename2.compareTo(filename1);
        });
        files.get(0).downloadFile();
    }

    public static void calculateAllWorkbaskets(EtkProject project) {
        // Eigenes EtkProject verwenden, weil als DB-Sprache Englisch verwendet werden soll und diese ansonsten im zentralen
        // EtkProject gesetzt werden würde
        EtkProject workbasketProject = EtkEndpointHelper.createProject(DWFile.get(project.getConfig().getStorageInfo()), true);
        if (workbasketProject == null) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR,
                       "Unable to create temporary project for workbaskets export.");
            return;
        }
        try {
            workbasketProject.getConfig().setCurrentDatabaseLanguage(WORKBASKET_EXPORT_LANGUAGE);
            DWFile tempDir = DWFile.createTempDirectory("daim");
            if (tempDir == null) {
                Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR,
                           "Unable to create temporary directory for workbaskets export.");
                return;
            }
            WorkbasketSupplierMapping supplierMapping = new WorkbasketSupplierMapping(workbasketProject);

            calculateSaaKemConstMissing(workbasketProject, tempDir, supplierMapping);
            calculateEdsKemWorkbasket(workbasketProject, tempDir, supplierMapping);
            calculateMbsKemWorkbasket(workbasketProject, tempDir, supplierMapping);
            calculateSaaWorkbasket(workbasketProject, tempDir, iPartsImportDataOrigin.SAP_MBS, supplierMapping);
            calculateSaaWorkbasket(workbasketProject, tempDir, iPartsImportDataOrigin.EDS, supplierMapping);
            calculateSaaWorkbasket(workbasketProject, tempDir, iPartsImportDataOrigin.SAP_CTT, supplierMapping);

            finishExport(tempDir);
        } finally {
            workbasketProject.setDBActive(false, false);
        }
    }

    public static void finishExport(DWFile tempDir) {
        DWFile destDir = iPartsEditPlugin.getDirForWorkbasketsCalcAndExport();
        if (destDir == null) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR,
                       "Unable to to copy result file. Target directory is null.");
            if (tempDir != null) {
                tempDir.deleteRecursivelyWithRepeat();
            }
            return;
        }
        for (DWFile file : destDir.listDWFiles()) {
            if (!file.deleteRecursivelyWithRepeat(1000)) {
                Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR,
                           "Error while deleting old files.");
            }
        }
        if (tempDir == null) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR,
                       "Unable to to copy result file. Temp directory is null.");
            return;
        }
        String timestamp = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
        String filename = ZIP_FILENAME + timestamp;
        DWFile zipFile = tempDir.getChild(filename + ".zip");
        try {
            CompressionUtils.zipDir(zipFile.getAbsolutePath(), tempDir.getAbsolutePath(), null, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR,
                       "Error while zipping files.");
            Logger.logExceptionWithoutThrowing(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR, e);
        }
        if (!zipFile.copy(destDir, true)) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR,
                       "Error while copying file " + zipFile.getName() + " to target directory " + destDir.getPath());
        }
        if (!tempDir.deleteRecursivelyWithRepeat()) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR,
                       "Error while deleting Temp Dir.");
        }
    }

    private static String[] createHeader(EtkProject project, EtkDisplayFields displayFields, String headerLang) {
        String[] header = new String[displayFields.getFields().size()];
        int i = 0;
        List<String> fallbackLangs = project.getDataBaseFallbackLanguages();

        for (EtkDisplayField field : displayFields.getFields()) {
            String displayText = field.getText().getTextByNearestLanguage(headerLang, fallbackLangs);
            header[i++] = displayText;
        }
        return header;
    }

    private static String[] createLine(EtkProject project, DBDataObjectAttributes attributes, EtkDisplayFields displayFields, String lineLang) {
        String[] line = new String[displayFields.getFields().size()];
        int i = 0;
        for (EtkDisplayField field : displayFields.getFields()) {
            String fieldName = field.getKey().getFieldName();
            String tableName = field.getKey().getTableName();
            EtkFieldType fieldType = field.getEtkDatabaseFieldType(project.getConfig());

            DBDataObjectAttribute attribute = attributes.getField(fieldName);
            String fieldValue = project.getVisObject().asString(tableName, fieldName, attribute, lineLang, true);

            if ((fieldType == EtkFieldType.feBoolean) && fieldValue.isEmpty()) {
                fieldValue = SQLStringConvert.booleanToPPString(false);
            }
            line[i++] = fieldValue;
        }
        return line;
    }

    private static void writeFile(EtkProject project, DWFile exportFile, EtkDisplayFields displayFields,
                                  List<DBDataObjectAttributes> attributesList, boolean append) {
        writeFile(project, exportFile, displayFields, attributesList, append, WORKBASKET_EXPORT_LANGUAGE, WORKBASKET_EXPORT_LANGUAGE);
    }

    private static void writeFile(EtkProject project, DWFile exportFile, EtkDisplayFields displayFields,
                                  List<DBDataObjectAttributes> attributesList, boolean append, String headerLang, String lineLang) {
        String[] header = null;
        if (!append) {
            header = createHeader(project, displayFields, headerLang);
        }
        try (CsvWriter csvWriter = new CsvWriter(exportFile, DWFileCoding.UTF8_BOM, '\t', CsvWriter.DEFAULT_QUOTE, CsvWriter.DEFAULT_NEWLINE,
                                                 append)) {
            if (!append) {
                csvWriter.writeNext(header);
            } else {
                csvWriter.writeNewLine(); // Newline fehlt am Ende der bisher geschriebenen Daten
            }
            if (attributesList != null) {
                for (DBDataObjectAttributes attributes : attributesList) {
                    String[] line = createLine(project, attributes, displayFields, lineLang);
                    csvWriter.writeNext(line);
                }
            }
        } catch (IOException e) {
            Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR,
                       "Error while writing file " + exportFile.getAbsolutePath());
            Logger.logExceptionWithoutThrowing(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.ERROR, e);
        }
    }

    private static WorkbasketSearchCallback createSearchCallback() {
        return new WorkbasketSearchCallback() {
            @Override
            public boolean searchWasCanceled() {
                return false; // nicht abbrechbar
            }

            @Override
            public void showProgress(int progress) {
                // absichtlich leer
            }
        };
    }

    private static WorkbasketSAASearchCallback createSearchCallbackSaa(EtkProject project) {
        return new WorkbasketSAASearchCallback() {
            @Override
            public boolean searchWasCanceled() {
                return false; // nicht abbrechbar
            }

            @Override
            public void showProgress(int progress, VarParam<Long> lastUpdateResultsCountTime) {
                // absichtlich leer
            }

            @Override
            public void addResults(boolean lastResults, Set<String> usedModelNumbers, Map<String, EtkDataObject> attribJoinMap) {
                // absichtlich leer
            }

            @Override
            public String getVisualValueOfDbValue(String tableName, String fieldName, String dbValue) {
                return project.getVisObject().asText(tableName, fieldName, dbValue, project.getDBLanguage());
            }
        };
    }

    public static void calculateSaaKemConstMissing(EtkProject project, DWFile tempDir, WorkbasketSupplierMapping supplierMapping) {
        long startTime = System.currentTimeMillis();
        String exportName = "Missing Construction SAA";
        logStartExport(exportName, startTime);
        WorkBasketNutzDokRemarkHelper saaNutzDokHelper = new WorkBasketNutzDokRemarkHelper(null, project, iPartsWSWorkBasketItem.TYPE.SAA);
        SaaConstMissingSearchHelper saaHelper = new SaaConstMissingSearchHelper(project, createSearchCallback(), saaNutzDokHelper);
        saaHelper.setForExport(true);

        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(
                iPartsConst.TABLE_DA_NUTZDOK_SAA, iPartsConst.FIELD_DNS_PROCESSING_STATE) };
        String[] whereValues = new String[]{ iPartsNutzDokProcessingState.NEW.getDBValue() };
        saaHelper.setWhereFieldsAndValues(whereTableAndFields, whereValues);

        EtkDisplayFields saaFields = buildCSVDisplayFields(project, saaHelper.displayFields);
        DBDataObjectAttributesList saaResults = new DBDataObjectAttributesList();
        saaHelper.loadFromDB(saaResults);
        addDefaultSupplierValues(saaResults);
        writeFile(project, getExportFile(tempDir, "MISSING_CONSTRUCTION_SAA"), saaFields, saaResults, false);
        logFinishExport(exportName, startTime, System.currentTimeMillis());

        startTime = System.currentTimeMillis();
        exportName = "Missing Construction KEM";
        logStartExport(exportName, startTime);
        WorkBasketNutzDokRemarkHelper kemNutzDokHelper = new WorkBasketNutzDokRemarkHelper(null, project, iPartsWSWorkBasketItem.TYPE.KEM);
        KemConstMissingSearchHelper kemHelper = new KemConstMissingSearchHelper(project, createSearchCallback(), kemNutzDokHelper);
        kemHelper.setForExport(true);
        kemHelper.copyAndModifyWhereFieldsAndValues(saaHelper);

        EtkDisplayFields kemFields = buildCSVDisplayFields(project, kemHelper.displayFields);
        DBDataObjectAttributesList kemResults = new DBDataObjectAttributesList();

        kemHelper.loadFromDB(kemResults);
        addDefaultSupplierValues(kemResults);
        writeFile(project, getExportFile(tempDir, "MISSING_CONSTRUCTION_KEM"), kemFields, kemResults, false);
        logFinishExport(exportName, startTime, System.currentTimeMillis());
    }

    public static void calculateEdsKemWorkbasket(EtkProject project, DWFile tempDir, WorkbasketSupplierMapping supplierMapping) {
        long startTime = System.currentTimeMillis();
        String exportName = "EDS KEM";
        logStartExport(exportName, startTime);
        WorkBasketNutzDokRemarkHelper nutzDokHelper = new WorkBasketNutzDokRemarkHelper(null, project,
                                                                                        iPartsWSWorkBasketItem.TYPE.KEM);
        SearchWorkBasketEDSKEMHelper helper = new SearchWorkBasketEDSKEMHelper(project, createSearchCallback(), nutzDokHelper);
        helper.setForExport(true);
        helper.setWhereFieldsAndValues(null, null);

        EtkDisplayFields displayFields = buildCSVDisplayFields(project, helper.getDisplayFields());

        DBDataObjectAttributesList results = new DBDataObjectAttributesList();
        helper.loadFromDB(results);
        results = helper.filterResultsForExport(results);
        results = helper.calculateSupplier(results, CALCULATED_KG_FIELDNAME, CALCULATED_SUPPLIER_FIELDNAME,
                                           CALCULATED_SUPPLIER_DEFAULT, supplierMapping);

        writeFile(project, getExportFile(tempDir, "EDS_KEM"), displayFields, results, false);
        logFinishExport(exportName, startTime, System.currentTimeMillis());
    }

    public static void calculateMbsKemWorkbasket(EtkProject project, DWFile tempDir, WorkbasketSupplierMapping supplierMapping) {
        long startTime = System.currentTimeMillis();
        String exportName = "MBS KEM";
        logStartExport(exportName, startTime);
        WorkBasketNutzDokRemarkHelper nutzDokHelper = new WorkBasketNutzDokRemarkHelper(null, project,
                                                                                        iPartsWSWorkBasketItem.TYPE.KEM);
        SearchWorkBasketMBSKEMHelper helper = new SearchWorkBasketMBSKEMHelper(project, createSearchCallback(), nutzDokHelper);
        helper.setForExport(true);

        helper.setWhereFieldsAndValues(null, null);

        EtkDisplayFields displayFields = buildCSVDisplayFields(project, helper.getDisplayFields());

        DBDataObjectAttributesList results = new DBDataObjectAttributesList();
        helper.loadFromDB(results);
        results = helper.filterResultsForExport(results);
        results = helper.calculateSupplier(results, CALCULATED_KG_FIELDNAME, CALCULATED_SUPPLIER_FIELDNAME,
                                           CALCULATED_SUPPLIER_DEFAULT, supplierMapping);

        writeFile(project, getExportFile(tempDir, "MBS_KEM"), displayFields, results, false);
        logFinishExport(exportName, startTime, System.currentTimeMillis());
    }

    public static void calculateSaaWorkbasket(EtkProject project, DWFile tempDir, iPartsImportDataOrigin source, WorkbasketSupplierMapping supplierMapping) {
        long startTime = System.currentTimeMillis();
        String exportName = "SAA " + source.name();
        logStartExport(exportName, startTime);

        WorkBasketNutzDokRemarkHelper nutzDokHelper = new WorkBasketNutzDokRemarkHelper(null, project,
                                                                                        iPartsWSWorkBasketItem.TYPE.SAA);

        AbstractSearchWorkBasketHelper helper;
        if (source == iPartsImportDataOrigin.SAP_MBS) {
            helper = new SearchWorkBasketHelperMBS(project, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_MBS,
                                                   createSearchCallbackSaa(project), nutzDokHelper);
        } else if (source == iPartsImportDataOrigin.SAP_CTT) {
            helper = new SearchWorkBasketHelperCTT(project, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_CTT,
                                                   createSearchCallbackSaa(project), nutzDokHelper);
        } else {
            helper = new SearchWorkBasketHelperEDS(project, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_EDS,
                                                   createSearchCallbackSaa(project), nutzDokHelper);
        }
        helper.setForExport(true);
        EtkDisplayFields displayFields = buildCSVDisplayFields(project, helper.getDisplayFields());

        Set<String> allModelTypes = new TreeSet<>();
        // alle Nicht-DIALOG/PSK-Produkte bestimmen und deren Typkennzahl ermitteln
        for (iPartsProduct product : iPartsProduct.getAllProducts(project)) {
            if (!product.getDocumentationType().isPKWDocumentationType() && !product.isPSK()) {
                Set<String> productModelTypes = product.getAllModelTypes(project);
                allModelTypes.addAll(productModelTypes);
            }
        }
        Set<String> existingModelTypes = getExistingModelTypes(project, source);
        if (!existingModelTypes.isEmpty()) {
            allModelTypes.removeIf(modelType -> {
                return !existingModelTypes.contains(modelType);
            });
        }
//        //!! nur zum Debuggen
//        String debugModelType = allModelTypes.iterator().next();
//        allModelTypes.clear();
//        allModelTypes.add(debugModelType);
//        //!! nur zum Debuggen Ende

        int threadCount = iPartsEditPlugin.getPluginConfig().getConfigValueAsInteger(iPartsEditPlugin.CONFIG_AUTO_CALC_WORKBASKETS_THREAD_COUNT);
        Session session = Session.get();

        String filename;
        if (source == iPartsImportDataOrigin.SAP_MBS) {
            filename = "MBS_SAA";
        } else if (source == iPartsImportDataOrigin.SAP_CTT) {
            filename = "SAP_CTT";
        } else {
            filename = "EDS_SAA";
        }
        DWFile exportFile = getExportFile(tempDir, filename);
        writeFile(project, exportFile, displayFields, null, false); // Zunächst nur den Header schreiben

        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG,
                   "Running " + threadCount + " parallel threads for calculating " + allModelTypes.size() + " model types ("
                   + exportName + ")");

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        try {
            // suche pro Typkennzahl in parallelem Thread suchen
            for (String modelType : allModelTypes) {
                executorService.execute(() -> {
                    Runnable calculationRunnable = () -> {
                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG,
                                   "Starting calculation for model type \"" + modelType + "\" (" + exportName + ")...");
                        String modelTypeSearchValue = modelType + "*";

                        Map<String, AbstractSearchWorkBasketHelper.KatalogData> attributesKatMap = Collections.synchronizedMap(new HashMap<>());
                        Map<AssemblyId, String> assemblyProductMap = Collections.synchronizedMap(new HashMap<>());
                        iPartsWBSaaStatesManagement stateManager = new iPartsWBSaaStatesManagement(project, source);

                        WorkBasketNutzDokRemarkHelper localNutzDokHelper = new WorkBasketNutzDokRemarkHelper(null, project,
                                                                                                             iPartsWSWorkBasketItem.TYPE.SAA);
                        AbstractSearchWorkBasketHelper searchHelper;
                        if (source == iPartsImportDataOrigin.SAP_MBS) {
                            searchHelper = new SearchWorkBasketHelperMBS(project, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_MBS,
                                                                         createSearchCallbackSaa(project), localNutzDokHelper);
                        } else if (source == iPartsImportDataOrigin.SAP_CTT) {
                            searchHelper = new SearchWorkBasketHelperCTT(project, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_CTT,
                                                                         createSearchCallbackSaa(project), localNutzDokHelper);
                        } else {
                            searchHelper = new SearchWorkBasketHelperEDS(project, iPartsDataVirtualFieldsDefinition.TABLE_WORK_BASKET_EDS,
                                                                         createSearchCallbackSaa(project), localNutzDokHelper);
                        }
                        searchHelper.setForExport(true);
                        // ????
                        searchHelper.reset(assemblyProductMap, attributesKatMap, stateManager);

                        searchHelper.doSearchChangesets(assemblyProductMap, "", "", modelTypeSearchValue, source);
                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG,
                                   "Search in change sets finished for model type \"" + modelType + "\" (" + exportName + ")");

                        Map<String, String> searchValuesAndFields = new HashMap<>();
                        searchValuesAndFields.put(TableAndFieldName.make(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO),
                                                  modelTypeSearchValue);
                        searchValuesAndFields.put(TableAndFieldName.make("", FIELD_WSC_SOURCE), source.getOrigin());
                        searchHelper.setSearchValuesAndFields(searchValuesAndFields);

                        Set<String> usedModelNumbers = new HashSet<>();
                        Set<String> modelNumbers = new HashSet<>();
                        Map<String, EtkDataObject> attribJoinMap = new LinkedHashMap<>();

                        Set<iPartsEDSSaaCase> saaCases = new HashSet<>();
                        saaCases.add(iPartsEDSSaaCase.EDS_CASE_NEW);
                        saaCases.add(iPartsEDSSaaCase.EDS_CASE_VALIDITY_EXPANSION);
                        EtkDisplayFields externalSelectFields = new EtkDisplayFields();
                        externalSelectFields.addFeld(new EtkDisplayField(TABLE_DA_NUTZDOK_SAA,
                                                                         FIELD_DNS_GROUP, false, false));

                        // wird für die Bestimmung der Lieferanten benötigt (KG aus Nutzdok)
                        searchHelper.setNeedsNutzDokJoin(true);

                        searchHelper.loadFromDB(modelNumbers, saaCases, usedModelNumbers, attribJoinMap, externalSelectFields);
                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG,
                                   "Search in database finished for model type \"" + modelType + "\" with " + attribJoinMap.size()
                                   + " results (" + exportName + ")");

                        // nach der Suche die exakteren Geschäftsfälle hinzufügen für den Filter
                        saaCases.add(iPartsEDSSaaCase.EDS_CASE_MODEL_VALIDITY_EXPANSION);
                        saaCases.add(iPartsEDSSaaCase.EDS_CASE_SAA_VALIDITY_EXPANSION);

                        String today = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());
                        stateManager.addModels(usedModelNumbers);
                        List<DBDataObjectAttributes> collectedResults = new DBDataObjectAttributesList();
                        Iterator<Map.Entry<String, EtkDataObject>> attribJoinMapIterator = attribJoinMap.entrySet().iterator();
                        while (attribJoinMapIterator.hasNext()) {
                            EtkDataObject dataObject = attribJoinMapIterator.next().getValue();
                            attribJoinMapIterator.remove(); // Speicher freigeben so früh wie möglich
                            DBDataObjectAttributes attributes = searchHelper.calculateVirtualFields(dataObject.getAttributes(),
                                                                                                    attributesKatMap, assemblyProductMap,
                                                                                                    today);
                            if (attributes != null) {
                                iPartsEDSSaaCase caseValue = iPartsEDSSaaCase.getFromDBValue(attributes.getFieldValue(searchHelper.getFieldSaaCase()));
                                if (saaCases.contains(caseValue)) { // Filtern nach Geschäftsfall
                                    addCachedTexts(searchHelper, displayFields, attributes);
                                    collectedResults.addAll(searchHelper.calculateSupplier(
                                            attributes, CALCULATED_KG_FIELDNAME, CALCULATED_SUPPLIER_FIELDNAME,
                                            CALCULATED_SUPPLIER_DEFAULT, supplierMapping));
                                }
                            }
                        }
                        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG,
                                   "Calculation finished for model type \"" + modelType + "\" (" + exportName + ")");

                        if (!collectedResults.isEmpty()) {
                            synchronized (exportFile) {
                                writeFile(project, exportFile, displayFields, collectedResults, true); // Teilergebnis für aktuelle Typkennzahl ergänzen
                            }
                        }
                    };
                    if (session != null) {
                        session.runInSession(calculationRunnable);
                    } else {
                        calculationRunnable.run();
                    }
                });
            }
        } finally {
            // Alle gewünschten Typkennzahlen wurden zum ExecutorService hinzugefügt -> Shutdown aufrufen und warten bis alle
            // Tasks abgearbeitet wurden
            executorService.shutdown();
        }
        boolean finished;
        try {
            finished = executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            finished = false;
        }

        if (!finished) {
            exportFile.delete(); // Export-Datei wieder löschen, weil das Ergebnis unvollständig wäre
        }

        logFinishExport(exportName, startTime, System.currentTimeMillis());
    }

    private static DWFile getExportFile(DWFile tempDir, String filename) {
        return tempDir.getChild(filename + "." + MimeTypes.EXTENSION_CSV);
    }

    private static void addFollowUpDisplayFieldIfNeccessary(EtkProject project, EtkDisplayFields displayFields) {
        int index = displayFields.getIndexOfFeld(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_FOLLOWUP_DATE, false);
        if (index == -1) {
            EtkDisplayField displayField = new EtkDisplayField(ConstMissingSearchHelper.TABLE_WORK_BASKET, ConstMissingSearchHelper.FIELD_KEM_FOLLOWUP_DATE, false, false);
            displayField.loadStandards(project.getConfig());
            displayFields.addFeld(displayField);
        }
    }

    private static Set<String> getExistingModelTypes(EtkProject project, iPartsImportDataOrigin source) {
        Set<String> result = new HashSet<>();
        List<String> fromFields = new ArrayList<>();
        String modelNoFieldName = TableAndFieldName.make(TABLE_DA_WB_SAA_CALCULATION, FIELD_WSC_MODEL_NO);
        fromFields.add(modelNoFieldName);

        EtkSqlCommonDbSelect sqlSelect = new EtkSqlCommonDbSelect(project, TABLE_DA_WB_SAA_CALCULATION);
        sqlSelect.getQuery().selectDistinct(new Fields(fromFields));
        sqlSelect.getQuery().from(new Tables(TABLE_DA_WB_SAA_CALCULATION));

        Condition condition = new Condition(FIELD_WSC_SOURCE, Condition.OPERATOR_EQUALS, source.getOrigin());

        sqlSelect.getQuery().where(condition);
        DBDataSet ds = sqlSelect.createAbfrage();

        try {
            while (ds.next()) {
                EtkRecord record = ds.getRecord(fromFields);
                String modelNo = record.getField(modelNoFieldName).getAsString();
                if (StrUtils.isValid(modelNo)) {
                    iPartsModelId modelId = new iPartsModelId(modelNo);
                    result.add(modelId.getModelTypeNumber());
                }
            }
        } catch (Exception e) {
            result.clear();
            Logger.logExceptionWithoutThrowing(LogChannels.UNEXPECTED, LogType.ERROR, e);
        } finally {
            ds.close();
        }

        return result;
    }

    private static void addCachedTexts(AbstractSearchWorkBasketHelper searchHelper, EtkDisplayFields displayFields, DBDataObjectAttributes attributes) {
        // Die Baumustertexte aus dem Cache holen -> aus den selectFields entfernen
        for (EtkDisplayField displayField : displayFields.getFields()) {
            String fieldName = displayField.getKey().getFieldName();
            String tableName = displayField.getKey().getTableName();
            if (tableName.equals(TABLE_DA_MODEL) && (fieldName.equals(FIELD_DM_NAME) || fieldName.equals(FIELD_DM_SALES_TITLE)
                                                     || fieldName.equals(FIELD_DM_ADD_TEXT))) {

                String value = searchHelper.getModelValueForHint(fieldName, attributes);
                attributes.addField(fieldName, value, DBActionOrigin.FROM_DB);
            }
        }
    }

    private static void logStartExport(String exportName, long startTime) {
        String formattedTimestamp = "(" + CalendarUtils.format(startTime, CalendarUtils.DEFAULT_STYLE_LOCALE_PATTERN,
                                                               Language.EN.getCode()) + ") ";

        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG,
                   "Started Workbasket Export: " + exportName + " " + formattedTimestamp);
    }

    private static void logFinishExport(String exportName, long startTime, long currentTime) {
        String formattedTimestamp = "(" + CalendarUtils.format(currentTime, CalendarUtils.DEFAULT_STYLE_LOCALE_PATTERN,
                                                               Language.EN.getCode()) + ") ";
        String formattedDuration = DateUtils.formatTimeDurationString(currentTime - startTime, false, false,
                                                                      Language.EN.getCode());
        Logger.log(iPartsEditPlugin.LOG_CHANNEL_WORKBASKETS_CALC_AND_EXPORT, LogType.DEBUG,
                   "Finished Workbasket Export: " + exportName + "; Duration: " + formattedDuration + " " + formattedTimestamp);
    }

    private static void addDisplayfieldsForCalculatedSupplier(EtkProject project, EtkDisplayFields displayFields) {
        List<String> viewerLanguages = project.getConfig().getViewerLanguages();
        EtkDisplayField kgField = new EtkDisplayField(CALCULATED_KG_FIELDNAME, false, false);
        kgField.setText(new EtkMultiSprache(CALCULATED_KG_NAME, viewerLanguages));
        displayFields.addFeld(kgField);

        EtkDisplayField supplierField = new EtkDisplayField(CALCULATED_SUPPLIER_FIELDNAME, false, false);
        supplierField.setText(new EtkMultiSprache(CALCULATED_SUPPLIER_NAME, viewerLanguages));
        displayFields.addFeld(supplierField);
    }

    private static void moveDisplayFieldsToEnd(EtkDisplayFields displayFields) {
        if (!RECENTLY_ADDED_FIELDS.isEmpty() && !displayFields.getFields().isEmpty()) {
            for (String tableAndFieldName : RECENTLY_ADDED_FIELDS) {
                int index = displayFields.getIndexOfFeld(tableAndFieldName, false);
                if (index != -1) {
                    EtkDisplayField displayField = displayFields.getFeld(index);
                    displayFields.removeField(displayField);
                    displayFields.addFeld(displayField);
                }
            }
        }
    }

    private static EtkDisplayFields buildCSVDisplayFields(EtkProject project, EtkDisplayFields baseDisplayFields) {
        addFollowUpDisplayFieldIfNeccessary(project, baseDisplayFields);

        addDisplayfieldsForCalculatedSupplier(project, baseDisplayFields);
        moveDisplayFieldsToEnd(baseDisplayFields);
        return baseDisplayFields;
    }

    private static void addDefaultSupplierValues(DBDataObjectAttributesList attributesList) {
        // hier gibt es nicht ausreichend Informationen um Lieferanten zu berechnen, also werden Default Werte eingetragen
        for (DBDataObjectAttributes attributes : attributesList) {
            attributes.addField(CALCULATED_KG_FIELDNAME, "", DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            attributes.addField(CALCULATED_SUPPLIER_FIELDNAME, CALCULATED_SUPPLIER_DEFAULT, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        }
    }
}
