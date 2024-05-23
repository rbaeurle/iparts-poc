/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.functions.EtkFunction;
import de.docware.apps.etk.base.importer.base.model.AbstractKeyValueRecordReader;
import de.docware.apps.etk.base.importer.base.model.KeyValueRecordGzTarFileReader;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.translation.TranslationKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictTextKindUsageList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsProductModelHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductFactoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractMainDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.dialog.DIALOGImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;

import java.util.*;

/**
 * Master Importer für einen Baureihe. Übernimmt das Entpacken der TAR.GZ-Datei und die Ansteuerung der einzelnen Importer
 */
public class MigrationDialogSeriesMainImporter extends AbstractMainDataImporter implements iPartsConst, EtkDbConst {

    //    private Map<String, EtkFunctionImportHelper> importerList;
    private Map<DictTextKindTypes, String> allUsedDictionaryEntries;
    private iPartsCatalogImportWorker catalogImportWorker;
    private Set<String> messageSet;
    private boolean importToDB = true; //sollen die Daten abgespeichert werden?
    private boolean mergingProducts;

    public MigrationDialogSeriesMainImporter(EtkProject project) {
        super(project, "DIALOG Baureihe",
              new FilesImporterFileListType("table", "!!DIALOG Baureihe", true, false, false,
                                            new String[]{ MimeTypes.EXTENSION_GZ, MimeTypes.EXTENSION_ALL_FILES }));
        allUsedDictionaryEntries = new HashMap<>();
        // Für Logfile-Einträge, nicht versorgungsrelevant gekennzeichnete Baureihen sollen nur einmalig ausgeben werden.
        messageSet = new HashSet<>();
    }

    private void initImporterMapping() {
        importerList = new LinkedHashMap<>();

        // Die meisten Unter-Importer müssen bei der Zusammenführung von Produkten nicht ausgeführt werden, da die Daten
        // über catalogImportWorker.getCatalogImportWorkerToMerge() abgerufen werden können; lediglich BTDP, RPOS, TTEL
        // sowie POSD müssen für den Aufbau der zusammengeführten Stücklisten inkl. Ersetzungen und Mitlieferteilen
        // ausgeführt werden
        if (!mergingProducts) {
            // zuerst die Produkt-Steuer Datei
            importerList.put("produkt_steuer", new EtkFunctionImportHelper("iPartsMigrationDIALOG_Catalogue_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogProductImporter(getProject(), false, getDatasetDate());
                }

                @Override
                public boolean isFileMandatory() {
                    return true;
                }
            });
            addUsedDictionaryEntries(MigrationDialogProductImporter.getDictionaryEntries());

            // jetzt baureihe_bm, damit evtl. noch nicht vorhandene AS-Baumuster durch die importierten Konstruktions-Baumuster angelegt werden
            importerList.put("baureihe_bm", new EtkFunctionImportHelper("iPartsMigrationDIALOG_SeriesModel_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogProductSeriesModelImporter(getProject(), false);
                }

                @Override
                public boolean isFileMandatory() {
                    return true;
                }
            });
            addUsedDictionaryEntries(MigrationDialogProductSeriesModelImporter.getDictionaryEntries());

            // dann die Baumuster zum Produkt
            importerList.put("produkt_bm", new EtkFunctionImportHelper("iPartsMigrationDIALOG_BMRE_TDAT_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogProductModelsImporter(getProject(), false);
                }

                @Override
                public boolean isFileMandatory() {
                    return true;
                }
            });
            addUsedDictionaryEntries(MigrationDialogProductModelsImporter.getDictionaryEntries());
        }

        // Katalog Referenz
        importerList.put("baureihe_btdp", new EtkFunctionImportHelper("iPartsMigrationDIALOG_BTDP_Importer") {
            @Override
            public AbstractCatalogDataImporter createImporter() {
                return new MigrationDialogBTDPImporter(getProject(), false);
            }

            @Override
            public boolean isFileMandatory() {
                return true;
            }
        });
        addUsedDictionaryEntries(MigrationDialogBTDPImporter.getDictionaryEntries());

        if (!mergingProducts) {
            // baureihe_sctd (Fehlerorte)  MUSS VOR POSD laufen
            importerList.put("baureihe_sctd", new EtkFunctionImportHelper("iPartsMigrationDIALOG_SCTD_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogSCTDImporter(getProject(), false);
                }

                @Override
                public boolean isFileMandatory() {
                    return false;
                }
            });
            addUsedDictionaryEntries(MigrationDialogSCTDImporter.getDictionaryEntries());

            // baureihe_diaf (Fussnoten)  MUSS VOR POSD laufen
            importerList.put("baureihe_diaf", new EtkFunctionImportHelper("iPartsMigrationDIALOG_DIAF_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogDIAFImporter(getProject(), false);
                }

                @Override
                public boolean isFileMandatory() {
                    return false;
                }
            });
            addUsedDictionaryEntries(MigrationDialogDIAFImporter.getDictionaryEntries());
        }

        // baureihe_rpos  MUSS VOR POSD laufen
        importerList.put("baureihe_rpos", new EtkFunctionImportHelper("iPartsMigrationDIALOG_RPos_Importer") {
            @Override
            public AbstractCatalogDataImporter createImporter() {
                return new MigrationDialogRPosImporter(getProject(), false);
            }

            @Override
            public boolean isFileMandatory() {
                return false;
            }
        });
        addUsedDictionaryEntries(MigrationDialogRPosImporter.getDictionaryEntries());


        // baureihe_ttel  MUSS VOR POSD laufen
        importerList.put("baureihe_ttel", new EtkFunctionImportHelper("iPartsMigrationDIALOG_TTel_Importer") {
            @Override
            public AbstractCatalogDataImporter createImporter() {
                return new MigrationDialogTTelImporter(getProject(), false);
            }

            @Override
            public boolean isFileMandatory() {
                return false;
            }
        });
        addUsedDictionaryEntries(MigrationDialogTTelImporter.getDictionaryEntries());

        // POSD Daten (Stücklisten)
        importerList.put("baureihe_posd", new EtkFunctionImportHelper("iPartsMigrationDIALOG_POSD_Importer") {
            @Override
            public AbstractCatalogDataImporter createImporter() {
                return new MigrationDialogPosDImporter(getProject(), false);
            }

            @Override
            public boolean isFileMandatory() {
                return true;
            }
        });
        addUsedDictionaryEntries(MigrationDialogPosDImporter.getDictionaryEntries());

        if (!mergingProducts) {
            // produkt_werke
            importerList.put("produkt_werke", new EtkFunctionImportHelper("iPartsMigrationDIALOG_Factories_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogProductFactoriesImporter(getProject(), false);
                }

                @Override
                public boolean isFileMandatory() {
                    return false;
                }
            });
            addUsedDictionaryEntries(MigrationDialogProductFactoriesImporter.getDictionaryEntries());

            // PODW Daten (Werkseinsatzdaten)
            importerList.put("baureihe_podw", new EtkFunctionImportHelper("iPartsMigrationDIALOG_PODW_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogPODWImporter(getProject(), false);
                }

                @Override
                public boolean isFileMandatory() {
                    return false;
                }
            });
            addUsedDictionaryEntries(MigrationDialogPODWImporter.getDictionaryEntries());

            // baureihe_ftte
            importerList.put("baureihe_ftte", new EtkFunctionImportHelper("iPartsMigrationDIALOG_ColorTableFactory_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogProductColorTableFactoryImporter(getProject(), false);
                }

                @Override
                public boolean isFileMandatory() {
                    return false;
                }
            });
            addUsedDictionaryEntries(MigrationDialogProductColorTableFactoryImporter.getDictionaryEntries());

            // baureihe_ftab
            importerList.put("baureihe_ftab", new EtkFunctionImportHelper("iPartsMigrationDIALOG_ColorTableFactoryFTab_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogProductColorTableFactoryFTabImporter(getProject(), false);
                }

                @Override
                public boolean isFileMandatory() {
                    return false;
                }
            });
            addUsedDictionaryEntries(MigrationDialogProductColorTableFactoryFTabImporter.getDictionaryEntries());

            // baureihe_fbst
            importerList.put("baureihe_fbst", new EtkFunctionImportHelper("iPartsMigrationDIALOG_FBST_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogFBSTImporter(getProject(), false);
                }

                @Override
                public boolean isFileMandatory() {
                    return false;
                }
            });
            addUsedDictionaryEntries(MigrationDialogFBSTImporter.getDictionaryEntries());

            // Wichtig: Reihenfolge beachten. PEMQ muss vor PEMZ ausgeführt werden
            // Seit DAIMLER-6897 müssen PEMQ und PEMZ auch nach FTAB und FTTE laufen
            // PEMQ Daten (Rückmeldedaten Idents)
            final HashSet<iPartsCatalogImportWorker.PemForSeries> processedPEMSs = new HashSet<>();  // PEMs, die von diesem Import betroffen sind (wird von Importer gefüllt)
            importerList.put("baureihe_pemq", new EtkFunctionImportHelper("iPartsMigrationDIALOG_PEMQ_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogResponseIdentsImporter(getProject(), false, processedPEMSs);
                }

                @Override
                public boolean isFileMandatory() {
                    return false;
                }
            });
            addUsedDictionaryEntries(MigrationDialogResponseIdentsImporter.getDictionaryEntries());

            // PEMZ Daten (Rückmeldedaten Ausreißer)
            importerList.put("baureihe_pemz", new EtkFunctionImportHelper("iPartsMigrationDIALOG_PEMZ_Importer") {
                @Override
                public AbstractCatalogDataImporter createImporter() {
                    return new MigrationDialogResponseSpikesImporter(getProject(), false, processedPEMSs);
                }

                @Override
                public boolean isFileMandatory() {
                    return false;
                }
            });
            addUsedDictionaryEntries(MigrationDialogResponseSpikesImporter.getDictionaryEntries());
        }
    }

    private void addUsedDictionaryEntries(Map<DictTextKindTypes, String> usedDictionaryEntries) {
        if ((usedDictionaryEntries != null) && !usedDictionaryEntries.isEmpty()) {
            for (Map.Entry<DictTextKindTypes, String> elem : usedDictionaryEntries.entrySet()) {
                allUsedDictionaryEntries.put(elem.getKey(), elem.getValue());
            }
        }
    }

    public void addErrorCount(int addErrors) {
        errorCount += addErrors;
    }

    public void addWarningCount(int addWarnings) {
        warningCount += addWarnings;
    }

    @Override
    protected void setMustCheckData(AbstractKeyValueRecordReader importer) {
    }

    @Override
    protected boolean importTableIsValid(AbstractKeyValueRecordReader importer) {
        //Überprüfung der Lexika
        DictTxtKindIdByMADId dictTxtKindIdByMADId = DictTxtKindIdByMADId.getInstance(getProject());

        if (!allUsedDictionaryEntries.isEmpty()) {
            Set<DictTextKindTypes> dictKinds = allUsedDictionaryEntries.keySet();
            if (!dictTxtKindIdByMADId.checkDictionariesExistsWithErrorLogMessageForMAD(getMessageLog(), getLogLanguage(),
                                                                                       dictKinds)) {
                return false;
            }
            boolean result = true;
            for (DictTextKindTypes textKindType : dictKinds) {
                iPartsDataDictTextKindUsageList usageList = dictTxtKindIdByMADId.getTxtKindUsages(textKindType);
                String usage = allUsedDictionaryEntries.get(textKindType);
                if (!StrUtils.isEmpty(usage)) {
                    if (!usageList.containsField(usage)) {
                        getMessageLog().fireMessage(translateForLog("!!Fehlende Verwendung %1 bei Textart %2 im Lexikon", usage, translateForLog(textKindType.getTextKindName())),
                                                    MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                        result = false;
                    }
                }
            }
            return result;
        }
        return true;
    }

    @Override
    protected boolean isDialogRecordValid(AbstractKeyValueRecordReader importer, Map<String, String> importRec, List<String> errors) {
        return true;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        importList = new LinkedHashMap<>();
        // Map in der die gefundenen Dateien nach Baureihe sortiert sind
        Map<String, Set<String>> seriesAndFiles = new HashMap<>();
        for (int lfdNr = 0; lfdNr < importRec.size(); lfdNr++) {
            String fileName = importRec.get(KeyValueRecordGzTarFileReader.FILE_PREFIX_IDENTIFIER + lfdNr);
            DWFile importFile = DWFile.get(fileName);
            // Name der Datei
            String importName = getImportNameFromFilename(importFile);
            // Aus dem Dateinamen extrahierte Baureihe
            String tempSeries = getSeriesFromFilename(importFile);
            importList.put(importName, importFile);
            Set<String> fileSet = seriesAndFiles.get(tempSeries);
            if (fileSet == null) {
                fileSet = new HashSet<>();
                seriesAndFiles.put(tempSeries, fileSet);
            }
            fileSet.add(importName);
        }

        // Check, ob in einem Baureihenimporter fälschlicherweise verschiedene Baureihen vorkommen
        iPartsSeriesId seriesId = checkMultipleSeriesInImportFile(seriesAndFiles);
        if (!seriesId.isValidId()) {
            getMessageLog().fireMessage(translateForLog("!!Keine gültige Baureihe gefunden."),
                                        MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            cancelImport();
            return;
        }

        // Baureihen, die "versorgungsrelevant" gekennzeichnet sind, dürfen nicht mehr über die Migration "zerschossen" werden.
        MigrationDialogSeriesImporterHelper seriesImporterHelper = new MigrationDialogSeriesImporterHelper(getProject(), importRec, TABLE_DA_DIALOG);
        String seriesNumber = seriesId.getSeriesNumber();
        boolean versorgungsrelevant = messageSet.contains(seriesNumber) || seriesImporterHelper.checkImportRelevanceForSeries(seriesNumber,
                                                                                                                              messageSet, this);
        if (versorgungsrelevant) {
            // Super wichtig, weil ansonsten in postImportTask() noch ein catalogImportWorker von der vorherigen Baureihe
            // bei Mehrfachauswahl vorhanden wäre
            catalogImportWorker = null;

            // Die Meldung nur einmalig ausgeben
            if (!messageSet.contains(seriesNumber)) {
                getMessageLog().fireMessage(translateForLog("!!DIALOG-Baureihe \"%1\" ist als versorgungsrelevant gekennzeichnet und darf nicht mehr über MAD migriert werden.",
                                                            seriesNumber),
                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                messageSet.add(seriesNumber);
            }
            return;

        } else {
            if (!mergingProducts) {
                checkImporter();

                catalogImportWorker = new iPartsCatalogImportWorker(getProject(), getDatasetDate());
                catalogImportWorker.setSeriesId(seriesId);

                if (!checkMandatoryImportFiles()) {
                    cancelImport();
                    return;
                }
            } else {
                createMergedProducts();
                // catalogImportWorker wurde bereits in startMergeProductsImporter() erzeugt
            }

            getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);

            for (Map.Entry<String, EtkFunction> entry : importerList.entrySet()) {
                if (isCancelled()) {
                    break;
                }
                if (Thread.currentThread().isInterrupted()) {
                    cancelImport("!!Import-Thread wurde frühzeitig beendet");
                    return;
                }

                DWFile importFile = importList.get(entry.getKey());
                if (importFile != null) {
                    EtkFunctionImportHelper functionImportHelper = (EtkFunctionImportHelper)entry.getValue();
                    currentFunctionImportHelper = functionImportHelper;
                    functionImportHelper.importFile = importFile;
                    functionImportHelper.catalogImportWorker = catalogImportWorker;
                    functionImportHelper.run(null);  //mainWindow
                    // Fehler und Warnungen werden schon direkt über das gemeinsame EtkMessageLog hochgezählt
                    if (functionImportHelper.isCanceled || (functionImportHelper.errorCount > 0)) {
                        cancelImport();
                        return;
                    }
                } else if (!mergingProducts) { // Bei der Zusammenführung werden nur einige Unter-Importer verwendet
                    getMessageLog().fireMessage(translateForLog("!!Keine Datei für Importer \"%1\" vorhanden", entry.getKey()),
                                                MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                    getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);
                }
            }
        }
    }

    /**
     * Überprüft, ob in einem Baureihenimporter fälschlicherweise mehrere verschiedene Baureihen vorkommen. Falls ja,
     * dann wir die Baureihe bevorzugt, die die meisten Importdateien besitzt.
     *
     * @param seriesAndFiles
     * @return
     */
    private iPartsSeriesId checkMultipleSeriesInImportFile(Map<String, Set<String>> seriesAndFiles) {
        String series = "";
        if (seriesAndFiles.size() > 1) {
            String differentSeries = StrUtils.makeDelimitedString(", ", ArrayUtil.toStringArray(seriesAndFiles.keySet()));
            getMessageLog().fireMessage(translateForLog("!!Dateien für verschiedene Baureihen innerhalb einer Baureihe gefunden: %1", differentSeries),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            Set<String> setWithMaxInput = null;
            for (Map.Entry<String, Set<String>> entry : seriesAndFiles.entrySet()) {
                if (setWithMaxInput == null) {
                    setWithMaxInput = entry.getValue();
                    series = entry.getKey();
                    continue;
                }
                boolean replaceSet = setWithMaxInput.size() < entry.getValue().size();
                Set<String> setForDeletion = replaceSet ? setWithMaxInput : entry.getValue();
                for (String importname : setForDeletion) {
                    importList.remove(importname);
                }
                if (replaceSet) {
                    setWithMaxInput = entry.getValue();
                    series = entry.getKey();
                }
            }
            getMessageLog().fireMessage(translateForLog("!!Es werden nur die Dateien der Baureihe \"%1\" für den Import verwendet.", series),
                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
        } else if (seriesAndFiles.size() == 1) {
            series = seriesAndFiles.keySet().iterator().next();
        }
        return new iPartsSeriesId(series);
    }

    private boolean checkMandatoryImportFiles() {
        boolean result = true;
        for (Map.Entry<String, EtkFunction> entry : importerList.entrySet()) {
            EtkFunctionImportHelper functionImportHelper = (EtkFunctionImportHelper)entry.getValue();
            DWFile importFile = importList.get(entry.getKey());
            if ((importFile == null) && functionImportHelper.isFileMandatory()) {
                getMessageLog().fireMessage(translateForLog("!!Keine Datei für Importer \"%1\" vorhanden.", entry.getKey()),
                                            MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                getMessageLog().fireMessage(translateForLog("!!Der Importer \"%1\" ist für den Baureihen-Import unbedingt nötig!", entry.getKey()),
                                            MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                result = false;
            }
        }
        return result;
    }

    @Override
    protected void preImportTask() {
        super.preImportTask();
        initImporterMapping();
    }

    @Override
    protected void postImportTask() {
        messageSet.clear();
        if (!isCancelled()) {
            if (!importToDB) {
                cancelImport(translateForLog("!!Abbruch/Rollback im Test"));
            }
        }
        super.postImportTask();

        if (!isCancelled()) {
            try {
                // Bisherige Transaktion explizit committen (und danach eine neue Transaktion starten, damit der generische
                // Import-Wrapper am Ende keine Probleme wegen fehlender Transaktion hat), damit alle Daten der Produkte in
                // der DB abgespeichert sind bevor das Synchronisieren der Bildreferenzen gestartet wird
                getProject().getDbLayer().commit();
                getProject().getDbLayer().startTransaction();
                syncProductModelToModelData();
                if (catalogImportWorker != null) {
                    catalogImportWorker.finishSeriesImport(this);
                }
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                cancelImport(e.getMessage());
            }
        }
        clearCatalogWorker();
    }

    @Override
    public boolean finishImport() {
        String seriesNumber = "";
        if (catalogImportWorker != null) {
            seriesNumber = catalogImportWorker.getSeriesId().getSeriesNumber();
        }

        // Produkte zusammenführen?
        boolean startMergeProducts = !isCancelled() && !mergingProducts && (catalogImportWorker != null) && catalogImportWorker.isMergeProductsForSeries();
        if (startMergeProducts) {
            getMessageLog().fireMessage(TranslationKeys.LINE_SEPARATOR);
            getMessageLog().fireMessage(translateForLog("!!Für die Baureihe \"%1\" sollen die Produkte zusammengeführt werden. Der Import für die Zusammenführung wird nach diesem Import automatisch gestartet.",
                                                        seriesNumber), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }

        boolean result = super.finishImport(false); // Keine Caches löschen bei diesem Importer

        // Produkte zusammenführen starten?
        if (startMergeProducts) {
            // Diese beiden Log-Meldungen erscheinen nur im Dialog, aber nicht in der Log-Datei, weil diese bereits beendet wurde
            getMessageLog().fireMessage("\n");
            getMessageLog().fireMessageWithSeparators(translateForLog("!!Zusammenführung der Produkte für die Baureihe \"%1\"",
                                                                      seriesNumber), MessageLogOption.TIME_STAMP);
            startMergeProductsImporter();
        }

        // Jetzt erst catalogImportWorker auf null setzen, weil er in startMergeProductsImporter() noch benötigt wird
        catalogImportWorker = null;

        return result;
    }

    @Override
    protected void clearCaches() {
        super.clearCaches();
        if (!mergingProducts) {
            ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(DictTextKindTypes.NEUTRAL_TEXT));
        }
    }

    private void clearCatalogWorker() {
        if (catalogImportWorker != null) {
            catalogImportWorker.clearDiffMap();
        }
    }

    @Override
    public void cancelImport(String message, MessageLogType messageLogType) {
        clearCatalogWorker();
        super.cancelImport(message, messageLogType);
    }

    /**
     * Synchronisiert die gemeinsamen Felder von DA_MODEL und DA_PRODUCT_MODEL
     */
    private void syncProductModelToModelData() {
        setBufferedSave(true);
        // Nachbehandlung für Models und ProductModels
        if (catalogImportWorker != null) {
            int oldSkippedRecords = skippedRecords;
            Set<iPartsModelId> processedModels = catalogImportWorker.getProcessedModels();
            for (iPartsModelId modelId : processedModels) {
                iPartsDataModel dataModel = new iPartsDataModel(getProject(), modelId);
                // Sollte iegentlich nie passieren, weil der CatalogWorker während dem Import die Existenz der Baumuster prüft
                if (!dataModel.existsInDB()) {
                    continue;
                }
                iPartsDataProductModelsList productModelsList = iPartsDataProductModelsList.loadDataProductModelsList(getProject(), modelId);
                iPartsProductModelHelper.syncProductModelsWithModel(dataModel, productModelsList);
                if (importToDB) {
                    saveToDB(dataModel);
                    for (iPartsDataProductModels productModel : productModelsList) {
                        saveToDB(productModel);
                    }
                }
            }
            skippedRecords = oldSkippedRecords;
        }
        super.postImportTask();
    }

    @Override
    protected void logImportRecordsFinished(int importRecordCount) {
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals("table")) {
            // Es kann sein, dass ein "höherer" Importer das Datum schon gesetzt hat. Falls nicht, versuche das Datum
            // aus dem Dateinamen zu extrahieren.
            if (getDatasetDate() == null) {
                setDatasetDate(iPartsMADDateTimeHandler.extractDateFromFilename(importFile.getPath(), false, this));
            }
            return importMasterData(prepareImporterGZTar(importFile, "table"));
        }
        return false;
    }

    @Override
    public String getImportName(String language) {
        String importName = super.getImportName(language);
        if (mergingProducts) {
            importName += " (" + TranslationHandler.translateForLanguage("!!Zusammenführung", language) + ")";
        }
        return importName;
    }

    /**
     * Extrahiert den Importnamen aus dem Dateinamen
     *
     * @param importFile
     * @return
     */
    private String getImportNameFromFilename(DWFile importFile) {
        String[] split = getFilenameToken(importFile);
        String importName = importFile.extractFileName(false);
        if (split.length >= 3) {
            importName = split[1] + "_" + split[2];
        }
        return importName.toLowerCase();
    }

    /**
     * Extrahiert die Baureihe aus dem Dateinamen
     *
     * @param importFile
     * @return
     */
    private String getSeriesFromFilename(DWFile importFile) {
        String[] split = getFilenameToken(importFile);
        if (split.length >= 1) {
            return split[0];
        }
        return "";
    }

    private String[] getFilenameToken(DWFile importFile) {
        String importName = importFile.extractFileName(false);
        String[] split = StrUtils.toStringArray(importName, "_", false);
        return split;

    }

    private void startMergeProductsImporter() {
        clearCaches(); // Vor dem Zusammenführen müssen die Caches gelöscht werden
        MigrationDialogSeriesMainImporter mergeProductsImporter = new MigrationDialogSeriesMainImporter(getProject());
        mergeProductsImporter.mergingProducts = true;
        mergeProductsImporter.catalogImportWorker = new iPartsCatalogImportWorker(catalogImportWorker);
        DWFile runningLogFile = mergeProductsImporter.importJobRunning();
        EtkMessageLog messageLog = new EtkMessageLog();
        mergeProductsImporter.initImport(messageLog);

        // Damit die Logausgaben und Fortschritte auch im Dialog des aufrufenden Importers angezeigt werden, aber nicht in der Log-Datei
        setLogFile(null, false);
        MessageEvent messageListener = new MessageEvent() {
            @Override
            public void fireEvent(MessageEventData event) {
                getMessageLog().fireMessage(event);
            }
        };
        messageLog.addMessageEventListener(messageListener);
        ProgressEvent progressListener = new ProgressEvent() {
            @Override
            public void fireEvent(ProgressEventData event) {
                getMessageLog().fireProgress(event);
            }
        };
        messageLog.addProgressEventListener(progressListener);

        try {
            mergeProductsImporter.importFiles(mergeProductsImporter.getImportFileTypes()[0], importFilesMap.values().iterator().next(), false);
        } finally {
            mergeProductsImporter.finishImport();
            messageLog.removeMessageEventListener(messageListener);
            messageLog.removeProgressEventListener(progressListener);
            if (mergeProductsImporter.getErrorCount() == 0) { // -> ProcessedLogs
                mergeProductsImporter.setLogFile(iPartsJobsManager.getInstance().jobProcessed(runningLogFile), false);
            } else { // -> ErrorLogs
                mergeProductsImporter.setLogFile(iPartsJobsManager.getInstance().jobError(runningLogFile), false);
            }
        }
    }

    private void createMergedProducts() {
        String seriesNumber = catalogImportWorker.getSeriesId().getSeriesNumber();
        getMessageLog().fireMessage(translateForLog("!!Zusammenführung der Produkte für die Baureihe \"%1\" gestartet...",
                                                    seriesNumber), MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        getMessageLog().fireMessage(translateForLog("!!Bei der Zusammenführung müssen nur die folgenden Import-Dateien neu importiert werden: BTDP, RPOS, TTEL, POSD"),
                                    MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

        // Produkt-IDs für die zusammengeführten Produkte ermitteln und Original-Produkt-IDs dazu merken
        Map<iPartsProductId, Set<iPartsProductId>> mergedProductIdsToOriginalProductIdsList = new LinkedHashMap<>();
        Map<iPartsProductId, Set<String>> productsToAAMap = catalogImportWorker.getCatalogImportWorkerToMerge().getProductManagement().aaMap;
        for (Map.Entry<iPartsProductId, Set<String>> productToAAEntry : productsToAAMap.entrySet()) {
            iPartsProductId productId = productToAAEntry.getKey();
            Set<String> aaSet = productToAAEntry.getValue();
            if (aaSet.isEmpty()) { // Kann eigentlich nicht passieren
                continue;
            }

            // Jedes Produkt darf nur eine Ausführungsart beinhalten
            if (aaSet.size() > 1) {
                cancelImport(translateForLog("!!Die Produkte für die Baureihe \"%1\" können nicht zusammengeführt werden, weil mindestens das Produkt \"%2\" mehr als eine Ausführungsart beinhaltet: %3",
                                             seriesNumber, productId.getProductNumber(),
                                             StrUtils.stringListToString(aaSet, ", ")), MessageLogType.tmlError);
                return;
            }

            iPartsProductId mergedProductId = new iPartsProductId(seriesNumber + "_" + aaSet.iterator().next());
            Set<iPartsProductId> originalProductIds = mergedProductIdsToOriginalProductIdsList.get(mergedProductId);
            if (originalProductIds == null) {
                originalProductIds = new TreeSet<>();
                mergedProductIdsToOriginalProductIdsList.put(mergedProductId, originalProductIds);
            }
            originalProductIds.add(productId);
            catalogImportWorker.getOriginalProductsToMergedProductsMap().put(productId.getProductNumber(), mergedProductId.getProductNumber());
        }

        // Gibt es überhaupt Produkte zum zusammenführen?
        if (catalogImportWorker.getOriginalProductsToMergedProductsMap().isEmpty()) {
            cancelImport(translateForLog("!!Es konnte kein zusammengeführtes Produkt für die Baureihe \"%1\" erzeugt werden, da kein Produkt mit gültiger Ausführungsart existiert.",
                                         seriesNumber), MessageLogType.tmlError);
            return;
        }

        // Produkt-Stammdaten für die zusammengeführten Produkte bestimmen und speichern
        EtkProject project = getProject();
        List<String> databaseLanguages = project.getConfig().getDatabaseLanguages();
        for (Map.Entry<iPartsProductId, Set<iPartsProductId>> mergedProductEntry : mergedProductIdsToOriginalProductIdsList.entrySet()) {
            iPartsProductId mergedProductId = mergedProductEntry.getKey();
            iPartsDataProduct mergedDataProduct = new iPartsDataProduct(project, mergedProductId);
            if (mergedDataProduct.existsInDB()) {
                catalogImportWorker.loadExistingModuleReferencesForProduct(mergedDataProduct);
                mergedDataProduct.loadChildren();

                // Bisherige Werke sowie Baumuster für das zusammengeführte Produkt zunächst löschen
                mergedDataProduct.getProductFactoriesList().deleteAll(DBActionOrigin.FROM_EDIT);
                mergedDataProduct.getProductModelsList().deleteAll(DBActionOrigin.FROM_EDIT);
            } else {
                mergedDataProduct.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

                // Titel entspricht der Produkt-ID
                EtkMultiSprache mergedProductTitle = new EtkMultiSprache(mergedProductId.getProductNumber(), databaseLanguages);
                mergedDataProduct.setFieldValueAsMultiLanguage(FIELD_DP_TITLE, mergedProductTitle, DBActionOrigin.FROM_EDIT);

                mergedDataProduct.setFieldValueAsBoolean(FIELD_DP_PRODUCT_VISIBLE, false, DBActionOrigin.FROM_EDIT);
            }

            // Diese Daten immer setzen (auch wenn das Produkt bereits existiert)
            mergedDataProduct.setFieldValue(FIELD_DP_STRUCTURING_TYPE, PRODUCT_STRUCTURING_TYPE.KG_TU.name(), DBActionOrigin.FROM_EDIT);
            mergedDataProduct.setDocuMethod(iPartsDocumentationType.DIALOG.getDBValue());
            mergedDataProduct.setFieldValue(FIELD_DP_SOURCE, iPartsImportDataOrigin.MAD.getOrigin(), DBActionOrigin.FROM_EDIT);
            mergedDataProduct.setFieldValueAsBoolean(FIELD_DP_MIGRATION, true, DBActionOrigin.FROM_EDIT);
            mergedDataProduct.setFieldValue(FIELD_DP_SERIES_REF, seriesNumber, DBActionOrigin.FROM_EDIT);
            mergedDataProduct.refreshModificationTimeStamp();

            // KG/TU-Benennungen von allen Quell-Produkten sammeln und übernehmen (Ausgang ist eine leere Liste, damit
            // doppelte KG/TU-Benennungen in den Quell-Produkten korrekt erkannt werden können)
            iPartsDataKgTuAfterSalesList mergedDataKgTuAfterSalesList = new iPartsDataKgTuAfterSalesList();

            // Daten vom ersten passenden Produkt verwenden sowie Werke und Baumuster inkl. Daten an das zusammengeführte
            // Produkt übernehmen
            boolean firstProduct = true;
            Set<String> productNumbers = new TreeSet<>();
            Set<String> modelNumbers = new TreeSet<>();
            for (iPartsProductId productId : mergedProductEntry.getValue()) {
                iPartsDataProduct dataProduct = new iPartsDataProduct(project, productId);
                if (dataProduct.existsInDB()) {
                    productNumbers.add(productId.getProductNumber());
                    if (firstProduct) {
                        firstProduct = false;

                        // Einige Attribute vom ersten passenden Produkt übernehmen
                        mergedDataProduct.setFieldValue(FIELD_DP_PRODUCT_GRP, dataProduct.getFieldValue(FIELD_DP_PRODUCT_GRP), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValue(FIELD_DP_AGGREGATE_TYPE, dataProduct.getFieldValue(FIELD_DP_AGGREGATE_TYPE), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValue(FIELD_DP_ASSORTMENT_CLASSES, dataProduct.getFieldValue(FIELD_DP_ASSORTMENT_CLASSES), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValue(FIELD_DP_ASPRODUCT_CLASSES, dataProduct.getFieldValue(FIELD_DP_ASPRODUCT_CLASSES), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValue(FIELD_DP_MIGRATION_DATE, dataProduct.getFieldValue(FIELD_DP_MIGRATION_DATE), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValue(FIELD_DP_DATASET_DATE, dataProduct.getFieldValue(FIELD_DP_DATASET_DATE), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValue(FIELD_DP_BRAND, dataProduct.getFieldValue(FIELD_DP_BRAND), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValueAsBoolean(FIELD_DP_KZ_DELTA, dataProduct.getFieldValueAsBoolean(FIELD_DP_KZ_DELTA), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValueAsBoolean(FIELD_DP_IDENT_CLASS_OLD, dataProduct.getFieldValueAsBoolean(FIELD_DP_IDENT_CLASS_OLD), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValueAsBoolean(FIELD_DP_EPC_RELEVANT, dataProduct.getFieldValueAsBoolean(FIELD_DP_EPC_RELEVANT), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValueAsBoolean(FIELD_DP_TTZ_FILTER, dataProduct.getFieldValueAsBoolean(FIELD_DP_TTZ_FILTER), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValueAsBoolean(FIELD_DP_SCORING_WITH_MCODES, dataProduct.getFieldValueAsBoolean(FIELD_DP_SCORING_WITH_MCODES), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValueAsBoolean(FIELD_DP_CAB_FALLBACK, dataProduct.getFieldValueAsBoolean(FIELD_DP_CAB_FALLBACK), DBActionOrigin.FROM_EDIT);
                        mergedDataProduct.setFieldValueAsBoolean(FIELD_DP_SHOW_SAS, dataProduct.getFieldValueAsBoolean(FIELD_DP_SHOW_SAS), DBActionOrigin.FROM_EDIT);
                    }

                    // Werke an das zusammengeführte Produkt übernehmen
                    DBDataObjectList<iPartsDataProductFactory> mergedProductFactoriesList = mergedDataProduct.getProductFactoriesList();
                    DBDataObjectList<iPartsDataProductFactory> dataProductFactoriesList = dataProduct.getProductFactoriesList();
                    for (iPartsDataProductFactory dataProductFactory : dataProductFactoriesList) {
                        iPartsProductFactoryId mergedProductFactoryId = new iPartsProductFactoryId(mergedProductId.getProductNumber(),
                                                                                                   dataProductFactory.getAsId().getFactoryNumber());
                        if (!mergedProductFactoriesList.containsId(mergedProductFactoryId)) {
                            iPartsDataProductFactory mergedDataProductFactory = dataProductFactory.cloneMe(project);
                            mergedDataProductFactory.setId(mergedProductFactoryId, DBActionOrigin.FROM_EDIT);
                            mergedDataProductFactory.updateOldId(); // Durch PK-Änderung würden Original-Daten sonst gelöscht werden
                            mergedDataProductFactory.__internal_setNew(true); // Alle Werke zum zusammengeführten Produkt wurden vorher gelöscht
                            mergedProductFactoriesList.add(mergedDataProductFactory, DBActionOrigin.FROM_EDIT);
                        }
                    }

                    // Baumuster an das zusammengeführte Produkt übernehmen mit Log-Meldung bei mehrfacher Verwendung desselben Baumusters
                    DBDataObjectList<iPartsDataProductModels> mergedProductModelsList = mergedDataProduct.getProductModelsList();
                    DBDataObjectList<iPartsDataProductModels> dataProductModelsList = dataProduct.getProductModelsList();
                    for (iPartsDataProductModels dataProductModels : dataProductModelsList) {
                        iPartsProductModelsId mergedProductModelsId = new iPartsProductModelsId(mergedProductId.getProductNumber(),
                                                                                                dataProductModels.getAsId().getModelNumber());
                        if (!mergedProductModelsList.containsId(mergedProductModelsId)) {
                            modelNumbers.add(mergedProductModelsId.getModelNumber());
                            iPartsDataProductModels mergedDataProductModels = dataProductModels.cloneMe(project);
                            mergedDataProductModels.setId(mergedProductModelsId, DBActionOrigin.FROM_EDIT);
                            mergedDataProductModels.updateOldId(); // Durch PK-Änderung würden Original-Daten sonst gelöscht werden
                            mergedDataProductModels.__internal_setNew(true); // Alle Baumuster zum zusammengeführten Produkt wurden vorher gelöscht
                            mergedProductModelsList.add(mergedDataProductModels, DBActionOrigin.FROM_EDIT);
                        } else {
                            getMessageLog().fireMessage(translateForLog("!!Das Baumuster \"%1\" für das zusammengeführte Produkt \"%2\" wird in mehreren Quell-Produkten verwendet.",
                                                                        dataProductModels.getAsId().getModelNumber(), mergedProductId.getProductNumber()),
                                                        MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                        }
                    }

                    // KG-TU-Benennungen für das zusammengeführte Produkt übernehmen
                    iPartsDataKgTuAfterSalesList dataKgTuAfterSalesList = iPartsDataKgTuAfterSalesList.loadKgTuForProductListWithTexts(project,
                                                                                                                                       dataProduct.getAsId());
                    for (iPartsDataKgTuAfterSales dataKgTuAfterSales : dataKgTuAfterSalesList) {
                        iPartsDataKgTuAfterSalesId mergedKgTuAfterSalesId = new iPartsDataKgTuAfterSalesId(mergedProductId.getProductNumber(),
                                                                                                           dataKgTuAfterSales.getAsId().getKg(),
                                                                                                           dataKgTuAfterSales.getAsId().getTu());
                        if (!mergedDataKgTuAfterSalesList.containsId(mergedKgTuAfterSalesId)) {
                            iPartsDataKgTuAfterSales mergedDataKgTuAfterSales = dataKgTuAfterSales.cloneMe(project);
                            mergedDataKgTuAfterSales.setId(mergedKgTuAfterSalesId, DBActionOrigin.FROM_EDIT);
                            mergedDataKgTuAfterSales.updateOldId(); // Durch PK-Änderung würden Original-Daten sonst gelöscht werden

                            // existsInDB() muss auf einer neuen Instanz von iPartsDataKgTuAfterSales aufgerufen werden,
                            // weil durch das Klonen mergedDataKgTuAfterSales sonst immer true zurückliefern würde
                            mergedDataKgTuAfterSales.__internal_setNew(!(new iPartsDataKgTuAfterSales(project, mergedKgTuAfterSalesId).existsInDB()));

                            mergedDataKgTuAfterSalesList.add(mergedDataKgTuAfterSales, DBActionOrigin.FROM_EDIT);
                        } else {
                            // KG/TU-Benennung vergleichen falls diese bereits existiert
                            iPartsDataKgTuAfterSales existingDataKgTuAfterSales = mergedDataKgTuAfterSalesList.getById(mergedKgTuAfterSalesId);
                            EtkMultiSprache existingTitle = existingDataKgTuAfterSales.getFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC);
                            EtkMultiSprache newTitle = dataKgTuAfterSales.getFieldValueAsMultiLanguage(FIELD_DA_DKM_DESC);

                            // Vergleich nicht über equalContent() machen, weil dort auch die Text-ID geprüft wird, die
                            // durchaus eine andere sein könnte
                            if (!existingTitle.getLanguagesAndTexts().equals(newTitle.getLanguagesAndTexts())) {
                                String kgTuString = mergedKgTuAfterSalesId.getKg();
                                if (!mergedKgTuAfterSalesId.getTu().isEmpty()) {
                                    kgTuString += "/" + mergedKgTuAfterSalesId.getTu();
                                }
                                getMessageLog().fireMessage(translateForLog("!!Die KG/TU-Benennung für \"%1\" ist nicht eindeutig: \"%2\" <> \"%3\"",
                                                                            kgTuString, existingTitle.getText(getLogLanguage()),
                                                                            newTitle.getText(getLogLanguage())),
                                                            MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                            }
                        }
                    }
                }
            }

            boolean mergedProductWasNew = mergedDataProduct.isNew();
            saveToDB(mergedDataProduct);
            mergedDataKgTuAfterSalesList.saveToDB(project);
            catalogImportWorker.getProductManagement().addProduct(mergedDataProduct, DBActionOrigin.FROM_EDIT);

            getMessageLog().fireMessage(translateForLog("!!Zusammengeführtes Produkt \"%1\" für die Produkte \"%2\" und Baumuster \"%3\" wurde %4",
                                                        mergedProductId.getProductNumber(), StrUtils.stringListToString(productNumbers, ", "),
                                                        StrUtils.stringListToString(modelNumbers, ", "),
                                                        translateForLog(mergedProductWasNew ? "!!erzeugt" : "!!aktualisiert")),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        }
    }

    /**
     * Helper-Klasse, um über den DIALOGImportHelper und checkImportRelevanceForSeries()
     * auf den iPartsDIALOGSeriesValidityCache zugreifen zu können.
     */
    private class MigrationDialogSeriesImporterHelper extends DIALOGImportHelper {

        public MigrationDialogSeriesImporterHelper(EtkProject project, Map<String, String> mapping, String tableName) {
            super(project, mapping, tableName);
        }
    }


    /**
     * Ermöglicht das Erzeugen einer anonymen Klasse und implementiert für alle Katalog-Importer die run()-Funktion
     */
    private abstract class EtkFunctionImportHelper extends AbstractEtkFunctionImportHelper {

        public DWFile importFile;
        public iPartsCatalogImportWorker catalogImportWorker;

        @Override
        public void run(AbstractJavaViewerForm owner) {
            AbstractDataImporter importer = createImporter();
            if (!(importer instanceof AbstractCatalogDataImporter)) {
                return;
            }

            currentImporter = importer;
            importer.setSingleCall(false);
            ((AbstractCatalogDataImporter)importer).setProgressMessageType(ProgressMessageType.READING);
            ((AbstractCatalogDataImporter)importer).setCatalogImportWorker(catalogImportWorker);

            List<DWFile> selectedFiles = new DwList<DWFile>();
            selectedFiles.add(importFile);
            importer.initImport(getMessageLog());
            try {
                if (importer.importFiles(importer.getImportFileTypes()[0], selectedFiles, false)) {
                    errorCount = importer.getErrorCount();
                    warningCount = importer.getWarningCount();
                } else { // -> Abbruch
                    isCanceled = true;
                }
            } finally {
                importer.finishImport();
            }

        }

        // Hier wird der Importer verkabelt.
        public EtkFunctionImportHelper(String importAliasName) {
            super(importAliasName);
        }

        // Hier ist die Verknüpfung zur anonymen Klasse. Diese wird auf diese Weise gezwungen, isFileMandatory() zu implementieren.
        public abstract boolean isFileMandatory();
    }
}